# StateFlow 源码解析（面试向）

> 基于 `kotlinx-coroutines-core` **1.9.0** 源码。核心实现类：`StateFlowImpl`，位于 `kotlinx/coroutines/flow/StateFlow.kt`；订阅者管理基类 `AbstractSharedFlow`，位于 `flow/internal/AbstractSharedFlow.kt`。

---

## 0. 一句话总览

`StateFlow` 是 `SharedFlow` 的**特化子类**，专门表示「单一状态值」。它**始终持有一个 `value`**，等价于一个 `replay=1, DROP_OLDEST` 且自带去重（`distinctUntilChanged`）的 SharedFlow。但实现上**完全独立**于 `SharedFlowImpl`，是一套为「读写单值」高度优化、几乎零分配的代码（`StateFlowImpl`），用**原子引用 + 序列号**而非环形数组。

官方文档里给出的等价关系：
```kotlin
// MutableStateFlow(initialValue) 约等于：
val shared = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
shared.tryEmit(initialValue)
val state = shared.distinctUntilChanged()
```
注意是「约等于」，真实实现见下文。

---

## 1. 类型层次

```
Flow<T>
  └── SharedFlow<T>
        └── StateFlow<T>              // 只读，新增 val value: T
              └── MutableStateFlow<T> // 可写，var value + compareAndSet
                    （同时继承 MutableSharedFlow<T>）

实现类：StateFlowImpl<T> : AbstractSharedFlow<StateFlowSlot>(), MutableStateFlow<T>, CancellableFlow<T>, FusibleFlow<T>
```

**面试点**：「StateFlow 是 SharedFlow 的子类」是接口继承关系；但 `StateFlowImpl` **并不继承** `SharedFlowImpl`，两者各写各的 `collect`/`emit`。共同点只是都继承了管理订阅者 slot 的 `AbstractSharedFlow`。

构造入口：
```kotlin
public fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = StateFlowImpl(value ?: NULL)
```
- **必须有初始值**（这是和 SharedFlow 最直观的区别）。
- `null` 被包装成内部哨兵 `NULL`，所以 `StateFlow` 可以持有 `null` 值而内部数组不存真正的 `null`。

---

## 2. 核心数据结构：原子引用 + 序列号，而非数组

```kotlin
private class StateFlowImpl<T>(initialState: Any) : ... {
    private val _state = atomic(initialState)  // 当前值：T 或 NULL 哨兵
    private var sequence = 0                    // 序列号，奇数=有更新正在广播中
}
```

对比 SharedFlow 的环形数组，StateFlow **只存一个原子引用 `_state`**。这就是文档所说「allocation-free / 内存友好」的原因——无论更新多少次，都不分配新缓冲对象。

`value` 的读写：
```kotlin
override var value: T
    get() = NULL.unbox(_state.value)
    set(value) { updateState(null, value ?: NULL) }

override fun compareAndSet(expect: T, update: T): Boolean =
    updateState(expect ?: NULL, update ?: NULL)
```

`replayCache` 也极简——永远就是当前值这一个元素：
```kotlin
override val replayCache: List<T> get() = listOf(value)
```

---

## 3. updateState —— 去重 + 序列号「平坦合并」

这是 StateFlow 最核心、面试最值得讲的一段：

```kotlin
private fun updateState(expectedState: Any?, newState: Any): Boolean {
    var curSequence: Int
    var curSlots: Array<StateFlowSlot?>?
    synchronized(this) {
        val oldState = _state.value
        if (expectedState != null && oldState != expectedState) return false // CAS 失败
        if (oldState == newState) return true   // ★ 去重：值没变（equals 相等）直接返回，不通知订阅者
        _state.value = newState                 // 写入新值

        curSequence = sequence
        if (curSequence and 1 == 0) {           // 偶数 = 当前空闲
            curSequence++                        // 变奇数，宣告「我来负责广播」
            sequence = curSequence
        } else {
            // 已经有别的线程在广播了，只需通知它「值又变了，多广播一轮」
            sequence = curSequence + 2           // 保持奇数
            return true                          // 自己直接返回，不参与广播
        }
        curSlots = slots
    }
    // —— 锁外广播 ——
    while (true) {
        curSlots?.forEach { it?.makePending() }  // 通知每个订阅者「有新值待取」
        synchronized(this) {
            if (sequence == curSequence) {       // 期间没有新更新
                sequence = curSequence + 1       // 变回偶数，广播结束
                return true
            }
            curSequence = sequence               // 期间有人改了值，再广播一轮
            curSlots = slots
        }
    }
}
```

**两大设计点（面试必答）**：

### (1) 强一致去重（conflation）
`if (oldState == newState) return true` —— 用 **`equals`** 比较，新值等于旧值就**不触发任何订阅者**。这就是 StateFlow「自动去重」的源头，等价于内建 `distinctUntilChanged`。所以连续 `value = x; value = x` 只通知一次。
> 注意：基于 `equals`，data class 改个字段才算变化；若 class 违反 equals 契约，行为未定义。

### (2) 序列号实现「平坦合并（flat combining）」
多线程并发改 `value` 时，**只有一个线程真正负责广播**（把 sequence 从偶数 +1 变奇数的那个），其余线程只是把 sequence `+2` 留个「待办标记」就走人。负责广播的线程在 `while(true)` 里循环，直到没有新更新（sequence 恢复偶数）才退出。

好处：
- **避免 resume 风暴**：N 个线程同时改值，不会产生 N 轮对所有订阅者的唤醒，而是合并成尽量少的几轮。
- **保证最终一致**：订阅者最终一定能看到最新值（虽然中间的快速变化会被「合并」跳过——这正是 conflate 语义）。

---

## 4. 订阅者 Slot：状态机 _state

`StateFlowSlot` 不像 SharedFlowSlot 那样存「下标 index」，而是一个**四态状态机**（用原子引用 `_state`）：

```kotlin
private class StateFlowSlot : AbstractSharedFlowSlot<StateFlowImpl<*>>() {
    private val _state = WorkaroundAtomicReference<Any?>(null)
    // 四种状态：
    //   null   -- 未使用（可被新订阅者分配）
    //   NONE   -- 已被订阅者占用，既没挂起也没待处理值
    //   PENDING-- 有新值待处理
    //   CancellableContinuationImpl -- 订阅者已挂起，等新值
}
```

三个关键方法：

```kotlin
// 生产者侧：通知该订阅者「有新值了」
fun makePending() {
    _state.loop { state -> when {
        state == null    -> return                 // 空闲 slot，跳过
        state === PENDING-> return                 // 已经是待处理，无需重复
        state === NONE   -> if (CAS(NONE, PENDING)) return        // 标记待处理
        else -> if (CAS(state, NONE)) {            // 订阅者正挂起 → 唤醒它
            (state as CancellableContinuationImpl<Unit>).resume(Unit); return
        }
    }}
}

// 消费者侧：快路径，看看有没有待处理的值
fun takePending(): Boolean = _state.getAndSet(NONE)!! === PENDING

// 消费者侧：没有待处理就挂起
suspend fun awaitPending() = suspendCancellableCoroutine { cont ->
    if (_state.compareAndSet(NONE, cont)) return   // 装入续体，挂起等待
    // CAS 失败说明此刻已变 PENDING，无需挂起，直接 resume
    cont.resume(Unit)
}
```

这套 CAS 状态机让「生产者标记 / 消费者取值 / 消费者挂起」三方在**无锁**（lock-free，针对单个 slot）的情况下安全协作。

> 源码注释提到 `WorkaroundAtomicReference` 是为绕过 Android 上的一个 atomicfu bug（issue #3820）临时引入的。

---

## 5. collect 全流程 —— 为什么「一订阅就拿到当前值」

```kotlin
override suspend fun collect(collector: FlowCollector<T>): Nothing {
    val slot = allocateSlot()
    try {
        if (collector is SubscribedFlowCollector) collector.onSubscription()
        val collectorJob = currentCoroutineContext()[Job]
        var oldState: Any? = null   // 该订阅者上次发出的值；null 表示还没发过
        while (true) {
            val newState = _state.value          // ★ 每轮都读「最新」值，天然合并掉过期值
            collectorJob?.ensureActive()         // 发射前检查取消
            if (oldState == null || oldState != newState) {  // ★ 去重：和自己上次发的不同才发
                //新订阅者第一次循环 oldState=null，必然发出当前值
                collector.emit(NULL.unbox(newState))
                oldState = newState
            }
            if (!slot.takePending()) {           // 快路径：没有待处理值
                slot.awaitPending()              // 才挂起等待
            }
        }
    } finally {
        freeSlot(slot)
    }
}
```

面试关键点：
1. **粘性（永远 replay=1）**：循环第一轮就直接读 `_state.value` 并发射 → 新订阅者**立刻**收到当前值，无需等下一次更新。这是「StateFlow 永远粘性、深度恒为 1」的实现。
2. **双重去重**：除了 `updateState` 里写入时去重，`collect` 里还有一层 `oldState != newState` 的去重。意义在于——订阅者被唤醒时可能值已经又变过几次，它**只看最新值**，中间值被合并掉（慢订阅者跳过快更新）。
3. **conflation / 取最新**：注释明确写道「coroutine 可能等了一会儿才被调度，所以这里用最新 state 以获得最好的合并效果」。这就是「慢收集器跳过中间值，但总能拿到最新值」。
4. `collect` 返回 `Nothing`，热流永不结束，发射前 `ensureActive()` 保证可取消。

---

## 6. emit / tryEmit —— 其实就是写 value

```kotlin
override fun tryEmit(value: T): Boolean { this.value = value; return true } // 永远成功
override suspend fun emit(value: T) { this.value = value }                   // 永不挂起
override fun resetReplayCache() = throw UnsupportedOperationException(...)   // 不支持
```

对比 SharedFlow：
- **StateFlow 的 emit 永不挂起、tryEmit 永远返回 true** —— 因为它只是覆盖单个值（conflated），不存在「缓冲区满」的概念，溢出策略恒为 `DROP_OLDEST`。
- **不支持 `resetReplayCache`**：StateFlow 永远有且仅有一个值，无缓存可重置；想「重置」就直接给 `value` 赋初值。

`update / updateAndGet / getAndUpdate` 是基于 `compareAndSet` 的 CAS 自旋扩展函数，保证并发下原子更新：
```kotlin
public inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
    while (true) {
        val prevValue = value
        val nextValue = function(prevValue)
        if (compareAndSet(prevValue, nextValue)) return
    }
}
```
> 注意：`function` 在高并发下可能被执行多次（CAS 失败重试），所以**它必须是纯函数、无副作用**。

---

## 7. Operator fusion（算子融合）

```kotlin
internal fun <T> StateFlow<T>.fuseStateFlow(context, capacity, onBufferOverflow): Flow<T> {
    if ((capacity in 0..1 || capacity == Channel.BUFFERED) && onBufferOverflow == DROP_OLDEST)
        return this   // 这些操作对 StateFlow 无意义，直接返回自身
    return fuseSharedFlow(...)
}
```

对 StateFlow 应用 `flowOn`、`conflate`、`distinctUntilChanged`、`buffer(CONFLATED/RENDEZVOUS)`、`cancellable` **全部无效**——因为 StateFlow 本身就是 conflated + distinct + cancellable 的。这也是面试常考：「为什么给 StateFlow 加 distinctUntilChanged 没用？」答案：算子融合把它优化掉了，StateFlow 自带去重。

---

## 8. StateFlow vs SharedFlow 源码层面对比

| 维度 | StateFlow (`StateFlowImpl`) | SharedFlow (`SharedFlowImpl`) |
|------|------------------------------|-------------------------------|
| 内部存储 | 单个原子引用 `_state` + 序列号 | 环形数组 `buffer` + 多游标下标 |
| 初始值 | 必须有 | 无 |
| replay | 恒为 1，不可改 | 0..N 可配 |
| 去重 | 内建（写入 + collect 双重 `equals` 去重） | 无，需手动 `distinctUntilChanged` |
| 溢出策略 | 恒等价 DROP_OLDEST（覆盖单值） | SUSPEND/DROP_LATEST/DROP_OLDEST 可配 |
| emit 是否可能挂起 | **永不**挂起 | SUSPEND 策略 + 缓冲满时会挂起 |
| resetReplayCache | 抛 UnsupportedOperationException | 支持 |
| 分配 | 几乎零分配（只覆盖引用） | 有缓冲数组、挂起时有 Emitter 对象 |
| 粘性 | 永远粘性，深度=1 | 由 replay 决定，replay=0 即非粘 |
| 并发优化 | 序列号「平坦合并」避免 resume 风暴 | 锁内改状态、锁外 resume |
| 复杂度 | 订阅 O(1)，更新 O(N) | 订阅 O(1) 摊还，emit O(N) |

---

## 9. 面试速答清单

**Q：StateFlow 和 SharedFlow 是什么关系？**
A：接口上 `StateFlow : SharedFlow`，是特化子类；但实现类 `StateFlowImpl` 不继承 `SharedFlowImpl`，是为「单值状态」单独优化的实现，用原子引用而非环形数组。语义上等价于 `replay=1, DROP_OLDEST` 且自带 `distinctUntilChanged` 的 SharedFlow。

**Q：StateFlow 的去重是怎么实现的？**
A：两层。写入时 `updateState` 里 `oldState == newState`（`equals`）相等直接返回不通知；`collect` 里再判 `oldState != newState` 才发射。基于 `equals`，所以 data class 要真正字段变化才算更新。

**Q：为什么新订阅者一订阅就能拿到当前值（粘性）？**
A：`collect` 的循环第一轮直接读 `_state.value` 发射，不等下次更新；本质是 replay=1 恒成立。

**Q：StateFlow 的 emit 会挂起吗？**
A：不会。`emit/tryEmit` 只是覆盖 `value`，conflated 语义没有缓冲满的概念，`tryEmit` 永远 true、`emit` 永不挂起。

**Q：sequence 序列号是干嘛的？**
A：实现「平坦合并」。多线程并发改值时，只有一个线程负责广播（sequence 偶→奇），其余线程把 sequence +2 标记后即返回，避免每次更新都唤醒全部订阅者的 resume 风暴，同时保证订阅者最终看到最新值。

**Q：慢订阅者会丢值吗？**
A：会丢中间值，但永远能拿到最新值。collect 每轮读 `_state.value` 取最新，中间的快速变化被合并跳过（conflation）。

**Q：为什么 StateFlow 不能发一次性事件？**
A：永远粘性 + 去重。Activity 重建重新订阅会立刻重放最后一个值，导致 Toast/导航重复触发；且相同事件因去重发不出去。一次性事件应用 `replay=0` 的 SharedFlow（详见 `StateFlow 和 SharedFlow.md` 的粘性章节）。

**Q：update {} 为什么要写成 CAS 自旋？lambda 会执行几次？**
A：保证并发原子更新；CAS 失败会重试，lambda 可能执行多次，故必须无副作用。

---

## 10. 关联文档
- SharedFlow 的环形数组、缓冲、溢出策略实现，详见 `SharedFlow 源码解析.md`。
- 使用层面对比、粘性事件陷阱、多 collect 误用，详见 `StateFlow 和 SharedFlow.md`。

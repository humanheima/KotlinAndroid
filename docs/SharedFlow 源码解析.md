# SharedFlow 源码解析（面试向）

> 基于 `kotlinx-coroutines-core` **1.9.0** 源码。核心实现类：`SharedFlowImpl`，位于 `kotlinx/coroutines/flow/SharedFlow.kt`；订阅者管理基类 `AbstractSharedFlow`，位于 `flow/internal/AbstractSharedFlow.kt`。

---

## 0. 一句话总览

`SharedFlow` 是一个**热流**：它的实例独立于订阅者存在，`emit` 出去的值会**广播**给当前所有活跃订阅者（subscriber）。其行为由三个构造参数决定：`replay`（重放缓存大小）、`extraBufferCapacity`（额外缓冲区）、`onBufferOverflow`（缓冲区满时的策略）。底层是**一个共享的环形数组 + 每个订阅者一个 slot（游标）** 的设计。

```kotlin
public fun <T> MutableSharedFlow(
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
): MutableSharedFlow<T>
```

---

## 1. 类型层次

```
Flow<T>
  └── SharedFlow<T>            // 只读：replayCache + collect(): Nothing
        └── MutableSharedFlow<T>  // 可写：emit / tryEmit / subscriptionCount / resetReplayCache
              （还继承 FlowCollector<T>，所以它本身就是个收集器）

实现类：SharedFlowImpl<T> : AbstractSharedFlow<SharedFlowSlot>(), MutableSharedFlow<T>, CancellableFlow<T>, FusibleFlow<T>
```

**面试点**：`collect` 的返回类型是 `Nothing` —— 编译器层面就告诉你**热流的 collect 永不正常返回**，它会一直挂起当前协程直到被取消。这也是「在一个协程里串行写两个 collect，第二个是死代码」的根因。

---

## 2. 构造函数做了什么

```kotlin
public fun <T> MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow): MutableSharedFlow<T> {
    require(replay >= 0)
    require(extraBufferCapacity >= 0)
    // 关键约束：非默认的溢出策略，必须有缓冲空间
    require(replay > 0 || extraBufferCapacity > 0 || onBufferOverflow == BufferOverflow.SUSPEND)

    val bufferCapacity0 = replay + extraBufferCapacity
    val bufferCapacity = if (bufferCapacity0 < 0) Int.MAX_VALUE else bufferCapacity0 // 溢出兜底
    return SharedFlowImpl(replay, bufferCapacity, onBufferOverflow)
}
```

要点：
- **`bufferCapacity = replay + extraBufferCapacity`**：总缓冲容量是这两者之和。
- `MutableSharedFlow()`（无参）→ `replay=0, bufferCapacity=0, onBufferOverflow=SUSPEND`，即「无缓存、无缓冲」的**同步（rendezvous）共享流**：`emit` 会挂起直到所有订阅者都拿到值；没有订阅者时值直接丢弃。
- 当 `replay==0 && extraBufferCapacity==0` 时，`onBufferOverflow` 只能是 `SUSPEND`，否则 `require` 抛 `IllegalArgumentException`。原因：没有缓冲空间，谈不上「溢出丢弃」。

---

## 3. 核心数据结构：一个环形数组承担三段职责

`SharedFlowImpl` 内部只有一个数组 `buffer`，但它在逻辑上被切成三段（源码里有一段精彩的 ASCII 注释）：

```
              buffered values
         /-----------------------\
                      replayCache      queued emitters
                      /----------\/----------------------\
     +---+---+---+---+---+---+---+---+---+---+---+---+---+
     |   | 1 | 2 | 3 | 4 | 5 | 6 | E | E | E | E | E |   |
     +---+---+---+---+---+---+---+---+---+---+---+---+---+
           ^           ^           ^
          head         |      head+bufferSize
                 replayIndex
```

关键字段：

| 字段 | 含义 |
|------|------|
| `buffer: Array<Any?>?` | 环形数组，懒分配，大小恒为 2 的幂 |
| `replayIndex` | 新订阅者从这个下标开始读（replay 缓存的起点） |
| `minCollectorIndex` | 所有活跃订阅者里最小的下标（最慢的那个订阅者读到哪了） |
| `bufferSize` | 已缓冲的「值」的个数 |
| `queueSize` | 已排队的「挂起的 emitter」个数 |
| `head` | `minOf(minCollectorIndex, replayIndex)`，整个 buffer 的起点 |
| `replaySize` | `head + bufferSize - replayIndex`，当前重放缓存实际大小 |

**为什么是环形数组而不是链表？** 注释里写明：添加订阅者 `O(1)` 摊还（「摊还」是 amortized（摊销 / 平摊） 的翻译,是算法复杂度分析里的一个术语。摊还复杂度指的是:把一系列操作的总代价平均到每次操作上,得到的「平均每次」代价。它不看单次最坏情况,而看长期平均。），但 emit 是 `O(N)`（N = 订阅者数），因为要唤醒每个等待中的订阅者。环形数组配合 `index and (size-1)` 取模（见文末工具函数），分配/回收无 GC 压力。

```kotlin
private fun Array<Any?>.getBufferAt(index: Long) = get(index.toInt() and (size - 1))
private fun Array<Any?>.setBufferAt(index: Long, item: Any?) = set(index.toInt() and (size - 1), item)
```

`index` 是单调递增的 `Long`，物理位置靠 `and (size-1)` 折回数组范围 —— 这就是「环形」的实现，且 `Long` 几乎不会溢出。

---

## 4. 订阅者模型：Slot 游标（AbstractSharedFlow）

每个订阅者（一次 `collect`）持有一个 `SharedFlowSlot`：

```kotlin
internal class SharedFlowSlot : AbstractSharedFlowSlot<SharedFlowImpl<*>>() {
    var index = -1L            // 当前「待发射」的下标，-1 表示空闲
    var cont: Continuation<Unit>? = null // 没有新值时挂起的续体

    override fun allocateLocked(flow): Boolean {
        if (index >= 0) return false       // 非空闲
        index = flow.updateNewCollectorIndexLocked() // 新订阅者从 replayIndex 开始
        return true
    }
}
```

`AbstractSharedFlow` 用一个 `slots` 数组管理所有订阅者，关键方法：
- `allocateSlot()`：找一个空闲 slot 给新订阅者，`nCollectors++`，并自增 `subscriptionCount`。数组满了就翻倍扩容。
- `freeSlot()`：订阅者取消时归还 slot，`nCollectors--`，可能唤醒被慢订阅者卡住的 emitter，并自减 `subscriptionCount`。

**面试点 —— `subscriptionCount` 是怎么实现的？** 它是个 `StateFlow<Int>`，但内部是一个 `replay=1, capacity=MAX, DROP_OLDEST` 的 `SharedFlowImpl`（`SubscriptionCountStateFlow`）。**故意不用真正的 StateFlow**，因为 StateFlow 会去重/合并（conflate），而订阅数对「0→1→0」这种瞬时变化敏感，不能被合并掉，否则 `SharingStarted.WhileSubscribed` 之类逻辑会失效。

---

## 5. emit / tryEmit 全流程

```kotlin
override suspend fun emit(value: T) {
    if (tryEmit(value)) return  // 快路径：能立刻放进缓冲就不挂起
    emitSuspend(value)          // 慢路径：缓冲满且策略为 SUSPEND，挂起等待
}

override fun tryEmit(value: T): Boolean {
    var resumes = EMPTY_RESUMES
    val emitted = synchronized(this) {
        if (tryEmitLocked(value)) {
            resumes = findSlotsToResumeLocked(resumes) // 找出能被唤醒的订阅者
            true
        } else false
    }
    for (cont in resumes) cont?.resume(Unit)  // 锁外唤醒，避免死锁
    return emitted
}
```

**设计精髓**：所有状态修改在 `synchronized` 锁内完成，但**续体的 `resume` 一律放到锁外**。注释解释原因：unconfined 协程在 resume 时可能同步往回执行代码，若在锁内 resume 会重入死锁。

### tryEmitLocked —— 决策核心

```kotlin
private fun tryEmitLocked(value: T): Boolean {
    // 1. 没有订阅者：走 no-collector 快路径，永远成功
    if (nCollectors == 0) return tryEmitNoCollectorsLocked(value)

    // 2. 有订阅者，但缓冲满了且被最慢的订阅者卡住
    if (bufferSize >= bufferCapacity && minCollectorIndex <= replayIndex) {
        when (onBufferOverflow) {
            BufferOverflow.SUSPEND     -> return false  // → 让 emit 走 emitSuspend 挂起
            BufferOverflow.DROP_LATEST -> return true   // 直接丢掉这个新值
            BufferOverflow.DROP_OLDEST -> {}            // 继续入队，下面再丢最旧的
        }
    }
    enqueueLocked(value)
    bufferSize++
    if (bufferSize > bufferCapacity) dropOldestLocked()       // DROP_OLDEST：丢队头
    if (replaySize > replay) updateBufferLocked(replayIndex+1, ...) // 保持 replay 不超额
    return true
}
```

**面试高频：没有订阅者时 emit 会怎样？**

```kotlin
private fun tryEmitNoCollectorsLocked(value: T): Boolean {
    if (replay == 0) return true            // 不需要重放，值直接丢弃（"错过就错过"）
    enqueueLocked(value); bufferSize++
    if (bufferSize > replay) dropOldestLocked()   // 只保留最近 replay 个
    minCollectorIndex = head + bufferSize
    return true
}
```

结论：
- **没有订阅者时，emit 永不挂起**（即使策略是 SUSPEND）。
- `replay==0`：值瞬间丢失，`tryEmit` 返回 `true`（文档原话：tryEmit 在无订阅者时总成功）。
- `replay>0`：只把值塞进 replay 缓存，超出 `replay` 就丢最旧的。
- 所以**「缓冲区溢出」只可能发生在「至少有一个慢订阅者还没消费完」时**。

### emitSuspend —— 挂起的 emitter 也排进同一个数组

```kotlin
private suspend fun emitSuspend(value: T) = suspendCancellableCoroutine { cont ->
    val emitter = synchronized(this) {
        if (tryEmitLocked(value)) { cont.resume(Unit); ...; return@lock null } // 再试一次
        // 仍满：把「emitter」对象塞进 buffer 队尾，queueSize++
        Emitter(this, head + totalSize, value, cont).also {
            enqueueLocked(it); queueSize++
            if (bufferCapacity == 0) resumes = findSlotsToResumeLocked(resumes) // rendezvous
        }
    }
    emitter?.let { cont.disposeOnCancellation(it) } // 取消时能把自己从队列摘掉
    for (r in resumes) r?.resume(Unit)
}
```

**精妙处**：挂起的生产者被包成 `Emitter` 对象，和普通的值**共用同一个 buffer 数组**（图里的 `E` 段）。当慢订阅者前进、腾出缓冲空间时（`updateCollectorIndexLocked`），这些排队的 emitter 会被依次唤醒，它们的值「转正」进入 buffer。

---

## 6. collect 全流程

```kotlin
override suspend fun collect(collector: FlowCollector<T>): Nothing {
    val slot = allocateSlot()                  // 注册订阅者
    try {
        if (collector is SubscribedFlowCollector) collector.onSubscription()
        val collectorJob = currentCoroutineContext()[Job]
        while (true) {
            var newValue: Any?
            while (true) {
                newValue = tryTakeValue(slot)  // 快路径：直接从 buffer 取
                if (newValue !== NO_VALUE) break
                awaitValue(slot)               // 没有值就挂起，等 emit 唤醒
            }
            collectorJob?.ensureActive()       // 每次发射前检查取消（cancellable）
            collector.emit(newValue as T)
        }
    } finally {
        freeSlot(slot)                         // 取消时归还 slot，可能唤醒排队的 emitter
    }
}
```

要点：
- 新订阅者的 `slot.index` 初始化为 `replayIndex` → **一订阅就能立刻拿到 replay 缓存里的历史值**（这就是「粘性」的来源，由 `replay` 决定深度）。
- `tryTakeValue` 取到值后 `slot.index++` 前进，并调用 `updateCollectorIndexLocked` 重算 `minCollectorIndex`，可能唤醒被自己卡住的 emitter。
- **每次发射前 `ensureActive()`** —— `SharedFlow` 永远是 `cancellable` 的，所以「在 collect 里不响应取消」的问题在这里不存在。

---

## 7. BufferOverflow 三种策略对比

| 策略 | emit 行为（缓冲满且有慢订阅者时） | 谁会丢值 |
|------|----------------------------------|----------|
| `SUSPEND`（默认） | `tryEmitLocked` 返回 false → `emitSuspend` 挂起生产者，直到有空间 | 不丢，靠背压 |
| `DROP_LATEST` | 直接返回 true，**新值被丢弃** | 丢新到的 |
| `DROP_OLDEST` | 入队后 `dropOldestLocked` 丢掉队头最旧值 | 丢最旧的 |

注意：`DROP_*` 策略下 `emit` **永不挂起**（`tryEmit` 总返回 true）。

---

## 8. 线程安全与性能

- **一把 `synchronized` 锁**保护所有内部状态（`SynchronizedObject`）。
- **resume 在锁外**：避免 unconfined 协程重入死锁，这是源码里反复强调的设计约束。
- 复杂度：订阅 `O(1)` 摊还；**emit `O(N)`**，N 为订阅者数（要遍历所有 slot 找可唤醒的）。
- 环形数组 + `Long` 单调下标 + `and (size-1)` 取模 → 分配无 GC、几乎零额外对象（除挂起的 Emitter）。

---

## 9. Operator fusion（算子融合）

```kotlin
override fun fuse(context, capacity, onBufferOverflow) = fuseSharedFlow(...)
```

对 `SharedFlow` 应用 `flowOn`、`buffer(RENDEZVOUS)`、`cancellable` **无效**（直接返回自身），因为热流本身已经带缓冲、已经 cancellable。只有当你要求额外的非平凡缓冲（给特别慢的订阅者）时，才会真正包一层 `ChannelFlowOperatorImpl`。

---

## 10. 面试速答清单

**Q：SharedFlow 和普通 cold Flow 的本质区别？**
A：cold Flow 每个 collector 触发一次独立执行；SharedFlow 是热流，实例独立存在，一份数据广播给所有订阅者，`collect` 返回 `Nothing` 永不结束。

**Q：replay 和 extraBufferCapacity 区别？**
A：`bufferCapacity = replay + extraBufferCapacity`。`replay` 是「新订阅者能重放到几个历史值」；`extraBufferCapacity` 是「在 replay 之外，额外给慢订阅者多少缓冲，让 emit 不至于挂起」。两者共同决定缓冲总容量。

**Q：没有订阅者时 emit 的值去哪了？**
A：`replay==0` 直接丢弃，`tryEmit` 仍返回 true；`replay>0` 进 replay 缓存只留最近 replay 个。无订阅者时 emit 永不挂起。

**Q：缓冲区溢出什么时候才会触发？**
A：必须**至少有一个慢订阅者**还没消费完缓冲区里的值。无订阅者时只维护 replay 缓存，永不触发溢出策略。

**Q：emit 为什么可能挂起？**
A：仅当 `onBufferOverflow == SUSPEND` 且有订阅者把缓冲填满时，`emitSuspend` 把生产者包成 `Emitter` 排进同一个 buffer 队列挂起，等慢订阅者前进腾出空间后被唤醒。

**Q：为什么 resume 要放到锁外？**
A：unconfined 协程 resume 时可能同步回调执行用户代码，若在 `synchronized` 锁内 resume 会重入同一把锁导致死锁。

**Q：subscriptionCount 为什么不是真 StateFlow？**
A：它底层是 `replay=1` 的 SharedFlow，故意**不做去重合并**，因为订阅数对「瞬时 0→1→0」敏感，conflate 会丢掉这种变化，破坏 `WhileSubscribed` 等共享策略。

---

## 11. 关联文档
- StateFlow 是 SharedFlow 的特化子类，详见 `StateFlow 源码解析.md`。
- 使用层面的对比、粘性、多 collect 误用，详见 `StateFlow 和 SharedFlow.md`。

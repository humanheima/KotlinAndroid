# SharedFlow


在 Kotlin 的协程库中，`SharedFlow` 和 `StateFlow` 都是用于处理数据流的热流（Hot Flow）工具，但它们的设计目标和使用场景有所不同。以下是对 `SharedFlow` 和 `StateFlow` 的详细对比，涵盖定义、特性、使用场景和代码示例，帮助你更好地理解两者的区别和适用场景。

### **1. 定义与核心概念**
| 特性 | SharedFlow | StateFlow |
|------|------------|-----------|
| **定义** | 一种通用的热数据流，适合广播事件或数据给多个订阅者，支持灵活的配置（如缓存和溢出策略）。 | 一种专为状态管理设计的热数据流，表示单一状态值，始终保留最新的值，适合状态更新场景。 |
| **类型** | 热流，数据不依赖订阅者存在即可发射。 | 热流，始终持有最新的状态值。 |
| **初始值** | 不需要初始值，可动态发射数据。 | 必须提供初始值，始终有一个值。 |
| **变体** | `MutableSharedFlow`（可变）和 `SharedFlow`（只读）。 | `MutableStateFlow`（可变）和 `StateFlow`（只读）。 |

### **2. 关键特性对比**
| 特性 | SharedFlow | StateFlow |
|------|------------|-----------|
| **数据性质** | 事件或数据流，适合一次性事件或连续数据流。 | 单一状态，适合表示 UI 状态或应用程序状态。 |
| **缓存（Replay）** | 支持配置 `replay` 参数（默认 0），可缓存 0 到 N 个历史数据，新订阅者可收到缓存数据。 | 固定 `replay = 1`，始终缓存最新值，新订阅者收到最新状态。 |
| **重复值处理** | 不自动过滤重复值，需手动处理（如使用 `distinctUntilChanged`）。 | 自动过滤重复值（基于 `equals`），仅在值变化时通知订阅者。 |
| **缓冲区溢出** | 支持灵活的缓冲区溢出策略（`BufferOverflow.DROP_OLDEST`、`DROP_LATEST`、`SUSPEND`）。 | 固定为 `DROP_OLDEST`，只保留最新值。 |
| **订阅者行为** | 所有活跃订阅者都会收到新数据，支持多消费者广播。 | 所有活跃订阅者收到最新状态，适合状态共享。 |
| **初始值要求** | 无需初始值，适合动态事件流。 | 必须提供初始值，适合状态管理。 |
| **线程安全** | 线程安全，可在多线程/协程环境中使用。 | 线程安全，专为状态更新优化。 |

### **3. 使用场景**
| 场景 | SharedFlow | StateFlow |
|------|------------|-----------|
| **事件广播** | 适合广播一次性事件（如按钮点击、Toast 提示、导航事件）。 | 不适合一次性事件，适合持久状态。 |
| **状态管理** | 可用于状态共享，但需要手动配置（如 `replay = 1` 模拟状态）。 | 专为 UI 状态或应用程序状态设计（如加载状态、主题模式）。 |
| **多消费者** | 天然支持多个订阅者，适合多组件共享数据流。 | 也支持多个订阅者，但专注于状态一致性。 |
| **实时数据** | 适合实时数据流（如传感器数据、消息推送）。 | 适合表示最新状态（如网络连接状态）。 |
| **Android 替代** | 可替代 `LiveData` 或 RxJava 的 `PublishSubject`，用于事件分发。 | 可替代 `LiveData`，用于状态管理。 |

### **4. 代码示例**
#### **SharedFlow 示例：事件广播**
```kotlin
class EventViewModel : ViewModel() {
    private val _events = MutableSharedFlow<String>(replay = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events = _events.asSharedFlow()

    fun sendEvent(message: String) {
        viewModelScope.launch {
            _events.emit(message)
        }
    }
}

// 订阅（例如在 Activity 中）
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.events.collect { message ->
            showToast("Event: $message")
        }
    }
}
```

#### **StateFlow 示例：状态管理**
```kotlin
class NetworkViewModel : ViewModel() {
    private val _networkStatus = MutableStateFlow(false) // 初始值为 false
    val networkStatus = _networkStatus.asStateFlow()

    fun updateNetworkStatus(isConnected: Boolean) {
        _networkStatus.value = isConnected
    }
}

// 订阅（例如在 Activity 中）
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.networkStatus.collect { isConnected ->
            showToast(if (isConnected) "Connected" else "Disconnected")
        }
    }
}
```

### **5. 性能与注意事项**
| 方面 | SharedFlow | StateFlow |
|------|------------|-----------|
| **性能** | 更灵活，但需要手动配置缓存和溢出策略，可能增加复杂性。 | 更简单，自动处理重复值和最新状态，性能优化更好。 |
| **内存管理** | 大量缓存（高 `replay` 值）可能导致内存占用高。 | 仅缓存最新值，内存占用低。 |
| **生命周期** | 在 Android 中需配合 `repeatOnLifecycle` 管理订阅，避免内存泄漏。 | 同 `SharedFlow`，需生命周期管理。 |
| **误用风险** | 配置不当（如过大的 `replay` 或错误的溢出策略）可能导致性能问题。 | 强制初始值和单一状态设计降低误用风险。 |

### **6. 选择建议**
- **选择 SharedFlow 的场景**：
   - 需要广播事件（如用户操作、通知）。
   - 需要灵活的缓存策略（如缓存多条历史数据）。
   - 处理实时数据流或不需要初始值的场景。
   - 示例场景：推送聊天消息、导航事件、一次性提示。
- **选择 StateFlow 的场景**：
   - 需要管理单一状态（如 UI 状态、应用程序配置）。
   - 希望自动过滤重复值，简化状态更新逻辑。
   - 替代 `LiveData` 用于 Android 的 ViewModel 状态管理。
   - 示例场景：网络状态、加载状态、主题切换。

### **7. 总结**
- **`SharedFlow`**：通用性强，适合事件驱动场景和多消费者数据流，支持灵活的缓存和溢出策略，但需要开发者手动管理重复值和配置。
- **`StateFlow`**：专为状态管理优化，始终保留最新状态，自动去重，适合 UI 状态或单一状态的场景，简单易用。
- **如何选择**：如果你的需求是事件广播或动态数据流，选择 `SharedFlow`；如果需要管理状态并确保最新值一致性，选择 `StateFlow`。

通过结合具体需求（如是否需要初始值、是否过滤重复值、是否为状态或事件），可以轻松决定使用 `SharedFlow` 还是 `StateFlow`，并在 Android 或其他 Kotlin 项目中高效管理数据流。




#  StateFlow

StateFlow 是 SharedFlow 的一个子类.


是的，**StateFlow** 可以有多个订阅者。

### 详细说明：
`StateFlow` 是 Kotlin 提供的一种响应式数据流（Flow）实现，专门用于表示状态。它支持多个订阅者（即多个 `Collector`），每个订阅者都可以接收到 `StateFlow` 的最新状态以及后续的状态更新。

#### 关键点：
1. **多订阅者支持**：
    - `StateFlow` 是一个热流（Hot Flow），意味着它始终保持一个当前值（`value`），并且无论有多少订阅者，它们都会收到最新的状态值。
    - 多个协程或订阅者可以通过调用 `collect` 或其他 Flow 操作（如 `onEach`）来订阅 `StateFlow`，每个订阅者都会独立接收状态更新。

2. **行为示例**：
    - 当一个新的订阅者开始收集 `StateFlow` 时，它会立即收到当前的 `value`（如果存在）。
    - 之后，每当 `StateFlow` 的值发生变化（通过 `MutableStateFlow` 的 `value` 属性或 `emit` 更新），所有活跃的订阅者都会收到新的值。

3. **代码示例**：
   ```kotlin
   import kotlinx.coroutines.*
   import kotlinx.coroutines.flow.*

   fun main() = runBlocking {
       val stateFlow = MutableStateFlow(0)

       // 订阅者 1
       launch {
           stateFlow.collect { value ->
               println("订阅者 1 收到: $value")
           }
       }

       // 订阅者 2
       launch {
           stateFlow.collect { value ->
               println("订阅者 2 收到: $value")
           }
       }

       // 模拟状态更新
       delay(1000)
       stateFlow.value = 1
       delay(1000)
       stateFlow.value = 2
   }
   ```

   **输出**：
   ```
   订阅者 1 收到: 0
   订阅者 2 收到: 0
   订阅者 1 收到: 1
   订阅者 2 收到: 1
   订阅者 1 收到: 2
   订阅者 2 收到: 2
   ```

   从输出可以看出，两个订阅者都收到了初始值 `0` 以及后续的更新 `1` 和 `2`。

4. **注意事项**：
    - **共享状态**：`StateFlow` 设计用于共享状态，适合在多个组件（如 UI 和 ViewModel）之间共享数据。
    - **去重机制**：`StateFlow` 默认会对重复值进行去重（通过 `equals` 判断）。如果新设置的值与当前值相同，订阅者不会收到更新。
    - **生命周期管理**：在 Android 中，建议结合 `repeatOnLifecycle` 或 `lifecycleScope` 来管理订阅，确保在适当的生命周期（如 `STARTED`）内收集数据，避免内存泄漏。
      ```kotlin
      lifecycleScope.launch {
          repeatOnLifecycle(Lifecycle.State.STARTED) {
              stateFlow.collect { value ->
                  println("收到: $value")
              }
          }
      }
      ```

5. **与 LiveData 的对比**：
    - 类似 `LiveData`，`StateFlow` 也支持多个观察者，但 `StateFlow` 更适合协程环境，且提供了更强大的 Flow 操作符。
    - `StateFlow` 是线程安全的，适合在多线程环境中使用。

### 结论：
`StateFlow` 支持多个订阅者，每个订阅者都会收到初始值和后续的状态更新。它是线程安全的，非常适合用于管理共享状态的场景，如 Android 的 ViewModel 层与 UI 层之间的数据通信。


# 粘性（Sticky）对比

> **什么是「粘性」？** 当一个**新订阅者**开始 `collect` 时，能否**立即收到订阅之前就已经发射过的旧值**。能收到 → 粘性；收不到、只能收到订阅之后的新值 → 非粘性。
>
> 粘性的本质就是 **replay 缓存**：订阅瞬间，流会先把缓存里的值重放给新订阅者。

## 三者对比

| 类型 | 是否粘性 | 说明 |
|------|---------|------|
| **StateFlow** | **永远粘性** | `replay` 固定为 1，始终缓存最新值。任何新订阅者一订阅就立刻收到当前 `value`。无法关闭。 |
| **SharedFlow（replay = 0）** | **非粘性** | 不缓存任何历史值。订阅之前发射的数据，新订阅者收不到（错过就是错过）。 |
| **SharedFlow（replay = N）** | **粘性** | 缓存最近 N 个值，新订阅者订阅时会先收到这最多 N 个旧值，可自定义粘性的「深度」。 |

**一句话：** StateFlow 是「强制粘性、深度恒为 1」；SharedFlow 的粘性完全由 `replay` 决定，`replay = 0` 即非粘性。

## 为什么粘性对「事件」是个坑

粘性对**状态**是好事（UI 一订阅就能拿到当前状态去渲染），但对**一次性事件**是灾难：

- 场景：用 `StateFlow` / `replay>0` 的 SharedFlow 发「弹 Toast」「导航跳转」这类事件。
- 问题：屏幕旋转、Activity 重建后重新订阅，会**立刻重放最后一个事件**，于是 Toast 又弹一次、页面又跳一次。
- 这正是 `LiveData` 著名的「**粘性事件 / SingleLiveEvent**」问题在 Flow 上的翻版。

```kotlin
// ❌ 错误：用 StateFlow 发一次性事件，重建后会重复触发
private val _toast = MutableStateFlow("")        // 粘性，旧值会重放

// ✅ 正确：一次性事件用 replay = 0 的 SharedFlow，非粘性
private val _toast = MutableSharedFlow<String>() // replay 默认 0，订阅前的事件不会重放
val toast = _toast.asSharedFlow()
```

## 选择原则

- **状态（UI State）** → 要粘性 → 用 `StateFlow`（或 `replay = 1` 的 SharedFlow）。新订阅者需要立刻拿到「当前是什么」。
- **事件（Event，一次性）** → 不要粘性 → 用 `replay = 0` 的 `SharedFlow`。事件只应被消费一次，不该在重新订阅时被重放。

> 补充：StateFlow 的粘性还叠加了**去重**——重放的最新值若与订阅者已见的值相等也不会重复触发；而 SharedFlow 不去重，`replay` 缓存里的值会原样重放。


# 多个 collect 的常见错误用法

> 核心前提：**`StateFlow` 是热流，`collect` 永远不会结束**——它会挂起当前协程，直到流被取消。理解这一点就能看懂下面所有的坑。
>
> `collect`（对热流而言）= **占住当前协程不放**。

## 错误一：在一个 `collect` 的 lambda 里嵌套另一个 `collect`

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.count.collect { value ->          // 外层
            binding.tvCount.text = "count = $value"

            viewModel.count.collect { value ->      // 内层：进去就再也不出来
                Log.i(TAG, "订阅者2 收到 count = $value")
            }
        }
    }
}
```

**执行过程：**
1. 外层收到第一个值（如 `0`），更新 UI，正常。
2. 走到内层 `collect`，它先收到当前值 `0`，然后**一直挂起等待后续值，永不返回**。
3. 外层 lambda 被内层卡死，外层**再也拿不到下一个值**。

**后果：** 「订阅者1」只执行一次（UI 永远停在 `0`），之后的更新只由内层的「订阅者2」打印。等于把外层订阅者废了。

## 错误二：在一个协程里顺序写多个 `collect`

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.count.collect { value ->          // 第一个：进去就一直挂起，永不返回
            binding.tvCount.text = "count = $value"
        }
        viewModel.count.collect { value ->          // 第二个：永远到不了，死代码
            Log.i(TAG, "订阅者2 收到 count = $value")
        }
    }
}
```

**后果：** 「订阅者1」正常工作；第一个 `collect` 后面的代码是**不可达的死代码**，「订阅者2」一条日志都不会打印。

> 错误一和错误二是同一个坑的两种表现：**`collect` 之后的代码在热流场景下是不可达的**。

## 正确做法：每个 `collect` 包一个 `launch`

要让多个 `collect` **同时并发运行**，必须让它们各自处于**独立的协程**中。`launch` 会立即返回（不挂起），启动子协程后马上往下走：

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {                                    // 子协程1
            viewModel.count.collect { value ->
                binding.tvCount.text = "count = $value"
            }
        }
        launch {                                    // 子协程2
            viewModel.count.collect { value ->
                Log.i(TAG, "订阅者2 收到 count = $value")
            }
        }
    }
}
```

或者使用多个顶层 `lifecycleScope.launch`，效果相同：

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.count.collect { /* 订阅者1 */ }
    }
}
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.count.collect { /* 订阅者2 */ }
    }
}
```

**一句话总结：** 同一个协程里串行/嵌套多个热流 `collect`，后面的永远轮不到；要并发就给每个 `collect` 套一个 `launch`。
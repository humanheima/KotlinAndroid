# Kotlin 协程核心概念：CoroutineContext、Job、Scope、Dispatcher

> 配套阅读：[[Kotlin协程异常处理]]。本篇讲清「协程靠什么运行、由谁管理」这几个底层概念。

---

## 0. 一张全景图

```
CoroutineScope                      ← 协程的「启动入口」，持有一个 CoroutineContext
   └── CoroutineContext             ← 一个「带类型 Key 的 Map」，存放各种运行要素
          ├── Job                   ← 生命周期 / 取消 / 父子结构
          ├── CoroutineDispatcher   ← 在哪个线程（池）上跑
          ├── CoroutineExceptionHandler ← 未捕获异常兜底（仅根协程有效）
          └── CoroutineName         ← 调试用名字
```

一句话：**Scope 启动协程，Context 描述协程，Job 管理协程，Dispatcher 调度线程。**

---

## 1. CoroutineContext —— 协程的「运行环境」

`CoroutineContext` 本质是一个**以类型为 Key 的不可变集合**（类似 `Map<Key, Element>`），每种要素是一个 `Element`，各自有唯一的 `Key`。

常见 Element：

| Element | Key | 作用 |
|---|---|---|
| `Job` | `Job` | 生命周期、取消、父子关系 |
| `CoroutineDispatcher` | `ContinuationInterceptor` | 决定运行线程 |
| `CoroutineExceptionHandler` | `CoroutineExceptionHandler` | 未捕获异常处理 |
| `CoroutineName` | `CoroutineName` | 协程名（日志/调试） |

### 用 `+` 组合

```kotlin
val context = Dispatchers.IO + Job() + CoroutineName("myCoroutine")
```

### 合并规则：相同 Key，右边覆盖左边

这是个**极其重要**的规则，前面文档里 `launch(SupervisorJob())` 的坑就源于此：

```kotlin
val a = Dispatchers.Main + Dispatchers.IO
// a 里的 Dispatcher 是 IO —— 同一个 Key（ContinuationInterceptor），右边赢

val b = parentContext + SupervisorJob()
// b 里的 Job 是 SupervisorJob —— 父级原来的 Job 被覆盖掉了
```

### 取出某个元素

```kotlin
val job = context[Job]                 // 用 Key 索引，像 map[key]
val dispatcher = context[ContinuationInterceptor]
```

---

## 2. Job —— 协程的「生命周期句柄」

每次 `launch` 都会返回一个 `Job`；`async` 返回的 `Deferred` 是 `Job` 的子接口（多了一个结果）。Job 代表一个**可取消的、有生命周期的任务**。

### 2.1 状态机

```
              start()                  完成
New ─────────────────► Active ───────────────► Completing ──► Completed
（懒启动才有）            │                                        ▲
                        │ cancel() / 子失败                       │
                        ▼                                        │
                    Cancelling ──────────────────────────────► Cancelled
```

常用属性：

```kotlin
job.isActive        // 正在运行
job.isCompleted     // 已结束（正常或异常）
job.isCancelled     // 已取消
```

### 2.2 父子结构（结构化并发的基石）

协程之间是**树形父子关系**，由 Job 维护：

```kotlin
scope.launch {            // 父 Job
    launch { ... }        // 子 Job（父亲是上面那个）
    launch { ... }        // 子 Job
}
```

父子关系带来三条规则：

1. **父协程会等待所有子协程结束**才算真正完成（结构化并发）。
2. **取消父 → 取消所有子**。
3. **子失败 → 默认取消父和兄弟**（普通 Job；`SupervisorJob` 会打破这条）。

### 2.3 常用方法

```kotlin
job.cancel()              // 取消（向下传播到所有子协程）
job.join()                // 挂起等待该 Job 结束（不抛异常）
job.cancelAndJoin()       // 取消并等待结束
job.children              // 所有直接子 Job
```

### 2.4 `Job()` vs `SupervisorJob()`

| | `Job()` | `SupervisorJob()` |
|---|---|---|
| 子协程失败时 | 取消父级 + 所有兄弟 | **只失败它自己**，不连累兄弟 |
| 适用场景 | 一荣俱荣一损俱损的任务组 | 互相独立的任务（如多个独立请求） |

> 注意：监督性**只对它的直接子协程生效**。详见 [[Kotlin协程异常处理]] 中的 `testParentJob`。

---

## 3. CoroutineScope —— 协程的「作用域 / 启动入口」

Scope 只是对一个 `CoroutineContext` 的封装，提供 `launch` / `async` 等启动函数。它的核心价值是**给协程划定一个生命周期边界**。

```kotlin
val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
scope.launch { ... }     // 这个协程的生命周期挂靠在 scope 上
scope.cancel()           // 一次性取消 scope 内所有协程
```

### 3.1 Scope 与 Job 的关系

`CoroutineScope(context)` 要求 context 里**必须有一个 Job**（如果没传会自动补一个）。scope 启动的协程都会成为这个 Job 的**子协程** —— 这正是 `scope.cancel()` 能统一取消的原因。

### 3.2 Android 里的现成 Scope

| Scope | 绑定的生命周期 | 何时自动取消 |
|---|---|---|
| `lifecycleScope` | Activity/Fragment | 页面销毁时 |
| `viewModelScope` | ViewModel | ViewModel clear 时 |
| `GlobalScope` | 整个进程 | **永不**（慎用，易泄漏） |

### 3.3 coroutineScope vs supervisorScope（挂起函数）

它们不是 `CoroutineScope` 对象，而是**挂起函数**，用于在协程内部创建一个临时子作用域，会**挂起等待内部所有子协程完成**：

```kotlin
suspend fun loadAll() = coroutineScope {       // 任一子失败 → 全部取消并向上抛
    val a = async { loadA() }
    val b = async { loadB() }
    a.await() + b.await()
}

suspend fun loadAll() = supervisorScope {      // 子之间互相隔离
    launch { loadA() }
    launch { loadB() }
}
```

---

## 4. CoroutineDispatcher —— 协程的「线程调度器」

决定协程代码在**哪个线程（池）**执行：

| Dispatcher | 用途 | 底层 |
|---|---|---|
| `Dispatchers.Main` | UI 操作（Android 主线程） | 主线程 Handler |
| `Dispatchers.Default` | CPU 密集计算 | 与 CPU 核数相当的线程池 |
| `Dispatchers.IO` | 阻塞 IO（网络/磁盘） | 可弹性扩容的大线程池 |
| `Dispatchers.Unconfined` | 不限定（一般测试用） | 当前线程，遇挂起后随恢复点变化 |

### 切换线程：`withContext`

```kotlin
lifecycleScope.launch {                 // 默认在 Main
    val data = withContext(Dispatchers.IO) {
        fetchFromNetwork()              // 切到 IO 线程做网络
    }
    binding.text.text = data            // 回到 Main 更新 UI
}
```

> `withContext` 用临时 context 覆盖当前 context（同样遵循「右边覆盖」规则），块结束后自动切回。

---

## 5. CoroutineExceptionHandler —— 未捕获异常的兜底

```kotlin
val handler = CoroutineExceptionHandler { context, throwable ->
    Log.e(TAG, "caught: ${throwable.message}")
}
val scope = CoroutineScope(SupervisorJob() + handler)
scope.launch { throw RuntimeException("boom") }   // 被 handler 接住，不崩
```

要点：

- 它**只对根协程生效**（直接在 scope 上 launch 的，或 supervisorScope 内的直接 launch）。
- 子协程的异常会先**沿父子树往上传播**，到根才交给 handler；所以在中间层 launch 上装 handler 通常无效。
- `async` 的异常**不走 handler**，而是封存在 `Deferred`，在 `await()` 时抛出。

详细的异常传播规则见 [[Kotlin协程异常处理]]。

---

## 6. 把概念串起来：一段典型代码逐行拆解

```kotlin
val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)   // ①

val job = scope.launch(CoroutineName("loader")) {                // ②
    val data = withContext(Dispatchers.IO) { fetch() }          // ③
    binding.text.text = data                                    // ④
}
```

1. 创建 scope：Context = `SupervisorJob` + `Main 调度器`。scope 内协程互相隔离、默认跑在主线程。
2. `launch` 启动一个子协程，返回它的 `Job`。它的 Context = scope 的 Context 再叠加 `CoroutineName`（Job 仍是 SupervisorJob 的子 Job —— 因为这里没传新 Job，没有覆盖）。
3. `withContext(IO)` 临时把调度器换成 IO，在 IO 线程执行 `fetch()`，完成后切回。
4. 回到 Main 线程更新 UI。

如果某处把 ② 写成 `scope.launch(Job()) { ... }`，就会触发 [[Kotlin协程异常处理]] 里讲的「脱钩」问题——务必避免。

---

## 速查总结

| 概念 | 一句话 |
|---|---|
| **CoroutineContext** | 带类型 Key 的不可变集合，相同 Key 右边覆盖左边 |
| **Job** | 协程的生命周期句柄，维护父子树、负责取消传播 |
| **CoroutineScope** | 启动入口 + 生命周期边界，`cancel()` 一键取消全部 |
| **Dispatcher** | 决定在哪个线程跑，用 `withContext` 切换 |
| **SupervisorJob / supervisorScope** | 让子协程失败互不连累（只管取消传播，不管异常处理） |
| **CoroutineExceptionHandler** | 根协程未捕获异常的兜底 |

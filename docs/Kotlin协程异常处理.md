# Kotlin 协程异常处理：SupervisorJob 与 supervisorScope

> 基于 `ExceptionTestActivity.kt` 的示例整理。核心一句话：
> **`SupervisorJob` / `supervisorScope` 只负责「隔离取消传播」，都不负责「处理异常」。**

---

## 0. 两个必须分清的概念

| 概念 | 它管什么 | 它不管什么 |
|---|---|---|
| **取消传播 (cancellation propagation)** | 一个子协程失败，要不要取消父级和兄弟协程 | —— |
| **异常处理 (exception handling)** | —— | 未捕获异常会交给默认处理器，在 Android 上直接 **crash** |

`SupervisorJob` 和 `supervisorScope` 解决的是**第一件事**。它们让「一个子协程失败」不会牵连兄弟，但**异常本身仍然需要有人捕获/处理**，否则照样崩溃。

---

## 1. test1：用了 SupervisorJob 为什么还崩溃

```kotlin
private fun test1() {
    val scope = CoroutineScope(SupervisorJob())

    scope.launch {           // Child 1
        delay(100)
        throw IllegalStateException("Child1 failed")
    }

    scope.launch {           // Child 2
        delay(2000)
        Log.e(TAG, "test1: Child finished")
    }
}
```

**崩溃原因：**

- `SupervisorJob` 确实生效了——Child 1 失败**不会**取消 Child 2。
- 但 Child 1 抛出的异常**没有人捕获**：
  1. 自己没有 `try/catch`
  2. scope 没有安装 `CoroutineExceptionHandler`
- 于是异常作为「未捕获异常」交给 `Thread.defaultUncaughtExceptionHandler` → **App crash**。

> 结论：`SupervisorJob` 阻止的是「兄弟被取消」，**不阻止「未处理异常导致崩溃」**。

**修复版 test1Revolution —— 每个子协程自己处理异常：**

```kotlin
scope.launch {
    try {
        delay(100)
        throw IllegalStateException("Child1 failed")
    } catch (e: Exception) {
        Log.e(TAG, "Child1 caught: ${e.message}")   // 异常被消化，不会崩
    }
}
```

**另一种修复 —— 给 scope 装 handler（对根协程有效）：**

```kotlin
val handler = CoroutineExceptionHandler { _, e ->
    Log.e(TAG, "caught: ${e.message}")
}
val scope = CoroutineScope(SupervisorJob() + handler)
```

---

## 2. test2：supervisorScope —— 和 test1 是重复的吗？

```kotlin
private fun test2() {
    val scope = CoroutineScope(Job())          // 注意是普通 Job

    scope.launch {
        supervisorScope {                      // 监督性来自这里
            launch { delay(100); throw IllegalStateException("Child1 failed") }
            launch { delay(2000); Log.e(TAG, "Child finished") }
        }
    }
}
```

**结论相同（都会崩），但演示的是另一套 API。**

| | test1 | test2 |
|---|---|---|
| 监督来源 | `CoroutineScope(SupervisorJob())` | `CoroutineScope(Job())` + `supervisorScope {}` |
| 监督载体 | 一个 **Job 实例** | 一个 **挂起函数（作用域构建器）** |
| 生命周期 | 长期存活的 scope，`launch` 后立即返回 | 结构化作用域，**挂起等待**所有子协程完成才返回 |

两者都说明「监督不处理异常」，所以都崩；修复版思路也一致（自己 try/catch 或装 handler）。保留两个例子是为了对比这两个**容易混淆**的 API，不算冗余。

**修复版 test2Revolution —— handler 装在内层 launch 上：**

```kotlin
val handler = CoroutineExceptionHandler { _, e -> Log.e(TAG, "handler caught ${e.message}") }
scope.launch {
    supervisorScope {
        launch(handler) {                 // ← handler 必须装在这个子协程上
            delay(100); throw IllegalStateException("Child1 failed")
        }
        launch { delay(2000); Log.e(TAG, "Child2 finished") }  // 依然完成
    }
}
```

> ⚠️ `CoroutineExceptionHandler` 只对**根协程**生效。在 `supervisorScope` 里，每个 `launch` 子协程被当作「根」对待，所以 handler 要装在那个 `launch` 上，装在外层无效。

---

## 3. testParentJob：launch(SupervisorJob()) 的经典坑

```kotlin
private fun testParentJob() {
    val scope = CoroutineScope(Job())

    val job = scope.launch(SupervisorJob()) {     // ← 看这里
        launch { delay(100); /* Child 1 */ }
        launch { delay(2000); /* Child 2 */ }
        Log.e(TAG, "parent $isActive")
    }
}
```

### 关键：`launch` 永远会给自己创建一个新的普通 Job

`launch(SupervisorJob())` **不会**让该协程以监督方式管理子协程，因为：

1. `launch` 拿到你传入的 `SupervisorJob`；
2. **再创建一个全新的普通 `Job`** 作为自己协程体的 Job；
3. 这个新 Job 的父级才是你传入的 `SupervisorJob`。

### 真实的 Job 树

```
SupervisorJob()                    ← 你传入的，脱离了 scope 的 Job，成了顶层
   └── 外层 launch 的协程 Job        ← 普通 Job！（launch 自动创建）
          ├── Child 1 的 Job        ← 父级是「外层协程的普通 Job」
          └── Child 2 的 Job        ← 父级是「外层协程的普通 Job」
```

由此得出两点：

1. **Child 1 / Child 2 的直接父级是「外层协程的普通 Job」**，不是 SupervisorJob。所以 Child 1 失败 → 取消普通 Job → Child 2 也被取消并向上传播。SupervisorJob 完全没用。
2. **SupervisorJob 只对「自己的直接子协程」生效**，而它的直接子只有外层那一个协程，同级只有一个，监督性无从体现。

### 额外副作用：脱离 scope，破坏结构化并发

`launch(SupervisorJob())` 还把这个协程**从 `scope` 脱钩了**。要理解为什么，看 `launch` 内部做的两件事：

**第 1 步：context 合并，新协程的父级由合并后的 Job 决定**

```kotlin
val newContext = newCoroutineContext(context)   // ≈ scope.coroutineContext + context
```

`CoroutineContext` 合并时，相同 Key 的元素**右边覆盖左边**，而 `Job` 就是一个 Key：

```
scope 的 Job()  +  你传入的 SupervisorJob()  =  Job 槽位最终是 SupervisorJob()
                                                （scope 原来的 Job 被挤掉）
```

**第 2 步：新协程把「合并后的 Job」当父级挂上去**

```kotlin
val parent = parentContext[Job]   // 取到的是 SupervisorJob，不是 scope 的 Job
parent?.attachChild(this)
```

而新建的 `SupervisorJob()` 自己没有父亲，是个游离的根。于是整棵子树脱离了 scope：

```
scope 的 Job()              SupervisorJob()  ← 游离，没父亲
   │（本该挂这里，但没有）      └── 外层 launch 协程
                                   ├── Child 1
                                   └── Child 2
```

**具体危害：`scope.cancel()` 取消不到它**

```kotlin
val scope = CoroutineScope(Job())
scope.launch(SupervisorJob()) {
    delay(10_000)
    Log.e(TAG, "我还在跑！")   // ← scope.cancel() 之后这行依然执行
}
scope.cancel()   // 只取消 scope 的 Job 及其真·子协程，传播不到上面那个协程
```

在 Android 里尤其危险：若 `scope` 是 `lifecycleScope` / `viewModelScope`，页面销毁时框架调用 `scope.cancel()`，但这个脱钩的协程不会被取消，继续持有 Activity/ViewModel 引用 → **内存泄漏 / 销毁后仍更新 UI 导致崩溃**。

**铁律：永远不要给 `launch` / `async` 单独传新建的 `Job()` 或 `SupervisorJob()`**——既无监督效果，又切断与 scope 的父子关系。**既无用又有害。**

### 正确写法

```kotlin
// 方式一：supervisorScope（推荐，结构化）
scope.launch {
    supervisorScope {
        launch { /* Child 1 */ }
        launch { /* Child 2 */ }
    }
}

// 方式二：scope 本身用 SupervisorJob（同 test1）
val scope = CoroutineScope(SupervisorJob())
scope.launch { /* Child 1 */ }
scope.launch { /* Child 2 */ }
```

> **监督要装在「子协程的直接父级」上才有用。**

---

## 4. async 的异常时机（testAsyncBlockThrow）

```kotlin
val deferred = async {
    throw IllegalStateException("...")
}
deferred.await()        // 异常在 await() 时才抛给调用方
```

- `async` 的异常**不会立刻**抛出，而是被封存在 `Deferred` 里；
- 调用 `await()` 时才重新抛出 → 所以可以用 `try/catch` 包住 `await()` 捕获。

---

## 总结速查

| 想达到的效果 | 正确做法 |
|---|---|
| 一个子协程失败不连累兄弟 | `SupervisorJob` 作为 scope 的 Job，或用 `supervisorScope {}` |
| 不让未捕获异常 crash | 子协程内 `try/catch`，或给**根协程**装 `CoroutineExceptionHandler` |
| 捕获 `async` 的异常 | 在 `await()` 外面 `try/catch` |
| 避免的反模式 | `launch(SupervisorJob())`——无监督效果且脱离 scope |

**最核心的一句话：**
> 监督机制管「取消传播」，异常处理管「会不会崩」，这是两件独立的事，必须都安排好。

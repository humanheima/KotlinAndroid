## 异常处理

本章节包含异常处理和取消异常。我们已经知道取消的协程会在挂起点抛出[CancellationException](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-cancellation-exception/index.html) 并且该异常被协程的机制所忽略。现在让我们看一看如果在取消过程中发生了异常，或者协程中的多个子协程抛出异常的时候会发生什么。

## 异常传播

协程构建器有两种类型：自动传播异常([launch](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/launch.html) 和 [actor](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/actor.html))或者将异常暴露给用户([async](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/async.html) 和 [produce](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/produce.html))。当使用这两种构建器构建根协程的时候，前一种类型的构建器将异常视为未捕获的异常，类似Java的`Thread.uncaughtExceptionHandler`。后一种类型的构建器则依赖用户来消费异常，例如通过 [await](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-deferred/await.html) or [receive](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/-receive-channel/receive.html)。

举个例子，我们使用 [GlobalScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-global-scope/index.html)来创建根协程。

```kotlin
private fun main() = runBlocking {
    //注释1处，创建根协程
    val job = GlobalScope.launch {
        println("Throwing exception from launch")
        throw IndexOutOfBoundsException() // Will be printed to the console by Thread.defaultUncaughtExceptionHandler
    }
    job.join()
    println("Joined failed job")

    //注释2处
    val deferred = GlobalScope.async {
        println("Throwing exception from async")
        throw ArithmeticException() // Nothing is printed, relying on user to call await
    }
    try {
        deferred.await()
        println("Unreached")
    } catch (e: ArithmeticException) {
        println("Caught ArithmeticException")
    }
}
```

注释1处，使用GlobalScope.launch创建根协程（出现异常并不会影响`runBlocking{...}`），这种类型的协程自动传播异常。

注释2处，使用async创建根协程（出现异常并不会影响`runBlocking{...}`），这种类型的协程依赖用户来消费异常。


输出结果：

```
Throwing exception from launch
Exception in thread "DefaultDispatcher-worker-1" java.lang.IndexOutOfBoundsException
	at ...
Joined failed job
Throwing exception from async
Caught ArithmeticException
```


## 协程异常处理器

我们可以自定义将未捕获的异常输出到控制台的默认行为。 根协程的 [CoroutineExceptionHandler](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-exception-handler/index.html) 这个上下文元素可以用来作为根协程及其子协程的通用的`catch`块，然后在其中自定义异常处理。和 [`Thread.uncaughtExceptionHandler`](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#setUncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)) 类似，在CoroutineExceptionHandler中，你不能从异常中恢复。当CoroutineExceptionHandler被调用的时候，协程已经以异常结束了。通常来说 CoroutineExceptionHandler 被用来打印异常，展示某些类型的错误信息，终止和/或重启应用。

在JVM平台上，可通过 [`ServiceLoader`](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) 来为所有的协程注册一个全局的 [CoroutineExceptionHandler](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-exception-handler/index.html)。 全局的异常处理器和 [`Thread.defaultUncaughtExceptionHandler`](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#setDefaultUncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)) 类似，在没有更多指定的异常处理者被注册的时候被使用。在Android平台上，`uncaughtExceptionPreHandler `被用作全局的协程异常处理器。

CoroutineExceptionHandler仅在未捕获的异常出现的时候调用。特别是，所有子协程都将异常的处理委托给其父协程，父协程也会委托给父协程，以此类推，直到根协程为止，因此子协程永远不会使用在其上下文中安装的CoroutineExceptionHandler。除此之外，[async](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/async.html) 构建器总是捕获所有的异常并且使用返回的 [Deferred](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-deferred/index.html) 来表示异常，所以CoroutineExceptionHandler对async也不起作用。

注意：在监督作用域中运行的协程不会讲异常传播到父协程。所以上述规则不适用。更多的细节请参考 [Supervision](https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/exception-handling.md#supervision) 相关章节。

```kotlin
fun main() = runBlocking {
    //声明异常处理器
    val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }
    //注释1处，GlobalScope.launch
    val job = GlobalScope.launch(handler) { //根协程
        throw AssertionError()
    }
    //注释2处，GlobalScope.async
    val deferred = GlobalScope.async(handler) { // also root, but async instead of launch
        throw ArithmeticException() //注释3处，不会打印，依赖用户调用deferred.await()
    }
    joinAll(job, deferred)
}
```

先声明了异常处理器，然后注释1处使用GlobalScope.launch创建根协程并传入handler。异常信息可以打印。

注释2处，使用GlobalScope.async创建根协程并传入handler。不会打印，依赖用户调用deferred.await()。

输出结果：

```
CoroutineExceptionHandler got java.lang.AssertionError
```

## 取消和异常

取消和异常紧密相关。协程内部使用`CancellationException`来进行取消，这些异常会被所有的异常处理器忽略，所以它们应该只被用来作为额外的调试信息的资源，可以通过`catch`块获取。当一个协程使用 [Job.cancel](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/cancel.html) 取消的时候，该协程终止，但是不会取消它的父协程。

```kotlin
fun main() = runBlocking {
    val job = launch {
        //创建子协程
        val child = launch {
            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("Child is cancelled")
            }
        }
        yield()
        println("Cancelling child")
        //取消子协程
        child.cancel()
        child.join()
        yield()
        println("Parent is not cancelled")
    }
    job.join()
}
```

输出结果：

```
Cancelling child
Child is cancelled
Parent is not cancelled
```
使用Job.cance取消子协程，但是父协程没有被取消。


如果一个协程遇到了CancellationException之外的异常，那么它会以该异常取消父协程。该行为不能被覆盖，该行为为 [structured concurrency](https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/composing-suspending-functions.md#structured-concurrency-with-async) 提供了稳定的协程层级结构。CoroutineExceptionHandler的实现并不是用于子协程的。

在上面的例子中，CoroutineExceptionHandler总是用于使用GlobalScope创建的协程。对于在`runBlocking`的作用域中启动的协程来说，使用CoroutineExceptionHandler是没有意义的，因为对于主协程来说，即使子协程设置了CoroutineExceptionHandler，当他的子协程异常终止的时候，主协程总是被取消。

父协程只有在所有的子协程完成的时候，才会处理原始的异常，如下例所示：

```kotlin
fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        //注释1处
        println("CoroutineExceptionHandler got $exception")
    }
    val job = GlobalScope.launch(handler) {
        launch { // the first child
            try {
                delay(Long.MAX_VALUE)
            } finally {
                withContext(NonCancellable) {
                    println("Children are cancelled, but exception is not handled until all children terminate")
                    delay(100)
                    //注释2处
                    println("The first child finished its non cancellable block")
                }
            }
        }
        launch { // the second child
            delay(10)
            println("Second child throws an exception")
            throw ArithmeticException()
        }
    }
    job.join()
}
```

只有注释2处的代码执行完毕以后，注释1处的异常信息才会打印出来。


输出结果：

```
Second child throws an exception
Children are cancelled, but exception is not handled until all children terminate
The first child finished its non cancellable block
CoroutineExceptionHandler got java.lang.ArithmeticException
```

## 异常聚合

当协程的多个子协程因异常而失败时， 一般规则是“取第一个异常”，因此将处理第一个异常。 在第一个异常之后发生的所有其他异常都作为被抑制的异常关联到第一个异常。

```kotlin
private fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        //注释1处
        println("Caught $exception with suppressed ${exception.suppressed?.contentToString()}")
    }
    val job = GlobalScope.launch(handler) {
        launch {
            try {
                delay(Long.MAX_VALUE)
            } finally {
                throw ArithmeticException()
            }
        }
        launch {
            delay(100)
            throw IOException()
        }
        delay(Long.MAX_VALUE)
    }
    job.join()
}
```

注释1处，获取所有被抑制的异常。

Throwable的getSuppressed方法（还是第一次知道Throwable有这个方法）

```java
public final synchronized Throwable[] getSuppressed() {
    if (suppressedExceptions == SUPPRESSED_SENTINEL || suppressedExceptions == null)
        return EMPTY_THROWABLE_ARRAY;
    else
        return suppressedExceptions.toArray(EMPTY_THROWABLE_ARRAY);
}
```

输出结果，输出了第一个异常和被抑制的异常。

```
Caught java.io.IOException with suppressed [java.lang.ArithmeticException]
```

取消异常是透明的，默认情况下是未包装的：

```kotlin
fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }
    val job = GlobalScope.launch(handler) {
        val inner = launch { // all this stack of coroutines will get cancelled
            launch {
                launch {
                    throw IOException() //原始异常
                }
            }
        }
        try {
            inner.join()
        } catch (e: CancellationException) {
            println("Rethrowing CancellationException with original cause")
            throw e //注释1处，抛出的是CancellationException，但是handler获得的是原始的IOException
        }
    }
    job.join()
}
```
注释1处，抛出的是CancellationException，但是CoroutineExceptionHandler获得的是原始的IOException。

输出结果：

```
Rethrowing CancellationException with original cause
CoroutineExceptionHandler got java.io.IOException
```

## 监督

就像我们前面研究的那样，在整个协程层级结构中，取消的传播是双向的。让我们来看一下需要单向取消的场景。

一个很好的例子是：一个UI组件，该组件在它的作用域中创建了多个Job。当该组件的任何一个子任务失败的时候，不总是必须取消整个UI组件。但是当UI组件销毁了，那么有必要所有的子任务，因为子任务的执行结果不再需要了。

另一个例子是一个服务进程孵化了多个子任务并且需要监督它们的执行，追踪它们的故障并在某个子作业执行失败的时候重启该作业。

### 监督工作

 [SupervisorJob](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-supervisor-job.html) 可以用在上述场景。它类似于一个常规的 [Job](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job.html) ，唯一的不同是 SupervisorJob 的取消只会向下传播。这是很容易用以下示例说明：

```kotlin
private fun main() = runBlocking {
    val supervisor = SupervisorJob()
    with(CoroutineScope(coroutineContext + supervisor)) {
        //注释1处，启动第一个子协程
        val firstChild = launch(CoroutineExceptionHandler { _, _ ->
            println("Caught firstChild's exception")
        }) {
            println("First child is failing")
            throw AssertionError("First child is cancelled")
        }
        //注释2处，启动第二个子协程
        val secondChild = launch {
            firstChild.join()
            //注释3处，第一个子协程的取消并没有传递到第二个子协程，所以第二个子协程依然存活
            println("First child is cancelled: ${firstChild.isCancelled}, but second one is still active")
            try {
                delay(Long.MAX_VALUE)
            } finally {
                //注释4处，当SupervisorJob取消的时候，取消传递到了子协程，子协程也会取消
                println("Second child is cancelled because supervisor is cancelled")
            }
        }
        // wait until the first child fails & completes
        firstChild.join()
        println("Cancelling supervisor")
        supervisor.cancel()
        secondChild.join()
    }
}
```

注释1处，启动第一个子协程。
注释2处，启动第二个子协程。
注释3处，第一个子协程的取消并没有传递到第二个子协程，所以第二个子协程依然存活。
注释4处，当SupervisorJob取消的时候，取消传递到了子协程，子协程也会取消。

输出结果：

```
First child is failing
Caught firstChild's exception
First child is cancelled: true, but second one is still active
Cancelling supervisor
Second child is cancelled because supervisor is cancelled
```

### 监督作用域

除了 [coroutineScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/coroutine-scope.html) 我们也可以使用 [supervisorScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/supervisor-scope.html) 来实现作用域内的并发。supervisorScope的异常传播是单向的，当supervisorScope自己失败的时候会取消所有的子协程。它也和coroutineScope一样会等待所有的子协程执行完毕。

```kotlin
private fun main() = runBlocking {
    try {
        supervisorScope {
            //创建一个子协程
            val child = launch {
                try {
                    println("Child is sleeping")
                    //注释2处，挂起
                    delay(Long.MAX_VALUE)
                } finally {
                    println("Child is cancelled")
                }
            }
            //注释1处，将当前协程调度器的线程（或者线程池让出），让子协程有机会执行。
            yield()
            println("Throwing exception from scope")
            //注释3处
            throw AssertionError()
        }
    } catch (e: AssertionError) {
        println("Caught assertion error")
    }
}
```

我们首先在supervisorScope内创建了一个子协程。
注释1处，将当前协程调度器的线程（或者线程池让出），让子协程有机会执行。
注释2处，子协程挂起很长时间。
注释3处，抛出异常，异常传递给子协程，子协程终止运行。

输出结果：

```
Child is sleeping
Throwing exception from scope
Child is cancelled
Caught assertion error
```

### 监督协程中的异常

普通的 [Job](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/index.html) 和监督作业的另一个关键区别是异常处理。监督作业中的每个子协程都应该自己处理异常。这是因为监督作业中子协程的失败不会传播到父协程。这意味着，在 [supervisorScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/supervisor-scope.html) 中直接启动的子协程和使用 [GlobalScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-global-scope/index.html) 创建的根协程一样都会使用在他们自己作用域内设置的 [CoroutineExceptionHandler](https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/exception-handling.md#coroutineexceptionhandler) 来处理异常。

```kotlin
private fun main() = runBlocking {
    
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught $exception")
    }
    supervisorScope {
        //注释1处，设置CoroutineExceptionHandler
        val child = launch(handler) {
            println("Child throws an exception")
            throw AssertionError()
        }
        println("Scope is completing")
    }
    println("Scope is completed")
}
```

注释1处，子协程`child`设置CoroutineExceptionHandler。然后当child内部抛出异常的时候是可以被捕获到的。

输出结果：

```
Scope is completing
Child throws an exception
Caught java.lang.AssertionError
Scope is completed
```

参考链接：
* [https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/exception-handling.md](https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/exception-handling.md)
* [异常处理](https://www.kotlincn.net/docs/reference/coroutines/exception-handling.html#%E7%9B%91%E7%9D%A3%E4%BD%9C%E4%B8%9A)



















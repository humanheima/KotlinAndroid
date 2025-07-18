使用的协程版本1.2.1

```
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.2.1'
```
launch和async是CoroutineScope的扩展函数。launch和async很像Java中的`Runnable`和`Callable`。


总结
用 launch 当你只需要执行任务而不需要结果。
用 async 当你需要异步计算并获取结果。 选择哪种方式取决于你的具体需求：是否需要返回值以及如何处理协程的结果和异常。


### CoroutineScope.launch

启动一个协程，不会阻塞当前线程，返回一个`Job`对象。可以调用`Job`的cancel方法取消协程。

协程的上下文是从一个CoroutineScope(比如GlobalScope)中继承的。可以通过`context`参数指定额外的协程上下文。如果协程上下文没有任何调度器(dispatcher)或任何其他协程拦截器(ContinuationInterceptor)，那么就使用默认的调度器(Dispatchers.Default)。父级Job也是从一个CoroutineScope中继承的，但是可以通过一个相应的`coroutineContext`元素来覆盖。

默认情况下，协程会被立即调度执行。其他的启动选项可以通过`start`参数指定。细节可以查看`CoroutineStart`。一个可选的`start`参数可以是`CoroutineStart.LAZY`来延迟启动一个协程。在这种情况下，launch方法返回的`Job`对象处于`_new_`状态。可以通过`Job.start`函数来显式启动这个协程，并且当第一次调用`Job.join`方法的时候该协程也会被显式启动。

默认情况下该协程中的未捕获的异常会取消父级协程，除非指定一个`CoroutineExceptionHandler`。

```kotlin
/**
 * @param context 该协程除了CoroutineScope.coroutineContext之外的上下文
 * @param 协程启动选项。默认值是[CoroutineStart.DEFAULT]
 * @param block 在所提供协程作用域的上下文中调用的协程代码
 **/
public fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val newContext = newCoroutineContext(context)
    //注释1处
    val coroutine = if (start.isLazy)
        LazyStandaloneCoroutine(newContext, block) else
        StandaloneCoroutine(newContext, active = true)
    coroutine.start(start, coroutine, block)
    return coroutine
}
```

注释1处根据协程的启动模式返回对应的协程对象，默认是StandaloneCoroutine。我们接下来看一看StandaloneCoroutine对于异常的处理。

StandaloneCoroutine间接继承JobSupport类并覆盖了handleJobException方法。JobSupport类中handleJobException方法的声明
```
/**
 * 处理最终没有被父协程处理的最终job异常。
 * 如果处理了异常返回true。
 * 该方法被设计用来被`launch-like`类型的协程(StandaloneCoroutine，ActorCoroutine)覆盖。
 * 这类协程没有可以表示异常返回类型。
 * 当Job的最终异常确定了，该方法会在Job完成之前被调用一次。在这个调用时刻，这个Job的所有子协程已经完成了。
 *
 * @suppress 这是一个不稳定的API，可能会发生更改。
 */
//默认返回false
protected open fun handleJobException(exception: Throwable): Boolean = false
```
StandaloneCoroutine覆盖了handleJobException方法。
```
override fun handleJobException(exception: Throwable): Boolean {
    //注释1处
    handleCoroutineException(context, exception)
    return true
}
```
注释1处调用了handleCoroutineException方法，这个方法是协程内部的方法。
```kotlin
/**
 * 该方法帮助协程构建器来处理协程内部未捕获的和未知的异常，
 * 这些异常无法通过结构化并发来正常处理，无法存储在一个`future`对象中也不能被重新抛出。
 * 该方法是防止丢失异常的最后一个处理步骤。
 * 如果协程上下文中有CoroutineExceptionHandler对象，那么这个CoroutineExceptionHandler对象会被使用。
 * 如果CoroutineExceptionHandler对象在处理异常中抛出了异常，或者不存在，
 * 那么通过ServiceLoader加载的所有使用CoroutineExceptionHandler实例和
 * Thread.uncaughtExceptionHandler会被调用。
 */
@InternalCoroutinesApi
public fun handleCoroutineException(context: CoroutineContext, exception: Throwable) {
    // 注释1处
    try {
        context[CoroutineExceptionHandler]?.let {
            it.handleException(context, exception)
            return
        }
    } catch (t: Throwable) {
        //注释2处，
        handleCoroutineExceptionImpl(context, handlerException(exception, t))
        return
    }
    // 注释3处
    handleCoroutineExceptionImpl(context, exception)
}
```
注释1处，如果协程上下文中存在CoroutineExceptionHandler对象，就调用它来处理异常。
注释2处，表示CoroutineExceptionHandler对象在处理异常过程中抛出了异常。注释3处，表示CoroutineExceptionHandler对象不存在。这两种情况下都使用全局的异常处理器来处理异常。

```kotlin
internal expect fun handleCoroutineExceptionImpl(context: CoroutineContext, exception: Throwable)

```
我们看一下这个方法的实现，在CoroutineExceptionHandlerImpl.kt文件中

```kotlin
//注释1处
private val handlers: List<CoroutineExceptionHandler> = 
                     CoroutineExceptionHandler::class.java.let { serviceClass ->
    ServiceLoader.load(serviceClass, serviceClass.classLoader).toList()
}

internal actual fun handleCoroutineExceptionImpl(context: CoroutineContext, exception: Throwable) {
    // 注释2处
    for (handler in handlers) {
        try {
            handler.handleException(context, exception)
        } catch (t: Throwable) {
            // 注释3处
            val currentThread = Thread.currentThread()
            currentThread.uncaughtExceptionHandler.uncaughtException(currentThread, handlerException(exception, t))
        }
    }

    // 注释4处
    val currentThread = Thread.currentThread()
    currentThread.uncaughtExceptionHandler.uncaughtException(currentThread, exception)
}
```
注释1处，加载所有CoroutineExceptionHandler接口的实现类的实例。
```
public interface CoroutineExceptionHandler : CoroutineContext.Element {
 
    public companion object Key : CoroutineContext.Key<CoroutineExceptionHandler>
 
    public fun handleException(context: CoroutineContext, exception: Throwable)
}
```
注释2处，如果存在CoroutineExceptionHandler的实现类的实例，则调用它们来处理异常。

注释3处，如果在处理过程中出现了异常，就调用当前线程的UncaughtExceptionHandler对象来处理异常。

注释4处，如果不存在CoroutineExceptionHandler的实现类的实例就直接调用当前线程的UncaughtExceptionHandler对象来处理异常。

我们看一下Android平台下的CoroutineExceptionHandlerde实现类，我这里看的是
```kotlin
@Keep
internal class AndroidExceptionPreHandler :
    AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler, Function0<Method?> {

    private val preHandler by lazy(this)

    // Reflectively lookup pre-handler. Implement Function0 to avoid generating second class for lambda
    override fun invoke(): Method? = try {
        Thread::class.java.getDeclaredMethod("getUncaughtExceptionPreHandler").takeIf {
            Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers)
        }
    } catch (e: Throwable) {
        null /* not found */
    }

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        val thread = Thread.currentThread()
        if (Build.VERSION.SDK_INT >= 28) {
            thread.uncaughtExceptionHandler.uncaughtException(thread, exception)
        } else {
            (preHandler?.invoke(null) as? Thread.UncaughtExceptionHandler)
                ?.uncaughtException(thread, exception)
        }
    }
}
```
内部还是调用当前线程的UncaughtExceptionHandler对象来处理异常，不过根据版本做了一些处理，保证应用在崩溃之前能记录异常信息。


### CoroutineScope.async

创建一个协程返回一个`Deferred`对象，可以通过Deferred(Deferred继承了Job)获取协程执行结果。可以调用Job的cancel方法取消协程。

协程的上下文是从一个CoroutineScope(比如GlobalScope)中继承的。可以通过`context`参数指定额外的协程上下文。如果协程上下文没有任何调度器(dispatcher)或任何其他协程拦截器(ContinuationInterceptor)，那么就使用默认的调度器(Dispatchers.Default)。父级Job也是从一个CoroutineScope中继承的，但是可以通过一个相应的`coroutineContext`元素来覆盖。

默认情况下，协程会被立即调度执行。其他的启动选项可以通过`start`参数指定。细节可以查看`CoroutineStart`。一个可选的`start`参数可以是`CoroutineStart.LAZY`来延迟启动一个协程。在这种情况下，async方法返回的`Deferred`对象处于`_new_`状态。可以通过`Job.start`方法来显式启动这个协程，并且当第一次调用`Job.join`方法，第一次调用`Deferred.await`方法或者`Deferred.awaitAll`方法的时候该协程也会被显式启动。

```kotlin
/**
 * @param context 该协程除了CoroutineScope.coroutineContext之外的上下文
 * @param 协程启动选项。默认值是[CoroutineStart.DEFAULT]
 * @param block 在所提供协程作用域的上下文中调用的协程代码
 **/
public fun <T> CoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    val newContext = newCoroutineContext(context)
    //注释1处
    val coroutine = if (start.isLazy)
        LazyDeferredCoroutine(newContext, block) else
        DeferredCoroutine<T>(newContext, active = true)
    coroutine.start(start, coroutine, block)
    return coroutine
}
```

### withContext

使用给定的协程上下文执行指定的阻塞代码块，阻塞直到代码块执行完毕，然后返回执行结果。

阻塞代码块最终的协程上下文是通过合并当前的coroutineContext和指定的context生成的。这个阻塞函数是可取消的。该函数立即检查最终的上下文是否处于
isActive状态，如果不是的话，抛出CancellationException。

这个函数使用最终上下文的调度器，如果指定了一个新的调度器，那么会将代码块的执行切换到不同的线程，然后当执行完毕后再切换到原始线程。
注意：`withContext`的调用结果是以一种可取消的方式分发到原始上下文的，  
这意味着当`withContext`的调度器开始执行代码块的时候如果调用`withContext`的原始上下文已经取消了，那么原始上下文就丢弃`withContext`的执行结果并抛出CancellationException。


```kotlin
public suspend fun <T> withContext(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = suspendCoroutineUninterceptedOrReturn sc@ { uCont ->
    // compute new context
    val oldContext = uCont.context
    val newContext = oldContext + context
    // always check for cancellation of new context
    newContext.checkCompletion()
    // FAST PATH #1 -- new context is the same as the old one
    if (newContext === oldContext) {
        val coroutine = ScopeCoroutine(newContext, uCont) // MODE_DIRECT
        return@sc coroutine.startUndispatchedOrReturn(coroutine, block)
    }
    // FAST PATH #2 -- the new dispatcher is the same as the old one (something else changed)
    // `equals` is used by design (see equals implementation is wrapper context like ExecutorCoroutineDispatcher)
    if (newContext[ContinuationInterceptor] == oldContext[ContinuationInterceptor]) {
        val coroutine = UndispatchedCoroutine(newContext, uCont) // MODE_UNDISPATCHED
        // There are changes in the context, so this thread needs to be updated
        withCoroutineContext(newContext, null) {
            return@sc coroutine.startUndispatchedOrReturn(coroutine, block)
        }
    }
    // SLOW PATH -- use new dispatcher
    val coroutine = DispatchedCoroutine(newContext, uCont) // MODE_ATOMIC_DEFAULT
    coroutine.initParentJob()
    block.startCoroutineCancellable(coroutine, coroutine)
    coroutine.getResult()
}


```

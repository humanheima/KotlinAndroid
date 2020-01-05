```
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_coroutine)
    job = Job()

    //启动一个协程
    launch {
        Log.d(TAG, "onCreate: 开始执行挂起函数")

        //执行挂起函数
        val events: List<Event> = RetrofitManager.get()
                .coroutineAPIService()
                .getPublicEventSuspend("humanheima")
                
        Log.d(TAG, "onCreate: 挂起函数执行完毕后恢复协程，继续向下执行")
        
        Log.d(TAG, "onCreate success: ${events.size}")
        
    }
        
    Log.d(TAG, "onCreate: 线程继续向下执行")
}
```
上面的代码输出结果如下

```
D/CoroutineActivity: onCreate: 线程继续向下执行
D/CoroutineActivity: onCreate: 开始执行挂起函数
D/CoroutineActivity: onCreate: 挂起函数执行完毕后恢复协程，继续向下执行
D/CoroutineActivity: onCreate success: 30

```
从上面的例子中可以看出来：
挂起函数挂起协程时，不会阻塞协程所在的线程，线程中的代码继续向下执行。
挂起函数执行完成后会恢复协程，协程中挂起函数后面的代码才会继续执行。
但是挂起函数只能在协程中或其他挂起函数中调用。


再看一个例子

```
private fun getPublicEvent() {
    GlobalScope.launch(Dispatchers.Main) {
        Log.d(TAG, "getPublicEvent: current thread ${Thread.currentThread().name}")
        try {
            //网络请求，并不会阻塞主线程
            val events: List<Event> = RetrofitManager.get()
                    .coroutineAPIService()
                    .publicEvent("humanheima").await()

            val builder = StringBuilder()
            for (event in events) {
                builder.append("${event.actor?.url}\n")
            }
            //更新UI展示
            tvResult.text = builder.toString()
        } catch (e: Exception) {
            Log.d(TAG, "getPublicEvent: ${e.message}")
        }
    }
}

```
上面的这个例子，我们可以理解为如下两段代码

1. Continuation可以理解为一个回调函数

```
//注意以下不是正确的代码，仅供大家理解协程使用
GlobalScope.launch(Dispatchers.Main) {
    gitHubServiceApi.getUser("bennyhuo").await(object: Continuation<User>{
            override fun resume(value: User) {
                showUser(value)
            }
            override fun resumeWithException(exception: Throwable){
                showError(exception)
            }
    })
}
```
await方法中使用handler的post方法执行continuation的成功或者失败回调
```
//注意以下并不是真实的实现，仅供大家理解协程使用
fun await(continuation: Continuation<User>): Any {
    ... // 切到非 UI 线程中执行，等待结果返回
    try {
        val user = ...
        handler.post{ continuation.resume(user) }
    } catch(e: Exception) {
        handler.post{ continuation.resumeWithException(e) }
    }
}
```



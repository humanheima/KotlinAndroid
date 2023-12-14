### 协程的概念

协程就是 Kotlin 提供的一套线程封装的 API。使用协程可以让多线程之间的通信更加简单。

举个例子：在IO线程发起网络请求，在主线程更新UI。

如果使用[Retrofit](https://github.com/square/retrofit)+回调的方式，代码类似下面这样

```kotlin
private fun normalRequest() {
    apiService.getWxarticle().enqueue(object : retrofit2.Callback<WxArticleResponse> {
        override fun onFailure(call: Call<WxArticleResponse>, t: Throwable) {
            //请求失败
        }

        override fun onResponse(call: Call<WxArticleResponse>, response: retrofit2.Response<WxArticleResponse>) {
            //请求成功，更新UI
        }
    })
}
```

如果使用[Retrofit](https://github.com/square/retrofit)+协程的方式，代码类似下面这样

```kotlin
private fun coroutineRequest2_6() {
    launch(Dispatchers.Main) {//在主线程启动协程
        val response = apiService.getWxarticle2()//在后台线程发起网络请求
        tvResult.text = sb.toString()//在主线程更新UI
    }
}
```
对比使用[Retrofit](https://github.com/square/retrofit)+回调的方式，使用协程代码明显更加简洁清晰。

关于使用协程请求网络请参考 [Kotlin协程请求网络](https://www.jianshu.com/p/f5f7b9750360)。


### 协程的挂起

看一个例子

```kotlin
private fun invokeMethod() {
       //注释1处，在主线程启动协程
        scope.launch(Dispatchers.Main) {
            Log.i(TAG, "getString: current thread " + Thread.currentThread().name)
            val result = getString()//注释2处，调用挂起函数
            etText.setText(result)//注释3处
        }
    }

    //挂起函数
    private suspend fun getString(): String {
        //切换到IO线程
        return withContext(Dispatchers.IO) {
            Log.i(TAG, "getString: current thread " + Thread.currentThread().name)
            "empty string"
        }
    }
```

输出结果

```
getString: current thread main
getString: current thread DefaultDispatcher-worker-2
```

注释1处，在主线程启动一个协程，就是指花括号里的这段代码，`scope.launch(Dispatchers.Main) {...}`。

注释2处，当协程运行到挂起函数的时候，这个协程会被挂起。什么意思呢？

1. 从挂起函数的第一个挂起点开始当前线程不再运行协程了。第一个挂起点：可以暂时理解为`withContext(Dispatchers.IO){...}`花括号里面的代码块。

解释一下：当前线程不再运行协程了，但是协程并没有停下来而是切换到其他线程上（在这里例子中是IO线程池中的线程）执行去了。当前线程继续运行其他代码。

2. 协程从挂起点恢复以后，重新切回到当前线程继续执行协程中的代码。
```
etText.setText(result)
```

###协程的挂起是非阻塞的是什么意思呢？

就是说挂起函数切到别的线程执行去了，执行完毕以后会再切回到当前线程继续执行协程中的代码。在挂起函数切到别的线程执行的这段期间，当前线程是可以继续运行其他代码的。

### 异常传播

参考链接：

* [Kotlin 的协程用力瞥一眼 - 学不会协程？很可能因为你看过的教程都是错的](https://kaixue.io/kotlin-coroutines-1/)
* [Kotlin 协程的挂起好神奇好难懂？今天我把它的皮给扒了](https://kaixue.io/kotlin-coroutines-2/)
* [到底什么是「非阻塞式」挂起？协程真的更轻量级吗？](https://kaixue.io/kotlin-coroutines-3/)
* [Kotlin协程请求网络](https://www.jianshu.com/p/f5f7b9750360)
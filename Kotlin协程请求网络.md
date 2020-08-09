本文记录一下Kotlin协程如何配合其他网络请求框架来进行网络请求。其中涉及的底层原理暂时不去关注。

本篇文章中使用到的接口来自[wanandroid](https://wanandroid.com/blog/show/2)提供的公开接口。

相关代码可以参考[KotlinAndroid](https://github.com/humanheima/KotlinAndroid)中的CoroutineOkHttpNetActivity和CoroutineRetrofitNetActivity。


## 加入依赖

```
//使用kotlin的依赖
implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"

//在Android中使用协程需要添加此依赖
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.2.1'
```

## Kotlin协程配合OkHttp

先初始化OkHttpClient
```kotlin
private lateinit var client: OkHttpClient
private lateinit var builder: OkHttpClient.Builder

override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_net)

        builder = OkHttpClient.Builder()
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000, TimeUnit.MILLISECONDS)

        client = builder
                .build()
}
```
### OkHttp正常发起网络请求

```
private fun normalRequest() {
    //请求公众号列表
    val request = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {

        override fun onFailure(call: okhttp3.Call, e: IOException) {
            Log.d(TAG, "onFailure: ${e.message}")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            val string = response.body()?.string()
            Log.d(TAG, "正常请求 onResponse: $string")
            runOnUiThread {
                tvResult.text = "正常请求 onResponse: $string"
            }
        }
    })
}
```

### 协程配合OkHttp正常发起网络请求

1. 首先给`okhttp3.Call`添加一个扩展函数，就是对`okhttp3.Call`的`enqueue`方法做了一个包装。
```
suspend fun okhttp3.Call.awaitResponse(): okhttp3.Response {

    return suspendCoroutine {
        enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                it.resumeWithException(e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                it.resume(response)
            }
        })
    }
}
```

然后我们在Activity中使用
```
class CoroutineOkHttpNetActivity : AppCompatActivity(), CoroutineScope by MainScope() {


    override fun onDestroy() {
        super.onDestroy()
        //取消所有的任务
        this.cancel()
    }

}
```
我们实现了CoroutineScope接口，然后我们将CoroutineScope的实现委托给`MainScope`。注意要在onDestroy的时候取消所有的任务。

**发起请求**
```
private fun coroutineRequest() {
    //请求公众号列表
    val request1 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()
    launch {
        try {
            //注释1处
            val response = client.newCall(request1).awaitResponse()
           //注释2处
            val string = getString(response)
            tvResult.text = "协程请求 onResponse: $string"
        } catch (e: Exception) {
            Log.d(TAG, "coroutine: error ${e.message}")
        }
    }
}
```
注释1处，调用`awaitResponse`方法发起网络请求。

注释2处，因为`okhttp3.ResponseBody`的`string`方法是一个IO操作，可能会比较耗时。所以我们通过一个挂起函数将这个操作放在后台线程执行。
```
private suspend fun getString(response: Response): String {
    return withContext(Dispatchers.IO) {
        response.body()?.string() ?: "empty string"
    }
}
```

我们在开发过程中，通常会遇到这样的场景：发起两次网络请求，第二次网络请求要依赖于第一次网络请求的结果。那我们该怎么写呢？

我们先获取公众号列表，然后查看列表中第一个公众号的历史数据。

```
private fun coroutineRequest() {
    val request1 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()

    launch {
        try {
            //第一个网络请求
            val response1 = client.newCall(request1).awaitResponse()
            val string1 = getString(response1)
            val wxArticleResponse = JsonUtilKt.instance.toObject(string1, WxArticleResponse::class.java)

            //第二个网络请求依赖于第一个网络请求结果
            val firstWxId = wxArticleResponse?.data?.get(0)?.id ?: return@launch
            //第二个网络请求
            val request2 = Request.Builder()
                    .url("https://wanandroid.com/wxarticle/list/${firstWxId}/1/json")
                    .build()
            val response2 = client.newCall(request2).awaitResponse()
            val string2 = getString(response2)

            tvResult.text = "协程请求 onResponse: ${string2}"
        } catch (e: Exception) {
            Log.d(TAG, "coroutine: error ${e.message}")
        }
    }
}
```

我们可以看到两次网络请求可以顺序书写下来，没有回调嵌套，非常简洁。


我们有时还会遇到这样的场景，需要将两次网络请求的结果合并到一起再进行下一步的操作，但是两个网络请求是没有依赖关系的，可以并发进行。我们该怎么写呢？

我们两次获取公众号列表，然后将结果合并起来。

如果按照上面的例子，我们可以这样写：

```
private fun coroutineRequest() {
    val request1 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()
    val request2 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()
    launch {
        try {
            val startTime = System.currentTimeMillis()
            //注释1处，发起两次请求    
            val response1 = client.newCall(request1).awaitResponse()
            val response2 = client.newCall(request2).awaitResponse()

            Log.d(TAG, "coroutineRequest: 网络请求消耗时间：${System.currentTimeMillis() - startTime}")
            val string1 = getString(response1)
            val string2 = getString(response2)
            
            //合并两次请求的结果更新UI
            tvResult.text = "协程请求 onResponse: ${string1 + string2}"
        } catch (e: Exception) {
            Log.d(TAG, "coroutine: error ${e.message}")
        }
    }
}
```
这种写法是不合适的，为什么呢？因为这两次请求是顺序执行的。在我的设备上多次执行coroutineRequest方法，两次网络请求最少的耗时时间是70毫秒。打印日志：
```
D/CoroutineOkHttpNetActiv: coroutineRequest: 网络请求消耗时间：70
```

正确的写法，并行发起请求。
```
    private fun coroutineRequest3() {
        val request1 = Request.Builder()
                .url("https://wanandroid.com/wxarticle/chapters/json")
                .build()
        val request2 = Request.Builder()
                .url("https://wanandroid.com/wxarticle/chapters/json")
                .build()

        launch {
            try {
                val startTime = System.currentTimeMillis()
                //注释1处，使用async要重新指定协程上下文，不然会出现有些异常捕捉不到造成崩溃
                withContext(Dispatchers.Main) {
                    //两次网络请求没有依赖关系，可以并发请求
                    val deferred1 = async { client.newCall(request1).awaitResponse() }
                    val deferred2 = async { client.newCall(request2).awaitResponse() }
                    val response1 = deferred1.await()
                    val response2 = deferred2.await()
                    Log.d(TAG, "coroutineRequest: 并发网络请求消耗时间：${System.currentTimeMillis() - startTime}")
                    val string1 = getString(response1)
                    val string2 = getString(response2)
                    tvResult.text = "协程请求 onResponse: ${string1 + string2}"
                }
            } catch (e: Exception) {
                Log.d(TAG, "coroutine: error ${e.message}")
            }
        }
    }

```

在我的设备上，并发执行两次网络请求最少的耗时时间是45毫秒。打印日志：
```
D/CoroutineOkHttpNetActiv: coroutineRequest: 并发网络请求消耗时间：45
```
**注意：使用async要重新指定协程上下文，不然会出现有些异常捕捉不到造成崩溃，至于其中原理暂时还没搞清楚。**

## Kotlin协程配合Retrofit

定义接口方法
```
interface ApiService {

    @GET("wxarticle/chapters/json")
    fun getWxarticle(): Call<WxArticleResponse>

}
```

构建接口实例
```
private val apiService = Retrofit.Builder()
            .baseUrl("https://www.wanandroid.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiService::class.java)
```

### Retrofit正常请求

```
private fun normalRequest() {
    apiService.getWxarticle().enqueue(object : retrofit2.Callback<WxArticleResponse> {
        override fun onFailure(call: Call<WxArticleResponse>, t: Throwable) {
            Log.d(TAG, "onFailure: ${t.message}")
        }

        override fun onResponse(call: Call<WxArticleResponse>, response: retrofit2.Response<WxArticleResponse>) {
            if (response.isSuccessful) {
                val wxArticleResponse = response.body()
                val sb = StringBuilder("Retrofit正常请求：\n")
                wxArticleResponse?.data?.forEach {
                    sb.append(it.name)
                    sb.append("\n")
                }
                tvResult.text = sb.toString()
            }
        }
    })
}
```

### 协程配合Retrofit（2.6.0以下版本）正常网络请求

1. 首先给`Retrofit.Call`类添加一个扩展函数，就是对`retrofit2.Call`的`enqueue`方法做了一个包装。
```
suspend fun <T : Any?> Call<T>.awaitResponse(): T {

    return suspendCoroutine {
        enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                it.resumeWithException(t)
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        it.resume(body)
                    } else {
                        it.resumeWithException(Throwable(response.toString()))
                    }

                } else {
                    it.resumeWithException(Throwable(response.toString()))
                }
            }
        })
    }
}
```
在Activity中使用
```
class CoroutineOkHttpNetActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onDestroy() {
        super.onDestroy()
        //取消所有的任务
        this.cancel()
    }

}
```
我们实现了CoroutineScope接口，然后我们将CoroutineScope的实现委托给MainScope。注意要在onDestroy的时候取消所有的任务。

```
private fun coroutineRequest() {
    launch {
        try {
            val response: WxArticleResponse = apiService.getWxarticle().awaitResponse()
            val sb = StringBuilder("Retrofit配合协程请求：\n")
            response.data.forEach {
                sb.append(it.name)
                sb.append("\n")
            }
            tvResult.text = sb.toString()

        } catch (e: Exception) {
            Log.d(TAG, "coroutineRequest: error: ${e.message}")
        }

    }
}
```

### 协程配合Retrofit（2.6.0及以上版本）发起网络请求

1. 不再需要给给Retrofit.Call类添加扩展函数。
2. 接口方法声明可以简化，方法返回类型直接声明成需要的数据类型。
```
interface ApiService {

    //Retrofit2.6.0以下方法声明
    //@GET("wxarticle/chapters/json")
    //fun getWxarticle(): Call<WxArticleResponse>

    //Retrofit2.6.0及以上方法声明
    @GET("wxarticle/chapters/json")
    suspend fun getWxarticle2(): WxArticleResponse

}
```
发起请求
```
private fun coroutineRequest2_6() {
    launch {
        try {
            val response = apiService.getWxarticle2()
            val sb = StringBuilder("Retrofit2.6配合协程请求：\n")
            response.data.forEach {
                sb.append(it.name)
                sb.append("\n")
            }
            tvResult.text = sb.toString()
        } catch (e: Exception) {
            Log.d(TAG, "coroutine: error ${e.message}")
        }
    }
}
```

### 协程异常处理

上面的协程请求，为了捕获异常，我们是放在`try/catch`中进行的。有没有更优雅的办法处理呢？有的。

1. 首先初始化一个`CoroutineExceptionHandler`实例。
```
val handler = CoroutineExceptionHandler { _, exception ->
    Log.d(TAG, "Caught original $exception")
}
```
使用
```
private fun coroutineRequest2() {
    launch(handler) {
        val response = apiService.getWxarticle2()
        val sb = StringBuilder("Retrofit2.6配合协程请求：\n")
        response.data.forEach {
            sb.append(it.name)
            sb.append("\n")
        }
        tvResult.text = sb.toString()
    }
}
```
## 返回结果的统一处理


我们的返回结果通常有两种样式

样式1：data是一个JSONObject
```
{
  "data": {},
  "errorCode": 0,
  "errorMsg": ""
}
```
样式2：data是一个JSONArray
```
{
  "data": [],
  "errorCode": 0,
  "errorMsg": ""
}
```

首先定义通用的响应类
```
class NetResponse<T> {
    var data: T? = null
    var errorMsg = ""
    var errorCode = 0


    fun success() = errorCode == 0
}
```

### Retrofit2.6.0以下格式统一处理

定义接口方法
```kotlin

//data是JSONObject
@GET("article/list/1/json")
fun getArticleLowLevelFormat1(): Call<NetResponse<Article>>

//data是JSONArray
@GET("wxarticle/chapters/json")
fun getWxarticleListLowLevelFormat2(): Call<NetResponse<MutableList<WxArticleResponse.DataBean>>>
```

使用
```kotlin
private fun handlerLowLevelResponseFormat1() {
    launch(handler) {
        //需要借助Retrofit.Call类的扩展方法
        val response: NetResponse<Article> =
                apiService.getArticleLowLevelFormat1().awaitResponse()
        if (response.success()) {
            val sb = StringBuilder("Retrofit2.6以下响应格式统一处理1：\n")
            response.data?.datas?.forEach {
                sb.append(it.title)
                sb.append("\n")
            }
            tvResult.text = sb.toString()
        } else {
           Log.d(TAG, "handlerLowLevelResponseFormat1: failed ${response.errorMsg}")
        }
    }
}

private fun handlerLowLevelResponseFormat2() {
    launch(handler) {
        val response: NetResponse<MutableList<WxArticleResponse.DataBean>> =
                apiService.getWxarticleListLowLevelFormat2().awaitResponse()
        if (response.success()) {
            val sb = StringBuilder("Retrofit2.6以下响应格式统一处理2：\n")
            response.data?.forEach {
                sb.append(it.name)
                sb.append("\n")
                Log.d(TAG, "handlerLowLevelResponseFormat2: ${it.name}")
            }
            tvResult.text = sb.toString()
        } else {
            Log.d(TAG, "handlerLowLevelResponseFormat2: failed ${response.errorMsg}")
        }
    }
}

```

### Retrofit2.6.0以上格式统一处理

定义接口方法
```
//data是JSONObject
@GET("article/list/1/json")
suspend fun getArticle(): NetResponse<Article>

//data是JSONArray
@GET("wxarticle/chapters/json")
suspend fun getWxarticleList(): NetResponse<MutableList<WxArticleResponse.DataBean>>
```
使用
```
 private fun handlerResponseFormat1() {
    launch(handler) {
        val response = apiService.getArticle()
        if (response.success()) {
            val article: Article? = apiService.getArticle().data
            val sb = StringBuilder("handlerResponseFormat1：\n")

            article?.datas?.forEach {
                sb.append(it.title)
                sb.append("\n")
                Log.d(TAG, "handlerResponseFormat1: ${it.title}")
            }
            tvResult.text = sb.toString()
        } else {
            Log.d(TAG, "handlerResponseFormat1: failed ${response.errorMsg}")
        }
    }
}

private fun handlerResponseFormat2() {
    launch(handler) {
        val response = apiService.getWxarticleList()
        if (response.success()) {
            val articleList: MutableList<WxArticleResponse.DataBean>? = apiService.getWxarticleList().data
            val sb = StringBuilder("handlerResponseFormat2：\n")
            articleList?.forEach {
                sb.append(it.name)
                sb.append("\n")
                Log.d(TAG, "handlerResponseFormat2: ${it.name}")
            }
            tvResult.text = sb.toString()
        } else {
            Log.d(TAG, "handlerResponseFormat2: failed ${response.errorMsg}")
        }
    }
}
```

其他：
* 协程 launch 和 async 的区别参考 [What is the difference between launch/join and async/await in Kotlin coroutines](https://stackoverflow.com/questions/46226518/what-is-the-difference-between-launch-join-and-async-await-in-kotlin-coroutines)

参考链接：
* [Android中用Kotlin Coroutine(协程)和Retrofit进行网络请求和取消请求](https://juejin.im/post/5cbd890bf265da03594871a5)
* [Retrofit 2.6.0 ! 更快捷的协程体验 ！](https://juejin.im/post/5d0793616fb9a07eac05d407)
* [Android 开发中 Kotlin Coroutines 如何优雅地处理异常](https://www.jianshu.com/p/2056d5424001)
* [https://github.com/Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines)
* [What is the difference between launch/join and async/await in Kotlin coroutines](https://stackoverflow.com/questions/46226518/what-is-the-difference-between-launch-join-and-async-await-in-kotlin-coroutines)



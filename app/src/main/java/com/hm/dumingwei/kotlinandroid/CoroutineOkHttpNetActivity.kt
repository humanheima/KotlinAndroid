package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hm.dumingwei.JsonUtilKt
import com.hm.dumingwei.kotlinandroid.databinding.ActivityCoroutineNetBinding
import com.hm.dumingwei.mvp.model.bean.WxArticleResponse
import com.hm.dumingwei.net.awaitResponse
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by dumingwei on 2020/4/24
 *
 * Desc: 使用协程和OkHttp做网络请求
 */
class CoroutineOkHttpNetActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var client: OkHttpClient
    private lateinit var builder: OkHttpClient.Builder

    private val TAG: String? = "CoroutineOkHttpNetActiv"

    private lateinit var scope: CoroutineScope

    companion object {

        @JvmStatic
        fun launch(context: Context) {
            val intent = Intent(context, CoroutineOkHttpNetActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityCoroutineNetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoroutineNetBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        builder = OkHttpClient.Builder()
            //.addInterceptor(new LoggingInterceptor())
            //.addNetworkInterceptor(new LoggingInterceptor())
            //.addInterceptor(interceptor)
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .writeTimeout(10000, TimeUnit.MILLISECONDS)

        client = builder
            .build()

        binding.btnNormalRequest.setOnClickListener {
            binding.tvResult.text = null
            normalRequest()
        }

        binding.btnCoroutineRequest.setOnClickListener {
            binding.tvResult.text = null
            coroutineRequest()
        }
        binding.btnCoroutineRequest1.setOnClickListener {
            binding.tvResult.text = null
            coroutineRequest1()
        }
        binding.btnCoroutineRequest2.setOnClickListener {
            binding.tvResult.text = null
            coroutineRequest2()
        }
        binding.btnCoroutineRequest3.setOnClickListener {
            binding.tvResult.text = null
            coroutineRequest3()
        }

        binding.btnCatchException.setOnClickListener {
            binding.tvResult.text = null
            requestCanCatchException()
        }

        binding.btnCanNotCatchException.setOnClickListener {
            binding.tvResult.text = null
            requestCanNotCatchException()
        }
        binding.btnCancelCoroutine.setOnClickListener {
            binding.tvResult.text = null
            cancelCoroutine()
        }

    }

    //注释1处
    val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.d(TAG, "coroutine: error ${throwable.message}")
        }

    private fun coroutineRequest() {
        //请求公众号列表
        val request1 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()

        //使用exceptionHandler
        launch(exceptionHandler) {
            //注释2处
            val response = client.newCall(request1).awaitResponse()
            //注释3处
            val string = getString(response)
            //合并两次请求的结果更新UI
            binding.tvResult.text = "协程请求 onResponse: $string"
        }
    }

    private fun coroutineRequest1() {
        val request1 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()

        launch(exceptionHandler) {
            //val startTime = System.currentTimeMillis()
            //第一个网络请求
            val response1 = client.newCall(request1).awaitResponse()
            val string1 = getString(response1)
            val wxArticleResponse =
                JsonUtilKt.instance.toObject(string1, WxArticleResponse::class.java)

            //第二个网络请求依赖于第一个网络请求结果
            val firstWxId = wxArticleResponse?.data?.get(0)?.id ?: return@launch
            val request2 = Request.Builder()
                .url("https://wanandroid.com/wxarticle/list/${firstWxId}/1/json")
                .build()
            val response2 = client.newCall(request2).awaitResponse()

            //Log.d(TAG, "coroutineRequest: 网络请求消耗时间：${System.currentTimeMillis() - startTime}")
            val string2 = getString(response2)

            binding.tvResult.text = "协程请求 onResponse: ${string2}"

        }
    }

    private fun coroutineRequest2() {
        val request1 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()
        val request2 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()
        launch(exceptionHandler) {
            val startTime = System.currentTimeMillis()
            //注释1处，发起两次请求
            val response1 = client.newCall(request1).awaitResponse()
            val response2 = client.newCall(request2).awaitResponse()

            Log.d(
                TAG,
                "coroutineRequest: 顺序网络请求消耗时间：${System.currentTimeMillis() - startTime}"
            )
            val string2 = getString(response2)
            val string1 = getString(response1)
            //合并两次请求的结果更新UI
            binding.tvResult.text = "协程请求 onResponse: ${string1 + string2}"
        }
    }

    /**
     * 并发执行两个网络请求
     */
    private fun coroutineRequest3() {
        val request1 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()
        val request2 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()

        launch(exceptionHandler) {
            val startTime = System.currentTimeMillis()
            //两次网络请求没有依赖关系，可以并发请求
            val deferred1 = async { client.newCall(request1).awaitResponse() }
            val deferred2 = async { client.newCall(request2).awaitResponse() }
            val response1 = deferred1.await()
            val response2 = deferred2.await()
            Log.d(
                TAG,
                "coroutineRequest: 并发网络请求消耗时间：${System.currentTimeMillis() - startTime}"
            )
            val string1 = getString(response1)
            val string2 = getString(response2)
            binding.tvResult.text = "协程请求 onResponse: ${string1 + string2}"
        }
    }

    private fun cancelCoroutine() {
        val request1 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()

        val job = launch {
            try {
                val startTime = System.currentTimeMillis()
                //注释1处，发起两次请求
                val response1 = client.newCall(request1).awaitResponse()
                val string1 = getString(response1)
                binding.tvResult.text = "协程请求 onResponse: $string1"
            } catch (e: Exception) {
                Log.d(TAG, "cancelCoroutine: error ${e.message}")
            }
        }

        launch {
            delay(200)
            //取消协程
            job.cancel()
        }

    }

    /**
     * 使用https://api.github.com/这个接口来测试 exception 是否能被捕获住
     *
     * 这个方法可以抓住异常，注意和 requestCanNotCatchException 方法对比
     */
    private fun requestCanCatchException() {
        val request1 = Request.Builder()
            .url("https://api.github.com/users/humanheima/events/public")
            .build()
        launch(exceptionHandler) {
            Log.d(TAG, "coroutineRequest: ${Thread.currentThread().name}")
            val response: Deferred<Response> = async {
                throw IllegalAccessException("exc")
                client.newCall(request1).awaitResponse()
            }
            val string = getString(response.await())
            binding.tvResult.text = "协程请求 onResponse: $string"
        }
    }

    /**
     * 使用https://api.github.com/这个接口来测试 exception 是否能被捕获住
     * 这个方法抓不住异常
     */
    private fun requestCanNotCatchException() {
        val request1 = Request.Builder()
            .url("https://api.github.com/users/humanheima/events/public")
            .header("User-Agent", "OkHttp Example")
            .build()
        launch {
            Log.e(TAG, "coroutineRequest: scope =  ${this}")
            supervisorScope {
                Log.e(TAG, "coroutineRequest: scope = ${this}")
                val response: Deferred<Response> = async {
                    throw IllegalAccessException("exc")
                    client.newCall(request1).awaitResponse()
                }
                try {
                    val string = getString(response.await())
                    binding.tvResult.text = "协程请求 onResponse: $string"
                } catch (e: Exception) {
                    Log.d(TAG, "coroutine: error ${e.message}")
                }
            }
        }
    }

    private fun requestCanNotCatchException3() {
        val request1 = Request.Builder()
            .url("https://api.github.com/users/humanheima/events/public")
            .header("User-Agent", "OkHttp Example")
            .build()
        //这里指定SupervisorJob不起作用
        launch(SupervisorJob()) {
            Log.e(TAG, "coroutineRequest: ${coroutineContext}")
            Log.e(TAG, "coroutineRequest: ${this}")
            //asyn返回的是一个Job不是SupervisorJob，所以async会传播异常，导致try/catch代码块不起作用
            val response: Deferred<Response?> = async {
                /*try {
                } catch (e: Exception) {
                    Log.e(TAG, "coroutine in async : error ${e.message}")

                }*/
                throw IllegalAccessException("exc")
                //client.newCall(request1).awaitResponse()
                null
            }
            try {
                val data = response.await()
                //val string = getString(response.await())
                //tvResult.text = "协程请求 onResponse: $string"
            } catch (e: Exception) {
                Log.d(TAG, "coroutine: error ${e.message}")
            }
        }
    }

    private fun requestCanNotCatchException2() {
        val request1 = Request.Builder()
            .url("https://api.github.com/users/humanheima/events/public")
            .header("User-Agent", "OkHttp Example")
            .build()
        launch {
            Log.e(TAG, "coroutineRequest: ${coroutineContext}")
            Log.e(TAG, "coroutineRequest: ${this}")
            //如果 async 抛出异常，launch 就会立即抛出异常，而不会调用 .await()，所以 try/catch也抓不住异常
            val response: Deferred<Response?> = async {
                /*try {
                } catch (e: Exception) {
                    Log.e(TAG, "coroutine in async : error ${e.message}")

                }*/
                throw IllegalAccessException("exc")
                //client.newCall(request1).awaitResponse()
                null
            }
            try {
                val data = response.await()
                //val string = getString(response.await())
                //tvResult.text = "协程请求 onResponse: $string"
            } catch (e: Exception) {
                Log.d(TAG, "coroutine: error ${e.message}")
            }
        }
    }

    /**
     * 使用https://api.github.com/这个接口来测试 exception 是否能被捕获住
     * 这个方法抓不住异常
     */
    private fun requestCanNotCatchException1() {
        val request1 = Request.Builder()
            .url("https://api.github.com/users/humanheima/events/public")
            .header("User-Agent", "OkHttp Example")
            .build()
        launch {
            Log.e(TAG, "coroutineRequest: ${coroutineContext}")
            try {
                val response: Deferred<Response> = async {
                    throw IllegalAccessException("exc")
                    client.newCall(request1).awaitResponse()
                }
                val data = response.await()
                //val string = getString(response.await())
                //tvResult.text = "协程请求 onResponse: $string"
            } catch (e: Exception) {
                Log.d(TAG, "coroutine: error ${e.message}")
            }
        }
    }

    private suspend fun getString(response: Response): String {
        return withContext(Dispatchers.IO) {
            response.body()?.string() ?: "empty string"
        }
    }

    private fun normalRequest() {
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
                    binding.tvResult.text = "正常请求 onResponse: $string"
                }
            }
        })
    }

    override fun onDestroy() {
        //取消所有的任务
        this.cancel()
        super.onDestroy()
    }
}

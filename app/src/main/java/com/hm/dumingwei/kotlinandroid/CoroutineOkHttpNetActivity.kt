package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_coroutine_net.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
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

    companion object {

        @JvmStatic
        fun launch(context: Context) {
            val intent = Intent(context, CoroutineOkHttpNetActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_net)

        builder = OkHttpClient.Builder()
                //.addInterceptor(new LoggingInterceptor())
                //.addNetworkInterceptor(new LoggingInterceptor())
                //.addInterceptor(interceptor)
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000, TimeUnit.MILLISECONDS)

        client = builder
                .build()

        btnNormalRequest.setOnClickListener {
            normalRequest()
        }

        btnCoroutineRequest.setOnClickListener {
            coroutineRequest()

        }

    }

    private fun coroutineRequest() {
        val request1 = Request.Builder()
                .url("http://www.publicobject.com/helloworld.txt")
                .header("User-Agent", "OkHttp Example")
                .build()
        launch {
            try {
                val response = client.newCall(request1).await()
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "协程请求 onResponse: ${response.body()?.string()}")
                }

            } catch (e: Exception) {
                Log.d(TAG, "coroutine: error ${e.message}")
            }
        }
    }

    private fun normalRequest() {
        val request = Request.Builder()
                .url("http://www.publicobject.com/helloworld.txt")
                .header("User-Agent", "OkHttp Example")
                .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Log.d(TAG, "正常请求 onResponse: ${response.body()?.string()}")

            }
        })
    }

    override fun onDestroy() {
        //取消所有的任务
        this.cancel()
        super.onDestroy()
    }
}

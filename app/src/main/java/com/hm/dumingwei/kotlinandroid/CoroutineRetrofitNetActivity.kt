package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.hm.dumingwei.mvp.model.bean.WxArticleResponse
import com.hm.dumingwei.net.ApiService
import kotlinx.android.synthetic.main.activity_coroutine_retrofit_net.*
import kotlinx.coroutines.*
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by dumingwei on 2020/4/24
 *
 * Desc: 使用协程和OkHttp做网络请求
 */
class CoroutineRetrofitNetActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val TAG: String? = "CoroutineRetrofitNetAct"

    private val apiService = Retrofit.Builder()
            .baseUrl("https://www.wanandroid.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiService::class.java)

    companion object {

        @JvmStatic
        fun launch(context: Context) {
            val intent = Intent(context, CoroutineRetrofitNetActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_retrofit_net)

        btnNormalRequest.setOnClickListener {
            tvResult.text = null
            normalRequest()
        }

        btnCoroutineRequest.setOnClickListener {
            tvResult.text = null
            coroutineRequest()
            //coroutineRequest1()

        }
        btnCoroutineRequest1.setOnClickListener {
            tvResult.text = null
            coroutineRequest2_6()
        }
        btnCoroutineRequest2.setOnClickListener {
            coroutineRequest2()
        }
    }

    private fun coroutineRequest() {
        launch {
            try {
                //需要借助Retrofit.Call类的扩展方法
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

    /**
     *
     * 2.6版本的Retrofit的使用
     */
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


    val handler = CoroutineExceptionHandler { _, exception ->
        Log.d(TAG, "Caught original $exception")
    }

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

    private suspend fun getString(response: Response): String {
        return withContext(Dispatchers.IO) {
            response.body()?.string() ?: "empty string"
        }
    }

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

    override fun onDestroy() {
        //取消所有的任务
        this.cancel()
        super.onDestroy()
    }
}

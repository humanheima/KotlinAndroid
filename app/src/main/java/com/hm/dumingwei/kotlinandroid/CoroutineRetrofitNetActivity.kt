package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hm.dumingwei.mvp.model.bean.Article
import com.hm.dumingwei.mvp.model.bean.WxArticleResponse
import com.hm.dumingwei.net.ApiService
import com.hm.dumingwei.net.HttpLoggingInterceptor
import com.hm.dumingwei.net.NetResponse
import com.hm.dumingwei.net.awaitResponse
import kotlinx.android.synthetic.main.activity_coroutine_retrofit_net.btnCoroutineRequest
import kotlinx.android.synthetic.main.activity_coroutine_retrofit_net.btnCoroutineRequest1
import kotlinx.android.synthetic.main.activity_coroutine_retrofit_net.btnCoroutineRequest2
import kotlinx.android.synthetic.main.activity_coroutine_retrofit_net.btnHandleLowLevelResponseFormat1
import kotlinx.android.synthetic.main.activity_coroutine_retrofit_net.btnHandleLowLevelResponseFormat2
import kotlinx.android.synthetic.main.activity_coroutine_retrofit_net.btnHandleResponseFormat1
import kotlinx.android.synthetic.main.activity_coroutine_retrofit_net.btnHandleResponseFormat2
import kotlinx.android.synthetic.main.activity_coroutine_retrofit_net.btnNormalRequest
import kotlinx.android.synthetic.main.activity_coroutine_retrofit_net.tvResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException


/**
 * Created by dumingwei on 2020/4/24
 *
 * Desc: 使用协程和OkHttp做网络请求
 */
class CoroutineRetrofitNetActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val TAG: String? = "CoroutineRetrofitNetAct"


    private val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Level.BODY)

    private lateinit var apiService: ApiService

    private lateinit var scope: CoroutineScope

    companion object {

        @JvmStatic
        fun launch(context: Context) {
            val intent = Intent(context, CoroutineRetrofitNetActivity::class.java)
            context.startActivity(intent)
        }
    }

    // 创建一个信任所有证书的 TrustManager
    val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory


        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(object : HostnameVerifier {
                override fun verify(
                    hostname: String?,
                    session: javax.net.ssl.SSLSession?
                ): Boolean {
                    return true
                }
            })
            .addInterceptor(httpLoggingInterceptor)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl("https://www.wanandroid.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build().create(ApiService::class.java)

        scope = MainScope() + CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.d(TAG, "coroutine: error ${throwable.message}")
        }

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

        btnHandleResponseFormat1.setOnClickListener {
            handlerResponseFormat1()
        }
        btnHandleResponseFormat2.setOnClickListener {
            handlerResponseFormat2()
        }
        btnHandleLowLevelResponseFormat1.setOnClickListener {
            handlerLowLevelResponseFormat1()
        }
        btnHandleLowLevelResponseFormat2.setOnClickListener {
            handlerLowLevelResponseFormat2()
        }
    }

    /**
     * 子协程 CoroutineExceptionHandler 不起作用，
     * SupervisorJob 内部的写成也会捕获异常，不会使用 CoroutineExceptionHandler
     */
    //注释1处
    val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.d(TAG, "coroutine: error ${throwable.message}")
        }

    private fun coroutineRequest() {
        launch {
            //需要借助Retrofit.Call类的扩展方法
            val response: WxArticleResponse = apiService.getWxarticle().awaitResponse()
            val sb = StringBuilder("Retrofit配合协程请求：\n")
            response.data.forEach {
                sb.append(it.name).append("\n")
            }
            tvResult.text = sb.toString()
        }
    }

    /**
     *
     * 2.6版本的Retrofit的使用
     */
    private fun coroutineRequest2_6() {
        launch(exceptionHandler) {
            val response = apiService.getWxarticle2Temp("", 1, null)
            val sb = StringBuilder("Retrofit2.6配合协程请求：\n")
            response.data.forEach { sb.append(it.name).append("\n") }
            tvResult.text = sb.toString()
        }
    }


    private fun coroutineRequest2() {
        launch(exceptionHandler) {
            val response = apiService.getWxarticle2()
            val sb = StringBuilder("Retrofit2.6配合协程请求：\n")
            response.data.forEach { sb.append(it.name).append("\n") }
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

            override fun onResponse(
                call: Call<WxArticleResponse>,
                response: retrofit2.Response<WxArticleResponse>
            ) {
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

    private fun handlerResponseFormat1() {
        launch(exceptionHandler) {
            val response = apiService.getArticle()
            if (response.success()) {
                val article: Article? = apiService.getArticle().data
                val sb = StringBuilder("handlerResponseFormat1：\n")
                article?.datas?.forEach {
                    sb.append(it.title).append("\n")
                    Log.d(TAG, "handlerResponseFormat1: ${it.title}")
                }
                tvResult.text = sb.toString()
            } else {
                Log.d(TAG, "handlerResponseFormat1: failed ${response.errorMsg}")
            }
        }
    }

    private fun handlerResponseFormat2() {
        launch(exceptionHandler) {
            val response = apiService.getWxarticleList()
            if (response.success()) {
                //val articleList: MutableList<WxArticleResponse.DataBean>? = apiService.getWxarticleList().data
                val articleList: MutableList<WxArticleResponse.DataBean>? = response.data
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

    private fun handlerLowLevelResponseFormat1() {
        launch(exceptionHandler) {
            //需要借助Retrofit.Call类的扩展方法
            val response: NetResponse<Article> =
                apiService.getArticleLowLevelFormat1().awaitResponse()
            if (response.success()) {
                val sb = StringBuilder("Retrofit2.6以下响应格式统一处理1：\n")
                response.data?.datas?.forEach { sb.append(it.title).append("\n") }
                tvResult.text = sb.toString()
            } else {
                Log.d(TAG, "handlerLowLevelResponseFormat1: failed ${response.errorMsg}")
            }
        }
    }

    private fun handlerLowLevelResponseFormat2() {
        launch(exceptionHandler) {
            val response: NetResponse<MutableList<WxArticleResponse.DataBean>> =
                apiService.getWxarticleListLowLevelFormat2().awaitResponse()
            if (response.success()) {
                val sb = StringBuilder("Retrofit2.6以下响应格式统一处理2：\n")
                response.data?.forEach {
                    sb.append(it.name).append("\n")
                    Log.d(TAG, "handlerLowLevelResponseFormat2: ${it.name}")
                }
                tvResult.text = sb.toString()
            } else {
                Log.d(TAG, "handlerLowLevelResponseFormat2: failed ${response.errorMsg}")
            }
        }
    }

    override fun onDestroy() {
        //取消所有的任务
        this.cancel()
        super.onDestroy()
    }
}

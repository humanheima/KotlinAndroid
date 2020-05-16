package com.hm.dumingwei.kotlinandroid.handbook.thirteen

import com.hm.dumingwei.net.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by dmw on 2019/1/7.
 * Desc:
 */
class RetrofitManager private constructor() {

    companion object {

        private lateinit var mRetrofit: Retrofit

        fun get(): RetrofitManager {
            return Holder.MANAGER
        }
    }

    private object Holder {
        val MANAGER = RetrofitManager()
    }

    private val apiService: APIService

    private val adService: ADService

    private val coroutineAPIService: CoroutineAPIService

    private val okhttpClient: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()
        builder.writeTimeout((5 * 1000).toLong(), TimeUnit.MILLISECONDS)
        builder.readTimeout((5 * 1000).toLong(), TimeUnit.MILLISECONDS)
        builder.connectTimeout((5 * 1000).toLong(), TimeUnit.MILLISECONDS)
        builder.addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Level.BODY))

        okhttpClient = builder.build()

        mRetrofit = Retrofit.Builder()
                .baseUrl(APIService.API_BASE_SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okhttpClient)
                .build()

        apiService = mRetrofit.create(APIService::class.java)

        adService = mRetrofit.create(ADService::class.java)

        coroutineAPIService = mRetrofit.create(CoroutineAPIService::class.java)
    }

    fun retrofit(): Retrofit = mRetrofit

    fun apiService(): APIService = apiService

    // 新增的 adService() 用于调用 ADService 中的接口
    fun adService(): ADService = adService

    fun coroutineAPIService() = coroutineAPIService

    fun okhttpClient(): OkHttpClient = okhttpClient

}
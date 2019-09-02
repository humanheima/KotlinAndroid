package com.hm.dumingwei.kotlinandroid.handbook.thirteen

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by dmw on 2019/1/7.
 * Desc: 测试使用协程发起网络请求
 */
interface CoroutineAPIService {

    companion object {

        const val API_BASE_SERVER_URL = "https://api.github.com/"
    }

    @GET("users/{username}/events/public")
    fun publicEvent(@Path("username") userName: String): Call<List<Event>>


    @GET("users/{username}/events/public")
    suspend fun getPublicEventSuspend(@Path("username") userName: String): List<Event>

    @GET("users/{username}/events/public")
    suspend fun getPublicEvent(@Path("username") userName: String): Response<List<Event>>

}
package com.hm.dumingwei.kotlinandroid.handbook.thirteen

import io.reactivex.Maybe
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by dmw on 2019/1/7.
 * Desc:
 */
interface APIService {

    companion object {

        const val API_BASE_SERVER_URL = "https://api.github.com/"
    }

    @GET("users/{username}/events/public")
    fun publicEvent(@Path("username") userName: String): Maybe<List<Event>>
}
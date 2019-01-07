package com.hm.dumingwei.kotlinandroid.handbook.thirteen

import io.reactivex.Maybe
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by dmw on 2019/1/7.
 * Desc:
 */
interface ADService {

    @GET("https://ad/nowad")
    fun getAd(@Path("username") userName: String): Maybe<List<String>>


}
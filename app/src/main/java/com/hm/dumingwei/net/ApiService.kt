package com.hm.dumingwei.net

import com.hm.dumingwei.mvp.model.bean.GankResult
import retrofit2.Call
import retrofit2.http.GET

/**
 * Created by dumingwei on 2020/4/24.
 *
 * Desc:
 */
interface ApiService {

    @GET("data/iOS/2/1")
    fun getIOSGank(): Call<GankResult>

    @GET("data/Android/2/1")
    fun getAndroidGank(): Call<GankResult>

}
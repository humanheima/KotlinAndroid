package com.hm.dumingwei.net

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by dumingwei on 2020/4/24.
 *
 * Desc:
 */
class ApiSource {

    companion object {

        @JvmField
        val instance = Retrofit.Builder()
                .baseUrl("http://gank.io/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiService::class.java)
    }
}
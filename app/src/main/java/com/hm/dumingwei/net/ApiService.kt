package com.hm.dumingwei.net

import com.hm.dumingwei.mvp.model.bean.WxArticleResponse
import retrofit2.Call
import retrofit2.http.GET

/**
 * Created by dumingwei on 2020/4/24.
 *
 * Desc:
 */
interface ApiService {


    @GET("wxarticle/chapters/json")
    fun getWxarticle(): Call<WxArticleResponse>

    @GET("wxarticle/chapters/json")
    suspend fun getWxarticle2(): WxArticleResponse

}
package com.hm.dumingwei.net

import com.hm.dumingwei.mvp.model.bean.Article
import com.hm.dumingwei.mvp.model.bean.WxArticleResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by dumingwei on 2020/4/24.
 *
 * Desc:
 */
interface ApiService {


    @GET("wxarticle/chapters/json")
    fun getWxarticle(): Call<WxArticleResponse>

    /**
     * retrofit2.6.0以下格式统一处理
     */
    @GET("article/list/1/json")
    fun getArticleLowLevelFormat1(): Call<NetResponse<Article>>

    @GET("wxarticle/chapters/json")
    fun getWxarticleListLowLevelFormat2(): Call<NetResponse<MutableList<WxArticleResponse.DataBean>>>

    @GET("wxarticle/chapters/json")
    suspend fun getWxarticle2(): WxArticleResponse

    @GET("wxarticle/chapters/json")
    suspend fun getWxarticle2Temp(
        @Query("params1") params1: String?,
        @Query("params2") params2: Int?,
        @Query("params3") params3: String?,
    ): WxArticleResponse

    /**
     * retrofit2.6.0以上格式统一处理
     */
    @GET("article/list/1/json")
    suspend fun getArticle(): NetResponse<Article>

    @GET("wxarticle/chapters/json")
    suspend fun getWxarticleList(): NetResponse<MutableList<WxArticleResponse.DataBean>>
}
package com.hm.dumingwei.kotlinandroid

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.hm.dumingwei.mvp.model.bean.GankResult
import com.hm.dumingwei.net.ApiSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by dumingwei on 2020/4/24
 *
 * Desc: 使用协程做网络请求
 */
class CoroutineNetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_net)


        //Retrofit 做网络请求
        ApiSource.instance.getAndroidGank().enqueue(object : Callback<GankResult> {
            override fun onFailure(call: Call<GankResult>, t: Throwable) {
            }

            override fun onResponse(call: Call<GankResult>, response: Response<GankResult>) {
            }
        })
    }
}

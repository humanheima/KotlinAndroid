package com.hm.dumingwei.net

import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 扩展Retrofit.Call类，为其扩展一个awaitResponse方法，并标识为挂起函数
 */
suspend fun <T : Any?> Call<T>.awaitResponse(): T {

    return suspendCancellableCoroutine {
        it.invokeOnCancellation {
            //取消网路请求
            cancel()

        }
        enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                it.resumeWithException(t)
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        it.resume(body)
                    } else {
                        it.resumeWithException(Throwable(response.toString()))
                    }

                } else {
                    it.resumeWithException(Throwable(response.toString()))
                }
            }
        })
    }
}
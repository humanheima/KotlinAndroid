package com.hm.dumingwei.net

import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun okhttp3.Call.awaitResponse(): okhttp3.Response {

    return suspendCancellableCoroutine {
        //当协程被取消的时候，取消网络请求
        it.invokeOnCancellation { throwable ->
            try {
                Log.d("awaitResponse", "invokeOnCancellation: ${throwable?.message}")
                cancel()
            } catch (e: Exception) {
                //Ignore cancel exception
            }
        }

        enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                //协程被取消，直接返回
//                if (it.isCancelled) {
//                    return
//                }
                it.resumeWithException(e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                //后台线程
                it.resume(response)
            }
        })
    }
}

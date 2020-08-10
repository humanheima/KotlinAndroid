package com.hm.dumingwei.net

import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun okhttp3.Call.awaitResponse(): okhttp3.Response {

    return suspendCancellableCoroutine {
        it.invokeOnCancellation {
            //当协程被取消的时候，取消网络请求
            cancel()
        }

        enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                it.resumeWithException(e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                //后台线程
                it.resume(response)
            }
        })
    }
}

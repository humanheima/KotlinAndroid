package com.hm.dumingwei.net

import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun okhttp3.Call.awaitResponse(): okhttp3.Response {

    return suspendCoroutine {
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

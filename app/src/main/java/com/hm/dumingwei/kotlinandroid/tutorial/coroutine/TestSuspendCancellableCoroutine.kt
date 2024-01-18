package com.hm.dumingwei.kotlinandroid.tutorial.coroutine

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * Created by p_dmweidu on 2024/1/18
 * Desc: 测试 suspendCancellableCoroutine
 */
class TestSuspendCancellableCoroutine {


    companion object {
        private const val TAG = "TestSuspendCancellableC"

    }

    /**
     * suspendCancellableCoroutine 的写法
     */
    suspend fun test(): String {
        if (Math.random() < 0.5) {
            return "正常返回 Hello, World!"
        }
        return suspendCancellableCoroutine { continuation ->

            // 当协程被取消时，取消子协程
//            continuation.invokeOnCancellation {
//                if (continuation.isCancelled) {
//                    try {
//                        continuation.cancel()
//                    } catch (ex: Exception) {
//                        // Ignore
//
//                        Log.i(TAG, "test: ex = $ex")
//                    }
//                }
//            }


//            Thread {
//                // 模拟一个长时间运行的操作
//                Thread.sleep(1000)
//                continuation.resume("耗时操作，返回Hello, World!")
//            }.start()

            // 在新的子协程中执行长时间运行的操作
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    delay(1000) // 延迟 1 秒
                    continuation.resume("延迟返回：Hello, World!")
                } catch (e: Exception) {

                    Log.i(TAG, "test: e = $e")
                    continuation.resumeWithException(e)
                }
            }
        }

    }
}
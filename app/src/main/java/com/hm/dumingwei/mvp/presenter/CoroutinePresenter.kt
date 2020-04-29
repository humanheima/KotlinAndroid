package com.hm.dumingwei.mvp.presenter

import android.util.Log
import com.hm.dumingwei.kotlinandroid.await
import com.hm.dumingwei.kotlinandroid.handbook.thirteen.Event
import com.hm.dumingwei.kotlinandroid.handbook.thirteen.RetrofitManager
import com.hm.dumingwei.mvp.view.CoroutineView
import kotlinx.coroutines.*
import retrofit2.Response
import kotlin.coroutines.CoroutineContext

/**
 * Created by dumingwei on 2020/4/24.
 *
 * Desc:
 * 参考链接：https://www.jianshu.com/p/2056d5424001
 */
class CoroutinePresenter(scope: CoroutineScope?) : BaseNetPresenter<CoroutineView>(scope) {

    private val TAG: String? = "CoroutinePresenter"

    private val coroutineAPIService = RetrofitManager.get()
            .coroutineAPIService()

    fun getPublicEvent1() {
        Log.d(TAG, "getPublicEvent1 start : current thread ${Thread.currentThread().name}")
        scope?.launch {
            try {
                //网络请求，并不会阻塞主线程
                val events: List<Event> = coroutineAPIService.publicEvent("humanheima").await()
                val events2: List<Event> = coroutineAPIService.publicEvent("humanheima").await()

                val builder = StringBuilder()
                for (event in events) {
                    builder.append("event1 ${event.actor?.url}\n")
                }
                for (event in events2) {
                    builder.append("event2 ${event.actor?.url}\n")
                }

                view?.let {
                    if (it.canUpdateUI()) {
                        it.setResult(builder.toString())
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "getPublicEvent1: ${e.message}")
            }
        }
        Log.d(TAG, "getPublicEvent1 end : current thread ${Thread.currentThread().name}")
    }

    fun getPublicEvent2() {
        /*val handler = CoroutineExceptionHandler { _, exception ->
            Log.d(TAG, "getPublicEvent2: Caught original $exception")
        }*/

        scope?.launch {
            try {

                /**
                 * 注意，使用async的时候一定要重新指定协程上下文
                 */
                withContext(Dispatchers.Main) {
                    val one: Deferred<List<Event>> = async {
                        Log.d(TAG, "getPublicEvent2: async1 ${Thread.currentThread().name}")
                        coroutineAPIService.publicEvent("humanheima").await()
                    }
                    val two = async {
                        Log.d(TAG, "getPublicEvent2: async2 ${Thread.currentThread().name}")
                        coroutineAPIService.publicEvent("humanheima").await()
                    }

                    val combine: MutableList<Event> = arrayListOf()

                    val oneResult: List<Event> = one.await()
                    val twoResult = two.await()

                    combine.addAll(oneResult)
                    combine.addAll(twoResult)
                    view?.let {
                        if (it.canUpdateUI()) {
                            it.setResult("getPublicEvent2: ${combine.size}")
                        }
                    }
                    Log.d(TAG, "getPublicEvent2: ${combine.size}")
                }

            } catch (e: Throwable) {
                Log.d(TAG, "getPublicEvent2: error  ${e.message}")
            }
        }
    }

    fun getPublicEvent3() {
        scope?.launch {
            try {
                Log.d(TAG, "getPublicEvent3: 开始执行挂起函数")

                val events: List<Event> = coroutineAPIService.getPublicEventSuspend("humanheima")
                Log.d(TAG, "getPublicEvent3: 挂起函数执行完毕后恢复协程，继续向下执行")

                view?.let {
                    if (it.canUpdateUI()) {
                        it.setResult("getPublicEvent3 success: ${events.size}")
                    }
                }

                Log.d(TAG, "getPublicEvent3 success: ${events.size}")
            } catch (e: Exception) {
                Log.d(TAG, "getPublicEvent3: ${e.message}")
            }
        }
    }

    fun getPublicEvent4() {
        scope?.launch {
            try {
                val response: Response<List<Event>> = coroutineAPIService.getPublicEvent("humanheima")
                Log.d(TAG, "getPublicEvent4 success: ${response.isSuccessful}")
                val events: List<Event>? = response.body()

                if (events != null) {
                    view?.let {
                        if (it.canUpdateUI()) {
                            it.setResult("getPublicEvent4 success: ${events.size}")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.d(TAG, "getPublicEvent4: ${e.message}")
            }
        }
    }

    override fun detachView() {
        super.detachView()
        scope?.cancel()
        scope = null
    }

}
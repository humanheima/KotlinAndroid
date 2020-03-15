package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.hm.dumingwei.kotlinandroid.handbook.thirteen.Event
import com.hm.dumingwei.kotlinandroid.handbook.thirteen.RetrofitManager
import kotlinx.android.synthetic.main.activity_coroutine.*
import kotlinx.coroutines.*
import retrofit2.Response
import kotlin.coroutines.CoroutineContext

/**
 * Crete by dumingwei on 2019/3/8
 * Desc: 协程关联生命周期
 * 我们通过创建一个和Activity生命周期关联的Job来管理协程的生命周期。我们在onCreate方法中创建Job对象，
 * 在onDestroy中调用Job的取消方法。
 */
class CoroutineActivity : AppCompatActivity(), CoroutineScope {

    private val TAG = "CoroutineActivity"

    private lateinit var job: Job

    companion object {

        @JvmStatic
        fun launch(context: Context) {
            val intent = Intent(context, CoroutineActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        job = Job()

        if(this::job.isInitialized){

        }
        getPublicEvent()
        //getPublicEvent1()
        //getPublicEvent2()
        //getPublicEvent3()
        Log.d(TAG, "onCreate: ")
        /*launch {
            Log.d(TAG, "onCreate: 开始执行挂起函数")

            //执行挂起函数
            val events: List<Event> = RetrofitManager.get()
                    .coroutineAPIService()
                    .getPublicEventSuspend("humanheima")

            Log.d(TAG, "onCreate: 挂起函数执行完毕后恢复协程，继续向下执行")

            Log.d(TAG, "onCreate success: ${events.size}")

        }

        Log.d(TAG, "onCreate: 线程继续向下执行")*/
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job


    // class Activity continues
    fun doSomething() {
        // launch ten coroutines for a demo, each working for a different time
        repeat(10) { i ->
            launch {
                delay((i + 1) * 200L) // variable delay 200ms, 400ms, ... etc
                println("Coroutine $i is done")
            }
        }
    }

    private fun getPublicEvent() {
        launch {
            Log.d(TAG, "getPublicEvent: current thread ${Thread.currentThread().name}")
            try {
                //网络请求，并不会阻塞主线程
                val events: List<Event> = RetrofitManager.get()
                        .coroutineAPIService()
                        .publicEvent("humanheima").await()

                val builder = StringBuilder()
                for (event in events) {
                    builder.append("${event.actor?.url}\n")
                }
                tvResult.text = builder.toString()
            } catch (e: Exception) {
                Log.d(TAG, "getPublicEvent: ${e.message}")
            }
        }
    }

    private fun getPublicEvent1() {

        launch {
            val one: Deferred<List<Event>> = async {
                RetrofitManager.get()
                        .coroutineAPIService()
                        .publicEvent("humanheima").await()
            }
            val two = async {
                RetrofitManager.get()
                        .coroutineAPIService()
                        .publicEvent("humanheima").await()
            }
            val combine: MutableList<Event> = arrayListOf()

            val oneResult: List<Event> = one.await()
            val twoResult = two.await()

            combine.addAll(oneResult)
            combine.addAll(twoResult)

            Log.d(TAG, "getPublicEvent1: ${combine.size}")

        }
    }

    private fun getPublicEvent2() {
        launch {
            Log.d(TAG, "getPublicEvent2: 开始执行挂起函数")

            val events: List<Event> = RetrofitManager.get()
                    .coroutineAPIService()
                    .getPublicEventSuspend("humanheima")
            Log.d(TAG, "getPublicEvent2: 挂起函数执行完毕后恢复协程，继续向下执行")
            Log.d(TAG, "getPublicEvent2 success: ${events.size}")
        }
    }

    private fun getPublicEvent3() {
        launch {
            val response: Response<List<Event>> = RetrofitManager.get()
                    .coroutineAPIService()
                    .getPublicEvent("humanheima")
            Log.d(TAG, "getPublicEvent3 success: ${response.isSuccessful}")
            var events = response.body()
            if (events != null) {
                Log.d(TAG, "getPublicEvent3 success: ${events.size}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}

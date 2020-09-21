package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_async_exception_test.*
import kotlinx.coroutines.*

/**
 * Created by dumingwei on 2020/9/16
 *
 * Desc: 测试 asyn 启动的协程无法被捕获的原因
 *
 * stack overflow上提问的问题
 *
 * https://stackoverflow.com/questions/63930492/why-i-cant-use-try-catch-to-catch-exception-in-kotlin-coroutine
 */
class AsyncExceptionTestActivity : AppCompatActivity() {

    private val TAG: String = "AsyncExceptionTestActiv"


    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, AsyncExceptionTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_async_exception_test)

        btnTest1.setOnClickListener {
            test()
        }

        btnTest2.setOnClickListener {
            test2()
        }

        btnTest3.setOnClickListener {
            test3()
        }
        btnTest4.setOnClickListener {
            test4()
        }
        btnTest5.setOnClickListener {
            test5()
        }
    }

    private val scope = MainScope()

    private fun test() {
        scope.launch {
            Log.d(TAG, "test: launch${coroutineContext}")
            try {
                val response: Deferred<String> = async {
                    Log.d(TAG, "test: in async block")
                    throw IllegalStateException("an IllegalStateException")
                }
                response.await()
            } catch (e: Exception) {
                Log.d(TAG, "test: error ${e.message}")
            }
        }
    }

    /**
     * 异常被CoroutineExceptionHandler捕获
     */
    private fun test2() {
        val expHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.d(TAG, "test1: expHandler caught exception : ${throwable.message}")
        }
        val expHandler1 = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.d(TAG, "test1: expHandler1 caught exception : ${throwable.message}")
        }
        scope.launch(expHandler) {
            Log.d(TAG, "test1: launch")
            try {
                val response: Deferred<String> = async(expHandler1) {
                    Log.d(TAG, "test1: in async block")
                    throw IllegalStateException("在async中抛出异常")
                }
                response.await()

            } catch (e: Exception) {
                Log.d(TAG, "test1: error ${e.message}")
            }
        }
    }

    private fun test3() {
        try {
            scope.launch {
                Log.d(TAG, "test1: launch")
                val response: Deferred<String> = async {
                    Log.d(TAG, "test1: in async block")
                    throw IllegalStateException("在async中抛出异常")
                }
                response.await()
            }
        } catch (e: Exception) {
            Log.d(TAG, "test1: error ${e.message}")
        }
    }

    private fun test4() {
        try {
            scope.launch {
                Log.d(TAG, "test1: launch")
                val response: Deferred<String> = async {
                    Log.d(TAG, "test1: in async block")
                    throw IllegalStateException("在async中抛出异常")
                }
                response.await()
            }
        } catch (e: Exception) {
            Log.d(TAG, "test1: error ${e.message}")
        }
    }

    private fun test5() {
        scope.launch {
            Log.d(TAG, "test: launch")
            try {
                val response: Deferred<String> = GlobalScope.async {
                    Log.d(TAG, "test: in async block")
                    throw IllegalStateException("an IllegalStateException")
                }
                response.await()
            } catch (e: Exception) {
                Log.d(TAG, "test: error ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

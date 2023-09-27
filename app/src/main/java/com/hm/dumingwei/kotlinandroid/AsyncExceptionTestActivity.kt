package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
 *
 * 当 async 被用作根协程 (CoroutineScope 实例或 supervisorScope 的直接子协程) 时不会自动抛出异常，而是在您调用 .await() 时才会抛出异常。
 *
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

        btnTestAsync1.setOnClickListener {
            asyncAsRootCoroutine1()
        }

        btnTest2.setOnClickListener {
            //testExpHandler()
            test2()
        }

        btnTest3.setOnClickListener {
            testExpHandler()
        }
        btnTest4.setOnClickListener {
            test4()
        }
        btnTest5.setOnClickListener {
            test5()
        }

    }

    val uiScope = CoroutineScope(SupervisorJob())


    private val scope = MainScope()

    /**
     * async 不是用作根协程，所以其中 async代码块中的异常捕获不住
     */
    private fun test() {
        scope.launch {
            Log.d(TAG, "test: launch${coroutineContext}")
            try {
                val response: Deferred<String> = async {
                    Log.d(TAG, "test: in async block")
                    throw IllegalStateException("an IllegalStateException")
                }
                //response.await()
            } catch (e: Exception) {
                // async 中抛出的异常将不会在这里被捕获
                // 但是异常会被传播和传递到 scope
                Log.d(TAG, "test: error ${e.message}")
            }
        }
    }

    /**
     * async 用作根协程，所以其中 async代码块中的异常可以捕获住
     */
    private fun asyncAsRootCoroutine1() {
        scope.async {
            try {
                Log.d(TAG, "asyncAsRootCoroutine1: in async block")
                throw IllegalStateException("an IllegalStateException")
                //response.await()
            } catch (e: Exception) {
                Log.d(TAG, "asyncAsRootCoroutine1: error ${e.message}")
            }
        }
    }

    private fun test2() {
        val scope = CoroutineScope(Job())
        //注释1处
        //scope.launch {
        scope.async {
            val response: Deferred<String> = async {
                Log.d(TAG, "test2: in async block")
                //注释1处，使用launch的时候会传播这个异常，造成crash
                //注释1处，使用async的时候不会传播这个异常，不会crash
                throw IllegalStateException("在async中抛出异常")
            }
            try {
                response.await()
            } catch (e: Exception) {
                Log.d(TAG, "test2: caught error ${e.message}")
            }
        }
    }

    /**
     * 异常被CoroutineExceptionHandler捕获
     */
    private fun testExpHandler() {
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

    /**
     * async 被用作根协程（supervisorScope 的直接子协程）时不会自动抛出异常，而是在您调用 .await() 时才会抛出异常。
     */
    private fun test4() {
        val scope = CoroutineScope(SupervisorJob())
        scope.launch {
            //supervisorScope 的直接子协程
            supervisorScope {
                val deferred: Deferred<String> = async {
                    throw IllegalStateException("an IllegalStateException")
                }

                try {
                    deferred.await()
                } catch (e: Exception) {
                    // 处理 async 中抛出的异常
                    Log.d(TAG, "test4: caught error ${e.message}")
                }
            }
        }
    }

    /**
     * 当 async 被用作根协程 (CoroutineScope的直接子协程) 时不会自动抛出异常，而是在您调用 .await() 时才会抛出异常。
     */
    private fun test5() {
        val scope = CoroutineScope(Job())
        scope.launch {
            val childScope = CoroutineScope(Job())
            //childScope的直接子协程
            val deferred: Deferred<String> = childScope.async {
                throw IllegalStateException("an IllegalStateException")
            }
            try {
                deferred.await()
            } catch (e: Exception) {
                //async 中抛出的异常将不会在这里被捕获
                Log.d(TAG, "test4: caught error ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

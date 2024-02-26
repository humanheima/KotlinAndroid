package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hm.dumingwei.kotlinandroid.databinding.ActivityAsyncExceptionTestBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

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

    private lateinit var binding: ActivityAsyncExceptionTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAsyncExceptionTestBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnTestOthers.setOnClickListener {
        }
        binding.btnTest1.setOnClickListener {
            test()
        }

        binding.btnTestAsync1.setOnClickListener {
            asyncAsRootCoroutine1()
        }

        binding.btnTest2.setOnClickListener {
            //testExpHandler()
            test2()
        }

        binding.btnTest3.setOnClickListener {
            testExpHandler()
        }
        binding.btnTest4.setOnClickListener {
            test4()
        }
        binding.btnTest5.setOnClickListener {
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
                response.await()
            } catch (e: Exception) {
                // async 中抛出的异常将不会在这里被捕获
                // 但是异常会被传播和传递到 scope，这里能够打印出来
                Log.d(TAG, "catch test: error ${e.message}")
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
                val response: Deferred<String> = async {
                    Log.d(TAG, "test: in async block")
                    throw IllegalStateException("an IllegalStateException")
                }
                response.await()
            } catch (e: Exception) {
                Log.d(TAG, "catch asyncAsRootCoroutine1: error ${e.message} ")
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
                //这里的异常会被expHandler捕获，expHandler1不会捕获
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
     * async 被用作supervisorScope 的直接子协程时不会自动抛出异常，而是在您调用 .await() 时才会抛出异常。
     */
    private fun test4() {

        GlobalScope.launch(Dispatchers.Main) {
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
                Log.d(TAG, "test5: caught error ${e.message}")
            }
        }
    }

    /**
     * supervisorScope 内部一个子协程失败，不影响另外一个子协程
     */
    private fun test6() {
        GlobalScope.launch(Dispatchers.Main) {

            val stringBuilder = StringBuilder()

            var string1: String? = null
            var string2: String? = null

            //supervisorScope 的直接子协程
            // supervisorScope {

            // }

            try {
                supervisorScope {

                    throw IllegalStateException("test4: an IllegalStateException")
                    val deferred1: Deferred<String> = async {
                        //throw IllegalStateException("第1个请求，an IllegalStateException")
                        "第一个请求结果成功"
                    }

                    try {
                        string1 = deferred1.await()
                    } catch (e: Exception) {
                        // 处理 async 中抛出的异常
                        Log.d(TAG, "test4: caught error ${e.message}")
                    }

                    val deferred2: Deferred<String> = async {
                        throw IllegalStateException("第2个请求，an IllegalStateException")
                        "第二个请求结果成功"
                    }

                    try {
                        string2 = deferred2.await()
                    } catch (e: Exception) {
                        // 处理 async 中抛出的异常
                        Log.d(TAG, "test4: caught error ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "test4: caught supervisorScope error ${e.message}")
            }

            Log.i(TAG, "test4: string1 = $string1")
            Log.i(TAG, "test4: string2 = $string2")
            if (string1 == null) {
                Log.i(TAG, "test4: 第一个请求失败，string1 is null，直接return了")
                return@launch
            }

            stringBuilder.append("string1 = $string1").append(";")
            stringBuilder.append("string2 = $string2")
            Log.i(TAG, "test4: stringBuilder = ${stringBuilder.toString()}")

            //Note: 这种写法也可以
//            val deferred: Deferred<String> = async(SupervisorJob()) {
//                throw IllegalStateException("an IllegalStateException")
//            }
//
//            try {
//                deferred.await()
//            } catch (e: Exception) {
//                // 处理 async 中抛出的异常
//                Log.d(TAG, "test4: caught error ${e.message}")
//            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

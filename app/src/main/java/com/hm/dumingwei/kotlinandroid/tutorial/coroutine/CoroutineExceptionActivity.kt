package com.hm.dumingwei.kotlinandroid.tutorial.coroutine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.hm.dumingwei.kotlinandroid.R
import kotlinx.android.synthetic.main.activity_coroutine_exception.*
import kotlinx.coroutines.*
import java.io.Closeable
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Created by dumingwei on 2020/4/26
 *
 * Desc: 关于Kotlin的异常处理
 */
class CoroutineExceptionActivity : AppCompatActivity() {

    private val TAG: String? = "CoroutineExceptionActiv"

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, CoroutineExceptionActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_exception)

        btnTest1.setOnClickListener {
            test1()
        }
        btnTest2.setOnClickListener {
            test2()
        }
        btnTest3.setOnClickListener {
            test3()
        }
        btnTest4.setOnClickListener { test4() }
    }

    private fun test1() {
        GlobalScope.launch {

            throw RuntimeException("this is an exception")
            "doSomething..."
        }

        Thread.sleep(5000)

    }

    /**
     * 异常catch不住
     */
    private fun test2() {
        try {
            GlobalScope.launch {
                doSomething().await()
            }
        } catch (e: Exception) {
            Log.d(TAG, "test2: ${e.message}")
        }

    }

    val job: Job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)

    /**
     * 异常catch不住
     */
    private fun test3() {
        try {
            scope.launch {
                doSomething().await()
            }
        } catch (e: Exception) {
            Log.d(TAG, "test3: ${e.message}")
        }
    }

    private val errorHandle = object : CoroutineErrorCallback {
        override fun onError(throwable: Throwable) {
            Log.d(TAG, "onError: ${throwable.localizedMessage}")
        }
    }

    /**
     * 异常可以catch住
     */
    private fun test4() {
        uiScope(errorHandle).launch {
            doSomething().await()
        }
    }

    private fun doSomething(): Deferred<String> = GlobalScope.async {
        throw RuntimeException("this is an exception")
        "doSomething..."
    }

}

interface CoroutineErrorCallback {

    fun onError(throwable: Throwable)
}

fun uiScope(errorCallback: CoroutineErrorCallback? = null) = SafeCoroutineScope(Dispatchers.Main, errorCallback)

class SafeCoroutineScope(context: CoroutineContext, errorCallback: CoroutineErrorCallback? = null) : CoroutineScope, Closeable {

    override val coroutineContext: CoroutineContext = SupervisorJob() + context + UncaughtCoroutineExceptionHandler(errorCallback)

    override fun close() {
        coroutineContext.cancelChildren()
    }
}

class UncaughtCoroutineExceptionHandler(val errorCallback: CoroutineErrorCallback? = null) :
        CoroutineExceptionHandler, AbstractCoroutineContextElement(CoroutineExceptionHandler.Key) {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        exception.printStackTrace()
        errorCallback?.onError(exception)
    }
}
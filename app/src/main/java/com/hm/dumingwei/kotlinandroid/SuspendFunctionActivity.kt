package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_suspend_function.*
import kotlinx.coroutines.*

/**
 * Created by dumingwei on 2020/9/30
 *
 * Desc: 挂起函数
 */
class SuspendFunctionActivity : AppCompatActivity() {

    private val TAG: String = "SuspendFunctionActivity"

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, SuspendFunctionActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suspend_function)

        btnInvoke.setOnClickListener {
            invokeMethod()
        }
    }

    private fun invokeMethod() {
        //默认协程是在主线程运行的，因为我们的scope指定的调度器是Dispatchers.Main
        scope.launch(Dispatchers.Main) {
            val result = getString()
            etText.setText(result)
        }
    }

    private suspend fun getString(): String {
        Log.i(TAG, "getString: current thread " + Thread.currentThread().name)
        //切换到IO线程
        return withContext(Dispatchers.IO) {
            Log.i(TAG, "getString: current thread " + Thread.currentThread().name)
            "empty string"
        }
    }

    override fun onDestroy() {
        //取消所有的协程
        scope.cancel()
        super.onDestroy()

    }
}

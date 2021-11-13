package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_suspend_function.*
import kotlinx.coroutines.*

/**
 * Created by dumingwei on 2020/9/30
 *
 * Desc: 挂起函数
 */
class SuspendFunctionActivity : AppCompatActivity() {

    private val TAG: String = "SuspendFunctionActivity"

    private lateinit var tvText1: TextView
    private lateinit var tvText2: TextView

    private lateinit var btnSuspend: Button
    private lateinit var btnUnSuspend: Button

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

        tvText1 = findViewById(R.id.tvText1)
        tvText2 = findViewById(R.id.tvText2)

        btnSuspend = findViewById(R.id.btnSuspend)
        btnUnSuspend = findViewById(R.id.btnUnSuspend)

        btnSuspend.setOnClickListener {
            tvText1.text = getStringSuspend()
            tvText2.text = "兴百姓苦，亡百姓苦"

        }

        btnUnSuspend.setOnClickListener {

            scope.launch {
                tvText1.text = getStringUnSuspend()
            }
            tvText2.text = "兴百姓苦，亡百姓苦"

        }
        btnInvoke.setOnClickListener {
            invokeMethod()
        }
    }

    /**
     * 模拟耗时操作，2秒后返回字符串
     */
    private fun getStringSuspend(): String {
        Thread.sleep(2000)
        return "峰峦如聚，波涛如怒，山河表里潼关路，望西都意踌躇，伤心秦汉经行处，宫阙万间都做了土。"
    }

    /**
     * 模拟耗时操作，2秒后返回字符串。正确的的非阻塞，不阻塞当前线程，阻塞后台线程。
     */
    private suspend fun getStringUnSuspend(): String {
        delay(2000)
        return "峰峦如聚，波涛如怒，山河表里潼关路，望西都意踌躇，伤心秦汉经行处，宫阙万间都做了土。"

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

package com.hm.dumingwei.kotlinandroid.tutorial.coroutine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.hm.dumingwei.kotlinandroid.R
import kotlinx.coroutines.*

/**
 * Crete by dumingwei on 2020-01-05
 * Desc:
 *
 */
class CoroutineBaseActivity : AppCompatActivity() {

    private val TAG = "CoroutineBaseActivity"

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, CoroutineBaseActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_base)
        //test0()
    }

    public fun onClick(view: View) {
        when (view.id) {
            R.id.btnTest1 -> {
                test1()
            }
            R.id.btnTest2 -> {
                test2()
            }
        }
    }

    private fun test0() {
        GlobalScope.launch(block = {
            delay(1000L)
            Log.d(TAG, "test0: Hello,World!")
        })
    }

    /**
     * 通过指定CoroutineContext为Dispatchers.IO在线程池里执行耗时代码
     */
    private fun test1() {
        Log.d(TAG, "test1: start ${Thread.currentThread().name}")
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000L)
            Log.d(TAG, "test1: Hello,World! ${Thread.currentThread().name}")

        }
        Log.d(TAG, "test1: end ${Thread.currentThread().name}")
    }

    private fun test2() {
        GlobalScope.launch(Dispatchers.Main) {
            Log.d(TAG, "Hello ${Thread.currentThread().name}")
            test()
            Log.d(TAG, "End ${Thread.currentThread().name}")

        }

    }

    private suspend fun test() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "World ${Thread.currentThread().name}")
        }
    }

    /**
     * coroutineScope 和 supervisorScope的区别
     */
    private suspend fun testScope() {
        coroutineScope {

        }

        supervisorScope {

        }
    }

    suspend fun loadLots(){
        coroutineScope {
            repeat(1_000){
                launch {
                    //Log.d(TAG, "loadLots: ")
                    //todo network
                    //fetchDocs()
                }
            }
        }

    }
}

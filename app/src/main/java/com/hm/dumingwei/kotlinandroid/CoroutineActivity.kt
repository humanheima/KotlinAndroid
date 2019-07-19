package com.hm.dumingwei.kotlinandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Crete by dumingwei on 2019/3/8
 * Desc: 协程关联生命周期
 * 我们通过创建一个和Activity生命周期关联的Job来管理协程的生命周期。我们在onCreate方法中创建Job对象，
 * 在onDestroy中调用Job的取消方法。
 */
class CoroutineActivity : AppCompatActivity(), CoroutineScope {

    lateinit var job: Job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        job = Job()
    }

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

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

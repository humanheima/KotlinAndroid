package com.hm.dumingwei.kotlinandroid.lazy_init

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.hm.dumingwei.kotlinandroid.R

/**
 * Crete by dumingwei on 2020-03-14
 * Desc: 测试 by lazy
 *
 */
class LazyInitActivity : AppCompatActivity() {


    private val messageView: TextView by lazy {
        findViewById<TextView>(R.id.tvByLazy)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lazy_init)
    }

    fun onSayHello() {
        // Initialization would be run at here!!
        messageView.text = "Hello"

        val error=OutOfMemoryError()
    }

}

package com.hm.dumingwei.kotlinandroid.testbase

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.hm.dumingwei.kotlinandroid.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by dumingwei on 2019/4/15.
 * Desc:
 * 测试 子类中的控件是否可以直接在父类中使用
 */
abstract class BaseWidgetActivity<T : ViewBinding> : AppCompatActivity() {

    private lateinit var binding: T
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = createBinding()
        setContentView(binding.root)
        init()
        main()
    }

    abstract fun createBinding(): T

    private fun init() {
        binding.root.findViewById<TextView>(R.id.tvWidget).setOnClickListener {
            if (this@BaseWidgetActivity is FirstWidgetActivity) {
                Toast.makeText(this, "FirstWidgetActivity", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "SecondWidgetActivity", Toast.LENGTH_SHORT).show()
            }
        }

        val clickListener = View.OnClickListener {

        }

        val clicklistener1 = object : View.OnClickListener {
            override fun onClick(v: View?) {
            }
        }
    }

    fun main() {
        GlobalScope.launch {
            println("Hello world! GlobalScope current Thread ${Thread.currentThread().name}")
        }
        println("Hello world! current Thread  ${Thread.currentThread().name}")
        Thread.sleep(1000)
    }


}

package com.hm.dumingwei.kotlinandroid.testbase

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_first_widget.*

/**
 * Created by dumingwei on 2019/4/15.
 * Desc:
 * 测试 子类中的控件是否可以直接在父类中使用
 */
abstract class BaseWidgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        init()
    }

    abstract fun getLayoutId(): Int

    private fun init() {
        tvWidget.setOnClickListener {
            if (this@BaseWidgetActivity is FirstWidgetActivity) {
                Toast.makeText(this, "FirstWidgetActivity", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "SecondWidgetActivity", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.hm.dumingwei.kotlinandroid.testbase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hm.dumingwei.kotlinandroid.R

class SecondWidgetActivity : BaseWidgetActivity() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, SecondWidgetActivity::class.java)
            context.startActivity(intent)
        }
    }
    override fun getLayoutId(): Int {
        return R.layout.activity_second_widget
    }
}

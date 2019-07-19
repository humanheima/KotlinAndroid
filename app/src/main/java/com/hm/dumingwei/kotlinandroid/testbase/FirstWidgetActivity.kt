package com.hm.dumingwei.kotlinandroid.testbase

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.hm.dumingwei.kotlinandroid.R

class FirstWidgetActivity : BaseWidgetActivity() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, FirstWidgetActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_first_widget

}

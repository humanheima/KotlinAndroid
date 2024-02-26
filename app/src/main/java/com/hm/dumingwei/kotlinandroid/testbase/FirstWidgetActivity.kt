package com.hm.dumingwei.kotlinandroid.testbase

import android.content.Context
import android.content.Intent
import com.hm.dumingwei.kotlinandroid.databinding.ActivityFirstWidgetBinding

class FirstWidgetActivity : BaseWidgetActivity<ActivityFirstWidgetBinding>() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, FirstWidgetActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun createBinding(): ActivityFirstWidgetBinding {
        return ActivityFirstWidgetBinding.inflate(layoutInflater)
    }

}

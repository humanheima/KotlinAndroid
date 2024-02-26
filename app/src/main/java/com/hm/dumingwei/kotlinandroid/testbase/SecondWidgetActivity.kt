package com.hm.dumingwei.kotlinandroid.testbase

import android.content.Context
import android.content.Intent
import com.hm.dumingwei.kotlinandroid.databinding.ActivitySecondWidgetBinding

class SecondWidgetActivity : BaseWidgetActivity<ActivitySecondWidgetBinding>() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, SecondWidgetActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun createBinding(): ActivitySecondWidgetBinding {
        return ActivitySecondWidgetBinding.inflate(layoutInflater)
    }

}

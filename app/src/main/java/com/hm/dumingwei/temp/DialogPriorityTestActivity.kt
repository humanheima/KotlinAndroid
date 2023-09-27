package com.hm.dumingwei.temp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.hm.dumingwei.DialogPriorityManager
import com.hm.dumingwei.Priority
import com.hm.dumingwei.kotlinandroid.R

/**
 * Created by dumingwei on 2021/11/25
 *
 * Desc: 测试弹窗优先级
 */
class DialogPriorityTestActivity : AppCompatActivity() {

    private val TAG: String = "DialogPriorityTestActiv"


    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, DialogPriorityTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog_priority_test)

        Log.i(TAG, "onCreate: ${DialogPriorityManager.hasHigherShowing(Priority.NO_LOGIN_4000_COIN_DIALOG)}")
        Log.i(TAG, "onCreate: ${DialogPriorityManager.sparseArray.size()}")
        Log.i(TAG, "onCreate: ${DialogPriorityManager.sparseArray}")
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.bnt_test1 -> {
                DialogPriorityManager.setShowing(Priority.NOVEL_TOP_GUIDE_DIALOG, true)
                Log.i(TAG, "onCreate: ${DialogPriorityManager.hasHigherShowing(Priority.NO_LOGIN_4000_COIN_DIALOG)}")
            }
            R.id.bnt_test2 -> {
                DialogPriorityManager.setShowing(Priority.NOVEL_TOP_GUIDE_DIALOG, false)
                Log.i(TAG, "onCreate: ${DialogPriorityManager.hasHigherShowing(Priority.NO_LOGIN_4000_COIN_DIALOG)}")
            }
            R.id.bnt_test3 -> {
                DialogPriorityManager.setShowing(Priority.NOVEL_GUIDE_DIALOG, true)
                DialogPriorityManager.setShowing(Priority.NOVEL_TOP_GUIDE_DIALOG, true)

                Log.i(TAG, "onCreate: ${DialogPriorityManager.hasHigherShowing(Priority.NO_LOGIN_4000_COIN_DIALOG)}")
            }
            R.id.bnt_test4 -> {
                DialogPriorityManager.setShowing(Priority.NOVEL_GUIDE_DIALOG, false)
                DialogPriorityManager.setShowing(Priority.NOVEL_TOP_GUIDE_DIALOG, false)
                Log.i(TAG, "onCreate: ${DialogPriorityManager.hasHigherShowing(Priority.NO_LOGIN_4000_COIN_DIALOG)}")
            }
        }
    }


}
package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.hm.dumingwei.LoadingDialog


/**
 * Created by p_dmweidu on 2022/9/16
 * Desc:
 */
class TestFuncActivity : AppCompatActivity() {


    private var loadingDialog: LoadingDialog? = null

    val handler: Handler = object : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }

    val showDialogRunnable: Runnable = Runnable {
        Log.i(TAG, "在runnable里面展示弹窗")
        showLoading()
    }

//    val showDialogRunnable = Runnable {
//        Log.i(TAG, "在runnable里面展示弹窗")
//        showLoading()
//    }


    companion object {


        private const val TAG = "TestFuncActivity"

        fun launch(context: Context) {
            val starter = Intent(context, TestFuncActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_func)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btn_send_msg -> {
                Log.i(TAG, "onClick: 发送延迟消息")
                handler.postDelayed(showDialogRunnable, 5000)
            }
            R.id.btn_remove_msg -> {
                Log.i(TAG, "onClick: 移除消息")
                handler.removeCallbacks(showDialogRunnable)
            }
        }
    }

    fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this)
        }
        if (loadingDialog?.isShowing == false) {
            loadingDialog?.show()
        }
    }

    fun hideLoading() {
        if (loadingDialog != null && loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
    }


}
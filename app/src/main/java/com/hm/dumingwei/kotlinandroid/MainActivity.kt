package com.hm.dumingwei.kotlinandroid

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hm.dumingwei.kotlinandroid.findviewbyid.FindViewByIdActivity
import com.hm.dumingwei.kotlinandroid.handbook.eleven.SecondActivity
import com.hm.dumingwei.kotlinandroid.handbook.thirteen.GithubEventActivity
import com.hm.dumingwei.kotlinandroid.testbase.FirstWidgetActivity
import com.hm.dumingwei.kotlinandroid.tutorial.coroutine.CoroutineBaseActivity
import com.hm.dumingwei.kotlinandroid.tutorial.coroutine.CoroutineExceptionActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRetrofitAndCoroutine.setOnClickListener {
            CoroutineRetrofitNetActivity.launch(this)
        }
        btnOkHttpVsCoroutine.setOnClickListener {
            CoroutineOkHttpNetActivity.launch(this)
        }
        btnAsyncException.setOnClickListener {
            AsyncExceptionTestActivity.launch(this)
        }
        btnException.setOnClickListener {
            CoroutineExceptionActivity.launch(this)
        }

        btnRetrofit.withTrigger().click {
            GithubEventActivity.launch(this)
        }
        btnPreventClick.withTrigger().click {
            //toastSHortly("prevent repeat click")
            SecondActivity.launch(this, "dumingwei", 27)
        }

        btnTestWidget.setOnClickListener {
            FirstWidgetActivity.launch(this)
        }

        /*btnCoroutineRequest.setOnClickListener {
            CoroutineActivity.launch(this)
        }*/

        btnBaseCoroutine.onClick {
            CoroutineBaseActivity.launch(this)
        }
        btnCoroutineRequest.onClick {
            CoroutineActivity.launch(this)
        }

        btnFindViewById.setOnClickListener {
            FindViewByIdActivity.launch(this)
        }
        btnExpTest.setOnClickListener {
            ExceptionTestActivity.launch(this)
        }

        btnSuspendFunction.setOnClickListener {
            SuspendFunctionActivity.launch(this)
        }
    }

    fun support(apiVersion: Int, block: () -> Unit) {

        if (versionOrHigher(apiVersion)) {

            block()
        }
    }

    fun <T> support(apiVersion: Int, function: () -> T, default: () -> T): T =
            if (versionOrHigher(apiVersion)) function() else default()

    private fun versionOrHigher(version: Int) = Build.VERSION.SDK_INT >= version
}
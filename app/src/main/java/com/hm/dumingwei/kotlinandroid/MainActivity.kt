package com.hm.dumingwei.kotlinandroid

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hm.dumingwei.kotlinandroid.bytest.PropertiesByActivity
import com.hm.dumingwei.kotlinandroid.databinding.ActivityMainBinding
import com.hm.dumingwei.kotlinandroid.findviewbyid.FindViewByIdActivity
import com.hm.dumingwei.kotlinandroid.handbook.eleven.SecondActivity
import com.hm.dumingwei.kotlinandroid.handbook.thirteen.GithubEventActivity
import com.hm.dumingwei.kotlinandroid.testbase.FirstWidgetActivity
import com.hm.dumingwei.kotlinandroid.tutorial.coroutine.CoroutineBaseActivity
import com.hm.dumingwei.kotlinandroid.tutorial.coroutine.CoroutineExceptionActivity
import com.hm.dumingwei.temp.DialogPriorityTestActivity

class MainActivity : AppCompatActivity() {

    private val TAG: String = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private fun runExtension(receiver: String, block: String.(number: Int, number1: Int) -> Unit) {
        receiver.block(2, 3)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val myExtension: String.(number: Int, number1: Int) -> Unit = { number, number1 ->
            Log.i(TAG, "onCreate: $this $number $number1")
            //println(number)
            //println(number1)
        }
        runExtension("Hello, World!", myExtension)

        binding.btnTestFlow.setOnClickListener {
            FlowTestActivity.launch(this)
        }
        binding.btnBaseCoroutine.withTrigger().onClick {
            CoroutineBaseActivity.launch(this)
        }

        binding.btnTestRemoveMsg.setOnClickListener {
            Log.i(TAG, "onCreate: " + (null as String?))

            //TestFuncActivity.launch(this)
        }
        binding.btnTestDialogPriority.setOnClickListener {
            DialogPriorityTestActivity.launch(this)
        }

        binding.btnRetrofitAndCoroutine.setOnClickListener {
            CoroutineRetrofitNetActivity.launch(this)
        }

        binding.btnTestJson.setOnClickListener {
            GsonTestActivity.launch(this)
        }

        binding.btnOkHttpVsCoroutine.setOnClickListener {
            CoroutineOkHttpNetActivity.launch(this)
        }
        binding.btnAsyncException.setOnClickListener {
            AsyncExceptionTestActivity.launch(this)
        }
        binding.btnException.setOnClickListener {
            CoroutineExceptionActivity.launch(this)
        }

        binding.btnRetrofit.withTrigger().click {
            GithubEventActivity.launch(this)
        }
        binding.btnPreventClick.withTrigger().click {
            //toastSHortly("prevent repeat click")
            SecondActivity.launch(this, "dumingwei", 27)
        }

        binding.btnTestWidget.setOnClickListener {
            FirstWidgetActivity.launch(this)
        }

        /*btnCoroutineRequest.setOnClickListener {
            CoroutineActivity.launch(this)
        }*/

        binding.btnCoroutineRequest.onClick {
            CoroutineActivity.launch(this)
        }

        binding.btnFindViewById.setOnClickListener {
            FindViewByIdActivity.launch(this)
        }
        binding.btnExpTest.setOnClickListener {
            ExceptionTestActivity.launch(this)
        }

        binding.btnSuspendFunction.setOnClickListener {
            SuspendFunctionActivity.launch(this)
        }


        testReturn()
        Log.d(TAG, "onCreate: 分割线")
        testReturn2()
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btnTestPropertiesDelegate -> {
                PropertiesByActivity.launch(this)
            }
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


    private fun testReturn() {
        val list = listOf<String?>(null, "1", "22222")

        val success: Boolean = list.any { item ->
            Log.d(TAG, "testReturn: item = $item")
            if (item == null) {
                return@any false
            } else {
                item.length > 2
            }
        }

        Log.d(TAG, "testReturn: success = $success")

    }


    private fun testReturn2() {

        val list = listOf<String?>(null, "1", "22222")

        val success: Boolean = list.any { item ->
            Log.d(TAG, "testReturn2: item = $item")
            if (item == null) {
                return@any false
            } else {
                item.length > 2

            }
        }

        Log.d(TAG, "testReturn2: success = $success")

    }


}
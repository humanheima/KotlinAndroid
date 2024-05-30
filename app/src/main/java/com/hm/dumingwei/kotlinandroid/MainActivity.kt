package com.hm.dumingwei.kotlinandroid

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hm.dumingwei.JsonUtilKt
import com.hm.dumingwei.kotlinandroid.bytest.PropertiesByActivity
import com.hm.dumingwei.kotlinandroid.databinding.ActivityMainBinding
import com.hm.dumingwei.kotlinandroid.findviewbyid.FindViewByIdActivity
import com.hm.dumingwei.kotlinandroid.handbook.eleven.SecondActivity
import com.hm.dumingwei.kotlinandroid.handbook.thirteen.GithubEventActivity
import com.hm.dumingwei.kotlinandroid.testbase.FirstWidgetActivity
import com.hm.dumingwei.kotlinandroid.tutorial.coroutine.CoroutineBaseActivity
import com.hm.dumingwei.kotlinandroid.tutorial.coroutine.CoroutineExceptionActivity
import com.hm.dumingwei.temp.DialogPriorityTestActivity
import com.hm.dumingwei.temp.FrequencyBean

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

        test1()
        test2()
        test3()

        Log.e(TAG, "onCreate: 分割线")
        test4()
        test5()
        test6()



        val myExtension: String.(number: Int, number1: Int) -> Unit = { number, number1 ->
            Log.i(TAG, "onCreate: $this $number $number1")
            //println(number)
            //println(number1)
        }
        runExtension("Hello, World!", myExtension)

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
    }

    private fun test1() {
        val frequencyBean = JsonUtilKt.instance.toObject(
            "{\"chatFrequencyMillisecond\": 1000,\"chatFrequencyTips\": \"too fast\"}",
            FrequencyBean::class.java
        )

        //chatFrequencyMillisecond 字段正常有值，onCreate: frequencyBean = FrequencyBean(chatFrequencyMillisecond=1000, chatFrequencyTips=too fast)
        Log.d(TAG, "onCreate: frequencyBean = $frequencyBean")
    }

    private fun test2() {

        val frequencyBean = JsonUtilKt.instance.toObject(
            "{\"chatFrequencyMillisecond\": null,\"chatFrequencyTips\": \"too fast\"}",
            FrequencyBean::class.java
        )

        //chatFrequencyMillisecond 字段为 null onCreate: frequencyBean = FrequencyBean(chatFrequencyMillisecond=2000, chatFrequencyTips=too fast)
        Log.d(TAG, "onCreate: frequencyBean = $frequencyBean")
    }

    private fun test3() {
        val frequencyBean = JsonUtilKt.instance.toObject(
            "{\"chatFrequencyTips\": \"too fast\"}",
            FrequencyBean::class.java
        )

        //chatFrequencyMillisecond 没有这个字段，onCreate: frequencyBean = FrequencyBean(chatFrequencyMillisecond=1000, chatFrequencyTips=too fast)
        Log.d(TAG, "onCreate: frequencyBean = $frequencyBean")
    }

    private fun test4() {
        val frequencyBean = JsonUtilKt.instance.toObject(
            "{\"chatFrequencyMillisecond\": 1000,\"chatFrequencyTips\": \"too fast\"}",
            FrequencyBean::class.java
        )

        //chatFrequencyTips 字段正常有值
        //FrequencyBean(chatFrequencyMillisecond=1000, chatFrequencyTips=too fast)
        Log.d(TAG, "onCreate: frequencyBean = $frequencyBean")
    }

    private fun test5() {
        val frequencyBean = JsonUtilKt.instance.toObject(
            "{\"chatFrequencyMillisecond\": 1000,\"chatFrequencyTips\": null}",
            FrequencyBean::class.java
        )

        //chatFrequencyTips 字段为null
        //FrequencyBean(chatFrequencyMillisecond=1000, chatFrequencyTips=null)
        Log.d(TAG, "onCreate: frequencyBean = $frequencyBean")
    }

    private fun test6() {
        val frequencyBean = JsonUtilKt.instance.toObject(
            "{\"chatFrequencyMillisecond\": 1000}",
            FrequencyBean::class.java
        )

        //没有 chatFrequencyTips 字段
        //FrequencyBean(chatFrequencyMillisecond=1000, chatFrequencyTips=default value)
        Log.d(TAG, "onCreate: frequencyBean = $frequencyBean")
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
}
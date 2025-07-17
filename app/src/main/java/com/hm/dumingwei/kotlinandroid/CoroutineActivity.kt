package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hm.dumingwei.kotlinandroid.databinding.ActivityCoroutineBinding
import com.hm.dumingwei.mvp.presenter.CoroutinePresenter
import com.hm.dumingwei.mvp.view.CoroutineView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Crete by dumingwei on 2019/3/8
 * Desc: 协程关联生命周期
 * 我们通过创建一个和Activity生命周期关联的Job来管理协程的生命周期。我们在onCreate方法中创建Job对象，
 * 在onDestroy中调用Job的取消方法。
 */
class CoroutineActivity : AppCompatActivity(), View.OnClickListener, CoroutineView, CoroutineScope by MainScope() {

    private lateinit var presenter: CoroutinePresenter

    companion object {

        @JvmStatic
        fun launch(context: Context) {
            val intent = Intent(context, CoroutineActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityCoroutineBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoroutineBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = CoroutinePresenter(this)
        presenter.attachView(this)

        binding.btnFirst.setOnClickListener(this)
        binding.btnSecond.setOnClickListener(this)
        binding.btnThird.setOnClickListener(this)
        binding.btnForth.setOnClickListener(this)

        testTryCatch { string ->
            Log.i(TAG, "onCreate: string = " + string)
        }
    }

    private val TAG: String = "CoroutineActivity"

    //var errorAction: ((exception: Exception) -> Unit)? = null

    private fun testTryCatch(action: ((msg: String) -> Unit)? = null, errorAction: ((msg: String) -> Unit)? = null) {
        try {
            action?.invoke("")
        } catch (e: Exception) {
            Log.i(TAG, "testTryCatch: ")
            errorAction?.invoke(e.message ?: "")
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnFirst -> {
                presenter.getPublicEvent1()
            }
            R.id.btnSecond -> {
                presenter.getPublicEvent2()
            }
            R.id.btnThird -> {
                presenter.getPublicEvent3()
            }
            R.id.btnForth -> {
                presenter.getPublicEvent4()
            }
        }
    }

    override fun canUpdateUI(): Boolean {
        return !isFinishing
    }

    override fun setResult(text: String) {
        binding.tvResult.text = text
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    private suspend fun methodA() {
        Log.d(TAG, "methodA: ")
    }

    private suspend fun methodB() {
        Log.d(TAG, "methodB: ")
    }

    private fun test() {
        GlobalScope.launch {
            //methodA ， methodB 顺序执行
            methodA()
            methodB()
        }
    }

    private fun testAsync() {
        GlobalScope.launch {
            //methodA ， methodB 并发执行
            val jobA = async { methodA() }
            val jobB = async { methodB() }
            jobA.await()
            jobB.await()
        }
    }

}

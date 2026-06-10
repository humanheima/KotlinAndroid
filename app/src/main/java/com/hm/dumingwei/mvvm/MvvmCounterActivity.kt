package com.hm.dumingwei.mvvm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * Created by p_dmweidu on 2026/6/10
 * Desc: MVVM 演示页面（计数器）
 *
 * View 的职责：
 * 1. 分别订阅 ViewModel 的多个状态（count、tips），各自更新对应控件。
 * 2. 按钮点击时，直接调用 ViewModel 对应的方法。
 */
class MvvmCounterActivity : AppCompatActivity() {

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, MvvmCounterActivity::class.java))
        }
    }

    // 没有 activity-ktx 的 by viewModels()，这里用 ViewModelProvider 获取
    private val viewModel by lazy {
        ViewModelProvider(this).get(CounterViewModel::class.java)
    }

    private lateinit var tvCount: TextView
    private lateinit var tvTips: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContentView())
        observeState()
    }

    /** 分别订阅每个状态 —— 这是 MVVM 的典型写法 */
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 订阅 count
                launch {
                    viewModel.count.collect { count ->
                        tvCount.text = "当前计数：$count"
                    }
                }
                // 订阅 tips
                launch {
                    viewModel.tips.collect { tips ->
                        tvTips.text = tips
                    }
                }
            }
        }
    }

    private fun buildContentView(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)

            tvCount = TextView(context).apply { textSize = 24f }
            tvTips = TextView(context).apply { textSize = 16f }

            // 按钮直接调用 ViewModel 的方法
            val btnInc = Button(context).apply {
                text = "加一"
                setOnClickListener { viewModel.increment() }
            }
            val btnDec = Button(context).apply {
                text = "减一"
                setOnClickListener { viewModel.decrement() }
            }
            val btnReset = Button(context).apply {
                text = "重置"
                setOnClickListener { viewModel.reset() }
            }

            addView(tvCount)
            addView(tvTips)
            addView(btnInc)
            addView(btnDec)
            addView(btnReset)
        }
    }
}

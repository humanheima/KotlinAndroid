package com.hm.dumingwei.mvi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * Created by p_dmweidu on 2026/6/10
 * Desc: MVI 演示页面（计数器）
 *
 * View 的职责（与 MVVM 对比）：
 * 1. 只订阅【一个】state，用 render(state) 整体刷新 UI。
 * 2. 按钮点击不直接调方法，而是发送 Intent：viewModel.dispatch(CounterIntent.Xxx)。
 * 3. 一次性事件单独订阅 effect。
 */
class MviCounterActivity : AppCompatActivity() {

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, MviCounterActivity::class.java))
        }
    }

    private val viewModel by lazy {
        ViewModelProvider(this).get(MviCounterViewModel::class.java)
    }

    private lateinit var tvCount: TextView
    private lateinit var tvTips: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContentView())
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 只订阅一个 state，整体渲染
                launch {
                    viewModel.state.collect { state -> render(state) }
                }
                // 订阅一次性副作用
                launch {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is CounterEffect.Toast ->
                                Toast.makeText(this@MviCounterActivity, effect.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    /** 单一渲染函数：state 来了就整体刷新 */
    private fun render(state: CounterState) {
        tvCount.text = "当前计数：${state.count}"
        tvTips.text = state.tips
    }

    private fun buildContentView(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)

            tvCount = TextView(context).apply { textSize = 24f }
            tvTips = TextView(context).apply { textSize = 16f }

            // 按钮发送 Intent，而不是直接调方法
            val btnInc = Button(context).apply {
                text = "加一"
                setOnClickListener { viewModel.dispatch(CounterIntent.Increment) }
            }
            val btnDec = Button(context).apply {
                text = "减一"
                setOnClickListener { viewModel.dispatch(CounterIntent.Decrement) }
            }
            val btnReset = Button(context).apply {
                text = "重置"
                setOnClickListener { viewModel.dispatch(CounterIntent.Reset) }
            }

            addView(tvCount)
            addView(tvTips)
            addView(btnInc)
            addView(btnDec)
            addView(btnReset)
        }
    }
}

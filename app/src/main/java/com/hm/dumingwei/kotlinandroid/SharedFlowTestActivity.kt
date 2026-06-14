package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hm.dumingwei.kotlinandroid.databinding.ActivityFlowTestBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Created by dumingwei on 2026/06/13
 * Desc: 测试 SharedFlow。
 *
 * SharedFlow 是通用的热流（事件流），与 StateFlow 的关键区别：
 * 1. 无需初始值，按需发射事件；
 * 2. 不去重，相同的值连续发射都会送达所有订阅者；
 * 3. 是否「粘性」由 replay 决定：replay = 0 非粘性（订阅前的事件收不到），
 *    replay = N 粘性（迟到订阅者会先收到最近 N 个历史事件）；
 * 4. 天然支持广播：一次 emit，所有活跃订阅者都会收到。
 *
 * 适合一次性事件（Toast、导航、点赞等），用 replay = 0 可避免重复触发。
 */
class SharedFlowTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFlowTestBinding

    private val viewModel: SharedFlowViewModel by lazy {
        ViewModelProvider(this)[SharedFlowViewModel::class.java]
    }

    private val logBuilder = StringBuilder()

    // 订阅者编号，用于在日志里区分不同订阅者。
    private var subscriberIndex = 0

    companion object {

        private const val TAG = "SharedFlowTest"

        fun launch(context: Context) {
            val starter = Intent(context, SharedFlowTestActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlowTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // —— 一、非粘性事件流（replay = 0） ——

        // 注册一个订阅者，之后发送的事件它都会收到。
        binding.btnSubscribe.setOnClickListener {
            subscribe(viewModel.events, "非粘性")
        }

        // 一次 emit 广播给所有活跃订阅者；没有订阅者时事件直接丢失。
        binding.btnSendEvent.setOnClickListener {
            lifecycleScope.launch {
                viewModel.sendEvent("事件@${nextSeq()}")
            }
        }

        // 连发两个相同的值，SharedFlow 不去重，订阅者会收到两次（对比 StateFlow 去重）。
        binding.btnSendDuplicate.setOnClickListener {
            lifecycleScope.launch {
                viewModel.sendEvent("重复值X")
                viewModel.sendEvent("重复值X")
            }
        }

        // 先发事件、再注册新订阅者：因为 replay = 0，新订阅者收不到这条旧事件，
        // 但能收到之后再发的事件，以此证明「非粘性」。
        binding.btnLateSubscribe.setOnClickListener {
            lifecycleScope.launch {
                viewModel.sendEvent("订阅前发送的旧事件（迟到者收不到）")
                delay(200)
                val id = subscribe(viewModel.events, "非粘性-迟到")
                delay(200)
                viewModel.sendEvent("订阅后发送的新事件（迟到者$id 能收到）")
            }
        }

        // —— 二、粘性事件流（replay = 2） ——

        // 向 replay = 2 的流发送事件，最近 2 个会被缓存下来用于重放。
        binding.btnSendReplayEvent.setOnClickListener {
            lifecycleScope.launch {
                viewModel.sendReplayEvent("replay事件@${nextSeq()}")
            }
        }

        // 注册迟到订阅者：即使在它订阅之前就发过事件，它也会先收到最近 2 个历史事件，证明「粘性」。
        binding.btnLateSubscribeReplay.setOnClickListener {
            subscribe(viewModel.replayEvents, "粘性-迟到")
        }

        binding.btnClearLog.setOnClickListener {
            logBuilder.setLength(0)
            binding.tvLog.text = ""
        }
    }

    /**
     * 每个订阅者用独立的协程收集（绑定到 lifecycleScope，页面销毁时自动取消）。
     * 返回订阅者编号，方便日志区分。
     */
    private fun subscribe(flow: SharedFlow<String>, tag: String): Int {
        val id = ++subscriberIndex
        appendLog("▶ 注册订阅者#$id（$tag）")
        lifecycleScope.launch {
            flow.collect { value ->
                appendLog("订阅者#$id（$tag）收到：$value")
            }
        }
        return id
    }

    private var seq = 0
    private fun nextSeq(): Int = ++seq

    private fun appendLog(message: String) {
        logBuilder.insert(0, message + "\n")
        // 只保留最近若干行，避免无限增长。
        val lines = logBuilder.lines()
        if (lines.size > 20) {
            logBuilder.setLength(0)
            logBuilder.append(lines.take(20).joinToString("\n"))
        }
        binding.tvLog.text = logBuilder.toString()
        Log.i(TAG, message)
    }
}

/**
 * 用 ViewModel 持有 SharedFlow，演示事件总线（EventBus）的典型用法。
 */
class SharedFlowViewModel : ViewModel() {

    // 非粘性事件流：replay 默认 0，订阅之前发送的事件收不到。一次性事件应该这样用。
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    // 粘性事件流：replay = 2，迟到订阅者会先收到最近 2 个历史事件。
    private val _replayEvents = MutableSharedFlow<String>(replay = 2)
    val replayEvents: SharedFlow<String> = _replayEvents.asSharedFlow()

    suspend fun sendEvent(event: String) {
        _events.emit(event)
    }

    suspend fun sendReplayEvent(event: String) {
        _replayEvents.emit(event)
    }
}

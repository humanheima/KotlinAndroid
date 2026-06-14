package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.hm.dumingwei.kotlinandroid.databinding.ActivityStateFlowTestBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by dumingwei on 2026/06/13
 * Desc: 测试 StateFlow。
 *
 * StateFlow 是 SharedFlow 的子类，专为状态管理设计：
 * 热流，线程安全
 * 1. 必须有初始值，始终持有最新值；
 * 2. replay = 1，新订阅者会立即收到当前值；
 * 3. 自动去重（基于 equals），相同的值不会通知订阅者；
 * 4. 支持多个订阅者，所有活跃订阅者都会收到最新状态。
 */
class StateFlowTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStateFlowTestBinding

    private val viewModel: StateFlowViewModel by lazy {
        ViewModelProvider(this)[StateFlowViewModel::class.java]
    }

    private val logBuilder = StringBuilder()

    companion object {

        private const val TAG = "StateFlowTest"

        fun launch(context: Context) {
            val starter = Intent(context, StateFlowTestActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStateFlowTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeState()
        setupClickListeners()
    }

    private fun observeState() {
        // 订阅者 1：更新计数 UI。
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.count.collect {value ->
                    binding.tvCount.text = "count = $value"
                    appendLog("订阅者1 收到 count = $value")
                }
            }
        }

        // 订阅者 2：同一个 StateFlow 的第二个订阅者，演示多订阅者一致性。
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.count.collect { value ->
                    Log.i(TAG, "订阅者2 收到 count = $value")
                }
            }
        }

        // 订阅网络状态。
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.networkConnected.collect { connected ->
                    binding.tvNetworkStatus.text = if (connected) "已连接" else "未连接"
                    binding.tvNetworkStatus.setTextColor(
                        if (connected) 0xFF2E7D32.toInt() else 0xFFC62828.toInt()
                    )
                    appendLog("网络状态 = ${if (connected) "已连接" else "未连接"}")
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnIncrement.setOnClickListener {
            viewModel.increment()
        }

        // 发射与当前相同的值，StateFlow 会自动去重，订阅者不会收到通知。
        binding.btnSameValue.setOnClickListener {
            viewModel.emitSameValue()
        }

        binding.btnAutoIncrement.setOnClickListener {
            viewModel.autoIncrement()
        }

        binding.btnToggleNetwork.setOnClickListener {
            viewModel.toggleNetwork()
        }
    }

    private fun appendLog(message: String) {
        logBuilder.insert(0, message + "\n")
        // 只保留最近若干行，避免无限增长。
        val lines = logBuilder.lines()
        if (lines.size > 12) {
            logBuilder.setLength(0)
            logBuilder.append(lines.take(12).joinToString("\n"))
        }
        binding.tvLog.text = logBuilder.toString()
        Log.i(TAG, message)
    }
}

/**
 * 用 ViewModel 持有 StateFlow，是 Android 中推荐的状态管理方式，可替代 LiveData。
 */
class StateFlowViewModel : ViewModel() {

    // 计数状态，初始值为 0。
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    // 网络连接状态，初始值为 false。
    private val _networkConnected = MutableStateFlow(false)
    val networkConnected: StateFlow<Boolean> = _networkConnected.asStateFlow()

    // update {} 内部是 CAS 自旋（compareAndSet），天然保证「读-改-写」的原子性，
    // 多线程并发自增也不会丢失更新，无需额外加锁。
    // 注意：lambda 在 CAS 失败时会重试，可能执行多次，所以必须是无副作用的纯函数。
    fun increment() {
        _count.update { it + 1 }
    }

    /**
     * 发射与当前相同的值，由于 StateFlow 自动去重，订阅者不会收到通知。
     * 用 update { it } 而非 `value = value`：CAS 保证写回的就是读到的值，
     * 不会因并发期间别的线程改了值而把新值覆盖回旧值。
     */
    fun emitSameValue() {
        _count.update { it }
    }

    fun autoIncrement() {
        viewModelScope.launch {
            repeat(5) {
                delay(1000)
                _count.update { it + 1 }
            }
        }
    }

    fun toggleNetwork() {
        _networkConnected.value = !_networkConnected.value
    }
}

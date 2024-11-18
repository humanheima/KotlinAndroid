package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hm.dumingwei.kotlinandroid.databinding.ActivityFlowTestBinding
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by p_dmweidu on 2024/11/18
 * Desc: 测试Kotlin flow
 */
class FlowTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFlowTestBinding

    companion object {

        private const val TAG = "FlowTestActivity"

        fun launch(context: Context) {
            val starter = Intent(context, FlowTestActivity::class.java)
            context.startActivity(starter)
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlowTestBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnTest1.setOnClickListener {
            val flow = simpleFlow()
            lifecycleScope.launch {
                flow.collect(object : FlowCollector<Int> {
                    override suspend fun emit(value: Int) {
                        Log.i(TAG, "onCreate: emit $value")
                    }
                })
            }
        }

        binding.btnFilterMap.setOnClickListener {
            //val flow = transformFlow()
            val flow = transformFlow()
            lifecycleScope.launch {
                flow.collect(object : FlowCollector<Int> {
                    override suspend fun emit(value: Int) {
                        Log.i(TAG, "onCreate: emit $value")
                    }
                })
            }
        }

        binding.btnCombine.setOnClickListener {
            /**
             * Flow1，Flow2，如果其中一个Flow2输出的值少,那么另一个Flow1会继续执行，并以Flow2的最新的值为组合
             */
            val flow = combineFlow(simpleFlow(), simpleFlow1())
            lifecycleScope.launch {
                flow.collect(object : FlowCollector<String> {
                    override suspend fun emit(value: String) {
                        Log.i(TAG, "onCreate: emit $value")
                    }
                })
            }
        }


        binding.btnErrorHandle.setOnClickListener {
            val flow = safeFlow()
            lifecycleScope.launch {
                flow.collect(object : FlowCollector<Int> {
                    override suspend fun emit(value: Int) {
                        Log.i(TAG, "onCreate: emit $value")
                    }
                })
            }
        }

        val counter = Counter()

        binding.btnStateFlow.setOnClickListener {
            lifecycleScope.launch {

                counter.count.collect(object : FlowCollector<Int> {
                    override suspend fun emit(value: Int) {
                        Log.i(TAG, "onCreate: emit $value")
                    }
                })
            }
        }

        binding.btnAddStateFlow.setOnClickListener {
            lifecycleScope.launch {
                // 更新计数
                repeat(5) {
                    delay(1000) // 每秒增加一次
                    counter.increment()
                }
            }
        }
        val eventBus = EventBus()

        binding.btnSharedFlow.setOnClickListener {


//            repeat(3) { index ->
//                lifecycleScope.launch {
//                    eventBus.events.collect(object : FlowCollector<String> {
//                        override suspend fun emit(value: String) {
//                            Log.d(TAG, "emit $index: 收到的值 $value ")
//                        }
//                    })
//                }
//            }
            // 启动一个协程来收集事件
//            lifecycleScope.launch {
//                repeat(3) { index ->
//                    Log.d(TAG, "onCreate: index = $index")
//                    eventBus.events.collect(object : FlowCollector<String> {
//                        override suspend fun emit(value: String) {
//                            Log.d(TAG, "内部repeat emit$index:收到的值  $value ")
//                        }
//                    })
//                }
//            }

//            lifecycleScope.launch {
//                eventBus.events.collect(object : FlowCollector<String> {
//                    override suspend fun emit(value: String) {
//                        Log.d(TAG, "emit1:  $value ")
//                    }
//                })
//
//                eventBus.events.collect(object : FlowCollector<String> {
//                    override suspend fun emit(value: String) {
//                        Log.d(TAG, "emit2:  $value ")
//                    }
//                })
//            }
        }

        binding.btnSendSharedFlowEvent.setOnClickListener {
            lifecycleScope.launch {
                eventBus.sendEvent("Hello")
                delay(1000) // 延迟 1 秒()
                eventBus.sendEvent("World")
            }
        }
    }

    private fun simpleFlow(): Flow<Int> = flow {
        for (i in 1..10) {
            emit(i)
            delay(1000L) // 延迟 1 秒()
        }
    }

    private fun simpleFlow1(): Flow<Int> = flow {
        for (i in 1..4) {
            emit(i)
            delay(1000L) // 延迟 1 秒()
        }
    }

    private fun transformFlow(): Flow<Int> {
        return simpleFlow().filter {
            it % 2 == 0
        }.map {
            it * 10
        }
    }

    private fun combineFlow(flow1: Flow<Int>, flow2: Flow<Int>): Flow<String> {
        return flow1.combine(flow2) { a, b ->
            "flow1 $a  flow2 $b"
        }
    }


    private fun safeFlow(): Flow<Int> {
        return flow {
            for (i in 1..4) {
                emit(i)
                delay(1000L) // 延迟 1 秒()
                throw RuntimeException("Error!")
            }
        }.catch { e ->
            emit(-1) // 发生错误时发射一个默认值
        }
    }


}

// 创建一个 MutableStateFlow
class Counter {
    private val _count = MutableStateFlow(0) // 私有的可变状态流
    val count: StateFlow<Int> get() = _count // 公共只读状态流

    fun increment() {
        _count.value += 1 // 更新状态
    }
}

// 创建一个 MutableSharedFlow
class EventBus {
    private val _events = MutableSharedFlow<String>() // 私有的可变共享流

    // 发射事件
    suspend fun sendEvent(event: String) {
        _events.emit(event)
    }

    // 获取共享流
    val events = _events
}

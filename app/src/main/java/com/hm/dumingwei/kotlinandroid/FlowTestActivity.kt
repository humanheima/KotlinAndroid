package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hm.dumingwei.kotlinandroid.databinding.ActivityFlowTestBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

/**
 * Created by p_dmweidu on 2024/11/18
 * Desc: 测试Kotlin flow
 */
class FlowTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFlowTestBinding


    /**
     * 点赞
     */
    private val _mLikeFlow by lazy { MutableStateFlow<String?>(null) }

    companion object {

        private const val TAG = "FlowTestActivity"

        fun launch(context: Context) {
            val starter = Intent(context, FlowTestActivity::class.java)
            context.startActivity(starter)
        }
    }

    private val _sharedFlow = MutableSharedFlow<String?>()
    val sharedFlow = _sharedFlow.asSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlowTestBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        lifecycleScope.launch {
            sharedFlow.collect { Log.e(TAG, "onCreate:Received: $it") }
        }

        lifecycleScope.launch {
            sharedFlow.filterNotNull().collect { Log.e(TAG, "onCreate:Received2: $it") }

        }
        lifecycleScope.launch {

            delay(1000)
            _sharedFlow.emit(null)
            _sharedFlow.emit("A")
            _sharedFlow.emit("A") // 重复值
            _sharedFlow.emit("B")
        }

        binding.btnUpdateFlowValue.setOnClickListener {
            lifecycleScope.launch {
                _sharedFlow.emit("点赞")
            }

        }


        binding.btnTest1.setOnClickListener {
            val flow = listToFlow()
            lifecycleScope.launch {
                flow.onCompletion {
                    Log.i(TAG, "onCreate: onCompletion")
                }.collect(object : FlowCollector<Int> {
                    override suspend fun emit(value: Int) {
                        Log.i(TAG, "onCreate: emit $value")
                    }
                })
            }
        }

        binding.btnFilterMap.setOnClickListener {
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
            lifecycleScope.launch { flow.collect { value -> Log.i(TAG, "onCreate: emit $value") } }
        }

        binding.btnZip.setOnClickListener {
            /**
             * Flow1，Flow2，如果其中一个Flow2输出的值少,那么另一个Flow1会继续执行，并以Flow2的最新的值为组合
             */
            val flow = zipFlow()
            lifecycleScope.launch {
                flow.collect { value -> Log.i(TAG, "onCreate: zip $value") }
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

        stateFlowTest()


        val eventBus = EventBus()


        binding.btnSharedFlow.setOnClickListener {
            repeat(3) { index ->
                lifecycleScope.launch {
                    eventBus.events.collect { value -> Log.d(TAG, "emit $index: 收到的值 $value ") }
                }
            }
        }

        binding.btnSendSharedFlowEvent.setOnClickListener {
            lifecycleScope.launch {
                eventBus.sendEvent("Hello")
                delay(1000) // 延迟 1 秒()
                eventBus.sendEvent("World")
            }
        }

        binding.btnSendSharedFlowEventReplay.setOnClickListener {
            lifecycleScope.launch {
                eventBus.events.collect(object : FlowCollector<String> {
                    override suspend fun emit(value: String) {
                        Log.d(TAG, "在发射数据之后收集，测试replay : 收到的值 $value ")
                    }
                })
            }
        }
    }

    private fun stateFlowTest(): Counter {
        val counter = Counter()
        lifecycleScope.launch {
            counter.count.collect { value ->
                Log.i(
                    TAG,
                    "stateFlow 第一个订阅者 onCreate: emit $value"
                )
            }
        }
        lifecycleScope.launch {
            counter.count.collect { value ->
                Log.i(
                    TAG,
                    "stateFlow 第二个订阅者 onCreate: emit $value"
                )
            }
        }

        lifecycleScope.launch {

            //只有当 生命周期到达 STARTED 时才会收集才执行。如果从 STARTED 到了 STOPPED 状态的时候，协程会取消。
            //当再次从 STOPPED 到 STARTED 状态，会重新执行这个协程
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                counter.count.collect { value ->
                    Log.i(
                        TAG,
                        "stateFlow 第三个订阅者 onCreate: emit $value"
                    )
                }
            }
        }

        binding.btnStateFlow.setOnClickListener {
            //相同的值不会发射
            counter.increment()
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

        return counter
    }

    private fun simpleFlow(): Flow<Int> {
        val flow = flow<Int> {
            for (i in 1..10) {
                emit(i)
                delay(1000L) // 延迟 1 秒()
            }
        }

        //固定元素个数的flow
        //flowOf(1,2,3,4)
        return flow
    }

    /**
     * channelFlow
     */
    val flow = channelFlow {
        launch {
            send(1) // 类似 emit，但更适合并发场景
            delay(100)
            send(2)
        }
    }

    /**
     * callbackFlow
     */
//    val callbackFlow = callbackFlow {
//        val callback = object : SomeCallback {
//            override fun onData(data: Int) {
//                trySend(data) // 发射数据
//            }
//        }
//        registerCallback(callback) // 注册回调
//        awaitClose { unregisterCallback(callback) } // 清理资源
//    }


    /**
     * 将 集合 转换为 Flow
     */
    private fun listToFlow(): Flow<Int> {
        return listOf(1, 2, 3).asFlow()
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


    /**
     * zip，一个flow结束，另一个flow立即结束
     * 只会输出3个值
     *
     *  flow 1    flow2 a
     *
     *  flow 2    flow2 b
     *
     *  flow 3    flow2 c
     *
     */
    private fun zipFlow(): Flow<String> {
        val flow = flowOf(1, 2, 3).onEach { delay(1000) }
        val flow2 = flowOf("a", "b", "c", "d").onEach { delay(15) }
        return flow.zip<Int, String, String>(flow2) { a, b ->
            "flow $a    flow2 $b"
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
    private val _events = MutableSharedFlow<String>(replay = 2) // 私有的可变共享流

    // 发射事件
    suspend fun sendEvent(event: String) {
        _events.emit(event)
    }

    // 获取共享流
    val events = _events
}

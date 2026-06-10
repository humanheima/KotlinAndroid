package com.hm.dumingwei.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * ============================================================
 * Kotlin 协程 —— Channel（通道）速览
 * ============================================================
 *
 * 一句话：Channel 是协程之间的「管道」，一端 send 一端 receive，
 *        是面向「流式 / 多个值」的通信原语。可以类比 BlockingQueue，
 *        但它的 send/receive 是「挂起」而不是「阻塞」线程。
 *
 * 与其它概念的关系：
 * - suspend 函数 / Deferred(async)  ：返回「单个」结果。
 * - Channel                         ：传递「多个」值的「热」数据流，面向通信。
 * - Flow                            ：传递「多个」值的「冷」数据流，面向数据转换。
 *   （Channel 是热的——没有接收者时生产者也可能在跑；Flow 是冷的——
 *     不 collect 就不执行。需要「多个协程之间通信」用 Channel，
 *     需要「一条数据处理链」优先用 Flow。）
 *
 * 四种容量（capacity）：
 * - Channel.RENDEZVOUS(0,默认) ：无缓冲，send 必须等到有人 receive 才返回（一手交钱一手交货）。
 * - Channel.BUFFERED / 指定 N   ：有缓冲，缓冲满了 send 才挂起。
 * - Channel.CONFLATED          ：只保留最新值，旧值被覆盖，send 永不挂起。
 * - Channel.UNLIMITED          ：无限缓冲，send 永不挂起（注意内存）。
 *
 * 关闭：
 * - 生产者 close() 后，消费者用 for / consumeEach 会在「取空后」自动结束循环。
 * - close 像是发了一个特殊的「结束标记」，标记之前的元素仍可被取走。
 */

/* ---------- 示例 1：最基础的 send / receive ---------- */

private suspend fun basicSendReceive() = runBlocking {
    println("--- 示例 1：基础 send / receive ---")
    val channel = Channel<Int>()

    // 生产者协程：发 5 个平方数
    launch {
        for (x in 1..5) {
            channel.send(x * x)          // 没人收时在此挂起（默认无缓冲）
        }
        channel.close()                  // 发完一定要关，否则消费端会一直等
    }

    // 消费者：for 循环会在 channel 关闭并取空后自动退出
    for (y in channel) {
        println("收到: $y")
    }
    println("通道已关闭，接收结束\n")
}

/* ---------- 示例 2：四种容量的区别 ---------- */

private suspend fun capacityDemo() = runBlocking {
    println("--- 示例 2：CONFLATED 只保留最新值 ---")
    // CONFLATED：生产快、消费慢时，中间值会被丢掉，消费者只看到最新的
    val channel = Channel<Int>(Channel.CONFLATED)
    launch {
        for (i in 1..5) {
            channel.send(i)
            println("发送: $i")
        }
        channel.close()
    }
    delay(100)                           // 故意让消费者慢一点
    for (v in channel) {
        println("收到: $v")              // 大概率只收到 5（最新值）
    }
    println()
}

/* ---------- 示例 3：produce —— 生产者的便捷写法 ---------- */

/**
 * produce{} 会启动一个协程并返回 ReceiveChannel，
 * 协程结束时自动 close()，比手动 launch + close 更省心。
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
    for (x in 1..5) send(x * x)
}

private suspend fun produceDemo() = runBlocking {
    println("--- 示例 3：produce 生产者 ---")
    val squares = produceSquares()
    squares.consumeEach { println("平方数: $it") }   // consumeEach 收完自动取消通道
    println()
}

/* ---------- 示例 4：Pipeline 管道 —— 通道串联 ---------- */

/**
 * 一个协程的输出通道，作为下一个协程的输入通道，串成流水线。
 * 这里：自然数 -> 平方 -> 打印。
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.numbersFrom(start: Int): ReceiveChannel<Int> = produce {
    var n = start
    while (true) send(n++)               // 无限生产，靠下游取消来停止
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
    for (n in numbers) send(n * n)
}

private suspend fun pipelineDemo() = runBlocking {
    println("--- 示例 4：Pipeline 管道 ---")
    val numbers = numbersFrom(1)
    val squares = square(numbers)
    repeat(5) { println("管道输出: ${squares.receive()}") }
    println("取消上游协程")
    coroutineContext.cancelChildren()    // 取消管道里所有还在跑的协程
    println()
}

/* ---------- 示例 5：Fan-out 扇出 —— 多个消费者抢一个通道 ---------- */

/**
 * 多个 worker 从同一个通道 receive，谁空闲谁来取，天然实现负载均衡。
 * 这正是 TicketSellingCoroutine.kt 里「4 个售货员卖 50 张票」的模型。
 */
private suspend fun fanOutDemo() = runBlocking {
    println("--- 示例 5：Fan-out 多消费者 ---")
    val tasks = Channel<Int>()
    launch {                             // 一个生产者发 10 个任务
        for (i in 1..10) tasks.send(i)
        tasks.close()
    }
    repeat(3) { workerId ->              // 3 个 worker 抢着处理
        launch(Dispatchers.Default) {
            for (task in tasks) {
                println("worker $workerId 处理任务 $task")
            }
        }
    }
    delay(200)                           // 等 worker 处理完（示例图省事，生产代码应 join）
    println()
}

fun main() = runBlocking {
    basicSendReceive()
    capacityDemo()
    produceDemo()
    pipelineDemo()
    fanOutDemo()
}

/*
 * 小结 / 选型建议：
 * 1. 只在「多个协程之间需要通信 / 传值」时用 Channel；单纯做数据流转换优先 Flow。
 * 2. send 完记得 close，否则消费端的 for 循环永远等不到结束。
 * 3. Channel 是热的、只能被消费一次（一个元素被一个接收者取走）。
 *    需要广播给多个订阅者请用 BroadcastChannel(已废弃) 的替代品 —— SharedFlow。
 * 4. 默认无缓冲(RENDEZVOUS) 能形成天然背压；要削峰填谷用 BUFFERED；
 *    只关心最新状态用 CONFLATED（很适合 UI 状态）。
 * 5. 「共享内存 + 锁」往往可以改写成「用 Channel 通信」，从根上消除竞态。
 */

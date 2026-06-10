package com.hm.dumingwei.thread

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 协程版「50 张票，4 个售货员，不超卖」。
 *
 * 与线程版的区别：
 * - 线程版用 synchronized / ReentrantLock 这种「阻塞锁」；
 * - 协程里不能用阻塞锁（会卡住整个线程），要用协程友好的 [Mutex]（挂起而非阻塞），
 *   或者干脆用 [Channel] 把共享状态消除掉。
 */

private const val TOTAL = 50
private const val SELLERS = 4

/* ---------- 方案一：Mutex（协程的锁）---------- */

class CoroutineTicketOfficeMutex {
    private val mutex = Mutex()
    private var remaining = TOTAL

    /** @return 票号；-1 表示售罄 */
    suspend fun sell(): Int = mutex.withLock {
        if (remaining <= 0) return@withLock -1
        val ticketNo = TOTAL - remaining + 1
        remaining--
        ticketNo
    }
}

/**
 * 启动 4 个并发售货员协程，跑在多线程调度器上（真正并发，能暴露竞态）。
 */
private suspend fun runSaleMutex() = coroutineScope {
    val office = CoroutineTicketOfficeMutex()
    val soldBySeller = IntArray(SELLERS)

    val jobs = (0 until SELLERS).map { id ->
        launch(Dispatchers.Default) {
            while (true) {
                val no = office.sell()
                if (no == -1) break
                soldBySeller[id]++
                println("[Mutex] 售货员 ${id + 1} 卖出第 $no 张")
            }
        }
    }
    jobs.forEach { it.join() }

    println("--- 方案一：Mutex ---")
    soldBySeller.forEachIndexed { i, c -> println("售货员 ${i + 1} 共卖出 $c 张") }
    println("总计 ${soldBySeller.sum()} 张（应为 $TOTAL）\n")
}

/* ---------- 方案二：Channel（无共享状态，从根上避免竞态）---------- */

/**
 * 把 50 张票一次性发进一个 Channel，4 个售货员协程去 receive。
 * Channel 里只有 50 个元素，收完就关闭，物理上不可能卖超。
 * 这是 CSP 风格：用「通信」代替「共享内存 + 锁」。
 */
private suspend fun runSaleChannel() = coroutineScope {
    val tickets = Channel<Int>()
    val soldBySeller = IntArray(SELLERS)

    // 生产者：发出 1..50，发完关闭通道
    launch {
        for (no in 1..TOTAL) tickets.send(no)
        tickets.close()
    }

    // 4 个消费者（售货员）：通道关闭且取空后 for 循环自动结束
    val jobs = (0 until SELLERS).map { id ->
        launch(Dispatchers.Default) {
            for (no in tickets) {
                soldBySeller[id]++
                println("[Channel] 售货员 ${id + 1} 卖出第 $no 张")
            }
        }
    }
    jobs.forEach { it.join() }

    println("--- 方案二：Channel ---")
    soldBySeller.forEachIndexed { i, c -> println("售货员 ${i + 1} 共卖出 $c 张") }
    println("总计 ${soldBySeller.sum()} 张（应为 $TOTAL）\n")
}

fun main() = runBlocking {
    runSaleMutex()
    runSaleChannel()
}

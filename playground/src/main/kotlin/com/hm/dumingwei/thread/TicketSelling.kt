package com.hm.dumingwei.thread

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 经典并发问题：50 张票，4 个售货员同时售卖，不能超卖。
 *
 * 核心：「判断是否还有票」和「卖出一张票」这两步必须是原子操作。
 * 如果不加锁，多个线程可能同时通过 remaining > 0 的判断，导致卖出第 51、52 张票（超卖）。
 *
 * 下面给出三种正确实现 + 一种错误示范。
 */

private const val TOTAL_TICKETS = 50
private const val SELLER_COUNT = 4

/**
 * 方案一：synchronized 同步方法（最直观）
 *
 * 把「检查 + 扣减」整体放进同步块，保证同一时刻只有一个线程能执行。
 */
class TicketOfficeSynchronized {
    private var remaining = TOTAL_TICKETS

    /** @return 卖出的票号；返回 -1 表示已售罄 */
    @Synchronized
    fun sell(): Int {
        if (remaining <= 0) return -1
        val ticketNo = TOTAL_TICKETS - remaining + 1
        remaining--
        return ticketNo
    }
}

/**
 * 方案二：ReentrantLock（更灵活，可中断、可设超时）
 */
class TicketOfficeLock {
    private val lock = ReentrantLock()
    private var remaining = TOTAL_TICKETS

    fun sell(): Int = lock.withLock {
        if (remaining <= 0) return@withLock -1
        val ticketNo = TOTAL_TICKETS - remaining + 1
        remaining--
        ticketNo
    }
}

/**
 * 方案三：AtomicInteger 无锁（CAS）实现
 *
 * decrementAndGet() 是原子操作；先扣减再判断，若扣过头则不算有效售出。
 */
class TicketOfficeAtomic {
    private val remaining = AtomicInteger(TOTAL_TICKETS)

    fun sell(): Int {
        val left = remaining.decrementAndGet()
        if (left < 0) {
            // 扣过头了，说明票已售罄，把多扣的加回去
            remaining.incrementAndGet()
            return -1
        }
        return TOTAL_TICKETS - left
    }
}

/**
 * 错误示范：没有任何同步。
 * 多个线程会同时通过 remaining > 0 的检查，导致超卖（卖出超过 50 张）。
 */
class TicketOfficeWrong {
    private var remaining = TOTAL_TICKETS

    fun sell(): Int {
        if (remaining <= 0) return -1
        // 这里人为放大竞态窗口，更容易复现超卖
        Thread.sleep(1)
        val ticketNo = TOTAL_TICKETS - remaining + 1
        remaining--
        return ticketNo
    }
}

/**
 * 通用的运行器：启动 SELLER_COUNT 个线程不停地调用 sell()，
 * 统计每个售货员卖出的数量，并校验总数不超过 50。
 */
private fun runSale(name: String, sell: () -> Int) {
    val soldBySeller = IntArray(SELLER_COUNT)
    val soldTickets = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()
    var oversold = false

    val threads = (0 until SELLER_COUNT).map { sellerId ->
        Thread {
            while (true) {
                val ticketNo = sell()
                if (ticketNo == -1) break // 已售罄
                if (ticketNo > TOTAL_TICKETS) oversold = true
                if (!soldTickets.add(ticketNo)) {
                    // 同一张票被卖了两次，也属于超卖
                    oversold = true
                }
                soldBySeller[sellerId]++
                println("售货员 ${sellerId + 1} 卖出第 $ticketNo 张票")
            }
        }
    }

    threads.forEach { it.start() }
    threads.forEach { it.join() }

    val total = soldBySeller.sum()
    println("--- $name ---")
    soldBySeller.forEachIndexed { i, count -> println("售货员 ${i + 1} 共卖出 $count 张") }
    println("总计卖出 $total 张（应为 $TOTAL_TICKETS），是否超卖：${total > TOTAL_TICKETS || oversold}")
    println()
}

fun main() {
    // 注意：每个售票处实例只能跑一次（卖完就售罄），所以每个方案各 new 一个。
    runSale("方案一：synchronized", TicketOfficeSynchronized()::sell)
    runSale("方案二：ReentrantLock", TicketOfficeLock()::sell)
    runSale("方案三：AtomicInteger", TicketOfficeAtomic()::sell)
    runSale("错误示范：无同步（可能超卖）", TicketOfficeWrong()::sell)
}

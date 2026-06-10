package com.hm.dumingwei.mvi

/**
 * Created by p_dmweidu on 2026/6/10
 * Desc: MVI 契约（Contract）—— 把一个页面的 State / Intent / Effect 定义集中在一起。
 *
 * MVI 三要素：
 * - State ：描述 UI 在某一时刻的【完整】状态，是一个不可变数据类（唯一数据源）。
 * - Intent：用户/外部的【意图】，是密封类，枚举出页面所有可能的操作。
 * - Effect：一次性的副作用事件（弹 Toast、跳转、震动），消费一次就没了，不进 State。
 */

/** 唯一状态：用一个不可变 data class 描述整个 UI */
data class CounterState(
    val count: Int = 0,
    val tips: String = "点击按钮开始计数"
)

/** 意图：页面所有可能的操作都收敛到这个密封类里 */
sealed class CounterIntent {
    object Increment : CounterIntent()
    object Decrement : CounterIntent()
    object Reset : CounterIntent()
}

/** 一次性副作用事件 */
sealed class CounterEffect {
    data class Toast(val message: String) : CounterEffect()
}

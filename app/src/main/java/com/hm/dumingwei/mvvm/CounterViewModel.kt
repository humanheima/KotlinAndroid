package com.hm.dumingwei.mvvm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Created by p_dmweidu on 2026/6/10
 * Desc: MVVM 中的 ViewModel
 *
 * MVVM 特点：
 * 1. ViewModel 持有【多个】可观察状态（这里是 count 和 tips），View 分别订阅。
 * 2. View 直接调用 ViewModel 暴露的【多个方法】(increment / decrement / reset) 来驱动状态变化。
 * 3. 数据流是「双向感觉」的：View 调方法 -> VM 改状态 -> View 收到通知刷新。
 *    没有统一的入口，方法和状态都是按业务点分散暴露的。
 */
class CounterViewModel : ViewModel() {

    // 计数状态
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    // 另一个独立暴露的状态：提示文案。MVVM 里状态是可以这样一个个分开暴露的
    private val _tips = MutableStateFlow("点击按钮开始计数")
    val tips: StateFlow<String> = _tips.asStateFlow()

    fun increment() {
        _count.value += 1
        _tips.value = "已加到 ${_count.value}"
    }

    fun decrement() {
        if (_count.value <= 0) {
            _tips.value = "已经是 0 了，不能再减"
            return
        }
        _count.value -= 1
        _tips.value = "已减到 ${_count.value}"
    }

    fun reset() {
        _count.value = 0
        _tips.value = "已重置"
    }
}

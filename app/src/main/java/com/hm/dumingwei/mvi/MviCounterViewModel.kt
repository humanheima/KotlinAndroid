package com.hm.dumingwei.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Created by p_dmweidu on 2026/6/10
 * Desc: MVI 中的 ViewModel
 *
 * MVI 特点（与 MVVM 对比）：
 * 1. 对外只暴露【一个】状态 state（单一数据源 Single Source of Truth）。
 * 2. 对外只暴露【一个】入口 dispatch(intent)，所有操作都从这里进来（单向数据流）。
 * 3. 状态变化由 reduce(旧状态, intent) -> 新状态 这个纯函数集中决定，可预测、好测试。
 *    数据流是严格单向的：Intent -> reduce -> 新 State -> 渲染。
 */
class MviCounterViewModel : ViewModel() {

    // 唯一状态
    private val _state = MutableStateFlow(CounterState())
    val state: StateFlow<CounterState> = _state.asStateFlow()

    // 一次性事件用 Channel，保证只被消费一次（不会因为旋转屏幕重复弹 Toast）
    private val _effect = Channel<CounterEffect>(Channel.BUFFERED)
    val effect: Flow<CounterEffect> = _effect.receiveAsFlow()

    /** 唯一入口：所有用户操作都通过 dispatch 进来 */
    fun dispatch(intent: CounterIntent) {
        val old = _state.value
        val new = reduce(old, intent)
        _state.value = new

        // 触发一次性副作用
        if (intent is CounterIntent.Decrement && old.count <= 0) {
            _effect.trySend(CounterEffect.Toast("已经是 0 了，不能再减"))
        }
    }

    /** 纯函数 reducer：根据旧状态 + 意图，计算出新状态 */
    private fun reduce(state: CounterState, intent: CounterIntent): CounterState {
        return when (intent) {
            is CounterIntent.Increment ->
                state.copy(count = state.count + 1, tips = "已加到 ${state.count + 1}")

            is CounterIntent.Decrement ->
                if (state.count <= 0) state.copy(tips = "已经是 0 了，不能再减")
                else state.copy(count = state.count - 1, tips = "已减到 ${state.count - 1}")

            is CounterIntent.Reset ->
                CounterState(count = 0, tips = "已重置")
        }
    }
}

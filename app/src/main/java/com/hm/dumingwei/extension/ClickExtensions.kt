package com.hm.dumingwei.extension

import android.os.SystemClock
import android.util.Log
import android.view.View

/**
 * Created by p_dmweidu on 2026/6/10
 * Desc: 防止 View 重复点击（防抖）的扩展函数示例
 *
 * 核心思路：
 * 1. 记录上一次有效点击的时间戳，两次点击间隔小于 interval 时直接丢弃，
 *    避免快速连点导致重复触发（例如重复跳转页面、重复提交订单）。
 * 2. 用 SystemClock.elapsedRealtime 而非 System.currentTimeMillis，
 *    前者不受系统时间被修改影响，更适合做时间间隔判断。
 * 3. 状态保存在监听器闭包内，每个 View 各自独立、互不影响，
 *    也无需依赖资源 id 或 setTag。
 */

private const val TAG = "ClickExtensions"

// 默认防抖间隔，单位毫秒
const val DEFAULT_CLICK_INTERVAL = 500L

/**
 * 防抖点击：替代 setOnClickListener，自动过滤掉间隔过短的重复点击。
 *
 * 用法：
 * button.setOnSingleClickListener {
 *     // 真正的点击逻辑，连点只会触发一次
 * }
 *
 * @param interval 防抖间隔，单位毫秒，默认 500ms
 * @param onClick  有效点击时回调
 */
fun View.setOnSingleClickListener(
    interval: Long = DEFAULT_CLICK_INTERVAL,
    onClick: (View) -> Unit
) {
    // lastClickTime 保存在闭包中，是这个监听器（也就是这个 View）的私有状态
    var lastClickTime = 0L
    setOnClickListener { view ->
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime >= interval) {
            lastClickTime = now
            onClick(view)
        } else {
            Log.d(TAG, "setOnSingleClickListener: 距上次点击 ${now - lastClickTime}ms < ${interval}ms，忽略")
        }
    }
}

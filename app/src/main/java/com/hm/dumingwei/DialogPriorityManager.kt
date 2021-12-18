package com.hm.dumingwei

import android.util.Log
import android.util.SparseBooleanArray

/**
 * Created by dumingwei on 2021/11/25.
 *
 * Desc: 数字大，优先级高 因为今天(2021/11/25)是因为经典模式下小说引导弹窗开始的，就以小说引导弹窗为起点3000。
 * 以10为分割，防止中间插入弹窗。
 */
class DialogPriorityManager {

    companion object {

        private val TAG: String = "DialogPriorityManager"

        val sparseArray: SparseBooleanArray = SparseBooleanArray().also {
            it.put(Priority.NO_LOGIN_4000_COIN_DIALOG.value, false)
            it.put(Priority.START_TRUCK_DIALOG.value, false)
            it.put(Priority.NOVEL_TOP_GUIDE_DIALOG.value, false)
            it.put(Priority.NOVEL_GUIDE_DIALOG.value, false)
        }

        @JvmStatic
        fun setShowing(priority: Priority, showing: Boolean) {
            if (sparseArray.indexOfKey(priority.value) >= 0) {
                sparseArray.put(priority.value, showing)
            }
        }

        /**
         * 未登录模式下是否有高优先级的弹窗正在显示
         */
        @JvmStatic
        fun hasHigherShowing(priority: Priority): Boolean {
            Log.i(TAG, "checking ${priority.name} , ${priority.value}")
            val startIndex = sparseArray.indexOfKey(priority.value)
            if (startIndex < 0) {
                return false
            }
            for (i in startIndex until sparseArray.size()) {
                val key = sparseArray.keyAt(i)
                val hasShow = sparseArray[key]
                if (hasShow) {
                    Log.i(TAG, "checking ${priority.name} , ${priority.value}" +
                            "，has High Priority：${key}")
                    return true
                }
            }
            return false
        }


        private val loginedSparseArray: SparseBooleanArray = SparseBooleanArray()


        @JvmStatic
        fun setLoginedShowing(priority: Priority, showing: Boolean) {
            if (loginedSparseArray.indexOfKey(priority.value) >= 0) {
                loginedSparseArray.put(priority.value, showing)
            }
        }

        /**
         * 已登录模式下是否有高优先级的弹窗正在显示
         */
        @JvmStatic
        fun loginedHasHigherShowing(priority: Priority): Boolean {
            Log.i(TAG, "checking ${priority.name} , ${priority.value}")
            val startIndex = loginedSparseArray.indexOfKey(priority.value)
            if (startIndex < 0) {
                return false
            }
            for (i in startIndex until loginedSparseArray.size()) {
                val key = loginedSparseArray.keyAt(i)
                val hasShow = loginedSparseArray[key]
                if (hasShow) {
                    Log.i(TAG, "checking ${priority.name} , ${priority.value}" +
                            "，has High Priority：${key}")
                    return true
                }
            }
            return false
        }
    }

}

enum class Priority(val value: Int) {

    /**
     * 未登录4000金币的引导弹窗
     */
    NO_LOGIN_4000_COIN_DIALOG(2970),

    /**
     * 开启车友模式的引导弹窗
     */
    START_TRUCK_DIALOG(2980),

    /**
     * 极速听模式下的小说引导弹窗
     */
    NOVEL_TOP_GUIDE_DIALOG(2990),

    /**
     * 正常模式下的小说引导弹窗
     */
    NOVEL_GUIDE_DIALOG(3000), ;

}

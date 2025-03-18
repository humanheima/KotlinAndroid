package com.hm.dumingwei.kotlinandroid.bytest

import android.util.Log
import kotlin.reflect.KProperty


/**
 * Created by p_dmweidu on 2023/6/28
 * Desc: 代理类，要提供 getValue 和 setValue（对于var类型变量） 方法
 */
class Delegate {

    private val TAG = "Delegate"
    private var mvalue = "默认值"
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        Log.d(TAG, "getValue: $thisRef, 这里委托了 ${property.name} 属性")
        return mvalue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        Log.d(TAG, "setValue: $thisRef 的 ${property.name} 属性赋值为 $value")
        mvalue = value
    }
}

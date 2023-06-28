package com.hm.dumingwei.kotlinandroid.bytest

import kotlin.reflect.KProperty


/**
 * Created by p_dmweidu on 2023/6/28
 * Desc: 代理类，要提供 getValue 和 setValue（对于var类型变量） 方法
 */
class Delegate {

    private var mvalue = ""
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        println("$thisRef, 这里委托了 ${property.name} 属性")
        return mvalue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("$thisRef 的 ${property.name} 属性赋值为 $value")
        mvalue = value
    }
}

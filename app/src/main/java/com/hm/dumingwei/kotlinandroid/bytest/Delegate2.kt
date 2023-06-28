package com.hm.dumingwei.kotlinandroid.bytest

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * Created by p_dmweidu on 2023/6/28
 * Desc: 代理类，要提供 getValue 和 setValue（对于var类型变量） 方法
 */
class Delegate2 : ReadWriteProperty<Example, String> {

    private var mValue: String = "默认值"

    override fun getValue(thisRef: Example, property: KProperty<*>): String {
        println("$thisRef, 这里委托了 ${property.name} 属性")
        return mValue
    }

    override fun setValue(thisRef: Example, property: KProperty<*>, value: String) {
        println("$thisRef 的 ${property.name} 属性赋值为 $value")
        mValue = value
    }

}

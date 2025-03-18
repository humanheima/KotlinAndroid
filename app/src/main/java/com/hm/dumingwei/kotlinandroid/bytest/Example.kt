package com.hm.dumingwei.kotlinandroid.bytest

import kotlin.properties.Delegates

/**
 * Created by p_dmweidu on 2023/6/28
 * Desc: 测试，被委托的类
 */
class Example {

    var p: String by Delegate()
    //var p2: String by Delegate2()

}

class Person {
    var name: String by Delegates.notNull<String>()

    fun initName(name: String) {
        this.name = name // 初始化
    }
}
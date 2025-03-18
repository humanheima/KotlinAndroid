package com.hm.dumingwei.kotlinandroid.bytest

/**
 * Created by p_dmweidu on 2023/6/28
 * Desc: 测试属性委托
 */

fun main() {
    val e = Example()
    println(e.p)
    e.p = "Runoob"
    println(e.p)

    println("-----------------")
}

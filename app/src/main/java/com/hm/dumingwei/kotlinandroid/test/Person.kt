package com.hm.dumingwei.kotlinandroid.test

/**
 * Created by p_dmweidu on 2024/8/22
 * Desc: 伴生对象的例子
 */
interface Person {

    fun callMe()

    //省略了伴生对象的名称
    companion object : Person {
        override fun callMe() = println("I'm called.")
    }
}

fun main(args: Array<String>) {
    Person.callMe()
}
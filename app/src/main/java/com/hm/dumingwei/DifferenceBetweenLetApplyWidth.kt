package base

import java.util.*


/**
 * Crete by dumingwei on 2020-02-20
 * Desc: Kotlin之let,apply,run,with等函数区别
 *
 */
fun main() {
    //testRepeat()
    testLet()
    //testApply()
    //testRun()
    //testAlso()
    //takeIf()
    //takeUnless()
    //testWith()

}

fun testRepeat() {
    repeat(3) {
        println("Hello world")
    }
}

fun testLet() {
    "Hello".let {
        //注释1处
        println(it.length)
    }
}

/**
 * 这个run是扩展函数
 */
fun testRun(): Unit {
    "testRun".run {
        println(length)
    }
}

/**
 * 另一个run，直接执行block，然后返回执行结果
 */
fun testAnotherRun(): Unit {
    val date = run {
        Date()
    }
    println("date = $date")
}

fun testApply() {
    val arrayList: ArrayList<String> = arrayListOf()
    arrayList.apply {
        add("Hello world")
        add("Hello world")
        add("Hello world")
    }.let {
        println(it)
    }
}

/**
 * 执行block，返回this
 */
fun testAlso() {

    val date = Date()
    val alsoDate = date.also {
        println("in also time = ${it.time}")
    }
    println(date)
    println(alsoDate)

}

/**
 * 满足block中条件，则返回当前值，否则返回null，block的返回值Boolean类型
 */
fun takeIf() {

    val date = Date().takeIf {
        //it.after(Date(System.currentTimeMillis() - 20 * 1000))
        it.after(Date(System.currentTimeMillis()))
    }

    println(date)

}

/**
 *
 */
fun takeUnless() {
    val date = Date().takeUnless {
        it.after(Date(System.currentTimeMillis() - 20 * 1000))
        //it.after(Date(System.currentTimeMillis()))
    }

    println(date)

}

fun testWith() {
    val arrayList = arrayListOf<String>()
    with(arrayList) {
        add("Hello world")
        add("Hello world")
        add("Hello world")

    }
    println(arrayList)
}
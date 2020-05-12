package com.hm.dumingwei.extension

/*
fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1] // “this”对应该列表
    this[index1] = this[index2]
    this[index2] = tmp
}

val <T> List<T>.lastIndex: Int
    get() = size - 1
*/

/*
class MyClass {
    companion object {}  // 将被称为 "Companion"
}

fun MyClass.Companion.printCompanion() {
    println("companion")
}

fun main(args: Array<String>) {
    MyClass.printCompanion()
}*/

class Host(val hostname: String) {
    fun printHostname() {
        print(hostname)
    }


}

class Connection(val host: Host, val port: Int) {
    fun printPort() {
        print(port)
    }

    fun Host.printConnectionString() {
        printHostname()   // 调用 Host.printHostname()
        print(":")
        printPort()   // 调用 Connection.printPort()
    }

    fun connect() {
        host.printConnectionString()   // 调用扩展函数
        host.getConnectionString()
    }

    fun Host.getConnectionString() {
        toString()         // 调用 Host.toString()
        this@Connection.toString()  // 调用 Connection.toString()
    }
}

fun main(args: Array<String>) {
    Connection(Host("kotlin"), 443).connect()
    //Host("kotl.in").printConnectionString(443)  // 错误，该扩展函数在 Connection 外不可用
}
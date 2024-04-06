package com.hm.dumingwei.temp


class Test {
    fun test() {
        val anonymousFunction = fun(x: Int, y: Int): Int {
            return x + y
        }
        println(anonymousFunction(1, 2))
    }

}


fun operateOnNumbers(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}


fun main() {
    val myExtension: String.(number: Int, number1: Int) -> Unit = { number, number1 ->
        println(this)
        println(number)
        println(number1)
    }
    //runExtension("Hello, World!", myExtension)
}

fun runExtension(receiver: String, block: String.(number: Int, number1: Int) -> Unit) {
    receiver.block(2, 3)
}


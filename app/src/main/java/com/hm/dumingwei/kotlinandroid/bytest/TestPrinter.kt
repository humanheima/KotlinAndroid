package com.hm.dumingwei.kotlinandroid.bytest

interface Printer {
    fun printMessage()
}

class ConsolePrinter : Printer {
    override fun printMessage() {
        println("控制台打印")
    }
}

class FilePrinter : Printer {
    override fun printMessage() {
        println("文件打印")
    }
}

class PrinterManager(printer: Printer) : Printer by printer

fun main() {
    val consolePrinter = ConsolePrinter()
    val filePrinter = FilePrinter()

    // 动态选择委托对象
    var manager = PrinterManager(consolePrinter)
    manager.printMessage() // 输出: 控制台打印

    manager = PrinterManager(filePrinter)
    manager.printMessage() // 输出: 文件打印
}
### let  和 run 的区别

* let 是一个扩展函数，调用指定的block，并把接收者作为 **block 的参数** ，返回block的执行结果。
```kotlin
/**
 * Calls the specified function [block] with `this` value as its argument and returns its result.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#let).
 */
@kotlin.internal.InlineOnly
public inline fun <T, R> T.let(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block(this)
}

fun testLet() {
    "Hello".let {
        //注释1处，
        println(it.length)
    }
}
```

* run 调用指定的block，并把 this 作为 **block 的接受者**，返回block的执行结果。这里传入的block，相当于接受者执行一段逻辑。

```kotlin
/**
 * Calls the specified function [block] with `this` value as its receiver and returns its result.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#run).
 */
@kotlin.internal.InlineOnly
public inline fun <T, R> T.run(block: T.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

fun testRun(): Unit {
    "testRun".run {
        //使用，这里直接输出字符串的长度
        println(length)
    }
}
```

上面 let 和 run 的区别在于调用者想干吗？如果想以调用者作为参数执行一段逻辑，那么使用let，如果想以调用者执行一段逻辑，那么使用run。


* 另一个run，直接执行block，然后返回执行结果

```kotlin
/**
 * Calls the specified function [block] and returns its result.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#run).
 */
@kotlin.internal.InlineOnly
public inline fun <R> run(block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

fun testAnotherRun(): Unit {
    val date = run {
        Date()
    }
    println("date = $date")
}
```

### 匿名函数

普通函数，fun 关键字后面有函数名 addNumber ，如下所示：

```kotlin
fun addNumber(x: Int, y: Int): Int {
    return x + y
}
```

在 Kotlin 中，匿名函数是一种没有名称的函数，可以作为表达式使用。匿名函数的实现原理主要基于 Java 的匿名内部类。

匿名函数的基本语法如下：为什么叫匿名函数呢？因为 fun 关键字后面没有函数名。


* 匿名函数赋值给变量

```kotlin

val anonymousFunction = fun(x: Int, y: Int): Int {
    return x + y
}

println(anonymousFunction(1, 2)) // 输出 3
```

在这个例子中，anonymousFunction 是一个匿名函数，它接受两个 Int 参数并返回它们的和。


* 匿名函数作为参数传递

```kotlin
fun calculate(x: Int, y: Int, operation: (Int, Int) -> Int): Int {
    return operation(x, y)
}

//调用
fun main() {
    val result = operateOnNumbers(4, 2, fun(a: Int, b: Int): Int {
        return a - b
    })
    println(result)  // 输出: 2
}

```

在这个例子中，calculate 函数接受两个 Int 参数和一个函数参数 operation，operation 是一个接受两个 Int 参数并返回 Int 的函数。
在调用 calculate 函数时，我们传递了一个匿名函数作为 operation 参数，这个匿名函数接受两个 Int 参数并返回它们的差。


> 在 Kotlin 中，匿名函数和普通函数的主要区别在于，匿名函数没有名称，不能单独存在，必须赋值给变量或者作为参数传递。 
> 在编译时，Kotlin 编译器会将匿名函数转换为实现了相应函数接口的匿名内部类的实例。例如，如果一个匿名函数被赋值给一个实现了 Function2 接口的变量，
> 那么这个匿名函数就会被编译器转换为一个实现了 Function2 接口的匿名内部类的实例。 
> 匿名函数的实现原理使得它们可以像普通对象一样被传递和操作，这使得 Kotlin 在处理函数式编程模式时更加灵活和强大。



### 扩展函数 & 匿名扩展函数

看 [扩展.md](%E6%89%A9%E5%B1%95.md) 

```kotlin

### by lazy 

### 函数接受者






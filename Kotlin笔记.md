### let  和 run 的区别

```kotlin
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

```

注释1处，this是以参数的形式传入到block中的。调用this的方法，需要用`it.`前缀。

注释2处，this是block的接受者，可以直接调用this的方法。


```kotlin
/**
 * 另一个run，直接执行block，然后返回执行结果
 */
fun testAnotherRun(): Unit {
    val date = run {
        Date()
    }
    println("date = $date")
}
```

### let 和 run 的功能给是不是重复了？

### 匿名扩展函数







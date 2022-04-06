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



### let 和 run 的功能给是不是重复了？





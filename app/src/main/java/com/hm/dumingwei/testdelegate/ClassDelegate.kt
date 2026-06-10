package com.hm.dumingwei.testdelegate

import android.util.Log

/**
 * Created by p_dmweidu on 2026/6/10
 * Desc: 类委托（Class Delegation）示例
 *
 * 类委托的核心：一个类的接口实现可以委托给另一个对象来完成，
 * 通过 by 关键字，编译器会自动把接口中的方法转发给被委托对象。
 * 这是组合优于继承的一种语言级支持，避免了大量样板转发代码。
 */

private const val TAG = "ClassDelegate"

/**
 * 定义一个基础接口
 */
interface Repository {
    fun save(data: String)
    fun load(): String
}

/**
 * 基础实现类，真正干活的对象
 */
class DataBaseRepository : Repository {
    private var cache: String = ""

    override fun save(data: String) {
        Log.d(TAG, "DataBaseRepository save: $data")
        cache = data
    }

    override fun load(): String {
        Log.d(TAG, "DataBaseRepository load: $cache")
        return cache
    }
}

/**
 * 类委托：RepositoryWrapper 通过 by 把 Repository 接口的实现委托给传入的 repository 对象。
 * 这样无需手动重写 save / load 转发方法，编译器自动生成转发代码。
 * 同时还可以选择性地重写某些方法来增强功能（这里重写了 save 加上日志埋点）。
 */
class RepositoryWrapper(
    private val repository: Repository
) : Repository by repository {

    // 选择性增强：重写 save，添加额外逻辑后再委托给被代理对象
    override fun save(data: String) {
        Log.d(TAG, "RepositoryWrapper 增强逻辑：记录埋点，准备保存 $data")
        repository.save(data)
    }

    // load 没有重写，会自动委托给 repository.load()
}

/**
 * 演示入口
 */
fun testClassDelegate() {
    val real = DataBaseRepository()
    val wrapper = RepositoryWrapper(real)

    wrapper.save("Hello Kotlin Delegation")
    val result = wrapper.load()
    Log.d(TAG, "testClassDelegate 最终读取到: $result")
}

package com.hm.dumingwei.testdelegate

import android.util.Log
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by p_dmweidu on 2026/6/10
 * Desc: 自定义属性委托（Custom Property Delegation）示例
 *
 * 属性委托的核心：把属性的 get/set 逻辑交给一个委托对象处理。
 * - val 属性：委托对象需要提供 getValue
 * - var 属性：委托对象需要提供 getValue 和 setValue
 * 可以手动实现 operator 方法，也可以实现 ReadOnlyProperty / ReadWriteProperty 接口。
 */

private const val TAG = "CustomPropertyDelegate"

/**
 * 方式一：手动实现 operator 方法的可读写委托。
 * 这里做一个带非空校验和日志的委托：赋值为空白时忽略并保留旧值。
 */
class NotBlankDelegate(private var value: String = "") {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        Log.d(TAG, "getValue: 读取 ${property.name} = $value")
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: String) {
        if (newValue.isBlank()) {
            Log.d(TAG, "setValue: ${property.name} 新值为空白，忽略本次赋值，保留 $value")
            return
        }
        Log.d(TAG, "setValue: ${property.name} 由 $value 改为 $newValue")
        value = newValue
    }
}

/**
 * 方式一·补充：用实现 ReadWriteProperty 接口的方式，实现和 NotBlankDelegate 一样的非空白校验逻辑。
 * 和手动写 operator 方法相比，实现接口的好处是有类型约束、IDE 提示更友好，
 * 也能借助 Kotlin 标准库提供的工厂方法（如 Delegates.observable）做组合。
 */
class NotBlankRWDelegate(
    private var value: String = ""
) : ReadWriteProperty<Any?, String> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        Log.d(TAG, "NotBlankRWDelegate getValue: 读取 ${property.name} = $value")
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        if (value.isBlank()) {
            Log.d(TAG, "NotBlankRWDelegate setValue: ${property.name} 新值为空白，忽略本次赋值，保留 ${this.value}")
            return
        }
        Log.d(TAG, "NotBlankRWDelegate setValue: ${property.name} 由 ${this.value} 改为 $value")
        this.value = value
    }
}

/**
 * 方式二：实现 ReadWriteProperty 接口的范型可读写委托。
 * 每次赋值时记录变更次数，演示委托可以持有自己的状态。
 */
class ObservableDelegate<T>(private var value: T) : ReadWriteProperty<Any?, T> {

    private var changeCount = 0

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        changeCount++
        Log.d(TAG, "ObservableDelegate: ${property.name} 第 $changeCount 次变更，${this.value} -> $value")
        this.value = value
    }
}

/**
 * 方式三：实现 ReadOnlyProperty 接口的只读委托。
 * 第一次读取时计算，之后缓存结果（类似一个可观察 property.name 的 lazy）。
 */
class CachedDelegate<T>(private val initializer: () -> T) : ReadOnlyProperty<Any?, T> {

    private var cached: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val current = cached
        if (current != null) {
            Log.d(TAG, "CachedDelegate: ${property.name} 命中缓存")
            return current
        }
        Log.d(TAG, "CachedDelegate: ${property.name} 首次计算并缓存")
        return initializer().also { cached = it }
    }
}

/**
 * 使用上面三种委托的示例类
 */
class UserProfile {
    // 方式一：空白校验委托（手动 operator 实现）
    var nickName: String by NotBlankDelegate("游客")

    // 方式一·补充：空白校验委托（实现 ReadWriteProperty 接口实现）
    var realName: String by NotBlankRWDelegate("匿名")

    // 方式二：可观察变更次数委托
    var score: Int by ObservableDelegate(0)

    // 方式三：只读缓存委托
    val token: String by CachedDelegate { "token-" + (1000..9999).random() }
}

/**
 * 演示入口
 */
fun testCustomPropertyDelegate() {
    val profile = UserProfile()

    // NotBlankDelegate：空白赋值被忽略
    profile.nickName = "小明"
    profile.nickName = "   "
    Log.d(TAG, "最终 nickName = ${profile.nickName}")

    // NotBlankRWDelegate：实现 ReadWriteProperty 接口，效果同上
    profile.realName = "张三"
    profile.realName = ""
    Log.d(TAG, "最终 realName = ${profile.realName}")

    // ObservableDelegate：每次赋值都计数
    profile.score = 10
    profile.score = 20
    Log.d(TAG, "最终 score = ${profile.score}")

    // CachedDelegate：多次读取只计算一次
    Log.d(TAG, "第一次 token = ${profile.token}")
    Log.d(TAG, "第二次 token = ${profile.token}")
}

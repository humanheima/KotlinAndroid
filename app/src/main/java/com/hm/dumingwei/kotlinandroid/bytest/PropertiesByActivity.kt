package com.hm.dumingwei.kotlinandroid.bytest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hm.dumingwei.kotlinandroid.R
import com.hm.dumingwei.kotlinandroid.databinding.ActivityPropertiesByBinding
import kotlin.properties.Delegates

/**
 * Created by p_dmweidu on 2023/6/28
 * Desc: 测试属性委托
 */
class PropertiesByActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "PropertiesByActivity"

        fun launch(context: Context) {
            val starter = Intent(context, PropertiesByActivity::class.java)
            context.startActivity(starter)
        }
    }

    /**
     * 属性委托
     * by 关键字之后的表达式就是委托, 属性的 get() 方法(以及set() 方法)
     * 将被委托给这个对象的 getValue() 和 setValue() 方法。
     * 属性委托不必实现任何接口, 但必须提供 getValue() 函数(对于 var属性,还需要 setValue() 函数)。
     */
    private var name: String by UtilSharedPreference("name", "默认值")

    private val settings by lazy { UserSettings(this) }

    private lateinit var binding: ActivityPropertiesByBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPropertiesByBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //初始化sp
        UtilSharedPreference.initSharedPreference(this, "sp_file")

        testNotNullDelegate()

        binding.btnSpDelegate3.setOnClickListener {

            // 写入 SharedPreferences
            settings.userName = "Alice"
            settings.userAge = 25
            settings.isDarkMode = true

            // 读取 SharedPreferences
            Log.d(TAG, "onCreate: User Name: ${settings.userName}") // 输出: Alice
            Log.d(TAG, "onCreate: User Age: ${settings.userAge}")
            Log.d(TAG, "onCreate: Dark Mode: ${settings.isDarkMode}")

            // 尝试写入空值会编译错误
            // settings.userName = null // 编译错误：Null can not be a value of a non-null type String

        }


    }

    //属性委托：属性变化时触发回调。
    var observable: String by Delegates.observable("initial") { prop, old, new ->
        Log.d(TAG, "onCreate: ${prop.name} changed from $old to $new")
    }

    /**
     * 非空委托
     */
    fun testNotNullDelegate() {
        val person = Person()

        // println(person.name) // 错误！未初始化会抛出 IllegalStateException

        person.initName("Alice")
        println(person.name) // 输出: Alice

        person.name = "Bob"
        println(person.name) // 输出: Bob

        //person.name = null // 编译错误，类型是 String 而非 String?
    }

    /**
     * 可以阻止不符合条件的赋值。新值大于0，才会被赋值。
     */
    var vetoable: Int by Delegates.vetoable(0) { _, _, new -> new >= 0 }


    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_delegate_sample -> {
                val example = Example()

                observable = "你好"
                Log.d(TAG, "onClick: example.name = ${example.p}")

            }

            R.id.btn_sp_delegate -> {
                var newName = name
                Log.i(TAG, "onClick: newName= $newName")
                name = "我是新的值"
                newName = name
                Log.i(TAG, "onClick: newName= $newName")
            }

        }

    }
}
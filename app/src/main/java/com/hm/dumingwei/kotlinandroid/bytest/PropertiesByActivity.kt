package com.hm.dumingwei.kotlinandroid.bytest

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.hm.dumingwei.kotlinandroid.R

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

    private var user: SharedPreferencesUtils.User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_properties_by)
        //初始化sp
        UtilSharedPreference.initSharedPreference(this, "sp_file")

        user = SharedPreferencesUtils.User(this)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_sp_delegate -> {
                var newName = name
                Log.i(TAG, "onClick: newName= $newName")
                name = "我是新的值"
                newName = name
                Log.i(TAG, "onClick: newName= $newName")
            }

            R.id.btn_sp_delegate2 -> {
                user?.name = "你好"
                user?.phone = 124

                Log.i(TAG, "onClick: user?.name =${user?.name} user?.phone = ${user?.phone}")

            }
        }

    }
}
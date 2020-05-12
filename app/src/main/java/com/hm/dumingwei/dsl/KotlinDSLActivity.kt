package com.hm.dumingwei.dsl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.hm.dumingwei.kotlinandroid.R
import com.hm.dumingwei.kotlinandroid.swap
import kotlinx.android.synthetic.main.activity_kotlin_dsl.*

/**
 * Created by dumingwei on 2020/5/12
 *
 * Desc: Kotlin 领域特定语言
 */
class KotlinDSLActivity : AppCompatActivity() {

    private val TAG: String? = "KotlinDSLActivity"

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, KotlinDSLActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_dsl)

        etFirst.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        etSecond.onTextChange {
            afterTextChanged {
                Log.d(TAG, "onCreate: $it")
            }
        }


        val list = mutableListOf(1, 2, 3, 4)
        list.swap(0, 2)

    }
}

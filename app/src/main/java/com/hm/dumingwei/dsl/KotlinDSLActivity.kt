package com.hm.dumingwei.dsl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hm.dumingwei.kotlinandroid.databinding.ActivityKotlinDslBinding

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

    private lateinit var binding: ActivityKotlinDslBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKotlinDslBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.etFirst.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.etSecond.onTextChange {
            afterTextChanged {
                Log.d(TAG, "onCreate: $it")
            }
        }

        val list = mutableListOf(1, 2, 3, 4)

    }
}

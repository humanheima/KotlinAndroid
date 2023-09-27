package com.hm.dumingwei.kotlinandroid.handbook.eleven

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.hm.dumingwei.kotlinandroid.R

class SecondActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    private val name: String? by extraDelegate("name")
    private val age: Int? by extraDelegate("age")

    companion object {
        fun launch(context: Context, name: String, age: Int) {
            val intent = Intent(context, SecondActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("age", age)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        Log.d(TAG, "onCreate: $name")
        Log.d(TAG, "onCreate: $age")
    }
}

package com.hm.dumingwei.kotlinandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.hm.dumingwei.JsonUtilKt

/**
 * Created by dumingwei on 2021/10/28
 *
 * Desc:测试Kotlin类使用Gson序列化和反序列化
 */
class GsonTestActivity : AppCompatActivity(), View.OnClickListener {


    private val TAG: String = "GsonTestActivity"


    private lateinit var tvResult: TextView
    private lateinit var btnTest1: Button
    private lateinit var btnTest2: Button
    private lateinit var btnTest3: Button
    private lateinit var btnTest4: Button


    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, GsonTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gson_test)

        tvResult = findViewById(R.id.tvResult)

        btnTest1 = findViewById(R.id.btnTest1)
        btnTest2 = findViewById(R.id.btnTest2)
        btnTest3 = findViewById(R.id.btnTest3)
        btnTest4 = findViewById(R.id.btnTest4)

        btnTest1.setOnClickListener(this)
        btnTest2.setOnClickListener(this)
        btnTest3.setOnClickListener(this)
        btnTest4.setOnClickListener(this)

        //val jsonString = "{\"showable\": \"true\"," + "\"moreThan\": \"30.01%\"," + "\"totalCoinOfYesterday\": 1903," + "\"extraCoin\": 300}"
        //val jsonString = "{\"showable\": \"true\"," + "\"totalCoinOfYesterday\": 1903," + "\"extraCoin\": 300}"
        //val jsonString = "{\"showable\": \"true\"," + "\"moreThan\": \"null\"," + "\"totalCoinOfYesterday\": 1903," + "\"extraCoin\": 300}"
        //val jsonString = "{\"showable\": \"true\"," + "\"moreThan\": \"30.01%\"," + "\"extraCoin\": 300}"
        val jsonString = "{\"show\": \"true\"," + "\"number\": \"10086\"," + "\"string\":\"hello world\" }"

        Log.i(TAG, "onCreate: json = ${jsonString.trim()}")

        val info = JsonUtilKt.instance.toObject(jsonString, JsonModel::class.java)

        Log.i(TAG, "onCreate: info = $info")
    }

    override fun onClick(v: View) {
        when (v.id) {


            R.id.btnTest1 -> {
                //字段都有
                val jsonString = "{\"show\": \"true\"," + "\"number\": 10086," + "\"string\":\"hello world\" }"

                val model = JsonUtilKt.instance.toObject(jsonString, JsonModel::class.java)

                Log.i(TAG, "onClick: mode = $model")


            }
            R.id.btnTest2 -> {

                //缺少一个boolean类型变量
                val jsonString = "{\"number\": 10086," + "\"string\":\"hello world\" }"

                val model = JsonUtilKt.instance.toObject(jsonString, JsonModel::class.java)

                Log.i(TAG, "onClick: 缺少一个boolean类型变量：mode = $model")


                //boolean类型变量为null
                val jsonString1 = "{\"show\": null," + "\"number\": 10086," + "\"string\":\"hello world\" }"

                val model1 = JsonUtilKt.instance.toObject(jsonString1, JsonModel::class.java)

                Log.i(TAG, "onClick: boolean类型变量为null：mode = $model1")


            }
            R.id.btnTest3 -> {

                //缺少一个int类型变量
                val jsonString = "{\"show\": \"true\"," + "\"string\":\"hello world\" }"

                val model = JsonUtilKt.instance.toObject(jsonString, JsonModel::class.java)

                Log.i(TAG, "onClick: 缺少一个int类型变量：mode = $model")

                //int类型变量为null
                val jsonString1 = "{\"show\": \"true\"," + "\"number\": null," + "\"string\":\"hello world\" }"

                val model1 = JsonUtilKt.instance.toObject(jsonString1, JsonModel::class.java)

                Log.i(TAG, "onClick: int类型变量为null：mode = $model1")


            }
            R.id.btnTest4 -> {

                //缺少一个引用类型变量
                val jsonString = "{\"show\": \"true\"," + "\"number\": 10086}"

                val model = JsonUtilKt.instance.toObject(jsonString, JsonModel::class.java)

                Log.i(TAG, "onClick: 缺少一个引用类型变量：mode = $model")


                //引用类型变量为null
                val jsonString1 = "{\"show\": \"true\"," + "\"number\": \"10086\"," + "\"string\":null }"

                val model1 = JsonUtilKt.instance.toObject(jsonString1, JsonModel::class.java)

                Log.i(TAG, "onClick: 引用类型变量为null：mode = $model1")


            }
        }
    }
}


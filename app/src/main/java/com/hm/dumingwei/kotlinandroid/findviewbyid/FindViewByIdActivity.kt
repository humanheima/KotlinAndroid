package com.hm.dumingwei.kotlinandroid.findviewbyid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.dumingwei.kotlinandroid.R

/**
 * Crete by dumingwei on 2020-03-09
 * Desc: kotlin实现findViewById实现原理
 *
 */
class FindViewByIdActivity : AppCompatActivity() {


    private var rv: RecyclerView? = null

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, FindViewByIdActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_view_by_id)

        rv = findViewById(R.id.rv)
        /*btn.setOnClickListener {
            // do nothing
            val fragment = FindViewByIdFragment.newInstance()
            supportFragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit()
        }
*/
        initAdapter()
    }

    private fun initAdapter() {
        val data = arrayListOf<String>()
        for (i in 0..10) {
            data.add("Hello world$i")
        }

        val adapter = MyAdapter(this, data)
        rv?.layoutManager = LinearLayoutManager(this)
        rv?.adapter = adapter
    }
}

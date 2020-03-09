package com.hm.dumingwei.kotlinandroid.findviewbyid

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.hm.dumingwei.kotlinandroid.R
import kotlinx.android.synthetic.main.activity_find_view_by_id.*
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Crete by dumingwei on 2020-03-09
 * Desc: kotlin实现findViewById实现原理
 *
 */
class FindViewByIdActivity : AppCompatActivity() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, FindViewByIdActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_view_by_id)

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
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }
}

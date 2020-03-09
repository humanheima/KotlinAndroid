package com.hm.dumingwei.kotlinandroid.findviewbyid

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.hm.dumingwei.kotlinandroid.R
import kotlinx.android.synthetic.main.item_recyclerview.view.*

class MyAdapter(
        val context: Context,
        val data: List<String>
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recyclerview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //原来的使用方式
        holder.itemView.tvInRv.text = data[position]
        //启用LayoutContainer扩展功能后的使用方式
        //holder.tvInRv.text = data[position]
        holder.itemView.setOnClickListener {
            Toast.makeText(context, "点击了第$position 项", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    /* class ViewHolder(view: View) : RecyclerView.ViewHolder(view), LayoutContainer {

         override val containerView: View = itemView
     }*/
}

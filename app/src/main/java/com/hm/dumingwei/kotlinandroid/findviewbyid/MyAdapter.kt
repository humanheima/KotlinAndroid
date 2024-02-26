package com.hm.dumingwei.kotlinandroid.findviewbyid

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.hm.dumingwei.kotlinandroid.R
import com.hm.dumingwei.kotlinandroid.databinding.ItemRecyclerviewBinding

class MyAdapter(
    val context: Context,
    val data: List<String>
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecyclerviewBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //原来的使用方式
        holder.tvInRv.text = data[position]
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
        val tvInRv: TextView = view.findViewById<TextView>(R.id.tvInRv)

    }

    /* class ViewHolder(view: View) : RecyclerView.ViewHolder(view), LayoutContainer {

         override val containerView: View = itemView
     }*/
}

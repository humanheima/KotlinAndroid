package com.hm.dumingwei.kotlinandroid.findviewbyid

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.hm.dumingwei.kotlinandroid.databinding.CustomLinearLayoutBinding

/**
 * Created by dumingwei on 2020-03-09.
 * Desc:
 */
class MyLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    private val binding: CustomLinearLayoutBinding

    init {

        binding = CustomLinearLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        binding.tvInCustomLayout.text = "Hello world"

    }
}
package com.hm.dumingwei.kotlinandroid.findviewbyid

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.hm.dumingwei.kotlinandroid.R
import kotlinx.android.synthetic.main.custom_linear_layout.view.*

/**
 * Created by dumingwei on 2020-03-09.
 * Desc:
 */
class MyLinearLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {

        View.inflate(context, R.layout.custom_linear_layout, this)
        tvInCustomLayout.text = "Hello world"

    }
}
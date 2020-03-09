package com.hm.dumingwei.kotlinandroid.findviewbyid


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.hm.dumingwei.kotlinandroid.R
import kotlinx.android.synthetic.main.fragment_find_view_by_id.*

/**
 * Crete by dumingwei on 2020-03-09
 * Desc:
 *
 */
class FindViewByIdFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        //tvInFragment.text = "fragment中的字符串"
        return inflater.inflate(R.layout.fragment_find_view_by_id, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvInFragment.text = "fragment中的字符串"
    }

    companion object {

        @JvmStatic
        fun newInstance() = FindViewByIdFragment()
    }
}

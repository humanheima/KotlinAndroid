package com.hm.dumingwei.kotlinandroid.findviewbyid


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hm.dumingwei.kotlinandroid.databinding.FragmentFindViewByIdBinding

/**
 * Crete by dumingwei on 2020-03-09
 * Desc:
 *
 */
class FindViewByIdFragment : Fragment() {


    companion object {

        @JvmStatic
        fun newInstance() = FindViewByIdFragment()
    }

    private lateinit var binding: FragmentFindViewByIdBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFindViewByIdBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
        // Inflate the layout for this fragment

        //tvInFragment.text = "fragment中的字符串"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvInFragment.text = "fragment中的字符串"
    }

}

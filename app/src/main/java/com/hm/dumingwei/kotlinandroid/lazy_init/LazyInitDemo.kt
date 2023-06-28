package com.hm.dumingwei.kotlinandroid.lazy_init

/**
 * Created by dumingwei on 2020-03-14.
 * Desc:
 */
class LazyInitDemo {

    val myName: String by lazy { "John" }

    private fun getNameFromPreference(): () -> String = { "John" }

}
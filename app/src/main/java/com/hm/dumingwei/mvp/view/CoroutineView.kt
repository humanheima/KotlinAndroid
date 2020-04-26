package com.hm.dumingwei.mvp.view

/**
 * Created by dumingwei on 2020/4/24.
 *
 * Desc:
 */
interface CoroutineView : MvpView {

    override fun canUpdateUI(): Boolean

    fun setResult(text: String)



}
package com.hm.dumingwei.mvp.view

interface MvpView {

    /**
     * @return 在声明周期结束的时候，返回false。
     */
    fun canUpdateUI(): Boolean
}
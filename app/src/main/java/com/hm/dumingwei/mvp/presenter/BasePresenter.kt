package com.hm.dumingwei.mvp.presenter

import com.hm.dumingwei.mvp.view.MvpView

/**
 * Created by dumingwei on 2020/4/24.
 *
 * Desc:
 */
open class BasePresenter<V : MvpView> : MvpPresenter<V> {

    var view: V? = null

    override fun attachView(view: V) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}


package com.hm.dumingwei.mvp.presenter

import androidx.annotation.UiThread
import com.hm.dumingwei.mvp.view.MvpView

interface MvpPresenter<V : MvpView> {

    @UiThread
    fun attachView(view: V)

    @UiThread
    fun detachView()
}
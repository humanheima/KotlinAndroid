package com.hm.dumingwei.mvp.presenter

import android.support.annotation.UiThread
import com.hm.dumingwei.mvp.view.MvpView

interface MvpPresenter<V : MvpView> {

    @UiThread
    fun attachView(view: V)

    @UiThread
    fun detachView()
}
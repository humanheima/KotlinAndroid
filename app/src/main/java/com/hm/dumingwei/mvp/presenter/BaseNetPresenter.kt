package com.hm.dumingwei.mvp.presenter

import com.hm.dumingwei.mvp.view.MvpView
import kotlinx.coroutines.CoroutineScope

/**
 * Created by dumingwei on 2020/4/24.
 *
 * Desc:
 */
open class BaseNetPresenter<V : MvpView>(var scope: CoroutineScope?) : BasePresenter<V>() {

}
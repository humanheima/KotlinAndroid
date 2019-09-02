package com.hm.dumingwei.kotlinandroid.handbook.fifteen

import android.accounts.NetworkErrorException
import android.content.Context
import android.util.Log
import com.hm.dumingwei.kotlinandroid.App
import com.hm.dumingwei.kotlinandroid.R
import io.reactivex.observers.DisposableMaybeObserver
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Created by dmw on 2019/1/8.
 * Desc:
 */
abstract class BaseMaybeObserver<T> : DisposableMaybeObserver<T>() {

    private val TAG = javaClass.simpleName

    internal var mAppContext: Context

    init {
        mAppContext = App.instance
    }

    override fun onSuccess(data: T) {
        onMaybeSuccess(data)
    }

    abstract fun onMaybeSuccess(data: T)

    open fun onMaybeError(msg: String?) {

        // do nothing
    }

    override fun onError(e: Throwable) {
        var message = e.message
        Log.d(TAG, "onError: ")
        message = when (e) { // 枚举各种网络异常
            is ConnectException -> mAppContext.getString(R.string.connect_exception_error)
            is SocketTimeoutException -> mAppContext.getString(R.string.timeout_error)
            is UnknownHostException -> mAppContext.getString(R.string.network_error)
            is NetworkErrorException -> mAppContext.getString(R.string.network_error)
            else -> mAppContext.getString(R.string.something_went_wrong)
        }
        onMaybeError(message)
    }

    override fun onComplete() {
        // do nothing
    }

}
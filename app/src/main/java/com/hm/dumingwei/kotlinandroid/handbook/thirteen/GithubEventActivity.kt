package com.hm.dumingwei.kotlinandroid.handbook.thirteen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.hm.dumingwei.kotlinandroid.R
import com.hm.dumingwei.kotlinandroid.handbook.fifteen.BaseMaybeObserver
import com.hm.dumingwei.kotlinandroid.handbook.fourteen.RetryWithDelay
import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

@SuppressLint("CheckResult")
class GithubEventActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, GithubEventActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_github_event)
        RetrofitManager.get()
                .apiService()
                .publicEvent("humanheima")
                .retryWhen(RetryWithDelay(3, 1000))
                .compose(RxUtils.toCacheTransformer("key"))//数据加入缓存
                .compose(RxUtils.maybeToMain<List<Event>>())//转换线程
                .subscribe(object : MaybeObserver<List<Event>> {
                    override fun onSuccess(eventList: List<Event>) {
                        Log.d(TAG, "onSuccess: ${eventList[0]}")
                    }

                    override fun onComplete() {
                        //do nothing
                    }

                    override fun onSubscribe(d: Disposable) {
                        //do nothing
                    }

                    override fun onError(e: Throwable) {
                        //do nothing
                    }

                })
        test0()
    }

    /**
     * 关于Retrofit 的异常处理
     */

    /**
     * 1. 必须要在每一个 onError 中都要处理。否则对于网络请求而言，万一出现异常的话，App 就会出现 Crash。
     */
    private fun test0() {
        RetrofitManager.get().apiService().publicEvent("humanheima")
                .compose(RxUtils.maybeToMain<List<Event>>())
                .subscribe(object : Consumer<List<Event>> {
                    override fun accept(t: List<Event>?) {

                    }
                }, object : Consumer<Throwable> {
                    override fun accept(t: Throwable) {

                    }
                })
    }

    /**
     * 封装成基类,BaseMaybeObserver内部根据不同的错误，做出了相应的的处理
     */
    private fun test1() {
        RetrofitManager.get().apiService().publicEvent("humanheima")
                .compose(RxUtils.maybeToMain<List<Event>>())
                .subscribe(object : BaseMaybeObserver<List<Event>>() {
                    override fun onMaybeSuccess(data: List<Event>) {

                    }

                    override fun onMaybeError(msg: String?) {
                        super.onMaybeError(msg)
                    }
                })
    }

    /**
     *结合扩展函数、RxJava的错误处理操作符
     */
    private fun test2() {
        RetrofitManager.get().apiService().publicEvent("humanheima")
                .compose(RxUtils.maybeToMain<List<Event>>())
                .errorReturn(listOf(), action = {
                    Log.d(TAG, "test2: ${it.message}")
                })
                .subscribe(object : Consumer<List<Event>> {
                    override fun accept(t: List<Event>?) {

                    }
                }, object : Consumer<Throwable> {
                    override fun accept(t: Throwable) {

                    }
                })
    }
}

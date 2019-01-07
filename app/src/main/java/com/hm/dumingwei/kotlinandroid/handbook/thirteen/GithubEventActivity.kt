package com.hm.dumingwei.kotlinandroid.handbook.thirteen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.hm.dumingwei.kotlinandroid.R
import com.hm.dumingwei.kotlinandroid.handbook.fourteen.RetryWithDelay
import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable

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
        /* RetrofitManager.get().apiService().publicEvent("humanheima")
                 .compose(RxUtils.maybeToMain<List<Event>>())
                 .subscribe(object : Consumer<List<Event>>{
                     override fun accept(t: List<Event>?) {
                         TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                     }
                 })*/
        RetrofitManager.get()
                .apiService()
                .publicEvent("humanheima")
                .retryWhen(RetryWithDelay(3, 1000))
                .compose(RxUtils.maybeToMain<List<Event>>())
                .subscribe(object : MaybeObserver<List<Event>> {
                    override fun onSuccess(eventList: List<Event>) {
                        Log.d(TAG, "onSuccess: ${eventList[0]}")
                    }

                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                    }

                })
    }
}

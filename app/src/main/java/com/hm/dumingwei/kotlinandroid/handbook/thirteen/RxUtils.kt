package com.hm.dumingwei.kotlinandroid.handbook.thirteen

import io.reactivex.MaybeTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by dmw on 2019/1/7.
 * Desc:
 */
class RxUtils {

    companion object {
        /**
         * 1. 使用默认参数
         */
        @JvmOverloads //如果希望也向 Java 调用者暴露多个重载，可以使用 @JvmOverloads 注解
        @JvmStatic //表示该方法为静态方法
        fun <T> maybeToMain(): MaybeTransformer<T, T> {
            return MaybeTransformer { upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
        }

    }
}
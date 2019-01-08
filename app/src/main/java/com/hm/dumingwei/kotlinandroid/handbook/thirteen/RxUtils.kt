package com.hm.dumingwei.kotlinandroid.handbook.thirteen

import io.reactivex.Maybe
import io.reactivex.MaybeTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
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

        /**
         * 加入缓存
         */
        fun <T> toCacheTransformer(key: String): MaybeTransformer<T, T> {
            return MaybeTransformer { upstream ->
                upstream.map { t ->
                    //加入到缓存
                    //CacheManager.getInstance().put(key, t as Serializable)
                    t
                }
            }
        }
    }
}

fun <T> Maybe<T>.errorReturnJava(defValue: T): Maybe<T> = this.onErrorReturn(object : Function<Throwable, T> {
    override fun apply(t: Throwable): T {
        t.printStackTrace()
        return defValue
    }
})

/**
 * 遇到错误时，能够提前捕获异常，并发射一个默认的值。
 * 后面无须再做异常处理
 */
fun <T> Maybe<T>.errorReturn(defValue: T): Maybe<T> = this.onErrorReturn { t ->
    t.printStackTrace()
    return@onErrorReturn defValue
}

fun <T> Maybe<T>.errorReturn(defValue: T, action: (Throwable) -> Unit): Maybe<T> = this.onErrorReturn {
    action.invoke(it)
    return@onErrorReturn defValue
}

/**
 * 遇到错误时，能够提前捕获异常，并返回一个新的Maybe
 * 后面无须再做异常处理
 */

fun <T> Maybe<T>.errorResumeNext(defValue: T): Maybe<T> = this.onErrorResumeNext(Maybe.just(defValue))

fun <T> Maybe<T>.errorResumeNext(): Maybe<T> = this.onErrorResumeNext(Maybe.empty())

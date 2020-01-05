package com.hm.dumingwei.kotlinandroid

import android.app.Activity
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Toast的封装
 */
fun Toast.setGravityCenter(): Toast {
    setGravity(Gravity.CENTER, 0, 0)
    return this
}

fun Activity.toastSHortly(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.toastSHortly(messageId: Int) {
    Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show()
}


/**
 * 设置Toast字体及背景颜色
 * @param messageColor
 * @param backgroundColor
 * @return
 */
fun Toast.setToastColor(@ColorInt messageColor: Int, @ColorInt backgroundColor: Int) {

    val view = view
    if (view != null) {
        val message = view.findViewById(android.R.id.message) as TextView
        message.setBackgroundColor(backgroundColor)
        message.setTextColor(messageColor)
    }
}


/**
 * Glide 的封装
 */
fun ImageView.load(url: String?, placeholderRes: Int = R.drawable.ic_launcher_background, errorRes: Int = R.drawable.ic_launcher_background) {
    get(url).placeholder(placeholderRes)
            .error(errorRes)
            .into(this)
}

fun ImageView.loadRound(url: String?, centerCrop: Boolean = false) {
    get(url).placeholder(R.drawable.ic_launcher_background)
            .transform(GlideRoundTransform(12))
            .into(this)
}

fun ImageView.loadCircle(url: String?) {
    get(url).placeholder(R.drawable.ic_launcher_background)
            .apply(RequestOptions.circleCropTransform())
            .into(this)
}

fun ImageView.get(url: String?): GlideRequest<Drawable> = GlideApp.with(context).load(url)
fun ImageView.get(drawable: Drawable?): GlideRequest<Drawable> = GlideApp.with(context).load(drawable)

/***
 * 设置延迟时间的View扩展
 * @param delay Long 延迟时间，默认600毫秒
 * @return T
 */
fun <T : View> T.withTrigger(delay: Long = 600): T {
    Log.d("extentions", "withTrigger")
    triggerDelay = delay
    return this
}

/***
 * 点击事件的View扩展
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.click(block: (T) -> Unit) = setOnClickListener {

    if (clickEnable()) {
        block(it as T)
    }
}

/***
 * 带延迟过滤的点击事件View扩展
 * @param delay Long 延迟时间，默认600毫秒
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.clickWithTrigger(time: Long = 600, block: (T) -> Unit) {
    triggerDelay = time
    setOnClickListener {
        if (clickEnable()) {
            block(it as T)
        }
    }
}

/**
 * 利用了View的tag属性来保存我们的triggerDelay
 */
private var <T : View> T.triggerLastTime: Long
    get() = if (getTag(1123460103) != null) getTag(1123460103) as Long else 0
    set(value) {
        setTag(1123460103, value)
    }

/**
 * 利用了View的tag属性来保存我们的triggerDelay
 */
private var <T : View> T.triggerDelay: Long
    get() = if (getTag(1123461123) != null) getTag(1123461123) as Long else -1
    set(value) {
        setTag(1123461123, value)
    }

private fun <T : View> T.clickEnable(): Boolean {
    var flag = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - triggerLastTime >= triggerDelay) {
        flag = true
    }
    triggerLastTime = currentClickTime
    return flag
}


/**
 * 使用协程，实现防止View重复点击
 */

interface JobHolder {
    val job: Job
}

val View.contextJob: Job
    get() = (context as? JobHolder)?.job ?: NonCancellable

fun View.onClick(time: Long = 600, action: suspend (View) -> Unit) {

    val eventActor = GlobalScope.actor<Unit>(contextJob + Dispatchers.Main) {

        for (event in channel) {
            delay(time)
            action(this@onClick)
        }

    }

    setOnClickListener {
        eventActor.offer(Unit)
    }
}

/**
 * retrofit2.Call的扩展函数
 */
suspend fun <T : Any?> Call<T>.await(): T {

    return suspendCoroutine {
        enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                it.resumeWithException(t)
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        it.resume(body)
                    }
                } else {
                    it.resumeWithException(Throwable(response.toString()))
                }
            }
        })
    }
}

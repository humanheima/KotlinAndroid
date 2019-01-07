package com.hm.dumingwei.kotlinandroid.handbook.fourteen

import io.reactivex.Flowable
import io.reactivex.functions.Function
import org.reactivestreams.Publisher
import java.util.concurrent.TimeUnit

/**
 * Created by dmw on 2019/1/7.
 * Desc:
 */
class RetryWithDelay(private val maxRetries: Int, private val retryDelayMillis: Int) : Function<Flowable<Throwable>, Publisher<*>> {

    private var retryCount: Int = 0

    init {
        this.retryCount = 0
    }

    override fun apply(attempts: Flowable<Throwable>): Publisher<*> {

        return attempts.flatMap(object : Function<Throwable, Publisher<*>> {
            override fun apply(t: Throwable): Publisher<*> {
                if (++retryCount <= maxRetries) {
                    // When this Observable calls onNext, the original
                    // Observable will be retried (i.e. re-subscribed).
                    return Flowable.timer(retryDelayMillis.toLong(), TimeUnit.MILLISECONDS)
                } else {
                    return Flowable.error<Any>(t)
                }
            }
        })
    }
}
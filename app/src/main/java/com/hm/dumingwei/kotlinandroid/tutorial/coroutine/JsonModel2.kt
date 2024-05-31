package com.hm.dumingwei.kotlinandroid.tutorial.coroutine

import com.hm.dumingwei.kotlinandroid.JsonModel


/**
 * Created by p_dmweidu on 2024/5/31
 * Desc:
 */
data class JsonModel2(
    var show: Boolean,
    var number: Int,
    var string: String?,
    var jsonModel: JsonModel
)

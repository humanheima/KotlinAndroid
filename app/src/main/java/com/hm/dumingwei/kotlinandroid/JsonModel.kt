package com.hm.dumingwei.kotlinandroid

/**
 * Created by dumingwei on 2021/10/27
 *
 * Desc:
 */

data class JsonModel(
        var show: Boolean,
        var number: Int,
        var string: String?
)


data class JsonModel2(
        var show: Boolean,
        var number: Int,
        var string: String?,
        var jsonModel: JsonModel
)


//data class JsonModel(
//        var show: Boolean?,
//        var number: Int?,
//        var string: String?
//)
package com.hm.dumingwei.net

class NetResponse<T> {
    var data: T? = null
    var errorMsg = ""
    var errorCode = 0


    fun success() = errorCode == 0
}
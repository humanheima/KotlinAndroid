package com.hm.dumingwei.mvp.model.bean

/**
 * Created by dumingwei on 2020/4/24.
 *
 * Desc:
 */
data class WxArticleResponse(
        var data: List<DataBean>,
        var errorCode: Int = 0,
        var errorMsg: String? = null

) {


    class DataBean {
        /**
         * courseId : 13
         * id : 408
         * name : 鸿洋
         * order : 190000
         * parentChapterId : 407
         * userControlSetTop : false
         * visible : 1
         */
        var courseId = 0
        var id = 0
        var name: String? = null
        var order = 0
        var parentChapterId = 0
        var isUserControlSetTop = false
        var visible = 0

    }

}
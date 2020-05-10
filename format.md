```
private fun coroutineRequest() {
    val request1 = Request.Builder()
            .url("https://wanandroid.com/wxarticle/chapters/json")
            .build()

        launch {
            try {
                //val startTime = System.currentTimeMillis()

                //第一个网络请求
                val response1 = client.newCall(request1).awaitResponse()
                val string1 = getString(response1)
                val wxArticleResponse = JsonUtilKt.instance.toObject(string1, WxArticleResponse::class.java)

                //第二个网络请求依赖于第一个网络请求结果
                val firstWxId = wxArticleResponse?.data?.get(0)?.id ?: return@launch
                val request2 = Request.Builder()
                        .url("https://wanandroid.com/wxarticle/list/${firstWxId}/1/json")
                        .build()
                val response2 = client.newCall(request2).awaitResponse()

                //Log.d(TAG, "coroutineRequest: 网络请求消耗时间：${System.currentTimeMillis() - startTime}")
                val string2 = getString(response2)

                tvResult.text = "协程请求 onResponse: ${string2}"
            } catch (e: Exception) {
                Log.d(TAG, "coroutine: error ${e.message}")
            }
        }
    }




```
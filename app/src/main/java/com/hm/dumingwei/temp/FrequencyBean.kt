package com.hm.dumingwei.temp

/**
 * Created by p_dmweidu on 2024/5/28
 * Desc: 发送频率限制
 */
data class FrequencyBean(
    var chatFrequencyMillisecond: Long = 2000,
    var chatFrequencyTips: String? = "default value"
) {
}
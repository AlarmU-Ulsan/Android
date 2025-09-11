package com.alarmit.uou_alarm_it

data class PostFCMSubscribeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Result
) {
    data class Result(
        val deviceId: String,
        val token: String,
        val subscribeMajors: List<SubscribeMajor>
    ) {
        data class SubscribeMajor(
            val id: Id
        ) {
            data class Id(
                val deviceId: String,
                val major: String
            )
        }
    }
}
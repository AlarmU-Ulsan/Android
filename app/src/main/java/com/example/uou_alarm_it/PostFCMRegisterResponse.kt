package com.example.uou_alarm_it

data class PostFCMTokenResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: FCMResult
)

data class FCMResult(
    val deviceId: String,
    val token: String,
    val subscribeMajors: List<SubscribeMajor>
)

data class SubscribeMajor(
    val id: SubscribeMajorId
)

data class SubscribeMajorId(
    val deviceId: String,
    val major: String
)
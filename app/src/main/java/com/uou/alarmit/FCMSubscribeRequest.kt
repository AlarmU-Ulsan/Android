package com.uou.alarmit

data class FCMSubscribeRequest(
    val deviceId: String,
    val major: String
)
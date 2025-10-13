package com.alarmit.alarm_it

data class FCMSubscribeRequest(
    val deviceId: String,
    val major: String
)
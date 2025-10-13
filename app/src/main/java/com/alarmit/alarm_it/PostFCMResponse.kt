package com.alarmit.alarm_it

data class PostFCMResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)
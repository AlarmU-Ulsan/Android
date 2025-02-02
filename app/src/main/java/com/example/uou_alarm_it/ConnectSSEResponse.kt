package com.example.uou_alarm_it

data class ConnectSSEResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Result
) {
    data class Result(
        val title: String,
        val link: String
    )
}
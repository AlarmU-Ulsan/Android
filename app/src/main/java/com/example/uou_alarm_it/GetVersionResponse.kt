package com.example.uou_alarm_it

data class GetVersionResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Result
) {
    data class Result(
        val id: Int,
        val platform: String,
        val latestVersion: String,
        val minimumVersion: String,
        val link: String
    )
}
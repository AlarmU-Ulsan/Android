package com.uou.alarmit

data class GetVersionResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Result
) {
    data class Result(
        val platform: String,
        val latestVersion: String,
        val minimumVersion: String,
        val link: String
    )
}
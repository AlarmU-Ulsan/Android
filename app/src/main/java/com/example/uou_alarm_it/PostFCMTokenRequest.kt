package com.example.uou_alarm_it

import com.google.gson.annotations.SerializedName

data class PostFCMTokenRequest(
    @SerializedName("deviceId") val deviceId : String,
    @SerializedName("fcmToken") val fcmToken : String
)



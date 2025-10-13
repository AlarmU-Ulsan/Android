package com.alarmit.alarm_it

data class Setting(
    val deviceId: String, //
    var noiceMajor: String = "ICT융합학부", // 처음에 띄울 공지
    var alarmSetting: Boolean = true, // 알림 on/off
    var alarmMajor: String,
    var FCM: Boolean = false // FCM post 여부
)

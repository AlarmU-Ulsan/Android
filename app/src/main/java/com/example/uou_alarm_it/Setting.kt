package com.example.uou_alarm_it

data class Setting(
    val deviceId: String, //
    var noiceMajor: String = "ICT융합학부", // 처음에 띄울 공지
    var alarmSetting: Boolean = true, // 알림 on/off
    var alarmMajor: ArrayList<String> = arrayListOf("ICT융합학부"), // 알림 받을 전공 리스트
    var FCM: Boolean = false // FCM post 여부
)

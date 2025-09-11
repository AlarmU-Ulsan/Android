package com.alarmit.uou_alarm_it

data class College(
    val collegeName: String,
    val majors: MutableList<Major>
)

data class Major(
    val majorName: String,
    var isChecked: Boolean = false
)
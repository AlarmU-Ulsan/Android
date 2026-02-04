package com.uou.alarmit

data class College(
    val collegeName: String,
    val majors: MutableList<Major>
)

data class Major(
    val majorName: String,
    var isChecked: Boolean = false
)
package com.example.uou_alarm_it

object CollegesList {
    val collegesList: MutableList<College> = mutableListOf(
        College("미래엔지니어링융합대학", mutableListOf("ICT융합학부")),
        College("IT융합학부", mutableListOf("IT융합전공", "AI융합전공"))
    )

    data class College(
        val name: String,
        val majors: MutableList<String>
    )
}

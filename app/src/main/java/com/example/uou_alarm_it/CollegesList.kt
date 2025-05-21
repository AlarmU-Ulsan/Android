package com.example.uou_alarm_it

object CollegesList {
    val collegesList: MutableList<College> = mutableListOf(
        College("미래엔지니어링융합대학", mutableListOf("ICT융합학부","미래모빌리티공학부","에너지화학공학부","신소재·반도체융합학부","전기전자융합학부","바이오매디컬헬스학부")),
        College("스마트도시융합대학", mutableListOf("건축·도시환경학부", "디자인융합학부","스포츠과학부")),
        College("경영·공공정책대학", mutableListOf("공공인재학부", "경영경제융합학부")),
        College("인문예술대학", mutableListOf("글로벌인문학부", "예술학부")),
        College("의과대학", mutableListOf("의예과[의학과]", "간호학과")),
        College("아산아너스칼리지", mutableListOf("자율전공학부")),
        College("IT융합학부", mutableListOf("IT융합전공", "AI융합전공"))
    )

    data class College(
        val name: String,
        val majors: MutableList<String>
    )
}

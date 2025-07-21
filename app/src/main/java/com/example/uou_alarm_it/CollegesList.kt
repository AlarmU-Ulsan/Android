package com.example.uou_alarm_it

object CollegesList {
    data class College(
        val name: String,
        val majors: MutableList<major>
    )

    data class major(
        val name: String,
        val enable: Boolean
    )

    val collegesList: MutableList<College> = mutableListOf(
        College("미래엔지니어링융합대학",
            mutableListOf(
                major("ICT융합학부",true),
                major("미래모빌리티공학부",true),
                major("에너지화학공학부", false), // 서비스 제외(1.0.3.1)
                major("신소재·반도체융합학부", true),
                major("전기전자융합학부", true),
                major("바이오매디컬헬스학부", false) // 서비스 제외(1.0.3.1)
            )
        ),
        College("스마트도시융합대학",
            mutableListOf(
                major("건축·도시환경학부", true),
                major("디자인융합학부", true),
                major("스포츠과학부", true)
            )
        ),
        College("경영·공공정책대학",
            mutableListOf(
                major("공공인재학부", false), // 서비스 제외(1.0.3.1)
                major("경영경제융합학부", true)
            )
        ),
        College("인문예술대학",
            mutableListOf(
                major("글로벌인문학부", true),
                major("예술학부", true)
            )
        ),
        College("의과대학",
            mutableListOf(
                major("의예과[의학과]", false), // 서비스 제외(1.0.3.1)
                major("간호학과", false) // 서비스 제외(1.0.3.1)
            )
        ),
        College("아산아너스칼리지",
            mutableListOf(
                major("자율전공학부", true)
            )
        ),
        College("IT융합학부",
            mutableListOf(
                major("IT융합전공", true),
                major("AI융합전공", true)
            )
        )
    )
}

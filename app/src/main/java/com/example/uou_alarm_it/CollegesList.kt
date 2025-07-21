package com.example.uou_alarm_it

object CollegesList {
    data class College(
        val name: String,
        val majors: MutableList<Major>,
        val enable: Boolean = true
    )

    data class Major(
        val name: String,
        val enable: Boolean
    )

    val collegesList: MutableList<College> = mutableListOf(
        College("미래엔지니어링융합대학",
            mutableListOf(
                Major("ICT융합학부",true),
                Major("미래모빌리티공학부",true),
                Major("에너지화학공학부", false), // 서비스 제외(1.0.3.1)
                Major("신소재·반도체융합학부", true),
                Major("전기전자융합학부", true),
                Major("바이오매디컬헬스학부", false) // 서비스 제외(1.0.3.1)
            )
        ),
        College("스마트도시융합대학",
            mutableListOf(
                Major("건축·도시환경학부", true),
                Major("디자인융합학부", true),
                Major("스포츠과학부", true)
            )
        ),
        College("경영·공공정책대학",
            mutableListOf(
                Major("공공인재학부", false), // 서비스 제외(1.0.3.1)
                Major("경영경제융합학부", true)
            )
        ),
        College("인문예술대학",
            mutableListOf(
                Major("글로벌인문학부", true),
                Major("예술학부", true)
            )
        ),
        College("의과대학",
            mutableListOf(
                Major("의예과[의학과]", false), // 서비스 제외(1.0.3.1)
                Major("간호학과", false) // 서비스 제외(1.0.3.1)
            ),
            false // 의과대학 전체 전공 서비스 제외(1.0.3.1)
        ),
        College("아산아너스칼리지",
            mutableListOf(
                Major("자율전공학부", true)
            )
        ),
        College("IT융합학부",
            mutableListOf(
                Major("IT융합전공", true),
                Major("AI융합전공", true)
            )
        )
    )
}

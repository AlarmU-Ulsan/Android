package com.uou.alarmit

object MajorApiMapper {
    private val supportedMajors = setOf(
        "IT융합전공",
        "AI융합전공",
        "자율전공학부",
        "미래모빌리티공학부",
        "신소재반도체융합학부",
        "전기전자융합학부",
        "ICT융합학부",
        "건축도시환경학부",
        "디자인융합학부",
        "스포츠과학부",
        "경영경제융합학부",
        "글로벌인문학부",
        "예술학부",
        "간호학과"
    )

    private val aliases = mapOf(
        "신소재·반도체융합학부" to "신소재반도체융합학부",
        "건축·도시환경학부" to "건축도시환경학부"
    )

    fun toApiMajor(rawMajor: String?): String? {
        val major = rawMajor?.trim().orEmpty()
        if (major.isEmpty()) return null
        if (supportedMajors.contains(major)) return major
        return aliases[major]
    }
}

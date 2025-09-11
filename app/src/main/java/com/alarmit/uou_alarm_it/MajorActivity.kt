package com.alarmit.uou_alarm_it

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alarmit.uou_alarm_it.databinding.ActivityMajorBinding

class MajorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMajorBinding

    // 원본 데이터 (단과대학 및 전공 목록)
    private val originalCollegeList = mutableListOf(
        College("미래엔지니어링융합대학", mutableListOf(
            Major("ICT융합학부"),
            Major("미래모빌리티공학부"),
//            Major("에너지화학공학부"),
            Major("신소재·반도체융합학부"),
            Major("전기전자융합학부"),
//            Major("바이오매디컬헬스학부")
        )),
        College("스마트도시융합대학", mutableListOf(
            Major("건축·도시환경학부"),
            Major("디자인융합학부"),
            Major("스포츠과학부")
        )),
        College("경영·공공정책대학", mutableListOf(
//            Major("공공인재학부"),
            Major("경영경제융합학부")
        )),
        College("인문예술대학", mutableListOf(
            Major("글로벌인문학부"),
            Major("예술학부")
        )),
//        College("의과대학", mutableListOf(
////            Major("의예과[의학과]"),
////            Major("간호학과")
//        )),
        College("아산아너스칼리지", mutableListOf(
            Major("자율전공학부")
        )),
        College("IT융합학부", mutableListOf(
            Major("IT융합전공"),
            Major("AI융합전공")
        ))
    )

    // 필터링 후 보여줄 리스트 (초기에는 전체 데이터)
    private val filteredCollegeList = mutableListOf<College>()

    // 상위 RecyclerView 어댑터
    private lateinit var collegeAdapter: CollegeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMajorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 전달된 현재 선택된 전공 정보를 읽어옴
        val currentSelectedMajor = intent.getStringExtra("currentSelectedMajor") ?:
        getSharedPreferences("app_preferences", Context.MODE_PRIVATE).getString("selected_major", "")

        // 기존 전공 리스트에서 전달된 전공과 이름이 일치하면 isChecked를 true로 설정
        if (!currentSelectedMajor.isNullOrEmpty()) {
            originalCollegeList.forEach { college ->
                college.majors.forEach { major ->
                    major.isChecked = major.majorName == currentSelectedMajor
                }
            }
        }

        // 초기 상태: 전체 리스트 표시
        filteredCollegeList.clear()
        filteredCollegeList.addAll(originalCollegeList)

        // CollegeAdapter 생성 시, 전공 클릭 콜백 전달
        collegeAdapter = CollegeAdapter(filteredCollegeList) { selectedMajor ->
            onMajorSelected(selectedMajor)
        }
        binding.majorChoiceCollegeRv.apply {
            layoutManager = LinearLayoutManager(this@MajorActivity)
            adapter = collegeAdapter
            isNestedScrollingEnabled = false
        }

        binding.majorBackBtnLl.setOnClickListener {
            finish()
        }

        // "다음" 버튼 클릭 시, 선택한 전공명을 Intent extra로 전달하며 FirstAlarmChoiceActivity 실행
        binding.majorNextBtnTv.setOnClickListener {
            var selectedMajor: String? = null
            originalCollegeList.forEach { college ->
                college.majors.forEach { major ->
                    if (major.isChecked) {
                        selectedMajor = major.majorName
                    }
                }
            }
            if (selectedMajor.isNullOrEmpty()) {
                // 선택된 항목이 없으면 기본값(첫 전공)을 사용
                selectedMajor = originalCollegeList.firstOrNull()?.majors?.firstOrNull()?.majorName ?: ""
            }

            // 선택된 전공을 SharedPreferences에 저장 (필요하다면)
            val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            sharedPref.edit().putString("selected_major", selectedMajor).apply()

            // 결과 Intent에 선택한 전공을 담아 반환
            val resultIntent = Intent().apply {
                putExtra("selectedItem", selectedMajor)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // 검색 EditText에 TextWatcher 추가
        binding.majorSearchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterMajors(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // 전공 선택 시, 전체 전공의 선택 상태를 업데이트하여 하나만 선택되도록 함
    private fun onMajorSelected(selectedMajor: Major) {
        originalCollegeList.forEach { college ->
            college.majors.forEach { major ->
                major.isChecked = false
            }
        }
        selectedMajor.isChecked = true
        collegeAdapter.notifyDataSetChanged()
    }

    // 전공명 검색 로직
    private fun filterMajors(query: String) {
        val trimmedQuery = query.trim().lowercase()

        filteredCollegeList.clear()

        if (trimmedQuery.isEmpty()) {
            // 검색어가 없으면 전체 데이터 표시
            filteredCollegeList.addAll(originalCollegeList)
        } else {
            // 각 단과대학 별로 검색어를 포함하는 전공만 필터링
            for (college in originalCollegeList) {
                val matchedMajors = college.majors.filter { major ->
                    major.majorName.lowercase().contains(trimmedQuery)
                }

                // 전공 결과가 하나라도 있으면 해당 단과대학 객체(같은 이름, 필터된 전공 목록)를 추가
                if (matchedMajors.isNotEmpty()) {
                    val filteredCollege = College(
                        collegeName = college.collegeName,
                        // 원본 Major 객체를 그대로 참조하여 체크 상태도 유지
                        majors = matchedMajors.toMutableList()
                    )
                    filteredCollegeList.add(filteredCollege)
                }
            }
        }
        collegeAdapter.notifyDataSetChanged()
    }

}
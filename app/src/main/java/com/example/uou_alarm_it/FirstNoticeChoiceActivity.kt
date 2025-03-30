package com.example.uou_alarm_it

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uou_alarm_it.NoticeActivity.Companion.REQUEST_CODE_MAJOR
import com.example.uou_alarm_it.databinding.ActivityFirstNoticeChoiceBinding

class FirstNoticeChoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstNoticeChoiceBinding

    // 원본 데이터 (단과대학 및 전공 목록)
    private val originalCollegeList = mutableListOf(
        College("미래엔지니어링융합대학", mutableListOf(
            Major("ICT융합학부")
        )),
        College("다른 단과대학", mutableListOf(
            Major("IT융합학부"),
            Major("AI융합전공")
        ))
    )

    // 필터링 후 보여줄 리스트 (초기에는 전체 데이터)
    private val filteredCollegeList = mutableListOf<College>()

    // 상위 RecyclerView 어댑터
    private lateinit var collegeAdapter: CollegeAdapter

    // 사용자가 선택한 전공명을 저장하는 변수
    private var selectedMajorName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstNoticeChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 초기 상태: 전체 리스트 표시
        filteredCollegeList.clear()
        filteredCollegeList.addAll(originalCollegeList)

        // CollegeAdapter 생성 시, 전공 클릭 콜백 전달
        collegeAdapter = CollegeAdapter(filteredCollegeList) { selectedMajor ->
            onMajorSelected(selectedMajor)
        }
        binding.firstNoticeChoiceCollegeRv.apply {
            layoutManager = LinearLayoutManager(this@FirstNoticeChoiceActivity)
            adapter = collegeAdapter
        }

        // "다음" 버튼 클릭 시, 선택한 전공명을 Intent extra로 전달하며 FirstAlarmChoiceActivity 실행
        binding.firstNoticeNextBtnTv.setOnClickListener {
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
            val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            sharedPref.edit().putString("selected_major", selectedMajor).apply()
            val intent = Intent(this, FirstAlarmChoiceActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 검색 EditText에 TextWatcher 추가
        binding.firstNoticeSearchEt.addTextChangedListener(object : TextWatcher {
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
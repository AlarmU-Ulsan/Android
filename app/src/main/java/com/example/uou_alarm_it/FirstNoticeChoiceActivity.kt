package com.example.uou_alarm_it

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
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
        // 원본 전체 전공을 순회하며 모두 false로 설정
        originalCollegeList.forEach { college ->
            college.majors.forEach { major ->
                major.isChecked = false
            }
        }
        // 선택된 전공만 true로 변경
        selectedMajor.isChecked = true

        // 필터링된 리스트도 동일 객체를 참조하므로 업데이트됨
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
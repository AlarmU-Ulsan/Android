package com.example.uou_alarm_it

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uou_alarm_it.databinding.ActivityFirstAlarmChoiceBinding
import com.example.uou_alarm_it.databinding.ActivityFirstNoticeChoiceBinding

class FirstAlarmChoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstAlarmChoiceBinding

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

    // 검색 후 표시할 리스트 (초기에는 전체 데이터)
    private val filteredCollegeList = mutableListOf<College>()

    // 상위 RecyclerView 어댑터 (CollegeAlarmAdapter는 내부에서 전공 선택 시
    // 체크박스(혹은 이미지뷰) 토글 등 멀티 셀렉트를 구현한다고 가정)
    private lateinit var collegeAdapter: CollegeAlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstAlarmChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 초기 상태: 전체 리스트 표시
        filteredCollegeList.clear()
        filteredCollegeList.addAll(originalCollegeList)

        // 상위 RecyclerView 세팅
        collegeAdapter = CollegeAlarmAdapter(filteredCollegeList) { selectedMajor ->
            // 여기서 클릭 시 처리할 로직을 구현합니다.
            // 예: 멀티 선택이 가능하므로, 단순 토글 처리
            selectedMajor.isChecked = !selectedMajor.isChecked
            collegeAdapter.notifyDataSetChanged()
        }

        binding.firstAlarmChoiceCollegeRv.apply {
            layoutManager = LinearLayoutManager(this@FirstAlarmChoiceActivity)
            adapter = collegeAdapter
        }

        binding.firstAlarmBackBtnTv.setOnClickListener {
            val intent = Intent(this, FirstNoticeChoiceActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.firstAlarmNextBtnTv.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 검색 EditText에 TextWatcher 추가
        binding.firstAlarmSearchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterMajors(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
    }

    // 전공명 검색 로직: 검색어가 포함된 전공만 필터링하여,
    // 해당 전공이 있는 단과대학만 filteredCollegeList에 추가합니다.
    private fun filterMajors(query: String) {
        val trimmedQuery = query.trim().lowercase()

        filteredCollegeList.clear()

        if (trimmedQuery.isEmpty()) {
            // 검색어가 없으면 전체 데이터 표시
            filteredCollegeList.addAll(originalCollegeList)
        } else {
            // 각 단과대학 순회 -> 전공 중 검색어가 포함된 것만 추출
            for (college in originalCollegeList) {
                val matchedMajors = college.majors.filter { major ->
                    major.majorName.lowercase().contains(trimmedQuery)
                }

                if (matchedMajors.isNotEmpty()) {
                    // 필터된 전공들만 가진 단과대학 객체 생성 (원본 Major 객체 참조하여 체크 상태 유지)
                    val filteredCollege = College(
                        collegeName = college.collegeName,
                        majors = matchedMajors.toMutableList()
                    )
                    filteredCollegeList.add(filteredCollege)
                }
            }
        }
        collegeAdapter.notifyDataSetChanged()
    }
}
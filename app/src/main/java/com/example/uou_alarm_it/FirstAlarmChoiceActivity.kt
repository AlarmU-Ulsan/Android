package com.example.uou_alarm_it

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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

        // 어댑터 생성 (전공 클릭 시 단순 토글 처리)
        collegeAdapter = CollegeAlarmAdapter(filteredCollegeList) { selectedMajor ->
            selectedMajor.isChecked = !selectedMajor.isChecked
            collegeAdapter.notifyDataSetChanged()
            updateNextButtonVisibility()
        }

        binding.firstAlarmChoiceCollegeRv.apply {
            layoutManager = LinearLayoutManager(this@FirstAlarmChoiceActivity)
            adapter = collegeAdapter
        }

        binding.firstAlarmBackBtnTv.setOnClickListener {
            // 뒤로가기 버튼 : FirstNoticeChoiceActivity로 돌아감.
            val intent = Intent(this, FirstNoticeChoiceActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.firstAlarmNextBtnTv.setOnClickListener {
            // 사용자가 "다음" 버튼을 클릭하면, 초기 플로우 완료 플래그를 true로 저장하여 NoticeActivity로 전환합니다.
            val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("isInitialFlowComplete", true).apply()
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 검색 EditText 관련 로직
        binding.firstAlarmSearchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterMajors(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterMajors(query: String) {
        val trimmedQuery = query.trim().lowercase()
        filteredCollegeList.clear()
        if (trimmedQuery.isEmpty()) {
            filteredCollegeList.addAll(originalCollegeList)
        } else {
            for (college in originalCollegeList) {
                val matchedMajors = college.majors.filter { it.majorName.lowercase().contains(trimmedQuery) }
                if (matchedMajors.isNotEmpty()) {
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

    // 선택된 전공(알림 채널)이 1개 이상이면 "다음" 버튼을 VISIBLE, 아니면 GONE으로 설정
    private fun updateNextButtonVisibility() {
        var selectedCount = 0
        originalCollegeList.forEach { college ->
            college.majors.forEach { major ->
                if (major.isChecked) selectedCount++
            }
        }

        if (selectedCount > 0) {
            // 버튼이 이미 VISIBLE 상태가 아니면 fade in 애니메이션 적용
            if (binding.firstAlarmNextBtnTv.visibility != View.VISIBLE) {
                binding.firstAlarmNextBtnTv.visibility = View.VISIBLE
                val fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_finish_btn)
                binding.firstAlarmNextBtnTv.startAnimation(fadeInAnim)
            }
        } else {
            // 버튼이 보이는 상태면 fade out 애니메이션 적용 후 GONE으로 설정
            if (binding.firstAlarmNextBtnTv.visibility == View.VISIBLE) {
                val fadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out_finish_btn)
                fadeOutAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        binding.firstAlarmNextBtnTv.visibility = View.GONE
                    }
                })
                binding.firstAlarmNextBtnTv.startAnimation(fadeOutAnim)
            }
        }
    }
}
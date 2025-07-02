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

class FirstAlarmChoiceActivity : AppCompatActivity(), SettingInterface {

    private lateinit var binding: ActivityFirstAlarmChoiceBinding

    lateinit var setting: Setting

    private val originalCollegeList = mutableListOf(
        College("미래엔지니어링융합대학", mutableListOf(
            Major("ICT융합학부"),
            Major("미래모빌리티공학부"),
            Major("에너지화학공학부"),
            Major("신소재·반도체융합학부"),
            Major("전기전자융합학부"),
            Major("바이오매디컬헬스학부")
        )),
        College("스마트도시융합대학", mutableListOf(
            Major("건축·도시환경학부"),
            Major("디자인융합학부"),
            Major("스포츠과학부")
        )),
        College("경영·공공정책대학", mutableListOf(
            Major("공공인재학부"),
            Major("경영경제융합학부")
        )),
        College("인문예술대학", mutableListOf(
            Major("글로벌인문학부"),
            Major("예술학부")
        )),
        College("의과대학", mutableListOf(
            Major("의예과[의학과]"),
            Major("간호학과")
        )),
        College("아산아너스칼리지", mutableListOf(
            Major("자율전공학부")
        )),
        College("IT융합학부", mutableListOf(
            Major("IT융합전공"),
            Major("AI융합전공")
        ))
    )

    private val filteredCollegeList = mutableListOf<College>()

    private lateinit var collegeAdapter: CollegeAlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstAlarmChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setting = loadSetting(this)

        filteredCollegeList.clear()
        filteredCollegeList.addAll(originalCollegeList)

        collegeAdapter = CollegeAlarmAdapter(filteredCollegeList) { selectedMajor ->
            val checkedCount = getCheckedMajorsCount()
            if (!selectedMajor.isChecked && checkedCount >= 2) {
                return@CollegeAlarmAdapter
            }

            selectedMajor.isChecked = !selectedMajor.isChecked
            collegeAdapter.notifyDataSetChanged()
            updateNextButtonVisibility()

            val selectedMajors = getSelectedMajors().joinToString(",")
            changeMajor2(this, selectedMajors)
        }

        binding.firstAlarmChoiceCollegeRv.apply {
            layoutManager = LinearLayoutManager(this@FirstAlarmChoiceActivity)
            adapter = collegeAdapter
            isNestedScrollingEnabled = false
        }

        binding.firstAlarmBackBtnTv.setOnClickListener {
            val intent = Intent(this, FirstNoticeChoiceActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }

        binding.firstAlarmNextBtnTv.setOnClickListener {
            val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("isInitialFlowComplete", true).apply()
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
            finish()
        }

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
                    filteredCollegeList.add(College(college.collegeName, matchedMajors.toMutableList()))
                }
            }
        }
        collegeAdapter.notifyDataSetChanged()
    }

    private fun updateNextButtonVisibility() {
        val selectedCount = getCheckedMajorsCount()
        binding.firstAlarmNextBtnTv.text = if (selectedCount > 0) "완료" else "건너뛰기"

        if (binding.firstAlarmNextBtnTv.visibility != View.VISIBLE) {
            binding.firstAlarmNextBtnTv.visibility = View.VISIBLE
            val fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_finish_btn)
            binding.firstAlarmNextBtnTv.startAnimation(fadeInAnim)
        }
    }

    private fun getCheckedMajorsCount(): Int {
        return originalCollegeList.sumOf { college ->
            college.majors.count { it.isChecked }
        }
    }

    private fun getSelectedMajors(): List<String> {
        return originalCollegeList.flatMap { college ->
            college.majors.filter { it.isChecked }.map { it.majorName }
        }
    }

    private fun changeMajor2(context: Context, majors: String) {
        setFCM(Setting(false, setting.notificationMajor))
        setting.notificationMajor = majors
        setFCM(setting)
        saveSetting(context, setting)
    }
}
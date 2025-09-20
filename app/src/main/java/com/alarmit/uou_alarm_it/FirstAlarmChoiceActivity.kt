package com.alarmit.uou_alarm_it

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alarmit.uou_alarm_it.CollegesList.College
import com.alarmit.uou_alarm_it.CollegesList.collegesList
import com.alarmit.uou_alarm_it.databinding.ActivityFirstAlarmChoiceBinding
import com.alarmit.uou_alarm_it.databinding.ItemAlarmChoiceBinding
import com.alarmit.uou_alarm_it.databinding.ItemAlarmChoiceCollegeBinding

class FirstAlarmChoiceActivity : AppCompatActivity(), SettingInterface {

    private lateinit var binding: ActivityFirstAlarmChoiceBinding

    lateinit var setting: Setting

    private var searchKeyword: String = ""
    private var alarmCollegeList = collegesList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstAlarmChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setting = loadSetting(this)

        initRV(alarmCollegeList)

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
                searchKeyword = s.toString().lowercase()
                alarmCollegeList = mutableListOf()

                if (searchKeyword.isEmpty()) {
                    alarmCollegeList.addAll(collegesList)
                } else {
                    for (c in collegesList) {
                        val filteredMajors = c.majors.filter { it.name.lowercase().contains(searchKeyword) }.toMutableList()
                        if (filteredMajors.isNotEmpty()) {
                            alarmCollegeList.add(College(c.name, filteredMajors))
                        }
                    }
                }

                initRV(alarmCollegeList)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updateNextButtonVisibility() {
        val selectedCount = setting.alarmMajor.size
        binding.firstAlarmNextBtnTv.text = if (selectedCount > 0) "완료" else "건너뛰기"

        if (binding.firstAlarmNextBtnTv.visibility != View.VISIBLE) {
            binding.firstAlarmNextBtnTv.visibility = View.VISIBLE
            val fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_finish_btn)
            binding.firstAlarmNextBtnTv.startAnimation(fadeInAnim)
        }
    }

    private fun initRV(colleges: MutableList<College>) {
        val collegeRVAdapter = CollegeRVAdapter(colleges)
        binding.firstAlarmChoiceCollegeRv.adapter = collegeRVAdapter
        binding.firstAlarmChoiceCollegeRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    inner class CollegeRVAdapter(private val colleges: MutableList<CollegesList.College>): RecyclerView.Adapter<CollegeRVAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val collegeBinding = ItemAlarmChoiceCollegeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(collegeBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(colleges[position])
        }

        override fun getItemCount(): Int = colleges.size

        inner class ViewHolder(private val collegeBinding: ItemAlarmChoiceCollegeBinding): RecyclerView.ViewHolder(collegeBinding.root) {
            fun bind(college: CollegesList.College) {
                if (college.enable) {
                    collegeBinding.itemAlarmChoiceCollegeTitle.text = college.name
                    initCollege(college)
                }
                else {
                    collegeBinding.root.visibility = View.GONE
                }
            }

            fun initCollege(college: CollegesList.College) {
                val majorRVAdapter = MajorRVAdapter(college)
                collegeBinding.itemAlarmChoiceCollegeRv.adapter = majorRVAdapter
                collegeBinding.itemAlarmChoiceCollegeRv.layoutManager = LinearLayoutManager(this@FirstAlarmChoiceActivity, LinearLayoutManager.VERTICAL, false)
            }

            inner class MajorRVAdapter(private val college: CollegesList.College): RecyclerView.Adapter<MajorRVAdapter.ViewHolder>() {
                private val majors = college.majors

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val majorBinding = ItemAlarmChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    return ViewHolder(majorBinding)
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    holder.bind(majors[position])
                }

                override fun getItemCount(): Int = majors.size

                inner class ViewHolder(private val majorBinding: ItemAlarmChoiceBinding): RecyclerView.ViewHolder(majorBinding.root) {
                    fun bind(major: CollegesList.Major) {
                        majorBinding.itemAlarmChoiceTitle.text = major.name

                        if (setting.alarmMajor.contains(major.name)) {
                            majorBinding.itemAlarmChoiceToggle.setImageResource(R.drawable.alarm_check_on)
                        } else {
                            majorBinding.itemAlarmChoiceToggle.setImageResource(R.drawable.alarm_check_off)
                        }

                        majorBinding.root.setOnClickListener {
                            if (setting.alarmMajor.contains(major.name)) {
                                setting.alarmMajor.remove(major.name)
                            } else {
                                setting.alarmMajor.clear()
                                setting.alarmMajor.add(major.name)
                            }

                            updateNextButtonVisibility()
                            saveSetting(this@FirstAlarmChoiceActivity, setting)

                            initRV(this@FirstAlarmChoiceActivity.alarmCollegeList)
                        }
                    }
                }
            }
        }
    }
}

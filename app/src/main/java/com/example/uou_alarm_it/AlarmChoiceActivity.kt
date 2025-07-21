package com.example.uou_alarm_it

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uou_alarm_it.CollegesList.College
import com.example.uou_alarm_it.CollegesList.collegesList
import com.example.uou_alarm_it.NoticeActivity.Companion.bookmarkList
import com.example.uou_alarm_it.NoticeActivity.Companion.category
import com.example.uou_alarm_it.NoticeActivity.Companion.noticeList
import com.example.uou_alarm_it.databinding.ActivityAlarmChoiceBinding
import com.example.uou_alarm_it.databinding.ItemAlarmChoiceBinding
import com.example.uou_alarm_it.databinding.ItemAlarmChoiceCollegeBinding

class AlarmChoiceActivity: AppCompatActivity(), SettingInterface {
    lateinit var binding: ActivityAlarmChoiceBinding
    lateinit var setting: Setting

    private var searchKeyword: String = ""
    private var alarmCollegeList = collegesList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRV(collegesList)

        setting = loadSetting(this)
        initToggle()

        binding.alarmChoiceToggle.setOnClickListener {
            setting.alarmSetting = changeSetting(this)
            initToggle()
        }

        binding.alarmChoiceBack.setOnClickListener {
            finish()
        }

        binding.alarmChoiceEt.addTextChangedListener(object : TextWatcher {
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

    private fun initRV(colleges: MutableList<College>) {
        val collegeRVAdapter = CollegeRVAdapter(colleges)
        binding.alarmChoiceRv.adapter = collegeRVAdapter
        binding.alarmChoiceRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun initToggle() {
        if (setting.alarmSetting) {
            binding.alarmChoiceToggleOn.visibility = View.VISIBLE
            binding.alarmChoiceToggleOff.visibility = View.GONE
        } else {
            binding.alarmChoiceToggleOn.visibility = View.GONE
            binding.alarmChoiceToggleOff.visibility = View.VISIBLE
        }
    }

    inner class CollegeRVAdapter(private val colleges: MutableList<College>): RecyclerView.Adapter<CollegeRVAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val collegeBinding = ItemAlarmChoiceCollegeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(collegeBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(colleges[position])
        }

        override fun getItemCount(): Int = colleges.size

        inner class ViewHolder(private val collegeBinding: ItemAlarmChoiceCollegeBinding): RecyclerView.ViewHolder(collegeBinding.root) {
            fun bind(college: College) {
                if (college.enable) {
                    collegeBinding.itemAlarmChoiceCollegeTitle.text = college.name
                    initCollege(college)
                }
                else {
                    collegeBinding.root.visibility = View.GONE
                }
            }

            fun initCollege(college: College) {
                val majorRVAdapter = MajorRVAdapter(college)
                collegeBinding.itemAlarmChoiceCollegeRv.adapter = majorRVAdapter
                collegeBinding.itemAlarmChoiceCollegeRv.layoutManager = LinearLayoutManager(this@AlarmChoiceActivity, LinearLayoutManager.VERTICAL, false)
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
                            majorBinding.itmeAlarmChoiceToggle.setImageResource(R.drawable.alarm_check_on)
                        } else {
                            majorBinding.itmeAlarmChoiceToggle.setImageResource(R.drawable.alarm_check_off)
                        }

                        majorBinding.root.setOnClickListener {
                            if (setting.alarmMajor.contains(major.name)) {
                                setting.alarmMajor.remove(major.name)
                            } else {
                                if (setting.alarmMajor.size >= 2) {
                                    setting.alarmMajor = arrayListOf(setting.alarmMajor.get(1))
                                }
                                setting.alarmMajor.add(major.name)
                            }

                            saveSetting(this@AlarmChoiceActivity, setting)

                            initRV(this@AlarmChoiceActivity.alarmCollegeList)
                        }
                    }
                }
            }
        }
    }
}
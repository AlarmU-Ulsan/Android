package com.example.uou_alarm_it

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uou_alarm_it.CollegesList.College
import com.example.uou_alarm_it.databinding.ActivityAlarmChoiceBinding
import com.example.uou_alarm_it.databinding.ItemAlarmChoiceBinding
import com.example.uou_alarm_it.databinding.ItemAlarmChoiceCollegeBinding

class AlarmChoiceActivity: AppCompatActivity(), SettingInterface {
    lateinit var binding: ActivityAlarmChoiceBinding
    lateinit var setting: Setting
    lateinit var selectMajor: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRV()

        setting = loadSetting(this)
        selectMajor = setting.notificationMajor
        initToggle()

        binding.alarmChoiceToggle.setOnClickListener {
            setting.notificationSetting = changeSetting(this)
            initToggle()
        }

        binding.alarmChoiceBack.setOnClickListener {
            finish()
        }
    }

    private fun initRV() {
        val collegeRVAdapter = CollegeRVAdapter(CollegesList.collegesList)
        binding.alarmChoiceRv.adapter = collegeRVAdapter
        binding.alarmChoiceRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun initToggle() {
        if (setting.notificationSetting) {
            binding.alarmChoiceToggle.setImageResource(R.drawable.toggle_on)
        }
        else {
            binding.alarmChoiceToggle.setImageResource(R.drawable.toggle_off)
        }
    }

    inner class CollegeRVAdapter(private val colleges: Array<College>): RecyclerView.Adapter<CollegeRVAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollegeRVAdapter.ViewHolder {
            val collegeBinding = ItemAlarmChoiceCollegeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(collegeBinding)
        }

        override fun onBindViewHolder(holder: CollegeRVAdapter.ViewHolder, collegePos: Int) {
            holder.bind(colleges[collegePos])
        }

        override fun getItemCount(): Int = colleges.size

        inner class ViewHolder(private val collegeBinding: ItemAlarmChoiceCollegeBinding): RecyclerView.ViewHolder(collegeBinding.root) {
            fun bind(college: CollegesList.College) {
                collegeBinding.itemAlarmChoiceCollegeTitle.text = college.name
                initCollege(college)
            }

            fun initCollege(college: College) {
                val majorRVAdapter = MajorRVAdapter(college)
                collegeBinding.itemAlarmChoiceCollegeRv.adapter = majorRVAdapter
                collegeBinding.itemAlarmChoiceCollegeRv.layoutManager = LinearLayoutManager(this@AlarmChoiceActivity, LinearLayoutManager.VERTICAL, false)

            }

            inner class MajorRVAdapter(private val college: College): RecyclerView.Adapter<MajorRVAdapter.ViewHolder>() {
                val majors = college.majors

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): MajorRVAdapter.ViewHolder {
                    val majorBinding = ItemAlarmChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    return ViewHolder(majorBinding)
                }

                override fun onBindViewHolder(holder: MajorRVAdapter.ViewHolder, position: Int) {
                    holder.bind(majors[position])
                }

                override fun getItemCount(): Int = majors.size

                inner class ViewHolder(private val majorBinding: ItemAlarmChoiceBinding): RecyclerView.ViewHolder(majorBinding.root) {
                    fun bind(major: String) {
                        majorBinding.itemAlarmChoiceTitle.text = major
                        if(major == selectMajor) {
                            majorBinding.itmeAlarmChoiceToggle.setImageResource(R.drawable.alarm_check_on)
                        }
                        else{
                            majorBinding.itmeAlarmChoiceToggle.setImageResource(R.drawable.alarm_check_off)
                        }
                        majorBinding.root.setOnClickListener{
                            selectMajor = major
                            changeMajor(this@AlarmChoiceActivity, selectMajor)
                            initRV()
                        }
                    }
                }
            }
        }
    }
}
package com.example.uou_alarm_it

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CollegeAlarmAdapter(
    private val collegeList: List<College>,
    // 단과대학 내 전공 선택 이벤트 콜백 (Activity에서 전달)
    private val onMajorClick: (Major) -> Unit
) : RecyclerView.Adapter<CollegeAlarmAdapter.CollegeAlarmViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CollegeAlarmAdapter.CollegeAlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_college_alarm, parent, false)
        return CollegeAlarmViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CollegeAlarmAdapter.CollegeAlarmViewHolder,
        position: Int
    ) {
        holder.bind(collegeList[position])
    }

    override fun getItemCount(): Int = collegeList.size

    inner class CollegeAlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCollegeName: TextView = itemView.findViewById(R.id.item_college_alarm_name_tv)
        private val rvMajors: RecyclerView = itemView.findViewById(R.id.item_college_alarm_major_rv)

        fun bind(college: College) {
            tvCollegeName.text = college.collegeName

            // 자식(전공) RecyclerView 설정
            rvMajors.layoutManager = LinearLayoutManager(itemView.context)
            rvMajors.adapter = MajorAlarmAdapter(college.majors, onMajorClick)
            rvMajors.setHasFixedSize(true)
        }
    }
}
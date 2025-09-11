package com.alarmit.uou_alarm_it

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MajorAlarmAdapter(
    private val majorList: List<Major>,
    private val onMajorClick: (Major) -> Unit
) : RecyclerView.Adapter<MajorAlarmAdapter.MajorAlarmViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MajorAlarmAdapter.MajorAlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_major_alarm_choice, parent, false)
        return MajorAlarmViewHolder(view)
    }

    override fun getItemCount(): Int = majorList.size

    override fun onBindViewHolder(holder: MajorAlarmAdapter.MajorAlarmViewHolder, position: Int) {
        holder.bind(majorList[position])
    }
    inner class MajorAlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMajorName: TextView = itemView.findViewById(R.id.item_major_alarm_choice_name_tv)
        private val ivCheck: ImageView = itemView.findViewById(R.id.item_major_alarm_choice_checkbox_iv)

        fun bind(major: Major) {
            tvMajorName.text = major.majorName
            // 상태에 따라 이미지 변경
            if (major.isChecked) {
                ivCheck.setImageResource(R.drawable.alarm_check_on)
            } else {
                ivCheck.setImageResource(R.drawable.alarm_check_off)
            }

            // 전체 아이템 클릭 시 선택 이벤트 전달
            itemView.setOnClickListener {
                onMajorClick(major)
            }
        }
    }

}

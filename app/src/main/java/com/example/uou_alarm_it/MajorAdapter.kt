package com.example.uou_alarm_it

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MajorAdapter(
    private val majorList: List<Major>,
    // 클릭 시 선택된 Major를 전달하는 콜백
    private val onMajorClick: (Major) -> Unit
) : RecyclerView.Adapter<MajorAdapter.MajorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MajorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_major_choice, parent, false)
        return MajorViewHolder(view)
    }

    override fun onBindViewHolder(holder: MajorViewHolder, position: Int) {
        holder.bind(majorList[position])
    }

    override fun getItemCount(): Int = majorList.size

    inner class MajorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMajorName: TextView = itemView.findViewById(R.id.item_major_choice_name_tv)
        private val ivCheck: ImageView = itemView.findViewById(R.id.item_major_choice_checkbox_iv)

        fun bind(major: Major) {
            tvMajorName.text = major.majorName
            // 상태에 따라 이미지 변경
            if (major.isChecked) {
                ivCheck.setImageResource(R.drawable.notice_check_on)
            } else {
                ivCheck.setImageResource(R.drawable.notice_check_off)
            }

            // 전체 아이템 클릭 시 선택 이벤트 전달
            itemView.setOnClickListener {
                onMajorClick(major)
            }
        }
    }
}
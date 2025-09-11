package com.alarmit.uou_alarm_it

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.alarmit.uou_alarm_it.databinding.DialogPrivacyPolicyBinding

class PrivacyPolicyDialog(
    context: Context,
    private val onAgree: () -> Unit,
    private val onCancel: () -> Unit
) : Dialog(context) {

    private val binding: DialogPrivacyPolicyBinding =
        DialogPrivacyPolicyBinding.inflate(LayoutInflater.from(context))

    private var isChecked = false

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 투명 배경
            attributes = attributes.apply {
                width = context.resources.displayMetrics.widthPixels // 가로: match_parent
                height = WindowManager.LayoutParams.WRAP_CONTENT     // 세로: wrap_content
                gravity = Gravity.BOTTOM                             // 하단 정렬
            }
        }

        // 초기 상태: 다음 버튼 비활성화
        val nextBtn = binding.root.findViewById<TextView>(R.id.privacy_next_btn_tv)
        nextBtn.isEnabled = false
        nextBtn.setTextColor(Color.parseColor("#666666"))
        nextBtn.setBackgroundResource(R.drawable.bg_privacy_gray_btn)

        // 체크박스 클릭 시 UI 및 상태 변경
        binding.privacyCheckboxIv.setOnClickListener {
            isChecked = !isChecked
            val checkRes = if (isChecked) R.drawable.notice_check_on else R.drawable.notice_check_off
            binding.privacyCheckboxIv.setImageResource(checkRes)

            if (isChecked) {
                // ✅ 버튼 활성화
                nextBtn.isEnabled = true
                nextBtn.setBackgroundResource(R.drawable.bg_privacy_green_btn) // 👉 활성화된 배경 drawable 추가 필요
                nextBtn.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            } else {
                // ✅ 버튼 비활성화
                nextBtn.isEnabled = false
                nextBtn.setBackgroundResource(R.drawable.bg_privacy_gray_btn)
                nextBtn.setTextColor(Color.parseColor("#666666"))
            }
        }

        binding.root.findViewById<TextView>(R.id.privacy_close_btn_tv).setOnClickListener {
            onCancel()
            dismiss()
        }

        binding.root.findViewById<TextView>(R.id.privacy_next_btn_tv).setOnClickListener {
            if (isChecked) {
                onAgree()
                dismiss()
            }
        }

        binding.root.findViewById<TextView>(R.id.privacy_policy_detail_tv).setOnClickListener {
            val intent = Intent(context, PrivacyPolicyActivity::class.java)
            context.startActivity(intent)
        }
    }
}
package com.alarmit.uou_alarm_it

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alarmit.uou_alarm_it.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.privacyBackBtn.setOnClickListener {
            finish()
        }

        binding.privacyContentTv.text = """
            본 앱(이하 “알림IT”)은 「개인정보 보호법」 등 관련 법령에 따라 
            이용자의 개인정보를 보호하고 권익을 보장하기 위해 
            다음과 같이 개인정보처리방침을 수립·공개합니다.

            1. 수집하는 개인정보 항목
            - 필수항목: 디바이스 식별값, FCM 토큰
            - 선택항목: 없음

            2. 개인정보의 수집·이용 목적
            - 학교 공지 및 안내사항 푸시 알림 전송

            3. 개인정보의 보유 및 이용기간
            - 수집한 개인정보는 서비스 제공 기간 동안 보유·이용하며, 
              이용자가 앱을 삭제하거나 동의를 철회하는 경우 즉시 파기합니다.

            4. 개인정보의 제3자 제공
            - 서비스는 원칙적으로 이용자의 개인정보를 외부에 제공하지 않습니다. 
              다만, 법령에 근거가 있거나 수사기관의 요청이 있는 경우 
              예외적으로 제공될 수 있습니다.

            5. 개인정보의 처리 위탁
            - 서비스는 이용자의 개인정보 처리를 제3자에게 위탁하지 않습니다.

            6. 개인정보의 파기 절차 및 방법
            - 개인정보는 보유기간이 경과하거나 처리 목적이 달성된 즉시 파기합니다.
            - 전자적 파일은 복구 불가능한 방법으로 삭제하며, 
              종이 문서가 있는 경우 분쇄 또는 소각하여 파기합니다.

            7. 이용자의 권리와 행사 방법
            - 이용자는 언제든지 개인정보의 조회, 수정, 삭제, 처리정지를 요구할 수 있습니다.
            - 앱 내 설정 또는 아래 연락처를 통해 요청할 수 있으며, 즉시 조치하겠습니다.

            8. 개인정보 보호 책임자
            - 개인정보 보호 책임자: 이규재
            - 연락처: starlee2427@ulsan.ac.kr 

            9. 개인정보처리방침 변경
            - 본 방침은 법령, 지침 및 서비스의 변경에 따라 개정될 수 있습니다.
            - 중요한 변경사항이 있을 경우 앱 내 공지사항을 통해 사전 안내하겠습니다.

            시행일자: 2025년 9월 1일
            
            
            
            
            
        """.trimIndent()
    }
}
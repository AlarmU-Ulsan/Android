package com.example.uou_alarm_it

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.uou_alarm_it.databinding.ActivitySplashBinding
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.background.BackgroundEventSource
import java.net.URL
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {
    lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SSE
        val eventSource: BackgroundEventSource = BackgroundEventSource
            .Builder(
                SSEService(),
                EventSource.Builder(
                    ConnectStrategy
                        .http(URL("https://alarm-it.githyeon.shop/subscribe"))
                        // 커스텀 요청 헤더를 명시
//                        .header(
//                            "Authorization",
//                            "Bearer {token}"
//                        )
                        .connectTimeout(3, TimeUnit.SECONDS)
                        // 최대 연결 유지 시간을 설정, 서버에 설정된 최대 연결 유지 시간보다 길게 설정
                        .readTimeout(600, TimeUnit.SECONDS)
                )
            )
            .threadPriority(Thread.MAX_PRIORITY)
            .build()

// EventSource 연결 시작
        eventSource.start()

        android.os.Handler().postDelayed({
            val intent = Intent(this, NoticeActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        },2000)
    }
}
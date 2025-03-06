package com.example.uou_alarm_it

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.uou_alarm_it.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var link = ""

        intent?.extras?.let{
            link = it.getString("link") ?:""
        }

        android.os.Handler().postDelayed({
            Log.d("splashFCM", link)
            val intent = Intent(this, NoticeActivity::class.java).apply {
                putExtra("link", link)  // 알림에 포함된 데이터 전송
            }
            startActivity(intent)
            finish()
        },2000)
    }


}
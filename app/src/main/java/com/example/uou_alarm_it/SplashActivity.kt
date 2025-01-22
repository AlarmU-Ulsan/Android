package com.example.uou_alarm_it

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uou_alarm_it.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}
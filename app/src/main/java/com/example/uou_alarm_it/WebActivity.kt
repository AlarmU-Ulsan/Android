package com.example.uou_alarm_it

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uou_alarm_it.databinding.ActivityWebBinding

class WebActivity : AppCompatActivity(){
    lateinit var binding : ActivityWebBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)



        setContentView(binding.root)
    }
}
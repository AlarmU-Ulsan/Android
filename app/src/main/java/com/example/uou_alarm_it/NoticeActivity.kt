package com.example.uou_alarm_it

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uou_alarm_it.databinding.ActivityNoticeBinding

class NoticeActivity : AppCompatActivity() {
    lateinit var binding : ActivityNoticeBinding
    private val noticeRVAdapter = NoticeRVAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)

//        initRV()

        setContentView(binding.root)
    }

    private fun initRV() {
        binding.noticeRv.adapter = noticeRVAdapter
        binding.noticeRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
}
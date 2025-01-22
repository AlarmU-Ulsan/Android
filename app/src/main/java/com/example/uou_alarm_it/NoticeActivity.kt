package com.example.uou_alarm_it

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uou_alarm_it.databinding.ActivityNoticeBinding

class NoticeActivity : AppCompatActivity() {
    lateinit var binding : ActivityNoticeBinding
    private val noticeRVAdapter = NoticeRVAdapter()

    var position : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)

        setposition(position)

        binding.noticeTabAll.setOnClickListener{
            position = 0
            setposition(position)
        }
        binding.noticeTabImportant.setOnClickListener {
            position = 1
            setposition(position)
        }
        binding.noticeTabBookmark.setOnClickListener {
            position = 2
            setposition(position)
        }
        binding.noticeSearchIv.setOnClickListener{
            if(binding.noticeSearchEt.visibility == View.GONE) {
                binding.noticeSearchEt.visibility = View.VISIBLE
                binding.noticeTabLayout.visibility = View.GONE
            }
            else {
                binding.noticeTabLayout.visibility = View.VISIBLE
                noticeSearch()
            }
        }

        setContentView(binding.root)
    }

    private fun noticeSearch() {
        Log.d("Notice Search", binding.noticeSearchEt.text.toString())
    }

    override fun onBackPressed() {
        if (binding.noticeSearchEt.visibility == View.VISIBLE) {
            binding.noticeSearchEt.visibility = View.GONE
            binding.noticeTabLayout.visibility = View.VISIBLE
        }
        else {
            super.onBackPressed()
        }
    }

    private fun setposition(position:Int){

        binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.gray40))
        binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.gray40))
        binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.gray40))

        when (position) {
            1 -> {
                updateRVImportant()
                binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            2 -> {
                updateRVBookmark()
                binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.black))

            }
            else -> {
                updateRVAll()
                binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }

    private fun updateRVAll() {
        binding.noticeRv.adapter = noticeRVAdapter
        binding.noticeRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun updateRVImportant() {
        binding.noticeRv.adapter = noticeRVAdapter
        binding.noticeRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun updateRVBookmark() {
        binding.noticeRv.adapter = noticeRVAdapter
        binding.noticeRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
}
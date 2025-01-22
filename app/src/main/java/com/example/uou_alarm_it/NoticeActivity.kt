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
            setposition(0)
        }
        binding.noticeTabImportant.setOnClickListener {
            setposition(1)
        }
        binding.noticeTabBookmark.setOnClickListener {
            setposition(2)
        }
        binding.noticeSearchIv.setOnClickListener{
            if(binding.noticeSearchEt.visibility == View.GONE) {
                binding.noticeSearchEt.visibility = View.VISIBLE
                binding.noticeTabLayout.visibility = View.GONE
            }
            else {
                noticeSearch()
            }
        }

        setContentView(binding.root)
    }

    private fun setposition(position:Int){
        binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.gray40))
        binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.gray40))
        binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.gray40))
        binding.noticeRv.adapter = noticeRVAdapter

        when (position) {
            1 -> {
                this.position = 1
                binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            2 -> {
                this.position = 2
                binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            else -> {
                this.position = 0
                binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        noticeRVAdapter.setMyClickListener(object : NoticeRVAdapter.MyClickListener{
            override fun onItemClick(notice: Notice) {
                Log.d("test", "Item")
            }

            override fun onBookmarkClick(notice: Notice) {
                Log.d("test", "Bookmark")
                noticeRVAdapter.bookmarkList.add(notice)
            }

        })
        binding.noticeRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
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
}
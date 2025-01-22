package com.example.uou_alarm_it

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uou_alarm_it.databinding.ActivityNoticeBinding
import com.google.gson.Gson

class NoticeActivity : AppCompatActivity() {
    lateinit var binding : ActivityNoticeBinding
    lateinit var noticeRVAdapter : NoticeRVAdapter


    companion object {
        var position : Int = 0
        var noticeList : ArrayList<Notice> = arrayListOf(
            Notice(true, "★재학생(편입, 복전) 필독★ 졸업작품 이수 관련 안내", 9, "2024-09-05"),
            Notice(true, "2024-2학기 빛냄장학 신청 안내", 10, "2024-09-05"),
            Notice(true, "2024-2학기 역량개발장학 신청 안내", 11, "2024-09-05"),
            Notice(false, "개인단위 수준별 영어교육 시행에 따른 기초학력...", 388, "2025-01-06"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 387, "2025-01-03"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 386, "2025-01-03"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 385, "2025-01-03"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 384, "2025-01-03"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 383, "2025-01-03"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 382, "2025-01-03"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 381, "2025-01-03"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 380, "2025-01-03"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 379, "2025-01-03"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 378, "2025-01-03"),
            Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 377, "2025-01-03"),
        )

        var bookmarkList : HashSet<Int> = hashSetOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        bookmarkList = loadNumberList()

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

        when (position) {
            1 -> {
                NoticeActivity.position = 1
                binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            2 -> {
                NoticeActivity.position = 2
                binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            else -> {
                NoticeActivity.position = 0
                binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
        noticeRVAdapter = NoticeRVAdapter()
        binding.noticeRv.adapter = noticeRVAdapter
        noticeRVAdapter.setMyClickListener(object : NoticeRVAdapter.MyClickListener{
            override fun onItemClick(notice: Notice) {
                Log.d("test", "Item")
            }

            override fun onBookmarkClick(notice: Notice) {
                Log.d("test", "Bookmark")
                if (notice.number in bookmarkList) {
                    bookmarkList.remove(notice.number)
                }
                else {
                    bookmarkList.add(notice.number)
                }
                noticeRVAdapter.bookmarkList = bookmarkList
                saveNumberList(bookmarkList)
                Log.d("Save Bookmark", bookmarkList.toString())
            }

        })
        binding.noticeRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    fun saveNumberList(numberList: HashSet<Int>) {
        val sharedPreferences = this.getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(numberList) // List를 JSON 문자열로 변환
        editor.putString("Bookmark", json)
        editor.apply()
    }

    fun loadNumberList(): HashSet<Int> {
        val sharedPreferences = this.getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("Bookmark", null) // 기본값은 null로 설정

        return if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<HashSet<Int>>() {}.type
            gson.fromJson(json, type) // JSON 문자열을 ArrayList로 변환
        } else {
            hashSetOf() // 데이터가 없으면 빈 리스트 반환
        }
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
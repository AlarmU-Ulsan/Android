package com.example.uou_alarm_it

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uou_alarm_it.databinding.ActivityNoticeBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeActivity : AppCompatActivity() {
    lateinit var binding : ActivityNoticeBinding
    lateinit var noticeRVAdapter : NoticeRVAdapter


    companion object {
        var category : Int = 1
        var noticeList : ArrayList<Notice> = arrayListOf()
        var bookmarkList : HashSet<Notice> = hashSetOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        bookmarkList = loadBookmarkList()

        RetrofitClient.service.getNotice(1,0).enqueue(object : Callback<GetNoticeRequest>{
            override fun onResponse(
                call: Call<GetNoticeRequest>,
                response: Response<GetNoticeRequest>
            ) {
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result

                    noticeList = res.content
                    Log.d("retrofit", res.content.toString())

                    setCategory(category)
                }
            }

            override fun onFailure(call: Call<GetNoticeRequest>, t: Throwable) {
                Log.e("retrofit", t.toString())
            }

        })

        binding.noticeTabAll.setOnClickListener{
            setCategory(1)
        }
        binding.noticeTabImportant.setOnClickListener {
            setCategory(0)
        }
        binding.noticeTabBookmark.setOnClickListener {
            setCategory(3)
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

    private fun setCategory(category:Int){

        binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.gray40))
        binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.gray40))
        binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.gray40))

        if(category != NoticeActivity.category) {
            noticeList = arrayListOf()
        }

        when (category) {
            1 -> {
                NoticeActivity.category = 1
                binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.black))
                RetrofitClient.service.getNotice(category,0).enqueue(object : Callback<GetNoticeRequest>{
                    override fun onResponse(
                        call: Call<GetNoticeRequest>,
                        response: Response<GetNoticeRequest>
                    ) {
                        if (response.body()?.code == "COMMON200") {
                            val res = response.body()!!.result

                            noticeList += res.content
                            Log.d("retrofit", res.content.toString())

                            initRV()
                        }
                    }

                    override fun onFailure(call: Call<GetNoticeRequest>, t: Throwable) {
                        Log.e("retrofit", t.toString())
                    }

                })
            }
            0 -> {
                NoticeActivity.category = 0
                binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.black))
                RetrofitClient.service.getNotice(category,0).enqueue(object : Callback<GetNoticeRequest>{
                    override fun onResponse(
                        call: Call<GetNoticeRequest>,
                        response: Response<GetNoticeRequest>
                    ) {
                        if (response.body()?.code == "COMMON200") {
                            val res = response.body()!!.result

                            noticeList += res.content
                            Log.d("retrofit", res.content.toString())
                            initRV()
                        }
                    }

                    override fun onFailure(call: Call<GetNoticeRequest>, t: Throwable) {
                        Log.e("retrofit", t.toString())
                    }

                })
            }
            3 -> {
                NoticeActivity.category = 3
                binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.black))
                noticeList = bookmarkList.toCollection(ArrayList())
                initRV()
            }
        }
    }

    fun initRV() {
        noticeRVAdapter = NoticeRVAdapter()
        binding.noticeRv.adapter = noticeRVAdapter
        noticeRVAdapter.setMyClickListener(object : NoticeRVAdapter.MyClickListener{
            override fun onItemClick(notice: Notice) {
                Log.d("test", "Item")
            }

            override fun onBookmarkClick(notice: Notice) {
                Log.d("test", "Bookmark")
                if (notice in bookmarkList) {
                    bookmarkList.remove(notice)
                }
                else {
                    bookmarkList.add(notice)
                }
                noticeRVAdapter.bookmarkList = bookmarkList
                saveBookmarkList(bookmarkList)
                Log.d("Save Bookmark", bookmarkList.toString())
            }

        })
        binding.noticeRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    fun saveBookmarkList(BookmarkList : HashSet<Notice>) {
        val sharedPreferences = this.getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(BookmarkList) // List를 JSON 문자열로 변환
        editor.putString("Bookmark", json)
        editor.apply()
    }

    fun loadBookmarkList(): HashSet<Notice> {
        val sharedPreferences = this.getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("Bookmark", null) // 기본값은 null로 설정

        return if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<HashSet<Notice>>() {}.type
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
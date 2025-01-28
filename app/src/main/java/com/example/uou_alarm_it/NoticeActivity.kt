package com.example.uou_alarm_it

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.uou_alarm_it.databinding.ActivityNoticeBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeActivity : AppCompatActivity() {
    lateinit var binding : ActivityNoticeBinding
    lateinit var noticeRVAdapter : NoticeRVAdapter

    var bookmarkImportant : HashSet<Notice> = hashSetOf()
    var bookmarkCommon : HashSet<Notice> = hashSetOf()

    var keyWord : String = ""


    companion object {
        var category : Int = 1
        var noticeList : ArrayList<Notice> = arrayListOf()
        var bookmarkList : HashSet<Notice> = hashSetOf()
    }

    var isLast = false
    var page = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        bookmarkList = loadBookmarkList()
        bookmarkList.filter { it.category == "NOTICE"}.toCollection(bookmarkImportant)
        bookmarkList.filter { it.category == "COMMON"}.toCollection(bookmarkCommon)
        bookmarkList = (bookmarkImportant + bookmarkCommon) as HashSet<Notice>

        initAllTab()

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
                noticeSearch(binding.noticeSearchEt.text.toString())

                binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.gray40))
                binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.gray40))

                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(binding.noticeSearchEt.windowToken, 0)
            }
        }
        
        binding.noticeSearchEt.setOnKeyListener { view, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KEYCODE_ENTER) {
                noticeSearch(binding.noticeSearchEt.text.toString())

                binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.gray40))
                binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.gray40))

                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)

                return@setOnKeyListener true
            }
            false
        }

        setContentView(binding.root)
    }

    private fun initAllTab() {
        RetrofitClient.service.getNotice(0,page++).enqueue(object : Callback<GetNoticeRequest>{
            override fun onResponse(
                call: Call<GetNoticeRequest>,
                response: Response<GetNoticeRequest>
            ) {
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result

                    noticeList += res.content
                    Log.d("retrofit_important", res.content.toString())

                    isLast = res.last

                    if (isLast) {
                        page = 0
                        RetrofitClient.service.getNotice(1,page++).enqueue(object : Callback<GetNoticeRequest>{
                            override fun onResponse(
                                call: Call<GetNoticeRequest>,
                                response: Response<GetNoticeRequest>
                            ) {
                                if (response.body()?.code == "COMMON200") {
                                    val res = response.body()!!.result

                                    noticeList += res.content
                                    Log.d("retrofit_all", res.content.toString())
                                    initRV()
                                }
                            }

                            override fun onFailure(call: Call<GetNoticeRequest>, t: Throwable) {
                                Log.e("retrofit", t.toString())
                            }

                        })
                    } else {
                        initAllTab()
                    }
                }
            }

            override fun onFailure(call: Call<GetNoticeRequest>, t: Throwable) {
                Log.e("retrofit", t.toString())
            }

        })
    }

    private fun initImportantTab() {
        RetrofitClient.service.getNotice(0,page++).enqueue(object : Callback<GetNoticeRequest>{
            override fun onResponse(
                call: Call<GetNoticeRequest>,
                response: Response<GetNoticeRequest>
            ) {
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result

                    noticeList += res.content
                    Log.d("retrofit_important", res.content.toString())

                    isLast = res.last

                    if (!isLast) {
                        initImportantTab()
                    } else {
                        initRV()
                    }
                }
            }

            override fun onFailure(call: Call<GetNoticeRequest>, t: Throwable) {
                Log.e("retrofit", t.toString())
            }

        })

    }

    private fun setCategory(category:Int){

        binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.gray40))
        binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.gray40))
        binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.gray40))

        if(category != NoticeActivity.category) {
            noticeList = arrayListOf()
            page = 0
        }

        when (category) {
            1 -> {
                NoticeActivity.category = 1
                binding.noticeTabAll.setTextColor(ContextCompat.getColor(this, R.color.black))
                initAllTab()
            }
            0 -> {
                NoticeActivity.category = 0
                binding.noticeTabImportant.setTextColor(ContextCompat.getColor(this, R.color.black))
                initImportantTab()
            }
            3 -> {
                NoticeActivity.category = 3
                binding.noticeTabBookmark.setTextColor(ContextCompat.getColor(this, R.color.black))
                noticeList = bookmarkList.toCollection(ArrayList())
                Log.d("Bookmark", noticeList.toString())
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
                val intent = Intent(this@NoticeActivity, WebActivity::class.java)
                intent.putExtra("url", notice.link)
                this@NoticeActivity.startActivity(intent)
            }

            override fun onBookmarkClick(notice: Notice) {
                Log.d("test", "Bookmark")
                if (notice in bookmarkList) {
                    bookmarkList.remove(notice)
                    if (notice.category == "COMMON") {
                        bookmarkCommon.remove(notice)
                    } else {
                        bookmarkImportant.remove(notice)
                    }
                }
                else {
                    if (notice.category == "COMMON") {
                        bookmarkCommon.add(notice)
                    } else {
                        bookmarkImportant.add(notice)
                    }

                    bookmarkList = (bookmarkImportant + bookmarkCommon) as HashSet<Notice>
                }
                noticeRVAdapter.bookmarkList = bookmarkList
                saveBookmarkList(bookmarkList)
                Log.d("Save Bookmark", bookmarkList.toString())
            }

        })
        binding.noticeRv.setOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (category == 4) {
                    if(!binding.noticeRv.canScrollVertically(-1)){
                        Log.d("Paging", "Top of list")
                    } else if(!binding.noticeRv.canScrollVertically(1)){
                        noticeSearch(keyWord)
                    }
                }
                else if (category != 3) {
                    if(!binding.noticeRv.canScrollVertically(-1)){
                        Log.d("Paging", "Top of list")
                    } else if(!binding.noticeRv.canScrollVertically(1)){
                        RetrofitClient.service.getNotice(category,page++).enqueue(object : Callback<GetNoticeRequest>{
                            override fun onResponse(
                                call: Call<GetNoticeRequest>,
                                response: Response<GetNoticeRequest>
                            ) {
                                if (response.body()?.code == "COMMON200") {
                                    val res = response.body()!!.result

                                    noticeList += res.content
                                    binding.noticeRv.adapter?.notifyDataSetChanged()
                                    Log.d("Paging", res.content.toString())
                                }
                            }

                            override fun onFailure(call: Call<GetNoticeRequest>, t: Throwable) {
                                Log.e("retrofit", t.toString())
                            }

                        })
                    }
                }
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
            gson.fromJson(json, type)
        } else {
            hashSetOf() // 데이터가 없으면 빈 리스트 반환
        }
    }



    private fun noticeSearch(keyword : String) {
        Log.d("Notice Search", keyword)

        if (keyWord != keyword || category != 4) {
            noticeList = arrayListOf()
            keyWord = keyword
            page = 0
            category = 4
        }

        RetrofitClient.service.getSearch(keyword, page++).enqueue(object : Callback<GetNoticeRequest>{
            override fun onResponse(
                call: Call<GetNoticeRequest>,
                response: Response<GetNoticeRequest>
            ) {
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result

                    noticeList += res.content
                    initRV()

                }
            }

            override fun onFailure(call: Call<GetNoticeRequest>, t: Throwable) {
                Log.e("retrofit", t.toString())
            }

        })

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
package com.example.uou_alarm_it

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.uou_alarm_it.databinding.ActivityNoticeBinding
import com.google.gson.Gson
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.background.BackgroundEventSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.concurrent.TimeUnit

class NoticeActivity : AppCompatActivity() {
    lateinit var binding: ActivityNoticeBinding
    lateinit var noticeRVAdapter: NoticeRVAdapter

    var eventSource: BackgroundEventSource? = null

    var bookmarkImportant: HashSet<Notice> = hashSetOf()
    var bookmarkCommon: HashSet<Notice> = hashSetOf()

    var keyWord: String = ""
    var major: String = "IT융합전공"

    lateinit var setting: Setting

    companion object {
        var category: Int = 1
        var noticeList: ArrayList<Notice> = arrayListOf()
        var bookmarkList: HashSet<Notice> = hashSetOf()
    }

    var isLast = false
    var page = 0
    var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        // 뷰를 먼저 attach합니다.
        setContentView(binding.root)

        setting = loadSetting()
        bookmarkList = loadBookmarkList()
        bookmarkList.filter { it.type == "NOTICE" }.toCollection(bookmarkImportant)
        bookmarkList.filter { it.type == "COMMON" }.toCollection(bookmarkCommon)
        bookmarkList = (bookmarkImportant + bookmarkCommon) as HashSet<Notice>

        initAllTab()

        binding.noticeTabAllIv.setOnClickListener {
            setCategory(1)
        }
        binding.noticeTabImportIv.setOnClickListener {
            setCategory(0)
        }
        binding.noticeTabBookmarkIv.setOnClickListener {
            setCategory(3)
        }

        binding.noticeSearchIv.setOnClickListener {
            if (binding.noticeSearchEt.visibility == View.GONE) {
                animSearch()
            } else {
                noticeSearch(binding.noticeSearchEt.text.toString())

                binding.noticeTabAllIv.setImageResource(R.drawable.btn_tab_all_on)
                binding.noticeTabImportIv.setImageResource(R.drawable.btn_tab_import_off)
                binding.noticeTabBookmarkIv.setImageResource(R.drawable.btn_tab_bookmark_off)

                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(binding.noticeSearchEt.windowToken, 0)
            }
        }

        binding.noticeSearchEt.setOnKeyListener { view, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KEYCODE_ENTER) {
                noticeSearch(binding.noticeSearchEt.text.toString())

                binding.noticeTabAllIv.setImageResource(R.drawable.btn_tab_all_on)
                binding.noticeTabImportIv.setImageResource(R.drawable.btn_tab_import_off)
                binding.noticeTabBookmarkIv.setImageResource(R.drawable.btn_tab_bookmark_off)

                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)

                return@setOnKeyListener true
            }
            false
        }

        binding.noticeSearchEt.setTextCursorDrawable(R.drawable.edittext_cusor)

        binding.noticeCloseSearchIv.setOnClickListener {
            animSearch()
        }

        initNotification()

        binding.noticeNoticeIv.setOnClickListener {
            setting.notificationSetting = !setting.notificationSetting
            saveSetting(setting)
            initNotification()
        }

//        // 팝업을 위한 dropdownView 인플레이트 및 설정
//        val dropdownView = layoutInflater.inflate(R.layout.notice_dropdown, null)
//        val popupWindow = PopupWindow(
//            dropdownView,
//            ConstraintLayout.LayoutParams.MATCH_PARENT,
//            ConstraintLayout.LayoutParams.WRAP_CONTENT,
//            true
//        )
//
//        // 드롭다운 내 각 뷰 초기화
//        val ictTextView = dropdownView.findViewById<TextView>(R.id.dropdown_ict_tv)
//        val itTextView = dropdownView.findViewById<TextView>(R.id.dropdown_it_tv)
//        val itItTextView = dropdownView.findViewById<TextView>(R.id.dropdown_it_it_tv)
//        val itAiTextView = dropdownView.findViewById<TextView>(R.id.dropdown_it_ai_tv)
//        val itDownBtn = dropdownView.findViewById<ImageView>(R.id.dropdown_it_down_btn_iv)
//
//        // IT 하위 옵션은 처음에 숨깁니다.
//        itItTextView.visibility = View.GONE
//        itAiTextView.visibility = View.GONE
//
//        // ICT 클릭 -> 팝업 종료 처리
//        ictTextView.setOnClickListener {
//            // ICT 선택 처리 (예: 상태 저장, UI 업데이트 등)
//            popupWindow.dismiss()
//        }
//
//        // IT 클릭 -> 하위 옵션 토글 처리
//        itTextView.setOnClickListener {
//            if (itItTextView.visibility == View.GONE) {
//                itItTextView.visibility = View.VISIBLE
//                itAiTextView.visibility = View.VISIBLE
//                itDownBtn.setImageResource(R.drawable.btn_it_dropdown_up)
//            } else {
//                itItTextView.visibility = View.GONE
//                itAiTextView.visibility = View.GONE
//                itDownBtn.setImageResource(R.drawable.btn_it_dropdown_down)
//            }
//        }
//
//        // IT융합학과 클릭 -> 팝업 종료
//        itItTextView.setOnClickListener {
//            // IT융합학과 선택 처리
//            popupWindow.dismiss()
//        }
//
//        // AI융합학과 클릭 -> 팝업 종료
//        itAiTextView.setOnClickListener {
//            // AI융합학과 선택 처리
//            popupWindow.dismiss()
//        }
//
//        // 팝업 외부 터치를 위한 오버레이 클릭 시 팝업 종료
//        binding.overlayView.setOnClickListener { popupWindow.dismiss() }
//
//        // binding.noticeLogoIv가 attach된 후에 팝업을 표시하도록 post()를 사용합니다.
//        binding.noticeLogoIv.setOnClickListener {
//            // 팝업이 이미 열려있다면 닫고, 아니면 열기
//            if (!popupWindow.isShowing) {
//                popupWindow.showAsDropDown(binding.noticeLogoIv)
//            } else {
//                popupWindow.dismiss()
//            }
//        }
    }

    private fun initNotification() {
        if (setting.notificationSetting) {
            binding.noticeNoticeIv.setImageResource(R.drawable.notice_on)
            connectNotification()
        } else {
            binding.noticeNoticeIv.setImageResource(R.drawable.notice_off)
            unConnectNotification()
        }
    }

    private fun initAllTab() {
        // 탭이 변경될 때 페이지와 리스트 초기화
        page = 0
        noticeList.clear()

        RetrofitClient.service.getNotice(0, page++, major).enqueue(object : Callback<GetNoticeResponse> {
            override fun onResponse(call: Call<GetNoticeResponse>, response: Response<GetNoticeResponse>) {
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result
                    noticeList.addAll(res.content)
                    initRV()  // RecyclerView 어댑터를 초기화해서 화면에 첫 페이지 데이터를 표시
                }
            }
            override fun onFailure(call: Call<GetNoticeResponse>, t: Throwable) {
                Log.e("retrofit", t.toString())
            }
        })
    }

    private fun initImportantTab() {
        // 중요 탭일 경우에도 동일하게 한 페이지만 초기 로딩
        page = 0
        noticeList.clear()

        RetrofitClient.service.getNotice(0, page++, major).enqueue(object : Callback<GetNoticeResponse> {
            override fun onResponse(call: Call<GetNoticeResponse>, response: Response<GetNoticeResponse>) {
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result
                    noticeList.addAll(res.content.filter { it.type == "NOTICE" })
                    initRV()  // RecyclerView 초기화
                }
            }
            override fun onFailure(call: Call<GetNoticeResponse>, t: Throwable) {
                Log.e("retrofit", t.toString())
            }
        })
    }

    private fun setCategory(category: Int) {
        binding.noticeTabAllIv.setImageResource(R.drawable.btn_tab_all_off)
        binding.noticeTabImportIv.setImageResource(R.drawable.btn_tab_import_off)
        binding.noticeTabBookmarkIv.setImageResource(R.drawable.btn_tab_bookmark_off)

        if (category != NoticeActivity.category) {
            noticeList = arrayListOf()
            page = 0
        }

        when (category) {
            1 -> {
                NoticeActivity.category = 1
                binding.noticeTabAllIv.setImageResource(R.drawable.btn_tab_all_on)
                initAllTab()
            }
            0 -> {
                NoticeActivity.category = 0
                binding.noticeTabImportIv.setImageResource(R.drawable.btn_tab_import_on)
                initImportantTab()
            }
            3 -> {
                NoticeActivity.category = 3
                binding.noticeTabBookmarkIv.setImageResource(R.drawable.btn_tab_bookmark_on)
                noticeList = bookmarkList.toCollection(ArrayList())
                Log.d("Bookmark", noticeList.toString())
                initRV()
            }
        }
    }

    fun initRV() {
        noticeRVAdapter = NoticeRVAdapter()
        binding.noticeRv.adapter = noticeRVAdapter
        noticeRVAdapter.setMyClickListener(object : NoticeRVAdapter.MyClickListener {
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
                    if (notice.type == "NOTICE") {
                        bookmarkCommon.remove(notice)
                    } else {
                        bookmarkImportant.remove(notice)
                    }
                } else {
                    if (notice.type == "NOTICE") {
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
        binding.noticeRv.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                // 스크롤이 멈췄을 때 (IDLE 상태) 처리하도록 변경
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (category == 4) {
                        // 검색 탭의 경우
                        if (!binding.noticeRv.canScrollVertically(-1)) {
                            Log.d("Paging", "Top of list")
                        } else if (!binding.noticeRv.canScrollVertically(1)) {
                            if (!isLoading) {
                                isLoading = true
                                noticeSearch(keyWord)
                            }
                        }
                    } else if (category != 3) {
                        if (!binding.noticeRv.canScrollVertically(-1)) {
                            Log.d("Paging", "Top of list")
                        } else if (!binding.noticeRv.canScrollVertically(1)) {
                            if (!isLoading) {
                                isLoading = true
                                RetrofitClient.service.getNotice(category, page++, major)
                                    .enqueue(object : Callback<GetNoticeResponse> {
                                        override fun onResponse(
                                            call: Call<GetNoticeResponse>,
                                            response: Response<GetNoticeResponse>
                                        ) {
                                            isLoading = false
                                            if (response.body()?.code == "COMMON200") {
                                                val res = response.body()!!.result
                                                // 만약 중요 탭(category == 0)이라면 type이 "NOTICE"인 항목만 추가
                                                val newItems = if (category == 0) {
                                                    res.content.filter { it.type == "NOTICE" }
                                                } else {
                                                    res.content
                                                }
                                                noticeList.addAll(newItems)
                                                binding.noticeRv.adapter?.notifyDataSetChanged()
                                                Log.d("Paging", newItems.toString())
                                            }
                                        }
                                        override fun onFailure(call: Call<GetNoticeResponse>, t: Throwable) {
                                            isLoading = false
                                            Log.e("retrofit", t.toString())
                                        }
                                    })
                            }
                        }
                    }
                }
            }
        })
        binding.noticeRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    fun saveBookmarkList(BookmarkList: HashSet<Notice>) {
        val sharedPreferences = this.getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(BookmarkList)
        editor.putString("Bookmark", json)
        editor.apply()
    }

    fun loadBookmarkList(): HashSet<Notice> {
        val sharedPreferences = this.getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("Bookmark", null)
        return if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<HashSet<Notice>>() {}.type
            gson.fromJson(json, type)
        } else {
            hashSetOf()
        }
    }

    private fun noticeSearch(keyword: String) {
        Log.d("Notice Search", keyword)

        if (keyWord != keyword || category != 4) {
            noticeList = arrayListOf()
            keyWord = keyword
            page = 0
            category = 4
            initRV()
        }

        // 이미 로딩 중이면 중복 호출 방지
        if (isLoading) return
        isLoading = true

        RetrofitClient.service.getSearch(keyword, major, page++).enqueue(object : Callback<GetNoticeResponse> {
            override fun onResponse(
                call: Call<GetNoticeResponse>,
                response: Response<GetNoticeResponse>
            ) {
                isLoading = false
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result
                    noticeList.addAll(res.content)
                    binding.noticeRv.adapter?.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: Call<GetNoticeResponse>, t: Throwable) {
                isLoading = false
                Log.e("retrofit", t.toString())
            }
        })
    }

    override fun onBackPressed() {
        if (binding.noticeSearchEt.visibility == View.VISIBLE) {
            animSearch()
        } else {
            super.onBackPressed()
        }
    }

    private fun animSearch() {
        if (binding.noticeSearchEt.visibility == View.VISIBLE) {
            val animation = AnimationUtils.loadAnimation(this, R.anim.anim_search_close)
            animation.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                    binding.noticeCloseSearchIv.visibility = View.GONE
                    binding.noticeSearchIv.visibility = View.VISIBLE

                }
                override fun onAnimationEnd(p0: Animation?) {
                    binding.noticeNoticeIv.visibility = View.VISIBLE
                    binding.noticeSearchEt.visibility = View.GONE
                    binding.noticeSearchEt.setText("")
                }
                override fun onAnimationRepeat(p0: Animation?) {}
            })
            binding.noticeSearchEt.startAnimation(animation)
            Log.d("anim", "close")
        } else {
            val animation = AnimationUtils.loadAnimation(this, R.anim.anim_search_open)
            animation.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                    binding.noticeSearchEt.visibility = View.VISIBLE
                    binding.noticeNoticeIv.visibility = View.GONE
                    binding.noticeCloseSearchIv.visibility = View.VISIBLE
                    binding.noticeSearchIv.visibility = View.GONE

                }
                override fun onAnimationEnd(p0: Animation?) {
                }
                override fun onAnimationRepeat(p0: Animation?) {}
            })
            binding.noticeSearchEt.visibility = View.VISIBLE
            binding.noticeSearchEt.startAnimation(animation)
            Log.d("anim", "open")
        }
    }

    private fun connectNotification() {
        eventSource = BackgroundEventSource.Builder(
            SSEService(this),
            EventSource.Builder(
                ConnectStrategy.http(URL("https://alarm-it.ulsan.ac.kr:58080/subscribe"))
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .readTimeout(600, TimeUnit.SECONDS)
            )
        )
            .threadPriority(Thread.MAX_PRIORITY)
            .build()
        eventSource?.start()
    }

    private fun unConnectNotification() {
        try {
            eventSource?.close()
        } catch (e: Exception) {
            Log.e("SSEService", "오류 발생: ${e.message}")
        } finally {
            eventSource = null
        }
    }

    fun saveSetting(setting: Setting) {
        val sharedPreferences = this.getSharedPreferences("Setting", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(setting)
        editor.putString("Setting", json)
        editor.apply()
    }

    fun loadSetting(): Setting {
        val sharedPreferences = this.getSharedPreferences("Setting", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("Setting", null)
        return if (json != null) {
            gson.fromJson(json, Setting::class.java)
        } else {
            Setting(true)
        }
    }
}
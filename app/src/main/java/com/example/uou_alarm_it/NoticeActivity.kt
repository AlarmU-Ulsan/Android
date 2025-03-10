package com.example.uou_alarm_it

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.messaging.FirebaseMessaging
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
        const val REQUEST_CODE_MAJOR = 100
    }

    var isLast = false
    var page = 0
    var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        // 뷰를 먼저 attach합니다.
        setContentView(binding.root)

        var link = ""
        intent?.extras?.let{
            link = it.getString("link") ?:""
            if (link != "") {
                Log.d("noticeFCM", link)
                val intent = Intent(this, WebActivity::class.java).apply {
                    putExtra("url", link)  // 알림에 포함된 데이터 전송
                }
                startActivity(intent)
            }
        }

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

        binding.noticeSearchCl.setOnClickListener {
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

        binding.noticeSearchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                // 검색어가 비면 기본 조회로 복원
                if(query.isEmpty()){
                    when(category){
                        1 -> initAllTab()
                        0 -> initImportantTab()
                        3 -> {
                            noticeList = bookmarkList.toCollection(ArrayList())
                            initRV()
                            updateEmptyState()
                        }
                    }
                } else {
                    noticeSearch(query)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.noticeCloseSearchIv.setOnClickListener {
            animSearch()
        }

        initNotification()

        binding.noticeNoticeCl.setOnClickListener {
            setting.notificationSetting = !setting.notificationSetting
            saveSetting(setting)
            initNotification()
        }
        binding.noticeSelectBtnLl.setOnClickListener {
            val intent = Intent(this, MajorActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_MAJOR)
        }
        updateEmptyState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAJOR && resultCode == RESULT_OK) {
            // MajorActivity에서 전달한 선택된 아이템의 텍스트 데이터 받기
            val selectedText = data?.getStringExtra("selectedItem")
            Log.d("NoticeActivity", "Selected item: $selectedText")
            binding.noticeSelectedMajorTv.text = selectedText
            if (!selectedText.isNullOrEmpty()) {
                major = selectedText
                unConnectNotification()
                connectNotification()
            }
            // 현재 탭(모든 API 호출 함수들에서 major 변수 사용)이 다시 새로고침되도록 재설정
            setCategory(category)
        }
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

    private fun updateEmptyState() {
        if (noticeList.isEmpty()) {
            binding.noticeEmptyLogoIv.visibility = View.VISIBLE
            binding.noticeRv.visibility = View.GONE
        } else {
            binding.noticeEmptyLogoIv.visibility = View.GONE
            binding.noticeRv.visibility = View.VISIBLE
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
                    updateEmptyState()
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
                    updateEmptyState()
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
                updateEmptyState()
            }
            0 -> {
                NoticeActivity.category = 0
                binding.noticeTabImportIv.setImageResource(R.drawable.btn_tab_import_on)
                initImportantTab()
                updateEmptyState()
            }
            3 -> {
                NoticeActivity.category = 3
                binding.noticeTabBookmarkIv.setImageResource(R.drawable.btn_tab_bookmark_on)
                noticeList = bookmarkList.toCollection(ArrayList())
                Log.d("Bookmark", noticeList.toString())
                initRV()
                updateEmptyState()
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
                                                updateEmptyState()
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

        // 만약 이전 검색어와 달라졌다면 리스트와 페이지를 초기화
        if (keyWord != keyword) {
            noticeList = arrayListOf()
            keyWord = keyword
            page = 0
            initRV()
        }

        // 검색어가 비었으면 현재 탭의 기본 조회로 복원
        if (keyword.isEmpty()) {
            when (NoticeActivity.category) {
                1 -> initAllTab()
                0 -> initImportantTab()
                3 -> {
                    noticeList = bookmarkList.toCollection(ArrayList())
                    initRV()
                    updateEmptyState()
                }
            }
            return
        }

        // 북마크 탭일 경우, 로컬 필터링 진행
        if (NoticeActivity.category == 3) {
            val filtered = bookmarkList.filter {
                it.title.contains(keyword, ignoreCase = true)
            }
            noticeList = filtered.toCollection(ArrayList())
            initRV()
            updateEmptyState()
            return
        }

        // 이미 로딩 중이면 중복 호출 방지
        if (isLoading) return
        isLoading = true

        // 전체 탭(1)와 중요 탭(0)의 경우 API 호출
        RetrofitClient.service.getSearch(keyword, major, page++).enqueue(object : Callback<GetNoticeResponse> {
            override fun onResponse(call: Call<GetNoticeResponse>, response: Response<GetNoticeResponse>) {
                isLoading = false
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result
                    // 중요 탭이면 type이 "NOTICE"인 게시물만 선택
                    val newItems = if (NoticeActivity.category == 0) {
                        res.content.filter { it.type == "NOTICE" }
                    } else {
                        res.content
                    }
                    noticeList.addAll(newItems)
                    binding.noticeRv.adapter?.notifyDataSetChanged()
                    updateEmptyState()
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
                    binding.noticeCloseSearchCl.visibility = View.GONE
                    binding.noticeSearchIv.visibility = View.VISIBLE
                    binding.noticeSearchCl.visibility = View.VISIBLE
                    binding.noticeSelectBtnLl.visibility = View.VISIBLE
                }
                override fun onAnimationEnd(p0: Animation?) {
                    binding.noticeNoticeIv.visibility = View.VISIBLE
                    binding.noticeNoticeCl.visibility = View.VISIBLE
                    binding.noticeSearchEt.visibility = View.GONE
                    binding.noticeSearchEt.setText("")
                    // 검색 취소 시 기본 조회로 복원 (예: AllTab, 카테고리 1)
                    if (category == 4) {
                        setCategory(1)
                    }
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
                    binding.noticeNoticeCl.visibility = View.GONE
                    binding.noticeCloseSearchIv.visibility = View.VISIBLE
                    binding.noticeCloseSearchCl.visibility = View.VISIBLE
                    binding.noticeSearchIv.visibility = View.GONE
                    binding.noticeSearchCl.visibility = View.GONE
                    binding.noticeSelectBtnLl.visibility = View.GONE
                }
                override fun onAnimationEnd(p0: Animation?) {}
                override fun onAnimationRepeat(p0: Animation?) {}
            })
            binding.noticeSearchEt.visibility = View.VISIBLE
            binding.noticeSearchEt.startAnimation(animation)
            Log.d("anim", "open")
        }
    }

    private fun getFcmToken(){
        var token = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "FCM 토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }
            else {
                token = task.result.toString()
                Log.d("FCM", "FCM 토큰: $token")
                RetrofitClient.service.postFCMRegister(token, major).enqueue(object: Callback<PostFCMRegisterResponse>{
                    override fun onResponse(
                        call: Call<PostFCMRegisterResponse>,
                        response: Response<PostFCMRegisterResponse>
                    ) {
                        Log.d("FCM", "FCM 연결 성공")
                    }

                    override fun onFailure(call: Call<PostFCMRegisterResponse>, t: Throwable) {
                        Log.e("FCM", "FCM 연결 실패" + t)
                    }

                })
            }
        }
    }
    private fun deleteFCMToken() {
        var token = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "FCM 토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }
            else {
                token = task.result.toString()
                Log.d("FCM", "FCM 토큰: $token")
                RetrofitClient.service.deleteFCMUnregister(token, major).enqueue(object: Callback<PostFCMRegisterResponse>{
                    override fun onResponse(
                        call: Call<PostFCMRegisterResponse>,
                        response: Response<PostFCMRegisterResponse>
                    ) {
                        Log.d("FCM", "FCM 연결 해제 성공")
                    }

                    override fun onFailure(call: Call<PostFCMRegisterResponse>, t: Throwable) {
                        Log.e("FCM", "FCM 연결 해제 실패" + t)
                    }

                })
            }
        }
    }

    private fun connectNotification() {
        eventSource = BackgroundEventSource.Builder(
            SSEService(this),
            EventSource.Builder(
                ConnectStrategy.http(URL("https://alarm-it.ulsan.ac.kr:58080/notification/subscribe?major=$major"))
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .readTimeout(600, TimeUnit.SECONDS)
            )
        )
            .threadPriority(Thread.MAX_PRIORITY)
            .build()
        eventSource?.start()
        getFcmToken()
    }

    private fun unConnectNotification() {
        try {
            eventSource?.close()
        } catch (e: Exception) {
            Log.e("SSEService", "오류 발생: ${e.message}")
        } finally {
            eventSource = null
        }
        deleteFCMToken()
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
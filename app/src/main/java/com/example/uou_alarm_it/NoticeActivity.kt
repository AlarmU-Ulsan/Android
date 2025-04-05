package com.example.uou_alarm_it

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
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

    lateinit var setting: Setting

    var bookmarkImportant: HashSet<Notice> = hashSetOf()
    var bookmarkCommon: HashSet<Notice> = hashSetOf()

    var keyWord: String = ""
    var major: String = "ICT융합학부" // 기본값

    var isLast = false
    var page = 0
    var isLoading = false

    private var customNotificationView: View? = null
    private val notificationDuration = 5000L // 5초
    private val notificationHandler = Handler(Looper.getMainLooper())
    private var notificationRunnable: Runnable? = null


    companion object {
        var category: Int = 1
        var noticeList: ArrayList<Notice> = arrayListOf()
        var bookmarkList: HashSet<Notice> = hashSetOf()
        const val REQUEST_CODE_MAJOR = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 알림 링크 처리 (생략)
        var link = ""
        intent.extras?.let {
            link = it.getString("link") ?: ""
            if (link.isNotEmpty()) {
                Log.d("NoticeActivity", "알림 링크: $link")
                val webIntent = Intent(this, WebActivity::class.java).apply {
                    putExtra("url", link)
                }
                startActivity(webIntent)
            }
        }

        // 기존 저장된 Setting을 불러옵니다.
        setting = loadSetting()

        // SharedPreferences에서 "selected_major"와 최초 실행 플래그("isFirstNoticeRun")를 읽어옵니다.
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val spMajor = sharedPref.getString("selected_major", null)
        val isFirstRun = sharedPref.getBoolean("isFirstNoticeRun", true)
        val defaultMajor = "ICT융합학부"

        // Intent extra "major" 값
        val intentMajor = intent.getStringExtra("major")

        // 최초 실행 시에만 Intent extra "major"를 적용하고 플래그를 false로 업데이트
        major = if (isFirstRun && !intentMajor.isNullOrEmpty() && setting.notificationMajor == defaultMajor) {
            sharedPref.edit().putString("selected_major", intentMajor)
                .putBoolean("isFirstNoticeRun", false)
                .apply()
            intentMajor
        } else {
            spMajor ?: setting.notificationMajor
        }

        // 업데이트된 major 값을 Setting에 반영하고 저장
        setting.notificationMajor = major
        saveSetting(setting)

        Log.d("NoticeActivity", "최종 major 값: $major")
        binding.noticeSelectedMajorTv.text = major

        // 즐겨찾기 리스트 불러오기 등 나머지 로직은 그대로 진행합니다.
        bookmarkList = loadBookmarkList()
        bookmarkList.filter { it.type == "NOTICE" }.toCollection(bookmarkImportant)
        bookmarkList.filter { it.type == "COMMON" }.toCollection(bookmarkCommon)
        bookmarkList = (bookmarkImportant + bookmarkCommon) as HashSet<Notice>

        initAllTab()

        binding.noticeTabAllIv.setOnClickListener { setCategory(1) }
        binding.noticeTabImportIv.setOnClickListener { setCategory(0) }
        binding.noticeTabBookmarkIv.setOnClickListener { setCategory(3) }

        binding.noticeSearchCl.setOnClickListener {
            if (binding.noticeSearchBarCl.visibility == View.GONE) {
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
                if (query.isEmpty()) {
                    when (category) {
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

        binding.noticeCloseSearchIv.setOnClickListener { animSearch() }

        initNotification()

        binding.noticeNoticeCl.setOnClickListener {
            setting.notificationSetting = !setting.notificationSetting
            setting.notificationMajor = major
            saveSetting(setting)
            initNotification()
        }
        binding.noticeSelectBtnLl.setOnClickListener {
            val intent = Intent(this, MajorActivity::class.java).apply {
                putExtra("currentSelectedMajor", major)  // NoticeActivity에 저장된 전공 변수
            }
            startActivityForResult(intent, REQUEST_CODE_MAJOR)
        }
        updateEmptyState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAJOR && resultCode == RESULT_OK) {
            val selectedText = data?.getStringExtra("selectedItem")
            // 기존 전공 값을 임시로 보관
            val previousMajor = major

            // 새 전공이 비어있지 않으면 처리
            if (!selectedText.isNullOrEmpty()) {
                binding.noticeSelectedMajorTv.text = selectedText
                major = selectedText
                setting.notificationMajor = major
                saveSetting(setting)
                // SharedPreferences "selected_major" 업데이트
                val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                sharedPref.edit().putString("selected_major", major).apply()

                unConnectNotification()
                connectNotification()
                setCategory(category)

                // 전공이 실제로 변경되었을 때만 다이얼로그 표시
                if (selectedText != previousMajor) {
                    showCustomNotification()
                }
            }
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

    // 전공 변경 다이얼로그 위치 설정을 위한 dp단위 변환 함수
    fun Context.dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    private fun showCustomNotification() {
        // 만약 이미 다이얼로그가 표시 중이라면 먼저 슬라이드 아웃 애니메이션으로 제거 후 새 다이얼로그 표시
        if (customNotificationView != null) {
            // 기존에 예약된 hide 콜백 취소
            notificationRunnable?.let { notificationHandler.removeCallbacks(it) }
            // 기존 뷰에 슬라이드 아웃 애니메이션 적용 후 제거
            val oldView = customNotificationView
            val slideOut = AnimationUtils.loadAnimation(this, R.anim.anim_slide_out_bottom)
            slideOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    (oldView?.parent as? ViewGroup)?.removeView(oldView)
                    customNotificationView = null
                    // 이전 다이얼로그가 완전히 제거된 후 새 다이얼로그 표시
                    actuallyShowCustomNotification()
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            oldView?.startAnimation(slideOut)
        } else {
            actuallyShowCustomNotification()
        }
    }

    private fun actuallyShowCustomNotification() {
        // 1) 레이아웃 인플레이트
        customNotificationView = layoutInflater.inflate(R.layout.dialog_change_major, null)
        val rootView = findViewById<ViewGroup>(android.R.id.content)

        // 2) 위치와 마진 설정 (하단, 좌우 12dp, 아래쪽 82dp)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
            bottomMargin = dpToPx(82f)
            leftMargin = dpToPx(12f)
            rightMargin = dpToPx(12f)
        }
        rootView.addView(customNotificationView, params)

        // 3) 슬라이드 인 애니메이션 시작
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_bottom)
        customNotificationView?.startAnimation(slideIn)

        // 4) 기존에 예약된 hide 콜백 제거 후, 5초 후 hideCustomNotification() 호출 예약
        notificationRunnable?.let { notificationHandler.removeCallbacks(it) }
        notificationRunnable = Runnable { hideCustomNotification() }
        notificationHandler.postDelayed(notificationRunnable!!, notificationDuration)
    }

    private fun hideCustomNotification() {
        customNotificationView?.let { view ->
            val slideOut = AnimationUtils.loadAnimation(this, R.anim.anim_slide_out_bottom)
            slideOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    (view.parent as? ViewGroup)?.removeView(view)
                    customNotificationView = null
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            view.startAnimation(slideOut)
        }
        notificationRunnable?.let { notificationHandler.removeCallbacks(it) }
        notificationRunnable = null
    }

    private fun initAllTab() {
        page = 0
        noticeList.clear()
        RetrofitClient.service.getNotice(0, page++, major).enqueue(object : Callback<GetNoticeResponse> {
            override fun onResponse(call: Call<GetNoticeResponse>, response: Response<GetNoticeResponse>) {
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result
                    noticeList.addAll(res.content)
                    initRV()
                    updateEmptyState()
                }
            }
            override fun onFailure(call: Call<GetNoticeResponse>, t: Throwable) {
                Log.e("retrofit", t.toString())
            }
        })
    }

    private fun initImportantTab() {
        page = 0
        noticeList.clear()
        RetrofitClient.service.getNotice(0, page++, major).enqueue(object : Callback<GetNoticeResponse> {
            override fun onResponse(call: Call<GetNoticeResponse>, response: Response<GetNoticeResponse>) {
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result
                    noticeList.addAll(res.content.filter { it.type == "NOTICE" })
                    initRV()
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
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (category == 4) {
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
                                        override fun onResponse(call: Call<GetNoticeResponse>, response: Response<GetNoticeResponse>) {
                                            isLoading = false
                                            if (response.body()?.code == "COMMON200") {
                                                val res = response.body()!!.result
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
        if (keyWord != keyword) {
            noticeList = arrayListOf()
            keyWord = keyword
            page = 0
            initRV()
        }
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
        if (NoticeActivity.category == 3) {
            val filtered = bookmarkList.filter {
                it.title.contains(keyword, ignoreCase = true)
            }
            noticeList = filtered.toCollection(ArrayList())
            initRV()
            updateEmptyState()
            return
        }
        if (isLoading) return
        isLoading = true
        RetrofitClient.service.getSearch(keyword, major, page++).enqueue(object : Callback<GetNoticeResponse> {
            override fun onResponse(call: Call<GetNoticeResponse>, response: Response<GetNoticeResponse>) {
                isLoading = false
                if (response.body()?.code == "COMMON200") {
                    val res = response.body()!!.result
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
        if (binding.noticeSearchBarCl.visibility == View.VISIBLE) {
            val animation = AnimationUtils.loadAnimation(this, R.anim.anim_search_close)
            animation.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                    binding.noticeCloseSearchIv.visibility = View.GONE
                    binding.noticeCloseSearchCl.visibility = View.GONE
                    binding.noticeSearchIv.visibility = View.VISIBLE
                    binding.noticeSearchCl.visibility = View.VISIBLE
                }
                override fun onAnimationEnd(p0: Animation?) {
                    binding.noticeSearchBarCl.visibility = View.GONE
                    binding.noticeTabLl.visibility = View.VISIBLE
                    binding.noticeLineView.visibility = View.VISIBLE
                    binding.noticeSearchEt.setText("")
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
                    binding.noticeSearchBarCl.visibility = View.VISIBLE
                    binding.noticeCloseSearchIv.visibility = View.VISIBLE
                    binding.noticeCloseSearchCl.visibility = View.VISIBLE
                    binding.noticeSearchIv.visibility = View.GONE
                    binding.noticeSearchCl.visibility = View.GONE
                    binding.noticeTabLl.visibility = View.GONE
                    binding.noticeLineView.visibility = View.GONE
                }
                override fun onAnimationEnd(p0: Animation?) {}
                override fun onAnimationRepeat(p0: Animation?) {}
            })
            binding.noticeSearchBarCl.visibility = View.VISIBLE
            binding.noticeSearchBarCl.startAnimation(animation)
            Log.d("anim", "open")
        }
    }

    private fun connectNotification() {
        var token = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "FCM 토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            } else {
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
    private fun unConnectNotification() {
        var token = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "FCM 토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            } else {
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
            Setting(true, "ICT융합학부")
        }
    }
}
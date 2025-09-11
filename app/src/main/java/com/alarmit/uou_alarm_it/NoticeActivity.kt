package com.alarmit.uou_alarm_it

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
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
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.alarmit.uou_alarm_it.databinding.ActivityNoticeBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeActivity : AppCompatActivity(), SettingInterface {
    lateinit var binding: ActivityNoticeBinding
    lateinit var noticeRVAdapter: NoticeRVAdapter
    lateinit var setting: Setting

    var bookmarkImportant: HashSet<Notice> = hashSetOf()
    var bookmarkCommon: HashSet<Notice> = hashSetOf()

    var keyWord: String = ""
    var major: String = "ICT융합학부" // 기본값

    companion object {
        var category: Int = 1
        var noticeList: ArrayList<Notice> = arrayListOf()
        var bookmarkList: HashSet<Notice> = hashSetOf()
        const val REQUEST_CODE_MAJOR = 100
    }

    var isLast = false
    var page = 0
    var isLoading = false

    // 커스텀 알림 관련 변수...
    private var customNotificationView: View? = null
    private val notificationDuration = 5000L // 5초
    private val notificationHandler = Handler(Looper.getMainLooper())
    private var notificationRunnable: Runnable? = null

    private var layoutManagerState: Parcelable? = null

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

        // 기존 저장된 Setting 불러오기
        setting = loadSetting(this)

        // SharedPreferences에서 "selected_major"와 초기 플로우 완료 플래그("isInitialFlowComplete")를 읽어옵니다.
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val spMajor = sharedPref.getString("selected_major", null)
        val isInitialFlowComplete = sharedPref.getBoolean("isInitialFlowComplete", false)
        val defaultMajor = "ICT융합학부"

        // Intent extra "major"
        val intentMajor = intent.getStringExtra("major")

        // 만약 초기 플로우가 완료되지 않았다면 다시 FirstNoticeChoiceActivity로 돌아갑니다.
        if (!isInitialFlowComplete && intentMajor.isNullOrEmpty()) {
            // 초기 플로우 미완료: FirstNoticeChoiceActivity부터 시작
            val startIntent = Intent(this, FirstNoticeChoiceActivity::class.java)
            startActivity(startIntent)
            finish()
            return
        }

        // 초기 플로우가 완료되었거나 Intent extra가 있는 경우:
        // 최초 실행 시 Intent extra가 있으면 우선 적용 (그리고 플래그는 이미 true)
        major = if (!intentMajor.isNullOrEmpty() && setting.noiceMajor == defaultMajor) {
            intent.removeExtra("major")
            intentMajor
        } else {
            spMajor ?: setting.noiceMajor
        }

        // 업데이트된 major 값을 setting에 반영하고 저장
        setting.noiceMajor = major
        saveSetting(this, setting)

        Log.d("NoticeActivity", "최종 major 값: $major")
        binding.noticeSelectedMajorTv.text = major

        // 즐겨찾기, 공지사항 조회, 탭, 검색 등의 나머지 로직은 기존 코드와 동일하게 실행
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
            // 예시: AlarmChoiceActivity 실행
            val intent = Intent(this, AlarmChoiceActivity::class.java)
            startActivity(intent)
        }
        binding.noticeSelectBtnLl.setOnClickListener {
            val intent = Intent(this, MajorActivity::class.java).apply {
                putExtra("currentSelectedMajor", major)
            }
            startActivityForResult(intent, REQUEST_CODE_MAJOR)
        }
        updateEmptyState()
    }

    override fun onResume() {
        super.onResume()
        setting = loadSetting(this)
        initNotification()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAJOR && resultCode == RESULT_OK) {
            val selectedText = data?.getStringExtra("selectedItem") ?: return
            val previousMajor = major
            if (selectedText != previousMajor) {
                // 1) 텍스트, 변수, SharedPreferences 즉시 업데이트
                binding.noticeSelectedMajorTv.text = selectedText
                major = selectedText
                getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                    .edit().putString("selected_major", major).apply()

                // 2) 알림 다이얼로그 먼저 표시
                showCustomNotification()

                // 3) 그 다음에 탭 새로고침
                setCategory(category)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        layoutManagerState = binding.noticeRv.layoutManager?.onSaveInstanceState()
        outState.putParcelable("layout_manager_state", layoutManagerState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        layoutManagerState = savedInstanceState.getParcelable("layout_manager_state")
    }

    private fun initNotification() {
        if (setting.alarmSetting) {
            binding.noticeNoticeIv.setImageResource(R.drawable.notice_on)
        } else {
            binding.noticeNoticeIv.setImageResource(R.drawable.notice_off)
        }
    }

    private fun updateEmptyState() {
        if (noticeList.isEmpty()) {
            binding.noticeRv.visibility = View.GONE
            if (category == 3) {
                // 북마크 탭이면 텍스트만 보여줌
                binding.noticeEmptyTextTv.visibility = View.VISIBLE
                binding.noticeEmptyTextTv.text = "북마크된 공지가 없습니다"
            } else {
                // 나머지 탭은 이미지 보여줌
                binding.noticeEmptyTextTv.visibility = View.VISIBLE
                binding.noticeEmptyTextTv.text = "공지가 없습니다"
            }
        } else {
            binding.noticeRv.visibility = View.VISIBLE
            binding.noticeEmptyTextTv.visibility = View.GONE
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
            bottomMargin = dpToPx(12f)
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
        RetrofitClient.service.getNotice(0, page++, major.replace("·", "")).enqueue(object : Callback<GetNoticeResponse> {
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
        RetrofitClient.service.getNotice(0, page++, major.replace("·", "")).enqueue(object : Callback<GetNoticeResponse> {
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

        // ✅ 스크롤 복원은 RecyclerView 렌더링이 끝난 뒤에 해야 정확하게 동작
        layoutManagerState?.let { state ->
            binding.noticeRv.post {
                binding.noticeRv.layoutManager?.onRestoreInstanceState(state)
                layoutManagerState = null
            }
        }

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
                                RetrofitClient.service.getNotice(category, page++, major.replace("·", ""))
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
        RetrofitClient.service.getSearch(keyword, major.replace("·", ""), page++).enqueue(object : Callback<GetNoticeResponse> {
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
        if (binding.noticeSearchBarCl.visibility == View.VISIBLE) {
            animSearch()
        } else {
            super.onBackPressed()
            finish()
        }
    }

    private fun animSearch() {
        if (binding.noticeSearchBarCl.visibility == View.VISIBLE) {
            // 검색창 닫기
            binding.noticeSearchBarCl.visibility = View.GONE
            binding.noticeCloseSearchIv.visibility = View.GONE
            binding.noticeCloseSearchCl.visibility = View.GONE
            binding.noticeSearchIv.visibility = View.VISIBLE
            binding.noticeSearchCl.visibility = View.VISIBLE
            binding.noticeTabLl.visibility = View.VISIBLE
            binding.noticeLineView.visibility = View.VISIBLE
            binding.noticeSearchEt.setText("")

            if (category == 4) {
                setCategory(1)
            }

            Log.d("anim", "close")
        } else {
            // 검색창 열기
            binding.noticeSearchBarCl.visibility = View.VISIBLE
            binding.noticeCloseSearchIv.visibility = View.VISIBLE
            binding.noticeCloseSearchCl.visibility = View.VISIBLE
            binding.noticeSearchIv.visibility = View.GONE
            binding.noticeSearchCl.visibility = View.GONE
            binding.noticeTabLl.visibility = View.GONE
            binding.noticeLineView.visibility = View.GONE

            Log.d("anim", "open")
        }
    }
}
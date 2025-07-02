package com.example.uou_alarm_it

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.uou_alarm_it.databinding.ActivitySplashBinding
import com.google.firebase.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity(), UpdateDialogInterface, SettingInterface {
    private lateinit var binding: ActivitySplashBinding

    companion object {
        const val DEVICE_ID_KEY = "device_id"
        const val PREF_NAME = "app_preferences"
        const val APP_FLOW_TAG = "AppFlow"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 현재 앱 버전 표시
        val version = packageManager.getPackageInfo(packageName, 0).versionName.toString()
        binding.splashVersionTv.text = version

        var link = ""

        // 기기 고유 ID 가져오기 및 저장
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        if (deviceId.isNullOrEmpty()) {
            Log.e(APP_FLOW_TAG, "ANDROID_ID 없음.")
            Log.d("deviceId", "device 정보 없음")
            finish()
            return
        } else {
            Log.d(APP_FLOW_TAG, "기기 ID: $deviceId")
            val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            sharedPref.edit().putString(DEVICE_ID_KEY, deviceId).apply()
        }

        // ✅ FCM 토큰 발급 및 서버 전송
        setFCM(this)

        RetrofitClient.service.getVersion().enqueue(object : Callback<GetVersionResponse> {
            override fun onResponse(
                call: Call<GetVersionResponse>,
                response: Response<GetVersionResponse>
            ) {
                if (response.code() == 200 && response.body()?.result != null) {
                    if (response.body()!!.result.latestVersion == version) {
                        // 버전이 최신이면
                        intent?.extras?.let {
                            link = it.getString("link") ?: ""
                        }

                        // SharedPreferences를 통해 최초 실행 여부 체크
                        val sharedPref = getSharedPreferences("app_preferences", MODE_PRIVATE)
                        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)

                        // 2초 딜레이 후 다음 액티비티 전환
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (isFirstRun) {
                                // 최초 실행이면, flag 업데이트 후 FirstNoticeChoiceActivity로 이동
                                sharedPref.edit().putBoolean("isFirstRun", false).apply()
                                val intent = Intent(this@SplashActivity, FirstNoticeChoiceActivity::class.java)
                                intent.putExtra("link", link)
                                startActivity(intent)
                            } else {
                                // 최초 실행이 아니면 NoticeActivity로 이동
                                val intent = Intent(this@SplashActivity, NoticeActivity::class.java)
                                intent.putExtra("link", link)
                                startActivity(intent)
                            }
                            finish()
                        }, 2000)
                    } else {
                        // 최신 버전이 아닐 경우, 업데이트 다이얼로그 띄우기
                        val updateLink = response.body()!!.result.link
                        val lastVersion = response.body()!!.result.latestVersion
                        val dialog = UpdateDialog(this@SplashActivity, updateLink, lastVersion, version)
                        dialog.isCancelable = false
                        dialog.show(supportFragmentManager, "UpdateDialog")
                    }
                } else {
                    // 네트워크 에러 다이얼로그 처리 등 추가 로직
                }
            }

            override fun onFailure(call: Call<GetVersionResponse>, t: Throwable) {
                // 네트워크 에러 처리 로직
            }
        })
    }

    override fun onClickYes(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
        ActivityCompat.finishAffinity(this)
        System.exit(0)
    }

    override fun onClickNo() {
        ActivityCompat.finishAffinity(this)
        System.exit(0)
    }
}
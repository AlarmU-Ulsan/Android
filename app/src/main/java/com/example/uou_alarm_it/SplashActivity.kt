package com.example.uou_alarm_it

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
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
                Log.d(APP_FLOW_TAG, "버전 API 응답 코드: ${response.code()}")

                if (response.code() == 200 && response.body()?.result != null) {
                    val serverVersion = response.body()!!.result.latestVersion
                    Log.d(APP_FLOW_TAG, "서버 최신 버전: $serverVersion")

                    if (serverVersion <= version) {
                        // 버전 최신일 때
                        intent?.extras?.let {
                            link = it.getString("link") ?: ""
                        }

                        val sharedPref = getSharedPreferences("app_preferences", MODE_PRIVATE)
                        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (isFirstRun) {
                                sharedPref.edit().putBoolean("isFirstRun", false).apply()
                                val intent = Intent(this@SplashActivity, FirstNoticeChoiceActivity::class.java)
                                intent.putExtra("link", link)
                                startActivity(intent)
                            } else {
                                val intent = Intent(this@SplashActivity, NoticeActivity::class.java)
                                intent.putExtra("link", link)
                                startActivity(intent)
                            }
                            finish()
                        }, 2000)
                    } else {
                        // 업데이트 필요
                        val updateLink = response.body()!!.result.link
                        val dialog = UpdateDialog(this@SplashActivity, updateLink, serverVersion, version)
                        dialog.isCancelable = false
                        dialog.show(supportFragmentManager, "UpdateDialog")
                    }
                } else {
                    Log.e(APP_FLOW_TAG, "버전 정보 없음 또는 코드 200 아님")
                    Toast.makeText(this@SplashActivity, "버전 확인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    moveToFallbackScreen()
                }
            }

            override fun onFailure(call: Call<GetVersionResponse>, t: Throwable) {
                Log.e(APP_FLOW_TAG, "버전 API 호출 실패: ${t.message}")
                Toast.makeText(this@SplashActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                moveToFallbackScreen()
            }
        })
    }

    private fun moveToFallbackScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, NoticeActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
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
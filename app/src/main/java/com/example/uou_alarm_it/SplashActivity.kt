package com.example.uou_alarm_it

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.uou_alarm_it.databinding.ActivitySplashBinding
import com.google.firebase.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity(), UpdateDialogInterface {
    lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var link = ""

        val version = this.packageManager.getPackageInfo(this.packageName, 0).versionName.toString()
        binding.splashVersionTv.text = version.toString()

        RetrofitClient.service.getVersion().enqueue(object : Callback<GetVersionResponse> {
            override fun onResponse(
                call: Call<GetVersionResponse>,
                response: Response<GetVersionResponse>
            ) {
                if(response.code() == 200 && response.body()?.result != null) {
                    if (response.body()!!.result.latestVersion == version) {
                        // 버전이 최신 버전일 때, 알림 클릭 or 메인 화면 이동
                        intent?.extras?.let{
                            link = it.getString("link") ?:""
                        }

                        android.os.Handler().postDelayed({
                            Log.d("splashFCM", link)
                            val intent = Intent(this@SplashActivity, NoticeActivity::class.java).apply {
                                putExtra("link", link)  // 알림에 포함된 데이터 전송
                            }
                            startActivity(intent)
                            finish()
                        },2000)
                    } else {
                        // 버전이 최신 버전이 아닐 때, 업데이트 다이얼로그 띄우기
                        // url 설정 필요
//                        val url = response.body()!!.result.url
                        val lastVersion = response.body()!!.result.latestVersion
                        val dialog = UpdateDialog(this@SplashActivity, "https://www.naver.com", lastVersion, version)
                        dialog.isCancelable = false
                        dialog.show(this@SplashActivity.supportFragmentManager, "UpdateDialog")
                    }
                } else {
                    // 네트워크 에러 다이얼 로그 표시하기
                }
            }

            override fun onFailure(call: Call<GetVersionResponse>, t: Throwable) {
                Log.e("GetVersion", t.toString())
                // 네트워크 에러 다이얼 로그 표시하기
            }

        })
    }

    override fun onClickYes(url: String) {
        // url로 드라이브 이동
        val browserIntent  = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
        ActivityCompat.finishAffinity(this)
        System.exit(0)
    }

    override fun onClickNo() {
        ActivityCompat.finishAffinity(this)
        System.exit(0)
    }


}
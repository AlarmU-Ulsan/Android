package com.example.uou_alarm_it

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface SettingInterface{
    fun loadSetting(context: Context): Setting {
        val sharedPreferences = context.getSharedPreferences("Setting", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("Setting", null)
        return if (json != null) {
            gson.fromJson(json, Setting::class.java)
        } else {
            Setting(true, "ICT융합학부")
        }
    }

    fun saveSetting(context: Context, setting: Setting) {
        val sharedPreferences = context.getSharedPreferences("Setting", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(setting)

        editor.putString("Setting", json)
        editor.apply()
    }

    fun changeSetting(context: Context): Boolean{
        var setting = loadSetting(context)
        setting.notificationSetting = !setting.notificationSetting

        saveSetting(context, setting)
        setFCM(setting)

        return setting.notificationSetting
    }

    fun changeMajor(context: Context, majors: String) {
        val setting = loadSetting(context)

        // 기존 알림 해제
        setFCM(Setting(false, setting.notificationMajor))

        // 알림 전공 변경 (콤마로 구분된 복수 전공)
        setting.notificationMajor = majors
        setFCM(setting)

        saveSetting(context, setting)
    }

    fun setFCM(setting: Setting) {
        var token = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "FCM 토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            } else {
                token = task.result.toString()
                Log.d("FCM", "FCM 토큰: $token")

                if (setting.notificationSetting) {
                    RetrofitClient.service.postFCMRegister(token, setting.notificationMajor).enqueue(object:
                        Callback<PostFCMTokenResponse> {
                        override fun onResponse(
                            call: Call<PostFCMTokenResponse>,
                            response: Response<PostFCMTokenResponse>
                        ) {
                            Log.d("FCM", "FCM 연결 성공")
                        }
                        override fun onFailure(call: Call<PostFCMTokenResponse>, t: Throwable) {
                            Log.e("FCM", "FCM 연결 실패" + t)
                        }
                    })
                }
                else {
                    RetrofitClient.service.deleteFCMUnregister(token, setting.notificationMajor).enqueue(object:
                        Callback<PostFCMResponse> {
                        override fun onResponse(
                            call: Call<PostFCMResponse>,
                            response: Response<PostFCMResponse>
                        ) {
                            Log.d("FCM", "FCM 연결 해제 성공")
                        }
                        override fun onFailure(call: Call<PostFCMResponse>, t: Throwable) {
                            Log.e("FCM", "FCM 연결 해제 실패" + t)
                        }
                    })
                }
            }
        }
    }

}
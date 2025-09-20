package com.alarmit.uou_alarm_it

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

        val sh = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val deviceId = sh.getString("device_id", "")

        return if (json != null) {
            gson.fromJson(json, Setting::class.java)
        } else {
            Setting(deviceId!!,"ICT융합학부",true, arrayListOf(), false)
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
        setting.alarmSetting = !setting.alarmSetting

        saveSetting(context, setting)
        setFCM(context)

        return setting.alarmSetting
    }

    fun postFCM(deviceId: String, token: String) {
        val request = PostFCMTokenRequest(
            deviceId = deviceId,
            fcmToken = token
        )
        RetrofitClient.service.postFCMToken(request).enqueue(object : Callback<PostFCMResponse> {
            override fun onResponse(
                call: Call<PostFCMResponse>,
                response: Response<PostFCMResponse>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("FCM/SettingInterface", "FCM 등록 성공: ${response.body()?.message}")
                } else {
                    Log.e("FCM/SettingInterface", "FCM 등록 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<PostFCMResponse>, t: Throwable) {
                Log.e("FCM/SettingInterface", "FCM 등록 실패: ${t.message}")
            }
        })
    }

    fun setFCM(context: Context) {
        var token = ""
        var setting = loadSetting(context)

        if(setting.alarmSetting){
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM/SettingInterface", "FCM 토큰 가져오기 실패", task.exception)
                    return@addOnCompleteListener
                } else {
                    token = task.result.toString()
                    Log.d("FCM/SettingInterface", "FCM 토큰: $token")

                    postFCM(setting.deviceId, token)
                    setting.FCM = true
                    saveSetting(context, setting)
                    Log.d("FCM/SettingInterface", "알림 연결 완료")
                }
            }
        } else {
            deleteFCM(setting.deviceId)
            setting.FCM = false
            saveSetting(context, setting)
            Log.d("FCM/SettingInterface", "알림 해제 완료")
        }
    }

    fun deleteFCM(deviceId: String){
        RetrofitClient.service.deleteFCMToken(deviceId).enqueue(object : Callback<PostFCMResponse> {
            override fun onResponse(
                call: Call<PostFCMResponse>,
                response: Response<PostFCMResponse>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("FCM/SettingInterface", "FCM 해제 성공: ${response.body()?.message}")
                } else {
                    Log.e("FCM/SettingInterface", "FCM 해제 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<PostFCMResponse>, t: Throwable) {
                Log.e("FCM/SettingInterface", "FCM 해제 실패: ${t.message}")
            }
        })
    }

    fun postFCMSub(deviceId: String, major: String) {
        val request = FCMSubscribeRequest(
            deviceId = deviceId,
            major = major
        )

        RetrofitClient.service.postFCMRegister(request).enqueue(object:
            Callback<PostFCMSubscribeResponse> {
            override fun onResponse(
                call: Call<PostFCMSubscribeResponse>,
                response: Response<PostFCMSubscribeResponse>
            ) {
                Log.d("FCM/SettingInterface", "FCM 전공 연결 성공: " + response.body()?.code.toString())
            }
            override fun onFailure(call: Call<PostFCMSubscribeResponse>, t: Throwable) {
                Log.e("FCM/SettingInterface", "FCM 전공 연결 실패: " + t)
            }
        })
    }

    fun deleteFCMSub(deviceId: String, major: String) {
        val request = FCMSubscribeRequest(
            deviceId = deviceId,
            major = major
        )

        RetrofitClient.service.deleteFCMUnregister(request).enqueue(object:
            Callback<PostFCMResponse> {
            override fun onResponse(
                call: Call<PostFCMResponse>,
                response: Response<PostFCMResponse>
            ) {
                Log.d("FCM/SettingInterface", "FCM 전공 연결 해제 성공: " + response.body()?.code.toString())
            }
            override fun onFailure(call: Call<PostFCMResponse>, t: Throwable) {
                Log.e("FCM/SettingInterface", "FCM 전공 연결 해제 실패: " + t)
            }
        })
    }


    fun changeMajor(context: Context, majors: List<String>) {
        val setting = loadSetting(context)
        val post_majors = setting.alarmMajor.toList()

        for(major in post_majors) {
            // 원래 데이터에 있는 게 최신 데이터에 없다면 -> 삭제
            if (!majors.contains(major)) {
                deleteFCMSub(setting.deviceId, major)
                setting.alarmMajor.remove(major)
            }
        }
        for (major in majors) {
            // 원래 데이터에 없던 게 생겼다면 -> 추가
            if (!post_majors.contains(major)) {
                postFCMSub(setting.deviceId, major)
                setting.alarmMajor.add(major)
            }
        }

        saveSetting(context, setting)
    }

}
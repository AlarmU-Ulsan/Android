package com.uou.alarmit

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface SettingInterface {
    fun loadSetting(context: Context): Setting {
        val sharedPreferences = context.getSharedPreferences("Setting", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("Setting", null)

        val appPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val deviceId = appPreferences.getString("device_id", "").orEmpty()

        return if (json != null) {
            gson.fromJson(json, Setting::class.java)
        } else {
            Setting(deviceId, "ICT융합학부", true, "", false)
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

    fun changeSetting(context: Context): Boolean {
        val setting = loadSetting(context)
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
                    Log.d("FCM/SettingInterface", "FCM token registered: ${response.body()?.message}")
                } else {
                    Log.e("FCM/SettingInterface", "FCM token registration failed(${response.code()}): ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<PostFCMResponse>, t: Throwable) {
                Log.e("FCM/SettingInterface", "FCM token registration failed: ${t.message}")
            }
        })
    }

    fun setFCM(context: Context) {
        val setting = loadSetting(context)

        if (setting.alarmSetting) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM/SettingInterface", "Failed to fetch FCM token", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result.orEmpty()
                Log.d("FCM/SettingInterface", "FCM token: $token")

                if (token.isBlank()) {
                    Log.w("FCM/SettingInterface", "FCM token is blank")
                    return@addOnCompleteListener
                }

                postFCM(setting.deviceId, token)

                val apiMajor = MajorApiMapper.toApiMajor(setting.alarmMajor)
                if (apiMajor != null) {
                    postFCMSub(setting.deviceId, apiMajor)
                } else {
                    Log.d("FCM/SettingInterface", "Skipping subscribe because no valid alarm major is selected: ${setting.alarmMajor}")
                }

                setting.FCM = true
                saveSetting(context, setting)
                Log.d("FCM/SettingInterface", "FCM setup finished")
            }
        } else {
            val apiMajor = MajorApiMapper.toApiMajor(setting.alarmMajor)
            if (apiMajor != null) {
                deleteFCMSub(setting.deviceId, apiMajor)
            }

            deleteFCM(setting.deviceId)
            setting.FCM = false
            saveSetting(context, setting)
            Log.d("FCM/SettingInterface", "FCM disabled")
        }
    }

    fun syncAlarmSubscription(context: Context, previousMajor: String, newMajor: String) {
        val setting = loadSetting(context)
        if (!setting.alarmSetting || setting.deviceId.isBlank()) return

        val previousApiMajor = MajorApiMapper.toApiMajor(previousMajor)
        val newApiMajor = MajorApiMapper.toApiMajor(newMajor)

        if (previousApiMajor != null && previousApiMajor != newApiMajor) {
            deleteFCMSub(setting.deviceId, previousApiMajor)
        }

        if (newApiMajor == null) {
            Log.d("FCM/SettingInterface", "Skipping subscribe because no valid alarm major is selected")
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM/SettingInterface", "Failed to refresh FCM token after major change", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result.orEmpty()
            if (token.isBlank()) {
                Log.w("FCM/SettingInterface", "Blank FCM token after major change")
                return@addOnCompleteListener
            }

            postFCM(setting.deviceId, token)
            postFCMSub(setting.deviceId, newApiMajor)
        }
    }

    fun deleteFCM(deviceId: String) {
        val request = DeleteFCMTokenRequest(deviceId)
        RetrofitClient.service.deleteFCMToken(request).enqueue(object : Callback<PostFCMResponse> {
            override fun onResponse(call: Call<PostFCMResponse>, response: Response<PostFCMResponse>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("FCM/SettingInterface", "FCM token deleted: ${response.body()?.message}")
                } else {
                    Log.e("FCM/SettingInterface", "FCM token delete failed(${response.code()}): ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<PostFCMResponse>, t: Throwable) {
                Log.e("FCM/SettingInterface", "FCM token delete failed: ${t.message}")
            }
        })
    }

    fun postFCMSub(deviceId: String, major: String) {
        val apiMajor = MajorApiMapper.toApiMajor(major)
        if (apiMajor == null) {
            Log.w("FCM/SettingInterface", "Skipping subscribe because major is unsupported: $major")
            return
        }

        val request = FCMSubscribeRequest(
            deviceId = deviceId,
            major = apiMajor
        )

        RetrofitClient.service.postFCMRegister(request).enqueue(object : Callback<PostFCMSubscribeResponse> {
            override fun onResponse(
                call: Call<PostFCMSubscribeResponse>,
                response: Response<PostFCMSubscribeResponse>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("FCM/SettingInterface", "FCM subscribe success: ${request.deviceId}, ${request.major}")
                } else {
                    Log.e("FCM/SettingInterface", "FCM subscribe failed(${response.code()}): ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<PostFCMSubscribeResponse>, t: Throwable) {
                Log.e("FCM/SettingInterface", "FCM subscribe failed: $t")
            }
        })
    }

    fun deleteFCMSub(deviceId: String, major: String) {
        val apiMajor = MajorApiMapper.toApiMajor(major)
        if (apiMajor == null) {
            Log.w("FCM/SettingInterface", "Skipping unsubscribe because major is unsupported: $major")
            return
        }

        val request = FCMSubscribeRequest(
            deviceId = deviceId,
            major = apiMajor
        )

        RetrofitClient.service.deleteFCMUnregister(request).enqueue(object : Callback<PostFCMResponse> {
            override fun onResponse(
                call: Call<PostFCMResponse>,
                response: Response<PostFCMResponse>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("FCM/SettingInterface", "FCM unsubscribe success: ${request.deviceId}, ${request.major}")
                } else {
                    Log.e("FCM/SettingInterface", "FCM unsubscribe failed(${response.code()}): ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<PostFCMResponse>, t: Throwable) {
                Log.e("FCM/SettingInterface", "FCM unsubscribe failed: $t")
            }
        })
    }
}

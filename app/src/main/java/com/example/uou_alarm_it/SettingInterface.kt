package com.example.uou_alarm_it

import android.content.Context
import com.google.gson.Gson

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

    private fun saveSetting(context: Context, setting: Setting) {
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

        return setting.notificationSetting
    }

    fun changeMajor(context: Context, major: String) {
        var setting = loadSetting(context)
        setting.notificationMajor = major

        saveSetting(context, setting)
    }
}
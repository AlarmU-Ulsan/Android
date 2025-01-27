package com.example.uou_alarm_it

import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager

class RetrofitClient {

    companion object {
        private const val BASE_URL = "https://alarm-it.githyeon.shop/" // url

        var builder = OkHttpClient().newBuilder()
        var okHttpClient = builder
            .cookieJar(JavaNetCookieJar(CookieManager()))
            .build()


        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: RetrofitService = retrofit.create(RetrofitService::class.java)
    }

}
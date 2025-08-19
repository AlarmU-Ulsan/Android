package com.example.uou_alarm_it

import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import com.example.uou_alarm_it.BuildConfig

class RetrofitClient {

    companion object {

        // Retrofit baseUrl은 반드시 슬래시로 끝나야 합니다.
        private fun ensureSlash(url: String) =
            if (url.endsWith("/")) url else "$url/"

        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .cookieJar(JavaNetCookieJar(CookieManager()))
                .build()
        }

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(ensureSlash(BuildConfig.BASE_URL)) // ✅ 빌드 시 주입된 값 사용
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val service: RetrofitService by lazy { retrofit.create(RetrofitService::class.java) }
    }
}
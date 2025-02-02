package com.example.uou_alarm_it

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.*
import java.io.IOException

class SSEService(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val okHttpClient = OkHttpClient()

    override suspend fun doWork(): Result {
        val request = Request.Builder()
            .url(RetrofitClient.BASE_URL + "subscribe")
            .get()
            .build()

        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("SSEService 연결 실패","${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("SSEService", "code :" + response.code.toString() + " / message :" + response.message)
                if (response.code == 200){
                    Log.d("SSEService 데이터", response.body.toString())
                }

            }
        })

        return Result.success()
    }
}
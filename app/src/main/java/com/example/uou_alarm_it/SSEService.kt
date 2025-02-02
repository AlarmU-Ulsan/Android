package com.example.uou_alarm_it

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.*
import java.io.IOException

class SSEService(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val request = Request.Builder()
            .url(RetrofitClient.BASE_URL + "/subscribe")
            .get()
            .build()

        val call = RetrofitClient.okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("SSEWorker 연결 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.byteStream()?.bufferedReader()?.useLines { lines ->
                    lines.forEach { line ->
                        println("SSEWorker 받은 데이터: $line")
                    }
                }
            }
        })

        return Result.success()
    }
}
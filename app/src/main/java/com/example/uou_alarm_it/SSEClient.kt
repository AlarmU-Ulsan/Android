package com.example.uou_alarm_it

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

interface EventSource {
    fun onOpen(eventSource: EventSource?, response: Response)
    fun onEvent(eventSource: EventSource?, id: String?, type: String?, data: String?)
    fun onClosed(eventSource: EventSource?)
    fun onFailure(eventSource: EventSource?, t: Throwable?, response: Response?)
}

class SSEClient(private val context: Context, private val client: OkHttpClient) {
    private val sseUrl = "https://alarm-it.githyeon.shop/subscribe"

    fun connectToSSE() {
        val request = Request.Builder()
            .url(sseUrl)
            .build()

        val eventSource = object : EventSource {
            override fun onOpen(eventSource: EventSource?, response: Response) {
                Log.d("SSEClient", "Connection opened")
            }

            override fun onEvent(eventSource: EventSource?, id: String?, type: String?, data: String?) {
                Log.d("SSEClient", "Event received: ID=$id, Type=$type, Data=$data")
            }

            override fun onClosed(eventSource: EventSource?) {
                Log.d("SSEClient", "Connection closed")
            }

            override fun onFailure(eventSource: EventSource?, t: Throwable?, response: Response?) {
                Log.e("SSEClient", "Connection failed: ${t?.message}")
            }
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SSEClient", "Failed to connect: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("SSEClient", "Successful response")
                    eventSource.onOpen(null, response)
                } else {
                    Log.e("SSEClient", "Error response: ${response.code}")
                }
            }
        })
    }
}
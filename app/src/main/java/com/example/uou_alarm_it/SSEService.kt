package com.example.uou_alarm_it

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler

class SSEService(context: Context) : BackgroundEventHandler {

    private val CHANNEL_ID = "sse_channel_id"
    private val context = context

    init {
        createNotificationChannel(context)
    }

    override fun onOpen() {
        // SSE 연결 성공 시 처리 로직 작성
        Log.d("SSEService", "SSE 연결 성공")
        // 예: 연결 상태를 UI에 반영하거나 알림을 표시할 수 있음
    }

    override fun onClosed() {
        // SSE 연결 종료 시 처리 로직 작성
        Log.d("SSEService", "SSE 연결 종료")
        // 예: 연결 종료 후 재연결 시도를 하거나 알림을 업데이트할 수 있음
    }

    override fun onMessage(event: String, messageEvent: MessageEvent) {
        // SSE 이벤트 도착 시 처리 로직 작성
        Log.d("SSEService", "Received event: $event")
        Log.d("SSEService", "Data: ${messageEvent.data}")

        // 이벤트 데이터 처리
        // 예: 받은 데이터를 파싱하거나 화면에 표시하는 등의 작업을 수행할 수 있음
        showNotification(messageEvent.data, context)
    }

    override fun onComment(comment: String) {
        // 서버에서 보내는 주석을 처리하는 로직
        // 주석은 보통 데이터 전송이 아닌 로그 메시지이므로 필요한 경우만 사용
        Log.d("SSEService", "Received comment: $comment")
    }

    override fun onError(t: Throwable) {
        // 오류 발생 시 처리 로직 작성
        Log.e("SSEService", "SSE 오류 발생: ${t.message}")

        // 오류의 종류에 따라 적절히 처리
        when (t) {
            is com.launchdarkly.eventsource.StreamHttpErrorException -> {
                // HTTP 오류 처리, 예: 401 Unauthorized
                Log.e("SSEService", "HTTP 오류 발생: ${t.code}")
            }
            is java.net.SocketTimeoutException -> {
                // 타임아웃 오류 처리
                Log.e("SSEService", "타임아웃 오류 발생")
            }
            is com.launchdarkly.eventsource.StreamClosedByServerException -> {
                // 서버에 의해 스트림이 종료된 경우 처리
                Log.e("SSEService", "서버에 의해 스트림 종료")
            }
            else -> {
                Log.e("SSEService", "알 수 없는 오류: ${t.message}")
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SSE Notification Channel"
            val descriptionText = "Channel for SSE notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // 시스템에 채널 등록
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 알림을 생성하고 표시하는 메서드
    private fun showNotification(message: String, context: Context) {
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // 알림 아이콘 설정
            .setContentTitle("새로운 알림")  // 알림 제목
            .setContentText(message)  // 알림 내용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // 우선순위 설정
            .setAutoCancel(true)  // 사용자가 알림을 클릭하면 자동으로 알림이 사라짐

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notificationBuilder.build())  // 알림 표시
    }
}
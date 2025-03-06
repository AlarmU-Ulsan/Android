package com.example.uou_alarm_it

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler

class SSEService(context: Context) : BackgroundEventHandler {

    private val CHANNEL_ID = "sse_channel_id"
    private val context = context
    val Icon128 = BitmapFactory.decodeResource(context.resources, R.drawable.icon128)
    private val GROUP_KEY_NOTICES = "group_key_notices"

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
        val data = Gson().fromJson(messageEvent.data, Notification::class.java)
        showNotification(data, context)
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

        retrySSEConnection()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "공지 알림"
            val descriptionText = "공지 알림"
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
    private fun showNotification(data: Notification, context: Context) {
        val intent = Intent(context, WebActivity::class.java).apply {
            putExtra("url", data.link)  // 알림에 포함된 데이터 전송
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,  // requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = System.currentTimeMillis().toInt()

        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notice_icon)
            .setContentTitle("울산대학교 알림it")
            .setStyle(NotificationCompat.InboxStyle()
                .setSummaryText("공지 알림")
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup(GROUP_KEY_NOTICES)  // 🔥 동일한 그룹 키
            .setGroupSummary(true)  // 📌 요약 알림 활성화
            .build()

        notificationManager.notify(0, summaryNotification)  // 알림 표시

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notice_icon)
            .setContentTitle(data.title)  // 알림 제목
            .setContentText(data.body)  // 알림 내용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // 우선순위 설정
            .setAutoCancel(true)  // 사용자가 알림을 클릭하면 자동으로 알림이 사라짐
            .setContentIntent(pendingIntent)  // 알림 클릭 시 실행될 PendingIntent 설정
            .setGroup(GROUP_KEY_NOTICES)  // 🔥 그룹 키 설정
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun retrySSEConnection() {
        // 일정 시간 후 재시도
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("SSEService", "재시도 중...")
            // 재시도 코드: 다시 SSE 연결을 시도하는 로직을 구현
        }, 5000) // 5초 후 재시도
    }
}
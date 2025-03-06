package com.example.uou_alarm_it

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

class FCMService(context: Context) : FirebaseMessagingService(){

    private val CHANNEL_ID = "sse_channel_id"
    private val context = context
    val Icon128 = BitmapFactory.decodeResource(context.resources, R.drawable.icon128)
    private val GROUP_KEY_NOTICES = "group_key_notices"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {
            // SSE 이벤트 도착 시 처리 로직 작성
            Log.d("SSEService", remoteMessage.data["tile"].toString())
            Log.d("SSEService", remoteMessage.data["body"].toString())

            // 이벤트 데이터 처리
            // 예: 받은 데이터를 파싱하거나 화면에 표시하는 등의 작업을 수행할 수 있음
//            val data = Gson().fromJson(remoteMessage.data["tile"], Notification::class.java)
//            showNotification(data, context)
        }
    }

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
            .setLargeIcon(Icon128)
            .setContentTitle("알림it")  // 알림 제목
            .setContentText("새 공지가 올라왔어요! \""+data.title+"\"")  // 알림 내용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // 우선순위 설정
            .setAutoCancel(true)  // 사용자가 알림을 클릭하면 자동으로 알림이 사라짐
            .setContentIntent(pendingIntent)  // 알림 클릭 시 실행될 PendingIntent 설정
            .setGroup(GROUP_KEY_NOTICES)  // 🔥 그룹 키 설정
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
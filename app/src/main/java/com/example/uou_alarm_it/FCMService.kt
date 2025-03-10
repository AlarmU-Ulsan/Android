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


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCMService", remoteMessage.data.toString())
    }
}
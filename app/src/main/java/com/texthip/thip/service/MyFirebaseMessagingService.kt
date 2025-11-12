package com.texthip.thip.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.texthip.thip.MainActivity
import com.texthip.thip.R
import com.texthip.thip.data.manager.FcmTokenManager
import com.texthip.thip.data.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    @Inject
    lateinit var notificationRepository: NotificationRepository

    companion object {
        private const val TAG = "FCM"
        private const val CHANNEL_ID = "thip_notifications"
        private const val CHANNEL_NAME = "THIP 알림"
        private const val CHANNEL_DESCRIPTION = "THIP 앱의 푸시 알림"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


        // 푸시 알림 도착 시 알림 상태 새로고침 (비차단 방식)
        try {
            notificationRepository.onNotificationReceived()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trigger notification refresh", e)
        }

        // Data payload 처리
        val dataPayload = remoteMessage.data
        if (dataPayload.isNotEmpty()) {
            Log.d(TAG, "Message data payload: $dataPayload")
        }

        val title = remoteMessage.notification?.title ?: "THIP"
        val body = remoteMessage.notification?.body ?: "새로운 알림이 있습니다"

        Log.d(TAG, "App is in foreground, showing custom notification: title=$title, body=$body")
        showNotification(title, body, dataPayload)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // FCM 토큰 관리자를 통해 처리
        CoroutineScope(Dispatchers.IO).launch {
            fcmTokenManager.handleNewToken(token)
        }
    }

    private fun showNotification(
        title: String?,
        messageBody: String?,
        dataPayload: Map<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // FCM 데이터를 Intent에 추가
            dataPayload["notificationId"]?.let { notificationId ->
                putExtra("notification_id", notificationId)
                putExtra("from_notification", true)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(), // 고유한 requestCode 사용
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title ?: "THIP")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId =
            dataPayload["notificationId"]?.toIntOrNull() ?: System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
            setShowBadge(true)
            enableLights(true)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
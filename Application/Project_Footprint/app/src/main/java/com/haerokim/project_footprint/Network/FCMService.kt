package com.haerokim.project_footprint.Network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.haerokim.project_footprint.Activity.HomeActivity
import com.haerokim.project_footprint.R

/**
 *  Firebase Cloud Messaging을 위한 Service Class
 *  - 차후 추가 기능을 위해 이식만 했음 (작동 확인 후 기능 구현 보류)
 **/

open class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 메시지에 데이터가 있으면 진입
        if (remoteMessage != null) {
            Log.d(TAG, "From: " + remoteMessage.from)
            // 데이터 메시지인 경우
            if (remoteMessage.data.isNotEmpty()) {
                sendNotification(remoteMessage.data)
                Log.d(TAG, "Message data payload: " + remoteMessage.data)
            }
            // 알림 메시지인 경우
            if (remoteMessage.notification != null) {
                val remoteMessageData = mapOf(
                    "title" to remoteMessage.notification?.title.toString(),
                    "msg" to remoteMessage.notification?.body.toString()
                )
                sendNotification(remoteMessageData)
                Log.d(TAG, "Message Notification Body: " + remoteMessage.notification?.body)
            }
        }
    }

    private fun sendNotification(msgData: Map<String, String>) {
        // RequestCode, Id를 고유값으로 지정하여 알림이 개별 표시되도록 함
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // PendingIntent : Intent 의 실행 권한을 외부의 어플리케이션에게 위임
        val pendingIntent =
            PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.firebase_notification_channel_id)    // Notice
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(msgData.getValue("title"))
            .setContentText(msgData.getValue("msg"))
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Oreo 8.0 이후 Notification 채널 지원
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        notificationManager.notify(uniId, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed Token: $token")

        if (token != null) {
            sendRegistrationToServer(token)
        }
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d("FCM","sendRegistrationTokenToServer($token)")
    }
}

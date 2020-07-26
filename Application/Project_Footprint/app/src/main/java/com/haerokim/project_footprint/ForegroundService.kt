package com.haerokim.project_footprint

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.haerokim.project_footprint.ui.home.HomeFragment

class ForegroundService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val clsIntent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, clsIntent, 0)
        val clsBuilder: NotificationCompat.Builder
        clsBuilder = if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "channel_foreground"
            val clsChannel =
                NotificationChannel(CHANNEL_ID, "서비스 앱", NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                clsChannel
            )
            NotificationCompat.Builder(this, CHANNEL_ID)

        } else {
            NotificationCompat.Builder(this)
        }
        clsBuilder.setSmallIcon(R.drawable.ic_baseline_location_on_24)
            .setContentTitle("당신의 발자취를 따라가는 중").setContentText("오늘도 좋은 하루 되세요 :)")
            .setContentIntent(pendingIntent)

        // Foreground 서비스로 실행한다
        startForeground(1, clsBuilder.build())

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
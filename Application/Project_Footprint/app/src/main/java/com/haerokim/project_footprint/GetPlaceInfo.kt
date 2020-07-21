package com.haerokim.project_footprint

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException


class GetPlaceInfo(var context: Context, var place: String) : AsyncTask<Void, Void, Void>() {
    private var placeName = place

    override fun onPreExecute() {
        super.onPreExecute()
        Log.d("async_test", "AsyncTask 시작")
    }

    override fun doInBackground(vararg params: Void?): Void? {
        lateinit var placeTitle: String
        lateinit var placeCategory: String
        lateinit var placeDescription: String
        lateinit var placeTime: String
        lateinit var placeImageSrc: String

        try {
            var doc: Document =
                Jsoup.connect("https://search.naver.com/search.naver?query=$placeName").get()
            var titleElement: Elements = doc.select("div[class=biz_name_area]").select("a")
            var categoryElement: Elements = doc.select("div[class=biz_name_area]").select("span")
            var descriptionElement: Elements = doc.select("div[class=info] div").select("span")
            var timeElement: Elements = doc.select("div[class=biztime] span").select("span")
            var imageElement: Elements = doc.select("div[class=thumb_area] a").select("img")

            //Description, Time은 Null일 수도 있음

            Log.d("HTML_title", titleElement[0].text())
            Log.d("HTML_category", categoryElement[0].text())
            Log.d("HTML_description", descriptionElement[0].text())
            Log.d("HTML_time", timeElement[0].text())
            Log.d("HTML_time", imageElement[0].toString())


            placeTitle = titleElement[0].text()
            placeCategory = categoryElement[0].text()
            placeDescription = descriptionElement[0].text()
            placeTime = timeElement[0].text()
            placeImageSrc = imageElement[0].attr("src")


        } catch (e: IOException) {
            e.printStackTrace()
        }

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, 0)

        if (Build.VERSION.SDK_INT >= 26) {
            var mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val id = "my_channel_01"
            val name: CharSequence = "주변에 맛집 $placeName 가 있어요!"
            val description = "탭하여 더 많은 정보 확인하기"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(id, name, importance)
            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mNotificationManager.createNotificationChannel(mChannel)
            mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notifyID = 1
            val CHANNEL_ID = "my_channel_01"
            val notification: Notification =
                Notification.Builder(context)
                    .setContentTitle("주변에 맛집 ${placeName}가 있어요!")
                    .setContentText("탭하여 더 많은 정보 확인하기")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setChannelId(CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()
            mNotificationManager.notify(1, notification)
        } else {
            val builder =
                NotificationCompat.Builder(context, "detected")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("농장에 침입이 감지되었습니다!")
                    .setContentText("탭하여 CCTV 확인하기")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                    .setAutoCancel(true)
            val notificationManager =
                NotificationManagerCompat.from(context)

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1004, builder.build())
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
    }
}
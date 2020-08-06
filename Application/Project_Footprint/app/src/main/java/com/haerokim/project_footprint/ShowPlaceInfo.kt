package com.haerokim.project_footprint

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startActivity
import com.haerokim.project_footprint.Activity.PlaceDetailActivity
import com.haerokim.project_footprint.Data.Place

// Notification 형식으로 보여주는 메소드와, 액티비티 형식으로 보여주는 메소드를 가짐
class ShowPlaceInfo(var context: Context, var placeID: String) : Activity(){
    //GetPlaceInfo() 를 실행하는 시점에, 비콘 모듈의 UUID 값을 넣어줄 예정
    //넘어온 UUID를 기반으로 SQL 쿼리를 하고, 쿼리를 통해 네이버 플레이스 등록 ID 취득 예정
    private var placeName = placeID

    fun notifyInfo() {
        var place = GetPlaceInfo(placeID).execute().get()

        var placeTitle = place.title
        var placeCategory = place.category
        var placeDescription = place.description
        var placeTime = place.time
        var placeLocation = place.location
        var placeImageSrc = place.imageSrc
        var placeMenuName = place.menuName
        var placeMenuPrice = place.menuPrice

        //PlaceDetailActivity 로 보낼 장소 데이터 모두 번들에 담음
        val intent = Intent(context, PlaceDetailActivity::class.java)
        val bundle: Bundle = Bundle()
        bundle.putString("Title", placeTitle)
        bundle.putString("Category", placeCategory)
        bundle.putString("Description", placeDescription)
        bundle.putString("Time", placeTime)
        bundle.putString("Location", placeLocation)
        bundle.putString("Image", placeImageSrc)
        bundle.putStringArrayList("MenuName", placeMenuName)
        bundle.putStringArrayList("MenuPrice", placeMenuPrice)

        //번들 intent data로 담아줌
        intent.putExtras(bundle)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // ========================= Head-Up Notification 구현  (SDK 26 기준으로 다르게 구현 필요) ========================= //

        if (Build.VERSION.SDK_INT >= 26) {
            var mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val id = "channel_notify"
            val name: CharSequence = "주변에 맛집 $placeTitle 가 있어요!"
            val description = "탭하여 더 많은 정보 확인하기"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(id, name, importance)

            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mNotificationManager.createNotificationChannel(mChannel)

            val CHANNEL_ID = "channel_notify"
            val notification: Notification =
                Notification.Builder(context)
                    .setContentTitle("당신 주변의 맛집 ${placeTitle} 발견!")
                    .setContentText("탭하여 더 많은 정보 확인하기")
                    .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                    .setChannelId(CHANNEL_ID)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .build()

            mNotificationManager.notify(5603, notification)

        } else {
            val builder =
                NotificationCompat.Builder(context, "channel_notify")
                    .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                    .setContentTitle("당신 주변의 맛집 ${placeTitle} 발견!")
                    .setContentText("탭하여 더 많은 정보 확인하기")
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                    .setAutoCancel(true)

            val notificationManager =
                NotificationManagerCompat.from(context)

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(5603, builder.build())
        }
    }

    // Place 객체를 받아 정보를 추출한 뒤 Activity 이동
    fun showInfo(place: Place){

        var placeTitle = place.title
        var placeCategory = place.category
        var placeDescription = place.description
        var placeTime = place.time
        var placeLocation = place.location
        var placeImageSrc = place.imageSrc
        var placeMenuName = place.menuName
        var placeMenuPrice = place.menuPrice

        //PlaceDetailActivity 로 보낼 장소 데이터 모두 번들에 담음
        val intent = Intent(context, PlaceDetailActivity::class.java)

        val bundle: Bundle = Bundle()
        bundle.putString("Title", placeTitle)
        bundle.putString("Category", placeCategory)
        bundle.putString("Description", placeDescription)
        bundle.putString("Time", placeTime)
        bundle.putString("Location", placeLocation)
        bundle.putString("Image", placeImageSrc)
        bundle.putStringArrayList("MenuName", placeMenuName)
        bundle.putStringArrayList("MenuPrice", placeMenuPrice)

        //번들 intent data로 담아줌
        intent.putExtras(bundle)
        context.startActivity(intent)
    }
}
package com.haerokim.project_footprint.Utility

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.haerokim.project_footprint.Activity.PlaceDetailActivity
import com.haerokim.project_footprint.DataClass.Place
import com.haerokim.project_footprint.DataClass.VisitedPlace
import com.haerokim.project_footprint.R
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 *  장소의 상세정보를 사용자에게 보여주기 위한 다양한 기능 제공
 *  - Notification 형식으로 보여주는 메소드와, Activity 형식으로 보여주는 메소드를 가짐
 *  - 주로 ForegroundService 에서 특정 장소를 스캔했을 때 호출됨
 **/

/**
 *  장소의 상세정보를 사용자에게 푸시알림으로 발송
 *  - NaverPlaceID 를 파라미터로 받아 GetPlaceInfo 를 통해 데이터 구성
 *  - 주로 ForegroundService 에서 특정 장소를 스캔했을 때 호출됨
 **/
class ShowPlaceInfo(var context: Context, var placeID: String) : Activity() {
    private var placeName = placeID

    fun notifyInfo(mode: String?) {
        // Realm 사용을 위해 init() 필요
        Realm.init(context)

        // 저장된 장소 정보를 활용하기 위해 Realm 객체 생성 및 초기화
        val config: RealmConfiguration = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(config)
        var realm = Realm.getDefaultInstance()

        //  푸시알림 데이터 구성을 위해 GetPlaceInfo() 호출
        var place = GetPlaceInfo(placeID).execute().get()

        var placeTitle = place.title
        var placeCategory = place.category
        var placeDescription = place.description
        var placeTime = place.businessHours
        var placeLocation = place.location
        var placeImageSrc = place.imageSrc
        var placeMenuName = place.menuName
        var placeMenuPrice = place.menuPrice

        // Realm (Local DB)에 해당 장소의 이름을 저장함
        realm.executeTransaction {
            val visitedPlace = it.where(VisitedPlace::class.java).equalTo("naverPlaceID", placeID).findFirst()
            visitedPlace.placeTitle = placeTitle
        }

        // 해당 푸시알림을 탭하면 장소의 상세정보를 보여주는 Activity 로 이동하게 됨
        // PlaceDetailActivity 로 보낼 장소 데이터 모두 번들에 담음
        val intent = Intent(context, PlaceDetailActivity::class.java)
        val bundle: Bundle = Bundle()
        bundle.putString("PlaceID", placeID)
        bundle.putString("Title", placeTitle)
        bundle.putString("Category", placeCategory)
        bundle.putString("Description", placeDescription)
        bundle.putString("Time", placeTime)
        bundle.putString("Location", placeLocation)
        bundle.putString("Image", placeImageSrc)
        bundle.putStringArrayList("MenuName", placeMenuName)
        bundle.putStringArrayList("MenuPrice", placeMenuPrice)

        // Bundle Intent Data 로 담아줌
        intent.putExtras(bundle)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        // ========================= Head-Up Notification 구현  (SDK 26 기준으로 다르게 구현 필요) ========================= //

        if (Build.VERSION.SDK_INT >= 26) {
            var mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val id = "channel_notify"
            val name: CharSequence = if (mode == "nearPlace") {
                "주변에 맛집 $placeTitle 가 있어요!"
            } else {
                "$placeTitle 를 방문하셨군요!"
            }
            val description = if (mode == "nearPlace") {
                "탭하여 더 많은 정보 확인하기"
            } else {
                "좋은 추억 남기세요 :)"
            }
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
                if (mode == "nearPlace") {
                    Notification.Builder(context)
                        .setContentTitle("당신 주변의 맛집 ${placeTitle} 발견!")
                        .setContentText("탭하여 더 많은 정보 확인하기")
                        .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                        .setChannelId(CHANNEL_ID)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .build()
                } else {
                    Notification.Builder(context)
                        .setContentTitle("$placeTitle 를 방문하셨군요!")
                        .setContentText("좋은 추억 남기세요 :)")
                        .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                        .setChannelId(CHANNEL_ID)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .build()
                }

            mNotificationManager.notify(5603, notification)

        } else {
            val builder =
                if (mode == "nearPlace") {
                    NotificationCompat.Builder(context, "channel_notify")
                        .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                        .setContentTitle("당신 주변의 맛집 ${placeTitle} 발견!")
                        .setContentText("탭하여 더 많은 정보 확인하기")
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                        .setAutoCancel(true)
                } else {
                    NotificationCompat.Builder(context, "channel_notify")
                        .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                        .setContentTitle("$placeTitle 를 방문하셨군요!")
                        .setContentText("좋은 추억 남기세요 :)")
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                        .setAutoCancel(true)
                }


            val notificationManager =
                NotificationManagerCompat.from(context)

            notificationManager.notify(5603, builder.build())
        }
    }

    /**
     *  장소의 상세정보를 Activity 를 통해 보여줌
     *  - Place 객체 자체를 파라미터로 받고, PlaceDetailActivity 에 해당 장소 정보 전달
     **/

    fun showInfo(place: Place) {

        var placeTitle = place.title
        var placeCategory = place.category
        var placeDescription = place.description
        var placeTime = place.businessHours
        var placeLocation = place.location
        var placeImageSrc = place.imageSrc
        var placeMenuName = place.menuName
        var placeMenuPrice = place.menuPrice

        //PlaceDetailActivity 로 보낼 장소 데이터 모두 번들에 담음
        val intent = Intent(context, PlaceDetailActivity::class.java)

        val bundle: Bundle = Bundle()
        bundle.putString("PlaceID", place.naverPlaceID)
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
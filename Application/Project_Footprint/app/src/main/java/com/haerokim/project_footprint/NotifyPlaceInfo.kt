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
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.haerokim.project_footprint.Activity.PlaceDetailActivity
import com.haerokim.project_footprint.Data.Place
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException

class NotifyPlaceInfo(var context: Context, var place: String) : AsyncTask<Void, Void, Void>() {
    //GetPlaceInfo() 를 실행하는 시점에, 비콘 모듈의 UUID 값을 넣어줄 예정
    //넘어온 UUID를 기반으로 SQL 쿼리를 하고, 쿼리를 통해 네이버 플레이스 등록 ID 취득 예정
    private var placeName = place

    override fun doInBackground(vararg params: Void?): Void? {

        // 해당 데이터 처리하는 Activity에서 Null 대응하므로 Nullable 타입으로 지정
        var placeTitle: String? = null
        var placeCategory: String? = null
        var placeDescription: String? = null
        var placeTime: String? = null
        var placeLocation: String? = null
        var placeImageSrc: String? = null
        var placeMenuName: ArrayList<String> = arrayListOf()
        var placeMenuPrice: ArrayList<String> = arrayListOf()

        try {
            // 네이버 플레이스 URL로 변경 예정 ( 아이디 SQL 쿼리로 얻어올 수 있게끔 매핑 예정 )

            val doc: Document =
                Jsoup.connect("https://search.naver.com/search.naver?query=$placeName").get()
            val titleElement: Elements = doc.select("div[class=biz_name_area]").select("a")
            val categoryElement: Elements = doc.select("div[class=biz_name_area]").select("span")
            val descriptionElement: Elements = doc.select("div[class=info] div").select("span")
            val timeElement: Elements = doc.select("div[class=biztime] span").select("span")
            val locationElement: Elements = doc.select("span[class=addr]")
            val menuNameElement: Elements = doc.select("span[class=name]")
            val menuPriceElement: Elements = doc.select("div em[class=price]")

            // 네이버 플레이스 URL에다가 tab=photo 쿼리 붙이면 이미지 파싱 URL임
            val imageDoc: Document =
                Jsoup.connect("https://store.naver.com/restaurants/detail?entry=plt&id=36177811&query=%EA%B0%90%EC%B9%A0&tab=photo")
                    .get()
            val imageElement: Elements = imageDoc.select("div.list_photo img")

            // ===================HTML 파싱 데이터 모두 변수에 담아줌=================== //

            placeTitle = if (titleElement.size != 0) {
                titleElement[0].text()
            } else {
                null
            }
            placeCategory = if (categoryElement.size != 0) {
                categoryElement[0].text()
            } else {
                null
            }
            placeDescription = if (descriptionElement.size != 0) {
                descriptionElement[0].text()
            } else {
                null
            }
            placeTime = if (timeElement.size != 0) {
                timeElement[0].text()
            } else {
                null
            }
            placeLocation = if (locationElement.size != 0) {
                locationElement[0].text()
            } else {
                null
            }

            //Jsoup Parser의 Return 형태인 Elements에서 ArrayList로 변환
            for (element in menuNameElement) {
                placeMenuName.add(element.text())
            }

            for (element in menuPriceElement) {
                placeMenuPrice.add(element.text())
            }

            placeImageSrc = if (imageElement.size != 0) {
                imageElement[0].attr("src")
            } else {
                ""
            }

            // ===================HTML 파싱 데이터 모두 변수에 담아줌=================== //

        } catch (e: IOException) {
            e.printStackTrace()
        }

        //Place 정보를 담은 클래스 생성

        var placeInformation = Place(
            placeTitle, placeCategory, placeDescription
            , placeTime, placeLocation, placeImageSrc, placeMenuName, placeMenuPrice
        )

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

            val CHANNEL_ID = "channel_notify"
            val notification: Notification =
                Notification.Builder(context)
                    .setContentTitle("당신 주변의 맛집 ${placeName} 발견!")
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
                    .setContentTitle("당신 주변의 맛집 ${placeName} 발견!")
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

        // ========================= Head-Up Notification 구현 ========================= //

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
    }
}
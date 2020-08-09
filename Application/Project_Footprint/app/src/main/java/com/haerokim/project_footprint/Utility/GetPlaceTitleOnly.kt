package com.haerokim.project_footprint.Utility

import android.os.AsyncTask
import com.haerokim.project_footprint.DataClass.Place
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class GetPlaceTitleOnly(placeID: String) : AsyncTask<String, Void, String>() {
    //GetPlaceInfo() 를 실행하는 시점에, 비콘 모듈의 UUID 값을 넣어줄 예정
    //넘어온 UUID를 기반으로 SQL 쿼리를 하고, 쿼리를 통해 네이버 플레이스 등록 ID 취득 예정
    private lateinit var placeTitle: Place
    private var placeNaverID = placeID

    //호출하는 쪽에서 GetPlaceTitleOnly(naverPlaceID).execute().get() 을 통해 Place 객체 받음

    override fun doInBackground(vararg params: String?): String {
        //NaverPlaceID를 통해 Place의 이름만 얻고자 함 (History 관련 기능에서 사용)
        var placeTitle: String = ""

        try {
            // 네이버 플레이스 URL로 변경 예정 ( 아이디 SQL 쿼리로 얻어올 수 있게끔 매핑 예정 )
            val doc: Document =
                Jsoup.connect("https://store.naver.com/restaurants/detail?id=$placeNaverID").get()
            val titleElement: Elements = doc.select("div[class=biz_name_area]").select("strong")
            placeTitle = if (titleElement.size != 0) { titleElement[0].text() } else { "Null" }

        } catch (e: Exception) {

        }
        return placeTitle
    }
}
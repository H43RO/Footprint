package com.haerokim.project_footprint.Utility

import android.os.AsyncTask
import android.util.Log
import com.haerokim.project_footprint.DataClass.Place
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

/**
 *  특정 장소의 이름을 크롤링하는 기능 제공
 *  - NaverPlaceID 를 파라미터로 받아, 네이버 플레이스에서 이름을 크롤링함
 *  - 'Jsoup' 라이브러리 활용하여 HTML 문서를 통해 크롤링 함
 *  - 굳이 장소 상세정보가 모두 필요하지 않을 때 사용 (HistoryList Item Layout 등)
 **/

class GetPlaceTitleOnly(placeID: String) : AsyncTask<String, Void, String>() {
    private lateinit var placeTitle: Place
    private var placeNaverID = placeID

    //호출하는 쪽에서 GetPlaceTitleOnly(naverPlaceID).execute().get() 을 통해 장소 이름 받음
    override fun doInBackground(vararg params: String?): String {
        var placeTitle: String = ""
        try {
            //NaverPlaceID를 통해 Place의 이름만 얻고자 함 (HistoryList 조회 관련 기능에서 사용)
            val doc: Document =
                Jsoup.connect("https://store.naver.com/restaurants/detail?id=$placeNaverID").get()
            val titleElement: Elements = doc.select("div[class=biz_name_area]").select("strong")
            placeTitle = if (titleElement.size != 0) { titleElement[0].text() } else { "Null" }

        } catch (e: Exception) {
            Log.e("Error Crawling", e.message)
        }
        return placeTitle
    }
}
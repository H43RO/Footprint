package com.haerokim.project_footprint.Utility

import android.os.AsyncTask
import com.haerokim.project_footprint.DataClass.Place
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException

/**
 *  특정 장소의 상세정보를 크롤링하는 기능 제공
 *  - NaverPlaceID 를 파라미터로 받아, 네이버 플레이스에서 상세정보 크롤링함
 *  - 'Jsoup' 라이브러리 활용하여 HTML 문서를 통해 크롤링 함
 *  - Place Data class 객체 형태로 획득한 정보 반환
 **/

class GetPlaceInfo(placeID: String) : AsyncTask<Place, Void, Place>() {
    //넘어온 NaverPlaceID 기반으로 네이버 플레이스 크롤링
    private lateinit var placeInfo: Place // 반환될 객체
    private var placeNaverID = placeID

    //호출하는 쪽에서 GetPlaceInfo("name").execute().get() 을 통해 Place 객체 받음
    override fun doInBackground(vararg params: Place?): Place {
        var placeTitle: String? = null
        var placeCategory: String? = null
        var placeDescription: String? = null
        var placeTime: String? = null
        var placeLocation: String? = null
        var placeImageSrc: String? = null
        var placeMenuName: ArrayList<String> = arrayListOf()
        var placeMenuPrice: ArrayList<String> = arrayListOf()

        try {
            val doc: Document =
                Jsoup.connect("https://store.naver.com/restaurants/detail?id=$placeNaverID").get()
            val titleElement: Elements = doc.select("div[class=biz_name_area]").select("strong")
            val categoryElement: Elements = doc.select("div[class=biz_name_area]").select("span")
            val descriptionElement: Elements = doc.select("div[class=info] div").select("span")
            val timeElement: Elements = doc.select("div[class=biztime] span").select("span")
            val locationElement: Elements = doc.select("span[class=addr]")
            val menuNameElement: Elements = doc.select("div[class=list_menu_inner]").select("span[class=name]")
            val menuPriceElement: Elements = doc.select("div em[class=price]")

            // 네이버 플레이스 URL에다가 tab=photo 쿼리 붙여 이미지 획득 URL로 사용
            val imageDoc: Document =
                Jsoup.connect("https://store.naver.com/restaurants/detail?id=$placeNaverID&tab=photo")
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

        //Place 정보를 담은 객체 생성해서 리턴해줌
        placeInfo = Place(placeNaverID,
            placeTitle, placeCategory, placeDescription
            , placeTime, placeLocation, placeImageSrc, placeMenuName, placeMenuPrice
        )

        return placeInfo
    }
}
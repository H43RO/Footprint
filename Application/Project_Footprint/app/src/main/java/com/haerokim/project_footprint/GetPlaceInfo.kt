package com.haerokim.project_footprint

import android.os.AsyncTask
import com.haerokim.project_footprint.Data.Place
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException

class GetPlaceInfo(placeID: String) : AsyncTask<Place, Void, Place>() {
    //GetPlaceInfo() 를 실행하는 시점에, 비콘 모듈의 UUID 값을 넣어줄 예정
    //넘어온 UUID를 기반으로 SQL 쿼리를 하고, 쿼리를 통해 네이버 플레이스 등록 ID 취득 예정
    private lateinit var placeInfo: Place
    private var placeNaverID = placeID

    //호출하는 쪽에서 GetPlaceInfo("name").execute().get() 을 통해 Place 객체 받음

    override fun doInBackground(vararg params: Place?): Place {

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
                Jsoup.connect("https://store.naver.com/restaurants/detail?id=$placeNaverID").get()
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
        //Place 정보를 담은 객체 생성해서 리턴해줌

        placeInfo = Place(placeNaverID,
            placeTitle, placeCategory, placeDescription
            , placeTime, placeLocation, placeImageSrc, placeMenuName, placeMenuPrice
        )

        return placeInfo
    }


    override fun onPostExecute(result: Place) {
        super.onPostExecute(result)
    }
}
package com.haerokim.project_footprint

import android.os.AsyncTask
import android.util.Log
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.lang.Exception

class GetPlaceInfo : AsyncTask<Void, Void, Void>() {

    override fun onPreExecute() {
        super.onPreExecute()
        Log.d("async_test","AsyncTask 시작")
    }

    override fun doInBackground(vararg params: Void?): Void? {
        try{
            var doc : Document = Jsoup.connect("https://search.naver.com/search.naver?query=j의꽃다방").get()
            var titleElement: Elements = doc.select("div[class=biz_name_area]").select("a")
            var categoryElement: Elements = doc.select("div[class=biz_name_area]").select("span")
            var descriptionElement: Elements = doc.select("div[class=info] div").select("span")
            var timeElement: Elements = doc.select("div[class=biztime] span").select("span")

            //Description, Time은 Null일 수도 있음

            Log.d("HTML_title", titleElement[0].text())
            Log.d("HTML_category", categoryElement[0].text())
            Log.d("HTML_description", descriptionElement[0].text())
            Log.d("HTML_time", timeElement[0].text())


        }catch (e:IOException){
            e.printStackTrace()
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
    }
}
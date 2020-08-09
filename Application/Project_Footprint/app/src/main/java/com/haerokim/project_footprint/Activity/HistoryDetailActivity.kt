package com.haerokim.project_footprint.Activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_history_detail.*
import kotlinx.android.synthetic.main.activity_place_detail.*
import kotlinx.android.synthetic.main.history_item.*

class HistoryDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)

        val historyInfo: Bundle? = intent.extras

        var historyID = historyInfo?.getInt("id")
        var historyImage = historyInfo?.getString("image")
        var historyTitle = historyInfo?.getString("title") ?: "발자취 : 제목 미상"
        var historyMood = historyInfo?.getInt("mood") //기본 '0' (감정 - So So)
        var historyComment = historyInfo?.getString("comment") ?: "발자취 : 내용 미상"
        var historyPlaceTitle = historyInfo?.getString("placeTitle")
        var historyCreatedAt = historyInfo?.getString("createdAt") ?: "어느 멋진 날"
        var historyUserID = historyInfo?.getInt("userID")

        if(historyImage == null){
            // Image URL 없을 시 기본 이미지
            history_detail_image.setImageResource(R.drawable.placeholder)
        }else{
            Glide.with(this)
                .load(historyImage)
                .centerCrop()
                .thumbnail(0.1f)
                .into(history_detail_image)
        }

        text_history_detail_title.text = historyTitle
        text_history_detail_place.text = historyPlaceTitle
        text_history_detail_time.text = historyCreatedAt
        text_history_detail_content.text = historyComment
    }
}


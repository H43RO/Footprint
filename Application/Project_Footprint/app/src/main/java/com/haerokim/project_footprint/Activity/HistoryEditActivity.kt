package com.haerokim.project_footprint.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_history_detail.*
import kotlinx.android.synthetic.main.activity_history_detail.history_detail_image
import kotlinx.android.synthetic.main.activity_history_detail.text_history_detail_content
import kotlinx.android.synthetic.main.activity_history_detail.text_history_detail_place
import kotlinx.android.synthetic.main.activity_history_detail.text_history_detail_time
import kotlinx.android.synthetic.main.activity_history_detail.text_history_detail_title
import kotlinx.android.synthetic.main.activity_history_edit.*

class HistoryEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_edit)

        val historyInfo: Bundle? = intent.extras

        var historyID = historyInfo?.getInt("id")
        var historyImage = historyInfo?.getString("image")
        var historyTitle = historyInfo?.getString("title") ?: "어느 멋진 날"
        var historyMood = historyInfo?.getString("mood") //기본 감정 - SoSo
        var historyComment = historyInfo?.getString("comment") ?: "내용 미상"
        var historyPlaceTitle = historyInfo?.getString("placeTitle")
        var historyCreatedAt = historyInfo?.getString("createdAt") ?: "어느 멋진 날"
        var historyUserID = historyInfo?.getInt("userID")

        if (historyImage == null) {
            // Image URL 없을 시 기본 이미지
            history_detail_image.setImageResource(R.drawable.placeholder)
        } else {
            Glide.with(this)
                .load(historyImage)
                .centerCrop()
                .thumbnail(0.1f)
                .into(history_detail_image)
        }

        text_history_detail_title.text = historyTitle
        edit_history_detail_place.text = historyPlaceTitle
        edit_history_detail_time.text = historyCreatedAt
        edit_history_detail_content.setText(historyComment)

        val items = resources.getStringArray(R.array.moode_list)
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner_select_mood.adapter = spinnerAdapter

        spinner_select_mood.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // NOP
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {

                    }

                    1 -> {

                    }
                }
            }
        }
    }
}
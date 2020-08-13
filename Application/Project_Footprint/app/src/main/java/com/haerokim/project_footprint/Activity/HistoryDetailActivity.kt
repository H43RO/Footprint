package com.haerokim.project_footprint.Activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_history_detail.*

class HistoryDetailActivity : AppCompatActivity() {
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 수정된 정보가 넘어오면 실행
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            text_history_detail_title.text = data?.getStringExtra("title")
            text_history_detail_content.text = data?.getStringExtra("comment")
//            TODO ("나머지 2개 Extra 미구현")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)

        val historyInfo: Bundle? = intent.extras

        var historyID = historyInfo?.getInt("id")
        var historyImage = historyInfo?.getString("image")
        var historyTitle = historyInfo?.getString("title") ?: "어느 멋진 날"
        var historyMood = historyInfo?.getString("mood") //기본 감정 - SoSo
        var historyComment = historyInfo?.getString("comment") ?: "당신만의 이야기를 들려주세요."
        var historyPlaceTitle = historyInfo?.getString("placeTitle")
        var historyCreatedAt = historyInfo?.getString("createdAt")
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
        text_history_detail_place.text = historyPlaceTitle
        text_history_detail_time.text = historyCreatedAt
        text_history_detail_content.text = historyComment

        button_history_detail_action.setOnClickListener {
            val popup: PopupMenu = PopupMenu(this, it)
            popup.inflate(R.menu.history_menu)
            // 기본 이미지로 변경할 건지, 갤러리 및 촬영 사진으로 변경할 것인
            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                when (it.itemId) {
                    R.id.edit_history -> {
                        val intent = Intent(this, HistoryEditActivity::class.java)
                        intent.putExtras(historyInfo!!)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivityForResult(intent, 1)
//                        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                    }

                    R.id.share_history -> {

                    }

                    R.id.delete_history -> {

                    }
                }
                true
            })
            popup.show()
        }
    }
}


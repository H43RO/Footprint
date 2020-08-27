package com.haerokim.project_footprint.Activity

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_editor_pick_deatil.*

/**
 *  에디터 추천 장소 게시물의 상세 내용을 화면에 보여줌
 *  - 게시물 내용 HTML 형태로 오기 때문에, WebView 를 이용하여 HTML 문서가 화면에 보여지도록 함
 *  - 이미 구성된 EditorPick 객체의 값을 사용하기 때문에 네트워킹 작업 필요 없음
 **/

class EditorPickDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_pick_deatil)

        val bundle = intent.extras
        val title = bundle?.getString("title")
        val contents = bundle?.getString("contents")
        val img = bundle?.getString("img")
        val description = bundle?.getString("description")

        Glide.with(this)
            .load(img)
            .centerCrop()
            .override(600, 400)
            .thumbnail(0.1f)
            .into(image_editor_detail)

        // 이미지 색상에 따라 Title이 보이지 않는 경우도 있을 수 있어 검은색 필터를 씌움
        image_editor_detail.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY)

        text_editor_detail_title.text = title
        text_editor_detail_content.text = contents

        val htmlDoc = description
        editor_web_view.loadData(htmlDoc, "text/html", "UTF-8")
    }
}
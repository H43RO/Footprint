package com.haerokim.project_footprint.Activity

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_editor_pick_deatil.*
import kotlinx.android.synthetic.main.editor_item.view.*

class EditorPickDeatilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_pick_deatil)

        val bundle = intent.extras
        val title = bundle?.getString("title")
        val contents= bundle?.getString("contents")
        val img= bundle?.getString("img")
        val description= bundle?.getString("description")

        Glide.with(this) // 확인 필요
            .load(img)
            .centerCrop()
            .override(600, 400)
            .thumbnail(0.1f)
            .into(image_editor_detail)

        image_editor_detail.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY)

        text_editor_detail_title.text = title
        text_editor_detail_content.text = contents

//        editor_web_view.webViewClient = object: WebViewClient(){
//            override fun shouldOverrideUrlLoading(
//                view: WebView?,
//                request: WebResourceRequest?
//            ): Boolean  = false
//
//            override fun onPageFinished(view: WebView?, url: String?) {
//
//            }
//        }

        val htmlDoc = description
        editor_web_view.loadData(htmlDoc, "text/html", "UTF-8")


    }
}
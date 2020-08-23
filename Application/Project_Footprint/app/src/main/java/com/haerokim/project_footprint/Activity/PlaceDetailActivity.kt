package com.haerokim.project_footprint.Activity

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_place_detail.*

class PlaceDetailActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        // 번들로부터 데이터 얻음
        val bundle = intent.extras

        val placeTitle = bundle?.getString("Title") ?: "등록된 정보가 없습니다."
        val placeCategory = bundle?.getString("Category") ?: "등록된 정보가 없습니다."
        val placeDescription = bundle?.getString("Description") ?: "여기서 식사하시는거 어때요?"
        val placeLocation = bundle?.getString("Location") ?: "등록된 정보가 없습니다"
        val placeTime = bundle?.getString("Time") ?: "등록된 정보가 없습니다."
        val placeMenuName = bundle?.getStringArrayList("MenuName")
        val placeMenuPrice = bundle?.getStringArrayList("MenuPrice")
        val placeImageSrc = bundle?.getString("Image") ?: "등록된 정보가 없습니다."

        title_text.setText(placeTitle)
        category_text.setText(placeCategory)

        if(placeDescription.length < 3){
            description_text.setText("이 곳에서 당신의 추억을 남겨보세요")
        }else{
            description_text.setText(placeDescription)
        }

        if(placeLocation.length < 3){
            location_text.setText("등록된 정보가 없습니다.")
        }else{
            location_text.setText(placeLocation)
        }

        Log.d("plcaeTime", placeTime + "임미당")

        if(placeTime.length < 3){
            time_text.setText("등록된 정보가 없습니다.")
        }else{
            time_text.setText(placeTime)
        }

        location_card.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=$placeLocation")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        //추후 SQL 쿼리를 통한 ID 값 취득이 가능해지면(링크 조합이 가능해지면) 완성할 예정
        link_card.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://www.naver.com/")
            startActivity(intent)
        }

        if (placeMenuName != null) {
            for (menu in placeMenuName) {
                if (menu == placeMenuName.last()) {
                    menu_name_list.append(menu)
                } else {
                    menu_name_list.append(menu + '\n')
                }
            }
        }

        //각각 다른 메뉴인데 가격이 같을 수 있으므로 last() 속성 사용 불가
        var i : Int = 0
        if (placeMenuPrice != null) {
            for (price in placeMenuPrice) {
                if (i++ == placeMenuPrice.size - 1) {
                    menu_price_list.append(price)
                } else {
                    menu_price_list.append(price + '\n')
                }
                Log.d("element", price)
            }
        }

        // 얻은 이미지 URL을 기반으로 ImageView에 뿌려줌 (Glide 라이브러리 사용)
        Glide.with(this)
            .load(placeImageSrc)
            .centerCrop()
            .into(title_background)

        title_background.setColorFilter(Color.parseColor("#9F9F9F"), PorterDuff.Mode.MULTIPLY)
    }
}
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

/**
 *  선택한 장소의 상세 정보를 보여줌
 *  - 수정 및 삭제 기능, SNS 공유 (인스타그램, 페이스북) 지원
 *  - 서비스 특성 상 장소 수정, 생성 시각 수정은 지원안함
 **/

class PlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        // Place 조회 관련 페이지에서 넘어온 Bundle Data 사용
        val bundle = intent.extras

        val placeID = bundle?.getString("PlaceID")
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

        // 공백 데이터의 길이가 0 ~ 1로 상이한 것을 확인하여 2 미만 일 때 공백으로 취급
        if(placeDescription.length < 2) description_text.setText("이 곳에서 당신의 추억을 남겨보세요")
        else description_text.setText(placeDescription)

        if(placeLocation.length < 2) location_text.setText("등록된 정보가 없습니다.")
        else location_text.setText(placeLocation)


        if(placeTime.length < 2) time_text.setText("등록된 정보가 없습니다.")
        else time_text.setText(placeTime)

        // 장소 위치 정보를 기반으로 Google Map Intent
        location_card.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=$placeLocation")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        // Naver Place ID를 기반으로 네이버 플레이스 페이지 Intent
        link_card.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://store.naver.com/restaurants/detail?id=$placeID")
            startActivity(intent)
        }

        // 배열 형태이기 때문에 TextView 의 append() 사용
        if (placeMenuName != null) {
            for (menu in placeMenuName) {
                if (menu == placeMenuName.last()) menu_name_list.append(menu)
                else menu_name_list.append(menu + '\n')
            }
        }

        // 배열 형태이기 때문에 TextView 의 append() 사용
        //각각 다른 메뉴인데 가격이 같을 수 있으므로 last() 메소드 사용 X
        var i : Int = 0
        if (placeMenuPrice != null) {
            for (price in placeMenuPrice) {
                if (i++ == placeMenuPrice.size - 1) menu_price_list.append(price)
                else menu_price_list.append(price + '\n')
            }
        }

        // 얻은 식당 대표 이미지 URL을 기반으로 ImageView에 보여줌 (Glide 라이브러리 사용)
        Glide.with(this)
            .load(placeImageSrc)
            .centerCrop()
            .into(title_background)

        // 이미지 색상에 따라 Title이 보이지 않는 경우도 있을 수 있어 검은색 필터를 씌움
        title_background.setColorFilter(Color.parseColor("#9F9F9F"), PorterDuff.Mode.MULTIPLY)
    }
}
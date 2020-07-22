package com.haerokim.project_footprint

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_place_detail.*
import java.io.BufferedInputStream
import java.lang.Exception
import java.net.URL
import java.net.URLConnection

class PlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        val bundle = intent.extras

        val placeTitle = bundle?.getString("Title") ?: "등록된 정보가 없습니다."
        val placeCategory = bundle?.getString("Category") ?: "등록된 정보가 없습니다."
        val placeDescription = bundle?.getString("Description") ?: "등록된 정보가 없습니다."
        val placeLocation = bundle?.getString("Location") ?: "등록된 정보가 없습니다"
        val placeTime = bundle?.getString("Time") ?: "등록된 정보가 없습니다."
        val placeMenuName = bundle?.getStringArrayList("MenuName")
        val placeMenuPrice = bundle?.getStringArrayList("MenuPrice")
        val placeImageSrc = bundle?.getString("Image") ?: "등록된 정보가 없습니다."

//        Log.d("Intent Title", placeTitle)
//        Log.d("Intent Description", placeDescription)
//        Log.d("Intent Image", placeImageSrc)

        title_text.setText(placeTitle)
        category_text.setText(placeCategory)
        description_text.setText(placeDescription)

        location_text.setText(placeLocation)

        if (placeMenuName != null) {
            for(menu in placeMenuName){
                if(menu == placeMenuName.last()){
                    menu_name_list.append(menu)
                }else{
                    menu_name_list.append(menu + '\n')
                }
            }
        }

        if (placeMenuPrice != null) {
            for(price in placeMenuPrice){
                if(price == placeMenuPrice.last()){
                    menu_price_list.append(price)
                }else{
                    menu_price_list.append(price + '\n')
                }
            }
        }

        time_text.setText(placeTime)

        Glide.with(this)
            .load(placeImageSrc)
            .centerCrop()
            .into(title_background)

        title_background.setColorFilter(Color.parseColor("#9F9F9F"), PorterDuff.Mode.MULTIPLY);

        location_card.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=$placeTitle")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }


    }
}
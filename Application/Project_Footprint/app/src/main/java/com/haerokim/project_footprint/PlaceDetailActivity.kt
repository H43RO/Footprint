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
    override fun onBackPressed() {
        val home: Intent = Intent(this, MainActivity::class.java)
        startActivity(home)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        var bundle = intent.extras

        var placeTitle = bundle?.getString("Title") ?: "등록된 정보가 없습니다."
        var placeCategory = bundle?.getString("Category") ?: "등록된 정보가 없습니다."
        var placeDescription = bundle?.getString("Description") ?: "여기서 식사하시는거 어때요?"
        var placeLocation = bundle?.getString("Location") ?: "등록된 정보가 없습니다"
        var placeTime = bundle?.getString("Time") ?: "등록된 정보가 없습니다."
        var placeMenuName = bundle?.getStringArrayList("MenuName")
        var placeMenuPrice = bundle?.getStringArrayList("MenuPrice")
        var placeImageSrc = bundle?.getString("Image") ?: "등록된 정보가 없습니다."

        title_text.setText(placeTitle)
        category_text.setText(placeCategory)
        description_text.setText(placeDescription)

        location_text.setText(placeLocation)

        var i : Int = 0

        if (placeMenuName != null) {
            for (menu in placeMenuName) {
                if (menu == placeMenuName.last()) {
                    menu_name_list.append(menu)
                } else {
                    menu_name_list.append(menu + '\n')
                }
            }
        }

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

        time_text.setText(placeTime)

        Glide.with(this)
            .load(placeImageSrc)
            .centerCrop()
            .into(title_background)

        title_background.setColorFilter(Color.parseColor("#9F9F9F"), PorterDuff.Mode.MULTIPLY);

        location_card.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=$placeLocation")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

    }
}
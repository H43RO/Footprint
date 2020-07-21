package com.haerokim.project_footprint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class PlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        val bundle = intent.extras

        val placeTitle = bundle?.getString("Title") ?: "등록된 정보가 없습니다."
        val placeCategory = bundle?.getString("Category") ?: "등록된 정보가 없습니다."
        val placeDescription = bundle?.getString("Decsription") ?: "등록된 정보가 없습니다."
        val placeTime = bundle?.getString("Time") ?: "등록된 정보가 없습니다."
        val placeMenuName = bundle?.getStringArrayList("MenuName") ?: "등록된 정보가 없습니다."
        val placeMenuPrice = bundle?.getStringArrayList("MenuPrice") ?: "등록된 정보가 없습니다."
        val placeImageSrc = bundle?.getString("Image") ?: "등록된 정보가 없습니다."
    }
}
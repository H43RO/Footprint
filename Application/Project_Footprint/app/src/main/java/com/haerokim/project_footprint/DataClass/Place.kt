package com.haerokim.project_footprint.DataClass

import kotlin.String

/**  Place 정보를 담는 Object  **/

data class Place(
    var naverPlaceID: String,
    var title: String?,
    var category: String?,
    var description: String?,
    var businessHours: String?,
    var location: String?,
    var imageSrc: String?,
    var menuName: ArrayList<String>,
    var menuPrice: ArrayList<String>
)
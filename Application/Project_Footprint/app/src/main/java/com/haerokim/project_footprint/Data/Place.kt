package com.haerokim.project_footprint.Data

data class Place(
    var naverPlaceID: String,
    var title: String?,
    var category: String?,
    var description: String?,
    var time: String?,
    var location: String?,
    var imageSrc: String?,
    var menuName: ArrayList<String>,
    var menuPrice: ArrayList<String>
) {
    fun getNaverPlaceId(): String {
        return naverPlaceID ?: "등록된 정보가 없습니다"
    }

    fun getPlaceTitle(): String {
        return title ?: "등록된 정보가 없습니다"
    }

    fun getPlaceCategory(): String {
        return category ?: "등록된 정보가 없습니다"
    }

    fun getPlaceDescription(): String {
        return description ?: "등록된 정보가 없습니다"
    }

    fun getPlaceTime(): String {
        return time ?: "등록된 정보가 없습니다"
    }

    fun getPlaceLocation(): String {
        return location ?: "등록된 정보가 없습니다"
    }

    fun getPlaceImaggSrc(): String {
        return imageSrc ?: "등록된 정보가 없습니다"
    }

    fun getPlaceMenuName(): ArrayList<String> {
        return menuName
    }

    fun getPlaceMenuPrice(): ArrayList<String> {
        return menuPrice
    }

}
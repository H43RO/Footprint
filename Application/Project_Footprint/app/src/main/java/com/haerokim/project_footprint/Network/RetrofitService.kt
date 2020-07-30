package com.haerokim.project_footprint.Network

import com.haerokim.project_footprint.Data.Login
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitService {
    @FormUrlEncoded
    @POST("/api/login/") // Django 서버단 URL 확정되면 변경할 것
    fun requestLogin( // 서버단 변수명과 통일해야함 (Converting 호환성 중요)
        @Field("userID") userID: String,
        @Field("userPW") userPW: String
    ): Call<Login>

    // 수정 예정
    @FormUrlEncoded
    @GET("/api/get-place-id")
    fun requestPlaceInfo(
        @Field("placeID") placeID: String
    ):Call<String>
}
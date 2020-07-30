package com.haerokim.project_footprint.Service

import com.haerokim.project_footprint.Data.Login
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RetrofitService {
    @FormUrlEncoded
    @POST("/login/") //Django 서버단 URL 확정되면 변경할 것
    fun requestLogin( //서버단 변수명과 통일해야함 (Converting 호환성 중요)
        @Field("userid") userID: String,
        @Field("userpw") userPW: String
    ): Call<Login>
}
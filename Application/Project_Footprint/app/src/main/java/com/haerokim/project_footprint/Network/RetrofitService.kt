package com.haerokim.project_footprint.Network

import com.haerokim.project_footprint.DataClass.*
import retrofit2.Call
import retrofit2.http.*
import kotlin.collections.ArrayList

interface RetrofitService {
    // 로그인
    @FormUrlEncoded
    @POST("api/v1/accounts/login/")
    fun requestLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<User>

    // 장소 정보 요청
    @GET("api/places")
    fun requestNaverPlaceID(
        @Query("beacon_uuid") UUID: String
    ): Call<NaverPlaceID> //Response : NaverPlaceID

    // 사용자 회원정보 수정
    @PUT("userinfo/{userID}/update/")
    fun updateUserInfo(
        @Path("userID") userID: Int,
        @Body body: UpdateProfile
    ): Call<UpdateProfile> //Response : User Object

    // 실제 방문 히스토리 생성
    @FormUrlEncoded
    @POST("api/historys/")
    fun createRealVisitHistory(
        @Field("place") naverPlaceID: String,
        @Field("user") userID: Int
    ): Call<History> //Response : Status Code

    // 사용자 임의 히스토리 생성 : 수정 예정
    @FormUrlEncoded
    @POST("api/historys/")
    fun createVirtualHistory(
        @Field("place") placeName: String
    ): Call<History> //Response : Status Code

    // 히스토리 조회 API (날짜별) : 수정 예정
    @FormUrlEncoded
    @GET("/api/diary-list")
    fun requestHistoryList(
        @Field("date") date: String
    ): Call<ArrayList<History>>

    // 사용자 탈퇴 : 수정 예정
    @FormUrlEncoded
    @DELETE("userinfo/{userID}/delete")
    fun withDrawUser(
        @Field("userID") userID: Int
    ): Call<String> //Response : Status Code

    // 히스토리 작성 및 수정 : 수정 예정
    @FormUrlEncoded
    @PUT("/api/diary-write")
    fun writeHistory(
        @Field("history") history : History
    ): Call<String> //Response : Status Code


    
    // 히스토리 삭제 : 수정 예정
    @FormUrlEncoded
    @POST("/api/delete-history")
    fun deleteHistory(
        @Field("historyID") historyID: String
    ): Call<String> //Response : Status Code


    // 회원 가입 : 수정 예정
    @FormUrlEncoded
    @POST("/api/register")
    fun registerUser(
        @Field("name") name: String
    ): Call<String> //Response : Status Code
}
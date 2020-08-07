package com.haerokim.project_footprint.Network

import com.haerokim.project_footprint.Data.*
import retrofit2.Call
import retrofit2.http.*
import java.util.*
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
    fun requestPlaceInfo(
        @Query("beacon_uuid") UUID: String
    ): Call<List<NaverPlaceID>> //Response : NaverPlaceID

    // 사용자 회원정보 수정
    @PUT("userinfo/{userID}/update/")
    fun updateUserInfo(
        @Path("userID") userID: Int,
        @Body body: UpdateProfile
    ): Call<UpdateProfile> //Response : User Object

    // 사용자 탈퇴 : 수정 예정
    @FormUrlEncoded
    @DELETE("/api/user-leave")
    fun leaveUser(
        @Field("userID") userID: String,
        @Field("userPW") userPW: String
    ): Call<String> //Response : Status Code

    // 히스토리 작성 및 수정 : 수정 예정
    @FormUrlEncoded
    @PUT("/api/diary-write")
    fun writeHistory(
        @Field("history") history : History
    ): Call<String> //Response : Status Code

    // 히스토리 조회 API (날짜별) : 수정 예정
    @FormUrlEncoded
    @GET("/api/diary-list")
    fun requestHistoryList(
        @Field("date") date: String
    ): Call<ArrayList<History>>
    
    // 히스토리 삭제 : 수정 예정
    @FormUrlEncoded
    @POST("/api/delete-history")
    fun deleteHistory(
        @Field("historyID") historyID: String
    ): Call<String> //Response : Status Code

    // 히스토리 생성 : 수정 예정
    @FormUrlEncoded
    @POST("/api/create-history")
    fun createHistory(
        @Field("history") history: History
    ): Call<String> //Response : Status Code

    // 회원 가입 : 수정 예정
    @FormUrlEncoded
    @POST("/api/register")
    fun registerUser(
        @Field("name") name: String
    ): Call<String> //Response : Status Code
}
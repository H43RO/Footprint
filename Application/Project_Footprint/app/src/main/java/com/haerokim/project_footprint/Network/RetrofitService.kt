package com.haerokim.project_footprint.Network

import com.haerokim.project_footprint.DataClass.*
import retrofit2.Call
import retrofit2.http.*
import kotlin.collections.ArrayList

interface RetrofitService {

    /** 대응 완료 API**/

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
    ): Call<ArrayList<NaverPlaceID>> //Response : NaverPlaceID

    // 사용자 회원정보 수정
    @PUT("userinfo/{userID}/update/")
    fun updateUserInfo(
        @Path("userID") userID: Int,
        @Body body: UpdateProfile
    ): Call<UpdateProfile> //Response : User Object

    // 실제 방문 히스토리 생성
    @FormUrlEncoded
    @POST("api/histories/")
    fun createRealVisitHistory(
        @Field("place") naverPlaceID: String,
        @Field("user") userID: Int
    ): Call<History>

    // 사용자 임의 히스토리 생성 : 수정 예정
    @FormUrlEncoded
    @POST("api/histories/")
    fun createVirtualHistory(
        @Field("place") placeName: String
    ): Call<History> //Response : Status Code

    // 히스토리 조회 API (* 오늘 히스토리)
    @GET("api/histories/")
    fun requestTodayHistoryList(
        @Query("user") userID: Int,
        @Query("created_at__date__gte") today: String
    ): Call<ArrayList<History>>

    // 히스토리 조회 API (* 전체 히스토리)
    @GET("api/histories/")
    fun requestWholeHistoryList(
        @Query("user") userID: Int
    ): Call<ArrayList<History>>

    // 히스토리 조회 API (* 기간별 히스토리)
    @GET("api/histories/")
    fun requestDateHistoryList(
        @Query("user") userID: Int,
        @Query("created_at__date__lte") minDate: String,
        @Query("created_at__date__gte") maxDate: String
    ): Call<ArrayList<History>>

    // 히스토리 조회 API (* 키워드별 히스토리)
    @Headers("charset: utf-8")
    @GET("api/histories/")
    fun requestKeywordHistoryList(
        @Query("user") userID: Int,
        @Query("title__icontains") keyword: String
    ): Call<ArrayList<History>>

    // 히스토리 수정
    @PUT("api/histories/{historyID}/edit/")
    fun updateHistory(
        @Path("historyID") historyID: Int,
        @Body body: UpdateHistory
    ): Call<History>

    @GET("api/noticelist/")
    fun requestNoticeList(): Call<ArrayList<Notice>>

    // 사용자 탈퇴
    @FormUrlEncoded
    @DELETE("userinfo/{userID}/delete/")
    fun withDrawUser(
        @Field("userID") userID: Int
    ): Call<String> //Response : Status Code

    // 회원 가입 : 수정 예정
    @FormUrlEncoded
    @POST("/api/v1/accounts/register/")
    fun registerUser(
        @Body body: RegisterForm
    ): Call<User> //Response : Status Code

    /** 미 대응 API**/

    @Multipart
    // History Image Upload


    // 히스토리 삭제 : 수정 예정
    @FormUrlEncoded
    @POST("/api/delete-history")
    fun deleteHistory(
        @Field("historyID") historyID: String
    ): Call<String> //Response : Status Code


}
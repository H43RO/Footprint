package com.haerokim.project_footprint.Network

import com.haerokim.project_footprint.DataClass.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import kotlin.collections.ArrayList

interface RetrofitService {

    /** PLACE 관련 API**/

    // 장소 정보 요청
    @GET("api/places")
    fun requestNaverPlaceID(
        @Query("beacon_uuid") UUID: String
    ): Call<ArrayList<NaverPlaceID>> //Response : NaverPlaceID

    // 핫 플레이스 정보 요청
    @GET("api/hotplaces/")
    fun requestHotPlaceList(): Call<ArrayList<NaverPlaceID>>

    /** 부수기능 관련 API**/

    // 공지사항 요청
    @GET("api/noticelist/")
    fun requestNoticeList(): Call<ArrayList<Notice>>

    /** USER 관련 API**/

    // 로그인 요청
    @FormUrlEncoded
    @POST("api/v1/accounts/login/")
    fun requestLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<User>

    // 사용자 회원정보 수정
    @PUT("userinfo/{userID}/update/")
    fun updateUserInfo(
        @Path("userID") userID: Int,
        @Body body: UpdateProfile
    ): Call<UpdateProfile> //Response : User Object

    // 사용자 탈퇴
    @DELETE("userinfo/{userID}/delete/")
    fun withDrawUser(
        @Path("userID") userID: Int
    ): Call<String>

    // 회원 가입
    @POST("/api/v1/accounts/register/")
    fun registerUser(
        @Body body: RegisterForm
    ): Call<User>

    // 비밀번호 초기화
    @FormUrlEncoded
    @POST("/api/v1/accounts/send-reset-password-link/")
    fun resetPassword(
        @Field("email") email: String
    ): Call<String>


    /** HISTORY 관련 API **/

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

    // 히스토리 수정 (이미지 포함)
    @Multipart
    @PUT("api/histories/{historyID}/edit/")
    fun updateHistoryWithImage(
        @Path("historyID") historyID: Int,
        @Part("title") title: RequestBody,
        @Part("comment") content: RequestBody,
        @Part("mood") mood: RequestBody,
        @Part img: MultipartBody.Part
    ): Call<History>

    // 히스토리 수정 (이미지 미포함)
    @PUT("api/histories/{historyID}/edit/")
    fun updateHistoryWithoutImage(
        @Path("historyID") historyID: Int,
        @Body body: UpdateHistory
    ): Call<History>

    // 히스토리 삭제
    @DELETE("/api/histories/{historyID}/delete/")
    fun deleteHistory(
        @Path("historyID") historyID: Int
    ): Call<String>

    // 임의 히스토리 생성 (이미지 포함)
    @Multipart
    @POST("/api/histories/")
    fun writeHistoryWithImage(
        @Part("user") userID: RequestBody,
        @Part img: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("comment") content: RequestBody,
        @Part("mood") mood: RequestBody,
        @Part("custom_place") customPlace: RequestBody,
        @Part("created_at") createdAt: RequestBody
    ): Call<History>

    // 임의 히스토리 생성 (이미지 미포함)
    @POST("/api/histories/")
    fun writeHistoryNoImage(
        @Body body: WriteHistory
    ): Call<History>
}
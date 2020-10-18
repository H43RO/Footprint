package com.haerokim.project_footprint.DataClass
import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.String

/**  로그인 시 User 정보를 담는 Object -> Paper 에 저장 **/

data class User(
    val id: Int,
    val email: String,
    @SerializedName("birth_date")
    val birth_date: Date,
    val nickname: String,
    val age: Int,
    val gender: Int,
    val token: String
)
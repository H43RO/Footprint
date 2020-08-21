package com.haerokim.project_footprint.DataClass
import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.String

data class User(
    val id: Int,
    val email: String,
    @SerializedName("birth_date")
    val birthDate: Date,
    val nickname: String,
    val age: Int,
    val gender: Int,
    val token: String
)
package com.haerokim.project_footprint.Data
import com.google.gson.annotations.SerializedName
import java.util.*

class User(
    val id: Int,
    val email: String,
    @SerializedName("birth_date")
    val birthDate: Date,
    val nickname: String,
    val age: Int,
    val gender: Int,
    val token: String
)
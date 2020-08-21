package com.haerokim.project_footprint.DataClass

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.String

data class UpdateProfile(
    val nickname: String,
    @SerializedName("birth_date")
    val birthDate: Date,
    val age: Int,
    val gender: Int
)
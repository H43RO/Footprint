package com.haerokim.project_footprint.Data

import com.google.gson.annotations.SerializedName
import java.util.*

class UpdateProfile(
    val nickname: String,
    @SerializedName("birth_date")
    val birthDate: Date,
    val age: Int,
    val gender: Int
)
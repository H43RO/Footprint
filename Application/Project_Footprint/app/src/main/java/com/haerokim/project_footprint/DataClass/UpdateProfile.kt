package com.haerokim.project_footprint.DataClass

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.String

/**  프로필 수정 Data Object  **/

data class UpdateProfile(
    val nickname: String,
    @SerializedName("birth_date")
    val birthDate: Date,
    val age: Int,
    val gender: Int
)
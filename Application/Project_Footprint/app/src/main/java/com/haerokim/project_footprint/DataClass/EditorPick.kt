package com.haerokim.project_footprint.DataClass

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class EditorPick(
    val id: Int,
    val contents: String,
    val title: String,
    val img: String,
    val post_div: Int,
    val description: String,
    val created_at: Date,
    val updated_at: Date
): Parcelable
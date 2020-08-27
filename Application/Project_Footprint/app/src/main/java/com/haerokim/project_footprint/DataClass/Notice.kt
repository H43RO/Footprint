package com.haerokim.project_footprint.DataClass

import java.util.*

/** 공지사항 게시물 Object **/

data class Notice(
    val id: Int,
    val title: String,
    val contents: String,
    val post_div: Int,
    val created_at: Date,
    val updated_at: Date
)
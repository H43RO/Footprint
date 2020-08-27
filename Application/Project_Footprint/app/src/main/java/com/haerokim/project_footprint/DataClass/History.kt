package com.haerokim.project_footprint.DataClass

import java.util.*
import kotlin.String

/**
 *  History Object
 *  - created_at Format : ex) 2020-08-17T22:21:51
 *  - ID, created_at, place, user 필수항목
 **/


data class History(
    var id: Int,
    var img: String?,
    var title: String?,
    var mood: String?,
    var comment: String?,
    var custom_place: String?,
    var created_at: Date?,
    var updated_at: Date?,
    var place: String?,
    var user: Int
)
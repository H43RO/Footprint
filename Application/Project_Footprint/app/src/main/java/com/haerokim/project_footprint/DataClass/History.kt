package com.haerokim.project_footprint.DataClass

import java.util.*
import kotlin.String

//created_at Format : ex) 2017-01-06T22:21:51

class History(
    var id: Int,
    var img: String?,
    var title: String?,
    var mood: Int?,
    var comment: String?,
    var created_at: Date,
    var updated_at: Date?,
    var place: String,
    var user: Int
)
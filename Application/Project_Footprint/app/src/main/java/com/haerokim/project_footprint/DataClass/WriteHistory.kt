package com.haerokim.project_footprint.DataClass

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class WriteHistory(
    var title: String?,
    var mood: String?,
    var comment: String?,
    var custom_place: String?,
    var created_at: LocalDateTime?,
    var updated_at: LocalDateTime?,
    var user: Int
)
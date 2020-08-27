package com.haerokim.project_footprint.DataClass

/**  회원 가입 Form Data Object  **/

data class RegisterForm(
    val email: String,
    val birth_date: String,
    val nickname: String,
    val age: Int,
    val gender: Int,
    val password : String,
    val password_confirm: String
)
package com.haerokim.project_footprint.Activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.DataClass.RegisterForm
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.LoadingDialog
import kotlinx.android.synthetic.main.activity_register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 *  회원가입 기능 제공
 *  - 필수 입력 폼 Validation Check 포함
 **/

class RegisterActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        var userEmail: String
        var userPassword: String
        var userPasswordConfirm: String
        var userBirthDate: String
        var userAge: Int
        var userGender: Int? = null
        var userNickname: String

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        // API 호출을 위한 Retrofit 객체 생성
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var registerService: RetrofitService = retrofit.create(RetrofitService::class.java)

        register_gender.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.male ->  userGender = 0
                R.id.female -> userGender = 1
                R.id.non_specified -> userGender = 2
            }
        }

        button_submit_register.setOnClickListener {
            // Validation Check
            if (register_email.text.isEmpty() || register_password.text.isEmpty() || register_password_confirm.text.isEmpty()
                || register_birth_date.text.isEmpty() || userGender == null || register_nickname.text.isEmpty()
            ) {
                if (register_email.text.isEmpty()) register_email.setError("이메일을 입력해주세요")
                if (register_password.text.isEmpty()) register_password.setError("비밀번호를 입력해주세요")
                if (register_password_confirm.text.isEmpty()) register_password_confirm.setError("비밀번호를 다시 입력해주세요")
                if (register_birth_date.text.isEmpty()) register_birth_date.setError("생년월일을 입력해주세요")
                if (userGender == null) Toast.makeText(this, "성별을 선택해주세요", Toast.LENGTH_LONG).show()
                if (register_nickname.text.isEmpty()) register_nickname.setError("닉네임을 입력해주세요")

            } else if (register_password.text.toString() != register_password_confirm.text.toString()) {
                register_password.setError("비밀번호가 서로 일치하지 않습니다")
                register_password_confirm.setError("비밀번호가 서로 일치하지 않습니다")

            } else if (register_password.text.toString().length !in 8..25) {
                register_password.setError("비밀번호 길이는 8~25자 이내로 해주세요")

            } else if (!Patterns.EMAIL_ADDRESS.matcher(register_email.text.toString()).matches()) {
                register_email.setError("이메일 형식이 올바르지 않습니다")

            } else if (!checkValidationDate(register_birth_date.text.toString())) {
                register_birth_date.setError("생년월일 형식이 올바르지 않습니다")

            } else {
                // 모든 Validation Check 를 통과하면 진입
                LoadingDialog(this).show()
                userEmail = register_email.text.toString()
                userPassword = register_password.text.toString()
                userPasswordConfirm = register_password_confirm.text.toString()
                userBirthDate = register_birth_date.text.toString()
                userNickname = register_nickname.text.toString()

                // 입력된 생년월일을 기반으로 사용자 나이 계산
                val userBirthYear: Int = userBirthDate.substring(0, 4).toInt()
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ISO_DATE
                val formatted = current.format(formatter)
                val currentYear: Int = formatted.substring(0, 4).toInt()
                userAge = (currentYear - userBirthYear) + 1

                val registerForm: RegisterForm = RegisterForm(
                    email = userEmail,
                    password = userPassword,
                    password_confirm = userPasswordConfirm,
                    birth_date = userBirthDate,
                    nickname = userNickname,
                    gender = userGender!!,
                    age = userAge
                )

                registerService.registerUser(registerForm).enqueue(object : Callback<User> {
                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Log.e("Register Error", t.message)
                    }
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.code() == 400) {
                            LoadingDialog(applicationContext).dismiss()
                            register_email.setError("이미 가입된 이메일 입니다.")
                            Toast.makeText(applicationContext, "이미 가입된 이메일 입니다", Toast.LENGTH_LONG).show()
                        } else if (response.code() == 201) {
                            LoadingDialog(applicationContext).dismiss()
                            startActivity(Intent(applicationContext, RegisterConfirmActivity::class.java))
                        }
                    }
                })
            }
        }
    }

    /** 사용자 생년월일 Validation Check 메소드 **/
    fun checkValidationDate(birthDate: String): Boolean {
        val birthDateFormat = SimpleDateFormat("yyyy-MM-dd")
        try {
            birthDateFormat.isLenient()
            birthDateFormat.parse(birthDate)
            return true
        } catch (e: ParseException) {
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LoadingDialog(applicationContext).dismiss()
    }
}

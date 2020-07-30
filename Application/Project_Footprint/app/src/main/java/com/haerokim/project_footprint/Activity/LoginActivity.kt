package com.haerokim.project_footprint.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.haerokim.project_footprint.Data.Login
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Service.RetrofitService
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    var login: Login? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var retrofit = Retrofit.Builder()
            .baseUrl("http://0.0.0.0:8000") //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var loginService: RetrofitService = retrofit.create(RetrofitService::class.java)


        button_login.setOnClickListener {
            var email = edit_text_email.text.toString()
            var password = edit_text_password.text.toString()

            if (email.isEmpty() && password.isEmpty()) {
                if (email.isEmpty()) {
                    edit_text_email.requestFocus()
                    edit_text_email.setError("이메일을 입력해주세요")

                } else if (password.isEmpty()) {
                    edit_text_password.requestFocus()
                    edit_text_password.setError("비밀번호를 입력해주세요")
                }
            } else {
                if (!Patterns.EMAIL_ADDRESS.matcher(edit_text_email.text.toString()).matches()) {
                    edit_text_email.setError("이메일 형식이 올바르지 않습니다")
                } else if (edit_text_password.text.toString().length in 8..25) {
                    edit_text_email.setError("비밀번호 길이는 8자 이상 25자 이하 입니다.")
                } else {
                    // 모든 Validation Check를 통과하면
                    loginService.requestLogin(email, password).enqueue(object : Callback<Login> {
                        override fun onFailure(call: Call<Login>, t: Throwable) {
                            Log.d("success", "로그인 실패")
                        }
                        override fun onResponse(call: Call<Login>, response: Response<Login>) {
                            login = response.body()
                            startActivity(Intent(applicationContext, HomeActivity::class.java))
                            Log.d("success", "로그인 성공")
                        }
                    })
                }

            }
        }
    }
}
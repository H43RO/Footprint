package com.haerokim.project_footprint.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_password_reset.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PasswordResetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var resetPasswordService: RetrofitService = retrofit.create(RetrofitService::class.java)

        button_reset_password.setOnClickListener {
            if (!Patterns.EMAIL_ADDRESS.matcher(edit_text_email_reset_password.text.toString()).matches()) {
                edit_text_email_reset_password.error = "이메일 형식이 올바르지 않습니다"
            } else if (edit_text_email_reset_password.text.isEmpty()) {
                edit_text_email_reset_password.error = "이메일을 입력해주세요"
            } else {
                resetPasswordService.resetPassword(edit_text_email_reset_password.text.toString())
                    .enqueue(object : Callback<String> {
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.e("Reset Password Error", t.message)
                        }

                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.code() == 404) {
                                edit_text_email_reset_password.error = "가입되지 않은 이메일 입니다."
                            } else if (response.code() == 200) {
                                Toast.makeText(applicationContext, "이메일로 비밀번호 초기화 링크를 보냈습니다!", Toast.LENGTH_LONG).show()
                                startActivity(Intent(applicationContext, LoginActivity::class.java))
                            }
                        }
                    })
            }
        }
    }
}
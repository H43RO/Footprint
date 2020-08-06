package com.haerokim.project_footprint.Activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.haerokim.project_footprint.Data.User
import com.haerokim.project_footprint.Data.Website
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.R
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Paper.init(this)

        val autoLogin = getSharedPreferences("auto_login", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = autoLogin.edit()

        if (autoLogin.getBoolean("auto_login_enable", false)) {
            val intent = Intent(applicationContext, HomeActivity::class.java)

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.baseUrl) //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var loginService: RetrofitService = retrofit.create(RetrofitService::class.java)


        button_login.setOnClickListener {
            var email = edit_text_email.text.toString()
            var password = edit_text_password.text.toString()

            if (email.isEmpty() && password.isEmpty()) {
                if (email.isEmpty()) {
                    edit_text_email.requestFocus()
                    edit_text_email.error = "이메일을 입력해주세요"

                } else if (password.isEmpty()) {
                    edit_text_password.requestFocus()
                    edit_text_password.error = "비밀번호를 입력해주세요"
                }
            } else {
                if (!Patterns.EMAIL_ADDRESS.matcher(edit_text_email.text.toString()).matches()) {
                    edit_text_email.error = "이메일 형식이 올바르지 않습니다"
                } else if (edit_text_password.text.toString().length !in 8..25) {
                    edit_text_password.error = "비밀번호는 8자 이상 25자 이하 입니다."
                } else {
                    // 모든 Validation Check를 통과하면
                    loginService.requestLogin(email, password).enqueue(object : Callback<User> {
                        override fun onFailure(call: Call<User>, t: Throwable) {

                        }

                        override fun onResponse(call: Call<User>, response: Response<User>) {

                            //로그인 성공 시 해당 회원의 정보를 로컬에 저장함
                            Paper.book().write("user_profile", response.body())
                            Log.d("Login_Success", response.body()?.nickname)

                            //자동 로그인을 위한 SharedPreference 적용
                            editor.putBoolean("auto_login_enable", true)
                            editor.commit()

                            val intent = Intent(applicationContext, HomeActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            startActivity(intent)
                        }
                    })
                }

            }
        }
    }
}

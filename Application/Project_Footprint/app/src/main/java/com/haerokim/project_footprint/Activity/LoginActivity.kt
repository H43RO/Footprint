package com.haerokim.project_footprint.Activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Utility.LoadingDialog
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 *  로그인 기능
 *  - 로그인, 회원가입 이동, 비밀번호 리셋 페이지 이동 기능 제공
 *  - 회원가입 후 미 인증 회원 예외 처리 포함
 **/

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 'Paper' 라이브러리 사용 - NoSQL Local Database
        // 사용자 정보 저장을 위한 Paper DB 객체 초기화
        Paper.init(this)

        val autoLogin = getSharedPreferences("auto_login", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = autoLogin.edit()

        if (autoLogin.getBoolean("auto_login_enable", false)) {
            val intent = Intent(applicationContext, HomeActivity::class.java)

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        // API 호출을 위한 Retrofit 객체 생성
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var loginService: RetrofitService = retrofit.create(RetrofitService::class.java)

        button_login.setOnClickListener {
            var email = edit_text_email.text.toString()
            var password = edit_text_password.text.toString()

            // Validation Check
            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) {
                    edit_text_email.requestFocus()
                    edit_text_email.error = "이메일을 입력해주세요"
                }
                if (password.isEmpty()) {
                    edit_text_password.requestFocus()
                    edit_text_password.error = "비밀번호를 입력해주세요"
                }
            } else {
                if (!Patterns.EMAIL_ADDRESS.matcher(edit_text_email.text.toString()).matches()) {
                    edit_text_email.error = "이메일 형식이 올바르지 않습니다"
                } else if (edit_text_password.text.toString().length !in 8..25) {
                    edit_text_password.error = "비밀번호는 8자 이상 25자 이하 입니다."
                } else {
                    // 모든 Validation Check 를 통과하면 진입 + 로딩 다이얼로그 호출
                    LoadingDialog(this).show()

                    loginService.requestLogin(email, password).enqueue(object : Callback<User> {
                        override fun onFailure(call: Call<User>, t: Throwable) {
                            LoadingDialog(applicationContext).dismiss()
                            Toast.makeText(applicationContext, "예기치 못한 오류가 발생했습니다", Toast.LENGTH_LONG).show()
                        }
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            //로그인 성공 시 해당 회원의 정보를 로컬에 저장함

                            when(response.code()){
                                400->{  // 올바르지 않은 로그인 정보일 경우
                                    Log.e("login error", "Password Invalid")
                                    Toast.makeText(
                                        applicationContext,
                                        "이메일 및 비밀번호를 다시 확인해주세요",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                404->{  // 인증되지 않은 회원일 경우
                                    Log.e("login error", "Not activated")
                                    Toast.makeText(
                                        applicationContext,
                                        "이메일 인증을 완료해주세요",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                200->{  // 정상적인 로그인일 경우
                                    LoadingDialog(applicationContext).dismiss()

                                    // Paper DB에 사용자의 정보를 저장함
                                    Paper.book().write("user_profile", response.body())
                                    Log.d("login success", response.body()?.nickname)
                                    
                                    //자동 로그인을 위한 SharedPreference 저장 (로그인 정보)
                                    editor.putBoolean("auto_login_enable", true)
                                    editor.apply()

                                    val intent = Intent(applicationContext, HomeActivity::class.java)
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                    })
                }

            }
        }

        text_go_to_reset_password.setOnClickListener{
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }

        button_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LoadingDialog(applicationContext).dismiss()
    }
}
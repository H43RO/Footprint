package com.haerokim.project_footprint.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_register_confirm.*

/**
 *  회원가입 완료 후 인증 안내 페이지
 **/

class RegisterConfirmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_confirm)

        // 로그인 화면으로 Intent
        button_go_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // 이메일 앱으로 Intent
        button_check_email.setOnClickListener {
            val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
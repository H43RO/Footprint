package com.haerokim.project_footprint.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_register_confirm.*

class RegisterConfirmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_confirm)

        button_go_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        button_check_email.setOnClickListener {
            val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
package com.haerokim.project_footprint.Activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.R
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_withdraw.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class WithdrawActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)

        val user: User = Paper.book().read("user_profile")
        text_caution.text = user.nickname + "님의 소중한 기록이 삭제됩니다."

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var service: RetrofitService = retrofit.create(RetrofitService::class.java)

        cancel_withdraw.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        submit_withdraw.setOnClickListener {
            if (edit_text_password.text.toString().isEmpty()) {
                edit_text_password.error = "비밀번호를 입력해주세요"
            } else {
                service.requestLogin(user.email, edit_text_password.text.toString())
                    .enqueue(object : Callback<User> {
                        override fun onFailure(call: Call<User>, t: Throwable) {
                            edit_text_password.error = "올바르지 않은 비밀번호 입니다"
                        }

                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            // 회원 정보 일치하여 API 통해 탈퇴 요청
                            if (user.id == response.body()?.id) {
                                val builder: AlertDialog.Builder =
                                    AlertDialog.Builder(this@WithdrawActivity)
                                builder.setTitle("회원 탈퇴")
                                builder.setMessage("정말 탈퇴하시겠습니까?")
                                builder.setPositiveButton("예",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        service.withDrawUser(user.id)
                                            .enqueue(object : Callback<String> {
                                                override fun onFailure(call: Call<String>, t: Throwable) {
                                                }

                                                override fun onResponse(call: Call<String>, response: Response<String>) {
                                                    Paper.book().destroy()
                                                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                                                }
                                            })
                                    })
                                builder.setNegativeButton("아니오",
                                    DialogInterface.OnClickListener { dialog, which ->
                                    })
                                val alertDialog = builder.create()
                                alertDialog.show()
                                val view: ViewGroup.MarginLayoutParams =
                                    alertDialog.getButton(Dialog.BUTTON_POSITIVE).layoutParams as ViewGroup.MarginLayoutParams
                                view.leftMargin = 16
                                alertDialog.getButton(Dialog.BUTTON_NEGATIVE)
                                    .setBackgroundColor(Color.parseColor("#e8e8e8"))
                                alertDialog.getButton(Dialog.BUTTON_NEGATIVE)
                                    .setTextColor(Color.parseColor("#000000"))

                            } else {
                                edit_text_password.error = "올바르지 않은 비밀번호 입니다"
                            }
                        }
                    })


            }
        }
    }

}
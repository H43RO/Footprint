package com.haerokim.project_footprint.Activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.DataClass.UpdateHistory
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_history_detail.*
import kotlinx.android.synthetic.main.activity_history_detail.history_detail_image
import kotlinx.android.synthetic.main.activity_history_detail.text_history_detail_content
import kotlinx.android.synthetic.main.activity_history_detail.text_history_detail_place
import kotlinx.android.synthetic.main.activity_history_detail.text_history_detail_time
import kotlinx.android.synthetic.main.activity_history_detail.text_history_detail_title
import kotlinx.android.synthetic.main.activity_history_edit.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HistoryEditActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_edit)

        val historyInfo: Bundle? = intent.extras

        var historyID = historyInfo?.getInt("id")
        var historyImage = historyInfo?.getString("image")
        var historyTitle = historyInfo?.getString("title") ?: "어느 멋진 날"
        var historyMood = historyInfo?.getString("mood") //기본 감정 - SoSo
        var historyComment = historyInfo?.getString("comment") ?: "내용 미상"
        var historyPlaceTitle = historyInfo?.getString("placeTitle")
        var historyCreatedAt = historyInfo?.getString("createdAt") ?: "어느 멋진 날"
        var historyUserID = historyInfo?.getInt("userID")

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var updateHistoryService: RetrofitService =
            retrofit.create(RetrofitService::class.java)

        if (historyImage == null) {
            // Image URL 없을 시 기본 이미지
            history_detail_image.setImageResource(R.drawable.placeholder)
        } else {
            Glide.with(this)
                .load(historyImage)
                .centerCrop()
                .thumbnail(0.1f)
                .into(history_detail_image)
        }

        edit_history_detail_title.setText(historyTitle)
        edit_history_detail_place.text = historyPlaceTitle
        edit_history_detail_time.text = historyCreatedAt
        edit_history_detail_content.setText(historyComment)

        val items = resources.getStringArray(R.array.moode_list)
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner_select_mood.adapter = spinnerAdapter

        spinner_select_mood.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // NOP
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {

                    }

                    1 -> {

                    }
                }
//                TODO("Spinner 메뉴 (기분) Int 값 Django History 모델과 조율 필요")
            }
        }

        button_save_history.setOnClickListener {
            historyTitle = edit_history_detail_title.text.toString()
            historyComment = edit_history_detail_content.text.toString()
            val builder: AlertDialog.Builder =
                AlertDialog.Builder(this)
            builder.setTitle("편집하기")
            builder.setMessage("모두 작성하셨나요?")
            builder.setPositiveButton("예",
                DialogInterface.OnClickListener { dialog, which ->
                    val updateHistory =
                        UpdateHistory(historyImage, historyTitle, historyMood, historyComment)
                    updateHistoryService.updateHistory(historyID!!, updateHistory)
                        .enqueue(object : Callback<History> {
                            override fun onFailure(call: Call<History>, t: Throwable) {
                                Log.e("Update History Error", t.message)
                            }

                            override fun onResponse(
                                call: Call<History>,
                                response: Response<History>
                            ) {
                                Log.d("Update History", "History 수정 완료")

                                val resultHistory = response.body()
                                val intent = Intent()

                                intent.putExtra("image", resultHistory?.img)
                                intent.putExtra("title", resultHistory?.title)
                                intent.putExtra("mood", resultHistory?.mood)
                                intent.putExtra("comment", resultHistory?.comment)

                                setResult(Activity.RESULT_OK, intent)
                                finish()
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
        }
    }
}
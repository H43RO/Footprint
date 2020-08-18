package com.haerokim.project_footprint.Activity

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.DataClass.WriteHistory
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.ui.history.TodayHistoryFragment
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_history_write.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*


class HistoryWriteActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_write)

        var user: User = Paper.book().read("user_profile")

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var writeHistoryService: RetrofitService =
            retrofit.create(RetrofitService::class.java)

        var historyImage: String? = null
        var historyTitle: String
        var historyMood: String? = null
        var historyComment: String
        var historyPlaceTitle: String
        var historyUserID: Int

        var historyCreatedAt: String? = null  //historyDate + historyTime
        var historyDate: String? = null
        var historyTime: String? = null

        val items = resources.getStringArray(R.array.moode_list)
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner_select_mood.adapter = spinnerAdapter

        val calendarInstance = Calendar.getInstance()
        val year = calendarInstance.get(Calendar.YEAR)
        val month = calendarInstance.get(Calendar.MONTH)
        val day = calendarInstance.get(Calendar.DAY_OF_MONTH)
        val hour = calendarInstance.get(Calendar.HOUR_OF_DAY)
        val minute = calendarInstance.get(Calendar.MINUTE)
        edit_history_date.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    historyDate =
                        year.toString() + "-" + (month + 1).toString() + "-" + dayOfMonth.toString()
                    Log.d("HistoryCreatedAt", historyDate)
                }, year, month, day
            )
            datePicker.show()
        }

        edit_history_time.setOnClickListener {
            val timePicker = TimePickerDialog(
                this, R.style.DatePicker,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    historyTime = "T" + hourOfDay + ":" + minute + ":00.000000"
                    Log.d("HistoryCreatedAt", historyTime)
                }, hour, minute, false
            )
            timePicker.show()
            val view: ViewGroup.MarginLayoutParams =
                timePicker.getButton(Dialog.BUTTON_POSITIVE).layoutParams as ViewGroup.MarginLayoutParams
            view.leftMargin = 16
            timePicker.getButton(Dialog.BUTTON_NEGATIVE)
                .setBackgroundColor(Color.parseColor("#00000000"))
            timePicker.getButton(Dialog.BUTTON_NEGATIVE)
                .setTextColor(Color.parseColor("#293263"))

            timePicker.getButton(Dialog.BUTTON_POSITIVE)
                .setBackgroundColor(Color.parseColor("#00000000"))
            timePicker.getButton(Dialog.BUTTON_POSITIVE)
                .setTextColor(Color.parseColor("#293263"))
        }

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

        button_upload_history.setOnClickListener {
            if (edit_history_title.text.isEmpty() || edit_history_place.text.isEmpty() || historyDate == null || historyTime == null) {
                if (edit_history_title.text.isEmpty()) edit_history_title.error = "제목은 필수입력 항목입니다."
                if (edit_history_place.text.isEmpty()) edit_history_place.error = "장소명은 필수입력 항목입니다."
                if (historyDate == null) {
                    Toast.makeText(this, "날짜를 입력해주세요", Toast.LENGTH_LONG).show()
                }
                if (historyTime == null) {
                    Toast.makeText(this, "시간을 입력해주세요", Toast.LENGTH_LONG).show()
                }
            } else {
//                TODO("이미지 JSON 처리 어떻게 할지 논의 필요")
//                TODO("기분(Mood) 처리 어떻게 할지 논의 필요")
                historyTitle = edit_history_title.text.toString()
                historyComment = edit_history_content.text.toString()
                historyPlaceTitle = edit_history_place.text.toString()
                historyCreatedAt = historyDate + historyTime

                historyUserID = user.id

                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                val result: Date = format.parse(historyCreatedAt)

                val writtenHistory = WriteHistory(
                    img = historyImage,
                    title = historyTitle,
                    mood = historyMood,
                    comment = historyComment,
                    custom_place = historyPlaceTitle,
                    user = historyUserID,
                    created_at = result,
                    updated_at = result,
                    place = null
                )

                writeHistoryService.writeHistory(writtenHistory).enqueue(object: Callback<History>{
                    override fun onFailure(call: Call<History>, t: Throwable) {
                        Log.e("History Create Failed", t.message)
                    }

                    override fun onResponse(call: Call<History>, response: Response<History>) {
                        if(response.code() == 201){
                            Log.d("History Create Success", "임의 히스토리 생성완료")
                            Toast.makeText(applicationContext, "발자취를 남겼습니다!", Toast.LENGTH_LONG).show()
                            finish()
                        }else{
                            Log.d("History Create Failed", "임의 히스토리 생성실패")
                        }
                    }
                })

            }
        }
    }
}
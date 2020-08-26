package com.haerokim.project_footprint.Activity

import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.LongDef
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_history_write.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class HistoryWriteActivity : AppCompatActivity() {
    var imageUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 업로드를 위한 사진이 선택 및 편집되면 Uri 형태로 결과가 반환됨
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                val bitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                imageUri = bitmapToFile(bitmap!!) // Uri
                edit_history_detail_image.setImageURI(imageUri)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(this)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "write_image.jpg")
        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Error Saving Image", e.message)
        }
        return Uri.parse(file.absolutePath)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_write)

        var user: User = Paper.book().read("user_profile")

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var writeHistoryService: RetrofitService =
            retrofit.create(RetrofitService::class.java)

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

        edit_history_detail_image.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("이미지 추가")
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setCropMenuCropButtonTitle("완료")
                .setRequestedSize(1280, 900)
                .start(this)
        }

        edit_history_date.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    historyDate =
                        year.toString() + "-"
                    historyDate +=
                        if ((monthOfYear + 1) < 10) {
                            "0" + (monthOfYear + 1).toString() + "-"
                        } else {
                            (monthOfYear + 1).toString() + "-"
                        }
                    historyDate +=
                        if (dayOfMonth < 10) {
                            "0$dayOfMonth"
                        } else {
                            "$dayOfMonth"
                        }

                    edit_history_date.text = historyDate
                    historyDate += "T"
                    Log.d("HistoryCreatedAt", historyDate)
                }, year, month, day
            )
            datePicker.show()
        }

        edit_history_time.setOnClickListener {
            val timePicker = TimePickerDialog(
                this, R.style.DatePicker,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    historyTime = "$hourOfDay:"

                    historyTime += if (minute < 10) {
                        "0$minute"
                    } else {
                        minute.toString()
                    }
                    Log.d("HistoryCreatedAt", historyTime)
                    edit_history_time.text = hourOfDay.toString() + "시 " + minute.toString() + "분"
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
                historyTitle = edit_history_title.text.toString()
                historyComment = edit_history_content.text.toString()
                historyPlaceTitle = edit_history_place.text.toString()
                historyCreatedAt = historyDate + historyTime
                historyUserID = user.id

                val localTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LocalDateTime.parse(historyCreatedAt)
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
                Log.d("resultDate", localTime.toString())

                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(this)
                builder.setTitle("작성하기")
                builder.setMessage("모두 작성하셨나요?")
                builder.setPositiveButton("예",
                    DialogInterface.OnClickListener { dialog, which ->
                        if (imageUri != null) {  // 이미지와 함께 업로드 할 시
                            val image = File(imageUri!!.path.toString())
                            val requestFile: RequestBody =
                                RequestBody.create(MediaType.parse("multipart/data"), image)
                            val uploadImage: MultipartBody.Part =
                                MultipartBody.Part.createFormData("img", image.name, requestFile)

                            val userID = RequestBody.create(
                                MediaType.parse("text/plain"),
                                historyUserID.toString()
                            )
                            val title =
                                RequestBody.create(MediaType.parse("text/plain"), historyTitle)
                            val comment =
                                RequestBody.create(MediaType.parse("text/plain"), historyComment)
                            val mood =
                                RequestBody.create(
                                    MediaType.parse("text/plain"),
                                    historyMood ?: "기분 좋았던 순간"
                                )
                            val customPlace = RequestBody.create(
                                MediaType.parse("text/plain"),
                                historyPlaceTitle
                            )
                            val createdAt =
                                RequestBody.create(
                                    MediaType.parse("text/plain"),
                                    localTime.toString()
                                )

                            writeHistoryService.writeHistoryWithImage(
                                userID = userID,
                                img = uploadImage,
                                title = title,
                                content = comment,
                                mood = mood,
                                customPlace = customPlace,
                                createdAt = createdAt
                            ).enqueue(object : Callback<History> {
                                override fun onFailure(call: Call<History>, t: Throwable) {
                                    Log.e("History Create Failed", t.message)
                                }

                                override fun onResponse(
                                    call: Call<History>,
                                    response: Response<History>
                                ) {
                                    if (response.code() == 201) {
                                        Log.d("History Create Success", "임의 히스토리 생성완료")
                                        Toast.makeText(
                                            applicationContext,
                                            "발자취를 남겼습니다!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        finish()
                                    } else {
                                        Log.e("History Create Failed", "임의 히스토리 생성실패")
                                        Log.e("History Create Failed", response.body().toString())
                                    }
                                }
                            })
                        } else {  // 이미지 없이 업로드 할 시
                            writeHistoryService.writeHistoryNoImage(
                                user.id,
                                historyTitle,
                                historyComment,
                                historyPlaceTitle,
                                localTime,
                                historyMood
                            ).enqueue(object : Callback<History> {
                                    override fun onFailure(call: Call<History>, t: Throwable) {
                                        Log.d("History Create Error", t.message)
                                    }

                                    override fun onResponse(
                                        call: Call<History>,
                                        response: Response<History>
                                    ) {
                                        if (response.code() == 201) {
                                            Log.d("History Create Success", "임의 히스토리 생성완료")
                                            Toast.makeText(
                                                applicationContext,
                                                "발자취를 남겼습니다!",
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                            finish()
                                        } else {
                                            Log.d("History Create Failed", "임의 히스토리 생성실패")
                                            Log.e(
                                                "History Create Failed",
                                                response.body().toString()
                                            )
                                        }
                                    }
                                })
                        }
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
}
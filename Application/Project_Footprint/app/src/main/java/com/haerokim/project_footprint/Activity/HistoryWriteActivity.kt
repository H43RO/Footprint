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
import java.time.LocalDateTime
import java.util.*

/**
 *  사용자 임의로 History 생성 기능 제공
 *  - 이미지 수정 및 업로드 시 Retrofit @Multipart 이용
 *  - 'Android Image Cropper' 라이브러리 사용
 **/

class HistoryWriteActivity : AppCompatActivity() {
    var imageUri: Uri? = null

    /**
     *  Android Image Cropper 라이브러리를 통해 이미지 선택 및 편집 완료 후 진입
     *  - imageUri 변수에 업로드 될 이미지의 Uri 값을 넣게 됨
     **/

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
                Log.e("Error Image Selecting", "이미지 선택 및 편집 오류")
            }
        }
    }

    /**  Bitmap 이미지를 Local에 저장하고, URI를 반환함  **/
    private fun bitmapToFile(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(this)

        // Bitmap 파일 저장을 위한 File 객체
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "write_image.jpg")
        try {
            // Bitmap 파일을 JPEG 형태로 압축해서 출력
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_write)

        Paper.init(this)
        var user: User = Paper.book().read("user_profile")

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()

        // API 호출을 위한 Retrofit 객체 생성
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
        var historyCreatedAt: String? = null  // historyDate + historyTime
        var historyDate: String? = null
        var historyTime: String? = null

        // 사용자 기분 선택 구성 요소
        val items = resources.getStringArray(R.array.mood_list)
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner_select_mood.adapter = spinnerAdapter

        // Date, Time Picker 기본 값으로 사용될 현재 날짜 및 시각 가져옴
        val calendarInstance = Calendar.getInstance()
        val year = calendarInstance.get(Calendar.YEAR)
        val month = calendarInstance.get(Calendar.MONTH)
        val day = calendarInstance.get(Calendar.DAY_OF_MONTH)
        val hour = calendarInstance.get(Calendar.HOUR_OF_DAY)
        val minute = calendarInstance.get(Calendar.MINUTE)

        // 히스토리 이미지 추가
        edit_history_detail_image.setOnClickListener {
            // Android Image Cropper 라이브러리 사용
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("이미지 추가")
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setCropMenuCropButtonTitle("완료")
                .setRequestedSize(1280, 900)
                .start(this)
        }

        // 히스토리 날짜 입력
        edit_history_date.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    historyDate =
                        year.toString() + "-"
                    //DatePicker 특성 상 한 자리 날짜 입력에 대한 대응을 해줘야 함
                    historyDate +=
                        if ((monthOfYear + 1) < 10) "0" + (monthOfYear + 1).toString() + "-"
                        else (monthOfYear + 1).toString() + "-"
                    historyDate +=
                        if (dayOfMonth < 10) "0$dayOfMonth"
                        else "$dayOfMonth"

                    edit_history_date.text = historyDate

                    historyDate += "T"
                }, year, month, day
            )
            datePicker.show()
        }

        // 히스토리 시간 입력
        edit_history_time.setOnClickListener {
            val timePicker = TimePickerDialog(
                this, R.style.DatePicker,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    historyTime =
                        if (hourOfDay < 10) "0$hourOfDay:"
                        else hourOfDay.toString() + ":"
                    historyTime +=
                        if (minute < 10) "0$minute"
                        else minute.toString()

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

        // 히스토리 기분 (감정) 입력
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
                    0 -> historyMood = "기분 좋았던 순간"
                    1 -> historyMood = "기뻤던 순간"
                    2 -> historyMood = "평화로웠던 순간"
                    3 -> historyMood = "황홀했던 순간"
                    4 -> historyMood = "행복했던 순간"
                    5 -> historyMood = "뭉클했던 순간"
                    6 -> historyMood = "우울했던 순간"
                    7 -> historyMood = "당황했던 순간"
                    8 -> historyMood = "화났던 순간"
                    9 -> historyMood = "아쉬웠던 순간"
                    10 -> historyMood = "최악이었던 순간"
                }
            }
        }

        // 작성 완료 및 업로드 버튼 눌렀을 때 진입
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

                // 날짜 및 시각은 LocalDateTime 객체 형태로 Request 해야함
                val localTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LocalDateTime.parse(historyCreatedAt)
                } else {
                    TODO("VERSION.SDK_INT < O")
                }

                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(this)
                builder.setTitle("작성하기")
                builder.setMessage("모두 작성하셨나요?")
                builder.setPositiveButton("예",
                    DialogInterface.OnClickListener { dialog, which ->
                        // API 호출 형태가 다르므로 이미지 수정 여부에 따라 다른 메소드 호출함
                        if (imageUri != null) {  // 이미지와 함께 업로드 할 시
                            // 저장된 이미지 Uri를 통해 업로드할 File 객체 생성
                            val image = File(imageUri!!.path.toString())

                            // Django ImageField 에 담을 데이터를 전송할 때는 MultipartBody, RequestBody 등에 데이터 담아야 함
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
                            val customPlace =
                                RequestBody.create(MediaType.parse("text/plain"), historyPlaceTitle)
                            val createdAt =
                                RequestBody.create(
                                    MediaType.parse("text/plain"),
                                    localTime.toString()
                                )

                            // image 포함한 MultipartBody 및 RequestBody 작성
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
                                        ).show()
                                        finish()
                                    } else {
                                        Log.d("History Create Failed", "임의 히스토리 생성실패")
                                        Log.e("History Create Failed", response.body().toString())
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
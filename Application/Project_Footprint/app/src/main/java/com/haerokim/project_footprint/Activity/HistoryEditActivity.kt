package com.haerokim.project_footprint.Activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.DataClass.UpdateHistory
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_history_detail.history_detail_image
import kotlinx.android.synthetic.main.activity_history_edit.*
import kotlinx.android.synthetic.main.fragment_edit_profile.*
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

class HistoryEditActivity : AppCompatActivity() {
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
                history_detail_image.setImageURI(imageUri)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(this)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "profile_image.jpg")
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

        history_detail_image.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("이미지 추가")
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setCropMenuCropButtonTitle("완료")
                .setRequestedSize(1280, 900)
                .start(this)
        }

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
                    // 이미지 수정을 하지 않았을 때 다른 메소드 호출함
                    if (imageUri == null) {
                        val updateHistory =
                            UpdateHistory(historyTitle, historyMood, historyComment)
                        updateHistoryService.updateHistoryWithoutImage(historyID!!, updateHistory)
                            .enqueue(object : Callback<History> {
                                override fun onFailure(call: Call<History>, t: Throwable) {
                                    Log.e("Update History Error", t.message)

                                }

                                override fun onResponse(
                                    call: Call<History>,
                                    response: Response<History>
                                ) {
                                    if (response.code() == 400) {
                                        Log.e("Update History Error", response.message())
                                    } else {
                                        Log.d("Update History", "History 수정 완료")

                                        val resultHistory = response.body()
                                        val intent = Intent()

                                        intent.putExtra("title", resultHistory?.title)
                                        intent.putExtra("mood", resultHistory?.mood)
                                        intent.putExtra("comment", resultHistory?.comment)

                                        setResult(Activity.RESULT_OK, intent)
                                        finish()
                                    }
                                }
                            })
                    } else {  // 이미지 수정했을 시
                        val image = File(imageUri!!.path.toString())
                        val requestFile: RequestBody =
                            RequestBody.create(MediaType.parse("multipart/data"), image)
                        val uploadImage: MultipartBody.Part =
                            MultipartBody.Part.createFormData("img", image.name, requestFile)
                        val title = RequestBody.create(MediaType.parse("text/plain"), historyTitle)
                        val comment =
                            RequestBody.create(MediaType.parse("text/plain"), historyTitle)
                        val mood =
                            RequestBody.create(MediaType.parse("text/plain"), historyMood ?: "1")

                        updateHistoryService.updateHistoryWithImage(
                            historyID = historyID!!,
                            title = title,
                            content = comment,
                            mood = mood,
                            img = uploadImage
                        )
                            .enqueue(object : Callback<History> {
                                override fun onFailure(call: Call<History>, t: Throwable) {
                                    Log.e("Update History Error", t.message)
                                }

                                override fun onResponse(
                                    call: Call<History>,
                                    response: Response<History>
                                ) {
                                    if (response.code() == 400) {
                                        Log.e("Update History Error", response.message())
                                    } else if (response.code() == 200) {
                                        Log.d("Update History", "History 수정 완료")

                                        val resultHistory = response.body()
                                        val intent = Intent()

                                        intent.putExtra("image", imageUri.toString())
                                        intent.putExtra("title", resultHistory?.title)
                                        intent.putExtra("mood", resultHistory?.mood)
                                        intent.putExtra("comment", resultHistory?.comment)

                                        setResult(Activity.RESULT_OK, intent)
                                        finish()
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
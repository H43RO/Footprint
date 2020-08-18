package com.haerokim.project_footprint.Activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.Network.ImageDownloader
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_history_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class HistoryDetailActivity : AppCompatActivity() {
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 수정된 정보가 넘어오면 실행
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            text_history_detail_title.text = data?.getStringExtra("title")
            text_history_detail_content.text = data?.getStringExtra("comment")
            if(data?.getStringExtra("image") != null){
                history_detail_image.setImageURI(Uri.parse(data?.getStringExtra("image")))
            }
//            TODO ("나머지 1개 Extra 미구현 Mood")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)
        val historyInfo: Bundle? = intent.extras

        var historyID = historyInfo?.getInt("id")
        var historyImage = historyInfo?.getString("image")
        var historyTitle = historyInfo?.getString("title") ?: "어느 멋진 날"
        var historyMood = historyInfo?.getString("mood") //기본 감정 - SoSo
        var historyComment = historyInfo?.getString("comment") ?: "당신만의 이야기를 들려주세요."
        var historyPlaceTitle = historyInfo?.getString("placeTitle")
        var historyCreatedAt = historyInfo?.getString("createdAt")
        var historyUserID = historyInfo?.getInt("userID")

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

        text_history_detail_title.text = historyTitle
        text_history_detail_place.text = historyPlaceTitle + "에서"
        text_history_detail_time.text = historyCreatedAt
        text_history_detail_content.text = historyComment

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var deleteHistoryService: RetrofitService =
            retrofit.create(RetrofitService::class.java)

        button_history_detail_action.setOnClickListener {
            val popup: PopupMenu = PopupMenu(this, it)
            popup.inflate(R.menu.history_menu)
            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                when (it.itemId) {
                    R.id.edit_history -> {
                        val intent = Intent(this, HistoryEditActivity::class.java)
                        intent.putExtras(historyInfo!!)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivityForResult(intent, 1)
//                        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                    }

                    R.id.share_history -> {
                        var sns = arrayOf("인스타그램 피드 및 DM", "인스타그램 스토리", "페이스북 피드")
                        val alertDialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                        alertDialog.setTitle("SNS 공유하기")
                            .setItems(sns, DialogInterface.OnClickListener { dialog, which ->
                                when(which){
                                    0->{  // 인스타그램 피드
                                        val snsIntent = Intent(Intent.ACTION_SEND)
                                        val localPath: String = ImageDownloader().execute(historyImage).get()
                                        val uri = FileProvider.getUriForFile(applicationContext, "com.haerokim.project_footprint", File(localPath))

                                        snsIntent.setPackage("com.instagram.android")
                                        snsIntent.setType("image/*")
                                        snsIntent.putExtra(Intent.EXTRA_STREAM, uri)
                                        startActivity(snsIntent)
                                    }
                                    1->{  // 인스타그램 스토리
                                        val snsIntent = Intent("com.instagram.share.ADD_TO_STORY")
                                        val localPath: String = ImageDownloader().execute(historyImage).get()
                                        val uri = FileProvider.getUriForFile(applicationContext, "com.haerokim.project_footprint", File(localPath))
                                        snsIntent.setType("image/*")
                                        snsIntent.putExtra("top_background_color", "#e8e8e8")
                                        snsIntent.putExtra("bottom_background_color", "#e8e8e8")
                                        snsIntent.putExtra("interactive_asset_uri", uri)
                                        this.grantUriPermission("com.instagram.android", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        this.startActivityForResult(snsIntent, 0)
                                    }
                                    2->{  // 페이스북 피드

                                    }
                                }
                            })
                            .setCancelable(true)
                            .show()
                    }

                    R.id.delete_history -> {
                        val builder: AlertDialog.Builder =
                            AlertDialog.Builder(this)
                        builder.setTitle("발자취 삭제")
                        builder.setMessage("정말 삭제하겠습니까?")
                        builder.setPositiveButton("예",
                            DialogInterface.OnClickListener { dialog, which ->
                                deleteHistoryService.deleteHistory(historyID!!).enqueue(object: Callback<String>{
                                    override fun onFailure(call: Call<String>, t: Throwable) {
                                        Log.e("Delete History Error", t.message)
                                    }

                                    override fun onResponse(call: Call<String>, response: Response<String>) {
                                        if(response.code() == 204){
                                            Toast.makeText(applicationContext, "삭제되었습니다", Toast.LENGTH_LONG).show()
                                            finish()
                                        }else{
                                            Log.d("Delete History Error", "삭제 실패")
                                            Toast.makeText(applicationContext, "삭제에 실패했습니다", Toast.LENGTH_LONG).show()
                                        }
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
                true
            })
            popup.show()
        }
    }
}


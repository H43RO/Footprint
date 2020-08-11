package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.Activity.HistoryDetailActivity
import com.haerokim.project_footprint.Activity.PlaceDetailActivity
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.GetPlaceTitleOnly
import kotlinx.android.synthetic.main.history_item.view.*
import java.text.SimpleDateFormat

class HistoryListAdapter(
    private val historyList: ArrayList<History>, private val context: Context
) :
    RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)

        return ViewHolder(
            view
        )
    }

    override fun getItemCount() = historyList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyCreatedFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
        var historyCreatedAt = historyCreatedFormat.format(historyList[position].created_at)
//        var historyCreatedAt = historyList[position].created_at

        holder.view.setOnClickListener {
            val intent: Intent = Intent(context, HistoryDetailActivity::class.java)
            var historyID = historyList[position].id
            var historyImage = historyList[position].img
            var historyTitle = historyList[position].title
            var historyMood = historyList[position].mood ?: "Soso"
            var historyComment = historyList[position].comment
            var historyPlaceTitle = historyList[position].place
            var historyUserID = historyList[position].user

            val bundle: Bundle = Bundle()
            bundle.putInt("id", historyID)
//            bundle.putString("image", historyImage)
            bundle.putString("title", historyTitle)
            bundle.putString("mood", historyMood)
            bundle.putString("comment", historyComment)
            bundle.putString("createdAt", historyCreatedAt)
            bundle.putString("placeTitle", historyPlaceTitle)
            bundle.putInt("userID", historyUserID)

            //번들 intent data로 담아줌
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
        holder.view.text_history_title.text = historyList[position].title ?: "탭 하여 작성하기"
        holder.view.text_history_detail.text =
            historyList[position].place + "에서, " + historyCreatedAt
        Glide.with(holder.view) // 확인 필요
            .load(historyList[position].img)
            .centerCrop()
            .override(600, 400)
            .thumbnail(0.1f)
            .into(holder.view.image_history)
    }
}
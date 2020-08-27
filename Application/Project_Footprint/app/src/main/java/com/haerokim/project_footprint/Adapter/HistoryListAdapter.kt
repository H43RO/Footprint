package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.Activity.HistoryDetailActivity
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.history_item.view.*
import java.text.SimpleDateFormat

/**
 *  사용자 히스토리 (다이어리) 리스트 RecyclerView Adapter
 *  - API 를 통해 얻은 히스토리 리스트를 데이터로 가짐
 **/

class HistoryListAdapter(
    private val historyList: ArrayList<History>,
    private val context: Context
) : RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val historyCreatedFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
            var historyCreatedAt = historyCreatedFormat.format(historyList.get(adapterPosition-1).created_at)

            val intent: Intent = Intent(context, HistoryDetailActivity::class.java)
            var historyID = historyList[adapterPosition-1].id
            var historyImage = historyList[adapterPosition-1].img
            var historyTitle = historyList[adapterPosition-1].title
            var historyMood = historyList[adapterPosition-1].mood ?: "Soso"
            var historyComment = historyList[adapterPosition-1].comment
            var historyPlaceTitle =
                if (historyList[adapterPosition-1].place == null) historyList[adapterPosition-1].custom_place else historyList.get(adapterPosition-1).place
            var historyUserID = historyList[adapterPosition-1].user

            val bundle: Bundle = Bundle()
            bundle.putInt("id", historyID)
            bundle.putString("image", historyImage)
            bundle.putString("title", historyTitle)
            bundle.putString("mood", historyMood)
            bundle.putString("comment", historyComment)
            bundle.putString("createdAt", historyCreatedAt)
            bundle.putString("placeTitle", historyPlaceTitle)
            bundle.putInt("userID", historyUserID)

            // Bundle Data 를 담아 HistoryDetailActivity 로 이동
            intent.putExtras(bundle)
            context.startActivity(intent)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = historyList.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyCreatedFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
        var historyCreatedAt = historyCreatedFormat.format(historyList[position].created_at)

        holder.view.text_history_title.text =
            historyList[position].title ?: historyList[position].place + "에서의 추억"
        holder.view.text_history_detail.text =
            if (historyList[position].place == null) historyList[position].custom_place + "에서, " + historyCreatedAt else historyList[position].place + "에서, " + historyCreatedAt

        if (historyList[position].img == null) {
            holder.view.image_history.visibility = View.GONE
        } else {
            Glide.with(holder.view) // 확인 필요
                .load(historyList[position].img)
                .centerCrop()
                .override(600, 400)
                .thumbnail(0.1f)
                .into(holder.view.image_history)
        }
    }
}


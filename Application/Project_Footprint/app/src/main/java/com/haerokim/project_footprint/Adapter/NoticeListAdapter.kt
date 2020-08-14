package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.Activity.HistoryDetailActivity
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.DataClass.Notice
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.history_item.view.*
import kotlinx.android.synthetic.main.notice_item.view.*
import java.text.SimpleDateFormat

class NoticeListAdapter(private val noticeList: ArrayList<Notice>, private val context: Context) :
    RecyclerView.Adapter<NoticeListAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoticeListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notice_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = noticeList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val noticeCreatedFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
        val noticeCreatedAt = noticeCreatedFormat.format(noticeList[position].created_at)

        holder.view.setOnClickListener {

        }

        holder.view.text_notice_title.text = noticeList[position].title
        holder.view.text_notice_date.text = noticeCreatedAt
    }
}

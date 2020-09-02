package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.haerokim.project_footprint.DataClass.Notice
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.notice_item.view.*
import java.text.SimpleDateFormat

/**
 *  공지사항 리스트 RecyclerView Adapter
 *  - API 를 통해 얻은 공지사항 리스트를 데이터로 가짐
 **/

class NoticeListAdapter(private val noticeList: ArrayList<Notice>, private val context: Context) :
    RecyclerView.Adapter<NoticeListAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notice_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = noticeList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val noticeCreatedFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
        val noticeCreatedAt = noticeCreatedFormat.format(noticeList[position].created_at)

        holder.view.setOnClickListener {
            var bundle = bundleOf(
                "notice_title" to noticeList[position].title,
                "notice_content" to noticeList[position].contents,
                "notice_date" to noticeCreatedAt
            )

            // Fragment to Fragment 이므로 navigate() 이용 + Bundle 동봉
            it.findNavController().navigate(R.id.action_navigation_notice_to_navigation_notice_detail, bundle)
        }

        holder.view.text_notice_title.text = noticeList[position].title
        holder.view.text_notice_date.text = noticeCreatedAt
    }
}

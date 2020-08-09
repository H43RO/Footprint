package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.GetPlaceTitleOnly
import kotlinx.android.synthetic.main.history_item.view.*

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
        holder.view.setOnClickListener {

        }
        holder.view.text_history_title.text = historyList[position].title ?: "탭 하여 작성하기"
        holder.view.text_history_detail.text = historyList[position].place + "에서, " + historyList[position].created_at
        Glide.with(holder.view) // 확인 필요
            .load(historyList[position].img)
            .centerCrop()
            .into(holder.view.image_history)
    }
}
package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.DataClass.Place
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.ShowPlaceInfo
import kotlinx.android.synthetic.main.hot_place_item.view.*
import kotlinx.android.synthetic.main.place_item.view.*

class HotPlaceListAdapter(
    private val hotPlaceList: ArrayList<Place>,
    private val context: Context
) : RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hot_place_item, parent, false)

        return HistoryListAdapter.ViewHolder(view)
    }

    override fun getItemCount() = hotPlaceList.size

    override fun onBindViewHolder(holder: HistoryListAdapter.ViewHolder, position: Int) {
        holder.view.setOnClickListener {
            ShowPlaceInfo(context, hotPlaceList[position].naverPlaceID).showInfo(hotPlaceList[position])
        }
        holder.view.text_hot_place_title.text = hotPlaceList[position].title
        holder.view.text_hot_place_category.text = hotPlaceList[position].category

        Glide.with(holder.view) // 확인 필요
            .load(hotPlaceList[position].imageSrc)
            .centerCrop()
            .override(600, 400)
            .into(holder.view.hot_place_image)
    }
}
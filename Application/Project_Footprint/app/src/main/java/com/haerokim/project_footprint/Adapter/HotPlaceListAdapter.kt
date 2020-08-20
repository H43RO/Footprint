package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.Adapter.HotPlaceListAdapter.ViewHolder
import com.haerokim.project_footprint.DataClass.Place
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.ShowPlaceInfo
import kotlinx.android.synthetic.main.hot_place_item.view.*
import kotlinx.android.synthetic.main.place_item.view.*

class HotPlaceListAdapter(
    private val hotPlaceList: ArrayList<Place>,
    private val context: Context
) : RecyclerView.Adapter<ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            view.setOnClickListener {
                ShowPlaceInfo(context, hotPlaceList[adapterPosition - 1].naverPlaceID).showInfo(
                    hotPlaceList[adapterPosition - 1]
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hot_place_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = hotPlaceList.size

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.text_hot_place_title.text = hotPlaceList[position].title
        holder.view.text_hot_place_category.text = hotPlaceList[position].category

        Glide.with(holder.view) // 확인 필요
            .load(hotPlaceList[position].imageSrc)
            .centerCrop()
            .override(600, 400)
            .into(holder.view.hot_place_image)
    }
}
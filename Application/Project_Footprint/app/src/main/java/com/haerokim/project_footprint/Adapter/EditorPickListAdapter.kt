package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.DataClass.EditorPick
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.editor_item.view.*
import kotlinx.android.synthetic.main.history_item.view.*
import kotlinx.android.synthetic.main.hot_place_item.view.*

class EditorPickListAdapter(
    private val editorPickList: ArrayList<EditorPick>,
    private val context: Context
) : RecyclerView.Adapter<EditorPickListAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
//            val historyCreatedFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
//            var historyCreatedAt = historyCreatedFormat.format(historyList.get(adapterPosition-1).created_at)
//
//            val intent: Intent = Intent(context, HistoryDetailActivity::class.java)
//            var historyID = historyList[adapterPosition-1].id
//            var historyImage = historyList[adapterPosition-1].img
//            var historyTitle = historyList[adapterPosition-1].title
//            var historyMood = historyList[adapterPosition-1].mood ?: "Soso"
//            var historyComment = historyList[adapterPosition-1].comment
//            var historyPlaceTitle =
//                if (historyList[adapterPosition-1].place == null) historyList[adapterPosition-1].custom_place else historyList.get(adapterPosition-1).place
//            var historyUserID = historyList[adapterPosition-1].user
//
//            val bundle: Bundle = Bundle()
//            bundle.putInt("id", historyID)
//            bundle.putString("image", historyImage)
//            bundle.putString("title", historyTitle)
//            bundle.putString("mood", historyMood)
//            bundle.putString("comment", historyComment)
//            bundle.putString("createdAt", historyCreatedAt)
//            bundle.putString("placeTitle", historyPlaceTitle)
//            bundle.putInt("userID", historyUserID)
//
//            //번들 intent data로 담아줌
//            intent.putExtras(bundle)
//            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.editor_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = editorPickList.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.view.text_editor_title.text = editorPickList[position].title
        holder.view.text_editor_content.text = editorPickList[position].contents

        Glide.with(holder.view) // 확인 필요
            .load(editorPickList[position].img)
            .centerCrop()
            .override(600, 400)
            .thumbnail(0.1f)
            .into(holder.view.image_editor_pick)
    }
}


package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.Activity.EditorPickDeatilActivity
import com.haerokim.project_footprint.DataClass.EditorPick
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.editor_item.view.*
import kotlinx.android.synthetic.main.home_editor_item.view.*

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
            val intent = Intent(context, EditorPickDeatilActivity::class.java)
            val bundle: Bundle = Bundle()

            val title = editorPickList[adapterPosition - 1].title
            val contents= editorPickList[adapterPosition - 1].contents
            val img= editorPickList[adapterPosition - 1].img
            val description= editorPickList[adapterPosition - 1].description

            bundle.putString("title", title)
            bundle.putString("contents", contents)
            bundle.putString("img", img)
            bundle.putString("description", description)

            //번들 intent data로 담아줌
            intent.putExtras(bundle)
            context.startActivity(intent)
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


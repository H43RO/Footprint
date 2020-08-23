package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.Activity.EditorPickDeatilActivity
import com.haerokim.project_footprint.DataClass.EditorPick
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.home_editor_item.view.*

class EditorPickViewPagerAdapter(val context: Context, val editorPickList: ArrayList<EditorPick>) :
    PagerAdapter() {
    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.home_editor_item, container, false)

        view.home_text_editor_title.text = editorPickList[position].title
        view.home_text_editor_content.text = editorPickList[position].contents

        Glide.with(context)
            .load(editorPickList[position].img)
            .centerCrop()
            .thumbnail(0.1f)
            .into(view.home_image_editor_pick)

        view.setOnClickListener {
            val intent = Intent(context, EditorPickDeatilActivity::class.java)
            val bundle: Bundle = Bundle()

            val title = editorPickList[position].title
            val contents = editorPickList[position].contents
            val img = editorPickList[position].img
            val description = editorPickList[position].description

            bundle.putString("title", title)
            bundle.putString("contents", contents)
            bundle.putString("img", img)
            bundle.putString("description", description)

            //번들 intent data로 담아줌
            intent.putExtras(bundle)
            context.startActivity(intent)
        }

        container.addView(view)
        return view
    }

    override fun getCount(): Int {
        return editorPickList.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View?)
    }
}
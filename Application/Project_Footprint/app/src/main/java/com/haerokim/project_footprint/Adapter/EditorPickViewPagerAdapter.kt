package com.haerokim.project_footprint.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.DataClass.EditorPick
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.editor_item.view.*

class EditorPickViewPagerAdapter(val context: Context, val editorPickList: ArrayList<EditorPick>): PagerAdapter() {
    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.editor_item, container, false)

        view.text_editor_title.text = editorPickList[position].title
        view.text_editor_content.text = editorPickList[position].contents

        Glide.with(context)
            .load(editorPickList[position].img)
            .centerCrop()
            .thumbnail(0.1f)
            .into(view.image_editor_pick)

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
package com.haerokim.project_footprint.ui.notice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.fragment_notcie_detail.*

class NoticeDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notcie_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        text_notice_detail_title.text = arguments?.getString("notice_title")
        text_notice_detail_content.text = arguments?.getString("notice_content")
        text_notice_detail_time.text = arguments?.getString("notice_date")
    }
}
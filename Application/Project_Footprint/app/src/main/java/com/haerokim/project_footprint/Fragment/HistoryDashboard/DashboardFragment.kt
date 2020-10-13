package com.haerokim.project_footprint.Fragment.HistoryDashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.haerokim.project_footprint.Activity.HistoryWriteActivity
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.fragment_dashboard.*

/**  History 조회 및 작성 메뉴 선택 기능 제공  **/

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        card_today_history.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_today_history)
        }

        card_date_history.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_date_history)
        }

        card_keyword_history.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_keyword_history)
        }

        card_write_history.setOnClickListener {
            startActivity(Intent(context, HistoryWriteActivity::class.java))
        }
    }
}
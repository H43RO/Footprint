package com.haerokim.project_footprint.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.haerokim.project_footprint.ForegroundService
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)

        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val foreground = Intent(context, ForegroundService::class.java)

        toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= 26) {
                    context?.startForegroundService(foreground)
                } else {
                    context?.startService(foreground)
                }
            } else {
                context?.stopService(foreground)
            }
        }

    }
}
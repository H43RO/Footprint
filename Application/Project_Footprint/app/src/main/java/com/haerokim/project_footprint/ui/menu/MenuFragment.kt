package com.haerokim.project_footprint.ui.menu

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.haerokim.project_footprint.Data.User
import com.haerokim.project_footprint.R
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : Fragment() {

    private lateinit var menuViewModel: MenuViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        menuViewModel =
            ViewModelProviders.of(this).get(MenuViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_menu, container, false)


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user: User = Paper.book().read("user_profile")
        text_menu_user_nickname.text = user.nickname

        frame_profile_edit.bringChildToFront(icon_edit_profile)

        image_user_profile.setBackground(ShapeDrawable(OvalShape()))
        image_user_profile.setClipToOutline(true)

        frame_profile_edit.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_menu_to_navigation_edit_profile)
        }


    }
}
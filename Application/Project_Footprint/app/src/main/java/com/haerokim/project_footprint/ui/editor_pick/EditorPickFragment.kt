package com.haerokim.project_footprint.ui.editor_pick

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haerokim.project_footprint.Adapter.EditorPickListAdapter
import com.haerokim.project_footprint.Adapter.HotPlaceListAdapter
import com.haerokim.project_footprint.DataClass.EditorPick
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.R

class EditorPickFragment : Fragment() {
    var editorPickList: ArrayList<EditorPick> = arrayListOf()
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_editor_pick, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editorPickList = arguments?.getParcelableArrayList<EditorPick>("editorPickList") as ArrayList<EditorPick>

        viewManager =
            LinearLayoutManager(context)
        viewAdapter = EditorPickListAdapter(editorPickList, requireContext())

        viewAdapter.setHasStableIds(true)

        recyclerView =
            view.findViewById<RecyclerView>(R.id.editor_pick_list).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }

    }
}
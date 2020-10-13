package com.haerokim.project_footprint.Fragment.EditorPick

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haerokim.project_footprint.Adapter.EditorPickListAdapter
import com.haerokim.project_footprint.DataClass.EditorPick
import com.haerokim.project_footprint.R

/**
 *  에디터 추천 장소 리스트 기능 제공
 *  - EditorPickListAdapter 를 통해 RecyclerView 구현
 **/

class EditorPickFragment : Fragment() {
    var editorPickList: ArrayList<EditorPick> = arrayListOf()
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editor_pick, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // HomeFragment 에서 넘어온 EditorPick List Bundle Data
        editorPickList = arguments?.getParcelableArrayList<EditorPick>("editorPickList") as ArrayList<EditorPick>

        viewManager =
            LinearLayoutManager(context)
        viewAdapter = EditorPickListAdapter(editorPickList, requireContext())

        viewAdapter.setHasStableIds(true)

        // 에디터 추천 장소 리스트를 보여주는 RecyclerView 설정
        recyclerView =
            view.findViewById<RecyclerView>(R.id.editor_pick_list).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }

    }
}
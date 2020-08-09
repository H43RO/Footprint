package com.haerokim.project_footprint.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haerokim.project_footprint.Data.History
import com.haerokim.project_footprint.Data.HistoryListAdapter
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.ui.surround.SurroundFragment
import kotlinx.android.synthetic.main.fragment_dashboard.*

class WholeHistoryFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
     var historyList: ArrayList<History> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_whole_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState )
        historyList.add(History(1,"https://search.pstatic.net/common/?autoRotate=true&quality=95&src=http%3A%2F%2Fldb.phinf.naver.net%2F20190516_87%2F1557942349202W596n_JPEG%2FPTiwb5M_pwa_4PBBXZ1H_aAN.jpeg.jpg&type=m1000_692",
        "연남동도 식후경", 2, "맛있었당", "2020년 7월 25일 13시 34분", "", "연남동 질리", 1))

        historyList.add(History(1,"https://search.pstatic.net/common/?autoRotate=true&quality=95&src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyMDA3MTdfMjg1%2FMDAxNTk0OTk0MDUzOTg4.lq4A28aW911VkJju6oeNhz2FFoYBL8TFUZGAcZ9ZKEIg.SZqy9d2QkZYMzuq9HODsLfqQtsGS91ze-UUJesYq1Acg.JPEG.gg12200%2F1594994054392.jpg&type=m862_636",
            "그들의 수다떨기", 2, "맛있었당", "2020년 7월 25일 15시 08분", "", "어나더룸", 1))


        viewManager = LinearLayoutManager(context)
        viewAdapter = HistoryListAdapter(
                historyList,
                requireContext()
            )

        recyclerView = view.findViewById<RecyclerView>(R.id.history_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }


    }
}
package com.haerokim.project_footprint.ui.history

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haerokim.project_footprint.Adapter.HistoryListAdapter
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.GetPlaceTitleOnly
import io.paperdb.Paper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TodayHistoryList : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
    var historyList: ArrayList<History> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_today_history_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user: User = Paper.book().read("user_profile")

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var getWholeHistory: RetrofitService = retrofit.create(RetrofitService::class.java)

//        * 오늘의 History를 조회하기 위해 Query Params에 넣을 날짜 값을 구한다.
        val todayDate: Date = Calendar.getInstance().time
        val historyCreatedFormat = SimpleDateFormat("yyyy-MM-dd")
        var historyCreatedAt =historyCreatedFormat.format(todayDate)

        getWholeHistory.requestTodayHistoryList(user.id, historyCreatedAt)
            .enqueue(object : Callback<ArrayList<History>> {
                override fun onFailure(call: Call<ArrayList<History>>, t: Throwable) {
                    Log.e("Error", t.message)
                }

                override fun onResponse(
                    call: Call<ArrayList<History>>,
                    response: Response<ArrayList<History>>
                ) {
                    historyList = response.body() ?: ArrayList()
                    for (history in historyList) {
                        history.place = GetPlaceTitleOnly(history.place).execute().get()
                        Log.d("Today History 등록 완료", history.place)
                    }
                }
            })

//         Adapter에 List를 넘기기 전에, List 내의 모든 장소들의 Title을 먼저 얻어야함
//         또한, created_at Format 재정의하여 어댑팅할 것

        viewManager = LinearLayoutManager(context)
        viewAdapter = HistoryListAdapter(
            historyList,
            requireContext()
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.today_history_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

    }
}
package com.haerokim.project_footprint.ui.history

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.Adapter.HistoryListAdapter
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.GetPlaceTitleOnly
import io.paperdb.Paper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.ArrayList

class WholeHistoryFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
    var historyList: ArrayList<History> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_whole_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var user: User = Paper.book().read("user_profile")

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        var getWholeHistory: RetrofitService = retrofit.create(RetrofitService::class.java)


//        * 날짜별 조회시 필요
//        val todayDate:Date = Calendar.getInstance().time
//        val historyCreatedFormat = SimpleDateFormat("yyyy-MM-dd")
//        var historyCreatedAt =historyCreatedFormat.format(todayDate)

        Log.d("User ID", user?.id.toString())
        getWholeHistory.requestWholeHistoryList(user!!.id)
            .enqueue(object : Callback<ArrayList<History>>{
                override fun onFailure(call: Call<ArrayList<History>>, t: Throwable) {
                    Log.e("Whole_history_Error", t.message)
                }

                override fun onResponse(
                    call: Call<ArrayList<History>>,
                    response: Response<ArrayList<History>>
                ) {
                    historyList = response.body()!!
                    for (history in historyList) {
                        history.place = GetPlaceTitleOnly(history.place).execute().get()
                        Log.d("정보 획득", "장소명" + history.place + ", 타이틀 : " + history.title)
                    }

                    viewManager = LinearLayoutManager(context)
                    viewAdapter = HistoryListAdapter(
                        historyList,
                        requireContext()
                    )

                    recyclerView = view.findViewById<RecyclerView>(R.id.whole_history_list).apply {
                        setHasFixedSize(true)
                        layoutManager = viewManager
                        adapter = viewAdapter
                    }
                }
            })


    }


}
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
import com.haerokim.project_footprint.Adapter.HistoryListAdapter
import com.haerokim.project_footprint.DataClass.History
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.DataClass.VisitedPlace
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.GetPlaceTitleOnly
import io.paperdb.Paper
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.fragment_today_history.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TodayHistoryFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
    var historyList: ArrayList<History> = ArrayList()
    var responseBody: ArrayList<History> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_today_history, container, false)
    }

    override fun onResume() {
        super.onResume()
        getTodayHistoryList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Realm을 활용해 장소의 정보를 Local에 저장하게 됨
        Realm.init(context)
        Paper.init(context)

        viewManager = LinearLayoutManager(context)
        viewAdapter = HistoryListAdapter(
            historyList,
            requireContext()
        )
        recyclerView =
            view.findViewById<RecyclerView>(R.id.today_history_list).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
    }

    fun getTodayHistoryList(){
        text_today_no_data.visibility = View.GONE
        loading_today_history.visibility = View.VISIBLE

        val config: RealmConfiguration = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()

        Realm.setDefaultConfiguration(config)

        var realm = Realm.getDefaultInstance()
        val user: User = Paper.book().read("user_profile")
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        var getTodayHistoryService: RetrofitService = retrofit.create(RetrofitService::class.java)

//        * 오늘의 History를 조회하기 위해 Query Params에 넣을 오늘 날짜 값을 구한다.
        val todayDate: Date = Calendar.getInstance().time
        val historyCreatedFormat = SimpleDateFormat("yyyy-MM-dd")
        var historyCreatedAt = historyCreatedFormat.format(todayDate)

        getTodayHistoryService.requestTodayHistoryList(user.id, historyCreatedAt)
            .enqueue(object : Callback<ArrayList<History>> {
                override fun onFailure(call: Call<ArrayList<History>>, t: Throwable) {
                    Log.e("Error", t.message)
                    today_history_list.visibility = View.GONE
                    text_today_no_data.visibility = View.VISIBLE
                    loading_today_history.visibility = View.GONE
                    text_today_no_data.text = "정보를 가져오지 못했습니다"
                }
                override fun onResponse(
                    call: Call<ArrayList<History>>,
                    response: Response<ArrayList<History>>
                ) {
                    historyList.clear()
                    text_today_no_data.visibility = View.GONE

                    if (response.body()?.size == 0) {
                        today_history_list.visibility = View.GONE
                        text_today_no_data.visibility = View.VISIBLE
                        loading_today_history.visibility = View.GONE
                        text_today_no_data.text = "기록이 없습니다"
                    } else {
                        today_history_list.visibility = View.VISIBLE
                        responseBody = response.body()!!
                        for (history in responseBody) {
                            realm.executeTransaction {
                                val visitedPlace: VisitedPlace? = it.where(VisitedPlace::class.java).equalTo("naverPlaceID", history.place).findFirst()
                                history.place = visitedPlace?.placeTitle ?: GetPlaceTitleOnly(history.place).execute().get()
                            }
                        }
                        historyList.addAll(responseBody)
                        viewAdapter.notifyDataSetChanged()
                        loading_today_history.visibility = View.GONE
                    }
                }
            })
    }
}
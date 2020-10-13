package com.haerokim.project_footprint.Fragment.History

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.Activity.HistoryWriteActivity
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

/**
 *  당일 History 조회 기능 제공
 *  - 진입 시 자동으로 당일 History 리스트를 보내주는 API 호출
 *  - HistoryListAdapter 를 통해 RecyclerView 구현
 *  - History 작성 기능도 제공하기 때문에, onResume() 이 호출될 때마다
 *    리스트 아이템 바인딩을 다시 해줘야 함 (History 작성 시 아이템의 변화가 일어나기 때문)
 **/

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
        // Realm 사용을 위해 init() 필요
        Realm.init(context)

        return inflater.inflate(R.layout.fragment_today_history, container, false)
    }

    override fun onResume() {
        super.onResume()
        getTodayHistoryList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 저장된 장소 정보를 활용하기 위해 Realm 객체 생성 및 초기화
        val config: RealmConfiguration = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()

        Realm.setDefaultConfiguration(config)

        viewManager = LinearLayoutManager(context)
        viewAdapter = HistoryListAdapter(
            historyList,
            requireContext()
        )

        // 오늘의 History 리스트를 보여주는 RecyclerView 설정
        recyclerView =
            view.findViewById<RecyclerView>(R.id.today_history_list).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }

        button_write_history.setOnClickListener {
            startActivity(Intent(requireContext(), HistoryWriteActivity::class.java))
        }

            getTodayHistoryList()

    }

    // onResume() 실행될 때마다 진입 (리스트 아이템 바인딩 동작)
    fun getTodayHistoryList() {
        text_today_no_data.visibility = View.GONE
        loading_today_history.visibility = View.VISIBLE

        var realm = Realm.getDefaultInstance()
        val user: User = Paper.book().read("user_profile")

        // API 호출 시 DateField Format 지정해줘야 함
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()

        // API 호출을 위한 Retrofit 객체 생성
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        var getTodayHistoryService: RetrofitService = retrofit.create(RetrofitService::class.java)

        // API 호출을 위 Query Params 에 넣을 오늘 날짜 값을 구함
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

                override fun onResponse(call: Call<ArrayList<History>>, response: Response<ArrayList<History>>) {
                    historyList.clear()
                    text_today_no_data.visibility = View.GONE

                    // 해당 날짜에 기록이 없을 경우 진입
                    if (response.body()?.size == 0 || response.body() == null) {
                        today_history_list.visibility = View.GONE
                        text_today_no_data.visibility = View.VISIBLE
                        loading_today_history.visibility = View.GONE
                        text_today_no_data.text = "기록이 없습니다"
                    } else {  // 기록이 있을 경우 진입
                        today_history_list.visibility = View.VISIBLE
                        responseBody = response.body()!!
                        // History 객체 각각의 place 속성에 Naver Place ID가 담겨있기 때문에 장소명으로 변환해줘야함
                        for (history in responseBody) {
                            // place 가 null 이면 임의로 생성한 History 이므로 custom_place 속성에 정보가 이미 있음
                            if (history.place != null) {
                                // 장소 이름이 Realm 에 저장되어 있으면 (방문한 적 있으면 캐싱됨) 사용하고, 없으면 GetPlaceTitleOnly() 호출
                                realm.executeTransaction {
                                    val visitedPlace: VisitedPlace? =
                                        it.where(VisitedPlace::class.java).equalTo("naverPlaceID", history.place).findFirst()
                                    history.place = visitedPlace?.placeTitle ?: GetPlaceTitleOnly(history.place!!).execute().get()
                                }
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
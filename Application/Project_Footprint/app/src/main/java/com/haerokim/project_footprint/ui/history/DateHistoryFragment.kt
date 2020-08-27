package com.haerokim.project_footprint.ui.history

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
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
import kotlinx.android.synthetic.main.fragment_date_history.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 *  날짜별 History 조회 기능 제공
 *  - 사용자가 CalendarView 를 통해 날짜를 선택할 때마다 API 호출
 *  - HistoryListAdapter 를 통해 RecyclerView 구현
 *  - 달력의 Extendable 한 UI를 위해 Animation 사용
 **/

class DateHistoryFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_date_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // API 호출 시 DateField Format 지정해줘야 함
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()

        // API 호출을 위한 Retrofit 객체 생성
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        var getDateHistory: RetrofitService = retrofit.create(RetrofitService::class.java)

        viewManager = LinearLayoutManager(context)
        viewAdapter = HistoryListAdapter(
            historyList,
            requireContext()
        )

        // 조회한 날짜의 History 리스트를 보여주는 RecyclerView 설정
        recyclerView =
            view.findViewById<RecyclerView>(R.id.date_history_list).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }

        text_date_no_data.visibility = View.GONE
        loading_date_history.visibility = View.GONE

        // 저장된 장소 정보를 활용하기 위해 Realm 객체 생성 및 초기화
        val config: RealmConfiguration = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(config)
        var realm = Realm.getDefaultInstance()

        // API 구조 상 [날짜 정보, 사용자 ID] 를 파라미터로 받기 때문에
        // User 정보를 얻어야 함 (Paper DB에 저장되어 있음)
        val user: User = Paper.book().read("user_profile")

        var selectDate: String

        // 사용자가 날짜를 선택할 때마다 진입
        calendar.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
            selectDate = "" + year + "-" + (month + 1) + "-" + dayOfMonth
            Log.d("Test", selectDate)

            loading_date_history.visibility = View.VISIBLE
            date_history_list.visibility = View.GONE

            // 사용자 식별 ID, 날짜 범위 (사용자가 선택한 날짜만 조회하므로 동일한 값 넣음) 와 함께 API 호출
            getDateHistory.requestDateHistoryList(user.id, selectDate, selectDate)
                .enqueue(object : Callback<ArrayList<History>> {
                    override fun onFailure(call: Call<ArrayList<History>>, t: Throwable) {
                        Log.e("Error", t.message)
                        date_history_list.visibility = View.GONE
                        text_date_no_data.visibility = View.VISIBLE
                        loading_date_history.visibility = View.GONE
                        text_date_no_data.text = "정보를 가져오지 못했습니다"
                    }
                    override fun onResponse(
                        call: Call<ArrayList<History>>,
                        response: Response<ArrayList<History>>
                    ) {
                        historyList.clear()
                        responseBody.clear()
                        text_date_no_data.visibility = View.GONE

                        // 해당 날짜에 기록이 없을 경우 진입
                        if (response.body()?.size == 0 || response.body() == null) {
                            date_history_list.visibility = View.GONE
                            text_date_no_data.visibility = View.VISIBLE
                            loading_date_history.visibility = View.GONE
                            text_date_no_data.text = "기록이 없습니다"

                            TransitionManager.beginDelayedTransition(layout_list, AutoTransition())
                        } else {  // 기록이 있을 경우 진입
                            date_history_list.visibility = View.VISIBLE
                            responseBody.addAll(response.body()!!)
                            // History 객체 각각의 place 속성에 Naver Place ID가 담겨있기 때문에 장소명으로 변환해줘야함
                            for (history in responseBody) {
                                // place 가 null 이면 임의로 생성한 History 이므로 custom_place 속성에 정보가 이미 있음
                                if(history.place != null) {
                                    // 장소 이름이 Realm 에 저장되어 있으면 (방문한 적 있으면 캐싱됨) 사용하고, 없으면 GetPlaceTitleOnly() 호출
                                    realm.executeTransaction {
                                        val visitedPlace: VisitedPlace? =
                                            it.where(VisitedPlace::class.java).equalTo("naverPlaceID", history.place).findFirst()
                                        history.place = visitedPlace?.placeTitle ?: GetPlaceTitleOnly(history.place!!).execute().get()
                                    }
                                }
                            }

                            viewAdapter.notifyDataSetChanged()
                            historyList.addAll(responseBody)
                            loading_date_history.visibility = View.GONE

                            // 매끄러운 UX 를 위해 CalendarView 를 없앰
                            TransitionManager.beginDelayedTransition(card_calendar, AutoTransition())
                            TransitionManager.beginDelayedTransition(layout_list, AutoTransition())
                            image_card_view_status.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                            layout_calendar.visibility = View.GONE
                        }
                    }
                })
        }

        // 누를 때마다 CalendarView 를 접었다 폈다 함 (Animation 포함)
        button_show_calendar.setOnClickListener {
            if(layout_calendar.visibility == View.GONE){
                TransitionManager.beginDelayedTransition(card_calendar, AutoTransition())
                image_card_view_status.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                layout_calendar.visibility = View.VISIBLE
            }else{
                TransitionManager.beginDelayedTransition(card_calendar, AutoTransition())
                TransitionManager.beginDelayedTransition(layout_list, AutoTransition())
                image_card_view_status.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                layout_calendar.visibility = View.GONE
            }
        }

    }
}
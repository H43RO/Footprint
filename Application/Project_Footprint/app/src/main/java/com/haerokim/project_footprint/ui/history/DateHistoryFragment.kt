package com.haerokim.project_footprint.ui.history

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.Transition
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
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.GetPlaceTitleOnly
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_date_history.*
import kotlinx.android.synthetic.main.fragment_today_history.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DateHistoryFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
    var historyList: ArrayList<History> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_date_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text_date_no_data.visibility = View.GONE

        val user: User = Paper.book().read("user_profile")

        var selectDate: String
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        var getDateHistory: RetrofitService = retrofit.create(RetrofitService::class.java)

        calendar.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
            selectDate = "" + year + "-" + (month + 1) + "-" + dayOfMonth
            Log.d("Test", selectDate)

            getDateHistory.requestDateHistoryList(user.id, selectDate, selectDate)
                .enqueue(object : Callback<ArrayList<History>> {
                    override fun onFailure(call: Call<ArrayList<History>>, t: Throwable) {
                        Log.e("Error", t.message)
                        date_history_list.visibility = View.GONE
                        text_date_no_data.visibility = View.VISIBLE
                        text_date_no_data.text = "정보를 가져오지 못했습니다"
                    }

                    override fun onResponse(
                        call: Call<ArrayList<History>>,
                        response: Response<ArrayList<History>>
                    ) {
                        text_date_no_data.visibility = View.GONE

                        if (response.body()?.size == 0 || response.body() == null) {
                            date_history_list.visibility = View.GONE
                            text_date_no_data.visibility = View.VISIBLE
                            text_date_no_data.text = "기록이 없습니다"
                        } else {
                            historyList = response.body()!!
                            for (history in historyList) {
                                history.place = GetPlaceTitleOnly(history.place).execute().get()
                                Log.d("Date History 등록 완료", history.place)
                            }
                            viewManager = LinearLayoutManager(context)
                            viewAdapter = HistoryListAdapter(
                                historyList,
                                requireContext()
                            )

                            recyclerView =
                                view.findViewById<RecyclerView>(R.id.date_history_list).apply {
                                    setHasFixedSize(true)
                                    layoutManager = viewManager
                                    adapter = viewAdapter
                                }

                            TransitionManager.beginDelayedTransition(card_calendar, AutoTransition())
                            TransitionManager.beginDelayedTransition(layout_list, AutoTransition())
                            image_card_view_status.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                            layout_calendar.visibility = View.GONE
                        }
                    }
                })
        }

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
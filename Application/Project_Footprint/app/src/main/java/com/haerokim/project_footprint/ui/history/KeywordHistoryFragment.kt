package com.haerokim.project_footprint.ui.history

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
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
import com.haerokim.project_footprint.Network.ResponseInterceptor
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.GetPlaceTitleOnly
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_keyword_history.*
import kotlinx.android.synthetic.main.fragment_today_history.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class KeywordHistoryFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
    var historyList: ArrayList<History> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_keyword_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        text_keyword_no_data.visibility = View.GONE

        var userInputKeyword: String
        var user: User = Paper.book().read("user_profile")
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()

        val client = OkHttpClient.Builder()
            .addInterceptor(ResponseInterceptor())
            .build()

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()

        var getKeywordHistory: RetrofitService = retrofit.create(RetrofitService::class.java)

        loading_keyword_history.visibility = View.GONE

        edit_text_keyword.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN && edit_text_keyword.text.toString() != "") {
                // Enter Action
                userInputKeyword = edit_text_keyword.text.toString() + " "

                loading_keyword_history.visibility = View.VISIBLE

                getKeywordHistory.requestKeywordHistoryList(user.id, userInputKeyword)
                    .enqueue(object : Callback<ArrayList<History>> {
                        override fun onFailure(call: Call<ArrayList<History>>, t: Throwable) {
                            Log.e("Error", t.message)

                            keyword_history_list.visibility = View.GONE
                            text_keyword_no_data.visibility = View.VISIBLE
                            loading_keyword_history.visibility = View.GONE
                            text_keyword_no_data.text = "정보를 가져오지 못했습니다"
                        }

                        override fun onResponse(
                            call: Call<ArrayList<History>>,
                            response: Response<ArrayList<History>>
                        ) {
                            if (response.body()?.size == 0) {
                                keyword_history_list.visibility = View.GONE
                                text_keyword_no_data.visibility = View.VISIBLE
                                text_keyword_no_data.text = "기록이 없습니다"
                            } else {
                                text_keyword_no_data.visibility = View.GONE

                                historyList = response.body()!!
                                for (history in historyList) {
                                    history.place = GetPlaceTitleOnly(history.place).execute().get()
                                    Log.d(
                                        "정보 획득",
                                        "장소명 : " + history.place + ", 타이틀 : " + history.title
                                    )
                                }
                                loading_keyword_history.visibility = View.GONE

                                viewManager = LinearLayoutManager(context)
                                viewAdapter = HistoryListAdapter(
                                    historyList,
                                    requireContext()
                                )

                                recyclerView =
                                    view.findViewById<RecyclerView>(R.id.keyword_history_list)
                                        .apply {
                                            setHasFixedSize(true)
                                            layoutManager = viewManager
                                            adapter = viewAdapter
                                        }
                            }
                        }
                    })
                true
            } else if (edit_text_keyword.text.toString() == "") {
                edit_text_keyword.error = "검색할 키워드를 입력해주세요"
                true
            } else {
                false
            }

        }

    }
}


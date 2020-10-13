package com.haerokim.project_footprint.Fragment.History

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
import com.haerokim.project_footprint.DataClass.VisitedPlace
import com.haerokim.project_footprint.Network.ResponseInterceptor
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.GetPlaceTitleOnly
import io.paperdb.Paper
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.fragment_keyword_history.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 *  키워드별 History 조회 기능 제공
 *  - 사용자가 검색 키워드를 입력하고 엔터를 누르면 API 호출
 *  - HistoryListAdapter 를 통해 RecyclerView 구현
 **/

class KeywordHistoryFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_keyword_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()

        val client = OkHttpClient.Builder()
            .addInterceptor(ResponseInterceptor())
            .build()

        // API 호출을 위한 Retrofit 객체 생성
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()

        var getKeywordHistory: RetrofitService = retrofit.create(RetrofitService::class.java)

        // API 구조 상 [키워드, 사용자 ID] 를 파라미터로 받기 때문에
        // User 정보를 얻어야 함 (Paper DB에 저장되어 있음)
        var user: User = Paper.book().read("user_profile")

        viewManager = LinearLayoutManager(context)
        viewAdapter = HistoryListAdapter(
            historyList,
            requireContext()
        )

        // 조회한 History 리스트를 보여주는 RecyclerView 설정
        recyclerView =
            view.findViewById<RecyclerView>(R.id.keyword_history_list)
                .apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }

        // 저장된 장소 정보를 활용하기 위해 Realm 객체 생성 및 초기화
        val config: RealmConfiguration = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(config)
        var realm = Realm.getDefaultInstance()

        text_keyword_no_data.visibility = View.GONE
        loading_keyword_history.visibility = View.GONE

        var userInputKeyword: String

        // 사용자가 Enter 키를 입력할 때마다 진입
        edit_text_keyword.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN && edit_text_keyword.text.toString() != "") {
                // Enter Action
                userInputKeyword = edit_text_keyword.text.toString() + " "

                loading_keyword_history.visibility = View.VISIBLE

                // 사용자 식별 ID, 입력한 키워드와 함께 API 호출
                getKeywordHistory.requestKeywordHistoryList(user.id, userInputKeyword)
                    .enqueue(object : Callback<ArrayList<History>> {
                        override fun onFailure(call: Call<ArrayList<History>>, t: Throwable) {
                            Log.e("Error", t.message)

                            keyword_history_list.visibility = View.GONE
                            text_keyword_no_data.visibility = View.VISIBLE
                            loading_keyword_history.visibility = View.GONE
                            text_keyword_no_data.text = "정보를 가져오지 못했습니다"
                        }

                        override fun onResponse(call: Call<ArrayList<History>>, response: Response<ArrayList<History>>) {
                            historyList.clear()

                            // 해당 키워드와 관련된 기록이 없을 경우 진입
                            if (response.body()?.size == 0) {
                                keyword_history_list.visibility = View.GONE
                                text_keyword_no_data.visibility = View.VISIBLE
                                loading_keyword_history.visibility = View.GONE
                                text_keyword_no_data.text = "기록이 없습니다"
                            } else {  // 기록이 있을 경우 진입
                                text_keyword_no_data.visibility = View.GONE
                                responseBody = response.body()!!
                                // History 객체 각각의 place 속성에 Naver Place ID가 담겨있기 때문에 장소명으로 변환해줘야함
                                for (history in responseBody) {
                                    // place 가 null 이면 임의로 생성한 History 이므로 custom_place 속성에 정보가 이미 있음
                                    if (history.place != null) {
                                        // 장소 이름이 Realm 에 저장되어 있으면 (방문한 적 있으면 캐싱됨) 사용하고, 없으면 GetPlaceTitleOnly() 호출
                                        realm.executeTransaction {
                                            val visitedPlace: VisitedPlace? =
                                                it.where(VisitedPlace::class.java)
                                                    .equalTo("naverPlaceID", history.place)
                                                    .findFirst()
                                            history.place = visitedPlace?.placeTitle ?: GetPlaceTitleOnly(history.place!!).execute().get()
                                        }
                                    }
                                }
                                historyList.addAll(responseBody)
                                viewAdapter.notifyDataSetChanged()
                                loading_keyword_history.visibility = View.GONE
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


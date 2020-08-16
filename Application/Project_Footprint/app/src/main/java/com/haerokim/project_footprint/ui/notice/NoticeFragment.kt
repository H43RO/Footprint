package com.haerokim.project_footprint.ui.notice

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.ActivityNavigator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.Adapter.NoticeListAdapter
import com.haerokim.project_footprint.DataClass.Notice
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.fragment_notice.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NoticeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lateinit var recyclerView: RecyclerView
        lateinit var viewAdapter: RecyclerView.Adapter<*>
        lateinit var viewManager: RecyclerView.LayoutManager

        var noticeList: ArrayList<Notice> = ArrayList()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        var getNoticeList: RetrofitService = retrofit.create(RetrofitService::class.java)

        text_notice_no_data.visibility = View.GONE
        loading_notice.visibility = View.VISIBLE

        getNoticeList.requestNoticeList().enqueue(object : Callback<ArrayList<Notice>> {
            override fun onFailure(call: Call<ArrayList<Notice>>, t: Throwable) {
                Log.e("Notice Loading Error", t.message)
            }

            override fun onResponse(
                call: Call<ArrayList<Notice>>,
                response: Response<ArrayList<Notice>>
            ) {
                if (response.body()?.size == 0) {
                    notice_list.visibility = View.GONE
                    text_notice_no_data.visibility = View.VISIBLE
                    loading_notice.visibility = View.GONE
                    text_notice_no_data.text = "기록이 없습니다"
                } else {
                    text_notice_no_data.visibility = View.GONE
                    noticeList = response.body()!!

                    loading_notice.visibility = View.GONE

                    viewManager = LinearLayoutManager(context)
                    viewAdapter = NoticeListAdapter(
                        noticeList,
                        requireContext()
                    )

                    recyclerView =
                        view.findViewById<RecyclerView>(R.id.notice_list).apply {
                            setHasFixedSize(true)
                            layoutManager = viewManager
                            adapter = viewAdapter
                        }
                }
            }
        })
    }
}
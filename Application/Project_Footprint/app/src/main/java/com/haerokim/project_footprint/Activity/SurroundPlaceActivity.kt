package com.haerokim.project_footprint.Activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.Data.NaverPlaceID
import com.haerokim.project_footprint.Data.Place
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.place_item.view.*
import org.altbeacon.beacon.Beacon
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class SurroundPlaceActivity : AppCompatActivity() {
    private var surroundBeaconList: ArrayList<Beacon> = ArrayList()
    private var surroundPlaceList: ArrayList<Place> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    class PlaceListAdapter(
        private val surroundPlaceList: ArrayList<Place>, private val context: Context
    ) :
        RecyclerView.Adapter<PlaceListAdapter.ViewHolder>() {

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.place_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = surroundPlaceList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.view.text_place_title.text = surroundPlaceList[position].title
            holder.view.text_place_category.text = surroundPlaceList[position].category
            Glide.with(holder.view) // 확인 필요
                .load(surroundPlaceList[position].imageSrc)
                .centerCrop()
                .into(holder.view.place_image)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surround_place)

        val timer = Timer()
        val refreshBeaconList: TimerTask = object : TimerTask() {
            override fun run() {
                // 현재 Scanning 중인 서비스를 통해 주위 비콘 리스트를 얻음
                //Retrofit Service를 통해 네이버 Place ID를 받아올 수 있도록 구현할 예정
                //네이버 Place ID를 받아오면, GetPlaceInfo 클래스를 통해 정보 얻을 수 있음

                var retrofit = Retrofit.Builder()
                    .baseUrl(" http://0c0c648cfd92.ngrok.io") //사이트 Base URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                var getPlaceInfoService: RetrofitService =
                    retrofit.create(RetrofitService::class.java)

                // 현재 Beacon 객체 각각은 UUID, 거리 등을 갖고 있는 상태
                // 갖고있는 UUID 값을 기반으로 Place 객체를 채우는 동작을 함
                for (beacon in surroundBeaconList) {
                    getPlaceInfoService.requestNaverPlaceID(beacon.id1.toString())
                        .enqueue(object : Callback<List<NaverPlaceID>> {
                            override fun onFailure(call: Call<List<NaverPlaceID>>, t: Throwable) {
                                Log.d("GetPlaceInfo", "정보 얻기 실패")
                            }
                            // 네이버 플레이스 ID를 받아와서 GetPlaceInfo에 정보 요청함
                            override fun onResponse(
                                call: Call<List<NaverPlaceID>>,
                                response: Response<List<NaverPlaceID>>
                            ) {
                                Log.d("GetPlaceInfo", "정보 얻기 성공")
//                                response.body()
//                                    ?.let {
//                                        surroundPlaceList.add(
//                                            GetPlaceInfo(it).execute().get()
//                                        )
//                                    }
                            }
                        })
                }
                viewAdapter.notifyDataSetChanged()
            }
        }

        timer.schedule(refreshBeaconList, 0, 1000) //1초 간격으로 리프레쉬
        timer.cancel() //타이머 종료

        viewManager = LinearLayoutManager(this)
        viewAdapter = PlaceListAdapter(surroundPlaceList, this)

        recyclerView = findViewById<RecyclerView>(R.id.surround_place_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}
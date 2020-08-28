package com.haerokim.project_footprint.ui.surround

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.DataClass.NaverPlaceID
import com.haerokim.project_footprint.DataClass.Place
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.Utility.GetPlaceInfo
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.ShowPlaceInfo
import com.haerokim.project_footprint.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.fragment_surround.*
import kotlinx.android.synthetic.main.place_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 *  주변 장소 보여주기 기능 제공
 *  - 발자취 따라가기 모드 ON 일때만 동작하도록 구현
 *  - ForegroundService 에서 주기적으로 Broadcasting 하는 주변 장소 리스트를 수신하여 동작
 **/

class SurroundFragment : Fragment() {
    var surroundBeaconList: ArrayList<String> = ArrayList()
    var tempBeaconList: ArrayList<String> = ArrayList()
    var surroundPlaceList: ArrayList<Place> = ArrayList()
    var tempNaverPlacIDList: ArrayList<NaverPlaceID> = ArrayList()
    var tempPlaceList: ArrayList<Place> = ArrayList()
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
    val surroundBeaconReceiver = SurroundBeaconReceiver()

    val viewModel: HomeViewModel by activityViewModels()

    inner class SurroundBeaconReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent != null) {
                surroundBeaconList.clear()
                surroundBeaconList.addAll(intent.getStringArrayListExtra("surround_beacon_list") ?: arrayListOf())
                // 기존 리스트와 다른 점이 없으면 새로고침하지 않음
                // 원소 순서와 상관 없이 원소가 같아야함 (Set 특성 이용)
                if (tempBeaconList.toSet() != surroundBeaconList.toSet()) {

                    // Test 용
                    Log.d("Surround Before", tempBeaconList.toString())
                    Log.d("Surround Current", surroundBeaconList.toString())

                    Log.d("Surround", "변화 감지 : AsyncTask 진입")
                    tempBeaconList.clear()
                    tempBeaconList.addAll(surroundBeaconList)

                    // surroundBeaconList에 대해 바인딩 시작
                    PlaceListBinder().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                } else {
                    Log.d("Surround", "변함 없음!")
                }
            }
        }
    }

    /**
     *  주변 장소 리스트 아이템 Binding 동작
     *  1. Broadcast 를 통해 수신한 주변 장소들의 UUID 를 통해 API 를 호출하여 각 장소의 NaverPlaceID 를 얻음
     *  2. 각각에 대하여 NaverPlaceID 를 기반으로 네이버 플레이스 정보를 크롤링하여 Place 객체 생성 및 추가
     *  3. 주변 장소 리스트 아이템 Binding 완료
     **/

    inner class PlaceListBinder : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            loading_spinner.visibility = View.VISIBLE
            tempPlaceList.clear()
        }

        override fun doInBackground(vararg params: Void?): Void? {

            // API 호출을 위한 Retrofit 객체 생성
            var retrofit = Retrofit.Builder()
                .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            var getPlaceInfoService: RetrofitService = retrofit.create(RetrofitService::class.java)
            surroundPlaceList.clear()

            // 현재 가지고 있는 UUID 값 각각을 기반으로 Place 객체를 찍어내는 동작을 함 (리스트 아이템으로 사용될 예정)
            for (beacon in surroundBeaconList) {
                getPlaceInfoService.requestNaverPlaceID(beacon)
                    .enqueue(object : Callback<ArrayList<NaverPlaceID>> {
                        override fun onFailure(call: Call<ArrayList<NaverPlaceID>>, t: Throwable) {
                            Log.d("GetPlaceInfo", "정보 얻기 실패")
                        }

                        // 네이버 플레이스 ID를 받아와서 GetPlaceInfo() 로 장소 정보 요청함
                        override fun onResponse(
                            call: Call<ArrayList<NaverPlaceID>>,
                            response: Response<ArrayList<NaverPlaceID>>
                        ) {
                            Log.d("GetPlaceInfo", "정보 얻기 성공!")

                            tempNaverPlacIDList.clear()
                            tempNaverPlacIDList.addAll(response.body()!!)

                            // Naver Place ID 각각에 대한 장소 정보를 얻어서 RecyclerView 에 보여줄 리스트에 추가
                            for (place in tempNaverPlacIDList) {
                                surroundPlaceList.add(
                                    GetPlaceInfo(place.naver_place_id).execute().get()
                                )
                                if (place == tempNaverPlacIDList[tempNaverPlacIDList.size - 1]) {
                                    viewAdapter.notifyDataSetChanged()
                                }
                            }
                            loading_spinner.visibility = View.GONE
                        }
                    })
            }
            return null
        }
    }

    // 발자취 따라가기 모드가 활성화 되어있을 때 && 화면 재구성 시 Broadcast Receiver 등록
    override fun onResume() {
        super.onResume()
        viewModel.scanMode.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                activity?.registerReceiver(
                    surroundBeaconReceiver,
                    IntentFilter("surround_beacon_list")
                )
            }
        })
    }

    /**
     *  주변 장소 리스트 RecyclerView Adapter
     *  - PlaceListBinder 에서 Binding 완료된 리스트를 데이터로 가짐
     **/

    class PlaceListAdapter(
        private val surroundPlaceList: ArrayList<Place>, private val context: Context
    ) :
        RecyclerView.Adapter<PlaceListAdapter.ViewHolder>() {

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.place_item, parent, false)

            return ViewHolder(
                view
            )
        }

        override fun getItemCount() = surroundPlaceList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.view.setOnClickListener {
                // 아이템을 터치했을 때 상세 정보 페이지로 이동 : ShowPlaceInfo().showInfo() 사용
                ShowPlaceInfo(context,
                    surroundPlaceList[position].naverPlaceID
                ).showInfo(surroundPlaceList[position])
            }
            holder.view.text_place_title.text = surroundPlaceList[position].title
            holder.view.text_place_category.text = surroundPlaceList[position].category
            Glide.with(holder.view) // 확인 필요
                .load(surroundPlaceList[position].imageSrc)
                .centerCrop()
                .override(600, 400)
                .into(holder.view.place_image)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_surround, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewManager = LinearLayoutManager(context)
        viewAdapter =
            PlaceListAdapter(
                surroundPlaceList,
                requireContext()
            )

        // 주변 장소 리스트를 보여주는 RecyclerView 설정
        recyclerView = view.findViewById<RecyclerView>(R.id.surround_place_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        viewModel.scanMode.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                loading_spinner.visibility = View.GONE
                Toast.makeText(context, "발자취 따라가기를 활성화 해주세요", Toast.LENGTH_LONG).show()
                text_state.text = "발자취 따라가기를 활성화 해주세요"
            } else {
                text_state.text = "가까운 주변 장소를 탐색합니다"
            }
        })
    }

    // 화면 이탈 시 Broadcast Receiver 해지
    override fun onPause() {
        super.onPause()
        viewModel.scanMode.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                activity?.unregisterReceiver(surroundBeaconReceiver)
            }
        })
    }
}

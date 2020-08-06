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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haerokim.project_footprint.Data.NaverPlaceID
import com.haerokim.project_footprint.Data.Place
import com.haerokim.project_footprint.Data.Website
import com.haerokim.project_footprint.GetPlaceInfo
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.ShowPlaceInfo
import kotlinx.android.synthetic.main.fragment_surround.*
import kotlinx.android.synthetic.main.place_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SurroundFragment : Fragment() {
    var surroundBeaconList: ArrayList<String> = ArrayList()
    var tempBeaconList:ArrayList<String> = ArrayList()
    var surroundPlaceList: ArrayList<Place> = ArrayList()
    var tempPlaceList: ArrayList<Place> = ArrayList()
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
    val surroundBeaconReceiver = SurroundBeaconReceiver()

    inner class SurroundBeaconReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                surroundBeaconList= intent.getStringArrayListExtra("surround_beacon_list") ?: arrayListOf()
                //기존 리스트와 다른 점이 없으면 새로고침하지 않음
                //원소 순서와 상관 없이 원소가 같아야함 (Set 의 특성 이용)
                if (tempBeaconList.toSet() != surroundBeaconList.toSet()) {
                    Log.d("Surround", "AsyncTask 진입!")
                    PlaceListBinder().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    tempBeaconList = surroundBeaconList
                }else{
                    Log.d("Surround", "변함 없음!")
                }
            }
        }
    }

    inner class PlaceListBinder : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()

            //Loading Splash 시작
            loading_spinner.visibility = View.VISIBLE

            tempPlaceList.clear()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            //Retrofit Service를 통해 네이버 Place ID를 받아올 수 있도록 구현할 예정
            //네이버 Place ID를 받아오면, GetPlaceInfo 클래스를 통해 정보 얻을 수 있음

            var retrofit = Retrofit.Builder()
                .baseUrl("http://5e637d81aee0.ngrok.io/") //사이트 Base URL
                .baseUrl(Website.baseUrl) //사이트 Base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            var getPlaceInfoService: RetrofitService = retrofit.create(RetrofitService::class.java)

            // 현재 Beacon 객체 각각은 UUID, 거리 등을 갖고 있는 상태
            // 갖고있는 UUID 값을 기반으로 Place 객체를 채우는 동작을 함
            for (beacon in surroundBeaconList) {
                getPlaceInfoService.requestPlaceInfo(beacon)
                    .enqueue(object : Callback<List<NaverPlaceID>> {
                        override fun onFailure(call: Call<List<NaverPlaceID>>, t: Throwable) {
                            Log.d("GetPlaceInfo", "정보 얻기 실패")
                        }

                        // 네이버 플레이스 ID를 받아와서 GetPlaceInfo에 정보 요청함
                        override fun onResponse(
                            call: Call<List<NaverPlaceID>>,
                            response: Response<List<NaverPlaceID>>
                        ) {
                            Log.d("GetPlaceInfo", "정보 얻기 성공!")
                            response.body()
                                ?.let {
                                    tempPlaceList.add(
                                        GetPlaceInfo(it[0].naver_place_id).executeOnExecutor(
                                            AsyncTask.THREAD_POOL_EXECUTOR
                                        ).get()
                                    )
                                }
                        }
                    })
            }

            Thread.sleep(5000)

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            //안정적인 DataSetChange를 위해 tempPlaceList 사용함
            surroundPlaceList.clear()
            surroundPlaceList.addAll(tempPlaceList)
            Log.d("Surround!", "바인딩 완료!")
            viewAdapter.notifyDataSetChanged()

            //Loading Splash 종료
            loading_spinner.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(surroundBeaconReceiver, IntentFilter("surround_beacon_list"))
    }
    //객체 배열이 완성되고 비동기적으로 ListAdapter가 완성될 수 있도록 구현할 예정
    //그렇지 않으면 Adapting 과정에서 충돌 발생할 것

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
            // 검증 필요
            holder.view.setOnClickListener {
                ShowPlaceInfo(context, surroundPlaceList[position].naverPlaceID).showInfo(surroundPlaceList[position])
            }
            holder.view.text_place_title.text = surroundPlaceList[position].title
            holder.view.text_place_category.text = surroundPlaceList[position].category
            Glide.with(holder.view) // 확인 필요
                .load(surroundPlaceList[position].imageSrc)
                .centerCrop()
                .into(holder.view.place_image)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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

        recyclerView = view.findViewById<RecyclerView>(R.id.surround_place_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

    }

    override fun onPause() {
        super.onPause()

        activity?.unregisterReceiver(surroundBeaconReceiver)
    }
}

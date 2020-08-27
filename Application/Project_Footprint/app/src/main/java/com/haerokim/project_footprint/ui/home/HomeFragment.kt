package com.haerokim.project_footprint.ui.home

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.haerokim.project_footprint.Activity.HistoryWriteActivity
import com.haerokim.project_footprint.Adapter.EditorPickViewPagerAdapter
import com.haerokim.project_footprint.Adapter.HotPlaceListAdapter
import com.haerokim.project_footprint.DataClass.EditorPick
import com.haerokim.project_footprint.DataClass.NaverPlaceID
import com.haerokim.project_footprint.DataClass.Place
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.Utility.ForegroundService
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.Utility.GetPlaceInfo
import kotlin.concurrent.timer
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Time
import java.util.*
import kotlin.collections.ArrayList

/**
 *  메인 화면 (홈 화면)
 *  - 발자취 따라가기 모드 ON/OFF, 핫플레이스 리스트, 에디터 픽 플레이스 리스트 등 포함
 *  - 앱 최초 실행 시 필요 권한 허용을 위한 Dialog 동작함 (내부 저장소 R/W, 위치 권한)
 **/

class HomeFragment : Fragment(), PermissionListener {
    // 발자취 따라가가기 모드의 ON/OFF 상태에 따라 SurroundFragment 가 다르게 동작하기 때문에 ViewModel 사용
    val viewModel: HomeViewModel by activityViewModels()

    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
    var hotPlaceList: ArrayList<Place> = ArrayList()
    var editorPickList: ArrayList<EditorPick> = ArrayList()

    // ViewPager 자동 전환 기능을 위한 Timer 선언, Interval 선언
    var timer = Timer()
    private val DELAY_MS: Long = 500
    private val PERIOD_MS: Long = 3000
    var currentPage: Int = 0

    // Bluetooth 활성화 동작의 Request Code
    private val REQUEST_ENABLE_BT = 5603
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onPermissionGranted() {
    }

    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Bluetooth 활성화 Intent 이후 진입
        when (requestCode) {
            REQUEST_ENABLE_BT -> Log.d("Bluetooth", "활성화 완료")
            else -> Log.d("Bluetooth", "활성화 실패")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        // ViewModel 에 저장된 상태에 따라 Switch 상태를 변경
        homeViewModel.scanMode.observe(viewLifecycleOwner, Observer<Boolean> {
            scanning_mode_switch.isChecked = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val switchStateSave = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val foregroundIntent = Intent(context, ForegroundService::class.java)
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        Paper.init(context)

        // User 정보 로드 필요 (닉네임 등)
        val user: User = Paper.book().read("user_profile")
        text_home_user_nickname.setText(user.nickname + "님")

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()

        // API 호출을 위한 Retrofit 객체 생성
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        var getPlaceList: RetrofitService = retrofit.create(RetrofitService::class.java)

        viewManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        viewAdapter = HotPlaceListAdapter(hotPlaceList, requireContext())

        // 핫플레이스 리스트를 보여주는 RecyclerView 설정
        recyclerView =
            view.findViewById<RecyclerView>(R.id.home_hot_place_list).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }

        // 서버와의 통신 Delay 가 있을 시 스켈레톤 UI가 먼저 보이도록 함
        val skeletonRecyclerView: SkeletonScreen =
            Skeleton.bind(recyclerView).adapter(viewAdapter)
                .color(R.color.shimmerColor)
                .duration(1200)
                .frozen(false)
                .count(5)
                .load(R.layout.skeleton_hot_place_item)
                .show()

        // 핫플레이스 리스트를 얻기 위해 API 호출
        getPlaceList.requestHotPlaceList().enqueue(object : Callback<ArrayList<Place>> {
            override fun onFailure(call: Call<ArrayList<Place>>, t: Throwable) {
                Log.e("Error Hot Place", t.message)
            }

            override fun onResponse(
                call: Call<ArrayList<Place>>,
                response: Response<ArrayList<Place>>
            ) {
                skeletonRecyclerView.hide()

                if (response.body() != null && response.code() == 200) {
                    hotPlaceList.clear()
                    skeletonRecyclerView.hide()
                    hotPlaceList.addAll(response.body()!!)
                    viewAdapter.notifyDataSetChanged()
                }
            }
        })

        // 에디터 픽 플레이스 게시물 리스트를 얻기 위해 API 호출
        getPlaceList.requestEditorPickList().enqueue(object : Callback<ArrayList<EditorPick>> {
            override fun onFailure(call: Call<ArrayList<EditorPick>>, t: Throwable) {
                Log.e("EditorPickList Error", t.message)
            }

            override fun onResponse(
                call: Call<ArrayList<EditorPick>>,
                response: Response<ArrayList<EditorPick>>
            ) {
                editorPickList.clear()
                if (response.body() != null && response.code() == 200 && home_editor_place_pager != null) {
                    editorPickList.addAll(response.body()!!)

                    home_editor_place_pager.adapter =
                        EditorPickViewPagerAdapter(context!!, editorPickList)
                    home_editor_place_pager.currentItem = 0

                    // 자동 전환 View Pager 동작을 위한 Handler 객체 + 동작부
                    val handler = Handler()
                    val updateTask: Runnable = object : Runnable {
                        override fun run() {
                            // 호출될 때 마다 페이지를 한 칸 이동하고, 마지막 페이지면 처음으로 이동
                            if (currentPage == editorPickList.size) {
                                currentPage = 0
                            }
                            home_editor_place_pager.setCurrentItem(currentPage++, true)
                        }
                    }
                    // 한 번 cancle()한 Timer는 재사용할 수 없어서 재정의해야함
                    // - Fragment 가 화면에서 사라질 때 cancle() 하게 됨  ex) onDestroy()
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            handler.post(updateTask)
                        }
                    }, DELAY_MS, PERIOD_MS)

                }
            }
        })

        // 앱 재시작 후 UI 복원 시 마지막 Switch 상태로 복구 (SharedPreference)
        scanning_mode_switch.isChecked = switchStateSave.getBoolean("state", false)

        // 사용자 프로필 이미지 로드를 위한 SharedPreferences 객체
        val pref: SharedPreferences? =
            context?.getSharedPreferences("profile_image", Activity.MODE_PRIVATE)
        val profileImageUri = Uri.parse(pref?.getString("profile_image", ""))

        if (profileImageUri.toString() != "") {
            image_home_user_profile.setImageURI(profileImageUri)
        } else {
            image_home_user_profile.setImageResource(R.drawable.basic_profile)
        }

        // 앱 최초 실행 시 위치 권한, 저장소 접근 권한 요구
        TedPermission.with(context)
            .setPermissionListener(this)
            .setDeniedMessage("위치 기반 서비스이므로 위치 정보 권한이 필요합니다.\n\n[설정] > [앱]을 통해 권한 허가를 해주세요.")
            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .check()

        // 발자취 따라가기 ON/OFF 상태에 따른 동작
        scanning_mode_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {  // 발자취 따라가기 모드를 켰을 때 진입
                //Switch 및 ViewModel 상태 저장
                with(switchStateSave.edit()) {
                    putBoolean("state", true)
                    commit()
                }
                viewModel.changeMode("on")

                card_switch_state.setCardBackgroundColor(Color.parseColor("#CC59628F"))
                text_switch_state.text = "발자취를 따라갑니다"

                //위치 권한 허용 되어있으면 비콘 스캔 시작
                if (TedPermission.isGranted(context)) {
                    if (mBluetoothAdapter == null) {
                        // 기종 자체가 블루투스 지원 안하는 경우
                    } else if (!mBluetoothAdapter.isEnabled) {
                        // 블루투스 안 켜져있는 경우 활성화 시킴
                        val bluetoothOnIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(bluetoothOnIntent, REQUEST_ENABLE_BT)
                    } else {
                        // 블루투스 켜져있는 경우 (정상 진입)
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "당신의 발자취를 따라가기 시작합니다!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else { //워치 권한 허용 안됨
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "앱 사용을 위한 위치 권한이 필요합니다",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                // Foreground Service 시작 (비콘 스캔 서비스 호출)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context?.startForegroundService(foregroundIntent)
                } else {
                    context?.startService(foregroundIntent)
                }

            } else {  // 발자취 따라가기 모드를 껐을 때 진입
                // Switch 및 ViewModel 상태 저장
                with(switchStateSave.edit()) {
                    putBoolean("state", false)
                    commit()
                }
                viewModel.changeMode("off")

                card_switch_state.setCardBackgroundColor(Color.parseColor("#6659628F"))
                text_switch_state.text = "발자취를 따라가지 않습니다"

                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "더 이상 발자취를 따라가지 않습니다.",
                    Snackbar.LENGTH_LONG
                ).show()

                // Foreground Service 중단
                // TODO("Android 8.0 이후 버그 대응 필요")
                context?.stopService(foregroundIntent)
            }
        }

        image_home_user_profile.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_home_to_navigation_menu)
        }

        card_today_history_list.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_home_to_navigation_today_history)
        }

        button_go_editor_detail.setOnClickListener {
            val bundle = bundleOf("editorPickList" to editorPickList)
            it.findNavController()
                .navigate(R.id.action_navigation_home_to_navigation_editor_pick, bundle)
        }
    }

    override fun onResume() {
        super.onResume()

        timer = Timer()
    }

    override fun onStop() {
        super.onStop()

        timer.cancel()
    }

}



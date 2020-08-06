package com.haerokim.project_footprint.ui.home

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.*
import android.text.TextUtils.replace
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.haerokim.project_footprint.*
import com.haerokim.project_footprint.Activity.HomeActivity
import com.haerokim.project_footprint.Activity.SurroundPlaceActivity
import com.haerokim.project_footprint.Data.User
import com.haerokim.project_footprint.R
import com.haerokim.project_footprint.ui.surround.SurroundFragment
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment(),  PermissionListener {

    private val REQUEST_ENABLE_BT = 5603
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onPermissionGranted() {
    }
    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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

        val user: User =  Paper.book().read("user_profile")
        text_home_user_nickname.text = user.nickname + " 님"

        //UI 복원 시 switch 모드 정상화 (SharedPreference)
        scanning_mode_switch.isChecked = switchStateSave.getBoolean("state", false)

        TedPermission.with(context)
            .setPermissionListener(this)
            .setDeniedMessage("위치 기반 서비스이므로 위치 정보 권한이 필요합니다.\n\n[설정] > [앱]을 통해 권한 허가를 해주세요.")
            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .check()

        scanning_mode_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                with(switchStateSave.edit()) {
                    putBoolean("state", true)
                    commit()
                }

                homeViewModel.changeMode("on")

                //위치 권한 허용 되어있으면 비콘 스캔 시작
                if (TedPermission.isGranted(context)) {
                    if (mBluetoothAdapter == null) {
                        // 블루투스 지원 안하는 경우
                    } else if (!mBluetoothAdapter.isEnabled) {
                        // 블루수트 안 켜져있는 경우 활성화 시킴
                        val bluetoothOnIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(bluetoothOnIntent, REQUEST_ENABLE_BT)
                    } else {
                        // 블루투스 켜져있는 경우
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), "당신의 발자취를 따라가기 시작합니다!", Snackbar.LENGTH_LONG).show()
                    }
                } else { //워치 권한 허용 안됨
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), "앱 사용을 위해 위치 권한이 필요합니다", Snackbar.LENGTH_LONG).show()
                }

                //Foreground Service 시작 (비콘 스캔 서비스)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context?.startForegroundService(foregroundIntent)
                } else {
                    context?.startService(foregroundIntent)
                }

            } else {
                with(switchStateSave.edit()) {
                    putBoolean("state", false)
                    commit()
                }
                Snackbar.make(requireActivity().findViewById(android.R.id.content), "더 이상 발자취를 따라가지 않습니다.", Snackbar.LENGTH_LONG).show()

                homeViewModel.changeMode("off")
                context?.stopService(foregroundIntent)
            }
        }

        card_surround_place.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_home_to_navigation_surround)
        }

    }

}

package com.haerokim.project_footprint.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.haerokim.project_footprint.ForegroundService
import com.haerokim.project_footprint.GetPlaceInfo
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.altbeacon.beacon.*

class HomeFragment : Fragment(), BeaconConsumer, PermissionListener {
    override fun getApplicationContext(): Context {
        val context = context
        return if (context != null) context else throw NullPointerException("Expression 'context' must not be null")
    }

    override fun unbindService(p0: ServiceConnection?) {

    }

    override fun bindService(p0: Intent?, p1: ServiceConnection?, p2: Int): Boolean {
        return false
    }

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var beaconManager: BeaconManager

    private var beaconList: MutableList<Beacon> = mutableListOf()

    override fun onPermissionGranted() {
        Toast.makeText(context, "권환 획득 완료!", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
        Toast.makeText(context, "권환 획득 실패", Toast.LENGTH_LONG).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)

        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TedPermission.with(context)
            .setPermissionListener(this)
            .setDeniedMessage("위치 기반 서비스이므로 위치 정보 권한이 필요합니다.\n\n[설정] > [앱]을 통해 권한 허가를 해주세요.")
            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .check();

        beaconManager = BeaconManager.getInstanceForApplication(applicationContext)
        beaconManager.getBeaconParsers()
            .add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))
        beaconManager.bind(this)

        var handler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                beacon_list.setText("")

                // 비콘의 아이디와 거리를 측정하여 보여줌
                for (beacon in beaconList) {
                    beacon_list.append(
                        "ID : " + beacon.id1 + " \n " + "Distance : " + String.format(
                            "%.3f",
                            beacon.distance
                        ).toDouble() + "m\n\n"
                    )
                }
                // 자기 자신을 0.5초마다 호출
                this.sendEmptyMessageDelayed(0, 500)
            }
        }

        val foregroundIntent = Intent(context, ForegroundService::class.java)
        toggle_test.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                //위치 권한 허용 되어있으면 비콘 스캔 시작
                if (TedPermission.isGranted(applicationContext)) {
                    val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    if (mBluetoothAdapter == null) {
                        // Device does not support Bluetooth
                    } else if (!mBluetoothAdapter.isEnabled) {
                        // Bluetooth is not enabled :)
                    } else {
                        // Bluetooth is enabled
                        handler.sendEmptyMessage(0)
                        Toast.makeText(context, "비콘 스캔시작", Toast.LENGTH_LONG).show()
                    }
                } else { //워치 권한 X
                    Toast.makeText(context, "앱 사용을 위해 위치 권한이 있어야합니다.", Toast.LENGTH_LONG).show()
                }

                //Foreground Service 시작 (비콘 스캔 서비스)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context?.startForegroundService(foregroundIntent)
                } else {
                    context?.startService(foregroundIntent)
                }

                GetPlaceInfo(applicationContext, "연남동 감칠").execute()

            } else {
                context?.stopService(foregroundIntent)
            }
        }
    }

    override fun onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(RangeNotifier { beacons, region ->
            // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어온다.
            if (beacons.size > 0) {
                beaconList.clear()
                for (beacon in beacons) {
                    beaconList.add(beacon)
                }
            }
        })

        try {
            beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
        } catch (e: RemoteException) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothAdapter.getDefaultAdapter().disable();
        beaconManager.unbind(this)
    }
}
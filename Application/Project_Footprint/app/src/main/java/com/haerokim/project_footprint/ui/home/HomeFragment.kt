package com.haerokim.project_footprint.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.haerokim.project_footprint.ForegroundService
import com.haerokim.project_footprint.GetPlaceInfo
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.altbeacon.beacon.*
import java.lang.Exception

class HomeFragment : Fragment(), BeaconConsumer, PermissionListener {
    private val REQUEST_ENABLE_BT = 5603

    override fun getApplicationContext(): Context {
        val context = context
        return if (context != null) context else throw NullPointerException("Expression 'context' must not be null")
    }

    override fun unbindService(p0: ServiceConnection?) {
    }

    override fun bindService(p0: Intent?, p1: ServiceConnection?, p2: Int): Boolean {
        return false
    }

    override fun onPermissionGranted() {
    }

    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
    }

    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var beaconManager: BeaconManager

    private var beaconList: MutableList<Beacon> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.scanMode.observe(viewLifecycleOwner, Observer<Boolean> {
            toggle_test.isChecked = it
        })

        TedPermission.with(context)
            .setPermissionListener(this)
            .setDeniedMessage("위치 기반 서비스이므로 위치 정보 권한이 필요합니다.\n\n[설정] > [앱]을 통해 권한 허가를 해주세요.")
            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .check()

        beaconManager = BeaconManager.getInstanceForApplication(applicationContext)
        beaconManager.getBeaconParsers()
            .add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))

        var handler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                if (beacon_list == null) {
                    //Place Detail Activity로 이동했을 때 충돌 가능성 대비
                } else if( beacon_list != null && toggle_test.isChecked){
                    beacon_list.setText("")
                    // 비콘의 아이디와 거리를 측정하여 보여줌
                    for (beacon in beaconList) {
                        beacon_list.append(
                            "ID : " + beacon.id1 + " \n " + "Distance : " + String.format(
                                "%.3f",
                                beacon.distance
                            ).toDouble() + "m\n\n"
                        )
                        Log.d("Scan Result", beacon.id1.toString())
                    }
                    // 자기 자신을 0.5초마다 호출
                    this.sendEmptyMessageDelayed(0, 500)
                }else{
                    beacon_list.setText("Scanning Stop")
                }
            }
        }

        val foregroundIntent = Intent(context, ForegroundService::class.java)
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        toggle_test.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                beaconManager.bind(this)
                homeViewModel.changeMode("on")

                //위치 권한 허용 되어있으면 비콘 스캔 시작
                if (TedPermission.isGranted(applicationContext)) {
                    if (mBluetoothAdapter == null) {
                        // Device does not support Bluetooth
                    } else if (!mBluetoothAdapter.isEnabled) {
                        // Bluetooth is not enabled :)
                        val bluetoothOnIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(bluetoothOnIntent, REQUEST_ENABLE_BT)
                        handler.sendEmptyMessage(0)
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
                homeViewModel.changeMode("off")
                beaconManager.unbind(this)
                //Beacon Scanner 멈추는 방법이 뭐가 있을 까여
                Toast.makeText(context, "비콘 스캔중단", Toast.LENGTH_LONG).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ENABLE_BT -> Log.d("Bluetooth", "활성화 완료")
            else -> Log.d("Bluetooth", "활성화 실패")
        }
    }
}

package com.haerokim.project_footprint

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.RemoteException
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.altbeacon.beacon.*

class MainActivity : AppCompatActivity(), BeaconConsumer {
    private lateinit var beaconManager : BeaconManager
    private var beaconList: MutableList<Beacon> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        beaconManager =  BeaconManager.getInstanceForApplication(this)

        beaconManager.getBeaconParsers()
            .add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))

        beaconManager.bind(this)

        var  handler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                Textview.setText("")

                // 비콘의 아이디와 거리를 측정하여 textView에 넣는다.
                for (beacon in beaconList) {
                    Textview.append(
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

        button.setOnClickListener {
            handler.sendEmptyMessage(0)
            Toast.makeText(this,"시작",Toast.LENGTH_LONG).show()
        }

        GetPlaceInfo().execute()
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothAdapter.getDefaultAdapter().disable();
        beaconManager.unbind(this)
    }

    override fun onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(RangeNotifier { beacons, region ->
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

}
package com.haerokim.project_footprint

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.util.rangeTo
import androidx.fragment.app.activityViewModels
import com.haerokim.project_footprint.Activity.HomeActivity
import com.haerokim.project_footprint.Data.History
import com.haerokim.project_footprint.Data.NaverPlaceID
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.ui.home.HomeFragment
import com.haerokim.project_footprint.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import org.altbeacon.beacon.*
import org.altbeacon.beacon.service.ScanJob
import org.altbeacon.beacon.service.ScanJobScheduler
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ForegroundService : Service(), BeaconConsumer {

    lateinit var beaconManager: BeaconManager
    var beaconList: ArrayList<Beacon> = ArrayList()
    var alreadyVisitedList: MutableSet<String> = mutableSetOf() // Beacon의 UUID가 기록될 예정
    var surroundBeaconList: ArrayList<String> = ArrayList() //BroadCast 할 List (UUID 담김)

    override fun onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(RangeNotifier { beacons, region ->
            // 비콘이 감지되면 해당 함수가 호출됨. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어옴.
            beaconList.clear()
            surroundBeaconList.clear()

            if (beacons.size > 0) {
                for (beacon in beacons) {
                    beaconList.add(beacon)
                    surroundBeaconList.add(beacon.id1.toString())
                }
                //데이터 생성될 때마다 Broadcasting
                broadcastSurroundBeacon()
            }
        })

        try {
            beaconManager.startRangingBeaconsInRegion(Region("RangingUniqueId", null, null, null))
        } catch (e: RemoteException) {

        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        beaconManager = BeaconManager.getInstanceForApplication(applicationContext!!)
        beaconManager.beaconParsers
            .add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))

        var retrofit = Retrofit.Builder()
            .baseUrl("http://5e637d81aee0.ngrok.io/") //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var getPlaceInfoService: RetrofitService =
            retrofit.create(RetrofitService::class.java)

        var handler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                for (beacon in beaconList) {
                    if (beacon.distance in 0..80 && beacon.id1.toString() !in alreadyVisitedList) {
                        alreadyVisitedList.add(beacon.id1.toString())
                        Log.d("beacon_scanned", beacon.id1.toString())
                        //API 통해 Naver Place ID 획득
                        getPlaceInfoService.requestPlaceInfo(beacon.id1.toString())
                            .enqueue(object : retrofit2.Callback<List<NaverPlaceID>> {
                                override fun onFailure(
                                    call: Call<List<NaverPlaceID>>,
                                    t: Throwable
                                ) {
                                    Log.e("Error", t.message)
                                }

                                override fun onResponse(
                                    call: Call<List<NaverPlaceID>>,
                                    response: Response<List<NaverPlaceID>>
                                ) {
                                    var id = response.body()
                                    Log.d(
                                        "Foreground_GetPlaceInfo",
                                        "감지된 장소 : " + id?.get(0)?.naver_place_id
                                    )

                                    // 특정 장소 근접 시 해당 장소에 대한 정보 푸시알
                                    id?.get(0)?.naver_place_id?.let {
                                        ShowPlaceInfo(applicationContext, it).notifyInfo()
                                    }
                                }
                            })
                    } else if (beacon.distance > 5 && beacon.id1.toString() !in alreadyVisitedList) { // '장소 방문'으로 감지했을 때 History POST

                        Log.d("beacon_near_by", beacon.id1.toString())
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ISO_DATE_TIME
                        val formatted = current.format(formatter) // ex) 2020-07-31T22:21:51

                        //History POST API 활용
                    }
                }
                // 미 검증 코드
//                if (HomeViewModel().scanMode.value == true) {
                this.sendEmptyMessageDelayed(0, 1000)
//                }
            }
        }

        val clsIntent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, clsIntent, 0)
        val clsBuilder: NotificationCompat.Builder
        clsBuilder = if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "channel_foreground"
            val clsChannel =
                NotificationChannel(CHANNEL_ID, "footprint", NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                clsChannel
            )
            NotificationCompat.Builder(this, CHANNEL_ID)

        } else {
            NotificationCompat.Builder(this)
        }
        clsBuilder.setSmallIcon(R.drawable.ic_baseline_location_on_24)
            .setContentTitle("당신의 발자취를 따라가는 중").setContentText("오늘도 좋은 하루 되세요 :)")
            .setContentIntent(pendingIntent)

        // Foreground 서비스로 실행한다
        startForeground(1, clsBuilder.build())

        handler.sendEmptyMessage(0)
//        beaconManager.setEnableScheduledScanJobs(false)
        beaconManager.bind(this)
    }

    fun broadcastSurroundBeacon() {
        Log.d("broad", "Broadcasting Now!")
        val intent = Intent("surround_beacon_list")
        intent.putStringArrayListExtra("surround_beacon_list", surroundBeaconList)

        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        ScanJobScheduler.getInstance().cancelSchedule(this)
        beaconManager.stopMonitoringBeaconsInRegion(Region("RangingUniqueId", null, null, null))
        beaconManager.removeAllRangeNotifiers()
        beaconManager.removeAllMonitorNotifiers()
        beaconManager.unbind(this)

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
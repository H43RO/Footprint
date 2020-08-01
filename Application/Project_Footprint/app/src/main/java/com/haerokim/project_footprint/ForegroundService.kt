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
import com.haerokim.project_footprint.Activity.HomeActivity
import com.haerokim.project_footprint.Data.History
import com.haerokim.project_footprint.Data.NaverPlaceID
import com.haerokim.project_footprint.Network.RetrofitService
import org.altbeacon.beacon.*
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
    var alreadyVisitedList: ArrayList<String> = ArrayList() // Beacon의 UUID가 기록될 예정

    override fun onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(RangeNotifier { beacons, region ->
            // 비콘이 감지되면 해당 함수가 호출됨. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어옴.
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

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext()!!)
        beaconManager.getBeaconParsers()
            .add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))

        var retrofit = Retrofit.Builder()
            .baseUrl("http://0c0c648cfd92.ngrok.io/") //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var getPlaceInfoService: RetrofitService =
            retrofit.create(RetrofitService::class.java)

        var history: History? = null
        var placeID: String? = null

        var handler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                for (beacon in beaconList) {
                    if (beacon.distance < 5) {
                        if (beacon.id1.toString() in alreadyVisitedList) {
                            Log.d("Scan", "이미 방문함")
                        } else {
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
                                        Log.d("GetPlaceInfo", id?.get(0)?.naver_place_id)

                                        id?.get(0)?.naver_place_id?.let {
                                            ShowPlaceInfo(
                                                applicationContext,
                                                it
                                            ).notifyInfo()
                                        }
                                        alreadyVisitedList.add(beacon.id1.toString())
                                    }
                                })
                        }

                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ISO_DATE_TIME
                        val formatted = current.format(formatter) // ex) 2020-07-31T22:21:51

                        //History POST
                        history = placeID?.let {
                            History(
                                user = "1234", placeID = it, created_at = formatted,
                                historyID = null, comment = null, image = null
                            )
                        }
                    }
                }
                this.sendEmptyMessageDelayed(0, 1000)
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
        beaconManager.bind(this)

    }

    fun getSurroundBeacon(): ArrayList<Beacon> {
        return beaconList
    }

    override fun stopService(name: Intent?): Boolean {
        beaconManager.unbind(this)
        return super.stopService(name)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
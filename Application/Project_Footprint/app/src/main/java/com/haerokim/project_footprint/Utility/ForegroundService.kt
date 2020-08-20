package com.haerokim.project_footprint.Utility

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
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.Activity.HomeActivity
import com.haerokim.project_footprint.DataClass.*
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import io.paperdb.Paper
import io.realm.Realm
import io.realm.RealmConfiguration
import org.altbeacon.beacon.*
import org.altbeacon.beacon.service.ScanJobScheduler
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ForegroundService : Service(), BeaconConsumer {

    lateinit var beaconManager: BeaconManager
    var beaconList: ArrayList<Beacon> = ArrayList()
    var alreadyNotifiedPlace: MutableSet<String> = mutableSetOf() // 푸시알림을 보냈던 Beacon의 UUID가 기록될 예정
    var alreadyVisitedPlace: MutableSet<String> = mutableSetOf() // 푸시알림을 보냈던 Beacon의 UUID가 기록될 예정
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
        Paper.init(applicationContext)

        beaconManager = BeaconManager.getInstanceForApplication(applicationContext!!)
        beaconManager.beaconParsers
            .add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))

        // Realm을 활용해 장소의 정보를 Local에 저장하게 됨
        Realm.init(applicationContext)
        val config: RealmConfiguration = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(config)

        var realm = Realm.getDefaultInstance()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        var retrofitService: RetrofitService =
            retrofit.create(RetrofitService::class.java)

        var user: User = Paper.book().read("user_profile")

        var handler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                for (beacon in beaconList) {
                    // 지나가는 장소 ( 주변에 있는 장소 )
                    if (beacon.distance in 0..80 && beacon.id1.toString() !in alreadyNotifiedPlace) {
                        alreadyNotifiedPlace.add(beacon.id1.toString()) //이미 Notification 한 장소 리스트에 등록
                        Log.d("beacon_scanned", beacon.id1.toString())

                        // 모듈의 UUID 통해서 Naver Place ID 얻어옴
                        retrofitService.requestNaverPlaceID(beacon.id1.toString())
                            .enqueue(object : retrofit2.Callback<ArrayList<NaverPlaceID>> {
                                override fun onFailure(call: Call<ArrayList<NaverPlaceID>>, t: Throwable) {
                                    Log.e("Retrofit_Error", t.message)
                                }

                                override fun onResponse(call: Call<ArrayList<NaverPlaceID>>, response: Response<ArrayList<NaverPlaceID>>) {
                                    var id = response.body()
                                    realm.executeTransaction {
                                        with(it.createObject(VisitedPlace::class.java)) {
                                            this.beaconUUID = beacon.id1.toString()
                                            this.naverPlaceID = id?.get(0)?.naver_place_id
                                        }
                                    }

                                    Log.d("Foreground_GetPlaceInfo", "감지된 장소 : " + id?.get(0)?.naver_place_id)
                                    // 특정 장소 근접 시 해당 장소에 대한 정보 푸시알림
                                    id?.get(0)?.naver_place_id.let {
                                        ShowPlaceInfo(applicationContext, it!!).notifyInfo("nearPlace")
                                    }
                                }
                            })
                    }
                    // 가까운 장소 ( 방문 장소 )
                    if (beacon.distance < 6 && beacon.id1.toString() !in alreadyVisitedPlace) {
                        alreadyVisitedPlace.add(beacon.id1.toString()) //이미 방문한 장소 리스트에 등록
                        // '장소 방문'으로 감지했을 때 History POST (거리 6m 이내로 가정)
                        Log.d("beacon_near_by", beacon.id1.toString())
                        var naverPlaceID: String?
                        retrofitService.requestNaverPlaceID(beacon.id1.toString())
                            .enqueue(object : retrofit2.Callback<ArrayList<NaverPlaceID>> {
                                override fun onFailure(call: Call<ArrayList<NaverPlaceID>>, t: Throwable) {
                                    Log.e("Retrofit_Error", t.message)
                                }

                                override fun onResponse(call: Call<ArrayList<NaverPlaceID>>, response: Response<ArrayList<NaverPlaceID>>) {
                                    var id = response.body()

                                    naverPlaceID = id?.get(0)?.naver_place_id

                                    ShowPlaceInfo(applicationContext, naverPlaceID!!).notifyInfo("visitedPlace")

                                    //History POST API 대응 : NaverPlaceID와 사용자 ID로 History 생성
                                    naverPlaceID?.let {
                                        retrofitService.createRealVisitHistory(it, user.id)
                                            .enqueue(object : retrofit2.Callback<History> {
                                                override fun onFailure(call: Call<History>, t: Throwable) {
                                                    Log.e("Upload Error", t.message)
                                                }

                                                override fun onResponse(call: Call<History>, response: Response<History>) {
                                                    Log.d("Foreground_HistoryCreate", "업로드 된 장소 : $naverPlaceID")
                                                }
                                            })
                                    }
                                }
                            })
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
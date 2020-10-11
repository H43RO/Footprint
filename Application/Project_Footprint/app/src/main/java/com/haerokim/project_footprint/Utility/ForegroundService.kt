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
import com.google.gson.Gson
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

/**
 *  Foreground 에서 Beacon Scanning Service 동작
 *  - '발자취 따라가기' 모드를 ON 하면 실행됨
 *  - 'Android Beacon Library' 사용하여 가까운 Beacon Module Scanning 동작
 *
 *  - 사용자와 불특정 Beacon Module 간의 거리에 따라 다른 동작 수행
 *       1)  근처에 있을 때 : 해당 장소의 정보를 푸시알림으로 보내줌
 *       2)  매우 가까이 있을 때 : 해당 장소를 방문한 것으로 인식하여 사용자 History 자동 생성
 **/

class ForegroundService : Service(), BeaconConsumer {
    lateinit var beaconManager: BeaconManager
    var beaconList: ArrayList<Beacon> = ArrayList()
    var alreadyNotifiedPlace: MutableSet<String> = mutableSetOf() // 푸시알림을 보냈던 Beacon의 UUID가 기록될 예정
    var alreadyVisitedPlace: MutableSet<String> = mutableSetOf() // 이미 방문 기록된 Beacon의 UUID가 기록될 예정
    var surroundBeaconList: ArrayList<String> = ArrayList() //BroadCast 할 List (UUID 담김)
    var handlerStatus: Boolean = true

    lateinit var user: User
    lateinit var realmConfig: RealmConfiguration
    lateinit var realm : Realm
    lateinit var gson: Gson
    lateinit var retrofit: Retrofit
    lateinit var retrofitService: RetrofitService

    override fun onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(RangeNotifier { beacons, region ->
            // 비콘이 감지되면 해당 함수가 호출됨
            // beacons: Collection<Beacon> 에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어옴.
            beaconList.clear()
            surroundBeaconList.clear()

            if (beacons.isNotEmpty()) {
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
        return START_STICKY  // 강제 종료 되어도 다시 자동으로 재시작하는 서비스를 실행함
    }

    override fun onCreate() {
        super.onCreate()

        handlerStatus = true

        // Realm, Paper DB 사용을 위해 init() 필요
        Realm.init(applicationContext)
        Paper.init(applicationContext)

        // 사용자 고유 History 생성을 위해 User 정보를 얻어야 함 (Paper DB에 저장되어 있음)
        user = Paper.book().read("user_profile")

        // Realm을 활용해 방문한 장소의 정보를 Local에 저장하게 됨 (캐싱 목적)
        realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(realmConfig)

        // 방문 장소 데이터 저장(캐싱)을 위한 Realm 객체 생성
        realm = Realm.getDefaultInstance()

        gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()

        // API 호출을 위한 Retrofit 객체 생성
        retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofitService =
            retrofit.create(RetrofitService::class.java)

        // Beacon Scanning 을 위한 BeaconManager 인스턴스 선언
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext!!)
        beaconManager.beaconParsers  // 일단 Beacon Module 스캔을 위한 레이아웃 정의
            .add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))

        // Beacon Scanning 기능 동작을 위해 BeaconManager Binding (스캔 실행 시작)
        beaconManager.bind(this)

        /**  안드로이드 정책 상 Foreground Service 중일 때 Notification 을 유지해야함  **/

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

        // 주기적으로 스캔한 Beacon 모듈 분석 을 위한 Handler
        val handler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                analyzeScannedBeacon()

                if (handlerStatus) {
                    this.sendEmptyMessageDelayed(0, 1000)
                }
            }
        }

        // Handler 최초 실행
        handler.sendEmptyMessage(0)
    }

    private fun analyzeScannedBeacon(){
        for (beacon in beaconList) {
            Log.d("beacon_scanned", beacon.id1.toString())

            // 지나가는 거리에 있는 장소 ( 주변에 있는 장소 )
            if (beacon.distance in 0..80 && beacon.id1.toString() !in alreadyNotifiedPlace) {
                // 이미 Notification 한 장소 리스트에 등록 ( 중복 동작 방지 )
                alreadyNotifiedPlace.add(beacon.id1.toString())

                // 모듈의 UUID 통해서 Naver Place ID 얻어옴
                retrofitService.requestNaverPlaceID(beacon.id1.toString())
                    .enqueue(object : retrofit2.Callback<ArrayList<NaverPlaceID>> {
                        override fun onFailure(
                            call: Call<ArrayList<NaverPlaceID>>,
                            t: Throwable
                        ) {
                            Log.e("Retrofit_Error", t.message)
                        }

                        override fun onResponse(
                            call: Call<ArrayList<NaverPlaceID>>,
                            response: Response<ArrayList<NaverPlaceID>>
                        ) {
                            val id = response.body()
                            id?.let {
                                // 해당 장소의 이름을 Realm (Local DB)에 저장함
                                realm.executeTransaction {
                                    with(it.createObject(VisitedPlace::class.java)) {
                                        this.beaconUUID = beacon.id1.toString()
                                        this.naverPlaceID = id[0].naver_place_id
                                    }
                                }
                                Log.d(
                                    "Foreground_GetPlaceInfo",
                                    "감지된 장소 : " + id[0].naver_place_id
                                )

                                // 해당 장소 상세정보 푸시알림 보내줌
                                ShowPlaceInfo(
                                    applicationContext,
                                    id[0].naver_place_id
                                ).notifyInfo("nearPlace")

                            }
                        }
                    })
            }
            // 매우 가까운 장소 ( 방문으로 인식 )
            if (beacon.distance < 6 && beacon.id1.toString() !in alreadyVisitedPlace) {
                Log.d("beacon_near_by", beacon.id1.toString())

                //이미 방문한 장소 리스트에 등록 ( 중복 동작 방지 )
                alreadyVisitedPlace.add(beacon.id1.toString())

                // '장소 방문'으로 감지했을 때 사용자 History 자동 생성 (거리 6m 이내로 가정)
                var naverPlaceID: String
                retrofitService.requestNaverPlaceID(beacon.id1.toString())
                    .enqueue(object : retrofit2.Callback<ArrayList<NaverPlaceID>> {
                        override fun onFailure(
                            call: Call<ArrayList<NaverPlaceID>>,
                            t: Throwable
                        ) {
                            Log.e("Retrofit_Error", t.message)
                        }

                        // API 특성 상 응답 객체는 1개 이지만 배열 형태로 오기 때문에 ArrayList 사용
                        override fun onResponse(
                            call: Call<ArrayList<NaverPlaceID>>,
                            response: Response<ArrayList<NaverPlaceID>>
                        ) {
                            val id = response.body()
                            id?.let {
                                // 따라서 배열의 0번째 객체가 응답으로 요하는 객체라고 봐도 무방
                                naverPlaceID = id[0].naver_place_id

                                // 사용자에게 해당 장소를 방문한 것을 확인했다는 푸시알림 보내줌
                                ShowPlaceInfo(
                                    applicationContext,
                                    naverPlaceID
                                ).notifyInfo("visitedPlace")

                                // NaverPlaceID 와 사용자 ID로 History 생성 API 호출
                                retrofitService.createRealVisitHistory(
                                    naverPlaceID,
                                    user.id
                                )
                                    .enqueue(object : retrofit2.Callback<History> {
                                        override fun onFailure(
                                            call: Call<History>,
                                            t: Throwable
                                        ) {
                                            Log.e("Upload Error", t.message)
                                        }

                                        override fun onResponse(
                                            call: Call<History>,
                                            response: Response<History>
                                        ) {
                                            Log.d(
                                                "Foreground_History",
                                                "업로드 된 장소 : $naverPlaceID"
                                            )
                                        }
                                    })
                            }
                        }
                    })
            }
        }
    }

    // SurroundFragment 로 주변 Beacon (장소) 리스트 Broadcasting
    private fun broadcastSurroundBeacon() {
        Log.d("broad", "Broadcasting Now!")
        val intent = Intent("surround_beacon_list")
        intent.putStringArrayListExtra("surround_beacon_list", surroundBeaconList)

        sendBroadcast(intent)
    }

    // 서비스 종료 및 종료 요청 시 BeaconManager Unbinding
    override fun onDestroy() {
        super.onDestroy()
        handlerStatus = false

        ScanJobScheduler.getInstance().cancelSchedule(this)
        beaconManager.stopMonitoringBeaconsInRegion(Region("RangingUniqueId", null, null, null))
        beaconManager.unbind(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
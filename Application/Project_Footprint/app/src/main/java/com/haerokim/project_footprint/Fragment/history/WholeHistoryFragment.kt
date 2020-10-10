package com.haerokim.project_footprint.Fragment.history

/**  현재 사용하지 않는 기능 (필요 없다고 판단) **/

//class WholeHistoryFragment : Fragment() {
//    lateinit var recyclerView: RecyclerView
//    lateinit var viewAdapter: RecyclerView.Adapter<*>
//    lateinit var viewManager: RecyclerView.LayoutManager
//    var historyList: ArrayList<History> = ArrayList()
//    var responseBody: ArrayList<History> = ArrayList()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Realm 사용을 위해 init() 필요
//        Realm.init(context)
//        return inflater.inflate(R.layout.fragment_whole_history, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//        viewManager = LinearLayoutManager(context)
//        viewAdapter = HistoryListAdapter(
//            historyList,
//            requireContext()
//        )
//
//        recyclerView =
//            view.findViewById<RecyclerView>(R.id.whole_history_list).apply {
//                setHasFixedSize(true)
//                layoutManager = viewManager
//                adapter = viewAdapter
//            }
//
//        whole_history_swipe.setColorSchemeColors(Color.GRAY)
//        whole_history_swipe.setOnRefreshListener {
//            getWholeHistoryList()
//        }
//
//        getWholeHistoryList()
//    }
//
//    fun getWholeHistoryList() {
//        text_whole_no_data.visibility = View.GONE
//        loading_whole_history.visibility = View.VISIBLE
//
//        val config: RealmConfiguration = RealmConfiguration.Builder()
//            .deleteRealmIfMigrationNeeded()
//            .build()
//        Realm.setDefaultConfiguration(config)
//
//        var realm = Realm.getDefaultInstance()
//
//        var user: User = Paper.book().read("user_profile")
//        val gson = GsonBuilder()
//            .setDateFormat("yyyy-MM-dd'T'HH:mm")
//            .create()
//        var retrofit = Retrofit.Builder()
//            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .build()
//        var getWholeHistoryService: RetrofitService = retrofit.create(RetrofitService::class.java)
//
//        Log.d("User ID", user?.id.toString())
//        getWholeHistoryService.requestWholeHistoryList(user!!.id)
//            .enqueue(object : Callback<ArrayList<History>> {
//                override fun onFailure(call: Call<ArrayList<History>>, t: Throwable) {
//                    Log.e("Whole_history_Error", t.message)
//
//                    loading_whole_history.visibility = View.GONE
//                    whole_history_list.visibility = View.GONE
//                    text_whole_no_data.visibility = View.VISIBLE
//                    text_whole_no_data.text = "정보를 가져오지 못했습니다"
//                    whole_history_swipe.isRefreshing = false
//                }
//
//                override fun onResponse(
//                    call: Call<ArrayList<History>>,
//                    response: Response<ArrayList<History>>
//                ) {
//                        historyList.clear()
//                        if (response.body()?.size == 0) {
//                            keyword_history_list.visibility = View.GONE
//                            text_whole_no_data.visibility = View.VISIBLE
//                            loading_whole_history.visibility = View.GONE
//                            text_whole_no_data.text = "기록이 없습니다"
//                            whole_history_swipe.isRefreshing = false
//                        } else {
//                            text_whole_no_data.visibility = View.GONE
//                            responseBody = response.body()!!
//                            for (history in responseBody) {
//                                if (history.place != null) { // place가 null이면 임의로 생성한 history이므로 이름 변환 과정을 건너뜀
//                                    realm.executeTransaction {
//                                        val visitedPlace: VisitedPlace? =
//                                            it.where(VisitedPlace::class.java)
//                                                .equalTo("naverPlaceID", history.place).findFirst()
//                                        history.place = visitedPlace?.placeTitle
//                                            ?: GetPlaceTitleOnly(history.place!!).execute().get()
//                                    }
//                                }
//                            }
//                            historyList.addAll(responseBody)
//                            viewAdapter.notifyDataSetChanged()
//                            whole_history_swipe.isRefreshing = false
//                            loading_whole_history.visibility = View.GONE
//                        }
//                }
//            })
//    }
//}
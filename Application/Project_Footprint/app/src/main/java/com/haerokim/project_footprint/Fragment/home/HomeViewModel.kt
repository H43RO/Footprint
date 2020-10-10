package com.haerokim.project_footprint.Fragment.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    //Fragment 단에서 관찰하고 있는 데이터 : Beacon Scanning, Foreground Service 여부
    val scanMode = MutableLiveData<Boolean>()

    fun changeMode(mode: String){
        // "on"이 입력되면 value를 true로 바꿔줌
        scanMode.value = (mode == "on")
    }

}
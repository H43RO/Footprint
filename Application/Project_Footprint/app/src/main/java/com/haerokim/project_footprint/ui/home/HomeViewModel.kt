package com.haerokim.project_footprint.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
    }

    fun setValue(string: String){
        _text.value = string
    }
    val text: LiveData<String> = _text
}
package com.example.whereami.ui.main.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.whereami.data.LocationLiveData

class MainViewModel(application: Application) : ViewModel() {

    private val location = LocationLiveData(application)

    fun getLocationData() = location
}
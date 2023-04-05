package com.bignerdranch.android.photogallery

import android.app.Application
import com.bignerdranch.android.photogallery.api.PreferencesRepository

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        PreferencesRepository.initialize(this)
    }
}
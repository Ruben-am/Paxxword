package com.rubenalba.paxxword

import android.app.Application
import com.rubenalba.paxxword.data.manager.SessionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PaxxwordApp : Application() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
    }
}
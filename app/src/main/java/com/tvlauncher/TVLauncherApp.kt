package com.tvlauncher

import android.app.Application
import android.os.StrictMode
import com.tvlauncher.data.PrefsManager

class TVLauncherApp : Application() {
    lateinit var prefsManager: PrefsManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefsManager = PrefsManager(this)
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }

    companion object {
        lateinit var instance: TVLauncherApp
            private set
    }
}

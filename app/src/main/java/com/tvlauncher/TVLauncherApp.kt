package com.tvlauncher

import android.app.Application
import android.os.StrictMode
import com.tvlauncher.BuildConfig

class TVLauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 仅在Debug模式启用StrictMode
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }
}

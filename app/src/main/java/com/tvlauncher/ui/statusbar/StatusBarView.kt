package com.tvlauncher.ui.statusbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.tvlauncher.R
import com.tvlauncher.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatusBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val wifiIcon: ImageView
    private val weatherIcon: ImageView
    private val weatherText: TextView
    private val dateText: TextView
    private val timeText: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var activity: MainActivity? = null
    private var isAreaFocused = false

    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000)
        }
    }

    private val weatherUpdateRunnable = object : Runnable {
        override fun run() {
            updateWeather()
            handler.postDelayed(this, 30 * 60 * 1000) // 30 minutes
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_status_bar, this, true)
        wifiIcon = findViewById(R.id.wifiIcon)
        weatherIcon = findViewById(R.id.weatherIcon)
        weatherText = findViewById(R.id.weatherText)
        dateText = findViewById(R.id.dateText)
        timeText = findViewById(R.id.timeText)

        alpha = 0.7f
    }

    fun setActivity(activity: MainActivity) {
        this.activity = activity
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(timeUpdateRunnable)
        handler.post(weatherUpdateRunnable)
        updateTime()
        updateDate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(timeUpdateRunnable)
        handler.removeCallbacks(weatherUpdateRunnable)
    }

    private fun updateTime() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeText.text = sdf.format(Date())
    }

    private fun updateDate() {
        val sdf = SimpleDateFormat("MM月dd日 EEEE", Locale.getDefault())
        dateText.text = sdf.format(Date())
    }

    fun updateWifiState(isConnected: Boolean) {
        if (isConnected) {
            wifiIcon.setImageResource(R.drawable.ic_wifi_on)
            wifiIcon.alpha = 1.0f
        } else {
            wifiIcon.setImageResource(R.drawable.ic_wifi_off)
            wifiIcon.alpha = 0.5f
        }
    }

    fun updateWeather() {
        // Weather data would be fetched from a weather API
        // For now, show placeholder
        weatherIcon.setImageResource(R.drawable.ic_weather_sunny)
        weatherText.text = "--°"
    }

    fun setWeatherData(iconRes: Int, temp: String) {
        weatherIcon.setImageResource(iconRes)
        weatherText.text = temp
    }

    fun setAreaFocused(focused: Boolean) {
        isAreaFocused = focused
        animate()
            .alpha(if (focused) 0.4f else 0.7f)
            .setDuration(200)
            .start()
    }
}

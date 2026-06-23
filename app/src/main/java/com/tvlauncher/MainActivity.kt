package com.tvlauncher

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    
    // Views
    private lateinit var statusWifi: ImageView
    private lateinit var statusWeather: ImageView
    private lateinit var statusTime: TextView
    private lateinit var statusDate: TextView
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var favoriteAppsRecyclerView: RecyclerView
    
    // Adapters
    private lateinit var appsAdapter: AppListAdapter
    private lateinit var favoriteAdapter: FavoriteAppAdapter
    
    // Data
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MM月dd日 EEEE", Locale.CHINESE)
    
    // WiFi状态广播接收器
    private var wifiReceiver: WifiStateReceiver? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 全屏沉浸模式
        setupFullScreen()
        
        setContentView(R.layout.activity_main)
        
        // 初始化Views
        initViews()
        
        // 加载应用列表
        loadApps()
        
        // 启动时钟更新
        startClockUpdate()
        
        // 注册WiFi状态监听
        registerWifiReceiver()
    }
    
    private fun setupFullScreen() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // 沉浸式模式
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }
    
    private fun initViews() {
        // 状态栏
        statusWifi = findViewById(R.id.statusWifi)
        statusWeather = findViewById(R.id.statusWeather)
        statusTime = findViewById(R.id.statusTime)
        statusDate = findViewById(R.id.statusDate)
        
        // 收藏应用（横向滚动）
        favoriteAppsRecyclerView = findViewById(R.id.favoriteAppsRecyclerView)
        favoriteAdapter = FavoriteAppAdapter { appInfo ->
            launchApp(appInfo)
        }
        favoriteAppsRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = favoriteAdapter
            // 优化性能
            setHasFixedSize(true)
            setItemViewCacheSize(10)
        }
        
        // 所有应用（网格布局）
        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        appsAdapter = AppListAdapter { appInfo ->
            launchApp(appInfo)
        }
        appsRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = appsAdapter
            // 优化性能
            setHasFixedSize(true)
            setItemViewCacheSize(20)
        }
        
        // 设置焦点顺序
        setupFocusNavigation()
    }
    
    private fun setupFocusNavigation() {
        // 收藏应用获得焦点时
        favoriteAppsRecyclerView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        appsRecyclerView.requestFocus()
                        true
                    }
                    else -> false
                }
            } else false
        }
        
        // 所有应用获得焦点时
        appsRecyclerView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        favoriteAppsRecyclerView.requestFocus()
                        true
                    }
                    else -> false
                }
            } else false
        }
    }
    
    @SuppressLint("QueryPermissionsNeeded")
    private fun loadApps() {
        Thread {
            val pm = packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            
            val apps = pm.queryIntentActivities(mainIntent, 0)
                .filter { it.activityInfo.packageName != packageName }
                .map { resolveInfo ->
                    AppInfo(
                        name = resolveInfo.loadLabel(pm).toString(),
                        packageName = resolveInfo.activityInfo.packageName,
                        icon = resolveInfo.loadIcon(pm)
                    )
                }
                .sortedBy { it.name.lowercase() }
            
            // 预加载收藏应用（前6个）
            val favorites = apps.take(6)
            
            runOnUiThread {
                favoriteAdapter.submitList(favorites)
                appsAdapter.submitList(apps)
                
                // 默认焦点到收藏应用
                favoriteAppsRecyclerView.requestFocus()
            }
        }.start()
    }
    
    private fun launchApp(appInfo: AppInfo) {
        val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
        intent?.let {
            startActivity(it)
        }
    }
    
    private fun startClockUpdate() {
        val updateRunnable = object : Runnable {
            override fun run() {
                updateClock()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateRunnable)
    }
    
    private fun updateClock() {
        val now = Date()
        statusTime.text = timeFormat.format(now)
        statusDate.text = dateFormat.format(now)
    }
    
    private fun registerWifiReceiver() {
        wifiReceiver = WifiStateReceiver { isConnected ->
            runOnUiThread {
                statusWifi.setImageResource(
                    if (isConnected) R.drawable.ic_wifi_on
                    else R.drawable.ic_wifi_off
                )
            }
        }
        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        registerReceiver(wifiReceiver, filter)
    }
    
    override fun onResume() {
        super.onResume()
        setupFullScreen()
        updateWifiStatus()
    }
    
    private fun updateWifiStatus() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val isConnected = wifiManager.isWifiEnabled && wifiManager.connectionInfo.networkId != -1
        statusWifi.setImageResource(
            if (isConnected) R.drawable.ic_wifi_on
            else R.drawable.ic_wifi_off
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        wifiReceiver?.let { unregisterReceiver(it) }
        handler.removeCallbacksAndMessages(null)
    }
    
    // 拦截HOME键
    override fun onBackPressed() {
        // 不做任何事，留在桌面
    }
}

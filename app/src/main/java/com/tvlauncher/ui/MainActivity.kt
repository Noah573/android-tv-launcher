package com.tvlauncher.ui

import android.app.UiModeManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.tvlauncher.R
import com.tvlauncher.databinding.ActivityMainBinding
import com.tvlauncher.data.PrefsManager
import com.tvlauncher.ui.controlcenter.ControlCenterFragment
import com.tvlauncher.ui.home.HomeFragment
import com.tvlauncher.ui.screensaver.ScreensaverFragment
import com.tvlauncher.ui.search.SearchFragment
import com.tvlauncher.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PrefsManager
    private var isRemoteMode = false
    private var screensaverHandler = Handler(Looper.getMainLooper())
    private var screensaverRunnable: Runnable? = null
    private val screensaverTimeout = 300_000L // 5 minutes

    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    updateStatusBarWifi(isWifiConnected())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PrefsManager(this)

        setupFullScreen()
        setupInputMode()
        setupWallpaper()
        setupFragments()
        setupOverlayContainer()

        if (savedInstanceState == null) {
            checkDefaultLauncher()
            if (prefsManager.isFirstLaunch()) {
                launchOnboarding()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerConnectivityReceiver()
        resetScreensaverTimer()
        updateStatusBarWifi(isWifiConnected())
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(connectivityReceiver)
        } catch (_: Exception) {}
        cancelScreensaverTimer()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetScreensaverTimer()
    }

    private fun setupFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, binding.root)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setupInputMode() {
        isRemoteMode = isRunningOnTV()
        if (!isRemoteMode) {
            binding.root.setOnGenericMotionListener { _, event ->
                if (event.source and InputDevice.SOURCE_CLASS_POINTER != 0) {
                    isRemoteMode = false
                    false
                } else {
                    false
                }
            }
        }
    }

    private fun isRunningOnTV(): Boolean {
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
    }

    fun isRemoteInputMode(): Boolean = isRemoteMode

    private fun setupWallpaper() {
        try {
            binding.wallpaperBackground.setImageResource(R.drawable.default_wallpaper)
        } catch (_: Exception) {
            binding.wallpaperBackground.setBackgroundColor(0xFF1A1A2E.toInt())
        }
        binding.wallpaperBackground.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    fun setWallpaper(resourceId: Int) {
        binding.wallpaperBackground.setImageResource(resourceId)
    }

    private fun setupFragments() {
        if (supportFragmentManager.findFragmentByTag("home") == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.homeContent, HomeFragment.newInstance(), "home")
                .commit()
        }
    }

    private fun setupOverlayContainer() {
        binding.overlayContainer.visibility = View.GONE
    }

    private fun checkDefaultLauncher() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val currentDefault = resolveInfo?.activityInfo?.packageName
        if (currentDefault != null && currentDefault != packageName) {
            showSetDefaultLauncherDialog()
        }
    }

    private fun showSetDefaultLauncherDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.set_default_launcher_title)
            .setMessage(R.string.set_default_launcher_message)
            .setPositiveButton(R.string.settings) { _, _ ->
                try {
                    startActivity(Intent(android.provider.Settings.ACTION_HOME_SETTINGS))
                } catch (_: Exception) {
                    Toast.makeText(this, R.string.cannot_open_settings, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.later, null)
            .show()
    }

    private fun launchOnboarding() {
        try {
            startActivity(Intent(this, com.tvlauncher.ui.onboarding.OnboardingActivity::class.java))
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to launch onboarding", e)
        }
    }

    // --- Status bar helpers ---

    private fun updateStatusBarWifi(isConnected: Boolean) {
        try {
            val statusBar = binding.root.findViewById<View>(R.id.statusBar) ?: return
            val wifiIcon = statusBar.findViewById<ImageView>(R.id.wifiIcon) ?: return
            if (isConnected) {
                wifiIcon.setImageResource(R.drawable.ic_wifi_on)
                wifiIcon.alpha = 1.0f
            } else {
                wifiIcon.setImageResource(R.drawable.ic_wifi_off)
                wifiIcon.alpha = 0.5f
            }
        } catch (_: Exception) {}
    }

    // --- Overlay management ---

    fun openControlCenter() {
        showOverlay(ControlCenterFragment.newInstance(), "controlCenter")
    }

    fun openSearch() {
        showOverlay(SearchFragment.newInstance(), "search")
    }

    fun openSettings() {
        showOverlay(SettingsFragment.newInstance(), "settings")
    }

    fun openScreensaver() {
        showOverlay(ScreensaverFragment.newInstance(), "screensaver")
    }

    private fun showOverlay(fragment: Fragment, tag: String) {
        binding.overlayContainer.visibility = View.VISIBLE
        binding.overlayContainer.alpha = 0f
        binding.overlayContainer.animate()
            .alpha(1f)
            .setDuration(200)
            .start()

        supportFragmentManager.beginTransaction()
            .replace(R.id.overlayContainer, fragment, tag)
            .commit()
    }

    fun closeOverlay() {
        binding.overlayContainer.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.overlayContainer.visibility = View.GONE
                val currentFragment = supportFragmentManager.findFragmentById(R.id.overlayContainer)
                if (currentFragment != null) {
                    supportFragmentManager.beginTransaction()
                        .remove(currentFragment)
                        .commit()
                }
            }
            .start()
    }

    fun isOverlayOpen(): Boolean {
        return binding.overlayContainer.visibility == View.VISIBLE
    }

    // --- Screensaver ---

    private fun resetScreensaverTimer() {
        cancelScreensaverTimer()
        if (isOverlayOpen()) return
        screensaverRunnable = Runnable {
            if (!isFinishing && !isOverlayOpen()) {
                openScreensaver()
            }
        }
        screensaverHandler.postDelayed(screensaverRunnable!!, screensaverTimeout)
    }

    private fun cancelScreensaverTimer() {
        screensaverRunnable?.let { screensaverHandler.removeCallbacks(it) }
        screensaverRunnable = null
    }

    // --- Connectivity ---

    private fun registerConnectivityReceiver() {
        val filter = IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(connectivityReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(connectivityReceiver, filter)
        }
    }

    fun isWifiConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
    }

    // --- Key events ---

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        resetScreensaverTimer()
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                openControlCenter()
                return true
            }
            KeyEvent.KEYCODE_SEARCH -> {
                openSearch()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (isOverlayOpen()) {
            closeOverlay()
            return
        }
        // Do nothing - stay on desktop (launcher behavior)
    }
}

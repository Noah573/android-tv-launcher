package com.tvlauncher.ui.controlcenter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.tvlauncher.R
import com.tvlauncher.ui.MainActivity
import com.tvlauncher.ui.settings.SettingsFragment

class ControlCenterFragment : Fragment() {

    private lateinit var wifiToggle: ImageButton
    private lateinit var bluetoothToggle: ImageButton
    private lateinit var brightnessSlider: SeekBar
    private lateinit var volumeSlider: SeekBar
    private lateinit var sleepButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private lateinit var wifiLabel: TextView
    private lateinit var bluetoothLabel: TextView

    private var isWifiEnabled = false
    private var isBluetoothEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_control_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wifiToggle = view.findViewById(R.id.wifiToggle)
        bluetoothToggle = view.findViewById(R.id.bluetoothToggle)
        brightnessSlider = view.findViewById(R.id.brightnessSlider)
        volumeSlider = view.findViewById(R.id.volumeSlider)
        sleepButton = view.findViewById(R.id.sleepButton)
        settingsButton = view.findViewById(R.id.settingsButton)
        wifiLabel = view.findViewById(R.id.wifiLabel)
        bluetoothLabel = view.findViewById(R.id.bluetoothLabel)

        setupInitialState()
        setupClickListeners()
        setupSliders()
        playSlideInAnimation(view)
    }

    private fun setupInitialState() {
        try {
            val wifiManager = requireContext().applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            isWifiEnabled = wifiManager.isWifiEnabled
        } catch (_: Exception) {}
        updateWifiUI()

        try {
            val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter
            isBluetoothEnabled = bluetoothAdapter?.isEnabled == true
        } catch (_: Exception) {}
        updateBluetoothUI()

        // Volume
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        volumeSlider.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeSlider.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        // Brightness
        brightnessSlider.max = 255
        try {
            brightnessSlider.progress = Settings.System.getInt(
                requireContext().contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (_: Exception) {
            brightnessSlider.progress = 128
        }
    }

    private fun setupClickListeners() {
        wifiToggle.setOnClickListener {
            toggleWifi()
        }

        bluetoothToggle.setOnClickListener {
            toggleBluetooth()
        }

        sleepButton.setOnClickListener {
            (activity as? MainActivity)?.openScreensaver()
        }

        settingsButton.setOnClickListener {
            (activity as? MainActivity)?.closeOverlay()
            (activity as? MainActivity)?.openSettings()
        }

        // Back button dismisses
        view?.findViewById<View>(R.id.controlCenterOverlay)?.setOnClickListener {
            (activity as? MainActivity)?.closeOverlay()
        }
    }

    private fun setupSliders() {
        volumeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        brightnessSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    try {
                        Settings.System.putInt(
                            requireContext().contentResolver,
                            Settings.System.SCREEN_BRIGHTNESS,
                            progress
                        )
                    } catch (_: SecurityException) {
                        Toast.makeText(context, R.string.brightness_permission, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    @Suppress("DEPRECATION")
    private fun toggleWifi() {
        try {
            val wifiManager = requireContext().applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startActivity(Intent(Settings.Panel.ACTION_WIFI))
            } else {
                wifiManager.setWifiEnabled(!isWifiEnabled)
                isWifiEnabled = !isWifiEnabled
                updateWifiUI()
            }
        } catch (_: Exception) {
            Toast.makeText(context, R.string.wifi_toggle_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleBluetooth() {
        try {
            val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            } else {
                if (bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.disable()
                    isBluetoothEnabled = false
                } else {
                    bluetoothAdapter.enable()
                    isBluetoothEnabled = true
                }
                updateBluetoothUI()
            }
        } catch (_: Exception) {
            Toast.makeText(context, R.string.bluetooth_toggle_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWifiUI() {
        if (isWifiEnabled) {
            wifiToggle.setImageResource(R.drawable.ic_wifi_on)
            wifiToggle.alpha = 1.0f
            wifiLabel.text = getString(R.string.wifi_on)
        } else {
            wifiToggle.setImageResource(R.drawable.ic_wifi_off)
            wifiToggle.alpha = 0.5f
            wifiLabel.text = getString(R.string.wifi_off)
        }
    }

    private fun updateBluetoothUI() {
        if (isBluetoothEnabled) {
            bluetoothToggle.setImageResource(R.drawable.ic_bluetooth_on)
            bluetoothToggle.alpha = 1.0f
            bluetoothLabel.text = getString(R.string.bluetooth_on)
        } else {
            bluetoothToggle.setImageResource(R.drawable.ic_bluetooth_off)
            bluetoothToggle.alpha = 0.5f
            bluetoothLabel.text = getString(R.string.bluetooth_off)
        }
    }

    private fun playSlideInAnimation(view: View) {
        val cardView = view.findViewById<View>(R.id.controlCenterCard) ?: return
        cardView.translationY = -500f
        cardView.alpha = 0f

        val slideDown = ObjectAnimator.ofFloat(cardView, "translationY", -500f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(cardView, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(slideDown, fadeIn)
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    companion object {
        fun newInstance(): ControlCenterFragment = ControlCenterFragment()
    }
}

package com.tvlauncher.ui.controlcenter

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
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.tvlauncher.R
import com.tvlauncher.ui.MainActivity

class ControlCenterFragment : Fragment() {

    private lateinit var wifiSwitch: SwitchCompat
    private lateinit var bluetoothSwitch: SwitchCompat
    private lateinit var brightnessSeekBar: SeekBar
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var sleepButton: View
    private lateinit var settingsButton: View

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

        wifiSwitch = view.findViewById(R.id.wifiSwitch)
        bluetoothSwitch = view.findViewById(R.id.bluetoothSwitch)
        brightnessSeekBar = view.findViewById(R.id.brightnessSeekBar)
        volumeSeekBar = view.findViewById(R.id.volumeSeekBar)
        sleepButton = view.findViewById(R.id.sleepButton)
        settingsButton = view.findViewById(R.id.settingsButton)

        setupInitialState()
        setupClickListeners()
        setupSliders()
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
        volumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        // Brightness
        brightnessSeekBar.max = 255
        try {
            brightnessSeekBar.progress = Settings.System.getInt(
                requireContext().contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (_: Exception) {
            brightnessSeekBar.progress = 128
        }
    }

    private fun setupClickListeners() {
        wifiSwitch.setOnCheckedChangeListener { _, _ ->
            toggleWifi()
        }

        bluetoothSwitch.setOnCheckedChangeListener { _, _ ->
            toggleBluetooth()
        }

        sleepButton.setOnClickListener {
            (activity as? MainActivity)?.openScreensaver()
        }

        settingsButton.setOnClickListener {
            (activity as? MainActivity)?.closeOverlay()
            (activity as? MainActivity)?.openSettings()
        }
    }

    private fun setupSliders() {
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
        wifiSwitch.isChecked = isWifiEnabled
        val wifiIcon = view?.findViewById<android.widget.ImageView>(R.id.wifiIcon)
        if (isWifiEnabled) {
            wifiIcon?.setImageResource(R.drawable.ic_wifi_on)
            wifiIcon?.alpha = 1.0f
        } else {
            wifiIcon?.setImageResource(R.drawable.ic_wifi_off)
            wifiIcon?.alpha = 0.5f
        }
    }

    private fun updateBluetoothUI() {
        bluetoothSwitch.isChecked = isBluetoothEnabled
        val bluetoothIcon = view?.findViewById<android.widget.ImageView>(R.id.bluetoothIcon)
        bluetoothIcon?.setImageResource(R.drawable.ic_bluetooth)
        bluetoothIcon?.alpha = if (isBluetoothEnabled) 1.0f else 0.5f
    }

    companion object {
        fun newInstance(): ControlCenterFragment = ControlCenterFragment()
    }
}

package com.tvlauncher.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tvlauncher.R
import com.tvlauncher.ui.MainActivity

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // No close button in layout, back key handles closing

        val recyclerView = view.findViewById<RecyclerView>(R.id.settingsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = SettingsAdapter(getSettingsItems())
    }

    private fun getSettingsItems(): List<SettingsItem> {
        return listOf(
            SettingsItem(R.drawable.ic_wallpaper, getString(R.string.settings_wallpaper)) {
                openWallpaperPicker()
            },
            SettingsItem(R.drawable.ic_animation, getString(R.string.settings_animation)) {
                cycleAnimationLevel()
            },
            SettingsItem(R.drawable.ic_about, getString(R.string.settings_about)) {
                showAbout()
            }
        )
    }

    private fun openWallpaperPicker() {
        val mainActivity = activity as? MainActivity ?: return
        val wallpapers = listOf(
            R.drawable.bg_wallpaper_1,
            R.drawable.bg_wallpaper_2,
            R.drawable.bg_wallpaper_3,
            R.drawable.bg_wallpaper_4,
            R.drawable.bg_wallpaper_5,
            R.drawable.bg_wallpaper_6
        )
        val names = wallpapers.map { "Wallpaper ${wallpapers.indexOf(it) + 1}" }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(mainActivity)
            .setTitle(R.string.settings_wallpaper)
            .setItems(names) { _, which ->
                mainActivity.setWallpaper(wallpapers[which])
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun cycleAnimationLevel() {
        val mainActivity = activity as? MainActivity ?: return
        val prefs = com.tvlauncher.data.PrefsManager(mainActivity)
        val current = prefs.getAnimationLevel()
        val next = when (current) {
            com.tvlauncher.util.Constants.ANIM_LEVEL_HIGH -> com.tvlauncher.util.Constants.ANIM_LEVEL_MEDIUM
            com.tvlauncher.util.Constants.ANIM_LEVEL_MEDIUM -> com.tvlauncher.util.Constants.ANIM_LEVEL_LOW
            else -> com.tvlauncher.util.Constants.ANIM_LEVEL_HIGH
        }
        prefs.setAnimationLevel(next)
        val levelName = when (next) {
            com.tvlauncher.util.Constants.ANIM_LEVEL_HIGH -> getString(R.string.anim_high)
            com.tvlauncher.util.Constants.ANIM_LEVEL_MEDIUM -> getString(R.string.anim_medium)
            else -> getString(R.string.anim_low)
        }
        android.widget.Toast.makeText(mainActivity, "${getString(R.string.settings_animation)}: $levelName", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showAbout() {
        val mainActivity = activity as? MainActivity ?: return
        androidx.appcompat.app.AlertDialog.Builder(mainActivity)
            .setTitle(R.string.settings_about)
            .setMessage("TV Launcher v2.0\n\nA modern Android TV launcher inspired by Apple TV.\n\nFeatures:\n• Remote & Touch support\n• Wallpaper customization\n• Performance adaptive animations\n• Weather integration\n• Screensaver\n• Global search")
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    data class SettingsItem(val iconRes: Int, val title: String, val onClick: () -> Unit)

    private inner class SettingsAdapter(private val items: List<SettingsItem>) :
        RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_setting, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val icon: ImageView = itemView.findViewById(R.id.settingIcon)
            private val title: TextView = itemView.findViewById(R.id.settingText)

            fun bind(item: SettingsItem) {
                icon.setImageResource(item.iconRes)
                title.text = item.title
                itemView.setOnClickListener { item.onClick() }
            }
        }
    }
}

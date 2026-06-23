package com.tvlauncher.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tvlauncher.R
import com.tvlauncher.data.AppInfo
import com.tvlauncher.data.AppManager
import com.tvlauncher.data.PrefsManager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var appManager: AppManager
    private val selectedApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsManager = PrefsManager(this)
        appManager = AppManager(this)

        if (!prefsManager.isFirstLaunch()) {
            startActivity(Intent(this, com.tvlauncher.ui.MainActivity::class.java))
            finish()
            return
        }

        showSelectAppsPage()
    }

    private fun showSelectAppsPage() {
        setContentView(R.layout.fragment_onboarding_select)
        val grid = findViewById<RecyclerView>(R.id.appSelectionGrid)
        val nextBtn = findViewById<Button>(R.id.nextButton)

        val apps = appManager.loadApps()
        val adapter = OnboardingAppAdapter(apps, selectedApps) { packageName, selected ->
            if (selected) selectedApps.add(packageName) else selectedApps.remove(packageName)
        }

        grid?.layoutManager = GridLayoutManager(this, 6)
        grid?.adapter = adapter

        nextBtn?.text = getString(R.string.onboarding_done)
        nextBtn?.setOnClickListener {
            val pinnedApps = apps.filter { selectedApps.contains(it.packageName) }
            pinnedApps.forEach { appManager.togglePin(it.packageName) }
            prefsManager.savePinnedOrder(selectedApps.toList())
            prefsManager.setFirstLaunchDone()
            startActivity(Intent(this, com.tvlauncher.ui.MainActivity::class.java))
            finish()
        }
    }

    private class OnboardingAppAdapter(
        private val apps: List<AppInfo>,
        private val selected: Set<String>,
        private val onToggle: (String, Boolean) -> Unit
    ) : RecyclerView.Adapter<OnboardingAppAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(apps[position])
        }

        override fun getItemCount() = apps.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val icon: ImageView = itemView.findViewById(R.id.appIcon)
            private val name: TextView = itemView.findViewById(R.id.appName)
            private val glow: View? = itemView.findViewById(R.id.focusGlow)

            fun bind(app: AppInfo) {
                icon.setImageDrawable(app.icon)
                name.text = app.name
                glow?.alpha = if (selected.contains(app.packageName)) 1f else 0f
                itemView.setOnClickListener {
                    val isSelected = !selected.contains(app.packageName)
                    onToggle(app.packageName, isSelected)
                    glow?.alpha = if (isSelected) 1f else 0f
                }
            }
        }
    }
}


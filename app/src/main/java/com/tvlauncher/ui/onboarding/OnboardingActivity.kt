package com.tvlauncher.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
    private var allApps = listOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsManager = PrefsManager(this)
        appManager = AppManager(this)

        if (!prefsManager.isFirstLaunch()) {
            goToMain()
            return
        }

        allApps = appManager.loadApps()
        showWelcome()
    }

    // ── Step 1: Welcome ──────────────────────────────────────────────

    private fun showWelcome() {
        setContentView(R.layout.fragment_onboarding_welcome)

        val root = findViewById<View>(R.id.welcomeIcon)?.parent as? View
        root?.let { fadeIn(it) }

        findViewById<Button>(R.id.welcomeStartBtn)?.setOnClickListener {
            showSelectApps()
        }
    }

    // ── Step 2: Select pinned apps ───────────────────────────────────

    private fun showSelectApps() {
        setContentView(R.layout.fragment_onboarding_select)

        val grid = findViewById<RecyclerView>(R.id.appSelectionGrid)
        val doneBtn = findViewById<Button>(R.id.doneBtn)
        val skipBtn = findViewById<Button>(R.id.skipBtn)
        val countText = findViewById<TextView>(R.id.selectedCount)

        // Restore previously selected apps
        val savedPinned = prefsManager.getPinnedOrder()
        selectedApps.clear()
        selectedApps.addAll(savedPinned)
        updateCountText(countText)

        val adapter = SelectAppAdapter(allApps, selectedApps) { packageName, isSelected ->
            if (isSelected) selectedApps.add(packageName)
            else selectedApps.remove(packageName)
            updateCountText(countText)
        }

        grid?.layoutManager = GridLayoutManager(this, getColumnCount())
        grid?.adapter = adapter

        // Slide in from right
        val container = (grid?.parent as? View)
        container?.let { slideInFromRight(it) }

        skipBtn?.setOnClickListener {
            selectedApps.clear()
            showReady()
        }

        doneBtn?.setOnClickListener {
            // Save pinned order
            prefsManager.savePinnedOrder(selectedApps.toList())
            // Toggle pins in AppManager
            allApps.forEach { app ->
                val shouldBePinned = selectedApps.contains(app.packageName)
                if (shouldBePinned != app.isPinned) {
                    appManager.togglePin(app.packageName)
                }
            }
            showReady()
        }
    }

    private fun updateCountText(countText: TextView?) {
        countText?.text = getString(R.string.onboarding_selected_count, selectedApps.size)
    }

    private fun getColumnCount(): Int {
        val widthDp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
        return when {
            widthDp >= 960 -> 9
            widthDp >= 720 -> 8
            widthDp >= 540 -> 7
            widthDp >= 400 -> 6
            else -> 5
        }
    }

    // ── Step 3: Ready ────────────────────────────────────────────────

    private fun showReady() {
        setContentView(R.layout.fragment_onboarding_ready)

        val root = findViewById<View>(R.id.goBtn)?.parent as? View
        root?.let { fadeIn(it) }

        findViewById<Button>(R.id.goBtn)?.setOnClickListener {
            prefsManager.setFirstLaunchDone()
            goToMain()
        }
    }

    // ── Navigation ───────────────────────────────────────────────────

    private fun goToMain() {
        startActivity(Intent(this, com.tvlauncher.ui.MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onBackPressed() {
        // Do nothing - user must complete onboarding
    }

    // ── Animations ───────────────────────────────────────────────────

    private fun fadeIn(view: View) {
        val anim = AlphaAnimation(0f, 1f).apply {
            duration = 500
            fillAfter = true
        }
        view.startAnimation(anim)
    }

    private fun slideInFromRight(view: View) {
        val anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        view.startAnimation(anim)
    }

    // ── Adapter ──────────────────────────────────────────────────────

    private class SelectAppAdapter(
        private val apps: List<AppInfo>,
        private val selected: MutableSet<String>,
        private val onToggle: (String, Boolean) -> Unit
    ) : RecyclerView.Adapter<SelectAppAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_small, parent, false)
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
                val isSelected = selected.contains(app.packageName)
                glow?.alpha = if (isSelected) 1f else 0f
                itemView.scaleX = if (isSelected) 1.05f else 1f
                itemView.scaleY = if (isSelected) 1.05f else 1f

                itemView.setOnClickListener {
                    val nowSelected = !selected.contains(app.packageName)
                    onToggle(app.packageName, nowSelected)
                    // Animate selection
                    if (nowSelected) {
                        selected.add(app.packageName)
                        itemView.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start()
                        glow?.animate()?.alpha(1f)?.setDuration(150)?.start()
                    } else {
                        selected.remove(app.packageName)
                        itemView.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                        glow?.animate()?.alpha(0f)?.setDuration(150)?.start()
                    }
                }
            }
        }
    }
}

package com.tvlauncher.ui.home

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tvlauncher.R
import com.tvlauncher.data.AppInfo
import com.tvlauncher.data.PrefsManager
import com.tvlauncher.databinding.FragmentHomeBinding
import com.tvlauncher.ui.MainActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var pinnedAdapter: PinnedAdapter
    private lateinit var appAdapter: AppAdapter
    private lateinit var prefsManager: PrefsManager

    private val allApps = mutableListOf<AppInfo>()
    private val pinnedApps = mutableListOf<AppInfo>()
    private var isInEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsManager = PrefsManager(requireContext())
        setupPinnedRow()
        setupAppGrid()
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        loadApps()
    }

    private fun setupPinnedRow() {
        pinnedAdapter = PinnedAdapter(
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app, view -> showContextMenu(app, view) },
            onFocusChange = { hasFocus ->
                (activity as? MainActivity)?.let {
                    // Dim status bar when apps focused
                }
            }
        )
        binding.pinnedRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = pinnedAdapter
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
    }

    private fun setupAppGrid() {
        appAdapter = AppAdapter(
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app, view -> showContextMenu(app, view) },
            onFocusChange = { hasFocus ->
                (activity as? MainActivity)?.let {
                    // Dim status bar when apps focused
                }
            }
        )
        binding.allAppsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, getColumnCount())
            adapter = appAdapter
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
    }

    private fun getColumnCount(): Int {
        val displayMetrics = resources.displayMetrics
        val widthDp = displayMetrics.widthPixels / displayMetrics.density
        return when {
            widthDp >= 1200 -> 8
            widthDp >= 960 -> 7
            widthDp >= 720 -> 6
            widthDp >= 540 -> 5
            else -> 4
        }
    }

    private fun loadApps() {
        allApps.clear()
        pinnedApps.clear()

        val packageManager = requireContext().packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

        val launcherPackage = requireContext().packageName
        val apps = resolveInfos
            .filter { it.activityInfo.packageName != launcherPackage }
            .map { resolveInfo ->
                AppInfo(
                    name = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    icon = resolveInfo.loadIcon(packageManager)
                )
            }
            .sortedBy { it.name.lowercase() }

        allApps.addAll(apps)

        val pinnedPackageNames = prefsManager.getPinnedOrder()
        pinnedApps.addAll(apps.filter { it.packageName in pinnedPackageNames })

        val unpinnedApps = apps.filter { it.packageName !in pinnedPackageNames }

        pinnedAdapter.submitList(pinnedApps.toList())
        appAdapter.submitList(unpinnedApps.toList())

        binding.pinnedRecyclerView.visibility = if (pinnedApps.isEmpty()) View.GONE else View.VISIBLE
        binding.pinnedLabel.visibility = if (pinnedApps.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun launchApp(appInfo: AppInfo) {
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent != null) {
                startActivity(intent)
            } else {
                Toast.makeText(context, R.string.app_launch_failed, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, R.string.app_launch_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showContextMenu(appInfo: AppInfo, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        val pinnedOrder = prefsManager.getPinnedOrder()
        val isPinned = pinnedOrder.contains(appInfo.packageName)

        if (isPinned) {
            popup.menu.add(0, 1, 0, R.string.unpin_app)
        } else {
            popup.menu.add(0, 0, 0, R.string.pin_app)
        }
        popup.menu.add(0, 2, 1, R.string.app_info)
        popup.menu.add(0, 3, 2, R.string.uninstall_app)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> { // Pin
                    val order = prefsManager.getPinnedOrder().toMutableList()
                    if (!order.contains(appInfo.packageName)) {
                        order.add(appInfo.packageName)
                    }
                    prefsManager.savePinnedOrder(order)
                    loadApps()
                    true
                }
                1 -> { // Unpin
                    val order = prefsManager.getPinnedOrder().toMutableList()
                    order.remove(appInfo.packageName)
                    prefsManager.savePinnedOrder(order)
                    loadApps()
                    true
                }
                2 -> { // App info
                    try {
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${appInfo.packageName}")
                        })
                    } catch (_: Exception) {}
                    true
                }
                3 -> { // Uninstall
                    try {
                        startActivity(Intent(Intent.ACTION_DELETE).apply {
                            data = Uri.parse("package:${appInfo.packageName}")
                        })
                    } catch (_: Exception) {}
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    fun toggleEditMode() {
        isInEditMode = !isInEditMode
        appAdapter.setEditMode(isInEditMode)
        pinnedAdapter.setEditMode(isInEditMode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }
}

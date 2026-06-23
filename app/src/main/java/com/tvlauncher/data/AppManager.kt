package com.tvlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import com.tvlauncher.util.Constants

/**
 * Manages the list of installed applications, pinning, and ordering.
 */
class AppManager(private val context: Context) {

    private val prefsManager = PrefsManager(context)
    private val packageManager: PackageManager = context.packageManager

    @Volatile
    private var allApps: MutableList<AppInfo> = mutableListOf()

    /**
     * Loads all launchable apps from the device, filtering out this launcher itself.
     * Restores pinned state and order from preferences.
     */
    fun loadApps(): List<AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        val pinnedOrder = prefsManager.getPinnedOrder().toMutableList()
        val pinnedSet = pinnedOrder.toHashSet()

        val myPackage = context.packageName

        val apps = resolveInfos
            .filter { it.activityInfo.packageName != myPackage }
            .distinctBy { it.activityInfo.packageName }
            .mapIndexed { index, resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(packageManager).toString()
                val icon = loadIconSafely(resolveInfo)
                val isPinned = pinnedSet.contains(pkg)
                val order = if (isPinned) {
                    pinnedOrder.indexOf(pkg).coerceAtLeast(0)
                } else {
                    pinnedOrder.size + index
                }
                AppInfo(
                    name = label,
                    packageName = pkg,
                    icon = icon,
                    isPinned = isPinned,
                    order = order
                )
            }
            .sortedWith(compareByDescending<AppInfo> { it.isPinned }.thenBy { it.order })
            .toMutableList()

        allApps = apps
        return apps.toList()
    }

    /**
     * Returns only pinned apps in their saved order.
     */
    fun getPinnedApps(): List<AppInfo> {
        return allApps.filter { it.isPinned }.sortedBy { it.order }
    }

    /**
     * Returns all loaded apps.
     */
    fun getAllApps(): List<AppInfo> {
        return allApps.toList()
    }

    /**
     * Toggles the pinned state of an app by package name.
     * Updates the saved pinned order accordingly.
     */
    fun togglePin(packageName: String) {
        val index = allApps.indexOfFirst { it.packageName == packageName }
        if (index < 0) return

        val app = allApps[index]
        val newPinned = !app.isPinned
        allApps[index] = app.copy(isPinned = newPinned)

        val pinnedOrder = prefsManager.getPinnedOrder().toMutableList()
        if (newPinned) {
            if (!pinnedOrder.contains(packageName)) {
                pinnedOrder.add(packageName)
            }
        } else {
            pinnedOrder.remove(packageName)
        }
        prefsManager.savePinnedOrder(pinnedOrder)

        // Update order values
        val pinnedSet = pinnedOrder.toHashSet()
        allApps.forEachIndexed { i, appInfo ->
            if (appInfo.isPinned) {
                val newOrder = pinnedOrder.indexOf(appInfo.packageName).coerceAtLeast(0)
                allApps[i] = appInfo.copy(order = newOrder)
            }
        }
        allApps.sortWith(compareByDescending<AppInfo> { it.isPinned }.thenBy { it.order })
    }

    /**
     * Reorders an app from one position to another within the pinned list.
     */
    fun reorder(from: Int, to: Int) {
        val pinnedApps = getPinnedApps().toMutableList()
        if (from < 0 || from >= pinnedApps.size || to < 0 || to >= pinnedApps.size) return
        if (from == to) return

        val app = pinnedApps.removeAt(from)
        pinnedApps.add(to, app)

        // Update internal list
        val pinnedOrder = pinnedApps.map { it.packageName }
        prefsManager.savePinnedOrder(pinnedOrder)

        // Rebuild order values
        val pinnedSet = pinnedOrder.toHashSet()
        allApps.forEachIndexed { i, appInfo ->
            val newOrder = if (pinnedSet.contains(appInfo.packageName)) {
                pinnedOrder.indexOf(appInfo.packageName)
            } else {
                pinnedOrder.size + i
            }
            allApps[i] = appInfo.copy(order = newOrder)
        }
        allApps.sortWith(compareByDescending<AppInfo> { it.isPinned }.thenBy { it.order })
    }

    /**
     * Launches an app by package name.
     */
    fun launchApp(packageName: String): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
            prefsManager.saveLastSelectedApp(packageName)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Safely loads an app icon, handling potential exceptions.
     */
    private fun loadIconSafely(resolveInfo: ResolveInfo): Drawable? {
        return try {
            resolveInfo.loadIcon(packageManager)
        } catch (e: Exception) {
            null
        }
    }
}

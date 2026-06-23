package com.tvlauncher.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tvlauncher.util.Constants

/**
 * SharedPreferences wrapper for persisting launcher settings and state.
 */
class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // ---- First Launch ----

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(Constants.KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchDone() {
        prefs.edit().putBoolean(Constants.KEY_FIRST_LAUNCH, false).apply()
    }

    // ---- Pinned App Order ----

    fun getPinnedOrder(): List<String> {
        val json = prefs.getString(Constants.KEY_PINNED_ORDER, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePinnedOrder(order: List<String>) {
        val json = gson.toJson(order)
        prefs.edit().putString(Constants.KEY_PINNED_ORDER, json).apply()
    }

    // ---- Wallpaper Path ----

    fun getWallpaperPath(): String? {
        return prefs.getString(Constants.KEY_WALLPAPER_PATH, null)
    }

    fun saveWallpaperPath(path: String?) {
        prefs.edit().putString(Constants.KEY_WALLPAPER_PATH, path).apply()
    }

    // ---- Last Selected App ----

    fun getLastSelectedApp(): String? {
        return prefs.getString(Constants.KEY_LAST_SELECTED_APP, null)
    }

    fun saveLastSelectedApp(packageName: String?) {
        prefs.edit().putString(Constants.KEY_LAST_SELECTED_APP, packageName).apply()
    }

    // ---- Animation Level ----

    fun getAnimationLevel(): Int {
        return prefs.getInt(Constants.KEY_ANIMATION_LEVEL, Constants.ANIM_LEVEL_HIGH)
    }

    fun setAnimationLevel(level: Int) {
        prefs.edit().putInt(Constants.KEY_ANIMATION_LEVEL, level).apply()
    }

    // ---- Default Launcher Prompted ----

    fun isDefaultLauncherPrompted(): Boolean {
        return prefs.getBoolean(Constants.KEY_DEFAULT_LAUNCHER_PROMPTED, false)
    }

    fun setDefaultLauncherPrompted() {
        prefs.edit().putBoolean(Constants.KEY_DEFAULT_LAUNCHER_PROMPTED, true).apply()
    }

    // ---- Weather Cache ----

    fun getWeatherCache(): String? {
        return prefs.getString(Constants.KEY_WEATHER_CACHE, null)
    }

    fun saveWeatherCache(json: String) {
        prefs.edit()
            .putString(Constants.KEY_WEATHER_CACHE, json)
            .putLong(Constants.KEY_WEATHER_CACHE_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getWeatherCacheTime(): Long {
        return prefs.getLong(Constants.KEY_WEATHER_CACHE_TIME, 0L)
    }

    fun isWeatherCacheValid(): Boolean {
        val cacheTime = getWeatherCacheTime()
        return cacheTime > 0 && (System.currentTimeMillis() - cacheTime) < Constants.WEATHER_CACHE_MS
    }
}

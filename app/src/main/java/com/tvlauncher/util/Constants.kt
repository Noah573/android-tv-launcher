package com.tvlauncher.util

object Constants {

    // Animation levels
    const val ANIM_LEVEL_HIGH = 0
    const val ANIM_LEVEL_MEDIUM = 1
    const val ANIM_LEVEL_LOW = 2

    // Weather
    const val DEFAULT_WEATHER_CITY = "Beijing"
    const val WEATHER_CACHE_MS = 30 * 60 * 1000L // 30 minutes
    const val WEATHER_API_URL =
        "https://api.open-meteo.com/v1/forecast?current=temperature_2m&latitude=39.9&longitude=116.4"

    // Screensaver
    const val SCREENSAVER_TIMEOUT_MS = 3 * 60 * 1000L // 3 minutes

    // SharedPreferences keys
    const val PREFS_NAME = "tv_launcher_prefs"
    const val KEY_FIRST_LAUNCH = "first_launch"
    const val KEY_PINNED_ORDER = "pinned_order"
    const val KEY_WALLPAPER_PATH = "wallpaper_path"
    const val KEY_LAST_SELECTED_APP = "last_selected_app"
    const val KEY_ANIMATION_LEVEL = "animation_level"
    const val KEY_DEFAULT_LAUNCHER_PROMPTED = "default_launcher_prompted"
    const val KEY_WEATHER_CACHE = "weather_cache"
    const val KEY_WEATHER_CACHE_TIME = "weather_cache_time"

    // Focus engine
    const val DEFAULT_GRID_ROWS = 3
    const val DEFAULT_GRID_COLS = 6

    // Animation durations (ms)
    const val FOCUS_DURATION_HIGH = 300L
    const val FOCUS_DURATION_MEDIUM = 200L
    const val FOCUS_DURATION_LOW = 100L

    const val TRANSITION_DURATION_HIGH = 400L
    const val TRANSITION_DURATION_MEDIUM = 250L
    const val TRANSITION_DURATION_LOW = 150L

    // Focus scale
    const val FOCUS_SCALE_HIGH = 1.08f
    const val FOCUS_SCALE_MEDIUM = 1.05f
    const val FOCUS_SCALE_LOW = 1.0f

    // Blur radius
    const val BLUR_RADIUS_HIGH = 25f
    const val BLUR_RADIUS_MEDIUM = 12f
    const val BLUR_RADIUS_LOW = 0f

    // Spring bounce
    const val SPRING_BOUNCE_HIGH = true
    const val SPRING_BOUNCE_MEDIUM = false
    const val SPRING_BOUNCE_LOW = false

    // Performance thresholds
    const val RAM_THRESHOLD_HIGH_MB = 3072  // 3 GB
    const val RAM_THRESHOLD_MEDIUM_MB = 1536 // 1.5 GB
    const val CPU_CORES_HIGH = 6
    const val CPU_CORES_MEDIUM = 4
}

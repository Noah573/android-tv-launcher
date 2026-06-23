package com.tvlauncher.engine

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.tvlauncher.util.Constants

/**
 * Animation tiering system that adapts visual effects based on device performance.
 */
class AnimationEngine {

    companion object {
        /**
         * Detects the device performance level for animation tiering.
         * Returns one of Constants.ANIM_LEVEL_HIGH, MEDIUM, or LOW.
         */
        fun detectPerformance(context: Context): Int {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                    ?: return Constants.ANIM_LEVEL_MEDIUM

            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val totalRamMb = memInfo.totalMem / (1024 * 1024)

            val cpuCores = Runtime.getRuntime().availableProcessors()
            val sdkVersion = Build.VERSION.SDK_INT

            var score = 0

            // RAM scoring
            when {
                totalRamMb >= Constants.RAM_THRESHOLD_HIGH_MB -> score += 3
                totalRamMb >= Constants.RAM_THRESHOLD_MEDIUM_MB -> score += 2
                else -> score += 1
            }

            // CPU scoring
            when {
                cpuCores >= Constants.CPU_CORES_HIGH -> score += 3
                cpuCores >= Constants.CPU_CORES_MEDIUM -> score += 2
                else -> score += 1
            }

            // SDK version scoring (newer API = likely newer hardware)
            when {
                sdkVersion >= Build.VERSION_CODES.S -> score += 3    // API 31+
                sdkVersion >= Build.VERSION_CODES.Q -> score += 2    // API 29+
                else -> score += 1
            }

            return when {
                score >= 7 -> Constants.ANIM_LEVEL_HIGH
                score >= 4 -> Constants.ANIM_LEVEL_MEDIUM
                else -> Constants.ANIM_LEVEL_LOW
            }
        }
    }

    /**
     * Returns the focus scale factor for the given animation level.
     * Higher levels produce a more pronounced zoom effect.
     */
    fun getFocusScale(level: Int): Float {
        return when (level) {
            Constants.ANIM_LEVEL_HIGH -> Constants.FOCUS_SCALE_HIGH
            Constants.ANIM_LEVEL_MEDIUM -> Constants.FOCUS_SCALE_MEDIUM
            else -> Constants.FOCUS_SCALE_LOW
        }
    }

    /**
     * Returns the focus animation duration in milliseconds.
     */
    fun getFocusDuration(level: Int): Long {
        return when (level) {
            Constants.ANIM_LEVEL_HIGH -> Constants.FOCUS_DURATION_HIGH
            Constants.ANIM_LEVEL_MEDIUM -> Constants.FOCUS_DURATION_MEDIUM
            else -> Constants.FOCUS_DURATION_LOW
        }
    }

    /**
     * Returns the blur radius for background effects.
     * LOW performance returns 0 (no blur).
     */
    fun getBlurRadius(level: Int): Float {
        return when (level) {
            Constants.ANIM_LEVEL_HIGH -> Constants.BLUR_RADIUS_HIGH
            Constants.ANIM_LEVEL_MEDIUM -> Constants.BLUR_RADIUS_MEDIUM
            else -> Constants.BLUR_RADIUS_LOW
        }
    }

    /**
     * Returns whether spring bounce animations should be used.
     */
    fun shouldUseSpringBounce(level: Int): Boolean {
        return when (level) {
            Constants.ANIM_LEVEL_HIGH -> Constants.SPRING_BOUNCE_HIGH
            Constants.ANIM_LEVEL_MEDIUM -> Constants.SPRING_BOUNCE_MEDIUM
            else -> Constants.SPRING_BOUNCE_LOW
        }
    }

    /**
     * Returns the screen/activity transition duration in milliseconds.
     */
    fun getTransitionDuration(level: Int): Long {
        return when (level) {
            Constants.ANIM_LEVEL_HIGH -> Constants.TRANSITION_DURATION_HIGH
            Constants.ANIM_LEVEL_MEDIUM -> Constants.TRANSITION_DURATION_MEDIUM
            else -> Constants.TRANSITION_DURATION_LOW
        }
    }
}

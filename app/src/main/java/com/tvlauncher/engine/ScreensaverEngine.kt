package com.tvlauncher.engine

import android.os.Handler
import android.os.Looper
import com.tvlauncher.util.Constants

/**
 * Screensaver engine that detects user idle time and triggers a screensaver callback.
 * The screensaver displays a clock with an aurora gradient animation.
 */
class ScreensaverEngine {

    /**
     * Callback interface for screensaver state changes.
     */
    interface ScreensaverCallback {
        /** Called when the screensaver should be shown after idle timeout. */
        fun onScreensaverStart()
        /** Called when the screensaver should be dismissed due to user input. */
        fun onScreensaverStop()
    }

    private val handler = Handler(Looper.getMainLooper())
    private var callback: ScreensaverCallback? = null
    private var isScreensaverActive = false
    private var timeoutMs = Constants.SCREENSAVER_TIMEOUT_MS

    private val idleRunnable = Runnable {
        if (!isScreensaverActive) {
            isScreensaverActive = true
            callback?.onScreensaverStart()
        }
    }

    /**
     * Sets the screensaver callback.
     */
    fun setCallback(callback: ScreensaverCallback?) {
        this.callback = callback
    }

    /**
     * Sets a custom idle timeout. Default is 3 minutes.
     */
    fun setTimeout(ms: Long) {
        timeoutMs = ms.coerceAtLeast(5000L) // Minimum 5 seconds
    }

    /**
     * Starts the idle timer. Call this when the launcher activity starts.
     */
    fun start() {
        handler.removeCallbacks(idleRunnable)
        handler.postDelayed(idleRunnable, timeoutMs)
    }

    /**
     * Stops the idle timer entirely. Call this when the launcher is paused/destroyed.
     */
    fun stop() {
        handler.removeCallbacks(idleRunnable)
        dismissScreensaver()
    }

    /**
     * Resets the idle timer. Call this on every user input event
     * (touch, key press, D-pad navigation, etc.).
     */
    fun resetTimer() {
        handler.removeCallbacks(idleRunnable)

        if (isScreensaverActive) {
            dismissScreensaver()
        }

        handler.postDelayed(idleRunnable, timeoutMs)
    }

    /**
     * Returns whether the screensaver is currently active.
     */
    fun isActive(): Boolean = isScreensaverActive

    /**
     * Manually triggers the screensaver regardless of idle time.
     */
    fun triggerNow() {
        handler.removeCallbacks(idleRunnable)
        if (!isScreensaverActive) {
            isScreensaverActive = true
            callback?.onScreensaverStart()
        }
    }

    private fun dismissScreensaver() {
        if (isScreensaverActive) {
            isScreensaverActive = false
            callback?.onScreensaverStop()
        }
    }
}

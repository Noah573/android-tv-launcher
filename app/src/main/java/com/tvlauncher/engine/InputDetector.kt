package com.tvlauncher.engine

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Detects and tracks the current input mode (remote/D-pad vs touch).
 * Automatically switches modes based on received input events.
 */
class InputDetector {

    /**
     * The detected input mode.
     */
    enum class InputMode {
        /** D-pad / remote control navigation */
        REMOTE,
        /** Touch / pointer navigation */
        TOUCH
    }

    private val _currentMode = MutableLiveData(InputMode.REMOTE)
    private var onModeChangedListener: ((InputMode) -> Unit)? = null

    /**
     * Observable current input mode. Defaults to REMOTE.
     */
    val currentMode: LiveData<InputMode> = _currentMode

    /**
     * Returns the current input mode value synchronously.
     */
    fun getMode(): InputMode = _currentMode.value ?: InputMode.REMOTE

    /**
     * Sets a listener for mode changes (alternative to LiveData).
     */
    fun setOnModeChangedListener(listener: ((InputMode) -> Unit)?) {
        onModeChangedListener = listener
    }

    /**
     * Processes a key event. If it's a D-pad key, switches to REMOTE mode.
     * Call this from Activity.dispatchKeyEvent() or View.onKeyDown().
     *
     * @return true if this event was consumed as a mode switch signal.
     */
    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isDpadKey(keyCode)) {
            switchMode(InputMode.REMOTE)
        }
        return false // Don't consume; let the event propagate
    }

    /**
     * Processes a touch/motion event. If it's an ACTION_DOWN, switches to TOUCH mode.
     * Call this from Activity.dispatchTouchEvent() or a View's OnTouchListener.
     *
     * @return true if this event was consumed as a mode switch signal.
     */
    fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        if (event.action == MotionEvent.ACTION_DOWN) {
            switchMode(InputMode.TOUCH)
        }
        return false // Don't consume; let the event propagate
    }

    /**
     * Forces a specific mode. Useful for testing or programmatic control.
     */
    fun setMode(mode: InputMode) {
        switchMode(mode)
    }

    /**
     * Returns true if currently in REMOTE mode (focus indicators should be visible).
     */
    fun isRemoteMode(): Boolean = getMode() == InputMode.REMOTE

    /**
     * Returns true if currently in TOUCH mode (focus indicators should be hidden).
     */
    fun isTouchMode(): Boolean = getMode() == InputMode.TOUCH

    private fun switchMode(newMode: InputMode) {
        val oldMode = _currentMode.value
        if (oldMode != newMode) {
            _currentMode.postValue(newMode)
            onModeChangedListener?.invoke(newMode)
        }
    }

    private fun isDpadKey(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME -> true
            else -> false
        }
    }
}

package com.tvlauncher.engine

import android.view.View

/**
 * Manages focus navigation for D-pad/remote control input on Android TV.
 * Tracks a grid-based focus position and resolves it to actual views.
 */
class FocusEngine {

    /**
     * Represents a position in the focus grid.
     */
    data class FocusPosition(val row: Int, val col: Int)

    /**
     * Interface for receiving focus change events.
     */
    interface FocusCallback {
        /**
         * Called when focus moves to a new position.
         * @param position The new focus position.
         * @param view The View at that position, or null if no view is mapped.
         */
        fun onFocusChanged(position: FocusPosition, view: View?)
    }

    private var currentRow = 0
    private var currentCol = 0
    private var maxRows = 3
    private var maxCols = 6

    /**
     * Map of (row, col) -> View for resolving focus positions to views.
     */
    private val focusableViews = mutableMapOf<Pair<Int, Int>, View>()

    private var callback: FocusCallback? = null

    /**
     * Sets the grid dimensions for focus navigation.
     */
    fun setFocusGrid(rows: Int, cols: Int) {
        maxRows = rows.coerceAtLeast(1)
        maxCols = cols.coerceAtLeast(1)
        // Clamp current position to new bounds
        currentRow = currentRow.coerceIn(0, maxRows - 1)
        currentCol = currentCol.coerceIn(0, maxCols - 1)
    }

    /**
     * Registers a view at a specific grid position.
     */
    fun registerView(row: Int, col: Int, view: View) {
        focusableViews[Pair(row, col)] = view
    }

    /**
     * Removes a view registration.
     */
    fun unregisterView(row: Int, col: Int) {
        focusableViews.remove(Pair(row, col))
    }

    /**
     * Clears all registered views.
     */
    fun clearViews() {
        focusableViews.clear()
    }

    /**
     * Sets the callback for focus change events.
     */
    fun setOnFocusChanged(callback: FocusCallback?) {
        this.callback = callback
    }

    /**
     * Sets the callback using a lambda.
     */
    fun setOnFocusChanged(listener: (FocusPosition, View?) -> Unit) {
        this.callback = object : FocusCallback {
            override fun onFocusChanged(position: FocusPosition, view: View?) {
                listener(position, view)
            }
        }
    }

    /**
     * Moves focus up. At the top row, requests focus on the status bar area
     * (returns a special signal via callback with view = null and row = -1).
     */
    fun moveUp() {
        if (currentRow <= 0) {
            // At top row: signal to move to status bar / system UI
            val position = FocusPosition(-1, currentCol)
            callback?.onFocusChanged(position, null)
            return
        }
        currentRow--
        notifyFocusChanged()
    }

    /**
     * Moves focus down. At the bottom row, does nothing.
     */
    fun moveDown() {
        if (currentRow >= maxRows - 1) {
            // At bottom row, do nothing
            return
        }
        currentRow++
        notifyFocusChanged()
    }

    /**
     * Moves focus left. Wraps to the last column if at the first column.
     */
    fun moveLeft() {
        currentCol = if (currentCol <= 0) maxCols - 1 else currentCol - 1
        notifyFocusChanged()
    }

    /**
     * Moves focus right. Wraps to the first column if at the last column.
     */
    fun moveRight() {
        currentCol = if (currentCol >= maxCols - 1) 0 else currentCol + 1
        notifyFocusChanged()
    }

    /**
     * Directly sets the focus position.
     */
    fun setFocusPosition(row: Int, col: Int) {
        currentRow = row.coerceIn(0, maxRows - 1)
        currentCol = col.coerceIn(0, maxCols - 1)
        notifyFocusChanged()
    }

    /**
     * Returns the current focus position.
     */
    fun getCurrentPosition(): FocusPosition {
        return FocusPosition(currentRow, currentCol)
    }

    /**
     * Returns the currently focused view, or null if no view is mapped at the current position.
     */
    fun getCurrentFocusedView(): View? {
        return focusableViews[Pair(currentRow, currentCol)]
    }

    /**
     * Returns the number of registered focusable views.
     */
    fun getRegisteredViewCount(): Int = focusableViews.size

    /**
     * Attempts to find the next available view in the grid if the current position
     * has no registered view. Searches right then down.
     */
    fun findNearestView(): View? {
        // Try current position first
        getCurrentFocusedView()?.let { return it }

        // Spiral search from current position
        for (radius in 1 until maxOf(maxRows, maxCols)) {
            for (dr in -radius..radius) {
                for (dc in -radius..radius) {
                    val r = currentRow + dr
                    val c = currentCol + dc
                    if (r in 0 until maxRows && c in 0 until maxCols) {
                        focusableViews[Pair(r, c)]?.let { view ->
                            currentRow = r
                            currentCol = c
                            return view
                        }
                    }
                }
            }
        }
        return null
    }

    private fun notifyFocusChanged() {
        val position = FocusPosition(currentRow, currentCol)
        val view = focusableViews[Pair(currentRow, currentCol)]
        callback?.onFocusChanged(position, view)
    }
}

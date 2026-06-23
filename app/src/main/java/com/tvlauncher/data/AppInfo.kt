package com.tvlauncher.data

import android.graphics.drawable.Drawable

/**
 * Represents an installed application with its display information.
 */
data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val isPinned: Boolean = false,
    val order: Int = 0
)

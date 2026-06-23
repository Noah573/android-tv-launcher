package com.tvlauncher.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.provider.MediaStore
import com.tvlauncher.util.Constants

/**
 * Represents a built-in wallpaper with a display name and drawable.
 */
data class BuiltInWallpaper(
    val name: String,
    val drawable: GradientDrawable
)

/**
 * Manages wallpapers: built-in gradients, custom gallery images, and persistence.
 */
class WallpaperManager(private val context: Context) {

    companion object {
        const val REQUEST_PICK_WALLPAPER = 1001
    }

    private val prefsManager = PrefsManager(context)

    /**
     * Returns the current wallpaper. If a custom path is saved, returns null
     * (caller should load from path). If no custom wallpaper, returns null
     * indicating built-in default should be used.
     */
    fun getCurrentWallpaper(): String? {
        return prefsManager.getWallpaperPath()
    }

    /**
     * Saves the selected wallpaper path. Pass null to reset to default.
     */
    fun setWallpaper(path: String?) {
        prefsManager.saveWallpaperPath(path)
    }

    /**
     * Returns a list of built-in dark gradient wallpapers.
     */
    fun getBuiltInWallpapers(): List<BuiltInWallpaper> {
        return listOf(
            BuiltInWallpaper(
                "Midnight Blue",
                createGradient(
                    Color.parseColor("#0D1B2A"),
                    Color.parseColor("#1B263B"),
                    Color.parseColor("#415A77")
                )
            ),
            BuiltInWallpaper(
                "Deep Purple",
                createGradient(
                    Color.parseColor("#1A002E"),
                    Color.parseColor("#2D1B69"),
                    Color.parseColor("#5B2C6F")
                )
            ),
            BuiltInWallpaper(
                "Dark Emerald",
                createGradient(
                    Color.parseColor("#0A1F0A"),
                    Color.parseColor("#1B4332"),
                    Color.parseColor("#2D6A4F")
                )
            ),
            BuiltInWallpaper(
                "Charcoal",
                createGradient(
                    Color.parseColor("#1A1A2E"),
                    Color.parseColor("#16213E"),
                    Color.parseColor("#0F3460")
                )
            ),
            BuiltInWallpaper(
                "Sunset Dusk",
                createGradient(
                    Color.parseColor("#1A1A2E"),
                    Color.parseColor("#3D1C56"),
                    Color.parseColor("#6B2FA0")
                )
            ),
            BuiltInWallpaper(
                "Ocean Dark",
                createGradient(
                    Color.parseColor("#03071E"),
                    Color.parseColor("#0A1128"),
                    Color.parseColor("#1A237E")
                )
            )
        )
    }

    /**
     * Launches a gallery picker for the user to select a custom wallpaper image.
     */
    fun pickFromGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        activity.startActivityForResult(intent, REQUEST_PICK_WALLPAPER)
    }

    /**
     * Handles the gallery result. Returns the selected image URI as a string, or null.
     */
    fun handleGalleryResult(requestCode: Int, resultCode: Int, data: Intent?): String? {
        if (requestCode == REQUEST_PICK_WALLPAPER && resultCode == Activity.RESULT_OK) {
            val uri = data?.data?.toString() ?: return null
            setWallpaper(uri)
            return uri
        }
        return null
    }

    private fun createGradient(
        color1: Int,
        color2: Int,
        color3: Int
    ): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(color1, color2, color3)
        ).apply {
            gradientType = GradientDrawable.LINEAR_GRADIENT
            // Will fill the entire view
        }
    }
}

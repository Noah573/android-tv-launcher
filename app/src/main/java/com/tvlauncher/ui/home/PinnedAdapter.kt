package com.tvlauncher.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tvlauncher.R
import com.tvlauncher.data.AppInfo

class PinnedAdapter(
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo, View) -> Unit,
    private val onFocusChange: (Boolean) -> Unit
) : ListAdapter<AppInfo, PinnedAdapter.PinnedViewHolder>(PinnedDiffCallback()) {

    private var isEditMode = false
    private val focusInterpolator = PathInterpolator(0.2f, 0.8f, 0.2f, 1f)

    fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinnedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pinned_app, parent, false)
        return PinnedViewHolder(view)
    }

    override fun onBindViewHolder(holder: PinnedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PinnedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.pinnedAppIcon)
        private val nameView: TextView = itemView.findViewById(R.id.pinnedAppName)
        private val focusBackground: View = itemView.findViewById(R.id.focusBackground)

        fun bind(appInfo: AppInfo) {
            iconView.setImageDrawable(appInfo.icon)
            nameView.text = appInfo.appName

            // Reset state
            itemView.scaleX = 1f
            itemView.scaleY = 1f
            itemView.translationY = 0f
            focusBackground.alpha = 0f

            if (isEditMode) {
                startShakeAnimation()
            } else {
                itemView.animate().cancel()
                itemView.rotation = 0f
            }

            itemView.setOnClickListener { onAppClick(appInfo) }
            itemView.setOnLongClickListener { view ->
                onAppLongClick(appInfo, view)
                true
            }

            itemView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    onFocusChange(true)
                    animateFocusGained()
                } else {
                    onFocusChange(false)
                    animateFocusLost()
                }
            }

            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true
        }

        private fun animateFocusGained() {
            val scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", 1f, 1.12f)
            val scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", 1f, 1.12f)
            val translateY = ObjectAnimator.ofFloat(itemView, "translationY", 0f, -8f)
            val glowAlpha = ObjectAnimator.ofFloat(focusBackground, "alpha", 0f, 1f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, translateY, glowAlpha)
                duration = 280
                interpolator = focusInterpolator
                start()
            }
        }

        private fun animateFocusLost() {
            val scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", 1.12f, 1f)
            val scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", 1.12f, 1f)
            val translateY = ObjectAnimator.ofFloat(itemView, "translationY", -8f, 0f)
            val glowAlpha = ObjectAnimator.ofFloat(focusBackground, "alpha", 1f, 0f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, translateY, glowAlpha)
                duration = 200
                interpolator = focusInterpolator
                start()
            }
        }

        private fun startShakeAnimation() {
            val shake = ObjectAnimator.ofFloat(itemView, "rotation", -2f, 2f).apply {
                duration = 150
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }
            shake.start()
        }
    }

    class PinnedDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}

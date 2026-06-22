package com.tvlauncher

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class FavoriteAppAdapter(
    private val onItemClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, FavoriteAppAdapter.ViewHolder>(AppDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_app, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.favoriteAppIcon)
        private val nameView: TextView = itemView.findViewById(R.id.favoriteAppName)
        private val focusBg: View = itemView.findViewById(R.id.focusBackground)
        
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            // Apple TV风格焦点动画
            itemView.setOnFocusChangeListener { view, hasFocus ->
                animateFocus(view, hasFocus)
            }
        }
        
        fun bind(appInfo: AppInfo) {
            iconView.setImageDrawable(appInfo.icon)
            nameView.text = appInfo.name
        }
        
        private fun animateFocus(view: View, hasFocus: Boolean) {
            val scaleX = if (hasFocus) 1.15f else 1.0f
            val scaleY = if (hasFocus) 1.15f else 1.0f
            val translateY = if (hasFocus) -8f else 0f
            val bgAlpha = if (hasFocus) 1f else 0f
            
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, "scaleX", scaleX),
                    ObjectAnimator.ofFloat(view, "scaleY", scaleY),
                    ObjectAnimator.ofFloat(view, "translationY", translateY),
                    ObjectAnimator.ofFloat(focusBg, "alpha", bgAlpha)
                )
                duration = 250
                interpolator = DecelerateInterpolator()
                start()
            }
            
            // 名称颜色变化
            nameView.setTextColor(
                view.context.getColor(
                    if (hasFocus) R.color.text_primary else R.color.text_secondary
                )
            )
        }
    }
    
    class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }
        
        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.name == newItem.name
        }
    }
}

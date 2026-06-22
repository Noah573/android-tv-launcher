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

class AppListAdapter(
    private val onItemClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.ViewHolder>(AppDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.appIcon)
        private val nameView: TextView = itemView.findViewById(R.id.appName)
        
        init {
            // 点击事件
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            // 焦点动画（轻量级）
            itemView.setOnFocusChangeListener { view, hasFocus ->
                animateFocus(view, hasFocus)
            }
        }
        
        fun bind(appInfo: AppInfo) {
            iconView.setImageDrawable(appInfo.icon)
            nameView.text = appInfo.name
        }
        
        private fun animateFocus(view: View, hasFocus: Boolean) {
            val scaleX = if (hasFocus) 1.1f else 1.0f
            val scaleY = if (hasFocus) 1.1f else 1.0f
            val elevation = if (hasFocus) 12f else 0f
            
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, "scaleX", scaleX),
                    ObjectAnimator.ofFloat(view, "scaleY", scaleY),
                    ObjectAnimator.ofFloat(view, "elevation", elevation)
                )
                duration = 200
                interpolator = DecelerateInterpolator()
                start()
            }
            
            // 显示/隐藏名称
            view.findViewById<TextView>(R.id.appName).alpha = if (hasFocus) 1f else 0.8f
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

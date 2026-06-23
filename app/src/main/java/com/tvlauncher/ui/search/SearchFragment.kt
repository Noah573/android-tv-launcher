package com.tvlauncher.ui.search

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tvlauncher.R
import com.tvlauncher.data.AppInfo
import com.tvlauncher.ui.MainActivity
import java.util.Locale

class SearchFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var searchResults: RecyclerView
    private lateinit var noResults: TextView
    private lateinit var closeBtn: ImageView
    private val adapter = SearchResultAdapter { appInfo ->
        val intent = requireContext().packageManager.getLaunchIntentForPackage(appInfo.packageName)
        intent?.let { startActivity(it) }
    }
    private var allApps = listOf<AppInfo>()

    companion object {
        fun newInstance() = SearchFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.searchInput)
        searchResults = view.findViewById(R.id.searchResults)
        noResults = view.findViewById(R.id.noResults)
        closeBtn = view.findViewById(R.id.closeSearch)

        searchResults.layoutManager = GridLayoutManager(requireContext(), 6)
        searchResults.adapter = adapter

        loadAllApps()
        setupSearch()
        setupClose()

        searchInput.requestFocus()
        searchInput.postDelayed({
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun loadAllApps() {
        val pm = requireContext().packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        allApps = pm.queryIntentActivities(mainIntent, 0)
            .filter { it.activityInfo.packageName != requireContext().packageName }
            .map {
                AppInfo(
                    name = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(pm),
                    isPinned = false,
                    order = 0
                )
            }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString() ?: "")
            }
        })

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
                true
            } else false
        }
    }

    private fun setupClose() {
        closeBtn.setOnClickListener {
            (activity as? MainActivity)?.closeOverlay()
        }
    }

    private fun filterApps(query: String) {
        if (query.isBlank()) {
            adapter.submitList(emptyList())
            noResults.visibility = View.GONE
            return
        }
        val filtered = allApps.filter {
            it.name.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
            it.packageName.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
        }
        adapter.submitList(filtered)
        noResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private class SearchResultAdapter(
        private val onItemClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

        private var items = listOf<AppInfo>()

        fun submitList(newItems: List<AppInfo>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val icon: ImageView = itemView.findViewById(R.id.appIcon)
            private val name: TextView = itemView.findViewById(R.id.appName)

            fun bind(appInfo: AppInfo) {
                icon.setImageDrawable(appInfo.icon)
                name.text = appInfo.name
                itemView.setOnClickListener { onItemClick(appInfo) }
            }
        }
    }
}

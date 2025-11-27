package com.example.cinephile.ui.watchlist

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.data.local.UserListEntity

class WatchlistManagerAdapter(
    private val onListClick: (UserListEntity) -> Unit,
    private val onSetCurrent: (UserListEntity) -> Unit,
    private val onMenuClick: (UserListEntity, View) -> Unit
) : ListAdapter<UserListEntity, WatchlistManagerAdapter.ListViewHolder>(ListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_watchlist_row, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position), onListClick, onSetCurrent, onMenuClick)
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvListName)
        private val ivCurrent: ImageView = itemView.findViewById(R.id.ivCurrentStatus)
        // Matched the ID from your XML
        private val ivMore: ImageView = itemView.findViewById(R.id.ivMoreOptions)

        fun bind(
            list: UserListEntity,
            onClick: (UserListEntity) -> Unit,
            onSet: (UserListEntity) -> Unit,
            onMenuClick: (UserListEntity, View) -> Unit
        ) {
            tvName.text = list.name

            if (list.isCurrent) {
                // --- ACTIVE STATE (Always Teal) ---
                ivCurrent.setImageResource(R.drawable.ic_check)
                ivCurrent.setColorFilter(Color.parseColor("#03DAC5"))

                tvName.setTextColor(Color.parseColor("#03DAC5"))
                tvName.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                // --- INACTIVE STATE (Dynamic Theme Color) ---
                ivCurrent.setImageResource(R.drawable.ic_circle_outline)

                // 1. Get the System Text Color (Black in Light, White in Dark)
                val themeColor = resolveThemeColor(itemView.context, android.R.attr.textColorPrimary)

                // 2. Apply it to the Circle
                ivCurrent.setColorFilter(themeColor)



                tvName.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            // Click Listeners
            itemView.setOnClickListener { onClick(list) }
            ivCurrent.setOnClickListener { onSet(list) }

            // Menu Click
            ivMore.setOnClickListener { view ->
                onMenuClick(list, view)
            }
        }

        // --- HELPER: Extracts the color from the current Theme ---
        private fun resolveThemeColor(context: Context, attr: Int): Int {
            val typedValue = TypedValue()
            val theme = context.theme
            theme.resolveAttribute(attr, typedValue, true)
            return typedValue.data
        }
    }

    class ListDiffCallback : DiffUtil.ItemCallback<UserListEntity>() {
        override fun areItemsTheSame(oldItem: UserListEntity, newItem: UserListEntity) = oldItem.listId == newItem.listId
        override fun areContentsTheSame(oldItem: UserListEntity, newItem: UserListEntity) = oldItem == newItem
    }
}
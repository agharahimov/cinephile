package com.example.cinephile.ui.watchlist

import android.graphics.Color
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
    private val onSetCurrent: (UserListEntity) -> Unit
) : ListAdapter<UserListEntity, WatchlistManagerAdapter.ListViewHolder>(ListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_watchlist_row, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position), onListClick, onSetCurrent)
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvListName)
        private val ivCurrent: ImageView = itemView.findViewById(R.id.ivCurrentStatus)

        fun bind(list: UserListEntity, onClick: (UserListEntity) -> Unit, onSet: (UserListEntity) -> Unit) {
            tvName.text = list.name

            // Visual Logic: Green if current, Gray if not
            if (list.isCurrent) {
                ivCurrent.setImageResource(android.R.drawable.checkbox_on_background)
                ivCurrent.setColorFilter(Color.parseColor("#03DAC5")) // Teal
                tvName.setTextColor(Color.parseColor("#03DAC5"))
            } else {
                ivCurrent.setImageResource(android.R.drawable.radiobutton_off_background)
                ivCurrent.setColorFilter(Color.GRAY)
                tvName.setTextColor(Color.WHITE)
            }

            itemView.setOnClickListener { onClick(list) }
            ivCurrent.setOnClickListener { onSet(list) }
        }
    }

    class ListDiffCallback : DiffUtil.ItemCallback<UserListEntity>() {
        override fun areItemsTheSame(oldItem: UserListEntity, newItem: UserListEntity) = oldItem.listId == newItem.listId
        override fun areContentsTheSame(oldItem: UserListEntity, newItem: UserListEntity) = oldItem == newItem
    }
}
package com.example.cinephile.ui.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.data.local.UserListEntity

class QuizListAdapter(
    private val onListClick: (UserListEntity) -> Unit
) : ListAdapter<UserListEntity, QuizListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onListClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvQuizListName)

        fun bind(list: UserListEntity, onClick: (UserListEntity) -> Unit) {
            tvName.text = list.name
            itemView.setOnClickListener { onClick(list) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserListEntity>() {
        override fun areItemsTheSame(oldItem: UserListEntity, newItem: UserListEntity) =
            oldItem.listId == newItem.listId
        override fun areContentsTheSame(oldItem: UserListEntity, newItem: UserListEntity) =
            oldItem == newItem
    }
}
package com.example.recyclerviewcontactsdemo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// The adapter takes the list of contacts as a constructor parameter.
class ContactAdapter(private val contactList: MutableList<Contact>,
                     private val itemTouchHelper: ItemTouchHelper)
    : RecyclerView.Adapter<ContactViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION
    // Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        // Create a new view, which defines the UI of the list item
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    // Returns the total number of items in the data set held by the adapter.
    override fun getItemCount(): Int {
        return contactList.size
    }

    @SuppressLint("ClickableViewAccessibility")
    // Called by RecyclerView to display the data at the specified position.
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        // Get the contact at the given position from our list
        val contact = contactList[position]

        holder.name_tv.text = contact.name
        holder.address_tv.text = contact.address
        holder.phone_tv.text = contact.phone
        holder.email_tv.text = contact.email

        // Load image using Glide
        val imageUrl = "https://avatar.iran.liara.run/public/${contact.id % 100}"
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.image_iv)

        // --- Handle Click and Long Click ---

        if (selectedPosition == position) {
            holder.itemLayout.setBackgroundColor(Color.LTGRAY)
        } else {
            holder.itemLayout.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemLayout.setOnClickListener {
            if (selectedPosition != holder.adapterPosition) {
                notifyItemChanged(selectedPosition)
                selectedPosition = holder.adapterPosition
                notifyItemChanged(selectedPosition)
            } else {
                notifyItemChanged(selectedPosition)
                selectedPosition = RecyclerView.NO_POSITION
            }
        }

        holder.itemLayout.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete ${contact.name}?")
                .setPositiveButton("Yes") { _, _ ->
                    contactList.removeAt(holder.bindingAdapterPosition)
                    notifyItemRemoved(holder.bindingAdapterPosition)
                }
                .setNegativeButton("No", null)
                .show()
            true
        }
        holder.dragHandle_iv.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(holder)
            }
            true
        }
    }
}
package com.example.recyclerviewcontactsdemo

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class ContactViewHolder(view: View) : ViewHolder(view) {
    val itemLayout: ConstraintLayout = view.findViewById(R.id.item_layout)

    val image_iv: ImageView = view.findViewById(R.id.contact_image_iv)
    val name_tv: TextView = view.findViewById(R.id.contact_name_tv)
    val address_tv: TextView = view.findViewById(R.id.contact_address_tv)
    val phone_tv: TextView = view.findViewById(R.id.contact_phone_tv)
    val email_tv: TextView = view.findViewById(R.id.contact_email_tv)
    val dragHandle_iv: ImageView = view.findViewById(R.id.drag_handle_iv)
}
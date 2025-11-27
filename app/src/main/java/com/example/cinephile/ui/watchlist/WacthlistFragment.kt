package com.example.cinephile.ui.watchlist

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.data.local.UserListEntity
import com.example.cinephile.ui.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class WatchlistFragment : Fragment(R.layout.fragment_watchlist_manager) {

    private lateinit var viewModel: WatchlistManagerViewModel
    private lateinit var adapter: WatchlistManagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[WatchlistManagerViewModel::class.java]

        val rvLists = view.findViewById<RecyclerView>(R.id.rvLists)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddList)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener { findNavController().navigateUp() }

        adapter = WatchlistManagerAdapter(
            onListClick = { list ->
                val bundle = Bundle().apply {
                    putLong("listId", list.listId)
                    putString("listName", list.name)
                }
                findNavController().navigate(R.id.action_watchlistFragment_to_watchlistDetailFragment, bundle)
            },
            onSetCurrent = { list ->
                viewModel.setAsCurrent(list)
            },
            onMenuClick = { list, anchorView ->
                showPopupMenu(list, anchorView)
            }
        )

        rvLists.layoutManager = LinearLayoutManager(context)
        rvLists.adapter = adapter

        lifecycleScope.launch {
            viewModel.lists.collect { lists ->
                adapter.submitList(lists)
            }
        }

        fabAdd.setOnClickListener {
            showCreateListDialog()
        }
    }

    private fun showPopupMenu(list: UserListEntity, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, "Rename")
        popup.menu.add(0, 2, 1, "Delete")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> { // Rename
                    showRenameDialog(list)
                    true
                }
                2 -> { // Delete
                    if (list.isCurrent) {
                        Toast.makeText(context, "Cannot delete the active list.", Toast.LENGTH_SHORT).show()
                    } else {
                        showDeleteDialog(list)
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // --- CUSTOM INPUT DIALOG (CREATE) ---
    private fun showCreateListDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_input, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etInput = dialogView.findViewById<EditText>(R.id.etDialogInput)
        val btnCancel = dialogView.findViewById<View>(R.id.btnDialogCancel)
        val btnConfirm = dialogView.findViewById<View>(R.id.btnDialogConfirm)

        tvTitle.text = "New Watchlist"
        etInput.hint = "e.g. Horror Movies"

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            val name = etInput.text.toString()
            if (name.isNotBlank()) {
                viewModel.createList(name)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    // --- CUSTOM INPUT DIALOG (RENAME) ---
    private fun showRenameDialog(list: UserListEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_input, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etInput = dialogView.findViewById<EditText>(R.id.etDialogInput)
        val btnCancel = dialogView.findViewById<View>(R.id.btnDialogCancel)
        val btnConfirm = dialogView.findViewById<View>(R.id.btnDialogConfirm)

        tvTitle.text = "Rename List"
        etInput.setText(list.name)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            val newName = etInput.text.toString()
            if (newName.isNotBlank()) {
                viewModel.renameList(list, newName)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    // --- CUSTOM CONFIRM DIALOG (DELETE) ---
    private fun showDeleteDialog(list: UserListEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_confirm, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
        val btnCancel = dialogView.findViewById<View>(R.id.btnDialogCancel)
        val btnConfirm = dialogView.findViewById<View>(R.id.btnDialogConfirm)

        tvTitle.text = "Delete List"
        tvMessage.text = "Delete '${list.name}'? This removes all movies inside it."

        // Make delete button Red
        btnConfirm.setBackgroundColor(Color.parseColor("#E91E63"))

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            viewModel.deleteList(list.listId)
            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }
}
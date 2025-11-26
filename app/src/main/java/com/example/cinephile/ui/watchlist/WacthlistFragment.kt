package com.example.cinephile.ui.watchlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu // Import PopupMenu
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

    // --- NEW: POPUP MENU LOGIC ---
    private fun showPopupMenu(list: UserListEntity, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        // Manually adding menu items since we didn't create a menu resource file
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

    private fun showCreateListDialog() {
        val input = EditText(requireContext())
        input.hint = "List Name"
        input.setPadding(50, 30, 50, 30)
        input.setTextColor(resources.getColor(android.R.color.white, null)) // White text for dark mode
        input.setHintTextColor(resources.getColor(android.R.color.darker_gray, null))

        // Use the DarkDialogTheme we defined
        AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
            .setTitle("New Watchlist")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    viewModel.createList(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRenameDialog(list: UserListEntity) {
        val input = EditText(requireContext())
        input.setText(list.name) // Pre-fill existing name
        input.setPadding(50, 30, 50, 30)
        input.setTextColor(resources.getColor(android.R.color.white, null))

        AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
            .setTitle("Rename List")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotBlank()) {
                    viewModel.renameList(list, newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(list: UserListEntity) {
        AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
            .setTitle("Delete List")
            .setMessage("Delete '${list.name}'? This removes all movies inside it.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteList(list.listId)
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
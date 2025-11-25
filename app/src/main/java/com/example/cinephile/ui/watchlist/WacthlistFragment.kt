package com.example.cinephile.ui.watchlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast // <--- FIX: Added Import
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

        // 3. Setup Adapter
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
            // --- FIX: ADD THE MISSING 3RD ARGUMENT ---
            onLongClick = { list ->
                if (list.isCurrent) {
                    Toast.makeText(context, "Cannot delete the active list.", Toast.LENGTH_SHORT).show()
                } else {
                    showDeleteDialog(list) // This calls the function below
                }
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

    // Helper to create list
    private fun showCreateListDialog() {
        val input = EditText(requireContext())
        input.hint = "List Name"
        input.setPadding(50, 30, 50, 30)
        // Fix text color for dark mode
        input.setTextColor(resources.getColor(android.R.color.black, null))
        input.background = resources.getDrawable(android.R.drawable.edit_text, null)

        AlertDialog.Builder(requireContext())
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

    // --- FIX: Helper to delete list (Ensure this function is inside the class) ---
    private fun showDeleteDialog(list: UserListEntity) {
        AlertDialog.Builder(requireContext())
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
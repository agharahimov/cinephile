package com.example.cinephile.ui.watchlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.ui.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class WatchlistFragment : Fragment(R.layout.fragment_watchlist_manager) {

    private lateinit var viewModel: WatchlistManagerViewModel
    private lateinit var adapter: WatchlistManagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup ViewModel
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[WatchlistManagerViewModel::class.java]

        // 2. Find Views
        val rvLists = view.findViewById<RecyclerView>(R.id.rvLists)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddList)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener { findNavController().navigateUp() }

        // 3. Setup Adapter
        adapter = WatchlistManagerAdapter(
            onListClick = { list ->
                // LINKING 1: Navigate to the Detail screen, passing the List ID
                val bundle = Bundle().apply {
                    putLong("listId", list.listId)
                    putString("listName", list.name)
                }
                findNavController().navigate(R.id.action_watchlistFragment_to_watchlistDetailFragment, bundle)
            },
            onSetCurrent = { list ->
                // LINKING 2: Update Database to make this the "Active" list
                viewModel.setAsCurrent(list)
            }
        )

        rvLists.layoutManager = LinearLayoutManager(context)
        rvLists.adapter = adapter

        // 4. Observe Data
        lifecycleScope.launch {
            viewModel.lists.collect { lists ->
                adapter.submitList(lists)
            }
        }

        // 5. Create New List
        fabAdd.setOnClickListener {
            showCreateListDialog()
        }
    }

    private fun showCreateListDialog() {
        val input = EditText(requireContext())
        input.hint = "List Name"
        input.setPadding(50, 30, 50, 30)
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
}
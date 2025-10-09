package com.example.deezerapidemo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deezerapidemo.adapter.TrackAdapter
import com.example.deezerapidemo.model.DeezerSearchResponse
import com.example.deezerapidemo.viewmodel.TracksViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerView: RecyclerView

    // Instantiate the ViewModel using the KTX extension
    private val viewModel: TracksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        recyclerView = findViewById(R.id.recyclerView)

        // Set up the RecyclerView with a GridLayoutManager
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

        // --- Create the observer for the ViewModel's LiveData ---
        val observer = Observer<DeezerSearchResponse> { response ->
            // This code will run every time the LiveData changes.
            recyclerView.adapter = TrackAdapter(response.data)
        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.data.observe(this, observer)

        // --- Set up the search button click listener ---
        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString()

            // What happens if a search is initiated with an empty query?
            if (searchText.isBlank()) {
                Toast.makeText(this, "Search query cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                // Trigger the search in the ViewModel
                viewModel.search(searchText)
            }
        }
    }
}
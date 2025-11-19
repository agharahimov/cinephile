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

    private val viewModel: TracksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

        val observer = Observer<DeezerSearchResponse> { response ->
            recyclerView.adapter = TrackAdapter(response.data)
        }
        viewModel.data.observe(this, observer)

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString()

            if (searchText.isBlank()) {
                Toast.makeText(this, "Search query cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.search(searchText)
            }
        }
    }
}
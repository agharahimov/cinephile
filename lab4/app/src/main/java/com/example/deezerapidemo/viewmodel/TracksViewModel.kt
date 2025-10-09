package com.example.deezerapidemo.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deezerapidemo.model.DeezerSearchResponse
import com.example.deezerapidemo.network.DeezerApiInterface
import com.example.deezerapidemo.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TracksViewModel : ViewModel() {

    // Use MutableLiveData to hold the search response.
    // It's private so only the ViewModel can modify it.
    private val _data = MutableLiveData<DeezerSearchResponse>()

    // Expose an immutable LiveData to the UI to observe.
    val data: MutableLiveData<DeezerSearchResponse> = _data

    private val apiInterface: DeezerApiInterface =
        RetrofitInstance.getInstance().create(DeezerApiInterface::class.java)

    /**
     * Perform a search query.
     * The result is posted to the _data LiveData object.
     */
    fun search(query: String) {
        // viewModelScope automatically handles the lifecycle of the coroutine.
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiInterface.searchTracks(query)
                // Use postValue when updating LiveData from a background thread.
                _data.postValue(response)
            } catch (e: Exception) {
                // Handle errors (e.g., network issues)
                Log.e("TracksViewModel", "API call failed: ${e.message}")
            }
        }
    }
}
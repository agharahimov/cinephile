package com.example.cinephile.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.data.local.UserListEntity
import com.example.cinephile.domain.repository.UserCollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WatchlistManagerViewModel(private val repository: UserCollectionsRepository) : ViewModel() {

    private val _lists = MutableStateFlow<List<UserListEntity>>(emptyList())
    val lists: StateFlow<List<UserListEntity>> = _lists

    init {
        loadLists()
    }

    fun loadLists() {
        viewModelScope.launch {
            // Ensure a default list exists before loading
            repository.ensureDefaultListExists()
            val result = repository.getAllCustomLists()
            result.onSuccess { _lists.value = it }
        }
    }

    fun createList(name: String) {
        viewModelScope.launch {
            repository.createCustomList(name)
            loadLists()
        }
    }

    fun setAsCurrent(list: UserListEntity) {
        viewModelScope.launch {
            repository.setCurrentList(list.listId)
            loadLists() // Refresh to show the green checkmark move
        }
    }
    fun deleteList(listId: Long) {
        viewModelScope.launch {
            repository.deleteUserList(listId)
            // Refresh the UI to show the list is gone
            loadLists()
        }
    }

    fun renameList(list: UserListEntity, newName: String) {
        viewModelScope.launch {
            repository.renameUserList(list.listId, newName)
            loadLists() // Refresh UI
        }
    }
}
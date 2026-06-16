package com.muhasebecim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.muhasebecim.data.model.Person
import com.muhasebecim.data.repository.MuhasebecimRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PersonListViewModel(private val repository: MuhasebecimRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val persons: StateFlow<List<Person>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllPersons()
            else repository.searchPersons(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addPerson(name: String) {
        viewModelScope.launch {
            repository.insertPerson(Person(name = name.trim()))
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            repository.deletePerson(person)
        }
    }

    fun updatePersonName(person: Person, newName: String) {
        viewModelScope.launch {
            repository.updatePerson(person.copy(name = newName.trim()))
        }
    }

    class Factory(private val repository: MuhasebecimRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PersonListViewModel(repository) as T
        }
    }
}

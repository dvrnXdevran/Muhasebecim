package com.muhasebecim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.muhasebecim.data.model.GeneralScheduledNote
import com.muhasebecim.data.model.ScheduledNote
import com.muhasebecim.data.model.ScheduledNoteType
import com.muhasebecim.data.repository.MuhasebecimRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UpcomingListItem(
    val id: Long,
    val description: String,
    val amount: Double,
    val dueDate: Long,
    val type: ScheduledNoteType,
    val isCompleted: Boolean,
    val personId: Long? = null,
    val isGeneral: Boolean = false
)

class UpcomingViewModel(private val repository: MuhasebecimRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0) // 0 = Tümü, 1 = Ödemeler, 2 = Alacaklar
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private fun ScheduledNote.toUpcomingItem() = UpcomingListItem(
        id = id,
        description = description,
        amount = amount,
        dueDate = dueDate,
        type = type,
        isCompleted = isCompleted,
        personId = personId
    )

    private fun GeneralScheduledNote.toUpcomingItem() = UpcomingListItem(
        id = id,
        description = description,
        amount = amount,
        dueDate = dueDate,
        type = type,
        isCompleted = isCompleted,
        isGeneral = true
    )

    val allUpcoming: StateFlow<List<UpcomingListItem>> = combine(
        repository.getAllUpcoming(),
        repository.getGeneralUpcoming()
    ) { personItems, generalItems ->
        (personItems.map { it.toUpcomingItem() } + generalItems.map { it.toUpcomingItem() })
            .sortedBy { it.dueDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingOdemeler: StateFlow<List<UpcomingListItem>> = combine(
        repository.getUpcomingByType(ScheduledNoteType.ODEME),
        repository.getGeneralUpcomingByType(ScheduledNoteType.ODEME)
    ) { personItems, generalItems ->
        (personItems.map { it.toUpcomingItem() } + generalItems.map { it.toUpcomingItem() })
            .sortedBy { it.dueDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingAlacaklar: StateFlow<List<UpcomingListItem>> = combine(
        repository.getUpcomingByType(ScheduledNoteType.ALACAK),
        repository.getGeneralUpcomingByType(ScheduledNoteType.ALACAK)
    ) { personItems, generalItems ->
        (personItems.map { it.toUpcomingItem() } + generalItems.map { it.toUpcomingItem() })
            .sortedBy { it.dueDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onTabSelected(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleCompleted(item: UpcomingListItem) {
        viewModelScope.launch {
            if (item.isGeneral) {
                repository.updateGeneralScheduledNote(
                    GeneralScheduledNote(
                        id = item.id,
                        description = item.description,
                        amount = item.amount,
                        dueDate = item.dueDate,
                        type = item.type,
                        isCompleted = !item.isCompleted
                    )
                )
            } else {
                repository.updateScheduledNote(
                    ScheduledNote(
                        id = item.id,
                        personId = item.personId ?: 0L,
                        description = item.description,
                        amount = item.amount,
                        dueDate = item.dueDate,
                        type = item.type,
                        isCompleted = !item.isCompleted
                    )
                )
            }
        }
    }

    fun deleteScheduledNote(item: UpcomingListItem) {
        viewModelScope.launch {
            if (item.isGeneral) {
                repository.deleteGeneralScheduledNote(
                    GeneralScheduledNote(
                        id = item.id,
                        description = item.description,
                        amount = item.amount,
                        dueDate = item.dueDate,
                        type = item.type,
                        isCompleted = item.isCompleted
                    )
                )
            } else {
                repository.deleteScheduledNote(
                    ScheduledNote(
                        id = item.id,
                        personId = item.personId ?: 0L,
                        description = item.description,
                        amount = item.amount,
                        dueDate = item.dueDate,
                        type = item.type,
                        isCompleted = item.isCompleted
                    )
                )
            }
        }
    }

    class Factory(private val repository: MuhasebecimRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpcomingViewModel(repository) as T
        }
    }
}

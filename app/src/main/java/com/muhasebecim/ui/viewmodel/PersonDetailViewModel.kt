package com.muhasebecim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.muhasebecim.data.model.*
import com.muhasebecim.data.repository.MuhasebecimRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PersonDetailViewModel(
    private val repository: MuhasebecimRepository,
    private val personId: Long
) : ViewModel() {

    val person: StateFlow<Person?> = repository.getPersonById(personId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<Transaction>> = repository.getTransactionsByPerson(personId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAlacak: StateFlow<Double> = repository.getTotalByPersonAndType(personId, TransactionType.ALACAK)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBorc: StateFlow<Double> = repository.getTotalByPersonAndType(personId, TransactionType.BORC)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val outstandingAlacak: StateFlow<Double> = combine(
        repository.getUnpaidTotalByPersonAndType(personId, TransactionType.ALACAK),
        repository.getPendingScheduledTotalByPersonAndType(personId, ScheduledNoteType.ALACAK)
    ) { unpaidTransactions, pendingScheduled ->
        unpaidTransactions + pendingScheduled
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val outstandingBorc: StateFlow<Double> = combine(
        repository.getUnpaidTotalByPersonAndType(personId, TransactionType.BORC),
        repository.getPendingScheduledTotalByPersonAndType(personId, ScheduledNoteType.ODEME)
    ) { unpaidTransactions, pendingScheduled ->
        unpaidTransactions + pendingScheduled
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val notes: StateFlow<List<Note>> = repository.getNotesByPerson(personId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledNotes: StateFlow<List<ScheduledNote>> = repository.getScheduledNotesByPerson(personId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(amount: Double, description: String, type: TransactionType) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    personId = personId,
                    amount = amount,
                    description = description.trim(),
                    type = type
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateTransaction(
        transaction: Transaction,
        amount: Double,
        description: String,
        type: TransactionType
    ) {
        viewModelScope.launch {
            repository.updateTransaction(
                transaction.copy(
                    amount = amount,
                    description = description.trim(),
                    type = type
                )
            )
        }
    }

    fun toggleTransactionPaid(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction.copy(isPaid = !transaction.isPaid))
        }
    }

    fun addNote(content: String) {
        viewModelScope.launch {
            repository.insertNote(
                Note(personId = personId, content = content.trim())
            )
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun updateNote(note: Note, content: String) {
        viewModelScope.launch {
            repository.updateNote(note.copy(content = content.trim()))
        }
    }

    fun addScheduledNote(description: String, amount: Double, dueDate: Long, type: ScheduledNoteType) {
        viewModelScope.launch {
            repository.insertScheduledNote(
                ScheduledNote(
                    personId = personId,
                    description = description.trim(),
                    amount = amount,
                    dueDate = dueDate,
                    type = type
                )
            )
        }
    }

    fun toggleScheduledNoteCompleted(scheduledNote: ScheduledNote) {
        viewModelScope.launch {
            repository.updateScheduledNote(scheduledNote.copy(isCompleted = !scheduledNote.isCompleted))
        }
    }

    fun deleteScheduledNote(scheduledNote: ScheduledNote) {
        viewModelScope.launch {
            repository.deleteScheduledNote(scheduledNote)
        }
    }

    fun updateScheduledNote(
        scheduledNote: ScheduledNote,
        description: String,
        amount: Double,
        dueDate: Long,
        type: ScheduledNoteType
    ) {
        viewModelScope.launch {
            repository.updateScheduledNote(
                scheduledNote.copy(
                    description = description.trim(),
                    amount = amount,
                    dueDate = dueDate,
                    type = type
                )
            )
        }
    }

    fun updatePersonName(newName: String) {
        viewModelScope.launch {
            person.value?.let { p ->
                repository.updatePerson(p.copy(name = newName.trim()))
            }
        }
    }

    class Factory(
        private val repository: MuhasebecimRepository,
        private val personId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PersonDetailViewModel(repository, personId) as T
        }
    }
}

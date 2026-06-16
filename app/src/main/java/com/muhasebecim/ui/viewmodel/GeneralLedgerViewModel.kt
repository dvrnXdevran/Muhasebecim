package com.muhasebecim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.muhasebecim.data.model.*
import com.muhasebecim.data.repository.MuhasebecimRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GeneralLedgerViewModel(private val repository: MuhasebecimRepository) : ViewModel() {

    // Genel Notlar
    val generalNotes: StateFlow<List<GeneralNote>> = repository.getAllGeneralNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Genel İşlemler
    val generalTransactions: StateFlow<List<GeneralTransaction>> = repository.getAllGeneralTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalGelir: StateFlow<Double> = repository.getGeneralTotalByType(GeneralTransactionType.GELIR)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalGider: StateFlow<Double> = repository.getGeneralTotalByType(GeneralTransactionType.GIDER)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val outstandingGelir: StateFlow<Double> = combine(
        repository.getUnpaidGeneralTotalByType(GeneralTransactionType.GELIR),
        repository.getPendingGeneralScheduledTotalByType(ScheduledNoteType.ALACAK)
    ) { unpaidTransactions, pendingScheduled ->
        unpaidTransactions + pendingScheduled
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val outstandingGider: StateFlow<Double> = combine(
        repository.getUnpaidGeneralTotalByType(GeneralTransactionType.GIDER),
        repository.getPendingGeneralScheduledTotalByType(ScheduledNoteType.ODEME)
    ) { unpaidTransactions, pendingScheduled ->
        unpaidTransactions + pendingScheduled
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Genel Planlanan İşlemler
    val generalScheduledNotes: StateFlow<List<GeneralScheduledNote>> = repository.getAllGeneralScheduledNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Genel Not
    fun addGeneralNote(content: String) {
        viewModelScope.launch {
            repository.insertGeneralNote(GeneralNote(content = content.trim()))
        }
    }

    fun deleteGeneralNote(note: GeneralNote) {
        viewModelScope.launch {
            repository.deleteGeneralNote(note)
        }
    }

    fun updateGeneralNote(note: GeneralNote, content: String) {
        viewModelScope.launch {
            repository.updateGeneralNote(note.copy(content = content.trim()))
        }
    }

    // Genel İşlem
    fun addGeneralTransaction(amount: Double, description: String, type: GeneralTransactionType) {
        viewModelScope.launch {
            repository.insertGeneralTransaction(
                GeneralTransaction(
                    amount = amount,
                    description = description.trim(),
                    type = type
                )
            )
        }
    }

    fun deleteGeneralTransaction(transaction: GeneralTransaction) {
        viewModelScope.launch {
            repository.deleteGeneralTransaction(transaction)
        }
    }

    fun updateGeneralTransaction(
        transaction: GeneralTransaction,
        amount: Double,
        description: String,
        type: GeneralTransactionType
    ) {
        viewModelScope.launch {
            repository.updateGeneralTransaction(
                transaction.copy(
                    amount = amount,
                    description = description.trim(),
                    type = type
                )
            )
        }
    }

    fun toggleGeneralTransactionPaid(transaction: GeneralTransaction) {
        viewModelScope.launch {
            repository.updateGeneralTransaction(transaction.copy(isPaid = !transaction.isPaid))
        }
    }

    // Genel Planlanan İşlem
    fun addGeneralScheduledNote(description: String, amount: Double, dueDate: Long, type: ScheduledNoteType) {
        viewModelScope.launch {
            repository.insertGeneralScheduledNote(
                GeneralScheduledNote(
                    description = description.trim(),
                    amount = amount,
                    dueDate = dueDate,
                    type = type
                )
            )
        }
    }

    fun toggleGeneralScheduledNoteCompleted(scheduledNote: GeneralScheduledNote) {
        viewModelScope.launch {
            repository.updateGeneralScheduledNote(scheduledNote.copy(isCompleted = !scheduledNote.isCompleted))
        }
    }

    fun deleteGeneralScheduledNote(scheduledNote: GeneralScheduledNote) {
        viewModelScope.launch {
            repository.deleteGeneralScheduledNote(scheduledNote)
        }
    }

    fun updateGeneralScheduledNote(
        scheduledNote: GeneralScheduledNote,
        description: String,
        amount: Double,
        dueDate: Long,
        type: ScheduledNoteType
    ) {
        viewModelScope.launch {
            repository.updateGeneralScheduledNote(
                scheduledNote.copy(
                    description = description.trim(),
                    amount = amount,
                    dueDate = dueDate,
                    type = type
                )
            )
        }
    }

    class Factory(private val repository: MuhasebecimRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GeneralLedgerViewModel(repository) as T
        }
    }
}

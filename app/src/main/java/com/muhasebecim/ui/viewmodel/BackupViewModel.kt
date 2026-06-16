package com.muhasebecim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.muhasebecim.data.backup.BackupRepository
import com.muhasebecim.data.backup.BackupSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BackupUiState(
    val isBusy: Boolean = false,
    val isConfigured: Boolean = false,
    val lastSuccessfulBackupAt: Long? = null,
    val lastStatusMessage: String? = null,
    val lastErrorMessage: String? = null,
    val pendingRestoreSnapshot: BackupSnapshot? = null
)

class BackupViewModel(private val backupRepository: BackupRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        refreshLocalStatus()
    }

    fun refreshLocalStatus() {
        val status = backupRepository.getLocalStatus()
        _uiState.update {
            it.copy(
                isConfigured = status.isConfigured,
                lastSuccessfulBackupAt = status.lastSuccessfulBackupAt,
                lastStatusMessage = status.lastStatusMessage,
                lastErrorMessage = status.lastErrorMessage
            )
        }
    }

    fun backupNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, lastErrorMessage = null) }
            val result = backupRepository.performBackup()
            val status = backupRepository.getLocalStatus()
            _uiState.update {
                it.copy(
                    isBusy = false,
                    isConfigured = status.isConfigured,
                    lastSuccessfulBackupAt = status.lastSuccessfulBackupAt,
                    lastStatusMessage = if (result.success) result.message else status.lastStatusMessage,
                    lastErrorMessage = if (result.success) null else result.message
                )
            }
        }
    }

    fun prepareRestore() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isBusy = true, pendingRestoreSnapshot = null, lastErrorMessage = null)
            }
            val result = backupRepository.fetchLatestBackup()
            _uiState.update {
                it.copy(
                    isBusy = false,
                    pendingRestoreSnapshot = result.snapshot,
                    lastStatusMessage = if (result.success) result.message else it.lastStatusMessage,
                    lastErrorMessage = if (result.success) null else result.message
                )
            }
        }
    }

    fun confirmRestore() {
        val snapshot = _uiState.value.pendingRestoreSnapshot ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, lastErrorMessage = null) }
            val result = backupRepository.restoreSnapshot(snapshot)
            val status = backupRepository.getLocalStatus()
            _uiState.update {
                it.copy(
                    isBusy = false,
                    isConfigured = status.isConfigured,
                    pendingRestoreSnapshot = null,
                    lastSuccessfulBackupAt = status.lastSuccessfulBackupAt,
                    lastStatusMessage = if (result.success) result.message else it.lastStatusMessage,
                    lastErrorMessage = if (result.success) null else result.message
                )
            }
        }
    }

    fun dismissRestoreDialog() {
        _uiState.update { it.copy(pendingRestoreSnapshot = null) }
    }

    class Factory(private val backupRepository: BackupRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackupViewModel(backupRepository) as T
        }
    }
}

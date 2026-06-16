package com.muhasebecim.data.backup

import com.muhasebecim.data.model.GeneralNote
import com.muhasebecim.data.model.GeneralScheduledNote
import com.muhasebecim.data.model.GeneralTransaction
import com.muhasebecim.data.model.Note
import com.muhasebecim.data.model.Person
import com.muhasebecim.data.model.ScheduledNote
import com.muhasebecim.data.model.Transaction

const val BACKUP_VERSION = 1
const val BACKUP_DATABASE_VERSION = 3

data class BackupPayload(
    val persons: List<Person>,
    val transactions: List<Transaction>,
    val notes: List<Note>,
    val scheduledNotes: List<ScheduledNote>,
    val generalNotes: List<GeneralNote>,
    val generalTransactions: List<GeneralTransaction>,
    val generalScheduledNotes: List<GeneralScheduledNote>
)

data class BackupSnapshot(
    val backupVersion: Int,
    val databaseVersion: Int,
    val createdAt: Long,
    val appVersion: String,
    val deviceLabel: String,
    val deviceId: String,
    val payload: BackupPayload
)

data class LocalBackupStatus(
    val lastSuccessfulBackupAt: Long?,
    val lastAutoBackupAttemptAt: Long?,
    val lastStatusMessage: String?,
    val lastErrorMessage: String?,
    val isConfigured: Boolean
)

data class RemoteBackupInfo(
    val hasBackup: Boolean,
    val snapshot: BackupSnapshot?
)

data class BackupOperationResult(
    val success: Boolean,
    val message: String,
    val snapshot: BackupSnapshot? = null
)

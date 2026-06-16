package com.muhasebecim.data.backup

import android.content.Context
import android.os.Build
import androidx.room.withTransaction
import com.muhasebecim.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val httpClient: BackupHttpClient = BackupHttpClient()
) {
    private val preferences = BackupPreferences(context.applicationContext)

    fun getLocalStatus(): LocalBackupStatus = preferences.getStatus()

    suspend fun runAutomaticBackupIfDue() {
        val now = System.currentTimeMillis()
        val lastAttempt = preferences.getStatus().lastAutoBackupAttemptAt ?: 0L
        val oneDayMillis = 24L * 60L * 60L * 1000L
        if (now - lastAttempt < oneDayMillis) return

        preferences.markAutoAttempt(now)
        if (!BackupConfig.isConfigured()) return

        performBackup(isAutomatic = true)
    }

    suspend fun performBackup(isAutomatic: Boolean = false): BackupOperationResult = withContext(Dispatchers.IO) {
        if (!BackupConfig.isConfigured()) {
            val message = "Sunucu adresi veya token ayari eksik"
            preferences.markError(message)
            return@withContext BackupOperationResult(false, message)
        }

        try {
            val snapshot = createSnapshot()
            httpClient.uploadBackup(snapshot)
            val prefix = if (isAutomatic) "Otomatik yedek alindi" else "Yedek alindi"
            val message = "$prefix: ${countRecords(snapshot)} kayit"
            preferences.markBackupSuccess(snapshot.createdAt, message)
            BackupOperationResult(true, message, snapshot)
        } catch (error: Exception) {
            val message = error.message ?: "Yedekleme sirasinda hata olustu"
            preferences.markError(message)
            BackupOperationResult(false, message)
        }
    }

    suspend fun fetchLatestBackup(): BackupOperationResult = withContext(Dispatchers.IO) {
        if (!BackupConfig.isConfigured()) {
            return@withContext BackupOperationResult(false, "Sunucu adresi veya token ayari eksik")
        }

        try {
            val remoteInfo = httpClient.downloadLatest(preferences.getDeviceId())
            if (!remoteInfo.hasBackup || remoteInfo.snapshot == null) {
                BackupOperationResult(false, "Sunucuda yedek bulunamadi")
            } else {
                BackupOperationResult(true, "Son yedek bulundu", remoteInfo.snapshot)
            }
        } catch (error: Exception) {
            BackupOperationResult(false, error.message ?: "Son yedek alinamadi")
        }
    }

    suspend fun restoreSnapshot(snapshot: BackupSnapshot): BackupOperationResult = withContext(Dispatchers.IO) {
        if (snapshot.backupVersion != BACKUP_VERSION) {
            return@withContext BackupOperationResult(false, "Desteklenmeyen yedek surumu: ${snapshot.backupVersion}")
        }

        try {
            database.withTransaction {
                database.noteDao().deleteAllNotes()
                database.transactionDao().deleteAllTransactions()
                database.scheduledNoteDao().deleteAllScheduledNotes()
                database.generalNoteDao().deleteAllNotes()
                database.generalTransactionDao().deleteAllTransactions()
                database.generalScheduledNoteDao().deleteAllScheduledNotes()
                database.personDao().deleteAllPersons()

                database.personDao().insertPersons(snapshot.payload.persons)
                database.generalNoteDao().insertNotes(snapshot.payload.generalNotes)
                database.generalTransactionDao().insertTransactions(snapshot.payload.generalTransactions)
                database.generalScheduledNoteDao().insertScheduledNotes(snapshot.payload.generalScheduledNotes)
                database.transactionDao().insertTransactions(snapshot.payload.transactions)
                database.noteDao().insertNotes(snapshot.payload.notes)
                database.scheduledNoteDao().insertScheduledNotes(snapshot.payload.scheduledNotes)
            }

            val message = "Yedek geri yuklendi: ${countRecords(snapshot)} kayit"
            preferences.markBackupSuccess(snapshot.createdAt, message)
            BackupOperationResult(true, message, snapshot)
        } catch (error: Exception) {
            BackupOperationResult(false, error.message ?: "Geri yukleme basarisiz")
        }
    }

    private suspend fun createSnapshot(): BackupSnapshot {
        return BackupSnapshot(
            backupVersion = BACKUP_VERSION,
            databaseVersion = BACKUP_DATABASE_VERSION,
            createdAt = System.currentTimeMillis(),
            appVersion = getAppVersionName(),
            deviceLabel = getDeviceLabel(),
            deviceId = preferences.getDeviceId(),
            payload = BackupPayload(
                persons = database.personDao().getAllPersonsForBackup(),
                transactions = database.transactionDao().getAllTransactionsForBackup(),
                notes = database.noteDao().getAllNotesForBackup(),
                scheduledNotes = database.scheduledNoteDao().getAllScheduledNotesForBackup(),
                generalNotes = database.generalNoteDao().getAllNotesForBackup(),
                generalTransactions = database.generalTransactionDao().getAllTransactionsForBackup(),
                generalScheduledNotes = database.generalScheduledNoteDao().getAllScheduledNotesForBackup()
            )
        )
    }

    private fun countRecords(snapshot: BackupSnapshot): Int {
        return snapshot.payload.persons.size +
            snapshot.payload.transactions.size +
            snapshot.payload.notes.size +
            snapshot.payload.scheduledNotes.size +
            snapshot.payload.generalNotes.size +
            snapshot.payload.generalTransactions.size +
            snapshot.payload.generalScheduledNotes.size
    }

    private fun getAppVersionName(): String {
        return runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        }.getOrDefault("unknown")
    }

    private fun getDeviceLabel(): String {
        return listOf(Build.MANUFACTURER, Build.MODEL)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Android cihaz" }
    }
}

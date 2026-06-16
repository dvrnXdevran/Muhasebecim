package com.muhasebecim

import android.app.Application
import com.muhasebecim.data.backup.BackupRepository
import com.muhasebecim.data.database.AppDatabase
import com.muhasebecim.data.repository.MuhasebecimRepository

class MuhasebecimApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val backupRepository by lazy {
        BackupRepository(this, database)
    }

    val repository by lazy {
        MuhasebecimRepository(
            database.personDao(),
            database.transactionDao(),
            database.noteDao(),
            database.scheduledNoteDao(),
            database.generalNoteDao(),
            database.generalTransactionDao(),
            database.generalScheduledNoteDao()
        )
    }
}

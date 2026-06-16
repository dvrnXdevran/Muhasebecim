package com.muhasebecim.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.muhasebecim.data.dao.*
import com.muhasebecim.data.model.*

@Database(
    entities = [
        Person::class,
        Transaction::class,
        Note::class,
        ScheduledNote::class,
        GeneralNote::class,
        GeneralTransaction::class,
        GeneralScheduledNote::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun personDao(): PersonDao
    abstract fun transactionDao(): TransactionDao
    abstract fun noteDao(): NoteDao
    abstract fun scheduledNoteDao(): ScheduledNoteDao
    abstract fun generalNoteDao(): GeneralNoteDao
    abstract fun generalTransactionDao(): GeneralTransactionDao
    abstract fun generalScheduledNoteDao(): GeneralScheduledNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "muhasebecim_database"
                )
                    .addMigrations(*DatabaseMigrations.ALL)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

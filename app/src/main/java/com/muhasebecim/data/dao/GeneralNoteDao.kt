package com.muhasebecim.data.dao

import androidx.room.*
import com.muhasebecim.data.model.GeneralNote
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneralNoteDao {

    @Query("SELECT * FROM general_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<GeneralNote>>

    @Query("SELECT * FROM general_notes ORDER BY id ASC")
    suspend fun getAllNotesForBackup(): List<GeneralNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: GeneralNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<GeneralNote>)

    @Update
    suspend fun updateNote(note: GeneralNote)

    @Delete
    suspend fun deleteNote(note: GeneralNote)

    @Query("DELETE FROM general_notes")
    suspend fun deleteAllNotes()
}

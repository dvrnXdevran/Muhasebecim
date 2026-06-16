package com.muhasebecim.data.dao

import androidx.room.*
import com.muhasebecim.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE personId = :personId ORDER BY createdAt DESC")
    fun getNotesByPerson(personId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY id ASC")
    suspend fun getAllNotesForBackup(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<Note>)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}

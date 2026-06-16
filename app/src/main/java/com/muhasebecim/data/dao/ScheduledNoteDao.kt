package com.muhasebecim.data.dao

import androidx.room.*
import com.muhasebecim.data.model.ScheduledNote
import com.muhasebecim.data.model.ScheduledNoteType
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledNoteDao {

    @Query("SELECT * FROM scheduled_notes WHERE personId = :personId ORDER BY dueDate ASC")
    fun getScheduledNotesByPerson(personId: Long): Flow<List<ScheduledNote>>

    @Query("SELECT * FROM scheduled_notes ORDER BY id ASC")
    suspend fun getAllScheduledNotesForBackup(): List<ScheduledNote>

    @Query("SELECT * FROM scheduled_notes WHERE type = :type AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getUpcomingByType(type: ScheduledNoteType): Flow<List<ScheduledNote>>

    @Query("SELECT * FROM scheduled_notes WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getAllUpcoming(): Flow<List<ScheduledNote>>

    @Query("SELECT * FROM scheduled_notes WHERE isCompleted = 0 AND dueDate >= :startOfDay ORDER BY dueDate ASC")
    fun getUpcomingFromDate(startOfDay: Long): Flow<List<ScheduledNote>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM scheduled_notes WHERE personId = :personId AND type = :type AND isCompleted = 0")
    fun getPendingTotalByPersonAndType(personId: Long, type: ScheduledNoteType): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledNote(scheduledNote: ScheduledNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledNotes(scheduledNotes: List<ScheduledNote>)

    @Update
    suspend fun updateScheduledNote(scheduledNote: ScheduledNote)

    @Delete
    suspend fun deleteScheduledNote(scheduledNote: ScheduledNote)

    @Query("DELETE FROM scheduled_notes")
    suspend fun deleteAllScheduledNotes()
}

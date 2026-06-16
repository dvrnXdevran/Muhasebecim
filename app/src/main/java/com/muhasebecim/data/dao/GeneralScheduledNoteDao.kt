package com.muhasebecim.data.dao

import androidx.room.*
import com.muhasebecim.data.model.GeneralScheduledNote
import com.muhasebecim.data.model.ScheduledNoteType
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneralScheduledNoteDao {

    @Query("SELECT * FROM general_scheduled_notes ORDER BY dueDate ASC")
    fun getAllScheduledNotes(): Flow<List<GeneralScheduledNote>>

    @Query("SELECT * FROM general_scheduled_notes ORDER BY id ASC")
    suspend fun getAllScheduledNotesForBackup(): List<GeneralScheduledNote>

    @Query("SELECT * FROM general_scheduled_notes WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getUpcoming(): Flow<List<GeneralScheduledNote>>

    @Query("SELECT * FROM general_scheduled_notes WHERE type = :type AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getUpcomingByType(type: ScheduledNoteType): Flow<List<GeneralScheduledNote>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM general_scheduled_notes WHERE type = :type AND isCompleted = 0")
    fun getPendingTotalByType(type: ScheduledNoteType): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledNote(scheduledNote: GeneralScheduledNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledNotes(scheduledNotes: List<GeneralScheduledNote>)

    @Update
    suspend fun updateScheduledNote(scheduledNote: GeneralScheduledNote)

    @Delete
    suspend fun deleteScheduledNote(scheduledNote: GeneralScheduledNote)

    @Query("DELETE FROM general_scheduled_notes")
    suspend fun deleteAllScheduledNotes()
}

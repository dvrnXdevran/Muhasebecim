package com.muhasebecim.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "general_scheduled_notes")
data class GeneralScheduledNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val amount: Double,
    val dueDate: Long,
    val type: ScheduledNoteType,  // ODEME veya ALACAK
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

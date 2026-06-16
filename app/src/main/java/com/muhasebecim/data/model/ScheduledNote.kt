package com.muhasebecim.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ScheduledNoteType {
    ODEME,   // Yaklaşan ödeme (biz ödeyeceğiz)
    ALACAK   // Yaklaşan alacak (bize ödenecek)
}

@Entity(
    tableName = "scheduled_notes",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personId"])]
)
data class ScheduledNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val description: String,
    val amount: Double,
    val dueDate: Long,
    val type: ScheduledNoteType,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

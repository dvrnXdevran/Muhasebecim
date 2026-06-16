package com.muhasebecim.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "general_notes")
data class GeneralNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

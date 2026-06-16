package com.muhasebecim.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GeneralTransactionType {
    GELIR,   // Gelen para
    GIDER    // Giden para
}

@Entity(tableName = "general_transactions")
data class GeneralTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String = "",
    val type: GeneralTransactionType,
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

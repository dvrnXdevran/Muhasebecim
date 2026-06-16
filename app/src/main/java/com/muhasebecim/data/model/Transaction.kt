package com.muhasebecim.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    ALACAK,  // Bize borçlu (biz alacağız)
    BORC     // Bizim borcumuz (biz ödeyeceğiz)
}

@Entity(
    tableName = "transactions",
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
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val amount: Double,
    val description: String = "",
    val type: TransactionType,
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

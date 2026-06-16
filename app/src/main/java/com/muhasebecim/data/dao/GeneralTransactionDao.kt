package com.muhasebecim.data.dao

import androidx.room.*
import com.muhasebecim.data.model.GeneralTransaction
import com.muhasebecim.data.model.GeneralTransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneralTransactionDao {

    @Query("SELECT * FROM general_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<GeneralTransaction>>

    @Query("SELECT * FROM general_transactions ORDER BY id ASC")
    suspend fun getAllTransactionsForBackup(): List<GeneralTransaction>

    @Query("SELECT * FROM general_transactions WHERE type = :type ORDER BY createdAt DESC")
    fun getTransactionsByType(type: GeneralTransactionType): Flow<List<GeneralTransaction>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM general_transactions WHERE type = :type")
    fun getTotalByType(type: GeneralTransactionType): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM general_transactions WHERE type = :type AND isPaid = 0")
    fun getUnpaidTotalByType(type: GeneralTransactionType): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: GeneralTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<GeneralTransaction>)

    @Update
    suspend fun updateTransaction(transaction: GeneralTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: GeneralTransaction)

    @Query("DELETE FROM general_transactions")
    suspend fun deleteAllTransactions()
}

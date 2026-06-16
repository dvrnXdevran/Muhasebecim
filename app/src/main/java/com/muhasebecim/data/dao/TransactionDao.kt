package com.muhasebecim.data.dao

import androidx.room.*
import com.muhasebecim.data.model.Transaction
import com.muhasebecim.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY createdAt DESC")
    fun getTransactionsByPerson(personId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY id ASC")
    suspend fun getAllTransactionsForBackup(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE personId = :personId AND type = :type ORDER BY createdAt DESC")
    fun getTransactionsByPersonAndType(personId: Long, type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE personId = :personId AND type = :type")
    fun getTotalByPersonAndType(personId: Long, type: TransactionType): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE personId = :personId AND type = :type AND isPaid = 0")
    fun getUnpaidTotalByPersonAndType(personId: Long, type: TransactionType): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

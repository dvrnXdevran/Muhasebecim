package com.muhasebecim.data.repository

import com.muhasebecim.data.dao.*
import com.muhasebecim.data.model.*
import kotlinx.coroutines.flow.Flow

class MuhasebecimRepository(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val noteDao: NoteDao,
    private val scheduledNoteDao: ScheduledNoteDao,
    private val generalNoteDao: GeneralNoteDao,
    private val generalTransactionDao: GeneralTransactionDao,
    private val generalScheduledNoteDao: GeneralScheduledNoteDao
) {
    // Person
    fun getAllPersons(): Flow<List<Person>> = personDao.getAllPersons()
    fun getPersonById(id: Long): Flow<Person?> = personDao.getPersonById(id)
    fun searchPersons(query: String): Flow<List<Person>> = personDao.searchPersons(query)
    suspend fun insertPerson(person: Person): Long = personDao.insertPerson(person)
    suspend fun updatePerson(person: Person) = personDao.updatePerson(person)
    suspend fun deletePerson(person: Person) = personDao.deletePerson(person)

    // Transaction
    fun getTransactionsByPerson(personId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByPerson(personId)
    fun getTransactionsByPersonAndType(personId: Long, type: TransactionType): Flow<List<Transaction>> =
        transactionDao.getTransactionsByPersonAndType(personId, type)
    fun getTotalByPersonAndType(personId: Long, type: TransactionType): Flow<Double> =
        transactionDao.getTotalByPersonAndType(personId, type)
    fun getUnpaidTotalByPersonAndType(personId: Long, type: TransactionType): Flow<Double> =
        transactionDao.getUnpaidTotalByPersonAndType(personId, type)
    fun getPendingScheduledTotalByPersonAndType(personId: Long, type: ScheduledNoteType): Flow<Double> =
        scheduledNoteDao.getPendingTotalByPersonAndType(personId, type)
    suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction)

    // Note
    fun getNotesByPerson(personId: Long): Flow<List<Note>> = noteDao.getNotesByPerson(personId)
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    // Scheduled Note
    fun getScheduledNotesByPerson(personId: Long): Flow<List<ScheduledNote>> =
        scheduledNoteDao.getScheduledNotesByPerson(personId)
    fun getUpcomingByType(type: ScheduledNoteType): Flow<List<ScheduledNote>> =
        scheduledNoteDao.getUpcomingByType(type)
    fun getAllUpcoming(): Flow<List<ScheduledNote>> = scheduledNoteDao.getAllUpcoming()
    fun getUpcomingFromDate(startOfDay: Long): Flow<List<ScheduledNote>> =
        scheduledNoteDao.getUpcomingFromDate(startOfDay)
    suspend fun insertScheduledNote(scheduledNote: ScheduledNote): Long =
        scheduledNoteDao.insertScheduledNote(scheduledNote)
    suspend fun updateScheduledNote(scheduledNote: ScheduledNote) =
        scheduledNoteDao.updateScheduledNote(scheduledNote)
    suspend fun deleteScheduledNote(scheduledNote: ScheduledNote) =
        scheduledNoteDao.deleteScheduledNote(scheduledNote)

    // General Note
    fun getAllGeneralNotes(): Flow<List<GeneralNote>> = generalNoteDao.getAllNotes()
    suspend fun insertGeneralNote(note: GeneralNote): Long = generalNoteDao.insertNote(note)
    suspend fun updateGeneralNote(note: GeneralNote) = generalNoteDao.updateNote(note)
    suspend fun deleteGeneralNote(note: GeneralNote) = generalNoteDao.deleteNote(note)

    // General Transaction
    fun getAllGeneralTransactions(): Flow<List<GeneralTransaction>> =
        generalTransactionDao.getAllTransactions()
    fun getGeneralTransactionsByType(type: GeneralTransactionType): Flow<List<GeneralTransaction>> =
        generalTransactionDao.getTransactionsByType(type)
    fun getGeneralTotalByType(type: GeneralTransactionType): Flow<Double> =
        generalTransactionDao.getTotalByType(type)
    fun getUnpaidGeneralTotalByType(type: GeneralTransactionType): Flow<Double> =
        generalTransactionDao.getUnpaidTotalByType(type)
    fun getPendingGeneralScheduledTotalByType(type: ScheduledNoteType): Flow<Double> =
        generalScheduledNoteDao.getPendingTotalByType(type)
    suspend fun insertGeneralTransaction(transaction: GeneralTransaction): Long =
        generalTransactionDao.insertTransaction(transaction)
    suspend fun updateGeneralTransaction(transaction: GeneralTransaction) =
        generalTransactionDao.updateTransaction(transaction)
    suspend fun deleteGeneralTransaction(transaction: GeneralTransaction) =
        generalTransactionDao.deleteTransaction(transaction)

    // General Scheduled Note
    fun getAllGeneralScheduledNotes(): Flow<List<GeneralScheduledNote>> =
        generalScheduledNoteDao.getAllScheduledNotes()
    fun getGeneralUpcoming(): Flow<List<GeneralScheduledNote>> =
        generalScheduledNoteDao.getUpcoming()
    fun getGeneralUpcomingByType(type: ScheduledNoteType): Flow<List<GeneralScheduledNote>> =
        generalScheduledNoteDao.getUpcomingByType(type)
    suspend fun insertGeneralScheduledNote(scheduledNote: GeneralScheduledNote): Long =
        generalScheduledNoteDao.insertScheduledNote(scheduledNote)
    suspend fun updateGeneralScheduledNote(scheduledNote: GeneralScheduledNote) =
        generalScheduledNoteDao.updateScheduledNote(scheduledNote)
    suspend fun deleteGeneralScheduledNote(scheduledNote: GeneralScheduledNote) =
        generalScheduledNoteDao.deleteScheduledNote(scheduledNote)
}

package com.muhasebecim.data.dao

import androidx.room.*
import com.muhasebecim.data.model.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons ORDER BY id ASC")
    suspend fun getAllPersonsForBackup(): List<Person>

    @Query("SELECT * FROM persons WHERE id = :id")
    fun getPersonById(id: Long): Flow<Person?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersons(persons: List<Person>)

    @Update
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(person: Person)

    @Query("SELECT * FROM persons WHERE name LIKE '%' || :query || '%'")
    fun searchPersons(query: String): Flow<List<Person>>

    @Query("DELETE FROM persons")
    suspend fun deleteAllPersons()
}

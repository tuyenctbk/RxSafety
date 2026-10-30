package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MedicationRegimenEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for local CRUD operations on multi-drug medication regimens.
 */
@Dao
interface MedicationRegimenDao {

    @Query("SELECT * FROM medication_regimens ORDER BY isActive DESC, createdAt DESC")
    fun getAllRegimens(): Flow<List<MedicationRegimenEntity>>

    @Query("SELECT * FROM medication_regimens WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveRegimens(): Flow<List<MedicationRegimenEntity>>

    @Query("SELECT * FROM medication_regimens WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActiveRegimensOnce(): List<MedicationRegimenEntity>

    @Query("SELECT * FROM medication_regimens WHERE id = :id LIMIT 1")
    fun getRegimenById(id: Long): Flow<MedicationRegimenEntity?>

    @Query("SELECT * FROM medication_regimens WHERE id = :id LIMIT 1")
    suspend fun getRegimenByIdOnce(id: Long): MedicationRegimenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegimen(regimen: MedicationRegimenEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegimens(regimens: List<MedicationRegimenEntity>): List<Long>

    @Update
    suspend fun updateRegimen(regimen: MedicationRegimenEntity)

    @Delete
    suspend fun deleteRegimen(regimen: MedicationRegimenEntity)

    @Query("DELETE FROM medication_regimens WHERE id = :id")
    suspend fun deleteRegimenById(id: Long)

    @Query("UPDATE medication_regimens SET isActive = :isActive WHERE id = :id")
    suspend fun updateRegimenStatus(id: Long, isActive: Boolean)

    @Query("SELECT * FROM medication_regimens WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchRegimens(query: String): Flow<List<MedicationRegimenEntity>>

    @Query("SELECT COUNT(*) FROM medication_regimens WHERE isActive = 1")
    fun getActiveRegimenCount(): Flow<Int>
}

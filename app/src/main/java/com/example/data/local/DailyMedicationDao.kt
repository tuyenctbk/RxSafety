package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyMedicationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for local CRUD operations on the user's daily medication list.
 */
@Dao
interface DailyMedicationDao {

    @Query("SELECT * FROM daily_medications ORDER BY isActive DESC, scheduledTime ASC, name ASC")
    fun getAllDailyMedications(): Flow<List<DailyMedicationEntity>>

    @Query("SELECT * FROM daily_medications WHERE isActive = 1 ORDER BY scheduledTime ASC, name ASC")
    fun getActiveDailyMedications(): Flow<List<DailyMedicationEntity>>

    @Query("SELECT * FROM daily_medications WHERE isActive = 1 ORDER BY scheduledTime ASC, name ASC")
    suspend fun getActiveDailyMedicationsOnce(): List<DailyMedicationEntity>

    @Query("SELECT * FROM daily_medications WHERE id = :id LIMIT 1")
    fun getMedicationById(id: Long): Flow<DailyMedicationEntity?>

    @Query("SELECT * FROM daily_medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationByIdOnce(id: Long): DailyMedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: DailyMedicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<DailyMedicationEntity>): List<Long>

    @Update
    suspend fun updateMedication(medication: DailyMedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: DailyMedicationEntity)

    @Query("DELETE FROM daily_medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Long)

    @Query("UPDATE daily_medications SET isTakenToday = :isTaken, lastTakenTimestamp = :timestamp WHERE id = :id")
    suspend fun updateTakenStatus(id: Long, isTaken: Boolean, timestamp: Long?)

    @Query("UPDATE daily_medications SET isTakenToday = 0")
    suspend fun resetAllDailyTakenStatus()

    @Query("SELECT * FROM daily_medications WHERE name LIKE '%' || :query || '%' OR instructions LIKE '%' || :query || '%'")
    fun searchMedications(query: String): Flow<List<DailyMedicationEntity>>

    @Query("SELECT COUNT(*) FROM daily_medications WHERE isActive = 1")
    fun getActiveCount(): Flow<Int>
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MedicationEntity
import com.example.data.model.PatientProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RxSafetyDao {
    // Patient Profiles
    @Query("SELECT * FROM patient_profiles ORDER BY isDefault DESC, createdAt ASC")
    fun getAllProfiles(): Flow<List<PatientProfileEntity>>

    @Query("SELECT * FROM patient_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): PatientProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PatientProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: PatientProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: PatientProfileEntity)

    // Medications
    @Query("SELECT * FROM medications WHERE profileId = :profileId ORDER BY name ASC")
    fun getMedicationsForProfile(profileId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE profileId = :profileId ORDER BY name ASC")
    suspend fun getMedicationsForProfileOnce(profileId: Long): List<MedicationEntity>

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY name ASC")
    suspend fun getAllMedicationsOnce(): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("DELETE FROM medications WHERE profileId = :profileId")
    suspend fun deleteMedicationsForProfile(profileId: Long)
}

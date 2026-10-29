package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DailyMedicationDao
import com.example.data.local.RxSafetyDao
import com.example.data.local.RxSafetyDatabase
import com.example.data.model.DailyMedicationEntity
import com.example.data.model.PatientProfileEntity
import com.example.data.model.SeverityLevel
import com.example.data.repository.MedicationRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MedicationRepositoryTest {

    private lateinit var db: RxSafetyDatabase
    private lateinit var dailyDao: DailyMedicationDao
    private lateinit var profileDao: RxSafetyDao
    private lateinit var repository: MedicationRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RxSafetyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dailyDao = db.dailyMedicationDao()
        profileDao = db.rxSafetyDao()
        repository = MedicationRepository(dailyDao, profileDao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `test repository daily medication insert and adherence update`() = runBlocking {
        val med = DailyMedicationEntity(
            name = "Warfarin",
            dosage = "5 mg",
            frequency = "Once daily in evening",
            isTakenToday = false
        )

        val id = repository.insertDailyMedication(med)
        assertTrue("Inserted ID should be positive", id > 0)

        val activeMeds = repository.getActiveDailyMedicationsOnce()
        assertEquals(1, activeMeds.size)
        assertEquals("Warfarin", activeMeds.first().name)

        repository.updateDailyMedicationTaken(id, true)
        val updated = repository.getDailyMedicationByIdOnce(id)
        assertNotNull(updated)
        assertTrue("Should be marked taken", updated!!.isTakenToday)
    }

    @Test
    fun `test repository patient profile and audit execution`() = runBlocking {
        val profId = repository.insertProfile(
            PatientProfileEntity(
                name = "Eleanor Vance",
                relationship = "Mom",
                age = 72,
                gender = "Female",
                conditions = listOf("Atrial Fibrillation")
            )
        )
        assertTrue(profId > 0)

        // Perform unified safety audit
        val audit = repository.performUnifiedSafetyAudit(
            medicationNames = listOf("Warfarin", "Aspirin"),
            conditions = listOf("Atrial Fibrillation"),
            profileName = "Eleanor Vance"
        )

        assertNotNull(audit)
        assertEquals("Eleanor Vance", audit.profileName)
        assertTrue("Should detect Warfarin + Aspirin interaction", audit.drugInteractions.isNotEmpty())
        assertEquals(SeverityLevel.CRITICAL, audit.highestSeverity)
    }
}

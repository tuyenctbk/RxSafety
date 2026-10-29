package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.clinical.ClinicalInteractionDatabase
import com.example.data.clinical.DoseFlowBridge
import com.example.data.clinical.FoodInteractionDatabase
import com.example.data.local.DailyMedicationDao
import com.example.data.local.RxSafetyDatabase
import com.example.data.model.DailyMedicationEntity
import com.example.data.model.MedicationEntity
import com.example.data.model.PatientProfileEntity
import com.example.data.model.SeverityLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: RxSafetyDatabase
  private lateinit var dailyDao: DailyMedicationDao

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, RxSafetyDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dailyDao = db.dailyMedicationDao()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RxSafety", appName)
  }

  @Test
  fun `test daily medication entity insertion and query`() = runBlocking {
    val med = DailyMedicationEntity(
      name = "Lisinopril",
      dosage = "20 mg",
      frequency = "Once daily",
      dosageForm = "Tablet",
      scheduledTime = "08:00 AM",
      instructions = "Take with water",
      prescriber = "Dr. Smith",
      isActive = true,
      isTakenToday = false
    )

    val id = dailyDao.insertMedication(med)
    assertTrue("Generated ID should be positive", id > 0)

    val list = dailyDao.getActiveDailyMedicationsOnce()
    assertEquals(1, list.size)
    val fetched = list.first()
    assertEquals("Lisinopril", fetched.name)
    assertEquals("20 mg", fetched.dosage)
    assertFalse(fetched.isTakenToday)
  }

  @Test
  fun `test daily medication taken status update`() = runBlocking {
    val med = DailyMedicationEntity(
      name = "Metformin",
      dosage = "500 mg",
      isTakenToday = false
    )
    val id = dailyDao.insertMedication(med)

    // Mark as taken
    dailyDao.updateTakenStatus(id, true, System.currentTimeMillis())

    val updated = dailyDao.getMedicationByIdOnce(id)
    assertNotNull(updated)
    assertTrue("Medication should be marked as taken", updated!!.isTakenToday)
    assertNotNull(updated.lastTakenTimestamp)
  }

  @Test
  fun `test severe drug interaction detection`() {
    val meds = listOf("Warfarin", "Aspirin")
    val interactions = ClinicalInteractionDatabase.checkInteractions(meds)
    assertTrue("Should detect Warfarin + Aspirin interaction", interactions.isNotEmpty())
    assertEquals(SeverityLevel.CRITICAL, interactions.first().severity)
  }

  @Test
  fun `test food interaction detection`() {
    val meds = listOf("Atorvastatin")
    val foods = FoodInteractionDatabase.checkFoodInteractions(meds)
    assertTrue("Should detect Grapefruit with Atorvastatin", foods.any { it.foodName.contains("Grapefruit") })
  }

  @Test
  fun `test doseflow serialization`() {
    val profile = PatientProfileEntity(
      name = "Arthur Test",
      relationship = "Self",
      age = 68,
      gender = "Male",
      conditions = listOf("Hypertension")
    )
    val meds = listOf(
      MedicationEntity(
        profileId = 1,
        name = "Lisinopril",
        dosage = "10 mg",
        frequency = "Daily"
      )
    )
    val json = DoseFlowBridge.exportToJson(profile, meds)
    assertTrue(json.contains("DoseFlow-Sync-v1"))
    assertTrue(json.contains("Arthur Test"))

    val imported = DoseFlowBridge.importFromJson(json)
    assertEquals("Arthur Test", imported.patientName)
    assertEquals(1, imported.medications.size)
    assertEquals("Lisinopril", imported.medications.first().name)
  }
}

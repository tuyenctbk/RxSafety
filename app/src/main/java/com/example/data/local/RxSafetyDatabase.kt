package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyMedicationEntity
import com.example.data.model.MedicationEntity
import com.example.data.model.MedicationRegimenEntity
import com.example.data.model.PatientProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PatientProfileEntity::class,
        MedicationEntity::class,
        DailyMedicationEntity::class,
        MedicationRegimenEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RxSafetyDatabase : RoomDatabase() {
    abstract fun rxSafetyDao(): RxSafetyDao
    abstract fun dailyMedicationDao(): DailyMedicationDao
    abstract fun medicationRegimenDao(): MedicationRegimenDao

    companion object {
        @Volatile
        private var INSTANCE: RxSafetyDatabase? = null

        fun getDatabase(context: Context): RxSafetyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RxSafetyDatabase::class.java,
                    "rxsafety_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        populateInitialData(
                            dao = database.rxSafetyDao(),
                            dailyDao = database.dailyMedicationDao(),
                            regimenDao = database.medicationRegimenDao()
                        )
                    }
                }
            }

            private suspend fun populateInitialData(
                dao: RxSafetyDao,
                dailyDao: DailyMedicationDao,
                regimenDao: MedicationRegimenDao
            ) {
                // Seed Eleanor (Mom)
                val momId = dao.insertProfile(
                    PatientProfileEntity(
                        name = "Eleanor Vance",
                        relationship = "Mom",
                        age = 72,
                        gender = "Female",
                        conditions = listOf("Atrial Fibrillation", "Hypertension", "Osteoarthritis"),
                        allergies = listOf("Penicillin", "Sulfa drugs"),
                        isDefault = true
                    )
                )

                dao.insertMedication(
                    MedicationEntity(
                        profileId = momId,
                        name = "Warfarin",
                        dosage = "5 mg",
                        frequency = "Once daily in evening",
                        rxcui = "11289",
                        notes = "Check INR monthly, avoid sudden dietary vitamin K shifts"
                    )
                )
                dao.insertMedication(
                    MedicationEntity(
                        profileId = momId,
                        name = "Lisinopril",
                        dosage = "20 mg",
                        frequency = "Once daily in morning",
                        rxcui = "29046",
                        notes = "Monitor blood pressure and serum potassium"
                    )
                )
                dao.insertMedication(
                    MedicationEntity(
                        profileId = momId,
                        name = "Aspirin",
                        dosage = "81 mg",
                        frequency = "Once daily with food",
                        rxcui = "1191",
                        notes = "Low-dose cardio aspirin - high bleeding risk with Warfarin"
                    )
                )

                // Seed Self
                val selfId = dao.insertProfile(
                    PatientProfileEntity(
                        name = "Myself",
                        relationship = "Self",
                        age = 45,
                        gender = "Male",
                        conditions = listOf("Hyperlipidemia", "Pre-diabetes"),
                        allergies = emptyList(),
                        isDefault = false
                    )
                )

                dao.insertMedication(
                    MedicationEntity(
                        profileId = selfId,
                        name = "Atorvastatin",
                        dosage = "20 mg",
                        frequency = "Once daily at bedtime",
                        rxcui = "83367",
                        notes = "Statin therapy - avoid large amounts of grapefruit juice"
                    )
                )
                dao.insertMedication(
                    MedicationEntity(
                        profileId = selfId,
                        name = "Metformin",
                        dosage = "500 mg",
                        frequency = "Twice daily with meals",
                        rxcui = "6809",
                        notes = "Take with food to minimize GI upset"
                    )
                )

                // Seed user's initial Daily Medication list stored locally on device
                dailyDao.insertMedications(
                    listOf(
                        DailyMedicationEntity(
                            name = "Lisinopril",
                            dosage = "20 mg",
                            frequency = "Once daily",
                            dosageForm = "Tablet",
                            scheduledTime = "08:00 AM",
                            scheduleTimes = listOf("Morning"),
                            rxcui = "29046",
                            instructions = "Take with water every morning. Check blood pressure regularly.",
                            prescriber = "Dr. Sarah Jenkins (Cardiology)",
                            isActive = true,
                            isTakenToday = true,
                            lastTakenTimestamp = System.currentTimeMillis() - 3600000,
                            notes = "Avoid potassium-based salt substitutes."
                        ),
                        DailyMedicationEntity(
                            name = "Metformin",
                            dosage = "500 mg",
                            frequency = "Twice daily",
                            dosageForm = "Tablet",
                            scheduledTime = "08:00 AM",
                            scheduleTimes = listOf("Morning", "Evening"),
                            rxcui = "6809",
                            instructions = "Take with meals to minimize stomach upset.",
                            prescriber = "Dr. Michael Torres (Internal Med)",
                            isActive = true,
                            isTakenToday = false,
                            lastTakenTimestamp = null,
                            notes = "Withhold 48 hours prior to iodinated radiocontrast scans."
                        ),
                        DailyMedicationEntity(
                            name = "Atorvastatin",
                            dosage = "20 mg",
                            frequency = "Once daily at bedtime",
                            dosageForm = "Tablet",
                            scheduledTime = "09:00 PM",
                            scheduleTimes = listOf("Bedtime"),
                            rxcui = "83367",
                            instructions = "Take at bedtime. Avoid large amounts of grapefruit juice.",
                            prescriber = "Dr. Sarah Jenkins (Cardiology)",
                            isActive = true,
                            isTakenToday = false,
                            lastTakenTimestamp = null,
                            notes = "Report unexplained muscle pain or tenderness."
                        )
                    )
                )

                // Seed initial structured clinical regimens
                regimenDao.insertRegimens(
                    listOf(
                        MedicationRegimenEntity(
                            name = "Cardiovascular & Lipid Control",
                            patientName = "Myself",
                            description = "Standard primary cardiovascular prevention and glycemic control regimen.",
                            medicationNames = listOf("Lisinopril", "Metformin", "Atorvastatin"),
                            targetConditions = listOf("Hypertension", "Hyperlipidemia", "Type 2 Diabetes"),
                            scheduleSummary = "Twice Daily (Morning & Evening)",
                            isActive = true,
                            prescriber = "Dr. Sarah Jenkins",
                            notes = "Monitor blood pressure weekly and periodic HbA1c."
                        ),
                        MedicationRegimenEntity(
                            name = "Anticoagulation & Blood Pressure Regimen",
                            patientName = "Eleanor Vance",
                            description = "Atrial fibrillation thromboembolism prophylaxis with antihypertensive coverage.",
                            medicationNames = listOf("Warfarin", "Lisinopril", "Aspirin"),
                            targetConditions = listOf("Atrial Fibrillation", "Hypertension"),
                            scheduleSummary = "Daily (Evening INR dose)",
                            isActive = true,
                            prescriber = "Memorial Heart Center",
                            notes = "Requires monthly INR lab monitoring; watch for bleeding signs."
                        )
                    )
                )
            }
        }
    }
}

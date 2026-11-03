package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.DailyMedicationEntity
import com.example.data.model.MedicationEntity
import com.example.data.model.MedicationRegimenEntity
import com.example.data.model.PatientProfileEntity

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
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

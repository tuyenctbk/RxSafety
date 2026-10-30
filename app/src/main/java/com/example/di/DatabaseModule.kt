package com.example.di

import android.content.Context
import com.example.data.local.DailyMedicationDao
import com.example.data.local.MedicationRegimenDao
import com.example.data.local.RxSafetyDao
import com.example.data.local.RxSafetyDatabase

object DatabaseModule {

    fun provideRxSafetyDatabase(context: Context): RxSafetyDatabase {
        return RxSafetyDatabase.getDatabase(context)
    }

    fun provideRxSafetyDao(context: Context): RxSafetyDao {
        return provideRxSafetyDatabase(context).rxSafetyDao()
    }

    fun provideDailyMedicationDao(context: Context): DailyMedicationDao {
        return provideRxSafetyDatabase(context).dailyMedicationDao()
    }

    fun provideMedicationRegimenDao(context: Context): MedicationRegimenDao {
        return provideRxSafetyDatabase(context).medicationRegimenDao()
    }
}


package com.example.di

import android.content.Context
import com.example.data.donation.DonationManager
import com.example.data.repository.MedicationRepository

object RepositoryModule {

    fun provideMedicationRepository(context: Context): MedicationRepository {
        val dailyDao = DatabaseModule.provideDailyMedicationDao(context)
        val safetyDao = DatabaseModule.provideRxSafetyDao(context)
        val rxNavApi = NetworkModule.provideRxNavApi()
        val openFdaApi = NetworkModule.provideOpenFdaApi()

        return MedicationRepository(
            dailyMedicationDao = dailyDao,
            rxSafetyDao = safetyDao,
            rxNavApi = rxNavApi,
            openFdaApi = openFdaApi
        )
    }

    fun provideDonationManager(context: Context): DonationManager {
        return DonationManager(context.applicationContext)
    }
}

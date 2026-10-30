package com.example.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    fun scheduleMedicationReminders() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        // Daily periodic reminder check
        val periodicWorkRequest = PeriodicWorkRequestBuilder<MedicationReminderWorker>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS // flex window
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MedicationReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    fun triggerImmediateReminderCheck() {
        val immediateRequest = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
            .build()
        WorkManager.getInstance(context).enqueue(immediateRequest)
    }
}

package com.example.workers

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {
    fun scheduleMedicationReminders() {
        val workRequest = PeriodicWorkRequestBuilder<MedicationReminderWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}

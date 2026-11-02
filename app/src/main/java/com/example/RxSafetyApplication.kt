package com.example

import android.app.Application

class RxSafetyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            com.example.workers.ReminderScheduler(this).scheduleMedicationReminders()
        } catch (e: Throwable) {
            android.util.Log.w("RxSafetyApplication", "Could not schedule reminders on startup: ${e.message}")
        }
    }
}

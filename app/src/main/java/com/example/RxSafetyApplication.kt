package com.example

import android.app.Application

class RxSafetyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.workers.ReminderScheduler(this).scheduleMedicationReminders()
    }
}

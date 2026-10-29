package com.example.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.R

class MedicationReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = com.example.data.local.RxSafetyDatabase.getDatabase(applicationContext)
        val medications = database.dailyMedicationDao().getActiveDailyMedicationsOnce()
        medications.forEach { medication ->
            // In a real app, you would check the time here.
            // For this implementation, we will just notify for the first one as an example.
        }
        if (medications.isNotEmpty()) {
            showNotification(medications[0].name)
        }
        return Result.success()
    }

    private fun showNotification(medName: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_reminders"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time to take your medication")
            .setContentText("It's time to take your $medName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(medName.hashCode(), notification)
    }
}

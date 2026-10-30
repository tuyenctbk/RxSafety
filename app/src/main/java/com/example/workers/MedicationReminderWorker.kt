package com.example.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.local.RxSafetyDatabase

class MedicationReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = RxSafetyDatabase.getDatabase(applicationContext)
        val dailyDao = database.dailyMedicationDao()
        val activeMedications = dailyDao.getActiveDailyMedicationsOnce()

        // Filter for active medications that have not been taken today yet
        val pendingMeds = activeMedications.filter { it.isActive && !it.isTakenToday }

        if (pendingMeds.isNotEmpty()) {
            val title = applicationContext.getString(R.string.reminder_notification_title)
            val channelName = applicationContext.getString(R.string.notification_channel_name)
            val channelDesc = applicationContext.getString(R.string.notification_channel_desc)

            createNotificationChannel(channelName, channelDesc)

            if (pendingMeds.size == 1) {
                val med = pendingMeds.first()
                val body = applicationContext.getString(
                    R.string.reminder_notification_text,
                    "${med.name} (${med.dosage})"
                )
                showNotification(med.id.toInt(), title, body)
            } else {
                val summary = applicationContext.getString(
                    R.string.reminder_notification_multiple,
                    pendingMeds.size
                )
                val medNames = pendingMeds.joinToString(", ") { "${it.name} (${it.dosage})" }
                showNotification(NOTIFICATION_SUMMARY_ID, title, "$summary: $medNames")
            }
        }

        return Result.success()
    }

    private fun createNotificationChannel(name: String, description: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.description = description
                enableVibration(true)
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(id: Int, title: String, content: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "medication_reminders_channel"
        const val WORK_NAME = "DailyMedicationReminderWork"
        private const val NOTIFICATION_SUMMARY_ID = 9001
    }
}

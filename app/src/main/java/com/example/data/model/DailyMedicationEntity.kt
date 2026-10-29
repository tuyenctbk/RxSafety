package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an item in the user's daily medication list stored locally on their device.
 */
@Entity(tableName = "daily_medications")
data class DailyMedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                              // Medication brand/generic name (e.g. "Lisinopril")
    val dosage: String,                            // Dosage strength (e.g. "10 mg", "500 mg")
    val frequency: String = "Once daily",          // Frequency (e.g. "Once daily", "Twice daily", "As needed")
    val dosageForm: String = "Tablet",             // Formulation (Tablet, Capsule, Liquid, Inhaler, Injection, Drops)
    val scheduledTime: String = "08:00 AM",        // Primary time of day (e.g. "08:00 AM", "08:00 PM")
    val scheduleTimes: List<String> = listOf("Morning"), // Schedule slots: Morning, Noon, Evening, Bedtime
    val rxcui: String? = null,                     // NIH RxNorm concept identifier for drug interaction verification
    val instructions: String = "",                 // Specific taking advice (e.g. "Take with food", "Avoid grapefruit")
    val prescriber: String = "",                   // Prescribing doctor or clinic
    val isActive: Boolean = true,                  // Active in current daily regimen
    val isTakenToday: Boolean = false,             // Daily adherence checklist flag
    val lastTakenTimestamp: Long? = null,          // Timestamp when marked as taken
    val notes: String = "",                        // Patient's personal notes
    val createdAt: Long = System.currentTimeMillis()
)

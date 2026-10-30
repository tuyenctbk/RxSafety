package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a comprehensive user medication regimen stored locally on the device.
 * Groups multi-drug protocols, clinical target conditions, schedule intervals, and adherence notes.
 */
@Entity(tableName = "medication_regimens")
data class MedicationRegimenEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                                      // Regimen title (e.g. "Cardiovascular Protocol", "Daily Vitality")
    val patientName: String = "Myself",                    // Associated patient or profile name
    val description: String = "",                          // Clinical notes or physician instructions
    val medicationNames: List<String> = emptyList(),       // List of medication names in this regimen
    val targetConditions: List<String> = emptyList(),      // Diagnostic indications (e.g. "Hypertension", "Dyslipidemia")
    val scheduleSummary: String = "Daily",                 // Schedule frequency (e.g. "Once daily", "Twice daily with meals")
    val isActive: Boolean = true,                          // Whether regimen is currently active
    val startDate: Long = System.currentTimeMillis(),      // Treatment course start date
    val endDate: Long? = null,                             // Optional treatment course completion date
    val prescriber: String = "",                           // Prescribing physician or clinic
    val notes: String = "",                                // Patient personal regimen notes
    val createdAt: Long = System.currentTimeMillis()
)

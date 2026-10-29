package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SeverityLevel(val displayName: String, val sortOrder: Int) {
    CRITICAL("Severe / High Risk", 1),
    MODERATE("Moderate Risk", 2),
    LOW("Minor / Caution", 3),
    SAFE("No Known Conflict", 4)
}

data class DrugInteractionItem(
    val id: String,
    val drugA: String,
    val drugB: String,
    val severity: SeverityLevel,
    val title: String,
    val mechanism: String,
    val clinicalEffect: String,
    val recommendation: String,
    val source: String = "NIH RxNav / ONC High Priority Clinical Knowledge Base"
)

data class FoodInteractionItem(
    val id: String,
    val foodName: String,
    val drugName: String,
    val severity: SeverityLevel,
    val mechanism: String,
    val recommendation: String
)

data class ConditionContraindication(
    val condition: String,
    val drugName: String,
    val severity: SeverityLevel,
    val reason: String
)

data class DrugRecall(
    val recallNumber: String,
    val productDescription: String,
    val reasonForRecall: String,
    val classification: String, // Class I, Class II, Class III
    val status: String, // Ongoing, Terminated, Completed
    val recallingFirm: String,
    val reportDate: String,
    val distributionPattern: String,
    val voluntaryMandated: String
)

data class DrugLabelInfo(
    val brandName: String,
    val genericName: String,
    val boxedWarning: String?,
    val warnings: String?,
    val adverseReactions: String?,
    val indications: String?
)

data class RxNavCandidate(
    val rxcui: String,
    val name: String,
    val score: String? = null
)

data class RxNavDrugDetail(
    val rxcui: String,
    val name: String,
    val synonym: String? = null,
    val termType: String = "SCD",
    val termTypeLabel: String = "Clinical Drug",
    val score: String? = null,
    val isStagedForCheck: Boolean = false
)

@Entity(tableName = "patient_profiles")
data class PatientProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relationship: String, // Self, Parent, Spouse, Child, Ward
    val age: Int,
    val gender: String, // Female, Male, Other
    val conditions: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val name: String,
    val dosage: String, // e.g. "10 mg"
    val frequency: String, // e.g. "Once daily in morning"
    val rxcui: String? = null,
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

data class AuditResult(
    val profileName: String?,
    val evaluatedDrugs: List<String>,
    val drugInteractions: List<DrugInteractionItem>,
    val foodInteractions: List<FoodInteractionItem>,
    val conditionContraindications: List<ConditionContraindication>,
    val activeRecalls: List<DrugRecall>,
    val highestSeverity: SeverityLevel,
    val timestamp: Long = System.currentTimeMillis()
)

// DoseFlow Integration Payload format
data class DoseFlowProfileSync(
    val appIdentifier: String = "DoseFlow-Sync-v1",
    val patientName: String,
    val patientAge: Int,
    val conditions: List<String>,
    val allergies: List<String>,
    val medications: List<DoseFlowMedicationSync>,
    val exportTimestamp: Long = System.currentTimeMillis()
)

data class DoseFlowMedicationSync(
    val name: String,
    val dosage: String,
    val frequency: String,
    val rxcui: String?,
    val notes: String
)

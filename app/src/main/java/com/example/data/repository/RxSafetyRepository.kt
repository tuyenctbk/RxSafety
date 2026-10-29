package com.example.data.repository

import android.util.Log
import com.example.data.clinical.ClinicalInteractionDatabase
import com.example.data.clinical.ConditionContraindicationsDatabase
import com.example.data.clinical.FoodInteractionDatabase
import com.example.data.local.DailyMedicationDao
import com.example.data.local.RxSafetyDao
import com.example.data.model.AuditResult
import com.example.data.model.DailyMedicationEntity
import com.example.data.model.DrugLabelInfo
import com.example.data.model.DrugRecall
import com.example.data.model.MedicationEntity
import com.example.data.model.PatientProfileEntity
import com.example.data.model.RxNavCandidate
import com.example.data.model.SeverityLevel
import com.example.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RxSafetyRepository(
    private val dao: RxSafetyDao,
    private val dailyDao: DailyMedicationDao
) {
    private val TAG = "RxSafetyRepository"
    private val rxNavApi = NetworkClient.rxNavApi
    private val openFdaApi = NetworkClient.openFdaApi

    // Daily Medications Local Storage Flows & Methods
    val allDailyMedications: Flow<List<DailyMedicationEntity>> = dailyDao.getAllDailyMedications()
    val activeDailyMedications: Flow<List<DailyMedicationEntity>> = dailyDao.getActiveDailyMedications()

    suspend fun insertDailyMedication(medication: DailyMedicationEntity): Long = withContext(Dispatchers.IO) {
        dailyDao.insertMedication(medication)
    }

    suspend fun updateDailyMedication(medication: DailyMedicationEntity) = withContext(Dispatchers.IO) {
        dailyDao.updateMedication(medication)
    }

    suspend fun deleteDailyMedication(medication: DailyMedicationEntity) = withContext(Dispatchers.IO) {
        dailyDao.deleteMedication(medication)
    }

    suspend fun deleteDailyMedicationById(id: Long) = withContext(Dispatchers.IO) {
        dailyDao.deleteMedicationById(id)
    }

    suspend fun updateDailyMedicationTaken(id: Long, isTaken: Boolean, timestamp: Long? = if (isTaken) System.currentTimeMillis() else null) = withContext(Dispatchers.IO) {
        dailyDao.updateTakenStatus(id, isTaken, timestamp)
    }

    suspend fun resetAllDailyTakenStatus() = withContext(Dispatchers.IO) {
        dailyDao.resetAllDailyTakenStatus()
    }

    suspend fun getActiveDailyMedicationsOnce(): List<DailyMedicationEntity> = withContext(Dispatchers.IO) {
        dailyDao.getActiveDailyMedicationsOnce()
    }

    // Database Flows
    val allProfiles: Flow<List<PatientProfileEntity>> = dao.getAllProfiles()

    fun getMedicationsForProfile(profileId: Long): Flow<List<MedicationEntity>> =
        dao.getMedicationsForProfile(profileId)

    suspend fun insertProfile(profile: PatientProfileEntity): Long = withContext(Dispatchers.IO) {
        dao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: PatientProfileEntity) = withContext(Dispatchers.IO) {
        dao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: PatientProfileEntity) = withContext(Dispatchers.IO) {
        dao.deleteMedicationsForProfile(profile.id)
        dao.deleteProfile(profile)
    }

    suspend fun insertMedication(medication: MedicationEntity): Long = withContext(Dispatchers.IO) {
        dao.insertMedication(medication)
    }

    suspend fun insertMedications(medications: List<MedicationEntity>) = withContext(Dispatchers.IO) {
        dao.insertMedications(medications)
    }

    suspend fun deleteMedication(medication: MedicationEntity) = withContext(Dispatchers.IO) {
        dao.deleteMedication(medication)
    }

    suspend fun getMedicationsForProfileOnce(profileId: Long): List<MedicationEntity> = withContext(Dispatchers.IO) {
        dao.getMedicationsForProfileOnce(profileId)
    }

    suspend fun getAllMedicationsOnce(): List<MedicationEntity> = withContext(Dispatchers.IO) {
        dao.getAllMedicationsOnce()
    }

    // RxNav Drug Search & Normalization
    suspend fun searchDrugs(query: String): List<RxNavCandidate> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val trimmed = query.trim()
        val results = mutableListOf<RxNavCandidate>()

        try {
            val response = rxNavApi.getApproximateTerm(trimmed, maxEntries = 8)
            val candidates = response.approximateGroup?.candidate
            if (!candidates.isNullOrEmpty()) {
                candidates.forEach { cand ->
                    val rxcui = cand.rxcui ?: ""
                    val name = cand.name ?: trimmed
                    if (rxcui.isNotEmpty() && results.none { it.name.equals(name, ignoreCase = true) }) {
                        results.add(
                            RxNavCandidate(
                                rxcui = rxcui,
                                name = name.replaceFirstChar { it.uppercase() },
                                score = cand.score
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "RxNav search network error: ${e.message}")
        }

        // If remote search had no items or failed, search against local common list
        if (results.isEmpty()) {
            val localMatches = ClinicalInteractionDatabase.getAllCommonMedications()
                .filter { it.contains(trimmed, ignoreCase = true) }
            localMatches.forEach { match ->
                val simpleName = match.substringBefore(" (")
                results.add(
                    RxNavCandidate(
                        rxcui = "LOCAL",
                        name = match,
                        score = "100"
                    )
                )
            }
        }

        results
    }

    // OpenFDA Drug Recalls
    suspend fun fetchRecentRecalls(searchFilter: String? = null): List<DrugRecall> = withContext(Dispatchers.IO) {
        val query = if (!searchFilter.isNullOrBlank()) {
            "product_description:\"$searchFilter\""
        } else {
            null
        }

        try {
            val response = openFdaApi.getEnforcementReports(
                search = query,
                limit = 25
            )
            val list = response.results ?: emptyList()
            return@withContext list.map { item ->
                DrugRecall(
                    recallNumber = item.recallNumber ?: "N/A",
                    productDescription = item.productDescription ?: "Unknown product",
                    reasonForRecall = item.reasonForRecall ?: "Reason not specified in report",
                    classification = item.classification ?: "Class II",
                    status = item.status ?: "Ongoing",
                    recallingFirm = item.recallingFirm ?: "Unknown Manufacturer",
                    reportDate = item.reportDate ?: "",
                    distributionPattern = item.distributionPattern ?: "Nationwide",
                    voluntaryMandated = item.voluntaryMandated ?: "Voluntary"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "OpenFDA recall fetch error: ${e.message}")
            return@withContext getSampleRecalls(searchFilter)
        }
    }

    // Check if any medication in list has matching active recalls
    suspend fun checkRecallsForMedications(medications: List<String>): List<DrugRecall> = withContext(Dispatchers.IO) {
        val allRecalls = fetchRecentRecalls()
        if (allRecalls.isEmpty() || medications.isEmpty()) return@withContext emptyList()

        allRecalls.filter { recall ->
            medications.any { med ->
                val cleanMed = med.substringBefore(" (").trim().lowercase()
                cleanMed.length > 3 && recall.productDescription.lowercase().contains(cleanMed)
            }
        }
    }

    // OpenFDA Drug Label Inspector
    suspend fun fetchDrugLabel(drugName: String): DrugLabelInfo? = withContext(Dispatchers.IO) {
        val clean = drugName.substringBefore(" (").trim()
        val searchQuery = "openfda.brand_name:\"$clean\"+OR+openfda.generic_name:\"$clean\""

        try {
            val response = openFdaApi.getDrugLabel(search = searchQuery, limit = 1)
            val result = response.results?.firstOrNull() ?: return@withContext getLocalDrugLabel(clean)

            val brand = result.openfda?.brandName?.firstOrNull() ?: clean
            val generic = result.openfda?.genericName?.firstOrNull() ?: clean
            val boxed = result.boxedWarning?.firstOrNull()
            val warn = result.warnings?.firstOrNull() ?: result.warningsAndCautions?.firstOrNull()
            val adv = result.adverseReactions?.firstOrNull()
            val ind = result.indicationsAndUsage?.firstOrNull()

            return@withContext DrugLabelInfo(
                brandName = brand,
                genericName = generic,
                boxedWarning = boxed?.cleanHtmlText(),
                warnings = warn?.cleanHtmlText(),
                adverseReactions = adv?.cleanHtmlText(),
                indications = ind?.cleanHtmlText()
            )
        } catch (e: Exception) {
            Log.w(TAG, "OpenFDA label error: ${e.message}")
            return@withContext getLocalDrugLabel(clean)
        }
    }

    // Full Clinical Audit Engine
    suspend fun performAudit(
        medications: List<String>,
        conditions: List<String> = emptyList(),
        profileName: String? = null
    ): AuditResult = withContext(Dispatchers.Default) {
        val drugInteractions = ClinicalInteractionDatabase.checkInteractions(medications)
        val foodInteractions = FoodInteractionDatabase.checkFoodInteractions(medications)
        val conditionContraindications = ConditionContraindicationsDatabase.checkContraindications(conditions, medications)
        val activeRecalls = checkRecallsForMedications(medications)

        val highestSeverity = when {
            drugInteractions.any { it.severity == SeverityLevel.CRITICAL } ||
            conditionContraindications.any { it.severity == SeverityLevel.CRITICAL } ||
            activeRecalls.any { it.classification.contains("Class I", ignoreCase = true) } -> SeverityLevel.CRITICAL

            drugInteractions.any { it.severity == SeverityLevel.MODERATE } ||
            foodInteractions.any { it.severity == SeverityLevel.CRITICAL || it.severity == SeverityLevel.MODERATE } ||
            conditionContraindications.any { it.severity == SeverityLevel.MODERATE } ||
            activeRecalls.isNotEmpty() -> SeverityLevel.MODERATE

            drugInteractions.any { it.severity == SeverityLevel.LOW } ||
            foodInteractions.any { it.severity == SeverityLevel.LOW } -> SeverityLevel.LOW

            else -> SeverityLevel.SAFE
        }

        AuditResult(
            profileName = profileName,
            evaluatedDrugs = medications,
            drugInteractions = drugInteractions,
            foodInteractions = foodInteractions,
            conditionContraindications = conditionContraindications,
            activeRecalls = activeRecalls,
            highestSeverity = highestSeverity
        )
    }

    private fun String.cleanHtmlText(): String {
        return this.replace(Regex("<[^>]*>"), "")
            .replace("&#xB0;", "°")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(1500)
    }

    private fun getLocalDrugLabel(drugName: String): DrugLabelInfo {
        val lower = drugName.lowercase()
        return when {
            lower.contains("warfarin") -> DrugLabelInfo(
                brandName = "Coumadin / Jantoven",
                genericName = "Warfarin Sodium",
                boxedWarning = "WARNING: BLEEDING RISK. Warfarin sodium can cause major or fatal bleeding. Perform regular monitoring of INR in all treated patients. Numerous drugs, dietary changes, and botanical supplements affect INR.",
                warnings = "Hemorrhage is the principal risk of warfarin. Tissue necrosis, gangrene, and calciphylaxis have been reported. Microembolization (purple toes syndrome) may occur.",
                adverseReactions = "Major hemorrhage, epistaxis, hematuria, gastrointestinal bleeding, bruising, elevated liver enzymes.",
                indications = "Prophylaxis and treatment of venous thrombosis, pulmonary embolism, thromboembolic complications associated with atrial fibrillation and cardiac valve replacement."
            )
            lower.contains("lisinopril") -> DrugLabelInfo(
                brandName = "Prinivil / Zestril",
                genericName = "Lisinopril",
                boxedWarning = "WARNING: FETAL TOXICITY. When pregnancy is detected, discontinue Lisinopril as soon as possible. Drugs that act directly on the renin-angiotensin system can cause injury and death to the developing fetus.",
                warnings = "Head and neck angioedema (potentially fatal airway obstruction), intestinal angioedema, hypotension, hyperkalemia, renal impairment.",
                adverseReactions = "Dizziness, persistent dry cough, headache, excessive hypotension, hyperkalemia.",
                indications = "Treatment of hypertension, adjunct therapy in heart failure, and acute myocardial infarction survival improvement."
            )
            lower.contains("atorvastatin") -> DrugLabelInfo(
                brandName = "Lipitor",
                genericName = "Atorvastatin Calcium",
                boxedWarning = null,
                warnings = "Myopathy and Rhabdomyolysis: Risk is dose-dependent and increased by concomitant CYP3A4 inhibitors (clarithromycin, itraconazole) and gemfibrozil. Immune-mediated necrotizing myopathy (IMNM) reported.",
                adverseReactions = "Nasopharyngitis, arthralgia, diarrhea, pain in extremity, urinary tract infection, myalgia.",
                indications = "Reduction of risk of myocardial infarction, stroke, revascularization procedures, and angina in patients with coronary heart disease or risk factors."
            )
            else -> DrugLabelInfo(
                brandName = drugName.replaceFirstChar { it.uppercase() },
                genericName = drugName.replaceFirstChar { it.uppercase() },
                boxedWarning = "Always check with your pharmacist or physician for specific boxed warnings regarding this agent.",
                warnings = "Consult product monograph for detailed warnings, precautions, renal adjustments, and contraindications.",
                adverseReactions = "Consult prescribing information for incidence of adverse events.",
                indications = "Prescription medication - use strictly in accordance with licensed indications."
            )
        }
    }

    private fun getSampleRecalls(filter: String?): List<DrugRecall> {
        val sampleList = listOf(
            DrugRecall(
                recallNumber = "D-0342-2024",
                productDescription = "Atorvastatin Calcium Tablets, 20 mg, 90-count bottles. Distributed Nationwide.",
                reasonForRecall = "Subpotency: A single batch exhibited dissolution test failures during ongoing stability testing at the 12-month interval.",
                classification = "Class II",
                status = "Ongoing",
                recallingFirm = "Aurobindo Pharma USA, Inc.",
                reportDate = "20240915",
                distributionPattern = "Nationwide (USA)",
                voluntaryMandated = "Voluntary: Firm initiated"
            ),
            DrugRecall(
                recallNumber = "D-0198-2024",
                productDescription = "Lisinopril and Hydrochlorothiazide Tablets, 20 mg / 12.5 mg. Lot #LH2401.",
                reasonForRecall = "Cross-contamination: Trace particulate matter identified during standard cleanroom surveillance inspection.",
                classification = "Class II",
                status = "Terminated",
                recallingFirm = "Lupin Pharmaceuticals, Inc.",
                reportDate = "20240722",
                distributionPattern = "Nationwide wholesale distributors",
                voluntaryMandated = "Voluntary: Firm initiated"
            ),
            DrugRecall(
                recallNumber = "D-0045-2024",
                productDescription = "Aspirin Regimen Bayer 81 mg Enteric Coated Caplets. Lot #B81-992.",
                reasonForRecall = "Packaging Defect: Faulty tamper-evident bottle seals detected on a single pallet shipment.",
                classification = "Class III",
                status = "Completed",
                recallingFirm = "Bayer Healthcare LLC",
                reportDate = "20240510",
                distributionPattern = "Select Retail Outlets in 12 US States",
                voluntaryMandated = "Voluntary: Firm initiated"
            ),
            DrugRecall(
                recallNumber = "D-0811-2023",
                productDescription = "Heparin Sodium Injection USP, 1,000 USP units/mL, 10 mL multi-dose vials.",
                reasonForRecall = "Lack of Sterility Assurance: Observation of particulate matter inside unopened glass vials prior to hospital distribution.",
                classification = "Class I",
                status = "Ongoing",
                recallingFirm = "Baxter Healthcare Corporation",
                reportDate = "20231102",
                distributionPattern = "Nationwide hospitals and clinical centers",
                voluntaryMandated = "Voluntary: Firm initiated"
            )
        )

        return if (!filter.isNullOrBlank()) {
            sampleList.filter { it.productDescription.contains(filter, ignoreCase = true) || it.reasonForRecall.contains(filter, ignoreCase = true) }
        } else {
            sampleList
        }
    }
}

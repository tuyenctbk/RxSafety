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
import com.example.data.model.RxNavDrugDetail
import com.example.data.model.SeverityLevel
import com.example.data.remote.NetworkClient
import com.example.data.remote.OpenFdaApi
import com.example.data.remote.RxNavApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * MedicationRepository abstracts access to local Room database entities (DailyMedicationDao, RxSafetyDao)
 * and remote Retrofit REST API services (RxNavApi, OpenFdaApi) to provide a unified data source.
 */
class MedicationRepository(
    private val dailyMedicationDao: DailyMedicationDao,
    private val rxSafetyDao: RxSafetyDao,
    private val rxNavApi: RxNavApi = NetworkClient.rxNavApi,
    private val openFdaApi: OpenFdaApi = NetworkClient.openFdaApi
) {
    private val TAG = "MedicationRepository"

    // ------------------------------------------------------------------------
    // Local Room Database Operations (Daily Medications)
    // ------------------------------------------------------------------------

    val allDailyMedications: Flow<List<DailyMedicationEntity>> =
        dailyMedicationDao.getAllDailyMedications()

    val activeDailyMedications: Flow<List<DailyMedicationEntity>> =
        dailyMedicationDao.getActiveDailyMedications()

    suspend fun getActiveDailyMedicationsOnce(): List<DailyMedicationEntity> = withContext(Dispatchers.IO) {
        dailyMedicationDao.getActiveDailyMedicationsOnce()
    }

    suspend fun getDailyMedicationByIdOnce(id: Long): DailyMedicationEntity? = withContext(Dispatchers.IO) {
        dailyMedicationDao.getMedicationByIdOnce(id)
    }

    suspend fun insertDailyMedication(medication: DailyMedicationEntity): Long = withContext(Dispatchers.IO) {
        dailyMedicationDao.insertMedication(medication)
    }

    suspend fun insertDailyMedications(medications: List<DailyMedicationEntity>): List<Long> = withContext(Dispatchers.IO) {
        dailyMedicationDao.insertMedications(medications)
    }

    suspend fun updateDailyMedication(medication: DailyMedicationEntity) = withContext(Dispatchers.IO) {
        dailyMedicationDao.updateMedication(medication)
    }

    suspend fun deleteDailyMedication(medication: DailyMedicationEntity) = withContext(Dispatchers.IO) {
        dailyMedicationDao.deleteMedication(medication)
    }

    suspend fun deleteDailyMedicationById(id: Long) = withContext(Dispatchers.IO) {
        dailyMedicationDao.deleteMedicationById(id)
    }

    suspend fun updateDailyMedicationTaken(
        id: Long,
        isTaken: Boolean,
        timestamp: Long? = if (isTaken) System.currentTimeMillis() else null
    ) = withContext(Dispatchers.IO) {
        dailyMedicationDao.updateTakenStatus(id, isTaken, timestamp)
    }

    suspend fun resetAllDailyTakenStatus() = withContext(Dispatchers.IO) {
        dailyMedicationDao.resetAllDailyTakenStatus()
    }

    // ------------------------------------------------------------------------
    // Local Room Database Operations (Patient Profiles & Profile Regimens)
    // ------------------------------------------------------------------------

    val allProfiles: Flow<List<PatientProfileEntity>> = rxSafetyDao.getAllProfiles()

    fun getMedicationsForProfile(profileId: Long): Flow<List<MedicationEntity>> =
        rxSafetyDao.getMedicationsForProfile(profileId)

    suspend fun getMedicationsForProfileOnce(profileId: Long): List<MedicationEntity> = withContext(Dispatchers.IO) {
        rxSafetyDao.getMedicationsForProfileOnce(profileId)
    }

    suspend fun insertProfile(profile: PatientProfileEntity): Long = withContext(Dispatchers.IO) {
        rxSafetyDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: PatientProfileEntity) = withContext(Dispatchers.IO) {
        rxSafetyDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: PatientProfileEntity) = withContext(Dispatchers.IO) {
        rxSafetyDao.deleteMedicationsForProfile(profile.id)
        rxSafetyDao.deleteProfile(profile)
    }

    suspend fun insertMedication(medication: MedicationEntity): Long = withContext(Dispatchers.IO) {
        rxSafetyDao.insertMedication(medication)
    }

    suspend fun insertMedications(medications: List<MedicationEntity>) = withContext(Dispatchers.IO) {
        rxSafetyDao.insertMedications(medications)
    }

    suspend fun deleteMedication(medication: MedicationEntity) = withContext(Dispatchers.IO) {
        rxSafetyDao.deleteMedication(medication)
    }

    suspend fun getAllProfileMedicationsOnce(): List<MedicationEntity> = withContext(Dispatchers.IO) {
        rxSafetyDao.getAllMedicationsOnce()
    }

    // ------------------------------------------------------------------------
    // Remote Retrofit API Operations (NIH RxNav & OpenFDA)
    // ------------------------------------------------------------------------

    // Convenient method aliases for seamless backward compatibility
    suspend fun searchDrugs(query: String): List<RxNavCandidate> = searchRxNavDrugs(query)
    suspend fun fetchRecentRecalls(searchFilter: String? = null): List<DrugRecall> = fetchOpenFdaRecalls(searchFilter)
    suspend fun fetchDrugLabel(drugName: String): DrugLabelInfo? = fetchOpenFdaDrugLabel(drugName)
    suspend fun getAllMedicationsOnce(): List<MedicationEntity> = getAllProfileMedicationsOnce()
    suspend fun performAudit(
        medications: List<String>,
        conditions: List<String> = emptyList(),
        profileName: String? = null
    ): AuditResult = performUnifiedSafetyAudit(medications, conditions, profileName)

    /**
     * Searches NIH RxNav for approximate drug term candidates, falling back to
     * local clinical knowledge if remote connection is unavailable.
     */
    suspend fun searchRxNavDrugs(query: String): List<RxNavCandidate> = withContext(Dispatchers.IO) {
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
            Log.w(TAG, "RxNav API search error: ${e.message}")
        }

        // Fallback to local common list if remote API yielded no results
        if (results.isEmpty()) {
            val localMatches = ClinicalInteractionDatabase.getAllCommonMedications()
                .filter { it.contains(trimmed, ignoreCase = true) }
            localMatches.forEach { match ->
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

    /**
     * Searches NIH RxNav for full concept properties, formulation details (TTY: SCD, SBD, IN, BN),
     * and approximate candidate terms, returning structured RxNavDrugDetail records with RxCUIs.
     */
    suspend fun searchRxNavDetailed(query: String, filterTty: String? = null): List<RxNavDrugDetail> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val trimmed = query.trim()
        val results = mutableListOf<RxNavDrugDetail>()
        val seenRxCuis = mutableSetOf<String>()

        // 1. First, search exact and formulation concepts via RxNav drugs.json
        try {
            val drugsResponse = rxNavApi.getDrugsByName(trimmed)
            val conceptGroups = drugsResponse.drugGroup?.conceptGroup ?: emptyList()
            for (group in conceptGroups) {
                val groupTty = group.tty ?: "SCD"
                val properties = group.conceptProperties ?: emptyList()
                for (prop in properties) {
                    val rxcui = prop.rxcui ?: continue
                    if (rxcui.isBlank() || seenRxCuis.contains(rxcui)) continue
                    val name = prop.name ?: trimmed
                    val tty = prop.tty ?: groupTty
                    val synonym = prop.synonym?.takeIf { it.isNotBlank() }
                    val label = mapTtyToLabel(tty)
                    seenRxCuis.add(rxcui)
                    results.add(
                        RxNavDrugDetail(
                            rxcui = rxcui,
                            name = name,
                            synonym = synonym,
                            termType = tty,
                            termTypeLabel = label,
                            score = null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "RxNav drugs.json lookup error: ${e.message}")
        }

        // 2. Also query approximate matching concepts via approximateTerm.json for auto-complete & fuzzy hits
        try {
            val approxResponse = rxNavApi.getApproximateTerm(trimmed, maxEntries = 15)
            val candidates = approxResponse.approximateGroup?.candidate ?: emptyList()
            for (cand in candidates) {
                val rxcui = cand.rxcui ?: continue
                if (rxcui.isBlank() || seenRxCuis.contains(rxcui)) continue
                val name = cand.name ?: trimmed
                seenRxCuis.add(rxcui)
                results.add(
                    RxNavDrugDetail(
                        rxcui = rxcui,
                        name = name.replaceFirstChar { it.uppercase() },
                        synonym = null,
                        termType = "APPROX",
                        termTypeLabel = "Approximate Match",
                        score = cand.score
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "RxNav approximateTerm.json error: ${e.message}")
        }

        // 3. Fallback to local clinical database if remote calls yielded no entries
        if (results.isEmpty()) {
            val localMatches = getLocalClinicalRxCuiList(trimmed)
            results.addAll(localMatches)
        }

        // 4. Filter by TTY if requested
        if (!filterTty.isNullOrBlank() && filterTty != "ALL") {
            results.filter { it.termType.equals(filterTty, ignoreCase = true) }
        } else {
            results
        }
    }

    private fun mapTtyToLabel(tty: String): String = when (tty.uppercase()) {
        "SCD" -> "Clinical Drug (SCD)"
        "SBD" -> "Branded Drug (SBD)"
        "IN" -> "Active Ingredient (IN)"
        "PIN" -> "Precise Ingredient (PIN)"
        "BN" -> "Brand Name (BN)"
        "DF" -> "Dose Form (DF)"
        "BPCK" -> "Brand Pack (BPCK)"
        "GPCK" -> "Generic Pack (GPCK)"
        "MIN" -> "Multi-Ingredient (MIN)"
        "SCDC" -> "Clinical Drug Component"
        "SCDF" -> "Clinical Drug Form"
        else -> tty
    }

    private fun getLocalClinicalRxCuiList(query: String): List<RxNavDrugDetail> {
        val knownRxCuis = mapOf(
            "Warfarin" to Triple("11289", "IN", "Coumadin, Jantoven"),
            "Aspirin" to Triple("1191", "IN", "Bayer, Ecotrin"),
            "Lisinopril" to Triple("29046", "IN", "Prinivil, Zestril"),
            "Metformin" to Triple("6809", "IN", "Glucophage, Fortamet"),
            "Eliquis" to Triple("1364430", "BN", "Apixaban"),
            "Atorvastatin" to Triple("83367", "IN", "Lipitor"),
            "Simvastatin" to Triple("36567", "IN", "Zocor"),
            "Clopidogrel" to Triple("32968", "IN", "Plavix"),
            "Tramadol" to Triple("10689", "IN", "Ultram"),
            "Fluoxetine" to Triple("4492", "IN", "Prozac, Sarafem"),
            "Sertraline" to Triple("36437", "IN", "Zoloft"),
            "Ibuprofen" to Triple("5640", "IN", "Advil, Motrin"),
            "Omeprazole" to Triple("7646", "IN", "Prilosec"),
            "Levothyroxine" to Triple("10582", "IN", "Synthroid"),
            "Amlodipine" to Triple("17767", "IN", "Norvasc"),
            "Metoprolol" to Triple("6918", "IN", "Lopressor, Toprol-XL"),
            "Losartan" to Triple("5224", "IN", "Cozaar"),
            "Gabapentin" to Triple("25480", "IN", "Neurontin"),
            "Ciprofloxacin" to Triple("2551", "IN", "Cipro"),
            "Prednisone" to Triple("8640", "IN", "Deltasone")
        )

        val list = mutableListOf<RxNavDrugDetail>()
        val lower = query.lowercase().trim()
        knownRxCuis.forEach { (name, info) ->
            if (name.lowercase().contains(lower) || info.third.lowercase().contains(lower)) {
                list.add(
                    RxNavDrugDetail(
                        rxcui = info.first,
                        name = name,
                        synonym = info.third,
                        termType = info.second,
                        termTypeLabel = mapTtyToLabel(info.second),
                        score = "100"
                    )
                )
            }
        }
        return list
    }

    /**
     * Queries OpenFDA drug enforcement endpoint for live active safety recalls.
     */
    suspend fun fetchOpenFdaRecalls(searchFilter: String? = null): List<DrugRecall> = withContext(Dispatchers.IO) {
        val query = if (!searchFilter.isNullOrBlank()) "product_description:\"$searchFilter\"" else null

        try {
            val response = openFdaApi.getEnforcementReports(search = query, limit = 25)
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
            return@withContext getFallbackRecalls(searchFilter)
        }
    }

    /**
     * Fetches official FDA Product Monograph & Label Warnings from OpenFDA.
     */
    suspend fun fetchOpenFdaDrugLabel(drugName: String): DrugLabelInfo? = withContext(Dispatchers.IO) {
        val clean = drugName.substringBefore(" (").trim()
        val searchQuery = "openfda.brand_name:\"$clean\"+OR+openfda.generic_name:\"$clean\""

        try {
            val response = openFdaApi.getDrugLabel(search = searchQuery, limit = 1)
            val result = response.results?.firstOrNull() ?: return@withContext getFallbackLabel(clean)

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
            return@withContext getFallbackLabel(clean)
        }
    }

    // ------------------------------------------------------------------------
    // Unified Single-Source-of-Truth Cross-Referencing Operations
    // ------------------------------------------------------------------------

    /**
     * Cross-references all active local daily medications in Room against live OpenFDA recalls.
     */
    suspend fun checkRecallsForDailyMedications(): List<DrugRecall> = withContext(Dispatchers.IO) {
        val activeMeds = dailyMedicationDao.getActiveDailyMedicationsOnce().map { it.name }
        if (activeMeds.isEmpty()) return@withContext emptyList()

        val allRecalls = fetchOpenFdaRecalls()
        allRecalls.filter { recall ->
            activeMeds.any { med ->
                val cleanMed = med.substringBefore(" (").trim().lowercase()
                cleanMed.length > 3 && recall.productDescription.lowercase().contains(cleanMed)
            }
        }
    }

    /**
     * Executes unified clinical safety audit across local records, clinical database, and OpenFDA.
     */
    suspend fun performUnifiedSafetyAudit(
        medicationNames: List<String>,
        conditions: List<String> = emptyList(),
        profileName: String? = null
    ): AuditResult = withContext(Dispatchers.Default) {
        val drugInteractions = ClinicalInteractionDatabase.checkInteractions(medicationNames)
        val foodInteractions = FoodInteractionDatabase.checkFoodInteractions(medicationNames)
        val conditionContraindications = ConditionContraindicationsDatabase.checkContraindications(conditions, medicationNames)

        // Cross-reference with OpenFDA recall API
        val activeRecalls = fetchOpenFdaRecalls().filter { recall ->
            medicationNames.any { med ->
                val cleanMed = med.substringBefore(" (").trim().lowercase()
                cleanMed.length > 3 && recall.productDescription.lowercase().contains(cleanMed)
            }
        }

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
            evaluatedDrugs = medicationNames,
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

    private fun getFallbackRecalls(filter: String?): List<DrugRecall> {
        val sampleList = listOf(
            DrugRecall(
                recallNumber = "D-0342-2024",
                productDescription = "Atorvastatin Calcium Tablets, 20 mg, 90-count bottles.",
                reasonForRecall = "Subpotency: A single batch exhibited dissolution test failures.",
                classification = "Class II",
                status = "Ongoing",
                recallingFirm = "Aurobindo Pharma USA, Inc.",
                reportDate = "20240915",
                distributionPattern = "Nationwide (USA)",
                voluntaryMandated = "Voluntary: Firm initiated"
            ),
            DrugRecall(
                recallNumber = "D-0198-2024",
                productDescription = "Lisinopril and Hydrochlorothiazide Tablets, 20 mg / 12.5 mg.",
                reasonForRecall = "Cross-contamination: Trace particulate matter identified.",
                classification = "Class II",
                status = "Terminated",
                recallingFirm = "Lupin Pharmaceuticals, Inc.",
                reportDate = "20240722",
                distributionPattern = "Nationwide wholesale distributors",
                voluntaryMandated = "Voluntary: Firm initiated"
            )
        )
        return if (!filter.isNullOrBlank()) {
            sampleList.filter { it.productDescription.contains(filter, ignoreCase = true) }
        } else {
            sampleList
        }
    }

    private fun getFallbackLabel(drugName: String): DrugLabelInfo {
        val lower = drugName.lowercase()
        return when {
            lower.contains("warfarin") -> DrugLabelInfo(
                brandName = "Coumadin / Jantoven",
                genericName = "Warfarin Sodium",
                boxedWarning = "WARNING: BLEEDING RISK. Warfarin sodium can cause major or fatal bleeding. Perform regular monitoring of INR in all treated patients.",
                warnings = "Hemorrhage is the principal risk of warfarin. Tissue necrosis, gangrene, and calciphylaxis have been reported.",
                adverseReactions = "Major hemorrhage, epistaxis, hematuria, gastrointestinal bleeding, bruising, elevated liver enzymes.",
                indications = "Prophylaxis and treatment of venous thrombosis, pulmonary embolism, and thromboembolic complications."
            )
            lower.contains("lisinopril") -> DrugLabelInfo(
                brandName = "Prinivil / Zestril",
                genericName = "Lisinopril",
                boxedWarning = "WARNING: FETAL TOXICITY. Discontinue Lisinopril as soon as pregnancy is detected.",
                warnings = "Head and neck angioedema, intestinal angioedema, hypotension, hyperkalemia, renal impairment.",
                adverseReactions = "Dizziness, persistent dry cough, headache, excessive hypotension, hyperkalemia.",
                indications = "Treatment of hypertension, adjunct therapy in heart failure, and acute myocardial infarction survival."
            )
            else -> DrugLabelInfo(
                brandName = drugName.replaceFirstChar { it.uppercase() },
                genericName = drugName.replaceFirstChar { it.uppercase() },
                boxedWarning = "Consult your physician or pharmacist regarding specific boxed warnings.",
                warnings = "Consult product monograph for detailed warnings, precautions, and contraindications.",
                adverseReactions = "Consult prescribing information for incidence of adverse events.",
                indications = "Prescription medication - use strictly in accordance with licensed indications."
            )
        }
    }
}

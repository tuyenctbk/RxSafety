package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.clinical.DoseFlowBridge
import com.example.data.donation.DonationManager
import com.example.data.local.RxSafetyDatabase
import com.example.data.model.AuditResult
import com.example.data.model.DailyMedicationEntity
import com.example.data.model.DoseFlowProfileSync
import com.example.data.model.DrugLabelInfo
import com.example.data.model.DrugRecall
import com.example.data.model.MedicationEntity
import com.example.data.model.MedicationRegimenEntity
import com.example.data.model.PatientProfileEntity
import com.example.data.model.RxNavCandidate
import com.example.data.model.RxNavDrugDetail
import com.example.data.model.SeverityLevel
import com.example.data.repository.MedicationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiAlertMessage(
    val title: String,
    val message: String,
    val severity: SeverityLevel
)

class RxSafetyViewModel(
    private val repository: MedicationRepository,
    val donationManager: DonationManager
) : ViewModel() {

    // Donation & Optimal UX Placement Strategy
    val isSupporter: StateFlow<Boolean> = donationManager.isSupporter
    val shouldShowHomeBanner: StateFlow<Boolean> = donationManager.shouldShowHomeBanner
    val shouldShowAutoPrompt: StateFlow<Boolean> = donationManager.shouldShowAutoPrompt
    val milestoneEvent: StateFlow<String?> = donationManager.milestoneEvent
    val operationCount: StateFlow<Int> = donationManager.operationCount

    private val _showDonationDialog = MutableStateFlow(false)
    val showDonationDialog: StateFlow<Boolean> = _showDonationDialog.asStateFlow()

    // Daily Medication List (stored locally on device via Room)
    val dailyMedications: StateFlow<List<DailyMedicationEntity>> = repository.allDailyMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Medication Regimens (stored locally on device via Room)
    val medicationRegimens: StateFlow<List<MedicationRegimenEntity>> = repository.allRegimens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation Tab
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Real-time Top Notification Banner
    private val _bannerAlert = MutableStateFlow<UiAlertMessage?>(null)
    val bannerAlert: StateFlow<UiAlertMessage?> = _bannerAlert.asStateFlow()

    // Checker Screen State
    private val _selectedMedications = MutableStateFlow<List<String>>(listOf("Warfarin", "Aspirin"))
    val selectedMedications: StateFlow<List<String>> = _selectedMedications.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<RxNavCandidate>>(emptyList())
    val searchResults: StateFlow<List<RxNavCandidate>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _auditResult = MutableStateFlow<AuditResult?>(null)
    val auditResult: StateFlow<AuditResult?> = _auditResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Recalls Screen State
    private val _recallsList = MutableStateFlow<List<DrugRecall>>(emptyList())
    val recallsList: StateFlow<List<DrugRecall>> = _recallsList.asStateFlow()

    private val _isLoadingRecalls = MutableStateFlow(false)
    val isLoadingRecalls: StateFlow<Boolean> = _isLoadingRecalls.asStateFlow()

    private val _recallSearchQuery = MutableStateFlow("")
    val recallSearchQuery: StateFlow<String> = _recallSearchQuery.asStateFlow()

    private val _filterRecallsByMyMeds = MutableStateFlow(false)
    val filterRecallsByMyMeds: StateFlow<Boolean> = _filterRecallsByMyMeds.asStateFlow()

    // Patient Profiles & Regimens (DoseFlow)
    val profiles: StateFlow<List<PatientProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProfile = MutableStateFlow<PatientProfileEntity?>(null)
    val selectedProfile: StateFlow<PatientProfileEntity?> = _selectedProfile.asStateFlow()

    private val _profileMeds = MutableStateFlow<List<MedicationEntity>>(emptyList())
    val profileMeds: StateFlow<List<MedicationEntity>> = _profileMeds.asStateFlow()

    private val _profileAuditResult = MutableStateFlow<AuditResult?>(null)
    val profileAuditResult: StateFlow<AuditResult?> = _profileAuditResult.asStateFlow()

    private val _isAuditingProfile = MutableStateFlow(false)
    val isAuditingProfile: StateFlow<Boolean> = _isAuditingProfile.asStateFlow()

    // DoseFlow JSON dialog
    private val _doseFlowDialogText = MutableStateFlow<String?>(null)
    val doseFlowDialogText: StateFlow<String?> = _doseFlowDialogText.asStateFlow()

    // Drug Inspector
    private val _inspectorQuery = MutableStateFlow("Warfarin")
    val inspectorQuery: StateFlow<String> = _inspectorQuery.asStateFlow()

    private val _inspectorResult = MutableStateFlow<DrugLabelInfo?>(null)
    val inspectorResult: StateFlow<DrugLabelInfo?> = _inspectorResult.asStateFlow()

    private val _isLoadingInspector = MutableStateFlow(false)
    val isLoadingInspector: StateFlow<Boolean> = _isLoadingInspector.asStateFlow()

    // NIH RxNav Dedicated Search State & Interaction Staging
    private val _rxSearchQuery = MutableStateFlow("")
    val rxSearchQuery: StateFlow<String> = _rxSearchQuery.asStateFlow()

    private val _rxSearchResults = MutableStateFlow<List<RxNavDrugDetail>>(emptyList())
    val rxSearchResults: StateFlow<List<RxNavDrugDetail>> = _rxSearchResults.asStateFlow()

    private val _isRxSearching = MutableStateFlow(false)
    val isRxSearching: StateFlow<Boolean> = _isRxSearching.asStateFlow()

    private val _rxSearchTtyFilter = MutableStateFlow("ALL")
    val rxSearchTtyFilter: StateFlow<String> = _rxSearchTtyFilter.asStateFlow()

    private val _stagedMedications = MutableStateFlow<List<RxNavDrugDetail>>(emptyList())
    val stagedMedications: StateFlow<List<RxNavDrugDetail>> = _stagedMedications.asStateFlow()

    private var searchJob: Job? = null
    private var rxSearchJob: Job? = null

    init {
        // Run initial check on default pair
        analyzeCurrentMedications()
        loadRecalls()
        inspectDrug("Warfarin")

        // Observe profiles to select default
        viewModelScope.launch {
            profiles.collect { list ->
                if (list.isNotEmpty() && _selectedProfile.value == null) {
                    selectProfile(list.first())
                }
            }
        }
    }

    // Donation Actions
    fun openDonationDialog() {
        _showDonationDialog.value = true
    }

    fun closeDonationDialog() {
        _showDonationDialog.value = false
    }

    fun dismissHomeBanner() {
        donationManager.dismissHomeBanner()
    }

    fun markAutoPromptShown() {
        donationManager.markAutoPromptShown()
    }

    fun markAsSupporter(tierLabel: String) {
        donationManager.markAsSupporter(tierLabel)
    }

    fun toggleSupporterDevMode() {
        donationManager.toggleSupporterForDevMode()
    }

    fun clearMilestoneEvent() {
        donationManager.clearMilestoneEvent()
    }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun dismissBanner() {
        _bannerAlert.value = null
    }

    // Checker Actions
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // debounce
            _isSearching.value = true
            val results = repository.searchDrugs(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun addMedication(name: String) {
        val cleanName = name.substringBefore(" (").trim()
        if (!_selectedMedications.value.any { it.equals(cleanName, ignoreCase = true) }) {
            _selectedMedications.value = _selectedMedications.value + cleanName
            _searchQuery.value = ""
            _searchResults.value = emptyList()
            analyzeCurrentMedications()
        }
    }

    fun removeMedication(name: String) {
        _selectedMedications.value = _selectedMedications.value.filter { !it.equals(name, ignoreCase = true) }
        analyzeCurrentMedications()
    }

    fun clearAllMedications() {
        _selectedMedications.value = emptyList()
        _auditResult.value = null
    }

    fun loadPresetPair(drugA: String, drugB: String) {
        _selectedMedications.value = listOf(drugA, drugB)
        analyzeCurrentMedications()
    }

    fun analyzeCurrentMedications() {
        val meds = _selectedMedications.value
        if (meds.isEmpty()) {
            _auditResult.value = null
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            val result = repository.performAudit(medications = meds)
            _auditResult.value = result
            _isAnalyzing.value = false

            // Record core value realization operation
            donationManager.recordCoreOperation()

            if (result.highestSeverity == SeverityLevel.CRITICAL) {
                _bannerAlert.value = UiAlertMessage(
                    title = "Severe Drug Interaction Detected!",
                    message = "Active combination has high clinical risk. Review mechanism and warnings.",
                    severity = SeverityLevel.CRITICAL
                )
            }
        }
    }

    // Recalls Actions
    fun onRecallSearchChanged(query: String) {
        _recallSearchQuery.value = query
        loadRecalls(query)
    }

    fun toggleFilterRecallsByMyMeds() {
        _filterRecallsByMyMeds.value = !_filterRecallsByMyMeds.value
        loadRecalls(_recallSearchQuery.value)
    }

    fun loadRecalls(filter: String = _recallSearchQuery.value) {
        viewModelScope.launch {
            _isLoadingRecalls.value = true
            val recalls = repository.fetchRecentRecalls(filter.ifBlank { null })
            if (_filterRecallsByMyMeds.value) {
                val allUserMeds = repository.getAllMedicationsOnce().map { it.name }
                _recallsList.value = recalls.filter { recall ->
                    allUserMeds.any { med ->
                        val clean = med.lowercase().trim()
                        clean.length > 3 && recall.productDescription.lowercase().contains(clean)
                    }
                }
            } else {
                _recallsList.value = recalls
            }
            _isLoadingRecalls.value = false
        }
    }

    // Profiles & DoseFlow
    fun selectProfile(profile: PatientProfileEntity) {
        _selectedProfile.value = profile
        _profileAuditResult.value = null
        viewModelScope.launch {
            repository.getMedicationsForProfile(profile.id).collect { meds ->
                _profileMeds.value = meds
            }
        }
    }

    fun auditProfileRegimen() {
        val profile = _selectedProfile.value ?: return
        val meds = _profileMeds.value.map { it.name }

        viewModelScope.launch {
            _isAuditingProfile.value = true
            val audit = repository.performAudit(
                medications = meds,
                conditions = profile.conditions,
                profileName = profile.name
            )
            _profileAuditResult.value = audit
            _isAuditingProfile.value = false
            donationManager.recordCoreOperation()

            if (audit.highestSeverity == SeverityLevel.CRITICAL) {
                _bannerAlert.value = UiAlertMessage(
                    title = "Regimen Conflict in ${profile.name}",
                    message = "Found ${audit.drugInteractions.size} drug interactions and ${audit.conditionContraindications.size} condition warnings.",
                    severity = SeverityLevel.CRITICAL
                )
            }
        }
    }

    fun addMedicationToProfile(name: String, dosage: String, frequency: String, notes: String) {
        val profile = _selectedProfile.value ?: return
        viewModelScope.launch {
            repository.insertMedication(
                MedicationEntity(
                    profileId = profile.id,
                    name = name.trim(),
                    dosage = dosage.trim().ifBlank { "As prescribed" },
                    frequency = frequency.trim().ifBlank { "Daily" },
                    notes = notes.trim()
                )
            )
            auditProfileRegimen()
        }
    }

    fun removeMedicationFromProfile(med: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(med)
            auditProfileRegimen()
        }
    }

    fun createProfile(name: String, relationship: String, age: Int, gender: String, conditions: List<String>, allergies: List<String>) {
        viewModelScope.launch {
            val newId = repository.insertProfile(
                PatientProfileEntity(
                    name = name.trim(),
                    relationship = relationship.trim(),
                    age = age,
                    gender = gender,
                    conditions = conditions,
                    allergies = allergies,
                    isDefault = false
                )
            )
            val prof = PatientProfileEntity(
                id = newId,
                name = name,
                relationship = relationship,
                age = age,
                gender = gender,
                conditions = conditions,
                allergies = allergies
            )
            selectProfile(prof)
        }
    }

    fun exportDoseFlowSync() {
        val profile = _selectedProfile.value ?: return
        val meds = _profileMeds.value
        val json = DoseFlowBridge.exportToJson(profile, meds)
        _doseFlowDialogText.value = json
        donationManager.recordCoreOperation()
    }

    fun closeDoseFlowDialog() {
        _doseFlowDialogText.value = null
    }

    fun syncDoseFlowPreset(preset: DoseFlowProfileSync) {
        viewModelScope.launch {
            val newProfileId = repository.insertProfile(
                PatientProfileEntity(
                    name = preset.patientName,
                    relationship = "DoseFlow Sync",
                    age = preset.patientAge,
                    gender = "Unspecified",
                    conditions = preset.conditions,
                    allergies = preset.allergies,
                    isDefault = false
                )
            )

            val medEntities = preset.medications.map {
                MedicationEntity(
                    profileId = newProfileId,
                    name = it.name,
                    dosage = it.dosage,
                    frequency = it.frequency,
                    rxcui = it.rxcui,
                    notes = it.notes
                )
            }
            repository.insertMedications(medEntities)

            _bannerAlert.value = UiAlertMessage(
                title = "DoseFlow Regimen Synced!",
                message = "Imported ${preset.patientName} with ${preset.medications.size} medications. Running instant conflict scan...",
                severity = SeverityLevel.MODERATE
            )

            val createdProfile = PatientProfileEntity(
                id = newProfileId,
                name = preset.patientName,
                relationship = "DoseFlow Sync",
                age = preset.patientAge,
                gender = "Unspecified",
                conditions = preset.conditions,
                allergies = preset.allergies
            )
            selectProfile(createdProfile)
            auditProfileRegimen()
        }
    }

    // Drug Inspector Actions
    fun onInspectorQueryChanged(query: String) {
        _inspectorQuery.value = query
    }

    fun inspectDrug(drugName: String = _inspectorQuery.value) {
        if (drugName.isBlank()) return
        viewModelScope.launch {
            _isLoadingInspector.value = true
            val label = repository.fetchDrugLabel(drugName)
            _inspectorResult.value = label
            _isLoadingInspector.value = false
        }
    }

    // Daily Medications Local Storage Actions
    fun toggleDailyMedicationTaken(id: Long, currentTaken: Boolean) {
        viewModelScope.launch {
            repository.updateDailyMedicationTaken(id, !currentTaken)
            donationManager.recordCoreOperation()
        }
    }

    fun addDailyMedication(
        name: String,
        dosage: String,
        frequency: String = "Once daily",
        dosageForm: String = "Tablet",
        scheduledTime: String = "08:00 AM",
        instructions: String = "",
        prescriber: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertDailyMedication(
                DailyMedicationEntity(
                    name = name.trim(),
                    dosage = dosage.trim().ifBlank { "Standard dose" },
                    frequency = frequency.trim().ifBlank { "Once daily" },
                    dosageForm = dosageForm,
                    scheduledTime = scheduledTime,
                    instructions = instructions.trim(),
                    prescriber = prescriber.trim(),
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteDailyMedication(medication: DailyMedicationEntity) {
        viewModelScope.launch {
            repository.deleteDailyMedication(medication)
        }
    }

    // Medication Regimens (Multi-drug protocols) Actions
    fun addMedicationRegimen(
        title: String,
        patientName: String = "Myself",
        medicationNames: List<String>,
        targetConditions: List<String> = emptyList(),
        scheduleSummary: String = "Daily",
        description: String = "",
        prescriber: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertRegimen(
                MedicationRegimenEntity(
                    name = title.trim(),
                    patientName = patientName.trim(),
                    medicationNames = medicationNames,
                    targetConditions = targetConditions,
                    scheduleSummary = scheduleSummary,
                    description = description.trim(),
                    prescriber = prescriber.trim(),
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteMedicationRegimen(regimen: MedicationRegimenEntity) {
        viewModelScope.launch {
            repository.deleteRegimen(regimen)
        }
    }

    fun toggleMedicationRegimenStatus(id: Long, currentActive: Boolean) {
        viewModelScope.launch {
            repository.updateRegimenStatus(id, !currentActive)
        }
    }

    fun resetDailyTakenStatus() {
        viewModelScope.launch {
            repository.resetAllDailyTakenStatus()
        }
    }

    fun checkDailyMedicationsConflicts() {
        viewModelScope.launch {
            val activeMeds = repository.getActiveDailyMedicationsOnce().map { it.name }
            if (activeMeds.isNotEmpty()) {
                _selectedMedications.value = activeMeds
                _currentTab.value = 0
                analyzeCurrentMedications()
            }
        }
    }

    // Dedicated NIH RxNav Search Actions
    fun onRxSearchQueryChanged(query: String) {
        _rxSearchQuery.value = query
        rxSearchJob?.cancel()
        if (query.isBlank()) {
            _rxSearchResults.value = emptyList()
            _isRxSearching.value = false
            return
        }

        rxSearchJob = viewModelScope.launch {
            delay(350)
            executeRxSearch(query, _rxSearchTtyFilter.value)
        }
    }

    fun setRxSearchTtyFilter(filter: String) {
        _rxSearchTtyFilter.value = filter
        if (_rxSearchQuery.value.isNotBlank()) {
            executeRxSearch(_rxSearchQuery.value, filter)
        }
    }

    fun executeRxSearch(query: String, filterTty: String = _rxSearchTtyFilter.value) {
        rxSearchJob?.cancel()
        rxSearchJob = viewModelScope.launch {
            _isRxSearching.value = true
            val results = repository.searchRxNavDetailed(query, filterTty)
            val stagedSet = _stagedMedications.value.map { it.rxcui }.toSet()
            _rxSearchResults.value = results.map { item ->
                item.copy(isStagedForCheck = stagedSet.contains(item.rxcui))
            }
            _isRxSearching.value = false
            donationManager.recordCoreOperation()
        }
    }

    fun toggleStageMedication(item: RxNavDrugDetail) {
        val currentStaged = _stagedMedications.value.toMutableList()
        val exists = currentStaged.any { it.rxcui == item.rxcui }
        if (exists) {
            currentStaged.removeAll { it.rxcui == item.rxcui }
        } else {
            currentStaged.add(item.copy(isStagedForCheck = true))
        }
        _stagedMedications.value = currentStaged

        val stagedSet = currentStaged.map { it.rxcui }.toSet()
        _rxSearchResults.value = _rxSearchResults.value.map {
            it.copy(isStagedForCheck = stagedSet.contains(it.rxcui))
        }
    }

    fun removeStagedMedication(rxcui: String) {
        _stagedMedications.value = _stagedMedications.value.filter { it.rxcui != rxcui }
        val stagedSet = _stagedMedications.value.map { it.rxcui }.toSet()
        _rxSearchResults.value = _rxSearchResults.value.map {
            it.copy(isStagedForCheck = stagedSet.contains(it.rxcui))
        }
    }

    fun clearStagedMedications() {
        _stagedMedications.value = emptyList()
        _rxSearchResults.value = _rxSearchResults.value.map { it.copy(isStagedForCheck = false) }
    }

    fun saveDrugToDailyMedications(drug: RxNavDrugDetail, dosageStr: String = "Standard dose") {
        viewModelScope.launch {
            val entity = DailyMedicationEntity(
                name = drug.name,
                dosage = dosageStr,
                frequency = "Once daily",
                dosageForm = if (drug.name.contains("Capsule", ignoreCase = true)) "Capsule" else "Tablet",
                rxcui = drug.rxcui,
                instructions = if (!drug.synonym.isNullOrBlank()) "Synonym: ${drug.synonym}" else "",
                scheduledTime = "08:00 AM",
                scheduleTimes = listOf("Morning"),
                isActive = true,
                isTakenToday = false
            )
            repository.insertDailyMedication(entity)
            _bannerAlert.value = UiAlertMessage(
                title = "Added to Daily Meds",
                message = "${drug.name} (RxCUI: ${drug.rxcui}) saved to your local regimen.",
                severity = SeverityLevel.SAFE
            )
        }
    }

    fun sendStagedToInteractionChecker() {
        val staged = _stagedMedications.value
        if (staged.isEmpty()) return
        val drugNames = staged.map { it.name.substringBefore(" (").trim() }
        _selectedMedications.value = drugNames
        analyzeCurrentMedications()
        _currentTab.value = 0
    }

    fun inspectDrugFromSearch(drugName: String) {
        val clean = drugName.substringBefore(" (").trim()
        inspectDrug(clean)
        _currentTab.value = 4
    }

    // Camera Barcode & QR Scanner Dialog State & Actions
    private val _showScannerDialog = MutableStateFlow(false)
    val showScannerDialog: StateFlow<Boolean> = _showScannerDialog.asStateFlow()

    fun openScannerDialog() {
        _showScannerDialog.value = true
    }

    fun closeScannerDialog() {
        _showScannerDialog.value = false
    }

    fun saveScannedMedicationToDaily(scanned: com.example.ui.components.ScannedMedicationResult) {
        viewModelScope.launch {
            val entity = DailyMedicationEntity(
                name = scanned.medName,
                dosage = scanned.dosage,
                frequency = "Once daily",
                dosageForm = scanned.dosageForm,
                rxcui = scanned.rxcui,
                instructions = scanned.instructions,
                scheduledTime = "08:00 AM",
                scheduleTimes = listOf("Morning"),
                isActive = true,
                isTakenToday = false
            )
            repository.insertDailyMedication(entity)
            donationManager.recordCoreOperation()
            _bannerAlert.value = UiAlertMessage(
                title = "Scanned Med Saved to Device!",
                message = "${scanned.medName} (${scanned.dosage}) saved to local Room database.",
                severity = SeverityLevel.SAFE
            )
        }
    }

    fun stageScannedMedicationForCheck(scanned: com.example.ui.components.ScannedMedicationResult) {
        val cleanName = scanned.medName.substringBefore(" (").trim()
        addMedication(cleanName)
        _currentTab.value = 0
    }
}

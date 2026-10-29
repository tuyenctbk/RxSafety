package com.example.data.clinical

import com.example.data.model.DoseFlowMedicationSync
import com.example.data.model.DoseFlowProfileSync
import com.example.data.model.MedicationEntity
import com.example.data.model.PatientProfileEntity
import org.json.JSONArray
import org.json.JSONObject

object DoseFlowBridge {

    fun exportToJson(profile: PatientProfileEntity, meds: List<MedicationEntity>): String {
        val root = JSONObject()
        root.put("appIdentifier", "DoseFlow-Sync-v1")
        root.put("patientName", profile.name)
        root.put("patientAge", profile.age)
        root.put("gender", profile.gender)
        root.put("relationship", profile.relationship)
        root.put("exportTimestamp", System.currentTimeMillis())

        val condArray = JSONArray()
        profile.conditions.forEach { condArray.put(it) }
        root.put("conditions", condArray)

        val allergyArray = JSONArray()
        profile.allergies.forEach { allergyArray.put(it) }
        root.put("allergies", allergyArray)

        val medsArray = JSONArray()
        meds.forEach { med ->
            val medObj = JSONObject()
            medObj.put("name", med.name)
            medObj.put("dosage", med.dosage)
            medObj.put("frequency", med.frequency)
            medObj.put("rxcui", med.rxcui ?: "")
            medObj.put("notes", med.notes)
            medsArray.put(medObj)
        }
        root.put("medications", medsArray)

        return root.toString(2)
    }

    fun importFromJson(jsonString: String): DoseFlowProfileSync {
        val root = JSONObject(jsonString)
        val name = root.optString("patientName", "DoseFlow Patient")
        val age = root.optInt("patientAge", 65)

        val condList = mutableListOf<String>()
        val condArray = root.optJSONArray("conditions")
        if (condArray != null) {
            for (i in 0 until condArray.length()) {
                condList.add(condArray.getString(i))
            }
        }

        val allergyList = mutableListOf<String>()
        val allergyArray = root.optJSONArray("allergies")
        if (allergyArray != null) {
            for (i in 0 until allergyArray.length()) {
                allergyList.add(allergyArray.getString(i))
            }
        }

        val medsList = mutableListOf<DoseFlowMedicationSync>()
        val medsArray = root.optJSONArray("medications")
        if (medsArray != null) {
            for (i in 0 until medsArray.length()) {
                val medObj = medsArray.getJSONObject(i)
                medsList.add(
                    DoseFlowMedicationSync(
                        name = medObj.optString("name", "Unknown"),
                        dosage = medObj.optString("dosage", "1 dose"),
                        frequency = medObj.optString("frequency", "Daily"),
                        rxcui = medObj.optString("rxcui", null).takeIf { !it.isNullOrBlank() },
                        notes = medObj.optString("notes", "")
                    )
                )
            }
        }

        return DoseFlowProfileSync(
            patientName = name,
            patientAge = age,
            conditions = condList,
            allergies = allergyList,
            medications = medsList
        )
    }

    // Pre-built DoseFlow clinical regimen presets for instant demonstration & safety auditing
    val sampleDoseFlowPresets: List<DoseFlowProfileSync> = listOf(
        DoseFlowProfileSync(
            patientName = "Arthur Pendelton (Cardiac Polypharmacy)",
            patientAge = 74,
            conditions = listOf("Atrial Fibrillation", "Hypertension", "Heart Failure"),
            allergies = listOf("Penicillin"),
            medications = listOf(
                DoseFlowMedicationSync("Warfarin", "5 mg", "Once daily evening", "11289", "Target INR 2.0 - 3.0"),
                DoseFlowMedicationSync("Aspirin", "81 mg", "Once daily with food", "1191", "Post-PCI prophylaxis"),
                DoseFlowMedicationSync("Lisinopril", "20 mg", "Once daily morning", "29046", "ACE inhibitor for BP"),
                DoseFlowMedicationSync("Spironolactone", "25 mg", "Once daily", "9997", "Potassium-sparing diuretic"),
                DoseFlowMedicationSync("Atorvastatin", "40 mg", "Once daily at bedtime", "83367", "Statin for plaque stability")
            )
        ),
        DoseFlowProfileSync(
            patientName = "Claire Bennett (Pain & Ortho Recovery)",
            patientAge = 58,
            conditions = listOf("Total Knee Arthroplasty", "Anxiety"),
            allergies = listOf("Codeine"),
            medications = listOf(
                DoseFlowMedicationSync("Oxycodone", "5 mg", "Every 6 hours as needed", "7804", "Post-op acute pain"),
                DoseFlowMedicationSync("Alprazolam", "0.5 mg", "Twice daily as needed", "596", "Anxiety"),
                DoseFlowMedicationSync("Ibuprofen", "600 mg", "Every 8 hours with meals", "5640", "Anti-inflammatory")
            )
        ),
        DoseFlowProfileSync(
            patientName = "Robert Chen (Respiratory & Acid Reflux)",
            patientAge = 66,
            conditions = listOf("COPD", "GERD", "Hyperlipidemia"),
            allergies = emptyList(),
            medications = listOf(
                DoseFlowMedicationSync("Simvastatin", "40 mg", "Once daily at bedtime", "36567", "Cholesterol control"),
                DoseFlowMedicationSync("Clarithromycin", "500 mg", "Twice daily for 10 days", "21212", "Antibiotic for chest infection"),
                DoseFlowMedicationSync("Omeprazole", "20 mg", "Once daily before breakfast", "7646", "PPI for acid reflux")
            )
        )
    )
}

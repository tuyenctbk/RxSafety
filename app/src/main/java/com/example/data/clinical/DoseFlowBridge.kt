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
                        rxcui = if (medObj.has("rxcui") && !medObj.isNull("rxcui")) medObj.optString("rxcui").takeIf { it.isNotBlank() } else null,
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

}

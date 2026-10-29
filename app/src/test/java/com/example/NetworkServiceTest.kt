package com.example

import com.example.data.remote.OpenFdaEnforcementResponse
import com.example.data.remote.OpenFdaLabelResponse
import com.example.data.remote.RxNavApproximateResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkServiceTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `test RxNav approximate term json parsing`() {
        val json = """
            {
              "approximateGroup": {
                "inputTerm": "lisinopril",
                "candidate": [
                  {
                    "rxcui": "29046",
                    "rxaui": "10282780",
                    "score": "11.95",
                    "rank": "1",
                    "name": "Lisinopril",
                    "source": "USP"
                  }
                ]
              }
            }
        """.trimIndent()

        val adapter = moshi.adapter(RxNavApproximateResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertNotNull(response?.approximateGroup)
        val candidates = response?.approximateGroup?.candidate
        assertNotNull(candidates)
        assertEquals(1, candidates?.size)
        assertEquals("29046", candidates?.first()?.rxcui)
        assertEquals("Lisinopril", candidates?.first()?.name)
    }

    @Test
    fun `test OpenFDA enforcement recall json parsing`() {
        val json = """
            {
              "results": [
                {
                  "recall_number": "D-0342-2024",
                  "product_description": "Atorvastatin Calcium Tablets 20 mg",
                  "reason_for_recall": "Subpotency detected during stability testing",
                  "classification": "Class II",
                  "status": "Ongoing",
                  "recalling_firm": "Aurobindo Pharma USA",
                  "report_date": "20240915",
                  "distribution_pattern": "Nationwide",
                  "voluntary_mandated": "Voluntary",
                  "openfda": {
                    "brand_name": ["Lipitor"],
                    "generic_name": ["Atorvastatin Calcium"]
                  }
                }
              ]
            }
        """.trimIndent()

        val adapter = moshi.adapter(OpenFdaEnforcementResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        val results = response?.results
        assertNotNull(results)
        assertEquals(1, results?.size)
        val recall = results?.first()
        assertEquals("D-0342-2024", recall?.recallNumber)
        assertEquals("Class II", recall?.classification)
        assertEquals("Lipitor", recall?.openfda?.brandName?.first())
    }

    @Test
    fun `test OpenFDA drug label warnings json parsing`() {
        val json = """
            {
              "results": [
                {
                  "boxed_warning": ["WARNING: BLEEDING RISK. Warfarin sodium can cause major or fatal bleeding."],
                  "warnings": ["Hemorrhage is the principal risk of warfarin."],
                  "drug_interactions": ["Concomitant use of aspirin increases GI bleeding risk."],
                  "openfda": {
                    "brand_name": ["Coumadin"],
                    "generic_name": ["Warfarin Sodium"]
                  }
                }
              ]
            }
        """.trimIndent()

        val adapter = moshi.adapter(OpenFdaLabelResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        val label = response?.results?.first()
        assertNotNull(label)
        assertTrue(label?.boxedWarning?.first()?.contains("BLEEDING RISK") == true)
        assertTrue(label?.drugInteractions?.first()?.contains("aspirin") == true)
    }
}

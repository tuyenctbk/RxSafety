package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenFdaEnforcementResponse(
    @Json(name = "results")
    val results: List<EnforcementResult>? = null
)

@JsonClass(generateAdapter = true)
data class EnforcementResult(
    @Json(name = "recall_number")
    val recallNumber: String? = null,
    @Json(name = "product_description")
    val productDescription: String? = null,
    @Json(name = "reason_for_recall")
    val reasonForRecall: String? = null,
    @Json(name = "classification")
    val classification: String? = null,
    @Json(name = "status")
    val status: String? = null,
    @Json(name = "recalling_firm")
    val recallingFirm: String? = null,
    @Json(name = "report_date")
    val reportDate: String? = null,
    @Json(name = "distribution_pattern")
    val distributionPattern: String? = null,
    @Json(name = "voluntary_mandated")
    val voluntaryMandated: String? = null,
    @Json(name = "openfda")
    val openfda: OpenFdaMetadata? = null
)

@JsonClass(generateAdapter = true)
data class OpenFdaMetadata(
    @Json(name = "brand_name")
    val brandName: List<String>? = null,
    @Json(name = "generic_name")
    val genericName: List<String>? = null,
    @Json(name = "manufacturer_name")
    val manufacturerName: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class OpenFdaLabelResponse(
    @Json(name = "results")
    val results: List<LabelResult>? = null
)

@JsonClass(generateAdapter = true)
data class LabelResult(
    @Json(name = "boxed_warning")
    val boxedWarning: List<String>? = null,
    @Json(name = "warnings")
    val warnings: List<String>? = null,
    @Json(name = "warnings_and_cautions")
    val warningsAndCautions: List<String>? = null,
    @Json(name = "adverse_reactions")
    val adverseReactions: List<String>? = null,
    @Json(name = "drug_interactions")
    val drugInteractions: List<String>? = null,
    @Json(name = "indications_and_usage")
    val indicationsAndUsage: List<String>? = null,
    @Json(name = "openfda")
    val openfda: OpenFdaMetadata? = null
)

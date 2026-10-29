package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RxNavApproximateResponse(
    @Json(name = "approximateGroup")
    val approximateGroup: ApproximateGroup? = null
)

@JsonClass(generateAdapter = true)
data class ApproximateGroup(
    @Json(name = "inputTerm")
    val inputTerm: String? = null,
    @Json(name = "candidate")
    val candidate: List<ApproximateCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class ApproximateCandidate(
    @Json(name = "rxcui")
    val rxcui: String? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "score")
    val score: String? = null,
    @Json(name = "rank")
    val rank: String? = null,
    @Json(name = "source")
    val source: String? = null
)

@JsonClass(generateAdapter = true)
data class RxNavDrugsResponse(
    @Json(name = "drugGroup")
    val drugGroup: DrugGroup? = null
)

@JsonClass(generateAdapter = true)
data class DrugGroup(
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "conceptGroup")
    val conceptGroup: List<ConceptGroup>? = null
)

@JsonClass(generateAdapter = true)
data class ConceptGroup(
    @Json(name = "tty")
    val tty: String? = null,
    @Json(name = "conceptProperties")
    val conceptProperties: List<ConceptProperty>? = null
)

@JsonClass(generateAdapter = true)
data class ConceptProperty(
    @Json(name = "rxcui")
    val rxcui: String? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "synonym")
    val synonym: String? = null,
    @Json(name = "tty")
    val tty: String? = null
)

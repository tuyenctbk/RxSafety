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

// ----------------------------------------------------------------------------
// NIH RxNav Drug Interaction API DTOs (REST/interaction/list.json & interaction.json)
// ----------------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class RxNavInteractionListResponse(
    @Json(name = "fullInteractionTypeGroup")
    val fullInteractionTypeGroup: List<FullInteractionTypeGroup>? = null,
    @Json(name = "nlmDisclaimer")
    val nlmDisclaimer: String? = null
)

@JsonClass(generateAdapter = true)
data class FullInteractionTypeGroup(
    @Json(name = "sourceDisclaimer")
    val sourceDisclaimer: String? = null,
    @Json(name = "sourceName")
    val sourceName: String? = null,
    @Json(name = "fullInteractionType")
    val fullInteractionType: List<FullInteractionType>? = null
)

@JsonClass(generateAdapter = true)
data class FullInteractionType(
    @Json(name = "comment")
    val comment: String? = null,
    @Json(name = "minConcept")
    val minConcept: List<MinConcept>? = null,
    @Json(name = "interactionPair")
    val interactionPair: List<InteractionPair>? = null
)

@JsonClass(generateAdapter = true)
data class MinConcept(
    @Json(name = "rxcui")
    val rxcui: String? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "tty")
    val tty: String? = null
)

@JsonClass(generateAdapter = true)
data class InteractionPair(
    @Json(name = "interactionConcept")
    val interactionConcept: List<InteractionConcept>? = null,
    @Json(name = "severity")
    val severity: String? = null,
    @Json(name = "description")
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class InteractionConcept(
    @Json(name = "minConceptItem")
    val minConceptItem: MinConcept? = null
)


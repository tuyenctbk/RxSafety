package com.example.data.clinical

import com.example.data.model.ConditionContraindication
import com.example.data.model.SeverityLevel

object ConditionContraindicationsDatabase {

    private data class ConditionRule(
        val conditionKeyword: String,
        val drugKeywords: List<String>,
        val severity: SeverityLevel,
        val reason: String
    )

    private val conditionRules = listOf(
        ConditionRule(
            conditionKeyword = "kidney",
            drugKeywords = listOf("ibuprofen", "naproxen", "celecoxib", "meloxicam", "metformin"),
            severity = SeverityLevel.CRITICAL,
            reason = "NSAIDs constrict afferent renal arterioles precipitating acute tubular necrosis; Metformin carries heightened lactic acidosis risk with reduced eGFR."
        ),
        ConditionRule(
            conditionKeyword = "asthma",
            drugKeywords = listOf("propranolol", "nadolol", "carvedilol", "aspirin"),
            severity = SeverityLevel.CRITICAL,
            reason = "Non-selective beta blockers block beta-2 pulmonary receptors causing bronchospasm; Aspirin may provoke aspirin-exacerbated respiratory disease (AERD)."
        ),
        ConditionRule(
            conditionKeyword = "heart failure",
            drugKeywords = listOf("ibuprofen", "naproxen", "diltiazem", "verapamil"),
            severity = SeverityLevel.MODERATE,
            reason = "NSAIDs cause renal sodium and fluid retention blunting diuretics; Non-dihydropyridine CCBs exert negative inotropic effects worsening heart failure."
        ),
        ConditionRule(
            conditionKeyword = "ulcer",
            drugKeywords = listOf("aspirin", "ibuprofen", "naproxen", "prednisone", "warfarin"),
            severity = SeverityLevel.CRITICAL,
            reason = "Inhibition of gastric cytoprotective prostaglandins dramatically increases the risk of recurrent peptic ulcer perforation and major hemorrhage."
        ),
        ConditionRule(
            conditionKeyword = "liver",
            drugKeywords = listOf("acetaminophen", "tylenol", "methotrexate", "amiodarone"),
            severity = SeverityLevel.MODERATE,
            reason = "Impaired hepatic metabolism increases the risk of drug-induced liver injury and toxic metabolite accumulation."
        ),
        ConditionRule(
            conditionKeyword = "hypertension",
            drugKeywords = listOf("pseudoephedrine", "phenylephrine", "ibuprofen", "naproxen"),
            severity = SeverityLevel.MODERATE,
            reason = "Alpha-adrenergic sympathomimetics and NSAID-mediated volume retention cause sudden elevations in systemic blood pressure."
        )
    )

    fun checkContraindications(
        conditions: List<String>,
        medications: List<String>
    ): List<ConditionContraindication> {
        val results = mutableListOf<ConditionContraindication>()
        val lowerMeds = medications.map { it.lowercase() }
        val lowerConds = conditions.map { it.lowercase() }

        for (cond in lowerConds) {
            for (rule in conditionRules) {
                if (cond.contains(rule.conditionKeyword)) {
                    for (med in lowerMeds) {
                        if (rule.drugKeywords.any { kw -> med.contains(kw) }) {
                            results.add(
                                ConditionContraindication(
                                    condition = cond.replaceFirstChar { it.uppercase() },
                                    drugName = med.replaceFirstChar { it.uppercase() },
                                    severity = rule.severity,
                                    reason = rule.reason
                                )
                            )
                        }
                    }
                }
            }
        }

        return results
    }
}

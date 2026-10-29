package com.example.data.clinical

import com.example.data.model.FoodInteractionItem
import com.example.data.model.SeverityLevel

object FoodInteractionDatabase {

    private data class FoodRule(
        val foodName: String,
        val targetKeywords: List<String>,
        val severity: SeverityLevel,
        val mechanism: String,
        val recommendation: String
    )

    private val foodRules = listOf(
        FoodRule(
            foodName = "Grapefruit & Grapefruit Juice",
            targetKeywords = listOf("atorvastatin", "simvastatin", "lovastatin", "lipitor", "zocor", "felodipine", "nifedipine", "amiodarone", "cyclosporine"),
            severity = SeverityLevel.CRITICAL,
            mechanism = "Furanocoumarins in grapefruit irreversibly inhibit intestinal CYP3A4 enzymes, causing up to a 3-fold spike in drug bioavailability.",
            recommendation = "Avoid consuming grapefruit, grapefruit juice, or Seville oranges while taking CYP3A4-metabolized statins. Discuss switching to pravastatin or rosuvastatin if grapefruit is desired."
        ),
        FoodRule(
            foodName = "Vitamin K Rich Greens (Kale, Spinach, Broccoli)",
            targetKeywords = listOf("warfarin", "coumadin", "jantoven"),
            severity = SeverityLevel.MODERATE,
            mechanism = "Vitamin K directly overcomes warfarin's competitive inhibition of Vitamin K Epoxide Reductase (VKORC1), decreasing INR and promoting thrombosis.",
            recommendation = "Maintain a steady, consistent intake of green leafy vegetables. Do NOT drastically increase or eliminate greens without notifying your anticoagulation clinic for INR dose adjustment."
        ),
        FoodRule(
            foodName = "Dairy Products & Calcium-Fortified Foods",
            targetKeywords = listOf("ciprofloxacin", "levofloxacin", "doxycycline", "tetracycline", "levothyroxine", "synthroid"),
            severity = SeverityLevel.MODERATE,
            mechanism = "Multivalent calcium ions bind with antibiotic or thyroid molecules, forming insoluble chelates that pass unabsorbed through the bowel.",
            recommendation = "Separate dairy meals and calcium supplements from your medication by at least 2 to 4 hours."
        ),
        FoodRule(
            foodName = "Alcohol & Alcoholic Beverages",
            targetKeywords = listOf("alprazolam", "lorazepam", "diazepam", "clonazepam", "oxycodone", "hydrocodone", "tramadol", "zolpidem", "metformin", "acetaminophen", "tylenol"),
            severity = SeverityLevel.CRITICAL,
            mechanism = "Alcohol amplifies central nervous system depression, exacerbates lactic acidosis risk with metformin, and depletes glutathione increasing acetaminophen hepatotoxicity.",
            recommendation = "Completely avoid alcoholic beverages while taking central nervous system depressants, opioids, or sedatives. Avoid high alcohol intake with acetaminophen or metformin."
        ),
        FoodRule(
            foodName = "Potassium-Rich Salt Substitutes",
            targetKeywords = listOf("lisinopril", "enalapril", "ramipril", "losartan", "valsartan", "spironolactone", "eplerenone"),
            severity = SeverityLevel.CRITICAL,
            mechanism = "Salt substitutes contain potassium chloride instead of sodium chloride. Concomitant use with potassium-sparing medications triggers dangerous hyperkalemia.",
            recommendation = "Do not use potassium-enriched 'light' salts or salt substitutes without explicit physician authorization."
        ),
        FoodRule(
            foodName = "St. John's Wort Herbal Supplement",
            targetKeywords = listOf("warfarin", "apixaban", "rivaroxaban", "digoxin", "oral contraceptive", "sertraline", "fluoxetine"),
            severity = SeverityLevel.CRITICAL,
            mechanism = "Potent inducer of intestinal P-glycoprotein and hepatic CYP3A4, precipitously dropping drug levels or precipitating Serotonin Syndrome.",
            recommendation = "Discontinue St. John's Wort immediately and disclose all botanical supplements to your healthcare team."
        )
    )

    fun checkFoodInteractions(medications: List<String>): List<FoodInteractionItem> {
        val results = mutableListOf<FoodInteractionItem>()
        val lowerMeds = medications.map { it.lowercase() }

        for (med in lowerMeds) {
            for (rule in foodRules) {
                val matched = rule.targetKeywords.any { kw -> med.contains(kw) }
                if (matched) {
                    val alreadyAdded = results.any { it.foodName == rule.foodName && it.drugName == med }
                    if (!alreadyAdded) {
                        results.add(
                            FoodInteractionItem(
                                id = "${rule.foodName}_${med}",
                                foodName = rule.foodName,
                                drugName = med.replaceFirstChar { it.uppercase() },
                                severity = rule.severity,
                                mechanism = rule.mechanism,
                                recommendation = rule.recommendation
                            )
                        )
                    }
                }
            }
        }

        return results
    }
}

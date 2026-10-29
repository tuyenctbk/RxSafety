package com.example.data.clinical

import com.example.data.model.DrugInteractionItem
import com.example.data.model.SeverityLevel

object ClinicalInteractionDatabase {

    // Synonym & Brand map to map brand names to generic entities for robust matching
    private val drugAliasMap = mapOf(
        "coumadin" to "warfarin",
        "jantoven" to "warfarin",
        "eliquis" to "apixaban",
        "xarelto" to "rivaroxaban",
        "pradaxa" to "dabigatran",
        "plavix" to "clopidogrel",
        "aspirin" to "aspirin",
        "ecotrin" to "aspirin",
        "bayer" to "aspirin",
        "advil" to "ibuprofen",
        "motrin" to "ibuprofen",
        "aleve" to "naproxen",
        "naprosyn" to "naproxen",
        "celebrex" to "celecoxib",
        "mobic" to "meloxicam",
        "zestril" to "lisinopril",
        "prinivil" to "lisinopril",
        "vasotec" to "enalapril",
        "altace" to "ramipril",
        "cozaar" to "losartan",
        "diovan" to "valsartan",
        "aldactone" to "spironolactone",
        "lipitor" to "atorvastatin",
        "zocor" to "simvastatin",
        "crestor" to "rosuvastatin",
        "pravachol" to "pravastatin",
        "biaxin" to "clarithromycin",
        "zithromax" to "azithromycin",
        "erythrocin" to "erythromycin",
        "zoloft" to "sertraline",
        "prozac" to "fluoxetine",
        "lexapro" to "escitalopram",
        "celexa" to "citalopram",
        "paxil" to "paroxetine",
        "cymbalta" to "duloxetine",
        "effexor" to "venlafaxine",
        "ultram" to "tramadol",
        "conzip" to "tramadol",
        "oxycontin" to "oxycodone",
        "percocet" to "oxycodone",
        "vicodin" to "hydrocodone",
        "norco" to "hydrocodone",
        "suboxone" to "buprenorphine",
        "xanax" to "alprazolam",
        "ativan" to "lorazepam",
        "valium" to "diazepam",
        "klonopin" to "clonazepam",
        "viagra" to "sildenafil",
        "revatio" to "sildenafil",
        "cialis" to "tadalafil",
        "adcirca" to "tadalafil",
        "nitrostat" to "nitroglycerin",
        "nitrolingual" to "nitroglycerin",
        "imdur" to "isosorbide mononitrate",
        "isordil" to "isosorbide dinitrate",
        "glucophage" to "metformin",
        "lanoxin" to "digoxin",
        "cordarone" to "amiodarone",
        "pacerone" to "amiodarone",
        "calan" to "verapamil",
        "isoptin" to "verapamil",
        "cardizem" to "diltiazem",
        "cipro" to "ciprofloxacin",
        "levaquin" to "levofloxacin",
        "nexium" to "esomeprazole",
        "prilosec" to "omeprazole",
        "protonix" to "pantoprazole",
        "synthroid" to "levothyroxine",
        "levoxyl" to "levothyroxine",
        "dilantin" to "phenytoin",
        "tegretol" to "carbamazepine",
        "depakote" to "valproic acid",
        "lithobid" to "lithium",
        "eskalith" to "lithium",
        "trexall" to "methotrexate"
    )

    private fun normalize(name: String): String {
        val clean = name.lowercase().trim()
            .replace(Regex("^(tablet|capsule|oral|solution|injection|extended release|er|xr|sr|hcl|sodium|potassium|calcium)\\b"), "")
            .trim()
        val firstWord = clean.split(" ", "/", "-").firstOrNull() ?: clean
        return drugAliasMap[firstWord] ?: drugAliasMap[clean] ?: firstWord
    }

    private data class Rule(
        val drug1: String,
        val drug2: String,
        val severity: SeverityLevel,
        val title: String,
        val mechanism: String,
        val clinicalEffect: String,
        val recommendation: String
    )

    private val interactionRules = listOf(
        Rule(
            drug1 = "warfarin",
            drug2 = "aspirin",
            severity = SeverityLevel.CRITICAL,
            title = "Severe Synergistic Bleeding & Hemorrhage Risk",
            mechanism = "Warfarin inhibits vitamin K-dependent clotting factors while Aspirin irreversibly inhibits platelet aggregation and damages gastric mucosa.",
            clinicalEffect = "Exponentially increased risk of life-threatening gastrointestinal bleeding, cerebral hemorrhage, and uncontrolled hematomas.",
            recommendation = "Combination should generally be avoided unless specifically indicated post-ACS or mechanical heart valve. If co-prescribed, require close INR monitoring and gastroprotective PPI co-therapy."
        ),
        Rule(
            drug1 = "warfarin",
            drug2 = "ibuprofen",
            severity = SeverityLevel.CRITICAL,
            title = "Major Gastrointestinal Bleeding Hazard",
            mechanism = "NSAIDs displace warfarin from albumin, inhibit platelet COX-1, and cause gastric mucosal erosions.",
            clinicalEffect = "Marked elevation in bleeding risk, ulcer perforation, and INR prolongation.",
            recommendation = "Avoid NSAIDs. Use topical therapies or acetaminophen for mild-to-moderate pain under physician guidance."
        ),
        Rule(
            drug1 = "warfarin",
            drug2 = "naproxen",
            severity = SeverityLevel.CRITICAL,
            title = "Major Gastrointestinal Bleeding Hazard",
            mechanism = "Additive anticoagulation and antiplatelet inhibition coupled with mucosal barrier disruption.",
            clinicalEffect = "Severe gastrointestinal bleeding and prolonged bleeding times.",
            recommendation = "Avoid combination. Consult prescriber for safer alternative analgesics."
        ),
        Rule(
            drug1 = "sildenafil",
            drug2 = "nitroglycerin",
            severity = SeverityLevel.CRITICAL,
            title = "Fatal Hypotension & Cardiovascular Collapse",
            mechanism = "Nitrates stimulate guanylyl cyclase to produce cGMP, while PDE5 inhibitors prevent cGMP breakdown, causing profound vascular smooth muscle relaxation.",
            clinicalEffect = "Catastrophic precipitous drop in systemic blood pressure, myocardial infarction, syncope, and death.",
            recommendation = "CONTRAINDICATED. Never administer nitrates within 24 hours of sildenafil or 48 hours of tadalafil. If angina occurs, emergency medical services must be informed immediately."
        ),
        Rule(
            drug1 = "tadalafil",
            drug2 = "nitroglycerin",
            severity = SeverityLevel.CRITICAL,
            title = "Fatal Hypotension & Cardiovascular Collapse",
            mechanism = "Uncontrolled intracellular accumulation of cGMP resulting in extreme peripheral vasodilation.",
            clinicalEffect = "Profound refractory hypotension, coronary hypoperfusion, and cardiogenic shock.",
            recommendation = "CONTRAINDICATED. Withhold nitrates for at least 48 hours following tadalafil use."
        ),
        Rule(
            drug1 = "sildenafil",
            drug2 = "isosorbide mononitrate",
            severity = SeverityLevel.CRITICAL,
            title = "Lethal Systemic Hypotension",
            mechanism = "Synergistic cGMP accumulation causing total loss of vascular tone.",
            clinicalEffect = "Severe hypotension, syncope, ischemic stroke, and fatal arrhythmia.",
            recommendation = "CONTRAINDICATED. Concomitant administration is strictly prohibited."
        ),
        Rule(
            drug1 = "lisinopril",
            drug2 = "spironolactone",
            severity = SeverityLevel.CRITICAL,
            title = "Severe Hyperkalemia Risk",
            mechanism = "Both agents diminish aldosterone action in the renal distal tubule, dramatically reducing potassium excretion.",
            clinicalEffect = "Life-threatening hyperkalemia, cardiac conduction abnormalities, ventricular arrhythmias, and cardiac arrest.",
            recommendation = "Check baseline serum potassium and creatinine before initiating. Repeat within 1 week and monitor monthly. Avoid high-potassium salt substitutes."
        ),
        Rule(
            drug1 = "losartan",
            drug2 = "spironolactone",
            severity = SeverityLevel.CRITICAL,
            title = "Severe Hyperkalemia Risk",
            mechanism = "Angiotensin receptor blockade combined with mineralocorticoid receptor antagonism decreases renal potassium clearance.",
            clinicalEffect = "Dangerous elevation in serum potassium (>5.5 mEq/L) causing cardiac toxicity.",
            recommendation = "Frequent laboratory monitoring of serum electrolytes and renal function is mandatory."
        ),
        Rule(
            drug1 = "simvastatin",
            drug2 = "clarithromycin",
            severity = SeverityLevel.CRITICAL,
            title = "Severe Rhabdomyolysis & Acute Renal Failure",
            mechanism = "Clarithromycin is a potent inhibitor of CYP3A4, the primary enzyme responsible for simvastatin elimination.",
            clinicalEffect = "Up to 10-fold increase in simvastatin plasma levels, leading to extensive skeletal muscle breakdown (rhabdomyolysis) and renal failure.",
            recommendation = "CONTRAINDICATED. Temporarily suspend simvastatin therapy while taking clarithromycin or use an alternative antibiotic like azithromycin."
        ),
        Rule(
            drug1 = "atorvastatin",
            drug2 = "clarithromycin",
            severity = SeverityLevel.CRITICAL,
            title = "High Risk of Statin Toxicity & Myopathy",
            mechanism = "Potent CYP3A4 inhibition increases systemic atorvastatin exposure several-fold.",
            clinicalEffect = "Myalgia, marked creatine kinase elevation, and risk of rhabdomyolysis.",
            recommendation = "If combination is unavoidable, cap atorvastatin dose at 20 mg daily or temporarily interrupt statin therapy."
        ),
        Rule(
            drug1 = "sertraline",
            drug2 = "tramadol",
            severity = SeverityLevel.CRITICAL,
            title = "Life-Threatening Serotonin Syndrome",
            mechanism = "Sertraline blocks serotonin reuptake, while tramadol inhibits both serotonin and norepinephrine reuptake and stimulates serotonin release.",
            clinicalEffect = "Serotonin syndrome: hyperthermia, clonus, autonomic instability, delirium, rigidity, and seizure.",
            recommendation = "Avoid concurrent use. If necessary, educate patient on early symptoms (tremor, sweating, agitation) and seek emergency care immediately if observed."
        ),
        Rule(
            drug1 = "fluoxetine",
            drug2 = "tramadol",
            severity = SeverityLevel.CRITICAL,
            title = "Life-Threatening Serotonin Syndrome & Reduced Analgesia",
            mechanism = "Dual serotonergic stimulation plus CYP2D6 inhibition by fluoxetine preventing tramadol's conversion to active M1 metabolite.",
            clinicalEffect = "Severe serotonin syndrome accompanied by reduced pain control.",
            recommendation = "Avoid combination. Choose a non-serotonergic analgesic."
        ),
        Rule(
            drug1 = "alprazolam",
            drug2 = "oxycodone",
            severity = SeverityLevel.CRITICAL,
            title = "FDA Boxed Warning: Profound Sedation, Respiratory Depression & Death",
            mechanism = "Synergistic central nervous system (CNS) and respiratory center depression via dual GABA and mu-opioid activation.",
            clinicalEffect = "Severe hypoventilation, coma, respiratory arrest, and fatal overdose.",
            recommendation = "FDA Black Box Warning. Limit dosages and duration to the minimum required. Prescribe take-home Naloxone (Narcan) to patient/caregiver."
        ),
        Rule(
            drug1 = "lorazepam",
            drug2 = "hydrocodone",
            severity = SeverityLevel.CRITICAL,
            title = "FDA Boxed Warning: Fatal Respiratory Depression",
            mechanism = "Dual CNS depression on respiratory rhythm generation in the brainstem.",
            clinicalEffect = "Dangerous sedation, bradycardia, hypoxia, and respiratory failure.",
            recommendation = "Avoid co-prescribing whenever possible. Co-prescribe Naloxone and instruct family on emergency administration."
        ),
        Rule(
            drug1 = "digoxin",
            drug2 = "amiodarone",
            severity = SeverityLevel.CRITICAL,
            title = "Severe Digoxin Toxicity & Atrioventricular Block",
            mechanism = "Amiodarone inhibits renal and intestinal P-glycoprotein efflux transporters, reducing digoxin clearance by 50% or more.",
            clinicalEffect = "Digoxin toxicity: nausea, yellow-green visual halos, lethal ventricular arrhythmias, and severe bradycardia.",
            recommendation = "Reduce digoxin maintenance dose by 50% when starting amiodarone. Measure serum digoxin level within 1–2 weeks."
        ),
        Rule(
            drug1 = "methotrexate",
            drug2 = "ibuprofen",
            severity = SeverityLevel.CRITICAL,
            title = "Severe Methotrexate Toxicity & Myelosuppression",
            mechanism = "NSAIDs reduce renal perfusion via prostaglandin inhibition and competitively inhibit renal tubular secretion of methotrexate.",
            clinicalEffect = "Pancytopenia, severe bone marrow suppression, mucositis, and acute kidney injury.",
            recommendation = "Avoid high-dose methotrexate with NSAIDs. If co-prescribed with low-dose weekly methotrexate, monitor CBC, platelets, and renal function closely."
        ),
        Rule(
            drug1 = "lithium",
            drug2 = "ibuprofen",
            severity = SeverityLevel.CRITICAL,
            title = "Severe Lithium Toxicity",
            mechanism = "Inhibition of renal prostaglandins reduces glomerular filtration rate and renal clearance of lithium.",
            clinicalEffect = "Serum lithium levels can spike by 30–60%, triggering coarse tremors, ataxia, confusion, and irreversible neurotoxicity.",
            recommendation = "Avoid NSAIDs in patients on lithium. Acetaminophen, aspirin, or sulindac may be safer options with physician approval."
        ),
        Rule(
            drug1 = "clopidogrel",
            drug2 = "omeprazole",
            severity = SeverityLevel.MODERATE,
            title = "Reduced Antiplatelet Efficacy & Stent Thrombosis Risk",
            mechanism = "Omeprazole inhibits CYP2C19, preventing the bioactivation of clopidogrel into its active antiplatelet thiol metabolite.",
            clinicalEffect = "Decreased antiplatelet effect, elevated risk of recurrent myocardial infarction and coronary stent thrombosis.",
            recommendation = "Switch to a PPI with lower CYP2C19 inhibitory affinity (such as pantoprazole) or use an H2 blocker (famotidine)."
        ),
        Rule(
            drug1 = "ciprofloxacin",
            drug2 = "calcium",
            severity = SeverityLevel.MODERATE,
            title = "Antibiotic Chelation & Treatment Failure",
            mechanism = "Polyvalent multivalent cations (calcium, magnesium, aluminum, iron) form insoluble chelate complexes with fluoroquinolones in the GI tract.",
            clinicalEffect = "Dramatically reduced antibiotic absorption (up to 70-90%), leading to clinical treatment failure.",
            recommendation = "Administer ciprofloxacin at least 2 hours before or 6 hours after taking calcium supplements, antacids, or dairy meals."
        ),
        Rule(
            drug1 = "levothyroxine",
            drug2 = "calcium",
            severity = SeverityLevel.MODERATE,
            title = "Decreased Thyroid Hormone Absorption",
            mechanism = "Calcium carbonate adsorbs levothyroxine in an acidic gastric environment, creating an unabsorbable compound.",
            clinicalEffect = "Worsening hypothyroidism symptoms, elevated TSH levels.",
            recommendation = "Separate administration times by at least 4 hours. Take levothyroxine in the morning on an empty stomach with water."
        ),
        Rule(
            drug1 = "metformin",
            drug2 = "contrast",
            severity = SeverityLevel.CRITICAL,
            title = "Contrast-Induced Lactic Acidosis Risk",
            mechanism = "Intravascular iodinated radiocontrast can precipitate acute renal impairment, impairing metformin elimination.",
            clinicalEffect = "Severe metformin-associated lactic acidosis (MALA), with mortality rates exceeding 30%.",
            recommendation = "Discontinue metformin at the time of or prior to iodinated contrast procedures, and withhold for 48 hours until renal function is re-evaluated."
        ),
        Rule(
            drug1 = "apixaban",
            drug2 = "aspirin",
            severity = SeverityLevel.CRITICAL,
            title = "Major Bleeding Risk from Dual Antithrombotic Therapy",
            mechanism = "Factor Xa inhibition combined with antiplatelet COX-1 blockade.",
            clinicalEffect = "Substantially elevated major bleeding, GI hemorrhage, and intracranial bleeding.",
            recommendation = "Avoid routine aspirin unless patient has a recent coronary stent or ACS; limit duration to strictly necessary timeline."
        ),
        Rule(
            drug1 = "lisinopril",
            drug2 = "potassium",
            severity = SeverityLevel.CRITICAL,
            title = "Hazardous Hyperkalemia",
            mechanism = "ACE inhibitors decrease aldosterone, impeding renal potassium elimination while potassium supplement increases load.",
            clinicalEffect = "Arrhythmias, weakness, numbness, cardiac arrest.",
            recommendation = "Avoid potassium supplements unless prescribed specifically for documented hypokalemia under close lab supervision."
        ),
        Rule(
            drug1 = "celecoxib",
            drug2 = "lisinopril",
            severity = SeverityLevel.MODERATE,
            title = "Blunted Antihypertensive Effect & Acute Renal Impairment",
            mechanism = "COX-2 inhibition blocks vasodilatory prostaglandins in renal afferent arterioles, while ACE inhibitors dilate efferent arterioles.",
            clinicalEffect = "Loss of blood pressure control and decline in glomerular filtration rate ('triple whammy' risk if diuretic added).",
            recommendation = "Monitor blood pressure and serum creatinine. Use the lowest effective dose of celecoxib for the shortest duration."
        )
    )

    fun checkInteractions(medications: List<String>): List<DrugInteractionItem> {
        if (medications.size < 2) return emptyList()

        val normalized = medications.map { it to normalize(it) }
        val results = mutableListOf<DrugInteractionItem>()

        for (i in 0 until normalized.size) {
            for (j in i + 1 until normalized.size) {
                val (rawA, normA) = normalized[i]
                val (rawB, normB) = normalized[j]

                val matchedRule = interactionRules.find { rule ->
                    (rule.drug1 == normA && rule.drug2 == normB) ||
                    (rule.drug1 == normB && rule.drug2 == normA)
                }

                if (matchedRule != null) {
                    results.add(
                        DrugInteractionItem(
                            id = "${normA}_${normB}_${System.currentTimeMillis()}",
                            drugA = rawA,
                            drugB = rawB,
                            severity = matchedRule.severity,
                            title = matchedRule.title,
                            mechanism = matchedRule.mechanism,
                            clinicalEffect = matchedRule.clinicalEffect,
                            recommendation = matchedRule.recommendation,
                            source = "RxSafety Clinical Knowledge Engine (NIH / FDA Guidelines)"
                        )
                    )
                }
            }
        }

        return results.sortedBy { it.severity.sortOrder }
    }

    fun getAllCommonMedications(): List<String> = listOf(
        "Warfarin (Coumadin)",
        "Aspirin (Ecotrin)",
        "Lisinopril (Zestril)",
        "Spironolactone (Aldactone)",
        "Atorvastatin (Lipitor)",
        "Simvastatin (Zocor)",
        "Clarithromycin (Biaxin)",
        "Sertraline (Zoloft)",
        "Tramadol (Ultram)",
        "Oxycodone (OxyContin)",
        "Alprazolam (Xanax)",
        "Sildenafil (Viagra)",
        "Nitroglycerin (Nitrostat)",
        "Metformin (Glucophage)",
        "Digoxin (Lanoxin)",
        "Amiodarone (Pacerone)",
        "Ibuprofen (Advil)",
        "Naproxen (Aleve)",
        "Clopidogrel (Plavix)",
        "Omeprazole (Prilosec)",
        "Ciprofloxacin (Cipro)",
        "Levothyroxine (Synthroid)",
        "Lithium (Lithobid)",
        "Apixaban (Eliquis)",
        "Losartan (Cozaar)",
        "Potassium Chloride"
    )
}

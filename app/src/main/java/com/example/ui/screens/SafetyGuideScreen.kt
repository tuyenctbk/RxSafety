package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OnSeverityCriticalContainer
import com.example.ui.theme.SeverityCriticalContainer
import com.example.ui.theme.SeverityCriticalRed
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary

@Composable
fun SafetyGuideScreen(
    onOpenSupportDialog: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("safety_guide_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.widthIn(max = 640.dp)) {
                    // Header
                    Text(
                        text = "Clinical Safety Guide",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Emergency red flags, pharmacist checklist, and drug safety protocols",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Emergency Hotline Card (Poison Control)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SeverityCriticalContainer),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(SeverityCriticalRed, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "US Poison Control Help",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSeverityCriticalContainer
                                        )
                                        Text(
                                            text = "24/7 Free & Confidential Medical Guidance",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate700
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:18002221222")
                                        }
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SeverityCriticalRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("dial_poison_control_button")
                                ) {
                                    Text(stringResource(R.string.call_poison_control), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Critical Red Flag Symptoms
                    Text(
                        text = stringResource(R.string.red_flags_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    RedFlagCard(
                        syndrome = "Serotonin Syndrome (SS)",
                        drugs = "SSRIs, SNRIs, Tramadol, Triptans, MAOIs",
                        symptoms = "Agitation, confusion, rapid heart rate, shivering, severe muscle stiffness, tremors, high fever, and seizure."
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    RedFlagCard(
                        syndrome = "Severe Internal Bleeding",
                        drugs = "Warfarin, DOACs (Eliquis, Xarelto), Aspirin, NSAIDs",
                        symptoms = "Black tarry stools, coughing up blood, vomiting coffee-ground emesis, unusual weakness, severe persistent headache."
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    RedFlagCard(
                        syndrome = "Severe Angioedema & Airway Closure",
                        drugs = "ACE Inhibitors (Lisinopril, Enalapril), ARBs",
                        symptoms = "Rapid swelling of lips, tongue, throat, face, or larynx causing stridor and shortness of breath. Call 911 immediately."
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    RedFlagCard(
                        syndrome = "Severe Rhabdomyolysis",
                        drugs = "Statins combined with Macrolides or Azoles",
                        symptoms = "Severe diffuse muscle pain, profound weakness, and dark tea-colored urine indicating myoglobinuria."
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5 Pharmacist Questions
                    Text(
                        text = "📋 5 Questions to Ask Your Pharmacist:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PharmacistQuestionItem(
                        number = "1",
                        question = "Does this interact with any of my current prescription or OTC medications?",
                        tip = "Always bring a complete list of your current medicines, including vitamins, antacids, and herbal supplements."
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    PharmacistQuestionItem(
                        number = "2",
                        question = "Are there specific foods, juices, or alcohol I need to avoid?",
                        tip = "Common triggers include grapefruit, calcium/dairy products, potassium salt substitutes, and alcohol."
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    PharmacistQuestionItem(
                        number = "3",
                        question = "What should I do if I accidentally miss a dose?",
                        tip = "Never double up on doses without explicit instructions from your pharmacist or prescriber."
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    PharmacistQuestionItem(
                        number = "4",
                        question = "What are the early warning signs of an adverse reaction?",
                        tip = "Understand the difference between mild temporary side effects and urgent red flags requiring emergency care."
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    PharmacistQuestionItem(
                        number = "5",
                        question = "Does this medication require periodic laboratory monitoring?",
                        tip = "Drugs like Warfarin (INR), Lisinopril (Potassium/Creatinine), Digoxin, and Lithium need scheduled blood tests."
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Support Independent Development Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("about_support_dev_card"),
                        colors = CardDefaults.cardColors(containerColor = TealContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = TealPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.about_support_dev_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.TealDark
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.about_support_dev_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onOpenSupportDialog?.invoke() },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("guide_open_donation_button")
                            ) {
                                Text(stringResource(R.string.btn_support_options), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Disclaimer Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Slate600, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Government Open Data & Medical Disclaimer",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate600
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "RxSafety references 100% free public domain data from the US National Library of Medicine (NIH RxNav) and the US Food and Drug Administration (OpenFDA). This application is intended for educational and clinical screening purposes and does not substitute for professional medical judgment. Always consult a licensed physician or pharmacist regarding medication regimen changes.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RedFlagCard(
    syndrome: String,
    drugs: String,
    symptoms: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = syndrome,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SeverityCriticalRed
                )
                Surface(
                    color = SeverityCriticalContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "EMERGENCY",
                        color = OnSeverityCriticalContainer,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Associated with: $drugs",
                style = MaterialTheme.typography.labelSmall,
                color = Slate600,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = symptoms,
                style = MaterialTheme.typography.bodySmall,
                color = Slate700
            )
        }
    }
}

@Composable
fun PharmacistQuestionItem(
    number: String,
    question: String,
    tip: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(TealContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    color = TealPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    fontSize = 11.sp
                )
            }
        }
    }
}

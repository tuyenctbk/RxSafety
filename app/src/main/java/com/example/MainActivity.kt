package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.RepositoryModule
import com.example.di.RxSafetyViewModelFactory
import com.example.ui.components.MultiMethodDonationDialog
import com.example.ui.components.TopAlertBanner
import com.example.ui.screens.CheckerScreen
import com.example.ui.screens.InspectorScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.RecallsScreen
import com.example.ui.screens.RxNavSearchScreen
import com.example.ui.screens.SafetyGuideScreen
import com.example.ui.theme.NavySurfaceDark
import com.example.ui.theme.OnSeveritySafeContainer
import com.example.ui.theme.RxSafetyTheme
import com.example.ui.theme.SeveritySafeContainer
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.RxSafetyViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: RxSafetyViewModel by viewModels {
        RxSafetyViewModelFactory(
            RepositoryModule.provideMedicationRepository(applicationContext),
            RepositoryModule.provideDonationManager(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RxSafetyTheme {
                RxSafetyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RxSafetyApp(viewModel: RxSafetyViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val bannerAlert by viewModel.bannerAlert.collectAsState()

    val isSupporter by viewModel.isSupporter.collectAsState()
    val showDonationDialog by viewModel.showDonationDialog.collectAsState()
    val shouldShowAutoPrompt by viewModel.shouldShowAutoPrompt.collectAsState()
    val milestoneEvent by viewModel.milestoneEvent.collectAsState()

    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1200)
        showSplash = false
    }

    // Auto-prompt trigger on 7+ operations
    LaunchedEffect(shouldShowAutoPrompt) {
        if (shouldShowAutoPrompt) {
            viewModel.markAutoPromptShown()
            viewModel.openDonationDialog()
        }
    }

    // Milestone celebrations
    LaunchedEffect(milestoneEvent) {
        if (milestoneEvent != null) {
            val msg = when (milestoneEvent) {
                "10_CHECKS" -> context.getString(R.string.milestone_toast_10)
                "25_CHECKS" -> context.getString(R.string.milestone_toast_25)
                else -> "🎉 Milestone reached! Thank you for using RxSafety."
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearMilestoneEvent()
        }
    }

    // Handle Android system back button
    BackHandler(enabled = currentTab != 0 && !showSplash) {
        viewModel.setTab(0)
    }

    // Multi-Method Donation Dialog
    if (showDonationDialog) {
        MultiMethodDonationDialog(
            isSupporter = isSupporter,
            onDismiss = { viewModel.closeDonationDialog() },
            onSimulateSupport = { tierLabel ->
                viewModel.markAsSupporter(tierLabel)
            }
        )
    }

    if (showSplash) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavySurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.HealthAndSafety,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.splash_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400
                )
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.setTab(0) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 0) Icons.Filled.HealthAndSafety else Icons.Outlined.HealthAndSafety,
                                contentDescription = stringResource(R.string.nav_checker)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_checker), fontSize = 11.sp, fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealContainer
                        ),
                        modifier = Modifier.testTag("nav_item_checker")
                    )

                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.setTab(1) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 1) Icons.Filled.Search else Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.nav_rx_search)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_rx_search), fontSize = 11.sp, fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealContainer
                        ),
                        modifier = Modifier.testTag("nav_item_rx_search")
                    )

                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.setTab(2) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 2) Icons.Filled.WarningAmber else Icons.Outlined.WarningAmber,
                                contentDescription = stringResource(R.string.nav_recalls)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_recalls), fontSize = 11.sp, fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealContainer
                        ),
                        modifier = Modifier.testTag("nav_item_recalls")
                    )

                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { viewModel.setTab(3) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 3) Icons.Filled.Medication else Icons.Outlined.Medication,
                                contentDescription = stringResource(R.string.nav_profiles)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_profiles), fontSize = 11.sp, fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealContainer
                        ),
                        modifier = Modifier.testTag("nav_item_profiles")
                    )

                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { viewModel.setTab(4) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 4) Icons.Filled.Biotech else Icons.Outlined.Biotech,
                                contentDescription = stringResource(R.string.nav_inspector)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_inspector), fontSize = 11.sp, fontWeight = if (currentTab == 4) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealContainer
                        ),
                        modifier = Modifier.testTag("nav_item_inspector")
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header Top Row: Title, Supporter Badge, Donation Launcher, Safety Guide Launcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentTab != 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.setTab(0) }
                                .padding(vertical = 4.dp)
                                .testTag("back_to_checker_header")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = TealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.nav_checker),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TealPrimary
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.HealthAndSafety,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Supporter Badge or Support Action Button
                        if (isSupporter) {
                            Surface(
                                color = SeveritySafeContainer,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .clickable { viewModel.openDonationDialog() }
                                    .testTag("supporter_badge_header")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = OnSeveritySafeContainer, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.active_supporter_badge),
                                        color = OnSeveritySafeContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            IconButton(
                                onClick = { viewModel.openDonationDialog() },
                                modifier = Modifier.testTag("donation_header_icon_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Support Development",
                                    tint = TealPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.setTab(if (currentTab == 5) 0 else 5) },
                            modifier = Modifier.testTag("nav_guide_header_button")
                        ) {
                            Icon(
                                imageVector = if (currentTab == 5) Icons.Filled.Security else Icons.Outlined.Security,
                                contentDescription = stringResource(R.string.nav_guide),
                                tint = if (currentTab == 5) TealPrimary else Slate600
                            )
                        }
                    }
                }

                // Real-time top alert notification banner
                AnimatedVisibility(
                    visible = bannerAlert != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (bannerAlert != null) {
                        val alert = bannerAlert!!
                        TopAlertBanner(
                            title = alert.title,
                            message = alert.message,
                            severity = alert.severity,
                            onDismiss = { viewModel.dismissBanner() }
                        )
                    }
                }

                // Screen content
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentTab) {
                        0 -> CheckerScreen(viewModel = viewModel)
                        1 -> RxNavSearchScreen(viewModel = viewModel)
                        2 -> RecallsScreen(viewModel = viewModel)
                        3 -> ProfilesScreen(viewModel = viewModel)
                        4 -> InspectorScreen(viewModel = viewModel)
                        5 -> SafetyGuideScreen(onOpenSupportDialog = { viewModel.openDonationDialog() })
                    }
                }
            }
        }
    }
}

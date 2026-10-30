package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.theme.MedicalCyan
import com.example.ui.theme.NavySurfaceDark
import com.example.ui.theme.SeveritySafeGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.TealPrimary
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

data class ScannedMedicationResult(
    val rawCode: String,
    val medName: String,
    val dosage: String,
    val dosageForm: String,
    val rxcui: String = "",
    val instructions: String = ""
)

object BottleBarcodeParser {
    // Clinical NDC & Barcode Knowledge Base mapping
    private val ndcDatabase = mapOf(
        "00003-0147-20" to ScannedMedicationResult("00003-0147-20", "Lisinopril", "10 mg", "Tablet", "29046", "Take 1 tablet daily for blood pressure"),
        "03014720" to ScannedMedicationResult("03014720", "Lisinopril", "10 mg", "Tablet", "29046", "Take 1 tablet daily for blood pressure"),
        "00071-0155-23" to ScannedMedicationResult("00071-0155-23", "Lipitor (Atorvastatin)", "20 mg", "Tablet", "83367", "Take 1 tablet daily at bedtime"),
        "71015523" to ScannedMedicationResult("71015523", "Lipitor (Atorvastatin)", "20 mg", "Tablet", "83367", "Take 1 tablet daily at bedtime"),
        "00093-0058-01" to ScannedMedicationResult("00093-0058-01", "Amoxicillin", "500 mg", "Capsule", "308189", "Take 1 capsule every 8 hours with water"),
        "93005801" to ScannedMedicationResult("93005801", "Amoxicillin", "500 mg", "Capsule", "308189", "Take 1 capsule every 8 hours with water"),
        "00002-3228-30" to ScannedMedicationResult("00002-3228-30", "Prozac (Fluoxetine)", "20 mg", "Capsule", "205928", "Take 1 capsule daily in morning"),
        "2322830" to ScannedMedicationResult("2322830", "Prozac (Fluoxetine)", "20 mg", "Capsule", "205928", "Take 1 capsule daily in morning"),
        "00078-0358-15" to ScannedMedicationResult("00078-0358-15", "Metformin", "500 mg", "Tablet", "860975", "Take 1 tablet twice daily with meals"),
        "78035815" to ScannedMedicationResult("78035815", "Metformin", "500 mg", "Tablet", "860975", "Take 1 tablet twice daily with meals"),
        "00054-4221-25" to ScannedMedicationResult("00054-4221-25", "Warfarin", "5 mg", "Tablet", "11289", "Take 1 tablet daily as prescribed"),
        "54422125" to ScannedMedicationResult("54422125", "Warfarin", "5 mg", "Tablet", "11289", "Take 1 tablet daily as prescribed"),
        "00590-0112-01" to ScannedMedicationResult("00590-0112-01", "Aspirin", "81 mg", "Tablet", "1191", "Take 1 low-dose tablet daily")
    )

    fun parse(code: String): ScannedMedicationResult {
        val clean = code.trim().replace("-", "")
        
        // Match exact or partial NDC
        ndcDatabase.entries.firstOrNull { 
            it.key.replace("-", "") == clean || code.contains(it.key, ignoreCase = true)
        }?.let { return it.value }

        // Parse QR JSON or structured string like MED:Name|Dosage
        if (code.contains("med", ignoreCase = true) || code.contains("drug", ignoreCase = true)) {
            val parts = code.split("|", ":", ",")
            if (parts.size >= 2) {
                val name = parts[1].trim()
                val dose = if (parts.size >= 3) parts[2].trim() else "As prescribed"
                return ScannedMedicationResult(
                    rawCode = code,
                    medName = name,
                    dosage = dose,
                    dosageForm = "Tablet",
                    instructions = "Scanned from QR code"
                )
            }
        }

        // Default extracted barcode result
        return ScannedMedicationResult(
            rawCode = code,
            medName = if (code.length > 5) "Medication ($code)" else "Scanned Rx ($code)",
            dosage = "Standard dose",
            dosageForm = "Tablet",
            instructions = "Barcode NDC: $code"
        )
    }

    val samplePresets = listOf(
        ScannedMedicationResult("00003-0147-20", "Lisinopril", "10 mg", "Tablet", "29046", "Take 1 tablet daily for blood pressure"),
        ScannedMedicationResult("00071-0155-23", "Lipitor (Atorvastatin)", "20 mg", "Tablet", "83367", "Take 1 tablet daily at bedtime"),
        ScannedMedicationResult("00078-0358-15", "Metformin", "500 mg", "Tablet", "860975", "Take 1 tablet twice daily with meals"),
        ScannedMedicationResult("00054-4221-25", "Warfarin", "5 mg", "Tablet", "11289", "Take 1 tablet daily as prescribed"),
        ScannedMedicationResult("00093-0058-01", "Amoxicillin", "500 mg", "Capsule", "308189", "Take 1 capsule every 8 hours")
    )
}

@Composable
fun CameraBarcodeScannerDialog(
    onDismiss: () -> Unit,
    onSaveToDailyMeds: (ScannedMedicationResult) -> Unit,
    onStageForChecker: (ScannedMedicationResult) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var scannedResult by remember { mutableStateOf<ScannedMedicationResult?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavySurfaceDark)
                .testTag("camera_scanner_dialog")
        ) {
            if (!hasCameraPermission) {
                // Permission Request Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(TealPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.scanner_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.camera_permission_required),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.testTag("grant_camera_permission_button")
                    ) {
                        Text(stringResource(R.string.grant_camera_permission), color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    // Simulator fallback for testing without camera
                    Text(
                        text = stringResource(R.string.sample_barcodes_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MedicalCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BottleBarcodeParser.samplePresets.take(3).forEach { sample ->
                            OutlinedButton(
                                onClick = { scannedResult = sample },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${sample.medName} (${sample.rawCode})", color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.close), color = Slate400)
                    }
                }
            } else {
                // Live CameraX Preview + Barcode Scanning Overlay
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreviewView(
                        isTorchOn = isTorchOn,
                        onBarcodeScanned = { rawCode ->
                            if (scannedResult == null) {
                                scannedResult = BottleBarcodeParser.parse(rawCode)
                            }
                        }
                    )

                    // Viewfinder & Scanning laser animation
                    ScannerOverlay()

                    // Header Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.scanner_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.scanner_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { isTorchOn = !isTorchOn },
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = stringResource(R.string.torch_toggle),
                                    tint = if (isTorchOn) Color.Yellow else Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.close),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Bottom Simulation Bar (for quick clinical testing)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(NavySurfaceDark.copy(alpha = 0.95f))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.sample_barcodes_title),
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BottleBarcodeParser.samplePresets.forEach { sample ->
                                    Surface(
                                        onClick = { scannedResult = sample },
                                        shape = RoundedCornerShape(16.dp),
                                        color = TealPrimary.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary)
                                    ) {
                                        Text(
                                            text = sample.medName.substringBefore(" "),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Scanned Result Overlay Card
                    scannedResult?.let { result ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("scanned_result_card"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(SeveritySafeGreen.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SeveritySafeGreen,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = stringResource(R.string.scanned_med_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.medName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TealPrimary
                                    )
                                    Text(
                                        text = "${result.dosage} • ${result.dosageForm}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.scanned_code_label, result.rawCode),
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = Slate400
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = {
                                            onSaveToDailyMeds(result)
                                            onDismiss()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("save_scanned_to_daily_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                    ) {
                                        Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.add_scanned_to_daily))
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            onStageForChecker(result)
                                            onDismiss()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.add_scanned_to_checker))
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { scannedResult = null }) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.scan_again))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreviewView(
    isTorchOn: Boolean,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_ALL_FORMATS
                        )
                        .build()

                    val barcodeScanner = BarcodeScanning.getClient(options)

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy, onBarcodeScanned)
                    }

                    try {
                        provider.unbindAll()
                        val camera = provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                        cameraControl = camera.cameraControl
                    } catch (e: Exception) {
                        Log.e("CameraScanner", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        update = {
            cameraControl?.enableTorch(isTorchOn)
        },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { code ->
                        if (code.isNotBlank()) {
                            onBarcodeScanned(code)
                            break
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("BarcodeScanner", "Scanning failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@Composable
fun ScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_anim")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val boxWidth = width * 0.75f
        val boxHeight = width * 0.75f
        val left = (width - boxWidth) / 2
        val top = (height - boxHeight) / 2.5f

        // Semi-transparent background
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = Size(width, height)
        )

        // Clear scanning cutout viewport
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // Border target frame
        drawRoundRect(
            color = TealPrimary,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // Laser scan beam
        val laserY = top + (boxHeight * laserPosition)
        drawLine(
            color = SeveritySafeGreen,
            start = Offset(left + 16.dp.toPx(), laserY),
            end = Offset(left + boxWidth - 16.dp.toPx(), laserY),
            strokeWidth = 3.dp.toPx()
        )
    }
}

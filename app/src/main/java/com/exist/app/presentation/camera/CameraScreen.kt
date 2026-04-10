package com.exist.app.presentation.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaActionSound
import android.widget.Toast
import android.hardware.camera2.CaptureRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.exist.app.domain.model.EmotionTag
import com.exist.app.presentation.common.ExistBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.scale

@Composable
fun CameraScreen(
    viewModel: CaptureViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var lastNavigateSignal by remember { mutableIntStateOf(0) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var torchEnabled by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var cameraMessage by remember { mutableStateOf("") }
    var focusPoint by remember { mutableStateOf(Offset.Zero) }
    var focusMarkerVisible by remember { mutableStateOf(false) }
    var focusMarkerPulse by remember { mutableIntStateOf(0) }
    val focusAlpha by animateFloatAsState(if (focusMarkerVisible) 1f else 0f, label = "focus-alpha")
    val focusScale by animateFloatAsState(if (focusMarkerVisible) 1f else 0.75f, label = "focus-scale")

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(state.randomVideoChallengeEnabled) {
        viewModel.startCameraSession(state.randomVideoChallengeEnabled)
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(state.navigateBackSignal) {
        if (state.navigateBackSignal > lastNavigateSignal) {
            lastNavigateSignal = state.navigateBackSignal
            viewModel.consumeNavigateBackSignal()
            onSaved()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ExistBackground()

        if (!hasPermission) {
            PermissionState(onGrant = { launcher.launch(Manifest.permission.CAMERA) }, onBack = onBack)
        } else {
            CameraPreview(
                lifecycleOwner = lifecycleOwner,
                lensFacing = lensFacing,
                torchEnabled = torchEnabled,
                onCaptured = viewModel::onPhotoCaptured,
                onVideoRecorded = {
                    isRecording = false
                    viewModel.onChallengeVideoRecorded(it)
                },
                onRecordingStateChanged = { isRecording = it },
                onCameraMessage = { cameraMessage = it },
                onTapToFocus = { x, y ->
                    focusPoint = Offset(x, y)
                    focusMarkerPulse++
                }
            )

            CameraControls(
                savedCount = state.savedCountInSession,
                challengeRequired = state.challengeRequired,
                challengeCompleted = state.challengeCompleted,
                isRecording = isRecording,
                lensFacing = lensFacing,
                torchEnabled = torchEnabled,
                cameraMessage = state.saveMessage.ifBlank { cameraMessage },
                onBack = onBack,
                onSwitchLens = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        torchEnabled = false
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                onToggleTorch = {
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        torchEnabled = !torchEnabled
                    } else {
                        Toast.makeText(context, "Flash is available on back camera only.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        state.capturedPath?.let { path ->
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                EmotionTaggingSheet(
                    imagePath = path,
                    selectedEmotion = state.selectedEmotion,
                    caption = state.caption,
                    isTemporary = state.temporaryMemory,
                    isSaving = state.isSaving,
                    challengeRequired = state.challengeRequired,
                    challengeCompleted = state.challengeCompleted,
                    onEmotionSelected = viewModel::onEmotionSelected,
                    onCaptionChanged = viewModel::onCaptionChanged,
                    onTemporaryToggle = viewModel::onTemporaryToggled,
                    onDiscard = viewModel::clearCapturePreview,
                    onSaveAndContinue = viewModel::savePhotoAndContinue,
                    onSaveAndFinish = viewModel::savePhotoAndFinish
                )
            }
        }

        if (focusAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (focusPoint.x - 28.dp.toPx()).roundToInt(),
                            (focusPoint.y - 28.dp.toPx()).roundToInt()
                        )
                    }
                    .size(56.dp)
                    .scale(focusScale)
                    .alpha(focusAlpha)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
            )
        }
    }

    LaunchedEffect(focusMarkerPulse) {
        if (focusMarkerPulse == 0) return@LaunchedEffect
        focusMarkerVisible = true
        delay(650)
        focusMarkerVisible = false
    }
}

@Composable
private fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    lensFacing: Int,
    torchEnabled: Boolean,
    onCaptured: (String) -> Unit,
    onVideoRecorded: (String) -> Unit,
    onRecordingStateChanged: (Boolean) -> Unit,
    onCameraMessage: (String) -> Unit,
    onTapToFocus: (Float, Float) -> Unit
) {
    val context = LocalContext.current
    val shutterSound = remember { MediaActionSound().apply { load(MediaActionSound.SHUTTER_CLICK) } }
    val imageCapture = remember {
        val builder = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
        Camera2Interop.Extender(builder)
            .setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            .setCaptureRequestOption(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY)
        builder.build()
    }
    val recorder = remember {
        Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
    }
    val videoCapture = remember { VideoCapture.withOutput(recorder) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            activeRecording?.stop()
            activeRecording?.close()
            shutterSound.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        var boundCamera by remember { mutableStateOf<Camera?>(null) }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    tag = "unbound"
                }
            },
            update = { previewView ->
                val desiredTag = "camera-$lensFacing-$torchEnabled"
                if (previewView.tag != desiredTag) {
                    bindCamera(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        previewView = previewView,
                        imageCapture = imageCapture,
                        videoCapture = videoCapture,
                        lensFacing = lensFacing,
                        torchEnabled = torchEnabled,
                        onCameraReady = { camera -> boundCamera = camera }
                    )
                    previewView.tag = desiredTag
                }

                previewView.setOnTouchListener { _, event ->
                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                        val point = previewView.meteringPointFactory.createPoint(event.x, event.y)
                        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                        boundCamera?.cameraControl?.startFocusAndMetering(action)
                        onTapToFocus(event.x, event.y)
                    }
                    true
                }
            }
        )

        val scope = androidx.compose.runtime.rememberCoroutineScope()

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (activeRecording != null) return@IconButton
                    val outputFile = createCaptureFile(context)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
                    shutterSound.play(MediaActionSound.SHUTTER_CLICK)

                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                onCaptured(outputFile.absolutePath)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onCameraMessage("Photo capture failed. Try again.")
                            }
                        }
                    )
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), CircleShape)
                    .size(76.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "Capture photo",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = {
                    if (activeRecording == null) {
                        val file = createVideoFile(context)
                        val output = FileOutputOptions.Builder(file).build()
                        val recording = videoCapture.output
                            .prepareRecording(context, output)
                            .start(ContextCompat.getMainExecutor(context)) { event ->
                                when (event) {
                                    is VideoRecordEvent.Start -> onRecordingStateChanged(true)
                                    is VideoRecordEvent.Finalize -> {
                                        onRecordingStateChanged(false)
                                        activeRecording = null
                                        if (!event.hasError()) {
                                            onVideoRecorded(file.absolutePath)
                                        } else {
                                            onCameraMessage("Video challenge failed. Try again.")
                                        }
                                    }
                                }
                            }
                        activeRecording = recording
                        onRecordingStateChanged(true)
                        onCameraMessage("Recording 10-second challenge clip...")
                        scope.launch {
                            delay(10_000)
                            activeRecording?.stop()
                        }
                    } else {
                        activeRecording?.stop()
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    .size(62.dp)
            ) {
                Icon(
                    imageVector = if (activeRecording == null) Icons.Rounded.Videocam else Icons.Rounded.Stop,
                    contentDescription = if (activeRecording == null) "Record 10-second video" else "Stop recording",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CameraControls(
    savedCount: Int,
    challengeRequired: Boolean,
    challengeCompleted: Boolean,
    isRecording: Boolean,
    lensFacing: Int,
    torchEnabled: Boolean,
    cameraMessage: String,
    onBack: () -> Unit,
    onSwitchLens: () -> Unit,
    onToggleTorch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onToggleTorch,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (torchEnabled && lensFacing == CameraSelector.LENS_FACING_BACK) {
                            Icons.Rounded.FlashOn
                        } else {
                            Icons.Rounded.FlashOff
                        },
                        contentDescription = "Toggle flash"
                    )
                }
                IconButton(
                    onClick = onSwitchLens,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Cameraswitch, contentDescription = "Flip camera")
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (challengeRequired || cameraMessage.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (challengeRequired) {
                        val challengeText = when {
                            isRecording -> "Challenge: recording 10s clip..."
                            challengeCompleted -> "Challenge complete."
                            else -> "Challenge active: record a 10s clip before save."
                        }
                        Text(challengeText, color = MaterialTheme.colorScheme.primary)
                    } else if (savedCount > 0) {
                        Text("Saved in this session: $savedCount", color = MaterialTheme.colorScheme.tertiary)
                    }
                    if (cameraMessage.isNotBlank()) {
                        Text(cameraMessage, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmotionTaggingSheet(
    imagePath: String,
    selectedEmotion: EmotionTag,
    caption: String,
    isTemporary: Boolean,
    isSaving: Boolean,
    challengeRequired: Boolean,
    challengeCompleted: Boolean,
    onEmotionSelected: (EmotionTag) -> Unit,
    onCaptionChanged: (String) -> Unit,
    onTemporaryToggle: (Boolean) -> Unit,
    onDiscard: () -> Unit,
    onSaveAndContinue: () -> Unit,
    onSaveAndFinish: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Tag this moment", style = MaterialTheme.typography.headlineMedium)

            AsyncImage(
                model = imagePath,
                contentDescription = "Captured photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            OutlinedTextField(
                value = caption,
                onValueChange = onCaptionChanged,
                label = { Text("Caption (optional)") },
                singleLine = false,
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(EmotionTag.entries) { emotion ->
                    AssistChip(
                        onClick = { onEmotionSelected(emotion) },
                        label = { Text(emotion.displayName) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (emotion == selectedEmotion) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Temporary memory")
                Switch(checked = isTemporary, onCheckedChange = onTemporaryToggle)
            }

            if (challengeRequired && !challengeCompleted) {
                Text(
                    "Record a 10-second challenge video first to enable saving.",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onDiscard,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = null)
                }
                Button(
                    onClick = onSaveAndContinue,
                    enabled = !isSaving && (!challengeRequired || challengeCompleted),
                    modifier = Modifier.weight(2f)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save + Add Another")
                    }
                }
                Button(
                    onClick = onSaveAndFinish,
                    enabled = !isSaving && (!challengeRequired || challengeCompleted),
                    modifier = Modifier.weight(2f)
                ) {
                    Text("Save + Finish")
                }
            }
        }
    }
}

@Composable
private fun PermissionState(onGrant: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera access is required to capture your proof moments.")
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onGrant) { Text("Allow Camera") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}

private fun bindCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    videoCapture: VideoCapture<Recorder>,
    lensFacing: Int,
    torchEnabled: Boolean,
    onCameraReady: (Camera) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        cameraProvider.unbindAll()
        val camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture, videoCapture)
        camera.cameraControl.enableTorch(torchEnabled && lensFacing == CameraSelector.LENS_FACING_BACK)
        onCameraReady(camera)
    }, ContextCompat.getMainExecutor(context))
}

private fun createCaptureFile(context: Context): File {
    val capturesDir = File(context.filesDir, "captures")
    if (!capturesDir.exists()) capturesDir.mkdirs()
    return File(capturesDir, "exist_${System.currentTimeMillis()}.jpg")
}

private fun createVideoFile(context: Context): File {
    val capturesDir = File(context.filesDir, "captures")
    if (!capturesDir.exists()) capturesDir.mkdirs()
    return File(capturesDir, "exist_video_${System.currentTimeMillis()}.mp4")
}

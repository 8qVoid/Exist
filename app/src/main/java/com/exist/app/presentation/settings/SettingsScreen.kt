package com.exist.app.presentation.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.exist.app.presentation.common.ExistBackground

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val session by viewModel.authSession.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var displayNameDraft by remember(settings.displayName) { mutableStateOf(settings.displayName) }
    var taglineDraft by remember(settings.profileTagline) { mutableStateOf(settings.profileTagline) }
    var photoUriDraft by remember(settings.profilePhotoUri) { mutableStateOf(settings.profilePhotoUri) }

    val notificationsGranted = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    Box(modifier = Modifier.fillMaxSize()) {
        ExistBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.displayLarge)
            Button(onClick = onBack) { Text("Back") }

            SettingsCard("Account") {
                Text(if (session.isAuthenticated) "Signed in as ${session.email}" else "Not signed in")
                if (session.isAuthenticated) {
                    Button(onClick = viewModel::signOut) { Text("Sign out") }
                }
            }

            SettingsCard("Profile") {
                OutlinedTextField(
                    value = displayNameDraft,
                    onValueChange = {
                        displayNameDraft = it.take(24)
                        viewModel.setDisplayName(displayNameDraft)
                    },
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = taglineDraft,
                    onValueChange = {
                        taglineDraft = it.take(80)
                        viewModel.setProfileTagline(taglineDraft)
                    },
                    label = { Text("Tagline (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = photoUriDraft,
                    onValueChange = {
                        photoUriDraft = it
                        viewModel.setProfilePhotoUri(photoUriDraft)
                    },
                    label = { Text("Profile photo URI (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (settings.profilePhotoUri.isNotBlank()) {
                    AsyncImage(
                        model = settings.profilePhotoUri,
                        contentDescription = "Profile image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            }

            SettingsCard("Random daily prompt") {
                SwitchRow(
                    label = "Enable poetic reminders",
                    checked = settings.randomPromptEnabled,
                    onCheckedChange = viewModel::setRandomPrompt
                )
                Text(
                    "Sends one quiet reminder notification at a random time each day.",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            SettingsCard("Random 10s video challenge") {
                SwitchRow(
                    label = "Enable random challenge",
                    checked = settings.randomVideoChallengeEnabled,
                    onCheckedChange = viewModel::setRandomVideoChallenge
                )
                Text(
                    "When enabled, some camera sessions require a 10-second clip before saving photos.",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            SettingsCard("Temporary memory duration") {
                Text("${settings.temporaryDurationDays} days", color = MaterialTheme.colorScheme.tertiary)
                Slider(
                    value = settings.temporaryDurationDays.toFloat(),
                    onValueChange = { viewModel.setTemporaryDuration(it.toInt()) },
                    valueRange = 1f..30f,
                    steps = 28
                )
                Text(
                    "Temporary photos expire after this many days.",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            SettingsCard("Notifications") {
                Text(
                    text = if (notificationsGranted) "Permission granted"
                    else "Permission required for prompts",
                    color = MaterialTheme.colorScheme.tertiary
                )
                if (!notificationsGranted && Build.VERSION.SDK_INT >= 33) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                        Text("Allow Notifications")
                    }
                }
            }

            SettingsCard("About Exist") {
                Text(
                    "One day, one proof. Exist is a personal memory ritual for presence, not volume.",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.87f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            content()
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

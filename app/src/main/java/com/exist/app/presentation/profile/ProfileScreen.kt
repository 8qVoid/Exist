package com.exist.app.presentation.profile

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
import com.exist.app.presentation.settings.SettingsViewModel

@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    onOpenAdvancedSettings: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val session by viewModel.authSession.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var displayName by remember(settings.displayName) { mutableStateOf(settings.displayName) }
    var tagline by remember(settings.profileTagline) { mutableStateOf(settings.profileTagline) }
    var photoUri by remember(settings.profilePhotoUri) { mutableStateOf(settings.profilePhotoUri) }

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
            Text("Profile", style = MaterialTheme.typography.displayLarge)
            Text(
                if (session.isAuthenticated) session.email else "Signed out",
                color = MaterialTheme.colorScheme.tertiary
            )

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (settings.profilePhotoUri.isNotBlank()) {
                        AsyncImage(
                            model = settings.profilePhotoUri,
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                        )
                    }
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = {
                            displayName = it.take(24)
                            viewModel.setDisplayName(displayName)
                        },
                        label = { Text("Display name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tagline,
                        onValueChange = {
                            tagline = it.take(80)
                            viewModel.setProfileTagline(tagline)
                        },
                        label = { Text("Tagline") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = photoUri,
                        onValueChange = {
                            photoUri = it
                            viewModel.setProfilePhotoUri(photoUri)
                        },
                        label = { Text("Photo URI (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SwitchRow("Random daily prompt", settings.randomPromptEnabled, viewModel::setRandomPrompt)
                    Text("One random reminder notification per day.", color = MaterialTheme.colorScheme.tertiary)
                    SwitchRow("Random 10s video challenge", settings.randomVideoChallengeEnabled, viewModel::setRandomVideoChallenge)
                    Text("Sometimes requires a 10-second clip before saving photos.", color = MaterialTheme.colorScheme.tertiary)
                    Text("Temporary memory duration: ${settings.temporaryDurationDays} days")
                    Slider(
                        value = settings.temporaryDurationDays.toFloat(),
                        onValueChange = { viewModel.setTemporaryDuration(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28
                    )
                    if (!notificationsGranted && Build.VERSION.SDK_INT >= 33) {
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                            Text("Allow notifications")
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onOpenAdvancedSettings, modifier = Modifier.weight(1f)) {
                    Text("Advanced")
                }
                Button(onClick = viewModel::signOut, modifier = Modifier.weight(1f)) {
                    Text("Sign out")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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

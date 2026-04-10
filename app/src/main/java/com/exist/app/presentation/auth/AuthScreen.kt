package com.exist.app.presentation.auth

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.exist.app.presentation.common.ExistBackground
import java.io.File
import java.io.FileOutputStream

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.onProfilePhotoUriChanged(uri.toString())
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val uri = saveBitmap(context, it)
            viewModel.onProfilePhotoUriChanged(uri.toString())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ExistBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Exist", style = MaterialTheme.typography.displayLarge)

                    when (state.step) {
                        AuthStep.CREDENTIALS -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = { viewModel.switchMode(AuthMode.SIGN_IN) }) { Text("Sign in") }
                                Button(onClick = { viewModel.switchMode(AuthMode.SIGN_UP) }) { Text("Sign up") }
                            }

                            OutlinedTextField(
                                value = state.email,
                                onValueChange = viewModel::onEmailChanged,
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = state.password,
                                onValueChange = viewModel::onPasswordChanged,
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (state.mode == AuthMode.SIGN_UP) {
                                OutlinedTextField(
                                    value = state.confirmPassword,
                                    onValueChange = viewModel::onConfirmPasswordChanged,
                                    label = { Text("Confirm password") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Button(onClick = { viewModel.submitCredentials() }, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
                                Text(if (state.mode == AuthMode.SIGN_UP) "Create account" else "Log in")
                            }
                        }

                        AuthStep.ONBOARDING -> {
                            Text("Profile onboarding")
                            OutlinedTextField(
                                value = state.fullName,
                                onValueChange = viewModel::onFullNameChanged,
                                label = { Text("Full name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = state.birthday,
                                onValueChange = viewModel::onBirthdayChanged,
                                label = { Text("Birthday (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (state.profilePhotoUri.isNotBlank()) {
                                AsyncImage(
                                    model = state.profilePhotoUri,
                                    contentDescription = "Profile photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = { galleryLauncher.launch("image/*") }) { Text("Pick photo") }
                                Button(onClick = { cameraLauncher.launch(null) }) { Text("Take photo") }
                            }

                            Button(onClick = { viewModel.completeOnboarding() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Finish")
                            }
                        }
                    }

                    if (state.error.isNotBlank()) {
                        Text(state.error, color = MaterialTheme.colorScheme.error)
                    }
                    if (state.message.isNotBlank()) {
                        Text(state.message, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

private fun saveBitmap(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.filesDir, "profile_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.toUri()
}

package com.nyxtesla.talk2u

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(onBackRequested: () -> Unit, onSignOut: () -> Unit, onAdminRequested: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val email = auth.currentUser?.email ?: ""
    val scope = rememberCoroutineScope()
    var showTestDialog by remember { mutableStateOf(false) }
    var testPrompt by remember { mutableStateOf("Hello, what's new?") }
    var testResponse by remember { mutableStateOf<String?>(null) }
    var testingLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Settings", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onBackRequested) { Text("Close") }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Appearance")
            Switch(checked = AppSettings.darkMode.value, onCheckedChange = { AppSettings.darkMode.value = it })
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Notifications")
            Switch(checked = AppSettings.notificationsEnabled.value, onCheckedChange = { AppSettings.notificationsEnabled.value = it })
        }

        OutlinedButton(onClick = { showTestDialog = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("Test AI")
        }

        if (email == "rmsm369@gmail.com") {
            Button(onClick = onAdminRequested, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Admin Panel") }
        }

        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Sign Out") }
    }

    if (showTestDialog) {
        AlertDialog(
            onDismissRequest = { showTestDialog = false; testResponse = null },
            title = { Text("Test AI") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = testPrompt, onValueChange = { testPrompt = it }, label = { Text("Prompt") }, modifier = Modifier.fillMaxWidth())
                    if (testingLoading) Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(); Spacer(Modifier.width(8.dp)); Text("Thinking...") }
                    testResponse?.let { Text(it) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    testingLoading = true
                    testResponse = null
                    scope.launch {
                        try {
                            testResponse = "AI is now handled by the web app. Open Chat from Home or visit https://rmsm369-tech.github.io/hoxip.ai/index.html"
                        } catch (e: Exception) {
                            testResponse = e.message ?: "Error"
                        } finally {
                            testingLoading = false
                        }
                    }
                }) { Text("Open Chat") }
            },
            dismissButton = { TextButton(onClick = { showTestDialog = false; testResponse = null }) { Text("Close") } }
        )
    }
}

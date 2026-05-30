package com.nyxtesla.talk2u

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val hoxipUrl = "https://rmsm369-tech.github.io/hoxip.ai/index.html"

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AI Chat Ready", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("For the best AI experience, please continue in your browser.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(hoxipUrl))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Chat with AI")
        }
    }
}

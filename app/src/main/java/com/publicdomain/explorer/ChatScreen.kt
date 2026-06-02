package com.nyxtesla.talk2u

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*

// --- FIREBASE IMPORTS ---
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@Composable
fun ChatScreen() {
   
    val context = LocalContext.current

    // --- NEW QUOTE SNIPPET ---
    val quotes = listOf(
        "“Truth is not a concept, truth is a realization.”",
        "“The mind is a beautiful servant, a dangerous master.”",
        "“I write from the shadows — where truth hides.”",
        "“To understand everything is to forgive everything.”"
    )
    // -------------------------
    val hoxipUrl = "https://rmsm369-tech.github.io/hoxip.ai/index.html"

    // --- PASTE THIS SETUP BLOCK HERE ---
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    
    // 1. LOCAL STORAGE KEPT: Fast local check
    val sharedPrefs = context.getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
    var isTelegramConnected by remember { 
        mutableStateOf(sharedPrefs.getBoolean("is_telegram_connected", false)) 
    }
    // ADD THIS NEW LINE
    var isLoading by remember { mutableStateOf(false) }

    // 2. BACKUP SECURITY: Check DB only if local says they aren't connected
    remember(user) {
        if (user != null && !isTelegramConnected) {
            db.collection("telegram_links")
                .whereEqualTo("uid", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // They uninstalled, but DB remembers them. Fix local storage.
                        isTelegramConnected = true
                        sharedPrefs.edit().putBoolean("is_telegram_connected", true).apply()
                    }
                }
        }
    }
    // --- END SETUP BLOCK ---


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
       
                
        
            
        

        // --- 2. YOUR ORIGINAL UI ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
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

        // --- 3. TELEGRAM BUTTON WITH DIAGNOSTICS ---
       
    Button(
            onClick = {
                // 1. PREVENT DOUBLE TAPS
                if (isLoading) return@Button
                
                val botUsername = "Samaham_omniagent_bot"

                if (isTelegramConnected) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$botUsername"))

                    // --- SHOW QUOTE HERE ---
                    val dailyQuote = quotes.random()
                    Toast.makeText(context, dailyQuote, Toast.LENGTH_LONG).show()
                    
                    context.startActivity(intent)
                    return@Button
                }

                try {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        Toast.makeText(context, "AUTH FAIL: You are not logged in.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // 2. LOCK BUTTON UI
                    isLoading = true 

                    val db = FirebaseFirestore.getInstance()
                    
                    db.collection("telegram_links")
                        .whereEqualTo("uid", user.uid)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                sharedPrefs.edit().putBoolean("is_telegram_connected", true).apply()
                                isTelegramConnected = true
                                isLoading = false // UNLOCK
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$botUsername"))
                                context.startActivity(intent)
                            } else {
                                val token = UUID.randomUUID().toString().substring(0, 6).uppercase()
                                val linkData = hashMapOf(
                                    "uid" to user.uid,
                                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                )
                                
                                db.collection("telegram_links").document(token)
                                    .set(linkData)
                                    .addOnSuccessListener {
                                        sharedPrefs.edit().putBoolean("is_telegram_connected", true).apply()
                                        isTelegramConnected = true
                                        isLoading = false // UNLOCK
                                        
                                        Toast.makeText(context, "Firebase OK. Opening Telegram...", Toast.LENGTH_SHORT).show()
                                        
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$botUsername?start=$token"))
                                        context.startActivity(intent)
                                        
                                        val dailyQuote = quotes.random()
        Toast.makeText(context, dailyQuote, Toast.LENGTH_LONG).show()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false // UNLOCK ON ERROR
                                        Toast.makeText(context, "DB FAIL: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            isLoading = false // UNLOCK ON ERROR
                            Toast.makeText(context, "Network Check Fail: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } catch (e: Exception) {
                    isLoading = false // UNLOCK ON ERROR
                    Toast.makeText(context, "SYSTEM CRASH: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            // 3. DISABLE BUTTON VISUALLY WHEN LOADING
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0B2E59),
                disabledContainerColor = Color(0xFF0B2E59).copy(alpha = 0.5f) // Dim when loading
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            // 4. SHOW CIRCULAR PROGRESS WIDGET OR TEXT
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF4DB8FF),
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isTelegramConnected) "Open Telegram (Connected)" else "Connect Telegram for ₹0.00",
                    color = Color(0xFF4DB8FF),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }    
}
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

// --- FIREBASE IMPORTS ---
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val hoxipUrl = "https://rmsm369-tech.github.io/hoxip.ai/index.html"

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
                try {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        Toast.makeText(context, "AUTH FAIL: You are not logged in.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    Toast.makeText(context, "Auth OK. Writing to Firebase...", Toast.LENGTH_SHORT).show()
                    
                    val db = FirebaseFirestore.getInstance()
                    val token = UUID.randomUUID().toString().substring(0, 6).uppercase()
                    val linkData = hashMapOf(
                        "uid" to user.uid,
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                    
                    db.collection("telegram_links").document(token)
                        .set(linkData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Firebase OK. Opening Telegram...", Toast.LENGTH_SHORT).show()
                            // REPLACE WITH REAL BOT USERNAME OR IT WILL CRASH
                            val botUsername = "Samaham_omniagent_bot" 
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$botUsername?start=$token"))
                            context.startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "DB FAIL: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "SYSTEM CRASH: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0B2E59)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Connect Telegram for ₹0.00",
                color = Color(0xFF4DB8FF),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
package com.nyxtesla.talk2u

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun AdminScreen(onBack: () -> Unit) {
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("users").get().await()
            users = snapshot.documents.map {
                val data = it.data ?: mapOf<String, Any>()
                val map = HashMap<String, Any>(data)
                map["id"] = it.id
                map
            }
        } catch (_: Exception) { }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Admin Panel", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onBack) { Text("Close") }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(users) { user ->
                Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(user["email"] as? String ?: "(no email)")
                        val uid = user["uid"] as? String ?: user["id"] as? String ?: ""
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                // toggle ban
                                val doc = db.collection("users").document(uid)
                                val isBanned = user["isBanned"] as? Boolean ?: false
                                doc.update("isBanned", !isBanned)
                            }) { Text("Toggle Ban") }
                        }
                    }
                }
            }
        }
    }
}

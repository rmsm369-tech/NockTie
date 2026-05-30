package com.nyxtesla.talk2u

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun NotesScreen() {
    val context = LocalContext.current
    val db = NotesDatabase.getDatabase(context)
    val dao = db.noteDao()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()
    val notes by dao.getNotes(userId).collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf<Note?>(null) }
    var editNote by remember { mutableStateOf<Note?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val dailyQuote = prefs.getString("daily_quote", null)

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("My Notes", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            dailyQuote?.let { q ->
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Daily Quote", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(6.dp))
                        Text(q, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search notes...") })
            Spacer(Modifier.height(12.dp))
            val filtered = if (query.isBlank()) notes else notes.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes found.\nTap + to create one.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtered, key = { it.id }) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (note.title.isNotBlank()) {
                                        Text(note.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    Text(
                                        note.content,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column {
                                    IconButton(onClick = { editNote = note }) {
                                        Icon(Icons.Default.Add, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { showDelete = note }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false; title = ""; content = "" },
            title = { Text("New Note", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Write your note...") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (content.isNotBlank()) {
                        scope.launch {
                                val newNote = Note(userId = userId, title = title.trim(), content = content.trim())
                                dao.insert(newNote)
                                try {
                                    NotificationHelper.notifyImmediate(context, 3001, if (newNote.title.isNotBlank()) newNote.title else "Note saved", newNote.content.take(120))
                                } catch (_: Exception) {}
                                title = ""; content = ""; showAdd = false
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false; title = ""; content = "" }) { Text("Cancel") } }
        )
    }

    showDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { showDelete = null },
            title = { Text("Delete Note?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                Button(onClick = { scope.launch { dao.delete(note) }; showDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { showDelete = null }) { Text("Cancel") } }
        )
    }

    editNote?.let { note ->
        var editTitle by remember { mutableStateOf(note.title) }
        var editContent by remember { mutableStateOf(note.content) }
        AlertDialog(
            onDismissRequest = { editNote = null },
            title = { Text("Edit Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = editContent, onValueChange = { editContent = it }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 6)
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch { dao.insert(note.copy(title = editTitle.trim(), content = editContent.trim())) }
                    editNote = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editNote = null }) { Text("Cancel") } }
        )
    }
}

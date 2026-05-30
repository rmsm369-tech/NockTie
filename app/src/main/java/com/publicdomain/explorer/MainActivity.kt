package com.nyxtesla.talk2u

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load Dark Mode state physically from device storage
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedDarkMode = sharedPref.getBoolean("isDarkMode", false)
        // Mark session start time for usage tracking
        sharedPref.edit().putLong("app_session_start", System.currentTimeMillis()).apply()

        // Ensure notification channel exists and schedule daily quote worker
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "daily_quotes"
                val channel = NotificationChannel(channelId, "Daily Quotes", NotificationManager.IMPORTANCE_DEFAULT)
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(channel)
            }
        } catch (_: Exception) {}

        try {
            val workRequest = PeriodicWorkRequestBuilder<QuotesWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork("daily_quotes", ExistingPeriodicWorkPolicy.KEEP, workRequest)
        } catch (_: Exception) {}

        setContent {
            var isDarkMode by remember { mutableStateOf(savedDarkMode) }
            val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme = colorScheme) {
                Surface {
                    AppNavigation(
                        darkMode = isDarkMode,
                        onToggleDark = {
                            isDarkMode = it
                            // Save choice permanently when flipped
                            sharedPref.edit().putBoolean("isDarkMode", it).apply()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        try {
            val prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            val start = prefs.getLong("app_session_start", System.currentTimeMillis())
            val now = System.currentTimeMillis()
            val elapsed = (now - start).coerceAtLeast(0L)
            val prev = prefs.getLong("total_time_ms", 0L)
            prefs.edit().putLong("total_time_ms", prev + elapsed).apply()
        } catch (_: Exception) {
        }
        super.onDestroy()
    }
}

@Composable
fun AppNavigation(darkMode: Boolean = false, onToggleDark: (Boolean) -> Unit = {}) {
    val auth = FirebaseAuth.getInstance()
    // Show a brief logo splash on startup, then route instantly to login/home
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        showSplash = false
    }

    // INSTANT ROUTING: No Gateway, no database checks.
    // If you have a Google token, you go straight to "home".
    var screen by remember { mutableStateOf(if (auth.currentUser != null) "home" else "login") }

    if (showSplash) {
        LogoSplashScreen()
    } else {
        when (screen) {
            "login" -> LoginScreen(onLoginSuccess = { screen = "home" })
            "home" -> HomeScreen(onSignOut = { auth.signOut(); screen = "login" }, darkMode = darkMode, onToggleDark = onToggleDark)
        }
    }
}

@Composable
fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nocktie", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Your AI self", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
    }
}

// QuoteSplashScreen removed; replaced by LogoSplashScreen for a branded static launch.

@Composable
fun HomeScreen(onSignOut: () -> Unit, darkMode: Boolean, onToggleDark: (Boolean) -> Unit) {
    var tab by remember { mutableStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.Note, contentDescription = "Notes") }, label = { Text("Notes") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") }, label = { Text("Chat") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }, label = { Text("Profile") })
                    NavigationBarItem(selected = tab == 3, onClick = { tab = 3 }, icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Games") }, label = { Text("Games") })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                0 -> NotesScreen()
                1 -> ChatScreen()
                2 -> ProfileScreen(onSignOut = onSignOut, darkMode = darkMode, onToggleDark = onToggleDark)
                3 -> GamesScreen()
            }
        }
    }
}

@Composable
fun ProfileScreen(onSignOut: () -> Unit, darkMode: Boolean, onToggleDark: (Boolean) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val userId = user?.uid ?: ""
    val email = user?.email ?: "User"
    val initials = email.take(2).uppercase()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = NotesDatabase.getDatabase(context)
    val dao = db.noteDao()

    var showAddNote by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    var locationText by remember { mutableStateOf("Unknown") }
    var locationLoading by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scope.launch {
                locationLoading = true
                try {
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                    var loc: Location? = null
                    for (p in providers) {
                        try {
                            loc = lm.getLastKnownLocation(p)
                            if (loc != null) break
                        } catch (_: Exception) { }
                    }
                    if (loc != null) {
                        val geos = withContext(Dispatchers.IO) { Geocoder(context, Locale.getDefault()).getFromLocation(loc.latitude, loc.longitude, 1) }
                        if (!geos.isNullOrEmpty()) {
                            val addr = geos[0]
                            val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Unknown"
                            val county = addr.subAdminArea ?: addr.adminArea ?: "Unknown"
                            locationText = "$city, $county"
                        } else {
                            locationText = "Unknown"
                        }
                    } else {
                        locationText = "Location unavailable"
                    }
                } catch (e: Exception) {
                    locationText = "Error: ${e.message}"
                } finally {
                    locationLoading = false
                }
            }
        } else {
            locationText = "Permission denied"
        }
    }

    // Session timer
    val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    var sessionElapsed by remember { mutableStateOf(0L) }
    var totalPrevious by remember { mutableStateOf(prefs.getLong("total_time_ms", 0L)) }
    LaunchedEffect(Unit) {
        while (true) {
            val start = prefs.getLong("app_session_start", System.currentTimeMillis())
            val now = System.currentTimeMillis()
            sessionElapsed = now - start
            totalPrevious = prefs.getLong("total_time_ms", 0L)
            delay(1000)
        }
    }

    fun formatMs(ms: Long): String {
        val s = ms / 1000
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return "%02d:%02d:%02d".format(h, m, sec)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.height(16.dp))
        Text(email, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Sign Out")
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(if (darkMode) "Dark Mode" else "Light Mode", modifier = Modifier.weight(1f))
            Switch(checked = darkMode, onCheckedChange = { onToggleDark(it) })
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showAddNote = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
                Spacer(Modifier.width(8.dp))
                Text("Add Note")
            }
            OutlinedButton(onClick = { locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                Text(if (locationLoading) "Locating..." else "Update Location")
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Try Nocktie")
                    putExtra(Intent.EXTRA_TEXT, "Try Nocktie: https://rmsm369-tech.github.io/hoxip.ai/index.html")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share App"))
            } catch (_: Exception) {}
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Share App")
        }

        Spacer(Modifier.height(12.dp))
        Text("Location: $locationText", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Text("Session: ${formatMs(sessionElapsed)}", style = MaterialTheme.typography.bodyMedium)
        Text("Total: ${formatMs(totalPrevious + sessionElapsed)}", style = MaterialTheme.typography.bodyMedium)
    }

    if (showAddNote) {
        AlertDialog(
            onDismissRequest = { showAddNote = false; noteTitle = ""; noteContent = "" },
            title = { Text("New Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = noteTitle, onValueChange = { noteTitle = it }, label = { Text("Title (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = noteContent, onValueChange = { noteContent = it }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth().height(120.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (noteContent.isNotBlank()) {
                        scope.launch {
                            val newNote = Note(userId = userId, title = noteTitle.trim(), content = noteContent.trim())
                            dao.insert(newNote)
                            noteTitle = ""; noteContent = ""; showAddNote = false
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAddNote = false; noteTitle = ""; noteContent = "" }) { Text("Cancel") } }
        )
    }
}
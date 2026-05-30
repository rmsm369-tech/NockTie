package com.nyxtesla.talk2u

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GamesScreen() {
    var currentGame by remember { mutableStateOf<String?>(null) }

    if (currentGame == null) {
        // Main Menu
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Nocktie Game Center", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Button(onClick = { currentGame = "FinoTap" }, modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        Text("Fino Tap")
                    }
                }
                // Placeholder for additional games
                item {
                    Button(onClick = { /* future game */ }, modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        Text("Coming Soon")
                    }
                }
            }
        }
    } else {
        // Game Logic
        when (currentGame) {
            "FinoTap" -> FinoTapGame(onBack = { currentGame = null })
            else -> currentGame = null
        }
    }
}

@Composable
fun FinoTapGame(onBack: () -> Unit) {
    var count by remember { mutableStateOf(0) }
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Fino Tap Challenge", style = MaterialTheme.typography.headlineSmall)
        Text("$count", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { count++ }) { Text("TAP ME!") }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBack) { Text("Back to Menu") }
    }
}

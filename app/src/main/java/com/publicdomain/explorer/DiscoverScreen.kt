package com.nyxtesla.talk2u

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PublicBotProfile(val name: String, val handle: String, val interactions: String)

@Composable
fun DiscoverScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val simulatedBots = remember {
        listOf(
            PublicBotProfile("Nyx Tesla AI", "@sentienceflux", "4.8k chats"),
            PublicBotProfile("Sora Matrix", "@soravision", "2.1k chats"),
            PublicBotProfile("XRP Ledger Oracle", "@ripplelink", "9.3k chats"),
            PublicBotProfile("Grandmaster Engine", "@carlsen369", "12.5k chats")
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Text("Discover Public Personas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search system handles...") })

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(simulatedBots.filter { it.name.contains(searchQuery, ignoreCase = true) }) { bot ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F9)), modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color(0xFF6650A4).copy(alpha = 0.1f)) {
                            Box(contentAlignment = Alignment.Center) { Text(bot.name.take(1), color = Color(0xFF6650A4), fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(bot.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(bot.handle, fontSize = 12.sp, color = Color.Gray)
                        Text(bot.interactions, fontSize = 11.sp, color = Color(0xFF6650A4), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

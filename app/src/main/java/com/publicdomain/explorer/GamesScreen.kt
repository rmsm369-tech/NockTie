package com.nyxtesla.talk2u

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GamesScreen() {
    var currentGame by remember { mutableStateOf<String?>(null) }

    AnimatedContent(
        targetState = currentGame,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
        }, label = "GameTransition"
    ) { targetGame ->
        if (targetGame == null) {
            // Main Menu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0A0A))
                    .padding(16.dp)
            ) {
                Text(
                    "Nocktie Arcade",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF4DB8FF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp, top = 32.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { GameMenuCard("Neon Snake", Color(0xFF4CAF50)) { currentGame = "Snake" } }
                    item { GameMenuCard("Shadow Dodge", Color(0xFFFF5252)) { currentGame = "Dodge" } }
                    item { GameMenuCard("Echo Sequence", Color(0xFFE040FB)) { currentGame = "Echo" } }
                    item { GameMenuCard("Memory Matrix", Color(0xFFFFC107)) { currentGame = "Memory" } }
                    item { GameMenuCard("Reflex Strike", Color(0xFF00E5FF)) { currentGame = "Reflex" } }
                    item { GameMenuCard("Tic-Tac-Toe", Color(0xFF8C9EFF)) { currentGame = "TicTacToe" } }
                    
                    // The requested Coming Soon box
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("More Coming Soon", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Game Routing
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
                when (targetGame) {
                    "Snake" -> NeonSnakeGame(onBack = { currentGame = null })
                    "Dodge" -> ShadowDodgeGame(onBack = { currentGame = null })
                    "Echo" -> EchoSequenceGame(onBack = { currentGame = null })
                    "Memory" -> MemoryMatrixGame(onBack = { currentGame = null })
                    "Reflex" -> ReflexStrikeGame(onBack = { currentGame = null })
                    "TicTacToe" -> AnimatedTicTacToeGame(onBack = { currentGame = null })
                    else -> currentGame = null
                }
            }
        }
    }
}

@Composable
fun GameMenuCard(title: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, color.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

// ============================================================================
// 1. NEON SNAKE GAME (Deep 2D Canvas Loop)
// ============================================================================
@Composable
fun NeonSnakeGame(onBack: () -> Unit) {
    val gridSize = 20
    var snake by remember { mutableStateOf(listOf(Pair(10, 10))) }
    var direction by remember { mutableStateOf(Pair(1, 0)) }
    var food by remember { mutableStateOf(Pair(5, 5)) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(120) // Game Speed
            val head = snake.first()
            val nextHead = Pair(head.first + direction.first, head.second + direction.second)

            if (nextHead.first !in 0 until gridSize || nextHead.second !in 0 until gridSize || snake.contains(nextHead)) {
                isGameOver = true
            } else {
                val newSnake = snake.toMutableList()
                newSnake.add(0, nextHead)
                if (nextHead == food) {
                    score += 10
                    food = Pair(Random.nextInt(gridSize), Random.nextInt(gridSize))
                } else {
                    newSnake.removeLast()
                }
                snake = newSnake
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        GameHeader("Neon Snake", score, onBack)
        
        Box(
            modifier = Modifier
                .size(350.dp)
                .background(Color.Black)
                .border(2.dp, Color(0xFF4CAF50))
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / gridSize
                snake.forEachIndexed { index, pos ->
                    val color = if (index == 0) Color(0xFF4DB8FF) else Color(0xFF4CAF50)
                    drawRect(
                        color = color,
                        topLeft = Offset(pos.first * cellSize, pos.second * cellSize),
                        size = Size(cellSize - 2, cellSize - 2)
                    )
                }
                drawCircle(
                    color = Color.Red,
                    radius = cellSize / 2.5f,
                    center = Offset(food.first * cellSize + cellSize / 2, food.second * cellSize + cellSize / 2)
                )
            }
            if (isGameOver) GameOverOverlay(score) {
                snake = listOf(Pair(10, 10)); direction = Pair(1, 0); isGameOver = false; score = 0
            }
        }

        Spacer(Modifier.height(32.dp))
        // D-PAD Controls
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { if (direction != Pair(0, 1)) direction = Pair(0, -1) }) { Text("UP") }
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Button(onClick = { if (direction != Pair(1, 0)) direction = Pair(-1, 0) }) { Text("LEFT") }
                Button(onClick = { if (direction != Pair(-1, 0)) direction = Pair(1, 0) }) { Text("RIGHT") }
            }
            Button(onClick = { if (direction != Pair(0, -1)) direction = Pair(0, 1) }) { Text("DOWN") }
        }
    }
}

// ============================================================================
// 2. SHADOW DODGE (Continuous Animation & Drag Gesture)
// ============================================================================
@Composable
fun ShadowDodgeGame(onBack: () -> Unit) {
    var playerX by remember { mutableStateOf(500f) }
    var enemies by remember { mutableStateOf(listOf<Pair<Float, Float>>()) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    LaunchedEffect(isGameOver) {
        var tick = 0
        while (!isGameOver) {
            delay(30)
            tick++
            if (tick % 10 == 0) {
                score++
                enemies = enemies + Pair(Random.nextFloat() * 1000f, 0f)
            }
            enemies = enemies.map { it.copy(second = it.second + 25f) }.filter { it.second < 2000f }
            
            enemies.forEach { enemy ->
                if (enemy.second > 1600f && abs(enemy.first - playerX) < 100f) {
                    isGameOver = true
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        GameHeader("Shadow Dodge", score, onBack)
        Text("Drag the blue block horizontally to survive!", color = Color.Gray)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .background(Color(0xFF121212))
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        playerX = (playerX + dragAmount.x).coerceIn(0f, size.width.toFloat())
                    }
                }
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val effectiveX = (playerX / 1000f) * size.width
                
                // Player
                drawRoundRect(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(effectiveX - 40f, size.height - 100f),
                    size = Size(80f, 80f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )
                
                // Enemies
                enemies.forEach { enemy ->
                    val ex = (enemy.first / 1000f) * size.width
                    val ey = (enemy.second / 2000f) * size.height
                    drawCircle(color = Color(0xFFFF5252), radius = 30f, center = Offset(ex, ey))
                }
            }
            if (isGameOver) GameOverOverlay(score) {
                enemies = emptyList(); playerX = 500f; isGameOver = false; score = 0
            }
        }
    }
}

// ============================================================================
// 3. ECHO SEQUENCE (Simon Says with Coroutines)
// ============================================================================
@Composable
fun EchoSequenceGame(onBack: () -> Unit) {
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var playerIndex by remember { mutableStateOf(0) }
    var activeFlasher by remember { mutableStateOf<Int?>(null) }
    var isPlayerTurn by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    
    val colors = listOf(Color(0xFFFF5252), Color(0xFF4CAF50), Color(0xFF4DB8FF), Color(0xFFFFC107))

    fun nextRound() {
        sequence = sequence + Random.nextInt(4)
        playerIndex = 0
        isPlayerTurn = false
    }

    LaunchedEffect(sequence) {
        if (sequence.isNotEmpty() && !isGameOver) {
            delay(1000)
            for (colorIdx in sequence) {
                activeFlasher = colorIdx
                delay(400)
                activeFlasher = null
                delay(200)
            }
            isPlayerTurn = true
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        GameHeader("Echo Sequence", sequence.size.takeIf { it > 0 }?.minus(1) ?: 0, onBack)
        
        Spacer(Modifier.height(32.dp))
        Text(if (isPlayerTurn) "YOUR TURN" else "WATCH CLOSELY", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        if (!isGameOver && sequence.isEmpty()) {
            Button(onClick = { nextRound() }) { Text("Start Game") }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EchoButton(0, colors[0], activeFlasher == 0) { 
                        if (isPlayerTurn) handleEchoTap(0, sequence, playerIndex, { nextRound() }, { playerIndex++ }, { isGameOver = true }) 
                    }
                    EchoButton(1, colors[1], activeFlasher == 1) { 
                        if (isPlayerTurn) handleEchoTap(1, sequence, playerIndex, { nextRound() }, { playerIndex++ }, { isGameOver = true }) 
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EchoButton(2, colors[2], activeFlasher == 2) { 
                        if (isPlayerTurn) handleEchoTap(2, sequence, playerIndex, { nextRound() }, { playerIndex++ }, { isGameOver = true }) 
                    }
                    EchoButton(3, colors[3], activeFlasher == 3) { 
                        if (isPlayerTurn) handleEchoTap(3, sequence, playerIndex, { nextRound() }, { playerIndex++ }, { isGameOver = true }) 
                    }
                }
            }
        }
        
        if (isGameOver) {
            Spacer(Modifier.height(32.dp))
            GameOverOverlay(sequence.size - 1) {
                sequence = emptyList(); isGameOver = false
            }
        }
    }
}

fun handleEchoTap(idx: Int, seq: List<Int>, pIdx: Int, onWin: () -> Unit, onNext: () -> Unit, onFail: () -> Unit) {
    if (seq[pIdx] == idx) {
        if (pIdx == seq.size - 1) onWin() else onNext()
    } else onFail()
}

@Composable
fun EchoButton(index: Int, baseColor: Color, isFlashing: Boolean, onClick: () -> Unit) {
    val animatedColor by animateColorAsState(if (isFlashing) Color.White else baseColor.copy(alpha = 0.5f), tween(200))
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(animatedColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    )
}

// ============================================================================
// 4. MEMORY MATRIX (3D Flip Animations)
// ============================================================================
@Composable
fun MemoryMatrixGame(onBack: () -> Unit) {
    val emojis = listOf("🧠", "👁️", "🌑", "⚡", "🔥", "🧊", "🌪️", "✨")
    var cards by remember { mutableStateOf((emojis + emojis).shuffled()) }
    var flippedIndices by remember { mutableStateOf(setOf<Int>()) }
    var matchedIndices by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(flippedIndices) {
        if (flippedIndices.size == 2) {
            delay(800)
            val list = flippedIndices.toList()
            if (cards[list[0]] == cards[list[1]]) {
                matchedIndices = matchedIndices + flippedIndices
            }
            flippedIndices = emptySet()
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        GameHeader("Memory Matrix", matchedIndices.size / 2, onBack)
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.padding(16.dp).height(400.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards.size) { index ->
                val isFlipped = flippedIndices.contains(index) || matchedIndices.contains(index)
                val rotation by animateFloatAsState(targetValue = if (isFlipped) 180f else 0f, tween(400))
                
                Card(
                    modifier = Modifier
                        .height(80.dp)
                        .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
                        .clickable(enabled = !isFlipped && flippedIndices.size < 2) {
                            flippedIndices = flippedIndices + index
                        },
                    colors = CardDefaults.cardColors(containerColor = if (isFlipped) Color(0xFF1E1E1E) else Color(0xFF4DB8FF))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (rotation > 90f) {
                            Text(cards[index], fontSize = 32.sp, modifier = Modifier.graphicsLayer { rotationY = 180f })
                        }
                    }
                }
            }
        }
        
        if (matchedIndices.size == cards.size) {
            Text("MATRIX SOLVED!", color = Color(0xFF4CAF50), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { cards = (emojis + emojis).shuffled(); matchedIndices = emptySet(); flippedIndices = emptySet() }) {
                Text("Re-encrypt Matrix")
            }
        }
    }
}

// ============================================================================
// 5. REFLEX STRIKE (Shrinking Targets & Timing)
// ============================================================================
@Composable
fun ReflexStrikeGame(onBack: () -> Unit) {
    var target by remember { mutableStateOf(Pair(0.5f, 0.5f)) }
    var score by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(30) }
    var isGameOver by remember { mutableStateOf(false) }

    LaunchedEffect(isGameOver) {
        while (timeRemaining > 0 && !isGameOver) {
            delay(1000)
            timeRemaining--
        }
        if (timeRemaining == 0) isGameOver = true
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        GameHeader("Reflex Strike", score, onBack)
        Text("Time: ${timeRemaining}s", color = if (timeRemaining < 10) Color.Red else Color.White, fontSize = 24.sp)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A1A))
                .clickable { if (!isGameOver) score = maxOf(0, score - 1) } // Miss penalty
        ) {
            if (!isGameOver) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (target.first * 300).dp,
                            y = (target.second * 400).dp
                        )
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF))
                        .clickable {
                            score += 5
                            target = Pair(Random.nextFloat(), Random.nextFloat())
                        }
                )
            } else {
                GameOverOverlay(score) {
                    score = 0; timeRemaining = 30; isGameOver = false
                }
            }
        }
    }
}

// ============================================================================
// 6. ANIMATED TIC-TAC-TOE (Juicy UI Upgrades)
// ============================================================================
@Composable
fun AnimatedTicTacToeGame(onBack: () -> Unit) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var isPlayerXTurn by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf<String?>(null) }

    val checkWinner = { newBoard: List<String> ->
        val winLines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        winLines.find { (a, b, c) ->
            newBoard[a].isNotEmpty() && newBoard[a] == newBoard[b] && newBoard[a] == newBoard[c]
        }?.let { newBoard[it[0]] } ?: if (!newBoard.contains("")) "Draw" else null
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        GameHeader("Tic-Tac-Toe", 0, onBack)
        
        Text(
            text = winner?.let { if (it == "Draw") "It's a Draw!" else "$it WINS!" } ?: "Turn: ${if (isPlayerXTurn) "X" else "O"}",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))

        Column {
            for (i in 0..2) {
                Row {
                    for (j in 0..2) {
                        val index = i * 3 + j
                        val cellValue = board[index]
                        val scale by animateFloatAsState(if (cellValue.isEmpty()) 0f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E1E))
                                .clickable(enabled = cellValue.isEmpty() && winner == null) {
                                    val newBoard = board.toMutableList()
                                    newBoard[index] = if (isPlayerXTurn) "X" else "O"
                                    board = newBoard
                                    isPlayerXTurn = !isPlayerXTurn
                                    winner = checkWinner(newBoard)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellValue.isNotEmpty()) {
                                Text(
                                    text = cellValue,
                                    fontSize = 48.sp,
                                    color = if (cellValue == "X") Color(0xFFFF5252) else Color(0xFF4DB8FF),
                                    modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { board = List(9) { "" }; winner = null; isPlayerXTurn = true }) { 
            Text("Restart Match") 
        }
    }
}

// ============================================================================
// SHARED UI COMPONENTS
// ============================================================================
@Composable
fun GameHeader(title: String, score: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) { Text("< Back", color = Color.Gray) }
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Pts: $score", color = Color(0xFF4DB8FF), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GameOverOverlay(score: Int, onRestart: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GAME OVER", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Final Score: $score", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(16.dp))
            Button(onClick = onRestart) { Text("Play Again") }
        }
    }
}
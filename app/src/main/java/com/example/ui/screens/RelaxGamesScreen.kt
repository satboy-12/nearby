package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HealingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RelaxGamesScreen(
    viewModel: HealingViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Zen Sand Garden, 1: Aura Balloon Pop
    val userProfile by viewModel.userProfile.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF7F6F0) // Natural Canvas Warm Beige
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant Visual Header (matching Natural Vibe design theme)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "ZEN NATURE ROOM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mind relaxing games",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    // Simple score/points indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Spark points: ${userProfile?.ratingScore ?: 0}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Elegant Pill Selector for Games
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEFEFEA))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeTab == 0) Color.White else Color.Transparent)
                        .clickable { activeTab = 0 }
                        .padding(vertical = 10.dp)
                        .testTag("garden_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪵", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Zen Sand Garden",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 0) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeTab == 1) Color.White else Color.Transparent)
                        .clickable { activeTab = 1 }
                        .padding(vertical = 10.dp)
                        .testTag("aura_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎈", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aura Balloon Pop",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 1) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }

            // Gameroom Content Router
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                    },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> ZenSandGardenScreen(viewModel = viewModel)
                        1 -> AuraPopScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// GAME 1: ZEN SAND GARDEN IMPLEMENTATION
// -----------------------------------------------------------------

data class SandRakePoint(val position: Offset, val strokeWidth: Float)
data class ZenStone(val position: Offset, val symbol: String, val size: Float, val color: Color)
data class SandRipple(val center: Offset, val radius: Float, val maxRadius: Float, val alpha: Float)

@Composable
fun ZenSandGardenScreen(
    viewModel: HealingViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Garden State
    val rakedLines = remember { mutableStateListOf<List<SandRakePoint>>() }
    var currentLine = remember { mutableStateOf<List<SandRakePoint>>(emptyList()) }
    val placedStones = remember { mutableStateListOf<ZenStone>() }
    val ripples = remember { mutableStateListOf<SandRipple>() }

    // Rake settings
    var selectedStoneEmoji by remember { mutableStateOf("🪨") }
    var clearAnimationPercent by remember { mutableStateOf(1f) }

    val stoneEmojis = listOf("🪨", "💎", "☘️", "🐚", "💮")

    // Dynamic wave ripples updates
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L) // ~60fps wave ticks
            val iterator = ripples.listIterator()
            while (iterator.hasNext()) {
                val rip = iterator.next()
                if (rip.radius >= rip.maxRadius) {
                    iterator.remove()
                } else {
                    iterator.set(
                        rip.copy(
                            radius = rip.radius + 1.8f,
                            alpha = (1f - (rip.radius / rip.maxRadius)).coerceIn(0.0f, 1f)
                        )
                    )
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("zen_sand_sandbox"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF1F0EA))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFEBE5DF), Color(0xFFDFD7CD)) // Realistic Sand Warm Gradients
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { tapOffset ->
                                // Drop stone
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                placedStones.add(
                                    ZenStone(
                                        position = tapOffset,
                                        symbol = selectedStoneEmoji,
                                        size = Random.nextFloat() * 12f + 30f,
                                        color = Color.White
                                    )
                                )
                                // Spark concentric dynamic ripples
                                ripples.add(
                                    SandRipple(
                                        center = tapOffset,
                                        radius = 2.0f,
                                        maxRadius = 140f,
                                        alpha = 0.9f
                                    )
                                )
                                ripples.add(
                                    SandRipple(
                                        center = tapOffset,
                                        radius = 40.0f,
                                        maxRadius = 180f,
                                        alpha = 0.7f
                                    )
                                )
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                currentLine.value = listOf(SandRakePoint(startOffset, 8f))
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val currentPoints = currentLine.value.toMutableList()
                                if (currentPoints.isNotEmpty()) {
                                    val lastPoint = currentPoints.last().position
                                    val nextPosition = lastPoint + dragAmount
                                    currentPoints.add(SandRakePoint(nextPosition, 8f))
                                    currentLine.value = currentPoints
                                    
                                    // Gentle tick feedback as they rake
                                    if (currentPoints.size % 4 == 0) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                            },
                            onDragEnd = {
                                rakedLines.add(currentLine.value)
                                currentLine.value = emptyList()
                            }
                        )
                    }
            ) {
                // Interactive drawing Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleAlpha = clearAnimationPercent

                    // 1. Draw older permanent sand rake waves
                    rakedLines.forEach { pointsList ->
                        if (pointsList.size > 1) {
                            val path = Path()
                            path.moveTo(pointsList[0].position.x, pointsList[0].position.y)
                            for (i in 1 until pointsList.size) {
                                path.lineTo(pointsList[i].position.x, pointsList[i].position.y)
                            }
                            // Deep sand trail shadows & light highlight to look engraved
                            drawPath(
                                path = path,
                                color = Color(0xFFC0B4A6).copy(alpha = 0.6f * scaleAlpha),
                                style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            drawPath(
                                path = path,
                                color = Color.White.copy(alpha = 0.4f * scaleAlpha),
                                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    // 2. Draw current active line
                    val activePoints = currentLine.value
                    if (activePoints.size > 1) {
                        val path = Path()
                        path.moveTo(activePoints[0].position.x, activePoints[0].position.y)
                        for (i in 1 until activePoints.size) {
                            path.lineTo(activePoints[i].position.x, activePoints[i].position.y)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFC0B4A6).copy(alpha = 0.7f * scaleAlpha),
                            style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.45f * scaleAlpha),
                            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // 3. Draw sand ripple currents propagating outward
                    ripples.forEach { rip ->
                        drawCircle(
                            color = Color(0xFFBCAE9C).copy(alpha = rip.alpha * scaleAlpha),
                            radius = rip.radius,
                            center = rip.center,
                            style = Stroke(width = 4f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = (rip.alpha * 0.4f) * scaleAlpha),
                            radius = rip.radius + 4f,
                            center = rip.center,
                            style = Stroke(width = 2f)
                        )
                    }

                    // 4. Trace light zen patterns guide circles
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f * scaleAlpha),
                        radius = size.width * 0.25f,
                        center = Offset(size.width / 2, size.height / 2),
                        style = Stroke(width = 2f)
                    )
                }

                // Emojis (Zen Stones) layered on top of drawing at coordinates
                placedStones.forEach { stone ->
                    // Adjust placement offset center perfectly
                    val sizePx = with(LocalDensity.current) { stone.size.dp.toPx() }
                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(LocalDensity.current) { (stone.position.x).toDp() } - (stone.size.dp / 2),
                                y = with(LocalDensity.current) { (stone.position.y).toDp() } - (stone.size.dp / 2)
                            )
                            .size(stone.size.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stone.symbol,
                            fontSize = (stone.size * 0.75f).sp
                        )
                    }
                }

                // Dynamic Sandbox Guide Overlay
                if (rakedLines.isEmpty() && currentLine.value.isEmpty() && placedStones.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gesture,
                            contentDescription = "rake guide",
                            tint = Color(0xFF8B7F72).copy(alpha = 0.46f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Rake patterns on the cozy sand canvas.\nTap anywhere to place calming garden stones.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8B7F72).copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tool controls under sandbox garden
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF1F0EA))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stone Selector
                Column {
                    Text(
                        "GARDEN STONES",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stoneEmojis.forEach { emoji ->
                            val isSelected = selectedStoneEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFFF5F5F0))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedStoneEmoji = emoji }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                // Action Controls
                Button(
                    onClick = {
                        // Smoothly clear with simple flash/fade
                        coroutineScope.launch {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Fade out
                            val anim = Animatable(1f)
                            anim.animateTo(0f, animationSpec = tween(350)) {
                                clearAnimationPercent = value
                            }
                            rakedLines.clear()
                            placedStones.clear()
                            ripples.clear()
                            clearAnimationPercent = 1f
                            
                            // Increase alignment rating points for mind relaxation
                            val currentLiveProfile = viewModel.userProfile.value ?: com.example.data.model.UserProfile(id = 1)
                            viewModel.updateProfile(currentLiveProfile.copy(ratingScore = currentLiveProfile.ratingScore + 1))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC7B198)),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Waves, contentDescription = "rake", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Smoothen", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// -----------------------------------------------------------------
// GAME 2: AURA BALLOON POP IMPLEMENTATION
// -----------------------------------------------------------------

data class SparklingParticle(
    val x: Float,
    val y: Float,
    val vectorX: Float,
    val vectorY: Float,
    val color: Color,
    val alpha: Float,
    val size: Float
)

data class GlowingAuraBalloon(
    val id: Int,
    val relativeX: Float, // 0.1 to 0.9 screen margin
    val relativeY: Float, // 0.1 to 0.9 screen margin
    val color: Color,
    val sizeSp: Float,
    val breathScaleOffset: Float, // speed deviation
    val thoughtMessage: String
)

@Composable
fun AuraPopScreen(
    viewModel: HealingViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Set of quotes for tranquil minds
    val soulSparks = listOf(
        "I am safe right now.",
        "Peace flows inside my lungs.",
        "I release expectation.",
        "I am grounded like a massive mountain.",
        "Today, I choose kindness.",
        "This breath is fully enough.",
        "Softly breathing, soft in heart.",
        "My thoughts swim clear and calm.",
        "No hurry. No delay.",
        "My emotional anchor holds steady."
    )

    val softColours = listOf(
        Color(0xFFA3D5C3), // Seafoam
        Color(0xFFE2C2C6), // Pale Rose
        Color(0xFFC5BADA), // Soft Violet
        Color(0xFFF3D299), // Sunray Gold
        Color(0xFFABD1E5)  // Pale Sky Blue
    )

    // Game States
    var flowScore by remember { mutableStateOf(0) }
    var revealedAffirmation by remember { mutableStateOf("Touch a floating aura sphere to release mental tension...") }
    val floatingBalloons = remember { mutableStateListOf<GlowingAuraBalloon>() }
    val activeParticles = remember { mutableStateListOf<SparklingParticle>() }

    // Breathing pulse timer sync
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseFraction by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing), // 4 seconds grow and shrink
            repeatMode = RepeatMode.Reverse
        ),
        label = "radial"
    )

    // Spawn 5 balloon spheres to begin
    LaunchedEffect(Unit) {
        if (floatingBalloons.isEmpty()) {
            for (i in 0 until 5) {
                floatingBalloons.add(
                    GlowingAuraBalloon(
                        id = i,
                        relativeX = (0.15f + Random.nextFloat() * 0.7f),
                        relativeY = (0.2f + Random.nextFloat() * 0.5f),
                        color = softColours[i % softColours.size],
                        sizeSp = (65f + Random.nextFloat() * 30f),
                        breathScaleOffset = (0.1f + Random.nextFloat() * 0.2f),
                        thoughtMessage = soulSparks[Random.nextInt(soulSparks.size)]
                    )
                )
            }
        }
    }

    // Dynamic floating translation animations & particles updating
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L) // Tick update loop

            // Translate particles
            val partIterator = activeParticles.listIterator()
            while (partIterator.hasNext()) {
                val p = partIterator.next()
                if (p.alpha <= 0.05f) {
                    partIterator.remove()
                } else {
                    partIterator.set(
                        p.copy(
                            x = p.x + p.vectorX,
                            y = p.y + p.vectorY,
                            vectorY = p.vectorY + 0.15f, // soft gravity pull down
                            alpha = p.alpha - 0.024f
                        )
                    )
                }
            }

            // Slowly drift aura balloon coords to look floating
            for (j in 0 until floatingBalloons.size) {
                val bal = floatingBalloons[j]
                // Dynamic circular rotation offset trajectory
                val driftX = bal.relativeX + (sin((System.currentTimeMillis() / 2000.0) + bal.id) * 0.0007f).toFloat()
                val driftY = bal.relativeY + (sin((System.currentTimeMillis() / 2800.0) + bal.id * 2) * 0.0007f).toFloat()
                floatingBalloons[j] = bal.copy(
                    relativeX = driftX.coerceIn(0.08f, 0.92f),
                    relativeY = driftY.coerceIn(0.12f, 0.88f)
                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("aura_pop_sandbox"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF1F0EA))
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFCFCFB), Color(0xFFF3F2EC)),
                            center = Offset(200f, 300f)
                        )
                    )
            ) {
                val boxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                val boxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

                // 1. Particle Canvas layer to draw popping sparks
                Canvas(modifier = Modifier.fillMaxSize()) {
                    activeParticles.forEach { p ->
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha),
                            radius = p.size,
                            center = Offset(p.x, p.y)
                        )
                    }
                }

                // 2. Interactive Glowing Floating Balloons
                floatingBalloons.forEach { bal ->
                    // Calculate precise location based on responsive viewport
                    val xPos = boxWidthPx * bal.relativeX
                    val yPos = boxHeightPx * bal.relativeY

                    // Combine global rhythmic pulse factor with scale offsets
                    val currentPulseFactor = 1f + ((pulseFraction - 1f) * bal.breathScaleOffset)
                    val diameterDp = bal.sizeSp.dp * currentPulseFactor

                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(LocalDensity.current) { xPos.toDp() } - (diameterDp / 2),
                                y = with(LocalDensity.current) { yPos.toDp() } - (diameterDp / 2)
                            )
                            .size(diameterDp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(bal.color.copy(alpha = 0.9f), bal.color.copy(alpha = 0.2f))
                                )
                            )
                            .border(BorderStroke(1.dp, Color.White), CircleShape)
                            .clickable {
                                // POP ACTION
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                revealedAffirmation = bal.thoughtMessage
                                flowScore += 1

                                // Spawn explosion spark particles at coordinate
                                for (i in 0 until 18) {
                                    val angleRad = (i * (360f / 18)) * (3.1415f / 180f)
                                    val impulseSpeed = 5f + Random.nextFloat() * 6f
                                    activeParticles.add(
                                        SparklingParticle(
                                            x = xPos,
                                            y = yPos,
                                            vectorX = (cos(angleRad.toDouble()) * impulseSpeed).toFloat(),
                                            vectorY = (sin(angleRad.toDouble()) * impulseSpeed).toFloat(),
                                            color = bal.color,
                                            alpha = 1.0f,
                                            size = 4f + Random.nextFloat() * 6f
                                        )
                                    )
                                }

                                // Replace with a new floating space candidate
                                val index = floatingBalloons.indexOf(bal)
                                if (index != -1) {
                                    floatingBalloons[index] = GlowingAuraBalloon(
                                        id = bal.id + 10,
                                        relativeX = (0.15f + Random.nextFloat() * 0.7f),
                                        relativeY = (0.2f + Random.nextFloat() * 0.5f),
                                        color = softColours[Random.nextInt(softColours.size)],
                                        sizeSp = (65f + Random.nextFloat() * 30f),
                                        breathScaleOffset = (0.1f + Random.nextFloat() * 0.2f),
                                        thoughtMessage = soulSparks[Random.nextInt(soulSparks.size)]
                                    )
                                }

                                // Reward points on profile view model triggers
                                val currentLiveProfile = viewModel.userProfile.value ?: com.example.data.model.UserProfile(id = 1)
                                viewModel.updateProfile(currentLiveProfile.copy(ratingScore = currentLiveProfile.ratingScore + 1))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "✨",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "breathe",
                                fontSize = 8.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Thought Display Box showing latest popped content
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "LATEST MIND REFLECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = revealedAffirmation,
                    transitionSpec = {
                        slideInVertically { h -> h } + fadeIn() togetherWith
                                slideOutVertically { h -> -h } + fadeOut()
                    },
                    label = "thought_cycle"
                ) { advice ->
                    Text(
                        text = "\"$advice\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (pulseFraction > 1.1f) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (pulseFraction > 1.1f) "EXHALE GENTLY" else "INHALE CLARITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

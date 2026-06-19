package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HealingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun BreatheScreen(viewModel: HealingViewModel) {
    var activeTechnique by remember { mutableStateOf(0) } // 0 = 4-7-8, 1 = Box Breathing, 2 = 5-4-3-2-1 Grounding, 3 = Muscle Relaxation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = activeTechnique,
            edgePadding = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = activeTechnique == 0, onClick = { activeTechnique = 0 }, text = { Text("4-7-8 Calm") }, modifier = Modifier.testTag("tab_478"))
            Tab(selected = activeTechnique == 1, onClick = { activeTechnique = 1 }, text = { Text("Box Breathe") }, modifier = Modifier.testTag("tab_box"))
            Tab(selected = activeTechnique == 2, onClick = { activeTechnique = 2 }, text = { Text("5-4-3-2-1") }, modifier = Modifier.testTag("tab_grounding"))
            Tab(selected = activeTechnique == 3, onClick = { activeTechnique = 3 }, text = { Text("Relaxation") }, modifier = Modifier.testTag("tab_muscle"))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTechnique) {
                0 -> Breathe478Card(viewModel)
                1 -> BoxBreathingCard(viewModel)
                2 -> SenseGroundingCard(viewModel)
                3 -> ProgressiveRelaxationCard()
            }
        }
    }
}

@Composable
fun Breathe478Card(viewModel: HealingViewModel) {
    val scope = rememberCoroutineScope()
    var isRunning by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("Ready") } // Inhale, Hold, Exhale
    var countdownValue by remember { mutableStateOf(4) }
    var cycleCounter by remember { mutableStateOf(0) }

    // Pulse diameter scale animator
    val infiniteTransition = rememberInfiniteTransition()
    val pulsatedMultiplier by animateFloatAsState(
        targetValue = when (currentPhase) {
            "Breathe In" -> 2.2f
            "Hold Breath" -> 2.2f
            "Exhale Softly" -> 1.0f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )

    LaunchedEffect(key1 = isRunning, key2 = currentPhase) {
        if (!isRunning) return@LaunchedEffect

        when (currentPhase) {
            "Ready" -> {
                currentPhase = "Breathe In"
                countdownValue = 4
            }
            "Breathe In" -> {
                while (countdownValue > 1) {
                    delay(1000)
                    countdownValue--
                }
                currentPhase = "Hold Breath"
                countdownValue = 7
            }
            "Hold Breath" -> {
                while (countdownValue > 1) {
                    delay(1000)
                    countdownValue--
                }
                currentPhase = "Exhale Softly"
                countdownValue = 8
            }
            "Exhale Softly" -> {
                while (countdownValue > 1) {
                    delay(1000)
                    countdownValue--
                }
                cycleCounter++
                if (cycleCounter >= 4) {
                    isRunning = false
                    currentPhase = "Complete!"
                    viewModel.logBreathingExercise("4-7-8 Breathing", 19 * 4, true)
                } else {
                    currentPhase = "Breathe In"
                    countdownValue = 4
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
            Text(
                "4-7-8 Healing Breath",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Inhale 4s ➜ Hold 7s ➜ Exhale 8s. Complete 4 complete rounds.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Animated Breathing circle sphere
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Echo rings
            Box(
                modifier = Modifier
                    .size((100 * pulsatedMultiplier).dp)
                    .clip(CircleShape)
                    .background(
                        when (currentPhase) {
                            "Breathe In" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            "Hold Breath" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                            else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                        }
                    )
            )

            // Core card sphere
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRunning) countdownValue.toString() else "🌬️",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = currentPhase.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text("Rounds Completed: $cycleCounter of 4", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        if (isRunning) {
                            isRunning = false
                            currentPhase = "Paused"
                        } else {
                            if (currentPhase == "Complete!" || currentPhase == "Ready") {
                                cycleCounter = 0
                            }
                            isRunning = true
                            currentPhase = "Ready"
                        }
                    },
                    modifier = Modifier.testTag("btn_478_toggle")
                ) {
                    Icon(if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isRunning) "Pause" else "Start Calm Cycle")
                }

                OutlinedButton(
                    onClick = {
                        isRunning = false
                        currentPhase = "Ready"
                        countdownValue = 4
                        cycleCounter = 0
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun BoxBreathingCard(viewModel: HealingViewModel) {
    var isRunning by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("Ready") } // Inhale, Hold, Exhale, Hold
    var counter by remember { mutableStateOf(4) }
    var roundsCount by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = isRunning, key2 = currentPhase) {
        if (!isRunning) return@LaunchedEffect

        when (currentPhase) {
            "Ready" -> {
                currentPhase = "Breathe In"
                counter = 4
            }
            "Breathe In" -> {
                while (counter > 1) {
                    delay(1000)
                    counter--
                }
                currentPhase = "Hold In"
                counter = 4
            }
            "Hold In" -> {
                while (counter > 1) {
                    delay(1000)
                    counter--
                }
                currentPhase = "Exhale"
                counter = 4
            }
            "Exhale" -> {
                while (counter > 1) {
                    delay(1000)
                    counter--
                }
                currentPhase = "Hold Out"
                counter = 4
            }
            "Hold Out" -> {
                while (counter > 1) {
                    delay(1000)
                    counter--
                }
                roundsCount++
                if (roundsCount >= 4) {
                    isRunning = false
                    currentPhase = "Finished!"
                    viewModel.logBreathingExercise("Box Breathing", 16 * 4, true)
                } else {
                    currentPhase = "Breathe In"
                    counter = 4
                }
            }
        }
    }

    val boxPacingSize by animateDpAsState(
        targetValue = when (currentPhase) {
            "Breathe In" -> 160.dp
            "Hold In" -> 160.dp
            "Exhale" -> 80.dp
            else -> 80.dp
        },
        animationSpec = tween(4000, easing = LinearEasing)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
            Text(
                "Box Breathing Pacer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                "4s Inhale ➜ 4s Hold In ➜ 4s Exhale ➜ 4s Hold Out",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        // Square animated canvas pacing
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(boxPacingSize)
                    .border(4.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = counter.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = currentPhase.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary
            )

            Text("Box Rounds: $roundsCount of 4", fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (isRunning) {
                            isRunning = false
                            currentPhase = "Paused"
                        } else {
                            if (currentPhase == "Finished!" || currentPhase == "Ready") {
                                roundsCount = 0
                            }
                            isRunning = true
                            currentPhase = "Ready"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.testTag("btn_box_toggle")
                ) {
                    Icon(if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isRunning) "Pause" else "Start Box Round")
                }

                OutlinedButton(
                    onClick = {
                        isRunning = false
                        currentPhase = "Ready"
                        counter = 4
                        roundsCount = 0
                    }
                ) {
                    Text("Reset")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SenseGroundingCard(viewModel: HealingViewModel) {
    val items5 = remember { mutableStateListOf("", "", "", "", "") }
    val items4 = remember { mutableStateListOf("", "", "", "") }
    val items3 = remember { mutableStateListOf("", "", "") }
    val items2 = remember { mutableStateListOf("", "") }
    val items1 = remember { mutableStateOf("") }

    var logStatusSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "5-4-3-2-1 Sensory Grounding",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "A therapeutic grounding practice that overrides trauma spikes, fear, and impulsive triggers by rooting your mind in the active physical present.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Section 5
        GroundingInputsSection(label = "5 things you can SEE", list = items5, placeholder = "e.g. green leaf, clock, chair")

        // Section 4
        GroundingInputsSection(label = "4 things you can TOUCH/FEEL", list = items4, placeholder = "e.g. denim pants, warm breeze, phone surface")

        // Section 3
        GroundingInputsSection(label = "3 things you can HEAR", list = items3, placeholder = "e.g. bird chirp, fan buzz, cars passing")

        // Section 2
        GroundingInputsSection(label = "2 things you can SMILE/SMELL", list = items2, placeholder = "e.g. brewed coffee, pine, fresh laundry")

        // Section 1
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("1 thing you can TASTE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = items1.value,
                onValueChange = { items1.value = it },
                placeholder = { Text("e.g. peppermint toothpaste, water freshness") },
                maxLines = 1,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .testTag("grounding_input_taste")
            )
        }

        if (logStatusSuccess) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Excellent work! Your mind is grounded and safe here in the present. This urge has zero power over you.",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Button(
            onClick = {
                val completeCount = items5.count { it.isNotBlank() } +
                        items4.count { it.isNotBlank() } +
                        items3.count { it.isNotBlank() } +
                        items2.count { it.isNotBlank() } +
                        (if (items1.value.isNotBlank()) 1 else 0)
                
                if (completeCount >= 5) {
                    viewModel.logBreathingExercise("5-4-3-2-1 Grounding", 120, true)
                    logStatusSuccess = true
                    // Reset inputs
                    for (i in 0..4) { if (i < items5.size) items5[i] = "" }
                    for (i in 0..3) { if (i < items4.size) items4[i] = "" }
                    for (i in 0..2) { if (i < items3.size) items3[i] = "" }
                    for (i in 0..1) { if (i < items2.size) items2[i] = "" }
                    items1.value = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("btn_grounding_save")
        ) {
            Text("Verify Grounding Answers")
        }
    }
}

@Composable
fun GroundingInputsSection(
    label: String,
    list: List<String>,
    placeholder: String
) {
    val mutableList = list as MutableList<String>
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        list.forEachIndexed { idx, it ->
            OutlinedTextField(
                value = it,
                onValueChange = { mutableList[idx] = it },
                placeholder = { Text("$placeholder (${idx + 1})") },
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
fun ProgressiveRelaxationCard() {
    val items = listOf(
        "Feet & Toes" to "Squeeze your toes tightly into the ground for 6 seconds, then release. Feel the heavy warmth of relaxation flowing through them.",
        "Calves & Legs" to "Tighten your lower calves as if pushing up onto tip-toes, hold for 6 seconds, then exhale and release all tension.",
        "Abdomen & Core" to "Brace your abdominal muscles strongly, holding the tightness for 6 seconds, then release completely.",
        "Chest & Shoulders" to "Pull your shoulders high up to your ears, squeeze them for 6 seconds, then drop them heavily on a deep exhalation.",
        "Arms & Fists" to "Clench both hands into strong fists while tightening your biceps, squeeze for 6 seconds, then release.",
        "Neck & Face" to "Squeeze your eyelids and purse your lips together, squeezing your neck muscles for 6 seconds, then completely let go."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Progressive Muscle Relaxation",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Progressively tensing and heavily releasing major muscle groups relieves deep-seated somatic stress blocks instantly.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        items.forEachIndexed { index, (group, directive) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(group, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(directive, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

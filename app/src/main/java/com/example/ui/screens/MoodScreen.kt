package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MoodLog
import com.example.ui.viewmodel.HealingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodScreen(viewModel: HealingViewModel) {
    val moods by viewModel.allMoodLogs.collectAsState()
    
    var activeTab by remember { mutableStateOf(0) } // 0 = Log Mood, 1 = History & Trends

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Log Mood", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("log_mood_tab")
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Trends & Logs", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("trends_tab")
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeTab == 0) {
                LogMoodForm(onSave = { scale, emotions, trigger, symptoms, location, note ->
                    viewModel.logMood(scale, emotions, trigger, symptoms, location, note)
                })
            } else {
                MoodTrendsAndHistory(
                    moodList = moods,
                    onDelete = { id -> viewModel.deleteMoodLog(id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogMoodForm(
    onSave: (Int, List<String>, String, List<String>, String, String) -> Unit
) {
    var scale by remember { mutableStateOf(5) }
    val selectedEmotions = remember { mutableStateListOf<String>() }
    var triggerText by remember { mutableStateOf("") }
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var seedLocation by remember { mutableStateOf("Home") }
    var noteText by remember { mutableStateOf("") }
    
    var showSuccessSnack by remember { mutableStateOf(false) }

    val emojis = listOf("😢", "😔", "😐", "🙂", "😊", "✨", "🌺", "🧠", "🌱", "🧘")
    val emotionsList = listOf("anxious", "sad", "hopeful", "peaceful", "angry", "lonely", "numb", "vulnerable", "guilty", "strong")
    val physicalSymptoms = listOf("tight chest", "fatigue", "headache", "restlessness", "insomnia", "nausea")
    val locations = listOf("Home", "Work", "Public Space", "Nature", "Therapy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Slider scale card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = emojis[scale.coerceIn(1, 10) - 1],
                    fontSize = 72.sp,
                    modifier = Modifier.animateContentSize()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mood Intensity: $scale / 10",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = scale.toFloat(),
                    onValueChange = { scale = it.toInt().coerceIn(1, 10) },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.testTag("mood_form_scale")
                )
            }
        }

        // Emotions collection
        Column {
            Text("Emotions felt:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emotionsList.forEach { emotion ->
                    val isSelected = selectedEmotions.contains(emotion)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedEmotions.remove(emotion) else selectedEmotions.add(emotion)
                        },
                        label = { Text(emotion) },
                        modifier = Modifier.testTag("form_emotion_$emotion")
                    )
                }
            }
        }

        // Trigger input
        OutlinedTextField(
            value = triggerText,
            onValueChange = { triggerText = it },
            label = { Text("Trigger identification") },
            placeholder = { Text("e.g. Evening loneliness, social media post, memory") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mood_form_trigger"),
            shape = RoundedCornerShape(12.dp)
        )

        // Symptoms collection
        Column {
            Text("Physical symptoms (optional):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                physicalSymptoms.forEach { symptom ->
                    val isSelected = selectedSymptoms.contains(symptom)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedSymptoms.remove(symptom) else selectedSymptoms.add(symptom)
                        },
                        label = { Text(symptom) },
                        modifier = Modifier.testTag("form_symptom_$symptom")
                    )
                }
            }
        }

        // Location context
        Column {
            Text("Context / Location:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                locations.forEach { loc ->
                    val isSelected = seedLocation == loc
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { seedLocation = loc }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = loc,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Log note
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Quick feelings reflection notes") },
            placeholder = { Text("Describe details or thoughts...") },
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mood_form_note"),
            shape = RoundedCornerShape(12.dp)
        )

        if (showSuccessSnack) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mood log saved. Be gentle with yourself.", fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = {
                onSave(
                    scale,
                    selectedEmotions.toList(),
                    triggerText.ifBlank { "None" },
                    selectedSymptoms.toList(),
                    seedLocation,
                    noteText.ifBlank { "No notes provided" }
                )
                // Clear state
                scale = 5
                selectedEmotions.clear()
                triggerText = ""
                selectedSymptoms.clear()
                seedLocation = "Home"
                noteText = ""
                showSuccessSnack = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("mood_form_save_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Mood Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun MoodTrendsAndHistory(
    moodList: List<MoodLog>,
    onDelete: (Int) -> Unit
) {
    if (moodList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No Mood Logs Registered", fontWeight = FontWeight.Bold)
                Text(
                    "Switch tabs to save your current emotion baseline rating.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val averageVal = moodList.map { it.scale }.average()
        val highest = moodList.maxOf { it.scale }
        val lowest = moodList.minOf { it.scale }

        // Core stats panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Historic Reflection trends", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TrendStatColumn("Average Rating", String.format(Locale.getDefault(), "%.1f", averageVal), modifier = Modifier.weight(1f))
                    TrendStatColumn("Highest", "$highest/10", modifier = Modifier.weight(1f))
                    TrendStatColumn("Lowest", "$lowest/10", modifier = Modifier.weight(1f))
                }
            }
        }

        // Simple Visual Progress Bar chart for last 7 logs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Last 7 Days Journey", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val last7 = moodList.take(7).reversed()
                    last7.forEachIndexed { idx, log ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("${log.scale}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            // Progress vertical column bar
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height((log.scale * 10).dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (log.scale <= 3) Color(0xFFD85A30)
                                        else if (log.scale <= 6) Color(0xFF7F77DD)
                                        else Color(0xFF1D9E75)
                                    )
                            )
                            val SimpleDateFormat = SimpleDateFormat("EE", Locale.getDefault())
                            Text(SimpleDateFormat.format(Date(log.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }

        // Logs Timeline Title
        Text("Detailed Timeline History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        moodList.forEach { log ->
            MoodHistoryCard(log = log, onDelete = { onDelete(log.id) })
        }
    }
}

@Composable
fun TrendStatColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun MoodHistoryCard(log: MoodLog, onDelete: () -> Unit) {
    val emojis = listOf("😢", "😔", "😐", "🙂", "😊", "✨", "🌺", "🧠", "🌱", "🧘")
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val currentEmoji = emojis [log.scale.coerceIn(1, 10) - 1]
                    Text(currentEmoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val SimpleDateFormat = SimpleDateFormat("MMM d, yyyy - HH:mm", Locale.getDefault())
                        Text(SimpleDateFormat.format(Date(log.timestamp)), fontWeight = FontWeight.Bold)
                        Text("Trigger: ${log.trigger}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "${log.scale}/10",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Log", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (log.emotions.isNotEmpty()) {
                        Text("Emotions:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            log.emotions.forEach { emo ->
                                SuggestionChip(onClick = {}, label = { Text(emo) })
                            }
                        }
                    }

                    if (log.symptoms.isNotEmpty()) {
                        Text("Physical Symptoms:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            log.symptoms.forEach { sym ->
                                SuggestionChip(onClick = {}, label = { Text(sym) })
                            }
                        }
                    }

                    Text("Location: ${log.location}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Reflections notes:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(log.note, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }
        }
    }
}

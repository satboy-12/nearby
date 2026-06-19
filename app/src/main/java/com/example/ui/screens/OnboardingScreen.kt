package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.HealingViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: HealingViewModel,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    
    // Step 1: Welcome
    // Step 2: Story Tags
    val storyTags = remember { mutableStateListOf<String>() }
    val availableStoryTags = listOf("Heartbreak recovery", "Boundary building", "Rejection processing", "Self-esteem rebuilding")
    
    // Step 3: Goals
    val goalTags = remember { mutableStateListOf<String>() }
    val availableGoalTags = listOf("Maintain no-contact", "Reduce communication", "Build independence", "Process feelings")
    
    // Step 4: Baseline mood
    var baselineMood by remember { mutableStateOf(5) }
    
    // Step 5: Notification choices
    var notificationTime by remember { mutableStateOf("08:00") }
    var notificationEnabled by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("") }

    val totalSteps = 5

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP $currentStep of $totalSteps",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                LinearProgressIndicator(
                    progress = { currentStep.toFloat() / totalSteps.toFloat() },
                    modifier = Modifier
                        .width(150.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Step Transition
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    1 -> WelcomeStep(
                        userName = userName,
                        onNameChange = { userName = it }
                    )
                    2 -> StoryStep(
                        selectedTags = storyTags,
                        availableTags = availableStoryTags
                    )
                    3 -> GoalsStep(
                        selectedTags = goalTags,
                        availableTags = availableGoalTags
                    )
                    4 -> BaselineMoodStep(
                        scale = baselineMood,
                        onScaleChanged = { baselineMood = it }
                    )
                    5 -> NotificationStep(
                        time = notificationTime,
                        onTimeChange = { notificationTime = it },
                        enabled = notificationEnabled,
                        onEnabledToggle = { notificationEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Controller Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("onboarding_back_button")
                    ) {
                        Text("Back")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            viewModel.submitOnboarding(
                                name = userName,
                                storyTags = storyTags,
                                goalTags = goalTags,
                                baselineMood = baselineMood,
                                notificationTime = if (notificationEnabled) notificationTime else ""
                            )
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(text = if (currentStep == totalSteps) "Create My Healing Plan" else "Next")
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(
    userName: String,
    onNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_healing_onboarding),
            contentDescription = "Serene Healing Art",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Healing Starts Here",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Welcome to Mending. We provide a private, offline-first space to track emotional states, build resilient contact boundaries, and restore your self-love.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            label = { Text("What is your nickname?") },
            placeholder = { Text("e.g. Hopeful, Brave") },
            maxLines = 1,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("onboarding_name_input"),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoryStep(
    selectedTags: MutableList<String>,
    availableTags: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What brings you here today?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select all that apply to help personalize your affirmations and boundary recommendations.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            availableTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                    },
                    label = { Text(tag, fontSize = 15.sp, modifier = Modifier.padding(6.dp)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("story_tag_$tag")
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalsStep(
    selectedTags: MutableList<String>,
    availableTags: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Choose Your Healing Goals",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Establishing your intention creates the mental framework needed to break unhealthy patterns.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            availableTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                    },
                    label = { Text(tag, fontSize = 15.sp, modifier = Modifier.padding(6.dp)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.testTag("goal_tag_$tag")
                )
            }
        }
    }
}

@Composable
fun BaselineMoodStep(
    scale: Int,
    onScaleChanged: (Int) -> Unit
) {
    val emojis = listOf("😢", "😔", "😐", "🙂", "😊", "✨", "🌺", "🧠", "🌱", "🧘")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Baseline Mood",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "On a scale from 1 (deep pain) to 10 (peaceful expansion), how do you feel right now? This establishes day one.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Large Emoji representation
        val currentEmoji = emojis[scale.coerceIn(1, 10) - 1]
        Text(
            text = currentEmoji,
            fontSize = 96.sp,
            modifier = Modifier.animateContentSize()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mood rating: $scale / 10",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(36.dp))

        Slider(
            value = scale.toFloat(),
            onValueChange = { onScaleChanged(it.toInt().coerceIn(1, 10)) },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .testTag("baseline_mood_slider")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Struggling", style = MaterialTheme.typography.bodySmall)
            Text("Neutral", style = MaterialTheme.typography.bodySmall)
            Text("Flourishing", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun NotificationStep(
    time: String,
    onTimeChange: (String) -> Unit,
    enabled: Boolean,
    onEnabledToggle: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Healing Reminders",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Habituating soft check-ins and reflections is central to emotional healing. Set an alarm to prompt mood-logging and journaling alerts.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily check-in alert",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Recieve daily comforting affirmation reminders",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledToggle,
                        modifier = Modifier.testTag("onboarding_notif_switch")
                    )
                }

                if (enabled) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Preferred reminder hour:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("08:00", "12:00", "18:00", "21:00").forEach { preset ->
                            val isSelected = time == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { onTimeChange(preset) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

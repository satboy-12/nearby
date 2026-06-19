package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoalLog
import com.example.ui.viewmodel.HealingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalsScreen(viewModel: HealingViewModel) {
    val allGoals by viewModel.allGoals.collectAsState()
    val activeGoals by viewModel.activeGoals.collectAsState()
    val badges by viewModel.allBadges.collectAsState()

    var activeGoalsTab by remember { mutableStateOf(0) } // 0 = Habits & Goals, 1 = Milestone Badges
    var showAddGoalForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(selectedTabIndex = activeGoalsTab) {
            Tab(selected = activeGoalsTab == 0, onClick = { activeGoalsTab = 0 }, text = { Text("Habits & Tasks", fontWeight = FontWeight.Bold) }, modifier = Modifier.testTag("tab_habits"))
            Tab(selected = activeGoalsTab == 1, onClick = { activeGoalsTab = 1 }, text = { Text("Unlocks & Badges", fontWeight = FontWeight.Bold) }, modifier = Modifier.testTag("tab_badges"))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeGoalsTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Your Boundary & Care Goals", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Setting minor self-care habits helps establish boundaries and structure your day while recovering from emotional distress.", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = { showAddGoalForm = !showAddGoalForm },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_add_goal"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showAddGoalForm) "Close Form" else "Write New Self-Care Goal")
                    }

                    AnimatedVisibility(visible = showAddGoalForm) {
                        AddGoalForm(onSave = { name, desc, cat, freq, target, duration ->
                            viewModel.createGoal(name, desc, cat, freq, target, duration)
                            showAddGoalForm = false
                        })
                    }

                    Text("Active Objectives Checklist", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    if (activeGoals.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Active Habits Configured", fontWeight = FontWeight.Bold)
                                Text("Add customized care habits like meditating or journaling above.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    } else {
                        activeGoals.forEach { goal ->
                            GoalProgressCardItem(
                                goal = goal,
                                viewModel = viewModel,
                                onDelete = { viewModel.deleteGoal(goal.id) },
                                onCompletedToggle = { viewModel.toggleGoalActive(goal.id, false) }
                            )
                        }
                    }

                    val closedGoals = allGoals.filter { !it.isActive }
                    if (closedGoals.isNotEmpty()) {
                        Text("Archived / Completed Objectives", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        closedGoals.forEach { goal ->
                            CompletedGoalCardItem(goal = goal, onDelete = { viewModel.deleteGoal(goal.id) })
                        }
                    }
                }
            } else {
                BadgesListSection(earnedBadges = badges)
            }
        }
    }
}

@Composable
fun AddGoalForm(
    onSave: (String, String, String, String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Self-Care") }
    var freq by remember { mutableStateOf("Daily") }
    var target by remember { mutableStateOf(1) }
    var duration by remember { mutableStateOf(30) }

    val categories = listOf("Self-Care", "No-Contact", "Reflections", "Body Centering")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Create Self-Care Goal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("What habit do you want to build?") },
                placeholder = { Text("e.g. Journaling, 10 min Breathing, Walking") },
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_form_name"),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Intention notes (optional)") },
                placeholder = { Text("Describe why you choose this habit...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = category == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                            .clickable { category = cat }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Text("Frequency / Target Amount: $target times per $freq", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Daily", "Weekly").forEach { f ->
                    val isSelected = freq == f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                            )
                            .clickable { freq = f }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = f,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Slider(
                value = target.toFloat(),
                onValueChange = { target = it.toInt().coerceIn(1, 10) },
                valueRange = 1f..10f,
                steps = 8
            )

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, desc, category, freq, target, duration)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_form_save"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Lock Habit Goal")
            }
        }
    }
}

@Composable
fun GoalProgressCardItem(
    goal: GoalLog,
    viewModel: HealingViewModel,
    onDelete: () -> Unit,
    onCompletedToggle: () -> Unit
) {
    val progressLogs by viewModel.getGoalProgressFlow(goal.id).collectAsState(initial = emptyList())
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayProgress = progressLogs.firstOrNull { it.dateStr == todayStr }?.progress ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (goal.description.isNotBlank()) {
                        Text(goal.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                Row {
                    IconButton(onClick = onCompletedToggle) {
                        Icon(Icons.Default.Check, contentDescription = "Complete goal", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goal.category} - ${goal.targetFrequency}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "$todayProgress of ${goal.targetAmount} logged today",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val pct = (todayProgress.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { pct },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        viewModel.logGoalProgress(goal.id, todayStr, 1)
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_inc_progress_${goal.id}")
                ) {
                    Text("+1")
                }
            }
        }
    }
}

@Composable
fun CompletedGoalCardItem(
    goal: GoalLog,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(goal.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BadgesListSection(earnedBadges: List<com.example.data.model.BadgeLog>) {
    val predefinedBadges = listOf(
        Triple("Emotional Aware", "📊", "Logged 5 mood state logs"),
        Triple("Inner Path Finder", "📈", "Maintained emotional trends logs"),
        Triple("Iron Guard", "🛡️", "Successfully resisted contact urges"),
        Triple("Zen Shield Master", "💎", "Deep self-barrier mastery in temptations"),
        Triple("Writer's Journey", "📔", "Drafted 5 diaries entries"),
        Triple("Reflection Master", "📚", "Penned over 10 reflective logs"),
        Triple("Serene First Inhale", "🌬️", "Completed breathing calming exercises"),
        Triple("Zen Breath Harmony", "🌀", "Maintained deep oxygens inside the lounge"),
        Triple("Goal Achiever Elite", "🔥", "Completed habit goals targets")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Healing Achievements Milestone",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Celebrate your emotional boundary growth. Complete tracking and logging to automatically unlock milestone tokens.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(predefinedBadges) { (name, icon, label) ->
                val isEarned = earnedBadges.any { it.name.equals(name, ignoreCase = true) }
                
                Card(
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(
                         containerColor = if (isEarned) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                     ),
                     modifier = Modifier.fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = icon,
                            fontSize = 44.sp,
                            modifier = Modifier.background(
                                color = if (isEarned) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                                shape = CircleShape
                            ).padding(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            label,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 12.sp,
                            minLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isEarned) {
                            Surface(color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(8.dp)) {
                                Text("UNLOCKED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        } else {
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                                Text("LOCKED", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

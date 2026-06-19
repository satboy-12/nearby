package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
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
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.HealingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    viewModel: HealingViewModel,
    onNavigateToTab: (Int) -> Unit, // tab index
    onNavigateToSettings: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val rawMoods by viewModel.allMoodLogs.collectAsState()
    val contacts by viewModel.allContacts.collectAsState()
    val publications by viewModel.publishedJournals.collectAsState()
    val affirmations by viewModel.allAffirmations.collectAsState()
    val meditations by viewModel.allBreatheLogs.collectAsState()
    val goals by viewModel.allGoals.collectAsState()
    val badges by viewModel.allBadges.collectAsState()

    val currentAffirmations = affirmations.filter { !it.isCustom || it.isFavorite }
    var seedOffset by remember { mutableStateOf(0) }
    val matchingAff = remember(currentAffirmations, seedOffset) {
        if (currentAffirmations.isNotEmpty()) {
            currentAffirmations[(seedOffset) % currentAffirmations.size]
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mending",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToSettings() },
                        modifier = Modifier.testTag("nav_settings_button")
                    ) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings Page")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val validProfile = profile ?: UserProfile()

            // 1. GREETING & MOOD BAR
            GreetingCard(
                username = validProfile.displayName,
                lastMoodLogText = if (rawMoods.isNotEmpty()) {
                    val lastLog = rawMoods.first()
                    val format = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    "${lastLog.scale}/10 logged on ${format.format(Date(lastLog.timestamp))}"
                } else {
                    "No moods logged yet today"
                },
                onQuickMoodLog = { scale ->
                    val quickEmotions = when (scale) {
                        1, 2 -> listOf("struggling", "anxious")
                        3, 4 -> listOf("sad", "tired")
                        5, 6 -> listOf("neutral", "reflective")
                        7, 8 -> listOf("hopeful", "calm")
                        else -> listOf("peaceful", "strong")
                    }
                    viewModel.logMood(
                        scale = scale,
                        emotions = quickEmotions,
                        trigger = "Quick Entry",
                        symptoms = emptyList(),
                        location = "Home Dashboard",
                        note = "Quick-logged from the home screen dashboard."
                    )
                }
            )

            // 2. STREAK PROGRESS CIRCLE
            val noContactDays = if (contacts.isNotEmpty()) {
                val activeBlock = contacts.firstOrNull { it.isActive }
                if (activeBlock != null) {
                    val diff = System.currentTimeMillis() - activeBlock.blockedDate
                    (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                } else {
                    0
                }
            } else {
                0
            }
            StreakCard(
                daysCount = noContactDays.coerceAtLeast(validProfile.distanceStreakDays),
                onDetailClick = { onNavigateToTab(1) } // Contacts/Distance Tab
            )

            // 3. QUICK STATS PANEL (3-column layout)
            StatsGrid(
                journalsCount = publications.size,
                meditationsCount = meditations.size,
                badgesCount = badges.size
            )

            // 4. WEEKLY GOALS SUMMARY
            if (goals.isNotEmpty()) {
                val completedCount = goals.count { !it.isActive }
                GoalsProgressCard(
                    completed = completedCount,
                    total = goals.size,
                    goalsList = goals.take(3),
                    onViewGoals = { onNavigateToTab(4) } // Goals Tab
                )
            }

            // 5. AFFIRMATION GRADIENT CARD
            if (matchingAff != null) {
                AffirmationCard(
                    text = matchingAff.text,
                    category = matchingAff.category,
                    isLiked = matchingAff.isFavorite,
                    onNext = { seedOffset++ },
                    onLikeToggle = {
                        viewModel.toggleAffirmationFavorite(matchingAff.id, !matchingAff.isFavorite)
                    }
                )
            }

            // 6. QUICK ACTIONS TILE (2x3 Grid)
            QuickActionsGrid(
                onActionLogMood = { onNavigateToTab(1) }, // Mood Logging
                onActionJournal = { onNavigateToTab(3) }, // Journals
                onActionBreathe = { onNavigateToTab(4) }, // Zen Breathing
                onActionTemptation = { onNavigateToTab(2) }, // Boundary blocker
                onActionGames = { onNavigateToTab(6) } // Zen Games/Mind relax tab
            )

            // 7. RECENT ACHIEVEMENTS
            if (badges.isNotEmpty()) {
                BadgesListCard(badges = badges)
            }

            // 8. CRISIS EMERGENCIES PROMPT CARDS
            CrisisSupportPrompt { onNavigateToTab(0) } // Handled via sheet or profile trigger
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GreetingCard(
    username: String,
    lastMoodLogText: String,
    onQuickMoodLog: (Int) -> Unit
) {
    val uppercaseDate = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Natural Top Header Row (matching HTML exactly)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = uppercaseDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Hello, $username",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            // Glow profile dot inside white border shell
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(1.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF7F77DD), Color(0xFF1D9E75))
                            )
                        )
                )
            }
        }

        // Quick Mood Check Card styled with Natural Tones card specs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.65f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "How are you feeling right now?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 5 point emoji selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val emojiList = listOf("😢" to 2, "😔" to 4, "😐" to 6, "🙂" to 8, "✨" to 10)
                    emojiList.forEach { (emoji, scale) ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onQuickMoodLog(scale) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreakCard(
    daysCount: Int,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DISTANCE STREAK",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (daysCount > 0) {
                    Text(
                        text = "$daysCount Days",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "0 Days",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "of choosing yourself",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDetailClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Boundary Blocker", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Progress wheel matching HTML custom SVG circular gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White.copy(alpha = 0.2f),
                    strokeWidth = 4.dp
                )
                val fraction = if (daysCount > 0) ((daysCount % 30) / 30f).coerceIn(0.1f, 1f) else 0.35f
                CircularProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "${(fraction * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StatsGrid(
    journalsCount: Int,
    meditationsCount: Int,
    badgesCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val statsList = listOf(
            Triple(journalsCount.toString(), "Journals", Icons.Default.Edit),
            Triple(meditationsCount.toString(), "Inhales", Icons.Default.PlayArrow),
            Triple(badgesCount.toString(), "Badges", Icons.Default.Star)
        )
        statsList.forEach { triple ->
            val value = triple.first
            val label = triple.second
            val icon = triple.third
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        value,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        label,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun GoalsProgressCard(
    completed: Int,
    total: Int,
    goalsList: List<GoalLog>,
    onViewGoals: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Weekly Objectives",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onViewGoals) {
                    Text("All Goals", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val pct = if (total > 0) (completed.toFloat() / total.toFloat()) else 0f
            Text(
                "Progress rating: $completed of $total tasks completed (${(pct * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            goalsList.forEach { goal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (goal.isActive) Icons.Default.PlayArrow else Icons.Default.Check,
                            contentDescription = "Status icon",
                            tint = if (goal.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            goal.name,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        goal.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AffirmationCard(
    text: String,
    category: String,
    isLiked: Boolean,
    onNext: () -> Unit,
    onLikeToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${category.uppercase(Locale.getDefault())} INTENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onLikeToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isLiked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "\"$text\"",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Beautiful white pill button matching the mockup's 'Reflect' button but retaining live action
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Reflex",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onActionLogMood: () -> Unit,
    onActionJournal: () -> Unit,
    onActionBreathe: () -> Unit,
    onActionTemptation: () -> Unit,
    onActionGames: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = "Log Mood",
                emoji = "😔",
                description = "Feelings",
                color = MaterialTheme.colorScheme.primary,
                onClick = onActionLogMood,
                modifier = Modifier
                    .weight(1f)
                    .testTag("action_log_mood")
            )
            ActionTile(
                title = "Journal",
                emoji = "📝",
                description = "Express",
                color = MaterialTheme.colorScheme.tertiary,
                onClick = onActionJournal,
                modifier = Modifier
                    .weight(1f)
                    .testTag("action_write_journal")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = "Breathe",
                emoji = "🌬️",
                description = "Ground",
                color = MaterialTheme.colorScheme.secondary,
                onClick = onActionBreathe,
                modifier = Modifier
                    .weight(1f)
                    .testTag("action_breathe")
            )
            ActionTile(
                title = "Urges",
                emoji = "⚡",
                description = "Triggers",
                color = MaterialTheme.colorScheme.error,
                onClick = onActionTemptation,
                modifier = Modifier
                    .weight(1f)
                    .testTag("action_resisted_urge")
            )
            ActionTile(
                title = "Games",
                emoji = "🪵",
                description = "Zen Play",
                color = MaterialTheme.colorScheme.primary,
                onClick = onActionGames,
                modifier = Modifier
                    .weight(1f)
                    .testTag("action_mind_relaxing_games")
            )
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    emoji: String,
    description: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(58.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F0EA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Elegant colored background for emoji
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun BadgesListCard(badges: List<com.example.data.model.BadgeLog>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Earned Milestones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(badges) { badge ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(badge.icon, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(badge.name, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrisisSupportPrompt(
    onTriggerSupport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Struggling severely?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 15.sp
                )
                Text(
                    "Instant crisis hotlines & grounding toolkits are always available.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onTriggerSupport,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("HELP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

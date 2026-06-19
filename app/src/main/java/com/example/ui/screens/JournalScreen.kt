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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JournalEntry
import com.example.ui.viewmodel.AiState
import com.example.ui.viewmodel.HealingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: HealingViewModel) {
    val journals by viewModel.allJournals.collectAsState()

    var activeViewTab by remember { mutableStateOf(0) } // 0 = Timeline history, 1 = Write journal
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(selectedTabIndex = activeViewTab) {
            Tab(
                selected = activeViewTab == 0,
                onClick = { activeViewTab = 0 },
                text = { Text("Timeline History", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("timeline_tab")
            )
            Tab(
                selected = activeViewTab == 1,
                onClick = { activeViewTab = 1 },
                text = { Text("Write Entry", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("write_entry_tab")
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeViewTab == 0) {
                JournalTimeline(
                    journals = journals,
                    onDelete = { id -> viewModel.deleteJournal(id) }
                )
            } else {
                WriteJournalForm(
                    onSave = { content, mood, tags, isPrivate, isDraft ->
                        viewModel.saveJournalEntry(content, mood, tags, isPrivate, isDraft, "neutral")
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun JournalTimeline(
    journals: List<JournalEntry>,
    onDelete: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf("") }

    val filteredList = remember(journals, searchQuery, selectedTagFilter) {
        journals.filter { entry ->
            val matchQuery = entry.content.contains(searchQuery, ignoreCase = true)
            val matchTag = if (selectedTagFilter.isEmpty()) true else entry.tags.contains(selectedTagFilter)
            matchQuery && matchTag
        }
    }

    // Extract all unique tags
    val allTags = remember(journals) {
        journals.flatMap { it.tags }.distinct().take(6)
    }

    if (journals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Your Reflection Timeline is Empty", fontWeight = FontWeight.Bold)
                Text(
                    "Switch tabs to write your initial comforting reflections and unlock milestones.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Filters row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search journals by keyword...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            maxLines = 1,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("journal_search"),
            shape = RoundedCornerShape(12.dp)
        )

        // Filter chips list
        if (allTags.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTagFilter.isEmpty(),
                    onClick = { selectedTagFilter = "" },
                    label = { Text("All") },
                    shape = RoundedCornerShape(12.dp)
                )
                allTags.forEach { tag ->
                    val isSelected = selectedTagFilter == tag
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTagFilter = if (isSelected) "" else tag },
                        label = { Text(tag) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("filter_tag_$tag")
                    )
                }
            }
        }

        // Timeline log lists
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Diaries Log Entries (${filteredList.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            filteredList.forEach { entry ->
                JournalListEntryCard(entry = entry, onDelete = { onDelete(entry.id) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WriteJournalForm(
    onSave: (String, Int?, List<String>, Boolean, Boolean) -> Unit,
    viewModel: HealingViewModel
) {
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<Int?>(null) }
    var tagsInput by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(true) }
    var isDraft by remember { mutableStateOf(false) }

    val aiState by viewModel.aiInsight.collectAsState()

    val currentOnboardingDetails by viewModel.userProfile.collectAsState()

    val prompts = listOf(
        "What is one feeling you are running away from today?",
        "If you could write a letter of forgiveness to yourself, what would it say?",
        "Describe three small boundary steps to maintain your peace tonight.",
        "What holds you back from letting go entirely?",
        "Detail the emotional changes you have noticed since starting the no-contact rule."
    )
    var activePromptIdx by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Compassionate Prompts Rotating banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DAILY COMPASSIONATE PROMPT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { activePromptIdx = (activePromptIdx + 1) % prompts.size }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Next Prompt", modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    prompts[activePromptIdx],
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Mood quick indicator selector
        Text("Quick Mood Reference (optional):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val emojis = listOf("😢" to 2, "😔" to 4, "😐" to 6, "🙂" to 8, "😊" to 10)
            emojis.forEach { (emoji, scaleValue) ->
                val isSelected = selectedMood == scaleValue
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { selectedMood = if (isSelected) null else scaleValue },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 20.sp)
                }
            }
        }

        // Content diary fields
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("Begin letting go. Express feelings, triggers, or boundaries openly here...") },
            minLines = 8,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("journal_form_content"),
            shape = RoundedCornerShape(12.dp)
        )

        // Character counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Words: ${content.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Text("Max symbols: ${content.length}/5000", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }

        // Tags picker
        OutlinedTextField(
            value = tagsInput,
            onValueChange = { tagsInput = it },
            label = { Text("Reflection tags (comma-separated)") },
            placeholder = { Text("e.g. heartbreak, healing, victory") },
            maxLines = 1,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        // Privacy switch row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Private Diary Lock", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Securely persistent to local Room file only", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
        }

        // Gemini helper block!
        Button(
            onClick = {
                if (content.isNotBlank()) {
                    viewModel.getEmotionalSupportInsight(content, isContactTemptation = false)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Star, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Review with AI Therapist Insight")
        }

        when (val state = aiState) {
            is AiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is AiState.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Active Insight", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compassionate AI Reframing Support:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(state.text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            is AiState.Error -> Text("Connection info: ${state.message}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            else -> {}
        }

        // Action controllers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (content.isNotBlank()) {
                        val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onSave(content, selectedMood, tagsList, isPrivate, true)
                        content = ""
                        tagsInput = ""
                        selectedMood = null
                        viewModel.clearAiInsight()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Draft")
            }

            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onSave(content, selectedMood, tagsList, isPrivate, false)
                        content = ""
                        tagsInput = ""
                        selectedMood = null
                        viewModel.clearAiInsight()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("journal_form_publish"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Publish to Logs")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun JournalListEntryCard(
    entry: JournalEntry,
    onDelete: () -> Unit
) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val SimpleDateFormat = SimpleDateFormat("MMM d, yyyy - HH:mm", Locale.getDefault())
                    Text(SimpleDateFormat.format(Date(entry.timestamp)), fontWeight = FontWeight.Bold)
                    Text("Words: ${entry.wordCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (entry.isDraft) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp)) {
                            Text("DRAFT", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    if (entry.moodScale != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        val emojis = listOf("😢", "😔", "😐", "🙂", "😊", "✨", "🌺", "🧠", "🌱", "🧘")
                        Text(emojis[entry.moodScale.coerceIn(1, 10) - 1], fontSize = 24.sp)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete entry", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = entry.content,
                fontSize = 14.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )

            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.tags.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "#$tag",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.HealingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HealingViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val profile by viewModel.userProfile.collectAsState()
    val moods by viewModel.allMoodLogs.collectAsState()
    val journals by viewModel.allJournals.collectAsState()
    val contacts by viewModel.allContacts.collectAsState()

    val validProfile = profile ?: UserProfile()

    var nickname by remember(profile) { mutableStateOf(validProfile.displayName) }
    var notificationTime by remember(profile) { mutableStateOf(validProfile.notificationTime) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSOSDialog by remember { mutableStateOf(false) }

    var expandedFaq by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Care Configurations", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            // 1. PROFILE CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Your Healing Identity", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Nickname") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = notificationTime,
                        onValueChange = { notificationTime = it },
                        label = { Text("Check-in Alert Hour") },
                        placeholder = { Text("e.g. 08:00") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (nickname.isNotBlank()) {
                                viewModel.updateProfile(
                                    validProfile.copy(
                                        displayName = nickname,
                                        notificationTime = notificationTime
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_profile_save"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Profile Changes")
                    }
                }
            }

            // 2. CRISIS HOTLINES (MODULE 10)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Immediate Crisis Support (24/7)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                    }

                    Text("If you find yourself experiencing severe anxiety, painful trauma spikes, or thoughts of self-harm, please reach out to professional support networks immediately. Your safety is paramount.", fontSize = 12.sp)

                    Divider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f))

                    // US Phone Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("US Suicide Lifeline", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Call or message 988", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:988"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.testTag("hotline_call_988")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call 988", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    // UK Samaritans Phone Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("UK Samaritans Hotline", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Call 111", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:111"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call UK hotline", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    Button(
                        onClick = { showSOSDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_sos_draft"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Draft Emergency Support Message")
                    }
                }
            }

            // 3. FAQS AND GUIDES
            Text("Inner Healing Guides & FAQs", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            val faqs = listOf(
                "Why is the 'No-Contact Rule' so vital?" to "Separating yourself entirely from a former connection blocks the emotional triggers that renew attachment spikes. It allows your somatic system to level out and reclaim independent identity.",
                "How do I manage an intense urge to call?" to "When a temptation spike hits, try our '5-4-3-2-1 Sensory Grounding' or '4-7-8 Breathing' exercises in the Breathe tab. Deliberate deep breathes shut down amygdala fear impulses.",
                "How are my data and journals protected?" to "Mending utilizes SQLite Room databases local to your device. ZERO notes or journals are uploaded to cloud servers. Your raw emotional states remain 100% private to you."
            )

            faqs.forEachIndexed { index, (q, a) ->
                val isExpanded = expandedFaq == index
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedFaq = if (isExpanded) -1 else index },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(q, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Icon(
                                if (isExpanded) Icons.Default.Clear else Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(a, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // 4. DATA PORTABILITY (LOCAL JSON EXPORT)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Portability & Local Backups", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Export your offline logs, journals, and boundary configurations as a standard JSON string for backups or sharing with your counselor.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_json"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Offline Data as JSON")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // JSON Export dialog
    if (showExportDialog) {
        val backupJson = remember(moods, journals, contacts) {
            val moodMap = moods.map { "{ scale:${it.scale}, trigger:\"${it.trigger}\", note:\"${it.note}\", time:${it.timestamp} }" }
            val journalMap = journals.map { "{ content:\"${it.content.take(60)}\", wordCount:${it.wordCount}, time:${it.timestamp} }" }
            val contactMap = contacts.map { "{ name:\"${it.name}\", totalStreak:${it.durationDays} }" }
            
            """
            {
              "app": "Mending Healing",
              "exported_at": ${System.currentTimeMillis()},
              "mood_logs_count": ${moods.size},
              "mood_logs": [ ${moodMap.joinToString(", ")} ],
              "journals_count": ${journals.size},
              "journal_entries": [ ${journalMap.joinToString(", ")} ],
              "contacts_rules_count": ${contacts.size},
              "contacts": [ ${contactMap.joinToString(", ")} ]
            }
            """.trimIndent()
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exported Offline Backup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Copy the backup string below. It represents your entire secure offline repository.", fontSize = 12.sp)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        Text(
                            backupJson,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(backupJson))
                        showExportDialog = false
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // SOS SMS message dialog
    if (showSOSDialog) {
        val sosTemplate = "Hey. I am currently feeling triggered and practicing boundary grounding. I would value some brief, peaceful distraction conversation right now. Let me know if you can chat."
        AlertDialog(
            onDismissRequest = { showSOSDialog = false },
            title = { Text("Grounding SOS Message", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("We've drafted a supportive draft template to secure clarity and prevent breaking no-contact barriers. Share this with a trusted supporter.", fontSize = 12.sp)
                    OutlinedTextField(
                        value = sosTemplate,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(sosTemplate))
                        showSOSDialog = false
                    }
                ) {
                    Text("Copy Template")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSOSDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

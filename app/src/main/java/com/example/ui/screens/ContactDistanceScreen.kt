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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactBlock
import com.example.data.model.TemptationLog
import com.example.ui.viewmodel.AiState
import com.example.ui.viewmodel.HealingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactDistanceScreen(viewModel: HealingViewModel) {
    val contacts by viewModel.allContacts.collectAsState()
    val temptations by viewModel.allTemptations.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }
    var showUrgeForm by remember { mutableStateOf(false) }
    var selectedContactForUrge by remember { mutableStateOf<ContactBlock?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Intro
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Distance & Boundaries",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Distance creates emotional clarity. Register active blocks to calculate growth metrics and prevent contact breakages.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showAddForm = !showAddForm },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("block_new_contact_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Boundary Rule")
                }

                if (contacts.any { it.isActive }) {
                    Button(
                        onClick = {
                            selectedContactForUrge = contacts.firstOrNull { it.isActive }
                            showUrgeForm = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("temptation_block_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Impulsive Urge")
                    }
                }
            }

            // Forms drop down animator
            AnimatedVisibility(visible = showAddForm) {
                AddContactForm(onSave = { name, phone, social, note, days ->
                    viewModel.blockContact(name, phone, social, note, days)
                    showAddForm = false
                })
            }

            AnimatedVisibility(visible = showUrgeForm) {
                TemptationUrgeForm(
                    contactId = selectedContactForUrge?.id ?: 0,
                    contactName = selectedContactForUrge?.name ?: "Contact",
                    onDismiss = { showUrgeForm = false },
                    onSave = { cId, intensity, trigger, didAct, redirection ->
                        viewModel.logTemptation(cId, intensity, trigger, didAct, redirection)
                        showUrgeForm = false
                    },
                    viewModel = viewModel
                )
            }

            // Active boundary statuses list
            Text(
                "Active Boundary Blocks",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            val activeContacts = contacts.filter { it.isActive }
            if (activeContacts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Active Boundaries Set", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "Add a person above to launch stopwatch day counters & motivation badges.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                activeContacts.forEach { contact ->
                    ActiveContactBlockCard(
                        contact = contact,
                        onUnblock = { viewModel.unblockContact(contact.id) },
                        onResetStreak = { viewModel.resetContactStreak(contact.id) }
                    )
                }
            }

            // Urges log entries
            if (temptations.isNotEmpty()) {
                Text(
                    "Resisted Urges Log Journal",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                temptations.forEach { log ->
                    TemptationLogCard(
                        log = log,
                        contactName = contacts.firstOrNull { it.id == log.contactId }?.name ?: "Mended Connection",
                        onDelete = { viewModel.deleteTemptation(log.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AddContactForm(
    onSave: (String, String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var social by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf(30) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Rule Boundary Definition", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Person Nickname Code") },
                placeholder = { Text("e.g. Shadow, X, Redacted") },
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_form_name"),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                placeholder = { Text("Optional") },
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_form_phone"),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = social,
                onValueChange = { social = it },
                label = { Text("Social Accounts handles") },
                placeholder = { Text("e.g. IG, Snap - Optional") },
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_form_social"),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Reason for boundary / distance notes") },
                placeholder = { Text("Remind yourself why you chose healthy space...") },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_form_notes"),
                shape = RoundedCornerShape(10.dp)
            )

            Text("Goal duration (days):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(7, 14, 30, 90, 180).forEach { days ->
                    val isSelected = durationDays == days
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                            .clickable { durationDays = days }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$days d",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, phone, social, notes, durationDays)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_form_save"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Lock Boundary Status")
            }
        }
    }
}

@Composable
fun TemptationUrgeForm(
    contactId: Int,
    contactName: String,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String, Boolean, String) -> Unit,
    viewModel: HealingViewModel
) {
    var intensity by remember { mutableStateOf(5) }
    var trigger by remember { mutableStateOf("") }
    var didAct by remember { mutableStateOf(false) }
    var redirectionUsed by remember { mutableStateOf("4-7-8 Breathing") }

    val aiState by viewModel.aiInsight.collectAsState()

    val redirections = listOf("4-7-8 Breathing", "5-4-3-2-1 Grounding", "Write Journal", "Drink Cold Water")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Log Boundary Temptation urge: $contactName", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Form")
                }
            }

            Text("How intense is the urge? ($intensity/10)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = intensity.toFloat(),
                onValueChange = { intensity = it.toInt().coerceIn(1, 10) },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.testTag("temptation_slider")
            )

            OutlinedTextField(
                value = trigger,
                onValueChange = { trigger = it },
                label = { Text("What triggered this urge?") },
                placeholder = { Text("e.g. heard an old playlist, felt tired, lonely...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("temptation_trigger_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Text("Redirection method utilized:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                redirections.take(2).forEach { rdr ->
                    val isSelected = redirectionUsed == rdr
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface
                            )
                            .clickable { redirectionUsed = rdr }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rdr,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = didAct,
                    onCheckedChange = { didAct = it },
                    modifier = Modifier.testTag("temptation_did_act_box")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Did you contact them? (Act on urge)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Selecting this resets the contact days stream.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            Button(
                onClick = {
                    viewModel.getEmotionalSupportInsight(trigger, isContactTemptation = true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Compassionate AI Grounding")
            }

            when (val state = aiState) {
                is AiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is AiState.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Comforting AI Grounding:", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                is AiState.Error -> Text("Connection info: ${state.message}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                else -> {}
            }

            Button(
                onClick = {
                    onSave(contactId, intensity, trigger, didAct, redirectionUsed)
                    viewModel.clearAiInsight()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("temptation_submit"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Log Impulse Urge Entry")
            }
        }
    }
}

@Composable
fun ActiveContactBlockCard(
    contact: ContactBlock,
    onUnblock: () -> Unit,
    onResetStreak: () -> Unit
) {
    val totalDiffMs = System.currentTimeMillis() - contact.blockedDate
    val elapsedDays = (totalDiffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    var showConfirmReset by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            contact.name.take(2).uppercase(Locale.getDefault()),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Duration target: ${contact.durationDays} days", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                Row {
                    IconButton(onClick = { showConfirmReset = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset streak", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onUnblock) {
                        Icon(Icons.Default.Lock, contentDescription = "Remove rule", tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Contact distance active:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Text(
                    "$elapsedDays of ${contact.durationDays} days",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (elapsedDays.toFloat() / contact.durationDays.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            if (contact.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        contact.notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }

            if (showConfirmReset) {
                AlertDialog(
                    onDismissRequest = { showConfirmReset = false },
                    title = { Text("Reset No-Contact Streak?", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you broke no-contact boundaries with ${contact.name}? Confirming this resets your continuous boundary streak and counters back to day 0.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onResetStreak()
                                showConfirmReset = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Yes, Reset Streak", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmReset = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TemptationLogCard(
    log: TemptationLog,
    contactName: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (log.didAct) Icons.Default.Close else Icons.Default.Check,
                        contentDescription = "Status",
                        tint = if (log.didAct) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (log.didAct) "Broke contact with $contactName" else "Successfully Resisted Contact urge",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete log", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Intensity rating: ${log.intensity}/10", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("Trigger reported: ${log.trigger}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text("Redirection tool: ${log.redirectionUsed}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)

            val SimpleDateFormat = SimpleDateFormat("MMM d, yyyy - HH:mm", Locale.getDefault())
            Text(
                "Logged on ${SimpleDateFormat.format(Date(log.timestamp))}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

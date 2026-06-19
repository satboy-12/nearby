package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.api.GeminiContent
import com.example.api.GeminiGenerationConfig
import com.example.api.GeminiPart
import com.example.api.GeminiRequest
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val text: String) : AiState()
    data class Error(val message: String) : AiState()
}

class HealingViewModel(private val repository: AppRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureProfileExists()
        }
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMoodLogs: StateFlow<List<MoodLog>> = repository.allMoodLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContacts: StateFlow<List<ContactBlock>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeContacts: StateFlow<List<ContactBlock>> = repository.activeContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTemptations: StateFlow<List<TemptationLog>> = repository.allTemptations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJournals: StateFlow<List<JournalEntry>> = repository.allJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedJournals: StateFlow<List<JournalEntry>> = repository.publishedJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAffirmations: StateFlow<List<AffirmationLog>> = repository.allAffirmations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBreatheLogs: StateFlow<List<BreatheLog>> = repository.allBreatheLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<GoalLog>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeGoals: StateFlow<List<GoalLog>> = repository.activeGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBadges: StateFlow<List<BadgeLog>> = repository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkoutLogs: StateFlow<List<WorkoutLog>> = repository.allWorkoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Insight State
    private val _aiInsight = MutableStateFlow<AiState>(AiState.Idle)
    val aiInsight: StateFlow<AiState> = _aiInsight.asStateFlow()

    // Workout AI Assistant State
    private val _workoutAiInsight = MutableStateFlow<AiState>(AiState.Idle)
    val workoutAiInsight: StateFlow<AiState> = _workoutAiInsight.asStateFlow()

    // --- ONBOARDING ACTIONS ---
    fun submitOnboarding(
        name: String,
        storyTags: List<String>,
        goalTags: List<String>,
        baselineMood: Int,
        notificationTime: String
    ) {
        viewModelScope.launch {
            val defaultProfile = UserProfile(
                id = 1,
                onboarded = true,
                displayName = name.ifBlank { "Healer" },
                healingGoalTags = storyTags.joinToString(","),
                selectedGoals = goalTags.joinToString(","),
                notificationTime = notificationTime,
                totalActiveDays = 1,
                totalMoodLogs = 1
            )
            repository.updateProfile(defaultProfile)

            // Log their initial mood based on baseline selection
            repository.logMood(
                scale = baselineMood,
                emotions = listOf("Onboarding baseline"),
                trigger = "Onboarding",
                symptoms = emptyList(),
                location = "App Onboarding",
                note = "My baseline mood upon starting the healing journey."
            )
        }
    }

    // --- MOOD ACTIONS ---
    fun logMood(
        scale: Int,
        emotions: List<String>,
        trigger: String,
        symptoms: List<String>,
        location: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.logMood(scale, emotions, trigger, symptoms, location, note)
        }
    }

    fun deleteMoodLog(id: Int) {
        viewModelScope.launch {
            repository.deleteMoodLog(id)
        }
    }

    // --- CONTACT BLOCKING ACTIONS ---
    fun blockContact(name: String, phone: String, social: String, notes: String, durationDays: Int) {
        viewModelScope.launch {
            repository.blockContact(name, phone, social, notes, durationDays)
        }
    }

    fun unblockContact(id: Int) {
        viewModelScope.launch {
            repository.unblockContact(id)
        }
    }

    fun deleteContact(id: Int) {
        viewModelScope.launch {
            repository.deleteContact(id)
        }
    }

    fun resetContactStreak(contactId: Int) {
        viewModelScope.launch {
            val contactList = allContacts.value
            val target = contactList.firstOrNull { it.id == contactId }
            if (target != null) {
                // Update the contact's blockedDate to current system time, effectively resetting days counter to 0!
                val resetContact = target.copy(blockedDate = System.currentTimeMillis())
                repository.updateProfile(userProfile.value?.copy(distanceStreakDays = 0) ?: UserProfile())
                // Insert the updated contact block
                repository.blockContact(
                    name = resetContact.name,
                    phone = resetContact.phone,
                    social = resetContact.socialMedia,
                    notes = "Streak reset at ${System.currentTimeMillis()}: " + resetContact.notes,
                    days = resetContact.durationDays
                )
                // Delete previous reference
                repository.deleteContact(target.id)
            }
        }
    }

    // --- TEMPTATION ACTIONS ---
    fun logTemptation(contactId: Int, intensity: Int, trigger: String, didAct: Boolean, redirectionUsed: String) {
        viewModelScope.launch {
            repository.logTemptation(contactId, intensity, trigger, didAct, redirectionUsed)
        }
    }

    fun deleteTemptation(id: Int) {
        viewModelScope.launch {
            repository.deleteTemptation(id)
        }
    }

    // --- DIARY JOURNAL ACTIONS ---
    fun saveJournalEntry(
        content: String,
        mood: Int?,
        tags: List<String>,
        isPrivate: Boolean,
        isDraft: Boolean,
        sentiment: String
    ) {
        viewModelScope.launch {
            repository.saveJournal(content, mood, tags, isPrivate, isDraft, sentiment)
        }
    }

    fun deleteJournal(id: Int) {
        viewModelScope.launch {
            repository.deleteJournal(id)
        }
    }

    // --- AFFIRMATION ACTIONS ---
    fun createCustomAffirmation(text: String, category: String) {
        viewModelScope.launch {
            repository.createAffirmation(text, category, isCustom = true, isFav = true)
        }
    }

    fun toggleAffirmationFavorite(id: Int, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleAffirmationFavorite(id, isFav)
        }
    }

    fun deleteAffirmation(id: Int) {
        viewModelScope.launch {
            repository.deleteAffirmation(id)
        }
    }

    // --- BREATHING EXERCISE ACTIONS ---
    fun logBreathingExercise(technique: String, durationSeconds: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.logBreathing(technique, durationSeconds, completed)
        }
    }

    // --- GOALS ACTIONS ---
    fun createGoal(name: String, description: String, category: String, targetFrequency: String, targetAmount: Int, durationDays: Int) {
        viewModelScope.launch {
            repository.createGoal(name, description, category, targetFrequency, targetAmount, durationDays)
        }
    }

    fun toggleGoalActive(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleGoalActiveStatus(id, isActive)
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    fun logGoalProgress(goalId: Int, dateStr: String, increment: Int) {
        viewModelScope.launch {
            repository.logGoalProgress(goalId, dateStr, increment)
        }
    }

    fun getGoalProgressFlow(goalId: Int): Flow<List<GoalProgress>> {
        return repository.getProgressByGoal(goalId)
    }

    // --- GEMINI INTELLIGENT EMOTIONAL FEEDBACK ---
    fun getEmotionalSupportInsight(journalContent: String, isContactTemptation: Boolean = false) {
        viewModelScope.launch {
            _aiInsight.value = AiState.Loading

            val apiKey = try {
                com.example.BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // Return a compassionate placeholder if API key is not ready
                val fallbackText = if (isContactTemptation) {
                    "Stay strong. Distance is an act of profound self-respect, not rejection. Take a deep breath, count to ten, and let the impulse pass without acting. You are enough."
                } else {
                    "Thank you for sharing your heart. Deep down, you possess the grace and inner strength to heal from this. Honor your feelings, but also honor your future self."
                }
                _aiInsight.value = AiState.Success(fallbackText)
                return@launch
            }

            val promptText = if (isContactTemptation) {
                "The user is struggling with an emotional urge to break contact/boundaries. Here is their state details: '$journalContent'. Respond with a short (2-3 sentences max) compassionate, non-judgmental, and highly grounding supportive response to help them stay strong and safe."
            } else {
                "The user wrote a personal journal entry: '$journalContent'. Respond with a warm, caring, gentle (2-3 sentences max) therapeutic guidance that honors their feelings, builds boundary resilience, and restores self-worth."
            }

            try {
                val requestModel = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = promptText)))
                    ),
                    generationConfig = GeminiGenerationConfig(temperature = 0.6f, maxOutputTokens = 200)
                )

                val response = GeminiClient.service.generateContent(
                    apiKey = apiKey,
                    request = requestModel
                )

                val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!resultText.isNullOrBlank()) {
                    _aiInsight.value = AiState.Success(resultText)
                } else {
                    _aiInsight.value = AiState.Error("Could not generate text candidates.")
                }
            } catch (e: Exception) {
                _aiInsight.value = AiState.Error("Connection error: ${e.localizedMessage}")
            }
        }
    }

    fun clearAiInsight() {
        _aiInsight.value = AiState.Idle
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    // --- WORKOUT ASSISTANT ACTIONS ---
    fun logWorkout(type: String, durationMins: Int, exercisesCount: Int, notes: String, feedbackEmoji: String) {
        viewModelScope.launch {
            repository.logWorkout(type, durationMins, exercisesCount, notes, feedbackEmoji)
        }
    }

    fun deleteWorkoutLog(id: Int) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(id)
        }
    }

    fun getWorkoutAssistantAdvice(focus: String, durationMinutes: Int, name: String) {
        viewModelScope.launch {
            _workoutAiInsight.value = AiState.Loading

            val apiKey = try {
                com.example.BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                val naturalFallback = when (focus) {
                    "Restorative Yoga" -> "1. Tree Pose (Vrikshasana) - 3 mins\nRoot your feet deep into the floor, align your core, and breathe slowly.\n\n2. Child's Pose (Balasana) - 4 mins\nRelease lower back tension and stretch arms forward flatly.\n\n3. Pigeon Pose (Kapotasana) - 4 mins\nDeep hip-opener pose to release stored emotional blockages.\n\n4. Corpse Pose (Savasana) - 4 mins\nRest in complete physical and mental surrender on the ground."
                    "Strength & Tone" -> "1. Alignment Squats - 3 mins\nLower mindfully to the ground, keeping knees straight and pushing from heels.\n\n2. Mindful Push-ups - 3 mins\nHover close to the floor, pushing with core engagement in high plank.\n\n3. Grounding Plank Hold - 4 mins\nForearms down flatly, gaze neutral, breathing slow under belly tension.\n\n4. Superman Arch - 4 mins\nLie on stomach, raising upper chest and thighs off floor to restore spine."
                    "Cardio Flow" -> "1. Dynamic High Knees - 3 mins\nLightweight rhythmic bounding, pumping elbows in sync with dynamic tempo.\n\n2. Mountain Climbers - 4 mins\nQuick high plank leg alternations, maintaining hips level with shoulders.\n\n3. Lateral Lunges - 4 mins\nDeep side-to-side sweeping shifts and centered balance placements.\n\n4. Mindful Jumping Jacks - 4 mins\nWide open shoulder extensions, breathing out with each floating jump."
                    "HIIT Grounding" -> "1. Burpee Drops - 3 mins\nJump up, drop and touch chest to floor, then rise with resilience.\n\n2. Dynamic Plank Jacks - 4 mins\nIn high plank, bounce feet outward and inward with solid abdominal lock.\n\n3. Speed Skater Leaps - 4 mins\nJump laterally from left to right, matching each hop with physical sweep.\n\n4. Mindful Squat Jumps - 4 mins\nDeep earth squats exploding upward to land smoothly like floating feathers."
                    "Core Alignment" -> "1. Pilates Hundred - 3 mins\nLie down, lift shoulders, pump arms rapidly while breathing deeply.\n\n2. Bicycle Crunches - 4 mins\nRotate torso mindfully to squeeze core on opposing knee drives.\n\n3. Bird Dog Placements - 4 mins\nExtend opposite arm and leg mindfully with back flat like a table.\n\n4. Russian Twists - 4 mins\nHover in high boat pose, twisting torso slowly with exhalation taps."
                    else -> "1. Deep Neck Stretches - 3 mins\nStretch side-to-side mindfully.\n\n2. Cat-Cow Pose - 4 mins\nArch and release back to restore energy flows.\n\n3. Mindful Forward Fold - 4 mins\nStretch hamstrings and relax head.\n\n4. Corpse Pose - 4 mins\nSurrender weights on modern ground mat."
                }
                _workoutAiInsight.value = AiState.Success(naturalFallback)
                return@launch
            }

            val promptText = "You are an expert mindful Workout Assistant. The user '$name' wants a customized wellness physical workout session focusing on '$focus' for '$durationMinutes' minutes. " +
                    "Generate a highly specific instruction detailing exactly 4 customized movements or exercises. For each exercise, specify the title name, duration in minutes, and a 1-sentence mindful execution instruction under it. " +
                    "Keep the response professional, encouraging, formatted as a numbered list with double line breaks, and free of any introductory conversational text."

            try {
                val requestModel = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = promptText)))
                    ),
                    generationConfig = GeminiGenerationConfig(temperature = 0.5f, maxOutputTokens = 350)
                )

                val response = GeminiClient.service.generateContent(
                    apiKey = apiKey,
                    request = requestModel
                )

                val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!resultText.isNullOrBlank()) {
                    _workoutAiInsight.value = AiState.Success(resultText)
                } else {
                    _workoutAiInsight.value = AiState.Error("Generation returned an empty response. Try again.")
                }
            } catch (e: Exception) {
                _workoutAiInsight.value = AiState.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun clearWorkoutAiInsight() {
        _workoutAiInsight.value = AiState.Idle
    }
}

class HealingViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

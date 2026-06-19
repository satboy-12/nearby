package com.example.data.repository

import com.example.data.db.HealingDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val dao: HealingDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allMoodLogs: Flow<List<MoodLog>> = dao.getAllMoodLogs()
    val allContacts: Flow<List<ContactBlock>> = dao.getAllContacts()
    val activeContacts: Flow<List<ContactBlock>> = dao.getActiveContacts()
    val allTemptations: Flow<List<TemptationLog>> = dao.getAllTemptations()
    val allJournals: Flow<List<JournalEntry>> = dao.getAllJournals()
    val publishedJournals: Flow<List<JournalEntry>> = dao.getPublishedJournals()
    val allAffirmations: Flow<List<AffirmationLog>> = dao.getAllAffirmations()
    val allBreatheLogs: Flow<List<BreatheLog>> = dao.getAllBreatheLogs()
    val allGoals: Flow<List<GoalLog>> = dao.getAllGoals()
    val activeGoals: Flow<List<GoalLog>> = dao.getActiveGoals()
    val allBadges: Flow<List<BadgeLog>> = dao.getAllBadges()
    val allWorkoutLogs: Flow<List<WorkoutLog>> = dao.getAllWorkoutLogs()

    suspend fun ensureProfileExists() {
        val current = dao.getUserProfileDirect()
        if (current == null) {
            val defaultProfile = UserProfile(
                id = 1,
                onboarded = false,
                displayName = "Healer",
                healingGoalTags = "Heartbreak recovery,Boundary building",
                selectedGoals = "Maintain no-contact,Build independence",
                language = "en",
                themeMode = "system"
            )
            dao.insertUserProfile(defaultProfile)
            populatePreloadedAffirmations()
        }
    }

    suspend fun updateProfile(profile: UserProfile) {
        dao.insertUserProfile(profile)
    }

    // --- MOOD PROGRESSION & BADGES ---
    suspend fun logMood(
        scale: Int,
        emotions: List<String>,
        trigger: String,
        symptoms: List<String>,
        location: String,
        note: String
    ) {
        val log = MoodLog(
            scale = scale,
            emotions = emotions,
            trigger = trigger,
            symptoms = symptoms,
            location = location,
            note = note
        )
        dao.insertMoodLog(log)

        // Increment counts and evaluate achievements
        val profile = dao.getUserProfileDirect() ?: UserProfile()
        val nextMoodLogsCount = profile.totalMoodLogs + 1
        val updated = profile.copy(totalMoodLogs = nextMoodLogsCount)
        dao.insertUserProfile(updated)

        // Achievements triggers
        if (nextMoodLogsCount >= 5) {
            checkAndUnlockBadge("Emotional Aware", "📊", "Self-reflection base camp reached")
        }
        if (nextMoodLogsCount >= 20) {
            checkAndUnlockBadge("Inner Path Finder", "📈", "Logged 20 mood states over time")
        }
    }

    suspend fun deleteMoodLog(id: Int) {
        dao.deleteMoodLogById(id)
    }

    // --- CONTACT BOUNDARY TRACKING ---
    suspend fun blockContact(name: String, phone: String, social: String, notes: String, days: Int) {
        val contact = ContactBlock(
            name = name,
            phone = phone,
            socialMedia = social,
            notes = notes,
            durationDays = days,
            isActive = true
        )
        dao.insertContact(contact)
    }

    suspend fun unblockContact(id: Int) {
        dao.updateContactStatus(id, false)
    }

    suspend fun deleteContact(id: Int) {
        dao.deleteContactById(id)
    }

    // --- TEMPTATION BLOCKER ---
    suspend fun logTemptation(
        contactId: Int,
        intensity: Int,
        trigger: String,
        didAct: Boolean,
        redirection: String
    ) {
        val log = TemptationLog(
            contactId = contactId,
            intensity = intensity,
            trigger = trigger,
            didAct = didAct,
            redirectionUsed = redirection
        )
        dao.insertTemptation(log)

        // If user resisted (didAct == false), check if they earn a prize/motivation!
        if (!didAct) {
            val profile = dao.getUserProfileDirect() ?: UserProfile()
            val currentResistedCount = profile.ratingScore + 1
            dao.insertUserProfile(profile.copy(ratingScore = currentResistedCount))

            // Unlock badge for self-control!
            if (currentResistedCount >= 3) {
                checkAndUnlockBadge("Iron Guard", "🛡️", "Resisted communication impulses 3 times")
            }
            if (currentResistedCount >= 10) {
                checkAndUnlockBadge("Zen Shield Master", "💎", "Demonstrated high barrier mastery and withstood intense urges")
            }
        }
    }

    suspend fun deleteTemptation(id: Int) {
        dao.deleteTemptationById(id)
    }

    // --- DIARY JOURNAL ---
    suspend fun saveJournal(
        content: String,
        mood: Int?,
        tags: List<String>,
        isPrivate: Boolean,
        isDraft: Boolean,
        sentiment: String
    ) {
        val count = content.trim().split("\\s+".toRegex()).size
        val entry = JournalEntry(
            content = content,
            moodScale = mood,
            tags = tags,
            wordCount = count,
            isPrivate = isPrivate,
            isDraft = isDraft,
            sentiment = sentiment
        )
        dao.insertJournal(entry)

        if (!isDraft) {
            val profile = dao.getUserProfileDirect() ?: UserProfile()
            val totalJournalsCount = profile.totalJournalEntries + 1
            
            // Auto streak counter updates
            val currentJournalStreak = profile.journalStreakDays + 1
            dao.insertUserProfile(
                profile.copy(
                    totalJournalEntries = totalJournalsCount,
                    journalStreakDays = currentJournalStreak
                )
            )

            // Triggers for unlocking journal badges
            if (totalJournalsCount >= 5) {
                checkAndUnlockBadge("Writer's Journey", "📔", "Written 5 journal entries successfully")
            }
            if (totalJournalsCount >= 10) {
                checkAndUnlockBadge("Reflection Master", "📚", "Cultivated strong mental awareness with 10 diaries")
            }
        }
    }

    suspend fun deleteJournal(id: Int) {
        dao.deleteJournalById(id)
    }

    // --- AFFIRMATIONS ---
    suspend fun createAffirmation(text: String, category: String, isCustom: Boolean, isFav: Boolean) {
        val aff = AffirmationLog(
            text = text,
            category = category,
            isCustom = isCustom,
            isFavorite = isFav
        )
        dao.insertAffirmation(aff)

        val profile = dao.getUserProfileDirect() ?: UserProfile()
        checkAndUnlockBadge("Affirmation Seeder", "✨", "Wrote customized affirmations of positive strength")
    }

    suspend fun toggleAffirmationFavorite(id: Int, isFav: Boolean) {
        dao.updateAffirmationFavorite(id, isFav)
    }

    suspend fun deleteAffirmation(id: Int) {
        dao.deleteAffirmationById(id)
    }

    private suspend fun populatePreloadedAffirmations() {
        val preloaded = listOf(
            AffirmationLog(text = "You are worthy of love—most importantly, love from yourself.", category = "Love"),
            AffirmationLog(text = "I am building a strong, independent version of myself.", category = "Strength"),
            AffirmationLog(text = "My pain is temporary; my strength is permanent.", category = "Healing"),
            AffirmationLog(text = "I choose myself first.", category = "Boundaries"),
            AffirmationLog(text = "Distance creates clarity; I'm choosing clarity.", category = "Clarity"),
            AffirmationLog(text = "Every day without breaking no-contact is a victory.", category = "Boundary"),
            AffirmationLog(text = "I release what I cannot control and focus on what I can.", category = "Inner Peace"),
            AffirmationLog(text = "My healing journey is uniquely mine. I honor my pace.", category = "Journey"),
            AffirmationLog(text = "I am enough, exactly as I am, right now.", category = "Self Worth"),
            AffirmationLog(text = "My boundaries are an act of self-love, not selfishness.", category = "Boundaries"),
            AffirmationLog(text = "I am learning to love the person I'm becoming.", category = "Growth"),
            AffirmationLog(text = "Healing is not linear, and that's perfectly okay.", category = "Healing"),
            AffirmationLog(text = "I choose growth over comfort.", category = "Growth"),
            AffirmationLog(text = "My worth is not determined by their opinion of me.", category = "Self Worth"),
            AffirmationLog(text = "I am strong enough to walk away from what hurts me.", category = "Strength")
        )
        for (aff in preloaded) {
            dao.insertAffirmation(aff)
        }
    }

    // --- BREATHING EXERCISES ---
    suspend fun logBreathing(technique: String, durationSecs: Int, completed: Boolean) {
        val log = BreatheLog(
            technique = technique,
            durationSeconds = durationSecs,
            completed = completed
        )
        dao.insertBreatheLog(log)

        if (completed) {
            val profile = dao.getUserProfileDirect() ?: UserProfile()
            val totalMeditationsCount = profile.totalMeditations + 1
            dao.insertUserProfile(profile.copy(totalMeditations = totalMeditationsCount))

            // Meditations milestones badge unlocking
            if (totalMeditationsCount >= 1) {
                checkAndUnlockBadge("Serene First Inhale", "🌬️", "Completed your initial healing breathing exercise")
            }
            if (totalMeditationsCount >= 10) {
                checkAndUnlockBadge("Breath Harmony Practitioner", "🌀", "Maintained oxygenated centering with 10 completed modules")
            }
        }
    }

    // --- GOAL SETTINGS & TRACKING ---
    suspend fun createGoal(name: String, description: String, category: String, frequency: String, amount: Int, duration: Int) {
        val goal = GoalLog(
            name = name,
            description = description,
            category = category,
            targetFrequency = frequency,
            targetAmount = amount,
            durationDays = duration,
            isActive = true
        )
        dao.insertGoal(goal)
    }

    suspend fun toggleGoalActiveStatus(id: Int, isActive: Boolean) {
        dao.updateGoalStatus(id, isActive)
    }

    suspend fun deleteGoal(id: Int) {
        dao.deleteGoalById(id)
    }

    suspend fun logGoalProgress(goalId: Int, dateStr: String, increment: Int) {
        // Find if progress exists for this date and goalId
        val list = dao.getProgressForGoal(goalId).firstOrNull() ?: emptyList()
        val existing = list.firstOrNull { it.dateStr == dateStr }
        if (existing != null) {
            val nextProg = existing.progress + increment
            dao.insertGoalProgress(existing.copy(progress = nextProg, lastLoggedAt = System.currentTimeMillis()))
        } else {
            val progress = GoalProgress(
                goalId = goalId,
                dateStr = dateStr,
                progress = increment
            )
            dao.insertGoalProgress(progress)
        }

        // Streak & Active day validation
        val profile = dao.getUserProfileDirect() ?: UserProfile()
        // If they checked a goal, let's increment active points!
        val nextPoints = profile.ratingScore + 1
        dao.insertUserProfile(profile.copy(ratingScore = nextPoints))

        if (nextPoints >= 10) {
            checkAndUnlockBadge("Goal Achiever Elite", "🔥", "Completed progress edits 10 times")
        }
    }

    fun getProgressByGoal(goalId: Int): Flow<List<GoalProgress>> {
        return dao.getProgressForGoal(goalId)
    }

    // --- UNLOCK BADGES HELPER ---
    private suspend fun checkAndUnlockBadge(name: String, icon: String, description: String) {
        val currentBadges = dao.getAllBadges().firstOrNull() ?: emptyList()
        val exists = currentBadges.any { it.name.equals(name, ignoreCase = true) }
        if (!exists) {
            val badge = BadgeLog(
                name = name,
                icon = icon,
                category = description
            )
            dao.insertBadge(badge)
        }
    }

    // --- WORKOUT OPERATIONS ---
    suspend fun logWorkout(type: String, durationMins: Int, exercisesCount: Int, notes: String, feedbackEmoji: String) {
        val log = WorkoutLog(
            workoutType = type,
            durationMinutes = durationMins,
            exercisesCount = exercisesCount,
            notes = notes,
            feedbackEmoji = feedbackEmoji
        )
        dao.insertWorkoutLog(log)

        val profile = dao.getUserProfileDirect() ?: UserProfile()
        val nextPoints = profile.ratingScore + 2
        val nextActiveDays = profile.totalActiveDays + 1
        dao.insertUserProfile(profile.copy(ratingScore = nextPoints, totalActiveDays = nextActiveDays))

        checkAndUnlockBadge("Primal Flow", "🏃‍♀️", "Activated physical vitality through focused bodily movement")
        if (nextPoints >= 15) {
            checkAndUnlockBadge("Vibrant Aligned", "🎯", "Accumulated high score alignment points across wellness exercises")
        }
    }

    suspend fun deleteWorkoutLog(id: Int) {
        dao.deleteWorkoutLogById(id)
    }
}

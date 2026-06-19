package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val onboarded: Boolean = false,
    val displayName: String = "Struggling Soul",
    val healingGoalTags: String = "", // comma-separated selected tags
    val selectedGoals: String = "", // comma-separated selects
    val notificationTime: String = "08:00",
    val notificationEnabled: Boolean = true,
    val themeMode: String = "system", // light, dark, system
    val fontSize: String = "normal", // small, normal, large
    val language: String = "en",
    val totalActiveDays: Int = 1,
    val journalStreakDays: Int = 0,
    val distanceStreakDays: Int = 0,
    val totalMeditations: Int = 0,
    val totalJournalEntries: Int = 0,
    val totalMoodLogs: Int = 0,
    val ratingScore: Int = 0
)

@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val scale: Int, // 1-10 representation
    val emotions: List<String>,
    val trigger: String,
    val symptoms: List<String>,
    val location: String,
    val note: String
)

@Entity(tableName = "contact_blocks")
data class ContactBlock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val socialMedia: String,
    val notes: String,
    val blockedDate: Long = System.currentTimeMillis(),
    val durationDays: Int,
    val isActive: Boolean = true
)

@Entity(tableName = "temptation_logs")
data class TemptationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val intensity: Int, // 1-10
    val trigger: String,
    val didAct: Boolean,
    val redirectionUsed: String
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val moodScale: Int? = null,
    val tags: List<String>,
    val wordCount: Int,
    val isPrivate: Boolean = true,
    val isDraft: Boolean = false,
    val sentiment: String = "neutral"
)

@Entity(tableName = "affirmations")
data class AffirmationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val category: String,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val favoriteCount: Int = 0
)

@Entity(tableName = "breathe_logs")
data class BreatheLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val technique: String, // Box, 4-7-8, Grounding
    val durationSeconds: Int,
    val completed: Boolean = true,
    val notes: String = ""
)

@Entity(tableName = "goals")
data class GoalLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val category: String,
    val targetFrequency: String, // Daily, Weekly
    val targetAmount: Int,
    val durationDays: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "goal_progress")
data class GoalProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val dateStr: String, // yyyy-MM-dd
    val progress: Int,
    val lastLoggedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "badges")
data class BadgeLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String, // emoji or vector resource string
    val unlockedAt: Long = System.currentTimeMillis(),
    val category: String
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val workoutType: String, // Restorative Yoga, Strength & Tone, Cardio Flow, HIIT Grounding, Core Alignment
    val durationMinutes: Int,
    val exercisesCount: Int,
    val notes: String = "",
    val feedbackEmoji: String = "🙂"
)

class StringListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("|||")
    }

    @TypeConverter
    fun toString(list: List<String>?): String {
        if (list.isNullOrEmpty()) return ""
        return list.joinToString("|||")
    }
}

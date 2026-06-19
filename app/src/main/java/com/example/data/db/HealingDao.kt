package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealingDao {

    // --- USER PROFILE ---
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    // --- MOOD LOGS ---
    @Query("SELECT * FROM mood_logs ORDER BY timestamp DESC")
    fun getAllMoodLogs(): Flow<List<MoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodLog(log: MoodLog)

    @Query("DELETE FROM mood_logs WHERE id = :id")
    suspend fun deleteMoodLogById(id: Int)

    // --- CONTACT BLOCKS ---
    @Query("SELECT * FROM contact_blocks ORDER BY blockedDate DESC")
    fun getAllContacts(): Flow<List<ContactBlock>>

    @Query("SELECT * FROM contact_blocks WHERE isActive = 1 ORDER BY blockedDate DESC")
    fun getActiveContacts(): Flow<List<ContactBlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactBlock)

    @Query("UPDATE contact_blocks SET isActive = :isActive WHERE id = :id")
    suspend fun updateContactStatus(id: Int, isActive: Boolean)

    @Query("DELETE FROM contact_blocks WHERE id = :id")
    suspend fun deleteContactById(id: Int)

    // --- TEMPTATIONS ---
    @Query("SELECT * FROM temptation_logs ORDER BY timestamp DESC")
    fun getAllTemptations(): Flow<List<TemptationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemptation(log: TemptationLog)

    @Query("DELETE FROM temptation_logs WHERE id = :id")
    suspend fun deleteTemptationById(id: Int)

    // --- JOURNAL ENTRIES ---
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournals(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE isDraft = 0 ORDER BY timestamp DESC")
    fun getPublishedJournals(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalById(id: Int)

    // --- AFFIRMATIONS ---
    @Query("SELECT * FROM affirmations ORDER BY isCustom DESC, id ASC")
    fun getAllAffirmations(): Flow<List<AffirmationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAffirmation(aff: AffirmationLog)

    @Query("UPDATE affirmations SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateAffirmationFavorite(id: Int, isFav: Boolean)

    @Query("DELETE FROM affirmations WHERE id = :id")
    suspend fun deleteAffirmationById(id: Int)

    // --- BREATHE LOGS ---
    @Query("SELECT * FROM breathe_logs ORDER BY timestamp DESC")
    fun getAllBreatheLogs(): Flow<List<BreatheLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreatheLog(log: BreatheLog)

    // --- GOALS ---
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalLog>>

    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveGoals(): Flow<List<GoalLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalLog)

    @Query("UPDATE goals SET isActive = :isActive WHERE id = :id")
    suspend fun updateGoalStatus(id: Int, isActive: Boolean)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Int)

    // --- GOAL PROGRESS ---
    @Query("SELECT * FROM goal_progress WHERE dateStr = :dateStr")
    fun getProgressForDate(dateStr: String): Flow<List<GoalProgress>>

    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId")
    fun getProgressForGoal(goalId: Int): Flow<List<GoalProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalProgress(progress: GoalProgress)

    // --- BADGES ---
    @Query("SELECT * FROM badges ORDER BY unlockedAt DESC")
    fun getAllBadges(): Flow<List<BadgeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeLog)

    // --- WORKOUT LOGS ---
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLog)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteWorkoutLogById(id: Int)
}

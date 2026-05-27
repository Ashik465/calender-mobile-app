package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Int): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)
}

@Dao
interface HabitProgressDao {
    @Query("SELECT * FROM habit_progress WHERE dateString = :dateString")
    fun getProgress量ForDate(dateString: String): Flow<List<HabitProgress>>

    @Query("SELECT * FROM habit_progress")
    fun getAllProgress(): Flow<List<HabitProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: HabitProgress)

    @Query("DELETE FROM habit_progress WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteProgress(habitId: Int, dateString: String)

    @Query("DELETE FROM habit_progress WHERE habitId = :habitId")
    suspend fun deleteProgressForHabit(habitId: Int)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY priority DESC, id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDateString = :dateString ORDER BY orderInSlot ASC, id DESC")
    fun getTasksByDate(dateString: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY startTimestamp ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE dateString = :dateString ORDER BY startTimestamp ASC")
    fun getEventsByDate(dateString: String): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("DELETE FROM calendar_events WHERE isFromGoogle = 1")
    suspend fun clearGoogleEvents()
}

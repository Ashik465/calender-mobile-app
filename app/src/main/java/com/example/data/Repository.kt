package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class KokoroRepository(private val database: AppDatabase) {

    private val habitDao = database.habitDao()
    private val habitProgressDao = database.habitProgressDao()
    private val taskDao = database.taskDao()
    private val calendarEventDao = database.calendarEventDao()

    // Habits
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allHabitProgress: Flow<List<HabitProgress>> = habitProgressDao.getAllProgress()

    fun getProgressForDate(dateString: String): Flow<List<HabitProgress>> {
        return habitProgressDao.getProgress量ForDate(dateString)
    }

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) {
        // Also delete progress
        habitProgressDao.deleteProgressForHabit(habit.id)
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabitCompletion(habitId: Int, dateString: String) {
        val allProgress = database.habitProgressDao().getAllProgress()
        // Simple search (can be optimized but perfectly fine for offline app)
        val progressList = mutableListOf<HabitProgress>()
        // We will query if completion exists
        var foundExisted = false
        // Fetch snapshot
        val databaseSnapshot = database.openHelper.writableDatabase
        // For simplicity we do it via DAO checking state
        // Let's check using some basic suspend query
    }

    suspend fun completeHabit(habitId: Int, dateString: String) {
        habitProgressDao.insertProgress(HabitProgress(habitId = habitId, dateString = dateString, completed = true))
        // Update streak
        habitDao.getHabitById(habitId)?.let { habit ->
            val newStreak = habit.streak + 1
            val best = if (newStreak > habit.bestStreak) newStreak else habit.bestStreak
            habitDao.updateHabit(habit.copy(streak = newStreak, bestStreak = best))
        }
    }

    suspend fun uncompleteHabit(habitId: Int, dateString: String) {
        habitProgressDao.deleteProgress(habitId, dateString)
        // Update streak
        habitDao.getHabitById(habitId)?.let { habit ->
            val newStreak = if (habit.streak > 0) habit.streak - 1 else 0
            habitDao.updateHabit(habit.copy(streak = newStreak))
        }
    }

    // Tasks
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    fun getTasksByDate(dateString: String): Flow<List<Task>> = taskDao.getTasksByDate(dateString)

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    // Calendar Events
    val allCalendarEvents: Flow<List<CalendarEvent>> = calendarEventDao.getAllEvents()
    fun getEventsByDate(dateString: String): Flow<List<CalendarEvent>> = calendarEventDao.getEventsByDate(dateString)

    suspend fun insertEvent(event: CalendarEvent) = calendarEventDao.insertEvent(event)
    suspend fun deleteEvent(event: CalendarEvent) = calendarEventDao.deleteEvent(event)

    // Prepopulate some interactive data so it starts with cozy, anime styled content
    suspend fun prepopulateDefaultDataIfEmpty() {
        // We can check if habits are empty
        // Since we are running on flow, we can check via simple query or just populate
    }

    // Simulate Google Calendar integration with cute anime activities!
    suspend fun syncWithGoogleCalendar(email: String, selectDate: String) {
        calendarEventDao.clearGoogleEvents()
        
        // Let's create gorgeous mock events around the selected date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val centerDate = try {
            sdf.parse(selectDate) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        val calendar = Calendar.getInstance()
        calendar.time = centerDate

        // Set hours for a few beautiful scheduled GCal events
        val mockData = listOf(
            Triple("Matcha Tea Ceremony 🍵", "Morning meditation and tasting premium matcha tea.", 9),
            Triple("Sketching Anime Character Draft 🎨", "Drafting an original magical girl concept in sketchpad.", 11),
            Triple("Japanese Grammar Study 🇯🇵", "Reviewing N3 grammar: causative passive and Honorifics.", 14),
            Triple("Lo-fi Beats Listening & Coding 💻", "Coding beautiful JetCompose interfaces on tablet.", 16),
            Triple("Cozy Ramen Dinner w/ Friends 🍜", "Enjoying Spicy Shoyu Raman at Ichiraku Shop.", 19)
        )

        for ((title, desc, hour) in mockData) {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, 0)
            val start = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, hour + 1)
            val end = calendar.timeInMillis

            val event = CalendarEvent(
                id = "gcal_${hour}_${selectDate}",
                title = title,
                description = desc,
                startTimestamp = start,
                endTimestamp = end,
                isFromGoogle = true,
                dateString = selectDate
            )
            calendarEventDao.insertEvent(event)
        }
    }
}

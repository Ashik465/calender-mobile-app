package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class KokoroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KokoroRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = KokoroRepository(database)
        
        // Populate default data on start
        viewModelScope.launch {
            repository.allHabits.first().let { habits ->
                if (habits.isEmpty()) {
                    // Create default habits
                    val defaults = listOf(
                        Habit(name = "Drink Matcha Water", icon = "🍵", colorHex = "#98FF98", streak = 3, bestStreak = 5),
                        Habit(name = "Read Manga / Novel", icon = "📖", colorHex = "#FFD1DC", streak = 5, bestStreak = 12),
                        Habit(name = "Japanese Grammar 🇯🇵", icon = "🌸", colorHex = "#E6E6FA", streak = 1, bestStreak = 3),
                        Habit(name = "Drawing Anime Sketch", icon = "🎨", colorHex = "#FFE5B4", streak = 2, bestStreak = 4)
                    )
                    defaults.forEach { repository.insertHabit(it) }
                }
            }

            repository.allTasks.first().let { tasks ->
                if (tasks.isEmpty()) {
                    val todayStr = getTodayDateString()
                    val defaultTasks = listOf(
                        Task(title = "Plan Sakura Picnic 🌸", description = "Draft invitations and location map for the group.", category = "Hobby", priority = "LOW", dueDateString = todayStr, timeSlotHour = 10),
                        Task(title = "Buy Vol 5 of Chainsaw Man", description = "Check local anime Bookstore for special covers.", category = "General", priority = "MEDIUM", dueDateString = todayStr, timeSlotHour = 15),
                        Task(title = "Submit Coding Project", description = "Finish Jetpack Compose layout for the widgets.", category = "Study", priority = "HIGH", dueDateString = todayStr, timeSlotHour = 18)
                    )
                    defaultTasks.forEach { repository.insertTask(it) }
                }
            }
        }
    }

    // Selected Date
    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun selectDate(dateString: String) {
        _selectedDate.value = dateString
    }

    // User Authentication State (Simulated Firebase)
    private val _isAuthenticated = MutableStateFlow(true) // Start authenticated with the User Email!
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _userEmail = MutableStateFlow("helloashikul@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    fun signIn(email: String) {
        viewModelScope.launch {
            _userEmail.value = email
            _isAuthenticated.value = true
        }
    }

    fun signOut() {
        _isAuthenticated.value = false
        _userEmail.value = ""
    }

    // Theme (Light/Blossom vs Dark/Tokyo)
    private val _isDarkMode = MutableStateFlow(true) // Start dark to match Tokyo Cyberpunk
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _animeTheme = MutableStateFlow("MIDNIGHT_BASS")
    val animeTheme: StateFlow<String> = _animeTheme.asStateFlow()

    fun setAnimeTheme(themeName: String) {
        _animeTheme.value = themeName
        _isDarkMode.value = (themeName == "MIDNIGHT_BASS" || themeName == "RETRO_VINYL")
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
        if (_isDarkMode.value) {
            _animeTheme.value = "MIDNIGHT_BASS"
        } else {
            _animeTheme.value = "COZY_CAFE"
        }
    }

    // Calendar Integration States
    private val _isGoogleCalendarSynced = MutableStateFlow(false)
    val isGoogleCalendarSynced: StateFlow<Boolean> = _isGoogleCalendarSynced.asStateFlow()

    private val _syncingState = MutableStateFlow(false)
    val syncingState: StateFlow<Boolean> = _syncingState.asStateFlow()

    fun syncGoogleCalendar() {
        viewModelScope.launch {
            _syncingState.value = true
            // Real looking sync delay!
            kotlinx.coroutines.delay(1800)
            repository.syncWithGoogleCalendar(_userEmail.value, _selectedDate.value)
            _isGoogleCalendarSynced.value = true
            _syncingState.value = false
        }
    }

    fun unsyncGoogleCalendar() {
        viewModelScope.launch {
            _isGoogleCalendarSynced.value = false
            // Clear google events from db
            AppDatabase.getDatabase(getApplication()).calendarEventDao().clearGoogleEvents()
        }
    }

    // Google Workspace App Connectivity States
    private val _isGmailConnected = MutableStateFlow(false)
    val isGmailConnected: StateFlow<Boolean> = _isGmailConnected.asStateFlow()

    private val _isDriveConnected = MutableStateFlow(false)
    val isDriveConnected: StateFlow<Boolean> = _isDriveConnected.asStateFlow()

    private val _isTasksConnected = MutableStateFlow(false)
    val isTasksConnected: StateFlow<Boolean> = _isTasksConnected.asStateFlow()

    private val _isKeepConnected = MutableStateFlow(false)
    val isKeepConnected: StateFlow<Boolean> = _isKeepConnected.asStateFlow()

    private val _isMeetConnected = MutableStateFlow(false)
    val isMeetConnected: StateFlow<Boolean> = _isMeetConnected.asStateFlow()

    fun toggleGmailConnection() {
        viewModelScope.launch {
            val next = !_isGmailConnected.value
            _isGmailConnected.value = next
            if (next) {
                repository.insertTask(Task(
                    title = "📨 [Gmail] Reply to Sensei about Sakura picnic",
                    description = "Urgent unread mail thread re: location coordinates and food list.",
                    category = "Study",
                    priority = "HIGH",
                    dueDateString = _selectedDate.value,
                    timeSlotHour = 9
                ))
            }
        }
    }

    fun toggleDriveConnection() {
        _isDriveConnected.value = !_isDriveConnected.value
    }

    fun toggleTasksConnection() {
        viewModelScope.launch {
            val next = !_isTasksConnected.value
            _isTasksConnected.value = next
            if (next) {
                repository.insertTask(Task(
                    title = "📋 [Google Tasks] Buy Vol 6 of Chainsaw Man",
                    description = "Imported single task item from Google Tasks mobile app.",
                    category = "General",
                    priority = "MEDIUM",
                    dueDateString = _selectedDate.value,
                    timeSlotHour = 14
                ))
            }
        }
    }

    fun toggleKeepConnection() {
        viewModelScope.launch {
            val next = !_isKeepConnected.value
            _isKeepConnected.value = next
            if (next) {
                repository.insertHabit(Habit(
                    name = "📓 Sync Keep doodle notes",
                    icon = "📓",
                    colorHex = "#FFE5B4",
                    streak = 1,
                    bestStreak = 4
                ))
            }
        }
    }

    fun toggleMeetConnection() {
        _isMeetConnected.value = !_isMeetConnected.value
    }

    // Stream list of habits with reactive progress tracking!
    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitProgress: StateFlow<List<HabitProgress>> = repository.allHabitProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter tasks & events corresponding to the current active date reactively!
    val tasksForSelectedDate: StateFlow<List<Task>> = _selectedDate
        .flatMapLatest { date -> repository.getTasksByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventsForSelectedDate: StateFlow<List<CalendarEvent>> = _selectedDate
        .flatMapLatest { date -> repository.getEventsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // CRITICAL USER ACTION: Add, Edit, Delete Habits
    fun addNewHabit(name: String, icon: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertHabit(Habit(name = name, icon = icon, colorHex = colorHex))
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleHabitCompletion(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value
            if (isCompleted) {
                repository.completeHabit(habitId, dateStr)
            } else {
                repository.uncompleteHabit(habitId, dateStr)
            }
        }
    }

    // CRITICAL USER ACTION: Add, Edit, Drag-and-Drop / Reschedule Tasks
    fun addNewTask(title: String, description: String, category: String, priority: String, timeSlotHour: Int = -1) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                category = category,
                priority = priority,
                dueDateString = _selectedDate.value,
                timeSlotHour = timeSlotHour
            )
            repository.insertTask(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun rescheduleTask(task: Task, newHour: Int) {
        viewModelScope.launch {
            repository.updateTask(task.copy(timeSlotHour = newHour))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Quick customizable widgets styling state (for priority display inside widget preview)
    private val _widgetColorTheme = MutableStateFlow("#FFD1DC") // Blossom Pink default
    val widgetColorTheme: StateFlow<String> = _widgetColorTheme.asStateFlow()

    private val _widgetSelectedCategory = MutableStateFlow("All")
    val widgetSelectedCategory: StateFlow<String> = _widgetSelectedCategory.asStateFlow()

    fun updateWidgetCustomization(colorHex: String, category: String) {
        _widgetColorTheme.value = colorHex
        _widgetSelectedCategory.value = category
    }

    // Standard static helper
    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}

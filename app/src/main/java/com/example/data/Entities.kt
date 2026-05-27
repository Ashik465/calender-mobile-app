package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val frequency: String = "DAILY", // DAILY, WEEKLY
    val icon: String = "⭐️", // emoji/icon tag
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val colorHex: String = "#FFD1DC", // Kawaii Pink as default
    val targetDaysPerWeek: Int = 7
)

@Entity(tableName = "habit_progress")
data class HabitProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val dateString: String, // YYYY-MM-DD
    val completed: Boolean = true
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val dueDateString: String = "", // YYYY-MM-DD
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val category: String = "General", // Work, Habit, Study, Hobby
    val recurring: String = "NONE", // NONE, DAILY, WEEKLY
    val timeSlotHour: Int = -1, // -1 means unscheduled, 0-23 represents scheduled hour
    val orderInSlot: Int = 0,
    val reminderMinutes: Int = 0 // minutes before
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey val id: String, // Google Cal ID or Local ID
    val title: String,
    val description: String = "",
    val startTimestamp: Long,
    val endTimestamp: Long,
    val isFromGoogle: Boolean = false,
    val dateString: String // YYYY-MM-DD
)

package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.R
import com.example.data.CalendarEvent
import com.example.data.Habit
import com.example.data.Task
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    viewModel: KokoroViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val animeTheme by viewModel.animeTheme.collectAsStateWithLifecycle()
    val authState by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val isSynced by viewModel.isGoogleCalendarSynced.collectAsStateWithLifecycle()
    val isSyncing by viewModel.syncingState.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Calendar + Sync, 1: Habits, 2: Analytics, 3: Widget

    // Anime Styling variables based on light/dark mode
    val mainBackground = MaterialTheme.colorScheme.background
    val bubbleCardBorder = if (isDark) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
    } else {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mainBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // High-fidelity Japan Aesthetics Top Banner
            HeaderBannerSection(
                isDark = isDark,
                onToggleDark = { viewModel.toggleDarkMode() },
                authState = authState,
                userEmail = userEmail,
                onLogInSimulation = { viewModel.signIn("helloashikul@gmail.com") },
                onLogOutSimulation = { viewModel.signOut() }
            )

            // Dynamic Anime Master Theme picker strip
            AnimeThemeSelector(
                activeTheme = animeTheme,
                onThemeSelected = { viewModel.setAnimeTheme(it) }
            )

            // Date strip controller
            CalendarStripSection(
                selectedDate = selectedDate,
                onDateSelected = { viewModel.selectDate(it) },
                isDark = isDark
            )

            // Dynamic bottom nav indicator tabs with kawaii custom badges
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("今日 Schedule", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Schedule") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Habits ☘️", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Habits") }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Analytics 📊", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("Kokoro Widget", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Layers, contentDescription = "Widget") }
                )
            }

            // Screen content transition
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (activeTab) {
                    0 -> CalendarScheduleView(
                        viewModel = viewModel,
                        isDark = isDark,
                        bubbleCardBorder = bubbleCardBorder,
                        isSynced = isSynced,
                        isSyncing = isSyncing,
                        selectedDate = selectedDate
                    )
                    1 -> HabitTrackerView(
                        viewModel = viewModel,
                        isDark = isDark,
                        bubbleCardBorder = bubbleCardBorder
                    )
                    2 -> ProductivityAnalyticsView(
                        viewModel = viewModel,
                        isDark = isDark,
                        bubbleCardBorder = bubbleCardBorder
                    )
                    3 -> CustomizableWidgetView(
                        viewModel = viewModel,
                        isDark = isDark,
                        bubbleCardBorder = bubbleCardBorder
                    )
                }
            }
        }
    }
}

// ==================== HEADER BAR ====================
@Composable
fun HeaderBannerSection(
    isDark: Boolean,
    onToggleDark: () -> Unit,
    authState: Boolean,
    userEmail: String,
    onLogInSimulation: () -> Unit,
    onLogOutSimulation: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        // Render generated high-quality Ghibli desk banner as background image!
        Image(
            painter = painterResource(id = R.drawable.img_anime_banner_1779909007170),
            contentDescription = "Anime background cozy desk style",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient overlay for readability
        val colorOverlay = if (isDark) {
            Brush.verticalGradient(listOf(Color(0xE00D0D12), Color(0xAA0D0D12)))
        } else {
            Brush.verticalGradient(listOf(Color(0xBBFFF6F7), Color(0x66FFF6F7)))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorOverlay)
        )

        // Foreground headers controls
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isDark) "クロノス・同期" else "こころ・同期",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF00F5FF) else Color(0xFFFF4E8D),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = "Kokoro Cal",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color.White else Color(0xFFFF4E8D),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
                Text(
                    text = "Artistic Anime Planner • Active Sync",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(6.dp))
                // Simulated Sync Authentication status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (authState) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                        contentDescription = "Sync",
                        tint = if (authState) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (authState) "Synced: $userEmail" else "Offline-only Mode",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Quick Toggle Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // AuthService Mock Login Button
                IconButton(
                    onClick = {
                        if (authState) onLogOutSimulation() else onLogInSimulation()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (authState) Icons.Default.PowerSettingsNew else Icons.Default.LockOpen,
                        contentDescription = "User authentication toggle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Light/Dark mode selector button
                IconButton(
                    onClick = onToggleDark,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Dark mode toggle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==================== ANIME THEME SELECTOR ====================
@Composable
fun AnimeThemeSelector(
    activeTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        "TOKYO_NIGHT" to ("🌃 Tokyo Cyber" to Color(0xFFFF4E8D)),
        "SAKURA_BLOSSOM" to ("🌸 Sakura Bloom" to Color(0xFFFF8DA1)),
        "MIKU_TEAL" to ("🎤 Miku Teal" to Color(0xFF00E5FF)),
        "SUNSHINE_ORANGE" to ("🦊 Sunny Naruto" to Color(0xFFFF7E00)),
        "EVA_UNIT_01" to ("🤖 Neon Eva" to Color(0xFF9400D3))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "🎨 SELECT ANIME THEME",
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            themes.forEach { (key, info) ->
                val (label, tint) = info
                val isSelected = activeTheme == key
                Card(
                    modifier = Modifier
                        .clickable { onThemeSelected(key) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) tint.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) tint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(tint, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ==================== CALENDAR WEEK STRIP ====================
@Composable
fun CalendarStripSection(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    isDark: Boolean
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatDay = SimpleDateFormat("dd", Locale.getDefault())
    val formatDayOfWeek = SimpleDateFormat("EEE", Locale.getDefault())
    val formatMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val currentDate = remember(selectedDate) {
        try { sdf.parse(selectedDate) } catch(e: Exception) { Date() } ?: Date()
    }

    var isMonthView by remember { mutableStateOf(true) }

    // Generate current week dates
    val datesOfCurrentWeek = remember {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek) // Go to start of this week
        for (i in 0..6) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    val cal = Calendar.getInstance()
    cal.time = currentDate
    val currentMonthIdx = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)

    // Generate month days list (including padded empty slots)
    val monthDays = remember(currentMonthIdx, currentYear) {
        val list = mutableListOf<Date?>()
        val tempCal = Calendar.getInstance()
        tempCal.set(Calendar.YEAR, currentYear)
        tempCal.set(Calendar.MONTH, currentMonthIdx)
        tempCal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
        // Pad days before the first day of the month
        for (i in 1 until firstDayOfWeek) {
            list.add(null)
        }

        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysInMonth) {
            list.add(tempCal.time.clone() as Date)
            tempCal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isMonthView = !isMonthView }
            ) {
                Text(
                    text = if (isMonthView) "📅 Whole Month Grid" else "🌸 Weekly Stream",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isMonthView) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand month calendar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "${formatMonthYear.format(currentDate)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isMonthView) {
            // Render beautiful whole month calendar grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // S M T W T F S headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { wd ->
                            Text(
                                text = wd,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val rows = monthDays.chunked(7)
                    rows.forEach { rowDays ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowDays.forEach { dayDate ->
                                if (dayDate == null) {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1.2f).padding(2.dp))
                                } else {
                                    val dateStr = sdf.format(dayDate)
                                    val isSelected = dateStr == selectedDate
                                    val isToday = sdf.format(Date()) == dateStr
                                    val dayNum = formatDay.format(dayDate)

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.2f)
                                            .padding(2.dp)
                                            .clickable { onDateSelected(dateStr) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else if (isToday) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            } else {
                                                Color.Transparent
                                            }
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary 
                                            else if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
                                            else Color.Transparent
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNum,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowDays.size < 7) {
                                for (i in 0 until (7 - rowDays.size)) {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1.2f).padding(2.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Render standard single row week strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                datesOfCurrentWeek.forEach { dateStr ->
                    val date = sdf.parse(dateStr) ?: Date()
                    val isSelected = dateStr == selectedDate
                    val dayNum = formatDay.format(date)
                    val dayOfWeek = formatDayOfWeek.format(date).take(3)

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 3.dp)
                            .clickable { onDateSelected(dateStr) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayOfWeek,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayNum,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 0: DAILY SCHEDULE SCHEDULE VIEW ====================
@Composable
fun CalendarScheduleView(
    viewModel: KokoroViewModel,
    isDark: Boolean,
    bubbleCardBorder: BorderStroke,
    isSynced: Boolean,
    isSyncing: Boolean,
    selectedDate: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tasks by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val events by viewModel.eventsForSelectedDate.collectAsStateWithLifecycle()

    val isGmailConnected by viewModel.isGmailConnected.collectAsStateWithLifecycle()
    val isDriveConnected by viewModel.isDriveConnected.collectAsStateWithLifecycle()
    val isTasksConnected by viewModel.isTasksConnected.collectAsStateWithLifecycle()
    val isKeepConnected by viewModel.isKeepConnected.collectAsStateWithLifecycle()
    val isMeetConnected by viewModel.isMeetConnected.collectAsStateWithLifecycle()

    val loadingStates = remember { mutableStateMapOf<String, Boolean>() }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var clickedHourToAdd by remember { mutableStateOf(-1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Google Workspace Ecosystem Hub
        item {
            var showWorkspaceIntegrations by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = bubbleCardBorder,
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF13122B) else Color(0xFFFFECEF)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF4285F4).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "Google",
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Cloud Synchronizer",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isSynced || isGmailConnected || isDriveConnected || isTasksConnected) "Active connections synchronized offline" else "Select Workspace Apps below to bridge your schedule",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(onClick = { showWorkspaceIntegrations = !showWorkspaceIntegrations }) {
                            Icon(
                                imageVector = if (showWorkspaceIntegrations) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle integrations panel",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Main Google Calendar integration
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Calendar Client",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isSynced) "Imported timeslots into timeline" else "Sync timeslots & calendar events",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Button(
                                onClick = {
                                    if (isSynced) {
                                        viewModel.unsyncGoogleCalendar()
                                        Toast.makeText(context, "Cleared Google Calendar Cache!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.syncGoogleCalendar()
                                        Toast.makeText(context, "Google Calendar Cache Synced!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isSynced) "Disconnect" else "Sync Now",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Expanded Subsection of other Google apps (Gmail, Drive, Tasks, Keep, Meet)
                    AnimatedVisibility(visible = showWorkspaceIntegrations || isGmailConnected || isDriveConnected || isTasksConnected || isKeepConnected || isMeetConnected) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "CONNECT GOOGLE WORKSPACE APPS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Grid or vertical list of other Workspace platforms
                            val itemsList = listOf(
                                Triple("Gmail", "Bridge unread threads as task items", isGmailConnected),
                                Triple("Drive", "Link project assets & file folders", isDriveConnected),
                                Triple("Tasks", "Bi-directional personal checkboxes sync", isTasksConnected),
                                Triple("Keep", "Import visual stickies and doodles", isKeepConnected),
                                Triple("Meet", "Auto-insert meeting links to timelines", isMeetConnected)
                            )

                            itemsList.forEach { (name, subtitle, isConnected) ->
                                val isLoadingAppConnection = loadingStates[name] == true

                                val iconColor = when (name) {
                                    "Gmail" -> Color(0xFFEA4335)
                                    "Drive" -> Color(0xFFFBBC05)
                                    "Tasks" -> Color(0xFF4285F4)
                                    "Keep" -> Color(0xFFFFBB00)
                                    else -> Color(0xFF34A853) // Google Meet
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(iconColor.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (name) {
                                                "Gmail" -> Icons.Default.Email
                                                "Drive" -> Icons.Default.FolderOpen
                                                "Tasks" -> Icons.Default.ListAlt
                                                "Keep" -> Icons.Default.Edit
                                                else -> Icons.Default.Videocam
                                            },
                                            contentDescription = name,
                                            tint = iconColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Google $name Link",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = subtitle,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isLoadingAppConnection) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = iconColor,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        TextButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    loadingStates[name] = true
                                                    kotlinx.coroutines.delay(800)
                                                    loadingStates[name] = false
                                                    when (name) {
                                                        "Gmail" -> {
                                                            viewModel.toggleGmailConnection()
                                                            Toast.makeText(context, if (!isGmailConnected) "Successfully integrated Gmail inbox items!" else "Disconnected Gmail", Toast.LENGTH_SHORT).show()
                                                        }
                                                        "Drive" -> {
                                                            viewModel.toggleDriveConnection()
                                                            Toast.makeText(context, if (!isDriveConnected) "Linked Google Drive repository asset-vault!" else "Disconnected Drive Assets", Toast.LENGTH_SHORT).show()
                                                        }
                                                        "Tasks" -> {
                                                            viewModel.toggleTasksConnection()
                                                            Toast.makeText(context, if (!isTasksConnected) "Google Tasks synchronized with Priority board!" else "Disconnected Google Tasks", Toast.LENGTH_SHORT).show()
                                                        }
                                                        "Keep" -> {
                                                            viewModel.toggleKeepConnection()
                                                            Toast.makeText(context, if (!isKeepConnected) "Google Keep digital note boards synchronized!" else "Disconnected Google Keep", Toast.LENGTH_SHORT).show()
                                                        }
                                                        "Meet" -> {
                                                            viewModel.toggleMeetConnection()
                                                            Toast.makeText(context, if (!isMeetConnected) "Google Meet digital video camera links enabled!" else "Disabled Meet links", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = if (isConnected) MaterialTheme.colorScheme.error else iconColor
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text(
                                                text = if (isConnected) "Disconnect" else "Connect Link",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Anime Helper Mascot status block
        item {
            MascotReactionSection(tasks = tasks, isDark = isDark, bubbleCardBorder = bubbleCardBorder)
        }

        // Timeline Schedule header with additive priority
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏰ Hour Planner Timeline",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = {
                        clickedHourToAdd = -1
                        showAddTaskDialog = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Schedule", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Scheduled Slots: from 8:00 AM to 8:00 PM for easy timeline overview!
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20).forEach { hour ->
                    val hourLabel = if (hour > 12) "${hour - 12}:00 PM" else "$hour:00 AM"

                    // Find tasks scheduled to this hour
                    val tasksInHour = tasks.filter { it.timeSlotHour == hour }
                    // Find synced Google Calendar events
                    val eventsInHour = events.filter {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = it.startTimestamp
                        cal.get(Calendar.HOUR_OF_DAY) == hour
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour indicator label
                        Text(
                            text = hourLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(76.dp)
                        )

                        // Vertical Timeline connector bar drawer
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Box holding Cards or Empty slots
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        ) {
                            if (tasksInHour.isEmpty() && eventsInHour.isEmpty()) {
                                // Dynamic Tap-to-Schedule placeholder card
                                EmptySlotPlaceholder(
                                    isDark = isDark,
                                    onAddClick = {
                                        clickedHourToAdd = hour
                                        showAddTaskDialog = true
                                    }
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    eventsInHour.forEach { event ->
                                        GoogleEventCard(event = event, isDark = isDark)
                                    }

                                    // Display task schedules with drag-simulating controller arrows
                                    tasksInHour.forEach { task ->
                                        ScheduledTaskCard(
                                            task = task,
                                            isDark = isDark,
                                            isMeetConnected = isMeetConnected,
                                            isDriveConnected = isDriveConnected,
                                            onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                                            onMoveHour = { newHr -> viewModel.rescheduleTask(task, newHr) },
                                            onDeleteTask = { viewModel.deleteTask(it) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Unscheduled Task priorities zone if there's any
        val unscheduledTasks = tasks.filter { it.timeSlotHour == -1 }
        if (unscheduledTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "📦 Unscheduled Priorities (Backlog)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(unscheduledTasks) { task ->
                UnscheduledTaskCard(
                    task = task,
                    isDark = isDark,
                    isMeetConnected = isMeetConnected,
                    isDriveConnected = isDriveConnected,
                    onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                    onScheduleToHour = { hour -> viewModel.rescheduleTask(task, hour) },
                    onDeleteTask = { viewModel.deleteTask(it) }
                )
            }
        }
    }

    // Add Task Sheet
    if (showAddTaskDialog) {
        AddTaskDialog(
            defaultHour = clickedHourToAdd,
            onDismiss = { showAddTaskDialog = false },
            onTaskAdded = { title, desc, cat, prio, hr ->
                viewModel.addNewTask(title, desc, cat, prio, hr)
                showAddTaskDialog = false
            }
        )
    }
}

// ==================== COMPOSE SUBCOMPONENTS FOR TAB 0 ====================
@Composable
fun EmptySlotPlaceholder(isDark: Boolean, onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onAddClick() }
            .background(if (isDark) Color(0xFF161533) else Color(0xFFFAF2F4)),
        contentAlignment = Alignment.CenterStart
    ) {
        // Dotted Canvas design
        val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = color,
                size = size,
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
        }

        Row(
            modifier = Modifier.padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AddCircleOutline,
                contentDescription = "Tap to schedule",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tap to Schedule Slot",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun GoogleEventCard(event: CalendarEvent, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF3B2A22) else Color(0xFFFFF2D9)
        ),
        border = BorderStroke(1.dp, Color(0xFFFFA500).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.GppGood,
                contentDescription = "Google Sync verified",
                tint = Color(0xFFFFA500),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (event.description.isNotEmpty()) {
                    Text(
                        text = event.description,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            Text(
                text = "Google Cal",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFA500)
            )
        }
    }
}

@Composable
fun ScheduledTaskCard(
    task: Task,
    isDark: Boolean,
    isMeetConnected: Boolean = false,
    isDriveConnected: Boolean = false,
    onToggleComplete: (Task) -> Unit,
    onMoveHour: (Int) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    val containerColor = if (task.isCompleted) {
        if (isDark) Color(0xFF172C24) else Color(0xFFDFF0E8)
    } else {
        when (task.priority) {
            "HIGH" -> if (isDark) Color(0xFF2E1B1D) else Color(0xFFFFEBEE)
            "MEDIUM" -> if (isDark) Color(0xFF2E261B) else Color(0xFFFFF3E0)
            else -> if (isDark) Color(0xFF1B2E33) else Color(0xFFE0F7FA)
        }
    }

    val borderColor = when (task.priority) {
        "HIGH" -> Color.Red.copy(alpha = 0.6f)
        "MEDIUM" -> Color(0xFFFFA500).copy(alpha = 0.6f)
        else -> Color.Cyan.copy(alpha = 0.6f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox completion toggle
            IconButton(
                onClick = { onToggleComplete(task) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Complete task indicator",
                    tint = if (task.isCompleted) Color(0xFF2E7D32) else borderColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                // Row showing category and priority tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = task.category,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Priority: ${task.priority}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = borderColor
                    )
                }

                if (isMeetConnected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFF34A853).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Meet link",
                            tint = Color(0xFF34A853),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Meet Space Ready • Click to Join Call",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34A853)
                        )
                    }
                }

                if (isDriveConnected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFBBC05).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Drive link",
                            tint = Color(0xFFFBBC05),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Linked Drive folder: workspace_assets_v2",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBC05)
                        )
                    }
                }
            }

            // Quick Drag-and-Drop scheduling controllers: Move forward or backward in hour timeline!
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    IconButton(
                        onClick = { if (task.timeSlotHour > 8) onMoveHour(task.timeSlotHour - 1) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Schedule hour earlier", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { if (task.timeSlotHour < 20) onMoveHour(task.timeSlotHour + 1) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Schedule hour later", modifier = Modifier.size(16.dp))
                    }
                }
                IconButton(
                    onClick = { onDeleteTask(task) },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete task schedule", tint = Color.Red.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
fun UnscheduledTaskCard(
    task: Task,
    isDark: Boolean,
    isMeetConnected: Boolean = false,
    isDriveConnected: Boolean = false,
    onToggleComplete: (Task) -> Unit,
    onScheduleToHour: (Int) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    var expandedSchedule by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF131526) else Color(0xFFFFF2FD)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onToggleComplete(task) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Complete task indicator",
                        tint = if (task.isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                    Text(
                        text = "Category: ${task.category} • Private Recurrence",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { expandedSchedule = !expandedSchedule },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Schedule", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { onDeleteTask(task) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                    }
                }
            }

            if (isMeetConnected) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF34A853).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Meet Link",
                        tint = Color(0xFF34A853),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Meet session configured dynamically",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34A853)
                    )
                }
            }

            if (isDriveConnected) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFFBBC05).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Drive Link",
                        tint = Color(0xFFFBBC05),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Google Drive reference attached",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBC05)
                    )
                }
            }

            // Expanded dropdown selector panel to simulate quick drag allocation
            AnimatedVisibility(visible = expandedSchedule) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text("Select Time Slot to Drag task in:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items((8..20).toList()) { hour ->
                            Button(
                                onClick = {
                                    onScheduleToHour(hour)
                                    expandedSchedule = false
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(26.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                val hrLbl = if (hour > 12) "${hour - 12} PM" else "$hour AM"
                                Text(hrLbl, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Add new task timeline model
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    defaultHour: Int,
    onDismiss: () -> Unit,
    onTaskAdded: (String, String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var scheduledHour by remember { mutableStateOf(if (defaultHour != -1) defaultHour.toString() else "") }
    var recurring by remember { mutableStateOf("NONE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🌸 New Kokoro Task", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category select radio row
                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Work", "Study", "Hobby", "General").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                // Priority select radio row
                Text("Priority:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("HIGH", "MEDIUM", "LOW").forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio, fontSize = 10.sp) }
                        )
                    }
                }

                // Recurrence option
                Text("Recurrence (Offline Alerting):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("NONE", "DAILY", "WEEKLY").forEach { rec ->
                        FilterChip(
                            selected = recurring == rec,
                            onClick = { recurring = rec },
                            label = { Text(rec, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = scheduledHour,
                    onValueChange = { scheduledHour = it },
                    label = { Text("Hour slot (e.g. 8-20, blank for unscheduled)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        val hr = scheduledHour.toIntOrNull() ?: -1
                        onTaskAdded(title, desc, category, priority, hr)
                    }
                }
            ) {
                Text("Simulate Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}

// Anime Mascot state reactive card
@Composable
fun MascotReactionSection(tasks: List<Task>, isDark: Boolean, bubbleCardBorder: BorderStroke) {
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val completionRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val speechBubbleText = when {
        totalCount == 0 -> "Okaeri! Click the 'New Schedule' to add priorities and begin! (✿˵•́ ᴗ •̀˵)"
        completionRatio == 1f -> "Yatta! You completed everything today! Splendid work Master! ฅ(≈>ܫ<≈)ฅ"
        completionRatio >= 0.5f -> "Sugoi! You are doing amazing! Ganbare, keep going! ٩(ˊᗜˋ*)و"
        else -> "Ready to check some priorities, Master? Let's take it task by task! (~ ˘▾˘)~"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = bubbleCardBorder,
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1B1B3D) else Color(0xFFFFF0F3)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mascot illustration (Render the chibi cat mascot generated previously!)
            Image(
                painter = painterResource(id = R.drawable.img_anime_mascot_1779909024101),
                contentDescription = "Kokoro App mascot interactive chibi helper",
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Speech Bubble UI design
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "Kokoro Mascot (Chibi helper)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = speechBubbleText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==================== TAB 1: HABIT TRACKER BUBBLE LIST ====================
@Composable
fun HabitTrackerView(
    viewModel: KokoroViewModel,
    isDark: Boolean,
    bubbleCardBorder: BorderStroke
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val progressLogs by viewModel.habitProgress.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    var showAddHabitDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "☘️ Daily Habits tracker",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Build incredible long-term streaks offline",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddHabitDialog = true },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Habit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Empty Habits list! Tap Create Habit to add your first daily priority.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(habits) { habit ->
                    // Determine if completed for selected date
                    val isCompleted = progressLogs.any {
                        it.habitId == habit.id && it.dateString == selectedDate
                    }

                    HabitCard(
                        habit = habit,
                        isCompleted = isCompleted,
                        isDark = isDark,
                        bubbleCardBorder = bubbleCardBorder,
                        onToggleClick = { completed ->
                            viewModel.toggleHabitCompletion(habit.id, completed)
                        },
                        onDeleteHabit = { viewModel.deleteHabit(habit) }
                    )
                }
            }
        }
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onHabitCreated = { name, icon, col ->
                viewModel.addNewHabit(name, icon, col)
                showAddHabitDialog = false
            }
        )
    }
}

// Habit list subcomponent Card
@Composable
fun HabitCard(
    habit: Habit,
    isCompleted: Boolean,
    isDark: Boolean,
    bubbleCardBorder: BorderStroke,
    onToggleClick: (Boolean) -> Unit,
    onDeleteHabit: () -> Unit
) {
    val context = LocalContext.current
    val tintColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, if (isCompleted) tintColor else tintColor.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                tintColor.copy(alpha = if (isDark) 0.15f else 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Beautiful emoji circle icon tag
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(tintColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = habit.icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Waves,
                        contentDescription = "Streak record icon",
                        tint = if (isCompleted) Color.Red else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Current Streak: ${habit.streak} days (High: ${habit.bestStreak})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Checklist toggle trigger
            IconButton(
                onClick = {
                    onToggleClick(!isCompleted)
                    if (!isCompleted) {
                        Toast.makeText(context, "Yatta! Habit milestone +1! 🌸", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) tintColor else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = "Check off habit checklist",
                    tint = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onDeleteHabit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Habit record", tint = Color.Red.copy(alpha = 0.5f))
            }
        }
    }
}

// Add Habit Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onHabitCreated: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emojiTag by remember { mutableStateOf("🍵") }
    var colorThemeHex by remember { mutableStateOf("#FFD1DC") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("☘️ Create New Active Habit", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit goal name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Emoji list presets
                Text("Select Habit Icon:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("🍵", "📖", "🇯🇵", "🎨", "🏃", "💧", "🧘").forEach { emoji ->
                        FilterChip(
                            selected = emojiTag == emoji,
                            onClick = { emojiTag = emoji },
                            label = { Text(emoji, fontSize = 14.sp) }
                        )
                    }
                }

                // Pastel Color selection hex preset
                Text("Select Theme Color:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "#FFD1DC" to "Pink",
                        "#E6E6FA" to "Lavender",
                        "#98FF98" to "Matcha",
                        "#FFE5B4" to "Peach",
                        "#8EE5EE" to "Cyan"
                    ).forEach { (hexCode, labelName) ->
                        FilterChip(
                            selected = colorThemeHex == hexCode,
                            onClick = { colorThemeHex = hexCode },
                            label = { Text(labelName, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        onHabitCreated(name, emojiTag, colorThemeHex)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}

// ==================== TAB 2: PRODUCTIVITY ANALYTICS (CANVAS DRAWINGS) ====================
@Composable
fun ProductivityAnalyticsView(
    viewModel: KokoroViewModel,
    isDark: Boolean,
    bubbleCardBorder: BorderStroke
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val progressLogs by viewModel.habitProgress.collectAsStateWithLifecycle()

    // Calculate metrics
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val taskCompletionPct = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

    val completedHabitLogsCount = progressLogs.size
    val totalAvailableHabitStreaksSum = habits.sumOf { it.streak }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "📊 Insightful Productivity Analytics",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Observe your dynamic efficiency trends offline",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Circular dynamic score Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = bubbleCardBorder
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = taskCompletionPct.toFloat() / 100f,
                            modifier = Modifier.size(80.dp),
                            strokeWidth = 8.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        Text(
                            text = "$taskCompletionPct%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Daily Completion score",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tasks scheduled: $completedTasks done of $totalTasks total.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Habit check-ins logged: $completedHabitLogsCount times.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // CUSTOM CANVAS DRAWN CHART: Weekly efficiency bars
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = bubbleCardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📈 Weekly Efficiency Trends (Custom Canvas Chart)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated 7 day completions: percentages are 40%, 65%, 50%, 80%, 95%, 60%, 85%
                    val weeklyCompletionData = listOf(
                        "Mon" to 0.40f,
                        "Tue" to 0.65f,
                        "Wed" to 0.50f,
                        "Thu" to 0.80f,
                        "Fri" to 0.95f,
                        "Sat" to 0.60f,
                        "Sun" to 0.85f
                    )

                    val barColor = MaterialTheme.colorScheme.primary
                    val hoverColor = MaterialTheme.colorScheme.secondary

                    // Draw the chart on Compose Canvas!
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val canvasWidth = maxWidth
                        val canvasHeight = maxHeight

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val spacing = canvasWidth.toPx() / 8f
                            val activeHeightRange = canvasHeight.toPx() - 30.dp.toPx()

                            // Draw baseline grid support lines
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.2f),
                                start = Offset(0f, activeHeightRange * 0.5f),
                                end = Offset(size.width, activeHeightRange * 0.5f),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.2f),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                            // Baseline axis
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.4f),
                                start = Offset(0f, activeHeightRange),
                                end = Offset(size.width, activeHeightRange),
                                strokeWidth = 1.5.dp.toPx()
                            )

                            weeklyCompletionData.forEachIndexed { idx, (dayName, value) ->
                                val xPos = (idx + 1) * spacing
                                val barHeight = activeHeightRange * value
                                val barWidth = 22.dp.toPx()

                                // Draw Rounded Bar filled with nice gradient
                                drawRoundRect(
                                    color = if (idx == 4) hoverColor else barColor,
                                    topLeft = Offset(xPos - barWidth / 2f, activeHeightRange - barHeight),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }
                    }

                    // Horizontal Labels Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Mon 🌸", "Tue 🍃", "Wed 🌊", "Thu 🔥", "Fri ⚡", "Sat 🌟", "Sun ☀️").forEach { label ->
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tokyo Peak: Friday is your most productive day (95%)!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Leaderboard level card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = bubbleCardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏆 Kokoro Habits Leaderboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (habits.isEmpty()) {
                        Text("Finish standard habit accomplishments to observe leaderboard ranking here.")
                    } else {
                        habits.sortedByDescending { it.streak }.take(3).forEachIndexed { rank, habit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${rank + 1}. ${habit.icon} ${habit.name}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Streak: ${habit.streak} days 🔥",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 3: CUSTOMIZABLE HOMESCREEN WIDGET IN-APP CONTROLLER ====================
@Composable
fun CustomizableWidgetView(
    viewModel: KokoroViewModel,
    isDark: Boolean,
    bubbleCardBorder: BorderStroke
) {
    val tasks by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()

    val widgetColorHex by viewModel.widgetColorTheme.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.widgetSelectedCategory.collectAsStateWithLifecycle()

    // Parse widget background color cleanly
    val parsedBgColor = try {
        Color(android.graphics.Color.parseColor(widgetColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "🌟 Custom Widget Creator",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Configure custom layouts & categories for daily widgets preview",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Widget live interactive simulator
        item {
            Text(
                text = "Live Interactive Preview (Simulated Homescreen layout):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Widget Card drawing
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = parsedBgColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "こ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kokoro Tracker (2x2 Widget)",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Category: $selectedCategory",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated list item display matching the active categories inside simulator!
                    val widgetDisplayItems = tasks.filter {
                        selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)
                    }.take(3)

                    if (widgetDisplayItems.isEmpty()) {
                        Text(
                            text = "No active scheduled items in Category: '$selectedCategory' today!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            widgetDisplayItems.forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Status",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = task.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                        )
                                    }
                                    Text(
                                        text = task.priority,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (task.priority == "HIGH") Color.Yellow else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Offline widget is fully persistent with SQLite AppDatabase snapshot.",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Widget styling custom selectors controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = bubbleCardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎨 Widget Design Palette Tracker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Customize background color:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "#FFD1DC" to "Blossom Pink",
                            "#FFA07A" to "Warm Peach",
                            "#E6E6FA" to "Lavender",
                            "#0F0F26" to "Cyber Deep Navy",
                            "#FF1A75" to "Neon Magenta"
                        ).forEach { (hexCode, labelName) ->
                            Button(
                                onClick = { viewModel.updateWidgetCustomization(hexCode, selectedCategory) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(android.graphics.Color.parseColor(hexCode))
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = labelName,
                                    fontSize = 9.sp,
                                    color = if (hexCode == "#0F0F26") Color.White else Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Filter displays Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Work", "Study", "Hobby").forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { viewModel.updateWidgetCustomization(widgetColorHex, category) },
                                label = { Text(category, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        }
    }
}

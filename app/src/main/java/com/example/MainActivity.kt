package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.CanvaDarkColorScheme
import com.example.ui.theme.CanvaLightColorScheme
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.Typography
import java.net.URL
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val DEFAULT_FALLBACK_LATITUDE = 10.51
private const val DEFAULT_FALLBACK_LONGITUDE = 124.03

private val CreamCardColor = Color(0xFFFFFBF0)
private val IvoryInnerCardColor = Color(0xFFFDFBF7)
private val WarmCreamTrackColor = Color(0xFFE6DFCF)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      CivilEngineeringProductivityApp()
    }
  }
}

/**
 * Responsive screen metrics computed from `LocalConfiguration.current.screenWidthDp`.
 * Scales up padding and text sizes by `1.2f` when `screenWidth > 600` (tablets),
 * and scales down gently on compact narrow phones (`< 360dp`) while keeping icons and
 * the right-edge "Extras" pill strictly bounded.
 */
data class ResponsiveMetrics(
  val screenWidthDp: Int,
  val scaleMultiplier: Float,
  val maxContentWidth: Dp
) {
  fun scaledDp(baseDp: Int): Dp = (baseDp * scaleMultiplier).dp
  fun scaledSp(baseSp: Int): TextUnit = (baseSp * scaleMultiplier).sp
}

@Composable
private fun rememberResponsiveMetrics(): ResponsiveMetrics {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp
  val scaleMultiplier = when {
    screenWidth > 600 -> 1.2f
    screenWidth < 360 -> 0.92f
    else -> 1.0f
  }
  val maxContentWidth = if (screenWidth > 600) 840.dp else 640.dp
  return remember(screenWidth) {
    ResponsiveMetrics(
      screenWidthDp = screenWidth,
      scaleMultiplier = scaleMultiplier,
      maxContentWidth = maxContentWidth
    )
  }
}

/**
 * Five main navigation tabs:
 * Dashboard, Tasks, Schedule, Clock, Calendar.
 */
enum class CeNavTab(
  val title: String,
  val routeTag: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  DASHBOARD(
    title = "Dashboard",
    routeTag = "nav_tab_dashboard",
    selectedIcon = Icons.Filled.Dashboard,
    unselectedIcon = Icons.Outlined.Dashboard
  ),
  TASKS(
    title = "Tasks",
    routeTag = "nav_tab_tasks",
    selectedIcon = Icons.Filled.Assignment,
    unselectedIcon = Icons.Outlined.Assignment
  ),
  SCHEDULE(
    title = "Schedule",
    routeTag = "nav_tab_schedule",
    selectedIcon = Icons.Filled.EventNote,
    unselectedIcon = Icons.Outlined.EventNote
  ),
  CLOCK(
    title = "Clock",
    routeTag = "nav_tab_clock",
    selectedIcon = Icons.Filled.Schedule,
    unselectedIcon = Icons.Outlined.Schedule
  ),
  CALENDAR(
    title = "Calendar",
    routeTag = "nav_tab_calendar",
    selectedIcon = Icons.Filled.CalendarMonth,
    unselectedIcon = Icons.Outlined.CalendarMonth
  )
}

/**
 * Shared Task Data Model including `id`, `title`, `dueDate`, and `isCompleted`.
 */
data class Task(
  val id: Int,
  val title: String,
  val dueDate: String,
  val isCompleted: Boolean = false
)

/**
 * Data Model for Today's Class Schedule items on the Dashboard.
 */
data class ClassScheduleItem(
  val id: String,
  val subject: String,
  val timeRange: String,
  val room: String
)

/**
 * Data Model for the colored Timetable Schedule on the "Schedule" tab:
 * `id: Int, subjectName: String, timeRange: String, days: List<String>, room: String, colorHex: Long`.
 */
data class ClassSchedule(
  val id: Int,
  val subjectName: String,
  val timeRange: String,
  val days: List<String>,
  val room: String,
  val colorHex: Long
)

/**
 * Selectable Day options for the Week View in the Schedule tab.
 */
enum class ScheduleWeekDay(
  val shortLabel: String,
  val dayCode: String,
  val fullLabel: String
) {
  MON(shortLabel = "Mon", dayCode = "M", fullLabel = "Monday"),
  TUE(shortLabel = "Tue", dayCode = "Tue", fullLabel = "Tuesday"),
  WED(shortLabel = "Wed", dayCode = "W", fullLabel = "Wednesday"),
  THU(shortLabel = "Thu", dayCode = "Th", fullLabel = "Thursday"),
  FRI(shortLabel = "Fri", dayCode = "F", fullLabel = "Friday"),
  SAT(shortLabel = "Sat", dayCode = "Sat", fullLabel = "Saturday")
}

/**
 * Data Model for Engineering Tool Placeholder Cards on Page 1 (Extras).
 */
data class ExtraToolItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val icon: ImageVector
)

/**
 * Shared ViewModel storing and managing:
 * 1. Global theme state (`isDarkMode`, default `false`).
 * 2. User session data (`userName` and `userEmail`).
 * 3. Live Open-Meteo weather summary (`weatherText`, starting at `"Fetching Weather..."`).
 * 4. Mutable list of coursework tasks shared across the Academic Dashboard and Task Manager.
 * 5. Today's Schedule list on the Dashboard.
 * 6. Mutable list of `ClassSchedule` items for the colored Week Timetable & COR Auto-Extract.
 */
class TaskViewModel : ViewModel() {
  var isDarkMode by mutableStateOf(false)
    private set

  var userName by mutableStateOf("Alberto Jr. M. Manzo")
    private set

  var userEmail by mutableStateOf("alberto@civil.edu")
    private set

  var weatherText by mutableStateOf("Fetching Weather...")
    private set

  var hasRequestedInitialWeather by mutableStateOf(false)
    private set

  var isExtractingCor by mutableStateOf(false)
    private set

  var isCorExtracted by mutableStateOf(false)
    private set

  private var nextId by mutableIntStateOf(6)
  private var nextScheduleId by mutableIntStateOf(100)

  private val _tasks = mutableStateListOf(
    Task(
      id = 1,
      title = "Reinforced Concrete Design - Plate 3",
      dueDate = "Oct 22",
      isCompleted = false
    ),
    Task(
      id = 2,
      title = "Hydraulics Quiz",
      dueDate = "Oct 24",
      isCompleted = false
    ),
    Task(
      id = 3,
      title = "Complete Surveying Field Report",
      dueDate = "Oct 25",
      isCompleted = false
    ),
    Task(
      id = 4,
      title = "Read Steel Design Chapter 4",
      dueDate = "Oct 27",
      isCompleted = true
    ),
    Task(
      id = 5,
      title = "Prepare Soil Mechanics Sieve Analysis Data",
      dueDate = "Oct 29",
      isCompleted = true
    )
  )
  val tasks: List<Task> get() = _tasks

  val todaysSchedule: List<ClassScheduleItem> = listOf(
    ClassScheduleItem(
      id = "sched_rcd",
      subject = "Reinforced Concrete Design",
      timeRange = "7:00 AM - 9:00 AM",
      room = "Room 307"
    ),
    ClassScheduleItem(
      id = "sched_foundation",
      subject = "Foundation Design",
      timeRange = "9:30 AM - 12:30 PM",
      room = "Room 402"
    ),
    ClassScheduleItem(
      id = "sched_techno",
      subject = "Technopreneurship",
      timeRange = "1:30 PM - 3:30 PM",
      room = "Room 305"
    )
  )

  private val _classSchedules = mutableStateListOf<ClassSchedule>()
  val classSchedules: List<ClassSchedule> get() = _classSchedules

  /**
   * Simulates a 2-second COR PDF extraction and populates `_classSchedules`
   * with the 8 courses from the user's CTU-Danao BSCE 4 Certificate of Registration.
   */
  suspend fun extractCorSchedule() {
    if (isExtractingCor) return
    isExtractingCor = true
    delay(2_000L)

    val extractedCorCourses = listOf(
      ClassSchedule(
        id = 1,
        subjectName = "CE COMPREHENSIVE COURSE 1",
        timeRange = "04:30PM-07:30PM",
        days = listOf("Th", "F"),
        room = "ENG. 307",
        colorHex = 0xFF5C6BC0L // Light Indigo
      ),
      ClassSchedule(
        id = 2,
        subjectName = "CONSTRUCTION METHODS & PROJECT MGT",
        timeRange = "04:30PM-06:30PM",
        days = listOf("M", "Tue"),
        room = "COEN LAB",
        colorHex = 0xFFF97316L // Soft Orange
      ),
      ClassSchedule(
        id = 3,
        subjectName = "REINFORCED CONCRETE DESIGN",
        timeRange = "02:30PM-04:00PM",
        days = listOf("M", "Tue"),
        room = "ENG. 301",
        colorHex = 0xFF0D9488L // Muted Teal
      ),
      ClassSchedule(
        id = 4,
        subjectName = "DATABASE MANAGEMENT IN CONSTRUCTION",
        timeRange = "07:00AM-08:30AM",
        days = listOf("M", "Tue"),
        room = "ENG. 307",
        colorHex = 0xFFEF4444L // Soft Red
      ),
      ClassSchedule(
        id = 5,
        subjectName = "BRIDGE DESIGN",
        timeRange = "10:00AM-12:00PM",
        days = listOf("F"),
        room = "ENG. 306",
        colorHex = 0xFF3B82F6L // Slate Blue
      ),
      ClassSchedule(
        id = 6,
        subjectName = "CE PROJECT 1",
        timeRange = "10:00AM-12:00PM",
        days = listOf("Tue", "W"),
        room = "COEN LAB",
        colorHex = 0xFF8B5CF6L // Light Purple
      ),
      ClassSchedule(
        id = 7,
        subjectName = "QUANTITY SURVEYING",
        timeRange = "02:30PM-04:00PM",
        days = listOf("W", "Th"),
        room = "ENG. 307",
        colorHex = 0xFF10B981L // Soft Green
      ),
      ClassSchedule(
        id = 8,
        subjectName = "TECHNOPRENEURSHIP 101",
        timeRange = "04:30PM-07:30PM",
        days = listOf("Th", "F"),
        room = "ENG. 307",
        colorHex = 0xFFD97706L // Amber
      )
    )

    // Keep any manually added courses (id >= 100) while replacing/populating the COR courses
    val manualCourses = _classSchedules.filter { it.id >= 100 }
    _classSchedules.clear()
    _classSchedules.addAll(extractedCorCourses)
    _classSchedules.addAll(manualCourses)

    isExtractingCor = false
    isCorExtracted = true
  }

  fun addClassSchedule(
    subjectName: String,
    timeRange: String,
    days: List<String>,
    room: String,
    colorHex: Long
  ) {
    val trimmedSubject = subjectName.trim()
    val trimmedTime = timeRange.trim().ifEmpty { "08:00AM-10:00AM" }
    val trimmedRoom = room.trim().ifEmpty { "ENG. 301" }
    val normalizedDays = if (days.isEmpty()) listOf("M") else days

    if (trimmedSubject.isNotEmpty()) {
      _classSchedules.add(
        0,
        ClassSchedule(
          id = nextScheduleId++,
          subjectName = trimmedSubject,
          timeRange = trimmedTime,
          days = normalizedDays,
          room = trimmedRoom,
          colorHex = colorHex
        )
      )
    }
  }

  fun deleteClassSchedule(scheduleId: Int) {
    _classSchedules.removeAll { it.id == scheduleId }
  }

  /**
   * Fetches real-time weather from Open-Meteo using `Dispatchers.IO`,
   * `java.net.URL(url).readText()`, and `org.json.JSONObject`.
   */
  suspend fun fetchWeather(
    latitude: Double = DEFAULT_FALLBACK_LATITUDE,
    longitude: Double = DEFAULT_FALLBACK_LONGITUDE
  ) {
    hasRequestedInitialWeather = true
    weatherText = "Fetching Weather..."

    val updatedWeather = withContext(Dispatchers.IO) {
      try {
        val url =
          "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true"
        val rawJson = URL(url).readText()
        val rootObject = JSONObject(rawJson)
        val currentWeather = rootObject.getJSONObject("current_weather")
        val temperature = currentWeather.getDouble("temperature")
        val weatherCode = currentWeather.getInt("weathercode")
        val condition = mapWeatherCodeToCondition(weatherCode)
        "$condition • ${temperature.roundToInt()}°C"
      } catch (e: CancellationException) {
        throw e
      } catch (_: Exception) {
        "Weather Offline"
      }
    }

    weatherText = updatedWeather
  }

  private fun mapWeatherCodeToCondition(weatherCode: Int): String {
    return when (weatherCode) {
      0 -> "Clear"
      1, 2, 3 -> "Cloudy"
      45, 48 -> "Foggy"
      51, 53, 55, 56, 57 -> "Drizzle"
      61, 63, 65, 66, 67, 80, 81, 82 -> "Rain"
      71, 73, 75, 77, 85, 86 -> "Snow"
      95, 96, 99 -> "Thunderstorm"
      else -> "Cloudy"
    }
  }

  fun updateDarkMode(enabled: Boolean) {
    isDarkMode = enabled
  }

  fun loginWithEmail(typedEmail: String) {
    userName = "Alberto Jr. M. Manzo"
    userEmail = typedEmail.trim().ifEmpty { "alberto@civil.edu" }
  }

  fun loginAsGuest() {
    userName = "Guest"
    userEmail = ""
  }

  fun addTask(title: String, dueDate: String) {
    val trimmedTitle = title.trim()
    val trimmedDueDate = dueDate.trim().ifEmpty { "Upcoming" }
    if (trimmedTitle.isNotEmpty()) {
      _tasks.add(
        0,
        Task(
          id = nextId++,
          title = trimmedTitle,
          dueDate = trimmedDueDate,
          isCompleted = false
        )
      )
    }
  }

  fun toggleTaskCompletion(taskId: Int) {
    val index = _tasks.indexOfFirst { it.id == taskId }
    if (index != -1) {
      val current = _tasks[index]
      _tasks[index] = current.copy(isCompleted = !current.isCompleted)
    }
  }

  fun deleteTask(taskId: Int) {
    _tasks.removeAll { it.id == taskId }
  }
}

/**
 * Formats the live device time using `java.time.LocalDateTime` as "EEEE, MMM dd • h:mm a"
 * (e.g., "Wednesday, Oct 21 • 8:58 PM").
 */
@SuppressLint("NewApi")
private fun formatLiveDeviceDateTime(): String {
  val formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd • h:mm a", Locale.getDefault())
  return LocalDateTime.now().format(formatter)
}

/**
 * Determines the real-world day of week using `java.time.LocalDate.now().dayOfWeek`
 * and maps it to the exact abbreviations used in the mock COR data ("M", "Tue", "W", "Th", "F", "Sat").
 */
@SuppressLint("NewApi")
private fun resolveTodayCorDayAbbreviation(): String {
  return when (LocalDate.now().dayOfWeek) {
    DayOfWeek.MONDAY -> "M"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "W"
    DayOfWeek.THURSDAY -> "Th"
    DayOfWeek.FRIDAY -> "F"
    DayOfWeek.SATURDAY -> "Sat"
    DayOfWeek.SUNDAY -> "Sun"
  }
}

/**
 * Returns the warm cream card background (`#FFFBF0`) in Light Mode and the dark card surface in Dark Mode.
 */
@Composable
private fun dashboardCreamCardColor(): Color {
  val isLight = MaterialTheme.colorScheme.background.red > 0.5f
  return if (isLight) CreamCardColor else MaterialTheme.colorScheme.surface
}

/**
 * Returns the warm ivory inner card background (`#FDFBF7`) in Light Mode and dark background in Dark Mode.
 */
@Composable
private fun dashboardIvoryInnerColor(): Color {
  val isLight = MaterialTheme.colorScheme.background.red > 0.5f
  return if (isLight) IvoryInnerCardColor else MaterialTheme.colorScheme.background
}

/**
 * Returns a high-contrast warm track color for progress indicators on cream cards in Light Mode.
 */
@Composable
private fun dashboardProgressTrackColor(): Color {
  val isLight = MaterialTheme.colorScheme.background.red > 0.5f
  return if (isLight) WarmCreamTrackColor else MaterialTheme.colorScheme.surfaceVariant
}

/**
 * Retrieves the device's coarse coordinates from `LocationManager` if `ACCESS_COARSE_LOCATION`
 * is granted, or safely falls back to Latitude 10.51, Longitude 124.03 when denied or null.
 */
private fun resolveDeviceCoordinatesOrFallback(context: Context): Pair<Double, Double> {
  val fallback = DEFAULT_FALLBACK_LATITUDE to DEFAULT_FALLBACK_LONGITUDE
  val hasCoarsePermission = ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.ACCESS_COARSE_LOCATION
  ) == PackageManager.PERMISSION_GRANTED

  if (!hasCoarsePermission) {
    return fallback
  }

  return try {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      ?: return fallback

    val providers = listOf(
      LocationManager.NETWORK_PROVIDER,
      LocationManager.PASSIVE_PROVIDER,
      LocationManager.GPS_PROVIDER
    )

    val lastKnownLocation = providers.firstNotNullOfOrNull { provider ->
      try {
        if (locationManager.isProviderEnabled(provider)) {
          locationManager.getLastKnownLocation(provider)
        } else {
          null
        }
      } catch (_: SecurityException) {
        null
      } catch (_: Exception) {
        null
      }
    }

    if (lastKnownLocation != null) {
      lastKnownLocation.latitude to lastKnownLocation.longitude
    } else {
      fallback
    }
  } catch (_: SecurityException) {
    fallback
  } catch (_: Exception) {
    fallback
  }
}

@Composable
fun CivilEngineeringProductivityApp(
  taskViewModel: TaskViewModel = viewModel()
) {
  // Dynamically switch between darkColorScheme() and lightColorScheme() based on ViewModel.isDarkMode
  val dynamicColorScheme = if (taskViewModel.isDarkMode) {
    CanvaDarkColorScheme
  } else {
    CanvaLightColorScheme
  }

  MaterialTheme(
    colorScheme = dynamicColorScheme,
    typography = Typography
  ) {
    // Top-level navigation state: launches into the Startup/Login Screen first
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    Crossfade(
      targetState = isLoggedIn,
      animationSpec = tween(durationMillis = 300),
      label = "auth_to_main_transition"
    ) { loggedIn ->
      if (!loggedIn) {
        StartupLoginScreen(
          onLoginWithEmail = { typedEmail ->
            taskViewModel.loginWithEmail(typedEmail)
            isLoggedIn = true
          },
          onContinueAsGuest = {
            taskViewModel.loginAsGuest()
            isLoggedIn = true
          }
        )
      } else {
        MainProductivityScaffold(
          taskViewModel = taskViewModel,
          onLogout = { isLoggedIn = false }
        )
      }
    }
  }
}

/**
 * Canva-Inspired Startup / Login Screen:
 * - Soft gradient background that adapts smoothly to Light and Dark mode.
 * - Uses `LocalConfiguration` scaling multiplier so text and padding adapt cleanly to phones and tablets.
 * - Displays "Sibil App" in large, bold, elegant typography with "CE Student Command Center" underneath.
 * - Captures typed Email on "Log In" (setting userName to "Alberto Jr. M. Manzo") or sets "Guest" on "Continue as Guest".
 */
@Composable
fun StartupLoginScreen(
  onLoginWithEmail: (String) -> Unit,
  onContinueAsGuest: () -> Unit,
  modifier: Modifier = Modifier
) {
  val metrics = rememberResponsiveMetrics()
  var email by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

  val topGradientColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
  val baseBackground = MaterialTheme.colorScheme.background

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(topGradientColor, baseBackground, baseBackground)
        )
      )
      .testTag("startup_login_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = if (metrics.screenWidthDp > 600) 520.dp else 440.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState())
        .padding(
          horizontal = metrics.scaledDp(26),
          vertical = metrics.scaledDp(32)
        ),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(24))
    ) {
      // Brand Header: "Sibil App" + "CE Student Command Center"
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
      ) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(22.dp),
          shadowElevation = 6.dp,
          modifier = Modifier.size(64.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Architecture,
              contentDescription = stringResource(R.string.app_name),
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(32.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = metrics.scaledSp(34),
            lineHeight = metrics.scaledSp(40),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onBackground,
          textAlign = TextAlign.Center,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_app_title")
        )

        Text(
          text = stringResource(R.string.login_subtitle),
          style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(16),
            fontWeight = FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_app_slogan")
        )
      }

      // Elevated, Floating Card Centered on Screen
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(
              horizontal = metrics.scaledDp(24),
              vertical = metrics.scaledDp(28)
            ),
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
        ) {
          // Email OutlinedTextField
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
              Text(
                text = stringResource(R.string.email_label),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            placeholder = {
              Text(
                text = stringResource(R.string.email_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              fontSize = metrics.scaledSp(16),
              color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
              focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
              unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
              cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Email,
              imeAction = ImeAction.Next
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_email_input")
          )

          // Password OutlinedTextField (visually obscured)
          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
              Text(
                text = stringResource(R.string.password_label),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            placeholder = {
              Text(
                text = stringResource(R.string.password_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
            },
            trailingIcon = {
              IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible },
                modifier = Modifier
                  .size(48.dp)
                  .minimumInteractiveComponentSize()
                  .testTag("toggle_password_visibility_button")
              ) {
                Icon(
                  imageVector = if (isPasswordVisible) {
                    Icons.Filled.VisibilityOff
                  } else {
                    Icons.Filled.Visibility
                  },
                  contentDescription = if (isPasswordVisible) {
                    "Hide password"
                  } else {
                    "Show password"
                  },
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(24.dp)
                )
              }
            },
            visualTransformation = if (isPasswordVisible) {
              VisualTransformation.None
            } else {
              PasswordVisualTransformation()
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              fontSize = metrics.scaledSp(16),
              color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
              focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
              unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
              cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = { onLoginWithEmail(email) }
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_password_input")
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Pill-Shaped Primary "Log In" Button
          Button(
            onClick = { onLoginWithEmail(email) },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(metrics.scaledDp(52))
              .testTag("login_submit_button")
          ) {
            Text(
              text = stringResource(R.string.login_button),
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = metrics.scaledSp(17),
                fontWeight = FontWeight.Bold
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Pill-Shaped Secondary "Continue as Guest" TextButton
          TextButton(
            onClick = onContinueAsGuest,
            shape = CircleShape,
            modifier = Modifier
              .fillMaxWidth()
              .height(metrics.scaledDp(48))
              .testTag("continue_as_guest_button")
          ) {
            Text(
              text = stringResource(R.string.continue_guest_button),
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = metrics.scaledSp(16),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.secondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MainProductivityScaffold(
  taskViewModel: TaskViewModel,
  onLogout: () -> Unit
) {
  val metrics = rememberResponsiveMetrics()
  var selectedTab by rememberSaveable { mutableStateOf(CeNavTab.DASHBOARD) }
  var isAddTaskDialogOpen by rememberSaveable { mutableStateOf(false) }
  var isAddScheduleDialogOpen by rememberSaveable { mutableStateOf(false) }
  var isProfileSettingsDialogOpen by rememberSaveable { mutableStateOf(false) }

  // Return to Dashboard when pressing system Back on any secondary bottom tab
  if (selectedTab != CeNavTab.DASHBOARD) {
    BackHandler {
      selectedTab = CeNavTab.DASHBOARD
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .testTag("main_scaffold"),
    contentWindowInsets = WindowInsets.safeDrawing,
    floatingActionButton = {
      when (selectedTab) {
        CeNavTab.TASKS -> {
          FloatingActionButton(
            onClick = { isAddTaskDialogOpen = true },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
            modifier = Modifier
              .size(60.dp)
              .testTag("add_task_fab")
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = stringResource(R.string.add_task_fab_label),
              modifier = Modifier.size(28.dp)
            )
          }
        }
        CeNavTab.SCHEDULE -> {
          FloatingActionButton(
            onClick = { isAddScheduleDialogOpen = true },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
            modifier = Modifier
              .size(60.dp)
              .testTag("add_schedule_fab")
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = stringResource(R.string.add_schedule_fab_label),
              modifier = Modifier.size(28.dp)
            )
          }
        }
        else -> {}
      }
    },
    bottomBar = {
      NavigationBar(
        modifier = Modifier.testTag("bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
      ) {
        CeNavTab.entries.forEach { tab ->
          val isSelected = selectedTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { selectedTab = tab },
            modifier = Modifier.testTag(tab.routeTag),
            icon = {
              Icon(
                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.title,
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = tab.title,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = metrics.scaledSp(11),
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.primary,
              selectedTextColor = MaterialTheme.colorScheme.primary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer,
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter
    ) {
      when (selectedTab) {
        CeNavTab.DASHBOARD -> SwipeableDashboardAndExtrasPager(
          viewModel = taskViewModel,
          onOpenProfileSettings = { isProfileSettingsDialogOpen = true },
          modifier = Modifier.fillMaxSize()
        )
        CeNavTab.TASKS -> CourseworkTaskManagerScreen(
          viewModel = taskViewModel,
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = metrics.maxContentWidth)
        )
        CeNavTab.SCHEDULE -> ClassTimetableScheduleScreen(
          viewModel = taskViewModel,
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = metrics.maxContentWidth)
        )
        CeNavTab.CLOCK -> PlaceholderScreen(
          title = "Clock",
          testTag = "placeholder_screen_clock"
        )
        CeNavTab.CALENDAR -> PlaceholderScreen(
          title = "Calendar",
          testTag = "placeholder_screen_calendar"
        )
      }
    }

    if (isAddTaskDialogOpen) {
      AddTaskDialog(
        onDismiss = { isAddTaskDialogOpen = false },
        onAddTask = { newTaskTitle, newTaskDueDate ->
          taskViewModel.addTask(newTaskTitle, newTaskDueDate)
          isAddTaskDialogOpen = false
        }
      )
    }

    if (isAddScheduleDialogOpen) {
      AddClassScheduleDialog(
        onDismiss = { isAddScheduleDialogOpen = false },
        onSaveSchedule = { subjectName, timeRange, selectedDays, room, colorHex ->
          taskViewModel.addClassSchedule(
            subjectName = subjectName,
            timeRange = timeRange,
            days = selectedDays,
            room = room,
            colorHex = colorHex
          )
          isAddScheduleDialogOpen = false
        }
      )
    }

    if (isProfileSettingsDialogOpen) {
      ProfileAndSettingsDialog(
        viewModel = taskViewModel,
        onDismiss = { isProfileSettingsDialogOpen = false },
        onSwitchAccount = {
          isProfileSettingsDialogOpen = false
          onLogout()
        }
      )
    }
  }
}

/**
 * Swipeable `HorizontalPager` on the Dashboard tab:
 * - Page 0: Main Academic Dashboard with a subtle geometric Canvas background,
 *   real Open-Meteo Weather Summary & live ticking `LocalDateTime` header,
 *   clickable upper-right Profile Icon opening the Settings Dialog, side-by-side
 *   "Daily Progress" ring + "Today's Schedule" interactive card, dynamic "Upcoming Deadlines",
 *   and a fixed-dimension vertical "Extras" tab anchored on the right edge that never overlaps cards.
 * - Page 1: Extras screen containing ONLY the engineering tool placeholders
 *   (Scientific Calculator, Unit Converter, BOQ Estimator, AI Study Assistant).
 */
@Composable
fun SwipeableDashboardAndExtrasPager(
  viewModel: TaskViewModel,
  onOpenProfileSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
  val coroutineScope = rememberCoroutineScope()

  // When viewing Page 1 (Extras) inside the Dashboard pager, system Back returns to Page 0
  if (pagerState.currentPage == 1) {
    BackHandler {
      coroutineScope.launch {
        pagerState.animateScrollToPage(0)
      }
    }
  }

  HorizontalPager(
    state = pagerState,
    modifier = modifier
      .fillMaxSize()
      .testTag("dashboard_horizontal_pager")
  ) { page ->
    when (page) {
      0 -> AcademicDashboardPage(
        viewModel = viewModel,
        onProfileIconClick = onOpenProfileSettings,
        onOpenExtrasPage = {
          coroutineScope.launch {
            pagerState.animateScrollToPage(1)
          }
        }
      )
      1 -> ExtrasToolsOnlyPage(
        onBackToDashboard = {
          coroutineScope.launch {
            pagerState.animateScrollToPage(0)
          }
        }
      )
    }
  }
}

/**
 * Page 0 of the Dashboard HorizontalPager:
 * - Uses `LocalConfiguration.current.screenWidthDp` via `rememberResponsiveMetrics()` to scale
 *   fonts and padding by `1.2f` on tablets (`screenWidth > 600`) and keep proportional `weight(1f)`
 *   allocations on all rows and columns.
 * - Reserves dedicated right-side space (`end = 34.dp`) for the fixed `28.dp x 112.dp` vertical
 *   "Extras" pill indicator so it never overlaps any Dashboard card, regardless of screen width.
 */
@Composable
fun AcademicDashboardPage(
  viewModel: TaskViewModel,
  onProfileIconClick: () -> Unit,
  onOpenExtrasPage: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val metrics = rememberResponsiveMetrics()

  val allTasks = viewModel.tasks
  val totalTasks = allTasks.size
  val completedTasks = allTasks.count { it.isCompleted }
  val activeTasks = allTasks.filter { !it.isCompleted }

  // Runtime permission launcher for ACCESS_COARSE_LOCATION
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    coroutineScope.launch {
      if (isGranted) {
        val (lat, lon) = resolveDeviceCoordinatesOrFallback(context)
        viewModel.fetchWeather(latitude = lat, longitude = lon)
      } else {
        viewModel.fetchWeather(
          latitude = DEFAULT_FALLBACK_LATITUDE,
          longitude = DEFAULT_FALLBACK_LONGITUDE
        )
      }
    }
  }

  // Trigger location permission check & initial Open-Meteo weather fetch when Dashboard opens
  LaunchedEffect(Unit) {
    if (!viewModel.hasRequestedInitialWeather) {
      val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED

      if (hasPermission) {
        val (lat, lon) = resolveDeviceCoordinatesOrFallback(context)
        viewModel.fetchWeather(latitude = lat, longitude = lon)
      } else {
        try {
          locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        } catch (_: Exception) {
          viewModel.fetchWeather(
            latitude = DEFAULT_FALLBACK_LATITUDE,
            longitude = DEFAULT_FALLBACK_LONGITUDE
          )
        }
      }
    }
  }

  // Live ticking device time using java.time.LocalDateTime updated every second
  var currentDateTimeText by remember { mutableStateOf(formatLiveDeviceDateTime()) }
  LaunchedEffect(Unit) {
    while (true) {
      currentDateTimeText = formatLiveDeviceDateTime()
      delay(1_000L)
    }
  }

  val primaryShapeColor = MaterialTheme.colorScheme.primary
  val secondaryShapeColor = MaterialTheme.colorScheme.secondary
  val tertiaryShapeColor = MaterialTheme.colorScheme.tertiary

  // Fixed width for the right-edge Extras pill so we guarantee non-overlapping clearance
  val extrasPillWidth = 28.dp
  val extrasPillHeight = 112.dp

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter
  ) {
    // Subtle, low-opacity geometric Canvas background behind dashboard content
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .testTag("dashboard_geometric_canvas")
    ) {
      val w = size.width
      val h = size.height

      drawCircle(
        color = primaryShapeColor.copy(alpha = 0.05f),
        radius = w * 0.44f,
        center = Offset(x = w * 0.88f, y = h * 0.12f)
      )
      drawCircle(
        color = secondaryShapeColor.copy(alpha = 0.04f),
        radius = w * 0.36f,
        center = Offset(x = w * 0.14f, y = h * 0.34f)
      )
      drawCircle(
        color = tertiaryShapeColor.copy(alpha = 0.045f),
        radius = w * 0.46f,
        center = Offset(x = w * 0.82f, y = h * 0.82f)
      )

      rotate(degrees = -14f, pivot = Offset(w * 0.25f, h * 0.18f)) {
        drawRoundRect(
          color = primaryShapeColor.copy(alpha = 0.04f),
          topLeft = Offset(x = -w * 0.08f, y = h * 0.08f),
          size = Size(width = w * 0.56f, height = h * 0.14f),
          cornerRadius = CornerRadius(36f, 36f)
        )
      }

      val polyPath = Path().apply {
        moveTo(w * 0.06f, h * 0.68f)
        lineTo(w * 0.28f, h * 0.62f)
        lineTo(w * 0.42f, h * 0.74f)
        lineTo(w * 0.30f, h * 0.88f)
        lineTo(w * 0.08f, h * 0.84f)
        close()
      }
      drawPath(
        path = polyPath,
        color = primaryShapeColor.copy(alpha = 0.035f)
      )
    }

    // Foreground Scrollable Dashboard Content with guaranteed right margin for the Extras pill
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(end = extrasPillWidth + 6.dp)
        .widthIn(max = metrics.maxContentWidth)
        .testTag("academic_dashboard_list"),
      contentPadding = PaddingValues(
        start = metrics.scaledDp(16),
        end = metrics.scaledDp(10),
        top = metrics.scaledDp(20),
        bottom = metrics.scaledDp(32)
      ),
      verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(20))
    ) {
      // 1. Dynamic Top Header: Weather Summary, Live LocalDateTime, and Profile Icon
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(10)),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(6))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                  coroutineScope.launch {
                    val (lat, lon) = resolveDeviceCoordinatesOrFallback(context)
                    viewModel.fetchWeather(latitude = lat, longitude = lon)
                  }
                },
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(10))
            ) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.size(40.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Filled.Cloud,
                    contentDescription = "Live Weather Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              Text(
                text = viewModel.weatherText,
                style = MaterialTheme.typography.headlineLarge.copy(
                  fontSize = metrics.scaledSp(24),
                  lineHeight = metrics.scaledSp(30),
                  fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .weight(1f)
                  .testTag("dashboard_weather_header")
              )
            }

            Text(
              text = currentDateTimeText,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = metrics.scaledSp(15),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_datetime_header")
            )
          }

          IconButton(
            onClick = onProfileIconClick,
            modifier = Modifier
              .size(48.dp)
              .minimumInteractiveComponentSize()
              .testTag("dashboard_profile_icon_button")
          ) {
            Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = "Open Profile & Settings",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(36.dp)
            )
          }
        }
      }

      // 2. Middle Section (Side-by-Side): Floating Daily Progress Card next to Dynamic Today's Schedule Card
      item {
        val middleRowHeight = metrics.scaledDp(276)
        val todayAbbreviation = resolveTodayCorDayAbbreviation()
        val todaysFilteredClasses = viewModel.classSchedules.filter { schedule ->
          schedule.days.any { dayToken ->
            dayToken.equals(todayAbbreviation, ignoreCase = true)
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(middleRowHeight),
          horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(12)),
          verticalAlignment = Alignment.CenterVertically
        ) {
          CompactDailyProgressCard(
            completedCount = completedTasks,
            totalCount = totalTasks,
            metrics = metrics,
            modifier = Modifier
              .weight(0.95f)
              .fillMaxHeight()
          )

          TodaysScheduleInteractiveCard(
            todaysClasses = todaysFilteredClasses,
            todayAbbreviation = todayAbbreviation,
            metrics = metrics,
            modifier = Modifier
              .weight(1.15f)
              .fillMaxHeight()
          )
        }
      }

      // 3. Bottom Section: Dynamic "Upcoming Deadlines" Task List connected to ViewModel
      item {
        UpcomingDeadlinesSectionCard(
          activeTasks = activeTasks,
          metrics = metrics,
          onToggleComplete = { taskId ->
            viewModel.toggleTaskCompletion(taskId)
          }
        )
      }
    }

    // Fixed-dimension vertical "Extras" tab safely anchored to the right edge without overlapping cards
    Surface(
      onClick = onOpenExtrasPage,
      color = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
      shadowElevation = 10.dp,
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .width(extrasPillWidth)
        .height(extrasPillHeight)
        .testTag("extras_swipe_pill_indicator")
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = stringResource(R.string.extras_pill_label),
          style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          softWrap = false,
          modifier = Modifier
            .requiredWidth(92.dp)
            .graphicsLayer { rotationZ = -90f },
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

/**
 * Side-by-side Floating Daily Progress Ring Card for the middle row of Page 0.
 * Styled with a warm cream background (`#FFFBF0`), `8.dp` elevation, and high-contrast text/rings.
 */
@Composable
private fun CompactDailyProgressCard(
  completedCount: Int,
  totalCount: Int,
  metrics: ResponsiveMetrics,
  modifier: Modifier = Modifier
) {
  val rawProgress = if (totalCount > 0) {
    (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
  } else {
    0f
  }

  val animatedProgress by animateFloatAsState(
    targetValue = rawProgress,
    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
    label = "compact_task_progress"
  )
  val percentInt = (rawProgress * 100).toInt()
  val ringDiameter = if (metrics.screenWidthDp < 360) 104.dp else metrics.scaledDp(118)
  val cardContainerColor = dashboardCreamCardColor()
  val ringTrackColor = dashboardProgressTrackColor()

  Card(
    modifier = modifier.testTag("daily_study_progress_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(
      containerColor = cardContainerColor
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 8.dp,
      pressedElevation = 10.dp
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(metrics.scaledDp(14)),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(R.string.study_progress_title),
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(16),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
      }

      Box(
        modifier = Modifier
          .size(ringDiameter)
          .testTag("daily_study_progress_ring")
          .semantics {
            contentDescription = "Daily progress $completedCount of $totalCount tasks completed ($percentInt percent)"
          },
        contentAlignment = Alignment.Center
      ) {
        val progressColor = MaterialTheme.colorScheme.primary

        Canvas(modifier = Modifier.fillMaxSize()) {
          val strokeWidth = 11.dp.toPx()
          val diameter = size.minDimension - strokeWidth
          val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
          val arcSize = Size(diameter, diameter)

          drawArc(
            color = ringTrackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 8.dp)
        ) {
          Text(
            text = "$percentInt%",
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = metrics.scaledSp(20),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "$completedCount/$totalCount Done",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = metrics.scaledSp(11),
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(6))
      ) {
        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .testTag("daily_study_linear_progress"),
          color = MaterialTheme.colorScheme.primary,
          trackColor = ringTrackColor
        )
        Text(
          text = "${(totalCount - completedCount).coerceAtLeast(0)} tasks remaining",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = metrics.scaledSp(11),
            fontWeight = FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

/**
 * Dynamic Floating "Today's Schedule" Card on the Main Dashboard:
 * - Styled with warm cream background (`#FFFBF0`) and `elevation = 8.dp`.
 * - Displays classes from `ViewModel.classSchedules` filtered by `LocalDate.now().dayOfWeek`
 *   ("M", "Tue", "W", "Th", "F", "Sat").
 * - If the filtered list is empty (or COR hasn't been uploaded yet), displays a cleanly
 *   centered "Empty" label inside the card.
 */
@Composable
private fun TodaysScheduleInteractiveCard(
  todaysClasses: List<ClassSchedule>,
  todayAbbreviation: String,
  metrics: ResponsiveMetrics,
  modifier: Modifier = Modifier
) {
  var selectedClassId by rememberSaveable { mutableIntStateOf(todaysClasses.firstOrNull()?.id ?: -1) }
  val cardContainerColor = dashboardCreamCardColor()
  val innerItemColor = dashboardIvoryInnerColor()

  Card(
    modifier = modifier.testTag("todays_schedule_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(
      containerColor = cardContainerColor
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 8.dp,
      pressedElevation = 10.dp
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(metrics.scaledDp(13)),
      verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(R.string.todays_schedule_title),
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(15),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = CircleShape
        ) {
          Text(
            text = "$todayAbbreviation • ${todaysClasses.size}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = metrics.scaledSp(10),
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }
      }

      if (todaysClasses.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .testTag("todays_schedule_empty_box"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Empty",
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = metrics.scaledSp(16),
              fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("todays_schedule_empty_text")
          )
        }
      } else {
        // Scrollable list of today's classes inside the card
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
        ) {
          todaysClasses.forEach { item ->
            val isSelected = selectedClassId == item.id
            val accentColor = Color(item.colorHex)
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { selectedClassId = item.id }
                .testTag("schedule_item_${item.id}"),
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                innerItemColor
              },
              shadowElevation = 1.dp
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(
                    horizontal = metrics.scaledDp(10),
                    vertical = metrics.scaledDp(8)
                  ),
                verticalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(accentColor)
                  )
                  Text(
                    text = item.subjectName,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = metrics.scaledSp(12),
                      fontWeight = FontWeight.Bold
                    ),
                    color = if (isSelected) {
                      MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                      MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                  )
                }

                Text(
                  text = item.timeRange,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = metrics.scaledSp(11),
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.fillMaxWidth()
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = item.room,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = metrics.scaledSp(11),
                      fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
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

/**
 * Floating rounded section Card containing the dynamic "Upcoming Deadlines" list,
 * styled with a warm cream background (`#FFFBF0`) and `elevation = 8.dp`.
 */
@Composable
private fun UpcomingDeadlinesSectionCard(
  activeTasks: List<Task>,
  metrics: ResponsiveMetrics,
  onToggleComplete: (Int) -> Unit
) {
  val cardContainerColor = dashboardCreamCardColor()

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("upcoming_deadlines_section_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = cardContainerColor
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 8.dp,
      pressedElevation = 10.dp
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(metrics.scaledDp(20)),
      verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(R.string.upcoming_deadlines_title),
          style = MaterialTheme.typography.titleLarge.copy(
            fontSize = metrics.scaledSp(19),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .weight(1f)
            .testTag("upcoming_deadlines_header")
        )

        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = CircleShape
        ) {
          Text(
            text = "${activeTasks.size} Active",
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            modifier = Modifier.padding(
              horizontal = metrics.scaledDp(12),
              vertical = metrics.scaledDp(5)
            )
          )
        }
      }

      if (activeTasks.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = metrics.scaledDp(24))
            .testTag("no_upcoming_deadlines_box"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = stringResource(R.string.no_upcoming_deadlines),
            style = MaterialTheme.typography.bodyLarge.copy(
              fontSize = metrics.scaledSp(16),
              fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      } else {
        Column(
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(10))
        ) {
          activeTasks.forEach { task ->
            DashboardTaskMiniCard(
              task = task,
              metrics = metrics,
              onToggleComplete = { onToggleComplete(task.id) }
            )
          }
        }
      }
    }
  }
}

/**
 * Clean, breathable mini-card for each active task in the Upcoming Deadlines section.
 * Uses warm ivory (`#FDFBF7`) in light mode, `Modifier.weight(1f)` on the text column,
 * and `TextOverflow.Ellipsis`.
 */
@Composable
private fun DashboardTaskMiniCard(
  task: Task,
  metrics: ResponsiveMetrics,
  onToggleComplete: () -> Unit
) {
  val innerSurfaceColor = dashboardIvoryInnerColor()

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onToggleComplete)
      .testTag("dashboard_deadline_card_${task.id}"),
    shape = RoundedCornerShape(16.dp),
    color = innerSurfaceColor,
    shadowElevation = 2.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = metrics.scaledDp(16),
          vertical = metrics.scaledDp(14)
        ),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
    ) {
      Icon(
        imageVector = Icons.Filled.RadioButtonUnchecked,
        contentDescription = "Mark ${task.title} complete",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
      )

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = task.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(16),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "Due ${task.dueDate}",
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

/**
 * Cleaned-up Page 1 of the Dashboard HorizontalPager (Extras Screen):
 * Contains ONLY the engineering tool placeholders arranged in a neat 2x2 grid with floating elevation:
 * - Scientific Calculator
 * - Unit Converter
 * - BOQ Estimator
 * - AI Study Assistant
 */
@Composable
fun ExtrasToolsOnlyPage(
  onBackToDashboard: () -> Unit,
  modifier: Modifier = Modifier
) {
  val metrics = rememberResponsiveMetrics()
  var selectedToolTitle by rememberSaveable { mutableStateOf<String?>(null) }

  val extraTools = remember {
    listOf(
      ExtraToolItem(
        id = "scientific_calculator",
        title = "Scientific Calculator",
        subtitle = "Beam & matrix solver",
        icon = Icons.Filled.Calculate
      ),
      ExtraToolItem(
        id = "unit_converter",
        title = "Unit Converter",
        subtitle = "SI & Imperial units",
        icon = Icons.Filled.SwapHoriz
      ),
      ExtraToolItem(
        id = "boq_estimator",
        title = "BOQ Estimator",
        subtitle = "Material & cost takeoff",
        icon = Icons.Filled.RequestQuote
      ),
      ExtraToolItem(
        id = "ai_study_assistant",
        title = "AI Study Assistant",
        subtitle = "CE concept companion",
        icon = Icons.Filled.AutoAwesome
      )
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .widthIn(max = metrics.maxContentWidth)
      .verticalScroll(rememberScrollState())
      .padding(
        horizontal = metrics.scaledDp(20),
        vertical = metrics.scaledDp(24)
      )
      .testTag("extras_tools_page"),
    verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(20))
  ) {
    // Top Header with Back to Dashboard Pill
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = stringResource(R.string.extras_screen_title),
          style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = metrics.scaledSp(26),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("extras_screen_title")
        )
        Text(
          text = "Civil Engineering utilities & companion tools",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = metrics.scaledSp(14),
            fontWeight = FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.fillMaxWidth()
        )
      }

      Surface(
        onClick = onBackToDashboard,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = CircleShape,
        shadowElevation = 4.dp,
        modifier = Modifier.testTag("back_to_dashboard_pill")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Back to Dashboard",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = stringResource(R.string.nav_dashboard),
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }

    // Neat 2x2 Grid of Floating Engineering Tool Placeholder Cards
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
      ) {
        ExtraToolPlaceholderCard(
          tool = extraTools[0],
          metrics = metrics,
          onClick = { selectedToolTitle = extraTools[0].title },
          modifier = Modifier.weight(1f)
        )
        ExtraToolPlaceholderCard(
          tool = extraTools[1],
          metrics = metrics,
          onClick = { selectedToolTitle = extraTools[1].title },
          modifier = Modifier.weight(1f)
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
      ) {
        ExtraToolPlaceholderCard(
          tool = extraTools[2],
          metrics = metrics,
          onClick = { selectedToolTitle = extraTools[2].title },
          modifier = Modifier.weight(1f)
        )
        ExtraToolPlaceholderCard(
          tool = extraTools[3],
          metrics = metrics,
          onClick = { selectedToolTitle = extraTools[3].title },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }

  if (selectedToolTitle != null) {
    AlertDialog(
      onDismissRequest = { selectedToolTitle = null },
      shape = RoundedCornerShape(24.dp),
      containerColor = MaterialTheme.colorScheme.surface,
      title = {
        Text(
          text = selectedToolTitle ?: "",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = metrics.scaledSp(22),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      },
      text = {
        Text(
          text = "$selectedToolTitle module placeholder. Ready for upcoming Civil Engineering calculations.",
          style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15)
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      },
      confirmButton = {
        Button(
          onClick = { selectedToolTitle = null },
          shape = CircleShape
        ) {
          Text(
            text = "Close",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    )
  }
}

@Composable
private fun ExtraToolPlaceholderCard(
  tool: ExtraToolItem,
  metrics: ResponsiveMetrics,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cardContainerColor = dashboardCreamCardColor()

  Card(
    modifier = modifier
      .height(metrics.scaledDp(168))
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .testTag("extra_tool_card_${tool.id}"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(
      containerColor = cardContainerColor
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 8.dp,
      pressedElevation = 12.dp
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(metrics.scaledDp(18)),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(48.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = tool.icon,
            contentDescription = tool.title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
          )
        }
      }

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = tool.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(16),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.fillMaxWidth()
        )

        Text(
          text = tool.subtitle,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = metrics.scaledSp(13),
            fontWeight = FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

/**
 * Fully functional "Schedule" tab featuring:
 * 1. Prominent "Upload COR (Auto-Extract)" button with a 2-second loading state that populates
 *    the ViewModel with the 8 courses from the user's CTU-Danao Certificate of Registration.
 * 2. Interactive Week View horizontal row of selectable Days (Mon, Tue, Wed, Thu, Fri, Sat).
 * 3. Tall, rounded, colored graphic cards displaying Subject Name, Time, and Room Assignment.
 */
@Composable
fun ClassTimetableScheduleScreen(
  viewModel: TaskViewModel,
  modifier: Modifier = Modifier
) {
  val metrics = rememberResponsiveMetrics()
  val coroutineScope = rememberCoroutineScope()
  var selectedWeekDay by rememberSaveable { mutableStateOf(ScheduleWeekDay.MON) }

  val allSchedules = viewModel.classSchedules
  val filteredSchedules = allSchedules.filter { schedule ->
    schedule.days.any { dayToken ->
      dayToken.equals(selectedWeekDay.dayCode, ignoreCase = true) ||
        dayToken.equals(selectedWeekDay.shortLabel, ignoreCase = true)
    }
  }

  LazyColumn(
    modifier = modifier.testTag("schedule_screen_list"),
    contentPadding = PaddingValues(
      start = metrics.scaledDp(18),
      end = metrics.scaledDp(18),
      top = metrics.scaledDp(22),
      bottom = metrics.scaledDp(108)
    ),
    verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(18))
  ) {
    // Top Header Row: "Class Timetable" Title on the left + Subtle "Upload COR" / "COR Synced" Button on the far right
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(R.string.schedule_screen_title),
          style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = metrics.scaledSp(26),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .weight(1f)
            .testTag("schedule_screen_title")
        )

        OutlinedButton(
          onClick = {
            coroutineScope.launch {
              viewModel.extractCorSchedule()
            }
          },
          enabled = !viewModel.isExtractingCor,
          shape = CircleShape,
          contentPadding = PaddingValues(
            horizontal = metrics.scaledDp(12),
            vertical = metrics.scaledDp(6)
          ),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (viewModel.isCorExtracted) {
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            } else {
              MaterialTheme.colorScheme.surface
            },
            contentColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier
            .height(metrics.scaledDp(36))
            .testTag("upload_cor_button")
        ) {
          when {
            viewModel.isExtractingCor -> {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                CircularProgressIndicator(
                  color = MaterialTheme.colorScheme.primary,
                  strokeWidth = 2.dp,
                  modifier = Modifier
                    .size(14.dp)
                    .testTag("cor_loading_indicator")
                )
                Text(
                  text = stringResource(R.string.extracting_cor_label),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = metrics.scaledSp(12),
                    fontWeight = FontWeight.Bold
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
            viewModel.isCorExtracted -> {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = stringResource(R.string.cor_synced_button),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = metrics.scaledSp(12),
                    fontWeight = FontWeight.Bold
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
            else -> {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.UploadFile,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = stringResource(R.string.upload_cor_button),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = metrics.scaledSp(12),
                    fontWeight = FontWeight.Bold
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }

    // 2. Week View Horizontal Row of Selectable Days (Mon, Tue, Wed, Thu, Fri, Sat)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("schedule_week_day_selector"),
        horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(6)),
        verticalAlignment = Alignment.CenterVertically
      ) {
        ScheduleWeekDay.entries.forEach { day ->
          val isSelected = selectedWeekDay == day
          val countForDay = allSchedules.count { sched ->
            sched.days.any { token ->
              token.equals(day.dayCode, ignoreCase = true) ||
                token.equals(day.shortLabel, ignoreCase = true)
            }
          }

          Surface(
            onClick = { selectedWeekDay = day },
            shape = RoundedCornerShape(18.dp),
            color = if (isSelected) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.surface
            },
            shadowElevation = if (isSelected) 8.dp else 3.dp,
            modifier = Modifier
              .weight(1f)
              .height(metrics.scaledDp(66))
              .testTag("week_day_tab_${day.shortLabel.lowercase()}")
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 4.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = day.shortLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = metrics.scaledSp(12),
                  fontWeight = FontWeight.Bold
                ),
                color = if (isSelected) {
                  MaterialTheme.colorScheme.onPrimary
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )

              Surface(
                color = if (isSelected) {
                  MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
                } else {
                  MaterialTheme.colorScheme.primaryContainer
                },
                shape = CircleShape
              ) {
                Text(
                  text = "$countForDay",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = metrics.scaledSp(10),
                    fontWeight = FontWeight.Bold
                  ),
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                  } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  },
                  maxLines = 1,
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }

    // Selected Day Subheader
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${selectedWeekDay.fullLabel} Classes",
          style = MaterialTheme.typography.titleLarge.copy(
            fontSize = metrics.scaledSp(18),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        Text(
          text = "${filteredSchedules.size} scheduled",
          style = MaterialTheme.typography.labelMedium.copy(
            fontSize = metrics.scaledSp(12),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
      }
    }

    // 3. Visual Timetable Cards for the Selected Day
    if (filteredSchedules.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_schedule_day_card"),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(metrics.scaledDp(28)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.EventNote,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(36.dp)
            )
            Text(
              text = if (allSchedules.isEmpty()) {
                "No classes loaded yet"
              } else {
                "No classes on ${selectedWeekDay.fullLabel}"
              },
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = metrics.scaledSp(18),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (allSchedules.isEmpty()) {
                "Tap \"Upload COR (Auto-Extract)\" above to import your CTU-Danao schedule, or tap + to add a class manually."
              } else {
                "Select another day of the week above or tap + to add a class for ${selectedWeekDay.fullLabel}."
              },
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = metrics.scaledSp(14)
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              maxLines = 3,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    } else {
      items(
        items = filteredSchedules,
        key = { "${selectedWeekDay.dayCode}_${it.id}" }
      ) { scheduleItem ->
        ColoredClassScheduleCard(
          schedule = scheduleItem,
          metrics = metrics,
          onDelete = { viewModel.deleteClassSchedule(scheduleItem.id) }
        )
      }
    }
  }
}

/**
 * Tall, rounded, colored graphic card displaying Subject Name, Time, Room Assignment,
 * and scheduled Day badges.
 */
@Composable
private fun ColoredClassScheduleCard(
  schedule: ClassSchedule,
  metrics: ResponsiveMetrics,
  onDelete: () -> Unit
) {
  val cardAccentColor = Color(schedule.colorHex)
  val cardGradientEnd = Color(
    red = (cardAccentColor.red * 0.82f).coerceIn(0f, 1f),
    green = (cardAccentColor.green * 0.82f).coerceIn(0f, 1f),
    blue = (cardAccentColor.blue * 0.82f).coerceIn(0f, 1f),
    alpha = 1f
  )

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = metrics.scaledDp(136))
      .testTag("class_schedule_card_${schedule.id}"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = cardAccentColor
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 10.dp,
      pressedElevation = 12.dp
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.horizontalGradient(
            colors = listOf(cardAccentColor, cardGradientEnd)
          )
        )
        .padding(metrics.scaledDp(20))
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
      ) {
        // Top Row: Day Pills + Delete Icon
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            schedule.days.forEach { dayToken ->
              Surface(
                color = Color.White.copy(alpha = 0.22f),
                shape = CircleShape
              ) {
                Text(
                  text = dayToken,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = metrics.scaledSp(11),
                    fontWeight = FontWeight.Bold
                  ),
                  color = Color.White,
                  maxLines = 1,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(36.dp)
              .testTag("delete_schedule_button_${schedule.id}")
          ) {
            Icon(
              imageVector = Icons.Outlined.DeleteOutline,
              contentDescription = "Remove ${schedule.subjectName}",
              tint = Color.White.copy(alpha = 0.9f),
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Middle: Bold Subject Name (up to 2 lines with ellipsis)
        Text(
          text = schedule.subjectName,
          style = MaterialTheme.typography.titleLarge.copy(
            fontSize = metrics.scaledSp(20),
            lineHeight = metrics.scaledSp(25),
            fontWeight = FontWeight.Bold
          ),
          color = Color.White,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_subject_name_${schedule.id}")
        )

        // Bottom Row: Time Pill + Room Assignment Badge
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = schedule.timeRange,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontSize = metrics.scaledSp(12),
                  fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )
            }
          }

          Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = cardAccentColor,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = schedule.room,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontSize = metrics.scaledSp(12),
                  fontWeight = FontWeight.Bold
                ),
                color = cardAccentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Manual Entry Dialog opened by the Schedule screen's Floating Action Button (FAB).
 * Allows typing Subject Name, Room, Time, and selecting Days of the week via checkboxes.
 */
@Composable
private fun AddClassScheduleDialog(
  onDismiss: () -> Unit,
  onSaveSchedule: (String, String, List<String>, String, Long) -> Unit
) {
  val metrics = rememberResponsiveMetrics()
  var subjectName by rememberSaveable { mutableStateOf("") }
  var timeRange by rememberSaveable { mutableStateOf("") }
  var roomAssignment by rememberSaveable { mutableStateOf("") }

  val selectedDayCodes = remember { mutableStateListOf("M", "Tue") }
  val availableColors = remember {
    listOf(
      0xFF5C6BC0L, // Light Indigo
      0xFFF97316L, // Soft Orange
      0xFF0D9488L, // Muted Teal
      0xFFEF4444L, // Soft Red
      0xFF3B82F6L, // Slate Blue
      0xFF8B5CF6L, // Light Purple
      0xFF10B981L, // Soft Green
      0xFFD97706L  // Amber
    )
  }
  var selectedColorHex by rememberSaveable { mutableLongStateOf(availableColors.first()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_schedule_dialog"),
    shape = RoundedCornerShape(24.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Text(
        text = stringResource(R.string.add_schedule_dialog_title),
        style = MaterialTheme.typography.headlineMedium.copy(
          fontSize = metrics.scaledSp(20),
          fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
      ) {
        OutlinedTextField(
          value = subjectName,
          onValueChange = { subjectName = it },
          label = {
            Text(
              text = stringResource(R.string.subject_name_label),
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          placeholder = {
            Text(
              text = stringResource(R.string.subject_name_placeholder),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = MaterialTheme.colorScheme.onSurface
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_schedule_subject_input")
        )

        OutlinedTextField(
          value = timeRange,
          onValueChange = { timeRange = it },
          label = {
            Text(
              text = stringResource(R.string.schedule_time_label),
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          placeholder = {
            Text(
              text = stringResource(R.string.schedule_time_placeholder),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = MaterialTheme.colorScheme.onSurface
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            imeAction = ImeAction.Next
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_schedule_time_input")
        )

        OutlinedTextField(
          value = roomAssignment,
          onValueChange = { roomAssignment = it },
          label = {
            Text(
              text = stringResource(R.string.schedule_room_label),
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          placeholder = {
            Text(
              text = stringResource(R.string.schedule_room_placeholder),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = MaterialTheme.colorScheme.onSurface
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            imeAction = ImeAction.Done
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_schedule_room_input")
        )

        Text(
          text = stringResource(R.string.select_days_label),
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(14),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurface
        )

        // Checkboxes for Days of the Week (2 rows of 3 for clean responsive fit)
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          ScheduleWeekDay.entries.chunked(3).forEach { rowDays ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              rowDays.forEach { day ->
                val isChecked = selectedDayCodes.contains(day.dayCode)
                Row(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                      if (isChecked) {
                        selectedDayCodes.remove(day.dayCode)
                      } else {
                        selectedDayCodes.add(day.dayCode)
                      }
                    }
                    .padding(vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                      if (checked) {
                        if (!selectedDayCodes.contains(day.dayCode)) {
                          selectedDayCodes.add(day.dayCode)
                        }
                      } else {
                        selectedDayCodes.remove(day.dayCode)
                      }
                    },
                    modifier = Modifier
                      .size(24.dp)
                      .testTag("schedule_day_checkbox_${day.shortLabel.lowercase()}")
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = day.shortLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontSize = metrics.scaledSp(13),
                      fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }

        // Card Color Picker Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          availableColors.forEach { colorHex ->
            val isColorSelected = selectedColorHex == colorHex
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(colorHex))
                .border(
                  width = if (isColorSelected) 3.dp else 0.dp,
                  color = if (isColorSelected) {
                    MaterialTheme.colorScheme.onSurface
                  } else {
                    Color.Transparent
                  },
                  shape = CircleShape
                )
                .clickable { selectedColorHex = colorHex },
              contentAlignment = Alignment.Center
            ) {
              if (isColorSelected) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = "Selected Color",
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSaveSchedule(
            subjectName,
            timeRange,
            selectedDayCodes.toList(),
            roomAssignment,
            selectedColorHex
          )
        },
        enabled = subjectName.isNotBlank(),
        shape = CircleShape,
        modifier = Modifier
          .height(48.dp)
          .testTag("confirm_save_schedule_button")
      ) {
        Text(
          text = stringResource(R.string.save_schedule_button),
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(15),
            fontWeight = FontWeight.Bold
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
        modifier = Modifier
          .height(48.dp)
          .testTag("cancel_save_schedule_button")
      ) {
        Text(
          text = stringResource(R.string.cancel_button),
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(15),
            fontWeight = FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  )
}

/**
 * Cleanly styled popup `Dialog` opened by clicking the Profile Icon in the upper right
 * corner of the Dashboard. Houses the Canva-inspired Account, Display (Dark Mode switch),
 * and About cards with floating elevation (`8.dp`).
 */
@Composable
private fun ProfileAndSettingsDialog(
  viewModel: TaskViewModel,
  onDismiss: () -> Unit,
  onSwitchAccount: () -> Unit
) {
  val metrics = rememberResponsiveMetrics()
  val isGuest = viewModel.userName == "Guest" || viewModel.userEmail.isEmpty()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = if (metrics.screenWidthDp > 600) 600.dp else 520.dp)
        .heightIn(max = 720.dp)
        .testTag("profile_settings_dialog"),
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.background,
      tonalElevation = 6.dp,
      shadowElevation = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(metrics.scaledDp(20)),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
      ) {
        // Dialog Header with Close Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Profile & Settings",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontSize = metrics.scaledSp(22),
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f)
              .testTag("profile_settings_dialog_title")
          )

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(48.dp)
              .minimumInteractiveComponentSize()
              .testTag("close_profile_settings_dialog_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close Settings",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(24.dp)
            )
          }
        }

        // 1. Account Section Card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("extras_account_card"),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(metrics.scaledDp(18)),
            verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
          ) {
            Text(
              text = stringResource(R.string.account_section_title),
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = metrics.scaledSp(18),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
            ) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(54.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Account Profile Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                  )
                }
              }

              if (isGuest) {
                Text(
                  text = stringResource(R.string.guest_account_label),
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = metrics.scaledSp(18),
                    fontWeight = FontWeight.Bold
                  ),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("account_guest_text")
                )
              } else {
                Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                  Text(
                    text = viewModel.userName,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontSize = metrics.scaledSp(16),
                      fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("account_user_name_text")
                  )
                  Text(
                    text = viewModel.userEmail,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontSize = metrics.scaledSp(14)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("account_user_email_text")
                  )
                }
              }
            }

            OutlinedButton(
              onClick = onSwitchAccount,
              shape = CircleShape,
              modifier = Modifier
                .fillMaxWidth()
                .height(metrics.scaledDp(46))
                .testTag("switch_account_button")
            ) {
              Text(
                text = stringResource(R.string.switch_account_button),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontSize = metrics.scaledSp(15),
                  fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        // 2. Display Section Card (with functional Dark Mode Switch)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("extras_display_card"),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(metrics.scaledDp(18)),
            verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
          ) {
            Text(
              text = stringResource(R.string.display_section_title),
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = metrics.scaledSp(18),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { viewModel.updateDarkMode(!viewModel.isDarkMode) }
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.size(40.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Filled.DarkMode,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }

                Text(
                  text = stringResource(R.string.dark_mode_label),
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = metrics.scaledSp(16),
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
              }

              Switch(
                checked = viewModel.isDarkMode,
                onCheckedChange = { isChecked -> viewModel.updateDarkMode(isChecked) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                  checkedTrackColor = MaterialTheme.colorScheme.primary,
                  uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("dark_mode_switch")
              )
            }
          }
        }

        // 3. About Section Card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("extras_about_card"),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(metrics.scaledDp(18)),
            verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
              Text(
                text = stringResource(R.string.about_section_title),
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = metrics.scaledSp(18),
                  fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = metrics.scaledSp(18),
                  fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .weight(1f)
                  .testTag("about_app_name_text")
              )

              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
              ) {
                Text(
                  text = stringResource(R.string.app_version),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = metrics.scaledSp(12),
                    fontWeight = FontWeight.Bold
                  ),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  maxLines = 1,
                  modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .testTag("about_app_version_text")
                )
              }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Text(
              text = stringResource(R.string.developed_by_text),
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = metrics.scaledSp(14),
                fontWeight = FontWeight.SemiBold
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("about_developed_by_text")
            )
          }
        }
      }
    }
  }
}

/**
 * Coursework & Task Manager Screen connected to the shared `TaskViewModel`.
 * Scales padding and typography dynamically via `rememberResponsiveMetrics()`.
 */
@Composable
fun CourseworkTaskManagerScreen(
  viewModel: TaskViewModel,
  modifier: Modifier = Modifier
) {
  val metrics = rememberResponsiveMetrics()
  val tasks = viewModel.tasks
  val remainingCount = tasks.count { !it.isCompleted }

  LazyColumn(
    modifier = modifier.testTag("tasks_lazy_column"),
    contentPadding = PaddingValues(
      start = metrics.scaledDp(20),
      end = metrics.scaledDp(20),
      top = metrics.scaledDp(24),
      bottom = metrics.scaledDp(104)
    ),
    verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
  ) {
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(R.string.tasks_screen_title),
          style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = metrics.scaledSp(26),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .weight(1f)
            .testTag("tasks_screen_title")
        )

        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = CircleShape
        ) {
          Text(
            text = "$remainingCount Active",
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            modifier = Modifier.padding(
              horizontal = metrics.scaledDp(14),
              vertical = metrics.scaledDp(6)
            )
          )
        }
      }
    }

    if (tasks.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .testTag("empty_tasks_card"),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(metrics.scaledDp(28)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "No tasks in your list",
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = metrics.scaledSp(19),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "Tap the + button in the bottom corner to add a task and due date.",
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = metrics.scaledSp(15)
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    } else {
      items(
        items = tasks,
        key = { it.id }
      ) { task ->
        TaskItemRowCard(
          task = task,
          metrics = metrics,
          onToggle = { viewModel.toggleTaskCompletion(task.id) },
          onDelete = { viewModel.deleteTask(task.id) }
        )
      }
    }
  }
}

/**
 * Task row card using `Modifier.weight(1f)` on the middle text column, explicit `Modifier.size()`
 * bounds on the Checkbox and Delete icon buttons, and `maxLines = 2` with `TextOverflow.Ellipsis`.
 */
@Composable
private fun TaskItemRowCard(
  task: Task,
  metrics: ResponsiveMetrics,
  onToggle: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onToggle)
      .testTag("task_item_card_${task.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = if (task.isCompleted) 2.dp else 8.dp
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = metrics.scaledDp(16),
          vertical = metrics.scaledDp(14)
        ),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(10))
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .minimumInteractiveComponentSize(),
        contentAlignment = Alignment.Center
      ) {
        Checkbox(
          checked = task.isCompleted,
          onCheckedChange = { onToggle() },
          colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
            checkmarkColor = MaterialTheme.colorScheme.onPrimary,
            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          modifier = Modifier
            .size(24.dp)
            .testTag("task_checkbox_${task.id}")
        )
      }

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = task.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(16),
            fontWeight = FontWeight.Bold
          ),
          color = if (task.isCompleted) {
            MaterialTheme.colorScheme.onSurfaceVariant
          } else {
            MaterialTheme.colorScheme.onSurface
          },
          textDecoration = if (task.isCompleted) {
            TextDecoration.LineThrough
          } else {
            TextDecoration.None
          },
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("task_title_${task.id}")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = null,
            tint = if (task.isCompleted) {
              MaterialTheme.colorScheme.onSurfaceVariant
            } else {
              MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "Due: ${task.dueDate}",
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.SemiBold
            ),
            color = if (task.isCompleted) {
              MaterialTheme.colorScheme.onSurfaceVariant
            } else {
              MaterialTheme.colorScheme.primary
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
        }
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(48.dp)
          .minimumInteractiveComponentSize()
          .testTag("delete_task_button_${task.id}")
      ) {
        Icon(
          imageVector = Icons.Outlined.DeleteOutline,
          contentDescription = "Delete ${task.title}",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

/**
 * Minimalist Dialog with two `OutlinedTextField` inputs:
 * 1. Task Title
 * 2. Due Date (e.g., Oct 25)
 * Saves both the title and the due date to the new task when clicking "Add".
 */
@Composable
private fun AddTaskDialog(
  onDismiss: () -> Unit,
  onAddTask: (String, String) -> Unit
) {
  val metrics = rememberResponsiveMetrics()
  var taskTitle by rememberSaveable { mutableStateOf("") }
  var taskDueDate by rememberSaveable { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_task_dialog"),
    shape = RoundedCornerShape(24.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Text(
        text = stringResource(R.string.add_task_dialog_title),
        style = MaterialTheme.typography.headlineMedium.copy(
          fontSize = metrics.scaledSp(21),
          fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
      ) {
        OutlinedTextField(
          value = taskTitle,
          onValueChange = { taskTitle = it },
          label = {
            Text(
              text = stringResource(R.string.task_title_label),
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          placeholder = {
            Text(
              text = stringResource(R.string.add_task_input_placeholder),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = MaterialTheme.colorScheme.onSurface
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
          ),
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_task_input")
        )

        OutlinedTextField(
          value = taskDueDate,
          onValueChange = { taskDueDate = it },
          label = {
            Text(
              text = stringResource(R.string.due_date_label),
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          placeholder = {
            Text(
              text = stringResource(R.string.due_date_placeholder),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.CalendarToday,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = MaterialTheme.colorScheme.onSurface
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
          ),
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              if (taskTitle.isNotBlank()) {
                onAddTask(taskTitle, taskDueDate)
              }
            }
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_task_due_date_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onAddTask(taskTitle, taskDueDate) },
        enabled = taskTitle.isNotBlank(),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
          .height(48.dp)
          .testTag("confirm_add_task_button")
      ) {
        Text(
          text = stringResource(R.string.add_button),
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(15),
            fontWeight = FontWeight.Bold
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(horizontal = 10.dp)
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
        modifier = Modifier
          .height(48.dp)
          .testTag("cancel_add_task_button")
      ) {
        Text(
          text = stringResource(R.string.cancel_button),
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(15),
            fontWeight = FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  )
}

/**
 * Simple blank placeholder screen that displays its title centered on the screen.
 */
@Composable
fun PlaceholderScreen(
  title: String,
  testTag: String,
  modifier: Modifier = Modifier
) {
  val metrics = rememberResponsiveMetrics()
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(metrics.scaledDp(24))
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.headlineLarge.copy(
        fontSize = metrics.scaledSp(28),
        fontWeight = FontWeight.Bold
      ),
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Preview(showBackground = true)
@Composable
fun CivilEngineeringProductivityAppPreview() {
  CivilEngineeringProductivityApp()
}

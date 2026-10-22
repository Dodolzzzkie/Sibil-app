package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.ViewList
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
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
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import org.json.JSONArray
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
  val colorHex: Long,
  val startTime: String = extractStartEndTimeTokens(timeRange).first,
  val endTime: String = extractStartEndTimeTokens(timeRange).second
)

/**
 * Selectable Day options for the Week View in the Schedule tab and
 * circular Day chips (`M`, `T`, `W`, `Th`, `F`, `S`) in the Add Class Schedule dialog.
 */
enum class ScheduleWeekDay(
  val shortLabel: String,
  val dayCode: String,
  val chipLabel: String,
  val fullLabel: String
) {
  MON(shortLabel = "Mon", dayCode = "M", chipLabel = "M", fullLabel = "Monday"),
  TUE(shortLabel = "Tue", dayCode = "Tue", chipLabel = "T", fullLabel = "Tuesday"),
  WED(shortLabel = "Wed", dayCode = "W", chipLabel = "W", fullLabel = "Wednesday"),
  THU(shortLabel = "Thu", dayCode = "Th", chipLabel = "Th", fullLabel = "Thursday"),
  FRI(shortLabel = "Fri", dayCode = "F", chipLabel = "F", fullLabel = "Friday"),
  SAT(shortLabel = "Sat", dayCode = "Sat", chipLabel = "S", fullLabel = "Saturday");

  fun matchesToken(token: String): Boolean {
    return token.equals(dayCode, ignoreCase = true) ||
      token.equals(shortLabel, ignoreCase = true) ||
      token.equals(chipLabel, ignoreCase = true) ||
      token.equals(fullLabel, ignoreCase = true)
  }
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
 * Result of validating user credentials against the `SharedPreferences` mock backend.
 */
enum class LoginValidationResult {
  SUCCESS,
  INVALID_EMAIL_FORMAT,
  NO_ACCOUNT_FOUND,
  INCORRECT_PASSWORD
}

/**
 * Strictly validates an email string before login or sign-up using Android's native
 * `android.util.Patterns.EMAIL_ADDRESS` matcher with a standard Regex fallback.
 */
fun isValidEmailFormat(email: String): Boolean {
  val trimmed = email.trim()
  if (trimmed.isEmpty()) return false
  val nativePattern = android.util.Patterns.EMAIL_ADDRESS
  return nativePattern?.matcher(trimmed)?.matches()
    ?: Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(trimmed)
}

/**
 * Lightweight offline data manager wrapping Android's native `SharedPreferences`.
 * Acts as a local mock backend that persists:
 * 1. Registered user accounts (`email` -> `password` map).
 * 2. Account-segregated `Task` lists (`tasks_<email>` or `tasks_guest`).
 * 3. Account-segregated `ClassSchedule` lists (`schedules_<email>` or `schedules_guest`).
 * 4. Active user session (`userName`, `userEmail`) and `isDarkMode`.
 * 5. Explicit Guest data wipe (`clearGuestData()`) when logging out of Guest mode.
 */
class LocalStorageManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  fun normalizeAccountKey(emailOrGuest: String?): String {
    val trimmed = emailOrGuest?.trim().orEmpty()
    return if (trimmed.isEmpty() || trimmed.equals(GUEST_ACCOUNT_KEY, ignoreCase = true)) {
      GUEST_ACCOUNT_KEY
    } else {
      trimmed.lowercase(Locale.US)
    }
  }

  private fun tasksKeyFor(accountKey: String): String {
    return "${KEY_TASKS_PREFIX}${normalizeAccountKey(accountKey)}"
  }

  private fun schedulesKeyFor(accountKey: String): String {
    return "${KEY_SCHEDULES_PREFIX}${normalizeAccountKey(accountKey)}"
  }

  /**
   * Returns all registered users stored in `SharedPreferences` (normalized lowercase email -> password).
   */
  fun getRegisteredUsers(): Map<String, String> {
    val rawJson = prefs.getString(KEY_REGISTERED_USERS, null) ?: return emptyMap()
    return try {
      val jsonObject = JSONObject(rawJson)
      val map = mutableMapOf<String, String>()
      val keys = jsonObject.keys()
      while (keys.hasNext()) {
        val emailKey = keys.next()
        map[emailKey.trim().lowercase(Locale.US)] = jsonObject.optString(emailKey, "")
      }
      map
    } catch (_: Exception) {
      emptyMap()
    }
  }

  /**
   * Checks whether the given email is already registered in `SharedPreferences`.
   */
  fun isEmailRegistered(email: String): Boolean {
    val normalizedEmail = email.trim().lowercase(Locale.US)
    if (normalizedEmail.isEmpty() || normalizedEmail == GUEST_ACCOUNT_KEY) return false
    return getRegisteredUsers().containsKey(normalizedEmail)
  }

  /**
   * Verifies that the email exists and its stored password matches `password`.
   */
  fun verifyUserPassword(email: String, password: String): Boolean {
    val normalizedEmail = email.trim().lowercase(Locale.US)
    if (normalizedEmail.isEmpty()) return false
    val savedPassword = getRegisteredUsers()[normalizedEmail] ?: return false
    return savedPassword == password
  }

  /**
   * Registers a new user account (`email` -> `password`) in `SharedPreferences`.
   */
  fun registerUser(email: String, password: String) {
    val normalizedEmail = email.trim().lowercase(Locale.US)
    if (normalizedEmail.isEmpty() || normalizedEmail == GUEST_ACCOUNT_KEY) return
    val currentUsers = getRegisteredUsers().toMutableMap()
    currentUsers[normalizedEmail] = password
    val jsonObject = JSONObject()
    currentUsers.forEach { (k, v) -> jsonObject.put(k, v) }
    prefs.edit().putString(KEY_REGISTERED_USERS, jsonObject.toString()).apply()
  }

  fun saveTasks(tasks: List<Task>, accountKey: String = GUEST_ACCOUNT_KEY) {
    val jsonArray = JSONArray()
    for (task in tasks) {
      val obj = JSONObject().apply {
        put("id", task.id)
        put("title", task.title)
        put("dueDate", task.dueDate)
        put("isCompleted", task.isCompleted)
      }
      jsonArray.put(obj)
    }
    prefs.edit().putString(tasksKeyFor(accountKey), jsonArray.toString()).apply()
  }

  fun loadTasks(accountKey: String = GUEST_ACCOUNT_KEY): List<Task>? {
    val storageKey = tasksKeyFor(accountKey)
    if (!prefs.contains(storageKey)) return null
    val rawJson = prefs.getString(storageKey, null) ?: return null
    return try {
      val jsonArray = JSONArray(rawJson)
      val loaded = mutableListOf<Task>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.optJSONObject(i) ?: continue
        loaded.add(
          Task(
            id = obj.optInt("id", i + 1),
            title = obj.optString("title", ""),
            dueDate = obj.optString("dueDate", "Upcoming"),
            isCompleted = obj.optBoolean("isCompleted", false)
          )
        )
      }
      loaded
    } catch (_: Exception) {
      null
    }
  }

  fun saveSchedules(schedules: List<ClassSchedule>, accountKey: String = GUEST_ACCOUNT_KEY) {
    val jsonArray = JSONArray()
    for (schedule in schedules) {
      val daysArray = JSONArray()
      schedule.days.forEach { day -> daysArray.put(day) }
      val obj = JSONObject().apply {
        put("id", schedule.id)
        put("subjectName", schedule.subjectName)
        put("timeRange", schedule.timeRange)
        put("days", daysArray)
        put("room", schedule.room)
        put("colorHex", schedule.colorHex)
        put("startTime", schedule.startTime)
        put("endTime", schedule.endTime)
      }
      jsonArray.put(obj)
    }
    prefs.edit().putString(schedulesKeyFor(accountKey), jsonArray.toString()).apply()
  }

  fun loadSchedules(accountKey: String = GUEST_ACCOUNT_KEY): List<ClassSchedule>? {
    val storageKey = schedulesKeyFor(accountKey)
    if (!prefs.contains(storageKey)) return null
    val rawJson = prefs.getString(storageKey, null) ?: return null
    return try {
      val jsonArray = JSONArray(rawJson)
      val loaded = mutableListOf<ClassSchedule>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.optJSONObject(i) ?: continue
        val daysArray = obj.optJSONArray("days")
        val daysList = mutableListOf<String>()
        if (daysArray != null) {
          for (j in 0 until daysArray.length()) {
            val dayToken = daysArray.optString(j, "").trim()
            if (dayToken.isNotEmpty()) {
              daysList.add(dayToken)
            }
          }
        }
        val timeRange = obj.optString("timeRange", "7:00 AM - 9:00 AM")
        val (fallbackStart, fallbackEnd) = extractStartEndTimeTokens(timeRange)
        loaded.add(
          ClassSchedule(
            id = obj.optInt("id", 100 + i),
            subjectName = obj.optString("subjectName", ""),
            timeRange = timeRange,
            days = if (daysList.isEmpty()) listOf("M") else daysList,
            room = obj.optString("room", "ENG. 307"),
            colorHex = obj.optLong("colorHex", 0xFF00C4CCL),
            startTime = obj.optString("startTime", fallbackStart).ifBlank { fallbackStart },
            endTime = obj.optString("endTime", fallbackEnd).ifBlank { fallbackEnd }
          )
        )
      }
      loaded
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Explicitly deletes all temporary data associated with the `"guest"` keys
   * (`tasks_guest` and `schedules_guest`) when logging out of Guest mode.
   */
  fun clearGuestData() {
    val editor = prefs.edit()
      .remove(tasksKeyFor(GUEST_ACCOUNT_KEY))
      .remove(schedulesKeyFor(GUEST_ACCOUNT_KEY))
    prefs.all.keys
      .filter { key ->
        key.endsWith("_$GUEST_ACCOUNT_KEY")
      }
      .forEach { key ->
        editor.remove(key)
      }
    editor.apply()
  }

  fun saveUserProfile(userName: String, userEmail: String) {
    prefs.edit()
      .putString(KEY_USER_NAME, userName)
      .putString(KEY_USER_EMAIL, userEmail)
      .apply()
  }

  fun loadUserName(): String? {
    val saved = prefs.getString(KEY_USER_NAME, null)
    return saved?.takeIf { it.isNotBlank() }
  }

  fun loadUserEmail(): String? {
    return prefs.getString(KEY_USER_EMAIL, null)
  }

  fun clearUserProfile() {
    prefs.edit()
      .remove(KEY_USER_NAME)
      .remove(KEY_USER_EMAIL)
      .apply()
  }

  fun saveDarkMode(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
  }

  fun loadDarkMode(defaultValue: Boolean = false): Boolean {
    return prefs.getBoolean(KEY_DARK_MODE, defaultValue)
  }

  companion object {
    const val GUEST_ACCOUNT_KEY = "guest"
    private const val PREFS_NAME = "sibil_app_offline_storage"
    private const val KEY_REGISTERED_USERS = "registered_users_json"
    private const val KEY_TASKS_PREFIX = "tasks_"
    private const val KEY_SCHEDULES_PREFIX = "schedules_"
    private const val KEY_USER_NAME = "saved_user_name"
    private const val KEY_USER_EMAIL = "saved_user_email"
    private const val KEY_DARK_MODE = "saved_dark_mode"
  }
}

/**
 * Shared ViewModel storing and managing:
 * 1. Global theme state (`isDarkMode`, default `false`).
 * 2. User session data (`userName`, `userEmail`, and `hasSavedSession`).
 * 3. Live Open-Meteo weather summary (`weatherText`, starting at `"Fetching Weather..."`).
 * 4. Account-segregated coursework tasks shared across the Academic Dashboard and Task Manager.
 * 5. Today's Schedule list on the Dashboard.
 * 6. Account-segregated `ClassSchedule` items for the colored Week Timetable & Grid View.
 * 7. Simulated Authentication & Guest Mode data wipe via `LocalStorageManager` (`SharedPreferences`).
 */
class TaskViewModel(
  private var storageManager: LocalStorageManager? = null
) : ViewModel() {
  var isDarkMode by mutableStateOf(false)
    private set

  var userName by mutableStateOf("Alberto Jr. M. Manzo")
    private set

  var userEmail by mutableStateOf("alberto@civil.edu")
    private set

  var hasSavedSession by mutableStateOf(false)
    private set

  var weatherText by mutableStateOf("Fetching Weather...")
    private set

  var hasRequestedInitialWeather by mutableStateOf(false)
    private set

  private var nextId by mutableIntStateOf(6)
  private var nextScheduleId by mutableIntStateOf(100)
  private var hasLoadedFromStorage = false

  val isGuestMode: Boolean
    get() = userName.equals("Guest", ignoreCase = true) ||
      userEmail.isBlank() ||
      userEmail.equals(LocalStorageManager.GUEST_ACCOUNT_KEY, ignoreCase = true)

  val currentAccountKey: String
    get() = if (isGuestMode) {
      LocalStorageManager.GUEST_ACCOUNT_KEY
    } else {
      userEmail.trim().lowercase(Locale.US)
    }

  private fun buildDefaultStarterTasks(): List<Task> = listOf(
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

  private val _tasks = mutableStateListOf<Task>().apply {
    addAll(buildDefaultStarterTasks())
  }
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

  init {
    storageManager?.let { loadFromStorage(it) }
  }

  /**
   * Attaches a `LocalStorageManager` if the `ViewModel` was constructed without one,
   * immediately loading saved tasks, schedules, theme preference, and user login state.
   */
  fun attachStorage(manager: LocalStorageManager) {
    if (storageManager == null) {
      storageManager = manager
    }
    if (!hasLoadedFromStorage) {
      loadFromStorage(manager)
    }
  }

  private fun loadFromStorage(manager: LocalStorageManager) {
    hasLoadedFromStorage = true

    isDarkMode = manager.loadDarkMode(defaultValue = isDarkMode)

    val savedUserName = manager.loadUserName()
    if (!savedUserName.isNullOrBlank()) {
      userName = savedUserName
      userEmail = manager.loadUserEmail() ?: ""
      hasSavedSession = true
      loadAccountDataForKey(manager, currentAccountKey)
    } else {
      hasSavedSession = false
    }
  }

  private fun loadAccountDataForKey(manager: LocalStorageManager, accountKey: String) {
    val savedTasks = manager.loadTasks(accountKey)
    if (savedTasks != null) {
      _tasks.clear()
      _tasks.addAll(savedTasks)
      nextId = (savedTasks.maxOfOrNull { it.id } ?: 0) + 1
    } else {
      val starterTasks = buildDefaultStarterTasks()
      _tasks.clear()
      _tasks.addAll(starterTasks)
      nextId = 6
      manager.saveTasks(_tasks, accountKey)
    }

    val savedSchedules = manager.loadSchedules(accountKey)
    if (savedSchedules != null) {
      val sorted = savedSchedules.sortedBy { parseTimeToFloat(it.startTime) }
      _classSchedules.clear()
      _classSchedules.addAll(sorted)
      nextScheduleId = (savedSchedules.maxOfOrNull { it.id } ?: 99) + 1
    } else {
      _classSchedules.clear()
      nextScheduleId = 100
    }
  }

  /**
   * Filters `classSchedules` for the selected `ScheduleWeekDay` and actively sorts them
   * in chronological order based on Start Time before displaying them.
   */
  fun getSchedulesForDay(weekDay: ScheduleWeekDay): List<ClassSchedule> {
    return _classSchedules
      .filter { schedule ->
        schedule.days.any { dayToken -> weekDay.matchesToken(dayToken) }
      }
      .sortedBy { schedule ->
        parseTimeToFloat(schedule.startTime)
      }
  }

  /**
   * Filters `classSchedules` for a day abbreviation (e.g., today's day on the Dashboard)
   * and sorts them in chronological order based on Start Time.
   */
  fun getSchedulesForDayAbbreviation(dayAbbreviation: String): List<ClassSchedule> {
    return _classSchedules
      .filter { schedule ->
        schedule.days.any { dayToken ->
          doesDayTokenMatchAbbreviation(dayToken, dayAbbreviation)
        }
      }
      .sortedBy { schedule ->
        parseTimeToFloat(schedule.startTime)
      }
  }

  fun addClassSchedule(
    subjectName: String,
    timeRange: String,
    days: List<String>,
    room: String,
    colorHex: Long
  ) {
    val trimmedSubject = subjectName.trim()
    val trimmedTime = timeRange.trim().ifEmpty { "7:00 AM - 9:00 AM" }
    val trimmedRoom = room.trim().ifEmpty { "ENG. 307" }
    val normalizedDays = if (days.isEmpty()) listOf("M") else days
    val (parsedStart, parsedEnd) = extractStartEndTimeTokens(trimmedTime)

    if (trimmedSubject.isNotEmpty()) {
      _classSchedules.add(
        ClassSchedule(
          id = nextScheduleId++,
          subjectName = trimmedSubject,
          timeRange = trimmedTime,
          days = normalizedDays,
          room = trimmedRoom,
          colorHex = colorHex,
          startTime = parsedStart,
          endTime = parsedEnd
        )
      )
      val sorted = _classSchedules.sortedBy {
        parseTimeToFloat(it.startTime)
      }
      _classSchedules.clear()
      _classSchedules.addAll(sorted)
      storageManager?.saveSchedules(_classSchedules, currentAccountKey)
    }
  }

  fun updateClassSchedule(
    scheduleId: Int,
    subjectName: String,
    timeRange: String,
    days: List<String>,
    room: String,
    colorHex: Long
  ) {
    val index = _classSchedules.indexOfFirst { it.id == scheduleId }
    val trimmedSubject = subjectName.trim()
    val trimmedTime = timeRange.trim().ifEmpty { "7:00 AM - 9:00 AM" }
    val trimmedRoom = room.trim().ifEmpty { "ENG. 307" }
    val normalizedDays = if (days.isEmpty()) listOf("M") else days
    val (parsedStart, parsedEnd) = extractStartEndTimeTokens(trimmedTime)

    if (index != -1 && trimmedSubject.isNotEmpty()) {
      _classSchedules[index] = _classSchedules[index].copy(
        subjectName = trimmedSubject,
        timeRange = trimmedTime,
        days = normalizedDays,
        room = trimmedRoom,
        colorHex = colorHex,
        startTime = parsedStart,
        endTime = parsedEnd
      )
      val sorted = _classSchedules.sortedBy {
        parseTimeToFloat(it.startTime)
      }
      _classSchedules.clear()
      _classSchedules.addAll(sorted)
      storageManager?.saveSchedules(_classSchedules, currentAccountKey)
    }
  }

  fun deleteClassSchedule(scheduleId: Int) {
    _classSchedules.removeAll { it.id == scheduleId }
    storageManager?.saveSchedules(_classSchedules, currentAccountKey)
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
    storageManager?.saveDarkMode(enabled)
  }

  /**
   * Validates `typedEmail` and `typedPassword` against the `SharedPreferences` registered users map.
   * - Validates email format first and returns `INVALID_EMAIL_FORMAT` if invalid.
   * - Returns `NO_ACCOUNT_FOUND` if the email is not registered.
   * - Returns `INCORRECT_PASSWORD` if the email is registered but the password does not match.
   * - Logs the user in and returns `SUCCESS` when valid.
   */
  fun attemptLogin(typedEmail: String, typedPassword: String): LoginValidationResult {
    val cleanEmail = typedEmail.trim().lowercase(Locale.US)
    if (!isValidEmailFormat(cleanEmail)) {
      return LoginValidationResult.INVALID_EMAIL_FORMAT
    }
    val manager = storageManager
    if (manager == null || !manager.isEmailRegistered(cleanEmail)) {
      return LoginValidationResult.NO_ACCOUNT_FOUND
    }
    if (!manager.verifyUserPassword(cleanEmail, typedPassword)) {
      return LoginValidationResult.INCORRECT_PASSWORD
    }
    loginWithEmail(cleanEmail)
    return LoginValidationResult.SUCCESS
  }

  /**
   * Checks whether `email` is already registered in `SharedPreferences`.
   */
  fun isEmailRegistered(email: String): Boolean {
    return storageManager?.isEmailRegistered(email.trim()) == true
  }

  /**
   * Saves a newly registered account (`email` -> `password`) to `SharedPreferences`
   * and immediately logs the user in with their segregated storage keys.
   */
  fun completeSignUp(email: String, password: String) {
    val cleanEmail = email.trim().lowercase(Locale.US)
    storageManager?.registerUser(cleanEmail, password)
    loginWithEmail(cleanEmail)
  }

  fun loginWithEmail(typedEmail: String) {
    val cleanEmail = typedEmail.trim().lowercase(Locale.US).ifEmpty { "alberto@civil.edu" }
    userName = "Alberto Jr. M. Manzo"
    userEmail = cleanEmail
    hasSavedSession = true
    storageManager?.let { manager ->
      manager.saveUserProfile(userName, userEmail)
      loadAccountDataForKey(manager, currentAccountKey)
    }
  }

  fun loginAsGuest() {
    userName = "Guest"
    userEmail = ""
    hasSavedSession = true
    storageManager?.let { manager ->
      manager.saveUserProfile(userName, userEmail)
      loadAccountDataForKey(manager, LocalStorageManager.GUEST_ACCOUNT_KEY)
    }
  }

  /**
   * Logs out the current user.
   * - If logging out from Guest mode, explicitly deletes all temporary data associated with
   *   the `"guest"` keys (`tasks_guest` and `schedules_guest`) in `SharedPreferences`.
   * - Registered accounts logging out keep all of their saved Tasks and Schedules intact.
   */
  fun logout() {
    val wasGuest = isGuestMode
    hasSavedSession = false
    storageManager?.let { manager ->
      if (wasGuest) {
        manager.clearGuestData()
      }
      manager.clearUserProfile()
    }
    if (wasGuest) {
      _tasks.clear()
      _tasks.addAll(buildDefaultStarterTasks())
      _classSchedules.clear()
      nextId = 6
      nextScheduleId = 100
    }
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
      storageManager?.saveTasks(_tasks, currentAccountKey)
    }
  }

  fun toggleTaskCompletion(taskId: Int) {
    val index = _tasks.indexOfFirst { it.id == taskId }
    if (index != -1) {
      val current = _tasks[index]
      _tasks[index] = current.copy(isCompleted = !current.isCompleted)
      storageManager?.saveTasks(_tasks, currentAccountKey)
    }
  }

  fun deleteTask(taskId: Int) {
    _tasks.removeAll { it.id == taskId }
    storageManager?.saveTasks(_tasks, currentAccountKey)
  }

  companion object {
    fun provideFactory(storageManager: LocalStorageManager): ViewModelProvider.Factory {
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return TaskViewModel(storageManager) as T
        }
      }
    }
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
 * Resolves the initial `ScheduleWeekDay` tab to highlight based on today's real-world day of week
 * (falling back to Monday on Sundays).
 */
@SuppressLint("NewApi")
private fun resolveInitialScheduleWeekDay(): ScheduleWeekDay {
  return when (LocalDate.now().dayOfWeek) {
    DayOfWeek.MONDAY -> ScheduleWeekDay.MON
    DayOfWeek.TUESDAY -> ScheduleWeekDay.TUE
    DayOfWeek.WEDNESDAY -> ScheduleWeekDay.WED
    DayOfWeek.THURSDAY -> ScheduleWeekDay.THU
    DayOfWeek.FRIDAY -> ScheduleWeekDay.FRI
    DayOfWeek.SATURDAY -> ScheduleWeekDay.SAT
    DayOfWeek.SUNDAY -> ScheduleWeekDay.MON
  }
}

/**
 * Checks whether a class schedule day token (e.g., "M", "T", "Tue", "W", "Th", "F", "S", "Sat")
 * matches the target day abbreviation (e.g., today's abbreviation on the Dashboard).
 */
private fun doesDayTokenMatchAbbreviation(token: String, targetAbbreviation: String): Boolean {
  if (token.equals(targetAbbreviation, ignoreCase = true)) return true
  val matchingWeekDay = ScheduleWeekDay.entries.firstOrNull {
    it.dayCode.equals(targetAbbreviation, ignoreCase = true) ||
      it.chipLabel.equals(targetAbbreviation, ignoreCase = true) ||
      it.shortLabel.equals(targetAbbreviation, ignoreCase = true)
  }
  return matchingWeekDay?.matchesToken(token) == true
}

/**
 * Safely parses standard time inputs (e.g., "7:00 AM", "07:30 AM", "1:00 PM", "12:00 PM")
 * and converts them into a 24-hour float:
 * - "7:00 AM"  -> 7.0f
 * - "7:30 AM"  -> 7.5f
 * - "1:00 PM"  -> 13.0f
 * - "12:00 PM" -> 12.0f
 * If parsing fails, defaults to 7.0f to prevent crashes.
 */
fun parseTimeToFloat(timeString: String): Float {
  return try {
    val trimmed = timeString.trim()
    if (trimmed.isEmpty()) return 7.0f

    val upper = trimmed.uppercase(Locale.US)
    val isPm = upper.contains("PM")
    val isAm = upper.contains("AM")

    val clockPortion = upper
      .replace("AM", "")
      .replace("PM", "")
      .trim()

    if (clockPortion.isEmpty()) return 7.0f

    val hour: Int
    val minute: Int

    if (clockPortion.contains(":")) {
      val parts = clockPortion.split(":")
      hour = parts.getOrNull(0)?.trim()?.filter { it.isDigit() }?.toIntOrNull() ?: return 7.0f
      minute = (parts.getOrNull(1)?.trim()?.filter { it.isDigit() }?.toIntOrNull() ?: 0)
        .coerceIn(0, 59)
    } else {
      val digits = clockPortion.filter { it.isDigit() }
      when (digits.length) {
        1, 2 -> {
          hour = digits.toIntOrNull() ?: return 7.0f
          minute = 0
        }
        3 -> {
          hour = digits.substring(0, 1).toIntOrNull() ?: return 7.0f
          minute = (digits.substring(1, 3).toIntOrNull() ?: 0).coerceIn(0, 59)
        }
        4 -> {
          hour = digits.substring(0, 2).toIntOrNull() ?: return 7.0f
          minute = (digits.substring(2, 4).toIntOrNull() ?: 0).coerceIn(0, 59)
        }
        else -> return 7.0f
      }
    }

    if (hour !in 0..23) return 7.0f

    val hour24 = when {
      hour in 13..23 -> hour
      isPm -> if (hour == 12) 12 else hour + 12
      isAm -> if (hour == 12) 12 else hour
      else -> {
        when {
          hour == 12 -> 12
          hour in 7..11 -> hour
          hour in 1..6 -> hour + 12
          else -> hour
        }
      }
    }

    hour24.toFloat() + (minute.toFloat() / 60.0f)
  } catch (_: Exception) {
    7.0f
  }
}

/**
 * Splits a raw `timeRange` string (e.g., `"7:00 AM - 9:00 AM"`) into `(startTime, endTime)`.
 */
private fun extractStartEndTimeTokens(timeRange: String): Pair<String, String> {
  val parts = timeRange.split("-", "–")
  val startToken = parts.getOrNull(0)?.trim().orEmpty().ifEmpty { "7:00 AM" }
  val endToken = parts.getOrNull(1)?.trim().orEmpty().ifEmpty { "9:00 AM" }
  return startToken to endToken
}

/**
 * Infers the academic daytime meridiem ("AM" for 7..11, "PM" for 12 and 1..6) as the user types
 * into the Start Time or End Time field so the AM/PM toggle automatically assists them.
 */
private fun inferAcademicMeridiem(rawTimeInput: String): String? {
  val trimmed = rawTimeInput.trim()
  if (trimmed.isEmpty()) return null
  val upper = trimmed.uppercase(Locale.US)
  if (upper.contains("PM")) return "PM"
  if (upper.contains("AM")) return "AM"

  val hour = if (trimmed.contains(":")) {
    trimmed.substringBefore(":").filter { it.isDigit() }.toIntOrNull()
  } else {
    val digits = trimmed.filter { it.isDigit() }
    when (digits.length) {
      1, 2 -> digits.toIntOrNull()
      3 -> digits.substring(0, 1).toIntOrNull()
      4 -> digits.substring(0, 2).toIntOrNull()
      else -> null
    }
  } ?: return null

  return when (hour) {
    in 7..11 -> "AM"
    12, in 1..6 -> "PM"
    in 13..23 -> "PM"
    else -> null
  }
}

/**
 * Preserves the user's raw clock input paired with the selected AM/PM toggle
 * (e.g., `"7:00"` + `"AM"` -> `"7:00 AM"`, `"9:00"` + `"AM"` -> `"9:00 AM"`,
 * `"7:00 AM - 9:00 AM"`) without forcibly zero-padding or clamping digits.
 */
private fun formatStructuredTimeRange(
  startInput: String,
  startMeridiem: String,
  endInput: String,
  endMeridiem: String
): String {
  fun buildSingleToken(raw: String, meridiem: String, fallbackClock: String): String {
    val trimmed = raw.trim()
    val cleanMeridiem = if (meridiem.equals("PM", ignoreCase = true)) "PM" else "AM"
    if (trimmed.isEmpty()) {
      return "$fallbackClock $cleanMeridiem"
    }

    val explicitAmPm = when {
      trimmed.uppercase(Locale.US).contains("PM") -> "PM"
      trimmed.uppercase(Locale.US).contains("AM") -> "AM"
      else -> null
    }
    val resolvedMeridiem = explicitAmPm ?: cleanMeridiem

    val clockOnly = trimmed
      .replace(Regex("""(?i)\s*(am|pm)"""), "")
      .trim()

    if (clockOnly.isEmpty()) {
      return "$fallbackClock $resolvedMeridiem"
    }

    // Preserve the user's raw clock string if they included ':'
    if (clockOnly.contains(":")) {
      return "$clockOnly $resolvedMeridiem"
    }

    // If the user typed only digits without ':' (e.g., "7" -> "7:00", "730" -> "7:30", "1030" -> "10:30")
    val digits = clockOnly.filter { it.isDigit() }
    val normalizedClock = when (digits.length) {
      1, 2 -> "$digits:00"
      3 -> "${digits.substring(0, 1)}:${digits.substring(1, 3)}"
      4 -> "${digits.substring(0, 2)}:${digits.substring(2, 4)}"
      else -> clockOnly
    }
    return "$normalizedClock $resolvedMeridiem"
  }

  val startFormatted = buildSingleToken(startInput, startMeridiem, fallbackClock = "7:00")
  val endFormatted = buildSingleToken(endInput, endMeridiem, fallbackClock = "9:00")
  return "$startFormatted - $endFormatted"
}

private data class StructuredTimeRangeInputs(
  val startTime: String,
  val startMeridiem: String,
  val endTime: String,
  val endMeridiem: String
)

/**
 * Parses a saved `timeRange` string (e.g., `"7:00 AM - 9:00 AM"`, `"1:00 PM - 3:00 PM"`)
 * back into structured `startTime`, `startMeridiem`, `endTime`, and `endMeridiem` fields
 * while preserving the user's raw clock text.
 */
private fun parseTimeRangeToStructuredInputs(timeRange: String): StructuredTimeRangeInputs {
  val (startToken, endToken) = extractStartEndTimeTokens(timeRange)

  fun splitClockAndMeridiem(token: String, fallbackMeridiem: String): Pair<String, String> {
    val upper = token.uppercase(Locale.US)
    val meridiem = when {
      upper.contains("PM") -> "PM"
      upper.contains("AM") -> "AM"
      else -> inferAcademicMeridiem(token) ?: fallbackMeridiem
    }
    val rawClock = token
      .replace(Regex("""(?i)\s*(am|pm)"""), "")
      .trim()
      .ifEmpty { "7:00" }
    return rawClock to meridiem
  }

  val (startClock, startAmPm) = splitClockAndMeridiem(startToken, "AM")
  val (endClock, endAmPm) = splitClockAndMeridiem(endToken, startAmPm)
  return StructuredTimeRangeInputs(
    startTime = startClock,
    startMeridiem = startAmPm,
    endTime = endClock,
    endMeridiem = endAmPm
  )
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
  taskViewModel: TaskViewModel? = null
) {
  val context = LocalContext.current
  val storageManager = remember(context) {
    LocalStorageManager(context.applicationContext ?: context)
  }
  val resolvedViewModel: TaskViewModel = taskViewModel ?: viewModel(
    factory = TaskViewModel.provideFactory(storageManager)
  )
  remember(resolvedViewModel, storageManager) {
    resolvedViewModel.attachStorage(storageManager)
    true
  }

  // Dynamically switch between darkColorScheme() and lightColorScheme() based on ViewModel.isDarkMode
  val dynamicColorScheme = if (resolvedViewModel.isDarkMode) {
    CanvaDarkColorScheme
  } else {
    CanvaLightColorScheme
  }

  MaterialTheme(
    colorScheme = dynamicColorScheme,
    typography = Typography
  ) {
    // Top-level navigation state: if a userName is already saved in SharedPreferences,
    // bypass the Login screen entirely and go straight to the Dashboard.
    var isLoggedIn by rememberSaveable { mutableStateOf(resolvedViewModel.hasSavedSession) }

    Crossfade(
      targetState = isLoggedIn,
      animationSpec = tween(durationMillis = 300),
      label = "auth_to_main_transition"
    ) { loggedIn ->
      if (!loggedIn) {
        StartupLoginScreen(
          onAttemptLogin = { typedEmail, typedPassword ->
            val result = resolvedViewModel.attemptLogin(typedEmail, typedPassword)
            if (result == LoginValidationResult.SUCCESS) {
              isLoggedIn = true
            }
            result
          },
          onCheckEmailRegistered = { emailToCheck ->
            resolvedViewModel.isEmailRegistered(emailToCheck)
          },
          onCompleteSignUp = { registeredEmail, createdPassword ->
            resolvedViewModel.completeSignUp(registeredEmail, createdPassword)
            isLoggedIn = true
          },
          onContinueAsGuest = {
            resolvedViewModel.loginAsGuest()
            isLoggedIn = true
          }
        )
      } else {
        MainProductivityScaffold(
          taskViewModel = resolvedViewModel,
          onLogout = {
            resolvedViewModel.logout()
            isLoggedIn = false
          }
        )
      }
    }
  }
}

/**
 * Canva-Inspired Startup / Login Screen with Simulated Authentication & Sign-Up Verification Flow:
 * - Validates "Log In" against `SharedPreferences` registered accounts:
 *   * Displays bold red `"No account found"` if email is not in `SharedPreferences`.
 *   * Displays bold red `"Incorrect password"` if email exists but password does not match.
 * - Includes a `"Sign Up"` button opening the 4-step Sign-Up & Verification Dialog.
 * - Includes `"Continue as Guest"` which loads segregated `tasks_guest` and `schedules_guest` keys.
 */
@Composable
fun StartupLoginScreen(
  onAttemptLogin: (String, String) -> LoginValidationResult,
  onCheckEmailRegistered: (String) -> Boolean,
  onCompleteSignUp: (String, String) -> Unit,
  onContinueAsGuest: () -> Unit,
  modifier: Modifier = Modifier
) {
  val metrics = rememberResponsiveMetrics()
  var email by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
  var loginErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
  var isSignUpDialogOpen by rememberSaveable { mutableStateOf(false) }
  var isGuestWarningDialogOpen by rememberSaveable { mutableStateOf(false) }

  val topGradientColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
  val baseBackground = MaterialTheme.colorScheme.background

  fun handleLoginClick() {
    if (!isValidEmailFormat(email)) {
      loginErrorMessage = "Invalid email format"
      return
    }
    when (onAttemptLogin(email, password)) {
      LoginValidationResult.INVALID_EMAIL_FORMAT -> {
        loginErrorMessage = "Invalid email format"
      }
      LoginValidationResult.NO_ACCOUNT_FOUND -> {
        loginErrorMessage = "No account found"
      }
      LoginValidationResult.INCORRECT_PASSWORD -> {
        loginErrorMessage = "Incorrect password"
      }
      LoginValidationResult.SUCCESS -> {
        loginErrorMessage = null
      }
    }
  }

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
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
        ) {
          // Email OutlinedTextField
          OutlinedTextField(
            value = email,
            onValueChange = {
              email = it
              if (loginErrorMessage != null) loginErrorMessage = null
            },
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
            onValueChange = {
              password = it
              if (loginErrorMessage != null) loginErrorMessage = null
            },
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
              onDone = { handleLoginClick() }
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_password_input")
          )

          // Bold, red validation error text below the fields ("No account found" / "Incorrect password")
          loginErrorMessage?.let { errorText ->
            Text(
              text = errorText,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = metrics.scaledSp(14),
                fontWeight = FontWeight.Bold
              ),
              color = Color(0xFFD32F2F),
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("login_error_text")
            )
          }

          Spacer(modifier = Modifier.height(2.dp))

          // Row / Column of Primary "Log In" and Secondary "Sign Up" Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(10)),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = { handleLoginClick() },
              shape = CircleShape,
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              ),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
              modifier = Modifier
                .weight(1f)
                .height(metrics.scaledDp(52))
                .testTag("login_submit_button")
            ) {
              Text(
                text = stringResource(R.string.login_button),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontSize = metrics.scaledSp(16),
                  fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            OutlinedButton(
              onClick = {
                loginErrorMessage = null
                isSignUpDialogOpen = true
              },
              shape = CircleShape,
              border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
              modifier = Modifier
                .weight(1f)
                .height(metrics.scaledDp(52))
                .testTag("sign_up_button")
            ) {
              Text(
                text = "Sign Up",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontSize = metrics.scaledSp(16),
                  fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // Pill-Shaped Secondary "Continue as Guest" TextButton (opens Guest Mode Warning Dialog)
          TextButton(
            onClick = {
              loginErrorMessage = null
              isGuestWarningDialogOpen = true
            },
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

    if (isGuestWarningDialogOpen) {
      GuestModeWarningDialog(
        onCancel = { isGuestWarningDialogOpen = false },
        onProceed = {
          isGuestWarningDialogOpen = false
          onContinueAsGuest()
        }
      )
    }

    if (isSignUpDialogOpen) {
      SignUpVerificationDialog(
        initialEmail = email,
        onCheckEmailRegistered = onCheckEmailRegistered,
        onDismiss = { isSignUpDialogOpen = false },
        onCompleteSignUp = { registeredEmail, createdPassword ->
          isSignUpDialogOpen = false
          onCompleteSignUp(registeredEmail, createdPassword)
        }
      )
    }
  }
}

/**
 * Guest Mode Warning Dialog styled to match the warm cream card aesthetics:
 * - Title: "Guest Mode Warning"
 * - Body Text: "You are logging in as a Guest. Your tasks and schedules will NOT be permanently saved. All data will be wiped completely when you log out."
 * - Buttons: "Cancel" (closes dialog) and "Proceed" (logs in as Guest and navigates to Dashboard).
 */
@Composable
private fun GuestModeWarningDialog(
  onCancel: () -> Unit,
  onProceed: () -> Unit
) {
  val metrics = rememberResponsiveMetrics()
  val dialogBackgroundColor = dashboardCreamCardColor()

  Dialog(
    onDismissRequest = onCancel,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = if (metrics.screenWidthDp > 600) 480.dp else 420.dp)
        .testTag("guest_mode_warning_dialog"),
      shape = RoundedCornerShape(28.dp),
      color = dialogBackgroundColor,
      tonalElevation = 8.dp,
      shadowElevation = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = metrics.scaledDp(24),
            vertical = metrics.scaledDp(24)
          ),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
      ) {
        Text(
          text = "Guest Mode Warning",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = metrics.scaledSp(21),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("guest_warning_dialog_title")
        )

        Text(
          text = "You are logging in as a Guest. Your tasks and schedules will NOT be permanently saved. All data will be wiped completely when you log out.",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = metrics.scaledSp(15),
            lineHeight = metrics.scaledSp(22),
            fontWeight = FontWeight.Medium
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("guest_warning_dialog_body")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(12)),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = onCancel,
            shape = CircleShape,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier
              .weight(1f)
              .height(metrics.scaledDp(48))
              .testTag("guest_warning_cancel_button")
          ) {
            Text(
              text = "Cancel",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = metrics.scaledSp(15),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Button(
            onClick = onProceed,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            modifier = Modifier
              .weight(1f)
              .height(metrics.scaledDp(48))
              .testTag("guest_warning_proceed_button")
          ) {
            Text(
              text = "Proceed",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = metrics.scaledSp(15),
                fontWeight = FontWeight.Bold
              )
            )
          }
        }
      }
    }
  }
}

/**
 * 4-Step Sign-Up & Verification Dialog:
 * - Step 1: User enters email. Strictly validates email format first (`"Invalid email format"`).
 *   If valid and already registered in `SharedPreferences`, displays `"Account already exists, please log in"`.
 * - Step 2: If the email is valid and new, clicking `"Send Verification Code"` simulates a 2-second
 *   loading state (`delay(2000)`), dynamically generates a 4-digit code via `(1000..9999).random().toString()`,
 *   and displays `"Mock email sent! Code: [RANDOM_CODE]"` on-screen and via Toast.
 * - Step 3: Shows a text field for the verification code. Requires the exact generated 4-digit code to unlock Step 4.
 * - Step 4: Shows a `"Create Password"` field and `"Complete Sign Up"` button that saves the
 *   account to `SharedPreferences` and logs the user in.
 */
@Composable
private fun SignUpVerificationDialog(
  initialEmail: String,
  onCheckEmailRegistered: (String) -> Boolean,
  onDismiss: () -> Unit,
  onCompleteSignUp: (String, String) -> Unit
) {
  val context = LocalContext.current
  val metrics = rememberResponsiveMetrics()
  val coroutineScope = rememberCoroutineScope()

  var signUpEmail by rememberSaveable { mutableStateOf(initialEmail.trim()) }
  var signUpError by rememberSaveable { mutableStateOf<String?>(null) }
  var isSendingCode by rememberSaveable { mutableStateOf(false) }
  var isCodeSent by rememberSaveable { mutableStateOf(false) }
  var generatedVerificationCode by rememberSaveable { mutableStateOf("") }
  var verificationCode by rememberSaveable { mutableStateOf("") }
  var newPassword by rememberSaveable { mutableStateOf("") }
  var isNewPasswordVisible by rememberSaveable { mutableStateOf(false) }

  val isCodeVerified =
    isCodeSent && generatedVerificationCode.isNotEmpty() && verificationCode.trim() == generatedVerificationCode
  val dialogBackgroundColor = dashboardCreamCardColor()

  Dialog(
    onDismissRequest = {
      if (!isSendingCode) onDismiss()
    },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = if (metrics.screenWidthDp > 600) 520.dp else 440.dp)
        .testTag("sign_up_dialog"),
      shape = RoundedCornerShape(28.dp),
      color = dialogBackgroundColor,
      tonalElevation = 8.dp,
      shadowElevation = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(
            horizontal = metrics.scaledDp(22),
            vertical = metrics.scaledDp(24)
          ),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
      ) {
        // Header Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontSize = metrics.scaledSp(22),
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f)
              .testTag("sign_up_dialog_title")
          )

          IconButton(
            onClick = {
              if (!isSendingCode) onDismiss()
            },
            modifier = Modifier
              .size(48.dp)
              .minimumInteractiveComponentSize()
              .testTag("close_sign_up_dialog_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close Sign Up",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(24.dp)
            )
          }
        }

        // Step 1: Email Field
        OutlinedTextField(
          value = signUpEmail,
          onValueChange = { updated ->
            signUpEmail = updated
            isCodeSent = false
            generatedVerificationCode = ""
            verificationCode = ""
            newPassword = ""
            val trimmed = updated.trim()
            signUpError = if (trimmed.isNotEmpty() && isValidEmailFormat(trimmed) && onCheckEmailRegistered(trimmed)) {
              "Account already exists, please log in"
            } else {
              null
            }
          },
          enabled = !isSendingCode,
          label = {
            Text(
              text = stringResource(R.string.email_label),
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1
            )
          },
          placeholder = {
            Text(
              text = "student@civil.edu",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              maxLines = 1
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Email,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = dashboardIvoryInnerColor(),
            unfocusedContainerColor = dashboardIvoryInnerColor(),
            cursorColor = MaterialTheme.colorScheme.primary
          ),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("signup_email_input")
        )

        // Error message (e.g., "Invalid email format" or "Account already exists, please log in")
        signUpError?.let { errorText ->
          Text(
            text = errorText,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = metrics.scaledSp(14),
              fontWeight = FontWeight.Bold
            ),
            color = Color(0xFFD32F2F),
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("signup_error_text")
          )
        }

        // Step 2: "Send Verification Code" Button + 2-second simulated loading state
        Button(
          onClick = {
            val trimmedEmail = signUpEmail.trim()
            if (!isValidEmailFormat(trimmedEmail)) {
              signUpError = "Invalid email format"
            } else if (onCheckEmailRegistered(trimmedEmail)) {
              signUpError = "Account already exists, please log in"
            } else {
              signUpError = null
              isSendingCode = true
              verificationCode = ""
              newPassword = ""
              coroutineScope.launch {
                delay(2000L)
                val dynamicCode = (1000..9999).random().toString()
                generatedVerificationCode = dynamicCode
                isSendingCode = false
                isCodeSent = true
                Toast.makeText(
                  context,
                  "Mock email sent! Code: $dynamicCode",
                  Toast.LENGTH_SHORT
                ).show()
              }
            }
          },
          enabled = !isSendingCode,
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(metrics.scaledDp(48))
            .testTag("send_verification_code_button")
        ) {
          if (isSendingCode) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = "Sending Code...",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontSize = metrics.scaledSp(15),
                  fontWeight = FontWeight.Bold
                ),
                maxLines = 1
              )
            }
          } else {
            Text(
              text = "Send Verification Code",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = metrics.scaledSp(15),
                fontWeight = FontWeight.Bold
              ),
              maxLines = 1
            )
          }
        }

        // Step 3: Verification Code Text & Input
        if (isCodeSent && generatedVerificationCode.isNotEmpty()) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mock_code_sent_banner")
          ) {
            Text(
              text = "Mock email sent! Code: $generatedVerificationCode",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = metrics.scaledSp(14),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.primary,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("mock_code_sent_text")
            )
          }

          OutlinedTextField(
            value = verificationCode,
            onValueChange = { verificationCode = it.trim() },
            label = {
              Text(
                text = "Verification Code",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
              )
            },
            placeholder = {
              Text(
                text = "Enter 4-digit code",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1
              )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
              focusedContainerColor = dashboardIvoryInnerColor(),
              unfocusedContainerColor = dashboardIvoryInnerColor(),
              cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("verification_code_input")
          )

          if (verificationCode.isNotEmpty() && !isCodeVerified) {
            Text(
              text = "Enter $generatedVerificationCode to verify your email",
              style = MaterialTheme.typography.labelMedium.copy(
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.SemiBold
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        // Step 4: "Create Password" Field & "Complete Sign Up" Button once the generated 4-digit code is entered
        if (isCodeVerified) {
          OutlinedTextField(
            value = newPassword,
            onValueChange = {
              newPassword = it
              if (signUpError != null) signUpError = null
            },
            label = {
              Text(
                text = "Create Password",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
              )
            },
            placeholder = {
              Text(
                text = "Enter your new password",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            },
            trailingIcon = {
              IconButton(
                onClick = { isNewPasswordVisible = !isNewPasswordVisible },
                modifier = Modifier
                  .size(48.dp)
                  .minimumInteractiveComponentSize()
                  .testTag("toggle_signup_password_visibility_button")
              ) {
                Icon(
                  imageVector = if (isNewPasswordVisible) {
                    Icons.Filled.VisibilityOff
                  } else {
                    Icons.Filled.Visibility
                  },
                  contentDescription = if (isNewPasswordVisible) {
                    "Hide password"
                  } else {
                    "Show password"
                  },
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(22.dp)
                )
              }
            },
            visualTransformation = if (isNewPasswordVisible) {
              VisualTransformation.None
            } else {
              PasswordVisualTransformation()
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
              focusedContainerColor = dashboardIvoryInnerColor(),
              unfocusedContainerColor = dashboardIvoryInnerColor(),
              cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("signup_create_password_input")
          )

          Button(
            onClick = {
              val trimmedEmail = signUpEmail.trim()
              if (!isValidEmailFormat(trimmedEmail)) {
                signUpError = "Invalid email format"
              } else if (onCheckEmailRegistered(trimmedEmail)) {
                signUpError = "Account already exists, please log in"
              } else if (newPassword.isBlank()) {
                signUpError = "Please create a password"
              } else {
                onCompleteSignUp(trimmedEmail, newPassword)
              }
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(metrics.scaledDp(50))
              .testTag("complete_sign_up_button")
          ) {
            Text(
              text = "Complete Sign Up",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = metrics.scaledSp(16),
                fontWeight = FontWeight.Bold
              ),
              maxLines = 1
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
  var selectedScheduleWeekDay by rememberSaveable { mutableStateOf(resolveInitialScheduleWeekDay()) }
  var isAddTaskDialogOpen by rememberSaveable { mutableStateOf(false) }
  var isAddScheduleDialogOpen by rememberSaveable { mutableStateOf(false) }
  var editingSchedule by remember { mutableStateOf<ClassSchedule?>(null) }
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
            onClick = {
              editingSchedule = null
              isAddScheduleDialogOpen = true
            },
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
          selectedWeekDay = selectedScheduleWeekDay,
          onSelectWeekDay = { selectedScheduleWeekDay = it },
          onEditSchedule = { scheduleToEdit ->
            editingSchedule = scheduleToEdit
            isAddScheduleDialogOpen = true
          },
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
        initialDay = selectedScheduleWeekDay,
        editingSchedule = editingSchedule,
        onDismiss = {
          isAddScheduleDialogOpen = false
          editingSchedule = null
        },
        onSaveSchedule = { subjectName, timeRange, selectedDays, room, colorHex ->
          val currentEditing = editingSchedule
          if (currentEditing != null) {
            taskViewModel.updateClassSchedule(
              scheduleId = currentEditing.id,
              subjectName = subjectName,
              timeRange = timeRange,
              days = selectedDays,
              room = room,
              colorHex = colorHex
            )
          } else {
            taskViewModel.addClassSchedule(
              subjectName = subjectName,
              timeRange = timeRange,
              days = selectedDays,
              room = room,
              colorHex = colorHex
            )
          }
          // Automatically switch the active Week View day tab if needed so the saved class is immediately visible
          val matchesCurrentTab = selectedDays.any { selectedScheduleWeekDay.matchesToken(it) }
          if (!matchesCurrentTab) {
            val firstMatchingDay = ScheduleWeekDay.entries.firstOrNull { weekDay ->
              selectedDays.any { weekDay.matchesToken(it) }
            }
            if (firstMatchingDay != null) {
              selectedScheduleWeekDay = firstMatchingDay
            }
          }
          isAddScheduleDialogOpen = false
          editingSchedule = null
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
        val todaysFilteredClasses = viewModel.getSchedulesForDayAbbreviation(todayAbbreviation)

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
 * 1. Top Header with "Class Timetable" title and a top-right toggle button switching between
 *    "List View" and "Grid View" (Graphic Table).
 * 2. In List View:
 *    - Interactive Week View horizontal row of selectable Days (Mon, Tue, Wed, Thu, Fri, Sat)
 *      with a distinct `BorderStroke(2.dp, MaterialTheme.colorScheme.primary)` on the selected day.
 *    - Chronologically sorted colored class schedule cards for the selected day.
 * 3. In Grid View:
 *    - Scrollable Graphic Timetable replicating the hand-drawn reference table with:
 *      * Far-left "AM" and "PM" group indicators.
 *      * Left Y-axis displaying hours from 7:00 down to 5:00.
 *      * Prominent horizontal divider line at the 12:00 mark.
 *      * Top X-axis for MON, TUE, WED, THU, FRI, SAT.
 *      * Colored `ClassSchedule` blocks mapped into their corresponding Day columns and Time rows.
 */
@Composable
fun ClassTimetableScheduleScreen(
  viewModel: TaskViewModel,
  selectedWeekDay: ScheduleWeekDay = resolveInitialScheduleWeekDay(),
  onSelectWeekDay: (ScheduleWeekDay) -> Unit = {},
  onEditSchedule: (ClassSchedule) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val metrics = rememberResponsiveMetrics()
  var isGridView by rememberSaveable { mutableStateOf(false) }

  val allSchedules = viewModel.classSchedules
  // Actively filters AND chronologically sorts classes by Start Time via ViewModel
  val filteredSchedules = viewModel.getSchedulesForDay(selectedWeekDay)

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
    // 1. Top Header Row: "Class Timetable" Title + Top-Right Grid/List View Toggle Button
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

        Surface(
          onClick = { isGridView = !isGridView },
          shape = RoundedCornerShape(16.dp),
          color = if (isGridView) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            dashboardCreamCardColor()
          },
          border = BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary
          ),
          shadowElevation = 4.dp,
          modifier = Modifier
            .height(42.dp)
            .testTag("schedule_view_toggle_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = if (isGridView) {
                Icons.AutoMirrored.Filled.ViewList
              } else {
                Icons.Filled.GridView
              },
              contentDescription = if (isGridView) {
                stringResource(R.string.list_view_label)
              } else {
                stringResource(R.string.grid_view_label)
              },
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = if (isGridView) {
                stringResource(R.string.list_view_label)
              } else {
                stringResource(R.string.grid_view_label)
              },
              style = MaterialTheme.typography.labelMedium.copy(
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.primary,
              maxLines = 1
            )
          }
        }
      }
    }

    if (isGridView) {
      // Graphic Table (Grid View) Mode
      item {
        ScheduleTimetableGridView(
          schedules = allSchedules,
          selectedWeekDay = selectedWeekDay,
          onSelectWeekDay = onSelectWeekDay,
          metrics = metrics
        )
      }
    } else {
      // 2. Week View Horizontal Row of Selectable Days (Mon, Tue, Wed, Thu, Fri, Sat)
      //    with distinct BorderStroke(2.dp, MaterialTheme.colorScheme.primary) on the selected day
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
            val countForDay = viewModel.getSchedulesForDay(day).size

            Surface(
              onClick = { onSelectWeekDay(day) },
              shape = RoundedCornerShape(18.dp),
              color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                dashboardCreamCardColor()
              },
              border = if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
              } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
              },
              shadowElevation = if (isSelected) 8.dp else 4.dp,
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
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                Surface(
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
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

      // 3. Visual Timetable Cards for the Selected Day (chronologically sorted)
      if (filteredSchedules.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("empty_schedule_day_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
              containerColor = dashboardCreamCardColor()
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
                  "No classes scheduled yet"
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
                  "Tap the + button below to add your Civil Engineering classes to the timetable."
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
            onEdit = { onEditSchedule(scheduleItem) },
            onDelete = { viewModel.deleteClassSchedule(scheduleItem.id) }
          )
        }
      }
    }
  }
}

/**
 * Scrollable Graphic Table (Grid View) replicating the hand-drawn reference timetable:
 * - Far-left edge: vertical "AM" and "PM" group indicators.
 * - Left Y-axis: hours from `7:00` down to `5:00` (`7:00, 8:00, 9:00, 10:00, 11:00` in AM;
 *   `12:00, 1:00, 2:00, 3:00, 4:00, 5:00` in PM).
 * - Prominent horizontal divider line at the `12:00` mark.
 * - Top X-axis: `MON, TUE, WED, THU, FRI, SAT`.
 * - Colored `ClassSchedule` blocks mapped into the corresponding Day columns and Time rows.
 */
@Composable
private fun ScheduleTimetableGridView(
  schedules: List<ClassSchedule>,
  selectedWeekDay: ScheduleWeekDay,
  onSelectWeekDay: (ScheduleWeekDay) -> Unit,
  metrics: ResponsiveMetrics
) {
  val amHourLabels = remember { listOf("7:00", "8:00", "9:00", "10:00", "11:00") }
  val pmHourLabels = remember { listOf("12:00", "1:00", "2:00", "3:00", "4:00", "5:00") }
  val allHourLabels = remember { amHourLabels + pmHourLabels }

  val hourHeight = metrics.scaledDp(56)
  val lunchRowHeight = metrics.scaledDp(28) // Shorter height for the 12:00 PM LUNCH divider row
  val amSectionHeight = hourHeight * amHourLabels.size
  val pmSectionHeight = lunchRowHeight + hourHeight * (pmHourLabels.size - 1)
  val totalGridBodyHeight = amSectionHeight + pmSectionHeight

  val meridiemStripWidth = metrics.scaledDp(22)
  val hourLabelColumnWidth = metrics.scaledDp(42)
  val leftAxisTotalWidth = meridiemStripWidth + hourLabelColumnWidth

  val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
  val lunchPurpleColor = MaterialTheme.colorScheme.primary
  val creamSurface = dashboardCreamCardColor()
  val ivoryCellSurface = dashboardIvoryInnerColor()

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("schedule_timetable_grid_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = creamSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
  ) {
    Column(
      modifier = Modifier.fillMaxWidth()
    ) {
      // 1. Top X-Axis Header Row: [AM/PM + TIME Corner] | MON | TUE | WED | THU | FRI | SAT
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(metrics.scaledDp(42))
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
          .testTag("schedule_grid_header_row"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Top-left corner cell above AM/PM + Hour labels
        Box(
          modifier = Modifier
            .width(leftAxisTotalWidth)
            .fillMaxHeight()
            .border(width = 0.5.dp, color = gridLineColor),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "TIME",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = metrics.scaledSp(9),
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }

        // Day Headers: MON, TUE, WED, THU, FRI, SAT
        ScheduleWeekDay.entries.forEach { day ->
          val isSelectedDay = day == selectedWeekDay
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .background(
                if (isSelectedDay) {
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                } else {
                  Color.Transparent
                }
              )
              .border(
                width = if (isSelectedDay) 1.5.dp else 0.5.dp,
                color = if (isSelectedDay) MaterialTheme.colorScheme.primary else gridLineColor
              )
              .clickable { onSelectWeekDay(day) }
              .testTag("grid_header_day_${day.shortLabel.uppercase(Locale.US)}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = day.shortLabel.uppercase(Locale.US),
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = metrics.scaledSp(10),
                fontWeight = FontWeight.Bold
              ),
              color = if (isSelectedDay) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurface
              },
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      HorizontalDivider(thickness = 1.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

      // 2. Main Timetable Body: Left Y-Axis (AM/PM + 7:00..5:00) + 6 Day Columns + Custom 12:00 LUNCH Divider
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(totalGridBodyHeight)
      ) {
        Row(
          modifier = Modifier.fillMaxSize()
        ) {
          // Far-left edge: "AM" and "PM" group indicator column
          Column(
            modifier = Modifier
              .width(meridiemStripWidth)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
          ) {
            // AM Group Box (7:00 to 11:00 rows)
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(amSectionHeight)
                .border(width = 0.5.dp, color = gridLineColor)
                .testTag("grid_am_indicator"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "AM",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = metrics.scaledSp(10),
                  fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.primary
              )
            }

            // PM Group Box (12:00 LUNCH row to 5:00 row)
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(pmSectionHeight)
                .border(width = 0.5.dp, color = gridLineColor)
                .testTag("grid_pm_indicator"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "PM",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = metrics.scaledSp(10),
                  fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.secondary
              )
            }
          }

          // Hour labels column (7:00 down to 5:00, with shorter 12:00 row)
          Column(
            modifier = Modifier
              .width(hourLabelColumnWidth)
              .fillMaxHeight()
              .background(creamSurface)
          ) {
            allHourLabels.forEach { hourText ->
              val isLunchRow = hourText == "12:00"
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(if (isLunchRow) lunchRowHeight else hourHeight)
                  .background(
                    if (isLunchRow) {
                      lunchPurpleColor.copy(alpha = 0.14f)
                    } else {
                      Color.Transparent
                    }
                  )
                  .border(width = 0.5.dp, color = gridLineColor)
                  .padding(
                    top = if (isLunchRow) 0.dp else 4.dp,
                    end = 4.dp
                  ),
                contentAlignment = if (isLunchRow) Alignment.CenterEnd else Alignment.TopEnd
              ) {
                Text(
                  text = hourText,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = metrics.scaledSp(10),
                    fontWeight = if (isLunchRow) FontWeight.ExtraBold else FontWeight.Bold
                  ),
                  color = if (isLunchRow) {
                    lunchPurpleColor
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
                  maxLines = 1
                )
              }
            }
          }

          // 6 Day Columns (MON, TUE, WED, THU, FRI, SAT) + Full-Width 12:00 PM "LUNCH" Divider
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
          ) {
            Row(modifier = Modifier.fillMaxSize()) {
              ScheduleWeekDay.entries.forEach { day ->
                val daySchedules = remember(schedules, day) {
                  schedules
                    .filter { sched -> sched.days.any { token -> day.matchesToken(token) } }
                    .sortedBy { sched -> parseTimeToFloat(sched.startTime) }
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(ivoryCellSurface)
                    .testTag("grid_day_column_${day.shortLabel.lowercase(Locale.US)}")
                ) {
                  // Background hourly grid cells for this day column (with shorter 12:00 row)
                  Column(modifier = Modifier.fillMaxSize()) {
                    allHourLabels.forEach { hourText ->
                      val isLunchRow = hourText == "12:00"
                      Box(
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(if (isLunchRow) lunchRowHeight else hourHeight)
                          .border(width = 0.5.dp, color = gridLineColor)
                      )
                    }
                  }

                  // Box overlay for class blocks positioned via parseTimeToFloat(startTime / endTime)
                  daySchedules.forEach { classInfo ->
                    val startFloat = parseTimeToFloat(classInfo.startTime).coerceIn(7.0f, 17.5f)
                    val parsedEndFloat = parseTimeToFloat(classInfo.endTime)
                    val endFloat = (if (parsedEndFloat > startFloat) parsedEndFloat else startFloat + 1.0f)
                      .coerceIn(startFloat + 0.5f, 18.0f)

                    val rawTopOffset = hourHeight * (startFloat - 7.0f)
                    val topOffset = when {
                      startFloat >= 13.0f -> rawTopOffset - (hourHeight - lunchRowHeight)
                      startFloat > 12.0f -> amSectionHeight + lunchRowHeight * (startFloat - 12.0f)
                      else -> rawTopOffset
                    }.coerceAtLeast(0.dp)

                    val cardHeight = (hourHeight * (endFloat - startFloat))
                      .coerceAtLeast(metrics.scaledDp(26))
                    val blockColor = Color(classInfo.colorHex)

                    // High-contrast text color calculation based on block background luminance
                    val blockLuminance =
                      0.299f * blockColor.red + 0.587f * blockColor.green + 0.114f * blockColor.blue
                    val primaryTextColor =
                      if (blockLuminance > 0.62f) Color(0xFF1C1B1F) else Color.White
                    val secondaryTextColor =
                      if (blockLuminance > 0.62f) {
                        Color(0xFF1C1B1F).copy(alpha = 0.88f)
                      } else {
                        Color.White.copy(alpha = 0.94f)
                      }

                    Surface(
                      onClick = { onSelectWeekDay(day) },
                      shape = RoundedCornerShape(8.dp),
                      color = blockColor,
                      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                      shadowElevation = 4.dp,
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                        .offset(y = topOffset)
                        .height(cardHeight)
                        .testTag("grid_class_block_${day.shortLabel.lowercase(Locale.US)}_${classInfo.id}")
                    ) {
                      Column(
                        modifier = Modifier
                          .fillMaxSize()
                          .padding(4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                      ) {
                        // 1. Subject Name (Bold, centered, maxLines = 2, ellipsis)
                        Text(
                          text = classInfo.subjectName,
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = metrics.scaledSp(9),
                            lineHeight = metrics.scaledSp(10),
                            fontWeight = FontWeight.Bold
                          ),
                          color = primaryTextColor,
                          textAlign = TextAlign.Center,
                          maxLines = 2,
                          overflow = TextOverflow.Ellipsis,
                          modifier = Modifier.fillMaxWidth()
                        )

                        // 2. Room Assignment (Standard font, centered, maxLines = 1, ellipsis)
                        if (classInfo.room.isNotBlank()) {
                          Spacer(modifier = Modifier.height(1.dp))
                          Text(
                            text = classInfo.room,
                            style = MaterialTheme.typography.labelSmall.copy(
                              fontSize = metrics.scaledSp(8),
                              lineHeight = metrics.scaledSp(9),
                              fontWeight = FontWeight.Normal
                            ),
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                          )
                        }

                        // 3. Time Duration (Directly displays the raw string inputted by the user)
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                          text = classInfo.timeRange,
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = metrics.scaledSp(7),
                            lineHeight = metrics.scaledSp(8),
                            fontWeight = FontWeight.Normal
                          ),
                          color = secondaryTextColor,
                          textAlign = TextAlign.Center,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                          modifier = Modifier.fillMaxWidth()
                        )
                      }
                    }
                  }
                }
              }
            }

            // Custom "LUNCH" Horizontal Divider Block at 12:00 PM spanning across all day columns
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(lunchRowHeight)
                .offset(y = amSectionHeight)
                .background(lunchPurpleColor)
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.35f))
                .testTag("schedule_grid_noon_divider"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "LUNCH",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontSize = metrics.scaledSp(11),
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 2.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
              )
            }
          }
        }
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
  onEdit: () -> Unit = {},
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
        // Top Row: Day Pills + Edit (Pen) Icon + Delete (Trash) Icon
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

          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onEdit,
              modifier = Modifier
                .size(36.dp)
                .testTag("edit_schedule_button_${schedule.id}")
            ) {
              Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit ${schedule.subjectName}",
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(20.dp)
              )
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
 * Polished Manual Entry `Dialog` opened by the Schedule screen's "+" Floating Action Button (FAB).
 * Styled with the warm cream card palette (`#FFFBF0` in light mode) and includes:
 * - Title: "Add Class Schedule"
 * - `OutlinedTextField` for "Subject Name" (e.g., "Reinforced Concrete Design")
 * - `OutlinedTextField` for "Room" (e.g., "ENG. 307")
 * - Structured "Start Time" + AM/PM toggle buttons and "End Time" + AM/PM toggle buttons
 * - A row of selectable circular Day toggles (`M`, `T`, `W`, `Th`, `F`, `S`)
 * - "Cancel" and "Save" buttons
 */
@Composable
private fun AddClassScheduleDialog(
  initialDay: ScheduleWeekDay = resolveInitialScheduleWeekDay(),
  editingSchedule: ClassSchedule? = null,
  onDismiss: () -> Unit,
  onSaveSchedule: (String, String, List<String>, String, Long) -> Unit
) {
  val metrics = rememberResponsiveMetrics()
  val isEditMode = editingSchedule != null
  val prefilledTimeInputs = remember(editingSchedule) {
    editingSchedule?.let { parseTimeRangeToStructuredInputs(it.timeRange) }
  }

  var subjectName by rememberSaveable(editingSchedule?.id) {
    mutableStateOf(editingSchedule?.subjectName ?: "")
  }
  var roomAssignment by rememberSaveable(editingSchedule?.id) {
    mutableStateOf(editingSchedule?.room ?: "")
  }
  var startTimeInput by rememberSaveable(editingSchedule?.id) {
    mutableStateOf(prefilledTimeInputs?.startTime ?: "")
  }
  var startMeridiem by rememberSaveable(editingSchedule?.id) {
    mutableStateOf(prefilledTimeInputs?.startMeridiem ?: "AM")
  }
  var endTimeInput by rememberSaveable(editingSchedule?.id) {
    mutableStateOf(prefilledTimeInputs?.endTime ?: "")
  }
  var endMeridiem by rememberSaveable(editingSchedule?.id) {
    mutableStateOf(prefilledTimeInputs?.endMeridiem ?: "AM")
  }

  val selectedDayCodes = remember(editingSchedule?.id) {
    if (editingSchedule != null) {
      val matchedCodes = ScheduleWeekDay.entries
        .filter { weekDay -> editingSchedule.days.any { token -> weekDay.matchesToken(token) } }
        .map { it.dayCode }
        .ifEmpty { editingSchedule.days }
      mutableStateListOf(*matchedCodes.toTypedArray())
    } else {
      val todayCode = resolveTodayCorDayAbbreviation()
      val defaultCodes = buildList {
        add(initialDay.dayCode)
        if (todayCode != "Sun" && !contains(todayCode)) {
          add(todayCode)
        }
      }
      mutableStateListOf(*defaultCodes.toTypedArray())
    }
  }

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
  var selectedColorHex by rememberSaveable(editingSchedule?.id) {
    mutableLongStateOf(editingSchedule?.colorHex ?: availableColors.first())
  }

  val dialogSurfaceColor = dashboardCreamCardColor()
  val chipUnselectedColor = dashboardIvoryInnerColor()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = metrics.scaledDp(20))
        .widthIn(max = 480.dp)
        .testTag("add_schedule_dialog"),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(
        containerColor = dialogSurfaceColor
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(metrics.scaledDp(22)),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
      ) {
        // Dialog Header Title: "Edit Class Schedule" in edit mode, or "Add Class Schedule" in add mode
        Text(
          text = if (isEditMode) {
            stringResource(R.string.edit_schedule_dialog_title)
          } else {
            stringResource(R.string.add_schedule_dialog_title)
          },
          style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = metrics.scaledSp(22),
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.testTag("schedule_dialog_title")
        )

        // 1. OutlinedTextField for "Subject Name" (e.g., "Reinforced Concrete Design")
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
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
          ),
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_schedule_subject_input")
        )

        // 2. OutlinedTextField for "Room" (e.g., "ENG. 307")
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
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.LocationOn,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
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
            capitalization = KeyboardCapitalization.Characters,
            imeAction = ImeAction.Next
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_schedule_room_input")
        )

        // 3. Structured Start Time + AM/PM Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(8)),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = startTimeInput,
            onValueChange = { newValue ->
              startTimeInput = newValue
              inferAcademicMeridiem(newValue)?.let { startMeridiem = it }
            },
            label = {
              Text(
                text = stringResource(R.string.start_time_label),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            placeholder = {
              Text(
                text = stringResource(R.string.start_time_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
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
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Next
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("new_schedule_start_time_input")
          )

          AmPmSegmentedToggle(
            selectedMeridiem = startMeridiem,
            onSelectMeridiem = { startMeridiem = it },
            amTestTag = "start_time_am_toggle",
            pmTestTag = "start_time_pm_toggle",
            metrics = metrics
          )
        }

        // 4. Structured End Time + AM/PM Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(8)),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = endTimeInput,
            onValueChange = { newValue ->
              endTimeInput = newValue
              inferAcademicMeridiem(newValue)?.let { endMeridiem = it }
            },
            label = {
              Text(
                text = stringResource(R.string.end_time_label),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            placeholder = {
              Text(
                text = stringResource(R.string.end_time_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
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
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Done
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("new_schedule_end_time_input")
          )

          AmPmSegmentedToggle(
            selectedMeridiem = endMeridiem,
            onSelectMeridiem = { endMeridiem = it },
            amTestTag = "end_time_am_toggle",
            pmTestTag = "end_time_pm_toggle",
            metrics = metrics
          )
        }

        // 5. Selectable Circular Day Toggles Row (M, T, W, Th, F, S)
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
        ) {
          Text(
            text = stringResource(R.string.select_days_label),
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = metrics.scaledSp(14),
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("schedule_day_chips_row"),
            horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(6)),
            verticalAlignment = Alignment.CenterVertically
          ) {
            ScheduleWeekDay.entries.forEach { day ->
              val isSelected = selectedDayCodes.any { day.matchesToken(it) }
              Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
              ) {
                Surface(
                  onClick = {
                    if (isSelected) {
                      selectedDayCodes.removeAll { day.matchesToken(it) }
                    } else {
                      selectedDayCodes.add(day.dayCode)
                    }
                  },
                  shape = CircleShape,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    chipUnselectedColor
                  },
                  shadowElevation = if (isSelected) 6.dp else 2.dp,
                  modifier = Modifier
                    .size(44.dp)
                    .border(
                      width = if (isSelected) 0.dp else 1.dp,
                      color = if (isSelected) {
                        Color.Transparent
                      } else {
                        MaterialTheme.colorScheme.surfaceVariant
                      },
                      shape = CircleShape
                    )
                    .testTag("schedule_day_chip_${day.chipLabel.lowercase()}")
                ) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = day.chipLabel,
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = metrics.scaledSp(13),
                        fontWeight = FontWeight.Bold
                      ),
                      color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                      } else {
                        MaterialTheme.colorScheme.onSurface
                      },
                      maxLines = 1
                    )
                  }
                }
              }
            }
          }
        }

        // 6. Card Accent Color Picker Row
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

        // 7. Action Buttons: "Cancel" and "Save"
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
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

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              val formattedRange = formatStructuredTimeRange(
                startInput = startTimeInput,
                startMeridiem = startMeridiem,
                endInput = endTimeInput,
                endMeridiem = endMeridiem
              )
              onSaveSchedule(
                subjectName,
                formattedRange,
                selectedDayCodes.toList(),
                roomAssignment,
                selectedColorHex
              )
            },
            enabled = subjectName.isNotBlank(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
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
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.padding(horizontal = 10.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Compact AM / PM toggle buttons paired with the Start Time and End Time inputs.
 */
@Composable
private fun AmPmSegmentedToggle(
  selectedMeridiem: String,
  onSelectMeridiem: (String) -> Unit,
  amTestTag: String,
  pmTestTag: String,
  metrics: ResponsiveMetrics
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = dashboardIvoryInnerColor(),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
    modifier = Modifier.height(52.dp)
  ) {
    Row(
      modifier = Modifier.padding(3.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      listOf("AM" to amTestTag, "PM" to pmTestTag).forEach { (label, tag) ->
        val isSelected = selectedMeridiem.equals(label, ignoreCase = true)
        Surface(
          onClick = { onSelectMeridiem(label) },
          shape = RoundedCornerShape(11.dp),
          color = if (isSelected) {
            MaterialTheme.colorScheme.primary
          } else {
            Color.Transparent
          },
          modifier = Modifier
            .fillMaxHeight()
            .width(metrics.scaledDp(42))
            .testTag(tag)
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelMedium.copy(
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.Bold
              ),
              color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              maxLines = 1
            )
          }
        }
      }
    }
  }
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

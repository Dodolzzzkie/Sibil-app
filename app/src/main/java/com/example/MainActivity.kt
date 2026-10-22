package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AvTimer
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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.input.pointer.pointerInput
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
import java.time.LocalTime
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
 * Five main navigation tabs ordered Left to Right:
 * Clock, Extras, Dashboard, Tasks, Schedule.
 */
enum class CeNavTab(
  val title: String,
  val routeTag: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  CLOCK(
    title = "Clock",
    routeTag = "nav_tab_clock",
    selectedIcon = Icons.Filled.Schedule,
    unselectedIcon = Icons.Outlined.Schedule
  ),
  EXTRAS(
    title = "Extras",
    routeTag = "nav_tab_extras",
    selectedIcon = Icons.Filled.GridView,
    unselectedIcon = Icons.Outlined.GridView
  ),
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
  )
}

const val DEFAULT_TASK_CATEGORY = "General"

/**
 * Shared Task Data Model including `id`, `title`, `dueDate`, `isCompleted`, and `subjectCategory`.
 */
data class Task(
  val id: Int,
  val title: String,
  val dueDate: String,
  val isCompleted: Boolean = false,
  val subjectCategory: String = DEFAULT_TASK_CATEGORY
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
  INVALID_USERNAME,
  NO_ACCOUNT_FOUND,
  INCORRECT_PASSWORD
}

/**
 * Validates that a username is not blank and has at least 3 characters.
 */
fun isValidUsername(username: String): Boolean {
  val trimmed = username.trim()
  return trimmed.isNotBlank() && trimmed.length >= 3
}

/**
 * Lightweight offline data manager wrapping Android's native `SharedPreferences`.
 * Acts as a local mock backend that persists:
 * 1. Registered user accounts (`username` -> `password` map).
 * 2. Account-segregated `Task` lists (`tasks_[username]` or `tasks_guest`).
 * 3. Account-segregated `ClassSchedule` lists (`schedules_[username]` or `schedules_guest`).
 * 4. Active user session (`username`) and `isDarkMode`.
 * 5. Explicit Guest data wipe (`clearGuestData()`) when logging out of Guest mode.
 */
class LocalStorageManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  init {
    migrateTaskSchemaIfNeeded()
  }

  /**
   * Safely migrates saved task lists to the v2 `Task` schema (`subjectCategory` field)
   * and clears legacy hardcoded starter placeholder tasks so old data never causes JSON
   * parsing crashes or unwanted placeholder clutter.
   */
  private fun migrateTaskSchemaIfNeeded() {
    val savedSchemaVersion = prefs.getInt(KEY_TASK_SCHEMA_VERSION, 1)
    if (savedSchemaVersion >= CURRENT_TASK_SCHEMA_VERSION) return

    val legacyStarterTitles = setOf(
      "Reinforced Concrete Design - Plate 3",
      "Hydraulics Quiz",
      "Complete Surveying Field Report",
      "Read Steel Design Chapter 4",
      "Prepare Soil Mechanics Sieve Analysis Data"
    )

    val editor = prefs.edit()
    val allEntries = prefs.all
    for ((key, value) in allEntries) {
      if (!key.startsWith(KEY_TASKS_PREFIX)) continue
      val rawJson = value as? String
      if (rawJson.isNullOrBlank()) {
        editor.remove(key)
        continue
      }
      try {
        val jsonArray = JSONArray(rawJson)
        val migratedArray = JSONArray()
        for (i in 0 until jsonArray.length()) {
          val obj = jsonArray.optJSONObject(i) ?: continue
          val title = obj.optString("title", "").trim()
          if (title.isEmpty() || title in legacyStarterTitles) {
            continue
          }
          val category = obj.optString("subjectCategory", DEFAULT_TASK_CATEGORY)
            .trim()
            .ifEmpty { DEFAULT_TASK_CATEGORY }
          val migratedObj = JSONObject().apply {
            put("id", obj.optInt("id", i + 1))
            put("title", title)
            put("dueDate", obj.optString("dueDate", "Upcoming").ifBlank { "Upcoming" })
            put("isCompleted", obj.optBoolean("isCompleted", false))
            put("subjectCategory", category)
          }
          migratedArray.put(migratedObj)
        }
        editor.putString(key, migratedArray.toString())
      } catch (_: Exception) {
        editor.remove(key)
      }
    }
    editor.putInt(KEY_TASK_SCHEMA_VERSION, CURRENT_TASK_SCHEMA_VERSION)
    editor.apply()
  }

  fun normalizeAccountKey(usernameOrGuest: String?): String {
    val trimmed = usernameOrGuest?.trim().orEmpty()
    return if (
      trimmed.isEmpty() ||
      trimmed.equals(GUEST_ACCOUNT_KEY, ignoreCase = true) ||
      trimmed.equals(GUEST_DISPLAY_NAME, ignoreCase = true)
    ) {
      GUEST_ACCOUNT_KEY
    } else {
      trimmed.lowercase(Locale.US)
    }
  }

  private fun tasksKeyFor(username: String): String {
    return "${KEY_TASKS_PREFIX}${normalizeAccountKey(username)}"
  }

  private fun schedulesKeyFor(username: String): String {
    return "${KEY_SCHEDULES_PREFIX}${normalizeAccountKey(username)}"
  }

  private fun alarmsKeyFor(username: String): String {
    return "${KEY_ALARMS_PREFIX}${normalizeAccountKey(username)}"
  }

  /**
   * Returns all registered users stored in `SharedPreferences` (normalized lowercase username -> password).
   */
  fun getRegisteredUsers(): Map<String, String> {
    val rawJson = prefs.getString(KEY_REGISTERED_USERS, null) ?: return emptyMap()
    return try {
      val jsonObject = JSONObject(rawJson)
      val map = mutableMapOf<String, String>()
      val keys = jsonObject.keys()
      while (keys.hasNext()) {
        val usernameKey = keys.next()
        map[usernameKey.trim().lowercase(Locale.US)] = jsonObject.optString(usernameKey, "")
      }
      map
    } catch (_: Exception) {
      emptyMap()
    }
  }

  /**
   * Checks whether the given username is already registered in `SharedPreferences`.
   */
  fun isUsernameRegistered(username: String): Boolean {
    val normalizedUsername = normalizeAccountKey(username)
    if (normalizedUsername.isEmpty() || normalizedUsername == GUEST_ACCOUNT_KEY) return false
    return getRegisteredUsers().containsKey(normalizedUsername)
  }

  /**
   * Verifies that the username exists and its stored password matches `password`.
   */
  fun verifyUserPassword(username: String, password: String): Boolean {
    val normalizedUsername = normalizeAccountKey(username)
    if (normalizedUsername.isEmpty() || normalizedUsername == GUEST_ACCOUNT_KEY) return false
    val savedPassword = getRegisteredUsers()[normalizedUsername] ?: return false
    return savedPassword == password
  }

  /**
   * Registers a new user account (`username` -> `password`) in `SharedPreferences`.
   */
  fun registerUser(username: String, password: String) {
    val normalizedUsername = normalizeAccountKey(username)
    if (normalizedUsername.isEmpty() || normalizedUsername == GUEST_ACCOUNT_KEY) return
    val currentUsers = getRegisteredUsers().toMutableMap()
    currentUsers[normalizedUsername] = password
    val jsonObject = JSONObject()
    currentUsers.forEach { (k, v) -> jsonObject.put(k, v) }
    prefs.edit().putString(KEY_REGISTERED_USERS, jsonObject.toString()).apply()
  }

  fun saveTasks(tasks: List<Task>, username: String = GUEST_ACCOUNT_KEY) {
    val jsonArray = JSONArray()
    for (task in tasks) {
      val obj = JSONObject().apply {
        put("id", task.id)
        put("title", task.title)
        put("dueDate", task.dueDate)
        put("isCompleted", task.isCompleted)
        put("subjectCategory", task.subjectCategory.trim().ifEmpty { DEFAULT_TASK_CATEGORY })
      }
      jsonArray.put(obj)
    }
    prefs.edit().putString(tasksKeyFor(username), jsonArray.toString()).apply()
  }

  fun loadTasks(username: String = GUEST_ACCOUNT_KEY): List<Task>? {
    val storageKey = tasksKeyFor(username)
    if (!prefs.contains(storageKey)) return null
    val rawJson = prefs.getString(storageKey, null) ?: return null
    return try {
      val jsonArray = JSONArray(rawJson)
      val loaded = mutableListOf<Task>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.optJSONObject(i) ?: continue
        val title = obj.optString("title", "").trim()
        if (title.isEmpty()) continue
        val category = obj.optString("subjectCategory", DEFAULT_TASK_CATEGORY)
          .trim()
          .ifEmpty { DEFAULT_TASK_CATEGORY }
        loaded.add(
          Task(
            id = obj.optInt("id", i + 1),
            title = title,
            dueDate = obj.optString("dueDate", "Upcoming").ifBlank { "Upcoming" },
            isCompleted = obj.optBoolean("isCompleted", false),
            subjectCategory = category
          )
        )
      }
      loaded
    } catch (_: Exception) {
      // Safely clear corrupted or incompatible task entries so JSON parsing never crashes
      prefs.edit().remove(storageKey).apply()
      emptyList()
    }
  }

  fun saveSchedules(schedules: List<ClassSchedule>, username: String = GUEST_ACCOUNT_KEY) {
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
    prefs.edit().putString(schedulesKeyFor(username), jsonArray.toString()).apply()
  }

  fun loadSchedules(username: String = GUEST_ACCOUNT_KEY): List<ClassSchedule>? {
    val storageKey = schedulesKeyFor(username)
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

  fun saveAlarms(alarms: List<AlarmItem>, username: String = GUEST_ACCOUNT_KEY) {
    val jsonArray = JSONArray()
    for (alarm in alarms) {
      val daysArray = JSONArray()
      alarm.activeDays.distinct().sorted().forEach { dayIndex ->
        if (dayIndex in 0..6) {
          daysArray.put(dayIndex)
        }
      }
      val obj = JSONObject().apply {
        put("id", alarm.id)
        put("name", alarm.name.trim().ifEmpty { "Alarm" })
        put("hour", alarm.hour)
        put("minute", alarm.minute)
        put("amPm", alarm.amPm)
        put("activeDays", daysArray)
        put("isActive", alarm.isActive)
      }
      jsonArray.put(obj)
    }
    prefs.edit().putString(alarmsKeyFor(username), jsonArray.toString()).apply()
  }

  fun loadAlarms(username: String = GUEST_ACCOUNT_KEY): List<AlarmItem>? {
    val storageKey = alarmsKeyFor(username)
    if (!prefs.contains(storageKey)) return null
    val rawJson = prefs.getString(storageKey, null) ?: return null
    return try {
      val jsonArray = JSONArray(rawJson)
      val loaded = mutableListOf<AlarmItem>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.optJSONObject(i) ?: continue
        val daysArray = obj.optJSONArray("activeDays")
        val activeDays = mutableListOf<Int>()
        if (daysArray != null) {
          for (j in 0 until daysArray.length()) {
            val dayIndex = daysArray.optInt(j, -1)
            if (dayIndex in 0..6 && !activeDays.contains(dayIndex)) {
              activeDays.add(dayIndex)
            }
          }
        }
        val rawHour = obj.optString("hour", "07").trim()
        val parsedHour = (rawHour.toIntOrNull() ?: 7).coerceIn(1, 12)
        val rawMinute = obj.optString("minute", "00").trim()
        val parsedMinute = (rawMinute.toIntOrNull() ?: 0).coerceIn(0, 59)
        val rawAmPm = obj.optString("amPm", "AM").trim().uppercase(Locale.US)
        val normalizedAmPm = if (rawAmPm == "PM") "PM" else "AM"
        val name = obj.optString("name", "Alarm").trim().ifEmpty { "Alarm" }
        loaded.add(
          AlarmItem(
            id = obj.optInt("id", i + 1),
            name = name,
            hour = String.format(Locale.US, "%02d", parsedHour),
            minute = String.format(Locale.US, "%02d", parsedMinute),
            amPm = normalizedAmPm,
            activeDays = activeDays.sorted(),
            isActive = obj.optBoolean("isActive", true)
          )
        )
      }
      loaded
    } catch (_: Exception) {
      prefs.edit().remove(storageKey).apply()
      emptyList()
    }
  }

  /**
   * Explicitly deletes all temporary data associated with the `"guest"` keys
   * (`tasks_guest`, `schedules_guest`, and `alarms_guest`) when logging out of Guest mode.
   */
  fun clearGuestData() {
    val editor = prefs.edit()
      .remove(tasksKeyFor(GUEST_ACCOUNT_KEY))
      .remove(schedulesKeyFor(GUEST_ACCOUNT_KEY))
      .remove(alarmsKeyFor(GUEST_ACCOUNT_KEY))
    prefs.all.keys
      .filter { key ->
        key.endsWith("_$GUEST_ACCOUNT_KEY")
      }
      .forEach { key ->
        editor.remove(key)
      }
    editor.apply()
  }

  fun saveUserProfile(username: String) {
    prefs.edit()
      .putString(KEY_USER_NAME, username.trim())
      .apply()
  }

  fun loadUserName(): String? {
    val saved = prefs.getString(KEY_USER_NAME, null)
    return saved?.takeIf { it.isNotBlank() }
  }

  fun clearUserProfile() {
    prefs.edit()
      .remove(KEY_USER_NAME)
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
    const val GUEST_DISPLAY_NAME = "Guest User"
    private const val PREFS_NAME = "sibil_app_offline_storage"
    private const val KEY_REGISTERED_USERS = "registered_users_json"
    private const val KEY_TASKS_PREFIX = "tasks_"
    private const val KEY_SCHEDULES_PREFIX = "schedules_"
    private const val KEY_ALARMS_PREFIX = "alarms_"
    private const val KEY_USER_NAME = "saved_user_name"
    private const val KEY_DARK_MODE = "saved_dark_mode"
    private const val KEY_TASK_SCHEMA_VERSION = "task_schema_version"
    private const val CURRENT_TASK_SCHEMA_VERSION = 2
  }
}

/**
 * Shared ViewModel storing and managing:
 * 1. Global theme state (`isDarkMode`, default `false`).
 * 2. User session data (`username` and `hasSavedSession`, displaying `"Guest User"` when in guest mode).
 * 3. Live Open-Meteo weather summary (`weatherText`, starting at `"Fetching Weather..."`).
 * 4. Account-segregated coursework tasks shared across the Academic Dashboard and Task Manager (`tasks_[username]`).
 * 5. Today's Schedule list on the Dashboard.
 * 6. Account-segregated `ClassSchedule` items for the colored Week Timetable & Grid View (`schedules_[username]`).
 * 7. Simulated Username Authentication & Guest Mode data wipe via `LocalStorageManager` (`SharedPreferences`).
 */
class TaskViewModel(
  private var storageManager: LocalStorageManager? = null
) : ViewModel() {
  var isDarkMode by mutableStateOf(false)
    private set

  var username by mutableStateOf(LocalStorageManager.GUEST_DISPLAY_NAME)
    private set

  val userName: String
    get() = displayUsername

  val displayUsername: String
    get() = if (isGuestMode) {
      LocalStorageManager.GUEST_DISPLAY_NAME
    } else {
      username
    }

  var hasSavedSession by mutableStateOf(false)
    private set

  var weatherText by mutableStateOf("Fetching Weather...")
    private set

  var hasRequestedInitialWeather by mutableStateOf(false)
    private set

  private var nextId by mutableIntStateOf(1)
  private var nextScheduleId by mutableIntStateOf(100)
  private var nextAlarmId by mutableIntStateOf(1)
  private var hasLoadedFromStorage = false

  var selectedTaskFolder by mutableStateOf(DEFAULT_TASK_CATEGORY)
    private set

  fun selectTaskFolder(folderName: String) {
    selectedTaskFolder = folderName.trim().ifEmpty { DEFAULT_TASK_CATEGORY }
  }

  val isGuestMode: Boolean
    get() = username.isBlank() ||
      username.equals(LocalStorageManager.GUEST_ACCOUNT_KEY, ignoreCase = true) ||
      username.equals(LocalStorageManager.GUEST_DISPLAY_NAME, ignoreCase = true)

  val currentAccountKey: String
    get() = if (isGuestMode) {
      LocalStorageManager.GUEST_ACCOUNT_KEY
    } else {
      username.trim().lowercase(Locale.US)
    }

  // Default state of the Task list is completely empty (no hardcoded placeholder tasks)
  private val _tasks = mutableStateListOf<Task>()
  val tasks: List<Task> get() = _tasks

  val todaysSchedule: List<ClassScheduleItem> = emptyList()

  private val _classSchedules = mutableStateListOf<ClassSchedule>()
  val classSchedules: List<ClassSchedule> get() = _classSchedules

  // Default state of the Alarm list is completely empty (no hardcoded mock alarms)
  private val _alarms = mutableStateListOf<AlarmItem>()
  val alarms: List<AlarmItem> get() = _alarms

  /**
   * Dynamically builds the list of Subject Folders:
   * Starts with a permanent `"General"` folder, then appends unique `subjectName`s
   * extracted directly from the `ClassSchedule` list.
   */
  fun getSubjectFolders(): List<String> {
    val uniqueScheduleSubjects = _classSchedules
      .map { it.subjectName.trim() }
      .filter { it.isNotEmpty() && !it.equals(DEFAULT_TASK_CATEGORY, ignoreCase = true) }
      .distinctBy { it.lowercase(Locale.US) }
    return listOf(DEFAULT_TASK_CATEGORY) + uniqueScheduleSubjects
  }

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
      username = if (
        savedUserName.equals(LocalStorageManager.GUEST_ACCOUNT_KEY, ignoreCase = true) ||
        savedUserName.equals(LocalStorageManager.GUEST_DISPLAY_NAME, ignoreCase = true)
      ) {
        LocalStorageManager.GUEST_DISPLAY_NAME
      } else {
        savedUserName.trim()
      }
      hasSavedSession = true
      loadAccountDataForKey(manager, currentAccountKey)
    } else {
      hasSavedSession = false
    }
  }

  private fun loadAccountDataForKey(manager: LocalStorageManager, accountKey: String) {
    val savedTasks = manager.loadTasks(accountKey)
    _tasks.clear()
    if (savedTasks != null) {
      _tasks.addAll(savedTasks)
      nextId = (savedTasks.maxOfOrNull { it.id } ?: 0) + 1
    } else {
      nextId = 1
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

    val savedAlarms = manager.loadAlarms(accountKey)
    _alarms.clear()
    if (savedAlarms != null) {
      _alarms.addAll(savedAlarms)
      nextAlarmId = (savedAlarms.maxOfOrNull { it.id } ?: 0) + 1
    } else {
      nextAlarmId = 1
    }

    val validFolders = getSubjectFolders()
    if (validFolders.none { it.equals(selectedTaskFolder, ignoreCase = true) }) {
      selectedTaskFolder = DEFAULT_TASK_CATEGORY
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
   * Validates `typedUsername` and `typedPassword` against the `SharedPreferences` registered users map.
   * - Checks that the username is not blank and has at least 3 characters (`INVALID_USERNAME`).
   * - Returns `NO_ACCOUNT_FOUND` if the username is not registered.
   * - Returns `INCORRECT_PASSWORD` if the username is registered but the password does not match.
   * - Logs the user in and returns `SUCCESS` when valid.
   */
  fun attemptLogin(typedUsername: String, typedPassword: String): LoginValidationResult {
    val cleanUsername = typedUsername.trim()
    if (!isValidUsername(cleanUsername)) {
      return LoginValidationResult.INVALID_USERNAME
    }
    val manager = storageManager
    if (manager == null || !manager.isUsernameRegistered(cleanUsername)) {
      return LoginValidationResult.NO_ACCOUNT_FOUND
    }
    if (!manager.verifyUserPassword(cleanUsername, typedPassword)) {
      return LoginValidationResult.INCORRECT_PASSWORD
    }
    loginWithUsername(cleanUsername)
    return LoginValidationResult.SUCCESS
  }

  /**
   * Checks whether `username` is already registered in `SharedPreferences`.
   */
  fun isUsernameRegistered(username: String): Boolean {
    return storageManager?.isUsernameRegistered(username.trim()) == true
  }

  /**
   * Saves a newly registered account (`username` -> `password`) to `SharedPreferences`
   * and immediately logs the user in with their segregated storage keys (`tasks_[username]`).
   */
  fun completeSignUp(usernameToRegister: String, password: String) {
    val cleanUsername = usernameToRegister.trim()
    storageManager?.registerUser(cleanUsername, password)
    loginWithUsername(cleanUsername)
  }

  fun loginWithUsername(typedUsername: String) {
    val cleanUsername = typedUsername.trim()
    username = if (cleanUsername.isEmpty()) LocalStorageManager.GUEST_DISPLAY_NAME else cleanUsername
    hasSavedSession = true
    storageManager?.let { manager ->
      manager.saveUserProfile(username)
      loadAccountDataForKey(manager, currentAccountKey)
    }
  }

  fun loginAsGuest() {
    username = LocalStorageManager.GUEST_DISPLAY_NAME
    hasSavedSession = true
    storageManager?.let { manager ->
      manager.saveUserProfile(username)
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
      _classSchedules.clear()
      _alarms.clear()
      nextId = 1
      nextScheduleId = 100
      nextAlarmId = 1
    }
    selectedTaskFolder = DEFAULT_TASK_CATEGORY
  }

  fun addAlarm(
    name: String,
    hour: String,
    minute: String,
    amPm: String,
    activeDays: List<Int>,
    isActive: Boolean = true
  ) {
    val cleanName = name.trim().ifEmpty { "Alarm" }
    val parsedHour = (hour.trim().toIntOrNull() ?: 7).coerceIn(1, 12)
    val parsedMinute = (minute.trim().toIntOrNull() ?: 0).coerceIn(0, 59)
    val normalizedHour = String.format(Locale.US, "%02d", parsedHour)
    val normalizedMinute = String.format(Locale.US, "%02d", parsedMinute)
    val normalizedAmPm = if (amPm.trim().equals("PM", ignoreCase = true)) "PM" else "AM"
    val normalizedDays = activeDays.filter { it in 0..6 }.distinct().sorted()

    _alarms.add(
      AlarmItem(
        id = nextAlarmId++,
        name = cleanName,
        hour = normalizedHour,
        minute = normalizedMinute,
        amPm = normalizedAmPm,
        activeDays = normalizedDays,
        isActive = isActive
      )
    )
    storageManager?.saveAlarms(_alarms, currentAccountKey)
  }

  fun toggleAlarmActive(alarmId: Int, isActive: Boolean) {
    val index = _alarms.indexOfFirst { it.id == alarmId }
    if (index != -1) {
      _alarms[index] = _alarms[index].copy(isActive = isActive)
      storageManager?.saveAlarms(_alarms, currentAccountKey)
    }
  }

  fun toggleAlarmDay(alarmId: Int, dayIndex: Int) {
    if (dayIndex !in 0..6) return
    val index = _alarms.indexOfFirst { it.id == alarmId }
    if (index != -1) {
      val current = _alarms[index]
      val updatedDays = if (current.activeDays.contains(dayIndex)) {
        current.activeDays - dayIndex
      } else {
        (current.activeDays + dayIndex).sorted()
      }
      _alarms[index] = current.copy(activeDays = updatedDays)
      storageManager?.saveAlarms(_alarms, currentAccountKey)
    }
  }

  fun deleteAlarm(alarmId: Int) {
    _alarms.removeAll { it.id == alarmId }
    storageManager?.saveAlarms(_alarms, currentAccountKey)
  }

  fun addTask(
    title: String,
    dueDate: String,
    subjectCategory: String = DEFAULT_TASK_CATEGORY
  ) {
    val trimmedTitle = title.trim()
    val trimmedDueDate = dueDate.trim().ifEmpty { "Upcoming" }
    val trimmedCategory = subjectCategory.trim().ifEmpty { DEFAULT_TASK_CATEGORY }
    if (trimmedTitle.isNotEmpty()) {
      _tasks.add(
        0,
        Task(
          id = nextId++,
          title = trimmedTitle,
          dueDate = trimmedDueDate,
          isCompleted = false,
          subjectCategory = trimmedCategory
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
    storageManager?.saveAlarms(_alarms, currentAccountKey)
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
          onAttemptLogin = { typedUsername, typedPassword ->
            val result = resolvedViewModel.attemptLogin(typedUsername, typedPassword)
            if (result == LoginValidationResult.SUCCESS) {
              isLoggedIn = true
            }
            result
          },
          onCheckUsernameRegistered = { usernameToCheck ->
            resolvedViewModel.isUsernameRegistered(usernameToCheck)
          },
          onCompleteSignUp = { registeredUsername, createdPassword ->
            resolvedViewModel.completeSignUp(registeredUsername, createdPassword)
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
 * Canva-Inspired Startup / Login Screen with Simulated Username Authentication & Sign-Up Flow:
 * - Validates "Log In" against `SharedPreferences` registered accounts:
 *   * Checks that the username is not blank and has at least 3 characters.
 *   * Displays bold red `"No account found"` if username is not in `SharedPreferences`.
 *   * Displays bold red `"Incorrect password"` if username exists but password does not match.
 * - Includes a `"Sign Up"` button opening the Sign-Up & Verification Dialog.
 * - Includes `"Continue as Guest"` which opens the Guest Mode Warning Dialog and loads segregated `tasks_guest` and `schedules_guest` keys.
 */
@Composable
fun StartupLoginScreen(
  onAttemptLogin: (String, String) -> LoginValidationResult,
  onCheckUsernameRegistered: (String) -> Boolean,
  onCompleteSignUp: (String, String) -> Unit,
  onContinueAsGuest: () -> Unit,
  modifier: Modifier = Modifier
) {
  val metrics = rememberResponsiveMetrics()
  var username by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
  var loginErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
  var isSignUpDialogOpen by rememberSaveable { mutableStateOf(false) }
  var isGuestWarningDialogOpen by rememberSaveable { mutableStateOf(false) }

  val topGradientColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
  val baseBackground = MaterialTheme.colorScheme.background

  fun handleLoginClick() {
    if (!isValidUsername(username)) {
      loginErrorMessage = "Username must be at least 3 characters"
      return
    }
    when (onAttemptLogin(username, password)) {
      LoginValidationResult.INVALID_USERNAME -> {
        loginErrorMessage = "Username must be at least 3 characters"
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
          // Username OutlinedTextField
          OutlinedTextField(
            value = username,
            onValueChange = {
              username = it
              if (loginErrorMessage != null) loginErrorMessage = null
            },
            label = {
              Text(
                text = stringResource(R.string.username_label),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            placeholder = {
              Text(
                text = stringResource(R.string.username_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Person,
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
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Next
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_username_input")
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

          // Row of Primary "Log In" and Secondary "Sign Up" Buttons
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
        initialUsername = username,
        onCheckUsernameRegistered = onCheckUsernameRegistered,
        onDismiss = { isSignUpDialogOpen = false },
        onCompleteSignUp = { registeredUsername, createdPassword ->
          isSignUpDialogOpen = false
          onCompleteSignUp(registeredUsername, createdPassword)
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
 * - Step 1: User enters Username. Validates that the username is not blank and has at least 3 characters.
 *   If valid and already registered in `SharedPreferences`, displays `"Account already exists, please log in"`.
 * - Step 2: If the username is valid and new, clicking `"Send Verification Code"` simulates a 2-second
 *   loading state (`delay(2000)`), dynamically generates a 4-digit code via `(1000..9999).random().toString()`,
 *   and displays `"Verification code generated! Code: [RANDOM_CODE]"` on-screen and via Toast.
 * - Step 3: Shows a text field for the verification code. Requires the exact generated 4-digit code to unlock Step 4.
 * - Step 4: Shows a `"Create Password"` field and `"Complete Sign Up"` button that saves the
 *   account to `SharedPreferences` and logs the user in.
 */
@Composable
private fun SignUpVerificationDialog(
  initialUsername: String,
  onCheckUsernameRegistered: (String) -> Boolean,
  onDismiss: () -> Unit,
  onCompleteSignUp: (String, String) -> Unit
) {
  val context = LocalContext.current
  val metrics = rememberResponsiveMetrics()
  val coroutineScope = rememberCoroutineScope()

  var signUpUsername by rememberSaveable { mutableStateOf(initialUsername.trim()) }
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

        // Step 1: Username Field
        OutlinedTextField(
          value = signUpUsername,
          onValueChange = { updated ->
            signUpUsername = updated
            isCodeSent = false
            generatedVerificationCode = ""
            verificationCode = ""
            newPassword = ""
            val trimmed = updated.trim()
            signUpError = if (trimmed.isNotEmpty() && isValidUsername(trimmed) && onCheckUsernameRegistered(trimmed)) {
              "Account already exists, please log in"
            } else {
              null
            }
          },
          enabled = !isSendingCode,
          label = {
            Text(
              text = stringResource(R.string.username_label),
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1
            )
          },
          placeholder = {
            Text(
              text = stringResource(R.string.username_placeholder),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              maxLines = 1
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Person,
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
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("signup_username_input")
        )

        // Error message (e.g., "Username must be at least 3 characters" or "Account already exists, please log in")
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
            val trimmedUsername = signUpUsername.trim()
            if (!isValidUsername(trimmedUsername)) {
              signUpError = "Username must be at least 3 characters"
            } else if (onCheckUsernameRegistered(trimmedUsername)) {
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
                  "Verification code sent! Code: $dynamicCode",
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
              text = "Verification code sent! Code: $generatedVerificationCode",
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
              text = "Enter $generatedVerificationCode to verify your account",
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
              val trimmedUsername = signUpUsername.trim()
              if (!isValidUsername(trimmedUsername)) {
                signUpError = "Username must be at least 3 characters"
              } else if (onCheckUsernameRegistered(trimmedUsername)) {
                signUpError = "Account already exists, please log in"
              } else if (newPassword.isBlank()) {
                signUpError = "Please create a password"
              } else {
                onCompleteSignUp(trimmedUsername, newPassword)
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
      CustomDockedBottomTaskBar(
        selectedTab = selectedTab,
        onSelectTab = { selectedTab = it },
        metrics = metrics
      )
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
        CeNavTab.CLOCK -> ClockScreen(
          viewModel = taskViewModel,
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = metrics.maxContentWidth)
        )
        CeNavTab.EXTRAS -> ExtrasScreen(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = metrics.maxContentWidth)
        )
        CeNavTab.DASHBOARD -> DashboardScreen(
          viewModel = taskViewModel,
          onProfileIconClick = { isProfileSettingsDialogOpen = true },
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
      }
    }

    if (isAddTaskDialogOpen) {
      val schedulesSnapshot = taskViewModel.classSchedules.toList()
      val availableCategories = remember(schedulesSnapshot) {
        buildList {
          add(DEFAULT_TASK_CATEGORY)
          schedulesSnapshot
            .map { it.subjectName.trim() }
            .filter { it.isNotEmpty() && !it.equals(DEFAULT_TASK_CATEGORY, ignoreCase = true) }
            .distinctBy { it.lowercase(Locale.US) }
            .forEach { add(it) }
        }
      }
      val preselectedCategory = availableCategories.firstOrNull {
        it.equals(taskViewModel.selectedTaskFolder, ignoreCase = true)
      } ?: DEFAULT_TASK_CATEGORY

      AddTaskDialog(
        availableCategories = availableCategories,
        initialCategory = preselectedCategory,
        onDismiss = { isAddTaskDialogOpen = false },
        onAddTask = { newTaskTitle, newTaskDueDate, selectedCategory ->
          taskViewModel.addTask(
            title = newTaskTitle,
            dueDate = newTaskDueDate,
            subjectCategory = selectedCategory
          )
          taskViewModel.selectTaskFolder(selectedCategory)
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
 * Redesigned Custom Bottom Task Bar docked at the bottom of the Scaffold:
 * - Uses the warm cream card palette (`#FFFBF0`) in Light Mode and dark surface in Dark Mode.
 * - Arranges the 5 destinations horizontally from Left to Right:
 *   Clock, Extras, [Spacer / Center Floating Dashboard Button], Tasks, Schedule.
 * - Center Item (Dashboard): Prominent, floating-style button overlapping the center of the bar
 *   with distinct depth (`elevation = 12.dp`), rounded-square/circular shape, colored with
 *   `MaterialTheme.colorScheme.primary` and a high-contrast white/light icon (`onPrimary`).
 * - 4 Side Items (Clock, Extras, Tasks, Schedule): Highlighted with subtle colors when active.
 */
@Composable
private fun CustomDockedBottomTaskBar(
  selectedTab: CeNavTab,
  onSelectTab: (CeNavTab) -> Unit,
  metrics: ResponsiveMetrics,
  modifier: Modifier = Modifier
) {
  val barContainerColor = dashboardCreamCardColor()
  val isLight = MaterialTheme.colorScheme.background.red > 0.5f
  val topBorderColor = if (isLight) {
    Color(0xFFE6DEC8)
  } else {
    MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .testTag("bottom_navigation_bar"),
    contentAlignment = Alignment.BottomCenter
  ) {
    // Docked Cream Bar Surface
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 18.dp),
      color = barContainerColor,
      tonalElevation = 6.dp,
      shadowElevation = 10.dp,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      border = BorderStroke(1.dp, topBorderColor)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = metrics.maxContentWidth)
          .padding(horizontal = metrics.scaledDp(8), vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left Side: Clock & Extras
        CustomSideBottomBarItem(
          tab = CeNavTab.CLOCK,
          isSelected = selectedTab == CeNavTab.CLOCK,
          onClick = { onSelectTab(CeNavTab.CLOCK) },
          metrics = metrics,
          modifier = Modifier.weight(1f)
        )
        CustomSideBottomBarItem(
          tab = CeNavTab.EXTRAS,
          isSelected = selectedTab == CeNavTab.EXTRAS,
          onClick = { onSelectTab(CeNavTab.EXTRAS) },
          metrics = metrics,
          modifier = Modifier.weight(1f)
        )

        // Center Spacer reserved for the overlapping Dashboard button
        Spacer(modifier = Modifier.width(metrics.scaledDp(76)))

        // Right Side: Tasks & Schedule
        CustomSideBottomBarItem(
          tab = CeNavTab.TASKS,
          isSelected = selectedTab == CeNavTab.TASKS,
          onClick = { onSelectTab(CeNavTab.TASKS) },
          metrics = metrics,
          modifier = Modifier.weight(1f)
        )
        CustomSideBottomBarItem(
          tab = CeNavTab.SCHEDULE,
          isSelected = selectedTab == CeNavTab.SCHEDULE,
          onClick = { onSelectTab(CeNavTab.SCHEDULE) },
          metrics = metrics,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Prominent Floating Center Button for Dashboard overlapping the center of the bar
    val isDashboardSelected = selectedTab == CeNavTab.DASHBOARD
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(bottom = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Card(
        onClick = { onSelectTab(CeNavTab.DASHBOARD) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(
          defaultElevation = 12.dp,
          pressedElevation = 16.dp
        ),
        border = if (isDashboardSelected) {
          BorderStroke(2.5.dp, barContainerColor)
        } else {
          BorderStroke(1.5.dp, barContainerColor.copy(alpha = 0.85f))
        },
        modifier = Modifier
          .size(metrics.scaledDp(58))
          .testTag(CeNavTab.DASHBOARD.routeTag)
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isDashboardSelected) {
              CeNavTab.DASHBOARD.selectedIcon
            } else {
              CeNavTab.DASHBOARD.unselectedIcon
            },
            contentDescription = CeNavTab.DASHBOARD.title,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }
      }

      Text(
        text = CeNavTab.DASHBOARD.title,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = metrics.scaledSp(11),
          fontWeight = if (isDashboardSelected) FontWeight.Bold else FontWeight.SemiBold
        ),
        color = if (isDashboardSelected) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun CustomSideBottomBarItem(
  tab: CeNavTab,
  isSelected: Boolean,
  onClick: () -> Unit,
  metrics: ResponsiveMetrics,
  modifier: Modifier = Modifier
) {
  val activePillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
  val iconTint = if (isSelected) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.onSurfaceVariant
  }
  val textColor = if (isSelected) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.onSurfaceVariant
  }

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .minimumInteractiveComponentSize()
      .padding(vertical = 4.dp, horizontal = 4.dp)
      .testTag(tab.routeTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    Box(
      modifier = Modifier
        .height(32.dp)
        .width(52.dp)
        .clip(CircleShape)
        .background(if (isSelected) activePillColor else Color.Transparent),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
        contentDescription = tab.title,
        tint = iconTint,
        modifier = Modifier.size(22.dp)
      )
    }

    Text(
      text = tab.title,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = metrics.scaledSp(11),
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
      ),
      color = textColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

/**
 * Main Academic Dashboard Screen (single, static vertical scrolling screen without HorizontalPager or right-edge Extras pill):
 * - Subtle geometric Canvas background behind dashboard content.
 * - Real Open-Meteo Weather Summary & live ticking `LocalDateTime` header.
 * - Clickable upper-right Profile Icon opening the Settings Dialog.
 * - Side-by-side "Daily Progress" ring + "Today's Schedule" interactive card.
 * - Dynamic "Upcoming Deadlines" Task List connected to `TaskViewModel`.
 */
@Composable
fun DashboardScreen(
  viewModel: TaskViewModel,
  onProfileIconClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val metrics = rememberResponsiveMetrics()

  val allTasks = viewModel.tasks
  val tasksSnapshot = allTasks.toList()
  val totalTasks = tasksSnapshot.size
  val completedTasks = remember(tasksSnapshot) { tasksSnapshot.count { it.isCompleted } }
  val activeTasks = remember(tasksSnapshot) { tasksSnapshot.filter { !it.isCompleted } }

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

    // Single, static vertical scrolling Dashboard Content
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = metrics.maxContentWidth)
        .testTag("academic_dashboard_list"),
      contentPadding = PaddingValues(
        horizontal = metrics.scaledDp(16),
        vertical = metrics.scaledDp(20)
      ),
      verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(20))
    ) {
      // 1. Dynamic Top Header: Logged-in Username ("Guest User" if guest), Weather Summary, Live LocalDateTime, and Profile Icon
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
            Text(
              text = viewModel.displayUsername,
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = metrics.scaledSp(16),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.primary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_username_header")
            )

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
        val todayAbbreviation = remember { resolveTodayCorDayAbbreviation() }
        val schedulesSnapshot = viewModel.classSchedules.toList()
        val todaysFilteredClasses = remember(schedulesSnapshot, todayAbbreviation) {
          viewModel.getSchedulesForDayAbbreviation(todayAbbreviation)
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
 * - Uses `LazyColumn` to recycle schedule items efficiently on low-end devices.
 * - Displays classes from `ViewModel.classSchedules` filtered by `LocalDate.now().dayOfWeek`
 *   ("M", "Tue", "W", "Th", "F", "Sat").
 * - If the filtered list is empty, displays a cleanly centered "Empty" label inside the card.
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
        // LazyColumn of today's classes inside the card for low-end device view recycling
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .testTag("todays_schedule_lazy_list"),
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
        ) {
          items(
            items = todaysClasses,
            key = { it.id }
          ) { item ->
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
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
          ) {
            Text(
              text = task.subjectCategory.ifBlank { DEFAULT_TASK_CATEGORY },
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = metrics.scaledSp(10),
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .widthIn(max = 110.dp)
                .padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Dedicated Extras Screen (`ExtrasScreen`):
 * Contains the engineering tool placeholders arranged in a neat 2x2 grid with floating elevation
 * and cream card styling (`#FFFBF0`):
 * - Scientific Calculator
 * - Unit Converter
 * - BOQ Estimator
 * - AI Study Assistant
 */
@Composable
fun ExtrasScreen(
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
    // Top Header
    Column(
      modifier = Modifier.fillMaxWidth(),
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
private data class PositionedGridClassBlock(
  val classInfo: ClassSchedule,
  val startFloat: Float,
  val endFloat: Float,
  val topOffset: Dp,
  val cardHeight: Dp,
  val blockColor: Color,
  val primaryTextColor: Color,
  val secondaryTextColor: Color
)

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
  var focusedClass by remember { mutableStateOf<ClassSchedule?>(null) }

  val allSchedules = viewModel.classSchedules.toList()
  // Actively filters AND chronologically sorts classes by Start Time via ViewModel, memoized for low-end devices
  val filteredSchedules = remember(allSchedules, selectedWeekDay) {
    viewModel.getSchedulesForDay(selectedWeekDay)
  }
  val dayCountsMap = remember(allSchedules) {
    ScheduleWeekDay.entries.associateWith { day ->
      viewModel.getSchedulesForDay(day).size
    }
  }

  if (focusedClass != null) {
    BackHandler {
      focusedClass = null
    }
  }

  Box(
    modifier = modifier.fillMaxSize()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("schedule_screen_list"),
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
            onClick = {
              isGridView = !isGridView
              if (!isGridView) focusedClass = null
            },
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
            onLongPressClass = { longPressedSchedule ->
              focusedClass = longPressedSchedule
            },
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
              val countForDay = dayCountsMap[day] ?: 0

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

    // Full-screen Long-Press Details Overlay with performant 0.6f Black Scrim & large Cream Card
    focusedClass?.let { detailedClass ->
      val accentColor = Color(detailedClass.colorHex)
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.6f))
          .clickable { focusedClass = null }
          .testTag("schedule_long_press_scrim"),
        contentAlignment = Alignment.Center
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth(0.88f)
            .widthIn(max = 460.dp)
            .pointerInput(Unit) {
              detectTapGestures(onTap = { /* Consume tap inside card so scrim doesn't dismiss */ })
            }
            .testTag("schedule_long_press_detail_card"),
          shape = RoundedCornerShape(28.dp),
          colors = CardDefaults.cardColors(
            containerColor = CreamCardColor
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
          border = BorderStroke(2.dp, accentColor)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(metrics.scaledDp(26)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
          ) {
            // Color Accent & Scheduled Days Pill
            Surface(
              color = accentColor,
              shape = CircleShape
            ) {
              Text(
                text = detailedClass.days.joinToString(" • "),
                style = MaterialTheme.typography.labelLarge.copy(
                  fontSize = metrics.scaledSp(13),
                  fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
              )
            }

            // 1. Full, Untruncated Subject Name
            Text(
              text = detailedClass.subjectName,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = metrics.scaledSp(24),
                lineHeight = metrics.scaledSp(30),
                fontWeight = FontWeight.Bold
              ),
              color = Color(0xFF1C1B1F),
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("focused_class_subject_text")
            )

            HorizontalDivider(
              color = WarmCreamTrackColor,
              thickness = 1.dp
            )

            // 2. Full, Untruncated Room Assignment
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = detailedClass.room.ifBlank { "TBA" },
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = metrics.scaledSp(20),
                  lineHeight = metrics.scaledSp(26),
                  fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1C1B1F),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("focused_class_room_text")
              )
            }

            // 3. Full, Untruncated Time Duration
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = detailedClass.timeRange,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = metrics.scaledSp(19),
                  lineHeight = metrics.scaledSp(25),
                  fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF3E3B32),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("focused_class_time_text")
              )
            }
          }
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
 * - Prominent horizontal "LUNCH" divider block at the `12:00` mark.
 * - Top X-axis: `MON, TUE, WED, THU, FRI, SAT`.
 * - Colored `ClassSchedule` blocks with memoized time-to-float layout math and long-press gesture detection.
 */
@Composable
private fun ScheduleTimetableGridView(
  schedules: List<ClassSchedule>,
  selectedWeekDay: ScheduleWeekDay,
  onSelectWeekDay: (ScheduleWeekDay) -> Unit,
  onLongPressClass: (ClassSchedule) -> Unit = {},
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

  // Memoize all time-to-float calculations and layout positioning keyed to the schedule list
  // so low-end devices do not recalculate math on every frame.
  val positionedBlocksByDay = remember(schedules, hourHeight, lunchRowHeight) {
    val minBlockHeight = metrics.scaledDp(26)
    ScheduleWeekDay.entries.associateWith { day ->
      schedules
        .filter { sched -> sched.days.any { token -> day.matchesToken(token) } }
        .map { classInfo ->
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
            .coerceAtLeast(minBlockHeight)
          val blockColor = Color(classInfo.colorHex)

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

          PositionedGridClassBlock(
            classInfo = classInfo,
            startFloat = startFloat,
            endFloat = endFloat,
            topOffset = topOffset,
            cardHeight = cardHeight,
            blockColor = blockColor,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor
          )
        }
        .sortedBy { it.startFloat }
    }
  }

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
                val dayPositionedBlocks = remember(positionedBlocksByDay, day) {
                  positionedBlocksByDay[day].orEmpty()
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

                  // Box overlay for class blocks using pre-remembered positions and long-press gesture
                  dayPositionedBlocks.forEach { positioned ->
                    val classInfo = positioned.classInfo
                    val currentClassInfo by rememberUpdatedState(classInfo)
                    val currentOnLongPress by rememberUpdatedState(onLongPressClass)

                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = positioned.blockColor,
                      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                      shadowElevation = 4.dp,
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                        .offset(y = positioned.topOffset)
                        .height(positioned.cardHeight)
                        .pointerInput(Unit) {
                          detectTapGestures(
                            onLongPress = {
                              currentOnLongPress(currentClassInfo)
                            }
                          )
                        }
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
                          color = positioned.primaryTextColor,
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
                            color = positioned.secondaryTextColor,
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
                          color = positioned.secondaryTextColor,
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

        // 6. Card Accent Color Picker Row (LazyRow for low-end device smoothness)
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          items(
            items = availableColors,
            key = { it }
          ) { colorHex ->
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
  val isGuest = viewModel.isGuestMode

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
                  text = "Guest User",
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
                    text = viewModel.displayUsername,
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
                    text = "@${viewModel.username}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontSize = metrics.scaledSp(14)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("account_username_handle_text")
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
 * Coursework & Task Manager Screen connected to the shared `TaskViewModel`:
 * 1. Top horizontal scrollable row (`LazyRow`) of selectable Subject "Folder" chips:
 *    - Starts with a permanent `"General"` folder, followed by unique `subjectName`s extracted
 *      directly from the `ViewModel`'s `ClassSchedule` list.
 *    - Highlights the currently selected folder using `MaterialTheme.colorScheme.primary`, while
 *      unselected folders use the cream background (`#FFFBF0`).
 * 2. Main `LazyColumn` below the folders displaying ONLY tasks that match the currently selected
 *    folder's `subjectCategory`.
 * 3. Task cards styled with the warm cream color palette (`#FFFBF0`) and `elevation = 8.dp`.
 */
@Composable
fun CourseworkTaskManagerScreen(
  viewModel: TaskViewModel,
  modifier: Modifier = Modifier
) {
  val metrics = rememberResponsiveMetrics()
  val allTasksSnapshot = viewModel.tasks.toList()
  val schedulesSnapshot = viewModel.classSchedules.toList()

  // Dynamically generate Subject Folders: permanent "General" + unique subjectNames from ClassSchedule list
  val subjectFolders = remember(schedulesSnapshot) {
    buildList {
      add(DEFAULT_TASK_CATEGORY)
      schedulesSnapshot
        .map { it.subjectName.trim() }
        .filter { it.isNotEmpty() && !it.equals(DEFAULT_TASK_CATEGORY, ignoreCase = true) }
        .distinctBy { it.lowercase(Locale.US) }
        .forEach { add(it) }
    }
  }

  val activeFolder = remember(subjectFolders, viewModel.selectedTaskFolder) {
    subjectFolders.firstOrNull {
      it.equals(viewModel.selectedTaskFolder, ignoreCase = true)
    } ?: DEFAULT_TASK_CATEGORY
  }

  // Ensure ViewModel's selected folder stays valid if a schedule subject was removed
  LaunchedEffect(activeFolder) {
    if (!viewModel.selectedTaskFolder.equals(activeFolder, ignoreCase = true)) {
      viewModel.selectTaskFolder(activeFolder)
    }
  }

  // Filter tasks to only those matching the currently selected folder's subjectCategory
  val filteredTasks = remember(allTasksSnapshot, activeFolder) {
    allTasksSnapshot.filter { task ->
      val taskCategory = task.subjectCategory.trim().ifEmpty { DEFAULT_TASK_CATEGORY }
      taskCategory.equals(activeFolder, ignoreCase = true)
    }
  }

  val folderCountsMap = remember(allTasksSnapshot, subjectFolders) {
    subjectFolders.associateWith { folder ->
      allTasksSnapshot.count { task ->
        val taskCategory = task.subjectCategory.trim().ifEmpty { DEFAULT_TASK_CATEGORY }
        taskCategory.equals(folder, ignoreCase = true) && !task.isCompleted
      }
    }
  }

  val remainingInFolderCount = remember(filteredTasks) {
    filteredTasks.count { !it.isCompleted }
  }

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
    // 1. Screen Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 2.dp),
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
            text = "$remainingInFolderCount Active",
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
              horizontal = metrics.scaledDp(14),
              vertical = metrics.scaledDp(6)
            )
          )
        }
      }
    }

    // 2. Dynamic Subject Folders Horizontal Scrollable Row (LazyRow)
    item {
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp)
          .testTag("task_subject_folders_row"),
        horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(10)),
        verticalAlignment = Alignment.CenterVertically
      ) {
        items(
          items = subjectFolders,
          key = { it.lowercase(Locale.US) }
        ) { folderName ->
          val isSelected = folderName.equals(activeFolder, ignoreCase = true)
          val activeCount = folderCountsMap[folderName] ?: 0
          val chipBgColor = if (isSelected) {
            MaterialTheme.colorScheme.primary
          } else {
            CreamCardColor
          }
          val chipTextColor = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
          } else {
            Color(0xFF1E293B)
          }

          Surface(
            onClick = { viewModel.selectTaskFolder(folderName) },
            shape = RoundedCornerShape(18.dp),
            color = chipBgColor,
            border = if (isSelected) {
              null
            } else {
              BorderStroke(1.5.dp, Color(0xFFE6DFCF))
            },
            shadowElevation = if (isSelected) 8.dp else 4.dp,
            modifier = Modifier
              .height(metrics.scaledDp(44))
              .testTag("task_folder_chip_${folderName.lowercase(Locale.US).replace(" ", "_")}")
          ) {
            Row(
              modifier = Modifier
                .padding(
                  horizontal = metrics.scaledDp(14),
                  vertical = metrics.scaledDp(8)
                ),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
            ) {
              Icon(
                imageVector = if (isSelected) Icons.Filled.Folder else Icons.Outlined.Folder,
                contentDescription = null,
                tint = if (isSelected) {
                  MaterialTheme.colorScheme.onPrimary
                } else {
                  Color(0xFF4F46E5)
                },
                modifier = Modifier.size(18.dp)
              )

              Text(
                text = folderName,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontSize = metrics.scaledSp(14),
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = chipTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 180.dp)
              )

              if (activeCount > 0) {
                Surface(
                  color = if (isSelected) {
                    Color.White.copy(alpha = 0.24f)
                  } else {
                    Color(0xFFEEF2FF)
                  },
                  shape = CircleShape
                ) {
                  Text(
                    text = activeCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = metrics.scaledSp(11),
                      fontWeight = FontWeight.Bold
                    ),
                    color = if (isSelected) {
                      MaterialTheme.colorScheme.onPrimary
                    } else {
                      Color(0xFF4F46E5)
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
    }

    // 3. Filtered Task Cards List for Selected Subject Folder
    if (filteredTasks.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .testTag("empty_tasks_card"),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = CreamCardColor),
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
              imageVector = Icons.Outlined.Folder,
              contentDescription = null,
              tint = Color(0xFF4F46E5),
              modifier = Modifier.size(36.dp)
            )
            Text(
              text = "No tasks in \"$activeFolder\"",
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = metrics.scaledSp(19),
                fontWeight = FontWeight.Bold
              ),
              color = Color(0xFF1E293B),
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "Tap the + button in the bottom corner to add a task to this subject folder.",
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = metrics.scaledSp(15)
              ),
              color = Color(0xFF57534E),
              textAlign = TextAlign.Center,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    } else {
      items(
        items = filteredTasks,
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
 * Cream-themed Task row card (`#FFFBF0`) with `elevation = 8.dp`, high-contrast charcoal/indigo
 * typography and checkbox colors, and `maxLines` + `TextOverflow.Ellipsis` on all text elements.
 */
@Composable
private fun TaskItemRowCard(
  task: Task,
  metrics: ResponsiveMetrics,
  onToggle: () -> Unit,
  onDelete: () -> Unit
) {
  val creamBackground = CreamCardColor
  val titleTextColor = if (task.isCompleted) {
    Color(0xFF64748B)
  } else {
    Color(0xFF1E293B)
  }
  val accentColor = if (task.isCompleted) {
    Color(0xFF64748B)
  } else {
    Color(0xFF4F46E5)
  }
  val subtextColor = Color(0xFF57534E)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onToggle)
      .testTag("task_item_card_${task.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = creamBackground
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 8.dp,
      pressedElevation = 10.dp
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
            checkedColor = Color(0xFF4F46E5),
            checkmarkColor = Color.White,
            uncheckedColor = Color(0xFF475569)
          ),
          modifier = Modifier
            .size(24.dp)
            .testTag("task_checkbox_${task.id}")
        )
      }

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = task.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(16),
            fontWeight = FontWeight.Bold
          ),
          color = titleTextColor,
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
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.CalendarToday,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = "Due: ${task.dueDate}",
              style = MaterialTheme.typography.labelMedium.copy(
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.SemiBold
              ),
              color = accentColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )
          }

          Surface(
            color = if (task.isCompleted) {
              Color(0xFFE6DFCF)
            } else {
              Color(0xFFEEF2FF)
            },
            shape = CircleShape
          ) {
            Text(
              text = task.subjectCategory.ifBlank { DEFAULT_TASK_CATEGORY },
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = metrics.scaledSp(11),
                fontWeight = FontWeight.Bold
              ),
              color = if (task.isCompleted) subtextColor else Color(0xFF3730A3),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .widthIn(max = 120.dp)
                .padding(horizontal = 9.dp, vertical = 3.dp)
                .testTag("task_category_badge_${task.id}")
            )
          }
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
          tint = subtextColor,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

/**
 * Cream-styled Dialog for adding a new coursework task:
 * 1. Task Title (`OutlinedTextField`)
 * 2. Due Date (`OutlinedTextField`)
 * 3. Subject Category horizontal scrollable row of chips (`LazyRow`) showing `"General"` +
 *    extracted schedule subjects, pre-selected based on the folder currently viewed.
 */
@Composable
private fun AddTaskDialog(
  availableCategories: List<String> = listOf(DEFAULT_TASK_CATEGORY),
  initialCategory: String = DEFAULT_TASK_CATEGORY,
  onDismiss: () -> Unit,
  onAddTask: (String, String, String) -> Unit
) {
  val metrics = rememberResponsiveMetrics()
  var taskTitle by rememberSaveable { mutableStateOf("") }
  var taskDueDate by rememberSaveable { mutableStateOf("") }
  val categories = remember(availableCategories) {
    if (availableCategories.isEmpty()) listOf(DEFAULT_TASK_CATEGORY) else availableCategories
  }
  var selectedCategory by rememberSaveable(initialCategory) {
    mutableStateOf(
      categories.firstOrNull { it.equals(initialCategory, ignoreCase = true) }
        ?: DEFAULT_TASK_CATEGORY
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_task_dialog"),
    shape = RoundedCornerShape(24.dp),
    containerColor = CreamCardColor,
    title = {
      Text(
        text = stringResource(R.string.add_task_dialog_title),
        style = MaterialTheme.typography.headlineMedium.copy(
          fontSize = metrics.scaledSp(21),
          fontWeight = FontWeight.Bold
        ),
        color = Color(0xFF1E293B),
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
              color = Color(0xFF57534E),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          placeholder = {
            Text(
              text = stringResource(R.string.add_task_input_placeholder),
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFF57534E).copy(alpha = 0.65f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = Color(0xFF1E293B)
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color(0xFFD6CFC0),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = Color(0xFF1E293B),
            unfocusedTextColor = Color(0xFF1E293B)
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
              color = Color(0xFF57534E),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          placeholder = {
            Text(
              text = stringResource(R.string.due_date_placeholder),
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFF57534E).copy(alpha = 0.65f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.CalendarToday,
              contentDescription = null,
              tint = Color(0xFF4F46E5),
              modifier = Modifier.size(22.dp)
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = Color(0xFF1E293B)
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color(0xFFD6CFC0),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = Color(0xFF1E293B),
            unfocusedTextColor = Color(0xFF1E293B)
          ),
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              if (taskTitle.isNotBlank()) {
                onAddTask(taskTitle, taskDueDate, selectedCategory)
              }
            }
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_task_due_date_input")
        )

        // Subject Category Selector ("General" + extracted schedule subjects)
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
        ) {
          Text(
            text = stringResource(R.string.task_subject_category_label),
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = metrics.scaledSp(14),
              fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF1E293B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          LazyRow(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("add_task_category_chips_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            items(
              items = categories,
              key = { it.lowercase(Locale.US) }
            ) { categoryName ->
              val isSelected = categoryName.equals(selectedCategory, ignoreCase = true)
              Surface(
                onClick = { selectedCategory = categoryName },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  IvoryInnerCardColor
                },
                border = if (isSelected) {
                  null
                } else {
                  BorderStroke(1.dp, Color(0xFFD6CFC0))
                },
                shadowElevation = if (isSelected) 4.dp else 1.dp,
                modifier = Modifier.testTag(
                  "add_task_category_chip_${categoryName.lowercase(Locale.US).replace(" ", "_")}"
                )
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = if (isSelected) Icons.Filled.Folder else Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = if (isSelected) {
                      MaterialTheme.colorScheme.onPrimary
                    } else {
                      Color(0xFF4F46E5)
                    },
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = categoryName,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontSize = metrics.scaledSp(13),
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = if (isSelected) {
                      MaterialTheme.colorScheme.onPrimary
                    } else {
                      Color(0xFF1E293B)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 170.dp)
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onAddTask(taskTitle, taskDueDate, selectedCategory) },
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
          color = Color(0xFF57534E),
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

// ============================================================================
// GOOGLE CLOCK INSPIRED CLOCK SCREEN (ALARM, TIMER, STOPWATCH)
// ============================================================================

private enum class ClockSubTab(val title: String, val icon: ImageVector, val testTag: String) {
  ALARM("Alarm", Icons.Filled.Alarm, "clock_tab_alarm"),
  TIMER("Timer", Icons.Filled.Timer, "clock_tab_timer"),
  STOPWATCH("Stopwatch", Icons.Filled.AvTimer, "clock_tab_stopwatch")
}

data class AlarmItem(
  val id: Int,
  val name: String,
  val hour: String,
  val minute: String,
  val amPm: String,
  val activeDays: List<Int>,
  val isActive: Boolean
)

data class StopwatchLapItem(
  val lapNumber: Int,
  val lapSplitMillis: Long,
  val totalElapsedMillis: Long
)

data class CeFocusPreset(
  val label: String,
  val durationSeconds: Long,
  val subtitle: String,
  val testTag: String
)

private val CE_FOCUS_PRESETS = listOf(
  CeFocusPreset(
    label = "Pomodoro (25m)",
    durationSeconds = 25L * 60L,
    subtitle = "Standard 25-min deep engineering sprint",
    testTag = "preset_chip_pomodoro"
  ),
  CeFocusPreset(
    label = "CELE: Structural (5h)",
    durationSeconds = 5L * 3600L,
    subtitle = "Full 5-hour Structural Engineering & Construction board exam simulation",
    testTag = "preset_chip_structural"
  ),
  CeFocusPreset(
    label = "CELE: Hydraulics (4h)",
    durationSeconds = 4L * 3600L,
    subtitle = "Full 4-hour Hydraulics & Geotechnical Engineering board exam simulation",
    testTag = "preset_chip_hydraulics"
  )
)

/**
 * Plays the device's native alarm sound using RingtoneManager.
 * Uses TYPE_ALARM first as required, with safe fallback to TYPE_NOTIFICATION or TYPE_RINGTONE
 * if the device has no default alarm URI configured.
 */
fun playNativeAlarmSound(context: Context): Ringtone? {
  return try {
    val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    val ringtone = RingtoneManager.getRingtone(context, alarmUri)
    ringtone?.play()
    ringtone
  } catch (e: Exception) {
    null
  }
}

@Composable
fun ClockScreen(
  viewModel: TaskViewModel? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val localStorageManager = remember(context) { LocalStorageManager(context) }
  val metrics = rememberResponsiveMetrics()
  val creamCardColor = dashboardCreamCardColor()
  val ivoryInnerColor = if (MaterialTheme.colorScheme.background == CanvaLightColorScheme.background) {
    IvoryInnerCardColor
  } else {
    MaterialTheme.colorScheme.surfaceVariant
  }

  var selectedSubTab by rememberSaveable { mutableStateOf(ClockSubTab.ALARM) }

  // Notification Permission state (Android 13+)
  var hasNotificationPermission by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
      } else {
        true
      }
    )
  }
  var isPermissionRationaleDismissed by rememberSaveable { mutableStateOf(false) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
  }

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  // Active Ringtone state so user can stop ringing alarm or timer
  var activeRingtone by remember { mutableStateOf<Ringtone?>(null) }
  var ringingBannerMessage by remember { mutableStateOf<String?>(null) }

  fun triggerAlarmRingtone(message: String) {
    try {
      activeRingtone?.stop()
    } catch (_: Exception) {
    }
    activeRingtone = playNativeAlarmSound(context)
    ringingBannerMessage = message
  }

  fun stopActiveRingtone() {
    try {
      activeRingtone?.stop()
    } catch (_: Exception) {
    }
    activeRingtone = null
    ringingBannerMessage = null
  }

  DisposableEffect(Unit) {
    onDispose {
      try {
        activeRingtone?.stop()
      } catch (_: Exception) {
      }
    }
  }

  // ==========================================================================
  // ALARM STATE (SHARED PREFERENCES BACKED, STARTS COMPLETELY EMPTY)
  // ==========================================================================
  val fallbackAlarms = remember {
    val saved = localStorageManager.loadAlarms(LocalStorageManager.GUEST_ACCOUNT_KEY) ?: emptyList()
    mutableStateListOf<AlarmItem>().apply { addAll(saved) }
  }
  val alarms: List<AlarmItem> = viewModel?.alarms ?: fallbackAlarms
  var isAddAlarmDialogOpen by rememberSaveable { mutableStateOf(false) }
  var lastTriggeredAlarmKey by remember { mutableStateOf("") }

  // Check current device time every second while ClockScreen is open
  LaunchedEffect(alarms.toList()) {
    while (true) {
      val now = LocalTime.now()
      val currentHour24 = now.hour
      val currentMinute = now.minute
      val currentAmPm = if (currentHour24 < 12) "AM" else "PM"
      val currentHour12 = when {
        currentHour24 == 0 -> 12
        currentHour24 > 12 -> currentHour24 - 12
        else -> currentHour24
      }
      // Map Java DayOfWeek (1=Mon..7=Sun) to 0..6 (0=Sun..6=Sat)
      val currentDayIndex = LocalDate.now().dayOfWeek.value % 7

      val matchingAlarm = alarms.firstOrNull { alarm ->
        val alarmHour = alarm.hour.toIntOrNull() ?: -1
        val alarmMinute = alarm.minute.toIntOrNull() ?: -1
        val isDayMatch = alarm.activeDays.isEmpty() || alarm.activeDays.contains(currentDayIndex)
        alarm.isActive &&
          isDayMatch &&
          alarmHour == currentHour12 &&
          alarmMinute == currentMinute &&
          alarm.amPm.equals(currentAmPm, ignoreCase = true)
      }

      if (matchingAlarm != null) {
        val triggerKey = "${matchingAlarm.id}_${currentHour24}_$currentMinute"
        if (lastTriggeredAlarmKey != triggerKey) {
          lastTriggeredAlarmKey = triggerKey
          val formattedTime = "${matchingAlarm.hour}:${matchingAlarm.minute} ${matchingAlarm.amPm}"
          triggerAlarmRingtone("Alarm ringing: $formattedTime (${matchingAlarm.name})")
        }
      }
      delay(1000L)
    }
  }

  // ==========================================================================
  // TIMER STATE & COUNTDOWN ENGINE
  // ==========================================================================
  // Digit buffer for custom keypad input (up to 6 digits: HHMMSS)
  var timerDigitInput by rememberSaveable { mutableStateOf("") }
  var timerTotalDurationSeconds by rememberSaveable { mutableLongStateOf(0L) }
  var timerRemainingSeconds by rememberSaveable { mutableLongStateOf(0L) }
  var isTimerRunning by rememberSaveable { mutableStateOf(false) }
  var activePresetLabel by rememberSaveable { mutableStateOf<String?>(null) }

  LaunchedEffect(isTimerRunning) {
    while (isTimerRunning && timerRemainingSeconds > 0L) {
      delay(1000L)
      if (isTimerRunning && timerRemainingSeconds > 0L) {
        timerRemainingSeconds -= 1L
        if (timerRemainingSeconds == 0L) {
          isTimerRunning = false
          val labelSuffix = activePresetLabel?.let { " ($it)" } ?: ""
          triggerAlarmRingtone("Timer finished: 00:00:00$labelSuffix")
        }
      }
    }
  }

  // ==========================================================================
  // STOPWATCH STATE & HIGH-PRECISION TICKER
  // ==========================================================================
  var isStopwatchRunning by rememberSaveable { mutableStateOf(false) }
  var stopwatchElapsedMillis by rememberSaveable { mutableLongStateOf(0L) }
  val stopwatchLaps = remember { mutableStateListOf<StopwatchLapItem>() }

  LaunchedEffect(isStopwatchRunning) {
    if (isStopwatchRunning) {
      val baseTime = SystemClock.elapsedRealtime() - stopwatchElapsedMillis
      while (isStopwatchRunning) {
        stopwatchElapsedMillis = SystemClock.elapsedRealtime() - baseTime
        delay(16L)
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("clock_screen")
  ) {
    // Top Google Clock-style TabRow (Alarm, Timer, Stopwatch)
    Surface(
      color = creamCardColor,
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(
              horizontal = metrics.scaledDp(20),
              vertical = metrics.scaledDp(12)
            ),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Clock",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = metrics.scaledSp(22)
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "CE Time Suite",
            style = MaterialTheme.typography.labelMedium.copy(
              fontFamily = JetBrainsMonoFamily,
              fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary
          )
        }

        TabRow(
          selectedTabIndex = selectedSubTab.ordinal,
          containerColor = creamCardColor,
          contentColor = MaterialTheme.colorScheme.primary,
          indicator = { tabPositions ->
            if (selectedSubTab.ordinal < tabPositions.size) {
              TabRowDefaults.SecondaryIndicator(
                modifier = Modifier
                  .tabIndicatorOffset(tabPositions[selectedSubTab.ordinal])
                  .height(3.dp)
                  .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                color = MaterialTheme.colorScheme.primary
              )
            }
          },
          divider = {
            HorizontalDivider(
              thickness = 1.dp,
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("clock_top_tab_row")
        ) {
          ClockSubTab.entries.forEach { tab ->
            val selected = selectedSubTab == tab
            Tab(
              selected = selected,
              onClick = { selectedSubTab = tab },
              modifier = Modifier
                .height(metrics.scaledDp(52))
                .testTag(tab.testTag),
              text = {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    modifier = Modifier.size(18.dp),
                    tint = if (selected) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    }
                  )
                  Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = metrics.scaledSp(13)
                    ),
                    color = if (selected) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            )
          }
        }
      }
    }

    // Dismissible POST_NOTIFICATIONS Permission Rationale Card when denied
    if (!hasNotificationPermission && !isPermissionRationaleDismissed) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = creamCardColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = metrics.scaledDp(16),
            vertical = metrics.scaledDp(10)
          )
          .testTag("clock_permission_rationale_card")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(metrics.scaledDp(14)),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            modifier = Modifier.size(40.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = "Notification permission required",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Please grant notification permissions so your alarms can ring.",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = metrics.scaledSp(13)
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              TextButton(
                onClick = {
                  notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.testTag("grant_notification_permission_button")
              ) {
                Text(
                  text = "Grant Permission",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }

          IconButton(
            onClick = { isPermissionRationaleDismissed = true },
            modifier = Modifier
              .size(48.dp)
              .testTag("dismiss_permission_rationale_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Dismiss notification rationale",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Active ringing alarm/timer banner with Silence button
    ringingBannerMessage?.let { activeMessage ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = metrics.scaledDp(16),
            vertical = metrics.scaledDp(8)
          )
          .testTag("active_ringing_banner")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = metrics.scaledDp(16), vertical = metrics.scaledDp(12)),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.NotificationsActive,
              contentDescription = "Ringing",
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = activeMessage,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
          Button(
            onClick = { stopActiveRingtone() },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("stop_alarm_sound_button")
          ) {
            Icon(
              imageVector = Icons.Filled.VolumeOff,
              contentDescription = "Stop sound",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Dismiss")
          }
        }
      }
    }

    // Main Sub-screen Content
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      when (selectedSubTab) {
        ClockSubTab.ALARM -> AlarmSubScreen(
          alarms = alarms,
          creamCardColor = creamCardColor,
          ivoryInnerColor = ivoryInnerColor,
          metrics = metrics,
          onToggleAlarm = { alarmId, isActive ->
            if (viewModel != null) {
              viewModel.toggleAlarmActive(alarmId, isActive)
            } else {
              val index = fallbackAlarms.indexOfFirst { it.id == alarmId }
              if (index >= 0) {
                fallbackAlarms[index] = fallbackAlarms[index].copy(isActive = isActive)
                localStorageManager.saveAlarms(fallbackAlarms, LocalStorageManager.GUEST_ACCOUNT_KEY)
              }
            }
          },
          onToggleAlarmDay = { alarmId, dayIndex ->
            if (viewModel != null) {
              viewModel.toggleAlarmDay(alarmId, dayIndex)
            } else {
              val index = fallbackAlarms.indexOfFirst { it.id == alarmId }
              if (index >= 0 && dayIndex in 0..6) {
                val current = fallbackAlarms[index]
                val updatedDays = if (current.activeDays.contains(dayIndex)) {
                  current.activeDays - dayIndex
                } else {
                  (current.activeDays + dayIndex).sorted()
                }
                fallbackAlarms[index] = current.copy(activeDays = updatedDays)
                localStorageManager.saveAlarms(fallbackAlarms, LocalStorageManager.GUEST_ACCOUNT_KEY)
              }
            }
          },
          onOpenAddAlarmDialog = {
            isAddAlarmDialogOpen = true
          },
          onTestRingNow = { alarm ->
            val formattedTime = "${alarm.hour}:${alarm.minute} ${alarm.amPm}"
            triggerAlarmRingtone("Alarm ringing: $formattedTime (${alarm.name})")
          }
        )

        ClockSubTab.TIMER -> TimerSubScreen(
          digitInput = timerDigitInput,
          totalDurationSeconds = timerTotalDurationSeconds,
          remainingSeconds = timerRemainingSeconds,
          isRunning = isTimerRunning,
          activePresetLabel = activePresetLabel,
          creamCardColor = creamCardColor,
          ivoryInnerColor = ivoryInnerColor,
          metrics = metrics,
          onAppendDigits = { digits ->
            if (!isTimerRunning && timerRemainingSeconds == 0L) {
              val combined = (timerDigitInput + digits).trimStart('0')
              timerDigitInput = combined.take(6)
            }
          },
          onBackspace = {
            if (!isTimerRunning && timerRemainingSeconds == 0L && timerDigitInput.isNotEmpty()) {
              timerDigitInput = timerDigitInput.dropLast(1)
            }
          },
          onClearInput = {
            isTimerRunning = false
            timerRemainingSeconds = 0L
            timerTotalDurationSeconds = 0L
            timerDigitInput = ""
            activePresetLabel = null
          },
          onStartOrPause = {
            if (isTimerRunning) {
              isTimerRunning = false
            } else {
              if (timerRemainingSeconds > 0L) {
                isTimerRunning = true
              } else {
                val parsedSeconds = parseKeypadDigitsToSeconds(timerDigitInput)
                if (parsedSeconds > 0L) {
                  timerTotalDurationSeconds = parsedSeconds
                  timerRemainingSeconds = parsedSeconds
                  activePresetLabel = "Custom Timer"
                  isTimerRunning = true
                }
              }
            }
          },
          onResetTimer = {
            isTimerRunning = false
            timerRemainingSeconds = 0L
            timerTotalDurationSeconds = 0L
            activePresetLabel = null
          },
          onSelectPreset = { preset ->
            stopActiveRingtone()
            timerDigitInput = ""
            timerTotalDurationSeconds = preset.durationSeconds
            timerRemainingSeconds = preset.durationSeconds
            activePresetLabel = preset.label
            isTimerRunning = true
          }
        )

        ClockSubTab.STOPWATCH -> StopwatchSubScreen(
          elapsedMillis = stopwatchElapsedMillis,
          isRunning = isStopwatchRunning,
          laps = stopwatchLaps,
          creamCardColor = creamCardColor,
          ivoryInnerColor = ivoryInnerColor,
          metrics = metrics,
          onToggleStartStop = {
            isStopwatchRunning = !isStopwatchRunning
          },
          onLapOrReset = {
            if (isStopwatchRunning) {
              val previousTotal = stopwatchLaps.firstOrNull()?.totalElapsedMillis ?: 0L
              val split = (stopwatchElapsedMillis - previousTotal).coerceAtLeast(0L)
              stopwatchLaps.add(
                0,
                StopwatchLapItem(
                  lapNumber = stopwatchLaps.size + 1,
                  lapSplitMillis = split,
                  totalElapsedMillis = stopwatchElapsedMillis
                )
              )
            } else {
              stopwatchElapsedMillis = 0L
              stopwatchLaps.clear()
            }
          }
        )
      }
    }
  }

  if (isAddAlarmDialogOpen) {
    AddAlarmDialog(
      creamCardColor = creamCardColor,
      ivoryInnerColor = ivoryInnerColor,
      metrics = metrics,
      onDismiss = { isAddAlarmDialogOpen = false },
      onSaveAlarm = { name, hour, minute, amPm, activeDays ->
        if (viewModel != null) {
          viewModel.addAlarm(
            name = name,
            hour = hour,
            minute = minute,
            amPm = amPm,
            activeDays = activeDays,
            isActive = true
          )
        } else {
          val nextId = (fallbackAlarms.maxOfOrNull { it.id } ?: 0) + 1
          fallbackAlarms.add(
            AlarmItem(
              id = nextId,
              name = name.trim().ifEmpty { "Alarm" },
              hour = hour,
              minute = minute,
              amPm = amPm,
              activeDays = activeDays.sorted(),
              isActive = true
            )
          )
          localStorageManager.saveAlarms(fallbackAlarms, LocalStorageManager.GUEST_ACCOUNT_KEY)
        }
        isAddAlarmDialogOpen = false
      }
    )
  }
}

private fun parseKeypadDigitsToSeconds(digits: String): Long {
  if (digits.isBlank()) return 0L
  val padded = digits.padStart(6, '0').takeLast(6)
  val hours = padded.substring(0, 2).toLongOrNull() ?: 0L
  val minutes = padded.substring(2, 4).toLongOrNull() ?: 0L
  val seconds = padded.substring(4, 6).toLongOrNull() ?: 0L
  return hours * 3600L + minutes * 60L + seconds
}

private fun formatKeypadDigitsPreview(digits: String): Triple<String, String, String> {
  val padded = digits.padStart(6, '0').takeLast(6)
  return Triple(
    padded.substring(0, 2),
    padded.substring(2, 4),
    padded.substring(4, 6)
  )
}

private fun formatDurationSecondsHms(totalSeconds: Long): String {
  val clamped = totalSeconds.coerceAtLeast(0L)
  val hours = clamped / 3600L
  val minutes = (clamped % 3600L) / 60L
  val seconds = clamped % 60L
  return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

private fun formatStopwatchMillis(elapsedMillis: Long): String {
  val clamped = elapsedMillis.coerceAtLeast(0L)
  val totalSeconds = clamped / 1000L
  val minutes = totalSeconds / 60L
  val seconds = totalSeconds % 60L
  val centiseconds = (clamped % 1000L) / 10L
  return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centiseconds)
}

// ============================================================================
// 1. ALARM SUB-SCREEN & ADD ALARM DIALOG
// ============================================================================

private val ALARM_WEEK_DAY_LABELS = listOf("S", "M", "T", "W", "T", "F", "S")

@Composable
private fun AlarmSubScreen(
  alarms: List<AlarmItem>,
  creamCardColor: Color,
  ivoryInnerColor: Color,
  metrics: ResponsiveMetrics,
  onToggleAlarm: (Int, Boolean) -> Unit,
  onToggleAlarmDay: (Int, Int) -> Unit,
  onOpenAddAlarmDialog: () -> Unit,
  onTestRingNow: (AlarmItem) -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .testTag("alarm_sub_screen")
  ) {
    if (alarms.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = metrics.scaledDp(24)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
        ) {
          Icon(
            imageVector = Icons.Filled.Alarm,
            contentDescription = "No alarms set",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            modifier = Modifier.size(metrics.scaledDp(48))
          )
          Text(
            text = "No Alarms Yet",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = metrics.scaledSp(18)
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Tap the + button below to create an alarm.",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = metrics.scaledSp(13)
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("alarm_list_lazy_column"),
      contentPadding = PaddingValues(
        start = metrics.scaledDp(16),
        end = metrics.scaledDp(16),
        top = metrics.scaledDp(14),
        bottom = metrics.scaledDp(108)
      ),
      verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
    ) {
      items(alarms, key = { it.id }) { alarm ->
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = creamCardColor),
          border = BorderStroke(
            width = 1.dp,
            color = if (alarm.isActive) {
              MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else {
              MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            }
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_card_${alarm.id}")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(metrics.scaledDp(18)),
            verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
          ) {
            // Top Row: Alarm name on the left and a "Test Ring" button with a small bell icon on the right
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = alarm.name,
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = metrics.scaledSp(14)
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )

              TextButton(
                onClick = { onTestRingNow(alarm) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("test_alarm_ring_${alarm.id}")
              ) {
                Icon(
                  imageVector = Icons.Filled.NotificationsActive,
                  contentDescription = "Test alarm sound",
                  modifier = Modifier.size(15.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Test Ring",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            // Middle Row: Massive digital font for the time, baseline-aligned AM/PM, and Switch toggle pushed to the far right
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "${alarm.hour}:${alarm.minute}",
                  style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = metrics.scaledSp(46),
                    lineHeight = metrics.scaledSp(50)
                  ),
                  color = if (alarm.isActive) {
                    MaterialTheme.colorScheme.onSurface
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                  },
                  modifier = Modifier.alignByBaseline()
                )
                Text(
                  text = alarm.amPm,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = metrics.scaledSp(18)
                  ),
                  color = if (alarm.isActive) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                  },
                  modifier = Modifier.alignByBaseline()
                )
              }

              Switch(
                checked = alarm.isActive,
                onCheckedChange = { checked -> onToggleAlarm(alarm.id, checked) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                  checkedTrackColor = MaterialTheme.colorScheme.primary,
                  uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                  uncheckedTrackColor = ivoryInnerColor
                ),
                modifier = Modifier.testTag("alarm_switch_${alarm.id}")
              )
            }

            // Bottom Row: 7 circular indicators for the days of the week (S, M, T, W, T, F, S)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              ALARM_WEEK_DAY_LABELS.forEachIndexed { dayIndex, dayLetter ->
                val isDayActive = alarm.activeDays.contains(dayIndex)
                Surface(
                  onClick = { onToggleAlarmDay(alarm.id, dayIndex) },
                  shape = CircleShape,
                  color = if (isDayActive) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                  },
                  border = BorderStroke(
                    width = 1.dp,
                    color = if (isDayActive) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                    }
                  ),
                  modifier = Modifier
                    .size(metrics.scaledDp(34))
                    .testTag("alarm_${alarm.id}_day_$dayIndex")
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = dayLetter,
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = metrics.scaledSp(12)
                      ),
                      color = if (isDayActive) {
                        Color.White
                      } else {
                        MaterialTheme.colorScheme.primary
                      }
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Large circular Google Clock-style FAB (+) at the bottom center
    FloatingActionButton(
      onClick = onOpenAddAlarmDialog,
      shape = CircleShape,
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = metrics.scaledDp(20))
        .size(metrics.scaledDp(68))
        .testTag("add_alarm_fab")
    ) {
      Icon(
        imageVector = Icons.Filled.Add,
        contentDescription = "Add alarm",
        modifier = Modifier.size(metrics.scaledDp(32))
      )
    }
  }
}

@Composable
private fun AddAlarmDialog(
  creamCardColor: Color,
  ivoryInnerColor: Color,
  metrics: ResponsiveMetrics,
  onDismiss: () -> Unit,
  onSaveAlarm: (name: String, hour: String, minute: String, amPm: String, activeDays: List<Int>) -> Unit
) {
  var alarmName by rememberSaveable { mutableStateOf("") }
  var hourInput by rememberSaveable { mutableStateOf("07") }
  var minuteInput by rememberSaveable { mutableStateOf("00") }
  var selectedAmPm by rememberSaveable { mutableStateOf("AM") }
  val selectedDays = remember { mutableStateListOf(1, 2, 3, 4, 5) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = creamCardColor,
    title = {
      Text(
        text = "Add Alarm",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          fontSize = metrics.scaledSp(20)
        ),
        color = MaterialTheme.colorScheme.onSurface
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
      ) {
        // Time Picker Row: Hour (01-12), Minute (00-59), and AM/PM Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = hourInput,
            onValueChange = { raw ->
              val digits = raw.filter { it.isDigit() }.take(2)
              hourInput = digits
            },
            label = { Text("Hour") },
            placeholder = { Text("07") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next
            ),
            textStyle = MaterialTheme.typography.titleLarge.copy(
              fontFamily = JetBrainsMonoFamily,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = ivoryInnerColor,
              unfocusedContainerColor = ivoryInnerColor,
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("add_alarm_hour_input")
          )

          Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontFamily = JetBrainsMonoFamily,
              fontWeight = FontWeight.ExtraBold
            ),
            color = MaterialTheme.colorScheme.onSurface
          )

          OutlinedTextField(
            value = minuteInput,
            onValueChange = { raw ->
              val digits = raw.filter { it.isDigit() }.take(2)
              minuteInput = digits
            },
            label = { Text("Minute") },
            placeholder = { Text("00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next
            ),
            textStyle = MaterialTheme.typography.titleLarge.copy(
              fontFamily = JetBrainsMonoFamily,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = ivoryInnerColor,
              unfocusedContainerColor = ivoryInnerColor,
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("add_alarm_minute_input")
          )

          // AM / PM Toggle Column
          Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            listOf("AM", "PM").forEach { period ->
              val isSelected = selectedAmPm == period
              Surface(
                onClick = { selectedAmPm = period },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  ivoryInnerColor
                },
                border = BorderStroke(
                  width = 1.dp,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.outlineVariant
                  }
                ),
                modifier = Modifier
                  .width(metrics.scaledDp(52))
                  .height(metrics.scaledDp(32))
                  .testTag("add_alarm_ampm_$period")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = period,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold
                    ),
                    color = if (isSelected) {
                      Color.White
                    } else {
                      MaterialTheme.colorScheme.primary
                    }
                  )
                }
              }
            }
          }
        }

        // Alarm Name OutlinedTextField (defaults to "Alarm" if left blank)
        OutlinedTextField(
          value = alarmName,
          onValueChange = { alarmName = it },
          label = { Text("Alarm Name") },
          placeholder = { Text("Alarm") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
          ),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ivoryInnerColor,
            unfocusedContainerColor = ivoryInnerColor,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_alarm_name_input")
        )

        // Repeat Days Section: 7 selectable day circles (S, M, T, W, T, F, S)
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Repeat Days",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = metrics.scaledSp(13)
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            ALARM_WEEK_DAY_LABELS.forEachIndexed { dayIndex, dayLetter ->
              val isSelected = selectedDays.contains(dayIndex)
              Surface(
                onClick = {
                  if (isSelected) {
                    selectedDays.remove(dayIndex)
                  } else {
                    selectedDays.add(dayIndex)
                  }
                },
                shape = CircleShape,
                color = if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                },
                border = BorderStroke(
                  width = 1.dp,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                  }
                ),
                modifier = Modifier
                  .size(metrics.scaledDp(36))
                  .testTag("add_alarm_day_circle_$dayIndex")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = dayLetter,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = metrics.scaledSp(12)
                    ),
                    color = if (isSelected) {
                      Color.White
                    } else {
                      MaterialTheme.colorScheme.primary
                    }
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsedHour = (hourInput.trim().toIntOrNull() ?: 7).coerceIn(1, 12)
          val parsedMinute = (minuteInput.trim().toIntOrNull() ?: 0).coerceIn(0, 59)
          val formattedHour = String.format(Locale.US, "%02d", parsedHour)
          val formattedMinute = String.format(Locale.US, "%02d", parsedMinute)
          val resolvedName = alarmName.trim().ifEmpty { "Alarm" }
          onSaveAlarm(
            resolvedName,
            formattedHour,
            formattedMinute,
            selectedAmPm,
            selectedDays.toList().sorted()
          )
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.testTag("save_alarm_button")
      ) {
        Text(
          text = "Save",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_alarm_button")
      ) {
        Text(
          text = "Cancel",
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    modifier = Modifier.testTag("add_alarm_dialog")
  )
}

// ============================================================================
// 2. TIMER SUB-SCREEN (WITH CE FOCUS PRESETS)
// ============================================================================

@Composable
private fun TimerSubScreen(
  digitInput: String,
  totalDurationSeconds: Long,
  remainingSeconds: Long,
  isRunning: Boolean,
  activePresetLabel: String?,
  creamCardColor: Color,
  ivoryInnerColor: Color,
  metrics: ResponsiveMetrics,
  onAppendDigits: (String) -> Unit,
  onBackspace: () -> Unit,
  onClearInput: () -> Unit,
  onStartOrPause: () -> Unit,
  onResetTimer: () -> Unit,
  onSelectPreset: (CeFocusPreset) -> Unit
) {
  val isCountdownActive = remainingSeconds > 0L || totalDurationSeconds > 0L
  val (hhPreview, mmPreview, ssPreview) = remember(digitInput) {
    formatKeypadDigitsPreview(digitInput)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(
        horizontal = metrics.scaledDp(16),
        vertical = metrics.scaledDp(14)
      )
      .testTag("timer_sub_screen"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
  ) {
    // Massive Digital Timer Display Card
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = creamCardColor),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = metrics.scaledDp(18),
            vertical = metrics.scaledDp(20)
          ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(10))
      ) {
        if (activePresetLabel != null) {
          Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
          ) {
            Text(
              text = activePresetLabel,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = metrics.scaledSp(12)
              ),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
          }
        }

        if (isCountdownActive) {
          Text(
            text = formatDurationSecondsHms(remainingSeconds),
            style = MaterialTheme.typography.displayLarge.copy(
              fontFamily = JetBrainsMonoFamily,
              fontWeight = FontWeight.ExtraBold,
              fontSize = metrics.scaledSp(50),
              lineHeight = metrics.scaledSp(54)
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("timer_countdown_display")
          )

          val progress = if (totalDurationSeconds > 0L) {
            (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
          } else {
            0f
          }
          LinearProgressIndicator(
            progress = { progress },
            color = MaterialTheme.colorScheme.primary,
            trackColor = WarmCreamTrackColor,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
          )
        } else {
          // Keypad Digital Entry Display (00h 00m 00s)
          Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("timer_keypad_display")
          ) {
            TimerUnitSegment(value = hhPreview, unit = "h", isHighlighted = digitInput.length >= 5, metrics = metrics)
            Spacer(modifier = Modifier.width(metrics.scaledDp(8)))
            TimerUnitSegment(value = mmPreview, unit = "m", isHighlighted = digitInput.length >= 3, metrics = metrics)
            Spacer(modifier = Modifier.width(metrics.scaledDp(8)))
            TimerUnitSegment(value = ssPreview, unit = "s", isHighlighted = digitInput.isNotEmpty(), metrics = metrics)

            IconButton(
              onClick = onBackspace,
              modifier = Modifier
                .padding(start = 4.dp)
                .size(48.dp)
                .testTag("timer_backspace_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete digit",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // CE Focus Presets Section
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = creamCardColor),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("ce_focus_presets_section")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(metrics.scaledDp(14)),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(10))
      ) {
        Text(
          text = "CE Focus Presets",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = metrics.scaledSp(14)
          ),
          color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(8)),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(CE_FOCUS_PRESETS, key = { it.label }) { preset ->
            val isSelected = activePresetLabel == preset.label && isCountdownActive
            Surface(
              shape = RoundedCornerShape(50),
              color = if (isSelected) {
                MaterialTheme.colorScheme.primary
              } else {
                ivoryInnerColor
              },
              border = BorderStroke(
                width = 1.dp,
                color = if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                }
              ),
              modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onSelectPreset(preset) }
                .testTag(preset.testTag)
            ) {
              Row(
                modifier = Modifier.padding(
                  horizontal = metrics.scaledDp(14),
                  vertical = metrics.scaledDp(10)
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Timer,
                  contentDescription = preset.label,
                  modifier = Modifier.size(16.dp),
                  tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                  } else {
                    MaterialTheme.colorScheme.primary
                  }
                )
                Text(
                  text = preset.label,
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = metrics.scaledSp(13)
                  ),
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
      }
    }

    // Standard Google Clock Number Pad when entering a custom timer
    if (!isCountdownActive) {
      val keypadRows = remember {
        listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf("00", "0", "C")
        )
      }

      Column(
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("timer_number_keypad")
      ) {
        keypadRows.forEach { rowDigits ->
          Row(
            horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(14)),
            verticalAlignment = Alignment.CenterVertically
          ) {
            rowDigits.forEach { keyLabel ->
              Surface(
                shape = CircleShape,
                color = creamCardColor,
                border = BorderStroke(
                  1.dp,
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                  .size(metrics.scaledDp(62))
                  .clip(CircleShape)
                  .clickable {
                    if (keyLabel == "C") {
                      onClearInput()
                    } else {
                      onAppendDigits(keyLabel)
                    }
                  }
                  .testTag("timer_key_$keyLabel")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = keyLabel,
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontFamily = JetBrainsMonoFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = metrics.scaledSp(22)
                    ),
                    color = if (keyLabel == "C") {
                      MaterialTheme.colorScheme.error
                    } else {
                      MaterialTheme.colorScheme.onSurface
                    }
                  )
                }
              }
            }
          }
        }
      }
    }

    // Circular Play/Pause and Reset FAB Controls
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = metrics.scaledDp(4), bottom = metrics.scaledDp(16)),
      horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(24), Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isCountdownActive || digitInput.isNotEmpty()) {
        Surface(
          shape = CircleShape,
          color = creamCardColor,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier
            .size(metrics.scaledDp(58))
            .clip(CircleShape)
            .clickable { onResetTimer(); onClearInput() }
            .testTag("timer_reset_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Replay,
              contentDescription = "Reset timer",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(26.dp)
            )
          }
        }
      }

      FloatingActionButton(
        onClick = onStartOrPause,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
        modifier = Modifier
          .size(metrics.scaledDp(72))
          .testTag("timer_play_pause_fab")
      ) {
        Icon(
          imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
          contentDescription = if (isRunning) "Pause timer" else "Start timer",
          modifier = Modifier.size(metrics.scaledDp(34))
        )
      }
    }
  }
}

@Composable
private fun TimerUnitSegment(
  value: String,
  unit: String,
  isHighlighted: Boolean,
  metrics: ResponsiveMetrics
) {
  Row(verticalAlignment = Alignment.Bottom) {
    Text(
      text = value,
      style = MaterialTheme.typography.displayMedium.copy(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = metrics.scaledSp(40)
      ),
      color = if (isHighlighted) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
      }
    )
    Text(
      text = unit,
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = metrics.scaledSp(18)
      ),
      color = if (isHighlighted) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
      },
      modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
    )
  }
}

// ============================================================================
// 3. STOPWATCH SUB-SCREEN
// ============================================================================

@Composable
private fun StopwatchSubScreen(
  elapsedMillis: Long,
  isRunning: Boolean,
  laps: List<StopwatchLapItem>,
  creamCardColor: Color,
  ivoryInnerColor: Color,
  metrics: ResponsiveMetrics,
  onToggleStartStop: () -> Unit,
  onLapOrReset: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(
        horizontal = metrics.scaledDp(16),
        vertical = metrics.scaledDp(14)
      )
      .testTag("stopwatch_sub_screen"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Massive circular-inspired digital stopwatch header
    Card(
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(containerColor = creamCardColor),
      border = BorderStroke(
        width = 1.5.dp,
        color = if (isRunning) {
          MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        }
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = metrics.scaledDp(6))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = metrics.scaledDp(20),
            vertical = metrics.scaledDp(28)
          ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(6))
      ) {
        Text(
          text = if (isRunning) "RUNNING" else if (elapsedMillis > 0L) "PAUSED" else "READY",
          style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
          ),
          color = if (isRunning) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          }
        )

        Text(
          text = formatStopwatchMillis(elapsedMillis),
          style = MaterialTheme.typography.displayLarge.copy(
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = metrics.scaledSp(54),
            lineHeight = metrics.scaledSp(58)
          ),
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("stopwatch_digital_counter")
        )
      }
    }

    Spacer(modifier = Modifier.height(metrics.scaledDp(16)))

    // Two circular buttons: Lap/Reset and Start/Stop
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(24), Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Secondary circular button: Lap (when running) or Reset (when stopped and > 0)
      val canLapOrReset = isRunning || elapsedMillis > 0L
      Surface(
        shape = CircleShape,
        color = if (canLapOrReset) creamCardColor else creamCardColor.copy(alpha = 0.5f),
        border = BorderStroke(
          width = 1.5.dp,
          color = if (canLapOrReset) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
          } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
          }
        ),
        modifier = Modifier
          .size(metrics.scaledDp(62))
          .clip(CircleShape)
          .clickable(enabled = canLapOrReset) { onLapOrReset() }
          .testTag("stopwatch_lap_reset_button")
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = if (isRunning) Icons.Filled.Flag else Icons.Filled.Replay,
            contentDescription = if (isRunning) "Record lap" else "Reset stopwatch",
            tint = if (canLapOrReset) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            },
            modifier = Modifier.size(22.dp)
          )
          Text(
            text = if (isRunning) "Lap" else "Reset",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = metrics.scaledSp(10)
            ),
            color = if (canLapOrReset) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            }
          )
        }
      }

      // Primary circular FAB: Start / Stop
      FloatingActionButton(
        onClick = onToggleStartStop,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
        modifier = Modifier
          .size(metrics.scaledDp(74))
          .testTag("stopwatch_start_stop_button")
      ) {
        Icon(
          imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
          contentDescription = if (isRunning) "Stop stopwatch" else "Start stopwatch",
          modifier = Modifier.size(metrics.scaledDp(36))
        )
      }
    }

    Spacer(modifier = Modifier.height(metrics.scaledDp(16)))

    // Scrollable list of Lap Times below the buttons
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = creamCardColor),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(metrics.scaledDp(14))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = metrics.scaledDp(8)),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Lap Splits",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = metrics.scaledSp(14)
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${laps.size} recorded",
            style = MaterialTheme.typography.labelMedium.copy(
              fontFamily = JetBrainsMonoFamily
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        HorizontalDivider(
          thickness = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )

        if (laps.isEmpty()) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Start the stopwatch and tap Lap to record split intervals.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(top = metrics.scaledDp(8))
              .testTag("stopwatch_laps_list"),
            verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
          ) {
            items(laps, key = { it.lapNumber }) { lap ->
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = ivoryInnerColor,
                border = BorderStroke(
                  1.dp,
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("stopwatch_lap_row_${lap.lapNumber}")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                      horizontal = metrics.scaledDp(14),
                      vertical = metrics.scaledDp(10)
                    ),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = String.format(Locale.US, "#%02d", lap.lapNumber),
                    style = MaterialTheme.typography.labelLarge.copy(
                      fontFamily = JetBrainsMonoFamily,
                      fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                  )
                  Text(
                    text = "+${formatStopwatchMillis(lap.lapSplitMillis)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontFamily = JetBrainsMonoFamily,
                      fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = formatStopwatchMillis(lap.totalElapsedMillis),
                    style = MaterialTheme.typography.bodyLarge.copy(
                      fontFamily = JetBrainsMonoFamily,
                      fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
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

@Preview(showBackground = true)
@Composable
fun CivilEngineeringProductivityAppPreview() {
  CivilEngineeringProductivityApp()
}

package com.example

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

/**
 * Shared Data Bridge between Sibil CE's in-app state (Tasks & Class Schedule)
 * and the native Android Home Screen Widgets (`SibilTasksWidgetProvider` & `SibilScheduleWidgetProvider`).
 */
object SibilWidgetSync {
  const val PREFS_NAME = "sibil_widget_prefs"
  const val KEY_TASKS_JSON = "tasks_json"
  const val KEY_SCHEDULE_JSON = "schedule_json"

  const val EXTRA_WIDGET_TARGET_TAB = "sibil_widget_target_tab"
  const val EXTRA_WIDGET_OPEN_ADD_TASK = "sibil_widget_open_add_task"
  const val TARGET_TAB_TASKS = "TASKS"
  const val TARGET_TAB_SCHEDULE = "SCHEDULE"

  fun saveAndRefreshWidgets(
    context: Context,
    tasks: List<TaskItem>,
    classes: List<ScheduleItem>
  ) {
    try {
      val appContext = context.applicationContext
      val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

      // 1. Serialize active (uncompleted) tasks sorted by due date, then priority
      val activeSortedTasks = tasks
        .filter { !it.isCompleted && it.title.isNotBlank() }
        .sortedWith(
          compareBy<TaskItem> { it.resolveLocalDateOrNull() ?: LocalDate.MAX }
            .thenByDescending { it.priority }
            .thenBy { it.createdAtMillis }
        )

      val tasksJsonArray = JSONArray()
      for (task in activeSortedTasks) {
        val smartDue = formatIsoDateSmartDisplay(task.dueDateIso, task.dueDate).ifBlank { "Upcoming" }
        val obj = JSONObject().apply {
          put("id", task.id)
          put("title", task.title.trim())
          put("dueDateText", smartDue)
          put("priority", task.priority)
        }
        tasksJsonArray.put(obj)
      }

      // 2. Serialize all scheduled classes
      val scheduleJsonArray = JSONArray()
      for (cls in classes) {
        if (cls.subjectName.isBlank()) continue
        val (fallbackStart, fallbackEnd) = parseTimeRangeTokens(cls.timeRange)
        val resolvedStart = cls.startTime.trim().ifEmpty { fallbackStart }
        val resolvedEnd = cls.endTime.trim().ifEmpty { fallbackEnd }
        val daysArray = JSONArray()
        cls.days.forEach { d -> daysArray.put(d.trim()) }
        val obj = JSONObject().apply {
          put("id", cls.id)
          put("subject", cls.subjectName.trim())
          put("day", cls.days.joinToString(","))
          put("days", daysArray)
          put("startTime", resolvedStart)
          put("endTime", resolvedEnd)
          put("room", cls.room.trim().ifEmpty { "TBA" })
        }
        scheduleJsonArray.put(obj)
      }

      prefs.edit()
        .putString(KEY_TASKS_JSON, tasksJsonArray.toString())
        .putString(KEY_SCHEDULE_JSON, scheduleJsonArray.toString())
        .apply()

      refreshAllWidgets(appContext)
    } catch (_: Throwable) {
    }
  }

  fun refreshAllWidgets(context: Context) {
    try {
      val appContext = context.applicationContext
      val appWidgetManager = AppWidgetManager.getInstance(appContext)

      val tasksComponent = ComponentName(appContext, SibilTasksWidgetProvider::class.java)
      val taskIds = appWidgetManager.getAppWidgetIds(tasksComponent)
      if (taskIds.isNotEmpty()) {
        SibilTasksWidgetProvider.updateWidgets(appContext, appWidgetManager, taskIds)
      }
      appContext.sendBroadcast(
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
          component = tasksComponent
          setPackage(appContext.packageName)
          putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, taskIds)
        }
      )

      val scheduleComponent = ComponentName(appContext, SibilScheduleWidgetProvider::class.java)
      val scheduleIds = appWidgetManager.getAppWidgetIds(scheduleComponent)
      if (scheduleIds.isNotEmpty()) {
        SibilScheduleWidgetProvider.updateWidgets(appContext, appWidgetManager, scheduleIds)
      }
      appContext.sendBroadcast(
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
          component = scheduleComponent
          setPackage(appContext.packageName)
          putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, scheduleIds)
        }
      )
    } catch (_: Throwable) {
    }
  }

  fun requestPinWidget(
    context: Context,
    providerClass: Class<out AppWidgetProvider>
  ) {
    try {
      val appWidgetManager = AppWidgetManager.getInstance(context)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
        val providerComponent = ComponentName(context, providerClass)
        val pinned = appWidgetManager.requestPinAppWidget(providerComponent, null, null)
        if (!pinned) {
          Toast.makeText(
            context,
            "Long-press Home Screen → Widgets → Sibil CE",
            Toast.LENGTH_LONG
          ).show()
        }
      } else {
        Toast.makeText(
          context,
          "Long-press Home Screen → Widgets → Sibil CE",
          Toast.LENGTH_LONG
        ).show()
      }
    } catch (_: Throwable) {
      Toast.makeText(
        context,
        "Long-press Home Screen → Widgets → Sibil CE",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  internal fun createLaunchPendingIntent(
    context: Context,
    targetTab: String,
    openAddTask: Boolean = false,
    requestCode: Int
  ): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
      putExtra(EXTRA_WIDGET_TARGET_TAB, targetTab)
      putExtra(EXTRA_WIDGET_OPEN_ADD_TASK, openAddTask)
    }
    return PendingIntent.getActivity(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }

  private fun parseTimeRangeTokens(timeRange: String): Pair<String, String> {
    val parts = timeRange.split("-", "–", "to", ignoreCase = true).map { it.trim() }.filter { it.isNotEmpty() }
    return if (parts.size >= 2) {
      parts[0] to parts[1]
    } else {
      "7:00 AM" to "9:00 AM"
    }
  }
}

/**
 * WIDGET 1 — SIBIL CE TASKS WIDGET (`SibilTasksWidgetProvider`)
 * Displays up to 5 upcoming uncompleted coursework/board exam tasks with live count & "+ Add" shortcut.
 */
class SibilTasksWidgetProvider : AppWidgetProvider() {

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, SibilTasksWidgetProvider::class.java))
    if (ids.isNotEmpty()) {
      updateWidgets(context, appWidgetManager, ids)
    }
  }

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    updateWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle
  ) {
    super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (
      action == Intent.ACTION_BOOT_COMPLETED ||
      action == Intent.ACTION_MY_PACKAGE_REPLACED ||
      action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
    ) {
      val appWidgetManager = AppWidgetManager.getInstance(context)
      val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, SibilTasksWidgetProvider::class.java))
      if (ids.isNotEmpty()) {
        updateWidgets(context, appWidgetManager, ids)
      }
    }
  }

  companion object {
    private val ROW_IDS = intArrayOf(
      R.id.widget_task_row_1,
      R.id.widget_task_row_2,
      R.id.widget_task_row_3,
      R.id.widget_task_row_4,
      R.id.widget_task_row_5
    )
    private val TITLE_IDS = intArrayOf(
      R.id.widget_task_title_1,
      R.id.widget_task_title_2,
      R.id.widget_task_title_3,
      R.id.widget_task_title_4,
      R.id.widget_task_title_5
    )
    private val DUE_IDS = intArrayOf(
      R.id.widget_task_due_1,
      R.id.widget_task_due_2,
      R.id.widget_task_due_3,
      R.id.widget_task_due_4,
      R.id.widget_task_due_5
    )

    fun updateWidgets(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetIds: IntArray
    ) {
      val prefs = context.getSharedPreferences(SibilWidgetSync.PREFS_NAME, Context.MODE_PRIVATE)
      val rawJson = prefs.getString(SibilWidgetSync.KEY_TASKS_JSON, "[]").orEmpty()
      val parsedTasks = parseTasksFromJson(rawJson)

      val openTasksPendingIntent = SibilWidgetSync.createLaunchPendingIntent(
        context = context,
        targetTab = SibilWidgetSync.TARGET_TAB_TASKS,
        openAddTask = false,
        requestCode = 7101
      )
      val addTaskPendingIntent = SibilWidgetSync.createLaunchPendingIntent(
        context = context,
        targetTab = SibilWidgetSync.TARGET_TAB_TASKS,
        openAddTask = true,
        requestCode = 7102
      )

      for (widgetId in appWidgetIds) {
        val views = RemoteViews(context.packageName, R.layout.widget_sibil_tasks)
        views.setOnClickPendingIntent(R.id.widget_tasks_root, openTasksPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_tasks_add_button, addTaskPendingIntent)
        views.setTextViewText(R.id.widget_tasks_count_badge, "${parsedTasks.size} Pending")

        if (parsedTasks.isEmpty()) {
          views.setViewVisibility(R.id.widget_tasks_empty_text, View.VISIBLE)
          views.setOnClickPendingIntent(R.id.widget_tasks_empty_text, openTasksPendingIntent)
          for (i in ROW_IDS.indices) {
            views.setViewVisibility(ROW_IDS[i], View.GONE)
          }
        } else {
          views.setViewVisibility(R.id.widget_tasks_empty_text, View.GONE)
          val visibleTasks = parsedTasks.take(ROW_IDS.size)
          for (i in ROW_IDS.indices) {
            if (i < visibleTasks.size) {
              val item = visibleTasks[i]
              views.setViewVisibility(ROW_IDS[i], View.VISIBLE)
              views.setTextViewText(TITLE_IDS[i], item.title)
              views.setTextViewText(DUE_IDS[i], item.dueDateText.ifBlank { "Upcoming" })
              views.setOnClickPendingIntent(ROW_IDS[i], openTasksPendingIntent)
            } else {
              views.setViewVisibility(ROW_IDS[i], View.GONE)
            }
          }
        }

        appWidgetManager.updateAppWidget(widgetId, views)
      }
    }

    private data class WidgetTaskRow(
      val id: Int,
      val title: String,
      val dueDateText: String,
      val priority: Int
    )

    private fun parseTasksFromJson(rawJson: String): List<WidgetTaskRow> {
      return try {
        val arr = JSONArray(rawJson)
        buildList {
          for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val title = obj.optString("title", "").trim()
            if (title.isEmpty()) continue
            add(
              WidgetTaskRow(
                id = obj.optInt("id", i),
                title = title,
                dueDateText = obj.optString("dueDateText", "Upcoming").trim().ifEmpty { "Upcoming" },
                priority = obj.optInt("priority", 0)
              )
            )
          }
        }
      } catch (_: Throwable) {
        emptyList()
      }
    }
  }
}

/**
 * WIDGET 2 — SIBIL CE LIVE CLASS SCHEDULE WIDGET (`SibilScheduleWidgetProvider`)
 * Real-time class highlighting engine comparing `LocalTime.now()` against each class's `startTime` & `endTime`.
 */
class SibilScheduleWidgetProvider : AppWidgetProvider() {

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, SibilScheduleWidgetProvider::class.java))
    if (ids.isNotEmpty()) {
      updateWidgets(context, appWidgetManager, ids)
    }
  }

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    updateWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle
  ) {
    super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (
      action == Intent.ACTION_BOOT_COMPLETED ||
      action == Intent.ACTION_MY_PACKAGE_REPLACED ||
      action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
    ) {
      val appWidgetManager = AppWidgetManager.getInstance(context)
      val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, SibilScheduleWidgetProvider::class.java))
      if (ids.isNotEmpty()) {
        updateWidgets(context, appWidgetManager, ids)
      }
    }
  }

  companion object {
    private val ROW_IDS = intArrayOf(
      R.id.widget_schedule_row_1,
      R.id.widget_schedule_row_2,
      R.id.widget_schedule_row_3,
      R.id.widget_schedule_row_4
    )
    private val SUBJECT_IDS = intArrayOf(
      R.id.widget_schedule_subject_1,
      R.id.widget_schedule_subject_2,
      R.id.widget_schedule_subject_3,
      R.id.widget_schedule_subject_4
    )
    private val TIME_ROOM_IDS = intArrayOf(
      R.id.widget_schedule_time_room_1,
      R.id.widget_schedule_time_room_2,
      R.id.widget_schedule_time_room_3,
      R.id.widget_schedule_time_room_4
    )
    private val STATUS_IDS = intArrayOf(
      R.id.widget_schedule_status_1,
      R.id.widget_schedule_status_2,
      R.id.widget_schedule_status_3,
      R.id.widget_schedule_status_4
    )

    private enum class LiveClassStatus {
      LIVE_NOW,
      UP_NEXT,
      UPCOMING,
      DONE
    }

    private data class EvaluatedClassRow(
      val id: Int,
      val subject: String,
      val startTimeText: String,
      val endTimeText: String,
      val room: String,
      val startLocalTime: LocalTime,
      val endLocalTime: LocalTime,
      val status: LiveClassStatus
    )

    fun updateWidgets(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetIds: IntArray
    ) {
      val prefs = context.getSharedPreferences(SibilWidgetSync.PREFS_NAME, Context.MODE_PRIVATE)
      val rawJson = prefs.getString(SibilWidgetSync.KEY_SCHEDULE_JSON, "[]").orEmpty()

      val today = LocalDate.now()
      val nowTime = LocalTime.now()
      val dayFullName = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
      val dateFormatted = today.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
      val headerTitleText = "$dayFullName Schedule • $dateFormatted"

      val todayClasses = evaluateTodayClasses(rawJson, today.dayOfWeek, nowTime)

      val openSchedulePendingIntent = SibilWidgetSync.createLaunchPendingIntent(
        context = context,
        targetTab = SibilWidgetSync.TARGET_TAB_SCHEDULE,
        openAddTask = false,
        requestCode = 7201
      )

      val liveCount = todayClasses.count { it.status == LiveClassStatus.LIVE_NOW }
      val summaryText = when {
        liveCount > 0 -> "● 1 Live Now"
        todayClasses.isEmpty() -> "Rest Day"
        todayClasses.size == 1 -> "1 Class"
        else -> "${todayClasses.size} Classes"
      }

      for (widgetId in appWidgetIds) {
        val views = RemoteViews(context.packageName, R.layout.widget_sibil_schedule)
        views.setOnClickPendingIntent(R.id.widget_schedule_root, openSchedulePendingIntent)
        views.setTextViewText(R.id.widget_schedule_header_title, headerTitleText)
        views.setTextViewText(R.id.widget_schedule_summary_badge, summaryText)

        if (todayClasses.isEmpty()) {
          views.setViewVisibility(R.id.widget_schedule_empty_text, View.VISIBLE)
          views.setOnClickPendingIntent(R.id.widget_schedule_empty_text, openSchedulePendingIntent)
          for (i in ROW_IDS.indices) {
            views.setViewVisibility(ROW_IDS[i], View.GONE)
          }
        } else {
          views.setViewVisibility(R.id.widget_schedule_empty_text, View.GONE)
          // Prioritize LIVE_NOW and UP_NEXT/UPCOMING classes first so active classes are always visible
          val prioritizedClasses = todayClasses.sortedWith(
            compareBy<EvaluatedClassRow> {
              when (it.status) {
                LiveClassStatus.LIVE_NOW -> 0
                LiveClassStatus.UP_NEXT -> 1
                LiveClassStatus.UPCOMING -> 2
                LiveClassStatus.DONE -> 3
              }
            }.thenBy { it.startLocalTime }
          ).take(ROW_IDS.size)

          for (i in ROW_IDS.indices) {
            if (i < prioritizedClasses.size) {
              val cls = prioritizedClasses[i]
              views.setViewVisibility(ROW_IDS[i], View.VISIBLE)
              views.setOnClickPendingIntent(ROW_IDS[i], openSchedulePendingIntent)

              val timeRoomText = "${cls.startTimeText} - ${cls.endTimeText} • ${cls.room}"
              views.setTextViewText(SUBJECT_IDS[i], cls.subject)
              views.setTextViewText(TIME_ROOM_IDS[i], timeRoomText)

              when (cls.status) {
                LiveClassStatus.LIVE_NOW -> {
                  views.setInt(ROW_IDS[i], "setBackgroundResource", R.drawable.widget_card_live_cyan)
                  views.setTextColor(SUBJECT_IDS[i], Color.parseColor("#FFFFFF"))
                  views.setTextColor(TIME_ROOM_IDS[i], Color.parseColor("#E0F2FE"))
                  views.setTextViewText(STATUS_IDS[i], "● LIVE NOW")
                  views.setInt(STATUS_IDS[i], "setBackgroundResource", R.drawable.widget_pill_live_white)
                  views.setTextColor(STATUS_IDS[i], Color.parseColor("#FFFFFF"))
                }
                LiveClassStatus.UP_NEXT, LiveClassStatus.UPCOMING -> {
                  views.setInt(ROW_IDS[i], "setBackgroundResource", R.drawable.widget_card_slate)
                  views.setTextColor(SUBJECT_IDS[i], Color.parseColor("#FFFFFF"))
                  views.setTextColor(TIME_ROOM_IDS[i], Color.parseColor("#38BDF8"))
                  views.setTextViewText(
                    STATUS_IDS[i],
                    if (cls.status == LiveClassStatus.UP_NEXT) "UP NEXT" else "LATER"
                  )
                  views.setInt(STATUS_IDS[i], "setBackgroundResource", R.drawable.widget_pill_cyan)
                  views.setTextColor(STATUS_IDS[i], Color.parseColor("#38BDF8"))
                }
                LiveClassStatus.DONE -> {
                  views.setInt(ROW_IDS[i], "setBackgroundResource", R.drawable.widget_card_done_dim)
                  views.setTextColor(SUBJECT_IDS[i], Color.parseColor("#64748B"))
                  views.setTextColor(TIME_ROOM_IDS[i], Color.parseColor("#64748B"))
                  views.setTextViewText(STATUS_IDS[i], "DONE")
                  views.setInt(STATUS_IDS[i], "setBackgroundResource", R.drawable.widget_pill_done)
                  views.setTextColor(STATUS_IDS[i], Color.parseColor("#64748B"))
                }
              }
            } else {
              views.setViewVisibility(ROW_IDS[i], View.GONE)
            }
          }
        }

        appWidgetManager.updateAppWidget(widgetId, views)
      }
    }

    private fun evaluateTodayClasses(
      rawJson: String,
      dayOfWeek: DayOfWeek,
      now: LocalTime
    ): List<EvaluatedClassRow> {
      return try {
        val arr = JSONArray(rawJson)
        val rawMatching = mutableListOf<Triple<JSONObject, LocalTime, LocalTime>>()

        for (i in 0 until arr.length()) {
          val obj = arr.optJSONObject(i) ?: continue
          val subject = obj.optString("subject", "").trim()
          if (subject.isEmpty()) continue

          val daysTokens = mutableListOf<String>()
          val daysArr = obj.optJSONArray("days")
          if (daysArr != null) {
            for (dIdx in 0 until daysArr.length()) {
              val token = daysArr.optString(dIdx, "").trim()
              if (token.isNotEmpty()) daysTokens.add(token)
            }
          }
          val dayStr = obj.optString("day", "").trim()
          if (dayStr.isNotEmpty()) {
            dayStr.split(",", ";", "/").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
              daysTokens.add(it)
            }
          }

          if (!matchesDayOfWeek(daysTokens, dayOfWeek)) continue

          val startText = obj.optString("startTime", "7:00 AM").trim().ifEmpty { "7:00 AM" }
          val endText = obj.optString("endTime", "9:00 AM").trim().ifEmpty { "9:00 AM" }
          val startLocal = parseFlexibleLocalTime(startText) ?: LocalTime.of(7, 0)
          val endLocal = parseFlexibleLocalTime(endText) ?: startLocal.plusHours(2)
          rawMatching.add(Triple(obj, startLocal, endLocal))
        }

        val sortedByStart = rawMatching.sortedBy { it.second }
        var hasAssignedUpNext = false

        sortedByStart.mapIndexed { idx, (obj, startLocal, endLocal) ->
          val status = when {
            !now.isBefore(startLocal) && !now.isAfter(endLocal) -> LiveClassStatus.LIVE_NOW
            now.isBefore(startLocal) -> {
              if (!hasAssignedUpNext) {
                hasAssignedUpNext = true
                LiveClassStatus.UP_NEXT
              } else {
                LiveClassStatus.UPCOMING
              }
            }
            else -> LiveClassStatus.DONE
          }
          EvaluatedClassRow(
            id = obj.optInt("id", idx),
            subject = obj.optString("subject", "").trim(),
            startTimeText = obj.optString("startTime", "7:00 AM").trim(),
            endTimeText = obj.optString("endTime", "9:00 AM").trim(),
            room = obj.optString("room", "TBA").trim().ifEmpty { "TBA" },
            startLocalTime = startLocal,
            endLocalTime = endLocal,
            status = status
          )
        }
      } catch (_: Throwable) {
        emptyList()
      }
    }

    private fun matchesDayOfWeek(tokens: List<String>, dayOfWeek: DayOfWeek): Boolean {
      if (tokens.isEmpty()) return false
      val acceptedTokens = when (dayOfWeek) {
        DayOfWeek.MONDAY -> setOf("m", "mon", "monday")
        DayOfWeek.TUESDAY -> setOf("t", "tu", "tue", "tues", "tuesday")
        DayOfWeek.WEDNESDAY -> setOf("w", "wed", "wednesday")
        DayOfWeek.THURSDAY -> setOf("th", "thu", "thur", "thurs", "thursday")
        DayOfWeek.FRIDAY -> setOf("f", "fri", "friday")
        DayOfWeek.SATURDAY -> setOf("s", "sat", "saturday")
        DayOfWeek.SUNDAY -> setOf("su", "sun", "sunday")
      }
      return tokens.any { raw ->
        acceptedTokens.contains(raw.trim().lowercase(Locale.US))
      }
    }

    private fun parseFlexibleLocalTime(rawTime: String): LocalTime? {
      val clean = rawTime.trim().uppercase(Locale.US).replace(".", "")
      if (clean.isEmpty()) return null
      val regex = Regex("""^(\d{1,2})(?::(\d{2}))?\s*(AM|PM)?$""")
      val match = regex.find(clean) ?: return null
      val hourRaw = match.groupValues[1].toIntOrNull() ?: return null
      val minute = match.groupValues[2].toIntOrNull()?.coerceIn(0, 59) ?: 0
      val amPm = match.groupValues[3]

      val hour24 = when (amPm) {
        "AM" -> if (hourRaw == 12) 0 else hourRaw.coerceIn(0, 11)
        "PM" -> if (hourRaw == 12) 12 else (hourRaw.coerceIn(1, 11) + 12)
        else -> hourRaw.coerceIn(0, 23)
      }
      return LocalTime.of(hour24, minute)
    }
  }
}

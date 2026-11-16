package com.example

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitFirebaseTask(): T = suspendCancellableCoroutine { cont ->
  addOnSuccessListener { result ->
    if (cont.isActive) cont.resume(result)
  }
  addOnFailureListener { err ->
    if (cont.isActive) cont.resumeWithException(err)
  }
  addOnCanceledListener {
    if (cont.isActive) cont.cancel()
  }
}

internal data class RealtimeDbHttpResponse(
  val statusCode: Int,
  val body: String
)

internal class RealtimeDbHttpException(
  val statusCode: Int,
  val responseBody: String,
  message: String = "Realtime Database HTTP $statusCode"
) : Exception(message)

/**
 * Shared Firebase Realtime Database REST transport using standard `HttpURLConnection` + `org.json.JSONObject`
 * so zero extra Gradle dependencies are needed and missing `firebase_url` in `google-services.json`
 * is handled cleanly via dynamic asia-southeast1 fallback.
 */
internal object SibilRealtimeDbRest {
  private const val FALLBACK_PROJECT_ID = "gen-lang-client-0434293060"
  private const val TAG = "SibilRTDB"

  fun resolveDatabaseUrl(): String {
    return try {
      val options = FirebaseApp.getInstance().options
      val configured = options.databaseUrl?.trim().orEmpty().trimEnd('/')
      if (configured.isNotEmpty()) {
        configured
      } else {
        val projectId = options.projectId?.trim().orEmpty().ifEmpty { FALLBACK_PROJECT_ID }
        "https://${projectId}-default-rtdb.asia-southeast1.firebasedatabase.app"
      }
    } catch (_: Throwable) {
      "https://${FALLBACK_PROJECT_ID}-default-rtdb.asia-southeast1.firebasedatabase.app"
    }
  }

  suspend fun fetchIdToken(user: FirebaseUser, forceRefresh: Boolean = false): String? {
    return try {
      user.getIdToken(forceRefresh).awaitFirebaseTask().token?.takeIf { it.isNotBlank() }
    } catch (e: Throwable) {
      Log.w(TAG, "Unable to fetch Firebase ID token: ${e.message}")
      null
    }
  }

  suspend fun requestJson(
    method: String,
    relativePath: String,
    idToken: String?,
    jsonBody: String? = null
  ): RealtimeDbHttpResponse = withContext(Dispatchers.IO) {
    val dbUrl = resolveDatabaseUrl().trimEnd('/')
    val cleanPath = relativePath.trimStart('/').removeSuffix(".json")
    val authQuery = if (!idToken.isNullOrBlank()) {
      "?auth=${URLEncoder.encode(idToken, "UTF-8")}"
    } else {
      ""
    }
    val endpoint = "$dbUrl/$cleanPath.json$authQuery"
    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
      connectTimeout = 10_000
      readTimeout = 12_000
      setRequestProperty("Accept", "application/json")
      val upperMethod = method.uppercase(Locale.US)
      if (upperMethod == "PATCH") {
        try {
          requestMethod = "PATCH"
        } catch (_: ProtocolException) {
          requestMethod = "POST"
          setRequestProperty("X-HTTP-Method-Override", "PATCH")
        }
      } else {
        requestMethod = upperMethod
      }
      if (jsonBody != null && (upperMethod == "PUT" || upperMethod == "POST" || upperMethod == "PATCH")) {
        doOutput = true
        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
      }
    }

    try {
      if (jsonBody != null && connection.doOutput) {
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
          writer.write(jsonBody)
          writer.flush()
        }
      }
      val statusCode = connection.responseCode
      val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
      val responseText = stream?.let { input ->
        BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
      }.orEmpty()
      RealtimeDbHttpResponse(statusCode = statusCode, body = responseText)
    } finally {
      connection.disconnect()
    }
  }
}

internal data class RemoteSyncSettings(
  val isDarkMode: Boolean? = null,
  val profileName: String = "",
  val username: String = "",
  val updatedAt: Long = 0L
) {
  fun toJsonObject(): JSONObject = JSONObject().apply {
    if (isDarkMode != null) {
      put("isDarkMode", isDarkMode)
      put("darkMode", isDarkMode)
    }
    put("profileName", profileName)
    put("displayName", profileName)
    put("username", username)
    put("updatedAt", updatedAt)
  }

  companion object {
    fun fromJsonObjectOrNull(obj: JSONObject?): RemoteSyncSettings? {
      if (obj == null) return null
      val darkMode = when {
        obj.has("isDarkMode") && !obj.isNull("isDarkMode") -> obj.optBoolean("isDarkMode")
        obj.has("darkMode") && !obj.isNull("darkMode") -> obj.optBoolean("darkMode")
        else -> null
      }
      val profile = obj.optString("profileName", "").trim()
        .ifEmpty { obj.optString("displayName", "").trim() }
      val user = obj.optString("username", "").trim()
      val updated = obj.optLong("updatedAt", 0L)
      return RemoteSyncSettings(
        isDarkMode = darkMode,
        profileName = profile,
        username = user,
        updatedAt = updated
      )
    }
  }
}

internal data class RemoteSyncPayload(
  val tasks: List<Task>? = null,
  val schedules: List<ClassSchedule>? = null,
  val flashcards: List<FlashcardDeck>? = null,
  val settings: RemoteSyncSettings? = null,
  val updatedAt: Long = 0L
)

internal interface RealtimeSyncGateway {
  suspend fun pullSyncData(uid: String, idToken: String): RemoteSyncPayload?
  suspend fun pushSyncData(uid: String, idToken: String, payload: RemoteSyncPayload)
  suspend fun patchSettings(uid: String, idToken: String, settings: RemoteSyncSettings)
}

internal object SibilSyncJsonCodec {
  fun tasksToJsonArray(tasks: List<Task>): JSONArray {
    val array = JSONArray()
    for (task in tasks) {
      val resolvedCategory = task.categoryTag.trim().ifEmpty {
        task.subjectCategory.trim().ifEmpty { DEFAULT_TASK_CATEGORY }
      }
      val subtasksJson = JSONArray()
      task.subtasks.forEach { st ->
        subtasksJson.put(
          JSONObject().apply {
            put("id", st.id)
            put("text", st.text)
            put("isDone", st.isDone)
          }
        )
      }
      array.put(
        JSONObject().apply {
          put("id", task.id)
          put("taskUid", task.taskUid.ifBlank { task.id.toString() })
          put("title", task.title)
          put("description", task.description)
          put("dueDateIso", task.dueDateIso)
          put("dueDate", task.dueDate.ifBlank { formatIsoDateSmartDisplay(task.dueDateIso) })
          put("priority", task.priority.coerceIn(0, 3))
          put("categoryTag", resolvedCategory)
          put("subjectCategory", resolvedCategory)
          put("subtasks", subtasksJson)
          put("isCompleted", task.isCompleted)
          if (task.completedAtMillis != null) {
            put("completedAtMillis", task.completedAtMillis)
          }
          put("createdAtMillis", task.createdAtMillis)
        }
      )
    }
    return array
  }

  fun parseTasksFromAny(raw: Any?): List<Task> {
    val objects = extractJsonObjects(raw)
    val loaded = mutableListOf<Task>()
    objects.forEachIndexed { index, obj ->
      val title = obj.optString("title", "").trim()
      if (title.isEmpty() || LocalStorageManager.isLegacyPlaceholderTaskTitle(title)) {
        return@forEachIndexed
      }
      val category = obj.optString("categoryTag", "").trim().ifEmpty {
        obj.optString("subjectCategory", DEFAULT_TASK_CATEGORY).trim().ifEmpty { DEFAULT_TASK_CATEGORY }
      }
      val legacyDueDate = obj.optString("dueDate", "Upcoming").ifBlank { "Upcoming" }
      val dueDateIso = obj.optString("dueDateIso", "").trim().ifEmpty {
        inferIsoDateFromLegacyText(legacyDueDate)
      }
      val subtasksList = mutableListOf<SubtaskItem>()
      val subtaskObjs = extractJsonObjects(obj.opt("subtasks"))
      for (stObj in subtaskObjs) {
        val stText = stObj.optString("text", "").trim()
        if (stText.isNotEmpty()) {
          subtasksList.add(
            SubtaskItem(
              id = stObj.optString("id", UUID.randomUUID().toString()).ifBlank { UUID.randomUUID().toString() },
              text = stText,
              isDone = stObj.optBoolean("isDone", stObj.optBoolean("done", false))
            )
          )
        }
      }
      val idVal = obj.optInt("id", index + 1)
      loaded.add(
        Task(
          id = idVal,
          taskUid = obj.optString("taskUid", idVal.toString()).ifBlank { idVal.toString() },
          title = title,
          description = obj.optString("description", ""),
          dueDateIso = dueDateIso,
          dueDate = if (dueDateIso.isNotBlank()) formatIsoDateSmartDisplay(dueDateIso, legacyDueDate) else legacyDueDate,
          priority = obj.optInt("priority", 0).coerceIn(0, 3),
          categoryTag = category,
          subjectCategory = category,
          subtasks = subtasksList,
          isCompleted = obj.optBoolean("isCompleted", obj.optBoolean("completed", false)),
          completedAtMillis = if (obj.has("completedAtMillis") && !obj.isNull("completedAtMillis")) {
            obj.optLong("completedAtMillis")
          } else {
            null
          },
          createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
        )
      )
    }
    return loaded.sortedByDescending { it.id }
  }

  fun schedulesToJsonArray(schedules: List<ClassSchedule>): JSONArray {
    val array = JSONArray()
    for (schedule in schedules) {
      val daysArray = JSONArray()
      schedule.days.forEach { day -> daysArray.put(day) }
      array.put(
        JSONObject().apply {
          put("id", schedule.id)
          put("subjectName", schedule.subjectName)
          put("timeRange", schedule.timeRange)
          put("days", daysArray)
          put("room", schedule.room)
          put("colorHex", schedule.colorHex)
          put("startTime", schedule.startTime)
          put("endTime", schedule.endTime)
          put("cognitiveWeight", schedule.cognitiveWeight.coerceIn(1, 3))
          put("linkedTopic", schedule.linkedTopic)
          if (!schedule.linkedDeckId.isNullOrBlank()) {
            put("linkedDeckId", schedule.linkedDeckId)
          }
        }
      )
    }
    return array
  }

  fun parseSchedulesFromAny(raw: Any?): List<ClassSchedule> {
    val objects = extractJsonObjects(raw)
    val loaded = mutableListOf<ClassSchedule>()
    objects.forEachIndexed { index, obj ->
      val subjectName = obj.optString("subjectName", "").trim()
      if (subjectName.isEmpty()) return@forEachIndexed
      val daysList = mutableListOf<String>()
      val rawDays = obj.opt("days")
      if (rawDays is JSONArray) {
        for (j in 0 until rawDays.length()) {
          val token = rawDays.optString(j, "").trim()
          if (token.isNotEmpty()) daysList.add(token)
        }
      } else if (rawDays is JSONObject) {
        val keys = rawDays.keys()
        while (keys.hasNext()) {
          val k = keys.next()
          val token = rawDays.optString(k, "").trim()
          if (token.isNotEmpty()) daysList.add(token)
        }
      }
      val timeRange = obj.optString("timeRange", "7:00 AM - 9:00 AM").ifBlank { "7:00 AM - 9:00 AM" }
      val (fallbackStart, fallbackEnd) = extractStartEndTimeTokens(timeRange)
      val rawDeckId = obj.optString("linkedDeckId", "").trim().ifEmpty { null }
      loaded.add(
        ClassSchedule(
          id = obj.optInt("id", 100 + index),
          subjectName = subjectName,
          timeRange = timeRange,
          days = if (daysList.isEmpty()) listOf("M") else daysList,
          room = obj.optString("room", "ENG. 307").ifBlank { "ENG. 307" },
          colorHex = obj.optLong("colorHex", 0xFF00C4CCL),
          startTime = obj.optString("startTime", fallbackStart).ifBlank { fallbackStart },
          endTime = obj.optString("endTime", fallbackEnd).ifBlank { fallbackEnd },
          cognitiveWeight = obj.optInt("cognitiveWeight", 2).coerceIn(1, 3),
          linkedTopic = obj.optString("linkedTopic", "None").ifBlank { "None" },
          linkedDeckId = rawDeckId
        )
      )
    }
    return loaded.sortedBy { parseTimeToFloat(it.startTime) }
  }

  fun flashcardsToJsonArray(decks: List<FlashcardDeck>): JSONArray {
    val array = JSONArray()
    for (deck in decks) {
      val cardsArray = JSONArray()
      for (card in deck.cards) {
        val isMastered = card.isMastered || card.masteryLevel.equals("Good", ignoreCase = true)
        cardsArray.put(
          JSONObject().apply {
            put("id", card.id)
            put("question", card.question)
            put("answer", card.answer)
            put("category", card.category)
            put("masteryLevel", card.masteryLevel.ifBlank { if (isMastered) "Good" else "New" })
            put("isMastered", isMastered)
          }
        )
      }
      array.put(
        JSONObject().apply {
          put("id", deck.id)
          put("title", deck.title.trim().ifEmpty { "Untitled Deck" })
          put("subjectTag", deck.subjectTag.trim().ifEmpty { "General CE" })
          put("cards", cardsArray)
        }
      )
    }
    return array
  }

  fun parseFlashcardsFromAny(raw: Any?): List<FlashcardDeck> {
    val deckObjs = extractJsonObjects(raw)
    val loadedDecks = mutableListOf<FlashcardDeck>()
    deckObjs.forEachIndexed { i, deckObj ->
      val title = deckObj.optString("title", "").trim()
      if (title.isEmpty()) return@forEachIndexed
      val subjectTag = deckObj.optString("subjectTag", "General CE").trim().ifEmpty { "General CE" }
      val cardObjs = extractJsonObjects(deckObj.opt("cards"))
      val loadedCards = mutableListOf<FlashcardItem>()
      cardObjs.forEachIndexed { j, cardObj ->
        val question = cardObj.optString("question", "").trim()
        val answer = cardObj.optString("answer", "").trim()
        if (question.isEmpty() || answer.isEmpty()) return@forEachIndexed
        val rawMastery = cardObj.optString("masteryLevel", "New").trim().ifEmpty { "New" }
        val isMastered = cardObj.optBoolean("isMastered", rawMastery.equals("Good", ignoreCase = true))
        loadedCards.add(
          FlashcardItem(
            id = cardObj.optInt("id", (i + 1) * 100 + j + 1),
            question = question,
            answer = answer,
            category = cardObj.optString("category", subjectTag).trim().ifEmpty { subjectTag },
            masteryLevel = if (isMastered && rawMastery.equals("New", ignoreCase = true)) "Good" else rawMastery,
            isMastered = isMastered
          )
        )
      }
      loadedDecks.add(
        FlashcardDeck(
          id = deckObj.optInt("id", i + 1),
          title = title,
          subjectTag = subjectTag,
          cards = loadedCards
        )
      )
    }
    return loadedDecks.sortedByDescending { it.id }
  }

  fun payloadToJsonObject(payload: RemoteSyncPayload): JSONObject {
    return JSONObject().apply {
      put("tasks", tasksToJsonArray(payload.tasks.orEmpty()))
      put("schedules", schedulesToJsonArray(payload.schedules.orEmpty()))
      put("flashcards", flashcardsToJsonArray(payload.flashcards.orEmpty()))
      if (payload.settings != null) {
        put("settings", payload.settings.toJsonObject())
      }
      put("updatedAt", if (payload.updatedAt > 0L) payload.updatedAt else System.currentTimeMillis())
    }
  }

  fun parsePayloadFromJson(rawBody: String): RemoteSyncPayload? {
    val trimmed = rawBody.trim()
    if (trimmed.isEmpty() || trimmed == "null") return null
    val root = JSONObject(trimmed)
    val tasks = if (root.has("tasks") && !root.isNull("tasks")) {
      parseTasksFromAny(root.opt("tasks"))
    } else if (root.has("tasks")) {
      emptyList()
    } else {
      null
    }
    val schedules = if (root.has("schedules") && !root.isNull("schedules")) {
      parseSchedulesFromAny(root.opt("schedules"))
    } else if (root.has("schedules")) {
      emptyList()
    } else {
      null
    }
    val flashcards = if (root.has("flashcards") && !root.isNull("flashcards")) {
      parseFlashcardsFromAny(root.opt("flashcards"))
    } else if (root.has("flashcards")) {
      emptyList()
    } else {
      null
    }
    val settings = RemoteSyncSettings.fromJsonObjectOrNull(root.optJSONObject("settings"))
    return RemoteSyncPayload(
      tasks = tasks,
      schedules = schedules,
      flashcards = flashcards,
      settings = settings,
      updatedAt = root.optLong("updatedAt", 0L)
    )
  }

  private fun extractJsonObjects(raw: Any?): List<JSONObject> {
    return when (raw) {
      is JSONArray -> buildList {
        for (i in 0 until raw.length()) {
          val item = raw.optJSONObject(i)
          if (item != null) add(item)
        }
      }
      is JSONObject -> buildList {
        val keys = raw.keys().asSequence().toList()
          .sortedWith(compareBy({ it.toIntOrNull() ?: Int.MAX_VALUE }, { it }))
        for (key in keys) {
          val item = raw.optJSONObject(key)
          if (item != null) add(item)
        }
      }
      else -> emptyList()
    }
  }
}

internal object FirebaseRealtimeSyncGateway : RealtimeSyncGateway {
  override suspend fun pullSyncData(uid: String, idToken: String): RemoteSyncPayload? {
    val response = SibilRealtimeDbRest.requestJson(
      method = "GET",
      relativePath = "users/$uid/syncData",
      idToken = idToken
    )
    if (response.statusCode == 404 || response.body.trim() == "null") {
      return null
    }
    if (response.statusCode !in 200..299) {
      throw RealtimeDbHttpException(response.statusCode, response.body)
    }
    return SibilSyncJsonCodec.parsePayloadFromJson(response.body)
  }

  override suspend fun pushSyncData(uid: String, idToken: String, payload: RemoteSyncPayload) {
    val json = SibilSyncJsonCodec.payloadToJsonObject(payload).toString()
    val response = SibilRealtimeDbRest.requestJson(
      method = "PUT",
      relativePath = "users/$uid/syncData",
      idToken = idToken,
      jsonBody = json
    )
    if (response.statusCode !in 200..299) {
      throw RealtimeDbHttpException(response.statusCode, response.body)
    }
  }

  override suspend fun patchSettings(uid: String, idToken: String, settings: RemoteSyncSettings) {
    val json = settings.toJsonObject().toString()
    val response = SibilRealtimeDbRest.requestJson(
      method = "PATCH",
      relativePath = "users/$uid/syncData/settings",
      idToken = idToken,
      jsonBody = json
    )
    if (response.statusCode !in 200..299) {
      throw RealtimeDbHttpException(response.statusCode, response.body)
    }
  }
}

internal data class MergedSyncState(
  val tasks: List<Task>,
  val schedules: List<ClassSchedule>,
  val flashcards: List<FlashcardDeck>,
  val settings: RemoteSyncSettings?,
  val shouldPushBackMergedToCloud: Boolean
)

/**
 * Pure merge helper for Firebase Realtime Database `users/{uid}/syncData` so pulling on sign-in
 * preserves both cloud items and any offline/pre-sign-in local items without duplicates or data loss.
 */
internal object SibilRealtimeSyncMerger {
  fun mergeOnPull(
    localTasks: List<Task>,
    localSchedules: List<ClassSchedule>,
    localDecks: List<FlashcardDeck>,
    localDarkMode: Boolean,
    localDisplayName: String,
    localUsername: String,
    remote: RemoteSyncPayload?
  ): MergedSyncState {
    if (remote == null) {
      val hasAnyLocal = localTasks.isNotEmpty() || localSchedules.isNotEmpty() || localDecks.isNotEmpty()
      return MergedSyncState(
        tasks = localTasks.sortedByDescending { it.id },
        schedules = localSchedules.sortedBy { parseTimeToFloat(it.startTime) },
        flashcards = localDecks.sortedByDescending { it.id },
        settings = RemoteSyncSettings(
          isDarkMode = localDarkMode,
          profileName = localDisplayName,
          username = localUsername,
          updatedAt = System.currentTimeMillis()
        ),
        shouldPushBackMergedToCloud = hasAnyLocal
      )
    }

    var shouldPushBack = false

    // 1. Merge Tasks
    val mergedTasks = if (remote.tasks == null) {
      if (localTasks.isNotEmpty()) shouldPushBack = true
      localTasks.toMutableList()
    } else {
      val list = remote.tasks.toMutableList()
      val usedIds = list.map { it.id }.toMutableSet()
      val seenSignatures = list.map { taskSignature(it) }.toMutableSet()
      var nextAllocId = (list.maxOfOrNull { it.id } ?: 0) + 1
      for (localTask in localTasks) {
        val sig = taskSignature(localTask)
        if (seenSignatures.add(sig)) {
          val resolvedId = if (usedIds.add(localTask.id)) localTask.id else {
            while (!usedIds.add(nextAllocId)) nextAllocId++
            nextAllocId++
          }
          list.add(localTask.copy(id = resolvedId, taskUid = localTask.taskUid.ifBlank { resolvedId.toString() }))
          shouldPushBack = true
        }
      }
      list
    }

    // 2. Merge Class Schedules
    val mergedSchedules = if (remote.schedules == null) {
      if (localSchedules.isNotEmpty()) shouldPushBack = true
      localSchedules.toMutableList()
    } else {
      val list = remote.schedules.toMutableList()
      val usedIds = list.map { it.id }.toMutableSet()
      val seenSignatures = list.map { scheduleSignature(it) }.toMutableSet()
      var nextAllocId = (list.maxOfOrNull { it.id } ?: 99) + 1
      for (localSchedule in localSchedules) {
        val sig = scheduleSignature(localSchedule)
        if (seenSignatures.add(sig)) {
          val resolvedId = if (usedIds.add(localSchedule.id)) localSchedule.id else {
            while (!usedIds.add(nextAllocId)) nextAllocId++
            nextAllocId++
          }
          list.add(localSchedule.copy(id = resolvedId))
          shouldPushBack = true
        }
      }
      list
    }

    // 3. Merge Flashcard Decks (merging cards into matching deck title)
    val mergedDecks = if (remote.flashcards == null) {
      if (localDecks.isNotEmpty()) shouldPushBack = true
      localDecks.toMutableList()
    } else {
      val list = remote.flashcards.toMutableList()
      val usedDeckIds = list.map { it.id }.toMutableSet()
      var nextAllocDeckId = (list.maxOfOrNull { it.id } ?: 0) + 1
      var nextAllocCardId = maxOf(
        1000,
        (list.flatMap { it.cards }.maxOfOrNull { it.id } ?: 999) + 1
      )
      for (localDeck in localDecks) {
        val existingIdx = list.indexOfFirst {
          it.title.trim().equals(localDeck.title.trim(), ignoreCase = true)
        }
        if (existingIdx == -1) {
          val resolvedDeckId = if (usedDeckIds.add(localDeck.id)) localDeck.id else {
            while (!usedDeckIds.add(nextAllocDeckId)) nextAllocDeckId++
            nextAllocDeckId++
          }
          list.add(localDeck.copy(id = resolvedDeckId))
          shouldPushBack = true
        } else {
          val remoteDeck = list[existingIdx]
          val existingCardSigs = remoteDeck.cards.map {
            "${it.question.trim().lowercase(Locale.US)}|${it.answer.trim().lowercase(Locale.US)}"
          }.toMutableSet()
          val extraCards = mutableListOf<FlashcardItem>()
          for (localCard in localDeck.cards) {
            val sig = "${localCard.question.trim().lowercase(Locale.US)}|${localCard.answer.trim().lowercase(Locale.US)}"
            if (existingCardSigs.add(sig)) {
              extraCards.add(localCard.copy(id = nextAllocCardId++))
            }
          }
          if (extraCards.isNotEmpty()) {
            val combinedCards = remoteDeck.cards + extraCards
            list[existingIdx] = remoteDeck.copy(
              cards = combinedCards,
              masteredCardIds = combinedCards.filter { it.isMastered }.map { it.id }
            )
            shouldPushBack = true
          }
        }
      }
      list
    }

    if (remote.settings == null) {
      shouldPushBack = true
    }

    return MergedSyncState(
      tasks = mergedTasks.sortedByDescending { it.id },
      schedules = mergedSchedules.sortedBy { parseTimeToFloat(it.startTime) },
      flashcards = mergedDecks.sortedByDescending { it.id },
      settings = remote.settings,
      shouldPushBackMergedToCloud = shouldPushBack
    )
  }

  private fun taskSignature(task: Task): String {
    return "${task.title.trim().lowercase(Locale.US)}|${task.dueDateIso.trim()}|${task.subjectCategory.trim().lowercase(Locale.US)}"
  }

  private fun scheduleSignature(schedule: ClassSchedule): String {
    val daysSig = schedule.days.map { it.trim().lowercase(Locale.US) }.sorted().joinToString(",")
    return "${schedule.subjectName.trim().lowercase(Locale.US)}|${schedule.timeRange.trim().lowercase(Locale.US)}|$daysSig"
  }
}

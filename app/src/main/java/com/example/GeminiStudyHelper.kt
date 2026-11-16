package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

// ============================================================================
// SIBIL AI • GLOBAL HOLOGRAPHIC GEMINI ASSISTANT
// - Multi-Chat Thread Persistence (`sibil_gemini_sessions`)
// - Live Google Search Grounding (`google_search`) + Clickable Source Pills
// - Open-Topic Freedom + Strict Explicit Content Censorship (`safetySettings`)
// ============================================================================

data class WebSource(
  val title: String,
  val uri: String
) {
  fun toJsonObject(): JSONObject = JSONObject().apply {
    put("title", title)
    put("uri", uri)
  }

  companion object {
    fun fromJsonObject(obj: JSONObject?): WebSource? {
      if (obj == null) return null
      val uri = obj.optString("uri").trim()
      if (uri.isEmpty()) return null
      val title = obj.optString("title").trim().ifEmpty {
        try {
          URL(uri).host.ifBlank { "Web Source" }
        } catch (_: Exception) {
          "Web Source"
        }
      }
      return WebSource(title = title, uri = uri)
    }
  }
}

data class GeminiMessage(
  val id: String = UUID.randomUUID().toString(),
  val isUser: Boolean,
  val text: String,
  val imageBase64: String? = null,
  val sources: List<WebSource> = emptyList(),
  val isSafetyBlocked: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
) {
  fun toJsonObject(): JSONObject = JSONObject().apply {
    put("id", id)
    put("isUser", isUser)
    put("text", text)
    if (!imageBase64.isNullOrBlank()) put("imageBase64", imageBase64)
    if (sources.isNotEmpty()) {
      put("sources", JSONArray().apply { sources.forEach { put(it.toJsonObject()) } })
    }
    if (isSafetyBlocked) put("isSafetyBlocked", true)
    put("timestamp", timestamp)
  }

  companion object {
    fun fromJsonObject(obj: JSONObject?): GeminiMessage? {
      if (obj == null) return null
      val id = obj.optString("id").trim().ifEmpty { UUID.randomUUID().toString() }
      val text = obj.optString("text")
      val imageBase64 = obj.optString("imageBase64").trim().takeIf { it.isNotEmpty() }
      if (text.isBlank() && imageBase64 == null) return null
      val sourcesArr = obj.optJSONArray("sources")
      val parsedSources = buildList {
        if (sourcesArr != null) {
          for (i in 0 until sourcesArr.length()) {
            WebSource.fromJsonObject(sourcesArr.optJSONObject(i))?.let { add(it) }
          }
        }
      }
      return GeminiMessage(
        id = id,
        isUser = obj.optBoolean("isUser", false),
        text = text,
        imageBase64 = imageBase64,
        sources = parsedSources.distinctBy { it.uri },
        isSafetyBlocked = obj.optBoolean("isSafetyBlocked", false),
        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
      )
    }
  }
}

data class GeminiChatThread(
  val id: String = UUID.randomUUID().toString(),
  val title: String = "New Chat",
  val updatedAt: Long = System.currentTimeMillis(),
  val messages: List<GeminiMessage> = emptyList()
) {
  fun toJsonObject(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("updatedAt", updatedAt)
    put("messages", JSONArray().apply { messages.forEach { put(it.toJsonObject()) } })
  }

  companion object {
    fun fromJsonObject(obj: JSONObject?): GeminiChatThread? {
      if (obj == null) return null
      val id = obj.optString("id").trim().ifEmpty { UUID.randomUUID().toString() }
      val title = obj.optString("title").trim().ifEmpty { "New Chat" }
      val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
      val msgArr = obj.optJSONArray("messages")
      val parsedMessages = buildList {
        if (msgArr != null) {
          for (i in 0 until msgArr.length()) {
            GeminiMessage.fromJsonObject(msgArr.optJSONObject(i))?.let { add(it) }
          }
        }
      }
      return GeminiChatThread(
        id = id,
        title = title,
        updatedAt = updatedAt,
        messages = parsedMessages
      )
    }
  }
}

data class StudyImageAttachment(
  val base64Jpeg: String,
  val previewBitmap: ImageBitmap,
  val width: Int,
  val height: Int
)

internal enum class GroundingToolMode {
  GOOGLE_SEARCH,
  LEGACY_RETRIEVAL,
  NONE
}

private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)

private val HoloRimColors = listOf(
  GoogleBlue,
  GoogleRed,
  GoogleYellow,
  GoogleGreen,
  GoogleBlue
)

private val SibilAiAccentGradient = Brush.linearGradient(
  colors = listOf(
    GoogleBlue,
    Color(0xFF0284C7),
    Color(0xFF6366F1)
  )
)

/**
 * Reusable Google-inspired 4-Color Concave Gemini Sparkle Vector Logo (`SibilAiLogo`):
 * - Primary 4-point concave star drawn with smooth cubic Bezier curves blended across
 *   Google Blue (`#4285F4`), Red (`#EA4335`), Yellow (`#FBBC05`), and Green (`#34A853`).
 * - Companion top-right micro-sparkle rendered in Blue/Green.
 */
@Composable
fun SibilAiLogo(
  modifier: Modifier = Modifier.size(24.dp)
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val minDim = minOf(w, h)

    // Primary 4-point concave Gemini star
    val cx = w * 0.43f
    val cy = h * 0.55f
    val r = minDim * 0.43f

    val mainStar = Path().apply {
      moveTo(cx, cy - r)
      cubicTo(
        cx + r * 0.04f, cy - r * 0.34f,
        cx + r * 0.34f, cy - r * 0.04f,
        cx + r, cy
      )
      cubicTo(
        cx + r * 0.34f, cy + r * 0.04f,
        cx + r * 0.04f, cy + r * 0.34f,
        cx, cy + r
      )
      cubicTo(
        cx - r * 0.04f, cy + r * 0.34f,
        cx - r * 0.34f, cy + r * 0.04f,
        cx - r, cy
      )
      cubicTo(
        cx - r * 0.34f, cy - r * 0.04f,
        cx - r * 0.04f, cy - r * 0.34f,
        cx, cy - r
      )
      close()
    }

    drawPath(
      path = mainStar,
      brush = Brush.linearGradient(
        colorStops = arrayOf(
          0.0f to GoogleBlue,
          0.35f to GoogleRed,
          0.68f to GoogleYellow,
          1.0f to GoogleGreen
        ),
        start = Offset(cx - r, cy - r),
        end = Offset(cx + r, cy + r)
      )
    )

    // Companion micro-sparkle at top-right
    val mcx = w * 0.84f
    val mcy = h * 0.18f
    val mr = minDim * 0.16f

    val microStar = Path().apply {
      moveTo(mcx, mcy - mr)
      cubicTo(
        mcx + mr * 0.04f, mcy - mr * 0.34f,
        mcx + mr * 0.34f, mcy - mr * 0.04f,
        mcx + mr, mcy
      )
      cubicTo(
        mcx + mr * 0.34f, mcy + mr * 0.04f,
        mcx + mr * 0.04f, mcy + mr * 0.34f,
        mcx, mcy + mr
      )
      cubicTo(
        mcx - mr * 0.04f, mcy + mr * 0.34f,
        mcx - mr * 0.34f, mcy + mr * 0.04f,
        mcx - mr, mcy
      )
      cubicTo(
        mcx - mr * 0.34f, mcy - mr * 0.04f,
        mcx - mr * 0.04f, mcy - mr * 0.34f,
        mcx, mcy - mr
      )
      close()
    }

    drawPath(
      path = microStar,
      brush = Brush.linearGradient(
        colors = listOf(GoogleBlue, GoogleGreen),
        start = Offset(mcx - mr, mcy - mr),
        end = Offset(mcx + mr, mcy + mr)
      )
    )
  }
}

/**
 * Global Holographic Floating Gemini Button (`56.dp`):
 * - Subtle animated 4-color Google-spectrum outer ring (`#4285F4`, `#EA4335`, `#FBBC05`, `#34A853`)
 * - Translucent frosted-glass core with top-left specular highlight
 * - Custom 4-color `SibilAiLogo` emblem (`28.dp`)
 */
@Composable
fun HolographicGeminiButton(
  isDarkMode: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "holo_gemini_rim")
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "holo_rim_rotation"
  )

  val sweepRimBrush = remember { Brush.sweepGradient(HoloRimColors) }
  val coreColor = if (isDarkMode) {
    Color(0xFF0F172A).copy(alpha = 0.84f)
  } else {
    Color.White.copy(alpha = 0.90f)
  }
  val specularHighlight = remember(isDarkMode) {
    Brush.linearGradient(
      colors = listOf(
        Color.White.copy(alpha = if (isDarkMode) 0.22f else 0.75f),
        Color.Transparent
      ),
      start = Offset.Zero,
      end = Offset(110f, 110f)
    )
  }

  Box(
    modifier = modifier
      .size(56.dp)
      .shadow(
        elevation = 10.dp,
        shape = CircleShape,
        ambientColor = GoogleBlue.copy(alpha = 0.38f),
        spotColor = GoogleGreen.copy(alpha = 0.34f)
      )
      .clip(CircleShape)
      .clickable(
        onClickLabel = "Open Sibil AI Assistant",
        onClick = onClick
      )
      .testTag("holographic_gemini_fab"),
    contentAlignment = Alignment.Center
  ) {
    // Subtle animated 4-color Google-spectrum outer rim
    Box(
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer { rotationZ = rotationAngle }
        .border(width = 2.dp, brush = sweepRimBrush, shape = CircleShape)
    )

    // Translucent frosted-glass inner core + top-left specular sheen
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(2.dp)
        .clip(CircleShape)
        .background(coreColor)
        .background(specularHighlight),
      contentAlignment = Alignment.Center
    ) {
      SibilAiLogo(modifier = Modifier.size(28.dp))
    }
  }
}

/**
 * Pure Android SDK / JDK Gemini REST & Multi-Chat Session Engine (`GeminiStudyClient`):
 * - Uses shared `resolveInjectedGeminiKeys(context)` pipeline.
 * - Persists multiple `GeminiChatThread` conversations in `SharedPreferences("sibil_gemini_sessions")`.
 * - Enables live Google Search grounding (`tools: [{"google_search": {}}]`) and parses clickable citations.
 * - Enforces strict Gemini API `safetySettings` + pre-flight explicit-content filter.
 */
object GeminiStudyClient {
  const val SIBIL_PREFS_NAME = "sibil_prefs"
  const val PREFS_NAME = "sibil_app_offline_storage"
  const val SESSIONS_PREFS_NAME = "sibil_gemini_sessions"
  const val CUSTOM_KEY_PREF = "sibil_gemini_custom_key"
  private const val THREADS_KEY = "threads_json"
  private const val ACTIVE_THREAD_ID_KEY = "active_thread_id"
  private const val MAX_PERSISTED_THREADS = 40

  const val SAFETY_REFUSAL_MESSAGE =
    "This topic is restricted by Sibil AI's explicit content safety policy."

  private val keyRotationCursor = AtomicInteger(0)

  val MODEL_FALLBACK_ORDER = listOf(
    "gemini-3.5-flash-lite",
    "gemini-2.5-flash",
    "gemini-3.8-flash"
  )

  const val GENERAL_SYSTEM_INSTRUCTION =
    "You are Sibil AI, an intelligent, helpful assistant built into Sibil CE. Give users full freedom to ask about any topic—including Civil Engineering (MSTE, PSAD, HPGE, NSCP 2015, Surveying, Max Fajardo Estimating, Casio fx-570ES PLUS shortcuts), programming, science, history, writing, or general everyday questions. Use Google Search grounding for current or factual queries. However, you must strictly refuse and censor any sexually explicit, NSFW, pornographic, erotic, violent, self-harm, or hate-speech content."

  private val EXPLICIT_CONTENT_PATTERNS = listOf(
    Regex("""\b(porn|pornography|pornographic|hentai|nsfw|xxx|nudes?|nudity|onlyfans)\b""", RegexOption.IGNORE_CASE),
    Regex("""\b(erotic|erotica|sexually\s+explicit|explicit\s+sex|hardcore\s+sex|anal\s+sex|oral\s+sex|blowjob|handjob)\b""", RegexOption.IGNORE_CASE),
    Regex("""\b(masturbat(e|ing|ion)|orgasm|ejaculat(e|ion)|incest|bestiality|pedophil(e|ia)|child\s+porn)\b""", RegexOption.IGNORE_CASE),
    Regex("""\b(how\s+to\s+(commit\s+suicide|kill\s+myself|self[\s-]harm|make\s+a\s+bomb))\b""", RegexOption.IGNORE_CASE)
  )

  fun isExplicitOrRestrictedPrompt(prompt: String): Boolean {
    val clean = prompt.trim()
    if (clean.isEmpty()) return false
    return EXPLICIT_CONTENT_PATTERNS.any { it.containsMatchIn(clean) }
  }

  fun getCustomApiKey(context: Context): String {
    val appCtx = context.applicationContext
    val sibilPrefs = appCtx.getSharedPreferences(SIBIL_PREFS_NAME, Context.MODE_PRIVATE)
    val primary = sibilPrefs.getString(CUSTOM_KEY_PREF, "")?.trim().orEmpty()
    if (primary.isNotEmpty()) return primary
    val offlinePrefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return offlinePrefs.getString(CUSTOM_KEY_PREF, "")?.trim().orEmpty()
  }

  fun saveCustomApiKey(context: Context, key: String) {
    val clean = key.trim()
    val appCtx = context.applicationContext
    val sibilPrefs = appCtx.getSharedPreferences(SIBIL_PREFS_NAME, Context.MODE_PRIVATE)
    val offlinePrefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    if (clean.isEmpty()) {
      sibilPrefs.edit().remove(CUSTOM_KEY_PREF).apply()
      offlinePrefs.edit().remove(CUSTOM_KEY_PREF).apply()
    } else {
      sibilPrefs.edit().putString(CUSTOM_KEY_PREF, clean).apply()
      offlinePrefs.edit().putString(CUSTOM_KEY_PREF, clean).apply()
    }
  }

  fun getOrderedApiKeys(context: Context): List<String> = getSharedGeminiApiKeys(context)

  internal fun getRotatedApiKeys(context: Context): List<String> {
    val overrideKeys = getSharedPersonalOverrideKeys(context)
    val injectedPool = getSharedInjectedAndBuildConfigKeys()
    val rotatedInjected = if (injectedPool.size <= 1) {
      injectedPool
    } else {
      val start = (keyRotationCursor.getAndIncrement() and 0x7FFFFFFF) % injectedPool.size
      injectedPool.drop(start) + injectedPool.take(start)
    }
    return (overrideKeys + rotatedInjected).distinct()
  }

  fun hasConfiguredKey(context: Context): Boolean = getOrderedApiKeys(context).isNotEmpty()

  fun deriveAutoTitle(prompt: String, hasImage: Boolean = false): String {
    val collapsed = prompt.replace(Regex("\\s+"), " ").trim()
    if (collapsed.isEmpty()) {
      return if (hasImage) "Photo Question" else "New Chat"
    }
    return collapsed.take(38)
  }

  fun loadThreads(context: Context): List<GeminiChatThread> {
    return try {
      val prefs = context.applicationContext.getSharedPreferences(SESSIONS_PREFS_NAME, Context.MODE_PRIVATE)
      val raw = prefs.getString(THREADS_KEY, "").orEmpty()
      if (raw.isBlank()) return emptyList()
      val arr = JSONArray(raw)
      buildList {
        for (i in 0 until arr.length()) {
          GeminiChatThread.fromJsonObject(arr.optJSONObject(i))?.let { add(it) }
        }
      }.sortedByDescending { it.updatedAt }
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun saveThreads(
    context: Context,
    threads: List<GeminiChatThread>,
    activeThreadId: String? = null
  ) {
    try {
      val prefs = context.applicationContext.getSharedPreferences(SESSIONS_PREFS_NAME, Context.MODE_PRIVATE)
      val sorted = threads
        .distinctBy { it.id }
        .sortedByDescending { it.updatedAt }
        .take(MAX_PERSISTED_THREADS)
      val arr = JSONArray().apply {
        sorted.forEach { put(it.toJsonObject()) }
      }
      prefs.edit().apply {
        putString(THREADS_KEY, arr.toString())
        if (activeThreadId != null) {
          putString(ACTIVE_THREAD_ID_KEY, activeThreadId)
        }
      }.apply()
    } catch (e: Exception) {
      Log.w("GeminiStudyClient", "Failed saving threads: ${e.message}")
    }
  }

  fun upsertThread(context: Context, thread: GeminiChatThread): List<GeminiChatThread> {
    val existing = loadThreads(context).filterNot { it.id == thread.id }
    val updated = (listOf(thread) + existing)
      .sortedByDescending { it.updatedAt }
      .take(MAX_PERSISTED_THREADS)
    saveThreads(context, updated, activeThreadId = thread.id)
    return updated
  }

  fun deleteThread(context: Context, threadId: String): List<GeminiChatThread> {
    val remaining = loadThreads(context).filterNot { it.id == threadId }
    val nextActive = remaining.firstOrNull()?.id
    saveThreads(context, remaining, activeThreadId = nextActive)
    return remaining
  }

  fun loadActiveThreadId(context: Context): String? {
    val prefs = context.applicationContext.getSharedPreferences(SESSIONS_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(ACTIVE_THREAD_ID_KEY, null)?.trim()?.takeIf { it.isNotEmpty() }
  }

  fun saveActiveThreadId(context: Context, threadId: String?) {
    val prefs = context.applicationContext.getSharedPreferences(SESSIONS_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().apply {
      if (threadId.isNullOrBlank()) remove(ACTIVE_THREAD_ID_KEY) else putString(ACTIVE_THREAD_ID_KEY, threadId)
    }.apply()
  }

  suspend fun prepareImageAttachment(
    context: Context,
    imageUri: Uri
  ): StudyImageAttachment? = withContext(Dispatchers.IO) {
    try {
      val maxDim = 1024
      val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      context.contentResolver.openInputStream(imageUri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, boundsOpts)
      }
      val origW = boundsOpts.outWidth
      val origH = boundsOpts.outHeight
      if (origW <= 0 || origH <= 0) return@withContext null

      var sampleSize = 1
      while ((origW / sampleSize) > maxDim * 2 || (origH / sampleSize) > maxDim * 2) {
        sampleSize *= 2
      }
      val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
      val decoded = context.contentResolver.openInputStream(imageUri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodeOpts)
      } ?: return@withContext null

      val longest = max(decoded.width, decoded.height)
      val scaled = if (longest > maxDim) {
        val ratio = maxDim.toFloat() / longest.toFloat()
        val targetW = (decoded.width * ratio).roundToInt().coerceAtLeast(1)
        val targetH = (decoded.height * ratio).roundToInt().coerceAtLeast(1)
        Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
          if (it !== decoded) decoded.recycle()
        }
      } else {
        decoded
      }

      val baos = ByteArrayOutputStream()
      scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos)
      val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
      StudyImageAttachment(
        base64Jpeg = b64,
        previewBitmap = scaled.asImageBitmap(),
        width = scaled.width,
        height = scaled.height
      )
    } catch (e: Exception) {
      Log.e("GeminiStudyClient", "Image compression failed: ${e.message}", e)
      null
    }
  }

  sealed class StudyAiCallResult {
    data class Success(val message: GeminiMessage) : StudyAiCallResult()
    data class Failure(val errorMessage: String) : StudyAiCallResult()
  }

  internal fun buildSafetySettingsArray(): JSONArray {
    return JSONArray().apply {
      put(
        JSONObject().apply {
          put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT")
          put("threshold", "BLOCK_LOW_AND_ABOVE")
        }
      )
      put(
        JSONObject().apply {
          put("category", "HARM_CATEGORY_HATE_SPEECH")
          put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
        }
      )
      put(
        JSONObject().apply {
          put("category", "HARM_CATEGORY_HARASSMENT")
          put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
        }
      )
      put(
        JSONObject().apply {
          put("category", "HARM_CATEGORY_DANGEROUS_CONTENT")
          put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
        }
      )
    }
  }

  internal fun buildGenerateContentPayload(
    userPrompt: String,
    history: List<GeminiMessage>,
    imageBase64: String?,
    groundingMode: GroundingToolMode = GroundingToolMode.GOOGLE_SEARCH
  ): JSONObject {
    val contentsArray = JSONArray()
    val recentTurns = history.filterNot { it.isSafetyBlocked }.takeLast(10)
    for (msg in recentTurns) {
      if (msg.text.isNotBlank()) {
        contentsArray.put(
          JSONObject().apply {
            put("role", if (msg.isUser) "user" else "model")
            put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
          }
        )
      }
    }

    val currentParts = JSONArray().apply {
      put(JSONObject().put("text", userPrompt))
      if (!imageBase64.isNullOrBlank()) {
        put(
          JSONObject().put(
            "inline_data",
            JSONObject().apply {
              put("mime_type", "image/jpeg")
              put("data", imageBase64)
            }
          )
        )
      }
    }
    contentsArray.put(
      JSONObject().apply {
        put("role", "user")
        put("parts", currentParts)
      }
    )

    return JSONObject().apply {
      put(
        "system_instruction",
        JSONObject().put("parts", JSONArray().put(JSONObject().put("text", GENERAL_SYSTEM_INSTRUCTION)))
      )
      put("contents", contentsArray)
      put("safetySettings", buildSafetySettingsArray())
      when (groundingMode) {
        GroundingToolMode.GOOGLE_SEARCH -> {
          put("tools", JSONArray().put(JSONObject().put("google_search", JSONObject())))
        }
        GroundingToolMode.LEGACY_RETRIEVAL -> {
          put("tools", JSONArray().put(JSONObject().put("googleSearchRetrieval", JSONObject())))
        }
        GroundingToolMode.NONE -> {
          // Omit tools when model does not support search grounding
        }
      }
    }
  }

  internal fun detectSafetyBlock(root: JSONObject): Boolean {
    val promptBlock = root.optJSONObject("promptFeedback")?.optString("blockReason").orEmpty().trim()
    if (promptBlock.isNotEmpty()) return true
    val candidates = root.optJSONArray("candidates") ?: return false
    if (candidates.length() == 0) return false
    val finishReason = candidates.optJSONObject(0)?.optString("finishReason").orEmpty().uppercase(Locale.US)
    return finishReason == "SAFETY" ||
      finishReason == "PROHIBITED_CONTENT" ||
      finishReason == "SPII" ||
      finishReason == "BLOCKLIST"
  }

  internal fun parseGroundingSources(root: JSONObject): List<WebSource> {
    return try {
      val candidates = root.optJSONArray("candidates") ?: return emptyList()
      if (candidates.length() == 0) return emptyList()
      val metadata = candidates.optJSONObject(0)?.optJSONObject("groundingMetadata") ?: return emptyList()
      val chunks = metadata.optJSONArray("groundingChunks") ?: return emptyList()
      buildList {
        for (i in 0 until chunks.length()) {
          val webObj = chunks.optJSONObject(i)?.optJSONObject("web")
          val parsed = WebSource.fromJsonObject(webObj)
          if (parsed != null && (parsed.uri.startsWith("http://") || parsed.uri.startsWith("https://"))) {
            add(parsed)
          }
        }
      }.distinctBy { it.uri }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun extractCandidateText(root: JSONObject): String {
    return try {
      val candidates = root.optJSONArray("candidates") ?: return ""
      if (candidates.length() == 0) return ""
      val parts = candidates.optJSONObject(0)
        ?.optJSONObject("content")
        ?.optJSONArray("parts") ?: return ""
      buildString {
        for (i in 0 until parts.length()) {
          val textPart = parts.optJSONObject(i)?.optString("text").orEmpty()
          if (textPart.isNotEmpty()) {
            if (isNotEmpty()) append('\n')
            append(textPart)
          }
        }
      }.trim()
    } catch (_: Exception) {
      ""
    }
  }

  private fun extractErrorMessage(rawErrorJson: String): String {
    return try {
      val root = JSONObject(rawErrorJson)
      root.optJSONObject("error")?.optString("message").orEmpty().ifBlank { rawErrorJson.take(180) }
    } catch (_: Exception) {
      rawErrorJson.take(180).ifBlank { "Request rejected" }
    }
  }

  private fun executeGenerateRequest(
    endpoint: String,
    payloadJson: String
  ): Pair<Int, String> {
    var conn: HttpURLConnection? = null
    return try {
      conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20_000
        readTimeout = 45_000
        doInput = true
        doOutput = true
        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
      }
      conn.outputStream.use { os ->
        os.write(payloadJson.toByteArray(Charsets.UTF_8))
      }
      val status = conn.responseCode
      val body = try {
        val stream = if (status in 200..299) conn.inputStream else conn.errorStream
        stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
      } catch (_: Exception) {
        ""
      }
      status to body
    } finally {
      conn?.disconnect()
    }
  }

  internal fun isInvalidApiKeyError(status: Int, apiMsg: String): Boolean {
    if (status == 401) return true
    if (status != 400 && status != 403) return false
    return apiMsg.contains("API key not valid", ignoreCase = true) ||
      apiMsg.contains("API_KEY_INVALID", ignoreCase = true) ||
      apiMsg.contains("API key expired", ignoreCase = true) ||
      apiMsg.contains("invalid api key", ignoreCase = true)
  }

  internal fun formatFriendlyFailureMessage(
    sawQuotaOrRateLimit: Boolean,
    sawAuthOrKeyError: Boolean,
    sawNetworkError: Boolean,
    lastStatus: Int,
    rawDetail: String
  ): String {
    val sanitizedDetail = rawDetail.replace(Regex("""https?://\S+"""), "").trim()
    return when {
      sawQuotaOrRateLimit ->
        "Sibil AI is temporarily busy (high traffic across available Gemini models and keys). Please try again in a few seconds, or paste a personal Gemini API key in Settings (⚙)."
      sawAuthOrKeyError ->
        "Unable to verify the Gemini API key. Tap the Settings (⚙) icon at the top right to check or add a valid Gemini API key."
      sawNetworkError ->
        "Unable to reach Sibil AI right now. Please check your internet connection and try again."
      lastStatus > 0 ->
        "Sibil AI could not complete your request right now (HTTP $lastStatus). Please try again in a moment."
      sanitizedDetail.isNotBlank() ->
        "Sibil AI could not complete your request right now. Please try again in a moment."
      else ->
        "Unable to reach Sibil AI right now. Please try again in a moment."
    }
  }

  suspend fun generateReply(
    context: Context,
    userPrompt: String,
    history: List<GeminiMessage>,
    imageBase64: String?
  ): StudyAiCallResult = withContext(Dispatchers.IO) {
    val rotatedKeys = getRotatedApiKeys(context)
    generateReplyWithExecutor(
      apiKeys = rotatedKeys,
      userPrompt = userPrompt,
      history = history,
      imageBase64 = imageBase64,
      requestExecutor = ::executeGenerateRequest
    )
  }

  internal suspend fun generateReplyWithExecutor(
    apiKeys: List<String>,
    userPrompt: String,
    history: List<GeminiMessage>,
    imageBase64: String?,
    requestExecutor: (endpoint: String, payloadJson: String) -> Pair<Int, String>
  ): StudyAiCallResult {
    if (isExplicitOrRestrictedPrompt(userPrompt)) {
      return StudyAiCallResult.Success(
        GeminiMessage(
          isUser = false,
          text = SAFETY_REFUSAL_MESSAGE,
          isSafetyBlocked = true
        )
      )
    }

    if (apiKeys.isEmpty()) {
      return StudyAiCallResult.Failure(
        "No Gemini API key configured yet. Tap the Settings (⚙) icon at the top right to add your Gemini API key."
      )
    }

    val invalidKeys = mutableSetOf<String>()
    var sawQuotaOrRateLimit = false
    var sawAuthOrKeyError = false
    var sawNetworkError = false
    var lastStatus = 0
    var lastError = ""

    for (model in MODEL_FALLBACK_ORDER) {
      for (apiKey in apiKeys) {
        if (invalidKeys.contains(apiKey)) continue

        val endpoint =
          "https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=$apiKey"
        val toolModes = listOf(
          GroundingToolMode.GOOGLE_SEARCH,
          GroundingToolMode.NONE
        )

        for (toolMode in toolModes) {
          try {
            val payloadJson = buildGenerateContentPayload(
              userPrompt = userPrompt,
              history = history,
              imageBase64 = imageBase64,
              groundingMode = toolMode
            ).toString()

            val (status, body) = requestExecutor(endpoint, payloadJson)
            if (status in 200..299) {
              val root = JSONObject(body)
              if (detectSafetyBlock(root)) {
                return StudyAiCallResult.Success(
                  GeminiMessage(
                    isUser = false,
                    text = SAFETY_REFUSAL_MESSAGE,
                    isSafetyBlocked = true
                  )
                )
              }
              val candidateText = extractCandidateText(root)
              if (candidateText.isNotBlank()) {
                val sources = parseGroundingSources(root)
                return StudyAiCallResult.Success(
                  GeminiMessage(
                    isUser = false,
                    text = candidateText,
                    sources = sources
                  )
                )
              } else {
                lastStatus = status
                lastError = "Empty response returned by $model."
                Log.w("GeminiStudyClient", lastError)
                break
              }
            } else {
              val apiMsg = extractErrorMessage(body)
              lastStatus = status
              lastError = "HTTP $status ($model): $apiMsg"
              Log.w("GeminiStudyClient", "Attempt failed: $lastError [toolMode=$toolMode]")

              if (isInvalidApiKeyError(status, apiMsg)) {
                sawAuthOrKeyError = true
                invalidKeys.add(apiKey)
                break
              }

              if (status == 429 ||
                apiMsg.contains("quota", ignoreCase = true) ||
                apiMsg.contains("RESOURCE_EXHAUSTED", ignoreCase = true)
              ) {
                sawQuotaOrRateLimit = true
              } else if (status == 403) {
                sawAuthOrKeyError = true
              }

              // If google_search grounding failed with HTTP 400, 429, or 403, immediately retry once without tools
              val shouldRetryWithoutTools = toolMode == GroundingToolMode.GOOGLE_SEARCH &&
                (status == 400 || status == 429 || status == 403)
              if (shouldRetryWithoutTools) {
                continue
              }

              // Continue smoothly to the next API key / fallback model on 429, 404, 403, 503, etc.
              break
            }
          } catch (e: Exception) {
            sawNetworkError = true
            lastError = e.localizedMessage ?: "Connection failed"
            Log.w("GeminiStudyClient", "Network exception on $model: $lastError")
            break
          }
        }
      }
    }

    return StudyAiCallResult.Failure(
      formatFriendlyFailureMessage(
        sawQuotaOrRateLimit = sawQuotaOrRateLimit,
        sawAuthOrKeyError = sawAuthOrKeyError,
        sawNetworkError = sawNetworkError,
        lastStatus = lastStatus,
        rawDetail = lastError
      )
    )
  }
}

private fun formatRelativeTimestamp(timestampMillis: Long): String {
  val diffMillis = (System.currentTimeMillis() - timestampMillis).coerceAtLeast(0L)
  val mins = diffMillis / 60_000L
  val hours = mins / 60L
  val days = hours / 24L
  return when {
    mins < 1L -> "Just now"
    mins < 60L -> "${mins}m ago"
    hours < 24L -> "${hours}h ago"
    days == 1L -> "Yesterday"
    days < 7L -> "${days}d ago"
    else -> SimpleDateFormat("MMM d", Locale.US).format(Date(timestampMillis))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GeminiStudyHelperScreen(
  viewModel: TaskViewModel,
  metrics: ResponsiveMetrics,
  onBackToExtras: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val localView = LocalView.current
  val clipboardManager = LocalClipboardManager.current
  val uriHandler = LocalUriHandler.current
  val palette = currentGlassPalette()
  val scope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  BackHandler { onBackToExtras() }

  var promptInput by rememberSaveable { mutableStateOf("") }
  var pendingAttachment by remember { mutableStateOf<StudyImageAttachment?>(null) }
  var isCompressingImage by remember { mutableStateOf(false) }
  var isGenerating by remember { mutableStateOf(false) }
  var isApiSettingsOpen by remember { mutableStateOf(false) }
  var isSavedChatsSheetOpen by remember { mutableStateOf(false) }
  var customKeyDraft by remember { mutableStateOf(GeminiStudyClient.getCustomApiKey(context)) }
  var hasReadyKey by remember { mutableStateOf(GeminiStudyClient.hasConfiguredKey(context)) }

  var savedThreads by remember {
    mutableStateOf(GeminiStudyClient.loadThreads(context))
  }
  var activeThread by remember {
    val loaded = GeminiStudyClient.loadThreads(context)
    val activeId = GeminiStudyClient.loadActiveThreadId(context)
    val initial = loaded.firstOrNull { it.id == activeId } ?: GeminiChatThread(title = "New Chat")
    mutableStateOf(initial)
  }

  LaunchedEffect(activeThread.id, activeThread.messages.size) {
    if (activeThread.messages.isNotEmpty()) {
      listState.animateScrollToItem(activeThread.messages.lastIndex)
    }
  }

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      scope.launch {
        isCompressingImage = true
        val compressed = GeminiStudyClient.prepareImageAttachment(context, uri)
        isCompressingImage = false
        if (compressed != null) {
          pendingAttachment = compressed
        } else {
          Toast.makeText(context, "Unable to process selected image.", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  fun startNewBlankChat() {
    playCardHapticSound(context, localView)
    if (activeThread.messages.isNotEmpty()) {
      savedThreads = GeminiStudyClient.upsertThread(context, activeThread)
    }
    val blank = GeminiChatThread(title = "New Chat", updatedAt = System.currentTimeMillis())
    activeThread = blank
    GeminiStudyClient.saveActiveThreadId(context, blank.id)
    pendingAttachment = null
    promptInput = ""
  }

  fun switchToThread(thread: GeminiChatThread) {
    if (activeThread.messages.isNotEmpty() && activeThread.id != thread.id) {
      savedThreads = GeminiStudyClient.upsertThread(context, activeThread)
    }
    activeThread = thread
    GeminiStudyClient.saveActiveThreadId(context, thread.id)
    pendingAttachment = null
    isSavedChatsSheetOpen = false
  }

  fun submitPrompt(rawPrompt: String) {
    val cleanPrompt = rawPrompt.trim()
    val attachmentSnapshot = pendingAttachment
    if ((cleanPrompt.isEmpty() && attachmentSnapshot == null) || isGenerating) return

    val effectivePrompt = cleanPrompt.ifEmpty {
      "Analyze this image and answer helpfully."
    }
    val userMessage = GeminiMessage(
      isUser = true,
      text = effectivePrompt,
      imageBase64 = attachmentSnapshot?.base64Jpeg
    )

    val isFirstUserTurn = activeThread.messages.none { it.isUser } || activeThread.title == "New Chat"
    val nextTitle = if (isFirstUserTurn) {
      GeminiStudyClient.deriveAutoTitle(cleanPrompt, hasImage = attachmentSnapshot != null)
    } else {
      activeThread.title
    }

    val updatedBeforeCall = activeThread.copy(
      title = nextTitle,
      updatedAt = System.currentTimeMillis(),
      messages = activeThread.messages + userMessage
    )
    activeThread = updatedBeforeCall
    savedThreads = GeminiStudyClient.upsertThread(context, updatedBeforeCall)

    promptInput = ""
    pendingAttachment = null
    isGenerating = true

    val targetThreadId = updatedBeforeCall.id
    val priorHistory = updatedBeforeCall.messages.dropLast(1)

    scope.launch {
      val result = GeminiStudyClient.generateReply(
        context = context,
        userPrompt = effectivePrompt,
        history = priorHistory,
        imageBase64 = attachmentSnapshot?.base64Jpeg
      )
      isGenerating = false

      val assistantMessage = when (result) {
        is GeminiStudyClient.StudyAiCallResult.Success -> result.message
        is GeminiStudyClient.StudyAiCallResult.Failure -> GeminiMessage(
          isUser = false,
          text = result.errorMessage
        )
      }

      val latestThread = if (activeThread.id == targetThreadId) {
        activeThread
      } else {
        savedThreads.firstOrNull { it.id == targetThreadId } ?: updatedBeforeCall
      }
      val finalized = latestThread.copy(
        updatedAt = System.currentTimeMillis(),
        messages = latestThread.messages + assistantMessage
      )
      savedThreads = GeminiStudyClient.upsertThread(context, finalized)
      if (activeThread.id == targetThreadId) {
        activeThread = finalized
      }
    }
  }

  @Composable
  fun renderMessageItem(msg: GeminiMessage) {
    val bubbleTextColor = if (palette.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    if (msg.isUser) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        Surface(
          shape = RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp),
          color = Color(0xFF0284C7),
          shadowElevation = 4.dp,
          modifier = Modifier.widthIn(max = 520.dp)
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (!msg.imageBase64.isNullOrBlank()) {
              val decodedThumb = remember(msg.id, msg.imageBase64) {
                try {
                  val bytes = Base64.decode(msg.imageBase64, Base64.DEFAULT)
                  BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                } catch (_: Exception) {
                  null
                }
              }
              if (decodedThumb != null) {
                Image(
                  bitmap = decodedThumb,
                  contentDescription = "Attached photo",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .clip(RoundedCornerShape(12.dp))
                )
              }
            }
            Text(
              text = msg.text,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = AppleSansFamily,
                fontSize = 14.5.sp,
                lineHeight = 20.5.sp
              ),
              color = Color.White
            )
          }
        }
      }
      return
    }

    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      LiquidGlassCard(
        shape = RoundedCornerShape(22.dp),
        elevation = 6.dp,
        borderStroke = if (msg.isSafetyBlocked) {
          BorderStroke(1.2.dp, Color(0xFFF59E0B).copy(alpha = 0.65f))
        } else {
          null
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("study_ai_response_card_${msg.id}")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              if (msg.isSafetyBlocked) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              } else {
                SibilAiLogo(modifier = Modifier.size(20.dp))
              }
              Text(
                text = if (msg.isSafetyBlocked) "Safety Policy" else "Sibil AI",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontFamily = AppleSansFamily,
                  fontWeight = FontWeight.Bold
                ),
                color = if (msg.isSafetyBlocked) Color(0xFFF59E0B) else palette.textPrimary
              )
            }

            if (!msg.isSafetyBlocked) {
              IconButton(
                onClick = {
                  clipboardManager.setText(AnnotatedString(msg.text))
                  Toast.makeText(context, "Copied response", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                  .size(48.dp)
                  .testTag("copy_study_ai_response_${msg.id}")
              ) {
                Icon(
                  imageVector = Icons.Filled.ContentCopy,
                  contentDescription = "Copy response",
                  tint = palette.textSecondary,
                  modifier = Modifier.size(17.dp)
                )
              }
            }
          }

          Text(
            text = msg.text,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = AppleSansFamily,
              fontSize = 14.5.sp,
              lineHeight = 21.5.sp
            ),
            color = bubbleTextColor
          )
        }
      }

      // Clickable Web Grounding Source Pills (`🌐 Source Title`)
      if (msg.sources.isNotEmpty()) {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(horizontal = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("study_ai_sources_row_${msg.id}")
        ) {
          items(msg.sources, key = { it.uri }) { source ->
            Surface(
              onClick = {
                playCardHapticSound(context, localView)
                try {
                  uriHandler.openUri(source.uri)
                } catch (_: Exception) {
                  Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
                }
              },
              shape = RoundedCornerShape(16.dp),
              color = palette.nestedCardFill,
              border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.42f)),
              modifier = Modifier
                .heightIn(min = 36.dp)
                .widthIn(max = 220.dp)
                .testTag("study_ai_source_pill")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Rounded.Public,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = source.title,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = AppleSansFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = bubbleTextColor,
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

  @Composable
  fun renderChatFeed(modifier: Modifier = Modifier) {
    if (activeThread.messages.isEmpty() && !isGenerating) {
      Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.padding(horizontal = 32.dp)
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(palette.cardFill)
              .border(BorderStroke(1.5.dp, Brush.sweepGradient(HoloRimColors)), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            SibilAiLogo(modifier = Modifier.size(28.dp))
          }
          Text(
            text = "Sibil AI",
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = AppleSansFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            ),
            color = palette.textPrimary
          )
          Text(
            text = "Ask anything — CELE formulas, code, research, or everyday questions",
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = AppleSansFamily,
              fontSize = 13.sp
            ),
            color = palette.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
      ) {
        items(activeThread.messages, key = { it.id }) { msg ->
          renderMessageItem(msg)
        }
        if (isGenerating) {
          item(key = "generating_indicator") {
            LiquidGlassCard(
              shape = RoundedCornerShape(18.dp),
              elevation = 4.dp,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.4.dp,
                  color = Color(0xFF38BDF8)
                )
                Text(
                  text = "Sibil AI is thinking...",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = AppleSansFamily,
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = palette.textSecondary
                )
              }
            }
          }
        }
      }
    }
  }

  @Composable
  fun renderInputComposer(modifier: Modifier = Modifier) {
    Column(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val attachment = pendingAttachment
      if (attachment != null || isCompressingImage) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = palette.nestedCardFill,
          border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (attachment != null) {
              Image(
                bitmap = attachment.previewBitmap,
                contentDescription = "Attached photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(10.dp))
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Photo Attached",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = palette.textPrimary
                )
                Text(
                  text = "${attachment.width}×${attachment.height}px JPEG",
                  fontSize = 11.sp,
                  fontFamily = JetBrainsMonoFamily,
                  color = palette.textSecondary
                )
              }
              IconButton(
                onClick = { pendingAttachment = null },
                modifier = Modifier.size(48.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Remove photo",
                  tint = palette.textSecondary
                )
              }
            } else {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
              Text(
                text = "Optimizing photo...",
                fontSize = 12.sp,
                color = palette.textSecondary
              )
            }
          }
        }
      }

      val inputTextColor = if (palette.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
      val placeholderTextColor = if (palette.isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = palette.cardFill,
        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.42f)),
        shadowElevation = 6.dp,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 48.dp, max = 128.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          IconButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier
              .size(48.dp)
              .testTag("study_ai_attach_photo_button")
          ) {
            Icon(
              imageVector = Icons.Filled.PhotoCamera,
              contentDescription = "Attach photo",
              tint = palette.accentIndigo
            )
          }

          BasicTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            singleLine = false,
            maxLines = 5,
            textStyle = TextStyle(
              fontFamily = AppleSansFamily,
              fontSize = 15.sp,
              lineHeight = 20.sp,
              fontWeight = FontWeight.Normal,
              color = inputTextColor
            ),
            cursorBrush = SolidColor(Color(0xFF38BDF8)),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
              onSend = { submitPrompt(promptInput) }
            ),
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 14.dp, vertical = 12.dp)
              .testTag("study_ai_prompt_input"),
            decorationBox = { innerTextField ->
              Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
              ) {
                if (promptInput.isEmpty()) {
                  Text(
                    text = "Ask Sibil AI anything...",
                    style = TextStyle(
                      fontFamily = AppleSansFamily,
                      fontSize = 14.5.sp,
                      lineHeight = 20.sp,
                      color = placeholderTextColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                innerTextField()
              }
            }
          )

          val canSend = (promptInput.isNotBlank() || pendingAttachment != null) && !isGenerating
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(
                brush = if (canSend) SibilAiAccentGradient else Brush.linearGradient(
                  listOf(palette.nestedCardFill, palette.nestedCardFill)
                )
              )
              .clickable(enabled = canSend) {
                playCardHapticSound(context, localView)
                submitPrompt(promptInput)
              }
              .testTag("study_ai_send_button"),
            contentAlignment = Alignment.Center
          ) {
            if (isGenerating) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color(0xFF38BDF8)
              )
            } else {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send prompt",
                tint = if (canSend) Color.White else palette.textSecondary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  }

  // Clean Full-Screen Gemini Assistant Canvas (Zero Top Clutter)
  Column(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
      .imePadding()
      .testTag("gemini_study_helper_screen")
  ) {
    // Flat, Borderless & Airy Top Header Bar (54dp, flush on background)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .background(Color.Transparent)
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left Cluster: Back, Saved Chats, SibilAiLogo, Title & Tappable Key Status Dot
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.weight(1f, fill = false)
      ) {
        IconButton(
          onClick = onBackToExtras,
          modifier = Modifier
            .size(38.dp)
            .testTag("study_ai_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Close Sibil AI",
            tint = palette.textPrimary
          )
        }

        IconButton(
          onClick = {
            savedThreads = GeminiStudyClient.loadThreads(context)
            isSavedChatsSheetOpen = true
          },
          modifier = Modifier
            .size(38.dp)
            .testTag("study_ai_history_button")
        ) {
          Icon(
            imageVector = Icons.Rounded.History,
            contentDescription = "Saved Chats",
            tint = palette.textPrimary
          )
        }

        SibilAiLogo(modifier = Modifier.size(22.dp))

        Text(
          text = "Sibil AI",
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = AppleSansFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp
          ),
          color = palette.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Box(
          modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .clickable {
              customKeyDraft = GeminiStudyClient.getCustomApiKey(context)
              isApiSettingsOpen = true
            }
            .testTag("study_ai_status_dot"),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(if (hasReadyKey) GoogleGreen else Color(0xFFF59E0B))
          )
        }
      }

      // Right Cluster: Subtle Tinted + New Chat Pill & Borderless Settings Button
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val newChatTextColor = if (palette.isDark) Color(0xFF8AB4F8) else Color(0xFF1A73E8)
        Surface(
          onClick = { startNewBlankChat() },
          shape = RoundedCornerShape(16.dp),
          color = GoogleBlue.copy(alpha = 0.14f),
          modifier = Modifier
            .height(32.dp)
            .testTag("study_ai_new_chat_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Rounded.Add,
              contentDescription = null,
              tint = newChatTextColor,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = "New Chat",
              style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = AppleSansFamily,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold
              ),
              color = newChatTextColor,
              maxLines = 1,
              softWrap = false
            )
          }
        }

        IconButton(
          onClick = {
            customKeyDraft = GeminiStudyClient.getCustomApiKey(context)
            isApiSettingsOpen = true
          },
          modifier = Modifier
            .size(36.dp)
            .testTag("study_ai_settings_button")
        ) {
          Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = "Gemini API Key Settings",
            tint = palette.textPrimary
          )
        }
      }
    }

    // Full-bleed Chat Canvas + Docked Composer
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      renderChatFeed(modifier = Modifier.fillMaxSize())
    }

    renderInputComposer()
  }

  // Saved Chats Bottom Sheet
  if (isSavedChatsSheetOpen) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { isSavedChatsSheetOpen = false },
      sheetState = sheetState,
      containerColor = palette.dialogSurface,
      shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 18.dp)
          .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Rounded.History,
              contentDescription = null,
              tint = Color(0xFF38BDF8)
            )
            Text(
              text = "Saved Chats",
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = AppleSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              ),
              color = palette.textPrimary
            )
          }
          Surface(
            onClick = {
              isSavedChatsSheetOpen = false
              startNewBlankChat()
            },
            shape = RoundedCornerShape(16.dp),
            color = palette.nestedCardFill,
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
            modifier = Modifier.height(36.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "New Chat",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
              )
            }
          }
        }

        if (savedThreads.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No saved conversations yet.",
              style = MaterialTheme.typography.bodyMedium,
              color = palette.textSecondary
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(savedThreads, key = { it.id }) { thread ->
              val isCurrent = thread.id == activeThread.id
              val lastSnippet = thread.messages.lastOrNull()?.text
                ?.replace('\n', ' ')
                ?.trim()
                ?.ifEmpty { "Photo message" }
                ?: "Empty conversation"
              Surface(
                onClick = { switchToThread(thread) },
                shape = RoundedCornerShape(16.dp),
                color = if (isCurrent) {
                  Color(0xFF0284C7).copy(alpha = if (palette.isDark) 0.20f else 0.10f)
                } else {
                  palette.nestedCardFill
                },
                border = BorderStroke(
                  width = if (isCurrent) 1.4.dp else 1.dp,
                  color = if (isCurrent) Color(0xFF38BDF8) else palette.nestedBorderColor
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("saved_chat_thread_${thread.id}")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(if (isCurrent) Color(0xFF38BDF8) else palette.textSecondary.copy(alpha = 0.35f))
                  )
                  Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                  ) {
                    Text(
                      text = thread.title,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AppleSansFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                      ),
                      color = palette.textPrimary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = "${formatRelativeTimestamp(thread.updatedAt)} • $lastSnippet",
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = AppleSansFamily,
                        fontSize = 12.sp
                      ),
                      color = palette.textSecondary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  IconButton(
                    onClick = {
                      val updated = GeminiStudyClient.deleteThread(context, thread.id)
                      savedThreads = updated
                      if (thread.id == activeThread.id) {
                        val fallback = updated.firstOrNull() ?: GeminiChatThread(title = "New Chat")
                        activeThread = fallback
                        GeminiStudyClient.saveActiveThreadId(context, fallback.id)
                      }
                    },
                    modifier = Modifier
                      .size(48.dp)
                      .testTag("delete_chat_thread_${thread.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.DeleteOutline,
                      contentDescription = "Delete conversation",
                      tint = palette.textSecondary
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

  // Clean 1-Field "Paste Gemini API Key" Bottom Sheet (Tappable via Status Dot or Settings)
  if (isApiSettingsOpen) {
    val connectionLabel = if (hasReadyKey) "Cloud AI Ready" else "Add API Key"
    val keySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { isApiSettingsOpen = false },
      sheetState = keySheetState,
      containerColor = palette.dialogSurface,
      shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .imePadding()
          .padding(horizontal = 20.dp)
          .padding(bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            SibilAiLogo(modifier = Modifier.size(22.dp))
            Text(
              text = "Paste Gemini API Key",
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = AppleSansFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              ),
              color = palette.textPrimary
            )
          }

          RecessedGlassPill(
            isDarkMode = palette.isDark,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (hasReadyKey) GoogleGreen else Color(0xFFF59E0B))
            )
            Text(
              text = connectionLabel,
              fontWeight = FontWeight.SemiBold,
              fontSize = 11.5.sp,
              color = palette.textPrimary
            )
          }
        }

        OutlinedTextField(
          value = customKeyDraft,
          onValueChange = { customKeyDraft = it },
          label = { Text("Gemini API Key") },
          placeholder = { Text("AIzaSy...") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("study_ai_custom_key_input")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = { isApiSettingsOpen = false },
            modifier = Modifier.heightIn(min = 48.dp)
          ) {
            Text("Cancel", color = palette.textSecondary)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              val sanitized = customKeyDraft.trim()
                .removeSurrounding("\"")
                .removeSurrounding("'")
                .trim()
              GeminiStudyClient.saveCustomApiKey(context, sanitized)
              customKeyDraft = sanitized
              hasReadyKey = GeminiStudyClient.hasConfiguredKey(context)
              isApiSettingsOpen = false
              Toast.makeText(
                context,
                if (sanitized.isNotBlank()) "Gemini API key saved" else "Using built-in Cloud AI key pool",
                Toast.LENGTH_SHORT
              ).show()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = GoogleBlue,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.heightIn(min = 48.dp)
          ) {
            Text("Save Key", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

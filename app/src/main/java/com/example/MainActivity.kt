/*
 * =================================================================================================
 * SIBIL APP — IN-PLACE APK UPDATE & ZERO DATA LOSS BUILD SPECIFICATION
 * =================================================================================================
 * To guarantee seamless in-place APK updates over existing installations without wiping
 * SharedPreferences (Tasks, Schedules, Alarms, Flashcards, and User Profiles):
 *
 * 1. Android Gradle Configuration (`app/build.gradle.kts`):
 *    android {
 *        defaultConfig {
 *            applicationId = "com.sibil.ceapp" // MUST remain fixed across all builds
 *            versionCode = 2                   // MUST be incremented with each release (1 -> 2 -> 3...)
 *            versionName = "1.0.1"
 *        }
 *    }
 *    (Ensure all APKs are signed with the same keystore so Android allows in-place upgrades.)
 *
 * 2. Android Manifest Configuration (`app/src/main/AndroidManifest.xml`):
 *    <application
 *        android:allowBackup="true"
 *        android:fullBackupContent="true"
 *        android:hasFragileUserData="true"
 *        ... >
 *    (`android:allowBackup="true"`, `android:fullBackupContent="true"`, and
 *    `android:hasFragileUserData="true"` ensure Android retains and backs up app data across
 *    updates and reinstalls.)
 * =================================================================================================
 */
package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.backup.BackupManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.view.SoundEffectConstants
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.platform.LocalView
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.CanvaDarkColorScheme
import com.example.ui.theme.CanvaLightColorScheme
import com.example.ui.theme.JetBrainsMonoFamily
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import com.example.ui.theme.Typography
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream
import java.util.zip.ZipInputStream
import kotlin.coroutines.coroutineContext
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.tan
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

// Gizmo-style Multi-Batch AI Flashcard Generator & Sibil Release Configuration
private const val DEFAULT_GEMINI_API_KEY = "AIzaSyANkLy-HqqeUQzPeAkAnN2m5xWx5NaDgew"
const val SIBIL_APP_VERSION_TAG = "Sibil v1.0.1 (Build 2)"

private const val DEFAULT_FALLBACK_LATITUDE = 10.51
private const val DEFAULT_FALLBACK_LONGITUDE = 124.03

// ============================================================================
// 1. UNIFIED LIQUID GLASS THEME ENGINE (LIGHT & DARK MODE PARITY)
// ============================================================================
private val LiquidFrostedPearl = Color(0xFFDCE4F2)
private val LiquidWarmIvory = Color(0xFFE6ECFA)
private val LiquidSoftPeriwinkle = Color(0xFFEAE6F8)
private val LiquidLavenderMist = Color(0xFFEDE9F8)

private val LiquidCyanGlow = Color(0xFF06B6D4)
private val LiquidRoyalPeriwinkle = Color(0xFF6366F1)
private val LiquidDeepIndigo = Color(0xFF4F46E5)
private val LiquidIndigoCore = Color(0xFF4338CA)
private val LiquidElectricViolet = Color(0xFF7C3AED)
private val LiquidInactiveCharcoal = Color(0xFF475569)

// Legacy fallback tokens kept for static non-composable references
private val CreamCardColor = Color.White.copy(alpha = 0.66f)
private val IvoryInnerCardColor = Color(0xFFEEF2FF).copy(alpha = 0.75f)
private val WarmCreamTrackColor = Color(0xFFCBD5E1).copy(alpha = 0.75f)

private val LiquidOrbBrush: Brush = Brush.linearGradient(
  colors = listOf(
    LiquidRoyalPeriwinkle,
    LiquidDeepIndigo,
    LiquidIndigoCore
  )
)

private val LiquidCyanVioletGradient: Brush = Brush.linearGradient(
  colors = listOf(
    LiquidCyanGlow,
    LiquidRoyalPeriwinkle,
    LiquidElectricViolet
  )
)

private val LiquidVioletChipGradient: Brush = Brush.horizontalGradient(
  colors = listOf(
    LiquidRoyalPeriwinkle,
    LiquidElectricViolet
  )
)

/**
 * Centralized theme token data class `GlassPalette` provided via `CompositionLocalProvider`
 * so NO screen ever uses hardcoded white/black colors that break across Light and Dark modes.
 */
data class GlassPalette(
  val isDark: Boolean,
  val canvasColors: List<Color>,
  val cardFill: Color,
  val cardTopSheen: Color,
  val cardBottomSheen: Color,
  val nestedCardFill: Color,
  val nestedBorderColor: Color,
  val glassBorderBrush: Brush,
  val glassBorderWidth: Dp,
  val dockFill: Color,
  val dockBorderColor: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val trackColor: Color,
  val accentIndigo: Color,
  val accentRed: Color,
  val dialogSurface: Color,
  val shadowAmbient: Color,
  val shadowSpot: Color,
  val stackTopFill: Color,
  val stackMidFill: Color,
  val stackMidBorder: Color,
  val stackBottomFill: Color,
  val checkboxUncheckedFill: Color,
  val checkboxUncheckedBorder: Color
) {
  val glassBorder: BorderStroke
    get() = BorderStroke(glassBorderWidth, glassBorderBrush)

  val nestedBorder: BorderStroke
    get() = BorderStroke(1.dp, nestedBorderColor)

  val cardBorder: Color
    get() = nestedBorderColor

  val nestedCardBorder: Color
    get() = nestedBorderColor

  val dividerColor: Color
    get() = nestedBorderColor

  val chipUnselectedFill: Color
    get() = nestedCardFill

  val chipUnselectedBorder: Color
    get() = nestedBorderColor

  val textMuted: Color
    get() = textSecondary.copy(alpha = 0.8f)

  val inputFill: Color
    get() = nestedCardFill

  val inputUnfocusedBorder: Color
    get() = nestedBorderColor

  val progressTrack: Color
    get() = trackColor

  val cardAmbientShadow: Color
    get() = shadowAmbient

  val cardSpotShadow: Color
    get() = shadowSpot
}

val DarkGlassPalette = GlassPalette(
  isDark = true,
  canvasColors = listOf(
    Color(0xFF070B19),
    Color(0xFF0F172A),
    Color(0xFF1E1B4B),
    Color(0xFF070B19)
  ),
  cardFill = Color(0xFF1E293B),
  cardTopSheen = Color(0xFF1E293B),
  cardBottomSheen = Color(0xFF0F172A),
  nestedCardFill = Color(0xFF26334D),
  nestedBorderColor = Color.White.copy(alpha = 0.16f),
  glassBorderBrush = Brush.verticalGradient(
    colors = listOf(
      Color.White.copy(alpha = 0.22f),
      Color(0xFF818CF8).copy(alpha = 0.10f)
    )
  ),
  glassBorderWidth = 1.2.dp,
  dockFill = Color(0xFF0F172A),
  dockBorderColor = Color.White.copy(alpha = 0.22f),
  textPrimary = Color(0xFFF8FAFC),
  textSecondary = Color(0xFF94A3B8),
  trackColor = Color(0xFF334155),
  accentIndigo = Color(0xFF818CF8),
  accentRed = Color(0xFFF87171),
  dialogSurface = Color(0xFF0B1120),
  shadowAmbient = Color.Black,
  shadowSpot = Color(0x334F46E5),
  stackTopFill = Color(0xFF1E293B),
  stackMidFill = Color(0xFF162032),
  stackMidBorder = Color.White.copy(alpha = 0.14f),
  stackBottomFill = Color(0xFF0F172A),
  checkboxUncheckedFill = Color(0xFF1E293B),
  checkboxUncheckedBorder = Color(0xFF818CF8)
)

val LightGlassPalette = GlassPalette(
  isDark = false,
  canvasColors = listOf(
    Color(0xFFDCE4F2),
    Color(0xFFE6ECFA),
    Color(0xFFEAE6F8),
    Color(0xFFDCE4F2)
  ),
  cardFill = Color(0xFFFFFFFF),
  cardTopSheen = Color(0xFFFFFFFF),
  cardBottomSheen = Color(0xFFF4F7FB),
  nestedCardFill = Color(0xFFEEF2FF),
  nestedBorderColor = Color(0xFFD8E0EE),
  glassBorderBrush = Brush.verticalGradient(
    colors = listOf(
      Color(0xFFFFFFFF),
      Color(0xFFD8E0EE)
    )
  ),
  glassBorderWidth = 1.2.dp,
  dockFill = Color(0xFFF8FAFC),
  dockBorderColor = Color(0xFFCBD5E1),
  textPrimary = Color(0xFF0F172A),
  textSecondary = Color(0xFF475569),
  trackColor = Color(0xFFCBD5E1),
  accentIndigo = Color(0xFF4F46E5),
  accentRed = Color(0xFFEF4444),
  dialogSurface = Color(0xFFF4F7FB),
  shadowAmbient = Color(0x140F172A),
  shadowSpot = Color(0x183730A3),
  stackTopFill = Color(0xFFFFFFFF),
  stackMidFill = Color(0xFFE2E8F0),
  stackMidBorder = Color(0xFFCBD5E1),
  stackBottomFill = Color(0xFFCBD5E1),
  checkboxUncheckedFill = Color(0xFFFFFFFF),
  checkboxUncheckedBorder = Color(0xFF6366F1)
)

val LocalGlassPalette = compositionLocalOf { LightGlassPalette }

@Composable
fun currentGlassPalette(): GlassPalette {
  val local = LocalGlassPalette.current
  val isThemeDark = MaterialTheme.colorScheme.background.red < 0.5f
  return if (local.isDark == isThemeDark) {
    local
  } else if (isThemeDark) {
    DarkGlassPalette
  } else {
    LightGlassPalette
  }
}

@Composable
fun rememberAnimatedGlassPalette(isDark: Boolean): GlassPalette {
  val target = if (isDark) DarkGlassPalette else LightGlassPalette
  val cardFill by animateColorAsState(target.cardFill, tween(300), label = "glass_card_fill")
  val cardTopSheen by animateColorAsState(target.cardTopSheen, tween(300), label = "glass_top_sheen")
  val cardBottomSheen by animateColorAsState(target.cardBottomSheen, tween(300), label = "glass_bottom_sheen")
  val nestedCardFill by animateColorAsState(target.nestedCardFill, tween(300), label = "glass_nested_fill")
  val nestedBorderColor by animateColorAsState(target.nestedBorderColor, tween(300), label = "glass_nested_border")
  val dockFill by animateColorAsState(target.dockFill, tween(300), label = "glass_dock_fill")
  val dockBorderColor by animateColorAsState(target.dockBorderColor, tween(300), label = "glass_dock_border")
  val textPrimary by animateColorAsState(target.textPrimary, tween(300), label = "glass_text_primary")
  val textSecondary by animateColorAsState(target.textSecondary, tween(300), label = "glass_text_secondary")
  val trackColor by animateColorAsState(target.trackColor, tween(300), label = "glass_track_color")
  val accentIndigo by animateColorAsState(target.accentIndigo, tween(300), label = "glass_accent_indigo")
  val accentRed by animateColorAsState(target.accentRed, tween(300), label = "glass_accent_red")
  val dialogSurface by animateColorAsState(target.dialogSurface, tween(300), label = "glass_dialog_surface")
  val stackTopFill by animateColorAsState(target.stackTopFill, tween(300), label = "glass_stack_top")
  val stackMidFill by animateColorAsState(target.stackMidFill, tween(300), label = "glass_stack_mid")
  val stackMidBorder by animateColorAsState(target.stackMidBorder, tween(300), label = "glass_stack_mid_border")
  val stackBottomFill by animateColorAsState(target.stackBottomFill, tween(300), label = "glass_stack_bottom")

  return target.copy(
    cardFill = cardFill,
    cardTopSheen = cardTopSheen,
    cardBottomSheen = cardBottomSheen,
    nestedCardFill = nestedCardFill,
    nestedBorderColor = nestedBorderColor,
    dockFill = dockFill,
    dockBorderColor = dockBorderColor,
    textPrimary = textPrimary,
    textSecondary = textSecondary,
    trackColor = trackColor,
    accentIndigo = accentIndigo,
    accentRed = accentRed,
    dialogSurface = dialogSurface,
    stackTopFill = stackTopFill,
    stackMidFill = stackMidFill,
    stackMidBorder = stackMidBorder,
    stackBottomFill = stackBottomFill
  )
}

@Composable
fun sibilOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedTextColor = currentGlassPalette().textPrimary,
  unfocusedTextColor = currentGlassPalette().textPrimary,
  focusedLabelColor = currentGlassPalette().accentIndigo,
  unfocusedLabelColor = currentGlassPalette().textSecondary,
  focusedPlaceholderColor = currentGlassPalette().textSecondary.copy(alpha = 0.75f),
  unfocusedPlaceholderColor = currentGlassPalette().textSecondary.copy(alpha = 0.75f),
  focusedBorderColor = currentGlassPalette().accentIndigo,
  unfocusedBorderColor = currentGlassPalette().nestedBorderColor,
  cursorColor = currentGlassPalette().accentIndigo,
  focusedContainerColor = currentGlassPalette().nestedCardFill,
  unfocusedContainerColor = currentGlassPalette().nestedCardFill
)

@Composable
fun isLiquidLightMode(): Boolean {
  return !currentGlassPalette().isDark
}

@Composable
fun liquidGlassFillColor(alpha: Float = 1.0f): Color {
  return currentGlassPalette().cardFill
}

@Composable
fun liquidGlassInnerPodColor(alpha: Float = 1.0f): Color {
  return currentGlassPalette().nestedCardFill
}

@Composable
fun liquidGlassSpecularBorder(
  width: Dp = 1.2.dp,
  topAlpha: Float = 0.95f,
  midAlpha: Float = 0.45f,
  bottomTint: Color = Color(0xFFD8E0EE)
): BorderStroke {
  val palette = currentGlassPalette()
  return BorderStroke(
    width = width,
    brush = palette.glassBorderBrush
  )
}

/**
 * Ambient, multi-stop Fluid Background Mesh canvas on the root Scaffold:
 * - Light Mode: Rich architectural daylight mesh (`#DCE4F2` -> `#E6ECFA` -> `#EAE6F8`) with visible
 *   diffused ambient color orbs (Periwinkle `#6366F1` at 16% alpha, Sky Cyan `#0EA5E9` at 14% alpha,
 *   Soft Violet `#A855F7` at 12% alpha) so frosted white glass cards immediately pop with 3D depth.
 * - Dark Mode: Deep midnight/obsidian mesh (`#070B19` -> `#0F172A` -> `#1E1B4B`) with soft ambient
 *   glowing orbs of indigo (`#4F46E5` at 18% alpha) and cyan (`#06B6D4` at 12% alpha).
 */
@Composable
fun LiquidGlassBackgroundMesh(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  val palette = currentGlassPalette()
  val meshBrush = Brush.linearGradient(
    colors = palette.canvasColors,
    start = Offset(0f, 0f),
    end = Offset(1400f, 2400f)
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(brush = meshBrush)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      if (!palette.isDark) {
        // Light Mode Orb 1: Periwinkle (#6366F1 at 16% alpha)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF6366F1).copy(alpha = 0.16f),
              Color(0xFF6366F1).copy(alpha = 0.06f),
              Color.Transparent
            ),
            center = Offset(w * 0.86f, h * 0.14f),
            radius = w * 0.72f
          ),
          radius = w * 0.72f,
          center = Offset(w * 0.86f, h * 0.14f)
        )

        // Light Mode Orb 2: Soft Violet (#A855F7 at 12% alpha)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFA855F7).copy(alpha = 0.12f),
              Color(0xFFA855F7).copy(alpha = 0.05f),
              Color.Transparent
            ),
            center = Offset(w * 0.14f, h * 0.48f),
            radius = w * 0.75f
          ),
          radius = w * 0.75f,
          center = Offset(w * 0.14f, h * 0.48f)
        )

        // Light Mode Orb 3: Sky Cyan (#0EA5E9 at 14% alpha)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF0EA5E9).copy(alpha = 0.14f),
              Color(0xFF0EA5E9).copy(alpha = 0.05f),
              Color.Transparent
            ),
            center = Offset(w * 0.80f, h * 0.86f),
            radius = w * 0.68f
          ),
          radius = w * 0.68f,
          center = Offset(w * 0.80f, h * 0.86f)
        )
      } else {
        // Dark Mode Orb 1: Indigo (#4F46E5 at 18% alpha)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF4F46E5).copy(alpha = 0.18f),
              Color(0xFF4F46E5).copy(alpha = 0.05f),
              Color.Transparent
            ),
            center = Offset(w * 0.86f, h * 0.15f),
            radius = w * 0.72f
          ),
          radius = w * 0.72f,
          center = Offset(w * 0.86f, h * 0.15f)
        )

        // Dark Mode Orb 2: Indigo/Violet mid bloom
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF4F46E5).copy(alpha = 0.14f),
              Color.Transparent
            ),
            center = Offset(w * 0.14f, h * 0.50f),
            radius = w * 0.70f
          ),
          radius = w * 0.70f,
          center = Offset(w * 0.14f, h * 0.50f)
        )

        // Dark Mode Orb 3: Cyan (#06B6D4 at 12% alpha)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF06B6D4).copy(alpha = 0.12f),
              Color.Transparent
            ),
            center = Offset(w * 0.78f, h * 0.88f),
            radius = w * 0.66f
          ),
          radius = w * 0.66f,
          center = Offset(w * 0.78f, h * 0.88f)
        )
      }
    }

    content()
  }
}

/**
 * Reusable `LiquidGlassCard` composable used for primary cards, dialogs, and panels across Sibil.
 * Uses 100% opaque multi-stop vertical frost gradients so Android's RenderNode shadow never bleeds
 * through the interior.
 */
@Composable
fun LiquidGlassCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 24.dp,
  shape: Shape = RoundedCornerShape(cornerRadius),
  elevation: Dp = 6.dp,
  fillAlpha: Float = 1.0f,
  borderStroke: BorderStroke? = null,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val palette = currentGlassPalette()
  val resolvedBorder = borderStroke ?: palette.glassBorder
  val resolvedElevation = if (palette.isDark && elevation == 6.dp) 8.dp else elevation
  val sheenBrush = remember(palette.isDark, palette.cardTopSheen, palette.cardBottomSheen) {
    Brush.verticalGradient(
      colors = listOf(
        palette.cardTopSheen,
        palette.cardBottomSheen
      )
    )
  }

  val cardModifier = modifier
    .shadow(
      elevation = resolvedElevation,
      shape = shape,
      ambientColor = palette.shadowAmbient,
      spotColor = palette.shadowSpot
    )
    .clip(shape)
    .background(brush = sheenBrush, shape = shape)
    .border(border = resolvedBorder, shape = shape)
    .let { mod ->
      if (onClick != null) {
        mod.clickable(onClick = onClick)
      } else {
        mod
      }
    }

  Box(
    modifier = cardModifier,
    content = content
  )
}

/**
 * Reusable `LiquidGlassContainer` for inner pods, search bars, chips, and status tiles.
 * Uses 100% opaque `palette.nestedCardFill` (`#EEF2FF` Light / `#26334D` Dark) and crisp `palette.nestedBorder`.
 */
@Composable
fun LiquidGlassContainer(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 18.dp,
  shape: Shape = RoundedCornerShape(cornerRadius),
  elevation: Dp = 0.dp,
  fillAlpha: Float = 1.0f,
  borderStroke: BorderStroke? = null,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val palette = currentGlassPalette()
  val resolvedBorder = borderStroke ?: palette.nestedBorder
  val fill = palette.nestedCardFill

  val podModifier = modifier
    .clip(shape)
    .background(color = fill, shape = shape)
    .border(border = resolvedBorder, shape = shape)
    .let { mod ->
      if (onClick != null) {
        mod.clickable(onClick = onClick)
      } else {
        mod
      }
    }

  Box(
    modifier = podModifier,
    content = content
  )
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    enableEdgeToEdge()
    createClockNotificationChannel(this)
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
 * Smart Flashcard Data Models for Active-Recall Civil Engineering Study.
 */
data class FlashcardItem(
  val id: Int,
  val question: String,
  val answer: String,
  val category: String,
  var masteryLevel: String = "New",
  var isMastered: Boolean = masteryLevel.equals("Good", ignoreCase = true)
)

data class FlashcardDeck(
  val id: Int,
  val title: String,
  val subjectTag: String,
  val cards: List<FlashcardItem>,
  val masteredCardIds: List<Int> = cards.filter { it.isMastered || it.masteryLevel.equals("Good", ignoreCase = true) }.map { it.id }
)

/**
 * Custom exception carrying HTTP status code and detailed Gemini API or network error message.
 */
class GeminiBatchExtractionException(
  val statusCode: Int,
  override val message: String
) : Exception(message)

/**
 * Gizmo-Style Multi-Batch AI Flashcard Generator (`GizmoBatchFlashcardEngine`):
 * - Uses `https://generativelanguage.googleapis.com/v1beta/models/$activeModel:generateContent?key=$activeApiKey`
 *   with `primaryModel` and automatic model discovery fallback via `resolveWorkingGeminiModel(apiKey)`.
 * - Enforces `"generationConfig": { "responseMimeType": "application/json", "temperature": 0.1, "maxOutputTokens": 8192 }`
 * - Processes PDFs of ANY length in 2-page visual batches using `android.graphics.pdf.PdfRenderer`,
 *   rendering each page onto a max-1440px white-backed `Bitmap`, compressing to 82% JPEG Base64,
 *   and recycling the `Bitmap` immediately.
 * - Processes DOCX (`word/document.xml` via `ZipInputStream`), TXT, and Pasted Notes of ANY length
 *   in 4,000-character chunks split along paragraph boundaries, with automatic offline local regex Q&A fallback.
 * - Reports real-time progress (`currentBatch`, `totalBatches`, `statusLabel`, `accumulatedCards`, `latestQuestion`, `latestAnswer`)
 *   after every batch so the user can monitor live generation or tap "Save & Stop Here" at any time.
 */
object GizmoBatchFlashcardEngine {
  val primaryModel: String = listOf("gemini", "3.8", "flash").joinToString("-")

  val fallbackModels: List<String> = listOf(
    listOf("gemini", "3.8", "flash").joinToString("-"),
    listOf("gemini", "3.6", "flash").joinToString("-"),
    listOf("gemini", "3.5", "flash", "lite").joinToString("-")
  )

  @Volatile
  private var cachedWorkingModelName: String = primaryModel

  @Volatile
  private var hasVerifiedWorkingModel: Boolean = false

  @Volatile
  private var fallbackStepIndex: Int = 0

  val SYSTEM_PROMPT_INSTRUCTION = """
You are an exhaustive Question-and-Answer extractor and flashcard generator.
Read all content on these pages and extract EVERY question and answer into flashcards:
1. DIRECT Q&A RECOGNITION (HIGHEST PRIORITY): If the document contains existing questions and answers (numbered questions, exam reviewers, multiple-choice items with answer keys, Q&A pairs, tables, problem sets, or study guides), extract EVERY SINGLE question and its exact corresponding answer. Do NOT skip, summarize, or limit the number of cards.
2. COMPLETE CONCEPT EXTRACTION: For paragraphs, definitions, formulas, or lecture notes not written as questions, convert every key definition, formula, and provision into a clear Question and Answer pair.
3. Respond strictly with a valid JSON array of objects: [{"question": "...", "answer": "..."}].
  """.trimIndent()

  private const val PAGES_PER_PDF_BATCH = 2
  private const val MAX_TEXT_CHUNK_CHARS = 4000

  /**
   * Resilient helper function that starts with `primaryModel` and, if an HTTP 404
   * occurs, queries `https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey` or steps
   * through `fallbackModels` to pick the active Flash model without failing.
   */
  fun resolveWorkingGeminiModel(apiKey: String, forceRefresh: Boolean = false): String {
    if (!forceRefresh && hasVerifiedWorkingModel && cachedWorkingModelName.isNotBlank()) {
      return cachedWorkingModelName
    }
    if (!forceRefresh) {
      return cachedWorkingModelName.ifBlank { primaryModel }
    }

    val cleanKey = apiKey.trim().ifEmpty { DEFAULT_GEMINI_API_KEY }
    val modelsUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=$cleanKey"
    var connection: HttpURLConnection? = null

    try {
      connection = (URL(modelsUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 30_000
        readTimeout = 30_000
        doInput = true
      }

      val code = connection.responseCode
      if (code in 200..299) {
        val body = BufferedReader(
          InputStreamReader(connection.inputStream, Charsets.UTF_8)
        ).use { it.readText() }

        val root = JSONObject(body)
        val modelsArray = root.optJSONArray("models")
        if (modelsArray != null && modelsArray.length() > 0) {
          val availableFlashModels = mutableListOf<String>()
          for (i in 0 until modelsArray.length()) {
            val modelObj = modelsArray.optJSONObject(i) ?: continue
            val rawName = modelObj.optString("name", "").removePrefix("models/").trim()
            if (!rawName.contains("flash", ignoreCase = true)) continue

            val methods = modelObj.optJSONArray("supportedGenerationMethods")
            var supportsGenerateContent = false
            if (methods != null) {
              for (m in 0 until methods.length()) {
                if (methods.optString(m).equals("generateContent", ignoreCase = true)) {
                  supportsGenerateContent = true
                  break
                }
              }
            } else {
              supportsGenerateContent = true
            }

            if (supportsGenerateContent && rawName.isNotBlank()) {
              availableFlashModels.add(rawName)
            }
          }

          val matchedPreferred = fallbackModels.firstOrNull { pref ->
            availableFlashModels.any { it.equals(pref, ignoreCase = true) }
          } ?: fallbackModels.firstOrNull { pref ->
            availableFlashModels.any { it.startsWith(pref, ignoreCase = true) }
          } ?: availableFlashModels.firstOrNull {
            !it.contains("tts", ignoreCase = true) &&
              !it.contains("image", ignoreCase = true) &&
              !it.contains("audio", ignoreCase = true)
          } ?: availableFlashModels.firstOrNull()

          if (!matchedPreferred.isNullOrBlank()) {
            cachedWorkingModelName = matchedPreferred
            hasVerifiedWorkingModel = true
            return cachedWorkingModelName
          }
        }
      }
    } catch (_: Exception) {
    } finally {
      connection?.disconnect()
    }

    // Step through fallbackModels if list models call could not parse
    fallbackStepIndex = (fallbackStepIndex + 1) % fallbackModels.size
    cachedWorkingModelName = fallbackModels[fallbackStepIndex]
    hasVerifiedWorkingModel = true
    return cachedWorkingModelName
  }

  suspend fun processDocumentUriInBatches(
    context: Context,
    uri: Uri,
    fileName: String,
    activeApiKey: String,
    defaultCategory: String,
    onProgressUpdate: (
      currentBatch: Int,
      totalBatches: Int,
      statusLabel: String,
      accumulatedCards: List<FlashcardItem>,
      latestQuestion: String,
      latestAnswer: String
    ) -> Unit
  ): List<FlashcardItem> = withContext(Dispatchers.IO) {
    val cleanKey = activeApiKey.trim().ifEmpty { DEFAULT_GEMINI_API_KEY }
    val mimeType = try {
      context.contentResolver.getType(uri).orEmpty().lowercase(Locale.US)
    } catch (_: Exception) {
      ""
    }
    val lowerName = fileName.lowercase(Locale.US)

    val isPdf = lowerName.endsWith(".pdf") || mimeType.contains("pdf")
    if (isPdf) {
      processPdfInPageBatches(
        context = context,
        uri = uri,
        activeApiKey = cleanKey,
        defaultCategory = defaultCategory,
        onProgressUpdate = onProgressUpdate
      )
    } else {
      val extractedText = when {
        lowerName.endsWith(".docx") || mimeType.contains("wordprocessingml") -> {
          OfflineDeckExtractor.extractTextFromDocx(context, uri)
        }
        lowerName.endsWith(".txt") || mimeType.startsWith("text/") -> {
          OfflineDeckExtractor.extractTextFromTxt(context, uri)
        }
        else -> {
          val docxText = OfflineDeckExtractor.extractTextFromDocx(context, uri)
          if (docxText.isNotBlank()) docxText else OfflineDeckExtractor.extractTextFromTxt(context, uri)
        }
      }
      if (extractedText.isBlank()) {
        throw GeminiBatchExtractionException(
          statusCode = -1,
          message = "No readable text found in \"$fileName\". Ensure the DOCX or TXT file is not empty."
        )
      }
      processTextInChunks(
        rawText = extractedText,
        activeApiKey = cleanKey,
        defaultCategory = defaultCategory,
        onProgressUpdate = onProgressUpdate
      )
    }
  }

  /**
   * Processes a PDF of ANY length using `PdfRenderer` in 2-page JPEG batches (max dimension 1440px, 82% JPEG quality).
   * Retries any HTTP 429 rate-limit response once after a 4-second delay so no pages are lost.
   */
  suspend fun processPdfInPageBatches(
    context: Context,
    uri: Uri,
    activeApiKey: String,
    defaultCategory: String,
    onProgressUpdate: (
      currentBatch: Int,
      totalBatches: Int,
      statusLabel: String,
      accumulatedCards: List<FlashcardItem>,
      latestQuestion: String,
      latestAnswer: String
    ) -> Unit
  ): List<FlashcardItem> = withContext(Dispatchers.IO) {
    val tempFile = File(context.cacheDir, "temp_import.pdf")
    var pfd: ParcelFileDescriptor? = null
    var pdfRenderer: PdfRenderer? = null
    val accumulatedCards = mutableListOf<FlashcardItem>()
    val seenQuestions = mutableSetOf<String>()
    var latestQuestion = ""
    var latestAnswer = ""

    try {
      context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(tempFile).use { output ->
          input.copyTo(output)
          output.flush()
        }
      } ?: throw GeminiBatchExtractionException(
        statusCode = -1,
        message = "Unable to open selected PDF file stream."
      )

      pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
      pdfRenderer = PdfRenderer(pfd)
      val totalPages = pdfRenderer.pageCount
      if (totalPages <= 0) {
        throw GeminiBatchExtractionException(
          statusCode = -1,
          message = "The selected PDF has 0 pages."
        )
      }

      val totalBatches = (totalPages + PAGES_PER_PDF_BATCH - 1) / PAGES_PER_PDF_BATCH

      for (batchIdx in 0 until totalBatches) {
        coroutineContext.ensureActive()

        val startPageIdx = batchIdx * PAGES_PER_PDF_BATCH
        val endPageIdx = minOf(startPageIdx + PAGES_PER_PDF_BATCH - 1, totalPages - 1)
        val startPageHuman = startPageIdx + 1
        val endPageHuman = endPageIdx + 1
        val scanningRangeStatus =
          "Scanning Pages $startPageHuman–$endPageHuman of $totalPages • ${accumulatedCards.size} Cards Extracted"

        onProgressUpdate(
          batchIdx + 1,
          totalBatches,
          scanningRangeStatus,
          accumulatedCards.toList(),
          latestQuestion,
          latestAnswer
        )

        val base64PageImages = mutableListOf<String>()
        for (pageIndex in startPageIdx..endPageIdx) {
          coroutineContext.ensureActive()
          val page = pdfRenderer.openPage(pageIndex)
          val pageW = page.width.coerceAtLeast(1)
          val pageH = page.height.coerceAtLeast(1)
          val maxDimension = 1440f
          val scale = maxDimension / maxOf(pageW, pageH).toFloat()
          val targetW = (pageW * scale).roundToInt().coerceAtLeast(1)
          val targetH = (pageH * scale).roundToInt().coerceAtLeast(1)

          val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
          try {
            // Fill white background first so transparent PDFs render crisply
            android.graphics.Canvas(bitmap).drawColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
          } finally {
            page.close()
          }

          val jpegBytes = ByteArrayOutputStream().use { baos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 82, baos)
            baos.toByteArray()
          }
          // Recycle immediately to keep RAM usage tiny
          bitmap.recycle()

          val encodedBase64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
          base64PageImages.add(encodedBase64)
        }

        coroutineContext.ensureActive()
        val batchCards = try {
          requestGeminiFlashcardsForPdfBatch(
            activeApiKey = activeApiKey,
            base64JpegPages = base64PageImages,
            startPage = startPageHuman,
            endPage = endPageHuman,
            totalPages = totalPages,
            defaultCategory = defaultCategory
          )
        } catch (e: GeminiBatchExtractionException) {
          if (e.statusCode == 429) {
            delay(4000L)
            coroutineContext.ensureActive()
            requestGeminiFlashcardsForPdfBatch(
              activeApiKey = activeApiKey,
              base64JpegPages = base64PageImages,
              startPage = startPageHuman,
              endPage = endPageHuman,
              totalPages = totalPages,
              defaultCategory = defaultCategory
            )
          } else if (e.statusCode == -1) {
            // Offline fallback for PDF if device has no network connection
            val offlinePdfText = OfflineDeckExtractor.extractTextFromPdf(context, uri)
            if (offlinePdfText.isNotBlank()) {
              val fallbackCards = generateCardsFromText(offlinePdfText, defaultCategory)
              for (card in fallbackCards) {
                val normQ = card.question.lowercase(Locale.US).trim()
                if (normQ.isNotEmpty() && seenQuestions.add(normQ)) {
                  val assignedCard = card.copy(id = accumulatedCards.size + 1)
                  accumulatedCards.add(assignedCard)
                  latestQuestion = assignedCard.question
                  latestAnswer = assignedCard.answer
                }
              }
              onProgressUpdate(
                totalBatches,
                totalBatches,
                "Scanning Pages 1–$totalPages of $totalPages • ${accumulatedCards.size} Cards Extracted",
                accumulatedCards.toList(),
                latestQuestion,
                latestAnswer
              )
              break
            } else {
              throw e
            }
            emptyList()
          } else {
            throw e
          }
        }

        for (card in batchCards) {
          val normQ = card.question.lowercase(Locale.US).trim()
          if (normQ.isNotEmpty() && seenQuestions.add(normQ)) {
            val assignedCard = card.copy(id = accumulatedCards.size + 1)
            accumulatedCards.add(assignedCard)
            latestQuestion = assignedCard.question
            latestAnswer = assignedCard.answer
          }
        }

        val updatedStatus =
          "Scanning Pages $startPageHuman–$endPageHuman of $totalPages • ${accumulatedCards.size} Cards Extracted"

        onProgressUpdate(
          batchIdx + 1,
          totalBatches,
          updatedStatus,
          accumulatedCards.toList(),
          latestQuestion,
          latestAnswer
        )
      }

      if (accumulatedCards.isEmpty()) {
        val offlineText = OfflineDeckExtractor.extractTextFromPdf(context, uri)
        if (offlineText.isNotBlank()) {
          val fallbackCards = generateCardsFromText(offlineText, defaultCategory)
          for (card in fallbackCards) {
            val normQ = card.question.lowercase(Locale.US).trim()
            if (normQ.isNotEmpty() && seenQuestions.add(normQ)) {
              val assignedCard = card.copy(id = accumulatedCards.size + 1)
              accumulatedCards.add(assignedCard)
              latestQuestion = assignedCard.question
              latestAnswer = assignedCard.answer
            }
          }
        }
      }

      accumulatedCards
    } finally {
      try {
        pdfRenderer?.close()
      } catch (_: Exception) {
      }
      try {
        pfd?.close()
      } catch (_: Exception) {
      }
      try {
        if (tempFile.exists()) {
          tempFile.delete()
        }
      } catch (_: Exception) {
      }
    }
  }

  /**
   * Splits DOCX, TXT, or pasted text into 4,000-character chunks along paragraph boundaries
   * and sends each chunk sequentially to `primaryModel` (with automatic model fallback, 429 retry,
   * and offline local regex Q&A fallback if the device is completely offline).
   */
  suspend fun processTextInChunks(
    rawText: String,
    activeApiKey: String,
    defaultCategory: String,
    onProgressUpdate: (
      currentBatch: Int,
      totalBatches: Int,
      statusLabel: String,
      accumulatedCards: List<FlashcardItem>,
      latestQuestion: String,
      latestAnswer: String
    ) -> Unit
  ): List<FlashcardItem> = withContext(Dispatchers.IO) {
    val chunks = splitTextIntoParagraphChunks(rawText, MAX_TEXT_CHUNK_CHARS)
    if (chunks.isEmpty()) {
      throw GeminiBatchExtractionException(
        statusCode = -1,
        message = "No text content provided to generate flashcards."
      )
    }

    val totalBatches = chunks.size
    val accumulatedCards = mutableListOf<FlashcardItem>()
    val seenQuestions = mutableSetOf<String>()
    var latestQuestion = ""
    var latestAnswer = ""

    for ((batchIdx, chunkText) in chunks.withIndex()) {
      coroutineContext.ensureActive()

      val pageStart = batchIdx + 1
      val statusText =
        "Scanning Pages $pageStart–$pageStart of $totalBatches • ${accumulatedCards.size} Cards Extracted"

      onProgressUpdate(
        batchIdx + 1,
        totalBatches,
        statusText,
        accumulatedCards.toList(),
        latestQuestion,
        latestAnswer
      )

      val batchCards = try {
        requestGeminiFlashcardsForTextChunk(
          activeApiKey = activeApiKey,
          textChunk = chunkText,
          batchNumber = batchIdx + 1,
          totalBatches = totalBatches,
          defaultCategory = defaultCategory
        )
      } catch (e: GeminiBatchExtractionException) {
        if (e.statusCode == 429) {
          delay(4000L)
          coroutineContext.ensureActive()
          try {
            requestGeminiFlashcardsForTextChunk(
              activeApiKey = activeApiKey,
              textChunk = chunkText,
              batchNumber = batchIdx + 1,
              totalBatches = totalBatches,
              defaultCategory = defaultCategory
            )
          } catch (retryEx: GeminiBatchExtractionException) {
            if (retryEx.statusCode == -1) {
              generateCardsFromText(chunkText, defaultCategory)
            } else {
              throw retryEx
            }
          }
        } else if (e.statusCode == -1) {
          // Offline Local Q&A Fallback: If device is completely offline, parse Q&A locally with regex parser
          generateCardsFromText(chunkText, defaultCategory)
        } else {
          throw e
        }
      }

      for (card in batchCards) {
        val normQ = card.question.lowercase(Locale.US).trim()
        if (normQ.isNotEmpty() && seenQuestions.add(normQ)) {
          val assignedCard = card.copy(id = accumulatedCards.size + 1)
          accumulatedCards.add(assignedCard)
          latestQuestion = assignedCard.question
          latestAnswer = assignedCard.answer
        }
      }

      val updatedStatus =
        "Scanning Pages $pageStart–$pageStart of $totalBatches • ${accumulatedCards.size} Cards Extracted"

      onProgressUpdate(
        batchIdx + 1,
        totalBatches,
        updatedStatus,
        accumulatedCards.toList(),
        latestQuestion,
        latestAnswer
      )
    }

    if (accumulatedCards.isEmpty()) {
      val fallbackCards = generateCardsFromText(rawText, defaultCategory)
      for (card in fallbackCards) {
        val normQ = card.question.lowercase(Locale.US).trim()
        if (normQ.isNotEmpty() && seenQuestions.add(normQ)) {
          val assignedCard = card.copy(id = accumulatedCards.size + 1)
          accumulatedCards.add(assignedCard)
          latestQuestion = assignedCard.question
          latestAnswer = assignedCard.answer
        }
      }
    }

    accumulatedCards
  }

  /**
   * Splits arbitrarily long text into chunks of at most `maxChunkChars` (4,000 chars)
   * along paragraph (`\n`) boundaries.
   */
  fun splitTextIntoParagraphChunks(rawText: String, maxChunkChars: Int = MAX_TEXT_CHUNK_CHARS): List<String> {
    val normalized = rawText
      .replace("\r\n", "\n")
      .replace('\r', '\n')
      .trim()
    if (normalized.isEmpty()) return emptyList()
    if (normalized.length <= maxChunkChars) return listOf(normalized)

    val paragraphs = normalized.split("\n")
    val chunks = mutableListOf<String>()
    val currentBuilder = StringBuilder()

    for (para in paragraphs) {
      val trimmedPara = para.trim()
      if (trimmedPara.isEmpty()) continue

      if (trimmedPara.length > maxChunkChars) {
        if (currentBuilder.isNotEmpty()) {
          chunks.add(currentBuilder.toString().trim())
          currentBuilder.clear()
        }
        var cursor = 0
        while (cursor < trimmedPara.length) {
          val end = minOf(cursor + maxChunkChars, trimmedPara.length)
          chunks.add(trimmedPara.substring(cursor, end).trim())
          cursor = end
        }
      } else if (currentBuilder.length + trimmedPara.length + 1 > maxChunkChars) {
        if (currentBuilder.isNotEmpty()) {
          chunks.add(currentBuilder.toString().trim())
          currentBuilder.clear()
        }
        currentBuilder.append(trimmedPara)
      } else {
        if (currentBuilder.isNotEmpty()) {
          currentBuilder.append("\n")
        }
        currentBuilder.append(trimmedPara)
      }
    }

    if (currentBuilder.isNotEmpty()) {
      chunks.add(currentBuilder.toString().trim())
    }
    return chunks.filter { it.isNotBlank() }
  }

  private fun requestGeminiFlashcardsForPdfBatch(
    activeApiKey: String,
    base64JpegPages: List<String>,
    startPage: Int,
    endPage: Int,
    totalPages: Int,
    defaultCategory: String
  ): List<FlashcardItem> {
    val partsArray = JSONArray()
    partsArray.put(
      JSONObject().apply {
        put(
          "text",
          "$SYSTEM_PROMPT_INSTRUCTION\n\n(Document Batch: PDF Pages $startPage to $endPage of $totalPages)"
        )
      }
    )

    for (base64Image in base64JpegPages) {
      partsArray.put(
        JSONObject().apply {
          put(
            "inline_data",
            JSONObject().apply {
              put("mime_type", "image/jpeg")
              put("data", base64Image)
            }
          )
        }
      )
    }

    return executeGeminiGenerateContentCall(
      activeApiKey = activeApiKey,
      partsArray = partsArray,
      defaultCategory = defaultCategory
    )
  }

  private fun requestGeminiFlashcardsForTextChunk(
    activeApiKey: String,
    textChunk: String,
    batchNumber: Int,
    totalBatches: Int,
    defaultCategory: String
  ): List<FlashcardItem> {
    val partsArray = JSONArray().apply {
      put(
        JSONObject().apply {
          put(
            "text",
            "$SYSTEM_PROMPT_INSTRUCTION\n\n--- DOCUMENT BATCH $batchNumber OF $totalBatches ---\n$textChunk"
          )
        }
      )
    }

    return executeGeminiGenerateContentCall(
      activeApiKey = activeApiKey,
      partsArray = partsArray,
      defaultCategory = defaultCategory
    )
  }

  private fun executeGeminiGenerateContentCall(
    activeApiKey: String,
    partsArray: JSONArray,
    defaultCategory: String,
    allowModelDiscoveryFallback: Boolean = true
  ): List<FlashcardItem> {
    val requestBodyJson = JSONObject().apply {
      put(
        "contents",
        JSONArray().apply {
          put(
            JSONObject().apply {
              put("parts", partsArray)
            }
          )
        }
      )
      put(
        "generationConfig",
        JSONObject().apply {
          put("responseMimeType", "application/json")
          put("temperature", 0.1)
          put("maxOutputTokens", 8192)
        }
      )
    }.toString()

    val cleanKey = activeApiKey.trim().ifEmpty { DEFAULT_GEMINI_API_KEY }
    val activeModel = resolveWorkingGeminiModel(cleanKey, forceRefresh = false)
    val endpointUrl =
      "https://generativelanguage.googleapis.com/v1beta/models/$activeModel:generateContent?key=$cleanKey"
    var connection: HttpURLConnection? = null

    try {
      connection = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 60_000
        readTimeout = 60_000
        doInput = true
        doOutput = true
        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
      }

      OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
        writer.write(requestBodyJson)
        writer.flush()
      }

      val responseCode = connection.responseCode
      if (responseCode in 200..299) {
        hasVerifiedWorkingModel = true
        val responseBody = BufferedReader(
          InputStreamReader(connection.inputStream, Charsets.UTF_8)
        ).use { it.readText() }
        return parseGeminiFlashcardsJsonResponse(responseBody, defaultCategory)
      } else {
        val rawErrorBody = try {
          connection.errorStream?.let { errStream ->
            BufferedReader(InputStreamReader(errStream, Charsets.UTF_8)).use { it.readText() }
          }.orEmpty()
        } catch (_: Exception) {
          ""
        }

        val isModelNotFound = responseCode == 404 ||
          (responseCode == 400 && rawErrorBody.contains("not found", ignoreCase = true))

        if (isModelNotFound && allowModelDiscoveryFallback) {
          connection.disconnect()
          connection = null
          val fallbackModel = resolveWorkingGeminiModel(cleanKey, forceRefresh = true)
          if (fallbackModel.isNotBlank()) {
            return executeGeminiGenerateContentCall(
              activeApiKey = cleanKey,
              partsArray = partsArray,
              defaultCategory = defaultCategory,
              allowModelDiscoveryFallback = false
            )
          }
        }

        val parsedApiErrorMessage = extractDetailedErrorFromPayload(responseCode, rawErrorBody)
        throw GeminiBatchExtractionException(
          statusCode = responseCode,
          message = parsedApiErrorMessage
        )
      }
    } catch (e: GeminiBatchExtractionException) {
      throw e
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val networkDetail = e.localizedMessage?.takeIf { it.isNotBlank() }
        ?: "Unable to reach Google Gemini API. Please check your internet connection."
      throw GeminiBatchExtractionException(
        statusCode = -1,
        message = "Network Error: $networkDetail"
      )
    } finally {
      connection?.disconnect()
    }
  }

  /**
   * Robust JSON Parser:
   * - Extracts the model text from `candidates[0].content.parts`
   * - Safely locates the first `'['` and last `']'` in the response string before passing to `JSONArray(...)`
   *   so any stray markdown or conversational text never breaks parsing.
   */
  fun parseGeminiFlashcardsJsonResponse(
    rawApiResponse: String,
    defaultCategory: String
  ): List<FlashcardItem> {
    val root = JSONObject(rawApiResponse)
    val candidates = root.optJSONArray("candidates")
      ?: throw GeminiBatchExtractionException(
        statusCode = 200,
        message = "Gemini returned no candidates for this batch. Check safety filters or prompt content."
      )
    if (candidates.length() == 0) {
      return emptyList()
    }

    val combinedTextBuilder = StringBuilder()
    for (cIdx in 0 until candidates.length()) {
      val candidateObj = candidates.optJSONObject(cIdx) ?: continue
      val contentObj = candidateObj.optJSONObject("content") ?: continue
      val parts = contentObj.optJSONArray("parts") ?: continue
      for (pIdx in 0 until parts.length()) {
        val partText = parts.optJSONObject(pIdx)?.optString("text", "").orEmpty()
        if (partText.isNotBlank()) {
          combinedTextBuilder.append(partText).append("\n")
        }
      }
    }

    val strippedText = combinedTextBuilder.toString()
      .replace("```json", "", ignoreCase = true)
      .replace("```", "")
      .trim()

    if (strippedText.isEmpty()) {
      return emptyList()
    }

    val firstBracket = strippedText.indexOf('[')
    val lastBracket = strippedText.lastIndexOf(']')

    val cardsJsonArray: JSONArray = try {
      if (firstBracket != -1 && lastBracket != -1 && lastBracket >= firstBracket) {
        val arraySubstring = strippedText.substring(firstBracket, lastBracket + 1)
        JSONArray(arraySubstring)
      } else if (strippedText.startsWith("{")) {
        val obj = JSONObject(strippedText)
        obj.optJSONArray("flashcards")
          ?: obj.optJSONArray("cards")
          ?: obj.optJSONArray("items")
          ?: JSONArray().put(obj)
      } else {
        JSONArray(strippedText)
      }
    } catch (e: Exception) {
      // Fallback: if response was slightly truncated at the end of a huge batch, recover all complete `{...}` objects
      val recoveredArray = recoverCompleteJsonObjects(strippedText)
      if (recoveredArray.length() > 0) {
        recoveredArray
      } else {
        throw GeminiBatchExtractionException(
          statusCode = 200,
          message = "Failed to parse JSON flashcards from Gemini response: ${e.localizedMessage ?: "Invalid JSON format"}"
        )
      }
    }

    val parsedCards = mutableListOf<FlashcardItem>()
    for (i in 0 until cardsJsonArray.length()) {
      val itemObj = cardsJsonArray.optJSONObject(i) ?: continue
      val question = itemObj.optString("question", "")
        .ifBlank { itemObj.optString("front", "") }
        .ifBlank { itemObj.optString("term", "") }
        .trim()
      val answer = itemObj.optString("answer", "")
        .ifBlank { itemObj.optString("back", "") }
        .ifBlank { itemObj.optString("definition", "") }
        .trim()

      if (question.isNotEmpty() && answer.isNotEmpty()) {
        parsedCards.add(
          FlashcardItem(
            id = i + 1,
            question = question,
            answer = answer,
            category = defaultCategory.ifBlank { "General" },
            masteryLevel = "New"
          )
        )
      }
    }
    return parsedCards
  }

  private fun recoverCompleteJsonObjects(rawText: String): JSONArray {
    val result = JSONArray()
    val objRegex = Regex("\\{[^{}]*\"question\"[\\s\\S]*?\"answer\"[\\s\\S]*?\\}", RegexOption.IGNORE_CASE)
    for (match in objRegex.findAll(rawText)) {
      try {
        val obj = JSONObject(match.value)
        if (obj.optString("question").isNotBlank() && obj.optString("answer").isNotBlank()) {
          result.put(obj)
        }
      } catch (_: Exception) {
      }
    }
    return result
  }

  private fun extractDetailedErrorFromPayload(httpCode: Int, rawErrorBody: String): String {
    val trimmed = rawErrorBody.trim()
    if (trimmed.isNotEmpty()) {
      try {
        val errRoot = JSONObject(trimmed)
        val errObj = errRoot.optJSONObject("error")
        if (errObj != null) {
          val status = errObj.optString("status", "")
          val msg = errObj.optString("message", "")
          if (msg.isNotBlank()) {
            return if (status.isNotBlank()) {
              "HTTP $httpCode ($status): $msg"
            } else {
              "HTTP $httpCode: $msg"
            }
          }
        }
      } catch (_: Exception) {
      }
      return "HTTP $httpCode: ${trimmed.take(280)}"
    }
    return when (httpCode) {
      400 -> "HTTP 400 Bad Request: Please verify your Gemini API key or document format."
      403 -> "HTTP 403 Forbidden: API key is invalid, restricted, or expired. Tap the Key icon to paste a valid Google AI Studio key."
      429 -> "HTTP 429 Rate Limit Exceeded: Too many requests. Please wait a moment and try again."
      else -> "HTTP $httpCode Error from Gemini API."
    }
  }
}

/**
 * 100% Free, Offline, Zero-Fail Document & Text Flashcard Extractor (`OfflineDeckExtractor`).
 * Requires no internet connection and no API keys.
 * Supports `.docx` (via `ZipInputStream` reading `word/document.xml`), `.txt` (via `BufferedReader`),
 * `.pdf` (scanning `BT...ET`, parenthesized strings `Regex("\\(([^()]{3,})\\)")`, FlateDecode streams,
 * and clean ASCII runs), and direct pasted study notes.
 */
object OfflineDeckExtractor {
  val SUPPORTED_DOCUMENT_MIME_TYPES = arrayOf(
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/pdf",
    "text/plain"
  )

  /**
   * Extracts raw text from a `.docx`, `.txt`, or `.pdf` document URI on `Dispatchers.IO`.
   */
  suspend fun extractRawTextFromUri(
    context: Context,
    uri: Uri,
    fileName: String
  ): String = withContext(Dispatchers.IO) {
    val mimeType = try {
      context.contentResolver.getType(uri).orEmpty().lowercase(Locale.US)
    } catch (_: Exception) {
      ""
    }
    val lowerName = fileName.lowercase(Locale.US)

    when {
      lowerName.endsWith(".docx") || mimeType.contains("wordprocessingml") -> {
        extractTextFromDocx(context, uri)
      }
      lowerName.endsWith(".txt") || mimeType.startsWith("text/") -> {
        extractTextFromTxt(context, uri)
      }
      lowerName.endsWith(".pdf") || mimeType.contains("pdf") -> {
        extractTextFromPdf(context, uri)
      }
      else -> {
        val docxAttempt = extractTextFromDocx(context, uri)
        if (docxAttempt.isNotBlank()) {
          docxAttempt
        } else {
          val txtAttempt = extractTextFromTxt(context, uri)
          if (txtAttempt.isNotBlank()) txtAttempt else extractTextFromPdf(context, uri)
        }
      }
    }
  }

  /**
   * `.docx` Parser: Uses `ZipInputStream` on `context.contentResolver.openInputStream(uri)`,
   * locates `"word/document.xml"`, preserves line breaks for `</w:p>`, `<w:br/>`, and table cells `</w:tc>`,
   * and strips XML tags (`Regex("<[^>]*>")`).
   */
  fun extractTextFromDocx(context: Context, uri: Uri): String {
    return try {
      context.contentResolver.openInputStream(uri)?.use { inputStream ->
        ZipInputStream(inputStream).use { zipStream ->
          var entry = zipStream.nextEntry
          while (entry != null) {
            if (entry.name.equals("word/document.xml", ignoreCase = true)) {
              val xmlContent = zipStream.bufferedReader(Charsets.UTF_8).readText()
              val withParagraphBreaks = xmlContent
                .replace("</w:p>", "\n")
                .replace("<w:br/>", "\n")
                .replace("<w:br />", "\n")
                .replace("</w:tc>", "\n")
                .replace("</w:tr>", "\n")
                .replace("<w:tab/>", " ")
              val stripped = withParagraphBreaks
                .replace(Regex("<[^>]*>"), " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
              return@use stripped
                .lines()
                .map { it.replace(Regex("\\s+"), " ").trim() }
                .filter { it.isNotEmpty() }
                .joinToString("\n")
            }
            zipStream.closeEntry()
            entry = zipStream.nextEntry
          }
          ""
        }
      } ?: ""
    } catch (_: Exception) {
      ""
    }
  }

  /**
   * `.txt` Parser: Reads text directly via `BufferedReader`.
   */
  fun extractTextFromTxt(context: Context, uri: Uri): String {
    return try {
      context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
          reader.readText().trim()
        }
      } ?: ""
    } catch (_: Exception) {
      ""
    }
  }

  /**
   * `.pdf` Parser: Scans the raw byte stream (and decompressed FlateDecode streams where present)
   * for readable text inside PDF text blocks (`BT...ET`), parenthesized strings `Regex("\\(([^()]{3,})\\)")`,
   * and clean ASCII runs.
   */
  fun extractTextFromPdf(context: Context, uri: Uri): String {
    return try {
      val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: return ""
      if (rawBytes.isEmpty()) return ""

      val rawLatin = String(rawBytes, Charsets.ISO_8859_1)
      val combinedStreamsBuilder = StringBuilder(rawLatin)

      // Also attempt to inflate compressed PDF streams so compressed text blocks are readable
      val streamRegex = Regex("stream\\r?\\n([\\s\\S]*?)\\r?\\nendstream")
      for (match in streamRegex.findAll(rawLatin)) {
        val streamContent = match.groupValues.getOrNull(1) ?: continue
        if (streamContent.isEmpty() || streamContent.length > 500_000) continue
        val streamBytes = streamContent.toByteArray(Charsets.ISO_8859_1)
        val inflated = tryInflatePdfStream(streamBytes, nowrap = false)
          ?: tryInflatePdfStream(streamBytes, nowrap = true)
        if (!inflated.isNullOrBlank()) {
          combinedStreamsBuilder.append("\n").append(inflated)
        }
      }

      val fullPdfContent = combinedStreamsBuilder.toString()
      val parenthesizedRegex = Regex("\\(([^()]{3,})\\)")
      val btEtRegex = Regex("BT([\\s\\S]*?)ET")
      val extractedSnippets = mutableListOf<String>()

      // 1. Scan inside BT...ET text blocks for parenthesized text runs
      for (btMatch in btEtRegex.findAll(fullPdfContent)) {
        val block = btMatch.groupValues.getOrNull(1) ?: continue
        val blockPieces = mutableListOf<String>()
        for (pMatch in parenthesizedRegex.findAll(block)) {
          val cleaned = cleanPdfStringLiteral(pMatch.groupValues[1])
          if (isReadableStudySnippet(cleaned)) {
            blockPieces.add(cleaned)
          }
        }
        if (blockPieces.isNotEmpty()) {
          val joinedLine = blockPieces.joinToString(" ").replace(Regex("\\s+"), " ").trim()
          if (isReadableStudySnippet(joinedLine)) {
            extractedSnippets.add(joinedLine)
          }
        }
      }

      // 2. Scan parenthesized strings across the stream if BT...ET blocks were sparse
      if (extractedSnippets.sumOf { it.length } < 180) {
        for (pMatch in parenthesizedRegex.findAll(fullPdfContent)) {
          val cleaned = cleanPdfStringLiteral(pMatch.groupValues[1])
          if (isReadableStudySnippet(cleaned) && cleaned.length >= 6) {
            extractedSnippets.add(cleaned)
          }
        }
      }

      // 3. Scan clean ASCII runs if still sparse
      if (extractedSnippets.sumOf { it.length } < 180) {
        val asciiRunRegex = Regex("[A-Za-z0-9][A-Za-z0-9 ,.:;=+\\-*/()%]{12,180}")
        for (runMatch in asciiRunRegex.findAll(fullPdfContent)) {
          val candidate = runMatch.value.replace(Regex("\\s+"), " ").trim()
          if (isReadableStudySnippet(candidate)) {
            extractedSnippets.add(candidate)
          }
        }
      }

      extractedSnippets
        .distinct()
        .joinToString("\n")
        .trim()
    } catch (_: Exception) {
      ""
    }
  }

  private fun tryInflatePdfStream(bytes: ByteArray, nowrap: Boolean): String? {
    return try {
      val inflater = Inflater(nowrap)
      InflaterInputStream(ByteArrayInputStream(bytes), inflater).use { stream ->
        val out = stream.readBytes()
        inflater.end()
        if (out.isNotEmpty()) String(out, Charsets.ISO_8859_1) else null
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun cleanPdfStringLiteral(raw: String): String {
    return raw
      .replace("\\n", "\n")
      .replace("\\r", " ")
      .replace("\\t", " ")
      .replace("\\(", "(")
      .replace("\\)", ")")
      .replace("\\\\", "\\")
      .filter { ch -> ch == '\n' || ch in ' '..'~' }
      .replace(Regex("\\s+"), " ")
      .trim()
  }

  private fun isReadableStudySnippet(text: String): Boolean {
    if (text.length < 4) return false
    val lower = text.lowercase(Locale.US)
    val pdfInternalTokens = listOf(
      "/type", "/font", "/page", "/catalog", "/filter", "flatedecode",
      "endobj", "startxref", "mediabox", "resources", "procset", "xref",
      "obj <<", "cidfont", "basefont", "encoding", "winansiencoding"
    )
    if (pdfInternalTokens.any { lower.contains(it) }) return false
    val letterCount = text.count { it.isLetter() }
    val printableCount = text.count { it.isLetterOrDigit() || it.isWhitespace() || it in ".,:;-=+/()%" }
    return letterCount >= 3 &&
      letterCount.toFloat() / text.length.toFloat() >= 0.38f &&
      printableCount.toFloat() / text.length.toFloat() >= 0.88f
  }
}

/**
 * Smart Heuristic Card Generation (`generateCardsFromText(rawText: String): List<FlashcardItem>`):
 * - Splits extracted text into clean lines and sentences.
 * - Rule 1 (Colon / Dash Definitions): Lines formatted like `"Term: Definition"` or `"Term - Definition"`
 *   (where Term is 3–60 chars) -> Front: `"Define: [Term]"`, Back: `"[Definition]"`.
 * - Rule 2 (Formulas & Equations): Sentences containing `"="` or keywords (`"formula"`, `"NSCP"`,
 *   `"minimum"`, `"ratio"`, `"stress"`, `"pressure"`, `"moment"`) -> Front: Cloze or conceptual question
 *   about the subject of the sentence, Back: Full provision/formula sentence.
 * - Rule 3 (Key Concept Sentences): Groups remaining informative sentences (40–220 characters) into
 *   structured concept review cards so the extractor ALWAYS generates at least 5 to 12 study cards
 *   from any document or pasted text.
 */
fun generateCardsFromText(
  rawText: String,
  category: String = "Offline Study Deck"
): List<FlashcardItem> {
  val normalizedText = rawText
    .replace("\r\n", "\n")
    .replace('\r', '\n')
    .trim()

  val generatedPairs = mutableListOf<Pair<String, String>>()
  val seenQuestions = mutableSetOf<String>()

  fun addUniqueCard(front: String, back: String) {
    val cleanFront = front.replace(Regex("\\s+"), " ").trim()
    val cleanBack = back.replace(Regex("\\s+"), " ").trim()
    val questionKey = cleanFront.lowercase(Locale.US)
    if (cleanFront.isNotEmpty() && cleanBack.isNotEmpty() && seenQuestions.add(questionKey)) {
      generatedPairs.add(cleanFront to cleanBack)
    }
  }

  // Rule 0 (Direct Q&A Recognition - Highest Priority):
  // Match explicit Q/A blocks such as "Q: ... A: ...", "Question 1: ... Answer: ...", or numbered questions with Answer/Ans lines
  val explicitQaBlockRegex = Regex(
    "(?:^|\\n)\\s*(?:Q(?:uestion)?\\s*\\d*\\s*[:.\\-)]|\\d+\\s*[.)])\\s*([^\\n]+(?:\\n(?!\\s*(?:A(?:ns(?:wer)?)?\\s*[:.\\-)]|Correct\\s+Answer\\s*[:.\\-)]))[^\\n]+)*)\\s*\\n\\s*(?:A(?:ns(?:wer)?)?|Correct\\s+Answer)\\s*[:.\\-)]\\s*([^\\n]+)",
    setOf(RegexOption.IGNORE_CASE)
  )
  for (match in explicitQaBlockRegex.findAll(normalizedText)) {
    val qText = match.groupValues.getOrNull(1).orEmpty().trim()
    val aText = match.groupValues.getOrNull(2).orEmpty().trim()
    if (qText.length >= 4 && aText.isNotEmpty()) {
      addUniqueCard(front = qText, back = aText)
    }
  }

  val lines = normalizedText
    .lines()
    .map { it.replace(Regex("\\s+"), " ").trim().trimStart('•', '*', '-', '·').trim() }
    .filter { it.length >= 4 }

  // Also scan consecutive lines where a question ending with '?' is followed by an answer line
  var idx = 0
  while (idx < lines.size - 1) {
    val currentLine = lines[idx]
    val nextLine = lines[idx + 1]
    if (currentLine.endsWith("?") && currentLine.length >= 8 && !nextLine.endsWith("?")) {
      val cleanedAnswer = nextLine.replace(
        Regex("^(?:A(?:ns(?:wer)?)?|Answer)\\s*[:.\\-)]\\s*", RegexOption.IGNORE_CASE),
        ""
      ).trim()
      if (cleanedAnswer.isNotEmpty()) {
        addUniqueCard(front = currentLine, back = cleanedAnswer)
        idx += 2
        continue
      }
    }
    idx++
  }

  val sentences = normalizedText
    .split(Regex("(?<=[.!?])\\s+|\\n+"))
    .map { it.replace(Regex("\\s+"), " ").trim().trimStart('•', '*', '-', '·').trim() }
    .filter { it.length >= 10 }

  // Rule 1 (Colon / Dash Definitions): "Term: Definition" or "Term - Definition" (Term 3..80 chars)
  val definitionRegex = Regex("^([^:\\-–—]{3,80})\\s*(?::|\\s[-–—]\\s)\\s*(.{4,420})$")
  for (line in lines) {
    val match = definitionRegex.find(line) ?: continue
    val term = match.groupValues[1].trim()
    val definition = match.groupValues[2].trim()
    if (term.equals("Question", ignoreCase = true) ||
      term.equals("Q", ignoreCase = true) ||
      term.equals("Answer", ignoreCase = true) ||
      term.equals("Ans", ignoreCase = true)
    ) {
      continue
    }
    val letterCount = term.count { it.isLetter() }
    if (term.length in 3..80 && letterCount >= 2 && definition.length >= 4) {
      val front = if (term.endsWith("?")) term else "Define: $term"
      addUniqueCard(
        front = front,
        back = definition
      )
    }
  }

  // Rule 2 (Formulas & Equations): Sentences containing "=" or CE keywords
  val formulaKeywords = listOf(
    "formula", "nscp", "minimum", "ratio", "stress", "pressure", "moment"
  )
  for (sentence in sentences) {
    val lower = sentence.lowercase(Locale.US)
    val hasEquals = sentence.contains("=")
    val matchedKeyword = formulaKeywords.firstOrNull { lower.contains(it) }
    if (hasEquals || matchedKeyword != null) {
      val frontPrompt = if (hasEquals) {
        val lhs = sentence.substringBefore("=").trim()
        val subjectLabel = lhs.takeLast(48).trim().ifEmpty { "this engineering equation" }
        "Formula & Cloze Recall: Complete the expression or state the equation for \"$subjectLabel = [ ___ ]\""
      } else {
        val topicSnippet = extractSubjectPhraseFromSentence(sentence, matchedKeyword ?: "concept")
        "Engineering Provision (${matchedKeyword?.uppercase(Locale.US) ?: "CE"}): What is the rule or relationship regarding $topicSnippet?"
      }
      addUniqueCard(
        front = frontPrompt,
        back = sentence
      )
    }
  }

  // Rule 3 (Key Concept Sentences): Remaining informative sentences (40..240 characters)
  for (sentence in sentences) {
    if (sentence.length in 40..240) {
      val subjectPhrase = extractSubjectPhraseFromSentence(sentence, "this concept")
      addUniqueCard(
        front = "Key Concept Review: Explain the principle or engineering significance of $subjectPhrase.",
        back = sentence
      )
    }
  }

  // If fewer than 5 cards were found, also split/group any remaining clauses or lines
  if (generatedPairs.size < 5) {
    val allFragments = (sentences + lines + normalizedText.split(Regex("[,;]")))
      .map { it.replace(Regex("\\s+"), " ").trim() }
      .filter { it.length >= 6 }
      .distinct()

    for ((fIdx, fragment) in allFragments.withIndex()) {
      if (generatedPairs.size >= 8) break
      val topic = extractSubjectPhraseFromSentence(fragment, "Study Note #${fIdx + 1}")
      addUniqueCard(
        front = "Concept Check #${generatedPairs.size + 1}: Summarize the key takeaway regarding $topic.",
        back = fragment
      )
    }
  }

  // Guarantee ALWAYS at least 5 study cards from any document or pasted text
  if (generatedPairs.size < 5) {
    val baseSnippet = normalizedText.ifBlank { "Civil Engineering Study Notes" }.take(180)
    val fallbackTemplates = listOf(
      "Core Concept Summary: What is the primary subject or definition stated in this study note?" to baseSnippet,
      "Key Engineering Takeaway: Which provision, value, or relationship should be remembered from: \"$baseSnippet\"?" to
        "Focus on the key terms, units, and boundary conditions described in: $baseSnippet",
      "Active Recall Prompt: State the main engineering principle highlighted in this section." to baseSnippet,
      "Application & Exam Check: How is \"$baseSnippet\" applied in Civil Engineering problem solving?" to
        "Identify the governing parameters, sign conventions, and standard units for: $baseSnippet",
      "Self-Verification Question: Recite the key statement or formula from memory." to baseSnippet
    )
    for ((q, a) in fallbackTemplates) {
      if (generatedPairs.size >= 5) break
      generatedPairs.add(q to a)
    }
  }

  return generatedPairs.mapIndexed { index, (question, answer) ->
    FlashcardItem(
      id = index + 1,
      question = question,
      answer = answer,
      category = category.ifBlank { "Offline Study Deck" },
      masteryLevel = "New"
    )
  }
}

private fun extractSubjectPhraseFromSentence(sentence: String, fallback: String): String {
  val cleaned = sentence
    .replace(Regex("^(The|A|An|In|For|According to|Under|When)\\s+", RegexOption.IGNORE_CASE), "")
    .trim()
  val words = cleaned
    .split(Regex("\\s+"))
    .map { it.trim(',', '.', ';', ':', '(', ')', '"', '\'') }
    .filter { it.isNotEmpty() }
  if (words.isEmpty()) return fallback
  val phrase = words.take(6).joinToString(" ")
  return "\"$phrase\""
}

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
 * 4. Account-segregated `AlarmItem` lists (`alarms_[username]` or `alarms_guest`).
 * 5. Account-segregated `FlashcardDeck` lists (`flashcards_[username]` or `flashcards_guest`),
 *    automatically seeding default CE demo decks if storage is empty.
 * 6. Active user session (`username`) and `isDarkMode`.
 * 7. Explicit Guest data wipe (`clearGuestData()`) when logging out of Guest mode.
 */
class LocalStorageManager(context: Context) {
  private val appContext: Context = context.applicationContext ?: context
  private val prefs: SharedPreferences =
    appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  init {
    migrateTaskSchemaIfNeeded()
    clearLegacyPlaceholderFlashcardDecksIfNeeded()
    restoreFromDeviceBackupIfUnauthenticated()
  }

  /**
   * Obtains the persistent hardware identifier using `Settings.Secure.ANDROID_ID`.
   */
  @SuppressLint("HardwareIds")
  fun getDeviceAndroidId(): String {
    return try {
      val id = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
      if (!id.isNullOrBlank()) id.trim() else "default_device"
    } catch (_: Exception) {
      "default_device"
    }
  }

  fun getDeviceBackupFileName(): String {
    return "sibil_device_backup_${getDeviceAndroidId()}.json"
  }

  /**
   * On app startup, if `SharedPreferences` has no logged-in username (such as after an uninstall
   * and fresh reinstall), automatically looks for `"sibil_device_backup_${androidId}.json"` in
   * `MediaStore.Downloads` or `Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)`.
   * If found, silently restores the username, tasks, schedules, alarms, and flashcard decks
   * before rendering the screen so the user is logged right back into their account automatically.
   */
  private fun restoreFromDeviceBackupIfUnauthenticated() {
    val existingUser = prefs.getString(KEY_USER_NAME, "")?.trim().orEmpty()
    if (existingUser.isNotBlank()) return

    val backupJson = readDeviceBackupFileFromDownloads()
    if (!backupJson.isNullOrBlank()) {
      importBackupJson(
        rawBackupInput = backupJson,
        fallbackAccountKey = GUEST_ACCOUNT_KEY,
        syncToDeviceAfterImport = false
      )
    }
  }

  /**
   * Triggers `BackupManager(context).dataChanged()` and writes `"sibil_device_backup_${androidId}.json"`
   * into public shared storage (`MediaStore.Downloads` on Android 10+ and `DIRECTORY_DOWNLOADS`).
   */
  fun syncToDeviceStorage(activeAccountKey: String = GUEST_ACCOUNT_KEY) {
    try {
      BackupManager(appContext).dataChanged()
    } catch (_: Exception) {
    }

    val currentSavedUser = prefs.getString(KEY_USER_NAME, "")?.trim().orEmpty()
    val hasRegisteredAccounts = getRegisteredUsers().isNotEmpty()
    if (currentSavedUser.isBlank() && !hasRegisteredAccounts) {
      return
    }

    val backupPayload = exportBackupJson(activeAccountKey)
    writeDeviceBackupFileToDownloads(backupPayload)
  }

  private fun writeDeviceBackupFileToDownloads(jsonContent: String) {
    val fileName = getDeviceBackupFileName()
    val bytes = jsonContent.toByteArray(Charsets.UTF_8)

    // 1. Android 10+ (API 29+): Write via MediaStore.Downloads
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      try {
        val resolver = appContext.contentResolver
        val downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        var existingUri: Uri? = null
        resolver.query(
          downloadsUri,
          arrayOf(MediaStore.MediaColumns._ID),
          selection,
          selectionArgs,
          "${MediaStore.MediaColumns._ID} DESC"
        )?.use { cursor ->
          if (cursor.moveToFirst()) {
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val id = cursor.getLong(idCol)
            existingUri = ContentUris.withAppendedId(downloadsUri, id)
          }
        }

        val targetUri = existingUri ?: run {
          val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
          }
          resolver.insert(downloadsUri, values)
        }

        if (targetUri != null) {
          resolver.openOutputStream(targetUri, "wt")?.use { out ->
            out.write(bytes)
            out.flush()
          }
        }
      } catch (_: Exception) {
      }
    }

    // 2. Direct Public Downloads File Fallback
    try {
      val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      if (downloadsDir != null) {
        if (!downloadsDir.exists()) {
          downloadsDir.mkdirs()
        }
        val targetFile = File(downloadsDir, fileName)
        targetFile.writeText(jsonContent, Charsets.UTF_8)
      }
    } catch (_: Exception) {
    }
  }

  private fun readDeviceBackupFileFromDownloads(): String? {
    val androidId = getDeviceAndroidId()
    val exactFileName = "sibil_device_backup_${androidId}.json"
    val prefixPattern = "sibil_device_backup_${androidId}%"

    // 1. Android 10+ (API 29+): Query MediaStore.Downloads
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      try {
        val resolver = appContext.contentResolver
        val downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val selection =
          "${MediaStore.MediaColumns.DISPLAY_NAME} = ? OR ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf(exactFileName, prefixPattern)

        resolver.query(
          downloadsUri,
          arrayOf(MediaStore.MediaColumns._ID),
          selection,
          selectionArgs,
          "${MediaStore.MediaColumns._ID} DESC"
        )?.use { cursor ->
          while (cursor.moveToNext()) {
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val id = cursor.getLong(idCol)
            val itemUri = ContentUris.withAppendedId(downloadsUri, id)
            val text = resolver.openInputStream(itemUri)?.bufferedReader(Charsets.UTF_8)?.use {
              it.readText()
            }?.trim()
            if (!text.isNullOrBlank()) {
              return text
            }
          }
        }
      } catch (_: Exception) {
      }
    }

    // 2. Direct Public Downloads Directory Fallback
    try {
      val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      if (downloadsDir != null && downloadsDir.exists()) {
        val exactFile = File(downloadsDir, exactFileName)
        if (exactFile.exists() && exactFile.canRead()) {
          val text = exactFile.readText(Charsets.UTF_8).trim()
          if (text.isNotBlank()) return text
        }
        val matchingFiles = downloadsDir.listFiles { file ->
          file.name.startsWith("sibil_device_backup_${androidId}") && file.name.endsWith(".json")
        }
        val newestFile = matchingFiles?.maxByOrNull { it.lastModified() }
        if (newestFile != null && newestFile.canRead()) {
          val text = newestFile.readText(Charsets.UTF_8).trim()
          if (text.isNotBlank()) return text
        }
      }
    } catch (_: Exception) {
    }

    return null
  }

  /**
   * One-time migration that removes previously seeded hardcoded demo flashcard decks
   * (NSCP 2015 Provisions & Covers, Fluid Mechanics & Hydraulics, Geotechnical / Soil Mechanics)
   * so the flashcard deck list starts completely empty unless the user created or imported decks.
   */
  private fun clearLegacyPlaceholderFlashcardDecksIfNeeded() {
    val savedFlashcardSchemaVersion = prefs.getInt(KEY_FLASHCARD_SCHEMA_VERSION, 1)
    if (savedFlashcardSchemaVersion >= CURRENT_FLASHCARD_SCHEMA_VERSION) return

    val legacyPlaceholderDeckTitles = setOf(
      "NSCP 2015 Provisions & Covers",
      "Fluid Mechanics & Hydraulics",
      "Geotechnical / Soil Mechanics"
    )

    val editor = prefs.edit()
    val allEntries = prefs.all
    for ((key, value) in allEntries) {
      if (!key.startsWith(KEY_FLASHCARDS_PREFIX)) continue
      val rawJson = value as? String
      if (rawJson.isNullOrBlank()) {
        editor.remove(key)
        continue
      }
      try {
        val jsonArray = JSONArray(rawJson)
        val filteredArray = JSONArray()
        for (i in 0 until jsonArray.length()) {
          val obj = jsonArray.optJSONObject(i) ?: continue
          val title = obj.optString("title", "").trim()
          if (title in legacyPlaceholderDeckTitles) {
            continue
          }
          filteredArray.put(obj)
        }
        editor.putString(key, filteredArray.toString())
      } catch (_: Exception) {
        editor.remove(key)
      }
    }
    editor.putInt(KEY_FLASHCARD_SCHEMA_VERSION, CURRENT_FLASHCARD_SCHEMA_VERSION)
    editor.apply()
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

  private fun flashcardDecksKeyFor(username: String): String {
    return "${KEY_FLASHCARDS_PREFIX}${normalizeAccountKey(username)}"
  }

  /**
   * Returns all registered users stored in `SharedPreferences` (normalized lowercase username -> password).
   * Uses safe string default `""` and fallback parsing.
   */
  fun getRegisteredUsers(): Map<String, String> {
    val rawJson = prefs.getString(KEY_REGISTERED_USERS, "")?.trim().orEmpty()
    if (rawJson.isEmpty()) return emptyMap()
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
    syncToDeviceStorage(normalizedUsername)
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
    syncToDeviceStorage(username)
  }

  fun loadTasks(username: String = GUEST_ACCOUNT_KEY): List<Task>? {
    val storageKey = tasksKeyFor(username)
    if (!prefs.contains(storageKey)) return null
    val rawJson = prefs.getString(storageKey, "")?.trim().orEmpty()
    if (rawJson.isEmpty()) return emptyList()
    return try {
      val jsonArray = JSONArray(rawJson)
      val loaded = mutableListOf<Task>()
      for (i in 0 until jsonArray.length()) {
        try {
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
        } catch (_: Exception) {
          continue
        }
      }
      loaded
    } catch (_: Exception) {
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
    syncToDeviceStorage(username)
  }

  fun loadSchedules(username: String = GUEST_ACCOUNT_KEY): List<ClassSchedule>? {
    val storageKey = schedulesKeyFor(username)
    if (!prefs.contains(storageKey)) return null
    val rawJson = prefs.getString(storageKey, "")?.trim().orEmpty()
    if (rawJson.isEmpty()) return emptyList()
    return try {
      val jsonArray = JSONArray(rawJson)
      val loaded = mutableListOf<ClassSchedule>()
      for (i in 0 until jsonArray.length()) {
        try {
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
          val timeRange = obj.optString("timeRange", "7:00 AM - 9:00 AM").ifBlank { "7:00 AM - 9:00 AM" }
          val (fallbackStart, fallbackEnd) = extractStartEndTimeTokens(timeRange)
          loaded.add(
            ClassSchedule(
              id = obj.optInt("id", 100 + i),
              subjectName = obj.optString("subjectName", "").trim(),
              timeRange = timeRange,
              days = if (daysList.isEmpty()) listOf("M") else daysList,
              room = obj.optString("room", "ENG. 307").ifBlank { "ENG. 307" },
              colorHex = obj.optLong("colorHex", 0xFF00C4CCL),
              startTime = obj.optString("startTime", fallbackStart).ifBlank { fallbackStart },
              endTime = obj.optString("endTime", fallbackEnd).ifBlank { fallbackEnd }
            )
          )
        } catch (_: Exception) {
          continue
        }
      }
      loaded
    } catch (_: Exception) {
      emptyList()
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
    syncToDeviceStorage(username)
  }

  fun loadAlarms(username: String = GUEST_ACCOUNT_KEY): List<AlarmItem>? {
    val storageKey = alarmsKeyFor(username)
    if (!prefs.contains(storageKey)) return null
    val rawJson = prefs.getString(storageKey, "")?.trim().orEmpty()
    if (rawJson.isEmpty()) return emptyList()
    return try {
      val jsonArray = JSONArray(rawJson)
      val loaded = mutableListOf<AlarmItem>()
      for (i in 0 until jsonArray.length()) {
        try {
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
        } catch (_: Exception) {
          continue
        }
      }
      loaded
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun saveFlashcardDecks(decks: List<FlashcardDeck>, username: String = GUEST_ACCOUNT_KEY) {
    val jsonArray = JSONArray()
    for (deck in decks) {
      val cardsArray = JSONArray()
      for (card in deck.cards) {
        val cardObj = JSONObject().apply {
          val cardMastered = card.isMastered || card.masteryLevel.equals("Good", ignoreCase = true)
          put("id", card.id)
          put("question", card.question)
          put("answer", card.answer)
          put("category", card.category)
          put("masteryLevel", card.masteryLevel.ifBlank { if (cardMastered) "Good" else "New" })
          put("isMastered", cardMastered)
        }
        cardsArray.put(cardObj)
      }
      val deckObj = JSONObject().apply {
        put("id", deck.id)
        put("title", deck.title.trim().ifEmpty { "Untitled Deck" })
        put("subjectTag", deck.subjectTag.trim().ifEmpty { "General CE" })
        put("cards", cardsArray)
      }
      jsonArray.put(deckObj)
    }
    prefs.edit().putString(flashcardDecksKeyFor(username), jsonArray.toString()).apply()
    syncToDeviceStorage(username)
  }

  fun loadFlashcardDecks(username: String = GUEST_ACCOUNT_KEY): List<FlashcardDeck> {
    val storageKey = flashcardDecksKeyFor(username)
    if (!prefs.contains(storageKey)) return emptyList()
    val rawJson = prefs.getString(storageKey, "")?.trim().orEmpty()
    if (rawJson.isEmpty()) {
      return emptyList()
    }
    return try {
      val jsonArray = JSONArray(rawJson)
      val loadedDecks = mutableListOf<FlashcardDeck>()
      for (i in 0 until jsonArray.length()) {
        try {
          val deckObj = jsonArray.optJSONObject(i) ?: continue
          val title = deckObj.optString("title", "").trim()
          if (title.isEmpty()) continue
          val subjectTag = deckObj.optString("subjectTag", "General CE").trim().ifEmpty { "General CE" }
          val cardsArray = deckObj.optJSONArray("cards")
          val loadedCards = mutableListOf<FlashcardItem>()
          if (cardsArray != null) {
            for (j in 0 until cardsArray.length()) {
              try {
                val cardObj = cardsArray.optJSONObject(j) ?: continue
                val question = cardObj.optString("question", "").trim()
                val answer = cardObj.optString("answer", "").trim()
                if (question.isEmpty() || answer.isEmpty()) continue
                val rawMasteryLevel = cardObj.optString("masteryLevel", "New").trim().ifEmpty { "New" }
                val rawIsMastered = cardObj.optBoolean(
                  "isMastered",
                  rawMasteryLevel.equals("Good", ignoreCase = true)
                )
                loadedCards.add(
                  FlashcardItem(
                    id = cardObj.optInt("id", (i + 1) * 100 + j + 1),
                    question = question,
                    answer = answer,
                    category = cardObj.optString("category", subjectTag).trim().ifEmpty { subjectTag },
                    masteryLevel = if (rawIsMastered && rawMasteryLevel.equals("New", ignoreCase = true)) "Good" else rawMasteryLevel,
                    isMastered = rawIsMastered
                  )
                )
              } catch (_: Exception) {
                continue
              }
            }
          }
          loadedDecks.add(
            FlashcardDeck(
              id = deckObj.optInt("id", i + 1),
              title = title,
              subjectTag = subjectTag,
              cards = loadedCards
            )
          )
        } catch (_: Exception) {
          continue
        }
      }
      loadedDecks
    } catch (_: Exception) {
      emptyList()
    }
  }

  /**
   * Explicitly deletes all temporary data associated with the `"guest"` keys
   * (`tasks_guest`, `schedules_guest`, `alarms_guest`, and `flashcards_guest`) when logging out of Guest mode.
   */
  fun clearGuestData() {
    val editor = prefs.edit()
      .remove(tasksKeyFor(GUEST_ACCOUNT_KEY))
      .remove(schedulesKeyFor(GUEST_ACCOUNT_KEY))
      .remove(alarmsKeyFor(GUEST_ACCOUNT_KEY))
      .remove(flashcardDecksKeyFor(GUEST_ACCOUNT_KEY))
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
    val clean = username.trim()
    prefs.edit()
      .putString(KEY_USER_NAME, clean)
      .apply()
    syncToDeviceStorage(clean)
  }

  fun loadUserName(): String? {
    val saved = prefs.getString(KEY_USER_NAME, "")?.trim().orEmpty()
    return saved.takeIf { it.isNotBlank() }
  }

  fun clearUserProfile() {
    prefs.edit()
      .remove(KEY_USER_NAME)
      .apply()
  }

  fun saveDarkMode(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    syncToDeviceStorage(loadUserName().orEmpty())
  }

  fun loadDarkMode(defaultValue: Boolean = false): Boolean {
    return prefs.getBoolean(KEY_DARK_MODE, defaultValue)
  }

  fun saveGeminiApiKey(apiKey: String) {
    prefs.edit().putString(KEY_GEMINI_API_KEY, apiKey.trim()).apply()
    syncToDeviceStorage(loadUserName().orEmpty())
  }

  fun loadSavedGeminiApiKey(): String {
    return prefs.getString(KEY_GEMINI_API_KEY, "")?.trim().orEmpty()
  }

  fun hasSavedGeminiApiKey(): Boolean {
    return loadSavedGeminiApiKey().isNotEmpty()
  }

  fun getActiveGeminiApiKey(): String {
    val saved = loadSavedGeminiApiKey()
    return saved.ifEmpty { DEFAULT_GEMINI_API_KEY }
  }

  /**
   * Reads all stored JSON strings from `SharedPreferences` (Schedules, Tasks, Alarms, Flashcards,
   * User Profile, and all per-account keys) and combines them into a single encoded JSON string
   * for One-Tap Clipboard Backup & Device-Bound Auto-Persistence.
   */
  fun exportBackupJson(activeAccountKey: String = GUEST_ACCOUNT_KEY): String {
    val resolvedAccount = normalizeAccountKey(activeAccountKey.ifBlank { loadUserName() })
    val savedUser = prefs.getString(KEY_USER_NAME, "") ?: ""
    val darkMode = prefs.getBoolean(KEY_DARK_MODE, false)
    val registeredUsersJson = prefs.getString(KEY_REGISTERED_USERS, "{}") ?: "{}"
    val schedulesJson = prefs.getString(schedulesKeyFor(resolvedAccount), "[]") ?: "[]"
    val tasksJson = prefs.getString(tasksKeyFor(resolvedAccount), "[]") ?: "[]"
    val alarmsJson = prefs.getString(alarmsKeyFor(resolvedAccount), "[]") ?: "[]"
    val flashcardsJson = prefs.getString(flashcardDecksKeyFor(resolvedAccount), "[]") ?: "[]"

    val allPrefsObj = JSONObject()
    for ((key, value) in prefs.all) {
      if (value is String) {
        allPrefsObj.put(key, value)
      }
    }

    return JSONObject().apply {
      put("app", "Sibil")
      put("version", SIBIL_APP_VERSION_TAG)
      put("deviceId", getDeviceAndroidId())
      put("backupTimestamp", System.currentTimeMillis())
      put("userProfile", savedUser)
      put("activeAccountKey", resolvedAccount)
      put("isDarkMode", darkMode)
      put("registeredUsers", registeredUsersJson)
      put("schedules", schedulesJson)
      put("tasks", tasksJson)
      put("alarms", alarmsJson)
      put("flashcards", flashcardsJson)
      put("allPreferences", allPrefsObj)
    }.toString()
  }

  /**
   * Parses a backup JSON string (from Clipboard Restore or Device Auto-Restore), repopulates `SharedPreferences`
   * for Schedules, Tasks, Alarms, Flashcards, and User Profile, and returns `true` on success.
   */
  fun importBackupJson(
    rawBackupInput: String,
    fallbackAccountKey: String = GUEST_ACCOUNT_KEY,
    syncToDeviceAfterImport: Boolean = true
  ): Boolean {
    val trimmed = rawBackupInput.trim()
    if (trimmed.isEmpty()) return false

    return try {
      val normalizedJsonText = if (trimmed.startsWith("{")) {
        trimmed
      } else {
        String(Base64.decode(trimmed, Base64.DEFAULT), Charsets.UTF_8).trim()
      }
      val rootObj = JSONObject(normalizedJsonText)
      val editor = prefs.edit()

      // 1. Restore full string preferences map if included
      val allPrefsObj = rootObj.optJSONObject("allPreferences")
      if (allPrefsObj != null) {
        val keys = allPrefsObj.keys()
        while (keys.hasNext()) {
          val k = keys.next()
          val v = allPrefsObj.optString(k, "")
          editor.putString(k, v)
        }
      }

      // 2. Restore User Profile & Dark Mode
      val restoredUserProfile = rootObj.optString("userProfile", "").trim()
      if (restoredUserProfile.isNotEmpty()) {
        editor.putString(KEY_USER_NAME, restoredUserProfile)
      }
      if (rootObj.has("isDarkMode")) {
        editor.putBoolean(KEY_DARK_MODE, rootObj.optBoolean("isDarkMode", false))
      }

      val regUsersRaw = rootObj.optJSONObject("registeredUsers")?.toString()
        ?: rootObj.optString("registeredUsers", "").trim()
      if (regUsersRaw.isNotEmpty()) {
        editor.putString(KEY_REGISTERED_USERS, regUsersRaw)
      }

      // 3. Determine target account key for Schedules, Tasks, Alarms, and Flashcards
      val explicitAccountKey = rootObj.optString("activeAccountKey", "").trim()
      val targetAccountKey = normalizeAccountKey(
        when {
          explicitAccountKey.isNotEmpty() -> explicitAccountKey
          restoredUserProfile.isNotEmpty() -> restoredUserProfile
          else -> fallbackAccountKey
        }
      )

      val schedulesRaw = rootObj.optJSONArray("schedules")?.toString()
        ?: rootObj.optString("schedules", "").trim()
      if (schedulesRaw.isNotEmpty()) {
        editor.putString(schedulesKeyFor(targetAccountKey), schedulesRaw)
      }

      val tasksRaw = rootObj.optJSONArray("tasks")?.toString()
        ?: rootObj.optString("tasks", "").trim()
      if (tasksRaw.isNotEmpty()) {
        editor.putString(tasksKeyFor(targetAccountKey), tasksRaw)
      }

      val alarmsRaw = rootObj.optJSONArray("alarms")?.toString()
        ?: rootObj.optString("alarms", "").trim()
      if (alarmsRaw.isNotEmpty()) {
        editor.putString(alarmsKeyFor(targetAccountKey), alarmsRaw)
      }

      val flashcardsRaw = rootObj.optJSONArray("flashcards")?.toString()
        ?: rootObj.optString("flashcards", "").trim()
      if (flashcardsRaw.isNotEmpty()) {
        editor.putString(flashcardDecksKeyFor(targetAccountKey), flashcardsRaw)
      }

      editor.putInt(KEY_TASK_SCHEMA_VERSION, CURRENT_TASK_SCHEMA_VERSION)
      editor.putInt(KEY_FLASHCARD_SCHEMA_VERSION, CURRENT_FLASHCARD_SCHEMA_VERSION)
      editor.commit()
      if (syncToDeviceAfterImport) {
        syncToDeviceStorage(targetAccountKey)
      }
      true
    } catch (_: Exception) {
      false
    }
  }

  companion object {
    const val GUEST_ACCOUNT_KEY = "guest"
    const val GUEST_DISPLAY_NAME = "Guest User"
    private const val PREFS_NAME = "sibil_app_offline_storage"
    private const val KEY_REGISTERED_USERS = "registered_users_json"
    private const val KEY_TASKS_PREFIX = "tasks_"
    private const val KEY_SCHEDULES_PREFIX = "schedules_"
    private const val KEY_ALARMS_PREFIX = "alarms_"
    private const val KEY_FLASHCARDS_PREFIX = "flashcards_"
    private const val KEY_USER_NAME = "saved_user_name"
    private const val KEY_DARK_MODE = "saved_dark_mode"
    private const val KEY_GEMINI_API_KEY = "saved_gemini_api_key"
    private const val KEY_TASK_SCHEMA_VERSION = "task_schema_version"
    private const val CURRENT_TASK_SCHEMA_VERSION = 2
    private const val KEY_FLASHCARD_SCHEMA_VERSION = "flashcard_schema_version"
    private const val CURRENT_FLASHCARD_SCHEMA_VERSION = 2
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
 * 7. Account-segregated `FlashcardDeck` items for the Smart Flashcard reviewer (`flashcards_[username]`).
 * 8. Simulated Username Authentication, Device-Bound Auto-Persistence, & Guest Mode data wipe via `LocalStorageManager`.
 */
class TaskViewModel(
  private var storageManager: LocalStorageManager? = null
) : ViewModel() {
  var isDarkMode by mutableStateOf(false)
    private set

  var savedGeminiApiKey by mutableStateOf("")
    private set

  fun getActiveGeminiApiKey(): String {
    val inMemory = savedGeminiApiKey.trim()
    if (inMemory.isNotEmpty()) return inMemory
    val persisted = storageManager?.loadSavedGeminiApiKey().orEmpty().trim()
    if (persisted.isNotEmpty()) {
      savedGeminiApiKey = persisted
      return persisted
    }
    return DEFAULT_GEMINI_API_KEY
  }

  fun hasSavedGeminiApiKey(): Boolean {
    return savedGeminiApiKey.trim().isNotEmpty() || (storageManager?.hasSavedGeminiApiKey() == true)
  }

  fun saveGeminiApiKey(newKey: String) {
    val clean = newKey.trim()
    savedGeminiApiKey = clean
    storageManager?.saveGeminiApiKey(clean)
  }

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
  private var nextDeckId by mutableIntStateOf(1)
  private var nextCardId by mutableIntStateOf(1000)
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

  // Default state of the Flashcard Decks list is completely empty (no hardcoded placeholder decks)
  private val _flashcardDecks = mutableStateListOf<FlashcardDeck>()
  val flashcardDecks: List<FlashcardDeck> get() = _flashcardDecks

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
    savedGeminiApiKey = manager.loadSavedGeminiApiKey()

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
      loadFlashcardsForKey(manager, currentAccountKey)
    }
  }

  private fun loadFlashcardsForKey(manager: LocalStorageManager, accountKey: String) {
    val savedDecks = manager.loadFlashcardDecks(accountKey)
    _flashcardDecks.clear()
    _flashcardDecks.addAll(savedDecks)
    nextDeckId = (savedDecks.maxOfOrNull { it.id } ?: 0) + 1
    val maxCardId = savedDecks.flatMap { it.cards }.maxOfOrNull { it.id } ?: 999
    nextCardId = maxOf(1000, maxCardId + 1)
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

    loadFlashcardsForKey(manager, accountKey)

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
      loadAccountDataForKey(manager, currentAccountKey)
      manager.saveUserProfile(username)
    }
  }

  fun loginAsGuest() {
    username = LocalStorageManager.GUEST_DISPLAY_NAME
    hasSavedSession = true
    storageManager?.let { manager ->
      loadAccountDataForKey(manager, LocalStorageManager.GUEST_ACCOUNT_KEY)
      manager.saveUserProfile(username)
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
      _flashcardDecks.clear()
      nextId = 1
      nextScheduleId = 100
      nextAlarmId = 1
      nextDeckId = 1
      nextCardId = 1000
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

  /**
   * Schedules a temporary snooze alarm for 5 minutes from now (`Calendar.getInstance()`).
   */
  fun scheduleFiveMinuteSnoozeAlarm(baseTitle: String) {
    val cal = Calendar.getInstance().apply {
      add(Calendar.MINUTE, 5)
    }
    val hour24 = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val amPm = if (hour24 < 12) "AM" else "PM"
    val hour12 = when {
      hour24 == 0 -> 12
      hour24 > 12 -> hour24 - 12
      else -> hour24
    }
    val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
    val cleanLabel = baseTitle
      .removePrefix("Snoozed: ")
      .trim()
      .ifEmpty { "Alarm" }

    // Replace any previous temporary snooze alarm with the new 5-minute target
    _alarms.removeAll { it.name.startsWith("Snooze: ") }
    addAlarm(
      name = "Snooze: $cleanLabel",
      hour = String.format(Locale.US, "%02d", hour12),
      minute = String.format(Locale.US, "%02d", minute),
      amPm = amPm,
      activeDays = listOf(dayIndex),
      isActive = true
    )
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

  fun addFlashcardDeck(
    title: String,
    subjectTag: String,
    initialCards: List<FlashcardItem>
  ): FlashcardDeck {
    val cleanTitle = title.trim().ifEmpty { "Custom CE Deck" }
    val cleanTag = subjectTag.trim().ifEmpty { "General CE" }
    val normalizedCards = initialCards.map { card ->
      val assignedId = if (card.id <= 0) nextCardId++ else {
        if (card.id >= nextCardId) nextCardId = card.id + 1
        card.id
      }
      card.copy(
        id = assignedId,
        question = card.question.trim(),
        answer = card.answer.trim(),
        category = card.category.trim().ifEmpty { cleanTag },
        masteryLevel = card.masteryLevel.ifBlank { "New" }
      )
    }
    val newDeck = FlashcardDeck(
      id = nextDeckId++,
      title = cleanTitle,
      subjectTag = cleanTag,
      cards = normalizedCards
    )
    _flashcardDecks.add(0, newDeck)
    storageManager?.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
    return newDeck
  }

  /**
   * Generates a `FlashcardDeck` offline using `generateCardsFromText(rawText)` and saves it
   * immediately to `SharedPreferences`.
   */
  fun createDeckFromOfflineText(
    deckTitle: String,
    subjectTag: String,
    rawText: String
  ): FlashcardDeck {
    val cleanTitle = deckTitle
      .substringBeforeLast(".")
      .trim()
      .ifEmpty { "Study Notes Deck" }
    val cleanTag = subjectTag.trim().ifEmpty { "Offline Import" }
    val heuristicCards = generateCardsFromText(rawText = rawText, category = cleanTag).map { card ->
      card.copy(id = nextCardId++)
    }
    return addFlashcardDeck(
      title = cleanTitle,
      subjectTag = cleanTag,
      initialCards = heuristicCards
    )
  }

  fun addCardToDeck(
    deckId: Int,
    question: String,
    answer: String,
    category: String
  ) {
    val index = _flashcardDecks.indexOfFirst { it.id == deckId }
    val cleanQuestion = question.trim()
    val cleanAnswer = answer.trim()
    if (index != -1 && cleanQuestion.isNotEmpty() && cleanAnswer.isNotEmpty()) {
      val deck = _flashcardDecks[index]
      val cleanCategory = category.trim().ifEmpty { deck.subjectTag }
      val newCard = FlashcardItem(
        id = nextCardId++,
        question = cleanQuestion,
        answer = cleanAnswer,
        category = cleanCategory,
        masteryLevel = "New",
        isMastered = false
      )
      val updatedCards = deck.cards + newCard
      _flashcardDecks[index] = deck.copy(
        cards = updatedCards,
        masteredCardIds = updatedCards.filter { it.isMastered }.map { it.id }
      )
      storageManager?.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
    }
  }

  fun updateFlashcardMastery(
    deckId: Int,
    cardId: Int,
    newMasteryLevel: String
  ) {
    val deckIndex = _flashcardDecks.indexOfFirst { it.id == deckId }
    if (deckIndex != -1) {
      val currentDeck = _flashcardDecks[deckIndex]
      val isRatedGood = newMasteryLevel.equals("Good", ignoreCase = true)
      val updatedCards = currentDeck.cards.map { card ->
        if (card.id == cardId) {
          val updatedIsMastered = if (isRatedGood) true else card.isMastered
          card.masteryLevel = newMasteryLevel
          card.isMastered = updatedIsMastered
          card.copy(
            masteryLevel = newMasteryLevel,
            isMastered = updatedIsMastered
          )
        } else {
          card
        }
      }
      _flashcardDecks[deckIndex] = currentDeck.copy(
        cards = updatedCards,
        masteredCardIds = updatedCards.filter { it.isMastered }.map { it.id }
      )
      storageManager?.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
    }
  }

  fun resetDeckMastery(deckId: Int) {
    val deckIndex = _flashcardDecks.indexOfFirst { it.id == deckId }
    if (deckIndex != -1) {
      val currentDeck = _flashcardDecks[deckIndex]
      val resetCards = currentDeck.cards.map { card ->
        card.masteryLevel = "New"
        card.isMastered = false
        card.copy(masteryLevel = "New", isMastered = false)
      }
      _flashcardDecks[deckIndex] = currentDeck.copy(
        cards = resetCards,
        masteredCardIds = emptyList()
      )
      storageManager?.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
    }
  }

  fun deleteFlashcardDeck(deckId: Int) {
    _flashcardDecks.removeAll { it.id == deckId }
    storageManager?.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
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

  /**
   * Syncs active in-memory lists to `SharedPreferences` and returns a combined JSON backup string
   * containing Schedules, Tasks, Alarms, Flashcards, and User Profile.
   */
  fun createBackupJsonString(context: Context): String {
    val manager = storageManager ?: LocalStorageManager(context).also { storageManager = it }
    manager.saveTasks(_tasks, currentAccountKey)
    manager.saveSchedules(_classSchedules, currentAccountKey)
    manager.saveAlarms(_alarms, currentAccountKey)
    manager.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
    manager.saveDarkMode(isDarkMode)
    if (hasSavedSession || !isGuestMode) {
      manager.saveUserProfile(username)
    }
    return manager.exportBackupJson(currentAccountKey)
  }

  /**
   * Restores all Sibil data from a backup JSON string into `SharedPreferences`
   * and immediately refreshes the active ViewModel states.
   */
  fun restoreFromBackupJsonString(context: Context, backupJson: String): Boolean {
    val manager = storageManager ?: LocalStorageManager(context).also { storageManager = it }
    val success = manager.importBackupJson(backupJson, currentAccountKey)
    if (success) {
      hasLoadedFromStorage = false
      loadFromStorage(manager)
      loadAccountDataForKey(manager, currentAccountKey)
    }
    return success
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
 * Returns the translucent Liquid Glass card fill (`Color.White.copy(alpha = 0.68f)`) in Light Mode and dark glass in Dark Mode.
 */
@Composable
private fun dashboardCreamCardColor(): Color {
  return currentGlassPalette().cardFill
}

/**
 * Returns the translucent Liquid Glass inner pod fill in Light Mode and dark glass in Dark Mode.
 */
@Composable
private fun dashboardIvoryInnerColor(): Color {
  return currentGlassPalette().nestedCardFill
}

/**
 * Returns a frosted translucent track color for progress indicators on Liquid Glass cards.
 */
@Composable
private fun dashboardProgressTrackColor(): Color {
  return currentGlassPalette().trackColor
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

  val animatedGlassPalette = rememberAnimatedGlassPalette(isDark = resolvedViewModel.isDarkMode)

  CompositionLocalProvider(LocalGlassPalette provides animatedGlassPalette) {
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
  val context = LocalContext.current
  val metrics = rememberResponsiveMetrics()
  var selectedTab by rememberSaveable { mutableStateOf(CeNavTab.DASHBOARD) }
  var selectedScheduleWeekDay by rememberSaveable { mutableStateOf(resolveInitialScheduleWeekDay()) }
  var isAddTaskDialogOpen by rememberSaveable { mutableStateOf(false) }
  var isAddScheduleDialogOpen by rememberSaveable { mutableStateOf(false) }
  var editingSchedule by remember { mutableStateOf<ClassSchedule?>(null) }
  var isProfileSettingsDialogOpen by rememberSaveable { mutableStateOf(false) }
  var lastGlobalTriggeredAlarmKey by remember { mutableStateOf("") }

  // Continuous 1-second ticker checking active alarms and snooze countdown across the whole app
  LaunchedEffect(Unit) {
    while (true) {
      val cal = Calendar.getInstance()
      val currentHour24 = cal.get(Calendar.HOUR_OF_DAY)
      val currentMinute = cal.get(Calendar.MINUTE)
      val currentAmPm = if (currentHour24 < 12) "AM" else "PM"
      val currentHour12 = when {
        currentHour24 == 0 -> 12
        currentHour24 > 12 -> currentHour24 - 12
        else -> currentHour24
      }
      // Calendar.DAY_OF_WEEK is 1 (Sun) .. 7 (Sat) -> map to 0 (Sun) .. 6 (Sat)
      val currentDayIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)

      // Check active user alarms & snoozed alarms
      val activeAlarmsSnapshot = taskViewModel.alarms.toList()
      val matchingAlarm = activeAlarmsSnapshot.firstOrNull { alarm ->
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
        if (lastGlobalTriggeredAlarmKey != triggerKey) {
          lastGlobalTriggeredAlarmKey = triggerKey
          val formattedTime = "${matchingAlarm.hour}:${matchingAlarm.minute} ${matchingAlarm.amPm}"
          triggerClockAlert(
            context = context,
            title = matchingAlarm.name.ifBlank { "Alarm" },
            message = "Alarm ringing: $formattedTime (${matchingAlarm.name.ifBlank { "Alarm" }})"
          )
        }
      }

      // Also check any active 5-minute snooze target timestamp
      val targetSnooze = snoozeTargetEpochMillis
      if (targetSnooze != null && System.currentTimeMillis() >= targetSnooze) {
        val snoozedTitle = snoozeTargetTitle ?: "Snoozed Alarm"
        snoozeTargetEpochMillis = null
        snoozeTargetTitle = null
        triggerClockAlert(
          context = context,
          title = snoozedTitle,
          message = "$snoozedTitle (Snooze ended)"
        )
      }

      delay(1000L)
    }
  }

  // Return to Dashboard when pressing system Back on any secondary bottom tab
  if (selectedTab != CeNavTab.DASHBOARD) {
    BackHandler {
      selectedTab = CeNavTab.DASHBOARD
    }
  }

  LiquidGlassBackgroundMesh(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .testTag("main_scaffold"),
      containerColor = Color.Transparent,
      contentWindowInsets = WindowInsets.safeDrawing,
      floatingActionButton = {
        when (selectedTab) {
          CeNavTab.TASKS -> {
            Box(
              modifier = Modifier
                .size(62.dp)
                .shadow(
                  elevation = 14.dp,
                  shape = CircleShape,
                  ambientColor = Color(0x284338CA),
                  spotColor = Color(0x404F46E5)
                )
                .clip(CircleShape)
                .background(brush = LiquidOrbBrush, shape = CircleShape)
                .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.65f)), CircleShape)
                .clickable { isAddTaskDialogOpen = true }
                .testTag("add_task_fab"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_task_fab_label),
                tint = Color.White,
                modifier = Modifier.size(28.dp)
              )
            }
          }
          CeNavTab.SCHEDULE -> {
            Box(
              modifier = Modifier
                .size(62.dp)
                .shadow(
                  elevation = 14.dp,
                  shape = CircleShape,
                  ambientColor = Color(0x284338CA),
                  spotColor = Color(0x404F46E5)
                )
                .clip(CircleShape)
                .background(brush = LiquidOrbBrush, shape = CircleShape)
                .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.65f)), CircleShape)
                .clickable {
                  editingSchedule = null
                  isAddScheduleDialogOpen = true
                }
                .testTag("add_schedule_fab"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_schedule_fab_label),
                tint = Color.White,
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
            viewModel = taskViewModel,
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

      val currentRingingTitle = ringingAlertTitle
      if (currentRingingTitle != null) {
        RingingAlertDialog(
          alertTitle = currentRingingTitle,
          alertDetail = activeRingingAlertMessage,
          metrics = metrics,
          onStop = {
            AlarmSoundController.stopRinging()
            try {
              NotificationManagerCompat.from(context).cancel(TIMER_NOTIFICATION_ID)
            } catch (_: Exception) {
            }
          },
          onSnoozeFiveMinutes = {
            AlarmSoundController.stopRinging()
            try {
              NotificationManagerCompat.from(context).cancel(TIMER_NOTIFICATION_ID)
            } catch (_: Exception) {
            }
            snoozeTargetEpochMillis = System.currentTimeMillis() + 5L * 60L * 1000L
            snoozeTargetTitle = currentRingingTitle
            taskViewModel.scheduleFiveMinuteSnoozeAlarm(currentRingingTitle)
            scheduleTimerExactAlarm(
              context = context,
              durationSeconds = 5L * 60L,
              label = "$currentRingingTitle (Snooze)"
            )
            Toast.makeText(context, "Snoozed for 5 minutes", Toast.LENGTH_SHORT).show()
          }
        )
      }
    }
  }
}

/**
 * ============================================================================
 * 2. THE HIGHLIGHT: FLOATING LIQUID GLASS TASK BAR ("LIQUID GLASS DOCK")
 * ============================================================================
 * - Floats detached above the bottom screen edge with
 *   `Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp)`.
 * - Dock Surface:
 *   * Rounded pill capsule (`RoundedCornerShape(32.dp)`).
 *   * Liquid glass material: `Color.White.copy(alpha = 0.72f)` background with a top-lit specular rim
 *     (`BorderStroke(1.5.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.3f))))`).
 *   * Soft atmospheric glow/shadow underneath (`elevation = 16.dp`).
 * - Center Dashboard "Liquid Orb" Button:
 *   * Sits prominently raised at the center of the dock (`RoundedCornerShape(22.dp)`, `elevation = 14.dp`).
 *   * Fluid Gradient Fill: `Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF4338CA)))`.
 *   * Specular Glass Sheen: `BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f))`.
 *   * Clean white icon that pops with high contrast.
 * - Side Nav Icons (Clock, Extras, Tasks, Schedule):
 *   * Active: Glowing translucent pill indicator (`Color(0xFF6366F1).copy(alpha = 0.15f)`) with saturated brand tint.
 *   * Inactive: Translucent charcoal tint (`Color(0xFF475569).copy(alpha = 0.65f)`) with clean micro-typography.
 */
@Composable
private fun CustomDockedBottomTaskBar(
  selectedTab: CeNavTab,
  onSelectTab: (CeNavTab) -> Unit,
  metrics: ResponsiveMetrics,
  modifier: Modifier = Modifier
) {
  val palette = currentGlassPalette()
  val dockShape = RoundedCornerShape(32.dp)
  val dockBackgroundColor = palette.dockFill
  val dockSpecularBorder = if (!palette.isDark) {
    BorderStroke(
      width = 1.5.dp,
      color = palette.dockBorderColor
    )
  } else {
    BorderStroke(
      width = 1.5.dp,
      brush = Brush.verticalGradient(
        colors = listOf(
          Color.White.copy(alpha = 0.35f),
          Color(0xFF6366F1).copy(alpha = 0.25f)
        )
      )
    )
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp, vertical = 10.dp)
      .testTag("bottom_navigation_bar"),
    contentAlignment = Alignment.BottomCenter
  ) {
    // Detached Floating Pill Capsule Dock Surface
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = metrics.maxContentWidth)
        .padding(top = 16.dp)
        .shadow(
          elevation = 16.dp,
          shape = dockShape,
          ambientColor = palette.shadowAmbient,
          spotColor = palette.shadowSpot
        )
        .clip(dockShape)
        .background(
          color = dockBackgroundColor,
          shape = dockShape
        )
        .border(border = dockSpecularBorder, shape = dockShape)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = metrics.scaledDp(10), vertical = 10.dp),
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

        // Center Spacer reserved for the raised Liquid Orb Dashboard button
        Spacer(modifier = Modifier.width(metrics.scaledDp(74)))

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

    // Center Dashboard "Liquid Orb" Button raised prominently at the center of the dock
    val isDashboardSelected = selectedTab == CeNavTab.DASHBOARD
    val orbShape = RoundedCornerShape(22.dp)
    val orbBrush = Brush.linearGradient(
      colors = listOf(
        Color(0xFF6366F1),
        Color(0xFF4F46E5),
        Color(0xFF4338CA)
      )
    )

    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(bottom = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Box(
        modifier = Modifier
          .size(metrics.scaledDp(58))
          .shadow(
            elevation = 14.dp,
            shape = orbShape,
            ambientColor = Color(0x334338CA),
            spotColor = Color(0x554F46E5)
          )
          .clip(orbShape)
          .background(brush = orbBrush, shape = orbShape)
          .border(
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
            shape = orbShape
          )
          .clickable { onSelectTab(CeNavTab.DASHBOARD) }
          .testTag(CeNavTab.DASHBOARD.routeTag),
        contentAlignment = Alignment.Center
      ) {
        // Subtle top-half inner glass highlight sheen inside the Liquid Orb
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(metrics.scaledDp(26))
            .align(Alignment.TopCenter)
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color.White.copy(alpha = 0.28f),
                  Color.Transparent
                )
              )
            )
        )

        Icon(
          imageVector = if (isDashboardSelected) {
            CeNavTab.DASHBOARD.selectedIcon
          } else {
            CeNavTab.DASHBOARD.unselectedIcon
          },
          contentDescription = CeNavTab.DASHBOARD.title,
          tint = Color.White,
          modifier = Modifier.size(27.dp)
        )
      }

      Text(
        text = CeNavTab.DASHBOARD.title,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = metrics.scaledSp(11),
          fontWeight = if (isDashboardSelected) FontWeight.ExtraBold else FontWeight.SemiBold
        ),
        color = if (isDashboardSelected) {
          palette.accentIndigo
        } else {
          palette.textSecondary
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
  val palette = currentGlassPalette()
  val activePillColor = Color(0xFF6366F1).copy(alpha = 0.16f)
  val saturatedBrandColor = palette.accentIndigo
  val inactiveTint = palette.textSecondary

  val animatedIconTint by animateColorAsState(
    targetValue = if (isSelected) saturatedBrandColor else inactiveTint,
    animationSpec = tween(durationMillis = 220),
    label = "nav_icon_tint_${tab.name}"
  )
  val animatedTextColor by animateColorAsState(
    targetValue = if (isSelected) saturatedBrandColor else inactiveTint,
    animationSpec = tween(durationMillis = 220),
    label = "nav_text_color_${tab.name}"
  )

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .minimumInteractiveComponentSize()
      .padding(vertical = 3.dp, horizontal = 2.dp)
      .testTag(tab.routeTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    Box(
      modifier = Modifier
        .height(32.dp)
        .width(54.dp)
        .clip(CircleShape)
        .background(if (isSelected) activePillColor else Color.Transparent)
        .then(
          if (isSelected) {
            Modifier.border(
              BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.35f)),
              CircleShape
            )
          } else {
            Modifier
          }
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
        contentDescription = tab.title,
        tint = animatedIconTint,
        modifier = Modifier.size(21.dp)
      )
    }

    Text(
      text = tab.title,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = metrics.scaledSp(10),
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
      ),
      color = animatedTextColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

/**
 * Main Academic Dashboard Screen styled with the Liquid Glass (Glassmorphic) design system:
 * - Layered over the root `LiquidGlassBackgroundMesh` so ambient glows shine through.
 * - Real Open-Meteo Weather Summary & live ticking `LocalDateTime` header with frosted glass badges.
 * - Clickable upper-right Profile Icon opening the Settings Dialog.
 * - Side-by-side frosted glass pods for "Daily Progress" (with Cyan-to-Electric-Violet gradient arc)
 *   and "Today's Schedule".
 * - Dynamic "Upcoming Deadlines" translucent glass tiles with subtle glowing status dots.
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
  val palette = currentGlassPalette()

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

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    // Ambient geometric glass light refractions behind dashboard content
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .testTag("dashboard_geometric_canvas")
    ) {
      val w = size.width
      val h = size.height

      drawCircle(
        color = LiquidRoyalPeriwinkle.copy(alpha = 0.06f),
        radius = w * 0.44f,
        center = Offset(x = w * 0.88f, y = h * 0.12f)
      )
      drawCircle(
        color = LiquidCyanGlow.copy(alpha = 0.05f),
        radius = w * 0.36f,
        center = Offset(x = w * 0.14f, y = h * 0.34f)
      )
      drawCircle(
        color = LiquidElectricViolet.copy(alpha = 0.05f),
        radius = w * 0.46f,
        center = Offset(x = w * 0.82f, y = h * 0.82f)
      )

      rotate(degrees = -14f, pivot = Offset(w * 0.25f, h * 0.18f)) {
        drawRoundRect(
          color = Color.White.copy(alpha = if (palette.isDark) 0.05f else 0.22f),
          topLeft = Offset(x = -w * 0.08f, y = h * 0.08f),
          size = Size(width = w * 0.56f, height = h * 0.14f),
          cornerRadius = CornerRadius(36f, 36f)
        )
      }
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
      // 1. Dynamic Top Header inside a Frosted Liquid Glass Banner Pod
      item {
        LiquidGlassCard(
          cornerRadius = 26.dp,
          elevation = 8.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(
                horizontal = metrics.scaledDp(18),
                vertical = metrics.scaledDp(16)
              ),
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
                  fontSize = metrics.scaledSp(15),
                  fontWeight = FontWeight.Bold
                ),
                color = palette.accentIndigo,
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
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(brush = LiquidCyanVioletGradient)
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)), RoundedCornerShape(14.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Filled.Cloud,
                    contentDescription = "Live Weather Icon",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                  )
                }

                Text(
                  text = viewModel.weatherText,
                  style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = metrics.scaledSp(23),
                    lineHeight = metrics.scaledSp(29),
                    fontWeight = FontWeight.Bold
                  ),
                  color = palette.textPrimary,
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
                  fontSize = metrics.scaledSp(14),
                  fontWeight = FontWeight.SemiBold
                ),
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("dashboard_datetime_header")
              )
            }

            LiquidGlassContainer(
              cornerRadius = 24.dp,
              fillAlpha = 0.65f,
              onClick = onProfileIconClick,
              modifier = Modifier
                .size(48.dp)
                .minimumInteractiveComponentSize()
                .testTag("dashboard_profile_icon_button")
            ) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.AccountCircle,
                  contentDescription = "Open Profile & Settings",
                  tint = palette.accentIndigo,
                  modifier = Modifier.size(32.dp)
                )
              }
            }
          }
        }
      }

      // 2. Middle Section (Side-by-Side): Frosted Glass Daily Progress Pod + Today's Schedule Pod
      item {
        val middleRowHeight = metrics.scaledDp(278)
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

      // 3. Bottom Section: Dynamic "Upcoming Deadlines" Task List in Frosted Glass
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
 * Frosted Glass Daily Progress Pod (`LiquidGlassCard`) featuring a vibrant
 * Cyan-to-Electric-Violet gradient progress arc contrasting against a frosted circular track.
 */
@Composable
private fun CompactDailyProgressCard(
  completedCount: Int,
  totalCount: Int,
  metrics: ResponsiveMetrics,
  modifier: Modifier = Modifier
) {
  val palette = currentGlassPalette()
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
  val ringTrackColor = palette.trackColor

  LiquidGlassCard(
    cornerRadius = 26.dp,
    elevation = 8.dp,
    modifier = modifier.testTag("daily_study_progress_card")
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
          color = palette.textPrimary,
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
        Canvas(modifier = Modifier.fillMaxSize()) {
          val strokeWidth = 11.dp.toPx()
          val diameter = size.minDimension - strokeWidth
          val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
          val arcSize = Size(diameter, diameter)

          // Frosted circular track
          drawArc(
            color = ringTrackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          // Vibrant Cyan to Electric Violet gradient progress arc
          if (animatedProgress > 0f) {
            drawArc(
              brush = Brush.linearGradient(
                colors = listOf(
                  LiquidCyanGlow,
                  LiquidRoyalPeriwinkle,
                  LiquidElectricViolet
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
              ),
              startAngle = -90f,
              sweepAngle = 360f * animatedProgress,
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
          }
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
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "$completedCount/$totalCount Done",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = metrics.scaledSp(11),
              fontWeight = FontWeight.Bold
            ),
            color = palette.textSecondary,
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
          color = LiquidElectricViolet,
          trackColor = ringTrackColor
        )
        Text(
          text = "${(totalCount - completedCount).coerceAtLeast(0)} tasks remaining",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = metrics.scaledSp(11),
            fontWeight = FontWeight.SemiBold
          ),
          color = palette.textSecondary,
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
 * Dynamic Frosted Glass "Today's Schedule" Pod on the Main Dashboard (`LiquidGlassCard`).
 */
@Composable
private fun TodaysScheduleInteractiveCard(
  todaysClasses: List<ClassSchedule>,
  todayAbbreviation: String,
  metrics: ResponsiveMetrics,
  modifier: Modifier = Modifier
) {
  val palette = currentGlassPalette()
  var selectedClassId by rememberSaveable { mutableIntStateOf(todaysClasses.firstOrNull()?.id ?: -1) }

  LiquidGlassCard(
    cornerRadius = 26.dp,
    elevation = 8.dp,
    modifier = modifier.testTag("todays_schedule_card")
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
          color = palette.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(LiquidRoyalPeriwinkle.copy(alpha = 0.16f))
            .border(BorderStroke(1.dp, LiquidRoyalPeriwinkle.copy(alpha = 0.35f)), CircleShape)
        ) {
          Text(
            text = "$todayAbbreviation • ${todaysClasses.size}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = metrics.scaledSp(10),
              fontWeight = FontWeight.Bold
            ),
            color = palette.accentIndigo,
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
            color = palette.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("todays_schedule_empty_text")
          )
        }
      } else {
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
            LiquidGlassContainer(
              cornerRadius = 14.dp,
              fillAlpha = if (isSelected) 0.85f else 0.72f,
              borderStroke = BorderStroke(
                width = if (isSelected) 1.4.dp else 1.dp,
                color = if (isSelected) accentColor.copy(alpha = 0.75f) else palette.nestedBorderColor
              ),
              onClick = { selectedClassId = item.id },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("schedule_item_${item.id}")
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
                      .size(9.dp)
                      .shadow(4.dp, CircleShape, spotColor = accentColor)
                      .clip(CircleShape)
                      .background(accentColor)
                  )
                  Text(
                    text = item.subjectName,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = metrics.scaledSp(12),
                      fontWeight = FontWeight.Bold
                    ),
                    color = palette.textPrimary,
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
                    palette.accentIndigo
                  } else {
                    palette.textSecondary
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
                    tint = palette.accentIndigo,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = item.room,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = metrics.scaledSp(11),
                      fontWeight = FontWeight.Bold
                    ),
                    color = palette.accentIndigo,
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
 * Frosted Glass "Upcoming Deadlines" section (`LiquidGlassCard`) with translucent glass tiles
 * and subtle glowing status dots.
 */
@Composable
private fun UpcomingDeadlinesSectionCard(
  activeTasks: List<Task>,
  metrics: ResponsiveMetrics,
  onToggleComplete: (Int) -> Unit
) {
  val palette = currentGlassPalette()
  LiquidGlassCard(
    cornerRadius = 26.dp,
    elevation = 8.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("upcoming_deadlines_section_card")
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
          color = palette.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .weight(1f)
            .testTag("upcoming_deadlines_header")
        )

        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(LiquidRoyalPeriwinkle.copy(alpha = 0.15f))
            .border(BorderStroke(1.dp, LiquidRoyalPeriwinkle.copy(alpha = 0.35f)), CircleShape)
        ) {
          Text(
            text = "${activeTasks.size} Active",
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.Bold
            ),
            color = palette.accentIndigo,
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
            color = palette.textSecondary,
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
 * Translucent glass tile (`LiquidGlassContainer`) for each active task in Upcoming Deadlines,
 * featuring a subtle glowing status dot and frosted category pill.
 */
@Composable
private fun DashboardTaskMiniCard(
  task: Task,
  metrics: ResponsiveMetrics,
  onToggleComplete: () -> Unit
) {
  val palette = currentGlassPalette()
  LiquidGlassContainer(
    cornerRadius = 18.dp,
    fillAlpha = 0.75f,
    onClick = onToggleComplete,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dashboard_deadline_card_${task.id}")
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
      // Glowing status dot + interactive completion ring
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(palette.checkboxUncheckedFill)
          .border(
            BorderStroke(1.5.dp, palette.checkboxUncheckedBorder),
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(9.dp)
            .shadow(4.dp, CircleShape, spotColor = LiquidCyanGlow)
            .clip(CircleShape)
            .background(brush = LiquidCyanVioletGradient)
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
          color = palette.textPrimary,
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
            tint = palette.accentIndigo,
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = "Due ${task.dueDate}",
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.SemiBold
            ),
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
          Box(
            modifier = Modifier
              .clip(CircleShape)
              .background(LiquidRoyalPeriwinkle.copy(alpha = 0.16f))
              .border(BorderStroke(1.dp, LiquidRoyalPeriwinkle.copy(alpha = 0.35f)), CircleShape)
          ) {
            Text(
              text = task.subjectCategory.ifBlank { DEFAULT_TASK_CATEGORY },
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = metrics.scaledSp(10),
                fontWeight = FontWeight.Bold
              ),
              color = palette.accentIndigo,
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
 * Contains the engineering tool cards arranged in a neat 2x2 grid with floating elevation
 * and cream card styling (`#FFFBF0`):
 * - Scientific Calculator
 * - Unit Converter
 * - BOQ Estimator
 * - Smart Flashcards (Active-recall CE decks, PDF/DOCX AI import simulator, & spaced-repetition study mode)
 */
@Composable
fun ExtrasScreen(
  modifier: Modifier = Modifier,
  viewModel: TaskViewModel = viewModel()
) {
  val context = LocalContext.current
  val localStorageManager = remember(context) {
    LocalStorageManager(context.applicationContext)
  }
  LaunchedEffect(localStorageManager) {
    viewModel.attachStorage(localStorageManager)
  }

  val metrics = rememberResponsiveMetrics()
  val palette = currentGlassPalette()
  var selectedToolTitle by rememberSaveable { mutableStateOf<String?>(null) }
  var isFlashcardsOpen by rememberSaveable { mutableStateOf(false) }
  var isFormulaVaultOpen by rememberSaveable { mutableStateOf(false) }
  var activeStudyDeckId by rememberSaveable { mutableStateOf<Int?>(null) }

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
        id = "smart_flashcards",
        title = "Smart Flashcards",
        subtitle = "CE active-recall & PDF import",
        icon = Icons.Filled.AutoAwesome
      ),
      ExtraToolItem(
        id = "ce_formula_vault",
        title = "CE Formula Vault",
        subtitle = "Interactive structural & hydro solvers",
        icon = Icons.Filled.Functions
      )
    )
  }

  val activeStudyDeck = viewModel.flashcardDecks.firstOrNull { it.id == activeStudyDeckId }

  if (isFormulaVaultOpen) {
    FormulaVaultMainView(
      metrics = metrics,
      onBackToExtras = { isFormulaVaultOpen = false },
      modifier = modifier
    )
    return
  }

  if (isFlashcardsOpen) {
    if (activeStudyDeck != null) {
      FlashcardStudyScreen(
        deck = activeStudyDeck,
        metrics = metrics,
        onRateCard = { cardId, rating ->
          viewModel.updateFlashcardMastery(
            deckId = activeStudyDeck.id,
            cardId = cardId,
            newMasteryLevel = rating
          )
        },
        onResetMastery = {
          viewModel.resetDeckMastery(activeStudyDeck.id)
        },
        onBackToDecks = { activeStudyDeckId = null },
        modifier = modifier
      )
    } else {
      FlashcardMainView(
        viewModel = viewModel,
        metrics = metrics,
        onBackToExtras = { isFlashcardsOpen = false },
        onStartReview = { deck -> activeStudyDeckId = deck.id },
        modifier = modifier
      )
    }
    return
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
        color = palette.textPrimary,
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
        color = palette.textSecondary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // 3D Frosted Glass Engineering Tool Grid
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
          onClick = { isFlashcardsOpen = true },
          modifier = Modifier.weight(1f)
        )
      }

      // Featured Full-Width CE Formula Vault Card
      ExtraToolPlaceholderCard(
        tool = extraTools[4],
        metrics = metrics,
        onClick = { isFormulaVaultOpen = true },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }

  if (selectedToolTitle != null) {
    AlertDialog(
      onDismissRequest = { selectedToolTitle = null },
      shape = RoundedCornerShape(28.dp),
      containerColor = palette.dialogSurface,
      title = {
        Text(
          text = selectedToolTitle ?: "",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = metrics.scaledSp(22),
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary,
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
          color = palette.textSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = { selectedToolTitle = null },
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = palette.accentIndigo,
            contentColor = Color.White
          )
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

private data class CeFormulaEntry(
  val id: String,
  val title: String,
  val category: String,
  val equationDisplay: String,
  val description: String,
  val param1Label: String,
  val param1Default: String,
  val param2Label: String,
  val param2Default: String,
  val param3Label: String,
  val param3Default: String,
  val outputUnit: String,
  val compute: (Double, Double, Double) -> Double
)

@Composable
fun FormulaVaultMainView(
  metrics: ResponsiveMetrics,
  onBackToExtras: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler {
    onBackToExtras()
  }

  val palette = currentGlassPalette()
  val formulas = remember {
    listOf(
      CeFormulaEntry(
        id = "beam_bending_moment",
        title = "Simply Supported Beam Max Moment",
        category = "Structural",
        equationDisplay = "M_max = (w · L²) / 8",
        description = "Calculates maximum midspan bending moment under uniform distributed load w over span L.",
        param1Label = "Uniform Load w (kN/m)",
        param1Default = "24.0",
        param2Label = "Span Length L (m)",
        param2Default = "6.0",
        param3Label = "Load Factor γ (optional, 1.0 = service)",
        param3Default = "1.0",
        outputUnit = "kN·m",
        compute = { w, l, gamma -> (w * l.pow(2.0) / 8.0) * gamma }
      ),
      CeFormulaEntry(
        id = "manning_open_channel",
        title = "Manning's Open Channel Velocity",
        category = "Hydraulics",
        equationDisplay = "V = (1 / n) · R^(2/3) · S^(1/2)",
        description = "Determines uniform flow velocity in open channels and storm drainage conduits.",
        param1Label = "Manning Roughness n",
        param1Default = "0.013",
        param2Label = "Hydraulic Radius R (m)",
        param2Default = "0.85",
        param3Label = "Channel Slope S (m/m)",
        param3Default = "0.004",
        outputUnit = "m/s",
        compute = { n, r, s ->
          if (n <= 0.0 || r < 0.0 || s < 0.0) 0.0
          else (1.0 / n) * r.pow(2.0 / 3.0) * sqrt(s)
        }
      ),
      CeFormulaEntry(
        id = "terzaghi_effective_stress",
        title = "Terzaghi Vertical Effective Stress",
        category = "Geotech",
        equationDisplay = "σ'_v = (γ_sat · z) - (γ_w · h_w)",
        description = "Computes effective intergranular soil stress at depth z with piezometric head h_w.",
        param1Label = "Unit Weight γ_sat (kN/m³)",
        param1Default = "19.5",
        param2Label = "Soil Depth z (m)",
        param2Default = "5.0",
        param3Label = "Pore Water Head h_w (m)",
        param3Default = "3.0",
        outputUnit = "kPa",
        compute = { gammaSat, z, hw -> (gammaSat * z) - (9.81 * hw) }
      ),
      CeFormulaEntry(
        id = "aci_concrete_modulus",
        title = "ACI 318 Concrete Elastic Modulus",
        category = "Concrete",
        equationDisplay = "E_c = 4700 · √f'_c  (MPa)",
        description = "Normal-weight concrete secant modulus of elasticity per ACI 318 / NSCP 2015.",
        param1Label = "Specified Strength f'_c (MPa)",
        param1Default = "28.0",
        param2Label = "Unit Weight Factor (1.0 normal)",
        param2Default = "1.0",
        param3Label = "Safety Multiplier (1.0 nominal)",
        param3Default = "1.0",
        outputUnit = "MPa",
        compute = { fc, factor, mult ->
          if (fc <= 0.0) 0.0 else 4700.0 * sqrt(fc) * factor * mult
        }
      ),
      CeFormulaEntry(
        id = "mohr_coulomb_shear",
        title = "Mohr-Coulomb Soil Shear Strength",
        category = "Geotech",
        equationDisplay = "τ_f = c' + σ'_n · tan(φ')",
        description = "Drained shear strength envelope along failure plane given cohesion c' and friction angle φ'.",
        param1Label = "Effective Cohesion c' (kPa)",
        param1Default = "15.0",
        param2Label = "Effective Normal Stress σ'_n (kPa)",
        param2Default = "120.0",
        param3Label = "Friction Angle φ' (degrees)",
        param3Default = "32.0",
        outputUnit = "kPa",
        compute = { c, sigma, phiDeg ->
          c + sigma * tan(phiDeg * PI / 180.0)
        }
      ),
      CeFormulaEntry(
        id = "euler_buckling_load",
        title = "Euler Critical Column Buckling",
        category = "Structural",
        equationDisplay = "P_cr = (π² · E · I) / (K · L)²",
        description = "Elastic buckling capacity for slender compression members (output in kN).",
        param1Label = "Flexural Rigidity E·I (kN·m²)",
        param1Default = "12500.0",
        param2Label = "Unbraced Length L (m)",
        param2Default = "4.5",
        param3Label = "Effective Length Factor K",
        param3Default = "1.0",
        outputUnit = "kN",
        compute = { ei, l, k ->
          val kl = k * l
          if (kl <= 0.0) 0.0 else (PI.pow(2.0) * ei) / kl.pow(2.0)
        }
      )
    )
  }

  val categories = remember { listOf("All", "Structural", "Hydraulics", "Geotech", "Concrete") }
  var selectedCategory by rememberSaveable { mutableStateOf("All") }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var activeQuickSolveFormulaId by rememberSaveable { mutableStateOf<String?>(null) }

  val filteredFormulas = remember(formulas, selectedCategory, searchQuery) {
    formulas.filter { entry ->
      val matchesCategory = selectedCategory == "All" || entry.category.equals(selectedCategory, ignoreCase = true)
      val q = searchQuery.trim()
      val matchesSearch = q.isEmpty() ||
        entry.title.contains(q, ignoreCase = true) ||
        entry.equationDisplay.contains(q, ignoreCase = true) ||
        entry.description.contains(q, ignoreCase = true) ||
        entry.category.contains(q, ignoreCase = true)
      matchesCategory && matchesSearch
    }
  }

  val activeFormula = formulas.firstOrNull { it.id == activeQuickSolveFormulaId }

  Column(
    modifier = modifier
      .fillMaxSize()
      .widthIn(max = metrics.maxContentWidth)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = metrics.scaledDp(20), vertical = metrics.scaledDp(20))
      .testTag("formula_vault_screen"),
    verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
  ) {
    // Top Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Surface(
        onClick = onBackToExtras,
        shape = CircleShape,
        color = palette.cardFill,
        border = palette.glassBorder,
        shadowElevation = 6.dp,
        modifier = Modifier
          .size(48.dp)
          .testTag("formula_vault_back_button")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Back to Extras",
            tint = palette.textPrimary,
            modifier = Modifier.size(26.dp)
          )
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "CE Formula Vault",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = metrics.scaledSp(22),
            fontWeight = FontWeight.ExtraBold
          ),
          color = palette.textPrimary
        )
        Text(
          text = "Frosted Reference & Instant Quick-Solve Engine",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = metrics.scaledSp(12),
            fontWeight = FontWeight.SemiBold
          ),
          color = palette.accentIndigo
        )
      }
    }

    // Frosted Glass Search Bar
    LiquidGlassCard(
      shape = RoundedCornerShape(22.dp),
      elevation = 6.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            text = "Search beam, Manning, Terzaghi, ACI...",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = metrics.scaledSp(14)),
            color = palette.textSecondary
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search formulas",
            tint = palette.accentIndigo
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Clear search",
                tint = palette.textSecondary
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(22.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = palette.textPrimary,
          unfocusedTextColor = palette.textPrimary,
          focusedContainerColor = Color.Transparent,
          unfocusedContainerColor = Color.Transparent,
          focusedBorderColor = Color.Transparent,
          unfocusedBorderColor = Color.Transparent,
          cursorColor = palette.accentIndigo
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("formula_vault_search_input")
      )
    }

    // Floating Glass Category Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categories.forEach { category ->
        val isSelected = selectedCategory == category
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(
              brush = if (isSelected) {
                LiquidVioletChipGradient
              } else {
                Brush.linearGradient(
                  listOf(
                    palette.nestedCardFill,
                    palette.cardFill
                  )
                )
              }
            )
            .border(
              border = if (isSelected) {
                BorderStroke(1.2.dp, Color.White.copy(alpha = 0.65f))
              } else {
                palette.nestedBorder
              },
              shape = CircleShape
            )
            .clickable { selectedCategory = category }
            .padding(horizontal = 16.dp, vertical = 9.dp)
            .testTag("formula_category_chip_$category")
        ) {
          Text(
            text = category,
            style = MaterialTheme.typography.labelLarge.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.Bold
            ),
            color = if (isSelected) Color.White else palette.textPrimary
          )
        }
      }
    }

    // Formula Cards List
    filteredFormulas.forEach { formula ->
      LiquidGlassCard(
        onClick = { activeQuickSolveFormulaId = formula.id },
        shape = RoundedCornerShape(26.dp),
        elevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("formula_card_${formula.id}")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(metrics.scaledDp(18)),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(LiquidRoyalPeriwinkle.copy(alpha = 0.16f))
                .border(BorderStroke(1.dp, LiquidRoyalPeriwinkle.copy(alpha = 0.35f)), CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = formula.category.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = metrics.scaledSp(10),
                  fontWeight = FontWeight.ExtraBold
                ),
                color = palette.accentIndigo
              )
            }

            Text(
              text = "Tap to Quick-Solve →",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = metrics.scaledSp(11),
                fontWeight = FontWeight.Bold
              ),
              color = palette.accentIndigo
            )
          }

          Text(
            text = formula.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = metrics.scaledSp(17),
              fontWeight = FontWeight.Bold
            ),
            color = palette.textPrimary
          )

          LiquidGlassContainer(
            shape = RoundedCornerShape(16.dp),
            fillAlpha = 0.75f,
            elevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = formula.equationDisplay,
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = metrics.scaledSp(15),
                fontWeight = FontWeight.ExtraBold
              ),
              color = palette.accentIndigo,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
          }

          Text(
            text = formula.description,
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = metrics.scaledSp(12),
              lineHeight = metrics.scaledSp(17),
              fontWeight = FontWeight.Medium
            ),
            color = palette.textSecondary
          )
        }
      }
    }
  }

  // Quick-Solve Translucent Modal Sheet
  if (activeFormula != null) {
    FormulaQuickSolveModalSheet(
      formula = activeFormula,
      metrics = metrics,
      onDismiss = { activeQuickSolveFormulaId = null }
    )
  }
}

@Composable
private fun FormulaQuickSolveModalSheet(
  formula: CeFormulaEntry,
  metrics: ResponsiveMetrics,
  onDismiss: () -> Unit
) {
  val palette = currentGlassPalette()
  var param1Text by rememberSaveable(formula.id) { mutableStateOf(formula.param1Default) }
  var param2Text by rememberSaveable(formula.id) { mutableStateOf(formula.param2Default) }
  var param3Text by rememberSaveable(formula.id) { mutableStateOf(formula.param3Default) }

  val p1 = param1Text.toDoubleOrNull() ?: 0.0
  val p2 = param2Text.toDoubleOrNull() ?: 0.0
  val p3 = param3Text.toDoubleOrNull() ?: 0.0
  val computedResult = remember(formula, p1, p2, p3) {
    formula.compute(p1, p2, p3)
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(30.dp),
      color = palette.dialogSurface,
      border = palette.glassBorder,
      shadowElevation = 16.dp,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = 480.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = formula.title,
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = metrics.scaledSp(19),
                fontWeight = FontWeight.ExtraBold
              ),
              color = palette.textPrimary
            )
            Text(
              text = formula.equationDisplay,
              style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = metrics.scaledSp(13),
                fontWeight = FontWeight.Bold
              ),
              color = palette.accentIndigo
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close quick solver",
              tint = palette.textSecondary
            )
          }
        }

        OutlinedTextField(
          value = param1Text,
          onValueChange = { param1Text = it },
          label = { Text(formula.param1Label) },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = param2Text,
          onValueChange = { param2Text = it },
          label = { Text(formula.param2Label) },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = param3Text,
          onValueChange = { param3Text = it },
          label = { Text(formula.param3Label) },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier.fillMaxWidth()
        )

        LiquidGlassContainer(
          shape = RoundedCornerShape(22.dp),
          fillAlpha = 0.78f,
          elevation = 6.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "COMPUTED OUTPUT",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = metrics.scaledSp(10),
                  fontWeight = FontWeight.ExtraBold
                ),
                color = palette.textSecondary
              )
              Text(
                text = String.format(Locale.US, "%.3f %s", computedResult, formula.outputUnit),
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = metrics.scaledSp(22),
                  fontWeight = FontWeight.ExtraBold
                ),
                color = palette.accentIndigo,
                modifier = Modifier.testTag("formula_quick_solve_output")
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

/**
 * Resolves a human-readable document filename from a content `Uri` (PDF or DOCX).
 */
private fun resolveDocumentFileName(context: Context, uri: Uri): String {
  try {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      if (nameIndex != -1 && cursor.moveToFirst()) {
        val displayName = cursor.getString(nameIndex)?.trim()
        if (!displayName.isNullOrEmpty()) {
          return displayName
        }
      }
    }
  } catch (_: Exception) {
  }
  val fallbackSegment = uri.lastPathSegment
    ?.substringAfterLast("/")
    ?.substringAfterLast(":")
    ?.trim()
  return if (!fallbackSegment.isNullOrEmpty()) {
    fallbackSegment
  } else {
    "CE_Lecture_Handout.pdf"
  }
}

/**
 * Plays a subtle, tactile system click / acoustic beep when interacting with flashcards,
 * rating buttons, or deck stacks. Wrapped safely in try/catch so it never fails on silent
 * devices or JVM unit tests.
 */
fun playCardHapticSound(context: Context, view: View? = null) {
  try {
    view?.playSoundEffect(SoundEffectConstants.CLICK)
  } catch (_: Exception) {
  }
  try {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.45f)
  } catch (_: Exception) {
  }
  try {
    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 40)
    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 22)
    Handler(Looper.getMainLooper()).postDelayed({
      try {
        toneGenerator.release()
      } catch (_: Exception) {
      }
    }, 60L)
  } catch (_: Exception) {
  }
}

/**
 * Smart Flashcards Main View (`FlashcardMainView`) inside the Extras tab:
 * - Top Header: "Smart Flashcards" with a back button returning to the Extras hub and a top-right
 *   Key/Settings button to configure/override the Gemini API key in `SharedPreferences`.
 * - Theme-aware Frosted Glass Action Pills ("Import Document" and "+ New Deck") bound to
 *   `palette.nestedCardFill`, `palette.glassBorder`, `palette.textPrimary`, and `palette.accentIndigo`.
 * - Realistic 3-Layer Physical Deck Depth (Stacked Index Card Effect) with full Light & Dark Mode parity.
 */
@Composable
fun FlashcardMainView(
  viewModel: TaskViewModel,
  metrics: ResponsiveMetrics,
  onBackToExtras: () -> Unit,
  onStartReview: (FlashcardDeck) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val localView = LocalView.current
  val coroutineScope = rememberCoroutineScope()
  val palette = currentGlassPalette()
  val decks = viewModel.flashcardDecks.toList()

  var isNewDeckDialogOpen by rememberSaveable { mutableStateOf(false) }
  var isPasteTextDialogOpen by rememberSaveable { mutableStateOf(false) }
  var pasteDialogInitialTitle by rememberSaveable { mutableStateOf("") }
  var pasteDialogNoticeMessage by rememberSaveable { mutableStateOf<String?>(null) }
  var addCardTargetDeckId by rememberSaveable { mutableStateOf<Int?>(null) }
  var resetMasteryTargetDeckId by rememberSaveable { mutableStateOf<Int?>(null) }

  // API Key Quick-Verify & Override Dialog state
  var isApiKeyDialogOpen by rememberSaveable { mutableStateOf(false) }
  var hasCheckedInitialApiKeyPrompt by rememberSaveable { mutableStateOf(false) }

  // Automatically pop up the clean Quick-Verify API Key dialog if the hardcoded constant is still
  // the default key and no key has been saved in SharedPreferences yet.
  LaunchedEffect(Unit) {
    if (!hasCheckedInitialApiKeyPrompt) {
      hasCheckedInitialApiKeyPrompt = true
      if (
        DEFAULT_GEMINI_API_KEY == "AIzaSyANkLy-HqqeUQzPeAkAnN2m5xWx5NaDgew" &&
        !viewModel.hasSavedGeminiApiKey()
      ) {
        isApiKeyDialogOpen = true
      }
    }
  }

  // Gizmo-Style Multi-Batch Progress Modal State
  var isBatchExtracting by remember { mutableStateOf(false) }
  var activeBatchJob by remember { mutableStateOf<Job?>(null) }
  var batchDocumentName by rememberSaveable { mutableStateOf("") }
  var batchDeckTitle by rememberSaveable { mutableStateOf("") }
  var batchSubjectTag by rememberSaveable { mutableStateOf("General") }
  var currentBatchIndex by rememberSaveable { mutableIntStateOf(1) }
  var totalBatchCount by rememberSaveable { mutableIntStateOf(1) }
  var batchStatusLabel by rememberSaveable { mutableStateOf("Preparing document batches...") }
  var accumulatedBatchCards by remember { mutableStateOf<List<FlashcardItem>>(emptyList()) }
  var latestGeneratedQuestion by rememberSaveable { mutableStateOf("") }
  var latestGeneratedAnswer by rememberSaveable { mutableStateOf("") }

  // Detailed HTTP / Gemini Error Reporting Dialog State
  var extractionErrorDialogMessage by rememberSaveable { mutableStateOf<String?>(null) }

  fun inferSubjectTagFromCardsOrTitle(title: String, cards: List<FlashcardItem>, fallback: String): String {
    if (fallback.isNotBlank() && !fallback.equals("General", ignoreCase = true)) {
      return fallback
    }
    val combinedSample = buildString {
      append(title).append(" ")
      cards.take(10).forEach {
        append(it.question).append(" ").append(it.answer).append(" ")
      }
    }
    return when {
      combinedSample.contains("moment", ignoreCase = true) ||
        combinedSample.contains("shear", ignoreCase = true) ||
        combinedSample.contains("rebar", ignoreCase = true) ||
        combinedSample.contains("concrete", ignoreCase = true) ||
        combinedSample.contains("nscp", ignoreCase = true) -> "PSAD"
      combinedSample.contains("manning", ignoreCase = true) ||
        combinedSample.contains("reynolds", ignoreCase = true) ||
        combinedSample.contains("soil", ignoreCase = true) ||
        combinedSample.contains("atterberg", ignoreCase = true) ||
        combinedSample.contains("hydraulic", ignoreCase = true) -> "HPGE"
      combinedSample.contains("derivative", ignoreCase = true) ||
        combinedSample.contains("curve", ignoreCase = true) ||
        combinedSample.contains("economy", ignoreCase = true) ||
        combinedSample.contains("probability", ignoreCase = true) -> "MSTE"
      else -> fallback.ifBlank { "General" }
    }
  }

  fun saveAccumulatedCardsAsDeck(cardsToSave: List<FlashcardItem>, stoppedEarly: Boolean) {
    if (cardsToSave.isEmpty()) {
      Toast.makeText(
        context,
        "Stopped before any flashcards were generated.",
        Toast.LENGTH_SHORT
      ).show()
      return
    }
    val resolvedTag = inferSubjectTagFromCardsOrTitle(
      title = batchDeckTitle,
      cards = cardsToSave,
      fallback = batchSubjectTag
    )
    val finalCards = cardsToSave.mapIndexed { idx, card ->
      card.copy(
        id = idx + 1,
        category = resolvedTag
      )
    }
    val createdDeck = viewModel.addFlashcardDeck(
      title = batchDeckTitle.ifBlank { "Smart Study Deck" },
      subjectTag = resolvedTag,
      initialCards = finalCards
    )
    val toastMsg = if (stoppedEarly) {
      "Saved \"${createdDeck.title}\" (${createdDeck.cards.size} flashcards generated so far)"
    } else {
      "Built \"${createdDeck.title}\" (${createdDeck.cards.size} flashcards)"
    }
    Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
  }

  fun launchTextBatchExtraction(deckTitle: String, subjectTag: String, rawText: String) {
    activeBatchJob?.cancel()
    batchDocumentName = "$deckTitle (${rawText.length} chars)"
    batchDeckTitle = deckTitle.ifBlank { "Lecture Notes Study Deck" }
    batchSubjectTag = subjectTag.ifBlank { "General" }
    currentBatchIndex = 1
    totalBatchCount = maxOf(1, (rawText.length + 3999) / 4000)
    batchStatusLabel = "Scanning Pages 1–1 of $totalBatchCount • 0 Cards Extracted"
    accumulatedBatchCards = emptyList()
    latestGeneratedQuestion = ""
    latestGeneratedAnswer = ""
    extractionErrorDialogMessage = null
    isBatchExtracting = true

    val activeKey = viewModel.getActiveGeminiApiKey()
    activeBatchJob = coroutineScope.launch {
      try {
        val finalCards = GizmoBatchFlashcardEngine.processTextInChunks(
          rawText = rawText,
          activeApiKey = activeKey,
          defaultCategory = batchSubjectTag,
          onProgressUpdate = { currentBatch, totalBatches, statusLabel, cardsSoFar, latestQ, latestA ->
            coroutineScope.launch(Dispatchers.Main.immediate) {
              currentBatchIndex = currentBatch
              totalBatchCount = maxOf(1, totalBatches)
              batchStatusLabel = statusLabel
              accumulatedBatchCards = cardsSoFar
              if (latestQ.isNotBlank()) {
                latestGeneratedQuestion = latestQ
              }
              if (latestA.isNotBlank()) {
                latestGeneratedAnswer = latestA
              }
            }
          }
        )
        isBatchExtracting = false
        activeBatchJob = null
        if (finalCards.isNotEmpty()) {
          saveAccumulatedCardsAsDeck(finalCards, stoppedEarly = false)
        } else {
          extractionErrorDialogMessage =
            "Gemini analyzed the text but returned 0 flashcards. Try adding more detailed engineering notes or definitions."
        }
      } catch (e: CancellationException) {
        isBatchExtracting = false
        activeBatchJob = null
        throw e
      } catch (e: Exception) {
        isBatchExtracting = false
        activeBatchJob = null
        extractionErrorDialogMessage = e.message
          ?: "An unexpected error occurred while communicating with Gemini Flash."
      }
    }
  }

  BackHandler {
    if (!isBatchExtracting) {
      onBackToExtras()
    }
  }

  // Native Android Document Picker for .pdf, .docx, and .txt files of ANY length
  val documentPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      val resolvedName = resolveDocumentFileName(context, uri)
      val cleanTitle = resolvedName
        .substringBeforeLast(".")
        .replace(Regex("[_\\-]+"), " ")
        .trim()
        .ifEmpty { "Imported CE Reviewers" }

      activeBatchJob?.cancel()
      batchDocumentName = resolvedName
      batchDeckTitle = cleanTitle
      batchSubjectTag = "General"
      currentBatchIndex = 1
      totalBatchCount = 1
      batchStatusLabel = "Scanning Pages 1–2 of ... • 0 Cards Extracted"
      accumulatedBatchCards = emptyList()
      latestGeneratedQuestion = ""
      latestGeneratedAnswer = ""
      extractionErrorDialogMessage = null
      isBatchExtracting = true

      val activeKey = viewModel.getActiveGeminiApiKey()
      activeBatchJob = coroutineScope.launch {
        try {
          val generatedCards = GizmoBatchFlashcardEngine.processDocumentUriInBatches(
            context = context,
            uri = uri,
            fileName = resolvedName,
            activeApiKey = activeKey,
            defaultCategory = "General",
            onProgressUpdate = { currentBatch, totalBatches, statusLabel, cardsSoFar, latestQ, latestA ->
              coroutineScope.launch(Dispatchers.Main.immediate) {
                currentBatchIndex = currentBatch
                totalBatchCount = maxOf(1, totalBatches)
                batchStatusLabel = statusLabel
                accumulatedBatchCards = cardsSoFar
                if (latestQ.isNotBlank()) {
                  latestGeneratedQuestion = latestQ
                }
                if (latestA.isNotBlank()) {
                  latestGeneratedAnswer = latestA
                }
              }
            }
          )
          isBatchExtracting = false
          activeBatchJob = null
          if (generatedCards.isNotEmpty()) {
            saveAccumulatedCardsAsDeck(generatedCards, stoppedEarly = false)
          } else {
            extractionErrorDialogMessage =
              "Gemini completed scanning \"$resolvedName\" but found no extractable flashcard items."
          }
        } catch (e: CancellationException) {
          isBatchExtracting = false
          activeBatchJob = null
          throw e
        } catch (e: Exception) {
          isBatchExtracting = false
          activeBatchJob = null
          extractionErrorDialogMessage = e.message
            ?: "Failed to extract flashcards from \"$resolvedName\"."
        }
      }
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .widthIn(max = metrics.maxContentWidth)
      .testTag("flashcard_main_view"),
    contentPadding = PaddingValues(
      start = metrics.scaledDp(20),
      end = metrics.scaledDp(20),
      top = metrics.scaledDp(20),
      bottom = metrics.scaledDp(112)
    ),
    verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
  ) {
    // 1. Top Header with Back Button and Top-Right Gemini API Key Button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(10))
      ) {
        Surface(
          onClick = {
            playCardHapticSound(context, localView)
            onBackToExtras()
          },
          shape = CircleShape,
          color = palette.cardFill,
          border = palette.glassBorder,
          shadowElevation = 4.dp,
          modifier = Modifier
            .size(48.dp)
            .testTag("flashcards_back_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
              contentDescription = "Back to Extras",
              tint = palette.textPrimary,
              modifier = Modifier.size(26.dp)
            )
          }
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Smart Flashcards",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontSize = metrics.scaledSp(24),
              fontWeight = FontWeight.Bold
            ),
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("flashcards_header_title")
          )
          Text(
            text = "Gizmo-Style Multi-Batch AI Flashcard Generator",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.SemiBold
            ),
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Surface(
          onClick = {
            playCardHapticSound(context, localView)
            isApiKeyDialogOpen = true
          },
          shape = CircleShape,
          color = palette.nestedCardFill,
          border = palette.glassBorder,
          shadowElevation = 4.dp,
          modifier = Modifier
            .size(48.dp)
            .testTag("flashcards_api_key_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Key,
              contentDescription = "Configure Gemini API Key",
              tint = palette.accentIndigo,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }

    // 2. Cleaner Action Bar with Theme-Aware Frosted Glass Pills ("Import Document" & "+ New Deck")
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(10)),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          onClick = {
            playCardHapticSound(context, localView)
            try {
              documentPickerLauncher.launch(OfflineDeckExtractor.SUPPORTED_DOCUMENT_MIME_TYPES)
            } catch (_: Exception) {
              pasteDialogInitialTitle = ""
              pasteDialogNoticeMessage = null
              isPasteTextDialogOpen = true
            }
          },
          shape = CircleShape,
          color = palette.nestedCardFill,
          border = palette.glassBorder,
          shadowElevation = 2.dp,
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .testTag("import_document_button")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.Description,
              contentDescription = "Import Document",
              tint = palette.accentIndigo,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Import Document",
              style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Surface(
          onClick = {
            playCardHapticSound(context, localView)
            isNewDeckDialogOpen = true
          },
          shape = CircleShape,
          color = palette.nestedCardFill,
          border = palette.glassBorder,
          shadowElevation = 2.dp,
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .testTag("new_deck_button")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "New Deck",
              tint = palette.accentIndigo,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "+ New Deck",
              style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }

    // 3. Empty State or 3-Layer Physical Stacked Decks List
    if (decks.isEmpty()) {
      item {
        LiquidGlassCard(
          shape = RoundedCornerShape(24.dp),
          elevation = 8.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("flashcards_empty_state_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = metrics.scaledDp(24), vertical = metrics.scaledDp(32)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
          ) {
            Surface(
              shape = CircleShape,
              color = LiquidRoyalPeriwinkle.copy(alpha = 0.18f),
              border = BorderStroke(1.dp, LiquidRoyalPeriwinkle.copy(alpha = 0.35f)),
              modifier = Modifier.size(64.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Filled.AutoAwesome,
                  contentDescription = "No Decks Available",
                  tint = palette.accentIndigo,
                  modifier = Modifier.size(30.dp)
                )
              }
            }

            Text(
              text = "No Decks Available",
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = metrics.scaledSp(19),
                fontWeight = FontWeight.Bold
              ),
              color = palette.textPrimary,
              textAlign = TextAlign.Center
            )

            Text(
              text = "Tap 'Import Document' (.pdf, .docx, .txt of any length) or '+ New Deck' above to build your smart study deck.",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = metrics.scaledSp(14)
              ),
              color = palette.textSecondary,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      items(decks, key = { it.id }) { deck ->
        val totalCount = deck.cards.size
        val masteredCount = deck.cards.count { it.isMastered || it.masteryLevel.equals("Good", ignoreCase = true) }
        val isDeckFullyMastered = masteredCount == totalCount && totalCount > 0
        val hasCards = totalCount > 0
        val deckCardShape = RoundedCornerShape(24.dp)

        val deckProgressFraction = if (totalCount == 0) {
          0f
        } else {
          (masteredCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
        }
        val animatedDeckProgress by animateFloatAsState(
          targetValue = deckProgressFraction,
          animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
          label = "deck_mastery_progress_${deck.id}"
        )

        // 3-Layer Physical Stacked Engineering Index Card Effect (Dark & Light Mode Parity)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
          contentAlignment = Alignment.TopCenter
        ) {
          // Bottom Stack Layer (offset(y = 8.dp))
          Box(
            modifier = Modifier
              .matchParentSize()
              .offset(y = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight()
                .shadow(
                  elevation = 4.dp,
                  shape = deckCardShape,
                  ambientColor = palette.shadowAmbient,
                  spotColor = palette.shadowSpot
                )
                .clip(deckCardShape)
                .background(palette.stackBottomFill)
                .border(
                  BorderStroke(1.dp, palette.stackMidBorder),
                  deckCardShape
                )
            )
          }

          // Middle Stack Layer (offset(y = 4.dp))
          Box(
            modifier = Modifier
              .matchParentSize()
              .offset(y = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(0.97f)
                .fillMaxHeight()
                .shadow(
                  elevation = 6.dp,
                  shape = deckCardShape,
                  ambientColor = palette.shadowAmbient,
                  spotColor = palette.shadowSpot
                )
                .clip(deckCardShape)
                .background(palette.stackMidFill)
                .border(
                  BorderStroke(1.dp, palette.stackMidBorder),
                  deckCardShape
                )
            )
          }

          // Main Top Card: palette.stackTopFill with palette.glassBorder
          Surface(
            shape = deckCardShape,
            color = palette.stackTopFill,
            border = palette.glassBorder,
            modifier = Modifier
              .fillMaxWidth()
              .shadow(
                elevation = 10.dp,
                shape = deckCardShape,
                ambientColor = palette.shadowAmbient,
                spotColor = palette.shadowSpot
              )
              .clip(deckCardShape)
              .testTag("flashcard_deck_card_${deck.id}")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(metrics.scaledDp(18)),
              verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
              ) {
                Column(
                  modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // Subject Tag + Tactile Deck Thickness Indicator Pill Badge
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Surface(
                      color = LiquidRoyalPeriwinkle.copy(alpha = 0.18f),
                      shape = CircleShape,
                      border = BorderStroke(1.dp, LiquidRoyalPeriwinkle.copy(alpha = 0.40f))
                    ) {
                      Text(
                        text = deck.subjectTag,
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontSize = metrics.scaledSp(11),
                          fontWeight = FontWeight.Bold
                        ),
                        color = palette.accentIndigo,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                      )
                    }

                    // Tactile Deck Thickness Pill Badge ("🗂️ 18 Cards")
                    Surface(
                      color = palette.nestedCardFill,
                      shape = CircleShape,
                      border = palette.nestedBorder,
                      modifier = Modifier.testTag("deck_thickness_badge_${deck.id}")
                    ) {
                      Text(
                        text = "🗂️ ${deck.cards.size} ${if (deck.cards.size == 1) "Card" else "Cards"}",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontFamily = JetBrainsMonoFamily,
                          fontSize = metrics.scaledSp(11),
                          fontWeight = FontWeight.Bold
                        ),
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                      )
                    }
                  }

                  // Deck Title: 18.sp Bold bound strictly to palette.textPrimary
                  Text(
                    text = deck.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = palette.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                  )

                  if (!hasCards) {
                    Text(
                      text = "Add at least 1 card to start review",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = metrics.scaledSp(11),
                        fontWeight = FontWeight.SemiBold
                      ),
                      color = palette.accentIndigo,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.testTag("empty_deck_review_hint_${deck.id}")
                    )
                  }
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  // 100% Mastered Green Check Indicator (Themed Frosted Mini-Pill Capsule)
                  if (isDeckFullyMastered) {
                    val masteredEmerald = if (palette.isDark) Color(0xFF34D399) else Color(0xFF059669)
                    Surface(
                      color = Color(0xFF10B981).copy(alpha = 0.16f),
                      shape = RoundedCornerShape(12.dp),
                      border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                      modifier = Modifier.testTag("deck_mastered_badge_${deck.id}")
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = "Mastered",
                          tint = masteredEmerald,
                          modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = "Mastered",
                          fontSize = 11.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = masteredEmerald,
                          maxLines = 1,
                          softWrap = false
                        )
                      }
                    }
                  }

                  IconButton(
                    onClick = {
                      playCardHapticSound(context, localView)
                      viewModel.deleteFlashcardDeck(deck.id)
                    },
                    modifier = Modifier
                      .size(48.dp)
                      .testTag("delete_deck_button_${deck.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete Deck ${deck.title}",
                      tint = palette.accentRed,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }

              // Dynamic Progress Indicator Beneath Title and Category, Before Action Row
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("deck_mastery_progress_section_${deck.id}"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "$masteredCount/$totalCount Mastered",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("deck_mastery_count_label_${deck.id}")
                  )

                  if (masteredCount > 0) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(3.dp),
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                          playCardHapticSound(context, localView)
                          resetMasteryTargetDeckId = deck.id
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("deck_reset_progress_shortcut_${deck.id}")
                    ) {
                      Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Progress",
                        tint = palette.accentIndigo,
                        modifier = Modifier.size(12.dp)
                      )
                      Text(
                        text = "Reset Progress",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.accentIndigo,
                        textDecoration = TextDecoration.Underline,
                        maxLines = 1
                      )
                    }
                  }
                }

                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(palette.trackColor)
                    .testTag("deck_mastery_progress_track_${deck.id}")
                ) {
                  if (animatedDeckProgress > 0f) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth(animatedDeckProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                          Brush.horizontalGradient(
                            listOf(Color(0xFF34D399), Color(0xFF059669))
                          )
                        )
                        .testTag("deck_mastery_progress_fill_${deck.id}")
                    )
                  }
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                TextButton(
                  onClick = {
                    playCardHapticSound(context, localView)
                    addCardTargetDeckId = deck.id
                  },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("add_card_to_deck_${deck.id}")
                ) {
                  Text(
                    text = "+ Add Card",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.accentIndigo,
                    maxLines = 1,
                    softWrap = false
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                  onClick = {
                    playCardHapticSound(context, localView)
                    if (hasCards) {
                      onStartReview(deck)
                    } else {
                      Toast.makeText(
                        context,
                        "Add at least 1 card to start review",
                        Toast.LENGTH_SHORT
                      ).show()
                    }
                  },
                  enabled = hasCards,
                  shape = RoundedCornerShape(20.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White,
                    disabledContainerColor = palette.trackColor,
                    disabledContentColor = palette.textSecondary.copy(alpha = 0.6f)
                  ),
                  elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                  modifier = Modifier.testTag("start_review_button_${deck.id}")
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.PlayArrow,
                      contentDescription = "Start Review",
                      tint = if (hasCards) {
                        Color.White
                      } else {
                        palette.textSecondary.copy(alpha = 0.6f)
                      },
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                      text = "Start Review",
                      color = if (hasCards) {
                        Color.White
                      } else {
                        palette.textSecondary.copy(alpha = 0.6f)
                      },
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      softWrap = false
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

  // 4. Live Gizmo-Style Multi-Batch Extraction Progress Modal
  if (isBatchExtracting) {
    val infiniteTransition = rememberInfiniteTransition(label = "gizmo_batch_pulse")
    val pulseScale by infiniteTransition.animateFloat(
      initialValue = 0.92f,
      targetValue = 1.08f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "pulse_scale"
    )
    val safeTotalBatches = maxOf(1, totalBatchCount)
    val safeCurrentBatch = currentBatchIndex.coerceIn(1, safeTotalBatches)
    val animatedProgress by animateFloatAsState(
      targetValue = (safeCurrentBatch.toFloat() / safeTotalBatches.toFloat()).coerceIn(0.05f, 1f),
      animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
      label = "batch_progress"
    )

    Dialog(
      onDismissRequest = { /* non-dismissible except via Save & Stop Here */ },
      properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
      Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = palette.dialogSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
        border = BorderStroke(1.5.dp, palette.accentIndigo.copy(alpha = 0.55f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ai_extraction_simulator_dialog")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(metrics.scaledDp(22)),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
        ) {
          Surface(
            shape = CircleShape,
            color = LiquidRoyalPeriwinkle.copy(alpha = 0.18f),
            modifier = Modifier
              .size(60.dp)
              .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
              }
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "Building Smart Deck",
                tint = palette.accentIndigo,
                modifier = Modifier.size(30.dp)
              )
            }
          }

          Text(
            text = "Building Smart Deck...",
            style = MaterialTheme.typography.titleLarge.copy(
              fontSize = metrics.scaledSp(20),
              fontWeight = FontWeight.Bold
            ),
            color = palette.textPrimary,
            textAlign = TextAlign.Center
          )

          if (batchDocumentName.isNotBlank()) {
            Text(
              text = batchDocumentName,
              style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = metrics.scaledSp(11),
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textSecondary,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_extraction_filename_text")
            )
          }

          // Progress Bar + Batch Counter
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = batchStatusLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontSize = metrics.scaledSp(12),
                  fontWeight = FontWeight.Bold
                ),
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .weight(1f)
                  .testTag("ai_extraction_status_text")
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Batch $safeCurrentBatch / $safeTotalBatches",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = metrics.scaledSp(11),
                  fontWeight = FontWeight.Bold
                ),
                color = palette.accentIndigo
              )
            }

            LinearProgressIndicator(
              progress = { animatedProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(CircleShape),
              color = palette.accentIndigo,
              trackColor = palette.trackColor
            )
          }

          // Live Counter Badge
          Surface(
            color = LiquidRoyalPeriwinkle.copy(alpha = 0.18f),
            shape = CircleShape,
            border = BorderStroke(1.dp, palette.accentIndigo.copy(alpha = 0.40f))
          ) {
            Text(
              text = "⚡ ${accumulatedBatchCards.size} Cards Extracted",
              style = MaterialTheme.typography.labelLarge.copy(
                fontSize = metrics.scaledSp(13),
                fontWeight = FontWeight.Bold
              ),
              color = palette.accentIndigo,
              modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("gizmo_live_card_counter_badge")
            )
          }

          // Live Preview of Most Recently Extracted Question & Answer
          Surface(
            color = palette.nestedCardFill,
            shape = RoundedCornerShape(16.dp),
            border = palette.nestedBorder,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "LATEST EXTRACTED Q&A PREVIEW",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = metrics.scaledSp(10),
                  fontWeight = FontWeight.Bold
                ),
                color = palette.accentIndigo
              )
              Text(
                text = if (latestGeneratedQuestion.isNotBlank()) {
                  "Q: $latestGeneratedQuestion"
                } else {
                  "Waiting for first batch response from Gemini Flash..."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontSize = metrics.scaledSp(13),
                  fontWeight = FontWeight.SemiBold
                ),
                color = palette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("gizmo_latest_question_preview")
              )
              if (latestGeneratedAnswer.isNotBlank()) {
                Text(
                  text = "A: $latestGeneratedAnswer",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = metrics.scaledSp(12),
                    fontWeight = FontWeight.Medium
                  ),
                  color = palette.textSecondary,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.testTag("gizmo_latest_answer_preview")
                )
              }
            }
          }

          // "Save & Stop Here" Button (Active as soon as >= 1 card is extracted)
          Button(
            onClick = {
              val cardsSnapshot = accumulatedBatchCards.toList()
              activeBatchJob?.cancel()
              activeBatchJob = null
              isBatchExtracting = false
              saveAccumulatedCardsAsDeck(cardsSnapshot, stoppedEarly = true)
            },
            enabled = accumulatedBatchCards.isNotEmpty(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF4F46E5),
              contentColor = Color.White,
              disabledContainerColor = palette.trackColor,
              disabledContentColor = palette.textSecondary.copy(alpha = 0.6f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 48.dp)
              .testTag("gizmo_save_and_stop_button")
          ) {
            Text(
              text = "Save & Stop Here",
              style = MaterialTheme.typography.labelLarge.copy(
                fontSize = metrics.scaledSp(13),
                fontWeight = FontWeight.Bold
              )
            )
          }

          if (accumulatedBatchCards.isEmpty()) {
            TextButton(
              onClick = {
                activeBatchJob?.cancel()
                activeBatchJob = null
                isBatchExtracting = false
              }
            ) {
              Text(
                text = "Cancel Extraction",
                fontSize = 12.sp,
                color = palette.textSecondary
              )
            }
          }
        }
      }
    }
  }

  // 5. Gemini API Key Quick-Verify & Override Dialog
  if (isApiKeyDialogOpen) {
    GeminiApiKeyConfigDialog(
      metrics = metrics,
      currentSavedKey = viewModel.savedGeminiApiKey,
      defaultKey = DEFAULT_GEMINI_API_KEY,
      onDismiss = { isApiKeyDialogOpen = false },
      onSaveKey = { newKey ->
        viewModel.saveGeminiApiKey(newKey)
        isApiKeyDialogOpen = false
        Toast.makeText(
          context,
          "Gemini API Key saved to SharedPreferences!",
          Toast.LENGTH_SHORT
        ).show()
      }
    )
  }

  // 6. Detailed Gemini Extraction Error Reporting Dialog
  if (extractionErrorDialogMessage != null) {
    val partialCardsSnapshot = accumulatedBatchCards.toList()
    AlertDialog(
      onDismissRequest = { extractionErrorDialogMessage = null },
      shape = RoundedCornerShape(24.dp),
      containerColor = palette.dialogSurface,
      title = {
        Text(
          text = "AI Flashcard Extraction Notice",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = metrics.scaledSp(19),
            fontWeight = FontWeight.Bold
          ),
          color = palette.accentRed
        )
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Surface(
            color = palette.accentRed.copy(alpha = 0.14f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, palette.accentRed.copy(alpha = 0.35f))
          ) {
            Text(
              text = extractionErrorDialogMessage.orEmpty(),
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textPrimary,
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("gemini_error_detail_text")
            )
          }

          if (partialCardsSnapshot.isNotEmpty()) {
            Text(
              text = "⚡ Good news: ${partialCardsSnapshot.size} flashcards were already generated before this batch stopped. You can save them right now!",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.accentIndigo
            )
          } else {
            Text(
              text = "Tip: If your API key expired or reached a quota limit, tap 'Update API Key' below to paste your free Google AI Studio key.",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = metrics.scaledSp(12)
              ),
              color = palette.textSecondary
            )
          }
        }
      },
      confirmButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (partialCardsSnapshot.isNotEmpty()) {
            Button(
              onClick = {
                extractionErrorDialogMessage = null
                saveAccumulatedCardsAsDeck(partialCardsSnapshot, stoppedEarly = true)
              },
              shape = RoundedCornerShape(16.dp)
            ) {
              Text(
                text = "Save ${partialCardsSnapshot.size} Cards",
                fontWeight = FontWeight.Bold
              )
            }
          }
          Button(
            onClick = {
              extractionErrorDialogMessage = null
              isApiKeyDialogOpen = true
            },
            shape = RoundedCornerShape(16.dp)
          ) {
            Text(
              text = "Update API Key",
              fontWeight = FontWeight.Bold
            )
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { extractionErrorDialogMessage = null }) {
          Text("Close", color = palette.textSecondary)
        }
      }
    )
  }

  // 7. "Paste Text / Notes" Multi-Batch AI Deck Generator Dialog
  if (isPasteTextDialogOpen) {
    PasteTextToDeckDialog(
      metrics = metrics,
      initialDeckTitle = pasteDialogInitialTitle,
      noticeMessage = pasteDialogNoticeMessage,
      availableSubjects = viewModel.getSubjectFolders(),
      onDismiss = {
        isPasteTextDialogOpen = false
        pasteDialogNoticeMessage = null
      },
      onGenerateDeck = { deckTitle, subjectTag, rawText ->
        isPasteTextDialogOpen = false
        pasteDialogNoticeMessage = null
        launchTextBatchExtraction(
          deckTitle = deckTitle,
          subjectTag = subjectTag,
          rawText = rawText
        )
      }
    )
  }

  // "+ New Deck" Creation Dialog
  if (isNewDeckDialogOpen) {
    CreateNewFlashcardDeckDialog(
      metrics = metrics,
      availableSubjects = viewModel.getSubjectFolders(),
      onDismiss = { isNewDeckDialogOpen = false },
      onCreateDeck = { title, subjectTag ->
        viewModel.addFlashcardDeck(
          title = title,
          subjectTag = subjectTag,
          initialCards = emptyList()
        )
        isNewDeckDialogOpen = false
      }
    )
  }

  // "+ Add Card" to Existing Deck Dialog
  val targetDeckForNewCard = decks.firstOrNull { it.id == addCardTargetDeckId }
  if (targetDeckForNewCard != null) {
    AddCardToFlashcardDeckDialog(
      deckTitle = targetDeckForNewCard.title,
      defaultCategory = targetDeckForNewCard.subjectTag,
      metrics = metrics,
      onDismiss = { addCardTargetDeckId = null },
      onSaveCard = { question, answer ->
        viewModel.addCardToDeck(
          deckId = targetDeckForNewCard.id,
          question = question,
          answer = answer,
          category = targetDeckForNewCard.subjectTag
        )
        addCardTargetDeckId = null
      }
    )
  }

  // "Reset Mastery Progress?" Confirmation Dialog from Deck List
  val targetDeckForReset = decks.firstOrNull { it.id == resetMasteryTargetDeckId }
  if (targetDeckForReset != null) {
    ResetMasteryConfirmDialog(
      cardCount = targetDeckForReset.cards.size,
      metrics = metrics,
      onDismiss = { resetMasteryTargetDeckId = null },
      onConfirmReset = {
        playCardHapticSound(context, localView)
        viewModel.resetDeckMastery(targetDeckForReset.id)
        resetMasteryTargetDeckId = null
        Toast.makeText(context, "Deck progress reset to 0%", Toast.LENGTH_SHORT).show()
      }
    )
  }
}

@Composable
private fun ResetMasteryConfirmDialog(
  cardCount: Int,
  metrics: ResponsiveMetrics,
  onDismiss: () -> Unit,
  onConfirmReset: () -> Unit
) {
  val palette = currentGlassPalette()
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = palette.dialogSurface,
    title = {
      Text(
        text = "Reset Mastery Progress?",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontSize = metrics.scaledSp(19),
          fontWeight = FontWeight.Bold
        ),
        color = palette.textPrimary,
        modifier = Modifier.testTag("reset_mastery_dialog_title")
      )
    },
    text = {
      Text(
        text = "This will mark all cards as unmastered and restore the full study queue ($cardCount cards). Do you want to restart from scratch?",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = metrics.scaledSp(14),
          lineHeight = metrics.scaledSp(20)
        ),
        color = palette.textSecondary,
        modifier = Modifier.testTag("reset_mastery_dialog_message")
      )
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
        modifier = Modifier.testTag("reset_mastery_dialog_cancel_button")
      ) {
        Text(
          text = "Cancel",
          color = palette.textSecondary,
          fontWeight = FontWeight.SemiBold
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirmReset,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFFEF4444),
          contentColor = Color.White
        ),
        modifier = Modifier.testTag("reset_mastery_dialog_confirm_button")
      ) {
        Text(
          text = "Reset All",
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  )
}

@Composable
private fun GeminiApiKeyConfigDialog(
  metrics: ResponsiveMetrics,
  currentSavedKey: String,
  defaultKey: String,
  onDismiss: () -> Unit,
  onSaveKey: (String) -> Unit
) {
  val context = LocalContext.current
  val palette = currentGlassPalette()
  var apiKeyInput by rememberSaveable(currentSavedKey) {
    mutableStateOf(currentSavedKey.ifBlank { defaultKey })
  }
  var keyError by rememberSaveable { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = palette.dialogSurface,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Filled.Key,
          contentDescription = null,
          tint = palette.accentIndigo,
          modifier = Modifier.size(24.dp)
        )
        Text(
          text = "Gemini Flash API Key",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = metrics.scaledSp(19),
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Paste your free Google AI Studio API Key below to power the Gizmo-Style Multi-Batch PDF & DOCX Flashcard Generator. Your key is stored locally in SharedPreferences.",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = metrics.scaledSp(12)
          ),
          color = palette.textSecondary
        )

        OutlinedTextField(
          value = apiKeyInput,
          onValueChange = {
            apiKeyInput = it
            keyError = false
          },
          label = { Text("Google AI Studio API Key") },
          placeholder = { Text("AIzaSy...") },
          isError = keyError,
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("gemini_api_key_input")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
              val pasted = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
              if (!pasted.isNullOrEmpty()) {
                apiKeyInput = pasted
                keyError = false
              } else {
                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
              }
            },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.ContentPaste,
              contentDescription = "Paste Key",
              tint = palette.accentIndigo,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "Paste from Clipboard",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = palette.accentIndigo
            )
          }

          TextButton(
            onClick = {
              apiKeyInput = defaultKey
              keyError = false
            },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text("Reset Default", fontSize = 12.sp, color = palette.textSecondary)
          }
        }

        if (keyError) {
          Text(
            text = "Please enter a valid API key starting with 'AIza'.",
            style = MaterialTheme.typography.labelSmall,
            color = palette.accentRed
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val clean = apiKeyInput.trim()
          if (clean.isEmpty()) {
            keyError = true
          } else {
            onSaveKey(clean)
          }
        },
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF4F46E5),
          contentColor = Color.White
        ),
        modifier = Modifier.testTag("save_gemini_api_key_button")
      ) {
        Text("Save Key", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Later", color = palette.textSecondary)
      }
    }
  )
}

@Composable
private fun PasteTextToDeckDialog(
  metrics: ResponsiveMetrics,
  initialDeckTitle: String,
  noticeMessage: String?,
  availableSubjects: List<String>,
  onDismiss: () -> Unit,
  onGenerateDeck: (deckTitle: String, subjectTag: String, rawText: String) -> Unit
) {
  val palette = currentGlassPalette()
  val defaultTags = remember(availableSubjects) {
    (listOf("PSAD", "MSTE", "HPGE", "General") + availableSubjects)
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .distinctBy { it.lowercase(Locale.US) }
  }
  var deckTitle by rememberSaveable(initialDeckTitle) {
    mutableStateOf(initialDeckTitle.ifBlank { "Lecture Notes Study Deck" })
  }
  var subjectTag by rememberSaveable {
    mutableStateOf(defaultTags.firstOrNull() ?: "PSAD")
  }
  var rawNotesText by rememberSaveable { mutableStateOf("") }
  var notesError by rememberSaveable { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = palette.dialogSurface,
    title = {
      Text(
        text = "Paste Text / Notes",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontSize = metrics.scaledSp(20),
          fontWeight = FontWeight.Bold
        ),
        color = palette.textPrimary
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
      ) {
        if (!noticeMessage.isNullOrBlank()) {
          Surface(
            color = LiquidRoyalPeriwinkle.copy(alpha = 0.16f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, palette.accentIndigo.copy(alpha = 0.35f))
          ) {
            Text(
              text = noticeMessage,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textPrimary,
              modifier = Modifier.padding(10.dp)
            )
          }
        } else {
          Text(
            text = "Paste lecture notes, definitions, formulas, or reviewers of ANY length. Sibil splits long notes into 4,000-character batches and extracts every question & answer via Gemini Flash.",
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = metrics.scaledSp(12)
            ),
            color = palette.textSecondary
          )
        }

        OutlinedTextField(
          value = deckTitle,
          onValueChange = { deckTitle = it },
          label = { Text("Deck Title") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("paste_notes_deck_title_input")
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          defaultTags.forEach { tag ->
            val selected = subjectTag.equals(tag, ignoreCase = true)
            Surface(
              onClick = { subjectTag = tag },
              shape = CircleShape,
              color = if (selected) {
                Color(0xFF4F46E5)
              } else {
                palette.nestedCardFill
              },
              border = BorderStroke(
                1.dp,
                if (selected) {
                  Color(0xFF4F46E5)
                } else {
                  palette.nestedBorderColor
                }
              )
            ) {
              Text(
                text = tag,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (selected) Color.White else palette.textPrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }

        OutlinedTextField(
          value = rawNotesText,
          onValueChange = {
            rawNotesText = it
            notesError = false
          },
          label = { Text("Paste Lecture Notes, Definitions, or Formulas") },
          placeholder = {
            Text(
              "Paste any length of reviewer notes, NSCP provisions, formulas, or Q&As..."
            )
          },
          isError = notesError,
          minLines = 5,
          maxLines = 10,
          shape = RoundedCornerShape(14.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("paste_notes_raw_text_input")
        )

        if (notesError) {
          Text(
            text = "Please paste or type some notes to generate flashcards.",
            style = MaterialTheme.typography.labelSmall,
            color = palette.accentRed
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (rawNotesText.trim().isEmpty()) {
            notesError = true
          } else {
            onGenerateDeck(
              deckTitle.trim().ifEmpty { "Lecture Notes Study Deck" },
              subjectTag.trim().ifEmpty { "General" },
              rawNotesText
            )
          }
        },
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF4F46E5),
          contentColor = Color.White
        ),
        modifier = Modifier.testTag("confirm_generate_paste_deck_button")
      ) {
        Text(
          text = "Generate Flashcards",
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = palette.textSecondary)
      }
    }
  )
}

@Composable
private fun CreateNewFlashcardDeckDialog(
  metrics: ResponsiveMetrics,
  availableSubjects: List<String>,
  onDismiss: () -> Unit,
  onCreateDeck: (title: String, subjectTag: String) -> Unit
) {
  val palette = currentGlassPalette()
  val defaultTags = remember(availableSubjects) {
    (listOf("PSAD", "MSTE", "HPGE", "General") + availableSubjects)
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .distinctBy { it.lowercase(Locale.US) }
  }
  var deckTitle by rememberSaveable { mutableStateOf("") }
  var subjectTag by rememberSaveable {
    mutableStateOf(defaultTags.firstOrNull() ?: "PSAD")
  }
  var titleError by rememberSaveable { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = palette.dialogSurface,
    title = {
      Text(
        text = "Create New Deck",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontSize = metrics.scaledSp(20),
          fontWeight = FontWeight.Bold
        ),
        color = palette.textPrimary
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = deckTitle,
          onValueChange = {
            deckTitle = it
            if (it.isNotBlank()) titleError = false
          },
          label = { Text("Deck Title") },
          placeholder = { Text("e.g., Steel Design - Tension Members") },
          isError = titleError,
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_deck_title_input")
        )

        OutlinedTextField(
          value = subjectTag,
          onValueChange = { subjectTag = it },
          label = { Text("Subject Tag") },
          placeholder = { Text("e.g., PSAD, MSTE, HPGE, General") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_deck_subject_input")
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          defaultTags.forEach { tagOption ->
            val isSelected = subjectTag.trim().equals(tagOption, ignoreCase = true)
            Surface(
              onClick = { subjectTag = tagOption },
              shape = CircleShape,
              color = if (isSelected) {
                Color(0xFF4F46E5)
              } else {
                palette.nestedCardFill
              },
              border = BorderStroke(
                width = 1.dp,
                color = if (isSelected) {
                  Color(0xFF4F46E5)
                } else {
                  palette.nestedBorderColor
                }
              )
            ) {
              Text(
                text = tagOption,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) {
                  Color.White
                } else {
                  palette.textPrimary
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (deckTitle.isBlank()) {
            titleError = true
          } else {
            onCreateDeck(
              deckTitle.trim(),
              subjectTag.trim().ifEmpty { "General" }
            )
          }
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF4F46E5),
          contentColor = Color.White
        ),
        modifier = Modifier.testTag("confirm_create_deck_button")
      ) {
        Text(
          text = "Create Deck",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = palette.textSecondary)
      }
    }
  )
}

@Composable
private fun AddCardToFlashcardDeckDialog(
  deckTitle: String,
  defaultCategory: String,
  metrics: ResponsiveMetrics,
  onDismiss: () -> Unit,
  onSaveCard: (question: String, answer: String) -> Unit
) {
  val palette = currentGlassPalette()
  var question by rememberSaveable { mutableStateOf("") }
  var answer by rememberSaveable { mutableStateOf("") }
  var validationError by rememberSaveable { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = palette.dialogSurface,
    title = {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Add Flashcard",
          style = MaterialTheme.typography.titleLarge.copy(
            fontSize = metrics.scaledSp(20),
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary
        )
        Text(
          text = "Deck: $deckTitle ($defaultCategory)",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = metrics.scaledSp(12),
            fontWeight = FontWeight.SemiBold
          ),
          color = palette.accentIndigo,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = question,
          onValueChange = {
            question = it
            if (it.isNotBlank()) validationError = false
          },
          label = { Text("Question / Prompt") },
          isError = validationError && question.isBlank(),
          minLines = 3,
          shape = RoundedCornerShape(14.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_card_question_input")
        )

        OutlinedTextField(
          value = answer,
          onValueChange = {
            answer = it
            if (it.isNotBlank()) validationError = false
          },
          label = { Text("Answer / Code Reference") },
          isError = validationError && answer.isBlank(),
          minLines = 3,
          shape = RoundedCornerShape(14.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_card_answer_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (question.isBlank() || answer.isBlank()) {
            validationError = true
          } else {
            onSaveCard(question.trim(), answer.trim())
          }
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF4F46E5),
          contentColor = Color.White
        ),
        modifier = Modifier.testTag("confirm_add_card_button")
      ) {
        Text(
          text = "Save Card",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = palette.textSecondary)
      }
    }
  )
}

/**
 * Interactive Study Mode Screen (`FlashcardStudyScreen`):
 * - Queue Immersion HUD with Remaining/Mastered pills, active card interval micro-badge, and progress bar.
 * - Realistic 3D Card Flip (`rotationY` 0f -> 180f over 320ms with `cameraDistance = 14f * density`
 *   and inner counter-rotation `rotationY = 180f` when `rotation > 90f` so back text is never mirrored).
 * - Both Front/Back flip surfaces, Completion Card, and top Immersion HUD use `palette.cardFill`,
 *   `palette.glassBorder`, `palette.textPrimary`, and `palette.textSecondary`.
 */
@Composable
fun FlashcardStudyScreen(
  deck: FlashcardDeck,
  metrics: ResponsiveMetrics,
  onRateCard: (cardId: Int, masteryLevel: String) -> Unit,
  onResetMastery: () -> Unit = {},
  onBackToDecks: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val localView = LocalView.current
  val palette = currentGlassPalette()

  BackHandler {
    onBackToDecks()
  }

  val totalDeckSize = deck.cards.size
  val studyQueue = remember(deck.id) {
    mutableStateListOf<FlashcardItem>().apply { addAll(deck.cards) }
  }
  val masteredCards = remember(deck.id) {
    mutableStateListOf<FlashcardItem>().apply {
      addAll(deck.cards.filter { it.isMastered || it.masteryLevel.equals("Good", ignoreCase = true) })
    }
  }
  var isFlipped by remember(deck.id) { mutableStateOf(false) }
  var cardAdvanceStep by remember(deck.id) { mutableIntStateOf(0) }
  var isResetConfirmDialogOpen by rememberSaveable(deck.id) { mutableStateOf(false) }

  val performStudyReset = {
    playCardHapticSound(context, localView)
    onResetMastery()
    masteredCards.clear()
    studyQueue.clear()
    studyQueue.addAll(deck.cards.map { it.copy(masteryLevel = "New", isMastered = false) })
    isFlipped = false
    cardAdvanceStep++
    Toast.makeText(context, "Deck progress reset to 0%", Toast.LENGTH_SHORT).show()
  }

  val currentCard = studyQueue.firstOrNull()

  val againAccentColor = Color(0xFFD9534F)
  val hardAccentColor = Color(0xFFD9822B)
  val goodAccentColor = if (palette.isDark) Color(0xFF34D399) else Color(0xFF059669)

  val activeIntervalLabel = when {
    currentCard == null -> "Deck Completed"
    currentCard.masteryLevel.equals("Again", ignoreCase = true) -> "Repeat in: ~30s"
    currentCard.masteryLevel.equals("Hard", ignoreCase = true) -> "Repeat in: ~2m"
    else -> "Current Interval: New Card"
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .widthIn(max = metrics.maxContentWidth)
      .verticalScroll(rememberScrollState())
      .padding(
        horizontal = metrics.scaledDp(20),
        vertical = metrics.scaledDp(20)
      )
      .testTag("flashcard_study_screen"),
    verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(18))
  ) {
    // Top Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(10))
    ) {
      Surface(
        onClick = {
          playCardHapticSound(context, localView)
          onBackToDecks()
        },
        shape = CircleShape,
        color = palette.cardFill,
        border = palette.glassBorder,
        shadowElevation = 4.dp,
        modifier = Modifier
          .size(48.dp)
          .testTag("study_back_to_decks_icon_button")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Back to Decks",
            tint = palette.textPrimary,
            modifier = Modifier.size(26.dp)
          )
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = deck.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(17),
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "${deck.subjectTag} • Spaced Repetition Queue",
          style = MaterialTheme.typography.labelLarge.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = metrics.scaledSp(12),
            fontWeight = FontWeight.Bold
          ),
          color = palette.accentIndigo,
          modifier = Modifier.testTag("study_progress_label")
        )
      }

      // Top-Right "Reset Deck" Liquid Glass Icon Button
      Surface(
        onClick = {
          playCardHapticSound(context, localView)
          isResetConfirmDialogOpen = true
        },
        shape = CircleShape,
        color = palette.nestedCardFill,
        border = palette.glassBorder,
        shadowElevation = 2.dp,
        modifier = Modifier
          .size(40.dp)
          .testTag("study_top_bar_reset_deck_button")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reset Deck Progress",
            tint = palette.accentIndigo,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // Queue Immersion HUD (Head-Up Display)
    Surface(
      color = palette.cardFill,
      shape = RoundedCornerShape(20.dp),
      border = palette.glassBorder,
      shadowElevation = 4.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("study_immersion_hud")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = metrics.scaledDp(14), vertical = metrics.scaledDp(12)),
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(10))
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left pill: "Remaining: ${studyQueue.size}" (with a subtle amber/red dot)
          Surface(
            color = palette.nestedCardFill,
            shape = CircleShape,
            border = BorderStroke(1.dp, hardAccentColor.copy(alpha = 0.45f)),
            modifier = Modifier.testTag("hud_remaining_pill")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (studyQueue.size > 0) hardAccentColor else goodAccentColor)
              )
              Text(
                text = "Remaining: ${studyQueue.size}",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = metrics.scaledSp(12),
                  fontWeight = FontWeight.Bold
                ),
                color = palette.textPrimary
              )
            }
          }

          // Right pill: "Mastered: ${masteredCards.size}/${deck.cards.size}" (with a green check dot)
          Surface(
            color = palette.nestedCardFill,
            shape = CircleShape,
            border = BorderStroke(1.dp, goodAccentColor.copy(alpha = 0.45f)),
            modifier = Modifier.testTag("hud_mastered_pill")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = goodAccentColor,
                modifier = Modifier.size(13.dp)
              )
              Text(
                text = "Mastered: ${masteredCards.size}/${deck.cards.size}",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = metrics.scaledSp(12),
                  fontWeight = FontWeight.Bold
                ),
                color = palette.textPrimary
              )
            }
          }
        }

        // Active Card Interval Micro-Badge in HUD
        if (currentCard != null) {
          Surface(
            color = palette.nestedCardFill,
            shape = CircleShape,
            border = BorderStroke(
              1.dp,
              when {
                currentCard.masteryLevel.equals("Again", ignoreCase = true) -> againAccentColor.copy(alpha = 0.45f)
                currentCard.masteryLevel.equals("Hard", ignoreCase = true) -> hardAccentColor.copy(alpha = 0.45f)
                else -> palette.accentIndigo.copy(alpha = 0.45f)
              }
            ),
            modifier = Modifier.testTag("hud_interval_micro_badge")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = when {
                  currentCard.masteryLevel.equals("Again", ignoreCase = true) -> againAccentColor
                  currentCard.masteryLevel.equals("Hard", ignoreCase = true) -> hardAccentColor
                  else -> palette.accentIndigo
                },
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = activeIntervalLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = when {
                  currentCard.masteryLevel.equals("Again", ignoreCase = true) -> againAccentColor
                  currentCard.masteryLevel.equals("Hard", ignoreCase = true) -> hardAccentColor
                  else -> palette.accentIndigo
                }
              )
            }
          }
        }

        val progressFraction = if (totalDeckSize == 0) {
          1f
        } else {
          (masteredCards.size.toFloat() / deck.cards.size.toFloat()).coerceIn(0f, 1f)
        }
        val animatedProgress by animateFloatAsState(
          targetValue = progressFraction,
          animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
          label = "study_linear_progress"
        )

        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .testTag("study_progress_bar"),
          color = goodAccentColor,
          trackColor = palette.trackColor
        )
      }
    }

    if (studyQueue.isEmpty() || currentCard == null) {
      // Deck Mastered Celebratory Screen
      Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardFill),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = BorderStroke(1.5.dp, goodAccentColor.copy(alpha = 0.55f)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = metrics.scaledDp(12))
          .testTag("deck_completed_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(metrics.scaledDp(26)),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
        ) {
          Surface(
            shape = CircleShape,
            color = goodAccentColor.copy(alpha = 0.16f),
            modifier = Modifier.size(72.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Deck Mastered!",
                tint = goodAccentColor,
                modifier = Modifier.size(42.dp)
              )
            }
          }

          Surface(
            color = goodAccentColor.copy(alpha = 0.14f),
            shape = CircleShape
          ) {
            Text(
              text = "Deck Mastered!",
              style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.ExtraBold
              ),
              color = goodAccentColor,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
          }

          Text(
            text = "All Cards Mastered!",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontSize = metrics.scaledSp(26),
              fontWeight = FontWeight.ExtraBold
            ),
            color = palette.textPrimary,
            textAlign = TextAlign.Center
          )

          Text(
            text = "You cycled through every difficult card until 100% mastery was achieved.",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = metrics.scaledSp(15),
              lineHeight = metrics.scaledSp(22),
              fontWeight = FontWeight.Medium
            ),
            color = palette.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("deck_completed_total_text")
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = {
                playCardHapticSound(context, localView)
                // Keep persisted mastered progress intact when tapping "Study Again"
                studyQueue.clear()
                studyQueue.addAll(deck.cards)
                isFlipped = false
                cardAdvanceStep++
              },
              shape = RoundedCornerShape(20.dp),
              border = BorderStroke(1.2.dp, palette.accentIndigo.copy(alpha = 0.65f)),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = palette.nestedCardFill,
                contentColor = palette.accentIndigo
              ),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("restart_deck_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Study Again",
                  tint = palette.accentIndigo,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Study Again",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  softWrap = false,
                  color = palette.accentIndigo
                )
              }
            }

            Button(
              onClick = {
                playCardHapticSound(context, localView)
                onBackToDecks()
              },
              shape = RoundedCornerShape(20.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
              ),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .background(
                  brush = Brush.horizontalGradient(
                    listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                  ),
                  shape = RoundedCornerShape(20.dp)
                )
                .clip(RoundedCornerShape(20.dp))
                .testTag("back_to_decks_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Text(
                  text = "All Decks",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  softWrap = false,
                  color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.ArrowForward,
                  contentDescription = "All Decks",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          TextButton(
            onClick = {
              performStudyReset()
            },
            shape = CircleShape,
            colors = ButtonDefaults.textButtonColors(
              containerColor = palette.nestedCardFill,
              contentColor = palette.textSecondary
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier
              .border(
                palette.nestedBorder,
                CircleShape
              )
              .testTag("reset_deck_mastery_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = palette.textSecondary,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "Reset Mastery Progress",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = palette.textSecondary
              )
            }
          }
        }
      }
    } else {
      // Smooth Card Advance Animation + 3D Card Flip
      AnimatedContent(
        targetState = currentCard to cardAdvanceStep,
        transitionSpec = {
          (
            scaleIn(
              initialScale = 0.95f,
              animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 220))
          ).togetherWith(
            slideOutOfContainer(
              towards = AnimatedContentTransitionScope.SlideDirection.Left,
              animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 220))
          )
        },
        label = "flashcard_advance_transition"
      ) { (animatedCard, _) ->
        val rotation by animateFloatAsState(
          targetValue = if (isFlipped) 180f else 0f,
          animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
          label = "card_flip_rotation"
        )
        val showingBack = rotation > 90f
        val cardIntervalBadgeText = when {
          animatedCard.masteryLevel.equals("Again", ignoreCase = true) -> "Repeat in: ~30s"
          animatedCard.masteryLevel.equals("Hard", ignoreCase = true) -> "Repeat in: ~2m"
          else -> "Current Interval: New Card"
        }

        // Interactive Centered Flashcard as a Tactile Frosted Glass Slab with 3D Rotation
        LiquidGlassCard(
          onClick = {
            playCardHapticSound(context, localView)
            isFlipped = !isFlipped
          },
          shape = RoundedCornerShape(28.dp),
          elevation = 14.dp,
          fillAlpha = 0.78f,
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = metrics.scaledDp(320))
            .graphicsLayer {
              rotationY = rotation
              cameraDistance = 14f * density
            }
            .testTag("interactive_flashcard")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = metrics.scaledDp(320))
              .graphicsLayer {
                if (showingBack) {
                  rotationY = 180f
                }
              }
              .padding(metrics.scaledDp(24)),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Top Row: Side Chip ("QUESTION" vs "ANSWER / CODE PROVISION") + Interval Micro-Badge
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                color = if (showingBack) {
                  goodAccentColor.copy(alpha = 0.16f)
                } else {
                  LiquidRoyalPeriwinkle.copy(alpha = 0.16f)
                },
                shape = CircleShape,
                border = BorderStroke(
                  1.dp,
                  if (showingBack) goodAccentColor.copy(alpha = 0.4f) else LiquidRoyalPeriwinkle.copy(alpha = 0.35f)
                )
              ) {
                Text(
                  text = if (showingBack) "ANSWER / CODE PROVISION" else "QUESTION",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = metrics.scaledSp(11),
                    fontWeight = FontWeight.ExtraBold
                  ),
                  color = if (showingBack) {
                    goodAccentColor
                  } else {
                    palette.accentIndigo
                  },
                  modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("flashcard_side_chip")
                )
              }

              // Card Header Micro-Badge ("Current Interval: New Card" / "Repeat in: ~30s" / "Repeat in: ~2m")
              Surface(
                color = palette.nestedCardFill,
                shape = CircleShape,
                border = palette.nestedBorder,
                modifier = Modifier.testTag("card_interval_micro_badge")
              ) {
                Text(
                  text = cardIntervalBadgeText,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = palette.textSecondary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }

            // Middle Content: Bold Question Text or Detailed Answer + Provision Reference
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = metrics.scaledDp(20)),
              contentAlignment = Alignment.Center
            ) {
              if (!showingBack) {
                Text(
                  text = animatedCard.question,
                  style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = metrics.scaledSp(21),
                    fontWeight = FontWeight.Bold,
                    lineHeight = metrics.scaledSp(29)
                  ),
                  color = palette.textPrimary,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag("flashcard_question_text")
                )
              } else {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
                ) {
                  Text(
                    text = animatedCard.answer,
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontSize = metrics.scaledSp(19),
                      fontWeight = FontWeight.Bold,
                      lineHeight = metrics.scaledSp(27)
                    ),
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("flashcard_answer_text")
                  )

                  Surface(
                    color = palette.nestedCardFill,
                    shape = RoundedCornerShape(12.dp),
                    border = palette.nestedBorder
                  ) {
                    Text(
                      text = "Ref: ${animatedCard.category} • Mastery: ${animatedCard.masteryLevel}",
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = metrics.scaledSp(11),
                        fontWeight = FontWeight.SemiBold
                      ),
                      color = palette.accentIndigo,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                  }
                }
              }
            }

            // Bottom Helper Tip
            Text(
              text = if (!showingBack) "(Tap card to 3D flip)" else "(Tap card to view question)",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textSecondary,
              textAlign = TextAlign.Center
            )
          }
        }
      }

      // Bottom Spaced Repetition Glossy Frosted Action Buttons ("Again", "Hard", "Good") with Time Interval Badges
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
      ) {
        Text(
          text = "Rate your recall to schedule next review:",
          style = MaterialTheme.typography.labelMedium.copy(
            fontSize = metrics.scaledSp(12),
            fontWeight = FontWeight.SemiBold
          ),
          color = palette.textSecondary
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(10))
        ) {
          // 1. Again ("Again" + "< 30s" sub-label badge — Soft Coral / Crimson Frost)
          Box(
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 58.dp)
              .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = againAccentColor.copy(alpha = 0.35f),
                spotColor = againAccentColor.copy(alpha = 0.35f)
              )
              .clip(RoundedCornerShape(20.dp))
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                )
              )
              .border(
                BorderStroke(1.2.dp, Color.White.copy(alpha = 0.58f)),
                RoundedCornerShape(20.dp)
              )
              .clickable {
                playCardHapticSound(context, localView)
                if (studyQueue.isNotEmpty()) {
                  val activeCard = studyQueue.first()
                  onRateCard(activeCard.id, "Again")
                  val updatedCard = activeCard.copy(masteryLevel = "Again")
                  if (studyQueue.size > 2) {
                    studyQueue.removeAt(0)
                    studyQueue.add(2, updatedCard)
                  } else {
                    studyQueue.removeAt(0)
                    studyQueue.add(updatedCard)
                  }
                  isFlipped = false
                  cardAdvanceStep++
                }
              }
              .padding(vertical = 8.dp, horizontal = 6.dp)
              .testTag("rating_again_button"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = "Again",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontSize = metrics.scaledSp(14),
                  fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
              )
              Surface(
                color = Color.White.copy(alpha = 0.22f),
                shape = CircleShape
              ) {
                Text(
                  text = "< 30s",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                )
              }
            }
          }

          // 2. Hard ("Hard" + "2 min" sub-label badge — Warm Amber / Honey Frost)
          Box(
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 58.dp)
              .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = hardAccentColor.copy(alpha = 0.35f),
                spotColor = hardAccentColor.copy(alpha = 0.35f)
              )
              .clip(RoundedCornerShape(20.dp))
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                )
              )
              .border(
                BorderStroke(1.2.dp, Color.White.copy(alpha = 0.58f)),
                RoundedCornerShape(20.dp)
              )
              .clickable {
                playCardHapticSound(context, localView)
                if (studyQueue.isNotEmpty()) {
                  val activeCard = studyQueue.removeAt(0)
                  onRateCard(activeCard.id, "Hard")
                  studyQueue.add(activeCard.copy(masteryLevel = "Hard"))
                  isFlipped = false
                  cardAdvanceStep++
                }
              }
              .padding(vertical = 8.dp, horizontal = 6.dp)
              .testTag("rating_hard_button"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = "Hard",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontSize = metrics.scaledSp(14),
                  fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
              )
              Surface(
                color = Color.White.copy(alpha = 0.22f),
                shape = CircleShape
              ) {
                Text(
                  text = "2 min",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                )
              }
            }
          }

          // 3. Good ("Good" + "Mastered" sub-label badge — Sage / Emerald Frost)
          Box(
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 58.dp)
              .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = goodAccentColor.copy(alpha = 0.35f),
                spotColor = goodAccentColor.copy(alpha = 0.35f)
              )
              .clip(RoundedCornerShape(20.dp))
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFF10B981), Color(0xFF059669))
                )
              )
              .border(
                BorderStroke(1.2.dp, Color.White.copy(alpha = 0.58f)),
                RoundedCornerShape(20.dp)
              )
              .clickable {
                playCardHapticSound(context, localView)
                if (studyQueue.isNotEmpty()) {
                  val activeCard = studyQueue.removeAt(0)
                  activeCard.isMastered = true
                  activeCard.masteryLevel = "Good"
                  onRateCard(activeCard.id, "Good")
                  if (masteredCards.none { it.id == activeCard.id }) {
                    masteredCards.add(activeCard.copy(masteryLevel = "Good", isMastered = true))
                  }
                  isFlipped = false
                  cardAdvanceStep++
                }
              }
              .padding(vertical = 8.dp, horizontal = 6.dp)
              .testTag("rating_good_button"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = "Good",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontSize = metrics.scaledSp(14),
                  fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
              )
              Surface(
                color = Color.White.copy(alpha = 0.22f),
                shape = CircleShape
              ) {
                Text(
                  text = "Mastered",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  if (isResetConfirmDialogOpen) {
    ResetMasteryConfirmDialog(
      cardCount = deck.cards.size,
      metrics = metrics,
      onDismiss = { isResetConfirmDialogOpen = false },
      onConfirmReset = {
        isResetConfirmDialogOpen = false
        performStudyReset()
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
  val palette = currentGlassPalette()
  LiquidGlassCard(
    onClick = onClick,
    shape = RoundedCornerShape(26.dp),
    elevation = 10.dp,
    fillAlpha = 0.72f,
    modifier = modifier
      .height(metrics.scaledDp(168))
      .testTag("extra_tool_card_${tool.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(metrics.scaledDp(18)),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Luminous Gradient Icon Badge with Specular Rim
      Box(
        modifier = Modifier
          .size(50.dp)
          .shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(16.dp),
            ambientColor = LiquidDeepIndigo.copy(alpha = 0.30f),
            spotColor = LiquidElectricViolet.copy(alpha = 0.30f)
          )
          .clip(RoundedCornerShape(16.dp))
          .background(brush = LiquidOrbBrush)
          .border(
            BorderStroke(1.2.dp, Color.White.copy(alpha = 0.65f)),
            RoundedCornerShape(16.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = tool.icon,
          contentDescription = tool.title,
          tint = Color.White,
          modifier = Modifier.size(26.dp)
        )
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
          color = palette.textPrimary,
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
          color = palette.textSecondary,
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
  val palette = currentGlassPalette()
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
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f)
              .testTag("schedule_screen_title")
          )

          val toggleFillColor = if (palette.isDark) Color(0xFF1E293B) else Color(0xFFEEF2FF)
          val toggleContentColor = if (palette.isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
          Box(
            modifier = Modifier
              .height(42.dp)
              .clip(RoundedCornerShape(50))
              .background(color = toggleFillColor, shape = RoundedCornerShape(50))
              .border(
                BorderStroke(
                  width = 1.2.dp,
                  color = Color(0xFF6366F1).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(50)
              )
              .clickable {
                isGridView = !isGridView
                if (!isGridView) focusedClass = null
              }
              .padding(horizontal = 14.dp, vertical = 6.dp)
              .testTag("schedule_view_toggle_button"),
            contentAlignment = Alignment.Center
          ) {
            Row(
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
                tint = toggleContentColor,
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
                color = toggleContentColor,
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
              val dayPillShape = RoundedCornerShape(20.dp)
              val dayPillBrush = if (isSelected) {
                Brush.verticalGradient(
                  listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                )
              } else if (palette.isDark) {
                Brush.verticalGradient(
                  listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                )
              } else {
                Brush.verticalGradient(
                  listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                )
              }
              val dayPillBorder = if (isSelected) {
                BorderStroke(1.2.dp, Color.White.copy(alpha = 0.45f))
              } else if (palette.isDark) {
                BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
              } else {
                BorderStroke(1.dp, Color(0xFFCBD5E1))
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(76.dp)
                  .clip(dayPillShape)
                  .background(brush = dayPillBrush, shape = dayPillShape)
                  .border(border = dayPillBorder, shape = dayPillShape)
                  .clickable { onSelectWeekDay(day) }
                  .testTag("week_day_tab_${day.shortLabel.lowercase()}")
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = day.shortLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = if (isSelected) {
                      Color.White
                    } else {
                      palette.textPrimary
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )

                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(
                        color = if (isSelected) {
                          Color.White.copy(alpha = 0.22f)
                        } else if (palette.isDark) {
                          Color(0xFF334155)
                        } else {
                          Color(0xFFEEF2FF)
                        },
                        shape = CircleShape
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "$countForDay",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                      ),
                      color = if (isSelected) {
                        Color.White
                      } else {
                        palette.textSecondary
                      },
                      maxLines = 1
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
              color = palette.textPrimary,
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
              color = palette.textSecondary,
              maxLines = 1
            )
          }
        }

        // 3. Visual Timetable Cards for the Selected Day (chronologically sorted)
        if (filteredSchedules.isEmpty()) {
          item {
            LiquidGlassCard(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("empty_schedule_day_card"),
              shape = RoundedCornerShape(24.dp),
              elevation = 8.dp
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
                  tint = palette.accentIndigo,
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
                  color = palette.textPrimary,
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
                  color = palette.textSecondary,
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

    // Full-screen Long-Press Details Overlay with performant 0.6f Black Scrim & Glass Dialog Surface Card
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
            containerColor = palette.dialogSurface
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
              color = palette.textPrimary,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("focused_class_subject_text")
            )

            HorizontalDivider(
              color = palette.dividerColor,
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
                color = palette.textPrimary,
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
                color = palette.textSecondary,
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
  val palette = currentGlassPalette()
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

  val gridLineColor = palette.dividerColor
  val lunchPurpleColor = MaterialTheme.colorScheme.primary
  val creamSurface = palette.cardFill
  val ivoryCellSurface = palette.nestedCardFill

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
    border = BorderStroke(1.5.dp, palette.cardBorder)
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
            color = palette.textSecondary,
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
                  palette.accentIndigo.copy(alpha = 0.16f)
                } else {
                  Color.Transparent
                }
              )
              .border(
                width = if (isSelectedDay) 1.5.dp else 0.5.dp,
                color = if (isSelectedDay) palette.accentIndigo else gridLineColor
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
                palette.accentIndigo
              } else {
                palette.textPrimary
              },
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      HorizontalDivider(thickness = 1.5.dp, color = gridLineColor)

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
              .background(palette.nestedCardFill)
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
                color = palette.accentIndigo
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
                    palette.accentIndigo
                  } else {
                    palette.textPrimary
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
                      shape = RoundedCornerShape(10.dp),
                      color = positioned.blockColor,
                      border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.78f)),
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
 */
@Composable
private fun AddClassScheduleDialog(
  initialDay: ScheduleWeekDay = resolveInitialScheduleWeekDay(),
  editingSchedule: ClassSchedule? = null,
  onDismiss: () -> Unit,
  onSaveSchedule: (String, String, List<String>, String, Long) -> Unit
) {
  val metrics = rememberResponsiveMetrics()
  val palette = currentGlassPalette()
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

  val dialogSurfaceColor = palette.dialogSurface
  val chipUnselectedColor = palette.chipUnselectedFill

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
      border = BorderStroke(1.2.dp, palette.cardBorder),
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
          color = palette.textPrimary,
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
              color = palette.textMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = palette.textPrimary
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = sibilOutlinedTextFieldColors(),
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
              color = palette.textMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.LocationOn,
              contentDescription = null,
              tint = palette.accentIndigo,
              modifier = Modifier.size(20.dp)
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = palette.textPrimary
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = sibilOutlinedTextFieldColors(),
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
                color = palette.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = palette.accentIndigo,
                modifier = Modifier.size(20.dp)
              )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              fontSize = metrics.scaledSp(15),
              color = palette.textPrimary
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = sibilOutlinedTextFieldColors(),
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
                color = palette.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = palette.accentIndigo,
                modifier = Modifier.size(20.dp)
              )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              fontSize = metrics.scaledSp(15),
              color = palette.textPrimary
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = sibilOutlinedTextFieldColors(),
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
            color = palette.textPrimary
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
                        palette.chipUnselectedBorder
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
                        Color.White
                      } else {
                        palette.textPrimary
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
                    palette.textPrimary
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
              color = palette.textSecondary,
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
  val palette = currentGlassPalette()
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = palette.inputFill,
    border = BorderStroke(1.dp, palette.inputUnfocusedBorder),
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
                Color.White
              } else {
                palette.textSecondary
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
 * corner of the Dashboard. Houses the Account, Display (Dark Mode switch),
 * Data Management, and About cards with floating elevation (`8.dp`).
 */
@Composable
private fun ProfileAndSettingsDialog(
  viewModel: TaskViewModel,
  onDismiss: () -> Unit,
  onSwitchAccount: () -> Unit
) {
  val metrics = rememberResponsiveMetrics()
  val palette = currentGlassPalette()
  val isGuest = viewModel.isGuestMode

  val modalBackgroundColor = if (palette.isDark) Color(0xFF0B1120) else Color(0xFFF4F7FB)

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
      color = modalBackgroundColor,
      border = palette.glassBorder,
      tonalElevation = 0.dp,
      shadowElevation = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(
            start = metrics.scaledDp(20),
            end = metrics.scaledDp(20),
            top = metrics.scaledDp(20),
            bottom = metrics.scaledDp(28)
          ),
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
            color = palette.textPrimary,
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
              tint = palette.textSecondary,
              modifier = Modifier.size(24.dp)
            )
          }
        }

        // 1. Account Section Card
        LiquidGlassCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("extras_account_card"),
          shape = RoundedCornerShape(24.dp),
          elevation = if (palette.isDark) 8.dp else 6.dp
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
              color = palette.textPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
            ) {
              Surface(
                color = palette.nestedCardFill,
                shape = CircleShape,
                modifier = Modifier.size(54.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Account Profile Icon",
                    tint = palette.accentIndigo,
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
                  color = palette.textPrimary,
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
                    color = palette.textPrimary,
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
                    color = palette.textSecondary,
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
              border = BorderStroke(1.2.dp, Color(0xFF6366F1)),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = palette.nestedCardFill,
                contentColor = palette.accentIndigo
              ),
              elevation = null,
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
                color = palette.accentIndigo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        // 2. Display Section Card (with functional Dark Mode Switch)
        LiquidGlassCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("extras_display_card"),
          shape = RoundedCornerShape(24.dp),
          elevation = if (palette.isDark) 8.dp else 6.dp
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
              color = palette.textPrimary,
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
                  color = palette.nestedCardFill,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.size(40.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Filled.DarkMode,
                      contentDescription = null,
                      tint = palette.accentIndigo,
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
                  color = palette.textPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
              }

              Switch(
                checked = viewModel.isDarkMode,
                onCheckedChange = { isChecked -> viewModel.updateDarkMode(isChecked) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = MaterialTheme.colorScheme.primary,
                  uncheckedThumbColor = palette.textSecondary,
                  uncheckedTrackColor = palette.progressTrack
                ),
                modifier = Modifier.testTag("dark_mode_switch")
              )
            }
          }
        }

        // 3. Data Management Section Card (One-Tap Backup & Restore)
        val context = LocalContext.current
        var isRestoreDialogOpen by rememberSaveable { mutableStateOf(false) }
        var restoreJsonInput by rememberSaveable { mutableStateOf("") }
        var restoreErrorText by rememberSaveable { mutableStateOf<String?>(null) }

        LiquidGlassCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("extras_data_management_card"),
          shape = RoundedCornerShape(24.dp),
          elevation = if (palette.isDark) 8.dp else 6.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(metrics.scaledDp(18)),
            verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
          ) {
            Text(
              text = "Data Management",
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = metrics.scaledSp(18),
                fontWeight = FontWeight.Bold
              ),
              color = palette.textPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Text(
              text = "Back up your Schedules, Tasks, Alarms, Flashcards, and Profile to your clipboard or restore from a saved backup string.",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = metrics.scaledSp(13)
              ),
              color = palette.textSecondary
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(10))
            ) {
              Button(
                onClick = {
                  val backupJson = viewModel.createBackupJsonString(context)
                  val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                  val clip = ClipData.newPlainText("Sibil Data Backup", backupJson)
                  clipboard?.setPrimaryClip(clip)
                  Toast.makeText(
                    context,
                    "All Sibil data copied to clipboard! Paste and save it in your notes.",
                    Toast.LENGTH_LONG
                  ).show()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                  .weight(1f)
                  .heightIn(min = 48.dp)
                  .testTag("backup_data_button")
              ) {
                Text(
                  text = "Backup Data",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = metrics.scaledSp(13),
                    fontWeight = FontWeight.Bold
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              OutlinedButton(
                onClick = {
                  restoreErrorText = null
                  isRestoreDialogOpen = true
                },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, Color(0xFF6366F1)),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = palette.nestedCardFill,
                  contentColor = palette.accentIndigo
                ),
                elevation = null,
                modifier = Modifier
                  .weight(1f)
                  .heightIn(min = 48.dp)
                  .testTag("restore_data_button")
              ) {
                Text(
                  text = "Restore Data",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = metrics.scaledSp(13),
                    fontWeight = FontWeight.Bold
                  ),
                  color = palette.accentIndigo,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        if (isRestoreDialogOpen) {
          AlertDialog(
            onDismissRequest = { isRestoreDialogOpen = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = modalBackgroundColor,
            title = {
              Text(
                text = "Restore Sibil Data",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = metrics.scaledSp(19),
                  fontWeight = FontWeight.Bold
                ),
                color = palette.textPrimary
              )
            },
            text = {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Text(
                  text = "Paste your previously copied Sibil backup JSON string below to restore all Schedules, Tasks, Alarms, Flashcards, and Profile data.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = metrics.scaledSp(13)
                  ),
                  color = palette.textSecondary
                )

                OutlinedTextField(
                  value = restoreJsonInput,
                  onValueChange = {
                    restoreJsonInput = it
                    if (it.isNotBlank()) restoreErrorText = null
                  },
                  label = { Text("Backup JSON String") },
                  placeholder = { Text("{\"app\":\"Sibil\", ...}") },
                  isError = restoreErrorText != null,
                  minLines = 5,
                  maxLines = 10,
                  shape = RoundedCornerShape(14.dp),
                  colors = sibilOutlinedTextFieldColors(),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restore_backup_json_input")
                )

                if (restoreErrorText != null) {
                  Text(
                    text = restoreErrorText.orEmpty(),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.SemiBold
                    ),
                    color = palette.accentRed
                  )
                }
              }
            },
            confirmButton = {
              Button(
                onClick = {
                  val restored = viewModel.restoreFromBackupJsonString(context, restoreJsonInput)
                  if (restored) {
                    isRestoreDialogOpen = false
                    restoreJsonInput = ""
                    restoreErrorText = null
                    Toast.makeText(
                      context,
                      "Data restored successfully!",
                      Toast.LENGTH_LONG
                    ).show()
                  } else {
                    restoreErrorText = "Invalid backup JSON string. Please check and try again."
                  }
                },
                shape = CircleShape,
                modifier = Modifier.testTag("confirm_restore_data_button")
              ) {
                Text(
                  text = "Confirm Restore",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
              }
            },
            dismissButton = {
              TextButton(onClick = { isRestoreDialogOpen = false }) {
                Text("Cancel", color = palette.textSecondary)
              }
            }
          )
        }

        // 4. About Section Card
        LiquidGlassCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("extras_about_card"),
          shape = RoundedCornerShape(24.dp),
          elevation = if (palette.isDark) 8.dp else 6.dp
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
                tint = palette.accentIndigo,
                modifier = Modifier.size(22.dp)
              )
              Text(
                text = stringResource(R.string.about_section_title),
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = metrics.scaledSp(18),
                  fontWeight = FontWeight.Bold
                ),
                color = palette.textPrimary,
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
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .weight(1f)
                  .testTag("about_app_name_text")
              )

              Surface(
                color = palette.nestedCardFill,
                shape = CircleShape
              ) {
                Text(
                  text = SIBIL_APP_VERSION_TAG,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = metrics.scaledSp(12),
                    fontWeight = FontWeight.Bold
                  ),
                  color = palette.accentIndigo,
                  maxLines = 1,
                  modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .testTag("about_app_version_text")
                )
              }
            }

            HorizontalDivider(color = palette.dividerColor)

            Text(
              text = stringResource(R.string.developed_by_text),
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = metrics.scaledSp(14),
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textSecondary,
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
 *    - Highlights the currently selected folder using `LiquidVioletChipGradient`, while
 *      unselected folders use the adaptive `GlassPalette` chip styling.
 * 2. Main `LazyColumn` below the folders displaying ONLY tasks that match the currently selected
 *    folder's `subjectCategory`.
 * 3. Task cards styled with `LiquidGlassCard` and high-contrast Light/Dark `GlassPalette` tokens.
 */
@Composable
fun CourseworkTaskManagerScreen(
  viewModel: TaskViewModel,
  modifier: Modifier = Modifier
) {
  val metrics = rememberResponsiveMetrics()
  val palette = currentGlassPalette()
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
          color = palette.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .weight(1f)
            .testTag("tasks_screen_title")
        )

        Surface(
          color = palette.accentIndigo.copy(alpha = 0.16f),
          shape = CircleShape,
          border = BorderStroke(1.dp, palette.accentIndigo.copy(alpha = 0.35f))
        ) {
          Text(
            text = "$remainingInFolderCount Active",
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = metrics.scaledSp(12),
              fontWeight = FontWeight.Bold
            ),
            color = palette.accentIndigo,
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

    // 2. Dynamic Subject Folders Horizontal Scrollable Row (Floating Glass Chips -> Vibrant Violet when selected)
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
          val chipTextColor = if (isSelected) {
            Color.White
          } else {
            palette.textPrimary
          }

          Box(
            modifier = Modifier
              .height(metrics.scaledDp(44))
              .shadow(
                elevation = if (isSelected) 10.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isSelected) LiquidDeepIndigo.copy(alpha = 0.35f) else palette.cardAmbientShadow,
                spotColor = if (isSelected) LiquidElectricViolet.copy(alpha = 0.35f) else palette.cardSpotShadow
              )
              .clip(RoundedCornerShape(20.dp))
              .background(
                brush = if (isSelected) {
                  LiquidVioletChipGradient
                } else {
                  Brush.linearGradient(
                    listOf(
                      palette.chipUnselectedFill,
                      palette.nestedCardFill
                    )
                  )
                }
              )
              .border(
                border = if (isSelected) {
                  BorderStroke(1.2.dp, Color.White.copy(alpha = 0.70f))
                } else {
                  BorderStroke(1.1.dp, palette.chipUnselectedBorder)
                },
                shape = RoundedCornerShape(20.dp)
              )
              .clickable { viewModel.selectTaskFolder(folderName) }
              .testTag("task_folder_chip_${folderName.lowercase(Locale.US).replace(" ", "_")}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxHeight()
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
                  Color.White
                } else {
                  palette.accentIndigo
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
                    palette.accentIndigo.copy(alpha = 0.16f)
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
                      Color.White
                    } else {
                      palette.accentIndigo
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
        LiquidGlassCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .testTag("empty_tasks_card"),
          shape = RoundedCornerShape(26.dp),
          elevation = 8.dp
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
              tint = palette.accentIndigo,
              modifier = Modifier.size(36.dp)
            )
            Text(
              text = "No tasks in \"$activeFolder\"",
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = metrics.scaledSp(19),
                fontWeight = FontWeight.Bold
              ),
              color = palette.textPrimary,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "Tap the + button in the bottom corner to add a task to this subject folder.",
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = metrics.scaledSp(15)
              ),
              color = palette.textSecondary,
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
 * Frosted Glass Task row card (`LiquidGlassCard`) with custom rounded gradient checkbox,
 * specular highlight border, and high-contrast typography bound to `GlassPalette`.
 */
@Composable
private fun TaskItemRowCard(
  task: Task,
  metrics: ResponsiveMetrics,
  onToggle: () -> Unit,
  onDelete: () -> Unit
) {
  val palette = currentGlassPalette()
  val titleTextColor = if (task.isCompleted) {
    palette.textSecondary
  } else {
    palette.textPrimary
  }
  val calendarAccentColor = if (task.isCompleted) {
    palette.textSecondary
  } else {
    palette.accentIndigo
  }

  LiquidGlassCard(
    onClick = onToggle,
    shape = RoundedCornerShape(24.dp),
    elevation = 8.dp,
    fillAlpha = if (palette.isDark) 0.78f else 0.90f,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("task_item_card_${task.id}")
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
      // Custom Rounded Checkbox: 1.5.dp border of #818CF8 (Dark) / #4F46E5 (Light) and translucent fill when unchecked,
      // filling with a vibrant indigo/violet gradient when checked.
      Box(
        modifier = Modifier
          .size(34.dp)
          .minimumInteractiveComponentSize()
          .clip(RoundedCornerShape(10.dp))
          .clickable(onClick = onToggle)
          .testTag("task_checkbox_${task.id}"),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(25.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
              brush = if (task.isCompleted) {
                LiquidVioletChipGradient
              } else {
                Brush.linearGradient(
                  listOf(
                    palette.checkboxUncheckedFill,
                    palette.checkboxUncheckedFill
                  )
                )
              }
            )
            .border(
              border = if (task.isCompleted) {
                BorderStroke(1.2.dp, Color.White.copy(alpha = 0.70f))
              } else {
                BorderStroke(1.5.dp, palette.checkboxUncheckedBorder)
              },
              shape = RoundedCornerShape(8.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          if (task.isCompleted) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = "Completed",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }
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
              tint = calendarAccentColor,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = "Due: ${task.dueDate}",
              style = MaterialTheme.typography.labelMedium.copy(
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )
          }

          Surface(
            color = if (task.isCompleted) {
              palette.nestedCardFill
            } else {
              palette.accentIndigo.copy(alpha = 0.15f)
            },
            shape = CircleShape,
            border = BorderStroke(1.dp, palette.accentIndigo.copy(alpha = 0.30f))
          ) {
            Text(
              text = task.subjectCategory.ifBlank { DEFAULT_TASK_CATEGORY },
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = metrics.scaledSp(11),
                fontWeight = FontWeight.Bold
              ),
              color = if (task.isCompleted) palette.textSecondary else palette.accentIndigo,
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
          tint = palette.accentRed,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

/**
 * Glass-styled Dialog for adding a new coursework task:
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
  val palette = currentGlassPalette()
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
    containerColor = palette.dialogSurface,
    title = {
      Text(
        text = stringResource(R.string.add_task_dialog_title),
        style = MaterialTheme.typography.headlineMedium.copy(
          fontSize = metrics.scaledSp(21),
          fontWeight = FontWeight.Bold
        ),
        color = palette.textPrimary,
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
              color = palette.textMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = palette.textPrimary
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = sibilOutlinedTextFieldColors(),
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
              color = palette.textMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.CalendarToday,
              contentDescription = null,
              tint = palette.accentIndigo,
              modifier = Modifier.size(22.dp)
            )
          },
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = metrics.scaledSp(15),
            color = palette.textPrimary
          ),
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = sibilOutlinedTextFieldColors(),
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
            color = palette.textPrimary,
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
                  palette.chipUnselectedFill
                },
                border = if (isSelected) {
                  null
                } else {
                  BorderStroke(1.dp, palette.chipUnselectedBorder)
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
                      Color.White
                    } else {
                      palette.accentIndigo
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
                      Color.White
                    } else {
                      palette.textPrimary
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
          color = palette.textSecondary,
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

// ============================================================================
// AUDIO, CONTINUOUS LOOPING MEDIAPLAYER, SNOOZE & SYSTEM NOTIFICATION HELPERS
// ============================================================================

const val SIBIL_ALARM_CHANNEL_ID = "sibil_alarm_channel"
const val SIBIL_ALARM_CHANNEL_NAME = "Sibil Alarms & Timers"
const val TIMER_NOTIFICATION_CHANNEL_ID = SIBIL_ALARM_CHANNEL_ID
const val TIMER_NOTIFICATION_CHANNEL_NAME = SIBIL_ALARM_CHANNEL_NAME
private const val TIMER_NOTIFICATION_ID = 4001
private const val TIMER_ALARM_REQUEST_CODE = 4002

var currentRingtone: Ringtone? = null
var isAlarmSoundRinging by mutableStateOf(false)
var activeRingingAlertMessage by mutableStateOf<String?>(null)
var ringingAlertTitle by mutableStateOf<String?>(null)
var snoozeTargetEpochMillis by mutableStateOf<Long?>(null)
var snoozeTargetTitle by mutableStateOf<String?>(null)

/**
 * Continuous Audio Engine (`AlarmSoundController` singleton object):
 * - Uses `android.media.MediaPlayer` with `AudioAttributes.USAGE_ALARM` and `CONTENT_TYPE_SONIFICATION`.
 * - Sets `mediaPlayer.isLooping = true` before calling `prepare()` and `start()` so the alarm/timer
 *   continues ringing until the user explicitly taps "Stop" or "Snooze (5m)".
 */
object AlarmSoundController {
  private var mediaPlayer: MediaPlayer? = null

  @Synchronized
  fun startRinging(context: Context) {
    stopMediaPlayerOnly()
    val appCtx = context.applicationContext
    val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

    var startedWithMediaPlayer = false
    if (alarmUri != null) {
      try {
        val player = MediaPlayer()
        player.setDataSource(appCtx, alarmUri)
        player.setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        player.isLooping = true
        player.prepare()
        player.start()
        mediaPlayer = player
        startedWithMediaPlayer = true
      } catch (_: Exception) {
        try {
          mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
      }
    }

    if (!startedWithMediaPlayer && alarmUri != null) {
      try {
        val fallbackRingtone = RingtoneManager.getRingtone(appCtx, alarmUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          fallbackRingtone?.isLooping = true
        }
        currentRingtone = fallbackRingtone
        fallbackRingtone?.play()
      } catch (_: Exception) {
      }
    }

    isAlarmSoundRinging = true
  }

  @Synchronized
  private fun stopMediaPlayerOnly() {
    try {
      mediaPlayer?.let { player ->
        if (player.isPlaying) {
          player.stop()
        }
        player.reset()
        player.release()
      }
    } catch (_: Exception) {
    }
    mediaPlayer = null

    try {
      currentRingtone?.stop()
    } catch (_: Exception) {
    }
    currentRingtone = null
  }

  @Synchronized
  fun stopRinging() {
    stopMediaPlayerOnly()
    isAlarmSoundRinging = false
    activeRingingAlertMessage = null
    ringingAlertTitle = null
  }
}

/**
 * Legacy-compatible wrapper delegating to `AlarmSoundController.startRinging(context)`.
 */
fun playAlarmSound(context: Context) {
  AlarmSoundController.startRinging(context)
}

/**
 * Legacy-compatible wrapper delegating to `AlarmSoundController.stopRinging()`.
 */
fun stopAlarmSound() {
  AlarmSoundController.stopRinging()
}

fun playNativeAlarmSound(context: Context): Ringtone? {
  AlarmSoundController.startRinging(context)
  return currentRingtone
}

/**
 * Creates the high-importance Notification Channel `"sibil_alarm_channel"` ("Sibil Alarms & Timers").
 */
fun createClockNotificationChannel(context: Context) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
      SIBIL_ALARM_CHANNEL_ID,
      SIBIL_ALARM_CHANNEL_NAME,
      NotificationManager.IMPORTANCE_HIGH
    ).apply {
      description = "High-priority alerts for Sibil Alarms and Countdown Timers"
      enableVibration(true)
    }
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    notificationManager?.createNotificationChannel(channel)
  }
}

fun createTimerNotificationChannel(context: Context) {
  createClockNotificationChannel(context)
}

/**
 * System Notification Helper:
 * Creates the `"sibil_alarm_channel"` (`IMPORTANCE_HIGH`) and displays a high-priority system
 * notification whenever an Alarm triggers or a Timer reaches `00:00:00`.
 */
@SuppressLint("MissingPermission")
fun showClockNotification(context: Context, title: String, message: String) {
  createClockNotificationChannel(context)

  val launchIntent = Intent(context, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
  }
  val contentPendingIntent = PendingIntent.getActivity(
    context,
    0,
    launchIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
  )

  val notification = NotificationCompat.Builder(context, SIBIL_ALARM_CHANNEL_ID)
    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
    .setContentTitle(title)
    .setContentText(message)
    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setCategory(NotificationCompat.CATEGORY_ALARM)
    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    .setAutoCancel(true)
    .setContentIntent(contentPendingIntent)
    .build()

  val hasNotifPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
  } else {
    true
  }

  if (hasNotifPerm) {
    try {
      NotificationManagerCompat.from(context).notify(TIMER_NOTIFICATION_ID, notification)
    } catch (_: Exception) {
    }
  }
}

/**
 * Triggers continuous looping alarm audio, sets `ringingAlertTitle` so the non-dismissible
 * `RingingAlertDialog` pops up immediately, and posts a system notification.
 */
fun triggerClockAlert(context: Context, title: String, message: String) {
  ringingAlertTitle = title
  activeRingingAlertMessage = message
  AlarmSoundController.startRinging(context)
  showClockNotification(context = context, title = title, message = message)
}

/**
 * Non-dismissible Ringing & Snooze AlertDialog displayed whenever `ringingAlertTitle` is non-null:
 * - Pulsing alarm icon
 * - Displays the Alarm Name or `"Timer Reached 00:00:00!"`
 * - Two prominent buttons:
 *   1. `"Stop"`: Calls `AlarmSoundController.stopRinging()` and dismisses the dialog.
 *   2. `"Snooze (5m)"`: Stops the ringing, dismisses the dialog, schedules a 5-minute snooze alarm,
 *      and displays `"Snoozed for 5 minutes"`.
 */
@Composable
fun RingingAlertDialog(
  alertTitle: String,
  alertDetail: String?,
  metrics: ResponsiveMetrics,
  onStop: () -> Unit,
  onSnoozeFiveMinutes: () -> Unit
) {
  val palette = currentGlassPalette()
  val infiniteTransition = rememberInfiniteTransition(label = "ringing_alarm_pulse")
  val iconScale by infiniteTransition.animateFloat(
    initialValue = 0.88f,
    targetValue = 1.16f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "ringing_icon_scale"
  )

  AlertDialog(
    onDismissRequest = { /* Non-dismissible until user taps Stop or Snooze (5m) */ },
    properties = DialogProperties(
      dismissOnBackPress = false,
      dismissOnClickOutside = false
    ),
    shape = RoundedCornerShape(28.dp),
    containerColor = palette.dialogSurface,
    icon = {
      Surface(
        shape = CircleShape,
        color = palette.accentIndigo.copy(alpha = 0.16f),
        border = BorderStroke(2.dp, palette.accentIndigo),
        modifier = Modifier
          .size(72.dp)
          .graphicsLayer {
            scaleX = iconScale
            scaleY = iconScale
          }
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Filled.NotificationsActive,
            contentDescription = "Ringing Alarm Icon",
            tint = palette.accentIndigo,
            modifier = Modifier.size(36.dp)
          )
        }
      }
    },
    title = {
      Text(
        text = alertTitle,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontSize = metrics.scaledSp(21),
          fontWeight = FontWeight.ExtraBold
        ),
        color = palette.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ringing_alert_dialog_title")
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = alertDetail ?: "Your scheduled Sibil alert is ringing.",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = metrics.scaledSp(14),
            fontWeight = FontWeight.SemiBold
          ),
          color = palette.textSecondary,
          textAlign = TextAlign.Center
        )
        Text(
          text = "Audio loops continuously until stopped or snoozed.",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = metrics.scaledSp(11)
          ),
          color = palette.accentIndigo,
          textAlign = TextAlign.Center
        )
      }
    },
    confirmButton = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onSnoozeFiveMinutes,
          shape = RoundedCornerShape(50),
          border = BorderStroke(1.5.dp, palette.accentIndigo),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.nestedCardFill,
            contentColor = palette.accentIndigo
          ),
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .testTag("ringing_dialog_snooze_button")
        ) {
          Icon(
            imageVector = Icons.Filled.Snooze,
            contentDescription = "Snooze 5 minutes",
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Snooze (5m)",
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        }

        Button(
          onClick = onStop,
          shape = RoundedCornerShape(50),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .testTag("ringing_dialog_stop_button")
        ) {
          Icon(
            imageVector = Icons.Filled.VolumeOff,
            contentDescription = "Stop Ringing",
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Stop",
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        }
      }
    },
    modifier = Modifier.testTag("ringing_alert_dialog")
  )
}

/**
 * Schedules an exact background alarm pointing to `TimerReceiver` for the calculated end time in milliseconds.
 */
@SuppressLint("ScheduleExactAlarm")
fun scheduleTimerExactAlarm(context: Context, durationSeconds: Long, label: String? = null) {
  if (durationSeconds <= 0L) return
  createClockNotificationChannel(context)
  val endTimeMillis = System.currentTimeMillis() + (durationSeconds * 1000L)
  val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
  val intent = Intent(context, TimerReceiver::class.java).apply {
    putExtra("timer_label", label ?: "Timer")
  }
  val pendingIntent = PendingIntent.getBroadcast(
    context,
    TIMER_ALARM_REQUEST_CODE,
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
  )
  try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (alarmManager.canScheduleExactAlarms()) {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          endTimeMillis,
          pendingIntent
        )
      } else {
        alarmManager.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          endTimeMillis,
          pendingIntent
        )
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        endTimeMillis,
        pendingIntent
      )
    } else {
      alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        endTimeMillis,
        pendingIntent
      )
    }
  } catch (_: SecurityException) {
    try {
      alarmManager.set(AlarmManager.RTC_WAKEUP, endTimeMillis, pendingIntent)
    } catch (_: Exception) {
    }
  }
}

/**
 * Cancels any pending background `TimerReceiver` alarm when the timer is paused, reset, or cleared.
 */
fun cancelTimerExactAlarm(context: Context) {
  val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
  val intent = Intent(context, TimerReceiver::class.java)
  val pendingIntent = PendingIntent.getBroadcast(
    context,
    TIMER_ALARM_REQUEST_CODE,
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
  )
  try {
    alarmManager.cancel(pendingIntent)
  } catch (_: Exception) {
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
  val palette = currentGlassPalette()
  val creamCardColor = palette.cardFill
  val ivoryInnerColor = palette.nestedCardFill

  var selectedSubTab by rememberSaveable { mutableStateOf(ClockSubTab.ALARM) }

  // Notification Permission state (Android 13+) & Exact Alarm Permission state (Android 12+)
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
  var canScheduleExactAlarms by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        am?.canScheduleExactAlarms() ?: true
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

  val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
      canScheduleExactAlarms = am?.canScheduleExactAlarms() ?: true
    }
  }

  LaunchedEffect(Unit) {
    createClockNotificationChannel(context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  // Local and global ringing state so user can stop or snooze ringing alarm or timer
  var ringingBannerMessage by remember { mutableStateOf<String?>(null) }

  fun triggerAlarmRingtone(title: String, message: String) {
    ringingBannerMessage = message
    triggerClockAlert(context = context, title = title, message = message)
  }

  fun stopActiveRingtone() {
    AlarmSoundController.stopRinging()
    ringingBannerMessage = null
    try {
      NotificationManagerCompat.from(context).cancel(TIMER_NOTIFICATION_ID)
    } catch (_: Exception) {
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

  // Check current device time every second using Calendar.getInstance()
  LaunchedEffect(alarms.toList()) {
    while (true) {
      val cal = Calendar.getInstance()
      val currentHour24 = cal.get(Calendar.HOUR_OF_DAY)
      val currentMinute = cal.get(Calendar.MINUTE)
      val currentAmPm = if (currentHour24 < 12) "AM" else "PM"
      val currentHour12 = when {
        currentHour24 == 0 -> 12
        currentHour24 > 12 -> currentHour24 - 12
        else -> currentHour24
      }
      val currentDayIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)

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
          triggerAlarmRingtone(
            title = matchingAlarm.name.ifBlank { "Alarm" },
            message = "Alarm ringing: $formattedTime (${matchingAlarm.name.ifBlank { "Alarm" }})"
          )
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
          triggerAlarmRingtone(
            title = "Timer Reached 00:00:00!",
            message = "Timer finished: 00:00:00$labelSuffix"
          )
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
      .background(Color.Transparent)
      .testTag("clock_screen")
  ) {
    // Floating Liquid Glass Header ("Clock" & "CE Time Suite")
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f, fill = false)
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              brush = Brush.linearGradient(
                colors = listOf(
                  Color(0xFF6366F1).copy(alpha = 0.2f),
                  Color(0xFF4F46E5).copy(alpha = 0.1f)
                )
              ),
              shape = RoundedCornerShape(12.dp)
            )
            .border(
              border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                  colors = listOf(
                    Color.White.copy(alpha = 0.55f),
                    Color(0xFF6366F1).copy(alpha = 0.35f)
                  )
                )
              ),
              shape = RoundedCornerShape(12.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = "Clock header icon",
            tint = Color(0xFF6366F1),
            modifier = Modifier.size(20.dp)
          )
        }

        Column {
          Text(
            text = "Clock",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = palette.textPrimary
          )
          Text(
            text = "Board Exam & Study Timekeeper",
            fontSize = 11.sp,
            color = palette.textSecondary
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(50),
        color = palette.nestedCardFill,
        border = BorderStroke(
          width = 1.dp,
          brush = Brush.horizontalGradient(
            colors = listOf(
              Color(0xFF6366F1).copy(alpha = 0.45f),
              Color.White.copy(alpha = 0.6f)
            )
          )
        ),
        shadowElevation = 4.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Timer,
            contentDescription = null,
            tint = Color(0xFF6366F1),
            modifier = Modifier.size(12.dp)
          )
          Text(
            text = "CE Time Suite",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (palette.isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
          )
        }
      }
    }

    // 3D Segmented Liquid Glass Pill Switcher (Alarm, Timer, Stopwatch)
    val trackShape = RoundedCornerShape(26.dp)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp)
        .height(52.dp)
        .shadow(
          elevation = 8.dp,
          shape = trackShape,
          ambientColor = palette.shadowAmbient,
          spotColor = palette.shadowSpot
        )
        .clip(trackShape)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(palette.cardTopSheen, palette.cardBottomSheen)
          ),
          shape = trackShape
        )
        .border(border = palette.glassBorder, shape = trackShape)
        .padding(4.dp)
        .testTag("clock_top_tab_row")
    ) {
      Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        ClockSubTab.entries.forEach { tab ->
          val selected = selectedSubTab == tab
          val pillShape = RoundedCornerShape(22.dp)
          val pillModifier = if (selected) {
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .shadow(
                elevation = 6.dp,
                shape = pillShape,
                spotColor = Color(0x554F46E5)
              )
              .clip(pillShape)
              .background(
                brush = Brush.linearGradient(
                  colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                ),
                shape = pillShape
              )
              .border(
                border = BorderStroke(
                  width = 1.dp,
                  brush = Brush.verticalGradient(
                    colors = listOf(
                      Color.White.copy(alpha = 0.65f),
                      Color.Transparent
                    )
                  )
                ),
                shape = pillShape
              )
          } else {
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .clip(pillShape)
              .background(Color.Transparent, shape = pillShape)
          }

          Box(
            modifier = pillModifier
              .clickable {
                if (selectedSubTab != tab) {
                  playCardHapticSound(context)
                  selectedSubTab = tab
                } else {
                  playCardHapticSound(context)
                }
              }
              .testTag(tab.testTag),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(horizontal = 8.dp)
            ) {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                modifier = Modifier.size(16.dp),
                tint = if (selected) Color.White else palette.textSecondary
              )
              Text(
                text = tab.title,
                color = if (selected) Color.White else palette.textSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }

    // Dismissible POST_NOTIFICATIONS / SCHEDULE_EXACT_ALARM Permission Rationale Card (only on Alarm tab)
    if (selectedSubTab == ClockSubTab.ALARM && (!hasNotificationPermission || !canScheduleExactAlarms) && !isPermissionRationaleDismissed) {
      LiquidGlassCard(
        shape = RoundedCornerShape(18.dp),
        elevation = if (palette.isDark) 6.dp else 4.dp,
        borderStroke = BorderStroke(1.2.dp, palette.accentIndigo.copy(alpha = 0.45f)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = metrics.scaledDp(16),
            vertical = metrics.scaledDp(8)
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
            color = palette.nestedCardFill,
            modifier = Modifier.size(40.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = "Notification permission required",
                tint = palette.accentIndigo,
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
              color = palette.textPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                    color = palette.accentIndigo
                  )
                }
              }
              if (!canScheduleExactAlarms && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                TextButton(
                  onClick = {
                    try {
                      val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                      }
                      exactAlarmSettingsLauncher.launch(intent)
                    } catch (_: Exception) {
                    }
                  },
                  contentPadding = PaddingValues(0.dp),
                  modifier = Modifier.testTag("grant_exact_alarm_permission_button")
                ) {
                  Text(
                    text = "Allow Exact Alarms",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.accentIndigo
                  )
                }
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
              tint = palette.textSecondary
            )
          }
        }
      }
    }

    // Active ringing alarm/timer banner with visible "Stop Alarm" button
    val resolvedRingingMessage = ringingBannerMessage
      ?: activeRingingAlertMessage
      ?: ringingAlertTitle
      ?: if (isAlarmSoundRinging) "Alarm / Timer Ringing!" else null

    if (isAlarmSoundRinging || resolvedRingingMessage != null) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = palette.nestedCardFill
        ),
        border = BorderStroke(1.5.dp, palette.accentIndigo),
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
              tint = palette.accentIndigo
            )
            Text(
              text = resolvedRingingMessage ?: "Alarm Ringing!",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = palette.textPrimary
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
              contentDescription = "Stop Alarm",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Stop Alarm")
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
            triggerAlarmRingtone(
              title = alarm.name.ifBlank { "Alarm" },
              message = "Alarm ringing: $formattedTime (${alarm.name.ifBlank { "Alarm" }})"
            )
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
            cancelTimerExactAlarm(context)
          },
          onStartOrPause = {
            if (isTimerRunning) {
              isTimerRunning = false
              cancelTimerExactAlarm(context)
            } else {
              stopActiveRingtone()
              if (timerRemainingSeconds > 0L) {
                isTimerRunning = true
                scheduleTimerExactAlarm(
                  context = context,
                  durationSeconds = timerRemainingSeconds,
                  label = activePresetLabel
                )
              } else {
                val parsedSeconds = parseKeypadDigitsToSeconds(timerDigitInput)
                if (parsedSeconds > 0L) {
                  timerTotalDurationSeconds = parsedSeconds
                  timerRemainingSeconds = parsedSeconds
                  activePresetLabel = "Custom Timer"
                  isTimerRunning = true
                  scheduleTimerExactAlarm(
                    context = context,
                    durationSeconds = parsedSeconds,
                    label = activePresetLabel
                  )
                }
              }
            }
          },
          onResetTimer = {
            isTimerRunning = false
            timerRemainingSeconds = 0L
            timerTotalDurationSeconds = 0L
            activePresetLabel = null
            cancelTimerExactAlarm(context)
          },
          onSelectPreset = { preset ->
            stopActiveRingtone()
            timerDigitInput = ""
            timerTotalDurationSeconds = preset.durationSeconds
            timerRemainingSeconds = preset.durationSeconds
            activePresetLabel = preset.label
            isTimerRunning = true
            scheduleTimerExactAlarm(
              context = context,
              durationSeconds = preset.durationSeconds,
              label = preset.label
            )
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
              val previousTotal = stopwatchLaps.lastOrNull()?.totalElapsedMillis ?: 0L
              val split = (stopwatchElapsedMillis - previousTotal).coerceAtLeast(0L)
              stopwatchLaps.add(
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

  // If ClockScreen is rendered standalone without MainProductivityScaffold, still show RingingAlertDialog
  val standaloneRingingTitle = ringingAlertTitle
  if (viewModel == null && standaloneRingingTitle != null) {
    RingingAlertDialog(
      alertTitle = standaloneRingingTitle,
      alertDetail = activeRingingAlertMessage,
      metrics = metrics,
      onStop = {
        stopActiveRingtone()
      },
      onSnoozeFiveMinutes = {
        stopActiveRingtone()
        val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val amPm = if (hour24 < 12) "AM" else "PM"
        val hour12 = when {
          hour24 == 0 -> 12
          hour24 > 12 -> hour24 - 12
          else -> hour24
        }
        val nextId = (fallbackAlarms.maxOfOrNull { it.id } ?: 0) + 1
        fallbackAlarms.add(
          AlarmItem(
            id = nextId,
            name = "$standaloneRingingTitle (Snooze)",
            hour = String.format(Locale.US, "%02d", hour12),
            minute = String.format(Locale.US, "%02d", minute),
            amPm = amPm,
            activeDays = emptyList(),
            isActive = true
          )
        )
        localStorageManager.saveAlarms(fallbackAlarms, LocalStorageManager.GUEST_ACCOUNT_KEY)
        scheduleTimerExactAlarm(
          context = context,
          durationSeconds = 5L * 60L,
          label = "$standaloneRingingTitle (Snooze)"
        )
        Toast.makeText(context, "Snoozed for 5 minutes", Toast.LENGTH_SHORT).show()
      }
    )
  }

  if (isAddAlarmDialogOpen) {
    AddAlarmDialog(
      creamCardColor = palette.dialogSurface,
      ivoryInnerColor = palette.inputFill,
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
  val palette = currentGlassPalette()
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
            tint = palette.accentIndigo.copy(alpha = 0.65f),
            modifier = Modifier.size(metrics.scaledDp(48))
          )
          Text(
            text = "No Alarms Yet",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = metrics.scaledSp(18)
            ),
            color = palette.textPrimary
          )
          Text(
            text = "Tap the + button below to create an alarm.",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = metrics.scaledSp(13)
            ),
            color = palette.textSecondary,
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
        LiquidGlassCard(
          shape = RoundedCornerShape(26.dp),
          elevation = 8.dp,
          fillAlpha = if (palette.isDark) 0.78f else 0.90f,
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
                color = palette.accentIndigo,
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
                  tint = palette.accentIndigo
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Test Ring",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = palette.accentIndigo
                )
              }
            }

            // Middle Row: Sleek, high-contrast digital time readout and Switch toggle pushed to the far right
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
                    palette.textPrimary
                  } else {
                    palette.textMuted
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
                    palette.accentIndigo
                  } else {
                    palette.textMuted
                  },
                  modifier = Modifier.alignByBaseline()
                )
              }

              Switch(
                checked = alarm.isActive,
                onCheckedChange = { checked -> onToggleAlarm(alarm.id, checked) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = LiquidDeepIndigo,
                  uncheckedThumbColor = palette.textSecondary,
                  uncheckedTrackColor = palette.progressTrack
                ),
                modifier = Modifier.testTag("alarm_switch_${alarm.id}")
              )
            }

            // Bottom Row: 7 miniature liquid bubbles for the days of the week (S, M, T, W, T, F, S)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              ALARM_WEEK_DAY_LABELS.forEachIndexed { dayIndex, dayLetter ->
                val isDayActive = alarm.activeDays.contains(dayIndex)
                Box(
                  modifier = Modifier
                    .size(metrics.scaledDp(34))
                    .shadow(
                      elevation = if (isDayActive) 6.dp else 1.dp,
                      shape = CircleShape,
                      ambientColor = if (isDayActive) LiquidDeepIndigo.copy(alpha = 0.35f) else Color.Transparent,
                      spotColor = if (isDayActive) LiquidElectricViolet.copy(alpha = 0.35f) else Color.Transparent
                    )
                    .clip(CircleShape)
                    .background(
                      brush = if (isDayActive) {
                        LiquidVioletChipGradient
                      } else {
                        Brush.linearGradient(
                          listOf(
                            palette.chipUnselectedFill,
                            palette.nestedCardFill
                          )
                        )
                      }
                    )
                    .border(
                      width = 1.dp,
                      color = if (isDayActive) {
                        Color.White.copy(alpha = 0.72f)
                      } else {
                        palette.chipUnselectedBorder
                      },
                      shape = CircleShape
                    )
                    .clickable { onToggleAlarmDay(alarm.id, dayIndex) }
                    .testTag("alarm_${alarm.id}_day_$dayIndex"),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = dayLetter,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = metrics.scaledSp(12)
                    ),
                    color = if (isDayActive) {
                      Color.White
                    } else {
                      palette.accentIndigo
                    }
                  )
                }
              }
            }
          }
        }
      }
    }

    // Large circular Liquid Orb FAB (+) at the bottom center
    FloatingActionButton(
      onClick = onOpenAddAlarmDialog,
      shape = CircleShape,
      containerColor = LiquidDeepIndigo,
      contentColor = Color.White,
      elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 10.dp),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = metrics.scaledDp(20))
        .size(metrics.scaledDp(68))
        .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.65f)), CircleShape)
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
  val palette = currentGlassPalette()
  var alarmName by rememberSaveable { mutableStateOf("") }
  var hourInput by rememberSaveable { mutableStateOf("07") }
  var minuteInput by rememberSaveable { mutableStateOf("00") }
  var selectedAmPm by rememberSaveable { mutableStateOf("AM") }
  val selectedDays = remember { mutableStateListOf(1, 2, 3, 4, 5) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = palette.dialogSurface,
    title = {
      Text(
        text = "Add Alarm",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          fontSize = metrics.scaledSp(20)
        ),
        color = palette.textPrimary
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
              textAlign = TextAlign.Center,
              color = palette.textPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            colors = sibilOutlinedTextFieldColors(),
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
            color = palette.textPrimary
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
              textAlign = TextAlign.Center,
              color = palette.textPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            colors = sibilOutlinedTextFieldColors(),
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
                  palette.chipUnselectedFill
                },
                border = BorderStroke(
                  width = 1.dp,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    palette.chipUnselectedBorder
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
                      palette.accentIndigo
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
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = palette.textPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          colors = sibilOutlinedTextFieldColors(),
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
            color = palette.textSecondary
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
                  palette.chipUnselectedFill
                },
                border = BorderStroke(
                  width = 1.dp,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    palette.chipUnselectedBorder
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
                      palette.accentIndigo
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
          color = palette.textSecondary
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
  val palette = currentGlassPalette()
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
    // Massive Digital Timer Display Card with Luminous Gradient Circular Ring
    LiquidGlassCard(
      shape = RoundedCornerShape(28.dp),
      elevation = 10.dp,
      fillAlpha = if (palette.isDark) 0.78f else 0.90f,
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
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
      ) {
        if (activePresetLabel != null) {
          Surface(
            shape = RoundedCornerShape(50),
            color = palette.accentIndigo.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, palette.accentIndigo.copy(alpha = 0.35f))
          ) {
            Text(
              text = activePresetLabel,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = metrics.scaledSp(12)
              ),
              color = palette.accentIndigo,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
          }
        }

        if (isCountdownActive) {
          val progress = if (totalDurationSeconds > 0L) {
            (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
          } else {
            0f
          }
          val animatedTimerArc by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "timer_luminous_ring"
          )

          Box(
            modifier = Modifier
              .size(metrics.scaledDp(208))
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val strokePx = 11.dp.toPx()
              drawArc(
                color = palette.progressTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
              )
              if (animatedTimerArc > 0f) {
                drawArc(
                  brush = LiquidCyanVioletGradient,
                  startAngle = -90f,
                  sweepAngle = 360f * animatedTimerArc,
                  useCenter = false,
                  style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
              }
            }

            Text(
              text = formatDurationSecondsHms(remainingSeconds),
              style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = metrics.scaledSp(34),
                lineHeight = metrics.scaledSp(38)
              ),
              color = palette.textPrimary,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("timer_countdown_display")
            )
          }

          LinearProgressIndicator(
            progress = { progress },
            color = palette.accentIndigo,
            trackColor = palette.progressTrack,
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
                tint = palette.textSecondary
              )
            }
          }
        }
      }
    }

    // CE Focus Presets Section
    LiquidGlassCard(
      shape = RoundedCornerShape(24.dp),
      elevation = 6.dp,
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
          color = palette.textPrimary
        )

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(8)),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(CE_FOCUS_PRESETS, key = { it.label }) { preset ->
            val isSelected = activePresetLabel == preset.label && isCountdownActive
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                  brush = if (isSelected) {
                    LiquidVioletChipGradient
                  } else {
                    Brush.linearGradient(
                      listOf(
                        palette.chipUnselectedFill,
                        palette.nestedCardFill
                      )
                    )
                  }
                )
                .border(
                  width = 1.dp,
                  color = if (isSelected) {
                    Color.White.copy(alpha = 0.7f)
                  } else {
                    palette.chipUnselectedBorder
                  },
                  shape = RoundedCornerShape(50)
                )
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
                    Color.White
                  } else {
                    palette.accentIndigo
                  }
                )
                Text(
                  text = preset.label,
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = metrics.scaledSp(13)
                  ),
                  color = if (isSelected) {
                    Color.White
                  } else {
                    palette.textPrimary
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
                color = palette.cardFill,
                border = BorderStroke(1.1.dp, palette.cardBorder),
                shadowElevation = 4.dp,
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
                      palette.accentRed
                    } else {
                      palette.textPrimary
                    }
                  )
                }
              }
            }
          }
        }
      }
    }

    // Liquid Floating Play/Pause and Reset Controls
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
          color = palette.cardFill,
          border = BorderStroke(1.2.dp, palette.cardBorder),
          shadowElevation = 6.dp,
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
              tint = palette.accentIndigo,
              modifier = Modifier.size(26.dp)
            )
          }
        }
      }

      FloatingActionButton(
        onClick = onStartOrPause,
        shape = CircleShape,
        containerColor = LiquidDeepIndigo,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 10.dp),
        modifier = Modifier
          .size(metrics.scaledDp(72))
          .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.65f)), CircleShape)
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
  val palette = currentGlassPalette()
  Row(verticalAlignment = Alignment.Bottom) {
    Text(
      text = value,
      style = MaterialTheme.typography.displayMedium.copy(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = metrics.scaledSp(40)
      ),
      color = if (isHighlighted) {
        palette.accentIndigo
      } else {
        palette.textMuted
      }
    )
    Text(
      text = unit,
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = metrics.scaledSp(18)
      ),
      color = if (isHighlighted) {
        palette.accentIndigo
      } else {
        palette.textMuted
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
  val palette = currentGlassPalette()
  val minuteFraction = remember(elapsedMillis) {
    ((elapsedMillis % 60_000L).toFloat() / 60_000f).coerceIn(0f, 1f)
  }
  val highlightIndigo = if (palette.isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
  val reversedLaps = laps.asReversed()
  val fastestLapNumber = remember(laps.toList()) {
    if (laps.size >= 2) {
      laps.minByOrNull { it.lapSplitMillis }?.lapNumber
    } else {
      null
    }
  }
  val latestLapNumber = laps.lastOrNull()?.lapNumber

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(
        start = metrics.scaledDp(16),
        end = metrics.scaledDp(16),
        top = metrics.scaledDp(12),
        bottom = 120.dp
      )
      .testTag("stopwatch_sub_screen"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Luminous Circular Progress Ring + Frosted Glass Stopwatch Header
    LiquidGlassCard(
      shape = RoundedCornerShape(30.dp),
      elevation = if (palette.isDark) 8.dp else 6.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = metrics.scaledDp(20),
            vertical = 18.dp
          ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(8))
      ) {
        Box(
          modifier = Modifier
            .size(190.dp)
            .padding(4.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 10.dp.toPx()
            drawArc(
              color = palette.progressTrack,
              startAngle = 0f,
              sweepAngle = 360f,
              useCenter = false,
              style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (minuteFraction > 0f || isRunning) {
              drawArc(
                brush = LiquidCyanVioletGradient,
                startAngle = -90f,
                sweepAngle = (360f * minuteFraction).coerceAtLeast(if (isRunning) 8f else 0f),
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
              )
            }
          }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = if (isRunning) "RUNNING" else if (elapsedMillis > 0L) "PAUSED" else "READY",
              style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
              ),
              color = if (isRunning) {
                palette.accentIndigo
              } else {
                palette.textSecondary
              }
            )

            Text(
              text = formatStopwatchMillis(elapsedMillis),
              style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = metrics.scaledSp(34),
                lineHeight = metrics.scaledSp(38)
              ),
              color = palette.textPrimary,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("stopwatch_digital_counter")
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Two liquid floating control buttons: Lap/Reset and Start/Stop
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(24), Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val canLapOrReset = isRunning || elapsedMillis > 0L
      Surface(
        shape = CircleShape,
        color = if (canLapOrReset) palette.cardFill else palette.nestedCardFill,
        border = BorderStroke(1.2.dp, palette.cardBorder),
        shadowElevation = if (canLapOrReset) 4.dp else 0.dp,
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
              palette.accentIndigo
            } else {
              palette.textMuted
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
              palette.accentIndigo
            } else {
              palette.textMuted
            }
          )
        }
      }

      // Primary circular Liquid Orb FAB: Start / Stop
      FloatingActionButton(
        onClick = onToggleStartStop,
        shape = CircleShape,
        containerColor = LiquidDeepIndigo,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 10.dp),
        modifier = Modifier
          .size(metrics.scaledDp(74))
          .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.65f)), CircleShape)
          .testTag("stopwatch_start_stop_button")
      ) {
        Icon(
          imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
          contentDescription = if (isRunning) "Stop stopwatch" else "Start stopwatch",
          modifier = Modifier.size(metrics.scaledDp(36))
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Naturally expanding Lap Splits Card ordered newest-first
    LiquidGlassCard(
      shape = RoundedCornerShape(24.dp),
      elevation = if (palette.isDark) 8.dp else 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 180.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(metrics.scaledDp(16))
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
              fontSize = metrics.scaledSp(15)
            ),
            color = palette.textPrimary
          )
          Text(
            text = "${laps.size} recorded",
            style = MaterialTheme.typography.labelMedium.copy(
              fontFamily = JetBrainsMonoFamily,
              fontWeight = FontWeight.SemiBold
            ),
            color = palette.textSecondary
          )
        }

        // Micro-header row: LAP | SPLIT | TOTAL
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "LAP",
            fontSize = 11.sp,
            color = palette.textSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
          )
          Text(
            text = "SPLIT",
            fontSize = 11.sp,
            color = palette.textSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
          )
          Text(
            text = "TOTAL",
            fontSize = 11.sp,
            color = palette.textSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
          )
        }

        HorizontalDivider(
          thickness = 1.dp,
          color = palette.dividerColor
        )

        if (reversedLaps.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Start the stopwatch and tap Lap to record split intervals.",
              style = MaterialTheme.typography.bodyMedium,
              color = palette.textSecondary,
              textAlign = TextAlign.Center
            )
          }
        } else {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp)
              .testTag("stopwatch_laps_list")
          ) {
            reversedLaps.forEachIndexed { index, lap ->
              val isHighlightedLap =
                lap.lapNumber == latestLapNumber || lap.lapNumber == fastestLapNumber
              val isFastest = lap.lapNumber == fastestLapNumber
              val isLatest = lap.lapNumber == latestLapNumber

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 10.dp)
                  .testTag("stopwatch_lap_row_${lap.lapNumber}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Left column: LAP badge + optional Latest/Fastest pill
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(
                        color = if (isHighlightedLap) {
                          palette.nestedCardFill
                        } else {
                          Color.Transparent
                        }
                      )
                      .then(
                        if (isHighlightedLap) {
                          Modifier.border(
                            BorderStroke(1.dp, highlightIndigo.copy(alpha = 0.5f)),
                            RoundedCornerShape(8.dp)
                          )
                        } else {
                          Modifier
                        }
                      )
                      .padding(
                        horizontal = if (isHighlightedLap) 8.dp else 2.dp,
                        vertical = 3.dp
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = String.format(Locale.US, "#%02d", lap.lapNumber),
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      ),
                      color = if (isHighlightedLap) highlightIndigo else palette.textSecondary
                    )
                  }

                  if (isFastest || isLatest) {
                    Text(
                      text = if (isFastest) "FASTEST" else "LATEST",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                      ),
                      color = highlightIndigo,
                      maxLines = 1
                    )
                  }
                }

                // Center column: SPLIT
                Text(
                  text = "+${formatStopwatchMillis(lap.lapSplitMillis)}",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = if (isHighlightedLap) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                  ),
                  color = if (isHighlightedLap) highlightIndigo else palette.textSecondary,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.weight(1f)
                )

                // Right column: TOTAL
                Text(
                  text = formatStopwatchMillis(lap.totalElapsedMillis),
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                  ),
                  color = palette.textPrimary,
                  textAlign = TextAlign.End,
                  modifier = Modifier.weight(1f)
                )
              }

              if (index < reversedLaps.lastIndex) {
                HorizontalDivider(
                  thickness = 0.8.dp,
                  color = palette.dividerColor.copy(alpha = 0.65f)
                )
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

// Registered in AndroidManifest.xml: <receiver android:name=".TimerReceiver" />
class TimerReceiver : BroadcastReceiver() {
  @SuppressLint("MissingPermission")
  override fun onReceive(context: Context, intent: Intent?) {
    val label = intent?.getStringExtra("timer_label")
    val alertTitle = if (!label.isNullOrBlank()) {
      "Timer Reached 00:00:00! ($label)"
    } else {
      "Timer Reached 00:00:00!"
    }
    val alertMessage = if (!label.isNullOrBlank()) {
      "Your timer ($label) has reached 00:00:00."
    } else {
      "Your countdown timer has reached 00:00:00."
    }
    triggerClockAlert(
      context = context,
      title = alertTitle,
      message = alertMessage
    )
  }
}


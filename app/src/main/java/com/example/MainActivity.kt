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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
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
import androidx.core.content.FileProvider
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
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.SetOptions
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
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

// KEYS ARE INJECTED DURING CI/CD BUILD FROM GITHUB SECRETS.
// DO NOT HARDCODE RAW "AIzaSy..." STRINGS HERE IN THE PUBLIC SOURCE.
private val COMPILED_DEVELOPER_KEY_POOL = listOf(
    "__KEY_INJECTION_PLACEHOLDER__"
)

private val DEFAULT_GEMINI_API_KEY: String
  get() = COMPILED_DEVELOPER_KEY_POOL
    .map { it.trim() }
    .firstOrNull { !it.startsWith("__KEY_") && it.isNotBlank() }
    .orEmpty()

const val GITHUB_REPO_OWNER = "Dodolzzzkie" // Your GitHub username
const val GITHUB_REPO_NAME = "Sibil-app"    // Your repository name
const val APP_VERSION_CODE = 102            // Incremented with builds
const val APP_VERSION_NAME = "v1.0.2"
const val SIBIL_APP_VERSION_TAG = "Sibil $APP_VERSION_NAME (Official Release)"

private const val DEFAULT_FALLBACK_LATITUDE = 10.52
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

  val cardSurface: Color
    get() = cardFill

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

    // Initialize Firebase manually to prevent FirebaseAuth crashes
    if (FirebaseApp.getApps(this).isEmpty()) {
      FirebaseApp.initializeApp(this)
    }
    ensureFirebaseInitialized(this)

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
  val maxContentWidth: Dp,
  val screenHeightDp: Int = 800,
  val isLandscape: Boolean = false
) {
  val isTablet: Boolean
    get() = screenWidthDp >= 600
  val isTabletLandscape: Boolean
    get() = screenWidthDp >= 720 && isLandscape

  fun scaledDp(baseDp: Int): Dp = (baseDp * scaleMultiplier).dp
  fun scaledSp(baseSp: Int): TextUnit = (baseSp * scaleMultiplier).sp
}

@Composable
private fun rememberResponsiveMetrics(): ResponsiveMetrics {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp
  val screenHeight = configuration.screenHeightDp
  val isLandscape = screenWidth > screenHeight
  val scaleMultiplier = when {
    screenWidth >= 600 -> 1.12f
    screenWidth < 360 -> 0.92f
    else -> 1.0f
  }
  val maxContentWidth = 680.dp
  return remember(screenWidth, screenHeight) {
    ResponsiveMetrics(
      screenWidthDp = screenWidth,
      scaleMultiplier = scaleMultiplier,
      maxContentWidth = maxContentWidth,
      screenHeightDp = screenHeight,
      isLandscape = isLandscape
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
  val id: Int = 0,
  val title: String = "",
  val dueDate: String = "",
  @get:PropertyName("isCompleted")
  val isCompleted: Boolean = false,
  val subjectCategory: String = DEFAULT_TASK_CATEGORY
) {
  fun toFirestoreMap(): HashMap<String, Any?> = hashMapOf(
    "id" to id,
    "title" to title,
    "dueDate" to dueDate,
    "isCompleted" to isCompleted,
    "completed" to isCompleted,
    "subjectCategory" to subjectCategory
  )
}

typealias TaskItem = Task

fun DocumentSnapshot.toTaskItemOrNull(): TaskItem? {
  val parsed = try {
    toObject(TaskItem::class.java)
  } catch (_: Throwable) {
    null
  } ?: return null
  val resolvedId = getLong("id")?.toInt() ?: id.toIntOrNull() ?: parsed.id
  val resolvedCompleted = getBoolean("isCompleted") ?: getBoolean("completed") ?: parsed.isCompleted
  val resolvedTitle = (getString("title") ?: parsed.title).trim()
  if (resolvedTitle.isEmpty()) return null
  val resolvedDueDate = (getString("dueDate") ?: parsed.dueDate).trim().ifEmpty { "Upcoming" }
  val resolvedCategory = (getString("subjectCategory") ?: parsed.subjectCategory).trim().ifEmpty {
    DEFAULT_TASK_CATEGORY
  }
  return parsed.copy(
    id = resolvedId,
    title = resolvedTitle,
    dueDate = resolvedDueDate,
    isCompleted = resolvedCompleted,
    subjectCategory = resolvedCategory
  )
}

/**
 * Data Model for the colored Timetable Schedule on the "Schedule" tab:
 * `id: Int, subjectName: String, timeRange: String, days: List<String>, room: String, colorHex: Long`.
 */
data class ClassSchedule(
  val id: Int = 0,
  val subjectName: String = "",
  val timeRange: String = "7:00 AM - 9:00 AM",
  val days: List<String> = emptyList(),
  val room: String = "",
  val colorHex: Long = 0xFF6366F1,
  val startTime: String = extractStartEndTimeTokens(timeRange).first,
  val endTime: String = extractStartEndTimeTokens(timeRange).second
) {
  fun toFirestoreMap(): HashMap<String, Any?> = hashMapOf(
    "id" to id,
    "subjectName" to subjectName,
    "timeRange" to timeRange,
    "days" to days,
    "room" to room,
    "colorHex" to colorHex,
    "startTime" to startTime,
    "endTime" to endTime
  )
}

typealias ScheduleItem = ClassSchedule

fun DocumentSnapshot.toScheduleItemOrNull(): ScheduleItem? {
  val parsed = try {
    toObject(ScheduleItem::class.java)
  } catch (_: Throwable) {
    null
  } ?: return null
  val resolvedId = getLong("id")?.toInt() ?: id.toIntOrNull() ?: parsed.id
  val resolvedSubject = (getString("subjectName") ?: parsed.subjectName).trim()
  if (resolvedSubject.isEmpty()) return null
  val resolvedTimeRange = (getString("timeRange") ?: parsed.timeRange).trim().ifEmpty {
    "7:00 AM - 9:00 AM"
  }
  val (fallbackStart, fallbackEnd) = extractStartEndTimeTokens(resolvedTimeRange)
  val resolvedStart = (getString("startTime") ?: parsed.startTime).trim().ifEmpty { fallbackStart }
  val resolvedEnd = (getString("endTime") ?: parsed.endTime).trim().ifEmpty { fallbackEnd }
  val resolvedRoom = (getString("room") ?: parsed.room).trim().ifEmpty { "ENG. 307" }
  val resolvedColor = getLong("colorHex") ?: parsed.colorHex
  val resolvedDays = parsed.days.ifEmpty { listOf("M") }
  return parsed.copy(
    id = resolvedId,
    subjectName = resolvedSubject,
    timeRange = resolvedTimeRange,
    days = resolvedDays,
    room = resolvedRoom,
    colorHex = resolvedColor,
    startTime = resolvedStart,
    endTime = resolvedEnd
  )
}

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
  val id: Int = 0,
  val question: String = "",
  val answer: String = "",
  val category: String = "",
  var masteryLevel: String = "New",
  @get:PropertyName("isMastered")
  var isMastered: Boolean = masteryLevel.equals("Good", ignoreCase = true)
) {
  fun toFirestoreMap(): HashMap<String, Any?> = hashMapOf(
    "id" to id,
    "question" to question,
    "answer" to answer,
    "category" to category,
    "masteryLevel" to masteryLevel,
    "isMastered" to isMastered,
    "mastered" to isMastered
  )
}

data class FlashcardDeck(
  val id: Int = 0,
  val title: String = "",
  val subjectTag: String = "",
  val cards: List<FlashcardItem> = emptyList(),
  val masteredCardIds: List<Int> = cards.filter { it.isMastered || it.masteryLevel.equals("Good", ignoreCase = true) }.map { it.id }
) {
  fun toFirestoreMap(): HashMap<String, Any?> = hashMapOf(
    "id" to id,
    "title" to title,
    "subjectTag" to subjectTag,
    "cards" to cards.map { it.toFirestoreMap() },
    "masteredCardIds" to masteredCardIds
  )
}

fun DocumentSnapshot.toFlashcardDeckOrNull(): FlashcardDeck? {
  val parsed = try {
    toObject(FlashcardDeck::class.java)
  } catch (_: Throwable) {
    null
  } ?: return null
  val resolvedId = getLong("id")?.toInt() ?: id.toIntOrNull() ?: parsed.id
  val resolvedTitle = (getString("title") ?: parsed.title).trim()
  if (resolvedTitle.isEmpty()) return null
  val resolvedTag = (getString("subjectTag") ?: parsed.subjectTag).trim().ifEmpty { "General CE" }
  val normalizedCards = parsed.cards.map { card ->
    val mastered = card.isMastered ||
      card.masteryLevel.equals("Good", ignoreCase = true) ||
      parsed.masteredCardIds.contains(card.id)
    card.copy(
      category = card.category.ifBlank { resolvedTag },
      masteryLevel = card.masteryLevel.ifBlank { if (mastered) "Good" else "New" },
      isMastered = mastered
    )
  }
  return parsed.copy(
    id = resolvedId,
    title = resolvedTitle,
    subjectTag = resolvedTag,
    cards = normalizedCards,
    masteredCardIds = normalizedCards.filter { it.isMastered }.map { it.id }
  )
}

/**
 * Custom exception carrying HTTP status code and detailed Gemini API or network error message.
 */
class GeminiBatchExtractionException(
  val statusCode: Int,
  override val message: String
) : Exception(message)

/**
 * Online Multi-Batch AI Flashcard Generator (`GizmoBatchFlashcardEngine`) based on `kiendoantrung/gemini-flashcards`:
 * - Uses `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$activeApiKey`
 *   with automatic model discovery fallback if an HTTP 404 is encountered.
 * - Enforces Structured Outputs via `responseSchema` inside `generationConfig` (`responseMimeType = "application/json"`,
 *   `temperature = 0.1`, `maxOutputTokens = 8192`) so Gemini is guaranteed to return clean `question` and `answer` objects.
 * - Processes PDFs of ANY length in 2-page visual batches using `android.graphics.pdf.PdfRenderer`,
 *   rendering each page onto a max-1280px high-contrast white `Bitmap`, compressing to 80% JPEG Base64,
 *   and recycling the `Bitmap` immediately.
 * - Processes DOCX (`word/document.xml` via `ZipInputStream`), TXT, and Pasted Notes in 4,000-character chunks
 *   after verifying valid human-readable characters. Never falls back to offline regex or unparsed binary streams.
 */
object GizmoBatchFlashcardEngine {
  val SUPPORTED_DOCUMENT_MIME_TYPES = arrayOf(
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/pdf",
    "text/plain"
  )

  const val OFFLINE_ERROR_MESSAGE =
    "Internet connection required to analyze documents with Gemini AI."

  val primaryModel: String = "gemini-3.5-flash-lite"

  val fallbackModels: List<String> = listOf(
    "gemini-3.5-flash-lite",
    "gemini-3.8-flash",
    "gemini-3.6-flash",
    "gemini-3.5-flash",
    "gemini-flash-latest"
  )

  @Volatile
  private var cachedWorkingModelName: String = primaryModel

  @Volatile
  private var hasVerifiedWorkingModel: Boolean = false

  @Volatile
  private var fallbackStepIndex: Int = 0

  @Volatile
  private var activeClassroomKeyIndex: Int = 0

  /**
   * Builds the valid multi-key pool for classroom load-balancing.
   * Filters out un-injected placeholders (`!it.startsWith("__KEY_") && it.isNotBlank()`)
   * from `COMPILED_DEVELOPER_KEY_POOL` and combines them with any custom user keys saved in SharedPreferences.
   */
  fun buildValidKeyPool(userConfiguredKey: String = ""): List<String> {
    val compiledValidKeys = COMPILED_DEVELOPER_KEY_POOL
      .flatMap { it.split(',', '\n', ';', ' ') }
      .map { it.trim() }
      .filter { !it.startsWith("__KEY_") && it.isNotBlank() }

    val customKeys = userConfiguredKey
      .split(',', '\n', ';', ' ')
      .map { it.trim() }
      .filter { !it.startsWith("__KEY_") && it.isNotBlank() }

    return (customKeys + compiledValidKeys).distinct()
  }

  /**
   * Randomizes the starting key index when a student initiates deck generation
   * so 50+ concurrent students distribute evenly across the classroom key pool.
   */
  fun selectRandomizedKeyIndex(validPool: List<String>): Int {
    if (validPool.isEmpty()) return 0
    val chosen = Random.nextInt(validPool.size)
    activeClassroomKeyIndex = chosen
    return chosen
  }

  /**
   * Encodes a `FlashcardDeck` (title, subjectTag/domain, and all Q&A cards) into a compact
   * Base64/JSON share code that classmates can import with zero API calls.
   */
  fun encodeDeckToShareCode(deck: FlashcardDeck): String {
    val root = JSONObject().apply {
      put("v", 1)
      put("title", deck.title.trim().ifEmpty { "Shared Study Deck" })
      put("domain", deck.subjectTag.trim().ifEmpty { "General" })
      val cardsArray = JSONArray()
      for (card in deck.cards) {
        if (card.question.isNotBlank() && card.answer.isNotBlank()) {
          cardsArray.put(
            JSONObject().apply {
              put("q", card.question.trim())
              put("a", card.answer.trim())
              put("c", card.category.trim().ifEmpty { deck.subjectTag.trim().ifEmpty { "General" } })
            }
          )
        }
      }
      put("cards", cardsArray)
    }
    val jsonBytes = root.toString().toByteArray(Charsets.UTF_8)
    return Base64.encodeToString(jsonBytes, Base64.NO_WRAP)
  }

  /**
   * Decodes a Base64/JSON share code from a classmate back into a `FlashcardDeck`
   * with zero API calls and zero token consumption.
   */
  fun decodeDeckFromShareCode(rawCode: String): FlashcardDeck? {
    val cleanedInput = rawCode.trim()
    if (cleanedInput.isEmpty()) return null

    val jsonString = try {
      if (cleanedInput.startsWith("{")) {
        cleanedInput
      } else {
        val normalizedBase64 = cleanedInput.replace("\\s+".toRegex(), "")
        val decodedBytes = Base64.decode(normalizedBase64, Base64.DEFAULT)
        String(decodedBytes, Charsets.UTF_8).trim()
      }
    } catch (_: Exception) {
      return null
    }

    return try {
      val root = JSONObject(jsonString)
      val title = root.optString("title", "")
        .ifBlank { root.optString("t", "Shared Study Deck") }
        .trim()
      val domain = root.optString("domain", "")
        .ifBlank { root.optString("subjectTag", "") }
        .ifBlank { root.optString("s", "General") }
        .trim()
      val cardsArray = root.optJSONArray("cards") ?: root.optJSONArray("items") ?: return null
      val decodedCards = mutableListOf<FlashcardItem>()
      for (i in 0 until cardsArray.length()) {
        val cardObj = cardsArray.optJSONObject(i) ?: continue
        val q = cardObj.optString("q", "")
          .ifBlank { cardObj.optString("question", "") }
          .trim()
        val a = cardObj.optString("a", "")
          .ifBlank { cardObj.optString("answer", "") }
          .trim()
        val c = cardObj.optString("c", "")
          .ifBlank { cardObj.optString("category", domain) }
          .trim()
          .ifEmpty { domain }
        if (q.isNotEmpty() && a.isNotEmpty()) {
          decodedCards.add(
            FlashcardItem(
              id = i + 1,
              question = q,
              answer = a,
              category = c,
              masteryLevel = "New",
              isMastered = false
            )
          )
        }
      }
      if (decodedCards.isEmpty()) return null
      FlashcardDeck(
        id = 0,
        title = title.ifEmpty { "Shared Study Deck" },
        subjectTag = domain.ifEmpty { "General" },
        cards = decodedCards
      )
    } catch (_: Exception) {
      null
    }
  }

  val SYSTEM_PROMPT_INSTRUCTION = """
You are an expert board examination reviewer and academic question creator specialized in the Philippine collegiate and licensure examination style (e.g., PRC board exams, midterm/final departmental exams).

Convert the document into high-yield active-recall flashcards using these strict patterns:
1. IDENTIFICATION STYLE (PRIMARY FORMAT):
   - Question (Front): Write the exact definition, description, function, or principle as a formal inquiry. Use prompt forms such as:
     * "What term refers to [clear definition/description]?"
     * "Identify the [structural member / concept / law / phenomenon] that [descriptive function]."
     * "[Formal technical definition] is known as what?"
   - Answer (Back): Provide the exact technical term, law, or concept (capitalized, concise, with standard abbreviation/acronym if applicable).
2. CODE SPECIFICATION & STANDARD PROVISION:
   - Question: State the design standard, criterion, or code provision requirement (e.g., NSCP, AASHTO, ASTM, or subject-specific standard).
   - Answer: Provide the exact value, formula, limitation, or requirement.
3. PROBLEM-SOLVING FORMULA RECALL:
   - Question: "State the equation / formula used to calculate [target variable] given [parameters]."
   - Answer: Provide the clean formula with labeled variables.
4. NEGATIVE FILTER:
   - Skip school names, professor names, grading criteria, syllabus outlines, course codes, table of contents, and decorative headers.
5. OUTPUT SCHEMA:
   - Return strictly a valid JSON array: [{"question": "...", "answer": "..."}].
  """.trimIndent()

  private const val PAGES_PER_PDF_BATCH = 2
  private const val MAX_TEXT_CHUNK_CHARS = 4000

  /**
   * Queries `https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey` to discover
   * available Flash models that support `generateContent`, ordered from newest/preferred to oldest.
   */
  private fun discoverAvailableFlashModels(apiKey: String): List<String> {
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

          val textCapableFlash = availableFlashModels.filter {
            !it.contains("tts", ignoreCase = true) &&
              !it.contains("image", ignoreCase = true) &&
              !it.contains("audio", ignoreCase = true)
          }

          val orderedCandidates = mutableListOf<String>()
          for (pref in fallbackModels) {
            textCapableFlash.firstOrNull { it.equals(pref, ignoreCase = true) }?.let {
              if (!orderedCandidates.contains(it)) orderedCandidates.add(it)
            }
          }
          // Add newest models first (v1beta/models lists newer models toward the end)
          for (candidate in textCapableFlash.asReversed()) {
            if (!orderedCandidates.contains(candidate)) {
              orderedCandidates.add(candidate)
            }
          }
          return orderedCandidates
        }
      }
    } catch (_: Exception) {
    } finally {
      connection?.disconnect()
    }
    return fallbackModels
  }

  /**
   * Resilient helper function that starts with `gemini-3.8-flash` and, if an HTTP 404
   * occurs, queries `https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey`
   * to select an active Flash model.
   */
  fun resolveWorkingGeminiModel(apiKey: String, forceRefresh: Boolean = false): String {
    if (!forceRefresh && hasVerifiedWorkingModel && cachedWorkingModelName.isNotBlank()) {
      return cachedWorkingModelName
    }
    if (!forceRefresh) {
      return cachedWorkingModelName.ifBlank { primaryModel }
    }

    val discovered = discoverAvailableFlashModels(apiKey)
    val nextCandidate = discovered.firstOrNull { !it.equals(cachedWorkingModelName, ignoreCase = true) }
      ?: discovered.firstOrNull()
    if (!nextCandidate.isNullOrBlank()) {
      cachedWorkingModelName = nextCandidate
      return cachedWorkingModelName
    }

    fallbackStepIndex = (fallbackStepIndex + 1) % fallbackModels.size
    cachedWorkingModelName = fallbackModels[fallbackStepIndex]
    return cachedWorkingModelName
  }

  /**
   * `.docx` Parser: Extracts text strictly from `word/document.xml` using `ZipInputStream`.
   * Strips XML tags cleanly (`Regex("<[^>]*>")`), decodes common XML entities
   * (`&amp;`, `&lt;`, `&gt;`, `&quot;`, `&apos;`), and verifies that the extracted string
   * contains valid human-readable characters before proceeding.
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
              val cleanedText = stripped
                .lines()
                .map { it.replace(Regex("\\s+"), " ").trim() }
                .filter { it.isNotEmpty() }
                .joinToString("\n")
              return@use if (containsValidHumanReadableText(cleanedText)) cleanedText else ""
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
   * `.txt` Parser: Reads plain UTF-8 text via `BufferedReader` and verifies it contains
   * valid human-readable characters (rejecting binary or compressed streams).
   */
  fun extractTextFromTxt(context: Context, uri: Uri): String {
    return try {
      context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
          val text = reader.readText().trim()
          if (containsValidHumanReadableText(text)) text else ""
        }
      } ?: ""
    } catch (_: Exception) {
      ""
    }
  }

  /**
   * Verifies that extracted text is clean, human-readable content and never raw binary/PDF/ZIP bytes.
   */
  fun containsValidHumanReadableText(text: String): Boolean {
    val trimmed = text.trim()
    if (trimmed.length < 3) return false
    if (trimmed.startsWith("%PDF-") || trimmed.startsWith("PK\u0003\u0004")) return false
    val letterOrDigitCount = trimmed.count { it.isLetterOrDigit() }
    val controlCharCount = trimmed.count { ch ->
      ch.code < 32 && ch != '\n' && ch != '\r' && ch != '\t'
    }
    if (letterOrDigitCount < 3) return false
    val controlRatio = controlCharCount.toFloat() / trimmed.length.toFloat()
    return controlRatio < 0.02f
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
    val validPool = buildValidKeyPool(activeApiKey)
    if (validPool.isEmpty()) {
      throw GeminiBatchExtractionException(
        statusCode = 401,
        message = "No Gemini API key configured. Please tap 'Update API Key' or the Key icon to enter your Google AI Studio key."
      )
    }
    val startIdx = selectRandomizedKeyIndex(validPool)
    val initialKey = validPool[startIdx]
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
        activeApiKey = initialKey,
        defaultCategory = defaultCategory,
        onProgressUpdate = onProgressUpdate
      )
    } else {
      val extractedText = when {
        lowerName.endsWith(".docx") || mimeType.contains("wordprocessingml") -> {
          extractTextFromDocx(context, uri)
        }
        lowerName.endsWith(".txt") || mimeType.startsWith("text/") -> {
          extractTextFromTxt(context, uri)
        }
        else -> {
          val docxText = extractTextFromDocx(context, uri)
          if (docxText.isNotBlank()) docxText else extractTextFromTxt(context, uri)
        }
      }
      if (!containsValidHumanReadableText(extractedText)) {
        throw GeminiBatchExtractionException(
          statusCode = 400,
          message = "No valid human-readable text found in \"$fileName\". Please upload a valid PDF, DOCX, or TXT document."
        )
      }
      processTextInChunks(
        rawText = extractedText,
        activeApiKey = initialKey,
        defaultCategory = defaultCategory,
        onProgressUpdate = onProgressUpdate
      )
    }
  }

  /**
   * Processes a PDF of ANY length using Android's `PdfRenderer` in 2-page JPEG batches
   * rendered onto high-contrast white Bitmaps (scaled to max dimension 1280px, JPEG quality 80%).
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
        statusCode = 400,
        message = "Unable to open selected PDF file stream."
      )

      pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
      pdfRenderer = PdfRenderer(pfd)
      val totalPages = pdfRenderer.pageCount
      if (totalPages <= 0) {
        throw GeminiBatchExtractionException(
          statusCode = 400,
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
          "Analyzing Pages $startPageHuman–$endPageHuman of $totalPages"

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
          val maxDimension = 1280f
          val scale = maxDimension / maxOf(pageW, pageH).toFloat()
          val targetW = (pageW * scale).roundToInt().coerceAtLeast(1)
          val targetH = (pageH * scale).roundToInt().coerceAtLeast(1)

          val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
          try {
            try {
              // Render onto high-contrast white background
              android.graphics.Canvas(bitmap).drawColor(android.graphics.Color.WHITE)
              page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            } finally {
              page.close()
            }

            val jpegBytes = ByteArrayOutputStream().use { baos ->
              bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
              baos.toByteArray()
            }
            val encodedBase64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
            base64PageImages.add(encodedBase64)
          } finally {
            if (!bitmap.isRecycled) {
              bitmap.recycle()
            }
          }
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

        onProgressUpdate(
          batchIdx + 1,
          totalBatches,
          scanningRangeStatus,
          accumulatedCards.toList(),
          latestQuestion,
          latestAnswer
        )
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
   * and sends each chunk sequentially to Gemini AI with Structured JSON Schema.
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
    if (!containsValidHumanReadableText(rawText)) {
      throw GeminiBatchExtractionException(
        statusCode = 400,
        message = "No valid human-readable text provided to generate flashcards."
      )
    }

    val chunks = splitTextIntoParagraphChunks(rawText, MAX_TEXT_CHUNK_CHARS)
    if (chunks.isEmpty()) {
      throw GeminiBatchExtractionException(
        statusCode = 400,
        message = "No text content provided to generate flashcards."
      )
    }

    val validPool = buildValidKeyPool(activeApiKey)
    if (validPool.isEmpty()) {
      throw GeminiBatchExtractionException(
        statusCode = 401,
        message = "No Gemini API key configured. Please tap 'Update API Key' or the Key icon to enter your Google AI Studio key."
      )
    }
    selectRandomizedKeyIndex(validPool)
    val totalBatches = chunks.size
    val accumulatedCards = mutableListOf<FlashcardItem>()
    val seenQuestions = mutableSetOf<String>()
    var latestQuestion = ""
    var latestAnswer = ""

    for ((batchIdx, chunkText) in chunks.withIndex()) {
      coroutineContext.ensureActive()

      val chunkNumber = batchIdx + 1
      val statusText = "Analyzing Chunk $chunkNumber of $totalBatches"

      onProgressUpdate(
        chunkNumber,
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
          batchNumber = chunkNumber,
          totalBatches = totalBatches,
          defaultCategory = defaultCategory
        )
      } catch (e: GeminiBatchExtractionException) {
        if (e.statusCode == 429) {
          delay(4000L)
          coroutineContext.ensureActive()
          requestGeminiFlashcardsForTextChunk(
            activeApiKey = activeApiKey,
            textChunk = chunkText,
            batchNumber = chunkNumber,
            totalBatches = totalBatches,
            defaultCategory = defaultCategory
          )
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

      onProgressUpdate(
        chunkNumber,
        totalBatches,
        statusText,
        accumulatedCards.toList(),
        latestQuestion,
        latestAnswer
      )
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
          "$SYSTEM_PROMPT_INSTRUCTION\n\nAnalyze PDF Pages $startPage to $endPage of $totalPages and extract all flashcards:"
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
            "$SYSTEM_PROMPT_INSTRUCTION\n\n--- DOCUMENT CHUNK $batchNumber OF $totalBatches ---\n$textChunk"
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
    val responseSchemaJson = JSONObject().apply {
      put("type", "ARRAY")
      put(
        "items",
        JSONObject().apply {
          put("type", "OBJECT")
          put(
            "properties",
            JSONObject().apply {
              put(
                "question",
                JSONObject().apply {
                  put("type", "STRING")
                  put("description", "The exact question, exam item, definition prompt, or concept inquiry")
                }
              )
              put(
                "answer",
                JSONObject().apply {
                  put("type", "STRING")
                  put("description", "The concise, accurate answer, solution, code reference, or definition")
                }
              )
            }
          )
          put("required", JSONArray().put("question").put("answer"))
        }
      )
    }

    val requestBodyJson = JSONObject().apply {
      put(
        "systemInstruction",
        JSONObject().apply {
          put(
            "parts",
            JSONArray().apply {
              put(
                JSONObject().apply {
                  put("text", SYSTEM_PROMPT_INSTRUCTION)
                }
              )
            }
          )
        }
      )
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
          put("responseSchema", responseSchemaJson)
        }
      )
    }.toString()

    val validPool = buildValidKeyPool(activeApiKey)
    if (validPool.isEmpty()) {
      throw GeminiBatchExtractionException(
        statusCode = 401,
        message = "No Gemini API key configured. Please tap 'Update API Key' or the Key icon to enter your Google AI Studio key."
      )
    }
    var keyIndex = activeClassroomKeyIndex.coerceIn(0, maxOf(0, validPool.size - 1))
    var lastException: GeminiBatchExtractionException? = null

    for (attempt in 0 until validPool.size) {
      val activeKey = validPool[keyIndex]
      activeClassroomKeyIndex = keyIndex

      val primaryEndpointUrl =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=$activeKey"
      val activeModel = resolveWorkingGeminiModel(activeKey, forceRefresh = false)
      val endpointUrl = if (activeModel == primaryModel) {
        primaryEndpointUrl
      } else {
        "https://generativelanguage.googleapis.com/v1beta/models/$activeModel:generateContent?key=$activeKey"
      }
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
            // Automatic fallback to gemini-3.8-flash first, then discovered Flash models
            val fallbackCandidates = (listOf("gemini-3.8-flash") + discoverAvailableFlashModels(activeKey)).distinct()
            for (candidateModel in fallbackCandidates) {
              if (candidateModel.equals(activeModel, ignoreCase = true)) continue
              cachedWorkingModelName = candidateModel
              try {
                val result = executeGeminiGenerateContentCall(
                  activeApiKey = activeKey,
                  partsArray = partsArray,
                  defaultCategory = defaultCategory,
                  allowModelDiscoveryFallback = false
                )
                hasVerifiedWorkingModel = true
                return result
              } catch (retryEx: GeminiBatchExtractionException) {
                if (retryEx.statusCode != 404 && retryEx.statusCode != 400) {
                  throw retryEx
                }
              }
            }
          }

          val parsedApiErrorMessage = extractDetailedErrorFromPayload(responseCode, rawErrorBody)
          lastException = GeminiBatchExtractionException(
            statusCode = responseCode,
            message = parsedApiErrorMessage
          )

          // If rate-limited (429) or key quota/auth error (403/400), rotate to next classroom key in pool
          if (responseCode == 429 || responseCode == 403 || responseCode == 400) {
            keyIndex = (keyIndex + 1) % validPool.size
            activeClassroomKeyIndex = keyIndex
            continue
          } else {
            throw lastException
          }
        }
      } catch (e: GeminiBatchExtractionException) {
        lastException = e
        if (e.statusCode == 429 || e.statusCode == 403) {
          keyIndex = (keyIndex + 1) % validPool.size
          activeClassroomKeyIndex = keyIndex
          continue
        }
        throw e
      } catch (e: CancellationException) {
        throw e
      } catch (_: Exception) {
        throw GeminiBatchExtractionException(
          statusCode = -1,
          message = OFFLINE_ERROR_MESSAGE
        )
      } finally {
        connection?.disconnect()
      }
    }

    throw (lastException ?: GeminiBatchExtractionException(
      statusCode = 429,
      message = "All classroom API keys are currently busy. Please wait a few seconds or share a classmate's deck code."
    ))
  }

  /**
   * Structured JSON Response Parser:
   * Extracts the structured JSON array of `{ "question": "...", "answer": "..." }` items
   * returned by Gemini.
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
 * Result of validating user credentials against the `SharedPreferences` local authentication engine.
 */
enum class LoginValidationResult {
  SUCCESS,
  INVALID_USERNAME,
  NO_ACCOUNT_FOUND,
  INCORRECT_PASSWORD,
  WEAK_PASSWORD,
  ACCOUNT_ALREADY_EXISTS,
  NETWORK_ERROR
}

/**
 * Extracts the scholar's trimmed email address from a [FirebaseUser].
 */
fun extractUsernameFromFirebaseUser(user: FirebaseUser?, fallbackEmail: String = ""): String {
  if (user == null) return fallbackEmail.trim()
  val rawEmail = user.email?.trim().orEmpty()
  if (rawEmail.isNotEmpty()) return rawEmail
  val preferredFallback = fallbackEmail.trim()
  if (
    preferredFallback.isNotEmpty() &&
    !preferredFallback.equals(LocalStorageManager.GUEST_ACCOUNT_KEY, ignoreCase = true) &&
    !preferredFallback.equals(LocalStorageManager.GUEST_DISPLAY_NAME, ignoreCase = true)
  ) {
    return preferredFallback
  }
  return user.displayName?.trim().orEmpty().ifBlank { user.uid.take(8) }
}

private const val FALLBACK_FIREBASE_PROJECT_ID = "gen-lang-client-0434293060"
private const val FALLBACK_FIREBASE_APP_ID = "1:615100480820:android:94b22be69907263a557961"
private const val FALLBACK_FIREBASE_STORAGE_BUCKET = "gen-lang-client-0434293060.firebasestorage.app"
private const val FALLBACK_FIREBASE_GCM_SENDER_ID = "615100480820"
private const val FALLBACK_FIREBASE_CLIENT_TOKEN_B64 = "QUl6YVN5QzZHMlBISEUyWlVPNURTeXJ2QnoyVHpncW00U2NVSHlV"

private fun resolveFirebaseClientApiKey(context: Context): String {
  val resId = context.resources.getIdentifier("google_api_key", "string", context.packageName)
  if (resId != 0) {
    val fromRes = runCatching { context.getString(resId).trim() }.getOrDefault("")
    if (fromRes.isNotEmpty()) return fromRes
  }
  return runCatching {
    String(
      android.util.Base64.decode(FALLBACK_FIREBASE_CLIENT_TOKEN_B64, android.util.Base64.NO_WRAP),
      Charsets.UTF_8
    ).trim()
  }.getOrDefault("")
}

/**
 * Ensures the default [FirebaseApp] is initialized in this process.
 * First attempts standard resource-based initialization via [FirebaseApp.initializeApp],
 * and falls back to explicit [FirebaseOptions] if default resource options were not generated.
 */
fun ensureFirebaseInitialized(context: Context) {
  try {
    val appContext = context.applicationContext ?: context
    if (FirebaseApp.getApps(appContext).isEmpty()) {
      FirebaseApp.initializeApp(appContext)
    }
    if (FirebaseApp.getApps(appContext).isEmpty()) {
      val apiKey = resolveFirebaseClientApiKey(appContext)
      if (apiKey.isNotEmpty()) {
        val options = FirebaseOptions.Builder()
          .setProjectId(FALLBACK_FIREBASE_PROJECT_ID)
          .setApplicationId(FALLBACK_FIREBASE_APP_ID)
          .setApiKey(apiKey)
          .setStorageBucket(FALLBACK_FIREBASE_STORAGE_BUCKET)
          .setGcmSenderId(FALLBACK_FIREBASE_GCM_SENDER_ID)
          .build()
        FirebaseApp.initializeApp(appContext, options)
      }
    }
  } catch (_: Throwable) {
  }
}

/**
 * Safely returns [FirebaseAuth.getInstance], initializing [FirebaseApp] if needed.
 */
fun getFirebaseAuthOrNull(context: Context? = null): FirebaseAuth? {
  return try {
    if (context != null) {
      if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
      }
      ensureFirebaseInitialized(context)
    }
    FirebaseAuth.getInstance()
  } catch (_: Throwable) {
    null
  }
}

/**
 * Maps Firebase Authentication exceptions to clear, user-friendly messages for the Liquid Glass Auth card.
 */
fun mapFirebaseAuthExceptionToMessage(exception: Throwable?, isSignUp: Boolean): String {
  if (isSignUp) {
    return when (exception) {
      is FirebaseAuthUserCollisionException -> "An account with this email already exists."
      is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters."
      is FirebaseAuthInvalidCredentialsException -> "Please enter a valid email address."
      else -> "Registration failed: ${exception?.localizedMessage ?: "Unknown error"}"
    }
  } else {
    val errorCode = (exception as? FirebaseAuthException)?.errorCode.orEmpty()
    val rawMessage = exception?.message.orEmpty()
    val isNetworkFailure = exception is FirebaseNetworkException ||
      exception is java.net.SocketTimeoutException ||
      exception is java.net.UnknownHostException ||
      exception is java.io.IOException ||
      errorCode == "ERROR_NETWORK_REQUEST_FAILED" ||
      rawMessage.contains("network", ignoreCase = true) ||
      rawMessage.contains("timeout", ignoreCase = true) ||
      rawMessage.contains("unreachable", ignoreCase = true)
    return if (isNetworkFailure) {
      "Network error. Please check your internet connection."
    } else {
      "Invalid email or password. Please check your credentials."
    }
  }
}

/**
 * Computes the SHA-256 hex digest of [input] so raw plaintext passwords and PINs
 * are never stored in `SharedPreferences`.
 */
fun hashSecret(input: String): String {
  val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
  return bytes.joinToString("") { "%02x".format(it) }
}

/**
 * Stored account metadata for registered scholars.
 */
data class StoredScholarAccount(
  val username: String,
  val displayName: String,
  val passwordHash: String,
  val recoveryPinHash: String
)

/**
 * Validates that a username is not blank and has at least 3 characters.
 */
fun isValidUsername(username: String): Boolean {
  val trimmed = username.trim()
  return trimmed.isNotBlank() && trimmed.length >= 3
}

/**
 * Lightweight offline data manager wrapping Android's native `SharedPreferences`.
 * Acts as a local backend that persists:
 * 1. SHA-256 hashed user accounts (`auth_username`, `auth_display_name`, `auth_password_hash`,
 *    `auth_recovery_pin_hash`, `auth_is_logged_in`, `auth_is_guest`).
 * 2. Account-segregated `Task` lists (`tasks_[username]` or `tasks_guest`).
 * 3. Account-segregated `ClassSchedule` lists (`schedules_[username]` or `schedules_guest`).
 * 4. Account-segregated `AlarmItem` lists (`alarms_[username]` or `alarms_guest`).
 * 5. Account-segregated `FlashcardDeck` lists (`flashcards_[username]` or `flashcards_guest`).
 * 6. Active user session (`auth_is_logged_in` / `auth_is_guest`) and `isDarkMode`.
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
   * On app startup, if `SharedPreferences` has no active session (such as after an uninstall
   * and fresh reinstall), automatically looks for `"sibil_device_backup_${androidId}.json"` in
   * `MediaStore.Downloads` or `Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)`.
   * If found, silently restores the account, tasks, schedules, alarms, and flashcard decks.
   */
  private fun restoreFromDeviceBackupIfUnauthenticated() {
    val existingUser = prefs.getString(KEY_USER_NAME, "")?.trim().orEmpty()
    val hasAuthAccount = prefs.getString(KEY_AUTH_USERNAME, "")?.trim().orEmpty().isNotBlank()
    val isLoggedIn = prefs.getBoolean(KEY_AUTH_IS_LOGGED_IN, false)
    val isGuest = prefs.getBoolean(KEY_AUTH_IS_GUEST, false)
    if (existingUser.isNotBlank() || hasAuthAccount || isLoggedIn || isGuest) return

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
    val currentAuthUser = prefs.getString(KEY_AUTH_USERNAME, "")?.trim().orEmpty()
    val hasRegisteredAccounts = getRegisteredUsers().isNotEmpty() || getRegisteredAccountsMap().isNotEmpty()
    if (currentSavedUser.isBlank() && currentAuthUser.isBlank() && !hasRegisteredAccounts) {
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
   * Returns all registered scholar accounts stored in `SharedPreferences` (normalized lowercase username -> StoredScholarAccount).
   */
  fun getRegisteredAccountsMap(): Map<String, StoredScholarAccount> {
    val result = mutableMapOf<String, StoredScholarAccount>()
    val rawAccountsJson = prefs.getString(KEY_REGISTERED_ACCOUNTS_JSON, "")?.trim().orEmpty()
    if (rawAccountsJson.isNotEmpty()) {
      try {
        val root = JSONObject(rawAccountsJson)
        val keys = root.keys()
        while (keys.hasNext()) {
          val k = keys.next().trim().lowercase(Locale.US)
          val obj = root.optJSONObject(k) ?: continue
          val handle = obj.optString("username", k).trim().ifEmpty { k }
          val display = obj.optString("displayName", handle).trim().ifEmpty { handle }
          val passHash = obj.optString("passwordHash", "").trim()
          val pinHash = obj.optString("recoveryPinHash", "").trim()
          if (k.isNotEmpty() && passHash.isNotEmpty()) {
            result[k] = StoredScholarAccount(
              username = handle,
              displayName = display,
              passwordHash = passHash,
              recoveryPinHash = pinHash
            )
          }
        }
      } catch (_: Exception) {
      }
    }

    // Also include the primary top-level auth_username schema if present
    val primaryAuthUsername = prefs.getString(KEY_AUTH_USERNAME, "")?.trim().orEmpty()
    val primaryNormalized = normalizeAccountKey(primaryAuthUsername)
    val primaryPassHash = prefs.getString(KEY_AUTH_PASSWORD_HASH, "")?.trim().orEmpty()
    if (
      primaryNormalized.isNotEmpty() &&
      primaryNormalized != GUEST_ACCOUNT_KEY &&
      primaryPassHash.isNotEmpty()
    ) {
      val primaryDisplay = prefs.getString(KEY_AUTH_DISPLAY_NAME, primaryAuthUsername)
        ?.trim()
        .orEmpty()
        .ifEmpty { primaryAuthUsername }
      val primaryPinHash = prefs.getString(KEY_AUTH_RECOVERY_PIN_HASH, "")?.trim().orEmpty()
      val existing = result[primaryNormalized]
      result[primaryNormalized] = StoredScholarAccount(
        username = existing?.username?.ifBlank { primaryAuthUsername } ?: primaryAuthUsername,
        displayName = primaryDisplay.ifBlank { existing?.displayName ?: primaryAuthUsername },
        passwordHash = primaryPassHash,
        recoveryPinHash = primaryPinHash.ifBlank { existing?.recoveryPinHash.orEmpty() }
      )
    }

    return result
  }

  private fun saveRegisteredAccountsMap(accounts: Map<String, StoredScholarAccount>) {
    val root = JSONObject()
    accounts.forEach { (normKey, acct) ->
      val obj = JSONObject().apply {
        put("username", acct.username)
        put("displayName", acct.displayName)
        put("passwordHash", acct.passwordHash)
        put("recoveryPinHash", acct.recoveryPinHash)
      }
      root.put(normKey, obj)
    }
    prefs.edit().putString(KEY_REGISTERED_ACCOUNTS_JSON, root.toString()).apply()
  }

  /**
   * Returns all registered users stored in `SharedPreferences` (normalized lowercase username -> passwordHash).
   * Uses safe string default `""` and fallback parsing.
   */
  fun getRegisteredUsers(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    val rawJson = prefs.getString(KEY_REGISTERED_USERS, "")?.trim().orEmpty()
    if (rawJson.isNotEmpty()) {
      try {
        val jsonObject = JSONObject(rawJson)
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
          val usernameKey = keys.next()
          map[usernameKey.trim().lowercase(Locale.US)] = jsonObject.optString(usernameKey, "")
        }
      } catch (_: Exception) {
      }
    }
    getRegisteredAccountsMap().forEach { (normKey, acct) ->
      map[normKey] = acct.passwordHash
    }
    return map
  }

  /**
   * Checks whether the given username is already registered in `SharedPreferences`.
   */
  fun isUsernameRegistered(username: String): Boolean {
    val normalizedUsername = normalizeAccountKey(username)
    if (normalizedUsername.isEmpty() || normalizedUsername == GUEST_ACCOUNT_KEY) return false
    val primaryAuthUser = normalizeAccountKey(prefs.getString(KEY_AUTH_USERNAME, ""))
    if (primaryAuthUser == normalizedUsername && !prefs.getString(KEY_AUTH_PASSWORD_HASH, "").isNullOrBlank()) {
      return true
    }
    return getRegisteredUsers().containsKey(normalizedUsername)
  }

  /**
   * Verifies that the username exists and `hashSecret(enteredPassword) == storedPasswordHash`.
   */
  fun verifyUserPassword(username: String, password: String): Boolean {
    val normalizedUsername = normalizeAccountKey(username)
    if (normalizedUsername.isEmpty() || normalizedUsername == GUEST_ACCOUNT_KEY) return false
    val enteredHash = hashSecret(password)

    val primaryAuthUser = normalizeAccountKey(prefs.getString(KEY_AUTH_USERNAME, ""))
    val primaryStoredHash = prefs.getString(KEY_AUTH_PASSWORD_HASH, "")?.trim().orEmpty()
    if (primaryAuthUser == normalizedUsername && primaryStoredHash.isNotEmpty()) {
      if (enteredHash == primaryStoredHash || password == primaryStoredHash) return true
    }

    val acct = getRegisteredAccountsMap()[normalizedUsername]
    if (acct != null && acct.passwordHash.isNotEmpty()) {
      if (enteredHash == acct.passwordHash || password == acct.passwordHash) return true
    }

    val savedPassword = getRegisteredUsers()[normalizedUsername] ?: return false
    return savedPassword == enteredHash || savedPassword == password
  }

  /**
   * Verifies that the username exists and `hashSecret(enteredPin) == storedPinHash`.
   */
  fun verifyRecoveryPin(username: String, enteredPin: String): Boolean {
    val normalizedUsername = normalizeAccountKey(username)
    val cleanPin = enteredPin.trim()
    if (normalizedUsername.isEmpty() || normalizedUsername == GUEST_ACCOUNT_KEY || cleanPin.isEmpty()) {
      return false
    }
    val enteredPinHash = hashSecret(cleanPin)

    val primaryAuthUser = normalizeAccountKey(prefs.getString(KEY_AUTH_USERNAME, ""))
    val primaryPinHash = prefs.getString(KEY_AUTH_RECOVERY_PIN_HASH, "")?.trim().orEmpty()
    if (primaryAuthUser == normalizedUsername && primaryPinHash.isNotEmpty()) {
      if (enteredPinHash == primaryPinHash) return true
    }

    val acct = getRegisteredAccountsMap()[normalizedUsername] ?: return false
    return acct.recoveryPinHash.isNotEmpty() && enteredPinHash == acct.recoveryPinHash
  }

  /**
   * Resets a user's password after verifying their 4-digit Recovery PIN.
   * Updates `auth_password_hash` with the new SHA-256 hash without wiping flashcards or saved settings.
   */
  fun resetPasswordWithRecoveryPin(
    username: String,
    enteredPin: String,
    newPassword: String
  ): Boolean {
    val normalizedUsername = normalizeAccountKey(username)
    if (normalizedUsername.isEmpty() || normalizedUsername == GUEST_ACCOUNT_KEY) return false
    if (newPassword.isBlank()) return false
    if (!verifyRecoveryPin(username, enteredPin)) return false

    val newPasswordHash = hashSecret(newPassword)
    val accounts = getRegisteredAccountsMap().toMutableMap()
    val existingAcct = accounts[normalizedUsername]
    val canonicalUsername = existingAcct?.username ?: username.trim()
    val canonicalDisplay = existingAcct?.displayName ?: prefs.getString(KEY_AUTH_DISPLAY_NAME, canonicalUsername).orEmpty().ifBlank { canonicalUsername }
    val canonicalPinHash = existingAcct?.recoveryPinHash?.ifBlank { hashSecret(enteredPin.trim()) } ?: hashSecret(enteredPin.trim())

    accounts[normalizedUsername] = StoredScholarAccount(
      username = canonicalUsername,
      displayName = canonicalDisplay,
      passwordHash = newPasswordHash,
      recoveryPinHash = canonicalPinHash
    )
    saveRegisteredAccountsMap(accounts)

    val currentUsers = getRegisteredUsers().toMutableMap()
    currentUsers[normalizedUsername] = newPasswordHash
    val usersJson = JSONObject()
    currentUsers.forEach { (k, v) -> usersJson.put(k, v) }

    val editor = prefs.edit()
      .putString(KEY_REGISTERED_USERS, usersJson.toString())
      .putString(KEY_AUTH_PASSWORD_HASH, newPasswordHash)
    val primaryAuthUser = normalizeAccountKey(prefs.getString(KEY_AUTH_USERNAME, ""))
    if (primaryAuthUser.isEmpty() || primaryAuthUser == normalizedUsername) {
      editor.putString(KEY_AUTH_USERNAME, canonicalUsername)
      editor.putString(KEY_AUTH_DISPLAY_NAME, canonicalDisplay)
      editor.putString(KEY_AUTH_RECOVERY_PIN_HASH, canonicalPinHash)
    }
    editor.apply()
    syncToDeviceStorage(normalizedUsername)
    return true
  }

  /**
   * Registers a new user account with SHA-256 hashed password and 4-digit recovery PIN in `SharedPreferences`.
   */
  fun registerUser(
    username: String,
    password: String,
    displayName: String = "",
    recoveryPin: String = ""
  ) {
    val cleanUsername = username.trim()
    val normalizedUsername = normalizeAccountKey(cleanUsername)
    if (normalizedUsername.isEmpty() || normalizedUsername == GUEST_ACCOUNT_KEY) return

    val resolvedDisplayName = displayName.trim().ifBlank { cleanUsername }
    val passwordHash = hashSecret(password)
    val pinHash = if (recoveryPin.trim().isNotEmpty()) hashSecret(recoveryPin.trim()) else ""

    val accounts = getRegisteredAccountsMap().toMutableMap()
    accounts[normalizedUsername] = StoredScholarAccount(
      username = cleanUsername,
      displayName = resolvedDisplayName,
      passwordHash = passwordHash,
      recoveryPinHash = pinHash
    )
    saveRegisteredAccountsMap(accounts)

    val currentUsers = getRegisteredUsers().toMutableMap()
    currentUsers[normalizedUsername] = passwordHash
    val jsonObject = JSONObject()
    currentUsers.forEach { (k, v) -> jsonObject.put(k, v) }

    prefs.edit()
      .putString(KEY_REGISTERED_USERS, jsonObject.toString())
      .putString(KEY_AUTH_USERNAME, cleanUsername)
      .putString(KEY_AUTH_DISPLAY_NAME, resolvedDisplayName)
      .putString(KEY_AUTH_PASSWORD_HASH, passwordHash)
      .putString(KEY_AUTH_RECOVERY_PIN_HASH, pinHash)
      .putBoolean(KEY_AUTH_IS_LOGGED_IN, true)
      .putBoolean(KEY_AUTH_IS_GUEST, false)
      .putString(KEY_USER_NAME, cleanUsername)
      .apply()
    syncToDeviceStorage(normalizedUsername)
  }

  fun getDisplayNameForUser(username: String): String {
    val clean = username.trim()
    val normalized = normalizeAccountKey(clean)
    if (normalized.isEmpty() || normalized == GUEST_ACCOUNT_KEY) return GUEST_DISPLAY_NAME
    val acct = getRegisteredAccountsMap()[normalized]
    if (acct != null && acct.displayName.isNotBlank()) {
      return acct.displayName
    }
    val primaryAuthUser = normalizeAccountKey(prefs.getString(KEY_AUTH_USERNAME, ""))
    if (primaryAuthUser == normalized) {
      val primaryDisplay = prefs.getString(KEY_AUTH_DISPLAY_NAME, "")?.trim().orEmpty()
      if (primaryDisplay.isNotBlank()) return primaryDisplay
    }
    return clean
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

  fun isAuthLoggedIn(): Boolean {
    return prefs.getBoolean(KEY_AUTH_IS_LOGGED_IN, false)
  }

  fun isAuthGuest(): Boolean {
    return prefs.getBoolean(KEY_AUTH_IS_GUEST, false)
  }

  fun hasActiveAuthSession(): Boolean {
    if (prefs.contains(KEY_AUTH_IS_LOGGED_IN) || prefs.contains(KEY_AUTH_IS_GUEST)) {
      return isAuthLoggedIn() || isAuthGuest()
    }
    return !prefs.getString(KEY_USER_NAME, "").isNullOrBlank()
  }

  fun saveUserProfile(username: String, displayName: String = "") {
    val clean = username.trim()
    val isGuest = clean.isEmpty() ||
      clean.equals(GUEST_ACCOUNT_KEY, ignoreCase = true) ||
      clean.equals(GUEST_DISPLAY_NAME, ignoreCase = true) ||
      clean.equals("Offline Scholar", ignoreCase = true)

    val editor = prefs.edit()
    if (isGuest) {
      editor
        .putBoolean(KEY_AUTH_IS_GUEST, true)
        .putBoolean(KEY_AUTH_IS_LOGGED_IN, false)
        .putString(KEY_USER_NAME, GUEST_DISPLAY_NAME)
        .apply()
      syncToDeviceStorage(GUEST_ACCOUNT_KEY)
    } else {
      val norm = normalizeAccountKey(clean)
      val acct = getRegisteredAccountsMap()[norm]
      val resolvedDisplay = displayName.trim().ifBlank {
        acct?.displayName?.ifBlank { getDisplayNameForUser(clean) } ?: getDisplayNameForUser(clean)
      }
      editor
        .putString(KEY_AUTH_USERNAME, clean)
        .putString(KEY_AUTH_DISPLAY_NAME, resolvedDisplay)
        .putBoolean(KEY_AUTH_IS_LOGGED_IN, true)
        .putBoolean(KEY_AUTH_IS_GUEST, false)
        .putString(KEY_USER_NAME, clean)
      if (acct != null) {
        if (acct.passwordHash.isNotBlank()) {
          editor.putString(KEY_AUTH_PASSWORD_HASH, acct.passwordHash)
        }
        if (acct.recoveryPinHash.isNotBlank()) {
          editor.putString(KEY_AUTH_RECOVERY_PIN_HASH, acct.recoveryPinHash)
        }
      }
      editor.apply()
      syncToDeviceStorage(clean)
    }
  }

  fun loadUserName(): String? {
    if (prefs.contains(KEY_AUTH_IS_LOGGED_IN) || prefs.contains(KEY_AUTH_IS_GUEST)) {
      val loggedIn = prefs.getBoolean(KEY_AUTH_IS_LOGGED_IN, false)
      val guest = prefs.getBoolean(KEY_AUTH_IS_GUEST, false)
      if (loggedIn) {
        val authUser = prefs.getString(KEY_AUTH_USERNAME, "")?.trim().orEmpty()
        if (authUser.isNotBlank()) return authUser
      } else if (guest) {
        return GUEST_DISPLAY_NAME
      } else {
        return null
      }
    }
    val saved = prefs.getString(KEY_USER_NAME, "")?.trim().orEmpty()
    return saved.takeIf { it.isNotBlank() }
  }

  fun loadDisplayName(): String {
    if (isAuthGuest()) return GUEST_DISPLAY_NAME
    val savedDisplay = prefs.getString(KEY_AUTH_DISPLAY_NAME, "")?.trim().orEmpty()
    if (savedDisplay.isNotBlank()) return savedDisplay
    val activeUser = loadUserName().orEmpty()
    return if (activeUser.isNotBlank()) getDisplayNameForUser(activeUser) else GUEST_DISPLAY_NAME
  }

  fun clearUserProfile() {
    prefs.edit()
      .putBoolean(KEY_AUTH_IS_LOGGED_IN, false)
      .putBoolean(KEY_AUTH_IS_GUEST, false)
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

  fun shouldShowWhatsNew(context: Context = appContext, currentVersionCode: Int): Boolean {
    return currentVersionCode > prefs.getInt(PREF_LAST_SEEN_VERSION, 0)
  }

  fun markWhatsNewSeen(context: Context = appContext, currentVersionCode: Int) {
    prefs.edit().putInt(PREF_LAST_SEEN_VERSION, currentVersionCode).apply()
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
    val registeredAccountsV2Json = prefs.getString(KEY_REGISTERED_ACCOUNTS_JSON, "{}") ?: "{}"
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
      put("auth_username", prefs.getString(KEY_AUTH_USERNAME, "") ?: "")
      put("auth_display_name", prefs.getString(KEY_AUTH_DISPLAY_NAME, "") ?: "")
      put("auth_password_hash", prefs.getString(KEY_AUTH_PASSWORD_HASH, "") ?: "")
      put("auth_recovery_pin_hash", prefs.getString(KEY_AUTH_RECOVERY_PIN_HASH, "") ?: "")
      put("auth_is_logged_in", prefs.getBoolean(KEY_AUTH_IS_LOGGED_IN, false))
      put("auth_is_guest", prefs.getBoolean(KEY_AUTH_IS_GUEST, false))
      put("registeredUsers", registeredUsersJson)
      put("registeredAccountsV2", registeredAccountsV2Json)
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

      // 2. Restore User Profile, Auth Schema, & Dark Mode
      val restoredUserProfile = rootObj.optString("userProfile", "").trim()
      if (restoredUserProfile.isNotEmpty()) {
        editor.putString(KEY_USER_NAME, restoredUserProfile)
      }
      if (rootObj.has("auth_username")) {
        editor.putString(KEY_AUTH_USERNAME, rootObj.optString("auth_username", ""))
      }
      if (rootObj.has("auth_display_name")) {
        editor.putString(KEY_AUTH_DISPLAY_NAME, rootObj.optString("auth_display_name", ""))
      }
      if (rootObj.has("auth_password_hash")) {
        editor.putString(KEY_AUTH_PASSWORD_HASH, rootObj.optString("auth_password_hash", ""))
      }
      if (rootObj.has("auth_recovery_pin_hash")) {
        editor.putString(KEY_AUTH_RECOVERY_PIN_HASH, rootObj.optString("auth_recovery_pin_hash", ""))
      }
      if (rootObj.has("auth_is_logged_in")) {
        editor.putBoolean(KEY_AUTH_IS_LOGGED_IN, rootObj.optBoolean("auth_is_logged_in", false))
      }
      if (rootObj.has("auth_is_guest")) {
        editor.putBoolean(KEY_AUTH_IS_GUEST, rootObj.optBoolean("auth_is_guest", false))
      }
      if (rootObj.has("isDarkMode")) {
        editor.putBoolean(KEY_DARK_MODE, rootObj.optBoolean("isDarkMode", false))
      }

      val regUsersRaw = rootObj.optJSONObject("registeredUsers")?.toString()
        ?: rootObj.optString("registeredUsers", "").trim()
      if (regUsersRaw.isNotEmpty()) {
        editor.putString(KEY_REGISTERED_USERS, regUsersRaw)
      }

      val regAccountsV2Raw = rootObj.optJSONObject("registeredAccountsV2")?.toString()
        ?: rootObj.optString("registeredAccountsV2", "").trim()
      if (regAccountsV2Raw.isNotEmpty()) {
        editor.putString(KEY_REGISTERED_ACCOUNTS_JSON, regAccountsV2Raw)
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
    const val KEY_AUTH_USERNAME = "auth_username"
    const val KEY_AUTH_DISPLAY_NAME = "auth_display_name"
    const val KEY_AUTH_PASSWORD_HASH = "auth_password_hash"
    const val KEY_AUTH_RECOVERY_PIN_HASH = "auth_recovery_pin_hash"
    const val KEY_AUTH_IS_LOGGED_IN = "auth_is_logged_in"
    const val KEY_AUTH_IS_GUEST = "auth_is_guest"
    private const val PREFS_NAME = "sibil_app_offline_storage"
    private const val KEY_REGISTERED_USERS = "registered_users_json"
    private const val KEY_REGISTERED_ACCOUNTS_JSON = "registered_accounts_v2_json"
    private const val KEY_TASKS_PREFIX = "tasks_"
    private const val KEY_SCHEDULES_PREFIX = "schedules_"
    private const val KEY_ALARMS_PREFIX = "alarms_"
    private const val KEY_FLASHCARDS_PREFIX = "flashcards_"
    private const val KEY_USER_NAME = "saved_user_name"
    private const val KEY_DARK_MODE = "saved_dark_mode"
    const val PREF_LAST_SEEN_VERSION = "last_seen_version_code"
    private const val KEY_GEMINI_API_KEY = "saved_gemini_api_key"
    private const val KEY_TASK_SCHEMA_VERSION = "task_schema_version"
    private const val CURRENT_TASK_SCHEMA_VERSION = 2
    private const val KEY_FLASHCARD_SCHEMA_VERSION = "flashcard_schema_version"
    private const val CURRENT_FLASHCARD_SCHEMA_VERSION = 2

    fun shouldShowWhatsNew(context: Context, currentVersionCode: Int): Boolean {
      val sharedPrefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      return currentVersionCode > sharedPrefs.getInt(PREF_LAST_SEEN_VERSION, 0)
    }

    fun markWhatsNewSeen(context: Context, currentVersionCode: Int) {
      val sharedPrefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      sharedPrefs.edit().putInt(PREF_LAST_SEEN_VERSION, currentVersionCode).apply()
    }
  }
}

data class EnvironmentalWeatherState(
  val isLoading: Boolean = true,
  val isOffline: Boolean = false,
  val conditionText: String = "Fetching Weather...",
  val conditionEmoji: String = "⛅",
  val weatherCode: Int = 1,
  val temperatureCelsius: Int = 29,
  val heatIndexCelsius: Int = 33,
  val relativeHumidity: Int = 75,
  val usAqi: Int = 42,
  val pm25: Double = 8.5,
  val aqiRating: String = "Good"
)

/**
 * Shared ViewModel storing and managing:
 * 1. Global theme state (`isDarkMode`, default `false`).
 * 2. User session data (`username`, `displayName`, and `hasSavedSession`).
 * 3. Live Open-Meteo weather summary (`weatherText` and `weatherState`, starting at `"Fetching Weather..."`).
 * 4. Account-segregated coursework tasks shared across the Academic Dashboard and Task Manager (`tasks_[username]`).
 * 5. Today's Schedule list on the Dashboard.
 * 6. Account-segregated `ClassSchedule` items for the colored Week Timetable & Grid View (`schedules_[username]`).
 * 7. Account-segregated `FlashcardDeck` items for the Smart Flashcard reviewer (`flashcards_[username]`).
 * 8. SHA-256 Username + Password Authentication, 4-Digit Recovery PIN Reset, & Device-Bound Auto-Persistence via `LocalStorageManager`.
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

  var displayName by mutableStateOf(LocalStorageManager.GUEST_DISPLAY_NAME)
    private set

  val userName: String
    get() = displayUsername

  val displayUsername: String
    get() = if (isGuestMode) {
      LocalStorageManager.GUEST_DISPLAY_NAME
    } else {
      displayName.trim().ifBlank { username }
    }

  var hasSavedSession by mutableStateOf(false)
    private set

  var weatherText by mutableStateOf("Fetching Weather...")
    private set

  var weatherState by mutableStateOf(EnvironmentalWeatherState())
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

  var isGuestMode by mutableStateOf(false)
    private set

  val currentAccountKey: String
    get() = if (isGuestMode) {
      LocalStorageManager.GUEST_ACCOUNT_KEY
    } else {
      username.trim().lowercase(Locale.US).ifBlank { LocalStorageManager.GUEST_ACCOUNT_KEY }
    }

  // Default state of the Task list is completely empty (no hardcoded placeholder tasks)
  private val _tasks = mutableStateListOf<Task>()
  val tasks: List<Task> get() = _tasks

  private val _classSchedules = mutableStateListOf<ClassSchedule>()
  val classSchedules: List<ClassSchedule> get() = _classSchedules

  // Default state of the Alarm list is completely empty (no hardcoded mock alarms)
  private val _alarms = mutableStateListOf<AlarmItem>()
  val alarms: List<AlarmItem> get() = _alarms

  // Default state of the Flashcard Decks list is completely empty (no hardcoded placeholder decks)
  private val _flashcardDecks = mutableStateListOf<FlashcardDeck>()
  val flashcardDecks: List<FlashcardDeck> get() = _flashcardDecks

  // Initial Firestore payload loading states
  var isSyncingTasks by mutableStateOf(false)
    private set
  var isSyncingSchedule by mutableStateOf(false)
    private set
  var isSyncingFlashcards by mutableStateOf(false)
    private set
  var isSyncingSettings by mutableStateOf(false)
    private set

  val isFirestoreSyncing: Boolean
    get() = isSyncingTasks || isSyncingSchedule || isSyncingFlashcards || isSyncingSettings

  private var activeFirestoreUserId: String? = null
  private var tasksSnapshotListener: ListenerRegistration? = null
  private var scheduleSnapshotListener: ListenerRegistration? = null
  private var flashcardsSnapshotListener: ListenerRegistration? = null
  private var settingsSnapshotListener: ListenerRegistration? = null
  private var hasSeededTasksForUser: String? = null
  private var hasSeededScheduleForUser: String? = null
  private var hasSeededFlashcardsForUser: String? = null
  private var hasSeededSettingsForUser: String? = null

  private fun resolveActiveCloudUserId(): String? {
    if (isGuestMode) return null
    return try {
      FirebaseAuth.getInstance().currentUser?.uid?.takeIf { it.isNotBlank() }
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Starts real-time Firestore snapshot listeners isolated by [userId]:
   * - `db.collection("users").document(userId).collection("tasks")`
   * - `db.collection("users").document(userId).collection("schedule")`
   * - `db.collection("users").document(userId).collection("flashcards")`
   * - `db.collection("users").document(userId).collection("settings")`
   * If `FirebaseAuth.getInstance().currentUser == null` (Offline Scholar mode), falls back gracefully to local state.
   */
  fun startFirestoreSyncForUser(userId: String) {
    val cleanUid = userId.trim()
    if (cleanUid.isEmpty() || isGuestMode) {
      stopFirestoreSync()
      return
    }
    if (
      activeFirestoreUserId == cleanUid &&
      tasksSnapshotListener != null &&
      scheduleSnapshotListener != null &&
      flashcardsSnapshotListener != null &&
      settingsSnapshotListener != null
    ) {
      return
    }

    stopFirestoreSync()
    activeFirestoreUserId = cleanUid
    isSyncingTasks = true
    isSyncingSchedule = true
    isSyncingFlashcards = true
    isSyncingSettings = true

    try {
      val db = FirebaseFirestore.getInstance()
      val userDoc = db.collection("users").document(cleanUid)

      // 1. Real-time Snapshot Listener for Tasks
      tasksSnapshotListener = userDoc.collection("tasks")
        .addSnapshotListener { snapshot, error ->
          isSyncingTasks = false
          if (error != null || snapshot == null) return@addSnapshotListener
          val remoteTasks = snapshot.documents.mapNotNull { doc ->
            doc.toTaskItemOrNull()
          }.sortedByDescending { it.id }

          if (remoteTasks.isEmpty() && _tasks.isNotEmpty() && hasSeededTasksForUser != cleanUid) {
            hasSeededTasksForUser = cleanUid
            val localSnapshot = _tasks.toList()
            localSnapshot.forEach { taskItem ->
              userDoc.collection("tasks")
                .document(taskItem.id.toString())
                .set(taskItem.toFirestoreMap())
            }
          } else {
            hasSeededTasksForUser = cleanUid
            _tasks.clear()
            _tasks.addAll(remoteTasks)
            nextId = (remoteTasks.maxOfOrNull { it.id } ?: 0) + 1
            storageManager?.saveTasks(_tasks, currentAccountKey)
          }
        }

      // 2. Real-time Snapshot Listener for Schedule
      scheduleSnapshotListener = userDoc.collection("schedule")
        .addSnapshotListener { snapshot, error ->
          isSyncingSchedule = false
          if (error != null || snapshot == null) return@addSnapshotListener
          val remoteSchedules = snapshot.documents.mapNotNull { doc ->
            doc.toScheduleItemOrNull()
          }.sortedBy { parseTimeToFloat(it.startTime) }

          if (
            remoteSchedules.isEmpty() &&
            _classSchedules.isNotEmpty() &&
            hasSeededScheduleForUser != cleanUid
          ) {
            hasSeededScheduleForUser = cleanUid
            val localSnapshot = _classSchedules.toList()
            localSnapshot.forEach { scheduleItem ->
              userDoc.collection("schedule")
                .document(scheduleItem.id.toString())
                .set(scheduleItem.toFirestoreMap())
            }
          } else {
            hasSeededScheduleForUser = cleanUid
            _classSchedules.clear()
            _classSchedules.addAll(remoteSchedules)
            nextScheduleId = (remoteSchedules.maxOfOrNull { it.id } ?: 99) + 1
            storageManager?.saveSchedules(_classSchedules, currentAccountKey)
            val validFolders = getSubjectFolders()
            if (validFolders.none { it.equals(selectedTaskFolder, ignoreCase = true) }) {
              selectedTaskFolder = DEFAULT_TASK_CATEGORY
            }
          }
        }

      // 3. Real-time Snapshot Listener for Flashcards
      flashcardsSnapshotListener = userDoc.collection("flashcards")
        .addSnapshotListener { snapshot, error ->
          isSyncingFlashcards = false
          if (error != null || snapshot == null) return@addSnapshotListener
          val remoteDecks = snapshot.documents.mapNotNull { doc ->
            doc.toFlashcardDeckOrNull()
          }.sortedByDescending { it.id }

          if (
            remoteDecks.isEmpty() &&
            _flashcardDecks.isNotEmpty() &&
            hasSeededFlashcardsForUser != cleanUid
          ) {
            hasSeededFlashcardsForUser = cleanUid
            val localSnapshot = _flashcardDecks.toList()
            localSnapshot.forEach { deck ->
              userDoc.collection("flashcards")
                .document(deck.id.toString())
                .set(deck.toFirestoreMap())
            }
          } else {
            hasSeededFlashcardsForUser = cleanUid
            _flashcardDecks.clear()
            _flashcardDecks.addAll(remoteDecks)
            nextDeckId = (remoteDecks.maxOfOrNull { it.id } ?: 0) + 1
            val maxCardId = remoteDecks.flatMap { it.cards }.maxOfOrNull { it.id } ?: 999
            nextCardId = maxOf(1000, maxCardId + 1)
            storageManager?.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
          }
        }

      // 4. Real-time Snapshot Listener for Settings (Dark Mode & Profile Name)
      settingsSnapshotListener = userDoc.collection("settings")
        .addSnapshotListener { snapshot, error ->
          isSyncingSettings = false
          if (error != null || snapshot == null) return@addSnapshotListener
          val settingsDoc = snapshot.documents.firstOrNull { it.id == "preferences" }
            ?: snapshot.documents.firstOrNull()

          if (settingsDoc == null) {
            if (hasSeededSettingsForUser != cleanUid) {
              hasSeededSettingsForUser = cleanUid
              syncSettingsToFirestore()
            }
          } else {
            hasSeededSettingsForUser = cleanUid
            val remoteDark = settingsDoc.getBoolean("isDarkMode")
              ?: settingsDoc.getBoolean("darkMode")
            if (remoteDark != null && remoteDark != isDarkMode) {
              isDarkMode = remoteDark
              storageManager?.saveDarkMode(remoteDark)
            }
            val remoteProfileName = (
              settingsDoc.getString("profileName")
                ?: settingsDoc.getString("displayName")
              )?.trim().orEmpty()
            if (remoteProfileName.isNotEmpty() && remoteProfileName != displayName) {
              displayName = remoteProfileName
              storageManager?.saveUserProfile(username, remoteProfileName)
            }
          }
        }
    } catch (_: Throwable) {
      isSyncingTasks = false
      isSyncingSchedule = false
      isSyncingFlashcards = false
      isSyncingSettings = false
    }
  }

  /**
   * Detaches all active Firestore snapshot listeners and resets syncing states.
   */
  fun stopFirestoreSync() {
    tasksSnapshotListener?.remove()
    tasksSnapshotListener = null
    scheduleSnapshotListener?.remove()
    scheduleSnapshotListener = null
    flashcardsSnapshotListener?.remove()
    flashcardsSnapshotListener = null
    settingsSnapshotListener?.remove()
    settingsSnapshotListener = null
    activeFirestoreUserId = null
    isSyncingTasks = false
    isSyncingSchedule = false
    isSyncingFlashcards = false
    isSyncingSettings = false
  }

  fun clearInitialSyncIndicators() {
    isSyncingTasks = false
    isSyncingSchedule = false
    isSyncingFlashcards = false
    isSyncingSettings = false
  }

  private fun syncSettingsToFirestore() {
    val userId = resolveActiveCloudUserId() ?: return
    try {
      val db = FirebaseFirestore.getInstance()
      val settingsPayload = hashMapOf(
        "isDarkMode" to isDarkMode,
        "darkMode" to isDarkMode,
        "profileName" to displayName,
        "displayName" to displayName,
        "username" to username,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection("users")
        .document(userId)
        .collection("settings")
        .document("preferences")
        .set(settingsPayload, SetOptions.merge())
    } catch (_: Throwable) {
    }
  }

  override fun onCleared() {
    stopFirestoreSync()
    super.onCleared()
  }

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

    val firebaseUser = try {
      FirebaseAuth.getInstance().currentUser
    } catch (_: Throwable) {
      null
    }

    val savedUserName = manager.loadUserName()
    if (firebaseUser != null && firebaseUser.isEmailVerified) {
      val resolvedUser = extractUsernameFromFirebaseUser(
        user = firebaseUser,
        fallbackEmail = savedUserName.orEmpty()
      ).ifBlank { "scholar@sibil.app" }
      val resolvedDisplay = firebaseUser.displayName?.trim().orEmpty().ifBlank {
        manager.getDisplayNameForUser(resolvedUser)
      }.ifBlank { resolvedUser.substringBefore("@").ifBlank { resolvedUser } }
      isGuestMode = false
      username = resolvedUser
      displayName = resolvedDisplay
      hasSavedSession = true
      manager.saveUserProfile(resolvedUser, resolvedDisplay)
      loadAccountDataForKey(manager, currentAccountKey)
      startFirestoreSyncForUser(firebaseUser.uid)
    } else if (manager.isAuthGuest()) {
      stopFirestoreSync()
      isGuestMode = true
      username = LocalStorageManager.GUEST_DISPLAY_NAME
      displayName = LocalStorageManager.GUEST_DISPLAY_NAME
      hasSavedSession = true
      loadAccountDataForKey(manager, LocalStorageManager.GUEST_ACCOUNT_KEY)
    } else {
      stopFirestoreSync()
      isGuestMode = false
      hasSavedSession = false
      loadFlashcardsForKey(manager, currentAccountKey)
    }
  }

  /**
   * Restores an active scholar session directly from [FirebaseAuth.getInstance().currentUser]
   * on app launch so logged-in users bypass the Login screen and open the Dashboard immediately.
   */
  fun restoreSessionFromFirebaseUser(firebaseUser: FirebaseUser) {
    val savedUserName = storageManager?.loadUserName().orEmpty()
    val resolvedUser = extractUsernameFromFirebaseUser(
      user = firebaseUser,
      fallbackEmail = savedUserName
    ).ifBlank { "scholar@sibil.app" }
    val resolvedDisplay = firebaseUser.displayName?.trim().orEmpty().ifBlank {
      storageManager?.getDisplayNameForUser(resolvedUser).orEmpty()
    }.ifBlank { resolvedUser.substringBefore("@").ifBlank { resolvedUser } }
    isGuestMode = false
    loginWithUsername(resolvedUser, resolvedDisplay)
    startFirestoreSyncForUser(firebaseUser.uid)
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
      val newSchedule = ClassSchedule(
        id = nextScheduleId++,
        subjectName = trimmedSubject,
        timeRange = trimmedTime,
        days = normalizedDays,
        room = trimmedRoom,
        colorHex = colorHex,
        startTime = parsedStart,
        endTime = parsedEnd
      )
      _classSchedules.add(newSchedule)
      val sorted = _classSchedules.sortedBy {
        parseTimeToFloat(it.startTime)
      }
      _classSchedules.clear()
      _classSchedules.addAll(sorted)
      storageManager?.saveSchedules(_classSchedules, currentAccountKey)
      resolveActiveCloudUserId()?.let { userId ->
        try {
          val db = FirebaseFirestore.getInstance()
          db.collection("users")
            .document(userId)
            .collection("schedule")
            .document(newSchedule.id.toString())
            .set(newSchedule.toFirestoreMap())
        } catch (_: Throwable) {
        }
      }
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
      val updatedSchedule = _classSchedules[index].copy(
        subjectName = trimmedSubject,
        timeRange = trimmedTime,
        days = normalizedDays,
        room = trimmedRoom,
        colorHex = colorHex,
        startTime = parsedStart,
        endTime = parsedEnd
      )
      _classSchedules[index] = updatedSchedule
      val sorted = _classSchedules.sortedBy {
        parseTimeToFloat(it.startTime)
      }
      _classSchedules.clear()
      _classSchedules.addAll(sorted)
      storageManager?.saveSchedules(_classSchedules, currentAccountKey)
      resolveActiveCloudUserId()?.let { userId ->
        try {
          val db = FirebaseFirestore.getInstance()
          db.collection("users")
            .document(userId)
            .collection("schedule")
            .document(updatedSchedule.id.toString())
            .set(updatedSchedule.toFirestoreMap())
        } catch (_: Throwable) {
        }
      }
    }
  }

  fun deleteClassSchedule(scheduleId: Int) {
    _classSchedules.removeAll { it.id == scheduleId }
    storageManager?.saveSchedules(_classSchedules, currentAccountKey)
    resolveActiveCloudUserId()?.let { userId ->
      try {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
          .document(userId)
          .collection("schedule")
          .document(scheduleId.toString())
          .delete()
      } catch (_: Throwable) {
      }
    }
  }

  /**
   * Fetches real-time weather, Heat Index (`apparent_temperature`), and Air Quality (`us_aqi`, `pm2_5`)
   * from Open-Meteo on `Dispatchers.IO`.
   *
   * Endpoints:
   * - Forecast: `https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code&timezone=auto`
   * - Air Quality: `https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$latitude&longitude=$longitude&current=us_aqi,pm2_5&timezone=auto`
   */
  suspend fun fetchWeather(
    latitude: Double = DEFAULT_FALLBACK_LATITUDE,
    longitude: Double = DEFAULT_FALLBACK_LONGITUDE
  ) {
    hasRequestedInitialWeather = true
    weatherText = "Fetching Weather..."
    weatherState = weatherState.copy(isLoading = true, isOffline = false)

    val fetchedState = withContext(Dispatchers.IO) {
      try {
        val forecastUrl =
          "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code&timezone=auto"
        val forecastConnection = (URL(forecastUrl).openConnection() as HttpURLConnection).apply {
          requestMethod = "GET"
          connectTimeout = 6_000
          readTimeout = 6_000
          setRequestProperty("User-Agent", "Sibil-App-Android")
        }
        val forecastRaw = try {
          BufferedReader(InputStreamReader(forecastConnection.inputStream, Charsets.UTF_8)).use { it.readText() }
        } finally {
          forecastConnection.disconnect()
        }

        val forecastRoot = JSONObject(forecastRaw)
        val currentObj = forecastRoot.optJSONObject("current")
        val temperature = currentObj?.optDouble("temperature_2m", 29.0)
          ?: forecastRoot.optJSONObject("current_weather")?.optDouble("temperature", 29.0)
          ?: 29.0
        val apparentTemp = currentObj?.optDouble("apparent_temperature", temperature) ?: temperature
        val humidity = currentObj?.optInt("relative_humidity_2m", 75) ?: 75
        val weatherCode = currentObj?.optInt("weather_code", -1)?.takeIf { it >= 0 }
          ?: forecastRoot.optJSONObject("current_weather")?.optInt("weathercode", 1)
          ?: 1

        val (conditionLabel, conditionEmoji) = mapWmoWeatherCodeToConditionAndEmoji(weatherCode)

        // Fetch Air Quality (US AQI & PM2.5) in best-effort mode so weather still renders if AQI endpoint is unreachable
        var usAqiValue = 42
        var pm25Value = 8.5
        try {
          val aqiUrl =
            "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$latitude&longitude=$longitude&current=us_aqi,pm2_5&timezone=auto"
          val aqiConnection = (URL(aqiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 5_000
            setRequestProperty("User-Agent", "Sibil-App-Android")
          }
          val aqiRaw = try {
            BufferedReader(InputStreamReader(aqiConnection.inputStream, Charsets.UTF_8)).use { it.readText() }
          } finally {
            aqiConnection.disconnect()
          }
          val aqiRoot = JSONObject(aqiRaw)
          val aqiCurrent = aqiRoot.optJSONObject("current")
          if (aqiCurrent != null) {
            usAqiValue = aqiCurrent.optDouble("us_aqi", 42.0).roundToInt().coerceAtLeast(0)
            pm25Value = aqiCurrent.optDouble("pm2_5", 8.5).coerceAtLeast(0.0)
          }
        } catch (_: Exception) {
          // Retain safe default AQI metrics if air-quality endpoint times out
        }

        val aqiRating = mapUsAqiToRating(usAqiValue)
        EnvironmentalWeatherState(
          isLoading = false,
          isOffline = false,
          conditionText = conditionLabel,
          conditionEmoji = conditionEmoji,
          weatherCode = weatherCode,
          temperatureCelsius = temperature.roundToInt(),
          heatIndexCelsius = apparentTemp.roundToInt(),
          relativeHumidity = humidity,
          usAqi = usAqiValue,
          pm25 = pm25Value,
          aqiRating = aqiRating
        )
      } catch (e: CancellationException) {
        throw e
      } catch (_: Exception) {
        null
      }
    }

    if (fetchedState != null) {
      weatherState = fetchedState
      weatherText = "${fetchedState.conditionText} • ${fetchedState.temperatureCelsius}°C"
    } else {
      weatherState = weatherState.copy(
        isLoading = false,
        isOffline = true,
        conditionText = "Weather Offline"
      )
      weatherText = "Weather Offline"
    }
  }

  private fun mapWmoWeatherCodeToConditionAndEmoji(weatherCode: Int): Pair<String, String> {
    return when (weatherCode) {
      0 -> "Clear Sky" to "☀️"
      1 -> "Mainly Clear" to "⛅"
      2, 3 -> "Partly Cloudy" to "⛅"
      45, 48 -> "Foggy" to "🌫️"
      in 51..57 -> "Drizzle" to "🌦️"
      in 61..67 -> "Rain" to "🌧️"
      in 71..77, 85, 86 -> "Snow" to "🌨️"
      in 80..82 -> "Rain Showers" to "🌧️"
      in 95..99 -> "Thunderstorm" to "⛈️"
      else -> "Partly Cloudy" to "⛅"
    }
  }

  private fun mapUsAqiToRating(usAqi: Int): String {
    return when {
      usAqi <= 50 -> "Good"
      usAqi <= 100 -> "Moderate"
      usAqi <= 150 -> "Sensitive"
      else -> "Unhealthy"
    }
  }

  fun updateDarkMode(enabled: Boolean) {
    isDarkMode = enabled
    storageManager?.saveDarkMode(enabled)
    syncSettingsToFirestore()
  }

  /**
   * Validates `typedUsername` and `typedPassword` against the `SharedPreferences` SHA-256 credentials.
   * - Checks that the username is not blank (`INVALID_USERNAME`).
   * - Returns `NO_ACCOUNT_FOUND` if the username is not registered.
   * - Returns `INCORRECT_PASSWORD` if `hashSecret(typedPassword) != storedPasswordHash`.
   * - Logs the user in (`auth_is_logged_in = true`) and returns `SUCCESS` when valid.
   */
  fun attemptLogin(typedUsername: String, typedPassword: String): LoginValidationResult {
    val cleanUsername = typedUsername.trim()
    if (cleanUsername.isBlank()) {
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
   * Verifies a user's 4-digit Recovery PIN (`hashSecret(enteredPin) == storedPinHash`).
   */
  fun verifyRecoveryPin(username: String, enteredPin: String): Boolean {
    return storageManager?.verifyRecoveryPin(username.trim(), enteredPin.trim()) == true
  }

  /**
   * Resets a user's password after verifying their 4-digit Recovery PIN without wiping flashcards or settings.
   */
  fun resetPasswordWithRecoveryPin(
    username: String,
    enteredPin: String,
    newPassword: String
  ): Boolean {
    return storageManager?.resetPasswordWithRecoveryPin(
      username = username.trim(),
      enteredPin = enteredPin.trim(),
      newPassword = newPassword
    ) == true
  }

  /**
   * Saves a newly registered account (`displayName`, `username`, SHA-256 `password`, SHA-256 `recoveryPin`)
   * to `SharedPreferences` and immediately logs the user in (`auth_is_logged_in = true`).
   */
  fun completeSignUp(
    usernameToRegister: String,
    password: String,
    displayNameToRegister: String = "",
    recoveryPinToRegister: String = ""
  ) {
    val cleanUsername = usernameToRegister.trim()
    val cleanDisplay = displayNameToRegister.trim().ifBlank { cleanUsername }
    storageManager?.registerUser(
      username = cleanUsername,
      password = password,
      displayName = cleanDisplay,
      recoveryPin = recoveryPinToRegister.trim()
    )
    loginWithUsername(cleanUsername, cleanDisplay)
  }

  fun loginWithUsername(typedUsername: String, explicitDisplayName: String = "") {
    val cleanUsername = typedUsername.trim()
    isGuestMode = false
    username = if (cleanUsername.isEmpty()) LocalStorageManager.GUEST_DISPLAY_NAME else cleanUsername
    val resolvedDisplay = explicitDisplayName.trim().ifBlank {
      storageManager?.getDisplayNameForUser(username) ?: username.substringBefore("@").ifBlank { username }
    }
    displayName = resolvedDisplay
    hasSavedSession = true
    storageManager?.let { manager ->
      loadAccountDataForKey(manager, currentAccountKey)
      manager.saveUserProfile(username, displayName)
    }
    resolveActiveCloudUserId()?.let { uid ->
      startFirestoreSyncForUser(uid)
      if (explicitDisplayName.isNotBlank()) {
        syncSettingsToFirestore()
      }
    }
  }

  fun loginAsGuest() {
    stopFirestoreSync()
    isGuestMode = true
    username = LocalStorageManager.GUEST_DISPLAY_NAME
    displayName = LocalStorageManager.GUEST_DISPLAY_NAME
    hasSavedSession = true
    storageManager?.let { manager ->
      loadAccountDataForKey(manager, LocalStorageManager.GUEST_ACCOUNT_KEY)
      manager.saveUserProfile(LocalStorageManager.GUEST_ACCOUNT_KEY)
    }
  }

  /**
   * Logs out the current user by calling `FirebaseAuth.getInstance().signOut()` and clearing
   * `isGuestMode`, `auth_is_logged_in`, and `auth_is_guest`, safely returning the user to the Login screen
   * without deleting their created decks or formula favorites.
   */
  fun logout() {
    stopFirestoreSync()
    try {
      FirebaseAuth.getInstance().signOut()
    } catch (_: Throwable) {
    }
    isGuestMode = false
    hasSavedSession = false
    storageManager?.clearUserProfile()
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
    resolveActiveCloudUserId()?.let { userId ->
      try {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
          .document(userId)
          .collection("flashcards")
          .document(newDeck.id.toString())
          .set(newDeck.toFirestoreMap())
      } catch (_: Throwable) {
      }
    }
    return newDeck
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
      val updatedDeck = deck.copy(
        cards = updatedCards,
        masteredCardIds = updatedCards.filter { it.isMastered }.map { it.id }
      )
      _flashcardDecks[index] = updatedDeck
      storageManager?.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
      resolveActiveCloudUserId()?.let { userId ->
        try {
          val db = FirebaseFirestore.getInstance()
          db.collection("users")
            .document(userId)
            .collection("flashcards")
            .document(updatedDeck.id.toString())
            .set(updatedDeck.toFirestoreMap())
        } catch (_: Throwable) {
        }
      }
    }
  }

  /**
   * Appends a formula or geometry property card directly into the dedicated "CE Formula Vault"
   * deck in Smart Flashcards (creating the deck automatically if it does not yet exist).
   */
  fun addCardToFormulaVaultDeck(
    question: String,
    answer: String,
    category: String = "MSTE"
  ): Boolean {
    val cleanQ = question.trim()
    val cleanA = answer.trim()
    if (cleanQ.isEmpty() || cleanA.isEmpty()) return false
    val vaultDeckTitle = "CE Formula Vault"
    val existingIndex = _flashcardDecks.indexOfFirst {
      it.title.equals(vaultDeckTitle, ignoreCase = true)
    }
    if (existingIndex != -1) {
      val existingDeck = _flashcardDecks[existingIndex]
      val alreadyExists = existingDeck.cards.any {
        it.question.equals(cleanQ, ignoreCase = true)
      }
      if (!alreadyExists) {
        addCardToDeck(
          deckId = existingDeck.id,
          question = cleanQ,
          answer = cleanA,
          category = category
        )
      }
    } else {
      addFlashcardDeck(
        title = vaultDeckTitle,
        subjectTag = "MSTE",
        initialCards = listOf(
          FlashcardItem(
            id = nextCardId++,
            question = cleanQ,
            answer = cleanA,
            category = category,
            masteryLevel = "New",
            isMastered = false
          )
        )
      )
    }
    return true
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
          val updatedIsMastered = isRatedGood
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
      val updatedDeck = currentDeck.copy(
        cards = updatedCards,
        masteredCardIds = updatedCards.filter { it.isMastered }.map { it.id }
      )
      _flashcardDecks[deckIndex] = updatedDeck
      storageManager?.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
      resolveActiveCloudUserId()?.let { userId ->
        try {
          val db = FirebaseFirestore.getInstance()
          db.collection("users")
            .document(userId)
            .collection("flashcards")
            .document(updatedDeck.id.toString())
            .set(updatedDeck.toFirestoreMap())
        } catch (_: Throwable) {
        }
      }
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
      val updatedDeck = currentDeck.copy(
        cards = resetCards,
        masteredCardIds = emptyList()
      )
      _flashcardDecks[deckIndex] = updatedDeck
      storageManager?.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
      resolveActiveCloudUserId()?.let { userId ->
        try {
          val db = FirebaseFirestore.getInstance()
          db.collection("users")
            .document(userId)
            .collection("flashcards")
            .document(updatedDeck.id.toString())
            .set(updatedDeck.toFirestoreMap())
        } catch (_: Throwable) {
        }
      }
    }
  }

  fun deleteFlashcardDeck(deckId: Int) {
    _flashcardDecks.removeAll { it.id == deckId }
    storageManager?.saveFlashcardDecks(_flashcardDecks, currentAccountKey)
    resolveActiveCloudUserId()?.let { userId ->
      try {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
          .document(userId)
          .collection("flashcards")
          .document(deckId.toString())
          .delete()
      } catch (_: Throwable) {
      }
    }
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
      val newTask = Task(
        id = nextId++,
        title = trimmedTitle,
        dueDate = trimmedDueDate,
        isCompleted = false,
        subjectCategory = trimmedCategory
      )
      _tasks.add(0, newTask)
      storageManager?.saveTasks(_tasks, currentAccountKey)
      resolveActiveCloudUserId()?.let { userId ->
        try {
          val db = FirebaseFirestore.getInstance()
          db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(newTask.id.toString())
            .set(newTask.toFirestoreMap())
        } catch (_: Throwable) {
        }
      }
    }
  }

  fun toggleTaskCompletion(taskId: Int) {
    val index = _tasks.indexOfFirst { it.id == taskId }
    if (index != -1) {
      val current = _tasks[index]
      val updatedTask = current.copy(isCompleted = !current.isCompleted)
      _tasks[index] = updatedTask
      storageManager?.saveTasks(_tasks, currentAccountKey)
      resolveActiveCloudUserId()?.let { userId ->
        try {
          val db = FirebaseFirestore.getInstance()
          db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(updatedTask.id.toString())
            .set(updatedTask.toFirestoreMap())
        } catch (_: Throwable) {
        }
      }
    }
  }

  fun deleteTask(taskId: Int) {
    _tasks.removeAll { it.id == taskId }
    storageManager?.saveAlarms(_alarms, currentAccountKey)
    storageManager?.saveTasks(_tasks, currentAccountKey)
    resolveActiveCloudUserId()?.let { userId ->
      try {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
          .document(userId)
          .collection("tasks")
          .document(taskId.toString())
          .delete()
      } catch (_: Throwable) {
      }
    }
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
      // Cross-Device Persistence & Auto-Login:
      // 1. On app launch, check val user = FirebaseAuth.getInstance().currentUser.
      // 2. If user != null (and user.isEmailVerified == true) OR isGuestMode == true: navigate straight to the Dashboard (bypassing Auth screen).
      // 3. If user == null (or unverified) AND !isGuestMode: show the Liquid Glass Auth Screen.
      val initialFirebaseUser = remember {
        try {
          FirebaseAuth.getInstance().currentUser?.takeIf { it.isEmailVerified }
        } catch (_: Throwable) {
          getFirebaseAuthOrNull(context)?.currentUser?.takeIf { it.isEmailVerified }
        }
      }
      var isGuestMode by rememberSaveable {
        mutableStateOf(resolvedViewModel.isGuestMode)
      }
      var isLoggedIn by rememberSaveable {
        mutableStateOf(initialFirebaseUser != null || isGuestMode)
      }

      LaunchedEffect(Unit) {
        val user = try {
          FirebaseAuth.getInstance().currentUser
        } catch (_: Throwable) {
          getFirebaseAuthOrNull(context)?.currentUser
        }
        if (user != null && user.isEmailVerified) {
          isGuestMode = false
          resolvedViewModel.restoreSessionFromFirebaseUser(user)
          isLoggedIn = true
        } else if (user != null && !user.isEmailVerified) {
          try {
            FirebaseAuth.getInstance().signOut()
          } catch (_: Throwable) {
          }
          resolvedViewModel.stopFirestoreSync()
          isLoggedIn = false
        } else if (resolvedViewModel.isGuestMode || isGuestMode) {
          resolvedViewModel.stopFirestoreSync()
          isGuestMode = true
          isLoggedIn = true
        } else {
          resolvedViewModel.stopFirestoreSync()
          isLoggedIn = false
        }
      }

      // Connect real-time Firestore snapshot listeners whenever an authenticated user is logged in,
      // or fall back to local Offline Scholar state when FirebaseAuth.getInstance().currentUser == null.
      DisposableEffect(isLoggedIn, isGuestMode, resolvedViewModel.username) {
        val activeUser = try {
          FirebaseAuth.getInstance().currentUser?.takeIf { it.isEmailVerified }
        } catch (_: Throwable) {
          getFirebaseAuthOrNull(context)?.currentUser?.takeIf { it.isEmailVerified }
        }
        if (isLoggedIn && !isGuestMode && activeUser != null) {
          resolvedViewModel.startFirestoreSyncForUser(activeUser.uid)
        } else if (isGuestMode || activeUser == null) {
          resolvedViewModel.stopFirestoreSync()
        }
        onDispose { }
      }

      // Safety timeout so loading indicators never remain stuck if offline while logged in
      LaunchedEffect(resolvedViewModel.isFirestoreSyncing) {
        if (resolvedViewModel.isFirestoreSyncing) {
          delay(4_500L)
          resolvedViewModel.clearInitialSyncIndicators()
        }
      }

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
                isGuestMode = false
                isLoggedIn = true
              }
              result
            },
            onFirebaseLoginSuccess = { loggedInEmail, firebaseDisplayName, enteredPassword ->
              isGuestMode = false
              if (
                enteredPassword.isNotBlank() &&
                !resolvedViewModel.isUsernameRegistered(loggedInEmail)
              ) {
                resolvedViewModel.completeSignUp(
                  usernameToRegister = loggedInEmail,
                  password = enteredPassword,
                  displayNameToRegister = firebaseDisplayName,
                  recoveryPinToRegister = ""
                )
              } else {
                resolvedViewModel.loginWithUsername(loggedInEmail, firebaseDisplayName)
              }
              isLoggedIn = true
            },
            onCheckUsernameRegistered = { emailToCheck ->
              resolvedViewModel.isUsernameRegistered(emailToCheck)
            },
            onCompleteSignUp = { registeredEmail, createdPassword, displayName, recoveryPin ->
              isGuestMode = false
              resolvedViewModel.completeSignUp(
                usernameToRegister = registeredEmail,
                password = createdPassword,
                displayNameToRegister = displayName,
                recoveryPinToRegister = recoveryPin
              )
              isLoggedIn = true
            },
            onContinueAsGuest = {
              isGuestMode = true
              resolvedViewModel.loginAsGuest()
              isLoggedIn = true
            }
          )
        } else {
          MainProductivityScaffold(
            taskViewModel = resolvedViewModel,
            onLogout = {
              try {
                FirebaseAuth.getInstance().signOut()
              } catch (_: Throwable) {
              }
              isGuestMode = false
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
 * Refined Liquid Glass Login & Sign-Up Screen with Real Email & Password Firebase Authentication:
 * - Centered responsive card (`Modifier.fillMaxWidth().widthIn(max = 440.dp).padding(20.dp)`).
 * - Frosted glass card with specular gradient border and subtle depth shadow.
 * - Animated segmented pill slider at the top of the card switching between `"Log In"` and `"Sign Up"`.
 * - Clean rounded glass input containers (`14.dp` corner radius) with glowing outline when active.
 * - Cloud Registration (Sign Up):
 *   1. Full Name / Nickname (`Icons.Default.Badge`)
 *   2. Email Address (`Icons.Default.Email`, `KeyboardType.Email`, `KeyboardCapitalization.None`)
 *   3. Password (`Icons.Default.Lock`, min 6 characters, eye toggle)
 * - Cloud Login (Log In):
 *   1. Email Address (`Icons.Default.Email`, `KeyboardType.Email`, `KeyboardCapitalization.None`)
 *   2. Password (`Icons.Default.Lock`, eye toggle)
 * - Submit Button (`"Sign In"` or `"Create Account"`) disabled while `isLoading` is true with a circular progress spinner.
 * - `"Continue as Offline Scholar →"` guest bypass at the bottom.
 */
@Composable
fun StartupLoginScreen(
  onAttemptLogin: (String, String) -> LoginValidationResult = { _, _ -> LoginValidationResult.NO_ACCOUNT_FOUND },
  onFirebaseLoginSuccess: (email: String, displayName: String, password: String) -> Unit = { _, _, _ -> },
  onCheckUsernameRegistered: (String) -> Boolean = { false },
  onCompleteSignUp: (email: String, password: String, displayName: String, recoveryPin: String) -> Unit = { _, _, _, _ -> },
  onVerifyRecoveryPin: (username: String, pin: String) -> Boolean = { _, _ -> false },
  onResetPasswordWithPin: (username: String, pin: String, newPassword: String) -> Boolean = { _, _, _ -> false },
  onContinueAsGuest: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val palette = currentGlassPalette()
  val metrics = rememberResponsiveMetrics()

  // Mode toggle: false = "Log In", true = "Sign Up"
  var isSignUpMode by rememberSaveable { mutableStateOf(false) }
  var fullName by rememberSaveable { mutableStateOf("") }
  var email by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var loginErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
  var loginSuccessMessage by rememberSaveable { mutableStateOf<String?>(null) }
  var showResendVerification by rememberSaveable { mutableStateOf(false) }

  // Focus states for elevated glass input capsules
  var isFullNameFocused by remember { mutableStateOf(false) }
  var isEmailFocused by remember { mutableStateOf(false) }
  var isPasswordFocused by remember { mutableStateOf(false) }

  // Primary button press-scale animation
  val primaryButtonInteraction = remember { MutableInteractionSource() }
  val isPrimaryPressed by primaryButtonInteraction.collectIsPressedAsState()
  val pressScale by animateFloatAsState(
    targetValue = if (isPrimaryPressed && !isLoading) 0.96f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "loginPrimaryPressScale"
  )

  fun handleResendVerificationEmail() {
    val trimmedEmail = email.trim()
    if (trimmedEmail.isBlank() || password.isEmpty()) {
      Toast.makeText(
        context,
        "Please enter your email and password to resend verification.",
        Toast.LENGTH_SHORT
      ).show()
      return
    }
    try {
      if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
      }
      ensureFirebaseInitialized(context)
      FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(trimmedEmail, password)
        .addOnSuccessListener { it ->
          val user = it.user
          user?.sendEmailVerification()
          FirebaseAuth.getInstance().signOut()
          Toast.makeText(context, "Verification email sent!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
          Toast.makeText(
            context,
            mapFirebaseAuthExceptionToMessage(e, isSignUp = false),
            Toast.LENGTH_SHORT
          ).show()
        }
    } catch (e: Throwable) {
      Toast.makeText(
        context,
        mapFirebaseAuthExceptionToMessage(e, isSignUp = false),
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  fun handlePrimaryAuthAction() {
    if (isLoading) return
    val trimmedEmail = email.trim()

    if (!isSignUpMode) {
      if (trimmedEmail.isBlank() || password.isEmpty()) {
        loginSuccessMessage = null
        showResendVerification = false
        loginErrorMessage = "Invalid email or password. Please check your credentials."
        return
      }

      isLoading = true
      loginErrorMessage = null
      loginSuccessMessage = null
      showResendVerification = false
      try {
        if (FirebaseApp.getApps(context).isEmpty()) {
          FirebaseApp.initializeApp(context)
        }
        ensureFirebaseInitialized(context)
        FirebaseAuth.getInstance()
          .signInWithEmailAndPassword(trimmedEmail, password)
          .addOnSuccessListener { it ->
            isLoading = false
            val isVerified = it.user?.isEmailVerified == true
            if (isVerified) {
              val firebaseUser = it.user ?: FirebaseAuth.getInstance().currentUser
              val resolvedEmail = firebaseUser?.email?.trim().orEmpty().ifBlank { trimmedEmail }
              val resolvedDisplayName = firebaseUser?.displayName?.trim().orEmpty()
                .ifBlank { fullName.trim() }
                .ifBlank { resolvedEmail.substringBefore("@").ifBlank { resolvedEmail } }
              loginErrorMessage = null
              loginSuccessMessage = null
              showResendVerification = false
              onFirebaseLoginSuccess(resolvedEmail, resolvedDisplayName, password)
            } else {
              FirebaseAuth.getInstance().signOut()
              loginSuccessMessage = null
              showResendVerification = true
              loginErrorMessage =
                "Please verify your email address before logging in. Check your inbox or spam folder."
            }
          }
          .addOnFailureListener { e ->
            isLoading = false
            showResendVerification = false
            loginErrorMessage = mapFirebaseAuthExceptionToMessage(e, isSignUp = false)
          }
      } catch (e: Throwable) {
        isLoading = false
        showResendVerification = false
        loginErrorMessage = mapFirebaseAuthExceptionToMessage(e, isSignUp = false)
      }
    } else {
      val trimmedFullName = fullName.trim()
      if (trimmedFullName.isBlank()) {
        loginSuccessMessage = null
        loginErrorMessage = "Registration failed: Please enter your Full Name / Nickname."
        return
      }
      if (trimmedEmail.isBlank()) {
        loginSuccessMessage = null
        loginErrorMessage = "Please enter a valid email address."
        return
      }
      if (password.length < 6) {
        loginSuccessMessage = null
        loginErrorMessage = "Password must be at least 6 characters."
        return
      }

      isLoading = true
      loginErrorMessage = null
      loginSuccessMessage = null
      showResendVerification = false
      try {
        if (FirebaseApp.getApps(context).isEmpty()) {
          FirebaseApp.initializeApp(context)
        }
        ensureFirebaseInitialized(context)
        FirebaseAuth.getInstance()
          .createUserWithEmailAndPassword(trimmedEmail, password)
          .addOnSuccessListener { it ->
            val profileUpdate = UserProfileChangeRequest.Builder()
              .setDisplayName(trimmedFullName)
              .build()
            it.user?.updateProfile(profileUpdate)
            it.user?.sendEmailVerification()
            FirebaseAuth.getInstance().signOut()
            isLoading = false
            loginErrorMessage = null
            showResendVerification = false
            loginSuccessMessage =
              "Account created! Please check your email to verify your account before logging in."
            isSignUpMode = false
          }
          .addOnFailureListener { e ->
            isLoading = false
            loginSuccessMessage = null
            loginErrorMessage = when (e) {
              is FirebaseAuthUserCollisionException -> "An account with this email already exists."
              is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters."
              is FirebaseAuthInvalidCredentialsException -> "Please enter a valid email address."
              else -> "Registration failed: ${e.localizedMessage}"
            }
          }
      } catch (e: Throwable) {
        isLoading = false
        loginSuccessMessage = null
        loginErrorMessage = when (e) {
          is FirebaseAuthUserCollisionException -> "An account with this email already exists."
          is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters."
          is FirebaseAuthInvalidCredentialsException -> "Please enter a valid email address."
          else -> "Registration failed: ${e.localizedMessage}"
        }
      }
    }
  }

  val backdropColors = if (palette.isDark) {
    listOf(
      Color(0xFF0B1120),
      Color(0xFF0F172A),
      Color(0xFF1E1B4B)
    )
  } else {
    palette.canvasColors
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Brush.verticalGradient(backdropColors))
      .testTag("startup_login_screen"),
    contentAlignment = Alignment.Center
  ) {
    // Ambient radial mesh background with subtle indigo, violet, and dark slate tones
    // to provide refraction depth behind the frosted glass container.
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0xFF6366F1).copy(alpha = if (palette.isDark) 0.36f else 0.25f),
            Color(0xFF6366F1).copy(alpha = 0.08f),
            Color.Transparent
          ),
          center = Offset(w * 0.16f, h * 0.18f),
          radius = w * 0.75f
        ),
        radius = w * 0.75f,
        center = Offset(w * 0.16f, h * 0.18f)
      )

      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0xFF8B5CF6).copy(alpha = if (palette.isDark) 0.32f else 0.22f),
            Color(0xFF7C3AED).copy(alpha = 0.08f),
            Color.Transparent
          ),
          center = Offset(w * 0.88f, h * 0.44f),
          radius = w * 0.70f
        ),
        radius = w * 0.70f,
        center = Offset(w * 0.88f, h * 0.44f)
      )

      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0xFF334155).copy(alpha = if (palette.isDark) 0.42f else 0.16f),
            Color(0xFF06B6D4).copy(alpha = if (palette.isDark) 0.18f else 0.14f),
            Color.Transparent
          ),
          center = Offset(w * 0.26f, h * 0.84f),
          radius = w * 0.76f
        ),
        radius = w * 0.76f,
        center = Offset(w * 0.26f, h * 0.84f)
      )
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 440.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Main Glass Authentication Card:
      // Centered frosted glass card (28.dp corner radius) with specular gradient border and subtle depth shadow.
      val cardShape = RoundedCornerShape(28.dp)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(
            elevation = 24.dp,
            shape = cardShape,
            ambientColor = Color(0xFF6366F1).copy(alpha = if (palette.isDark) 0.42f else 0.22f),
            spotColor = Color(0xFF8B5CF6).copy(alpha = if (palette.isDark) 0.42f else 0.24f)
          )
          .clip(cardShape)
          .background(palette.cardSurface.copy(alpha = if (palette.isDark) 0.88f else 0.86f))
          .border(
            width = 1.dp,
            brush = Brush.linearGradient(
              colors = listOf(
                Color.White.copy(alpha = 0.45f),
                Color.White.copy(alpha = 0.08f)
              )
            ),
            shape = cardShape
          )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(
              horizontal = metrics.scaledDp(22),
              vertical = metrics.scaledDp(24)
            ),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
        ) {
          // App Header:
          // Sibil brand icon/monogram inside a glowing glass pill + Title "Sibil" (24.sp Bold) + Subtitle (12.sp)
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Box(
              modifier = Modifier
                .shadow(
                  elevation = 12.dp,
                  shape = CircleShape,
                  ambientColor = Color(0xFF6366F1).copy(alpha = 0.45f),
                  spotColor = Color(0xFF8B5CF6).copy(alpha = 0.45f)
                )
                .clip(CircleShape)
                .background(
                  brush = Brush.horizontalGradient(
                    colors = listOf(
                      Color(0xFF6366F1).copy(alpha = 0.22f),
                      Color(0xFF8B5CF6).copy(alpha = 0.22f)
                    )
                  )
                )
                .border(
                  width = 1.dp,
                  brush = Brush.linearGradient(
                    listOf(
                      Color.White.copy(alpha = 0.55f),
                      Color(0xFF6366F1).copy(alpha = 0.35f)
                    )
                  ),
                  shape = CircleShape
                )
                .padding(horizontal = 16.dp, vertical = 9.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                      Brush.linearGradient(
                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                      )
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Filled.Architecture,
                    contentDescription = "Sibil Monogram",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                }
                Text(
                  text = "SIBIL CE",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                  ),
                  color = palette.accentIndigo
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Sibil",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold
              ),
              color = palette.textPrimary,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("login_app_title")
            )

            Text(
              text = "Civil Engineering Board Examination Companion",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
              ),
              color = palette.textSecondary.copy(alpha = 0.85f),
              textAlign = TextAlign.Center,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("login_app_slogan")
            )
          }

          // Animated Mode Switcher:
          // Segmented pill slider at top of the card switching between "Log In" and "Sign Up"
          BoxWithConstraints(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .clip(CircleShape)
              .background(palette.nestedCardFill.copy(alpha = if (palette.isDark) 0.72f else 0.65f))
              .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                  listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.08f)
                  )
                ),
                shape = CircleShape
              )
              .padding(4.dp)
          ) {
            val segmentWidth = maxWidth / 2f
            val indicatorOffset by animateDpAsState(
              targetValue = if (isSignUpMode) segmentWidth else 0.dp,
              animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
              ),
              label = "authSegmentSlideOffset"
            )

            Box(
              modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(
                  elevation = 6.dp,
                  shape = CircleShape,
                  ambientColor = Color(0xFF6366F1).copy(alpha = 0.35f),
                  spotColor = Color(0xFF8B5CF6).copy(alpha = 0.35f)
                )
                .clip(CircleShape)
                .background(
                  brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                  )
                )
                .border(
                  width = 1.dp,
                  color = Color.White.copy(alpha = 0.45f),
                  shape = CircleShape
                )
            )

            Row(modifier = Modifier.fillMaxSize()) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .clip(CircleShape)
                  .clickable(enabled = !isLoading) {
                    isSignUpMode = false
                    loginErrorMessage = null
                  }
                  .testTag("auth_mode_login_tab"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Log In",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = if (!isSignUpMode) Color.White else palette.textSecondary
                )
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .clip(CircleShape)
                  .clickable(enabled = !isLoading) {
                    isSignUpMode = true
                    loginErrorMessage = null
                    loginSuccessMessage = null
                    showResendVerification = false
                  }
                  .testTag("sign_up_button"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Sign Up",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = if (isSignUpMode) Color.White else palette.textSecondary
                )
              }
            }
          }

          // Prominent Frosted Glass Success Message (e.g. after Sign Up email verification dispatch)
          loginSuccessMessage?.let { successText ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFF10B981).copy(alpha = 0.16f),
              border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.50f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = successText,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = if (palette.isDark) Color(0xFF6EE7B7) else Color(0xFF047857),
                textAlign = TextAlign.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp)
                  .testTag("login_success_text")
              )
            }
          }

          // Sign Up Field 1: Full Name / Nickname (Leading Icon: Icons.Default.Badge)
          AnimatedVisibility(
            visible = isSignUpMode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
          ) {
            GlassElevatedInputCapsule(
              value = fullName,
              onValueChange = {
                fullName = it
                if (loginErrorMessage != null) loginErrorMessage = null
              },
              label = "Full Name / Nickname",
              placeholder = "Engr. Juan Dela Cruz",
              leadingIcon = Icons.Default.Badge,
              isFocused = isFullNameFocused,
              onFocusChange = { isFullNameFocused = it },
              keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
              ),
              metrics = metrics,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_full_name_input")
            )
          }

          // Email Address Field (Icons.Default.Email, KeyboardType.Email, KeyboardCapitalization.None)
          GlassElevatedInputCapsule(
            value = email,
            onValueChange = {
              email = it
              if (loginErrorMessage != null) loginErrorMessage = null
            },
            label = "Email Address",
            placeholder = "scholar@university.edu",
            leadingIcon = Icons.Default.Email,
            isFocused = isEmailFocused,
            onFocusChange = { isEmailFocused = it },
            keyboardOptions = KeyboardOptions(
              capitalization = KeyboardCapitalization.None,
              autoCorrectEnabled = false,
              keyboardType = KeyboardType.Email,
              imeAction = ImeAction.Next
            ),
            metrics = metrics,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_username_input")
          )

          // Password Field (Icons.Default.Lock, min 6 characters on Sign Up, eye toggle)
          GlassElevatedInputCapsule(
            value = password,
            onValueChange = {
              password = it
              if (loginErrorMessage != null) loginErrorMessage = null
            },
            label = "Password",
            placeholder = if (isSignUpMode) "At least 6 characters" else "Enter your password",
            leadingIcon = Icons.Default.Lock,
            isFocused = isPasswordFocused,
            onFocusChange = { isPasswordFocused = it },
            isPassword = true,
            isPasswordVisible = isPasswordVisible,
            onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
            keyboardOptions = KeyboardOptions(
              capitalization = KeyboardCapitalization.None,
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = { handlePrimaryAuthAction() }
            ),
            metrics = metrics,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_password_input")
          )

          // Red Frosted Glass Error Badge
          loginErrorMessage?.let { errorText ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFFEF4444).copy(alpha = 0.14f),
              border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = errorText,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = if (palette.isDark) Color(0xFFFCA5A5) else Color(0xFFD32F2F),
                textAlign = TextAlign.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 9.dp)
                  .testTag("login_error_text")
              )
            }
          }

          Spacer(modifier = Modifier.height(2.dp))

          // Primary Action Button:
          // While isLoading is true, disable the submit button and show a circular progress spinner inside the button.
          // Button label: "Sign In" or "Create Account".
          val buttonBrush = if (isLoading) {
            Brush.horizontalGradient(
              colors = listOf(
                Color(0xFF6366F1).copy(alpha = 0.65f),
                Color(0xFF8B5CF6).copy(alpha = 0.65f)
              )
            )
          } else {
            Brush.horizontalGradient(
              colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(metrics.scaledDp(54))
              .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
              }
              .shadow(
                elevation = if (isLoading) 4.dp else 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF6366F1).copy(alpha = 0.45f),
                spotColor = Color(0xFF8B5CF6).copy(alpha = 0.45f)
              )
              .clip(RoundedCornerShape(16.dp))
              .background(brush = buttonBrush)
              .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                  listOf(
                    Color.White.copy(alpha = 0.50f),
                    Color.White.copy(alpha = 0.12f)
                  )
                ),
                shape = RoundedCornerShape(16.dp)
              )
              .clickable(
                enabled = !isLoading,
                interactionSource = primaryButtonInteraction,
                indication = null,
                onClick = { handlePrimaryAuthAction() }
              )
              .testTag("login_submit_button"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              if (isLoading) {
                CircularProgressIndicator(
                  color = Color.White,
                  strokeWidth = 2.2.dp,
                  modifier = Modifier.size(18.dp)
                )
              }
              Text(
                text = if (isSignUpMode) "Create Account" else "Sign In",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontSize = 15.5.sp,
                  fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              if (!isLoading) {
                Icon(
                  imageVector = Icons.Filled.ArrowForward,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          // Resend Verification Button (shown when login fails the isEmailVerified check)
          if (showResendVerification && !isSignUpMode) {
            TextButton(
              onClick = { handleResendVerificationEmail() },
              enabled = !isLoading,
              shape = CircleShape,
              modifier = Modifier
                .fillMaxWidth()
                .height(metrics.scaledDp(42))
                .testTag("resend_verification_button")
            ) {
              Text(
                text = "Didn't get the email? Resend.",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = palette.accentIndigo,
                textAlign = TextAlign.Center
              )
            }
          }

          // Offline Scholar / Guest Bypass:
          // Text button at bottom: "Continue as Offline Scholar →"
          // Allows offline students to access study tools immediately without signing up.
          TextButton(
            onClick = {
              if (!isLoading) {
                loginErrorMessage = null
                loginSuccessMessage = null
                showResendVerification = false
                onContinueAsGuest()
              }
            },
            enabled = !isLoading,
            shape = CircleShape,
            modifier = Modifier
              .fillMaxWidth()
              .height(metrics.scaledDp(46))
              .testTag("continue_as_guest_button")
          ) {
            Text(
              text = "Continue as Offline Scholar →",
              style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

/**
 * Local Password Recovery Modal triggered by `"Forgot Password?"` on the Login tab:
 * - Prompts for Username and 4-Digit Recovery PIN.
 * - Verifies `hashSecret(enteredPin) == storedPinHash`.
 * - Allows entering and confirming a new password, updating `auth_password_hash` without wiping flashcards or settings.
 */
@Composable
private fun ForgotPasswordRecoveryModal(
  initialUsername: String,
  onCheckUsernameRegistered: (String) -> Boolean,
  onVerifyRecoveryPin: (String, String) -> Boolean,
  onResetPasswordWithPin: (String, String, String) -> Boolean,
  onDismiss: () -> Unit,
  onPasswordResetSuccess: (String) -> Unit
) {
  val palette = currentGlassPalette()
  val metrics = rememberResponsiveMetrics()

  var recoveryUsername by rememberSaveable { mutableStateOf(initialUsername.trim()) }
  var recoveryPin by rememberSaveable { mutableStateOf("") }
  var newPassword by rememberSaveable { mutableStateOf("") }
  var confirmPassword by rememberSaveable { mutableStateOf("") }
  var isPinVerified by rememberSaveable { mutableStateOf(false) }
  var isPinVisible by rememberSaveable { mutableStateOf(false) }
  var isNewPasswordVisible by rememberSaveable { mutableStateOf(false) }
  var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
  var modalError by rememberSaveable { mutableStateOf<String?>(null) }

  var isUserFocused by remember { mutableStateOf(false) }
  var isPinFocused by remember { mutableStateOf(false) }
  var isNewPassFocused by remember { mutableStateOf(false) }
  var isConfirmPassFocused by remember { mutableStateOf(false) }

  val cardShape = RoundedCornerShape(28.dp)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = if (metrics.screenWidthDp > 600) 480.dp else 420.dp)
        .shadow(
          elevation = 24.dp,
          shape = cardShape,
          ambientColor = Color(0xFF6366F1).copy(alpha = 0.40f),
          spotColor = Color(0xFF8B5CF6).copy(alpha = 0.40f)
        )
        .clip(cardShape)
        .background(palette.cardSurface.copy(alpha = 0.96f))
        .border(
          width = 1.dp,
          brush = Brush.linearGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.45f),
              Color.White.copy(alpha = 0.08f)
            )
          ),
          shape = cardShape
        )
        .testTag("forgot_password_recovery_modal")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 22.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Text(
              text = "Reset Scholar Password",
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
              ),
              color = palette.textPrimary
            )
            Text(
              text = "4-DIGIT RECOVERY PIN VERIFICATION",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp
              ),
              color = palette.accentIndigo
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(palette.nestedCardFill)
              .testTag("close_forgot_password_modal_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Recovery Modal",
              tint = palette.textPrimary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        HorizontalDivider(color = palette.dividerColor)

        // Username / Student ID
        GlassElevatedInputCapsule(
          value = recoveryUsername,
          onValueChange = {
            recoveryUsername = it
            isPinVerified = false
            modalError = null
          },
          label = "Username / Student ID",
          placeholder = "Enter your registered username",
          leadingIcon = Icons.Default.Person,
          isFocused = isUserFocused,
          onFocusChange = { isUserFocused = it },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
          ),
          metrics = metrics,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("recovery_username_input")
        )

        // 4-Digit Recovery PIN
        GlassElevatedInputCapsule(
          value = recoveryPin,
          onValueChange = { raw ->
            recoveryPin = raw.filter { it.isDigit() }.take(4)
            isPinVerified = false
            modalError = null
          },
          label = "4-Digit Recovery PIN",
          placeholder = "Enter your 4-digit PIN",
          leadingIcon = Icons.Default.Key,
          isFocused = isPinFocused,
          onFocusChange = { isPinFocused = it },
          isPassword = true,
          isPasswordVisible = isPinVisible,
          onTogglePasswordVisibility = { isPinVisible = !isPinVisible },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = if (isPinVerified) ImeAction.Next else ImeAction.Done
          ),
          metrics = metrics,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("recovery_pin_input")
        )

        AnimatedVisibility(
          visible = isPinVerified,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            GlassElevatedInputCapsule(
              value = newPassword,
              onValueChange = {
                newPassword = it
                modalError = null
              },
              label = "New Password",
              placeholder = "Enter new password",
              leadingIcon = Icons.Default.Lock,
              isFocused = isNewPassFocused,
              onFocusChange = { isNewPassFocused = it },
              isPassword = true,
              isPasswordVisible = isNewPasswordVisible,
              onTogglePasswordVisibility = { isNewPasswordVisible = !isNewPasswordVisible },
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
              ),
              metrics = metrics,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("recovery_new_password_input")
            )

            GlassElevatedInputCapsule(
              value = confirmPassword,
              onValueChange = {
                confirmPassword = it
                modalError = null
              },
              label = "Confirm New Password",
              placeholder = "Re-enter new password",
              leadingIcon = Icons.Default.Lock,
              isFocused = isConfirmPassFocused,
              onFocusChange = { isConfirmPassFocused = it },
              isPassword = true,
              isPasswordVisible = isConfirmPasswordVisible,
              onTogglePasswordVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
              ),
              metrics = metrics,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("recovery_confirm_password_input")
            )
          }
        }

        modalError?.let { err ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEF4444).copy(alpha = 0.14f),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = err,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
              ),
              color = if (palette.isDark) Color(0xFFFCA5A5) else Color(0xFFD32F2F),
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("recovery_error_text")
            )
          }
        }

        // Action Button: Verify PIN -> Update Password
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(
              elevation = 10.dp,
              shape = RoundedCornerShape(14.dp),
              ambientColor = Color(0xFF6366F1).copy(alpha = 0.4f),
              spotColor = Color(0xFF8B5CF6).copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
              Brush.horizontalGradient(
                listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
              )
            )
            .clickable {
              val cleanUser = recoveryUsername.trim()
              val cleanPin = recoveryPin.trim()
              if (cleanUser.isBlank()) {
                modalError = "Please enter your Username / Student ID"
                return@clickable
              }
              if (!onCheckUsernameRegistered(cleanUser)) {
                modalError = "No scholar account found for that username"
                return@clickable
              }
              if (cleanPin.length != 4) {
                modalError = "Please enter your 4-digit Recovery PIN"
                return@clickable
              }
              if (!onVerifyRecoveryPin(cleanUser, cleanPin)) {
                modalError = "Invalid 4-Digit Recovery PIN"
                return@clickable
              }
              if (!isPinVerified) {
                isPinVerified = true
                modalError = null
              } else {
                if (newPassword.isBlank()) {
                  modalError = "Please enter a new password"
                  return@clickable
                }
                if (newPassword != confirmPassword) {
                  modalError = "Passwords do not match"
                  return@clickable
                }
                val updated = onResetPasswordWithPin(cleanUser, cleanPin, newPassword)
                if (updated) {
                  modalError = null
                  onPasswordResetSuccess(cleanUser)
                } else {
                  modalError = "Unable to update password. Please verify your PIN."
                }
              }
            }
            .testTag("recovery_submit_button"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isPinVerified) "Update Password" else "Verify Recovery PIN",
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = 15.sp,
              fontWeight = FontWeight.ExtraBold
            ),
            color = Color.White
          )
        }
      }
    }
  }
}

@Composable
private fun GlassElevatedInputCapsule(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  placeholder: String,
  leadingIcon: ImageVector,
  isFocused: Boolean,
  onFocusChange: (Boolean) -> Unit,
  metrics: ResponsiveMetrics,
  modifier: Modifier = Modifier,
  isPassword: Boolean = false,
  isPasswordVisible: Boolean = false,
  onTogglePasswordVisibility: (() -> Unit)? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default
) {
  val palette = currentGlassPalette()
  val focusGlowColor = Color(0xFF6366F1)
  val capsuleShape = RoundedCornerShape(14.dp)

  Box(
    modifier = modifier
      .shadow(
        elevation = if (isFocused) 10.dp else 4.dp,
        shape = capsuleShape,
        ambientColor = if (isFocused) focusGlowColor.copy(alpha = 0.35f) else palette.shadowAmbient,
        spotColor = if (isFocused) focusGlowColor.copy(alpha = 0.35f) else palette.shadowSpot
      )
      .clip(capsuleShape)
      .background(
        color = if (isFocused) {
          palette.nestedCardFill.copy(alpha = if (palette.isDark) 0.90f else 0.92f)
        } else {
          palette.nestedCardFill.copy(alpha = if (palette.isDark) 0.72f else 0.75f)
        }
      )
      .border(
        width = if (isFocused) 1.6.dp else 1.dp,
        brush = if (isFocused) {
          Brush.linearGradient(
            listOf(
              focusGlowColor,
              Color(0xFF8B5CF6).copy(alpha = 0.85f)
            )
          )
        } else {
          Brush.linearGradient(
            listOf(
              Color.White.copy(alpha = if (palette.isDark) 0.22f else 0.55f),
              Color.White.copy(alpha = 0.08f)
            )
          )
        },
        shape = capsuleShape
      )
  ) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = {
        Text(
          text = label,
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.SemiBold
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      },
      placeholder = {
        Text(
          text = placeholder,
          style = MaterialTheme.typography.bodyMedium,
          color = palette.textSecondary.copy(alpha = 0.65f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      },
      leadingIcon = {
        Icon(
          imageVector = leadingIcon,
          contentDescription = null,
          tint = if (isFocused) focusGlowColor else palette.accentIndigo,
          modifier = Modifier.size(22.dp)
        )
      },
      trailingIcon = if (isPassword && onTogglePasswordVisibility != null) {
        {
          IconButton(
            onClick = onTogglePasswordVisibility,
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
              contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
              tint = palette.textSecondary,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      } else null,
      visualTransformation = if (isPassword && !isPasswordVisible) {
        PasswordVisualTransformation()
      } else {
        VisualTransformation.None
      },
      textStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = metrics.scaledSp(15),
        color = palette.textPrimary,
        fontWeight = FontWeight.Medium
      ),
      singleLine = true,
      shape = capsuleShape,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedLabelColor = focusGlowColor,
        unfocusedLabelColor = palette.textSecondary,
        cursorColor = focusGlowColor
      ),
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      modifier = Modifier
        .fillMaxWidth()
        .onFocusChanged { onFocusChange(it.isFocused) }
    )
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
              .widthIn(max = if (metrics.isTabletLandscape) 1080.dp else metrics.maxContentWidth)
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

  val localVersion = remember(context) { getInstalledVersionName(context) }
  val currentVersion = localVersion
  val offlinePrefs = remember(context) {
    context.applicationContext.getSharedPreferences(
      "sibil_app_offline_storage",
      Context.MODE_PRIVATE
    )
  }
  val initialDismissedTag = remember(offlinePrefs) {
    offlinePrefs.getString("dismissed_update_tag", "").orEmpty()
  }

  var availableUpdateRelease by remember {
    val cached = cachedSessionUpdateRelease
    val validCached = if (
      cached != null &&
      isStrictlyNewer(cached.tagName, localVersion) &&
      cached.tagName != initialDismissedTag &&
      cached.tagName != dismissedSessionUpdateTag
    ) {
      cached
    } else {
      null
    }
    mutableStateOf(validCached)
  }
  var showUpdateDialog by remember {
    val release = availableUpdateRelease
    mutableStateOf(
      release != null &&
        !hasDismissedUpdateForSession &&
        isStrictlyNewer(release.tagName, localVersion) &&
        release.tagName != initialDismissedTag &&
        release.tagName != dismissedSessionUpdateTag
    )
  }

  var showWhatsNewDialog by rememberSaveable { mutableStateOf(false) }

  // First-Launch "What's New" Version Handshake Logic on App Launch
  LaunchedEffect(localVersion) {
    val lastSeenVersion = offlinePrefs.getString("last_seen_version", null)
    if (lastSeenVersion == null) {
      // First install ever -> Do not show "What's New". Save last_seen_version = localVersion.
      offlinePrefs.edit().putString("last_seen_version", localVersion).apply()
      showWhatsNewDialog = false
    } else if (isStrictlyNewer(localVersion, lastSeenVersion)) {
      // User just installed an update -> Trigger showWhatsNewDialog = true.
      showWhatsNewDialog = true
    } else if (lastSeenVersion != localVersion) {
      offlinePrefs.edit().putString("last_seen_version", localVersion).apply()
      showWhatsNewDialog = false
    }
  }

  // Startup Handshake Rules for Automatic Update Check:
  // Do NOT prompt automatically if:
  // a) isStrictlyNewer(remoteTag, localVersion) is false.
  // b) The user already tapped "Remind Me Later" for this exact remoteTag (dismissed_update_tag == remoteTag).
  // Only set showUpdateDialog = true when remoteTag is strictly greater than localVersion.
  LaunchedEffect(localVersion) {
    if (!hasDismissedUpdateForSession && availableUpdateRelease == null) {
      val detectedUpdate = checkForAppUpdate(context)
      val dismissedTag = offlinePrefs.getString("dismissed_update_tag", "").orEmpty()
      val remoteTag = detectedUpdate?.tagName.orEmpty()
      if (
        detectedUpdate != null &&
        isStrictlyNewer(remoteTag, localVersion) &&
        dismissedTag != remoteTag &&
        dismissedSessionUpdateTag != remoteTag
      ) {
        cachedSessionUpdateRelease = detectedUpdate
        availableUpdateRelease = detectedUpdate
        showUpdateDialog = true
      } else {
        cachedSessionUpdateRelease = null
        availableUpdateRelease = null
        showUpdateDialog = false
      }
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
        .widthIn(max = 680.dp)
        .align(Alignment.TopCenter)
        .testTag("academic_dashboard_list"),
      contentPadding = PaddingValues(
        horizontal = metrics.scaledDp(16),
        vertical = 12.dp
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Compact User Greeting & Profile Bar + Sleek Compact Weather & Environmental Widget
      item {
        val env = viewModel.weatherState
        val aqiAccentColor = when {
          env.usAqi <= 50 -> Color(0xFF10B981)   // Good (Green accent)
          env.usAqi <= 100 -> Color(0xFFF59E0B)  // Moderate (Yellow/Amber accent)
          env.usAqi <= 150 -> Color(0xFFF97316)  // Sensitive (Orange accent)
          else -> Color(0xFFEF4444)              // Unhealthy (Red accent)
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Ultra-compact Header Row: Username + Live Date/Time on Left, Profile Button on Right
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = viewModel.displayUsername,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = palette.accentIndigo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("dashboard_username_header")
              )

              Text(
                text = currentDateTimeText,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 12.sp,
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

            if (viewModel.isFirestoreSyncing) {
              CircularProgressIndicator(
                modifier = Modifier
                  .padding(end = 8.dp)
                  .size(18.dp)
                  .testTag("dashboard_firestore_sync_indicator"),
                strokeWidth = 2.dp,
                color = palette.accentIndigo
              )
            }

            LiquidGlassContainer(
              cornerRadius = 20.dp,
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
                  modifier = Modifier.size(28.dp)
                )
              }
            }
          }

          // Compact Dashboard Weather & Environmental Widget (16.dp corner radius, padding(12.dp))
          LiquidGlassCard(
            cornerRadius = 16.dp,
            elevation = 6.dp,
            onClick = {
              coroutineScope.launch {
                val (lat, lon) = resolveDeviceCoordinatesOrFallback(context)
                viewModel.fetchWeather(latitude = lat, longitude = lon)
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .widthIn(max = 680.dp)
              .align(Alignment.CenterHorizontally)
              .testTag("dashboard_compact_weather_card")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Row 1 (Condition & Temperature):
              // Weather icon (18.dp), Condition description (13.sp, FontWeight.Medium), and Current Temp (14.sp, FontWeight.Bold)
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
                  Icon(
                    imageVector = Icons.Filled.Cloud,
                    contentDescription = "Live Weather Icon",
                    tint = palette.accentIndigo,
                    modifier = Modifier.size(18.dp)
                  )

                  Text(
                    text = if (env.isLoading) {
                      "Fetching Weather..."
                    } else if (env.isOffline) {
                      "Weather Offline"
                    } else {
                      "${env.conditionEmoji} ${env.conditionText}"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Medium
                    ),
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("dashboard_weather_header")
                  )
                }

                if (!env.isLoading && !env.isOffline) {
                  Text(
                    text = "${env.temperatureCelsius}°C",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontFamily = FontFamily.Monospace,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = palette.textPrimary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.testTag("dashboard_weather_temp")
                  )
                }
              }

              // Row 2 (Environmental Metrics - Fits directly underneath):
              // Two compact micro-pills with subtle frosted outlines:
              // 1. Heat Index pill: "🌡️ Heat Index: 33°C" (11.sp, FontWeight.SemiBold)
              // 2. Air Quality pill: "🍃 AQI: 42 • Good" (11.sp, FontWeight.SemiBold)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(50),
                  color = palette.nestedCardFill.copy(alpha = if (palette.isDark) 0.72f else 0.85f),
                  border = BorderStroke(
                    width = 1.dp,
                    color = palette.accentIndigo.copy(alpha = 0.28f)
                  ),
                  modifier = Modifier.testTag("dashboard_heat_index_pill")
                ) {
                  Text(
                    text = "🌡️ Heat Index: ${env.heatIndexCelsius}°C",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold
                    ),
                    color = palette.textPrimary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  )
                }

                Surface(
                  shape = RoundedCornerShape(50),
                  color = aqiAccentColor.copy(alpha = if (palette.isDark) 0.16f else 0.12f),
                  border = BorderStroke(
                    width = 1.dp,
                    color = aqiAccentColor.copy(alpha = 0.45f)
                  ),
                  modifier = Modifier.testTag("dashboard_aqi_pill")
                ) {
                  Text(
                    text = "🍃 AQI: ${env.usAqi} • ${env.aqiRating}",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold
                    ),
                    color = aqiAccentColor,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  )
                }
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

    if (showWhatsNewDialog) {
      WhatsNewDialog(
        currentVersion = currentVersion,
        metrics = metrics,
        onExploreUpdates = {
          offlinePrefs.edit().putString("last_seen_version", currentVersion).apply()
          showWhatsNewDialog = false
        }
      )
    }

    val activeUpdateRelease = availableUpdateRelease
    if (
      showUpdateDialog &&
      activeUpdateRelease != null &&
      !hasDismissedUpdateForSession &&
      isStrictlyNewer(activeUpdateRelease.tagName, localVersion) &&
      dismissedSessionUpdateTag != activeUpdateRelease.tagName
    ) {
      UpdateAvailableDialog(
        releaseInfo = activeUpdateRelease,
        metrics = metrics,
        onUpdateNow = {
          hasDismissedUpdateForSession = true
          dismissedSessionUpdateTag = activeUpdateRelease.tagName
          cachedSessionUpdateRelease = null
          availableUpdateRelease = null
          showUpdateDialog = false
        },
        onRemindLater = {
          offlinePrefs.edit().putString("dismissed_update_tag", activeUpdateRelease.tagName).apply()
          dismissedSessionUpdateTag = activeUpdateRelease.tagName
          hasDismissedUpdateForSession = true
          cachedSessionUpdateRelease = null
          availableUpdateRelease = null
          showUpdateDialog = false
        }
      )
    }
  }
}

/**
 * Metadata for an available GitHub Release update discovered by `checkForAppUpdate`.
 */
data class GitHubReleaseUpdateInfo(
  val tagName: String,
  val versionBadgeText: String,
  val releaseNotes: String,
  val downloadUrl: String,
  val body: String = releaseNotes
)

/**
 * Result states for on-demand and startup GitHub release checks.
 */
sealed class GitHubUpdateCheckResult {
  data class UpdateAvailable(
    val releaseInfo: GitHubReleaseUpdateInfo,
    val localVersion: String
  ) : GitHubUpdateCheckResult()

  data class UpToDate(
    val localVersion: String,
    val remoteTag: String
  ) : GitHubUpdateCheckResult()

  data object NetworkError : GitHubUpdateCheckResult()
}

private const val DEFAULT_FALLBACK_RELEASE_NOTES =
  "• Performance enhancements & formula vault rendering optimizations.\n• Bug fixes and battery life improvements."

private var hasDismissedUpdateForSession by mutableStateOf(false)
private var dismissedSessionUpdateTag by mutableStateOf("")
private var cachedSessionUpdateRelease by mutableStateOf<GitHubReleaseUpdateInfo?>(null)

/**
 * Retrieves the installed version name dynamically from Android's `PackageManager`.
 */
fun getInstalledVersionName(context: Context): String {
  return try {
    val pInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      context.packageManager.getPackageInfo(
        context.packageName,
        android.content.pm.PackageManager.PackageInfoFlags.of(0)
      )
    } else {
      @Suppress("DEPRECATION")
      context.packageManager.getPackageInfo(context.packageName, 0)
    }
    pInfo.versionName ?: APP_VERSION_NAME.removePrefix("v").removePrefix("V")
  } catch (e: Exception) {
    APP_VERSION_NAME.removePrefix("v").removePrefix("V")
  }
}

/**
 * Extracts semantic version numbers (e.g., `[1, 0, 4]`) from any release tag, release title,
 * or changelog string (such as `"v1.0.4"`, `"Debug APK Build #14 (v1.0.14)"`, or `"release-1.2"`).
 */
fun extractVersionNumbers(text: String): List<Int> {
  val regex = Regex("""(\d+)\.(\d+)(?:\.(\d+))?""")
  val match = regex.find(text) ?: return emptyList()
  return match.groupValues.drop(1).filter { it.isNotEmpty() }.map { it.toInt() }
}

/**
 * Parses a semantic version string into integer components `[1, 0, 4]` using [extractVersionNumbers].
 */
fun parseVersionParts(raw: String): List<Int> {
  return extractVersionNumbers(raw)
}

/**
 * Strict Semantic Version Comparison (`remoteTag` vs `localVersion`):
 * Returns `true` ONLY when `remoteTag` is strictly greater than `localVersion`.
 * If either string contains no recognizable version numbers, or if equal/older, NEVER flags as newer.
 */
fun isStrictlyNewer(remoteTag: String, localVersion: String): Boolean {
  val remote = extractVersionNumbers(remoteTag)
  val local = extractVersionNumbers(localVersion)
  if (remote.isEmpty() || local.isEmpty()) return false
  val maxLen = maxOf(remote.size, local.size)
  for (i in 0 until maxLen) {
    val r = remote.getOrElse(i) { 0 }
    val l = local.getOrElse(i) { 0 }
    if (r > l) return true
    if (r < l) return false
  }
  return false // If equal or older, NEVER flag as newer
}

/**
 * Alias for `isStrictlyNewer` to ensure all callers use strict numerical version comparison.
 */
fun isNewerVersion(remoteTag: String, localVersion: String): Boolean =
  isStrictlyNewer(remoteTag = remoteTag, localVersion = localVersion)

/**
 * Formats raw GitHub markdown release notes (`release.body`) into clean bulleted items.
 * Converts markdown bullet prefixes (`- ` or `* `) into `• ` and falls back to the standard
 * release notes summary if blank.
 */
fun formatGitHubReleaseChangelogLines(rawBody: String?): List<String> {
  val source = rawBody?.trim().orEmpty().ifEmpty { DEFAULT_FALLBACK_RELEASE_NOTES }
  val parsedLines = source
    .lines()
    .map { line ->
      val trimmed = line.trim()
        .removePrefix("### ")
        .removePrefix("## ")
        .removePrefix("# ")
        .trim()
      when {
        trimmed.startsWith("- ") -> "• " + trimmed.removePrefix("- ").trim()
        trimmed.startsWith("* ") -> "• " + trimmed.removePrefix("* ").trim()
        trimmed.startsWith("• ") -> "• " + trimmed.removePrefix("• ").trim()
        else -> trimmed
      }
    }
    .filter { it.isNotEmpty() }

  return if (parsedLines.isEmpty()) {
    DEFAULT_FALLBACK_RELEASE_NOTES.lines()
  } else {
    parsedLines
  }
}

/**
 * Helper to fetch a JSON response string from a GitHub API URL with standard headers and timeouts.
 */
private fun fetchGitHubApiResponse(url: String): Pair<Int, String?> {
  var connection: HttpURLConnection? = null
  return try {
    connection = (URL(url).openConnection() as HttpURLConnection).apply {
      requestMethod = "GET"
      connectTimeout = 5_000
      readTimeout = 5_000
      doInput = true
      setRequestProperty("User-Agent", "Sibil-App-Android")
      setRequestProperty("Accept", "application/vnd.github.v3+json")
    }
    val code = connection.responseCode
    if (code in 200..299) {
      val body = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use {
        it.readText()
      }
      code to body
    } else {
      code to null
    }
  } catch (_: Exception) {
    -1 to null
  } finally {
    connection?.disconnect()
  }
}

/**
 * Resolves the latest valid release JSONObject from `Dodolzzzkie/Sibil-app`.
 * Queries `/releases/latest` first, and falls back to `/releases?per_page=10` if `/releases/latest`
 * returns 404 or points to `internal-keystore-vault`.
 */
private fun resolveLatestValidGitHubReleaseJson(): JSONObject? {
  val latestUrl = "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/latest"
  val (latestCode, latestBody) = fetchGitHubApiResponse(latestUrl)
  if (latestCode in 200..299 && !latestBody.isNullOrBlank()) {
    val obj = runCatching { JSONObject(latestBody) }.getOrNull()
    val tag = obj?.optString("tag_name", "")?.trim().orEmpty()
    if (obj != null && !tag.equals("internal-keystore-vault", ignoreCase = true)) {
      return obj
    }
  }

  // Fallback: inspect recent releases list (handles pre-releases or repos without a marked 'latest' release)
  val listUrl = "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases?per_page=10"
  val (listCode, listBody) = fetchGitHubApiResponse(listUrl)
  if (listCode in 200..299 && !listBody.isNullOrBlank()) {
    val arr = runCatching { JSONArray(listBody) }.getOrNull() ?: return null
    for (i in 0 until arr.length()) {
      val candidate = arr.optJSONObject(i) ?: continue
      if (candidate.optBoolean("draft", false)) continue
      val tag = candidate.optString("tag_name", "").trim()
      if (tag.equals("internal-keystore-vault", ignoreCase = true)) continue
      return candidate
    }
  }
  return null
}

/**
 * Queries `https://api.github.com/repos/Dodolzzzkie/Sibil-app/releases/latest` (with `/releases` fallback)
 * on `Dispatchers.IO`, extracts the semantic version from `tag_name`, release `name` (title), or `body`,
 * and evaluates `isStrictlyNewer(latestTag, localVersion)`.
 * Used by both the automatic startup check and the manual "Check for Updates" button in About Sibil.
 */
suspend fun queryGitHubLatestRelease(context: Context): GitHubUpdateCheckResult =
  withContext(Dispatchers.IO) {
    val defaultDownloadUrl =
      "https://github.com/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/latest/download/app-debug.apk"

    try {
      val root = resolveLatestValidGitHubReleaseJson()
        ?: return@withContext GitHubUpdateCheckResult.NetworkError

      val rawTag = root.optString("tag_name", "").trim()
      val releaseTitle = root.optString("name", "").trim()
      val rawBody = root.optString("body", "").trim()
      val localVersion = getInstalledVersionName(context).removePrefix("v").removePrefix("V")
      if ((rawTag.isBlank() && releaseTitle.isBlank()) || rawTag.equals("internal-keystore-vault", ignoreCase = true)) {
        return@withContext GitHubUpdateCheckResult.UpToDate(
          localVersion = localVersion,
          remoteTag = localVersion
        )
      }

      // Extract semantic version numbers from tag_name first, then release title (name), then body,
      // and finally CI build tag pattern ("debug-apk-build-<run>-<attempt>") if present.
      val extractedRemoteParts = extractVersionNumbers(rawTag)
        .ifEmpty { extractVersionNumbers(releaseTitle) }
        .ifEmpty { extractVersionNumbers(rawBody) }
        .ifEmpty {
          Regex("""debug-apk-build-(\d+)""", RegexOption.IGNORE_CASE)
            .find(rawTag)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?.let { listOf(1, 0, it) }
            .orEmpty()
        }

      if (extractedRemoteParts.isEmpty()) {
        return@withContext GitHubUpdateCheckResult.UpToDate(
          localVersion = localVersion,
          remoteTag = localVersion
        )
      }

      val latestTag = "v${extractedRemoteParts.joinToString(".")}"

      val prefs = context.applicationContext.getSharedPreferences(
        "sibil_app_offline_storage",
        Context.MODE_PRIVATE
      )

      if (rawBody.isNotBlank()) {
        prefs.edit().putString("pending_release_notes", rawBody).apply()
      }

      if (!isStrictlyNewer(remoteTag = latestTag, localVersion = localVersion)) {
        return@withContext GitHubUpdateCheckResult.UpToDate(
          localVersion = localVersion,
          remoteTag = latestTag
        )
      }

      val releaseBody = rawBody.ifEmpty {
        DEFAULT_FALLBACK_RELEASE_NOTES
      }

      var resolvedDownloadUrl = defaultDownloadUrl
      val assetsArray = root.optJSONArray("assets")
      if (assetsArray != null) {
        for (i in 0 until assetsArray.length()) {
          val assetObj = assetsArray.optJSONObject(i) ?: continue
          val assetName = assetObj.optString("name", "").trim()
          val assetUrl = assetObj.optString("browser_download_url", "").trim()
          if (assetName.endsWith(".apk", ignoreCase = true) && assetUrl.isNotBlank()) {
            resolvedDownloadUrl = assetUrl
            break
          }
        }
        if (resolvedDownloadUrl == defaultDownloadUrl && assetsArray.length() > 0) {
          val fallbackAssetUrl = assetsArray.optJSONObject(0)
            ?.optString("browser_download_url", "")
            ?.trim()
            .orEmpty()
          if (fallbackAssetUrl.isNotBlank()) {
            resolvedDownloadUrl = fallbackAssetUrl
          }
        }
      }

      GitHubUpdateCheckResult.UpdateAvailable(
        releaseInfo = GitHubReleaseUpdateInfo(
          tagName = latestTag,
          versionBadgeText = latestTag,
          releaseNotes = releaseBody,
          downloadUrl = resolvedDownloadUrl,
          body = releaseBody
        ),
        localVersion = localVersion
      )
    } catch (_: Exception) {
      GitHubUpdateCheckResult.NetworkError
    }
  }

/**
 * In-App GitHub Auto-Update Checker:
 * Queries `https://api.github.com/repos/Dodolzzzkie/Sibil-app/releases/latest`
 * with a 5-second timeout, resolves `getInstalledVersionName(context)`, and returns
 * a `GitHubReleaseUpdateInfo` ONLY if `isStrictlyNewer(remoteTag, localVersion)` is `true`
 * and `dismissed_update_tag != remoteTag`.
 */
suspend fun checkForAppUpdate(context: Context): GitHubReleaseUpdateInfo? = withContext(Dispatchers.IO) {
  when (val result = queryGitHubLatestRelease(context)) {
    is GitHubUpdateCheckResult.UpdateAvailable -> {
      val prefs = context.applicationContext.getSharedPreferences(
        "sibil_app_offline_storage",
        Context.MODE_PRIVATE
      )
      val dismissedTag = prefs.getString("dismissed_update_tag", "").orEmpty()
      val remoteTag = result.releaseInfo.tagName
      if (
        !isStrictlyNewer(remoteTag, result.localVersion) ||
        remoteTag == dismissedTag ||
        remoteTag == dismissedSessionUpdateTag
      ) {
        null
      } else {
        result.releaseInfo
      }
    }
    else -> null
  }
}

/**
 * Fetches the latest release notes (`body`) from GitHub Releases API when `pending_release_notes`
 * is not yet cached locally.
 */
suspend fun fetchLatestGitHubReleaseBody(context: Context): String? = withContext(Dispatchers.IO) {
  try {
    val root = resolveLatestValidGitHubReleaseJson() ?: return@withContext null
    val body = root.optString("body", "").trim()
    if (body.isNotBlank()) {
      val prefs = context.applicationContext.getSharedPreferences(
        "sibil_app_offline_storage",
        Context.MODE_PRIVATE
      )
      prefs.edit().putString("pending_release_notes", body).apply()
      body
    } else {
      null
    }
  } catch (_: Exception) {
    null
  }
}

/**
 * Launches the Android Package Installer intent for a downloaded APK using `FileProvider`,
 * falling back to direct browser `ACTION_VIEW` if a `FileProvider` or security restriction occurs.
 */
private fun launchInstalledApkOrBrowserFallback(
  context: Context,
  apkFile: File,
  releaseDownloadUrl: String
) {
  try {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.provider",
      apkFile
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(uri, "application/vnd.android.package-archive")
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
  } catch (_: Exception) {
    try {
      val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseDownloadUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(browserIntent)
    } catch (_: Exception) {
      Toast.makeText(
        context,
        "Unable to launch package installer or open browser.",
        Toast.LENGTH_SHORT
      ).show()
    }
  }
}

/**
 * Dynamic "What's New in Sibil" / Update Available Liquid Glass Modal (`UpdateAvailableDialog`)
 * with Live In-App Direct APK Downloader & Progress Animation:
 * - State Management:
 *   - `updateDownloadProgress`: `Float` state (`0.0f` to `1.0f`)
 *   - `isDownloadingUpdate`: `Boolean` state
 *   - `downloadError`: `String?` state
 * - When user taps "Update Now":
 *   - Switches dialog view to an animated Liquid Glass "Downloading Update" container.
 *   - Streams APK bytes on `Dispatchers.IO` via `HttpURLConnection` to `File(context.cacheDir, "sibil-update.apk")`.
 *   - Emits real-time progress (`0%` to `100%`), byte counter (`"12.4 MB / 24.8 MB"`), and speed indicator.
 *   - Once download reaches 100%, dismisses the modal and triggers Android package installer via `FileProvider`
 *     (with automatic browser `ACTION_VIEW` fallback).
 */
@Composable
fun UpdateAvailableDialog(
  releaseInfo: GitHubReleaseUpdateInfo,
  metrics: ResponsiveMetrics,
  onUpdateNow: () -> Unit,
  onRemindLater: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val palette = currentGlassPalette()
  val cardShape = RoundedCornerShape(28.dp)
  val cardGradientBrush = remember(palette.isDark, palette.cardSurface, palette.cardBottomSheen) {
    Brush.verticalGradient(
      colors = listOf(
        palette.cardSurface,
        palette.cardBottomSheen
      )
    )
  }

  // In-App Direct Downloader State Management
  var updateDownloadProgress by remember { mutableFloatStateOf(0.0f) }
  var isDownloadingUpdate by remember { mutableStateOf(false) }
  var downloadError by remember { mutableStateOf<String?>(null) }
  var downloadedBytes by remember { mutableLongStateOf(0L) }
  var totalBytes by remember { mutableLongStateOf(0L) }
  var downloadSpeedMBps by remember { mutableFloatStateOf(0.0f) }

  val animatedProgress by animateFloatAsState(
    targetValue = updateDownloadProgress.coerceIn(0f, 1f),
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "update_download_progress_anim"
  )

  val infiniteTransition = rememberInfiniteTransition(label = "download_arrow_pulse")
  val arrowBounceOffsetDp by infiniteTransition.animateFloat(
    initialValue = -5f,
    targetValue = 6f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "arrow_bounce_offset"
  )
  val orbPulseScale by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orb_pulse_scale"
  )

  val formattedChangelogItems = remember(releaseInfo.body, releaseInfo.releaseNotes) {
    val rawNotes = releaseInfo.body.ifBlank { releaseInfo.releaseNotes }
    formatGitHubReleaseChangelogLines(rawNotes)
  }

  fun startInAppApkDownload() {
    if (isDownloadingUpdate) return
    isDownloadingUpdate = true
    downloadError = null
    updateDownloadProgress = 0.0f
    downloadedBytes = 0L
    totalBytes = 0L
    downloadSpeedMBps = 0.0f

    // Cache release notes so the post-install "What's New" dialog has them ready
    val rawNotes = releaseInfo.body.ifBlank { releaseInfo.releaseNotes }.trim()
    if (rawNotes.isNotBlank()) {
      context.applicationContext
        .getSharedPreferences("sibil_app_offline_storage", Context.MODE_PRIVATE)
        .edit()
        .putString("pending_release_notes", rawNotes)
        .apply()
    }

    coroutineScope.launch {
      val targetFile = File(context.cacheDir, "sibil-update.apk")
      val downloadResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
          var currentUrl = releaseInfo.downloadUrl
          var redirectCount = 0
          while (redirectCount < 6) {
            val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
              requestMethod = "GET"
              instanceFollowRedirects = true
              connectTimeout = 15_000
              readTimeout = 20_000
              setRequestProperty("User-Agent", "Sibil-App-Android")
              setRequestProperty("Accept", "application/octet-stream, */*")
            }
            val code = conn.responseCode
            if (code in listOf(
                HttpURLConnection.HTTP_MOVED_PERM,
                HttpURLConnection.HTTP_MOVED_TEMP,
                HttpURLConnection.HTTP_SEE_OTHER,
                307,
                308
              )
            ) {
              val location = conn.getHeaderField("Location")
              conn.disconnect()
              if (location.isNullOrBlank()) break
              currentUrl = location
              redirectCount++
            } else {
              connection = conn
              break
            }
          }

          val activeConnection = connection
            ?: (URL(currentUrl).openConnection() as HttpURLConnection).apply {
              requestMethod = "GET"
              instanceFollowRedirects = true
              connectTimeout = 15_000
              readTimeout = 20_000
              setRequestProperty("User-Agent", "Sibil-App-Android")
            }
          connection = activeConnection

          val status = activeConnection.responseCode
          if (status !in 200..299) {
            return@withContext Result.failure<File>(
              IllegalStateException("HTTP $status while downloading update APK")
            )
          }

          val contentLength = activeConnection.contentLengthLong
          withContext(Dispatchers.Main) {
            totalBytes = if (contentLength > 0L) contentLength else 0L
          }

          val startTimeMs = SystemClock.elapsedRealtime()
          var bytesCopied = 0L

          activeConnection.inputStream.use { input ->
            FileOutputStream(targetFile).use { output ->
              val buffer = ByteArray(8 * 1024)
              var bytesRead: Int
              while (input.read(buffer).also { bytesRead = it } != -1) {
                ensureActive()
                output.write(buffer, 0, bytesRead)
                bytesCopied += bytesRead

                val elapsedSec = ((SystemClock.elapsedRealtime() - startTimeMs) / 1000f).coerceAtLeast(0.1f)
                val speed = (bytesCopied / (1024f * 1024f)) / elapsedSec
                val fraction = if (contentLength > 0L) {
                  (bytesCopied.toFloat() / contentLength.toFloat()).coerceIn(0f, 0.99f)
                } else {
                  // Smooth indeterminate-style ramp when Content-Length is omitted
                  (bytesCopied.toFloat() / (25f * 1024f * 1024f)).coerceIn(0.05f, 0.95f)
                }

                withContext(Dispatchers.Main) {
                  downloadedBytes = bytesCopied
                  if (contentLength > 0L) {
                    totalBytes = contentLength
                  }
                  downloadSpeedMBps = speed
                  updateDownloadProgress = fraction
                }
              }
              output.flush()
            }
          }

          withContext(Dispatchers.Main) {
            updateDownloadProgress = 1.0f
          }
          Result.success(targetFile)
        } catch (ce: CancellationException) {
          throw ce
        } catch (e: Exception) {
          Result.failure(e)
        } finally {
          connection?.disconnect()
        }
      }

      downloadResult.fold(
        onSuccess = { downloadedApkFile ->
          delay(220L)
          isDownloadingUpdate = false
          onUpdateNow()
          launchInstalledApkOrBrowserFallback(
            context = context,
            apkFile = downloadedApkFile,
            releaseDownloadUrl = releaseInfo.downloadUrl
          )
        },
        onFailure = { err ->
          isDownloadingUpdate = false
          downloadError = err.message ?: "Download interrupted. You can retry or download via browser."
        }
      )
    }
  }

  Dialog(
    onDismissRequest = {
      if (!isDownloadingUpdate) {
        onRemindLater()
      }
    },
    properties = DialogProperties(
      dismissOnBackPress = !isDownloadingUpdate,
      dismissOnClickOutside = !isDownloadingUpdate,
      usePlatformDefaultWidth = false
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = if (palette.isDark) 0.65f else 0.42f))
        .clickable(enabled = !isDownloadingUpdate, onClick = onRemindLater),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .widthIn(max = 460.dp)
          .shadow(
            elevation = 18.dp,
            shape = cardShape,
            ambientColor = palette.shadowAmbient,
            spotColor = palette.shadowSpot
          )
          .clip(cardShape)
          .background(brush = cardGradientBrush, shape = cardShape)
          .border(
            border = BorderStroke(
              1.dp,
              Brush.linearGradient(
                colors = listOf(
                  Color.White.copy(alpha = if (palette.isDark) 0.45f else 0.85f),
                  Color(0xFF6366F1).copy(alpha = 0.35f),
                  Color.White.copy(alpha = 0.12f)
                )
              )
            ),
            shape = cardShape
          )
          .clickable(enabled = false) {}
          .testTag("update_available_dialog")
      ) {
        AnimatedContent(
          targetState = isDownloadingUpdate,
          transitionSpec = {
            (fadeIn(tween(240)) + scaleIn(initialScale = 0.95f, animationSpec = tween(240)))
              .togetherWith(fadeOut(tween(180)))
          },
          label = "update_dialog_mode_transition"
        ) { downloading ->
          if (downloading) {
            // Animated Liquid Glass "Downloading Update" Container
            val percentInt = (animatedProgress * 100f).roundToInt().coerceIn(0, 100)
            val downloadedMbText = String.format(Locale.US, "%.1f MB", downloadedBytes / (1024f * 1024f))
            val totalMbText = if (totalBytes > 0L) {
              String.format(Locale.US, "%.1f MB", totalBytes / (1024f * 1024f))
            } else {
              "Calculating..."
            }
            val speedText = String.format(Locale.US, "%.1f MB/s", downloadSpeedMBps.coerceAtLeast(0.1f))

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(metrics.scaledDp(24))
                .testTag("update_downloading_container"),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
            ) {
              // Pulsating & Floating Animated Downward Arrow Orb
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .graphicsLayer {
                    scaleX = orbPulseScale
                    scaleY = orbPulseScale
                  }
                  .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Color(0x444338CA),
                    spotColor = Color(0x6606B6D4)
                  )
                  .clip(RoundedCornerShape(22.dp))
                  .background(brush = LiquidCyanVioletGradient)
                  .border(
                    BorderStroke(1.5.dp, Color.White.copy(alpha = 0.72f)),
                    RoundedCornerShape(22.dp)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.KeyboardArrowDown,
                  contentDescription = "Downloading Update",
                  tint = Color.White,
                  modifier = Modifier
                    .size(36.dp)
                    .offset(y = arrowBounceOffsetDp.dp)
                )
              }

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "Downloading Update",
                  style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                  ),
                  color = palette.textPrimary
                )
                Text(
                  text = "Sibil ${releaseInfo.tagName} • Keep app open",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = palette.accentIndigo
                )
              }

              // Frosted Glass Progress Card with Glowing Linear Progress Bar & Percentage
              Surface(
                shape = RoundedCornerShape(18.dp),
                color = palette.nestedCardFill,
                border = palette.nestedBorder,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "$percentInt%",
                      style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                      ),
                      color = palette.accentIndigo,
                      modifier = Modifier.testTag("update_download_percentage_text")
                    )

                    Surface(
                      shape = CircleShape,
                      color = Color(0xFF06B6D4).copy(alpha = 0.16f),
                      border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f))
                    ) {
                      Text(
                        text = speedText,
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontFamily = JetBrainsMonoFamily,
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold
                        ),
                        color = if (palette.isDark) Color(0xFF67E8F9) else Color(0xFF0E7490),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                      )
                    }
                  }

                  // Glowing Linear Progress Bar Track
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(12.dp)
                      .clip(CircleShape)
                      .background(
                        if (palette.isDark) Color.White.copy(alpha = 0.10f)
                        else Color(0xFF6366F1).copy(alpha = 0.14f)
                      )
                      .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                        CircleShape
                      )
                  ) {
                    Box(
                      modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress.coerceIn(0.03f, 1f))
                        .clip(CircleShape)
                        .background(
                          brush = Brush.horizontalGradient(
                            colors = listOf(
                              Color(0xFF06B6D4),
                              Color(0xFF6366F1),
                              Color(0xFF8B5CF6)
                            )
                          )
                        )
                        .testTag("update_download_progress_bar")
                    )
                  }

                  // Byte counter indicator (e.g., "12.4 MB / 24.8 MB")
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = if (totalBytes > 0L) {
                        "$downloadedMbText / $totalMbText"
                      } else {
                        "$downloadedMbText downloaded"
                      },
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                      ),
                      color = palette.textSecondary,
                      modifier = Modifier.testTag("update_download_bytes_text")
                    )

                    Text(
                      text = if (percentInt >= 100) "Launching installer..." else "Streaming APK...",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                      ),
                      color = palette.textSecondary
                    )
                  }
                }
              }
            }
          } else {
            // Standard Update Available View
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(metrics.scaledDp(22)),
              verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
            ) {
              // Header: Glowing rocket/sparkle badge icon + "What's New in Sibil" + dynamic tag_name badge
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .shadow(
                      elevation = 8.dp,
                      shape = RoundedCornerShape(14.dp),
                      ambientColor = Color(0x334338CA),
                      spotColor = Color(0x554F46E5)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(brush = LiquidOrbBrush)
                    .border(
                      BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                      RoundedCornerShape(14.dp)
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "What's New in Sibil",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                  )
                }

                Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = "What's New in Sibil",
                    style = MaterialTheme.typography.headlineSmall.copy(
                      fontSize = 20.sp,
                      fontWeight = FontWeight.ExtraBold
                    ),
                    color = palette.textPrimary,
                    modifier = Modifier.testTag("update_dialog_title")
                  )

                  Surface(
                    shape = CircleShape,
                    color = Color(0xFF6366F1).copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.45f))
                  ) {
                    Text(
                      text = releaseInfo.tagName,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                      ),
                      color = palette.accentIndigo,
                      modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                        .testTag("update_dialog_version_badge")
                    )
                  }
                }
              }

              // Dynamic Scrollable Changelog Container
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = palette.nestedCardFill,
                border = palette.nestedBorder,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("update_dialog_changelog_scroll"),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  formattedChangelogItems.forEach { itemText ->
                    Text(
                      text = itemText,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = metrics.scaledSp(13),
                        lineHeight = metrics.scaledSp(19),
                        fontWeight = FontWeight.Medium
                      ),
                      color = palette.textPrimary
                    )
                  }
                }
              }

              // Optional download error banner with browser fallback option
              val activeError = downloadError
              if (!activeError.isNullOrBlank()) {
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = Color(0xFFEF4444).copy(alpha = 0.14f),
                  border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = activeError,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                      ),
                      color = if (palette.isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
                    )
                    Text(
                      text = "Open Direct Browser Download →",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                      ),
                      color = palette.accentIndigo,
                      modifier = Modifier.clickable {
                        onUpdateNow()
                        try {
                          val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseInfo.downloadUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                          }
                          context.startActivity(browserIntent)
                        } catch (_: Exception) {
                          Toast.makeText(
                            context,
                            "Unable to open browser for APK download.",
                            Toast.LENGTH_SHORT
                          ).show()
                        }
                      }
                    )
                  }
                }
              }

              // Action Buttons: "Update Now" + "Remind Me Later"
              Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                      elevation = 8.dp,
                      shape = RoundedCornerShape(18.dp),
                      ambientColor = Color(0x334338CA),
                      spotColor = Color(0x554F46E5)
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(brush = LiquidOrbBrush, shape = RoundedCornerShape(18.dp))
                    .border(
                      BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                      RoundedCornerShape(18.dp)
                    )
                    .clickable { startInAppApkDownload() }
                    .padding(vertical = 14.dp)
                    .testTag("update_dialog_update_now_button"),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = if (downloadError == null) "Update Now" else "Retry Update Now",
                    style = MaterialTheme.typography.labelLarge.copy(
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                  )
                }

                TextButton(
                  onClick = onRemindLater,
                  shape = CircleShape,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("update_dialog_remind_later_button")
                ) {
                  Text(
                    text = "Remind Me Later",
                    style = MaterialTheme.typography.labelLarge.copy(
                      fontSize = 13.sp,
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
  }
}

/**
 * First-Launch "What's New" Changelog Card (`WhatsNewDialog`):
 * - Displayed automatically when `lastSeenVersion != null && isNewerVersion(currentVersion, lastSeenVersion)`.
 * - Frosted Liquid Glass Card (`28.dp` radius with specular border).
 * - Header: Glowing badge `v${currentVersion} Update Installed` + Title `"What's New in Sibil"`.
 * - Content Body: Displays cached `pending_release_notes` (or fetches the current release body from GitHub),
 *   parsing markdown bullets (`*`, `-`) into styled checkmark bullet rows with high-contrast text.
 * - Fallback text if notes are blank:
 *   `"• Performance enhancements & formula vault rendering optimizations.\n• Bug fixes and battery life improvements."`
 * - Action Button: `"Explore Updates"` gradient button that saves `last_seen_version = currentVersion` and dismisses.
 */
@Composable
fun WhatsNewDialog(
  metrics: ResponsiveMetrics,
  onExploreUpdates: () -> Unit,
  currentVersion: String = APP_VERSION_NAME
) {
  val context = LocalContext.current
  val palette = currentGlassPalette()
  val cardShape = RoundedCornerShape(28.dp)
  val cardGradientBrush = remember(palette.isDark, palette.cardSurface, palette.cardBottomSheen) {
    Brush.verticalGradient(
      colors = listOf(
        palette.cardSurface,
        palette.cardBottomSheen
      )
    )
  }

  val normalizedVersion = remember(currentVersion) {
    currentVersion.trim().removePrefix("v").removePrefix("V").ifEmpty { "1.0.0" }
  }

  var rawReleaseNotes by remember {
    val prefs = context.applicationContext.getSharedPreferences(
      "sibil_app_offline_storage",
      Context.MODE_PRIVATE
    )
    mutableStateOf(prefs.getString("pending_release_notes", "")?.trim().orEmpty())
  }

  // If cached release notes are blank, asynchronously fetch the latest release body from GitHub
  LaunchedEffect(Unit) {
    if (rawReleaseNotes.isBlank()) {
      val fetchedBody = fetchLatestGitHubReleaseBody(context)
      if (!fetchedBody.isNullOrBlank()) {
        rawReleaseNotes = fetchedBody
      }
    }
  }

  val parsedBulletLines = remember(rawReleaseNotes) {
    formatGitHubReleaseChangelogLines(
      rawReleaseNotes.ifBlank { DEFAULT_FALLBACK_RELEASE_NOTES }
    ).map { line ->
      line.removePrefix("• ").removePrefix("- ").removePrefix("* ").trim()
    }.filter { it.isNotEmpty() }
  }

  Dialog(
    onDismissRequest = onExploreUpdates,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = if (palette.isDark) 0.62f else 0.38f))
        .clickable(onClick = onExploreUpdates),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .widthIn(max = 460.dp)
          .shadow(
            elevation = 18.dp,
            shape = cardShape,
            ambientColor = palette.shadowAmbient,
            spotColor = palette.shadowSpot
          )
          .clip(cardShape)
          .background(brush = cardGradientBrush, shape = cardShape)
          .border(
            border = BorderStroke(
              1.dp,
              Brush.linearGradient(
                colors = listOf(
                  Color.White.copy(alpha = if (palette.isDark) 0.45f else 0.85f),
                  Color(0xFF6366F1).copy(alpha = 0.38f),
                  Color.White.copy(alpha = 0.10f)
                )
              )
            ),
            shape = cardShape
          )
          .clickable(enabled = false) {}
          .testTag("whats_new_dialog")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(metrics.scaledDp(22)),
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(16))
        ) {
          // Header: Sparkle icon + Glowing badge "v${currentVersion} Update Installed" + Title "What's New in Sibil"
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .shadow(
                  elevation = 8.dp,
                  shape = RoundedCornerShape(14.dp),
                  ambientColor = Color(0x334338CA),
                  spotColor = Color(0x554F46E5)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(brush = LiquidOrbBrush)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)), RoundedCornerShape(14.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "Sparkle",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = Color(0xFF6366F1).copy(alpha = 0.16f),
                border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.50f))
              ) {
                Text(
                  text = "v$normalizedVersion Update Installed",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = palette.accentIndigo,
                  modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 3.dp)
                    .testTag("whats_new_version_badge")
                )
              }

              Text(
                text = "What's New in Sibil",
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontSize = metrics.scaledSp(20),
                  fontWeight = FontWeight.ExtraBold
                ),
                color = palette.textPrimary,
                modifier = Modifier.testTag("whats_new_title")
              )
            }
          }

          // Styled Checkmark Bullet Rows in Scrollable Frosted Container
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 260.dp)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            parsedBulletLines.forEach { bulletText ->
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = palette.nestedCardFill,
                border = palette.nestedBorder,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                  verticalAlignment = Alignment.Top,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = palette.accentIndigo,
                    modifier = Modifier
                      .size(18.dp)
                      .padding(top = 1.dp)
                  )
                  Text(
                    text = bulletText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontSize = metrics.scaledSp(13),
                      lineHeight = metrics.scaledSp(19),
                      fontWeight = FontWeight.Medium
                    ),
                    color = palette.textPrimary,
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }
          }

          // Action Button: "Explore Updates" Gradient Button
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x334338CA),
                spotColor = Color(0x554F46E5)
              )
              .clip(RoundedCornerShape(18.dp))
              .background(brush = LiquidOrbBrush, shape = RoundedCornerShape(18.dp))
              .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                RoundedCornerShape(18.dp)
              )
              .clickable(onClick = onExploreUpdates)
              .padding(vertical = 14.dp)
              .testTag("whats_new_explore_button"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Explore Updates",
              style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              ),
              color = Color.White
            )
          }
        }
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
  val localView = LocalView.current
  val localStorageManager = remember(context) {
    LocalStorageManager(context.applicationContext)
  }
  LaunchedEffect(localStorageManager) {
    viewModel.attachStorage(localStorageManager)
  }

  val metrics = rememberResponsiveMetrics()
  val palette = currentGlassPalette()
  var isFlashcardsOpen by rememberSaveable { mutableStateOf(false) }
  var isFormulaVaultOpen by rememberSaveable { mutableStateOf(false) }
  var activeStudyDeckId by rememberSaveable { mutableStateOf<Int?>(null) }
  var comingSoonSnackbarMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(comingSoonSnackbarMessage) {
    if (comingSoonSnackbarMessage != null) {
      delay(2600L)
      comingSoonSnackbarMessage = null
    }
  }

  val comingSoonModules = remember {
    listOf(
      ExtraToolItem(
        id = "unit_converter",
        title = "Unit Converter",
        subtitle = "SI & Imperial engineering units",
        icon = Icons.Filled.SwapHoriz
      ),
      ExtraToolItem(
        id = "gpa_calculator",
        title = "GPA Calculator",
        subtitle = "Semester GWA & board weighting",
        icon = Icons.Filled.Calculate
      ),
      ExtraToolItem(
        id = "scientific_calculator",
        title = "Scientific Calculator",
        subtitle = "Beam & matrix solver",
        icon = Icons.Filled.Architecture
      ),
      ExtraToolItem(
        id = "boq_estimator",
        title = "Quick Tools & BOQ",
        subtitle = "Material & cost takeoff",
        icon = Icons.Filled.RequestQuote
      )
    )
  }

  val activeStudyDeck = viewModel.flashcardDecks.firstOrNull { it.id == activeStudyDeckId }

  if (isFormulaVaultOpen) {
    FormulaVaultMainView(
      metrics = metrics,
      onBackToExtras = { isFormulaVaultOpen = false },
      viewModel = viewModel,
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

  val deckCount = viewModel.flashcardDecks.size
  val totalCardCount = viewModel.flashcardDecks.sumOf { it.cards.size }

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 680.dp)
        .align(Alignment.TopCenter)
        .verticalScroll(rememberScrollState())
        .padding(
          start = metrics.scaledDp(20),
          end = metrics.scaledDp(20),
          top = metrics.scaledDp(24),
          bottom = metrics.scaledDp(116)
        )
        .testTag("extras_tools_page"),
      verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(18)),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Header
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

      // 1. TOP HERO CARD: Smart Flashcards Premier Featured Card
      LiquidGlassCard(
        onClick = {
          playCardHapticSound(context, localView)
          isFlashcardsOpen = true
        },
        shape = RoundedCornerShape(28.dp),
        elevation = 12.dp,
        borderStroke = BorderStroke(1.5.dp, palette.accentIndigo.copy(alpha = 0.55f)),
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 680.dp)
          .align(Alignment.CenterHorizontally)
          .testTag("extra_tool_card_smart_flashcards")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(metrics.scaledDp(20)),
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
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
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "Smart Flashcards",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
              )
            }

            // Gradient Badges ("AI Powered", "Multi-Batch")
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .clip(CircleShape)
                  .background(brush = LiquidVioletChipGradient)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "AI Powered",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1,
                  softWrap = false
                )
              }

              Surface(
                shape = CircleShape,
                color = Color(0xFF6366F1).copy(alpha = 0.16f),
                border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.45f))
              ) {
                Text(
                  text = "Multi-Batch",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = palette.accentIndigo,
                  maxLines = 1,
                  softWrap = false,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          Text(
            text = "Smart Flashcards",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontSize = metrics.scaledSp(21),
              fontWeight = FontWeight.ExtraBold
            ),
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = palette.nestedCardFill,
              border = palette.nestedBorder,
              modifier = Modifier
                .weight(1f, fill = false)
                .padding(end = 8.dp)
            ) {
              Text(
                text = "$deckCount ${if (deckCount == 1) "Deck" else "Decks"} • $totalCardCount Cards",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = palette.textPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }

            Button(
              onClick = {
                playCardHapticSound(context, localView)
                isFlashcardsOpen = true
              },
              shape = RoundedCornerShape(18.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White
              ),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
              modifier = Modifier
                .wrapContentWidth()
                .testTag("launch_flashcards_hero_button")
            ) {
              Text(
                text = "Launch Flashcards",
                maxLines = 1,
                softWrap = false,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
              )
            }
          }
        }
      }

      // 2. Active Companion Module: CE Formula Vault
      ExtraToolPlaceholderCard(
        tool = ExtraToolItem(
          id = "ce_formula_vault",
          title = "CE Formula Vault",
          subtitle = "",
          icon = Icons.Filled.Functions
        ),
        metrics = metrics,
        isComingSoon = false,
        onClick = {
          playCardHapticSound(context, localView)
          isFormulaVaultOpen = true
        },
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 680.dp)
          .align(Alignment.CenterHorizontally)
      )

      // 3. Secondary Modules ("Coming Soon" Frosted Cards)
      Text(
        text = "UPCOMING MODULES",
        style = MaterialTheme.typography.labelMedium.copy(
          fontFamily = JetBrainsMonoFamily,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.2.sp
        ),
        color = palette.textSecondary,
        modifier = Modifier.fillMaxWidth()
      )

      if (metrics.screenWidthDp >= 600) {
        // Tablets (screenWidthDp >= 600.dp): Dual-column adaptive grid rows
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
        ) {
          comingSoonModules.chunked(2).forEach { pair ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(14))
            ) {
              pair.forEach { module ->
                ExtraToolPlaceholderCard(
                  tool = module,
                  metrics = metrics,
                  isComingSoon = true,
                  onClick = {
                    comingSoonSnackbarMessage = "This module is scheduled for the next Sibil update."
                  },
                  modifier = Modifier.weight(1f)
                )
              }
              if (pair.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }
      } else {
        // Phones: Clean 1-column stack
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
        ) {
          comingSoonModules.forEach { module ->
            ExtraToolPlaceholderCard(
              tool = module,
              metrics = metrics,
              isComingSoon = true,
              onClick = {
                comingSoonSnackbarMessage = "This module is scheduled for the next Sibil update."
              },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }

    // Smooth micro-snackbar when tapping any "Coming Soon" module
    if (comingSoonSnackbarMessage != null) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = palette.dialogSurface,
        border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.45f)),
        shadowElevation = 10.dp,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(horizontal = 24.dp, vertical = 96.dp)
          .testTag("coming_soon_micro_snackbar")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = Color(0xFF818CF8),
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = comingSoonSnackbarMessage.orEmpty(),
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold
            ),
            color = palette.textPrimary
          )
        }
      }
    }
  }
}

// ============================================================================
// REBUILT CE FORMULA VAULT (REFERENCE-ONLY CHEAT SHEETS + SHAPE INSPECTOR)
// ============================================================================

private const val PREF_STARRED_FORMULAS_KEY = "ce_formula_vault_starred_ids"
private const val PREF_CUSTOM_FORMULAS_JSON_KEY = "ce_formula_vault_custom_formulas_json"

private data class FormulaVariableNote(
  val symbol: String,
  val meaning: String,
  val unit: String
)

private data class ReferenceFormulaCardData(
  val id: String,
  val title: String,
  val categoryTab: String, // "Mathematics", "PSAD", "HPGE"
  val mathSubTab: String,  // "Algebra", "Trigonometry", "Geometry", "Analytic Geometry", "Custom"
  val equationBanner: String,
  val summaryTag: String,
  val variableNotes: List<FormulaVariableNote>,
  val isCustom: Boolean = false
)

private data class GeometryShapeEntry(
  val id: String,
  val title: String,
  val subtitle: String,
  val is3DSolid: Boolean,
  val areaFormula: String,
  val perimeterOrCircumferenceLabel: String,
  val perimeterOrCircumferenceFormula: String,
  val volumeFormula: String?,
  val surfaceAreaFormula: String?,
  val centroidFromBase: String,
  val momentOfInertiaIx: String,
  val momentOfInertiaIy: String,
  val variableLegend: String
)

private fun loadStarredFormulaIds(context: Context): Set<String> {
  val prefs = context.getSharedPreferences("sibil_app_prefs", Context.MODE_PRIVATE)
  return prefs.getStringSet(PREF_STARRED_FORMULAS_KEY, emptySet())?.toSet() ?: emptySet()
}

private fun saveStarredFormulaIds(context: Context, ids: Set<String>) {
  val prefs = context.getSharedPreferences("sibil_app_prefs", Context.MODE_PRIVATE)
  prefs.edit().putStringSet(PREF_STARRED_FORMULAS_KEY, ids).apply()
}

private fun loadCustomFormulas(context: Context): List<ReferenceFormulaCardData> {
  val prefs = context.getSharedPreferences("sibil_app_prefs", Context.MODE_PRIVATE)
  val rawJson = prefs.getString(PREF_CUSTOM_FORMULAS_JSON_KEY, null) ?: return emptyList()
  return try {
    val array = JSONArray(rawJson)
    val result = mutableListOf<ReferenceFormulaCardData>()
    for (i in 0 until array.length()) {
      val obj = array.optJSONObject(i) ?: continue
      val id = obj.optString("id").ifBlank { "custom_$i" }
      val title = obj.optString("title").trim()
      val categoryTab = obj.optString("categoryTab", "Mathematics").trim()
      val mathSubTab = obj.optString("mathSubTab", "Algebra").trim()
      val equationBanner = obj.optString("equationBanner").trim()
      val variableNotesRaw = obj.optString("variableNotesRaw").trim()
      if (title.isNotEmpty() && equationBanner.isNotEmpty()) {
        val parsedNotes = variableNotesRaw
          .lines()
          .map { it.trim() }
          .filter { it.isNotEmpty() }
          .map { line ->
            val parts = line.split(":", "-", "=").map { it.trim() }
            if (parts.size >= 2) {
              FormulaVariableNote(
                symbol = parts[0],
                meaning = parts.drop(1).joinToString(" - "),
                unit = "SI / Metric"
              )
            } else {
              FormulaVariableNote(
                symbol = "•",
                meaning = line,
                unit = "Reference"
              )
            }
          }
          .ifEmpty {
            listOf(
              FormulaVariableNote(
                symbol = "Note",
                meaning = "Custom scholar formula entry",
                unit = "Standard units"
              )
            )
          }
        result.add(
          ReferenceFormulaCardData(
            id = id,
            title = title,
            categoryTab = categoryTab,
            mathSubTab = mathSubTab,
            equationBanner = equationBanner,
            summaryTag = "Custom Scholar Formula • $mathSubTab",
            variableNotes = parsedNotes,
            isCustom = true
          )
        )
      }
    }
    result
  } catch (_: Exception) {
    emptyList()
  }
}

private fun saveCustomFormulas(context: Context, customList: List<ReferenceFormulaCardData>) {
  val prefs = context.getSharedPreferences("sibil_app_prefs", Context.MODE_PRIVATE)
  val array = JSONArray()
  customList.forEach { item ->
    val obj = JSONObject()
    obj.put("id", item.id)
    obj.put("title", item.title)
    obj.put("categoryTab", item.categoryTab)
    obj.put("mathSubTab", item.mathSubTab)
    obj.put("equationBanner", item.equationBanner)
    val rawLines = item.variableNotes.joinToString("\n") { "${it.symbol}: ${it.meaning}" }
    obj.put("variableNotesRaw", rawLines)
    array.put(obj)
  }
  prefs.edit().putString(PREF_CUSTOM_FORMULAS_JSON_KEY, array.toString()).apply()
}

private fun buildPreloadedMathFormulas(): List<ReferenceFormulaCardData> = listOf(
  // =========================================================================
  // B. ALGEBRA (Interactive Formula Cards)
  // =========================================================================
  ReferenceFormulaCardData(
    id = "alg_quadratic_discriminant",
    title = "Quadratic Equation & Discriminant",
    categoryTab = "Mathematics",
    mathSubTab = "Algebra",
    equationBanner = "x = [-b ± √(b² - 4ac)] / (2a)\nΔ = b² - 4ac",
    summaryTag = "Roots of ax² + bx + c = 0 & Nature of Roots",
    variableNotes = listOf(
      FormulaVariableNote("a, b, c", "Real quadratic, linear, and constant coefficients (a ≠ 0)", "dimensionless"),
      FormulaVariableNote("Δ > 0", "Two distinct real roots (unequal real roots)", "real"),
      FormulaVariableNote("Δ = 0", "One repeated real root (double root / tangent to x-axis)", "real"),
      FormulaVariableNote("Δ < 0", "Two complex conjugate roots (no real intersection)", "complex"),
      FormulaVariableNote("x₁ + x₂ = -b/a", "Vieta's sum of roots relationship", "ratio"),
      FormulaVariableNote("x₁ · x₂ = c/a", "Vieta's product of roots relationship", "ratio")
    )
  ),
  ReferenceFormulaCardData(
    id = "alg_logarithmic_laws",
    title = "Logarithmic Laws & Change of Base",
    categoryTab = "Mathematics",
    mathSubTab = "Algebra",
    equationBanner = "log_b(xy) = log_b(x) + log_b(y)\nlog_b(x/y) = log_b(x) - log_b(y)\nlog_b(xⁿ) = n · log_b(x)\nlog_b(a) = ln(a) / ln(b)",
    summaryTag = "Product, Quotient, Power & Base Conversion Identities",
    variableNotes = listOf(
      FormulaVariableNote("b", "Logarithmic base (b > 0, b ≠ 1)", "base"),
      FormulaVariableNote("x, y, a", "Positive real arguments (x > 0, y > 0, a > 0)", "real"),
      FormulaVariableNote("ln(a)", "Natural logarithm with Euler base e ≈ 2.71828", "rad / nat"),
      FormulaVariableNote("log₁₀(a)", "Common (Briggsian) logarithm base 10", "decadic")
    )
  ),
  ReferenceFormulaCardData(
    id = "alg_arithmetic_progression",
    title = "Arithmetic Progression (AP)",
    categoryTab = "Mathematics",
    mathSubTab = "Algebra",
    equationBanner = "a_n = a₁ + (n - 1)d\nS_n = (n / 2) · [2a₁ + (n - 1)d] = (n / 2)(a₁ + a_n)",
    summaryTag = "Constant Common Difference Sequence & Series Sum",
    variableNotes = listOf(
      FormulaVariableNote("a₁", "First term of the arithmetic sequence", "units"),
      FormulaVariableNote("a_n", "n-th term of the arithmetic sequence", "units"),
      FormulaVariableNote("d", "Common difference between consecutive terms (a_k - a_{k-1})", "units"),
      FormulaVariableNote("n", "Number of terms in the progression", "integer"),
      FormulaVariableNote("S_n", "Cumulative sum of the first n terms", "units"),
      FormulaVariableNote("AM = (a + b)/2", "Arithmetic Mean between two quantities a and b", "units")
    )
  ),
  ReferenceFormulaCardData(
    id = "alg_geometric_progression",
    title = "Geometric Progression (GP) & Infinite Series",
    categoryTab = "Mathematics",
    mathSubTab = "Algebra",
    equationBanner = "a_n = a₁ · r^(n - 1)\nS_n = a₁(1 - rⁿ) / (1 - r)\nS_∞ = a₁ / (1 - r)   for |r| < 1",
    summaryTag = "Constant Common Ratio Sequence, Finite Sum & Infinite Sum",
    variableNotes = listOf(
      FormulaVariableNote("a₁", "First term of the geometric progression", "units"),
      FormulaVariableNote("r", "Common ratio between consecutive terms (a_k / a_{k-1})", "ratio"),
      FormulaVariableNote("n", "Total number of terms", "integer"),
      FormulaVariableNote("S_n", "Sum of the first n terms (r ≠ 1)", "units"),
      FormulaVariableNote("S_∞", "Convergent sum of infinite GP when |r| < 1", "units"),
      FormulaVariableNote("GM = √(a·b)", "Geometric Mean between two positive numbers a and b", "units")
    )
  ),
  ReferenceFormulaCardData(
    id = "alg_binomial_theorem",
    title = "Binomial Theorem & r-th Term Formula",
    categoryTab = "Mathematics",
    mathSubTab = "Algebra",
    equationBanner = "(x + y)ⁿ = Σ C(n, k) · x^(n-k) · y^k\nr-th Term = C(n, r - 1) · x^(n - r + 1) · y^(r - 1)",
    summaryTag = "Binomial Expansion, Combinatorial Coefficient & General r-th Term",
    variableNotes = listOf(
      FormulaVariableNote("n", "Non-negative integer exponent of the binomial power", "integer"),
      FormulaVariableNote("r", "1-indexed position of the desired term in expansion", "integer"),
      FormulaVariableNote("C(n, r-1)", "Binomial coefficient n! / [(r - 1)!(n - r + 1)!]", "combinations"),
      FormulaVariableNote("x, y", "First and second terms of the binomial expression", "algebraic"),
      FormulaVariableNote("Σ Coeff", "Sum of all coefficients obtained by substituting x = 1, y = 1 → 2ⁿ", "scalar")
    )
  ),

  // =========================================================================
  // C. TRIGONOMETRY
  // =========================================================================
  ReferenceFormulaCardData(
    id = "trig_pythagorean_identities",
    title = "Fundamental & Pythagorean Identities",
    categoryTab = "Mathematics",
    mathSubTab = "Trigonometry",
    equationBanner = "sin²θ + cos²θ = 1\n1 + tan²θ = sec²θ\n1 + cot²θ = csc²θ",
    summaryTag = "Core Circular & Reciprocal Trigonometric Relations",
    variableNotes = listOf(
      FormulaVariableNote("θ", "Plane angle in degrees (°) or radians (rad)", "deg / rad"),
      FormulaVariableNote("tan θ", "sin θ / cos θ (Opposite / Adjacent)", "ratio"),
      FormulaVariableNote("sec θ", "1 / cos θ (Hypotenuse / Adjacent)", "ratio"),
      FormulaVariableNote("csc θ", "1 / sin θ (Hypotenuse / Opposite)", "ratio"),
      FormulaVariableNote("cot θ", "cos θ / sin θ = 1 / tan θ", "ratio")
    )
  ),
  ReferenceFormulaCardData(
    id = "trig_double_half_angle",
    title = "Double-Angle & Half-Angle Formulas",
    categoryTab = "Mathematics",
    mathSubTab = "Trigonometry",
    equationBanner = "sin(2θ) = 2 sin θ cos θ\ncos(2θ) = cos²θ - sin²θ = 2cos²θ - 1 = 1 - 2sin²θ\ntan(2θ) = (2 tan θ) / (1 - tan²θ)\nsin(θ/2) = ±√[(1 - cos θ)/2]  •  cos(θ/2) = ±√[(1 + cos θ)/2]",
    summaryTag = "2θ Stress Transformation & θ/2 Reduction Identities",
    variableNotes = listOf(
      FormulaVariableNote("θ", "Reference angle (frequently principal stress plane angle in Mohr's Circle)", "deg / rad"),
      FormulaVariableNote("2θ", "Double angle used in Mohr's Circle principal stress & product of inertia equations", "deg / rad"),
      FormulaVariableNote("sin²θ", "(1 - cos 2θ) / 2 (Power-reduction identity)", "ratio"),
      FormulaVariableNote("cos²θ", "(1 + cos 2θ) / 2 (Power-reduction identity)", "ratio")
    )
  ),
  ReferenceFormulaCardData(
    id = "trig_oblique_triangles",
    title = "Oblique Triangles: Law of Sines & Law of Cosines",
    categoryTab = "Mathematics",
    mathSubTab = "Trigonometry",
    equationBanner = "a / sin A = b / sin B = c / sin C = 2R\nc² = a² + b² - 2ab cos C",
    summaryTag = "Force Polygon Resolution, Surveying Traverse & Truss Geometry",
    variableNotes = listOf(
      FormulaVariableNote("a, b, c", "Side lengths opposite to interior angles A, B, and C", "m / mm"),
      FormulaVariableNote("A, B, C", "Interior angles of the oblique triangle (A + B + C = 180°)", "degrees (°)"),
      FormulaVariableNote("R", "Radius of the circumscribed circle (circumradius) passing through all 3 vertices", "m")
    )
  ),
  ReferenceFormulaCardData(
    id = "trig_triangle_area_formulations",
    title = "Triangle Area Formulations (Standard, Heron, Inradius, Circumradius)",
    categoryTab = "Mathematics",
    mathSubTab = "Trigonometry",
    equationBanner = "A = ½ ab sin C\nA = √[s(s - a)(s - b)(s - c)]   where s = (a + b + c)/2\nA = r · s   (Inradius)  •  A = (a · b · c) / (4R)   (Circumradius)",
    summaryTag = "Surveying Lot Area & Cross-Sectional Area Formulations",
    variableNotes = listOf(
      FormulaVariableNote("A", "Total enclosed planar area of the triangle", "m²"),
      FormulaVariableNote("s", "Semi-perimeter of the triangle: s = (a + b + c) / 2", "m"),
      FormulaVariableNote("r", "Inradius (radius of inscribed circle tangent to all three sides)", "m"),
      FormulaVariableNote("R", "Circumradius (radius of circumscribed circle through all three vertices)", "m")
    )
  ),

  // =========================================================================
  // D. ANALYTIC GEOMETRY
  // =========================================================================
  ReferenceFormulaCardData(
    id = "analytic_lines_and_distance",
    title = "Lines: Distance, Slope, Forms & Point-to-Line Distance",
    categoryTab = "Mathematics",
    mathSubTab = "Analytic Geometry",
    equationBanner = "d = √[(x₂ - x₁)² + (y₂ - y₁)²]  •  m = (y₂ - y₁) / (x₂ - x₁)\ny - y₁ = m(x - x₁)  •  Ax + By + C = 0\nd_⊥ = |Ax₁ + By₁ + C| / √(A² + B²)",
    summaryTag = "Two-Point Distance, Gradient, Point-Slope, General Form & Perpendicular Offset",
    variableNotes = listOf(
      FormulaVariableNote("(x₁, y₁), (x₂, y₂)", "Cartesian coordinates of points P₁ and P₂", "m"),
      FormulaVariableNote("m", "Slope / grade of the line (m = tan θ); parallel: m₁ = m₂, perpendicular: m₁·m₂ = -1", "m/m"),
      FormulaVariableNote("A, B, C", "Coefficients of the General Linear Equation Ax + By + C = 0", "constants"),
      FormulaVariableNote("d_⊥", "Shortest perpendicular distance from point (x₁, y₁) to line Ax + By + C = 0", "m")
    )
  ),
  ReferenceFormulaCardData(
    id = "analytic_conic_circle",
    title = "Conic Sections: Circle",
    categoryTab = "Mathematics",
    mathSubTab = "Analytic Geometry",
    equationBanner = "(x - h)² + (y - k)² = r²\nx² + y² + Dx + Ey + F = 0   (e = 0)",
    summaryTag = "Center-Radius Form & General Second-Degree Equation",
    variableNotes = listOf(
      FormulaVariableNote("(h, k)", "Center coordinates of the circle: h = -D/2, k = -E/2", "m"),
      FormulaVariableNote("r", "Radius of the circle: r = ½√(D² + E² - 4F)", "m"),
      FormulaVariableNote("e = 0", "Eccentricity of a true circle is strictly zero", "dimensionless")
    )
  ),
  ReferenceFormulaCardData(
    id = "analytic_conic_parabola",
    title = "Conic Sections: Parabola (Arch & Cable Profile)",
    categoryTab = "Mathematics",
    mathSubTab = "Analytic Geometry",
    equationBanner = "(x - h)² = ±4p(y - k)   [Vertical Axis]\n(y - k)² = ±4p(x - h)   [Horizontal Axis]\nLatus Rectum LR = |4p|  •  Eccentricity e = 1",
    summaryTag = "Vertex, Focus, Directrix & Latus Rectum for Parabolic Arches/Cables",
    variableNotes = listOf(
      FormulaVariableNote("(h, k)", "Coordinates of the parabola vertex V", "m"),
      FormulaVariableNote("p", "Directed focal distance from vertex V to focus F (and vertex to directrix)", "m"),
      FormulaVariableNote("Focus F", "(h, k ± p) for vertical axis; (h ± p, k) for horizontal axis", "m"),
      FormulaVariableNote("Directrix", "y = k ∓ p (vertical axis) or x = h ∓ p (horizontal axis)", "m"),
      FormulaVariableNote("LR = |4p|", "Length of latus rectum (focal chord perpendicular to axis)", "m")
    )
  ),
  ReferenceFormulaCardData(
    id = "analytic_conic_ellipse",
    title = "Conic Sections: Ellipse",
    categoryTab = "Mathematics",
    mathSubTab = "Analytic Geometry",
    equationBanner = "(x - h)² / a² + (y - k)² / b² = 1\na² = b² + c²  •  e = c / a < 1  •  LR = 2b² / a",
    summaryTag = "Semi-Major/Minor Axes, Focal Relation & Eccentricity (e < 1)",
    variableNotes = listOf(
      FormulaVariableNote("(h, k)", "Center coordinates of the ellipse", "m"),
      FormulaVariableNote("a", "Semi-major axis length (major axis = 2a, always a > b)", "m"),
      FormulaVariableNote("b", "Semi-minor axis length (minor axis = 2b)", "m"),
      FormulaVariableNote("c", "Center-to-focus distance: c = √(a² - b²)", "m"),
      FormulaVariableNote("e = c/a", "Eccentricity (0 < e < 1); Directrix distance from center d = a / e", "ratio")
    )
  ),
  ReferenceFormulaCardData(
    id = "analytic_conic_hyperbola",
    title = "Conic Sections: Hyperbola",
    categoryTab = "Mathematics",
    mathSubTab = "Analytic Geometry",
    equationBanner = "(x - h)² / a² - (y - k)² / b² = 1\nc² = a² + b²  •  e = c / a > 1\nAsymptotes: y - k = ±(b / a)(x - h)",
    summaryTag = "Transverse/Conjugate Axes, Asymptotes & Eccentricity (e > 1)",
    variableNotes = listOf(
      FormulaVariableNote("(h, k)", "Center coordinates of the hyperbola", "m"),
      FormulaVariableNote("a", "Semi-transverse axis length (distance from center to vertex)", "m"),
      FormulaVariableNote("b", "Semi-conjugate axis length", "m"),
      FormulaVariableNote("c", "Center-to-focus distance: c = √(a² + b²)", "m"),
      FormulaVariableNote("e = c/a", "Eccentricity (strictly e > 1); Latus Rectum LR = 2b² / a", "ratio")
    )
  )
)

private fun buildGeometryShapesCatalog(): List<GeometryShapeEntry> = listOf(
  GeometryShapeEntry(
    id = "circle",
    title = "Circle",
    subtitle = "2D Circular Cross-Section",
    is3DSolid = false,
    areaFormula = "A = πr² = (πd²) / 4",
    perimeterOrCircumferenceLabel = "Circumference (C)",
    perimeterOrCircumferenceFormula = "C = 2πr = πd",
    volumeFormula = null,
    surfaceAreaFormula = null,
    centroidFromBase = "ȳ = r = d / 2  (from bottom tangent)",
    momentOfInertiaIx = "I_x = (πr⁴) / 4 = (πd⁴) / 64",
    momentOfInertiaIy = "I_y = (πr⁴) / 4 = (πd⁴) / 64  •  J = (πr⁴) / 2",
    variableLegend = "r = radius, d = diameter, J = polar moment of inertia"
  ),
  GeometryShapeEntry(
    id = "right_triangle",
    title = "Right Triangle",
    subtitle = "2D Wedge / Load Distribution",
    is3DSolid = false,
    areaFormula = "A = ½ · b · h",
    perimeterOrCircumferenceLabel = "Perimeter (P)",
    perimeterOrCircumferenceFormula = "P = b + h + √(b² + h²)",
    volumeFormula = null,
    surfaceAreaFormula = null,
    centroidFromBase = "ȳ = h / 3  (from base b)  •  x̄ = b / 3  (from vertical leg h)",
    momentOfInertiaIx = "I_x(centroid) = (b · h³) / 36  •  I_x(base) = (b · h³) / 12",
    momentOfInertiaIy = "I_y(centroid) = (h · b³) / 36",
    variableLegend = "b = base width, h = perpendicular altitude"
  ),
  GeometryShapeEntry(
    id = "isosceles_triangle",
    title = "Isosceles Triangle",
    subtitle = "2D Symmetric Triangular Section",
    is3DSolid = false,
    areaFormula = "A = ½ · b · h",
    perimeterOrCircumferenceLabel = "Perimeter (P)",
    perimeterOrCircumferenceFormula = "P = b + 2√[(b/2)² + h²]",
    volumeFormula = null,
    surfaceAreaFormula = null,
    centroidFromBase = "ȳ = h / 3  (above base b)  •  x̄ = b / 2  (axis of symmetry)",
    momentOfInertiaIx = "I_x(centroid) = (b · h³) / 36  •  I_x(base) = (b · h³) / 12",
    momentOfInertiaIy = "I_y(centroid) = (h · b³) / 48",
    variableLegend = "b = base width, h = altitude from base to apex"
  ),
  GeometryShapeEntry(
    id = "trapezoid",
    title = "Trapezoid",
    subtitle = "2D Open Channel / Retaining Wall",
    is3DSolid = false,
    areaFormula = "A = ½ · (a + b) · h",
    perimeterOrCircumferenceLabel = "Perimeter (P)",
    perimeterOrCircumferenceFormula = "P = a + b + s₁ + s₂",
    volumeFormula = null,
    surfaceAreaFormula = null,
    centroidFromBase = "ȳ = [h · (2a + b)] / [3 · (a + b)]  (measured from bottom base b)",
    momentOfInertiaIx = "I_x(centroid) = [h³ · (a² + 4ab + b²)] / [36 · (a + b)]",
    momentOfInertiaIy = "I_x(base) = [h³ · (3a + b)] / 12",
    variableLegend = "b = bottom base width, a = top parallel width, h = vertical height, s₁,s₂ = side slopes"
  ),
  GeometryShapeEntry(
    id = "parallelogram",
    title = "Parallelogram",
    subtitle = "2D Skewed Structural Section",
    is3DSolid = false,
    areaFormula = "A = b · h = a · b · sin θ",
    perimeterOrCircumferenceLabel = "Perimeter (P)",
    perimeterOrCircumferenceFormula = "P = 2(a + b)",
    volumeFormula = null,
    surfaceAreaFormula = null,
    centroidFromBase = "ȳ = h / 2  (from bottom base b)",
    momentOfInertiaIx = "I_x(centroid) = (b · h³) / 12  •  I_x(base) = (b · h³) / 3",
    momentOfInertiaIy = "I_y(centroid) = (h · b / 12) · (b² + a² cos²θ)",
    variableLegend = "b = base length, a = inclined side length, h = perpendicular height, θ = skew angle"
  ),
  GeometryShapeEntry(
    id = "cylinder",
    title = "Cylinder",
    subtitle = "3D Pier / Concrete Test Cylinder",
    is3DSolid = true,
    areaFormula = "A_cross = πr² = (πd²) / 4",
    perimeterOrCircumferenceLabel = "Base Circumference (C)",
    perimeterOrCircumferenceFormula = "C = 2πr = πd",
    volumeFormula = "V = πr²h = (πd²h) / 4",
    surfaceAreaFormula = "A_lateral = 2πrh  •  A_total = 2πr(r + h)",
    centroidFromBase = "ȳ = h / 2  (from circular base)",
    momentOfInertiaIx = "I_x(section) = (πr⁴) / 4  •  I_mass,x = (1/12)m(3r² + h²)",
    momentOfInertiaIy = "I_polar,z = ½ · m · r²",
    variableLegend = "r = radius, d = diameter, h = cylinder height"
  ),
  GeometryShapeEntry(
    id = "sphere",
    title = "Sphere",
    subtitle = "3D Tank /Dome Geometry",
    is3DSolid = true,
    areaFormula = "A_great_circle = πr²",
    perimeterOrCircumferenceLabel = "Great Circle Circumference (C)",
    perimeterOrCircumferenceFormula = "C = 2πr",
    volumeFormula = "V = (4/3)πr³ = (πd³) / 6",
    surfaceAreaFormula = "A_s = 4πr² = πd²",
    centroidFromBase = "ȳ = r  (from bottom pole)  •  Hemisphere: ȳ = (3/8)r from flat base",
    momentOfInertiaIx = "I_diametral = (2/5) · m · r²  (solid sphere)",
    momentOfInertiaIy = "I_shell = (2/3) · m · r²  (thin spherical shell)",
    variableLegend = "r = sphere radius, d = diameter, m = mass"
  ),
  GeometryShapeEntry(
    id = "cone",
    title = "Cone",
    subtitle = "3D Aggregate Stockpile / Hopper",
    is3DSolid = true,
    areaFormula = "A_base = πr²   •   Slant s = √(r² + h²)",
    perimeterOrCircumferenceLabel = "Base Circumference (C)",
    perimeterOrCircumferenceFormula = "C = 2πr",
    volumeFormula = "V = (1/3)πr²h",
    surfaceAreaFormula = "A_lateral = πrs = πr√(r² + h²)  •  A_total = πr(r + s)",
    centroidFromBase = "ȳ = h / 4  (solid cone from base)  •  ȳ_shell = h / 3  (conical shell)",
    momentOfInertiaIx = "I_z(axis) = (3/10) · m · r²",
    momentOfInertiaIy = "I_x(base) = m · [(3/20)r² + (1/10)h²]",
    variableLegend = "r = base radius, h = vertical height, s = slant height"
  ),
  GeometryShapeEntry(
    id = "frustum",
    title = "Frustum",
    subtitle = "3D Slump Cone / Tapered Pier",
    is3DSolid = true,
    areaFormula = "A₁ = πR² (bottom)  •  A₂ = πr² (top)  •  s = √[(R - r)² + h²]",
    perimeterOrCircumferenceLabel = "Circumferences (C₁, C₂)",
    perimeterOrCircumferenceFormula = "C₁ = 2πR   •   C₂ = 2πr",
    volumeFormula = "V = (πh / 3) · (R² + R·r + r²) = (h / 3)(A₁ + A₂ + √(A₁A₂))",
    surfaceAreaFormula = "A_lateral = π(R + r)s  •  A_total = π[(R + r)s + R² + r²]",
    centroidFromBase = "ȳ = [h · (R² + 2Rr + 3r²)] / [4 · (R² + Rr + r²)]  (from bottom base R)",
    momentOfInertiaIx = "I_section(top/bottom) = (π/4)(R⁴ or r⁴)",
    momentOfInertiaIy = "Prismoidal Equivalent: V = (h / 6)(A₁ + 4A_m + A₂)",
    variableLegend = "R = bottom base radius, r = top radius, h = frustum altitude, s = slant height"
  )
)

private enum class FormulaVaultTier {
  MASTER_VOLUMES,
  MSTE_SUB_SHELF,
  ALGEBRA_A4_SHEET,
  TRIGONOMETRY_A4_SHEET
}

private data class EngineeringNotebookSpec(
  val id: String,
  val title: String,
  val fullTitle: String,
  val subtitle: String,
  val departmentCode: String,
  val pageBadge: String,
  val isActive: Boolean,
  val spineAccentColor: Color,
  val secondaryAccentColor: Color
)

private fun buildMasterBoardVolumes(): List<EngineeringNotebookSpec> = listOf(
  EngineeringNotebookSpec(
    id = "mste",
    title = "MSTE",
    fullTitle = "",
    subtitle = "Mathematics & Surveying",
    departmentCode = "PRC BOARD PILLAR I • MSTE",
    pageBadge = "ACTIVE • 4 SUB-VOLUMES",
    isActive = true,
    spineAccentColor = Color(0xFF6366F1),
    secondaryAccentColor = Color(0xFF8B5CF6)
  ),
  EngineeringNotebookSpec(
    id = "psad",
    title = "PSAD",
    fullTitle = "",
    subtitle = "Structural Analysis & Design",
    departmentCode = "PRC BOARD PILLAR II • PSAD",
    pageBadge = "COMING SOON",
    isActive = false,
    spineAccentColor = Color(0xFFF59E0B),
    secondaryAccentColor = Color(0xFFEF4444)
  ),
  EngineeringNotebookSpec(
    id = "hpge",
    title = "HPGE",
    fullTitle = "",
    subtitle = "Hydraulics & Geotechnical",
    departmentCode = "PRC BOARD PILLAR III • HPGE",
    pageBadge = "COMING SOON",
    isActive = false,
    spineAccentColor = Color(0xFF06B6D4),
    secondaryAccentColor = Color(0xFF3B82F6)
  )
)

private fun buildMsteSubNotebooks(): List<EngineeringNotebookSpec> = listOf(
  EngineeringNotebookSpec(
    id = "algebra",
    title = "Algebra",
    fullTitle = "",
    subtitle = "Engineering Algebra & Number Theory",
    departmentCode = "MSTE / CE-01",
    pageBadge = "ACTIVE • A4 SHEET",
    isActive = true,
    spineAccentColor = Color(0xFF6366F1),
    secondaryAccentColor = Color(0xFF8B5CF6)
  ),
  EngineeringNotebookSpec(
    id = "trigonometry",
    title = "Trigonometry",
    fullTitle = "",
    subtitle = "Plane & Spherical Trigonometry",
    departmentCode = "MSTE / CE-02",
    pageBadge = "ACTIVE • 12 SECTIONS",
    isActive = true,
    spineAccentColor = Color(0xFF3B82F6),
    secondaryAccentColor = Color(0xFF06B6D4)
  ),
  EngineeringNotebookSpec(
    id = "geometry",
    title = "Geometry",
    fullTitle = "",
    subtitle = "Plane & Solid Mensuration",
    departmentCode = "MSTE / CE-03",
    pageBadge = "COMING SOON",
    isActive = false,
    spineAccentColor = Color(0xFF10B981),
    secondaryAccentColor = Color(0xFF14B8A6)
  ),
  EngineeringNotebookSpec(
    id = "analytic_geometry",
    title = "Analytic Geometry",
    fullTitle = "",
    subtitle = "Cartesian & Conic Sections",
    departmentCode = "MSTE / CE-04",
    pageBadge = "COMING SOON",
    isActive = false,
    spineAccentColor = Color(0xFF8B5CF6),
    secondaryAccentColor = Color(0xFFEC4899)
  )
)

@Composable
fun FormulaVaultMainView(
  metrics: ResponsiveMetrics,
  onBackToExtras: () -> Unit,
  viewModel: TaskViewModel? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val localView = LocalView.current
  val palette = currentGlassPalette()
  val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

  // 3-Tier PRC Board Exam Hierarchy State:
  // "MASTER_VOLUMES" -> "MSTE_SUB_SHELF" -> "ALGEBRA_A4_SHEET" / "TRIGONOMETRY_A4_SHEET"
  var currentTierName by rememberSaveable { mutableStateOf(FormulaVaultTier.MASTER_VOLUMES.name) }
  val currentTier = remember(currentTierName) {
    runCatching { FormulaVaultTier.valueOf(currentTierName) }.getOrDefault(FormulaVaultTier.MASTER_VOLUMES)
  }

  var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var isAddCustomDialogOpen by rememberSaveable { mutableStateOf(false) }
  var showTocSheet by rememberSaveable { mutableStateOf(false) }
  var showHolographicTriangleModal by rememberSaveable { mutableStateOf(false) }
  var highlightedSectionIndex by remember { mutableStateOf<Int?>(null) }

  val a4LazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
  val trigLazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

  // Automatically clear the arrival pulse glow after 1.8 seconds
  androidx.compose.runtime.LaunchedEffect(highlightedSectionIndex) {
    if (highlightedSectionIndex != null) {
      kotlinx.coroutines.delay(1800L)
      highlightedSectionIndex = null
    }
  }

  var starredIds by remember { mutableStateOf(loadStarredFormulaIds(context)) }
  var customFormulas by remember { mutableStateOf(loadCustomFormulas(context)) }

  val masterVolumes = remember { buildMasterBoardVolumes() }
  val msteSubNotebooks = remember { buildMsteSubNotebooks() }
  val algebraSections = remember { buildAlgebraHandbookSections() }
  val trigonometrySections = remember { buildTrigonometryHandbookSections() }

  fun navigateBackOneTier() {
    playCardHapticSound(context, localView)
    when (currentTier) {
      FormulaVaultTier.ALGEBRA_A4_SHEET,
      FormulaVaultTier.TRIGONOMETRY_A4_SHEET -> {
        currentTierName = FormulaVaultTier.MSTE_SUB_SHELF.name
      }
      FormulaVaultTier.MSTE_SUB_SHELF -> {
        currentTierName = FormulaVaultTier.MASTER_VOLUMES.name
      }
      FormulaVaultTier.MASTER_VOLUMES -> {
        onBackToExtras()
      }
    }
  }

  BackHandler {
    when {
      showHolographicTriangleModal -> showHolographicTriangleModal = false
      showTocSheet -> showTocSheet = false
      isSearchExpanded && searchQuery.isNotEmpty() -> {
        searchQuery = ""
        isSearchExpanded = false
      }
      else -> navigateBackOneTier()
    }
  }

  fun toggleStarFormula(formulaId: String) {
    playCardHapticSound(context, localView)
    val updated = if (starredIds.contains(formulaId)) {
      starredIds - formulaId
    } else {
      starredIds + formulaId
    }
    starredIds = updated
    saveStarredFormulaIds(context, updated)
  }

  val topBarTitle = when (currentTier) {
    FormulaVaultTier.MASTER_VOLUMES -> "CE Formula Vault"
    FormulaVaultTier.MSTE_SUB_SHELF -> "MSTE Library"
    FormulaVaultTier.ALGEBRA_A4_SHEET -> "MSTE • Algebra"
    FormulaVaultTier.TRIGONOMETRY_A4_SHEET -> "MSTE • Trigonometry"
  }

  val trimmedQuery = searchQuery.trim()
  val algebraMatchesQuery = remember(trimmedQuery, algebraSections) {
    if (trimmedQuery.isEmpty()) false
    else algebraSections.any { sec ->
      sec.sectionTitle.contains(trimmedQuery, ignoreCase = true) ||
        sec.items.any { item ->
          item.title.contains(trimmedQuery, ignoreCase = true) ||
            item.formulaOrStatement.contains(trimmedQuery, ignoreCase = true) ||
            item.note.contains(trimmedQuery, ignoreCase = true) ||
            item.subItems.any { it.contains(trimmedQuery, ignoreCase = true) }
        }
    }
  }

  val trigMatchesQuery = remember(trimmedQuery, trigonometrySections) {
    if (trimmedQuery.isEmpty()) false
    else trigonometrySections.any { sec ->
      sec.sectionTitle.contains(trimmedQuery, ignoreCase = true) ||
        sec.items.any { item ->
          item.title.contains(trimmedQuery, ignoreCase = true) ||
            item.formulaOrStatement.contains(trimmedQuery, ignoreCase = true) ||
            item.note.contains(trimmedQuery, ignoreCase = true) ||
            item.subItems.any { it.contains(trimmedQuery, ignoreCase = true) }
        }
    }
  }

  val noShelfMatchesQuery = trimmedQuery.isNotEmpty() &&
    masterVolumes.none {
      it.title.contains(trimmedQuery, ignoreCase = true) ||
        it.fullTitle.contains(trimmedQuery, ignoreCase = true)
    } &&
    msteSubNotebooks.none {
      it.title.contains(trimmedQuery, ignoreCase = true)
    }

  val showTrigDirectlyFromSearch = noShelfMatchesQuery && trigMatchesQuery && !algebraMatchesQuery
  val showAlgebraDirectlyFromSearch = noShelfMatchesQuery && algebraMatchesQuery

  val isShowingTrigSheet = currentTier == FormulaVaultTier.TRIGONOMETRY_A4_SHEET ||
    (showTrigDirectlyFromSearch && currentTier != FormulaVaultTier.ALGEBRA_A4_SHEET)
  val isShowingAlgebraSheet = !isShowingTrigSheet &&
    (currentTier == FormulaVaultTier.ALGEBRA_A4_SHEET || showAlgebraDirectlyFromSearch)

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("formula_vault_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = metrics.maxContentWidth)
        .align(Alignment.TopCenter)
        .padding(
          start = metrics.scaledDp(18),
          end = metrics.scaledDp(18),
          top = metrics.scaledDp(16)
        ),
      verticalArrangement = Arrangement.spacedBy(metrics.scaledDp(12))
    ) {
      // ====================================================================
      // 1. MINIMAL HEADER & TOP BAR (WITH TOC, +, AND SEARCH ACTIONS)
      // ====================================================================
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .weight(1f)
            .padding(end = 6.dp)
        ) {
          // Left: Circular glass Back navigation button
          Surface(
            onClick = { navigateBackOneTier() },
            shape = CircleShape,
            color = palette.cardFill,
            border = palette.glassBorder,
            shadowElevation = 6.dp,
            modifier = Modifier
              .size(38.dp)
              .testTag("formula_vault_back_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = when (currentTier) {
                  FormulaVaultTier.ALGEBRA_A4_SHEET,
                  FormulaVaultTier.TRIGONOMETRY_A4_SHEET -> "Back to MSTE Library"
                  FormulaVaultTier.MSTE_SUB_SHELF -> "Back to Master Volumes"
                  FormulaVaultTier.MASTER_VOLUMES -> "Back to Extras"
                },
                tint = palette.textPrimary,
                modifier = Modifier.size(22.dp)
              )
            }
          }

          // Dynamic Single-Line Title ("CE Formula Vault" / "MSTE Library" / "MSTE • Algebra" / "MSTE • Trigonometry")
          Text(
            text = topBarTitle,
            style = MaterialTheme.typography.titleLarge.copy(
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            ),
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Right: Compact Table of Contents (TOC) button, + Custom Formula button, & Search button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Dedicated Table of Contents (TOC) Top Bar Button
          Surface(
            onClick = {
              playCardHapticSound(context, localView)
              showTocSheet = true
            },
            shape = CircleShape,
            color = if (showTocSheet) Color(0xFF6366F1).copy(alpha = 0.18f) else palette.cardFill,
            border = if (showTocSheet) {
              BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.65f))
            } else {
              palette.glassBorder
            },
            shadowElevation = 4.dp,
            modifier = Modifier
              .size(38.dp)
              .testTag("formula_vault_toc_top_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ViewList,
                contentDescription = "Table of Contents",
                tint = if (showTocSheet) palette.accentIndigo else palette.textPrimary,
                modifier = Modifier.size(19.dp)
              )
            }
          }

          Surface(
            onClick = {
              playCardHapticSound(context, localView)
              isAddCustomDialogOpen = true
            },
            shape = CircleShape,
            color = palette.cardFill,
            border = palette.glassBorder,
            shadowElevation = 4.dp,
            modifier = Modifier
              .size(38.dp)
              .testTag("add_custom_formula_top_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Custom Formula",
                tint = palette.accentIndigo,
                modifier = Modifier.size(19.dp)
              )
            }
          }

          Surface(
            onClick = {
              playCardHapticSound(context, localView)
              isSearchExpanded = !isSearchExpanded
              if (!isSearchExpanded) {
                searchQuery = ""
              }
            },
            shape = CircleShape,
            color = if (isSearchExpanded) Color(0xFF6366F1).copy(alpha = 0.18f) else palette.cardFill,
            border = if (isSearchExpanded) {
              BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.65f))
            } else {
              palette.glassBorder
            },
            shadowElevation = 4.dp,
            modifier = Modifier
              .size(38.dp)
              .testTag("formula_vault_search_toggle_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                contentDescription = if (isSearchExpanded) "Close Search" else "Search Formulas",
                tint = if (isSearchExpanded) palette.accentIndigo else palette.textPrimary,
                modifier = Modifier.size(19.dp)
              )
            }
          }
        }
      }

      // Smoothly expanding inline search bar when Search icon is tapped
      AnimatedVisibility(
        visible = isSearchExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        LiquidGlassCard(
          shape = RoundedCornerShape(18.dp),
          elevation = 4.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
              Text(
                text = when (currentTier) {
                  FormulaVaultTier.TRIGONOMETRY_A4_SHEET ->
                    "Filter Trigonometry laws, triangle area, medians, bisectors, or identities..."
                  FormulaVaultTier.ALGEBRA_A4_SHEET ->
                    "Filter Algebra laws, equations, or symbols (e.g., quadratic, log, radical)..."
                  FormulaVaultTier.MSTE_SUB_SHELF ->
                    "Search MSTE sub-volumes, Algebra, or Trigonometry laws..."
                  FormulaVaultTier.MASTER_VOLUMES ->
                    "Search PRC board subjects (MSTE, PSAD, HPGE) or MSTE formulas..."
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = palette.textSecondary
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = palette.accentIndigo,
                modifier = Modifier.size(20.dp)
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear search",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
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
      }

      // ====================================================================
      // 2. 3-TIER HIERARCHY VIEW SWITCHER
      // ====================================================================
      when {
        isShowingTrigSheet -> {
          val customTrigFormulas = customFormulas.filter {
            it.categoryTab.equals("Mathematics", ignoreCase = true) &&
              it.mathSubTab.equals("Trigonometry", ignoreCase = true)
          }
          MsteTrigonometrySheet(
            metrics = metrics,
            lazyListState = trigLazyListState,
            highlightedSectionIndex = highlightedSectionIndex,
            searchQuery = trimmedQuery,
            customFormulas = customTrigFormulas,
            starredIds = starredIds,
            onToggleStar = { toggleStarFormula(it) },
            onBackToShelf = {
              playCardHapticSound(context, localView)
              currentTierName = FormulaVaultTier.MSTE_SUB_SHELF.name
            },
            onOpenToc = {
              playCardHapticSound(context, localView)
              showTocSheet = true
            },
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          )
        }

        isShowingAlgebraSheet -> {
          val customAlgebraFormulas = customFormulas.filter {
            it.categoryTab.equals("Mathematics", ignoreCase = true) &&
              it.mathSubTab.equals("Algebra", ignoreCase = true)
          }
          AlgebraA4PaperViewer(
            metrics = metrics,
            lazyListState = a4LazyListState,
            highlightedSectionIndex = highlightedSectionIndex,
            searchQuery = trimmedQuery,
            customFormulas = customAlgebraFormulas,
            starredIds = starredIds,
            onToggleStar = { toggleStarFormula(it) },
            onBackToShelf = {
              playCardHapticSound(context, localView)
              currentTierName = FormulaVaultTier.MSTE_SUB_SHELF.name
            },
            onOpenToc = {
              playCardHapticSound(context, localView)
              showTocSheet = true
            },
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          )
        }

        currentTier == FormulaVaultTier.MSTE_SUB_SHELF -> {
          NotebookShelfLibraryView(
            shelfHeaderTitle = "MSTE SUB-NOTEBOOK SHELF",
            shelfCountBadge = "4 SUB-VOLUMES",
            notebooks = msteSubNotebooks,
            searchQuery = trimmedQuery,
            customFormulasCount = customFormulas.size,
            metrics = metrics,
            breadcrumbLabel = "All PRC Board Pillars (MSTE • PSAD • HPGE)",
            onBreadcrumbClick = {
              playCardHapticSound(context, localView)
              currentTierName = FormulaVaultTier.MASTER_VOLUMES.name
            },
            onOpenNotebook = { notebook ->
              playCardHapticSound(context, localView)
              when {
                notebook.isActive && notebook.id == "algebra" -> {
                  currentTierName = FormulaVaultTier.ALGEBRA_A4_SHEET.name
                }
                notebook.isActive && notebook.id == "trigonometry" -> {
                  currentTierName = FormulaVaultTier.TRIGONOMETRY_A4_SHEET.name
                }
                else -> {
                  Toast.makeText(
                    context,
                    "${notebook.title} (${notebook.departmentCode}) reference sheet is coming soon!",
                    Toast.LENGTH_SHORT
                  ).show()
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          )
        }

        else -> {
          NotebookShelfLibraryView(
            shelfHeaderTitle = "PRC CE LICENSURE EXAM PILLARS",
            shelfCountBadge = "3 MASTER VOLUMES",
            notebooks = masterVolumes,
            searchQuery = trimmedQuery,
            customFormulasCount = customFormulas.size,
            metrics = metrics,
            breadcrumbLabel = null,
            onBreadcrumbClick = null,
            onOpenNotebook = { volume ->
              playCardHapticSound(context, localView)
              if (volume.isActive && volume.id == "mste") {
                currentTierName = FormulaVaultTier.MSTE_SUB_SHELF.name
              } else {
                Toast.makeText(
                  context,
                  "${volume.title} — ${volume.fullTitle} master volume is coming soon!",
                  Toast.LENGTH_SHORT
                ).show()
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          )
        }
      }
    }

    // ====================================================================
    // INTERACTIVE "HOLOGRAPHIC TRIANGLE" INSPECTOR FLOATING TRIGGER ICON
    // Positioned at lower-left corner (bottom = 24.dp, start = 20.dp)
    // ====================================================================
    val holoPulseTransition = rememberInfiniteTransition(label = "holographic_triangle_pulse")
    val holoGlowAlpha by holoPulseTransition.animateFloat(
      initialValue = 0.42f,
      targetValue = 0.92f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "holo_glow_alpha"
    )
    val holoPulseScale by holoPulseTransition.animateFloat(
      initialValue = 1.0f,
      targetValue = 1.06f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "holo_pulse_scale"
    )

    Surface(
      onClick = {
        playCardHapticSound(context, localView)
        showHolographicTriangleModal = true
      },
      shape = CircleShape,
      color = if (palette.isDark) {
        Color(0xFF0F172A).copy(alpha = 0.90f)
      } else {
        Color(0xFFF8FAFC).copy(alpha = 0.94f)
      },
      border = BorderStroke(
        width = 1.4.dp,
        brush = Brush.linearGradient(
          listOf(
            Color(0xFF06B6D4).copy(alpha = holoGlowAlpha),
            Color.White.copy(alpha = if (palette.isDark) 0.55f else 0.85f),
            Color(0xFF3B82F6).copy(alpha = holoGlowAlpha * 0.85f)
          )
        )
      ),
      shadowElevation = 10.dp,
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(bottom = 24.dp, start = 20.dp)
        .size(52.dp)
        .scale(holoPulseScale)
        .testTag("holographic_triangle_trigger_button")
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            brush = Brush.radialGradient(
              colors = listOf(
                Color(0xFF06B6D4).copy(alpha = 0.24f * holoGlowAlpha),
                Color.Transparent
              )
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ChangeHistory,
          contentDescription = "Interactive Triangle Anatomy Inspector",
          tint = Color(0xFF06B6D4),
          modifier = Modifier.size(26.dp)
        )
      }
    }

    // Frosted Glass Table of Contents (TOC) Quick-Jump Sheet
    if (showTocSheet) {
      val activeTocIsTrig = currentTier == FormulaVaultTier.TRIGONOMETRY_A4_SHEET || isShowingTrigSheet
      FormulaVaultTocGlassSheet(
        sections = if (activeTocIsTrig) trigonometrySections else algebraSections,
        sheetSubtitle = if (activeTocIsTrig) {
          "CIVIL ENGINEERING • TRIGONOMETRY CHEAT SHEET"
        } else {
          "CIVIL ENGINEERING • ALGEBRA CHEAT SHEET"
        },
        onDismiss = { showTocSheet = false },
        onSelectSection = { sectionZeroBasedIndex ->
          playCardHapticSound(context, localView)
          showTocSheet = false
          if (searchQuery.isNotEmpty()) {
            searchQuery = ""
            isSearchExpanded = false
          }
          if (activeTocIsTrig) {
            currentTierName = FormulaVaultTier.TRIGONOMETRY_A4_SHEET.name
            highlightedSectionIndex = sectionZeroBasedIndex
            val targetIndex = sectionZeroBasedIndex + 1
            coroutineScope.launch {
              kotlinx.coroutines.delay(60L)
              trigLazyListState.animateScrollToItem(index = targetIndex)
            }
          } else {
            currentTierName = FormulaVaultTier.ALGEBRA_A4_SHEET.name
            highlightedSectionIndex = sectionZeroBasedIndex
            val targetIndex = sectionZeroBasedIndex + 1
            coroutineScope.launch {
              kotlinx.coroutines.delay(60L)
              a4LazyListState.animateScrollToItem(index = targetIndex)
            }
          }
        }
      )
    }

    if (showHolographicTriangleModal) {
      HolographicTriangleModal(
        onDismiss = { showHolographicTriangleModal = false }
      )
    }

    if (isAddCustomDialogOpen) {
      AddCustomFormulaGlassDialog(
        metrics = metrics,
        onDismiss = { isAddCustomDialogOpen = false },
        onSaveFormula = { title, categoryTab, mathSubTab, equation, variableNotesRaw ->
          val parsedNotes = variableNotesRaw
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { line ->
              val parts = line.split(":", "-", "=").map { it.trim() }
              if (parts.size >= 2) {
                FormulaVariableNote(
                  symbol = parts[0],
                  meaning = parts.drop(1).joinToString(" - "),
                  unit = "SI / Metric"
                )
              } else {
                FormulaVariableNote(
                  symbol = "•",
                  meaning = line,
                  unit = "Reference"
                )
              }
            }
            .ifEmpty {
              listOf(
                FormulaVariableNote(
                  symbol = "Note",
                  meaning = "Custom scholar formula entry",
                  unit = "Standard units"
                )
              )
            }
          val newEntry = ReferenceFormulaCardData(
            id = "custom_${System.currentTimeMillis()}",
            title = title,
            categoryTab = categoryTab,
            mathSubTab = mathSubTab,
            equationBanner = equation,
            summaryTag = "Custom Scholar Formula • $mathSubTab",
            variableNotes = parsedNotes,
            isCustom = true
          )
          val updatedList = listOf(newEntry) + customFormulas
          customFormulas = updatedList
          saveCustomFormulas(context, updatedList)
          isAddCustomDialogOpen = false
          currentTierName = if (mathSubTab.equals("Trigonometry", ignoreCase = true)) {
            FormulaVaultTier.TRIGONOMETRY_A4_SHEET.name
          } else {
            FormulaVaultTier.ALGEBRA_A4_SHEET.name
          }
          Toast.makeText(context, "Saved \"$title\" to Formula Vault!", Toast.LENGTH_SHORT).show()
        }
      )
    }
  }
}

@Composable
private fun FormulaVaultTocGlassSheet(
  sections: List<AlgebraHandbookSection>,
  sheetSubtitle: String = "CIVIL ENGINEERING • ALGEBRA CHEAT SHEET",
  onDismiss: () -> Unit,
  onSelectSection: (Int) -> Unit
) {
  val palette = currentGlassPalette()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.42f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss
        ),
      contentAlignment = Alignment.BottomCenter
    ) {
      Surface(
        onClick = {},
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = palette.dialogSurface.copy(alpha = 0.96f),
        border = BorderStroke(
          width = 1.dp,
          brush = Brush.linearGradient(
            listOf(
              Color.White.copy(alpha = if (palette.isDark) 0.35f else 0.75f),
              Color(0xFF6366F1).copy(alpha = 0.25f)
            )
          )
        ),
        shadowElevation = 24.dp,
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 560.dp)
          .testTag("formula_vault_toc_sheet")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .navigationBarsPadding(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Subtle Drag Handle Indicator
          Box(
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .width(42.dp)
              .height(4.dp)
              .clip(CircleShape)
              .background(palette.textSecondary.copy(alpha = 0.35f))
          )

          // Header Row: Title, Subtitle, Close Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Text(
                text = "Table of Contents",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = palette.textPrimary
              )
              Text(
                text = sheetSubtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.2.sp
                ),
                color = palette.accentIndigo
              )
            }

            Surface(
              onClick = onDismiss,
              shape = CircleShape,
              color = palette.nestedCardFill,
              border = palette.nestedBorder,
              modifier = Modifier
                .size(38.dp)
                .testTag("formula_vault_toc_close_button")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close Table of Contents",
                  tint = palette.textSecondary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFF6366F1).copy(alpha = 0.22f)
          )

          // Itemized Clickable Chapter Cards
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            sections.forEachIndexed { index, section ->
              val chapterNumberText = String.format(Locale.ROOT, "[%02d]", index + 1)
              Surface(
                onClick = { onSelectSection(index) },
                shape = RoundedCornerShape(12.dp),
                color = palette.nestedCardFill.copy(alpha = if (palette.isDark) 0.82f else 0.92f),
                border = BorderStroke(
                  width = 1.dp,
                  color = Color(0xFF6366F1).copy(alpha = 0.25f)
                ),
                shadowElevation = 2.dp,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("toc_section_item_${index + 1}")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    // Chapter Indicator Chip on the left: "[01]", "[02]", ...
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = Color(0xFF6366F1).copy(alpha = 0.16f),
                      border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.40f))
                    ) {
                      Text(
                        text = chapterNumberText,
                        style = MaterialTheme.typography.labelMedium.copy(
                          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                          fontSize = 12.sp,
                          fontWeight = FontWeight.ExtraBold
                        ),
                        color = palette.accentIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                      )
                    }

                    // Topic Title + Formula count badge
                    Column(
                      modifier = Modifier.weight(1f),
                      verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                      Text(
                        text = section.sectionTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Bold
                        ),
                        color = palette.textPrimary
                      )
                      Text(
                        text = "${section.items.size} reference laws & equations",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Medium
                        ),
                        color = palette.textSecondary
                      )
                    }
                  }

                  Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Jump to ${section.sectionTitle}",
                    tint = palette.accentIndigo,
                    modifier = Modifier.size(18.dp)
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

@Composable
private fun NotebookShelfLibraryView(
  shelfHeaderTitle: String,
  shelfCountBadge: String,
  notebooks: List<EngineeringNotebookSpec>,
  searchQuery: String,
  customFormulasCount: Int,
  metrics: ResponsiveMetrics,
  breadcrumbLabel: String?,
  onBreadcrumbClick: (() -> Unit)?,
  onOpenNotebook: (EngineeringNotebookSpec) -> Unit,
  modifier: Modifier = Modifier
) {
  val palette = currentGlassPalette()
  val filteredNotebooks = remember(notebooks, searchQuery) {
    if (searchQuery.isEmpty()) {
      notebooks
    } else {
      notebooks.filter { nb ->
        nb.title.contains(searchQuery, ignoreCase = true) ||
          nb.fullTitle.contains(searchQuery, ignoreCase = true) ||
          nb.subtitle.contains(searchQuery, ignoreCase = true) ||
          nb.departmentCode.contains(searchQuery, ignoreCase = true) ||
          ((nb.id == "algebra" || nb.id == "mste") &&
            "algebra exponents logarithms radicals polynomials quadratic inequalities mste".contains(
              searchQuery.lowercase(Locale.ROOT)
            )) ||
          ((nb.id == "trigonometry" || nb.id == "mste") &&
            "trigonometry sine cosine tangent pythagorean oblique heron median altitude bisector incenter centroid orthocenter radians grads mils spherical".contains(
              searchQuery.lowercase(Locale.ROOT)
            ))
      }
    }
  }

  LazyVerticalGrid(
    columns = if (metrics.screenWidthDp >= 600) {
      GridCells.Adaptive(minSize = 300.dp)
    } else {
      GridCells.Fixed(1)
    } ,
    modifier = modifier
      .widthIn(max = 680.dp)
      .padding(bottom = 96.dp),
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    if (breadcrumbLabel != null && onBreadcrumbClick != null) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Surface(
            onClick = onBreadcrumbClick,
            shape = RoundedCornerShape(10.dp),
            color = palette.cardFill,
            border = palette.glassBorder,
            modifier = Modifier.testTag("back_to_master_volumes_chip")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back to Master Volumes",
                tint = palette.accentIndigo,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = breadcrumbLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp
                ),
                color = palette.accentIndigo
              )
            }
          }
        }
      }
    }

    // Shelf Section Header
    item(span = { GridItemSpan(maxLineSpan) }) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = shelfHeaderTitle,
          style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.3.sp
          ),
          color = palette.textSecondary
        )

        Text(
          text = shelfCountBadge,
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          ),
          color = palette.accentIndigo
        )
      }
    }

    // Styled Engineering Subject Notebooks (1 column on phones, Adaptive(300.dp) dual-column on tablets)
    items(
      items = filteredNotebooks,
      key = { it.id }
    ) { notebook ->
      MasterVolumeCard(
        notebook = notebook,
        customFormulasCount = if (notebook.id == "algebra") customFormulasCount else 0,
        metrics = metrics,
        onClick = { onOpenNotebook(notebook) }
      )
    }
  }
}

@Composable
private fun MasterVolumeCard(
  notebook: EngineeringNotebookSpec,
  customFormulasCount: Int,
  metrics: ResponsiveMetrics,
  onClick: () -> Unit
) {
  EngineeringNotebookBinderCard(
    notebook = notebook,
    customFormulasCount = customFormulasCount,
    metrics = metrics,
    onClick = onClick
  )
}

@Composable
private fun EngineeringNotebookBinderCard(
  notebook: EngineeringNotebookSpec,
  customFormulasCount: Int,
  metrics: ResponsiveMetrics,
  onClick: () -> Unit
) {
  val palette = currentGlassPalette()
  val cardShape = RoundedCornerShape(
    topStart = 12.dp,
    bottomStart = 12.dp,
    topEnd = 22.dp,
    bottomEnd = 22.dp
  )

  Surface(
    onClick = onClick,
    shape = cardShape,
    color = if (palette.isDark) {
      Color(0xFF1A1F2C).copy(alpha = if (notebook.isActive) 0.94f else 0.78f)
    } else {
      Color(0xFFFFFFFF).copy(alpha = if (notebook.isActive) 0.94f else 0.82f)
    },
    border = BorderStroke(
      width = if (notebook.isActive) 1.3.dp else 1.dp,
      brush = Brush.linearGradient(
        if (notebook.isActive) {
          listOf(
            notebook.spineAccentColor.copy(alpha = 0.65f),
            Color.White.copy(alpha = if (palette.isDark) 0.22f else 0.70f)
          )
        } else {
          listOf(
            Color.White.copy(alpha = if (palette.isDark) 0.18f else 0.55f),
            palette.textSecondary.copy(alpha = 0.18f)
          )
        }
      )
    ),
    shadowElevation = if (notebook.isActive) 8.dp else 3.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("notebook_card_${notebook.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Physical Engineering Pocket Reference Binder Spine on the Left Border
      Box(
        modifier = Modifier
          .width(12.dp)
          .fillMaxHeight()
          .background(
            brush = Brush.verticalGradient(
              listOf(
                notebook.spineAccentColor,
                notebook.secondaryAccentColor
              )
            )
          )
      ) {
        // Subtle binder spine groove line
        Box(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .width(1.5.dp)
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.18f))
        )
      }

      // Main Binder Cover Content
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Card Header Row: Title on Left, Unconstrained Single-Line Status Badge on Right
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = notebook.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = metrics.scaledSp(18),
              fontWeight = FontWeight.ExtraBold
            ),
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f, fill = false)
              .padding(end = 8.dp)
          )

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (notebook.isActive) {
              notebook.spineAccentColor.copy(alpha = 0.15f)
            } else {
              palette.textSecondary.copy(alpha = 0.12f)
            },
            border = BorderStroke(
              width = 0.8.dp,
              color = if (notebook.isActive) {
                notebook.spineAccentColor.copy(alpha = 0.45f)
              } else {
                palette.textSecondary.copy(alpha = 0.25f)
              }
            )
          ) {
            Text(
              text = if (customFormulasCount > 0) {
                "${notebook.pageBadge} + $customFormulasCount"
              } else {
                notebook.pageBadge
              },
              fontFamily = JetBrainsMonoFamily,
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1,
              softWrap = false,
              color = if (notebook.isActive) notebook.spineAccentColor else palette.textSecondary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        // Bottom Row: Clean Single-Line Subtitle on Left + Open/Lock Icon Affordance on Right
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = notebook.subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = metrics.scaledSp(13),
              fontWeight = FontWeight.Medium
            ),
            color = if (notebook.isActive) palette.textPrimary.copy(alpha = 0.82f) else palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f)
              .padding(end = 10.dp)
          )

          Surface(
            shape = CircleShape,
            color = if (notebook.isActive) {
              notebook.spineAccentColor.copy(alpha = 0.14f)
            } else {
              palette.nestedCardFill
            },
            border = BorderStroke(
              width = 1.dp,
              color = if (notebook.isActive) {
                notebook.spineAccentColor.copy(alpha = 0.35f)
              } else {
                palette.textSecondary.copy(alpha = 0.18f)
              }
            ),
            modifier = Modifier.size(34.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (notebook.isActive) {
                  Icons.Default.Description
                } else {
                  Icons.Outlined.Lock
                },
                contentDescription = if (notebook.isActive) "Open ${notebook.title}" else "Coming Soon",
                tint = if (notebook.isActive) notebook.spineAccentColor else palette.textSecondary.copy(alpha = 0.65f),
                modifier = Modifier.size(17.dp)
              )
            }
          }
        }
      }
    }
  }
}

private data class AlgebraHandbookRuleItem(
  val number: Int,
  val title: String,
  val formulaOrStatement: String,
  val note: String = "",
  val subItems: List<String> = emptyList()
)

private data class AlgebraHandbookSection(
  val sectionTitle: String,
  val items: List<AlgebraHandbookRuleItem>
)

private fun buildAlgebraHandbookSections(): List<AlgebraHandbookSection> = listOf(
  AlgebraHandbookSection(
    sectionTitle = "Basic Laws of Natural Numbers",
    items = listOf(
      AlgebraHandbookRuleItem(1, "Closure Law of Addition", "a + b ∈ ℕ"),
      AlgebraHandbookRuleItem(2, "Closure Law of Multiplication", "a · b ∈ ℕ"),
      AlgebraHandbookRuleItem(3, "Commutative Law of Addition", "a + b = b + a"),
      AlgebraHandbookRuleItem(4, "Commutative Law of Multiplication", "a · b = b · a"),
      AlgebraHandbookRuleItem(5, "Associative Law of Addition", "(a + b) + c = a + (b + c)"),
      AlgebraHandbookRuleItem(6, "Associative Law of Multiplication", "(a · b) · c = a · (b · c)"),
      AlgebraHandbookRuleItem(7, "Distributive Law of Multiplication over Addition", "a(b + c) = ab + ac"),
      AlgebraHandbookRuleItem(8, "Identity Law of Addition", "a + 0 = a", "(0 is additive identity)"),
      AlgebraHandbookRuleItem(9, "Identity Law of Multiplication", "a · 1 = a", "(1 is multiplicative identity)"),
      AlgebraHandbookRuleItem(10, "Inverse Law of Addition", "a + (-a) = 0"),
      AlgebraHandbookRuleItem(11, "Inverse Law of Multiplication", "a · (1/a) = 1", "(where a ≠ 0)")
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Basic Laws of Equality & Inequality",
    items = listOf(
      AlgebraHandbookRuleItem(1, "Reflexive Property", "a = a"),
      AlgebraHandbookRuleItem(2, "Symmetric Property", "If a = b, then b = a"),
      AlgebraHandbookRuleItem(3, "Transitive Property", "If a = b and b = c, then a = c"),
      AlgebraHandbookRuleItem(4, "Addition / Subtraction Property", "If a = b, then a ± c = b ± c"),
      AlgebraHandbookRuleItem(5, "Multiplication / Division Property", "If a = b, then ac = bc and a/c = b/c", "(c ≠ 0)"),
      AlgebraHandbookRuleItem(6, "Substitution Property", "If a = b, a may be substituted for b in any expression"),
      AlgebraHandbookRuleItem(7, "Trichotomy Property", "Exactly one is true: a < b, a = b, or a > b")
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Theorems on Inequalities",
    items = listOf(
      AlgebraHandbookRuleItem(1, "Sense Preservation", "If a > b and c > 0, then ac > bc and a/c > b/c"),
      AlgebraHandbookRuleItem(2, "Sense Reversal", "If a > b and c < 0, then ac < bc and a/c < b/c"),
      AlgebraHandbookRuleItem(3, "Addition of Inequalities", "If a > b and c > d, then (a + c) > (b + d)"),
      AlgebraHandbookRuleItem(4, "Transitivity of Inequalities", "If a > b and b > c, then a > c"),
      AlgebraHandbookRuleItem(5, "Reciprocal Reversal", "If a > b > 0, then 1/a < 1/b"),
      AlgebraHandbookRuleItem(6, "Non-Negative Square", "For any real number a, a² ≥ 0"),
      AlgebraHandbookRuleItem(7, "Absolute Value Inequality", "|x| < k  ⇔  -k < x < k ;   |x| > k  ⇔  x < -k or x > k", "(k > 0)")
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Laws of Exponents",
    items = listOf(
      AlgebraHandbookRuleItem(1, "Product Rule", "aᵐ · aⁿ = aᵐ⁺ⁿ"),
      AlgebraHandbookRuleItem(2, "Quotient Rule", "aᵐ / aⁿ = aᵐ⁻ⁿ", "(a ≠ 0)"),
      AlgebraHandbookRuleItem(3, "Power of a Power Rule", "(aᵐ)ⁿ = aᵐⁿ"),
      AlgebraHandbookRuleItem(4, "Power of a Product Rule", "(ab)ⁿ = aⁿbⁿ"),
      AlgebraHandbookRuleItem(5, "Power of a Quotient Rule", "(a / b)ⁿ = aⁿ / bⁿ", "(b ≠ 0)"),
      AlgebraHandbookRuleItem(6, "Zero Exponent Rule", "a⁰ = 1", "(a ≠ 0)"),
      AlgebraHandbookRuleItem(7, "Negative Exponent Rule", "a⁻ⁿ = 1 / aⁿ and (a / b)⁻ⁿ = (b / a)ⁿ"),
      AlgebraHandbookRuleItem(8, "Fractional Exponent Rule", "aᵐ/ⁿ = ⁿ√(aᵐ) = (ⁿ√a)ᵐ")
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Properties of Radicals",
    items = listOf(
      AlgebraHandbookRuleItem(1, "Equivalence & Root-Power Identity", "ⁿ√a = b ⇔ bⁿ = a ;   (ⁿ√a)ⁿ = a"),
      AlgebraHandbookRuleItem(2, "Product of Radicals", "ⁿ√a · ⁿ√b = ⁿ√(ab)"),
      AlgebraHandbookRuleItem(3, "Quotient of Radicals", "ⁿ√a / ⁿ√b = ⁿ√(a / b)", "(b ≠ 0)"),
      AlgebraHandbookRuleItem(4, "Nested / Index Multiplication", "ᵐ√(ⁿ√a) = ᵐⁿ√a"),
      AlgebraHandbookRuleItem(5, "Index Cancellation (Even vs. Odd)", "ⁿ√(aⁿ) = |a| (if n is even) ;   ⁿ√(aⁿ) = a (if n is odd)"),
      AlgebraHandbookRuleItem(6, "Conjugate Rationalization", "1 / (√a ± √b) = (√a ∓ √b) / (a - b)")
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Properties of Logarithms",
    items = listOf(
      AlgebraHandbookRuleItem(1, "Fundamental Definition", "y = log_b(x)  ⇔  bʸ = x", "(b > 0, b ≠ 1, x > 0)"),
      AlgebraHandbookRuleItem(2, "Natural & Common Logarithms", "ln(x) = log_e(x) ;   log(x) = log₁₀(x)"),
      AlgebraHandbookRuleItem(3, "Product Property", "log_b(MN) = log_b(M) + log_b(N)"),
      AlgebraHandbookRuleItem(4, "Quotient Property", "log_b(M / N) = log_b(M) - log_b(N)"),
      AlgebraHandbookRuleItem(5, "Power Property", "log_b(Mᵏ) = k · log_b(M)"),
      AlgebraHandbookRuleItem(6, "Identity & Base Properties", "log_b(1) = 0 ;   log_b(b) = 1 ;   log_b(bˣ) = x ;   b^(log_b x) = x"),
      AlgebraHandbookRuleItem(7, "Change of Base & Reciprocal", "log_b(M) = ln(M) / ln(b) = log₁₀(M) / log₁₀(b) ;   log_b(a) = 1 / log_a(b)")
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Polynomials & Factoring",
    items = listOf(
      AlgebraHandbookRuleItem(1, "Standard Polynomial Form", "P(x) = aₙxⁿ + aₙ₋₁xⁿ⁻¹ + ... + a₁x + a₀"),
      AlgebraHandbookRuleItem(2, "Difference of Two Squares", "a² - b² = (a - b)(a + b)"),
      AlgebraHandbookRuleItem(3, "Square of a Binomial", "(a ± b)² = a² ± 2ab + b²"),
      AlgebraHandbookRuleItem(4, "Cube of a Binomial", "(a ± b)³ = a³ ± 3a²b + 3ab² ± b³"),
      AlgebraHandbookRuleItem(5, "Sum & Difference of Two Cubes", "a³ ± b³ = (a ± b)(a² ∓ ab + b²)"),
      AlgebraHandbookRuleItem(6, "Square of a Trinomial", "(a + b + c)² = a² + b² + c² + 2(ab + bc + ca)"),
      AlgebraHandbookRuleItem(7, "Remainder & Factor Theorems", "Remainder R = P(c) when divided by (x - c) ;   (x - c) is a factor ⇔ P(c) = 0"),
      AlgebraHandbookRuleItem(
        number = 8,
        title = "Quadratic Formula & Discriminant Analysis (ax² + bx + c = 0)",
        formulaOrStatement = "x = (-b ± √(b² - 4ac)) / (2a)",
        subItems = listOf(
          "Discriminant: D = b² - 4ac",
          "Roots Nature: D > 0 (Real & Unequal), D = 0 (Real & Equal), D < 0 (Complex Conjugates)",
          "Vieta's Relations: r₁ + r₂ = -b/a ;   r₁ · r₂ = c/a"
        )
      )
    )
  )
)

@Composable
private fun AlgebraA4PaperViewer(
  metrics: ResponsiveMetrics,
  lazyListState: androidx.compose.foundation.lazy.LazyListState,
  highlightedSectionIndex: Int?,
  searchQuery: String,
  customFormulas: List<ReferenceFormulaCardData>,
  starredIds: Set<String>,
  onToggleStar: (String) -> Unit,
  onBackToShelf: () -> Unit,
  onOpenToc: () -> Unit,
  modifier: Modifier = Modifier
) {
  val palette = currentGlassPalette()
  val allSections = remember { buildAlgebraHandbookSections() }

  val filteredSections = remember(allSections, searchQuery) {
    if (searchQuery.isEmpty()) {
      allSections.mapIndexed { idx, sec -> idx to sec }
    } else {
      allSections.mapIndexedNotNull { idx, section ->
        if (section.sectionTitle.contains(searchQuery, ignoreCase = true)) {
          idx to section
        } else {
          val matchingItems = section.items.filter { item ->
            item.title.contains(searchQuery, ignoreCase = true) ||
              item.formulaOrStatement.contains(searchQuery, ignoreCase = true) ||
              item.note.contains(searchQuery, ignoreCase = true) ||
              item.subItems.any { it.contains(searchQuery, ignoreCase = true) }
          }
          if (matchingItems.isNotEmpty()) {
            idx to section.copy(items = matchingItems)
          } else {
            null
          }
        }
      }
    }
  }

  // Crisp paper background (Color(0xFFFAFAFC) in Light Mode, deep slate paper Color(0xFF1E222B) in Dark Mode)
  val a4PaperColor = if (palette.isDark) Color(0xFF1E222B) else Color(0xFFFAFAFC)
  val a4BorderColor = if (palette.isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFCBD5E1)
  val a4HeadingColor = if (palette.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
  val a4SubtleTextColor = if (palette.isDark) Color(0xFF94A3B8) else Color(0xFF475569)

  // Soft tinted glass fill & 1.dp Color(0xFF6366F1).copy(alpha = 0.25f) hairline border for formula callouts
  val mathCalloutBg = if (palette.isDark) {
    palette.cardSurface.copy(alpha = 0.9f)
  } else {
    Color(0xFFEEF2FF).copy(alpha = 0.92f)
  }
  val mathCalloutBorder = Color(0xFF6366F1).copy(alpha = 0.25f)
  // High-contrast primary accent (deep indigo/violet in light mode, bright electric violet in dark mode)
  val mathTextColor = if (palette.isDark) Color(0xFFC4B5FD) else Color(0xFF3730A3)

  LazyColumn(
    state = lazyListState,
    contentPadding = PaddingValues(bottom = 120.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .fillMaxSize()
      .testTag("algebra_a4_paper_sheet")
  ) {
    // ====================================================================
    // ITEM 0: Breadcrumb Bar + A4 Running Header & Title ("ALGEBRA")
    // ====================================================================
    item(key = "a4_header_item_0") {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 680.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Subtle Breadcrumb Bar to return to MSTE Sub-Topics Shelf + Quick TOC Chip
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            onClick = onBackToShelf,
            shape = RoundedCornerShape(10.dp),
            color = palette.cardFill,
            border = palette.glassBorder,
            modifier = Modifier.testTag("back_to_notebook_shelf_chip")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back to MSTE Library",
                tint = palette.accentIndigo,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "MSTE Library",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp
                ),
                color = palette.accentIndigo
              )
            }
          }

          Surface(
            onClick = onOpenToc,
            shape = RoundedCornerShape(10.dp),
            color = palette.cardFill,
            border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.30f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ViewList,
                contentDescription = "Table of Contents",
                tint = palette.accentIndigo,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "MSTE / CE-01 • TOC",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                ),
                color = palette.accentIndigo
              )
            }
          }
        }

        // Top of the A4 Paper Sheet
        Surface(
          shape = if (filteredSections.isEmpty()) {
            RoundedCornerShape(6.dp)
          } else {
            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
          },
          color = a4PaperColor,
          border = BorderStroke(1.dp, a4BorderColor),
          shadowElevation = 4.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "CIVIL ENGINEERING REFERENCE MANUAL • CHAPTER I: ALGEBRA",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
              ),
              color = palette.accentIndigo
            )

            HorizontalDivider(
              thickness = 1.5.dp,
              color = palette.accentIndigo.copy(alpha = 0.45f)
            )

            Text(
              text = "ALGEBRA",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              ),
              color = a4HeadingColor
            )

            if (filteredSections.isEmpty()) {
              Text(
                text = "No Algebra laws match \"$searchQuery\".",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = a4SubtleTextColor,
                modifier = Modifier.padding(vertical = 16.dp)
              )
            }
          }
        }
      }
    }

    // ====================================================================
    // ITEMS 1..7: Each Algebra Section + All Numbered Bold Formula Callouts
    // ====================================================================
    items(
      count = filteredSections.size,
      key = { idx -> "a4_section_item_${filteredSections[idx].first + 1}" }
    ) { listIdx ->
      val (originalSectionIndex, section) = filteredSections[listIdx]
      val isLastSection = listIdx == filteredSections.lastIndex
      val isHighlighted = highlightedSectionIndex == originalSectionIndex

      val sectionHeaderBgAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.24f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "toc_arrival_glow_alpha"
      )
      val sectionHeaderBorderAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.75f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "toc_arrival_border_alpha"
      )

      Surface(
        shape = if (isLastSection) {
          RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
        } else {
          RoundedCornerShape(0.dp)
        },
        color = a4PaperColor,
        border = BorderStroke(1.dp, a4BorderColor),
        shadowElevation = 4.dp,
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 680.dp)
          .testTag("algebra_section_${originalSectionIndex + 1}")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(
              start = 22.dp,
              end = 22.dp,
              top = 14.dp,
              bottom = if (isLastSection) 24.dp else 14.dp
            ),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Section Bullet Heading with Subtle Arrival Highlight Glow
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF6366F1).copy(alpha = sectionHeaderBgAlpha))
              .border(
                width = 1.dp,
                color = Color(0xFF6366F1).copy(alpha = sectionHeaderBorderAlpha),
                shape = RoundedCornerShape(10.dp)
              )
              .padding(
                horizontal = if (isHighlighted) 10.dp else 0.dp,
                vertical = if (isHighlighted) 6.dp else 2.dp
              ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "•",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
              ),
              color = palette.accentIndigo
            )
            Text(
              text = section.sectionTitle,
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.5.sp,
                fontWeight = FontWeight.ExtraBold
              ),
              color = a4HeadingColor
            )
          }

          // Indented Highlighted Math Callout Blocks (1., 2., 3., ...)
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            section.items.forEach { rule ->
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                // Law Title with Number Prefix + Optional Note placed ABOVE the formula container
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "${rule.number}. ${rule.title}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = a4HeadingColor
                  )
                  if (rule.note.isNotEmpty()) {
                    Text(
                      text = rule.note,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                      ),
                      color = a4SubtleTextColor
                    )
                  }
                }

                // Elevated Rounded Formula Callout Capsule (8.dp radius, 12.dp horizontal / 8.dp vertical padding, 0.25f border)
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = mathCalloutBg,
                  border = BorderStroke(1.dp, mathCalloutBorder),
                  shadowElevation = 2.dp,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(
                      text = rule.formulaOrStatement,
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 17.5.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                      ),
                      color = mathTextColor
                    )

                    if (rule.subItems.isNotEmpty()) {
                      HorizontalDivider(
                        thickness = 1.dp,
                        color = mathCalloutBorder
                      )
                      Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        rule.subItems.forEach { subLine ->
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                          ) {
                            Text(
                              text = "•",
                              style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                              ),
                              color = palette.accentIndigo
                            )
                            Text(
                              text = subLine,
                              style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 15.5.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Bold
                              ),
                              color = mathTextColor,
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

          if (!isLastSection) {
            HorizontalDivider(
              thickness = 0.8.dp,
              color = a4BorderColor.copy(alpha = 0.55f),
              modifier = Modifier.padding(top = 6.dp)
            )
          }
        }
      }
    }

    // Custom scholar formulas below the A4 paper sheet
    if (customFormulas.isNotEmpty()) {
      item(key = "a4_custom_formulas_footer") {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .padding(top = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          customFormulas.forEach { formula ->
            InteractiveReferenceFormulaCard(
              formula = formula,
              isStarred = starredIds.contains(formula.id),
              onToggleStar = { onToggleStar(formula.id) },
              metrics = metrics
            )
          }
        }
      }
    }
  }
}

private fun buildTrigonometryHandbookSections(): List<AlgebraHandbookSection> = listOf(
  AlgebraHandbookSection(
    sectionTitle = "Functions of a Right Triangle",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Primary Ratios",
        formulaOrStatement = "sin θ = opp / hyp ,  cos θ = adj / hyp ,  tan θ = opp / adj"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Reciprocal Ratios",
        formulaOrStatement = "csc θ = hyp / opp ,  sec θ = hyp / adj ,  cot θ = adj / opp"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Pythagorean Theorem & Right Triangle Metric Relations",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Hypotenuse & Legs",
        formulaOrStatement = "c² = a² + b²  ⟹  c = √(a² + b²)"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Altitude to Hypotenuse (h)",
        formulaOrStatement = "h = (a · b) / c ,  h² = p · q",
        note = "(where p, q are hypotenuse segments)"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Leg Projections",
        formulaOrStatement = "a² = c · p ,  b² = c · q"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Trigonometric Identities",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Quotient & Reciprocal",
        formulaOrStatement = "tan θ = sin θ / cos θ ,  cot θ = cos θ / sin θ = 1 / tan θ"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Pythagorean Identities",
        formulaOrStatement = "sin²θ + cos²θ = 1",
        subItems = listOf(
          "1 + tan²θ = sec²θ",
          "1 + cot²θ = csc²θ"
        )
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Co-Function Identities",
        formulaOrStatement = "sin(90° - θ) = cos θ ,  tan(90° - θ) = cot θ ,  sec(90° - θ) = csc θ"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Sum & Difference of Two Angles",
        formulaOrStatement = "sin(A ± B) = sin A cos B ± cos A sin B",
        subItems = listOf(
          "cos(A ± B) = cos A cos B ∓ sin A sin B",
          "tan(A ± B) = (tan A ± tan B) / (1 ∓ tan A tan B)"
        )
      ),
      AlgebraHandbookRuleItem(
        number = 5,
        title = "Double-Angle Formulas",
        formulaOrStatement = "sin 2θ = 2 sin θ cos θ",
        subItems = listOf(
          "cos 2θ = cos²θ - sin²θ = 2cos²θ - 1 = 1 - 2sin²θ",
          "tan 2θ = (2 tan θ) / (1 - tan²θ)"
        )
      ),
      AlgebraHandbookRuleItem(
        number = 6,
        title = "Half-Angle Formulas",
        formulaOrStatement = "sin(θ/2) = ±√((1 - cos θ) / 2)",
        subItems = listOf(
          "cos(θ/2) = ±√((1 + cos θ) / 2)",
          "tan(θ/2) = (1 - cos θ) / sin θ = sin θ / (1 + cos θ)"
        )
      ),
      AlgebraHandbookRuleItem(
        number = 7,
        title = "Product-to-Sum Formulas",
        formulaOrStatement = "sin A cos B = ½ [sin(A + B) + sin(A - B)]",
        subItems = listOf(
          "cos A cos B = ½ [cos(A + B) + cos(A - B)]",
          "sin A sin B = -½ [cos(A + B) - cos(A - B)]"
        )
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Laws of Oblique Triangles",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Sine Law",
        formulaOrStatement = "a / sin A = b / sin B = c / sin C = 2R",
        note = "(where R is circumradius)"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Cosine Law",
        formulaOrStatement = "a² = b² + c² - 2bc cos A",
        subItems = listOf(
          "b² = a² + c² - 2ac cos B",
          "c² = a² + b² - 2ab cos C"
        )
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Law of Tangents",
        formulaOrStatement = "(a - b) / (a + b) = tan[½(A - B)] / tan[½(A + B)]"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Angles & Classifications",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Classifications",
        formulaOrStatement = "Acute: 0° < θ < 90° | Right: θ = 90° | Obtuse: 90° < θ < 180° | Reflex: 180° < θ < 360°"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Pairs",
        formulaOrStatement = "Complementary: A + B = 90° (π/2 rad)",
        subItems = listOf(
          "Supplementary: A + B = 180° (π rad)",
          "Explementary (Conjugate): A + B = 360° (2π rad)"
        )
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Units of Angular Measurement & Exact Conversions",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Equivalence Relation",
        formulaOrStatement = "1 rev = 360° = 2π rad = 400 grads (gons) = 6400 mils"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Common Conversion Factors",
        formulaOrStatement = "1 rad = 180° / π ≈ 57.29578° = 57°17'45\"",
        subItems = listOf(
          "1° = π / 180° rad ≈ 0.017453 rad",
          "1 grad = 0.9° | 1 mil = 360° / 6400 = 0.05625°"
        )
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Area of a Triangle (All Engineering Formulas)",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Base and Altitude",
        formulaOrStatement = "Area = ½ · b · h"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Two Sides and Included Angle (SAS)",
        formulaOrStatement = "Area = ½ a b sin C = ½ b c sin A = ½ a c sin B"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Three Sides (Heron's Formula)",
        formulaOrStatement = "Area = √[s(s - a)(s - b)(s - c)] ,  where s = (a + b + c) / 2"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Inradius (r)",
        formulaOrStatement = "Area = r · s  ⟹  r = Area / s"
      ),
      AlgebraHandbookRuleItem(
        number = 5,
        title = "Circumradius (R)",
        formulaOrStatement = "Area = (a · b · c) / (4R)  ⟹  R = (a · b · c) / (4 · Area)"
      ),
      AlgebraHandbookRuleItem(
        number = 6,
        title = "Exradius (r_a, r_b, r_c)",
        formulaOrStatement = "Area = r_a(s - a) = r_b(s - b) = r_c(s - c)",
        subItems = listOf(
          "Area = √(r · r_a · r_b · r_c)"
        )
      ),
      AlgebraHandbookRuleItem(
        number = 7,
        title = "Analytic / Surveyor's Coordinates",
        formulaOrStatement = "Area = ½ |(x₁y₂ + x₂y₃ + x₃y₁) - (y₁x₂ + y₂x₃ + y₃x₁)|"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Medians of a Triangle & Centroid",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Length of Medians (Apollonius' Theorem)",
        formulaOrStatement = "m_a = ½ √(2b² + 2c² - a²)",
        subItems = listOf(
          "m_b = ½ √(2a² + 2c² - b²)",
          "m_c = ½ √(2a² + 2b² - c²)"
        )
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Centroid Intersection Properties",
        formulaOrStatement = "Centroid (G) divides each median in a 2:1 ratio from vertex to midpoint",
        subItems = listOf(
          "Area of 6 subtriangles formed by medians are strictly equal (Area / 6)"
        )
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Altitudes of a Triangle & Orthocenter",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Heights from Vertices",
        formulaOrStatement = "h_a = (2 · Area) / a ,  h_b = (2 · Area) / b ,  h_c = (2 · Area) / c"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Harmonic Relation",
        formulaOrStatement = "1/h_a + 1/h_b + 1/h_c = 1/r",
        note = "(where r is inradius)"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Angle Bisectors of a Triangle & Incenter",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Length of Internal Angle Bisectors",
        formulaOrStatement = "t_a = [2 √(b c s (s - a))] / (b + c) = [2 b c cos(A / 2)] / (b + c)",
        subItems = listOf(
          "t_b = [2 √(a c s (s - b))] / (a + c) = [2 a c cos(B / 2)] / (a + c)",
          "t_c = [2 √(a b s (s - c))] / (a + b) = [2 a b cos(C / 2)] / (a + b)"
        )
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Angle Bisector Theorem",
        formulaOrStatement = "Segments of opposite side: c₁ / c₂ = a / b"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Circumcircle, Incircle & Euler Line Relations",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Euler's Theorem (Distance d between Circumcenter O & Incenter I)",
        formulaOrStatement = "d² = R(R - 2r)  ⟹  R ≥ 2r"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Exradii & Circumradius Identity",
        formulaOrStatement = "r_a + r_b + r_c - r = 4R ,  cos A + cos B + cos C = 1 + r / R"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Spherical Trigonometry (Right & Oblique Spherical Triangles)",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Napier's Rules for Right Spherical Triangles (C = 90°)",
        formulaOrStatement = "sin(middle) = prod of cosines of opposite parts (SIN-COOP)",
        subItems = listOf(
          "sin(middle) = prod of tangents of adjacent parts (SIN-TAN-AD)",
          "Circular Parts: a ,  b ,  co-A ,  co-c ,  co-B"
        )
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Oblique Spherical Triangles & Spherical Excess (E)",
        formulaOrStatement = "sin a / sin A = sin b / sin B = sin c / sin C",
        subItems = listOf(
          "cos a = cos b cos c + sin b sin c cos A",
          "E = (A + B + C) - 180° ,  Area = (π R² E) / 180°"
        )
      )
    )
  )
)

@Composable
private fun MsteTrigonometrySheet(
  metrics: ResponsiveMetrics,
  lazyListState: androidx.compose.foundation.lazy.LazyListState,
  highlightedSectionIndex: Int?,
  searchQuery: String,
  customFormulas: List<ReferenceFormulaCardData>,
  starredIds: Set<String>,
  onToggleStar: (String) -> Unit,
  onBackToShelf: () -> Unit,
  onOpenToc: () -> Unit,
  modifier: Modifier = Modifier
) {
  val palette = currentGlassPalette()
  val allSections = remember { buildTrigonometryHandbookSections() }

  val filteredSections = remember(allSections, searchQuery) {
    if (searchQuery.isEmpty()) {
      allSections.mapIndexed { idx, sec -> idx to sec }
    } else {
      allSections.mapIndexedNotNull { idx, section ->
        if (section.sectionTitle.contains(searchQuery, ignoreCase = true)) {
          idx to section
        } else {
          val matchingItems = section.items.filter { item ->
            item.title.contains(searchQuery, ignoreCase = true) ||
              item.formulaOrStatement.contains(searchQuery, ignoreCase = true) ||
              item.note.contains(searchQuery, ignoreCase = true) ||
              item.subItems.any { it.contains(searchQuery, ignoreCase = true) }
          }
          if (matchingItems.isNotEmpty()) {
            idx to section.copy(items = matchingItems)
          } else {
            null
          }
        }
      }
    }
  }

  val a4PaperColor = if (palette.isDark) Color(0xFF1E222B) else Color(0xFFFAFAFC)
  val a4BorderColor = if (palette.isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFCBD5E1)
  val a4HeadingColor = if (palette.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
  val a4SubtleTextColor = if (palette.isDark) Color(0xFF94A3B8) else Color(0xFF475569)

  val mathCalloutBg = if (palette.isDark) {
    palette.cardSurface.copy(alpha = 0.9f)
  } else {
    Color(0xFFEEF2FF).copy(alpha = 0.92f)
  }
  val mathCalloutBorder = Color(0xFF6366F1).copy(alpha = 0.25f)
  val mathTextColor = if (palette.isDark) Color(0xFFC4B5FD) else Color(0xFF3730A3)

  LazyColumn(
    state = lazyListState,
    contentPadding = PaddingValues(bottom = 120.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .fillMaxSize()
      .testTag("trigonometry_a4_paper_sheet")
  ) {
    // ====================================================================
    // ITEM 0: Breadcrumb Bar + A4 Running Header & Title ("PLANE & SPHERICAL TRIGONOMETRY")
    // ====================================================================
    item(key = "trig_a4_header_item_0") {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 680.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            onClick = onBackToShelf,
            shape = RoundedCornerShape(10.dp),
            color = palette.cardFill,
            border = palette.glassBorder,
            modifier = Modifier.testTag("trig_back_to_notebook_shelf_chip")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back to MSTE Library",
                tint = palette.accentIndigo,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "MSTE Library",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp
                ),
                color = palette.accentIndigo
              )
            }
          }

          Surface(
            onClick = onOpenToc,
            shape = RoundedCornerShape(10.dp),
            color = palette.cardFill,
            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.35f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ViewList,
                contentDescription = "Table of Contents",
                tint = palette.accentIndigo,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "MSTE / CE-02 • TOC",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                ),
                color = palette.accentIndigo
              )
            }
          }
        }

        // Top of the A4 Paper Sheet
        Surface(
          shape = if (filteredSections.isEmpty()) {
            RoundedCornerShape(6.dp)
          } else {
            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
          },
          color = a4PaperColor,
          border = BorderStroke(1.dp, a4BorderColor),
          shadowElevation = 4.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "CIVIL ENGINEERING REFERENCE MANUAL • CHAPTER II: TRIGONOMETRY",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
              ),
              color = palette.accentIndigo
            )

            HorizontalDivider(
              thickness = 1.5.dp,
              color = palette.accentIndigo.copy(alpha = 0.45f)
            )

            Text(
              text = "PLANE & SPHERICAL TRIGONOMETRY",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
              ),
              color = a4HeadingColor
            )

            if (filteredSections.isEmpty()) {
              Text(
                text = "No Trigonometry formulas match \"$searchQuery\".",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = a4SubtleTextColor,
                modifier = Modifier.padding(vertical = 16.dp)
              )
            }
          }
        }
      }
    }

    // ====================================================================
    // ITEMS 1..12: Each Trigonometry Section + High-Contrast Bold Monospace Capsules
    // ====================================================================
    items(
      count = filteredSections.size,
      key = { idx -> "trig_a4_section_item_${filteredSections[idx].first + 1}" }
    ) { listIdx ->
      val (originalSectionIndex, section) = filteredSections[listIdx]
      val isLastSection = listIdx == filteredSections.lastIndex
      val isHighlighted = highlightedSectionIndex == originalSectionIndex

      val sectionHeaderBgAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.24f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "trig_toc_arrival_glow_alpha"
      )
      val sectionHeaderBorderAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.75f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "trig_toc_arrival_border_alpha"
      )

      Surface(
        shape = if (isLastSection) {
          RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
        } else {
          RoundedCornerShape(0.dp)
        },
        color = a4PaperColor,
        border = BorderStroke(1.dp, a4BorderColor),
        shadowElevation = 4.dp,
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 680.dp)
          .testTag("trigonometry_section_${originalSectionIndex + 1}")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(
              start = 22.dp,
              end = 22.dp,
              top = 14.dp,
              bottom = if (isLastSection) 24.dp else 14.dp
            ),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Section Bullet Heading with Subtle Arrival Highlight Glow
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF6366F1).copy(alpha = sectionHeaderBgAlpha))
              .border(
                width = 1.dp,
                color = Color(0xFF6366F1).copy(alpha = sectionHeaderBorderAlpha),
                shape = RoundedCornerShape(10.dp)
              )
              .padding(
                horizontal = if (isHighlighted) 10.dp else 0.dp,
                vertical = if (isHighlighted) 6.dp else 2.dp
              ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "•",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
              ),
              color = palette.accentIndigo
            )
            Text(
              text = section.sectionTitle,
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.5.sp,
                fontWeight = FontWeight.ExtraBold
              ),
              color = a4HeadingColor
            )
          }

          // Indented Highlighted Math Callout Blocks (1., 2., 3., ...)
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            section.items.forEach { rule ->
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "${rule.number}. ${rule.title}:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = a4HeadingColor
                  )
                  if (rule.note.isNotEmpty()) {
                    Text(
                      text = rule.note,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                      ),
                      color = a4SubtleTextColor
                    )
                  }
                }

                // High-contrast, bold monospace capsules (fontSize = 16.5.sp, FontWeight.Bold, FontFamily.Monospace, rounded glass container with hairline border)
                val allFormulaCapsules = remember(rule) {
                  listOf(rule.formulaOrStatement) + rule.subItems
                }
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  allFormulaCapsules.forEach { formulaLine ->
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = mathCalloutBg,
                      border = BorderStroke(1.dp, mathCalloutBorder),
                      shadowElevation = 2.dp,
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text(
                        text = formulaLine,
                        style = MaterialTheme.typography.titleMedium.copy(
                          fontFamily = FontFamily.Monospace,
                          fontSize = 16.5.sp,
                          lineHeight = 23.sp,
                          fontWeight = FontWeight.Bold,
                          letterSpacing = 0.15.sp
                        ),
                        color = mathTextColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          if (!isLastSection) {
            HorizontalDivider(
              thickness = 0.8.dp,
              color = a4BorderColor.copy(alpha = 0.55f),
              modifier = Modifier.padding(top = 6.dp)
            )
          }
        }
      }
    }

    // Custom scholar formulas below the A4 paper sheet
    if (customFormulas.isNotEmpty()) {
      item(key = "trig_a4_custom_formulas_footer") {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .padding(top = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          customFormulas.forEach { formula ->
            InteractiveReferenceFormulaCard(
              formula = formula,
              isStarred = starredIds.contains(formula.id),
              onToggleStar = { onToggleStar(formula.id) },
              metrics = metrics
            )
          }
        }
      }
    }
  }
}

private enum class HolographicTriangleTopic(
  val chipLabel: String,
  val conceptTitle: String,
  val accentColor: Color,
  val engineeringDefinition: String,
  val geometricTheorem: String,
  val boardExamFormula: String
) {
  SIDES_AND_ANGLES(
    chipLabel = "Sides & Angles",
    conceptTitle = "Oblique Sides (a, b, c) & Interior Angles (A, B, C)",
    accentColor = Color(0xFF06B6D4),
    engineeringDefinition = "In structural truss resolution and geodetic triangulation, side lengths a, b, and c lie strictly opposite interior vertex angles A, B, and C.",
    geometricTheorem = "Interior Angle Sum & Law of Sines / Cosines: A + B + C = 180°; any side is proportional to the sine of its opposite angle.",
    boardExamFormula = "a / sin A = b / sin B = c / sin C = 2R\na² = b² + c² - 2bc cos A"
  ),
  MEDIANS_AND_CENTROID(
    chipLabel = "Medians & Centroid",
    conceptTitle = "Medians (m_a, m_b, m_c) & Centroid (G)",
    accentColor = Color(0xFFA855F7),
    engineeringDefinition = "A median connects a vertex to the midpoint of the opposite side. All three medians intersect at the Centroid (G), the center of gravity of a uniform triangular plate.",
    geometricTheorem = "Apollonius' Theorem & 2:1 Trisection: Centroid G divides each median in an exact 2:1 ratio from vertex to midpoint and partitions the triangle into 6 equal-area subtriangles.",
    boardExamFormula = "m_a = ½ √(2b² + 2c² - a²)\nAG : GM_a = 2 : 1  •  A_sub = Area / 6"
  ),
  ALTITUDES_AND_ORTHOCENTER(
    chipLabel = "Altitudes & Orthocenter",
    conceptTitle = "Altitudes (h_a, h_b, h_c) & Orthocenter (H)",
    accentColor = Color(0xFFF59E0B),
    engineeringDefinition = "An altitude is the perpendicular drop from a vertex to the opposite side (or its extension). The three altitudes concur at the Orthocenter (H).",
    geometricTheorem = "Reciprocal Altitude-Inradius Harmonic Theorem: Each altitude is inversely proportional to its base length, and the sum of reciprocals of the three altitudes equals the reciprocal of the inradius r.",
    boardExamFormula = "h_a = (2 · Area) / a = b sin C = c sin B\n1/h_a + 1/h_b + 1/h_c = 1/r"
  ),
  BISECTORS_AND_INCENTER(
    chipLabel = "Bisectors & Incenter",
    conceptTitle = "Angle Bisectors (t_a, t_b, t_c) & Incenter (I)",
    accentColor = Color(0xFF10B981),
    engineeringDefinition = "An internal angle bisector divides a vertex angle into two equal halves (A/2). All three bisectors intersect at the Incenter (I), equidistant from all three sides.",
    geometricTheorem = "Angle Bisector Theorem: An internal bisector divides the opposite side into segments (c₁, c₂) strictly proportional to the adjacent triangle sides (a : b).",
    boardExamFormula = "t_a = [2 √(b c s (s - a))] / (b + c) = [2 b c cos(A/2)] / (b + c)\nc₁ / c₂ = b / c"
  ),
  RADII_R_AND_CAP_R(
    chipLabel = "Radii (r, R)",
    conceptTitle = "Inradius (r), Circumradius (R) & Centers (I, O)",
    accentColor = Color(0xFF3B82F6),
    engineeringDefinition = "Incenter (I) is the center of the inscribed circle (radius r tangent to all sides); Circumcenter (O) is the intersection of perpendicular bisectors (radius R through all vertices).",
    geometricTheorem = "Euler's Triangle Inequality & Heron-Radius Relations: R ≥ 2r (equality holds iff equilateral), with distance d between O and I satisfying d² = R(R - 2r).",
    boardExamFormula = "r = Area / s  •  R = (a · b · c) / (4 · Area)\nArea = √[s(s - a)(s - b)(s - c)]"
  )
}

@Composable
private fun HolographicTriangleModal(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val localView = LocalView.current
  var selectedTopic by rememberSaveable { mutableStateOf(HolographicTriangleTopic.SIDES_AND_ANGLES) }

  val pulseTransition = rememberInfiniteTransition(label = "holo_modal_canvas_pulse")
  val neonPulse by pulseTransition.animateFloat(
    initialValue = 0.55f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1350, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "neon_pulse"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF030712).copy(alpha = 0.76f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss
        ),
      contentAlignment = Alignment.BottomCenter
    ) {
      Surface(
        onClick = {},
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        color = Color(0xFF0B1120).copy(alpha = 0.97f),
        border = BorderStroke(
          width = 1.3.dp,
          brush = Brush.linearGradient(
            listOf(
              Color(0xFF06B6D4).copy(alpha = 0.75f),
              Color(0xFF6366F1).copy(alpha = 0.45f),
              Color(0xFFA855F7).copy(alpha = 0.40f)
            )
          )
        ),
        shadowElevation = 28.dp,
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 640.dp)
          .testTag("holographic_triangle_modal")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              brush = Brush.radialGradient(
                colors = listOf(
                  Color(0xFF06B6D4).copy(alpha = 0.12f),
                  Color(0xFF1E1B4B).copy(alpha = 0.22f),
                  Color.Transparent
                ),
                radius = 900f
              )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .navigationBarsPadding(),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Subtle Drag Handle
          Box(
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .width(44.dp)
              .height(4.dp)
              .clip(CircleShape)
              .background(Color(0xFF06B6D4).copy(alpha = 0.45f))
          )

          // Header: "Interactive Triangle Anatomy" with Close Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = "Interactive Triangle Anatomy",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = 19.sp,
                  fontWeight = FontWeight.ExtraBold
                ),
                color = Color(0xFFF8FAFC)
              )
              Text(
                text = "HOLOGRAPHIC CE BOARD GEOMETRY INSPECTOR • TAP CANVAS OR CHIPS",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                ),
                color = Color(0xFF06B6D4)
              )
            }

            Surface(
              onClick = onDismiss,
              shape = CircleShape,
              color = Color(0xFF1E293B),
              border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.35f)),
              modifier = Modifier
                .size(38.dp)
                .testTag("holographic_triangle_close_button")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close Interactive Triangle Anatomy",
                  tint = Color(0xFFE2E8F0),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          // ==================================================================
          // INTERACTIVE HOLOGRAPHIC CANVAS (OBLIQUE TRIANGLE ANATOMY)
          // ==================================================================
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF060B16),
            border = BorderStroke(
              width = 1.2.dp,
              color = selectedTopic.accentColor.copy(alpha = 0.55f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(236.dp)
              .testTag("holographic_triangle_canvas")
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              Canvas(
                modifier = Modifier
                  .fillMaxSize()
                  .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                      val w = size.width.toFloat()
                      val h = size.height.toFloat()
                      val ptA = Offset(w * 0.37f, h * 0.14f)
                      val ptB = Offset(w * 0.12f, h * 0.82f)
                      val ptC = Offset(w * 0.88f, h * 0.82f)
                      val ptHFoot = Offset(ptA.x, ptB.y)
                      val ptMidBC = Offset((ptB.x + ptC.x) / 2f, ptB.y)
                      val ptG = Offset((ptA.x + ptB.x + ptC.x) / 3f, (ptA.y + ptB.y + ptC.y) / 3f)
                      val ptI = Offset(w * 0.43f, h * 0.60f)
                      val ptO = Offset(w * 0.50f, h * 0.56f)

                      fun dist(p1: Offset, p2: Offset): Float =
                        sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))

                      val hitRadius = 46f
                      val nextTopic = when {
                        dist(tapOffset, ptG) < hitRadius || dist(tapOffset, ptMidBC) < hitRadius ->
                          HolographicTriangleTopic.MEDIANS_AND_CENTROID
                        dist(tapOffset, ptHFoot) < hitRadius || kotlin.math.abs(tapOffset.x - ptA.x) < 22f ->
                          HolographicTriangleTopic.ALTITUDES_AND_ORTHOCENTER
                        dist(tapOffset, ptI) < hitRadius ->
                          HolographicTriangleTopic.BISECTORS_AND_INCENTER
                        dist(tapOffset, ptO) < hitRadius ->
                          HolographicTriangleTopic.RADII_R_AND_CAP_R
                        else -> {
                          val all = HolographicTriangleTopic.entries
                          all[(all.indexOf(selectedTopic) + 1) % all.size]
                        }
                      }
                      playCardHapticSound(context, localView)
                      selectedTopic = nextTopic
                    }
                  }
              ) {
                val w = size.width
                val h = size.height

                // Subtle holographic engineering grid lines
                val gridStep = w / 10f
                for (i in 1..9) {
                  drawLine(
                    color = Color(0xFF06B6D4).copy(alpha = 0.06f),
                    start = Offset(i * gridStep, 0f),
                    end = Offset(i * gridStep, h),
                    strokeWidth = 1f
                  )
                }
                val hStep = h / 6f
                for (j in 1..5) {
                  drawLine(
                    color = Color(0xFF06B6D4).copy(alpha = 0.06f),
                    start = Offset(0f, j * hStep),
                    end = Offset(w, j * hStep),
                    strokeWidth = 1f
                  )
                }

                // Oblique Triangle Vertices: A (top-left-ish apex), B (bottom-left), C (bottom-right)
                val ptA = Offset(w * 0.37f, h * 0.14f)
                val ptB = Offset(w * 0.12f, h * 0.82f)
                val ptC = Offset(w * 0.88f, h * 0.82f)

                // Key Foot Points on Base BC (side a)
                // 1. Altitude foot H_a (perpendicular from A to BC)
                val ptAltitudeFoot = Offset(ptA.x, ptB.y)
                // 2. Median midpoint M_a of side BC
                val ptMedianFoot = Offset((ptB.x + ptC.x) / 2f, ptB.y)
                // 3. Angle Bisector foot T_a (divides BC in ratio c : b)
                val sideA = distBetween(ptB, ptC)
                val sideB = distBetween(ptA, ptC)
                val sideC = distBetween(ptA, ptB)
                val bisectorRatio = sideC / (sideB + sideC)
                val ptBisectorFoot = Offset(
                  x = ptB.x + (ptC.x - ptB.x) * bisectorRatio,
                  y = ptB.y
                )

                // Notable Triangle Centers:
                // Centroid G = (A + B + C) / 3
                val ptG = Offset(
                  x = (ptA.x + ptB.x + ptC.x) / 3f,
                  y = (ptA.y + ptB.y + ptC.y) / 3f
                )
                // Incenter I = (aA + bB + cC) / (a + b + c)
                val perimeter = sideA + sideB + sideC
                val ptI = Offset(
                  x = (sideA * ptA.x + sideB * ptB.x + sideC * ptC.x) / perimeter,
                  y = (sideA * ptA.y + sideB * ptB.y + sideC * ptC.y) / perimeter
                )
                // Inradius r = 2 * Area / perimeter (since base BC is horizontal, Area = 0.5 * sideA * (ptB.y - ptA.y))
                val triAreaPx = 0.5f * sideA * (ptB.y - ptA.y)
                val inradiusPx = (2f * triAreaPx) / perimeter

                // Circumcenter O (exact analytic circumcenter of A, B, C)
                val dVal = 2f * (ptA.x * (ptB.y - ptC.y) + ptB.x * (ptC.y - ptA.y) + ptC.x * (ptA.y - ptB.y))
                val aSq = ptA.x * ptA.x + ptA.y * ptA.y
                val bSq = ptB.x * ptB.x + ptB.y * ptB.y
                val cSq = ptC.x * ptC.x + ptC.y * ptC.y
                val ptO = Offset(
                  x = (aSq * (ptB.y - ptC.y) + bSq * (ptC.y - ptA.y) + cSq * (ptA.y - ptB.y)) / dVal,
                  y = (aSq * (ptC.x - ptB.x) + bSq * (ptA.x - ptC.x) + cSq * (ptB.x - ptA.x)) / dVal
                )

                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

                // Optional Radii (r, R) Holographic Rings
                val isRadiiSelected = selectedTopic == HolographicTriangleTopic.RADII_R_AND_CAP_R
                drawCircle(
                  color = Color(0xFF3B82F6).copy(alpha = if (isRadiiSelected) 0.55f * neonPulse else 0.18f),
                  radius = inradiusPx,
                  center = ptI,
                  style = Stroke(
                    width = if (isRadiiSelected) 2.2.dp.toPx() else 1.2.dp.toPx(),
                    pathEffect = dashEffect
                  )
                )

                // 1. Dashed Amber Line: Altitude (h_a)
                val isAltitudeSelected = selectedTopic == HolographicTriangleTopic.ALTITUDES_AND_ORTHOCENTER
                drawLine(
                  color = Color(0xFFF59E0B).copy(alpha = if (isAltitudeSelected) neonPulse else 0.65f),
                  start = ptA,
                  end = ptAltitudeFoot,
                  strokeWidth = if (isAltitudeSelected) 3.2.dp.toPx() else 2.dp.toPx(),
                  cap = StrokeCap.Round,
                  pathEffect = dashEffect
                )
                // Right-angle marker at H_a
                val sqSize = 8.dp.toPx()
                drawLine(
                  color = Color(0xFFF59E0B).copy(alpha = 0.85f),
                  start = Offset(ptAltitudeFoot.x, ptAltitudeFoot.y - sqSize),
                  end = Offset(ptAltitudeFoot.x + sqSize, ptAltitudeFoot.y - sqSize),
                  strokeWidth = 1.4.dp.toPx()
                )
                drawLine(
                  color = Color(0xFFF59E0B).copy(alpha = 0.85f),
                  start = Offset(ptAltitudeFoot.x + sqSize, ptAltitudeFoot.y - sqSize),
                  end = Offset(ptAltitudeFoot.x + sqSize, ptAltitudeFoot.y),
                  strokeWidth = 1.4.dp.toPx()
                )

                // 2. Dashed Emerald Line: Angle Bisector (t_a)
                val isBisectorSelected = selectedTopic == HolographicTriangleTopic.BISECTORS_AND_INCENTER
                drawLine(
                  color = Color(0xFF10B981).copy(alpha = if (isBisectorSelected) neonPulse else 0.68f),
                  start = ptA,
                  end = ptBisectorFoot,
                  strokeWidth = if (isBisectorSelected) 3.2.dp.toPx() else 2.dp.toPx(),
                  cap = StrokeCap.Round,
                  pathEffect = dashEffect
                )

                // 3. Dashed Purple Line: Median (m_a)
                val isMedianSelected = selectedTopic == HolographicTriangleTopic.MEDIANS_AND_CENTROID
                drawLine(
                  color = Color(0xFFA855F7).copy(alpha = if (isMedianSelected) neonPulse else 0.72f),
                  start = ptA,
                  end = ptMedianFoot,
                  strokeWidth = if (isMedianSelected) 3.2.dp.toPx() else 2.dp.toPx(),
                  cap = StrokeCap.Round,
                  pathEffect = dashEffect
                )

                // 4. Cyan Glowing Outer Triangle Border Lines (A-B-C)
                val trianglePath = Path().apply {
                  moveTo(ptA.x, ptA.y)
                  lineTo(ptB.x, ptB.y)
                  lineTo(ptC.x, ptC.y)
                  close()
                }
                val isSidesSelected = selectedTopic == HolographicTriangleTopic.SIDES_AND_ANGLES
                // Outer Neon Halo
                drawPath(
                  path = trianglePath,
                  color = Color(0xFF06B6D4).copy(alpha = if (isSidesSelected) 0.35f * neonPulse else 0.18f),
                  style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )
                // Crisp Cyan Core Line
                drawPath(
                  path = trianglePath,
                  color = Color(0xFF06B6D4).copy(alpha = if (isSidesSelected) 1f else 0.88f),
                  style = Stroke(width = if (isSidesSelected) 2.8.dp.toPx() else 2.2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Vertex Dots (A, B, C)
                listOf(ptA, ptB, ptC).forEach { vPt ->
                  drawCircle(
                    color = Color(0xFF06B6D4).copy(alpha = 0.32f),
                    radius = 8.dp.toPx(),
                    center = vPt
                  )
                  drawCircle(
                    color = Color(0xFFE0F2FE),
                    radius = 4.dp.toPx(),
                    center = vPt
                  )
                }

                // Notable Center Points: Centroid (G), Incenter (I), Circumcenter (O)
                // Centroid (G) - Purple
                drawCircle(
                  color = Color(0xFFA855F7).copy(alpha = 0.35f * neonPulse),
                  radius = if (isMedianSelected) 10.dp.toPx() else 7.dp.toPx(),
                  center = ptG
                )
                drawCircle(
                  color = Color(0xFFA855F7),
                  radius = 4.5.dp.toPx(),
                  center = ptG
                )

                // Incenter (I) - Emerald
                drawCircle(
                  color = Color(0xFF10B981).copy(alpha = 0.35f * neonPulse),
                  radius = if (isBisectorSelected || isRadiiSelected) 10.dp.toPx() else 7.dp.toPx(),
                  center = ptI
                )
                drawCircle(
                  color = Color(0xFF10B981),
                  radius = 4.5.dp.toPx(),
                  center = ptI
                )

                // Circumcenter (O) - Electric Blue
                drawCircle(
                  color = Color(0xFF3B82F6).copy(alpha = 0.35f * neonPulse),
                  radius = if (isRadiiSelected) 10.dp.toPx() else 7.dp.toPx(),
                  center = ptO
                )
                drawCircle(
                  color = Color(0xFF3B82F6),
                  radius = 4.5.dp.toPx(),
                  center = ptO
                )
              }

              // Vertex & Anatomy Legend Overlay Labels inside Canvas
              Text(
                text = "A",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color(0xFF67E8F9),
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .padding(start = 112.dp, top = 8.dp)
              )
              Text(
                text = "B",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color(0xFF67E8F9),
                modifier = Modifier
                  .align(Alignment.BottomStart)
                  .padding(start = 18.dp, bottom = 22.dp)
              )
              Text(
                text = "C",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color(0xFF67E8F9),
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .padding(end = 18.dp, bottom = 22.dp)
              )

              // Compact Color Legend Bar at Bottom of Canvas
              Row(
                modifier = Modifier
                  .align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .background(Color(0xFF020617).copy(alpha = 0.78f))
                  .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
              ) {
                HolographicLegendTag(color = Color(0xFFF59E0B), label = "h_a Altitude")
                HolographicLegendTag(color = Color(0xFF10B981), label = "t_a Bisector • I")
                HolographicLegendTag(color = Color(0xFFA855F7), label = "m_a Median • G")
                HolographicLegendTag(color = Color(0xFF3B82F6), label = "Center O")
              }
            }
          }

          // ==================================================================
          // INTERACTIVE SELECTOR CHIPS (BELOW CANVAS)
          // [Sides & Angles] [Medians & Centroid] [Altitudes & Orthocenter] [Bisectors & Incenter] [Radii (r, R)]
          // ==================================================================
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            HolographicTriangleTopic.entries.forEach { topic ->
              val isSelected = selectedTopic == topic
              Surface(
                onClick = {
                  playCardHapticSound(context, localView)
                  selectedTopic = topic
                },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                  topic.accentColor.copy(alpha = 0.22f)
                } else {
                  Color(0xFF1E293B).copy(alpha = 0.75f)
                },
                border = BorderStroke(
                  width = if (isSelected) 1.4.dp else 1.dp,
                  color = if (isSelected) topic.accentColor else Color.White.copy(alpha = 0.14f)
                ),
                modifier = Modifier.testTag("holo_chip_${topic.name.lowercase(Locale.ROOT)}")
              ) {
                Text(
                  text = topic.chipLabel,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                  ),
                  color = if (isSelected) topic.accentColor else Color(0xFFCBD5E1),
                  maxLines = 1,
                  softWrap = false,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
              }
            }
          }

          // ==================================================================
          // DYNAMIC CONCEPT CARD (UPDATES WHEN CHIP OR CANVAS ELEMENT IS TAPPED)
          // ==================================================================
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF111827).copy(alpha = 0.94f),
            border = BorderStroke(1.2.dp, selectedTopic.accentColor.copy(alpha = 0.48f)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("holographic_triangle_concept_card")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = selectedTopic.conceptTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontSize = 16.sp,
                  fontWeight = FontWeight.ExtraBold
                ),
                color = selectedTopic.accentColor
              )

              Text(
                text = selectedTopic.engineeringDefinition,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 13.sp,
                  lineHeight = 18.sp,
                  fontWeight = FontWeight.Medium
                ),
                color = Color(0xFFE2E8F0)
              )

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.72f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Text(
                    text = "GEOMETRIC THEOREM",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontFamily = FontFamily.Monospace,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.ExtraBold,
                      letterSpacing = 0.8.sp
                    ),
                    color = selectedTopic.accentColor
                  )
                  Text(
                    text = selectedTopic.geometricTheorem,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.5.sp,
                      lineHeight = 17.5.sp,
                      fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFFCBD5E1)
                  )
                }
              }

              // Primary Board Exam Formula Capsule
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF060B16),
                border = BorderStroke(1.dp, selectedTopic.accentColor.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = selectedTopic.boardExamFormula,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.5.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = Color(0xFF67E8F9),
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HolographicLegendTag(
  color: Color,
  label: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(7.dp)
        .clip(CircleShape)
        .background(color)
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold
      ),
      color = Color(0xFFE2E8F0),
      maxLines = 1,
      softWrap = false
    )
  }
}

private fun distBetween(p1: Offset, p2: Offset): Float =
  sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))

@Composable
private fun InteractiveReferenceFormulaCard(
  formula: ReferenceFormulaCardData,
  isStarred: Boolean,
  onToggleStar: () -> Unit,
  metrics: ResponsiveMetrics
) {
  val palette = currentGlassPalette()
  var isExpanded by rememberSaveable(formula.id) { mutableStateOf(false) }

  LiquidGlassCard(
    onClick = { isExpanded = !isExpanded },
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
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Top Tag + Star Favorite Toggle Icon
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
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
              text = formula.mathSubTab.uppercase(Locale.ROOT),
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = metrics.scaledSp(10),
                fontWeight = FontWeight.ExtraBold
              ),
              color = palette.accentIndigo
            )
          }

          if (formula.isCustom) {
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.16f))
                .border(BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.40f)), CircleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "CUSTOM",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = 9.5.sp,
                  fontWeight = FontWeight.ExtraBold
                ),
                color = Color(0xFF10B981)
              )
            }
          }
        }

        IconButton(
          onClick = onToggleStar,
          modifier = Modifier
            .size(40.dp)
            .testTag("star_formula_button_${formula.id}")
        ) {
          Icon(
            imageVector = if (isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = if (isStarred) "Remove from Starred" else "Star formula",
            tint = if (isStarred) Color(0xFFFBBF24) else palette.textSecondary,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      // Formula Title & Summary Tag
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = formula.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = metrics.scaledSp(17),
            fontWeight = FontWeight.ExtraBold
          ),
          color = palette.textPrimary
        )
        Text(
          text = formula.summaryTag,
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = metrics.scaledSp(12),
            fontWeight = FontWeight.Medium
          ),
          color = palette.textSecondary
        )
      }

      // High-Contrast Equation Banner (JetBrainsMonoFamily)
      LiquidGlassContainer(
        shape = RoundedCornerShape(16.dp),
        fillAlpha = 0.82f,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = formula.equationBanner,
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = metrics.scaledSp(14),
            lineHeight = metrics.scaledSp(20),
            fontWeight = FontWeight.ExtraBold
          ),
          color = palette.accentIndigo,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
      }

      // Expandable "Where:" Variable Breakdown Header (clean, without cluttered side buttons)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isExpanded) "Hide \"Where:\" Variable Breakdown" else "Tap for \"Where:\" Variable Breakdown",
          style = MaterialTheme.typography.labelMedium.copy(
            fontSize = metrics.scaledSp(12),
            fontWeight = FontWeight.Bold
          ),
          color = palette.accentIndigo
        )
        Icon(
          imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
          contentDescription = null,
          tint = palette.accentIndigo,
          modifier = Modifier.size(18.dp)
        )
      }

      // Collapsible "Where:" Variable Breakdown Section
      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = palette.nestedCardFill.copy(alpha = 0.72f),
          border = palette.nestedBorder,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "WHERE (PHYSICAL MEANING & METRIC UNITS):",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
              ),
              color = palette.accentIndigo
            )
            formula.variableNotes.forEach { note ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
              ) {
                Text(
                  text = note.symbol,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                  ),
                  color = palette.textPrimary,
                  modifier = Modifier.widthIn(min = 68.dp)
                )
                Text(
                  text = "${note.meaning} (${note.unit})",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  color = palette.textSecondary,
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

@Composable
private fun GeometryInteractiveShapeViewerSection(
  shapes: List<GeometryShapeEntry>,
  selectedShapeId: String,
  onSelectShape: (String) -> Unit,
  onSendShapeToFlashcards: (GeometryShapeEntry) -> Unit,
  metrics: ResponsiveMetrics
) {
  val palette = currentGlassPalette()
  val selectedShape = shapes.firstOrNull { it.id == selectedShapeId } ?: shapes.first()

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "INTERACTIVE VISUAL SHAPE VIEWER (TAP A SHAPE TO INSPECT)",
      style = MaterialTheme.typography.labelSmall.copy(
        fontFamily = JetBrainsMonoFamily,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.9.sp
      ),
      color = palette.textSecondary
    )

    // 3x3 Grid of 9 Shape Selector Cards
    val rows = remember(shapes) { shapes.chunked(3) }
    rows.forEach { rowShapes ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        rowShapes.forEach { shape ->
          val isSelected = shape.id == selectedShape.id
          Surface(
            onClick = { onSelectShape(shape.id) },
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) {
              Color(0xFF6366F1).copy(alpha = if (palette.isDark) 0.30f else 0.16f)
            } else {
              palette.cardSurface.copy(alpha = 0.82f)
            },
            border = BorderStroke(
              width = if (isSelected) 1.6.dp else 1.dp,
              color = if (isSelected) Color(0xFF6366F1) else Color.White.copy(alpha = 0.25f)
            ),
            shadowElevation = if (isSelected) 10.dp else 4.dp,
            modifier = Modifier
              .weight(1f)
              .testTag("geometry_shape_card_${shape.id}")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              GeometryShapeCanvasOutline(
                shapeId = shape.id,
                strokeColor = if (isSelected) palette.accentIndigo else palette.textPrimary,
                accentColor = palette.accentIndigo,
                modifier = Modifier.size(46.dp)
              )
              Text(
                text = shape.title,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontSize = 11.5.sp,
                  fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                ),
                color = if (isSelected) palette.accentIndigo else palette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }

    // Expanded Interactive Inspector Sheet for the selected shape
    AnimatedContent(
      targetState = selectedShape,
      transitionSpec = {
        (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.96f))
          .togetherWith(fadeOut(animationSpec = tween(150)))
      },
      label = "geometryInspectorSheetTransition"
    ) { activeShape ->
      GeometryInspectorSheetCard(
        shape = activeShape,
        metrics = metrics,
        onSendToFlashcards = { onSendShapeToFlashcards(activeShape) }
      )
    }
  }
}

@Composable
private fun GeometryInspectorSheetCard(
  shape: GeometryShapeEntry,
  metrics: ResponsiveMetrics,
  onSendToFlashcards: () -> Unit
) {
  val palette = currentGlassPalette()
  LiquidGlassCard(
    shape = RoundedCornerShape(26.dp),
    elevation = 10.dp,
    borderStroke = BorderStroke(1.4.dp, Color(0xFF6366F1).copy(alpha = 0.55f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("geometry_inspector_sheet_${shape.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(metrics.scaledDp(18)),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF6366F1).copy(alpha = 0.14f))
              .border(BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.40f)), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
          ) {
            GeometryShapeCanvasOutline(
              shapeId = shape.id,
              strokeColor = palette.accentIndigo,
              accentColor = Color(0xFF06B6D4),
              modifier = Modifier.size(38.dp)
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = shape.title,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = metrics.scaledSp(19),
                  fontWeight = FontWeight.ExtraBold
                ),
                color = palette.textPrimary
              )
              Box(
                modifier = Modifier
                  .clip(CircleShape)
                  .background(
                    if (shape.is3DSolid) Color(0xFF8B5CF6).copy(alpha = 0.18f)
                    else Color(0xFF06B6D4).copy(alpha = 0.18f)
                  )
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (shape.is3DSolid) "3D SOLID" else "2D SECTION",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold
                  ),
                  color = if (shape.is3DSolid) Color(0xFF8B5CF6) else Color(0xFF06B6D4)
                )
              }
            }
            Text(
              text = shape.subtitle,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.Medium
              ),
              color = palette.textSecondary
            )
          }
        }
      }

      // Inspector Property Rows: Area, Perimeter/Circumference, Volume & Surface Area (3D), Centroid, Moment of Inertia
      GeometryPropertyBannerRow(
        label = "AREA (A)",
        formula = shape.areaFormula
      )
      GeometryPropertyBannerRow(
        label = shape.perimeterOrCircumferenceLabel.uppercase(Locale.ROOT),
        formula = shape.perimeterOrCircumferenceFormula
      )
      if (shape.volumeFormula != null) {
        GeometryPropertyBannerRow(
          label = "VOLUME (V)",
          formula = shape.volumeFormula
        )
      }
      if (shape.surfaceAreaFormula != null) {
        GeometryPropertyBannerRow(
          label = "SURFACE AREA (A_s)",
          formula = shape.surfaceAreaFormula
        )
      }
      GeometryPropertyBannerRow(
        label = "CENTROID LOCATION (ȳ FROM BASE)",
        formula = shape.centroidFromBase
      )
      GeometryPropertyBannerRow(
        label = "MOMENT OF INERTIA (I_x, I_y)",
        formula = "${shape.momentOfInertiaIx}\n${shape.momentOfInertiaIy}"
      )

      Text(
        text = "Where: ${shape.variableLegend}",
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Medium
        ),
        color = palette.textSecondary
      )

      // "Send to Flashcards" button
      Button(
        onClick = onSendToFlashcards,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF6366F1),
          contentColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("send_shape_to_flashcards_${shape.id}")
      ) {
        Icon(
          imageVector = Icons.Filled.AutoAwesome,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Send to Flashcards",
          style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
          ),
          color = Color.White
        )
      }
    }
  }
}

@Composable
private fun GeometryPropertyBannerRow(
  label: String,
  formula: String
) {
  val palette = currentGlassPalette()
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = palette.nestedCardFill.copy(alpha = 0.78f),
    border = palette.nestedBorder,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 9.dp),
      verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = JetBrainsMonoFamily,
          fontSize = 9.5.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 0.7.sp
        ),
        color = palette.textSecondary
      )
      Text(
        text = formula,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = JetBrainsMonoFamily,
          fontSize = 12.5.sp,
          lineHeight = 18.sp,
          fontWeight = FontWeight.Bold
        ),
        color = palette.accentIndigo
      )
    }
  }
}

@Composable
private fun GeometryShapeCanvasOutline(
  shapeId: String,
  strokeColor: Color,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
    val dashedAccent = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round)

    when (shapeId) {
      "circle" -> {
        val r = minOf(w, h) * 0.38f
        val centerPt = Offset(w / 2f, h / 2f)
        drawCircle(color = strokeColor, radius = r, center = centerPt, style = stroke)
        drawLine(
          color = accentColor.copy(alpha = 0.75f),
          start = centerPt,
          end = Offset(centerPt.x + r, centerPt.y),
          strokeWidth = 1.5.dp.toPx()
        )
      }
      "right_triangle" -> {
        val path = Path().apply {
          moveTo(w * 0.18f, h * 0.82f)
          lineTo(w * 0.84f, h * 0.82f)
          lineTo(w * 0.18f, h * 0.18f)
          close()
        }
        drawPath(path = path, color = strokeColor, style = stroke)
        // Right-angle square marker
        val sq = w * 0.12f
        drawLine(accentColor, Offset(w * 0.18f + sq, h * 0.82f), Offset(w * 0.18f + sq, h * 0.82f - sq), 1.4.dp.toPx())
        drawLine(accentColor, Offset(w * 0.18f, h * 0.82f - sq), Offset(w * 0.18f + sq, h * 0.82f - sq), 1.4.dp.toPx())
      }
      "isosceles_triangle" -> {
        val path = Path().apply {
          moveTo(w * 0.50f, h * 0.16f)
          lineTo(w * 0.86f, h * 0.82f)
          lineTo(w * 0.14f, h * 0.82f)
          close()
        }
        drawPath(path = path, color = strokeColor, style = stroke)
        drawLine(accentColor.copy(alpha = 0.7f), Offset(w * 0.50f, h * 0.16f), Offset(w * 0.50f, h * 0.82f), 1.3.dp.toPx())
      }
      "trapezoid" -> {
        val path = Path().apply {
          moveTo(w * 0.28f, h * 0.24f)
          lineTo(w * 0.72f, h * 0.24f)
          lineTo(w * 0.88f, h * 0.78f)
          lineTo(w * 0.12f, h * 0.78f)
          close()
        }
        drawPath(path = path, color = strokeColor, style = stroke)
      }
      "parallelogram" -> {
        val path = Path().apply {
          moveTo(w * 0.28f, h * 0.25f)
          lineTo(w * 0.88f, h * 0.25f)
          lineTo(w * 0.72f, h * 0.78f)
          lineTo(w * 0.12f, h * 0.78f)
          close()
        }
        drawPath(path = path, color = strokeColor, style = stroke)
      }
      "cylinder" -> {
        val topRect = Size(w * 0.56f, h * 0.18f)
        val leftX = w * 0.22f
        val rightX = w * 0.78f
        drawOval(
          color = strokeColor,
          topLeft = Offset(leftX, h * 0.14f),
          size = topRect,
          style = stroke
        )
        drawOval(
          color = strokeColor,
          topLeft = Offset(leftX, h * 0.68f),
          size = topRect,
          style = stroke
        )
        drawLine(strokeColor, Offset(leftX, h * 0.23f), Offset(leftX, h * 0.77f), stroke.width)
        drawLine(strokeColor, Offset(rightX, h * 0.23f), Offset(rightX, h * 0.77f), stroke.width)
      }
      "sphere" -> {
        val r = minOf(w, h) * 0.38f
        val centerPt = Offset(w / 2f, h / 2f)
        drawCircle(color = strokeColor, radius = r, center = centerPt, style = stroke)
        drawOval(
          color = accentColor.copy(alpha = 0.75f),
          topLeft = Offset(centerPt.x - r, centerPt.y - r * 0.35f),
          size = Size(r * 2f, r * 0.70f),
          style = dashedAccent
        )
      }
      "cone" -> {
        val leftX = w * 0.20f
        val rightX = w * 0.80f
        val apex = Offset(w * 0.50f, h * 0.14f)
        drawLine(strokeColor, apex, Offset(leftX, h * 0.75f), stroke.width)
        drawLine(strokeColor, apex, Offset(rightX, h * 0.75f), stroke.width)
        drawOval(
          color = strokeColor,
          topLeft = Offset(leftX, h * 0.66f),
          size = Size(w * 0.60f, h * 0.18f),
          style = stroke
        )
      }
      "frustum" -> {
        val topLeftX = w * 0.32f
        val topRightX = w * 0.68f
        val botLeftX = w * 0.16f
        val botRightX = w * 0.84f
        drawOval(
          color = strokeColor,
          topLeft = Offset(topLeftX, h * 0.18f),
          size = Size(topRightX - topLeftX, h * 0.14f),
          style = stroke
        )
        drawOval(
          color = strokeColor,
          topLeft = Offset(botLeftX, h * 0.68f),
          size = Size(botRightX - botLeftX, h * 0.18f),
          style = stroke
        )
        drawLine(strokeColor, Offset(topLeftX, h * 0.25f), Offset(botLeftX, h * 0.77f), stroke.width)
        drawLine(strokeColor, Offset(topRightX, h * 0.25f), Offset(botRightX, h * 0.77f), stroke.width)
      }
    }
  }
}

@Composable
private fun VaultCategoryPlaceholderCard(
  title: String,
  subtitle: String,
  badgeText: String,
  isPsad: Boolean,
  metrics: ResponsiveMetrics,
  onRequestPriorityTopic: () -> Unit
) {
  val palette = currentGlassPalette()
  LiquidGlassCard(
    shape = RoundedCornerShape(28.dp),
    elevation = 10.dp,
    borderStroke = BorderStroke(
      width = 1.2.dp,
      brush = Brush.linearGradient(
        listOf(
          Color.White.copy(alpha = 0.45f),
          Color.White.copy(alpha = 0.10f)
        )
      )
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag(if (isPsad) "psad_placeholder_card" else "hpge_placeholder_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(metrics.scaledDp(22)),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
              brush = Brush.linearGradient(
                if (isPsad) {
                  listOf(Color(0xFF6366F1).copy(alpha = 0.25f), Color(0xFF8B5CF6).copy(alpha = 0.25f))
                } else {
                  listOf(Color(0xFF06B6D4).copy(alpha = 0.25f), Color(0xFF3B82F6).copy(alpha = 0.25f))
                }
              )
            )
            .border(
              BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
              RoundedCornerShape(18.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          if (isPsad) {
            Icon(
              imageVector = Icons.Filled.Architecture,
              contentDescription = title,
              tint = palette.accentIndigo,
              modifier = Modifier.size(30.dp)
            )
          } else {
            // Custom Water Wave + Soil Strata icon for HPGE
            Canvas(modifier = Modifier.size(30.dp)) {
              val w = size.width
              val h = size.height
              val wavePath = Path().apply {
                moveTo(0f, h * 0.30f)
                cubicTo(w * 0.25f, h * 0.10f, w * 0.35f, h * 0.50f, w * 0.55f, h * 0.30f)
                cubicTo(w * 0.75f, h * 0.10f, w * 0.85f, h * 0.50f, w, h * 0.30f)
              }
              drawPath(wavePath, color = Color(0xFF06B6D4), style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round))
              drawLine(Color(0xFF6366F1), Offset(0f, h * 0.62f), Offset(w, h * 0.62f), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
              drawLine(Color(0xFF8B5CF6), Offset(w * 0.12f, h * 0.84f), Offset(w * 0.88f, h * 0.84f), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
            }
          }
        }

        Surface(
          shape = CircleShape,
          color = Color(0xFFF59E0B).copy(alpha = 0.16f),
          border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f))
        ) {
          Text(
            text = badgeText,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = JetBrainsMonoFamily,
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold
            ),
            color = Color(0xFFF59E0B),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }

      Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontSize = metrics.scaledSp(20),
          fontWeight = FontWeight.ExtraBold
        ),
        color = palette.textPrimary
      )

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = metrics.scaledSp(14),
          lineHeight = metrics.scaledSp(19),
          fontWeight = FontWeight.Medium
        ),
        color = palette.textSecondary
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Plus (+) button below allows custom ${if (isPsad) "PSAD" else "HPGE"} entries",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          ),
          color = palette.textSecondary.copy(alpha = 0.8f),
          modifier = Modifier.weight(1f)
        )

        OutlinedButton(
          onClick = onRequestPriorityTopic,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, palette.accentIndigo.copy(alpha = 0.55f)),
          modifier = Modifier.testTag(if (isPsad) "psad_request_priority_button" else "hpge_request_priority_button")
        ) {
          Text(
            text = "Request Priority Topic",
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold
            ),
            color = palette.accentIndigo
          )
        }
      }
    }
  }
}

@Composable
private fun AddCustomFormulaGlassDialog(
  metrics: ResponsiveMetrics,
  onDismiss: () -> Unit,
  onSaveFormula: (
    title: String,
    categoryTab: String,
    mathSubTab: String,
    equation: String,
    variableNotes: String
  ) -> Unit
) {
  val palette = currentGlassPalette()
  var title by rememberSaveable { mutableStateOf("") }
  var selectedCategory by rememberSaveable { mutableStateOf("Mathematics") }
  var selectedMathSubTab by rememberSaveable { mutableStateOf("Algebra") }
  var equation by rememberSaveable { mutableStateOf("") }
  var variableNotes by rememberSaveable { mutableStateOf("") }
  var errorText by rememberSaveable { mutableStateOf<String?>(null) }

  val categoryOptions = remember { listOf("Mathematics", "PSAD", "HPGE") }
  val mathSubOptions = remember { listOf("Algebra", "Trigonometry", "Geometry", "Analytic Geometry") }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = palette.dialogSurface,
      border = BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(
          listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.10f))
        )
      ),
      shadowElevation = 20.dp,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = 480.dp)
        .testTag("add_custom_formula_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Add Custom Formula",
              style = MaterialTheme.typography.titleLarge.copy(
                fontSize = metrics.scaledSp(19),
                fontWeight = FontWeight.ExtraBold
              ),
              color = palette.textPrimary
            )
            Text(
              text = "Saved permanently to your Formula Vault JSON storage",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.accentIndigo
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close dialog",
              tint = palette.textSecondary
            )
          }
        }

        OutlinedTextField(
          value = title,
          onValueChange = {
            title = it
            errorText = null
          },
          label = { Text("Formula Title") },
          placeholder = { Text("e.g., Euler Buckling Stress") },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_formula_title_input")
        )

        Text(
          text = "Category",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = palette.textSecondary
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          categoryOptions.forEach { cat ->
            val isSelected = selectedCategory == cat
            Surface(
              onClick = { selectedCategory = cat },
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) Color(0xFF6366F1) else palette.nestedCardFill,
              border = BorderStroke(
                1.dp,
                if (isSelected) Color.White.copy(alpha = 0.5f) else palette.textSecondary.copy(alpha = 0.25f)
              ),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = cat,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.White else palette.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 10.dp)
              )
            }
          }
        }

        if (selectedCategory == "Mathematics") {
          Text(
            text = "Mathematics Topic",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.textSecondary
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            mathSubOptions.forEach { sub ->
              val isSelected = selectedMathSubTab == sub
              Surface(
                onClick = { selectedMathSubTab = sub },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.22f) else palette.nestedCardFill,
                border = BorderStroke(
                  1.dp,
                  if (isSelected) Color(0xFF6366F1) else palette.textSecondary.copy(alpha = 0.22f)
                )
              ) {
                Text(
                  text = sub,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = if (isSelected) palette.accentIndigo else palette.textSecondary,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = equation,
          onValueChange = {
            equation = it
            errorText = null
          },
          label = { Text("Formula Equation") },
          placeholder = { Text("e.g., σ_cr = π²E / (KL/r)²") },
          shape = RoundedCornerShape(16.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_formula_equation_input")
        )

        OutlinedTextField(
          value = variableNotes,
          onValueChange = { variableNotes = it },
          label = { Text("Variable Notes (one per line, Symbol: Meaning)") },
          placeholder = { Text("E: Modulus of elasticity (MPa)\nKL/r: Effective slenderness ratio") },
          minLines = 3,
          shape = RoundedCornerShape(16.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_formula_notes_input")
        )

        errorText?.let { err ->
          Text(
            text = err,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFEF4444)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              if (title.trim().isEmpty()) {
                errorText = "Please enter a formula title"
              } else if (equation.trim().isEmpty()) {
                errorText = "Please enter the formula equation"
              } else {
                onSaveFormula(
                  title.trim(),
                  selectedCategory,
                  if (selectedCategory == "Mathematics") selectedMathSubTab else selectedCategory,
                  equation.trim(),
                  variableNotes.trim()
                )
              }
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF6366F1),
              contentColor = Color.White
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("save_custom_formula_button")
          ) {
            Text(
              text = "Save Formula",
              fontWeight = FontWeight.ExtraBold
            )
          }
        }
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
  var isImportDeckCodeDialogOpen by rememberSaveable { mutableStateOf(false) }
  var isPasteTextDialogOpen by rememberSaveable { mutableStateOf(false) }
  var pasteDialogInitialTitle by rememberSaveable { mutableStateOf("") }
  var pasteDialogNoticeMessage by rememberSaveable { mutableStateOf<String?>(null) }
  var addCardTargetDeckId by rememberSaveable { mutableStateOf<Int?>(null) }
  var resetMasteryTargetDeckId by rememberSaveable { mutableStateOf<Int?>(null) }

  // API Key Quick-Verify & Override Dialog state
  var isApiKeyDialogOpen by rememberSaveable { mutableStateOf(false) }

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
    val activeKey = viewModel.getActiveGeminiApiKey()
    if (GizmoBatchFlashcardEngine.buildValidKeyPool(activeKey).isEmpty()) {
      isApiKeyDialogOpen = true
      Toast.makeText(
        context,
        "Please enter a Gemini API Key in the Key Manager to generate flashcards.",
        Toast.LENGTH_LONG
      ).show()
      return
    }

    activeBatchJob?.cancel()
    batchDocumentName = "$deckTitle (${rawText.length} chars)"
    batchDeckTitle = deckTitle.ifBlank { "Lecture Notes Study Deck" }
    batchSubjectTag = subjectTag.ifBlank { "General" }
    currentBatchIndex = 1
    totalBatchCount = maxOf(1, (rawText.length + 3999) / 4000)
    batchStatusLabel = "Analyzing Chunk 1 of $totalBatchCount"
    accumulatedBatchCards = emptyList()
    latestGeneratedQuestion = ""
    latestGeneratedAnswer = ""
    extractionErrorDialogMessage = null
    isBatchExtracting = true

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
          ?: GizmoBatchFlashcardEngine.OFFLINE_ERROR_MESSAGE
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
      val activeKey = viewModel.getActiveGeminiApiKey()
      if (GizmoBatchFlashcardEngine.buildValidKeyPool(activeKey).isEmpty()) {
        isApiKeyDialogOpen = true
        Toast.makeText(
          context,
          "Please enter a Gemini API Key in the Key Manager to analyze documents.",
          Toast.LENGTH_LONG
        ).show()
        return@rememberLauncherForActivityResult
      }

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
      batchStatusLabel = "Analyzing Pages 1–2..."
      accumulatedBatchCards = emptyList()
      latestGeneratedQuestion = ""
      latestGeneratedAnswer = ""
      extractionErrorDialogMessage = null
      isBatchExtracting = true
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
            ?: GizmoBatchFlashcardEngine.OFFLINE_ERROR_MESSAGE
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

        if (viewModel.isSyncingFlashcards) {
          CircularProgressIndicator(
            modifier = Modifier
              .size(18.dp)
              .testTag("flashcards_firestore_sync_indicator"),
            strokeWidth = 2.dp,
            color = palette.accentIndigo
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

    // 2. Cleaner Action Bar with Theme-Aware Frosted Glass Pills ("Import Document", "📥 Import Code", & "+ New Deck")
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(metrics.scaledDp(8)),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          onClick = {
            playCardHapticSound(context, localView)
            try {
              documentPickerLauncher.launch(GizmoBatchFlashcardEngine.SUPPORTED_DOCUMENT_MIME_TYPES)
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
              .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.Description,
              contentDescription = "Import Document",
              tint = palette.accentIndigo,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "Import Doc",
              style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 11.5.sp,
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
            isImportDeckCodeDialogOpen = true
          },
          shape = CircleShape,
          color = palette.nestedCardFill,
          border = palette.glassBorder,
          shadowElevation = 2.dp,
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .testTag("import_deck_code_button")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "📥 Import Code",
              style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 11.5.sp,
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
              .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "New Deck",
              tint = palette.accentIndigo,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "+ New Deck",
              style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 11.5.sp,
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
                      val shareCode = GizmoBatchFlashcardEngine.encodeDeckToShareCode(deck)
                      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                      clipboard?.setPrimaryClip(ClipData.newPlainText("Sibil Deck Code", shareCode))
                      Toast.makeText(
                        context,
                        "Deck code copied to clipboard! Share this with your classmates.",
                        Toast.LENGTH_LONG
                      ).show()
                    },
                    modifier = Modifier
                      .size(48.dp)
                      .testTag("share_deck_button_${deck.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Share,
                      contentDescription = "Share Deck ${deck.title}",
                      tint = palette.accentIndigo,
                      modifier = Modifier.size(20.dp)
                    )
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
            text = "Analyzing document with Gemini AI...",
            style = MaterialTheme.typography.titleLarge.copy(
              fontSize = metrics.scaledSp(19),
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
              text = "⚡ ${accumulatedBatchCards.size} Cards Generated",
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

          // "Save & Stop" Button (Active as soon as >= 1 batch/card completes)
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
              text = "Save & Stop",
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

  // "📥 Import Code" Zero-Quota Shared Deck Dialog
  if (isImportDeckCodeDialogOpen) {
    ImportSharedDeckCodeDialog(
      metrics = metrics,
      onDismiss = { isImportDeckCodeDialogOpen = false },
      onImportDeck = { importedDeck ->
        val savedDeck = viewModel.addFlashcardDeck(
          title = importedDeck.title,
          subjectTag = importedDeck.subjectTag,
          initialCards = importedDeck.cards
        )
        isImportDeckCodeDialogOpen = false
        Toast.makeText(
          context,
          "Imported \"${savedDeck.title}\" (${savedDeck.cards.size} cards) with zero quota used!",
          Toast.LENGTH_LONG
        ).show()
      }
    )
  }
}

@Composable
private fun ImportSharedDeckCodeDialog(
  metrics: ResponsiveMetrics,
  onDismiss: () -> Unit,
  onImportDeck: (FlashcardDeck) -> Unit
) {
  val context = LocalContext.current
  val palette = currentGlassPalette()
  var deckCodeInput by rememberSaveable { mutableStateOf("") }
  var decodeError by rememberSaveable { mutableStateOf<String?>(null) }

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
          imageVector = Icons.Default.Share,
          contentDescription = null,
          tint = palette.accentIndigo,
          modifier = Modifier.size(22.dp)
        )
        Text(
          text = "Import Classmate Deck",
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
          text = "One classmate can generate a deck from a PDF or DOCX and tap Share. Paste their deck code below to load the full deck instantly with zero AI quota usage.",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = metrics.scaledSp(12)
          ),
          color = palette.textSecondary
        )

        OutlinedTextField(
          value = deckCodeInput,
          onValueChange = {
            deckCodeInput = it
            decodeError = null
          },
          label = { Text("Paste Deck Code from Classmate") },
          placeholder = { Text("eyJ2IjoxLCJ0aXRsZSI6...") },
          isError = decodeError != null,
          minLines = 3,
          maxLines = 6,
          shape = RoundedCornerShape(14.dp),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("import_deck_code_input")
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
                deckCodeInput = pasted
                decodeError = null
              } else {
                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
              }
            },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.ContentPaste,
              contentDescription = "Paste Deck Code",
              tint = palette.accentIndigo,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Paste from Clipboard",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = palette.accentIndigo
            )
          }

          if (deckCodeInput.isNotEmpty()) {
            TextButton(
              onClick = {
                deckCodeInput = ""
                decodeError = null
              },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("Clear", fontSize = 12.sp, color = palette.textSecondary)
            }
          }
        }

        if (decodeError != null) {
          Text(
            text = decodeError.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = palette.accentRed
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val decoded = GizmoBatchFlashcardEngine.decodeDeckFromShareCode(deckCodeInput)
          if (decoded == null) {
            decodeError = "Invalid deck code. Please copy the full code shared by your classmate."
          } else {
            onImportDeck(decoded)
          }
        },
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF4F46E5),
          contentColor = Color.White
        ),
        modifier = Modifier.testTag("confirm_import_deck_code_button")
      ) {
        Text("Import Deck", fontWeight = FontWeight.Bold)
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
 * Zero-asset procedural audio synthesis utility using Android's native `ToneGenerator`
 * so flashcards and mastery celebrations require no external MP3 assets.
 */
object SoundEffectsManager {
  fun playGoodButtonBeep() {
    try {
      val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
      toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 60)
      Handler(Looper.getMainLooper()).postDelayed({
        try {
          toneGen.release()
        } catch (_: Exception) {
        }
      }, 90L)
    } catch (_: Exception) {
    }
  }

  fun playMasteryFanfare(coroutineScope: CoroutineScope) {
    coroutineScope.launch(Dispatchers.Default) {
      var toneGen: ToneGenerator? = null
      try {
        toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 78)
        // Triumphant rising 4-note celebratory sequence (C6 -> E6 -> G6 -> C7) over ~600ms
        val notes = listOf(
          ToneGenerator.TONE_DTMF_1 to 110,
          ToneGenerator.TONE_DTMF_3 to 110,
          ToneGenerator.TONE_DTMF_6 to 120,
          ToneGenerator.TONE_PROP_BEEP2 to 180
        )
        for ((toneType, durationMs) in notes) {
          toneGen.startTone(toneType, durationMs)
          delay(150L)
        }
      } catch (_: Exception) {
      } finally {
        try {
          toneGen?.release()
        } catch (_: Exception) {
        }
      }
    }
  }
}

data class StudyCardHistoryEntry(
  val card: FlashcardItem,
  val wasMarkedMastered: Boolean
)

data class ConfettiParticle(
  var x: Float,
  var y: Float,
  val vx: Float,
  val vy: Float,
  val color: Color,
  val size: Float,
  var rotation: Float,
  val vRot: Float
)

/**
 * Pure Jetpack Compose Canvas particle effect (`ConfettiOverlay`) for the "All Cards Mastered!"
 * completion screen. Spawns 50 colorful confetti particles and animates them downward with gravity,
 * horizontal wind sine drift, and 2D rotation without blocking buttons underneath.
 */
@Composable
fun ConfettiOverlay(
  modifier: Modifier = Modifier
) {
  val confettiColors = remember {
    listOf(
      Color(0xFF6366F1), // Indigo
      Color(0xFF10B981), // Emerald
      Color(0xFFF59E0B), // Amber
      Color(0xFFF43F5E), // Coral
      Color(0xFF8B5CF6), // Violet
      Color(0xFF06B6D4)  // Cyan
    )
  }

  val particles = remember {
    List(50) { index ->
      ConfettiParticle(
        x = (index * 37 % 100) / 100f,
        y = -Random.nextFloat() * 0.45f,
        vx = (Random.nextFloat() - 0.5f) * 0.18f,
        vy = 0.22f + Random.nextFloat() * 0.32f,
        color = confettiColors[index % confettiColors.size],
        size = 7f + Random.nextFloat() * 7f,
        rotation = Random.nextFloat() * 360f,
        vRot = (Random.nextFloat() - 0.5f) * 240f
      )
    }
  }

  var frameTick by remember { mutableLongStateOf(0L) }
  var elapsedSeconds by remember { mutableStateOf(0f) }

  LaunchedEffect(Unit) {
    var lastFrameNanos = 0L
    while (true) {
      withFrameNanos { nowNanos ->
        if (lastFrameNanos == 0L) {
          lastFrameNanos = nowNanos
        }
        val dt = ((nowNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
        lastFrameNanos = nowNanos
        elapsedSeconds += dt
        for ((idx, p) in particles.withIndex()) {
          val windDrift = sin(elapsedSeconds * 2.5f + idx * 0.4f) * 0.08f
          p.x += (p.vx + windDrift) * dt
          p.y += p.vy * dt
          p.rotation = (p.rotation + p.vRot * dt) % 360f
          if (p.y > 1.15f) {
            p.y = -0.08f
            p.x = Random.nextFloat()
          }
          if (p.x < -0.05f) p.x = 1.05f
          if (p.x > 1.05f) p.x = -0.05f
        }
        frameTick = nowNanos
      }
    }
  }

  Canvas(
    modifier = modifier.testTag("mastery_confetti_overlay")
  ) {
    // Read frameTick so Canvas redraws on each animation frame
    if (frameTick >= 0L) {
      val canvasW = size.width
      val canvasH = size.height
      for (p in particles) {
        val px = p.x * canvasW
        val py = p.y * canvasH
        if (py in -30f..(canvasH + 30f)) {
          rotate(degrees = p.rotation, pivot = Offset(px, py)) {
            drawRoundRect(
              color = p.color,
              topLeft = Offset(px - p.size / 2f, py - p.size * 0.3f),
              size = Size(p.size, p.size * 0.6f),
              cornerRadius = CornerRadius(2f, 2f)
            )
          }
        }
      }
    }
  }
}

/**
 * Interactive Study Mode Screen (`FlashcardStudyScreen`):
 * - Queue Immersion HUD with Remaining/Mastered pills, active card interval micro-badge, and progress bar.
 * - Undo / Previous Card Review System via top HUD button (`Icons.AutoMirrored.Filled.Undo`) and swipe-right gesture.
 * - Zero-asset audio synthesis (`SoundEffectsManager.playGoodButtonBeep()` and `playMasteryFanfare()`).
 * - Custom Jetpack Compose `ConfettiOverlay` when all cards are mastered.
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
  val coroutineScope = rememberCoroutineScope()
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
  val studyHistory = remember(deck.id) {
    mutableStateListOf<StudyCardHistoryEntry>()
  }
  var isFlipped by remember(deck.id) { mutableStateOf(false) }
  var cardAdvanceStep by remember(deck.id) { mutableIntStateOf(0) }
  var isResetConfirmDialogOpen by rememberSaveable(deck.id) { mutableStateOf(false) }

  val recordCardHistory = { cardSnapshot: FlashcardItem, markedMastered: Boolean ->
    studyHistory.add(
      StudyCardHistoryEntry(
        card = cardSnapshot.copy(),
        wasMarkedMastered = markedMastered
      )
    )
    if (studyHistory.size > 20) {
      studyHistory.removeAt(0)
    }
  }

  val performUndoPreviousCard = {
    if (studyHistory.isNotEmpty()) {
      playCardHapticSound(context, localView)
      val lastEntry = studyHistory.removeAt(studyHistory.lastIndex)
      val restoredCard = lastEntry.card.copy(
        isMastered = if (lastEntry.wasMarkedMastered) false else lastEntry.card.isMastered,
        masteryLevel = lastEntry.card.masteryLevel.ifBlank { "New" }
      )
      if (lastEntry.wasMarkedMastered) {
        masteredCards.removeAll { it.id == restoredCard.id }
        restoredCard.isMastered = false
      }
      onRateCard(restoredCard.id, restoredCard.masteryLevel)
      studyQueue.removeAll { it.id == restoredCard.id }
      studyQueue.add(0, restoredCard)
      isFlipped = false
      cardAdvanceStep++
    }
  }

  val performStudyReset = {
    playCardHapticSound(context, localView)
    onResetMastery()
    masteredCards.clear()
    studyHistory.clear()
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

      // Top HUD "Undo / Previous Card" Icon Button
      val canUndo = studyHistory.isNotEmpty()
      Surface(
        onClick = {
          if (canUndo) {
            performUndoPreviousCard()
          }
        },
        enabled = canUndo,
        shape = CircleShape,
        color = if (canUndo) palette.nestedCardFill else palette.trackColor.copy(alpha = 0.45f),
        border = palette.glassBorder,
        shadowElevation = if (canUndo) 2.dp else 0.dp,
        modifier = Modifier
          .size(40.dp)
          .testTag("study_top_bar_undo_button")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Undo,
            contentDescription = "Undo Previous Card",
            tint = if (canUndo) palette.accentIndigo else palette.textSecondary.copy(alpha = 0.45f),
            modifier = Modifier.size(18.dp)
          )
        }
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
      LaunchedEffect(Unit) {
        SoundEffectsManager.playMasteryFanfare(coroutineScope)
      }

      // Deck Mastered Celebratory Screen + Custom Confetti Celebration Overlay
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = metrics.scaledDp(12))
      ) {
        Card(
          shape = RoundedCornerShape(28.dp),
          colors = CardDefaults.cardColors(containerColor = palette.cardFill),
          elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
          border = BorderStroke(1.5.dp, goodAccentColor.copy(alpha = 0.55f)),
          modifier = Modifier
            .fillMaxWidth()
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
                  studyHistory.clear()
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

        // Confetti Celebration Overlay drawn above card visuals without intercepting button clicks
        ConfettiOverlay(
          modifier = Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(28.dp))
        )
      }
    } else {
      // Smooth Card Advance Animation + 3D Card Flip + Swipe Right to Undo Gesture
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

        // Interactive Centered Flashcard as a Tactile Frosted Glass Slab with 3D Rotation + Swipe-Right Undo
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
            .pointerInput(studyHistory.size) {
              var accumulatedHorizontalDrag = 0f
              detectHorizontalDragGestures(
                onDragStart = { accumulatedHorizontalDrag = 0f },
                onHorizontalDrag = { change, dragAmount ->
                  accumulatedHorizontalDrag += dragAmount
                  if (accumulatedHorizontalDrag > 85f && studyHistory.isNotEmpty()) {
                    change.consume()
                    accumulatedHorizontalDrag = 0f
                    performUndoPreviousCard()
                  }
                }
              )
            }
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
              text = if (!showingBack) {
                if (studyHistory.isNotEmpty()) "(Tap card to 3D flip • Swipe right to undo)" else "(Tap card to 3D flip)"
              } else {
                "(Tap card to view question)"
              },
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
                  recordCardHistory(activeCard, false)
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
                  recordCardHistory(activeCard, false)
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
                SoundEffectsManager.playGoodButtonBeep()
                if (studyQueue.isNotEmpty()) {
                  val activeCard = studyQueue.removeAt(0)
                  recordCardHistory(activeCard, true)
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
  isComingSoon: Boolean = false,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val palette = currentGlassPalette()
  if (isComingSoon) {
    Surface(
      onClick = onClick,
      shape = RoundedCornerShape(24.dp),
      color = palette.nestedCardFill,
      border = palette.nestedBorder,
      modifier = modifier
        .height(metrics.scaledDp(156))
        .testTag("extra_tool_card_${tool.id}")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(metrics.scaledDp(16)),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(palette.trackColor)
              .border(palette.nestedBorder, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = tool.icon,
              contentDescription = tool.title,
              tint = palette.textSecondary,
              modifier = Modifier.size(22.dp)
            )
          }

          Surface(
            shape = CircleShape,
            color = Color(0xFF6366F1).copy(alpha = 0.15f),
            border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.35f))
          ) {
            Text(
              text = "Coming Soon",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF818CF8),
              maxLines = 1,
              softWrap = false,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Text(
            text = tool.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = metrics.scaledSp(15),
              fontWeight = FontWeight.Bold
            ),
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (tool.subtitle.isNotBlank()) {
            Text(
              text = tool.subtitle,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = metrics.scaledSp(12),
                fontWeight = FontWeight.Medium
              ),
              color = palette.textSecondary.copy(alpha = 0.8f),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  } else {
    LiquidGlassCard(
      onClick = onClick,
      shape = RoundedCornerShape(26.dp),
      elevation = 8.dp,
      modifier = modifier.testTag("extra_tool_card_${tool.id}")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(metrics.scaledDp(18)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
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
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = tool.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontSize = metrics.scaledSp(16),
              fontWeight = FontWeight.Bold
            ),
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (tool.subtitle.isNotBlank()) {
            Text(
              text = tool.subtitle,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = metrics.scaledSp(13),
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textSecondary,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Icon(
          imageVector = Icons.Filled.ArrowForward,
          contentDescription = null,
          tint = palette.accentIndigo,
          modifier = Modifier.size(20.dp)
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

          if (viewModel.isSyncingSchedule) {
            CircularProgressIndicator(
              modifier = Modifier
                .size(18.dp)
                .testTag("schedule_firestore_sync_indicator"),
              strokeWidth = 2.dp,
              color = palette.accentIndigo
            )
          }

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
 * Compact, high-performance Liquid Glass Profile & Settings Dialog (`ProfileAndSettingsDialog`):
 * - Uses a single consolidated Liquid Glass container (`18.dp` corner radius, slim specular border)
 *   and lightweight solid tints (`palette.cardSurface.copy(alpha = 0.85f)`) instead of heavy
 *   multi-pass blur shaders.
 * - Hoists and caches `SharedPreferences` and version reads inside `remember { ... }` blocks to
 *   prevent disk reads during recomposition and eliminate swipe/tab lag.
 * - Relies on a single lightweight `Modifier.verticalScroll(rememberScrollState())` without
 *   redundant nested scroll states.
 * - Includes:
 *   1. Compact Profile Header (`36.dp` avatar icon, Student Name + Handle in a single row, trailing
 *      "Edit Profile" icon button).
 *   2. Compact Settings Row Items (`8.dp`–`10.dp` vertical spacing):
 *      - "Dark Mode" (`Icons.Default.DarkMode`, title, compact `Switch`)
 *      - "Auto Backup & Sync" (`Icons.Default.CloudSync`, title, `"Enabled (Drive)"` status chip +
 *        1-tap clipboard Backup & Restore actions)
 *      - "About Sibil" (installed version `Version 1.0.x (Official Build)`, license status, and
 *        manual `"Check for Updates"` button with inline spinning progress and status toast/chip)
 *      - "Account Actions" (minimalist text button `"Log Out / Switch Account"` with neutral red tint)
 */
@Composable
private fun ProfileAndSettingsDialog(
  viewModel: TaskViewModel,
  onDismiss: () -> Unit,
  onSwitchAccount: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val metrics = rememberResponsiveMetrics()
  val palette = currentGlassPalette()
  val isGuest = viewModel.isGuestMode

  // Hoist and cache SharedPreferences & PackageInfo reads inside remember blocks
  val cachedPrefs = remember(context) {
    context.applicationContext.getSharedPreferences(
      "sibil_app_offline_storage",
      Context.MODE_PRIVATE
    )
  }
  val localVersion = remember(context) {
    getInstalledVersionName(context).removePrefix("v").removePrefix("V")
  }
  val autoBackupStatusLabel = remember(cachedPrefs) {
    cachedPrefs.getString("auto_backup_status_label", "Enabled (Drive)") ?: "Enabled (Drive)"
  }

  // Lightweight solid tints with low alpha instead of heavy multi-pass blur shaders
  val modalBackgroundColor = if (palette.isDark) Color(0xFF0B1120) else Color(0xFFF4F7FB)
  val consolidatedContainerColor = palette.cardSurface.copy(alpha = 0.85f)
  val rowTileSurfaceColor = palette.cardSurface.copy(alpha = 0.85f)
  val slimSpecularBorder = remember(palette.isDark) {
    BorderStroke(
      width = 1.dp,
      brush = Brush.linearGradient(
        colors = if (palette.isDark) {
          listOf(
            Color.White.copy(alpha = 0.32f),
            Color.White.copy(alpha = 0.08f)
          )
        } else {
          listOf(
            Color.White.copy(alpha = 0.85f),
            Color(0xFFCBD5E1).copy(alpha = 0.55f)
          )
        }
      )
    )
  }

  // Inline Edit Profile state
  var isEditingProfile by rememberSaveable { mutableStateOf(false) }
  var editDisplayNameInput by rememberSaveable(viewModel.displayUsername) {
    mutableStateOf(if (isGuest) "" else viewModel.displayUsername)
  }
  var editHandleInput by rememberSaveable(viewModel.username) {
    mutableStateOf(if (isGuest) "" else viewModel.username)
  }

  // Data Backup & Restore dialog state
  var isRestoreDialogOpen by rememberSaveable { mutableStateOf(false) }
  var restoreJsonInput by rememberSaveable { mutableStateOf("") }
  var restoreErrorText by rememberSaveable { mutableStateOf<String?>(null) }

  // Manual "Check for Updates" state
  var isCheckingUpdates by remember { mutableStateOf(false) }
  var updateStatusMessage by remember { mutableStateOf<String?>(null) }
  var isUpdateStatusError by remember { mutableStateOf(false) }
  var manualUpdateRelease by remember { mutableStateOf<GitHubReleaseUpdateInfo?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = 680.dp)
        .heightIn(max = 680.dp)
        .testTag("profile_settings_dialog"),
      shape = RoundedCornerShape(22.dp),
      color = modalBackgroundColor,
      border = slimSpecularBorder,
      tonalElevation = 0.dp,
      shadowElevation = 12.dp
    ) {
      // Single lightweight verticalScroll without nested conflicting scroll states
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Compact Dialog Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Profile & Settings",
            style = MaterialTheme.typography.titleLarge.copy(
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            ),
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f)
              .testTag("profile_settings_dialog_title")
          )

          if (viewModel.isSyncingSettings || viewModel.isFirestoreSyncing) {
            CircularProgressIndicator(
              modifier = Modifier
                .padding(end = 8.dp)
                .size(16.dp)
                .testTag("settings_firestore_sync_indicator"),
              strokeWidth = 2.dp,
              color = palette.accentIndigo
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(36.dp)
              .testTag("close_profile_settings_dialog_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close Settings",
              tint = palette.textSecondary,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Single Consolidated Liquid Glass Container (18.dp corner radius, slim specular border)
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .align(Alignment.CenterHorizontally)
            .testTag("extras_account_card"),
          shape = RoundedCornerShape(18.dp),
          color = consolidatedContainerColor,
          border = slimSpecularBorder,
          tonalElevation = 0.dp,
          shadowElevation = 0.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
          ) {
            // 1. COMPACT PROFILE HEADER (36.dp avatar + Student Name & Handle in a single row + Edit Profile icon)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                color = palette.nestedCardFill.copy(alpha = 0.85f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Account Profile Icon",
                    tint = palette.accentIndigo,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              if (isGuest) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "Guest User",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("account_guest_text")
                  )
                  Text(
                    text = "• @guest",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium
                    ),
                    color = palette.textSecondary,
                    maxLines = 1,
                    softWrap = false
                  )
                }
              } else {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = viewModel.displayUsername,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                      .weight(1f, fill = false)
                      .testTag("account_user_name_text")
                  )
                  Text(
                    text = if (viewModel.username.contains("@")) {
                      "• ${viewModel.username}"
                    } else {
                      "@${viewModel.username}"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium
                    ),
                    color = palette.textSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("account_username_handle_text")
                  )
                }
              }

              IconButton(
                onClick = { isEditingProfile = !isEditingProfile },
                modifier = Modifier
                  .size(32.dp)
                  .testTag("edit_profile_icon_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit Profile",
                  tint = palette.accentIndigo,
                  modifier = Modifier.size(17.dp)
                )
              }
            }

            // Optional Compact Inline Profile Editor when "Edit Profile" is tapped
            if (isEditingProfile) {
              Surface(
                color = palette.nestedCardFill.copy(alpha = 0.70f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, palette.dividerColor)
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  OutlinedTextField(
                    value = editDisplayNameInput,
                    onValueChange = { editDisplayNameInput = it },
                    label = { Text("Full Name / Nickname", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = sibilOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                  )
                  OutlinedTextField(
                    value = editHandleInput,
                    onValueChange = { editHandleInput = it },
                    label = { Text("Email Address", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = sibilOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                  )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    TextButton(onClick = { isEditingProfile = false }) {
                      Text("Cancel", fontSize = 12.sp, color = palette.textSecondary)
                    }
                    Button(
                      onClick = {
                        val cleanHandle = editHandleInput.trim().removePrefix("@").ifBlank {
                          viewModel.username
                        }
                        val cleanName = editDisplayNameInput.trim().ifBlank { cleanHandle }
                        try {
                          val profileUpdate = UserProfileChangeRequest.Builder()
                            .setDisplayName(cleanName)
                            .build()
                          FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdate)
                        } catch (_: Throwable) {
                        }
                        viewModel.loginWithUsername(cleanHandle, cleanName)
                        isEditingProfile = false
                      },
                      shape = RoundedCornerShape(10.dp),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                      Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }

            HorizontalDivider(color = palette.dividerColor.copy(alpha = 0.65f))

            // 2. "DARK MODE" COMPACT LIST TILE
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(rowTileSurfaceColor)
                .clickable { viewModel.updateDarkMode(!viewModel.isDarkMode) }
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("extras_display_card"),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Surface(
                  color = palette.nestedCardFill.copy(alpha = 0.85f),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.size(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.DarkMode,
                      contentDescription = null,
                      tint = palette.accentIndigo,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }

                Text(
                  text = stringResource(R.string.dark_mode_label),
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = palette.textPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
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
                modifier = Modifier
                  .scale(0.86f)
                  .testTag("dark_mode_switch")
              )
            }

            HorizontalDivider(color = palette.dividerColor.copy(alpha = 0.65f))

            // 3. "AUTO BACKUP & SYNC" COMPACT LIST TILE + COMPACT ACTIONS
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(rowTileSurfaceColor)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .testTag("extras_data_management_card"),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Surface(
                    color = palette.nestedCardFill.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(32.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = palette.accentIndigo,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }

                  Text(
                    text = "Auto Backup & Sync",
                    style = MaterialTheme.typography.bodyLarge.copy(
                      fontSize = 14.sp,
                      fontWeight = FontWeight.SemiBold
                    ),
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                Surface(
                  color = Color(0xFF10B981).copy(alpha = 0.14f),
                  shape = CircleShape,
                  border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.38f))
                ) {
                  Text(
                    text = autoBackupStatusLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (palette.isDark) Color(0xFF34D399) else Color(0xFF047857),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                      .padding(horizontal = 8.dp, vertical = 4.dp)
                      .testTag("auto_backup_status_chip")
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    val backupJson = viewModel.createBackupJsonString(context)
                    val clipboard =
                      context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val clip = ClipData.newPlainText("Sibil Data Backup", backupJson)
                    clipboard?.setPrimaryClip(clip)
                    Toast.makeText(
                      context,
                      "Backup copied to clipboard!",
                      Toast.LENGTH_SHORT
                    ).show()
                  },
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, palette.accentIndigo.copy(alpha = 0.45f)),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.nestedCardFill.copy(alpha = 0.65f),
                    contentColor = palette.accentIndigo
                  ),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .testTag("backup_data_button")
                ) {
                  Text(
                    text = "Copy Backup",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                  )
                }

                OutlinedButton(
                  onClick = {
                    restoreErrorText = null
                    isRestoreDialogOpen = true
                  },
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, palette.accentIndigo.copy(alpha = 0.45f)),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.nestedCardFill.copy(alpha = 0.65f),
                    contentColor = palette.accentIndigo
                  ),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .testTag("restore_data_button")
                ) {
                  Text(
                    text = "Restore Backup",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                  )
                }
              }
            }

            HorizontalDivider(color = palette.dividerColor.copy(alpha = 0.65f))

            // 4. "ABOUT SIBIL" COMPACT TILE WITH MANUAL "CHECK FOR UPDATES"
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(rowTileSurfaceColor)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .testTag("extras_about_card"),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Surface(
                    color = palette.nestedCardFill.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(32.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = palette.accentIndigo,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }

                  Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                      text = "About Sibil",
                      style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                      ),
                      color = palette.textPrimary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.testTag("about_app_name_text")
                    )
                    Text(
                      text = "Version $localVersion (Official Build)",
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                      ),
                      color = palette.textSecondary,
                      maxLines = 1,
                      softWrap = false,
                      modifier = Modifier.testTag("about_app_version_text")
                    )
                  }
                }

                // Compact, sleek frosted action button: "Check for Updates"
                OutlinedButton(
                  onClick = {
                    if (!isCheckingUpdates) {
                      isCheckingUpdates = true
                      updateStatusMessage = null
                      isUpdateStatusError = false
                      coroutineScope.launch {
                        when (val checkResult = queryGitHubLatestRelease(context)) {
                          is GitHubUpdateCheckResult.UpdateAvailable -> {
                            isCheckingUpdates = false
                            cachedSessionUpdateRelease = checkResult.releaseInfo
                            manualUpdateRelease = checkResult.releaseInfo
                          }
                          is GitHubUpdateCheckResult.UpToDate -> {
                            isCheckingUpdates = false
                            isUpdateStatusError = false
                            val msg = "You're on the latest version (v${checkResult.localVersion}) ✓"
                            updateStatusMessage = msg
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                          }
                          GitHubUpdateCheckResult.NetworkError -> {
                            isCheckingUpdates = false
                            isUpdateStatusError = true
                            val errMsg = "Unable to check updates. Please check your connection."
                            updateStatusMessage = errMsg
                            Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                          }
                        }
                      }
                    }
                  },
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, palette.accentIndigo.copy(alpha = 0.55f)),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = palette.nestedCardFill.copy(alpha = 0.80f),
                    contentColor = palette.accentIndigo
                  ),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier
                    .wrapContentWidth()
                    .height(34.dp)
                    .testTag("check_for_updates_button")
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    if (isCheckingUpdates) {
                      CircularProgressIndicator(
                        modifier = Modifier.size(13.dp),
                        strokeWidth = 2.dp,
                        color = palette.accentIndigo
                      )
                    } else {
                      Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = palette.accentIndigo,
                        modifier = Modifier.size(14.dp)
                      )
                    }
                    Text(
                      text = if (isCheckingUpdates) "Checking..." else "Check for Updates",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      softWrap = false
                    )
                  }
                }
              }

              // Inline Status Toast / Chip for Manual Update Check Result
              val currentStatusMsg = updateStatusMessage
              if (currentStatusMsg != null) {
                Surface(
                  color = if (isUpdateStatusError) {
                    palette.accentRed.copy(alpha = 0.14f)
                  } else {
                    Color(0xFF10B981).copy(alpha = 0.14f)
                  },
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(
                    1.dp,
                    if (isUpdateStatusError) {
                      palette.accentRed.copy(alpha = 0.35f)
                    } else {
                      Color(0xFF10B981).copy(alpha = 0.35f)
                    }
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("update_check_status_chip")
                ) {
                  Text(
                    text = currentStatusMsg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUpdateStatusError) {
                      palette.accentRed
                    } else if (palette.isDark) {
                      Color(0xFF34D399)
                    } else {
                      Color(0xFF047857)
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = stringResource(R.string.developed_by_text),
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  color = palette.textSecondary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("about_developed_by_text")
                )

                Text(
                  text = "Academic License • Verified",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = palette.accentIndigo,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }

            HorizontalDivider(color = palette.dividerColor.copy(alpha = 0.65f))

            // 5. "ACCOUNT ACTIONS": Minimalist Text Button for "Log Out" with neutral red tint
            TextButton(
              onClick = {
                try {
                  FirebaseAuth.getInstance().signOut()
                } catch (_: Throwable) {
                }
                onSwitchAccount()
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.textButtonColors(
                contentColor = palette.accentRed
              ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("switch_account_button")
            ) {
              Text(
                text = "Log Out / Switch Account",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = palette.accentRed,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }
      }
    }
  }

  // Restore Backup Modal Dialog
  if (isRestoreDialogOpen) {
    AlertDialog(
      onDismissRequest = { isRestoreDialogOpen = false },
      shape = RoundedCornerShape(22.dp),
      containerColor = modalBackgroundColor,
      title = {
        Text(
          text = "Restore Sibil Data",
          style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Paste your previously copied Sibil backup JSON string below to restore all Schedules, Tasks, Alarms, Flashcards, and Profile data.",
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 12.sp
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
            minLines = 4,
            maxLines = 8,
            shape = RoundedCornerShape(12.dp),
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

  // Trigger UpdateAvailableDialog immediately if manual "Check for Updates" detects a newer release
  val activeManualRelease = manualUpdateRelease
  if (activeManualRelease != null) {
    UpdateAvailableDialog(
      releaseInfo = activeManualRelease,
      metrics = metrics,
      onUpdateNow = {
        manualUpdateRelease = null
      },
      onRemindLater = {
        cachedPrefs.edit().putString("dismissed_update_tag", activeManualRelease.tagName).apply()
        dismissedSessionUpdateTag = activeManualRelease.tagName
        hasDismissedUpdateForSession = true
        manualUpdateRelease = null
      }
    )
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

        if (viewModel.isSyncingTasks) {
          CircularProgressIndicator(
            modifier = Modifier
              .size(18.dp)
              .testTag("tasks_firestore_sync_indicator"),
            strokeWidth = 2.dp,
            color = palette.accentIndigo
          )
        }

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
const val SIBIL_TIMER_CHANNEL_ID = "sibil_timer_channel"
const val SIBIL_TIMER_CHANNEL_NAME = "Sibil Countdown Timers"
const val TIMER_NOTIFICATION_CHANNEL_ID = SIBIL_TIMER_CHANNEL_ID
const val TIMER_NOTIFICATION_CHANNEL_NAME = SIBIL_TIMER_CHANNEL_NAME
const val ACTION_STOP_TIMER_ALARM = "com.example.ACTION_STOP_TIMER_ALARM"
private const val TIMER_NOTIFICATION_ID = 4001
private const val TIMER_ALARM_REQUEST_CODE = 4002
private const val TIMER_STOP_ACTION_REQUEST_CODE = 4003

@Volatile
private var lastTimerAlarmFiredRealtimeMs: Long = 0L

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
 *   continues ringing until the user explicitly taps "Stop / Dismiss" or "Snooze (5m)".
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
 * Creates the high-importance Notification Channels `"sibil_timer_channel"` and `"sibil_alarm_channel"`.
 */
fun createClockNotificationChannel(context: Context) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    val timerChannel = NotificationChannel(
      SIBIL_TIMER_CHANNEL_ID,
      SIBIL_TIMER_CHANNEL_NAME,
      NotificationManager.IMPORTANCE_HIGH
    ).apply {
      description = "High-priority heads-up alerts for Sibil Countdown Timers"
      enableVibration(true)
    }
    val alarmChannel = NotificationChannel(
      SIBIL_ALARM_CHANNEL_ID,
      SIBIL_ALARM_CHANNEL_NAME,
      NotificationManager.IMPORTANCE_HIGH
    ).apply {
      description = "High-priority alerts for Sibil Alarms and Countdown Timers"
      enableVibration(true)
    }
    notificationManager?.createNotificationChannel(timerChannel)
    notificationManager?.createNotificationChannel(alarmChannel)
  }
}

fun createTimerNotificationChannel(context: Context) {
  createClockNotificationChannel(context)
}

/**
 * System Notification Helper:
 * Dispatches a high-priority heads-up `NotificationCompat.Builder` to the phone's status bar
 * with channel ID `sibil_timer_channel` and an interactive `Stop / Dismiss` action button
 * that stops the ringing sound and clears the notification.
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

  val stopIntent = Intent(context, TimerReceiver::class.java).apply {
    action = ACTION_STOP_TIMER_ALARM
  }
  val stopPendingIntent = PendingIntent.getBroadcast(
    context,
    TIMER_STOP_ACTION_REQUEST_CODE,
    stopIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
  )

  val notification = NotificationCompat.Builder(context, SIBIL_TIMER_CHANNEL_ID)
    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
    .setContentTitle(title)
    .setContentText(message)
    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setCategory(NotificationCompat.CATEGORY_ALARM)
    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    .setAutoCancel(true)
    .setContentIntent(contentPendingIntent)
    .setDeleteIntent(stopPendingIntent)
    .addAction(
      android.R.drawable.ic_media_pause,
      "Stop / Dismiss",
      stopPendingIntent
    )
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
 * Fires the timer finished alarm sound and high-priority `sibil_timer_channel` status bar
 * notification ONCE (guarded by `lastTimerAlarmFiredRealtimeMs` to prevent double-firing
 * between the foreground coroutine and `TimerReceiver`), without showing an in-app banner.
 */
@Synchronized
fun triggerSingleTimerExpirationAlert(context: Context, title: String, message: String) {
  val now = SystemClock.elapsedRealtime()
  if (now - lastTimerAlarmFiredRealtimeMs < 2_500L) {
    return
  }
  lastTimerAlarmFiredRealtimeMs = now
  AlarmSoundController.startRinging(context)
  showClockNotification(context = context, title = title, message = message)
}

/**
 * Triggers continuous looping alarm audio for scheduled Alarms, sets `ringingAlertTitle`
 * so `RingingAlertDialog` pops up for scheduled Alarms, and posts a system notification.
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

  fun triggerAlarmRingtone(title: String, message: String) {
    triggerClockAlert(context = context, title = title, message = message)
  }

  fun stopActiveRingtone() {
    AlarmSoundController.stopRinging()
    try {
      NotificationManagerCompat.from(context).cancel(TIMER_NOTIFICATION_ID)
    } catch (_: Exception) {
    }
  }

  // ==========================================================================
  // ALARM STATE (SHARED PREFERENCES BACKED, STARTS COMPLETELY EMPTY)
  // ==========================================================================
  val fallbackAlarms = remember(viewModel == null) {
    if (viewModel == null) {
      val saved = localStorageManager.loadAlarms(LocalStorageManager.GUEST_ACCOUNT_KEY) ?: emptyList()
      mutableStateListOf<AlarmItem>().apply { addAll(saved) }
    } else {
      mutableStateListOf()
    }
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
  var hasFiredAlarm by remember { mutableStateOf(false) }

  LaunchedEffect(isTimerRunning) {
    while (isTimerRunning && timerRemainingSeconds > 0L) {
      delay(1000L)
      if (isTimerRunning && timerRemainingSeconds > 0L) {
        timerRemainingSeconds -= 1L
        if (timerRemainingSeconds <= 0L) {
          timerRemainingSeconds = 0L
          isTimerRunning = false
          if (!hasFiredAlarm) {
            hasFiredAlarm = true
            cancelTimerExactAlarm(context)
            val labelSuffix = activePresetLabel?.let { " ($it)" } ?: ""
            triggerSingleTimerExpirationAlert(
              context = context,
              title = "Timer Reached 00:00:00!$labelSuffix",
              message = "Countdown timer finished (00:00:00)$labelSuffix. Tap Stop / Dismiss to silence."
            )
          }
          break
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
    // Minimalist Floating Liquid Glass Header ("Clock")
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
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

      Text(
        text = "Clock",
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        color = palette.textPrimary,
        maxLines = 1
      )
    }

    val isStopwatchTabletLandscape =
      selectedSubTab == ClockSubTab.STOPWATCH && metrics.isTabletLandscape

    @Composable
    fun renderClockModeSwitcher(modifier: Modifier = Modifier) {
      val trackShape = RoundedCornerShape(26.dp)
      Box(
        modifier = modifier
          .fillMaxWidth()
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
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
              ) {
                Icon(
                  imageVector = tab.icon,
                  contentDescription = tab.title,
                  modifier = Modifier.size(14.dp),
                  tint = if (selected) Color.White else palette.textSecondary
                )
                Text(
                  text = tab.title,
                  color = if (selected) Color.White else palette.textSecondary,
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 13.sp,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
        }
      }
    }

    // 3D Segmented Liquid Glass Pill Switcher (Alarm, Timer, Stopwatch)
    if (!isStopwatchTabletLandscape) {
      renderClockModeSwitcher(
        modifier = Modifier
          .widthIn(max = 680.dp)
          .align(Alignment.CenterHorizontally)
          .padding(horizontal = 20.dp, vertical = 6.dp)
      )
    }

    // Dismissible POST_NOTIFICATIONS / SCHEDULE_EXACT_ALARM Permission Rationale Card (only on Alarm tab)
    if (selectedSubTab == ClockSubTab.ALARM && (!hasNotificationPermission || !canScheduleExactAlarms) && !isPermissionRationaleDismissed) {
      LiquidGlassCard(
        shape = RoundedCornerShape(18.dp),
        elevation = if (palette.isDark) 6.dp else 4.dp,
        borderStroke = BorderStroke(1.2.dp, palette.accentIndigo.copy(alpha = 0.45f)),
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 680.dp)
          .align(Alignment.CenterHorizontally)
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
            hasFiredAlarm = false
            stopActiveRingtone()
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
              hasFiredAlarm = false
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
            hasFiredAlarm = false
            stopActiveRingtone()
            timerRemainingSeconds = 0L
            timerTotalDurationSeconds = 0L
            activePresetLabel = null
            cancelTimerExactAlarm(context)
          },
          onSelectPreset = { preset ->
            stopActiveRingtone()
            hasFiredAlarm = false
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
          modeSwitcherContent = if (isStopwatchTabletLandscape) {
            { renderClockModeSwitcher(modifier = Modifier.fillMaxWidth()) }
          } else {
            null
          },
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
          val dialDiameter = if (metrics.screenWidthDp >= 600) 280.dp else 220.dp
          val timeFontSize = if (metrics.screenWidthDp >= 600) 36.sp else 30.sp

          Box(
            modifier = Modifier
              .size(dialDiameter)
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
              maxLines = 1,
              softWrap = false,
              style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = timeFontSize,
                lineHeight = if (metrics.screenWidthDp >= 600) 40.sp else 34.sp,
                letterSpacing = (-0.5).sp
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
                    maxLines = 1,
                    softWrap = false,
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
      maxLines = 1,
      softWrap = false,
      style = MaterialTheme.typography.displayMedium.copy(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = if (metrics.screenWidthDp >= 600) 36.sp else 32.sp,
        letterSpacing = (-0.5).sp
      ),
      color = if (isHighlighted) {
        palette.accentIndigo
      } else {
        palette.textMuted
      }
    )
    Text(
      text = unit,
      maxLines = 1,
      softWrap = false,
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
  modeSwitcherContent: (@Composable () -> Unit)? = null,
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
  val dialDiameter = if (metrics.screenWidthDp >= 600) 280.dp else 220.dp
  val timeFontSize = if (metrics.screenWidthDp >= 600) 36.sp else 30.sp

  @Composable
  fun renderStopwatchDialCard(modifier: Modifier = Modifier) {
    LiquidGlassCard(
      shape = RoundedCornerShape(30.dp),
      elevation = if (palette.isDark) 8.dp else 6.dp,
      modifier = modifier.fillMaxWidth()
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
            .size(dialDiameter)
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
              maxLines = 1,
              softWrap = false,
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
              maxLines = 1,
              softWrap = false,
              style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = timeFontSize,
                lineHeight = if (metrics.screenWidthDp >= 600) 40.sp else 34.sp,
                letterSpacing = (-0.5).sp
              ),
              color = palette.textPrimary,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("stopwatch_digital_counter")
            )
          }
        }
      }
    }
  }

  @Composable
  fun renderStopwatchControlsRow() {
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
            maxLines = 1,
            softWrap = false,
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
  }

  @Composable
  fun renderLapSplitsCard(
    dedicatedScroll: Boolean,
    modifier: Modifier = Modifier
  ) {
    LiquidGlassCard(
      shape = RoundedCornerShape(24.dp),
      elevation = if (palette.isDark) 8.dp else 6.dp,
      modifier = modifier
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
          val listModifier = if (dedicatedScroll) {
            Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
              .padding(top = 4.dp)
              .testTag("stopwatch_laps_list")
          } else {
            Modifier
              .fillMaxWidth()
              .padding(top = 4.dp)
              .testTag("stopwatch_laps_list")
          }

          Column(modifier = listModifier) {
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
                      maxLines = 1,
                      softWrap = false,
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
                      maxLines = 1,
                      softWrap = false
                    )
                  }
                }

                // Center column: SPLIT
                Text(
                  text = "+${formatStopwatchMillis(lap.lapSplitMillis)}",
                  maxLines = 1,
                  softWrap = false,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
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
                  maxLines = 1,
                  softWrap = false,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
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

  if (metrics.isTabletLandscape) {
    // Tablet Landscape Layout (screenWidthDp >= 720.dp in landscape):
    // Balanced two-column layout:
    // Left column: Circular Dial, Mode Switcher, and Primary Control Buttons.
    // Right column: Lap Splits list with its own dedicated scroll state.
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          start = metrics.scaledDp(16),
          end = metrics.scaledDp(16),
          top = metrics.scaledDp(8),
          bottom = 96.dp
        )
        .testTag("stopwatch_sub_screen"),
      horizontalArrangement = Arrangement.spacedBy(20.dp),
      verticalAlignment = Alignment.Top
    ) {
      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        renderStopwatchDialCard()
        modeSwitcherContent?.invoke()
        renderStopwatchControlsRow()
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
      ) {
        renderLapSplitsCard(
          dedicatedScroll = true,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  } else {
    // Standard Portrait / Phone Layout: Clean vertical stack
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
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 680.dp)
          .align(Alignment.CenterHorizontally),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        renderStopwatchDialCard()
        Spacer(modifier = Modifier.height(12.dp))
        renderStopwatchControlsRow()
        Spacer(modifier = Modifier.height(12.dp))
        renderLapSplitsCard(dedicatedScroll = false)
      }
    }
  }
}

// Registered in AndroidManifest.xml: <receiver android:name=".TimerReceiver" />
class TimerReceiver : BroadcastReceiver() {
  @SuppressLint("MissingPermission")
  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action == ACTION_STOP_TIMER_ALARM) {
      AlarmSoundController.stopRinging()
      try {
        NotificationManagerCompat.from(context).cancel(TIMER_NOTIFICATION_ID)
      } catch (_: Exception) {
      }
      return
    }

    val label = intent?.getStringExtra("timer_label")
    val alertTitle = if (!label.isNullOrBlank()) {
      "Timer Reached 00:00:00! ($label)"
    } else {
      "Timer Reached 00:00:00!"
    }
    val alertMessage = if (!label.isNullOrBlank()) {
      "Your timer ($label) has reached 00:00:00. Tap Stop / Dismiss to silence."
    } else {
      "Your countdown timer has reached 00:00:00. Tap Stop / Dismiss to silence."
    }
    triggerSingleTimerExpirationAlert(
      context = context,
      title = alertTitle,
      message = alertMessage
    )
  }
}


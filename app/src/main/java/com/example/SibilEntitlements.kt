package com.example

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * 3-Tier One-Time Payment Entitlement Model for Sibil CE.
 */
enum class SibilTier(
  val level: Int,
  val badge: String,
  val title: String,
  val price: String,
  val maxFlashcardDecks: Int
) {
  FREE(
    level = 0,
    badge = "FREE",
    title = "Free Version",
    price = "₱0",
    maxFlashcardDecks = 10
  ),
  TIER_49(
    level = 1,
    badge = "₱49",
    title = "Plus Plan",
    price = "₱49 One-Time",
    maxFlashcardDecks = 25
  ),
  TIER_99(
    level = 2,
    badge = "₱99 PRO",
    title = "Pro Unlimited",
    price = "₱99 One-Time",
    maxFlashcardDecks = Int.MAX_VALUE
  );

  fun compactLockLabel(): String = when (this) {
    FREE -> "FREE"
    TIER_49 -> "₱49"
    TIER_99 -> "₱99"
  }

  fun currentPlanPillLabel(): String = when (this) {
    FREE -> "Current: Free Version"
    TIER_49 -> "Current: ₱49 Plus"
    TIER_99 -> "Current: ₱99 Pro"
  }

  companion object {
    fun fromNameOrNull(raw: String?): SibilTier? {
      val clean = raw?.trim()?.uppercase(Locale.US).orEmpty()
      return entries.firstOrNull { it.name == clean }
        ?: when (clean) {
          "0", "FREE_VERSION" -> FREE
          "49", "PLUS", "PLUS_PLAN" -> TIER_49
          "99", "PRO", "PRO_UNLIMITED" -> TIER_99
          else -> null
        }
    }

    fun fromLevel(level: Int): SibilTier = when {
      level >= TIER_99.level -> TIER_99
      level == TIER_49.level -> TIER_49
      else -> FREE
    }
  }
}

sealed class VoucherResult {
  data class Success(val tier: SibilTier, val message: String) : VoucherResult()
  data class Error(val message: String) : VoucherResult()
  data object RequiresSignIn : VoucherResult()
}

internal data class VoucherRecordSnapshot(
  val exists: Boolean,
  val tierRaw: String? = null,
  val used: Boolean = false,
  val redeemedByUid: String = ""
)

internal interface EntitlementRealtimeDbGateway {
  suspend fun fetchVoucher(code: String, idToken: String): VoucherRecordSnapshot
  suspend fun markVoucherRedeemed(
    code: String,
    uid: String,
    email: String,
    redeemedAtMillis: Long,
    idToken: String
  )
  suspend fun fetchUserTier(uid: String, idToken: String): SibilTier?
  suspend fun upsertUserTier(
    uid: String,
    tier: SibilTier,
    lastVoucher: String?,
    updatedAtMillis: Long,
    idToken: String
  )
}

internal data class AuthenticatedUserIdentity(
  val uid: String,
  val email: String,
  val isGuestOrAnonymous: Boolean,
  val tokenFetcher: suspend () -> String? = { null }
)

internal object FirebaseRealtimeDbEntitlementGateway : EntitlementRealtimeDbGateway {
  override suspend fun fetchVoucher(code: String, idToken: String): VoucherRecordSnapshot {
    val response = SibilRealtimeDbRest.requestJson(
      method = "GET",
      relativePath = "vouchers/$code",
      idToken = idToken
    )
    if (response.statusCode == 404 || response.body.trim() == "null" || response.body.isBlank()) {
      return VoucherRecordSnapshot(exists = false)
    }
    if (response.statusCode !in 200..299) {
      throw RealtimeDbHttpException(response.statusCode, response.body)
    }
    val obj = JSONObject(response.body)
    return VoucherRecordSnapshot(
      exists = true,
      tierRaw = obj.optString("tier", "").trim().ifEmpty { null },
      used = obj.optBoolean("used", false),
      redeemedByUid = obj.optString("redeemedByUid", "").trim()
    )
  }

  override suspend fun markVoucherRedeemed(
    code: String,
    uid: String,
    email: String,
    redeemedAtMillis: Long,
    idToken: String
  ) {
    val payload = JSONObject().apply {
      put("used", true)
      put("redeemedByUid", uid)
      put("redeemedByEmail", email)
      put("redeemedAt", redeemedAtMillis)
    }
    val response = SibilRealtimeDbRest.requestJson(
      method = "PATCH",
      relativePath = "vouchers/$code",
      idToken = idToken,
      jsonBody = payload.toString()
    )
    if (response.statusCode !in 200..299) {
      throw RealtimeDbHttpException(response.statusCode, response.body)
    }
  }

  override suspend fun fetchUserTier(uid: String, idToken: String): SibilTier? {
    val response = SibilRealtimeDbRest.requestJson(
      method = "GET",
      relativePath = "users/$uid/entitlement",
      idToken = idToken
    )
    if (response.statusCode == 404 || response.body.trim() == "null" || response.body.isBlank()) {
      return null
    }
    if (response.statusCode !in 200..299) {
      throw RealtimeDbHttpException(response.statusCode, response.body)
    }
    val trimmed = response.body.trim()
    return if (trimmed.startsWith("{")) {
      val obj = JSONObject(trimmed)
      SibilTier.fromNameOrNull(obj.optString("tier", ""))
    } else {
      val cleaned = trimmed.removeSurrounding("\"")
      SibilTier.fromNameOrNull(cleaned)
    }
  }

  override suspend fun upsertUserTier(
    uid: String,
    tier: SibilTier,
    lastVoucher: String?,
    updatedAtMillis: Long,
    idToken: String
  ) {
    val payload = JSONObject().apply {
      put("tier", tier.name)
      put("updatedAt", updatedAtMillis)
      if (!lastVoucher.isNullOrBlank()) {
        put("lastVoucher", lastVoucher)
      }
    }
    val response = SibilRealtimeDbRest.requestJson(
      method = "PUT",
      relativePath = "users/$uid/entitlement",
      idToken = idToken,
      jsonBody = payload.toString()
    )
    if (response.statusCode !in 200..299) {
      throw RealtimeDbHttpException(response.statusCode, response.body)
    }
  }
}

/**
 * Dual-Layer Offline + Cloud Entitlement & Voucher Manager (`SibilEntitlementManager`):
 * - Caches each user's unlocked `SibilTier` in `SharedPreferences("sibil_tier_prefs")` keyed by `"tier_${uid}"`
 *   (or `"tier_guest"` when signed out) for instant 100% offline access.
 * - Syncs with Firebase Realtime Database `users/{uid}/entitlement/tier` when signed in and takes
 *   `maxOf(localTier.level, remoteTier.level)`.
 * - Verifies & marks one-time payment vouchers in Firebase Realtime Database `vouchers/{code}.json`.
 */
object SibilEntitlementManager {
  const val TIER_PREFS_NAME = "sibil_tier_prefs"
  private const val KEY_TIER_PREFIX = "tier_"
  private const val KEY_DEMO_OVERRIDE_PREFIX = "demo_override_"
  const val GUEST_UID = "guest"

  internal var realtimeGateway: EntitlementRealtimeDbGateway = FirebaseRealtimeDbEntitlementGateway

  internal var currentUserProvider: () -> AuthenticatedUserIdentity? = {
    try {
      val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
      user?.let {
        AuthenticatedUserIdentity(
          uid = it.uid,
          email = it.email.orEmpty(),
          isGuestOrAnonymous = it.isAnonymous,
          tokenFetcher = { SibilRealtimeDbRest.fetchIdToken(it, forceRefresh = false) }
        )
      }
    } catch (_: Throwable) {
      null
    }
  }

  fun normalizeVoucherCode(rawCode: String): String =
    rawCode.trim().uppercase(Locale.US).replace(" ", "")

  fun resolveCacheUid(): String {
    val identity = currentUserProvider()
    return if (identity == null || identity.isGuestOrAnonymous || identity.uid.isBlank()) {
      GUEST_UID
    } else {
      identity.uid.trim()
    }
  }

  fun getCachedTier(context: Context, uid: String? = null): SibilTier {
    val appCtx = context.applicationContext ?: context
    val targetUid = uid?.trim()?.ifBlank { GUEST_UID } ?: resolveCacheUid()
    val prefs = appCtx.getSharedPreferences(TIER_PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString("${KEY_TIER_PREFIX}${targetUid}", null)
    return SibilTier.fromNameOrNull(raw) ?: SibilTier.FREE
  }

  fun saveCachedTier(
    context: Context,
    tier: SibilTier,
    uid: String? = null,
    isDemoOverride: Boolean = false
  ) {
    val appCtx = context.applicationContext ?: context
    val targetUid = uid?.trim()?.ifBlank { GUEST_UID } ?: resolveCacheUid()
    val prefs = appCtx.getSharedPreferences(TIER_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putString("${KEY_TIER_PREFIX}${targetUid}", tier.name)
      .putBoolean("${KEY_DEMO_OVERRIDE_PREFIX}${targetUid}", isDemoOverride)
      .commit()
  }

  fun isDemoOverrideActive(context: Context, uid: String? = null): Boolean {
    val appCtx = context.applicationContext ?: context
    val targetUid = uid?.trim()?.ifBlank { GUEST_UID } ?: resolveCacheUid()
    val prefs = appCtx.getSharedPreferences(TIER_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean("${KEY_DEMO_OVERRIDE_PREFIX}${targetUid}", false)
  }

  fun setDemoTierOverride(context: Context, tier: SibilTier): SibilTier {
    val targetUid = resolveCacheUid()
    saveCachedTier(context = context, tier = tier, uid = targetUid, isDemoOverride = true)
    return tier
  }

  /**
   * Syncs the signed-in Firebase Auth user's `SibilTier` from Firebase Realtime Database
   * (`users/{uid}/entitlement`), always preserving `maxOf(localTier.level, remoteTier.level)`
   * unless an offline Panel Defense Demo Switcher override is currently active.
   */
  suspend fun syncTierFromCloud(context: Context): SibilTier = withContext(Dispatchers.IO) {
    val identity = currentUserProvider()
    if (identity == null || identity.isGuestOrAnonymous || identity.uid.isBlank()) {
      return@withContext getCachedTier(context, GUEST_UID)
    }

    val uid = identity.uid.trim()
    val localTier = getCachedTier(context, uid)
    if (isDemoOverrideActive(context, uid)) {
      return@withContext localTier
    }

    return@withContext try {
      val idToken = identity.tokenFetcher().orEmpty()
      if (idToken.isBlank()) return@withContext localTier
      val remoteTier = realtimeGateway.fetchUserTier(uid, idToken)
      val mergedTier = if (remoteTier != null) {
        SibilTier.fromLevel(maxOf(localTier.level, remoteTier.level))
      } else {
        localTier
      }
      if (mergedTier != localTier || remoteTier != null) {
        saveCachedTier(context = context, tier = mergedTier, uid = uid, isDemoOverride = false)
      }
      mergedTier
    } catch (e: Throwable) {
      Log.w("SibilEntitlements", "Cloud tier sync skipped/offline: ${e.message}")
      localTier
    }
  }

  suspend fun redeemVoucherCode(
    context: Context,
    rawCode: String
  ): VoucherResult = withContext(Dispatchers.IO) {
    val code = normalizeVoucherCode(rawCode)
    if (code.isBlank()) {
      return@withContext VoucherResult.Error("Please enter a valid voucher code.")
    }

    val identity = currentUserProvider()
    if (identity == null || identity.isGuestOrAnonymous || identity.uid.isBlank()) {
      return@withContext VoucherResult.RequiresSignIn
    }

    val uid = identity.uid.trim()

    return@withContext try {
      val idToken = identity.tokenFetcher().orEmpty()
      if (idToken.isBlank()) {
        return@withContext VoucherResult.Error(
          "Unable to authenticate session with Firebase. Please sign in again and try again."
        )
      }

      val voucher = realtimeGateway.fetchVoucher(code, idToken)
      if (!voucher.exists) {
        return@withContext VoucherResult.Error(
          "Invalid voucher code. Check your code and try again."
        )
      }

      val voucherTier = SibilTier.fromNameOrNull(voucher.tierRaw)
      if (voucherTier == null || voucherTier == SibilTier.FREE) {
        return@withContext VoucherResult.Error(
          "This voucher does not contain a valid paid plan tier."
        )
      }

      if (voucher.used) {
        if (voucher.redeemedByUid == uid) {
          val remoteTier = try {
            realtimeGateway.fetchUserTier(uid, idToken)
          } catch (_: Throwable) {
            null
          }
          val restoredTier = SibilTier.fromLevel(
            maxOf(voucherTier.level, remoteTier?.level ?: 0)
          )
          saveCachedTier(context = context, tier = restoredTier, uid = uid, isDemoOverride = false)
          try {
            realtimeGateway.upsertUserTier(
              uid = uid,
              tier = restoredTier,
              lastVoucher = code,
              updatedAtMillis = System.currentTimeMillis(),
              idToken = idToken
            )
          } catch (_: Throwable) {
          }
          return@withContext VoucherResult.Success(
            tier = restoredTier,
            message = "Your ${voucherTier.price} access has been restored!"
          )
        } else {
          return@withContext VoucherResult.Error(
            "This voucher code has already been redeemed by another account."
          )
        }
      }

      val now = System.currentTimeMillis()
      realtimeGateway.markVoucherRedeemed(
        code = code,
        uid = uid,
        email = identity.email,
        redeemedAtMillis = now,
        idToken = idToken
      )

      val existingRemoteTier = try {
        realtimeGateway.fetchUserTier(uid, idToken)
      } catch (_: Throwable) {
        null
      }
      val existingLocalTier = if (isDemoOverrideActive(context, uid)) {
        SibilTier.FREE
      } else {
        getCachedTier(context, uid)
      }
      val finalTier = SibilTier.fromLevel(
        maxOf(
          voucherTier.level,
          existingRemoteTier?.level ?: 0,
          existingLocalTier.level
        )
      )

      realtimeGateway.upsertUserTier(
        uid = uid,
        tier = finalTier,
        lastVoucher = code,
        updatedAtMillis = now,
        idToken = idToken
      )
      saveCachedTier(context = context, tier = finalTier, uid = uid, isDemoOverride = false)

      VoucherResult.Success(
        tier = finalTier,
        message = "${finalTier.title} (${finalTier.price}) unlocked forever!"
      )
    } catch (e: RealtimeDbHttpException) {
      Log.w("SibilEntitlements", "RTDB voucher error HTTP ${e.statusCode}: ${e.responseBody}", e)
      when (e.statusCode) {
        404 ->
          VoucherResult.Error("Invalid voucher code. Check your code and try again.")
        401, 403 ->
          VoucherResult.Error("Unable to verify voucher permissions. Please sign in again and retry.")
        else ->
          VoucherResult.Error("Unable to verify voucher right now (HTTP ${e.statusCode}). Please try again.")
      }
    } catch (e: Exception) {
      Log.w("SibilEntitlements", "Voucher verification failed: ${e.message}", e)
      VoucherResult.Error("Internet connection required to verify voucher code. Please try again online.")
    }
  }
}

private data class TierComparisonSpec(
  val tier: SibilTier,
  val features: List<String>,
  val accentColor: Color
)

private val SIBIL_TIER_SPECS = listOf(
  TierComparisonSpec(
    tier = SibilTier.FREE,
    features = listOf(
      "Clock (Pomodoro & PIN Lock), Tasks & Schedule",
      "Scientific Calculator (fx-570ES PLUS MathO)",
      "CE Formula Vault (MSTE, PSAD, HPGE)",
      "Up to 10 Smart Flashcard Decks"
    ),
    accentColor = Color(0xFF64748B)
  ),
  TierComparisonSpec(
    tier = SibilTier.TIER_49,
    features = listOf(
      "Everything in Free Version",
      "Up to 25 Smart Flashcard Decks",
      "Unit Converter (12 CE Categories)",
      "Surveying & Curve Solver"
    ),
    accentColor = Color(0xFF0284C7)
  ),
  TierComparisonSpec(
    tier = SibilTier.TIER_99,
    features = listOf(
      "Everything in ₱49 Plus Plan",
      "Unlimited Smart Flashcard Decks",
      "Estimate (Max Fajardo BOQ & Take-Off)",
      "Sibil AI (Gemini + Google Search Grounding)"
    ),
    accentColor = Color(0xFF4F46E5)
  )
)

/**
 * Frosted Lock Pill shown on gated tool cards (`₱49` or `₱99`) and Holographic Sibil AI button.
 */
@Composable
fun SibilTierLockPill(
  label: String,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier,
  compact: Boolean = false
) {
  val accent = if (label.contains("99")) {
    if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF4F46E5)
  } else {
    if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
  }
  val fill = if (isDarkMode) {
    accent.copy(alpha = 0.22f)
  } else {
    accent.copy(alpha = 0.13f)
  }
  Surface(
    color = fill,
    shape = CircleShape,
    border = BorderStroke(1.dp, accent.copy(alpha = 0.48f)),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(
        horizontal = if (compact) 7.dp else 9.dp,
        vertical = if (compact) 3.dp else 4.dp
      ),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Icon(
        imageVector = Icons.Rounded.Lock,
        contentDescription = "Locked behind $label",
        tint = accent,
        modifier = Modifier.size(if (compact) 10.dp else 12.dp)
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = JetBrainsMonoFamily,
          fontSize = if (compact) 10.sp else 11.sp,
          fontWeight = FontWeight.ExtraBold
        ),
        color = accent,
        maxLines = 1,
        softWrap = false
      )
    }
  }
}

/**
 * Upgrade & Voucher Redemption Modal (`SibilUpgradeSheet`):
 * - Header: "Unlock Sibil CE" + current plan pill.
 * - Voucher Redemption Box at top priority with GCash / Maya 3-step guide.
 * - 3 Tier Comparison Cards (`Free ₱0`, `₱49 One-Time`, `₱99 One-Time`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SibilUpgradeSheet(
  activeTier: SibilTier,
  highlightedTier: SibilTier,
  onDismiss: () -> Unit,
  onTierUnlocked: (SibilTier) -> Unit,
  onRequestSignIn: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val palette = currentGlassPalette()
  val coroutineScope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var voucherInput by rememberSaveable { mutableStateOf("") }
  var isRedeeming by remember { mutableStateOf(false) }
  var redemptionResult by remember { mutableStateOf<VoucherResult?>(null) }

  fun submitVoucherRedemption() {
    if (isRedeeming) return
    val normalized = SibilEntitlementManager.normalizeVoucherCode(voucherInput)
    if (normalized.isEmpty()) {
      redemptionResult = VoucherResult.Error("Please enter a voucher code.")
      return
    }
    voucherInput = normalized
    isRedeeming = true
    redemptionResult = null
    coroutineScope.launch {
      val result = SibilEntitlementManager.redeemVoucherCode(context, normalized)
      isRedeeming = false
      redemptionResult = result
      if (result is VoucherResult.Success) {
        voucherInput = ""
        onTierUnlocked(result.tier)
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = palette.dialogSurface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 680.dp)
        .align(Alignment.CenterHorizontally)
        .verticalScroll(rememberScrollState())
        .navigationBarsPadding()
        .imePadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 26.dp)
        .testTag("sibil_upgrade_sheet"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Header: "Unlock Sibil CE" + Current Plan Pill + Close Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                brush = Brush.linearGradient(
                  colors = listOf(
                    Color(0xFF38BDF8),
                    Color(0xFF0284C7),
                    Color(0xFF2563EB)
                  )
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Rounded.WorkspacePremium,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(21.dp)
            )
          }

          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Text(
              text = "Unlock Sibil CE",
              style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = AppleSansFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
              ),
              color = palette.textPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("upgrade_sheet_title")
            )

            Surface(
              color = if (activeTier == SibilTier.FREE) {
                palette.nestedCardFill
              } else {
                Color(0xFF10B981).copy(alpha = 0.16f)
              },
              shape = CircleShape,
              border = BorderStroke(
                1.dp,
                if (activeTier == SibilTier.FREE) {
                  palette.nestedBorderColor
                } else {
                  Color(0xFF10B981).copy(alpha = 0.45f)
                }
              )
            ) {
              Text(
                text = activeTier.currentPlanPillLabel(),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = AppleSansFamily,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = if (activeTier == SibilTier.FREE) {
                  palette.textSecondary
                } else if (palette.isDark) {
                  Color(0xFF34D399)
                } else {
                  Color(0xFF047857)
                },
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                  .padding(horizontal = 10.dp, vertical = 3.dp)
                  .testTag("upgrade_current_plan_pill")
              )
            }
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(48.dp)
            .testTag("upgrade_sheet_close_button")
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Close Upgrade Sheet",
            tint = palette.textSecondary
          )
        }
      }

      // 2. Top-Priority Voucher Redemption Box
      Surface(
        color = palette.nestedCardFill,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, palette.accentIndigo.copy(alpha = 0.42f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("voucher_redemption_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Rounded.ConfirmationNumber,
              contentDescription = null,
              tint = palette.accentIndigo,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Redeem One-Time Voucher Code",
              style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = AppleSansFamily,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
              ),
              color = palette.textPrimary
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = voucherInput,
              onValueChange = { next ->
                voucherInput = next.uppercase(Locale.US)
                redemptionResult = null
              },
              label = { Text("Enter Voucher Code") },
              placeholder = { Text("e.g. SIBIL99-XXXX") },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              colors = sibilOutlinedTextFieldColors(),
              keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
              ),
              keyboardActions = KeyboardActions(
                onDone = { submitVoucherRedemption() }
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("voucher_code_input")
            )

            Button(
              onClick = { submitVoucherRedemption() },
              enabled = !isRedeeming && voucherInput.isNotBlank(),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = palette.accentIndigo,
                contentColor = Color.White
              ),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
              modifier = Modifier
                .heightIn(min = 52.dp)
                .testTag("voucher_redeem_button")
            ) {
              if (isRedeeming) {
                CircularProgressIndicator(
                  modifier = Modifier.size(18.dp),
                  strokeWidth = 2.dp,
                  color = Color.White
                )
              } else {
                Text(
                  text = "Redeem",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.5.sp,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }

          // Inline Success / Error / Sign-In Banner
          when (val feedback = redemptionResult) {
            is VoucherResult.Success -> {
              Surface(
                color = Color(0xFF10B981).copy(alpha = 0.16f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("voucher_feedback_banner")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = if (palette.isDark) Color(0xFF34D399) else Color(0xFF047857),
                    modifier = Modifier.size(18.dp)
                  )
                  Text(
                    text = feedback.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.5.sp,
                      fontWeight = FontWeight.SemiBold
                    ),
                    color = if (palette.isDark) Color(0xFF34D399) else Color(0xFF047857)
                  )
                }
              }
            }

            is VoucherResult.RequiresSignIn -> {
              Surface(
                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.48f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("voucher_feedback_banner")
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text(
                    text = "Sign in or create a free Sibil CE account first so your one-time payment is permanently bound to your account.",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.5.sp,
                      fontWeight = FontWeight.SemiBold
                    ),
                    color = palette.textPrimary
                  )
                  if (onRequestSignIn != null) {
                    Button(
                      onClick = {
                        onDismiss()
                        onRequestSignIn()
                      },
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accentIndigo,
                        contentColor = Color.White
                      ),
                      modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .testTag("voucher_sign_in_prompt_button")
                    ) {
                      Text(
                        text = "Sign In / Create Free Account",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              }
            }

            is VoucherResult.Error -> {
              Surface(
                color = palette.accentRed.copy(alpha = 0.14f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, palette.accentRed.copy(alpha = 0.45f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("voucher_feedback_banner")
              ) {
                Text(
                  text = feedback.message,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = palette.accentRed,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
              }
            }

            null -> Unit
          }

          Text(
            text = "1. Pay ₱49 or ₱99 one-time via GCash / Maya  →  2. Receive your Sibil Voucher Code  →  3. Redeem above to unlock forever.",
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = AppleSansFamily,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Medium,
              lineHeight = 16.sp
            ),
            color = palette.textSecondary
          )
        }
      }

      // 3. Three Tier Comparison Cards (Free ₱0, ₱49 One-Time, ₱99 One-Time)
      SIBIL_TIER_SPECS.forEach { spec ->
        val isActive = activeTier == spec.tier
        val isHighlighted = highlightedTier == spec.tier && !isActive
        val cardBorder = when {
          isActive -> BorderStroke(1.6.dp, Color(0xFF10B981).copy(alpha = 0.72f))
          isHighlighted -> BorderStroke(1.8.dp, spec.accentColor.copy(alpha = 0.85f))
          else -> palette.nestedBorder
        }
        val cardFill = when {
          isActive -> Color(0xFF10B981).copy(alpha = if (palette.isDark) 0.12f else 0.08f)
          isHighlighted -> spec.accentColor.copy(alpha = if (palette.isDark) 0.16f else 0.09f)
          else -> palette.nestedCardFill
        }

        Surface(
          color = cardFill,
          shape = RoundedCornerShape(20.dp),
          border = cardBorder,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("upgrade_tier_card_${spec.tier.name}")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                if (spec.tier == SibilTier.TIER_99) {
                  Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = spec.accentColor,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Text(
                  text = spec.tier.title,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = AppleSansFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                  ),
                  color = palette.textPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (isActive) {
                  Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.18f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.55f))
                  ) {
                    Text(
                      text = "Active Plan",
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (palette.isDark) Color(0xFF34D399) else Color(0xFF047857),
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                  }
                } else if (isHighlighted) {
                  Surface(
                    color = spec.accentColor.copy(alpha = 0.20f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, spec.accentColor.copy(alpha = 0.55f))
                  ) {
                    Text(
                      text = "Recommended",
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (palette.isDark) Color.White else spec.accentColor,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                  }
                }

                Surface(
                  color = spec.accentColor.copy(alpha = 0.16f),
                  shape = CircleShape,
                  border = BorderStroke(1.dp, spec.accentColor.copy(alpha = 0.35f))
                ) {
                  Text(
                    text = spec.tier.price,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontFamily = JetBrainsMonoFamily,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = if (palette.isDark) Color.White else spec.accentColor,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                  )
                }
              }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              spec.features.forEach { featureLine ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (isActive) Color(0xFF10B981) else spec.accentColor,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = featureLine,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = AppleSansFamily,
                      fontSize = 12.5.sp,
                      fontWeight = FontWeight.Medium
                    ),
                    color = palette.textPrimary
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

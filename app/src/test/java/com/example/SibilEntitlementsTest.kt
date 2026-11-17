package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SibilEntitlementsTest {

  private class FakeEntitlementRealtimeDbGateway : EntitlementRealtimeDbGateway {
    val vouchers = mutableMapOf<String, MutableMap<String, Any>>()
    val usersEntitlement = mutableMapOf<String, MutableMap<String, Any>>()

    override suspend fun fetchVoucher(code: String, idToken: String): VoucherRecordSnapshot {
      val doc = vouchers[code] ?: return VoucherRecordSnapshot(exists = false)
      return VoucherRecordSnapshot(
        exists = true,
        tierRaw = doc["tier"] as? String,
        used = (doc["used"] as? Boolean) == true,
        redeemedByUid = (doc["redeemedByUid"] as? String).orEmpty()
      )
    }

    override suspend fun markVoucherRedeemed(
      code: String,
      uid: String,
      email: String,
      redeemedAtMillis: Long,
      idToken: String
    ) {
      val existing = vouchers.getOrPut(code) { mutableMapOf() }
      existing["used"] = true
      existing["redeemedByUid"] = uid
      existing["redeemedByEmail"] = email
      existing["redeemedAt"] = redeemedAtMillis
    }

    override suspend fun fetchUserTier(uid: String, idToken: String): SibilTier? {
      val doc = usersEntitlement[uid] ?: return null
      return SibilTier.fromNameOrNull(doc["tier"] as? String)
    }

    override suspend fun upsertUserTier(
      uid: String,
      tier: SibilTier,
      lastVoucher: String?,
      updatedAtMillis: Long,
      idToken: String
    ) {
      val existing = usersEntitlement.getOrPut(uid) { mutableMapOf() }
      existing["tier"] = tier.name
      if (!lastVoucher.isNullOrBlank()) {
        existing["lastVoucher"] = lastVoucher
      }
      existing["updatedAt"] = updatedAtMillis
    }
  }

  private lateinit var context: Context
  private lateinit var fakeGateway: FakeEntitlementRealtimeDbGateway
  private var activeUser: AuthenticatedUserIdentity? = null

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    context.getSharedPreferences(SibilEntitlementManager.TIER_PREFS_NAME, Context.MODE_PRIVATE)
      .edit().clear().commit()
    fakeGateway = FakeEntitlementRealtimeDbGateway()
    SibilEntitlementManager.realtimeGateway = fakeGateway
    SibilEntitlementManager.currentUserProvider = { activeUser }
  }

  @After
  fun tearDown() {
    SibilEntitlementManager.realtimeGateway = FirebaseRealtimeDbEntitlementGateway
    context.getSharedPreferences(SibilEntitlementManager.TIER_PREFS_NAME, Context.MODE_PRIVATE)
      .edit().clear().commit()
  }

  @Test
  fun tierDefinitions_matchExact3TierSpecification() {
    assertEquals(0, SibilTier.FREE.level)
    assertEquals("FREE", SibilTier.FREE.badge)
    assertEquals("Free Version", SibilTier.FREE.title)
    assertEquals("₱0", SibilTier.FREE.price)
    assertEquals(10, SibilTier.FREE.maxFlashcardDecks)

    assertEquals(1, SibilTier.TIER_49.level)
    assertEquals("₱69", SibilTier.TIER_49.badge)
    assertEquals("Plus Plan", SibilTier.TIER_49.title)
    assertEquals("₱69 One-Time", SibilTier.TIER_49.price)
    assertEquals(25, SibilTier.TIER_49.maxFlashcardDecks)

    assertEquals(2, SibilTier.TIER_99.level)
    assertEquals("₱109 PRO", SibilTier.TIER_99.badge)
    assertEquals("Pro Unlimited", SibilTier.TIER_99.title)
    assertEquals("₱109 One-Time", SibilTier.TIER_99.price)
    assertEquals(Int.MAX_VALUE, SibilTier.TIER_99.maxFlashcardDecks)
  }

  @Test
  fun realtimeDbUrl_resolvesDynamicAsiaSoutheastFallbackWhenMissingFromGoogleServices() {
    val resolvedUrl = SibilRealtimeDbRest.resolveDatabaseUrl()
    assertTrue(resolvedUrl.startsWith("https://"))
    assertTrue(resolvedUrl.contains("firebasedatabase.app"))
    assertFalse(resolvedUrl.endsWith("/"))
  }

  @Test
  fun redeemVoucherCode_requiresSignedInNonAnonymousUser() = runBlocking {
    activeUser = null
    assertEquals(
      VoucherResult.RequiresSignIn,
      SibilEntitlementManager.redeemVoucherCode(context, "SIBIL99-1234")
    )

    activeUser = AuthenticatedUserIdentity(
      uid = "anon-uid-1",
      email = "",
      isGuestOrAnonymous = true,
      tokenFetcher = { "fake-token" }
    )
    assertEquals(
      VoucherResult.RequiresSignIn,
      SibilEntitlementManager.redeemVoucherCode(context, "SIBIL99-1234")
    )
  }

  @Test
  fun redeemVoucherCode_validatesRedeemRestoreConflictAndNoDowngrade() = runBlocking {
    activeUser = AuthenticatedUserIdentity(
      uid = "user-ce-001",
      email = "engineer@sibil.app",
      isGuestOrAnonymous = false,
      tokenFetcher = { "valid-id-token" }
    )

    // Missing / invalid voucher
    val missingResult = SibilEntitlementManager.redeemVoucherCode(context, "  bad code 99 ")
    assertTrue(missingResult is VoucherResult.Error)
    assertEquals(
      "Invalid voucher code. Check your code and try again.",
      (missingResult as VoucherResult.Error).message
    )

    // Seed vouchers in fake Realtime Database
    fakeGateway.vouchers["SIBIL49-AAAA"] = mutableMapOf(
      "tier" to "TIER_49",
      "used" to false
    )
    fakeGateway.vouchers["SIBIL99-BBBB"] = mutableMapOf(
      "tier" to "TIER_99",
      "used" to false
    )
    fakeGateway.vouchers["SIBIL99-OTHER"] = mutableMapOf(
      "tier" to "TIER_99",
      "used" to true,
      "redeemedByUid" to "someone-else"
    )

    // 1. Already redeemed by another account
    val conflictResult = SibilEntitlementManager.redeemVoucherCode(context, "SIBIL99-OTHER")
    assertTrue(conflictResult is VoucherResult.Error)
    assertEquals(
      "This voucher code has already been redeemed by another account.",
      (conflictResult as VoucherResult.Error).message
    )

    // 2. Redeem ₱49 Plus voucher (with lowercase & spaces to verify normalization)
    val plusResult = SibilEntitlementManager.redeemVoucherCode(context, " sibil49 - aaaa ".replace(" - ", "-"))
    assertTrue(plusResult is VoucherResult.Success)
    assertEquals(SibilTier.TIER_49, (plusResult as VoucherResult.Success).tier)
    assertEquals(true, fakeGateway.vouchers["SIBIL49-AAAA"]?.get("used"))
    assertEquals("user-ce-001", fakeGateway.vouchers["SIBIL49-AAAA"]?.get("redeemedByUid"))
    assertEquals("engineer@sibil.app", fakeGateway.vouchers["SIBIL49-AAAA"]?.get("redeemedByEmail"))
    assertEquals("TIER_49", fakeGateway.usersEntitlement["user-ce-001"]?.get("tier"))
    assertEquals("SIBIL49-AAAA", fakeGateway.usersEntitlement["user-ce-001"]?.get("lastVoucher"))
    assertEquals(SibilTier.TIER_49, SibilEntitlementManager.getCachedTier(context, "user-ce-001"))

    // 3. Upgrade to ₱99 Pro voucher
    val proResult = SibilEntitlementManager.redeemVoucherCode(context, "sibil99-bbbb")
    assertTrue(proResult is VoucherResult.Success)
    assertEquals(SibilTier.TIER_99, (proResult as VoucherResult.Success).tier)
    assertEquals("TIER_99", fakeGateway.usersEntitlement["user-ce-001"]?.get("tier"))
    assertEquals(SibilTier.TIER_99, SibilEntitlementManager.getCachedTier(context, "user-ce-001"))

    // 4. Re-redeeming same user's ₱49 voucher restores without downgrading from ₱99
    val restoreResult = SibilEntitlementManager.redeemVoucherCode(context, "SIBIL49-AAAA")
    assertTrue(restoreResult is VoucherResult.Success)
    assertEquals("Your ₱69 One-Time access has been restored!", (restoreResult as VoucherResult.Success).message)
    assertEquals(SibilTier.TIER_99, (restoreResult as VoucherResult.Success).tier)
    assertEquals("TIER_99", fakeGateway.usersEntitlement["user-ce-001"]?.get("tier"))
  }

  @Test
  fun offlineCacheAndCloudSync_neverDowngradesValidPurchase_and_demoSwitcherWorksOffline() = runBlocking {
    activeUser = AuthenticatedUserIdentity(
      uid = "user-ce-002",
      email = "student@sibil.app",
      isGuestOrAnonymous = false,
      tokenFetcher = { "valid-id-token" }
    )

    // Remote has TIER_99 while local cache starts FREE -> upgrades to TIER_99
    fakeGateway.usersEntitlement["user-ce-002"] = mutableMapOf("tier" to "TIER_99")
    assertEquals(SibilTier.TIER_99, SibilEntitlementManager.syncTierFromCloud(context))
    assertEquals(SibilTier.TIER_99, SibilEntitlementManager.getCachedTier(context, "user-ce-002"))

    // Remote somehow reports TIER_49 while local cache is TIER_99 -> maxOf keeps TIER_99
    fakeGateway.usersEntitlement["user-ce-002"] = mutableMapOf("tier" to "TIER_49")
    assertEquals(SibilTier.TIER_99, SibilEntitlementManager.syncTierFromCloud(context))

    // Panel Defense Demo Switcher overrides locally in 1 tap even offline
    SibilEntitlementManager.setDemoTierOverride(context, SibilTier.FREE)
    assertTrue(SibilEntitlementManager.isDemoOverrideActive(context, "user-ce-002"))
    assertEquals(SibilTier.FREE, SibilEntitlementManager.getCachedTier(context, "user-ce-002"))
    assertEquals(SibilTier.FREE, SibilEntitlementManager.syncTierFromCloud(context))

    // Switching demo tier to TIER_49 updates offline cache
    SibilEntitlementManager.setDemoTierOverride(context, SibilTier.TIER_49)
    assertEquals(SibilTier.TIER_49, SibilEntitlementManager.getCachedTier(context, "user-ce-002"))

    // Real voucher redemption clears demo override flag
    fakeGateway.vouchers["SIBIL99-REAL"] = mutableMapOf("tier" to "TIER_99", "used" to false)
    val redeemed = SibilEntitlementManager.redeemVoucherCode(context, "SIBIL99-REAL")
    assertTrue(redeemed is VoucherResult.Success)
    assertFalse(SibilEntitlementManager.isDemoOverrideActive(context, "user-ce-002"))
    assertEquals(SibilTier.TIER_99, SibilEntitlementManager.getCachedTier(context, "user-ce-002"))
  }

  @Test
  fun realtimeSyncJsonCodecAndMerger_preserveTasksSchedulesFlashcardsAndSettings() {
    val payload = RemoteSyncPayload(
      tasks = listOf(
        Task(
          id = 10,
          taskUid = "10",
          title = "Solve PSAD Shear & Moment",
          dueDate = "2026-11-20",
          dueDateIso = "2026-11-20",
          priority = 2,
          subjectCategory = "PSAD",
          categoryTag = "PSAD",
          subtasks = listOf(SubtaskItem(id = "s1", text = "Draw FBD", isDone = true))
        )
      ),
      schedules = listOf(
        ClassSchedule(
          id = 101,
          subjectName = "CE 412 Reinforced Concrete",
          timeRange = "8:00 AM - 10:00 AM",
          days = listOf("M", "W"),
          room = "ENG 204"
        )
      ),
      flashcards = listOf(
        FlashcardDeck(
          id = 5,
          title = "Geotechnical Formulas",
          subjectTag = "HPGE",
          cards = listOf(
            FlashcardItem(
              id = 501,
              question = "Darcy's Law",
              answer = "q = k i A",
              category = "HPGE",
              masteryLevel = "Good",
              isMastered = true
            )
          )
        )
      ),
      settings = RemoteSyncSettings(
        isDarkMode = true,
        profileName = "Engr. Reyes",
        username = "reyes@sibil.app",
        updatedAt = 1700000000000L
      ),
      updatedAt = 1700000000000L
    )

    val rawJson = SibilSyncJsonCodec.payloadToJsonObject(payload).toString()
    val parsed = SibilSyncJsonCodec.parsePayloadFromJson(rawJson)
    assertNotNull(parsed)
    assertEquals(1, parsed?.tasks?.size)
    assertEquals("Draw FBD", parsed?.tasks?.firstOrNull()?.subtasks?.firstOrNull()?.text)
    assertEquals(1, parsed?.schedules?.size)
    assertEquals("CE 412 Reinforced Concrete", parsed?.schedules?.firstOrNull()?.subjectName)
    assertEquals(1, parsed?.flashcards?.size)
    assertEquals("q = k i A", parsed?.flashcards?.firstOrNull()?.cards?.firstOrNull()?.answer)
    assertEquals(true, parsed?.settings?.isDarkMode)
    assertEquals("Engr. Reyes", parsed?.settings?.profileName)

    // Merge local offline task + remote task without duplicates
    val merged = SibilRealtimeSyncMerger.mergeOnPull(
      localTasks = listOf(
        Task(
          id = 10,
          taskUid = "10",
          title = "Local Unsynced HPGE Review",
          dueDate = "2026-11-22",
          dueDateIso = "2026-11-22",
          subjectCategory = "HPGE",
          categoryTag = "HPGE"
        )
      ),
      localSchedules = emptyList(),
      localDecks = emptyList(),
      localDarkMode = false,
      localDisplayName = "Local User",
      localUsername = "reyes@sibil.app",
      remote = parsed
    )

    assertEquals(2, merged.tasks.size)
    assertTrue(merged.tasks.any { it.title == "Solve PSAD Shear & Moment" })
    assertTrue(merged.tasks.any { it.title == "Local Unsynced HPGE Review" })
    assertTrue(merged.shouldPushBackMergedToCloud)
  }
}

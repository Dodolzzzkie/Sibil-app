package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GeminiStudyHelperTest {

  @Test
  fun modelFallbackOrder_and_customKeyPriority_areConfiguredCorrectly() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals(
      listOf("gemini-3.5-flash-lite", "gemini-2.5-flash", "gemini-3.8-flash"),
      GeminiStudyClient.MODEL_FALLBACK_ORDER
    )

    GeminiStudyClient.saveCustomApiKey(context, "")
    assertEquals("", GeminiStudyClient.getCustomApiKey(context))

    GeminiStudyClient.saveCustomApiKey(context, "AIzaTestCustomKey12345")
    assertEquals("AIzaTestCustomKey12345", GeminiStudyClient.getCustomApiKey(context))
    assertTrue(GeminiStudyClient.hasConfiguredKey(context))
    assertEquals("AIzaTestCustomKey12345", GeminiStudyClient.getOrderedApiKeys(context).first())
  }

  @Test
  fun resolveInjectedGeminiKeys_filtersPlaceholders_and_readsSharedAndFlashcardPrefs() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sibilPrefs = context.getSharedPreferences(GeminiStudyClient.SIBIL_PREFS_NAME, Context.MODE_PRIVATE)
    val offlinePrefs = context.getSharedPreferences(GeminiStudyClient.PREFS_NAME, Context.MODE_PRIVATE)
    sibilPrefs.edit().clear().commit()
    offlinePrefs.edit().clear().commit()

    assertTrue(
      parseValidGeminiKeyTokens("__KEY_INJECTION_PLACEHOLDER__, \"__KEY_INJECTION_PLACEHOLDER__\", 'MY_KEY_INJECTION_PLACEHOLDER_TOKEN', MY_GEMINI_API_KEY, null,  ").isEmpty()
    )
    assertEquals(
      listOf("AIzaPoolKey1", "AIzaPoolKey2"),
      parseValidGeminiKeyTokens("\"AIzaPoolKey1\", __KEY_INJECTION_PLACEHOLDER__, 'AIzaPoolKey2'").toList()
    )

    offlinePrefs.edit().putString("saved_gemini_api_key", "AIzaFlashcardKey1, AIzaFlashcardKey2").commit()
    assertEquals(
      listOf("AIzaFlashcardKey1", "AIzaFlashcardKey2"),
      getSharedGeminiApiKeys(context).take(2)
    )
    assertEquals(
      getSharedGeminiApiKeys(context),
      resolveInjectedGeminiKeys(context)
    )
    assertTrue(GeminiStudyClient.hasConfiguredKey(context))

    GeminiStudyClient.saveCustomApiKey(context, "AIzaStudyOverrideKey")
    val ordered = GeminiStudyClient.getOrderedApiKeys(context)
    assertEquals("AIzaStudyOverrideKey", ordered.first())
    assertTrue(ordered.contains("AIzaFlashcardKey1"))

    val flashcardPool = GizmoBatchFlashcardEngine.buildValidKeyPool("", context)
    assertEquals("AIzaStudyOverrideKey", flashcardPool.first())
    assertTrue(flashcardPool.contains("AIzaFlashcardKey1"))

    sibilPrefs.edit().clear().commit()
    offlinePrefs.edit().clear().commit()
  }

  @Test
  fun multiChatPersistence_savesThreadsAutoTitlesAndDeletesCleanly() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences(GeminiStudyClient.SESSIONS_PREFS_NAME, Context.MODE_PRIVATE)
      .edit().clear().commit()

    val firstTitle = GeminiStudyClient.deriveAutoTitle("  Compute the moment capacity   of a singly reinforced concrete beam per NSCP 2015  ")
    assertEquals(38, firstTitle.length)
    assertEquals("Compute the moment capacity of a singl", firstTitle)

    val thread1 = GeminiChatThread(
      id = "t1",
      title = firstTitle,
      updatedAt = 1000L,
      messages = listOf(
        GeminiMessage(id = "u1", isUser = true, text = "Explain NSCP 2015 phi factors"),
        GeminiMessage(
          id = "a1",
          isUser = false,
          text = "For tension-controlled sections, phi = 0.90.",
          sources = listOf(WebSource("ASEP NSCP 2015 Reference", "https://example.org/nscp2015"))
        )
      )
    )
    val thread2 = GeminiChatThread(
      id = "t2",
      title = "Kotlin Coroutines Guide",
      updatedAt = 2000L,
      messages = listOf(
        GeminiMessage(id = "u2", isUser = true, text = "Explain Dispatchers.IO")
      )
    )

    GeminiStudyClient.upsertThread(context, thread1)
    GeminiStudyClient.upsertThread(context, thread2)

    val loaded = GeminiStudyClient.loadThreads(context)
    assertEquals(2, loaded.size)
    assertEquals("t2", loaded[0].id)
    assertEquals("t1", loaded[1].id)
    assertEquals(1, loaded[1].messages[1].sources.size)
    assertEquals("https://example.org/nscp2015", loaded[1].messages[1].sources[0].uri)

    val afterDelete = GeminiStudyClient.deleteThread(context, "t2")
    assertEquals(1, afterDelete.size)
    assertEquals("t1", afterDelete[0].id)
  }

  @Test
  fun payloadAndGroundingParser_includeGoogleSearchToolsSafetySettingsAndDeduplicatedSources() {
    val payload = GeminiStudyClient.buildGenerateContentPayload(
      userPrompt = "Latest typhoon PAGASA update today",
      history = emptyList(),
      imageBase64 = null,
      groundingMode = GroundingToolMode.GOOGLE_SEARCH
    )

    val tools = payload.getJSONArray("tools")
    assertEquals(1, tools.length())
    assertTrue(tools.getJSONObject(0).has("google_search"))

    val safety = payload.getJSONArray("safetySettings")
    assertEquals(4, safety.length())
    assertEquals("HARM_CATEGORY_SEXUALLY_EXPLICIT", safety.getJSONObject(0).getString("category"))
    assertEquals("BLOCK_LOW_AND_ABOVE", safety.getJSONObject(0).getString("threshold"))

    val fakeApiResponse = JSONObject().apply {
      put(
        "candidates",
        JSONArray().put(
          JSONObject().apply {
            put("finishReason", "STOP")
            put(
              "groundingMetadata",
              JSONObject().apply {
                put(
                  "groundingChunks",
                  JSONArray()
                    .put(
                      JSONObject().put(
                        "web",
                        JSONObject()
                          .put("title", "PAGASA Weather")
                          .put("uri", "https://bagong.pagasa.dost.gov.ph/")
                      )
                    )
                    .put(
                      JSONObject().put(
                        "web",
                        JSONObject()
                          .put("title", "PAGASA Duplicate")
                          .put("uri", "https://bagong.pagasa.dost.gov.ph/")
                      )
                    )
                    .put(
                      JSONObject().put(
                        "web",
                        JSONObject()
                          .put("title", "DPWH Standards")
                          .put("uri", "https://www.dpwh.gov.ph/")
                      )
                    )
                )
              }
            )
          }
        )
      )
    }

    val sources = GeminiStudyClient.parseGroundingSources(fakeApiResponse)
    assertEquals(2, sources.size)
    assertEquals("PAGASA Weather", sources[0].title)
    assertEquals("https://www.dpwh.gov.ph/", sources[1].uri)
  }

  @Test
  fun explicitContentFilter_blocksExplicitPrompts_whileAllowingEngineeringAndGeneralTopics() {
    assertTrue(GeminiStudyClient.isExplicitOrRestrictedPrompt("Show me hardcore pornography"))
    assertTrue(GeminiStudyClient.isExplicitOrRestrictedPrompt("Write nsfw erotica story"))
    assertTrue(GeminiStudyClient.isExplicitOrRestrictedPrompt("How to commit suicide"))

    assertFalse(GeminiStudyClient.isExplicitOrRestrictedPrompt("Steel truss erection sequence and bracing"))
    assertFalse(GeminiStudyClient.isExplicitOrRestrictedPrompt("Standard penetration test N-value bearing capacity"))
    assertFalse(GeminiStudyClient.isExplicitOrRestrictedPrompt("World War II Pacific theater timeline"))
    assertFalse(GeminiStudyClient.isExplicitOrRestrictedPrompt("Write a Python binary search function"))

    val safetyBlockedJson = JSONObject().apply {
      put("promptFeedback", JSONObject().put("blockReason", "SAFETY"))
    }
    assertTrue(GeminiStudyClient.detectSafetyBlock(safetyBlockedJson))

    val safetyFinishJson = JSONObject().apply {
      put("candidates", JSONArray().put(JSONObject().put("finishReason", "SAFETY")))
    }
    assertTrue(GeminiStudyClient.detectSafetyBlock(safetyFinishJson))

    val safeStopJson = JSONObject().apply {
      put("candidates", JSONArray().put(JSONObject().put("finishReason", "STOP")))
    }
    assertFalse(GeminiStudyClient.detectSafetyBlock(safeStopJson))
  }

  @Test
  fun cascadeFallback_retriesWithoutToolsOn429_rotatesKeysAndModels_andSanitizesErrors() = runBlocking {
    val calls = mutableListOf<Triple<String, String, Boolean>>()

    // 1. If google_search hits HTTP 429 on first model/key, retries without tools on same model/key & succeeds
    val toolFallbackResult = GeminiStudyClient.generateReplyWithExecutor(
      apiKeys = listOf("AIzaKeyOne", "AIzaKeyTwo"),
      userPrompt = "hi",
      history = emptyList(),
      imageBase64 = null
    ) { endpoint, payloadJson ->
      val hasTools = JSONObject(payloadJson).has("tools")
      val model = endpoint.substringAfter("/models/").substringBefore(":generateContent")
      val key = endpoint.substringAfter("?key=")
      calls += Triple(model, key, hasTools)
      if (hasTools) {
        429 to """{"error":{"message":"Quota exceeded for search grounding https://ai.google.dev/gemini-api/docs/rate-limits"}}"""
      } else {
        200 to """{"candidates":[{"finishReason":"STOP","content":{"parts":[{"text":"Hello from Sibil AI!"}]}}]}"""
      }
    }

    assertTrue(toolFallbackResult is GeminiStudyClient.StudyAiCallResult.Success)
    assertEquals(
      "Hello from Sibil AI!",
      (toolFallbackResult as GeminiStudyClient.StudyAiCallResult.Success).message.text
    )
    assertEquals(
      listOf(
        Triple("gemini-3.5-flash-lite", "AIzaKeyOne", true),
        Triple("gemini-3.5-flash-lite", "AIzaKeyOne", false)
      ),
      calls
    )

    // 2. If all keys hit 429 on first model, cascade falls through to gemini-2.5-flash on remaining keys
    val modelCascadeCalls = mutableListOf<Triple<String, String, Boolean>>()
    val modelCascadeResult = GeminiStudyClient.generateReplyWithExecutor(
      apiKeys = listOf("AIzaKeyA", "AIzaKeyB"),
      userPrompt = "Explain Darcy's law",
      history = emptyList(),
      imageBase64 = null
    ) { endpoint, payloadJson ->
      val hasTools = JSONObject(payloadJson).has("tools")
      val model = endpoint.substringAfter("/models/").substringBefore(":generateContent")
      val key = endpoint.substringAfter("?key=")
      modelCascadeCalls += Triple(model, key, hasTools)
      if (model == "gemini-2.5-flash" && key == "AIzaKeyB") {
        200 to """{"candidates":[{"finishReason":"STOP","content":{"parts":[{"text":"Q = k i A"}]}}]}"""
      } else {
        429 to """{"error":{"message":"RESOURCE_EXHAUSTED"}}"""
      }
    }

    assertTrue(modelCascadeResult is GeminiStudyClient.StudyAiCallResult.Success)
    assertEquals("Q = k i A", (modelCascadeResult as GeminiStudyClient.StudyAiCallResult.Success).message.text)
    assertTrue(modelCascadeCalls.any { it.first == "gemini-3.5-flash-lite" && it.second == "AIzaKeyA" })
    assertTrue(modelCascadeCalls.any { it.first == "gemini-3.5-flash-lite" && it.second == "AIzaKeyB" })
    assertTrue(modelCascadeCalls.any { it.first == "gemini-2.5-flash" && it.second == "AIzaKeyA" })
    assertTrue(modelCascadeCalls.any { it.first == "gemini-2.5-flash" && it.second == "AIzaKeyB" })

    // 3. When all models x all keys return 429 with billing URLs, returns clean friendly message without raw URLs
    val totalExhaustedResult = GeminiStudyClient.generateReplyWithExecutor(
      apiKeys = listOf("AIzaKeyA", "AIzaKeyB"),
      userPrompt = "hi",
      history = emptyList(),
      imageBase64 = null
    ) { _, _ ->
      429 to """{"error":{"message":"You exceeded your current quota. Check https://ai.google.dev/gemini-api/docs/rate-limits"}}"""
    }

    assertTrue(totalExhaustedResult is GeminiStudyClient.StudyAiCallResult.Failure)
    val errMsg = (totalExhaustedResult as GeminiStudyClient.StudyAiCallResult.Failure).errorMessage
    assertFalse(errMsg.contains("https://"))
    assertTrue(errMsg.contains("temporarily busy", ignoreCase = true))
  }
}

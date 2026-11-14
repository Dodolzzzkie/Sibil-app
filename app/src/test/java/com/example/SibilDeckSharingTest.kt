package com.example

import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SibilDeckSharingTest {

  private fun buildLargeCeDeck(cardCount: Int = 78): FlashcardDeck {
    val cards = (1..cardCount).map { idx ->
      FlashcardItem(
        id = idx,
        question = "CE Board Item #$idx: What is the NSCP 2015 strength reduction factor and design requirement for flexural member section $idx with fc'=28 MPa and fy=415 MPa?",
        answer = "Phi = 0.90 when net tensile strain >= 0.005; minimum steel ratio As,min = max(0.25*sqrt(fc')/fy, 1.4/fy)*bw*d (Section 409.$idx).",
        category = "PSAD"
      )
    }
    return FlashcardDeck(
      id = 42,
      title = "PSAD Reinforced Concrete (NSCP 2015)",
      subjectTag = "PSAD",
      cards = cards
    )
  }

  @Test
  fun base45_roundTrip_preservesAllBytesIncludingColonsAndSpaces() {
    val sample = ByteArray(257) { i -> (i * 31 + 7).toByte() }
    val encoded = SibilDeckCodec.encodeBase45(sample)
    assertTrue(encoded.all { it in SibilDeckCodec.BASE45_ALPHABET })
    val decoded = SibilDeckCodec.decodeBase45(encoded)
    assertNotNull(decoded)
    assertTrue(sample.contentEquals(decoded))
  }

  @Test
  fun singleCode_and_large78CardDeck_airScanOutOfOrderReassembly_succeeds() {
    val deck = buildLargeCeDeck(78)
    val fullCode = exportDeckToOfflineCode(deck)
    assertTrue(fullCode.startsWith("SIBIL2:1/1:"))

    // Full single-string import
    val directImported = importDeckFromOfflineCode(fullCode)
    assertNotNull(directImported)
    assertEquals(deck.title, directImported!!.title)
    assertEquals(deck.subjectTag, directImported.subjectTag)
    assertEquals(78, directImported.cards.size)
    assertEquals(deck.cards.first().question, directImported.cards.first().question)
    assertEquals(deck.cards.last().answer, directImported.cards.last().answer)

    // Multi-frame AirScan split
    val frames = SibilDeckCodec.encodeAirScanFrames(deck, SibilDeckCodec.DEFAULT_AIRSCAN_CHUNK_CHARS)
    assertTrue("Expected multiple frames for 78 cards", frames.size > 1)
    for (frame in frames) {
      assertTrue(frame.base45Chunk.length <= SibilDeckCodec.DEFAULT_AIRSCAN_CHUNK_CHARS)
      // Verify strict QR Alphanumeric character set across entire packet
      assertTrue(
        "Packet must stay in QR Alphanumeric set: ${frame.packetString}",
        frame.packetString.all { it in SibilDeckCodec.BASE45_ALPHABET }
      )
    }

    // Parse frames in reversed (out-of-order) arrival order and reassemble
    val captured = mutableMapOf<Int, String>()
    var expectedHash: String? = null
    var expectedTotal = 0
    for (rawPacket in frames.reversed().map { it.packetString }) {
      val parsed = SibilDeckCodec.parseAirScanFrame(rawPacket)
      assertNotNull(parsed)
      expectedHash = parsed!!.deckHash4
      expectedTotal = parsed.totalParts
      captured[parsed.partIndex] = parsed.base45Chunk
    }

    val reassembled = SibilDeckCodec.reassembleAndDecode(
      parts = captured,
      totalParts = expectedTotal,
      expectedDeckHash4 = expectedHash
    )
    assertNotNull(reassembled)
    assertEquals(deck.title, reassembled!!.title)
    assertEquals(78, reassembled.cards.size)
    assertEquals(deck.cards[39].question, reassembled.cards[39].question)
    assertEquals(deck.cards[77].answer, reassembled.cards[77].answer)

    // Verify QR bitmap generation succeeds on SIBIL2 frame without SIBIL_DECK prefix corruption
    val qrBitmap = generateDeckQrBitmap(frames.first().packetString, size = 256)
    assertNotNull(qrBitmap)
    assertEquals(256, qrBitmap!!.width)
  }

  @Test
  fun checksumMismatch_isRejected() {
    val deck = buildLargeCeDeck(78)
    val frames = SibilDeckCodec.encodeAirScanFrames(deck)
    val parts = frames.associate { it.partIndex to it.base45Chunk }
    val invalidResult = SibilDeckCodec.reassembleAndDecode(
      parts = parts,
      totalParts = frames.size,
      expectedDeckHash4 = "0000"
    )
    // Only skip if actual hash happened to be 0000
    if (frames.first().deckHash4 != "0000") {
      assertNull(invalidResult)
    }
  }

  @Test
  fun legacy_SIBIL_DECK_format_remainsBackwardCompatible() {
    val legacyJson = """{"t":"Hydraulics Formulas","s":"HPGE","c":[{"q":"Manning Equation","a":"V = (1/n) R^(2/3) S^(1/2)"}]}"""
    val baos = ByteArrayOutputStream()
    GZIPOutputStream(baos).use { it.write(legacyJson.toByteArray(Charsets.UTF_8)) }
    val legacyCode = "SIBIL_DECK:" + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

    val decoded = importDeckFromOfflineCode(legacyCode)
    assertNotNull(decoded)
    assertEquals("Hydraulics Formulas", decoded!!.title)
    assertEquals("HPGE", decoded.subjectTag)
    assertEquals(1, decoded.cards.size)
    assertEquals("Manning Equation", decoded.cards[0].question)
    assertEquals("V = (1/n) R^(2/3) S^(1/2)", decoded.cards[0].answer)
  }
}

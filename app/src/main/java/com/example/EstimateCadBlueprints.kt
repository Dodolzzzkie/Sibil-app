package com.example

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

// ============================================================================
// CAD SCHEMATICS & LIVE DYNAMIC BLUEPRINT PREVIEWS FOR ESTIMATE MODULE
// ============================================================================

internal data class EstimateBlueprintSpecs(
  val trade: EstimateTrade,
  val primaryDimLabel: String = "",
  val secondaryDimLabel: String = "",
  val tertiaryDimLabel: String = "",
  val badgeNote: String = ""
)

@Composable
internal fun EstimateCadThumbnail(
  trade: EstimateTrade,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  val bgColor = if (isDarkMode) Color(0xFF0B1628).copy(alpha = 0.86f) else Color(0xFFF0F7FF).copy(alpha = 0.92f)
  val borderColor = if (isDarkMode) Color(0xFF38BDF8).copy(alpha = 0.28f) else Color(0xFF0284C7).copy(alpha = 0.24f)
  val lineMain = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
  val lineAccent = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFD97706)
  val lineEmerald = if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669)
  val inkMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)
  val gridColor = lineMain.copy(alpha = if (isDarkMode) 0.12f else 0.10f)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(96.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(14.dp))
      .padding(4.dp)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawCadBackgroundGrid(gridColor)
      when (trade) {
        EstimateTrade.CHB_MASONRY -> drawChbCadSchematic(lineMain, lineAccent, inkMuted, null, null, null)
        EstimateTrade.CONCRETE_WORKS -> drawConcreteFootingColumnCad(lineMain, lineAccent, lineEmerald, inkMuted, null, null, null)
        EstimateTrade.REINFORCING_STEEL -> drawRebarTieCad(lineMain, lineAccent, lineEmerald, inkMuted, null, null)
        EstimateTrade.FORMWORKS_LUMBER -> drawFormworksCad(lineMain, lineAccent, inkMuted, null, null)
        EstimateTrade.ROOFING_PURLINS -> drawRoofingTrussCad(lineMain, lineAccent, lineEmerald, inkMuted, null, null)
        EstimateTrade.TILEWORKS -> drawIsometricTileCad(lineMain, lineAccent, lineEmerald, inkMuted, null, null)
        EstimateTrade.PAINTING_WORKS -> drawPaintingElevationCad(lineMain, lineAccent, lineEmerald, inkMuted, null, null)
        EstimateTrade.EARTHWORKS -> drawEarthworksTrenchCad(lineMain, lineAccent, lineEmerald, inkMuted, null, null, null)
      }
    }
  }
}

@Composable
internal fun EstimateDynamicBlueprintPreview(
  specs: EstimateBlueprintSpecs,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier,
  heightDp: Int = 142
) {
  val density = LocalDensity.current
  val labelPx = with(density) { 10.5f * density.density }
  val bgColor = if (isDarkMode) Color(0xFF091426).copy(alpha = 0.92f) else Color(0xFFEFF6FF)
  val borderColor = if (isDarkMode) Color(0xFF38BDF8).copy(alpha = 0.38f) else Color(0xFF0284C7).copy(alpha = 0.32f)
  val lineMain = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
  val lineAccent = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFD97706)
  val lineEmerald = if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669)
  val inkMuted = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155)
  val gridColor = lineMain.copy(alpha = if (isDarkMode) 0.14f else 0.11f)

  val textPaint = remember(inkMuted, labelPx) {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = inkMuted.toArgb()
      textSize = labelPx
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
  }
  val accentPaint = remember(lineAccent, labelPx) {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = lineAccent.toArgb()
      textSize = labelPx
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(heightDp.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(bgColor)
      .border(1.2.dp, borderColor, RoundedCornerShape(18.dp))
      .padding(8.dp)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawCadBackgroundGrid(gridColor)
      when (specs.trade) {
        EstimateTrade.CHB_MASONRY -> drawChbCadSchematic(
          lineMain, lineAccent, inkMuted, textPaint, specs.primaryDimLabel, specs.secondaryDimLabel
        )
        EstimateTrade.CONCRETE_WORKS -> drawConcreteFootingColumnCad(
          lineMain, lineAccent, lineEmerald, inkMuted, accentPaint, specs.primaryDimLabel, specs.secondaryDimLabel
        )
        EstimateTrade.REINFORCING_STEEL -> drawRebarTieCad(
          lineMain, lineAccent, lineEmerald, inkMuted, textPaint, specs.primaryDimLabel
        )
        EstimateTrade.FORMWORKS_LUMBER -> drawFormworksCad(
          lineMain, lineAccent, inkMuted, textPaint, specs.primaryDimLabel
        )
        EstimateTrade.ROOFING_PURLINS -> drawRoofingTrussCad(
          lineMain, lineAccent, lineEmerald, inkMuted, textPaint, specs.primaryDimLabel
        )
        EstimateTrade.TILEWORKS -> drawIsometricTileCad(
          lineMain, lineAccent, lineEmerald, inkMuted, textPaint, specs.primaryDimLabel
        )
        EstimateTrade.PAINTING_WORKS -> drawPaintingElevationCad(
          lineMain, lineAccent, lineEmerald, inkMuted, textPaint, specs.primaryDimLabel
        )
        EstimateTrade.EARTHWORKS -> drawEarthworksTrenchCad(
          lineMain, lineAccent, lineEmerald, inkMuted, textPaint, specs.primaryDimLabel, specs.secondaryDimLabel
        )
      }

      if (specs.badgeNote.isNotBlank()) {
        drawContext.canvas.nativeCanvas.drawText(
          specs.badgeNote,
          size.width * 0.5f,
          13f * density.density,
          accentPaint
        )
      }
    }
  }
}

private fun DrawScope.drawCadBackgroundGrid(gridColor: Color) {
  val step = 18.dp.toPx()
  var x = step
  while (x < size.width) {
    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.8f)
    x += step
  }
  var y = step
  while (y < size.height) {
    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.8f)
    y += step
  }
}

private fun DrawScope.drawChbCadSchematic(
  main: Color,
  accent: Color,
  muted: Color,
  labelPaint: Paint?,
  dimBottom: String?,
  dimSide: String?
) {
  val w = size.width
  val h = size.height
  val padTop = if (labelPaint != null) h * 0.19f else h * 0.14f
  val padBottom = if (labelPaint != null) h * 0.22f else h * 0.14f
  val wallLeft = w * 0.15f
  val wallRight = w * 0.85f
  val wallTop = padTop
  val wallBottom = h - padBottom
  val wallW = wallRight - wallLeft
  val wallH = wallBottom - wallTop

  // Wall outline + 3 courses of staggered 20x40cm CHB blocks
  drawRect(
    color = main.copy(alpha = 0.10f),
    topLeft = Offset(wallLeft, wallTop),
    size = Size(wallW, wallH)
  )
  drawRect(
    color = main,
    topLeft = Offset(wallLeft, wallTop),
    size = Size(wallW, wallH),
    style = Stroke(width = 2f)
  )

  val courses = 3
  val rowH = wallH / courses
  for (r in 1 until courses) {
    val y = wallTop + r * rowH
    drawLine(main.copy(alpha = 0.78f), Offset(wallLeft, y), Offset(wallRight, y), strokeWidth = 1.6f)
  }
  val cols = 4
  val colW = wallW / cols
  for (r in 0 until courses) {
    val y0 = wallTop + r * rowH
    val offsetX = if (r % 2 == 1) colW * 0.5f else 0f
    for (c in 0..cols) {
      val vx = wallLeft + offsetX + c * colW
      if (vx > wallLeft + 3f && vx < wallRight - 3f) {
        drawLine(main.copy(alpha = 0.72f), Offset(vx, y0), Offset(vx, y0 + rowH), strokeWidth = 1.4f)
      }
    }
    // Draw 3-core hollow cell motif inside middle block
    val cellBoxLeft = wallLeft + colW + 4f
    if (r == 1 && colW > 22f) {
      val coreW = (colW - 16f) / 3f
      for (k in 0 until 3) {
        drawRect(
          color = accent.copy(alpha = 0.65f),
          topLeft = Offset(cellBoxLeft + k * (coreW + 4f), y0 + rowH * 0.24f),
          size = Size(coreW.coerceAtLeast(3f), rowH * 0.52f),
          style = Stroke(width = 1.2f)
        )
      }
    }
  }

  // Vertical & horizontal dashed rebar overlay inside CHB wall
  val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
  drawLine(
    color = accent,
    start = Offset(wallLeft + wallW * 0.25f, wallTop - 4f),
    end = Offset(wallLeft + wallW * 0.25f, wallBottom + 4f),
    strokeWidth = 1.5f,
    pathEffect = dash
  )
  drawLine(
    color = accent,
    start = Offset(wallLeft + wallW * 0.75f, wallTop - 4f),
    end = Offset(wallLeft + wallW * 0.75f, wallBottom + 4f),
    strokeWidth = 1.5f,
    pathEffect = dash
  )

  // Dimension lines
  if (labelPaint != null) {
    val dimY = wallBottom + (h - wallBottom) * 0.45f
    drawLine(muted, Offset(wallLeft, dimY), Offset(wallRight, dimY), 1.3f)
    drawLine(muted, Offset(wallLeft, dimY - 4f), Offset(wallLeft, dimY + 4f), 1.3f)
    drawLine(muted, Offset(wallRight, dimY - 4f), Offset(wallRight, dimY + 4f), 1.3f)
    if (!dimBottom.isNullOrBlank()) {
      drawContext.canvas.nativeCanvas.drawText(dimBottom, w * 0.5f, h - 3f, labelPaint)
    }
    if (!dimSide.isNullOrBlank()) {
      drawContext.canvas.nativeCanvas.drawText(dimSide, wallRight + (w - wallRight) * 0.52f, (wallTop + wallBottom) * 0.52f, labelPaint)
    }
  }
}

private fun DrawScope.drawConcreteFootingColumnCad(
  main: Color,
  accent: Color,
  emerald: Color,
  muted: Color,
  labelPaint: Paint?,
  dimBottom: String?,
  dimSide: String?
) {
  val w = size.width
  val h = size.height
  val padTop = if (labelPaint != null) h * 0.18f else h * 0.12f
  val baseBottom = h - (if (labelPaint != null) h * 0.20f else h * 0.12f)
  val footingW = w * 0.52f
  val footingH = (baseBottom - padTop) * 0.34f
  val footingLeft = (w - footingW) * 0.5f
  val footingTop = baseBottom - footingH
  val colW = footingW * 0.32f
  val colLeft = (w - colW) * 0.5f

  // Column + Footing concrete fill
  drawRect(main.copy(alpha = 0.16f), Offset(colLeft, padTop), Size(colW, footingTop - padTop))
  drawRect(main.copy(alpha = 0.20f), Offset(footingLeft, footingTop), Size(footingW, footingH))
  drawRect(main, Offset(colLeft, padTop), Size(colW, footingTop - padTop), style = Stroke(2f))
  drawRect(main, Offset(footingLeft, footingTop), Size(footingW, footingH), style = Stroke(2f))

  // Gravel bedding strip below footing
  drawRect(
    color = emerald.copy(alpha = 0.55f),
    topLeft = Offset(footingLeft - 6f, baseBottom),
    size = Size(footingW + 12f, 5f),
    style = Stroke(1.3f)
  )

  // Internal column dowel hooks
  val dowelOffset = colW * 0.22f
  val hookY = baseBottom - 6f
  drawLine(accent, Offset(colLeft + dowelOffset, padTop + 4f), Offset(colLeft + dowelOffset, hookY), 1.8f)
  drawLine(accent, Offset(colLeft + dowelOffset, hookY), Offset(footingLeft + 8f, hookY), 1.8f)
  drawLine(accent, Offset(colLeft + colW - dowelOffset, padTop + 4f), Offset(colLeft + colW - dowelOffset, hookY), 1.8f)
  drawLine(accent, Offset(colLeft + colW - dowelOffset, hookY), Offset(footingLeft + footingW - 8f, hookY), 1.8f)

  if (labelPaint != null) {
    if (!dimBottom.isNullOrBlank()) {
      drawContext.canvas.nativeCanvas.drawText(dimBottom, w * 0.5f, h - 3f, labelPaint)
    }
    if (!dimSide.isNullOrBlank()) {
      drawContext.canvas.nativeCanvas.drawText(dimSide, footingLeft + footingW + (w - footingLeft - footingW) * 0.52f, (footingTop + baseBottom) * 0.55f, labelPaint)
    }
  }
}

private fun DrawScope.drawRebarTieCad(
  main: Color,
  accent: Color,
  emerald: Color,
  muted: Color,
  labelPaint: Paint?,
  dimBottom: String?
) {
  val w = size.width
  val h = size.height
  val padTop = if (labelPaint != null) h * 0.18f else h * 0.12f
  val padBottom = if (labelPaint != null) h * 0.20f else h * 0.12f
  val midH = h - padTop - padBottom

  // Two ribbed deformed bars on left + square stirrup section on right
  val barLeft = w * 0.10f
  val barRight = w * 0.54f
  val y1 = padTop + midH * 0.30f
  val y2 = padTop + midH * 0.72f
  drawLine(main, Offset(barLeft, y1), Offset(barRight, y1), 3.4f, cap = StrokeCap.Round)
  drawLine(main, Offset(barLeft, y2), Offset(barRight, y2), 3.4f, cap = StrokeCap.Round)
  var rx = barLeft + 8f
  while (rx < barRight - 6f) {
    drawLine(accent, Offset(rx - 2f, y1 - 4f), Offset(rx + 2f, y1 + 4f), 1.3f)
    drawLine(accent, Offset(rx - 2f, y2 - 4f), Offset(rx + 2f, y2 + 4f), 1.3f)
    rx += 12f
  }

  // Stirrup cross-section with 135-deg seismic hook
  val boxSize = midH * 0.82f
  val sLeft = w * 0.65f
  val sTop = padTop + (midH - boxSize) * 0.5f
  drawRoundRect(
    color = emerald,
    topLeft = Offset(sLeft, sTop),
    size = Size(boxSize, boxSize),
    cornerRadius = CornerRadius(6f, 6f),
    style = Stroke(2.2f)
  )
  // 4 corner main bar dots + #16 GI tie cross marks
  val rDot = 3.2f
  val inset = 6f
  val corners = listOf(
    Offset(sLeft + inset, sTop + inset),
    Offset(sLeft + boxSize - inset, sTop + inset),
    Offset(sLeft + inset, sTop + boxSize - inset),
    Offset(sLeft + boxSize - inset, sTop + boxSize - inset)
  )
  corners.forEach { pt ->
    drawCircle(accent, rDot, pt)
  }
  // 135-degree hook at top-left
  drawLine(emerald, Offset(sLeft + inset, sTop + inset), Offset(sLeft + inset + 9f, sTop + inset + 9f), 1.8f)

  if (labelPaint != null && !dimBottom.isNullOrBlank()) {
    drawContext.canvas.nativeCanvas.drawText(dimBottom, w * 0.5f, h - 3f, labelPaint)
  }
}

private fun DrawScope.drawFormworksCad(
  main: Color,
  accent: Color,
  muted: Color,
  labelPaint: Paint?,
  dimBottom: String?
) {
  val w = size.width
  val h = size.height
  val padTop = if (labelPaint != null) h * 0.18f else h * 0.12f
  val padBottom = if (labelPaint != null) h * 0.22f else h * 0.12f
  val sLeft = w * 0.18f
  val sRight = w * 0.82f
  val sTop = padTop
  val sBottom = h - padBottom
  val sW = sRight - sLeft
  val sH = sBottom - sTop

  // 4'x8' Plywood sheet
  drawRect(main.copy(alpha = 0.14f), Offset(sLeft, sTop), Size(sW, sH))
  drawRect(main, Offset(sLeft, sTop), Size(sW, sH), style = Stroke(2f))

  // Framing vertical studs & horizontal walers
  val studPositions = listOf(0.18f, 0.50f, 0.82f)
  studPositions.forEach { frac ->
    val x = sLeft + sW * frac
    drawRect(accent.copy(alpha = 0.72f), Offset(x - 3f, sTop), Size(6f, sH), style = Stroke(1.3f))
  }
  drawLine(accent, Offset(sLeft, sTop + sH * 0.30f), Offset(sRight, sTop + sH * 0.30f), 1.8f)
  drawLine(accent, Offset(sLeft, sTop + sH * 0.70f), Offset(sRight, sTop + sH * 0.70f), 1.8f)

  if (labelPaint != null && !dimBottom.isNullOrBlank()) {
    drawContext.canvas.nativeCanvas.drawText(dimBottom, w * 0.5f, h - 3f, labelPaint)
  }
}

private fun DrawScope.drawRoofingTrussCad(
  main: Color,
  accent: Color,
  emerald: Color,
  muted: Color,
  labelPaint: Paint?,
  dimBottom: String?
) {
  val w = size.width
  val h = size.height
  val baseTop = if (labelPaint != null) h * 0.20f else h * 0.16f
  val eaveY = h - (if (labelPaint != null) h * 0.24f else h * 0.18f)
  val leftX = w * 0.14f
  val rightX = w * 0.86f
  val midX = w * 0.5f
  val apexY = baseTop

  // Triangular roof truss chords
  val trussPath = Path().apply {
    moveTo(leftX, eaveY)
    lineTo(midX, apexY)
    lineTo(rightX, eaveY)
    close()
  }
  drawPath(trussPath, main.copy(alpha = 0.10f))
  drawPath(trussPath, main, style = Stroke(2f, join = StrokeJoin.Round))

  // Web struts
  drawLine(main.copy(alpha = 0.8f), Offset(midX, apexY), Offset(midX, eaveY), 1.5f)
  drawLine(main.copy(alpha = 0.8f), Offset((leftX + midX) * 0.5f, (eaveY + apexY) * 0.5f), Offset(midX, eaveY), 1.4f)
  drawLine(main.copy(alpha = 0.8f), Offset((rightX + midX) * 0.5f, (eaveY + apexY) * 0.5f), Offset(midX, eaveY), 1.4f)

  // Corrugated / Rib profile along top slope & C-purlin squares
  listOf(0.25f, 0.50f, 0.75f).forEach { t ->
    val pxL = leftX + (midX - leftX) * t
    val pyL = eaveY + (apexY - eaveY) * t
    drawCircle(accent, 2.5f, Offset(pxL, pyL - 4f))
    val pxR = rightX + (midX - rightX) * t
    val pyR = eaveY + (apexY - eaveY) * t
    drawCircle(accent, 2.5f, Offset(pxR, pyR - 4f))
  }
  // Ridge cap dot
  drawCircle(emerald, 4f, Offset(midX, apexY - 3f))

  if (labelPaint != null && !dimBottom.isNullOrBlank()) {
    drawContext.canvas.nativeCanvas.drawText(dimBottom, w * 0.5f, h - 3f, labelPaint)
  }
}

private fun DrawScope.drawIsometricTileCad(
  main: Color,
  accent: Color,
  emerald: Color,
  muted: Color,
  labelPaint: Paint?,
  dimBottom: String?
) {
  val w = size.width
  val h = size.height
  val padTop = if (labelPaint != null) h * 0.18f else h * 0.12f
  val padBottom = if (labelPaint != null) h * 0.22f else h * 0.12f
  val cx = w * 0.5f
  val cy = padTop + (h - padTop - padBottom) * 0.5f
  val rx = w * 0.34f
  val ry = (h - padTop - padBottom) * 0.44f

  val pTop = Offset(cx, cy - ry)
  val pRight = Offset(cx + rx, cy)
  val pBottom = Offset(cx, cy + ry)
  val pLeft = Offset(cx - rx, cy)

  val diamond = Path().apply {
    moveTo(pTop.x, pTop.y)
    lineTo(pRight.x, pRight.y)
    lineTo(pBottom.x, pBottom.y)
    lineTo(pLeft.x, pLeft.y)
    close()
  }
  drawPath(diamond, main.copy(alpha = 0.14f))
  drawPath(diamond, main, style = Stroke(2f))

  // 3x3 isometric grid subdivisions
  for (k in 1..2) {
    val t = k / 3f
    val a1 = Offset(pTop.x + (pLeft.x - pTop.x) * t, pTop.y + (pLeft.y - pTop.y) * t)
    val a2 = Offset(pRight.x + (pBottom.x - pRight.x) * t, pRight.y + (pBottom.y - pRight.y) * t)
    drawLine(main.copy(alpha = 0.78f), a1, a2, 1.3f)

    val b1 = Offset(pTop.x + (pRight.x - pTop.x) * t, pTop.y + (pRight.y - pTop.y) * t)
    val b2 = Offset(pLeft.x + (pBottom.x - pLeft.x) * t, pLeft.y + (pBottom.y - pLeft.y) * t)
    drawLine(main.copy(alpha = 0.78f), b1, b2, 1.3f)
  }
  // Spacer crosses
  listOf(Offset(cx, cy - ry * 0.33f), Offset(cx, cy + ry * 0.33f)).forEach { pt ->
    drawLine(accent, Offset(pt.x - 4f, pt.y), Offset(pt.x + 4f, pt.y), 1.6f)
    drawLine(accent, Offset(pt.x, pt.y - 3f), Offset(pt.x, pt.y + 3f), 1.6f)
  }

  if (labelPaint != null && !dimBottom.isNullOrBlank()) {
    drawContext.canvas.nativeCanvas.drawText(dimBottom, w * 0.5f, h - 3f, labelPaint)
  }
}

private fun DrawScope.drawPaintingElevationCad(
  main: Color,
  accent: Color,
  emerald: Color,
  muted: Color,
  labelPaint: Paint?,
  dimBottom: String?
) {
  val w = size.width
  val h = size.height
  val padTop = if (labelPaint != null) h * 0.18f else h * 0.12f
  val padBottom = if (labelPaint != null) h * 0.22f else h * 0.12f
  val wallLeft = w * 0.16f
  val wallRight = w * 0.84f
  val wallTop = padTop
  val wallBottom = h - padBottom
  val wallW = wallRight - wallLeft
  val wallH = wallBottom - wallTop

  drawRect(main.copy(alpha = 0.10f), Offset(wallLeft, wallTop), Size(wallW, wallH))
  drawRect(main, Offset(wallLeft, wallTop), Size(wallW, wallH), style = Stroke(2f))

  // Painted band + roller head + handle
  val bandW = wallW * 0.24f
  val rollerX = wallLeft + wallW * 0.32f
  drawRect(
    color = emerald.copy(alpha = 0.26f),
    topLeft = Offset(rollerX - bandW * 0.5f, wallTop + 8f),
    size = Size(bandW, wallH * 0.62f)
  )
  drawRoundRect(
    color = accent,
    topLeft = Offset(rollerX - bandW * 0.52f, wallTop + 10f),
    size = Size(bandW * 1.04f, 10f),
    cornerRadius = CornerRadius(3f, 3f)
  )
  drawLine(muted, Offset(rollerX + bandW * 0.52f, wallTop + 15f), Offset(rollerX + bandW * 0.72f, wallTop + 15f), 2f)
  drawLine(muted, Offset(rollerX + bandW * 0.72f, wallTop + 15f), Offset(rollerX + bandW * 0.72f, wallTop + 30f), 2f)
  drawLine(muted, Offset(rollerX + bandW * 0.72f, wallTop + 30f), Offset(rollerX, wallTop + 34f), 2f)
  drawLine(muted, Offset(rollerX, wallTop + 34f), Offset(rollerX, wallBottom - 6f), 2.4f, cap = StrokeCap.Round)

  // Window cutout dashed outline on right half
  drawRect(
    color = accent.copy(alpha = 0.7f),
    topLeft = Offset(wallLeft + wallW * 0.62f, wallTop + wallH * 0.24f),
    size = Size(wallW * 0.24f, wallH * 0.44f),
    style = Stroke(1.3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
  )

  if (labelPaint != null && !dimBottom.isNullOrBlank()) {
    drawContext.canvas.nativeCanvas.drawText(dimBottom, w * 0.5f, h - 3f, labelPaint)
  }
}

private fun DrawScope.drawEarthworksTrenchCad(
  main: Color,
  accent: Color,
  emerald: Color,
  muted: Color,
  labelPaint: Paint?,
  dimBottom: String?,
  dimSide: String?
) {
  val w = size.width
  val h = size.height
  val padTop = if (labelPaint != null) h * 0.22f else h * 0.18f
  val padBottom = if (labelPaint != null) h * 0.22f else h * 0.14f
  val groundY = padTop + (h - padTop - padBottom) * 0.22f
  val trenchBottom = h - padBottom
  val topLeftX = w * 0.24f
  val topRightX = w * 0.76f
  val botLeftX = w * 0.34f
  val botRightX = w * 0.66f

  // Natural ground line left & right
  drawLine(emerald, Offset(w * 0.08f, groundY), Offset(topLeftX, groundY), 2.2f)
  drawLine(emerald, Offset(topRightX, groundY), Offset(w * 0.92f, groundY), 2.2f)

  // Trapezoidal trench profile
  val trenchPath = Path().apply {
    moveTo(topLeftX, groundY)
    lineTo(botLeftX, trenchBottom)
    lineTo(botRightX, trenchBottom)
    lineTo(topRightX, groundY)
  }
  drawPath(trenchPath, main, style = Stroke(2.2f, join = StrokeJoin.Round))

  // G-1 Gravel bedding strip at bottom of pit
  val bedH = 8f
  drawRect(
    color = accent.copy(alpha = 0.28f),
    topLeft = Offset(botLeftX + 1f, trenchBottom - bedH),
    size = Size(botRightX - botLeftX - 2f, bedH)
  )
  drawRect(
    color = accent,
    topLeft = Offset(botLeftX + 1f, trenchBottom - bedH),
    size = Size(botRightX - botLeftX - 2f, bedH),
    style = Stroke(1.4f)
  )

  if (labelPaint != null) {
    if (!dimBottom.isNullOrBlank()) {
      drawContext.canvas.nativeCanvas.drawText(dimBottom, w * 0.5f, h - 3f, labelPaint)
    }
    if (!dimSide.isNullOrBlank()) {
      drawContext.canvas.nativeCanvas.drawText(dimSide, w * 0.84f, (groundY + trenchBottom) * 0.55f, labelPaint)
    }
  }
}

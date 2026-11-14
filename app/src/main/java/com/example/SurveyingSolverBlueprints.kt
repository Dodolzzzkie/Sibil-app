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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

// ============================================================================
// INTERACTIVE CAD BLUEPRINTS (210.dp LIVE CANVASES FOR ALL 6 SURVEYING TOOLS)
// ============================================================================

internal data class SurveyingBlueprintState(
  val activeTab: SurveyingToolTab,
  val traverseResult: TraverseComputationResult? = null,
  val simpleCurveResult: SimpleCurveResult? = null,
  val compoundCurveResult: CompoundCurveResult? = null,
  val verticalCurveResult: VerticalCurveResult? = null,
  val stadiaResult: StadiaLevelingResult? = null,
  val earthworkResult: EarthworkVolumeResult? = null
)

@Composable
internal fun SurveyingCadBlueprintCanvas(
  state: SurveyingBlueprintState,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  val density = LocalDensity.current
  val labelPx = with(density) { 10.5f * density.density }
  val titlePx = with(density) { 11.5f * density.density }

  val bgColor = if (isDarkMode) Color(0xFF091426).copy(alpha = 0.92f) else Color(0xFFEFF6FF)
  val borderColor = if (isDarkMode) Color(0xFF38BDF8).copy(alpha = 0.36f) else Color(0xFF0284C7).copy(alpha = 0.28f)
  val lineMain = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
  val lineAccent = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFD97706)
  val lineEmerald = if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669)
  val lineRose = if (isDarkMode) Color(0xFFFB7185) else Color(0xFFE11D48)
  val inkMuted = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155)
  val gridColor = lineMain.copy(alpha = if (isDarkMode) 0.14f else 0.11f)

  val textPaint = remember(inkMuted, labelPx) {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = inkMuted.toArgb()
      textSize = labelPx
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
  }
  val accentPaint = remember(lineAccent, titlePx) {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = lineAccent.toArgb()
      textSize = titlePx
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
  }
  val emeraldPaint = remember(lineEmerald, labelPx) {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = lineEmerald.toArgb()
      textSize = labelPx
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
  }
  val mainPaint = remember(lineMain, labelPx) {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = lineMain.toArgb()
      textSize = labelPx
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(210.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(18.dp))
      .padding(6.dp)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawSurveyCadGrid(gridColor)

      when (state.activeTab) {
        SurveyingToolTab.TRAVERSE_DMD -> drawTraversePolygonCad(
          result = state.traverseResult,
          lineMain = lineMain,
          lineAccent = lineAccent,
          lineEmerald = lineEmerald,
          inkMuted = inkMuted,
          textPaint = textPaint,
          accentPaint = accentPaint,
          emeraldPaint = emeraldPaint
        )
        SurveyingToolTab.SIMPLE_CURVE -> drawSimpleHorizontalCurveCad(
          result = state.simpleCurveResult,
          lineMain = lineMain,
          lineAccent = lineAccent,
          lineEmerald = lineEmerald,
          inkMuted = inkMuted,
          textPaint = textPaint,
          accentPaint = accentPaint,
          emeraldPaint = emeraldPaint
        )
        SurveyingToolTab.COMPOUND_CURVE -> drawCompoundCurveCad(
          result = state.compoundCurveResult,
          lineMain = lineMain,
          lineAccent = lineAccent,
          lineEmerald = lineEmerald,
          inkMuted = inkMuted,
          textPaint = textPaint,
          accentPaint = accentPaint,
          emeraldPaint = emeraldPaint
        )
        SurveyingToolTab.VERTICAL_CURVE -> drawVerticalParabolicCurveCad(
          result = state.verticalCurveResult,
          lineMain = lineMain,
          lineAccent = lineAccent,
          lineEmerald = lineEmerald,
          lineRose = lineRose,
          inkMuted = inkMuted,
          textPaint = textPaint,
          accentPaint = accentPaint,
          emeraldPaint = emeraldPaint
        )
        SurveyingToolTab.STADIA_LEVELING -> drawStadiaLevelingCad(
          result = state.stadiaResult,
          lineMain = lineMain,
          lineAccent = lineAccent,
          lineEmerald = lineEmerald,
          inkMuted = inkMuted,
          textPaint = textPaint,
          accentPaint = accentPaint,
          mainPaint = mainPaint
        )
        SurveyingToolTab.EARTHWORK_VOLUMES -> drawEarthworkPrismoidalCad(
          result = state.earthworkResult,
          lineMain = lineMain,
          lineAccent = lineAccent,
          lineEmerald = lineEmerald,
          inkMuted = inkMuted,
          textPaint = textPaint,
          accentPaint = accentPaint,
          emeraldPaint = emeraldPaint
        )
      }
    }
  }
}

private fun DrawScope.drawSurveyCadGrid(gridColor: Color) {
  val step = size.height / 7f
  var x = step
  while (x < size.width) {
    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
    x += step
  }
  var y = step
  while (y < size.height) {
    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
    y += step
  }
}

// ----------------------------------------------------------------------------
// 1. CLOSED TRAVERSE POLYGON CAD
// ----------------------------------------------------------------------------

private fun DrawScope.drawTraversePolygonCad(
  result: TraverseComputationResult?,
  lineMain: Color,
  lineAccent: Color,
  lineEmerald: Color,
  inkMuted: Color,
  textPaint: Paint,
  accentPaint: Paint,
  emeraldPaint: Paint
) {
  val w = size.width
  val h = size.height

  // Top-right North Arrow
  val nx = w - 26f
  val nyTop = 18f
  val nyBot = 50f
  drawLine(lineAccent, Offset(nx, nyBot), Offset(nx, nyTop), strokeWidth = 2.4f, cap = StrokeCap.Round)
  val arrowHead = Path().apply {
    moveTo(nx, nyTop - 3f)
    lineTo(nx - 6f, nyTop + 8f)
    lineTo(nx + 6f, nyTop + 8f)
    close()
  }
  drawPath(arrowHead, color = lineAccent)
  drawContext.canvas.nativeCanvas.drawText("N", nx - 4f, nyTop + 24f, accentPaint)

  if (result == null || !result.isValid || result.vertices.size < 3) {
    drawContext.canvas.nativeCanvas.drawText(
      "CLOSED TRAVERSE & DMD • ENTER COURSES BELOW",
      14f,
      h * 0.52f,
      textPaint
    )
    return
  }

  val verts = result.vertices
  val minX = verts.minOf { it.x }
  val maxX = verts.maxOf { it.x }
  val minY = verts.minOf { it.y }
  val maxY = verts.maxOf { it.y }

  val spanX = max(maxX - minX, 1e-3)
  val spanY = max(maxY - minY, 1e-3)

  val padLeft = 44f
  val padRight = 56f
  val padTop = 30f
  val padBot = 34f
  val availW = max(w - padLeft - padRight, 40f)
  val availH = max(h - padTop - padBot, 40f)

  val scale = min(availW / spanX, availH / spanY).toFloat()
  val drawnW = (spanX * scale).toFloat()
  val drawnH = (spanY * scale).toFloat()
  val originX = padLeft + (availW - drawnW) / 2f
  val originY = padTop + (availH - drawnH) / 2f

  fun toCanvas(v: TraverseVertex): Offset {
    val cx = originX + ((v.x - minX) * scale).toFloat()
    val cy = originY + drawnH - ((v.y - minY) * scale).toFloat()
    return Offset(cx, cy)
  }

  val pts = verts.map { toCanvas(it) }
  val polyPath = Path().apply {
    moveTo(pts.first().x, pts.first().y)
    for (i in 1 until pts.size) {
      lineTo(pts[i].x, pts[i].y)
    }
    close()
  }

  drawPath(polyPath, color = lineMain.copy(alpha = 0.14f))
  drawPath(
    polyPath,
    color = lineMain,
    style = Stroke(width = 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
  )

  val centerX = pts.map { it.x }.average().toFloat()
  val centerY = pts.map { it.y }.average().toFloat()

  pts.forEachIndexed { idx, p ->
    drawCircle(color = lineAccent, radius = 6.5f, center = p)
    drawCircle(color = Color(0xFF0F172A), radius = 2.6f, center = p)
    val dx = p.x - centerX
    val dy = p.y - centerY
    val len = max(hypotFloat(dx, dy), 1f)
    val lx = p.x + (dx / len) * 14f - 4f
    val ly = p.y + (dy / len) * 14f + 4f
    drawContext.canvas.nativeCanvas.drawText(verts[idx].label, lx, ly, accentPaint)
  }

  // Footer HUD inside canvas
  drawContext.canvas.nativeCanvas.drawText(
    "A = ${fmtNum(result.areaSqm, 2)} m² (${fmtNum(result.areaHectares, 4)} ha)",
    14f,
    h - 10f,
    emeraldPaint
  )
  drawContext.canvas.nativeCanvas.drawText(
    "RP ${result.relativePrecisionText}",
    w - 150f,
    h - 10f,
    textPaint
  )
  drawLine(
    inkMuted.copy(alpha = 0.25f),
    Offset(12f, h - 24f),
    Offset(w - 12f, h - 24f),
    strokeWidth = 1f
  )
}

// ----------------------------------------------------------------------------
// 2. SIMPLE CIRCULAR HORIZONTAL CURVE CAD
// ----------------------------------------------------------------------------

private fun DrawScope.drawSimpleHorizontalCurveCad(
  result: SimpleCurveResult?,
  lineMain: Color,
  lineAccent: Color,
  lineEmerald: Color,
  inkMuted: Color,
  textPaint: Paint,
  accentPaint: Paint,
  emeraldPaint: Paint
) {
  val w = size.width
  val h = size.height
  val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)

  val iDeg = (result?.takeIf { it.isValid }?.angleI ?: 56.0).coerceIn(14.0, 145.0)
  val iRad = iDeg * PI / 180.0
  val halfI = (iRad / 2.0).toFloat()

  // Center O near bottom center, symmetric upward curve
  val rPx = min(w * 0.34f / sin(halfI).coerceAtLeast(0.18f), h * 0.78f).coerceIn(72f, 210f)
  val chordHalfW = rPx * sin(halfI)
  val arcRise = rPx * (1f - cos(halfI))
  val extRise = rPx * ((1f / cos(halfI)) - 1f)
  val totalApex = min(arcRise + extRise, h * 0.56f)

  val centerO = Offset(w * 0.50f, h * 0.86f + (rPx - arcRise).coerceAtLeast(0f) * 0.25f)
  val pc = Offset(centerO.x - chordHalfW, centerO.y - rPx * cos(halfI))
  val pt = Offset(centerO.x + chordHalfW, centerO.y - rPx * cos(halfI))
  val pi = Offset(centerO.x, pc.y - min(chordHalfW * tan(halfI), totalApex)).let {
    Offset(it.x, it.y.coerceAtLeast(24f))
  }

  // Radii O-PC and O-PT (clipped near bottom if large R)
  val oDraw = Offset(centerO.x, min(centerO.y, h - 18f))
  drawLine(inkMuted.copy(alpha = 0.50f), oDraw, pc, strokeWidth = 1.8f, pathEffect = dash)
  drawLine(inkMuted.copy(alpha = 0.50f), oDraw, pt, strokeWidth = 1.8f, pathEffect = dash)
  drawLine(inkMuted.copy(alpha = 0.42f), oDraw, pi, strokeWidth = 1.4f, pathEffect = dash)

  // Tangents PC-PI and PI-PT
  drawLine(lineAccent, pc, pi, strokeWidth = 2.4f, cap = StrokeCap.Round)
  drawLine(lineAccent, pi, pt, strokeWidth = 2.4f, cap = StrokeCap.Round)

  // Long Chord PC-PT
  drawLine(lineEmerald, pc, pt, strokeWidth = 2.0f, pathEffect = dash)

  // Circular Curve Arc PC -> PT via quadratic approximation matched to sagitta
  val midArcY = pc.y - arcRise.coerceAtMost((pc.y - pi.y) * 0.72f)
  val midArc = Offset(centerO.x, midArcY)
  val ctrlY = 2f * midArcY - pc.y
  val arcPath = Path().apply {
    moveTo(pc.x, pc.y)
    quadraticBezierTo(centerO.x, ctrlY, pt.x, pt.y)
  }
  drawPath(arcPath, color = lineMain, style = Stroke(width = 4.0f, cap = StrokeCap.Round))

  // External distance E (pi -> midArc) & Middle ordinate M (midArc -> midChord)
  val midChord = Offset(centerO.x, pc.y)
  drawLine(lineAccent, pi, midArc, strokeWidth = 2.4f)
  drawLine(lineEmerald, midArc, midChord, strokeWidth = 2.4f)

  // Key nodes
  listOf(pc to "PC", pt to "PT", pi to "PI").forEach { (pos, lbl) ->
    drawCircle(lineMain, radius = 5.5f, center = pos)
    drawCircle(Color(0xFF0F172A), radius = 2.2f, center = pos)
    val dx = when (lbl) {
      "PC" -> -22f
      "PT" -> 8f
      else -> 8f
    }
    val dy = if (lbl == "PI") -4f else 15f
    drawContext.canvas.nativeCanvas.drawText(lbl, pos.x + dx, pos.y + dy, accentPaint)
  }
  drawCircle(inkMuted, radius = 4f, center = oDraw)
  drawContext.canvas.nativeCanvas.drawText("O", oDraw.x + 7f, oDraw.y + 4f, textPaint)

  // Dimension Callouts
  val rLbl = if (result != null && result.isValid) "R=${fmtNum(result.radius, 2)}m" else "R"
  val iLbl = if (result != null && result.isValid) "I=${fmtNum(result.angleI, 2)}°" else "I"
  val eLbl = if (result != null && result.isValid) "E=${fmtNum(result.externalE, 2)}m" else "E"
  val mLbl = if (result != null && result.isValid) "M=${fmtNum(result.middleOrdinateM, 2)}m" else "M"
  val lcLbl = if (result != null && result.isValid) "LC=${fmtNum(result.longChordLc, 2)}m" else "LC"

  drawContext.canvas.nativeCanvas.drawText(rLbl, 14f, 22f, textPaint)
  drawContext.canvas.nativeCanvas.drawText(iLbl, 14f, 38f, accentPaint)
  drawContext.canvas.nativeCanvas.drawText(eLbl, pi.x + 10f, (pi.y + midArc.y) * 0.5f + 4f, accentPaint)
  drawContext.canvas.nativeCanvas.drawText(mLbl, midArc.x + 8f, (midArc.y + midChord.y) * 0.5f + 4f, emeraldPaint)
  drawContext.canvas.nativeCanvas.drawText(lcLbl, midChord.x - 38f, midChord.y + 15f, emeraldPaint)
}

// ----------------------------------------------------------------------------
// 3. COMPOUND CURVE CAD
// ----------------------------------------------------------------------------

private fun DrawScope.drawCompoundCurveCad(
  result: CompoundCurveResult?,
  lineMain: Color,
  lineAccent: Color,
  lineEmerald: Color,
  inkMuted: Color,
  textPaint: Paint,
  accentPaint: Paint,
  emeraldPaint: Paint
) {
  val w = size.width
  val h = size.height
  val dash = PathEffect.dashPathEffect(floatArrayOf(9f, 6f), 0f)

  val pc = Offset(w * 0.14f, h * 0.78f)
  val pi = Offset(w * 0.50f, h * 0.18f)
  val pt = Offset(w * 0.87f, h * 0.76f)

  // Proportions driven dynamically by R1/(R1+R2) and I1/(I1+I2)
  val r1Ratio = if (result != null && result.isValid) {
    (result.lc1 / max(result.totalLength, 1.0)).toFloat().coerceIn(0.28f, 0.72f)
  } else 0.54f

  val v1 = Offset(
    pc.x + (pi.x - pc.x) * 0.62f,
    pc.y + (pi.y - pc.y) * 0.62f
  )
  val v2 = Offset(
    pt.x + (pi.x - pt.x) * 0.64f,
    pt.y + (pi.y - pt.y) * 0.64f
  )
  val pcc = Offset(
    v1.x + (v2.x - v1.x) * r1Ratio,
    v1.y + (v2.y - v1.y) * r1Ratio
  )

  // Outer Tangents T1 and T2
  drawLine(lineAccent.copy(alpha = 0.85f), pc, pi, strokeWidth = 2.2f, pathEffect = dash)
  drawLine(lineAccent.copy(alpha = 0.85f), pi, pt, strokeWidth = 2.2f, pathEffect = dash)
  // Common Tangent V1-V2 through PCC
  drawLine(inkMuted.copy(alpha = 0.70f), v1, v2, strokeWidth = 2.0f, pathEffect = dash)

  // Two smooth tangent arcs meeting at PCC (tangent to V1-V2)
  val arc1 = Path().apply {
    moveTo(pc.x, pc.y)
    quadraticBezierTo(v1.x, v1.y, pcc.x, pcc.y)
  }
  val arc2 = Path().apply {
    moveTo(pcc.x, pcc.y)
    quadraticBezierTo(v2.x, v2.y, pt.x, pt.y)
  }
  drawPath(arc1, color = lineMain, style = Stroke(width = 4.2f, cap = StrokeCap.Round))
  drawPath(arc2, color = lineEmerald, style = Stroke(width = 4.2f, cap = StrokeCap.Round))

  // Nodes
  listOf(
    pc to "PC",
    pcc to "PCC",
    pt to "PT",
    pi to "PI"
  ).forEach { (pos, lbl) ->
    val dotColor = if (lbl == "PCC") lineAccent else lineMain
    drawCircle(dotColor, radius = if (lbl == "PCC") 6.2f else 5.0f, center = pos)
    drawCircle(Color(0xFF0F172A), radius = 2.2f, center = pos)
    val dx = when (lbl) {
      "PC" -> -18f
      "PT" -> 8f
      "PCC" -> -12f
      else -> 8f
    }
    val dy = if (lbl == "PI" || lbl == "PCC") -10f else 16f
    drawContext.canvas.nativeCanvas.drawText(lbl, pos.x + dx, pos.y + dy, accentPaint)
  }

  val t1Lbl = if (result != null && result.isValid) "T₁=${fmtNum(result.t1Long, 2)}m" else "T₁"
  val t2Lbl = if (result != null && result.isValid) "T₂=${fmtNum(result.t2Long, 2)}m" else "T₂"
  val tcLbl = if (result != null && result.isValid) "Tc=${fmtNum(result.commonTangentTc, 2)}m" else "Tc"

  drawContext.canvas.nativeCanvas.drawText(t1Lbl, (pc.x + pi.x) * 0.42f - 42f, (pc.y + pi.y) * 0.5f, textPaint)
  drawContext.canvas.nativeCanvas.drawText(t2Lbl, (pi.x + pt.x) * 0.56f + 6f, (pi.y + pt.y) * 0.5f, emeraldPaint)
  drawContext.canvas.nativeCanvas.drawText(tcLbl, pcc.x - 24f, pcc.y + 18f, textPaint)
}

// ----------------------------------------------------------------------------
// 4. SYMMETRICAL VERTICAL PARABOLIC CURVE CAD
// ----------------------------------------------------------------------------

private fun DrawScope.drawVerticalParabolicCurveCad(
  result: VerticalCurveResult?,
  lineMain: Color,
  lineAccent: Color,
  lineEmerald: Color,
  lineRose: Color,
  inkMuted: Color,
  textPaint: Paint,
  accentPaint: Paint,
  emeraldPaint: Paint
) {
  val w = size.width
  val h = size.height
  val dash = PathEffect.dashPathEffect(floatArrayOf(9f, 6f), 0f)

  val g1 = result?.takeIf { it.isValid }?.g1 ?: 3.5
  val g2 = result?.takeIf { it.isValid }?.g2 ?: -2.5
  val lengthL = result?.takeIf { it.isValid }?.lengthL ?: 200.0
  val halfL = lengthL / 2.0

  // Relative elevation model for authentic scaling
  val relPvc = -(g1 / 100.0) * halfL
  val relPvi = 0.0
  val relPvt = (g2 / 100.0) * halfL

  val samples = 40
  val dg = g2 - g1
  val curveRel = (0..samples).map { i ->
    val t = i.toDouble() / samples
    val x = t * lengthL
    relPvc + (g1 / 100.0) * x + (dg / (200.0 * lengthL)) * x * x
  }

  val allY = curveRel + listOf(relPvc, relPvi, relPvt)
  val minY = allY.minOrNull() ?: -1.0
  val maxY = allY.maxOrNull() ?: 1.0
  val spanY = max(maxY - minY, 0.25)

  val leftX = w * 0.11f
  val rightX = w * 0.89f
  val topY = 34f
  val botY = h - 34f

  fun mapPt(t: Float, relElev: Double): Offset {
    val cx = leftX + t * (rightX - leftX)
    val cy = botY - ((relElev - minY) / spanY).toFloat() * (botY - topY)
    return Offset(cx, cy)
  }

  val pvcPt = mapPt(0f, relPvc)
  val pviPt = mapPt(0.5f, relPvi)
  val pvtPt = mapPt(1f, relPvt)

  // Tangents PVC -> PVI -> PVT
  drawLine(lineAccent.copy(alpha = 0.78f), pvcPt, pviPt, strokeWidth = 2.0f, pathEffect = dash)
  drawLine(lineAccent.copy(alpha = 0.78f), pviPt, pvtPt, strokeWidth = 2.0f, pathEffect = dash)

  // Parabolic curve
  val paraPath = Path()
  curveRel.forEachIndexed { idx, elev ->
    val p = mapPt(idx.toFloat() / samples, elev)
    if (idx == 0) paraPath.moveTo(p.x, p.y) else paraPath.lineTo(p.x, p.y)
  }
  drawPath(paraPath, color = lineMain, style = Stroke(width = 4.0f, cap = StrokeCap.Round))

  // Middle vertical offset H between PVI and mid-curve
  val midCurvePt = mapPt(0.5f, curveRel[samples / 2])
  drawLine(lineEmerald, pviPt, midCurvePt, strokeWidth = 2.4f)

  // Key stations
  listOf(pvcPt to "PVC", pviPt to "PVI", pvtPt to "PVT").forEach { (pt, lbl) ->
    drawCircle(lineMain, radius = 4.8f, center = pt)
    val dy = if (lbl == "PVI") (if (g1 > g2) -10f else 16f) else 16f
    drawContext.canvas.nativeCanvas.drawText(lbl, pt.x - 10f, pt.y + dy, textPaint)
  }

  // Glowing Extremum Highlight Pin (Summit / Sag)
  if (result != null && result.isValid && result.hasExtremum) {
    val tExt = (result.xExt / result.lengthL).toFloat()
    val clampedT = tExt.coerceIn(0f, 1f)
    val extRel = relPvc + (g1 / 100.0) * (clampedT * lengthL) +
      (dg / (200.0 * lengthL)) * (clampedT * lengthL) * (clampedT * lengthL)
    val pinPt = mapPt(clampedT, extRel)

    // Glowing halo
    drawCircle(lineRose.copy(alpha = 0.24f), radius = 13f, center = pinPt)
    drawCircle(lineRose.copy(alpha = 0.45f), radius = 8.5f, center = pinPt)
    drawCircle(lineRose, radius = 5.0f, center = pinPt)
    drawCircle(Color.White, radius = 2.0f, center = pinPt)

    val pinTag = if (result.isExtWithinCurve) {
      "${if (result.isSummit) "MAX" else "MIN"} ${fmtNum(result.elevExt, 2)}m"
    } else {
      "EXT OUTSIDE"
    }
    drawContext.canvas.nativeCanvas.drawText(
      pinTag,
      (pinPt.x - 36f).coerceIn(10f, w - 105f),
      (pinPt.y - 16f).coerceAtLeast(16f),
      accentPaint
    )
  }

  // Header Badge
  val typeText = result?.takeIf { it.isValid }?.curveType?.uppercase() ?: "PARABOLIC VERTICAL CURVE"
  drawContext.canvas.nativeCanvas.drawText(typeText, 12f, 18f, emeraldPaint)
  drawContext.canvas.nativeCanvas.drawText("g₁=${fmtNum(g1, 1)}%  g₂=${fmtNum(g2, 1)}%", w - 145f, 18f, textPaint)
}

// ----------------------------------------------------------------------------
// 5. LEVELING & STADIA CAD
// ----------------------------------------------------------------------------

private fun DrawScope.drawStadiaLevelingCad(
  result: StadiaLevelingResult?,
  lineMain: Color,
  lineAccent: Color,
  lineEmerald: Color,
  inkMuted: Color,
  textPaint: Paint,
  accentPaint: Paint,
  mainPaint: Paint
) {
  val w = size.width
  val h = size.height
  val dash = PathEffect.dashPathEffect(floatArrayOf(9f, 6f), 0f)

  val alphaDeg = (result?.takeIf { it.isValid }?.alphaDeg ?: 12.0).coerceIn(-35.0, 35.0)
  val groundY = h * 0.80f
  val transitX = w * 0.16f
  val instY = h * 0.56f
  val rodX = w * 0.78f

  // Ground line
  drawLine(inkMuted.copy(alpha = 0.55f), Offset(18f, groundY), Offset(w - 18f, groundY), strokeWidth = 2.0f)

  // Transit Tripod
  drawLine(lineMain, Offset(transitX, instY), Offset(transitX - 18f, groundY), strokeWidth = 2.6f)
  drawLine(lineMain, Offset(transitX, instY), Offset(transitX + 18f, groundY), strokeWidth = 2.6f)
  drawCircle(lineAccent, radius = 6.5f, center = Offset(transitX, instY))

  // Stadia Rod
  val rodTop = 26f
  drawLine(lineEmerald, Offset(rodX, rodTop), Offset(rodX, groundY), strokeWidth = 4.0f, cap = StrokeCap.Round)

  // Horizontal line of sight (HI plane)
  drawLine(inkMuted.copy(alpha = 0.65f), Offset(transitX, instY), Offset(rodX, instY), strokeWidth = 1.8f, pathEffect = dash)

  // Inclined line of sight M and stadia rays U, L
  val run = rodX - transitX
  val dy = (-tan(alphaDeg * PI / 180.0) * run * 0.65).toFloat().coerceIn(-(instY - 34f), (groundY - instY - 18f))
  val mTarget = Offset(rodX, instY + dy)
  val spread = 22f
  val uTarget = Offset(rodX, (mTarget.y - spread).coerceAtLeast(24f))
  val lTarget = Offset(rodX, (mTarget.y + spread).coerceAtMost(groundY - 6f))

  drawLine(lineAccent, Offset(transitX, instY), mTarget, strokeWidth = 2.6f)
  drawLine(lineMain.copy(alpha = 0.75f), Offset(transitX, instY), uTarget, strokeWidth = 1.5f, pathEffect = dash)
  drawLine(lineMain.copy(alpha = 0.75f), Offset(transitX, instY), lTarget, strokeWidth = 1.5f, pathEffect = dash)

  // Rod ticks U, M, L
  listOf(uTarget to "U", mTarget to "M", lTarget to "L").forEach { (pt, lbl) ->
    drawCircle(lineAccent, radius = 4.0f, center = pt)
    drawContext.canvas.nativeCanvas.drawText(lbl, pt.x + 8f, pt.y + 4f, textPaint)
  }

  // Vertical Distance V highlight on rod
  drawLine(lineRoseColorApprox(lineAccent), Offset(rodX - 10f, instY), Offset(rodX - 10f, mTarget.y), strokeWidth = 2.4f)

  val hLbl = if (result != null && result.isValid) "H=${fmtNum(result.horizontalDistanceH, 2)}m" else "H = Ks·cos²α"
  val vLbl = if (result != null && result.isValid) "V=${fmtNum(result.verticalDistanceV, 2)}m" else "V"
  val elevLbl = if (result != null && result.isValid) "Elev = ${fmtNum(result.rodElevation, 3)} m" else "Elev = HI + V - M"

  drawContext.canvas.nativeCanvas.drawText("α=${fmtNum(alphaDeg, 2)}°", transitX + 36f, instY + dy * 0.25f - 6f, accentPaint)
  drawContext.canvas.nativeCanvas.drawText(hLbl, (transitX + rodX) * 0.44f, instY + 16f, mainPaint)
  drawContext.canvas.nativeCanvas.drawText(vLbl, rodX - 66f, (instY + mTarget.y) * 0.5f + 4f, accentPaint)
  drawContext.canvas.nativeCanvas.drawText(elevLbl, 14f, 22f, textPaint)
}

private fun lineRoseColorApprox(fallback: Color): Color = fallback

// ----------------------------------------------------------------------------
// 6. EARTHWORK PRISMOIDAL VOLUME BLOCK CAD
// ----------------------------------------------------------------------------

private fun DrawScope.drawEarthworkPrismoidalCad(
  result: EarthworkVolumeResult?,
  lineMain: Color,
  lineAccent: Color,
  lineEmerald: Color,
  inkMuted: Color,
  textPaint: Paint,
  accentPaint: Paint,
  emeraldPaint: Paint
) {
  val w = size.width
  val h = size.height
  val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

  val a1 = result?.takeIf { it.isValid }?.area1 ?: 24.0
  val am = result?.takeIf { it.isValid }?.areaMid ?: 30.0
  val a2 = result?.takeIf { it.isValid }?.area2 ?: 36.0
  val maxA = max(max(a1, max(am, a2)), 1.0)

  val h1 = (28f + 54f * sqrt((a1 / maxA).toFloat())).coerceIn(26f, 88f)
  val hm = (28f + 54f * sqrt((am / maxA).toFloat())).coerceIn(26f, 88f)
  val h2 = (28f + 54f * sqrt((a2 / maxA).toFloat())).coerceIn(26f, 88f)

  val baseLeftY = h * 0.78f
  val baseMidY = h * 0.72f
  val baseRightY = h * 0.66f

  val x1L = w * 0.12f
  val x1R = w * 0.30f
  val xmL = w * 0.39f
  val xmR = w * 0.59f
  val x2L = w * 0.68f
  val x2R = w * 0.88f

  val skew = 16f

  fun drawTrap(xL: Float, xR: Float, baseY: Float, heightPx: Float, color: Color, fillAlpha: Float, dashed: Boolean) {
    val p = Path().apply {
      moveTo(xL, baseY)
      lineTo(xR, baseY)
      lineTo(xR - skew, baseY - heightPx)
      lineTo(xL + skew, baseY - heightPx)
      close()
    }
    drawPath(p, color = color.copy(alpha = fillAlpha))
    drawPath(
      p,
      color = color,
      style = Stroke(width = 2.6f, pathEffect = if (dashed) dash else null, join = StrokeJoin.Round)
    )
  }

  // Longitudinal connecting edges
  drawLine(inkMuted.copy(alpha = 0.65f), Offset(x1L, baseLeftY), Offset(x2L, baseRightY), strokeWidth = 1.8f, pathEffect = dash)
  drawLine(lineMain, Offset(x1R, baseLeftY), Offset(x2R, baseRightY), strokeWidth = 2.4f)
  drawLine(lineMain, Offset(x1L + skew, baseLeftY - h1), Offset(x2L + skew, baseRightY - h2), strokeWidth = 2.4f)
  drawLine(lineMain, Offset(x1R - skew, baseLeftY - h1), Offset(x2R - skew, baseRightY - h2), strokeWidth = 2.4f)

  // Three cross sections A1, Am, A2
  drawTrap(x2L, x2R, baseRightY, h2, lineEmerald, 0.16f, false)
  drawTrap(xmL, xmR, baseMidY, hm, lineAccent, 0.12f, true)
  drawTrap(x1L, x1R, baseLeftY, h1, lineMain, 0.20f, false)

  // Labels
  drawContext.canvas.nativeCanvas.drawText("A₁=${fmtNum(a1, 1)}m²", x1L, baseLeftY + 16f, textPaint)
  drawContext.canvas.nativeCanvas.drawText("Am=${fmtNum(am, 1)}m²", xmL + 6f, baseMidY - hm - 8f, accentPaint)
  drawContext.canvas.nativeCanvas.drawText("A₂=${fmtNum(a2, 1)}m²", x2L + 6f, baseRightY - h2 - 8f, emeraldPaint)

  val lLbl = if (result != null && result.isValid) "L = ${fmtNum(result.lengthL, 2)} m" else "L"
  val vLbl = if (result != null && result.isValid) "Vp = ${fmtNum(result.volPrismoidal, 2)} m³" else "Prismoidal Block"
  drawContext.canvas.nativeCanvas.drawText(lLbl, w * 0.45f, h - 10f, textPaint)
  drawContext.canvas.nativeCanvas.drawText(vLbl, 14f, 20f, emeraldPaint)
}

private fun hypotFloat(dx: Float, dy: Float): Float = sqrt(dx * dx + dy * dy)

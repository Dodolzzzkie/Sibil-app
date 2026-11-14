package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import kotlin.math.max
import kotlin.math.roundToInt

// ============================================================================
// CASIO NATURAL TEXTBOOK DISPLAY (2D MathO RENDERER)
// ============================================================================

internal val MathAxisLine = HorizontalAlignmentLine(merger = { old, _ -> old })

private val LcdMathInk = Color(0xFF0F172A)
private val LcdDottedBoxBorder = Color(0xFF475569)

private fun childFontSize(parentSize: TextUnit): TextUnit {
  return (parentSize.value * 0.75f).coerceAtLeast(10.5f).sp
}

private fun miniSubFontSize(parentSize: TextUnit): TextUnit {
  return (parentSize.value * 0.62f).coerceAtLeast(9.0f).sp
}

private class RadicalLayoutMetrics(
  var leftZonePx: Float = 0f,
  var topPadPx: Float = 0f,
  var radWidthPx: Float = 0f,
  var radHeightPx: Float = 0f
)

@Composable
internal fun MathContainerView(
  container: MathContainer,
  cursor: CursorLocation,
  blinkOn: Boolean,
  fontSize: TextUnit,
  isRoot: Boolean = false,
  isEditable: Boolean = true,
  inkColor: Color = LcdMathInk,
  onSelectCursor: (CursorLocation) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val isFocused = isEditable && cursor.container === container
  val activeIdx = if (isFocused) cursor.index.coerceIn(0, container.nodes.size) else -1
  val boxW = (fontSize.value * 0.78f).coerceAtLeast(10.5f).dp
  val boxH = (fontSize.value * 1.10f).coerceAtLeast(14f).dp
  val cursorH = (fontSize.value * 1.14f).coerceAtLeast(13f).dp

  val tapSource = remember { MutableInteractionSource() }

  if (container.nodes.isEmpty()) {
    if (!isEditable) {
      SpacerBox()
      return
    }
    if (isRoot) {
      Box(
        modifier = modifier
          .defaultMinSize(minWidth = 10.dp, minHeight = cursorH)
          .clickable(interactionSource = tapSource, indication = null) {
            onSelectCursor(CursorLocation(container, 0))
          },
        contentAlignment = Alignment.CenterStart
      ) {
        MathBlinkingCursorBar(
          visible = isFocused && blinkOn,
          height = cursorH,
          color = inkColor
        )
      }
    } else {
      // Dotted empty placeholder box [ ] matching Casio Natural Display
      Box(
        modifier = modifier
          .padding(horizontal = 1.5.dp, vertical = 1.dp)
          .width(boxW)
          .height(boxH)
          .drawBehind {
            val dash = PathEffect.dashPathEffect(floatArrayOf(3.5f, 3f), 0f)
            drawRoundRect(
              color = if (isFocused) inkColor else LcdDottedBoxBorder.copy(alpha = 0.85f),
              topLeft = Offset(0.5f, 0.5f),
              size = Size(size.width - 1f, size.height - 1f),
              cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
              style = Stroke(
                width = if (isFocused) 1.35.dp.toPx() else 1.dp.toPx(),
                pathEffect = dash
              )
            )
          }
          .clickable(interactionSource = tapSource, indication = null) {
            onSelectCursor(CursorLocation(container, 0))
          },
        contentAlignment = Alignment.Center
      ) {
        if (isFocused) {
          MathBlinkingCursorBar(
            visible = blinkOn,
            height = (boxH - 4.dp).coerceAtLeast(8.dp),
            color = inkColor
          )
        }
      }
    }
    return
  }

  val rowMod = if (isEditable) {
    modifier.clickable(interactionSource = tapSource, indication = null) {
      onSelectCursor(CursorLocation(container, container.nodes.size))
    }
  } else modifier

  MathAxisRow(modifier = rowMod) {
    container.nodes.forEachIndexed { idx, node ->
      if (isFocused && activeIdx == idx) {
        MathBlinkingCursorBar(visible = blinkOn, height = cursorH, color = inkColor)
      }
      MathNodeView(
        node = node,
        parentContainer = container,
        nodeIndex = idx,
        cursor = cursor,
        blinkOn = blinkOn,
        fontSize = fontSize,
        isEditable = isEditable,
        inkColor = inkColor,
        onSelectCursor = onSelectCursor
      )
    }
    if (isFocused && activeIdx == container.nodes.size) {
      MathBlinkingCursorBar(visible = blinkOn, height = cursorH, color = inkColor)
    }
  }
}

@Composable
private fun MathBlinkingCursorBar(
  visible: Boolean,
  height: Dp,
  color: Color
) {
  Box(
    modifier = Modifier
      .width(1.8.dp)
      .height(height)
      .background(if (visible) color else Color.Transparent)
  )
}

@Composable
private fun MathAxisRow(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Layout(modifier = modifier, content = content) { measurables, constraints ->
    val loose = constraints.copy(minWidth = 0, minHeight = 0)
    val placeables = measurables.map { it.measure(loose) }
    if (placeables.isEmpty()) {
      return@Layout layout(0, 0) {}
    }

    val axes = placeables.map { p ->
      val line = p[MathAxisLine]
      if (line != AlignmentLine.Unspecified) line else p.height / 2
    }
    val maxAbove = axes.maxOrNull() ?: 0
    val maxBelow = placeables.indices.maxOf { i -> placeables[i].height - axes[i] }
    val totalW = placeables.sumOf { it.width }
    val totalH = max(1, maxAbove + maxBelow)

    layout(totalW, totalH, mapOf(MathAxisLine to maxAbove)) {
      var x = 0
      placeables.forEachIndexed { i, p ->
        val y = maxAbove - axes[i]
        p.placeRelative(x, y)
        x += p.width
      }
    }
  }
}

@Composable
private fun MathTextGlyph(
  text: String,
  fontSize: TextUnit,
  inkColor: Color = LcdMathInk,
  fontWeight: FontWeight = FontWeight.SemiBold,
  fontStyle: FontStyle = FontStyle.Normal,
  onClick: (() -> Unit)? = null
) {
  val style = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontSize = fontSize,
    fontWeight = fontWeight,
    fontStyle = fontStyle,
    color = inkColor,
    lineHeight = fontSize * 1.08f,
    platformStyle = PlatformTextStyle(includeFontPadding = false)
  )
  val tapMod = if (onClick != null) {
    val src = remember { MutableInteractionSource() }
    Modifier.clickable(interactionSource = src, indication = null, onClick = onClick)
  } else Modifier

  Text(
    text = text,
    style = style,
    maxLines = 1,
    softWrap = false,
    modifier = tapMod
  )
}

@Composable
private fun MathNodeView(
  node: MathNode,
  parentContainer: MathContainer,
  nodeIndex: Int,
  cursor: CursorLocation,
  blinkOn: Boolean,
  fontSize: TextUnit,
  isEditable: Boolean,
  inkColor: Color,
  onSelectCursor: (CursorLocation) -> Unit
) {
  when (node) {
    is MathNode.TextNode -> {
      val pretty = when (node.text) {
        "-" -> "−"
        "*" -> "×"
        else -> node.text
      }
      MathTextGlyph(
        text = pretty,
        fontSize = fontSize,
        inkColor = inkColor,
        onClick = if (isEditable) {
          { onSelectCursor(CursorLocation(parentContainer, nodeIndex + 1)) }
        } else null
      )
    }

    is MathNode.FractionNode -> {
      FractionNodeView(
        num = node.num,
        den = node.den,
        cursor = cursor,
        blinkOn = blinkOn,
        fontSize = fontSize,
        isEditable = isEditable,
        inkColor = inkColor,
        onSelectCursor = onSelectCursor
      )
    }

    is MathNode.MixedFractionNode -> {
      val subSize = childFontSize(fontSize)
      MathAxisRow(modifier = Modifier.padding(horizontal = 1.dp)) {
        MathContainerView(
          container = node.whole,
          cursor = cursor,
          blinkOn = blinkOn,
          fontSize = fontSize,
          isEditable = isEditable,
          inkColor = inkColor,
          onSelectCursor = onSelectCursor,
          modifier = Modifier.padding(end = 2.dp)
        )
        FractionNodeView(
          num = node.num,
          den = node.den,
          cursor = cursor,
          blinkOn = blinkOn,
          fontSize = fontSize,
          isEditable = isEditable,
          inkColor = inkColor,
          subFontSize = subSize,
          onSelectCursor = onSelectCursor
        )
      }
    }

    is MathNode.PowerNode -> {
      PowerNodeView(
        base = node.base,
        exp = node.exp,
        cursor = cursor,
        blinkOn = blinkOn,
        fontSize = fontSize,
        isEditable = isEditable,
        inkColor = inkColor,
        onSelectCursor = onSelectCursor
      )
    }

    is MathNode.RootNode -> {
      RootNodeView(
        index = node.index,
        fixedIndexText = node.fixedIndexText,
        radicand = node.radicand,
        cursor = cursor,
        blinkOn = blinkOn,
        fontSize = fontSize,
        isEditable = isEditable,
        inkColor = inkColor,
        onSelectCursor = onSelectCursor
      )
    }

    is MathNode.LogBaseNode -> {
      LogBaseNodeView(
        base = node.base,
        arg = node.arg,
        cursor = cursor,
        blinkOn = blinkOn,
        fontSize = fontSize,
        isEditable = isEditable,
        inkColor = inkColor,
        onSelectCursor = onSelectCursor,
        onExitAfter = { onSelectCursor(CursorLocation(parentContainer, nodeIndex + 1)) }
      )
    }

    is MathNode.IntegralNode -> {
      IntegralNodeView(
        lower = node.lower,
        upper = node.upper,
        integrand = node.integrand,
        cursor = cursor,
        blinkOn = blinkOn,
        fontSize = fontSize,
        isEditable = isEditable,
        inkColor = inkColor,
        onSelectCursor = onSelectCursor,
        onExitAfter = { onSelectCursor(CursorLocation(parentContainer, nodeIndex + 1)) }
      )
    }

    is MathNode.DerivativeNode -> {
      DerivativeNodeView(
        expr = node.expr,
        atX = node.atX,
        cursor = cursor,
        blinkOn = blinkOn,
        fontSize = fontSize,
        isEditable = isEditable,
        inkColor = inkColor,
        onSelectCursor = onSelectCursor,
        onExitAfter = { onSelectCursor(CursorLocation(parentContainer, nodeIndex + 1)) }
      )
    }

    is MathNode.SummationNode -> {
      SummationNodeView(
        lower = node.lower,
        upper = node.upper,
        body = node.body,
        cursor = cursor,
        blinkOn = blinkOn,
        fontSize = fontSize,
        isEditable = isEditable,
        inkColor = inkColor,
        onSelectCursor = onSelectCursor,
        onExitAfter = { onSelectCursor(CursorLocation(parentContainer, nodeIndex + 1)) }
      )
    }

    is MathNode.AbsNode -> {
      AbsNodeView(
        inner = node.inner,
        cursor = cursor,
        blinkOn = blinkOn,
        fontSize = fontSize,
        isEditable = isEditable,
        inkColor = inkColor,
        onSelectCursor = onSelectCursor,
        onExitAfter = { onSelectCursor(CursorLocation(parentContainer, nodeIndex + 1)) }
      )
    }
  }
}

@Composable
private fun FractionNodeView(
  num: MathContainer,
  den: MathContainer,
  cursor: CursorLocation,
  blinkOn: Boolean,
  fontSize: TextUnit,
  isEditable: Boolean,
  inkColor: Color,
  subFontSize: TextUnit = childFontSize(fontSize),
  onSelectCursor: (CursorLocation) -> Unit
) {
  Layout(
    modifier = Modifier.padding(horizontal = 2.dp),
    content = {
      Box(modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)) {
        MathContainerView(num, cursor, blinkOn, subFontSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
      }
      Box(
        modifier = Modifier
          .height(1.4.dp)
          .background(inkColor)
      )
      Box(modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)) {
        MathContainerView(den, cursor, blinkOn, subFontSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
      }
    }
  ) { measurables, constraints ->
    val loose = constraints.copy(minWidth = 0, minHeight = 0)
    val numP = measurables[0].measure(loose)
    val denP = measurables[2].measure(loose)
    val barW = max(max(numP.width, denP.width), 16.dp.roundToPx())
    val barP = measurables[1].measure(
      loose.copy(minWidth = barW, maxWidth = barW)
    )
    val gap = 1.dp.roundToPx()
    val totalH = numP.height + gap + barP.height + gap + denP.height
    val axisY = numP.height + gap + barP.height / 2

    layout(barW, totalH, mapOf(MathAxisLine to axisY)) {
      numP.placeRelative((barW - numP.width) / 2, 0)
      barP.placeRelative(0, numP.height + gap)
      denP.placeRelative((barW - denP.width) / 2, numP.height + gap + barP.height + gap)
    }
  }
}

@Composable
private fun PowerNodeView(
  base: MathContainer,
  exp: MathContainer,
  cursor: CursorLocation,
  blinkOn: Boolean,
  fontSize: TextUnit,
  isEditable: Boolean,
  inkColor: Color,
  onSelectCursor: (CursorLocation) -> Unit
) {
  val expSize = childFontSize(fontSize)
  Layout(
    modifier = Modifier.padding(horizontal = 0.5.dp),
    content = {
      MathContainerView(base, cursor, blinkOn, fontSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
      MathContainerView(exp, cursor, blinkOn, expSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
    }
  ) { measurables, constraints ->
    val loose = constraints.copy(minWidth = 0, minHeight = 0)
    val baseP = measurables[0].measure(loose)
    val expP = measurables[1].measure(loose)

    val baseAxis = baseP[MathAxisLine].let {
      if (it != AlignmentLine.Unspecified) it else baseP.height / 2
    }

    // Raise exponent above baseline (~44% from top of base)
    val expAnchorFromBaseTop = (baseP.height * 0.44f).roundToInt()
    val expYIdeal = expAnchorFromBaseTop - expP.height
    val topShift = if (expYIdeal < 0) -expYIdeal else 0
    val baseY = topShift
    val expY = topShift + expYIdeal
    val totalH = max(baseY + baseP.height, expY + expP.height)
    val totalW = baseP.width + expP.width + 1.dp.roundToPx()
    val axisY = baseY + baseAxis

    layout(totalW, totalH, mapOf(MathAxisLine to axisY)) {
      baseP.placeRelative(0, baseY)
      expP.placeRelative(baseP.width + 1.dp.roundToPx(), expY)
    }
  }
}

@Composable
private fun RootNodeView(
  index: MathContainer?,
  fixedIndexText: String?,
  radicand: MathContainer,
  cursor: CursorLocation,
  blinkOn: Boolean,
  fontSize: TextUnit,
  isEditable: Boolean,
  inkColor: Color,
  onSelectCursor: (CursorLocation) -> Unit
) {
  val idxSize = miniSubFontSize(fontSize)
  val hasIndex = index != null || fixedIndexText != null
  val radicalMetrics = remember { RadicalLayoutMetrics() }

  Layout(
    modifier = Modifier
      .padding(horizontal = 1.5.dp)
      .drawBehind {
        val leftZone = radicalMetrics.leftZonePx
        val topPad = radicalMetrics.topPadPx
        val radW = radicalMetrics.radWidthPx
        val radH = radicalMetrics.radHeightPx
        if (radW <= 0f || radH <= 0f) return@drawBehind

        val strokeW = 1.4.dp.toPx()
        val yOverbar = topPad + 1.dp.toPx()
        val yValley = (topPad + radH - 1.5.dp.toPx()).coerceAtLeast(yOverbar + 4.dp.toPx())
        val yMid = topPad + radH * 0.56f
        val xTickStart = (leftZone - 8.dp.toPx()).coerceAtLeast(1.dp.toPx())
        val xTickKnee = (leftZone - 5.8.dp.toPx()).coerceAtLeast(xTickStart + 1.5.dp.toPx())
        val xValley = (leftZone - 3.2.dp.toPx()).coerceAtLeast(xTickKnee + 1.5.dp.toPx())
        val xPeak = (leftZone - 0.5.dp.toPx()).coerceAtLeast(xValley + 2.dp.toPx())
        val xEnd = leftZone + radW + 1.dp.toPx()

        val p = Path().apply {
          moveTo(xTickStart, yMid + 1.2.dp.toPx())
          lineTo(xTickKnee, yMid)
          lineTo(xValley, yValley)
          lineTo(xPeak, yOverbar)
          lineTo(xEnd, yOverbar)
          lineTo(xEnd, yOverbar + 2.5.dp.toPx())
        }
        drawPath(
          path = p,
          color = inkColor,
          style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
      },
    content = {
      if (index != null) {
        MathContainerView(index, cursor, blinkOn, idxSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
      } else if (fixedIndexText != null) {
        MathTextGlyph(fixedIndexText, idxSize, inkColor, FontWeight.Bold)
      } else {
        SpacerBox()
      }
      Box(
        modifier = Modifier.padding(start = 2.dp, end = 2.5.dp, top = 3.dp, bottom = 1.dp)
      ) {
        MathContainerView(radicand, cursor, blinkOn, fontSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
      }
    }
  ) { measurables, constraints ->
    val loose = constraints.copy(minWidth = 0, minHeight = 0)
    val idxP = measurables[0].measure(loose)
    val radP = measurables[1].measure(loose)

    val minNotchW = 10.5.dp.roundToPx()
    val leftZoneW = if (hasIndex) max(12.dp.roundToPx(), idxP.width + 5.dp.roundToPx()) else minNotchW
    val topPad = if (hasIndex) max(2.dp.roundToPx(), (idxP.height * 0.45f).roundToInt()) else 2.dp.roundToPx()
    val totalW = leftZoneW + radP.width + 2.dp.roundToPx()
    val totalH = topPad + radP.height

    radicalMetrics.leftZonePx = leftZoneW.toFloat()
    radicalMetrics.topPadPx = topPad.toFloat()
    radicalMetrics.radWidthPx = radP.width.toFloat()
    radicalMetrics.radHeightPx = radP.height.toFloat()

    val radAxis = radP[MathAxisLine].let {
      if (it != AlignmentLine.Unspecified) it else radP.height / 2
    }

    layout(totalW, totalH, mapOf(MathAxisLine to (topPad + radAxis))) {
      if (hasIndex) {
        val ix = (leftZoneW - 4.5.dp.roundToPx() - idxP.width).coerceAtLeast(0)
        val iy = (topPad + radP.height * 0.42f - idxP.height).roundToInt().coerceAtLeast(0)
        idxP.placeRelative(ix, iy)
      }
      radP.placeRelative(leftZoneW, topPad)
    }
  }
}

@Composable
private fun LogBaseNodeView(
  base: MathContainer,
  arg: MathContainer,
  cursor: CursorLocation,
  blinkOn: Boolean,
  fontSize: TextUnit,
  isEditable: Boolean,
  inkColor: Color,
  onSelectCursor: (CursorLocation) -> Unit,
  onExitAfter: () -> Unit
) {
  val subSize = childFontSize(fontSize)
  Layout(
    modifier = Modifier.padding(horizontal = 1.dp),
    content = {
      MathTextGlyph("log", fontSize, inkColor, FontWeight.SemiBold, onClick = if (isEditable) { { onSelectCursor(CursorLocation(base, base.nodes.size)) } } else null)
      MathContainerView(base, cursor, blinkOn, subSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
      MathTextGlyph("(", fontSize, inkColor, FontWeight.SemiBold, onClick = if (isEditable) { { onSelectCursor(CursorLocation(arg, 0)) } } else null)
      MathContainerView(arg, cursor, blinkOn, fontSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
      MathTextGlyph(")", fontSize, inkColor, FontWeight.SemiBold, onClick = if (isEditable) onExitAfter else null)
    }
  ) { measurables, constraints ->
    val loose = constraints.copy(minWidth = 0, minHeight = 0)
    val logP = measurables[0].measure(loose)
    val baseP = measurables[1].measure(loose)
    val lpP = measurables[2].measure(loose)
    val argP = measurables[3].measure(loose)
    val rpP = measurables[4].measure(loose)

    val rowItems = listOf(logP, lpP, argP, rpP)
    val rowAxes = rowItems.map { p ->
      p[MathAxisLine].let { if (it != AlignmentLine.Unspecified) it else p.height / 2 }
    }
    val maxAbove = rowAxes.maxOrNull() ?: 0
    val subBottomOffset = (logP.height * 0.42f).roundToInt()
    val baseTopRelLog = maxAbove - rowAxes[0] + subBottomOffset
    val maxBelow = max(
      rowItems.indices.maxOf { i -> rowItems[i].height - rowAxes[i] },
      baseTopRelLog + baseP.height - maxAbove
    )
    val totalH = max(1, maxAbove + maxBelow)
    val gap = 1.dp.roundToPx()
    val totalW = logP.width + baseP.width + gap + lpP.width + argP.width + rpP.width

    layout(totalW, totalH, mapOf(MathAxisLine to maxAbove)) {
      var x = 0
      logP.placeRelative(x, maxAbove - rowAxes[0])
      x += logP.width
      baseP.placeRelative(x, baseTopRelLog)
      x += baseP.width + gap
      lpP.placeRelative(x, maxAbove - rowAxes[1])
      x += lpP.width
      argP.placeRelative(x, maxAbove - rowAxes[2])
      x += argP.width
      rpP.placeRelative(x, maxAbove - rowAxes[3])
    }
  }
}

@Composable
private fun IntegralNodeView(
  lower: MathContainer,
  upper: MathContainer,
  integrand: MathContainer,
  cursor: CursorLocation,
  blinkOn: Boolean,
  fontSize: TextUnit,
  isEditable: Boolean,
  inkColor: Color,
  onSelectCursor: (CursorLocation) -> Unit,
  onExitAfter: () -> Unit
) {
  val boundSize = childFontSize(fontSize)
  val symH = (fontSize.value * 1.95f).coerceAtLeast(26f).dp
  val symW = (fontSize.value * 0.72f).coerceAtLeast(10f).dp

  MathAxisRow(modifier = Modifier.padding(horizontal = 1.5.dp)) {
    // ∫ symbol with top-right upper limit & bottom-right lower limit
    Layout(
      content = {
        Box(
          modifier = Modifier
            .width(symW)
            .height(symH)
            .drawBehind {
              val w = size.width
              val h = size.height
              val sw = 1.5.dp.toPx()
              val path = Path().apply {
                moveTo(w * 0.85f, h * 0.08f)
                cubicTo(w * 0.65f, 0f, w * 0.50f, h * 0.14f, w * 0.50f, h * 0.44f)
                lineTo(w * 0.44f, h * 0.58f)
                cubicTo(w * 0.44f, h * 0.88f, w * 0.30f, h, w * 0.12f, h * 0.92f)
              }
              drawPath(
                path = path,
                color = inkColor,
                style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
              )
              // Classic Casio integral endpoint dots
              drawCircle(inkColor, radius = 1.15.dp.toPx(), center = Offset(w * 0.85f, h * 0.08f))
              drawCircle(inkColor, radius = 1.15.dp.toPx(), center = Offset(w * 0.12f, h * 0.92f))
            }
        )
        MathContainerView(upper, cursor, blinkOn, boundSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
        MathContainerView(lower, cursor, blinkOn, boundSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
      }
    ) { measurables, constraints ->
      val loose = constraints.copy(minWidth = 0, minHeight = 0)
      val symP = measurables[0].measure(loose)
      val upP = measurables[1].measure(loose)
      val loP = measurables[2].measure(loose)

      val limW = max(upP.width, loP.width)
      val totalW = symP.width + limW + 2.dp.roundToPx()
      val upOver = max(0, upP.height / 2 - 2.dp.roundToPx())
      val loOver = max(0, loP.height / 2 - 2.dp.roundToPx())
      val totalH = max(upOver + symP.height + loOver, upP.height + loP.height + 2.dp.roundToPx())
      val symY = (totalH - symP.height) / 2
      val axisY = symY + symP.height / 2

      layout(totalW, totalH, mapOf(MathAxisLine to axisY)) {
        symP.placeRelative(0, symY)
        upP.placeRelative(symP.width, 0)
        loP.placeRelative(symP.width - 1.dp.roundToPx(), totalH - loP.height)
      }
    }

    MathTextGlyph("(", fontSize, inkColor, FontWeight.SemiBold, onClick = if (isEditable) { { onSelectCursor(CursorLocation(integrand, 0)) } } else null)
    MathContainerView(integrand, cursor, blinkOn, fontSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
    MathTextGlyph(")", fontSize, inkColor, FontWeight.SemiBold, onClick = if (isEditable) { { onSelectCursor(CursorLocation(integrand, integrand.nodes.size)) } } else null)
    MathTextGlyph("d", fontSize * 0.85f, inkColor, FontWeight.SemiBold, onClick = if (isEditable) onExitAfter else null)
    MathTextGlyph("x", fontSize * 0.85f, inkColor, FontWeight.Bold, FontStyle.Italic, onClick = if (isEditable) onExitAfter else null)
  }
}

@Composable
private fun DerivativeNodeView(
  expr: MathContainer,
  atX: MathContainer,
  cursor: CursorLocation,
  blinkOn: Boolean,
  fontSize: TextUnit,
  isEditable: Boolean,
  inkColor: Color,
  onSelectCursor: (CursorLocation) -> Unit,
  onExitAfter: () -> Unit
) {
  val subSize = childFontSize(fontSize)
  MathAxisRow(modifier = Modifier.padding(horizontal = 1.5.dp)) {
    // d / dx stacked prefix aligned cleanly on MathAxisLine
    Layout(
      modifier = Modifier.padding(end = 2.dp),
      content = {
        Row {
          MathTextGlyph("d", subSize, inkColor, FontWeight.Bold)
        }
        Box(modifier = Modifier.height(1.3.dp).background(inkColor))
        Row {
          MathTextGlyph("d", subSize, inkColor, FontWeight.Bold)
          MathTextGlyph("x", subSize, inkColor, FontWeight.Bold, FontStyle.Italic)
        }
      }
    ) { measurables, constraints ->
      val loose = constraints.copy(minWidth = 0, minHeight = 0)
      val dP = measurables[0].measure(loose)
      val dxP = measurables[2].measure(loose)
      val barW = max(dP.width, dxP.width) + 2.dp.roundToPx()
      val barP = measurables[1].measure(loose.copy(minWidth = barW, maxWidth = barW))
      val gap = 1.dp.roundToPx()
      val totalH = dP.height + gap + barP.height + gap + dxP.height
      val axisY = dP.height + gap + barP.height / 2

      layout(barW, totalH, mapOf(MathAxisLine to axisY)) {
        dP.placeRelative((barW - dP.width) / 2, 0)
        barP.placeRelative(0, dP.height + gap)
        dxP.placeRelative((barW - dxP.width) / 2, dP.height + gap + barP.height + gap)
      }
    }

    MathTextGlyph("(", fontSize, inkColor, FontWeight.SemiBold, onClick = if (isEditable) { { onSelectCursor(CursorLocation(expr, 0)) } } else null)
    MathContainerView(expr, cursor, blinkOn, fontSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
    MathTextGlyph(")", fontSize, inkColor, FontWeight.SemiBold, onClick = if (isEditable) { { onSelectCursor(CursorLocation(expr, expr.nodes.size)) } } else null)

    // Tall vertical bar |_{x = [ ]}
    Layout(
      modifier = Modifier.padding(start = 2.dp),
      content = {
        val barBaseMod = Modifier
          .width(1.4.dp)
          .fillMaxHeight()
          .background(inkColor)
        Box(
          modifier = if (isEditable) {
            barBaseMod.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onExitAfter)
          } else barBaseMod
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          MathTextGlyph("x=", subSize, inkColor, FontWeight.Bold, onClick = if (isEditable) { { onSelectCursor(CursorLocation(atX, 0)) } } else null)
          MathContainerView(atX, cursor, blinkOn, subSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
        }
      }
    ) { measurables, constraints ->
      val loose = constraints.copy(minWidth = 0, minHeight = 0)
      val evalP = measurables[1].measure(loose)
      val barH = max((fontSize.value * 1.55f).dp.roundToPx(), evalP.height + 6.dp.roundToPx())
      val barP = measurables[0].measure(loose.copy(minHeight = barH, maxHeight = barH))
      val totalW = barP.width + 2.dp.roundToPx() + evalP.width
      val axisY = barH / 2
      layout(totalW, barH, mapOf(MathAxisLine to axisY)) {
        barP.placeRelative(0, 0)
        evalP.placeRelative(barP.width + 2.dp.roundToPx(), barH - evalP.height)
      }
    }
  }
}

@Composable
private fun SummationNodeView(
  lower: MathContainer,
  upper: MathContainer,
  body: MathContainer,
  cursor: CursorLocation,
  blinkOn: Boolean,
  fontSize: TextUnit,
  isEditable: Boolean,
  inkColor: Color,
  onSelectCursor: (CursorLocation) -> Unit,
  onExitAfter: () -> Unit
) {
  val boundSize = miniSubFontSize(fontSize)
  val sigH = (fontSize.value * 1.18f).coerceAtLeast(16f).dp
  val sigW = (fontSize.value * 0.96f).coerceAtLeast(13f).dp

  MathAxisRow(modifier = Modifier.padding(horizontal = 1.5.dp)) {
    // Centered vertical stack: upper limit / Σ vector / x=lower limit
    Layout(
      content = {
        MathContainerView(upper, cursor, blinkOn, boundSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
        Box(
          modifier = Modifier
            .width(sigW)
            .height(sigH)
            .drawBehind {
              val w = size.width
              val h = size.height
              val sw = 1.45.dp.toPx()
              val path = Path().apply {
                moveTo(w * 0.90f, h * 0.16f)
                lineTo(w * 0.90f, sw * 0.5f)
                lineTo(w * 0.08f, sw * 0.5f)
                lineTo(w * 0.56f, h * 0.50f)
                lineTo(w * 0.08f, h - sw * 0.5f)
                lineTo(w * 0.90f, h - sw * 0.5f)
                lineTo(w * 0.90f, h * 0.84f)
              }
              drawPath(
                path = path,
                color = inkColor,
                style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
              )
            }
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          MathTextGlyph("x=", boundSize, inkColor, FontWeight.Bold, onClick = if (isEditable) { { onSelectCursor(CursorLocation(lower, 0)) } } else null)
          MathContainerView(lower, cursor, blinkOn, boundSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
        }
      }
    ) { measurables, constraints ->
      val loose = constraints.copy(minWidth = 0, minHeight = 0)
      val upP = measurables[0].measure(loose)
      val sigP = measurables[1].measure(loose)
      val loP = measurables[2].measure(loose)

      val colW = max(sigP.width, max(upP.width, loP.width)) + 2.dp.roundToPx()
      val gap = 1.dp.roundToPx()
      val totalH = upP.height + gap + sigP.height + gap + loP.height
      val axisY = upP.height + gap + sigP.height / 2

      layout(colW, totalH, mapOf(MathAxisLine to axisY)) {
        upP.placeRelative((colW - upP.width) / 2, 0)
        sigP.placeRelative((colW - sigP.width) / 2, upP.height + gap)
        loP.placeRelative((colW - loP.width) / 2, upP.height + gap + sigP.height + gap)
      }
    }

    MathTextGlyph("(", fontSize, inkColor, FontWeight.SemiBold, onClick = if (isEditable) { { onSelectCursor(CursorLocation(body, 0)) } } else null)
    MathContainerView(body, cursor, blinkOn, fontSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
    MathTextGlyph(")", fontSize, inkColor, FontWeight.SemiBold, onClick = if (isEditable) onExitAfter else null)
  }
}

@Composable
private fun AbsNodeView(
  inner: MathContainer,
  cursor: CursorLocation,
  blinkOn: Boolean,
  fontSize: TextUnit,
  isEditable: Boolean,
  inkColor: Color,
  onSelectCursor: (CursorLocation) -> Unit,
  onExitAfter: () -> Unit
) {
  Layout(
    modifier = Modifier.padding(horizontal = 1.dp),
    content = {
      val leftBarMod = Modifier.width(1.4.dp).background(inkColor)
      Box(
        modifier = if (isEditable) {
          leftBarMod.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
            onSelectCursor(CursorLocation(inner, 0))
          }
        } else leftBarMod
      )
      MathContainerView(inner, cursor, blinkOn, fontSize, isEditable = isEditable, inkColor = inkColor, onSelectCursor = onSelectCursor)
      val rightBarMod = Modifier.width(1.4.dp).background(inkColor)
      Box(
        modifier = if (isEditable) {
          rightBarMod.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onExitAfter)
        } else rightBarMod
      )
    }
  ) { measurables, constraints ->
    val loose = constraints.copy(minWidth = 0, minHeight = 0)
    val innerP = measurables[1].measure(loose)
    val barH = max(innerP.height + 2.dp.roundToPx(), (fontSize.value * 1.15f).dp.roundToPx())
    val leftBarP = measurables[0].measure(loose.copy(minHeight = barH, maxHeight = barH))
    val rightBarP = measurables[2].measure(loose.copy(minHeight = barH, maxHeight = barH))
    val pad = 1.5.dp.roundToPx()
    val totalW = leftBarP.width + pad + innerP.width + pad + rightBarP.width
    val innerAxis = innerP[MathAxisLine].let {
      if (it != AlignmentLine.Unspecified) it else innerP.height / 2
    }
    val innerY = (barH - innerP.height) / 2
    val axisY = innerY + innerAxis

    layout(totalW, barH, mapOf(MathAxisLine to axisY)) {
      leftBarP.placeRelative(0, 0)
      innerP.placeRelative(leftBarP.width + pad, innerY)
      rightBarP.placeRelative(leftBarP.width + pad + innerP.width + pad, 0)
    }
  }
}

@Composable
private fun SpacerBox() {
  Box(modifier = Modifier.width(0.dp).height(0.dp))
}

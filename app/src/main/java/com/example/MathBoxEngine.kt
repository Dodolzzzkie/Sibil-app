package com.example

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.atanh
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh
import kotlin.random.Random

// ============================================================================
// CASIO NATURAL-V.P.A.M. (MathO) 2D AST, CURSOR NAVIGATION & EVALUATOR
// ============================================================================

internal class MathContainer(
  val nodes: SnapshotStateList<MathNode> = mutableStateListOf()
)

internal sealed interface MathNode {
  data class TextNode(val text: String) : MathNode
  data class FractionNode(val num: MathContainer, val den: MathContainer) : MathNode
  data class MixedFractionNode(
    val whole: MathContainer,
    val num: MathContainer,
    val den: MathContainer
  ) : MathNode
  data class PowerNode(val base: MathContainer, val exp: MathContainer) : MathNode
  data class RootNode(
    val index: MathContainer?,
    val radicand: MathContainer,
    val fixedIndexText: String? = null
  ) : MathNode
  data class LogBaseNode(val base: MathContainer, val arg: MathContainer) : MathNode
  data class IntegralNode(
    val lower: MathContainer,
    val upper: MathContainer,
    val integrand: MathContainer
  ) : MathNode
  data class DerivativeNode(val expr: MathContainer, val atX: MathContainer) : MathNode
  data class SummationNode(
    val lower: MathContainer,
    val upper: MathContainer,
    val body: MathContainer
  ) : MathNode
  data class AbsNode(val inner: MathContainer) : MathNode
}

internal data class CursorLocation(
  val container: MathContainer,
  val index: Int
)

internal data class ParentSlotInfo(
  val parentContainer: MathContainer,
  val nodeIndexInParent: Int,
  val parentNode: MathNode,
  val slotIndexInNode: Int
)

internal fun MathNode.childSlots(): List<MathContainer> = when (this) {
  is MathNode.TextNode -> emptyList()
  is MathNode.FractionNode -> listOf(num, den)
  is MathNode.MixedFractionNode -> listOf(whole, num, den)
  is MathNode.PowerNode -> listOf(base, exp)
  is MathNode.RootNode -> if (index != null) listOf(index, radicand) else listOf(radicand)
  is MathNode.LogBaseNode -> listOf(base, arg)
  is MathNode.IntegralNode -> listOf(lower, upper, integrand)
  is MathNode.DerivativeNode -> listOf(expr, atX)
  is MathNode.SummationNode -> listOf(lower, upper, body)
  is MathNode.AbsNode -> listOf(inner)
}

internal fun cloneMathNode(node: MathNode): MathNode = when (node) {
  is MathNode.TextNode -> node.copy()
  is MathNode.FractionNode -> MathNode.FractionNode(
    num = cloneMathContainer(node.num),
    den = cloneMathContainer(node.den)
  )
  is MathNode.MixedFractionNode -> MathNode.MixedFractionNode(
    whole = cloneMathContainer(node.whole),
    num = cloneMathContainer(node.num),
    den = cloneMathContainer(node.den)
  )
  is MathNode.PowerNode -> MathNode.PowerNode(
    base = cloneMathContainer(node.base),
    exp = cloneMathContainer(node.exp)
  )
  is MathNode.RootNode -> MathNode.RootNode(
    index = node.index?.let { cloneMathContainer(it) },
    radicand = cloneMathContainer(node.radicand),
    fixedIndexText = node.fixedIndexText
  )
  is MathNode.LogBaseNode -> MathNode.LogBaseNode(
    base = cloneMathContainer(node.base),
    arg = cloneMathContainer(node.arg)
  )
  is MathNode.IntegralNode -> MathNode.IntegralNode(
    lower = cloneMathContainer(node.lower),
    upper = cloneMathContainer(node.upper),
    integrand = cloneMathContainer(node.integrand)
  )
  is MathNode.DerivativeNode -> MathNode.DerivativeNode(
    expr = cloneMathContainer(node.expr),
    atX = cloneMathContainer(node.atX)
  )
  is MathNode.SummationNode -> MathNode.SummationNode(
    lower = cloneMathContainer(node.lower),
    upper = cloneMathContainer(node.upper),
    body = cloneMathContainer(node.body)
  )
  is MathNode.AbsNode -> MathNode.AbsNode(inner = cloneMathContainer(node.inner))
}

internal fun cloneMathContainer(container: MathContainer): MathContainer {
  val copy = MathContainer()
  container.nodes.forEach { copy.nodes.add(cloneMathNode(it)) }
  return copy
}

internal fun mathContainerOfText(raw: String): MathContainer {
  val c = MathContainer()
  raw.forEach { ch -> c.nodes.add(MathNode.TextNode(ch.toString())) }
  return c
}

internal fun findParentInfo(root: MathContainer, target: MathContainer): ParentSlotInfo? {
  if (root === target) return null
  for (i in root.nodes.indices) {
    val node = root.nodes[i]
    val slots = node.childSlots()
    for (sIdx in slots.indices) {
      val slot = slots[sIdx]
      if (slot === target) {
        return ParentSlotInfo(root, i, node, sIdx)
      }
      val nested = findParentInfo(slot, target)
      if (nested != null) return nested
    }
  }
  return null
}

internal fun containsContainer(root: MathContainer, target: MathContainer): Boolean {
  if (root === target) return true
  for (node in root.nodes) {
    for (slot in node.childSlots()) {
      if (containsContainer(slot, target)) return true
    }
  }
  return false
}

internal fun findFirstEmptySlot(container: MathContainer, checkRootNonEmpty: Boolean = false): MathContainer? {
  if (checkRootNonEmpty && container.nodes.isEmpty()) return container
  for (node in container.nodes) {
    for (slot in node.childSlots()) {
      if (slot.nodes.isEmpty()) return slot
      val deeper = findFirstEmptySlot(slot, checkRootNonEmpty = false)
      if (deeper != null) return deeper
    }
  }
  return null
}

internal fun collectVariablesInTree(container: MathContainer, out: MutableSet<String>) {
  val varSet = setOf("A", "B", "C", "D", "E", "F", "X", "Y", "M")
  for (node in container.nodes) {
    when (node) {
      is MathNode.TextNode -> {
        if (node.text in varSet) out.add(node.text)
      }
      else -> node.childSlots().forEach { collectVariablesInTree(it, out) }
    }
  }
}

// ============================================================================
// CURSOR NAVIGATION (REPLAY ◄ ► ▲ ▼) & SMART DELETE / UNWRAP
// ============================================================================

internal fun moveCursorRight(root: MathContainer, cursor: CursorLocation): CursorLocation {
  val safeC = if (containsContainer(root, cursor.container)) cursor.container else root
  val i = cursor.index.coerceIn(0, safeC.nodes.size)
  if (i < safeC.nodes.size) {
    val node = safeC.nodes[i]
    val slots = node.childSlots()
    return if (slots.isNotEmpty()) {
      CursorLocation(slots.first(), 0)
    } else {
      CursorLocation(safeC, i + 1)
    }
  }
  val parent = findParentInfo(root, safeC) ?: return CursorLocation(safeC, safeC.nodes.size)
  val slots = parent.parentNode.childSlots()
  return if (parent.slotIndexInNode + 1 < slots.size) {
    CursorLocation(slots[parent.slotIndexInNode + 1], 0)
  } else {
    CursorLocation(parent.parentContainer, parent.nodeIndexInParent + 1)
  }
}

internal fun moveCursorLeft(root: MathContainer, cursor: CursorLocation): CursorLocation {
  val safeC = if (containsContainer(root, cursor.container)) cursor.container else root
  val i = cursor.index.coerceIn(0, safeC.nodes.size)
  if (i > 0) {
    val node = safeC.nodes[i - 1]
    val slots = node.childSlots()
    return if (slots.isNotEmpty()) {
      val lastSlot = slots.last()
      CursorLocation(lastSlot, lastSlot.nodes.size)
    } else {
      CursorLocation(safeC, i - 1)
    }
  }
  val parent = findParentInfo(root, safeC) ?: return CursorLocation(safeC, 0)
  val slots = parent.parentNode.childSlots()
  return if (parent.slotIndexInNode > 0) {
    val prevSlot = slots[parent.slotIndexInNode - 1]
    CursorLocation(prevSlot, prevSlot.nodes.size)
  } else {
    CursorLocation(parent.parentContainer, parent.nodeIndexInParent)
  }
}

/**
 * Returns null if at root / no vertical parent handled the move (so caller can scroll history).
 */
internal fun moveCursorVertical(
  root: MathContainer,
  cursor: CursorLocation,
  isUp: Boolean
): CursorLocation? {
  var current = cursor.container
  while (true) {
    val info = findParentInfo(root, current) ?: return null
    val pNode = info.parentNode
    val nextSlot: MathContainer? = when (pNode) {
      is MathNode.FractionNode -> when {
        !isUp && current === pNode.num -> pNode.den
        isUp && current === pNode.den -> pNode.num
        else -> null
      }
      is MathNode.MixedFractionNode -> when {
        !isUp && current === pNode.num -> pNode.den
        isUp && current === pNode.den -> pNode.num
        !isUp && current === pNode.whole -> pNode.den
        isUp && current === pNode.whole -> pNode.num
        else -> null
      }
      is MathNode.IntegralNode -> when {
        isUp && current === pNode.lower -> pNode.upper
        !isUp && current === pNode.upper -> pNode.lower
        isUp && current === pNode.integrand -> pNode.upper
        !isUp && current === pNode.integrand -> pNode.lower
        else -> null
      }
      is MathNode.SummationNode -> when {
        isUp && current === pNode.lower -> pNode.upper
        !isUp && current === pNode.upper -> pNode.lower
        isUp && current === pNode.body -> pNode.upper
        !isUp && current === pNode.body -> pNode.lower
        else -> null
      }
      is MathNode.PowerNode -> when {
        isUp && current === pNode.base -> pNode.exp
        !isUp && current === pNode.exp -> pNode.base
        else -> null
      }
      is MathNode.LogBaseNode -> when {
        !isUp && current === pNode.arg -> pNode.base
        isUp && current === pNode.base -> pNode.arg
        else -> null
      }
      is MathNode.DerivativeNode -> when {
        !isUp && current === pNode.expr -> pNode.atX
        isUp && current === pNode.atX -> pNode.expr
        else -> null
      }
      else -> null
    }
    if (nextSlot != null) {
      val targetIdx = if (current === cursor.container) {
        cursor.index.coerceIn(0, nextSlot.nodes.size)
      } else {
        nextSlot.nodes.size
      }
      return CursorLocation(nextSlot, targetIdx)
    }
    current = info.parentContainer
  }
}

internal fun deleteOrUnwrapAtCursor(root: MathContainer, cursor: CursorLocation): CursorLocation {
  val safeC = if (containsContainer(root, cursor.container)) cursor.container else root
  val i = cursor.index.coerceIn(0, safeC.nodes.size)
  if (i > 0) {
    safeC.nodes.removeAt(i - 1)
    return CursorLocation(safeC, i - 1)
  }
  val info = findParentInfo(root, safeC) ?: return CursorLocation(safeC, 0)
  val pContainer = info.parentContainer
  val nodeIdx = info.nodeIndexInParent
  val pNode = info.parentNode

  val preservedNodes: List<MathNode>
  val newCursorOffset: Int

  when (pNode) {
    is MathNode.FractionNode -> {
      val nNodes = pNode.num.nodes.toList()
      val dNodes = pNode.den.nodes.toList()
      preservedNodes = nNodes + dNodes
      newCursorOffset = if (safeC === pNode.den) nNodes.size else 0
    }
    is MathNode.MixedFractionNode -> {
      val wNodes = pNode.whole.nodes.toList()
      val nNodes = pNode.num.nodes.toList()
      val dNodes = pNode.den.nodes.toList()
      preservedNodes = wNodes + nNodes + dNodes
      newCursorOffset = when (safeC) {
        pNode.num -> wNodes.size
        pNode.den -> wNodes.size + nNodes.size
        else -> 0
      }
    }
    is MathNode.PowerNode -> {
      val bNodes = pNode.base.nodes.toList()
      val eNodes = pNode.exp.nodes.toList()
      preservedNodes = bNodes + eNodes
      newCursorOffset = if (safeC === pNode.exp) bNodes.size else 0
    }
    is MathNode.RootNode -> {
      val iNodes = pNode.index?.nodes?.toList().orEmpty()
      val rNodes = pNode.radicand.nodes.toList()
      preservedNodes = iNodes + rNodes
      newCursorOffset = if (safeC === pNode.radicand) iNodes.size else 0
    }
    is MathNode.LogBaseNode -> {
      val bNodes = pNode.base.nodes.toList()
      val aNodes = pNode.arg.nodes.toList()
      preservedNodes = bNodes + aNodes
      newCursorOffset = if (safeC === pNode.arg) bNodes.size else 0
    }
    is MathNode.DerivativeNode -> {
      val eNodes = pNode.expr.nodes.toList()
      val xNodes = pNode.atX.nodes.toList()
      preservedNodes = eNodes + xNodes
      newCursorOffset = if (safeC === pNode.atX) eNodes.size else 0
    }
    is MathNode.IntegralNode -> {
      val fNodes = pNode.integrand.nodes.toList()
      preservedNodes = fNodes.ifEmpty { pNode.lower.nodes.toList() + pNode.upper.nodes.toList() }
      newCursorOffset = 0
    }
    is MathNode.SummationNode -> {
      val bNodes = pNode.body.nodes.toList()
      preservedNodes = bNodes.ifEmpty { pNode.lower.nodes.toList() + pNode.upper.nodes.toList() }
      newCursorOffset = 0
    }
    is MathNode.AbsNode -> {
      preservedNodes = pNode.inner.nodes.toList()
      newCursorOffset = 0
    }
    is MathNode.TextNode -> {
      preservedNodes = emptyList()
      newCursorOffset = 0
    }
  }

  pContainer.nodes.removeAt(nodeIdx)
  preservedNodes.forEachIndexed { offset, node ->
    pContainer.nodes.add(nodeIdx + offset, node)
  }
  return CursorLocation(pContainer, (nodeIdx + newCursorOffset).coerceIn(0, pContainer.nodes.size))
}

// ============================================================================
// SMART CASIO INSERTION & PRECEDING OPERAND WRAPPING
// ============================================================================

private fun isBinaryOperatorOrDelimiterText(txt: String): Boolean {
  return txt in setOf(
    "+", "-", "−", "×", "÷", "*", "/", "(", ",", ":", "=", "∠", "^",
    " and ", " or ", " xor ", " xnor ", "not(", "neg(", "P", "C", "•"
  ) || txt.endsWith("(")
}

private fun isNumericDigitOrDot(node: MathNode): Boolean {
  if (node !is MathNode.TextNode) return false
  return node.text.length == 1 && (node.text[0].isDigit() || node.text[0] == '.')
}

internal fun extractContiguousOperandBefore(
  container: MathContainer,
  cursorIdx: Int,
  digitsOnly: Boolean = false,
  singleAtomForPower: Boolean = false
): List<MathNode> {
  if (cursorIdx <= 0) return emptyList()
  val nodes = container.nodes
  if (digitsOnly) {
    var start = cursorIdx
    while (start > 0 && isNumericDigitOrDot(nodes[start - 1])) {
      start--
    }
    if (start == cursorIdx) return emptyList()
    val taken = nodes.subList(start, cursorIdx).toList()
    repeat(cursorIdx - start) { nodes.removeAt(start) }
    return taken
  }

  if (singleAtomForPower) {
    var start = cursorIdx
    val lastNode = nodes[cursorIdx - 1]
    when {
      lastNode !is MathNode.TextNode -> {
        start = cursorIdx - 1
      }
      lastNode.text == ")" -> {
        var parenDepth = 0
        while (start > 0) {
          val prev = nodes[start - 1]
          if (prev is MathNode.TextNode) {
            val t = prev.text
            if (t == ")") {
              parenDepth++
              start--
              continue
            }
            if (t == "(" || (t.endsWith("(") && parenDepth > 0)) {
              parenDepth--
              start--
              if (parenDepth == 0) break
              continue
            }
            start--
          } else {
            start--
          }
        }
      }
      isNumericDigitOrDot(lastNode) -> {
        while (start > 0 && isNumericDigitOrDot(nodes[start - 1])) {
          start--
        }
      }
      !isBinaryOperatorOrDelimiterText(lastNode.text) -> {
        // Single variable or named constant atom (e.g. X, π, e, Ans, A..F, i)
        // so `3X` + x² yields `3 * (X²)` rather than `(3X)²`.
        start = cursorIdx - 1
      }
    }
    if (start == cursorIdx) return emptyList()
    val taken = nodes.subList(start, cursorIdx).toList()
    repeat(cursorIdx - start) { nodes.removeAt(start) }
    return taken
  }

  var start = cursorIdx
  var parenDepth = 0

  while (start > 0) {
    val prev = nodes[start - 1]
    if (prev is MathNode.TextNode) {
      val t = prev.text
      if (t == ")") {
        parenDepth++
        start--
        continue
      }
      if (t == "(" || (t.endsWith("(") && parenDepth > 0)) {
        if (parenDepth > 0) {
          parenDepth--
          start--
          if (parenDepth == 0) {
            // Stop once balanced group + function prefix is captured
            break
          }
          continue
        } else {
          break
        }
      }
      if (parenDepth == 0 && isBinaryOperatorOrDelimiterText(t)) {
        break
      }
      start--
    } else {
      start--
      // If a standalone 2D node is at cursorIdx - 1 and parenDepth == 0, keep walking
      // so e.g. `2√3` or `(1+2)` stays cohesive until an operator or `(`.
    }
  }

  if (start == cursorIdx) return emptyList()
  val taken = nodes.subList(start, cursorIdx).toList()
  repeat(cursorIdx - start) { nodes.removeAt(start) }
  return taken
}

internal fun insertSmartFraction(root: MathContainer, cursor: CursorLocation): CursorLocation {
  val safeC = if (containsContainer(root, cursor.container)) cursor.container else root
  val i = cursor.index.coerceIn(0, safeC.nodes.size)
  val lifted = extractContiguousOperandBefore(safeC, i)
  val insertIdx = i - lifted.size
  val numC = MathContainer()
  lifted.forEach { numC.nodes.add(it) }
  val denC = MathContainer()
  val frac = MathNode.FractionNode(numC, denC)
  safeC.nodes.add(insertIdx, frac)
  return if (lifted.isNotEmpty()) {
    CursorLocation(denC, 0)
  } else {
    CursorLocation(numC, 0)
  }
}

internal fun insertSmartMixedFraction(root: MathContainer, cursor: CursorLocation): CursorLocation {
  val safeC = if (containsContainer(root, cursor.container)) cursor.container else root
  val i = cursor.index.coerceIn(0, safeC.nodes.size)
  val liftedWhole = extractContiguousOperandBefore(safeC, i, digitsOnly = true)
  val insertIdx = i - liftedWhole.size
  val wholeC = MathContainer().apply { liftedWhole.forEach { nodes.add(it) } }
  val numC = MathContainer()
  val denC = MathContainer()
  val mixed = MathNode.MixedFractionNode(wholeC, numC, denC)
  safeC.nodes.add(insertIdx, mixed)
  return if (liftedWhole.isNotEmpty()) {
    CursorLocation(numC, 0)
  } else {
    CursorLocation(wholeC, 0)
  }
}

internal fun insertSmartPower(
  root: MathContainer,
  cursor: CursorLocation,
  presetExp: List<String>? = null
): CursorLocation {
  val safeC = if (containsContainer(root, cursor.container)) cursor.container else root
  val i = cursor.index.coerceIn(0, safeC.nodes.size)
  val lifted = extractContiguousOperandBefore(safeC, i, singleAtomForPower = true)
  val insertIdx = i - lifted.size
  val baseC = MathContainer().apply { lifted.forEach { nodes.add(it) } }
  val expC = MathContainer().apply {
    presetExp?.forEach { tok -> nodes.add(MathNode.TextNode(tok)) }
  }
  val powNode = MathNode.PowerNode(baseC, expC)
  safeC.nodes.add(insertIdx, powNode)
  return when {
    lifted.isEmpty() -> CursorLocation(baseC, 0)
    presetExp == null -> CursorLocation(expC, 0)
    else -> CursorLocation(safeC, insertIdx + 1)
  }
}

internal fun insertFixedBasePower(
  root: MathContainer,
  cursor: CursorLocation,
  baseChars: List<String>
): CursorLocation {
  val safeC = if (containsContainer(root, cursor.container)) cursor.container else root
  val i = cursor.index.coerceIn(0, safeC.nodes.size)
  val baseC = MathContainer().apply {
    baseChars.forEach { nodes.add(MathNode.TextNode(it)) }
  }
  val expC = MathContainer()
  val powNode = MathNode.PowerNode(baseC, expC)
  safeC.nodes.add(i, powNode)
  return CursorLocation(expC, 0)
}

internal fun insertSmartNthRoot(root: MathContainer, cursor: CursorLocation): CursorLocation {
  val safeC = if (containsContainer(root, cursor.container)) cursor.container else root
  val i = cursor.index.coerceIn(0, safeC.nodes.size)
  val liftedIdx = extractContiguousOperandBefore(safeC, i)
  val insertIdx = i - liftedIdx.size
  val idxC = MathContainer().apply { liftedIdx.forEach { nodes.add(it) } }
  val radC = MathContainer()
  val rootNode = MathNode.RootNode(index = idxC, radicand = radC)
  safeC.nodes.add(insertIdx, rootNode)
  return if (liftedIdx.isNotEmpty()) {
    CursorLocation(radC, 0)
  } else {
    CursorLocation(idxC, 0)
  }
}

internal fun insertNodeAndFocus(
  root: MathContainer,
  cursor: CursorLocation,
  node: MathNode,
  focusSlot: MathContainer?
): CursorLocation {
  val safeC = if (containsContainer(root, cursor.container)) cursor.container else root
  val i = cursor.index.coerceIn(0, safeC.nodes.size)
  safeC.nodes.add(i, node)
  return if (focusSlot != null) {
    CursorLocation(focusSlot, focusSlot.nodes.size)
  } else {
    CursorLocation(safeC, i + 1)
  }
}

internal fun insertTokenIntoTree(
  root: MathContainer,
  cursor: CursorLocation,
  token: String
): CursorLocation {
  val safeC = if (containsContainer(root, cursor.container)) cursor.container else root
  var i = cursor.index.coerceIn(0, safeC.nodes.size)
  val multiDisplayMap = mapOf(
    "asin(" to "sin⁻¹(",
    "acos(" to "cos⁻¹(",
    "atan(" to "tan⁻¹(",
    "asinh(" to "sinh⁻¹(",
    "acosh(" to "cosh⁻¹(",
    "atanh(" to "tanh⁻¹("
  )
  val mapped = multiDisplayMap[token] ?: token

  when (mapped) {
    "sqrt(" -> {
      val rad = MathContainer()
      return insertNodeAndFocus(root, cursor, MathNode.RootNode(null, rad), rad)
    }
    "cbrt(" -> {
      val rad = MathContainer()
      return insertNodeAndFocus(root, cursor, MathNode.RootNode(null, rad, fixedIndexText = "3"), rad)
    }
    "nroot(" -> return insertSmartNthRoot(root, cursor)
    "logBase(" -> {
      val base = MathContainer()
      val arg = MathContainer()
      return insertNodeAndFocus(root, cursor, MathNode.LogBaseNode(base, arg), base)
    }
    "integral(" -> {
      val lo = MathContainer()
      val hi = MathContainer()
      val body = MathContainer()
      return insertNodeAndFocus(root, cursor, MathNode.IntegralNode(lo, hi, body), lo)
    }
    "deriv(" -> {
      val expr = MathContainer()
      val atX = MathContainer()
      return insertNodeAndFocus(root, cursor, MathNode.DerivativeNode(expr, atX), expr)
    }
    "sum(" -> {
      val lo = MathContainer()
      val hi = MathContainer()
      val body = MathContainer()
      return insertNodeAndFocus(root, cursor, MathNode.SummationNode(lo, hi, body), lo)
    }
    "abs(" -> {
      val inner = MathContainer()
      return insertNodeAndFocus(root, cursor, MathNode.AbsNode(inner), inner)
    }
    "10^" -> return insertFixedBasePower(root, cursor, listOf("1", "0"))
    "exp(" -> return insertFixedBasePower(root, cursor, listOf("e"))
    "²" -> return insertSmartPower(root, cursor, listOf("2"))
    "³" -> return insertSmartPower(root, cursor, listOf("3"))
    "⁻¹" -> return insertSmartPower(root, cursor, listOf("-", "1"))
    "^" -> return insertSmartPower(root, cursor, null)
    "/", "÷" -> {
      if (mapped == "/") return insertSmartFraction(root, cursor)
    }
  }

  // Atomic multi-char function/constant/operator tokens vs digit strings
  val atomicPrefixes = setOf(
    "sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "sinh⁻¹(", "cosh⁻¹(", "tanh⁻¹(",
    "sinh(", "cosh(", "tanh(", "sin(", "cos(", "tan(",
    "log(", "ln(", "Pol(", "Rec(", "arg(", "Conjg(", "ReP(", "ImP(",
    "Det(", "Trn(", "UnitV(", "Angle(", "Rnd(", "RanInt(1,100)", "Ran#",
    "MatA", "MatB", "MatC", "VctA", "VctB", "VctC", "Ans",
    "×10^", " and ", " or ", " xor ", " xnor ", "not(", "neg(",
    "x̂₁", "x̂₂", "x̂", "ŷ", "Σx²", "Σy²", "Σxy", "Σx", "Σy",
    "x̄", "σx", "sx", "ȳ", "σy", "sy",
    "▶r∠θ", "▶a+bi", "►r∠θ", "►a+bi"
  )

  if (mapped in atomicPrefixes || mapped.length == 1) {
    safeC.nodes.add(i, MathNode.TextNode(mapped))
    return CursorLocation(safeC, i + 1)
  }

  // Split plain numeric/text pastes (e.g. physical constants or history results) into atomic TextNodes
  mapped.forEach { ch ->
    safeC.nodes.add(i, MathNode.TextNode(ch.toString()))
    i++
  }
  return CursorLocation(safeC, i)
}

// ============================================================================
// CANONICAL STRING SERIALIZATION (FOR HISTORY & CMPLX/STAT/MATRIX/VECTOR/TABLE)
// ============================================================================

internal fun MathContainer.toCanonicalString(): String {
  return nodes.toCanonicalString()
}

internal fun List<MathNode>.toCanonicalString(): String {
  val sb = StringBuilder()
  for (node in this) {
    sb.append(
      when (node) {
        is MathNode.TextNode -> when (node.text) {
          "sin⁻¹(" -> "asin("
          "cos⁻¹(" -> "acos("
          "tan⁻¹(" -> "atan("
          "sinh⁻¹(" -> "asinh("
          "cosh⁻¹(" -> "acosh("
          "tanh⁻¹(" -> "atanh("
          "−" -> "-"
          else -> node.text
        }
        is MathNode.FractionNode -> "((${node.num.toCanonicalString()})/(${node.den.toCanonicalString()}))"
        is MathNode.MixedFractionNode -> {
          val w = node.whole.toCanonicalString()
          val n = node.num.toCanonicalString()
          val d = node.den.toCanonicalString()
          if (w.trimStart().startsWith("-")) {
            "((${w})-((${n})/(${d})))"
          } else {
            "((${w})+((${n})/(${d})))"
          }
        }
        is MathNode.PowerNode -> {
          val bStr = node.base.toCanonicalString()
          val eStr = node.exp.toCanonicalString()
          if ((bStr == "MatA" || bStr == "MatB" || bStr == "MatC") && eStr == "-1") {
            "${bStr}⁻¹"
          } else {
            "((${bStr})^(${eStr}))"
          }
        }
        is MathNode.RootNode -> when {
          node.fixedIndexText == "3" -> "cbrt(${node.radicand.toCanonicalString()})"
          node.index == null -> "sqrt(${node.radicand.toCanonicalString()})"
          else -> "nroot(${node.index.toCanonicalString()},${node.radicand.toCanonicalString()})"
        }
        is MathNode.LogBaseNode -> "logBase(${node.base.toCanonicalString()},${node.arg.toCanonicalString()})"
        is MathNode.IntegralNode -> "integral(${node.integrand.toCanonicalString()},${node.lower.toCanonicalString()},${node.upper.toCanonicalString()})"
        is MathNode.DerivativeNode -> "deriv(${node.expr.toCanonicalString()},${node.atX.toCanonicalString()})"
        is MathNode.SummationNode -> "sum(${node.body.toCanonicalString()},${node.lower.toCanonicalString()},${node.upper.toCanonicalString()})"
        is MathNode.AbsNode -> "abs(${node.inner.toCanonicalString()})"
      }
    )
  }
  return sb.toString()
}

// ============================================================================
// DIRECT RECURSIVE AST EVALUATOR WITH EXACT ERROR LOCATION TRACKING
// ============================================================================

internal class AstEvalException(
  val errorTitle: String, // "Syntax ERROR" or "Math ERROR"
  val gotoLocation: CursorLocation? = null,
  message: String = errorTitle
) : RuntimeException(message)

private sealed interface AstToken {
  data class NumberLit(val value: Double, val container: MathContainer, val nodeIdx: Int) : AstToken
  data class Operator(val op: String, val container: MathContainer, val nodeIdx: Int) : AstToken
  data class FunctionStart(val name: String, val container: MathContainer, val nodeIdx: Int) : AstToken
  data class Identifier(val name: String, val container: MathContainer, val nodeIdx: Int) : AstToken
  data class NodeValue(val node: MathNode, val container: MathContainer, val nodeIdx: Int) : AstToken
  data class PostfixOp(val op: String, val container: MathContainer, val nodeIdx: Int) : AstToken
}

private fun tokenizeContainerNodes(container: MathContainer, nodes: List<MathNode>): List<AstToken> {
  val tokens = mutableListOf<AstToken>()
  var i = 0
  while (i < nodes.size) {
    val node = nodes[i]
    if (node !is MathNode.TextNode) {
      tokens.add(AstToken.NodeValue(node, container, i))
      i++
      continue
    }

    val t = node.text
    // Numeric literal accumulation across contiguous single-digit / dot TextNodes
    if (t.length == 1 && (t[0].isDigit() || t[0] == '.')) {
      val startIdx = i
      val numSb = StringBuilder()
      while (i < nodes.size) {
        val cur = nodes[i]
        if (cur is MathNode.TextNode && cur.text.length == 1 && (cur.text[0].isDigit() || cur.text[0] == '.')) {
          numSb.append(cur.text)
          i++
        } else break
      }
      // Scientific notation if user pasted e.g. "1.67E-27"
      if (i < nodes.size) {
        val eNode = nodes[i]
        if (eNode is MathNode.TextNode && (eNode.text == "E" || eNode.text == "e") && i + 1 < nodes.size) {
          val after = nodes[i + 1]
          val after2 = nodes.getOrNull(i + 2)
          if (after is MathNode.TextNode && (after.text.length == 1 && after.text[0].isDigit())) {
            numSb.append('E').append(after.text)
            i += 2
            while (i < nodes.size) {
              val d = nodes[i]
              if (d is MathNode.TextNode && d.text.length == 1 && d.text[0].isDigit()) {
                numSb.append(d.text); i++
              } else break
            }
          } else if (after is MathNode.TextNode && (after.text == "-" || after.text == "+") &&
            after2 is MathNode.TextNode && after2.text.length == 1 && after2.text[0].isDigit()
          ) {
            numSb.append('E').append(after.text)
            i += 2
            while (i < nodes.size) {
              val d = nodes[i]
              if (d is MathNode.TextNode && d.text.length == 1 && d.text[0].isDigit()) {
                numSb.append(d.text); i++
              } else break
            }
          }
        }
      }
      val parsed = numSb.toString().toDoubleOrNull()
        ?: throw AstEvalException("Syntax ERROR", CursorLocation(container, startIdx))
      tokens.add(AstToken.NumberLit(parsed, container, startIdx))
      continue
    }

    when {
      t == "×10^" -> {
        tokens.add(AstToken.Operator("×10^", container, i))
      }
      t in setOf("+", "-", "−", "×", "*", "÷", "/", "^", "(", ")", ",", "=") -> {
        val normOp = when (t) {
          "−" -> "-"
          "*" -> "×"
          "/" -> "÷"
          else -> t
        }
        tokens.add(AstToken.Operator(normOp, container, i))
      }
      t == "P" || t == "C" -> {
        // Disambiguate variable P/C? Variables only A-F, X, Y, M. 'P' is always nPr; 'C' is nCr if between operands!
        val prevVal = tokens.lastOrNull()
        val prevIsVal = prevVal is AstToken.NumberLit || prevVal is AstToken.NodeValue ||
          (prevVal is AstToken.Operator && prevVal.op == ")") ||
          prevVal is AstToken.Identifier || prevVal is AstToken.PostfixOp
        if (t == "C" && prevIsVal) {
          tokens.add(AstToken.Operator("C", container, i))
        } else if (t == "P" && prevIsVal) {
          tokens.add(AstToken.Operator("P", container, i))
        } else {
          tokens.add(AstToken.Identifier(t, container, i))
        }
      }
      t in setOf("!", "%", "²", "³", "⁻¹", "°", "ʳ", "ᵍ") -> {
        tokens.add(AstToken.PostfixOp(t, container, i))
      }
      t == "RanInt(1,100)" -> {
        val v = Random.nextInt(1, 101).toDouble()
        tokens.add(AstToken.NumberLit(v, container, i))
      }
      t == "Ran#" -> {
        tokens.add(AstToken.NumberLit(Random.nextDouble(), container, i))
      }
      t.endsWith("(") -> {
        tokens.add(AstToken.FunctionStart(t.removeSuffix("("), container, i))
      }
      else -> {
        tokens.add(AstToken.Identifier(t, container, i))
      }
    }
    i++
  }
  return tokens
}

internal fun evaluateMathAst(
  container: MathContainer,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit
): Double {
  return evalRealContainer(container, emptyMap(), vars, angleUnit)
}

internal fun evalRealContainer(
  container: MathContainer,
  localVars: Map<String, Double> = emptyMap(),
  vars: Map<String, Double> = emptyMap(),
  angleUnit: ClassWizAngleUnit = ClassWizAngleUnit.DEG
): Double {
  return evaluateNodeListAst(container, container.nodes, vars, angleUnit, localVars)
}

internal fun evaluateNodeListAst(
  ownerContainer: MathContainer,
  nodes: List<MathNode>,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit,
  localVars: Map<String, Double> = emptyMap()
): Double {
  val emptySlot = findFirstEmptySlotInList(nodes)
  if (emptySlot != null) {
    throw AstEvalException("Syntax ERROR", CursorLocation(emptySlot, 0))
  }
  if (nodes.isEmpty()) {
    throw AstEvalException("Syntax ERROR", CursorLocation(ownerContainer, 0))
  }
  val tokens = tokenizeContainerNodes(ownerContainer, nodes)
  return AstTokenParser(ownerContainer, tokens, vars, angleUnit, localVars).parse()
}

private fun findFirstEmptySlotInList(nodes: List<MathNode>): MathContainer? {
  for (n in nodes) {
    for (slot in n.childSlots()) {
      if (slot.nodes.isEmpty()) return slot
      val deeper = findFirstEmptySlot(slot, checkRootNonEmpty = false)
      if (deeper != null) return deeper
    }
  }
  return null
}

private class AstTokenParser(
  private val ownerContainer: MathContainer,
  private val tokens: List<AstToken>,
  private val vars: Map<String, Double>,
  private val angleUnit: ClassWizAngleUnit,
  private val localVars: Map<String, Double> = emptyMap()
) {
  private var pos = 0

  fun parse(): Double {
    val res = parseExpr()
    if (pos < tokens.size) {
      val bad = tokens[pos]
      throw AstEvalException("Syntax ERROR", tokenCursor(bad))
    }
    if (res.isNaN() || res.isInfinite()) {
      throw AstEvalException("Math ERROR", CursorLocation(ownerContainer, ownerContainer.nodes.size))
    }
    return res
  }

  private fun tokenCursor(tok: AstToken): CursorLocation = when (tok) {
    is AstToken.NumberLit -> CursorLocation(tok.container, tok.nodeIdx)
    is AstToken.Operator -> CursorLocation(tok.container, tok.nodeIdx)
    is AstToken.FunctionStart -> CursorLocation(tok.container, tok.nodeIdx)
    is AstToken.Identifier -> CursorLocation(tok.container, tok.nodeIdx)
    is AstToken.NodeValue -> CursorLocation(tok.container, tok.nodeIdx)
    is AstToken.PostfixOp -> CursorLocation(tok.container, tok.nodeIdx)
  }

  private fun peek(): AstToken? = tokens.getOrNull(pos)

  private fun matchOp(op: String): Boolean {
    val t = peek()
    if (t is AstToken.Operator && t.op == op) {
      pos++
      return true
    }
    return false
  }

  private fun canStartImplicitPrimary(tok: AstToken?): Boolean = when (tok) {
    is AstToken.NumberLit,
    is AstToken.FunctionStart,
    is AstToken.Identifier,
    is AstToken.NodeValue -> true
    is AstToken.Operator -> tok.op == "("
    else -> false
  }

  private fun parseExpr(): Double {
    var x = parseTerm()
    while (true) {
      x = when {
        matchOp("+") -> x + parseTerm()
        matchOp("-") -> x - parseTerm()
        else -> return x
      }
    }
  }

  private fun parseTerm(): Double {
    var x = parseSciOrPower()
    while (true) {
      val t = peek() ?: return x
      x = when {
        t is AstToken.Operator && t.op == "×" -> {
          pos++
          x * parseSciOrPower()
        }
        t is AstToken.Operator && t.op == "÷" -> {
          pos++
          val divisorTok = peek()
          val d = parseSciOrPower()
          if (d == 0.0) {
            val loc = divisorTok?.let { tokenCursor(it) } ?: tokenCursor(t)
            throw AstEvalException("Math ERROR", loc, "Math ERROR: Divide by 0")
          }
          x / d
        }
        t is AstToken.Operator && t.op == "P" -> {
          pos++
          permutation(x, parseSciOrPower(), tokenCursor(t))
        }
        t is AstToken.Operator && t.op == "C" -> {
          pos++
          combination(x, parseSciOrPower(), tokenCursor(t))
        }
        canStartImplicitPrimary(t) -> {
          x * parseSciOrPower()
        }
        else -> return x
      }
    }
  }

  private fun parseSciOrPower(): Double {
    var x = parsePower()
    while (true) {
      val t = peek()
      if (t is AstToken.Operator && t.op == "×10^") {
        pos++
        val expVal = parseUnary()
        val scale = 10.0.pow(expVal)
        if (scale.isNaN() || scale.isInfinite()) {
          throw AstEvalException("Math ERROR", tokenCursor(t))
        }
        x *= scale
      } else {
        return x
      }
    }
  }

  private fun parsePower(): Double {
    val base = parseUnary()
    val t = peek()
    if (t is AstToken.Operator && t.op == "^") {
      pos++
      val expVal = parsePower()
      val res = base.pow(expVal)
      if (res.isNaN() || res.isInfinite()) {
        throw AstEvalException("Math ERROR", tokenCursor(t))
      }
      return res
    }
    return base
  }

  private fun parseUnary(): Double {
    return when {
      matchOp("+") -> parseUnary()
      matchOp("-") -> -parseUnary()
      else -> parsePostfix()
    }
  }

  private fun parsePostfix(): Double {
    var x = parsePrimary()
    while (true) {
      val t = peek()
      if (t !is AstToken.PostfixOp) break
      pos++
      x = when (t.op) {
        "!" -> factorial(x, tokenCursor(t))
        "%" -> x * 0.01
        "²" -> x * x
        "³" -> x * x * x
        "⁻¹" -> {
          if (x == 0.0) throw AstEvalException("Math ERROR", tokenCursor(t))
          1.0 / x
        }
        "°" -> parseDmsContinuation(x)
        "ʳ" -> angleUnit.fromRadians(x)
        "ᵍ" -> angleUnit.fromRadians(x * PI / 200.0)
        else -> x
      }
    }
    return x
  }

  private fun parseDmsContinuation(degPart: Double): Double {
    val sign = if (degPart < 0.0) -1.0 else 1.0
    var total = abs(degPart)
    val minTok = peek()
    if (minTok is AstToken.NumberLit) {
      pos++
      total += minTok.value / 60.0
      val post1 = peek()
      if (post1 is AstToken.PostfixOp && post1.op == "°") {
        pos++
        val secTok = peek()
        if (secTok is AstToken.NumberLit) {
          pos++
          total += secTok.value / 3600.0
          val post2 = peek()
          if (post2 is AstToken.PostfixOp && post2.op == "°") {
            pos++
          }
        }
      }
    }
    return sign * total
  }

  private fun parsePrimary(): Double {
    val t = peek() ?: throw AstEvalException("Syntax ERROR", CursorLocation(ownerContainer, ownerContainer.nodes.size))
    when (t) {
      is AstToken.Operator -> {
        if (t.op == "(") {
          pos++
          val inner = parseExpr()
          if (!matchOp(")")) {
            throw AstEvalException("Syntax ERROR", CursorLocation(t.container, ownerContainer.nodes.size))
          }
          return inner
        }
        throw AstEvalException("Syntax ERROR", tokenCursor(t))
      }
      is AstToken.NumberLit -> {
        pos++
        return t.value
      }
      is AstToken.Identifier -> {
        pos++
        return when (t.name) {
          "π", "pi" -> PI
          "e" -> kotlin.math.E
          "Ans" -> localVars["Ans"] ?: vars["Ans"] ?: 0.0
          "X", "x" -> localVars["X"] ?: localVars["x"] ?: vars["X"] ?: vars["x"] ?: 0.0
          "A", "B", "C", "D", "E", "F", "Y", "M" -> localVars[t.name] ?: vars[t.name] ?: 0.0
          else -> throw AstEvalException("Syntax ERROR", tokenCursor(t))
        }
      }
      is AstToken.FunctionStart -> {
        pos++
        val args = mutableListOf<Double>()
        val closeCheck = peek()
        if (closeCheck is AstToken.Operator && closeCheck.op == ")") {
          throw AstEvalException("Syntax ERROR", tokenCursor(t))
        }
        args.add(parseExpr())
        while (matchOp(",")) {
          args.add(parseExpr())
        }
        if (!matchOp(")")) {
          throw AstEvalException("Syntax ERROR", CursorLocation(t.container, t.nodeIdx + 1))
        }
        return evalAstFunction(t.name, args, tokenCursor(t))
      }
      is AstToken.NodeValue -> {
        pos++
        return eval2DNode(t.node, t.container, t.nodeIdx)
      }
      is AstToken.PostfixOp -> {
        throw AstEvalException("Syntax ERROR", tokenCursor(t))
      }
    }
  }

  private fun eval2DNode(node: MathNode, parent: MathContainer, idx: Int): Double {
    return when (node) {
      is MathNode.TextNode -> 0.0
      is MathNode.FractionNode -> {
        val n = evalRealContainer(node.num, localVars, vars, angleUnit)
        val d = evalRealContainer(node.den, localVars, vars, angleUnit)
        if (d == 0.0) throw AstEvalException("Math ERROR", CursorLocation(node.den, 0))
        n / d
      }
      is MathNode.MixedFractionNode -> {
        val w = evalRealContainer(node.whole, localVars, vars, angleUnit)
        val n = evalRealContainer(node.num, localVars, vars, angleUnit)
        val d = evalRealContainer(node.den, localVars, vars, angleUnit)
        if (d == 0.0) throw AstEvalException("Math ERROR", CursorLocation(node.den, 0))
        if (w < 0.0) w - (n / d) else w + (n / d)
      }
      is MathNode.PowerNode -> {
        val b = evalRealContainer(node.base, localVars, vars, angleUnit)
        val e = evalRealContainer(node.exp, localVars, vars, angleUnit)
        val res = b.pow(e)
        if (res.isNaN() || res.isInfinite()) {
          throw AstEvalException("Math ERROR", CursorLocation(node.exp, 0))
        }
        res
      }
      is MathNode.RootNode -> {
        val rad = evalRealContainer(node.radicand, localVars, vars, angleUnit)
        when {
          node.fixedIndexText == "3" -> cbrt(rad)
          node.index == null -> {
            if (rad < 0.0) throw AstEvalException("Math ERROR", CursorLocation(node.radicand, 0))
            sqrt(rad)
          }
          else -> {
            val nVal = evalRealContainer(node.index, localVars, vars, angleUnit)
            if (nVal == 0.0) throw AstEvalException("Math ERROR", CursorLocation(node.index, 0))
            if (rad < 0.0) {
              val nInt = nVal.roundToInt()
              if (abs(nVal - nInt) < 1e-11 && nInt % 2 != 0) {
                -abs(rad).pow(1.0 / nVal)
              } else {
                throw AstEvalException("Math ERROR", CursorLocation(node.radicand, 0))
              }
            } else {
              rad.pow(1.0 / nVal)
            }
          }
        }
      }
      is MathNode.LogBaseNode -> {
        val b = evalRealContainer(node.base, localVars, vars, angleUnit)
        val a = evalRealContainer(node.arg, localVars, vars, angleUnit)
        if (b <= 0.0 || abs(b - 1.0) < 1e-13) {
          throw AstEvalException("Math ERROR", CursorLocation(node.base, 0))
        }
        if (a <= 0.0) {
          throw AstEvalException("Math ERROR", CursorLocation(node.arg, 0))
        }
        ln(a) / ln(b)
      }
      is MathNode.IntegralNode -> {
        if (node.lower.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.lower, 0))
        if (node.upper.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.upper, 0))
        if (node.integrand.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.integrand, 0))
        val a = evalRealContainer(node.lower, localVars, vars, angleUnit)
        val b = evalRealContainer(node.upper, localVars, vars, angleUnit)
        integrateAstGaussKronrod(node.integrand, a, b, localVars, vars, angleUnit)
      }
      is MathNode.DerivativeNode -> {
        if (node.expr.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.expr, 0))
        if (node.atX.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.atX, 0))
        val x0 = evalRealContainer(node.atX, localVars, vars, angleUnit)
        differentiateAstCentral(node.expr, x0, localVars, vars, angleUnit)
      }
      is MathNode.SummationNode -> {
        if (node.lower.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.lower, 0))
        if (node.upper.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.upper, 0))
        if (node.body.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.body, 0))
        val a = evalRealContainer(node.lower, localVars, vars, angleUnit).roundToInt()
        val b = evalRealContainer(node.upper, localVars, vars, angleUnit).roundToInt()
        if (abs(b - a) > 10000) {
          throw AstEvalException("Math ERROR", CursorLocation(node.upper, 0))
        }
        var acc = 0.0
        val step = if (a <= b) 1 else -1
        var k = a
        while (true) {
          val bound = localVars + ("X" to k.toDouble()) + ("x" to k.toDouble())
          acc += evalRealContainer(node.body, bound, vars, angleUnit)
          if (k == b) break
          k += step
        }
        acc
      }
      is MathNode.AbsNode -> {
        abs(evalRealContainer(node.inner, localVars, vars, angleUnit))
      }
    }
  }

  private fun evalAstFunction(name: String, args: List<Double>, loc: CursorLocation): Double {
    fun req1(): Double = args.firstOrNull() ?: throw AstEvalException("Syntax ERROR", loc)
    return when (name) {
      "sin" -> sin(angleUnit.toRadians(req1()))
      "cos" -> cos(angleUnit.toRadians(req1()))
      "tan" -> {
        val rad = angleUnit.toRadians(req1())
        if (abs(cos(rad)) < 1e-13) throw AstEvalException("Math ERROR", loc)
        tan(rad)
      }
      "asin", "sin⁻¹" -> {
        val v = req1()
        if (v < -1.0 || v > 1.0) throw AstEvalException("Math ERROR", loc)
        angleUnit.fromRadians(asin(v))
      }
      "acos", "cos⁻¹" -> {
        val v = req1()
        if (v < -1.0 || v > 1.0) throw AstEvalException("Math ERROR", loc)
        angleUnit.fromRadians(acos(v))
      }
      "atan", "tan⁻¹" -> angleUnit.fromRadians(atan(req1()))
      "sinh" -> sinh(req1())
      "cosh" -> cosh(req1())
      "tanh" -> tanh(req1())
      "asinh", "sinh⁻¹" -> asinh(req1())
      "acosh", "cosh⁻¹" -> {
        val v = req1()
        if (v < 1.0) throw AstEvalException("Math ERROR", loc)
        acosh(v)
      }
      "atanh", "tanh⁻¹" -> {
        val v = req1()
        if (v <= -1.0 || v >= 1.0) throw AstEvalException("Math ERROR", loc)
        atanh(v)
      }
      "log" -> {
        val v = req1()
        if (v <= 0.0) throw AstEvalException("Math ERROR", loc)
        log10(v)
      }
      "ln" -> {
        val v = req1()
        if (v <= 0.0) throw AstEvalException("Math ERROR", loc)
        ln(v)
      }
      "sqrt" -> {
        val v = req1()
        if (v < 0.0) throw AstEvalException("Math ERROR", loc)
        sqrt(v)
      }
      "cbrt" -> cbrt(req1())
      "abs" -> abs(req1())
      "exp" -> exp(req1())
      "Pol" -> {
        if (args.size < 2) throw AstEvalException("Syntax ERROR", loc)
        hypot(args[0], args[1])
      }
      "Rec" -> {
        if (args.size < 2) throw AstEvalException("Syntax ERROR", loc)
        args[0] * cos(angleUnit.toRadians(args[1]))
      }
      "Rnd" -> {
        val v = req1()
        String.format(Locale.US, "%.4f", v).toDoubleOrNull() ?: v
      }
      "RanInt" -> {
        if (args.size < 2) throw AstEvalException("Syntax ERROR", loc)
        val lo = min(args[0].roundToInt(), args[1].roundToInt())
        val hi = max(args[0].roundToInt(), args[1].roundToInt())
        Random.nextInt(lo, hi + 1).toDouble()
      }
      else -> throw AstEvalException("Syntax ERROR", loc)
    }
  }

  private fun factorial(x: Double, loc: CursorLocation): Double {
    val n = x.roundToInt()
    if (x < 0.0 || abs(x - n) > 1e-9 || n > 170) throw AstEvalException("Math ERROR", loc)
    var res = 1.0
    for (i in 2..n) res *= i.toDouble()
    return res
  }

  private fun permutation(nVal: Double, rVal: Double, loc: CursorLocation): Double {
    val n = nVal.roundToInt()
    val r = rVal.roundToInt()
    if (n < 0 || r < 0 || r > n || abs(nVal - n) > 1e-9 || abs(rVal - r) > 1e-9) {
      throw AstEvalException("Math ERROR", loc)
    }
    var res = 1.0
    for (i in 0 until r) res *= (n - i).toDouble()
    return res
  }

  private fun combination(nVal: Double, rVal: Double, loc: CursorLocation): Double {
    val n = nVal.roundToInt()
    val r = rVal.roundToInt()
    if (n < 0 || r < 0 || r > n || abs(nVal - n) > 1e-9 || abs(rVal - r) > 1e-9) {
      throw AstEvalException("Math ERROR", loc)
    }
    val k = min(r, n - r)
    var res = 1.0
    for (i in 1..k) res = res * (n - k + i).toDouble() / i.toDouble()
    return res
  }
}

// ----------------------------------------------------------------------------
// Adaptive 15-point Gauss-Kronrod Quadrature & Central Difference (h = 1e-7)
// ----------------------------------------------------------------------------

private val GK_NODES = doubleArrayOf(
  0.0,
  0.2077849550078985,
  0.4058451513773972,
  0.5860872354676911,
  0.7415311855993944,
  0.8648644233597691,
  0.9491079123427585,
  0.9914553711208126
)

private val GK_W15 = doubleArrayOf(
  0.2094821410847278,
  0.2044329400752989,
  0.1903505780647854,
  0.1690047266392679,
  0.1406532597155259,
  0.1047900103222502,
  0.06309209262997855,
  0.02293532201052922
)

private val GK_W7 = doubleArrayOf(
  0.4179591836734694,
  0.0,
  0.3818300505051189,
  0.0,
  0.2797053914892766,
  0.0,
  0.1294849661688697,
  0.0
)

private fun integrateAstGaussKronrod(
  integrand: MathContainer,
  aRaw: Double,
  bRaw: Double,
  localVars: Map<String, Double>,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit
): Double {
  if (aRaw == bRaw) return 0.0
  val sign = if (aRaw > bRaw) -1.0 else 1.0
  val a = min(aRaw, bRaw)
  val b = max(aRaw, bRaw)

  fun f(x: Double): Double =
    evalRealContainer(integrand, localVars + ("X" to x) + ("x" to x), vars, angleUnit)

  fun gkStep(x0: Double, x1: Double): Pair<Double, Double> {
    val mid = 0.5 * (x0 + x1)
    val halfLen = 0.5 * (x1 - x0)
    val fCenter = f(mid)
    var resK15 = GK_W15[0] * fCenter
    var resG7 = GK_W7[0] * fCenter
    for (j in 1..7) {
      val absc = halfLen * GK_NODES[j]
      val fSum = f(mid - absc) + f(mid + absc)
      resK15 += GK_W15[j] * fSum
      if (j % 2 == 0) {
        resG7 += GK_W7[j] * fSum
      }
    }
    resK15 *= halfLen
    resG7 *= halfLen
    return resK15 to abs(resK15 - resG7)
  }

  fun recurse(x0: Double, x1: Double, tol: Double, depth: Int): Double {
    val (k15, err) = gkStep(x0, x1)
    if (err <= tol || depth >= 10) return k15
    val mid = 0.5 * (x0 + x1)
    return recurse(x0, mid, tol * 0.5, depth + 1) +
      recurse(mid, x1, tol * 0.5, depth + 1)
  }

  return sign * recurse(a, b, tol = 1e-9, depth = 0)
}

private fun differentiateAstCentral(
  expr: MathContainer,
  x0: Double,
  localVars: Map<String, Double>,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit
): Double {
  fun f(x: Double): Double =
    evalRealContainer(expr, localVars + ("X" to x) + ("x" to x), vars, angleUnit)
  val h = 1e-5 * max(1.0, abs(x0))
  return (-f(x0 + 2.0 * h) + 8.0 * f(x0 + h) - 8.0 * f(x0 - h) + f(x0 - 2.0 * h)) / (12.0 * h)
}

internal fun solveAstEquationForX(
  lhsNodes: List<MathNode>,
  rhsNodes: List<MathNode>,
  owner: MathContainer,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit
): Double {
  val initialGuess = vars["X"] ?: 0.0
  val seeds = listOf(initialGuess, 0.0, 1.0, -1.0, 2.5, -2.5, 10.0, -10.0, 0.1, 100.0).distinct()

  fun evalDiff(x: Double): Double {
    val l = evaluateNodeListAst(owner, lhsNodes, vars + ("X" to x), angleUnit)
    val r = evaluateNodeListAst(owner, rhsNodes, vars + ("X" to x), angleUnit)
    return l - r
  }

  for (seed in seeds) {
    var x = seed
    for (iter in 0 until 80) {
      val fx = try { evalDiff(x) } catch (_: Throwable) { Double.NaN }
      if (fx.isNaN() || fx.isInfinite()) break
      if (abs(fx) < 1e-10) return x
      val h = 1e-6 * max(1.0, abs(x))
      val dfx = try { (evalDiff(x + h) - evalDiff(x - h)) / (2.0 * h) } catch (_: Throwable) { Double.NaN }
      if (dfx.isNaN() || abs(dfx) < 1e-13) {
        x += 0.37
        continue
      }
      val step = (fx / dfx).coerceIn(-1e4, 1e4)
      x -= step
      if (abs(step) < 1e-12) {
        val check = runCatching { evalDiff(x) }.getOrDefault(1.0)
        if (abs(check) < 1e-7) return x
      }
    }
  }
  throw AstEvalException("Math ERROR", CursorLocation(owner, 0), "Can't Solve")
}

// ============================================================================
// 2D NATURAL TEXTBOOK DISPLAY (MathO) ANSWER AST BUILDERS
// ============================================================================

private fun astRationalApprox(target: Double, maxDenom: Long = 1000L): Pair<Long, Long>? {
  if (target.isNaN() || target.isInfinite() || abs(target) > 1e7) return null
  val sign = if (target < 0) -1L else 1L
  var x = abs(target)
  var h1 = 1L
  var h0 = 0L
  var k1 = 0L
  var k0 = 1L
  var iter = 0
  while (iter < 24) {
    val a = floor(x).toLong()
    val h2 = a * h1 + h0
    val k2 = a * k1 + k0
    if (k2 > maxDenom) break
    h0 = h1
    h1 = h2
    k0 = k1
    k1 = k2
    val frac = x - a
    if (frac < 1e-11) break
    x = 1.0 / frac
    iter++
  }
  if (k1 <= 0L) return null
  val approx = h1.toDouble() / k1.toDouble()
  return if (abs(abs(target) - approx) < 1e-9) (sign * h1) to k1 else null
}

internal fun buildDecimalOrSciAnswerTree(
  value: Double,
  format: ClassWizNumberFormat = ClassWizNumberFormat.NORM,
  fixDigits: Int = 4,
  engShift: Int = 0
): MathContainer {
  val raw = formatDecimalCompact(value, format, fixDigits, engShift)
  return parseSciStringToMathContainer(raw)
}

internal fun parseSciStringToMathContainer(raw: String): MathContainer {
  val sciIdx = raw.indexOf("×10^")
  if (sciIdx >= 0) {
    val mantissa = raw.substring(0, sciIdx)
    val expStr = raw.substring(sciIdx + 4).trim()
    val container = MathContainer()
    mantissa.forEach { ch -> container.nodes.add(MathNode.TextNode(ch.toString())) }
    container.nodes.add(MathNode.TextNode("×"))
    val baseC = mathContainerOfText("10")
    val expC = mathContainerOfText(expStr)
    container.nodes.add(MathNode.PowerNode(baseC, expC))
    return container
  }

  // Also parse Java E-notation (used in SCI format e.g. 1.250E-06 or 2.500E+03)
  val eMatch = Regex("^([+-]?[0-9]+(?:\\.[0-9]+)?)[eE]([+-]?[0-9]+)$").matchEntire(raw.trim())
  if (eMatch != null) {
    val mantissa = eMatch.groupValues[1]
    val expNum = eMatch.groupValues[2].toIntOrNull()
    if (expNum != null) {
      val container = MathContainer()
      mantissa.forEach { ch -> container.nodes.add(MathNode.TextNode(ch.toString())) }
      container.nodes.add(MathNode.TextNode("×"))
      val baseC = mathContainerOfText("10")
      val expC = mathContainerOfText(expNum.toString())
      container.nodes.add(MathNode.PowerNode(baseC, expC))
      return container
    }
  }

  return mathContainerOfText(raw)
}

internal fun buildExactSymbolicAnswerTree(
  value: Double,
  mixedMode: Boolean = false
): MathContainer? {
  if (value.isNaN() || value.isInfinite()) return null
  val v = cleanZero(value)
  if (v == 0.0) return mathContainerOfText("0")

  // 1. Exact integer or rational p/q
  astRationalApprox(v, maxDenom = 1000L)?.let { (n, d) ->
    val c = MathContainer()
    if (d == 1L) {
      n.toString().forEach { ch -> c.nodes.add(MathNode.TextNode(ch.toString())) }
      return c
    }
    if (mixedMode && abs(n) > d) {
      if (n < 0) c.nodes.add(MathNode.TextNode("-"))
      val absN = abs(n)
      val whole = absN / d
      val rem = absN % d
      if (rem == 0L) {
        whole.toString().forEach { ch -> c.nodes.add(MathNode.TextNode(ch.toString())) }
      } else {
        c.nodes.add(
          MathNode.MixedFractionNode(
            whole = mathContainerOfText(whole.toString()),
            num = mathContainerOfText(rem.toString()),
            den = mathContainerOfText(d.toString())
          )
        )
      }
      return c
    }
    if (n < 0) c.nodes.add(MathNode.TextNode("-"))
    c.nodes.add(
      MathNode.FractionNode(
        num = mathContainerOfText(abs(n).toString()),
        den = mathContainerOfText(d.toString())
      )
    )
    return c
  }

  // 2. Exact rational multiple of pi: (p/q) * pi
  astRationalApprox(v / PI, maxDenom = 36L)?.let { (n, d) ->
    if (n != 0L) {
      val c = MathContainer()
      if (n < 0) c.nodes.add(MathNode.TextNode("-"))
      val absN = abs(n)
      val topNodes = MathContainer().apply {
        if (absN > 1L) {
          absN.toString().forEach { ch -> nodes.add(MathNode.TextNode(ch.toString())) }
        }
        nodes.add(MathNode.TextNode("π"))
      }
      if (d == 1L) {
        c.nodes.addAll(topNodes.nodes)
      } else {
        c.nodes.add(MathNode.FractionNode(topNodes, mathContainerOfText(d.toString())))
      }
      return c
    }
  }

  // 3. Pure radical: (a * sqrt(b)) / c
  val radBases = listOf(2, 3, 5, 6, 7, 10, 11, 13, 14, 15)
  for (b in radBases) {
    val ratio = v / sqrt(b.toDouble())
    astRationalApprox(ratio, maxDenom = 24L)?.let { (a, cDen) ->
      if (a != 0L) {
        val c = MathContainer()
        if (a < 0) c.nodes.add(MathNode.TextNode("-"))
        val absA = abs(a)
        val topNodes = MathContainer().apply {
          if (absA > 1L) {
            absA.toString().forEach { ch -> nodes.add(MathNode.TextNode(ch.toString())) }
          }
          nodes.add(MathNode.RootNode(index = null, radicand = mathContainerOfText(b.toString())))
        }
        if (cDen == 1L) {
          c.nodes.addAll(topNodes.nodes)
        } else {
          c.nodes.add(MathNode.FractionNode(topNodes, mathContainerOfText(cDen.toString())))
        }
        return c
      }
    }
  }

  // 4. Quadratic surd: (a ± b*sqrt(k)) / c
  val smallRad = listOf(2, 3, 5, 6)
  for (cDen in 1..6) {
    val scaled = v * cDen
    for (k in smallRad) {
      val sk = sqrt(k.toDouble())
      for (b in -10..10) {
        if (b == 0) continue
        val rem = scaled - b * sk
        val a = rem.roundToLong()
        if (abs(rem - a) < 1e-8 && abs(a) <= 16) {
          val numC = MathContainer()
          if (a == 0L) {
            if (b < 0) numC.nodes.add(MathNode.TextNode("-"))
          } else {
            a.toString().forEach { ch -> numC.nodes.add(MathNode.TextNode(ch.toString())) }
            numC.nodes.add(MathNode.TextNode(if (b > 0) "+" else "-"))
          }
          val absB = abs(b)
          if (absB > 1) {
            absB.toString().forEach { ch -> numC.nodes.add(MathNode.TextNode(ch.toString())) }
          }
          numC.nodes.add(MathNode.RootNode(index = null, radicand = mathContainerOfText(k.toString())))

          return if (cDen == 1) {
            numC
          } else {
            MathContainer().apply {
              nodes.add(MathNode.FractionNode(numC, mathContainerOfText(cDen.toString())))
            }
          }
        }
      }
    }
  }

  return null
}

internal fun buildCompAnswerTree(
  value: Double,
  isExact: Boolean,
  isMixedFraction: Boolean,
  isDmsOutput: Boolean,
  format: ClassWizNumberFormat,
  fixDigits: Int,
  engShift: Int
): MathContainer {
  val v = cleanZero(value)
  if (v.isNaN() || v.isInfinite()) return mathContainerOfText("Math ERROR")
  if (isDmsOutput) return mathContainerOfText(formatDmsOutput(v))
  if (isExact && engShift == 0 && format == ClassWizNumberFormat.NORM) {
    buildExactSymbolicAnswerTree(v, isMixedFraction)?.let { return it }
  }
  return buildDecimalOrSciAnswerTree(v, format, fixDigits, engShift)
}

internal fun buildComplexAnswerTree(
  z: ComplexNum,
  polarMode: Boolean,
  angleUnit: ClassWizAngleUnit,
  exactMode: Boolean,
  format: ClassWizNumberFormat,
  fixDigits: Int
): MathContainer {
  if (z.re.isNaN() || z.im.isNaN() || z.re.isInfinite() || z.im.isInfinite()) {
    return mathContainerOfText("Math ERROR")
  }
  val re = cleanZero(z.re)
  val im = cleanZero(z.im)

  fun partTree(v: Double): MathContainer {
    if (exactMode && format == ClassWizNumberFormat.NORM) {
      buildExactSymbolicAnswerTree(v, mixedMode = false)?.let { return it }
    }
    return buildDecimalOrSciAnswerTree(v, format, fixDigits, engShift = 0)
  }

  val out = MathContainer()
  if (polarMode) {
    val r = z.abs()
    val th = cleanZero(angleUnit.fromRadians(z.argRad()))
    out.nodes.addAll(partTree(r).nodes)
    out.nodes.add(MathNode.TextNode("∠"))
    out.nodes.addAll(partTree(th).nodes)
    if (angleUnit == ClassWizAngleUnit.DEG) {
      out.nodes.add(MathNode.TextNode("°"))
    }
    return out
  }

  return when {
    im == 0.0 -> partTree(re)
    re == 0.0 -> when (im) {
      1.0 -> mathContainerOfText("i")
      -1.0 -> mathContainerOfText("-i")
      else -> {
        out.nodes.addAll(partTree(im).nodes)
        out.nodes.add(MathNode.TextNode("i"))
        out
      }
    }
    else -> {
      out.nodes.addAll(partTree(re).nodes)
      out.nodes.add(MathNode.TextNode(if (im > 0) "+" else "-"))
      val absIm = abs(im)
      if (abs(absIm - 1.0) >= 1e-11) {
        out.nodes.addAll(partTree(absIm).nodes)
      }
      out.nodes.add(MathNode.TextNode("i"))
      out
    }
  }
}

// ============================================================================
// 2D NATURAL TEXTBOOK AST EVALUATOR FOR CMPLX MODE
// ============================================================================

internal fun evaluateComplexMathAst(
  container: MathContainer,
  vars: Map<String, ComplexNum>,
  angleUnit: ClassWizAngleUnit
): ComplexNum {
  return evaluateComplexNodeListAst(container, container.nodes, vars, angleUnit)
}

internal fun evaluateComplexNodeListAst(
  ownerContainer: MathContainer,
  nodes: List<MathNode>,
  vars: Map<String, ComplexNum>,
  angleUnit: ClassWizAngleUnit
): ComplexNum {
  val emptySlot = findFirstEmptySlotInList(nodes)
  if (emptySlot != null) {
    throw AstEvalException("Syntax ERROR", CursorLocation(emptySlot, 0))
  }
  if (nodes.isEmpty()) {
    throw AstEvalException("Syntax ERROR", CursorLocation(ownerContainer, 0))
  }
  val tokens = tokenizeComplexContainerNodes(ownerContainer, nodes)
  return ComplexAstTokenParser(ownerContainer, tokens, vars, angleUnit).parse()
}

private fun tokenizeComplexContainerNodes(container: MathContainer, nodes: List<MathNode>): List<AstToken> {
  val tokens = mutableListOf<AstToken>()
  var i = 0
  while (i < nodes.size) {
    val node = nodes[i]
    if (node !is MathNode.TextNode) {
      tokens.add(AstToken.NodeValue(node, container, i))
      i++
      continue
    }

    val t = node.text
    if (t.length == 1 && (t[0].isDigit() || t[0] == '.')) {
      val startIdx = i
      val numSb = StringBuilder()
      while (i < nodes.size) {
        val cur = nodes[i]
        if (cur is MathNode.TextNode && cur.text.length == 1 && (cur.text[0].isDigit() || cur.text[0] == '.')) {
          numSb.append(cur.text)
          i++
        } else break
      }
      val parsed = numSb.toString().toDoubleOrNull()
        ?: throw AstEvalException("Syntax ERROR", CursorLocation(container, startIdx))
      tokens.add(AstToken.NumberLit(parsed, container, startIdx))
      continue
    }

    when {
      t == "×10^" -> tokens.add(AstToken.Operator("×10^", container, i))
      t in setOf("+", "-", "−", "×", "*", "÷", "/", "^", "(", ")", ",", "∠") -> {
        val normOp = when (t) {
          "−" -> "-"
          "*" -> "×"
          "/" -> "÷"
          else -> t
        }
        tokens.add(AstToken.Operator(normOp, container, i))
      }
      t in setOf("!", "%", "²", "³", "⁻¹", "°", "ʳ", "ᵍ") -> {
        tokens.add(AstToken.PostfixOp(t, container, i))
      }
      t.endsWith("(") -> {
        tokens.add(AstToken.FunctionStart(t.removeSuffix("("), container, i))
      }
      else -> tokens.add(AstToken.Identifier(t, container, i))
    }
    i++
  }
  return tokens
}

private class ComplexAstTokenParser(
  private val ownerContainer: MathContainer,
  private val tokens: List<AstToken>,
  private val vars: Map<String, ComplexNum>,
  private val angleUnit: ClassWizAngleUnit
) {
  private var pos = 0

  fun parse(): ComplexNum {
    val res = parseExpr()
    if (pos < tokens.size) {
      throw AstEvalException("Syntax ERROR", tokenCursor(tokens[pos]))
    }
    if (res.re.isNaN() || res.im.isNaN() || res.re.isInfinite() || res.im.isInfinite()) {
      throw AstEvalException("Math ERROR", CursorLocation(ownerContainer, ownerContainer.nodes.size))
    }
    return res
  }

  private fun tokenCursor(tok: AstToken): CursorLocation = when (tok) {
    is AstToken.NumberLit -> CursorLocation(tok.container, tok.nodeIdx)
    is AstToken.Operator -> CursorLocation(tok.container, tok.nodeIdx)
    is AstToken.FunctionStart -> CursorLocation(tok.container, tok.nodeIdx)
    is AstToken.Identifier -> CursorLocation(tok.container, tok.nodeIdx)
    is AstToken.NodeValue -> CursorLocation(tok.container, tok.nodeIdx)
    is AstToken.PostfixOp -> CursorLocation(tok.container, tok.nodeIdx)
  }

  private fun peek(): AstToken? = tokens.getOrNull(pos)

  private fun matchOp(op: String): Boolean {
    val t = peek()
    if (t is AstToken.Operator && t.op == op) {
      pos++
      return true
    }
    return false
  }

  private fun canStartImplicitPrimary(tok: AstToken?): Boolean = when (tok) {
    is AstToken.NumberLit,
    is AstToken.FunctionStart,
    is AstToken.Identifier,
    is AstToken.NodeValue -> true
    is AstToken.Operator -> tok.op == "("
    else -> false
  }

  private fun parseExpr(): ComplexNum {
    var x = parseTerm()
    while (true) {
      x = when {
        matchOp("+") -> x + parseTerm()
        matchOp("-") -> x - parseTerm()
        else -> return x
      }
    }
  }

  private fun parseTerm(): ComplexNum {
    var x = parseSciOrPower()
    while (true) {
      val t = peek() ?: return x
      x = when {
        t is AstToken.Operator && t.op == "×" -> {
          pos++
          x * parseSciOrPower()
        }
        t is AstToken.Operator && t.op == "÷" -> {
          pos++
          val divisorTok = peek()
          val d = parseSciOrPower()
          if (d.abs() < 1e-15) {
            val loc = divisorTok?.let { tokenCursor(it) } ?: tokenCursor(t)
            throw AstEvalException("Math ERROR", loc, "Math ERROR: Divide by 0")
          }
          x / d
        }
        canStartImplicitPrimary(t) -> x * parseSciOrPower()
        else -> return x
      }
    }
  }

  private fun parseSciOrPower(): ComplexNum {
    var x = parsePolar()
    while (true) {
      val t = peek()
      if (t is AstToken.Operator && t.op == "×10^") {
        pos++
        val expVal = parseUnary()
        val scale = complexPow(ComplexNum(10.0, 0.0), expVal, tokenCursor(t))
        x *= scale
      } else {
        return x
      }
    }
  }

  private fun parsePolar(): ComplexNum {
    val mag = parsePower()
    val t = peek()
    if (t is AstToken.Operator && t.op == "∠") {
      pos++
      val ang = parseUnary()
      if (abs(mag.im) > 1e-10 || abs(ang.im) > 1e-10) {
        throw AstEvalException("Math ERROR", tokenCursor(t))
      }
      val rad = angleUnit.toRadians(ang.re)
      return ComplexNum(mag.re * cos(rad), mag.re * sin(rad))
    }
    return mag
  }

  private fun parsePower(): ComplexNum {
    val base = parseUnary()
    val t = peek()
    if (t is AstToken.Operator && t.op == "^") {
      pos++
      val expVal = parsePower()
      return complexPow(base, expVal, tokenCursor(t))
    }
    return base
  }

  private fun parseUnary(): ComplexNum {
    return when {
      matchOp("+") -> parseUnary()
      matchOp("-") -> -parseUnary()
      else -> parsePostfix()
    }
  }

  private fun parsePostfix(): ComplexNum {
    var x = parsePrimary()
    while (true) {
      val t = peek()
      if (t !is AstToken.PostfixOp) break
      pos++
      x = when (t.op) {
        "%" -> ComplexNum(x.re * 0.01, x.im * 0.01)
        "²" -> x * x
        "³" -> x * x * x
        "⁻¹" -> {
          if (x.abs() < 1e-15) throw AstEvalException("Math ERROR", tokenCursor(t))
          ComplexNum(1.0, 0.0) / x
        }
        "!" -> {
          if (abs(x.im) > 1e-9) throw AstEvalException("Math ERROR", tokenCursor(t))
          val n = x.re.roundToInt()
          if (x.re < 0.0 || abs(x.re - n) > 1e-9 || n > 170) throw AstEvalException("Math ERROR", tokenCursor(t))
          var f = 1.0
          for (k in 2..n) f *= k.toDouble()
          ComplexNum(f, 0.0)
        }
        "°" -> parseComplexDmsContinuation(x)
        "ʳ" -> ComplexNum(angleUnit.fromRadians(x.re), x.im)
        "ᵍ" -> ComplexNum(angleUnit.fromRadians(x.re * PI / 200.0), x.im)
        else -> x
      }
    }
    return x
  }

  private fun parseComplexDmsContinuation(degPart: ComplexNum): ComplexNum {
    val sign = if (degPart.re < 0.0) -1.0 else 1.0
    var total = abs(degPart.re)
    val minTok = peek()
    if (minTok is AstToken.NumberLit) {
      pos++
      total += minTok.value / 60.0
      val post1 = peek()
      if (post1 is AstToken.PostfixOp && post1.op == "°") {
        pos++
        val secTok = peek()
        if (secTok is AstToken.NumberLit) {
          pos++
          total += secTok.value / 3600.0
          val post2 = peek()
          if (post2 is AstToken.PostfixOp && post2.op == "°") {
            pos++
          }
        }
      }
    }
    return ComplexNum(sign * total, degPart.im)
  }

  private fun parsePrimary(): ComplexNum {
    val t = peek() ?: throw AstEvalException("Syntax ERROR", CursorLocation(ownerContainer, ownerContainer.nodes.size))
    when (t) {
      is AstToken.Operator -> {
        if (t.op == "(") {
          pos++
          val inner = parseExpr()
          if (!matchOp(")")) {
            throw AstEvalException("Syntax ERROR", CursorLocation(t.container, ownerContainer.nodes.size))
          }
          return inner
        }
        throw AstEvalException("Syntax ERROR", tokenCursor(t))
      }
      is AstToken.NumberLit -> {
        pos++
        return ComplexNum(t.value, 0.0)
      }
      is AstToken.Identifier -> {
        pos++
        return when (t.name) {
          "i" -> ComplexNum(0.0, 1.0)
          "π", "pi" -> ComplexNum(PI, 0.0)
          "e" -> ComplexNum(kotlin.math.E, 0.0)
          "Ans" -> vars["Ans"] ?: ComplexNum(0.0, 0.0)
          "X", "x" -> vars["X"] ?: vars["x"] ?: ComplexNum(0.0, 0.0)
          "A", "B", "C", "D", "E", "F", "Y", "M" -> vars[t.name] ?: ComplexNum(0.0, 0.0)
          else -> throw AstEvalException("Syntax ERROR", tokenCursor(t))
        }
      }
      is AstToken.FunctionStart -> {
        pos++
        val args = mutableListOf<ComplexNum>()
        val closeCheck = peek()
        if (closeCheck is AstToken.Operator && closeCheck.op == ")") {
          throw AstEvalException("Syntax ERROR", tokenCursor(t))
        }
        args.add(parseExpr())
        while (matchOp(",")) {
          args.add(parseExpr())
        }
        if (!matchOp(")")) {
          throw AstEvalException("Syntax ERROR", CursorLocation(t.container, t.nodeIdx + 1))
        }
        return evalComplexFunction(t.name, args, tokenCursor(t))
      }
      is AstToken.NodeValue -> {
        pos++
        return eval2DComplexNode(t.node, t.container, t.nodeIdx)
      }
      is AstToken.PostfixOp -> throw AstEvalException("Syntax ERROR", tokenCursor(t))
    }
  }

  private fun eval2DComplexNode(node: MathNode, parent: MathContainer, idx: Int): ComplexNum {
    val realVars = vars.mapValues { it.value.re }
    return when (node) {
      is MathNode.TextNode -> ComplexNum(0.0, 0.0)
      is MathNode.FractionNode -> {
        val n = evaluateComplexMathAst(node.num, vars, angleUnit)
        val d = evaluateComplexMathAst(node.den, vars, angleUnit)
        if (d.abs() < 1e-15) throw AstEvalException("Math ERROR", CursorLocation(node.den, 0))
        n / d
      }
      is MathNode.MixedFractionNode -> {
        val w = evaluateComplexMathAst(node.whole, vars, angleUnit)
        val n = evaluateComplexMathAst(node.num, vars, angleUnit)
        val d = evaluateComplexMathAst(node.den, vars, angleUnit)
        if (d.abs() < 1e-15) throw AstEvalException("Math ERROR", CursorLocation(node.den, 0))
        if (w.re < 0.0) w - (n / d) else w + (n / d)
      }
      is MathNode.PowerNode -> {
        val b = evaluateComplexMathAst(node.base, vars, angleUnit)
        val e = evaluateComplexMathAst(node.exp, vars, angleUnit)
        complexPow(b, e, CursorLocation(node.exp, 0))
      }
      is MathNode.RootNode -> {
        val rad = evaluateComplexMathAst(node.radicand, vars, angleUnit)
        when {
          node.fixedIndexText == "3" -> complexCbrt(rad)
          node.index == null -> complexSqrt(rad)
          else -> {
            val nVal = evaluateComplexMathAst(node.index, vars, angleUnit)
            if (nVal.abs() < 1e-15) throw AstEvalException("Math ERROR", CursorLocation(node.index, 0))
            if (abs(nVal.im) < 1e-12 && abs(rad.im) < 1e-12 && rad.re < 0.0) {
              val nInt = nVal.re.roundToInt()
              if (abs(nVal.re - nInt) < 1e-11 && nInt % 2 != 0) {
                ComplexNum(-abs(rad.re).pow(1.0 / nVal.re), 0.0)
              } else {
                complexPow(rad, ComplexNum(1.0, 0.0) / nVal, CursorLocation(node.index, 0))
              }
            } else {
              complexPow(rad, ComplexNum(1.0, 0.0) / nVal, CursorLocation(node.index, 0))
            }
          }
        }
      }
      is MathNode.LogBaseNode -> {
        val b = evaluateComplexMathAst(node.base, vars, angleUnit)
        val a = evaluateComplexMathAst(node.arg, vars, angleUnit)
        if (b.abs() < 1e-15 || a.abs() < 1e-15) throw AstEvalException("Math ERROR", CursorLocation(node.arg, 0))
        val lnB = complexLn(b, CursorLocation(node.base, 0))
        val lnA = complexLn(a, CursorLocation(node.arg, 0))
        if (lnB.abs() < 1e-15) throw AstEvalException("Math ERROR", CursorLocation(node.base, 0))
        lnA / lnB
      }
      is MathNode.IntegralNode -> {
        if (node.lower.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.lower, 0))
        if (node.upper.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.upper, 0))
        if (node.integrand.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.integrand, 0))
        val a = evalRealContainer(node.lower, emptyMap(), realVars, angleUnit)
        val b = evalRealContainer(node.upper, emptyMap(), realVars, angleUnit)
        ComplexNum(integrateAstGaussKronrod(node.integrand, a, b, emptyMap(), realVars, angleUnit), 0.0)
      }
      is MathNode.DerivativeNode -> {
        if (node.expr.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.expr, 0))
        if (node.atX.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.atX, 0))
        val x0 = evalRealContainer(node.atX, emptyMap(), realVars, angleUnit)
        ComplexNum(differentiateAstCentral(node.expr, x0, emptyMap(), realVars, angleUnit), 0.0)
      }
      is MathNode.SummationNode -> {
        if (node.lower.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.lower, 0))
        if (node.upper.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.upper, 0))
        if (node.body.nodes.isEmpty()) throw AstEvalException("Syntax ERROR", CursorLocation(node.body, 0))
        val a = evalRealContainer(node.lower, emptyMap(), realVars, angleUnit).roundToInt()
        val b = evalRealContainer(node.upper, emptyMap(), realVars, angleUnit).roundToInt()
        if (abs(b - a) > 10000) throw AstEvalException("Math ERROR", CursorLocation(node.upper, 0))
        var acc = 0.0
        val step = if (a <= b) 1 else -1
        var k = a
        while (true) {
          val bound = mapOf("X" to k.toDouble(), "x" to k.toDouble())
          acc += evalRealContainer(node.body, bound, realVars, angleUnit)
          if (k == b) break
          k += step
        }
        ComplexNum(acc, 0.0)
      }
      is MathNode.AbsNode -> {
        ComplexNum(evaluateComplexMathAst(node.inner, vars, angleUnit).abs(), 0.0)
      }
    }
  }

  private fun evalComplexFunction(name: String, args: List<ComplexNum>, loc: CursorLocation): ComplexNum {
    val z = args.firstOrNull() ?: throw AstEvalException("Syntax ERROR", loc)
    return when (name) {
      "arg" -> {
        if (z.abs() < 1e-15) throw AstEvalException("Math ERROR", loc)
        ComplexNum(angleUnit.fromRadians(z.argRad()), 0.0)
      }
      "Conjg" -> z.conjg()
      "ReP" -> ComplexNum(z.re, 0.0)
      "ImP" -> ComplexNum(z.im, 0.0)
      "abs" -> ComplexNum(z.abs(), 0.0)
      "sqrt" -> complexSqrt(z)
      "cbrt" -> complexCbrt(z)
      "exp" -> complexExp(z)
      "ln" -> complexLn(z, loc)
      "log" -> complexLn(z, loc) / ComplexNum(ln(10.0), 0.0)
      "sin" -> {
        if (abs(z.im) < 1e-12) ComplexNum(sin(angleUnit.toRadians(z.re)), 0.0)
        else ComplexNum(sin(z.re) * cosh(z.im), cos(z.re) * sinh(z.im))
      }
      "cos" -> {
        if (abs(z.im) < 1e-12) ComplexNum(cos(angleUnit.toRadians(z.re)), 0.0)
        else ComplexNum(cos(z.re) * cosh(z.im), -sin(z.re) * sinh(z.im))
      }
      "tan" -> {
        if (abs(z.im) < 1e-12) {
          val rad = angleUnit.toRadians(z.re)
          if (abs(cos(rad)) < 1e-13) throw AstEvalException("Math ERROR", loc)
          ComplexNum(tan(rad), 0.0)
        } else {
          val s = ComplexNum(sin(z.re) * cosh(z.im), cos(z.re) * sinh(z.im))
          val c = ComplexNum(cos(z.re) * cosh(z.im), -sin(z.re) * sinh(z.im))
          if (c.abs() < 1e-14) throw AstEvalException("Math ERROR", loc)
          s / c
        }
      }
      "sinh" -> ComplexNum(sinh(z.re) * cos(z.im), cosh(z.re) * sin(z.im))
      "cosh" -> ComplexNum(cosh(z.re) * cos(z.im), sinh(z.re) * sin(z.im))
      "tanh" -> {
        val sh = ComplexNum(sinh(z.re) * cos(z.im), cosh(z.re) * sin(z.im))
        val ch = ComplexNum(cosh(z.re) * cos(z.im), sinh(z.re) * sin(z.im))
        if (ch.abs() < 1e-14) throw AstEvalException("Math ERROR", loc)
        sh / ch
      }
      "asin", "sin⁻¹" -> {
        if (abs(z.im) < 1e-12 && z.re in -1.0..1.0) ComplexNum(angleUnit.fromRadians(asin(z.re)), 0.0)
        else throw AstEvalException("Math ERROR", loc)
      }
      "acos", "cos⁻¹" -> {
        if (abs(z.im) < 1e-12 && z.re in -1.0..1.0) ComplexNum(angleUnit.fromRadians(acos(z.re)), 0.0)
        else throw AstEvalException("Math ERROR", loc)
      }
      "atan", "tan⁻¹" -> {
        if (abs(z.im) < 1e-12) ComplexNum(angleUnit.fromRadians(atan(z.re)), 0.0)
        else throw AstEvalException("Math ERROR", loc)
      }
      "asinh", "sinh⁻¹" -> {
        if (abs(z.im) < 1e-12) ComplexNum(asinh(z.re), 0.0) else throw AstEvalException("Math ERROR", loc)
      }
      "acosh", "cosh⁻¹" -> {
        if (abs(z.im) < 1e-12 && z.re >= 1.0) ComplexNum(acosh(z.re), 0.0) else throw AstEvalException("Math ERROR", loc)
      }
      "atanh", "tanh⁻¹" -> {
        if (abs(z.im) < 1e-12 && z.re > -1.0 && z.re < 1.0) ComplexNum(atanh(z.re), 0.0) else throw AstEvalException("Math ERROR", loc)
      }
      "Rnd" -> {
        val r = String.format(Locale.US, "%.4f", z.re).toDoubleOrNull() ?: z.re
        val im = String.format(Locale.US, "%.4f", z.im).toDoubleOrNull() ?: z.im
        ComplexNum(r, im)
      }
      else -> throw AstEvalException("Syntax ERROR", loc)
    }
  }

  private fun complexExp(z: ComplexNum): ComplexNum =
    ComplexNum(exp(z.re) * cos(z.im), exp(z.re) * sin(z.im))

  private fun complexLn(z: ComplexNum, loc: CursorLocation): ComplexNum {
    val r = z.abs()
    if (r < 1e-15) throw AstEvalException("Math ERROR", loc)
    return ComplexNum(ln(r), z.argRad())
  }

  private fun complexSqrt(z: ComplexNum): ComplexNum {
    val r = z.abs()
    if (r == 0.0) return ComplexNum(0.0, 0.0)
    val mag = sqrt(r)
    val ang = z.argRad() * 0.5
    return ComplexNum(mag * cos(ang), mag * sin(ang))
  }

  private fun complexCbrt(z: ComplexNum): ComplexNum {
    if (abs(z.im) < 1e-12) return ComplexNum(cbrt(z.re), 0.0)
    val mag = cbrt(z.abs())
    val ang = z.argRad() / 3.0
    return ComplexNum(mag * cos(ang), mag * sin(ang))
  }

  private fun complexPow(base: ComplexNum, exp: ComplexNum, loc: CursorLocation): ComplexNum {
    if (base.abs() < 1e-15) {
      if (exp.re > 0.0 && abs(exp.im) < 1e-12) return ComplexNum(0.0, 0.0)
      if (exp.abs() < 1e-15) return ComplexNum(1.0, 0.0)
      throw AstEvalException("Math ERROR", loc)
    }
    if (abs(exp.im) < 1e-12) {
      val nInt = exp.re.roundToInt()
      if (abs(exp.re - nInt) < 1e-11 && abs(nInt) <= 12) {
        var res = ComplexNum(1.0, 0.0)
        val absN = abs(nInt)
        for (k in 0 until absN) res *= base
        return if (nInt >= 0) res else ComplexNum(1.0, 0.0) / res
      }
      if (abs(base.im) < 1e-12 && base.re > 0.0) {
        val v = base.re.pow(exp.re)
        if (v.isNaN() || v.isInfinite()) throw AstEvalException("Math ERROR", loc)
        return ComplexNum(v, 0.0)
      }
    }
    val lnBase = complexLn(base, loc)
    val prod = lnBase * exp
    val out = complexExp(prod)
    if (out.re.isNaN() || out.im.isNaN() || out.re.isInfinite() || out.im.isInfinite()) {
      throw AstEvalException("Math ERROR", loc)
    }
    return out
  }
}


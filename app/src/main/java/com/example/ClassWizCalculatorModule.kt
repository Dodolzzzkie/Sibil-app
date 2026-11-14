package com.example

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.JetBrainsMonoFamily
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
// CASIO CLASSWIZ (fx-570EX / fx-991EX) SCIENTIFIC CALCULATOR MODULE
// ============================================================================

internal enum class ClassWizMode(
  val code: Int,
  val badge: String,
  val title: String,
  val subtitle: String,
  val icon: ImageVector
) {
  COMP(1, "COMP", "Calculate", "General, Calculus & SOLVE", Icons.Filled.Calculate),
  CMPLX(2, "CMPLX", "Complex", "Rect (a+bi) & Polar (r∠θ)", Icons.Filled.Functions),
  STAT(3, "STAT", "Statistics", "1-Var & 7 Regressions", Icons.Filled.BarChart),
  BASE_N(4, "BASE-N", "Base-N", "DEC / HEX / BIN / OCT", Icons.Filled.Tag),
  EQN(5, "EQN", "Equation", "Simul 2–4 & Poly Deg 2–4", Icons.Filled.GridOn),
  MATRIX(6, "MAT", "Matrix", "2×2 to 4×4 • Det, Inv, Trn", Icons.Filled.ViewModule),
  TABLE(7, "TABLE", "Table", "f(x) & g(x) Tabular Scan", Icons.Filled.TableChart),
  VECTOR(8, "VCT", "Vector", "2D/3D • Dot, Cross, Angle", Icons.Filled.NorthEast)
}

internal enum class ClassWizAngleUnit(val badge: String, val label: String) {
  DEG("D", "Deg"),
  RAD("R", "Rad"),
  GRAD("G", "Gra");

  fun toRadians(value: Double): Double = when (this) {
    DEG -> value * PI / 180.0
    RAD -> value
    GRAD -> value * PI / 200.0
  }

  fun fromRadians(radians: Double): Double = when (this) {
    DEG -> radians * 180.0 / PI
    RAD -> radians
    GRAD -> radians * 200.0 / PI
  }
}

internal enum class ClassWizNumberFormat(val label: String) {
  NORM("Math▲"),
  FIX("FIX"),
  SCI("SCI")
}

internal enum class StatRegModel(val code: Int, val label: String, val formula: String) {
  ONE_VAR(1, "1-VAR", "Single Variable Stats"),
  LINEAR(2, "A+BX", "y = a + bx"),
  QUADRATIC(3, "_+CX²", "y = a + bx + cx²"),
  LOGARITHMIC(4, "ln X", "y = a + b·ln(x)"),
  EXPONENTIAL(5, "e^X", "y = a·e^(bx)"),
  AB_EXP(6, "A·B^X", "y = a·b^x"),
  POWER(7, "A·X^B", "y = a·x^b"),
  INVERSE_X(8, "1/X", "y = a + b/x")
}

internal enum class EqnSubMode(val label: String, val isPoly: Boolean, val size: Int) {
  SIMUL_2("2×2 Linear", false, 2),
  SIMUL_3("3×3 Linear", false, 3),
  SIMUL_4("4×4 Linear", false, 4),
  POLY_2("Deg 2 Poly", true, 2),
  POLY_3("Deg 3 Poly", true, 3),
  POLY_4("Deg 4 Poly", true, 4)
}

internal data class ComplexNum(val re: Double, val im: Double = 0.0) {
  operator fun plus(other: ComplexNum) = ComplexNum(re + other.re, im + other.im)
  operator fun minus(other: ComplexNum) = ComplexNum(re - other.re, im - other.im)
  operator fun times(other: ComplexNum) = ComplexNum(
    re * other.re - im * other.im,
    re * other.im + im * other.re
  )
  operator fun div(other: ComplexNum): ComplexNum {
    val denom = other.re * other.re + other.im * other.im
    if (denom == 0.0) throw ArithmeticException("Math ERROR: Division by 0")
    return ComplexNum(
      (re * other.re + im * other.im) / denom,
      (im * other.re - re * other.im) / denom
    )
  }
  operator fun unaryMinus() = ComplexNum(-re, -im)
  fun abs(): Double = hypot(re, im)
  fun argRad(): Double = atan2(im, re)
  fun conjg(): ComplexNum = ComplexNum(re, -im)
  fun pow(exponent: ComplexNum): ComplexNum {
    if (abs() == 0.0) {
      return if (exponent.re > 0.0 && abs(exponent.im) < 1e-12) ComplexNum(0.0, 0.0)
      else throw ArithmeticException("Math ERROR: 0^z")
    }
    if (abs(exponent.im) < 1e-12) {
      val n = exponent.re.roundToInt()
      if (abs(exponent.re - n) < 1e-11 && n in -12..12) {
        val r = abs().pow(n.toDouble())
        val th = argRad() * n
        return ComplexNum(r * cos(th), r * sin(th))
      }
    }
    val logR = ln(abs())
    val th = argRad()
    // exp((a+bi)(logR + i*th)) = exp((a*logR - b*th) + i*(b*logR + a*th))
    val realPow = exponent.re * logR - exponent.im * th
    val imagPow = exponent.im * logR + exponent.re * th
    val mag = exp(realPow)
    return ComplexNum(mag * cos(imagPow), mag * sin(imagPow))
  }
}

internal data class ClassWizMatrixData(
  val rows: Int = 2,
  val cols: Int = 2,
  val cells: List<Double> = listOf(1.0, 0.0, 0.0, 1.0)
) {
  fun get(r: Int, c: Int): Double = cells.getOrElse(r * cols + c) { 0.0 }
}

internal data class ClassWizVectorData(
  val dim: Int = 3,
  val components: List<Double> = listOf(1.0, 0.0, 0.0)
) {
  fun get(i: Int): Double = components.getOrElse(i) { 0.0 }
}

// ============================================================================
// FORMATTING, S⇔D RATIONALIZER & RADICAL SIMPLIFIER
// ============================================================================

internal fun cleanZero(v: Double): Double = if (abs(v) < 1e-12) 0.0 else v

internal fun formatDecimalCompact(
  value: Double,
  format: ClassWizNumberFormat = ClassWizNumberFormat.NORM,
  fixDigits: Int = 4,
  engShift: Int = 0
): String {
  if (value.isNaN() || value.isInfinite()) return "Math ERROR"
  val v = cleanZero(value)
  if (engShift != 0 && v != 0.0) {
    val expBase = (floor(kotlin.math.log10(abs(v)) / 3.0).toInt() + engShift) * 3
    val mantissa = v / 10.0.pow(expBase.toDouble())
    val mStr = String.format(Locale.US, "%.6f", mantissa).trimEnd('0').trimEnd('.')
    return "${mStr}×10^$expBase"
  }
  return when (format) {
    ClassWizNumberFormat.FIX -> String.format(Locale.US, "%.${fixDigits.coerceIn(0, 9)}f", v)
    ClassWizNumberFormat.SCI -> {
      val digits = fixDigits.coerceIn(1, 9)
      String.format(Locale.US, "%.${digits - 1}E", v)
    }
    ClassWizNumberFormat.NORM -> {
      if (v == 0.0) "0"
      else if (abs(v) >= 1e10 || abs(v) < 1e-4) {
        String.format(Locale.US, "%.6E", v).replace("E+0", "×10^").replace("E+", "×10^").replace("E-0", "×10^-").replace("E-", "×10^-")
      } else {
        val raw = String.format(Locale.US, "%.10f", v).trimEnd('0').trimEnd('.')
        if (raw == "-0") "0" else raw
      }
    }
  }
}

private fun continuedFractionApprox(target: Double, maxDenom: Long = 1000L): Pair<Long, Long>? {
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

private fun formatFractionString(num: Long, den: Long, mixedMode: Boolean): String {
  if (den == 1L) return num.toString()
  if (!mixedMode || abs(num) < den) return "$num/$den"
  val sign = if (num < 0) "-" else ""
  val absN = abs(num)
  val whole = absN / den
  val rem = absN % den
  return if (rem == 0L) "$sign$whole" else "$sign$whole $rem/$den"
}

internal fun tryFormatExactSymbolic(value: Double, mixedMode: Boolean = false): String? {
  if (value.isNaN() || value.isInfinite()) return null
  val v = cleanZero(value)
  if (v == 0.0) return "0"

  // 1. Exact integer or rational p/q
  continuedFractionApprox(v, maxDenom = 1000L)?.let { (n, d) ->
    return formatFractionString(n, d, mixedMode)
  }

  // 2. Exact rational multiple of pi: (p/q) * pi
  continuedFractionApprox(v / PI, maxDenom = 36L)?.let { (n, d) ->
    if (n != 0L) {
      val coeff = when {
        n == 1L && d == 1L -> "π"
        n == -1L && d == 1L -> "-π"
        d == 1L -> "${n}π"
        n == 1L -> "π/$d"
        n == -1L -> "-π/$d"
        else -> "${n}π/$d"
      }
      return coeff
    }
  }

  // 3. Pure radical (a * sqrt(b)) / c
  val radBases = listOf(2, 3, 5, 6, 7, 10, 11, 13, 14, 15)
  for (b in radBases) {
    val ratio = v / sqrt(b.toDouble())
    continuedFractionApprox(ratio, maxDenom = 24L)?.let { (a, c) ->
      if (a != 0L) {
        val top = when (a) {
          1L -> "√$b"
          -1L -> "-√$b"
          else -> "${a}√$b"
        }
        return if (c == 1L) top else "$top / $c"
      }
    }
  }

  // 4. Quadratic surd (a + b*sqrt(k)) / c
  val smallRad = listOf(2, 3, 5, 6)
  for (c in 1..6) {
    val scaled = v * c
    for (k in smallRad) {
      val sk = sqrt(k.toDouble())
      for (b in -10..10) {
        if (b == 0) continue
        val rem = scaled - b * sk
        val a = rem.roundToLong()
        if (abs(rem - a) < 1e-8 && abs(a) <= 16) {
          val bAbs = abs(b)
          val bTerm = if (bAbs == 1) "√$k" else "${bAbs}√$k"
          val signStr = if (b > 0) " + " else " - "
          val numer = if (a == 0L) (if (b < 0) "-$bTerm" else bTerm) else "$a$signStr$bTerm"
          return if (c == 1) numer else "($numer) / $c"
        }
      }
    }
  }

  return null
}

internal fun formatDmsOutput(decimalDegrees: Double): String {
  if (decimalDegrees.isNaN() || decimalDegrees.isInfinite()) return "Math ERROR"
  val sign = if (decimalDegrees < 0) "-" else ""
  val absDeg = abs(decimalDegrees)
  val d = floor(absDeg).toInt()
  val remMin = (absDeg - d) * 60.0
  var m = floor(remMin).toInt()
  var s = (remMin - m) * 60.0
  if (abs(s - 60.0) < 1e-5) {
    s = 0.0
    m += 1
  }
  val secStr = String.format(Locale.US, "%.2f", cleanZero(s)).trimEnd('0').trimEnd('.')
  return "$sign${d}°${m}'${secStr}\""
}

internal fun toNaturalDisplayText(raw: String): String {
  return raw
    .replace("integral(", "∫(")
    .replace("deriv(", "d/dx(")
    .replace("sum(", "Σ(")
    .replace("logBase(", "logₐ(")
    .replace("cbrt(", "³√(")
    .replace("nroot(", "ˣ√(")
    .replace("sqrt(", "√(")
    .replace("asinh(", "sinh⁻¹(")
    .replace("acosh(", "cosh⁻¹(")
    .replace("atanh(", "tanh⁻¹(")
    .replace("asin(", "sin⁻¹(")
    .replace("acos(", "cos⁻¹(")
    .replace("atan(", "tan⁻¹(")
    .replace("*", "×")
    .replace("pi", "π")
    .replace("E10^", "×10^")
}

// ============================================================================
// SCALAR, CALCULUS & EQUATION SOLVER ENGINE (COMP + TABLE)
// ============================================================================

private class ClassWizScalarParser(
  private val input: String,
  private val vars: Map<String, Double>,
  private val angleUnit: ClassWizAngleUnit
) {
  private var pos = 0

  fun parse(): Double {
    val v = parseExpr()
    skipSpaces()
    if (pos < input.length) {
      throw IllegalArgumentException("Syntax ERROR near '${input.substring(pos)}'")
    }
    return v
  }

  private fun skipSpaces() {
    while (pos < input.length && input[pos].isWhitespace()) pos++
  }

  private fun peek(): Char? {
    skipSpaces()
    return if (pos < input.length) input[pos] else null
  }

  private fun consume(c: Char): Boolean {
    skipSpaces()
    if (pos < input.length && input[pos] == c) {
      pos++
      return true
    }
    return false
  }

  private fun parseExpr(): Double {
    var x = parseTerm()
    while (true) {
      when {
        consume('+') -> x += parseTerm()
        consume('-') -> x -= parseTerm()
        else -> return x
      }
    }
  }

  private fun parseTerm(): Double {
    var x = parsePower()
    while (true) {
      skipSpaces()
      if (pos >= input.length) return x
      val ch = input[pos]
      when {
        ch == '*' || ch == '×' -> {
          pos++
          x *= parsePower()
        }
        ch == '/' || ch == '÷' -> {
          pos++
          val d = parsePower()
          if (d == 0.0) throw ArithmeticException("Math ERROR: Divide by 0")
          x /= d
        }
        ch == 'P' && isCombinatoricBoundary() -> {
          pos++
          x = permutation(x, parsePower())
        }
        ch == 'C' && isCombinatoricBoundary() -> {
          pos++
          x = combination(x, parsePower())
        }
        shouldImplicitMultiply(ch) -> {
          x *= parsePower()
        }
        else -> return x
      }
    }
  }

  private fun isCombinatoricBoundary(): Boolean {
    // 'P' or 'C' between numbers/parens represents nPr / nCr
    val next = input.getOrNull(pos + 1) ?: return false
    return next.isDigit() || next == '(' || next == ' ' || next == '-' || next == '+'
  }

  private fun shouldImplicitMultiply(ch: Char): Boolean {
    return ch == '(' || ch.isLetter() || ch == 'π' || ch == '√'
  }

  private fun parsePower(): Double {
    val base = parseUnary()
    skipSpaces()
    if (pos < input.length && input[pos] == '^') {
      pos++
      val expVal = parsePower()
      val res = base.pow(expVal)
      if (res.isNaN()) throw ArithmeticException("Math ERROR: Domain")
      return res
    }
    return base
  }

  private fun parseUnary(): Double {
    return when {
      consume('+') -> parseUnary()
      consume('-') -> -parseUnary()
      else -> parsePostfix()
    }
  }

  private fun parsePostfix(): Double {
    var x = parsePrimary()
    while (true) {
      skipSpaces()
      if (pos >= input.length) break
      when {
        input[pos] == '!' -> {
          pos++
          x = factorial(x)
        }
        input[pos] == '%' -> {
          pos++
          x *= 0.01
        }
        input[pos] == '²' -> {
          pos++
          x *= x
        }
        input[pos] == '³' -> {
          pos++
          x = x * x * x
        }
        input.startsWith("⁻¹", pos) -> {
          pos += 2
          if (x == 0.0) throw ArithmeticException("Math ERROR: Divide by 0")
          x = 1.0 / x
        }
        input[pos] == '°' -> {
          // DMS chain or explicit degree unit suffix
          pos++
          val sign = if (x < 0) -1.0 else 1.0
          var totalDeg = abs(x)
          skipSpaces()
          if (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
            val mins = readPlainNumber()
            totalDeg += mins / 60.0
            if (pos < input.length && (input[pos] == '\'' || input[pos] == '°')) {
              pos++
              skipSpaces()
              if (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
                val secs = readPlainNumber()
                totalDeg += secs / 3600.0
                if (pos < input.length && (input[pos] == '"' || input[pos] == '°')) {
                  pos++
                }
              }
            }
          }
          x = sign * totalDeg
        }
        input[pos] == 'ʳ' -> {
          pos++
          x = angleUnit.fromRadians(x)
        }
        input[pos] == 'ᵍ' -> {
          pos++
          x = angleUnit.fromRadians(x * PI / 200.0)
        }
        else -> break
      }
    }
    return x
  }

  private fun readPlainNumber(): Double {
    val start = pos
    while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
      pos++
    }
    return input.substring(start, pos).toDoubleOrNull() ?: 0.0
  }

  private fun parsePrimary(): Double {
    skipSpaces()
    if (pos >= input.length) throw IllegalArgumentException("Syntax ERROR: Unexpected end")

    if (consume('(')) {
      val inner = parseExpr()
      if (!consume(')')) throw IllegalArgumentException("Syntax ERROR: Missing ')'")
      return inner
    }

    if (input[pos] == '√') {
      pos++
      val arg = if (consume('(')) {
        val inner = parseExpr()
        if (!consume(')')) throw IllegalArgumentException("Syntax ERROR: Missing ')'")
        inner
      } else {
        parseUnary()
      }
      if (arg < 0.0) throw ArithmeticException("Math ERROR: √ of negative")
      return sqrt(arg)
    }

    if (input[pos] == 'π') {
      pos++
      return PI
    }

    if (input[pos].isDigit() || input[pos] == '.') {
      val start = pos
      while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
      // Scientific notation e.g. 2.5E-3 or 2.5e3 (only if followed by digit or +/-digit)
      if (pos < input.length && (input[pos] == 'E' || input[pos] == 'e')) {
        val next = input.getOrNull(pos + 1)
        val after = input.getOrNull(pos + 2)
        if (next != null && (next.isDigit() || ((next == '+' || next == '-') && after?.isDigit() == true))) {
          pos++
          if (input[pos] == '+' || input[pos] == '-') pos++
          while (pos < input.length && input[pos].isDigit()) pos++
        }
      }
      return input.substring(start, pos).toDoubleOrNull()
        ?: throw IllegalArgumentException("Syntax ERROR: Bad number")
    }

    // Identifiers, functions, constants, variables
    if (input[pos].isLetter()) {
      val start = pos
      while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
      val ident = input.substring(start, pos)

      // Check 2-arg/3-arg special raw-expression functions first: integral, deriv, sum
      if (ident == "integral" || ident == "deriv" || ident == "sum") {
        if (!consume('(')) throw IllegalArgumentException("Syntax ERROR: Expected '('")
        val rawArgs = readBalancedArgs()
        return when (ident) {
          "integral" -> {
            if (rawArgs.size < 3) throw IllegalArgumentException("Syntax ERROR: ∫(f(X),a,b)")
            val a = evaluateScalar(rawArgs[1], vars, angleUnit)
            val b = evaluateScalar(rawArgs[2], vars, angleUnit)
            integrateSimpson(rawArgs[0], a, b, vars, angleUnit)
          }
          "deriv" -> {
            if (rawArgs.size < 2) throw IllegalArgumentException("Syntax ERROR: d/dx(f(X),a)")
            val a = evaluateScalar(rawArgs[1], vars, angleUnit)
            differentiateCentral(rawArgs[0], a, vars, angleUnit)
          }
          else -> {
            if (rawArgs.size < 3) throw IllegalArgumentException("Syntax ERROR: Σ(f(X),a,b)")
            val a = evaluateScalar(rawArgs[1], vars, angleUnit).roundToInt()
            val b = evaluateScalar(rawArgs[2], vars, angleUnit).roundToInt()
            if (abs(b - a) > 10000) throw ArithmeticException("Math ERROR: Σ range > 10000")
            var acc = 0.0
            val step = if (a <= b) 1 else -1
            var k = a
            while (true) {
              acc += evaluateScalar(rawArgs[0], vars + ("X" to k.toDouble()), angleUnit)
              if (k == b) break
              k += step
            }
            acc
          }
        }
      }

      if (ident == "Ran" && pos < input.length && input[pos] == '#') {
        pos++
        return Random.nextDouble()
      }

      if (peek() == '(') {
        consume('(')
        val args = mutableListOf<Double>()
        if (peek() != ')') {
          args.add(parseExpr())
          while (consume(',')) {
            args.add(parseExpr())
          }
        }
        if (!consume(')')) throw IllegalArgumentException("Syntax ERROR: Missing ')'")
        return evalFunction(ident, args)
      }

      return when (ident) {
        "pi" -> PI
        "e" -> kotlin.math.E
        "Ans" -> vars["Ans"] ?: 0.0
        "A", "B", "C", "D", "E", "F", "X", "Y", "M" -> vars[ident] ?: 0.0
        else -> throw IllegalArgumentException("Syntax ERROR: Unknown '$ident'")
      }
    }

    throw IllegalArgumentException("Syntax ERROR near '${input[pos]}'")
  }

  private fun readBalancedArgs(): List<String> {
    val result = mutableListOf<String>()
    var depth = 1
    val current = StringBuilder()
    while (pos < input.length && depth > 0) {
      val c = input[pos++]
      when {
        c == '(' -> {
          depth++
          current.append(c)
        }
        c == ')' -> {
          depth--
          if (depth == 0) {
            result.add(current.toString().trim())
          } else {
            current.append(c)
          }
        }
        c == ',' && depth == 1 -> {
          result.add(current.toString().trim())
          current.clear()
        }
        else -> current.append(c)
      }
    }
    if (depth != 0) throw IllegalArgumentException("Syntax ERROR: Missing ')'")
    return result
  }

  private fun evalFunction(name: String, args: List<Double>): Double {
    fun req1(): Double = args.firstOrNull() ?: throw IllegalArgumentException("Syntax ERROR: $name arg")
    return when (name) {
      "sin" -> sin(angleUnit.toRadians(req1()))
      "cos" -> cos(angleUnit.toRadians(req1()))
      "tan" -> {
        val rad = angleUnit.toRadians(req1())
        if (abs(cos(rad)) < 1e-13) throw ArithmeticException("Math ERROR: tan undefined")
        tan(rad)
      }
      "asin" -> {
        val v = req1()
        if (v < -1.0 || v > 1.0) throw ArithmeticException("Math ERROR: asin domain")
        angleUnit.fromRadians(asin(v))
      }
      "acos" -> {
        val v = req1()
        if (v < -1.0 || v > 1.0) throw ArithmeticException("Math ERROR: acos domain")
        angleUnit.fromRadians(acos(v))
      }
      "atan" -> angleUnit.fromRadians(atan(req1()))
      "sinh" -> sinh(req1())
      "cosh" -> cosh(req1())
      "tanh" -> tanh(req1())
      "asinh" -> asinh(req1())
      "acosh" -> {
        val v = req1()
        if (v < 1.0) throw ArithmeticException("Math ERROR: acosh domain")
        acosh(v)
      }
      "atanh" -> {
        val v = req1()
        if (v <= -1.0 || v >= 1.0) throw ArithmeticException("Math ERROR: atanh domain")
        atanh(v)
      }
      "log" -> {
        val v = req1()
        if (v <= 0.0) throw ArithmeticException("Math ERROR: log ≤ 0")
        log10(v)
      }
      "ln" -> {
        val v = req1()
        if (v <= 0.0) throw ArithmeticException("Math ERROR: ln ≤ 0")
        ln(v)
      }
      "logBase" -> {
        if (args.size < 2) throw IllegalArgumentException("Syntax ERROR: log_a(b)")
        val base = args[0]
        val v = args[1]
        if (base <= 0.0 || base == 1.0 || v <= 0.0) throw ArithmeticException("Math ERROR: logBase")
        ln(v) / ln(base)
      }
      "sqrt" -> {
        val v = req1()
        if (v < 0.0) throw ArithmeticException("Math ERROR: √ of negative")
        sqrt(v)
      }
      "cbrt" -> cbrt(req1())
      "nroot" -> {
        if (args.size < 2) throw IllegalArgumentException("Syntax ERROR: nroot(n, x)")
        val n = args[0]
        val x = args[1]
        if (n == 0.0) throw ArithmeticException("Math ERROR: 0th root")
        if (x < 0.0) {
          val nInt = n.roundToInt()
          if (abs(n - nInt) < 1e-11 && nInt % 2 != 0) -abs(x).pow(1.0 / n)
          else throw ArithmeticException("Math ERROR: Even root of negative")
        } else x.pow(1.0 / n)
      }
      "abs" -> abs(req1())
      "exp" -> exp(req1())
      "RanInt" -> {
        if (args.size < 2) throw IllegalArgumentException("Syntax ERROR: RanInt(a, b)")
        val lo = min(args[0].roundToInt(), args[1].roundToInt())
        val hi = max(args[0].roundToInt(), args[1].roundToInt())
        Random.nextInt(lo, hi + 1).toDouble()
      }
      "Pol" -> {
        if (args.size < 2) throw IllegalArgumentException("Syntax ERROR: Pol(x, y)")
        hypot(args[0], args[1])
      }
      "Rec" -> {
        if (args.size < 2) throw IllegalArgumentException("Syntax ERROR: Rec(r, θ)")
        args[0] * cos(angleUnit.toRadians(args[1]))
      }
      "Rnd" -> {
        val v = req1()
        String.format(Locale.US, "%.4f", v).toDoubleOrNull() ?: v
      }
      else -> throw IllegalArgumentException("Syntax ERROR: Unknown fn '$name'")
    }
  }

  private fun factorial(x: Double): Double {
    val n = x.roundToInt()
    if (x < 0 || abs(x - n) > 1e-9 || n > 170) throw ArithmeticException("Math ERROR: n!")
    var res = 1.0
    for (i in 2..n) res *= i.toDouble()
    return res
  }

  private fun permutation(nVal: Double, rVal: Double): Double {
    val n = nVal.roundToInt()
    val r = rVal.roundToInt()
    if (n < 0 || r < 0 || r > n || abs(nVal - n) > 1e-9 || abs(rVal - r) > 1e-9) {
      throw ArithmeticException("Math ERROR: nPr")
    }
    var res = 1.0
    for (i in 0 until r) res *= (n - i).toDouble()
    return res
  }

  private fun combination(nVal: Double, rVal: Double): Double {
    val n = nVal.roundToInt()
    val r = rVal.roundToInt()
    if (n < 0 || r < 0 || r > n || abs(nVal - n) > 1e-9 || abs(rVal - r) > 1e-9) {
      throw ArithmeticException("Math ERROR: nCr")
    }
    val k = min(r, n - r)
    var res = 1.0
    for (i in 1..k) {
      res = res * (n - k + i).toDouble() / i.toDouble()
    }
    return res
  }
}

internal fun evaluateScalar(
  expr: String,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit
): Double {
  val sanitized = expr.trim()
    .replace("×10^", "*10^")
    .replace("×", "*")
    .replace("÷", "/")
  if (sanitized.isEmpty()) throw IllegalArgumentException("Empty expression")
  return ClassWizScalarParser(sanitized, vars, angleUnit).parse()
}

private fun integrateSimpson(
  expr: String,
  a: Double,
  b: Double,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit
): Double {
  if (a == b) return 0.0
  val n = 200
  val h = (b - a) / n
  var sum = evaluateScalar(expr, vars + ("X" to a), angleUnit) +
    evaluateScalar(expr, vars + ("X" to b), angleUnit)
  for (i in 1 until n) {
    val x = a + i * h
    val coeff = if (i % 2 == 0) 2.0 else 4.0
    sum += coeff * evaluateScalar(expr, vars + ("X" to x), angleUnit)
  }
  return sum * h / 3.0
}

private fun differentiateCentral(
  expr: String,
  x0: Double,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit
): Double {
  val h = 1e-5 * max(1.0, abs(x0))
  val fP2 = evaluateScalar(expr, vars + ("X" to (x0 + 2 * h)), angleUnit)
  val fP1 = evaluateScalar(expr, vars + ("X" to (x0 + h)), angleUnit)
  val fM1 = evaluateScalar(expr, vars + ("X" to (x0 - h)), angleUnit)
  val fM2 = evaluateScalar(expr, vars + ("X" to (x0 - 2 * h)), angleUnit)
  return (-fP2 + 8.0 * fP1 - 8.0 * fM1 + fM2) / (12.0 * h)
}

internal fun solveEquationForX(
  equation: String,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit,
  initialGuess: Double = vars["X"] ?: 0.0
): Pair<Double, Double> {
  val parts = equation.split("=")
  val targetExpr = if (parts.size >= 2) "(${parts[0]}) - (${parts[1]})" else equation
  val seeds = listOf(initialGuess, 0.0, 1.0, -1.0, 2.5, -2.5, 10.0, -10.0, 0.1, 100.0).distinct()

  for (seed in seeds) {
    var x = seed
    for (iter in 0 until 80) {
      val fx = try {
        evaluateScalar(targetExpr, vars + ("X" to x), angleUnit)
      } catch (_: Throwable) {
        Double.NaN
      }
      if (fx.isNaN() || fx.isInfinite()) break
      if (abs(fx) < 1e-10) return x to fx
      val dfx = try {
        differentiateCentral(targetExpr, x, vars, angleUnit)
      } catch (_: Throwable) {
        Double.NaN
      }
      if (dfx.isNaN() || abs(dfx) < 1e-13) {
        x += 0.37
        continue
      }
      val step = (fx / dfx).coerceIn(-1e4, 1e4)
      x -= step
      if (abs(step) < 1e-12) {
        val check = evaluateScalar(targetExpr, vars + ("X" to x), angleUnit)
        if (abs(check) < 1e-7) return x to check
      }
    }
  }
  throw ArithmeticException("Can't Solve: No root found")
}

// ============================================================================
// COMPLEX NUMBER ENGINE (CMPLX)
// ============================================================================

private class ClassWizComplexParser(
  private val input: String,
  private val vars: Map<String, ComplexNum>,
  private val angleUnit: ClassWizAngleUnit
) {
  private var pos = 0

  fun parse(): ComplexNum {
    val v = parseExpr()
    skipSpaces()
    if (pos < input.length) throw IllegalArgumentException("Syntax ERROR")
    return v
  }

  private fun skipSpaces() {
    while (pos < input.length && input[pos].isWhitespace()) pos++
  }

  private fun peek(): Char? {
    skipSpaces()
    return if (pos < input.length) input[pos] else null
  }

  private fun consume(c: Char): Boolean {
    skipSpaces()
    if (pos < input.length && input[pos] == c) {
      pos++
      return true
    }
    return false
  }

  private fun parseExpr(): ComplexNum {
    var x = parseTerm()
    while (true) {
      when {
        consume('+') -> x += parseTerm()
        consume('-') -> x -= parseTerm()
        else -> return x
      }
    }
  }

  private fun parseTerm(): ComplexNum {
    var x = parsePolarOrPower()
    while (true) {
      skipSpaces()
      if (pos >= input.length) return x
      val ch = input[pos]
      when {
        ch == '*' || ch == '×' -> {
          pos++
          x *= parsePolarOrPower()
        }
        ch == '/' || ch == '÷' -> {
          pos++
          x /= parsePolarOrPower()
        }
        ch == '(' || ch.isLetter() || ch == 'π' || ch == '√' -> {
          x *= parsePolarOrPower()
        }
        else -> return x
      }
    }
  }

  private fun parsePolarOrPower(): ComplexNum {
    val base = parsePower()
    skipSpaces()
    if (pos < input.length && input[pos] == '∠') {
      pos++
      val ang = parsePower()
      val r = base.re
      val rad = angleUnit.toRadians(ang.re)
      return ComplexNum(r * cos(rad), r * sin(rad))
    }
    return base
  }

  private fun parsePower(): ComplexNum {
    val base = parseUnary()
    skipSpaces()
    if (pos < input.length && input[pos] == '^') {
      pos++
      return base.pow(parsePower())
    }
    return base
  }

  private fun parseUnary(): ComplexNum {
    return when {
      consume('+') -> parseUnary()
      consume('-') -> -parseUnary()
      else -> parsePostfix()
    }
  }

  private fun parsePostfix(): ComplexNum {
    var x = parsePrimary()
    while (true) {
      skipSpaces()
      if (pos >= input.length) break
      when {
        input[pos] == '²' -> {
          pos++
          x *= x
        }
        input[pos] == '³' -> {
          pos++
          x = x * x * x
        }
        input.startsWith("⁻¹", pos) -> {
          pos += 2
          x = ComplexNum(1.0, 0.0) / x
        }
        else -> break
      }
    }
    return x
  }

  private fun parsePrimary(): ComplexNum {
    skipSpaces()
    if (pos >= input.length) throw IllegalArgumentException("Syntax ERROR")
    if (consume('(')) {
      val inner = parseExpr()
      if (!consume(')')) throw IllegalArgumentException("Syntax ERROR: Missing ')'")
      return inner
    }
    if (input[pos] == '√') {
      pos++
      val arg = if (consume('(')) {
        val inner = parseExpr()
        if (!consume(')')) throw IllegalArgumentException("Syntax ERROR")
        inner
      } else parseUnary()
      return complexSqrt(arg)
    }
    if (input[pos] == 'π') {
      pos++
      return ComplexNum(PI, 0.0)
    }
    if (input[pos].isDigit() || input[pos] == '.') {
      val start = pos
      while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
      if (pos < input.length && (input[pos] == 'E' || input[pos] == 'e')) {
        val next = input.getOrNull(pos + 1)
        val after = input.getOrNull(pos + 2)
        if (next != null && (next.isDigit() || ((next == '+' || next == '-') && after?.isDigit() == true))) {
          pos++
          if (input[pos] == '+' || input[pos] == '-') pos++
          while (pos < input.length && input[pos].isDigit()) pos++
        }
      }
      val v = input.substring(start, pos).toDoubleOrNull() ?: throw IllegalArgumentException("Syntax ERROR")
      return ComplexNum(v, 0.0)
    }
    if (input[pos].isLetter()) {
      val start = pos
      while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
      val ident = input.substring(start, pos)
      if (peek() == '(') {
        consume('(')
        val arg = parseExpr()
        if (!consume(')')) throw IllegalArgumentException("Syntax ERROR")
        return when (ident) {
          "arg" -> ComplexNum(angleUnit.fromRadians(arg.argRad()), 0.0)
          "Conjg", "conjg" -> arg.conjg()
          "ReP", "re" -> ComplexNum(arg.re, 0.0)
          "ImP", "im" -> ComplexNum(arg.im, 0.0)
          "abs" -> ComplexNum(arg.abs(), 0.0)
          "sqrt" -> complexSqrt(arg)
          "sin" -> {
            if (abs(arg.im) < 1e-12) ComplexNum(sin(angleUnit.toRadians(arg.re)), 0.0)
            else ComplexNum(sin(arg.re) * cosh(arg.im), cos(arg.re) * sinh(arg.im))
          }
          "cos" -> {
            if (abs(arg.im) < 1e-12) ComplexNum(cos(angleUnit.toRadians(arg.re)), 0.0)
            else ComplexNum(cos(arg.re) * cosh(arg.im), -sin(arg.re) * sinh(arg.im))
          }
          "ln" -> ComplexNum(ln(arg.abs()), arg.argRad())
          "exp" -> ComplexNum(exp(arg.re) * cos(arg.im), exp(arg.re) * sin(arg.im))
          else -> throw IllegalArgumentException("Unsupported in CMPLX: $ident")
        }
      }
      return when (ident) {
        "i" -> ComplexNum(0.0, 1.0)
        "pi" -> ComplexNum(PI, 0.0)
        "e" -> ComplexNum(kotlin.math.E, 0.0)
        "Ans" -> vars["Ans"] ?: ComplexNum(0.0, 0.0)
        "A", "B", "C", "D", "E", "F", "X", "Y", "M" -> vars[ident] ?: ComplexNum(0.0, 0.0)
        else -> throw IllegalArgumentException("Unknown '$ident'")
      }
    }
    throw IllegalArgumentException("Syntax ERROR")
  }

  private fun complexSqrt(z: ComplexNum): ComplexNum {
    val r = sqrt(z.abs())
    val th = z.argRad() / 2.0
    return ComplexNum(r * cos(th), r * sin(th))
  }
}

internal fun evaluateComplex(
  expr: String,
  vars: Map<String, ComplexNum>,
  angleUnit: ClassWizAngleUnit
): ComplexNum {
  val sanitized = expr.trim()
    .replace("×10^", "*10^")
    .replace("×", "*")
    .replace("÷", "/")
  if (sanitized.isEmpty()) throw IllegalArgumentException("Empty expression")
  return ClassWizComplexParser(sanitized, vars, angleUnit).parse()
}

internal fun formatComplexDisplay(
  z: ComplexNum,
  polarMode: Boolean,
  angleUnit: ClassWizAngleUnit,
  exactMode: Boolean,
  format: ClassWizNumberFormat,
  fixDigits: Int
): String {
  if (z.re.isNaN() || z.im.isNaN()) return "Math ERROR"
  val re = cleanZero(z.re)
  val im = cleanZero(z.im)
  if (polarMode) {
    val r = z.abs()
    val th = cleanZero(angleUnit.fromRadians(z.argRad()))
    val rStr = (if (exactMode) tryFormatExactSymbolic(r) else null)
      ?: formatDecimalCompact(r, format, fixDigits)
    val thStr = formatDecimalCompact(th, format, fixDigits)
    return "$rStr ∠ $thStr${if (angleUnit == ClassWizAngleUnit.DEG) "°" else ""}"
  }
  val fmtPart = { v: Double ->
    (if (exactMode) tryFormatExactSymbolic(v) else null) ?: formatDecimalCompact(v, format, fixDigits)
  }
  return when {
    im == 0.0 -> fmtPart(re)
    re == 0.0 -> when (im) {
      1.0 -> "i"
      -1.0 -> "-i"
      else -> "${fmtPart(im)}i"
    }
    else -> {
      val op = if (im > 0) " + " else " - "
      val absIm = abs(im)
      val imPart = if (abs(absIm - 1.0) < 1e-11) "i" else "${fmtPart(absIm)}i"
      "${fmtPart(re)}$op$imPart"
    }
  }
}

// ============================================================================
// STATISTICS, EQUATION, MATRIX & VECTOR MATHEMATICAL SOLVERS
// ============================================================================

internal data class StatSummaryResult(
  val n: Int,
  val meanX: Double,
  val sumX: Double,
  val sumX2: Double,
  val popStdX: Double,
  val sampStdX: Double,
  val minX: Double,
  val q1: Double,
  val median: Double,
  val q3: Double,
  val maxX: Double,
  val meanY: Double? = null,
  val popStdY: Double? = null,
  val sampStdY: Double? = null,
  val regA: Double? = null,
  val regB: Double? = null,
  val regC: Double? = null,
  val regR: Double? = null,
  val error: String? = null
)

private fun percentileSorted(sorted: List<Double>, q: Double): Double {
  if (sorted.isEmpty()) return 0.0
  if (sorted.size == 1) return sorted[0]
  val idx = q * (sorted.size - 1)
  val lo = floor(idx).toInt()
  val hi = min(sorted.size - 1, lo + 1)
  val frac = idx - lo
  return sorted[lo] * (1.0 - frac) + sorted[hi] * frac
}

private fun solveLinearSystem(matrixA: Array<DoubleArray>, vectorB: DoubleArray): List<Double>? {
  val n = vectorB.size
  val a = Array(n) { r -> DoubleArray(n + 1) { c -> if (c < n) matrixA[r][c] else vectorB[r] } }
  for (col in 0 until n) {
    var pivotRow = col
    for (r in col + 1 until n) {
      if (abs(a[r][col]) > abs(a[pivotRow][col])) pivotRow = r
    }
    val tmp = a[col]
    a[col] = a[pivotRow]
    a[pivotRow] = tmp
    if (abs(a[col][col]) < 1e-12) return null
    val pivot = a[col][col]
    for (c in col..n) a[col][c] /= pivot
    for (r in 0 until n) {
      if (r != col) {
        val factor = a[r][col]
        for (c in col..n) a[r][c] -= factor * a[col][c]
      }
    }
  }
  return List(n) { r -> cleanZero(a[r][n]) }
}

internal fun computeStatSummary(
  pairs: List<Pair<Double, Double>>,
  model: StatRegModel
): StatSummaryResult {
  if (pairs.isEmpty()) {
    return StatSummaryResult(
      0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
      error = "Add at least 1 data row"
    )
  }
  val xs = pairs.map { it.first }
  val n = xs.size
  val sumX = xs.sum()
  val sumX2 = xs.sumOf { it * it }
  val meanX = sumX / n
  val varPopX = xs.sumOf { (it - meanX) * (it - meanX) } / n
  val popStdX = sqrt(varPopX)
  val sampStdX = if (n > 1) sqrt(varPopX * n / (n - 1.0)) else 0.0
  val sortedX = xs.sorted()
  val minX = sortedX.first()
  val maxX = sortedX.last()
  val q1 = percentileSorted(sortedX, 0.25)
  val med = percentileSorted(sortedX, 0.50)
  val q3 = percentileSorted(sortedX, 0.75)

  if (model == StatRegModel.ONE_VAR) {
    return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX)
  }

  val ys = pairs.map { it.second }
  val meanY = ys.sum() / n
  val varPopY = ys.sumOf { (it - meanY) * (it - meanY) } / n
  val popStdY = sqrt(varPopY)
  val sampStdY = if (n > 1) sqrt(varPopY * n / (n - 1.0)) else 0.0

  if (n < 2) {
    return StatSummaryResult(
      n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX,
      meanY, popStdY, sampStdY,
      error = "Need ≥ 2 points for regression"
    )
  }

  fun linearFit(tx: List<Double>, ty: List<Double>): Triple<Double, Double, Double>? {
    val mx = tx.average()
    val my = ty.average()
    val sxx = tx.sumOf { (it - mx) * (it - mx) }
    val syy = ty.sumOf { (it - my) * (it - my) }
    val sxy = tx.indices.sumOf { (tx[it] - mx) * (ty[it] - my) }
    if (abs(sxx) < 1e-14) return null
    val b = sxy / sxx
    val a = my - b * mx
    val r = if (syy > 1e-14) sxy / sqrt(sxx * syy) else 1.0
    return Triple(a, b, r)
  }

  return when (model) {
    StatRegModel.LINEAR -> {
      val fit = linearFit(xs, ys)
        ?: return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "Zero X variance")
      StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, fit.first, fit.second, null, fit.third)
    }
    StatRegModel.QUADRATIC -> {
      if (n < 3) return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "Need ≥ 3 points for Quad")
      val sx = xs.sum()
      val sx2 = xs.sumOf { it * it }
      val sx3 = xs.sumOf { it * it * it }
      val sx4 = xs.sumOf { it * it * it * it }
      val sy = ys.sum()
      val sxy = xs.indices.sumOf { xs[it] * ys[it] }
      val sx2y = xs.indices.sumOf { xs[it] * xs[it] * ys[it] }
      val sol = solveLinearSystem(
        arrayOf(
          doubleArrayOf(n.toDouble(), sx, sx2),
          doubleArrayOf(sx, sx2, sx3),
          doubleArrayOf(sx2, sx3, sx4)
        ),
        doubleArrayOf(sy, sxy, sx2y)
      ) ?: return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "Singular data matrix")
      val (a, b, c) = sol
      val ssTot = ys.sumOf { (it - meanY) * (it - meanY) }
      val ssRes = xs.indices.sumOf {
        val pred = a + b * xs[it] + c * xs[it] * xs[it]
        (ys[it] - pred) * (ys[it] - pred)
      }
      val r2 = if (ssTot > 1e-14) max(0.0, 1.0 - ssRes / ssTot) else 1.0
      StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, a, b, c, sqrt(r2))
    }
    StatRegModel.LOGARITHMIC -> {
      if (xs.any { it <= 0.0 }) return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "ln(x) requires x > 0")
      val fit = linearFit(xs.map { ln(it) }, ys)
        ?: return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "Zero variance")
      StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, fit.first, fit.second, null, fit.third)
    }
    StatRegModel.EXPONENTIAL -> {
      if (ys.any { it <= 0.0 }) return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "Exponential requires y > 0")
      val fit = linearFit(xs, ys.map { ln(it) })
        ?: return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "Zero variance")
      StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, exp(fit.first), fit.second, null, fit.third)
    }
    StatRegModel.AB_EXP -> {
      if (ys.any { it <= 0.0 }) return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "A·B^X requires y > 0")
      val fit = linearFit(xs, ys.map { ln(it) })
        ?: return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "Zero variance")
      StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, exp(fit.first), exp(fit.second), null, fit.third)
    }
    StatRegModel.POWER -> {
      if (xs.any { it <= 0.0 } || ys.any { it <= 0.0 }) {
        return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "Power requires x > 0, y > 0")
      }
      val fit = linearFit(xs.map { ln(it) }, ys.map { ln(it) })
        ?: return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "Zero variance")
      StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, exp(fit.first), fit.second, null, fit.third)
    }
    StatRegModel.INVERSE_X -> {
      if (xs.any { abs(it) < 1e-13 }) {
        return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "1/X requires x ≠ 0")
      }
      val fit = linearFit(xs.map { 1.0 / it }, ys)
        ?: return StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, error = "Zero variance")
      StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX, meanY, popStdY, sampStdY, fit.first, fit.second, null, fit.third)
    }
    StatRegModel.ONE_VAR -> StatSummaryResult(n, meanX, sumX, sumX2, popStdX, sampStdX, minX, q1, med, q3, maxX)
  }
}

internal data class SimulSolveOutcome(
  val status: String, // "UNIQUE", "NONE", "INFINITE"
  val solutions: List<Double> = emptyList()
)

internal fun solveSimultaneousWithStatus(
  coeffs: Array<DoubleArray>,
  rhs: DoubleArray
): SimulSolveOutcome {
  val n = rhs.size
  val aug = Array(n) { r -> DoubleArray(n + 1) { c -> if (c < n) coeffs[r][c] else rhs[r] } }
  var rank = 0
  val pivotCols = mutableListOf<Int>()

  for (col in 0 until n) {
    var pivotRow = rank
    for (r in rank + 1 until n) {
      if (abs(aug[r][col]) > abs(aug[pivotRow][col])) pivotRow = r
    }
    if (pivotRow < n && abs(aug[pivotRow][col]) > 1e-11) {
      val tmp = aug[rank]
      aug[rank] = aug[pivotRow]
      aug[pivotRow] = tmp
      val p = aug[rank][col]
      for (c in col..n) aug[rank][c] /= p
      for (r in 0 until n) {
        if (r != rank) {
          val f = aug[r][col]
          for (c in col..n) aug[r][c] -= f * aug[rank][c]
        }
      }
      pivotCols.add(col)
      rank++
    }
  }

  for (r in rank until n) {
    if (abs(aug[r][n]) > 1e-9) {
      return SimulSolveOutcome("NONE")
    }
  }
  if (rank < n) {
    return SimulSolveOutcome("INFINITE")
  }
  return SimulSolveOutcome("UNIQUE", List(n) { r -> cleanZero(aug[r][n]) })
}

internal fun solvePolynomialRoots(coeffsDesc: List<Double>): Pair<List<ComplexNum>, List<String>> {
  // coeffsDesc = [a_n, a_{n-1}, ..., a_0]
  val trimmed = coeffsDesc.dropWhile { abs(it) < 1e-13 }
  val deg = trimmed.size - 1
  if (deg <= 0) return emptyList<ComplexNum>() to listOf("Leading coefficients are all 0")

  val a0Lead = trimmed[0]
  val norm = trimmed.map { it / a0Lead } // monic: x^d + c1 x^{d-1} + ... + cd

  val roots = when (deg) {
    1 -> listOf(ComplexNum(-norm[1], 0.0))
    2 -> {
      val b = norm[1]
      val c = norm[2]
      val disc = b * b - 4.0 * c
      if (disc >= 0.0) {
        val sd = sqrt(disc)
        listOf(ComplexNum((-b + sd) / 2.0, 0.0), ComplexNum((-b - sd) / 2.0, 0.0))
      } else {
        val sd = sqrt(-disc)
        listOf(ComplexNum(-b / 2.0, sd / 2.0), ComplexNum(-b / 2.0, -sd / 2.0))
      }
    }
    else -> {
      // Durand-Kerner complex root finder
      val radius = 1.0 + norm.drop(1).maxOfOrNull { abs(it) }!!.coerceAtMost(20.0)
      val z = MutableList(deg) { k ->
        val ang = 2.0 * PI * k / deg + 0.17
        ComplexNum(radius * cos(ang), radius * sin(ang))
      }
      fun evalPoly(pt: ComplexNum): ComplexNum {
        var acc = ComplexNum(1.0, 0.0)
        for (i in 1 until norm.size) {
          acc = acc * pt + ComplexNum(norm[i], 0.0)
        }
        return acc
      }
      repeat(120) {
        var maxDelta = 0.0
        for (i in 0 until deg) {
          var denom = ComplexNum(1.0, 0.0)
          for (j in 0 until deg) {
            if (i != j) denom *= (z[i] - z[j])
          }
          if (denom.abs() > 1e-15) {
            val delta = evalPoly(z[i]) / denom
            z[i] = z[i] - delta
            maxDelta = max(maxDelta, delta.abs())
          }
        }
        if (maxDelta < 1e-12) return@repeat
      }
      z.map { ComplexNum(cleanZero(it.re), if (abs(it.im) < 1e-9) 0.0 else it.im) }
    }
  }

  val notes = mutableListOf<String>()
  if (coeffsDesc.size == 3 && abs(coeffsDesc[0]) > 1e-12) {
    val a = coeffsDesc[0]
    val b = coeffsDesc[1]
    val c = coeffsDesc[2]
    val vx = -b / (2.0 * a)
    val vy = a * vx * vx + b * vx + c
    val kind = if (a > 0) "Minimum" else "Maximum"
    notes.add("$kind Vertex: x = ${formatDecimalCompact(vx)}, y = ${formatDecimalCompact(vy)}")
  } else if (deg >= 3) {
    // Extrema from P'(x) = 0
    val derivCoeffs = (0 until trimmed.lastIndex).map { idx ->
      trimmed[idx] * (deg - idx)
    }
    if (derivCoeffs.size >= 2) {
      val critRoots = solvePolynomialRoots(derivCoeffs).first.filter { abs(it.im) < 1e-8 }
      critRoots.forEachIndexed { idx, r ->
        var py = 0.0
        for (c in trimmed) py = py * r.re + c
        notes.add("Extremum ${idx + 1}: x = ${formatDecimalCompact(r.re)}, y = ${formatDecimalCompact(py)}")
      }
    }
  }
  return roots to notes
}

private fun matrixDet(m: ClassWizMatrixData): Double {
  if (m.rows != m.cols) throw ArithmeticException("Dimension ERROR: Non-square")
  val n = m.rows
  if (n == 1) return m.get(0, 0)
  if (n == 2) return m.get(0, 0) * m.get(1, 1) - m.get(0, 1) * m.get(1, 0)
  val a = Array(n) { r -> DoubleArray(n) { c -> m.get(r, c) } }
  var det = 1.0
  for (col in 0 until n) {
    var pivotRow = col
    for (r in col + 1 until n) {
      if (abs(a[r][col]) > abs(a[pivotRow][col])) pivotRow = r
    }
    if (abs(a[pivotRow][col]) < 1e-13) return 0.0
    if (pivotRow != col) {
      val tmp = a[col]
      a[col] = a[pivotRow]
      a[pivotRow] = tmp
      det = -det
    }
    val pivot = a[col][col]
    det *= pivot
    for (r in col + 1 until n) {
      val f = a[r][col] / pivot
      for (c in col until n) a[r][c] -= f * a[col][c]
    }
  }
  return cleanZero(det)
}

private fun matrixTranspose(m: ClassWizMatrixData): ClassWizMatrixData {
  val out = MutableList(m.rows * m.cols) { 0.0 }
  for (r in 0 until m.rows) {
    for (c in 0 until m.cols) {
      out[c * m.rows + r] = m.get(r, c)
    }
  }
  return ClassWizMatrixData(m.cols, m.rows, out)
}

private fun matrixInverse(m: ClassWizMatrixData): ClassWizMatrixData {
  if (m.rows != m.cols) throw ArithmeticException("Dimension ERROR: Non-square")
  val n = m.rows
  val aug = Array(n) { r ->
    DoubleArray(2 * n) { c ->
      if (c < n) m.get(r, c) else if (c - n == r) 1.0 else 0.0
    }
  }
  for (col in 0 until n) {
    var pivotRow = col
    for (r in col + 1 until n) {
      if (abs(aug[r][col]) > abs(aug[pivotRow][col])) pivotRow = r
    }
    val tmp = aug[col]
    aug[col] = aug[pivotRow]
    aug[pivotRow] = tmp
    val pivot = aug[col][col]
    if (abs(pivot) < 1e-12) throw ArithmeticException("Math ERROR: Singular Matrix")
    for (c in 0 until 2 * n) aug[col][c] /= pivot
    for (r in 0 until n) {
      if (r != col) {
        val f = aug[r][col]
        for (c in 0 until 2 * n) aug[r][c] -= f * aug[col][c]
      }
    }
  }
  val cells = MutableList(n * n) { idx -> cleanZero(aug[idx / n][n + (idx % n)]) }
  return ClassWizMatrixData(n, n, cells)
}

private fun matrixMultiply(a: ClassWizMatrixData, b: ClassWizMatrixData): ClassWizMatrixData {
  if (a.cols != b.rows) throw ArithmeticException("Dimension ERROR: ${a.rows}×${a.cols} · ${b.rows}×${b.cols}")
  val out = MutableList(a.rows * b.cols) { 0.0 }
  for (r in 0 until a.rows) {
    for (c in 0 until b.cols) {
      var sum = 0.0
      for (k in 0 until a.cols) sum += a.get(r, k) * b.get(k, c)
      out[r * b.cols + c] = cleanZero(sum)
    }
  }
  return ClassWizMatrixData(a.rows, b.cols, out)
}

private fun matrixAddSub(a: ClassWizMatrixData, b: ClassWizMatrixData, sign: Double): ClassWizMatrixData {
  if (a.rows != b.rows || a.cols != b.cols) throw ArithmeticException("Dimension ERROR")
  return ClassWizMatrixData(
    a.rows,
    a.cols,
    a.cells.indices.map { cleanZero(a.cells[it] + sign * b.cells[it]) }
  )
}

private fun vectorDot(a: ClassWizVectorData, b: ClassWizVectorData): Double {
  if (a.dim != b.dim) throw ArithmeticException("Dimension ERROR")
  return (0 until a.dim).sumOf { a.get(it) * b.get(it) }
}

private fun vectorCross(a: ClassWizVectorData, b: ClassWizVectorData): ClassWizVectorData {
  val a3 = listOf(a.get(0), a.get(1), if (a.dim == 3) a.get(2) else 0.0)
  val b3 = listOf(b.get(0), b.get(1), if (b.dim == 3) b.get(2) else 0.0)
  return ClassWizVectorData(
    dim = 3,
    components = listOf(
      cleanZero(a3[1] * b3[2] - a3[2] * b3[1]),
      cleanZero(a3[2] * b3[0] - a3[0] * b3[2]),
      cleanZero(a3[0] * b3[1] - a3[1] * b3[0])
    )
  )
}

private fun vectorNorm(v: ClassWizVectorData): Double =
  sqrt((0 until v.dim).sumOf { v.get(it) * v.get(it) })

private fun vectorUnit(v: ClassWizVectorData): ClassWizVectorData {
  val mag = vectorNorm(v)
  if (mag < 1e-13) throw ArithmeticException("Math ERROR: Zero Vector")
  return ClassWizVectorData(v.dim, (0 until v.dim).map { cleanZero(v.get(it) / mag) })
}

private fun vectorAngle(a: ClassWizVectorData, b: ClassWizVectorData, angleUnit: ClassWizAngleUnit): Double {
  val magA = vectorNorm(a)
  val magB = vectorNorm(b)
  if (magA < 1e-13 || magB < 1e-13) throw ArithmeticException("Math ERROR: Zero Vector")
  val cosTheta = (vectorDot(a, b) / (magA * magB)).coerceIn(-1.0, 1.0)
  return cleanZero(angleUnit.fromRadians(acos(cosTheta)))
}

// ============================================================================
// MAIN COMPOSABLE SCREEN: ClassWizCalculatorScreen
// ============================================================================

@Composable
internal fun ClassWizCalculatorScreen(
  metrics: ResponsiveMetrics,
  onBackToExtras: () -> Unit,
  modifier: Modifier = Modifier
) {
  ScientificCalculatorScreen(
    metrics = metrics,
    onBackToExtras = onBackToExtras,
    modifier = modifier
  )
}

@Composable
private fun LegacyClassWizCalculatorUnused(
  metrics: ResponsiveMetrics,
  onBackToExtras: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val localView = LocalView.current
  val clipboardManager = LocalClipboardManager.current
  val palette = currentGlassPalette()
  val isDarkMode = palette.isDark

  BackHandler { onBackToExtras() }

  // Core ClassWiz Calculator State
  var currentMode by rememberSaveable { mutableStateOf(ClassWizMode.COMP) }
  var angleUnit by rememberSaveable { mutableStateOf(ClassWizAngleUnit.DEG) }
  var numberFormat by rememberSaveable { mutableStateOf(ClassWizNumberFormat.NORM) }
  var fixDigits by rememberSaveable { mutableIntStateOf(4) }
  var engShift by rememberSaveable { mutableIntStateOf(0) }

  var isShiftActive by rememberSaveable { mutableStateOf(false) }
  var isAlphaActive by rememberSaveable { mutableStateOf(false) }
  var isHypPending by rememberSaveable { mutableStateOf(false) }
  var isStoPending by rememberSaveable { mutableStateOf(false) }
  var isExactMode by rememberSaveable { mutableStateOf(true) }
  var isMixedFraction by rememberSaveable { mutableStateOf(false) }
  var isDmsOutput by rememberSaveable { mutableStateOf(false) }
  var complexPolarOutput by rememberSaveable { mutableStateOf(false) }

  var exprText by rememberSaveable { mutableStateOf("") }
  var cursorPos by rememberSaveable { mutableIntStateOf(0) }
  var displayResult by rememberSaveable { mutableStateOf("0") }
  var lastNumericAns by rememberSaveable { mutableStateOf(0.0) }
  var lastComplexAns by remember { mutableStateOf(ComplexNum(0.0, 0.0)) }
  var lastMatrixResult by remember { mutableStateOf<ClassWizMatrixData?>(null) }
  var lastVectorResult by remember { mutableStateOf<ClassWizVectorData?>(null) }
  var statusToast by remember { mutableStateOf<String?>(null) }

  val variables = remember {
    mutableStateMapOf(
      "A" to 0.0, "B" to 0.0, "C" to 0.0, "D" to 0.0,
      "E" to 0.0, "F" to 0.0, "X" to 0.0, "Y" to 0.0, "M" to 0.0
    )
  }
  val history = remember { mutableStateListOf<ClassWizHistoryEntry>() }
  var historyIndex by rememberSaveable { mutableIntStateOf(-1) }

  // Dialog & Sheet Flags
  var showMenuModal by remember { mutableStateOf(false) }
  var showSetupModal by remember { mutableStateOf(false) }
  var showOptnModal by remember { mutableStateOf(false) }
  var showVariablesModal by remember { mutableStateOf(false) }
  var showCalcPrompt by remember { mutableStateOf(false) }
  var calcPromptVars by remember { mutableStateOf<List<String>>(emptyList()) }
  var calcPromptDrafts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
  var isModePanelExpanded by rememberSaveable { mutableStateOf(true) }

  // Mode 3: Statistics State
  var statModel by rememberSaveable { mutableStateOf(StatRegModel.LINEAR) }
  val statRows = remember { mutableStateListOf<Pair<Double, Double>>() }
  var statXInput by rememberSaveable { mutableStateOf("") }
  var statYInput by rememberSaveable { mutableStateOf("") }
  var statPredX by rememberSaveable { mutableStateOf("") }
  var statPredY by rememberSaveable { mutableStateOf("") }

  // Mode 4: Equation State
  var eqnSubMode by rememberSaveable { mutableStateOf(EqnSubMode.SIMUL_2) }
  val simulCoeffs = remember { mutableStateListOf(*Array(20) { "0" }) }
  val polyCoeffs = remember { mutableStateListOf("1", "-5", "6", "0", "0") }

  // Mode 5: Matrix State (MatA..MatD)
  val matrices = remember {
    mutableStateMapOf(
      "MatA" to ClassWizMatrixData(2, 2, listOf(2.0, 1.0, 1.0, 3.0)),
      "MatB" to ClassWizMatrixData(2, 2, listOf(1.0, 0.0, 0.0, 1.0)),
      "MatC" to ClassWizMatrixData(2, 2, listOf(0.0, 0.0, 0.0, 0.0)),
      "MatD" to ClassWizMatrixData(2, 2, listOf(0.0, 0.0, 0.0, 0.0))
    )
  }
  var activeMatKey by rememberSaveable { mutableStateOf("MatA") }

  // Mode 6: Vector State (VctA..VctC)
  val vectors = remember {
    mutableStateMapOf(
      "VctA" to ClassWizVectorData(3, listOf(3.0, 4.0, 0.0)),
      "VctB" to ClassWizVectorData(3, listOf(1.0, 2.0, 2.0)),
      "VctC" to ClassWizVectorData(3, listOf(0.0, 0.0, 0.0))
    )
  }
  var activeVctKey by rememberSaveable { mutableStateOf("VctA") }

  // Mode 7: Table State
  var tableFx by rememberSaveable { mutableStateOf("X^2 - 3X + 2") }
  var tableGx by rememberSaveable { mutableStateOf("") }
  var tableStart by rememberSaveable { mutableStateOf("-2") }
  var tableEnd by rememberSaveable { mutableStateOf("5") }
  var tableStep by rememberSaveable { mutableStateOf("1") }

  fun refreshCompDisplayFromNumeric(valNum: Double, prefix: String = "") {
    lastNumericAns = cleanZero(valNum)
    val text = when {
      isDmsOutput -> formatDmsOutput(lastNumericAns)
      isExactMode && engShift == 0 && numberFormat == ClassWizNumberFormat.NORM -> {
        tryFormatExactSymbolic(lastNumericAns, isMixedFraction)
          ?: formatDecimalCompact(lastNumericAns, numberFormat, fixDigits, engShift)
      }
      else -> formatDecimalCompact(lastNumericAns, numberFormat, fixDigits, engShift)
    }
    displayResult = if (prefix.isNotEmpty()) "$prefix$text" else text
  }

  fun refreshComplexDisplay(z: ComplexNum) {
    lastComplexAns = z
    displayResult = formatComplexDisplay(
      z = z,
      polarMode = complexPolarOutput,
      angleUnit = angleUnit,
      exactMode = isExactMode,
      format = numberFormat,
      fixDigits = fixDigits
    )
  }

  fun insertToken(token: String) {
    val safeCursor = cursorPos.coerceIn(0, exprText.length)
    exprText = exprText.substring(0, safeCursor) + token + exprText.substring(safeCursor)
    cursorPos = safeCursor + token.length
    isShiftActive = false
    isAlphaActive = false
    isHypPending = false
  }

  fun deleteAtCursor() {
    if (cursorPos <= 0 || exprText.isEmpty()) return
    val safeCursor = cursorPos.coerceIn(1, exprText.length)
    val multiTokens = listOf(
      "integral(", "deriv(", "logBase(", "asinh(", "acosh(", "atanh(",
      "nroot(", "Conjg(", "sinh(", "cosh(", "tanh(", "asin(", "acos(", "atan(",
      "sqrt(", "cbrt(", "RanInt(", "Ran#", "sin(", "cos(", "tan(", "log(",
      "abs(", "Pol(", "Rec(", "arg(", "ReP(", "ImP(", "MatA", "MatB", "MatC",
      "MatD", "VctA", "VctB", "VctC", "Det(", "Trn(", "UnitV(", "Angle(",
      "Ans", "sum(", "ln(", "×10^", "⁻¹"
    )
    val prefix = exprText.substring(0, safeCursor)
    val matched = multiTokens.firstOrNull { prefix.endsWith(it) }
    val delLen = matched?.length ?: 1
    exprText = exprText.substring(0, safeCursor - delLen) + exprText.substring(safeCursor)
    cursorPos = (safeCursor - delLen).coerceAtLeast(0)
  }

  fun executeEvaluation(forceApprox: Boolean = false) {
    playCardHapticSound(context, localView)
    if (forceApprox) isExactMode = false
    engShift = 0
    isDmsOutput = false
    val raw = exprText.trim()
    if (raw.isEmpty()) {
      displayResult = "0"
      return
    }
    try {
      when (currentMode) {
        ClassWizMode.COMP -> {
          if (raw.contains("=")) {
            val (rootX, _) = solveEquationForX(raw, variables + ("Ans" to lastNumericAns), angleUnit)
            variables["X"] = cleanZero(rootX)
            refreshCompDisplayFromNumeric(rootX, prefix = "X = ")
            history.add(0, ClassWizHistoryEntry(raw, displayResult, rootX))
          } else {
            val res = evaluateScalar(raw, variables + ("Ans" to lastNumericAns), angleUnit)
            if (raw.startsWith("Pol(") && raw.contains(",")) {
              val args = raw.removePrefix("Pol(").removeSuffix(")").split(",")
              if (args.size == 2) {
                val x = evaluateScalar(args[0], variables, angleUnit)
                val y = evaluateScalar(args[1], variables, angleUnit)
                val r = hypot(x, y)
                val th = angleUnit.fromRadians(atan2(y, x))
                lastNumericAns = r
                variables["X"] = r
                variables["Y"] = th
                displayResult = "r=${formatDecimalCompact(r, numberFormat, fixDigits)}, θ=${formatDecimalCompact(th, numberFormat, fixDigits)}"
                history.add(0, ClassWizHistoryEntry(raw, displayResult, r))
                return
              }
            }
            if (raw.startsWith("Rec(") && raw.contains(",")) {
              val args = raw.removePrefix("Rec(").removeSuffix(")").split(",")
              if (args.size == 2) {
                val r = evaluateScalar(args[0], variables, angleUnit)
                val th = evaluateScalar(args[1], variables, angleUnit)
                val rad = angleUnit.toRadians(th)
                val x = r * cos(rad)
                val y = r * sin(rad)
                lastNumericAns = x
                variables["X"] = x
                variables["Y"] = y
                displayResult = "x=${formatDecimalCompact(x, numberFormat, fixDigits)}, y=${formatDecimalCompact(y, numberFormat, fixDigits)}"
                history.add(0, ClassWizHistoryEntry(raw, displayResult, x))
                return
              }
            }
            refreshCompDisplayFromNumeric(res)
            history.add(0, ClassWizHistoryEntry(raw, displayResult, res))
          }
          historyIndex = -1
        }
        ClassWizMode.CMPLX -> {
          val cVars = variables.mapValues { ComplexNum(it.value, 0.0) } + ("Ans" to lastComplexAns)
          val z = evaluateComplex(raw, cVars, angleUnit)
          refreshComplexDisplay(z)
          history.add(0, ClassWizHistoryEntry(raw, displayResult, z.re))
          historyIndex = -1
        }
        ClassWizMode.MATRIX -> {
          val mat = evaluateMatrixExpression(raw, matrices, variables + ("Ans" to lastNumericAns), angleUnit)
          if (mat.rows == 1 && mat.cols == 1 && raw.startsWith("Det(")) {
            lastMatrixResult = null
            refreshCompDisplayFromNumeric(mat.get(0, 0))
          } else {
            lastMatrixResult = mat
            displayResult = "${mat.rows}×${mat.cols} Matrix Computed"
          }
        }
        ClassWizMode.VECTOR -> {
          val vRes = evaluateVectorExpression(raw, vectors, variables + ("Ans" to lastNumericAns), angleUnit)
          if (vRes.second != null) {
            lastVectorResult = null
            refreshCompDisplayFromNumeric(vRes.second!!)
          } else if (vRes.first != null) {
            lastVectorResult = vRes.first
            val vec = vRes.first!!
            displayResult = "[ " + (0 until vec.dim).joinToString(", ") {
              formatDecimalCompact(vec.get(it), numberFormat, fixDigits)
            } + " ]"
          }
        }
        else -> {
          val res = evaluateScalar(raw, variables + ("Ans" to lastNumericAns), angleUnit)
          refreshCompDisplayFromNumeric(res)
        }
      }
    } catch (e: Throwable) {
      displayResult = e.message?.take(34) ?: "Math ERROR"
    }
  }

  fun handleVariableKey(varName: String) {
    if (isStoPending) {
      isStoPending = false
      val valToStore = if (exprText.isNotBlank()) {
        try {
          evaluateScalar(exprText, variables + ("Ans" to lastNumericAns), angleUnit)
        } catch (_: Throwable) {
          lastNumericAns
        }
      } else {
        lastNumericAns
      }
      variables[varName] = cleanZero(valToStore)
      lastNumericAns = cleanZero(valToStore)
      displayResult = "$varName ← ${formatDecimalCompact(valToStore, numberFormat, fixDigits)}"
      statusToast = "Stored in $varName"
    } else {
      insertToken(varName)
    }
  }

  val headerTitleColor = palette.textPrimary
  val headerSubtitleColor = palette.textSecondary
  val accentCyan = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)

  Column(
    modifier = modifier
      .fillMaxSize()
      .widthIn(max = 680.dp)
      .padding(
        start = metrics.scaledDp(12),
        end = metrics.scaledDp(12),
        top = metrics.scaledDp(12),
        bottom = metrics.scaledDp(92)
      )
      .testTag("classwiz_calculator_screen"),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // 1. TOP HEADER BAR
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
        IconButton(
          onClick = onBackToExtras,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(palette.nestedCardFill)
            .border(palette.nestedBorder, CircleShape)
            .testTag("classwiz_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Extras",
            tint = headerTitleColor,
            modifier = Modifier.size(19.dp)
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "ClassWiz fx-991EX Pro",
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = AppleSansFamily,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            ),
            color = headerTitleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${currentMode.title} (${currentMode.badge}) • ${currentMode.subtitle}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = AppleSansFamily,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            ),
            color = accentCyan,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      RecessedGlassPill(
        isDarkMode = isDarkMode,
        fillColor = accentCyan.copy(alpha = 0.16f),
        borderColor = accentCyan.copy(alpha = 0.55f),
        onClick = {
          playCardHapticSound(context, localView)
          showMenuModal = true
        },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
        modifier = Modifier.testTag("classwiz_top_mode_chip")
      ) {
        Text(
          text = "MODE ${currentMode.code}",
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
          ),
          color = accentCyan
        )
      }
    }

    // 2. MAIN SCROLLABLE CALCULATOR CHASSIS (Ensures zero clipping on all phone heights)
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // CALCULATOR METALLIC SLATE BEZEL + NATURAL TEXTBOOK LCD VIEWPORT
      val bezelFill = if (isDarkMode) Color(0xFF111827).copy(alpha = 0.88f) else Color(0xFF1E293B).copy(alpha = 0.94f)
      val lcdBg = Color(0xFFE9F2EC)
      val lcdInk = Color(0xFF0F172A)

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = bezelFill,
        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.22f)),
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // LCD SCREEN
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = lcdBg,
            border = BorderStroke(1.5.dp, Color(0xFF64748B).copy(alpha = 0.55f)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("classwiz_lcd_viewport")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              // Top LCD Status Strip
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  LcdFlagBadge("S", isShiftActive, Color(0xFFD97706), Color.White)
                  LcdFlagBadge("A", isAlphaActive, Color(0xFFDC2626), Color.White)
                  if (isHypPending) LcdFlagBadge("HYP", true, Color(0xFF2563EB), Color.White)
                  if (isStoPending) LcdFlagBadge("STO", true, Color(0xFF059669), Color.White)
                  if (abs(variables["M"] ?: 0.0) > 1e-12) {
                    LcdFlagBadge("M", true, Color(0xFF334155), Color.White)
                  }

                  // Tappable Angle Unit Pill (D / R / G)
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Color(0xFF0F172A))
                      .clickable {
                        angleUnit = when (angleUnit) {
                          ClassWizAngleUnit.DEG -> ClassWizAngleUnit.RAD
                          ClassWizAngleUnit.RAD -> ClassWizAngleUnit.GRAD
                          ClassWizAngleUnit.GRAD -> ClassWizAngleUnit.DEG
                        }
                      }
                      .padding(horizontal = 6.dp, vertical = 1.dp)
                      .testTag("classwiz_angle_badge")
                  ) {
                    Text(
                      text = angleUnit.badge,
                      fontFamily = JetBrainsMonoFamily,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = Color.White
                    )
                  }

                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Color(0xFF0284C7).copy(alpha = 0.16f))
                      .padding(horizontal = 5.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = currentMode.badge,
                      fontFamily = JetBrainsMonoFamily,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF0369A1)
                    )
                  }
                }

                Row(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (engShift != 0) "ENG" else numberFormat.label,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                  )
                  Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copy Result",
                    tint = Color(0xFF334155),
                    modifier = Modifier
                      .size(14.dp)
                      .clickable {
                        clipboardManager.setText(AnnotatedString(displayResult))
                        Toast.makeText(context, "Copied: $displayResult", Toast.LENGTH_SHORT).show()
                      }
                  )
                }
              }

              // Upper Expression Line with Cursor
              val displayExpr = remember(exprText, cursorPos) {
                val safe = cursorPos.coerceIn(0, exprText.length)
                val left = toNaturalDisplayText(exprText.substring(0, safe))
                val right = toNaturalDisplayText(exprText.substring(safe))
                if (exprText.isEmpty()) "│" else "$left│$right"
              }
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = displayExpr,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum"
                  ),
                  color = lcdInk,
                  maxLines = 1,
                  modifier = Modifier.testTag("classwiz_lcd_expression")
                )
              }

              // Lower Result Line
              Text(
                text = displayResult,
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Bold,
                  fontFeatureSettings = "tnum"
                ),
                color = lcdInk,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("classwiz_lcd_result")
              )
            }
          }

          if (statusToast != null) {
            Text(
              text = statusToast!!,
              fontFamily = JetBrainsMonoFamily,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFFBBF24),
              modifier = Modifier.padding(horizontal = 4.dp)
            )
          }
        }
      }

      // MODE-SPECIFIC WORKSPACE PANEL (STAT, EQN, MATRIX, VECTOR, TABLE)
      if (currentMode != ClassWizMode.COMP && currentMode != ClassWizMode.CMPLX) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = palette.cardFill,
          border = palette.glassBorder,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { isModePanelExpanded = !isModePanelExpanded },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${currentMode.title.uppercase(Locale.US)} WORKSPACE",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp,
                color = accentCyan
              )
              Icon(
                imageVector = if (isModePanelExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = "Toggle Mode Workspace",
                tint = palette.textSecondary,
                modifier = Modifier.size(18.dp)
              )
            }

            AnimatedVisibility(
              visible = isModePanelExpanded,
              enter = expandVertically() + fadeIn(),
              exit = shrinkVertically() + fadeOut()
            ) {
              when (currentMode) {
                ClassWizMode.STAT -> ClassWizStatWorkspace(
                  statModel = statModel,
                  onSelectModel = { statModel = it },
                  rows = statRows,
                  xInput = statXInput,
                  onXInputChange = { statXInput = it },
                  yInput = statYInput,
                  onYInputChange = { statYInput = it },
                  predX = statPredX,
                  onPredXChange = { statPredX = it },
                  predY = statPredY,
                  onPredYChange = { statPredY = it },
                  accentCyan = accentCyan
                )
                ClassWizMode.EQN -> ClassWizEqnWorkspace(
                  subMode = eqnSubMode,
                  onSelectSubMode = { eqnSubMode = it },
                  simulCoeffs = simulCoeffs,
                  polyCoeffs = polyCoeffs,
                  isExactMode = isExactMode,
                  accentCyan = accentCyan
                )
                ClassWizMode.MATRIX -> ClassWizMatrixWorkspace(
                  matrices = matrices,
                  activeKey = activeMatKey,
                  onSelectKey = { activeMatKey = it },
                  lastMatrixResult = lastMatrixResult,
                  isExactMode = isExactMode,
                  onQuickOp = { expr ->
                    exprText = expr
                    cursorPos = expr.length
                    executeEvaluation()
                  },
                  accentCyan = accentCyan
                )
                ClassWizMode.VECTOR -> ClassWizVectorWorkspace(
                  vectors = vectors,
                  activeKey = activeVctKey,
                  onSelectKey = { activeVctKey = it },
                  lastVectorResult = lastVectorResult,
                  angleUnit = angleUnit,
                  onQuickOp = { expr ->
                    exprText = expr
                    cursorPos = expr.length
                    executeEvaluation()
                  },
                  accentCyan = accentCyan
                )
                ClassWizMode.TABLE -> ClassWizTableWorkspace(
                  fx = tableFx,
                  onFxChange = { tableFx = it },
                  gx = tableGx,
                  onGxChange = { tableGx = it },
                  startText = tableStart,
                  onStartChange = { tableStart = it },
                  endText = tableEnd,
                  onEndChange = { tableEnd = it },
                  stepText = tableStep,
                  onStepChange = { tableStep = it },
                  angleUnit = angleUnit,
                  vars = variables,
                  accentCyan = accentCyan
                )
                else -> {}
              }
            }
          }
        }
      }

      // 3. AUTHENTIC CASIO CLASSWIZ KEYPAD CHASSIS
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (isDarkMode) Color(0xFF0F172A).copy(alpha = 0.85f) else Color(0xFF1E293B).copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
          // TOP SYSTEM ROW: SHIFT | ALPHA | 4-WAY REPLAY D-PAD | MENU | AC
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ClassWizSystemKey(
                  label = "SHIFT",
                  isActive = isShiftActive,
                  activeColor = Color(0xFFD97706),
                  textColor = Color(0xFFFBBF24),
                  modifier = Modifier.weight(1f).testTag("cw_key_shift")
                ) {
                  isShiftActive = !isShiftActive
                  if (isShiftActive) isAlphaActive = false
                }
                ClassWizSystemKey(
                  label = "ALPHA",
                  isActive = isAlphaActive,
                  activeColor = Color(0xFFDC2626),
                  textColor = Color(0xFFF87171),
                  modifier = Modifier.weight(1f).testTag("cw_key_alpha")
                ) {
                  isAlphaActive = !isAlphaActive
                  if (isAlphaActive) isShiftActive = false
                }
              }
              Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ClassWizSciKey(
                  mainLabel = "OPTN",
                  shiftLabel = "",
                  alphaLabel = "",
                  modifier = Modifier.weight(1f).testTag("cw_key_optn")
                ) {
                  showOptnModal = true
                  isShiftActive = false
                }
                ClassWizSciKey(
                  mainLabel = "CALC",
                  shiftLabel = "SOLVE",
                  alphaLabel = "=",
                  modifier = Modifier.weight(1f).testTag("cw_key_calc")
                ) {
                  when {
                    isShiftActive -> {
                      isShiftActive = false
                      executeEvaluation()
                    }
                    isAlphaActive -> insertToken("=")
                    else -> {
                      val varsFound = listOf("A", "B", "C", "D", "E", "F", "X", "Y", "M")
                        .filter { Regex("\\b$it\\b").containsMatchIn(exprText) }
                      if (varsFound.isEmpty()) {
                        executeEvaluation()
                      } else {
                        calcPromptVars = varsFound
                        calcPromptDrafts = varsFound.associateWith {
                          formatDecimalCompact(variables[it] ?: 0.0)
                        }
                        showCalcPrompt = true
                      }
                    }
                  }
                }
              }
            }

            // 4-WAY CIRCULAR REPLAY D-PAD
            Surface(
              shape = CircleShape,
              color = Color(0xFF334155),
              border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.25f)),
              shadowElevation = 6.dp,
              modifier = Modifier.size(92.dp)
            ) {
              Box(modifier = Modifier.fillMaxSize()) {
                // Up (History older)
                IconButton(
                  onClick = {
                    if (history.isNotEmpty()) {
                      historyIndex = (historyIndex + 1).coerceAtMost(history.lastIndex)
                      val h = history[historyIndex]
                      exprText = h.expression
                      cursorPos = exprText.length
                      displayResult = h.exactOrDisplay
                    }
                  },
                  modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopCenter)
                    .testTag("cw_dpad_up")
                ) {
                  Icon(Icons.Filled.KeyboardArrowUp, "History Up", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                // Down (History newer)
                IconButton(
                  onClick = {
                    if (historyIndex > 0) {
                      historyIndex--
                      val h = history[historyIndex]
                      exprText = h.expression
                      cursorPos = exprText.length
                      displayResult = h.exactOrDisplay
                    } else {
                      historyIndex = -1
                    }
                  },
                  modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomCenter)
                    .testTag("cw_dpad_down")
                ) {
                  Icon(Icons.Filled.KeyboardArrowDown, "History Down", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                // Left (Cursor left)
                IconButton(
                  onClick = { cursorPos = (cursorPos - 1).coerceAtLeast(0) },
                  modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterStart)
                    .testTag("cw_dpad_left")
                ) {
                  Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Cursor Left", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                // Right (Cursor right)
                IconButton(
                  onClick = { cursorPos = (cursorPos + 1).coerceAtMost(exprText.length) },
                  modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterEnd)
                    .testTag("cw_dpad_right")
                ) {
                  Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Cursor Right", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text(
                  text = "REPLAY",
                  fontSize = 7.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF94A3B8),
                  modifier = Modifier.align(Alignment.Center)
                )
              }
            }

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ClassWizSystemKey(
                  label = "MENU",
                  shiftHint = "SETUP",
                  isActive = showMenuModal || showSetupModal,
                  activeColor = Color(0xFF0284C7),
                  textColor = Color.White,
                  modifier = Modifier.weight(1f).testTag("cw_key_menu")
                ) {
                  if (isShiftActive) {
                    isShiftActive = false
                    showSetupModal = true
                  } else {
                    showMenuModal = true
                  }
                }
                ClassWizSystemKey(
                  label = "ON",
                  shiftHint = "OFF",
                  isActive = false,
                  activeColor = Color(0xFF0F172A),
                  textColor = Color(0xFF38BDF8),
                  modifier = Modifier.weight(1f).testTag("cw_key_on")
                ) {
                  if (isShiftActive) {
                    isShiftActive = false
                    exprText = ""
                    cursorPos = 0
                    displayResult = "CASIO ClassWiz"
                    statusToast = "Calculator reset"
                  } else {
                    exprText = ""
                    cursorPos = 0
                    displayResult = "0"
                    engShift = 0
                    isDmsOutput = false
                    statusToast = null
                  }
                }
              }
              Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ClassWizSciKey(
                  mainLabel = "∫dx",
                  shiftLabel = "d/dx",
                  alphaLabel = "",
                  modifier = Modifier.weight(1f).testTag("cw_key_integral")
                ) {
                  if (isShiftActive) insertToken("deriv(,0)")
                  else insertToken("integral(,0,1)")
                }
                ClassWizSciKey(
                  mainLabel = "x⁻¹",
                  shiftLabel = "x!",
                  alphaLabel = "",
                  modifier = Modifier.weight(1f).testTag("cw_key_inv")
                ) {
                  if (isShiftActive) insertToken("!")
                  else insertToken("⁻¹")
                }
              }
            }
          }

          // SCIENTIFIC FUNCTION GRID (Rows 1-3: 6 columns each)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            ClassWizSciKey("■/□", "d/c", "", Modifier.weight(1f).testTag("cw_key_frac")) {
              if (isShiftActive) {
                isMixedFraction = !isMixedFraction
                isShiftActive = false
                refreshCompDisplayFromNumeric(lastNumericAns)
              } else insertToken("/")
            }
            ClassWizSciKey("√■", "³√■", "", Modifier.weight(1f).testTag("cw_key_sqrt")) {
              if (isShiftActive) insertToken("cbrt(") else insertToken("sqrt(")
            }
            ClassWizSciKey("x²", "x³", "", Modifier.weight(1f).testTag("cw_key_sq")) {
              if (isShiftActive) insertToken("³") else insertToken("²")
            }
            ClassWizSciKey("x^■", "ˣ√■", "", Modifier.weight(1f).testTag("cw_key_pow")) {
              if (isShiftActive) insertToken("nroot(") else insertToken("^")
            }
            ClassWizSciKey("log", "10^x", "", Modifier.weight(1f).testTag("cw_key_log")) {
              if (isShiftActive) insertToken("10^") else insertToken("log(")
            }
            ClassWizSciKey("ln", "e^x", "", Modifier.weight(1f).testTag("cw_key_ln")) {
              if (isShiftActive) insertToken("exp(") else insertToken("ln(")
            }
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            ClassWizSciKey("(-)", "∠", "A", Modifier.weight(1f).testTag("cw_key_neg")) {
              when {
                isAlphaActive -> handleVariableKey("A")
                isShiftActive -> insertToken("∠")
                else -> insertToken("-")
              }
            }
            ClassWizSciKey("° ' \"", "logₐ", "B", Modifier.weight(1f).testTag("cw_key_dms")) {
              when {
                isAlphaActive -> handleVariableKey("B")
                isShiftActive -> insertToken("logBase(")
                else -> {
                  if (exprText.isEmpty() && lastNumericAns != 0.0) {
                    isDmsOutput = !isDmsOutput
                    refreshCompDisplayFromNumeric(lastNumericAns)
                  } else {
                    insertToken("°")
                  }
                }
              }
            }
            ClassWizSciKey("hyp", "Σ", "C", Modifier.weight(1f).testTag("cw_key_hyp")) {
              when {
                isAlphaActive -> handleVariableKey("C")
                isShiftActive -> insertToken("sum(,1,5)")
                else -> isHypPending = !isHypPending
              }
            }
            ClassWizSciKey("sin", "sin⁻¹", "D", Modifier.weight(1f).testTag("cw_key_sin")) {
              when {
                isAlphaActive -> handleVariableKey("D")
                isHypPending && isShiftActive -> insertToken("asinh(")
                isHypPending -> insertToken("sinh(")
                isShiftActive -> insertToken("asin(")
                else -> insertToken("sin(")
              }
            }
            ClassWizSciKey("cos", "cos⁻¹", "E", Modifier.weight(1f).testTag("cw_key_cos")) {
              when {
                isAlphaActive -> handleVariableKey("E")
                isHypPending && isShiftActive -> insertToken("acosh(")
                isHypPending -> insertToken("cosh(")
                isShiftActive -> insertToken("acos(")
                else -> insertToken("cos(")
              }
            }
            ClassWizSciKey("tan", "tan⁻¹", "F", Modifier.weight(1f).testTag("cw_key_tan")) {
              when {
                isAlphaActive -> handleVariableKey("F")
                isHypPending && isShiftActive -> insertToken("atanh(")
                isHypPending -> insertToken("tanh(")
                isShiftActive -> insertToken("atan(")
                else -> insertToken("tan(")
              }
            }
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            ClassWizSciKey("STO", "RCL", "", Modifier.weight(1f).testTag("cw_key_sto")) {
              if (isShiftActive) {
                isShiftActive = false
                showVariablesModal = true
              } else {
                isStoPending = !isStoPending
                statusToast = if (isStoPending) "Tap variable A–F, X, Y, M to store" else null
              }
            }
            ClassWizSciKey("ENG", "←", "i", Modifier.weight(1f).testTag("cw_key_eng")) {
              when {
                isAlphaActive -> insertToken("i")
                isShiftActive -> {
                  isShiftActive = false
                  engShift -= 1
                  isExactMode = false
                  refreshCompDisplayFromNumeric(lastNumericAns)
                }
                else -> {
                  engShift += 1
                  isExactMode = false
                  refreshCompDisplayFromNumeric(lastNumericAns)
                }
              }
            }
            ClassWizSciKey("(", "%", "", Modifier.weight(1f).testTag("cw_key_lparen")) {
              if (isShiftActive) insertToken("%") else insertToken("(")
            }
            ClassWizSciKey(")", ",", "X", Modifier.weight(1f).testTag("cw_key_rparen")) {
              when {
                isAlphaActive -> handleVariableKey("X")
                isShiftActive -> insertToken(",")
                else -> insertToken(")")
              }
            }
            ClassWizSciKey("S⇔D", "ab/c", "Y", Modifier.weight(1f).testTag("cw_key_sd")) {
              when {
                isAlphaActive -> handleVariableKey("Y")
                isShiftActive -> {
                  isMixedFraction = !isMixedFraction
                  isExactMode = true
                  isShiftActive = false
                  refreshCompDisplayFromNumeric(lastNumericAns)
                }
                else -> {
                  isExactMode = !isExactMode
                  engShift = 0
                  if (currentMode == ClassWizMode.CMPLX) refreshComplexDisplay(lastComplexAns)
                  else refreshCompDisplayFromNumeric(lastNumericAns)
                }
              }
            }
            ClassWizSciKey("M+", "M-", "M", Modifier.weight(1f).testTag("cw_key_mplus")) {
              when {
                isAlphaActive -> handleVariableKey("M")
                isShiftActive -> {
                  isShiftActive = false
                  val cur = if (exprText.isNotBlank()) {
                    runCatching { evaluateScalar(exprText, variables + ("Ans" to lastNumericAns), angleUnit) }.getOrElse { lastNumericAns }
                  } else lastNumericAns
                  variables["M"] = cleanZero((variables["M"] ?: 0.0) - cur)
                  statusToast = "M = ${formatDecimalCompact(variables["M"] ?: 0.0)}"
                }
                else -> {
                  val cur = if (exprText.isNotBlank()) {
                    runCatching { evaluateScalar(exprText, variables + ("Ans" to lastNumericAns), angleUnit) }.getOrElse { lastNumericAns }
                  } else lastNumericAns
                  variables["M"] = cleanZero((variables["M"] ?: 0.0) + cur)
                  statusToast = "M = ${formatDecimalCompact(variables["M"] ?: 0.0)}"
                }
              }
            }
          }

          // NUMERIC & PRIMARY OPERATOR KEYPAD (4 rows × 5 columns)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ClassWizNumKey("7", Modifier.weight(1f).testTag("cw_key_7")) { insertToken("7") }
            ClassWizNumKey("8", Modifier.weight(1f).testTag("cw_key_8")) { insertToken("8") }
            ClassWizNumKey("9", Modifier.weight(1f).testTag("cw_key_9")) { insertToken("9") }
            ClassWizActionKey("DEL", Color(0xFFEA580C), Modifier.weight(1f).testTag("cw_key_del")) { deleteAtCursor() }
            ClassWizActionKey("AC", Color(0xFFDC2626), Modifier.weight(1f).testTag("cw_key_ac")) {
              exprText = ""
              cursorPos = 0
              displayResult = "0"
              engShift = 0
              isDmsOutput = false
              isShiftActive = false
              isAlphaActive = false
            }
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ClassWizNumKey("4", Modifier.weight(1f).testTag("cw_key_4")) { insertToken("4") }
            ClassWizNumKey("5", Modifier.weight(1f).testTag("cw_key_5")) { insertToken("5") }
            ClassWizNumKey("6", Modifier.weight(1f).testTag("cw_key_6")) { insertToken("6") }
            ClassWizOpKey("×", "nPr", modifier = Modifier.weight(1f).testTag("cw_key_mul")) {
              if (isShiftActive) insertToken("P") else insertToken("×")
            }
            ClassWizOpKey("÷", "nCr", modifier = Modifier.weight(1f).testTag("cw_key_div")) {
              if (isShiftActive) insertToken("C") else insertToken("÷")
            }
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ClassWizNumKey("1", Modifier.weight(1f).testTag("cw_key_1")) { insertToken("1") }
            ClassWizNumKey("2", Modifier.weight(1f).testTag("cw_key_2")) { insertToken("2") }
            ClassWizNumKey("3", Modifier.weight(1f).testTag("cw_key_3")) { insertToken("3") }
            ClassWizOpKey("+", "Pol(", modifier = Modifier.weight(1f).testTag("cw_key_add")) {
              if (isShiftActive) insertToken("Pol(") else insertToken("+")
            }
            ClassWizOpKey("-", "Rec(", modifier = Modifier.weight(1f).testTag("cw_key_sub")) {
              if (isShiftActive) insertToken("Rec(") else insertToken("-")
            }
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ClassWizNumKey("0", Modifier.weight(1f).testTag("cw_key_0")) { insertToken("0") }
            ClassWizNumKey(".", Modifier.weight(1f).testTag("cw_key_dot")) { insertToken(".") }
            ClassWizOpKey("×10ˣ", "π", "e", Modifier.weight(1f).testTag("cw_key_exp10")) {
              when {
                isAlphaActive -> insertToken("e")
                isShiftActive -> insertToken("π")
                else -> insertToken("×10^")
              }
            }
            ClassWizOpKey("Ans", "Ran#", "", Modifier.weight(1f).testTag("cw_key_ans")) {
              if (isShiftActive) insertToken("Ran#") else insertToken("Ans")
            }
            ClassWizActionKey("=", Color(0xFF0284C7), Modifier.weight(1f).testTag("cw_key_eq")) {
              executeEvaluation(forceApprox = isShiftActive)
              isShiftActive = false
            }
          }
        }
      }
    }
  }

  // MENU DIALOG (7 ClassWiz Mode Icons)
  if (showMenuModal) {
    Dialog(
      onDismissRequest = { showMenuModal = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Surface(
        shape = RoundedCornerShape(26.dp),
        color = palette.dialogSurface,
        border = palette.glassBorder,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .widthIn(max = 520.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "ClassWiz MENU • Select Mode",
              fontFamily = AppleSansFamily,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = palette.textPrimary
            )
            IconButton(onClick = { showMenuModal = false }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Filled.Close, "Close Menu", tint = palette.textSecondary)
            }
          }
          ClassWizMode.entries.chunked(2).forEach { pair ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              pair.forEach { mode ->
                val selected = currentMode == mode
                Surface(
                  onClick = {
                    currentMode = mode
                    showMenuModal = false
                    exprText = ""
                    cursorPos = 0
                    displayResult = if (mode == ClassWizMode.COMP || mode == ClassWizMode.CMPLX) "0" else mode.subtitle
                  },
                  shape = RoundedCornerShape(16.dp),
                  color = if (selected) accentCyan.copy(alpha = 0.20f) else palette.nestedCardFill,
                  border = BorderStroke(1.2.dp, if (selected) accentCyan else palette.nestedBorderColor),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("cw_menu_mode_${mode.badge}")
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(mode.icon, contentDescription = null, tint = accentCyan, modifier = Modifier.size(22.dp))
                    Column {
                      Text(
                        text = "${mode.code}: ${mode.title}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                      )
                      Text(
                        text = mode.badge,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = accentCyan
                      )
                    }
                  }
                }
              }
              if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
          }
        }
      }
    }
  }

  // OPTN MODAL (Mode-Contextual Operations)
  if (showOptnModal) {
    Dialog(onDismissRequest = { showOptnModal = false }) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = palette.dialogSurface,
        border = palette.glassBorder,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "OPTN • ${currentMode.title}",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
          )
          when (currentMode) {
            ClassWizMode.CMPLX -> {
              OptnActionRow("1: arg( — Argument", palette) { insertToken("arg("); showOptnModal = false }
              OptnActionRow("2: Conjg( — Conjugate", palette) { insertToken("Conjg("); showOptnModal = false }
              OptnActionRow("3: ReP( — Real Part", palette) { insertToken("ReP("); showOptnModal = false }
              OptnActionRow("4: ImP( — Imaginary Part", palette) { insertToken("ImP("); showOptnModal = false }
              OptnActionRow("5: ►r∠θ — Polar Display", palette) {
                complexPolarOutput = true
                refreshComplexDisplay(lastComplexAns)
                showOptnModal = false
              }
              OptnActionRow("6: ►a+bi — Rectangular Display", palette) {
                complexPolarOutput = false
                refreshComplexDisplay(lastComplexAns)
                showOptnModal = false
              }
            }
            ClassWizMode.MATRIX -> {
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("MatA", "MatB", "MatC", "MatD").forEach { key ->
                  RecessedGlassPill(isDarkMode = isDarkMode, onClick = { insertToken(key); showOptnModal = false }) {
                    Text(key, fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentCyan)
                  }
                }
              }
              OptnActionRow("Det( — Determinant", palette) { insertToken("Det("); showOptnModal = false }
              OptnActionRow("Trn( — Transpose", palette) { insertToken("Trn("); showOptnModal = false }
              OptnActionRow("Mat⁻¹ — Matrix Inverse", palette) { insertToken("⁻¹"); showOptnModal = false }
            }
            ClassWizMode.VECTOR -> {
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("VctA", "VctB", "VctC").forEach { key ->
                  RecessedGlassPill(isDarkMode = isDarkMode, onClick = { insertToken(key); showOptnModal = false }) {
                    Text(key, fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentCyan)
                  }
                }
              }
              OptnActionRow("• — Dot Product", palette) { insertToken("•"); showOptnModal = false }
              OptnActionRow("× — Cross Product", palette) { insertToken("×"); showOptnModal = false }
              OptnActionRow("abs( — Vector Magnitude", palette) { insertToken("abs("); showOptnModal = false }
              OptnActionRow("UnitV( — Unit Vector", palette) { insertToken("UnitV("); showOptnModal = false }
              OptnActionRow("Angle(A, B) — Angle Between Vectors", palette) { insertToken("Angle(VctA,VctB)"); showOptnModal = false }
            }
            else -> {
              OptnActionRow("abs( — Absolute Value |x|", palette) { insertToken("abs("); showOptnModal = false }
              OptnActionRow("sinh( / cosh( / tanh(", palette) { insertToken("sinh("); showOptnModal = false }
              OptnActionRow("nPr — Permutation (P)", palette) { insertToken("P"); showOptnModal = false }
              OptnActionRow("nCr — Combination (C)", palette) { insertToken("C"); showOptnModal = false }
              OptnActionRow("RanInt(a, b) — Random Integer", palette) { insertToken("RanInt(1,100)"); showOptnModal = false }
            }
          }
          TextButton(onClick = { showOptnModal = false }, modifier = Modifier.align(Alignment.End)) {
            Text("Close")
          }
        }
      }
    }
  }

  // SETUP MODAL (SHIFT + MENU)
  if (showSetupModal) {
    Dialog(onDismissRequest = { showSetupModal = false }) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = palette.dialogSurface,
        border = palette.glassBorder,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Tune, null, tint = accentCyan)
            Text("Calculator SETUP", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
          }
          Text("Angle Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textSecondary)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClassWizAngleUnit.entries.forEach { u ->
              RecessedGlassPill(
                isDarkMode = isDarkMode,
                fillColor = if (angleUnit == u) accentCyan.copy(alpha = 0.25f) else null,
                borderColor = if (angleUnit == u) accentCyan else null,
                onClick = { angleUnit = u }
              ) {
                Text("${u.label} (${u.badge})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
              }
            }
          }
          Text("Number Format", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textSecondary)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClassWizNumberFormat.entries.forEach { f ->
              RecessedGlassPill(
                isDarkMode = isDarkMode,
                fillColor = if (numberFormat == f) accentCyan.copy(alpha = 0.25f) else null,
                borderColor = if (numberFormat == f) accentCyan else null,
                onClick = {
                  numberFormat = f
                  refreshCompDisplayFromNumeric(lastNumericAns)
                }
              ) {
                Text(f.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
              }
            }
          }
          if (numberFormat != ClassWizNumberFormat.NORM) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("Digits: $fixDigits", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
              RecessedGlassPill(isDarkMode = isDarkMode, onClick = { fixDigits = (fixDigits - 1).coerceAtLeast(1) }) {
                Text("-", fontWeight = FontWeight.Bold, color = palette.textPrimary)
              }
              RecessedGlassPill(isDarkMode = isDarkMode, onClick = { fixDigits = (fixDigits + 1).coerceAtMost(9) }) {
                Text("+", fontWeight = FontWeight.Bold, color = palette.textPrimary)
              }
            }
          }
          TextButton(onClick = { showSetupModal = false }, modifier = Modifier.align(Alignment.End)) {
            Text("Done")
          }
        }
      }
    }
  }

  // RCL VARIABLE MEMORY INSPECTOR
  if (showVariablesModal) {
    Dialog(onDismissRequest = { showVariablesModal = false }) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = palette.dialogSurface,
        border = palette.glassBorder,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("RCL • Variable Memory", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
          listOf("A", "B", "C", "D", "E", "F", "X", "Y", "M").chunked(3).forEach { rowKeys ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              rowKeys.forEach { k ->
                Surface(
                  onClick = {
                    insertToken(k)
                    showVariablesModal = false
                  },
                  shape = RoundedCornerShape(10.dp),
                  color = palette.nestedCardFill,
                  border = palette.nestedBorder,
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(8.dp)) {
                    Text(k, fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = accentCyan)
                    Text(
                      formatDecimalCompact(variables[k] ?: 0.0),
                      fontFamily = JetBrainsMonoFamily,
                      fontSize = 12.sp,
                      color = palette.textPrimary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }
          }
          TextButton(onClick = { showVariablesModal = false }, modifier = Modifier.align(Alignment.End)) {
            Text("Close")
          }
        }
      }
    }
  }

  // CALC VARIABLE PROMPT DIALOG
  if (showCalcPrompt) {
    Dialog(onDismissRequest = { showCalcPrompt = false }) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = palette.dialogSurface,
        border = palette.glassBorder,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text("CALC • Assign Variables", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
          calcPromptVars.forEach { vName ->
            OutlinedTextField(
              value = calcPromptDrafts[vName] ?: "0",
              onValueChange = { calcPromptDrafts = calcPromptDrafts + (vName to it) },
              label = { Text("Value for $vName") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
              colors = sibilOutlinedTextFieldColors(),
              modifier = Modifier.fillMaxWidth()
            )
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showCalcPrompt = false }) { Text("Cancel") }
            Button(
              onClick = {
                calcPromptDrafts.forEach { (k, rawVal) ->
                  variables[k] = runCatching { evaluateScalar(rawVal, variables, angleUnit) }.getOrDefault(0.0)
                }
                showCalcPrompt = false
                executeEvaluation()
              },
              colors = ButtonDefaults.buttonColors(containerColor = accentCyan)
            ) {
              Text("Evaluate", color = Color.White)
            }
          }
        }
      }
    }
  }
}

// ============================================================================
// KEYPAD UI BUILDING BLOCKS
// ============================================================================

@Composable
private fun LcdFlagBadge(text: String, active: Boolean, bg: Color, fg: Color) {
  if (!active) return
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(bg)
      .padding(horizontal = 5.dp, vertical = 1.dp)
  ) {
    Text(
      text = text,
      fontFamily = JetBrainsMonoFamily,
      fontSize = 9.5.sp,
      fontWeight = FontWeight.ExtraBold,
      color = fg
    )
  }
}

@Composable
private fun ClassWizSystemKey(
  label: String,
  shiftHint: String = "",
  isActive: Boolean,
  activeColor: Color,
  textColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    if (shiftHint.isNotEmpty()) {
      Text(
        text = shiftHint,
        fontFamily = JetBrainsMonoFamily,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFFBBF24),
        maxLines = 1
      )
    } else {
      Spacer(Modifier.height(11.dp))
    }
    Surface(
      onClick = onClick,
      shape = RoundedCornerShape(10.dp),
      color = if (isActive) activeColor else Color(0xFF334155),
      border = BorderStroke(1.dp, if (isActive) Color.White else Color.White.copy(alpha = 0.20f)),
      modifier = Modifier
        .fillMaxWidth()
        .height(34.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = label,
          fontFamily = JetBrainsMonoFamily,
          fontSize = 10.sp,
          fontWeight = FontWeight.ExtraBold,
          color = if (isActive) Color.White else textColor,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
private fun ClassWizSciKey(
  mainLabel: String,
  shiftLabel: String,
  alphaLabel: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = shiftLabel,
        fontFamily = JetBrainsMonoFamily,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFFBBF24),
        maxLines = 1
      )
      Text(
        text = alphaLabel,
        fontFamily = JetBrainsMonoFamily,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFF87171),
        maxLines = 1
      )
    }
    Surface(
      onClick = onClick,
      shape = RoundedCornerShape(9.dp),
      color = Color(0xFF1E293B),
      border = BorderStroke(0.9.dp, Color.White.copy(alpha = 0.16f)),
      modifier = Modifier
        .fillMaxWidth()
        .height(34.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = mainLabel,
          fontFamily = JetBrainsMonoFamily,
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
private fun ClassWizNumKey(
  label: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFFF8FAFC),
    border = BorderStroke(1.dp, Color.White),
    shadowElevation = 2.dp,
    modifier = modifier.height(48.dp)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label,
        fontFamily = AppleSansFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )
    }
  }
}

@Composable
private fun ClassWizOpKey(
  mainLabel: String,
  shiftLabel: String,
  alphaLabel: String = "",
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(shiftLabel, fontFamily = JetBrainsMonoFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), maxLines = 1)
      Text(alphaLabel, fontFamily = JetBrainsMonoFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF87171), maxLines = 1)
    }
    Surface(
      onClick = onClick,
      shape = RoundedCornerShape(11.dp),
      color = Color(0xFF334155),
      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = mainLabel,
          fontFamily = AppleSansFamily,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
private fun ClassWizActionKey(
  label: String,
  fillColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = fillColor,
    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
    shadowElevation = 4.dp,
    modifier = modifier.height(48.dp)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label,
        fontFamily = AppleSansFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        maxLines = 1
      )
    }
  }
}

@Composable
internal fun OptnActionRow(
  text: String,
  palette: GlassPalette,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = palette.nestedCardFill,
    border = palette.nestedBorder,
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = text,
      fontSize = 13.5.sp,
      fontWeight = FontWeight.SemiBold,
      color = palette.textPrimary,
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
    )
  }
}

// ============================================================================
// WORKSPACE PANELS: STAT, EQN, MATRIX, VECTOR, TABLE
// ============================================================================

@Composable
internal fun ClassWizStatWorkspace(
  statModel: StatRegModel,
  onSelectModel: (StatRegModel) -> Unit,
  rows: MutableList<Pair<Double, Double>>,
  xInput: String,
  onXInputChange: (String) -> Unit,
  yInput: String,
  onYInputChange: (String) -> Unit,
  predX: String,
  onPredXChange: (String) -> Unit,
  predY: String,
  onPredYChange: (String) -> Unit,
  accentCyan: Color
) {
  val palette = currentGlassPalette()
  val summary = remember(rows.toList(), statModel) { computeStatSummary(rows, statModel) }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      StatRegModel.entries.forEach { m ->
        val sel = statModel == m
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = if (sel) accentCyan.copy(alpha = 0.24f) else null,
          borderColor = if (sel) accentCyan else null,
          onClick = { onSelectModel(m) }
        ) {
          Text(m.label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (sel) accentCyan else palette.textPrimary)
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = xInput,
        onValueChange = onXInputChange,
        label = { Text("X", fontSize = 11.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = sibilOutlinedTextFieldColors(),
        modifier = Modifier.weight(1f).testTag("cw_stat_x_input")
      )
      if (statModel != StatRegModel.ONE_VAR) {
        OutlinedTextField(
          value = yInput,
          onValueChange = onYInputChange,
          label = { Text("Y", fontSize = 11.sp) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier.weight(1f).testTag("cw_stat_y_input")
        )
      }
      Button(
        onClick = {
          val x = xInput.trim().toDoubleOrNull() ?: return@Button
          val y = if (statModel == StatRegModel.ONE_VAR) 0.0 else (yInput.trim().toDoubleOrNull() ?: return@Button)
          rows.add(x to y)
          onXInputChange("")
          onYInputChange("")
        },
        colors = ButtonDefaults.buttonColors(containerColor = accentCyan),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = Modifier.testTag("cw_stat_add_btn")
      ) {
        Icon(Icons.Filled.Add, "Add Data Row", tint = Color.White, modifier = Modifier.size(18.dp))
      }
      if (rows.isNotEmpty()) {
        IconButton(onClick = { rows.clear() }) {
          Icon(Icons.Filled.Delete, "Clear Data", tint = palette.accentRed)
        }
      }
    }

    if (rows.isNotEmpty()) {
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        rows.forEachIndexed { idx, (x, y) ->
          RecessedGlassPill(
            isDarkMode = palette.isDark,
            onClick = { rows.removeAt(idx) }
          ) {
            Text(
              text = if (statModel == StatRegModel.ONE_VAR) "${idx + 1}: ${formatDecimalCompact(x)}"
              else "${idx + 1}: (${formatDecimalCompact(x)}, ${formatDecimalCompact(y)}) ×",
              fontFamily = JetBrainsMonoFamily,
              fontSize = 11.sp,
              color = palette.textPrimary
            )
          }
        }
      }
    }

    Surface(
      shape = RoundedCornerShape(12.dp),
      color = palette.nestedCardFill,
      border = palette.nestedBorder,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "${statModel.formula}  (n = ${summary.n})",
          fontFamily = JetBrainsMonoFamily,
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = accentCyan
        )
        if (summary.error != null) {
          Text(summary.error, fontSize = 12.sp, color = palette.textSecondary)
        } else {
          Text(
            text = "x̄ = ${formatDecimalCompact(summary.meanX)}  •  σx = ${formatDecimalCompact(summary.popStdX)}  •  sx = ${formatDecimalCompact(summary.sampStdX)}",
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.5.sp,
            color = palette.textPrimary
          )
          Text(
            text = "Σx = ${formatDecimalCompact(summary.sumX)}  •  Σx² = ${formatDecimalCompact(summary.sumX2)}  •  Med = ${formatDecimalCompact(summary.median)}",
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            color = palette.textSecondary
          )
          if (statModel != StatRegModel.ONE_VAR && summary.regA != null && summary.regB != null) {
            val cPart = summary.regC?.let { " • c=${formatDecimalCompact(it)}" } ?: ""
            Text(
              text = "a = ${formatDecimalCompact(summary.regA)} • b = ${formatDecimalCompact(summary.regB)}$cPart • r = ${formatDecimalCompact(summary.regR ?: 0.0)}",
              fontFamily = JetBrainsMonoFamily,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF10B981)
            )
            // Live y-hat predictor from x
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
              OutlinedTextField(
                value = predX,
                onValueChange = onPredXChange,
                label = { Text("Predict ŷ at x=", fontSize = 10.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = sibilOutlinedTextFieldColors(),
                modifier = Modifier.weight(1f)
              )
              val xVal = predX.toDoubleOrNull()
              val yHat = xVal?.let { xv ->
                when (statModel) {
                  StatRegModel.LINEAR -> summary.regA + summary.regB * xv
                  StatRegModel.QUADRATIC -> summary.regA + summary.regB * xv + (summary.regC ?: 0.0) * xv * xv
                  StatRegModel.LOGARITHMIC -> if (xv > 0) summary.regA + summary.regB * ln(xv) else Double.NaN
                  StatRegModel.EXPONENTIAL -> summary.regA * exp(summary.regB * xv)
                  StatRegModel.POWER -> if (xv > 0) summary.regA * xv.pow(summary.regB) else Double.NaN
                  StatRegModel.INVERSE_X -> if (abs(xv) > 1e-13) summary.regA + summary.regB / xv else Double.NaN
                  else -> Double.NaN
                }
              }
              Text(
                text = "ŷ = ${yHat?.let { formatDecimalCompact(it) } ?: "—"}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentCyan
              )
            }
          }
        }
      }
    }
  }
}

@Composable
internal fun ClassWizEqnWorkspace(
  subMode: EqnSubMode,
  onSelectSubMode: (EqnSubMode) -> Unit,
  simulCoeffs: MutableList<String>,
  polyCoeffs: MutableList<String>,
  isExactMode: Boolean,
  accentCyan: Color
) {
  val palette = currentGlassPalette()

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      EqnSubMode.entries.forEach { m ->
        val sel = subMode == m
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = if (sel) accentCyan.copy(alpha = 0.24f) else null,
          borderColor = if (sel) accentCyan else null,
          onClick = { onSelectSubMode(m) }
        ) {
          Text(m.label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (sel) accentCyan else palette.textPrimary)
        }
      }
    }

    if (!subMode.isPoly) {
      val n = subMode.size
      val varNames = listOf("x", "y", "z", "w")
      for (r in 0 until n) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          for (c in 0..n) {
            val idx = r * 5 + c
            val lbl = if (c < n) "a${r + 1}${c + 1}${varNames[c]}" else "c${r + 1}"
            OutlinedTextField(
              value = simulCoeffs[idx],
              onValueChange = { simulCoeffs[idx] = it },
              label = { Text(lbl, fontSize = 9.5.sp) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              colors = sibilOutlinedTextFieldColors(),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      val mat = Array(n) { r -> DoubleArray(n) { c -> simulCoeffs[r * 5 + c].toDoubleOrNull() ?: 0.0 } }
      val rhs = DoubleArray(n) { r -> simulCoeffs[r * 5 + n].toDoubleOrNull() ?: 0.0 }
      val outcome = remember(simulCoeffs.toList(), n) { solveSimultaneousWithStatus(mat, rhs) }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = palette.nestedCardFill,
        border = palette.nestedBorder,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
          when (outcome.status) {
            "NONE" -> Text("No Solution (Inconsistent System)", fontWeight = FontWeight.Bold, color = palette.accentRed)
            "INFINITE" -> Text("Infinite Solutions (Dependent System)", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
            else -> outcome.solutions.forEachIndexed { idx, sol ->
              val txt = (if (isExactMode) tryFormatExactSymbolic(sol) else null) ?: formatDecimalCompact(sol)
              Text(
                text = "${varNames[idx]} = $txt",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentCyan
              )
            }
          }
        }
      }
    } else {
      val deg = subMode.size
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0..deg) {
          val power = deg - i
          val lbl = if (power == 0) "c" else if (power == 1) "x" else "x^$power"
          OutlinedTextField(
            value = polyCoeffs[i],
            onValueChange = { polyCoeffs[i] = it },
            label = { Text(lbl, fontSize = 10.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = sibilOutlinedTextFieldColors(),
            modifier = Modifier.weight(1f)
          )
        }
      }
      val coeffsDesc = (0..deg).map { polyCoeffs[it].toDoubleOrNull() ?: 0.0 }
      val (roots, notes) = remember(coeffsDesc) { solvePolynomialRoots(coeffsDesc) }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = palette.nestedCardFill,
        border = palette.nestedBorder,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
          roots.forEachIndexed { idx, root ->
            val rTxt = formatComplexDisplay(root, false, ClassWizAngleUnit.DEG, isExactMode, ClassWizNumberFormat.NORM, 4)
            Text(
              text = "x${idx + 1} = $rTxt",
              fontFamily = JetBrainsMonoFamily,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = accentCyan
            )
          }
          notes.forEach { n ->
            Text(n, fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color(0xFF10B981))
          }
        }
      }
    }
  }
}

@Composable
internal fun ClassWizMatrixWorkspace(
  matrices: MutableMap<String, ClassWizMatrixData>,
  activeKey: String,
  onSelectKey: (String) -> Unit,
  lastMatrixResult: ClassWizMatrixData?,
  isExactMode: Boolean,
  onQuickOp: (String) -> Unit,
  accentCyan: Color
) {
  val palette = currentGlassPalette()
  val mat = matrices[activeKey] ?: ClassWizMatrixData()

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
      listOf("MatA", "MatB", "MatC", "MatD").forEach { key ->
        val sel = activeKey == key
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = if (sel) accentCyan.copy(alpha = 0.24f) else null,
          borderColor = if (sel) accentCyan else null,
          onClick = { onSelectKey(key) }
        ) {
          Text(key, fontFamily = JetBrainsMonoFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (sel) accentCyan else palette.textPrimary)
        }
      }
      Spacer(Modifier.weight(1f))
      listOf(2 to 2, 3 to 3, 4 to 4).forEach { (r, c) ->
        val sel = mat.rows == r && mat.cols == c
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = if (sel) accentCyan.copy(alpha = 0.20f) else null,
          onClick = {
            matrices[activeKey] = ClassWizMatrixData(r, c, List(r * c) { idx ->
              if (idx / c == idx % c) 1.0 else 0.0
            })
          }
        ) {
          Text("${r}×${c}", fontFamily = JetBrainsMonoFamily, fontSize = 10.5.sp, color = palette.textPrimary)
        }
      }
    }

    for (r in 0 until mat.rows) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (c in 0 until mat.cols) {
          val idx = r * mat.cols + c
          var cellDraft by remember(activeKey, mat.rows, mat.cols, idx) {
            mutableStateOf(formatDecimalCompact(mat.get(r, c)))
          }
          OutlinedTextField(
            value = cellDraft,
            onValueChange = { txt ->
              cellDraft = txt
              txt.toDoubleOrNull()?.let { v ->
                val next = mat.cells.toMutableList()
                next[idx] = v
                matrices[activeKey] = mat.copy(cells = next)
              }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = sibilOutlinedTextFieldColors(),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      RecessedGlassPill(isDarkMode = palette.isDark, onClick = { onQuickOp("Det($activeKey)") }) {
        Text("Det($activeKey)", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = accentCyan)
      }
      RecessedGlassPill(isDarkMode = palette.isDark, onClick = { onQuickOp("${activeKey}⁻¹") }) {
        Text("${activeKey}⁻¹", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = accentCyan)
      }
      RecessedGlassPill(isDarkMode = palette.isDark, onClick = { onQuickOp("Trn($activeKey)") }) {
        Text("Trn($activeKey)", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = accentCyan)
      }
      RecessedGlassPill(isDarkMode = palette.isDark, onClick = { onQuickOp("MatA×MatB") }) {
        Text("MatA×MatB", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = accentCyan)
      }
    }

    if (lastMatrixResult != null) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = palette.nestedCardFill,
        border = palette.nestedBorder,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          for (r in 0 until lastMatrixResult.rows) {
            val rowStr = (0 until lastMatrixResult.cols).joinToString("   ") { c ->
              val v = lastMatrixResult.get(r, c)
              (if (isExactMode) tryFormatExactSymbolic(v) else null) ?: formatDecimalCompact(v)
            }
            Text("[ $rowStr ]", fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentCyan)
          }
        }
      }
    }
  }
}

@Composable
internal fun ClassWizVectorWorkspace(
  vectors: MutableMap<String, ClassWizVectorData>,
  activeKey: String,
  onSelectKey: (String) -> Unit,
  lastVectorResult: ClassWizVectorData?,
  angleUnit: ClassWizAngleUnit,
  onQuickOp: (String) -> Unit,
  accentCyan: Color
) {
  val palette = currentGlassPalette()
  val vct = vectors[activeKey] ?: ClassWizVectorData()

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
      listOf("VctA", "VctB", "VctC").forEach { key ->
        val sel = activeKey == key
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = if (sel) accentCyan.copy(alpha = 0.24f) else null,
          borderColor = if (sel) accentCyan else null,
          onClick = { onSelectKey(key) }
        ) {
          Text(key, fontFamily = JetBrainsMonoFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (sel) accentCyan else palette.textPrimary)
        }
      }
      Spacer(Modifier.weight(1f))
      listOf(2, 3).forEach { d ->
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = if (vct.dim == d) accentCyan.copy(alpha = 0.20f) else null,
          onClick = {
            vectors[activeKey] = ClassWizVectorData(d, List(d) { i -> vct.get(i) })
          }
        ) {
          Text("${d}D", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = palette.textPrimary)
        }
      }
    }

    val compNames = listOf("i (x)", "j (y)", "k (z)")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      for (i in 0 until vct.dim) {
        var draft by remember(activeKey, vct.dim, i) { mutableStateOf(formatDecimalCompact(vct.get(i))) }
        OutlinedTextField(
          value = draft,
          onValueChange = { txt ->
            draft = txt
            txt.toDoubleOrNull()?.let { v ->
              val next = vct.components.toMutableList()
              next[i] = v
              vectors[activeKey] = vct.copy(components = next)
            }
          },
          label = { Text(compNames[i], fontSize = 10.sp) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          colors = sibilOutlinedTextFieldColors(),
          modifier = Modifier.weight(1f)
        )
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      RecessedGlassPill(isDarkMode = palette.isDark, onClick = { onQuickOp("VctA•VctB") }) {
        Text("A•B (Dot)", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = accentCyan)
      }
      RecessedGlassPill(isDarkMode = palette.isDark, onClick = { onQuickOp("VctA×VctB") }) {
        Text("A×B (Cross)", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = accentCyan)
      }
      RecessedGlassPill(isDarkMode = palette.isDark, onClick = { onQuickOp("abs($activeKey)") }) {
        Text("|$activeKey|", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = accentCyan)
      }
      RecessedGlassPill(isDarkMode = palette.isDark, onClick = { onQuickOp("UnitV($activeKey)") }) {
        Text("Unit($activeKey)", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = accentCyan)
      }
      RecessedGlassPill(isDarkMode = palette.isDark, onClick = { onQuickOp("Angle(VctA,VctB)") }) {
        Text("∠(${angleUnit.badge})", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = accentCyan)
      }
    }

    if (lastVectorResult != null) {
      Text(
        text = "Result = [ " + (0 until lastVectorResult.dim).joinToString(", ") {
          formatDecimalCompact(lastVectorResult.get(it))
        } + " ]",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = accentCyan
      )
    }
  }
}

@Composable
internal fun ClassWizTableWorkspace(
  fx: String,
  onFxChange: (String) -> Unit,
  gx: String,
  onGxChange: (String) -> Unit,
  startText: String,
  onStartChange: (String) -> Unit,
  endText: String,
  onEndChange: (String) -> Unit,
  stepText: String,
  onStepChange: (String) -> Unit,
  angleUnit: ClassWizAngleUnit,
  vars: Map<String, Double>,
  accentCyan: Color
) {
  val palette = currentGlassPalette()

  val tableRows = remember(fx, gx, startText, endText, stepText, angleUnit, vars.toMap()) {
    val s = startText.toDoubleOrNull() ?: 0.0
    val e = endText.toDoubleOrNull() ?: 5.0
    val st = (stepText.toDoubleOrNull() ?: 1.0).let { if (it <= 1e-9) 1.0 else it }
    val out = mutableListOf<Triple<Double, String, String>>()
    var x = s
    var count = 0
    while (x <= e + 1e-9 && count < 50) {
      val fStr = if (fx.isBlank()) "—" else runCatching {
        formatDecimalCompact(evaluateScalar(fx, vars + ("X" to x), angleUnit))
      }.getOrElse { "ERR" }
      val gStr = if (gx.isBlank()) "" else runCatching {
        formatDecimalCompact(evaluateScalar(gx, vars + ("X" to x), angleUnit))
      }.getOrElse { "ERR" }
      out.add(Triple(cleanZero(x), fStr, gStr))
      x += st
      count++
    }
    out
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      OutlinedTextField(
        value = fx,
        onValueChange = onFxChange,
        label = { Text("f(X)", fontSize = 11.sp) },
        singleLine = true,
        colors = sibilOutlinedTextFieldColors(),
        modifier = Modifier.weight(1f)
      )
      OutlinedTextField(
        value = gx,
        onValueChange = onGxChange,
        label = { Text("g(X) optional", fontSize = 11.sp) },
        singleLine = true,
        colors = sibilOutlinedTextFieldColors(),
        modifier = Modifier.weight(1f)
      )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      OutlinedTextField(
        value = startText,
        onValueChange = onStartChange,
        label = { Text("Start", fontSize = 10.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = sibilOutlinedTextFieldColors(),
        modifier = Modifier.weight(1f)
      )
      OutlinedTextField(
        value = endText,
        onValueChange = onEndChange,
        label = { Text("End", fontSize = 10.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = sibilOutlinedTextFieldColors(),
        modifier = Modifier.weight(1f)
      )
      OutlinedTextField(
        value = stepText,
        onValueChange = onStepChange,
        label = { Text("Step", fontSize = 10.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = sibilOutlinedTextFieldColors(),
        modifier = Modifier.weight(1f)
      )
    }

    Surface(
      shape = RoundedCornerShape(12.dp),
      color = palette.nestedCardFill,
      border = palette.nestedBorder,
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 180.dp)
    ) {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
          Text("X", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = accentCyan, modifier = Modifier.weight(0.8f))
          Text("f(X)", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = accentCyan, modifier = Modifier.weight(1.1f))
          if (gx.isNotBlank()) {
            Text("g(X)", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = accentCyan, modifier = Modifier.weight(1.1f))
          }
        }
        tableRows.forEach { (xVal, fVal, gVal) ->
          Row(modifier = Modifier.fillMaxWidth()) {
            Text(formatDecimalCompact(xVal), fontFamily = JetBrainsMonoFamily, fontSize = 11.5.sp, color = palette.textPrimary, modifier = Modifier.weight(0.8f))
            Text(fVal, fontFamily = JetBrainsMonoFamily, fontSize = 11.5.sp, color = palette.textPrimary, modifier = Modifier.weight(1.1f))
            if (gx.isNotBlank()) {
              Text(gVal, fontFamily = JetBrainsMonoFamily, fontSize = 11.5.sp, color = palette.textSecondary, modifier = Modifier.weight(1.1f))
            }
          }
        }
      }
    }
  }
}

// ============================================================================
// MATRIX & VECTOR EXPRESSION EVALUATORS
// ============================================================================

internal fun evaluateMatrixExpression(
  raw: String,
  matrices: Map<String, ClassWizMatrixData>,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit
): ClassWizMatrixData {
  val expr = raw.replace(" ", "").replace("*", "×")
  fun evalAtom(tok: String): ClassWizMatrixData {
    return when {
      tok.startsWith("Det(") && tok.endsWith(")") -> {
        val inner = evalAtom(tok.removePrefix("Det(").removeSuffix(")"))
        ClassWizMatrixData(1, 1, listOf(matrixDet(inner)))
      }
      tok.startsWith("Trn(") && tok.endsWith(")") -> {
        val inner = evalAtom(tok.removePrefix("Trn(").removeSuffix(")"))
        matrixTranspose(inner)
      }
      tok.endsWith("⁻¹") -> {
        val inner = evalAtom(tok.removeSuffix("⁻¹"))
        matrixInverse(inner)
      }
      tok in matrices -> matrices[tok]!!
      else -> throw IllegalArgumentException("Syntax ERROR: Matrix '$tok'")
    }
  }
  return when {
    expr.contains("+") -> {
      val parts = expr.split("+")
      parts.drop(1).fold(evalAtom(parts[0])) { acc, p -> matrixAddSub(acc, evalAtom(p), 1.0) }
    }
    expr.contains("-") && !expr.startsWith("-") -> {
      val parts = expr.split("-")
      parts.drop(1).fold(evalAtom(parts[0])) { acc, p -> matrixAddSub(acc, evalAtom(p), -1.0) }
    }
    expr.contains("×") -> {
      val parts = expr.split("×")
      var acc: ClassWizMatrixData? = null
      var scalarPending = 1.0
      for (p in parts) {
        val sVal = runCatching { evaluateScalar(p, vars, angleUnit) }.getOrNull()
        if (sVal != null) {
          if (acc == null) scalarPending *= sVal
          else acc = acc.copy(cells = acc.cells.map { cleanZero(it * sVal) })
        } else {
          val m = evalAtom(p)
          acc = if (acc == null) {
            if (scalarPending != 1.0) m.copy(cells = m.cells.map { cleanZero(it * scalarPending) }) else m
          } else {
            matrixMultiply(acc, m)
          }
          scalarPending = 1.0
        }
      }
      acc ?: throw IllegalArgumentException("Syntax ERROR")
    }
    else -> evalAtom(expr)
  }
}

internal fun evaluateVectorExpression(
  raw: String,
  vectors: Map<String, ClassWizVectorData>,
  vars: Map<String, Double>,
  angleUnit: ClassWizAngleUnit
): Pair<ClassWizVectorData?, Double?> {
  val expr = raw.replace(" ", "").replace("*", "×")
  if (expr.startsWith("abs(") && expr.endsWith(")")) {
    val inner = evaluateVectorExpression(expr.removePrefix("abs(").removeSuffix(")"), vectors, vars, angleUnit)
    return null to vectorNorm(inner.first ?: throw IllegalArgumentException("Vector expected"))
  }
  if (expr.startsWith("UnitV(") && expr.endsWith(")")) {
    val inner = evaluateVectorExpression(expr.removePrefix("UnitV(").removeSuffix(")"), vectors, vars, angleUnit)
    return vectorUnit(inner.first ?: throw IllegalArgumentException("Vector expected")) to null
  }
  if (expr.startsWith("Angle(") && expr.endsWith(")")) {
    val args = expr.removePrefix("Angle(").removeSuffix(")").split(",")
    if (args.size != 2) throw IllegalArgumentException("Angle(VctA,VctB)")
    val v1 = vectors[args[0]] ?: throw IllegalArgumentException("Unknown ${args[0]}")
    val v2 = vectors[args[1]] ?: throw IllegalArgumentException("Unknown ${args[1]}")
    return null to vectorAngle(v1, v2, angleUnit)
  }
  if (expr.contains("•")) {
    val parts = expr.split("•")
    if (parts.size != 2) throw IllegalArgumentException("Dot Syntax")
    val v1 = vectors[parts[0]] ?: throw IllegalArgumentException("Unknown ${parts[0]}")
    val v2 = vectors[parts[1]] ?: throw IllegalArgumentException("Unknown ${parts[1]}")
    return null to vectorDot(v1, v2)
  }
  if (expr.contains("×")) {
    val parts = expr.split("×")
    if (parts.size == 2 && parts[0] in vectors && parts[1] in vectors) {
      return vectorCross(vectors[parts[0]]!!, vectors[parts[1]]!!) to null
    }
    if (parts.size == 2) {
      val s0 = runCatching { evaluateScalar(parts[0], vars, angleUnit) }.getOrNull()
      val s1 = runCatching { evaluateScalar(parts[1], vars, angleUnit) }.getOrNull()
      if (s0 != null && parts[1] in vectors) {
        val v = vectors[parts[1]]!!
        return v.copy(components = v.components.map { cleanZero(it * s0) }) to null
      }
      if (s1 != null && parts[0] in vectors) {
        val v = vectors[parts[0]]!!
        return v.copy(components = v.components.map { cleanZero(it * s1) }) to null
      }
    }
  }
  if (expr.contains("+")) {
    val parts = expr.split("+")
    val base = vectors[parts[0]] ?: throw IllegalArgumentException("Unknown ${parts[0]}")
    val out = parts.drop(1).fold(base) { acc, p ->
      val other = vectors[p] ?: throw IllegalArgumentException("Unknown $p")
      if (acc.dim != other.dim) throw ArithmeticException("Dimension ERROR")
      ClassWizVectorData(acc.dim, (0 until acc.dim).map { cleanZero(acc.get(it) + other.get(it)) })
    }
    return out to null
  }
  if (expr.contains("-") && !expr.startsWith("-")) {
    val parts = expr.split("-")
    val base = vectors[parts[0]] ?: throw IllegalArgumentException("Unknown ${parts[0]}")
    val out = parts.drop(1).fold(base) { acc, p ->
      val other = vectors[p] ?: throw IllegalArgumentException("Unknown $p")
      if (acc.dim != other.dim) throw ArithmeticException("Dimension ERROR")
      ClassWizVectorData(acc.dim, (0 until acc.dim).map { cleanZero(acc.get(it) - other.get(it)) })
    }
    return out to null
  }
  val single = vectors[expr] ?: throw IllegalArgumentException("Syntax ERROR: Vector '$expr'")
  return single to null
}

package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.JetBrainsMonoFamily
import java.util.Locale

// ============================================================================
// CASIO fx-570ES PLUS: IN-LCD STATES, 40 CONSTANTS, 40 CONVERSIONS & BASE-N
// ============================================================================

internal enum class EsLcdScreenState {
  CALC_LINE,
  MODE_MENU,
  STAT_TYPE_MENU,
  STAT_EDITOR,
  STAT_SHIFT_MENU,
  STAT_SUM_MENU,
  STAT_VAR_MENU,
  STAT_REG_MENU,
  STAT_PREDICT_PROMPT,
  STAT_RESULTS,
  EQN_TYPE_MENU,
  EQN_COEFF_GRID,
  EQN_SOLUTIONS,
  MATRIX_SELECT_MENU,
  MATRIX_DIM_MENU,
  MATRIX_EDITOR,
  MATRIX_SHIFT_MENU,
  VECTOR_SELECT_MENU,
  VECTOR_DIM_MENU,
  VECTOR_EDITOR,
  VECTOR_SHIFT_MENU,
  TABLE_PROMPT_FX,
  TABLE_PROMPT_GX,
  TABLE_PROMPT_START,
  TABLE_PROMPT_END,
  TABLE_PROMPT_STEP,
  TABLE_VIEW,
  CONST_PROMPT,
  CONV_PROMPT,
  CLR_MENU
}

internal data class StatTableRow(
  val xText: String = "",
  val yText: String = ""
)

internal data class TableEvalRow(
  val x: Double,
  val fxText: String,
  val gxText: String? = null
)

internal enum class EqnPresetType(
  val code: Int,
  val menuLabel: String,
  val isPoly: Boolean,
  val size: Int,
  val headers: List<String>
) {
  SIMUL_2(1, "anX + bnY = cn", false, 2, listOf("a", "b", "c")),
  SIMUL_3(2, "anX + bnY + cnZ = dn", false, 3, listOf("a", "b", "c", "d")),
  QUAD_2(3, "aX² + bX + c = 0", true, 2, listOf("a", "b", "c")),
  CUBIC_3(4, "aX³ + bX² + cX + d = 0", true, 3, listOf("a", "b", "c", "d"))
}

internal enum class BaseNRadix(val label: String, val shortBadge: String, val radix: Int) {
  DEC("DEC", "Dec", 10),
  HEX("HEX", "Hex", 16),
  BIN("BIN", "Bin", 2),
  OCT("OCT", "Oct", 8)
}

internal data class EsConstantItem(
  val code: String,
  val symbol: String,
  val name: String,
  val unit: String,
  val value: Double
)

internal data class EsConversionItem(
  val code: String,
  val label: String,
  val fromUnit: String,
  val toUnit: String,
  val convert: (Double) -> Double
)

internal data class ClassWizHistoryEntry(
  val expression: String,
  val exactOrDisplay: String,
  val rawScalar: Double? = null
)

internal val ES_PLUS_CONSTANTS: List<EsConstantItem> = listOf(
  EsConstantItem("01", "mp", "Proton mass", "kg", 1.672621777e-27),
  EsConstantItem("02", "mn", "Neutron mass", "kg", 1.674927351e-27),
  EsConstantItem("03", "me", "Electron mass", "kg", 9.10938291e-31),
  EsConstantItem("04", "mμ", "Muon mass", "kg", 1.883531475e-28),
  EsConstantItem("05", "a₀", "Bohr radius", "m", 5.2917721092e-11),
  EsConstantItem("06", "h", "Planck constant", "J·s", 6.62606957e-34),
  EsConstantItem("07", "μN", "Nuclear magneton", "J/T", 5.05078353e-27),
  EsConstantItem("08", "μB", "Bohr magneton", "J/T", 9.27400968e-24),
  EsConstantItem("09", "ħ", "Reduced Planck constant", "J·s", 1.054571726e-34),
  EsConstantItem("10", "α", "Fine-structure constant", "", 7.2973525698e-3),
  EsConstantItem("11", "re", "Classical electron radius", "m", 2.8179403267e-15),
  EsConstantItem("12", "λc", "Compton wavelength", "m", 2.4263102389e-12),
  EsConstantItem("13", "γp", "Proton gyromagnetic ratio", "1/(s·T)", 2.675222005e8),
  EsConstantItem("14", "λcp", "Proton Compton wavelength", "m", 1.32140985623e-15),
  EsConstantItem("15", "λcn", "Neutron Compton wavelength", "m", 1.3195909068e-15),
  EsConstantItem("16", "R∞", "Rydberg constant", "1/m", 10973731.568539),
  EsConstantItem("17", "u", "Atomic mass constant", "kg", 1.660538921e-27),
  EsConstantItem("18", "μp", "Proton magnetic moment", "J/T", 1.410606743e-26),
  EsConstantItem("19", "μe", "Electron magnetic moment", "J/T", -9.28476430e-24),
  EsConstantItem("20", "μn", "Neutron magnetic moment", "J/T", -9.6623647e-27),
  EsConstantItem("21", "μμ", "Muon magnetic moment", "J/T", -4.49044807e-26),
  EsConstantItem("22", "F", "Faraday constant", "C/mol", 96485.3365),
  EsConstantItem("23", "e", "Elementary charge", "C", 1.602176565e-19),
  EsConstantItem("24", "NA", "Avogadro constant", "1/mol", 6.02214129e23),
  EsConstantItem("25", "k", "Boltzmann constant", "J/K", 1.3806488e-23),
  EsConstantItem("26", "Vm", "Molar volume of ideal gas", "m³/mol", 0.022710953),
  EsConstantItem("27", "R", "Molar gas constant", "J/(mol·K)", 8.3144621),
  EsConstantItem("28", "c₀", "Speed of light in vacuum", "m/s", 299792458.0),
  EsConstantItem("29", "c₁", "First radiation constant", "W·m²", 3.74177153e-16),
  EsConstantItem("30", "c₂", "Second radiation constant", "m·K", 0.014387770),
  EsConstantItem("31", "σ", "Stefan-Boltzmann constant", "W/(m²·K⁴)", 5.670373e-8),
  EsConstantItem("32", "ε₀", "Electric constant", "F/m", 8.854187817e-12),
  EsConstantItem("33", "μ₀", "Magnetic constant", "N/A²", 1.2566370614e-6),
  EsConstantItem("34", "Φ₀", "Magnetic flux quantum", "Wb", 2.067833758e-15),
  EsConstantItem("35", "g", "Standard gravity", "m/s²", 9.80665),
  EsConstantItem("36", "G₀", "Conductance quantum", "S", 7.7480917346e-5),
  EsConstantItem("37", "Z₀", "Characteristic impedance", "Ω", 376.730313461),
  EsConstantItem("38", "t", "Celsius temperature (273.15 K)", "K", 273.15),
  EsConstantItem("39", "G", "Newtonian constant of gravitation", "m³/(kg·s²)", 6.67384e-11),
  EsConstantItem("40", "atm", "Standard atmosphere", "Pa", 101325.0)
)

internal val ES_PLUS_CONVERSIONS: List<EsConversionItem> = listOf(
  EsConversionItem("01", "in►cm", "in", "cm") { it * 2.54 },
  EsConversionItem("02", "cm►in", "cm", "in") { it / 2.54 },
  EsConversionItem("03", "ft►m", "ft", "m") { it * 0.3048 },
  EsConversionItem("04", "m►ft", "m", "ft") { it / 0.3048 },
  EsConversionItem("05", "yd►m", "yd", "m") { it * 0.9144 },
  EsConversionItem("06", "m►yd", "m", "yd") { it / 0.9144 },
  EsConversionItem("07", "mile►km", "mile", "km") { it * 1.609344 },
  EsConversionItem("08", "km►mile", "km", "mile") { it / 1.609344 },
  EsConversionItem("09", "n mile►m", "n mile", "m") { it * 1852.0 },
  EsConversionItem("10", "m►n mile", "m", "n mile") { it / 1852.0 },
  EsConversionItem("11", "acre►m²", "acre", "m²") { it * 4046.8564224 },
  EsConversionItem("12", "m²►acre", "m²", "acre") { it / 4046.8564224 },
  EsConversionItem("13", "gal(US)►L", "gal(US)", "L") { it * 3.785411784 },
  EsConversionItem("14", "L►gal(US)", "L", "gal(US)") { it / 3.785411784 },
  EsConversionItem("15", "gal(UK)►L", "gal(UK)", "L") { it * 4.54609 },
  EsConversionItem("16", "L►gal(UK)", "L", "gal(UK)") { it / 4.54609 },
  EsConversionItem("17", "pc►km", "pc", "km") { it * 3.08567758149e13 },
  EsConversionItem("18", "km►pc", "km", "pc") { it / 3.08567758149e13 },
  EsConversionItem("19", "km/h►m/s", "km/h", "m/s") { it / 3.6 },
  EsConversionItem("20", "m/s►km/h", "m/s", "km/h") { it * 3.6 },
  EsConversionItem("21", "oz►g", "oz", "g") { it * 28.349523125 },
  EsConversionItem("22", "g►oz", "g", "oz") { it / 28.349523125 },
  EsConversionItem("23", "lb►kg", "lb", "kg") { it * 0.45359237 },
  EsConversionItem("24", "kg►lb", "kg", "lb") { it / 0.45359237 },
  EsConversionItem("25", "atm►Pa", "atm", "Pa") { it * 101325.0 },
  EsConversionItem("26", "Pa►atm", "Pa", "atm") { it / 101325.0 },
  EsConversionItem("27", "mmHg►Pa", "mmHg", "Pa") { it * 133.322387415 },
  EsConversionItem("28", "Pa►mmHg", "Pa", "mmHg") { it / 133.322387415 },
  EsConversionItem("29", "hp►kW", "hp", "kW") { it * 0.74569987158 },
  EsConversionItem("30", "kW►hp", "kW", "hp") { it / 0.74569987158 },
  EsConversionItem("31", "kgf/cm²►Pa", "kgf/cm²", "Pa") { it * 98066.5 },
  EsConversionItem("32", "Pa►kgf/cm²", "Pa", "kgf/cm²") { it / 98066.5 },
  EsConversionItem("33", "kgf·m►J", "kgf·m", "J") { it * 9.80665 },
  EsConversionItem("34", "J►kgf·m", "J", "kgf·m") { it / 9.80665 },
  EsConversionItem("35", "lbf/in²►kPa", "psi", "kPa") { it * 6.89475729317 },
  EsConversionItem("36", "kPa►lbf/in²", "kPa", "psi") { it / 6.89475729317 },
  EsConversionItem("37", "°F►°C", "°F", "°C") { (it - 32.0) * 5.0 / 9.0 },
  EsConversionItem("38", "°C►°F", "°C", "°F") { it * 9.0 / 5.0 + 32.0 },
  EsConversionItem("39", "kPa►kgf/cm²", "kPa", "kgf/cm²") { it / 98.0665 },
  EsConversionItem("40", "kgf/cm²►kPa", "kgf/cm²", "kPa") { it * 98.0665 }
)

internal val MATRIX_DIM_PRESETS: List<Triple<Int, String, Pair<Int, Int>>> = listOf(
  Triple(1, "3×3", 3 to 3),
  Triple(2, "3×2", 3 to 2),
  Triple(3, "3×1", 3 to 1),
  Triple(4, "2×3", 2 to 3),
  Triple(5, "2×2", 2 to 2),
  Triple(6, "2×1", 2 to 1)
)

// ============================================================================
// BASE-N BITWISE ARITHMETIC ENGINE (32-BIT TWO'S COMPLEMENT)
// ============================================================================

internal object BaseNLogic {
  fun evaluateBaseN(expr: String, radix: BaseNRadix): Int {
    val clean = expr.trim()
      .replace("×", "*")
      .replace("÷", "/")
    if (clean.isEmpty()) return 0
    return Parser(clean, radix.radix).parse()
  }

  fun formatForRadix(value: Int, radix: BaseNRadix): String {
    return when (radix) {
      BaseNRadix.DEC -> value.toString()
      BaseNRadix.HEX -> Integer.toHexString(value).uppercase(Locale.US)
      BaseNRadix.BIN -> {
        val raw = Integer.toBinaryString(value)
        if (raw.length <= 16) raw else raw.takeLast(16)
      }
      BaseNRadix.OCT -> Integer.toOctalString(value)
    }
  }

  private class Parser(private val s: String, private val defaultRadix: Int) {
    private var pos = 0

    fun parse(): Int {
      val v = parseOr()
      skipSpaces()
      if (pos < s.length) throw IllegalArgumentException("Syntax ERROR")
      return v
    }

    private fun skipSpaces() {
      while (pos < s.length && s[pos].isWhitespace()) pos++
    }

    private fun peekWord(): String? {
      skipSpaces()
      if (pos >= s.length || !s[pos].isLetter()) return null
      var end = pos
      while (end < s.length && s[end].isLetter()) end++
      return s.substring(pos, end).lowercase(Locale.US)
    }

    private fun consumeWord(word: String): Boolean {
      val w = peekWord() ?: return false
      if (w == word) {
        pos += word.length
        return true
      }
      return false
    }

    private fun parseOr(): Int {
      var x = parseXor()
      while (true) {
        x = when {
          consumeWord("or") -> x or parseXor()
          consumeWord("xor") -> x xor parseXor()
          consumeWord("xnor") -> (x xor parseXor()).inv()
          else -> return x
        }
      }
    }

    private fun parseXor(): Int = parseAnd()

    private fun parseAnd(): Int {
      var x = parseAddSub()
      while (consumeWord("and")) {
        x = x and parseAddSub()
      }
      return x
    }

    private fun parseAddSub(): Int {
      var x = parseMulDiv()
      while (true) {
        skipSpaces()
        if (pos < s.length && s[pos] == '+') {
          pos++
          x += parseMulDiv()
        } else if (pos < s.length && s[pos] == '-') {
          pos++
          x -= parseMulDiv()
        } else return x
      }
    }

    private fun parseMulDiv(): Int {
      var x = parseUnary()
      while (true) {
        skipSpaces()
        if (pos < s.length && s[pos] == '*') {
          pos++
          x *= parseUnary()
        } else if (pos < s.length && s[pos] == '/') {
          pos++
          val d = parseUnary()
          if (d == 0) throw ArithmeticException("Math ERROR: Div 0")
          x /= d
        } else return x
      }
    }

    private fun parseUnary(): Int {
      skipSpaces()
      if (consumeWord("not")) return parseUnary().inv()
      if (consumeWord("neg")) return -parseUnary()
      if (pos < s.length && s[pos] == '-') {
        pos++
        return -parseUnary()
      }
      if (pos < s.length && s[pos] == '+') {
        pos++
        return parseUnary()
      }
      return parsePrimary()
    }

    private fun parsePrimary(): Int {
      skipSpaces()
      if (pos >= s.length) throw IllegalArgumentException("Syntax ERROR")
      if (s[pos] == '(') {
        pos++
        val inner = parseOr()
        skipSpaces()
        if (pos >= s.length || s[pos] != ')') throw IllegalArgumentException("Missing )")
        pos++
        return inner
      }
      if (consumeWord("not") || consumeWord("neg")) {
        pos -= 3
        return parseUnary()
      }
      var radix = defaultRadix
      if (s.startsWith("0x", pos, ignoreCase = true)) {
        pos += 2; radix = 16
      } else if (s.startsWith("0b", pos, ignoreCase = true)) {
        pos += 2; radix = 2
      } else if (s.startsWith("0o", pos, ignoreCase = true)) {
        pos += 2; radix = 8
      }
      val start = pos
      while (pos < s.length && isDigitInRadix(s[pos], radix)) {
        pos++
      }
      if (pos == start) throw IllegalArgumentException("Syntax ERROR")
      val tok = s.substring(start, pos)
      return java.lang.Long.parseLong(tok, radix).toInt()
    }

    private fun isDigitInRadix(c: Char, r: Int): Boolean {
      val up = c.uppercaseChar()
      return when (r) {
        2 -> up == '0' || up == '1'
        8 -> up in '0'..'7'
        10 -> up in '0'..'9'
        16 -> up in '0'..'9' || up in 'A'..'F'
        else -> false
      }
    }
  }
}

// ============================================================================
// STAT SUM / VAR / REG & REGRESSION PREDICTION ENGINE (ŷ, x̂, x̂₁, x̂₂)
// ============================================================================

internal data class StatMenuOption(
  val code: Int,
  val label: String,
  val token: String
)

internal object StatPredictionLogic {
  fun sumMenuOptions(model: StatRegModel): List<StatMenuOption> =
    if (model == StatRegModel.ONE_VAR) {
      listOf(
        StatMenuOption(1, "Σx²", "Σx²"),
        StatMenuOption(2, "Σx", "Σx")
      )
    } else {
      listOf(
        StatMenuOption(1, "Σx²", "Σx²"),
        StatMenuOption(2, "Σx", "Σx"),
        StatMenuOption(3, "Σy²", "Σy²"),
        StatMenuOption(4, "Σy", "Σy"),
        StatMenuOption(5, "Σxy", "Σxy")
      )
    }

  fun varMenuOptions(model: StatRegModel): List<StatMenuOption> =
    if (model == StatRegModel.ONE_VAR) {
      listOf(
        StatMenuOption(1, "n", "n"),
        StatMenuOption(2, "x̄", "x̄"),
        StatMenuOption(3, "σx", "σx"),
        StatMenuOption(4, "sx", "sx")
      )
    } else {
      listOf(
        StatMenuOption(1, "n", "n"),
        StatMenuOption(2, "x̄", "x̄"),
        StatMenuOption(3, "σx", "σx"),
        StatMenuOption(4, "sx", "sx"),
        StatMenuOption(5, "ȳ", "ȳ"),
        StatMenuOption(6, "σy", "σy"),
        StatMenuOption(7, "sy", "sy")
      )
    }

  fun regMenuOptions(model: StatRegModel): List<StatMenuOption> =
    when (model) {
      StatRegModel.ONE_VAR -> emptyList()
      StatRegModel.QUADRATIC -> listOf(
        StatMenuOption(1, "A", "A"),
        StatMenuOption(2, "B", "B"),
        StatMenuOption(3, "C", "C"),
        StatMenuOption(4, "x̂₁", "x̂₁"),
        StatMenuOption(5, "x̂₂", "x̂₂"),
        StatMenuOption(6, "ŷ", "ŷ")
      )
      else -> listOf(
        StatMenuOption(1, "A", "A"),
        StatMenuOption(2, "B", "B"),
        StatMenuOption(3, "r", "r"),
        StatMenuOption(4, "x̂", "x̂"),
        StatMenuOption(5, "ŷ", "ŷ")
      )
    }

  data class StatExactSnapshot(
    val summary: StatSummaryResult,
    val exactN: Double,
    val sumY: Double,
    val sumY2: Double,
    val sumXY: Double
  )

  private fun percentileSortedList(sorted: List<Double>, q: Double): Double {
    if (sorted.isEmpty()) return 0.0
    if (sorted.size == 1) return sorted[0]
    val idx = q * (sorted.size - 1)
    val lo = kotlin.math.floor(idx).toInt()
    val hi = kotlin.math.min(sorted.size - 1, lo + 1)
    val frac = idx - lo
    return sorted[lo] * (1.0 - frac) + sorted[hi] * frac
  }

  private fun solveNormalLinearSystem(matrixA: Array<DoubleArray>, vectorB: DoubleArray): List<Double>? {
    val n = vectorB.size
    val a = Array(n) { r -> DoubleArray(n + 1) { c -> if (c < n) matrixA[r][c] else vectorB[r] } }
    for (col in 0 until n) {
      var pivotRow = col
      for (r in col + 1 until n) {
        if (kotlin.math.abs(a[r][col]) > kotlin.math.abs(a[pivotRow][col])) pivotRow = r
      }
      val tmp = a[col]
      a[col] = a[pivotRow]
      a[pivotRow] = tmp
      if (kotlin.math.abs(a[col][col]) < 1e-13) return null
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

  fun computeStatFromRows(
    rows: List<StatTableRow>,
    model: StatRegModel
  ): StatExactSnapshot {
    if (model == StatRegModel.ONE_VAR) {
      val validPts = rows.mapNotNull { row ->
        val x = row.xText.trim().toDoubleOrNull() ?: return@mapNotNull null
        val freqStr = row.yText.trim()
        val f = if (freqStr.isEmpty()) 1.0 else (freqStr.toDoubleOrNull() ?: return@mapNotNull null)
        if (f <= 0.0 || x.isNaN() || x.isInfinite() || f.isNaN() || f.isInfinite()) null else (x to f)
      }
      if (validPts.isEmpty()) {
        return StatExactSnapshot(
          summary = StatSummaryResult(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, error = "Add at least 1 X value"),
          exactN = 0.0, sumY = 0.0, sumY2 = 0.0, sumXY = 0.0
        )
      }
      val nDouble = validPts.sumOf { it.second }
      val sumX = validPts.sumOf { it.first * it.second }
      val sumX2 = validPts.sumOf { it.first * it.first * it.second }
      val meanX = sumX / nDouble
      val ssX = validPts.sumOf { it.second * (it.first - meanX) * (it.first - meanX) }
      val popStdX = kotlin.math.sqrt(ssX / nDouble)
      val sampStdX = if (nDouble > 1.0) kotlin.math.sqrt(ssX / (nDouble - 1.0)) else 0.0
      val expandedX = buildList {
        for ((xv, fv) in validPts) {
          val rep = fv.toInt().coerceIn(1, 50)
          repeat(rep) { add(xv) }
        }
      }.sorted()
      return StatExactSnapshot(
        summary = StatSummaryResult(
          n = nDouble.toInt(),
          meanX = cleanZero(meanX),
          sumX = cleanZero(sumX),
          sumX2 = cleanZero(sumX2),
          popStdX = cleanZero(popStdX),
          sampStdX = cleanZero(sampStdX),
          minX = expandedX.first(),
          q1 = percentileSortedList(expandedX, 0.25),
          median = percentileSortedList(expandedX, 0.50),
          q3 = percentileSortedList(expandedX, 0.75),
          maxX = expandedX.last()
        ),
        exactN = cleanZero(nDouble),
        sumY = 0.0,
        sumY2 = 0.0,
        sumXY = 0.0
      )
    }

    // Strict valid-row filtering for all 2-variable regressions (2..8): BOTH X and Y must be valid Doubles
    val validPairs = rows.mapNotNull { row ->
      val x = row.xText.trim().toDoubleOrNull()
      val y = row.yText.trim().toDoubleOrNull()
      if (x != null && y != null && !x.isNaN() && !y.isNaN() && !x.isInfinite() && !y.isInfinite()) {
        x to y
      } else null
    }
    if (validPairs.isEmpty()) {
      return StatExactSnapshot(
        summary = StatSummaryResult(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, error = "Enter at least 2 (X,Y) pairs"),
        exactN = 0.0, sumY = 0.0, sumY2 = 0.0, sumXY = 0.0
      )
    }

    val n = validPairs.size
    val nd = n.toDouble()
    val xs = validPairs.map { it.first }
    val ys = validPairs.map { it.second }
    val sumX = xs.sum()
    val sumX2 = xs.sumOf { it * it }
    val meanX = sumX / nd
    val ssX = xs.sumOf { (it - meanX) * (it - meanX) }
    val popStdX = kotlin.math.sqrt(ssX / nd)
    val sampStdX = if (n > 1) kotlin.math.sqrt(ssX / (nd - 1.0)) else 0.0
    val sortedX = xs.sorted()
    val minX = sortedX.first()
    val maxX = sortedX.last()
    val q1 = percentileSortedList(sortedX, 0.25)
    val med = percentileSortedList(sortedX, 0.50)
    val q3 = percentileSortedList(sortedX, 0.75)

    val sumY = ys.sum()
    val sumY2 = ys.sumOf { it * it }
    val sumXY = validPairs.sumOf { it.first * it.second }
    val meanY = sumY / nd
    val ssY = ys.sumOf { (it - meanY) * (it - meanY) }
    val popStdY = kotlin.math.sqrt(ssY / nd)
    val sampStdY = if (n > 1) kotlin.math.sqrt(ssY / (nd - 1.0)) else 0.0

    fun wrap(err: String? = null, regA: Double? = null, regB: Double? = null, regC: Double? = null, regR: Double? = null) =
      StatExactSnapshot(
        summary = StatSummaryResult(
          n = n,
          meanX = cleanZero(meanX),
          sumX = cleanZero(sumX),
          sumX2 = cleanZero(sumX2),
          popStdX = cleanZero(popStdX),
          sampStdX = cleanZero(sampStdX),
          minX = minX,
          q1 = q1,
          median = med,
          q3 = q3,
          maxX = maxX,
          meanY = cleanZero(meanY),
          popStdY = cleanZero(popStdY),
          sampStdY = cleanZero(sampStdY),
          regA = regA?.let { cleanZero(it) },
          regB = regB?.let { cleanZero(it) },
          regC = regC?.let { cleanZero(it) },
          regR = regR?.let { cleanZero(it) },
          error = err
        ),
        exactN = nd,
        sumY = cleanZero(sumY),
        sumY2 = cleanZero(sumY2),
        sumXY = cleanZero(sumXY)
      )

    if (n < 2) return wrap(err = "Need ≥ 2 (X,Y) pairs")

    // Exact Casio fx-570ES PLUS transformed least-squares formulas
    fun fitLeastSquares(tx: List<Double>, ty: List<Double>): Triple<Double, Double, Double>? {
      val sx = tx.sum()
      val sy = ty.sum()
      val sxx = tx.sumOf { it * it }
      val syy = ty.sumOf { it * it }
      val sxy = tx.indices.sumOf { tx[it] * ty[it] }
      val denomX = nd * sxx - sx * sx
      if (kotlin.math.abs(denomX) < 1e-14) return null
      val num = nd * sxy - sx * sy
      val bRaw = num / denomX
      val aRaw = (sy - bRaw * sx) / nd
      val denomY = nd * syy - sy * sy
      val r = if (denomY > 1e-14) num / kotlin.math.sqrt(denomX * denomY) else 1.0
      return Triple(aRaw, bRaw, r)
    }

    return when (model) {
      StatRegModel.LINEAR -> {
        val fit = fitLeastSquares(xs, ys) ?: return wrap(err = "Zero X variance")
        wrap(regA = fit.first, regB = fit.second, regR = fit.third)
      }
      StatRegModel.QUADRATIC -> {
        if (n < 3) return wrap(err = "Need ≥ 3 points for Quad")
        val sx = sumX
        val sx2 = sumX2
        val sx3 = xs.sumOf { it * it * it }
        val sx4 = xs.sumOf { it * it * it * it }
        val sy = sumY
        val sxy = sumXY
        val sx2y = validPairs.sumOf { it.first * it.first * it.second }
        val sol = solveNormalLinearSystem(
          arrayOf(
            doubleArrayOf(nd, sx, sx2),
            doubleArrayOf(sx, sx2, sx3),
            doubleArrayOf(sx2, sx3, sx4)
          ),
          doubleArrayOf(sy, sxy, sx2y)
        ) ?: return wrap(err = "Singular data matrix")
        val (a, b, c) = sol
        val ssTot = ys.sumOf { (it - meanY) * (it - meanY) }
        val ssRes = validPairs.sumOf { (xv, yv) ->
          val pred = a + b * xv + c * xv * xv
          (yv - pred) * (yv - pred)
        }
        val r2 = if (ssTot > 1e-14) kotlin.math.max(0.0, 1.0 - ssRes / ssTot) else 1.0
        wrap(regA = a, regB = b, regC = c, regR = kotlin.math.sqrt(r2))
      }
      StatRegModel.LOGARITHMIC -> {
        if (xs.any { it <= 0.0 }) return wrap(err = "ln(x) requires x > 0")
        val fit = fitLeastSquares(xs.map { kotlin.math.ln(it) }, ys) ?: return wrap(err = "Zero X variance")
        wrap(regA = fit.first, regB = fit.second, regR = fit.third)
      }
      StatRegModel.EXPONENTIAL -> {
        if (ys.any { it <= 0.0 }) return wrap(err = "e^X requires y > 0")
        val fit = fitLeastSquares(xs, ys.map { kotlin.math.ln(it) }) ?: return wrap(err = "Zero X variance")
        wrap(regA = kotlin.math.exp(fit.first), regB = fit.second, regR = fit.third)
      }
      StatRegModel.AB_EXP -> {
        if (ys.any { it <= 0.0 }) return wrap(err = "A·B^X requires y > 0")
        val fit = fitLeastSquares(xs, ys.map { kotlin.math.ln(it) }) ?: return wrap(err = "Zero X variance")
        wrap(regA = kotlin.math.exp(fit.first), regB = kotlin.math.exp(fit.second), regR = fit.third)
      }
      StatRegModel.POWER -> {
        if (xs.any { it <= 0.0 } || ys.any { it <= 0.0 }) return wrap(err = "A·X^B requires x,y > 0")
        val fit = fitLeastSquares(xs.map { kotlin.math.ln(it) }, ys.map { kotlin.math.ln(it) })
          ?: return wrap(err = "Zero X variance")
        wrap(regA = kotlin.math.exp(fit.first), regB = fit.second, regR = fit.third)
      }
      StatRegModel.INVERSE_X -> {
        if (xs.any { kotlin.math.abs(it) < 1e-13 }) return wrap(err = "1/X requires x ≠ 0")
        val fit = fitLeastSquares(xs.map { 1.0 / it }, ys) ?: return wrap(err = "Zero X variance")
        wrap(regA = fit.first, regB = fit.second, regR = fit.third)
      }
      StatRegModel.ONE_VAR -> wrap()
    }
  }

  fun predict(
    rows: List<StatTableRow>,
    model: StatRegModel,
    opToken: String,
    inputVal: Double
  ): Double {
    if (model == StatRegModel.ONE_VAR) {
      throw IllegalArgumentException("1-VAR has no Regression")
    }
    val snap = computeStatFromRows(rows, model)
    val summary = snap.summary
    if (summary.error != null) throw IllegalArgumentException(summary.error)
    val a = summary.regA ?: throw ArithmeticException("Math ERROR: No A")
    val b = summary.regB ?: throw ArithmeticException("Math ERROR: No B")
    val c = summary.regC ?: 0.0

    val result = if (opToken == "ŷ") {
      when (model) {
        StatRegModel.LINEAR -> a + b * inputVal
        StatRegModel.QUADRATIC -> a + b * inputVal + c * inputVal * inputVal
        StatRegModel.LOGARITHMIC -> {
          if (inputVal <= 0.0) throw ArithmeticException("Math ERROR: ln(x≤0)")
          a + b * kotlin.math.ln(inputVal)
        }
        StatRegModel.EXPONENTIAL -> a * kotlin.math.exp(b * inputVal)
        StatRegModel.AB_EXP -> a * Math.pow(b, inputVal)
        StatRegModel.POWER -> {
          if (inputVal < 0.0 || (inputVal == 0.0 && b <= 0.0)) {
            throw ArithmeticException("Math ERROR: Domain")
          }
          a * Math.pow(inputVal, b)
        }
        StatRegModel.INVERSE_X -> {
          if (kotlin.math.abs(inputVal) < 1e-14) throw ArithmeticException("Math ERROR: x = 0")
          a + b / inputVal
        }
        StatRegModel.ONE_VAR -> throw IllegalArgumentException("1-VAR has no Regression")
      }
    } else {
      // Inverse prediction: x̂, x̂₁, x̂₂ from Y = inputVal
      when (model) {
        StatRegModel.LINEAR -> {
          if (kotlin.math.abs(b) < 1e-14) throw ArithmeticException("Math ERROR: B = 0")
          (inputVal - a) / b
        }
        StatRegModel.QUADRATIC -> {
          if (kotlin.math.abs(c) < 1e-13) {
            if (kotlin.math.abs(b) < 1e-13) throw ArithmeticException("Math ERROR: C,B = 0")
            (inputVal - a) / b
          } else {
            val rawDisc = b * b - 4.0 * c * (a - inputVal)
            if (rawDisc < -1e-11) throw ArithmeticException("Math ERROR: No real x̂")
            val sqrtDisc = kotlin.math.sqrt(if (rawDisc < 0.0) 0.0 else rawDisc)
            if (opToken == "x̂₂") (-b - sqrtDisc) / (2.0 * c)
            else (-b + sqrtDisc) / (2.0 * c)
          }
        }
        StatRegModel.LOGARITHMIC -> {
          if (kotlin.math.abs(b) < 1e-14) throw ArithmeticException("Math ERROR: B = 0")
          kotlin.math.exp((inputVal - a) / b)
        }
        StatRegModel.EXPONENTIAL -> {
          if (kotlin.math.abs(a) < 1e-14 || kotlin.math.abs(b) < 1e-14 || inputVal / a <= 0.0) {
            throw ArithmeticException("Math ERROR: Domain")
          }
          kotlin.math.ln(inputVal / a) / b
        }
        StatRegModel.AB_EXP -> {
          if (kotlin.math.abs(a) < 1e-14 || b <= 0.0 || inputVal / a <= 0.0) {
            throw ArithmeticException("Math ERROR: Domain")
          }
          val lnB = kotlin.math.ln(b)
          if (kotlin.math.abs(lnB) < 1e-14) throw ArithmeticException("Math ERROR: B = 1")
          kotlin.math.ln(inputVal / a) / lnB
        }
        StatRegModel.POWER -> {
          if (kotlin.math.abs(a) < 1e-14 || kotlin.math.abs(b) < 1e-14) {
            throw ArithmeticException("Math ERROR: Domain")
          }
          val ratio = inputVal / a
          if (ratio < 0.0 || (ratio == 0.0 && 1.0 / b <= 0.0)) {
            throw ArithmeticException("Math ERROR: Domain")
          }
          Math.pow(ratio, 1.0 / b)
        }
        StatRegModel.INVERSE_X -> {
          val denom = inputVal - a
          if (kotlin.math.abs(denom) < 1e-14) throw ArithmeticException("Math ERROR: y = A")
          b / denom
        }
        StatRegModel.ONE_VAR -> throw IllegalArgumentException("1-VAR has no Regression")
      }
    }
    if (result.isNaN() || result.isInfinite()) throw ArithmeticException("Math ERROR: Overflow")
    return cleanZero(result)
  }

  private val postfixPredTokens = listOf("x̂₁", "x̂₂", "x̂", "ŷ")
  private val multiCharStatTokens = listOf(
    "Σx²", "Σy²", "Σxy", "Σx", "Σy", "x̄", "ȳ", "σx", "σy", "sx", "sy"
  )

  fun isStandaloneStatToken(text: String): Boolean {
    val t = text.trim()
    return t in multiCharStatTokens || t in listOf("n", "A", "B", "C", "r")
  }

  fun evaluateStatExpression(
    rawExpr: String,
    rows: List<StatTableRow>,
    model: StatRegModel,
    vars: Map<String, Double>,
    angleUnit: ClassWizAngleUnit
  ): Double {
    var work = rawExpr.trim()
    if (work.isEmpty()) throw IllegalArgumentException("Empty expression")

    // 1. Resolve all postfix prediction operators (x̂₁, x̂₂, x̂, ŷ) left-to-right
    while (true) {
      var bestIdx = -1
      var bestTok = ""
      for (tok in postfixPredTokens) {
        val idx = work.indexOf(tok)
        if (idx >= 0 && (bestIdx < 0 || idx < bestIdx || (idx == bestIdx && tok.length > bestTok.length))) {
          bestIdx = idx
          bestTok = tok
        }
      }
      if (bestIdx < 0) break

      val argStart = findPostfixOperandStart(work, bestIdx)
      val argExpr = work.substring(argStart, bestIdx).trim()
      if (argExpr.isEmpty()) throw IllegalArgumentException("Syntax ERROR near $bestTok")
      val argVal = evaluateStatExpression(argExpr, rows, model, vars, angleUnit)
      val predVal = predict(rows, model, bestTok, argVal)
      val rep = "(${predVal})"
      work = work.substring(0, argStart) + rep + work.substring(bestIdx + bestTok.length)
    }

    // 2. Replace stat tokens (Sum / Var / Reg symbols) with exact values over strict valid rows
    val snap = computeStatFromRows(rows, model)
    val summary = snap.summary

    fun statValueFor(sym: String): Double {
      if (snap.exactN <= 0.0) throw IllegalArgumentException("No valid STAT data")
      if (summary.error != null && sym in listOf("A", "B", "C", "r")) {
        throw IllegalArgumentException(summary.error)
      }
      return when (sym) {
        "Σx²" -> summary.sumX2
        "Σx" -> summary.sumX
        "Σy²" -> snap.sumY2
        "Σy" -> snap.sumY
        "Σxy" -> snap.sumXY
        "n" -> snap.exactN
        "x̄" -> summary.meanX
        "σx" -> summary.popStdX
        "sx" -> summary.sampStdX
        "ȳ" -> summary.meanY ?: throw IllegalArgumentException("1-VAR has no ȳ")
        "σy" -> summary.popStdY ?: throw IllegalArgumentException("1-VAR has no σy")
        "sy" -> summary.sampStdY ?: throw IllegalArgumentException("1-VAR has no sy")
        "A" -> summary.regA ?: throw IllegalArgumentException("1-VAR has no A")
        "B" -> summary.regB ?: throw IllegalArgumentException("1-VAR has no B")
        "C" -> summary.regC ?: throw IllegalArgumentException("No C for ${model.label}")
        "r" -> summary.regR ?: throw IllegalArgumentException("1-VAR has no r")
        else -> throw IllegalArgumentException("Unknown stat symbol $sym")
      }
    }

    val singleTokens = if (model != StatRegModel.ONE_VAR) {
      listOf("n", "r", "A", "B", "C")
    } else {
      listOf("n")
    }
    val allOrdered = multiCharStatTokens + singleTokens

    val sb = StringBuilder(work.length + 16)
    var i = 0
    while (i < work.length) {
      var matchedTok: String? = null
      for (tok in allOrdered) {
        if (work.startsWith(tok, i) && isStatTokenBoundary(work, i, tok)) {
          matchedTok = tok
          break
        }
      }
      if (matchedTok != null) {
        sb.append("(").append(statValueFor(matchedTok)).append(")")
        i += matchedTok.length
      } else {
        sb.append(work[i])
        i++
      }
    }

    return evaluateScalar(sb.toString(), vars, angleUnit)
  }

  private fun isAsciiLetter(c: Char): Boolean = c in 'a'..'z' || c in 'A'..'Z'

  private fun isStatTokenBoundary(s: String, idx: Int, tok: String): Boolean {
    val end = idx + tok.length
    val prev = if (idx > 0) s[idx - 1] else null
    val next = if (end < s.length) s[end] else null
    if (tok.length > 1) {
      if (prev != null && isAsciiLetter(prev)) return false
      if (next != null && (isAsciiLetter(next) || next == '\u0304' || next == '\u0302')) return false
      return true
    }
    if (prev != null && (isAsciiLetter(prev) || prev == '_')) return false
    if (next != null && (isAsciiLetter(next) || next == '_' || next == '\u0304' || next == '\u0302' || next == '(')) {
      return false
    }
    if (tok == "C" && prev != null && (prev.isDigit() || prev == ')') && next != null && next.isDigit()) {
      return false
    }
    return true
  }

  private fun findPostfixOperandStart(s: String, opIdx: Int): Int {
    var i = opIdx - 1
    while (i >= 0 && s[i].isWhitespace()) i--
    if (i < 0) return 0

    if (s[i] == ')') {
      var depth = 1
      i--
      while (i >= 0 && depth > 0) {
        if (s[i] == ')') depth++
        else if (s[i] == '(') depth--
        i--
      }
      while (i >= 0 && (isAsciiLetter(s[i]) || s[i] == '_')) i--
      return i + 1
    }

    val endPos = i + 1
    for (tok in multiCharStatTokens) {
      if (endPos >= tok.length && s.substring(endPos - tok.length, endPos) == tok) {
        return endPos - tok.length
      }
    }

    var k = i
    if (k >= 0 && isAsciiLetter(s[k])) {
      while (k >= 0 && (isAsciiLetter(s[k]) || s[k] == '_')) k--
      return k + 1
    }
    while (k >= 0) {
      val c = s[k]
      if (c.isDigit() || c == '.') {
        k--
      } else if ((c == '+' || c == '-') && k > 0 && (s[k - 1] == 'E' || s[k - 1] == 'e') &&
        k >= 2 && s[k - 2].isDigit()
      ) {
        k -= 2
      } else break
    }
    if (k >= 0 && s[k] == '-') {
      var p = k - 1
      while (p >= 0 && s[p].isWhitespace()) p--
      if (p < 0 || s[p] in charArrayOf('(', '+', '-', '*', '×', '/', '÷', '^', ',', ':')) {
        k--
      }
    }
    return (k + 1).coerceAtMost(opIdx)
  }
}

// ============================================================================
// IN-LCD INTERACTIVE WORKSPACE RENDERERS (CASIO fx-570ES PLUS)
// ============================================================================

private val LcdDarkInk = Color(0xFF0F172A)
private val LcdMutedInk = Color(0xFF334155)
private val LcdHighlightBg = Color(0xFF0F172A)
private val LcdHighlightFg = Color(0xFFE8F0E6)
private val LcdGridBorder = Color(0xFF475569).copy(alpha = 0.55f)

@Composable
internal fun InLcdTwoColumnMenu(
  title: String,
  items: List<Pair<String, String>>,
  onSelectIndex: (Int) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = title,
      fontFamily = JetBrainsMonoFamily,
      fontSize = 10.sp,
      fontWeight = FontWeight.ExtraBold,
      color = LcdMutedInk,
      maxLines = 1
    )
    Column(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      verticalArrangement = Arrangement.SpaceEvenly
    ) {
      items.chunked(2).forEachIndexed { rowIdx, pair ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          pair.forEachIndexed { colIdx, (code, label) ->
            val idx = rowIdx * 2 + colIdx
            Row(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onSelectIndex(idx) }
                .padding(horizontal = 4.dp, vertical = 1.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              val menuItemStyle = TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 13.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
              )
              Text(
                text = "$code:",
                style = menuItemStyle,
                color = LcdDarkInk
              )
              Spacer(Modifier.width(3.dp))
              Text(
                text = label,
                style = menuItemStyle.copy(fontWeight = FontWeight.Bold),
                color = LcdDarkInk,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
          if (pair.size == 1) Spacer(Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
internal fun rememberLcdBlinkOn(): Boolean {
  var on by remember { mutableStateOf(true) }
  LaunchedEffect(Unit) {
    while (true) {
      kotlinx.coroutines.delay(520L)
      on = !on
    }
  }
  return on
}

@Composable
internal fun InLcdStatSpreadsheet(
  model: StatRegModel,
  rows: List<StatTableRow>,
  cursorRow: Int,
  cursorCol: Int,
  cellBuffer: String,
  onSelectCell: (Int, Int) -> Unit
) {
  val isOneVar = model == StatRegModel.ONE_VAR
  val blinkOn = rememberLcdBlinkOn()
  val cursorGlyph = if (blinkOn) "│" else " "
  val totalDisplayRows = maxOf(rows.size, 4)
  val startRow = (cursorRow - 2).coerceAtLeast(0).coerceAtMost((totalDisplayRows - 4).coerceAtLeast(0))
  val visibleIndices = (startRow until minOf(startRow + 4, totalDisplayRows)).toList()

  val cellTextStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontSize = 10.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 11.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false)
  )

  Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
    // Table Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFFCBD5E1).copy(alpha = 0.65f))
        .border(0.8.dp, LcdGridBorder)
        .padding(vertical = 1.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("ROW", style = cellTextStyle.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold), color = LcdDarkInk, textAlign = TextAlign.Center, modifier = Modifier.width(30.dp))
      Text("X", style = cellTextStyle.copy(fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold), color = LcdDarkInk, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
      Text(if (isOneVar) "FREQ" else "Y", style = cellTextStyle.copy(fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold), color = LcdDarkInk, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
    }

    // 4 Visible Data Rows (Sliding Window with Auto-Scroll)
    Column(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.SpaceEvenly) {
      visibleIndices.forEach { rIdx ->
        val row = rows.getOrNull(rIdx) ?: StatTableRow()
        val selX = cursorRow == rIdx && cursorCol == 0
        val selY = cursorRow == rIdx && cursorCol == 1
        val xDisp = when {
          selX && cellBuffer.isNotEmpty() -> "$cellBuffer$cursorGlyph"
          selX && row.xText.isNotEmpty() -> "${row.xText}$cursorGlyph"
          selX -> cursorGlyph
          else -> row.xText
        }
        val yDisp = when {
          selY && cellBuffer.isNotEmpty() -> "$cellBuffer$cursorGlyph"
          selY && row.yText.isNotEmpty() -> "${row.yText}$cursorGlyph"
          selY -> cursorGlyph
          row.yText.isNotEmpty() -> row.yText
          isOneVar && row.xText.isNotBlank() -> "1"
          else -> ""
        }

        Row(
          modifier = Modifier.fillMaxWidth().border(0.5.dp, LcdGridBorder),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${rIdx + 1}",
            style = cellTextStyle.copy(fontSize = 9.5.sp),
            color = LcdMutedInk,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(30.dp)
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .background(if (selX) LcdHighlightBg else Color.Transparent)
              .clickable { onSelectCell(rIdx, 0) }
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = xDisp,
              style = cellTextStyle,
              color = if (selX) LcdHighlightFg else LcdDarkInk,
              textAlign = TextAlign.End,
              maxLines = 1,
              modifier = Modifier.fillMaxWidth()
            )
          }
          Box(
            modifier = Modifier
              .weight(1f)
              .background(if (selY) LcdHighlightBg else Color.Transparent)
              .clickable { onSelectCell(rIdx, 1) }
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = yDisp,
              style = cellTextStyle,
              color = if (selY) LcdHighlightFg else LcdDarkInk,
              textAlign = TextAlign.End,
              maxLines = 1,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }

    // Bottom active cell input bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val curRowObj = rows.getOrNull(cursorRow) ?: StatTableRow()
      val activeText = if (cellBuffer.isNotEmpty()) cellBuffer else (if (cursorCol == 0) curRowObj.xText else curRowObj.yText)
      Text(
        text = "${model.label} [R${cursorRow + 1},${if (cursorCol == 0) "X" else if (isOneVar) "FREQ" else "Y"}] = $activeText$cursorGlyph",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LcdDarkInk,
        maxLines = 1
      )
      Text(
        text = "AC:Calc  SHIFT+1:Stat",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = LcdMutedInk,
        maxLines = 1
      )
    }
  }
}

@Composable
internal fun InLcdEqnCoeffGrid(
  preset: EqnPresetType,
  coeffs: List<String>,
  cursorRow: Int,
  cursorCol: Int,
  cellBuffer: String,
  onSelectCell: (Int, Int) -> Unit
) {
  val rowsCount = if (preset.isPoly) 1 else preset.size
  val colsCount = preset.headers.size
  val blinkOn = rememberLcdBlinkOn()
  val cursorGlyph = if (blinkOn) "│" else " "

  Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
    // Header row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFFCBD5E1).copy(alpha = 0.65f))
        .border(0.8.dp, LcdGridBorder)
        .padding(vertical = 1.5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(if (preset.isPoly) "P(x)" else "EQ", fontFamily = JetBrainsMonoFamily, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, color = LcdDarkInk, textAlign = TextAlign.Center, modifier = Modifier.width(32.dp))
      preset.headers.forEach { h ->
        Text(h, fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = LcdDarkInk, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
      }
    }

    Column(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.SpaceEvenly) {
      for (r in 0 until rowsCount) {
        Row(
          modifier = Modifier.fillMaxWidth().border(0.5.dp, LcdGridBorder),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${r + 1}",
            fontFamily = JetBrainsMonoFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = LcdMutedInk,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(32.dp)
          )
          for (c in 0 until colsCount) {
            val flatIdx = if (preset.isPoly) c else r * 5 + c
            val isSel = cursorRow == r && cursorCol == c
            val stored = coeffs.getOrElse(flatIdx) { "" }
            val txt = when {
              isSel && cellBuffer.isNotEmpty() -> "$cellBuffer$cursorGlyph"
              isSel && stored.isNotEmpty() -> "$stored$cursorGlyph"
              isSel -> cursorGlyph
              else -> stored
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .background(if (isSel) LcdHighlightBg else Color.Transparent)
                .clickable { onSelectCell(r, c) }
                .padding(horizontal = 3.dp, vertical = 1.5.dp)
            ) {
              Text(
                text = txt,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSel) LcdHighlightFg else LcdDarkInk,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val hName = preset.headers.getOrElse(cursorCol) { "a" }
      val flatSel = if (preset.isPoly) cursorCol else cursorRow * 5 + cursorCol
      val activeVal = if (cellBuffer.isNotEmpty()) cellBuffer else coeffs.getOrElse(flatSel) { "" }
      Text(
        text = "${preset.menuLabel} • ${hName}${if (preset.isPoly) "" else "${cursorRow + 1}"} = $activeVal$cursorGlyph",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LcdDarkInk,
        maxLines = 1
      )
      Text(
        text = "Press = to commit/solve",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = LcdMutedInk,
        maxLines = 1
      )
    }
  }
}

@Composable
internal fun InLcdMatrixVectorGrid(
  title: String,
  rows: Int,
  cols: Int,
  cellTexts: List<String>,
  cursorRow: Int,
  cursorCol: Int,
  cellBuffer: String,
  hintRight: String,
  onSelectCell: (Int, Int) -> Unit
) {
  val blinkOn = rememberLcdBlinkOn()
  val cursorGlyph = if (blinkOn) "│" else " "

  Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "$title (${rows}×${cols})",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LcdDarkInk
      )
      Text(
        text = hintRight,
        fontFamily = JetBrainsMonoFamily,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = LcdMutedInk
      )
    }

    Column(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      verticalArrangement = Arrangement.SpaceEvenly
    ) {
      for (r in 0 until rows) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text("[", fontFamily = JetBrainsMonoFamily, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = LcdDarkInk)
          for (c in 0 until cols) {
            val isSel = cursorRow == r && cursorCol == c
            val stored = cellTexts.getOrElse(r * cols + c) { "" }
            val txt = when {
              isSel && cellBuffer.isNotEmpty() -> "$cellBuffer$cursorGlyph"
              isSel && stored.isNotEmpty() -> "$stored$cursorGlyph"
              isSel -> cursorGlyph
              else -> stored
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(3.dp))
                .background(if (isSel) LcdHighlightBg else Color(0xFFCBD5E1).copy(alpha = 0.45f))
                .clickable { onSelectCell(r, c) }
                .padding(horizontal = 4.dp, vertical = 1.5.dp)
            ) {
              Text(
                text = txt,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSel) LcdHighlightFg else LcdDarkInk,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
          Text("]", fontFamily = JetBrainsMonoFamily, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = LcdDarkInk)
        }
      }
    }

    val curStored = cellTexts.getOrElse(cursorRow * cols + cursorCol) { "" }
    val activeVal = if (cellBuffer.isNotEmpty()) cellBuffer else curStored
    Text(
      text = "$title[${cursorRow + 1},${cursorCol + 1}] = $activeVal$cursorGlyph",
      fontFamily = JetBrainsMonoFamily,
      fontSize = 9.5.sp,
      fontWeight = FontWeight.ExtraBold,
      color = LcdDarkInk,
      maxLines = 1
    )
  }
}

@Composable
internal fun InLcdCodeDirectory(
  title: String,
  codeBuffer: String,
  entries: List<Pair<String, String>>,
  pageIndex: Int,
  onSelectCode: (String) -> Unit
) {
  val pages = entries.chunked(6)
  val safePage = pageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
  val slice = pages.getOrElse(safePage) { emptyList() }

  Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "$title [${if (codeBuffer.isEmpty()) "__" else codeBuffer}]",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LcdDarkInk
      )
      Text(
        text = "Page ${safePage + 1}/${pages.size} (▲▼ / 01–40=)",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = LcdMutedInk
      )
    }

    Column(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.SpaceEvenly) {
      slice.chunked(2).forEach { pair ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          pair.forEach { (code, label) ->
            Row(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onSelectCode(code) }
                .padding(horizontal = 3.dp, vertical = 1.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "$code:",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LcdDarkInk
              )
              Spacer(Modifier.width(3.dp))
              Text(
                text = label,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = LcdDarkInk,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
          if (pair.size == 1) Spacer(Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
internal fun InLcdStatPredictPrompt(
  model: StatRegModel,
  targetOp: String,
  inputBuffer: String
) {
  val isPredictY = targetOp == "ŷ"
  val promptLine = if (isPredictY) "Predict ŷ for X =" else "Predict $targetOp for Y ="
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = "STAT REG (${model.label} • ${model.formula})",
      fontFamily = JetBrainsMonoFamily,
      fontSize = 10.sp,
      fontWeight = FontWeight.ExtraBold,
      color = LcdMutedInk,
      maxLines = 1
    )
    Text(
      text = promptLine,
      fontFamily = JetBrainsMonoFamily,
      fontSize = 13.5.sp,
      fontWeight = FontWeight.ExtraBold,
      color = LcdDarkInk,
      maxLines = 1
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Text(
        text = "AC:Cancel  DEL:Back  =:Compute",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = LcdMutedInk,
        maxLines = 1
      )
      Text(
        text = "${inputBuffer}│",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 19.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LcdDarkInk,
        maxLines = 1
      )
    }
  }
}

// ============================================================================
// DIALOGS: HISTORY (50), CLR, DRG
// ============================================================================

@Composable
internal fun EsHistoryDialog(
  history: List<ClassWizHistoryEntry>,
  onDismiss: () -> Unit,
  onClearHistory: () -> Unit,
  onSelectEntry: (ClassWizHistoryEntry, Boolean) -> Unit,
  accentCyan: Color
) {
  val palette = currentGlassPalette()
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = palette.dialogSurface,
      border = palette.glassBorder,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .widthIn(max = 520.dp)
        .heightIn(max = 540.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.History, null, tint = accentCyan, modifier = Modifier.size(20.dp))
            Text("History (${history.size}/50)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
          }
          Row {
            if (history.isNotEmpty()) {
              IconButton(onClick = onClearHistory, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Delete, "Clear History", tint = palette.accentRed, modifier = Modifier.size(18.dp))
              }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Filled.Close, "Close", tint = palette.textSecondary)
            }
          }
        }
        if (history.isEmpty()) {
          Box(modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp), contentAlignment = Alignment.Center) {
            Text("No calculations yet. Tap any past formula or result here to reuse it.", fontSize = 12.5.sp, color = palette.textSecondary)
          }
        } else {
          LazyColumn(
            modifier = Modifier.weight(1f, fill = false).heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(history) { entry ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.nestedCardFill,
                border = palette.nestedBorder,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = entry.expression,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 13.sp,
                    color = palette.textPrimary,
                    modifier = Modifier.fillMaxWidth().clickable { onSelectEntry(entry, false) }
                  )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Tap formula or result to insert", fontSize = 10.sp, color = palette.textSecondary)
                    Text(
                      text = "= ${entry.exactOrDisplay}",
                      fontFamily = JetBrainsMonoFamily,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold,
                      color = accentCyan,
                      modifier = Modifier.clickable { onSelectEntry(entry, true) }
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
}

@Composable
internal fun EsClrDialog(
  onDismiss: () -> Unit,
  onClearSetup: () -> Unit,
  onClearMemory: () -> Unit,
  onClearAll: () -> Unit
) {
  val palette = currentGlassPalette()
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = palette.dialogSurface,
      border = palette.glassBorder,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("CLR • Clear Calculator", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
        Surface(
          onClick = onClearSetup,
          shape = RoundedCornerShape(12.dp),
          color = palette.nestedCardFill,
          border = palette.nestedBorder,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("1: Setup (Deg, Norm, COMP)", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary, modifier = Modifier.padding(12.dp))
        }
        Surface(
          onClick = onClearMemory,
          shape = RoundedCornerShape(12.dp),
          color = palette.nestedCardFill,
          border = palette.nestedBorder,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("2: Memory (Clear A–F, X, Y, M)", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary, modifier = Modifier.padding(12.dp))
        }
        Surface(
          onClick = onClearAll,
          shape = RoundedCornerShape(12.dp),
          color = palette.accentRed.copy(alpha = 0.14f),
          border = BorderStroke(1.dp, palette.accentRed.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("3: All (Reset Everything)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = palette.accentRed, modifier = Modifier.padding(12.dp))
        }
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
          Text("Cancel")
        }
      }
    }
  }
}

@Composable
internal fun EsDrgUnitDialog(
  onDismiss: () -> Unit,
  onInsertToken: (String) -> Unit,
  accentCyan: Color
) {
  val palette = currentGlassPalette()
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = palette.dialogSurface,
      border = palette.glassBorder,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("DRG► Angle Unit Suffix", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("1: ° (Deg)" to "°", "2: ʳ (Rad)" to "ʳ", "3: ᵍ (Grad)" to "ᵍ").forEach { (lbl, tok) ->
            Button(
              onClick = { onInsertToken(tok) },
              colors = ButtonDefaults.buttonColors(containerColor = accentCyan),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text(lbl, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
            }
          }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Close") }
      }
    }
  }
}

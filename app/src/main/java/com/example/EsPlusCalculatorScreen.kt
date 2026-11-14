package com.example

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.JetBrainsMonoFamily
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// ============================================================================
// CASIO fx-570ES PLUS FULL-SCREEN SCIENTIFIC CALCULATOR (IN-LCD WORKSPACES)
// ============================================================================

@Composable
internal fun ScientificCalculatorScreen(
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

  // Core Calculator State
  var currentMode by rememberSaveable { mutableStateOf(ClassWizMode.COMP) }
  var lcdState by rememberSaveable { mutableStateOf(EsLcdScreenState.CALC_LINE) }
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
  var baseRadix by rememberSaveable { mutableStateOf(BaseNRadix.DEC) }
  var lastBaseInt by rememberSaveable { mutableIntStateOf(0) }

  val rootMathContainer = remember { MathContainer() }
  var cursorLocation by remember { mutableStateOf(CursorLocation(rootMathContainer, 0)) }
  var answerMathTree by remember { mutableStateOf(mathContainerOfText("0")) }
  var displayResult by rememberSaveable { mutableStateOf("0") }
  var activeDisplayNumeric by rememberSaveable { mutableStateOf(0.0) }
  var activeDisplayComplex by remember { mutableStateOf(ComplexNum(0.0, 0.0)) }
  var activeComplexPolar by rememberSaveable { mutableStateOf(false) }
  var hasCustomTextAnswer by rememberSaveable { mutableStateOf(false) }
  var preserveAnswerOnNextCalcLine by rememberSaveable { mutableStateOf(false) }
  var isErrorActive by rememberSaveable { mutableStateOf(false) }
  var errorGotoTarget by remember { mutableStateOf<CursorLocation?>(null) }
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

  // Auxiliary Modals (SETUP, History, Variables, DRG, CALC variable prompt)
  var showSetupModal by remember { mutableStateOf(false) }
  var showVariablesModal by remember { mutableStateOf(false) }
  var showHistoryModal by remember { mutableStateOf(false) }
  var showDrgModal by remember { mutableStateOf(false) }
  var showCalcPrompt by remember { mutableStateOf(false) }
  var calcPromptVars by remember { mutableStateOf<List<String>>(emptyList()) }
  var calcPromptDrafts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

  // Shared In-LCD Cell / Prompt Buffers
  var cellEditBuffer by rememberSaveable { mutableStateOf("") }
  var gridCursorRow by rememberSaveable { mutableIntStateOf(0) }
  var gridCursorCol by rememberSaveable { mutableIntStateOf(0) }
  var codePromptBuffer by rememberSaveable { mutableStateOf("") }
  var codePromptPage by rememberSaveable { mutableIntStateOf(0) }

  // Mode 3: Statistics State (In-LCD, strictly blank until user enters data)
  var statModel by rememberSaveable { mutableStateOf(StatRegModel.LINEAR) }
  val statRows = remember {
    mutableStateListOf(StatTableRow(), StatTableRow(), StatTableRow(), StatTableRow())
  }
  var statResultsLines by remember { mutableStateOf<List<String>>(emptyList()) }
  var statResultIndex by rememberSaveable { mutableIntStateOf(0) }
  var statPredictTarget by rememberSaveable { mutableStateOf("ŷ") }
  var lastAutoStatExpr by rememberSaveable { mutableStateOf<String?>(null) }

  // Mode 5: Equation State (In-LCD, blank cells)
  var eqnPreset by rememberSaveable { mutableStateOf(EqnPresetType.SIMUL_2) }
  val eqnCoeffs = remember { mutableStateListOf(*Array(15) { "" }) }
  var eqnSolutions by remember { mutableStateOf<List<Pair<String, Double?>>>(emptyList()) }
  var eqnSolutionIdx by rememberSaveable { mutableIntStateOf(0) }

  // Mode 6: Matrix State (In-LCD, blank cells)
  val matrices = remember {
    mutableStateMapOf(
      "MatA" to ClassWizMatrixData(2, 2, listOf(0.0, 0.0, 0.0, 0.0)),
      "MatB" to ClassWizMatrixData(2, 2, listOf(0.0, 0.0, 0.0, 0.0)),
      "MatC" to ClassWizMatrixData(2, 2, listOf(0.0, 0.0, 0.0, 0.0))
    )
  }
  val matCellTexts = remember {
    mutableStateMapOf(
      "MatA" to List(4) { "" },
      "MatB" to List(4) { "" },
      "MatC" to List(4) { "" }
    )
  }
  var activeMatKey by rememberSaveable { mutableStateOf("MatA") }
  var matMenuForDim by rememberSaveable { mutableStateOf(true) }

  // Mode 8: Vector State (In-LCD, blank cells)
  val vectors = remember {
    mutableStateMapOf(
      "VctA" to ClassWizVectorData(3, listOf(0.0, 0.0, 0.0)),
      "VctB" to ClassWizVectorData(3, listOf(0.0, 0.0, 0.0)),
      "VctC" to ClassWizVectorData(3, listOf(0.0, 0.0, 0.0))
    )
  }
  val vctCellTexts = remember {
    mutableStateMapOf(
      "VctA" to List(3) { "" },
      "VctB" to List(3) { "" },
      "VctC" to List(3) { "" }
    )
  }
  var activeVctKey by rememberSaveable { mutableStateOf("VctA") }
  var vctMenuForDim by rememberSaveable { mutableStateOf(true) }

  // Mode 7: Table State (In-LCD, blank prompts)
  var tableFx by rememberSaveable { mutableStateOf("") }
  var tableGx by rememberSaveable { mutableStateOf("") }
  var tableFxTree by remember { mutableStateOf<MathContainer?>(null) }
  var tableGxTree by remember { mutableStateOf<MathContainer?>(null) }
  var tableStart by rememberSaveable { mutableStateOf("") }
  var tableEnd by rememberSaveable { mutableStateOf("") }
  var tableStep by rememberSaveable { mutableStateOf("") }
  var tableRows by remember { mutableStateOf<List<TableEvalRow>>(emptyList()) }
  var tableScrollIdx by rememberSaveable { mutableIntStateOf(0) }

  fun clearErrorState() {
    if (isErrorActive) {
      isErrorActive = false
      errorGotoTarget = null
      if (statusToast == "[◄][►] : Goto") statusToast = null
    }
  }

  fun resetExpressionTree() {
    clearErrorState()
    rootMathContainer.nodes.clear()
    cursorLocation = CursorLocation(rootMathContainer, 0)
  }

  fun loadTreeIntoRoot(source: MathContainer?) {
    clearErrorState()
    rootMathContainer.nodes.clear()
    if (source != null) {
      val cloned = cloneMathContainer(source)
      rootMathContainer.nodes.addAll(cloned.nodes)
    }
    cursorLocation = CursorLocation(rootMathContainer, rootMathContainer.nodes.size)
  }

  fun pushHistory(expr: String, disp: String, scalar: Double?, tree: MathContainer? = null) {
    history.add(0, ClassWizHistoryEntry(expr, disp, scalar, tree?.let { cloneMathContainer(it) }))
    if (history.size > 50) history.removeAt(history.lastIndex)
    historyIndex = -1
  }

  fun formatValueForS2D(valNum: Double): String {
    val v = cleanZero(valNum)
    return when {
      isDmsOutput -> formatDmsOutput(v)
      isExactMode && engShift == 0 && numberFormat == ClassWizNumberFormat.NORM -> {
        tryFormatExactSymbolic(v, isMixedFraction)
          ?: formatDecimalCompact(v, numberFormat, fixDigits, engShift)
      }
      else -> formatDecimalCompact(v, numberFormat, fixDigits, engShift)
    }
  }

  fun refreshCompDisplayFromNumeric(
    valNum: Double,
    prefix: String = "",
    isCommit: Boolean = true
  ) {
    val cleaned = cleanZero(valNum)
    activeDisplayNumeric = cleaned
    if (isCommit) lastNumericAns = cleaned
    val text = formatValueForS2D(cleaned)
    if (prefix.isNotEmpty()) {
      hasCustomTextAnswer = true
      displayResult = "$prefix$text"
      answerMathTree = mathContainerOfText(displayResult)
    } else {
      hasCustomTextAnswer = false
      displayResult = text
      answerMathTree = buildCompAnswerTree(
        value = cleaned,
        isExact = isExactMode,
        isMixedFraction = isMixedFraction,
        isDmsOutput = isDmsOutput,
        format = numberFormat,
        fixDigits = fixDigits,
        engShift = engShift
      )
    }
  }

  fun refreshComplexDisplay(
    z: ComplexNum,
    polar: Boolean = complexPolarOutput,
    isCommit: Boolean = true
  ) {
    activeDisplayComplex = z
    activeComplexPolar = polar
    if (isCommit) lastComplexAns = z
    hasCustomTextAnswer = false
    displayResult = formatComplexDisplay(
      z = z,
      polarMode = polar,
      angleUnit = angleUnit,
      exactMode = isExactMode,
      format = numberFormat,
      fixDigits = fixDigits
    )
    answerMathTree = buildComplexAnswerTree(
      z = z,
      polarMode = polar,
      angleUnit = angleUnit,
      exactMode = isExactMode,
      format = numberFormat,
      fixDigits = fixDigits
    )
  }

  fun evalComplexTreeAware(
    rawExpr: String,
    cVars: Map<String, ComplexNum>
  ): Pair<ComplexNum, Boolean> {
    var coreExpr = rawExpr.trim()
    var polarOverride: Boolean? = null
    while (true) {
      when {
        coreExpr.endsWith("▶r∠θ") -> {
          if (polarOverride == null) polarOverride = true
          coreExpr = coreExpr.removeSuffix("▶r∠θ").trim()
        }
        coreExpr.endsWith("▶a+bi") -> {
          if (polarOverride == null) polarOverride = false
          coreExpr = coreExpr.removeSuffix("▶a+bi").trim()
        }
        coreExpr.endsWith("►r∠θ") -> {
          if (polarOverride == null) polarOverride = true
          coreExpr = coreExpr.removeSuffix("►r∠θ").trim()
        }
        coreExpr.endsWith("►a+bi") -> {
          if (polarOverride == null) polarOverride = false
          coreExpr = coreExpr.removeSuffix("►a+bi").trim()
        }
        else -> break
      }
    }
    val effPolar = polarOverride ?: complexPolarOutput
    val z = if (coreExpr.isEmpty()) {
      cVars["Ans"] ?: lastComplexAns
    } else {
      evaluateComplex(coreExpr, cVars, angleUnit)
    }
    return z to effPolar
  }

  fun resetStatRowsBlank() {
    statRows.clear()
    repeat(4) { statRows.add(StatTableRow()) }
  }

  fun resetEqnCoeffsBlank() {
    for (i in eqnCoeffs.indices) eqnCoeffs[i] = ""
  }

  fun enterMode(mode: ClassWizMode) {
    currentMode = mode
    isShiftActive = false
    isAlphaActive = false
    cellEditBuffer = ""
    gridCursorRow = 0
    gridCursorCol = 0
    lastMatrixResult = null
    lastVectorResult = null
    statusToast = null
    hasCustomTextAnswer = false
    preserveAnswerOnNextCalcLine = false
    activeDisplayNumeric = 0.0
    isExactMode = (mode != ClassWizMode.STAT)
    when (mode) {
      ClassWizMode.COMP, ClassWizMode.CMPLX -> {
        lcdState = EsLcdScreenState.CALC_LINE
        resetExpressionTree()
        displayResult = "0"
        answerMathTree = mathContainerOfText("0")
      }
      ClassWizMode.BASE_N -> {
        lcdState = EsLcdScreenState.CALC_LINE
        resetExpressionTree()
        displayResult = BaseNLogic.formatForRadix(lastBaseInt, baseRadix)
        answerMathTree = mathContainerOfText(displayResult)
      }
      ClassWizMode.STAT -> {
        resetStatRowsBlank()
        statResultsLines = emptyList()
        lastAutoStatExpr = null
        resetExpressionTree()
        displayResult = "0"
        answerMathTree = mathContainerOfText("0")
        lcdState = EsLcdScreenState.STAT_TYPE_MENU
      }
      ClassWizMode.EQN -> {
        resetEqnCoeffsBlank()
        eqnSolutions = emptyList()
        lcdState = EsLcdScreenState.EQN_TYPE_MENU
      }
      ClassWizMode.MATRIX -> {
        matMenuForDim = true
        resetExpressionTree()
        displayResult = "0"
        answerMathTree = mathContainerOfText("0")
        lcdState = EsLcdScreenState.MATRIX_SELECT_MENU
      }
      ClassWizMode.TABLE -> {
        tableFx = ""
        tableGx = ""
        tableFxTree = null
        tableGxTree = null
        tableStart = ""
        tableEnd = ""
        tableStep = ""
        tableRows = emptyList()
        tableScrollIdx = 0
        resetExpressionTree()
        lcdState = EsLcdScreenState.TABLE_PROMPT_FX
      }
      ClassWizMode.VECTOR -> {
        vctMenuForDim = true
        resetExpressionTree()
        displayResult = "0"
        answerMathTree = mathContainerOfText("0")
        lcdState = EsLcdScreenState.VECTOR_SELECT_MENU
      }
    }
  }

  fun computeEqnSolutionsForPreset() {
    val sols = mutableListOf<Pair<String, Double?>>()
    if (!eqnPreset.isPoly) {
      val n = eqnPreset.size
      val mat = Array(n) { r -> DoubleArray(n) { c -> eqnCoeffs.getOrElse(r * 5 + c) { "0" }.toDoubleOrNull() ?: 0.0 } }
      val rhs = DoubleArray(n) { r -> eqnCoeffs.getOrElse(r * 5 + n) { "0" }.toDoubleOrNull() ?: 0.0 }
      val outcome = solveSimultaneousWithStatus(mat, rhs)
      val names = listOf("X", "Y", "Z")
      when (outcome.status) {
        "NONE" -> sols.add("No Solution" to null)
        "INFINITE" -> sols.add("Infinite Solutions" to null)
        else -> outcome.solutions.forEachIndexed { idx, v ->
          sols.add("${names.getOrElse(idx) { "X${idx + 1}" }} = ${formatValueForS2D(v)}" to v)
        }
      }
    } else {
      val deg = eqnPreset.size
      val coeffsDesc = (0..deg).map { eqnCoeffs.getOrElse(it) { "0" }.toDoubleOrNull() ?: 0.0 }
      val (roots, notes) = solvePolynomialRoots(coeffsDesc)
      roots.forEachIndexed { idx, root ->
        if (abs(root.im) < 1e-9) {
          sols.add("X${idx + 1} = ${formatValueForS2D(root.re)}" to root.re)
        } else {
          val cStr = formatComplexDisplay(root, false, angleUnit, isExactMode, numberFormat, fixDigits)
          sols.add("X${idx + 1} = $cStr" to null)
        }
      }
      notes.forEach { n -> sols.add(n to null) }
    }
    eqnSolutions = if (sols.isEmpty()) listOf("Math ERROR" to null) else sols
    eqnSolutionIdx = 0
    lcdState = EsLcdScreenState.EQN_SOLUTIONS
  }

  fun buildStatResultLines(category: Int) {
    // 3:Sum, 4:Var, 5:Reg, 6:MinMax
    val snap = StatPredictionLogic.computeStatFromRows(statRows, statModel)
    val summary = snap.summary
    val lines = mutableListOf<String>()
    if (summary.error != null) {
      lines.add(summary.error)
    } else {
      when (category) {
        3 -> {
          lines.add("Σx² = ${formatDecimalCompact(summary.sumX2)}")
          lines.add("Σx = ${formatDecimalCompact(summary.sumX)}")
          if (statModel != StatRegModel.ONE_VAR) {
            lines.add("Σy² = ${formatDecimalCompact(snap.sumY2)}")
            lines.add("Σy = ${formatDecimalCompact(snap.sumY)}")
            lines.add("Σxy = ${formatDecimalCompact(snap.sumXY)}")
          }
        }
        4 -> {
          lines.add("n = ${formatDecimalCompact(snap.exactN)}")
          lines.add("x̄ = ${formatDecimalCompact(summary.meanX)}")
          lines.add("σx = ${formatDecimalCompact(summary.popStdX)}")
          lines.add("sx = ${formatDecimalCompact(summary.sampStdX)}")
          if (statModel != StatRegModel.ONE_VAR && summary.meanY != null) {
            lines.add("ȳ = ${formatDecimalCompact(summary.meanY)}")
            lines.add("σy = ${formatDecimalCompact(summary.popStdY ?: 0.0)}")
            lines.add("sy = ${formatDecimalCompact(summary.sampStdY ?: 0.0)}")
          }
        }
        5 -> {
          if (statModel == StatRegModel.ONE_VAR) {
            lines.add("1-VAR has no Regression (use 4:Var)")
          } else {
            lines.add("a = ${formatDecimalCompact(summary.regA ?: 0.0)}")
            lines.add("b = ${formatDecimalCompact(summary.regB ?: 0.0)}")
            if (summary.regC != null) lines.add("c = ${formatDecimalCompact(summary.regC)}")
            if (summary.regR != null) lines.add("r = ${formatDecimalCompact(summary.regR)}")
          }
        }
        6 -> {
          lines.add("minX = ${formatDecimalCompact(summary.minX)}")
          lines.add("Q1 = ${formatDecimalCompact(summary.q1)}")
          lines.add("Med = ${formatDecimalCompact(summary.median)}")
          lines.add("Q3 = ${formatDecimalCompact(summary.q3)}")
          lines.add("maxX = ${formatDecimalCompact(summary.maxX)}")
        }
      }
    }
    statResultsLines = lines
    statResultIndex = 0
    lcdState = EsLcdScreenState.STAT_RESULTS
  }

  fun buildTableData() {
    val fxAst = tableFxTree
    if (tableFx.isBlank() && (fxAst == null || fxAst.nodes.isEmpty())) {
      statusToast = "Enter f(X) first"
      loadTreeIntoRoot(tableFxTree)
      lcdState = EsLcdScreenState.TABLE_PROMPT_FX
      return
    }
    val s = tableStart.toDoubleOrNull()
    val e = tableEnd.toDoubleOrNull()
    val st = tableStep.toDoubleOrNull()
    if (s == null || e == null || st == null) {
      statusToast = "Invalid Start/End/Step"
      return
    }
    if (st <= 1e-12 || e < s) {
      statusToast = if (st <= 1e-12) "Step must be > 0" else "End must be ≥ Start"
      return
    }
    val out = mutableListOf<TableEvalRow>()
    var x = s
    var count = 0
    val gxAst = tableGxTree
    while (x <= e + 1e-9 && count < 200) {
      val fVal = runCatching {
        val v = if (fxAst != null && fxAst.nodes.isNotEmpty()) {
          evaluateMathAst(fxAst, variables + ("X" to x), angleUnit)
        } else {
          evaluateScalar(tableFx, variables + ("X" to x), angleUnit)
        }
        formatDecimalCompact(v, numberFormat, fixDigits)
      }.getOrElse { "Error" }
      val gVal = if ((gxAst != null && gxAst.nodes.isNotEmpty()) || tableGx.isNotBlank()) {
        runCatching {
          val v = if (gxAst != null && gxAst.nodes.isNotEmpty()) {
            evaluateMathAst(gxAst, variables + ("X" to x), angleUnit)
          } else {
            evaluateScalar(tableGx, variables + ("X" to x), angleUnit)
          }
          formatDecimalCompact(v, numberFormat, fixDigits)
        }.getOrElse { "Error" }
      } else null
      out.add(TableEvalRow(x = cleanZero(x), fxText = fVal, gxText = gVal))
      x += st
      count++
    }
    tableRows = out
    tableScrollIdx = 0
    statusToast = "${out.size} rows generated"
    lcdState = EsLcdScreenState.TABLE_VIEW
  }

  fun handleStatSymbolChoice(token: String) {
    clearErrorState()
    lcdState = EsLcdScreenState.CALC_LINE
    val curCanonical = rootMathContainer.toCanonicalString()
    if (rootMathContainer.nodes.isEmpty() || curCanonical == lastAutoStatExpr) {
      rootMathContainer.nodes.clear()
      cursorLocation = CursorLocation(rootMathContainer, 0)
      cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, token)
      lastAutoStatExpr = token
      val evalRes = runCatching {
        StatPredictionLogic.evaluateStatExpression(
          rawExpr = token,
          rows = statRows,
          model = statModel,
          vars = variables + ("Ans" to lastNumericAns),
          angleUnit = angleUnit
        )
      }
      evalRes.onSuccess { v ->
        isExactMode = false
        refreshCompDisplayFromNumeric(v)
        statusToast = "${statModel.label} • $token"
      }.onFailure { err ->
        displayResult = err.message?.take(34) ?: "Math ERROR"
      }
    } else {
      lastAutoStatExpr = null
      cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, token)
    }
  }

  fun handleStatPredictChoice(opToken: String) {
    clearErrorState()
    if (statModel == StatRegModel.ONE_VAR) {
      statResultsLines = listOf("1-VAR has no Regression", "Press SHIFT+1 → 1:Type (2–8)")
      statResultIndex = 0
      lcdState = EsLcdScreenState.STAT_RESULTS
      return
    }
    val curCanonical = rootMathContainer.toCanonicalString()
    if (rootMathContainer.nodes.isNotEmpty() && curCanonical != lastAutoStatExpr) {
      // Casio Postfix Syntax: append ŷ / x̂ / x̂₁ / x̂₂ right after typed operand
      lastAutoStatExpr = null
      cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, opToken)
      lcdState = EsLcdScreenState.CALC_LINE
      statusToast = "Press = to evaluate $opToken"
    } else {
      // Guided LCD Input Prompt
      statPredictTarget = opToken
      cellEditBuffer = ""
      lcdState = EsLcdScreenState.STAT_PREDICT_PROMPT
      statusToast = null
    }
  }

  fun handleMenuChoice(choiceOneBased: Int): Boolean {
    return when (lcdState) {
      EsLcdScreenState.CMPLX_OPS -> {
        when (choiceOneBased) {
          1 -> {
            lcdState = EsLcdScreenState.CALC_LINE
            clearErrorState()
            cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "arg(")
            true
          }
          2 -> {
            lcdState = EsLcdScreenState.CALC_LINE
            clearErrorState()
            cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "Conjg(")
            true
          }
          3 -> {
            lcdState = EsLcdScreenState.CALC_LINE
            clearErrorState()
            complexPolarOutput = true
            cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "▶r∠θ")
            true
          }
          4 -> {
            lcdState = EsLcdScreenState.CALC_LINE
            clearErrorState()
            complexPolarOutput = false
            cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "▶a+bi")
            true
          }
          else -> false
        }
      }
      EsLcdScreenState.MODE_MENU -> {
        val m = ClassWizMode.entries.firstOrNull { it.code == choiceOneBased } ?: return false
        enterMode(m)
        true
      }
      EsLcdScreenState.STAT_TYPE_MENU -> {
        val model = StatRegModel.entries.firstOrNull { it.code == choiceOneBased } ?: return false
        statModel = model
        resetStatRowsBlank()
        statResultsLines = emptyList()
        lastAutoStatExpr = null
        gridCursorRow = 0
        gridCursorCol = 0
        cellEditBuffer = ""
        lcdState = EsLcdScreenState.STAT_EDITOR
        true
      }
      EsLcdScreenState.STAT_SHIFT_MENU -> {
        when (choiceOneBased) {
          1 -> { lcdState = EsLcdScreenState.STAT_TYPE_MENU; true }
          2 -> { lcdState = EsLcdScreenState.STAT_EDITOR; true }
          3 -> { lcdState = EsLcdScreenState.STAT_SUM_MENU; true }
          4 -> { lcdState = EsLcdScreenState.STAT_VAR_MENU; true }
          5 -> {
            if (statModel == StatRegModel.ONE_VAR) {
              statResultsLines = listOf("1-VAR has no Regression", "Select 1:Type (2–8) first")
              statResultIndex = 0
              lcdState = EsLcdScreenState.STAT_RESULTS
            } else {
              lcdState = EsLcdScreenState.STAT_REG_MENU
            }
            true
          }
          6 -> { buildStatResultLines(6); true }
          else -> false
        }
      }
      EsLcdScreenState.STAT_SUM_MENU -> {
        val opt = StatPredictionLogic.sumMenuOptions(statModel).firstOrNull { it.code == choiceOneBased }
          ?: return false
        handleStatSymbolChoice(opt.token)
        true
      }
      EsLcdScreenState.STAT_VAR_MENU -> {
        val opt = StatPredictionLogic.varMenuOptions(statModel).firstOrNull { it.code == choiceOneBased }
          ?: return false
        handleStatSymbolChoice(opt.token)
        true
      }
      EsLcdScreenState.STAT_REG_MENU -> {
        val opt = StatPredictionLogic.regMenuOptions(statModel).firstOrNull { it.code == choiceOneBased }
          ?: return false
        if (opt.token in listOf("x̂", "x̂₁", "x̂₂", "ŷ")) {
          handleStatPredictChoice(opt.token)
        } else {
          handleStatSymbolChoice(opt.token)
        }
        true
      }
      EsLcdScreenState.EQN_TYPE_MENU -> {
        val p = EqnPresetType.entries.firstOrNull { it.code == choiceOneBased } ?: return false
        eqnPreset = p
        resetEqnCoeffsBlank()
        eqnSolutions = emptyList()
        gridCursorRow = 0
        gridCursorCol = 0
        cellEditBuffer = ""
        lcdState = EsLcdScreenState.EQN_COEFF_GRID
        true
      }
      EsLcdScreenState.MATRIX_SELECT_MENU -> {
        val key = when (choiceOneBased) {
          1 -> "MatA"
          2 -> "MatB"
          3 -> "MatC"
          else -> return false
        }
        activeMatKey = key
        if (matMenuForDim) {
          lcdState = EsLcdScreenState.MATRIX_DIM_MENU
        } else {
          gridCursorRow = 0
          gridCursorCol = 0
          cellEditBuffer = ""
          lcdState = EsLcdScreenState.MATRIX_EDITOR
        }
        true
      }
      EsLcdScreenState.MATRIX_DIM_MENU -> {
        val preset = MATRIX_DIM_PRESETS.firstOrNull { it.first == choiceOneBased } ?: return false
        val (r, c) = preset.third
        matrices[activeMatKey] = ClassWizMatrixData(
          rows = r,
          cols = c,
          cells = List(r * c) { 0.0 }
        )
        matCellTexts[activeMatKey] = List(r * c) { "" }
        gridCursorRow = 0
        gridCursorCol = 0
        cellEditBuffer = ""
        lcdState = EsLcdScreenState.MATRIX_EDITOR
        true
      }
      EsLcdScreenState.MATRIX_SHIFT_MENU -> {
        when (choiceOneBased) {
          1 -> { matMenuForDim = true; lcdState = EsLcdScreenState.MATRIX_SELECT_MENU; true }
          2 -> { matMenuForDim = false; lcdState = EsLcdScreenState.MATRIX_SELECT_MENU; true }
          3 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "MatA"); true }
          4 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "MatB"); true }
          5 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "MatC"); true }
          6 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "Det("); true }
          7 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "Trn("); true }
          8 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "⁻¹"); true }
          else -> false
        }
      }
      EsLcdScreenState.VECTOR_SELECT_MENU -> {
        val key = when (choiceOneBased) {
          1 -> "VctA"
          2 -> "VctB"
          3 -> "VctC"
          else -> return false
        }
        activeVctKey = key
        if (vctMenuForDim) {
          lcdState = EsLcdScreenState.VECTOR_DIM_MENU
        } else {
          gridCursorRow = 0
          gridCursorCol = 0
          cellEditBuffer = ""
          lcdState = EsLcdScreenState.VECTOR_EDITOR
        }
        true
      }
      EsLcdScreenState.VECTOR_DIM_MENU -> {
        val d = when (choiceOneBased) {
          1 -> 3
          2 -> 2
          else -> return false
        }
        vectors[activeVctKey] = ClassWizVectorData(d, List(d) { 0.0 })
        vctCellTexts[activeVctKey] = List(d) { "" }
        gridCursorRow = 0
        gridCursorCol = 0
        cellEditBuffer = ""
        lcdState = EsLcdScreenState.VECTOR_EDITOR
        true
      }
      EsLcdScreenState.VECTOR_SHIFT_MENU -> {
        when (choiceOneBased) {
          1 -> { vctMenuForDim = true; lcdState = EsLcdScreenState.VECTOR_SELECT_MENU; true }
          2 -> { vctMenuForDim = false; lcdState = EsLcdScreenState.VECTOR_SELECT_MENU; true }
          3 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "VctA"); true }
          4 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "VctB"); true }
          5 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "VctC"); true }
          6 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "•"); true }
          7 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "abs("); true }
          8 -> { lcdState = EsLcdScreenState.CALC_LINE; clearErrorState(); cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, "Angle(VctA,VctB)"); true }
          else -> false
        }
      }
      EsLcdScreenState.CLR_MENU -> {
        when (choiceOneBased) {
          1 -> {
            angleUnit = ClassWizAngleUnit.DEG
            numberFormat = ClassWizNumberFormat.NORM
            fixDigits = 4
            engShift = 0
            isExactMode = true
            isMixedFraction = false
            isDmsOutput = false
            complexPolarOutput = false
            lcdState = EsLcdScreenState.CALC_LINE
            statusToast = "Setup cleared"
            true
          }
          2 -> {
            variables.keys.forEach { variables[it] = 0.0 }
            lastNumericAns = 0.0
            lastComplexAns = ComplexNum(0.0, 0.0)
            lcdState = EsLcdScreenState.CALC_LINE
            statusToast = "Memory A–F, X, Y, M cleared"
            true
          }
          3 -> {
            variables.keys.forEach { variables[it] = 0.0 }
            history.clear()
            historyIndex = -1
            lastNumericAns = 0.0
            lastComplexAns = ComplexNum(0.0, 0.0)
            angleUnit = ClassWizAngleUnit.DEG
            numberFormat = ClassWizNumberFormat.NORM
            fixDigits = 4
            engShift = 0
            isExactMode = true
            isMixedFraction = false
            isDmsOutput = false
            complexPolarOutput = false
            resetStatRowsBlank()
            statResultsLines = emptyList()
            lastAutoStatExpr = null
            resetEqnCoeffsBlank()
            eqnSolutions = emptyList()
            listOf("MatA", "MatB", "MatC").forEach { k ->
              matrices[k] = ClassWizMatrixData(2, 2, listOf(0.0, 0.0, 0.0, 0.0))
              matCellTexts[k] = List(4) { "" }
            }
            listOf("VctA", "VctB", "VctC").forEach { k ->
              vectors[k] = ClassWizVectorData(3, listOf(0.0, 0.0, 0.0))
              vctCellTexts[k] = List(3) { "" }
            }
            tableFx = ""
            tableGx = ""
            tableStart = ""
            tableEnd = ""
            tableStep = ""
            tableRows = emptyList()
            tableScrollIdx = 0
            enterMode(ClassWizMode.COMP)
            statusToast = "All cleared"
            true
          }
          else -> false
        }
      }
      else -> false
    }
  }

  fun commitActiveGridCellAndAdvance() {
    when (lcdState) {
      EsLcdScreenState.STAT_EDITOR -> {
        while (statRows.size <= gridCursorRow && statRows.size < 100) {
          statRows.add(StatTableRow())
        }
        if (cellEditBuffer.isNotBlank()) {
          val v = runCatching { evaluateScalar(cellEditBuffer, variables, angleUnit) }.getOrNull()
          if (v == null || v.isNaN() || v.isInfinite()) {
            statusToast = "Syntax ERROR"
            return
          }
          val formatted = formatDecimalCompact(cleanZero(v))
          val cur = statRows[gridCursorRow]
          statRows[gridCursorRow] = if (gridCursorCol == 0) {
            cur.copy(xText = formatted)
          } else {
            cur.copy(yText = formatted)
          }
        }
        cellEditBuffer = ""
        if (statModel == StatRegModel.ONE_VAR) {
          gridCursorRow = (gridCursorRow + 1).coerceAtMost(99)
        } else {
          if (gridCursorCol == 0) {
            gridCursorCol = 1
          } else {
            gridCursorCol = 0
            gridCursorRow = (gridCursorRow + 1).coerceAtMost(99)
          }
        }
        while (gridCursorRow >= statRows.size - 1 && statRows.size < 100) {
          statRows.add(StatTableRow())
        }
      }
      EsLcdScreenState.EQN_COEFF_GRID -> {
        val rowsCount = if (eqnPreset.isPoly) 1 else eqnPreset.size
        val colsCount = eqnPreset.headers.size
        val flatIdx = if (eqnPreset.isPoly) gridCursorCol else gridCursorRow * 5 + gridCursorCol
        if (flatIdx in eqnCoeffs.indices) {
          if (cellEditBuffer.isNotBlank()) {
            val v = runCatching { evaluateScalar(cellEditBuffer, variables, angleUnit) }.getOrNull()
            if (v == null || v.isNaN() || v.isInfinite()) {
              statusToast = "Syntax ERROR"
              return
            }
            eqnCoeffs[flatIdx] = formatDecimalCompact(cleanZero(v))
          } else if (eqnCoeffs[flatIdx].isEmpty()) {
            eqnCoeffs[flatIdx] = "0"
          }
        }
        cellEditBuffer = ""
        if (gridCursorCol + 1 < colsCount) {
          gridCursorCol++
        } else if (gridCursorRow + 1 < rowsCount) {
          gridCursorCol = 0
          gridCursorRow++
        } else {
          computeEqnSolutionsForPreset()
        }
      }
      EsLcdScreenState.MATRIX_EDITOR -> {
        val mat = matrices[activeMatKey] ?: ClassWizMatrixData()
        val flatIdx = gridCursorRow * mat.cols + gridCursorCol
        val curTexts = (matCellTexts[activeMatKey] ?: List(mat.rows * mat.cols) { "" }).toMutableList()
        while (curTexts.size < mat.rows * mat.cols) curTexts.add("")
        if (flatIdx in mat.cells.indices) {
          val next = mat.cells.toMutableList()
          if (cellEditBuffer.isNotBlank()) {
            val v = runCatching { evaluateScalar(cellEditBuffer, variables, angleUnit) }.getOrNull()
            if (v == null || v.isNaN() || v.isInfinite()) {
              statusToast = "Syntax ERROR"
              return
            }
            val cleaned = cleanZero(v)
            next[flatIdx] = cleaned
            curTexts[flatIdx] = formatDecimalCompact(cleaned)
          } else if (curTexts[flatIdx].isEmpty()) {
            next[flatIdx] = 0.0
            curTexts[flatIdx] = "0"
          }
          matrices[activeMatKey] = mat.copy(cells = next)
          matCellTexts[activeMatKey] = curTexts
        }
        cellEditBuffer = ""
        if (gridCursorCol + 1 < mat.cols) {
          gridCursorCol++
        } else if (gridCursorRow + 1 < mat.rows) {
          gridCursorCol = 0
          gridCursorRow++
        } else {
          gridCursorRow = 0
          gridCursorCol = 0
        }
      }
      EsLcdScreenState.VECTOR_EDITOR -> {
        val vct = vectors[activeVctKey] ?: ClassWizVectorData()
        val flatIdx = gridCursorCol.coerceIn(0, vct.dim - 1)
        val curTexts = (vctCellTexts[activeVctKey] ?: List(vct.dim) { "" }).toMutableList()
        while (curTexts.size < vct.dim) curTexts.add("")
        val next = vct.components.toMutableList()
        if (cellEditBuffer.isNotBlank()) {
          val v = runCatching { evaluateScalar(cellEditBuffer, variables, angleUnit) }.getOrNull()
          if (v == null || v.isNaN() || v.isInfinite()) {
            statusToast = "Syntax ERROR"
            return
          }
          val cleaned = cleanZero(v)
          next[flatIdx] = cleaned
          curTexts[flatIdx] = formatDecimalCompact(cleaned)
        } else if (curTexts[flatIdx].isEmpty()) {
          next[flatIdx] = 0.0
          curTexts[flatIdx] = "0"
        }
        vectors[activeVctKey] = vct.copy(components = next)
        vctCellTexts[activeVctKey] = curTexts
        cellEditBuffer = ""
        gridCursorCol = (gridCursorCol + 1) % vct.dim
      }
      else -> {}
    }
  }

  fun insertToken(token: String) {
    playCardHapticSound(context, localView)
    // Intercept 1..8 when an In-LCD numbered menu is open
    if (token.length == 1 && token[0] in '1'..'8') {
      val digit = token[0] - '0'
      if (handleMenuChoice(digit)) {
        isShiftActive = false
        isAlphaActive = false
        return
      }
    }
    // Intercept CONST / CONV 2-digit code entry inside LCD
    if (lcdState == EsLcdScreenState.CONST_PROMPT || lcdState == EsLcdScreenState.CONV_PROMPT) {
      if (token.length == 1 && token[0].isDigit()) {
        codePromptBuffer = (codePromptBuffer + token).takeLast(2)
        if (codePromptBuffer.length == 2) {
          val codeInt = codePromptBuffer.toIntOrNull() ?: 1
          codePromptPage = ((codeInt - 1).coerceAtLeast(0)) / 6
        }
      }
      isShiftActive = false
      isAlphaActive = false
      return
    }
    // Intercept cell editing in STAT / EQN / MATRIX / VECTOR editors
    if (lcdState == EsLcdScreenState.STAT_EDITOR ||
      lcdState == EsLcdScreenState.EQN_COEFF_GRID ||
      lcdState == EsLcdScreenState.MATRIX_EDITOR ||
      lcdState == EsLcdScreenState.VECTOR_EDITOR
    ) {
      cellEditBuffer += token
      isShiftActive = false
      isAlphaActive = false
      return
    }
    // Intercept TABLE Start/End/Step and STAT Prediction numeric prompts
    if (lcdState == EsLcdScreenState.TABLE_PROMPT_START ||
      lcdState == EsLcdScreenState.TABLE_PROMPT_END ||
      lcdState == EsLcdScreenState.TABLE_PROMPT_STEP ||
      lcdState == EsLcdScreenState.STAT_PREDICT_PROMPT
    ) {
      cellEditBuffer += token
      isShiftActive = false
      isAlphaActive = false
      return
    }

    clearErrorState()
    lastAutoStatExpr = null
    cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, token)
    isShiftActive = false
    isAlphaActive = false
    isHypPending = false
  }

  fun deleteAtCursor() {
    playCardHapticSound(context, localView)
    if (lcdState == EsLcdScreenState.CMPLX_OPS) {
      lcdState = EsLcdScreenState.CALC_LINE
      return
    }
    if (lcdState == EsLcdScreenState.CONST_PROMPT || lcdState == EsLcdScreenState.CONV_PROMPT) {
      if (codePromptBuffer.isNotEmpty()) codePromptBuffer = codePromptBuffer.dropLast(1)
      else lcdState = EsLcdScreenState.CALC_LINE
      return
    }
    if (lcdState == EsLcdScreenState.STAT_EDITOR ||
      lcdState == EsLcdScreenState.EQN_COEFF_GRID ||
      lcdState == EsLcdScreenState.MATRIX_EDITOR ||
      lcdState == EsLcdScreenState.VECTOR_EDITOR ||
      lcdState == EsLcdScreenState.TABLE_PROMPT_START ||
      lcdState == EsLcdScreenState.TABLE_PROMPT_END ||
      lcdState == EsLcdScreenState.TABLE_PROMPT_STEP ||
      lcdState == EsLcdScreenState.STAT_PREDICT_PROMPT
    ) {
      if (cellEditBuffer.isNotEmpty()) {
        cellEditBuffer = cellEditBuffer.dropLast(1)
      } else {
        when (lcdState) {
          EsLcdScreenState.STAT_EDITOR -> if (gridCursorRow in statRows.indices) {
            val cur = statRows[gridCursorRow]
            statRows[gridCursorRow] = if (gridCursorCol == 0) cur.copy(xText = "") else cur.copy(yText = "")
          }
          EsLcdScreenState.EQN_COEFF_GRID -> {
            val flatIdx = if (eqnPreset.isPoly) gridCursorCol else gridCursorRow * 5 + gridCursorCol
            if (flatIdx in eqnCoeffs.indices) eqnCoeffs[flatIdx] = ""
          }
          EsLcdScreenState.MATRIX_EDITOR -> {
            val mat = matrices[activeMatKey] ?: ClassWizMatrixData()
            val flatIdx = gridCursorRow * mat.cols + gridCursorCol
            val curTexts = (matCellTexts[activeMatKey] ?: List(mat.rows * mat.cols) { "" }).toMutableList()
            if (flatIdx in curTexts.indices) {
              curTexts[flatIdx] = ""
              matCellTexts[activeMatKey] = curTexts
            }
            if (flatIdx in mat.cells.indices) {
              val next = mat.cells.toMutableList()
              next[flatIdx] = 0.0
              matrices[activeMatKey] = mat.copy(cells = next)
            }
          }
          EsLcdScreenState.VECTOR_EDITOR -> {
            val vct = vectors[activeVctKey] ?: ClassWizVectorData()
            val flatIdx = gridCursorCol.coerceIn(0, vct.dim - 1)
            val curTexts = (vctCellTexts[activeVctKey] ?: List(vct.dim) { "" }).toMutableList()
            if (flatIdx in curTexts.indices) {
              curTexts[flatIdx] = ""
              vctCellTexts[activeVctKey] = curTexts
            }
            if (flatIdx in vct.components.indices) {
              val next = vct.components.toMutableList()
              next[flatIdx] = 0.0
              vectors[activeVctKey] = vct.copy(components = next)
            }
          }
          else -> {}
        }
      }
      return
    }
    clearErrorState()
    lastAutoStatExpr = null
    cursorLocation = deleteOrUnwrapAtCursor(rootMathContainer, cursorLocation)
  }

  fun handleReplayDpad(dir: String) {
    playCardHapticSound(context, localView)
    when (lcdState) {
      EsLcdScreenState.STAT_EDITOR -> {
        when (dir) {
          "UP" -> gridCursorRow = (gridCursorRow - 1).coerceAtLeast(0)
          "DOWN" -> {
            if (gridCursorRow >= statRows.size - 1 && statRows.size < 100) {
              val cur = statRows.lastOrNull()
              if (cur != null && (cur.xText.isNotEmpty() || cur.yText.isNotEmpty())) {
                statRows.add(StatTableRow())
              }
            }
            val maxRow = (statRows.size - 1).coerceAtLeast(0)
            gridCursorRow = (gridCursorRow + 1).coerceAtMost(maxRow)
          }
          "LEFT" -> if (statModel != StatRegModel.ONE_VAR) gridCursorCol = 0
          "RIGHT" -> if (statModel != StatRegModel.ONE_VAR) gridCursorCol = 1
        }
        cellEditBuffer = ""
      }
      EsLcdScreenState.STAT_RESULTS -> {
        if (statResultsLines.isNotEmpty()) {
          when (dir) {
            "UP" -> statResultIndex = (statResultIndex - 1).coerceAtLeast(0)
            "DOWN" -> statResultIndex = (statResultIndex + 1).coerceAtMost(statResultsLines.lastIndex)
          }
        }
      }
      EsLcdScreenState.EQN_COEFF_GRID -> {
        val rowsCount = if (eqnPreset.isPoly) 1 else eqnPreset.size
        val colsCount = eqnPreset.headers.size
        when (dir) {
          "UP" -> gridCursorRow = (gridCursorRow - 1).coerceAtLeast(0)
          "DOWN" -> gridCursorRow = (gridCursorRow + 1).coerceAtMost(rowsCount - 1)
          "LEFT" -> gridCursorCol = (gridCursorCol - 1).coerceAtLeast(0)
          "RIGHT" -> gridCursorCol = (gridCursorCol + 1).coerceAtMost(colsCount - 1)
        }
        cellEditBuffer = ""
      }
      EsLcdScreenState.EQN_SOLUTIONS -> {
        if (eqnSolutions.isNotEmpty()) {
          when (dir) {
            "UP", "LEFT" -> eqnSolutionIdx = (eqnSolutionIdx - 1).coerceAtLeast(0)
            "DOWN", "RIGHT" -> eqnSolutionIdx = (eqnSolutionIdx + 1).coerceAtMost(eqnSolutions.lastIndex)
          }
        }
      }
      EsLcdScreenState.MATRIX_EDITOR -> {
        val mat = matrices[activeMatKey] ?: ClassWizMatrixData()
        when (dir) {
          "UP" -> gridCursorRow = (gridCursorRow - 1).coerceAtLeast(0)
          "DOWN" -> gridCursorRow = (gridCursorRow + 1).coerceAtMost(mat.rows - 1)
          "LEFT" -> gridCursorCol = (gridCursorCol - 1).coerceAtLeast(0)
          "RIGHT" -> gridCursorCol = (gridCursorCol + 1).coerceAtMost(mat.cols - 1)
        }
        cellEditBuffer = ""
      }
      EsLcdScreenState.VECTOR_EDITOR -> {
        val vct = vectors[activeVctKey] ?: ClassWizVectorData()
        when (dir) {
          "LEFT", "UP" -> gridCursorCol = (gridCursorCol - 1).coerceAtLeast(0)
          "RIGHT", "DOWN" -> gridCursorCol = (gridCursorCol + 1).coerceAtMost(vct.dim - 1)
        }
        cellEditBuffer = ""
      }
      EsLcdScreenState.TABLE_VIEW -> {
        val maxScroll = (tableRows.size - 3).coerceAtLeast(0)
        when (dir) {
          "UP" -> tableScrollIdx = (tableScrollIdx - 1).coerceAtLeast(0)
          "DOWN" -> tableScrollIdx = (tableScrollIdx + 1).coerceAtMost(maxScroll)
        }
      }
      EsLcdScreenState.CONST_PROMPT, EsLcdScreenState.CONV_PROMPT -> {
        when (dir) {
          "UP", "LEFT" -> codePromptPage = (codePromptPage - 1).coerceAtLeast(0)
          "DOWN", "RIGHT" -> codePromptPage = (codePromptPage + 1).coerceAtMost(6)
        }
      }
      else -> {
        if (isErrorActive && (dir == "LEFT" || dir == "RIGHT")) {
          val goto = errorGotoTarget
          clearErrorState()
          hasCustomTextAnswer = false
          displayResult = "0"
          answerMathTree = mathContainerOfText("0")
          cursorLocation = if (goto != null && containsContainer(rootMathContainer, goto.container)) {
            CursorLocation(goto.container, goto.index.coerceIn(0, goto.container.nodes.size))
          } else {
            CursorLocation(rootMathContainer, rootMathContainer.nodes.size)
          }
          return
        }
        clearErrorState()
        when (dir) {
          "UP" -> {
            val vert = moveCursorVertical(rootMathContainer, cursorLocation, isUp = true)
            if (vert != null) {
              cursorLocation = vert
            } else if (lcdState == EsLcdScreenState.CALC_LINE && history.isNotEmpty()) {
              historyIndex = (historyIndex + 1).coerceAtMost(history.lastIndex)
              val h = history[historyIndex]
              loadTreeIntoRoot(h.treeSnapshot ?: mathContainerOfText(h.expression))
              displayResult = h.exactOrDisplay
            }
          }
          "DOWN" -> {
            val vert = moveCursorVertical(rootMathContainer, cursorLocation, isUp = false)
            if (vert != null) {
              cursorLocation = vert
            } else if (lcdState == EsLcdScreenState.CALC_LINE) {
              if (historyIndex > 0) {
                historyIndex--
                val h = history[historyIndex]
                loadTreeIntoRoot(h.treeSnapshot ?: mathContainerOfText(h.expression))
                displayResult = h.exactOrDisplay
              } else {
                historyIndex = -1
              }
            }
          }
          "LEFT" -> cursorLocation = moveCursorLeft(rootMathContainer, cursorLocation)
          "RIGHT" -> cursorLocation = moveCursorRight(rootMathContainer, cursorLocation)
        }
      }
    }
  }

  fun triggerAstError(title: String, target: CursorLocation?) {
    displayResult = title
    statusToast = "[◄][►] : Goto"
    errorGotoTarget = target ?: CursorLocation(rootMathContainer, rootMathContainer.nodes.size)
    isErrorActive = true
  }

  fun splitStatementsAtColon(nodes: List<MathNode>): List<List<MathNode>> {
    val out = mutableListOf<List<MathNode>>()
    var cur = mutableListOf<MathNode>()
    for (n in nodes) {
      if (n is MathNode.TextNode && n.text == ":") {
        if (cur.isNotEmpty()) {
          out.add(cur)
          cur = mutableListOf()
        }
      } else {
        cur.add(n)
      }
    }
    if (cur.isNotEmpty()) out.add(cur)
    return out
  }

  fun splitTopLevelTwoArgs(nodes: List<MathNode>, prefixFn: String): Pair<List<MathNode>, List<MathNode>>? {
    val first = nodes.firstOrNull() as? MathNode.TextNode ?: return null
    val last = nodes.lastOrNull() as? MathNode.TextNode ?: return null
    if (first.text != prefixFn || last.text != ")") return null
    val inner = nodes.subList(1, nodes.size - 1)
    var depth = 0
    for (idx in inner.indices) {
      val n = inner[idx]
      if (n is MathNode.TextNode) {
        when {
          n.text == "(" || n.text.endsWith("(") -> depth++
          n.text == ")" -> depth = (depth - 1).coerceAtLeast(0)
          n.text == "," && depth == 0 -> {
            return inner.subList(0, idx) to inner.subList(idx + 1, inner.size)
          }
        }
      }
    }
    return null
  }

  fun executeEvaluation(forceApprox: Boolean = false) {
    playCardHapticSound(context, localView)
    if (forceApprox) isExactMode = false
    clearErrorState()

    // Handle non-CALC_LINE states first
    when (lcdState) {
      EsLcdScreenState.STAT_EDITOR,
      EsLcdScreenState.EQN_COEFF_GRID,
      EsLcdScreenState.MATRIX_EDITOR,
      EsLcdScreenState.VECTOR_EDITOR -> {
        commitActiveGridCellAndAdvance()
        return
      }
      EsLcdScreenState.EQN_SOLUTIONS -> {
        if (eqnSolutions.isNotEmpty()) {
          eqnSolutionIdx = (eqnSolutionIdx + 1) % eqnSolutions.size
        }
        return
      }
      EsLcdScreenState.TABLE_PROMPT_FX -> {
        if (rootMathContainer.nodes.isEmpty()) {
          statusToast = "Enter f(X) first"
          return
        }
        val emptySlot = findFirstEmptySlot(rootMathContainer)
        if (emptySlot != null) {
          cursorLocation = CursorLocation(emptySlot, 0)
          statusToast = "Fill empty box [ ] in f(X)"
          return
        }
        tableFxTree = cloneMathContainer(rootMathContainer)
        tableFx = rootMathContainer.toCanonicalString().trim()
        loadTreeIntoRoot(tableGxTree)
        lcdState = EsLcdScreenState.TABLE_PROMPT_GX
        return
      }
      EsLcdScreenState.TABLE_PROMPT_GX -> {
        if (rootMathContainer.nodes.isEmpty()) {
          tableGxTree = null
          tableGx = ""
        } else {
          val emptySlot = findFirstEmptySlot(rootMathContainer)
          if (emptySlot != null) {
            cursorLocation = CursorLocation(emptySlot, 0)
            statusToast = "Fill empty box [ ] in g(X)"
            return
          }
          tableGxTree = cloneMathContainer(rootMathContainer)
          tableGx = rootMathContainer.toCanonicalString().trim()
        }
        resetExpressionTree()
        cellEditBuffer = tableStart
        lcdState = EsLcdScreenState.TABLE_PROMPT_START
        return
      }
      EsLcdScreenState.TABLE_PROMPT_START -> {
        if (cellEditBuffer.isBlank()) {
          statusToast = "Enter Start value"
          return
        }
        tableStart = cellEditBuffer.trim()
        cellEditBuffer = tableEnd
        lcdState = EsLcdScreenState.TABLE_PROMPT_END
        return
      }
      EsLcdScreenState.TABLE_PROMPT_END -> {
        if (cellEditBuffer.isBlank()) {
          statusToast = "Enter End value"
          return
        }
        tableEnd = cellEditBuffer.trim()
        cellEditBuffer = tableStep
        lcdState = EsLcdScreenState.TABLE_PROMPT_STEP
        return
      }
      EsLcdScreenState.TABLE_PROMPT_STEP -> {
        if (cellEditBuffer.isBlank()) {
          statusToast = "Enter Step value"
          return
        }
        tableStep = cellEditBuffer.trim()
        cellEditBuffer = ""
        buildTableData()
        return
      }
      EsLcdScreenState.STAT_PREDICT_PROMPT -> {
        val rawArg = cellEditBuffer.trim()
        if (rawArg.isEmpty()) {
          statusToast = if (statPredictTarget == "ŷ") "Enter X value first" else "Enter Y value first"
          return
        }
        try {
          val argVal = StatPredictionLogic.evaluateStatExpression(
            rawExpr = rawArg,
            rows = statRows,
            model = statModel,
            vars = variables + ("Ans" to lastNumericAns),
            angleUnit = angleUnit
          )
          val pred = StatPredictionLogic.predict(statRows, statModel, statPredictTarget, argVal)
          val treeExpr = mathContainerOfText("$rawArg$statPredictTarget")
          loadTreeIntoRoot(treeExpr)
          lastAutoStatExpr = "$rawArg$statPredictTarget"
          cellEditBuffer = ""
          lcdState = EsLcdScreenState.CALC_LINE
          isExactMode = false
          refreshCompDisplayFromNumeric(pred)
          statusToast = if (statPredictTarget == "ŷ") {
            "ŷ at X=${formatDecimalCompact(argVal)}"
          } else {
            "$statPredictTarget at Y=${formatDecimalCompact(argVal)}"
          }
          pushHistory(lastAutoStatExpr!!, displayResult, pred, rootMathContainer)
        } catch (e: Throwable) {
          lcdState = EsLcdScreenState.CALC_LINE
          loadTreeIntoRoot(mathContainerOfText("$rawArg$statPredictTarget"))
          cellEditBuffer = ""
          displayResult = e.message?.take(34) ?: "Math ERROR"
        }
        return
      }
      EsLcdScreenState.CONST_PROMPT -> {
        val codeStr = codePromptBuffer.padStart(2, '0')
        val item = ES_PLUS_CONSTANTS.firstOrNull { it.code == codeStr }
        if (item != null) {
          lcdState = EsLcdScreenState.CALC_LINE
          cursorLocation = insertTokenIntoTree(rootMathContainer, cursorLocation, item.value.toString())
          statusToast = "${item.symbol} (${item.unit})"
        }
        return
      }
      EsLcdScreenState.CONV_PROMPT -> {
        val codeStr = codePromptBuffer.padStart(2, '0')
        val item = ES_PLUS_CONVERSIONS.firstOrNull { it.code == codeStr }
        if (item != null) {
          val baseVal = if (rootMathContainer.nodes.isNotEmpty()) {
            runCatching {
              evaluateMathAst(rootMathContainer, variables + ("Ans" to lastNumericAns), angleUnit)
            }.getOrElse { lastNumericAns }
          } else lastNumericAns
          val outVal = item.convert(baseVal)
          preserveAnswerOnNextCalcLine = true
          lcdState = EsLcdScreenState.CALC_LINE
          refreshCompDisplayFromNumeric(outVal)
          statusToast = "${item.label} (${item.toUnit})"
          pushHistory("${formatDecimalCompact(baseVal)} ${item.label}", displayResult, outVal)
        }
        return
      }
      else -> {}
    }

    engShift = 0
    isDmsOutput = false
    if (rootMathContainer.nodes.isEmpty()) {
      hasCustomTextAnswer = false
      if (currentMode == ClassWizMode.BASE_N) {
        displayResult = BaseNLogic.formatForRadix(lastBaseInt, baseRadix)
        answerMathTree = mathContainerOfText(displayResult)
      } else {
        activeDisplayNumeric = 0.0
        displayResult = "0"
        answerMathTree = mathContainerOfText("0")
      }
      return
    }
    val emptySlot = findFirstEmptySlot(rootMathContainer)
    if (emptySlot != null) {
      triggerAstError("Syntax ERROR", CursorLocation(emptySlot, 0))
      return
    }

    val raw = rootMathContainer.toCanonicalString().trim()
    try {
      when (currentMode) {
        ClassWizMode.COMP, ClassWizMode.STAT -> {
          val statements = splitStatementsAtColon(rootMathContainer.nodes)
          var latestRes = lastNumericAns
          var customDisp: String? = null
          for (stmtNodes in statements) {
            customDisp = null
            val eqIdx = stmtNodes.indexOfFirst { it is MathNode.TextNode && it.text == "=" }
            val polPair = splitTopLevelTwoArgs(stmtNodes, "Pol(")
            val recPair = splitTopLevelTwoArgs(stmtNodes, "Rec(")
            if (eqIdx >= 0) {
              val lhs = stmtNodes.subList(0, eqIdx)
              val rhs = stmtNodes.subList(eqIdx + 1, stmtNodes.size)
              if (lhs.isEmpty() || rhs.isEmpty()) {
                throw AstEvalException("Syntax ERROR", CursorLocation(rootMathContainer, rootMathContainer.nodes.size))
              }
              val rootX = solveAstEquationForX(lhs, rhs, rootMathContainer, variables + ("Ans" to latestRes), angleUnit)
              variables["X"] = cleanZero(rootX)
              latestRes = rootX
              customDisp = "X = ${formatDecimalCompact(rootX, numberFormat, fixDigits)}"
            } else if (polPair != null) {
              val x = evaluateNodeListAst(rootMathContainer, polPair.first, variables + ("Ans" to latestRes), angleUnit)
              val y = evaluateNodeListAst(rootMathContainer, polPair.second, variables + ("Ans" to latestRes), angleUnit)
              val r = hypot(x, y)
              val th = angleUnit.fromRadians(atan2(y, x))
              variables["X"] = cleanZero(r)
              variables["Y"] = cleanZero(th)
              latestRes = r
              customDisp = "r=${formatDecimalCompact(r, numberFormat, fixDigits)}, θ=${formatDecimalCompact(th, numberFormat, fixDigits)}"
            } else if (recPair != null) {
              val r = evaluateNodeListAst(rootMathContainer, recPair.first, variables + ("Ans" to latestRes), angleUnit)
              val th = evaluateNodeListAst(rootMathContainer, recPair.second, variables + ("Ans" to latestRes), angleUnit)
              val rad = angleUnit.toRadians(th)
              val x = r * cos(rad)
              val y = r * sin(rad)
              variables["X"] = cleanZero(x)
              variables["Y"] = cleanZero(y)
              latestRes = x
              customDisp = "x=${formatDecimalCompact(x, numberFormat, fixDigits)}, y=${formatDecimalCompact(y, numberFormat, fixDigits)}"
            } else if (currentMode == ClassWizMode.STAT) {
              latestRes = StatPredictionLogic.evaluateStatExpression(
                rawExpr = stmtNodes.toCanonicalString(),
                rows = statRows,
                model = statModel,
                vars = variables + ("Ans" to latestRes),
                angleUnit = angleUnit
              )
            } else {
              latestRes = evaluateNodeListAst(
                ownerContainer = rootMathContainer,
                nodes = stmtNodes,
                vars = variables + ("Ans" to latestRes),
                angleUnit = angleUnit
              )
            }
          }
          if (customDisp != null) {
            activeDisplayNumeric = cleanZero(latestRes)
            lastNumericAns = cleanZero(latestRes)
            hasCustomTextAnswer = true
            displayResult = customDisp
            answerMathTree = mathContainerOfText(customDisp)
          } else {
            if (currentMode == ClassWizMode.STAT && !forceApprox) {
              isExactMode = false
            }
            refreshCompDisplayFromNumeric(latestRes)
          }
          pushHistory(raw, displayResult, latestRes, rootMathContainer)
          if (currentMode == ClassWizMode.STAT && StatPredictionLogic.isStandaloneStatToken(raw)) {
            lastAutoStatExpr = raw
          } else {
            lastAutoStatExpr = null
          }
        }
        ClassWizMode.CMPLX -> {
          val cVars = variables.mapValues { ComplexNum(it.value, 0.0) } + ("Ans" to lastComplexAns)
          val (z, effPolar) = evalComplexTreeAware(raw, cVars)
          refreshComplexDisplay(z, polar = effPolar)
          pushHistory(raw, displayResult, z.re, rootMathContainer)
        }
        ClassWizMode.BASE_N -> {
          val bVal = BaseNLogic.evaluateBaseN(raw, baseRadix)
          lastBaseInt = bVal
          activeDisplayNumeric = bVal.toDouble()
          lastNumericAns = bVal.toDouble()
          hasCustomTextAnswer = false
          displayResult = BaseNLogic.formatForRadix(bVal, baseRadix)
          answerMathTree = mathContainerOfText(displayResult)
          pushHistory(raw, "${baseRadix.label}: $displayResult", bVal.toDouble(), rootMathContainer)
        }
        ClassWizMode.MATRIX -> {
          val mat = evaluateMatrixExpression(raw, matrices, variables + ("Ans" to lastNumericAns), angleUnit)
          if (mat.rows == 1 && mat.cols == 1 && raw.startsWith("Det(")) {
            lastMatrixResult = null
            refreshCompDisplayFromNumeric(mat.get(0, 0))
          } else {
            lastMatrixResult = mat
            hasCustomTextAnswer = true
            displayResult = "MatAns (${mat.rows}×${mat.cols})"
            answerMathTree = mathContainerOfText(displayResult)
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
            hasCustomTextAnswer = true
            displayResult = "[ " + (0 until vec.dim).joinToString(", ") {
              formatDecimalCompact(vec.get(it), numberFormat, fixDigits)
            } + " ]"
            answerMathTree = mathContainerOfText(displayResult)
          }
        }
        else -> {
          val res = evaluateMathAst(rootMathContainer, variables + ("Ans" to lastNumericAns), angleUnit)
          refreshCompDisplayFromNumeric(res)
          pushHistory(raw, displayResult, res, rootMathContainer)
        }
      }
    } catch (astErr: AstEvalException) {
      triggerAstError(astErr.errorTitle, astErr.gotoLocation)
    } catch (e: Throwable) {
      val msg = e.message?.take(34) ?: "Math ERROR"
      val title = when {
        msg.contains("Syntax", ignoreCase = true) -> "Syntax ERROR"
        msg.contains("Math", ignoreCase = true) -> msg
        else -> msg
      }
      triggerAstError(title, CursorLocation(rootMathContainer, rootMathContainer.nodes.size))
    }
  }

  fun handleAcButton() {
    playCardHapticSound(context, localView)
    isShiftActive = false
    isAlphaActive = false
    cellEditBuffer = ""
    statusToast = null
    lastAutoStatExpr = null
    clearErrorState()
    when (lcdState) {
      EsLcdScreenState.MODE_MENU,
      EsLcdScreenState.CMPLX_OPS,
      EsLcdScreenState.STAT_SHIFT_MENU,
      EsLcdScreenState.STAT_SUM_MENU,
      EsLcdScreenState.STAT_VAR_MENU,
      EsLcdScreenState.STAT_REG_MENU,
      EsLcdScreenState.STAT_PREDICT_PROMPT,
      EsLcdScreenState.STAT_RESULTS,
      EsLcdScreenState.MATRIX_SHIFT_MENU,
      EsLcdScreenState.VECTOR_SHIFT_MENU,
      EsLcdScreenState.CONST_PROMPT,
      EsLcdScreenState.CONV_PROMPT,
      EsLcdScreenState.CLR_MENU -> {
        lcdState = EsLcdScreenState.CALC_LINE
      }
      EsLcdScreenState.STAT_EDITOR,
      EsLcdScreenState.MATRIX_EDITOR,
      EsLcdScreenState.VECTOR_EDITOR -> {
        lcdState = EsLcdScreenState.CALC_LINE
        resetExpressionTree()
        hasCustomTextAnswer = false
        activeDisplayNumeric = 0.0
        displayResult = "0"
        answerMathTree = mathContainerOfText("0")
      }
      EsLcdScreenState.EQN_SOLUTIONS -> {
        lcdState = EsLcdScreenState.EQN_COEFF_GRID
      }
      EsLcdScreenState.TABLE_PROMPT_GX,
      EsLcdScreenState.TABLE_PROMPT_START,
      EsLcdScreenState.TABLE_PROMPT_END,
      EsLcdScreenState.TABLE_PROMPT_STEP,
      EsLcdScreenState.TABLE_VIEW -> {
        loadTreeIntoRoot(tableFxTree)
        lcdState = EsLcdScreenState.TABLE_PROMPT_FX
      }
      else -> {
        resetExpressionTree()
        lastMatrixResult = null
        lastVectorResult = null
        hasCustomTextAnswer = false
        activeDisplayNumeric = 0.0
        displayResult = "0"
        answerMathTree = mathContainerOfText("0")
        engShift = 0
        isDmsOutput = false
      }
    }
  }

  fun handleVariableKey(varName: String) {
    if (isStoPending) {
      isStoPending = false
      val valToStore = if (rootMathContainer.nodes.isNotEmpty()) {
        runCatching { evaluateMathAst(rootMathContainer, variables + ("Ans" to lastNumericAns), angleUnit) }
          .getOrElse { lastNumericAns }
      } else lastNumericAns
      variables[varName] = cleanZero(valToStore)
      activeDisplayNumeric = cleanZero(valToStore)
      lastNumericAns = cleanZero(valToStore)
      hasCustomTextAnswer = true
      displayResult = "$varName ← ${formatDecimalCompact(valToStore, numberFormat, fixDigits)}"
      answerMathTree = mathContainerOfText(displayResult)
      statusToast = "Stored in $varName"
    } else {
      insertToken(varName)
    }
  }

  // Automatic Live Evaluation (MathO 2D Natural Display Preview as user edits expression)
  val liveCanonicalKey = rootMathContainer.toCanonicalString()
  LaunchedEffect(
    lcdState,
    currentMode,
    liveCanonicalKey,
    isExactMode,
    isMixedFraction,
    isDmsOutput,
    complexPolarOutput,
    angleUnit,
    numberFormat,
    fixDigits,
    engShift,
    baseRadix,
    isErrorActive
  ) {
    if (lcdState != EsLcdScreenState.CALC_LINE || isErrorActive) return@LaunchedEffect
    if (preserveAnswerOnNextCalcLine) {
      preserveAnswerOnNextCalcLine = false
      return@LaunchedEffect
    }
    if (rootMathContainer.nodes.isEmpty()) {
      hasCustomTextAnswer = false
      lastMatrixResult = null
      lastVectorResult = null
      if (currentMode == ClassWizMode.BASE_N) {
        displayResult = BaseNLogic.formatForRadix(lastBaseInt, baseRadix)
        answerMathTree = mathContainerOfText(displayResult)
      } else {
        activeDisplayNumeric = 0.0
        displayResult = "0"
        answerMathTree = mathContainerOfText("0")
      }
      return@LaunchedEffect
    }
    if (findFirstEmptySlot(rootMathContainer) != null) {
      hasCustomTextAnswer = false
      lastMatrixResult = null
      lastVectorResult = null
      displayResult = ""
      answerMathTree = MathContainer()
      return@LaunchedEffect
    }
    val raw = liveCanonicalKey.trim()
    if (raw.contains("Ran#") || raw.contains("RanInt")) {
      hasCustomTextAnswer = false
      lastMatrixResult = null
      lastVectorResult = null
      displayResult = ""
      answerMathTree = MathContainer()
      return@LaunchedEffect
    }
    runCatching {
      when (currentMode) {
        ClassWizMode.COMP -> {
          val statements = splitStatementsAtColon(rootMathContainer.nodes)
          val hasSpecialStmt = statements.any { st ->
            st.any { it is MathNode.TextNode && it.text == "=" } ||
              splitTopLevelTwoArgs(st, "Pol(") != null ||
              splitTopLevelTwoArgs(st, "Rec(") != null
          }
          if (hasSpecialStmt) {
            hasCustomTextAnswer = false
            displayResult = ""
            answerMathTree = MathContainer()
          } else {
            var latestRes = lastNumericAns
            for (stmtNodes in statements) {
              latestRes = evaluateNodeListAst(
                ownerContainer = rootMathContainer,
                nodes = stmtNodes,
                vars = variables + ("Ans" to latestRes),
                angleUnit = angleUnit
              )
            }
            lastMatrixResult = null
            lastVectorResult = null
            refreshCompDisplayFromNumeric(latestRes, isCommit = false)
          }
        }
        ClassWizMode.STAT -> {
          val res = StatPredictionLogic.evaluateStatExpression(
            rawExpr = raw,
            rows = statRows,
            model = statModel,
            vars = variables + ("Ans" to lastNumericAns),
            angleUnit = angleUnit
          )
          lastMatrixResult = null
          lastVectorResult = null
          refreshCompDisplayFromNumeric(res, isCommit = false)
        }
        ClassWizMode.CMPLX -> {
          val cVars = variables.mapValues { ComplexNum(it.value, 0.0) } + ("Ans" to lastComplexAns)
          val (z, effPolar) = evalComplexTreeAware(raw, cVars)
          lastMatrixResult = null
          lastVectorResult = null
          refreshComplexDisplay(z, polar = effPolar, isCommit = false)
        }
        ClassWizMode.BASE_N -> {
          val bVal = BaseNLogic.evaluateBaseN(raw, baseRadix)
          hasCustomTextAnswer = false
          displayResult = BaseNLogic.formatForRadix(bVal, baseRadix)
          answerMathTree = mathContainerOfText(displayResult)
        }
        ClassWizMode.MATRIX -> {
          val mat = evaluateMatrixExpression(raw, matrices, variables + ("Ans" to lastNumericAns), angleUnit)
          if (mat.rows == 1 && mat.cols == 1 && raw.startsWith("Det(")) {
            lastMatrixResult = null
            refreshCompDisplayFromNumeric(mat.get(0, 0), isCommit = false)
          } else {
            lastMatrixResult = mat
            hasCustomTextAnswer = true
            displayResult = "MatAns (${mat.rows}×${mat.cols})"
            answerMathTree = mathContainerOfText(displayResult)
          }
        }
        ClassWizMode.VECTOR -> {
          val vRes = evaluateVectorExpression(raw, vectors, variables + ("Ans" to lastNumericAns), angleUnit)
          if (vRes.second != null) {
            lastVectorResult = null
            refreshCompDisplayFromNumeric(vRes.second!!, isCommit = false)
          } else if (vRes.first != null) {
            lastVectorResult = vRes.first
            val vec = vRes.first!!
            hasCustomTextAnswer = true
            displayResult = "[ " + (0 until vec.dim).joinToString(", ") {
              formatDecimalCompact(vec.get(it), numberFormat, fixDigits)
            } + " ]"
            answerMathTree = mathContainerOfText(displayResult)
          }
        }
        else -> {
          val res = evaluateMathAst(rootMathContainer, variables + ("Ans" to lastNumericAns), angleUnit)
          lastMatrixResult = null
          lastVectorResult = null
          refreshCompDisplayFromNumeric(res, isCommit = false)
        }
      }
    }.onFailure {
      hasCustomTextAnswer = false
      lastMatrixResult = null
      lastVectorResult = null
      displayResult = ""
      answerMathTree = MathContainer()
    }
  }

  val accentCyan = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
  val orientation = rememberScreenOrientationState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .widthIn(max = orientation.wideMaxContentWidth)
      .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = if (orientation.isCompactLandscape) 4.dp else 6.dp)
      .testTag("classwiz_calculator_screen"),
    verticalArrangement = Arrangement.spacedBy(if (orientation.isCompactLandscape) 3.dp else 4.dp)
  ) {
    // 1. ULTRA-COMPACT TOP HEADER
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(if (orientation.isCompactLandscape) 36.dp else 42.dp)
        .padding(horizontal = 12.dp),
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
            .size(34.dp)
            .clip(CircleShape)
            .background(palette.nestedCardFill)
            .border(palette.nestedBorder, CircleShape)
            .testTag("classwiz_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Extras",
            tint = palette.textPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
        Text(
          text = "fx-570ES PLUS",
          style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = AppleSansFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary,
          maxLines = 1,
          softWrap = false,
          overflow = TextOverflow.Ellipsis
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            playCardHapticSound(context, localView)
            showHistoryModal = true
          },
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(palette.nestedCardFill)
            .border(palette.nestedBorder, CircleShape)
            .testTag("es_history_button")
        ) {
          Icon(Icons.Filled.History, "History", tint = accentCyan, modifier = Modifier.size(16.dp))
        }

        RecessedGlassPill(
          isDarkMode = isDarkMode,
          onClick = {
            playCardHapticSound(context, localView)
            showVariablesModal = true
          },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.testTag("es_vars_button")
        ) {
          Text(
            text = "[X]",
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentCyan,
            maxLines = 1,
            softWrap = false
          )
        }

        RecessedGlassPill(
          isDarkMode = isDarkMode,
          fillColor = accentCyan.copy(alpha = 0.16f),
          borderColor = accentCyan.copy(alpha = 0.55f),
          onClick = {
            playCardHapticSound(context, localView)
            lcdState = if (lcdState == EsLcdScreenState.MODE_MENU) EsLcdScreenState.CALC_LINE else EsLcdScreenState.MODE_MENU
          },
          contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
          modifier = Modifier.testTag("classwiz_top_mode_chip")
        ) {
          Text(
            text = currentMode.badge,
            fontFamily = JetBrainsMonoFamily,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentCyan,
            maxLines = 1,
            softWrap = false
          )
        }
      }
    }

    // 2. FULL-HEIGHT CALCULATOR BODY: LCD (0.30f) + UNIFORM 9-ROW KEYPAD (0.70f)
    BoxWithConstraints(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      val isLandscapeSplit = maxWidth > maxHeight
      val ultraCompactHeight = maxHeight < 420.dp
      val compactHeight = maxHeight < 580.dp
      val sciSp: TextUnit = when {
        ultraCompactHeight -> 10.sp
        compactHeight -> 11.sp
        else -> 12.sp
      }
      val numSp: TextUnit = when {
        ultraCompactHeight -> 12.5.sp
        compactHeight -> 14.sp
        else -> 15.5.sp
      }
      val subSp: TextUnit = if (ultraCompactHeight) 7.5.sp else 8.5.sp
      val subRowHeight: Dp = if (ultraCompactHeight) 11.dp else 15.dp
      val subGap: Dp = if (ultraCompactHeight) 1.dp else 2.dp
      val keypadRowGap: Dp = if (ultraCompactHeight) 2.dp else 3.dp
      val keypadPadV: Dp = if (ultraCompactHeight) 3.dp else 5.dp
      val dpadBtnSize: Dp = if (ultraCompactHeight) 22.dp else 26.dp
      val dpadIconSize: Dp = if (ultraCompactHeight) 14.dp else 16.dp
      val exprSp: TextUnit = when {
        ultraCompactHeight -> 13.5.sp
        compactHeight -> 14.sp
        else -> 16.5.sp
      }
      val resSp: TextUnit = when {
        ultraCompactHeight -> 18.sp
        compactHeight -> 20.sp
        else -> 23.sp
      }

      val bezelFill = if (isDarkMode) Color(0xFF0F172A).copy(alpha = 0.92f) else Color(0xFF1E293B).copy(alpha = 0.95f)
      val lcdBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F0E6), Color(0xFFDCE7DA))
      )
      val lcdInk = Color(0xFF0F172A)

      CompositionLocalProvider(
        LocalEsCalcSubRowHeight provides subRowHeight,
        LocalEsCalcSubGap provides subGap
      ) {
        Layout(
          modifier = Modifier.fillMaxSize(),
          content = {
            // ====================================================================
            // A. NATURAL-V.P.A.M. BACKLIT LCD DISPLAY
            // ====================================================================
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = bezelFill,
              border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.22f)),
              shadowElevation = 6.dp,
              modifier = Modifier.fillMaxSize()
            ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(6.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(brush = lcdBrush)
              .border(1.2.dp, Color(0xFF475569).copy(alpha = 0.55f), RoundedCornerShape(10.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("classwiz_lcd_viewport")
          ) {
            Column(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              // Top LCD Status Bar
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  EsLcdBadge("S", isShiftActive, Color(0xFFD97706), Color.White)
                  EsLcdBadge("A", isAlphaActive, Color(0xFFDC2626), Color.White)
                  if (isHypPending) EsLcdBadge("HYP", true, Color(0xFF2563EB), Color.White)
                  if (isStoPending) EsLcdBadge("STO", true, Color(0xFF059669), Color.White)
                  if (abs(variables["M"] ?: 0.0) > 1e-12) {
                    EsLcdBadge("M", true, Color(0xFF334155), Color.White)
                  }

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
                      .padding(horizontal = 5.dp, vertical = 1.dp)
                      .testTag("classwiz_angle_badge")
                  ) {
                    Text(angleUnit.badge, fontFamily = JetBrainsMonoFamily, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                  }

                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Color(0xFF0F172A).copy(alpha = 0.12f))
                      .clickable {
                        if (currentMode == ClassWizMode.STAT) lcdState = EsLcdScreenState.STAT_EDITOR
                        else if (currentMode == ClassWizMode.EQN) lcdState = EsLcdScreenState.EQN_COEFF_GRID
                        else if (currentMode == ClassWizMode.MATRIX) lcdState = EsLcdScreenState.MATRIX_EDITOR
                        else if (currentMode == ClassWizMode.VECTOR) lcdState = EsLcdScreenState.VECTOR_EDITOR
                        else if (currentMode == ClassWizMode.TABLE) lcdState = EsLcdScreenState.TABLE_PROMPT_FX
                      }
                      .padding(horizontal = 5.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = if (currentMode == ClassWizMode.BASE_N) "BASE-N [${baseRadix.shortBadge}]" else currentMode.badge,
                      fontFamily = JetBrainsMonoFamily,
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = lcdInk
                    )
                  }
                }

                Row(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = statusToast ?: (if (engShift != 0) "ENG" else "Math ▲▼"),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 150.dp)
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

              // Main In-LCD Viewport Content (Switches by EsLcdScreenState)
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth()
                  .padding(top = 2.dp)
              ) {
                when (lcdState) {
                  EsLcdScreenState.CMPLX_OPS -> {
                    InLcdTwoColumnMenu(
                      title = "CMPLX • Complex Operations (1–4)",
                      items = listOf(
                        "1" to "arg(",
                        "2" to "Conjg(",
                        "3" to "▶r∠θ",
                        "4" to "▶a+bi"
                      ),
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.MODE_MENU -> {
                    InLcdTwoColumnMenu(
                      title = "SELECT MODE (Press 1–8)",
                      items = ClassWizMode.entries.map { it.code.toString() to it.badge },
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.STAT_TYPE_MENU -> {
                    InLcdTwoColumnMenu(
                      title = "STAT • Select Regression Type (1–8)",
                      items = StatRegModel.entries.map { it.code.toString() to it.label },
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.STAT_EDITOR -> {
                    InLcdStatSpreadsheet(
                      model = statModel,
                      rows = statRows,
                      cursorRow = gridCursorRow,
                      cursorCol = gridCursorCol,
                      cellBuffer = cellEditBuffer,
                      onSelectCell = { r, c ->
                        gridCursorRow = r
                        gridCursorCol = c
                        cellEditBuffer = ""
                      }
                    )
                  }
                  EsLcdScreenState.STAT_SHIFT_MENU -> {
                    InLcdTwoColumnMenu(
                      title = "STAT MENU (${statModel.label})",
                      items = listOf(
                        "1" to "Type", "2" to "Data",
                        "3" to "Sum", "4" to "Var",
                        "5" to "Reg", "6" to "MinMax"
                      ),
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.STAT_SUM_MENU -> {
                    val opts = StatPredictionLogic.sumMenuOptions(statModel)
                    InLcdTwoColumnMenu(
                      title = "STAT • Sum (Press 1–${opts.size})",
                      items = opts.map { it.code.toString() to it.label },
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.STAT_VAR_MENU -> {
                    val opts = StatPredictionLogic.varMenuOptions(statModel)
                    InLcdTwoColumnMenu(
                      title = "STAT • Var (Press 1–${opts.size})",
                      items = opts.map { it.code.toString() to it.label },
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.STAT_REG_MENU -> {
                    val opts = StatPredictionLogic.regMenuOptions(statModel)
                    InLcdTwoColumnMenu(
                      title = "STAT • Reg ${statModel.label} (Press 1–${opts.size})",
                      items = opts.map { it.code.toString() to it.label },
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.STAT_PREDICT_PROMPT -> {
                    InLcdStatPredictPrompt(
                      model = statModel,
                      targetOp = statPredictTarget,
                      inputBuffer = cellEditBuffer
                    )
                  }
                  EsLcdScreenState.STAT_RESULTS -> {
                    Column(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.SpaceBetween
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text(
                          text = "STAT RESULT (${statModel.label})",
                          fontFamily = JetBrainsMonoFamily,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.ExtraBold,
                          color = Color(0xFF334155)
                        )
                        Text(
                          text = "▲▼ (${statResultIndex + 1}/${statResultsLines.size.coerceAtLeast(1)}) • AC:Back",
                          fontFamily = JetBrainsMonoFamily,
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF334155)
                        )
                      }
                      val slice = statResultsLines.drop(statResultIndex).take(3)
                      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        slice.forEach { line ->
                          Text(
                            text = line,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = lcdInk
                          )
                        }
                      }
                    }
                  }
                  EsLcdScreenState.EQN_TYPE_MENU -> {
                    InLcdTwoColumnMenu(
                      title = "EQN • Select Equation Type (1–4)",
                      items = EqnPresetType.entries.map { it.code.toString() to it.menuLabel },
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.EQN_COEFF_GRID -> {
                    InLcdEqnCoeffGrid(
                      preset = eqnPreset,
                      coeffs = eqnCoeffs,
                      cursorRow = gridCursorRow,
                      cursorCol = gridCursorCol,
                      cellBuffer = cellEditBuffer,
                      onSelectCell = { r, c ->
                        gridCursorRow = r
                        gridCursorCol = c
                        cellEditBuffer = ""
                      }
                    )
                  }
                  EsLcdScreenState.EQN_SOLUTIONS -> {
                    val curPair = eqnSolutions.getOrNull(eqnSolutionIdx)
                    val dispText = curPair?.let { (label, rawNum) ->
                      if (rawNum != null) {
                        val prefix = label.substringBefore("=").trim()
                        "$prefix = ${formatValueForS2D(rawNum)}"
                      } else label
                    } ?: "No Solution"

                    Column(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.SpaceBetween
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text(
                          text = eqnPreset.menuLabel,
                          fontFamily = JetBrainsMonoFamily,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.ExtraBold,
                          color = Color(0xFF334155)
                        )
                        Text(
                          text = "Sol ${eqnSolutionIdx + 1}/${eqnSolutions.size} (REPLAY ▲▼ / S⇔D)",
                          fontFamily = JetBrainsMonoFamily,
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF334155)
                        )
                      }
                      Text(
                        text = dispText,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = lcdInk,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth().testTag("classwiz_lcd_result")
                      )
                    }
                  }
                  EsLcdScreenState.MATRIX_SELECT_MENU -> {
                    InLcdTwoColumnMenu(
                      title = if (matMenuForDim) "MATRIX • Select Matrix to Define (1–3)" else "MATRIX • Edit Matrix Data (1–3)",
                      items = listOf("1" to "MatA", "2" to "MatB", "3" to "MatC"),
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.MATRIX_DIM_MENU -> {
                    InLcdTwoColumnMenu(
                      title = "$activeMatKey (m×n)? Select Dimension (1–6)",
                      items = MATRIX_DIM_PRESETS.map { it.first.toString() to it.second },
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.MATRIX_EDITOR -> {
                    val mat = matrices[activeMatKey] ?: ClassWizMatrixData()
                    val cellTexts = matCellTexts[activeMatKey] ?: List(mat.rows * mat.cols) { "" }
                    InLcdMatrixVectorGrid(
                      title = activeMatKey,
                      rows = mat.rows,
                      cols = mat.cols,
                      cellTexts = cellTexts,
                      cursorRow = gridCursorRow,
                      cursorCol = gridCursorCol,
                      cellBuffer = cellEditBuffer,
                      hintRight = "AC:Calc  SHIFT+4:Mat",
                      onSelectCell = { r, c ->
                        gridCursorRow = r
                        gridCursorCol = c
                        cellEditBuffer = ""
                      }
                    )
                  }
                  EsLcdScreenState.MATRIX_SHIFT_MENU -> {
                    InLcdTwoColumnMenu(
                      title = "MATRIX MENU (Press 1–8)",
                      items = listOf(
                        "1" to "Dim", "2" to "Data",
                        "3" to "MatA", "4" to "MatB",
                        "5" to "MatC", "6" to "Det(",
                        "7" to "Trn(", "8" to "Mat⁻¹"
                      ),
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.VECTOR_SELECT_MENU -> {
                    InLcdTwoColumnMenu(
                      title = if (vctMenuForDim) "VECTOR • Select Vector to Define (1–3)" else "VECTOR • Edit Vector Data (1–3)",
                      items = listOf("1" to "VctA", "2" to "VctB", "3" to "VctC"),
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.VECTOR_DIM_MENU -> {
                    InLcdTwoColumnMenu(
                      title = "$activeVctKey (m)? Select Dimension (1–2)",
                      items = listOf("1" to "3D Vector", "2" to "2D Vector"),
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.VECTOR_EDITOR -> {
                    val vct = vectors[activeVctKey] ?: ClassWizVectorData()
                    val cellTexts = vctCellTexts[activeVctKey] ?: List(vct.dim) { "" }
                    InLcdMatrixVectorGrid(
                      title = activeVctKey,
                      rows = 1,
                      cols = vct.dim,
                      cellTexts = cellTexts,
                      cursorRow = 0,
                      cursorCol = gridCursorCol,
                      cellBuffer = cellEditBuffer,
                      hintRight = "AC:Calc  SHIFT+5:Vct",
                      onSelectCell = { _, c ->
                        gridCursorCol = c
                        cellEditBuffer = ""
                      }
                    )
                  }
                  EsLcdScreenState.VECTOR_SHIFT_MENU -> {
                    InLcdTwoColumnMenu(
                      title = "VECTOR MENU (Press 1–8)",
                      items = listOf(
                        "1" to "Dim", "2" to "Data",
                        "3" to "VctA", "4" to "VctB",
                        "5" to "VctC", "6" to "Dot(•)",
                        "7" to "Abs(", "8" to "Angle("
                      ),
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.TABLE_PROMPT_FX -> {
                    val blinkOn = rememberLcdBlinkOn()
                    val hScroll = rememberScrollState()
                    val vScroll = rememberScrollState()
                    Column(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = "TABLE • Enter Function f(X) & Press =",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF334155)
                      )
                      Row(
                        modifier = Modifier
                          .weight(1f)
                          .fillMaxWidth()
                          .verticalScroll(vScroll)
                          .horizontalScroll(hScroll),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "f(X) = ",
                          fontFamily = JetBrainsMonoFamily,
                          fontSize = exprSp * 0.92f,
                          fontWeight = FontWeight.Bold,
                          color = lcdInk
                        )
                        MathContainerView(
                          container = rootMathContainer,
                          cursor = cursorLocation,
                          blinkOn = blinkOn,
                          fontSize = exprSp * 0.92f,
                          isRoot = true,
                          inkColor = lcdInk,
                          onSelectCursor = { loc ->
                            clearErrorState()
                            cursorLocation = loc
                          }
                        )
                      }
                      Text(
                        text = "Tip: Use ALPHA + ) for X",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        color = Color(0xFF334155)
                      )
                    }
                  }
                  EsLcdScreenState.TABLE_PROMPT_GX -> {
                    val blinkOn = rememberLcdBlinkOn()
                    val hScroll = rememberScrollState()
                    val vScroll = rememberScrollState()
                    Column(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = "TABLE • Optional g(X) (Press = to skip)",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF334155),
                        maxLines = 1
                      )
                      Text(
                        text = "f(X) = ${toNaturalDisplayText(tableFx)}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        maxLines = 1
                      )
                      Row(
                        modifier = Modifier
                          .weight(1f)
                          .fillMaxWidth()
                          .verticalScroll(vScroll)
                          .horizontalScroll(hScroll),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "g(X) = ",
                          fontFamily = JetBrainsMonoFamily,
                          fontSize = exprSp * 0.90f,
                          fontWeight = FontWeight.Bold,
                          color = lcdInk
                        )
                        MathContainerView(
                          container = rootMathContainer,
                          cursor = cursorLocation,
                          blinkOn = blinkOn,
                          fontSize = exprSp * 0.90f,
                          isRoot = true,
                          inkColor = lcdInk,
                          onSelectCursor = { loc ->
                            clearErrorState()
                            cursorLocation = loc
                          }
                        )
                      }
                    }
                  }
                  EsLcdScreenState.TABLE_PROMPT_START,
                  EsLcdScreenState.TABLE_PROMPT_END,
                  EsLcdScreenState.TABLE_PROMPT_STEP -> {
                    val blinkOn = rememberLcdBlinkOn()
                    val cursorGlyph = if (blinkOn) "│" else " "
                    val promptLabel = when (lcdState) {
                      EsLcdScreenState.TABLE_PROMPT_START -> "Start?"
                      EsLcdScreenState.TABLE_PROMPT_END -> "End?"
                      else -> "Step?"
                    }
                    Column(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = if (tableGx.isNotBlank()) "f(X)=${toNaturalDisplayText(tableFx)}  g(X)=${toNaturalDisplayText(tableGx)}" else "f(X) = ${toNaturalDisplayText(tableFx)}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = promptLabel,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = lcdInk
                      )
                      Text(
                        text = "${cellEditBuffer}$cursorGlyph",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = lcdInk,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                      )
                    }
                  }
                  EsLcdScreenState.TABLE_VIEW -> {
                    val hasGx = tableGx.isNotBlank()
                    val listState = rememberLazyListState()
                    LaunchedEffect(tableScrollIdx, tableRows.size) {
                      if (tableRows.isNotEmpty()) {
                        listState.animateScrollToItem(tableScrollIdx.coerceIn(0, tableRows.lastIndex))
                      }
                    }
                    Column(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.SpaceBetween
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .background(Color(0xFFCBD5E1).copy(alpha = 0.65f))
                          .padding(horizontal = 4.dp, vertical = 1.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text("ROW", fontFamily = JetBrainsMonoFamily, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = lcdInk, modifier = Modifier.width(30.dp))
                        Text("X", fontFamily = JetBrainsMonoFamily, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = lcdInk, modifier = Modifier.weight(0.9f))
                        Text("f(X)", fontFamily = JetBrainsMonoFamily, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = lcdInk, textAlign = TextAlign.End, modifier = Modifier.weight(1.1f))
                        if (hasGx) {
                          Text("g(X)", fontFamily = JetBrainsMonoFamily, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = lcdInk, textAlign = TextAlign.End, modifier = Modifier.weight(1.1f))
                        }
                      }
                      LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                      ) {
                        itemsIndexed(tableRows) { idx, rowItem ->
                          Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 1.dp)) {
                            Text("${idx + 1}:", fontFamily = JetBrainsMonoFamily, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.width(30.dp))
                            Text(formatDecimalCompact(rowItem.x), fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = lcdInk, modifier = Modifier.weight(0.9f))
                            Text(rowItem.fxText, fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = lcdInk, textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.weight(1.1f))
                            if (hasGx) {
                              Text(rowItem.gxText ?: "", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = lcdInk, textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.weight(1.1f))
                            }
                          }
                        }
                      }
                    }
                  }
                  EsLcdScreenState.CLR_MENU -> {
                    InLcdTwoColumnMenu(
                      title = "CLEAR? (Press 1–3, AC:Cancel)",
                      items = listOf(
                        "1" to "Setup",
                        "2" to "Memory",
                        "3" to "All"
                      ),
                      onSelectIndex = { idx -> handleMenuChoice(idx + 1) }
                    )
                  }
                  EsLcdScreenState.CONST_PROMPT -> {
                    InLcdCodeDirectory(
                      title = "CONST [01-40]",
                      codeBuffer = codePromptBuffer,
                      entries = ES_PLUS_CONSTANTS.map { it.code to "${it.symbol} (${it.name})" },
                      pageIndex = codePromptPage,
                      onSelectCode = { code ->
                        codePromptBuffer = code
                        executeEvaluation()
                      }
                    )
                  }
                  EsLcdScreenState.CONV_PROMPT -> {
                    InLcdCodeDirectory(
                      title = "CONV [01-40]",
                      codeBuffer = codePromptBuffer,
                      entries = ES_PLUS_CONVERSIONS.map { it.code to it.label },
                      pageIndex = codePromptPage,
                      onSelectCode = { code ->
                        codePromptBuffer = code
                        executeEvaluation()
                      }
                    )
                  }
                  EsLcdScreenState.CALC_LINE -> {
                    // Standard 2D Natural-V.P.A.M. (MathO) Expression + Output (or Matrix/Vector result grid)
                    Column(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.SpaceBetween
                    ) {
                      val blinkOn = rememberLcdBlinkOn()
                      val hScroll = rememberScrollState()
                      val vScroll = rememberScrollState()
                      LaunchedEffect(rootMathContainer.nodes.size, cursorLocation) {
                        if (cursorLocation.container === rootMathContainer &&
                          cursorLocation.index == rootMathContainer.nodes.size
                        ) {
                          hScroll.animateScrollTo(hScroll.maxValue)
                        }
                      }
                      Box(
                        modifier = Modifier
                          .weight(1f)
                          .fillMaxWidth()
                          .verticalScroll(vScroll)
                          .horizontalScroll(hScroll),
                        contentAlignment = Alignment.CenterStart
                      ) {
                        MathContainerView(
                          container = rootMathContainer,
                          cursor = cursorLocation,
                          blinkOn = blinkOn,
                          fontSize = exprSp,
                          isRoot = true,
                          inkColor = lcdInk,
                          onSelectCursor = { loc ->
                            clearErrorState()
                            cursorLocation = loc
                          },
                          modifier = Modifier.testTag("classwiz_lcd_expression")
                        )
                      }

                      if (currentMode == ClassWizMode.MATRIX && lastMatrixResult != null) {
                        val m = lastMatrixResult!!
                        Column(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalAlignment = Alignment.End
                        ) {
                          for (r in 0 until m.rows) {
                            val rowTxt = (0 until m.cols).joinToString("  ") { c ->
                              formatValueForS2D(m.get(r, c))
                            }
                            Text(
                              text = "[ $rowTxt ]",
                              fontFamily = JetBrainsMonoFamily,
                              fontSize = 11.5.sp,
                              fontWeight = FontWeight.ExtraBold,
                              color = lcdInk
                            )
                          }
                        }
                      } else if (currentMode == ClassWizMode.BASE_N) {
                        Column(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalAlignment = Alignment.End
                        ) {
                          Text(
                            text = "${baseRadix.label}: $displayResult",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = resSp * 0.85f,
                            fontWeight = FontWeight.ExtraBold,
                            color = lcdInk,
                            maxLines = 1,
                            modifier = Modifier.testTag("classwiz_lcd_result")
                          )
                          Text(
                            text = "DEC:${BaseNLogic.formatForRadix(lastBaseInt, BaseNRadix.DEC)}  HEX:${BaseNLogic.formatForRadix(lastBaseInt, BaseNRadix.HEX)}  BIN:${BaseNLogic.formatForRadix(lastBaseInt, BaseNRadix.BIN)}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 8.8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155),
                            maxLines = 1
                          )
                        }
                      } else if (isErrorActive || hasCustomTextAnswer) {
                        Text(
                          text = displayResult,
                          style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = resSp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                          ),
                          color = lcdInk,
                          textAlign = TextAlign.End,
                          maxLines = 1,
                          softWrap = false,
                          overflow = TextOverflow.Ellipsis,
                          modifier = Modifier
                            .fillMaxWidth()
                            .testTag("classwiz_lcd_result")
                        )
                      } else {
                        val resHScroll = rememberScrollState()
                        Row(
                          modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(resHScroll)
                            .testTag("classwiz_lcd_result"),
                          horizontalArrangement = Arrangement.End,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          MathContainerView(
                            container = answerMathTree,
                            cursor = CursorLocation(answerMathTree, 0),
                            blinkOn = false,
                            fontSize = resSp * 0.84f,
                            isRoot = true,
                            isEditable = false,
                            inkColor = lcdInk
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

        // ====================================================================
        // B. UNIFORM 9-ROW CASIO fx-570ES PLUS KEYPAD
        // ====================================================================
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = if (isDarkMode) Color(0xFF0F172A).copy(alpha = 0.90f) else Color(0xFF1E293B).copy(alpha = 0.95f),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
          modifier = Modifier.fillMaxSize()
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 6.dp, vertical = keypadPadV),
            verticalArrangement = Arrangement.spacedBy(keypadRowGap)
          ) {
            // UPPER CONTROL CLUSTER (Rows 1 & 2 + Symmetric Circular REPLAY Pad): weight(2.15f)
            Row(
              modifier = Modifier
                .weight(2.15f)
                .fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Left Column (weight 2f): Row 1 [SHIFT | ALPHA], Row 2 [CALC | ∫dx]
              Column(
                modifier = Modifier
                  .weight(2f)
                  .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Row(
                  modifier = Modifier.weight(1f).fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  CalcKeyCell(
                    primary = "SHIFT",
                    shift = "",
                    alpha = "",
                    containerColor = if (isShiftActive) Color(0xFFD97706) else Color(0xFF334155),
                    contentColor = if (isShiftActive) Color.White else Color(0xFFF59E0B),
                    primarySp = sciSp,
                    subSp = subSp,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("cw_key_shift")
                  ) {
                    isShiftActive = !isShiftActive
                    if (isShiftActive) isAlphaActive = false
                  }
                  CalcKeyCell(
                    primary = "ALPHA",
                    shift = "",
                    alpha = "",
                    containerColor = if (isAlphaActive) Color(0xFFDC2626) else Color(0xFF334155),
                    contentColor = if (isAlphaActive) Color.White else Color(0xFFF43F5E),
                    primarySp = sciSp,
                    subSp = subSp,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("cw_key_alpha")
                  ) {
                    isAlphaActive = !isAlphaActive
                    if (isAlphaActive) isShiftActive = false
                  }
                }
                Row(
                  modifier = Modifier.weight(1f).fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  CalcKeyCell(
                    primary = "CALC",
                    shift = "SOLVE",
                    alpha = "=",
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White,
                    primarySp = sciSp,
                    subSp = subSp,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("cw_key_calc")
                  ) {
                    when {
                      isShiftActive -> {
                        isShiftActive = false
                        executeEvaluation()
                      }
                      isAlphaActive -> insertToken("=")
                      else -> {
                        val varsSet = linkedSetOf<String>()
                        collectVariablesInTree(rootMathContainer, varsSet)
                        val varsFound = listOf("A", "B", "C", "D", "E", "F", "X", "Y", "M")
                          .filter { it in varsSet }
                        if (varsFound.isEmpty()) executeEvaluation()
                        else {
                          calcPromptVars = varsFound
                          calcPromptDrafts = varsFound.associateWith { formatDecimalCompact(variables[it] ?: 0.0) }
                          showCalcPrompt = true
                        }
                      }
                    }
                  }
                  CalcKeyCell(
                    primary = "∫dx",
                    shift = "d/dx",
                    alpha = ":",
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White,
                    primarySp = sciSp,
                    subSp = subSp,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("cw_key_integral")
                  ) {
                    when {
                      isAlphaActive -> insertToken(":")
                      isShiftActive -> insertToken("deriv(")
                      else -> insertToken("integral(")
                    }
                  }
                }
              }

              // Center Column (weight 1.35f): True Circular REPLAY D-Pad (aspectRatio(1f))
              Box(
                modifier = Modifier
                  .weight(1.35f)
                  .fillMaxHeight()
                  .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
              ) {
                Surface(
                  shape = CircleShape,
                  color = Color(0xFF334155),
                  border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.28f)),
                  shadowElevation = 4.dp,
                  modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                ) {
                  Box(modifier = Modifier.fillMaxSize()) {
                    IconButton(
                      onClick = { handleReplayDpad("UP") },
                      modifier = Modifier.size(dpadBtnSize).align(Alignment.TopCenter).testTag("cw_dpad_up")
                    ) {
                      Icon(Icons.Filled.KeyboardArrowUp, "Replay Up", tint = Color.White, modifier = Modifier.size(dpadIconSize))
                    }
                    IconButton(
                      onClick = { handleReplayDpad("DOWN") },
                      modifier = Modifier.size(dpadBtnSize).align(Alignment.BottomCenter).testTag("cw_dpad_down")
                    ) {
                      Icon(Icons.Filled.KeyboardArrowDown, "Replay Down", tint = Color.White, modifier = Modifier.size(dpadIconSize))
                    }
                    IconButton(
                      onClick = { handleReplayDpad("LEFT") },
                      modifier = Modifier.size(dpadBtnSize).align(Alignment.CenterStart).testTag("cw_dpad_left")
                    ) {
                      Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Replay Left", tint = Color.White, modifier = Modifier.size(dpadIconSize))
                    }
                    IconButton(
                      onClick = { handleReplayDpad("RIGHT") },
                      modifier = Modifier.size(dpadBtnSize).align(Alignment.CenterEnd).testTag("cw_dpad_right")
                    ) {
                      Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Replay Right", tint = Color.White, modifier = Modifier.size(dpadIconSize))
                    }
                    Text(
                      text = "REPLAY",
                      fontSize = if (ultraCompactHeight) 5.5.sp else 6.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFCBD5E1),
                      maxLines = 1,
                      softWrap = false,
                      modifier = Modifier.align(Alignment.Center)
                    )
                  }
                }
              }

              // Right Column (weight 2f): Row 1 [MODE | ON], Row 2 [x⁻¹ | log_■(□)]
              Column(
                modifier = Modifier
                  .weight(2f)
                  .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Row(
                  modifier = Modifier.weight(1f).fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  CalcKeyCell(
                    primary = "MODE",
                    shift = "SETUP",
                    alpha = "",
                    containerColor = if (lcdState == EsLcdScreenState.MODE_MENU) Color(0xFF0284C7) else Color(0xFF334155),
                    contentColor = Color.White,
                    primarySp = sciSp,
                    subSp = subSp,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("cw_key_menu")
                  ) {
                    if (isShiftActive) {
                      isShiftActive = false
                      showSetupModal = true
                    } else {
                      lcdState = if (lcdState == EsLcdScreenState.MODE_MENU) EsLcdScreenState.CALC_LINE else EsLcdScreenState.MODE_MENU
                    }
                  }
                  CalcKeyCell(
                    primary = "ON",
                    shift = "",
                    alpha = "",
                    containerColor = Color(0xFF334155),
                    contentColor = Color(0xFF38BDF8),
                    primarySp = sciSp,
                    subSp = subSp,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("cw_key_on")
                  ) {
                    enterMode(currentMode)
                  }
                }
                Row(
                  modifier = Modifier.weight(1f).fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  CalcKeyCell(
                    primary = "x⁻¹",
                    shift = "x!",
                    alpha = "",
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White,
                    primarySp = sciSp,
                    subSp = subSp,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("cw_key_inv")
                  ) {
                    if (isShiftActive) insertToken("!") else insertToken("⁻¹")
                  }
                  CalcKeyCell(
                    primary = "log_■(□)",
                    shift = "Σ",
                    alpha = "",
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White,
                    primarySp = sciSp * 0.9f,
                    subSp = subSp,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("cw_key_logbase")
                  ) {
                    if (isShiftActive) insertToken("sum(") else insertToken("logBase(")
                  }
                }
              }
            }

            // ROW 3 (weight 1f): ■/□ | √■ | x² | x^■ | log | ln
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
              CalcKeyCell("■/□", "a b/c", "", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_frac")) {
                if (isShiftActive) {
                  playCardHapticSound(context, localView)
                  clearErrorState()
                  lastAutoStatExpr = null
                  cursorLocation = insertSmartMixedFraction(rootMathContainer, cursorLocation)
                  isShiftActive = false
                  isAlphaActive = false
                  isHypPending = false
                } else insertToken("/")
              }
              CalcKeyCell("√■", "³√■", "", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_sqrt")) {
                if (isShiftActive) insertToken("cbrt(") else insertToken("sqrt(")
              }
              CalcKeyCell("x²", "x³", if (currentMode == ClassWizMode.BASE_N) "DEC" else "", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_sq")) {
                if (currentMode == ClassWizMode.BASE_N) {
                  baseRadix = BaseNRadix.DEC
                  displayResult = BaseNLogic.formatForRadix(lastBaseInt, baseRadix)
                } else if (isShiftActive) insertToken("³") else insertToken("²")
              }
              CalcKeyCell("x^■", "ˣ√■", if (currentMode == ClassWizMode.BASE_N) "HEX" else "", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_pow")) {
                if (currentMode == ClassWizMode.BASE_N) {
                  baseRadix = BaseNRadix.HEX
                  displayResult = BaseNLogic.formatForRadix(lastBaseInt, baseRadix)
                } else if (isShiftActive) insertToken("nroot(") else insertToken("^")
              }
              CalcKeyCell("log", "10^x", if (currentMode == ClassWizMode.BASE_N) "BIN" else "", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_log")) {
                if (currentMode == ClassWizMode.BASE_N) {
                  baseRadix = BaseNRadix.BIN
                  displayResult = BaseNLogic.formatForRadix(lastBaseInt, baseRadix)
                } else if (isShiftActive) insertToken("10^") else insertToken("log(")
              }
              CalcKeyCell("ln", "e^x", if (currentMode == ClassWizMode.BASE_N) "OCT" else "", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_ln")) {
                if (currentMode == ClassWizMode.BASE_N) {
                  baseRadix = BaseNRadix.OCT
                  displayResult = BaseNLogic.formatForRadix(lastBaseInt, baseRadix)
                } else if (isShiftActive) insertToken("exp(") else insertToken("ln(")
              }
            }

            // ROW 4 (weight 1f): (-) | ° ' " | hyp | sin | cos | tan
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
              CalcKeyCell("(-)", "∠", "A", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_neg")) {
                when {
                  isAlphaActive || (currentMode == ClassWizMode.BASE_N && baseRadix == BaseNRadix.HEX) -> {
                    if (currentMode == ClassWizMode.BASE_N) insertToken("A") else handleVariableKey("A")
                  }
                  isShiftActive -> insertToken("∠")
                  else -> insertToken("-")
                }
              }
              CalcKeyCell("° ' \"", "←", "B", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_dms")) {
                when {
                  isAlphaActive || (currentMode == ClassWizMode.BASE_N && baseRadix == BaseNRadix.HEX) -> {
                    if (currentMode == ClassWizMode.BASE_N) insertToken("B") else handleVariableKey("B")
                  }
                  isShiftActive -> {
                    isShiftActive = false
                    isDmsOutput = !isDmsOutput
                    val targetVal = if (rootMathContainer.nodes.isEmpty() && activeDisplayNumeric == 0.0) lastNumericAns else activeDisplayNumeric
                    refreshCompDisplayFromNumeric(targetVal, isCommit = false)
                  }
                  else -> {
                    if (rootMathContainer.nodes.isEmpty() && (lastNumericAns != 0.0 || activeDisplayNumeric != 0.0)) {
                      isDmsOutput = !isDmsOutput
                      val targetVal = if (activeDisplayNumeric != 0.0) activeDisplayNumeric else lastNumericAns
                      refreshCompDisplayFromNumeric(targetVal, isCommit = false)
                    } else insertToken("°")
                  }
                }
              }
              CalcKeyCell("hyp", "", "C", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_hyp")) {
                if (isAlphaActive || (currentMode == ClassWizMode.BASE_N && baseRadix == BaseNRadix.HEX)) {
                  if (currentMode == ClassWizMode.BASE_N) insertToken("C") else handleVariableKey("C")
                } else isHypPending = !isHypPending
              }
              CalcKeyCell("sin", "sin⁻¹", "D", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_sin")) {
                when {
                  isAlphaActive || (currentMode == ClassWizMode.BASE_N && baseRadix == BaseNRadix.HEX) -> {
                    if (currentMode == ClassWizMode.BASE_N) insertToken("D") else handleVariableKey("D")
                  }
                  isHypPending && isShiftActive -> insertToken("asinh(")
                  isHypPending -> insertToken("sinh(")
                  isShiftActive -> insertToken("asin(")
                  else -> insertToken("sin(")
                }
              }
              CalcKeyCell("cos", "cos⁻¹", "E", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_cos")) {
                when {
                  isAlphaActive || (currentMode == ClassWizMode.BASE_N && baseRadix == BaseNRadix.HEX) -> {
                    if (currentMode == ClassWizMode.BASE_N) insertToken("E") else handleVariableKey("E")
                  }
                  isHypPending && isShiftActive -> insertToken("acosh(")
                  isHypPending -> insertToken("cosh(")
                  isShiftActive -> insertToken("acos(")
                  else -> insertToken("cos(")
                }
              }
              CalcKeyCell("tan", "tan⁻¹", "F", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_tan")) {
                when {
                  isAlphaActive || (currentMode == ClassWizMode.BASE_N && baseRadix == BaseNRadix.HEX) -> {
                    if (currentMode == ClassWizMode.BASE_N) insertToken("F") else handleVariableKey("F")
                  }
                  isHypPending && isShiftActive -> insertToken("atanh(")
                  isHypPending -> insertToken("tanh(")
                  isShiftActive -> insertToken("atan(")
                  else -> insertToken("tan(")
                }
              }
            }

            // ROW 5 (weight 1f): RCL | ENG | ( | ) | S⇔D | M+
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
              CalcKeyCell("RCL", "STO", "", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_sto")) {
                if (isShiftActive) {
                  isShiftActive = false
                  isStoPending = !isStoPending
                  statusToast = if (isStoPending) "Tap A–F, X, Y, M" else null
                } else showVariablesModal = true
              }
              CalcKeyCell("ENG", "←", "i", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_eng")) {
                when {
                  isAlphaActive -> insertToken("i")
                  isShiftActive -> {
                    isShiftActive = false
                    engShift -= 1
                    isExactMode = false
                    val targetVal = if (rootMathContainer.nodes.isEmpty() && activeDisplayNumeric == 0.0) lastNumericAns else activeDisplayNumeric
                    refreshCompDisplayFromNumeric(targetVal, isCommit = false)
                  }
                  else -> {
                    engShift += 1
                    isExactMode = false
                    val targetVal = if (rootMathContainer.nodes.isEmpty() && activeDisplayNumeric == 0.0) lastNumericAns else activeDisplayNumeric
                    refreshCompDisplayFromNumeric(targetVal, isCommit = false)
                  }
                }
              }
              CalcKeyCell("(", "%", "", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_lparen")) {
                if (isShiftActive) insertToken("%") else insertToken("(")
              }
              CalcKeyCell(")", ",", "X", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_rparen")) {
                when {
                  isAlphaActive -> handleVariableKey("X")
                  isShiftActive -> insertToken(",")
                  else -> insertToken(")")
                }
              }
              CalcKeyCell("S⇔D", "a b/c⇔d/c", "Y", Color(0xFF1E293B), Color.White, sciSp, subSp * 0.88f, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_sd")) {
                when {
                  isAlphaActive -> handleVariableKey("Y")
                  isShiftActive -> {
                    isMixedFraction = !isMixedFraction
                    isExactMode = true
                    isShiftActive = false
                    if (currentMode == ClassWizMode.CMPLX) {
                      refreshComplexDisplay(activeDisplayComplex, polar = activeComplexPolar, isCommit = false)
                    } else {
                      refreshCompDisplayFromNumeric(activeDisplayNumeric, isCommit = false)
                    }
                  }
                  else -> {
                    isExactMode = !isExactMode
                    engShift = 0
                    if (currentMode == ClassWizMode.CMPLX) {
                      refreshComplexDisplay(activeDisplayComplex, polar = activeComplexPolar, isCommit = false)
                    } else {
                      refreshCompDisplayFromNumeric(activeDisplayNumeric, isCommit = false)
                    }
                  }
                }
              }
              CalcKeyCell("M+", "M-", "M", Color(0xFF1E293B), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_mplus")) {
                when {
                  isAlphaActive -> handleVariableKey("M")
                  isShiftActive -> {
                    isShiftActive = false
                    val cur = if (rootMathContainer.nodes.isNotEmpty()) {
                      runCatching { evaluateMathAst(rootMathContainer, variables + ("Ans" to lastNumericAns), angleUnit) }.getOrElse { lastNumericAns }
                    } else lastNumericAns
                    variables["M"] = cleanZero((variables["M"] ?: 0.0) - cur)
                    statusToast = "M = ${formatDecimalCompact(variables["M"] ?: 0.0)}"
                  }
                  else -> {
                    val cur = if (rootMathContainer.nodes.isNotEmpty()) {
                      runCatching { evaluateMathAst(rootMathContainer, variables + ("Ans" to lastNumericAns), angleUnit) }.getOrElse { lastNumericAns }
                    } else lastNumericAns
                    variables["M"] = cleanZero((variables["M"] ?: 0.0) + cur)
                    statusToast = "M = ${formatDecimalCompact(variables["M"] ?: 0.0)}"
                  }
                }
              }
            }

            // ROW 6 (weight 1f): 7(CONST) | 8(CONV) | 9(CLR) | DEL(INS) | AC(OFF)
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
              CalcKeyCell("7", "CONST", "", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_7")) {
                if (isShiftActive) {
                  isShiftActive = false
                  codePromptBuffer = ""
                  codePromptPage = 0
                  lcdState = EsLcdScreenState.CONST_PROMPT
                } else insertToken("7")
              }
              CalcKeyCell("8", "CONV", "", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_8")) {
                if (isShiftActive) {
                  isShiftActive = false
                  codePromptBuffer = ""
                  codePromptPage = 0
                  lcdState = EsLcdScreenState.CONV_PROMPT
                } else insertToken("8")
              }
              CalcKeyCell("9", "CLR", "", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_9")) {
                if (isShiftActive) {
                  isShiftActive = false
                  lcdState = EsLcdScreenState.CLR_MENU
                } else insertToken("9")
              }
              CalcKeyCell("DEL", "INS", "", Color(0xFFEA580C), Color.White, numSp * 0.92f, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_del")) {
                deleteAtCursor()
              }
              CalcKeyCell("AC", "OFF", "", Color(0xFFDC2626), Color.White, numSp * 0.92f, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_ac")) {
                handleAcButton()
              }
            }

            // ROW 7 (weight 1f): 4(MATRIX) | 5(VECTOR) | 6 | ×(nPr) | ÷(nCr)
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
              CalcKeyCell("4", "MATRIX", "", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_4")) {
                if (isShiftActive) {
                  isShiftActive = false
                  currentMode = ClassWizMode.MATRIX
                  lcdState = EsLcdScreenState.MATRIX_SHIFT_MENU
                } else insertToken("4")
              }
              CalcKeyCell("5", "VECTOR", "", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_5")) {
                if (isShiftActive) {
                  isShiftActive = false
                  currentMode = ClassWizMode.VECTOR
                  lcdState = EsLcdScreenState.VECTOR_SHIFT_MENU
                } else insertToken("5")
              }
              CalcKeyCell("6", "", "", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_6")) {
                insertToken("6")
              }
              CalcKeyCell("×", "nPr", "", Color(0xFF334155), Color.White, numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_mul")) {
                if (isShiftActive) insertToken("P") else insertToken("×")
              }
              CalcKeyCell("÷", "nCr", "", Color(0xFF334155), Color.White, numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_div")) {
                if (isShiftActive) insertToken("C") else insertToken("÷")
              }
            }

            // ROW 8 (weight 1f): 1(STAT) | 2(CMPLX) | 3(BASE) | +(Pol() | -(Rec()
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
              CalcKeyCell("1", "STAT", "", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_1")) {
                if (isShiftActive) {
                  isShiftActive = false
                  currentMode = ClassWizMode.STAT
                  lcdState = EsLcdScreenState.STAT_SHIFT_MENU
                } else insertToken("1")
              }
              CalcKeyCell("2", "CMPLX", "", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_2")) {
                if (isShiftActive) {
                  isShiftActive = false
                  currentMode = ClassWizMode.CMPLX
                  lcdState = EsLcdScreenState.CMPLX_OPS
                } else insertToken("2")
              }
              CalcKeyCell("3", "BASE", "", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_3")) {
                if (isShiftActive) {
                  isShiftActive = false
                  enterMode(ClassWizMode.BASE_N)
                } else insertToken("3")
              }
              CalcKeyCell("+", "Pol(", "", Color(0xFF334155), Color.White, numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_add")) {
                if (isShiftActive) insertToken("Pol(") else insertToken("+")
              }
              CalcKeyCell("-", "Rec(", "", Color(0xFF334155), Color.White, numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_sub")) {
                if (isShiftActive) insertToken("Rec(") else insertToken("-")
              }
            }

            // ROW 9 (weight 1f): 0(Rnd) | .(Ran#/RanInt) | ×10ˣ(π/e) | Ans(DRG►) | =(≈)
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
              CalcKeyCell("0", "Rnd", "", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_0")) {
                if (isShiftActive) insertToken("Rnd(") else insertToken("0")
              }
              CalcKeyCell(".", "Ran#", "RanInt", Color(0xFFF8FAFC), Color(0xFF0F172A), numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_dot")) {
                when {
                  isAlphaActive -> insertToken("RanInt(1,100)")
                  isShiftActive -> insertToken("Ran#")
                  else -> insertToken(".")
                }
              }
              CalcKeyCell("×10ˣ", "π", "e", Color(0xFF334155), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_exp10")) {
                when {
                  isAlphaActive -> insertToken("e")
                  isShiftActive -> insertToken("π")
                  else -> insertToken("×10^")
                }
              }
              CalcKeyCell("Ans", "DRG►", "", Color(0xFF334155), Color.White, sciSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_ans")) {
                if (isShiftActive) {
                  isShiftActive = false
                  showDrgModal = true
                } else insertToken("Ans")
              }
              CalcKeyCell("=", "≈", "", Color(0xFF0284C7), Color.White, numSp, subSp, Modifier.weight(1f).fillMaxHeight().testTag("cw_key_eq")) {
                executeEvaluation(forceApprox = isShiftActive)
                isShiftActive = false
              }
            }
          }
        }
          }
        ) { measurables, constraints ->
          val lcdMeasurable = measurables[0]
          val keypadMeasurable = measurables[1]
          val maxW = constraints.maxWidth
          val maxH = constraints.maxHeight
          if (isLandscapeSplit) {
            val gapPx = 8.dp.roundToPx()
            val availW = (maxW - gapPx).coerceAtLeast(0)
            val lcdW = (availW * 0.38f).toInt()
            val keypadW = availW - lcdW
            val lcdPlaceable = lcdMeasurable.measure(Constraints.fixed(lcdW, maxH))
            val keypadPlaceable = keypadMeasurable.measure(Constraints.fixed(keypadW, maxH))
            layout(maxW, maxH) {
              lcdPlaceable.placeRelative(0, 0)
              keypadPlaceable.placeRelative(lcdW + gapPx, 0)
            }
          } else {
            val gapPx = 5.dp.roundToPx()
            val availH = (maxH - gapPx).coerceAtLeast(0)
            val lcdH = (availH * 0.28f).toInt()
            val keypadH = availH - lcdH
            val lcdPlaceable = lcdMeasurable.measure(Constraints.fixed(maxW, lcdH))
            val keypadPlaceable = keypadMeasurable.measure(Constraints.fixed(maxW, keypadH))
            layout(maxW, maxH) {
              lcdPlaceable.placeRelative(0, 0)
              keypadPlaceable.placeRelative(0, lcdH + gapPx)
            }
          }
        }
      }
    }
  }

  // ============================================================================
  // AUXILIARY DIALOGS (SETUP, VARS, HISTORY, DRG, CALC)
  // ============================================================================

  if (showSetupModal) {
    Dialog(onDismissRequest = { showSetupModal = false }) {
      Surface(shape = RoundedCornerShape(22.dp), color = palette.dialogSurface, border = palette.glassBorder, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Tune, null, tint = accentCyan)
            Text("SETUP • fx-570ES PLUS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
          }
          Text("Angle Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textSecondary)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClassWizAngleUnit.entries.forEach { u ->
              RecessedGlassPill(isDarkMode = isDarkMode, fillColor = if (angleUnit == u) accentCyan.copy(alpha = 0.25f) else null, borderColor = if (angleUnit == u) accentCyan else null, onClick = { angleUnit = u }) {
                Text("${u.label} (${u.badge})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
              }
            }
          }
          Text("Number Format", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textSecondary)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClassWizNumberFormat.entries.forEach { f ->
              RecessedGlassPill(isDarkMode = isDarkMode, fillColor = if (numberFormat == f) accentCyan.copy(alpha = 0.25f) else null, borderColor = if (numberFormat == f) accentCyan else null, onClick = { numberFormat = f; refreshCompDisplayFromNumeric(activeDisplayNumeric, isCommit = false) }) {
                Text(f.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
              }
            }
          }
          if (numberFormat != ClassWizNumberFormat.NORM) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("Digits: $fixDigits", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
              RecessedGlassPill(isDarkMode = isDarkMode, onClick = { fixDigits = (fixDigits - 1).coerceAtLeast(1) }) { Text("-", fontWeight = FontWeight.Bold, color = palette.textPrimary) }
              RecessedGlassPill(isDarkMode = isDarkMode, onClick = { fixDigits = (fixDigits + 1).coerceAtMost(9) }) { Text("+", fontWeight = FontWeight.Bold, color = palette.textPrimary) }
            }
          }
          TextButton(onClick = { showSetupModal = false }, modifier = Modifier.align(Alignment.End)) { Text("Done") }
        }
      }
    }
  }

  if (showVariablesModal) {
    Dialog(onDismissRequest = { showVariablesModal = false }) {
      Surface(shape = RoundedCornerShape(22.dp), color = palette.dialogSurface, border = palette.glassBorder, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Variables (A–F, X, Y, M, Ans)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
          listOf("A", "B", "C", "D", "E", "F", "X", "Y", "M").chunked(3).forEach { rowKeys ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              rowKeys.forEach { k ->
                Surface(
                  onClick = { insertToken(k); showVariablesModal = false },
                  shape = RoundedCornerShape(10.dp),
                  color = palette.nestedCardFill,
                  border = palette.nestedBorder,
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(8.dp)) {
                    Text(k, fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = accentCyan)
                    Text(formatDecimalCompact(variables[k] ?: 0.0), fontFamily = JetBrainsMonoFamily, fontSize = 11.5.sp, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  }
                }
              }
            }
          }
          Text("Ans = ${formatDecimalCompact(lastNumericAns)}", fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentCyan)
          TextButton(onClick = { showVariablesModal = false }, modifier = Modifier.align(Alignment.End)) { Text("Close") }
        }
      }
    }
  }

  if (showHistoryModal) {
    EsHistoryDialog(
      history = history,
      onDismiss = { showHistoryModal = false },
      onClearHistory = { history.clear() },
      onSelectEntry = { entry, useResult ->
        lcdState = EsLcdScreenState.CALC_LINE
        if (useResult) {
          insertToken(entry.exactOrDisplay)
        } else if (entry.treeSnapshot != null && rootMathContainer.nodes.isEmpty()) {
          loadTreeIntoRoot(entry.treeSnapshot)
        } else if (entry.treeSnapshot != null) {
          clearErrorState()
          val cloned = cloneMathContainer(entry.treeSnapshot)
          val targetC = if (containsContainer(rootMathContainer, cursorLocation.container)) cursorLocation.container else rootMathContainer
          var insertAt = cursorLocation.index.coerceIn(0, targetC.nodes.size)
          cloned.nodes.forEach { n ->
            targetC.nodes.add(insertAt, n)
            insertAt++
          }
          cursorLocation = CursorLocation(targetC, insertAt)
        } else {
          insertToken(entry.expression)
        }
        showHistoryModal = false
      },
      accentCyan = accentCyan
    )
  }

  if (showDrgModal) {
    EsDrgUnitDialog(
      onDismiss = { showDrgModal = false },
      onInsertToken = { tok ->
        insertToken(tok)
        showDrgModal = false
      },
      accentCyan = accentCyan
    )
  }

  if (showCalcPrompt) {
    Dialog(onDismissRequest = { showCalcPrompt = false }) {
      Surface(shape = RoundedCornerShape(22.dp), color = palette.dialogSurface, border = palette.glassBorder, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
// UNIFORM KEYCAP COMPONENT: CalcKeyCell (FIXED 15.dp SUB-LABEL SLOT + 2.dp GAP)
// ============================================================================

@Composable
private fun EsLcdBadge(text: String, active: Boolean, bg: Color, fg: Color) {
  if (!active) return
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(bg)
      .padding(horizontal = 4.dp, vertical = 1.dp)
  ) {
    Text(
      text = text,
      fontFamily = JetBrainsMonoFamily,
      fontSize = 9.sp,
      fontWeight = FontWeight.ExtraBold,
      color = fg,
      maxLines = 1,
      softWrap = false
    )
  }
}

private val LocalEsCalcSubRowHeight = staticCompositionLocalOf { 15.dp }
private val LocalEsCalcSubGap = staticCompositionLocalOf { 2.dp }

@Composable
private fun CalcKeyCell(
  primary: String,
  shift: String = "",
  alpha: String = "",
  containerColor: Color,
  contentColor: Color,
  primarySp: TextUnit,
  subSp: TextUnit = 8.5.sp,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val subRowHeight = LocalEsCalcSubRowHeight.current
  val subGap = LocalEsCalcSubGap.current
  val subLabelStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontSize = subSp,
    fontWeight = FontWeight.Bold,
    lineHeight = (subSp.value + 1f).sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false)
  )
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Fixed sub-label row on every single key (scales proportionally in compact landscape)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(subRowHeight)
        .padding(horizontal = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = shift,
        style = subLabelStyle,
        color = Color(0xFFF59E0B),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible
      )
      Text(
        text = alpha,
        style = subLabelStyle,
        color = Color(0xFFF43F5E),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible
      )
    }

    Spacer(modifier = Modifier.height(subGap))

    // Physical Key Surface (occupies exact remaining height of the row)
    Surface(
      onClick = onClick,
      shape = RoundedCornerShape(9.dp),
      color = containerColor,
      border = BorderStroke(
        width = 1.dp,
        color = if (containerColor == Color(0xFFF8FAFC)) Color.White else Color.White.copy(alpha = 0.22f)
      ),
      shadowElevation = 2.dp,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = primary,
          style = TextStyle(
            fontFamily = AppleSansFamily,
            fontSize = primarySp,
            fontWeight = FontWeight.Bold,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
          ),
          color = contentColor,
          maxLines = 1,
          softWrap = false
        )
      }
    }
  }
}

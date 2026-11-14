package com.example

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import kotlin.math.abs

// ============================================================================
// SIBIL CE • SURVEYING & CURVE SOLVER SCREEN (CELE MSTE)
// ============================================================================

@Composable
internal fun SurveyingSolverScreen(
  metrics: ResponsiveMetrics,
  onBackToExtras: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val localView = LocalView.current
  val clipboardManager = LocalClipboardManager.current
  val palette = currentGlassPalette()
  val isDarkMode = palette.isDark
  val accentCyan = palette.accentIndigo

  BackHandler { onBackToExtras() }

  var activeTabId by rememberSaveable { mutableStateOf(SurveyingToolTab.TRAVERSE_DMD.id) }
  val activeTab = SurveyingToolTab.entries.firstOrNull { it.id == activeTabId }
    ?: SurveyingToolTab.TRAVERSE_DMD

  // --- Tool 1 State: Closed Traverse & DMD Area ---
  var nextCourseId by rememberSaveable { mutableLongStateOf(4L) }
  val traverseCourses = remember {
    mutableStateListOf(
      TraverseCourseInput(1L, "120.50", "N", "35", "20", "E"),
      TraverseCourseInput(2L, "145.80", "S", "68", "45", "E"),
      TraverseCourseInput(3L, "198.40", "S", "82", "15", "W")
    )
  }
  var traverseRule by remember { mutableStateOf(TraverseAdjustmentRule.COMPASS) }
  val traverseResult = remember(traverseCourses.toList(), traverseRule) {
    computeClosedTraverseDmd(traverseCourses.toList(), traverseRule)
  }

  // --- Tool 2 State: Simple Circular Horizontal Curve ---
  var simpleMode by remember { mutableStateOf(SimpleCurveInputMode.R_AND_I) }
  var simpleBasis by remember { mutableStateOf(CurveStationBasis.ARC) }
  var simplePrimary by rememberSaveable { mutableStateOf("250.00") }
  var simpleAngleI by rememberSaveable { mutableStateOf("42.50") }
  var simplePiSta by rememberSaveable { mutableStateOf("10450.25") }
  val simpleResult = remember(simpleMode, simpleBasis, simplePrimary, simpleAngleI, simplePiSta) {
    computeSimpleHorizontalCurve(simpleMode, simpleBasis, simplePrimary, simpleAngleI, simplePiSta)
  }

  // --- Tool 3 State: Compound Curve ---
  var compR1 by rememberSaveable { mutableStateOf("220.00") }
  var compI1 by rememberSaveable { mutableStateOf("28.00") }
  var compR2 by rememberSaveable { mutableStateOf("160.00") }
  var compI2 by rememberSaveable { mutableStateOf("34.00") }
  var compPiSta by rememberSaveable { mutableStateOf("5240.00") }
  val compResult = remember(compR1, compI1, compR2, compI2, compPiSta) {
    computeCompoundCurve(compR1, compI1, compR2, compI2, compPiSta)
  }

  // --- Tool 4 State: Vertical Parabolic Curve ---
  var vertG1 by rememberSaveable { mutableStateOf("3.20") }
  var vertG2 by rememberSaveable { mutableStateOf("-2.40") }
  var vertL by rememberSaveable { mutableStateOf("200.00") }
  var vertStaPvi by rememberSaveable { mutableStateOf("1200.00") }
  var vertElevPvi by rememberSaveable { mutableStateOf("105.50") }
  var vertQuerySta by rememberSaveable { mutableStateOf("1160.00") }
  val vertResult = remember(vertG1, vertG2, vertL, vertStaPvi, vertElevPvi, vertQuerySta) {
    computeVerticalParabolicCurve(vertG1, vertG2, vertL, vertStaPvi, vertElevPvi, vertQuerySta)
  }

  // --- Tool 5 State: Leveling & Stadia ---
  var stadiaU by rememberSaveable { mutableStateOf("2.480") }
  var stadiaM by rememberSaveable { mutableStateOf("1.820") }
  var stadiaL by rememberSaveable { mutableStateOf("1.160") }
  var stadiaAlpha by rememberSaveable { mutableStateOf("8.50") }
  var stadiaHi by rememberSaveable { mutableStateOf("45.620") }
  var stadiaK by rememberSaveable { mutableStateOf("100") }
  var stadiaC by rememberSaveable { mutableStateOf("0") }
  val stadiaResult = remember(stadiaU, stadiaM, stadiaL, stadiaAlpha, stadiaHi, stadiaK, stadiaC) {
    computeStadiaLeveling(stadiaU, stadiaM, stadiaL, stadiaAlpha, stadiaHi, stadiaK, stadiaC)
  }

  // --- Tool 6 State: Earthwork Volumes ---
  var earthL by rememberSaveable { mutableStateOf("20.00") }
  var earthA1 by rememberSaveable { mutableStateOf("24.50") }
  var earthAm by rememberSaveable { mutableStateOf("29.20") }
  var earthA2 by rememberSaveable { mutableStateOf("36.80") }
  val earthResult = remember(earthL, earthA1, earthAm, earthA2) {
    computeEarthworkVolumes(earthL, earthA1, earthAm, earthA2)
  }

  val blueprintState = SurveyingBlueprintState(
    activeTab = activeTab,
    traverseResult = traverseResult,
    simpleCurveResult = simpleResult,
    compoundCurveResult = compResult,
    verticalCurveResult = vertResult,
    stadiaResult = stadiaResult,
    earthworkResult = earthResult
  )

  fun copyReport(text: String) {
    if (text.isBlank()) return
    playCardHapticSound(context, localView)
    clipboardManager.setText(AnnotatedString(text))
    Toast.makeText(context, "Surveying solution copied", Toast.LENGTH_SHORT).show()
  }

  val orientation = rememberScreenOrientationState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .widthIn(max = if (orientation.isLandscape) 1140.dp else 760.dp)
      .padding(top = if (orientation.isCompactLandscape) 8.dp else metrics.scaledDp(14))
      .testTag("surveying_solver_screen"),
    verticalArrangement = Arrangement.spacedBy(if (orientation.isCompactLandscape) 8.dp else 12.dp)
  ) {
    // 1. TOP STICKY HEADER
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.weight(1f)
      ) {
        IconButton(
          onClick = {
            playCardHapticSound(context, localView)
            onBackToExtras()
          },
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(40.dp)
            .clip(CircleShape)
            .background(
              if (isDarkMode) Color(0xFF1E293B).copy(alpha = 0.72f)
              else Color.White.copy(alpha = 0.88f)
            )
            .border(
              1.dp,
              if (isDarkMode) Color.White.copy(alpha = 0.18f) else Color(0xFFCBD5E1),
              CircleShape
            )
            .testTag("surveying_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Extras",
            tint = palette.textPrimary,
            modifier = Modifier.size(20.dp)
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Surveying & Curve Solver",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontFamily = AppleSansFamily,
              fontSize = metrics.scaledSp(20),
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.4).sp
            ),
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "CELE MSTE • Philippine 20m Stationing",
            style = MaterialTheme.typography.labelMedium.copy(
              fontFamily = AppleSansFamily,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold
            ),
            color = accentCyan,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(accentCyan.copy(alpha = 0.14f))
          .border(1.dp, accentCyan.copy(alpha = 0.32f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.Explore,
          contentDescription = null,
          tint = accentCyan,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    // 2. HORIZONTAL SCROLLABLE TOOL SELECTOR RIBBON
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .height(40.dp)
        .testTag("surveying_tool_ribbon"),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      items(SurveyingToolTab.entries, key = { it.id }) { tab ->
        val selected = tab == activeTab
        RecessedGlassPill(
          isDarkMode = isDarkMode,
          fillColor = if (selected) accentCyan else null,
          borderColor = if (selected) accentCyan else null,
          onClick = {
            playCardHapticSound(context, localView)
            activeTabId = tab.id
          },
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier.testTag("surveying_tab_${tab.id}")
        ) {
          Text(
            text = tab.title,
            style = MaterialTheme.typography.labelMedium.copy(
              fontFamily = AppleSansFamily,
              fontSize = 12.5.sp,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (selected) Color.White else palette.textPrimary,
            maxLines = 1,
            softWrap = false
          )
        }
      }
    }

    // 3. MAIN WORKSPACE (PORTRAIT: VERTICAL STACK; LANDSCAPE: CAD LEFT + WORKSPACE RIGHT)
    val renderLiveCadCard: @Composable () -> Unit = {
      LiquidGlassCard(
        cornerRadius = 22.dp,
        elevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "LIVE CAD SCHEMATIC",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp
              ),
              color = accentCyan
            )
            Text(
              text = activeTab.title,
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = AppleSansFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              ),
              color = palette.textSecondary
            )
          }

          SurveyingCadBlueprintCanvas(
            state = blueprintState,
            isDarkMode = isDarkMode,
            heightDp = when {
              orientation.isCompactLandscape -> 195
              orientation.isLandscape -> 240
              else -> 210
            }
          )
        }
      }
    }

    val renderActiveTabCards: @Composable () -> Unit = {
      when (activeTab) {
        SurveyingToolTab.TRAVERSE_DMD -> {
          TraverseInputCard(
            courses = traverseCourses,
            rule = traverseRule,
            onRuleChange = {
              playCardHapticSound(context, localView)
              traverseRule = it
            },
            onUpdateCourse = { index, updated -> traverseCourses[index] = updated },
            onAddCourse = {
              playCardHapticSound(context, localView)
              traverseCourses.add(
                TraverseCourseInput(
                  id = nextCourseId++,
                  distanceText = "100.00",
                  ns = "N",
                  degText = "15",
                  minText = "00",
                  ew = "W"
                )
              )
            },
            onDeleteCourse = { index ->
              if (traverseCourses.size > 3) {
                playCardHapticSound(context, localView)
                traverseCourses.removeAt(index)
              }
            }
          )
          TraverseResultsCard(
            result = traverseResult,
            onCopy = { copyReport(traverseResult.copySummary) }
          )
        }

        SurveyingToolTab.SIMPLE_CURVE -> {
          SimpleCurveInputCard(
            mode = simpleMode,
            basis = simpleBasis,
            primaryText = simplePrimary,
            angleIText = simpleAngleI,
            piStationText = simplePiSta,
            onModeChange = {
              playCardHapticSound(context, localView)
              simpleMode = it
            },
            onBasisChange = {
              playCardHapticSound(context, localView)
              simpleBasis = it
            },
            onPrimaryChange = { simplePrimary = it },
            onAngleIChange = { simpleAngleI = it },
            onPiStationChange = { simplePiSta = it }
          )
          SimpleCurveResultsCard(
            result = simpleResult,
            onCopy = { copyReport(simpleResult.copySummary) }
          )
        }

        SurveyingToolTab.COMPOUND_CURVE -> {
          CompoundCurveInputCard(
            r1 = compR1,
            i1 = compI1,
            r2 = compR2,
            i2 = compI2,
            piSta = compPiSta,
            onR1Change = { compR1 = it },
            onI1Change = { compI1 = it },
            onR2Change = { compR2 = it },
            onI2Change = { compI2 = it },
            onPiChange = { compPiSta = it }
          )
          CompoundCurveResultsCard(
            result = compResult,
            onCopy = { copyReport(compResult.copySummary) }
          )
        }

        SurveyingToolTab.VERTICAL_CURVE -> {
          VerticalCurveInputCard(
            g1 = vertG1,
            g2 = vertG2,
            lengthL = vertL,
            staPvi = vertStaPvi,
            elevPvi = vertElevPvi,
            querySta = vertQuerySta,
            result = vertResult,
            onG1Change = { vertG1 = it },
            onG2Change = { vertG2 = it },
            onLChange = { vertL = it },
            onStaPviChange = { vertStaPvi = it },
            onElevPviChange = { vertElevPvi = it },
            onQueryStaChange = { vertQuerySta = it }
          )
          VerticalCurveResultsCard(
            result = vertResult,
            onCopy = { copyReport(vertResult.copySummary) }
          )
        }

        SurveyingToolTab.STADIA_LEVELING -> {
          StadiaInputCard(
            u = stadiaU,
            m = stadiaM,
            l = stadiaL,
            alpha = stadiaAlpha,
            hi = stadiaHi,
            k = stadiaK,
            c = stadiaC,
            onUChange = { stadiaU = it },
            onMChange = { stadiaM = it },
            onLChange = { stadiaL = it },
            onAlphaChange = { stadiaAlpha = it },
            onHiChange = { stadiaHi = it },
            onKChange = { stadiaK = it },
            onCChange = { stadiaC = it }
          )
          StadiaResultsCard(
            result = stadiaResult,
            onCopy = { copyReport(stadiaResult.copySummary) }
          )
        }

        SurveyingToolTab.EARTHWORK_VOLUMES -> {
          EarthworkInputCard(
            lengthL = earthL,
            a1 = earthA1,
            am = earthAm,
            a2 = earthA2,
            onLChange = { earthL = it },
            onA1Change = { earthA1 = it },
            onAmChange = { earthAm = it },
            onA2Change = { earthA2 = it }
          )
          EarthworkResultsCard(
            result = earthResult,
            onCopy = { copyReport(earthResult.copySummary) }
          )
        }
      }
    }

    if (orientation.isLandscape) {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
      ) {
        Column(
          modifier = Modifier
            .weight(0.45f)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(top = 2.dp, bottom = if (orientation.isCompactLandscape) 24.dp else 96.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          renderLiveCadCard()
        }
        Column(
          modifier = Modifier
            .weight(0.55f)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(top = 2.dp, bottom = if (orientation.isCompactLandscape) 24.dp else 96.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          renderActiveTabCards()
        }
      }
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(
            start = 16.dp,
            end = 16.dp,
            top = 2.dp,
            bottom = metrics.scaledDp(116)
          ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        renderLiveCadCard()
        renderActiveTabCards()
      }
    }
  }
}

// ============================================================================
// SHARED UI BUILDING BLOCKS
// ============================================================================

@Composable
private fun SurveyNumericField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = ""
) {
  val palette = currentGlassPalette()
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = {
      Text(
        text = label,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 11.5.sp
      )
    },
    placeholder = if (placeholder.isNotEmpty()) {
      { Text(text = placeholder, fontSize = 12.sp, color = palette.textSecondary.copy(alpha = 0.6f)) }
    } else null,
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    textStyle = TextStyle(
      fontFamily = JetBrainsMonoFamily,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
      color = palette.textPrimary,
      fontFeatureSettings = "tnum"
    ),
    colors = sibilOutlinedTextFieldColors(),
    shape = RoundedCornerShape(14.dp),
    modifier = modifier
  )
}

@Composable
private fun <T> SegmentedGlassSelector(
  options: List<Pair<T, String>>,
  selected: T,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier
) {
  val palette = currentGlassPalette()
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    options.forEach { (item, label) ->
      val isSel = item == selected
      RecessedGlassPill(
        isDarkMode = palette.isDark,
        fillColor = if (isSel) palette.accentIndigo else null,
        borderColor = if (isSel) palette.accentIndigo else null,
        onClick = { onSelect(item) },
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp),
        modifier = Modifier
          .weight(1f)
          .minimumInteractiveComponentSize()
      ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
              fontFamily = AppleSansFamily,
              fontSize = 12.sp,
              fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (isSel) Color.White else palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@Composable
private fun ResultsShellCard(
  title: String,
  subtitle: String,
  errorMessage: String?,
  onCopy: () -> Unit,
  content: @Composable () -> Unit
) {
  val palette = currentGlassPalette()
  LiquidGlassCard(
    cornerRadius = 22.dp,
    elevation = 6.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = AppleSansFamily,
              fontSize = 15.5.sp,
              fontWeight = FontWeight.Bold
            ),
            color = palette.textPrimary
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = AppleSansFamily,
              fontSize = 11.sp
            ),
            color = palette.textSecondary
          )
        }

        if (errorMessage == null) {
          RecessedGlassPill(
            isDarkMode = palette.isDark,
            onClick = onCopy,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("surveying_copy_result_button")
          ) {
            Icon(
              imageVector = Icons.Filled.ContentCopy,
              contentDescription = "Copy result",
              tint = palette.accentIndigo,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = "Copy",
              style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = AppleSansFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              ),
              color = palette.textPrimary
            )
          }
        }
      }

      GlassHairlineDivider(isDarkMode = palette.isDark)

      if (errorMessage != null) {
        Text(
          text = errorMessage,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = AppleSansFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          ),
          color = Color(0xFFFB7185)
        )
      } else {
        content()
      }
    }
  }
}

@Composable
private fun FormulaMetricRow(
  label: String,
  value: String,
  formulaHint: String? = null,
  highlight: Boolean = false
) {
  val palette = currentGlassPalette()
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(
        if (highlight) palette.accentIndigo.copy(alpha = if (palette.isDark) 0.16f else 0.09f)
        else Color.Transparent
      )
      .padding(horizontal = if (highlight) 10.dp else 2.dp, vertical = if (highlight) 7.dp else 3.dp),
    verticalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(
          fontFamily = AppleSansFamily,
          fontSize = 12.5.sp,
          fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium
        ),
        color = palette.textSecondary,
        modifier = Modifier.weight(1f).padding(end = 8.dp)
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = JetBrainsMonoFamily,
          fontSize = if (highlight) 14.sp else 13.sp,
          fontWeight = FontWeight.Bold,
          fontFeatureSettings = "tnum"
        ),
        color = if (highlight) palette.accentIndigo else palette.textPrimary
      )
    }
    if (!formulaHint.isNullOrBlank()) {
      Text(
        text = formulaHint,
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = JetBrainsMonoFamily,
          fontSize = 10.5.sp,
          fontFeatureSettings = "tnum"
        ),
        color = palette.textSecondary.copy(alpha = 0.85f)
      )
    }
  }
}

// ============================================================================
// TOOL 1: TRAVERSE & DMD AREA INPUT + RESULTS
// ============================================================================

@Composable
private fun TraverseInputCard(
  courses: List<TraverseCourseInput>,
  rule: TraverseAdjustmentRule,
  onRuleChange: (TraverseAdjustmentRule) -> Unit,
  onUpdateCourse: (Int, TraverseCourseInput) -> Unit,
  onAddCourse: () -> Unit,
  onDeleteCourse: (Int) -> Unit
) {
  val palette = currentGlassPalette()
  LiquidGlassCard(
    cornerRadius = 22.dp,
    elevation = 6.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Traverse Courses (${courses.size} Lines)",
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = AppleSansFamily,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary
        )
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = palette.accentIndigo.copy(alpha = 0.16f),
          borderColor = palette.accentIndigo.copy(alpha = 0.45f),
          onClick = onAddCourse,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("traverse_add_line_button")
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add traverse line",
            tint = palette.accentIndigo,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "Add Line",
            style = MaterialTheme.typography.labelMedium.copy(
              fontFamily = AppleSansFamily,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            ),
            color = palette.accentIndigo
          )
        }
      }

      SegmentedGlassSelector(
        options = listOf(
          TraverseAdjustmentRule.COMPASS to "Compass (Bowditch)",
          TraverseAdjustmentRule.TRANSIT to "Transit Rule"
        ),
        selected = rule,
        onSelect = onRuleChange
      )

      courses.forEachIndexed { idx, course ->
        val nextNum = if (idx == courses.lastIndex) 1 else idx + 2
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = if (palette.isDark) Color.Black.copy(alpha = 0.24f) else Color(0xFFF8FAFC).copy(alpha = 0.85f),
          border = BorderStroke(
            1.dp,
            if (palette.isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFCBD5E1)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Line ${idx + 1}–$nextNum",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = palette.accentIndigo
              )
              if (courses.size > 3) {
                IconButton(
                  onClick = { onDeleteCourse(idx) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove line",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              SurveyNumericField(
                label = "Dist (m)",
                value = course.distanceText,
                onValueChange = { onUpdateCourse(idx, course.copy(distanceText = it)) },
                modifier = Modifier.weight(1.1f)
              )

              // N/S toggle pill
              QuadrantToggleChip(
                text = course.ns,
                onClick = {
                  onUpdateCourse(idx, course.copy(ns = if (course.ns == "N") "S" else "N"))
                }
              )

              SurveyNumericField(
                label = "Deg",
                value = course.degText,
                onValueChange = { onUpdateCourse(idx, course.copy(degText = it)) },
                modifier = Modifier.weight(0.75f)
              )

              SurveyNumericField(
                label = "Min",
                value = course.minText,
                onValueChange = { onUpdateCourse(idx, course.copy(minText = it)) },
                modifier = Modifier.weight(0.8f)
              )

              // E/W toggle pill
              QuadrantToggleChip(
                text = course.ew,
                onClick = {
                  onUpdateCourse(idx, course.copy(ew = if (course.ew == "E") "W" else "E"))
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun QuadrantToggleChip(
  text: String,
  onClick: () -> Unit
) {
  val palette = currentGlassPalette()
  Box(
    modifier = Modifier
      .minimumInteractiveComponentSize()
      .height(48.dp)
      .width(40.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(palette.accentIndigo.copy(alpha = if (palette.isDark) 0.24f else 0.14f))
      .border(1.dp, palette.accentIndigo.copy(alpha = 0.50f), RoundedCornerShape(12.dp))
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.titleMedium.copy(
        fontFamily = JetBrainsMonoFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold
      ),
      color = palette.accentIndigo
    )
  }
}

@Composable
private fun TraverseResultsCard(
  result: TraverseComputationResult,
  onCopy: () -> Unit
) {
  val palette = currentGlassPalette()
  ResultsShellCard(
    title = "Closure & DMD Breakdown",
    subtitle = "${result.rule.label} • Double Meridian Distance",
    errorMessage = if (result.isValid) null else result.errorMessage,
    onCopy = onCopy
  ) {
    FormulaMetricRow(
      label = "Lot Area (m² / ha)",
      value = "${fmtNum(result.areaSqm, 3)} m² (${fmtNum(result.areaHectares, 5)} ha)",
      formulaHint = "2A = |Σ(DMDᵢ·Lat_bal,ᵢ)| = ${fmtNum(result.sumDoubleArea, 3)} m²",
      highlight = true
    )
    FormulaMetricRow(
      label = "Perimeter (ΣD)",
      value = "${fmtNum(result.perimeter, 3)} m"
    )
    FormulaMetricRow(
      label = "Raw ΣLat / ΣDep",
      value = "${fmtNum(result.sumLat, 4)} / ${fmtNum(result.sumDep, 4)} m",
      formulaHint = "Lat = D·cos(θ), Dep = D·sin(θ)"
    )
    FormulaMetricRow(
      label = "Linear Error of Closure (LEC)",
      value = "${fmtNum(result.lec, 4)} m",
      formulaHint = "LEC = √(ΣLat² + ΣDep²)"
    )
    FormulaMetricRow(
      label = "Bearing of Side of Error",
      value = result.errorBearingText,
      formulaHint = "tan(β) = |ΣDep / ΣLat|"
    )
    FormulaMetricRow(
      label = "Relative Precision (RP)",
      value = result.relativePrecisionText,
      formulaHint = "RP = 1 : ⌊P / LEC⌋"
    )

    GlassHairlineDivider(isDarkMode = palette.isDark)

    Text(
      text = "COURSE-BY-COURSE BALANCED & DMD TABLE",
      style = MaterialTheme.typography.labelSmall.copy(
        fontFamily = JetBrainsMonoFamily,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
      ),
      color = palette.accentIndigo
    )

    result.courses.forEach { c ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (palette.isDark) Color.Black.copy(alpha = 0.22f) else Color(0xFFF1F5F9),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "${c.lineLabel} • ${c.bearingText}",
              style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
              ),
              color = palette.textPrimary
            )
            Text(
              text = "D = ${fmtNum(c.distance, 2)} m",
              style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.5.sp,
                fontFeatureSettings = "tnum"
              ),
              color = palette.accentIndigo
            )
          }
          Text(
            text = "Raw: Lat ${fmtNum(c.latRaw, 3)}, Dep ${fmtNum(c.depRaw, 3)}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = JetBrainsMonoFamily,
              fontSize = 10.5.sp,
              fontFeatureSettings = "tnum"
            ),
            color = palette.textSecondary
          )
          Text(
            text = "Bal: Lat ${fmtNum(c.latBal, 3)}, Dep ${fmtNum(c.depBal, 3)}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = JetBrainsMonoFamily,
              fontSize = 10.5.sp,
              fontFeatureSettings = "tnum"
            ),
            color = palette.textPrimary
          )
          Text(
            text = "DMD = ${fmtNum(c.dmd, 3)}  |  DMD·Lat = ${fmtNum(c.doubleAreaTerm, 3)}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = JetBrainsMonoFamily,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              fontFeatureSettings = "tnum"
            ),
            color = Color(0xFF10B981)
          )
        }
      }
    }
  }
}

// ============================================================================
// TOOL 2: SIMPLE HORIZONTAL CURVE
// ============================================================================

@Composable
private fun SimpleCurveInputCard(
  mode: SimpleCurveInputMode,
  basis: CurveStationBasis,
  primaryText: String,
  angleIText: String,
  piStationText: String,
  onModeChange: (SimpleCurveInputMode) -> Unit,
  onBasisChange: (CurveStationBasis) -> Unit,
  onPrimaryChange: (String) -> Unit,
  onAngleIChange: (String) -> Unit,
  onPiStationChange: (String) -> Unit
) {
  val palette = currentGlassPalette()
  val primaryLabel = when (mode) {
    SimpleCurveInputMode.R_AND_I -> "Radius R (m)"
    SimpleCurveInputMode.D_AND_I -> "Degree of Curve D (°)"
    SimpleCurveInputMode.T_AND_I -> "Tangent Distance T (m)"
  }

  LiquidGlassCard(cornerRadius = 22.dp, elevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        text = "Simple Curve Parameters (20m Full Station)",
        style = MaterialTheme.typography.titleMedium.copy(
          fontFamily = AppleSansFamily,
          fontSize = 15.5.sp,
          fontWeight = FontWeight.Bold
        ),
        color = palette.textPrimary
      )

      SegmentedGlassSelector(
        options = listOf(
          SimpleCurveInputMode.R_AND_I to "R & I",
          SimpleCurveInputMode.D_AND_I to "D & I",
          SimpleCurveInputMode.T_AND_I to "T & I"
        ),
        selected = mode,
        onSelect = onModeChange
      )

      SegmentedGlassSelector(
        options = listOf(
          CurveStationBasis.ARC to "Arc Basis",
          CurveStationBasis.CHORD to "Chord Basis"
        ),
        selected = basis,
        onSelect = onBasisChange
      )

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SurveyNumericField(
          label = primaryLabel,
          value = primaryText,
          onValueChange = onPrimaryChange,
          modifier = Modifier.weight(1f)
        )
        SurveyNumericField(
          label = "Angle I (°)",
          value = angleIText,
          onValueChange = onAngleIChange,
          modifier = Modifier.weight(1f)
        )
      }

      SurveyNumericField(
        label = "PI Station in meters (Optional, e.g. 10450.25)",
        value = piStationText,
        onValueChange = onPiStationChange,
        placeholder = "10450.25",
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun SimpleCurveResultsCard(result: SimpleCurveResult, onCopy: () -> Unit) {
  ResultsShellCard(
    title = "Horizontal Curve Elements",
    subtitle = "${result.basis.label} • Philippine 20m Stationing",
    errorMessage = if (result.isValid) null else result.errorMessage,
    onCopy = onCopy
  ) {
    FormulaMetricRow("Radius (R)", "${fmtNum(result.radius, 3)} m", highlight = true)
    FormulaMetricRow(
      "Degree of Curve (D)",
      formatDmsAngle(result.degreeD),
      formulaHint = if (result.basis == CurveStationBasis.ARC) {
        "Da = 1145.9156 / R = ${fmtNum(result.degreeD, 4)}°"
      } else {
        "Dc = 2·arcsin(10 / R) = ${fmtNum(result.degreeD, 4)}°"
      }
    )
    FormulaMetricRow("Deflection Angle (I)", formatDmsAngle(result.angleI))
    FormulaMetricRow("Tangent Distance (T)", "${fmtNum(result.tangentT, 3)} m", "T = R·tan(I/2)")
    FormulaMetricRow(
      "Length of Curve (Lc)",
      "${fmtNum(result.lengthLc, 3)} m",
      if (result.basis == CurveStationBasis.ARC) "Lc = π·R·I / 180" else "Lc = 20·I / Dc"
    )
    FormulaMetricRow("Long Chord (LC)", "${fmtNum(result.longChordLc, 3)} m", "LC = 2·R·sin(I/2)")
    FormulaMetricRow("External Distance (E)", "${fmtNum(result.externalE, 3)} m", "E = R·(sec(I/2) - 1)")
    FormulaMetricRow("Middle Ordinate (M)", "${fmtNum(result.middleOrdinateM, 3)} m", "M = R·(1 - cos(I/2))")

    if (result.staPi != null && result.staPc != null && result.staPt != null) {
      FormulaMetricRow("Sta. PC (PI - T)", formatMetricStation(result.staPc), highlight = true)
      FormulaMetricRow("Sta. PI", formatMetricStation(result.staPi))
      FormulaMetricRow("Sta. PT (PC + Lc)", formatMetricStation(result.staPt), highlight = true)
    }
  }
}

// ============================================================================
// TOOL 3: COMPOUND CURVE
// ============================================================================

@Composable
private fun CompoundCurveInputCard(
  r1: String,
  i1: String,
  r2: String,
  i2: String,
  piSta: String,
  onR1Change: (String) -> Unit,
  onI1Change: (String) -> Unit,
  onR2Change: (String) -> Unit,
  onI2Change: (String) -> Unit,
  onPiChange: (String) -> Unit
) {
  val palette = currentGlassPalette()
  LiquidGlassCard(cornerRadius = 22.dp, elevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        text = "Compound Curve Elements",
        style = MaterialTheme.typography.titleMedium.copy(
          fontFamily = AppleSansFamily,
          fontSize = 15.5.sp,
          fontWeight = FontWeight.Bold
        ),
        color = palette.textPrimary
      )
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SurveyNumericField("Radius R₁ (m)", r1, onR1Change, Modifier.weight(1f))
        SurveyNumericField("Angle I₁ (°)", i1, onI1Change, Modifier.weight(1f))
      }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SurveyNumericField("Radius R₂ (m)", r2, onR2Change, Modifier.weight(1f))
        SurveyNumericField("Angle I₂ (°)", i2, onI2Change, Modifier.weight(1f))
      }
      SurveyNumericField(
        label = "PI Station in meters (Optional)",
        value = piSta,
        onValueChange = onPiChange,
        placeholder = "5240.00",
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun CompoundCurveResultsCard(result: CompoundCurveResult, onCopy: () -> Unit) {
  ResultsShellCard(
    title = "Compound Curve Solution",
    subtitle = "Sine Law Tangent Triangle V₁–PI–V₂",
    errorMessage = if (result.isValid) null else result.errorMessage,
    onCopy = onCopy
  ) {
    FormulaMetricRow("Total Deflection (I = I₁ + I₂)", formatDmsAngle(result.totalI))
    FormulaMetricRow("Sub-Tangents (t₁ / t₂)", "${fmtNum(result.t1Small, 3)} / ${fmtNum(result.t2Small, 3)} m", "tᵢ = Rᵢ·tan(Iᵢ/2)")
    FormulaMetricRow("Common Tangent (Tc = t₁ + t₂)", "${fmtNum(result.commonTangentTc, 3)} m")
    FormulaMetricRow("Long Tangent T₁ (PI–PC)", "${fmtNum(result.t1Long, 3)} m", "T₁ = t₁ + Tc·sin(I₂)/sin(I)", highlight = true)
    FormulaMetricRow("Long Tangent T₂ (PI–PT)", "${fmtNum(result.t2Long, 3)} m", "T₂ = t₂ + Tc·sin(I₁)/sin(I)", highlight = true)
    FormulaMetricRow("Arc Lengths (Lc₁ / Lc₂)", "${fmtNum(result.lc1, 3)} / ${fmtNum(result.lc2, 3)} m")
    FormulaMetricRow("Total Curve Length", "${fmtNum(result.totalLength, 3)} m")

    if (result.staPc != null && result.staPcc != null && result.staPt != null) {
      FormulaMetricRow("Sta. PC (PI - T₁)", formatMetricStation(result.staPc), highlight = true)
      FormulaMetricRow("Sta. PCC (PC + Lc₁)", formatMetricStation(result.staPcc), highlight = true)
      FormulaMetricRow("Sta. PT (PCC + Lc₂)", formatMetricStation(result.staPt), highlight = true)
    }
  }
}

// ============================================================================
// TOOL 4: VERTICAL PARABOLIC CURVE
// ============================================================================

@Composable
private fun VerticalCurveInputCard(
  g1: String,
  g2: String,
  lengthL: String,
  staPvi: String,
  elevPvi: String,
  querySta: String,
  result: VerticalCurveResult,
  onG1Change: (String) -> Unit,
  onG2Change: (String) -> Unit,
  onLChange: (String) -> Unit,
  onStaPviChange: (String) -> Unit,
  onElevPviChange: (String) -> Unit,
  onQueryStaChange: (String) -> Unit
) {
  val palette = currentGlassPalette()
  LiquidGlassCard(cornerRadius = 22.dp, elevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Symmetrical Vertical Curve",
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = AppleSansFamily,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary
        )
        if (result.isValid) {
          RecessedGlassPill(
            isDarkMode = palette.isDark,
            fillColor = (if (result.isSummit) Color(0xFFF59E0B) else Color(0xFF10B981)).copy(alpha = 0.18f),
            borderColor = (if (result.isSummit) Color(0xFFF59E0B) else Color(0xFF10B981)).copy(alpha = 0.55f),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              text = result.curveType,
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = AppleSansFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              ),
              color = if (result.isSummit) Color(0xFFF59E0B) else Color(0xFF10B981)
            )
          }
        }
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SurveyNumericField("Grade g₁ (%)", g1, onG1Change, Modifier.weight(1f))
        SurveyNumericField("Grade g₂ (%)", g2, onG2Change, Modifier.weight(1f))
        SurveyNumericField("Length L (m)", lengthL, onLChange, Modifier.weight(1f))
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SurveyNumericField("Sta PVI (m)", staPvi, onStaPviChange, Modifier.weight(1f))
        SurveyNumericField("Elev PVI (m)", elevPvi, onElevPviChange, Modifier.weight(1f))
      }

      SurveyNumericField(
        label = "Query Station Staₓ in meters (Optional)",
        value = querySta,
        onValueChange = onQueryStaChange,
        placeholder = "e.g. 1160.00",
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun VerticalCurveResultsCard(result: VerticalCurveResult, onCopy: () -> Unit) {
  ResultsShellCard(
    title = "Vertical Parabolic Breakdown",
    subtitle = if (result.isValid) result.curveType else "Symmetrical Parabolic Elevation",
    errorMessage = if (result.isValid) null else result.errorMessage,
    onCopy = onCopy
  ) {
    FormulaMetricRow("Rate of Grade Change (r)", "${fmtNum(result.rateR, 5)} %/m", "r = (g₂ - g₁) / L")
    FormulaMetricRow(
      "PVC Station & Elevation",
      "${formatMetricStation(result.staPvc)} • ${fmtNum(result.elevPvc, 3)} m",
      "Elev_PVC = Elev_PVI - (g₁/100)(L/2)"
    )
    FormulaMetricRow(
      "PVT Station & Elevation",
      "${formatMetricStation(result.staPvt)} • ${fmtNum(result.elevPvt, 3)} m",
      "Elev_PVT = Elev_PVI + (g₂/100)(L/2)"
    )
    FormulaMetricRow(
      "Middle Offset (H)",
      "${fmtNum(abs(result.middleOffsetH), 3)} m (signed ${fmtNum(result.middleOffsetH, 3)})",
      "H = (g₁ - g₂)·L / 800"
    )
    if (result.hasExtremum) {
      val extLabel = if (result.isSummit) "Highest Point" else "Lowest Point"
      val statusTag = if (result.isExtWithinCurve) "Within Curve" else "Outside Curve"
      FormulaMetricRow(
        "$extLabel ($statusTag)",
        "${formatMetricStation(result.staExt)} • ${fmtNum(result.elevExt, 3)} m",
        "x = -g₁·L / (g₂ - g₁) = ${fmtNum(result.xExt, 3)} m from PVC",
        highlight = true
      )
    }
    if (result.querySta != null && result.queryElev != null) {
      FormulaMetricRow(
        "Elevation at ${formatMetricStation(result.querySta)}",
        "${fmtNum(result.queryElev, 3)} m",
        "y_offset = ${fmtNum(result.queryOffsetY ?: 0.0, 4)} m (${if (result.queryWithinCurve) "Within Curve" else "Outside Curve"})",
        highlight = true
      )
    }
  }
}

// ============================================================================
// TOOL 5: LEVELING & STADIA
// ============================================================================

@Composable
private fun StadiaInputCard(
  u: String,
  m: String,
  l: String,
  alpha: String,
  hi: String,
  k: String,
  c: String,
  onUChange: (String) -> Unit,
  onMChange: (String) -> Unit,
  onLChange: (String) -> Unit,
  onAlphaChange: (String) -> Unit,
  onHiChange: (String) -> Unit,
  onKChange: (String) -> Unit,
  onCChange: (String) -> Unit
) {
  val palette = currentGlassPalette()
  LiquidGlassCard(cornerRadius = 22.dp, elevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        text = "Stadia & Trigonometric Leveling",
        style = MaterialTheme.typography.titleMedium.copy(
          fontFamily = AppleSansFamily,
          fontSize = 15.5.sp,
          fontWeight = FontWeight.Bold
        ),
        color = palette.textPrimary
      )
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SurveyNumericField("Upper U (m)", u, onUChange, Modifier.weight(1f))
        SurveyNumericField("Middle M (m)", m, onMChange, Modifier.weight(1f))
        SurveyNumericField("Lower L (m)", l, onLChange, Modifier.weight(1f))
      }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SurveyNumericField("Vert Angle α (°)", alpha, onAlphaChange, Modifier.weight(1f))
        SurveyNumericField("Instr. HI (m)", hi, onHiChange, Modifier.weight(1f))
      }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SurveyNumericField("Stadia Factor K", k, onKChange, Modifier.weight(1f))
        SurveyNumericField("Constant C (m)", c, onCChange, Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun StadiaResultsCard(result: StadiaLevelingResult, onCopy: () -> Unit) {
  ResultsShellCard(
    title = "Stadia Reduction Solution",
    subtitle = "Inclined Sight • Horizontal & Vertical Components",
    errorMessage = if (result.isValid) null else result.errorMessage,
    onCopy = onCopy
  ) {
    FormulaMetricRow("Stadia Intercept (s = U - L)", "${fmtNum(result.interceptS, 3)} m")
    FormulaMetricRow("Slope Distance (D)", "${fmtNum(result.slopeDistanceD, 3)} m", "D = K·s·cos(α) + C")
    FormulaMetricRow("Horizontal Distance (H)", "${fmtNum(result.horizontalDistanceH, 3)} m", "H = K·s·cos²(α) + C·cos(α)", highlight = true)
    FormulaMetricRow("Vertical Distance (V)", "${fmtNum(result.verticalDistanceV, 3)} m", "V = ½·K·s·sin(2α) + C·sin(α)")
    FormulaMetricRow("Rod Station Elevation", "${fmtNum(result.rodElevation, 3)} m", "Elev = HI + V - M", highlight = true)
  }
}

// ============================================================================
// TOOL 6: EARTHWORK VOLUMES
// ============================================================================

@Composable
private fun EarthworkInputCard(
  lengthL: String,
  a1: String,
  am: String,
  a2: String,
  onLChange: (String) -> Unit,
  onA1Change: (String) -> Unit,
  onAmChange: (String) -> Unit,
  onA2Change: (String) -> Unit
) {
  val palette = currentGlassPalette()
  LiquidGlassCard(cornerRadius = 22.dp, elevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        text = "Cross-Section Areas & Station Spacing",
        style = MaterialTheme.typography.titleMedium.copy(
          fontFamily = AppleSansFamily,
          fontSize = 15.5.sp,
          fontWeight = FontWeight.Bold
        ),
        color = palette.textPrimary
      )
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SurveyNumericField("Distance L (m)", lengthL, onLChange, Modifier.weight(1f))
        SurveyNumericField("Area A₁ (m²)", a1, onA1Change, Modifier.weight(1f))
      }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SurveyNumericField("Mid Am (m², opt)", am, onAmChange, Modifier.weight(1f), placeholder = "Auto")
        SurveyNumericField("Area A₂ (m²)", a2, onA2Change, Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun EarthworkResultsCard(result: EarthworkVolumeResult, onCopy: () -> Unit) {
  ResultsShellCard(
    title = "Earthwork Volume Breakdown",
    subtitle = "Average End Area vs. Prismoidal Formula",
    errorMessage = if (result.isValid) null else result.errorMessage,
    onCopy = onCopy
  ) {
    FormulaMetricRow(
      "Mid-Section Area (Am)",
      "${fmtNum(result.areaMid, 3)} m²${if (result.isMidInterpolated) " (Auto)" else ""}",
      if (result.isMidInterpolated) "Interpolated Am = (A₁ + A₂) / 2" else "User-supplied mid-section area"
    )
    FormulaMetricRow(
      "Average End Area Volume (Ve)",
      "${fmtNum(result.volEndArea, 3)} m³",
      "Ve = (L / 2)·(A₁ + A₂)",
      highlight = true
    )
    FormulaMetricRow(
      "Prismoidal Volume (Vp)",
      "${fmtNum(result.volPrismoidal, 3)} m³",
      "Vp = (L / 6)·(A₁ + 4Am + A₂)",
      highlight = true
    )
    FormulaMetricRow(
      "Prismoidal Correction (Cp)",
      "${fmtNum(result.prismoidalCorrection, 3)} m³",
      "Cp = Ve - Vp"
    )
  }
}

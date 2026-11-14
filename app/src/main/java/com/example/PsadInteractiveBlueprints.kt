package com.example

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

internal fun psadSectionHasInteractiveBlueprint(subVolumeId: String, sectionIndex: Int): Boolean =
  when (subVolumeId) {
    "psad_strength_of_materials" -> sectionIndex in setOf(0, 1, 3, 4, 5, 7, 9)
    "psad_structural_analysis" -> sectionIndex in setOf(0, 1, 2, 7)
    "psad_rcd_pcd" -> sectionIndex in setOf(2, 3, 4, 6, 7)
    "psad_steel_design" -> sectionIndex in setOf(1, 2, 3, 6)
    "psad_foundation_engineering" -> sectionIndex in setOf(1, 3, 5, 7)
    "hpge_hydraulics_fluid_mechanics" -> sectionIndex in setOf(0, 1, 2, 3, 4, 8)
    "hpge_geotechnical_soil_mechanics" -> sectionIndex in setOf(0, 3, 4, 8)
    "hpge_hydrology" -> sectionIndex in setOf(1, 3, 6, 8)
    else -> false
  }

internal fun psadSectionHeaderBadgeLabel(subVolumeId: String, sectionIndex: Int, itemCount: Int): String =
  when {
    subVolumeId == "psad_strength_of_materials" && sectionIndex in setOf(5, 7, 9) -> "CAD LAB • $itemCount"
    subVolumeId == "psad_strength_of_materials" && sectionIndex in setOf(0, 1, 3, 4) -> "CAD FIG • $itemCount"
    subVolumeId == "psad_structural_analysis" && sectionIndex == 0 -> "CALC • $itemCount"
    subVolumeId == "psad_structural_analysis" && sectionIndex in setOf(1, 2, 7) -> "CAD LAB • $itemCount"
    subVolumeId == "psad_rcd_pcd" && sectionIndex in setOf(2, 3, 6, 7) -> "CAD LAB • $itemCount"
    subVolumeId == "psad_rcd_pcd" && sectionIndex == 4 -> "CAD FIG • $itemCount"
    subVolumeId == "psad_steel_design" && sectionIndex in setOf(1, 2, 3) -> "CAD LAB • $itemCount"
    subVolumeId == "psad_steel_design" && sectionIndex == 6 -> "CAD FIG • $itemCount"
    subVolumeId == "psad_foundation_engineering" && sectionIndex in setOf(1, 3, 5, 7) -> "CAD LAB • $itemCount"
    subVolumeId == "hpge_hydraulics_fluid_mechanics" && sectionIndex in setOf(0, 2, 3, 4, 8) -> "CAD LAB • $itemCount"
    subVolumeId == "hpge_hydraulics_fluid_mechanics" && sectionIndex == 1 -> "CAD FIG • $itemCount"
    subVolumeId == "hpge_geotechnical_soil_mechanics" && sectionIndex in setOf(0, 4, 8) -> "CAD LAB • $itemCount"
    subVolumeId == "hpge_geotechnical_soil_mechanics" && sectionIndex == 3 -> "CAD FIG • $itemCount"
    subVolumeId == "hpge_hydrology" && sectionIndex in setOf(1, 8) -> "CAD LAB • $itemCount"
    subVolumeId == "hpge_hydrology" && sectionIndex in setOf(3, 6) -> "CAD FIG • $itemCount"
    else -> "$itemCount FORMULAS"
  }

@Composable
internal fun PsadInteractiveSectionContent(
  subVolumeId: String,
  sectionIndex: Int,
  accentColor: Color
) {
  when (subVolumeId) {
    "psad_strength_of_materials" -> when (sectionIndex) {
      0 -> StrengthStaticCadFigureCard(
        figureTag = "CAD FIG • 01  |  FBD NORMAL & BEARING",
        caption = "Axial σ = P/A  •  Bolt Bearing σ_b = P/(t·d)  •  Cylinder σ_t = 2σ_L",
        accentColor = accentColor,
        testTag = "som_cad_fig_01"
      ) { cyan, amber, emerald, textCol ->
        drawSomChapter01Fbd(accentColor, cyan, amber, emerald, textCol)
      }
      1 -> StrengthStaticCadFigureCard(
        figureTag = "CAD FIG • 02  |  SINGLE, DOUBLE & PUNCHING SHEAR",
        caption = "1 Shear Plane: τ = V/A  •  2 Shear Planes: τ = V/(2A)  •  Punching: τ_p = P/(πdt)",
        accentColor = accentColor,
        testTag = "som_cad_fig_02"
      ) { cyan, amber, emerald, textCol ->
        drawSomChapter02Shear(accentColor, cyan, amber, emerald, textCol)
      }
      3 -> StrengthStaticCadFigureCard(
        figureTag = "CAD FIG • 04  |  CIRCULAR SHAFT TORSION",
        caption = "Linear shear τ(ρ) = Tρ/J  •  0 at centroidal axis to τ_max = Tr/J at outer fiber",
        accentColor = accentColor,
        testTag = "som_cad_fig_04"
      ) { cyan, amber, emerald, textCol ->
        drawSomChapter04Torsion(accentColor, cyan, amber, emerald, textCol)
      }
      4 -> StrengthStaticCadFigureCard(
        figureTag = "CAD FIG • 05  |  LOAD → SHEAR → MOMENT",
        caption = "dV/dx = -w(x)  •  dM/dx = V(x)  •  Zero shear V = 0 locates M_max",
        accentColor = accentColor,
        testTag = "som_cad_fig_05"
      ) { cyan, amber, emerald, textCol ->
        drawSomChapter05SfdBmd(accentColor, cyan, amber, emerald, textCol)
      }
      5 -> SomInteractiveStressDistributionCard(accentColor = accentColor)
      7 -> SomInteractiveMohrsCircleCard(accentColor = accentColor)
      9 -> SomInteractiveEulerBucklingCard(accentColor = accentColor)
    }
    "psad_structural_analysis" -> when (sectionIndex) {
      0 -> StructuralQuickDeterminacyCheckerCard(accentColor = accentColor)
      1 -> StructuralInteractiveBeamDeflectionCard(accentColor = accentColor)
      2 -> StructuralInteractiveFemCard(accentColor = accentColor)
      7 -> StructuralInteractiveInfluenceLineCard(accentColor = accentColor)
    }
    "psad_rcd_pcd" -> when (sectionIndex) {
      2 -> RcdWhitneyStressBlockCard(accentColor = accentColor)
      3 -> RcdTBeamEffectiveWidthCard(accentColor = accentColor)
      4 -> StrengthStaticCadFigureCard(
        figureTag = "CAD FIG • 05  |  DIAGONAL TENSION & STIRRUP SPACING",
        caption = "NSCP Sec. 422.5.10.5.3: V_s = (A_v · f_yt · d) / s  •  Critical V_u @ d from support",
        accentColor = accentColor,
        testTag = "rcd_cad_fig_05_shear"
      ) { cyan, amber, emerald, textCol ->
        drawRcdShearStirrupDiagram(accentColor, cyan, amber, emerald, textCol)
      }
      6 -> RcdColumnPmInteractionCard(accentColor = accentColor)
      7 -> RcdFootingShearPerimeterCard(accentColor = accentColor)
    }
    "psad_steel_design" -> when (sectionIndex) {
      1 -> SteelBlockShearStaggerCard(accentColor = accentColor)
      2 -> SteelColumnBucklingCurveCard(accentColor = accentColor)
      3 -> SteelBeamLtbCurveCard(accentColor = accentColor)
      6 -> StrengthStaticCadFigureCard(
        figureTag = "CAD FIG • 07  |  BOLT SHEAR & BEARING FBD",
        caption = "NSCP Sec. 510.3.10: R_n = 1.2 L_c t F_u ≤ 2.4 d_b t F_u  •  Hole = d_b + 2 mm",
        accentColor = accentColor,
        testTag = "steel_cad_fig_07_bolt"
      ) { cyan, amber, emerald, textCol ->
        drawSteelBoltBearingDiagram(accentColor, cyan, amber, emerald, textCol)
      }
    }
    "psad_foundation_engineering" -> when (sectionIndex) {
      1 -> FoundationGwtVisualizerCard(accentColor = accentColor)
      3 -> FoundationEccentricPressureCard(accentColor = accentColor)
      5 -> FoundationPileVisualizerCard(accentColor = accentColor)
      7 -> FoundationRetainingWallCard(accentColor = accentColor)
    }
    "hpge_hydraulics_fluid_mechanics" -> when (sectionIndex) {
      0 -> HydraulicsCapillaryCard(accentColor = accentColor)
      1 -> StrengthStaticCadFigureCard(
        figureTag = "CAD FIG • 02  |  U-TUBE DIFFERENTIAL MANOMETER",
        caption = "Jump-Across Pressure Balance: P_A + γ₁ h₁ - γ₂ h₂ - γ₃ h₃ = P_B",
        accentColor = accentColor,
        testTag = "hyd_cad_fig_02_manometer"
      ) { cyan, amber, emerald, textCol ->
        drawHydraulicsManometer(accentColor, cyan, amber, emerald, textCol)
      }
      2 -> HydraulicsSubmergedGateCard(accentColor = accentColor)
      3 -> HydraulicsMetacenterCard(accentColor = accentColor)
      4 -> HydraulicsTankMotionCard(accentColor = accentColor)
      8 -> HydraulicsOpenChannelCard(accentColor = accentColor)
    }
    "hpge_geotechnical_soil_mechanics" -> when (sectionIndex) {
      0 -> GeotechThreePhaseBlockCard(accentColor = accentColor)
      3 -> StrengthStaticCadFigureCard(
        figureTag = "CAD FIG • 04  |  PROCTOR COMPACTION & ZAV CURVE",
        caption = "Zero-Air-Void Line (S = 100%): γ_zav = (G_s · γ_w) / (1 + w · G_s)",
        accentColor = accentColor,
        testTag = "geo_cad_fig_04_compaction"
      ) { cyan, amber, emerald, textCol ->
        drawGeotechCompactionCurve(accentColor, cyan, amber, emerald, textCol)
      }
      4 -> GeotechPermeabilityFlowNetCard(accentColor = accentColor)
      8 -> GeotechMohrCoulombCard(accentColor = accentColor)
    }
    "hpge_hydrology" -> when (sectionIndex) {
      1 -> HydrologyPrecipitationCard(accentColor = accentColor)
      3 -> StrengthStaticCadFigureCard(
        figureTag = "CAD FIG • 04  |  STORM HYETOGRAPH & Φ-INDEX",
        caption = "Effective Rainfall ERH (i > Φ, Cyan) vs. Infiltration Loss (Amber): Φ = (P_eff - R_d) / t_e",
        accentColor = accentColor,
        testTag = "hydro_cad_fig_04_phi_index"
      ) { cyan, amber, emerald, textCol ->
        drawHydrologyHyetographPhi(accentColor, cyan, amber, emerald, textCol)
      }
      6 -> StrengthStaticCadFigureCard(
        figureTag = "CAD FIG • 07  |  FLOOD HYDROGRAPH & BASEFLOW SEPARATION",
        caption = "Rising Limb • Peak Q_p • Recession Limb • DRH Volume above Baseflow (N = 0.83 A^0.2)",
        accentColor = accentColor,
        testTag = "hydro_cad_fig_07_hydrograph"
      ) { cyan, amber, emerald, textCol ->
        drawHydrologyFloodHydrograph(accentColor, cyan, amber, emerald, textCol)
      }
      8 -> HydrologyWellConeCard(accentColor = accentColor)
    }
  }
}

// ============================================================================
// SHARED CAD BLUEPRINT SHELL
// ============================================================================

@Composable
private fun PsadCadCanvasShell(
  heightDp: Int = 186,
  accentColor: Color,
  topBadge: String,
  bottomFormula: String,
  testTag: String,
  tapHotspots: List<Pair<String, (Float, Float) -> Offset>> = emptyList(),
  onTapHotspot: ((String) -> Unit)? = null,
  onDraw: DrawScope.(cyan: Color, amber: Color, emerald: Color, textColor: Color) -> Unit
) {
  val palette = currentGlassPalette()
  var canvasSize by remember { mutableStateOf(IntSize.Zero) }
  val density = LocalDensity.current
  val hitRadiusPx = with(density) { 32.dp.toPx() }

  val canvasBg = if (palette.isDark) {
    Color(0xFF091224).copy(alpha = 0.92f)
  } else {
    Color(0xFFF0F7FF).copy(alpha = 0.96f)
  }
  val gridColor = if (palette.isDark) Color(0xFF38BDF8).copy(alpha = 0.10f) else Color(0xFF0284C7).copy(alpha = 0.10f)
  val cyanGlow = if (palette.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
  val amberColor = if (palette.isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
  val emeraldColor = if (palette.isDark) Color(0xFF34D399) else Color(0xFF059669)
  val textColor = if (palette.isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A)
  val orientation = rememberScreenOrientationState()
  val resolvedHeightDp = if (orientation.isCompactLandscape) {
    (heightDp * 0.85f).toInt().coerceAtLeast(150)
  } else {
    heightDp
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = canvasBg,
      border = BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(
          listOf(accentColor.copy(alpha = 0.45f), cyanGlow.copy(alpha = 0.30f))
        )
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(resolvedHeightDp.dp)
        .testTag(testTag)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .then(
              if (tapHotspots.isNotEmpty() && onTapHotspot != null) {
                Modifier.pointerInput(tapHotspots, canvasSize) {
                  detectTapGestures { tapOffset ->
                    val w = canvasSize.width.toFloat()
                    val h = canvasSize.height.toFloat()
                    if (w > 0f && h > 0f) {
                      val nearest = tapHotspots
                        .map { (id, fn) ->
                          val pt = fn(w, h)
                          id to hypot(tapOffset.x - pt.x, tapOffset.y - pt.y)
                        }
                        .filter { it.second <= hitRadiusPx }
                        .minByOrNull { it.second }
                      if (nearest != null) {
                        onTapHotspot(nearest.first)
                      }
                    }
                  }
                }
              } else {
                Modifier
              }
            )
        ) {
          drawBlueprintGrid(gridColor)
          onDraw(cyanGlow, amberColor, emeraldColor, textColor)
        }

        Surface(
          shape = CircleShape,
          color = canvasBg.copy(alpha = 0.88f),
          border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.36f)),
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            if (tapHotspots.isNotEmpty()) {
              Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = cyanGlow,
                modifier = Modifier.size(11.dp)
              )
            }
            Text(
              text = topBadge,
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
              ),
              color = accentColor
            )
          }
        }
      }
    }

    if (bottomFormula.isNotBlank()) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (palette.isDark) Color(0xFF0F172A).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.85f),
        border = BorderStroke(0.9.dp, accentColor.copy(alpha = 0.32f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = bottomFormula,
          style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
      }
    }
  }
}

@Composable
private fun StrengthStaticCadFigureCard(
  figureTag: String,
  caption: String,
  accentColor: Color,
  testTag: String,
  onDraw: DrawScope.(cyan: Color, amber: Color, emerald: Color, textColor: Color) -> Unit
) {
  PsadCadCanvasShell(
    heightDp = 174,
    accentColor = accentColor,
    topBadge = figureTag,
    bottomFormula = caption,
    testTag = testTag,
    onDraw = onDraw
  )
}

@Composable
private fun ModePillRow(
  labels: List<String>,
  selectedIndex: Int,
  accentColor: Color,
  testTagPrefix: String,
  onSelect: (Int) -> Unit
) {
  val palette = currentGlassPalette()
  val context = LocalContext.current
  val localView = LocalView.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    labels.forEachIndexed { idx, label ->
      val isSelected = idx == selectedIndex
      RecessedGlassPill(
        isDarkMode = palette.isDark,
        fillColor = if (isSelected) accentColor.copy(alpha = if (palette.isDark) 0.28f else 0.16f) else null,
        borderColor = if (isSelected) accentColor.copy(alpha = 0.72f) else null,
        onClick = {
          playCardHapticSound(context, localView)
          onSelect(idx)
        },
        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 6.dp),
        modifier = Modifier.testTag("${testTagPrefix}_$idx")
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = AppleSansFamily,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          ),
          color = if (isSelected) (if (palette.isDark) Color.White else accentColor) else palette.textSecondary
        )
      }
    }
  }
}

// ============================================================================
// STRENGTH OF MATERIALS — INTERACTIVE CAD FIGURES
// ============================================================================

@Composable
private fun SomInteractiveStressDistributionCard(accentColor: Color) {
  var selectedSection by remember { mutableIntStateOf(0) }
  val modes = listOf("Rectangular", "Circular", "Wide-Flange I-Beam")
  val bottomInfo = when (selectedSection) {
    1 -> "Circular: σ_max = Mc/I  |  τ_max = (4V)/(3A) at N.A."
    2 -> "I-Beam: Flanges carry ~90% Bending σ  |  Web carries ~95% Shear τ"
    else -> "Rectangular: σ_max = 6M/(bh²)  |  τ_max = 1.5(V/A) at N.A."
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = modes,
      selectedIndex = selectedSection,
      accentColor = accentColor,
      testTagPrefix = "som_stress_section_mode",
      onSelect = { selectedSection = it }
    )
    PsadCadCanvasShell(
      heightDp = 198,
      accentColor = accentColor,
      topBadge = "INTERACTIVE CROSS-SECTION • σ & τ PROFILES",
      bottomFormula = bottomInfo,
      testTag = "som_interactive_stress_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawSomChapter06StressProfile(selectedSection, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun SomInteractiveMohrsCircleCard(accentColor: Color) {
  val context = LocalContext.current
  val localView = LocalView.current
  val palette = currentGlassPalette()
  var selectedHotspot by remember { mutableStateOf("principal") }

  val hotspots = remember {
    listOf(
      "center" to { w: Float, h: Float -> Offset(w * 0.52f, h * 0.52f) },
      "principal" to { w: Float, h: Float -> Offset(w * 0.52f + h * 0.30f, h * 0.52f) },
      "tau_max" to { w: Float, h: Float -> Offset(w * 0.52f, h * 0.22f) },
      "angle" to { w: Float, h: Float -> Offset(w * 0.52f + h * 0.21f, h * 0.36f) }
    )
  }

  val pillSpecs = listOf(
    "center" to "Center C",
    "principal" to "Principal σ₁ / σ₂",
    "tau_max" to "Max Shear τ_max",
    "angle" to "Angle 2θ_p"
  )

  val detailText = when (selectedHotspot) {
    "center" -> "Center C = (σ_avg, 0)  where  σ_avg = (σ_x + σ_y) / 2"
    "tau_max" -> "Top/Bottom Peaks: τ_max = R = √[ ((σ_x - σ_y)/2)² + τ_xy² ]"
    "angle" -> "Double Rotation on Circle: tan(2θ_p) = 2τ_xy / (σ_x - σ_y)"
    else -> "Horizontal Intercepts: σ₁ = σ_avg + R  (Max)  •  σ₂ = σ_avg - R  (Min)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      pillSpecs.forEach { (id, label) ->
        val isSelected = selectedHotspot == id
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = if (isSelected) accentColor.copy(alpha = if (palette.isDark) 0.28f else 0.16f) else null,
          borderColor = if (isSelected) accentColor.copy(alpha = 0.72f) else null,
          onClick = {
            playCardHapticSound(context, localView)
            selectedHotspot = id
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("mohr_hotspot_pill_$id")
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = AppleSansFamily,
              fontSize = 10.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) (if (palette.isDark) Color.White else accentColor) else palette.textSecondary
          )
        }
      }
    }

    PsadCadCanvasShell(
      heightDp = 212,
      accentColor = accentColor,
      topBadge = "INTERACTIVE MOHR'S CIRCLE • TAP NODES",
      bottomFormula = detailText,
      testTag = "som_mohrs_circle_canvas",
      tapHotspots = hotspots,
      onTapHotspot = { id ->
        playCardHapticSound(context, localView)
        selectedHotspot = id
      }
    ) { cyan, amber, emerald, textCol ->
      drawSomChapter08MohrsCircle(selectedHotspot, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun SomInteractiveEulerBucklingCard(accentColor: Color) {
  var selectedCase by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "Pinned-Pinned K=1.0",
    "Fixed-Free K=2.0",
    "Fixed-Pinned K=0.7",
    "Fixed-Fixed K=0.5"
  )
  val formulaLine = when (selectedCase) {
    1 -> "Fixed-Free (Flagpole): K = 2.0  |  P_cr = π²EI / (2.0 L)² = π²EI / (4L²)"
    2 -> "Fixed-Pinned: K = 0.70  |  P_cr = π²EI / (0.70 L)² ≈ 2.04 π²EI / L²"
    3 -> "Fixed-Fixed: K = 0.50  |  P_cr = π²EI / (0.50 L)² = 4π²EI / L²"
    else -> "Pinned-Pinned: K = 1.0  |  P_cr = π²EI / L²  (Fundamental Euler Mode)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedCase,
      accentColor = accentColor,
      testTagPrefix = "som_buckling_k_mode",
      onSelect = { selectedCase = it }
    )
    PsadCadCanvasShell(
      heightDp = 196,
      accentColor = accentColor,
      topBadge = "INTERACTIVE EULER K-FACTOR DIAGRAM",
      bottomFormula = formulaLine,
      testTag = "som_euler_buckling_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawSomChapter10Buckling(selectedCase, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

// ============================================================================
// STRUCTURAL ANALYSIS — UNIQUE QUICK DETERMINACY CHECKER + INTERACTIVE LABS
// ============================================================================

@Composable
private fun StructuralQuickDeterminacyCheckerCard(accentColor: Color) {
  val palette = currentGlassPalette()
  val context = LocalContext.current
  val localView = LocalView.current

  var isFrameMode by remember { mutableStateOf(false) } // false = Planar Truss, true = Planar Frame
  var members by remember { mutableIntStateOf(5) }
  var reactions by remember { mutableIntStateOf(3) }
  var joints by remember { mutableIntStateOf(4) }
  var hinges by remember { mutableIntStateOf(0) }

  val lhs = if (isFrameMode) (3 * members + reactions) else (members + reactions)
  val rhs = if (isFrameMode) (3 * joints + hinges) else (2 * joints)
  val diff = lhs - rhs

  val statusText = when {
    diff < 0 -> "Unstable (${-diff} Missing Constraint${if (diff == -1) "" else "s"})"
    diff == 0 -> "Statically Determinate (D_s = 0)"
    else -> "Indeterminate ($diff Degree${if (diff == 1) "" else "s"})"
  }
  val statusColor = when {
    diff < 0 -> Color(0xFFEF4444)
    diff == 0 -> Color(0xFF10B981)
    else -> Color(0xFFF59E0B)
  }

  val equationSummary = if (isFrameMode) {
    "3m + r = 3($members) + $reactions = $lhs   vs   3j + c = 3($joints) + $hinges = $rhs"
  } else {
    "b + r = $members + $reactions = $lhs   vs   2j = 2($joints) = $rhs"
  }

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = if (palette.isDark) Color(0xFF091224).copy(alpha = 0.88f) else Color(0xFFF8FAFC).copy(alpha = 0.96f),
    border = BorderStroke(1.1.dp, accentColor.copy(alpha = 0.42f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("sa_quick_determinacy_checker")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "QUICK DETERMINACY CHECKER",
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
          ),
          color = accentColor
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf(false to "Planar Truss", true to "Planar Frame").forEach { (frameOpt, label) ->
            val active = isFrameMode == frameOpt
            RecessedGlassPill(
              isDarkMode = palette.isDark,
              fillColor = if (active) accentColor.copy(alpha = if (palette.isDark) 0.28f else 0.16f) else null,
              borderColor = if (active) accentColor.copy(alpha = 0.72f) else null,
              onClick = {
                playCardHapticSound(context, localView)
                isFrameMode = frameOpt
                if (!frameOpt) {
                  members = 5; reactions = 3; joints = 4; hinges = 0
                } else {
                  members = 3; reactions = 4; joints = 4; hinges = 0
                }
              },
              contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
              modifier = Modifier.testTag("det_mode_${if (frameOpt) "frame" else "truss"}")
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = AppleSansFamily,
                  fontSize = 10.5.sp,
                  fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (active) (if (palette.isDark) Color.White else accentColor) else palette.textSecondary
              )
            }
          }
        }
      }

      // Stepper Grid (2 columns)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DeterminacyStepperBox(
          label = if (isFrameMode) "Members (m)" else "Members (b)",
          value = members,
          accentColor = accentColor,
          onDecrement = { if (members > 1) members-- },
          onIncrement = { if (members < 99) members++ },
          modifier = Modifier.weight(1f),
          testTag = "det_step_members"
        )
        DeterminacyStepperBox(
          label = "Reactions (r)",
          value = reactions,
          accentColor = accentColor,
          onDecrement = { if (reactions > 0) reactions-- },
          onIncrement = { if (reactions < 40) reactions++ },
          modifier = Modifier.weight(1f),
          testTag = "det_step_reactions"
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DeterminacyStepperBox(
          label = "Joints (j)",
          value = joints,
          accentColor = accentColor,
          onDecrement = { if (joints > 2) joints-- },
          onIncrement = { if (joints < 60) joints++ },
          modifier = Modifier.weight(1f),
          testTag = "det_step_joints"
        )
        if (isFrameMode) {
          DeterminacyStepperBox(
            label = "Hinges (c)",
            value = hinges,
            accentColor = accentColor,
            onDecrement = { if (hinges > 0) hinges-- },
            onIncrement = { if (hinges < 30) hinges++ },
            modifier = Modifier.weight(1f),
            testTag = "det_step_hinges"
          )
        } else {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.nestedCardFill,
            border = palette.glassBorder,
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = "2j Constraint",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = AppleSansFamily,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.SemiBold
                ),
                color = palette.textSecondary
              )
              Text(
                text = "2($joints) = $rhs",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = AppleSansFamily,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  fontFeatureSettings = "tnum"
                ),
                color = palette.textPrimary
              )
            }
          }
        }
      }

      // Live Result Status Banner
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = statusColor.copy(alpha = if (palette.isDark) 0.16f else 0.11f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.50f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Text(
            text = statusText,
            style = MaterialTheme.typography.titleSmall.copy(
              fontFamily = AppleSansFamily,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Bold,
              fontFeatureSettings = "tnum"
            ),
            color = statusColor
          )
          Text(
            text = equationSummary,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = JetBrainsMonoFamily,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              fontFeatureSettings = "tnum"
            ),
            color = palette.textPrimary
          )
        }
      }
    }
  }
}

@Composable
private fun DeterminacyStepperBox(
  label: String,
  value: Int,
  accentColor: Color,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String
) {
  val palette = currentGlassPalette()
  val context = LocalContext.current
  val localView = LocalView.current

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = palette.nestedCardFill,
    border = palette.glassBorder,
    modifier = modifier.testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = AppleSansFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
          ),
          color = palette.textSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = value.toString(),
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = AppleSansFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFeatureSettings = "tnum"
          ),
          color = palette.textPrimary
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
          onClick = {
            playCardHapticSound(context, localView)
            onDecrement()
          },
          shape = CircleShape,
          color = accentColor.copy(alpha = 0.14f),
          border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f)),
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(30.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Remove,
              contentDescription = "Decrease $label",
              tint = accentColor,
              modifier = Modifier.size(15.dp)
            )
          }
        }
        Surface(
          onClick = {
            playCardHapticSound(context, localView)
            onIncrement()
          },
          shape = CircleShape,
          color = accentColor.copy(alpha = 0.14f),
          border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f)),
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(30.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Increase $label",
              tint = accentColor,
              modifier = Modifier.size(15.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StructuralInteractiveBeamDeflectionCard(accentColor: Color) {
  var selectedCase by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "Simple • Center P",
    "Simple • UDL w",
    "Simple • Eccentric P",
    "Cantilever • Tip P",
    "Cantilever • UDL w"
  )
  val formulaLine = when (selectedCase) {
    1 -> "Δ_max = 5wL⁴ / (384EI)   •   θ_max = wL³ / (24EI)"
    2 -> "Δ_P = Pa²b² / (3EIL)   •   Δ_max = Pb(L² - b²)^(3/2) / (9√3 EIL)"
    3 -> "Δ_max = PL³ / (3EI)   •   θ_max = PL² / (2EI)"
    4 -> "Δ_max = wL⁴ / (8EI)   •   θ_max = wL³ / (6EI)"
    else -> "Δ_max = PL³ / (48EI)   •   θ_max = PL² / (16EI)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedCase,
      accentColor = accentColor,
      testTagPrefix = "sa_beam_deflection_mode",
      onSelect = { selectedCase = it }
    )
    PsadCadCanvasShell(
      heightDp = 186,
      accentColor = accentColor,
      topBadge = "INTERACTIVE BEAM DEFLECTION BLUEPRINT",
      bottomFormula = formulaLine,
      testTag = "sa_beam_deflection_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawSaBeamDeflection(selectedCase, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun StructuralInteractiveFemCard(accentColor: Color) {
  var selectedCase by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "UDL w",
    "Center P",
    "Eccentric P",
    "Triangular w",
    "Settlement Δ"
  )
  val formulaLine = when (selectedCase) {
    1 -> "FEM_AB = -PL / 8   •   FEM_BA = +PL / 8"
    2 -> "FEM_AB = -P·a·b² / L²   •   FEM_BA = +P·a²·b / L²"
    3 -> "FEM_AB = -wL² / 30 (0 end)   •   FEM_BA = +wL² / 20 (w end)"
    4 -> "Settlement Δ: FEM_AB = FEM_BA = ± 6EIΔ / L²"
    else -> "FEM_AB = -wL² / 12   •   FEM_BA = +wL² / 12"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedCase,
      accentColor = accentColor,
      testTagPrefix = "sa_fem_mode",
      onSelect = { selectedCase = it }
    )
    PsadCadCanvasShell(
      heightDp = 186,
      accentColor = accentColor,
      topBadge = "INTERACTIVE FIXED-END MOMENT (FEM) LAB",
      bottomFormula = formulaLine,
      testTag = "sa_fem_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawSaFemDiagram(selectedCase, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun StructuralInteractiveInfluenceLineCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf("Reaction R_A", "Shear at C", "Moment at C")
  val formulaLine = when (selectedMode) {
    1 -> "IL V_C: -a/L (left) to +b/L (right)  •  Total Vertical Jump = 1.0"
    2 -> "IL M_C: Peak ordinate = (a·b) / L at C  •  Max M = Area_IL × w_UDL"
    else -> "IL R_A: Unit ordinate 1.0 at A, linear drop to 0.0 at B"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "sa_influence_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 186,
      accentColor = accentColor,
      topBadge = "INTERACTIVE INFLUENCE LINE • MÜLLER-BRESLAU",
      bottomFormula = formulaLine,
      testTag = "sa_influence_line_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawSaInfluenceLine(selectedMode, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

// ============================================================================
// CAD CANVAS DRAWING PRIMITIVES
// ============================================================================

private fun DrawScope.drawBlueprintGrid(gridColor: Color) {
  val step = 20.dp.toPx()
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

private fun DrawScope.cadLabel(
  text: String,
  x: Float,
  y: Float,
  color: Color,
  sizeSp: Float = 11f,
  align: Paint.Align = Paint.Align.CENTER,
  bold: Boolean = true
) {
  val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color.toArgb()
    textSize = sizeSp.sp.toPx()
    textAlign = align
    typeface = if (bold) Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) else Typeface.MONOSPACE
  }
  drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

private fun DrawScope.drawArrow(
  start: Offset,
  end: Offset,
  color: Color,
  strokePx: Float = 2.2.dp.toPx(),
  headLenPx: Float = 8.dp.toPx()
) {
  drawLine(color, start, end, strokeWidth = strokePx, cap = StrokeCap.Round)
  val dx = end.x - start.x
  val dy = end.y - start.y
  val angle = kotlin.math.atan2(dy.toDouble(), dx.toDouble())
  val p1 = Offset(
    (end.x - headLenPx * cos(angle - 0.45)).toFloat(),
    (end.y - headLenPx * sin(angle - 0.45)).toFloat()
  )
  val p2 = Offset(
    (end.x - headLenPx * cos(angle + 0.45)).toFloat(),
    (end.y - headLenPx * sin(angle + 0.45)).toFloat()
  )
  drawLine(color, end, p1, strokeWidth = strokePx, cap = StrokeCap.Round)
  drawLine(color, end, p2, strokeWidth = strokePx, cap = StrokeCap.Round)
}

private fun DrawScope.drawPinSupport(top: Offset, color: Color) {
  val s = 11.dp.toPx()
  val path = Path().apply {
    moveTo(top.x, top.y)
    lineTo(top.x - s * 0.72f, top.y + s)
    lineTo(top.x + s * 0.72f, top.y + s)
    close()
  }
  drawPath(path, color = color.copy(alpha = 0.25f))
  drawPath(path, color = color, style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round))
  drawLine(color, Offset(top.x - s, top.y + s), Offset(top.x + s, top.y + s), strokeWidth = 1.8.dp.toPx())
}

private fun DrawScope.drawRollerSupport(top: Offset, color: Color) {
  val r = 5.5.dp.toPx()
  drawCircle(color.copy(alpha = 0.25f), radius = r, center = Offset(top.x, top.y + r))
  drawCircle(color, radius = r, center = Offset(top.x, top.y + r), style = Stroke(1.8.dp.toPx()))
  drawLine(color, Offset(top.x - r * 1.6f, top.y + r * 2.1f), Offset(top.x + r * 1.6f, top.y + r * 2.1f), strokeWidth = 1.8.dp.toPx())
}

private fun DrawScope.drawFixedWall(x: Float, yCenter: Float, halfHeight: Float, isLeftWall: Boolean, color: Color) {
  drawLine(color, Offset(x, yCenter - halfHeight), Offset(x, yCenter + halfHeight), strokeWidth = 2.6.dp.toPx(), cap = StrokeCap.Round)
  val dir = if (isLeftWall) -1f else 1f
  val hatch = 7.dp.toPx()
  var y = yCenter - halfHeight + 4.dp.toPx()
  while (y <= yCenter + halfHeight - 2.dp.toPx()) {
    drawLine(color.copy(alpha = 0.65f), Offset(x, y), Offset(x + dir * hatch, y + hatch * 0.65f), strokeWidth = 1.3.dp.toPx())
    y += 7.dp.toPx()
  }
}

private fun DrawScope.drawSomChapter01Fbd(
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height

  // Left: Axial Member FBD (σ = P/A)
  val barTop = h * 0.38f
  val barH = h * 0.24f
  val barLeft = w * 0.14f
  val barW = w * 0.28f
  drawRect(cyan.copy(alpha = 0.18f), Offset(barLeft, barTop), Size(barW, barH))
  drawRect(cyan, Offset(barLeft, barTop), Size(barW, barH), style = Stroke(2.dp.toPx()))
  // Section cut dashed line
  drawLine(
    amber,
    Offset(barLeft + barW * 0.5f, barTop - 8.dp.toPx()),
    Offset(barLeft + barW * 0.5f, barTop + barH + 8.dp.toPx()),
    strokeWidth = 1.6.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
  )
  drawArrow(Offset(barLeft, barTop + barH / 2), Offset(barLeft - w * 0.08f, barTop + barH / 2), amber)
  drawArrow(Offset(barLeft + barW, barTop + barH / 2), Offset(barLeft + barW + w * 0.08f, barTop + barH / 2), amber)
  cadLabel("P", barLeft - w * 0.09f, barTop + barH / 2 - 8.dp.toPx(), amber)
  cadLabel("P", barLeft + barW + w * 0.09f, barTop + barH / 2 - 8.dp.toPx(), amber)
  cadLabel("σ = P/A", barLeft + barW * 0.5f, h * 0.80f, textCol, sizeSp = 10.5f)

  // Right: Bolt Bearing Plate + Thin-Wall Hoop Stress
  val plateLeft = w * 0.58f
  val plateW = w * 0.30f
  drawRect(emerald.copy(alpha = 0.16f), Offset(plateLeft, h * 0.32f), Size(plateW, h * 0.32f))
  drawRect(emerald, Offset(plateLeft, h * 0.32f), Size(plateW, h * 0.32f), style = Stroke(1.8.dp.toPx()))
  val holeCx = plateLeft + plateW * 0.38f
  val holeCy = h * 0.48f
  val holeR = h * 0.10f
  drawCircle(amber.copy(alpha = 0.32f), radius = holeR, center = Offset(holeCx, holeCy))
  drawCircle(amber, radius = holeR, center = Offset(holeCx, holeCy), style = Stroke(2.dp.toPx()))
  drawArrow(Offset(holeCx + holeR + 2.dp.toPx(), holeCy), Offset(plateLeft + plateW + w * 0.06f, holeCy), amber)
  cadLabel("A_b = t·d", plateLeft + plateW * 0.5f, h * 0.80f, textCol, sizeSp = 10.5f)
  cadLabel("σ_t = pD/2t", w * 0.73f, h * 0.24f, cyan, sizeSp = 10f)
}

private fun DrawScope.drawSomChapter02Shear(
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height

  // 1. Single Shear (Left)
  drawRect(cyan.copy(alpha = 0.18f), Offset(w * 0.07f, h * 0.34f), Size(w * 0.18f, h * 0.14f))
  drawRect(cyan, Offset(w * 0.07f, h * 0.34f), Size(w * 0.18f, h * 0.14f), style = Stroke(1.6.dp.toPx()))
  drawRect(cyan.copy(alpha = 0.18f), Offset(w * 0.15f, h * 0.48f), Size(w * 0.18f, h * 0.14f))
  drawRect(cyan, Offset(w * 0.15f, h * 0.48f), Size(w * 0.18f, h * 0.14f), style = Stroke(1.6.dp.toPx()))
  // Bolt
  drawRect(amber.copy(alpha = 0.75f), Offset(w * 0.185f, h * 0.30f), Size(w * 0.032f, h * 0.36f))
  // 1 shear plane marker
  drawLine(Color(0xFFEF4444), Offset(w * 0.13f, h * 0.48f), Offset(w * 0.27f, h * 0.48f), strokeWidth = 2.4.dp.toPx())
  cadLabel("SINGLE (1A)", w * 0.20f, h * 0.80f, textCol, sizeSp = 10f)

  // 2. Double Shear (Middle)
  drawRect(emerald.copy(alpha = 0.18f), Offset(w * 0.40f, h * 0.30f), Size(w * 0.18f, h * 0.10f))
  drawRect(emerald, Offset(w * 0.40f, h * 0.30f), Size(w * 0.18f, h * 0.10f), style = Stroke(1.5.dp.toPx()))
  drawRect(cyan.copy(alpha = 0.20f), Offset(w * 0.44f, h * 0.40f), Size(w * 0.18f, h * 0.12f))
  drawRect(cyan, Offset(w * 0.44f, h * 0.40f), Size(w * 0.18f, h * 0.12f), style = Stroke(1.5.dp.toPx()))
  drawRect(emerald.copy(alpha = 0.18f), Offset(w * 0.40f, h * 0.52f), Size(w * 0.18f, h * 0.10f))
  drawRect(emerald, Offset(w * 0.40f, h * 0.52f), Size(w * 0.18f, h * 0.10f), style = Stroke(1.5.dp.toPx()))
  drawRect(amber.copy(alpha = 0.75f), Offset(w * 0.495f, h * 0.27f), Size(w * 0.032f, h * 0.39f))
  drawLine(Color(0xFFEF4444), Offset(w * 0.43f, h * 0.40f), Offset(w * 0.59f, h * 0.40f), strokeWidth = 2.2.dp.toPx())
  drawLine(Color(0xFFEF4444), Offset(w * 0.43f, h * 0.52f), Offset(w * 0.59f, h * 0.52f), strokeWidth = 2.2.dp.toPx())
  cadLabel("DOUBLE (2A)", w * 0.51f, h * 0.80f, textCol, sizeSp = 10f)

  // 3. Punching Shear (Right)
  drawRect(cyan.copy(alpha = 0.16f), Offset(w * 0.70f, h * 0.46f), Size(w * 0.24f, h * 0.16f))
  drawRect(cyan, Offset(w * 0.70f, h * 0.46f), Size(w * 0.24f, h * 0.16f), style = Stroke(1.6.dp.toPx()))
  drawRect(amber.copy(alpha = 0.30f), Offset(w * 0.785f, h * 0.30f), Size(w * 0.07f, h * 0.32f))
  drawRect(amber, Offset(w * 0.785f, h * 0.30f), Size(w * 0.07f, h * 0.32f), style = Stroke(1.6.dp.toPx()))
  drawArrow(Offset(w * 0.82f, h * 0.18f), Offset(w * 0.82f, h * 0.29f), amber)
  cadLabel("PUNCH (πdt)", w * 0.82f, h * 0.80f, textCol, sizeSp = 10f)
}

private fun DrawScope.drawSomChapter04Torsion(
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height

  // Left: 3D Shaft with Torque T
  val wallX = w * 0.12f
  drawFixedWall(wallX, h * 0.52f, h * 0.24f, isLeftWall = true, color = textCol.copy(alpha = 0.7f))
  drawRect(cyan.copy(alpha = 0.18f), Offset(wallX, h * 0.38f), Size(w * 0.36f, h * 0.28f))
  drawRect(cyan, Offset(wallX, h * 0.38f), Size(w * 0.36f, h * 0.28f), style = Stroke(2.dp.toPx()))
  drawLine(amber, Offset(wallX, h * 0.52f), Offset(wallX + w * 0.36f, h * 0.52f), strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
  // Twist angle line
  drawLine(emerald, Offset(wallX, h * 0.38f), Offset(wallX + w * 0.36f, h * 0.44f), strokeWidth = 2.dp.toPx())
  cadLabel("θ = TL/JG", wallX + w * 0.18f, h * 0.82f, emerald, sizeSp = 10.5f)
  cadLabel("T", w * 0.51f, h * 0.36f, amber, sizeSp = 12f)

  // Right: Circular Cross-Section Linear Shear Distribution
  val cx = w * 0.76f
  val cy = h * 0.52f
  val r = h * 0.26f
  drawCircle(cyan.copy(alpha = 0.14f), radius = r, center = Offset(cx, cy))
  drawCircle(cyan, radius = r, center = Offset(cx, cy), style = Stroke(2.dp.toPx()))
  drawLine(textCol.copy(alpha = 0.45f), Offset(cx - r * 1.15f, cy), Offset(cx + r * 1.15f, cy), strokeWidth = 1.2.dp.toPx())
  // Radial distribution arrows
  for (frac in listOf(0.35f, 0.68f, 1.0f)) {
    val yOff = cy - r * frac
    drawArrow(Offset(cx, yOff), Offset(cx + r * 0.72f * frac, yOff), amber, strokePx = 1.8.dp.toPx(), headLenPx = 5.dp.toPx())
  }
  drawLine(amber, Offset(cx, cy), Offset(cx + r * 0.72f, cy - r), strokeWidth = 1.6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)))
  cadLabel("τ_max = Tr/J", cx, h * 0.90f, textCol, sizeSp = 10.5f)
}

private fun DrawScope.drawSomChapter05SfdBmd(
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val x0 = w * 0.14f
  val x1 = w * 0.86f
  val xm = (x0 + x1) * 0.5f

  // 1. Beam + UDL w(x)
  val beamY = h * 0.27f
  drawLine(textCol, Offset(x0, beamY), Offset(x1, beamY), strokeWidth = 2.6.dp.toPx())
  for (i in 0..6) {
    val ax = x0 + (x1 - x0) * (i / 6f)
    drawArrow(Offset(ax, h * 0.13f), Offset(ax, beamY - 3.dp.toPx()), amber, strokePx = 1.5.dp.toPx(), headLenPx = 4.5.dp.toPx())
  }
  drawLine(amber, Offset(x0, h * 0.13f), Offset(x1, h * 0.13f), strokeWidth = 1.8.dp.toPx())
  cadLabel("w(x)", w * 0.07f, h * 0.23f, amber, sizeSp = 10f)

  // 2. Shear V(x)
  val vZeroY = h * 0.52f
  drawLine(textCol.copy(alpha = 0.45f), Offset(x0, vZeroY), Offset(x1, vZeroY), strokeWidth = 1.2.dp.toPx())
  val sfdPath = Path().apply {
    moveTo(x0, vZeroY - h * 0.10f)
    lineTo(x1, vZeroY + h * 0.10f)
    lineTo(x1, vZeroY)
    lineTo(x0, vZeroY)
    close()
  }
  drawPath(sfdPath, color = cyan.copy(alpha = 0.22f))
  drawLine(cyan, Offset(x0, vZeroY - h * 0.10f), Offset(x1, vZeroY + h * 0.10f), strokeWidth = 2.2.dp.toPx())
  cadLabel("V(x)", w * 0.07f, vZeroY + 4.dp.toPx(), cyan, sizeSp = 10f)

  // 3. Moment M(x)
  val mZeroY = h * 0.85f
  drawLine(textCol.copy(alpha = 0.45f), Offset(x0, mZeroY), Offset(x1, mZeroY), strokeWidth = 1.2.dp.toPx())
  val bmdPath = Path().apply {
    moveTo(x0, mZeroY)
    quadraticTo(xm, mZeroY - h * 0.30f, x1, mZeroY)
    close()
  }
  drawPath(bmdPath, color = emerald.copy(alpha = 0.22f))
  val bmdStroke = Path().apply {
    moveTo(x0, mZeroY)
    quadraticTo(xm, mZeroY - h * 0.30f, x1, mZeroY)
  }
  drawPath(bmdStroke, color = emerald, style = Stroke(2.2.dp.toPx()))
  // Vertical dashed link V=0 -> M_max
  drawLine(amber.copy(alpha = 0.7f), Offset(xm, vZeroY), Offset(xm, mZeroY - h * 0.15f), strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
  drawCircle(amber, radius = 3.5.dp.toPx(), center = Offset(xm, mZeroY - h * 0.15f))
  cadLabel("M_max", xm, h * 0.96f, emerald, sizeSp = 10f)
}

private fun DrawScope.drawSomChapter06StressProfile(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val topY = h * 0.22f
  val botY = h * 0.78f
  val midY = (topY + botY) * 0.5f
  val secH = botY - topY

  // Neutral Axis dashed line across all 3 columns
  drawLine(
    textCol.copy(alpha = 0.4f),
    Offset(w * 0.06f, midY),
    Offset(w * 0.94f, midY),
    strokeWidth = 1.2.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
  )

  // Column 1: Cross-Section Geometry
  val secCx = w * 0.20f
  when (mode) {
    1 -> {
      val rad = secH * 0.5f
      drawCircle(cyan.copy(alpha = 0.18f), radius = rad, center = Offset(secCx, midY))
      drawCircle(cyan, radius = rad, center = Offset(secCx, midY), style = Stroke(2.dp.toPx()))
    }
    2 -> {
      val flW = w * 0.18f
      val flH = secH * 0.15f
      val webW = w * 0.045f
      val iPath = Path().apply {
        moveTo(secCx - flW / 2, topY)
        lineTo(secCx + flW / 2, topY)
        lineTo(secCx + flW / 2, topY + flH)
        lineTo(secCx + webW / 2, topY + flH)
        lineTo(secCx + webW / 2, botY - flH)
        lineTo(secCx + flW / 2, botY - flH)
        lineTo(secCx + flW / 2, botY)
        lineTo(secCx - flW / 2, botY)
        lineTo(secCx - flW / 2, botY - flH)
        lineTo(secCx - webW / 2, botY - flH)
        lineTo(secCx - webW / 2, topY + flH)
        lineTo(secCx - flW / 2, topY + flH)
        close()
      }
      drawPath(iPath, cyan.copy(alpha = 0.20f))
      drawPath(iPath, cyan, style = Stroke(2.dp.toPx()))
    }
    else -> {
      val rectW = w * 0.15f
      drawRect(cyan.copy(alpha = 0.18f), Offset(secCx - rectW / 2, topY), Size(rectW, secH))
      drawRect(cyan, Offset(secCx - rectW / 2, topY), Size(rectW, secH), style = Stroke(2.dp.toPx()))
    }
  }
  cadLabel("SECTION", secCx, h * 0.90f, textCol, sizeSp = 10f)

  // Column 2: Linear Bending Stress (σ = My/I)
  val sigCx = w * 0.53f
  val sigSpan = w * 0.10f
  drawLine(textCol.copy(alpha = 0.65f), Offset(sigCx, topY), Offset(sigCx, botY), strokeWidth = 1.6.dp.toPx())
  val compPath = Path().apply {
    moveTo(sigCx, topY)
    lineTo(sigCx - sigSpan, topY)
    lineTo(sigCx, midY)
    close()
  }
  val tensPath = Path().apply {
    moveTo(sigCx, midY)
    lineTo(sigCx + sigSpan, botY)
    lineTo(sigCx, botY)
    close()
  }
  drawPath(compPath, amber.copy(alpha = 0.26f))
  drawPath(tensPath, emerald.copy(alpha = 0.26f))
  drawLine(amber, Offset(sigCx - sigSpan, topY), Offset(sigCx + sigSpan, botY), strokeWidth = 2.2.dp.toPx())
  cadLabel("-σ (C)", sigCx - sigSpan * 0.5f, topY - 6.dp.toPx(), amber, sizeSp = 9.5f)
  cadLabel("+σ (T)", sigCx + sigSpan * 0.5f, botY + 13.dp.toPx(), emerald, sizeSp = 9.5f)
  cadLabel("BENDING σ", sigCx, h * 0.95f, textCol, sizeSp = 9.5f)

  // Column 3: Shear Stress (τ = VQ/Ib)
  val tauX0 = w * 0.77f
  val tauPeak = w * 0.13f
  drawLine(textCol.copy(alpha = 0.65f), Offset(tauX0, topY), Offset(tauX0, botY), strokeWidth = 1.6.dp.toPx())
  val tauPath = Path()
  if (mode == 2) {
    // Wide-flange: jump at flange-web junction
    val jTop = topY + secH * 0.15f
    val jBot = botY - secH * 0.15f
    tauPath.apply {
      moveTo(tauX0, topY)
      lineTo(tauX0 + tauPeak * 0.18f, jTop)
      lineTo(tauX0 + tauPeak * 0.85f, jTop)
      quadraticTo(tauX0 + tauPeak * 1.15f, midY, tauX0 + tauPeak * 0.85f, jBot)
      lineTo(tauX0 + tauPeak * 0.18f, jBot)
      lineTo(tauX0, botY)
    }
  } else {
    tauPath.apply {
      moveTo(tauX0, topY)
      quadraticTo(tauX0 + tauPeak * 1.55f, midY, tauX0, botY)
    }
  }
  drawPath(tauPath, accent.copy(alpha = 0.24f))
  drawPath(tauPath, accent, style = Stroke(2.2.dp.toPx()))
  drawArrow(Offset(tauX0, midY), Offset(tauX0 + tauPeak * (if (mode == 2) 1.0f else 0.78f), midY), accent, strokePx = 1.8.dp.toPx(), headLenPx = 5.dp.toPx())
  cadLabel("SHEAR τ", tauX0 + tauPeak * 0.35f, h * 0.95f, accent, sizeSp = 9.5f)
}

private fun DrawScope.drawSomChapter08MohrsCircle(
  selectedHotspot: String,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val cx = w * 0.52f
  val cy = h * 0.52f
  val r = h * 0.30f

  // σ and τ Axes
  drawLine(textCol.copy(alpha = 0.55f), Offset(w * 0.14f, cy), Offset(w * 0.90f, cy), strokeWidth = 1.6.dp.toPx())
  drawLine(textCol.copy(alpha = 0.55f), Offset(w * 0.24f, h * 0.12f), Offset(w * 0.24f, h * 0.92f), strokeWidth = 1.6.dp.toPx())
  cadLabel("+σ", w * 0.92f, cy + 4.dp.toPx(), textCol, sizeSp = 10f)
  cadLabel("+τ", w * 0.24f, h * 0.10f, textCol, sizeSp = 10f)

  // Circle
  drawCircle(cyan.copy(alpha = 0.12f), radius = r, center = Offset(cx, cy))
  drawCircle(cyan, radius = r, center = Offset(cx, cy), style = Stroke(2.2.dp.toPx()))

  // State point X(σ_x, τ_xy) rotated 42° above σ axis
  val thetaRad = (42.0 * PI / 180.0)
  val ptX = Offset((cx + r * cos(thetaRad)).toFloat(), (cy - r * sin(thetaRad)).toFloat())
  val ptY = Offset((cx - r * cos(thetaRad)).toFloat(), (cy + r * sin(thetaRad)).toFloat())
  drawLine(textCol.copy(alpha = 0.5f), ptY, ptX, strokeWidth = 1.6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)))
  drawCircle(textCol, radius = 3.5.dp.toPx(), center = ptX)
  cadLabel("X(σ_x,τ)", ptX.x + 28.dp.toPx(), ptX.y - 4.dp.toPx(), textCol, sizeSp = 9.5f)

  // Hotspot Highlights
  if (selectedHotspot == "angle") {
    drawArc(
      color = amber,
      startAngle = -42f,
      sweepAngle = 42f,
      useCenter = false,
      topLeft = Offset(cx - r * 0.42f, cy - r * 0.42f),
      size = Size(r * 0.84f, r * 0.84f),
      style = Stroke(width = 2.8.dp.toPx())
    )
    drawLine(amber, Offset(cx, cy), ptX, strokeWidth = 2.4.dp.toPx())
    cadLabel("2θ_p", cx + r * 0.55f, cy - r * 0.18f, amber, sizeSp = 11f)
  }

  // Center C
  val cColor = if (selectedHotspot == "center") amber else cyan
  drawCircle(cColor, radius = if (selectedHotspot == "center") 6.dp.toPx() else 4.dp.toPx(), center = Offset(cx, cy))
  cadLabel("C", cx - 10.dp.toPx(), cy + 14.dp.toPx(), cColor, sizeSp = 10.5f)

  // Principal σ1, σ2
  val pColor = if (selectedHotspot == "principal") amber else emerald
  val pRadius = if (selectedHotspot == "principal") 6.dp.toPx() else 4.dp.toPx()
  drawCircle(pColor, radius = pRadius, center = Offset(cx + r, cy))
  drawCircle(pColor, radius = pRadius, center = Offset(cx - r, cy))
  cadLabel("σ₁", cx + r + 12.dp.toPx(), cy + 14.dp.toPx(), pColor, sizeSp = 11f)
  cadLabel("σ₂", cx - r - 12.dp.toPx(), cy + 14.dp.toPx(), pColor, sizeSp = 11f)

  // Max Shear τ_max
  val tColor = if (selectedHotspot == "tau_max") amber else accent
  val tRadius = if (selectedHotspot == "tau_max") 6.dp.toPx() else 4.dp.toPx()
  if (selectedHotspot == "tau_max") {
    drawLine(tColor, Offset(cx, cy), Offset(cx, cy - r), strokeWidth = 2.2.dp.toPx())
  }
  drawCircle(tColor, radius = tRadius, center = Offset(cx, cy - r))
  drawCircle(tColor, radius = tRadius, center = Offset(cx, cy + r))
  cadLabel("+τ_max (R)", cx, cy - r - 8.dp.toPx(), tColor, sizeSp = 10f)
}

private fun DrawScope.drawSomChapter10Buckling(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val colX = w * 0.42f
  val topY = h * 0.22f
  val botY = h * 0.84f
  val len = botY - topY

  // Unbuckled reference axis
  drawLine(textCol.copy(alpha = 0.40f), Offset(colX, topY), Offset(colX, botY), strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
  // Load P_cr arrow at top
  drawArrow(Offset(colX, h * 0.08f), Offset(colX, topY - 2.dp.toPx()), amber)
  cadLabel("P_cr", colX + 20.dp.toPx(), h * 0.14f, amber, sizeSp = 10.5f)

  // Support symbols
  when (mode) {
    1 -> { // Fixed-Free
      drawFixedWall(colX, botY, 22.dp.toPx(), isLeftWall = false, color = textCol)
      drawLine(textCol, Offset(colX - 20.dp.toPx(), botY), Offset(colX + 20.dp.toPx(), botY), strokeWidth = 2.6.dp.toPx())
    }
    2 -> { // Fixed-Pinned
      drawPinSupport(Offset(colX, topY), textCol)
      drawLine(textCol, Offset(colX - 20.dp.toPx(), botY), Offset(colX + 20.dp.toPx(), botY), strokeWidth = 2.6.dp.toPx())
    }
    3 -> { // Fixed-Fixed
      drawLine(textCol, Offset(colX - 20.dp.toPx(), topY), Offset(colX + 20.dp.toPx(), topY), strokeWidth = 2.6.dp.toPx())
      drawLine(textCol, Offset(colX - 20.dp.toPx(), botY), Offset(colX + 20.dp.toPx(), botY), strokeWidth = 2.6.dp.toPx())
    }
    else -> { // Pinned-Pinned
      drawPinSupport(Offset(colX, topY - 8.dp.toPx()), textCol)
      drawPinSupport(Offset(colX, botY), textCol)
    }
  }

  // Buckled Curve
  val amp = w * 0.10f
  val curvePath = Path()
  val steps = 40
  for (i in 0..steps) {
    val t = i / steps.toFloat() // 0 = top, 1 = bottom
    val xOff = when (mode) {
      1 -> amp * (1f - cos((PI * 0.5 * (1f - t))).toFloat())
      2 -> amp * sin(PI * t * 1.15).toFloat() * t
      3 -> amp * 0.5f * (1f - cos(2.0 * PI * t).toFloat())
      else -> amp * sin(PI * t).toFloat()
    }
    val px = colX + xOff
    val py = topY + len * t
    if (i == 0) curvePath.moveTo(px, py) else curvePath.lineTo(px, py)
  }
  drawPath(curvePath, color = cyan, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round))

  // KL Bracket on the right
  val brX = w * 0.68f
  val (klTop, klBot, klLabel) = when (mode) {
    1 -> Triple(topY, botY, "KL = 2.0 L")
    2 -> Triple(topY, topY + len * 0.70f, "KL = 0.7 L")
    3 -> Triple(topY + len * 0.25f, topY + len * 0.75f, "KL = 0.5 L")
    else -> Triple(topY, botY, "KL = 1.0 L")
  }
  drawLine(emerald, Offset(brX, klTop), Offset(brX, klBot), strokeWidth = 2.dp.toPx())
  drawLine(emerald, Offset(brX - 6.dp.toPx(), klTop), Offset(brX + 6.dp.toPx(), klTop), strokeWidth = 2.dp.toPx())
  drawLine(emerald, Offset(brX - 6.dp.toPx(), klBot), Offset(brX + 6.dp.toPx(), klBot), strokeWidth = 2.dp.toPx())
  cadLabel(klLabel, brX + 12.dp.toPx(), (klTop + klBot) * 0.5f + 4.dp.toPx(), emerald, sizeSp = 11f, align = Paint.Align.LEFT)
}

// ============================================================================
// STRUCTURAL ANALYSIS — BEAM DEFLECTION, FEM & INFLUENCE LINE CAD CANVASES
// ============================================================================

private fun DrawScope.drawSaBeamDeflection(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val x0 = w * 0.14f
  val x1 = w * 0.86f
  val span = x1 - x0
  val beamY = h * 0.44f

  // Undeformed beam
  drawLine(textCol, Offset(x0, beamY), Offset(x1, beamY), strokeWidth = 2.8.dp.toPx(), cap = StrokeCap.Round)

  // Supports
  if (mode == 3 || mode == 4) {
    drawFixedWall(x0, beamY, 20.dp.toPx(), isLeftWall = true, color = textCol)
  } else {
    drawPinSupport(Offset(x0, beamY), textCol)
    drawRollerSupport(Offset(x1, beamY), textCol)
  }

  // Loads
  when (mode) {
    0 -> {
      val xm = (x0 + x1) * 0.5f
      drawArrow(Offset(xm, h * 0.17f), Offset(xm, beamY - 3.dp.toPx()), amber)
      cadLabel("P", xm + 12.dp.toPx(), h * 0.27f, amber, sizeSp = 11.5f)
    }
    2 -> {
      val xp = x0 + span * 0.35f
      drawArrow(Offset(xp, h * 0.17f), Offset(xp, beamY - 3.dp.toPx()), amber)
      cadLabel("P", xp + 12.dp.toPx(), h * 0.27f, amber, sizeSp = 11.5f)
      cadLabel("a", (x0 + xp) * 0.5f, beamY - 6.dp.toPx(), textCol, sizeSp = 10f)
      cadLabel("b", (xp + x1) * 0.5f, beamY - 6.dp.toPx(), textCol, sizeSp = 10f)
    }
    3 -> {
      drawArrow(Offset(x1, h * 0.17f), Offset(x1, beamY - 3.dp.toPx()), amber)
      cadLabel("P", x1 - 14.dp.toPx(), h * 0.27f, amber, sizeSp = 11.5f)
    }
    else -> {
      drawLine(amber, Offset(x0, h * 0.24f), Offset(x1, h * 0.24f), strokeWidth = 1.8.dp.toPx())
      for (i in 0..7) {
        val ax = x0 + span * (i / 7f)
        drawArrow(Offset(ax, h * 0.24f), Offset(ax, beamY - 3.dp.toPx()), amber, strokePx = 1.5.dp.toPx(), headLenPx = 4.5.dp.toPx())
      }
      cadLabel("w", (x0 + x1) * 0.5f, h * 0.18f, amber, sizeSp = 11f)
    }
  }

  // Dashed Elastic Deflection Curve
  val deflPath = Path()
  val sag = h * 0.25f
  val steps = 36
  for (i in 0..steps) {
    val t = i / steps.toFloat()
    val px = x0 + span * t
    val dy = when (mode) {
      3, 4 -> sag * (t * t)
      2 -> sag * sin(PI * t).toFloat() * (1.15f - 0.3f * t)
      else -> sag * sin(PI * t).toFloat()
    }
    val py = beamY + dy
    if (i == 0) deflPath.moveTo(px, py) else deflPath.lineTo(px, py)
  }
  drawPath(
    deflPath,
    color = cyan,
    style = Stroke(width = 2.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
  )

  val maxX = when (mode) {
    3, 4 -> x1
    2 -> x0 + span * 0.44f
    else -> (x0 + x1) * 0.5f
  }
  val maxY = beamY + sag
  drawArrow(Offset(maxX, beamY + 2.dp.toPx()), Offset(maxX, maxY), emerald, strokePx = 1.8.dp.toPx(), headLenPx = 5.dp.toPx())
  cadLabel("Δ_max", maxX + 10.dp.toPx(), maxY + 13.dp.toPx(), emerald, sizeSp = 10.5f)
}

private fun DrawScope.drawSaFemDiagram(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val x0 = w * 0.15f
  val x1 = w * 0.85f
  val span = x1 - x0
  val beamY = h * 0.52f

  drawFixedWall(x0, beamY, 22.dp.toPx(), isLeftWall = true, color = textCol)
  val rightY = if (mode == 4) beamY + h * 0.14f else beamY
  drawFixedWall(x1, rightY, 22.dp.toPx(), isLeftWall = false, color = textCol)

  if (mode == 4) {
    val settlePath = Path().apply {
      moveTo(x0, beamY)
      cubicTo(x0 + span * 0.45f, beamY, x0 + span * 0.55f, rightY, x1, rightY)
    }
    drawPath(settlePath, cyan, style = Stroke(2.8.dp.toPx()))
    drawArrow(Offset(x1 + 14.dp.toPx(), beamY), Offset(x1 + 14.dp.toPx(), rightY), amber, strokePx = 1.8.dp.toPx(), headLenPx = 5.dp.toPx())
    cadLabel("Δ", x1 + 26.dp.toPx(), (beamY + rightY) * 0.5f + 4.dp.toPx(), amber, sizeSp = 11f)
  } else {
    drawLine(cyan, Offset(x0, beamY), Offset(x1, beamY), strokeWidth = 2.8.dp.toPx())
    when (mode) {
      0 -> {
        drawLine(amber, Offset(x0, h * 0.28f), Offset(x1, h * 0.28f), strokeWidth = 1.8.dp.toPx())
        for (i in 0..7) {
          val ax = x0 + span * (i / 7f)
          drawArrow(Offset(ax, h * 0.28f), Offset(ax, beamY - 3.dp.toPx()), amber, strokePx = 1.5.dp.toPx(), headLenPx = 4.5.dp.toPx())
        }
        cadLabel("w", (x0 + x1) * 0.5f, h * 0.21f, amber, sizeSp = 11f)
      }
      1 -> {
        val xm = (x0 + x1) * 0.5f
        drawArrow(Offset(xm, h * 0.22f), Offset(xm, beamY - 3.dp.toPx()), amber)
        cadLabel("P", xm + 12.dp.toPx(), h * 0.32f, amber, sizeSp = 11.5f)
      }
      2 -> {
        val xp = x0 + span * 0.34f
        drawArrow(Offset(xp, h * 0.22f), Offset(xp, beamY - 3.dp.toPx()), amber)
        cadLabel("P", xp + 12.dp.toPx(), h * 0.32f, amber, sizeSp = 11.5f)
        cadLabel("a", (x0 + xp) * 0.5f, beamY + 15.dp.toPx(), textCol, sizeSp = 10f)
        cadLabel("b", (xp + x1) * 0.5f, beamY + 15.dp.toPx(), textCol, sizeSp = 10f)
      }
      3 -> {
        drawLine(amber, Offset(x0, beamY - 2.dp.toPx()), Offset(x1, h * 0.26f), strokeWidth = 1.8.dp.toPx())
        for (i in 1..6) {
          val frac = i / 6f
          val ax = x0 + span * frac
          val topArrowY = (beamY - 2.dp.toPx()) - (beamY - 2.dp.toPx() - h * 0.26f) * frac
          drawArrow(Offset(ax, topArrowY), Offset(ax, beamY - 3.dp.toPx()), amber, strokePx = 1.4.dp.toPx(), headLenPx = 4.dp.toPx())
        }
        cadLabel("w", x1 - 14.dp.toPx(), h * 0.20f, amber, sizeSp = 11f)
      }
    }
  }

  // End Moment Arcs (FEM_AB, FEM_BA)
  val arcR = 14.dp.toPx()
  drawArc(
    color = emerald,
    startAngle = 120f,
    sweepAngle = 210f,
    useCenter = false,
    topLeft = Offset(x0 + 6.dp.toPx(), beamY - arcR),
    size = Size(arcR * 2, arcR * 2),
    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
  )
  drawArc(
    color = emerald,
    startAngle = -150f,
    sweepAngle = 210f,
    useCenter = false,
    topLeft = Offset(x1 - 6.dp.toPx() - arcR * 2, rightY - arcR),
    size = Size(arcR * 2, arcR * 2),
    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
  )
  cadLabel("FEM_AB", x0 + 36.dp.toPx(), beamY + 26.dp.toPx(), emerald, sizeSp = 9.5f)
  cadLabel("FEM_BA", x1 - 36.dp.toPx(), rightY + 26.dp.toPx(), emerald, sizeSp = 9.5f)
}

private fun DrawScope.drawSaInfluenceLine(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val xA = w * 0.14f
  val xB = w * 0.86f
  val xC = xA + (xB - xA) * 0.38f
  val beamY = h * 0.30f

  // Girder A—B with section C
  drawLine(textCol, Offset(xA, beamY), Offset(xB, beamY), strokeWidth = 2.6.dp.toPx())
  drawPinSupport(Offset(xA, beamY), textCol)
  drawRollerSupport(Offset(xB, beamY), textCol)
  drawLine(amber, Offset(xC, beamY - 10.dp.toPx()), Offset(xC, h * 0.88f), strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
  cadLabel("A", xA, beamY - 10.dp.toPx(), textCol, sizeSp = 10.5f)
  cadLabel("C", xC, beamY - 10.dp.toPx(), amber, sizeSp = 10.5f)
  cadLabel("B", xB, beamY - 10.dp.toPx(), textCol, sizeSp = 10.5f)

  // Influence Line Base Axis
  val baseY = h * 0.68f
  drawLine(textCol.copy(alpha = 0.55f), Offset(xA, baseY), Offset(xB, baseY), strokeWidth = 1.4.dp.toPx())

  when (mode) {
    1 -> { // Shear at C
      val yNeg = baseY + h * 0.14f
      val yPos = baseY - h * 0.22f
      val negPoly = Path().apply {
        moveTo(xA, baseY); lineTo(xC, yNeg); lineTo(xC, baseY); close()
      }
      val posPoly = Path().apply {
        moveTo(xC, baseY); lineTo(xC, yPos); lineTo(xB, baseY); close()
      }
      drawPath(negPoly, Color(0xFFEF4444).copy(alpha = 0.22f))
      drawPath(posPoly, emerald.copy(alpha = 0.22f))
      drawLine(Color(0xFFEF4444), Offset(xA, baseY), Offset(xC, yNeg), strokeWidth = 2.4.dp.toPx())
      drawLine(amber, Offset(xC, yNeg), Offset(xC, yPos), strokeWidth = 2.dp.toPx())
      drawLine(emerald, Offset(xC, yPos), Offset(xB, baseY), strokeWidth = 2.4.dp.toPx())
      cadLabel("-a/L", xC - 16.dp.toPx(), yNeg + 12.dp.toPx(), Color(0xFFEF4444), sizeSp = 10f)
      cadLabel("+b/L", xC + 18.dp.toPx(), yPos - 6.dp.toPx(), emerald, sizeSp = 10f)
    }
    2 -> { // Moment at C
      val yPeak = baseY - h * 0.25f
      val mPoly = Path().apply {
        moveTo(xA, baseY); lineTo(xC, yPeak); lineTo(xB, baseY); close()
      }
      drawPath(mPoly, cyan.copy(alpha = 0.22f))
      drawLine(cyan, Offset(xA, baseY), Offset(xC, yPeak), strokeWidth = 2.4.dp.toPx())
      drawLine(cyan, Offset(xC, yPeak), Offset(xB, baseY), strokeWidth = 2.4.dp.toPx())
      drawCircle(amber, radius = 4.dp.toPx(), center = Offset(xC, yPeak))
      cadLabel("ab / L", xC + 22.dp.toPx(), yPeak - 4.dp.toPx(), amber, sizeSp = 10.5f)
    }
    else -> { // Reaction R_A
      val yOne = baseY - h * 0.24f
      val rPoly = Path().apply {
        moveTo(xA, baseY); lineTo(xA, yOne); lineTo(xB, baseY); close()
      }
      drawPath(rPoly, emerald.copy(alpha = 0.22f))
      drawLine(emerald, Offset(xA, yOne), Offset(xB, baseY), strokeWidth = 2.4.dp.toPx())
      drawCircle(amber, radius = 4.dp.toPx(), center = Offset(xA, yOne))
      cadLabel("1.0", xA + 16.dp.toPx(), yOne - 4.dp.toPx(), amber, sizeSp = 10.5f)
    }
  }
}

// ============================================================================
// RCD & PCD (NSCP 2015) — INTERACTIVE CAD BLUEPRINTS
// ============================================================================

@Composable
private fun RcdWhitneyStressBlockCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "Tension-Controlled εt ≥ 0.005",
    "Transition Zone",
    "Balanced εt = fy/Es"
  )
  val bottomInfo = when (selectedMode) {
    1 -> "Transition (0.002 < ε_t < 0.005): c/d = 0.46  •  φ = 0.65 + 0.25(ε_t - 0.002)/0.003 ≈ 0.78"
    2 -> "Balanced Failure: c_b/d = 600/(600 + f_y) ≈ 0.59  •  Compression-Controlled φ = 0.65"
    else -> "Tension-Controlled (ε_t ≥ 0.005): c/d ≤ 0.375  •  φ = 0.90  •  a = β₁·c"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "rcd_whitney_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 204,
      accentColor = accentColor,
      topBadge = "NSCP 422.2.2 • WHITNEY STRESS & STRAIN PROFILE",
      bottomFormula = bottomInfo,
      testTag = "rcd_whitney_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawRcdWhitneyStressBlock(selectedMode, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun RcdTBeamEffectiveWidthCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "Interior T-Beam",
    "Spandrel L-Beam"
  )
  val bottomInfo = when (selectedMode) {
    1 -> "Edge L-Beam (Sec. 406.3.2.1): b_f = b_w + min(6 h_f , S_w / 2 , L / 12)"
    else -> "Interior T-Beam (Sec. 406.3.2.1): b_f = min(b_w + 16 h_f , b_w + S_w , L / 4)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "rcd_tbeam_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 188,
      accentColor = accentColor,
      topBadge = "NSCP 406.3.2.1 • EFFECTIVE FLANGE WIDTH b_f",
      bottomFormula = bottomInfo,
      testTag = "rcd_tbeam_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawRcdTBeamFlange(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun RcdColumnPmInteractionCard(accentColor: Color) {
  val context = LocalContext.current
  val localView = LocalView.current
  val palette = currentGlassPalette()
  var selectedNode by remember { mutableStateOf("balanced") }

  val hotspots = remember {
    listOf(
      "pure_axial" to { w: Float, h: Float -> Offset(w * 0.22f, h * 0.16f) },
      "max_cap" to { w: Float, h: Float -> Offset(w * 0.36f, h * 0.28f) },
      "balanced" to { w: Float, h: Float -> Offset(w * 0.72f, h * 0.56f) },
      "pure_bending" to { w: Float, h: Float -> Offset(w * 0.48f, h * 0.80f) }
    )
  }

  val pillSpecs = listOf(
    "pure_axial" to "Pure Axial P₀",
    "max_cap" to "Max Cap 0.80 P₀",
    "balanced" to "Balanced (Pb, Mb)",
    "pure_bending" to "Pure Bending Mn"
  )

  val detailText = when (selectedNode) {
    "pure_axial" -> "Pure Axial P₀ = 0.85 f'_c (A_g - A_st) + f_y A_st   (ε = 0.002)"
    "max_cap" -> "NSCP 422.4.2.2 Cap: φP_n,max = 0.80 φP₀ (Tied, φ=0.65)  |  0.85 φP₀ (Spiral, φ=0.75)"
    "pure_bending" -> "Pure Flexure (P_n = 0): Tension-controlled limit φ = 0.90 (ε_t ≥ 0.005)"
    else -> "Balanced Point (P_b, M_b): Concrete ε_c = 0.003 while steel yields ε_s = f_y / E_s"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      pillSpecs.forEach { (id, label) ->
        val isSelected = selectedNode == id
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = if (isSelected) accentColor.copy(alpha = if (palette.isDark) 0.28f else 0.16f) else null,
          borderColor = if (isSelected) accentColor.copy(alpha = 0.72f) else null,
          onClick = {
            playCardHapticSound(context, localView)
            selectedNode = id
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("rcd_pm_pill_$id")
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = AppleSansFamily,
              fontSize = 10.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) (if (palette.isDark) Color.White else accentColor) else palette.textSecondary
          )
        }
      }
    }

    PsadCadCanvasShell(
      heightDp = 212,
      accentColor = accentColor,
      topBadge = "INTERACTIVE COLUMN P-M INTERACTION • TAP NODES",
      bottomFormula = detailText,
      testTag = "rcd_pm_interaction_canvas",
      tapHotspots = hotspots,
      onTapHotspot = { id ->
        playCardHapticSound(context, localView)
        selectedNode = id
      }
    ) { cyan, amber, emerald, textCol ->
      drawRcdColumnPmDiagram(selectedNode, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun RcdFootingShearPerimeterCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "One-Way Shear (d)",
    "Two-Way Punching (d/2)"
  )
  val bottomInfo = when (selectedMode) {
    1 -> "Two-Way Punching @ d/2 (Sec. 422.6.5.2): b₀ = 2(c₁ + d) + 2(c₂ + d)  •  V_u = q_u(B·L - A_punch)"
    else -> "One-Way Beam Shear @ d from Column Face (Sec. 413.3): V_u = q_u · B · ( (L - c)/2 - d ) ≤ φ 0.17√f'_c B d"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "rcd_footing_shear_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 196,
      accentColor = accentColor,
      topBadge = "NSCP 413.3 • FOOTING CRITICAL SHEAR PLAN",
      bottomFormula = bottomInfo,
      testTag = "rcd_footing_shear_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawRcdFootingShearPlan(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

private fun DrawScope.drawRcdWhitneyStressBlock(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val topY = h * 0.22f
  val botY = h * 0.84f
  val beamH = botY - topY
  val dY = topY + beamH * 0.84f

  // 1. Concrete Cross-Section (Left)
  val secX = w * 0.08f
  val secW = w * 0.20f
  drawRect(cyan.copy(alpha = 0.14f), Offset(secX, topY), Size(secW, beamH))
  drawRect(textCol, Offset(secX, topY), Size(secW, beamH), style = Stroke(2.dp.toPx()))
  // Rebar circles at depth d
  for (i in 0..2) {
    val bx = secX + secW * (0.25f + i * 0.25f)
    drawCircle(emerald, radius = 4.2.dp.toPx(), center = Offset(bx, dY))
  }
  cadLabel("b", secX + secW * 0.5f, topY - 7.dp.toPx(), textCol, sizeSp = 10f)
  cadLabel("d", secX - 10.dp.toPx(), (topY + dY) * 0.5f, cyan, sizeSp = 10f)

  // Neutral Axis position by mode
  val cRatio = when (mode) {
    1 -> 0.46f
    2 -> 0.59f
    else -> 0.34f
  }
  val naY = topY + (dY - topY) * cRatio
  val aY = topY + (naY - topY) * 0.85f

  // Dashed N.A. across all 3 diagrams
  drawLine(
    amber,
    Offset(secX - 6.dp.toPx(), naY),
    Offset(w * 0.93f, naY),
    strokeWidth = 1.4.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f))
  )
  cadLabel("N.A.", w * 0.34f, naY - 5.dp.toPx(), amber, sizeSp = 9.5f)

  // 2. Linear Strain Diagram (Center)
  val strainX = w * 0.50f
  val topStrainW = w * 0.11f
  val botStrainW = topStrainW * ((dY - naY) / (naY - topY))
  drawLine(textCol.copy(alpha = 0.65f), Offset(strainX, topY), Offset(strainX, dY), strokeWidth = 1.6.dp.toPx())
  drawLine(cyan, Offset(strainX, topY), Offset(strainX + topStrainW, topY), strokeWidth = 2.dp.toPx())
  drawLine(cyan, Offset(strainX + topStrainW, topY), Offset(strainX - botStrainW, dY), strokeWidth = 2.4.dp.toPx())
  drawLine(emerald, Offset(strainX, dY), Offset(strainX - botStrainW, dY), strokeWidth = 2.2.dp.toPx())
  cadLabel("0.003", strainX + topStrainW, topY - 6.dp.toPx(), cyan, sizeSp = 9.5f)
  val epsLabel = when (mode) {
    1 -> "εt≈0.0035"
    2 -> "εy=0.0021"
    else -> "εt≥0.005"
  }
  cadLabel(epsLabel, strainX - botStrainW * 0.6f, dY + 13.dp.toPx(), emerald, sizeSp = 9.5f)
  cadLabel("c", strainX - 10.dp.toPx(), (topY + naY) * 0.5f, amber, sizeSp = 9.5f)

  // 3. Whitney Stress Block (Right)
  val strX = w * 0.74f
  val blockW = w * 0.14f
  drawLine(textCol.copy(alpha = 0.65f), Offset(strX, topY), Offset(strX, dY), strokeWidth = 1.6.dp.toPx())
  drawRect(accent.copy(alpha = 0.28f), Offset(strX, topY), Size(blockW, aY - topY))
  drawRect(accent, Offset(strX, topY), Size(blockW, aY - topY), style = Stroke(2.dp.toPx()))
  drawArrow(Offset(strX + blockW + 18.dp.toPx(), (topY + aY) * 0.5f), Offset(strX + blockW + 2.dp.toPx(), (topY + aY) * 0.5f), accent, headLenPx = 5.5.dp.toPx())
  cadLabel("0.85f'c", strX + blockW * 0.5f, topY - 6.dp.toPx(), accent, sizeSp = 9.5f)
  cadLabel("C", strX + blockW + 24.dp.toPx(), (topY + aY) * 0.5f + 4.dp.toPx(), accent, sizeSp = 10f)
  cadLabel("a", strX - 9.dp.toPx(), (topY + aY) * 0.5f + 3.dp.toPx(), accent, sizeSp = 9.5f)
  drawArrow(Offset(strX, dY), Offset(strX + blockW, dY), emerald, headLenPx = 5.5.dp.toPx())
  cadLabel("T = As·fy", strX + blockW * 0.5f, dY + 13.dp.toPx(), emerald, sizeSp = 9.5f)
}

private fun DrawScope.drawRcdTBeamFlange(
  isLBeam: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val slabTop = h * 0.30f
  val slabH = h * 0.16f
  val webBot = h * 0.80f
  val webW = w * 0.14f

  if (!isLBeam) {
    val flangeLeft = w * 0.20f
    val flangeW = w * 0.60f
    val webLeft = w * 0.50f - webW * 0.5f
    val path = Path().apply {
      moveTo(flangeLeft, slabTop)
      lineTo(flangeLeft + flangeW, slabTop)
      lineTo(flangeLeft + flangeW, slabTop + slabH)
      lineTo(webLeft + webW, slabTop + slabH)
      lineTo(webLeft + webW, webBot)
      lineTo(webLeft, webBot)
      lineTo(webLeft, slabTop + slabH)
      lineTo(flangeLeft, slabTop + slabH)
      close()
    }
    drawPath(path, cyan.copy(alpha = 0.20f))
    drawPath(path, textCol, style = Stroke(2.2.dp.toPx(), join = StrokeJoin.Round))
    drawArrow(Offset(flangeLeft, slabTop - 10.dp.toPx()), Offset(flangeLeft + flangeW, slabTop - 10.dp.toPx()), accent, strokePx = 1.6.dp.toPx(), headLenPx = 5.dp.toPx())
    drawArrow(Offset(flangeLeft + flangeW, slabTop - 10.dp.toPx()), Offset(flangeLeft, slabTop - 10.dp.toPx()), accent, strokePx = 1.6.dp.toPx(), headLenPx = 5.dp.toPx())
    cadLabel("b_f (Effective Width)", w * 0.50f, slabTop - 18.dp.toPx(), accent, sizeSp = 10.5f)
    cadLabel("h_f", flangeLeft - 14.dp.toPx(), slabTop + slabH * 0.65f, amber, sizeSp = 10f)
    cadLabel("b_w", w * 0.50f, webBot + 14.dp.toPx(), emerald, sizeSp = 10f)
  } else {
    val webLeft = w * 0.30f
    val flangeRight = w * 0.74f
    val path = Path().apply {
      moveTo(webLeft, slabTop)
      lineTo(flangeRight, slabTop)
      lineTo(flangeRight, slabTop + slabH)
      lineTo(webLeft + webW, slabTop + slabH)
      lineTo(webLeft + webW, webBot)
      lineTo(webLeft, webBot)
      close()
    }
    drawPath(path, cyan.copy(alpha = 0.20f))
    drawPath(path, textCol, style = Stroke(2.2.dp.toPx(), join = StrokeJoin.Round))
    drawArrow(Offset(webLeft, slabTop - 10.dp.toPx()), Offset(flangeRight, slabTop - 10.dp.toPx()), accent, strokePx = 1.6.dp.toPx(), headLenPx = 5.dp.toPx())
    drawArrow(Offset(flangeRight, slabTop - 10.dp.toPx()), Offset(webLeft, slabTop - 10.dp.toPx()), accent, strokePx = 1.6.dp.toPx(), headLenPx = 5.dp.toPx())
    cadLabel("b_f (Spandrel L-Beam)", (webLeft + flangeRight) * 0.5f, slabTop - 18.dp.toPx(), accent, sizeSp = 10.5f)
    cadLabel("h_f", flangeRight + 14.dp.toPx(), slabTop + slabH * 0.65f, amber, sizeSp = 10f)
    cadLabel("b_w", webLeft + webW * 0.5f, webBot + 14.dp.toPx(), emerald, sizeSp = 10f)
  }
}

private fun DrawScope.drawRcdShearStirrupDiagram(
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val xSup = w * 0.14f
  val beamTop = h * 0.26f
  val beamBot = h * 0.76f
  val beamRight = w * 0.90f

  // Support column & RC beam
  drawRect(textCol.copy(alpha = 0.20f), Offset(w * 0.06f, beamTop), Size(xSup - w * 0.06f, h * 0.62f))
  drawRect(textCol, Offset(w * 0.06f, beamTop), Size(xSup - w * 0.06f, h * 0.62f), style = Stroke(1.8.dp.toPx()))
  drawRect(cyan.copy(alpha = 0.14f), Offset(xSup, beamTop), Size(beamRight - xSup, beamBot - beamTop))
  drawLine(textCol, Offset(xSup, beamTop), Offset(beamRight, beamTop), strokeWidth = 2.dp.toPx())
  drawLine(textCol, Offset(xSup, beamBot), Offset(beamRight, beamBot), strokeWidth = 2.dp.toPx())

  // Longitudinal tension steel
  val rebarY = beamBot - 10.dp.toPx()
  drawLine(emerald, Offset(xSup, rebarY), Offset(beamRight - 4.dp.toPx(), rebarY), strokeWidth = 2.6.dp.toPx())

  // Vertical stirrups spaced at s
  val sPx = (beamRight - xSup) * 0.11f
  var sx = xSup + sPx * 0.8f
  while (sx < beamRight - 10.dp.toPx()) {
    drawLine(cyan, Offset(sx, beamTop + 6.dp.toPx()), Offset(sx, rebarY), strokeWidth = 1.8.dp.toPx())
    sx += sPx
  }
  cadLabel("s", xSup + sPx * 2.3f, beamTop - 6.dp.toPx(), cyan, sizeSp = 10.5f)

  // 45° Diagonal Shear Crack + Critical Section at d
  val critX = xSup + (beamBot - beamTop) * 0.90f
  drawLine(Color(0xFFEF4444), Offset(xSup + 4.dp.toPx(), rebarY), Offset(critX + 18.dp.toPx(), beamTop + 6.dp.toPx()), strokeWidth = 2.6.dp.toPx())
  drawLine(
    amber,
    Offset(critX, beamTop - 10.dp.toPx()),
    Offset(critX, beamBot + 14.dp.toPx()),
    strokeWidth = 1.6.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f))
  )
  cadLabel("Vu @ d", critX, beamBot + 24.dp.toPx(), amber, sizeSp = 10f)
  cadLabel("45° Crack", critX + 36.dp.toPx(), beamTop + 18.dp.toPx(), Color(0xFFEF4444), sizeSp = 9.5f)
}

private fun DrawScope.drawRcdColumnPmDiagram(
  selectedNode: String,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val originX = w * 0.22f
  val originY = h * 0.80f
  val topY = h * 0.16f
  val rightX = w * 0.78f

  // Axes (Mn horizontal, Pn vertical)
  drawArrow(Offset(originX, originY + 8.dp.toPx()), Offset(originX, topY - 12.dp.toPx()), textCol.copy(alpha = 0.75f), strokePx = 1.8.dp.toPx(), headLenPx = 6.dp.toPx())
  drawArrow(Offset(originX - 8.dp.toPx(), originY), Offset(rightX + 12.dp.toPx(), originY), textCol.copy(alpha = 0.75f), strokePx = 1.8.dp.toPx(), headLenPx = 6.dp.toPx())
  cadLabel("P_n", originX - 18.dp.toPx(), topY, textCol, sizeSp = 10.5f)
  cadLabel("M_n", rightX + 12.dp.toPx(), originY + 14.dp.toPx(), textCol, sizeSp = 10.5f)

  val p0Pt = Offset(originX, topY)
  val capPt = Offset(w * 0.36f, h * 0.28f)
  val balPt = Offset(w * 0.72f, h * 0.56f)
  val bendPt = Offset(w * 0.48f, originY)

  // Interaction curve
  val curvePath = Path().apply {
    moveTo(p0Pt.x, p0Pt.y)
    quadraticTo(w * 0.58f, h * 0.26f, balPt.x, balPt.y)
    quadraticTo(w * 0.64f, h * 0.72f, bendPt.x, bendPt.y)
  }
  drawPath(curvePath, cyan, style = Stroke(2.6.dp.toPx(), cap = StrokeCap.Round))

  // Horizontal 0.80 P0 cutoff line
  drawLine(
    amber,
    Offset(originX, capPt.y),
    Offset(w * 0.50f, capPt.y),
    strokeWidth = 1.6.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
  )
  // Balanced ray from origin
  drawLine(
    emerald.copy(alpha = 0.60f),
    Offset(originX, originY),
    balPt,
    strokeWidth = 1.3.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
  )

  val nodes = listOf(
    Triple("pure_axial", p0Pt, "P₀"),
    Triple("max_cap", capPt, "0.80P₀"),
    Triple("balanced", balPt, "(Mb,Pb)"),
    Triple("pure_bending", bendPt, "Mn")
  )
  nodes.forEach { (id, pt, lbl) ->
    val active = selectedNode == id
    val col = if (active) amber else accent
    if (active) {
      drawCircle(amber.copy(alpha = 0.26f), radius = 12.dp.toPx(), center = pt)
    }
    drawCircle(col, radius = if (active) 5.5.dp.toPx() else 4.dp.toPx(), center = pt)
    cadLabel(lbl, pt.x + 22.dp.toPx(), pt.y - 6.dp.toPx(), col, sizeSp = 9.5f)
  }
}

private fun DrawScope.drawRcdFootingShearPlan(
  isTwoWay: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val fSize = h * 0.66f
  val fLeft = w * 0.50f - fSize * 0.5f
  val fTop = h * 0.18f

  // Footing Plan B x L
  drawRect(cyan.copy(alpha = 0.12f), Offset(fLeft, fTop), Size(fSize, fSize))
  drawRect(textCol, Offset(fLeft, fTop), Size(fSize, fSize), style = Stroke(2.dp.toPx()))
  cadLabel("B", w * 0.50f, fTop + fSize + 14.dp.toPx(), textCol, sizeSp = 10f)

  // Center Column
  val colS = fSize * 0.24f
  val colLeft = w * 0.50f - colS * 0.5f
  val colTop = fTop + fSize * 0.5f - colS * 0.5f
  drawRect(textCol.copy(alpha = 0.35f), Offset(colLeft, colTop), Size(colS, colS))
  drawRect(textCol, Offset(colLeft, colTop), Size(colS, colS), style = Stroke(1.8.dp.toPx()))

  if (!isTwoWay) {
    val dPx = fSize * 0.16f
    val critX = colLeft + colS + dPx
    drawRect(amber.copy(alpha = 0.24f), Offset(critX, fTop), Size(fLeft + fSize - critX, fSize))
    drawLine(
      amber,
      Offset(critX, fTop - 6.dp.toPx()),
      Offset(critX, fTop + fSize + 6.dp.toPx()),
      strokeWidth = 2.2.dp.toPx(),
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f))
    )
    cadLabel("d", (colLeft + colS + critX) * 0.5f, fTop + fSize * 0.5f, amber, sizeSp = 10.5f)
    cadLabel("1-Way Critical @ d", critX + 34.dp.toPx(), fTop + 14.dp.toPx(), amber, sizeSp = 9.5f)
  } else {
    val halfD = fSize * 0.11f
    val pLeft = colLeft - halfD
    val pTop = colTop - halfD
    val pSize = colS + halfD * 2f
    drawRect(
      emerald,
      Offset(pLeft, pTop),
      Size(pSize, pSize),
      style = Stroke(width = 2.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f)))
    )
    cadLabel("b₀ @ d/2", w * 0.50f, pTop - 6.dp.toPx(), emerald, sizeSp = 10f)
    cadLabel("c₁+d", pLeft + pSize + 22.dp.toPx(), pTop + pSize * 0.5f, emerald, sizeSp = 9.5f)
  }
}

// ============================================================================
// STEEL DESIGN (NSCP 2015) — INTERACTIVE CAD BLUEPRINTS
// ============================================================================

@Composable
private fun SteelBlockShearStaggerCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "Block Shear (Agv/Anv/Ant)",
    "Staggered Pitch s & Gage g"
  )
  val bottomInfo = when (selectedMode) {
    1 -> "Cochrane Formula (Sec. 502.4.3b): w_net = w_g - Σ d_h + Σ [ s² / (4g) ]  (d_h = d_b + 2 mm)"
    else -> "Block Shear (Sec. 510.4.3): R_n = 0.6 F_u A_nv + U_bs F_u A_nt ≤ 0.6 F_y A_gv + U_bs F_u A_nt"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "steel_tension_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 190,
      accentColor = accentColor,
      topBadge = "NSCP 502 / 510 • TENSION RUPTURE & BLOCK SHEAR",
      bottomFormula = bottomInfo,
      testTag = "steel_tension_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawSteelTensionDiagram(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun SteelColumnBucklingCurveCard(accentColor: Color) {
  val context = LocalContext.current
  val localView = LocalView.current
  val palette = currentGlassPalette()
  var selectedZone by remember { mutableStateOf("transition") }

  val hotspots = remember {
    listOf(
      "inelastic" to { w: Float, h: Float -> Offset(w * 0.34f, h * 0.36f) },
      "transition" to { w: Float, h: Float -> Offset(w * 0.54f, h * 0.54f) },
      "elastic" to { w: Float, h: Float -> Offset(w * 0.76f, h * 0.72f) }
    )
  }

  val pillSpecs = listOf(
    "inelastic" to "Inelastic Zone",
    "transition" to "4.71√(E/Fy)",
    "elastic" to "Elastic Euler Zone"
  )

  val detailText = when (selectedZone) {
    "inelastic" -> "Inelastic (KL/r ≤ 4.71√(E/F_y)): F_cr = [0.658^(F_y / F_e)] · F_y   (Eq. 505.3-2)"
    "elastic" -> "Elastic Buckling (KL/r > 4.71√(E/F_y)): F_cr = 0.877 · F_e = 0.877 π²E / (KL/r)²"
    else -> "Transition Limit KL/r = 4.71√(E/F_y)  (F_e = 0.44 F_y  ⇒  F_cr = 0.39 F_y)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      pillSpecs.forEach { (id, label) ->
        val isSelected = selectedZone == id
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = if (isSelected) accentColor.copy(alpha = if (palette.isDark) 0.28f else 0.16f) else null,
          borderColor = if (isSelected) accentColor.copy(alpha = 0.72f) else null,
          onClick = {
            playCardHapticSound(context, localView)
            selectedZone = id
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("steel_col_pill_$id")
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = AppleSansFamily,
              fontSize = 10.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) (if (palette.isDark) Color.White else accentColor) else palette.textSecondary
          )
        }
      }
    }

    PsadCadCanvasShell(
      heightDp = 200,
      accentColor = accentColor,
      topBadge = "NSCP 505.3 • F_cr vs. KL/r COLUMN BUCKLING CURVE",
      bottomFormula = detailText,
      testTag = "steel_column_buckling_canvas",
      tapHotspots = hotspots,
      onTapHotspot = { id ->
        playCardHapticSound(context, localView)
        selectedZone = id
      }
    ) { cyan, amber, emerald, textCol ->
      drawSteelColumnBucklingCurve(selectedZone, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun SteelBeamLtbCurveCard(accentColor: Color) {
  var selectedZone by remember { mutableIntStateOf(1) }
  val labels = listOf(
    "Zone 1: Lb ≤ Lp",
    "Zone 2: Lp < Lb ≤ Lr",
    "Zone 3: Lb > Lr"
  )
  val bottomInfo = when (selectedZone) {
    0 -> "Zone 1 Plastic Yielding (L_b ≤ L_p): M_n = M_p = F_y · Z_x  •  L_p = 1.76 r_y √(E/F_y)"
    2 -> "Zone 3 Elastic LTB (L_b > L_r): M_n = F_cr · S_x ≤ M_p  (Hyperbolic Euler Torsional Drop)"
    else -> "Zone 2 Inelastic LTB: M_n = C_b [ M_p - (M_p - 0.7 F_y S_x)(L_b - L_p)/(L_r - L_p) ] ≤ M_p"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedZone,
      accentColor = accentColor,
      testTagPrefix = "steel_ltb_zone",
      onSelect = { selectedZone = it }
    )
    PsadCadCanvasShell(
      heightDp = 198,
      accentColor = accentColor,
      topBadge = "NSCP 506.2 • M_n vs. L_b LATERAL-TORSIONAL BUCKLING",
      bottomFormula = bottomInfo,
      testTag = "steel_ltb_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawSteelLtbCurve(selectedZone, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

private fun DrawScope.drawSteelTensionDiagram(
  isStagger: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val pLeft = w * 0.16f
  val pTop = h * 0.24f
  val pW = w * 0.64f
  val pH = h * 0.54f

  drawRect(cyan.copy(alpha = 0.13f), Offset(pLeft, pTop), Size(pW, pH))
  drawRect(textCol, Offset(pLeft, pTop), Size(pW, pH), style = Stroke(2.dp.toPx()))
  drawArrow(Offset(pLeft + pW, pTop + pH * 0.5f), Offset(pLeft + pW + w * 0.12f, pTop + pH * 0.5f), amber)
  cadLabel("P", pLeft + pW + w * 0.10f, pTop + pH * 0.5f - 8.dp.toPx(), amber, sizeSp = 11f)

  if (!isStagger) {
    // 2x2 bolt array on right side of plate with block shear tear-out path
    val bx1 = pLeft + pW * 0.52f
    val bx2 = pLeft + pW * 0.78f
    val by1 = pTop + pH * 0.28f
    val by2 = pTop + pH * 0.72f
    val holes = listOf(Offset(bx1, by1), Offset(bx2, by1), Offset(bx1, by2), Offset(bx2, by2))
    holes.forEach { pt ->
      drawCircle(textCol, radius = 4.5.dp.toPx(), center = pt, style = Stroke(1.8.dp.toPx()))
    }
    // Shear lines (Anv / Agv) + Tension line (Ant)
    drawLine(Color(0xFFEF4444), Offset(pLeft + pW, by1), Offset(bx1, by1), strokeWidth = 2.4.dp.toPx())
    drawLine(Color(0xFFEF4444), Offset(pLeft + pW, by2), Offset(bx1, by2), strokeWidth = 2.4.dp.toPx())
    drawLine(emerald, Offset(bx1, by1), Offset(bx1, by2), strokeWidth = 2.8.dp.toPx())
    cadLabel("Shear A_nv", (bx1 + pLeft + pW) * 0.5f, by1 - 7.dp.toPx(), Color(0xFFEF4444), sizeSp = 9.5f)
    cadLabel("A_nt", bx1 - 18.dp.toPx(), (by1 + by2) * 0.5f + 3.dp.toPx(), emerald, sizeSp = 9.5f)
  } else {
    // Staggered zig-zag bolt holes
    val x1 = pLeft + pW * 0.42f
    val x2 = pLeft + pW * 0.62f
    val y1 = pTop + pH * 0.25f
    val y2 = pTop + pH * 0.50f
    val y3 = pTop + pH * 0.75f
    val pts = listOf(Offset(x1, y1), Offset(x2, y2), Offset(x1, y3))
    pts.forEach { pt ->
      drawCircle(textCol, radius = 4.5.dp.toPx(), center = pt, style = Stroke(1.8.dp.toPx()))
    }
    val zigPath = Path().apply {
      moveTo(x1, pTop)
      lineTo(x1, y1)
      lineTo(x2, y2)
      lineTo(x1, y3)
      lineTo(x1, pTop + pH)
    }
    drawPath(zigPath, amber, style = Stroke(2.4.dp.toPx(), join = StrokeJoin.Round))
    cadLabel("s", (x1 + x2) * 0.5f, pTop + 14.dp.toPx(), cyan, sizeSp = 10f)
    cadLabel("g", x2 + 16.dp.toPx(), (y1 + y2) * 0.5f + 3.dp.toPx(), emerald, sizeSp = 10f)
  }
}

private fun DrawScope.drawSteelColumnBucklingCurve(
  selectedZone: String,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val ox = w * 0.16f
  val oy = h * 0.82f
  val topY = h * 0.18f
  val rightX = w * 0.88f

  drawArrow(Offset(ox, oy + 6.dp.toPx()), Offset(ox, topY - 10.dp.toPx()), textCol.copy(alpha = 0.75f), strokePx = 1.8.dp.toPx(), headLenPx = 5.5.dp.toPx())
  drawArrow(Offset(ox - 6.dp.toPx(), oy), Offset(rightX + 8.dp.toPx(), oy), textCol.copy(alpha = 0.75f), strokePx = 1.8.dp.toPx(), headLenPx = 5.5.dp.toPx())
  cadLabel("F_cr", ox - 18.dp.toPx(), topY + 6.dp.toPx(), textCol, sizeSp = 10f)
  cadLabel("KL/r", rightX, oy + 14.dp.toPx(), textCol, sizeSp = 10f)

  val transX = ox + (rightX - ox) * 0.52f
  val transY = oy - (oy - topY) * 0.39f

  // Inelastic Segment
  val inelPath = Path()
  for (i in 0..20) {
    val t = i / 20f
    val px = ox + (transX - ox) * t
    val norm = 0.658.pow((t * t) * 2.27).toFloat()
    val py = oy - (oy - topY) * norm
    if (i == 0) inelPath.moveTo(px, py) else inelPath.lineTo(px, py)
  }
  drawPath(inelPath, if (selectedZone == "inelastic") amber else cyan, style = Stroke(if (selectedZone == "inelastic") 3.2.dp.toPx() else 2.2.dp.toPx()))

  // Elastic Segment
  val elPath = Path()
  for (i in 0..20) {
    val t = i / 20f
    val ratio = 1f + t * 0.95f
    val px = transX + (rightX - transX) * t
    val norm = 0.39f / (ratio * ratio)
    val py = oy - (oy - topY) * norm
    if (i == 0) elPath.moveTo(px, py) else elPath.lineTo(px, py)
  }
  drawPath(elPath, if (selectedZone == "elastic") amber else emerald, style = Stroke(if (selectedZone == "elastic") 3.2.dp.toPx() else 2.2.dp.toPx()))

  // Transition vertical dashed guide
  drawLine(
    amber,
    Offset(transX, topY),
    Offset(transX, oy),
    strokeWidth = 1.4.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
  )
  drawCircle(if (selectedZone == "transition") amber else accent, radius = 5.dp.toPx(), center = Offset(transX, transY))
  cadLabel("4.71√(E/Fy)", transX, oy + 14.dp.toPx(), amber, sizeSp = 9.5f)
}

private fun DrawScope.drawSteelLtbCurve(
  selectedZone: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val ox = w * 0.16f
  val oy = h * 0.82f
  val topY = h * 0.22f
  val rightX = w * 0.88f

  drawArrow(Offset(ox, oy + 6.dp.toPx()), Offset(ox, topY - 10.dp.toPx()), textCol.copy(alpha = 0.75f), strokePx = 1.8.dp.toPx(), headLenPx = 5.5.dp.toPx())
  drawArrow(Offset(ox - 6.dp.toPx(), oy), Offset(rightX + 8.dp.toPx(), oy), textCol.copy(alpha = 0.75f), strokePx = 1.8.dp.toPx(), headLenPx = 5.5.dp.toPx())
  cadLabel("M_n", ox - 18.dp.toPx(), topY + 6.dp.toPx(), textCol, sizeSp = 10f)
  cadLabel("L_b", rightX, oy + 14.dp.toPx(), textCol, sizeSp = 10f)

  val lpX = ox + (rightX - ox) * 0.26f
  val lrX = ox + (rightX - ox) * 0.62f
  val mrY = oy - (oy - topY) * 0.62f

  // Zone 1: Plastic plateau
  drawLine(if (selectedZone == 0) amber else cyan, Offset(ox, topY), Offset(lpX, topY), strokeWidth = if (selectedZone == 0) 3.4.dp.toPx() else 2.4.dp.toPx())
  // Zone 2: Linear inelastic LTB
  drawLine(if (selectedZone == 1) amber else accent, Offset(lpX, topY), Offset(lrX, mrY), strokeWidth = if (selectedZone == 1) 3.4.dp.toPx() else 2.4.dp.toPx())
  // Zone 3: Elastic LTB curve
  val z3Path = Path()
  for (i in 0..20) {
    val t = i / 20f
    val px = lrX + (rightX - lrX) * t
    val py = oy - (oy - mrY) * (1f / ((1f + t * 0.85f) * (1f + t * 0.85f)))
    if (i == 0) z3Path.moveTo(px, py) else z3Path.lineTo(px, py)
  }
  drawPath(z3Path, if (selectedZone == 2) amber else emerald, style = Stroke(if (selectedZone == 2) 3.4.dp.toPx() else 2.4.dp.toPx()))

  // Vertical markers for Lp and Lr
  drawLine(textCol.copy(alpha = 0.4f), Offset(lpX, topY), Offset(lpX, oy), strokeWidth = 1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
  drawLine(textCol.copy(alpha = 0.4f), Offset(lrX, mrY), Offset(lrX, oy), strokeWidth = 1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
  cadLabel("L_p", lpX, oy + 14.dp.toPx(), textCol, sizeSp = 10f)
  cadLabel("L_r", lrX, oy + 14.dp.toPx(), textCol, sizeSp = 10f)
  cadLabel("M_p", ox + 18.dp.toPx(), topY - 7.dp.toPx(), cyan, sizeSp = 9.5f)
}

private fun DrawScope.drawSteelBoltBearingDiagram(
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val pLeft = w * 0.16f
  val pTop = h * 0.30f
  val pW = w * 0.62f
  val pH = h * 0.40f

  drawRect(cyan.copy(alpha = 0.14f), Offset(pLeft, pTop), Size(pW, pH))
  drawRect(textCol, Offset(pLeft, pTop), Size(pW, pH), style = Stroke(2.dp.toPx()))

  val holeCx = pLeft + pW * 0.72f
  val holeCy = pTop + pH * 0.5f
  val holeR = h * 0.09f
  drawCircle(amber.copy(alpha = 0.32f), radius = holeR, center = Offset(holeCx, holeCy))
  drawCircle(amber, radius = holeR, center = Offset(holeCx, holeCy), style = Stroke(2.dp.toPx()))

  // Tear-out shear planes from hole edges to free plate edge
  drawLine(Color(0xFFEF4444), Offset(holeCx, holeCy - holeR), Offset(pLeft + pW, holeCy - holeR), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
  drawLine(Color(0xFFEF4444), Offset(holeCx, holeCy + holeR), Offset(pLeft + pW, holeCy + holeR), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
  drawArrow(Offset(holeCx + holeR, holeCy), Offset(pLeft + pW + w * 0.10f, holeCy), amber)

  cadLabel("L_c (Clear Edge)", (holeCx + holeR + pLeft + pW) * 0.5f + 6.dp.toPx(), pTop - 10.dp.toPx(), Color(0xFFEF4444), sizeSp = 9.5f)
  cadLabel("d_b + 2mm", holeCx, pTop + pH + 14.dp.toPx(), cyan, sizeSp = 9.5f)
  cadLabel("P", pLeft + pW + w * 0.09f, holeCy - 9.dp.toPx(), amber, sizeSp = 10.5f)
}

// ============================================================================
// FOUNDATION ENGINEERING (NSCP 2015 SEC. 300s) — INTERACTIVE CAD LABS
// ============================================================================

@Composable
private fun FoundationGwtVisualizerCard(accentColor: Color) {
  var selectedCase by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "Case I: 0 ≤ Dw ≤ Df",
    "Case II: Df < Dw < Df+B",
    "Case III: Dw ≥ Df+B"
  )
  val bottomInfo = when (selectedCase) {
    1 -> "Case II (Df < Dw < Df+B): q = γ·Df  •  Wedge γ̄ = γ' + (d/B)·(γ - γ')   [d = Dw - Df]"
    2 -> "Case III (Dw ≥ Df+B): q = γ·Df  •  Wedge γ̄ = γ  (Deep GWT • No Reduction)"
    else -> "Case I (0 ≤ Dw ≤ Df): q = γ·Dw + γ'·(Df - Dw)  •  Wedge γ̄ = γ' = γ_sat - γ_w"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedCase,
      accentColor = accentColor,
      testTagPrefix = "fnd_gwt_case",
      onSelect = { selectedCase = it }
    )
    PsadCadCanvasShell(
      heightDp = 204,
      accentColor = accentColor,
      topBadge = "GWT BEARING CORRECTION • 3-CASE VISUALIZER",
      bottomFormula = bottomInfo,
      testTag = "fnd_gwt_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawFoundationGwtVisualizer(selectedCase, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun FoundationEccentricPressureCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "Full Contact (e ≤ B/6)",
    "Partial Uplift (e > B/6)"
  )
  val bottomInfo = when (selectedMode) {
    1 -> "Partial Uplift (e > B/6): q_max = 2P / [3L·(B/2 - e)]  •  Effective Contact = 3(B/2 - e)"
    else -> "Full Contact (e ≤ B/6): q_max,min = (P / B·L)·(1 ± 6e / B)  •  Core Kern Width = B/3"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "fnd_eccentric_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 196,
      accentColor = accentColor,
      topBadge = "ECCENTRIC FOOTING • BASE PRESSURE DISTRIBUTION",
      bottomFormula = bottomInfo,
      testTag = "fnd_eccentric_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawFoundationEccentricPressure(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun FoundationPileVisualizerCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "Single Pile (Qp + Qs)",
    "Pile Group Block (Lg × Bg)"
  )
  val bottomInfo = when (selectedMode) {
    1 -> "Group Block: Q_g(u) = L_g B_g c_u N*_c + 2(L_g + B_g) L c_u  •  Check η·ΣQ_u vs Q_g(u)"
    else -> "Single Pile (Sec. 306.2): Q_u = Q_p + Q_s = q_p A_p + Σ f·p·ΔL  (Clay: 9c_u A_p + Σαc_u pΔL)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "fnd_pile_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 200,
      accentColor = accentColor,
      topBadge = "NSCP 306.2 • DEEP PILE & GROUP BLOCK CAPACITY",
      bottomFormula = bottomInfo,
      testTag = "fnd_pile_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawFoundationPileDiagram(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun FoundationRetainingWallCard(accentColor: Color) {
  val context = LocalContext.current
  val localView = LocalView.current
  val palette = currentGlassPalette()
  var selectedCheck by remember { mutableStateOf("overturning") }

  val hotspots = remember {
    listOf(
      "overturning" to { w: Float, h: Float -> Offset(w * 0.68f, h * 0.48f) },
      "sliding" to { w: Float, h: Float -> Offset(w * 0.44f, h * 0.78f) },
      "bearing" to { w: Float, h: Float -> Offset(w * 0.28f, h * 0.84f) }
    )
  }

  val pillSpecs = listOf(
    "overturning" to "Overturning FS ≥ 2.0",
    "sliding" to "Sliding FS ≥ 1.5",
    "bearing" to "Base Pressure q_toe ≤ q_all"
  )

  val detailText = when (selectedCheck) {
    "sliding" -> "Sliding: FS = [ (ΣV)tan(k₁φ₂) + B k₂ c₂ + P_p ] / (P_a cos α) ≥ 1.5   (k₁,k₂ ≈ 2/3)"
    "bearing" -> "Bearing: e = B/2 - (ΣM_R - ΣM_O)/ΣV ≤ B/6  •  q_toe,heel = (ΣV/B)(1 ± 6e/B) ≤ q_all"
    else -> "Overturning about Toe O: FS = ΣM_R / ΣM_O ≥ 2.0  (W_1, W_2, W_soil resist P_a)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      pillSpecs.forEach { (id, label) ->
        val isSelected = selectedCheck == id
        RecessedGlassPill(
          isDarkMode = palette.isDark,
          fillColor = if (isSelected) accentColor.copy(alpha = if (palette.isDark) 0.28f else 0.16f) else null,
          borderColor = if (isSelected) accentColor.copy(alpha = 0.72f) else null,
          onClick = {
            playCardHapticSound(context, localView)
            selectedCheck = id
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("fnd_wall_pill_$id")
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = AppleSansFamily,
              fontSize = 10.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) (if (palette.isDark) Color.White else accentColor) else palette.textSecondary
          )
        }
      }
    }

    PsadCadCanvasShell(
      heightDp = 216,
      accentColor = accentColor,
      topBadge = "NSCP 308 • CANTILEVER RETAINING WALL • TAP FORCES",
      bottomFormula = detailText,
      testTag = "fnd_retaining_wall_canvas",
      tapHotspots = hotspots,
      onTapHotspot = { id ->
        playCardHapticSound(context, localView)
        selectedCheck = id
      }
    ) { cyan, amber, emerald, textCol ->
      drawFoundationRetainingWall(selectedCheck, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

private fun DrawScope.drawFoundationGwtVisualizer(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val groundY = h * 0.24f
  val baseY = h * 0.50f
  val wedgeBotY = h * 0.80f
  val footW = w * 0.34f
  val footLeft = w * 0.50f - footW * 0.5f
  val footRight = footLeft + footW

  // GWT Y level by case
  val gwtY = when (mode) {
    1 -> baseY + (wedgeBotY - baseY) * 0.48f
    2 -> wedgeBotY + h * 0.08f
    else -> groundY + (baseY - groundY) * 0.45f
  }

  // Water zone translucent shading below GWT
  drawRect(
    color = cyan.copy(alpha = 0.14f),
    topLeft = Offset(w * 0.08f, gwtY),
    size = Size(w * 0.84f, (h * 0.94f - gwtY).coerceAtLeast(0f))
  )

  // Ground surface
  drawLine(emerald, Offset(w * 0.08f, groundY), Offset(w * 0.92f, groundY), strokeWidth = 2.dp.toPx())
  cadLabel("G.S.", w * 0.12f, groundY - 6.dp.toPx(), emerald, sizeSp = 9.5f)

  // Failure wedge zone below base (depth B)
  val wedgePath = Path().apply {
    moveTo(footLeft, baseY)
    lineTo(w * 0.50f, wedgeBotY)
    lineTo(footRight, baseY)
    close()
  }
  drawPath(wedgePath, amber.copy(alpha = 0.12f))
  drawPath(
    wedgePath,
    amber.copy(alpha = 0.72f),
    style = Stroke(width = 1.6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)))
  )

  // Spread Footing + Column Stub
  val colW = footW * 0.26f
  val colLeft = w * 0.50f - colW * 0.5f
  drawRect(textCol.copy(alpha = 0.24f), Offset(colLeft, groundY - 12.dp.toPx()), Size(colW, baseY - groundY))
  drawRect(textCol, Offset(colLeft, groundY - 12.dp.toPx()), Size(colW, baseY - groundY), style = Stroke(1.8.dp.toPx()))
  drawRect(accent.copy(alpha = 0.28f), Offset(footLeft, baseY - 14.dp.toPx()), Size(footW, 14.dp.toPx()))
  drawRect(textCol, Offset(footLeft, baseY - 14.dp.toPx()), Size(footW, 14.dp.toPx()), style = Stroke(2.dp.toPx()))
  cadLabel("B", w * 0.50f, baseY + 12.dp.toPx(), textCol, sizeSp = 10f)

  // Glowing GWT line + triangle symbol
  drawLine(cyan, Offset(w * 0.08f, gwtY), Offset(w * 0.92f, gwtY), strokeWidth = 2.4.dp.toPx())
  val triX = w * 0.82f
  val triPath = Path().apply {
    moveTo(triX - 6.dp.toPx(), gwtY - 7.dp.toPx())
    lineTo(triX + 6.dp.toPx(), gwtY - 7.dp.toPx())
    lineTo(triX, gwtY)
    close()
  }
  drawPath(triPath, cyan)
  cadLabel("G.W.T.", triX, gwtY - 10.dp.toPx(), cyan, sizeSp = 9.5f)

  // Right-side depth brackets Df and B
  val dimX = w * 0.20f
  drawLine(textCol.copy(alpha = 0.7f), Offset(dimX, groundY), Offset(dimX, baseY), strokeWidth = 1.4.dp.toPx())
  cadLabel("Df", dimX - 14.dp.toPx(), (groundY + baseY) * 0.5f + 3.dp.toPx(), textCol, sizeSp = 9.5f)
  drawLine(amber, Offset(dimX, baseY), Offset(dimX, wedgeBotY), strokeWidth = 1.4.dp.toPx())
  cadLabel("B", dimX - 12.dp.toPx(), (baseY + wedgeBotY) * 0.5f + 3.dp.toPx(), amber, sizeSp = 9.5f)

  // Mode-specific Dw / d label
  val dLabel = when (mode) {
    1 -> "d = Dw - Df"
    2 -> "Dw ≥ Df + B"
    else -> "Dw ≤ Df"
  }
  cadLabel(dLabel, w * 0.77f, gwtY + 13.dp.toPx(), cyan, sizeSp = 9.5f)
}

private fun DrawScope.drawFoundationEccentricPressure(
  isPartialUplift: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val fLeft = w * 0.16f
  val fRight = w * 0.84f
  val fW = fRight - fLeft
  val fTop = h * 0.34f
  val fBot = h * 0.46f
  val midX = w * 0.50f

  // Footing body & centerline
  drawRect(cyan.copy(alpha = 0.18f), Offset(fLeft, fTop), Size(fW, fBot - fTop))
  drawRect(textCol, Offset(fLeft, fTop), Size(fW, fBot - fTop), style = Stroke(2.dp.toPx()))
  drawLine(
    textCol.copy(alpha = 0.45f),
    Offset(midX, h * 0.16f),
    Offset(midX, h * 0.88f),
    strokeWidth = 1.2.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
  )

  // Kern limits (±B/6)
  val kernLeft = midX - fW / 6f
  val kernRight = midX + fW / 6f
  drawLine(emerald, Offset(kernLeft, fTop - 4.dp.toPx()), Offset(kernLeft, fBot + 4.dp.toPx()), strokeWidth = 1.6.dp.toPx())
  drawLine(emerald, Offset(kernRight, fTop - 4.dp.toPx()), Offset(kernRight, fBot + 4.dp.toPx()), strokeWidth = 1.6.dp.toPx())

  // Eccentric load arrow P
  val loadX = if (isPartialUplift) midX + fW * 0.28f else midX + fW * 0.11f
  drawArrow(Offset(loadX, h * 0.14f), Offset(loadX, fTop - 2.dp.toPx()), amber)
  drawArrow(Offset(midX, h * 0.24f), Offset(loadX, h * 0.24f), amber, strokePx = 1.4.dp.toPx(), headLenPx = 4.5.dp.toPx())
  cadLabel("P", loadX + 12.dp.toPx(), h * 0.20f, amber, sizeSp = 11f)
  cadLabel("e", (midX + loadX) * 0.5f, h * 0.21f, amber, sizeSp = 9.5f)

  val pBaseY = h * 0.84f
  if (!isPartialUplift) {
    val qMinH = h * 0.10f
    val qMaxH = h * 0.28f
    val stressPath = Path().apply {
      moveTo(fLeft, pBaseY)
      lineTo(fLeft, pBaseY - qMinH)
      lineTo(fRight, pBaseY - qMaxH)
      lineTo(fRight, pBaseY)
      close()
    }
    drawPath(stressPath, emerald.copy(alpha = 0.22f))
    drawPath(stressPath, emerald, style = Stroke(2.dp.toPx()))
    for (i in 0..6) {
      val frac = i / 6f
      val ax = fLeft + fW * frac
      val ayTop = pBaseY - (qMinH + (qMaxH - qMinH) * frac)
      drawArrow(Offset(ax, pBaseY), Offset(ax, ayTop), emerald, strokePx = 1.5.dp.toPx(), headLenPx = 4.5.dp.toPx())
    }
    cadLabel("q_min", fLeft + 16.dp.toPx(), pBaseY - qMinH - 7.dp.toPx(), emerald, sizeSp = 9.5f)
    cadLabel("q_max", fRight - 16.dp.toPx(), pBaseY - qMaxH - 7.dp.toPx(), emerald, sizeSp = 9.5f)
  } else {
    val contactStartX = fRight - fW * 0.62f
    val qMaxH = h * 0.32f
    val stressPath = Path().apply {
      moveTo(contactStartX, pBaseY)
      lineTo(fRight, pBaseY - qMaxH)
      lineTo(fRight, pBaseY)
      close()
    }
    drawPath(stressPath, Color(0xFFEF4444).copy(alpha = 0.22f))
    drawPath(stressPath, Color(0xFFEF4444), style = Stroke(2.dp.toPx()))
    for (i in 1..4) {
      val frac = i / 4f
      val ax = contactStartX + (fRight - contactStartX) * frac
      val ayTop = pBaseY - qMaxH * frac
      drawArrow(Offset(ax, pBaseY), Offset(ax, ayTop), Color(0xFFEF4444), strokePx = 1.5.dp.toPx(), headLenPx = 4.5.dp.toPx())
    }
    cadLabel("Uplift Gap", (fLeft + contactStartX) * 0.5f, pBaseY - 8.dp.toPx(), Color(0xFFEF4444), sizeSp = 9.5f)
    cadLabel("3(B/2 - e)", (contactStartX + fRight) * 0.5f, pBaseY + 12.dp.toPx(), textCol, sizeSp = 9.5f)
    cadLabel("q_max", fRight - 14.dp.toPx(), pBaseY - qMaxH - 7.dp.toPx(), Color(0xFFEF4444), sizeSp = 9.5f)
  }
}

private fun DrawScope.drawFoundationPileDiagram(
  isGroupBlock: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val capTop = h * 0.22f
  val capBot = h * 0.34f
  val tipY = h * 0.78f

  // Ground line
  drawLine(emerald.copy(alpha = 0.65f), Offset(w * 0.08f, capBot), Offset(w * 0.92f, capBot), strokeWidth = 1.6.dp.toPx())

  if (!isGroupBlock) {
    // Single Pile Elevation
    val capW = w * 0.26f
    val capLeft = w * 0.50f - capW * 0.5f
    drawRect(textCol.copy(alpha = 0.22f), Offset(capLeft, capTop), Size(capW, capBot - capTop))
    drawRect(textCol, Offset(capLeft, capTop), Size(capW, capBot - capTop), style = Stroke(1.8.dp.toPx()))

    val pileW = w * 0.10f
    val pileLeft = w * 0.50f - pileW * 0.5f
    drawRect(cyan.copy(alpha = 0.25f), Offset(pileLeft, capBot), Size(pileW, tipY - capBot))
    drawRect(cyan, Offset(pileLeft, capBot), Size(pileW, tipY - capBot), style = Stroke(2.dp.toPx()))

    // Applied load Q_u
    drawArrow(Offset(w * 0.50f, h * 0.08f), Offset(w * 0.50f, capTop - 2.dp.toPx()), amber)
    cadLabel("Q_u", w * 0.50f + 18.dp.toPx(), h * 0.16f, amber, sizeSp = 10.5f)

    // Skin friction arrows (Qs)
    for (i in 0..2) {
      val ay = capBot + (tipY - capBot) * (0.24f + i * 0.26f)
      drawArrow(Offset(pileLeft - 8.dp.toPx(), ay + 12.dp.toPx()), Offset(pileLeft - 8.dp.toPx(), ay - 6.dp.toPx()), emerald, strokePx = 1.6.dp.toPx(), headLenPx = 4.5.dp.toPx())
      drawArrow(Offset(pileLeft + pileW + 8.dp.toPx(), ay + 12.dp.toPx()), Offset(pileLeft + pileW + 8.dp.toPx(), ay - 6.dp.toPx()), emerald, strokePx = 1.6.dp.toPx(), headLenPx = 4.5.dp.toPx())
    }
    cadLabel("Q_s = Σ f·p·ΔL", w * 0.24f, (capBot + tipY) * 0.5f, emerald, sizeSp = 9.5f)

    // Tip bearing arrows (Qp)
    drawArrow(Offset(w * 0.50f, h * 0.92f), Offset(w * 0.50f, tipY + 3.dp.toPx()), accent, strokePx = 2.2.dp.toPx(), headLenPx = 6.dp.toPx())
    cadLabel("Q_p = q_p·A_p", w * 0.72f, tipY + 14.dp.toPx(), accent, sizeSp = 9.5f)
  } else {
    // Pile Group Elevation (left) + Block Plan Lg x Bg (right)
    val grpLeft = w * 0.14f
    val grpW = w * 0.34f
    drawRect(textCol.copy(alpha = 0.22f), Offset(grpLeft, capTop), Size(grpW, capBot - capTop))
    drawRect(textCol, Offset(grpLeft, capTop), Size(grpW, capBot - capTop), style = Stroke(1.8.dp.toPx()))
    // 3 piles
    val pW = grpW * 0.12f
    for (i in 0..2) {
      val px = grpLeft + grpW * (0.14f + i * 0.30f)
      drawRect(cyan.copy(alpha = 0.26f), Offset(px, capBot), Size(pW, tipY - capBot))
      drawRect(cyan, Offset(px, capBot), Size(pW, tipY - capBot), style = Stroke(1.6.dp.toPx()))
    }
    // Block outline
    drawRect(
      amber,
      Offset(grpLeft + grpW * 0.10f, capBot),
      Size(grpW * 0.80f, tipY - capBot),
      style = Stroke(1.6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)))
    )
    cadLabel("s", grpLeft + grpW * 0.35f, tipY + 14.dp.toPx(), cyan, sizeSp = 10f)
    cadLabel("Q_g(u)", grpLeft + grpW * 0.5f, h * 0.16f, amber, sizeSp = 10f)

    // Plan View of Pile Group Block Lg x Bg
    val planLeft = w * 0.60f
    val planTop = h * 0.28f
    val planW = w * 0.26f
    val planH = h * 0.48f
    drawRect(amber.copy(alpha = 0.14f), Offset(planLeft, planTop), Size(planW, planH))
    drawRect(amber, Offset(planLeft, planTop), Size(planW, planH), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))
    for (r in 0..2) {
      for (c in 0..2) {
        val cx = planLeft + planW * (0.20f + c * 0.30f)
        val cy = planTop + planH * (0.20f + r * 0.30f)
        drawCircle(cyan, radius = 4.2.dp.toPx(), center = Offset(cx, cy))
      }
    }
    cadLabel("L_g", planLeft + planW * 0.5f, planTop - 7.dp.toPx(), amber, sizeSp = 10f)
    cadLabel("B_g", planLeft + planW + 13.dp.toPx(), planTop + planH * 0.5f, amber, sizeSp = 10f)
  }
}

private fun DrawScope.drawFoundationRetainingWall(
  selectedCheck: String,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val topY = h * 0.22f
  val baseTopY = h * 0.68f
  val baseBotY = h * 0.76f

  val toeX = w * 0.22f
  val stemLeftX = w * 0.36f
  val stemRightX = w * 0.45f
  val heelX = w * 0.68f

  // Cantilever wall polygon (Stem + Toe + Heel + Shear Key)
  val wallPath = Path().apply {
    moveTo(stemLeftX, topY)
    lineTo(stemRightX - w * 0.02f, topY)
    lineTo(stemRightX, baseTopY)
    lineTo(heelX, baseTopY)
    lineTo(heelX, baseBotY)
    lineTo(stemRightX, baseBotY)
    lineTo(stemRightX, baseBotY + h * 0.06f)
    lineTo(stemLeftX + w * 0.02f, baseBotY + h * 0.06f)
    lineTo(stemLeftX + w * 0.02f, baseBotY)
    lineTo(toeX, baseBotY)
    lineTo(toeX, baseTopY)
    lineTo(stemLeftX, baseTopY)
    close()
  }
  drawPath(wallPath, textCol.copy(alpha = 0.18f))
  drawPath(wallPath, textCol, style = Stroke(2.dp.toPx(), join = StrokeJoin.Round))

  // Backfill surface
  drawLine(emerald.copy(alpha = 0.65f), Offset(stemRightX, topY), Offset(w * 0.90f, topY), strokeWidth = 1.6.dp.toPx())

  // Active Earth Pressure Triangle on Heel Plane (x = heelX)
  val paColor = if (selectedCheck == "overturning" || selectedCheck == "sliding") Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.45f)
  val paPath = Path().apply {
    moveTo(heelX, topY)
    lineTo(w * 0.85f, baseBotY)
    lineTo(heelX, baseBotY)
    close()
  }
  drawPath(paPath, paColor.copy(alpha = 0.20f))
  drawPath(paPath, paColor, style = Stroke(1.8.dp.toPx()))
  val paY = topY + (baseBotY - topY) * 0.67f
  drawArrow(Offset(w * 0.86f, paY), Offset(heelX + 2.dp.toPx(), paY), paColor, strokePx = 2.4.dp.toPx(), headLenPx = 6.dp.toPx())
  cadLabel("P_a", w * 0.86f, paY - 10.dp.toPx(), paColor, sizeSp = 10.5f)

  // Passive Pressure Pp at Toe
  val ppColor = if (selectedCheck == "sliding") emerald else emerald.copy(alpha = 0.55f)
  drawArrow(Offset(w * 0.10f, baseTopY + 6.dp.toPx()), Offset(toeX - 2.dp.toPx(), baseTopY + 6.dp.toPx()), ppColor, strokePx = 2.dp.toPx(), headLenPx = 5.dp.toPx())
  cadLabel("P_p", w * 0.14f, baseTopY - 4.dp.toPx(), ppColor, sizeSp = 9.5f)

  // Vertical Weight Vectors (W_soil & W_wall)
  val wColor = if (selectedCheck == "overturning" || selectedCheck == "bearing") amber else amber.copy(alpha = 0.50f)
  drawArrow(Offset(w * 0.57f, h * 0.36f), Offset(w * 0.57f, baseTopY - 4.dp.toPx()), wColor, strokePx = 2.dp.toPx(), headLenPx = 5.dp.toPx())
  cadLabel("W_soil", w * 0.57f, h * 0.32f, wColor, sizeSp = 9.5f)
  drawArrow(Offset((stemLeftX + stemRightX) * 0.5f, h * 0.38f), Offset((stemLeftX + stemRightX) * 0.5f, h * 0.54f), wColor, strokePx = 1.8.dp.toPx(), headLenPx = 4.5.dp.toPx())
  cadLabel("W_c", stemLeftX - 14.dp.toPx(), h * 0.45f, wColor, sizeSp = 9.5f)

  // Toe pivot point O
  drawCircle(accent, radius = 4.dp.toPx(), center = Offset(toeX, baseBotY))
  cadLabel("O", toeX - 10.dp.toPx(), baseBotY + 12.dp.toPx(), accent, sizeSp = 10f)

  // Base Bearing Stress Trapezoid (q_toe -> q_heel)
  val qColor = if (selectedCheck == "bearing") cyan else cyan.copy(alpha = 0.45f)
  val qTopY = h * 0.92f
  val qToeTop = qTopY - h * 0.12f
  val qHeelTop = qTopY - h * 0.05f
  val qPath = Path().apply {
    moveTo(toeX, qTopY)
    lineTo(toeX, qToeTop)
    lineTo(heelX, qHeelTop)
    lineTo(heelX, qTopY)
    close()
  }
  drawPath(qPath, qColor.copy(alpha = 0.20f))
  drawPath(qPath, qColor, style = Stroke(1.8.dp.toPx()))
  cadLabel("q_toe", toeX + 16.dp.toPx(), qTopY + 12.dp.toPx(), qColor, sizeSp = 9.5f)
  cadLabel("q_heel", heelX - 14.dp.toPx(), qTopY + 12.dp.toPx(), qColor, sizeSp = 9.5f)
}

// ============================================================================
// HYDRAULICS & FLUID MECHANICS (HPGE • 01) — INTERACTIVE CAD LABS
// ============================================================================

@Composable
private fun HydraulicsCapillaryCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "Capillary Rise (Water θ ≈ 0°)",
    "Capillary Depression (Mercury θ ≈ 140°)"
  )
  val bottomInfo = when (selectedMode) {
    1 -> "Mercury in Glass (θ ≈ 140°, cos θ < 0): Convex meniscus  •  Depression h = 4σ cos θ / (γ d)"
    else -> "Water in Glass (θ ≈ 0°, Adhesion > Cohesion): Concave meniscus  •  Rise h = 4σ cos θ / (γ d)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "hyd_capillary_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 188,
      accentColor = accentColor,
      topBadge = "HPGE • 01  |  CAPILLARY MENISCUS BLUEPRINT",
      bottomFormula = bottomInfo,
      testTag = "hyd_capillary_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawHydraulicsCapillary(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun HydraulicsSubmergedGateCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf("Vertical Gate", "Inclined Gate (θ)", "Curved Radial Gate")
  val bottomInfo = when (selectedMode) {
    1 -> "Inclined Plane (θ): F = γ h̄ A  •  e = I_g / (A ȳ)  •  y_p = ȳ + e  (y measured along plane)"
    2 -> "Curved Gate: F_H = γ h̄ A_proj  •  F_V = γ V_above/displaced  •  F_R = √(F_H² + F_V²)"
    else -> "Vertical Gate: F = γ h̄ A  •  Pressure Prism Centroid = cp  •  e = I_g / (A h̄) = h / 6 (if top @ WS)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "hyd_gate_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 196,
      accentColor = accentColor,
      topBadge = "HYDROSTATIC PRESSURE PRISM & CENTER OF PRESSURE",
      bottomFormula = bottomInfo,
      testTag = "hyd_gate_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawHydraulicsSubmergedGate(selectedMode, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun HydraulicsMetacenterCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf("Stable: M above G", "Unstable: M below G")
  val bottomInfo = when (selectedMode) {
    1 -> "Unstable (MG < 0): M falls below G  ⇒  Couple creates Overturning Moment (Vessel capsizes)"
    else -> "Stable (MG > 0): M lies above G  •  MG = MB_o - GB_o > 0  ⇒  Righting Moment RM = W·MG sin θ"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "hyd_metacenter_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 196,
      accentColor = accentColor,
      topBadge = "FLOATING STABILITY • METACENTER M vs. C.G. (G)",
      bottomFormula = bottomInfo,
      testTag = "hyd_metacenter_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawHydraulicsMetacenter(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun HydraulicsTankMotionCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf("Linear Acceleration (ax)", "Rotating Cylinder (ω)")
  val bottomInfo = when (selectedMode) {
    1 -> "Rotating Vortex (ω): y = ω²x² / (2g)  •  h = ω²R² / (2g)  •  Paraboloid Vol = (1/2) π R² h"
    else -> "Horizontal Acceleration (a_x): Free surface tilts at tan θ = a_x / g  (Rise = Fall at ends)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "hyd_tank_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 192,
      accentColor = accentColor,
      topBadge = "RELATIVE EQUILIBRIUM • FREE SURFACE PROFILE",
      bottomFormula = bottomInfo,
      testTag = "hyd_tank_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawHydraulicsTankMotion(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun HydraulicsOpenChannelCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf("Most Efficient Sections", "Hydraulic Jump (y₁ → y₂)")
  val bottomInfo = when (selectedMode) {
    1 -> "Hydraulic Jump: y₂/y₁ = 0.5(√(1 + 8 Fr₁²) - 1)  •  Energy Loss ΔE = (y₂ - y₁)³ / (4 y₁ y₂)"
    else -> "Most Efficient: Rect (b = 2y, R = y/2)  •  Trap (Half-Hex θ = 60°, R = y/2)  •  Tri (90°, R = y/2√2)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "hyd_channel_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 196,
      accentColor = accentColor,
      topBadge = "OPEN CHANNEL FLOW & HYDRAULIC JUMP",
      bottomFormula = bottomInfo,
      testTag = "hyd_channel_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawHydraulicsOpenChannel(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

private fun DrawScope.drawHydraulicsCapillary(
  isMercury: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val resY = h * 0.56f
  val fluidCol = if (isMercury) Color(0xFF94A3B8) else cyan

  // Reservoir water/mercury bath
  drawRect(fluidCol.copy(alpha = 0.22f), Offset(w * 0.14f, resY), Size(w * 0.72f, h * 0.30f))
  drawRect(textCol, Offset(w * 0.14f, resY - 20.dp.toPx()), Size(w * 0.72f, h * 0.30f + 20.dp.toPx()), style = Stroke(1.8.dp.toPx()))
  drawLine(fluidCol, Offset(w * 0.14f, resY), Offset(w * 0.86f, resY), strokeWidth = 2.dp.toPx())

  // Vertical glass capillary tube
  val tubeW = w * 0.10f
  val tubeL = w * 0.50f - tubeW * 0.5f
  val tubeR = tubeL + tubeW
  val tubeTop = h * 0.18f
  val tubeBot = h * 0.82f
  val colTop = if (isMercury) resY + h * 0.16f else h * 0.28f

  drawRect(fluidCol.copy(alpha = 0.36f), Offset(tubeL, colTop), Size(tubeW, tubeBot - colTop))
  drawLine(textCol, Offset(tubeL, tubeTop), Offset(tubeL, tubeBot), strokeWidth = 2.dp.toPx())
  drawLine(textCol, Offset(tubeR, tubeTop), Offset(tubeR, tubeBot), strokeWidth = 2.dp.toPx())

  // Curved Meniscus inside tube
  val meniscusPath = Path().apply {
    moveTo(tubeL, colTop)
    val ctrlY = if (isMercury) colTop - 10.dp.toPx() else colTop + 10.dp.toPx()
    quadraticTo(w * 0.50f, ctrlY, tubeR, colTop)
  }
  drawPath(meniscusPath, amber, style = Stroke(2.4.dp.toPx()))

  // Height bracket h
  val brX = tubeR + 16.dp.toPx()
  drawLine(amber, Offset(brX, resY), Offset(brX, colTop), strokeWidth = 1.8.dp.toPx())
  cadLabel(if (isMercury) "-h (Drop)" else "+h (Rise)", brX + 36.dp.toPx(), (resY + colTop) * 0.5f + 4.dp.toPx(), amber, sizeSp = 10f)
  cadLabel(if (isMercury) "θ ≈ 140°" else "θ ≈ 0°", w * 0.28f, colTop, emerald, sizeSp = 10f)
}

private fun DrawScope.drawHydraulicsManometer(
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val pipeAX = w * 0.16f
  val pipeAY = h * 0.34f
  val pipeBX = w * 0.84f
  val pipeBY = h * 0.28f

  // Pipe A and Pipe B circles
  drawCircle(cyan.copy(alpha = 0.25f), radius = 14.dp.toPx(), center = Offset(pipeAX, pipeAY))
  drawCircle(textCol, radius = 14.dp.toPx(), center = Offset(pipeAX, pipeAY), style = Stroke(2.dp.toPx()))
  cadLabel("A", pipeAX, pipeAY + 4.dp.toPx(), textCol, sizeSp = 11f)

  drawCircle(emerald.copy(alpha = 0.25f), radius = 14.dp.toPx(), center = Offset(pipeBX, pipeBY))
  drawCircle(textCol, radius = 14.dp.toPx(), center = Offset(pipeBX, pipeBY), style = Stroke(2.dp.toPx()))
  cadLabel("B", pipeBX, pipeBY + 4.dp.toPx(), textCol, sizeSp = 11f)

  // U-Tube centerline path
  val xL = w * 0.34f
  val xR = w * 0.66f
  val yBot = h * 0.80f
  val yMenL = h * 0.64f
  val yMenR = h * 0.48f

  // Tube walls
  val tubePath = Path().apply {
    moveTo(pipeAX + 14.dp.toPx(), pipeAY)
    lineTo(xL, pipeAY)
    lineTo(xL, yBot)
    lineTo(xR, yBot)
    lineTo(xR, pipeBY)
    lineTo(pipeBX - 14.dp.toPx(), pipeBY)
  }
  drawPath(tubePath, textCol.copy(alpha = 0.75f), style = Stroke(10.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))

  // Mercury gage fluid (amber) at bottom
  val hgPath = Path().apply {
    moveTo(xL, yMenL)
    lineTo(xL, yBot)
    lineTo(xR, yBot)
    lineTo(xR, yMenR)
  }
  drawPath(hgPath, amber, style = Stroke(6.dp.toPx(), join = StrokeJoin.Round))

  // Fluid 1 (left leg cyan) & Fluid 3 (right leg emerald)
  drawLine(cyan, Offset(pipeAX + 14.dp.toPx(), pipeAY), Offset(xL, pipeAY), strokeWidth = 6.dp.toPx())
  drawLine(cyan, Offset(xL, pipeAY), Offset(xL, yMenL), strokeWidth = 6.dp.toPx())
  drawLine(emerald, Offset(xR, yMenR), Offset(xR, pipeBY), strokeWidth = 6.dp.toPx())
  drawLine(emerald, Offset(xR, pipeBY), Offset(pipeBX - 14.dp.toPx(), pipeBY), strokeWidth = 6.dp.toPx())

  // Elevation dashed markers
  drawLine(amber.copy(alpha = 0.65f), Offset(xL - 12.dp.toPx(), yMenL), Offset(xR + 12.dp.toPx(), yMenL), strokeWidth = 1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
  cadLabel("h₁", xL - 20.dp.toPx(), (pipeAY + yMenL) * 0.5f, cyan, sizeSp = 10f)
  cadLabel("h₂", (xL + xR) * 0.5f, (yMenL + yMenR) * 0.5f, amber, sizeSp = 10f)
  cadLabel("h₃", xR + 20.dp.toPx(), (pipeBY + yMenR) * 0.5f, emerald, sizeSp = 10f)
}

private fun DrawScope.drawHydraulicsSubmergedGate(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val wsY = h * 0.22f

  // Water surface
  drawLine(cyan, Offset(w * 0.10f, wsY), Offset(w * 0.90f, wsY), strokeWidth = 2.dp.toPx())
  cadLabel("W.S.", w * 0.15f, wsY - 6.dp.toPx(), cyan, sizeSp = 9.5f)

  when (mode) {
    1 -> { // Inclined gate
      val topPt = Offset(w * 0.44f, h * 0.36f)
      val botPt = Offset(w * 0.64f, h * 0.80f)
      drawLine(textCol, topPt, botPt, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
      val cgPt = Offset((topPt.x + botPt.x) * 0.5f, (topPt.y + botPt.y) * 0.5f)
      val cpPt = Offset(topPt.x + (botPt.x - topPt.x) * 0.62f, topPt.y + (botPt.y - topPt.y) * 0.62f)
      drawCircle(emerald, radius = 4.dp.toPx(), center = cgPt)
      drawCircle(amber, radius = 4.5.dp.toPx(), center = cpPt)
      drawArrow(Offset(cpPt.x - 42.dp.toPx(), cpPt.y + 18.dp.toPx()), Offset(cpPt.x - 4.dp.toPx(), cpPt.y + 2.dp.toPx()), amber)
      cadLabel("cg", cgPt.x + 16.dp.toPx(), cgPt.y - 4.dp.toPx(), emerald, sizeSp = 9.5f)
      cadLabel("cp", cpPt.x + 16.dp.toPx(), cpPt.y + 6.dp.toPx(), amber, sizeSp = 9.5f)
      cadLabel("F = γ h̄ A", cpPt.x - 54.dp.toPx(), cpPt.y + 28.dp.toPx(), amber, sizeSp = 10f)
    }
    2 -> { // Curved radial gate
      val pivot = Offset(w * 0.66f, h * 0.32f)
      val r = h * 0.46f
      drawArc(textCol, startAngle = 120f, sweepAngle = 55f, useCenter = false, topLeft = Offset(pivot.x - r, pivot.y - r), size = Size(r * 2, r * 2), style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round))
      drawArrow(Offset(w * 0.24f, h * 0.62f), Offset(w * 0.38f, h * 0.62f), cyan)
      drawArrow(Offset(w * 0.48f, h * 0.42f), Offset(w * 0.48f, h * 0.58f), emerald)
      drawArrow(Offset(w * 0.28f, h * 0.48f), Offset(w * 0.42f, h * 0.60f), amber)
      cadLabel("F_H", w * 0.20f, h * 0.65f, cyan, sizeSp = 10f)
      cadLabel("F_V", w * 0.54f, h * 0.46f, emerald, sizeSp = 10f)
      cadLabel("F_R", w * 0.24f, h * 0.45f, amber, sizeSp = 10f)
    }
    else -> { // Vertical gate with trapezoidal pressure prism
      val gx = w * 0.56f
      val gTop = h * 0.34f
      val gBot = h * 0.82f
      drawLine(textCol, Offset(gx, gTop), Offset(gx, gBot), strokeWidth = 4.dp.toPx())
      val prism = Path().apply {
        moveTo(gx, gTop)
        lineTo(gx - w * 0.14f, gTop)
        lineTo(gx - w * 0.30f, gBot)
        lineTo(gx, gBot)
        close()
      }
      drawPath(prism, cyan.copy(alpha = 0.20f))
      drawPath(prism, cyan, style = Stroke(1.8.dp.toPx()))
      val cgY = (gTop + gBot) * 0.5f
      val cpY = gTop + (gBot - gTop) * 0.60f
      drawCircle(emerald, radius = 4.dp.toPx(), center = Offset(gx, cgY))
      drawCircle(amber, radius = 4.5.dp.toPx(), center = Offset(gx, cpY))
      drawArrow(Offset(gx - w * 0.26f, cpY), Offset(gx - 4.dp.toPx(), cpY), amber)
      cadLabel("cg", gx + 14.dp.toPx(), cgY, emerald, sizeSp = 9.5f)
      cadLabel("cp", gx + 14.dp.toPx(), cpY + 6.dp.toPx(), amber, sizeSp = 9.5f)
      cadLabel("e", gx + 28.dp.toPx(), (cgY + cpY) * 0.5f + 4.dp.toPx(), accent, sizeSp = 9.5f)
    }
  }
}

private fun DrawScope.drawHydraulicsMetacenter(
  isUnstable: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val wsY = h * 0.54f

  // Waterline
  drawLine(cyan, Offset(w * 0.10f, wsY), Offset(w * 0.90f, wsY), strokeWidth = 2.dp.toPx())
  drawRect(cyan.copy(alpha = 0.14f), Offset(w * 0.10f, wsY), Size(w * 0.80f, h * 0.34f))

  // Tilted barge hull polygon
  val cx = w * 0.50f
  val cy = h * 0.52f
  val hull = Path().apply {
    moveTo(cx - w * 0.20f, cy - h * 0.20f)
    lineTo(cx + w * 0.22f, cy - h * 0.08f)
    lineTo(cx + w * 0.16f, cy + h * 0.24f)
    lineTo(cx - w * 0.26f, cy + h * 0.12f)
    close()
  }
  drawPath(hull, textCol.copy(alpha = 0.16f))
  drawPath(hull, textCol, style = Stroke(2.2.dp.toPx(), join = StrokeJoin.Round))

  // Centerline of vessel (tilted axis)
  val mPt = if (isUnstable) Offset(cx - 2.dp.toPx(), cy + 6.dp.toPx()) else Offset(cx + 6.dp.toPx(), cy - 26.dp.toPx())
  val gPt = if (isUnstable) Offset(cx + 6.dp.toPx(), cy - 22.dp.toPx()) else Offset(cx + 1.dp.toPx(), cy - 4.dp.toPx())
  val bPrimePt = Offset(cx - 10.dp.toPx(), cy + 16.dp.toPx())

  drawLine(textCol.copy(alpha = 0.5f), Offset(cx - 16.dp.toPx(), cy + 28.dp.toPx()), Offset(cx + 12.dp.toPx(), cy - 36.dp.toPx()), strokeWidth = 1.3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
  // Vertical BF upward from B'
  drawArrow(Offset(bPrimePt.x, bPrimePt.y + 18.dp.toPx()), Offset(bPrimePt.x, mPt.y), cyan, strokePx = 2.dp.toPx(), headLenPx = 5.dp.toPx())
  // Vertical Weight W downward from G
  drawArrow(gPt, Offset(gPt.x, gPt.y + 28.dp.toPx()), Color(0xFFEF4444), strokePx = 2.dp.toPx(), headLenPx = 5.dp.toPx())

  drawCircle(cyan, radius = 4.dp.toPx(), center = bPrimePt)
  drawCircle(amber, radius = 4.5.dp.toPx(), center = gPt)
  drawCircle(if (isUnstable) Color(0xFFEF4444) else emerald, radius = 5.dp.toPx(), center = mPt)

  cadLabel("B'", bPrimePt.x - 14.dp.toPx(), bPrimePt.y + 4.dp.toPx(), cyan, sizeSp = 9.5f)
  cadLabel("G", gPt.x + 12.dp.toPx(), gPt.y, amber, sizeSp = 10f)
  cadLabel("M", mPt.x + 12.dp.toPx(), mPt.y, if (isUnstable) Color(0xFFEF4444) else emerald, sizeSp = 10f)
  cadLabel(if (isUnstable) "Overturning!" else "Righting RM", w * 0.78f, h * 0.30f, if (isUnstable) Color(0xFFEF4444) else emerald, sizeSp = 10f)
}

private fun DrawScope.drawHydraulicsTankMotion(
  isRotating: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val tLeft = w * 0.20f
  val tRight = w * 0.80f
  val tTop = h * 0.24f
  val tBot = h * 0.80f

  if (!isRotating) {
    // Linear Acceleration Tilted Free Surface
    val surfPath = Path().apply {
      moveTo(tLeft, h * 0.34f)
      lineTo(tRight, h * 0.60f)
      lineTo(tRight, tBot)
      lineTo(tLeft, tBot)
      close()
    }
    drawPath(surfPath, cyan.copy(alpha = 0.25f))
    drawLine(cyan, Offset(tLeft, h * 0.34f), Offset(tRight, h * 0.60f), strokeWidth = 2.4.dp.toPx())
    drawLine(textCol.copy(alpha = 0.45f), Offset(tLeft, h * 0.47f), Offset(tRight, h * 0.47f), strokeWidth = 1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
    drawArrow(Offset(tLeft + 24.dp.toPx(), h * 0.20f), Offset(tRight - 24.dp.toPx(), h * 0.20f), amber)
    cadLabel("a_x", w * 0.50f, h * 0.16f, amber, sizeSp = 10.5f)
    cadLabel("tan θ = a_x / g", w * 0.50f, h * 0.72f, textCol, sizeSp = 10f)
  } else {
    // Parabolic Vortex inside rotating cylinder
    val vortexPath = Path()
    val steps = 24
    val midX = w * 0.50f
    val apexY = h * 0.64f
    val edgeY = h * 0.30f
    for (i in 0..steps) {
      val frac = (i / steps.toFloat()) * 2f - 1f // -1 to +1
      val px = midX + (tRight - midX) * frac
      val py = apexY - (apexY - edgeY) * (frac * frac)
      if (i == 0) vortexPath.moveTo(px, py) else vortexPath.lineTo(px, py)
    }
    val fillPath = Path().apply {
      addPath(vortexPath)
      lineTo(tRight, tBot)
      lineTo(tLeft, tBot)
      close()
    }
    drawPath(fillPath, cyan.copy(alpha = 0.25f))
    drawPath(vortexPath, cyan, style = Stroke(2.4.dp.toPx()))
    drawLine(amber.copy(alpha = 0.6f), Offset(midX, h * 0.18f), Offset(midX, tBot + 8.dp.toPx()), strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
    cadLabel("ω", midX + 12.dp.toPx(), h * 0.22f, amber, sizeSp = 11f)
    cadLabel("h = ω²R² / (2g)", w * 0.72f, (apexY + edgeY) * 0.5f, emerald, sizeSp = 9.5f)
  }

  // Open-top tank outline
  drawLine(textCol, Offset(tLeft, tTop), Offset(tLeft, tBot), strokeWidth = 2.4.dp.toPx())
  drawLine(textCol, Offset(tLeft, tBot), Offset(tRight, tBot), strokeWidth = 2.4.dp.toPx())
  drawLine(textCol, Offset(tRight, tBot), Offset(tRight, tTop), strokeWidth = 2.4.dp.toPx())
}

private fun DrawScope.drawHydraulicsOpenChannel(
  isJump: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  if (!isJump) {
    // Most Efficient Trapezoid (Left) + Rectangle (Right)
    val trapPath = Path().apply {
      moveTo(w * 0.10f, h * 0.34f)
      lineTo(w * 0.20f, h * 0.70f)
      lineTo(w * 0.36f, h * 0.70f)
      lineTo(w * 0.46f, h * 0.34f)
      close()
    }
    drawPath(trapPath, cyan.copy(alpha = 0.20f))
    drawLine(textCol, Offset(w * 0.10f, h * 0.34f), Offset(w * 0.20f, h * 0.70f), strokeWidth = 2.2.dp.toPx())
    drawLine(textCol, Offset(w * 0.20f, h * 0.70f), Offset(w * 0.36f, h * 0.70f), strokeWidth = 2.2.dp.toPx())
    drawLine(textCol, Offset(w * 0.36f, h * 0.70f), Offset(w * 0.46f, h * 0.34f), strokeWidth = 2.2.dp.toPx())
    cadLabel("θ = 60°", w * 0.28f, h * 0.84f, amber, sizeSp = 10f)
    cadLabel("Trap: R = y/2", w * 0.28f, h * 0.26f, cyan, sizeSp = 9.5f)

    // Most efficient rectangle (b = 2y)
    val rL = w * 0.56f
    val rW = w * 0.32f
    val rTop = h * 0.42f
    val rBot = h * 0.70f
    drawRect(emerald.copy(alpha = 0.20f), Offset(rL, rTop), Size(rW, rBot - rTop))
    drawLine(textCol, Offset(rL, rTop - 8.dp.toPx()), Offset(rL, rBot), strokeWidth = 2.2.dp.toPx())
    drawLine(textCol, Offset(rL, rBot), Offset(rL + rW, rBot), strokeWidth = 2.2.dp.toPx())
    drawLine(textCol, Offset(rL + rW, rBot), Offset(rL + rW, rTop - 8.dp.toPx()), strokeWidth = 2.2.dp.toPx())
    cadLabel("b = 2y", rL + rW * 0.5f, h * 0.84f, emerald, sizeSp = 10f)
    cadLabel("Rect: R = y/2", rL + rW * 0.5f, h * 0.26f, emerald, sizeSp = 9.5f)
  } else {
    // Hydraulic Jump y1 -> y2 across yc
    val bedY = h * 0.78f
    val y1Top = h * 0.62f
    val y2Top = h * 0.34f
    val ycTop = h * 0.50f
    val jStart = w * 0.36f
    val jEnd = w * 0.56f

    val waterPath = Path().apply {
      moveTo(w * 0.10f, y1Top)
      lineTo(jStart, y1Top)
      cubicTo(jStart + w * 0.08f, y1Top, jEnd - w * 0.06f, y2Top, jEnd, y2Top)
      lineTo(w * 0.90f, y2Top)
      lineTo(w * 0.90f, bedY)
      lineTo(w * 0.10f, bedY)
      close()
    }
    drawPath(waterPath, cyan.copy(alpha = 0.24f))
    drawLine(textCol, Offset(w * 0.10f, bedY), Offset(w * 0.90f, bedY), strokeWidth = 2.4.dp.toPx())
    drawLine(amber, Offset(w * 0.10f, ycTop), Offset(w * 0.90f, ycTop), strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)))
    cadLabel("y₁ (Fr₁ > 1)", w * 0.22f, y1Top - 8.dp.toPx(), cyan, sizeSp = 9.5f)
    cadLabel("y₂ (Fr₂ < 1)", w * 0.76f, y2Top - 8.dp.toPx(), emerald, sizeSp = 9.5f)
    cadLabel("y_c", w * 0.86f, ycTop - 5.dp.toPx(), amber, sizeSp = 9.5f)
  }
}

// ============================================================================
// GEOTECHNICAL / SOIL MECHANICS (HPGE • 02) — INTERACTIVE CAD LABS
// ============================================================================

@Composable
private fun GeotechThreePhaseBlockCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf(
    "Partially Saturated",
    "Fully Saturated (S=100%)",
    "Oven-Dry (S=0%)"
  )
  val bottomInfo = when (selectedMode) {
    1 -> "Saturated (S = 1, V_air = 0): γ_sat = [ (G_s + e) / (1 + e) ] · γ_w  •  w = e / G_s"
    2 -> "Oven-Dry (S = 0, V_water = 0): γ_d = (G_s · γ_w) / (1 + e) = γ / (1 + w)"
    else -> "3-Phase Master Identity: S · e = w · G_s  •  γ = [ (G_s + S·e) / (1 + e) ] · γ_w"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "geo_phase_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 196,
      accentColor = accentColor,
      topBadge = "3-PHASE SOIL BLOCK • VOLUME & WEIGHT",
      bottomFormula = bottomInfo,
      testTag = "geo_phase_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawGeotechThreePhaseBlock(selectedMode, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun GeotechPermeabilityFlowNetCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf("Falling Head Test", "Seepage Flow Net (Nf / Nd)")
  val bottomInfo = when (selectedMode) {
    1 -> "Flow Net Seepage: q = k H (N_f / N_d)  •  Exit Gradient Check: FS_heave = i_cr / i_exit ≥ 3"
    else -> "Falling Head: k = (a L / A t) · ln(h₁ / h₂) = 2.303 (a L / A t) · log₁₀(h₁ / h₂)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "geo_perm_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 192,
      accentColor = accentColor,
      topBadge = "PERMEABILITY & SEEPAGE FLOW NET LAB",
      bottomFormula = bottomInfo,
      testTag = "geo_perm_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawGeotechPermFlowNet(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun GeotechMohrCoulombCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf("General c-φ Soil", "Undrained Clay (φ = 0)", "Clean Sand (c = 0)")
  val bottomInfo = when (selectedMode) {
    1 -> "Undrained Clay (φ = 0): τ_f = c_u = s_u = q_u / 2  •  Failure Angle θ = 45°"
    2 -> "Clean Sand (c = 0): τ_f = σ' tan φ'  •  σ'₁ / σ'₃ = tan²(45° + φ'/2)"
    else -> "General c-φ: τ_f = c' + σ' tan φ'  •  σ'₁ = σ'₃ tan²(45° + φ'/2) + 2c' tan(45° + φ'/2)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "geo_mc_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 200,
      accentColor = accentColor,
      topBadge = "MOHR-COULOMB FAILURE ENVELOPE • TRIAXIAL",
      bottomFormula = bottomInfo,
      testTag = "geo_mc_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawGeotechMohrCoulomb(selectedMode, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

private fun DrawScope.drawGeotechThreePhaseBlock(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val bLeft = w * 0.34f
  val bW = w * 0.32f
  val topY = h * 0.18f
  val botY = h * 0.84f
  val totalH = botY - topY

  val airFrac = when (mode) {
    1 -> 0.0f
    2 -> 0.42f
    else -> 0.20f
  }
  val waterFrac = when (mode) {
    1 -> 0.42f
    2 -> 0.0f
    else -> 0.22f
  }
  val airH = totalH * airFrac
  val waterH = totalH * waterFrac
  val solidTop = topY + airH + waterH

  if (airH > 0f) {
    drawRect(textCol.copy(alpha = 0.06f), Offset(bLeft, topY), Size(bW, airH))
    cadLabel("AIR", w * 0.50f, topY + airH * 0.55f, textCol.copy(alpha = 0.7f), sizeSp = 10f)
    cadLabel("V_a", bLeft - 18.dp.toPx(), topY + airH * 0.55f, textCol.copy(alpha = 0.7f), sizeSp = 9.5f)
    cadLabel("W_a=0", bLeft + bW + 24.dp.toPx(), topY + airH * 0.55f, textCol.copy(alpha = 0.7f), sizeSp = 9.5f)
  }
  if (waterH > 0f) {
    drawRect(cyan.copy(alpha = 0.28f), Offset(bLeft, topY + airH), Size(bW, waterH))
    cadLabel("WATER", w * 0.50f, topY + airH + waterH * 0.55f, cyan, sizeSp = 10f)
    cadLabel("V_w", bLeft - 18.dp.toPx(), topY + airH + waterH * 0.55f, cyan, sizeSp = 9.5f)
    cadLabel("W_w", bLeft + bW + 18.dp.toPx(), topY + airH + waterH * 0.55f, cyan, sizeSp = 9.5f)
  }
  drawRect(amber.copy(alpha = 0.30f), Offset(bLeft, solidTop), Size(bW, botY - solidTop))
  cadLabel("SOLIDS", w * 0.50f, (solidTop + botY) * 0.52f, amber, sizeSp = 10.5f)
  cadLabel("V_s", bLeft - 18.dp.toPx(), (solidTop + botY) * 0.52f, amber, sizeSp = 9.5f)
  cadLabel("W_s", bLeft + bW + 18.dp.toPx(), (solidTop + botY) * 0.52f, amber, sizeSp = 9.5f)

  drawRect(textCol, Offset(bLeft, topY), Size(bW, totalH), style = Stroke(2.dp.toPx()))
  if (airH > 0f && waterH > 0f) {
    drawLine(textCol.copy(alpha = 0.6f), Offset(bLeft, topY + airH), Offset(bLeft + bW, topY + airH), strokeWidth = 1.4.dp.toPx())
  }
  drawLine(textCol.copy(alpha = 0.8f), Offset(bLeft, solidTop), Offset(bLeft + bW, solidTop), strokeWidth = 1.8.dp.toPx())
}

private fun DrawScope.drawGeotechCompactionCurve(
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val ox = w * 0.18f
  val oy = h * 0.82f
  val topY = h * 0.18f
  val rightX = w * 0.88f

  drawArrow(Offset(ox, oy + 6.dp.toPx()), Offset(ox, topY - 10.dp.toPx()), textCol.copy(alpha = 0.75f), strokePx = 1.8.dp.toPx(), headLenPx = 5.5.dp.toPx())
  drawArrow(Offset(ox - 6.dp.toPx(), oy), Offset(rightX + 8.dp.toPx(), oy), textCol.copy(alpha = 0.75f), strokePx = 1.8.dp.toPx(), headLenPx = 5.5.dp.toPx())
  cadLabel("γ_d", ox - 16.dp.toPx(), topY + 6.dp.toPx(), textCol, sizeSp = 10f)
  cadLabel("w (%)", rightX - 10.dp.toPx(), oy + 13.dp.toPx(), textCol, sizeSp = 9.5f)

  // Bell-shaped Proctor compaction curve
  val peakX = w * 0.48f
  val peakY = h * 0.34f
  val bell = Path().apply {
    moveTo(w * 0.24f, h * 0.70f)
    quadraticTo(peakX - w * 0.04f, peakY, peakX, peakY)
    quadraticTo(peakX + w * 0.08f, peakY, w * 0.74f, h * 0.72f)
  }
  drawPath(bell, amber, style = Stroke(2.8.dp.toPx(), cap = StrokeCap.Round))

  // Dashed Zero Air Void (ZAV) line
  val zav = Path().apply {
    moveTo(w * 0.46f, h * 0.22f)
    quadraticTo(w * 0.62f, h * 0.44f, w * 0.84f, h * 0.74f)
  }
  drawPath(zav, cyan, style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f))))
  cadLabel("ZAV (S=100%)", w * 0.74f, h * 0.40f, cyan, sizeSp = 9.5f)

  // OMC & γd,max markers
  drawLine(emerald, Offset(ox, peakY), Offset(peakX, peakY), strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
  drawLine(emerald, Offset(peakX, peakY), Offset(peakX, oy), strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
  drawCircle(emerald, radius = 4.5.dp.toPx(), center = Offset(peakX, peakY))
  cadLabel("γ_d,max", ox + 28.dp.toPx(), peakY - 6.dp.toPx(), emerald, sizeSp = 9.5f)
  cadLabel("OMC", peakX, oy + 13.dp.toPx(), emerald, sizeSp = 10f)
}

private fun DrawScope.drawGeotechPermFlowNet(
  isFlowNet: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  if (!isFlowNet) {
    // Falling Head Permeameter
    val colX = w * 0.48f
    val colW = w * 0.18f
    drawRect(amber.copy(alpha = 0.25f), Offset(colX - colW * 0.5f, h * 0.56f), Size(colW, h * 0.24f))
    drawRect(textCol, Offset(colX - colW * 0.5f, h * 0.56f), Size(colW, h * 0.24f), style = Stroke(2.dp.toPx()))
    // Standpipe
    val pipeW = 10.dp.toPx()
    drawRect(cyan.copy(alpha = 0.30f), Offset(colX - pipeW * 0.5f, h * 0.22f), Size(pipeW, h * 0.34f))
    drawRect(textCol, Offset(colX - pipeW * 0.5f, h * 0.16f), Size(pipeW, h * 0.40f), style = Stroke(1.6.dp.toPx()))
    drawLine(cyan, Offset(colX - 24.dp.toPx(), h * 0.22f), Offset(colX + 24.dp.toPx(), h * 0.22f), strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
    drawLine(cyan, Offset(colX - 24.dp.toPx(), h * 0.38f), Offset(colX + 24.dp.toPx(), h * 0.38f), strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
    cadLabel("h₁", colX - 32.dp.toPx(), h * 0.24f, cyan, sizeSp = 10f)
    cadLabel("h₂", colX - 32.dp.toPx(), h * 0.40f, cyan, sizeSp = 10f)
    cadLabel("L", colX + colW * 0.5f + 14.dp.toPx(), h * 0.68f, amber, sizeSp = 10f)
    cadLabel("Standpipe (a)", w * 0.72f, h * 0.28f, textCol, sizeSp = 9.5f)
  } else {
    // Sheet-Pile Cut-Off Wall & Flow Net Channels
    val pileX = w * 0.50f
    val groundY = h * 0.44f
    val pileBot = h * 0.70f
    drawRect(cyan.copy(alpha = 0.18f), Offset(w * 0.12f, h * 0.26f), Size(pileX - w * 0.12f, groundY - h * 0.26f))
    drawLine(textCol, Offset(w * 0.12f, groundY), Offset(w * 0.88f, groundY), strokeWidth = 2.dp.toPx())
    drawLine(amber, Offset(pileX, h * 0.22f), Offset(pileX, pileBot), strokeWidth = 4.dp.toPx())
    // Flow lines curving under sheet pile
    for (i in 1..3) {
      val rad = 18.dp.toPx() * i
      drawArc(cyan, startAngle = 180f, sweepAngle = -180f, useCenter = false, topLeft = Offset(pileX - rad, groundY - rad * 0.35f), size = Size(rad * 2, rad * 1.65f), style = Stroke(1.8.dp.toPx()))
    }
    cadLabel("H", w * 0.24f, h * 0.36f, cyan, sizeSp = 10.5f)
    cadLabel("N_f / N_d", w * 0.76f, h * 0.32f, emerald, sizeSp = 10f)
  }
}

private fun DrawScope.drawGeotechMohrCoulomb(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val ox = w * 0.16f
  val oy = h * 0.78f
  val topY = h * 0.18f
  val rightX = w * 0.88f

  drawArrow(Offset(ox, oy + 6.dp.toPx()), Offset(ox, topY - 10.dp.toPx()), textCol.copy(alpha = 0.75f), strokePx = 1.8.dp.toPx(), headLenPx = 5.5.dp.toPx())
  drawArrow(Offset(ox - 6.dp.toPx(), oy), Offset(rightX + 8.dp.toPx(), oy), textCol.copy(alpha = 0.75f), strokePx = 1.8.dp.toPx(), headLenPx = 5.5.dp.toPx())
  cadLabel("τ", ox - 14.dp.toPx(), topY + 4.dp.toPx(), textCol, sizeSp = 10.5f)
  cadLabel("σ'", rightX, oy + 14.dp.toPx(), textCol, sizeSp = 10f)

  val r = h * 0.22f
  val cx = w * 0.50f
  drawCircle(cyan.copy(alpha = 0.14f), radius = r, center = Offset(cx, oy))
  drawCircle(cyan, radius = r, center = Offset(cx, oy), style = Stroke(2.2.dp.toPx()))

  when (mode) {
    1 -> { // Undrained Clay (phi = 0, horizontal envelope)
      val envY = oy - r
      drawLine(amber, Offset(ox, envY), Offset(rightX, envY), strokeWidth = 2.4.dp.toPx())
      drawCircle(amber, radius = 4.5.dp.toPx(), center = Offset(cx, envY))
      cadLabel("c_u = q_u / 2", ox + 40.dp.toPx(), envY - 8.dp.toPx(), amber, sizeSp = 10f)
      cadLabel("φ = 0°", w * 0.76f, envY - 8.dp.toPx(), emerald, sizeSp = 10f)
    }
    2 -> { // Clean Sand (c = 0, line from origin)
      val tanPt = Offset(cx - r * 0.45f, oy - r * 0.89f)
      val slope = (oy - tanPt.y) / (tanPt.x - ox)
      val endY = oy - slope * (rightX - ox)
      drawLine(amber, Offset(ox, oy), Offset(rightX, endY.coerceAtLeast(topY)), strokeWidth = 2.4.dp.toPx())
      drawCircle(amber, radius = 4.5.dp.toPx(), center = tanPt)
      cadLabel("c = 0", ox + 18.dp.toPx(), oy - 12.dp.toPx(), emerald, sizeSp = 9.5f)
      cadLabel("φ'", cx + 26.dp.toPx(), oy - r - 12.dp.toPx(), amber, sizeSp = 10f)
    }
    else -> { // General c-phi soil
      val cY = oy - h * 0.14f
      val tanPt = Offset(cx - r * 0.35f, oy - r * 0.93f)
      val slope = (cY - tanPt.y) / (tanPt.x - ox)
      val endY = cY - slope * (rightX - ox)
      drawLine(amber, Offset(ox, cY), Offset(rightX, endY.coerceAtLeast(topY)), strokeWidth = 2.4.dp.toPx())
      drawCircle(amber, radius = 4.5.dp.toPx(), center = tanPt)
      drawLine(emerald, Offset(cx, oy), tanPt, strokeWidth = 1.6.dp.toPx())
      cadLabel("c'", ox - 12.dp.toPx(), cY + 4.dp.toPx(), amber, sizeSp = 10f)
      cadLabel("σ'₃", cx - r, oy + 14.dp.toPx(), textCol, sizeSp = 9.5f)
      cadLabel("σ'₁", cx + r, oy + 14.dp.toPx(), textCol, sizeSp = 9.5f)
      cadLabel("45°+φ/2", cx + 18.dp.toPx(), oy - 10.dp.toPx(), emerald, sizeSp = 9f)
    }
  }
}

// ============================================================================
// HYDROLOGY (HPGE • 03) — INTERACTIVE CAD LABS & FIGURES
// ============================================================================

@Composable
private fun HydrologyPrecipitationCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(1) }
  val labels = listOf("Arithmetic Mean", "Thiessen Polygons", "Isohyetal Contours")
  val bottomInfo = when (selectedMode) {
    0 -> "Arithmetic Mean: P̄ = (P₁ + P₂ + P₃ + P₄) / 4  (Equal weight w_i = 0.25 per gauge)"
    2 -> "Isohyetal Method: P̄ = Σ[ A_i · (P_i + P_(i+1)) / 2 ] / ΣA_i  (Most accurate for orographic terrain)"
    else -> "Thiessen Polygons: P̄ = Σ(P_i · A_i) / ΣA_i = w₁P₁ + w₂P₂ + w₃P₃ + w₄P₄  (Σw_i = 1.0)"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "hydro_precip_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 198,
      accentColor = accentColor,
      topBadge = "WATERSHED AREAL RAINFALL • 4-GAUGE NETWORK",
      bottomFormula = bottomInfo,
      testTag = "hydro_precip_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawHydrologyWatershedPrecip(selectedMode, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

@Composable
private fun HydrologyWellConeCard(accentColor: Color) {
  var selectedMode by remember { mutableIntStateOf(0) }
  val labels = listOf("Confined Aquifer (Thiem)", "Unconfined Aquifer (Dupuit)")
  val bottomInfo = when (selectedMode) {
    1 -> "Dupuit Unconfined: Q = [ π · K · (h₂² - h₁²) ] / ln(r₂ / r₁)  •  Free phreatic water table"
    else -> "Thiem Confined: Q = [ 2π · K · b · (h₂ - h₁) ] / ln(r₂ / r₁)  •  Piezometric head above aquiclude"
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    ModePillRow(
      labels = labels,
      selectedIndex = selectedMode,
      accentColor = accentColor,
      testTagPrefix = "hydro_well_mode",
      onSelect = { selectedMode = it }
    )
    PsadCadCanvasShell(
      heightDp = 200,
      accentColor = accentColor,
      topBadge = "RADIAL WELL HYDRAULICS • CONE OF DEPRESSION",
      bottomFormula = bottomInfo,
      testTag = "hydro_well_canvas"
    ) { cyan, amber, emerald, textCol ->
      drawHydrologyWellCone(selectedMode == 1, accentColor, cyan, amber, emerald, textCol)
    }
  }
}

private fun DrawScope.drawHydrologyWatershedPrecip(
  mode: Int,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val cx = w * 0.48f
  val cy = h * 0.54f

  // Irregular watershed perimeter polygon
  val pTop = Offset(cx - w * 0.04f, h * 0.16f)
  val pTR = Offset(cx + w * 0.24f, h * 0.24f)
  val pR = Offset(cx + w * 0.28f, h * 0.54f)
  val pBR = Offset(cx + w * 0.18f, h * 0.84f)
  val pBL = Offset(cx - w * 0.16f, h * 0.86f)
  val pL = Offset(cx - w * 0.27f, h * 0.50f)
  val pTL = Offset(cx - w * 0.20f, h * 0.22f)

  val basinPath = Path().apply {
    moveTo(pTop.x, pTop.y)
    lineTo(pTR.x, pTR.y)
    lineTo(pR.x, pR.y)
    lineTo(pBR.x, pBR.y)
    lineTo(pBL.x, pBL.y)
    lineTo(pL.x, pL.y)
    lineTo(pTL.x, pTL.y)
    close()
  }
  drawPath(basinPath, cyan.copy(alpha = 0.12f))

  // 4 Rain Gauge Stations inside watershed
  val g1 = Offset(cx - w * 0.12f, h * 0.36f)
  val g2 = Offset(cx + w * 0.13f, h * 0.35f)
  val g3 = Offset(cx - w * 0.10f, h * 0.68f)
  val g4 = Offset(cx + w * 0.14f, h * 0.66f)

  when (mode) {
    1 -> { // Thiessen Polygon perpendicular bisectors
      val midX = cx + w * 0.01f
      val midY = h * 0.52f
      drawLine(amber, Offset(midX, h * 0.16f), Offset(midX, h * 0.86f), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
      drawLine(amber, Offset(cx - w * 0.27f, midY), Offset(cx + w * 0.28f, midY), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
      cadLabel("w₁A₁", g1.x - 24.dp.toPx(), g1.y - 10.dp.toPx(), amber, sizeSp = 9f)
      cadLabel("w₂A₂", g2.x + 24.dp.toPx(), g2.y - 10.dp.toPx(), amber, sizeSp = 9f)
      cadLabel("w₃A₃", g3.x - 24.dp.toPx(), g3.y + 12.dp.toPx(), amber, sizeSp = 9f)
      cadLabel("w₄A₄", g4.x + 24.dp.toPx(), g4.y + 12.dp.toPx(), amber, sizeSp = 9f)
    }
    2 -> { // Isohyetal Contours
      val isoCenter = Offset(cx + w * 0.02f, h * 0.48f)
      drawOval(emerald, topLeft = Offset(isoCenter.x - w * 0.08f, isoCenter.y - h * 0.12f), size = Size(w * 0.16f, h * 0.24f), style = Stroke(1.8.dp.toPx()))
      drawOval(amber, topLeft = Offset(isoCenter.x - w * 0.16f, isoCenter.y - h * 0.22f), size = Size(w * 0.32f, h * 0.44f), style = Stroke(1.8.dp.toPx()))
      drawOval(cyan, topLeft = Offset(isoCenter.x - w * 0.23f, isoCenter.y - h * 0.31f), size = Size(w * 0.46f, h * 0.62f), style = Stroke(1.6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f))))
      cadLabel("140mm", isoCenter.x, isoCenter.y - h * 0.02f, emerald, sizeSp = 9f)
      cadLabel("120mm", isoCenter.x + w * 0.13f, isoCenter.y - h * 0.17f, amber, sizeSp = 8.5f)
      cadLabel("100mm", isoCenter.x + w * 0.20f, isoCenter.y + h * 0.27f, cyan, sizeSp = 8.5f)
    }
    else -> { // Arithmetic Mean network links
      drawLine(emerald.copy(alpha = 0.55f), g1, g2, strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
      drawLine(emerald.copy(alpha = 0.55f), g2, g4, strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
      drawLine(emerald.copy(alpha = 0.55f), g4, g3, strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
      drawLine(emerald.copy(alpha = 0.55f), g3, g1, strokeWidth = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
      cadLabel("Equal Weight 1/4", cx, h * 0.54f, emerald, sizeSp = 9.5f)
    }
  }

  drawPath(basinPath, textCol, style = Stroke(2.2.dp.toPx()))
  listOf(g1 to "P₁", g2 to "P₂", g3 to "P₃", g4 to "P₄").forEach { (pt, lbl) ->
    drawCircle(accent, radius = 5.dp.toPx(), center = pt)
    drawCircle(Color.White, radius = 2.dp.toPx(), center = pt)
    cadLabel(lbl, pt.x, pt.y + 13.dp.toPx(), textCol, sizeSp = 9.5f)
  }
}

private fun DrawScope.drawHydrologyHyetographPhi(
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val ox = w * 0.14f
  val oy = h * 0.80f
  val topY = h * 0.20f
  val rightX = w * 0.88f

  drawArrow(Offset(ox, oy + 6.dp.toPx()), Offset(ox, topY - 8.dp.toPx()), textCol.copy(alpha = 0.78f), strokePx = 1.8.dp.toPx(), headLenPx = 5.dp.toPx())
  drawArrow(Offset(ox - 6.dp.toPx(), oy), Offset(rightX + 8.dp.toPx(), oy), textCol.copy(alpha = 0.78f), strokePx = 1.8.dp.toPx(), headLenPx = 5.dp.toPx())
  cadLabel("i (mm/hr)", ox + 20.dp.toPx(), topY - 4.dp.toPx(), textCol, sizeSp = 9f)
  cadLabel("t (hr)", rightX - 6.dp.toPx(), oy + 14.dp.toPx(), textCol, sizeSp = 9f)

  val intensities = listOf(0.26f, 0.56f, 0.86f, 0.64f, 0.34f, 0.18f)
  val phiFrac = 0.38f
  val barW = (rightX - ox - 16.dp.toPx()) / intensities.size
  val maxBarH = oy - topY
  val phiY = oy - phiFrac * maxBarH

  intensities.forEachIndexed { idx, frac ->
    val bx = ox + 6.dp.toPx() + idx * barW
    val barTop = oy - frac * maxBarH
    val lossTop = maxOf(barTop, phiY)
    // Amber infiltration loss below Phi
    drawRect(amber.copy(alpha = 0.32f), Offset(bx + 2.dp.toPx(), lossTop), Size(barW - 4.dp.toPx(), oy - lossTop))
    // Cyan effective rainfall ERH above Phi
    if (barTop < phiY) {
      drawRect(cyan.copy(alpha = 0.42f), Offset(bx + 2.dp.toPx(), barTop), Size(barW - 4.dp.toPx(), phiY - barTop))
    }
    drawRect(textCol.copy(alpha = 0.65f), Offset(bx + 2.dp.toPx(), barTop), Size(barW - 4.dp.toPx(), oy - barTop), style = Stroke(1.3.dp.toPx()))
  }

  // Glowing horizontal Phi-Index cutoff line
  drawLine(emerald, Offset(ox, phiY), Offset(rightX, phiY), strokeWidth = 2.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 4f)))
  cadLabel("Φ-Index", rightX - 24.dp.toPx(), phiY - 7.dp.toPx(), emerald, sizeSp = 9.5f)
  cadLabel("ERH (i > Φ)", ox + (rightX - ox) * 0.45f, topY + 12.dp.toPx(), cyan, sizeSp = 9.5f)
  cadLabel("Losses", ox + (rightX - ox) * 0.45f, oy - 10.dp.toPx(), amber, sizeSp = 9f)
}

private fun DrawScope.drawHydrologyFloodHydrograph(
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val ox = w * 0.14f
  val oy = h * 0.80f
  val topY = h * 0.20f
  val rightX = w * 0.88f

  drawArrow(Offset(ox, oy + 6.dp.toPx()), Offset(ox, topY - 8.dp.toPx()), textCol.copy(alpha = 0.78f), strokePx = 1.8.dp.toPx(), headLenPx = 5.dp.toPx())
  drawArrow(Offset(ox - 6.dp.toPx(), oy), Offset(rightX + 8.dp.toPx(), oy), textCol.copy(alpha = 0.78f), strokePx = 1.8.dp.toPx(), headLenPx = 5.dp.toPx())
  cadLabel("Q (m³/s)", ox + 22.dp.toPx(), topY - 4.dp.toPx(), textCol, sizeSp = 9f)
  cadLabel("t (hr)", rightX - 6.dp.toPx(), oy + 14.dp.toPx(), textCol, sizeSp = 9f)

  val pStart = Offset(ox + w * 0.04f, oy - h * 0.12f)
  val pPeak = Offset(ox + w * 0.28f, topY + h * 0.06f)
  val pInflect = Offset(ox + w * 0.56f, oy - h * 0.18f)
  val baseEnd = Offset(rightX - w * 0.04f, oy - h * 0.14f)

  val drhFill = Path().apply {
    moveTo(pStart.x, pStart.y)
    cubicTo(pStart.x + w * 0.10f, pStart.y - h * 0.08f, pPeak.x - w * 0.06f, pPeak.y, pPeak.x, pPeak.y)
    cubicTo(pPeak.x + w * 0.08f, pPeak.y, pInflect.x - w * 0.06f, pInflect.y - h * 0.06f, pInflect.x, pInflect.y)
    lineTo(pStart.x, pStart.y)
    close()
  }
  drawPath(drhFill, cyan.copy(alpha = 0.24f))

  val hydroCurve = Path().apply {
    moveTo(ox, pStart.y)
    lineTo(pStart.x, pStart.y)
    cubicTo(pStart.x + w * 0.10f, pStart.y - h * 0.08f, pPeak.x - w * 0.06f, pPeak.y, pPeak.x, pPeak.y)
    cubicTo(pPeak.x + w * 0.08f, pPeak.y, pInflect.x - w * 0.06f, pInflect.y - h * 0.06f, pInflect.x, pInflect.y)
    lineTo(baseEnd.x, baseEnd.y)
  }
  drawPath(hydroCurve, cyan, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))

  // Baseflow separation dashed line
  drawLine(amber, pStart, pInflect, strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
  drawCircle(emerald, radius = 4.5.dp.toPx(), center = pPeak)
  cadLabel("Q_p", pPeak.x, pPeak.y - 9.dp.toPx(), emerald, sizeSp = 10f)
  cadLabel("Rising", pPeak.x - w * 0.13f, h * 0.48f, cyan, sizeSp = 9f)
  cadLabel("Recession", pPeak.x + w * 0.16f, h * 0.46f, cyan, sizeSp = 9f)
  cadLabel("DRH", pPeak.x + w * 0.02f, oy - h * 0.22f, textCol, sizeSp = 9.5f)
  cadLabel("Baseflow", (pStart.x + pInflect.x) * 0.5f, oy - h * 0.05f, amber, sizeSp = 9f)
}

private fun DrawScope.drawHydrologyWellCone(
  isUnconfined: Boolean,
  accent: Color,
  cyan: Color,
  amber: Color,
  emerald: Color,
  textCol: Color
) {
  val w = size.width
  val h = size.height
  val wellX = w * 0.26f
  val groundY = h * 0.22f
  val baseY = h * 0.84f
  val aquicludeTop = h * 0.54f
  val piezy = h * 0.30f

  // Ground line & impermeable base
  drawLine(textCol, Offset(w * 0.08f, groundY), Offset(w * 0.92f, groundY), strokeWidth = 2.dp.toPx())
  drawRect(amber.copy(alpha = 0.25f), Offset(w * 0.08f, baseY), Size(w * 0.84f, h * 0.08f))
  drawLine(textCol, Offset(w * 0.08f, baseY), Offset(w * 0.92f, baseY), strokeWidth = 2.dp.toPx())

  if (!isUnconfined) {
    // Confining aquiclude layer above aquifer thickness b
    drawRect(amber.copy(alpha = 0.22f), Offset(w * 0.08f, h * 0.44f), Size(w * 0.84f, aquicludeTop - h * 0.44f))
    drawLine(amber, Offset(w * 0.08f, aquicludeTop), Offset(w * 0.92f, aquicludeTop), strokeWidth = 1.6.dp.toPx())
    drawRect(cyan.copy(alpha = 0.18f), Offset(w * 0.08f, aquicludeTop), Size(w * 0.84f, baseY - aquicludeTop))
    cadLabel("b", w * 0.13f, (aquicludeTop + baseY) * 0.5f, emerald, sizeSp = 10f)
  } else {
    drawRect(cyan.copy(alpha = 0.14f), Offset(w * 0.08f, piezy + 12.dp.toPx()), Size(w * 0.84f, baseY - (piezy + 12.dp.toPx())))
  }

  // Cone of depression curve from pumping well outward right
  val coneTopWell = if (isUnconfined) h * 0.62f else h * 0.46f
  val coneFarY = piezy
  val conePath = Path().apply {
    moveTo(wellX, coneTopWell)
    cubicTo(wellX + w * 0.10f, coneTopWell - h * 0.10f, wellX + w * 0.24f, coneFarY + h * 0.02f, w * 0.88f, coneFarY)
  }
  drawPath(conePath, cyan, style = Stroke(2.4.dp.toPx(), cap = StrokeCap.Round))

  // Pumping well casing & discharge Q
  drawRect(textCol.copy(alpha = 0.85f), Offset(wellX - 5.dp.toPx(), groundY - 8.dp.toPx()), Size(10.dp.toPx(), baseY - groundY + 8.dp.toPx()), style = Stroke(1.8.dp.toPx()))
  drawArrow(Offset(wellX, groundY - 4.dp.toPx()), Offset(wellX, groundY - 22.dp.toPx()), emerald, strokePx = 2.dp.toPx(), headLenPx = 5.dp.toPx())
  cadLabel("Q", wellX + 12.dp.toPx(), groundY - 12.dp.toPx(), emerald, sizeSp = 10f)

  // Observation wells at r1 and r2
  val obs1X = w * 0.48f
  val obs2X = w * 0.72f
  val h1Y = coneTopWell - h * 0.09f
  val h2Y = coneFarY + h * 0.02f
  drawLine(textCol.copy(alpha = 0.70f), Offset(obs1X, groundY), Offset(obs1X, baseY), strokeWidth = 1.6.dp.toPx())
  drawLine(textCol.copy(alpha = 0.70f), Offset(obs2X, groundY), Offset(obs2X, baseY), strokeWidth = 1.6.dp.toPx())
  drawLine(emerald, Offset(obs1X, h1Y), Offset(obs1X, baseY), strokeWidth = 2.6.dp.toPx())
  drawLine(emerald, Offset(obs2X, h2Y), Offset(obs2X, baseY), strokeWidth = 2.6.dp.toPx())

  cadLabel("r₁", (wellX + obs1X) * 0.5f, groundY + 12.dp.toPx(), textCol, sizeSp = 9f)
  cadLabel("r₂", (wellX + obs2X) * 0.55f, groundY - 4.dp.toPx(), textCol, sizeSp = 9f)
  cadLabel("h₁", obs1X + 12.dp.toPx(), (h1Y + baseY) * 0.5f, emerald, sizeSp = 9.5f)
  cadLabel("h₂", obs2X + 12.dp.toPx(), (h2Y + baseY) * 0.5f, emerald, sizeSp = 9.5f)
}





package com.example

import android.graphics.Paint
import android.graphics.Typeface
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.JetBrainsMonoFamily
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// ============================================================================
// MSTE ANALYTIC GEOMETRY HANDBOOK SECTIONS (10 CELE MSTE SECTIONS)
// ============================================================================

internal fun buildAnalyticGeometryHandbookSections(): List<AlgebraHandbookSection> = listOf(
  // [01] Cartesian Points, Distance & Division (Pure Formula List)
  AlgebraHandbookSection(
    sectionTitle = "Cartesian Points, Distance & Division",
    ribbonLabel = "Points",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Distance Between Two Points",
        formulaOrStatement = "d = √((x₂ - x₁)² + (y₂ - y₁)²)"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Midpoint of a Segment",
        formulaOrStatement = "x_m = (x₁ + x₂) / 2  |  y_m = (y₁ + y₂) / 2"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Division of Line Segment (Ratio r)",
        formulaOrStatement = "r = P₁P / P₁P₂  |  x = x₁ + r(x₂ - x₁)  |  y = y₁ + r(y₂ - y₁)"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Area of Polygon by Coordinates (Shoelace)",
        formulaOrStatement = "A = ½ | ∑(x_i y_(i+1) - x_(i+1) y_i) |"
      )
    )
  ),

  // [02] Straight Lines & Angle Between Lines (Interactive Blueprint + Formula List)
  AlgebraHandbookSection(
    sectionTitle = "Straight Lines & Angle Between Lines",
    ribbonLabel = "Lines",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Slope & Angle of Inclination",
        formulaOrStatement = "m = (y₂ - y₁) / (x₂ - x₁) = tan θ"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "General, Slope-Intercept & Point-Slope Forms",
        formulaOrStatement = "Ax + By + C = 0  |  y = mx + b  |  y - y₁ = m(x - x₁)"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Two-Point, Intercept & Normal Forms",
        formulaOrStatement = "y - y₁ = [(y₂ - y₁)/(x₂ - x₁)](x - x₁)  |  x/a + y/b = 1  |  x cos ω + y sin ω = p"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Parallel, Perpendicular & Acute Angle Between Lines",
        formulaOrStatement = "Parallel: m₁ = m₂  |  Perp: m₁ · m₂ = -1  |  tan θ = |(m₂ - m₁) / (1 + m₁m₂)|"
      )
    )
  ),

  // [03] Perpendicular Distances (Interactive Blueprint + Formula List)
  AlgebraHandbookSection(
    sectionTitle = "Perpendicular Distances (Point & Parallel Lines)",
    ribbonLabel = "Distances",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Point (x₁, y₁) to Line Ax + By + C = 0",
        formulaOrStatement = "d = |Ax₁ + By₁ + C| / √(A² + B²)"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Distance Between Parallel Lines",
        formulaOrStatement = "d = |C₂ - C₁| / √(A² + B²)"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Directed Distance (Origin Side Convention)",
        formulaOrStatement = "d = (Ax₁ + By₁ + C) / ±√(A² + B²)"
      )
    )
  ),

  // [04] Conic Identification & Eccentricity Rules (Pure Formula List)
  AlgebraHandbookSection(
    sectionTitle = "Conic Identification & Eccentricity Rules",
    ribbonLabel = "Conics",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "General Second-Degree Conic Equation",
        formulaOrStatement = "Ax² + Bxy + Cy² + Dx + Ey + F = 0"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Discriminant Test (B² - 4AC)",
        formulaOrStatement = "B² - 4AC < 0 (Ellipse)  |  B² - 4AC = 0 (Parabola)  |  B² - 4AC > 0 (Hyperbola)"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Eccentricity Classification (e = d_F / d_D)",
        formulaOrStatement = "e = 0 (Circle)  |  0 < e < 1 (Ellipse)  |  e = 1 (Parabola)  |  e > 1 (Hyperbola)"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Rotation of Axes (Eliminating xy-Term)",
        formulaOrStatement = "cot 2θ = (A - C) / B"
      )
    )
  ),

  // [05] Circle (Standard, General & Tangent) (Interactive Blueprint + Formula List)
  AlgebraHandbookSection(
    sectionTitle = "Circle (Standard, General & Tangent)",
    ribbonLabel = "Circle",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Standard Circle Equations",
        formulaOrStatement = "Origin: x² + y² = r²  |  Center (h, k): (x - h)² + (y - k)² = r²"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "General Form & Center-Radius Extraction",
        formulaOrStatement = "x² + y² + Dx + Ey + F = 0  |  h = -D/2,  k = -E/2,  r = ½√(D² + E² - 4F)"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Radical Axis of Two Circles",
        formulaOrStatement = "(D₁ - D₂)x + (E₁ - E₂)y + (F₁ - F₂) = 0"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Tangent Line at Point (x₁, y₁)",
        formulaOrStatement = "(x₁ - h)(x - h) + (y₁ - k)(y - k) = r²"
      )
    )
  ),

  // [06] Parabola (Vertical & Horizontal Axes) (Interactive Blueprint + Formula List)
  AlgebraHandbookSection(
    sectionTitle = "Parabola (Vertical & Horizontal Axes)",
    ribbonLabel = "Parabola",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Vertical Axis (Opens Up / Down)",
        formulaOrStatement = "(x - h)² = ±4a(y - k)  |  Focus: F(h, k ± a)  |  Directrix: y = k ∓ a"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Horizontal Axis (Opens Right / Left)",
        formulaOrStatement = "(y - k)² = ±4a(x - h)  |  Focus: F(h ± a, k)  |  Directrix: x = h ∓ a"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Vertex, Latus Rectum & Eccentricity",
        formulaOrStatement = "Vertex: V(h, k)  |  LR = 4a  |  e = 1  |  Focal Distance = a"
      )
    )
  ),

  // [07] Ellipse (Major/Minor Axes & Foci) (Interactive Blueprint + Formula List)
  AlgebraHandbookSection(
    sectionTitle = "Ellipse (Major/Minor Axes & Foci)",
    ribbonLabel = "Ellipse",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Standard Forms (a > b)",
        formulaOrStatement = "Horiz: (x - h)²/a² + (y - k)²/b² = 1  |  Vert: (x - h)²/b² + (y - k)²/a² = 1"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Focal Relation & Eccentricity",
        formulaOrStatement = "c² = a² - b²  |  e = c / a  (0 < e < 1)"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Latus Rectum, Directrix & Area",
        formulaOrStatement = "LR = 2b² / a  |  d = a / e = a² / c  |  Area = πab"
      )
    )
  ),

  // [08] Hyperbola & Asymptotes (Interactive Blueprint + Formula List)
  AlgebraHandbookSection(
    sectionTitle = "Hyperbola & Asymptotes",
    ribbonLabel = "Hyperbola",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Standard Forms (Transverse Axis 2a)",
        formulaOrStatement = "Horiz: (x - h)²/a² - (y - k)²/b² = 1  |  Vert: (y - k)²/a² - (x - h)²/b² = 1"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Focal Relation & Eccentricity",
        formulaOrStatement = "c² = a² + b²  |  e = c / a  (e > 1)"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Latus Rectum & Directrix Distance",
        formulaOrStatement = "LR = 2b² / a  |  d = a / e = a² / c"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Equations of Asymptotes",
        formulaOrStatement = "Horiz: y - k = ±(b / a)(x - h)  |  Vert: y - k = ±(a / b)(x - h)"
      )
    )
  ),

  // [09] Polar Coordinates & Curve Conversions (Pure Formula List)
  AlgebraHandbookSection(
    sectionTitle = "Polar Coordinates & Curve Conversions",
    ribbonLabel = "Polar",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Polar to Rectangular Conversion",
        formulaOrStatement = "x = r cos θ  |  y = r sin θ"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Rectangular to Polar Conversion",
        formulaOrStatement = "r = √(x² + y²)  |  θ = arctan(y / x)"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Distance Between Polar Points",
        formulaOrStatement = "d = √(r₁² + r₂² - 2r₁r₂ cos(θ₂ - θ₁))"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Polar Equation of a Conic (Focus at Pole)",
        formulaOrStatement = "r = (e · p) / (1 ± e cos θ)  |  r = (e · p) / (1 ± e sin θ)"
      )
    )
  ),

  // [10] 3D Space Analytic Geometry (CE Surveying & Vectors) (Pure Formula List)
  AlgebraHandbookSection(
    sectionTitle = "3D Space Analytic Geometry (CE Surveying)",
    ribbonLabel = "3D Space",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Distance Between Two Points in 3D Space",
        formulaOrStatement = "d = √((x₂ - x₁)² + (y₂ - y₁)² + (z₂ - z₁)²)"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Direction Cosines & Unit Identity",
        formulaOrStatement = "cos α = Δx/d,  cos β = Δy/d,  cos γ = Δz/d  |  cos²α + cos²β + cos²γ = 1"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "General Equation of a Plane",
        formulaOrStatement = "Ax + By + Cz + D = 0"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Point-to-Plane Perpendicular Distance",
        formulaOrStatement = "d = |Ax₁ + By₁ + Cz₁ + D| / √(A² + B² + C²)"
      )
    )
  )
)

// ============================================================================
// MAIN ANALYTIC GEOMETRY SHEET COMPOSABLE
// ============================================================================

@Composable
internal fun MsteAnalyticGeometrySheet(
  metrics: ResponsiveMetrics,
  lazyListState: LazyListState,
  highlightedSectionIndex: Int?,
  searchQuery: String,
  customFormulas: List<ReferenceFormulaCardData>,
  starredIds: Set<String>,
  onToggleStar: (String) -> Unit,
  onBackToShelf: () -> Unit,
  onOpenToc: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val localView = LocalView.current
  val palette = currentGlassPalette()
  val coroutineScope = rememberCoroutineScope()
  val allSections = remember { buildAnalyticGeometryHandbookSections() }
  var selectedSectionForDetailModal by remember { mutableStateOf<Pair<Int, AlgebraHandbookSection>?>(null) }

  // Preserve mode selection per section index across scrolls
  val sectionModes = remember {
    mutableStateMapOf(
      1 to 0, // [02] Lines: 0=Intersecting, 1=Perpendicular, 2=Normal Form
      2 to 0, // [03] Distances: 0=Point to Line, 1=Parallel Lines
      4 to 0, // [05] Circle: 0=Center (h,k), 1=Tangent Line, 2=Radical Axis
      5 to 0, // [06] Parabola: 0=Opens Up, 1=Opens Down, 2=Opens Right, 3=Opens Left
      6 to 0, // [07] Ellipse: 0=Horizontal Axis, 1=Vertical Axis
      7 to 0  // [08] Hyperbola: 0=Horizontal Axis, 1=Vertical Axis
    )
  }
  val sectionHotspots = remember { mutableStateMapOf<Int, String?>() }

  val filteredSections = remember(allSections, searchQuery) {
    if (searchQuery.isEmpty()) {
      allSections.mapIndexed { idx, sec -> idx to sec }
    } else {
      allSections.mapIndexedNotNull { idx, section ->
        if (section.sectionTitle.contains(searchQuery, ignoreCase = true) ||
          section.ribbonLabel.contains(searchQuery, ignoreCase = true)
        ) {
          idx to section
        } else {
          val matchingItems = section.items.filter { item ->
            item.title.contains(searchQuery, ignoreCase = true) ||
              item.formulaOrStatement.contains(searchQuery, ignoreCase = true) ||
              item.subItems.any { it.contains(searchQuery, ignoreCase = true) }
          }
          if (matchingItems.isNotEmpty()) {
            idx to section.copy(items = matchingItems)
          } else {
            null
          }
        }
      }
    }
  }

  val activeRibbonSectionIndex by remember(filteredSections, highlightedSectionIndex) {
    derivedStateOf {
      if (highlightedSectionIndex != null) {
        highlightedSectionIndex
      } else {
        val firstVisible = lazyListState.firstVisibleItemIndex
        if (firstVisible <= 0) {
          filteredSections.firstOrNull()?.first ?: 0
        } else {
          filteredSections.getOrNull(
            (firstVisible - 1).coerceIn(0, (filteredSections.size - 1).coerceAtLeast(0))
          )?.first ?: 0
        }
      }
    }
  }

  val sectionAccents = remember {
    listOf(
      Color(0xFF8B5CF6), // [01] Points
      Color(0xFF0EA5E9), // [02] Lines
      Color(0xFF06B6D4), // [03] Distances
      Color(0xFFA855F7), // [04] Conics
      Color(0xFF3B82F6), // [05] Circle
      Color(0xFFEC4899), // [06] Parabola
      Color(0xFF10B981), // [07] Ellipse
      Color(0xFFF59E0B), // [08] Hyperbola
      Color(0xFF14B8A6), // [09] Polar
      Color(0xFF6366F1)  // [10] 3D Space
    )
  }

  val orientation = rememberScreenOrientationState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .widthIn(max = orientation.wideMaxContentWidth)
      .testTag("analytic_geometry_sheet"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. STICKY QUICK-JUMP SECTION RIBBON
    if (filteredSections.isNotEmpty()) {
      StickyFormulaSectionRibbon(
        sections = filteredSections,
        activeSectionIndex = activeRibbonSectionIndex,
        accentColor = Color(0xFF8B5CF6),
        onSelectSection = { listIdx ->
          playCardHapticSound(context, localView)
          coroutineScope.launch {
            lazyListState.animateScrollToItem(index = listIdx + 1)
          }
        },
        testTagPrefix = "analytic_ribbon"
      )
    }

    // 2. SCROLLABLE BLUEPRINT LABS & NUMBERED LIQUID GLASS SECTIONS
    LazyColumn(
      state = lazyListState,
      contentPadding = PaddingValues(bottom = 120.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      // ITEM 0: Breadcrumb Bar + Spatial Glass Header Banner ("Analytic Geometry" / "MSTE • Conics, Lines & Space" / "Contents")
      item(key = "analytic_blueprint_header_item_0") {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              onClick = onBackToShelf,
              shape = RoundedCornerShape(10.dp),
              color = palette.cardFill,
              border = palette.glassBorder,
              modifier = Modifier.testTag("analytic_back_to_notebook_shelf_chip")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                  contentDescription = "Back to MSTE Library",
                  tint = Color(0xFF8B5CF6),
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "MSTE Library",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                  ),
                  color = Color(0xFF8B5CF6)
                )
              }
            }

            Surface(
              onClick = onOpenToc,
              shape = RoundedCornerShape(10.dp),
              color = palette.cardFill,
              border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.42f)),
              modifier = Modifier.testTag("analytic_toc_pill_button")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ViewList,
                  contentDescription = "Contents",
                  tint = Color(0xFF8B5CF6),
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "Contents • 10 Sections",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                  ),
                  color = Color(0xFF8B5CF6)
                )
              }
            }
          }

          // Hero Summary Glass Banner
          LiquidGlassCard(
            cornerRadius = 20.dp,
            elevation = 6.dp,
            borderStroke = BorderStroke(
              width = 1.1.dp,
              brush = Brush.linearGradient(
                listOf(
                  Color(0xFF8B5CF6).copy(alpha = 0.55f),
                  Color(0xFFEC4899).copy(alpha = 0.35f)
                )
              )
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  brush = Brush.horizontalGradient(
                    listOf(
                      Color(0xFF8B5CF6).copy(alpha = if (palette.isDark) 0.18f else 0.10f),
                      Color(0xFFEC4899).copy(alpha = if (palette.isDark) 0.12f else 0.06f)
                    )
                  )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(
                modifier = Modifier
                  .weight(1f)
                  .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Text(
                  text = "Analytic Geometry",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                  ),
                  color = palette.textPrimary
                )
                Text(
                  text = "MSTE • Conics, Lines & Space",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                  ),
                  color = if (palette.isDark) Color(0xFFC4B5FD) else Color(0xFF6D28D9)
                )
              }

              Surface(
                onClick = onOpenToc,
                shape = CircleShape,
                color = Color(0xFF8B5CF6).copy(alpha = if (palette.isDark) 0.24f else 0.14f),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.45f)),
                modifier = Modifier.testTag("analytic_hero_contents_button")
              ) {
                Text(
                  text = "MSTE / CE-04",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp
                  ),
                  color = if (palette.isDark) Color(0xFFE9D5FF) else Color(0xFF5B21B6),
                  modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                )
              }
            }
          }

          if (filteredSections.isEmpty()) {
            LiquidGlassCard(
              cornerRadius = 18.dp,
              elevation = 4.dp,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "No Analytic Geometry formulas match \"$searchQuery\".",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = palette.textSecondary,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
              )
            }
          }
        }
      }

      // ITEMS 1..10: Numbered Liquid Glass Section Cards with Optional Interactive Blueprint Lab + MathFormulaCapsule items
      items(
        count = filteredSections.size,
        key = { idx -> "analytic_section_card_${filteredSections[idx].first + 1}" }
      ) { listIdx ->
        val (originalSectionIndex, section) = filteredSections[listIdx]
        val sectionAccent = sectionAccents[originalSectionIndex % sectionAccents.size]
        val isHighlighted = highlightedSectionIndex == originalSectionIndex
        val badgeText = String.format(Locale.US, "%02d", originalSectionIndex + 1)
        val hasInteractiveBlueprint = originalSectionIndex in setOf(1, 2, 4, 5, 6, 7)

        val borderAlpha by animateFloatAsState(
          targetValue = if (isHighlighted) 0.90f else 0.42f,
          animationSpec = tween(durationMillis = 420),
          label = "analytic_sec_border_alpha"
        )

        LiquidGlassCard(
          cornerRadius = 22.dp,
          elevation = 6.dp,
          borderStroke = BorderStroke(
            width = if (isHighlighted) 1.6.dp else 1.1.dp,
            brush = Brush.linearGradient(
              listOf(
                sectionAccent.copy(alpha = borderAlpha),
                Color.White.copy(alpha = if (palette.isDark) 0.16f else 0.65f)
              )
            )
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("analytic_section_${originalSectionIndex + 1}")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Section Header Row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedSectionForDetailModal = originalSectionIndex to section },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .padding(end = 8.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = sectionAccent.copy(alpha = if (palette.isDark) 0.22f else 0.14f),
                  border = BorderStroke(1.dp, sectionAccent.copy(alpha = 0.55f))
                ) {
                  Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontFamily = JetBrainsMonoFamily,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.ExtraBold
                    ),
                    color = sectionAccent,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                  )
                }

                Text(
                  text = section.sectionTitle,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.ExtraBold
                  ),
                  color = palette.textPrimary,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }

              Surface(
                shape = CircleShape,
                color = sectionAccent.copy(alpha = if (palette.isDark) 0.16f else 0.10f),
                border = BorderStroke(0.8.dp, sectionAccent.copy(alpha = 0.38f))
              ) {
                Text(
                  text = if (hasInteractiveBlueprint) "CAD LAB • ${section.items.size}" else "${section.items.size} FORMULAS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                  ),
                  color = sectionAccent,
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
              }
            }

            HorizontalDivider(
              thickness = 0.8.dp,
              color = sectionAccent.copy(alpha = 0.22f)
            )

            // Interactive Conic & Geometric Blueprint Lab (Only for figure-based sections)
            if (hasInteractiveBlueprint) {
              val activeMode = sectionModes[originalSectionIndex] ?: 0
              val activeHotspot = sectionHotspots[originalSectionIndex]
              AnalyticBlueprintCard(
                sectionIndex = originalSectionIndex,
                accentColor = sectionAccent,
                selectedMode = activeMode,
                selectedHotspotId = activeHotspot,
                onSelectMode = { newMode ->
                  playCardHapticSound(context, localView)
                  sectionModes[originalSectionIndex] = newMode
                  sectionHotspots[originalSectionIndex] = null
                },
                onSelectHotspot = { hotspotId ->
                  playCardHapticSound(context, localView)
                  sectionHotspots[originalSectionIndex] =
                    if (sectionHotspots[originalSectionIndex] == hotspotId) null else hotspotId
                }
              )
            }

            // Formula List Capsules
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              section.items.forEachIndexed { ruleIdx, rule ->
                val secondaryEq = if (rule.subItems.isNotEmpty()) {
                  rule.subItems.joinToString("  |  ")
                } else {
                  null
                }
                MathFormulaCapsule(
                  label = rule.title,
                  equation = rule.formulaOrStatement,
                  secondaryEquation = secondaryEq,
                  accentColor = sectionAccent,
                  testTag = "analytic_formula_capsule_${originalSectionIndex + 1}_${ruleIdx + 1}"
                )
              }
            }
          }
        }
      }

      // Custom Scholar Formulas tagged Analytic Geometry
      if (customFormulas.isNotEmpty()) {
        item(key = "analytic_custom_formulas_footer") {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            customFormulas.forEach { formula ->
              InteractiveReferenceFormulaCard(
                formula = formula,
                isStarred = starredIds.contains(formula.id),
                onToggleStar = { onToggleStar(formula.id) },
                metrics = metrics
              )
            }
          }
        }
      }
    }
  }

  selectedSectionForDetailModal?.let { (secIndex, secData) ->
    AnalyticSectionDetailGlassModal(
      sectionNumber = secIndex + 1,
      section = secData,
      accentColor = sectionAccents[secIndex % sectionAccents.size],
      onDismiss = { selectedSectionForDetailModal = null }
    )
  }
}

// ============================================================================
// INTERACTIVE CONIC & GEOMETRIC BLUEPRINT LAB
// ============================================================================

private data class AnalyticHotspotSpec(
  val id: String,
  val label: String,
  val detail: String,
  val anchor: (w: Float, h: Float) -> Offset
)

private data class AnalyticBlueprintConfig(
  val modeLabels: List<String>,
  val liveEquation: String,
  val liveSummary: String,
  val hotspots: List<AnalyticHotspotSpec>
)

private fun buildBlueprintConfig(sectionIndex: Int, mode: Int): AnalyticBlueprintConfig {
  return when (sectionIndex) {
    // [02] Straight Lines & Angle Between Lines
    1 -> when (mode) {
      1 -> AnalyticBlueprintConfig(
        modeLabels = listOf("Intersecting", "Perpendicular", "Normal Form"),
        liveEquation = "m₁ · m₂ = -1   |   θ = 90°",
        liveSummary = "Orthogonal lines intersect at 90°; slopes are negative reciprocals (m₂ = -1/m₁).",
        hotspots = listOf(
          AnalyticHotspotSpec("l1", "Line L₁ (m₁)", "Slope m₁ = (y₂ - y₁)/(x₂ - x₁) = tan α₁") { w, h ->
            Offset(w * 0.72f, h * 0.30f)
          },
          AnalyticHotspotSpec("l2", "Line L₂ (m₂)", "Perpendicular slope condition: m₁ · m₂ = -1") { w, h ->
            Offset(w * 0.68f, h * 0.73f)
          },
          AnalyticHotspotSpec("theta", "Angle 90°", "1 + m₁m₂ = 0 ⇒ tan θ → ∞ (θ = 90°)") { w, h ->
            Offset(w * 0.50f, h * 0.50f)
          }
        )
      )
      2 -> AnalyticBlueprintConfig(
        modeLabels = listOf("Intersecting", "Perpendicular", "Normal Form"),
        liveEquation = "x cos ω + y sin ω = p",
        liveSummary = "p = perpendicular distance from Origin O(0,0) to line; ω = angle of normal vector.",
        hotspots = listOf(
          AnalyticHotspotSpec("normal_p", "Normal p", "Perpendicular length p = |C| / √(A² + B²) from Origin") { w, h ->
            Offset(w * 0.57f, h * 0.43f)
          },
          AnalyticHotspotSpec("omega", "Angle ω", "Counter-clockwise angle ω from +x axis to normal p") { w, h ->
            Offset(w * 0.52f, h * 0.55f)
          },
          AnalyticHotspotSpec("intercepts", "Intercepts", "x-int = a = p/cos ω  |  y-int = b = p/sin ω") { w, h ->
            Offset(w * 0.76f, h * 0.56f)
          }
        )
      )
      else -> AnalyticBlueprintConfig(
        modeLabels = listOf("Intersecting", "Perpendicular", "Normal Form"),
        liveEquation = "tan θ = |(m₂ - m₁) / (1 + m₁m₂)|",
        liveSummary = "Slope-Intercept: y = mx + b  •  Point-Slope: y - y₁ = m(x - x₁)",
        hotspots = listOf(
          AnalyticHotspotSpec("theta", "Acute Angle θ", "tan θ = |(m₂ - m₁) / (1 + m₁m₂)| between L₁ & L₂") { w, h ->
            Offset(w * 0.54f, h * 0.52f)
          },
          AnalyticHotspotSpec("l1", "Slope m₁", "m₁ = tan α₁ = rise / run along Line L₁") { w, h ->
            Offset(w * 0.74f, h * 0.28f)
          },
          AnalyticHotspotSpec("yint", "y-Intercept b", "Point (0, b) where line crosses the y-axis") { w, h ->
            Offset(w * 0.44f, h * 0.62f)
          }
        )
      )
    }

    // [03] Perpendicular Distances
    2 -> when (mode) {
      1 -> AnalyticBlueprintConfig(
        modeLabels = listOf("Point to Line", "Parallel Lines"),
        liveEquation = "d = |C₂ - C₁| / √(A² + B²)",
        liveSummary = "Ensure both parallel equations share identical A and B coefficients before subtracting C.",
        hotspots = listOf(
          AnalyticHotspotSpec("dist", "Distance d", "Shortest gap d = |C₂ - C₁| / √(A² + B²) ⊥ both lines") { w, h ->
            Offset(w * 0.50f, h * 0.50f)
          },
          AnalyticHotspotSpec("l1", "Line L₁", "Ax + By + C₁ = 0  (normalize A, B to match L₂)") { w, h ->
            Offset(w * 0.31f, h * 0.45f)
          },
          AnalyticHotspotSpec("l2", "Line L₂", "Ax + By + C₂ = 0  (parallel slope m = -A/B)") { w, h ->
            Offset(w * 0.69f, h * 0.55f)
          }
        )
      )
      else -> AnalyticBlueprintConfig(
        modeLabels = listOf("Point to Line", "Parallel Lines"),
        liveEquation = "d = |Ax₁ + By₁ + C| / √(A² + B²)",
        liveSummary = "Perpendicular distance from external point P(x₁, y₁) to line Ax + By + C = 0.",
        hotspots = listOf(
          AnalyticHotspotSpec("pt", "Point P(x₁,y₁)", "Target coordinate substituted into |Ax₁ + By₁ + C|") { w, h ->
            Offset(w * 0.34f, h * 0.31f)
          },
          AnalyticHotspotSpec("dist", "Perp Drop d", "Orthogonal segment of length d = |Ax₁ + By₁ + C| / √(A² + B²)") { w, h ->
            Offset(w * 0.48f, h * 0.45f)
          },
          AnalyticHotspotSpec("foot", "Line Foot", "Closest point on line Ax + By + C = 0 (angle = 90°)") { w, h ->
            Offset(w * 0.61f, h * 0.58f)
          }
        )
      )
    }

    // [05] Circle
    4 -> when (mode) {
      1 -> AnalyticBlueprintConfig(
        modeLabels = listOf("Center (h,k)", "Tangent Line", "Radical Axis"),
        liveEquation = "(x₁ - h)(x - h) + (y₁ - k)(y - k) = r²",
        liveSummary = "Tangent line at P(x₁, y₁) is always perpendicular to the radius vector CP.",
        hotspots = listOf(
          AnalyticHotspotSpec("center", "Center C(h,k)", "h = -D/2,  k = -E/2  from general form") { w, h ->
            Offset(w * 0.47f, h * 0.53f)
          },
          AnalyticHotspotSpec("tangent_pt", "Point P(x₁,y₁)", "Point of tangency where radius CP ⊥ Tangent Line") { w, h ->
            Offset(w * 0.47f + h * 0.22f, h * 0.53f - h * 0.22f)
          },
          AnalyticHotspotSpec("radius", "Radius r", "Normal length r = ½√(D² + E² - 4F) to tangent") { w, h ->
            Offset(w * 0.47f + h * 0.11f, h * 0.53f - h * 0.11f)
          }
        )
      )
      2 -> AnalyticBlueprintConfig(
        modeLabels = listOf("Center (h,k)", "Tangent Line", "Radical Axis"),
        liveEquation = "(D₁ - D₂)x + (E₁ - E₂)y + (F₁ - F₂) = 0",
        liveSummary = "Radical axis is the locus of equal tangent lengths, perpendicular to line of centers C₁C₂.",
        hotspots = listOf(
          AnalyticHotspotSpec("radical", "Radical Axis", "Subtract C₂ from C₁ (with x²,y² coeff = 1) to get line") { w, h ->
            Offset(w * 0.52f, h * 0.25f)
          },
          AnalyticHotspotSpec("c1", "Center C₁", "Center of Circle 1: (-D₁/2, -E₁/2)") { w, h ->
            Offset(w * 0.36f, h * 0.52f)
          },
          AnalyticHotspotSpec("c2", "Center C₂", "Center of Circle 2: (-D₂/2, -E₂/2)") { w, h ->
            Offset(w * 0.68f, h * 0.52f)
          }
        )
      )
      else -> AnalyticBlueprintConfig(
        modeLabels = listOf("Center (h,k)", "Tangent Line", "Radical Axis"),
        liveEquation = "(x - h)² + (y - k)² = r²",
        liveSummary = "General: x² + y² + Dx + Ey + F = 0  •  h = -D/2, k = -E/2, r = ½√(D² + E² - 4F)",
        hotspots = listOf(
          AnalyticHotspotSpec("center", "Center C(h,k)", "Center coordinates: h = -D/2,  k = -E/2") { w, h ->
            Offset(w * 0.50f, h * 0.50f)
          },
          AnalyticHotspotSpec("radius", "Radius r", "r = ½√(D² + E² - 4F)  (real circle if D² + E² - 4F > 0)") { w, h ->
            Offset(w * 0.50f + h * 0.24f, h * 0.50f - h * 0.14f)
          },
          AnalyticHotspotSpec("point", "Point P(x,y)", "Locus at constant distance r from C(h, k)") { w, h ->
            Offset(w * 0.50f + h * 0.31f, h * 0.50f)
          }
        )
      )
    }

    // [06] Parabola (Opens Up, Opens Down, Opens Right, Opens Left)
    5 -> when (mode) {
      1 -> AnalyticBlueprintConfig(
        modeLabels = listOf("Opens Up", "Opens Down", "Opens Right", "Opens Left"),
        liveEquation = "(x - h)² = -4a(y - k)",
        liveSummary = "Vertex V(h, k)  •  Focus F(h, k - a)  •  Directrix y = k + a  •  LR = 4a",
        hotspots = listOf(
          AnalyticHotspotSpec("vertex", "Vertex V", "V(h, k) — Highest point (maximum) on downward curve") { w, h ->
            Offset(w * 0.50f, h * 0.34f)
          },
          AnalyticHotspotSpec("focus", "Focus F", "F(h, k - a) — Lies 'a' units below Vertex inside curve") { w, h ->
            Offset(w * 0.50f, h * 0.52f)
          },
          AnalyticHotspotSpec("directrix", "Directrix D", "Line y = k + a — Horizontal line 'a' units above V") { w, h ->
            Offset(w * 0.72f, h * 0.16f)
          },
          AnalyticHotspotSpec("lr", "Latus Rectum", "Horizontal focal chord through F of total length LR = 4a") { w, h ->
            Offset(w * 0.69f, h * 0.52f)
          }
        )
      )
      2 -> AnalyticBlueprintConfig(
        modeLabels = listOf("Opens Up", "Opens Down", "Opens Right", "Opens Left"),
        liveEquation = "(y - k)² = 4a(x - h)",
        liveSummary = "Vertex V(h, k)  •  Focus F(h + a, k)  •  Directrix x = h - a  •  LR = 4a",
        hotspots = listOf(
          AnalyticHotspotSpec("vertex", "Vertex V", "V(h, k) — Leftmost point along horizontal axis y = k") { w, h ->
            Offset(w * 0.36f, h * 0.50f)
          },
          AnalyticHotspotSpec("focus", "Focus F", "F(h + a, k) — Lies 'a' units to the right of Vertex V") { w, h ->
            Offset(w * 0.52f, h * 0.50f)
          },
          AnalyticHotspotSpec("directrix", "Directrix D", "Line x = h - a — Vertical line 'a' units left of V") { w, h ->
            Offset(w * 0.20f, h * 0.26f)
          },
          AnalyticHotspotSpec("lr", "Latus Rectum", "Vertical focal chord through F of total length LR = 4a") { w, h ->
            Offset(w * 0.52f, h * 0.24f)
          }
        )
      )
      3 -> AnalyticBlueprintConfig(
        modeLabels = listOf("Opens Up", "Opens Down", "Opens Right", "Opens Left"),
        liveEquation = "(y - k)² = -4a(x - h)",
        liveSummary = "Vertex V(h, k)  •  Focus F(h - a, k)  •  Directrix x = h + a  •  LR = 4a",
        hotspots = listOf(
          AnalyticHotspotSpec("vertex", "Vertex V", "V(h, k) — Rightmost point along horizontal axis y = k") { w, h ->
            Offset(w * 0.64f, h * 0.50f)
          },
          AnalyticHotspotSpec("focus", "Focus F", "F(h - a, k) — Lies 'a' units to the left of Vertex V") { w, h ->
            Offset(w * 0.48f, h * 0.50f)
          },
          AnalyticHotspotSpec("directrix", "Directrix D", "Line x = h + a — Vertical line 'a' units right of V") { w, h ->
            Offset(w * 0.80f, h * 0.26f)
          },
          AnalyticHotspotSpec("lr", "Latus Rectum", "Vertical focal chord through F of total length LR = 4a") { w, h ->
            Offset(w * 0.48f, h * 0.24f)
          }
        )
      )
      else -> AnalyticBlueprintConfig(
        modeLabels = listOf("Opens Up", "Opens Down", "Opens Right", "Opens Left"),
        liveEquation = "(x - h)² = 4a(y - k)",
        liveSummary = "Vertex V(h, k)  •  Focus F(h, k + a)  •  Directrix y = k - a  •  LR = 4a",
        hotspots = listOf(
          AnalyticHotspotSpec("vertex", "Vertex V", "V(h, k) — Lowest point (minimum); midpoint of F and D") { w, h ->
            Offset(w * 0.50f, h * 0.66f)
          },
          AnalyticHotspotSpec("focus", "Focus F", "F(h, k + a) — Lies 'a' units above Vertex inside curve") { w, h ->
            Offset(w * 0.50f, h * 0.48f)
          },
          AnalyticHotspotSpec("directrix", "Directrix D", "Line y = k - a — Horizontal line 'a' units below V") { w, h ->
            Offset(w * 0.72f, h * 0.84f)
          },
          AnalyticHotspotSpec("lr", "Latus Rectum", "Horizontal focal chord through F of total length LR = 4a") { w, h ->
            Offset(w * 0.69f, h * 0.48f)
          }
        )
      )
    }

    // [07] Ellipse (Horizontal Axis, Vertical Axis)
    6 -> when (mode) {
      1 -> AnalyticBlueprintConfig(
        modeLabels = listOf("Horizontal Axis", "Vertical Axis"),
        liveEquation = "(x - h)²/b² + (y - k)²/a² = 1",
        liveSummary = "Vertical Major (2a)  •  Foci F(h, k ± c)  •  c² = a² - b²  •  LR = 2b²/a",
        hotspots = listOf(
          AnalyticHotspotSpec("center", "Center C", "C(h, k) — Intersection of major & minor axes") { w, h ->
            Offset(w * 0.50f, h * 0.50f)
          },
          AnalyticHotspotSpec("foci", "Foci F₁,F₂", "F(h, k ± c) where c = √(a² - b²) and e = c/a < 1") { w, h ->
            Offset(w * 0.50f, h * 0.28f)
          },
          AnalyticHotspotSpec("vertices", "Vertices V₁,V₂", "Major vertices at (h, k ± a); Co-vertices at (h ± b, k)") { w, h ->
            Offset(w * 0.50f, h * 0.16f)
          },
          AnalyticHotspotSpec("directrix", "Directrix D", "Horizontal directrices y = k ± a/e = k ± a²/c") { w, h ->
            Offset(w * 0.74f, h * 0.08f)
          },
          AnalyticHotspotSpec("lr", "Latus Rectum", "Chord through Focus ⊥ major axis: LR = 2b² / a") { w, h ->
            Offset(w * 0.61f, h * 0.28f)
          }
        )
      )
      else -> AnalyticBlueprintConfig(
        modeLabels = listOf("Horizontal Axis", "Vertical Axis"),
        liveEquation = "(x - h)²/a² + (y - k)²/b² = 1",
        liveSummary = "Horizontal Major (2a)  •  Foci F(h ± c, k)  •  c² = a² - b²  •  LR = 2b²/a",
        hotspots = listOf(
          AnalyticHotspotSpec("center", "Center C", "C(h, k) — Intersection of major (2a) & minor (2b) axes") { w, h ->
            Offset(w * 0.50f, h * 0.50f)
          },
          AnalyticHotspotSpec("foci", "Foci F₁,F₂", "F(h ± c, k) where c = √(a² - b²) and e = c/a < 1") { w, h ->
            Offset(w * 0.72f, h * 0.50f)
          },
          AnalyticHotspotSpec("vertices", "Vertices V₁,V₂", "Major vertices at (h ± a, k); Co-vertices at (h, k ± b)") { w, h ->
            Offset(w * 0.81f, h * 0.50f)
          },
          AnalyticHotspotSpec("directrix", "Directrix D", "Vertical directrices x = h ± a/e = h ± a²/c") { w, h ->
            Offset(w * 0.90f, h * 0.26f)
          },
          AnalyticHotspotSpec("lr", "Latus Rectum", "Chord through Focus ⊥ major axis: LR = 2b² / a") { w, h ->
            Offset(w * 0.72f, h * 0.34f)
          }
        )
      )
    }

    // [08] Hyperbola (Horizontal Axis, Vertical Axis)
    7 -> when (mode) {
      1 -> AnalyticBlueprintConfig(
        modeLabels = listOf("Horizontal Axis", "Vertical Axis"),
        liveEquation = "(y - k)²/a² - (x - h)²/b² = 1",
        liveSummary = "Vertical Transverse  •  Asymptotes y - k = ±(a/b)(x - h)  •  c² = a² + b²",
        hotspots = listOf(
          AnalyticHotspotSpec("center", "Center C", "C(h, k) — Center of auxiliary box & asymptotes") { w, h ->
            Offset(w * 0.50f, h * 0.50f)
          },
          AnalyticHotspotSpec("vertices", "Vertices V₁,V₂", "V(h, k ± a) along vertical transverse axis") { w, h ->
            Offset(w * 0.50f, h * 0.34f)
          },
          AnalyticHotspotSpec("foci", "Foci F₁,F₂", "F(h, k ± c) where c = √(a² + b²) and e = c/a > 1") { w, h ->
            Offset(w * 0.50f, h * 0.20f)
          },
          AnalyticHotspotSpec("asymptotes", "Asymptotes", "Diagonals of (2b × 2a) box: y - k = ±(a/b)(x - h)") { w, h ->
            Offset(w * 0.73f, h * 0.26f)
          },
          AnalyticHotspotSpec("directrix", "Directrix D", "Horizontal directrices y = k ± a/e  |  LR = 2b²/a") { w, h ->
            Offset(w * 0.72f, h * 0.40f)
          }
        )
      )
      else -> AnalyticBlueprintConfig(
        modeLabels = listOf("Horizontal Axis", "Vertical Axis"),
        liveEquation = "(x - h)²/a² - (y - k)²/b² = 1",
        liveSummary = "Horizontal Transverse  •  Asymptotes y - k = ±(b/a)(x - h)  •  c² = a² + b²",
        hotspots = listOf(
          AnalyticHotspotSpec("center", "Center C", "C(h, k) — Center of auxiliary box & asymptotes") { w, h ->
            Offset(w * 0.50f, h * 0.50f)
          },
          AnalyticHotspotSpec("vertices", "Vertices V₁,V₂", "V(h ± a, k) along horizontal transverse axis") { w, h ->
            Offset(w * 0.64f, h * 0.50f)
          },
          AnalyticHotspotSpec("foci", "Foci F₁,F₂", "F(h ± c, k) where c = √(a² + b²) and e = c/a > 1") { w, h ->
            Offset(w * 0.76f, h * 0.50f)
          },
          AnalyticHotspotSpec("asymptotes", "Asymptotes", "Diagonals of (2a × 2b) box: y - k = ±(b/a)(x - h)") { w, h ->
            Offset(w * 0.75f, h * 0.26f)
          },
          AnalyticHotspotSpec("directrix", "Directrix D", "Vertical directrices x = h ± a/e  |  LR = 2b²/a") { w, h ->
            Offset(w * 0.59f, h * 0.26f)
          }
        )
      )
    }

    else -> AnalyticBlueprintConfig(emptyList(), "", "", emptyList())
  }
}

@Composable
private fun AnalyticBlueprintCard(
  sectionIndex: Int,
  accentColor: Color,
  selectedMode: Int,
  selectedHotspotId: String?,
  onSelectMode: (Int) -> Unit,
  onSelectHotspot: (String) -> Unit
) {
  val palette = currentGlassPalette()
  val config = remember(sectionIndex, selectedMode) { buildBlueprintConfig(sectionIndex, selectedMode) }
  var canvasSize by remember { mutableStateOf(IntSize.Zero) }
  val density = LocalDensity.current
  val hitRadiusPx = with(density) { 30.dp.toPx() }

  val canvasBg = if (palette.isDark) {
    Color(0xFF091224).copy(alpha = 0.92f)
  } else {
    Color(0xFFF0F7FF).copy(alpha = 0.96f)
  }
  val gridColor = if (palette.isDark) Color(0xFF38BDF8).copy(alpha = 0.11f) else Color(0xFF0284C7).copy(alpha = 0.11f)
  val axisColor = if (palette.isDark) Color(0xFF94A3B8).copy(alpha = 0.45f) else Color(0xFF475569).copy(alpha = 0.40f)
  val cyanGlow = if (palette.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
  val amberHotspot = if (palette.isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
  val labelTextColor = if (palette.isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A)

  val activeHotspotSpec = remember(config, selectedHotspotId) {
    config.hotspots.firstOrNull { it.id == selectedHotspotId }
  }
  val orientation = rememberScreenOrientationState()

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("analytic_blueprint_lab_${sectionIndex + 1}"),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // 1. Mode / Orientation Switcher Pills
    if (config.modeLabels.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        config.modeLabels.forEachIndexed { idx, modeTitle ->
          val isSelected = idx == selectedMode
          RecessedGlassPill(
            isDarkMode = palette.isDark,
            fillColor = if (isSelected) {
              accentColor.copy(alpha = if (palette.isDark) 0.28f else 0.16f)
            } else {
              null
            },
            borderColor = if (isSelected) {
              accentColor.copy(alpha = 0.72f)
            } else {
              null
            },
            onClick = { onSelectMode(idx) },
            contentPadding = PaddingValues(horizontal = 11.dp, vertical = 6.dp),
            modifier = Modifier.testTag("analytic_mode_${sectionIndex + 1}_$idx")
          ) {
            Text(
              text = modeTitle,
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
              ),
              color = if (isSelected) {
                if (palette.isDark) Color.White else accentColor
              } else {
                palette.textSecondary
              }
            )
          }
        }
      }
    }

    val renderCanvasSurface: @Composable () -> Unit = {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = canvasBg,
        border = BorderStroke(
          width = 1.dp,
          brush = Brush.linearGradient(
            listOf(
              accentColor.copy(alpha = 0.45f),
              cyanGlow.copy(alpha = 0.30f)
            )
          )
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(if (orientation.isCompactLandscape) 200.dp else 240.dp)
      ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .pointerInput(sectionIndex, selectedMode, canvasSize) {
              detectTapGestures { tapOffset ->
                val w = canvasSize.width.toFloat()
                val h = canvasSize.height.toFloat()
                if (w > 0f && h > 0f) {
                  val nearest = config.hotspots
                    .map { spec ->
                      val anchor = spec.anchor(w, h)
                      spec to hypot(tapOffset.x - anchor.x, tapOffset.y - anchor.y)
                    }
                    .filter { it.second <= hitRadiusPx }
                    .minByOrNull { it.second }
                  if (nearest != null) {
                    onSelectHotspot(nearest.first.id)
                  }
                }
              }
            }
            .testTag("analytic_canvas_${sectionIndex + 1}")
        ) {
          drawAnalyticCadBlueprint(
            sectionIndex = sectionIndex,
            mode = selectedMode,
            selectedHotspotId = selectedHotspotId,
            hotspots = config.hotspots,
            accentColor = accentColor,
            cyanGlow = cyanGlow,
            amberHotspot = amberHotspot,
            gridColor = gridColor,
            axisColor = axisColor,
            labelColor = labelTextColor
          )
        }

        // Top-right subtle tap hint chip
        Surface(
          shape = CircleShape,
          color = canvasBg.copy(alpha = 0.85f),
          border = BorderStroke(0.8.dp, cyanGlow.copy(alpha = 0.32f)),
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.TouchApp,
              contentDescription = "Tap nodes to inspect",
              tint = cyanGlow,
              modifier = Modifier.size(11.dp)
            )
            Text(
              text = "TAP NODES",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp
              ),
              color = cyanGlow
            )
          }
        }
      }
    }
    }

    val renderHotspotLegendAndReadout: @Composable () -> Unit = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // 3. Tappable Landmark Hotspot Pill Chips (accessible alternative + clear legend)
    if (config.hotspots.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        config.hotspots.forEach { spot ->
          val isSpotSelected = spot.id == selectedHotspotId
          val pillBorderColor by animateColorAsState(
            targetValue = if (isSpotSelected) amberHotspot else cyanGlow.copy(alpha = 0.35f),
            animationSpec = tween(220),
            label = "hotspot_pill_border"
          )
          Surface(
            onClick = { onSelectHotspot(spot.id) },
            shape = CircleShape,
            color = if (isSpotSelected) {
              amberHotspot.copy(alpha = if (palette.isDark) 0.24f else 0.16f)
            } else {
              palette.nestedCardFill
            },
            border = BorderStroke(if (isSpotSelected) 1.2.dp else 0.8.dp, pillBorderColor),
            modifier = Modifier.testTag("analytic_hotspot_${sectionIndex + 1}_${spot.id}")
          ) {
            Text(
              text = spot.label,
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                fontWeight = if (isSpotSelected) FontWeight.ExtraBold else FontWeight.SemiBold
              ),
              color = if (isSpotSelected) amberHotspot else palette.textSecondary,
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            )
          }
        }
      }
    }

    // 4. Live Dynamic Coordinate / Equation & Hotspot Readout Card
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = if (palette.isDark) Color(0xFF0F172A).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.88f),
      border = BorderStroke(
        width = 0.9.dp,
        color = if (activeHotspotSpec != null) amberHotspot.copy(alpha = 0.55f) else accentColor.copy(alpha = 0.30f)
      ),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
      ) {
        Text(
          text = config.liveEquation,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold
          ),
          color = if (palette.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
        )
        Text(
          text = if (activeHotspotSpec != null) {
            "${activeHotspotSpec.label}: ${activeHotspotSpec.detail}"
          } else {
            config.liveSummary
          },
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = if (activeHotspotSpec != null) FontWeight.Bold else FontWeight.Medium,
            lineHeight = 15.sp
          ),
          color = if (activeHotspotSpec != null) amberHotspot else palette.textSecondary
        )
      }
    }
      }
    }

    if (orientation.isLandscape) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
      ) {
        Box(modifier = Modifier.weight(0.52f)) { renderCanvasSurface() }
        Box(modifier = Modifier.weight(0.48f)) { renderHotspotLegendAndReadout() }
      }
    } else {
      renderCanvasSurface()
      renderHotspotLegendAndReadout()
    }
  }
}

// ============================================================================
// CAD CANVAS DRAWING ENGINE FOR ALL 6 GEOMETRIC BLUEPRINTS
// ============================================================================

private fun DrawScope.drawAnalyticCadBlueprint(
  sectionIndex: Int,
  mode: Int,
  selectedHotspotId: String?,
  hotspots: List<AnalyticHotspotSpec>,
  accentColor: Color,
  cyanGlow: Color,
  amberHotspot: Color,
  gridColor: Color,
  axisColor: Color,
  labelColor: Color
) {
  val w = size.width
  val h = size.height
  val step = 24.dp.toPx()

  // Subtle CAD Grid
  var gx = step
  while (gx < w) {
    drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
    gx += step
  }
  var gy = step
  while (gy < h) {
    drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
    gy += step
  }

  // Coordinate Axes (centered unless section customizes origin)
  val cx = w * 0.50f
  val cy = h * 0.50f
  val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
  val fineDash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

  val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = labelColor.toArgb()
    textSize = 11.5.sp.toPx()
    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
  }

  fun drawCadLabel(text: String, pos: Offset, color: Color = labelColor) {
    textPaint.color = color.toArgb()
    drawContext.canvas.nativeCanvas.drawText(text, pos.x, pos.y, textPaint)
  }

  fun drawRightAngleMarker(corner: Offset, dir1: Offset, dir2: Offset, sizePx: Float = 10.dp.toPx()) {
    val u1 = dir1 / hypot(dir1.x, dir1.y).coerceAtLeast(0.001f)
    val u2 = dir2 / hypot(dir2.x, dir2.y).coerceAtLeast(0.001f)
    val p1 = corner + u1 * sizePx
    val p2 = p1 + u2 * sizePx
    val p3 = corner + u2 * sizePx
    val path = Path().apply {
      moveTo(p1.x, p1.y)
      lineTo(p2.x, p2.y)
      lineTo(p3.x, p3.y)
    }
    drawPath(path, color = cyanGlow, style = Stroke(width = 1.6.dp.toPx()))
  }

  when (sectionIndex) {
    // [02] STRAIGHT LINES & ANGLE BETWEEN LINES
    1 -> {
      drawLine(axisColor, Offset(w * 0.08f, h * 0.62f), Offset(w * 0.92f, h * 0.62f), strokeWidth = 1.4.dp.toPx())
      drawLine(axisColor, Offset(w * 0.44f, h * 0.08f), Offset(w * 0.44f, h * 0.92f), strokeWidth = 1.4.dp.toPx())
      drawCadLabel("x", Offset(w * 0.93f, h * 0.63f), axisColor)
      drawCadLabel("y", Offset(w * 0.45f, h * 0.12f), axisColor)

      when (mode) {
        1 -> { // Perpendicular
          val pInt = Offset(w * 0.50f, h * 0.50f)
          drawLine(accentColor, Offset(w * 0.16f, h * 0.84f), Offset(w * 0.84f, h * 0.16f), strokeWidth = 2.6.dp.toPx(), cap = StrokeCap.Round)
          drawLine(cyanGlow, Offset(w * 0.22f, h * 0.22f), Offset(w * 0.78f, h * 0.78f), strokeWidth = 2.6.dp.toPx(), cap = StrokeCap.Round)
          drawRightAngleMarker(pInt, Offset(1f, -1f), Offset(1f, 1f))
          drawCadLabel("L₁ (m₁)", Offset(w * 0.76f, h * 0.24f), accentColor)
          drawCadLabel("L₂ (m₂)", Offset(w * 0.72f, h * 0.76f), cyanGlow)
          drawCadLabel("90°", Offset(pInt.x + 16.dp.toPx(), pInt.y + 4.dp.toPx()), amberHotspot)
        }
        2 -> { // Normal Form
          val origin = Offset(w * 0.44f, h * 0.62f)
          val lStart = Offset(w * 0.30f, h * 0.14f)
          val lEnd = Offset(w * 0.86f, h * 0.72f)
          // Closest point from origin to line segment
          val vx = lEnd.x - lStart.x
          val vy = lEnd.y - lStart.y
          val t = ((origin.x - lStart.x) * vx + (origin.y - lStart.y) * vy) / (vx * vx + vy * vy)
          val foot = Offset(lStart.x + t * vx, lStart.y + t * vy)
          drawLine(accentColor, lStart, lEnd, strokeWidth = 2.8.dp.toPx(), cap = StrokeCap.Round)
          drawLine(cyanGlow, origin, foot, strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
          drawRightAngleMarker(foot, origin - foot, lStart - foot)
          val angleDeg = Math.toDegrees(atan2((origin.y - foot.y).toDouble(), (foot.x - origin.x).toDouble())).toFloat()
          drawArc(
            color = amberHotspot,
            startAngle = -angleDeg,
            sweepAngle = angleDeg,
            useCenter = false,
            topLeft = Offset(origin.x - 22.dp.toPx(), origin.y - 22.dp.toPx()),
            size = Size(44.dp.toPx(), 44.dp.toPx()),
            style = Stroke(width = 1.8.dp.toPx())
          )
          drawCadLabel("p", Offset((origin.x + foot.x) / 2f - 4.dp.toPx(), (origin.y + foot.y) / 2f - 8.dp.toPx()), cyanGlow)
          drawCadLabel("ω", Offset(origin.x + 25.dp.toPx(), origin.y - 8.dp.toPx()), amberHotspot)
        }
        else -> { // Intersecting
          drawLine(accentColor, Offset(w * 0.14f, h * 0.78f), Offset(w * 0.86f, h * 0.18f), strokeWidth = 2.6.dp.toPx(), cap = StrokeCap.Round)
          drawLine(cyanGlow, Offset(w * 0.18f, h * 0.36f), Offset(w * 0.86f, h * 0.72f), strokeWidth = 2.6.dp.toPx(), cap = StrokeCap.Round)
          drawArc(
            color = amberHotspot,
            startAngle = -38f,
            sweepAngle = 64f,
            useCenter = false,
            topLeft = Offset(w * 0.45f, h * 0.43f),
            size = Size(38.dp.toPx(), 38.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
          )
          drawCadLabel("L₁", Offset(w * 0.78f, h * 0.22f), accentColor)
          drawCadLabel("L₂", Offset(w * 0.78f, h * 0.66f), cyanGlow)
          drawCadLabel("θ", Offset(w * 0.56f, h * 0.52f), amberHotspot)
        }
      }
    }

    // [03] PERPENDICULAR DISTANCES
    2 -> {
      when (mode) {
        1 -> { // Parallel Lines
          drawLine(accentColor, Offset(w * 0.16f, h * 0.74f), Offset(w * 0.60f, h * 0.16f), strokeWidth = 2.8.dp.toPx(), cap = StrokeCap.Round)
          drawLine(accentColor, Offset(w * 0.40f, h * 0.84f), Offset(w * 0.84f, h * 0.26f), strokeWidth = 2.8.dp.toPx(), cap = StrokeCap.Round)
          val p1 = Offset(w * 0.38f, h * 0.45f)
          val p2 = Offset(w * 0.62f, h * 0.55f)
          drawLine(cyanGlow, p1, p2, strokeWidth = 2.4.dp.toPx(), pathEffect = dashEffect)
          drawRightAngleMarker(p1, p2 - p1, Offset(0.44f, -0.58f))
          drawRightAngleMarker(p2, p1 - p2, Offset(0.44f, -0.58f))
          drawCadLabel("L₁ (C₁)", Offset(w * 0.22f, h * 0.42f), accentColor)
          drawCadLabel("L₂ (C₂)", Offset(w * 0.68f, h * 0.60f), accentColor)
          drawCadLabel("d", Offset(w * 0.50f, h * 0.46f), cyanGlow)
        }
        else -> { // Point to Line
          val p = Offset(w * 0.34f, h * 0.31f)
          val foot = Offset(w * 0.61f, h * 0.58f)
          drawLine(accentColor, Offset(w * 0.24f, h * 0.85f), Offset(w * 0.84f, h * 0.25f), strokeWidth = 2.8.dp.toPx(), cap = StrokeCap.Round)
          drawLine(cyanGlow, p, foot, strokeWidth = 2.4.dp.toPx(), pathEffect = dashEffect)
          drawRightAngleMarker(foot, p - foot, Offset(1f, -1f))
          drawCadLabel("P(x₁, y₁)", Offset(p.x - 16.dp.toPx(), p.y - 12.dp.toPx()), amberHotspot)
          drawCadLabel("d", Offset((p.x + foot.x) / 2f + 6.dp.toPx(), (p.y + foot.y) / 2f - 4.dp.toPx()), cyanGlow)
          drawCadLabel("Ax+By+C=0", Offset(w * 0.56f, h * 0.79f), accentColor)
        }
      }
    }

    // [05] CIRCLE
    4 -> {
      drawLine(axisColor, Offset(w * 0.08f, h * 0.50f), Offset(w * 0.92f, h * 0.50f), strokeWidth = 1.1.dp.toPx())
      drawLine(axisColor, Offset(w * 0.50f, h * 0.08f), Offset(w * 0.50f, h * 0.92f), strokeWidth = 1.1.dp.toPx())

      when (mode) {
        1 -> { // Tangent Line
          val c = Offset(w * 0.47f, h * 0.53f)
          val r = h * 0.31f
          val pt = Offset(c.x + r * 0.707f, c.y - r * 0.707f)
          drawCircle(accentColor, r, c, style = Stroke(width = 2.6.dp.toPx()))
          drawLine(cyanGlow, c, pt, strokeWidth = 2.2.dp.toPx())
          drawLine(amberHotspot, Offset(pt.x - h * 0.24f, pt.y - h * 0.24f), Offset(pt.x + h * 0.26f, pt.y + h * 0.26f), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
          drawRightAngleMarker(pt, c - pt, Offset(1f, 1f))
          drawCadLabel("C(h,k)", Offset(c.x - 42.dp.toPx(), c.y + 14.dp.toPx()), labelColor)
          drawCadLabel("P(x₁,y₁)", Offset(pt.x + 8.dp.toPx(), pt.y - 8.dp.toPx()), amberHotspot)
          drawCadLabel("r", Offset((c.x + pt.x) / 2f - 12.dp.toPx(), (c.y + pt.y) / 2f - 6.dp.toPx()), cyanGlow)
        }
        2 -> { // Radical Axis
          val c1 = Offset(w * 0.36f, h * 0.52f)
          val r1 = h * 0.32f
          val c2 = Offset(w * 0.68f, h * 0.52f)
          val r2 = h * 0.26f
          drawCircle(accentColor, r1, c1, style = Stroke(width = 2.4.dp.toPx()))
          drawCircle(cyanGlow, r2, c2, style = Stroke(width = 2.4.dp.toPx()))
          drawLine(axisColor, c1, c2, strokeWidth = 1.3.dp.toPx(), pathEffect = fineDash)
          // Radical axis vertical line at x = w * 0.535f
          val rx = w * 0.535f
          drawLine(amberHotspot, Offset(rx, h * 0.09f), Offset(rx, h * 0.91f), strokeWidth = 2.4.dp.toPx(), pathEffect = dashEffect)
          drawRightAngleMarker(Offset(rx, h * 0.52f), Offset(0f, -1f), Offset(-1f, 0f))
          drawCadLabel("C₁", Offset(c1.x - 16.dp.toPx(), c1.y + 14.dp.toPx()), accentColor)
          drawCadLabel("C₂", Offset(c2.x + 6.dp.toPx(), c2.y + 14.dp.toPx()), cyanGlow)
          drawCadLabel("Radical Axis", Offset(rx + 6.dp.toPx(), h * 0.16f), amberHotspot)
        }
        else -> { // Center (h, k)
          val c = Offset(w * 0.50f, h * 0.50f)
          val r = h * 0.33f
          val pt = Offset(c.x + r * 0.866f, c.y - r * 0.5f)
          drawCircle(accentColor, r, c, style = Stroke(width = 2.8.dp.toPx()))
          drawLine(cyanGlow, c, pt, strokeWidth = 2.2.dp.toPx())
          drawCadLabel("C(h, k)", Offset(c.x - 42.dp.toPx(), c.y + 16.dp.toPx()), labelColor)
          drawCadLabel("r", Offset((c.x + pt.x) / 2f - 4.dp.toPx(), (c.y + pt.y) / 2f - 8.dp.toPx()), cyanGlow)
          drawCadLabel("P(x, y)", Offset(pt.x + 6.dp.toPx(), pt.y - 4.dp.toPx()), amberHotspot)
        }
      }
    }

    // [06] PARABOLA (0=Up, 1=Down, 2=Right, 3=Left)
    5 -> {
      drawLine(axisColor, Offset(w * 0.08f, cy), Offset(w * 0.92f, cy), strokeWidth = 1.1.dp.toPx())
      drawLine(axisColor, Offset(cx, h * 0.08f), Offset(cx, h * 0.92f), strokeWidth = 1.1.dp.toPx())

      val aPx = h * 0.18f
      val curvePath = Path()
      val steps = 80

      when (mode) {
        1 -> { // Opens Down
          val vx = w * 0.50f
          val vy = h * 0.34f
          val fy = vy + aPx
          val dy = vy - aPx
          val halfLr = 2f * aPx
          // Directrix
          drawLine(amberHotspot, Offset(w * 0.16f, dy), Offset(w * 0.84f, dy), strokeWidth = 2.dp.toPx(), pathEffect = dashEffect)
          // Latus Rectum
          drawLine(cyanGlow, Offset(vx - halfLr, fy), Offset(vx + halfLr, fy), strokeWidth = 2.dp.toPx(), pathEffect = fineDash)
          // Parabola (x - vx)^2 = 4*a*(y - vy)
          val maxDx = w * 0.30f
          for (i in 0..steps) {
            val dx = -maxDx + (2f * maxDx * i / steps)
            val py = vy + (dx * dx) / (4f * aPx)
            if (i == 0) curvePath.moveTo(vx + dx, py) else curvePath.lineTo(vx + dx, py)
          }
          drawPath(curvePath, accentColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
          drawCadLabel("V(h,k)", Offset(vx + 8.dp.toPx(), vy - 6.dp.toPx()), labelColor)
          drawCadLabel("F(h, k-a)", Offset(vx + 8.dp.toPx(), fy - 6.dp.toPx()), cyanGlow)
          drawCadLabel("Directrix: y = k + a", Offset(w * 0.17f, dy - 6.dp.toPx()), amberHotspot)
          drawCadLabel("LR = 4a", Offset(vx + halfLr + 6.dp.toPx(), fy + 4.dp.toPx()), cyanGlow)
        }
        2 -> { // Opens Right
          val vx = w * 0.36f
          val vy = h * 0.50f
          val fx = vx + w * 0.16f
          val dxLine = vx - w * 0.16f
          val aHoriz = w * 0.16f
          val halfLr = 2f * aHoriz.coerceAtMost(h * 0.26f)
          drawLine(amberHotspot, Offset(dxLine, h * 0.10f), Offset(dxLine, h * 0.90f), strokeWidth = 2.dp.toPx(), pathEffect = dashEffect)
          drawLine(cyanGlow, Offset(fx, vy - halfLr), Offset(fx, vy + halfLr), strokeWidth = 2.dp.toPx(), pathEffect = fineDash)
          val maxDy = h * 0.39f
          for (i in 0..steps) {
            val dy = -maxDy + (2f * maxDy * i / steps)
            val px = vx + (dy * dy) / (4f * aHoriz * 0.72f)
            if (i == 0) curvePath.moveTo(px, vy + dy) else curvePath.lineTo(px, vy + dy)
          }
          drawPath(curvePath, accentColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
          drawCadLabel("V", Offset(vx - 14.dp.toPx(), vy - 8.dp.toPx()), labelColor)
          drawCadLabel("F(h+a, k)", Offset(fx + 8.dp.toPx(), vy - 8.dp.toPx()), cyanGlow)
          drawCadLabel("D: x = h - a", Offset(dxLine - 48.dp.toPx(), h * 0.18f), amberHotspot)
          drawCadLabel("LR = 4a", Offset(fx + 6.dp.toPx(), vy - halfLr + 4.dp.toPx()), cyanGlow)
        }
        3 -> { // Opens Left
          val vx = w * 0.64f
          val vy = h * 0.50f
          val fx = vx - w * 0.16f
          val dxLine = vx + w * 0.16f
          val aHoriz = w * 0.16f
          val halfLr = 2f * aHoriz.coerceAtMost(h * 0.26f)
          drawLine(amberHotspot, Offset(dxLine, h * 0.10f), Offset(dxLine, h * 0.90f), strokeWidth = 2.dp.toPx(), pathEffect = dashEffect)
          drawLine(cyanGlow, Offset(fx, vy - halfLr), Offset(fx, vy + halfLr), strokeWidth = 2.dp.toPx(), pathEffect = fineDash)
          val maxDy = h * 0.39f
          for (i in 0..steps) {
            val dy = -maxDy + (2f * maxDy * i / steps)
            val px = vx - (dy * dy) / (4f * aHoriz * 0.72f)
            if (i == 0) curvePath.moveTo(px, vy + dy) else curvePath.lineTo(px, vy + dy)
          }
          drawPath(curvePath, accentColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
          drawCadLabel("V", Offset(vx + 6.dp.toPx(), vy - 8.dp.toPx()), labelColor)
          drawCadLabel("F(h-a, k)", Offset(fx - 64.dp.toPx(), vy - 8.dp.toPx()), cyanGlow)
          drawCadLabel("D: x = h + a", Offset(dxLine + 6.dp.toPx(), h * 0.18f), amberHotspot)
          drawCadLabel("LR = 4a", Offset(fx - 54.dp.toPx(), vy - halfLr + 4.dp.toPx()), cyanGlow)
        }
        else -> { // Opens Up
          val vx = w * 0.50f
          val vy = h * 0.66f
          val fy = vy - aPx
          val dy = vy + aPx
          val halfLr = 2f * aPx
          drawLine(amberHotspot, Offset(w * 0.16f, dy), Offset(w * 0.84f, dy), strokeWidth = 2.dp.toPx(), pathEffect = dashEffect)
          drawLine(cyanGlow, Offset(vx - halfLr, fy), Offset(vx + halfLr, fy), strokeWidth = 2.dp.toPx(), pathEffect = fineDash)
          val maxDx = w * 0.30f
          for (i in 0..steps) {
            val dx = -maxDx + (2f * maxDx * i / steps)
            val py = vy - (dx * dx) / (4f * aPx)
            if (i == 0) curvePath.moveTo(vx + dx, py) else curvePath.lineTo(vx + dx, py)
          }
          drawPath(curvePath, accentColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
          drawCadLabel("V(h,k)", Offset(vx + 8.dp.toPx(), vy + 14.dp.toPx()), labelColor)
          drawCadLabel("F(h, k+a)", Offset(vx + 8.dp.toPx(), fy - 8.dp.toPx()), cyanGlow)
          drawCadLabel("Directrix: y = k - a", Offset(w * 0.17f, dy + 14.dp.toPx()), amberHotspot)
          drawCadLabel("LR = 4a", Offset(vx + halfLr + 6.dp.toPx(), fy + 4.dp.toPx()), cyanGlow)
        }
      }
    }

    // [07] ELLIPSE (0=Horizontal Major, 1=Vertical Major)
    6 -> {
      drawLine(axisColor, Offset(w * 0.08f, cy), Offset(w * 0.92f, cy), strokeWidth = 1.1.dp.toPx())
      drawLine(axisColor, Offset(cx, h * 0.06f), Offset(cx, h * 0.94f), strokeWidth = 1.1.dp.toPx())

      if (mode == 1) { // Vertical Major Axis
        val rx = w * 0.18f
        val ry = h * 0.34f
        val cDist = h * 0.22f
        val dLine = h * 0.08f
        val lrHalfW = rx * 0.62f
        drawOval(
          color = accentColor,
          topLeft = Offset(cx - rx, cy - ry),
          size = Size(rx * 2f, ry * 2f),
          style = Stroke(width = 2.8.dp.toPx())
        )
        drawLine(amberHotspot, Offset(w * 0.16f, dLine), Offset(w * 0.84f, dLine), strokeWidth = 1.8.dp.toPx(), pathEffect = dashEffect)
        drawLine(amberHotspot, Offset(w * 0.16f, h - dLine), Offset(w * 0.84f, h - dLine), strokeWidth = 1.8.dp.toPx(), pathEffect = dashEffect)
        drawLine(cyanGlow, Offset(cx - lrHalfW, cy - cDist), Offset(cx + lrHalfW, cy - cDist), strokeWidth = 2.dp.toPx(), pathEffect = fineDash)
        drawCadLabel("C", Offset(cx + 6.dp.toPx(), cy + 12.dp.toPx()), labelColor)
        drawCadLabel("V₁", Offset(cx + 8.dp.toPx(), cy - ry + 6.dp.toPx()), accentColor)
        drawCadLabel("F₁", Offset(cx + 8.dp.toPx(), cy - cDist - 4.dp.toPx()), cyanGlow)
        drawCadLabel("F₂", Offset(cx + 8.dp.toPx(), cy + cDist + 10.dp.toPx()), cyanGlow)
        drawCadLabel("LR", Offset(cx + lrHalfW + 6.dp.toPx(), cy - cDist + 4.dp.toPx()), cyanGlow)
        drawCadLabel("D: y = k + a/e", Offset(w * 0.16f, dLine + 13.dp.toPx()), amberHotspot)
      } else { // Horizontal Major Axis
        val rx = w * 0.31f
        val ry = h * 0.25f
        val cDist = w * 0.22f
        val dX = w * 0.90f
        val lrHalfH = ry * 0.65f
        drawOval(
          color = accentColor,
          topLeft = Offset(cx - rx, cy - ry),
          size = Size(rx * 2f, ry * 2f),
          style = Stroke(width = 2.8.dp.toPx())
        )
        drawLine(amberHotspot, Offset(dX, h * 0.12f), Offset(dX, h * 0.88f), strokeWidth = 1.8.dp.toPx(), pathEffect = dashEffect)
        drawLine(amberHotspot, Offset(w - dX, h * 0.12f), Offset(w - dX, h * 0.88f), strokeWidth = 1.8.dp.toPx(), pathEffect = dashEffect)
        drawLine(cyanGlow, Offset(cx + cDist, cy - lrHalfH), Offset(cx + cDist, cy + lrHalfH), strokeWidth = 2.dp.toPx(), pathEffect = fineDash)
        drawCadLabel("C", Offset(cx + 6.dp.toPx(), cy + 13.dp.toPx()), labelColor)
        drawCadLabel("V₁", Offset(cx + rx + 5.dp.toPx(), cy + 14.dp.toPx()), accentColor)
        drawCadLabel("F₁", Offset(cx - cDist - 16.dp.toPx(), cy + 15.dp.toPx()), cyanGlow)
        drawCadLabel("F₂", Offset(cx + cDist - 4.dp.toPx(), cy + 15.dp.toPx()), cyanGlow)
        drawCadLabel("LR", Offset(cx + cDist + 5.dp.toPx(), cy - lrHalfH + 4.dp.toPx()), cyanGlow)
        drawCadLabel("D", Offset(dX - 14.dp.toPx(), h * 0.20f), amberHotspot)
      }
    }

    // [08] HYPERBOLA & ASYMPTOTES (0=Horizontal Transverse, 1=Vertical Transverse)
    7 -> {
      drawLine(axisColor, Offset(w * 0.08f, cy), Offset(w * 0.92f, cy), strokeWidth = 1.1.dp.toPx())
      drawLine(axisColor, Offset(cx, h * 0.06f), Offset(cx, h * 0.94f), strokeWidth = 1.1.dp.toPx())

      val aBox = w * 0.14f
      val bBox = h * 0.16f
      val slope = bBox / aBox
      // Asymptotes (same diagonals of fundamental rectangle)
      drawLine(
        amberHotspot,
        Offset(cx - w * 0.36f, cy + slope * (w * 0.36f)),
        Offset(cx + w * 0.36f, cy - slope * (w * 0.36f)),
        strokeWidth = 1.8.dp.toPx(),
        pathEffect = dashEffect
      )
      drawLine(
        amberHotspot,
        Offset(cx - w * 0.36f, cy - slope * (w * 0.36f)),
        Offset(cx + w * 0.36f, cy + slope * (w * 0.36f)),
        strokeWidth = 1.8.dp.toPx(),
        pathEffect = dashEffect
      )
      // Fundamental rectangle
      drawRect(
        color = axisColor.copy(alpha = 0.5f),
        topLeft = Offset(cx - aBox, cy - bBox),
        size = Size(aBox * 2f, bBox * 2f),
        style = Stroke(width = 1.2.dp.toPx(), pathEffect = fineDash)
      )

      val steps = 50
      if (mode == 1) { // Vertical Transverse
        val aV = h * 0.16f
        val bH = w * 0.14f
        val cDist = h * 0.30f
        val dY = h * 0.40f
        drawLine(cyanGlow, Offset(w * 0.24f, dY), Offset(w * 0.76f, dY), strokeWidth = 1.5.dp.toPx(), pathEffect = fineDash)
        drawLine(cyanGlow, Offset(w * 0.24f, h - dY), Offset(w * 0.76f, h - dY), strokeWidth = 1.5.dp.toPx(), pathEffect = fineDash)

        val upperBranch = Path()
        val lowerBranch = Path()
        val maxX = w * 0.28f
        for (i in 0..steps) {
          val dx = -maxX + (2f * maxX * i / steps)
          val dy = aV * sqrt(1f + (dx / bH).pow(2))
          if (i == 0) {
            upperBranch.moveTo(cx + dx, cy - dy)
            lowerBranch.moveTo(cx + dx, cy + dy)
          } else {
            upperBranch.lineTo(cx + dx, cy - dy)
            lowerBranch.lineTo(cx + dx, cy + dy)
          }
        }
        drawPath(upperBranch, accentColor, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round))
        drawPath(lowerBranch, accentColor, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round))

        drawCadLabel("C", Offset(cx + 6.dp.toPx(), cy + 12.dp.toPx()), labelColor)
        drawCadLabel("V₁", Offset(cx + 7.dp.toPx(), cy - aV - 4.dp.toPx()), accentColor)
        drawCadLabel("F₁", Offset(cx + 7.dp.toPx(), cy - cDist - 4.dp.toPx()), cyanGlow)
        drawCadLabel("Asymptote", Offset(w * 0.73f, h * 0.22f), amberHotspot)
        drawCadLabel("D", Offset(w * 0.73f, dY - 4.dp.toPx()), cyanGlow)
      } else { // Horizontal Transverse
        val aH = w * 0.14f
        val bV = h * 0.16f
        val cDist = w * 0.26f
        val dX = w * 0.59f
        drawLine(cyanGlow, Offset(dX, h * 0.16f), Offset(dX, h * 0.84f), strokeWidth = 1.5.dp.toPx(), pathEffect = fineDash)
        drawLine(cyanGlow, Offset(w - dX, h * 0.16f), Offset(w - dX, h * 0.84f), strokeWidth = 1.5.dp.toPx(), pathEffect = fineDash)

        val rightBranch = Path()
        val leftBranch = Path()
        val maxY = h * 0.34f
        for (i in 0..steps) {
          val dy = -maxY + (2f * maxY * i / steps)
          val dx = aH * sqrt(1f + (dy / bV).pow(2))
          if (i == 0) {
            rightBranch.moveTo(cx + dx, cy + dy)
            leftBranch.moveTo(cx - dx, cy + dy)
          } else {
            rightBranch.lineTo(cx + dx, cy + dy)
            leftBranch.lineTo(cx - dx, cy + dy)
          }
        }
        drawPath(rightBranch, accentColor, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round))
        drawPath(leftBranch, accentColor, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round))

        drawCadLabel("C", Offset(cx + 5.dp.toPx(), cy + 12.dp.toPx()), labelColor)
        drawCadLabel("V₁", Offset(cx + aH + 4.dp.toPx(), cy + 14.dp.toPx()), accentColor)
        drawCadLabel("F₁", Offset(cx + cDist + 5.dp.toPx(), cy + 14.dp.toPx()), cyanGlow)
        drawCadLabel("Asymptote", Offset(w * 0.72f, h * 0.21f), amberHotspot)
        drawCadLabel("D", Offset(dX + 4.dp.toPx(), h * 0.22f), cyanGlow)
      }
    }
  }

  // Draw glowing interactive hotspot nodes on top of the figure
  hotspots.forEach { spot ->
    val anchor = spot.anchor(w, h)
    val isSelected = spot.id == selectedHotspotId
    val nodeColor = if (isSelected) amberHotspot else cyanGlow
    drawCircle(
      color = nodeColor.copy(alpha = if (isSelected) 0.36f else 0.22f),
      radius = if (isSelected) 15.dp.toPx() else 10.dp.toPx(),
      center = anchor
    )
    drawCircle(
      color = nodeColor,
      radius = if (isSelected) 6.dp.toPx() else 4.2.dp.toPx(),
      center = anchor
    )
    drawCircle(
      color = Color.White,
      radius = if (isSelected) 2.5.dp.toPx() else 1.8.dp.toPx(),
      center = anchor
    )
  }
}

// ============================================================================
// SECTION DETAIL MODAL (TAPPING SECTION HEADER)
// ============================================================================

@Composable
private fun AnalyticSectionDetailGlassModal(
  sectionNumber: Int,
  section: AlgebraHandbookSection,
  accentColor: Color,
  onDismiss: () -> Unit
) {
  val palette = currentGlassPalette()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.55f))
        .padding(horizontal = 16.dp, vertical = 28.dp),
      contentAlignment = Alignment.Center
    ) {
      val modalOrientation = rememberScreenOrientationState()
      LiquidGlassCard(
        cornerRadius = 26.dp,
        elevation = 16.dp,
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = if (modalOrientation.isLandscape) 860.dp else 640.dp)
          .heightIn(max = if (modalOrientation.isCompactLandscape) 380.dp else 680.dp)
          .testTag("analytic_section_detail_modal_$sectionNumber")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = accentColor.copy(alpha = 0.16f),
              border = BorderStroke(1.dp, accentColor.copy(alpha = 0.40f))
            ) {
              Text(
                text = String.format(Locale.US, "MSTE / CE-04 • SECTION %02d", sectionNumber),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold
                ),
                color = accentColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }

            Surface(
              onClick = onDismiss,
              shape = CircleShape,
              color = palette.nestedCardFill,
              border = palette.nestedBorder,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(38.dp)
                .testTag("analytic_section_detail_close_button")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close Section Detail",
                  tint = palette.textPrimary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          Text(
            text = section.sectionTitle,
            style = MaterialTheme.typography.titleLarge.copy(
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold
            ),
            color = palette.textPrimary,
            softWrap = true,
            modifier = Modifier.fillMaxWidth()
          )

          HorizontalDivider(
            thickness = 1.dp,
            color = accentColor.copy(alpha = 0.25f)
          )

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            section.items.forEach { rule ->
              val secondaryEq = if (rule.subItems.isNotEmpty()) {
                rule.subItems.joinToString("  |  ")
              } else {
                null
              }
              MathFormulaCapsule(
                label = rule.title,
                equation = rule.formulaOrStatement,
                secondaryEquation = secondaryEq,
                accentColor = accentColor
              )
            }
          }
        }
      }
    }
  }
}

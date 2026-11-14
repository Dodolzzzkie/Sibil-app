package com.example

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.JetBrainsMonoFamily
import kotlinx.coroutines.launch
import java.util.Locale

internal enum class EngineeringVolumePillar(
  val code: String,
  val libraryTitle: String,
  val shelfHeader: String
) {
  PSAD(
    code = "PSAD",
    libraryTitle = "PSAD Library",
    shelfHeader = "PSAD SUB-VOLUMES"
  ),
  HPGE(
    code = "HPGE",
    libraryTitle = "HPGE Library",
    shelfHeader = "HPGE SUB-VOLUMES"
  )
}

internal data class EngineeringSubVolumeSpec(
  val id: String,
  val pillar: EngineeringVolumePillar,
  val title: String,
  val shortTitle: String,
  val compactSummaryTag: String,
  val tagBadge: String,
  val accentColor: Color,
  val secondaryAccentColor: Color,
  val searchKeywords: String,
  val sections: List<AlgebraHandbookSection>
)

internal fun buildPsadSubVolumeSpecs(): List<EngineeringSubVolumeSpec> = listOf(
  EngineeringSubVolumeSpec(
    id = "psad_strength_of_materials",
    pillar = EngineeringVolumePillar.PSAD,
    title = "Strength of Materials",
    shortTitle = "Strength of Materials",
    compactSummaryTag = "10 Chapters • Interactive CAD Figures",
    tagBadge = "PSAD • 01",
    accentColor = Color(0xFFF59E0B),
    secondaryAccentColor = Color(0xFFD97706),
    searchKeywords = "strength materials axial bearing pressure vessel shear punching poisson thermal torsion shaft sfd bmd flexural transverse kern mohr circle deflection buckling euler",
    sections = buildStrengthOfMaterialsSections()
  ),
  EngineeringSubVolumeSpec(
    id = "psad_structural_analysis",
    pillar = EngineeringVolumePillar.PSAD,
    title = "Structural Analysis",
    shortTitle = "Structural Analysis",
    compactSummaryTag = "8 Chapters • Determinacy & Beam Lab",
    tagBadge = "PSAD • 02",
    accentColor = Color(0xFFEF4444),
    secondaryAccentColor = Color(0xFFF97316),
    searchKeywords = "structural analysis determinacy stability truss frame beam deflection fem fixed end moment virtual work unit load castigliano three moment clapeyron slope deflection hardy cross influence line muller breslau",
    sections = buildStructuralAnalysisSections()
  ),
  EngineeringSubVolumeSpec(
    id = "psad_rcd_pcd",
    pillar = EngineeringVolumePillar.PSAD,
    title = "RCD & PCD (Reinforced and Prestressed Concrete Designs)",
    shortTitle = "RCD & PCD",
    compactSummaryTag = "9 Chapters • NSCP 2015 & CAD Labs",
    tagBadge = "PSAD • 03",
    accentColor = Color(0xFF10B981),
    secondaryAccentColor = Color(0xFF14B8A6),
    searchKeywords = "rcd pcd reinforced prestressed concrete nscp 2015 aci 318 load combination phi whitney stress block doubly t beam shear stirrup development length hook splice column interaction pm footing punching prestress",
    sections = buildRcdPcdSections()
  ),
  EngineeringSubVolumeSpec(
    id = "psad_steel_design",
    pillar = EngineeringVolumePillar.PSAD,
    title = "Steel Design",
    shortTitle = "Steel Design",
    compactSummaryTag = "8 Chapters • NSCP 2015 & Buckling Labs",
    tagBadge = "PSAD • 04",
    accentColor = Color(0xFF0EA5E9),
    secondaryAccentColor = Color(0xFF3B82F6),
    searchKeywords = "steel design nscp 2015 aisc lrfd asd a36 a992 tension rupture block shear stagger compression column euler buckling flexural compact ltb cb web crippling beam column interaction bolt bearing slip weld fillet",
    sections = buildSteelDesignSections()
  ),
  EngineeringSubVolumeSpec(
    id = "psad_foundation_engineering",
    pillar = EngineeringVolumePillar.PSAD,
    title = "Foundation Engineering",
    shortTitle = "Foundation Eng.",
    compactSummaryTag = "8 Chapters • NSCP 300s & CAD Labs",
    tagBadge = "PSAD • 05",
    accentColor = Color(0xFF6366F1),
    secondaryAccentColor = Color(0xFF8B5CF6),
    searchKeywords = "foundation engineering shallow deep bearing capacity terzaghi gwt water table meyerhof eccentric mat settlement consolidation pile group converse labarre enr rankine earth pressure retaining wall overturning sliding",
    sections = buildFoundationEngineeringSections()
  )
)

private fun buildStrengthOfMaterialsSections(): List<AlgebraHandbookSection> = listOf(
  AlgebraHandbookSection(
    sectionTitle = "Simple Normal & Bearing Stresses",
    ribbonLabel = "01 Normal",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Axial Normal Stress",
        formulaOrStatement = "σ = P / A",
        note = "Average normal stress on cross-sectional area A perpendicular to centric axial force P."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Bearing Stress",
        formulaOrStatement = "σ_b = P / A_b = P / (t · d)",
        note = "Contact pressure projected on plate thickness t and bolt/pin diameter d."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Thin-Walled Cylindrical Pressure Vessel",
        formulaOrStatement = "σ_t = (p · D) / (2 · t)  [Hoop]    •    σ_L = (p · D) / (4 · t)  [Longitudinal]",
        note = "Valid when wall ratio D/t ≥ 20; circumferential hoop stress is twice the longitudinal stress."
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Thin-Walled Spherical Vessel",
        formulaOrStatement = "σ = (p · D) / (4 · t)",
        note = "Equal biaxial membrane tension in every tangent direction."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Simple Shear & Punching Shear",
    ribbonLabel = "02 Shear",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Single & Double Shear",
        formulaOrStatement = "Single: τ = V / A    •    Double: τ = V / (2 · A)",
        note = "Divide shear force V by total resisting shear planes across the fastener."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Punching Shear (Circular)",
        formulaOrStatement = "τ_p = P / (π · d · t)",
        note = "Shear area equals cylindrical perimeter πd times plate/slab thickness t."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Punching Shear (Perimeter)",
        formulaOrStatement = "τ_p = P / (b_o · t)",
        note = "Critical perimeter b_o at d/2 from column face for two-way slab or footing punching."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Axial Deformation, Poisson's Ratio & Thermal Stress",
    ribbonLabel = "03 Axial/Thermal",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Hooke's Law & Axial Elongation",
        formulaOrStatement = "δ = (P · L) / (A · E) = Σ (P_i · L_i) / (A_i · E_i)",
        note = "Sum piecewise axial deformations for stepped shafts or varying axial loads."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Poisson's Ratio & Elastic Constants",
        formulaOrStatement = "ν = -ε_lat / ε_long    •    E = 2G(1 + ν) = 3K(1 - 2ν)",
        note = "Relates Young's modulus E, shear modulus G, bulk modulus K, and Poisson's ratio ν."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Volumetric Strain",
        formulaOrStatement = "ΔV / V = ε_x + ε_y + ε_z",
        note = "Hydrostatic triaxial strain sum; equals (σ_x + σ_y + σ_z)(1 - 2ν)/E in isotropic linear elasticity."
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Thermal Deformation & Restrained Stress",
        formulaOrStatement = "δ_T = α · L · ΔT    •    σ_T = E · α · ΔT",
        note = "Free expansion δ_T produces zero stress unless prevented by rigid supports."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Torsion of Circular & Thin-Walled Shafts",
    ribbonLabel = "04 Torsion",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Torsional Shear Stress & Angle of Twist",
        formulaOrStatement = "τ_max = (T · r) / J    •    θ = (T · L) / (J · G)  [radians]",
        note = "Shear modulus G; convert θ from radians to degrees by multiplying by 180°/π."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Polar Moment of Inertia (J)",
        formulaOrStatement = "Solid: J = πD⁴ / 32    •    Hollow: J = π(D⁴ - d⁴) / 32",
        note = "Also J = πr⁴/2 for solid shafts of radius r."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Power Transmission",
        formulaOrStatement = "P = T · ω = 2π · f · T",
        note = "Use angular speed ω = 2π·N/60 rad/s (N in rpm) with torque T in N·m for Watts."
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Thin-Walled Closed Tube",
        formulaOrStatement = "τ = T / (2 · t · A_m)",
        note = "Bredt-Batho formula where A_m is the area enclosed by the centerline of the wall."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Shear & Bending Moment Diagrams (SFD & BMD)",
    ribbonLabel = "05 SFD/BMD",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Load–Shear Differential Relation",
        formulaOrStatement = "dV / dx = -w(x)    •    ΔV = ∫ w(x) dx",
        note = "Slope of shear diagram equals negative intensity of distributed load."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Shear–Moment Differential Relation",
        formulaOrStatement = "dM / dx = V(x)    •    ΔM = ∫ V(x) dx",
        note = "Change in bending moment equals area under the shear diagram; M_max occurs where V = 0."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Flexural (Bending) & Transverse Shear Stresses",
    ribbonLabel = "06 Flexure/τ",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Flexure Formula",
        formulaOrStatement = "σ_b = (M · y) / I    •    σ_max = (M · c) / I = M / S",
        note = "Neutral axis passes through centroid; section modulus S = I / c."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Transverse Horizontal Shear",
        formulaOrStatement = "τ = (V · Q) / (I · b)    •    Q = A' · ȳ'",
        note = "First moment of area Q of the region above/below the cut about the neutral axis."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Rectangular & Solid Circular Maximums",
        formulaOrStatement = "Rect: τ_max = 3V / (2A) = 1.5(V / A)    •    Solid Circle: τ_max = 4V / (3A)",
        note = "Maximum transverse shear occurs at the neutral axis."
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Thin-Walled Hollow Circle Maximum",
        formulaOrStatement = "τ_max = 2V / A",
        note = "Applies to thin circular tubes where wall thickness t ≪ radius r."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Combined Axial + Bending & Eccentric Loading",
    ribbonLabel = "07 Kern",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Combined Normal Stress",
        formulaOrStatement = "σ = ± (P / A) ± (M_x · y) / I_x ± (M_y · x) / I_y",
        note = "Superimpose direct axial stress with biaxial flexural bending."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Kern Limits (No Tension Zone)",
        formulaOrStatement = "Rect: e_x ≤ b / 6,  e_y ≤ h / 6    •    Solid Circle: e ≤ D / 8",
        note = "Middle-third rule for rectangles and middle-quarter rule for solid circular piers."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Transformation of Stress & Mohr's Circle",
    ribbonLabel = "08 Mohr's",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Normal Stress on Inclined Plane",
        formulaOrStatement = "σ_n = (σ_x + σ_y)/2 + [(σ_x - σ_y)/2]cos(2θ) + τ_xy · sin(2θ)",
        note = "Counterclockwise rotation θ from the x-face."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Shear Stress on Inclined Plane",
        formulaOrStatement = "τ_n = -[(σ_x - σ_y)/2]sin(2θ) + τ_xy · cos(2θ)",
        note = "Vanishes on principal planes where normal stresses reach extrema."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Principal Stresses & Max In-Plane Shear",
        formulaOrStatement = "σ_1,2 = (σ_x + σ_y)/2 ± R    •    τ_max = R = √[ ((σ_x - σ_y)/2)² + τ_xy² ]",
        note = "Center C = ((σ_x + σ_y)/2, 0) and radius R equals maximum in-plane shear stress."
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Principal Plane Orientation",
        formulaOrStatement = "tan(2θ_p) = (2 · τ_xy) / (σ_x - σ_y)",
        note = "Maximum shear planes are oriented 45° from principal planes."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Elastic Beam Deflection (Integration & Area-Moment)",
    ribbonLabel = "09 Elastic",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Differential Equations of Elastic Curve",
        formulaOrStatement = "EI (d⁴y / dx⁴) = -w(x)    •    EI (d²y / dx²) = M(x)",
        note = "Integrate M(x)/EI successively to solve slope θ = dy/dx and deflection y(x)."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "First Area-Moment Theorem (Slope)",
        formulaOrStatement = "θ_B/A = ∫_A^B (M / EI) dx",
        note = "Angle between tangents at A and B equals area of M/EI diagram between A and B."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Second Area-Moment Theorem (Deviation)",
        formulaOrStatement = "t_B/A = ∫_A^B (M · x_B / EI) dx",
        note = "Tangential deviation of B from tangent at A equals moment of M/EI area about B."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Columns & Euler Buckling",
    ribbonLabel = "10 Buckling",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Euler Critical Buckling Load & Stress",
        formulaOrStatement = "P_cr = (π² · E · I) / (K · L)²    •    σ_cr = (π² · E) / (KL / r)²",
        note = "Valid for elastic long columns where σ_cr ≤ proportional limit; r = √(I/A)."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Effective Length Factors (K)",
        formulaOrStatement = "Pin-Pin: K = 1.0  |  Fix-Free: K = 2.0  |  Fix-Pin: K = 0.7  |  Fix-Fix: K = 0.5",
        note = "Use theoretical K values (or AISC design K = 0.65 / 0.80 / 2.1 when specified)."
      )
    )
  )
)

private fun buildStructuralAnalysisSections(): List<AlgebraHandbookSection> = listOf(
  AlgebraHandbookSection(
    sectionTitle = "Static Determinacy & Stability",
    ribbonLabel = "01 Determinacy",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Planar Beams & Frames",
        formulaOrStatement = "D_s = (3m + r) - (3j + c)    [or r vs 3n + c]",
        note = "m = members, r = support reactions, j = joints, c = condition equations (1 per internal hinge)."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Planar Trusses",
        formulaOrStatement = "Determinate: b + r = 2j    •    Indeterminate: D_s = (b + r) - 2j",
        note = "Unstable if b + r < 2j or if reactions are all parallel/concurrent."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Space Trusses",
        formulaOrStatement = "Determinate: b + r = 3j    •    Indeterminate: D_s = (b + r) - 3j",
        note = "Each ball-and-socket 3D joint provides 3 equilibrium equations (ΣFx, ΣFy, ΣFz = 0)."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Deflection of Beams (Standard Board Exam Cases)",
    ribbonLabel = "02 Deflection",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Simple Beam — Center Load P",
        formulaOrStatement = "Δ_max = PL³ / (48EI)    •    θ_max = PL² / (16EI)",
        note = "Maximum deflection occurs at midspan x = L/2."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Simple Beam — UDL w",
        formulaOrStatement = "Δ_max = 5wL⁴ / (384EI)    •    θ_max = wL³ / (24EI)",
        note = "Uniformly distributed load w across full simply supported span L."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Simple Beam — Eccentric Load P (a, b)",
        formulaOrStatement = "Δ_P = (P·a²·b²) / (3EIL)    •    Δ_max = [P·b·(L² - b²)^(3/2)] / (9√3 · EIL)",
        note = "For a > b, maximum deflection occurs at x = √[(L² - b²)/3] from the left support."
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Cantilever — Tip Load P & Full UDL w",
        formulaOrStatement = "Tip P: Δ = PL³/(3EI), θ = PL²/(2EI)    •    UDL w: Δ = wL⁴/(8EI), θ = wL³/(6EI)",
        note = "Maximum slope and deflection occur at the free tip."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Fixed-End Moments (FEM) Reference Table",
    ribbonLabel = "03 FEM",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Full UDL w & Center Point Load P",
        formulaOrStatement = "UDL w: FEM = ± wL² / 12    •    Center Load P: FEM = ± PL / 8",
        note = "CCW end moment at left support A, CW end moment at right support B."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Eccentric Load P (a from A, b from B)",
        formulaOrStatement = "FEM_AB = -P · a · b² / L²    •    FEM_BA = +P · a² · b / L²",
        note = "Quick memory aid: square the far-end distance (b² for left FEM_AB, a² for right FEM_BA)."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Triangular Load (0 to w) & Settlement Δ",
        formulaOrStatement = "Triangular: wL² / 30 (0 end), wL² / 20 (w end)    •    Settlement: ± 6EIΔ / L²",
        note = "For chord rotation ψ = Δ/L, settlement induces equal resisting moments at both fixed ends."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Virtual Work / Unit Load Method (Trusses & Beams)",
    ribbonLabel = "04 Virtual Work",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Truss Deflection (Loads, Temp, Fabrication)",
        formulaOrStatement = "Δ = Σ (n · N · L) / (A · E) + Σ (n · α · ΔT · L) + Σ (n · ΔL)",
        note = "n = virtual bar force under unit load; N = real bar force under actual loads."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Beam & Frame Deflection",
        formulaOrStatement = "Δ = ∫₀^L (m · M / EI) dx",
        note = "Use matching sign conventions for virtual moment m(x) and real moment M(x)."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Simpson's Rule / Visual Integration",
        formulaOrStatement = "∫ m · M dx = (L / 6) · (m₁M₁ + 4·m_m·M_m + m₂M₂)",
        note = "Exact for any product of linear and quadratic moment diagrams across a segment of length L."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Castigliano's Second Theorem & Strain Energy",
    ribbonLabel = "05 Castigliano",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Axial & Flexural Strain Energy",
        formulaOrStatement = "U_axial = Σ (P² · L) / (2 · A · E)    •    U_flex = ∫₀^L (M² / 2EI) dx",
        note = "Total elastic internal strain energy stored in linearly elastic members."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Displacement & Rotation Derivatives",
        formulaOrStatement = "Δ_i = ∂U / ∂P_i = ∫₀^L (M / EI)(∂M / ∂P_i) dx    •    θ_i = ∂U / ∂M_i",
        note = "Differentiate under the integral sign before plugging in the numerical value of dummy load P_i."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Three-Moment Equation (Clapeyron's Theorem)",
    ribbonLabel = "06 Three-Moment",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "General Three-Moment Equation (Constant EI)",
        formulaOrStatement = "M_A·L₁ + 2M_B(L₁ + L₂) + M_C·L₂ + 6A₁ā₁/L₁ + 6A₂b̄₂/L₂ = 6EI[(h_A - h_B)/L₁ + (h_C - h_B)/L₂]",
        note = "Solve redundant interior support moments M_B across consecutive spans L₁ and L₂."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Standard Load Terms (6Aā/L)",
        formulaOrStatement = "UDL w: wL³ / 4    •    Center P: 3PL² / 8    •    Eccentric P: Pa(L² - a²) / L",
        note = "Measure 'a' from the outer support (A for span 1, C for span 2)."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Slope-Deflection & Moment Distribution (Hardy Cross)",
    ribbonLabel = "07 Hardy Cross",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Slope-Deflection Equation",
        formulaOrStatement = "M_AB = (2EI / L) · (2θ_A + θ_B - 3Δ / L) + FEM_AB",
        note = "Clockwise end moments and clockwise chord rotations Δ/L are positive."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Member Stiffness Factors (K)",
        formulaOrStatement = "Far End Fixed: K = 4EI / L    •    Far End Pinned/Roller: K_mod = 3EI / L",
        note = "Use K_mod = 0.75(4EI/L) to eliminate carry-over iterations to exterior pin/roller supports."
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Distribution & Carry-Over Factors",
        formulaOrStatement = "DF_ij = K_ij / ΣK_i    •    COF = +½ (Fixed Far End)  |  0 (Pinned Far End)",
        note = "At any joint, ΣDF = 1.0 (use DF = 0 for a rigid fixed wall, DF = 1.0 for a free/pinned end)."
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Influence Lines & Moving Loads (Müller-Breslau)",
    ribbonLabel = "08 Influence",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Absolute Max Moment (Two Moving Loads P₁ ≥ P₂)",
        formulaOrStatement = "Place Resultant R & Heaviest Load P₁ equidistant from centerline: x = (P₂ · d) / (2 · R)",
        note = "Distance between R and P₁ is P₂·d/R; split equally across midspan so M_max occurs directly under P₁."
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Müller-Breslau Principle",
        formulaOrStatement = "IL Ordinate = Deflected shape from unit displacement of released restraint",
        note = "For statically determinate girders, influence lines are piecewise straight lines."
      )
    )
  )
)

internal fun buildHpgeSubVolumeSpecs(): List<EngineeringSubVolumeSpec> = listOf(
  EngineeringSubVolumeSpec(
    id = "hpge_hydraulics_fluid_mechanics",
    pillar = EngineeringVolumePillar.HPGE,
    title = "Hydraulics and Fluid Mechanics",
    shortTitle = "Hydraulics & Fluids",
    compactSummaryTag = "10 Chapters • CAD Labs & Jumps",
    tagBadge = "HPGE • 01",
    accentColor = Color(0xFF06B6D4),
    secondaryAccentColor = Color(0xFF0284C7),
    searchKeywords = "hydraulics fluid mechanics viscosity capillarity manometer hydrostatic pressure gate buoyancy metacenter relative equilibrium bernoulli venturi pitot orifice weir darcy hazen manning water hammer open channel hydraulic jump impulse momentum",
    sections = buildHydraulicsSections()
  ),
  EngineeringSubVolumeSpec(
    id = "hpge_geotechnical_soil_mechanics",
    pillar = EngineeringVolumePillar.HPGE,
    title = "Geotechnical (Soil Mechanics)",
    shortTitle = "Geotechnical",
    compactSummaryTag = "9 Chapters • Soil Block & Shear Lab",
    tagBadge = "HPGE • 02",
    accentColor = Color(0xFFD97706),
    secondaryAccentColor = Color(0xFFB45309),
    searchKeywords = "geotechnical soil mechanics phase relations void ratio porosity saturation atterberg limits gradation uscs aashto compaction proctor zav permeability darcy flow net effective stress boussinesq consolidation terzaghi mohr coulomb triaxial",
    sections = buildGeotechnicalSections()
  ),
  EngineeringSubVolumeSpec(
    id = "hpge_hydrology",
    pillar = EngineeringVolumePillar.HPGE,
    title = "Hydrology",
    shortTitle = "Hydrology",
    compactSummaryTag = "9 Chapters • Watershed & Well Lab",
    tagBadge = "HPGE • 03",
    accentColor = Color(0xFF3B82F6),
    secondaryAccentColor = Color(0xFF2563EB),
    searchKeywords = "hydrology hydrologic cycle water budget catchment morphometry drainage density form factor bifurcation precipitation missing data normal ratio arithmetic mean thiessen polygon isohyetal double mass curve optimum gauges evaporation pan meyer blaney criddle reservoir infiltration horton phi index w index streamflow current meter dilution stage discharge rational method kirpich time of concentration idf hydrograph unit hydrograph s curve snyder flood routing muskingum return period weibull risk reliability gumbel frequency groundwater well thiem dupuit",
    sections = buildHydrologySections()
  )
)

private fun buildHydraulicsSections(): List<AlgebraHandbookSection> = listOf(
  AlgebraHandbookSection(
    sectionTitle = "Fluid Properties, Viscosity & Capillarity",
    ribbonLabel = "01 Properties",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Density, Specific Weight & Specific Gravity",
        formulaOrStatement = "ρ = M / V    •    γ = ρ · g = W / V    •    SG = ρ_fluid / ρ_w = γ_fluid / γ_w",
        note = "Standard water values: ρ_w = 1000 kg/m³, γ_w = 9.81 kN/m³ = 62.4 lb/ft³.",
        subItems = listOf("SG_mercury ≈ 13.6", "SG_seawater ≈ 1.025–1.03")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Dynamic & Kinematic Viscosity",
        formulaOrStatement = "τ = μ · (dv / dy)    •    ν = μ / ρ",
        note = "CGS conversions: 1 Poise = 0.1 Pa·s = 0.1 N·s/m²; 1 Stoke = 10⁻⁴ m²/s = 1 cm²/s.",
        subItems = listOf("Newton's law of viscosity relates shear stress τ to velocity gradient dv/dy")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Bulk Modulus, Compressibility & Celerity",
        formulaOrStatement = "E_v = -ΔP / (ΔV / V) = ΔP / (Δρ / ρ)    •    β = 1 / E_v    •    c = √(E_v / ρ)",
        note = "For water at 20°C: E_v ≈ 2.2 GPa, giving acoustic wave speed c ≈ 1480 m/s.",
        subItems = listOf("Isothermal gas bulk modulus: E_v = P  •  Adiabatic gas: E_v = k · P")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Surface Tension & Capillary Action",
        formulaOrStatement = "Tube: h = (4 σ cos θ) / (γ d)  •  Droplet: ΔP = 4σ/d  •  Soap Bubble: ΔP = 8σ/d",
        note = "Water-glass contact angle θ ≈ 0° (rise); mercury-glass θ ≈ 140° (depression since cos θ < 0).",
        subItems = listOf("Between two parallel plates spaced by t: h = (2 σ cos θ) / (γ t)")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Hydrostatic Pressure & Manometry",
    ribbonLabel = "02 Manometry",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Pressure-Depth & Absolute Pressure",
        formulaOrStatement = "P = γ · h    •    P_abs = P_gage + P_atm",
        note = "Standard 1 atm = 101.325 kPa = 14.7 psi = 760 mmHg = 10.33 m H₂O = 33.9 ft H₂O.",
        subItems = listOf("Vacuum pressure P_vac = P_atm - P_abs (negative gage pressure)")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Equivalent Pressure Head Conversion",
        formulaOrStatement = "h₂ = h₁ · (SG₁ / SG₂) = h₁ · (γ₁ / γ₂)",
        note = "Converts column height of fluid 1 into an equivalent static head of fluid 2.",
        subItems = listOf("1 m H₂O = 73.5 mmHg (SG = 13.6)")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "U-Tube Differential Manometer Balance",
        formulaOrStatement = "P_A + γ₁ h₁ - γ₂ h₂ - γ₃ h₃ = P_B",
        note = "Jump-across rule: add γ·h when moving downward; subtract γ·h when moving upward.",
        subItems = listOf("Use equal-elevation horizontal datum across identical continuous fluid")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Hydrostatic Forces on Plane & Curved Surfaces",
    ribbonLabel = "03 Forces",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Total Force & Center of Pressure (Plane)",
        formulaOrStatement = "F = γ · h̄ · A    •    e = I_g / (A · ȳ)    •    y_p = ȳ + e",
        note = "ȳ and y_p are measured along the inclined plane from the water surface intersection; h̄ = ȳ sin θ.",
        subItems = listOf("Rectangle (width b, slant h): I_g = b h³ / 12", "Circle (dia D): I_g = π D⁴ / 64")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Hydrostatic Force on Curved Surfaces",
        formulaOrStatement = "F_H = γ · h̄ · A_proj    •    F_V = γ · V_above/displaced    •    F_R = √(F_H² + F_V²)",
        note = "F_V acts through the centroid of the real or imaginary liquid volume extending up to the free surface.",
        subItems = listOf("Resultant angle from horizontal: tan α = F_V / F_H  (line of action passes through arc center)")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Buoyancy & Stability of Floating Bodies",
    ribbonLabel = "04 Buoyancy",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Archimedes Buoyant Force",
        formulaOrStatement = "BF = γ_fluid · V_submerged    (Equilibrium Floating: BF = W_body)",
        note = "Fraction of uniform floating body submerged equals ρ_body / ρ_fluid (e.g., ice in seawater ≈ 89%).",
        subItems = listOf("Two-layer fluid: BF = γ₁ V₁ + γ₂ V₂")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Metacentric Height (MG) & Radius (MBo)",
        formulaOrStatement = "MG = MB_o ± GB_o    •    MB_o = I_waterline / V_submerged",
        note = "Use (+) if C.G. (G) is below Buoyancy (B_o); use (-) if G is above B_o. Stable when MG > 0 (M above G).",
        subItems = listOf("Rectangular barge (width B, draft D): MB_o = B² / (12 D)")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Righting / Overturning Moment",
        formulaOrStatement = "RM = W · MG · sin θ",
        note = "Positive MG restores the tilted vessel upright; negative MG creates an overturning couple.",
        subItems = listOf("For small angles θ ≤ 10°: M remains virtually stationary on centerline")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Relative Equilibrium of Liquids",
    ribbonLabel = "05 Tank Motion",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Horizontal Rectilinear Acceleration (ax)",
        formulaOrStatement = "tan θ = a_x / g",
        note = "Free surface slopes downward in the direction of acceleration; volume lost at front = volume gained at back.",
        subItems = listOf("Surface rise/drop from rest centerline: Δh = (L / 2) · tan θ")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Inclined & Vertical Acceleration (ay)",
        formulaOrStatement = "Inclined: tan θ = a_h / (g ± a_v)    •    Vertical: P = γ h · (1 ± a_y / g)",
        note = "Use (+) when accelerating upward and (-) when accelerating downward (free fall a_y = g gives P = 0).",
        subItems = listOf("a_h = a cos α,  a_v = a sin α")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Rotating Cylindrical Vessel (ω)",
        formulaOrStatement = "y = (ω² · x²) / (2 g)    •    h_vortex = (ω² · R²) / (2 g)    •    V_air = (1/2) π R² h_vortex",
        note = "Original flat water level always lies halfway (h/2) between the vertex and the rim rise of an open unspilled tank.",
        subItems = listOf("Angular velocity conversion: ω = (2 π · RPM) / 60  rad/s")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Fluid Dynamics, Bernoulli & Flow Meters",
    ribbonLabel = "06 Bernoulli",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Continuity & Mass / Weight Flow Rate",
        formulaOrStatement = "Q = A₁ V₁ = A₂ V₂    •    ṁ = ρ · Q    •    Ẇ = γ · Q",
        note = "For steady incompressible conduit flow, velocity varies inversely with diameter squared (V₂/V₁ = (D₁/D₂)²).",
        subItems = listOf("Pipe area A = π D² / 4")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Bernoulli's Extended Energy Equation",
        formulaOrStatement = "P₁/γ + V₁²/(2g) + z₁ + H_A - H_E - H_L = P₂/γ + V₂²/(2g) + z₂",
        note = "H_A = head added by pump; H_E = head extracted by turbine; H_L = total friction & minor head losses.",
        subItems = listOf("Total Energy Grade Line (EGL) = P/γ + V²/2g + z", "HGL = P/γ + z")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Hydraulic Power & Machine Efficiency",
        formulaOrStatement = "P_water = γ · Q · H    •    η_pump = P_water / P_shaft    •    η_turbine = P_shaft / P_water",
        note = "With γ in kN/m³ and Q in m³/s, P_water = γ Q H yields power directly in kilowatts (kW).",
        subItems = listOf("1 Horsepower (hp) = 746 W = 0.746 kW = 550 ft·lb/s")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Venturi Meter, Orifice Meter & Pitot Tube",
        formulaOrStatement = "Venturi/Orifice: Q = C_d A₂ √[ 2g Δh / (1 - (A₂/A₁)²) ]  •  Pitot: V = C_v √(2g Δh)",
        note = "Standard differential mercury manometer reading R_m gives Δh = R_m (SG_gage / SG_flow - 1).",
        subItems = listOf("Typical C_d: Venturi ≈ 0.96–0.98  •  Orifice plate ≈ 0.60–0.62")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Orifices, Weirs & Unsteady Tank Emptying",
    ribbonLabel = "07 Weirs",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Orifice Flow & Hydraulic Coefficients",
        formulaOrStatement = "C_d = C_c · C_v    •    V_t = √(2gH)    •    Q = C_d A_o √(2gH)    •    H_L = H(1 - C_v²)",
        note = "Free jet parabolic trajectory: x = V_act · t and y = (1/2) g t², giving V_act = √(g x² / (2y)).",
        subItems = listOf("Vena contracta area A_jet = C_c · A_o")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Unsteady Tank Emptying (Constant Area As)",
        formulaOrStatement = "t = [ (2 · A_s) / (C_d · A_o · √(2g)) ] · (√H₁ - √H₂)",
        note = "Time to completely empty (H₂ = 0) equals twice the time needed if head remained constant at H₁.",
        subItems = listOf("For variable area A_s(h), integrate dt = -A_s(h) dh / (C_d A_o √(2gh))")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Standard Sharp-Crested Weirs (Francis & V-Notch)",
        formulaOrStatement = "Suppressed: Q = 1.84 L H^(3/2)  •  V-Notch: Q = (8/15) C_d √(2g) tan(θ/2) H^(5/2)",
        note = "Contracted weir reduces effective crest to L' = L - 0.1 n H (n = 1 or 2 contractions); Cipolletti (1H:4V) uses Q = 1.859 L H^(3/2).",
        subItems = listOf("USCS Francis Suppressed: Q = 3.33 L H^(3/2)", "90° V-Notch quick formula: Q ≈ 1.40 H^(5/2) (m³/s)")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Closed-Conduit Flow & Water Hammer",
    ribbonLabel = "08 Pipe Flow",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Reynolds Number & Flow Regimes",
        formulaOrStatement = "Re = (ρ · V · D) / μ = (V · D) / ν",
        note = "Laminar flow: Re < 2000; Critical/Transitional: 2000 ≤ Re ≤ 4000; Turbulent: Re > 4000.",
        subItems = listOf("Kinematic viscosity of water at 20°C: ν ≈ 1.0 × 10⁻⁶ m²/s")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Darcy-Weisbach Friction Head Loss",
        formulaOrStatement = "h_f = f · (L / D) · (V² / 2g) = (0.0826 · f · L · Q²) / D⁵",
        note = "Laminar friction factor f = 64 / Re; turbulent f depends on Re and relative roughness ε / D (Moody Chart).",
        subItems = listOf("Laminar Hagen-Poiseuille loss: h_f = 32 μ L V / (γ D²)")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Hazen-Williams & Manning Pipe Formulas",
        formulaOrStatement = "H-W: V = 0.849 C R^0.63 S^0.54, h_f = 10.67 L Q^1.852 / (C^1.852 D^4.87)",
        note = "Manning's pipe loss equation (SI): h_f = (10.29 · n² · L · Q²) / D^(16/3).",
        subItems = listOf("For full circular pipe, hydraulic radius R = D / 4")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Pipes in Series, Parallel & Three-Reservoir",
        formulaOrStatement = "Series: Q_T = Q₁ = Q₂,  H_LT = h_f1 + h_f2   •   Parallel: Q_T = Q₁ + Q₂,  h_f1 = h_f2",
        note = "In parallel pipe networks, head loss between common junctions is identical along every branch.",
        subItems = listOf("Hardy Cross loop correction: ΔQ = -Σ h_f / (n · Σ |h_f / Q|)")
      ),
      AlgebraHandbookRuleItem(
        number = 5,
        title = "Water Hammer Celerity & Surge Pressure",
        formulaOrStatement = "c = √[ (E_v / ρ) / (1 + (E_v D)/(E_pipe t)) ]  •  T_c = 2L/c  •  ΔP_max = ρ c V",
        note = "Rapid valve closure (t_c ≤ T_c = 2L/c) develops full Joukowsky pressure surge ΔP_max = ρ c V.",
        subItems = listOf("Slow closure (t_c > T_c): ΔP ≈ (2 L ρ V) / t_c")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Open Channel Flow & Hydraulic Jump",
    ribbonLabel = "09 Open Chan",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Manning's & Chezy's Uniform Flow",
        formulaOrStatement = "V = (1 / n) · R^(2/3) · S^(1/2)    •    V = C · √(R · S)    •    R = A / P",
        note = "Chezy-Manning relation: C = (1 / n) R^(1/6); flow rate Q = A · V.",
        subItems = listOf("Pipe flowing half-full or full: R = D / 4")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Most Efficient (Economical) Channel Sections",
        formulaOrStatement = "Rect: b = 2y, R = y/2  •  Trap: Half-Hex (θ = 60°), R = y/2  •  Tri: 90°, R = y/(2√2)",
        note = "Minimizes wetted perimeter P for a fixed cross-sectional area A (maximum discharge for given slope and n).",
        subItems = listOf("Trapezoid efficiency rule: Top width T = 2 × Sloped Side Length")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Specific Energy, Froude No. & Critical Depth",
        formulaOrStatement = "E = y + V²/(2g)  •  Fr = V / √(g D_h)  •  Rect: y_c = ³√(q² / g), E_min = 1.5 y_c",
        note = "Hydraulic depth D_h = A / T. Flow is Subcritical (Fr < 1), Critical (Fr = 1 ⇒ Q²T / gA³ = 1), or Supercritical (Fr > 1).",
        subItems = listOf("Unit discharge in rectangular channel: q = Q / b")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Hydraulic Jump (Rectangular Channel)",
        formulaOrStatement = "y₂ / y₁ = (1 / 2) · [ √(1 + 8 Fr₁²) - 1 ]    •    ΔE = (y₂ - y₁)³ / (4 y₁ y₂)",
        note = "Always jumps from upstream supercritical depth (y₁ < y_c, Fr₁ > 1) to downstream subcritical depth (y₂ > y_c).",
        subItems = listOf("Power dissipated in jump: P_loss = γ · Q · ΔE")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Hydrodynamics (Jet Vanes & Pipe Bends)",
    ribbonLabel = "10 Momentum",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Impulse-Momentum Force Principle",
        formulaOrStatement = "Σ F_x = ρ · Q · (V_2x - V_1x)    •    Σ F_y = ρ · Q · (V_2y - V_1y)",
        note = "Force exerted BY the fluid ON a vane/bend is equal and opposite to the reaction force R on the fluid.",
        subItems = listOf("Consistent sign convention required for exit vs entrance velocity components")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Jet Force on Flat & Curved Vanes",
        formulaOrStatement = "Normal Plate: F = ρ Q V  •  Inclined: F_n = ρ Q V sin θ  •  Curved: F_x = ρ Q V (1 - cos θ)",
        note = "For a stationary curved deflector vane turning jet by angle θ: F_y = ρ Q V sin θ.",
        subItems = listOf("Moving vane at speed u: replace Q with Q_rel = A(V - u) and V with V_rel = V - u")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Anchor Force on Reducing Pipe Bend",
        formulaOrStatement = "P₁ A₁ - P₂ A₂ cos θ - R_x = ρ Q (V₂ cos θ - V₁)",
        note = "Include pressure thrust terms P₁A₁ and P₂A₂ in addition to momentum change; total force R = √(R_x² + R_y²).",
        subItems = listOf("Vertical component: P₂ A₂ sin θ + W_water - R_y = -ρ Q V₂ sin θ (or adjust signs per bend orientation)")
      )
    )
  )
)

private fun buildGeotechnicalSections(): List<AlgebraHandbookSection> = listOf(
  AlgebraHandbookSection(
    sectionTitle = "Soil Phase Relationships (Weight-Volume)",
    ribbonLabel = "01 Phases",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Fundamental Void, Porosity & Moisture Definitions",
        formulaOrStatement = "e = V_v / V_s  •  n = V_v / V = e/(1 + e)  •  S = V_w / V_v  •  w = W_w / W_s",
        note = "Specific gravity of soil solids G_s = γ_s / γ_w (typical mineral grains ≈ 2.65 to 2.75); e = n / (1 - n).",
        subItems = listOf("Degree of saturation S = 0 (oven-dry) to 1.0 or 100% (fully saturated)")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Master Phase Identity",
        formulaOrStatement = "S · e = w · G_s",
        note = "Most frequently tested soil mechanics bridge linking water content w, void ratio e, saturation S, and G_s.",
        subItems = listOf("If saturated (S = 1): e = w · G_s")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Moist, Dry, Saturated & Submerged Unit Weights",
        formulaOrStatement = "γ = [(G_s + S e) / (1 + e)] γ_w   •   γ_d = G_s γ_w / (1 + e) = γ / (1 + w)",
        note = "Saturated (S = 1): γ_sat = [(G_s + e)/(1 + e)] γ_w; Buoyant/Submerged: γ' = γ_sat - γ_w = [(G_s - 1)/(1 + e)] γ_w.",
        subItems = listOf("Moist alternative: γ = [G_s(1 + w) / (1 + e)] γ_w")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Relative Density (Granular Soils)",
        formulaOrStatement = "D_r = (e_max - e)/(e_max - e_min) = [ (γ_d - γ_d,min)/(γ_d,max - γ_d,min) ] · (γ_d,max / γ_d)",
        note = "Loose sand: D_r < 35%; Medium: 35%–65%; Dense: 65%–85%; Very Dense: > 85%.",
        subItems = listOf("Express D_r as a percentage (× 100%)")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Atterberg Limits & Grain-Size Gradation",
    ribbonLabel = "02 Limits",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Plasticity, Liquidity & Consistency Indices",
        formulaOrStatement = "PI = LL - PL    •    LI = (w - PL) / PI    •    CI = (LL - w) / PI",
        note = "If LI > 1, soil is above Liquid Limit (viscous fluid when remolded); Activity A = PI / (% finer than 2 μm).",
        subItems = listOf("Shrinkage Ratio: SR = M_s / (V_f · ρ_w)  •  Check LI + CI = 1.0")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Gradation Coefficients (Cu & Cc)",
        formulaOrStatement = "C_u = D₆₀ / D₁₀    •    C_c = D₃₀² / (D₆₀ · D₁₀)",
        note = "Well-graded Gravel (GW): C_u ≥ 4 and 1 ≤ C_c ≤ 3. Well-graded Sand (SW): C_u ≥ 6 and 1 ≤ C_c ≤ 3.",
        subItems = listOf("D₁₀ = effective particle size corresponding to 10% finer by weight")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Soil Classification (USCS & AASHTO)",
    ribbonLabel = "03 Classif.",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "AASHTO Group Index (GI)",
        formulaOrStatement = "GI = (F₂₀₀ - 35)[0.2 + 0.005(LL - 40)] + 0.01(F₂₀₀ - 15)(PI - 10)",
        note = "Round GI to the nearest whole integer; if calculated value is negative, report GI = 0. Coarse granular limit: F₂₀₀ ≤ 35%.",
        subItems = listOf("For A-2-6 and A-2-7 soils, use only the second term: GI = 0.01(F₂₀₀ - 15)(PI - 10)")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "USCS Casagrande Plasticity Chart (A-Line)",
        formulaOrStatement = "PI = 0.73 · (LL - 20)",
        note = "Above A-Line = Clay (CL if LL < 50, CH if LL ≥ 50); Below A-Line = Silt (ML / MH) or Organic (OL / OH).",
        subItems = listOf("Cross-hatched dual zone: 4 ≤ PI ≤ 7 and plots above A-Line ⇒ CL-ML")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Soil Compaction (Proctor & Field Control)",
    ribbonLabel = "04 Compaction",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Zero-Air-Void (ZAV) Dry Unit Weight",
        formulaOrStatement = "γ_zav = (G_s · γ_w) / (1 + w · G_s)",
        note = "Represents the theoretical 100% saturation (S = 1.0) boundary curve; actual compaction curves always stay below ZAV.",
        subItems = listOf("Any saturation line S: γ_d = (G_s · γ_w) / (1 + w · G_s / S)")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Relative Compaction & Field Control",
        formulaOrStatement = "R (%) = [ γ_d(field) / γ_d(max-lab) ] × 100%",
        note = "Field dry density verified via Sand Cone (ASTM D1556), Balloon, or Nuclear Densometer; typical spec R ≥ 95%.",
        subItems = listOf("Field dry unit weight from moist test: γ_d(field) = γ_field / (1 + w)")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Permeability, Seepage & Flow Nets",
    ribbonLabel = "05 Seepage",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Darcy's Law & True Seepage Velocity",
        formulaOrStatement = "v = k · i    •    Q = k · i · A    •    v_s = v / n",
        note = "Discharge velocity v treats total gross area A as flow path; actual pore water moves faster at seepage velocity v_s = v / n.",
        subItems = listOf("Hydraulic gradient i = Δh / L")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Constant Head & Falling Head Permeability",
        formulaOrStatement = "Const: k = (Q L) / (A h t)  •  Falling: k = (a L / A t) ln(h₁/h₂) = 2.303(a L/A t) log₁₀(h₁/h₂)",
        note = "Constant head suits coarse sands/gravels; falling head standpipe suits fine-grained silts and clays.",
        subItems = listOf("a = cross-sectional area of standpipe, A = area of soil specimen")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Stratified Soil Equivalent Permeability",
        formulaOrStatement = "k_H(eq) = Σ(k_i H_i) / Σ H_i    •    k_V(eq) = Σ H_i / Σ(H_i / k_i)",
        note = "Horizontal flow (parallel to strata) is governed by the most permeable layer; vertical flow by the least permeable layer.",
        subItems = listOf("Always satisfies k_H(eq) ≥ k_V(eq)")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Flow Nets & Critical Hydraulic Gradient",
        formulaOrStatement = "q = k H (N_f / N_d)  •  u_w = h_p γ_w  •  i_cr = γ'/γ_w = (G_s - 1)/(1 + e)",
        note = "Quicksands / boiling safety factor against heave: FS_heave = i_cr / i_exit (typically i_cr ≈ 1.0).",
        subItems = listOf("N_f = number of flow channels", "N_d = number of equipotential drops")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Effective Stress & Capillary Rise",
    ribbonLabel = "06 Eff Stress",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Terzaghi's Effective Stress Principle",
        formulaOrStatement = "σ' = σ - u",
        note = "Total stress σ = Σ γ_i z_i; pore water pressure u = γ_w h_w below groundwater table.",
        subItems = listOf("Shear strength and volume compressibility depend solely on effective stress σ'")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Negative Pore Pressure in Capillary Zone",
        formulaOrStatement = "u = -S · γ_w · z_c    ⇒    σ' = σ + S · γ_w · z_c",
        note = "Fully saturated capillary fringe (S = 1) above GWT experiences negative pore suction u = -γ_w z, increasing σ'.",
        subItems = listOf("Approx capillary rise in soil: h_c ≈ C / (e · D₁₀)")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Effective Stress Under Vertical Seepage",
        formulaOrStatement = "σ' = z · γ' ∓ i · z · γ_w   (- Upward Seepage, + Downward Seepage)",
        note = "Upward seepage reduces effective stress; boiling occurs when i · z · γ_w = z · γ' (i = i_cr = γ'/γ_w).",
        subItems = listOf("Seepage force per unit volume: j = i · γ_w")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Vertical Stress Increase (Boussinesq)",
    ribbonLabel = "07 Boussinesq",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Point Load Q (Boussinesq Equation)",
        formulaOrStatement = "Δσ_z = (3 Q) / [ 2 π z² · (1 + (r/z)²)^(5/2) ] = (Q / z²) · I_B",
        note = "Maximum stress directly under point load (r = 0): Δσ_z = 0.4775 · Q / z².",
        subItems = listOf("Line load q per unit length: Δσ_z = (2 q z³) / [ π (x² + z²)² ]")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Uniformly Loaded Circular Area (Center)",
        formulaOrStatement = "Δσ_z = q_o · [ 1 - 1 / (1 + (R / z)²)^(3/2) ]",
        note = "Applies directly below the center of a flexible circular footing of radius R carrying contact stress q_o.",
        subItems = listOf("Also remember 2:1 stress method: Δσ_z = P / [(B + z)(L + z)]")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Newmark's Influence Chart",
        formulaOrStatement = "Δσ_z = (IV) · q_o · M",
        note = "IV = influence value per sector (commonly 0.005 for 200 sectors); M = number of enclosed elements on scaled plan.",
        subItems = listOf("Depth z equals the reference scale bar length AB on the chart")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Consolidation Theory & Time Rate",
    ribbonLabel = "08 Time Rate",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Compression Index Cc & OCR",
        formulaOrStatement = "C_c = (e₁ - e₂) / log₁₀(σ'₂ / σ'₁)  •  Empirical: C_c = 0.009(LL - 10)  •  OCR = σ'_c / σ'_o",
        note = "OCR ≈ 1 indicates Normally Consolidated clay; OCR > 1 indicates Overconsolidated clay.",
        subItems = listOf("Swell index C_s ≈ (1/5 to 1/10) C_c")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Volume Compressibility mv & Permeability k",
        formulaOrStatement = "m_v = Δe / [ Δσ' · (1 + e_o) ]    •    k = c_v · m_v · γ_w",
        note = "Links coefficient of consolidation c_v from oedometer time curve directly to vertical clay permeability k.",
        subItems = listOf("Primary strain ε_v = S_c / H = m_v · Δσ'")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Terzaghi Time Factor Tv & Degree U%",
        formulaOrStatement = "T_v = (c_v · t) / H_dr²   (H_dr = H/2 Double, H Single)",
        note = "For U ≤ 60%: T_v = (π / 4)(U% / 100)²  •  For U > 60%: T_v = 1.781 - 0.933 log₁₀(100 - U%).",
        subItems = listOf("Key CELE landmarks: T₅₀ = 0.197 (U=50%)  •  T₉₀ = 0.848 (U=90%)")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Shear Strength (Mohr-Coulomb & Triaxial)",
    ribbonLabel = "09 Shear",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Mohr-Coulomb Failure Criterion",
        formulaOrStatement = "τ_f = c' + σ' · tan(φ')",
        note = "c' = effective cohesion intercept; φ' = effective internal friction angle.",
        subItems = listOf("Clean dry sand has c' = 0; saturated undrained clay (UU) has φ_u = 0")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Principal Stresses at Failure (Triaxial)",
        formulaOrStatement = "σ'₁ = σ'₃ · tan²(45° + φ'/2) + 2 c' · tan(45° + φ'/2)  •  θ = 45° + φ'/2",
        note = "Deviator stress at failure Δσ_d = σ₁ - σ₃; failure plane makes angle θ = 45° + φ'/2 with major principal plane.",
        subItems = listOf("Angle of shear stress pole to failure pole: 2θ = 90° + φ'")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Unconfined Compression Test (σ₃ = 0)",
        formulaOrStatement = "c_u = s_u = q_u / 2",
        note = "Special UU triaxial case with zero cell confining pressure (σ₃ = 0, σ₁ = q_u).",
        subItems = listOf("Sensitivity of clay: S_t = q_u(undisturbed) / q_u(remolded)")
      )
    )
  )
)

private fun buildHydrologySections(): List<AlgebraHandbookSection> = listOf(
  AlgebraHandbookSection(
    sectionTitle = "Hydrologic Cycle, Water Budget & Catchment Morphometry",
    ribbonLabel = "01 Cycle & Basin",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Catchment Water Balance Equation",
        formulaOrStatement = "ΔS = P - R - G - E - T    •    P = Q + ET + ΔS",
        note = "P = precipitation, R = surface runoff, G = groundwater outflow, E = evaporation, T = transpiration, ΔS = storage change.",
        subItems = listOf("Steady long-term annual budget (ΔS ≈ 0): P = Q + ET")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Runoff Coefficient (C)",
        formulaOrStatement = "C = Runoff Depth (R) / Precipitation Depth (P)",
        note = "Dimensionless ratio (0 ≤ C ≤ 1) representing fraction of total storm rainfall appearing as surface runoff.",
        subItems = listOf("Losses = P - R = (1 - C) · P")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Drainage Density, Form Factor & Bifurcation Ratio",
        formulaOrStatement = "D_d = ΣL_streams / A_basin    •    F_f = A / L_b²    •    R_b = N_u / N_(u+1)",
        note = "ΣL_streams = total length of all streams in basin; L_b = axial basin length; N_u = streams of order u.",
        subItems = listOf("High D_d indicates steep impermeable terrain with rapid peak runoff")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Precipitation, Missing Data & Mean Areal Rainfall",
    ribbonLabel = "02 Precipitation",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Estimation of Missing Rainfall Data (P_x)",
        formulaOrStatement = "Arithmetic (≤10%): P_x = (1/m) ΣP_i    •    Normal Ratio (>10%): P_x = (N_x / m) · Σ(P_i / N_i)",
        note = "Use Arithmetic Mean when normal annual precipitations N_i of index stations are within 10% of N_x; otherwise use Normal Ratio.",
        subItems = listOf("Normal Ratio check: |N_i - N_x| / N_x > 0.10 for any station i = 1..m")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Mean Areal Precipitation (P̄)",
        formulaOrStatement = "Thiessen: P̄ = Σ(P_i · A_i) / ΣA_i = Σw_i P_i    •    Isohyetal: P̄ = Σ[A_i · (P_i + P_(i+1)) / 2] / ΣA_i",
        note = "Arithmetic Mean P̄ = ΣP_i / n assumes uniform flat terrain; Isohyetal is most accurate for orographic storms.",
        subItems = listOf("Thiessen weight w_i = A_i / ΣA_i where Σw_i = 1.0")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Double-Mass Curve Consistency Adjustment",
        formulaOrStatement = "P_cx = P_x · (M_c / M_a)",
        note = "Adjusts older inconsistent gauge records (slope M_a) to current regime slope M_c after a break in cumulative slope.",
        subItems = listOf("M_c = slope of corrected (recent) regime; M_a = slope of original regime")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Optimum Number of Rain Gauges (N)",
        formulaOrStatement = "N = (C_v / ε)²    where    C_v = 100 · σ_(n-1) / P̄",
        note = "ε = allowable percentage error (commonly 10%); add (N - n) new stations if existing count n < N.",
        subItems = listOf("Sample std. dev.: σ_(n-1) = √[ Σ(P_i - P̄)² / (n - 1) ]")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Evaporation, Evapotranspiration & Reservoir Mass Balance",
    ribbonLabel = "03 Evaporation",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Pan & Lake Evaporation",
        formulaOrStatement = "E_lake = K_p · E_pan    (Class A Pan: K_p ≈ 0.70, Range 0.60–0.80)",
        note = "Sunken Colorado pan K_p ≈ 0.78; Class A pan overestimates open-water evaporation due to metal wall heat gain.",
        subItems = listOf("Volume lost = E_lake · A_surface")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Meyer's Empirical Evaporation Formula",
        formulaOrStatement = "E = K_m · (e_w - e_a) · (1 + u_9 / 16)",
        note = "E in mm/day; e_w, e_a = saturation & actual vapor pressures (mmHg); u_9 = monthly mean wind speed at 9 m height (km/hr).",
        subItems = listOf("K_m = 0.36 for deep large water bodies; K_m = 0.50 for shallow small ponds")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Blaney-Criddle Consumptive Use (ET)",
        formulaOrStatement = "U = Σ(k · f)    where    f = p · (0.46 · T_c + 8.13) / 100  [SI]",
        note = "T_c = mean monthly temp (°C); p = monthly % of annual daytime hours; k = crop coefficient.",
        subItems = listOf("USCS equivalent: f = (p · T_f) / 100 where T_f is in °F and U is in inches")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Reservoir Storage & Evaporation Loss",
        formulaOrStatement = "ΔS = (I + P_lake) - (O + E_lake + Seepage)",
        note = "All terms converted to consistent volume units (m³ or ha·m) or depth over mean reservoir surface area.",
        subItems = listOf("Pan-adjusted lake loss reduces net usable reservoir yield")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Infiltration (Horton's Equation, Φ-Index & W-Index)",
    ribbonLabel = "04 Infiltration",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Horton's Infiltration Capacity Curve",
        formulaOrStatement = "f_t = f_c + (f₀ - f_c) · e^(-k·t)",
        note = "f₀ = initial infiltration capacity (mm/hr); f_c = ultimate constant capacity; k = exponential decay constant (hr⁻¹).",
        subItems = listOf("Applies when rainfall intensity i ≥ f_t continuously (ponding condition)")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Cumulative Infiltration Depth (F_t)",
        formulaOrStatement = "F_t = ∫₀ᵗ f_t dt = f_c · t + [ (f₀ - f_c) / k ] · (1 - e^(-k·t))",
        note = "Total depth of water infiltrated from t = 0 to time t in mm or cm.",
        subItems = listOf("As t → ∞, excess transient infiltration approaches (f₀ - f_c)/k")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Φ-Index (Excess Rainfall Constant Loss Rate)",
        formulaOrStatement = "Φ = (P_eff_period - R_direct) / t_e",
        note = "Constant average infiltration rate above which rainfall becomes direct runoff; include ONLY intervals where intensity i > Φ.",
        subItems = listOf("Iterate if trial Φ exceeds any pulse intensity i_k: drop i_k and recompute t_e")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "W-Index (Average Infiltration Rate Over Entire Storm)",
        formulaOrStatement = "W = (P - R - I_a) / t_r",
        note = "Average loss rate over total storm duration t_r including initial abstraction I_a (interception + depression storage); W ≤ Φ.",
        subItems = listOf("If I_a = 0 and all pulses satisfy i > Φ, then W = Φ")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Streamflow Measurement & Stage-Discharge Rating Curve",
    ribbonLabel = "05 Streamflow",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Current Meter Velocity Equation",
        formulaOrStatement = "V = a · N_s + b",
        note = "V = point flow velocity (m/s); N_s = meter revolutions per second; a, b = factory rating calibration constants.",
        subItems = listOf("Revolutions per second: N_s = N_rev / time_sec")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Depth-Averaged Vertical Velocity",
        formulaOrStatement = "Shallow (y ≤ 0.6 m): V̄ = V_0.6d    •    Deep (y > 0.6 m): V̄ = (V_0.2d + V_0.8d) / 2",
        note = "Single-point measurement at 0.6 depth from water surface; two-point average at 0.2d and 0.8d for deeper streams.",
        subItems = listOf("Surface float correction factor: V̄ ≈ 0.85 · V_surface")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Mid-Section Discharge Integration",
        formulaOrStatement = "Q = Σ [ V̄_i · y_i · (w_(i-1) + w_i) / 2 ]",
        note = "Integrates stream discharge across n vertical panels with adjacent panel widths w_(i-1) and w_i.",
        subItems = listOf("End triangular edge panels use width correction Δw/2 with v_bank = 0")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Chemical / Salt Dilution Method",
        formulaOrStatement = "Continuous: Q = q · (C₁ - C₂) / (C₂ - C₀)    •    Gulp: Q = (V₁ · C₁) / ∫(C₂ - C₀) dt",
        note = "q = tracer injection rate; C₁ = injected tracer conc.; C₂ = downstream equilibrium conc.; C₀ = background stream conc.",
        subItems = listOf("Ideal for turbulent mountain streams where current meters cannot operate")
      ),
      AlgebraHandbookRuleItem(
        number = 5,
        title = "Stage-Discharge Rating Curve",
        formulaOrStatement = "Q = C_r · (G - a)ⁿ",
        note = "G = gauge height (stage); a = stage of zero flow (Q = 0); C_r, n = rating constants calibrated via log-log regression.",
        subItems = listOf("Linearized log form: log₁₀ Q = log₁₀ C_r + n · log₁₀(G - a)")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Peak Runoff Estimation (Rational Method & Time of Concentration)",
    ribbonLabel = "06 Peak Runoff",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Rational Formula (Urban & Small Catchments)",
        formulaOrStatement = "Q_p = (C · i · A) / 3.6  [km²]    •    Q_p = 0.00278 · C · i · A  [ha]    •    Q_p = C · i · A  [USCS]",
        note = "SI [km²]: Q_p in m³/s, i in mm/hr, A in km² (or ÷ 360 if A in ha). USCS: Q_p in cfs, i in in/hr, A in acres.",
        subItems = listOf("Valid for small catchments typically A ≤ 50 km² where storm duration ≥ t_c")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Composite Runoff Coefficient",
        formulaOrStatement = "C_eq = Σ(C_i · A_i) / ΣA_i",
        note = "Area-weighted equivalent runoff coefficient across roofs, pavements, lawns, and permeable sub-areas.",
        subItems = listOf("Typical values: Concrete/Asphalt 0.70–0.95 • Parks/Lawns 0.10–0.25")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Kirpich's Time of Concentration (t_c)",
        formulaOrStatement = "t_c = 0.01947 · L^0.77 · S^(-0.385)    [t_c in min, L in m, S = ΔH / L]",
        note = "Time for runoff from hydraulically remotest point of catchment to reach the outlet; use i at duration = t_c.",
        subItems = listOf("USCS form: t_c = 0.0078 · L_ft^0.77 · S^(-0.385) min")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Intensity-Duration-Frequency (IDF) Curve",
        formulaOrStatement = "i = (K · T^x) / (t_c + b)ⁿ",
        note = "Relates critical rainfall intensity i (mm/hr) to return period T (years) and storm duration d = t_c (min or hr).",
        subItems = listOf("Shorter duration storms produce higher peak intensity i for the same T")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Hydrograph Analysis, Unit Hydrograph & S-Curve",
    ribbonLabel = "07 Hydrographs",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Direct Runoff Depth (R_d)",
        formulaOrStatement = "R_d = V_DRH / A = (0.36 · Σ(Q_d · Δt)) / A_km²    [cm]",
        note = "V_DRH = ∫Q_d dt (total area under direct runoff hydrograph); Δt in hours, Q_d in m³/s, A in km² gives R_d in cm.",
        subItems = listOf("In mm: R_d(mm) = (3.6 · Σ(Q_d · Δt)) / A_km²")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Unit Hydrograph (UH) Ordinates",
        formulaOrStatement = "U_i = (Q_total,i - Q_base,i) / R_d",
        note = "Normalizes direct runoff ordinates Q_d,i by R_d (cm) to yield the D-hr Unit Hydrograph for 1 cm excess rainfall.",
        subItems = listOf("Superposition: Q_d(t) = Σ ER_k · U(t - (k-1)D)")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Baseflow Separation (Empirical Time N)",
        formulaOrStatement = "N = 0.83 · A^0.2    [N in days, A in km²]",
        note = "Empirical time after storm peak discharge Q_p where direct surface runoff ends and groundwater recession resumes.",
        subItems = listOf("USCS form: N = A_mi²^0.2 days")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "S-Curve Method (Duration D₁ → D₂)",
        formulaOrStatement = "U_D2(t) = (D₁ / D₂) · [ S(t) - S(t - D₂) ]",
        note = "Lag cumulative S-curve by desired new duration D₂, subtract, and scale by ratio D₁/D₂ to keep 1 cm volume.",
        subItems = listOf("Equilibrium discharge: Q_eq = 2.778 · (A_km² / D₁_hr) m³/s")
      ),
      AlgebraHandbookRuleItem(
        number = 5,
        title = "Snyder's Synthetic Unit Hydrograph",
        formulaOrStatement = "t_p = C_t · (L · L_c)^0.3    •    Q_p = (2.78 · C_p · A) / t_p    [SI]",
        note = "t_p = basin lag (hr); L = main stream length (km); L_c = distance along stream to centroid (km); A in km².",
        subItems = listOf("Standard storm duration t_r = t_p / 5.5; if t_R ≠ t_r then t_p' = t_p + 0.25(t_R - t_r)")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Flood Routing & Hydrologic Frequency Analysis",
    ribbonLabel = "08 Flood Routing",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Muskingum Channel Routing",
        formulaOrStatement = "S = K · [ x·I + (1 - x)·O ]    •    O₂ = C₀·I₂ + C₁·I₁ + C₂·O₁    (C₀ + C₁ + C₂ = 1)",
        note = "K = storage time constant (flood travel time); x = weighting factor (0 ≤ x ≤ 0.5, typically 0.1–0.3).",
        subItems = listOf(
          "C₀ = (-Kx + 0.5Δt) / [ K(1 - x) + 0.5Δt ]",
          "C₁ = (Kx + 0.5Δt) / [ K(1 - x) + 0.5Δt ]",
          "C₂ = [ K(1 - x) - 0.5Δt ] / [ K(1 - x) + 0.5Δt ]"
        )
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Return Period (T) & Exceedance Probability (P)",
        formulaOrStatement = "T = 1 / P    •    Weibull Plotting Position: T = (N + 1) / m",
        note = "N = total years of annual peak record; m = descending rank order (m = 1 for largest flood on record).",
        subItems = listOf("Non-exceedance probability in any year: q = 1 - P = 1 - 1/T")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Hydrologic Risk (R) & Reliability (R_e) Over n Years",
        formulaOrStatement = "R = 1 - (1 - P)ⁿ = 1 - (1 - 1/T)ⁿ    •    Reliability R_e = (1 - 1/T)ⁿ",
        note = "Probability that a flood of return period T is equaled or exceeded at least once during an n-year project life.",
        subItems = listOf("For 1-in-100 yr flood over 50 yrs: R = 1 - 0.99⁵⁰ = 39.5%")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Chow's Frequency Factor Equation (Gumbel / LP-III)",
        formulaOrStatement = "X_T = X̄ + K_T · σ_(n-1)",
        note = "Estimates T-year flood magnitude using mean X̄, standard deviation σ_(n-1), and distribution frequency factor K_T.",
        subItems = listOf("Gumbel: K_T = -(√6 / π) · [ 0.5772 + ln(ln(T / (T - 1))) ]")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Groundwater Hydrology & Well Hydraulics (Dupuit & Thiem)",
    ribbonLabel = "09 Groundwater",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Aquifer Transmissivity & Storativity",
        formulaOrStatement = "T = K · b    •    Confined: S = S_s · b    •    Unconfined: S ≈ S_y",
        note = "K = hydraulic conductivity; b = confined aquifer thickness; S_s = specific storage; S_y = specific yield.",
        subItems = listOf("Porosity n = S_y + S_r (Specific Yield + Specific Retention)")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Steady Radial Flow to Confined Well (Thiem Equation)",
        formulaOrStatement = "Q = [ 2π · K · b · (h₂ - h₁) ] / ln(r₂ / r₁) = [ 2π · T · (s₁ - s₂) ] / ln(r₂ / r₁)",
        note = "b = constant saturated thickness between impermeable aquicludes; drawdown s = H - h.",
        subItems = listOf("At well casing (r₁ = r_w, h₁ = h_w) to radius of influence (r₂ = R, h₂ = H): Q = 2πKb(H - h_w)/ln(R/r_w)")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Steady Radial Flow to Unconfined Well (Dupuit Equation)",
        formulaOrStatement = "Q = [ π · K · (h₂² - h₁²) ] / ln(r₂ / r₁)",
        note = "Saturated flow height varies with radial distance r; measured from impervious base up to free phreatic water table.",
        subItems = listOf("In base-10 log form: Q = [ 1.366 · K · (h₂² - h₁²) ] / log₁₀(r₂ / r₁)")
      )
    )
  )
)

private fun buildRcdPcdSections(): List<AlgebraHandbookSection> = listOf(
  AlgebraHandbookSection(
    sectionTitle = "NSCP 2015 Load Combinations & φ Factors",
    ribbonLabel = "01 Loads & φ",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Factored Load Combinations (USD)",
        formulaOrStatement = "U = 1.4D  |  1.2D + 1.6L + 0.5(Lr or R)  |  1.2D + 1.0E + 1.0L",
        note = "Also check wind and uplift combos: 1.2D + 1.0W + 1.0L, 0.9D + 1.0W, and 0.9D + 1.0E.",
        subItems = listOf("1.2D + 1.6(Lr or R) + (1.0L or 0.5W)", "0.9D + 1.0E (Uplift / Overturning)"),
        codeTag = "NSCP 2015 Sec. 405.3.1"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Strength Reduction Factors (φ)",
        formulaOrStatement = "Tension: 0.90  •  Shear/Torsion: 0.75  •  Bearing: 0.65",
        note = "Compression-controlled sections: φ = 0.65 (Tied columns) and φ = 0.75 (Spiral columns).",
        subItems = listOf("Corbels & Brackets: φ = 0.75", "Plain Concrete: φ = 0.60"),
        codeTag = "NSCP 2015 Sec. 421.2.1 & 421.2.2"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Transition Zone φ Interpolation",
        formulaOrStatement = "φ = 0.65 + 0.25 · (ε_t - ε_ty) / (0.005 - ε_ty)   [Tied]",
        note = "Applies when net tensile strain ε_ty < ε_t < 0.005 (Grade 415/420 uses ε_ty ≈ 0.002).",
        subItems = listOf("Spiral Members: φ = 0.75 + 0.15 · (ε_t - ε_ty) / (0.005 - ε_ty)"),
        codeTag = "NSCP 2015 Table 421.2.2"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Concrete Modulus Ec & Rupture fr",
        formulaOrStatement = "E_c = 4700 · √f'_c  (MPa)    •    f_r = 0.62 · λ · √f'_c  (MPa)",
        note = "Normal-weight concrete uses λ = 1.0; reinforcing steel modulus E_s = 200,000 MPa.",
        subItems = listOf("Modular Ratio: n = E_s / E_c"),
        codeTag = "NSCP 2015 Sec. 419.2.2.1 & 419.2.3.1"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Cover, Bar Spacing & Minimum Steel",
    ribbonLabel = "02 Detailing",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Minimum Concrete Cover (Non-Prestressed)",
        formulaOrStatement = "Earth-Cast: 75 mm  •  Exposed: 50 mm (≥ Ø20) / 40 mm (≤ Ø16)",
        note = "Interior non-exposed members: 40 mm for beams & columns, 20 mm for slabs & joists (≤ Ø36).",
        subItems = listOf("Interior Beams/Columns: 40 mm", "Interior Slabs/Walls: 20 mm"),
        codeTag = "NSCP 2015 Sec. 420.6.1.3.1"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Minimum Clear Bar Spacing",
        formulaOrStatement = "Beams: s ≥ max(25 mm, d_b, 4/3 d_agg)  •  Cols: max(40 mm, 1.5 d_b)",
        note = "Clear vertical spacing between parallel bar layers in beams must be at least 25 mm.",
        subItems = listOf("Bundled bars: max 4 bars per bundle, treated as equivalent diameter"),
        codeTag = "NSCP 2015 Sec. 425.2.1 & 425.2.3"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Minimum Flexural Reinforcement As,min",
        formulaOrStatement = "A_s,min = max( 0.25 · √f'_c / f_y , 1.4 / f_y ) · b_w · d",
        note = "Exception: Not required to exceed 4/3 of A_s required by structural analysis.",
        subItems = listOf("Check ρ_min = max(0.25√f'_c / f_y, 1.4 / f_y)"),
        codeTag = "NSCP 2015 Sec. 409.6.1.2"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Slab Shrinkage & Temperature Steel",
        formulaOrStatement = "Grade 420: ρ_min = 0.0018  •  Grade 280/350: ρ_min = 0.0020",
        note = "Applied on gross slab area (b·h); max bar spacing s ≤ min(5h, 450 mm).",
        subItems = listOf("For f_y > 420 MPa: ρ_min = max(0.0018 × 420 / f_y, 0.0014)"),
        codeTag = "NSCP 2015 Sec. 424.4.3.2"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Singly Reinforced Rectangular Beams",
    ribbonLabel = "03 Singly Beam",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Whitney Stress Block Depth & β₁",
        formulaOrStatement = "a = β₁ · c = (A_s · f_y) / (0.85 · f'_c · b)",
        note = "β₁ = 0.85 for f'_c ≤ 28 MPa; decreases by 0.05 per 7 MPa above 28 MPa (min 0.65).",
        subItems = listOf("β₁ = 0.85 - 0.05(f'_c - 28)/7 ≥ 0.65"),
        codeTag = "NSCP 2015 Sec. 422.2.2.4.1 & 422.2.2.4.3"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Nominal & Design Flexural Strength",
        formulaOrStatement = "M_n = A_s · f_y · (d - a / 2)    •    M_u ≤ φ · M_n",
        note = "Coefficient of resistance form: R_n = M_n / (b d²) = ρ f_y [ 1 - (ρ f_y) / (1.7 f'_c) ].",
        subItems = listOf("ω = ρ · f_y / f'_c  ⇒  R_n = f'_c · ω · (1 - 0.59 ω)"),
        codeTag = "NSCP 2015 Sec. 422.2.2.4"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Balanced Steel Ratio & Net Tensile Strain",
        formulaOrStatement = "ρ_b = 0.85 · β₁ · (f'_c / f_y) · [ 600 / (600 + f_y) ]",
        note = "Net tensile strain ε_t = 0.003(d_t - c)/c ≥ 0.004 for beams; ε_t ≥ 0.005 (c/d_t ≤ 0.375) for φ = 0.90.",
        subItems = listOf("Tension-controlled ρ_max = 0.375 · 0.85 · β₁ · (f'_c / f_y)"),
        codeTag = "NSCP 2015 Sec. 409.3.3.1"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Cracking Moment & Effective Inertia Ie",
        formulaOrStatement = "M_cr = (f_r · I_g) / y_t  •  I_e = (M_cr/M_a)³ I_g + [1 - (M_cr/M_a)³] I_cr",
        note = "Branson equation for immediate service-load deflection (I_e ≤ I_g).",
        subItems = listOf("Long-term deflection multiplier: λ_Δ = ξ / (1 + 50 ρ')"),
        codeTag = "NSCP 2015 Sec. 424.2.3.5"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Doubly Reinforced Beams & T-Beams",
    ribbonLabel = "04 Doubly & T",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Doubly Reinforced Capacity (Yielding Check)",
        formulaOrStatement = "M_n = (A_s - A'_s) · f_y · (d - a / 2) + A'_s · f'_s · (d - d')",
        note = "Compression steel yields (f'_s = f_y) if ρ - ρ' ≥ 0.85 β₁ (f'_c/f_y)(d'/d)[600/(600 - f_y)].",
        subItems = listOf("If yielded: a = (A_s - A'_s) f_y / (0.85 f'_c b)"),
        codeTag = "NSCP 2015 Sec. 422.2.2"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Unyielded Compression Steel Strain",
        formulaOrStatement = "f'_s = 600 · (c - d') / c ≤ f_y",
        note = "When ρ - ρ' < ρ_cy, solve quadratic equilibrium: 0.85 f'_c β₁ b c + A'_s [600(c - d')/c] = A_s f_y.",
        subItems = listOf("Verify final tension strain ε_t = 0.003(d - c)/c ≥ 0.004"),
        codeTag = "NSCP 2015 Sec. 422.2.1.2"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Effective Flange Width (T & L-Beams)",
        formulaOrStatement = "T-Beam: b_f = min(b_w + 16 h_f , b_w + S_w , L / 4)",
        note = "Spandrel (Edge) L-Beam: b_f = b_w + min(6 h_f , S_w / 2 , L / 12).",
        subItems = listOf("S_w = clear distance to adjacent beam web", "L = beam span length"),
        codeTag = "NSCP 2015 Sec. 406.3.2.1"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "True T-Beam Analysis (a > h_f)",
        formulaOrStatement = "A_sf = 0.85 f'_c (b_f - b_w) h_f / f_y  •  M_nf = A_sf f_y (d - h_f/2)",
        note = "If a = A_s f_y / (0.85 f'_c b_f) ≤ h_f, analyze as rectangular beam of width b_f.",
        subItems = listOf("Web Couple: M_nw = (A_s - A_sf) f_y (d - a/2)", "a = (A_s - A_sf) f_y / (0.85 f'_c b_w)"),
        codeTag = "NSCP 2015 Sec. 422.2.2"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "One-Way Shear & Stirrup Design",
    ribbonLabel = "05 Shear",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Factored Shear & Critical Section",
        formulaOrStatement = "V_u ≤ φ · (V_c + V_s)    (φ = 0.75)",
        note = "Critical factored shear V_u is taken at distance d from the face of support (when support reaction introduces compression).",
        subItems = listOf("Minimum shear steel required whenever V_u > 0.5 φ V_c"),
        codeTag = "NSCP 2015 Sec. 422.5.1.1 & 409.4.3.2"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Concrete Nominal Shear Strength Vc",
        formulaOrStatement = "V_c = 0.17 · λ · √f'_c · b_w · d   (Simplified)",
        note = "With factored axial compression N_u: V_c = 0.17 [ 1 + N_u / (14 A_g) ] λ √f'_c b_w d.",
        subItems = listOf("Detailed formula: V_c = [0.16 λ √f'_c + 17 ρ_w (V_u d / M_u)] b_w d ≤ 0.29 λ √f'_c b_w d"),
        codeTag = "NSCP 2015 Sec. 422.5.5.1"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Vertical Stirrup Strength Vs & Upper Cap",
        formulaOrStatement = "V_s = (A_v · f_yt · d) / s    ⇒    s_req = (A_v · f_yt · d) / V_s",
        note = "Section cross-section size limit: V_s must never exceed 0.66 · √f'_c · b_w · d.",
        subItems = listOf("Minimum stirrup area: A_v,min = max(0.062√f'_c, 0.35) · (b_w · s / f_yt)"),
        codeTag = "NSCP 2015 Sec. 422.5.10.5.3 & 422.5.1.2"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Maximum Stirrup Spacing Limits",
        formulaOrStatement = "V_s ≤ 0.33√f'_c b_w d: min(d/2, 600)  •  V_s > 0.33√f'_c b_w d: min(d/4, 300)",
        note = "If required spacing drops below practical placement (~75 mm), increase bar size or beam dimensions.",
        subItems = listOf("All spacing limits in millimeters (mm)"),
        codeTag = "NSCP 2015 Sec. 409.7.6.2.2"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Development Length, Hooks & Splices",
    ribbonLabel = "06 Bond & Splice",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "General Tension Development Length",
        formulaOrStatement = "l_d = [ (f_y · ψ_t · ψ_e · ψ_s) / (1.1 · λ · √f'_c · ((c_b + K_tr)/d_b)) ] · d_b ≥ 300 mm",
        note = "Confinement factor (c_b + K_tr)/d_b must not exceed 2.5; transverse index K_tr = 40 A_tr / (s · n).",
        subItems = listOf("Size factor ψ_s = 0.8 (≤ Ø20) | 1.0 (≥ Ø22)", "Top bar ψ_t = 1.3 (> 300 mm concrete below)"),
        codeTag = "NSCP 2015 Sec. 425.4.2.3"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Simplified Tension Development Length",
        formulaOrStatement = "≤ Ø20: l_d = (f_y ψ_t ψ_e)/(2.1 λ √f'_c) · d_b  •  ≥ Ø22: (f_y ψ_t ψ_e)/(1.7 λ √f'_c) · d_b",
        note = "Valid when clear spacing ≥ d_b, clear cover ≥ d_b, and code-minimum stirrups are provided (or clear spacing ≥ 2 d_b and cover ≥ d_b).",
        subItems = listOf("Uncoated bar ψ_e = 1.0  •  Minimum l_d ≥ 300 mm"),
        codeTag = "NSCP 2015 Sec. 425.4.2.2"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Standard 90° & 180° Hook Development",
        formulaOrStatement = "l_dh = (0.24 · f_y · ψ_e · ψ_c · ψ_r) / (λ · √f'_c) · d_b ≥ max(8 d_b, 150 mm)",
        note = "Measured from critical section to outside end of 90°/180° hook; ψ_c = 0.7 if side cover ≥ 65 mm.",
        subItems = listOf("90° hook extension: 12 d_b  •  180° hook extension: max(4 d_b, 65 mm)"),
        codeTag = "NSCP 2015 Sec. 425.4.3.1"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Tension Lap Splices & Compression ldc",
        formulaOrStatement = "Class A: 1.0 l_d  •  Class B: 1.3 l_d ≥ 300 mm  •  Comp: 0.24 f_y d_b / (λ√f'_c)",
        note = "Class B required when > 50% of steel is spliced at one section or A_s,provided / A_s,required < 2.0.",
        subItems = listOf("Compression l_dc ≥ max(0.043 f_y d_b, 200 mm)"),
        codeTag = "NSCP 2015 Sec. 425.5.2.1 & 425.4.9.2"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Short & Slender RC Columns (P-M)",
    ribbonLabel = "07 Columns",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Maximum Design Axial Capacity φPn,max",
        formulaOrStatement = "Tied (φ=0.65): φP_n,max = 0.80 · φ · [ 0.85 f'_c (A_g - A_st) + f_y A_st ]",
        note = "Spiral Columns (φ = 0.75): factor increases to 0.85 · φ · P₀ for enhanced core ductility.",
        subItems = listOf("Pure Axial Load P₀ = 0.85 f'_c (A_g - A_st) + f_y A_st"),
        codeTag = "NSCP 2015 Sec. 422.4.2.2"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Longitudinal Bar Ratio & Tie Spacing",
        formulaOrStatement = "0.01 · A_g ≤ A_st ≤ 0.08 · A_g    •    s_tie ≤ min(16 d_b, 48 d_t, b_min)",
        note = "Minimum 4 bars for rectangular/square tied columns and 6 bars for circular spiral columns.",
        subItems = listOf("Minimum tie diameter: Ø10 for ≤ Ø32 main bars, Ø12 for ≥ Ø36 or bundled"),
        codeTag = "NSCP 2015 Sec. 410.6.1.1 & 425.7.2"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Spiral Reinforcement Volumetric Ratio",
        formulaOrStatement = "ρ_s ≥ 0.45 · ( A_g / A_ch - 1 ) · ( f'_c / f_yt )",
        note = "A_ch is core area measured to outside diameter of spiral; clear spacing 25 mm ≤ s_clear ≤ 75 mm.",
        subItems = listOf("Volumetric ratio: ρ_s = (4 a_s) / (D_c · s)"),
        codeTag = "NSCP 2015 Sec. 425.7.3.3"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Slenderness Limit & Moment Magnifier",
        formulaOrStatement = "Braced: k·l_u / r ≤ 34 - 12(M₁/M₂) ≤ 40  •  δ_ns = C_m / (1 - P_u / (0.75 P_c)) ≥ 1.0",
        note = "Unbraced (sway) columns neglect slenderness only if k·l_u / r ≤ 22; r = 0.30 h (rect) or 0.25 D (circ).",
        subItems = listOf("P_c = π²(EI)_eff / (k l_u)²", "C_m = 0.6 + 0.4(M₁/M₂) for braced members without transverse load"),
        codeTag = "NSCP 2015 Sec. 406.2.5 & 406.6.4"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Reinforced Concrete Footings",
    ribbonLabel = "08 Footings",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Footing Area & Factored Soil Bearing",
        formulaOrStatement = "A_req = P_service / q_net    •    q_u = P_u / (B · L)",
        note = "Uniaxial eccentricity (e ≤ B/6): q_u,max/min = (P_u / B·L) · (1 ± 6e / B).",
        subItems = listOf("If e > B/6: q_u,max = 2 P_u / [ 3 L (B/2 - e) ]"),
        codeTag = "NSCP 2015 Sec. 413.3.1.1"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "One-Way (Wide Beam) Shear at Distance d",
        formulaOrStatement = "V_u = q_u · B · [ (L - c₁)/2 - d ] ≤ φ · 0.17 · λ · √f'_c · B · d",
        note = "Evaluated across full footing width B on critical plane located d from column face (φ = 0.75).",
        subItems = listOf("For round column of diameter D, equivalent square side c_eq = 0.886 D"),
        codeTag = "NSCP 2015 Sec. 422.5.5.1"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Two-Way (Punching) Shear at d/2",
        formulaOrStatement = "V_u = q_u · [ B·L - (c₁+d)(c₂+d) ]    •    b₀ = 2(c₁ + d) + 2(c₂ + d)",
        note = "Two-way φV_c = φ · λ · √f'_c · b₀ · d · min[ 0.33 , 0.17(1 + 2/β) , 0.083(α_s d/b₀ + 2) ].",
        subItems = listOf("α_s = 40 (Interior), 30 (Edge), 20 (Corner column)", "β = c_long / c_short"),
        codeTag = "NSCP 2015 Sec. 422.6.5.2"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Banded Flexural Reinforcement (Rect. Footing)",
        formulaOrStatement = "γ_s = 2 / (β + 1)   [Fraction placed in central band of width B]",
        note = "Critical flexural moment section is at the face of the concrete column or pedestal.",
        subItems = listOf("β = Long Side L / Short Side B ≥ 1.0", "Minimum footing steel ρ_min = 0.0018 (Gr. 420)"),
        codeTag = "NSCP 2015 Sec. 413.3.3.3"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Prestressed Concrete Design (PCD)",
    ribbonLabel = "09 PCD",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Combined Prestress & Bending Fiber Stress",
        formulaOrStatement = "f = -P / A ± (P · e · y) / I ± (M · y) / I",
        note = "Compression is negative (-); verify extreme top and bottom fibers at transfer (P_i, M_g) and service (P_e, M_T).",
        subItems = listOf("Top fiber (with +e below C.G.C): f_t = -P/A + Pey_t/I - My_t/I"),
        codeTag = "NSCP 2015 Sec. 424.5.3"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Permissible Concrete Fiber Stresses",
        formulaOrStatement = "Transfer: f_c ≤ 0.60 f'_ci, f_t ≤ 0.25√f'_ci (0.50 ends)  •  Service: 0.45 f'_c",
        note = "Class U uncracked service tension limit ≤ 0.62√f'_c; total sustained + live service compression ≤ 0.60 f'_c.",
        subItems = listOf("Class T (Transition): 0.62√f'_c < f_t ≤ 1.0√f'_c"),
        codeTag = "NSCP 2015 Sec. 424.5.3 & 424.5.4"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Prestress Losses (Elastic, Creep, Friction)",
        formulaOrStatement = "Δf_pES = n · f_cgp  (n = E_p / E_ci)   •   P_x = P_0 · e^-(μ α + K x)",
        note = "Total loss Δf_p = Δf_pES + Δf_pCR + Δf_pSH + Δf_pRE; effective prestress P_e = P_i - losses.",
        subItems = listOf("f_cgp = concrete stress at tendon C.G.S. immediately after transfer"),
        codeTag = "NSCP 2015 Sec. 424.5"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Load Balancing (Parabolic & Harped Tendons)",
        formulaOrStatement = "w_bal = (8 · P · e) / L²   ⇒   P_bal = (w · L²) / (8 · e)",
        note = "Balanced upward uniform force cancels dead load + portion of live load, leaving pure axial stress -P/A.",
        subItems = listOf("Single center harped drape point load: P_load = 4 · P · e / L"),
        codeTag = "NSCP 2015 Sec. 424.5"
      )
    )
  )
)

private fun buildSteelDesignSections(): List<AlgebraHandbookSection> = listOf(
  AlgebraHandbookSection(
    sectionTitle = "LRFD / ASD Basis & Material Grades",
    ribbonLabel = "01 Basis",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "LRFD & ASD Structural Design Inequalities",
        formulaOrStatement = "LRFD: R_u ≤ φ · R_n    •    ASD: R_a ≤ R_n / Ω",
        note = "Always verify governing LRFD (factored loads) or ASD (service loads) criteria required by the exam problem.",
        subItems = listOf("Consistent relationship: Ω ≈ 1.5 / φ"),
        codeTag = "NSCP 2015 Sec. 502.3.3 & 502.3.4"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Standard Structural Steel Properties",
        formulaOrStatement = "A36: F_y = 248 MPa, F_u = 400 MPa  •  A992/Gr.50: F_y = 345 MPa, F_u = 450 MPa",
        note = "Universal steel elastic constants: E = 200,000 MPa, Shear Modulus G = 77,200 MPa, ν = 0.30.",
        subItems = listOf("Unit weight of structural steel: γ = 7850 kg/m³ (77.0 kN/m³)"),
        codeTag = "NSCP 2015 Sec. 501.3"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Compact Section Width-Thickness Limits",
        formulaOrStatement = "W-Flange: b_f/(2 t_f) ≤ 0.38√(E/F_y)  •  Web: h/t_w ≤ 3.76√(E/F_y)",
        note = "Sections exceeding λ_r are slender: Flange λ_r = 1.0√(E/F_y), Web λ_r = 5.70√(E/F_y).",
        subItems = listOf("Non-compact regime occurs when λ_p < λ ≤ λ_r"),
        codeTag = "NSCP 2015 Table 502.4.1"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Tension Members & Block Shear Rupture",
    ribbonLabel = "02 Tension",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Tensile Yielding on Gross Section",
        formulaOrStatement = "P_n = F_y · A_g    (φ_t = 0.90,  Ω_t = 1.67)",
        note = "Prevents excessive elongation of the member along its unperforated gross length.",
        subItems = listOf("Recommended tension slenderness check: L / r ≤ 300"),
        codeTag = "NSCP 2015 Sec. 504.2(a)"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Tensile Rupture on Effective Net Area",
        formulaOrStatement = "P_n = F_u · A_e   (A_e = U · A_n,  φ_t = 0.75,  Ω_t = 2.00)",
        note = "Governs fracture across the bolted or connected cross-section.",
        subItems = listOf("Flat plate splice upper cap: A_e ≤ 0.85 A_g"),
        codeTag = "NSCP 2015 Sec. 504.2(b)"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Staggered Bolt Holes & Net Width",
        formulaOrStatement = "w_net = w_g - Σ d_h + Σ [ s² / (4 · g) ]    •    d_h = d_b + 2 mm",
        note = "NSCP 2015 uses nominal bolt hole diameter d_h = d_b + 2 mm for net area deduction (unlike AISC US +1/8 in = 3.2 mm).",
        subItems = listOf("A_n = w_net · t  (Check all straight and zig-zag failure paths)"),
        codeTag = "NSCP 2015 Sec. 502.4.3b"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Shear Lag Reduction Factor U",
        formulaOrStatement = "U = 1 - x̄ / L    (General Bolted / Welded Connection)",
        note = "U = 1.0 when all cross-section elements are connected; W-shapes with ≥ 3 bolts per line in flange use U = 0.90 (b_f ≥ 2/3 d) or 0.85.",
        subItems = listOf("Single/double angle with ≥ 4 fasteners: U = 0.80 (≥3: U = 0.60)"),
        codeTag = "NSCP 2015 Table 504.3.1"
      ),
      AlgebraHandbookRuleItem(
        number = 5,
        title = "Block Shear Rupture Strength",
        formulaOrStatement = "R_n = 0.60 F_u A_nv + U_bs F_u A_nt ≤ 0.60 F_y A_gv + U_bs F_u A_nt",
        note = "φ = 0.75 (LRFD) and Ω = 2.00 (ASD); U_bs = 1.0 for uniform tension stress, 0.5 for non-uniform (coped beam).",
        subItems = listOf("A_gv = gross shear area", "A_nv = net shear area", "A_nt = net tension area"),
        codeTag = "NSCP 2015 Sec. 510.4.3"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Compression Members & Column Buckling",
    ribbonLabel = "03 Columns",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Nominal Axial Compressive Capacity",
        formulaOrStatement = "P_n = F_cr · A_g    (φ_c = 0.90,  Ω_c = 1.67)",
        note = "Use largest effective slenderness ratio max(K_x L_x / r_x , K_y L_y / r_y).",
        subItems = listOf("Column slenderness guideline: KL / r ≤ 200"),
        codeTag = "NSCP 2015 Sec. 505.3"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Elastic Critical Euler Stress Fe",
        formulaOrStatement = "F_e = (π² · E) / (K · L / r)²",
        note = "Applies to both inelastic and elastic column buckling equations.",
        subItems = listOf("With E = 200,000 MPa: F_e ≈ 1,973,921 / (KL/r)² MPa"),
        codeTag = "NSCP 2015 Eq. 505.3-4"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Inelastic & Elastic Critical Stress Fcr",
        formulaOrStatement = "KL/r ≤ 4.71√(E/F_y): F_cr = [0.658^(F_y / F_e)] · F_y  |  Else: 0.877 F_e",
        note = "Transition limit 4.71√(E/F_y) corresponds to F_y / F_e = 2.25 (e.g., 133.7 for A36, 113.4 for Gr. 50).",
        subItems = listOf("For built-up or slender sections, multiply A_g by reduction factor Q"),
        codeTag = "NSCP 2015 Eq. 505.3-2 & 505.3-3"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Column Effective Length K-Factors",
        formulaOrStatement = "Pin-Pin: 1.0 • Fix-Pin: 0.70 (0.80) • Fix-Fix: 0.50 (0.65) • Fix-Free: 2.0 (2.1)",
        note = "Parenthetical values are NSCP / AISC recommended design values accounting for imperfect fixity.",
        subItems = listOf("Frame sway/non-sway columns: use alignment chart G_A, G_B"),
        codeTag = "NSCP 2015 Sec. 505.2"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Flexural Members (Compact Beams & LTB)",
    ribbonLabel = "04 Flexure",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Zone 1: Plastic Moment Yielding (Lb ≤ Lp)",
        formulaOrStatement = "M_n = M_p = F_y · Z_x    •    L_p = 1.76 · r_y · √(E / F_y)",
        note = "Compact, laterally braced beams reach full plastic moment M_p (φ_b = 0.90, Ω_b = 1.67).",
        subItems = listOf("Shape factor: SF = Z_x / S_x (≈ 1.10 to 1.15 for W-shapes)"),
        codeTag = "NSCP 2015 Sec. 506.2.1"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Zone 2: Inelastic LTB (Lp < Lb ≤ Lr)",
        formulaOrStatement = "M_n = C_b · [ M_p - (M_p - 0.7 F_y S_x) · (L_b - L_p)/(L_r - L_p) ] ≤ M_p",
        note = "Never allow amplified moment C_b · M_n to exceed full plastic capacity M_p.",
        subItems = listOf("Linear interpolation between M_p at L_p and 0.7 F_y S_x at L_r"),
        codeTag = "NSCP 2015 Sec. 506.2.2"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Zone 3: Elastic LTB (Lb > Lr)",
        formulaOrStatement = "M_n = F_cr · S_x ≤ M_p   •   F_cr = (C_b π²E)/(L_b/r_ts)² · √[1 + 0.078 (Jc)/(S_x h_0)(L_b/r_ts)²]",
        note = "For doubly symmetric I-shapes, c = 1.0 and h_0 = distance between flange centroids.",
        subItems = listOf("Effective radius of gyration: r_ts² = √(I_y · C_w) / S_x"),
        codeTag = "NSCP 2015 Sec. 506.2.2"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "LTB Moment Gradient Factor Cb",
        formulaOrStatement = "C_b = (12.5 · M_max) / (2.5 M_max + 3 M_A + 4 M_B + 3 M_C) ≤ 3.0",
        note = "M_A, M_B, M_C are absolute moments at quarter, center, and three-quarter points of unbraced segment L_b.",
        subItems = listOf("Uniform moment: C_b = 1.0", "Simply supported UDL unbraced: C_b = 1.14"),
        codeTag = "NSCP 2015 Eq. 506.1-1"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Beam Shear, Web Yielding & Crippling",
    ribbonLabel = "05 Shear & Web",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Nominal Web Shear Strength",
        formulaOrStatement = "V_n = 0.60 · F_y · A_w · C_v1    (A_w = d · t_w)",
        note = "For rolled I-shapes with h/t_w ≤ 2.24√(E/F_y): φ_v = 1.00, Ω_v = 1.50, and C_v1 = 1.0.",
        subItems = listOf("Other shapes / built-up webs: φ_v = 0.90, Ω_v = 1.67"),
        codeTag = "NSCP 2015 Sec. 507.2.1"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Web Local Yielding (Concentrated Load)",
        formulaOrStatement = "Interior: R_n = F_yw · t_w · (5k + l_b)  •  End (x ≤ d): F_yw · t_w · (2.5k + l_b)",
        note = "l_b is bearing plate length; k is distance from outer flange face to web toe of fillet (φ = 1.00, Ω = 1.50).",
        subItems = listOf("Provide transverse stiffeners if R_u > φ R_n"),
        codeTag = "NSCP 2015 Sec. 510.10.2"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Web Local Crippling",
        formulaOrStatement = "R_n = 0.80 t_w² [1 + 3(l_b/d)(t_w/t_f)^1.5] √(E F_yw t_f / t_w)   [x ≥ d/2]",
        note = "For interior concentrated loads at x ≥ d/2 from member end (φ = 0.75, Ω = 2.00).",
        subItems = listOf("End load (x < d/2, l_b/d ≤ 0.2): lead coeff 0.40 with 3(l_b/d) factor"),
        codeTag = "NSCP 2015 Sec. 510.10.3"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Beam-Columns (Combined Axial & Bending)",
    ribbonLabel = "06 Beam-Col",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "High Axial Load Interaction (Pr/Pc ≥ 0.20)",
        formulaOrStatement = "(P_r / P_c) + (8 / 9) · [ (M_rx / M_cx) + (M_ry / M_cy) ] ≤ 1.0",
        note = "LRFD: P_c = φ_c P_n, M_c = φ_b M_n; ASD: P_c = P_n / Ω_c, M_c = M_n / Ω_b.",
        subItems = listOf("Doubly & singly symmetric members subjected to flexure and axial force"),
        codeTag = "NSCP 2015 Eq. 508.1-1a"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Low Axial Load Interaction (Pr/Pc < 0.20)",
        formulaOrStatement = "(P_r / 2 P_c) + [ (M_rx / M_cx) + (M_ry / M_cy) ] ≤ 1.0",
        note = "Used when axial demand is under 20% of available axial strength.",
        subItems = listOf("Same equation applies for axial tension + bending"),
        codeTag = "NSCP 2015 Eq. 508.1-1b"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Second-Order P-δ Amplification (B₁ / B₂)",
        formulaOrStatement = "M_r = B₁ · M_nt + B₂ · M_lt    •    B₁ = C_m / (1 - α P_r / P_e1) ≥ 1.0",
        note = "α = 1.00 (LRFD) or α = 1.60 (ASD); C_m = 0.6 - 0.4(M₁/M₂) for braced members with no transverse load.",
        subItems = listOf("P_e1 = π² E I / (K₁ L)²", "B₂ = 1 / (1 - α ΣP_story / ΣP_e2)"),
        codeTag = "NSCP 2015 Appendix 8"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Bolted Connections (Shear, Bearing & Slip)",
    ribbonLabel = "07 Bolts",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Bolt Nominal Shear & Tension Strength",
        formulaOrStatement = "R_n = F_n · A_b · N_planes    (φ = 0.75,  Ω = 2.00)",
        note = "A325-N (threads included): F_nv = 372 MPa; A325-X (threads excluded): F_nv = 469 MPa; A325 F_nt = 620 MPa.",
        subItems = listOf("A490-N: F_nv = 469 MPa  •  A490-X: F_nv = 579 MPa  •  A_b = π d_b² / 4"),
        codeTag = "NSCP 2015 Sec. 510.3.6"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Bearing & Tear-Out at Bolt Holes",
        formulaOrStatement = "R_n = 1.2 · L_c · t · F_u ≤ 2.4 · d_b · t · F_u",
        note = "L_c = clear distance between hole edge and adjacent hole or free plate edge (φ = 0.75, Ω = 2.00).",
        subItems = listOf("If deformation at service load is NOT a design consideration: 1.5 L_c t F_u ≤ 3.0 d_b t F_u"),
        codeTag = "NSCP 2015 Sec. 510.3.10"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Slip-Critical Connection Strength",
        formulaOrStatement = "R_n = μ · D_u · h_f · T_b · n_s    (φ = 1.00,  Ω = 1.50 [Std Holes])",
        note = "Class A unpainted mill scale: μ = 0.30, D_u = 1.13, filler factor h_f = 1.0.",
        subItems = listOf("Minimum bolt pretension T_b ≈ 0.70 F_ub A_st"),
        codeTag = "NSCP 2015 Sec. 510.3.8"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Eccentric Bolt Group (Elastic Vector)",
        formulaOrStatement = "R_x = (P_x/N) + (M·y)/Σ(x²+y²)  •  R_y = (P_y/N) + (M·x)/Σ(x²+y²)",
        note = "Combine perpendicular components on the critical outermost bolt: R_max = √(R_x² + R_y²).",
        subItems = listOf("Polar moment of bolt array: J = Σ(x_i² + y_i²)"),
        codeTag = "NSCP 2015 Sec. 510.3"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Welded Connections (Fillet Welds)",
    ribbonLabel = "08 Welds",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Fillet Weld Nominal Shear Strength",
        formulaOrStatement = "R_n = 0.60 · F_EXX · (0.707 · w · L_w)    (φ = 0.75,  Ω = 2.00)",
        note = "E70XX electrode: F_EXX = 480 MPa (485 MPa); effective throat thickness t_e = 0.707 · w.",
        subItems = listOf("Directional strength boost (when θ ≠ 0°): F_nw = 0.60 F_EXX (1.0 + 0.50 sin^1.5 θ)"),
        codeTag = "NSCP 2015 Sec. 510.2.4"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Minimum & Maximum Fillet Weld Size",
        formulaOrStatement = "t ≤ 6: 3 mm • 6<t≤13: 5 mm • 13<t≤19: 6 mm • t>19: 8 mm",
        note = "Maximum weld along lap edge: w_max = t (for t < 6 mm) and w_max = t - 2 mm (for t ≥ 6 mm).",
        subItems = listOf("Minimum effective weld length: L_w ≥ 4 · w"),
        codeTag = "NSCP 2015 Table 510.2.4 & Sec. 510.2.2b"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Eccentric Weld Group (Elastic Vector)",
        formulaOrStatement = "f_v = √[ (P_x/L_w + M·y/I_p)² + (P_y/L_w + M·x/I_p)² ]",
        note = "Treat weld as a unit-width line segment to compute centroid (x̄, ȳ) and polar inertia I_p = I_x + I_y.",
        subItems = listOf("Allowable weld force per mm: q_all = φ · 0.60 · F_EXX · 0.707 · w"),
        codeTag = "NSCP 2015 Sec. 510.2"
      )
    )
  )
)

private fun buildFoundationEngineeringSections(): List<AlgebraHandbookSection> = listOf(
  AlgebraHandbookSection(
    sectionTitle = "Terzaghi Ultimate & Allowable Bearing",
    ribbonLabel = "01 Terzaghi",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "General Shear Failure Equations",
        formulaOrStatement = "Strip: q_u = c N_c + q N_q + 0.5 γ B N_γ  •  Square: 1.3 c N_c + q N_q + 0.4 γ B N_γ",
        note = "Circular Footing: q_u = 1.3 c N_c + q N_q + 0.3 γ B N_γ, where overburden q = γ · D_f.",
        subItems = listOf("Strip = Continuous wall footing (L >> B)", "Diameter B applies to circular footing")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Local Shear Failure Reduction",
        formulaOrStatement = "c' = (2 / 3) · c    •    tan(φ') = (2 / 3) · tan(φ)",
        note = "Applies to loose sand or soft clay; replace c, N_c, N_q, N_γ with c' and reduced factors N'_c, N'_q, N'_γ.",
        subItems = listOf("Strip Local: q_u = (2/3) c N'_c + q N'_q + 0.5 γ B N'_γ", "Square: 0.867 c N'_c + q N'_q + 0.4 γ B N'_γ")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Gross & Net Allowable Bearing Capacity",
        formulaOrStatement = "q_net(u) = q_u - q  (q = γ D_f)  •  q_all = q_u / FS  |  q_all(net) = (q_u - q)/FS + q",
        note = "Standard factor of safety FS = 3.0 against general bearing shear failure.",
        subItems = listOf("Net footing soil pressure check: q_net ≤ (q_u - q) / FS"),
        codeTag = "NSCP 2015 Sec. 304.2"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Groundwater Table (GWT) Corrections",
    ribbonLabel = "02 GWT Cases",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Case I: Water Table Above or At Base (0 ≤ Dw ≤ Df)",
        formulaOrStatement = "q = γ · D_w + γ' · (D_f - D_w)    •    Wedge γ̄ = γ' = γ_sat - γ_w",
        note = "Submerged unit weight γ' reduces both the overburden pressure q and the third failure-wedge term 0.5 γ̄ B N_γ.",
        subItems = listOf("If GWT at ground surface (D_w = 0): q = γ' · D_f"),
        codeTag = "NSCP 2015 Sec. 304.2"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Case II: Water Table Within Depth B (Df < Dw < Df + B)",
        formulaOrStatement = "q = γ · D_f    •    γ̄ = γ' + (d / B) · (γ - γ')   [where d = D_w - D_f]",
        note = "Overburden term q remains unchanged; linearly interpolate unit weight γ̄ in the 0.5 γ̄ B N_γ term.",
        subItems = listOf("When D_w = D_f (d = 0): γ̄ = γ'  •  When D_w = D_f + B (d = B): γ̄ = γ"),
        codeTag = "NSCP 2015 Sec. 304.2"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Case III: Deep Water Table (Dw ≥ Df + B)",
        formulaOrStatement = "q = γ · D_f    •    Wedge γ̄ = γ  (Moist Unit Weight)",
        note = "Groundwater table lies completely below the failure wedge zone; zero water-table correction needed.",
        subItems = listOf("Standard γ_w = 9.81 kN/m³ (or 1000 kg/m³)")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Meyerhof General Bearing Capacity",
    ribbonLabel = "03 Meyerhof",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Meyerhof General Bearing Equation",
        formulaOrStatement = "q_u = c N_c F_cs F_cd F_ci + q N_q F_qs F_qd F_qi + 0.5 γ B N_γ F_γs F_γd F_γi",
        note = "Accounts for rectangular shape (F_s), embedment depth (F_d), and load inclination (F_i).",
        subItems = listOf("Vertical load: F_ci = F_qi = F_γi = 1.0  •  Inclined β: F_ci = F_qi = (1 - β/90°)²")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Meyerhof Bearing Capacity Factors",
        formulaOrStatement = "N_q = tan²(45° + φ/2) · e^(π tan φ)  •  N_c = (N_q - 1) cot φ  •  N_γ = 2(N_q + 1) tan φ",
        note = "For undrained clay (φ = 0): N_c = 5.14, N_q = 1.0, and N_γ = 0.",
        subItems = listOf("Inclination F_γi = (1 - β / φ)²  (zero if β ≥ φ)")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Shape Factors (Fcs, Fqs, Fγs)",
        formulaOrStatement = "F_cs = 1 + (B/L)(N_q/N_c)  •  F_qs = 1 + (B/L) tan φ  •  F_γs = 1 - 0.4(B/L)",
        note = "Also check Depth Factors (D_f/B ≤ 1): F_cd = 1 + 0.4(D_f/B), F_qd = 1 + 2 tanφ(1 - sinφ)²(D_f/B), F_γd = 1.",
        subItems = listOf("For φ = 0: F_cs = 1 + 0.2(B/L), F_qs = F_γs = 1.0")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Eccentrically Loaded Footings & Mats",
    ribbonLabel = "04 Eccentric",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Meyerhof Effective Area Reduction",
        formulaOrStatement = "B' = B - 2 e_B    •    L' = L - 2 e_L    •    A' = B' × L'",
        note = "Use reduced B' and L' in bearing capacity F_s factors and 0.5 γ B' N_γ term; use minimum of (B', L') in wedge term.",
        subItems = listOf("One-way eccentricity e = M / P")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Full Contact Soil Pressure (e ≤ B/6)",
        formulaOrStatement = "One-Way: q_max,min = (P / B·L) · (1 ± 6e / B)  |  Two-Way: P/(BL) ± M_x y/I_x ± M_y x/I_y",
        note = "When resultant falls inside the middle third (kern), full footing area remains in compression (q_min ≥ 0).",
        subItems = listOf("Biaxial kern check: (6 e_B / B) + (6 e_L / L) ≤ 1.0")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Partial Uplift Triangular Pressure (e > B/6)",
        formulaOrStatement = "q_max = 2 P / [ 3 L · (B/2 - e) ]    •    Contact Length = 3 · (B/2 - e)",
        note = "Soil cannot carry tension; bearing stress redistributes as a triangle over the compressed toe.",
        subItems = listOf("Also verify footing does not overturn (e < B/2)")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Compensated (Floating) Mat Foundation",
        formulaOrStatement = "q_net = (Q / A) - γ · D_f    ⇒    Zero Net Pressure: D_f = Q / (A · γ)",
        note = "Excavating soil of weight equal to building load eliminates net consolidation settlement.",
        subItems = listOf("Mat FS against bearing failure: FS = q_net(u) / (Q/A - γ D_f)"),
        codeTag = "NSCP 2015 Sec. 305.5"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Foundation Settlement (Elastic & Consolidation)",
    ribbonLabel = "05 Settlement",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Immediate Elastic Settlement Se",
        formulaOrStatement = "S_e = q_o · B · [ (1 - ν_s²) / E_s ] · I_w",
        note = "Influence factor I_w ≈ 0.82 (rigid square), 1.12 (flexible center), 0.56 (flexible corner).",
        subItems = listOf("Also check layered elastic avg using Steinbrenner influence factor I_s")
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Primary Consolidation (Normally Consolidated)",
        formulaOrStatement = "S_c = [ C_c · H / (1 + e_o) ] · log₁₀[ (σ'_o + Δσ') / σ'_o ]",
        note = "Valid for OCR = 1 (σ'_o = σ'_c); compression index C_c ≈ 0.009(LL - 10) for undisturbed clays.",
        subItems = listOf("Time of consolidation: t = (T_v · H_dr²) / c_v  (T₅₀ = 0.197, T₉₀ = 0.848)")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Overconsolidated Clay Consolidation (OC)",
        formulaOrStatement = "σ'_o+Δσ' ≤ σ'_c: (C_s H/(1+e_o)) log[(σ'_o+Δσ')/σ'_o]  •  Else: C_s term(σ'_c/σ'_o) + C_c term((σ'_o+Δσ')/σ'_c)",
        note = "Swell/recompression index C_s ≈ (1/5 to 1/10) C_c; split calculation across preconsolidation pressure σ'_c.",
        subItems = listOf("Overconsolidation Ratio: OCR = σ'_c / σ'_o")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "2:1 Approximate Stress Distribution",
        formulaOrStatement = "Δσ' = P / [ (B + z) · (L + z) ]",
        note = "Rapid board-exam method to estimate vertical stress increment Δσ' at mid-height z of compressible clay stratum.",
        subItems = listOf("Circular footing of diameter D: Δσ' = P / [ (π/4) · (D + z)² ]")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Deep Foundations: Single Piles & Groups",
    ribbonLabel = "06 Deep Piles",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Ultimate Single Pile Capacity Qu",
        formulaOrStatement = "Q_u = Q_p + Q_s = (q_p · A_p) + Σ (f · p · ΔL)",
        note = "Allowable pile load Q_all = Q_u / FS (typically FS = 2.5 to 3.0); p = pile perimeter, A_p = tip area.",
        subItems = listOf("For open-ended pipe piles, check plugged vs. unplugged capacity"),
        codeTag = "NSCP 2015 Sec. 306.2"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Pile Point & Skin Friction in Sand (φ-Soil)",
        formulaOrStatement = "Q_p = A_p q' N*_q ≤ A_p (0.5 p_a N*_q tan φ)    •    f = K · σ'_v · tan δ",
        note = "Effective overburden σ'_v reaches critical limiting depth at ~15D to 20D in homogeneous sand.",
        subItems = listOf("Atmospheric pressure p_a ≈ 100 kPa  •  Driven pile K ≈ 1.0–1.5, δ ≈ 0.75 φ")
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Pile Capacity in Saturated Clay (α-Method)",
        formulaOrStatement = "Q_p = 9 · c_u · A_p    •    f = α · c_u   (Undrained φ = 0)",
        note = "Adhesion factor α ranges from 1.0 (soft clay c_u ≤ 25 kPa) down to ~0.4–0.5 (stiff clay).",
        subItems = listOf("Side resistance dominates friction piles in deep clay deposits"),
        codeTag = "NSCP 2015 Sec. 306.2"
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Engineering News Record (ENR) Dynamic Formula",
        formulaOrStatement = "Q_all = (W_R · h) / [ 6 · (S + C) ]",
        note = "Includes built-in FS = 6; C = 25.4 mm (1.0 in) for drop hammer, C = 2.54 mm (0.1 in) for steam/air hammer.",
        subItems = listOf("S = average pile penetration (set) per blow in mm; h in mm")
      ),
      AlgebraHandbookRuleItem(
        number = 5,
        title = "Group Efficiency & Block Capacity",
        formulaOrStatement = "η = 1 - θ · [ (n-1)m + (m-1)n ] / (90 m n)   [θ = arctan(d / s) in deg]",
        note = "Group block capacity in clay: Q_g(u) = L_g B_g c_u N*_c + 2(L_g + B_g) L c_u; use min(η · m·n·Q_u , Q_g(u)).",
        subItems = listOf("L_g = (n-1)s + d, B_g = (m-1)s + d  •  Min spacing s ≥ 2.5d to 3.0d"),
        codeTag = "NSCP 2015 Sec. 306.3"
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Lateral Earth Pressure (Rankine & Coulomb)",
    ribbonLabel = "07 Earth Press",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "At-Rest Earth Pressure Coefficient Ko",
        formulaOrStatement = "NC Soil: K_o = 1 - sin φ  (Jaky)    •    OC Soil: K_o(OC) = K_o(NC) · √OCR",
        note = "Used for braced basement walls and rigid bridge abutments unable to yield laterally.",
        subItems = listOf("At-rest lateral effective stress: σ'_o(h) = K_o · σ'_v"),
        codeTag = "NSCP 2015 Sec. 308.2"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Rankine Active & Passive (Level Backfill)",
        formulaOrStatement = "K_a = tan²(45° - φ/2) = (1 - sin φ)/(1 + sin φ)  •  K_p = (1 + sin φ)/(1 - sin φ)",
        note = "Notice K_a · K_p = 1.0; total active thrust for dry sand wall height H is P_a = 0.5 γ H² K_a acting at H/3 above base.",
        subItems = listOf("Wall movement required: Δ_active ≈ 0.001H–0.004H, Δ_passive ≈ 0.02H–0.05H"),
        codeTag = "NSCP 2015 Sec. 308.2"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Rankine Pressure in Cohesive (c-φ) Soil",
        formulaOrStatement = "σ'_a = σ'_v K_a - 2 c' √K_a   •   Tensile Crack Depth z_o = (2 c') / (γ √K_a)",
        note = "Passive earth pressure: σ'_p = σ'_v K_p + 2 c' √K_p. Neglect negative active tension zone from z = 0 to z_o.",
        subItems = listOf("Max unsupported vertical cut in cohesive soil: H_c = 2 z_o = 4c' / (γ √K_a)")
      ),
      AlgebraHandbookRuleItem(
        number = 4,
        title = "Rankine Inclined Backfill (Angle β)",
        formulaOrStatement = "K_a = cos β · [ cos β - √(cos²β - cos²φ) ] / [ cos β + √(cos²β - cos²φ) ]",
        note = "Resultant active thrust P_a acts parallel to the sloped backfill surface at angle β from horizontal.",
        subItems = listOf("Horizontal component: P_h = P_a cos β", "Vertical component: P_v = P_a sin β")
      )
    )
  ),
  AlgebraHandbookSection(
    sectionTitle = "Retaining Walls & Stability Checks",
    ribbonLabel = "08 Walls",
    items = listOf(
      AlgebraHandbookRuleItem(
        number = 1,
        title = "Overturning Stability Check (about Toe O)",
        formulaOrStatement = "FS_overturning = Σ M_R / Σ M_O ≥ 2.0",
        note = "Resisting moments ΣM_R include concrete wall stem/base + soil above heel (+ P_a sin α · B); overturning M_O = (P_a cos α) · ȳ.",
        subItems = listOf("Cohesive backfill often requires FS ≥ 2.0 to 3.0; usually ignore passive P_p or reduce safely"),
        codeTag = "NSCP 2015 Sec. 308.3"
      ),
      AlgebraHandbookRuleItem(
        number = 2,
        title = "Sliding Stability Check",
        formulaOrStatement = "FS_sliding = [ (ΣV) · tan(k₁ φ₂) + B · k₂ · c₂ + P_p ] / (P_a cos α) ≥ 1.5",
        note = "Typically k₁ = k₂ = 2/3 for concrete cast against soil; add a base shear key to mobilize passive resistance P_p.",
        subItems = listOf("If FS_sliding < 1.5 without P_p, add shear key or widen heel"),
        codeTag = "NSCP 2015 Sec. 308.3"
      ),
      AlgebraHandbookRuleItem(
        number = 3,
        title = "Base Eccentricity & Bearing Pressure Check",
        formulaOrStatement = "e = B/2 - (Σ M_R - Σ M_O) / ΣV ≤ B/6    •    q_toe,heel = (ΣV / B) · (1 ± 6e / B) ≤ q_all",
        note = "Keep resultant within middle third (e ≤ B/6) to prevent base uplift; verify FS_bearing = q_u / q_toe ≥ 3.0.",
        subItems = listOf("Toe has max pressure q_toe = (ΣV/B)(1 + 6e/B)", "Heel has q_heel = (ΣV/B)(1 - 6e/B)"),
        codeTag = "NSCP 2015 Sec. 308.3"
      )
    )
  )
)

@Composable
internal fun EngineeringSubVolumeHubView(
  pillar: EngineeringVolumePillar,
  subVolumes: List<EngineeringSubVolumeSpec>,
  searchQuery: String,
  customFormulas: List<ReferenceFormulaCardData>,
  starredIds: Set<String>,
  onToggleStar: (String) -> Unit,
  metrics: ResponsiveMetrics,
  onBreadcrumbClick: () -> Unit,
  onOpenSubVolume: (EngineeringSubVolumeSpec) -> Unit,
  modifier: Modifier = Modifier
) {
  val palette = currentGlassPalette()
  val filteredSubVolumes = remember(subVolumes, searchQuery) {
    if (searchQuery.isEmpty()) {
      subVolumes
    } else {
      subVolumes.filter { spec ->
        spec.title.contains(searchQuery, ignoreCase = true) ||
          spec.compactSummaryTag.contains(searchQuery, ignoreCase = true) ||
          spec.tagBadge.contains(searchQuery, ignoreCase = true) ||
          spec.searchKeywords.contains(searchQuery, ignoreCase = true) ||
          spec.sections.any { sec ->
            sec.sectionTitle.contains(searchQuery, ignoreCase = true) ||
              sec.items.any { item ->
                item.title.contains(searchQuery, ignoreCase = true) ||
                  item.formulaOrStatement.contains(searchQuery, ignoreCase = true)
              }
          }
      }
    }
  }

  val pillarCustomFormulas = remember(customFormulas, pillar, searchQuery) {
    customFormulas.filter { f ->
      val matchesPillar = f.categoryTab.contains(pillar.code, ignoreCase = true) ||
        f.mathSubTab.contains(pillar.code, ignoreCase = true)
      val matchesSearch = searchQuery.isEmpty() ||
        f.title.contains(searchQuery, ignoreCase = true) ||
        f.equationBanner.contains(searchQuery, ignoreCase = true)
      matchesPillar && matchesSearch
    }
  }

  val headerAccent = if (pillar == EngineeringVolumePillar.PSAD) Color(0xFFF59E0B) else Color(0xFF06B6D4)
  val orientation = rememberScreenOrientationState()

  LazyVerticalGrid(
    columns = if (metrics.screenWidthDp >= 600 || orientation.isLandscape) {
      GridCells.Adaptive(minSize = 260.dp)
    } else {
      GridCells.Fixed(1)
    },
    modifier = modifier
      .widthIn(max = orientation.wideMaxContentWidth)
      .padding(bottom = 96.dp)
      .testTag("${pillar.code.lowercase(Locale.ROOT)}_sub_hub_grid"),
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Breadcrumb Chip back to All Master Volumes (Clean Algebra-Style)
    item(span = { GridItemSpan(maxLineSpan) }) {
      Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
          onClick = onBreadcrumbClick,
          shape = RoundedCornerShape(10.dp),
          color = palette.cardFill,
          border = palette.glassBorder,
          modifier = Modifier.testTag("${pillar.code.lowercase(Locale.ROOT)}_back_to_master_chip")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
              contentDescription = "Back to Master Volumes",
              tint = headerAccent,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "All PRC Board Pillars (MSTE • PSAD • HPGE)",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = AppleSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
              ),
              color = headerAccent
            )
          }
        }
      }
    }

    // Clean Shelf Section Header (Fixed Zero Overlap: weight(1f) + maxLines = 1)
    item(span = { GridItemSpan(maxLineSpan) }) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = pillar.shelfHeader,
          style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp
          ),
          color = palette.textSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .weight(1f)
            .padding(end = 10.dp)
        )

        Text(
          text = "${subVolumes.size} SUB-VOLUMES",
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          ),
          color = headerAccent,
          maxLines = 1,
          softWrap = false
        )
      }
    }

    if (filteredSubVolumes.isEmpty()) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        LiquidGlassCard(
          cornerRadius = 18.dp,
          elevation = 4.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No ${pillar.code} sub-volumes match \"$searchQuery\".",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = palette.textSecondary,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
          )
        }
      }
    } else {
      items(
        items = filteredSubVolumes,
        key = { it.id }
      ) { spec ->
        EngineeringSubVolumeTopicCard(
          spec = spec,
          metrics = metrics,
          onClick = { onOpenSubVolume(spec) }
        )
      }
    }

    if (pillarCustomFormulas.isNotEmpty()) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
          text = "CUSTOM ${pillar.code} FORMULAS (${pillarCustomFormulas.size})",
          style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp
          ),
          color = headerAccent,
          modifier = Modifier.padding(top = 6.dp)
        )
      }
      items(
        items = pillarCustomFormulas,
        key = { it.id },
        span = { GridItemSpan(maxLineSpan) }
      ) { formula ->
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

@Composable
private fun EngineeringSubVolumeTopicCard(
  spec: EngineeringSubVolumeSpec,
  metrics: ResponsiveMetrics,
  onClick: () -> Unit
) {
  val palette = currentGlassPalette()
  val cardShape = RoundedCornerShape(
    topStart = 12.dp,
    bottomStart = 12.dp,
    topEnd = 22.dp,
    bottomEnd = 22.dp
  )

  Surface(
    onClick = onClick,
    shape = cardShape,
    color = if (palette.isDark) {
      Color(0xFF1A1F2C).copy(alpha = 0.94f)
    } else {
      Color(0xFFFFFFFF).copy(alpha = 0.94f)
    },
    border = BorderStroke(
      width = 1.25.dp,
      brush = Brush.linearGradient(
        listOf(
          spec.accentColor.copy(alpha = 0.65f),
          Color.White.copy(alpha = if (palette.isDark) 0.22f else 0.72f)
        )
      )
    ),
    shadowElevation = 7.dp,
    modifier = Modifier
      .fillMaxWidth()
      .minimumInteractiveComponentSize()
      .testTag("sub_volume_card_${spec.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Frosted Binder Spine Accent
      Box(
        modifier = Modifier
          .width(12.dp)
          .fillMaxHeight()
          .background(
            brush = Brush.verticalGradient(
              listOf(spec.accentColor, spec.secondaryAccentColor)
            )
          )
      ) {
        Box(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .width(1.5.dp)
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.16f))
        )
      }

      // Decluttered Algebra-Style Content: Title + Badge, Compact 1-Line Count Tag + Chevron
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = spec.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = AppleSansFamily,
              fontSize = metrics.scaledSp(17),
              lineHeight = metrics.scaledSp(22),
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.3).sp
            ),
            color = palette.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f)
              .padding(end = 10.dp)
          )

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = spec.accentColor.copy(alpha = if (palette.isDark) 0.18f else 0.13f),
            border = BorderStroke(0.9.dp, spec.accentColor.copy(alpha = 0.48f))
          ) {
            Text(
              text = spec.tagBadge,
              fontFamily = JetBrainsMonoFamily,
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1,
              softWrap = false,
              color = spec.accentColor,
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = spec.compactSummaryTag,
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = AppleSansFamily,
              fontSize = metrics.scaledSp(13),
              fontWeight = FontWeight.Medium
            ),
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f)
              .padding(end = 10.dp)
          )

          Surface(
            shape = CircleShape,
            color = spec.accentColor.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, spec.accentColor.copy(alpha = 0.36f)),
            modifier = Modifier.size(34.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open ${spec.title}",
                tint = spec.accentColor,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
internal fun EngineeringSubVolumeSheet(
  spec: EngineeringSubVolumeSpec,
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
  var selectedSectionForDetailModal by remember { mutableStateOf<Pair<Int, AlgebraHandbookSection>?>(null) }

  val filteredSections = remember(spec, searchQuery) {
    if (searchQuery.isEmpty()) {
      spec.sections.mapIndexed { idx, sec -> idx to sec }
    } else {
      spec.sections.mapIndexedNotNull { idx, section ->
        if (section.sectionTitle.contains(searchQuery, ignoreCase = true) ||
          section.ribbonLabel.contains(searchQuery, ignoreCase = true)
        ) {
          idx to section
        } else {
          val matchingItems = section.items.filter { item ->
            item.title.contains(searchQuery, ignoreCase = true) ||
              item.formulaOrStatement.contains(searchQuery, ignoreCase = true) ||
              item.note.contains(searchQuery, ignoreCase = true) ||
              item.codeTag.contains(searchQuery, ignoreCase = true) ||
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

  val orientation = rememberScreenOrientationState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .widthIn(max = orientation.wideMaxContentWidth)
      .testTag("engineering_sub_volume_sheet_${spec.id}"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Sticky Quick-Jump Section Ribbon (matches MSTE Algebra UI)
    if (filteredSections.isNotEmpty()) {
      StickyFormulaSectionRibbon(
        sections = filteredSections,
        activeSectionIndex = activeRibbonSectionIndex,
        accentColor = spec.accentColor,
        onSelectSection = { listIdx ->
          playCardHapticSound(context, localView)
          coroutineScope.launch {
            lazyListState.animateScrollToItem(index = listIdx + 1)
          }
        },
        testTagPrefix = "${spec.id}_ribbon"
      )
    }

    // 2. Scrollable Sub-Volume Formula Chapters
    LazyColumn(
      state = lazyListState,
      contentPadding = PaddingValues(bottom = 120.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      // ITEM 0: Clean Algebra-Style Breadcrumb + TOC Row (Zero Long Paragraph Clutter)
      item(key = "${spec.id}_header_item_0") {
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
              modifier = Modifier.testTag("${spec.id}_back_to_library_chip")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                  contentDescription = "Back to ${spec.pillar.libraryTitle}",
                  tint = spec.accentColor,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = spec.pillar.libraryTitle,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = AppleSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                  ),
                  color = spec.accentColor
                )
              }
            }

            Surface(
              onClick = onOpenToc,
              shape = RoundedCornerShape(10.dp),
              color = palette.cardFill,
              border = BorderStroke(1.dp, spec.accentColor.copy(alpha = 0.42f)),
              modifier = Modifier.testTag("${spec.id}_toc_pill_button")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ViewList,
                  contentDescription = "Table of Contents",
                  tint = spec.accentColor,
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "${spec.tagBadge} • ${spec.sections.size} SECTIONS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                  ),
                  color = spec.accentColor
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
                text = "No ${spec.shortTitle} formulas match \"$searchQuery\".",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = palette.textSecondary,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
              )
            }
          }
        }
      }

      // ITEMS 1..N: Numbered Liquid Glass Section Cards + CAD Blueprints + MathFormulaCapsule items
      items(
        count = filteredSections.size,
        key = { idx -> "${spec.id}_section_card_${filteredSections[idx].first + 1}" }
      ) { listIdx ->
        val (originalSectionIndex, section) = filteredSections[listIdx]
        val sectionAccent = if (originalSectionIndex % 2 == 0) spec.accentColor else spec.secondaryAccentColor
        val isHighlighted = highlightedSectionIndex == originalSectionIndex
        val badgeText = String.format(Locale.US, "%02d", originalSectionIndex + 1)
        val hasBlueprint = psadSectionHasInteractiveBlueprint(spec.id, originalSectionIndex)

        val borderAlpha by animateFloatAsState(
          targetValue = if (isHighlighted) 0.90f else 0.42f,
          animationSpec = tween(durationMillis = 420),
          label = "${spec.id}_sec_border_alpha"
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
            .testTag("${spec.id}_section_${originalSectionIndex + 1}")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
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
                      fontFamily = FontFamily.Monospace,
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
                    fontFamily = AppleSansFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.25).sp
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
                  text = psadSectionHeaderBadgeLabel(spec.id, originalSectionIndex, section.items.size),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
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

            // Interactive CAD Blueprint / Determinacy Calculator when applicable
            if (hasBlueprint) {
              PsadInteractiveSectionContent(
                subVolumeId = spec.id,
                sectionIndex = originalSectionIndex,
                accentColor = sectionAccent
              )
            }

            // Crisp MathFormulaCapsule List
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
                  codeTag = rule.codeTag.ifBlank { null },
                  accentColor = sectionAccent,
                  testTag = "${spec.id}_formula_capsule_${originalSectionIndex + 1}_${ruleIdx + 1}"
                )
              }
            }
          }
        }
      }

      // Custom Scholar Formulas matching this sub-volume or pillar
      if (customFormulas.isNotEmpty()) {
        item(key = "${spec.id}_custom_formulas_footer") {
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
    EngineeringSectionDetailGlassModal(
      spec = spec,
      sectionNumber = secIndex + 1,
      section = secData,
      onDismiss = { selectedSectionForDetailModal = null }
    )
  }
}

@Composable
private fun EngineeringSectionDetailGlassModal(
  spec: EngineeringSubVolumeSpec,
  sectionNumber: Int,
  section: AlgebraHandbookSection,
  onDismiss: () -> Unit
) {
  val palette = currentGlassPalette()
  val accentColor = spec.accentColor

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.56f))
        .clickable(onClick = onDismiss),
      contentAlignment = Alignment.Center
    ) {
      val modalOrientation = rememberScreenOrientationState()
      Surface(
        onClick = {},
        shape = RoundedCornerShape(28.dp),
        color = palette.dialogSurface,
        border = BorderStroke(
          width = 1.dp,
          brush = Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.12f))
          )
        ),
        shadowElevation = 24.dp,
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .widthIn(max = if (modalOrientation.isLandscape) 820.dp else 520.dp)
          .heightIn(max = if (modalOrientation.isCompactLandscape) 380.dp else 680.dp)
          .testTag("${spec.id}_section_detail_modal")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = String.format(Locale.US, "%s • CHAPTER %02d", spec.tagBadge, sectionNumber),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 1.sp
                ),
                color = accentColor
              )
              Text(
                text = section.sectionTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontFamily = AppleSansFamily,
                  fontSize = 19.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = (-0.35).sp
                ),
                color = palette.textPrimary
              )
            }

            Surface(
              onClick = onDismiss,
              shape = CircleShape,
              color = palette.nestedCardFill,
              border = palette.glassBorder,
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close chapter detail",
                  tint = palette.textSecondary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          HorizontalDivider(color = accentColor.copy(alpha = 0.25f))

          section.items.forEachIndexed { idx, item ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              MathFormulaCapsule(
                label = item.title,
                equation = item.formulaOrStatement,
                secondaryEquation = if (item.subItems.isNotEmpty()) item.subItems.joinToString("  |  ") else null,
                codeTag = item.codeTag.ifBlank { null },
                accentColor = accentColor,
                testTag = "${spec.id}_modal_capsule_${sectionNumber}_${idx + 1}"
              )
              if (item.note.isNotBlank()) {
                Text(
                  text = item.note,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = AppleSansFamily,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                  ),
                  color = palette.textSecondary,
                  modifier = Modifier.padding(horizontal = 4.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

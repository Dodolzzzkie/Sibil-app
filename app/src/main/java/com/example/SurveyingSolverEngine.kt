package com.example

import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.tan

// ============================================================================
// SIBIL CE • SURVEYING & CURVE SOLVER ENGINE (CELE MSTE • PHILIPPINE 20m BASIS)
// ============================================================================

internal enum class SurveyingToolTab(val id: String, val title: String) {
  TRAVERSE_DMD("traverse_dmd", "Traverse & DMD Area"),
  SIMPLE_CURVE("simple_curve", "Simple Horizontal Curve"),
  COMPOUND_CURVE("compound_curve", "Compound Curve"),
  VERTICAL_CURVE("vertical_curve", "Vertical Parabolic Curve"),
  STADIA_LEVELING("stadia_leveling", "Leveling & Stadia"),
  EARTHWORK_VOLUMES("earthwork_volumes", "Earthwork Volumes")
}

// ----------------------------------------------------------------------------
// Formatting Helpers (Metric Stationing & DMS)
// ----------------------------------------------------------------------------

internal fun formatMetricStation(stationMeters: Double): String {
  val sign = if (stationMeters < 0.0) "-" else ""
  val absVal = abs(stationMeters)
  val km = floor(absVal / 1000.0).toLong()
  val rem = absVal - km * 1000.0
  val remStr = String.format(Locale.US, "%06.2f", rem)
  return "${sign}Sta. ${km}+${remStr}"
}

internal fun formatDmsAngle(degDecimal: Double): String {
  val sign = if (degDecimal < 0.0) "-" else ""
  val absDeg = abs(degDecimal)
  var d = floor(absDeg).toInt()
  val remMin = (absDeg - d) * 60.0
  var m = floor(remMin).toInt()
  var s = (remMin - m) * 60.0
  if (s >= 59.995) {
    s = 0.0
    m += 1
  }
  if (m >= 60) {
    m = 0
    d += 1
  }
  return String.format(Locale.US, "%s%d° %02d' %05.2f\"", sign, d, m, s)
}

internal fun fmtNum(value: Double, decimals: Int = 3): String {
  if (value.isNaN() || value.isInfinite()) return "—"
  return String.format(Locale.US, "%,.${decimals}f", value)
}

// ----------------------------------------------------------------------------
// 1. CLOSED TRAVERSE & DMD AREA
// ----------------------------------------------------------------------------

internal enum class TraverseAdjustmentRule(val label: String) {
  COMPASS("Compass Rule (Bowditch)"),
  TRANSIT("Transit Rule")
}

internal data class TraverseCourseInput(
  val id: Long,
  val distanceText: String,
  val ns: String, // "N" or "S"
  val degText: String,
  val minText: String,
  val ew: String // "E" or "W"
)

internal data class TraverseCourseResult(
  val lineLabel: String,
  val distance: Double,
  val bearingText: String,
  val latRaw: Double,
  val depRaw: Double,
  val corrLat: Double,
  val corrDep: Double,
  val latBal: Double,
  val depBal: Double,
  val dmd: Double,
  val doubleAreaTerm: Double
)

internal data class TraverseVertex(
  val label: String,
  val x: Double,
  val y: Double
)

internal data class TraverseComputationResult(
  val isValid: Boolean,
  val errorMessage: String? = null,
  val rule: TraverseAdjustmentRule = TraverseAdjustmentRule.COMPASS,
  val courses: List<TraverseCourseResult> = emptyList(),
  val vertices: List<TraverseVertex> = emptyList(),
  val sumLat: Double = 0.0,
  val sumDep: Double = 0.0,
  val perimeter: Double = 0.0,
  val lec: Double = 0.0,
  val errorBearingText: String = "—",
  val relativePrecisionText: String = "—",
  val sumDoubleArea: Double = 0.0,
  val areaSqm: Double = 0.0,
  val areaHectares: Double = 0.0,
  val copySummary: String = ""
)

internal fun computeClosedTraverseDmd(
  inputs: List<TraverseCourseInput>,
  rule: TraverseAdjustmentRule
): TraverseComputationResult {
  if (inputs.size < 3) {
    return TraverseComputationResult(
      isValid = false,
      errorMessage = "At least 3 courses are required to form a closed traverse."
    )
  }

  data class RawCourse(
    val label: String,
    val d: Double,
    val bearingStr: String,
    val lat: Double,
    val dep: Double
  )

  val rawList = mutableListOf<RawCourse>()
  for (i in inputs.indices) {
    val item = inputs[i]
    val nextIdx = if (i == inputs.lastIndex) 1 else i + 2
    val label = "Line ${i + 1}–$nextIdx"
    val d = item.distanceText.toDoubleOrNull()
    val deg = item.degText.toDoubleOrNull()
    val min = item.minText.ifBlank { "0" }.toDoubleOrNull()
    if (d == null || d <= 0.0 || deg == null || deg < 0.0 || deg > 90.0 || min == null || min < 0.0 || min >= 60.0) {
      return TraverseComputationResult(
        isValid = false,
        errorMessage = "Enter valid positive Distance, Deg (0–90), and Min (0–59.9) for $label."
      )
    }
    val totalDeg = deg + min / 60.0
    if (totalDeg > 90.0 + 1e-6) {
      return TraverseComputationResult(
        isValid = false,
        errorMessage = "Quadrant bearing for $label cannot exceed 90° 00'."
      )
    }
    val rad = totalDeg * PI / 180.0
    val latSign = if (item.ns == "S") -1.0 else 1.0
    val depSign = if (item.ew == "W") -1.0 else 1.0
    val lat = latSign * d * cos(rad)
    val dep = depSign * d * sin(rad)
    val minFmt = if (min == floor(min)) String.format(Locale.US, "%02d'", min.toInt())
    else String.format(Locale.US, "%04.1f'", min)
    val bearingStr = "${item.ns} ${deg.toInt()}° $minFmt ${item.ew}"
    rawList.add(RawCourse(label, d, bearingStr, lat, dep))
  }

  val perimeter = rawList.sumOf { it.d }
  val sumLat = rawList.sumOf { it.lat }
  val sumDep = rawList.sumOf { it.dep }
  val sumAbsLat = rawList.sumOf { abs(it.lat) }
  val sumAbsDep = rawList.sumOf { abs(it.dep) }
  val lec = hypot(sumLat, sumDep)

  val errorBearingText = if (lec < 1e-9) {
    "Closed (0.000 m)"
  } else {
    val nsErr = if (sumLat >= 0.0) "N" else "S"
    val ewErr = if (sumDep >= 0.0) "E" else "W"
    val angleDeg = if (abs(sumLat) < 1e-12) 90.0 else atan(abs(sumDep / sumLat)) * 180.0 / PI
    "$nsErr ${formatDmsAngle(angleDeg)} $ewErr"
  }

  val relativePrecisionText = if (lec < 1e-9) {
    "Exact Closure"
  } else {
    val denom = floor(perimeter / lec).toLong().coerceAtLeast(1L)
    String.format(Locale.US, "1 : %,d", denom)
  }

  val courseResults = mutableListOf<TraverseCourseResult>()
  val vertices = mutableListOf(TraverseVertex("1", 0.0, 0.0))
  var curX = 0.0
  var curY = 0.0
  var prevDmd = 0.0
  var prevDepBal = 0.0
  var sumDoubleArea = 0.0

  rawList.forEachIndexed { idx, raw ->
    val cLat = when (rule) {
      TraverseAdjustmentRule.COMPASS -> if (perimeter > 0.0) -sumLat * (raw.d / perimeter) else 0.0
      TraverseAdjustmentRule.TRANSIT -> if (sumAbsLat > 1e-12) -sumLat * (abs(raw.lat) / sumAbsLat) else 0.0
    }
    val cDep = when (rule) {
      TraverseAdjustmentRule.COMPASS -> if (perimeter > 0.0) -sumDep * (raw.d / perimeter) else 0.0
      TraverseAdjustmentRule.TRANSIT -> if (sumAbsDep > 1e-12) -sumDep * (abs(raw.dep) / sumAbsDep) else 0.0
    }
    val latBal = raw.lat + cLat
    val depBal = raw.dep + cDep
    val dmd = if (idx == 0) depBal else prevDmd + prevDepBal + depBal
    val doubleAreaTerm = dmd * latBal
    sumDoubleArea += doubleAreaTerm
    prevDmd = dmd
    prevDepBal = depBal

    curX += depBal
    curY += latBal
    if (idx < rawList.lastIndex) {
      vertices.add(TraverseVertex("${idx + 2}", curX, curY))
    }

    courseResults.add(
      TraverseCourseResult(
        lineLabel = raw.label,
        distance = raw.d,
        bearingText = raw.bearingStr,
        latRaw = raw.lat,
        depRaw = raw.dep,
        corrLat = cLat,
        corrDep = cDep,
        latBal = latBal,
        depBal = depBal,
        dmd = dmd,
        doubleAreaTerm = doubleAreaTerm
      )
    )
  }

  val twoA = abs(sumDoubleArea)
  val areaSqm = twoA / 2.0
  val areaHa = areaSqm / 10000.0

  val sb = StringBuilder()
  sb.appendLine("SIBIL CE • CLOSED TRAVERSE & DMD AREA (${rule.label})")
  sb.appendLine("Perimeter (ΣD) = ${fmtNum(perimeter, 3)} m")
  sb.appendLine("ΣLat = ${fmtNum(sumLat, 4)} m | ΣDep = ${fmtNum(sumDep, 4)} m")
  sb.appendLine("LEC = ${fmtNum(lec, 4)} m | Error Bearing = $errorBearingText")
  sb.appendLine("Relative Precision = $relativePrecisionText")
  sb.appendLine("----------------------------------------")
  courseResults.forEach { c ->
    sb.appendLine(
      "${c.lineLabel} (${c.bearingText}, ${fmtNum(c.distance, 2)} m) -> " +
        "Bal Lat=${fmtNum(c.latBal, 3)}, Bal Dep=${fmtNum(c.depBal, 3)}, DMD=${fmtNum(c.dmd, 3)}, 2A=${fmtNum(c.doubleAreaTerm, 3)}"
    )
  }
  sb.appendLine("----------------------------------------")
  sb.appendLine("2A = ${fmtNum(twoA, 3)} m²")
  sb.appendLine("Area = ${fmtNum(areaSqm, 3)} m² (${fmtNum(areaHa, 5)} ha)")

  return TraverseComputationResult(
    isValid = true,
    rule = rule,
    courses = courseResults,
    vertices = vertices,
    sumLat = sumLat,
    sumDep = sumDep,
    perimeter = perimeter,
    lec = lec,
    errorBearingText = errorBearingText,
    relativePrecisionText = relativePrecisionText,
    sumDoubleArea = twoA,
    areaSqm = areaSqm,
    areaHectares = areaHa,
    copySummary = sb.toString().trim()
  )
}

// ----------------------------------------------------------------------------
// 2. SIMPLE CIRCULAR HORIZONTAL CURVE (PHILIPPINE 20m STATIONING)
// ----------------------------------------------------------------------------

internal enum class SimpleCurveInputMode(val label: String) {
  R_AND_I("R & I"),
  D_AND_I("D & I"),
  T_AND_I("T & I")
}

internal enum class CurveStationBasis(val label: String) {
  ARC("Arc Basis"),
  CHORD("Chord Basis")
}

internal data class SimpleCurveResult(
  val isValid: Boolean,
  val errorMessage: String? = null,
  val basis: CurveStationBasis = CurveStationBasis.ARC,
  val radius: Double = 0.0,
  val degreeD: Double = 0.0,
  val angleI: Double = 0.0,
  val tangentT: Double = 0.0,
  val lengthLc: Double = 0.0,
  val longChordLc: Double = 0.0,
  val externalE: Double = 0.0,
  val middleOrdinateM: Double = 0.0,
  val staPi: Double? = null,
  val staPc: Double? = null,
  val staPt: Double? = null,
  val copySummary: String = ""
)

internal fun computeSimpleHorizontalCurve(
  mode: SimpleCurveInputMode,
  basis: CurveStationBasis,
  primaryText: String, // R, D, or T
  angleIText: String,
  piStationText: String
): SimpleCurveResult {
  val primary = primaryText.toDoubleOrNull()
  val angleI = angleIText.toDoubleOrNull()
  if (primary == null || primary <= 0.0 || angleI == null || angleI <= 0.0 || angleI >= 180.0) {
    return SimpleCurveResult(
      isValid = false,
      errorMessage = "Enter positive primary parameter and deflection angle I between 0° and 180°."
    )
  }

  val iRad = angleI * PI / 180.0
  val radius: Double
  val degreeD: Double

  when (mode) {
    SimpleCurveInputMode.R_AND_I -> {
      radius = primary
      if (basis == CurveStationBasis.ARC) {
        degreeD = 3600.0 / (PI * radius) // 1145.91559026 / R
      } else {
        if (radius < 10.0) {
          return SimpleCurveResult(
            isValid = false,
            errorMessage = "For 20m Chord Basis, Radius R must be at least 10.0 m."
          )
        }
        degreeD = 2.0 * asin(10.0 / radius) * 180.0 / PI
      }
    }
    SimpleCurveInputMode.D_AND_I -> {
      degreeD = primary
      if (degreeD <= 0.0 || degreeD >= 180.0) {
        return SimpleCurveResult(
          isValid = false,
          errorMessage = "Degree of Curve D must be between 0° and 180°."
        )
      }
      radius = if (basis == CurveStationBasis.ARC) {
        3600.0 / (PI * degreeD)
      } else {
        10.0 / sin((degreeD * PI / 180.0) / 2.0)
      }
    }
    SimpleCurveInputMode.T_AND_I -> {
      val t = primary
      radius = t / tan(iRad / 2.0)
      if (basis == CurveStationBasis.ARC) {
        degreeD = 3600.0 / (PI * radius)
      } else {
        if (radius < 10.0) {
          return SimpleCurveResult(
            isValid = false,
            errorMessage = "Computed Radius R (${fmtNum(radius, 2)} m) is under 10.0 m for 20m Chord Basis."
          )
        }
        degreeD = 2.0 * asin(10.0 / radius) * 180.0 / PI
      }
    }
  }

  val tangentT = radius * tan(iRad / 2.0)
  val lengthLc = if (basis == CurveStationBasis.ARC) {
    PI * radius * angleI / 180.0
  } else {
    20.0 * (angleI / degreeD)
  }
  val longChord = 2.0 * radius * sin(iRad / 2.0)
  val externalE = radius * ((1.0 / cos(iRad / 2.0)) - 1.0)
  val middleM = radius * (1.0 - cos(iRad / 2.0))

  val piParsed = piStationText.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
  if (piStationText.isNotBlank() && piParsed == null) {
    return SimpleCurveResult(
      isValid = false,
      errorMessage = "PI Station must be numeric meters (e.g., 10450.25)."
    )
  }
  val pcSta = piParsed?.let { it - tangentT }
  val ptSta = pcSta?.let { it + lengthLc }

  val sb = StringBuilder()
  sb.appendLine("SIBIL CE • SIMPLE HORIZONTAL CURVE (${basis.label}, 20m)")
  sb.appendLine("R = ${fmtNum(radius, 3)} m")
  sb.appendLine("D = ${formatDmsAngle(degreeD)} (${fmtNum(degreeD, 4)}°)")
  sb.appendLine("I = ${formatDmsAngle(angleI)}")
  sb.appendLine("T = ${fmtNum(tangentT, 3)} m")
  sb.appendLine("Lc = ${fmtNum(lengthLc, 3)} m")
  sb.appendLine("Long Chord (LC) = ${fmtNum(longChord, 3)} m")
  sb.appendLine("External (E) = ${fmtNum(externalE, 3)} m")
  sb.appendLine("Middle Ordinate (M) = ${fmtNum(middleM, 3)} m")
  if (piParsed != null && pcSta != null && ptSta != null) {
    sb.appendLine("PI = ${formatMetricStation(piParsed)}")
    sb.appendLine("PC = ${formatMetricStation(pcSta)}")
    sb.appendLine("PT = ${formatMetricStation(ptSta)}")
  }

  return SimpleCurveResult(
    isValid = true,
    basis = basis,
    radius = radius,
    degreeD = degreeD,
    angleI = angleI,
    tangentT = tangentT,
    lengthLc = lengthLc,
    longChordLc = longChord,
    externalE = externalE,
    middleOrdinateM = middleM,
    staPi = piParsed,
    staPc = pcSta,
    staPt = ptSta,
    copySummary = sb.toString().trim()
  )
}

// ----------------------------------------------------------------------------
// 3. COMPOUND CURVE
// ----------------------------------------------------------------------------

internal data class CompoundCurveResult(
  val isValid: Boolean,
  val errorMessage: String? = null,
  val r1: Double = 0.0,
  val i1: Double = 0.0,
  val r2: Double = 0.0,
  val i2: Double = 0.0,
  val totalI: Double = 0.0,
  val t1Small: Double = 0.0,
  val t2Small: Double = 0.0,
  val commonTangentTc: Double = 0.0,
  val t1Long: Double = 0.0,
  val t2Long: Double = 0.0,
  val lc1: Double = 0.0,
  val lc2: Double = 0.0,
  val totalLength: Double = 0.0,
  val staPi: Double? = null,
  val staPc: Double? = null,
  val staPcc: Double? = null,
  val staPt: Double? = null,
  val copySummary: String = ""
)

internal fun computeCompoundCurve(
  r1Text: String,
  i1Text: String,
  r2Text: String,
  i2Text: String,
  piStationText: String
): CompoundCurveResult {
  val r1 = r1Text.toDoubleOrNull()
  val i1 = i1Text.toDoubleOrNull()
  val r2 = r2Text.toDoubleOrNull()
  val i2 = i2Text.toDoubleOrNull()

  if (r1 == null || r1 <= 0.0 || r2 == null || r2 <= 0.0 ||
    i1 == null || i1 <= 0.0 || i2 == null || i2 <= 0.0 || (i1 + i2) >= 180.0
  ) {
    return CompoundCurveResult(
      isValid = false,
      errorMessage = "Enter positive R₁, R₂, I₁, I₂ with total deflection I = I₁ + I₂ < 180°."
    )
  }

  val piParsed = piStationText.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
  if (piStationText.isNotBlank() && piParsed == null) {
    return CompoundCurveResult(
      isValid = false,
      errorMessage = "PI Station must be numeric meters (e.g., 5240.00)."
    )
  }

  val totalI = i1 + i2
  val i1Rad = i1 * PI / 180.0
  val i2Rad = i2 * PI / 180.0
  val iRad = totalI * PI / 180.0

  val t1Small = r1 * tan(i1Rad / 2.0)
  val t2Small = r2 * tan(i2Rad / 2.0)
  val tc = t1Small + t2Small
  val sinI = sin(iRad)
  val t1Long = t1Small + tc * sin(i2Rad) / sinI
  val t2Long = t2Small + tc * sin(i1Rad) / sinI
  val lc1 = PI * r1 * i1 / 180.0
  val lc2 = PI * r2 * i2 / 180.0
  val totalLength = lc1 + lc2

  val staPc = piParsed?.let { it - t1Long }
  val staPcc = staPc?.let { it + lc1 }
  val staPt = staPcc?.let { it + lc2 }

  val sb = StringBuilder()
  sb.appendLine("SIBIL CE • COMPOUND CURVE SOLVER")
  sb.appendLine("I = I₁ + I₂ = ${formatDmsAngle(totalI)}")
  sb.appendLine("t₁ = ${fmtNum(t1Small, 3)} m | t₂ = ${fmtNum(t2Small, 3)} m | Tc = ${fmtNum(tc, 3)} m")
  sb.appendLine("T₁ (PI–PC) = ${fmtNum(t1Long, 3)} m | T₂ (PI–PT) = ${fmtNum(t2Long, 3)} m")
  sb.appendLine("Lc₁ = ${fmtNum(lc1, 3)} m | Lc₂ = ${fmtNum(lc2, 3)} m | Total L = ${fmtNum(totalLength, 3)} m")
  if (piParsed != null && staPc != null && staPcc != null && staPt != null) {
    sb.appendLine("PC = ${formatMetricStation(staPc)}")
    sb.appendLine("PCC = ${formatMetricStation(staPcc)}")
    sb.appendLine("PT = ${formatMetricStation(staPt)}")
  }

  return CompoundCurveResult(
    isValid = true,
    r1 = r1,
    i1 = i1,
    r2 = r2,
    i2 = i2,
    totalI = totalI,
    t1Small = t1Small,
    t2Small = t2Small,
    commonTangentTc = tc,
    t1Long = t1Long,
    t2Long = t2Long,
    lc1 = lc1,
    lc2 = lc2,
    totalLength = totalLength,
    staPi = piParsed,
    staPc = staPc,
    staPcc = staPcc,
    staPt = staPt,
    copySummary = sb.toString().trim()
  )
}

// ----------------------------------------------------------------------------
// 4. SYMMETRICAL VERTICAL PARABOLIC CURVE
// ----------------------------------------------------------------------------

internal data class VerticalCurveResult(
  val isValid: Boolean,
  val errorMessage: String? = null,
  val g1: Double = 0.0,
  val g2: Double = 0.0,
  val lengthL: Double = 0.0,
  val curveType: String = "",
  val isSummit: Boolean = true,
  val rateR: Double = 0.0,
  val staPvc: Double = 0.0,
  val elevPvc: Double = 0.0,
  val staPvi: Double = 0.0,
  val elevPvi: Double = 0.0,
  val staPvt: Double = 0.0,
  val elevPvt: Double = 0.0,
  val middleOffsetH: Double = 0.0,
  val hasExtremum: Boolean = true,
  val xExt: Double = 0.0,
  val isExtWithinCurve: Boolean = true,
  val staExt: Double = 0.0,
  val elevExt: Double = 0.0,
  val querySta: Double? = null,
  val queryElev: Double? = null,
  val queryOffsetY: Double? = null,
  val queryWithinCurve: Boolean = true,
  val copySummary: String = ""
)

internal fun computeVerticalParabolicCurve(
  g1Text: String,
  g2Text: String,
  lengthLText: String,
  staPviText: String,
  elevPviText: String,
  queryStaText: String
): VerticalCurveResult {
  val g1 = g1Text.toDoubleOrNull()
  val g2 = g2Text.toDoubleOrNull()
  val lengthL = lengthLText.toDoubleOrNull()
  val staPvi = staPviText.toDoubleOrNull()
  val elevPvi = elevPviText.toDoubleOrNull()

  if (g1 == null || g2 == null || lengthL == null || lengthL <= 0.0 || staPvi == null || elevPvi == null) {
    return VerticalCurveResult(
      isValid = false,
      errorMessage = "Enter numeric grades g₁%, g₂%, positive length L (m), Sta PVI, and Elev PVI."
    )
  }

  val querySta = queryStaText.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
  if (queryStaText.isNotBlank() && querySta == null) {
    return VerticalCurveResult(
      isValid = false,
      errorMessage = "Query Station must be numeric meters."
    )
  }

  val isSummit = g1 > g2
  val curveType = when {
    g1 > g2 -> "Summit / Crest Curve"
    g1 < g2 -> "Sag Curve"
    else -> "Constant Grade (Zero Curvature)"
  }

  val rateR = (g2 - g1) / lengthL // % per meter
  val halfL = lengthL / 2.0
  val staPvc = staPvi - halfL
  val staPvt = staPvi + halfL
  val elevPvc = elevPvi - (g1 / 100.0) * halfL
  val elevPvt = elevPvi + (g2 / 100.0) * halfL
  val middleOffsetH = (g1 - g2) * lengthL / 800.0

  val dg = g2 - g1
  val hasExtremum = abs(dg) > 1e-9
  val xExt = if (hasExtremum) (-g1 * lengthL) / dg else 0.0
  val isExtWithin = hasExtremum && xExt >= -1e-6 && xExt <= lengthL + 1e-6
  val staExt = staPvc + xExt
  val elevExt = elevPvc + (g1 / 100.0) * xExt + (dg / (200.0 * lengthL)) * xExt * xExt

  val queryX = querySta?.let { it - staPvc }
  val queryElev = queryX?.let { x ->
    elevPvc + (g1 / 100.0) * x + (dg / (200.0 * lengthL)) * x * x
  }
  val queryOffsetY = queryX?.let { x -> (dg / (200.0 * lengthL)) * x * x }
  val queryWithin = queryX != null && queryX >= -1e-6 && queryX <= lengthL + 1e-6

  val sb = StringBuilder()
  sb.appendLine("SIBIL CE • VERTICAL PARABOLIC CURVE ($curveType)")
  sb.appendLine("g₁ = ${fmtNum(g1, 2)}% | g₂ = ${fmtNum(g2, 2)}% | L = ${fmtNum(lengthL, 2)} m")
  sb.appendLine("Rate of Change r = ${fmtNum(rateR, 5)} %/m")
  sb.appendLine("PVC: ${formatMetricStation(staPvc)}, Elev = ${fmtNum(elevPvc, 3)} m")
  sb.appendLine("PVI: ${formatMetricStation(staPvi)}, Elev = ${fmtNum(elevPvi, 3)} m")
  sb.appendLine("PVT: ${formatMetricStation(staPvt)}, Elev = ${fmtNum(elevPvt, 3)} m")
  sb.appendLine("Middle Offset H = ${fmtNum(middleOffsetH, 3)} m")
  if (hasExtremum) {
    val labelExt = if (isSummit) "Highest Point" else "Lowest Point"
    val status = if (isExtWithin) "Within Curve" else "Outside Curve"
    sb.appendLine("$labelExt ($status): x = ${fmtNum(xExt, 3)} m, ${formatMetricStation(staExt)}, Elev = ${fmtNum(elevExt, 3)} m")
  }
  if (querySta != null && queryElev != null) {
    sb.appendLine("Query ${formatMetricStation(querySta)}: Elev = ${fmtNum(queryElev, 3)} m")
  }

  return VerticalCurveResult(
    isValid = true,
    g1 = g1,
    g2 = g2,
    lengthL = lengthL,
    curveType = curveType,
    isSummit = isSummit,
    rateR = rateR,
    staPvc = staPvc,
    elevPvc = elevPvc,
    staPvi = staPvi,
    elevPvi = elevPvi,
    staPvt = staPvt,
    elevPvt = elevPvt,
    middleOffsetH = middleOffsetH,
    hasExtremum = hasExtremum,
    xExt = xExt,
    isExtWithinCurve = isExtWithin,
    staExt = staExt,
    elevExt = elevExt,
    querySta = querySta,
    queryElev = queryElev,
    queryOffsetY = queryOffsetY,
    queryWithinCurve = queryWithin,
    copySummary = sb.toString().trim()
  )
}

// ----------------------------------------------------------------------------
// 5. LEVELING & STADIA
// ----------------------------------------------------------------------------

internal data class StadiaLevelingResult(
  val isValid: Boolean,
  val errorMessage: String? = null,
  val upperU: Double = 0.0,
  val middleM: Double = 0.0,
  val lowerL: Double = 0.0,
  val alphaDeg: Double = 0.0,
  val hi: Double = 0.0,
  val k: Double = 100.0,
  val c: Double = 0.0,
  val interceptS: Double = 0.0,
  val slopeDistanceD: Double = 0.0,
  val horizontalDistanceH: Double = 0.0,
  val verticalDistanceV: Double = 0.0,
  val rodElevation: Double = 0.0,
  val copySummary: String = ""
)

internal fun computeStadiaLeveling(
  uText: String,
  mText: String,
  lText: String,
  alphaText: String,
  hiText: String,
  kText: String,
  cText: String
): StadiaLevelingResult {
  val u = uText.toDoubleOrNull()
  val m = mText.toDoubleOrNull()
  val l = lText.toDoubleOrNull()
  val alphaDeg = alphaText.toDoubleOrNull()
  val hi = hiText.toDoubleOrNull()
  val k = kText.toDoubleOrNull()
  val c = cText.ifBlank { "0" }.toDoubleOrNull()

  if (u == null || m == null || l == null || alphaDeg == null || hi == null || k == null || c == null) {
    return StadiaLevelingResult(
      isValid = false,
      errorMessage = "Enter numeric stadia readings U, M, L, angle α, HI, K, and C."
    )
  }
  if (u <= l) {
    return StadiaLevelingResult(
      isValid = false,
      errorMessage = "Upper stadia hair U must be greater than Lower hair L."
    )
  }
  if (alphaDeg <= -90.0 || alphaDeg >= 90.0) {
    return StadiaLevelingResult(
      isValid = false,
      errorMessage = "Vertical angle α must be strictly between -90° and +90°."
    )
  }

  val alphaRad = alphaDeg * PI / 180.0
  val s = u - l
  val slopeD = k * s * cos(alphaRad) + c
  val horizH = k * s * cos(alphaRad) * cos(alphaRad) + c * cos(alphaRad)
  val vertV = 0.5 * k * s * sin(2.0 * alphaRad) + c * sin(alphaRad)
  val elev = hi + vertV - m

  val sb = StringBuilder()
  sb.appendLine("SIBIL CE • STADIA & TRIGONOMETRIC LEVELING")
  sb.appendLine("Stadia Intercept s = U - L = ${fmtNum(s, 3)} m")
  sb.appendLine("Slope Distance D = ${fmtNum(slopeD, 3)} m")
  sb.appendLine("Horizontal Distance H = ${fmtNum(horizH, 3)} m")
  sb.appendLine("Vertical Distance V = ${fmtNum(vertV, 3)} m")
  sb.appendLine("Rod Station Elevation = HI + V - M = ${fmtNum(elev, 3)} m")

  return StadiaLevelingResult(
    isValid = true,
    upperU = u,
    middleM = m,
    lowerL = l,
    alphaDeg = alphaDeg,
    hi = hi,
    k = k,
    c = c,
    interceptS = s,
    slopeDistanceD = slopeD,
    horizontalDistanceH = horizH,
    verticalDistanceV = vertV,
    rodElevation = elev,
    copySummary = sb.toString().trim()
  )
}

// ----------------------------------------------------------------------------
// 6. EARTHWORK VOLUMES (AVERAGE END AREA & PRISMOIDAL)
// ----------------------------------------------------------------------------

internal data class EarthworkVolumeResult(
  val isValid: Boolean,
  val errorMessage: String? = null,
  val lengthL: Double = 0.0,
  val area1: Double = 0.0,
  val areaMid: Double = 0.0,
  val isMidInterpolated: Boolean = true,
  val area2: Double = 0.0,
  val volEndArea: Double = 0.0,
  val volPrismoidal: Double = 0.0,
  val prismoidalCorrection: Double = 0.0,
  val copySummary: String = ""
)

internal fun computeEarthworkVolumes(
  lengthLText: String,
  a1Text: String,
  amText: String,
  a2Text: String
): EarthworkVolumeResult {
  val l = lengthLText.toDoubleOrNull()
  val a1 = a1Text.toDoubleOrNull()
  val a2 = a2Text.toDoubleOrNull()
  val amParsed = amText.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()

  if (l == null || l <= 0.0 || a1 == null || a1 < 0.0 || a2 == null || a2 < 0.0) {
    return EarthworkVolumeResult(
      isValid = false,
      errorMessage = "Enter positive distance L (m) and non-negative cross-section areas A₁, A₂ (m²)."
    )
  }
  if (amText.isNotBlank() && (amParsed == null || amParsed < 0.0)) {
    return EarthworkVolumeResult(
      isValid = false,
      errorMessage = "Mid-section area Am must be a non-negative number (or leave blank to interpolate)."
    )
  }

  val isInterpolated = amParsed == null
  val am = amParsed ?: ((a1 + a2) / 2.0)

  val ve = (l / 2.0) * (a1 + a2)
  val vp = (l / 6.0) * (a1 + 4.0 * am + a2)
  val cp = ve - vp

  val sb = StringBuilder()
  sb.appendLine("SIBIL CE • EARTHWORK VOLUMES")
  sb.appendLine("L = ${fmtNum(l, 2)} m | A₁ = ${fmtNum(a1, 2)} m² | Am = ${fmtNum(am, 2)} m²${if (isInterpolated) " (interp)" else ""} | A₂ = ${fmtNum(a2, 2)} m²")
  sb.appendLine("Average End Area (Ve) = ${fmtNum(ve, 3)} m³")
  sb.appendLine("Prismoidal Volume (Vp) = ${fmtNum(vp, 3)} m³")
  sb.appendLine("Prismoidal Correction (Cp = Ve - Vp) = ${fmtNum(cp, 3)} m³")

  return EarthworkVolumeResult(
    isValid = true,
    lengthL = l,
    area1 = a1,
    areaMid = am,
    isMidInterpolated = isInterpolated,
    area2 = a2,
    volEndArea = ve,
    volPrismoidal = vp,
    prismoidalCorrection = cp,
    copySummary = sb.toString().trim()
  )
}

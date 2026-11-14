package com.example

import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

// ============================================================================
// MAX B. FAJARDO JR. ("SIMPLIFIED CONSTRUCTION ESTIMATE") QUANTITY TAKE-OFF ENGINE
// ============================================================================

internal enum class EstimateTrade(
  val id: String,
  val title: String,
  val subtitleTag: String,
  val fajardoRef: String
) {
  CHB_MASONRY("chb_masonry", "CHB Masonry Wall", "Blocks, Mortar & Rebar", "Fajardo Ch. 2 • Tables 2-2 to 2-5"),
  CONCRETE_WORKS("concrete_works", "Concrete Works", "Footing, Column, Beam, Slab", "Fajardo Ch. 1 • Table 1-2"),
  REINFORCING_STEEL("reinforcing_steel", "Reinforcing Steel", "Rebars & #16 GI Tie Wire", "Fajardo Ch. 3 • Deformed Bars"),
  FORMWORKS_LUMBER("formworks_lumber", "Formworks & Lumber", "Plywood & Board-Feet", "Fajardo Ch. 5 • Formwork Lumber"),
  ROOFING_PURLINS("roofing_purlins", "Roofing & Purlins", "Long-Span GI & Tekscrews", "Fajardo Ch. 7 • Roofing Sheets"),
  TILEWORKS("tileworks", "Tileworks", "Tiles, Adhesive & Grout", "Fajardo Ch. 9 • Floor & Wall Tiles"),
  PAINTING_WORKS("painting_works", "Painting Works", "Primer, Topcoat & Skimcoat", "Fajardo Ch. 10 • Paint Coverage"),
  EARTHWORKS("earthworks", "Earthworks", "Excavation, Swell & Bedding", "Fajardo Ch. 1 • Excavation & G-1")
}

// --- Shared Selectors ---

internal enum class ChbSizeOption(val label: String, val thicknessCm: Int) {
  CHB_10("10cm (4\")", 10),
  CHB_15("15cm (6\")", 15),
  CHB_20("20cm (8\")", 20)
}

internal enum class MortarMixClass(val label: String, val ratioText: String) {
  CLASS_A("Class A (1:2)", "1:2"),
  CLASS_B("Class B (1:3)", "1:3"),
  CLASS_C("Class C (1:4)", "1:4")
}

internal enum class PlasterFacesOption(val label: String, val faces: Int) {
  NONE("None (0)", 0),
  ONE_FACE("1 Face", 1),
  TWO_FACES("2 Faces", 2)
}

internal enum class VertRebarSpacing(val label: String, val spacingCm: Int, val factorMPerSqM: Double) {
  CM_40("40 cm", 40, 2.93),
  CM_60("60 cm", 60, 2.13),
  CM_80("80 cm", 80, 1.60)
}

internal enum class HorizRebarLayer(val label: String, val everyLayers: Int, val factorMPerSqM: Double) {
  LAYERS_2("Every 2 Layers", 2, 3.30),
  LAYERS_3("Every 3 Layers", 3, 2.15),
  LAYERS_4("Every 4 Layers", 4, 1.72)
}

internal enum class ConcreteClassOption(val label: String, val bagsPerCuM: Double) {
  CLASS_AA("Class AA (1:1.5:3)", 12.0),
  CLASS_A("Class A (1:2:4)", 9.0),
  CLASS_B("Class B (1:2.5:5)", 7.5),
  CLASS_C("Class C (1:3:6)", 6.0)
}

internal enum class FormworkElementType(
  val label: String,
  val boardFtPerPlywood: Double,
  val desc: String
) {
  SQUARE_COLUMN("Square Column", 29.67, "29.67 bd.ft./plywood"),
  BEAM_GIRDER("Beam / Girder", 25.06, "25.06 bd.ft./plywood"),
  FLOOR_SLAB("Floor Slab", 24.00, "24.00 bd.ft./plywood")
}

internal enum class RoofingSheetWidth(val label: String, val effWidthM: Double) {
  CORRUGATED_076("0.76 m (Corrugated)", 0.76),
  RIB_080("0.80 m (Rib-Type)", 0.80),
  WIDE_RIB_100("1.00 m (Wide Rib)", 1.00)
}

internal enum class TileSizeOption(val label: String, val areaSqM: Double, val wCm: Int, val hCm: Int) {
  TILE_30X30("30×30 cm (0.09 m²)", 0.09, 30, 30),
  TILE_40X40("40×40 cm (0.16 m²)", 0.16, 40, 40),
  TILE_60X60("60×60 cm (0.36 m²)", 0.36, 60, 60),
  TILE_30X60("30×60 cm (0.18 m²)", 0.18, 30, 60)
}

internal enum class PaintSurfaceOption(val label: String, val coverageSqMPerGal: Double) {
  CONCRETE_MASONRY("Concrete / Masonry (35 m²/gal)", 35.0),
  WOOD_DRYWALL("Wood / Drywall (30 m²/gal)", 30.0),
  STEEL_METAL("Steel / Metal (40 m²/gal)", 40.0)
}

// --- Material Breakdown Display Row ---

internal data class EstimateMaterialRow(
  val materialName: String,
  val exactValueText: String,
  val orderQuantityText: String,
  val multiplierFormula: String
)

// --- Structured Project BOQ Cart Models ---

internal data class BoqMaterialContribution(
  val cementBags40kg: Double = 0.0,
  val sandCuM: Double = 0.0,
  val gravelCuM: Double = 0.0,
  val chbPcs: Int = 0,
  val rebarPcs: Int = 0,
  val rebarWeightKg: Double = 0.0,
  val tieWireKg: Double = 0.0,
  val plywoodSheets: Int = 0,
  val lumberBdFt: Double = 0.0,
  val roofingSheets: Int = 0,
  val ridgeRollPcs: Int = 0,
  val tekscrewPcs: Int = 0,
  val tilePcs: Int = 0,
  val tileAdhesiveBags25kg: Int = 0,
  val tileGroutKg: Double = 0.0,
  val paintGallons: Double = 0.0,
  val skimcoatBags20kg: Int = 0,
  val excavationBankCuM: Double = 0.0,
  val excavationLooseCuM: Double = 0.0
)

internal data class BoqCartItem(
  val id: Long,
  val trade: EstimateTrade,
  val title: String,
  val specSummary: String,
  val keyMaterialsText: List<String>,
  val contribution: BoqMaterialContribution
)

// --- 1. CHB Masonry Wall Computation ---

internal data class ChbMasonryResult(
  val grossArea: Double,
  val netArea: Double,
  val chbExact: Double,
  val chbOrderPcs: Int,
  val mortarCementFactor: Double,
  val mortarSandFactor: Double,
  val mortarCementBagsExact: Double,
  val mortarSandCuMExact: Double,
  val plasterCementFactorPerFace: Double,
  val plasterSandFactorPerFace: Double,
  val plasterCementBagsExact: Double,
  val plasterSandCuMExact: Double,
  val totalCementBagsExact: Double,
  val totalCementBagsOrder: Int,
  val totalSandCuMExact: Double,
  val totalRebarMeters: Double,
  val totalRebarPieces: Int,
  val tieWireFactor: Double,
  val tieWireKgExact: Double,
  val rows: List<EstimateMaterialRow>
)

internal fun computeChbMasonry(
  lengthM: Double,
  heightM: Double,
  openingsAreaM2: Double,
  wastagePct: Double,
  chbSize: ChbSizeOption,
  mixClass: MortarMixClass,
  plasterFaces: PlasterFacesOption,
  vertSpacing: VertRebarSpacing,
  horizLayer: HorizRebarLayer,
  commRebarLenM: Double
): ChbMasonryResult {
  val l = max(0.0, lengthM)
  val h = max(0.0, heightM)
  val gross = l * h
  val net = max(0.0, gross - max(0.0, openingsAreaM2))
  val wasteFactor = 1.0 + max(0.0, wastagePct) / 100.0

  // Fajardo Table 2-1: 12.5 CHB per m²
  val chbExact = net * 12.5 * wasteFactor
  val chbOrder = ceil(chbExact).toInt()

  // Fajardo Table 2-2: Mortar (40kg bags & sand m³ per m²)
  val (mortarBagFactor, mortarSandFactor) = when (chbSize) {
    ChbSizeOption.CHB_10 -> when (mixClass) {
      MortarMixClass.CLASS_A -> 0.792 to 0.044
      MortarMixClass.CLASS_B -> 0.522 to 0.0435
      MortarMixClass.CLASS_C -> 0.394 to 0.0438
    }
    ChbSizeOption.CHB_15 -> when (mixClass) {
      MortarMixClass.CLASS_A -> 1.526 to 0.0848
      MortarMixClass.CLASS_B -> 1.018 to 0.0848
      MortarMixClass.CLASS_C -> 0.763 to 0.0848
    }
    ChbSizeOption.CHB_20 -> when (mixClass) {
      MortarMixClass.CLASS_A -> 2.260 to 0.1256
      MortarMixClass.CLASS_B -> 1.500 to 0.1250
      MortarMixClass.CLASS_C -> 1.125 to 0.1250
    }
  }
  val mortarCementExact = net * mortarBagFactor
  val mortarSandExact = net * mortarSandFactor

  // Fajardo Table 2-3: Plastering 16mm per m² per face
  val (plasterBagFactor, plasterSandFactor) = when (mixClass) {
    MortarMixClass.CLASS_A -> 0.288 to 0.016
    MortarMixClass.CLASS_B -> 0.192 to 0.016
    MortarMixClass.CLASS_C -> 0.144 to 0.016
  }
  val plasterCementExact = net * plasterBagFactor * plasterFaces.faces
  val plasterSandExact = net * plasterSandFactor * plasterFaces.faces

  val totalCementExact = mortarCementExact + plasterCementExact
  val totalCementOrder = ceil(totalCementExact).toInt()
  val totalSandExact = mortarSandExact + plasterSandExact

  // Fajardo Table 2-4: CHB Rebar m/m²
  val totalRebarMeters = net * (vertSpacing.factorMPerSqM + horizLayer.factorMPerSqM)
  val totalRebarPieces = ceil(totalRebarMeters / max(1.0, commRebarLenM)).toInt()

  // Fajardo Table 2-5: #16 GI Tie Wire (30 cm tie)
  val tieWireFactor = when {
    vertSpacing == VertRebarSpacing.CM_40 && horizLayer == HorizRebarLayer.LAYERS_2 -> 0.054
    vertSpacing == VertRebarSpacing.CM_60 && horizLayer == HorizRebarLayer.LAYERS_3 -> 0.027
    vertSpacing == VertRebarSpacing.CM_80 && horizLayer == HorizRebarLayer.LAYERS_4 -> 0.017
    else -> vertSpacing.factorMPerSqM * horizLayer.factorMPerSqM * 0.0056
  }
  val tieWireKgExact = net * tieWireFactor

  val rows = buildList {
    add(
      EstimateMaterialRow(
        materialName = "CHB ${chbSize.label}",
        exactValueText = "${estFmt2(chbExact)} pcs",
        orderQuantityText = "$chbOrder pcs",
        multiplierFormula = "Net ${estFmt2(net)} m² × 12.5 × ${estFmt2(wasteFactor)} (Fajardo Table 2-1)"
      )
    )
    add(
      EstimateMaterialRow(
        materialName = "Portland Cement (40kg) — Total",
        exactValueText = "${estFmt2(totalCementExact)} bags",
        orderQuantityText = "$totalCementOrder bags",
        multiplierFormula = "Mortar ${estFmt3(mortarBagFactor)} + Plaster ${estFmt3(plasterBagFactor)}×${plasterFaces.faces} bags/m² (${mixClass.label})"
      )
    )
    if (plasterFaces.faces > 0) {
      add(
        EstimateMaterialRow(
          materialName = "  • Mortar / Plaster Split",
          exactValueText = "${estFmt2(mortarCementExact)} + ${estFmt2(plasterCementExact)} bags",
          orderQuantityText = "${ceil(mortarCementExact).toInt()} + ${ceil(plasterCementExact).toInt()} bags",
          multiplierFormula = "Table 2-2 (${estFmt3(mortarBagFactor)}) • Table 2-3 (${estFmt3(plasterBagFactor)} × ${plasterFaces.faces})"
        )
      )
    }
    add(
      EstimateMaterialRow(
        materialName = "Washed Sand",
        exactValueText = "${estFmt3(totalSandExact)} m³",
        orderQuantityText = "${estFmt2(ceil(totalSandExact * 2.0) / 2.0)} m³",
        multiplierFormula = "Mortar ${estFmt4(mortarSandFactor)} + Plaster ${estFmt3(plasterSandFactor)}×${plasterFaces.faces} m³/m²"
      )
    )
    add(
      EstimateMaterialRow(
        materialName = "CHB Rebar (${estFmt1(commRebarLenM)} m)",
        exactValueText = "${estFmt2(totalRebarMeters)} m",
        orderQuantityText = "$totalRebarPieces pcs",
        multiplierFormula = "(${vertSpacing.factorMPerSqM} V + ${horizLayer.factorMPerSqM} H) m/m² ÷ ${estFmt1(commRebarLenM)}m (Table 2-4)"
      )
    )
    add(
      EstimateMaterialRow(
        materialName = "#16 GI Tie Wire (30cm)",
        exactValueText = "${estFmt3(tieWireKgExact)} kg",
        orderQuantityText = "${ceil(tieWireKgExact).toInt()} kg",
        multiplierFormula = "Net Area × ${estFmt4(tieWireFactor)} kg/m² (Fajardo Table 2-5)"
      )
    )
  }

  return ChbMasonryResult(
    grossArea = gross,
    netArea = net,
    chbExact = chbExact,
    chbOrderPcs = chbOrder,
    mortarCementFactor = mortarBagFactor,
    mortarSandFactor = mortarSandFactor,
    mortarCementBagsExact = mortarCementExact,
    mortarSandCuMExact = mortarSandExact,
    plasterCementFactorPerFace = plasterBagFactor,
    plasterSandFactorPerFace = plasterSandFactor,
    plasterCementBagsExact = plasterCementExact,
    plasterSandCuMExact = plasterSandExact,
    totalCementBagsExact = totalCementExact,
    totalCementBagsOrder = totalCementOrder,
    totalSandCuMExact = totalSandExact,
    totalRebarMeters = totalRebarMeters,
    totalRebarPieces = totalRebarPieces,
    tieWireFactor = tieWireFactor,
    tieWireKgExact = tieWireKgExact,
    rows = rows
  )
}

// --- 2. Concrete Works Computation ---

internal data class ConcreteWorksResult(
  val volumeCuM: Double,
  val cementBagsExact: Double,
  val cementBagsOrder: Int,
  val sandCuM: Double,
  val gravelCuM: Double,
  val rows: List<EstimateMaterialRow>
)

internal fun computeConcreteWorks(
  lengthM: Double,
  widthM: Double,
  thicknessM: Double,
  unitsCount: Int,
  wastagePct: Double,
  concreteClass: ConcreteClassOption
): ConcreteWorksResult {
  val l = max(0.0, lengthM)
  val w = max(0.0, widthM)
  val t = max(0.0, thicknessM)
  val n = max(1, unitsCount)
  val wasteFactor = 1.0 + max(0.0, wastagePct) / 100.0
  val volume = l * w * t * n * wasteFactor

  val cementExact = volume * concreteClass.bagsPerCuM
  val cementOrder = ceil(cementExact).toInt()
  val sand = volume * 0.50
  val gravel = volume * 1.00

  val rows = listOf(
    EstimateMaterialRow(
      materialName = "Total Concrete Volume",
      exactValueText = "${estFmt3(volume)} m³",
      orderQuantityText = "${estFmt2(volume)} m³",
      multiplierFormula = "$n × ${estFmt2(l)}×${estFmt2(w)}×${estFmt2(t)}m × ${estFmt2(wasteFactor)} waste"
    ),
    EstimateMaterialRow(
      materialName = "Portland Cement (40kg)",
      exactValueText = "${estFmt2(cementExact)} bags",
      orderQuantityText = "$cementOrder bags",
      multiplierFormula = "V × ${estFmt1(concreteClass.bagsPerCuM)} bags/m³ (${concreteClass.label}, Table 1-2)"
    ),
    EstimateMaterialRow(
      materialName = "Washed Sand",
      exactValueText = "${estFmt3(sand)} m³",
      orderQuantityText = "${estFmt2(ceil(sand * 2.0) / 2.0)} m³",
      multiplierFormula = "V × 0.50 m³/m³ (Fajardo Table 1-2)"
    ),
    EstimateMaterialRow(
      materialName = "Crushed Gravel (3/4\")",
      exactValueText = "${estFmt3(gravel)} m³",
      orderQuantityText = "${estFmt2(ceil(gravel * 2.0) / 2.0)} m³",
      multiplierFormula = "V × 1.00 m³/m³ (Fajardo Table 1-2)"
    )
  )

  return ConcreteWorksResult(volume, cementExact, cementOrder, sand, gravel, rows)
}

// --- 3. Reinforcing Steel & Tie Wire Computation ---

internal data class RebarResult(
  val totalLinearM: Double,
  val commercialPieces: Int,
  val totalWeightKg: Double,
  val tieWireKgExact: Double,
  val tieWireKgOrder: Int,
  val rows: List<EstimateMaterialRow>
)

internal fun computeReinforcingSteel(
  cutLengthM: Double,
  numBars: Int,
  numIntersections: Int,
  lapSplicePct: Double,
  diameterMm: Int,
  commLengthM: Double,
  tieCutCm: Int
): RebarResult {
  val lCut = max(0.0, cutLengthM)
  val nBars = max(0, numBars)
  val nTies = max(0, numIntersections)
  val spliceFactor = 1.0 + max(0.0, lapSplicePct) / 100.0
  val totalLin = lCut * nBars * spliceFactor
  val pieces = ceil(totalLin / max(1.0, commLengthM)).toInt()
  val unitWtKgPerM = (diameterMm * diameterMm) / 162.0
  val totalWt = totalLin * unitWtKgPerM
  val tieLenM = tieCutCm / 100.0
  val tieKgExact = (nTies * tieLenM) / 53.0
  val tieKgOrder = ceil(tieKgExact).toInt()

  val rows = listOf(
    EstimateMaterialRow(
      materialName = "Total Linear Length",
      exactValueText = "${estFmt2(totalLin)} m",
      orderQuantityText = "${estFmt2(totalLin)} m",
      multiplierFormula = "${estFmt2(lCut)}m × $nBars bars × ${estFmt2(spliceFactor)} splice"
    ),
    EstimateMaterialRow(
      materialName = "${diameterMm}mm Ø Deformed Bars (${estFmt1(commLengthM)}m)",
      exactValueText = "${estFmt2(totalLin / max(1.0, commLengthM))} pcs",
      orderQuantityText = "$pieces pcs",
      multiplierFormula = "L_total ÷ ${estFmt1(commLengthM)}m commercial length"
    ),
    EstimateMaterialRow(
      materialName = "Estimated Steel Weight",
      exactValueText = "${estFmt2(totalWt)} kg",
      orderQuantityText = "${ceil(totalWt).toInt()} kg",
      multiplierFormula = "L_total × (${diameterMm}² / 162 = ${estFmt3(unitWtKgPerM)} kg/m)"
    ),
    EstimateMaterialRow(
      materialName = "#16 GI Tie Wire (${tieCutCm}cm tie)",
      exactValueText = "${estFmt3(tieKgExact)} kg",
      orderQuantityText = "$tieKgOrder kg",
      multiplierFormula = "($nTies ties × ${estFmt2(tieLenM)}m) ÷ 53 m/kg"
    )
  )

  return RebarResult(totalLin, pieces, totalWt, tieKgExact, tieKgOrder, rows)
}

// --- 4. Formworks & Coco Lumber Computation ---

internal data class FormworksResult(
  val plywoodExact: Double,
  val plywoodOrderSheets: Int,
  val lumberBdFtExact: Double,
  val lumberBdFtOrder: Int,
  val rows: List<EstimateMaterialRow>
)

internal fun computeFormworks(
  contactAreaSqM: Double,
  reusesCount: Int,
  elementType: FormworkElementType
): FormworksResult {
  val area = max(0.0, contactAreaSqM)
  val reuses = max(1, reusesCount)
  val stdSheetSqM = 1.22 * 2.44 // 2.9768 m²
  val plyExact = area / (stdSheetSqM * reuses)
  val plyOrder = ceil(plyExact).toInt()
  val lumberExact = plyOrder * elementType.boardFtPerPlywood
  val lumberOrder = ceil(lumberExact).toInt()

  val rows = listOf(
    EstimateMaterialRow(
      materialName = "Phenolic / Ordinary Plywood (4'×8')",
      exactValueText = "${estFmt2(plyExact)} sheets",
      orderQuantityText = "$plyOrder sheets",
      multiplierFormula = "${estFmt2(area)} m² ÷ (2.9768 m² × ${reuses}x reuse)"
    ),
    EstimateMaterialRow(
      materialName = "Coco / Framing Lumber (${elementType.label})",
      exactValueText = "${estFmt2(lumberExact)} bd.ft.",
      orderQuantityText = "$lumberOrder bd.ft.",
      multiplierFormula = "$plyOrder sheets × ${estFmt2(elementType.boardFtPerPlywood)} bd.ft./sheet (Fajardo Ch. 5)"
    )
  )

  return FormworksResult(plyExact, plyOrder, lumberExact, lumberOrder, rows)
}

// --- 5. Roofing & C-Purlins Computation ---

internal data class RoofingResult(
  val sheetsPerRowPerSlope: Int,
  val totalSheets: Int,
  val ridgeRollPcs: Int,
  val tekscrewsPcs: Int,
  val rows: List<EstimateMaterialRow>
)

internal fun computeRoofing(
  rafterLengthM: Double,
  ridgeLengthM: Double,
  slopesCount: Int,
  widthOption: RoofingSheetWidth
): RoofingResult {
  val rafter = max(0.0, rafterLengthM)
  val ridge = max(0.0, ridgeLengthM)
  val slopes = max(1, slopesCount)

  val perSlope = if (ridge > 0.0) ceil(ridge / widthOption.effWidthM).toInt() else 0
  val totalSheets = perSlope * slopes
  val ridgeRolls = if (ridge > 0.0 && slopes >= 2) ceil(ridge / 2.10).toInt() else 0
  val tekscrews = totalSheets * 18

  val rows = buildList {
    add(
      EstimateMaterialRow(
        materialName = "Long-Span GI Sheets (${estFmt2(rafter)}m cut)",
        exactValueText = "${estFmt2(ridge / widthOption.effWidthM)} × $slopes slope",
        orderQuantityText = "$totalSheets sheets",
        multiplierFormula = "ceil(${estFmt2(ridge)}m ÷ ${estFmt2(widthOption.effWidthM)}m) × $slopes slope(s)"
      )
    )
    add(
      EstimateMaterialRow(
        materialName = "Ridge Roll (2.40m, 0.30m lap)",
        exactValueText = if (slopes >= 2) "${estFmt2(ridge / 2.10)} pcs" else "0 pcs (Shed)",
        orderQuantityText = "$ridgeRolls pcs",
        multiplierFormula = if (slopes >= 2) "ceil(${estFmt2(ridge)}m ÷ 2.10m eff.)" else "Single-slope shed (no ridge)"
      )
    )
    add(
      EstimateMaterialRow(
        materialName = "Tekscrews / Blind Rivets",
        exactValueText = "$tekscrews pcs",
        orderQuantityText = "$tekscrews pcs",
        multiplierFormula = "$totalSheets sheets × 18 pcs/sheet (Fajardo Ch. 7)"
      )
    )
  }

  return RoofingResult(perSlope, totalSheets, ridgeRolls, tekscrews, rows)
}

// --- 6. Tileworks Computation ---

internal data class TileworksResult(
  val areaSqM: Double,
  val tilesExact: Double,
  val tilesOrderPcs: Int,
  val adhesiveBagsOrder: Int,
  val fajardoMortarCementBags: Double,
  val fajardoMortarSandCuM: Double,
  val groutKgExact: Double,
  val groutKgOrder: Int,
  val grout2KgPacks: Int,
  val rows: List<EstimateMaterialRow>
)

internal fun computeTileworks(
  lengthM: Double,
  widthM: Double,
  wastagePct: Double,
  tileSize: TileSizeOption
): TileworksResult {
  val area = max(0.0, lengthM) * max(0.0, widthM)
  val wasteFactor = 1.0 + max(0.0, wastagePct) / 100.0
  val tilesExact = (area / tileSize.areaSqM) * wasteFactor
  val tilesOrder = ceil(tilesExact).toInt()
  val adhesiveOrder = ceil(area / 4.5).toInt()
  val fajardoCement = area * 0.086
  val fajardoSand = area * 0.0048
  val groutKgExact = area * 0.25
  val groutKgOrder = ceil(groutKgExact).toInt()
  val groutPacks = ceil(groutKgExact / 2.0).toInt()

  val rows = listOf(
    EstimateMaterialRow(
      materialName = "Ceramic / Porcelain Tiles (${tileSize.wCm}×${tileSize.hCm} cm)",
      exactValueText = "${estFmt2(tilesExact)} pcs",
      orderQuantityText = "$tilesOrder pcs",
      multiplierFormula = "(${estFmt2(area)}m² ÷ ${estFmt2(tileSize.areaSqM)}m²) × ${estFmt2(wasteFactor)} waste"
    ),
    EstimateMaterialRow(
      materialName = "Heavy-Duty Tile Adhesive (25kg)",
      exactValueText = "${estFmt2(area / 4.5)} bags",
      orderQuantityText = "$adhesiveOrder bags",
      multiplierFormula = "ceil(Area ÷ 4.5 m²/bag); Fajardo mortar: ${estFmt2(fajardoCement)} bags + ${estFmt4(fajardoSand)} m³ sand"
    ),
    EstimateMaterialRow(
      materialName = "Tile Grout (2kg Packs)",
      exactValueText = "${estFmt2(groutKgExact)} kg",
      orderQuantityText = "$groutKgOrder kg ($groutPacks packs)",
      multiplierFormula = "Area × 0.25 kg/m² (Fajardo Ch. 9)"
    )
  )

  return TileworksResult(
    areaSqM = area,
    tilesExact = tilesExact,
    tilesOrderPcs = tilesOrder,
    adhesiveBagsOrder = adhesiveOrder,
    fajardoMortarCementBags = fajardoCement,
    fajardoMortarSandCuM = fajardoSand,
    groutKgExact = groutKgExact,
    groutKgOrder = groutKgOrder,
    grout2KgPacks = groutPacks,
    rows = rows
  )
}

// --- 7. Painting Works Computation ---

internal data class PaintingResult(
  val netAreaSqM: Double,
  val primerGallonsExact: Double,
  val primerGallonsOrder: Int,
  val topcoatGallonsExact: Double,
  val topcoatGallonsOrder: Int,
  val skimcoatBagsOrder: Int,
  val rows: List<EstimateMaterialRow>
)

internal fun computePaintingWorks(
  lengthM: Double,
  heightM: Double,
  openingsAreaSqM: Double,
  topcoatsCount: Int,
  surfaceOption: PaintSurfaceOption
): PaintingResult {
  val gross = max(0.0, lengthM) * max(0.0, heightM)
  val net = max(0.0, gross - max(0.0, openingsAreaSqM))
  val coats = max(1, topcoatsCount)
  val cov = surfaceOption.coverageSqMPerGal
  val primerExact = net / cov
  val topcoatExact = (net * coats) / cov
  val primerOrder = ceil(primerExact).toInt()
  val topcoatOrder = ceil(topcoatExact).toInt()
  val skimcoatOrder = ceil(net / 20.0).toInt()

  val rows = listOf(
    EstimateMaterialRow(
      materialName = "Primer Paint (1 Coat)",
      exactValueText = "${estFmt2(primerExact)} gal",
      orderQuantityText = "$primerOrder gal",
      multiplierFormula = "Net ${estFmt2(net)}m² ÷ ${estFmt0(cov)} m²/gal (${surfaceOption.label.substringBefore(" (")})"
    ),
    EstimateMaterialRow(
      materialName = "Topcoat Paint ($coats Coat${if (coats > 1) "s" else ""})",
      exactValueText = "${estFmt2(topcoatExact)} gal",
      orderQuantityText = "$topcoatOrder gal",
      multiplierFormula = "(${estFmt2(net)}m² × $coats) ÷ ${estFmt0(cov)} m²/gal (Fajardo Ch. 10)"
    ),
    EstimateMaterialRow(
      materialName = "Skimcoat / Putty (20kg Bag)",
      exactValueText = "${estFmt2(net / 20.0)} bags",
      orderQuantityText = "$skimcoatOrder bags",
      multiplierFormula = "ceil(Net Area ÷ 20 m²/bag)"
    )
  )

  return PaintingResult(net, primerExact, primerOrder, topcoatExact, topcoatOrder, skimcoatOrder, rows)
}

// --- 8. Earthworks Computation ---

internal data class EarthworksResult(
  val bankExcavationCuM: Double,
  val looseHaulCuM: Double,
  val gravelBeddingCuM: Double,
  val rows: List<EstimateMaterialRow>
)

internal fun computeEarthworks(
  lengthM: Double,
  widthM: Double,
  depthM: Double,
  quantityCount: Int,
  gravelThickM: Double,
  swellPct: Double
): EarthworksResult {
  val l = max(0.0, lengthM)
  val w = max(0.0, widthM)
  val d = max(0.0, depthM)
  val n = max(1, quantityCount)
  val tg = max(0.0, gravelThickM)
  val swellFactor = 1.0 + max(0.0, swellPct) / 100.0

  val bank = l * w * d * n
  val loose = bank * swellFactor
  val gravel = l * w * tg * n

  val rows = listOf(
    EstimateMaterialRow(
      materialName = "Neat In-Situ Excavation (Bank)",
      exactValueText = "${estFmt3(bank)} m³",
      orderQuantityText = "${estFmt2(bank)} m³",
      multiplierFormula = "$n × ${estFmt2(l)}×${estFmt2(w)}×${estFmt2(d)}m"
    ),
    EstimateMaterialRow(
      materialName = "Loose Hauling Volume",
      exactValueText = "${estFmt3(loose)} m³",
      orderQuantityText = "${estFmt2(ceil(loose * 2.0) / 2.0)} m³",
      multiplierFormula = "V_bank × ${estFmt2(swellFactor)} (+${estFmt0(swellPct)}% Swell Factor)"
    ),
    EstimateMaterialRow(
      materialName = "Gravel Bedding (G-1 Crushed Base)",
      exactValueText = "${estFmt3(gravel)} m³",
      orderQuantityText = "${estFmt2(ceil(gravel * 2.0) / 2.0)} m³",
      multiplierFormula = "$n × ${estFmt2(l)}×${estFmt2(w)}×${estFmt2(tg)}m bedding"
    )
  )

  return EarthworksResult(bank, loose, gravel, rows)
}

// --- Unified BOQ Consolidation & Summary Export ---

internal fun consolidateBoqCart(items: List<BoqCartItem>): BoqMaterialContribution {
  var total = BoqMaterialContribution()
  for (item in items) {
    val c = item.contribution
    total = total.copy(
      cementBags40kg = total.cementBags40kg + c.cementBags40kg,
      sandCuM = total.sandCuM + c.sandCuM,
      gravelCuM = total.gravelCuM + c.gravelCuM,
      chbPcs = total.chbPcs + c.chbPcs,
      rebarPcs = total.rebarPcs + c.rebarPcs,
      rebarWeightKg = total.rebarWeightKg + c.rebarWeightKg,
      tieWireKg = total.tieWireKg + c.tieWireKg,
      plywoodSheets = total.plywoodSheets + c.plywoodSheets,
      lumberBdFt = total.lumberBdFt + c.lumberBdFt,
      roofingSheets = total.roofingSheets + c.roofingSheets,
      ridgeRollPcs = total.ridgeRollPcs + c.ridgeRollPcs,
      tekscrewPcs = total.tekscrewPcs + c.tekscrewPcs,
      tilePcs = total.tilePcs + c.tilePcs,
      tileAdhesiveBags25kg = total.tileAdhesiveBags25kg + c.tileAdhesiveBags25kg,
      tileGroutKg = total.tileGroutKg + c.tileGroutKg,
      paintGallons = total.paintGallons + c.paintGallons,
      skimcoatBags20kg = total.skimcoatBags20kg + c.skimcoatBags20kg,
      excavationBankCuM = total.excavationBankCuM + c.excavationBankCuM,
      excavationLooseCuM = total.excavationLooseCuM + c.excavationLooseCuM
    )
  }
  return total
}

internal fun formatBoqFullSummaryText(items: List<BoqCartItem>): String {
  if (items.isEmpty()) return "Project BOQ Summary is empty."
  val totals = consolidateBoqCart(items)
  return buildString {
    appendLine("SIBIL CE • PROJECT BOQ TAKE-OFF SUMMARY")
    appendLine("Ref: Max B. Fajardo Jr. (Simplified Construction Estimate)")
    appendLine("====================================================")
    items.forEachIndexed { idx, item ->
      appendLine("${idx + 1}. ${item.title}")
      appendLine("   Specs: ${item.specSummary}")
      item.keyMaterialsText.forEach { line ->
        appendLine("   - $line")
      }
    }
    appendLine("====================================================")
    appendLine("CONSOLIDATED GRAND TOTALS:")
    if (totals.cementBags40kg > 0.0) appendLine("• Portland Cement (40kg): ${ceil(totals.cementBags40kg).toInt()} bags (exact ${estFmt2(totals.cementBags40kg)})")
    if (totals.sandCuM > 0.0) appendLine("• Washed Sand: ${estFmt2(totals.sandCuM)} m³")
    if (totals.gravelCuM > 0.0) appendLine("• Crushed Gravel / G-1: ${estFmt2(totals.gravelCuM)} m³")
    if (totals.chbPcs > 0) appendLine("• Concrete Hollow Blocks (CHB): ${totals.chbPcs} pcs")
    if (totals.rebarPcs > 0) appendLine("• Deformed Rebars: ${totals.rebarPcs} pcs" + if (totals.rebarWeightKg > 0.0) " (${estFmt2(totals.rebarWeightKg)} kg)" else "")
    if (totals.tieWireKg > 0.0) appendLine("• #16 GI Tie Wire: ${ceil(totals.tieWireKg).toInt()} kg (exact ${estFmt2(totals.tieWireKg)} kg)")
    if (totals.plywoodSheets > 0) appendLine("• Formwork Plywood (4'×8'): ${totals.plywoodSheets} sheets")
    if (totals.lumberBdFt > 0.0) appendLine("• Coco / Framing Lumber: ${ceil(totals.lumberBdFt).toInt()} bd.ft.")
    if (totals.roofingSheets > 0) appendLine("• Roofing GI Sheets: ${totals.roofingSheets} sheets")
    if (totals.ridgeRollPcs > 0) appendLine("• Ridge Roll (2.40m): ${totals.ridgeRollPcs} pcs")
    if (totals.tekscrewPcs > 0) appendLine("• Tekscrews / Rivets: ${totals.tekscrewPcs} pcs")
    if (totals.tilePcs > 0) appendLine("• Ceramic/Porcelain Tiles: ${totals.tilePcs} pcs")
    if (totals.tileAdhesiveBags25kg > 0) appendLine("• Tile Adhesive (25kg): ${totals.tileAdhesiveBags25kg} bags")
    if (totals.tileGroutKg > 0.0) appendLine("• Tile Grout: ${ceil(totals.tileGroutKg).toInt()} kg (${ceil(totals.tileGroutKg / 2.0).toInt()} packs)")
    if (totals.paintGallons > 0.0) appendLine("• Paint (Primer + Topcoat): ${ceil(totals.paintGallons).toInt()} gal (exact ${estFmt2(totals.paintGallons)} gal)")
    if (totals.skimcoatBags20kg > 0) appendLine("• Skimcoat / Putty (20kg): ${totals.skimcoatBags20kg} bags")
    if (totals.excavationBankCuM > 0.0) appendLine("• Earthwork Excavation: ${estFmt2(totals.excavationBankCuM)} m³ bank / ${estFmt2(totals.excavationLooseCuM)} m³ loose")
  }
}

internal fun estFmt0(v: Double): String = String.format(Locale.US, "%.0f", v)
internal fun estFmt1(v: Double): String = String.format(Locale.US, "%.1f", v)
internal fun estFmt2(v: Double): String = String.format(Locale.US, "%.2f", v)
internal fun estFmt3(v: Double): String = String.format(Locale.US, "%.3f", v)
internal fun estFmt4(v: Double): String = String.format(Locale.US, "%.4f", v)

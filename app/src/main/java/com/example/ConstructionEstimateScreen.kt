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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import kotlin.math.ceil

// ============================================================================
// SIBIL CE • "ESTIMATE" MODULE (MAX B. FAJARDO JR. QUANTITY TAKE-OFF + BOQ CART)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConstructionEstimateScreen(
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

  var activeTrade by remember { mutableStateOf<EstimateTrade?>(null) }
  var isBoqCartOpen by remember { mutableStateOf(false) }
  val boqCart = remember { mutableStateListOf<BoqCartItem>() }
  var nextBoqId by rememberSaveable { mutableLongStateOf(1L) }

  val orientation = rememberScreenOrientationState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .widthIn(max = if (orientation.isLandscape) 1140.dp else 760.dp)
      .padding(top = if (orientation.isCompactLandscape) 8.dp else metrics.scaledDp(14))
      .testTag("construction_estimate_screen"),
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
            .testTag("estimate_back_button")
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
            text = "Estimate",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontFamily = AppleSansFamily,
              fontSize = metrics.scaledSp(21),
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.4).sp
            ),
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "Max Fajardo • Quantity Take-Off",
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

      // Glowing BOQ Cart (N) Pill Button
      val cartGlowBrush = remember(isDarkMode) {
        Brush.horizontalGradient(
          colors = listOf(Color(0xFF0284C7), Color(0xFF2563EB))
        )
      }
      Row(
        modifier = Modifier
          .minimumInteractiveComponentSize()
          .shadow(
            elevation = if (boqCart.isNotEmpty()) 10.dp else 4.dp,
            shape = RoundedCornerShape(50),
            ambientColor = accentCyan.copy(alpha = 0.45f),
            spotColor = accentCyan.copy(alpha = 0.55f)
          )
          .clip(RoundedCornerShape(50))
          .background(brush = cartGlowBrush, shape = RoundedCornerShape(50))
          .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(50))
          .clickable {
            playCardHapticSound(context, localView)
            isBoqCartOpen = true
          }
          .padding(horizontal = 13.dp, vertical = 9.dp)
          .testTag("estimate_boq_cart_button"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = Icons.Filled.RequestQuote,
          contentDescription = "Open Project BOQ Cart",
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = "BOQ Cart (${boqCart.size})",
          style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = AppleSansFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          ),
          color = Color.White,
          maxLines = 1,
          softWrap = false
        )
      }
    }

    // 2. ADAPTIVE INTERACTIVE CAD FIGURE GRID
    LazyVerticalGrid(
      columns = if (orientation.isLandscape || metrics.screenWidthDp >= 600) {
        GridCells.Adaptive(minSize = 180.dp)
      } else {
        GridCells.Fixed(2)
      },
      contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 4.dp,
        bottom = if (orientation.isCompactLandscape) 32.dp else metrics.scaledDp(116)
      ),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier
        .fillMaxSize()
        .testTag("estimate_trades_grid")
    ) {
      items(EstimateTrade.entries, key = { it.id }) { trade ->
        LiquidGlassCard(
          onClick = {
            playCardHapticSound(context, localView)
            activeTrade = trade
          },
          shape = RoundedCornerShape(22.dp),
          elevation = 6.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("estimate_card_${trade.id}")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            EstimateCadThumbnail(
              trade = trade,
              isDarkMode = isDarkMode
            )

            Text(
              text = trade.title,
              style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = AppleSansFamily,
                fontSize = metrics.scaledSp(14),
                fontWeight = FontWeight.Bold
              ),
              color = palette.textPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            RecessedGlassPill(
              isDarkMode = isDarkMode,
              fillColor = accentCyan.copy(alpha = if (isDarkMode) 0.16f else 0.10f),
              borderColor = accentCyan.copy(alpha = 0.35f),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = trade.subtitleTag,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = AppleSansFamily,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.SemiBold
                ),
                color = accentCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }

  // 3. INTERACTIVE TAKE-OFF CALCULATOR SHEET
  activeTrade?.let { trade ->
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { activeTrade = null },
      sheetState = sheetState,
      containerColor = palette.dialogSurface,
      contentColor = palette.textPrimary,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
      EstimateTakeOffSheetContent(
        trade = trade,
        onClose = { activeTrade = null },
        onAddToBoq = { newItemWithoutId ->
          val stamped = newItemWithoutId.copy(id = nextBoqId++)
          boqCart.add(stamped)
          playCardHapticSound(context, localView)
          Toast.makeText(context, "Added \"${stamped.title}\" to BOQ Cart", Toast.LENGTH_SHORT).show()
          activeTrade = null
        }
      )
    }
  }

  // 4. UNIFIED PROJECT BOQ TAKE-OFF SUMMARY SHEET
  if (isBoqCartOpen) {
    val cartSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { isBoqCartOpen = false },
      sheetState = cartSheetState,
      containerColor = palette.dialogSurface,
      contentColor = palette.textPrimary,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
      ProjectBoqCartSheetContent(
        items = boqCart,
        onRemoveItem = { targetId ->
          boqCart.removeAll { it.id == targetId }
        },
        onClearAll = {
          boqCart.clear()
          Toast.makeText(context, "Project BOQ cleared", Toast.LENGTH_SHORT).show()
        },
        onCopySummary = {
          val text = formatBoqFullSummaryText(boqCart)
          clipboardManager.setText(AnnotatedString(text))
          Toast.makeText(context, "Copied Project BOQ Summary", Toast.LENGTH_SHORT).show()
        },
        onClose = { isBoqCartOpen = false }
      )
    }
  }
}

// ============================================================================
// INTERACTIVE TAKE-OFF CALCULATOR SHEET (ALL 8 MAX FAJARDO TRADES)
// ============================================================================

@Composable
private fun EstimateTakeOffSheetContent(
  trade: EstimateTrade,
  onClose: () -> Unit,
  onAddToBoq: (BoqCartItem) -> Unit
) {
  val palette = currentGlassPalette()
  val isDarkMode = palette.isDark
  val accentCyan = palette.accentIndigo

  // Trade-specific state (kept clean inside sheet session)
  var customLabel by remember(trade) { mutableStateOf("") }

  // 1. CHB Masonry states
  var chbLenStr by remember(trade) { mutableStateOf("4.0") }
  var chbHtStr by remember(trade) { mutableStateOf("3.0") }
  var chbOpenStr by remember(trade) { mutableStateOf("0") }
  var chbWastePct by remember(trade) { mutableDoubleStateOf(5.0) }
  var chbSize by remember(trade) { mutableStateOf(ChbSizeOption.CHB_15) }
  var chbMix by remember(trade) { mutableStateOf(MortarMixClass.CLASS_B) }
  var chbPlaster by remember(trade) { mutableStateOf(PlasterFacesOption.TWO_FACES) }
  var chbVertSpacing by remember(trade) { mutableStateOf(VertRebarSpacing.CM_60) }
  var chbHorizLayer by remember(trade) { mutableStateOf(HorizRebarLayer.LAYERS_3) }
  var chbCommLen by remember(trade) { mutableDoubleStateOf(6.0) }

  // 2. Concrete Works states
  var concLenStr by remember(trade) { mutableStateOf("1.5") }
  var concWidStr by remember(trade) { mutableStateOf("1.5") }
  var concThickStr by remember(trade) { mutableStateOf("0.40") }
  var concUnitsStr by remember(trade) { mutableStateOf("1") }
  var concWastePct by remember(trade) { mutableDoubleStateOf(5.0) }
  var concClass by remember(trade) { mutableStateOf(ConcreteClassOption.CLASS_A) }

  // 3. Reinforcing Steel states
  var barCutStr by remember(trade) { mutableStateOf("3.6") }
  var barCountStr by remember(trade) { mutableStateOf("12") }
  var barTiesStr by remember(trade) { mutableStateOf("48") }
  var barSplicePct by remember(trade) { mutableDoubleStateOf(0.0) }
  var barDiamMm by remember(trade) { mutableIntStateOf(16) }
  var barCommLen by remember(trade) { mutableDoubleStateOf(6.0) }
  var barTieCm by remember(trade) { mutableIntStateOf(30) }

  // 4. Formworks states
  var formAreaStr by remember(trade) { mutableStateOf("18.0") }
  var formReuses by remember(trade) { mutableIntStateOf(2) }
  var formElement by remember(trade) { mutableStateOf(FormworkElementType.SQUARE_COLUMN) }

  // 5. Roofing states
  var roofRafterStr by remember(trade) { mutableStateOf("5.2") }
  var roofRidgeStr by remember(trade) { mutableStateOf("10.0") }
  var roofSlopes by remember(trade) { mutableIntStateOf(2) }
  var roofWidth by remember(trade) { mutableStateOf(RoofingSheetWidth.RIB_080) }

  // 6. Tileworks states
  var tileLenStr by remember(trade) { mutableStateOf("4.5") }
  var tileWidStr by remember(trade) { mutableStateOf("4.0") }
  var tileWastePct by remember(trade) { mutableDoubleStateOf(10.0) }
  var tileSize by remember(trade) { mutableStateOf(TileSizeOption.TILE_60X60) }

  // 7. Painting states
  var paintLenStr by remember(trade) { mutableStateOf("6.0") }
  var paintHtStr by remember(trade) { mutableStateOf("3.0") }
  var paintOpenStr by remember(trade) { mutableStateOf("2.0") }
  var paintCoats by remember(trade) { mutableIntStateOf(2) }
  var paintSurface by remember(trade) { mutableStateOf(PaintSurfaceOption.CONCRETE_MASONRY) }

  // 8. Earthworks states
  var earthLenStr by remember(trade) { mutableStateOf("1.5") }
  var earthWidStr by remember(trade) { mutableStateOf("1.5") }
  var earthDepthStr by remember(trade) { mutableStateOf("1.2") }
  var earthUnitsStr by remember(trade) { mutableStateOf("4") }
  var earthGravelStr by remember(trade) { mutableStateOf("0.10") }
  var earthSwellPct by remember(trade) { mutableDoubleStateOf(25.0) }

  // Live computations + dynamic CAD specs + pending BOQ cart item
  var blueprintSpecs: EstimateBlueprintSpecs
  var materialRows: List<EstimateMaterialRow>
  var preparedBoqItem: BoqCartItem

  when (trade) {
    EstimateTrade.CHB_MASONRY -> {
      val l = chbLenStr.toDoubleOrNull() ?: 0.0
      val h = chbHtStr.toDoubleOrNull() ?: 0.0
      val o = chbOpenStr.toDoubleOrNull() ?: 0.0
      val res = computeChbMasonry(
        l, h, o, chbWastePct, chbSize, chbMix, chbPlaster, chbVertSpacing, chbHorizLayer, chbCommLen
      )
      blueprintSpecs = EstimateBlueprintSpecs(
        trade = trade,
        primaryDimLabel = "L = ${estFmt2(l)} m",
        secondaryDimLabel = "H=${estFmt2(h)}m",
        badgeNote = "Net Area = ${estFmt2(res.netArea)} m² (${chbSize.label})"
      )
      materialRows = res.rows
      val defaultTitle = "CHB Wall (${estFmt1(l)}m × ${estFmt1(h)}m)"
      preparedBoqItem = BoqCartItem(
        id = 0L,
        trade = trade,
        title = customLabel.trim().ifEmpty { defaultTitle },
        specSummary = "${chbSize.label}, ${chbMix.label}, ${chbPlaster.label}, Net ${estFmt2(res.netArea)} m²",
        keyMaterialsText = listOf(
          "CHB: ${res.chbOrderPcs} pcs",
          "Cement: ${res.totalCementBagsOrder} bags",
          "Sand: ${estFmt2(res.totalSandCuMExact)} m³",
          "Rebar (${estFmt1(chbCommLen)}m): ${res.totalRebarPieces} pcs",
          "Tie Wire #16: ${estFmt2(res.tieWireKgExact)} kg"
        ),
        contribution = BoqMaterialContribution(
          cementBags40kg = res.totalCementBagsExact,
          sandCuM = res.totalSandCuMExact,
          chbPcs = res.chbOrderPcs,
          rebarPcs = res.totalRebarPieces,
          tieWireKg = res.tieWireKgExact
        )
      )
    }

    EstimateTrade.CONCRETE_WORKS -> {
      val l = concLenStr.toDoubleOrNull() ?: 0.0
      val w = concWidStr.toDoubleOrNull() ?: 0.0
      val t = concThickStr.toDoubleOrNull() ?: 0.0
      val n = (concUnitsStr.toIntOrNull() ?: 1).coerceAtLeast(1)
      val res = computeConcreteWorks(l, w, t, n, concWastePct, concClass)
      blueprintSpecs = EstimateBlueprintSpecs(
        trade = trade,
        primaryDimLabel = "${estFmt2(l)}m × ${estFmt2(w)}m",
        secondaryDimLabel = "t=${estFmt2(t)}m",
        badgeNote = "V = ${estFmt3(res.volumeCuM)} m³ (${n} unit${if (n > 1) "s" else ""})"
      )
      materialRows = res.rows
      val defaultTitle = "Concrete (${estFmt2(l)}m × ${estFmt2(w)}m × ${estFmt2(t)}m)"
      preparedBoqItem = BoqCartItem(
        id = 0L,
        trade = trade,
        title = customLabel.trim().ifEmpty { defaultTitle },
        specSummary = "${concClass.label}, Qty=$n, V=${estFmt2(res.volumeCuM)} m³",
        keyMaterialsText = listOf(
          "Cement: ${res.cementBagsOrder} bags",
          "Sand: ${estFmt2(res.sandCuM)} m³",
          "Gravel: ${estFmt2(res.gravelCuM)} m³"
        ),
        contribution = BoqMaterialContribution(
          cementBags40kg = res.cementBagsExact,
          sandCuM = res.sandCuM,
          gravelCuM = res.gravelCuM
        )
      )
    }

    EstimateTrade.REINFORCING_STEEL -> {
      val lCut = barCutStr.toDoubleOrNull() ?: 0.0
      val nBars = (barCountStr.toIntOrNull() ?: 0).coerceAtLeast(0)
      val nTies = (barTiesStr.toIntOrNull() ?: 0).coerceAtLeast(0)
      val res = computeReinforcingSteel(lCut, nBars, nTies, barSplicePct, barDiamMm, barCommLen, barTieCm)
      blueprintSpecs = EstimateBlueprintSpecs(
        trade = trade,
        primaryDimLabel = "${barDiamMm}mm Ø • ${nBars} bars × ${estFmt2(lCut)}m",
        badgeNote = "L_total = ${estFmt2(res.totalLinearM)} m (${res.commercialPieces} pcs)"
      )
      materialRows = res.rows
      val defaultTitle = "Rebar ${barDiamMm}mm Ø (${nBars} × ${estFmt2(lCut)}m)"
      preparedBoqItem = BoqCartItem(
        id = 0L,
        trade = trade,
        title = customLabel.trim().ifEmpty { defaultTitle },
        specSummary = "${estFmt1(barCommLen)}m commercial, $nTies ties @ ${barTieCm}cm",
        keyMaterialsText = listOf(
          "${barDiamMm}mm Ø (${estFmt1(barCommLen)}m): ${res.commercialPieces} pcs (${estFmt2(res.totalWeightKg)} kg)",
          "#16 Tie Wire: ${estFmt2(res.tieWireKgExact)} kg"
        ),
        contribution = BoqMaterialContribution(
          rebarPcs = res.commercialPieces,
          rebarWeightKg = res.totalWeightKg,
          tieWireKg = res.tieWireKgExact
        )
      )
    }

    EstimateTrade.FORMWORKS_LUMBER -> {
      val area = formAreaStr.toDoubleOrNull() ?: 0.0
      val res = computeFormworks(area, formReuses, formElement)
      blueprintSpecs = EstimateBlueprintSpecs(
        trade = trade,
        primaryDimLabel = "A_form = ${estFmt2(area)} m² (${formReuses}x reuse)",
        badgeNote = "${formElement.label} • ${res.plywoodOrderSheets} Plywood"
      )
      materialRows = res.rows
      val defaultTitle = "Formworks (${formElement.label}, ${estFmt1(area)} m²)"
      preparedBoqItem = BoqCartItem(
        id = 0L,
        trade = trade,
        title = customLabel.trim().ifEmpty { defaultTitle },
        specSummary = "${formElement.label}, ${formReuses}x reuse",
        keyMaterialsText = listOf(
          "4'×8' Plywood: ${res.plywoodOrderSheets} sheets",
          "Lumber: ${res.lumberBdFtOrder} bd.ft."
        ),
        contribution = BoqMaterialContribution(
          plywoodSheets = res.plywoodOrderSheets,
          lumberBdFt = res.lumberBdFtExact
        )
      )
    }

    EstimateTrade.ROOFING_PURLINS -> {
      val rafter = roofRafterStr.toDoubleOrNull() ?: 0.0
      val ridge = roofRidgeStr.toDoubleOrNull() ?: 0.0
      val res = computeRoofing(rafter, ridge, roofSlopes, roofWidth)
      blueprintSpecs = EstimateBlueprintSpecs(
        trade = trade,
        primaryDimLabel = "Rafter=${estFmt2(rafter)}m • Ridge=${estFmt2(ridge)}m",
        badgeNote = "$roofSlopes Slope(s) • ${res.totalSheets} GI Sheets"
      )
      materialRows = res.rows
      val defaultTitle = "Roofing (${estFmt1(rafter)}m × ${estFmt1(ridge)}m)"
      preparedBoqItem = BoqCartItem(
        id = 0L,
        trade = trade,
        title = customLabel.trim().ifEmpty { defaultTitle },
        specSummary = "${roofWidth.label}, $roofSlopes slope(s)",
        keyMaterialsText = listOf(
          "GI Sheets: ${res.totalSheets} sheets",
          "Ridge Roll: ${res.ridgeRollPcs} pcs",
          "Tekscrews: ${res.tekscrewsPcs} pcs"
        ),
        contribution = BoqMaterialContribution(
          roofingSheets = res.totalSheets,
          ridgeRollPcs = res.ridgeRollPcs,
          tekscrewPcs = res.tekscrewsPcs
        )
      )
    }

    EstimateTrade.TILEWORKS -> {
      val l = tileLenStr.toDoubleOrNull() ?: 0.0
      val w = tileWidStr.toDoubleOrNull() ?: 0.0
      val res = computeTileworks(l, w, tileWastePct, tileSize)
      blueprintSpecs = EstimateBlueprintSpecs(
        trade = trade,
        primaryDimLabel = "${estFmt2(l)}m × ${estFmt2(w)}m (${tileSize.wCm}×${tileSize.hCm}cm)",
        badgeNote = "Area = ${estFmt2(res.areaSqM)} m² • ${res.tilesOrderPcs} Tiles"
      )
      materialRows = res.rows
      val defaultTitle = "Tileworks (${estFmt1(l)}m × ${estFmt1(w)}m)"
      preparedBoqItem = BoqCartItem(
        id = 0L,
        trade = trade,
        title = customLabel.trim().ifEmpty { defaultTitle },
        specSummary = "${tileSize.label}, ${estFmt0(tileWastePct)}% waste",
        keyMaterialsText = listOf(
          "Tiles: ${res.tilesOrderPcs} pcs",
          "Tile Adhesive: ${res.adhesiveBagsOrder} bags (25kg)",
          "Grout: ${res.groutKgOrder} kg (${res.grout2KgPacks} packs)"
        ),
        contribution = BoqMaterialContribution(
          tilePcs = res.tilesOrderPcs,
          tileAdhesiveBags25kg = res.adhesiveBagsOrder,
          tileGroutKg = res.groutKgExact
        )
      )
    }

    EstimateTrade.PAINTING_WORKS -> {
      val l = paintLenStr.toDoubleOrNull() ?: 0.0
      val h = paintHtStr.toDoubleOrNull() ?: 0.0
      val o = paintOpenStr.toDoubleOrNull() ?: 0.0
      val res = computePaintingWorks(l, h, o, paintCoats, paintSurface)
      blueprintSpecs = EstimateBlueprintSpecs(
        trade = trade,
        primaryDimLabel = "L=${estFmt2(l)}m × H=${estFmt2(h)}m (-${estFmt1(o)}m²)",
        badgeNote = "Net = ${estFmt2(res.netAreaSqM)} m² • $paintCoats Topcoat(s)"
      )
      materialRows = res.rows
      val defaultTitle = "Painting (${estFmt1(l)}m × ${estFmt1(h)}m)"
      preparedBoqItem = BoqCartItem(
        id = 0L,
        trade = trade,
        title = customLabel.trim().ifEmpty { defaultTitle },
        specSummary = "${paintSurface.label.substringBefore(" (")}, $paintCoats topcoat(s)",
        keyMaterialsText = listOf(
          "Primer: ${res.primerGallonsOrder} gal",
          "Topcoat: ${res.topcoatGallonsOrder} gal",
          "Skimcoat: ${res.skimcoatBagsOrder} bags"
        ),
        contribution = BoqMaterialContribution(
          paintGallons = res.primerGallonsExact + res.topcoatGallonsExact,
          skimcoatBags20kg = res.skimcoatBagsOrder
        )
      )
    }

    EstimateTrade.EARTHWORKS -> {
      val l = earthLenStr.toDoubleOrNull() ?: 0.0
      val w = earthWidStr.toDoubleOrNull() ?: 0.0
      val d = earthDepthStr.toDoubleOrNull() ?: 0.0
      val n = (earthUnitsStr.toIntOrNull() ?: 1).coerceAtLeast(1)
      val tg = earthGravelStr.toDoubleOrNull() ?: 0.0
      val res = computeEarthworks(l, w, d, n, tg, earthSwellPct)
      blueprintSpecs = EstimateBlueprintSpecs(
        trade = trade,
        primaryDimLabel = "${estFmt2(l)}m × ${estFmt2(w)}m (G-1=${estFmt2(tg)}m)",
        secondaryDimLabel = "D=${estFmt2(d)}m",
        badgeNote = "Bank=${estFmt2(res.bankExcavationCuM)}m³ • Loose=${estFmt2(res.looseHaulCuM)}m³"
      )
      materialRows = res.rows
      val defaultTitle = "Earthworks (${estFmt1(l)}×${estFmt1(w)}×${estFmt1(d)}m)"
      preparedBoqItem = BoqCartItem(
        id = 0L,
        trade = trade,
        title = customLabel.trim().ifEmpty { defaultTitle },
        specSummary = "Qty=$n, Swell=${estFmt0(earthSwellPct)}%, Bedding=${estFmt2(tg)}m",
        keyMaterialsText = listOf(
          "Excavation (Bank): ${estFmt2(res.bankExcavationCuM)} m³",
          "Hauling (Loose): ${estFmt2(res.looseHaulCuM)} m³",
          "G-1 Bedding: ${estFmt2(res.gravelBeddingCuM)} m³"
        ),
        contribution = BoqMaterialContribution(
          gravelCuM = res.gravelBeddingCuM,
          excavationBankCuM = res.bankExcavationCuM,
          excavationLooseCuM = res.looseHaulCuM
        )
      )
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 18.dp)
      .padding(bottom = 16.dp)
      .testTag("estimate_calculator_sheet"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Sheet Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = trade.title,
          style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = AppleSansFamily,
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = trade.fajardoRef,
          style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          ),
          color = accentCyan,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      IconButton(
        onClick = onClose,
        modifier = Modifier.minimumInteractiveComponentSize()
      ) {
        Icon(Icons.Filled.Close, contentDescription = "Close Calculator", tint = palette.textSecondary)
      }
    }

    val sheetOrientation = rememberScreenOrientationState()

    val renderSheetInputsAndBreakdown: @Composable () -> Unit = {
      // Optional Custom Tag Input
      OutlinedTextField(
        value = customLabel,
        onValueChange = { customLabel = it },
        label = { Text("BOQ Line Item Name (Optional)") },
        placeholder = { Text(preparedBoqItem.title) },
        singleLine = true,
        colors = sibilOutlinedTextFieldColors(),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      )

      // Trade Inputs & Selectors
      when (trade) {
        EstimateTrade.CHB_MASONRY -> {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Length L (m)", chbLenStr, { chbLenStr = it }, Modifier.weight(1f))
            EstNumField("Height H (m)", chbHtStr, { chbHtStr = it }, Modifier.weight(1f))
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Openings (m²)", chbOpenStr, { chbOpenStr = it }, Modifier.weight(1f))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              EstSectionLabel("Wastage %")
              EstChipRow(
                options = listOf(0.0 to "0%", 5.0 to "5%", 10.0 to "10%"),
                selected = chbWastePct,
                onSelect = { chbWastePct = it }
              )
            }
          }
          EstSectionLabel("CHB Size")
          EstChipRow(
            options = ChbSizeOption.entries.map { it to it.label },
            selected = chbSize,
            onSelect = { chbSize = it }
          )
          EstSectionLabel("Mortar & Plaster Mix")
          EstChipRow(
            options = MortarMixClass.entries.map { it to it.label },
            selected = chbMix,
            onSelect = { chbMix = it }
          )
          EstSectionLabel("Plastering Sides (16mm)")
          EstChipRow(
            options = PlasterFacesOption.entries.map { it to it.label },
            selected = chbPlaster,
            onSelect = { chbPlaster = it }
          )
          EstSectionLabel("Vertical Rebar Spacing")
          EstChipRow(
            options = VertRebarSpacing.entries.map { it to it.label },
            selected = chbVertSpacing,
            onSelect = { chbVertSpacing = it }
          )
          EstSectionLabel("Horizontal Rebar Layer")
          EstChipRow(
            options = HorizRebarLayer.entries.map { it to it.label },
            selected = chbHorizLayer,
            onSelect = { chbHorizLayer = it }
          )
          EstSectionLabel("Commercial Rebar Length")
          EstChipRow(
            options = listOf(6.0 to "6.0 m", 7.5 to "7.5 m", 9.0 to "9.0 m", 12.0 to "12.0 m"),
            selected = chbCommLen,
            onSelect = { chbCommLen = it }
          )
        }

        EstimateTrade.CONCRETE_WORKS -> {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Length L (m)", concLenStr, { concLenStr = it }, Modifier.weight(1f))
            EstNumField("Width W (m)", concWidStr, { concWidStr = it }, Modifier.weight(1f))
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Thickness/Ht (m)", concThickStr, { concThickStr = it }, Modifier.weight(1f))
            EstNumField("Units N", concUnitsStr, { concUnitsStr = it }, Modifier.weight(1f), isInt = true)
          }
          EstSectionLabel("Fajardo Concrete Proportion (Table 1-2)")
          EstChipRow(
            options = ConcreteClassOption.entries.map { it to it.label },
            selected = concClass,
            onSelect = { concClass = it }
          )
          EstSectionLabel("Wastage Allowance")
          EstChipRow(
            options = listOf(0.0 to "0%", 5.0 to "5%", 10.0 to "10%"),
            selected = concWastePct,
            onSelect = { concWastePct = it }
          )
        }

        EstimateTrade.REINFORCING_STEEL -> {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Cut Length (m)", barCutStr, { barCutStr = it }, Modifier.weight(1f))
            EstNumField("No. of Bars", barCountStr, { barCountStr = it }, Modifier.weight(1f), isInt = true)
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Intersections (Ties)", barTiesStr, { barTiesStr = it }, Modifier.weight(1f), isInt = true)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              EstSectionLabel("Lap Splice %")
              EstChipRow(
                options = listOf(0.0 to "0%", 10.0 to "10%"),
                selected = barSplicePct,
                onSelect = { barSplicePct = it }
              )
            }
          }
          EstSectionLabel("Bar Diameter")
          EstChipRow(
            options = listOf(10, 12, 16, 20, 25, 28, 32).map { it to "${it}mm" },
            selected = barDiamMm,
            onSelect = { barDiamMm = it }
          )
          EstSectionLabel("Commercial Length")
          EstChipRow(
            options = listOf(6.0, 7.5, 9.0, 10.5, 12.0).map { it to "${estFmt1(it)} m" },
            selected = barCommLen,
            onSelect = { barCommLen = it }
          )
          EstSectionLabel("Tie Wire Cut Length (#16 GI @ 53 m/kg)")
          EstChipRow(
            options = listOf(30 to "30 cm", 40 to "40 cm"),
            selected = barTieCm,
            onSelect = { barTieCm = it }
          )
        }

        EstimateTrade.FORMWORKS_LUMBER -> {
          EstNumField("Contact Area A_form (m²)", formAreaStr, { formAreaStr = it }, Modifier.fillMaxWidth())
          EstSectionLabel("Number of Reuses")
          EstChipRow(
            options = listOf(1 to "1x Reuse", 2 to "2x Reuse", 3 to "3x Reuse"),
            selected = formReuses,
            onSelect = { formReuses = it }
          )
          EstSectionLabel("Structural Element Type (Fajardo Ch. 5)")
          EstChipRow(
            options = FormworkElementType.entries.map { it to "${it.label} (${estFmt2(it.boardFtPerPlywood)})" },
            selected = formElement,
            onSelect = { formElement = it }
          )
        }

        EstimateTrade.ROOFING_PURLINS -> {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Rafter Length (m)", roofRafterStr, { roofRafterStr = it }, Modifier.weight(1f))
            EstNumField("Ridge Length (m)", roofRidgeStr, { roofRidgeStr = it }, Modifier.weight(1f))
          }
          EstSectionLabel("Roof Slopes")
          EstChipRow(
            options = listOf(1 to "1 Slope (Shed)", 2 to "2 Slopes (Gable)"),
            selected = roofSlopes,
            onSelect = { roofSlopes = it }
          )
          EstSectionLabel("Effective Sheet Width")
          EstChipRow(
            options = RoofingSheetWidth.entries.map { it to it.label },
            selected = roofWidth,
            onSelect = { roofWidth = it }
          )
        }

        EstimateTrade.TILEWORKS -> {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Length L (m)", tileLenStr, { tileLenStr = it }, Modifier.weight(1f))
            EstNumField("Width W (m)", tileWidStr, { tileWidStr = it }, Modifier.weight(1f))
          }
          EstSectionLabel("Tile Size")
          EstChipRow(
            options = TileSizeOption.entries.map { it to it.label },
            selected = tileSize,
            onSelect = { tileSize = it }
          )
          EstSectionLabel("Cutting Wastage %")
          EstChipRow(
            options = listOf(5.0 to "5%", 10.0 to "10%"),
            selected = tileWastePct,
            onSelect = { tileWastePct = it }
          )
        }

        EstimateTrade.PAINTING_WORKS -> {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Length L (m)", paintLenStr, { paintLenStr = it }, Modifier.weight(1f))
            EstNumField("Height H (m)", paintHtStr, { paintHtStr = it }, Modifier.weight(1f))
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Openings (m²)", paintOpenStr, { paintOpenStr = it }, Modifier.weight(1f))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              EstSectionLabel("Topcoats")
              EstChipRow(
                options = listOf(1 to "1", 2 to "2", 3 to "3"),
                selected = paintCoats,
                onSelect = { paintCoats = it }
              )
            }
          }
          EstSectionLabel("Surface Type (Coverage)")
          EstChipRow(
            options = PaintSurfaceOption.entries.map { it to it.label },
            selected = paintSurface,
            onSelect = { paintSurface = it }
          )
        }

        EstimateTrade.EARTHWORKS -> {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Length L (m)", earthLenStr, { earthLenStr = it }, Modifier.weight(1f))
            EstNumField("Width W (m)", earthWidStr, { earthWidStr = it }, Modifier.weight(1f))
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Depth D (m)", earthDepthStr, { earthDepthStr = it }, Modifier.weight(1f))
            EstNumField("Qty N", earthUnitsStr, { earthUnitsStr = it }, Modifier.weight(1f), isInt = true)
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstNumField("Gravel Bed t_g (m)", earthGravelStr, { earthGravelStr = it }, Modifier.weight(1f))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              EstSectionLabel("Soil Swell %")
              EstChipRow(
                options = listOf(20.0 to "20%", 25.0 to "25%"),
                selected = earthSwellPct,
                onSelect = { earthSwellPct = it }
              )
            }
          }
        }
      }

      // Live Material Breakdown Cards
      EstSectionLabel("COMPUTED MATERIAL BREAKDOWN (EXACT vs COMMERCIAL ORDER)")
      materialRows.forEach { row ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = palette.nestedCardFill,
          border = palette.nestedBorder,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = row.materialName,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontFamily = AppleSansFamily,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = palette.textPrimary,
                modifier = Modifier.weight(1f)
              )
              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = row.exactValueText,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontFeatureSettings = "tnum"
                  ),
                  color = palette.textSecondary
                )
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentCyan.copy(alpha = if (isDarkMode) 0.24f else 0.14f))
                    .border(1.dp, accentCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Text(
                    text = row.orderQuantityText,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontFamily = JetBrainsMonoFamily,
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.ExtraBold,
                      fontFeatureSettings = "tnum"
                    ),
                    color = accentCyan
                  )
                }
              }
            }
            Text(
              text = row.multiplierFormula,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.5.sp
              ),
              color = palette.textSecondary
            )
          }
        }
      }
    }

    if (sheetOrientation.isLandscape) {
      Row(
        modifier = Modifier
          .weight(1f, fill = false)
          .heightIn(max = if (sheetOrientation.isCompactLandscape) 235.dp else 360.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
      ) {
        Column(
          modifier = Modifier
            .weight(0.40f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          EstimateDynamicBlueprintPreview(
            specs = blueprintSpecs,
            isDarkMode = isDarkMode,
            heightDp = if (sheetOrientation.isCompactLandscape) 156 else 186
          )
        }
        Column(
          modifier = Modifier
            .weight(0.60f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          renderSheetInputsAndBreakdown()
        }
      }
    } else {
      Column(
        modifier = Modifier
          .weight(1f, fill = false)
          .heightIn(max = 520.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        EstimateDynamicBlueprintPreview(
          specs = blueprintSpecs,
          isDarkMode = isDarkMode
        )
        renderSheetInputsAndBreakdown()
      }
    }

    // "+ Add to Project BOQ" CTA
    Button(
      onClick = { onAddToBoq(preparedBoqItem) },
      colors = ButtonDefaults.buttonColors(containerColor = accentCyan, contentColor = Color.White),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("estimate_add_to_boq_button")
    ) {
      Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(6.dp))
      Text(
        text = "+ Add to Project BOQ",
        style = MaterialTheme.typography.titleSmall.copy(
          fontFamily = AppleSansFamily,
          fontWeight = FontWeight.Bold
        )
      )
    }
  }
}

// ============================================================================
// UNIFIED PROJECT BOQ TAKE-OFF SUMMARY SHEET (`BOQ Cart`)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectBoqCartSheetContent(
  items: List<BoqCartItem>,
  onRemoveItem: (Long) -> Unit,
  onClearAll: () -> Unit,
  onCopySummary: () -> Unit,
  onClose: () -> Unit
) {
  val palette = currentGlassPalette()
  val isDarkMode = palette.isDark
  val accentCyan = palette.accentIndigo
  val totals = remember(items.toList()) { consolidateBoqCart(items) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 18.dp)
      .padding(bottom = 16.dp)
      .testTag("boq_cart_sheet"),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Project BOQ Take-Off Cart",
          style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = AppleSansFamily,
            fontWeight = FontWeight.Bold
          ),
          color = palette.textPrimary
        )
        Text(
          text = "${items.size} line item${if (items.size == 1) "" else "s"} • Max Fajardo Totals",
          style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = AppleSansFamily,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold
          ),
          color = accentCyan
        )
      }
      IconButton(
        onClick = onClose,
        modifier = Modifier.minimumInteractiveComponentSize()
      ) {
        Icon(Icons.Filled.Close, contentDescription = "Close BOQ Cart", tint = palette.textSecondary)
      }
    }

    if (items.isEmpty()) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = palette.nestedCardFill,
        border = palette.nestedBorder,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Inventory2,
            contentDescription = null,
            tint = accentCyan,
            modifier = Modifier.size(34.dp)
          )
          Text(
            text = "No BOQ Items Yet",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.textPrimary
          )
          Text(
            text = "Open any of the 8 CAD trades, adjust dimensions, and tap \"+ Add to Project BOQ\" to build your consolidated take-off.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary
          )
        }
      }
    } else {
      val cartOrientation = rememberScreenOrientationState()
      Column(
        modifier = Modifier
          .weight(1f, fill = false)
          .heightIn(max = if (cartOrientation.isCompactLandscape) 225.dp else 500.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Consolidated Grand Total Card
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = accentCyan.copy(alpha = if (isDarkMode) 0.16f else 0.10f),
          border = BorderStroke(1.2.dp, accentCyan.copy(alpha = 0.45f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("boq_consolidated_totals")
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "CONSOLIDATED GRAND TOTAL",
              style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
              ),
              color = accentCyan
            )
            val totalPairs = buildList {
              if (totals.cementBags40kg > 0.0) add("Cement (40kg)" to "${ceil(totals.cementBags40kg).toInt()} bags")
              if (totals.sandCuM > 0.0) add("Washed Sand" to "${estFmt2(totals.sandCuM)} m³")
              if (totals.gravelCuM > 0.0) add("Crushed Gravel / G-1" to "${estFmt2(totals.gravelCuM)} m³")
              if (totals.chbPcs > 0) add("CHB Blocks" to "${totals.chbPcs} pcs")
              if (totals.rebarPcs > 0) add("Deformed Rebars" to "${totals.rebarPcs} pcs")
              if (totals.rebarWeightKg > 0.0) add("Rebar Steel Weight" to "${estFmt2(totals.rebarWeightKg)} kg")
              if (totals.tieWireKg > 0.0) add("#16 GI Tie Wire" to "${ceil(totals.tieWireKg).toInt()} kg")
              if (totals.plywoodSheets > 0) add("Form Plywood (4'×8')" to "${totals.plywoodSheets} sheets")
              if (totals.lumberBdFt > 0.0) add("Framing Lumber" to "${ceil(totals.lumberBdFt).toInt()} bd.ft.")
              if (totals.roofingSheets > 0) add("Roofing Sheets" to "${totals.roofingSheets} sheets")
              if (totals.ridgeRollPcs > 0) add("Ridge Roll (2.40m)" to "${totals.ridgeRollPcs} pcs")
              if (totals.tekscrewPcs > 0) add("Tekscrews" to "${totals.tekscrewPcs} pcs")
              if (totals.tilePcs > 0) add("Tiles" to "${totals.tilePcs} pcs")
              if (totals.tileAdhesiveBags25kg > 0) add("Tile Adhesive (25kg)" to "${totals.tileAdhesiveBags25kg} bags")
              if (totals.tileGroutKg > 0.0) add("Tile Grout" to "${ceil(totals.tileGroutKg).toInt()} kg")
              if (totals.paintGallons > 0.0) add("Paint (Primer+Top)" to "${ceil(totals.paintGallons).toInt()} gal")
              if (totals.skimcoatBags20kg > 0) add("Skimcoat (20kg)" to "${totals.skimcoatBags20kg} bags")
              if (totals.excavationBankCuM > 0.0) add("Excavation (Bank)" to "${estFmt2(totals.excavationBankCuM)} m³")
              if (totals.excavationLooseCuM > 0.0) add("Haul Vol (Loose)" to "${estFmt2(totals.excavationLooseCuM)} m³")
            }
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              totalPairs.forEach { (mat, qty) ->
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = palette.dialogSurface,
                  border = palette.nestedBorder
                ) {
                  Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(
                      text = mat,
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                      color = palette.textSecondary
                    )
                    Text(
                      text = qty,
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFeatureSettings = "tnum"
                      ),
                      color = palette.textPrimary
                    )
                  }
                }
              }
            }
          }
        }

        // Added Line Items
        EstSectionLabel("ADDED TAKE-OFF ITEMS (${items.size})")
        items.forEach { item ->
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = palette.nestedCardFill,
            border = palette.nestedBorder,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Text(
                  text = item.title,
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = AppleSansFamily,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = palette.textPrimary
                )
                Text(
                  text = item.specSummary,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                  color = accentCyan
                )
                Text(
                  text = item.keyMaterialsText.joinToString(" • "),
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.5.sp,
                    fontFeatureSettings = "tnum"
                  ),
                  color = palette.textSecondary
                )
              }
              IconButton(
                onClick = { onRemoveItem(item.id) },
                modifier = Modifier.minimumInteractiveComponentSize()
              ) {
                Icon(
                  imageVector = Icons.Filled.DeleteOutline,
                  contentDescription = "Remove Item",
                  tint = palette.accentRed,
                  modifier = Modifier.size(19.dp)
                )
              }
            }
          }
        }
      }

      // Copy & Clear Action Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onClearAll,
          border = BorderStroke(1.dp, palette.accentRed.copy(alpha = 0.55f)),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .weight(0.9f)
            .height(48.dp)
            .testTag("boq_clear_list_button")
        ) {
          Icon(Icons.Filled.Delete, contentDescription = null, tint = palette.accentRed, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("Clear List", color = palette.accentRed, fontWeight = FontWeight.Bold, maxLines = 1)
        }

        Button(
          onClick = onCopySummary,
          colors = ButtonDefaults.buttonColors(containerColor = accentCyan, contentColor = Color.White),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .weight(1.4f)
            .height(48.dp)
            .testTag("boq_copy_summary_button")
        ) {
          Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Copy Full BOQ Summary", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
      }
    }
  }
}

// ============================================================================
// SHARED COMPACT INPUT & CHIP HELPERS FOR ESTIMATE SHEETS
// ============================================================================

@Composable
private fun EstNumField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  isInt: Boolean = false
) {
  val palette = currentGlassPalette()
  OutlinedTextField(
    value = value,
    onValueChange = { raw ->
      val filtered = raw.filter { c ->
        c.isDigit() || (!isInt && c == '.')
      }
      onValueChange(filtered)
    },
    label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.5.sp) },
    singleLine = true,
    textStyle = MaterialTheme.typography.bodyMedium.copy(
      fontFamily = JetBrainsMonoFamily,
      fontWeight = FontWeight.Bold,
      fontFeatureSettings = "tnum",
      color = palette.textPrimary
    ),
    keyboardOptions = KeyboardOptions(
      keyboardType = if (isInt) KeyboardType.Number else KeyboardType.Decimal
    ),
    colors = sibilOutlinedTextFieldColors(),
    shape = RoundedCornerShape(14.dp),
    modifier = modifier
  )
}

@Composable
private fun EstSectionLabel(text: String) {
  val palette = currentGlassPalette()
  Text(
    text = text,
    style = MaterialTheme.typography.labelSmall.copy(
      fontFamily = JetBrainsMonoFamily,
      fontSize = 10.5.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp
    ),
    color = palette.textSecondary
  )
}

@Composable
private fun <T> EstChipRow(
  options: List<Pair<T, String>>,
  selected: T,
  onSelect: (T) -> Unit
) {
  val palette = currentGlassPalette()
  val accentCyan = palette.accentIndigo
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    options.forEach { (optionVal, label) ->
      val isSel = optionVal == selected
      RecessedGlassPill(
        isDarkMode = palette.isDark,
        fillColor = if (isSel) accentCyan.copy(alpha = if (palette.isDark) 0.28f else 0.16f) else null,
        borderColor = if (isSel) accentCyan else null,
        onClick = { onSelect(optionVal) },
        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = AppleSansFamily,
            fontSize = 11.5.sp,
            fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold
          ),
          color = if (isSel) accentCyan else palette.textPrimary,
          maxLines = 1,
          softWrap = false
        )
      }
    }
  }
}

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CanvaLightColorScheme = lightColorScheme(
  primary = MutedIndigoPrimary,
  onPrimary = Color.White,
  primaryContainer = SoftIndigoContainer,
  onPrimaryContainer = DeepIndigoOnContainer,
  secondary = SlateBlueSecondary,
  onSecondary = Color.White,
  secondaryContainer = SoftCardTint,
  onSecondaryContainer = DarkCharcoalText,
  tertiary = SoftSageTertiary,
  onTertiary = Color.White,
  tertiaryContainer = SoftSageContainer,
  onTertiaryContainer = Color(0xFF115E59),
  background = OffWhiteBackground,
  onBackground = DarkCharcoalText,
  surface = PureWhiteSurface,
  onSurface = DarkCharcoalText,
  surfaceVariant = SoftCardTint,
  onSurfaceVariant = MutedSlateSubtext
)

val CanvaDarkColorScheme = darkColorScheme(
  primary = DarkIndigoPrimary,
  onPrimary = Color(0xFF0F172A),
  primaryContainer = DarkIndigoContainer,
  onPrimaryContainer = LightIndigoOnContainer,
  secondary = DarkSlateSecondary,
  onSecondary = Color(0xFF0F172A),
  secondaryContainer = DarkCardVariant,
  onSecondaryContainer = CrispLightText,
  tertiary = DarkSageTertiary,
  onTertiary = Color(0xFF0F172A),
  tertiaryContainer = DarkSageContainer,
  onTertiaryContainer = Color(0xFFCCFBF1),
  background = DeepCharcoalBackground,
  onBackground = CrispLightText,
  surface = ElevatedDarkCardSurface,
  onSurface = CrispLightText,
  surfaceVariant = DarkCardVariant,
  onSurfaceVariant = MutedLightSubtext
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) CanvaDarkColorScheme else CanvaLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = GoldPrimary,
  onPrimary = BlackMain,
  primaryContainer = GoldDark,
  onPrimaryContainer = GoldLight,
  secondary = GoldWarm,
  onSecondary = BlackMain,
  secondaryContainer = BlackSurfaceVariant,
  onSecondaryContainer = GoldLight,
  tertiary = GoldLight,
  onTertiary = BlackMain,
  background = BlackMain,
  onBackground = TextPrimaryDark,
  surface = BlackSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = BlackSurfaceVariant,
  onSurfaceVariant = TextSecondaryDark,
  outline = BlackCardBorder,
  outlineVariant = BlackDivider,
  error = RedDiscount,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = GoldVariant,
  onPrimary = Color.White,
  primaryContainer = GoldLight,
  onPrimaryContainer = BlackMain,
  secondary = GoldDark,
  onSecondary = Color.White,
  secondaryContainer = LightSurfaceVariant,
  onSecondaryContainer = TextPrimaryLight,
  tertiary = GoldPrimary,
  onTertiary = Color.White,
  background = LightCanvas,
  onBackground = TextPrimaryLight,
  surface = LightSurface,
  onSurface = TextPrimaryLight,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = TextSecondaryLight,
  outline = LightCardBorder,
  outlineVariant = LightCardBorder,
  error = RedDiscount,
  onError = Color.White
)

@Composable
fun ArshadCollectionTheme(
  darkTheme: Boolean = true, // Default to luxury dark mode
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

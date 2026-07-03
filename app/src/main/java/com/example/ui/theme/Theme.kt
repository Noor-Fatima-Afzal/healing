package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = EmeraldGreen,
  onPrimary = Color.White,
  primaryContainer = SoftGoldBg,
  onPrimaryContainer = DarkEmerald,
  secondary = Gold,
  onSecondary = Color.White,
  secondaryContainer = PaleGold,
  onSecondaryContainer = DarkEmerald,
  background = WarmWhite,
  onBackground = DarkGray,
  surface = Color.White,
  onSurface = DarkGray,
  surfaceVariant = SoftGoldBg,
  onSurfaceVariant = DarkEmerald,
  error = ErrorRed,
  onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
  primary = LightEmerald,
  onPrimary = Color.White,
  primaryContainer = DarkEmerald,
  onPrimaryContainer = PaleGold,
  secondary = Gold,
  onSecondary = Color.Black,
  background = Color(0xFF141E0A),
  onBackground = WarmWhite,
  surface = Color(0xFF1B2414),
  onSurface = WarmWhite,
  surfaceVariant = Color(0xFF222C1B),
  onSurfaceVariant = PaleGold,
  error = ErrorRed,
  onError = Color.White,
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Force brand colors for consistent luxury Islamic feel
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

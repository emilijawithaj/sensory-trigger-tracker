package com.example.soverloadtracker.presentation.theme
import android.app.Activity
import android.os.Build
import androidx.wear.compose.material3.MaterialTheme.typography
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.dynamicColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.MotionScheme
import androidx.wear.compose.material3.Shapes


private val darkScheme = ColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    onSurface = onSurfaceLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = darkScheme

  MaterialTheme(
    colorScheme = colorScheme,
    content = content,
      typography = typography,
      shapes = Shapes(),
      motionScheme = MotionScheme.standard(),
  )
}


package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp

private val ClassicGreenColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32), // Classic Leaf Green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF795548), // Warm Muted Brown
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFEBE9),
    onSecondaryContainer = Color(0xFF3E2723),
    background = Color(0xFFFAF9F4), // Warm Milk/Cream White
    onBackground = Color(0xFF243225), // Deep organic dark text
    surface = Color.White,
    onSurface = Color(0xFF243225),
    error = Color(0xFFD32F2F),
    onError = Color.White
)

private val ParrotGreenColorScheme = lightColorScheme(
    primary = Color(0xFF4CD137), // Vibrant Parrot Green
    onPrimary = Color.White,     // White text on primary (as requested!)
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF1B5E20), // Rich Green
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F8E9),
    onSecondaryContainer = Color(0xFF1B5E20),
    background = Color.White,    // White background
    onBackground = Color.Black,  // Pure black text on background (as requested!)
    surface = Color.White,       // White background
    onSurface = Color.Black,     // Pure black/dark text on background (as requested!)
    error = Color(0xFFD32F2F),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64DD17), // Bright green
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF81C784),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2E7D32),
    onSecondaryContainer = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val HighContrastColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEEEEE),
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDDDDD),
    onSecondaryContainer = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFD32F2F),
    onError = Color.White
)

fun getDynamicTypography(appFont: AppFontFamily): Typography {
    val family = when (appFont) {
        AppFontFamily.DEFAULT -> FontFamily.Default
        AppFontFamily.SAN_SERIF -> FontFamily.SansSerif
        AppFontFamily.SERIF -> FontFamily.Serif
        AppFontFamily.MONOSPACE -> FontFamily.Monospace
        AppFontFamily.CURSIVE -> FontFamily.Cursive
    }
    return Typography(
        displayLarge = TextStyle(fontFamily = family, fontSize = 57.sp, lineHeight = 64.sp),
        displayMedium = TextStyle(fontFamily = family, fontSize = 45.sp, lineHeight = 52.sp),
        displaySmall = TextStyle(fontFamily = family, fontSize = 36.sp, lineHeight = 44.sp),
        headlineLarge = TextStyle(fontFamily = family, fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
        headlineMedium = TextStyle(fontFamily = family, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
        headlineSmall = TextStyle(fontFamily = family, fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontFamily = family, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
        titleMedium = TextStyle(fontFamily = family, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
        titleSmall = TextStyle(fontFamily = family, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        bodyLarge = TextStyle(fontFamily = family, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = family, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = family, fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = TextStyle(fontFamily = family, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        labelMedium = TextStyle(fontFamily = family, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
        labelSmall = TextStyle(fontFamily = family, fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (AppSettingsState.themeMode) {
        AppThemeMode.CLASSIC_GREEN -> ClassicGreenColorScheme
        AppThemeMode.PARROT_GREEN -> ParrotGreenColorScheme
        AppThemeMode.DARK_MODE -> DarkColorScheme
        AppThemeMode.HIGH_CONTRAST -> HighContrastColorScheme
    }

    val typography = getDynamicTypography(AppSettingsState.fontFamily)

    // Override fontScale in density globally
    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * AppSettingsState.getFontScale()
    )

    CompositionLocalProvider(LocalDensity provides customDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

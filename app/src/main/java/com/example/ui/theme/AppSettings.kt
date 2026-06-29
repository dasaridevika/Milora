package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

enum class AppThemeMode {
    CLASSIC_GREEN,
    PARROT_GREEN,
    DARK_MODE,
    HIGH_CONTRAST
}

enum class AppFontFamily {
    DEFAULT,
    SAN_SERIF,
    SERIF,
    MONOSPACE,
    CURSIVE
}

enum class AppFontSize {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

object AppSettingsState {
    var themeMode by mutableStateOf(AppThemeMode.CLASSIC_GREEN)
    var fontFamily by mutableStateOf(AppFontFamily.DEFAULT)
    var fontSizeMode by mutableStateOf(AppFontSize.MEDIUM)

    fun load(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedTheme = prefs.getString("theme_mode", AppThemeMode.CLASSIC_GREEN.name) ?: AppThemeMode.CLASSIC_GREEN.name
        val savedFont = prefs.getString("font_family", AppFontFamily.DEFAULT.name) ?: AppFontFamily.DEFAULT.name
        val savedSize = prefs.getString("font_size_mode", AppFontSize.MEDIUM.name) ?: AppFontSize.MEDIUM.name
        
        themeMode = try { AppThemeMode.valueOf(savedTheme) } catch (e: Exception) { AppThemeMode.CLASSIC_GREEN }
        fontFamily = try { AppFontFamily.valueOf(savedFont) } catch (e: Exception) { AppFontFamily.DEFAULT }
        fontSizeMode = try { AppFontSize.valueOf(savedSize) } catch (e: Exception) { AppFontSize.MEDIUM }
    }

    fun save(context: Context, theme: AppThemeMode, font: AppFontFamily, size: AppFontSize) {
        val prefs = context.applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("theme_mode", theme.name)
            putString("font_family", font.name)
            putString("font_size_mode", size.name)
            apply()
        }
        themeMode = theme
        fontFamily = font
        fontSizeMode = size
    }

    fun getFontScale(): Float {
        return when (fontSizeMode) {
            AppFontSize.SMALL -> 0.85f
            AppFontSize.MEDIUM -> 1.0f
            AppFontSize.LARGE -> 1.15f
            AppFontSize.EXTRA_LARGE -> 1.35f
        }
    }
}

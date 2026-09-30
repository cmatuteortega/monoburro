package com.cmatuteortega.monoburro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.storage.ThemeMode

// Warm taqueria palette: salsa, avocado, tortilla, cream and charred brown.
val Salsa = Color(0xFFE4572E)
val SalsaLight = Color(0xFFFF8A5B)
val Avocado = Color(0xFF6A994E)
val AvocadoLight = Color(0xFF9BC37A)
val Tortilla = Color(0xFFE9B872)
val Cream = Color(0xFFFFF6EC)
val Crust = Color(0xFFFFFBF5)
val Char = Color(0xFF2B1D14)
val Night = Color(0xFF1B1411)
val Ember = Color(0xFF2A201B)

private val Light = lightColorScheme(
    primary = Salsa, onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF), onPrimaryContainer = Color(0xFF3A0B00),
    secondary = Avocado, onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDEFCB), onSecondaryContainer = Color(0xFF16270A),
    tertiary = Color(0xFFB7791F), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFBE3BC), onTertiaryContainer = Color(0xFF3B2600),
    background = Cream, onBackground = Char,
    surface = Crust, onSurface = Char,
    surfaceVariant = Color(0xFFF4E4D3), onSurfaceVariant = Color(0xFF6B5647),
    surfaceContainer = Color(0xFFFBEEE1), surfaceContainerHigh = Color(0xFFF7E7D6),
    outline = Color(0xFFCDB6A2), outlineVariant = Color(0xFFE8D6C4),
)

private val Dark = darkColorScheme(
    primary = SalsaLight, onPrimary = Color(0xFF4A1400),
    primaryContainer = Color(0xFF7A2E12), onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = AvocadoLight, onSecondary = Color(0xFF1C3309),
    secondaryContainer = Color(0xFF34501F), onSecondaryContainer = Color(0xFFDDEFCB),
    tertiary = Tortilla, onTertiary = Color(0xFF3B2600),
    tertiaryContainer = Color(0xFF5B4113), onTertiaryContainer = Color(0xFFFBE3BC),
    background = Night, onBackground = Color(0xFFF6E7DA),
    surface = Ember, onSurface = Color(0xFFF6E7DA),
    surfaceVariant = Color(0xFF3A2C24), onSurfaceVariant = Color(0xFFD7C0AE),
    surfaceContainer = Color(0xFF30251F), surfaceContainerHigh = Color(0xFF3A2D26),
    outline = Color(0xFF8C7565), outlineVariant = Color(0xFF4A3A31),
)

/** One colour per filling category: slider tracks, the ratio bar, chips. */
fun Category.color(dark: Boolean): Color = when (this) {
    Category.PROTEIN -> if (dark) SalsaLight else Salsa
    Category.CARB -> if (dark) Tortilla else Color(0xFFD79A3C)
    Category.VEG -> if (dark) AvocadoLight else Avocado
    Category.CHEESE -> if (dark) Color(0xFFF6D365) else Color(0xFFE0B21B)
    Category.SAUCE -> if (dark) Color(0xFFC9A3E6) else Color(0xFF8E5BB5)
    Category.TORTILLA -> Tortilla
}

private val Rounded = FontFamily.SansSerif
private val AppTypography = Typography().let { t ->
    t.copy(
        displaySmall = t.displaySmall.copy(fontWeight = FontWeight.Black, fontFamily = Rounded),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontFamily = Rounded),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontFamily = Rounded),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = Rounded),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.Bold),
        labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp),
    )
}

@Composable
fun isDark(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/** Whether the app (not necessarily the system) is currently dark. */
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun MonoburroTheme(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDarkTheme provides dark) {
        MaterialTheme(colorScheme = if (dark) Dark else Light, typography = AppTypography, content = content)
    }
}

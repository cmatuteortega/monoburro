package com.cmatuteortega.monoburro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.R
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.storage.ThemeMode

// Warm taqueria palette: salsa, avocado, tortilla, cream and charred brown.
val Salsa = Color(0xFFE4572E)
val SalsaLight = Color(0xFFFF8A5B)
val Avocado = Color(0xFF5E8F43)
val AvocadoLight = Color(0xFF9BC37A)
val Tortilla = Color(0xFFE9B872)
val Gold = Color(0xFFD9941E)
val Plum = Color(0xFF8E5BB5)
val PlumLight = Color(0xFFC9A3E6)
val Cream = Color(0xFFF8F2EA)
val Char = Color(0xFF2B1D14)
val Night = Color(0xFF151110)
val Ember = Color(0xFF201A17)

private val Light = lightColorScheme(
    primary = Salsa, onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDD0), onPrimaryContainer = Color(0xFF3A0B00),
    secondary = Avocado, onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDEFCB), onSecondaryContainer = Color(0xFF16270A),
    tertiary = Color(0xFFB7791F), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFBE3BC), onTertiaryContainer = Color(0xFF3B2600),
    background = Cream, onBackground = Char,
    surface = Color(0xFFFFFCF8), onSurface = Char,
    surfaceVariant = Color(0xFFF0E5D8), onSurfaceVariant = Color(0xFF75604F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFCF7F1),
    surfaceContainer = Color(0xFFF3EBE1), surfaceContainerHigh = Color(0xFFEDE3D7),
    surfaceContainerHighest = Color(0xFFE6DACC),
    outline = Color(0xFFCDB6A2), outlineVariant = Color(0xFFEADFD2),
)

private val Dark = darkColorScheme(
    primary = SalsaLight, onPrimary = Color(0xFF4A1400),
    primaryContainer = Color(0xFF6E2A12), onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = AvocadoLight, onSecondary = Color(0xFF1C3309),
    secondaryContainer = Color(0xFF2F4A1C), onSecondaryContainer = Color(0xFFDDEFCB),
    tertiary = Tortilla, onTertiary = Color(0xFF3B2600),
    tertiaryContainer = Color(0xFF524013), onTertiaryContainer = Color(0xFFFBE3BC),
    background = Night, onBackground = Color(0xFFF6E7DA),
    surface = Ember, onSurface = Color(0xFFF6E7DA),
    surfaceVariant = Color(0xFF3A2C24), onSurfaceVariant = Color(0xFFCDB8A7),
    surfaceContainerLowest = Color(0xFF100D0C),
    surfaceContainerLow = Color(0xFF1C1715),
    surfaceContainer = Color(0xFF2A221E), surfaceContainerHigh = Color(0xFF342A25),
    surfaceContainerHighest = Color(0xFF3F332D),
    outline = Color(0xFF8C7565), outlineVariant = Color(0xFF3A2F29),
)

/** One colour per filling category: slider tracks, the ratio bar, chips. */
fun Category.color(dark: Boolean): Color = when (this) {
    Category.PROTEIN -> if (dark) SalsaLight else Salsa
    Category.CARB -> if (dark) Tortilla else Color(0xFFD79A3C)
    Category.VEG -> if (dark) AvocadoLight else Avocado
    Category.CHEESE -> if (dark) Color(0xFFF6D365) else Color(0xFFE0B21B)
    Category.SAUCE -> if (dark) PlumLight else Plum
    Category.TORTILLA -> Tortilla
}

/** The four numbers the app talks about, each with one colour everywhere it shows up. */
enum class Macro(val label: String, val short: String, val unit: String) {
    KCAL("Calories", "kcal", "kcal"),
    PROTEIN("Protein", "protein", "g"),
    CARBS("Carbs", "carbs", "g"),
    FAT("Fat", "fat", "g"),
}

fun Macro.color(dark: Boolean): Color = when (this) {
    Macro.KCAL -> if (dark) SalsaLight else Salsa
    Macro.PROTEIN -> if (dark) AvocadoLight else Avocado
    Macro.CARBS -> if (dark) Tortilla else Gold
    Macro.FAT -> if (dark) PlumLight else Plum
}

/** Bricolage Grotesque (SIL OFL, licenses/): headlines, titles and the big numbers. */
val Display = FontFamily(
    Font(R.font.bricolage_semibold, FontWeight.SemiBold),
    Font(R.font.bricolage_bold, FontWeight.Bold),
    Font(R.font.bricolage_extrabold, FontWeight.ExtraBold),
    Font(R.font.bricolage_extrabold, FontWeight.Black),
)

private val AppTypography = Typography().let { t ->
    fun TextStyle.display(weight: FontWeight, tracking: Double = -0.2) =
        copy(fontFamily = Display, fontWeight = weight, letterSpacing = tracking.sp)
    t.copy(
        displayLarge = t.displayLarge.display(FontWeight.ExtraBold, -1.5),
        displayMedium = t.displayMedium.display(FontWeight.ExtraBold, -1.2),
        displaySmall = t.displaySmall.display(FontWeight.ExtraBold, -1.0),
        headlineLarge = t.headlineLarge.display(FontWeight.ExtraBold, -0.8),
        headlineMedium = t.headlineMedium.display(FontWeight.ExtraBold, -0.6),
        headlineSmall = t.headlineSmall.display(FontWeight.ExtraBold, -0.4),
        titleLarge = t.titleLarge.display(FontWeight.Bold, -0.3),
        titleMedium = t.titleMedium.display(FontWeight.Bold, -0.1),
        titleSmall = t.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp),
        labelSmall = t.labelSmall.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

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
        MaterialTheme(
            colorScheme = if (dark) Dark else Light,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

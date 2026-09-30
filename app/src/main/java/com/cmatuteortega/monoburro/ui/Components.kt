package com.cmatuteortega.monoburro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cmatuteortega.monoburro.model.Macros
import kotlin.math.roundToInt

/** kcal / protein / carbs / fat as four compact tiles. */
@Composable
fun MacroRow(macros: Macros, modifier: Modifier = Modifier, compact: Boolean = false) {
    val cs = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MacroTile("${macros.kcal.roundToInt()}", "kcal", cs.primaryContainer, cs.onPrimaryContainer, compact, Modifier.weight(1f))
        MacroTile("${macros.protein.roundToInt()} g", "protein", cs.secondaryContainer, cs.onSecondaryContainer, compact, Modifier.weight(1f))
        MacroTile("${macros.carbs.roundToInt()} g", "carbs", cs.tertiaryContainer, cs.onTertiaryContainer, compact, Modifier.weight(1f))
        MacroTile("${macros.fat.roundToInt()} g", "fat", cs.surfaceVariant, cs.onSurfaceVariant, compact, Modifier.weight(1f))
    }
}

@Composable
private fun MacroTile(value: String, label: String, bg: Color, fg: Color, compact: Boolean, modifier: Modifier) {
    Surface(modifier, color = bg, contentColor = fg, shape = RoundedCornerShape(14.dp)) {
        Column(
            Modifier.padding(vertical = if (compact) 6.dp else 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                value,
                style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** Full-width primary action pinned to the bottom, above the gesture bar. */
@Composable
fun BottomAction(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
        ) { Text(text, style = MaterialTheme.typography.labelLarge) }
    }
}

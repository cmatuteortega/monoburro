package com.cmatuteortega.monoburro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/** A rounded, outlined card: the main menu's basic block. */
@Composable
fun MenuCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(22.dp)
    val border = BorderStroke(1.dp, cs.outlineVariant)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = cs.surface, border = border, content = content)
    } else {
        Surface(modifier = modifier, shape = shape, color = cs.surface, border = border, content = content)
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier.padding(top = 22.dp, bottom = 10.dp)) {
        Text(text, style = MaterialTheme.typography.titleLarge)
        subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** − value + with big round buttons. */
@Composable
fun Stepper(
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
    minusEnabled: Boolean = true,
    plusEnabled: Boolean = true,
    buttonSize: Dp = 44.dp,
    valueWidth: Dp = 72.dp,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = onMinus, enabled = minusEnabled, modifier = Modifier.size(buttonSize)) {
            Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(valueWidth),
        )
        FilledTonalIconButton(onClick = onPlus, enabled = plusEnabled, modifier = Modifier.size(buttonSize)) {
            Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * A numeric text field. Keeps what's typed locally (so "72." survives) and
 * reports every edit; [onValue] gets null when the field is cleared.
 */
@Composable
fun NumberField(
    value: Number?,
    onValue: (Double?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    decimals: Boolean = false,
    maxLength: Int = 5,
) {
    var text by remember { mutableStateOf(value?.let(::formatNumber).orEmpty()) }
    OutlinedTextField(
        value = text,
        onValueChange = { v ->
            val cleaned = v.replace(',', '.').filter { it.isDigit() || (decimals && it == '.') }.take(maxLength)
            if (cleaned.count { it == '.' } > 1) return@OutlinedTextField
            text = cleaned
            onValue(cleaned.toDoubleOrNull())
        },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimals) KeyboardType.Decimal else KeyboardType.Number),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
    )
}

private fun formatNumber(n: Number): String {
    val d = n.toDouble()
    return if (d % 1.0 == 0.0) d.toLong().toString() else d.toString()
}

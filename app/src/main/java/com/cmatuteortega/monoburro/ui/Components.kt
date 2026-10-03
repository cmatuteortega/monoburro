package com.cmatuteortega.monoburro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Macros
import com.cmatuteortega.monoburro.ui.theme.LocalDarkTheme
import com.cmatuteortega.monoburro.ui.theme.Macro
import com.cmatuteortega.monoburro.ui.theme.color
import kotlin.math.roundToInt

/** Side padding of every screen. */
val ScreenPadding = 20.dp

fun Macros.value(m: Macro): Double = when (m) {
    Macro.KCAL -> kcal
    Macro.PROTEIN -> protein
    Macro.CARBS -> carbs
    Macro.FAT -> fat
}

private fun Macro.format(v: Double) = if (this == Macro.KCAL) "${v.roundToInt()}" else "${v.roundToInt()} g"

/**
 * kcal / protein / carbs / fat as four columns: a big number over a coloured
 * dot and its label. The hero numbers of a burrito or a proposal.
 */
@Composable
fun MacroStats(macros: Macros, modifier: Modifier = Modifier, large: Boolean = false, without: Macro? = null) {
    val dark = LocalDarkTheme.current
    Row(modifier.fillMaxWidth()) {
        Macro.entries.filter { it != without }.forEach { m ->
            Column(Modifier.weight(1f)) {
                Text(
                    m.format(macros.value(m)),
                    style = if (large) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(m.color(dark)))
                    Text(
                        m.short,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
        }
    }
}

/** The same four numbers on one quiet line, for lists: "580 kcal · 33 g P · 60 g C · 23 g F". */
@Composable
fun MacroLine(macros: Macros, modifier: Modifier = Modifier) {
    val dark = LocalDarkTheme.current
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Macro.entries.forEach { m ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(m.color(dark)))
                Text(
                    if (m == Macro.KCAL) "${macros.kcal.roundToInt()} kcal" else "${macros.value(m).roundToInt()}g ${m.short.first().uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (m == Macro.KCAL) FontWeight.Bold else FontWeight.Medium,
                    color = if (m == Macro.KCAL) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                    maxLines = 1,
                )
            }
        }
    }
}

/** How the filling splits by category, as one rounded bar. */
@Composable
fun CompositionBar(shares: Map<Category, Int>, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val dark = LocalDarkTheme.current
    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Category.FILLINGS.forEach { c ->
            val v = shares[c] ?: 0
            if (v > 0) Box(Modifier.weight(v.toFloat()).fillMaxHeight().background(c.color(dark)))
        }
    }
}

/** An emoji on a soft rounded square: how ingredients, burritos and options are pictured. */
@Composable
fun EmojiTile(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    Box(
        modifier.size(size).clip(RoundedCornerShape(size * 0.32f)).background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size.value * 0.52f).sp)
    }
}

/** A small rounded label: "Free", "Recommended", "2 a day". */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(modifier, color = color, contentColor = contentColor, shape = RoundedCornerShape(50)) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1,
        )
    }
}

/** Full-width primary action pinned to the bottom, above the gesture bar, over a soft fade. */
@Composable
fun BottomAction(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    val bg = MaterialTheme.colorScheme.background
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to bg.copy(alpha = 0f), 0.35f to bg))
            .navigationBarsPadding()
            .padding(start = ScreenPadding, end = ScreenPadding, top = 16.dp, bottom = 12.dp),
    ) {
        PrimaryButton(text, onClick, Modifier.fillMaxWidth(), enabled)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) { Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1) }
}

/** The basic block of every screen: a quiet card on the warm background. */
@Composable
fun MenuCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = color, border = border, content = content)
    } else {
        Surface(modifier = modifier, shape = shape, color = color, border = border, content = content)
    }
}

/** A card of rows separated by hairlines, settings-style. */
@Composable
fun ListGroup(modifier: Modifier = Modifier, rows: List<@Composable () -> Unit>) {
    MenuCard(modifier.fillMaxWidth()) {
        Column {
            rows.forEachIndexed { i, row ->
                if (i > 0) HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                row()
            }
        }
    }
}

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(text, style = MaterialTheme.typography.titleLarge)
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        trailing?.invoke(this)
    }
}

/** − value + inside one pill. */
@Composable
fun Stepper(
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
    minusEnabled: Boolean = true,
    plusEnabled: Boolean = true,
    buttonSize: Dp = 40.dp,
    valueWidth: Dp = 52.dp,
    label: String = "",
) {
    val cs = MaterialTheme.colorScheme
    val small = buttonSize < 36.dp
    Row(
        modifier.clip(RoundedCornerShape(50)).background(cs.surfaceContainer).padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val colors = IconButtonDefaults.iconButtonColors(contentColor = cs.onSurface)
        IconButton(
            onClick = onMinus,
            enabled = minusEnabled,
            colors = colors,
            modifier = Modifier.size(buttonSize).semantics { contentDescription = "Less $label".trim() },
        ) { Icon(Icons.Rounded.Remove, contentDescription = null, modifier = Modifier.size(if (small) 16.dp else 20.dp)) }
        Text(
            value,
            style = if (small) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.width(valueWidth),
        )
        IconButton(
            onClick = onPlus,
            enabled = plusEnabled,
            colors = colors,
            modifier = Modifier.size(buttonSize).semantics { contentDescription = "More $label".trim() },
        ) { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(if (small) 16.dp else 20.dp)) }
    }
}

/** An empty state: a big emoji, a line, a hint and an optional action. */
@Composable
fun EmptyState(emoji: String, title: String, text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Column(modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        EmojiTile(emoji, size = 96.dp)
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 20.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        action?.let {
            Spacer(Modifier.height(20.dp))
            it()
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

/** A row of equal tiles to pick one from: tortilla sizes, goals, themes. */
@Composable
fun <T> ChoiceRow(
    options: List<T>,
    selected: T,
    onPick: (T) -> Unit,
    title: (T) -> String,
    modifier: Modifier = Modifier,
    subtitle: ((T) -> String)? = null,
    leading: ((T) -> String)? = null,
    height: Dp = if (subtitle != null || leading != null) 72.dp else 48.dp,
) {
    val cs = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val on = option == selected
            Surface(
                onClick = { onPick(option) },
                selected = on,
                modifier = Modifier.weight(1f).height(height),
                shape = RoundedCornerShape(16.dp),
                color = if (on) cs.primaryContainer else cs.surfaceContainer,
                contentColor = if (on) cs.onPrimaryContainer else cs.onSurface,
                border = if (on) BorderStroke(1.5.dp, cs.primary) else null,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp),
                ) {
                    leading?.let { Text(it(option), fontSize = 22.sp) }
                    Text(title(option), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    subtitle?.let {
                        Text(
                            it(option),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (on) cs.onPrimaryContainer else cs.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

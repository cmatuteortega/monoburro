package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.ui.displayEmoji

private val DOCTRINE = listOf(
    "🍳" to "Breakfast? Burrito.",
    "🥗" to "Lunch? Burrito.",
    "🌙" to "Dinner? Burrito. Obviously.",
    "🧊" to "Freezes, reheats, travels. Plates were a phase.",
)

/**
 * First thing on launch until a mode is picked: the pitch, a straight-faced
 * case for eating nothing but burritos, and the Mono / Burro choice.
 */
@Composable
fun LandingScreen(price: String, onMono: () -> Unit, onBurro: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    // A Surface, not just a background, so untinted text gets onBackground in dark mode too.
    Surface(Modifier.fillMaxSize(), color = cs.background) {
        Column(Modifier.statusBarsPadding().navigationBarsPadding()) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(28.dp))
                Text("🌯", fontSize = 72.sp)
                Text(
                    "MONOBURRO",
                    style = MaterialTheme.typography.displaySmall,
                    fontSize = 42.sp,
                    letterSpacing = (-1).sp,
                    color = cs.primary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "One food. One format. Zero decisions.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    "Batch-cook a month of burritos in one afternoon. Tell us what you like, " +
                        "we work out the fillings, the grams and the macros. You just roll.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )

                Surface(
                    Modifier.fillMaxWidth().padding(top = 24.dp),
                    color = cs.surfaceContainer,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("The Burrito Doctrine", style = MaterialTheme.typography.titleMedium, color = cs.primary)
                        Text(
                            "Protein, carbs, veg, cheese and sauce in one edible envelope. " +
                                "It's the only food you'll ever need. Humanity peaked; we just wrapped it.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        DOCTRINE.forEach { (emoji, line) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji, fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                                Text(line, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Text(
                            "No nutritionists were consulted. The burrito was.",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = cs.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "Pick your animal",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    textAlign = TextAlign.Center,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModeCard(
                        mode = Mode.MONO,
                        badge = "Premium · AI",
                        price = "$price / month",
                        container = cs.tertiaryContainer,
                        content = cs.onTertiaryContainer,
                        border = cs.tertiary,
                        onClick = onMono,
                        modifier = Modifier.weight(1f),
                    )
                    ModeCard(
                        mode = Mode.BURRO,
                        badge = "Free",
                        price = "Free forever",
                        container = cs.surfaceVariant,
                        content = cs.onSurfaceVariant,
                        border = cs.outline,
                        onClick = onBurro,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeCard(
    mode: Mode,
    badge: String,
    price: String,
    container: Color,
    content: Color,
    border: Color,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = container,
        contentColor = content,
        border = BorderStroke(2.dp, border),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(color = border, contentColor = container, shape = RoundedCornerShape(50)) {
                Text(
                    badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
            Text(mode.displayEmoji, fontSize = 48.sp, modifier = Modifier.padding(top = 8.dp))
            Text("Mode ${mode.label}", style = MaterialTheme.typography.titleLarge)
            Text(price, style = MaterialTheme.typography.labelLarge, fontSize = 14.sp)
            Text(
                mode.tagline,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.ui.EmojiTile
import com.cmatuteortega.monoburro.ui.Pill
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
        Box(
            Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(Brush.verticalGradient(listOf(cs.primaryContainer.copy(alpha = 0.9f), cs.background))),
        )
        Column(Modifier.statusBarsPadding().navigationBarsPadding()) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(32.dp))
                Surface(shape = CircleShape, color = cs.surface, shadowElevation = 8.dp, modifier = Modifier.size(112.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("🌯", fontSize = 60.sp) }
                }
                Text(
                    "monoburro",
                    style = MaterialTheme.typography.displayMedium,
                    color = cs.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 20.dp),
                )
                Text(
                    "One food. One format.\nZero decisions.",
                    style = MaterialTheme.typography.headlineSmall,
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
                    Modifier.fillMaxWidth().padding(top = 28.dp),
                    color = cs.surface,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, cs.outlineVariant),
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "THE BURRITO DOCTRINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = cs.primary,
                        )
                        Text(
                            "Protein, carbs, veg, cheese and sauce in one edible envelope. " +
                                "It's the only food you'll ever need. Humanity peaked; we just wrapped it.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        DOCTRINE.forEach { (emoji, line) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EmojiTile(emoji, size = 36.dp)
                                Text(
                                    line,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(start = 12.dp),
                                )
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
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    textAlign = TextAlign.Center,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
                    ModeCard(
                        mode = Mode.MONO,
                        badge = "AI · Premium",
                        price = "$price / month",
                        highlighted = true,
                        onClick = onMono,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    ModeCard(
                        mode = Mode.BURRO,
                        badge = "Free",
                        price = "Free forever",
                        highlighted = false,
                        onClick = onBurro,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
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
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val container = if (highlighted) cs.tertiary else cs.surface
    val content = if (highlighted) cs.onTertiary else cs.onSurface
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = container,
        contentColor = content,
        border = if (highlighted) null else BorderStroke(1.dp, cs.outlineVariant),
        shadowElevation = if (highlighted) 8.dp else 0.dp,
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(
            if (highlighted) Modifier.background(Brush.verticalGradient(listOf(container, lerp(container, cs.primary, 0.45f)))) else Modifier,
        ) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Pill(
                    badge,
                    color = if (highlighted) Color.White.copy(alpha = 0.22f) else cs.surfaceContainer,
                    contentColor = content,
                )
                Text(mode.displayEmoji, fontSize = 44.sp, modifier = Modifier.padding(top = 8.dp))
                Text(mode.label, style = MaterialTheme.typography.headlineSmall)
                Text(price, style = MaterialTheme.typography.labelLarge, fontSize = 14.sp)
                Text(
                    mode.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = content.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

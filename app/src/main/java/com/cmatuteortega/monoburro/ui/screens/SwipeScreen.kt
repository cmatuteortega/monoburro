package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.data.INGREDIENTS
import com.cmatuteortega.monoburro.logic.SWIPE_QUOTA
import com.cmatuteortega.monoburro.logic.cardAfter
import com.cmatuteortega.monoburro.logic.likedCount
import com.cmatuteortega.monoburro.logic.nextCard
import com.cmatuteortega.monoburro.logic.quotaMet
import com.cmatuteortega.monoburro.logic.unseenCount
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Ingredient
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.storage.AppState
import com.cmatuteortega.monoburro.storage.Swipe
import com.cmatuteortega.monoburro.ui.PrimaryButton
import com.cmatuteortega.monoburro.ui.ScreenPadding
import com.cmatuteortega.monoburro.ui.theme.LocalDarkTheme
import com.cmatuteortega.monoburro.ui.theme.color
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun SwipeScreen(
    state: AppState,
    onSwipe: (String, Swipe) -> Unit,
    onUndo: () -> Unit,
    onKeepSwiping: () -> Unit,
    onContinue: () -> Unit,
) {
    val prefs = state.prefs
    val card = nextCard(prefs, state.keepSwiping)
    val behind = cardAfter(prefs, state.keepSwiping)

    Column(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = ScreenPadding)) {
        QuotaChips(prefs)
        LinearProgressIndicator(
            progress = { state.swipeOrder.size / INGREDIENTS.size.toFloat() },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(4.dp),
            drawStopIndicator = {},
        )
        Text(
            "${state.swipeOrder.size} of ${INGREDIENTS.size} fillings seen",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        Box(Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            if (card == null) {
                DoneCard(prefs, state.keepSwiping, onKeepSwiping, onContinue)
            } else {
                key(card.id) {
                    SwipeableCard(card, behind, onSwiped = { onSwipe(card.id, it) }, onUndo = onUndo, canUndo = state.swipeOrder.isNotEmpty())
                }
            }
        }
        if (card != null) {
            Text(
                "→ like   ← never   ↑ favourite",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun QuotaChips(prefs: UserPrefs) {
    val dark = LocalDarkTheme.current
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Category.FILLINGS.forEach { c ->
            val need = SWIPE_QUOTA.getValue(c)
            val have = likedCount(prefs, c)
            val met = have >= need
            Surface(
                modifier = Modifier.weight(1f).semantics { contentDescription = "${c.label}: $have of $need liked" },
                shape = RoundedCornerShape(12.dp),
                color = if (met) c.color(dark).copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainer,
                border = if (met) BorderStroke(1.5.dp, c.color(dark)) else null,
            ) {
                Column(Modifier.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(c.emoji, fontSize = 18.sp)
                    Text(
                        if (met) "✓ ${minOf(have, 9)}" else "$have/$need",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeableCard(
    card: Ingredient,
    behind: Ingredient?,
    onSwiped: (Swipe) -> Unit,
    onUndo: () -> Unit,
    canUndo: Boolean,
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var committed by remember { mutableStateOf(false) }

    fun commit(swipe: Swipe) {
        if (committed) return
        committed = true
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            val w = size.width.toFloat().coerceAtLeast(1f)
            val h = size.height.toFloat().coerceAtLeast(1f)
            val target = when (swipe) {
                Swipe.LIKE -> Offset(w * 1.6f, offset.value.y)
                Swipe.NEVER -> Offset(-w * 1.6f, offset.value.y)
                Swipe.FAVORITE -> Offset(offset.value.x, -h * 1.6f)
            }
            offset.animateTo(target, tween(durationMillis = 220))
            onSwiped(swipe)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged { size = it }
                // Gestures on the untransformed box so positions and velocity stay coherent.
                .pointerInput(card.id) {
                    val tracker = VelocityTracker()
                    detectDragGestures(
                        onDragStart = { tracker.resetTracking() },
                        onDragEnd = {
                            val v = tracker.calculateVelocity()
                            val o = offset.value
                            val tx = size.width * 0.28f
                            val ty = size.height * 0.18f
                            val fling = 1800f
                            when {
                                (o.y < -ty || v.y < -fling) && abs(o.y) > abs(o.x) -> commit(Swipe.FAVORITE)
                                o.x > tx || (v.x > fling && o.x > 0) -> commit(Swipe.LIKE)
                                o.x < -tx || (v.x < -fling && o.x < 0) -> commit(Swipe.NEVER)
                                else -> scope.launch {
                                    offset.animateTo(Offset.Zero, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow))
                                }
                            }
                        },
                        onDragCancel = { scope.launch { offset.animateTo(Offset.Zero, spring()) } },
                    ) { change, drag ->
                        change.consume()
                        tracker.addPosition(change.uptimeMillis, change.position)
                        scope.launch { offset.snapTo(offset.value + drag) }
                    }
                },
        ) {
            behind?.let {
                key(it.id) {
                    IngredientCard(
                        it,
                        Modifier.fillMaxSize().graphicsLayer {
                            scaleX = 0.93f; scaleY = 0.93f; translationY = 22.dp.toPx(); alpha = 0.65f
                        },
                    )
                }
            }
            val o = offset.value
            val w = size.width.coerceAtLeast(1)
            val h = size.height.coerceAtLeast(1)
            IngredientCard(
                card,
                Modifier.fillMaxSize().graphicsLayer {
                    translationX = o.x
                    translationY = o.y
                    rotationZ = (o.x / w) * 14f
                },
                likeAlpha = (o.x / (w * 0.28f)).coerceIn(0f, 1f),
                neverAlpha = (-o.x / (w * 0.28f)).coerceIn(0f, 1f),
                favAlpha = if (abs(o.y) > abs(o.x)) (-o.y / (h * 0.18f)).coerceIn(0f, 1f) else 0f,
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val cs = MaterialTheme.colorScheme
            SmallRound(Icons.AutoMirrored.Rounded.Undo, "Undo", enabled = canUndo, onClick = onUndo)
            BigRound(Icons.Rounded.Close, "Never", cs.surface, cs.error) { commit(Swipe.NEVER) }
            BigRound(Icons.Rounded.Star, "Favourite", cs.surface, cs.tertiary, size = 58) { commit(Swipe.FAVORITE) }
            BigRound(Icons.Rounded.Favorite, "Like", cs.primary, cs.onPrimary) { commit(Swipe.LIKE) }
            Spacer(Modifier.size(44.dp))
        }
    }
}

@Composable
private fun BigRound(icon: ImageVector, label: String, bg: Color, fg: Color, size: Int = 70, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bg,
        contentColor = fg,
        shadowElevation = 6.dp,
        modifier = Modifier.size(size.dp).semantics { contentDescription = label },
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, modifier = Modifier.size((size * 0.44).dp)) }
    }
}

@Composable
private fun SmallRound(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(44.dp).semantics { contentDescription = label },
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) }
}

@Composable
fun IngredientCard(
    card: Ingredient,
    modifier: Modifier = Modifier,
    likeAlpha: Float = 0f,
    neverAlpha: Float = 0f,
    favAlpha: Float = 0f,
) {
    val cs = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val accent = card.category.color(dark)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = cs.surface,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, cs.outlineVariant),
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(accent.copy(alpha = if (dark) 0.28f else 0.20f), Color.Transparent)),
            ),
        ) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.18f), contentColor = cs.onSurface) {
                    Text(
                        "${card.category.emoji}  ${card.category.label.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(card.emoji, fontSize = 112.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    card.name,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "per 100 g",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant,
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Stat("${card.per100g.kcal.roundToInt()}", "kcal")
                    Stat(fmt(card.per100g.protein), "protein")
                    Stat(fmt(card.per100g.carbs), "carbs")
                    Stat(fmt(card.per100g.fat), "fat")
                }
                if (card.tags.isNotEmpty()) {
                    Text(
                        card.tags.joinToString("  ·  ") { it.label },
                        style = MaterialTheme.typography.labelMedium,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                }
            }
            Stamp("YUM", cs.secondary, likeAlpha, Modifier.align(Alignment.TopStart).padding(28.dp).rotate(-14f))
            Stamp("NEVER", cs.error, neverAlpha, Modifier.align(Alignment.TopEnd).padding(28.dp).rotate(14f))
            Stamp("★ FAVE", cs.tertiary, favAlpha, Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp))
        }
    }
}

private fun fmt(v: Double) = if (v >= 10 || v % 1.0 == 0.0) "${v.roundToInt()} g" else "${"%.1f".format(v)} g"

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Stamp(text: String, color: Color, alpha: Float, modifier: Modifier) {
    if (alpha <= 0f) return
    Surface(
        modifier = modifier.graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        border = BorderStroke(3.dp, color),
    ) {
        Text(
            text,
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun DoneCard(prefs: UserPrefs, keepSwiping: Boolean, onKeepSwiping: () -> Unit, onContinue: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val met = quotaMet(prefs)
    val unseen = unseenCount(prefs)
    Surface(
        Modifier.fillMaxSize(),
        shape = RoundedCornerShape(32.dp),
        color = cs.surface,
        border = BorderStroke(1.dp, cs.outlineVariant),
    ) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(if (met) "🎉" else "🌯", fontSize = 84.sp)
            Text(
                if (met) "Taste profile ready!" else "That's the whole deck",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                if (met) {
                    "You liked ${prefs.liked.size + prefs.favorites.size} fillings, ${prefs.favorites.size} of them favourites."
                } else {
                    "We'll build burritos from what you liked. Categories with no likes are left out."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            val likedEmojis = INGREDIENTS.filter { prefs.likes(it.id) }.joinToString(" ") { it.emoji }
            if (likedEmojis.isNotEmpty()) {
                Text(likedEmojis, fontSize = 26.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))
            }
            Spacer(Modifier.height(28.dp))
            PrimaryButton("Set my ratios", onContinue, Modifier.fillMaxWidth())
            if (!keepSwiping && unseen > 0) {
                TextButton(onClick = onKeepSwiping, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Keep swiping ($unseen more)")
                }
            }
        }
    }
}

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.billing.BillingState
import com.cmatuteortega.monoburro.billing.MonoBilling
import com.cmatuteortega.monoburro.model.Mode

/** What Mono adds. AI first: that's what the subscription pays for. */
private val MONO_PERKS = listOf(
    "🧠" to ("AI burrito chef" to "Proposals written by AI from your swipes, your macros and what's left in the fridge."),
    "🎯" to ("Macro coach" to "Hit your kcal or protein target in every burrito; AI tunes the grams for you."),
    "🛒" to ("Smart shopping list" to "Batches merged into one list with real pack sizes, so nothing goes to waste."),
    "💪" to ("Bulk & Cut modes" to "Daily macros tuned for a surplus or a deficit. Burros just eat."),
    "🔁" to ("Endless remixes" to "New fillings every batch. Burrito fatigue isn't real, but just in case."),
)

/** Burro users: what Mono is, what it costs, and the button to get it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallSheet(
    billing: BillingState,
    debugBuild: Boolean,
    onSubscribe: () -> Unit,
    onSimulate: () -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String,
) {
    val cs = MaterialTheme.colorScheme
    val price = billing.price ?: MonoBilling.FALLBACK_PRICE
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(shape = CircleShape, color = cs.tertiaryContainer, modifier = Modifier.size(96.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(Mode.MONO.displayEmoji, fontSize = 52.sp) }
            }
            Text("Go Mono", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 12.dp))
            Text(
                "The premium burrito life, with AI in the kitchen.",
                style = MaterialTheme.typography.bodyLarge,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Column(
                Modifier.fillMaxWidth().padding(vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                MONO_PERKS.forEach { (emoji, perk) -> Perk(emoji, perk.first, perk.second) }
            }
            Surface(color = cs.surfaceContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${Mode.BURRO.displayEmoji} Burro stays free: swipe your tastes, set ratios, get proposals and a batch plan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp),
                )
            }

            billing.message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Button(
                onClick = onSubscribe,
                enabled = !billing.busy,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cs.tertiary, contentColor = cs.onTertiary),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                if (billing.busy) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = cs.onTertiary, strokeWidth = 2.dp)
                } else {
                    Text("Subscribe · $price / month", style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(
                "Billed monthly by Google Play. Renews automatically; cancel anytime in Play Store › Subscriptions.",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (debugBuild) {
                OutlinedButton(onClick = onSimulate, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text("Simulate purchase (debug build)")
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onRestore) { Text("Restore purchase") }
                TextButton(onClick = onDismiss) { Text(dismissLabel) }
            }
        }
    }
}

/** Mono users: what they have, and where to manage the subscription. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonoSheet(
    simulated: Boolean,
    onManage: () -> Unit,
    onEndSimulated: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(shape = CircleShape, color = cs.tertiaryContainer, modifier = Modifier.size(96.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(Mode.MONO.displayEmoji, fontSize = 52.sp) }
            }
            Text("You're Mono", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 12.dp))
            Text(
                "Everything's unlocked. The burrito thanks you.",
                style = MaterialTheme.typography.bodyLarge,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Column(
                Modifier.fillMaxWidth().padding(vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                MONO_PERKS.forEach { (emoji, perk) -> Perk(emoji, perk.first, perk.second) }
            }
            if (simulated) {
                OutlinedButton(onClick = onEndSimulated, modifier = Modifier.fillMaxWidth()) {
                    Text("End simulated subscription (debug build)")
                }
            } else {
                OutlinedButton(onClick = onManage, modifier = Modifier.fillMaxWidth()) {
                    Text("Manage subscription in Google Play")
                }
            }
        }
    }
}

@Composable
private fun Perk(emoji: String, title: String, body: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        EmojiTile(emoji, size = 44.dp, color = MaterialTheme.colorScheme.tertiaryContainer)
        Column(Modifier.padding(start = 14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

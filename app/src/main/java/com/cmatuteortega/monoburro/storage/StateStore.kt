package com.cmatuteortega.monoburro.storage

import android.content.Context
import kotlinx.serialization.json.Json

/** Local-only persistence: the whole [AppState] as JSON in SharedPreferences. */
class StateStore(context: Context) {
    private val prefs = context.getSharedPreferences("monoburro", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun load(): AppState = prefs.getString(KEY, null)
        ?.let { runCatching { json.decodeFromString(AppState.serializer(), it) }.getOrNull() }
        ?: AppState()

    fun save(state: AppState) {
        prefs.edit().putString(KEY, json.encodeToString(AppState.serializer(), state)).apply()
    }

    private companion object {
        const val KEY = "state_v1"
    }
}

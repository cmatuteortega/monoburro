package com.cmatuteortega.monoburro.ui

import android.graphics.Paint
import com.cmatuteortega.monoburro.model.Mode

/** The mode's emoji, or its fallback when the system font can't draw it. */
val Mode.displayEmoji: String
    get() = if (Paint().hasGlyph(emoji)) emoji else fallbackEmoji

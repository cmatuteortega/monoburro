package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.model.Feature
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.model.can
import com.cmatuteortega.monoburro.model.effectiveMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeTest {
    @Test
    fun monoNeedsASubscription() {
        assertEquals(Mode.MONO, effectiveMode(Mode.MONO, entitled = true))
        assertEquals(Mode.BURRO, effectiveMode(Mode.MONO, entitled = false))
    }

    @Test
    fun burroIsFreeEitherWay() {
        assertEquals(Mode.BURRO, effectiveMode(Mode.BURRO, entitled = false))
        assertEquals(Mode.BURRO, effectiveMode(Mode.BURRO, entitled = true))
    }

    @Test
    fun noChoiceStaysOnTheLanding() {
        assertNull(effectiveMode(null, entitled = true))
        assertNull(effectiveMode(null, entitled = false))
    }

    @Test
    fun everyFeatureIsBuiltForBothModesForNow() {
        Feature.entries.forEach {
            assertTrue(Mode.MONO.can(it))
            assertTrue("$it should be available in Burro", Mode.BURRO.can(it))
        }
    }
}

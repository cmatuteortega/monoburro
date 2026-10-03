package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.model.Feature
import com.cmatuteortega.monoburro.model.Goal
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.model.Profile
import com.cmatuteortega.monoburro.model.can
import com.cmatuteortega.monoburro.model.effectiveGoal
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
    fun monoCanDoEverything() {
        Feature.entries.forEach { assertTrue(Mode.MONO.can(it)) }
        Goal.entries.forEach { assertTrue(Mode.MONO.can(it)) }
    }

    @Test
    fun onlyBulkAndCutAreMonoOnly() {
        assertEquals(listOf(Feature.BULK_CUT), Feature.entries.filterNot { Mode.BURRO.can(it) })
        assertEquals(listOf(Goal.EAT), Goal.entries.filter { Mode.BURRO.can(it) })
    }

    @Test
    fun burroAlwaysEats() {
        assertEquals(Goal.EAT, Profile().goal)
        assertEquals(Goal.EAT, effectiveGoal(Goal.BULK, Mode.BURRO))
        assertEquals(Goal.EAT, effectiveGoal(Goal.CUT, Mode.BURRO))
        assertEquals(Goal.CUT, effectiveGoal(Goal.CUT, Mode.MONO))
    }
}

package com.cmatuteortega.monoburro.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProposalKind { CLASSIC, HIGH_PROTEIN, VEGGIE_FORWARD }

@Serializable
data class ProposalItem(val ingredientId: String, val gramsPerBurrito: Int)

@Serializable
data class Proposal(
    val id: String,
    val kind: ProposalKind,
    val name: String,
    val tagline: String,
    val items: List<ProposalItem>,
    val macrosPerBurrito: Macros,
)

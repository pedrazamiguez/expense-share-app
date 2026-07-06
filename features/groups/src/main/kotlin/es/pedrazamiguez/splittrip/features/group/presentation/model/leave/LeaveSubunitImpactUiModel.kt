package es.pedrazamiguez.splittrip.features.group.presentation.model.leave

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LeaveSubunitImpactUiModel(
    val hasSubunitImpact: Boolean = false,
    val affectedSubunitNames: ImmutableList<String> = persistentListOf(),
    val message: String = ""
)

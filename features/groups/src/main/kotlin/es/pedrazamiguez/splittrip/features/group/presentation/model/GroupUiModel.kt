package es.pedrazamiguez.splittrip.features.group.presentation.model

import es.pedrazamiguez.splittrip.domain.enums.SyncStatus

data class GroupUiModel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "",
    val membersCountText: String = "",
    val dateText: String = "",
    /** Cloud synchronization status of this group. */
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

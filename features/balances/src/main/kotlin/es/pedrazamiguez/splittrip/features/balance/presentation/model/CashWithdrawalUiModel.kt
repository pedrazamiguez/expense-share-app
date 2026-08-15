package es.pedrazamiguez.splittrip.features.balance.presentation.model

import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.MemberDisplay
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus

/**
 * UI model representing a cash withdrawal in the activity list.
 * Pre-formatted by the mapper for direct display.
 *
 * [memberDisplay] holds the member's identification and status.
 * [scopeLabel] is a pre-formatted label indicating scope: "Personal", subunit name, or "Group".
 * [title] is the optional user-provided label (e.g., "Airport ATM").
 * [notes] is the optional free-text description.
 * [createdByDisplayName] is the resolved actor name when the withdrawal was logged on behalf
 * of another member (impersonation). `null` when actor == target, `createdBy` is blank
 * (legacy data), or the actor's profile could not be resolved.
 */
data class CashWithdrawalUiModel(
    val id: String = "",
    val memberDisplay: MemberDisplay = MemberDisplay.Active("", ""),
    val isCurrentUser: Boolean = false,
    val formattedAmount: String = "",
    val formattedDeducted: String = "",
    val currency: String = "",
    val isForeignCurrency: Boolean = false,
    val dateText: String = "",
    val scopeLabel: String? = null,
    val isSubunitWithdrawal: Boolean = false,
    val isPersonalWithdrawal: Boolean = false,
    val isGroupWithdrawal: Boolean = false,
    val title: String? = null,
    val notes: String? = null,
    val createdByDisplayName: String? = null,
    /** Cloud synchronization status of this cash withdrawal. */
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val displayName: String = memberDisplay.displayName,
    val actionsTitle: String = ""
)

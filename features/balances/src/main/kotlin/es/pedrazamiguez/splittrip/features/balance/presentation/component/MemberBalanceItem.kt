package es.pedrazamiguez.splittrip.features.balance.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.MemberDisplay
import es.pedrazamiguez.splittrip.features.balance.R
import es.pedrazamiguez.splittrip.features.balance.presentation.model.MemberBalanceUiModel

@Composable
fun MemberBalanceItem(memberBalance: MemberBalanceUiModel, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    var showCashBreakdown by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val balanceColor = if (memberBalance.isPositiveBalance) colorScheme.primary else colorScheme.error

    val displayName = if (memberBalance.isCurrentUser) {
        stringResource(R.string.balances_member_you)
    } else {
        memberBalance.displayName
    }

    val expandedStateDesc = if (isExpanded) {
        stringResource(R.string.balances_member_expanded)
    } else {
        stringResource(R.string.balances_member_collapsed)
    }

    val toggleContentDesc = if (isExpanded) {
        stringResource(R.string.balances_member_collapse, displayName)
    } else {
        stringResource(R.string.balances_member_expand, displayName)
    }

    val isFormer = memberBalance.memberDisplay is MemberDisplay.Former
    val cardModifier = if (isFormer) modifier.alpha(0.6f) else modifier

    MemberBalanceCard(
        modifier = cardModifier,
        expandedStateDesc = expandedStateDesc,
        toggleContentDesc = toggleContentDesc,
        onToggle = { isExpanded = !isExpanded }
    ) {
        MemberBalanceSummaryRow(
            memberBalance = memberBalance,
            displayName = displayName,
            balanceColor = balanceColor,
            isExpanded = isExpanded,
            toggleContentDesc = toggleContentDesc
        )
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            MemberBalanceExpandedDetail(
                memberBalance = memberBalance,
                balanceColor = balanceColor,
                onShowCashBreakdown = { showCashBreakdown = true }
            )
        }
    }

    if (showCashBreakdown) {
        CashBreakdownBottomSheet(
            memberName = displayName,
            breakdown = memberBalance.cashBreakdown,
            formattedTotal = memberBalance.formattedCashInHand,
            formattedTotalFees = memberBalance.formattedTotalFees,
            onDismiss = { showCashBreakdown = false }
        )
    }
}

package es.pedrazamiguez.splittrip.features.balance.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewLocales
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemes
import es.pedrazamiguez.splittrip.features.balance.presentation.component.ContributionHistoryItem
import es.pedrazamiguez.splittrip.features.balance.presentation.component.GroupPocketBalanceCard

@PreviewThemes
@Composable
private fun GroupPocketBalanceCardPreview() {
    BalanceCardPreviewHelper {
        GroupPocketBalanceCard(balance = it)
    }
}

@PreviewThemes
@Composable
private fun GroupPocketBalanceCardEmptyPreview() {
    BalanceCardPreviewHelper(domainBalance = PREVIEW_POCKET_BALANCE_EMPTY) {
        GroupPocketBalanceCard(balance = it)
    }
}

@PreviewLocales
@Composable
private fun ContributionHistoryItemPreview() {
    ContributionItemPreviewHelper {
        ContributionHistoryItem(contribution = it)
    }
}

@PreviewLocales
@Composable
private fun LinkedContributionHistoryItemPreview() {
    ContributionItemPreviewHelper(domainContribution = PREVIEW_CONTRIBUTION_LINKED) {
        ContributionHistoryItem(contribution = it)
    }
}

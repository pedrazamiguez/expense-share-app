package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewLocales
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.ContributionHistoryItem
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.GroupPocketBalanceCard

@PreviewLocales
@Composable
private fun GroupPocketBalanceCardPreview() {
    BalanceCardPreviewHelper {
        GroupPocketBalanceCard(balance = it)
    }
}

@PreviewLocales
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

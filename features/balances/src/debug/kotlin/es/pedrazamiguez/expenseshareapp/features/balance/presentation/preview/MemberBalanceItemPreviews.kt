package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewLocales
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.MemberBalanceItem

@PreviewLocales
@Composable
private fun MemberBalanceItemPositivePreview() {
    MemberBalanceItemPreviewHelper(domainBalance = PREVIEW_MEMBER_BALANCE_POSITIVE) {
        MemberBalanceItem(
            memberBalance = it,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewLocales
@Composable
private fun MemberBalanceItemNegativePreview() {
    MemberBalanceItemPreviewHelper(domainBalance = PREVIEW_MEMBER_BALANCE_NEGATIVE) {
        MemberBalanceItem(
            memberBalance = it,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewLocales
@Composable
private fun MemberBalanceItemNegativeCashPreview() {
    MemberBalanceItemPreviewHelper(domainBalance = PREVIEW_MEMBER_BALANCE_NEGATIVE_CASH) {
        MemberBalanceItem(
            memberBalance = it,
            modifier = Modifier.padding(16.dp)
        )
    }
}

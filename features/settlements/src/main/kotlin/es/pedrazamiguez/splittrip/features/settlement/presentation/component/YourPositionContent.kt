package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.SettlementConsensusItemUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event.YourPositionUiEvent
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun YourPositionContent(
    personalPosition: PersonalPositionUiModel,
    isCashBreakdownVisible: Boolean,
    settlementConsensus: ImmutableList<SettlementConsensusItemUiModel>,
    onEvent: (YourPositionUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomPadding = LocalBottomPadding.current
    val scrollBehavior = rememberConnectedScrollBehavior()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(
            start = MaterialTheme.spacing.Default,
            end = MaterialTheme.spacing.Default,
            top = MaterialTheme.spacing.Default,
            bottom = bottomPadding + MaterialTheme.spacing.Default
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
    ) {
        item(key = "hero_net_position") {
            YourPositionHeroBanner(personalPosition = personalPosition)
        }

        item(key = "pocket_cash_row") {
            YourPositionPocketCashRow(
                personalPosition = personalPosition,
                onShowCashBreakdown = { onEvent(YourPositionUiEvent.ShowCashBreakdown) }
            )
        }

        item(key = "activity_breakdown") {
            YourPositionActivityBreakdown(personalPosition = personalPosition)
        }

        if (personalPosition.hasNegativeCashInHand) {
            item(key = "negative_cash_hint") {
                CaptionText(
                    text = stringResource(R.string.your_position_negative_cash_hint),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item(key = "settlement_consensus") {
            SettlementConsensusSection(
                settlements = settlementConsensus,
                onConfirm = { id -> onEvent(YourPositionUiEvent.ConfirmSettlement(id)) },
                onDispute = { id -> onEvent(YourPositionUiEvent.DisputeSettlement(id)) },
                onNudge = { id -> onEvent(YourPositionUiEvent.NudgeDebtor(id)) }
            )
        }
    }

    if (isCashBreakdownVisible) {
        CashBreakdownBottomSheet(
            breakdown = personalPosition.cashBreakdown,
            formattedTotal = personalPosition.formattedCashInHand,
            formattedTotalFees = personalPosition.formattedTotalFees,
            onDismiss = { onEvent(YourPositionUiEvent.DismissCashBreakdown) }
        )
    }
}

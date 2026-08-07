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
import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.InlineWarningBanner
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.sheet.CashBreakdownBottomSheet
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.MemberSpendingChartUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.SettlementConsensusItemUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event.YourPositionUiEvent
import kotlinx.collections.immutable.ImmutableList

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun YourPositionContent(
    personalPosition: PersonalPositionUiModel,
    isCashBreakdownVisible: Boolean,
    settlementConsensus: ImmutableList<SettlementConsensusItemUiModel>,
    chart: MemberSpendingChartUiModel?,
    isCashOnly: Boolean,
    onEvent: (YourPositionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    isOffline: Boolean = false
) {
    val bottomPadding = LocalBottomPadding.current
    val scrollBehavior = rememberConnectedScrollBehavior()
    val spacing = MaterialTheme.spacing

    LazyColumn(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(
            start = spacing.Default,
            end = spacing.Default,
            top = spacing.Default,
            bottom = bottomPadding + spacing.Default
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.Medium)
    ) {
        if (isOffline) {
            item(key = "offline_warning_banner") {
                InlineWarningBanner(warning = UiText.StringResource(R.string.your_position_offline_warning))
            }
        }
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
        item(key = "spending_chart") {
            chart?.let {
                MemberSpendingBarChart(
                    chart = it,
                    isCashOnly = isCashOnly,
                    onToggle = { cashOnly -> onEvent(YourPositionUiEvent.ChartModeToggled(cashOnly)) }
                )
            }
        }
        item(key = "settlement_consensus") {
            SettlementConsensusSection(
                settlements = settlementConsensus,
                isOffline = isOffline,
                onConfirm = { onEvent(YourPositionUiEvent.ConfirmSettlement(it)) },
                onDispute = { onEvent(YourPositionUiEvent.DisputeSettlement(it)) },
                onNudge = { onEvent(YourPositionUiEvent.NudgeDebtor(it)) }
            )
        }
    }

    if (isCashBreakdownVisible) {
        val breakdownItems = personalPosition.cashBreakdown
        CashBreakdownBottomSheet(
            memberName = stringResource(R.string.your_position_title),
            breakdown = breakdownItems,
            formattedTotal = personalPosition.formattedCashInHand,
            formattedTotalFees = personalPosition.formattedTotalFees ?: "",
            onDismiss = { onEvent(YourPositionUiEvent.DismissCashBreakdown) }
        )
    }
}

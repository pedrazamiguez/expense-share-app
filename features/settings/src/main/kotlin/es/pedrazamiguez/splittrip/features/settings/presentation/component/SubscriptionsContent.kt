package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.event.SubscriptionsUiEvent
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.state.SubscriptionsUiState

@Composable
fun SubscriptionsContent(
    uiState: SubscriptionsUiState,
    onEvent: (SubscriptionsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomPadding = LocalBottomPadding.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = MaterialTheme.spacing.Large)
            .padding(
                top = MaterialTheme.spacing.Medium,
                bottom = MaterialTheme.spacing.ExtraLarge + bottomPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.subscriptions_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.ExtraSmall))

        Text(
            text = stringResource(R.string.subscriptions_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.Medium)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.Large))

        BillingIntervalSelector(
            selectedInterval = uiState.selectedInterval,
            onIntervalSelected = { onEvent(SubscriptionsUiEvent.SelectBillingInterval(it)) }
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.Large))

        uiState.plans.forEach { plan ->
            PlanComparisonCard(
                plan = plan,
                onCtaClick = { onEvent(SubscriptionsUiEvent.UpgradePlan(it)) },
                isProcessingAction = uiState.isProcessingAction
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.Large))
        }

        SubscriptionsDisclaimerFooter(
            onRestorePurchasesClick = { onEvent(SubscriptionsUiEvent.RestorePurchases) }
        )
    }
}

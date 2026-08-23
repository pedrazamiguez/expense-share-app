package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.GradientButton
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.state.AccountSecurityUiState

@Composable
fun PasswordResetSection(
    uiState: AccountSecurityUiState,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.account_security_password_reset_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.ExtraSmall))

        Text(
            text = stringResource(R.string.account_security_password_reset_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.Medium))

        GradientButton(
            text = stringResource(R.string.account_security_password_reset_button),
            onClick = onResetClick,
            enabled = uiState.canResetPassword,
            isLoading = uiState.isPasswordResetSending,
            modifier = Modifier.fillMaxWidth()
        )

        if (!uiState.canResetPassword) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.Small))
            val disabledReason = if (uiState.isAnonymous) {
                stringResource(R.string.account_security_password_reset_disabled_guest)
            } else {
                stringResource(R.string.account_security_password_reset_disabled_google)
            }
            Text(
                text = disabledReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

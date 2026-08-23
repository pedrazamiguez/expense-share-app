package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard

@Composable
fun SecurityPreferencesCard(
    biometricLockEnabled: Boolean,
    onBiometricLockToggle: (Boolean) -> Unit,
    onManageProvidersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlatCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
        ) {
            BiometricLockRow(
                biometricLockEnabled = biometricLockEnabled,
                onBiometricLockToggle = onBiometricLockToggle
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.ExtraSmall))

            LinkedProvidersRow(
                onManageProvidersClick = onManageProvidersClick
            )
        }
    }
}

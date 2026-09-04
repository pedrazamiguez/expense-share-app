package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ShieldLock
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.GradientButton

/** Test tag for the app lock gate overlay, used in UI tests. */
const val APP_LOCK_GATE_TEST_TAG = "app_lock_gate"

private val LOCK_ICON_SIZE = 72.dp

/**
 * A security gate overlay displayed when the app is locked with biometrics.
 *
 * Prevents content leakage by presenting a solid barrier with a clear unlock CTA.
 *
 * @param onUnlockClick Triggered when the user taps the unlock button to launch the biometric prompt.
 * @param modifier Optional [Modifier] applied to the root container.
 */
@Composable
fun AppLockGate(
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag(APP_LOCK_GATE_TEST_TAG),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.Section),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = TablerIcons.Outline.ShieldLock,
                contentDescription = null,
                modifier = Modifier.size(LOCK_ICON_SIZE),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.Large))

            Text(
                text = stringResource(R.string.app_lock_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.Small))

            Text(
                text = stringResource(R.string.app_lock_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.Section))

            GradientButton(
                text = stringResource(R.string.app_lock_button_unlock),
                onClick = onUnlockClick,
                leadingIcon = TablerIcons.Outline.ShieldLock,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

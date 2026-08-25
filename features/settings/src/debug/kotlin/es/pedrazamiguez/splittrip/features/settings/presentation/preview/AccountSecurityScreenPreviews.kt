package es.pedrazamiguez.splittrip.features.settings.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.domain.enums.AuthProviderType
import es.pedrazamiguez.splittrip.domain.enums.BiometricCapability
import es.pedrazamiguez.splittrip.features.settings.presentation.mapper.impl.AccountSecurityUiMapperImpl
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.AccountSecurityScreen
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.state.AccountSecurityUiState
import kotlinx.collections.immutable.persistentListOf

@PreviewComplete
@Composable
private fun AccountSecurityScreenAvailablePreview() {
    val mapper = AccountSecurityUiMapperImpl()
    PreviewThemeWrapper {
        AccountSecurityScreen(
            uiState = AccountSecurityUiState(
                isLoading = false,
                email = "user@example.com",
                isAnonymous = false,
                linkedProviders = persistentListOf(AuthProviderType.EMAIL_PASSWORD),
                biometricLockEnabled = true,
                biometricCapability = BiometricCapability.AVAILABLE
            ),
            uiMapper = mapper,
            onEvent = {}
        )
    }
}

@PreviewComplete
@Composable
private fun AccountSecurityScreenNoHardwarePreview() {
    val mapper = AccountSecurityUiMapperImpl()
    PreviewThemeWrapper {
        AccountSecurityScreen(
            uiState = AccountSecurityUiState(
                isLoading = false,
                email = "user@example.com",
                isAnonymous = false,
                linkedProviders = persistentListOf(AuthProviderType.GOOGLE),
                biometricLockEnabled = false,
                biometricCapability = BiometricCapability.NO_HARDWARE
            ),
            uiMapper = mapper,
            onEvent = {}
        )
    }
}

@PreviewComplete
@Composable
private fun AccountSecurityScreenNotEnrolledPreview() {
    val mapper = AccountSecurityUiMapperImpl()
    PreviewThemeWrapper {
        AccountSecurityScreen(
            uiState = AccountSecurityUiState(
                isLoading = false,
                email = "guest@example.com",
                isAnonymous = true,
                linkedProviders = persistentListOf(),
                biometricLockEnabled = false,
                biometricCapability = BiometricCapability.NOT_ENROLLED
            ),
            uiMapper = mapper,
            onEvent = {}
        )
    }
}

package es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.domain.enums.AuthProviderType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class AccountSecurityUiState(
    val isLoading: Boolean = true,
    val email: String = "",
    val isAnonymous: Boolean = false,
    val linkedProviders: ImmutableList<AuthProviderType> = persistentListOf(),
    val isPasswordResetSending: Boolean = false,
    val showPasswordResetConfirmDialog: Boolean = false,
    val biometricLockEnabled: Boolean = false
) {
    val isEmailProviderLinked: Boolean
        get() = linkedProviders.contains(AuthProviderType.EMAIL_PASSWORD)

    val isGoogleOnly: Boolean
        get() = linkedProviders.size == 1 && linkedProviders.contains(AuthProviderType.GOOGLE)

    val canResetPassword: Boolean
        get() = !isAnonymous && isEmailProviderLinked
}

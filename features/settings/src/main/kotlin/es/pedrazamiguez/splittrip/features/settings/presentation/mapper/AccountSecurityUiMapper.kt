package es.pedrazamiguez.splittrip.features.settings.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.domain.enums.AuthProviderType

interface AccountSecurityUiMapper {
    fun formatProviderLabel(providers: List<AuthProviderType>, isAnonymous: Boolean): UiText
    fun formatPasswordResetSuccessMessage(email: String): UiText
}

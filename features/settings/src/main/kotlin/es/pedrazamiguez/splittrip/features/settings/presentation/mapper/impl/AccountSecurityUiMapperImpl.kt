package es.pedrazamiguez.splittrip.features.settings.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.domain.enums.AuthProviderType
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.mapper.AccountSecurityUiMapper

class AccountSecurityUiMapperImpl : AccountSecurityUiMapper {

    override fun formatProviderLabel(providers: List<AuthProviderType>, isAnonymous: Boolean): UiText {
        if (isAnonymous || providers.isEmpty()) {
            return UiText.StringResource(R.string.account_security_provider_guest)
        }
        if (providers.size > 1) {
            return UiText.StringResource(R.string.account_security_provider_multiple)
        }
        return when (providers.first()) {
            AuthProviderType.GOOGLE -> UiText.StringResource(R.string.account_security_provider_google)
            AuthProviderType.EMAIL_PASSWORD -> UiText.StringResource(R.string.account_security_provider_email)
        }
    }

    override fun formatPasswordResetSuccessMessage(email: String): UiText {
        return UiText.StringResource(R.string.account_security_password_reset_sent, email)
    }
}

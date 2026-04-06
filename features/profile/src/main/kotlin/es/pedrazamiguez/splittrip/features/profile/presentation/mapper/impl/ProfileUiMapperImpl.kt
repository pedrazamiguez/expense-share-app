package es.pedrazamiguez.splittrip.features.profile.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatMediumDate
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.profile.presentation.mapper.ProfileUiMapper
import es.pedrazamiguez.splittrip.features.profile.presentation.model.ProfileUiModel

class ProfileUiMapperImpl(private val localeProvider: LocaleProvider) : ProfileUiMapper {

    override fun toProfileUiModel(user: User): ProfileUiModel = with(user) {
        val currentLocale = localeProvider.getCurrentLocale()
        ProfileUiModel(
            displayName = displayName ?: email,
            email = email,
            profileImageUrl = profileImagePath,
            memberSinceText = createdAt?.formatMediumDate(currentLocale) ?: ""
        )
    }
}

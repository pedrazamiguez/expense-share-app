package es.pedrazamiguez.expenseshareapp.features.profile.presentation.mapper

import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.model.ProfileUiModel

interface ProfileUiMapper {
    fun toProfileUiModel(user: User): ProfileUiModel
}

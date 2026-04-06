package es.pedrazamiguez.splittrip.features.group.presentation.mapper

import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.splittrip.domain.model.Currency
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.features.group.presentation.model.GroupUiModel
import kotlinx.collections.immutable.ImmutableList

interface GroupUiMapper {
    fun toGroupUiModel(group: Group): GroupUiModel
    fun toGroupUiModelList(groups: List<Group>): ImmutableList<GroupUiModel>
    fun toCurrencyUiModel(currency: Currency): CurrencyUiModel
    fun toCurrencyUiModels(currencies: List<Currency>): ImmutableList<CurrencyUiModel>
}

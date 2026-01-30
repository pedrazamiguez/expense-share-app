package es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.impl

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.GroupUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class GroupUiMapperImpl(
    private val localeProvider: LocaleProvider, private val resourceProvider: ResourceProvider
) : GroupUiMapper {

    override fun toGroupUiModel(group: Group): GroupUiModel = with(group) {
        val currentLocale = localeProvider.getCurrentLocale()
        GroupUiModel(
            id = id,
            name = name,
            description = description,
            currency = currency,
            membersCountText = resourceProvider.getString(
                R.string.group_members_count, members.size
            ),
            dateText = createdAt?.formatShortDate(currentLocale) ?: ""
        )
    }

    override fun toGroupUiModelList(groups: List<Group>): ImmutableList<GroupUiModel> =
        groups.map { toGroupUiModel(it) }.toImmutableList()

}

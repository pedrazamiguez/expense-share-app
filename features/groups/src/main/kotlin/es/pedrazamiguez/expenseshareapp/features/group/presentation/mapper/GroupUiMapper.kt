package es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel

class GroupUiMapper(
    private val localeProvider: LocaleProvider, private val resourceProvider: ResourceProvider
) {

    fun map(group: Group): GroupUiModel {
        val appLocale = localeProvider.getCurrentLocale()
        return with(group) {
            GroupUiModel(
                id = id,
                name = name,
                description = description,
                currency = currency,
                membersCountText = resourceProvider.getString(
                    R.string.group_members_count, members.size
                ),
                dateText = createdAt?.formatShortDate(appLocale) ?: ""
            )
        }
    }
}

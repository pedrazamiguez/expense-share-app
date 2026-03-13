package es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.impl

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.SubunitUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.MemberUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.SubunitUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.text.NumberFormat

class SubunitUiMapperImpl(
    private val localeProvider: LocaleProvider,
    private val resourceProvider: ResourceProvider
) : SubunitUiMapper {

    override fun toSubunitUiModel(
        subunit: Subunit,
        memberProfiles: Map<String, User>
    ): SubunitUiModel {
        val memberNames = subunit.memberIds.map { userId ->
            memberProfiles[userId]?.displayName
                ?: memberProfiles[userId]?.email
                ?: userId
        }.toImmutableList()

        val memberCount = subunit.memberIds.size
        val memberCountText = resourceProvider.getQuantityString(
            R.plurals.subunit_member_count, memberCount, memberCount
        )

        val sharesSummary = formatSharesSummary(subunit)

        return SubunitUiModel(
            id = subunit.id,
            name = subunit.name,
            memberNames = memberNames,
            memberCount = memberCountText,
            sharesSummary = sharesSummary
        )
    }

    override fun toSubunitUiModelList(
        subunits: List<Subunit>,
        memberProfiles: Map<String, User>
    ): ImmutableList<SubunitUiModel> =
        subunits.map { toSubunitUiModel(it, memberProfiles) }.toImmutableList()

    override fun toMemberUiModelList(
        memberIds: List<String>,
        memberProfiles: Map<String, User>,
        subunits: List<Subunit>,
        excludeSubunitId: String?
    ): ImmutableList<MemberUiModel> {
        // Build a lookup: userId → subunit name (excluding the subunit being edited)
        val assignmentMap = buildMap<String, String> {
            subunits
                .filter { it.id != excludeSubunitId }
                .forEach { subunit ->
                    subunit.memberIds.forEach { memberId ->
                        put(memberId, subunit.name)
                    }
                }
        }

        return memberIds.map { userId ->
            val profile = memberProfiles[userId]
            val displayName = profile?.displayName ?: profile?.email ?: userId
            val assignedSubunitName = assignmentMap[userId]

            MemberUiModel(
                userId = userId,
                displayName = displayName,
                isAssigned = assignedSubunitName != null,
                assignedSubunitName = assignedSubunitName ?: ""
            )
        }.toImmutableList()
    }

    private fun formatSharesSummary(subunit: Subunit): String {
        if (subunit.memberShares.isEmpty()) return ""

        val locale = localeProvider.getCurrentLocale()
        val percentFormat = NumberFormat.getPercentInstance(locale).apply {
            maximumFractionDigits = 0
        }

        val separator = resourceProvider.getString(R.string.subunit_shares_summary_separator)

        return subunit.memberIds
            .mapNotNull { userId -> subunit.memberShares[userId] }
            .joinToString(separator) { share -> percentFormat.format(share) }
    }
}


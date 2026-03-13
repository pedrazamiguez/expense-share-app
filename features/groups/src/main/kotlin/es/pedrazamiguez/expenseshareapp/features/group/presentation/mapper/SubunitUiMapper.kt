package es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper

import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.MemberUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.SubunitUiModel
import kotlinx.collections.immutable.ImmutableList

interface SubunitUiMapper {

    fun toSubunitUiModel(
        subunit: Subunit,
        memberProfiles: Map<String, User>
    ): SubunitUiModel

    fun toSubunitUiModelList(
        subunits: List<Subunit>,
        memberProfiles: Map<String, User>
    ): ImmutableList<SubunitUiModel>

    /**
     * Builds the member selection list for the form dialog.
     *
     * @param memberIds All group member IDs.
     * @param memberProfiles Resolved user profiles.
     * @param subunits Existing subunits in the group.
     * @param excludeSubunitId When editing, exclude this subunit from the "already assigned" check.
     */
    fun toMemberUiModelList(
        memberIds: List<String>,
        memberProfiles: Map<String, User>,
        subunits: List<Subunit>,
        excludeSubunitId: String? = null
    ): ImmutableList<MemberUiModel>
}


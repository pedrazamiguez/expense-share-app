package es.pedrazamiguez.splittrip.features.subunit.presentation.mapper

import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.subunit.presentation.model.MemberUiModel
import es.pedrazamiguez.splittrip.features.subunit.presentation.model.SubunitUiModel
import java.math.BigDecimal
import kotlinx.collections.immutable.ImmutableList

interface SubunitUiMapper {

    fun toSubunitUiModel(subunit: Subunit, memberProfiles: Map<String, User>): SubunitUiModel

    fun toSubunitUiModelList(subunits: List<Subunit>, memberProfiles: Map<String, User>): ImmutableList<SubunitUiModel>

    /**
     * Builds the member selection list for the form.
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

    /**
     * Formats a domain share value (0–1) as a locale-aware percentage string
     * for display in form inputs. E.g., BigDecimal("0.5") → "50", BigDecimal("0.3333") → "33.33".
     */
    fun formatShareAsPercentage(share: BigDecimal): String

    /**
     * Formats a BigDecimal percentage (0–100) as a locale-aware string
     * for display in form inputs. E.g., BigDecimal("50") → "50", BigDecimal("33.33") → "33,33" (Spanish).
     */
    fun formatPercentageForInput(percentage: BigDecimal): String
}

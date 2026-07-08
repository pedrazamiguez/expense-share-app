package es.pedrazamiguez.splittrip.core.designsystem.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.enums.SelfIdentificationContext
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.common.util.DisplayNameResolver
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.MemberOptionUiModel
import es.pedrazamiguez.splittrip.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Presentation-layer mapper for User domain models in the core design-system.
 *
 * Provides shared formatting and resolution for User profiles.
 */
class UserUiMapper(
    private val resourceProvider: ResourceProvider
) {

    /**
     * Resolves the display name for a [User] domain object, falling back through the hierarchy:
     * displayName -> email -> userId.
     *
     * When [currentUserId] matches [fallbackUserId] and [selfIdentificationContext] is provided,
     * returns a localized second-person pronoun (e.g. "You" / "Tú") instead of the display name.
     *
     * @param user                     The [User] profile, or null if not resolved.
     * @param fallbackUserId           The fallback user ID (used if [user] is null).
     * @param currentUserId            The authenticated user's ID. When equal to [fallbackUserId], returns [youLabel] or self-identification pronoun.
     * @param youLabel                 Explicit localized label for the current user (e.g. "You"). Takes precedence over [selfIdentificationContext] when non-blank.
     * @param selfIdentificationContext Grammatical context for self-identification pronoun, used when [youLabel] is blank.
     */
    fun mapToDisplayName(
        user: User?,
        fallbackUserId: String,
        currentUserId: String? = null,
        youLabel: String = "",
        selfIdentificationContext: SelfIdentificationContext? = null
    ): String {
        val effectiveYouLabel = when {
            youLabel.isNotBlank() -> youLabel
            selfIdentificationContext != null && currentUserId != null -> mapToSelfIdentification(
                selfIdentificationContext
            )
            else -> ""
        }
        return DisplayNameResolver.resolve(
            userId = fallbackUserId,
            currentUserId = if (effectiveYouLabel.isNotBlank()) currentUserId else null,
            youLabel = effectiveYouLabel,
            displayName = user?.displayName,
            email = user?.email.orEmpty(),
            pendingLabel = resourceProvider.getString(R.string.user_pending_fallback)
        )
    }

    /**
     * Resolves the display name for a non-null [User] domain object.
     */
    fun mapToDisplayName(
        user: User,
        currentUserId: String? = null,
        youLabel: String = ""
    ): String {
        return mapToDisplayName(
            user = user,
            fallbackUserId = user.userId,
            currentUserId = currentUserId,
            youLabel = youLabel
        )
    }

    /**
     * Resolves a self-identification pronoun string based on the grammatical context.
     *
     * @param context The grammatical context (NOMINATIVE, BENEFICIARY, AGENT, RECIPIENT).
     * @return Localized pronoun string for the current user.
     */
    fun mapToSelfIdentification(context: SelfIdentificationContext): String {
        return when (context) {
            SelfIdentificationContext.NOMINATIVE -> resourceProvider.getString(R.string.self_identification_nominative)
            SelfIdentificationContext.BENEFICIARY -> resourceProvider.getString(
                R.string.self_identification_beneficiary
            )
            SelfIdentificationContext.AGENT -> resourceProvider.getString(R.string.self_identification_agent)
            SelfIdentificationContext.RECIPIENT -> resourceProvider.getString(R.string.self_identification_recipient)
        }
    }

    /**
     * Maps a list of member user IDs and their profiles to [MemberOptionUiModel] items
     * for display in member pickers.
     *
     * @param memberIds     Group member user IDs.
     * @param memberProfiles Resolved profiles keyed by userId.
     * @param currentUserId The authenticated user's ID.
     */
    fun toMemberOptions(
        memberIds: List<String>,
        memberProfiles: Map<String, User>,
        currentUserId: String?
    ): ImmutableList<MemberOptionUiModel> = memberIds.map { memberId ->
        val user = memberProfiles[memberId]
        MemberOptionUiModel(
            userId = memberId,
            displayName = mapToDisplayName(
                user = user,
                fallbackUserId = memberId
            ),
            isCurrentUser = memberId == currentUserId
        )
    }.toImmutableList()
}

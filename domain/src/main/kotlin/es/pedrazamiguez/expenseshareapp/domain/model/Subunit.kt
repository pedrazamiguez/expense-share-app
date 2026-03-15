package es.pedrazamiguez.expenseshareapp.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Represents a logical sub-grouping of members within a travel group.
 *
 * Sub-units model real-world travel arrangements: solo travelers, couples,
 * families, or any other grouping where one member may contribute or pay
 * expenses on behalf of the entire unit.
 *
 * A member can belong to at most **one** sub-unit within a group.
 * Members not assigned to any sub-unit are treated as solo participants.
 *
 * @param id Unique identifier (UUID generated locally).
 * @param groupId The group this sub-unit belongs to.
 * @param name Human-readable label (e.g., "Antonio & Me", "Ana's Family").
 * @param memberIds Denormalized list of user IDs belonging to this sub-unit.
 * @param memberShares Weight ratios per member (userId → weight) as [BigDecimal].
 *        Values should be normalized so they sum to 1; normalization
 *        is enforced by `SubunitValidationService`, not by this data class.
 *        Example: couple → {userA: 0.5, userB: 0.5},
 *        family of 3 → {parent1: 0.4, parent2: 0.3, child: 0.3}.
 *        When empty but [memberIds] is populated, equal shares are implied.
 * @param createdBy The userId who created this sub-unit.
 * @param createdAt Timestamp of creation.
 * @param lastUpdatedAt Timestamp of the last modification.
 */
data class Subunit(
    val id: String = "",
    val groupId: String = "",
    val name: String = "",
    val memberIds: List<String> = emptyList(),
    val memberShares: Map<String, BigDecimal> = emptyMap(),
    val createdBy: String = "",
    val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null
)

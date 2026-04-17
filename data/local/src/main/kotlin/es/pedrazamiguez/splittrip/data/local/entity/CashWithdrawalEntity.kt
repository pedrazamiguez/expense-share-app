package es.pedrazamiguez.splittrip.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cash_withdrawals",
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["groupId", "syncStatus"]),
        // FIFO query indexes: one per scoped variant so SQLite can satisfy all
        // leading equality predicates and the remainingAmount range filter.
        // Each index is tailored to the specific extra predicate of its query:
        //   GROUP-scoped:   groupId, currency, withdrawalScope='GROUP', remainingAmount > 0
        //   USER-scoped:    groupId, currency, withdrawalScope='USER', withdrawnBy, remainingAmount > 0
        //   SUBUNIT-scoped: groupId, currency, withdrawalScope='SUBUNIT', subunitId, remainingAmount > 0
        // The scope-blind query (reconciliation/test only) benefits from the GROUP index
        // via leading-column prefix matching.
        Index(value = ["groupId", "currency", "withdrawalScope", "remainingAmount"]),
        Index(value = ["groupId", "currency", "withdrawalScope", "withdrawnBy", "remainingAmount"]),
        Index(value = ["groupId", "currency", "withdrawalScope", "subunitId", "remainingAmount"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CashWithdrawalEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val withdrawnBy: String,
    val createdBy: String = "",
    val withdrawalScope: String = "GROUP",
    val subunitId: String? = null,
    val amountWithdrawn: Long,
    val remainingAmount: Long,
    val currency: String,
    val deductedBaseAmount: Long,
    val exchangeRate: String,
    val createdAtMillis: Long?,
    val lastUpdatedAtMillis: Long?,
    val addOnsJson: String? = null,
    val title: String? = null,
    val notes: String? = null,
    val receiptLocalUri: String? = null,
    val syncStatus: String = "SYNCED"
)

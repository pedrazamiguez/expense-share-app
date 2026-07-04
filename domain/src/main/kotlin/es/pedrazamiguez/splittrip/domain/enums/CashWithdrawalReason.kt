package es.pedrazamiguez.splittrip.domain.enums

enum class CashWithdrawalReason {
    ATM, // standard user-initiated ATM withdrawal (default)
    LEAVE_DEPOSIT, // system-generated: member returns physical cash on group leave
    LEAVE_REIMBURSEMENT; // reserved: group reimburses member for negative cash position

    companion object {
        fun fromString(value: String): CashWithdrawalReason =
            entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown cash withdrawal reason: $value")
    }
}

package es.pedrazamiguez.splittrip.domain.exception

class CannotLeaveGroupException(val reason: Reason) :
    Exception("Cannot leave group: ${reason.name}") {
    enum class Reason {
        NOT_A_MEMBER,
        IS_CREATOR,
        NON_ZERO_POCKET_BALANCE,
        USER_NOT_IN_BALANCES
    }
}

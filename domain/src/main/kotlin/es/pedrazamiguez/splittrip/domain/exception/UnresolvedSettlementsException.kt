package es.pedrazamiguez.splittrip.domain.exception

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord

class UnresolvedSettlementsException(
    val groupId: String,
    val pendingSettlements: List<SettlementRecord>
) : Exception("Group $groupId has ${pendingSettlements.size} unresolved settlement(s)")

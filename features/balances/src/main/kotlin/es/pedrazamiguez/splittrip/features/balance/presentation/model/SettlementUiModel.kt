package es.pedrazamiguez.splittrip.features.balance.presentation.model

import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType

data class SettlementUiModel(
    val debtorId: String,
    val creditorId: String,
    val debtorName: String,
    val creditorName: String,
    val formattedAmount: String,
    val isCurrentUserDebtor: Boolean,
    val isCurrentUserCreditor: Boolean,
    val pocketType: SettlementPocketType,
    val currencyCode: String
)

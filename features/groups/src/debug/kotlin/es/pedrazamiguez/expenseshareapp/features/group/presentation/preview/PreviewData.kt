package es.pedrazamiguez.expenseshareapp.features.group.presentation.preview

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import java.time.LocalDateTime

val GROUP_DOMAIN_1: Group = Group(
    id = "91e5b13f-8e61-4410-bd6d-96872e3c213e",
    name = "Thai 2.0",
    description = "Presupuesto Tailandia 2026",
    currency = "EUR",
    extraCurrencies = listOf("THB"),
    members = listOf(
        "17415e0b-7acb-40af-bd43-e7fd631116a2", "28774fc3-42e8-427f-ab7f-d3fb3642bf71"
    ),
    createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
    lastUpdatedAt = LocalDateTime.of(2026, 1, 1, 2, 0)
)

package es.pedrazamiguez.expenseshareapp.features.group.presentation.preview

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import java.time.LocalDateTime

val GROUP_DOMAIN_1: Group = Group(
    id = "91e5b13f-8e61-4410-bd6d-96872e3c213e",
    name = "Thai 2.0",
    description = "Presupuesto Tailandia 2026",
    currency = "EUR",
    extraCurrencies = listOf("THB"),
    members = listOf(
        "17415e0b-7acb-40af-bd43-e7fd631116a2",
        "28774fc3-42e8-427f-ab7f-d3fb3642bf71"
    ),
    createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
    lastUpdatedAt = LocalDateTime.of(2026, 1, 1, 2, 0)
)

val GROUP_DOMAIN_2: Group = Group(
    id = "b2c4d6e8-f0a1-2345-6789-abcdef012345",
    name = "Summer Trip",
    description = "Beach vacation 2026",
    currency = "USD",
    members = listOf(
        "17415e0b-7acb-40af-bd43-e7fd631116a2",
        "28774fc3-42e8-427f-ab7f-d3fb3642bf71",
        "39885fd4-53f9-538g-cb8g-e4gc4753cg82"
    ),
    createdAt = LocalDateTime.of(2026, 6, 1, 0, 0),
    lastUpdatedAt = LocalDateTime.of(2026, 6, 15, 18, 0)
)

val PREVIEW_GROUPS = listOf(GROUP_DOMAIN_1, GROUP_DOMAIN_2)

val CURRENCY_USD = Currency(
    "USD",
    "$",
    "US Dollar",
    2
)

val CURRENCY_EUR = Currency(
    "EUR",
    "€",
    "Euro",
    2
)

val CURRENCY_MXN = Currency(
    "MXN",
    "$",
    "Mexican Peso",
    2
)

package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.Currency

interface CurrencyRepository {
    suspend fun getCurrencies(): List<Currency>
}
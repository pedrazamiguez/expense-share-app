package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
// FIXME Inject data sources
class CurrencyRepositoryImpl() : CurrencyRepository {
    override suspend fun getCurrencies(): List<Currency> {
        TODO("Not yet implemented")
    }
}

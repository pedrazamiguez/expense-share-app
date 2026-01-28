package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetSupportedCurrenciesUseCase
import org.koin.dsl.module

val currenciesDomainModule = module {
    factory<GetSupportedCurrenciesUseCase> {
        GetSupportedCurrenciesUseCase(currencyRepository = get<CurrencyRepository>())
    }
}

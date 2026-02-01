package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.repository.impl.ExpenseRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import org.koin.dsl.module

val expensesDataModule = module {
    single<ExpenseRepository> {
        ExpenseRepositoryImpl(
            cloudExpenseDataSource = get<CloudExpenseDataSource>(),
            localExpenseDataSource = get<LocalExpenseDataSource>(),
            authenticationService = get<AuthenticationService>()
        )
    }
}

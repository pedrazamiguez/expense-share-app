package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.repository.impl.GroupRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import org.koin.dsl.module

val groupsDataModule = module {
    single<GroupRepository> { GroupRepositoryImpl() }
}

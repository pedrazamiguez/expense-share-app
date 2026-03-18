package es.pedrazamiguez.expenseshareapp.data.di

import androidx.work.WorkManager
import es.pedrazamiguez.expenseshareapp.data.repository.impl.GroupRepositoryImpl
import es.pedrazamiguez.expenseshareapp.data.worker.GroupDeletionRetryScheduler
import es.pedrazamiguez.expenseshareapp.data.worker.GroupDeletionRetrySchedulerImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val groupsDataModule = module {

    single<WorkManager> { WorkManager.getInstance(androidContext()) }

    single<GroupDeletionRetryScheduler> {
        GroupDeletionRetrySchedulerImpl(workManager = get<WorkManager>())
    }

    single<GroupRepository> {
        GroupRepositoryImpl(
            cloudGroupDataSource = get<CloudGroupDataSource>(),
            localGroupDataSource = get<LocalGroupDataSource>(),
            authenticationService = get<AuthenticationService>(),
            groupDeletionRetryScheduler = get<GroupDeletionRetryScheduler>()
        )
    }
}

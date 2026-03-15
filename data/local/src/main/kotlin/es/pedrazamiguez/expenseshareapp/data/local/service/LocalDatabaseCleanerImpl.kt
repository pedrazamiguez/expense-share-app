package es.pedrazamiguez.expenseshareapp.data.local.service

import es.pedrazamiguez.expenseshareapp.data.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.domain.service.LocalDatabaseCleaner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalDatabaseCleanerImpl(
    private val appDatabase: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : LocalDatabaseCleaner {

    override suspend fun clearAll() {
        withContext(ioDispatcher) {
            appDatabase.clearAllTables()
        }
    }
}

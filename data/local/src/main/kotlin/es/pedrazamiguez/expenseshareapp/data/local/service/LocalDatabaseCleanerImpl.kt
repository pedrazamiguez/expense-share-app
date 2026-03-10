package es.pedrazamiguez.expenseshareapp.data.local.service

import es.pedrazamiguez.expenseshareapp.data.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.domain.service.LocalDatabaseCleaner

class LocalDatabaseCleanerImpl(
    private val appDatabase: AppDatabase
) : LocalDatabaseCleaner {

    override suspend fun clearAll() {
        appDatabase.clearAllTables()
    }
}


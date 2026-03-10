package es.pedrazamiguez.expenseshareapp.domain.service

interface LocalDatabaseCleaner {
    suspend fun clearAll()
}


package es.pedrazamiguez.expenseshareapp.domain.service

interface LocalDatabaseCleanerService {
    suspend fun clearAll()
}

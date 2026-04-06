package es.pedrazamiguez.splittrip.domain.service

interface LocalDatabaseCleanerService {
    suspend fun clearAll()
}

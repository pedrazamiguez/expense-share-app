package es.pedrazamiguez.expenseshareapp.domain.service

interface CloudMetadataService {
    suspend fun getAppInstallationId(): Result<String>
}

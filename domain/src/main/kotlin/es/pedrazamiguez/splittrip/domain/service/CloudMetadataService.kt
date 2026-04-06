package es.pedrazamiguez.splittrip.domain.service

interface CloudMetadataService {
    suspend fun getAppInstallationId(): Result<String>
}

package es.pedrazamiguez.splittrip.domain.repository

interface DeviceRepository {
    suspend fun getDeviceToken(): Result<String>
}

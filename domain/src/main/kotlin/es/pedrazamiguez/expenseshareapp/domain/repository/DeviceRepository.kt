package es.pedrazamiguez.expenseshareapp.domain.repository

interface DeviceRepository {
    suspend fun getDeviceToken(): Result<String>
}
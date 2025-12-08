package es.pedrazamiguez.expenseshareapp.domain.provider

interface DeviceTokenProvider {
    suspend fun getDeviceToken(): Result<String>
}

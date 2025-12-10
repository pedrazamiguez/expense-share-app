package es.pedrazamiguez.expenseshareapp.domain.provider

interface AppMetadataProvider {
    val appVersionName: String
    val appVersionCode: Long
    val isEmulator: Boolean
    val deviceModel: String
    val androidVersion: String
}

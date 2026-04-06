package es.pedrazamiguez.splittrip.core.common.provider

interface AppMetadataProvider {
    val appVersionName: String
    val appVersionCode: Long
    val isEmulator: Boolean
    val deviceModel: String
    val androidVersion: String
}

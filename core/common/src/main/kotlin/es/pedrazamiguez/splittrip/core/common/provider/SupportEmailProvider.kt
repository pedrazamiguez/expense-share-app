package es.pedrazamiguez.splittrip.core.common.provider

data class SupportEmail(
    val recipient: String,
    val subject: String,
    val body: String
)

interface SupportEmailProvider {
    fun buildBugReportEmail(): SupportEmail
    fun buildFeatureSuggestionEmail(): SupportEmail
    fun buildContactSupportEmail(): SupportEmail
}

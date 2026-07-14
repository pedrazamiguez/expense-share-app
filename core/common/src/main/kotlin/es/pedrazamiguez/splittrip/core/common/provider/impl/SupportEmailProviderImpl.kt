package es.pedrazamiguez.splittrip.core.common.provider.impl

import es.pedrazamiguez.splittrip.core.common.R
import es.pedrazamiguez.splittrip.core.common.provider.AppMetadataProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.common.provider.SupportEmail
import es.pedrazamiguez.splittrip.core.common.provider.SupportEmailAddressProvider
import es.pedrazamiguez.splittrip.core.common.provider.SupportEmailProvider

class SupportEmailProviderImpl(
    private val appMetadataProvider: AppMetadataProvider,
    private val resourceProvider: ResourceProvider,
    private val supportEmailAddressProvider: SupportEmailAddressProvider
) : SupportEmailProvider {

    override fun buildBugReportEmail(): SupportEmail {
        val systemInfoHeader = resourceProvider.getString(R.string.support_email_system_info_header)
        val appVersionStr = resourceProvider.getString(
            R.string.support_email_app_version,
            appMetadataProvider.appVersionName
        )
        val osVersionStr = resourceProvider.getString(
            R.string.support_email_os_version,
            appMetadataProvider.androidReleaseVersion,
            appMetadataProvider.androidVersion
        )
        val deviceStr = resourceProvider.getString(
            R.string.support_email_device,
            appMetadataProvider.deviceModel
        )

        val emailBody = """
            
            
            -----------------------------------
            $systemInfoHeader
            $appVersionStr
            $osVersionStr
            $deviceStr
        """.trimIndent()

        return SupportEmail(
            recipient = supportEmailAddressProvider.getSupportEmailAddress(),
            subject = resourceProvider.getString(R.string.support_email_subject),
            body = emailBody
        )
    }
}

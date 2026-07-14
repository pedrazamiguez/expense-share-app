package es.pedrazamiguez.splittrip.core.common.provider

import es.pedrazamiguez.splittrip.core.common.R
import es.pedrazamiguez.splittrip.core.common.provider.impl.SupportEmailProviderImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SupportEmailProviderImplTest {

    @Test
    fun `buildBugReportEmail compiles correct support email data`() {
        // Arrange
        val appMetadataProvider = mockk<AppMetadataProvider>()
        every { appMetadataProvider.appVersionName } returns "1.2.3"
        every { appMetadataProvider.androidReleaseVersion } returns "14"
        every { appMetadataProvider.androidVersion } returns "34"
        every { appMetadataProvider.deviceModel } returns "Google Pixel 8"

        val resourceProvider = mockk<ResourceProvider>()
        every { resourceProvider.getString(R.string.support_email_subject) } returns "SplitTrip Bug Report"
        every { resourceProvider.getString(R.string.support_email_system_info_header) } returns
            "System Info (Do not modify):"
        every { resourceProvider.getString(R.string.support_email_app_version, "1.2.3") } returns "App Version: 1.2.3"
        every { resourceProvider.getString(R.string.support_email_os_version, "14", "34") } returns
            "OS Version: Android 14 (API 34)"
        every { resourceProvider.getString(R.string.support_email_device, "Google Pixel 8") } returns
            "Device: Google Pixel 8"

        val provider = SupportEmailProviderImpl(appMetadataProvider, resourceProvider)

        // Act
        val email = provider.buildBugReportEmail()

        // Assert
        assertEquals("support@splittrip.com", email.recipient)
        assertEquals("SplitTrip Bug Report", email.subject)

        // Verify body content format
        val expectedBodyPart = """
            System Info (Do not modify):
            App Version: 1.2.3
            OS Version: Android 14 (API 34)
            Device: Google Pixel 8
        """.trimIndent()

        assertTrue(email.body.contains(expectedBodyPart))
    }
}

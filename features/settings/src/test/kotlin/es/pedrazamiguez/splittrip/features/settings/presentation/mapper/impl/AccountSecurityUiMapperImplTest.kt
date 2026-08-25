package es.pedrazamiguez.splittrip.features.settings.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.domain.enums.AuthProviderType
import es.pedrazamiguez.splittrip.domain.enums.BiometricCapability
import es.pedrazamiguez.splittrip.features.settings.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("AccountSecurityUiMapperImpl")
class AccountSecurityUiMapperImplTest {

    private lateinit var mapper: AccountSecurityUiMapperImpl

    @BeforeEach
    fun setUp() {
        mapper = AccountSecurityUiMapperImpl()
    }

    @Test
    fun `formatProviderLabel returns correct string resource for Google`() {
        val result = mapper.formatProviderLabel(
            providers = listOf(AuthProviderType.GOOGLE),
            isAnonymous = false
        )

        val stringRes = assertInstanceOf(UiText.StringResource::class.java, result)
        assertEquals(R.string.account_security_provider_google, stringRes.resId)
    }

    @Test
    fun `formatProviderLabel returns correct string resource for Email`() {
        val result = mapper.formatProviderLabel(
            providers = listOf(AuthProviderType.EMAIL_PASSWORD),
            isAnonymous = false
        )

        val stringRes = assertInstanceOf(UiText.StringResource::class.java, result)
        assertEquals(R.string.account_security_provider_email, stringRes.resId)
    }

    @Test
    fun `formatProviderLabel returns correct string resource for Multiple providers`() {
        val result = mapper.formatProviderLabel(
            providers = listOf(AuthProviderType.GOOGLE, AuthProviderType.EMAIL_PASSWORD),
            isAnonymous = false
        )

        val stringRes = assertInstanceOf(UiText.StringResource::class.java, result)
        assertEquals(R.string.account_security_provider_multiple, stringRes.resId)
    }

    @Test
    fun `formatProviderLabel returns correct string resource for Guest`() {
        val result = mapper.formatProviderLabel(
            providers = emptyList(),
            isAnonymous = true
        )

        val stringRes = assertInstanceOf(UiText.StringResource::class.java, result)
        assertEquals(R.string.account_security_provider_guest, stringRes.resId)
    }

    @Test
    fun `formatPasswordResetSuccessMessage formats message with user email`() {
        val email = "alex@example.com"
        val result = mapper.formatPasswordResetSuccessMessage(email)

        val stringRes = assertInstanceOf(UiText.StringResource::class.java, result)
        assertEquals(R.string.account_security_password_reset_sent, stringRes.resId)
        assertEquals(1, stringRes.args.size)
        assertEquals(email, stringRes.args[0])
    }

    @Test
    fun `formatBiometricSubtitle returns default description when AVAILABLE`() {
        val result = mapper.formatBiometricSubtitle(BiometricCapability.AVAILABLE)

        val stringRes = assertInstanceOf(UiText.StringResource::class.java, result)
        assertEquals(R.string.account_security_biometric_lock_desc, stringRes.resId)
    }

    @Test
    fun `formatBiometricSubtitle returns no hardware message when NO_HARDWARE`() {
        val result = mapper.formatBiometricSubtitle(BiometricCapability.NO_HARDWARE)

        val stringRes = assertInstanceOf(UiText.StringResource::class.java, result)
        assertEquals(R.string.account_security_biometric_lock_no_hardware, stringRes.resId)
    }

    @Test
    fun `formatBiometricSubtitle returns not enrolled message when NOT_ENROLLED`() {
        val result = mapper.formatBiometricSubtitle(BiometricCapability.NOT_ENROLLED)

        val stringRes = assertInstanceOf(UiText.StringResource::class.java, result)
        assertEquals(R.string.account_security_biometric_lock_not_enrolled, stringRes.resId)
    }

    @Test
    fun `formatBiometricSubtitle returns unavailable message when UNAVAILABLE`() {
        val result = mapper.formatBiometricSubtitle(BiometricCapability.UNAVAILABLE)

        val stringRes = assertInstanceOf(UiText.StringResource::class.java, result)
        assertEquals(R.string.account_security_biometric_lock_unavailable, stringRes.resId)
    }
}

package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.enums.BiometricCapability
import es.pedrazamiguez.splittrip.domain.service.BiometricAuthService
import es.pedrazamiguez.splittrip.domain.usecase.setting.impl.GetBiometricCapabilityUseCaseImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetBiometricCapabilityUseCaseTest {

    private lateinit var biometricAuthService: BiometricAuthService
    private lateinit var useCase: GetBiometricCapabilityUseCase

    @BeforeEach
    fun setUp() {
        biometricAuthService = mockk()
        useCase = GetBiometricCapabilityUseCaseImpl(biometricAuthService)
    }

    @Test
    fun `invoke delegates to biometric auth service and returns AVAILABLE`() {
        every { biometricAuthService.getBiometricCapability() } returns BiometricCapability.AVAILABLE

        val result = useCase()

        assertEquals(BiometricCapability.AVAILABLE, result)
        verify(exactly = 1) { biometricAuthService.getBiometricCapability() }
    }

    @Test
    fun `invoke delegates to biometric auth service and returns NO_HARDWARE`() {
        every { biometricAuthService.getBiometricCapability() } returns BiometricCapability.NO_HARDWARE

        val result = useCase()

        assertEquals(BiometricCapability.NO_HARDWARE, result)
        verify(exactly = 1) { biometricAuthService.getBiometricCapability() }
    }

    @Test
    fun `invoke delegates to biometric auth service and returns NOT_ENROLLED`() {
        every { biometricAuthService.getBiometricCapability() } returns BiometricCapability.NOT_ENROLLED

        val result = useCase()

        assertEquals(BiometricCapability.NOT_ENROLLED, result)
        verify(exactly = 1) { biometricAuthService.getBiometricCapability() }
    }

    @Test
    fun `invoke delegates to biometric auth service and returns UNAVAILABLE`() {
        every { biometricAuthService.getBiometricCapability() } returns BiometricCapability.UNAVAILABLE

        val result = useCase()

        assertEquals(BiometricCapability.UNAVAILABLE, result)
        verify(exactly = 1) { biometricAuthService.getBiometricCapability() }
    }
}

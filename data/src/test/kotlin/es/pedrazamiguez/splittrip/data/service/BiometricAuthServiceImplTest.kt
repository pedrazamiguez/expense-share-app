package es.pedrazamiguez.splittrip.data.service

import android.content.Context
import androidx.biometric.BiometricManager
import es.pedrazamiguez.splittrip.domain.enums.BiometricCapability
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BiometricAuthServiceImplTest {

    private lateinit var context: Context
    private lateinit var biometricManager: BiometricManager
    private lateinit var service: BiometricAuthServiceImpl

    @BeforeEach
    fun setUp() {
        mockkStatic(BiometricManager::class)
        context = mockk()
        biometricManager = mockk()
        every { BiometricManager.from(context) } returns biometricManager
        service = BiometricAuthServiceImpl(context)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `getBiometricCapability returns AVAILABLE when BIOMETRIC_SUCCESS`() {
        every {
            biometricManager.canAuthenticate(any())
        } returns BiometricManager.BIOMETRIC_SUCCESS

        val result = service.getBiometricCapability()

        assertEquals(BiometricCapability.AVAILABLE, result)
    }

    @Test
    fun `getBiometricCapability returns NO_HARDWARE when BIOMETRIC_ERROR_NO_HARDWARE`() {
        every {
            biometricManager.canAuthenticate(any())
        } returns BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE

        val result = service.getBiometricCapability()

        assertEquals(BiometricCapability.NO_HARDWARE, result)
    }

    @Test
    fun `getBiometricCapability returns NOT_ENROLLED when BIOMETRIC_ERROR_NONE_ENROLLED`() {
        every {
            biometricManager.canAuthenticate(any())
        } returns BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED

        val result = service.getBiometricCapability()

        assertEquals(BiometricCapability.NOT_ENROLLED, result)
    }

    @Test
    fun `getBiometricCapability returns UNAVAILABLE when BIOMETRIC_ERROR_HW_UNAVAILABLE`() {
        every {
            biometricManager.canAuthenticate(any())
        } returns BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE

        val result = service.getBiometricCapability()

        assertEquals(BiometricCapability.UNAVAILABLE, result)
    }

    @Test
    fun `getBiometricCapability returns UNAVAILABLE when BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED`() {
        every {
            biometricManager.canAuthenticate(any())
        } returns BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED

        val result = service.getBiometricCapability()

        assertEquals(BiometricCapability.UNAVAILABLE, result)
    }

    @Test
    fun `getBiometricCapability returns UNAVAILABLE when BIOMETRIC_ERROR_UNSUPPORTED`() {
        every {
            biometricManager.canAuthenticate(any())
        } returns BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED

        val result = service.getBiometricCapability()

        assertEquals(BiometricCapability.UNAVAILABLE, result)
    }

    @Test
    fun `getBiometricCapability returns UNAVAILABLE when BIOMETRIC_STATUS_UNKNOWN`() {
        every {
            biometricManager.canAuthenticate(any())
        } returns BiometricManager.BIOMETRIC_STATUS_UNKNOWN

        val result = service.getBiometricCapability()

        assertEquals(BiometricCapability.UNAVAILABLE, result)
    }
}

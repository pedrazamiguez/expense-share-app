package es.pedrazamiguez.splittrip.core.designsystem.biometric

import android.text.TextUtils
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import java.util.concurrent.Executor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("BiometricPromptHelper")
class BiometricPromptHelperTest {

    private val activity = mockk<FragmentActivity>(relaxed = true)
    private val prompt = mockk<BiometricPrompt>(relaxed = true)
    private var capturedCallback: BiometricPrompt.AuthenticationCallback? = null
    private val testExecutor = Executor { it.run() }

    @BeforeEach
    fun setUp() {
        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } answers {
            val arg = firstArg<CharSequence?>()
            arg == null || arg.isEmpty()
        }

        capturedCallback = null
        BiometricPromptHelper.promptFactory = { _, _, callback ->
            capturedCallback = callback
            prompt
        }
        BiometricPromptHelper.executorProvider = { testExecutor }
    }

    @AfterEach
    fun tearDown() {
        BiometricPromptHelper.resetDefaults()
        unmockkStatic(TextUtils::class)
    }

    @Test
    @DisplayName("authenticate launches biometric prompt without crypto object when cryptoObject is null")
    fun `authenticate launches biometric prompt without crypto object when cryptoObject is null`() {
        BiometricPromptHelper.authenticate(
            activity = activity,
            title = "Unlock App",
            subtitle = "Verify Identity",
            negativeButtonText = "Cancel",
            cryptoObject = null,
            onSuccess = {}
        )

        val promptInfoSlot = slot<BiometricPrompt.PromptInfo>()
        verify(exactly = 1) { prompt.authenticate(capture(promptInfoSlot)) }
        verify(exactly = 0) { prompt.authenticate(any(), any()) }
        assertEquals("Unlock App", promptInfoSlot.captured.title)
        assertEquals("Verify Identity", promptInfoSlot.captured.subtitle)
        assertEquals("Cancel", promptInfoSlot.captured.negativeButtonText)
    }

    @Test
    @DisplayName("authenticate launches biometric prompt with crypto object when cryptoObject is provided")
    fun `authenticate launches biometric prompt with crypto object when cryptoObject is provided`() {
        val mockCryptoObject = mockk<BiometricPrompt.CryptoObject>()

        BiometricPromptHelper.authenticate(
            activity = activity,
            title = "Unlock App",
            subtitle = "Verify Identity",
            negativeButtonText = "Cancel",
            cryptoObject = mockCryptoObject,
            onSuccess = {}
        )

        val promptInfoSlot = slot<BiometricPrompt.PromptInfo>()
        verify(exactly = 1) { prompt.authenticate(capture(promptInfoSlot), eq(mockCryptoObject)) }
        assertEquals("Unlock App", promptInfoSlot.captured.title)
        assertEquals("Verify Identity", promptInfoSlot.captured.subtitle)
        assertEquals("Cancel", promptInfoSlot.captured.negativeButtonText)
    }

    @Test
    @DisplayName("onAuthenticationSucceeded callback invokes onSuccess")
    fun `onAuthenticationSucceeded callback invokes onSuccess`() {
        var successCalled = false

        BiometricPromptHelper.authenticate(
            activity = activity,
            title = "Unlock App",
            negativeButtonText = "Cancel",
            onSuccess = { successCalled = true }
        )

        assertNotNull(capturedCallback)
        val authResult = mockk<BiometricPrompt.AuthenticationResult>(relaxed = true)
        capturedCallback?.onAuthenticationSucceeded(authResult)

        assertTrue(successCalled)
    }

    @Test
    @DisplayName("onAuthenticationError callback invokes onError with code and error string")
    fun `onAuthenticationError callback invokes onError with code and error string`() {
        var capturedErrorCode: Int? = null
        var capturedErrorString: CharSequence? = null

        BiometricPromptHelper.authenticate(
            activity = activity,
            title = "Unlock App",
            negativeButtonText = "Cancel",
            onSuccess = {},
            onError = { code, msg ->
                capturedErrorCode = code
                capturedErrorString = msg
            }
        )

        assertNotNull(capturedCallback)
        capturedCallback?.onAuthenticationError(BiometricPrompt.ERROR_LOCKOUT, "Too many attempts")

        assertEquals(BiometricPrompt.ERROR_LOCKOUT, capturedErrorCode)
        assertEquals("Too many attempts", capturedErrorString)
    }

    @Test
    @DisplayName("onAuthenticationFailed callback invokes onFailed")
    fun `onAuthenticationFailed callback invokes onFailed`() {
        var failedCalled = false

        BiometricPromptHelper.authenticate(
            activity = activity,
            title = "Unlock App",
            negativeButtonText = "Cancel",
            onSuccess = {},
            onFailed = { failedCalled = true }
        )

        assertNotNull(capturedCallback)
        capturedCallback?.onAuthenticationFailed()

        assertTrue(failedCalled)
    }

    @Test
    @DisplayName("createCryptoObject catches keystore exceptions and returns null gracefully")
    fun `createCryptoObject catches keystore exceptions and returns null gracefully`() {
        val cryptoObject = BiometricPromptHelper.createCryptoObject()
        assertNull(cryptoObject)
    }

    @Test
    @DisplayName("purgeLegacyKey catches exceptions and cleans up key alias safely")
    fun `purgeLegacyKey catches exceptions and cleans up key alias safely`() {
        assertDoesNotThrow {
            BiometricPromptHelper.purgeLegacyKey()
        }
    }
}

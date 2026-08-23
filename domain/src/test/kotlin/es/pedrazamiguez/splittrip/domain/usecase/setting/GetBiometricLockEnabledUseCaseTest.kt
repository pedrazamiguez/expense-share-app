package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.UserPreferenceRepository
import es.pedrazamiguez.splittrip.domain.usecase.setting.impl.GetBiometricLockEnabledUseCaseImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetBiometricLockEnabledUseCaseTest {

    private lateinit var preferenceRepository: UserPreferenceRepository
    private lateinit var useCase: GetBiometricLockEnabledUseCase

    @BeforeEach
    fun setUp() {
        preferenceRepository = mockk()
        useCase = GetBiometricLockEnabledUseCaseImpl(preferenceRepository)
    }

    @Test
    fun `invoke returns value from preference repository when enabled`() = runTest {
        every { preferenceRepository.getBiometricLockEnabled() } returns flowOf(true)

        val result = useCase().first()

        assertTrue(result)
    }

    @Test
    fun `invoke returns value from preference repository when disabled`() = runTest {
        every { preferenceRepository.getBiometricLockEnabled() } returns flowOf(false)

        val result = useCase().first()

        assertFalse(result)
    }
}

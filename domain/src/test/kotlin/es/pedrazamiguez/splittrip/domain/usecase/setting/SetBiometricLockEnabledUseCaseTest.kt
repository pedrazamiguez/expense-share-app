package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.UserPreferenceRepository
import es.pedrazamiguez.splittrip.domain.usecase.setting.impl.SetBiometricLockEnabledUseCaseImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetBiometricLockEnabledUseCaseTest {

    private lateinit var preferenceRepository: UserPreferenceRepository
    private lateinit var useCase: SetBiometricLockEnabledUseCase

    @BeforeEach
    fun setUp() {
        preferenceRepository = mockk()
        useCase = SetBiometricLockEnabledUseCaseImpl(preferenceRepository)
    }

    @Test
    fun `invoke delegates enabled flag to preference repository`() = runTest {
        coEvery { preferenceRepository.setBiometricLockEnabled(true) } just runs

        useCase(true)

        coVerify(exactly = 1) { preferenceRepository.setBiometricLockEnabled(true) }
    }

    @Test
    fun `invoke delegates disabled flag to preference repository`() = runTest {
        coEvery { preferenceRepository.setBiometricLockEnabled(false) } just runs

        useCase(false)

        coVerify(exactly = 1) { preferenceRepository.setBiometricLockEnabled(false) }
    }
}

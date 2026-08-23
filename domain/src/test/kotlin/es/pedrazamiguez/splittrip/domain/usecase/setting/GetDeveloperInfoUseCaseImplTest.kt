package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.model.DeveloperInfo
import es.pedrazamiguez.splittrip.domain.repository.AppConfigRepository
import es.pedrazamiguez.splittrip.domain.usecase.setting.impl.GetDeveloperInfoUseCaseImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetDeveloperInfoUseCaseImplTest {

    private val appConfigRepository: AppConfigRepository = mockk()
    private val getDeveloperInfoUseCase = GetDeveloperInfoUseCaseImpl(appConfigRepository)

    @Test
    fun `invoke returns developerInfo StateFlow from repository`() {
        val sampleInfo = DeveloperInfo(
            name = "Test Author",
            avatarUrl = "https://example.com/avatar.jpg",
            githubUrl = "https://github.com/author",
            splitTripRepoUrl = "https://github.com/author/repo",
            linkedinUrl = "https://linkedin.com/in/author",
            portfolioUrl = "https://author.dev",
            roleMap = mapOf("en" to "Engineer"),
            bioMap = mapOf("en" to "Bio text"),
            creditsMap = mapOf("en" to "Credits text"),
            copyrightMap = mapOf("en" to "© 2026 Author")
        )
        val stateFlow = MutableStateFlow(sampleInfo)
        every { appConfigRepository.developerInfo } returns stateFlow

        val result = getDeveloperInfoUseCase()

        assertEquals(sampleInfo, result.value)
    }
}

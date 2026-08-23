package es.pedrazamiguez.splittrip.features.settings.presentation.mapper

import es.pedrazamiguez.splittrip.domain.model.DeveloperInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeveloperInfoUiMapperTest {

    private lateinit var mapper: DeveloperInfoUiMapper

    private val developerInfo = DeveloperInfo(
        name = "Andrés Pedraza Míguez",
        avatarUrl = "https://example.com/avatar.jpg",
        githubUrl = "https://github.com/pedrazamiguez",
        splitTripRepoUrl = "https://github.com/pedrazamiguez/split-trip",
        linkedinUrl = "https://www.linkedin.com/in/andres-pedraza-miguez/",
        portfolioUrl = "https://pedrazamiguez.es",
        roleMap = mapOf(
            "en" to "Lead Mobile & Systems Engineer",
            "es" to "Ingeniero Principal de Sistemas y Móviles",
            "es-rAN" to "Inxeniero Prencipá de Citemas y Móvile"
        ),
        bioMap = mapOf(
            "en" to "Passionate Android Engineer.",
            "es" to "Ingeniero Android apasionado.",
            "es-rAN" to "Inxeniero Androi apaçionao."
        ),
        creditsMap = mapOf(
            "en" to "Built with open-source love.",
            "es" to "Creado con amor open-source.",
            "es-rAN" to "Creao con amôh open-source."
        ),
        copyrightMap = mapOf(
            "en" to "© 2026 Andrés Pedraza Míguez. All rights reserved.",
            "es" to "© 2026 Andrés Pedraza Míguez. Todos los derechos reservados.",
            "es-rAN" to "© 2026 Andrés Pedraza Míguez. To los derechos recerbaos."
        )
    )

    @BeforeEach
    fun setUp() {
        mapper = DeveloperInfoUiMapper()
    }

    @Test
    fun `mapToUiState maps English language correctly`() {
        val uiState = mapper.mapToUiState(developerInfo, "en")

        assertEquals("Andrés Pedraza Míguez", uiState.name)
        assertEquals("Lead Mobile & Systems Engineer", uiState.role)
        assertEquals("Passionate Android Engineer.", uiState.bio)
        assertEquals("Built with open-source love.", uiState.credits)
        assertEquals("© 2026 Andrés Pedraza Míguez. All rights reserved.", uiState.copyright)
        assertEquals("https://example.com/avatar.jpg", uiState.avatarUrl)
        assertEquals("https://github.com/pedrazamiguez", uiState.githubUrl)
        assertEquals("https://github.com/pedrazamiguez/split-trip", uiState.splitTripRepoUrl)
        assertEquals("https://www.linkedin.com/in/andres-pedraza-miguez/", uiState.linkedinUrl)
        assertEquals("https://pedrazamiguez.es", uiState.portfolioUrl)
    }

    @Test
    fun `mapToUiState maps Spanish language correctly`() {
        val uiState = mapper.mapToUiState(developerInfo, "es")

        assertEquals("Ingeniero Principal de Sistemas y Móviles", uiState.role)
        assertEquals("Ingeniero Android apasionado.", uiState.bio)
        assertEquals("Creado con amor open-source.", uiState.credits)
        assertEquals("© 2026 Andrés Pedraza Míguez. Todos los derechos reservados.", uiState.copyright)
    }

    @Test
    fun `mapToUiState maps Andaluz language correctly`() {
        val uiState = mapper.mapToUiState(developerInfo, "es-rAN")

        assertEquals("Inxeniero Prencipá de Citemas y Móvile", uiState.role)
        assertEquals("Inxeniero Androi apaçionao.", uiState.bio)
        assertEquals("Creao con amôh open-source.", uiState.credits)
        assertEquals("© 2026 Andrés Pedraza Míguez. To los derechos recerbaos.", uiState.copyright)
    }

    @Test
    fun `mapToUiState falls back to English when language is unknown or null`() {
        val uiStateNull = mapper.mapToUiState(developerInfo, null)
        assertEquals("Lead Mobile & Systems Engineer", uiStateNull.role)

        val uiStateUnknown = mapper.mapToUiState(developerInfo, "fr")
        assertEquals("Lead Mobile & Systems Engineer", uiStateUnknown.role)
    }
}

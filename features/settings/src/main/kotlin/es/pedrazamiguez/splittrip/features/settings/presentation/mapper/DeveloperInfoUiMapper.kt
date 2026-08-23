package es.pedrazamiguez.splittrip.features.settings.presentation.mapper

import es.pedrazamiguez.splittrip.domain.enums.AppLanguage
import es.pedrazamiguez.splittrip.domain.model.DeveloperInfo
import es.pedrazamiguez.splittrip.features.settings.presentation.model.DeveloperInfoUiState

class DeveloperInfoUiMapper {

    fun mapToUiState(developerInfo: DeveloperInfo, languageCode: String?): DeveloperInfoUiState {
        val appLanguage = AppLanguage.fromCode(languageCode)
        val resolvedLang = appLanguage.code
        val fallbackLang = AppLanguage.EN.code

        fun resolveField(map: Map<String, String>): String {
            return map[resolvedLang]
                ?: (if (appLanguage == AppLanguage.ANDALUZ) map["es-rAN"] ?: map["es_rAN"] ?: map["es-AN"] else null)
                ?: map[fallbackLang]
                ?: ""
        }

        val role = resolveField(developerInfo.roleMap)
        val bio = resolveField(developerInfo.bioMap)
        val credits = resolveField(developerInfo.creditsMap)
        val copyright = resolveField(developerInfo.copyrightMap)

        return DeveloperInfoUiState(
            name = developerInfo.name,
            role = role,
            bio = bio,
            avatarUrl = developerInfo.avatarUrl,
            githubUrl = developerInfo.githubUrl,
            splitTripRepoUrl = developerInfo.splitTripRepoUrl,
            linkedinUrl = developerInfo.linkedinUrl,
            portfolioUrl = developerInfo.portfolioUrl,
            credits = credits,
            copyright = copyright
        )
    }
}

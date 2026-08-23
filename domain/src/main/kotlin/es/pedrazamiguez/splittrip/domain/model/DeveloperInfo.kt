package es.pedrazamiguez.splittrip.domain.model

/**
 * Domain model representing developer metadata, social/code links,
 * and multilingual text mappings.
 */
data class DeveloperInfo(
    val name: String,
    val avatarUrl: String,
    val githubUrl: String,
    val splitTripRepoUrl: String,
    val linkedinUrl: String,
    val portfolioUrl: String,
    val roleMap: Map<String, String>,
    val bioMap: Map<String, String>,
    val creditsMap: Map<String, String>,
    val copyrightMap: Map<String, String>
)
